package com.finlux.app.data.remote.firebase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.DealCategory
import com.finlux.app.domain.model.DealFlowType
import com.finlux.app.domain.model.DealStatus
import com.finlux.app.domain.model.FinancialDeal
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.repository.DealRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.util.Date
import java.util.UUID

class FirebaseDealRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : DealRepository {

    override fun observeDeals(): Flow<List<FinancialDeal>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("users").document(uid).collection("deals")
            .orderBy("startDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else {
                    val deals = snapshot?.documents.orEmpty().mapNotNull { it.toFinancialDeal() }
                    trySend(deals)
                }
            }
        awaitClose { registration.remove() }
    }

    override fun observeDeal(dealId: String): Flow<FinancialDeal?> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null || dealId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("users").document(uid).collection("deals").document(dealId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else {
                    trySend(snapshot?.toFinancialDeal())
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun upsertDeal(deal: FinancialDeal): AppResult<String> = firebaseResult("Không thể lưu thương vụ") {
        val uid = requireNotNull(auth.currentUser?.uid) { "Chưa đăng nhập" }
        val id = if (deal.id.isNotBlank()) deal.id else UUID.randomUUID().toString()
        val dealDoc = firestore.collection("users").document(uid).collection("deals").document(id)

        val data = mapOf(
            "title" to deal.title,
            "description" to deal.description,
            "category" to deal.category.name.lowercase(),
            "targetAmount" to deal.targetAmount.value,
            "totalCapitalOutlay" to deal.totalCapitalOutlay.value,
            "totalRecovered" to deal.totalRecovered.value,
            "netProfitLoss" to deal.netProfitLoss.value,
            "status" to deal.status.name.lowercase(),
            "startDate" to Timestamp(Date.from(deal.startDate)),
            "endDate" to deal.endDate?.let { Timestamp(Date.from(it)) },
            "createdAt" to Timestamp(Date.from(deal.createdAt)),
            "updatedAt" to Timestamp.now(),
        )
        dealDoc.set(data).await()
        id
    }

    override suspend fun deleteDeal(dealId: String): AppResult<Unit> = firebaseResult("Không thể xóa thương vụ") {
        val uid = requireNotNull(auth.currentUser?.uid) { "Chưa đăng nhập" }
        val dealDocRef = firestore.collection("users").document(uid).collection("deals").document(dealId)

        val txSnapshot = firestore.collection("users").document(uid).collection("transactions")
            .whereEqualTo("dealId", dealId)
            .get()
            .await()

        val txDocs = txSnapshot.documents
        if (txDocs.isEmpty()) {
            dealDocRef.delete().await()
            return@firebaseResult
        }

        firestore.runTransaction { atomic ->
            // Phase A: All reads first
            val walletIds = txDocs.mapNotNull { it.getString("walletId") }
                .filter { it.isNotBlank() && it != "DEAL_SETTLEMENT" }
                .toSet()
            val walletRefs = walletIds.associateWith {
                firestore.collection("users").document(uid).collection("wallets").document(it)
            }
            val walletDocs = walletRefs.mapValues { (_, ref) -> atomic.get(ref) }

            // Calculate balance adjustment per wallet
            val balanceDeltas = mutableMapOf<String, Long>()
            for (txDoc in txDocs) {
                val flowTypeStr = txDoc.getString("dealFlowType")?.uppercase()
                val walletId = txDoc.getString("walletId").orEmpty()
                val amount = txDoc.getLong("amount") ?: 0L
                if (walletId.isBlank() || walletId == "DEAL_SETTLEMENT" || amount <= 0L) continue

                when (flowTypeStr) {
                    "OUTLAY_CAPITAL" -> {
                        balanceDeltas[walletId] = (balanceDeltas[walletId] ?: 0L) + amount
                    }
                    "PRINCIPAL_RECOVERY", "CAPITAL_GAIN" -> {
                        balanceDeltas[walletId] = (balanceDeltas[walletId] ?: 0L) - amount
                    }
                }
            }

            // Phase B: All writes after reads
            for ((walletId, delta) in balanceDeltas) {
                val doc = walletDocs[walletId] ?: continue
                if (doc.exists()) {
                    val currentBal = doc.getLong("balance") ?: 0L
                    val newBal = currentBal + delta
                    atomic.update(walletRefs.getValue(walletId), "balance", newBal)
                }
            }

            for (txDoc in txDocs) {
                atomic.delete(txDoc.reference)
            }

            atomic.delete(dealDocRef)
        }.await()
    }

    override suspend fun recordDealOutlay(
        deal: FinancialDeal,
        walletId: String,
        amount: Long,
        date: Instant,
        note: String,
    ): AppResult<Unit> = firebaseResult("Không thể ghi nhận khoản xuất vốn") {
        require(amount > 0) { "Số tiền xuất vốn phải lớn hơn 0" }
        val uid = requireNotNull(auth.currentUser?.uid) { "Chưa đăng nhập" }

        val walletRef = firestore.collection("users").document(uid).collection("wallets").document(walletId)
        val dealRef = firestore.collection("users").document(uid).collection("deals").document(deal.id)
        val txId = UUID.randomUUID().toString()
        val txRef = firestore.collection("users").document(uid).collection("transactions").document(txId)

        firestore.runTransaction { tx ->
            val walletDoc = tx.get(walletRef)
            require(walletDoc.exists()) { "Ví không tồn tại" }
            val currentBalance = walletDoc.getLong("balance") ?: 0L
            val walletType = walletDoc.getString("type")
            val isCreditCard = walletType.equals("CARD", ignoreCase = true)
            require(isCreditCard || currentBalance >= amount) {
                "Số dư ví không đủ để xuất vốn cho thương vụ"
            }

            val dealDoc = tx.get(dealRef)
            require(dealDoc.exists()) { "Thương vụ không tồn tại" }
            val currentOutlay = dealDoc.getLong("totalCapitalOutlay") ?: 0L

            // 1. Trừ tiền ví
            tx.update(
                walletRef,
                mapOf(
                    "balance" to currentBalance - amount,
                    "lastTransactionId" to txId,
                )
            )

            // 2. Tăng vốn đã xuất của Deal
            tx.update(
                dealRef,
                "totalCapitalOutlay", currentOutlay + amount,
                "status", DealStatus.ACTIVE.name.lowercase(),
                "updatedAt", Timestamp.now()
            )

            // 3. Ghi giao dịch Sổ cái
            val txData = mapOf(
                "type" to TransactionType.EXPENSE.name.lowercase(),
                "amount" to amount,
                "categoryId" to null,
                "walletId" to walletId,
                "relatedWalletId" to null,
                "dealId" to deal.id,
                "dealFlowType" to DealFlowType.OUTLAY_CAPITAL.name.lowercase(),
                "note" to note.ifBlank { buildDefaultNote(deal, DealFlowType.OUTLAY_CAPITAL) },
                "receiptImageUrl" to null,
                "date" to Timestamp(Date.from(date)),
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now(),
            )
            tx.set(txRef, txData)
        }.await()
    }

    override suspend fun recordDealInflow(
        deal: FinancialDeal,
        walletId: String,
        amount: Long,
        date: Instant,
        note: String,
    ): AppResult<Unit> = firebaseResult("Không thể ghi nhận khoản thu hồi") {
        require(amount > 0) { "Số tiền thu hồi phải lớn hơn 0" }
        val uid = requireNotNull(auth.currentUser?.uid) { "Chưa đăng nhập" }

        val walletRef = firestore.collection("users").document(uid).collection("wallets").document(walletId)
        val dealRef = firestore.collection("users").document(uid).collection("deals").document(deal.id)
        val walletLedgerTransactionId = UUID.randomUUID().toString()

        firestore.runTransaction { tx ->
            val walletDoc = tx.get(walletRef)
            require(walletDoc.exists()) { "Ví không tồn tại" }
            val currentBalance = walletDoc.getLong("balance") ?: 0L

            val dealDoc = tx.get(dealRef)
            require(dealDoc.exists()) { "Thương vụ không tồn tại" }
            val totalOutlay = dealDoc.getLong("totalCapitalOutlay") ?: 0L
            val totalRecovered = dealDoc.getLong("totalRecovered") ?: 0L
            val currentProfitLoss = dealDoc.getLong("netProfitLoss") ?: 0L

            val remainingCapital = (totalOutlay - totalRecovered).coerceAtLeast(0L)

            // 1. Cộng toàn bộ tiền vào ví và liên kết mutation với transaction ledger.
            tx.update(
                walletRef,
                mapOf(
                    "balance" to currentBalance + amount,
                    "lastTransactionId" to walletLedgerTransactionId,
                )
            )

            // 2. Phân rã dòng tiền
            if (amount <= remainingCapital) {
                // Thu về <= Vốn còn lại: 100% Hoàn gốc
                val newRecovered = totalRecovered + amount
                val newStatus = if (newRecovered >= totalOutlay && totalOutlay > 0) DealStatus.COMPLETED.name.lowercase() else DealStatus.ACTIVE.name.lowercase()

                tx.update(
                    dealRef,
                    "totalRecovered", newRecovered,
                    "status", newStatus,
                    "updatedAt", Timestamp.now()
                )

                val txRef = firestore.collection("users").document(uid).collection("transactions").document(walletLedgerTransactionId)
                val txData = mapOf(
                    "type" to TransactionType.INCOME.name.lowercase(),
                    "amount" to amount,
                    "categoryId" to null,
                    "walletId" to walletId,
                    "relatedWalletId" to null,
                    "dealId" to deal.id,
                    "dealFlowType" to DealFlowType.PRINCIPAL_RECOVERY.name.lowercase(),
                    "note" to note.ifBlank { buildDefaultNote(deal, DealFlowType.PRINCIPAL_RECOVERY, isSplitPrincipal = false) },
                    "receiptImageUrl" to null,
                    "date" to Timestamp(Date.from(date)),
                    "createdAt" to Timestamp.now(),
                    "updatedAt" to Timestamp.now(),
                )
                tx.set(txRef, txData)
            } else {
                // Thu về > Vốn còn lại: Tách Vốn gốc & Lợi nhuận ròng
                val principalPortion = remainingCapital
                val gainPortion = amount - remainingCapital

                tx.update(
                    dealRef,
                    "totalRecovered", totalOutlay,
                    "netProfitLoss", currentProfitLoss + gainPortion,
                    "status", DealStatus.COMPLETED.name.lowercase(),
                    "updatedAt", Timestamp.now()
                )

                if (principalPortion > 0) {
                    val txId1 = UUID.randomUUID().toString()
                    val txRef1 = firestore.collection("users").document(uid).collection("transactions").document(txId1)
                    val txData1 = mapOf(
                        "type" to TransactionType.INCOME.name.lowercase(),
                        "amount" to principalPortion,
                        "categoryId" to null,
                        "walletId" to walletId,
                        "relatedWalletId" to null,
                        "dealId" to deal.id,
                        "dealFlowType" to DealFlowType.PRINCIPAL_RECOVERY.name.lowercase(),
                        "note" to note.ifBlank { buildDefaultNote(deal, DealFlowType.PRINCIPAL_RECOVERY, isSplitPrincipal = true) },
                        "receiptImageUrl" to null,
                        "date" to Timestamp(Date.from(date)),
                        "createdAt" to Timestamp.now(),
                        "updatedAt" to Timestamp.now(),
                    )
                    tx.set(txRef1, txData1)
                }

                val txRef2 = firestore.collection("users").document(uid).collection("transactions").document(walletLedgerTransactionId)
                val txData2 = mapOf(
                    "type" to TransactionType.INCOME.name.lowercase(),
                    "amount" to gainPortion,
                    "categoryId" to null,
                    "walletId" to walletId,
                    "relatedWalletId" to null,
                    "dealId" to deal.id,
                    "dealFlowType" to DealFlowType.CAPITAL_GAIN.name.lowercase(),
                    "note" to note.ifBlank { buildDefaultNote(deal, DealFlowType.CAPITAL_GAIN) },
                    "receiptImageUrl" to null,
                    "date" to Timestamp(Date.from(date)),
                    "createdAt" to Timestamp.now(),
                    "updatedAt" to Timestamp.now(),
                )
                tx.set(txRef2, txData2)
            }
        }.await()
    }

    override suspend fun closeDealWithLoss(
        deal: FinancialDeal,
        date: Instant,
        note: String,
    ): AppResult<Unit> = firebaseResult("Không thể chốt lỗ thương vụ") {
        val uid = requireNotNull(auth.currentUser?.uid) { "Chưa đăng nhập" }
        val dealRef = firestore.collection("users").document(uid).collection("deals").document(deal.id)

        firestore.runTransaction { tx ->
            val dealDoc = tx.get(dealRef)
            require(dealDoc.exists()) { "Thương vụ không tồn tại" }
            val totalOutlay = dealDoc.getLong("totalCapitalOutlay") ?: 0L
            val totalRecovered = dealDoc.getLong("totalRecovered") ?: 0L
            val currentProfitLoss = dealDoc.getLong("netProfitLoss") ?: 0L

            val lossAmount = (totalOutlay - totalRecovered).coerceAtLeast(0L)

            tx.update(
                dealRef,
                "netProfitLoss", currentProfitLoss - lossAmount,
                "status", DealStatus.COMPLETED.name.lowercase(),
                "endDate", Timestamp(Date.from(date)),
                "updatedAt", Timestamp.now()
            )

            if (lossAmount > 0) {
                val txId = UUID.randomUUID().toString()
                val txRef = firestore.collection("users").document(uid).collection("transactions").document(txId)
                val txData = mapOf(
                    "type" to TransactionType.EXPENSE.name.lowercase(),
                    "amount" to lossAmount,
                    "categoryId" to null,
                    "walletId" to "DEAL_SETTLEMENT",
                    "relatedWalletId" to null,
                    "dealId" to deal.id,
                    "dealFlowType" to DealFlowType.CAPITAL_LOSS.name.lowercase(),
                    "note" to note.ifBlank { buildDefaultNote(deal, DealFlowType.CAPITAL_LOSS) },
                    "receiptImageUrl" to null,
                    "date" to Timestamp(Date.from(date)),
                    "createdAt" to Timestamp.now(),
                    "updatedAt" to Timestamp.now(),
                )
                tx.set(txRef, txData)
            }
        }.await()
    }

    override suspend fun revertDealLoss(dealId: String): AppResult<Unit> = firebaseResult("Không thể thu hồi chốt lỗ") {
        val uid = requireNotNull(auth.currentUser?.uid) { "Chưa đăng nhập" }
        val lossDocs = firestore.collection("users").document(uid).collection("transactions")
            .whereEqualTo("dealId", dealId)
            .whereEqualTo("dealFlowType", DealFlowType.CAPITAL_LOSS.name.lowercase())
            .get()
            .await()

        val totalLossToRevert = lossDocs.documents.sumOf { it.getLong("amount") ?: 0L }
        val dealRef = firestore.collection("users").document(uid).collection("deals").document(dealId)

        firestore.runTransaction { tx ->
            val dealDoc = tx.get(dealRef)
            if (dealDoc.exists()) {
                val currentProfitLoss = dealDoc.getLong("netProfitLoss") ?: 0L
                tx.update(
                    dealRef,
                    "netProfitLoss", currentProfitLoss + totalLossToRevert,
                    "status", DealStatus.ACTIVE.name.lowercase(),
                    "endDate", null,
                    "updatedAt", Timestamp.now(),
                )
            }
            for (doc in lossDocs.documents) {
                tx.delete(doc.reference)
            }
        }.await()
    }

    override suspend fun reopenDeal(dealId: String): AppResult<Unit> = firebaseResult("Không thể mở lại thương vụ") {
        val uid = requireNotNull(auth.currentUser?.uid) { "Chưa đăng nhập" }
        val dealRef = firestore.collection("users").document(uid).collection("deals").document(dealId)
        dealRef.update(
            mapOf(
                "status" to DealStatus.ACTIVE.name.lowercase(),
                "endDate" to null,
                "updatedAt" to Timestamp.now(),
            )
        ).await()
    }

    /**
     * Build ghi chú mặc định cho giao dịch Deal theo category và tên thương vụ.
     * Định dạng: "[<Category>] <Hành động>: <Tên thương vụ>"
     *
     * @param isSplitPrincipal true khi PRINCIPAL_RECOVERY được tạo trong nhánh tách đôi
     *   (tống hoà về > vốn còn lại) để phân biệt "Thu hồi vốn" vs "Thu hồi vốn gốc".
     */
    private fun buildDefaultNote(
        deal: FinancialDeal,
        flowType: DealFlowType,
        isSplitPrincipal: Boolean = false,
    ): String {
        val prefix = when (deal.category) {
            DealCategory.INVESTMENT -> "[Đầu tư]"
            DealCategory.LENDING    -> "[Cho vay]"
        }
        val action = when (deal.category) {
            DealCategory.INVESTMENT -> when (flowType) {
                DealFlowType.OUTLAY_CAPITAL     -> "Xuất vốn"
                DealFlowType.PRINCIPAL_RECOVERY -> if (isSplitPrincipal) "Thu hồi vốn" else "Thu hồi vốn gốc"
                DealFlowType.CAPITAL_GAIN       -> "Lợi nhuận"
                DealFlowType.CAPITAL_LOSS       -> "Lỗ vốn"
            }
            DealCategory.LENDING -> when (flowType) {
                DealFlowType.OUTLAY_CAPITAL     -> "Xuất vốn vay"
                DealFlowType.PRINCIPAL_RECOVERY -> "Thu hồi vốn vay"
                DealFlowType.CAPITAL_GAIN       -> "Lợi nhuận vay"
                DealFlowType.CAPITAL_LOSS       -> "Mất vốn"
            }
        }
        return "$prefix $action: ${deal.title}"
    }

    private fun DocumentSnapshot.toFinancialDeal(): FinancialDeal? = runCatching {
        val rawStatus = getString("status") ?: DealStatus.ACTIVE.name
        val status = runCatching { DealStatus.valueOf(rawStatus.uppercase()) }.getOrDefault(DealStatus.ACTIVE)
        val rawCategory = getString("category") ?: DealCategory.INVESTMENT.name
        val category = runCatching { DealCategory.valueOf(rawCategory.uppercase()) }.getOrDefault(DealCategory.INVESTMENT)

        FinancialDeal(
            id = id,
            userId = auth.currentUser?.uid.orEmpty(),
            title = getString("title").orEmpty(),
            description = getString("description").orEmpty(),
            category = category,
            targetAmount = Money(getLong("targetAmount") ?: 0L),
            totalCapitalOutlay = Money(getLong("totalCapitalOutlay") ?: 0L),
            totalRecovered = Money(getLong("totalRecovered") ?: 0L),
            netProfitLoss = Money(getLong("netProfitLoss") ?: 0L),
            status = status,
            startDate = getTimestamp("startDate")?.toDate()?.toInstant() ?: Instant.now(),
            endDate = getTimestamp("endDate")?.toDate()?.toInstant(),
            createdAt = getTimestamp("createdAt")?.toDate()?.toInstant() ?: Instant.now(),
            updatedAt = getTimestamp("updatedAt")?.toDate()?.toInstant() ?: Instant.now(),
        )
    }.getOrNull()
}
