package com.finlux.app.data.remote.firebase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.DEBT_PAYMENT_CATEGORY_ID
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtPaymentHistory
import com.finlux.app.domain.model.DebtType
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.DebtRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.util.Date
import java.util.UUID

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FirebaseDebtRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : DebtRepository {

    override fun observeDebts(): Flow<List<DebtAccount>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("users").document(uid).collection("debts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else {
                    // Silent Self-Healing: Âm thầm xóa bỏ dueDate=1 cũ của PERSONAL_LOAN trên Firestore server
                    snapshot?.documents.orEmpty().forEach { doc ->
                        val debtTypeStr = doc.getString("type")
                        val rawDue = doc.getLong("dueDate")
                        if (debtTypeStr == DebtType.PERSONAL_LOAN.name && rawDue == 1L) {
                            runCatching {
                                doc.reference.update(mapOf("dueDate" to null))
                            }
                        }
                    }
                    trySend(snapshot?.documents.orEmpty().mapNotNull { it.toDebtAccount() })
                }
            }
        awaitClose { registration.remove() }
    }

    override fun observePaymentHistory(debtId: String): Flow<List<DebtPaymentHistory>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null || debtId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("users").document(uid)
            .collection("debts").document(debtId)
            .collection("payments")
            .orderBy("paymentDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) close(error)
                else trySend(snapshot?.documents.orEmpty().mapNotNull { it.toDebtPaymentHistory() })
            }
        awaitClose { registration.remove() }
    }

    override fun observeAllPaymentHistory(): Flow<List<DebtPaymentHistory>> =
        observeDebts().flatMapLatest { debts ->
            if (debts.isEmpty()) {
                flowOf(emptyList())
            } else {
                val flows = debts.map { debt -> observePaymentHistory(debt.id) }
                combine(flows) { arrays ->
                    arrays.flatMap { it }.sortedByDescending { it.paymentDate }
                }
            }
        }

    override suspend fun upsertDebt(debt: DebtAccount): AppResult<String> = firebaseResult("Không thể lưu khoản nợ") {
        val uid = requireUid()
        val id = debt.id.ifBlank { UUID.randomUUID().toString() }
        firestore.collection("users").document(uid).collection("debts").document(id)
            .set(debt.copy(id = id, userId = uid).toDebtMap()).await()
        id
    }

    override suspend fun deleteDebt(debt: DebtAccount): AppResult<Unit> = firebaseResult("Không thể xóa khoản nợ") {
        val uid = requireUid()
        firestore.collection("users").document(uid).collection("debts").document(debt.id).delete().await()
        Unit
    }

    override suspend fun processPayment(
        debtId: String,
        walletId: String,
        amount: Long,
        principalPaid: Long,
        interestPaid: Long,
        note: String,
        paymentDate: Instant,
    ): AppResult<Unit> = firebaseResult("Không thể thực hiện thanh toán nợ") {
        val uid = requireUid()
        val userDoc = firestore.collection("users").document(uid)
        val walletRef = userDoc.collection("wallets").document(walletId)
        val debtRef = userDoc.collection("debts").document(debtId)
        val paymentId = UUID.randomUUID().toString()
        val paymentRef = debtRef.collection("payments").document(paymentId)

        firestore.runTransaction { tx ->
            val walletSnap = tx.get(walletRef)
            if (!walletSnap.exists()) throw IllegalArgumentException("Ví thanh toán không tồn tại")

            val currentWalletBalance = walletSnap.getLong("balance") ?: 0L
            val walletTypeStr = walletSnap.getString("type") ?: WalletType.CASH.name
            val isCreditCardSource = walletTypeStr == WalletType.CARD.name

            if (!isCreditCardSource && currentWalletBalance < amount) {
                throw IllegalArgumentException("Số dư ví không đủ để thanh toán nợ")
            }

            val debtSnap = tx.get(debtRef)
            if (!debtSnap.exists()) throw IllegalArgumentException("Khoản nợ không tồn tại")

            val debtName = debtSnap.getString("name") ?: "Khoản nợ"
            val debtTypeStr = debtSnap.getString("type") ?: DebtType.PERSONAL_LOAN.name
            val linkedWalletId = debtSnap.getString("linkedWalletId")
            val isLinkedCard = debtTypeStr == DebtType.CREDIT_CARD.name && !linkedWalletId.isNullOrBlank()

            val currentDebtRemaining = debtSnap.getLong("remainingBalance") ?: 0L
            val newDebtRemaining = (currentDebtRemaining - principalPaid).coerceAtLeast(0L)
            val isSettled = newDebtRemaining <= 0L

            // 1. Cập nhật dư nợ tài khoản nợ
            tx.update(
                debtRef,
                mapOf(
                    "remainingBalance" to newDebtRemaining,
                    "isSettled" to isSettled,
                    "updatedAt" to Timestamp(Date.from(paymentDate)),
                )
            )

            // 2. Ghi log lịch sử trả nợ (lưu chi tiết Gốc, Lãi & Phân loại)
            tx.set(
                paymentRef,
                mapOf(
                    "debtId" to debtId,
                    "walletId" to walletId,
                    "amount" to amount,
                    "principalPaid" to principalPaid,
                    "interestPaid" to interestPaid,
                    "paymentDate" to Timestamp(Date.from(paymentDate)),
                    "note" to note,
                    "isCreditCardPayment" to isLinkedCard,
                )
            )

            if (isLinkedCard && linkedWalletId != null) {
                // VÒNG ĐỜI THẺ TÍN DỤNG: Chuyển tiền (TRANSFER) từ ví thanh toán sang ví thẻ để hoàn hạn mức
                val linkedWalletRef = userDoc.collection("wallets").document(linkedWalletId)
                val linkedWalletSnap = tx.get(linkedWalletRef)
                if (linkedWalletSnap.exists()) {
                    val currentCardBalance = linkedWalletSnap.getLong("balance") ?: 0L
                    tx.update(walletRef, "balance", currentWalletBalance - amount)
                    tx.update(linkedWalletRef, "balance", currentCardBalance + amount)

                    val pairId = UUID.randomUUID().toString()
                    val outRef = userDoc.collection("transactions").document("${pairId}_out")
                    val inRef = userDoc.collection("transactions").document("${pairId}_in")
                    val transferNote = if (note.isNotBlank()) note else "Thanh toán sao kê thẻ: $debtName"

                    tx.set(
                        outRef,
                        mapOf(
                            "type" to TransactionType.TRANSFER_OUT.name,
                            "amount" to amount,
                            "walletId" to walletId,
                            "relatedWalletId" to linkedWalletId,
                            "note" to transferNote,
                            "receiptImageUrl" to null,
                            "date" to Timestamp(Date.from(paymentDate)),
                            "createdAt" to Timestamp(Date.from(paymentDate)),
                            "updatedAt" to Timestamp(Date.from(paymentDate)),
                        )
                    )
                    tx.set(
                        inRef,
                        mapOf(
                            "type" to TransactionType.TRANSFER_IN.name,
                            "amount" to amount,
                            "walletId" to linkedWalletId,
                            "relatedWalletId" to walletId,
                            "note" to transferNote,
                            "receiptImageUrl" to null,
                            "date" to Timestamp(Date.from(paymentDate)),
                            "createdAt" to Timestamp(Date.from(paymentDate)),
                            "updatedAt" to Timestamp(Date.from(paymentDate)),
                        )
                    )
                }
            } else {
                // KHOẢN VAY THÔNG THƯỜNG: Trừ ví nguồn và ghi transaction sổ cái
                tx.update(walletRef, "balance", currentWalletBalance - amount)

                val defaultNote = if (interestPaid > 0 && principalPaid == 0L) {
                    "Trả lãi khoản vay: $debtName"
                } else {
                    "Thanh toán nợ: $debtName"
                }
                val txNote = if (note.isNotBlank()) note else defaultNote
                val categoryId = DEBT_PAYMENT_CATEGORY_ID

                val transactionRef = userDoc.collection("transactions").document(UUID.randomUUID().toString())
                tx.set(
                    transactionRef,
                    mapOf(
                        "type" to TransactionType.EXPENSE.name,
                        "amount" to amount,
                        "walletId" to walletId,
                        "categoryId" to categoryId,
                        "note" to txNote,
                        "receiptImageUrl" to null,
                        "date" to Timestamp(Date.from(paymentDate)),
                        "createdAt" to Timestamp(Date.from(paymentDate)),
                        "updatedAt" to Timestamp(Date.from(paymentDate)),
                    )
                )
            }
        }.await()
        Unit
    }

    private fun requireUid(): String = auth.currentUser?.uid ?: error("Phiên đăng nhập đã hết hạn")
}

