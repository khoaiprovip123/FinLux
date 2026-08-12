package com.finlux.app.data.remote.firebase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.DashboardSummary
import com.finlux.app.domain.model.FinancialGoal
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.model.ReminderRecurrence
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.DashboardRepository
import com.finlux.app.domain.repository.GoalRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.repository.ReminderRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date
import java.util.UUID

class FirebaseReadRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : WalletRepository, CategoryRepository, BudgetRepository, ReminderRepository, GoalRepository, DashboardRepository {

    override fun observeWallets(): Flow<List<Wallet>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            close()
            return@callbackFlow
        }
        val registration = firestore.userWallets(uid).addSnapshotListener { snapshot, error ->
            if (error != null) close(error)
            else trySend(snapshot?.documents.orEmpty().mapNotNull { it.toWallet() })
        }
        awaitClose { registration.remove() }
    }

    override fun observeCategories(): Flow<List<Category>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("users").document(uid).collection("categories")
            .addSnapshotListener { snapshot, error ->
                if (error != null) close(error)
                else trySend(snapshot?.documents.orEmpty().mapNotNull { it.toCategory() })
            }
        awaitClose { registration.remove() }
    }

    override fun observeBudgets(month: YearMonth): Flow<List<Budget>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("users").document(uid).collection("budgets")
            .whereEqualTo("month", month.toString())
            .addSnapshotListener { snapshot, error ->
                if (error != null) close(error)
                else trySend(snapshot?.documents.orEmpty().mapNotNull { it.toBudget() })
            }
        awaitClose { registration.remove() }
    }

    override fun observeReminders(): Flow<List<Reminder>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("users").document(uid).collection("reminders")
            .addSnapshotListener { snapshot, error ->
                if (error != null) close(error)
                else trySend(snapshot?.documents.orEmpty().mapNotNull { it.toReminder() })
            }
        awaitClose { registration.remove() }
    }

    override fun observeGoals(): Flow<List<FinancialGoal>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("users").document(uid).collection("goals")
            .addSnapshotListener { snapshot, error ->
                if (error != null) close(error)
                else trySend(snapshot?.documents.orEmpty().mapNotNull { it.toGoal() })
            }
        awaitClose { registration.remove() }
    }

    override suspend fun upsertWallet(wallet: Wallet): AppResult<String> = firebaseResult("Không thể lưu ví") {
        val uid = requireUid()
        val id = wallet.id.ifBlank { UUID.randomUUID().toString() }
        val reference = firestore.userWallets(uid).document(id)
        firestore.runTransaction { atomic ->
            atomic.set(reference, wallet.copy(id = id).toWalletMap())
        }.await()
        id
    }

    override suspend fun deleteWallet(wallet: Wallet): AppResult<Unit> = firebaseResult("Không thể xóa ví") {
        require(!wallet.isDefault) { "Không thể xóa ví mặc định" }
        val uid = requireUid()
        val usedAsSource = firestore.userTransactions(uid).whereEqualTo("walletId", wallet.id).limit(1).get().await()
        val usedAsRelated = firestore.userTransactions(uid).whereEqualTo("relatedWalletId", wallet.id).limit(1).get().await()
        require(usedAsSource.isEmpty && usedAsRelated.isEmpty) { "Ví đã có giao dịch, không thể xóa" }
        firestore.runTransaction { it.delete(firestore.userWallets(uid).document(wallet.id)) }.await()
        Unit
    }

    override suspend fun upsertCategory(category: Category): AppResult<String> = firebaseResult("Không thể lưu danh mục") {
        val uid = requireUid()
        val id = category.id.ifBlank { UUID.randomUUID().toString() }
        firestore.collection("users").document(uid).collection("categories").document(id)
            .set(category.copy(id = id).toCategoryMap()).await()
        id
    }

    override suspend fun deleteCategory(category: Category): AppResult<Unit> = firebaseResult("Không thể xóa danh mục") {
        require(!category.isDefault) { "Không thể xóa danh mục mặc định" }
        val uid = requireUid()
        val used = firestore.userTransactions(uid).whereEqualTo("categoryId", category.id).limit(1).get().await()
        require(used.isEmpty) { "Danh mục đã phát sinh giao dịch, không thể xóa" }
        firestore.collection("users").document(uid).collection("categories").document(category.id).delete().await()
        Unit
    }

    override suspend fun upsertBudget(budget: Budget): AppResult<String> = firebaseResult("Không thể lưu ngân sách") {
        val uid = requireUid()
        val id = budget.id.ifBlank { "${budget.categoryId}_${budget.month}" }
        firestore.collection("users").document(uid).collection("budgets").document(id)
            .set(budget.copy(id = id).toBudgetMap()).await()
        id
    }

    override suspend fun deleteBudget(budget: Budget): AppResult<Unit> = firebaseResult("Không thể xóa ngân sách") {
        val uid = requireUid()
        firestore.collection("users").document(uid).collection("budgets").document(budget.id).delete().await()
        Unit
    }

    override suspend fun upsertReminder(reminder: Reminder): AppResult<String> = firebaseResult("Không thể lưu nhắc nhở") {
        val uid = requireUid()
        val id = reminder.id.ifBlank { UUID.randomUUID().toString() }
        firestore.collection("users").document(uid).collection("reminders").document(id)
            .set(reminder.copy(id = id).toReminderMap()).await()
        id
    }

    override suspend fun deleteReminder(reminder: Reminder): AppResult<Unit> = firebaseResult("Không thể xóa nhắc nhở") {
        val uid = requireUid()
        firestore.collection("users").document(uid).collection("reminders").document(reminder.id).delete().await()
        Unit
    }

    override suspend fun upsertGoal(goal: FinancialGoal): AppResult<String> = firebaseResult("Không thể lưu mục tiêu") {
        val uid = requireUid()
        val id = goal.id.ifBlank { UUID.randomUUID().toString() }
        firestore.collection("users").document(uid).collection("goals").document(id)
            .set(goal.copy(id = id).toGoalMap()).await()
        id
    }

    override suspend fun deleteGoal(goal: FinancialGoal): AppResult<Unit> = firebaseResult("Không thể xóa mục tiêu") {
        val uid = requireUid()
        firestore.collection("users").document(uid).collection("goals").document(goal.id).delete().await()
        Unit
    }

    override fun observeCurrentMonthSummary(): Flow<DashboardSummary> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            close()
            return@callbackFlow
        }
        val zone = ZoneId.systemDefault()
        val month = YearMonth.now(zone)
        val start = month.atDay(1).atStartOfDay(zone).toInstant()
        val end = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant()
        val registration = firestore.userTransactions(uid)
            .whereGreaterThanOrEqualTo("date", Timestamp(Date.from(start)))
            .whereLessThan("date", Timestamp(Date.from(end)))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else {
                    val items = snapshot?.documents.orEmpty().mapNotNull { it.toFinanceTransaction() }
                    val income = items.filter { it.type == TransactionType.INCOME }.sumOf { it.amount.value }
                    val expense = items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount.value }
                    trySend(DashboardSummary(Money(income), Money(expense), income - expense))
                }
            }
        awaitClose { registration.remove() }
    }

    private fun requireUid(): String = auth.currentUser?.uid ?: error("Phiên đăng nhập đã hết hạn")
}

