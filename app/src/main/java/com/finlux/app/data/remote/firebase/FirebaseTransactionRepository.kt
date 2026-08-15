package com.finlux.app.data.remote.firebase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.repository.TransactionRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.util.UUID
import java.util.Date

class FirebaseTransactionRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : TransactionRepository {
    override fun observeRecent(limit: Int): Flow<List<FinanceTransaction>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = firestore.userTransactions(uid)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) close(error)
                else trySend(snapshot?.documents.orEmpty().mapNotNull { it.toFinanceTransaction() })
            }
        awaitClose { registration.remove() }
    }

    override fun observeMonth(month: YearMonth): Flow<List<FinanceTransaction>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val zone = ZoneId.systemDefault()
        val start = month.atDay(1).atStartOfDay(zone).toInstant()
        val end = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant()
        val registration = firestore.userTransactions(uid)
            .whereGreaterThanOrEqualTo("date", Timestamp(Date.from(start)))
            .whereLessThan("date", Timestamp(Date.from(end)))
            .addSnapshotListener { snapshot, error ->
                if (error != null) close(error)
                else trySend(snapshot?.documents.orEmpty().mapNotNull { it.toFinanceTransaction() })
            }
        awaitClose { registration.remove() }
    }

    override suspend fun addWithBalanceUpdate(transaction: FinanceTransaction): AppResult<String> =
        firebaseResult("Không thể thêm giao dịch") {
            val uid = requireUid()
            val transactionRef = firestore.userTransactions(uid).document()
            val walletRef = firestore.userWallets(uid).document(transaction.walletId)
            val budgetRef = transaction.budgetRef(firestore, uid)
            firestore.runTransaction { atomic ->
                val walletDoc = atomic.get(walletRef)
                val balance = walletDoc.getLong("balance") ?: 0L
                val budgetDoc = if (budgetRef != null && transaction.type == TransactionType.EXPENSE) {
                    atomic.get(budgetRef)
                } else null
                atomic.set(transactionRef, transaction.copy(id = transactionRef.id).toFirestoreMap())
                atomic.update(walletRef, "balance", balance + transaction.balanceDelta())
                // BR-06: atomically update budget.spentAmount for EXPENSE transactions if budget exists
                if (budgetDoc != null && budgetDoc.exists() && budgetRef != null) {
                    atomic.update(budgetRef, "spentAmount", FieldValue.increment(transaction.amount.value))
                }
            }.await()
            transactionRef.id
        }

    override suspend fun editWithBalanceUpdate(
        original: FinanceTransaction,
        updated: FinanceTransaction,
    ): AppResult<Unit> = firebaseResult("Không thể sửa giao dịch") {
        val uid = requireUid()
        val transactionRef = firestore.userTransactions(uid).document(original.id)
        val oldWalletRef = firestore.userWallets(uid).document(original.walletId)
        val newWalletRef = firestore.userWallets(uid).document(updated.walletId)
        val oldBudgetRef = original.budgetRef(firestore, uid)
        val newBudgetRef = updated.budgetRef(firestore, uid)
        firestore.runTransaction { atomic ->
            val stored = atomic.get(transactionRef).toFinanceTransaction()
                ?: error("Không tìm thấy giao dịch")
            val oldBalance = atomic.get(oldWalletRef).getLong("balance") ?: error("Không tìm thấy ví cũ")
            val newBalance = if (oldWalletRef.path != newWalletRef.path) {
                atomic.get(newWalletRef).getLong("balance") ?: error("Không tìm thấy ví mới")
            } else null

            val oldBudgetDoc = if (oldBudgetRef != null && original.type == TransactionType.EXPENSE) {
                atomic.get(oldBudgetRef)
            } else null
            val newBudgetDoc = if (newBudgetRef != null && updated.type == TransactionType.EXPENSE) {
                if (oldBudgetRef?.path == newBudgetRef.path) oldBudgetDoc
                else atomic.get(newBudgetRef)
            } else null

            if (oldWalletRef.path == newWalletRef.path) {
                atomic.update(oldWalletRef, "balance", oldBalance - stored.balanceDelta() + updated.balanceDelta())
            } else {
                atomic.update(oldWalletRef, "balance", oldBalance - stored.balanceDelta())
                atomic.update(newWalletRef, "balance", (newBalance ?: 0L) + updated.balanceDelta())
            }
            atomic.set(transactionRef, updated.copy(id = original.id, createdAt = stored.createdAt).toFirestoreMap())
            // BR-06: reverse old budget spent, apply new budget spent if budget exists
            if (oldBudgetDoc != null && oldBudgetDoc.exists() && oldBudgetRef != null) {
                atomic.update(oldBudgetRef, "spentAmount", FieldValue.increment(-original.amount.value))
            }
            if (newBudgetDoc != null && newBudgetDoc.exists() && newBudgetRef != null) {
                atomic.update(newBudgetRef, "spentAmount", FieldValue.increment(updated.amount.value))
            }
        }.await()
        Unit
    }

    override suspend fun deleteWithBalanceUpdate(
        transaction: FinanceTransaction,
    ): AppResult<Unit> = firebaseResult("Không thể xóa giao dịch") {
        val uid = requireUid()
        val transactionRef = firestore.userTransactions(uid).document(transaction.id)
        val budgetRef = transaction.budgetRef(firestore, uid)
        firestore.runTransaction { atomic ->
            val stored = atomic.get(transactionRef).toFinanceTransaction()
                ?: error("Không tìm thấy giao dịch")
            val walletRef = firestore.userWallets(uid).document(stored.walletId)
            val balance = atomic.get(walletRef).getLong("balance") ?: error("Không tìm thấy ví")
            val budgetDoc = if (budgetRef != null && stored.type == TransactionType.EXPENSE) {
                atomic.get(budgetRef)
            } else null

            atomic.delete(transactionRef)
            atomic.update(walletRef, "balance", balance - stored.balanceDelta())
            // BR-06: reverse spentAmount when deleting an EXPENSE transaction if budget exists
            if (budgetDoc != null && budgetDoc.exists() && budgetRef != null) {
                atomic.update(budgetRef, "spentAmount", FieldValue.increment(-stored.amount.value))
            }
        }.await()
        Unit
    }

    override suspend fun transferBetweenWallets(
        sourceWalletId: String,
        destinationWalletId: String,
        amount: Long,
        note: String,
        date: Instant,
    ): AppResult<Unit> = firebaseResult("Không thể chuyển tiền") {
        require(sourceWalletId != destinationWalletId) { "Hai ví phải khác nhau" }
        require(amount > 0L) { "Số tiền phải lớn hơn 0" }
        val uid = requireUid()
        val sourceRef = firestore.userWallets(uid).document(sourceWalletId)
        val destinationRef = firestore.userWallets(uid).document(destinationWalletId)
        val pairId = UUID.randomUUID().toString()
        val outRef = firestore.userTransactions(uid).document("${pairId}_out")
        val inRef = firestore.userTransactions(uid).document("${pairId}_in")
        val now = Instant.now()
        firestore.runTransaction { atomic ->
            val sourceBalance = atomic.get(sourceRef).getLong("balance") ?: error("Không tìm thấy ví nguồn")
            val sourceDoc = atomic.get(sourceRef)
            val sourceType = sourceDoc.getString("type")
            val isCard = sourceType.equals("CARD", ignoreCase = true)
            if (!isCard && sourceBalance < amount) {
                error("Số dư ví nguồn không đủ để thực hiện chuyển tiền")
            }
            val destinationBalance = atomic.get(destinationRef).getLong("balance") ?: error("Không tìm thấy ví đích")
            val outgoing = FinanceTransaction(
                id = outRef.id,
                type = TransactionType.TRANSFER_OUT,
                amount = Money(amount),
                categoryId = null,
                walletId = sourceWalletId,
                relatedWalletId = destinationWalletId,
                note = note,
                date = date,
                createdAt = now,
                updatedAt = now,
            )
            val incoming = outgoing.copy(
                id = inRef.id,
                type = TransactionType.TRANSFER_IN,
                walletId = destinationWalletId,
                relatedWalletId = sourceWalletId,
            )
            atomic.update(sourceRef, "balance", sourceBalance - amount)
            atomic.update(destinationRef, "balance", destinationBalance + amount)
            atomic.set(outRef, outgoing.toFirestoreMap())
            atomic.set(inRef, incoming.toFirestoreMap())
        }.await()
        Unit
    }

    private fun requireUid(): String = auth.currentUser?.uid ?: error("Phiên đăng nhập đã hết hạn")
}