internal fun DebtAccount.toDebtMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "type" to type.name,
    "totalAmount" to totalAmount.value,
    "remainingBalance" to remainingBalance.value,
    "interestRateApr" to interestRateApr,
    "minimumPayment" to minimumPayment.value,
    "dueDate" to if (type == DebtType.PERSONAL_LOAN && dueDate == 1) null else dueDate,
    "statementDate" to statementDate,
    "linkedWalletId" to linkedWalletId,
    "gracePeriodDays" to gracePeriodDays,
    "colorHex" to colorHex,
    "isReminderEnabled" to isReminderEnabled,
    "reminderDaysBefore" to reminderDaysBefore,
    "isSettled" to isSettled,
    "createdAt" to Timestamp(Date.from(createdAt)),
    "updatedAt" to Timestamp(Date.from(updatedAt)),
)

internal fun DocumentSnapshot.toDebtAccount(): DebtAccount? = runCatching {
    val debtType = DebtType.valueOf(getString("type") ?: DebtType.PERSONAL_LOAN.name)
    val rawDueDate = getLong("dueDate")?.toInt()?.takeIf { it in 1..31 }
    // In-Memory Sanitization: Nợ cá nhân bị gán dueDate = 1 rác từ bản build cũ được tự động chuẩn hóa về null ngay trên bộ nhớ
    val sanitizedDueDate = if (debtType == DebtType.PERSONAL_LOAN && rawDueDate == 1) {
        null
    } else {
        rawDueDate
    }

    DebtAccount(
        id = id,
        userId = getString("userId").orEmpty(),
        name = requireNotNull(getString("name")),
        type = debtType,
        totalAmount = Money(getLong("totalAmount") ?: 0L),
        remainingBalance = Money(getLong("remainingBalance") ?: 0L),
        interestRateApr = getDouble("interestRateApr") ?: 0.0,
        minimumPayment = Money(getLong("minimumPayment") ?: 0L),
        dueDate = sanitizedDueDate,
        statementDate = getLong("statementDate")?.toInt(),
        linkedWalletId = getString("linkedWalletId"),
        gracePeriodDays = (getLong("gracePeriodDays") ?: 45L).toInt(),
        colorHex = getString("colorHex") ?: "#E11D48",
        isReminderEnabled = getBoolean("isReminderEnabled") ?: true,
        reminderDaysBefore = (getLong("reminderDaysBefore") ?: 3L).toInt().coerceIn(1, 10),
        isSettled = getBoolean("isSettled") ?: false,
        createdAt = getTimestamp("createdAt")?.toDate()?.toInstant() ?: Instant.now(),
        updatedAt = getTimestamp("updatedAt")?.toDate()?.toInstant() ?: Instant.now(),
    )
}.getOrNull()

internal fun DocumentSnapshot.toDebtPaymentHistory(): DebtPaymentHistory? = runCatching {
    DebtPaymentHistory(
        id = id,
        debtId = requireNotNull(getString("debtId")),
        walletId = requireNotNull(getString("walletId")),
        amount = Money(getLong("amount") ?: 0L),
        principalPaid = Money(getLong("principalPaid") ?: 0L),
        interestPaid = Money(getLong("interestPaid") ?: 0L),
        paymentDate = getTimestamp("paymentDate")?.toDate()?.toInstant() ?: Instant.now(),
        note = getString("note").orEmpty(),
        isCreditCardPayment = getBoolean("isCreditCardPayment") ?: false,
    )
}.getOrNull()