private fun Wallet.toWalletMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "type" to type.name.lowercase(),
    "balance" to balance.value,
    "color" to colorHex,
    "isDefault" to isDefault,
    "createdAt" to Timestamp(Date.from(createdAt)),
)

private fun Category.toCategoryMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "type" to type.name.lowercase(),
    "icon" to icon,
    "color" to colorHex,
    "isDefault" to isDefault,
    "createdAt" to Timestamp(Date.from(createdAt)),
)

private fun Budget.toBudgetMap(): Map<String, Any?> = mapOf(
    "categoryId" to categoryId,
    "month" to month.toString(),
    "limitAmount" to limitAmount.value,
    "spentAmount" to spentAmount.value,
    "notified80" to notified80,
    "notified100" to notified100,
)

private fun Reminder.toReminderMap(): Map<String, Any?> = mapOf(
    "title" to title,
    "amount" to amount.value,
    "categoryId" to categoryId,
    "walletId" to walletId,
    "recurrence" to recurrence.name.lowercase(),
    "startDate" to Timestamp(Date.from(startDate)),
    "enabled" to enabled,
    "nextTriggerDate" to Timestamp(Date.from(nextTriggerDate)),
)

private fun FinancialGoal.toGoalMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "targetAmount" to targetAmount.value,
    "savedAmount" to savedAmount.value,
    "deadline" to Timestamp(Date.from(deadline)),
    "category" to category,
    "monthlyContribution" to monthlyContribution.value,
    "imageUri" to imageUri,
    "createdAt" to Timestamp(Date.from(createdAt)),
)

