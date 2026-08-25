package com.finlux.app.data.remote.firebase

import com.finlux.app.core.common.AppResult
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
                if (error != null) close(error)
                else trySend(snapshot?.documents.orEmpty().mapNotNull { it.toDebtAccount() })
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
        val categoryRef = userDoc.collection("categories").document("debt_payment")
        val transactionId = UUID.randomUUID().toString()
        val transactionRef = userDoc.collection("transactions").document(transactionId)
        val paymentId = UUID.randomUUID().toString()
        val paymentRef = debtRef.collection("payments").document(paymentId)

        firestore.runTransaction { tx ->
            val walletSnap = tx.get(walletRef)
            if (!walletSnap.exists()) throw IllegalArgumentException("Ví thanh toán không tồn tại")

            val currentWalletBalance = walletSnap.getLong("balance") ?: 0L
            val walletTypeStr = walletSnap.getString("type") ?: WalletType.CASH.name
            val isCreditCard = walletTypeStr == WalletType.CARD.name

            if (!isCreditCard && currentWalletBalance < amount) {
                throw IllegalArgumentException("Số dư ví không đủ để thanh toán nợ")
            }

            val debtSnap = tx.get(debtRef)
            if (!debtSnap.exists()) throw IllegalArgumentException("Khoản nợ không tồn tại")

            val categorySnap = tx.get(categoryRef)
            if (!categorySnap.exists()) {
                tx.set(
                    categoryRef,
                    mapOf(
                        "name" to "Trả nợ & Tín dụng",
                        "type" to "expense",
                        "icon" to "credit_card",
                        "color" to "#E11D48",
                        "isDefault" to true,
                        "createdAt" to Timestamp(Date.from(paymentDate)),
                    )
                )
            }

            val debtName = debtSnap.getString("name") ?: "Khoản nợ"
            val currentDebtRemaining = debtSnap.getLong("remainingBalance") ?: 0L
            val newDebtRemaining = (currentDebtRemaining - principalPaid).coerceAtLeast(0L)
            val isSettled = newDebtRemaining <= 0L

            // 1. Trừ tiền ví nguồn
            tx.update(walletRef, "balance", currentWalletBalance - amount)

            // 2. Cập nhật dư nợ
            tx.update(
                debtRef,
                mapOf(
                    "remainingBalance" to newDebtRemaining,
                    "isSettled" to isSettled,
                    "updatedAt" to Timestamp(Date.from(paymentDate)),
                )
            )

            // 3. Ghi transaction chi tiêu vào sổ cái
            val txNote = if (note.isNotBlank()) note else "Thanh toán nợ: $debtName"
            tx.set(
                transactionRef,
                mapOf(
                    "type" to TransactionType.EXPENSE.name,
                    "amount" to amount,
                    "walletId" to walletId,
                    "categoryId" to "debt_payment",
                    "note" to txNote,
                    "receiptImageUrl" to null,
                    "date" to Timestamp(Date.from(paymentDate)),
                    "createdAt" to Timestamp(Date.from(paymentDate)),
                    "updatedAt" to Timestamp(Date.from(paymentDate)),
                )
            )

            // 4. Ghi log lịch sử trả nợ
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
                )
            )
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
    "dueDate" to dueDate,
    "statementDate" to statementDate,
    "colorHex" to colorHex,
    "isReminderEnabled" to isReminderEnabled,
    "reminderDaysBefore" to reminderDaysBefore,
    "isSettled" to isSettled,
    "createdAt" to Timestamp(Date.from(createdAt)),
    "updatedAt" to Timestamp(Date.from(updatedAt)),
)

internal fun DocumentSnapshot.toDebtAccount(): DebtAccount? = runCatching {
    DebtAccount(
        id = id,
        userId = getString("userId").orEmpty(),
        name = requireNotNull(getString("name")),
        type = DebtType.valueOf(getString("type") ?: DebtType.PERSONAL_LOAN.name),
        totalAmount = Money(getLong("totalAmount") ?: 0L),
        remainingBalance = Money(getLong("remainingBalance") ?: 0L),
        interestRateApr = getDouble("interestRateApr") ?: 0.0,
        minimumPayment = Money(getLong("minimumPayment") ?: 0L),
        dueDate = (getLong("dueDate") ?: 15L).toInt().coerceIn(1, 31),
        statementDate = getLong("statementDate")?.toInt(),
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
    )
}.getOrNull()
