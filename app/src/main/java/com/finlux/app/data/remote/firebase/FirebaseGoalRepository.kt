package com.finlux.app.data.remote.firebase

import com.finlux.app.core.common.AppResult
import com.finlux.app.data.remote.firebase.schema.FirestoreSchema
import com.finlux.app.domain.model.FinanceBusinessConstants
import com.finlux.app.domain.model.FinancialGoal
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SystemCategories
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.GoalRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.util.Date
import java.util.UUID

class FirebaseGoalRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : GoalRepository {

    override fun observeGoals(): Flow<List<FinancialGoal>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            close()
            return@callbackFlow
        }
        val registration = firestore.collection(FirestoreSchema.USERS).document(uid).collection(FirestoreSchema.Collections.GOALS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) close(error)
                else trySend(snapshot?.documents.orEmpty().mapNotNull { it.toGoal() })
            }
        awaitClose { registration.remove() }
    }

    override suspend fun upsertGoal(goal: FinancialGoal): AppResult<String> = firebaseResult("Không thể lưu mục tiêu") {
        val uid = requireUid()
        val id = goal.id.ifBlank { UUID.randomUUID().toString() }
        firestore.collection(FirestoreSchema.USERS).document(uid).collection(FirestoreSchema.Collections.GOALS).document(id)
            .set(goal.copy(id = id).toGoalMap()).await()
        id
    }

    override suspend fun deleteGoal(goal: FinancialGoal): AppResult<Unit> = firebaseResult("Không thể xóa mục tiêu") {
        val uid = requireUid()
        firestore.collection(FirestoreSchema.USERS).document(uid).collection(FirestoreSchema.Collections.GOALS).document(goal.id).delete().await()
        Unit
    }

    override suspend fun depositToGoal(
        goalId: String,
        walletId: String,
        amount: Long,
        note: String,
        date: Instant,
    ): AppResult<Unit> = firebaseResult("Không thể nạp tiền vào mục tiêu") {
        val uid = requireUid()
        val userDoc = firestore.collection(FirestoreSchema.USERS).document(uid)
        val walletRef = userDoc.collection(FirestoreSchema.Collections.WALLETS).document(walletId)
        val goalRef = userDoc.collection(FirestoreSchema.Collections.GOALS).document(goalId)
        val categoryRef = userDoc.collection(FirestoreSchema.Collections.CATEGORIES).document(SystemCategories.SAVINGS)
        val transactionId = UUID.randomUUID().toString()
        val transactionRef = userDoc.collection(FirestoreSchema.Collections.TRANSACTIONS).document(transactionId)

        firestore.runTransaction { tx ->
            val walletSnap = tx.get(walletRef)
            if (!walletSnap.exists()) throw IllegalArgumentException("Ví thanh toán không tồn tại")

            val currentWalletBalance = walletSnap.getLong("balance") ?: 0L
            val walletTypeStr = walletSnap.getString("type") ?: WalletType.CASH.name
            val isCreditCard = walletTypeStr == WalletType.CARD.name

            if (!isCreditCard && currentWalletBalance < amount) {
                throw IllegalArgumentException("Số dư ví không đủ để nạp vào mục tiêu")
            }

            val goalSnap = tx.get(goalRef)
            if (!goalSnap.exists()) throw IllegalArgumentException("Mục tiêu tài chính không tồn tại")

            val goalName = goalSnap.getString("name") ?: "Mục tiêu"
            val currentSaved = goalSnap.getLong("savedAmount") ?: 0L
            val newSaved = currentSaved + amount

            val categorySnap = tx.get(categoryRef)
            if (!categorySnap.exists()) {
                tx.set(
                    categoryRef,
                    mapOf(
                        "name" to "Tích lũy & Mục tiêu",
                        "type" to "expense",
                        "icon" to "savings",
                        "color" to "#8B5CF6",
                        "isDefault" to true,
                        "createdAt" to Timestamp(Date.from(date)),
                    )
                )
            }

            // 1. Trừ tiền ví nguồn
            tx.update(walletRef, "balance", currentWalletBalance - amount)

            // 2. Tăng số tiền tích lũy của Goal
            tx.update(goalRef, "savedAmount", newSaved)

            // 3. Ghi transaction chi tiêu tích lũy vào Sổ cái
            val txNote = if (note.isNotBlank()) note else "Nạp tích lũy: $goalName"
            tx.set(
                transactionRef,
                mapOf(
                    "type" to TransactionType.EXPENSE.name,
                    "amount" to amount,
                    "walletId" to walletId,
                    "categoryId" to SystemCategories.SAVINGS,
                    "note" to txNote,
                    "receiptImageUrl" to null,
                    "date" to Timestamp(Date.from(date)),
                    "createdAt" to Timestamp(Date.from(date)),
                    "updatedAt" to Timestamp(Date.from(date)),
                )
            )
        }.await()
        Unit
    }

    override suspend fun withdrawFromGoal(
        goalId: String,
        walletId: String,
        amount: Long,
        note: String,
        date: Instant,
    ): AppResult<Unit> = firebaseResult("Không thể rút tiền từ mục tiêu") {
        val uid = requireUid()
        val userDoc = firestore.collection(FirestoreSchema.USERS).document(uid)
        val walletRef = userDoc.collection(FirestoreSchema.Collections.WALLETS).document(walletId)
        val goalRef = userDoc.collection(FirestoreSchema.Collections.GOALS).document(goalId)
        val categoryRef = userDoc.collection(FirestoreSchema.Collections.CATEGORIES).document(SystemCategories.SAVINGS)
        val transactionId = UUID.randomUUID().toString()
        val transactionRef = userDoc.collection(FirestoreSchema.Collections.TRANSACTIONS).document(transactionId)

        firestore.runTransaction { tx ->
            val goalSnap = tx.get(goalRef)
            if (!goalSnap.exists()) throw IllegalArgumentException("Mục tiêu tài chính không tồn tại")

            val goalName = goalSnap.getString("name") ?: "Mục tiêu"
            val currentSaved = goalSnap.getLong("savedAmount") ?: 0L
            if (currentSaved < amount) {
                throw IllegalArgumentException("Số tiền tích lũy hiện tại (${currentSaved}) nhỏ hơn số tiền muốn rút (${amount})")
            }

            val walletSnap = tx.get(walletRef)
            if (!walletSnap.exists()) throw IllegalArgumentException("Ví nhận tiền không tồn tại")

            val currentWalletBalance = walletSnap.getLong("balance") ?: 0L
            val newSaved = currentSaved - amount

            val categorySnap = tx.get(categoryRef)
            if (!categorySnap.exists()) {
                tx.set(
                    categoryRef,
                    mapOf(
                        "name" to "Tích lũy & Mục tiêu",
                        "type" to "expense",
                        "icon" to "savings",
                        "color" to "#8B5CF6",
                        "isDefault" to true,
                        "createdAt" to Timestamp(Date.from(date)),
                    )
                )
            }

            // 1. Giảm số tiền tích lũy của Goal
            tx.update(goalRef, "savedAmount", newSaved)

            // 2. Tăng số tiền ví nhận
            tx.update(walletRef, "balance", currentWalletBalance + amount)

            // 3. Ghi transaction thu nhập/hoàn tiền từ tích lũy vào Sổ cái
            val txNote = if (note.isNotBlank()) note else "Rút tích lũy: $goalName"
            tx.set(
                transactionRef,
                mapOf(
                    "type" to TransactionType.INCOME.name,
                    "amount" to amount,
                    "walletId" to walletId,
                    "categoryId" to SystemCategories.SAVINGS,
                    "note" to txNote,
                    "receiptImageUrl" to null,
                    "date" to Timestamp(Date.from(date)),
                    "createdAt" to Timestamp(Date.from(date)),
                    "updatedAt" to Timestamp(Date.from(date)),
                )
            )
        }.await()
        Unit
    }

    private fun requireUid(): String = auth.currentUser?.uid ?: error("Phiên đăng nhập đã hết hạn")
}