/**
 * Returns a DocumentReference to the Budget document for this transaction's month+category,
 * or null if the transaction is not an EXPENSE or has no categoryId.
 * Budget IDs follow the convention: "{categoryId}_{YearMonth}" (e.g. "abc123_2025-08").
 */
internal fun FinanceTransaction.budgetRef(
    firestore: FirebaseFirestore,
    uid: String,
): DocumentReference? {
    if (type != TransactionType.EXPENSE) return null
    val catId = categoryId ?: return null
    val month = YearMonth.from(date.atZone(java.time.ZoneOffset.UTC))
    val budgetId = "${catId}_${month}"
    return firestore.collection("users").document(uid).collection("budgets").document(budgetId)
}

internal suspend inline fun <T> firebaseResult(message: String, block: () -> T): AppResult<T> =
    runCatching(block).fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Error(it.localizedMessage ?: message, it) },
    )

internal fun FirebaseFirestore.userTransactions(uid: String) =
    collection("users").document(uid).collection("transactions")

internal fun FirebaseFirestore.userWallets(uid: String) =
    collection("users").document(uid).collection("wallets")

private fun FinanceTransaction.balanceDelta(): Long = when (type) {
    TransactionType.INCOME, TransactionType.TRANSFER_IN -> amount.value
    TransactionType.EXPENSE, TransactionType.TRANSFER_OUT -> -amount.value
}

private fun FinanceTransaction.toFirestoreMap(): Map<String, Any?> = mapOf(
    "type" to type.name.lowercase(),
    "amount" to amount.value,
    "categoryId" to categoryId,
    "walletId" to walletId,
    "relatedWalletId" to relatedWalletId,
    "note" to note,
    "receiptImageUrl" to receiptImageUrl,
    "date" to Timestamp(Date.from(date)),
    "createdAt" to Timestamp(Date.from(createdAt)),
    "updatedAt" to Timestamp.now(),
)

internal fun com.google.firebase.firestore.DocumentSnapshot.toFinanceTransaction(): FinanceTransaction? =
    runCatching {
        FinanceTransaction(
            id = id,
            type = TransactionType.valueOf(requireNotNull(getString("type")).uppercase()),
            amount = Money(requireNotNull(getLong("amount"))),
            categoryId = getString("categoryId"),
            walletId = requireNotNull(getString("walletId")),
            relatedWalletId = getString("relatedWalletId"),
            note = getString("note").orEmpty(),
            receiptImageUrl = getString("receiptImageUrl"),
            date = requireNotNull(getTimestamp("date")).toDate().toInstant(),
            createdAt = getTimestamp("createdAt")?.toDate()?.toInstant() ?: Instant.now(),
            updatedAt = getTimestamp("updatedAt")?.toDate()?.toInstant() ?: Instant.now(),
        )
    }.getOrNull()