private fun DocumentSnapshot.toWallet(): Wallet? = runCatching {
    Wallet(
        id = id,
        name = requireNotNull(getString("name")),
        type = WalletType.valueOf(requireNotNull(getString("type")).uppercase()),
        balance = Money(getLong("balance") ?: 0L),
        colorHex = getString("color") ?: "#1F6FBF",
        isDefault = getBoolean("isDefault") ?: false,
        createdAt = getTimestamp("createdAt")?.toDate()?.toInstant() ?: Instant.now(),
    )
}.getOrNull()

private fun DocumentSnapshot.toCategory(): Category? = runCatching {
    Category(
        id = id,
        name = requireNotNull(getString("name")),
        type = CategoryType.valueOf(requireNotNull(getString("type")).uppercase()),
        icon = getString("icon").orEmpty(),
        colorHex = getString("color") ?: "#1F6FBF",
        isDefault = getBoolean("isDefault") ?: false,
        createdAt = getTimestamp("createdAt")?.toDate()?.toInstant() ?: Instant.now(),
    )
}.getOrNull()

private fun DocumentSnapshot.toBudget(): Budget? = runCatching {
    Budget(
        id = id,
        categoryId = requireNotNull(getString("categoryId")),
        month = YearMonth.parse(requireNotNull(getString("month"))),
        limitAmount = Money(getLong("limitAmount") ?: 0L),
        spentAmount = Money(getLong("spentAmount") ?: 0L),
        notified80 = getBoolean("notified80") ?: false,
        notified100 = getBoolean("notified100") ?: false,
    )
}.getOrNull()

private fun DocumentSnapshot.toReminder(): Reminder? = runCatching {
    Reminder(
        id = id,
        title = requireNotNull(getString("title")),
        amount = Money(getLong("amount") ?: 0L),
        categoryId = requireNotNull(getString("categoryId")),
        walletId = requireNotNull(getString("walletId")),
        recurrence = ReminderRecurrence.valueOf(requireNotNull(getString("recurrence")).uppercase()),
        startDate = requireNotNull(getTimestamp("startDate")).toDate().toInstant(),
        enabled = getBoolean("enabled") ?: true,
        nextTriggerDate = requireNotNull(getTimestamp("nextTriggerDate")).toDate().toInstant(),
    )
}.getOrNull()

private fun DocumentSnapshot.toGoal(): FinancialGoal? = runCatching {
    FinancialGoal(
        id = id,
        name = requireNotNull(getString("name")),
        targetAmount = Money(getLong("targetAmount") ?: 0L),
        savedAmount = Money(getLong("savedAmount") ?: 0L),
        deadline = requireNotNull(getTimestamp("deadline")).toDate().toInstant(),
        category = getString("category") ?: "Khác",
        monthlyContribution = Money(getLong("monthlyContribution") ?: 0L),
        imageUri = getString("imageUri"),
        createdAt = getTimestamp("createdAt")?.toDate()?.toInstant() ?: Instant.now(),
    )
}.getOrNull()
