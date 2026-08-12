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
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
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

    override suspend fun addWithBalanceUpdate(transaction: FinanceTransaction): AppResult<String> =
        firebaseResult("Không thể thêm giao dịch") {
            val uid = requireUid()
            val transactionRef = firestore.userTransactions(uid).document()
            val walletRef = firestore.userWallets(uid).document(transaction.walletId)
            firestore.runTransaction { atomic ->
                val balance = atomic.get(walletRef).getLong("balance") ?: error("Không tìm thấy ví")
                atomic.set(transactionRef, transaction.copy(id = transactionRef.id).toFirestoreMap())
                atomic.update(walletRef, "balance", balance + transaction.balanceDelta())
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
        firestore.runTransaction { atomic ->
            val stored = atomic.get(transactionRef).toFinanceTransaction()
                ?: error("Không tìm thấy giao dịch")
            val oldBalance = atomic.get(oldWalletRef).getLong("balance") ?: error("Không tìm thấy ví cũ")
            if (oldWalletRef.path == newWalletRef.path) {
                atomic.update(oldWalletRef, "balance", oldBalance - stored.balanceDelta() + updated.balanceDelta())
            } else {
                val newBalance = atomic.get(newWalletRef).getLong("balance") ?: error("Không tìm thấy ví mới")
                atomic.update(oldWalletRef, "balance", oldBalance - stored.balanceDelta())
                atomic.update(newWalletRef, "balance", newBalance + updated.balanceDelta())
            }
            atomic.set(transactionRef, updated.copy(id = original.id, createdAt = stored.createdAt).toFirestoreMap())
        }.await()
        Unit
    }

    override suspend fun deleteWithBalanceUpdate(
        transaction: FinanceTransaction,
    ): AppResult<Unit> = firebaseResult("Không thể xóa giao dịch") {
        val uid = requireUid()
        val transactionRef = firestore.userTransactions(uid).document(transaction.id)
        firestore.runTransaction { atomic ->
            val stored = atomic.get(transactionRef).toFinanceTransaction()
                ?: error("Không tìm thấy giao dịch")
            val walletRef = firestore.userWallets(uid).document(stored.walletId)
            val balance = atomic.get(walletRef).getLong("balance") ?: error("Không tìm thấy ví")
            atomic.delete(transactionRef)
            atomic.update(walletRef, "balance", balance - stored.balanceDelta())
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