internal fun FinancialGoal.toGoalMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "targetAmount" to targetAmount.value,
    "savedAmount" to savedAmount.value,
    "deadline" to Timestamp(Date.from(deadline)),
    "category" to category,
    "monthlyContribution" to monthlyContribution.value,
    "imageUri" to imageUri,
    "createdAt" to Timestamp(Date.from(createdAt)),
)

private fun parseInstantField(raw: Any?, defaultInstant: Instant): Instant {
    if (raw == null) return defaultInstant
    return when (raw) {
        is Timestamp -> raw.toDate().toInstant()
        is Date -> raw.toInstant()
        is Number -> runCatching { Instant.ofEpochMilli(raw.toLong()) }.getOrDefault(defaultInstant)
        is String -> {
            runCatching { Instant.parse(raw) }
                .recoverCatching { Instant.ofEpochMilli(raw.toLong()) }
                .getOrDefault(defaultInstant)
        }
        else -> defaultInstant
    }
}

private fun parseMoneyField(raw: Any?): Money {
    if (raw == null) return Money(0L)
    val amount = when (raw) {
        is Number -> raw.toLong()
        is String -> raw.toLongOrNull() ?: 0L
        else -> 0L
    }
    return Money(amount)
}

internal fun DocumentSnapshot.toGoal(): FinancialGoal? = runCatching {
    val goalName = getString("name")?.takeIf { it.isNotBlank() } ?: return@runCatching null
    val defaultDeadline = Instant.now().plusSeconds(FinanceBusinessConstants.Goals.DEFAULT_DEADLINE_DAYS * 86400L)
    FinancialGoal(
        id = id,
        name = goalName,
        targetAmount = parseMoneyField(get("targetAmount")),
        savedAmount = parseMoneyField(get("savedAmount")),
        deadline = parseInstantField(get("deadline"), defaultDeadline),
        category = getString("category")?.takeIf { it.isNotBlank() } ?: "Khác",
        monthlyContribution = parseMoneyField(get("monthlyContribution")),
        imageUri = getString("imageUri"),
        createdAt = parseInstantField(get("createdAt"), Instant.now()),
    )
}.getOrNull()

