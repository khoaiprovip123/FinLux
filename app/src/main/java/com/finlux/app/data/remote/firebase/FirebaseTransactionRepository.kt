package com.finlux.app.data.remote.firebase

import com.finlux.app.core.common.AppResult
import com.finlux.app.core.time.FinanceTime
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

private const val MAX_MONEY_AMOUNT = 999_999_999_999_999L

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
        val start = FinanceTime.monthStart(month)
        val end = FinanceTime.monthEnd(month)
        val registration = firestore.userTransactions(uid)
            .whereGreaterThanOrEqualTo("date", Timestamp(Date.from(start)))
            .whereLessThan("date", Timestamp(Date.from(end)))
            .addSnapshotListener { snapshot, error ->
                if (error != null) close(error)
                else trySend(snapshot?.documents.orEmpty().mapNotNull { it.toFinanceTransaction() })
            }
        awaitClose { registration.remove() }
    }

    override fun observePeriod(start: Instant, endExclusive: Instant): Flow<List<FinanceTransaction>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = firestore.userTransactions(uid)
            .whereGreaterThanOrEqualTo("date", Timestamp(Date.from(start)))
            .whereLessThan("date", Timestamp(Date.from(endExclusive)))
            .addSnapshotListener { snapshot, error ->
                if (error != null) close(error)
                else trySend(snapshot?.documents.orEmpty().mapNotNull { it.toFinanceTransaction() })
            }
        awaitClose { registration.remove() }
    }

    override suspend fun addWithBalanceUpdate(transaction: FinanceTransaction): AppResult<String> =
        firebaseResult("Không thể thêm giao dịch") {
            require(transaction.amount.value in 1..MAX_MONEY_AMOUNT) { "Số tiền không hợp lệ" }
            val uid = requireUid()
            val transactionRef = if (transaction.id.isNotBlank()) {
                firestore.userTransactions(uid).document(transaction.id)
            } else {
                firestore.userTransactions(uid).document()
            }
            val walletRef = firestore.userWallets(uid).document(transaction.walletId)
            val budgetRef = transaction.budgetRef(firestore, uid)
            firestore.runTransaction { atomic ->
                if (transaction.id.isNotBlank()) {
                    if (atomic.get(transactionRef).exists()) {
                        error("Giao dịch này đã được xử lý (trùng lặp).")
                    }
                }
                val walletDoc = atomic.get(walletRef)
                val balance = walletDoc.getLong("balance") ?: error("Không tìm thấy ví")
                val walletType = walletDoc.getString("type")
                val isCard = walletType.equals("CARD", ignoreCase = true)
                val updatedBalance = Math.addExact(balance, transaction.balanceDelta())
                if (!isCard && updatedBalance < 0) {
                    error("Số dư ví không đủ để thực hiện giao dịch này")
                }
                val budgetDoc = if (budgetRef != null && transaction.type == TransactionType.EXPENSE) {
                    atomic.get(budgetRef)
                } else null
                atomic.set(transactionRef, transaction.copy(id = transactionRef.id).toFirestoreMap())
                atomic.update(
                    walletRef,
                    mapOf(
                        "balance" to updatedBalance,
                        "lastTransactionId" to transactionRef.id
                    )
                )
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
        require(updated.amount.value in 1..MAX_MONEY_AMOUNT) { "Số tiền không hợp lệ" }
        val uid = requireUid()
        val transactionRef = firestore.userTransactions(uid).document(original.id)
        val newWalletRef = firestore.userWallets(uid).document(updated.walletId)
        val newBudgetRef = updated.budgetRef(firestore, uid)

        firestore.runTransaction { atomic ->
            // P0-01: Use stored document from Firestore as authoritative source of truth for old state
            val stored = atomic.get(transactionRef).toFinanceTransaction()
                ?: error("Không tìm thấy giao dịch")
            if (stored.type == TransactionType.TRANSFER_OUT || stored.type == TransactionType.TRANSFER_IN) {
                error("Không thể chỉnh sửa giao dịch chuyển tiền. Vui lòng xóa và tạo lại giao dịch mới.")
            }
            val oldWalletRef = firestore.userWallets(uid).document(stored.walletId)
            val oldBudgetRef = stored.budgetRef(firestore, uid)

            val oldWalletDoc = atomic.get(oldWalletRef)
            val oldBalance = oldWalletDoc.getLong("balance") ?: error("Không tìm thấy ví cũ")
            val isOldCard = oldWalletDoc.getString("type").equals("CARD", ignoreCase = true)

            val newWalletDoc = if (oldWalletRef.path != newWalletRef.path) {
                atomic.get(newWalletRef)
            } else oldWalletDoc
            val newBalance = newWalletDoc.getLong("balance") ?: error("Không tìm thấy ví mới")
            val isNewCard = newWalletDoc.getString("type").equals("CARD", ignoreCase = true)

            val oldBudgetDoc = if (oldBudgetRef != null && stored.type == TransactionType.EXPENSE) {
                atomic.get(oldBudgetRef)
            } else null
            val newBudgetDoc = if (newBudgetRef != null && updated.type == TransactionType.EXPENSE) {
                if (oldBudgetRef?.path == newBudgetRef.path) oldBudgetDoc
                else atomic.get(newBudgetRef)
            } else null

            if (oldWalletRef.path == newWalletRef.path) {
                val finalBalance = Math.addExact(
                    Math.subtractExact(oldBalance, stored.balanceDelta()),
                    updated.balanceDelta(),
                )
                if (!isOldCard && finalBalance < 0) {
                    error("Số dư ví không đủ để sửa giao dịch này")
                }
                atomic.update(
                    oldWalletRef,
                    mapOf(
                        "balance" to finalBalance,
                        "lastTransactionId" to transactionRef.id
                    )
                )
            } else {
                val finalOldBalance = Math.subtractExact(oldBalance, stored.balanceDelta())
                val finalNewBalance = Math.addExact(newBalance, updated.balanceDelta())
                if (!isOldCard && finalOldBalance < 0) {
                    error("Số dư ví cũ không đủ sau khi hoàn tác giao dịch")
                }
                if (!isNewCard && finalNewBalance < 0) {
                    error("Số dư ví mới không đủ để thực hiện giao dịch")
                }
                atomic.update(
                    oldWalletRef,
                    mapOf(
                        "balance" to finalOldBalance,
                        "lastTransactionId" to transactionRef.id
                    )
                )
                atomic.update(
                    newWalletRef,
                    mapOf(
                        "balance" to finalNewBalance,
                        "lastTransactionId" to transactionRef.id
                    )
                )
            }
            atomic.set(transactionRef, updated.copy(id = stored.id, createdAt = stored.createdAt).toFirestoreMap())
            // BR-06: reverse old budget spent based on stored, apply new budget spent
            if (oldBudgetDoc != null && oldBudgetDoc.exists() && oldBudgetRef != null) {
                atomic.update(oldBudgetRef, "spentAmount", FieldValue.increment(-stored.amount.value))
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
        firestore.runTransaction { atomic ->
            // P0-02: Derive walletRef and budgetRef strictly from stored transaction in Firestore
            val stored = atomic.get(transactionRef).toFinanceTransaction()
                ?: error("Không tìm thấy giao dịch")

            if (stored.type == TransactionType.TRANSFER_OUT || stored.type == TransactionType.TRANSFER_IN) {
                // Cascade Atomic Deletion for Transfer Pair
                val isOutgoing = stored.type == TransactionType.TRANSFER_OUT
                val sourceWalletId = if (isOutgoing) stored.walletId else (stored.relatedWalletId ?: error("Không tìm thấy ví nguồn"))
                val destinationWalletId = if (isOutgoing) (stored.relatedWalletId ?: error("Không tìm thấy ví đích")) else stored.walletId
                val transferAmount = stored.amount.value

                val sourceWalletRef = firestore.userWallets(uid).document(sourceWalletId)
                val destWalletRef = firestore.userWallets(uid).document(destinationWalletId)

                // 1. ALL READS FIRST (Strict Firestore rule: all reads before any writes)
                val sourceDoc = atomic.get(sourceWalletRef)
                val sourceBalance = sourceDoc.getLong("balance") ?: error("Không tìm thấy ví nguồn")
                val destDoc = atomic.get(destWalletRef)
                val destBalance = destDoc.getLong("balance") ?: error("Không tìm thấy ví đích")
                val isDestCard = destDoc.getString("type").equals("CARD", ignoreCase = true)

                // Determine counterpart transaction ref and read it
                val counterpartId = if (stored.id.endsWith("_out")) {
                    stored.id.removeSuffix("_out") + "_in"
                } else if (stored.id.endsWith("_in")) {
                    stored.id.removeSuffix("_in") + "_out"
                } else null

                val counterpartRef = if (counterpartId != null) {
                    firestore.userTransactions(uid).document(counterpartId)
                } else null

                val counterpartDoc = if (counterpartRef != null) {
                    atomic.get(counterpartRef)
                } else null

                val finalSourceBalance = Math.addExact(sourceBalance, transferAmount)
                val finalDestBalance = Math.subtractExact(destBalance, transferAmount)

                if (!isDestCard && finalDestBalance < 0) {
                    error("Số dư ví đích hiện tại không đủ để hoàn tác thu hồi khoản tiền đã nhận")
                }

                // 2. ALL WRITES AFTER READS
                atomic.delete(transactionRef)
                if (counterpartRef != null && counterpartDoc != null && counterpartDoc.exists()) {
                    atomic.delete(counterpartRef)
                }

                atomic.update(
                    sourceWalletRef,
                    mapOf(
                        "balance" to finalSourceBalance,
                        "lastTransactionId" to transactionRef.id
                    )
                )
                atomic.update(
                    destWalletRef,
                    mapOf(
                        "balance" to finalDestBalance,
                        "lastTransactionId" to transactionRef.id
                    )
                )
            } else {
                // Normal deletion for INCOME / EXPENSE
                val walletRef = firestore.userWallets(uid).document(stored.walletId)
                val walletDoc = atomic.get(walletRef)
                val balance = walletDoc.getLong("balance") ?: error("Không tìm thấy ví")
                val isCard = walletDoc.getString("type").equals("CARD", ignoreCase = true)
                val budgetRef = stored.budgetRef(firestore, uid)
                val budgetDoc = if (budgetRef != null && stored.type == TransactionType.EXPENSE) {
                    atomic.get(budgetRef)
                } else null

                val finalBalance = Math.subtractExact(balance, stored.balanceDelta())
                if (!isCard && finalBalance < 0) {
                    error("Không thể xóa giao dịch vì số dư ví hiện tại không đủ để hoàn tác")
                }

                atomic.delete(transactionRef)
                atomic.update(
                    walletRef,
                    mapOf(
                        "balance" to finalBalance,
                        "lastTransactionId" to transactionRef.id
                    )
                )
                // BR-06: reverse spentAmount when deleting an EXPENSE transaction based on stored
                if (budgetDoc != null && budgetDoc.exists() && budgetRef != null) {
                    atomic.update(budgetRef, "spentAmount", FieldValue.increment(-stored.amount.value))
                }
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
        require(amount in 1..MAX_MONEY_AMOUNT) { "Số tiền phải lớn hơn 0" }
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
            atomic.update(
                sourceRef,
                mapOf(
                    "balance" to Math.subtractExact(sourceBalance, amount),
                    "lastTransactionId" to outRef.id
                )
            )
            atomic.update(
                destinationRef,
                mapOf(
                    "balance" to Math.addExact(destinationBalance, amount),
                    "lastTransactionId" to inRef.id
                )
            )
            atomic.set(outRef, outgoing.toFirestoreMap())
            atomic.set(inRef, incoming.toFirestoreMap())
        }.await()
        Unit
    }

    override suspend fun executeSalaryRolloverAtomic(
        cycleKey: String,
        sourceWalletId: String,
        destinationWalletId: String,
        amount: Long,
        note: String,
        date: Instant,
    ): AppResult<Unit> = firebaseResult("Không thể kết chuyển lương") {
        require(sourceWalletId != destinationWalletId) { "Hai ví phải khác nhau" }
        require(amount >= 0) { "Số tiền không hợp lệ" }
        val uid = requireUid()
        val sourceRef = firestore.userWallets(uid).document(sourceWalletId)
        val destinationRef = firestore.userWallets(uid).document(destinationWalletId)
        val rolloverRef = firestore.collection("users").document(uid).collection("salaryRollovers").document(cycleKey.replace(":", "_").replace("/", "_").replace(".", "_"))
        val pairId = UUID.randomUUID().toString()
        val outRef = firestore.userTransactions(uid).document("${pairId}_out")
        val inRef = firestore.userTransactions(uid).document("${pairId}_in")
        val now = Instant.now()

        firestore.runTransaction { atomic ->
            // 1. Check if already processed
            val rolloverDoc = atomic.get(rolloverRef)
            if (rolloverDoc.exists()) {
                error("Chu kỳ lương này đã được kết chuyển")
            }

            // 2. Write the rollover marker
            atomic.set(rolloverRef, mapOf(
                "cycleKey" to cycleKey,
                "processedAt" to FieldValue.serverTimestamp(),
            ))

            if (amount > 0) {
                // 3. Execute the transfer if amount > 0
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
                atomic.update(
                    sourceRef,
                    mapOf(
                        "balance" to Math.subtractExact(sourceBalance, amount),
                        "lastTransactionId" to outRef.id
                    )
                )
                atomic.update(
                    destinationRef,
                    mapOf(
                        "balance" to Math.addExact(destinationBalance, amount),
                        "lastTransactionId" to inRef.id
                    )
                )
                atomic.set(outRef, outgoing.toFirestoreMap())
                atomic.set(inRef, incoming.toFirestoreMap())
            }
        }.await()
        Unit
    }

    private fun requireUid(): String = auth.currentUser?.uid ?: error("Phiên đăng nhập đã hết hạn")
}

/**
 * Returns a DocumentReference to the Budget document for this transaction's period+category,
 * or null if the transaction is not an EXPENSE or has no categoryId.
 * Budget IDs follow the standard convention: "{categoryId}_{periodKey}" (e.g. "abc123_month:2026-08").
 */
internal fun FinanceTransaction.budgetRef(
    firestore: FirebaseFirestore,
    uid: String,
    zone: ZoneId = FinanceTime.defaultZone,
): DocumentReference? {
    if (type != TransactionType.EXPENSE) return null
    val catId = categoryId ?: return null
    val month = FinanceTime.financialMonth(date, zone)
    val budgetId = "${catId}_month:${month}"
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
