package com.finlux.app.data.demo

import android.content.Context
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.DashboardSummary
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.FinancialGoal
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.model.ReminderRecurrence
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.UserProfile
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.model.AppNotification
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtPaymentHistory
import com.finlux.app.domain.model.DebtType
import com.finlux.app.domain.repository.DebtRepository
import com.finlux.app.domain.repository.NotificationRepository
import com.finlux.app.domain.repository.AuthRepository
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.DashboardRepository
import com.finlux.app.domain.repository.GoalRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.repository.ReminderRepository
import com.finlux.app.domain.repository.ReceiptStorageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runnable local implementation used until app/google-services.json is supplied. It is deliberately
 * isolated in data/demo, so enabling Firebase does not leak demo decisions into domain or UI code.
 */
@Singleton
class DemoFinluxRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) :
    AuthRepository,
    TransactionRepository,
    WalletRepository,
    CategoryRepository,
    BudgetRepository,
    ReminderRepository,
    GoalRepository,
    ReceiptStorageRepository,
    DashboardRepository,
    NotificationRepository,
    DebtRepository {

    private val mutationMutex = Mutex()
    private val profilePreferences = context.getSharedPreferences("finlux_demo_profile", Context.MODE_PRIVATE)
    private val userState = MutableStateFlow<UserProfile?>(null)
    private val walletState = MutableStateFlow(seedWallets())
    private val categoryState = MutableStateFlow(seedCategories())
    private val transactionState = MutableStateFlow(seedTransactions())
    private val budgetState = MutableStateFlow(seedBudgets())
    private val reminderState = MutableStateFlow(seedReminders())
    private val goalState = MutableStateFlow(seedGoals())
    private val notificationState = MutableStateFlow(seedNotifications())
    private val debtState = MutableStateFlow(seedDebts())
    private val paymentHistoryState = MutableStateFlow<List<DebtPaymentHistory>>(seedPaymentHistory())

    override val currentUser: Flow<UserProfile?> = userState

    override suspend fun signIn(email: String, password: String): AppResult<UserProfile> {
        if (email.isBlank() || password.isBlank()) return AppResult.Error("Vui lòng nhập đủ thông tin")
        return AppResult.Success(demoUser(email)).also { userState.value = it.value }
    }

    override suspend fun register(
        displayName: String,
        email: String,
        password: String,
    ): AppResult<UserProfile> {
        if (displayName.isBlank()) return AppResult.Error("Vui lòng nhập họ tên")
        val user = demoUser(email).copy(displayName = displayName)
        saveDemoProfile(user)
        userState.value = user
        return AppResult.Success(user)
    }

    override suspend fun signInWithGoogle(idToken: String): AppResult<UserProfile> {
        val user = demoUser("google_user@finlux.app").copy(displayName = "Người dùng Google")
        saveDemoProfile(user)
        userState.value = user
        return AppResult.Success(user)
    }

    override suspend fun sendPasswordReset(email: String): AppResult<Unit> =
        if (email.contains('@')) AppResult.Success(Unit) else AppResult.Error("Email không hợp lệ")

    override suspend fun updateDisplayName(displayName: String): AppResult<UserProfile> {
        val normalizedName = displayName.trim()
        if (normalizedName.isBlank()) return AppResult.Error("Tên người dùng không được để trống")
        val current = userState.value ?: return AppResult.Error("Chưa đăng nhập")
        return AppResult.Success(current.copy(displayName = normalizedName).also {
            saveDemoProfile(it)
            userState.value = it
        })
    }

    override suspend fun updateAvatar(jpegBytes: ByteArray): AppResult<UserProfile> = runCatching {
        val current = userState.value ?: error("Chưa đăng nhập")
        val avatar = context.filesDir.resolve("finlux-avatar-${current.uid}.jpg")
        avatar.writeBytes(jpegBytes)
        current.copy(photoUrl = avatar.toUri().toString()).also { userState.value = it }
    }.fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Error(it.localizedMessage ?: "Không thể lưu ảnh đại diện", it) },
    )

    override suspend fun signOut() {
        userState.value = null
    }

    override fun observeRecent(limit: Int): Flow<List<FinanceTransaction>> =
        transactionState.map { items -> items.sortedByDescending { it.date }.take(limit) }

    override fun observeMonth(month: YearMonth): Flow<List<FinanceTransaction>> {
        val zone = ZoneId.systemDefault()
        val start = month.atDay(1).atStartOfDay(zone).toInstant()
        val end = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant()
        return transactionState.map { items ->
            items.filter { it.date >= start && it.date < end }
        }
    }
    override fun observePeriod(start: Instant, endExclusive: Instant): Flow<List<FinanceTransaction>> =
        transactionState.map { items -> items.filter { it.date >= start && it.date < endExclusive } }


    override fun observeWallets(): Flow<List<Wallet>> = walletState

    override fun observeCategories(): Flow<List<Category>> = categoryState

    override fun observeBudgets(periodKey: String): Flow<List<Budget>> =
        budgetState.map { budgets -> budgets.filter { it.periodKey == periodKey } }

    override fun observeReminders(): Flow<List<Reminder>> = reminderState

    override fun observeGoals(): Flow<List<FinancialGoal>> = goalState

    override fun observeNotifications(): Flow<List<AppNotification>> =
        notificationState.map { items -> items.sortedByDescending { it.timestamp } }

    override suspend fun saveNotification(notification: AppNotification): AppResult<String> = mutationMutex.withLock {
        val id = notification.id.ifBlank { UUID.randomUUID().toString() }
        val stored = notification.copy(id = id)
        notificationState.value = listOf(stored) + notificationState.value.filterNot { it.id == id }
        AppResult.Success(id)
    }

    override suspend fun markAsRead(id: String): AppResult<Unit> = mutationMutex.withLock {
        notificationState.value = notificationState.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        AppResult.Success(Unit)
    }

    override suspend fun markAsPaid(id: String): AppResult<Unit> = mutationMutex.withLock {
        notificationState.value = notificationState.value.map {
            if (it.id == id) it.copy(isRead = true, isPaid = true) else it
        }
        AppResult.Success(Unit)
    }

    override suspend fun markAsPaidWithAmount(
        id: String,
        amount: Money,
        newBody: String?,
    ): AppResult<Unit> = mutationMutex.withLock {
        notificationState.value = notificationState.value.map {
            if (it.id == id) {
                it.copy(
                    isRead = true,
                    isPaid = true,
                    amount = amount,
                    body = newBody ?: it.body,
                )
            } else it
        }
        AppResult.Success(Unit)
    }

    override suspend fun markAsPaidByReminderId(reminderId: String): AppResult<Unit> = mutationMutex.withLock {
        notificationState.value = notificationState.value.map {
            if (it.reminderId == reminderId) it.copy(isRead = true, isPaid = true) else it
        }
        AppResult.Success(Unit)
    }

    override suspend fun clearAll(): AppResult<Unit> = mutationMutex.withLock {
        notificationState.value = emptyList()
        AppResult.Success(Unit)
    }

    override suspend fun uploadReceipt(localUri: String): AppResult<String> =
        if (localUri.isBlank()) AppResult.Error("Không tìm thấy ảnh hóa đơn") else AppResult.Success(localUri)

    override suspend fun upsertGoal(goal: FinancialGoal): AppResult<String> = mutationMutex.withLock {
        val id = goal.id.ifBlank { UUID.randomUUID().toString() }
        val stored = goal.copy(id = id)
        goalState.value = if (goalState.value.any { it.id == id }) {
            goalState.value.map { if (it.id == id) stored else it }
        } else goalState.value + stored
        AppResult.Success(id)
    }

    override suspend fun deleteGoal(goal: FinancialGoal): AppResult<Unit> = mutationMutex.withLock {
        goalState.value = goalState.value.filterNot { it.id == goal.id }
        AppResult.Success(Unit)
    }

    override suspend fun depositToGoal(
        goalId: String,
        walletId: String,
        amount: Long,
        note: String,
        date: Instant,
    ): AppResult<Unit> = mutationMutex.withLock {
        val targetGoal = goalState.value.find { it.id == goalId }
            ?: return@withLock AppResult.Error("Không tìm thấy mục tiêu tài chính")
        val targetWallet = walletState.value.find { it.id == walletId }
            ?: return@withLock AppResult.Error("Không tìm thấy ví nguồn")

        if (targetWallet.type != WalletType.CARD && targetWallet.balance.value < amount) {
            return@withLock AppResult.Error("Số dư ví không đủ để nạp vào mục tiêu")
        }

        if (!changeWalletBalance(walletId, -amount)) {
            return@withLock AppResult.Error("Lỗi cập nhật số dư ví")
        }

        val newSaved = targetGoal.savedAmount.value + amount
        goalState.value = goalState.value.map {
            if (it.id == goalId) it.copy(savedAmount = Money(newSaved)) else it
        }

        val tx = FinanceTransaction(
            id = UUID.randomUUID().toString(),
            type = TransactionType.EXPENSE,
            amount = Money(amount),
            categoryId = "savings",
            walletId = walletId,
            note = if (note.isNotBlank()) note else "Nạp tích lũy: ${targetGoal.name}",
            date = date,
            createdAt = date,
            updatedAt = date,
        )
        transactionState.value = transactionState.value + tx
        AppResult.Success(Unit)
    }

    override suspend fun withdrawFromGoal(
        goalId: String,
        walletId: String,
        amount: Long,
        note: String,
        date: Instant,
    ): AppResult<Unit> = mutationMutex.withLock {
        val targetGoal = goalState.value.find { it.id == goalId }
            ?: return@withLock AppResult.Error("Không tìm thấy mục tiêu tài chính")
        if (targetGoal.savedAmount.value < amount) {
            return@withLock AppResult.Error("Số tiền tích lũy hiện tại nhỏ hơn số tiền muốn rút")
        }
        if (walletState.value.none { it.id == walletId }) {
            return@withLock AppResult.Error("Không tìm thấy ví nhận tiền")
        }

        if (!changeWalletBalance(walletId, amount)) {
            return@withLock AppResult.Error("Lỗi cập nhật số dư ví")
        }

        val newSaved = targetGoal.savedAmount.value - amount
        goalState.value = goalState.value.map {
            if (it.id == goalId) it.copy(savedAmount = Money(newSaved)) else it
        }

        val tx = FinanceTransaction(
            id = UUID.randomUUID().toString(),
            type = TransactionType.INCOME,
            amount = Money(amount),
            categoryId = "savings",
            walletId = walletId,
            note = if (note.isNotBlank()) note else "Rút tích lũy: ${targetGoal.name}",
            date = date,
            createdAt = date,
            updatedAt = date,
        )
        transactionState.value = transactionState.value + tx
        AppResult.Success(Unit)
    }

    override fun observeDebts(): Flow<List<DebtAccount>> = debtState

    override fun observePaymentHistory(debtId: String): Flow<List<DebtPaymentHistory>> =
        paymentHistoryState.map { list -> list.filter { it.debtId == debtId } }

    override fun observeAllPaymentHistory(): Flow<List<DebtPaymentHistory>> = paymentHistoryState

    override suspend fun upsertDebt(debt: DebtAccount): AppResult<String> = mutationMutex.withLock {
        val id = debt.id.ifBlank { UUID.randomUUID().toString() }
        val stored = debt.copy(id = id)
        debtState.value = if (debtState.value.any { it.id == id }) {
            debtState.value.map { if (it.id == id) stored else it }
        } else debtState.value + stored
        AppResult.Success(id)
    }

    override suspend fun deleteDebt(debt: DebtAccount): AppResult<Unit> = mutationMutex.withLock {
        debtState.value = debtState.value.filterNot { it.id == debt.id }
        paymentHistoryState.value = paymentHistoryState.value.filterNot { it.debtId == debt.id }
        AppResult.Success(Unit)
    }

    override suspend fun processPayment(
        debtId: String,
        walletId: String,
        amount: Long,
        principalPaid: Long,
        interestPaid: Long,
        note: String,
        paymentDate: Instant,
    ): AppResult<Unit> = mutationMutex.withLock {
        val targetDebt = debtState.value.find { it.id == debtId }
            ?: return@withLock AppResult.Error("Không tìm thấy khoản nợ")
        val targetWallet = walletState.value.find { it.id == walletId }
            ?: return@withLock AppResult.Error("Không tìm thấy ví thanh toán")

        if (targetWallet.type != WalletType.CARD && targetWallet.balance.value < amount) {
            return@withLock AppResult.Error("Số dư ví không đủ để thanh toán nợ")
        }

        if (!changeWalletBalance(walletId, -amount)) {
            return@withLock AppResult.Error("Lỗi cập nhật số dư ví")
        }

        val newRemaining = (targetDebt.remainingBalance.value - principalPaid).coerceAtLeast(0L)
        val isSettled = newRemaining <= 0L
        debtState.value = debtState.value.map {
            if (it.id == debtId) it.copy(
                remainingBalance = Money(newRemaining),
                isSettled = isSettled,
                updatedAt = paymentDate,
            ) else it
        }

        val paymentHistory = DebtPaymentHistory(
            id = UUID.randomUUID().toString(),
            debtId = debtId,
            walletId = walletId,
            amount = Money(amount),
            principalPaid = Money(principalPaid),
            interestPaid = Money(interestPaid),
            paymentDate = paymentDate,
            note = note,
        )
        paymentHistoryState.value = listOf(paymentHistory) + paymentHistoryState.value

        val tx = FinanceTransaction(
            id = UUID.randomUUID().toString(),
            type = TransactionType.EXPENSE,
            amount = Money(amount),
            categoryId = "debt_payment",
            walletId = walletId,
            note = if (note.isNotBlank()) note else "Thanh toán nợ: ${targetDebt.name}",
            date = paymentDate,
            createdAt = paymentDate,
            updatedAt = paymentDate,
        )
        transactionState.value = transactionState.value + tx

        AppResult.Success(Unit)
    }

    override suspend fun upsertWallet(wallet: Wallet): AppResult<String> = mutationMutex.withLock {
        val id = wallet.id.ifBlank { UUID.randomUUID().toString() }
        val existing = walletState.value.find { it.id == id }
        val stored = if (existing != null) {
            // Update metadata only, preserve actual balance
            wallet.copy(id = id, balance = existing.balance)
        } else {
            wallet.copy(id = id)
        }
        walletState.value = if (existing != null) {
            walletState.value.map {
                if (it.id == id) stored
                else if (wallet.isDefault) it.copy(isDefault = false)
                else it
            }
        } else {
            val list = if (wallet.isDefault) walletState.value.map { it.copy(isDefault = false) } else walletState.value
            list + stored
        }
        AppResult.Success(id)
    }

    override suspend fun deleteWallet(wallet: Wallet): AppResult<Unit> = mutationMutex.withLock {
        if (wallet.isDefault) return@withLock AppResult.Error("Không thể xóa ví mặc định")
        val hasTransactions = transactionState.value.any { it.walletId == wallet.id || it.relatedWalletId == wallet.id }
        if (hasTransactions) {
            // Archive wallet instead of hard delete
            walletState.value = walletState.value.map {
                if (it.id == wallet.id) it.copy(status = "archived", archivedAt = Instant.now()) else it
            }
        } else {
            walletState.value = walletState.value.filterNot { it.id == wallet.id }
        }
        AppResult.Success(Unit)
    }

    override suspend fun upsertCategory(category: Category): AppResult<String> = mutationMutex.withLock {
        val id = category.id.ifBlank { UUID.randomUUID().toString() }
        val stored = category.copy(id = id)
        categoryState.value = if (categoryState.value.any { it.id == id }) {
            categoryState.value.map { if (it.id == id) stored else it }
        } else categoryState.value + stored
        AppResult.Success(id)
    }

    override suspend fun deleteCategory(category: Category): AppResult<Unit> = mutationMutex.withLock {
        if (category.isDefault || transactionState.value.any { it.categoryId == category.id }) {
            return@withLock AppResult.Error("Danh mục mặc định hoặc đã phát sinh giao dịch không thể xóa")
        }
        categoryState.value = categoryState.value.filterNot { it.id == category.id }
        AppResult.Success(Unit)
    }

    override suspend fun upsertBudget(budget: Budget): AppResult<String> = mutationMutex.withLock {
        val id = budget.id.ifBlank { "${budget.categoryId}_${budget.month}" }
        val stored = budget.copy(id = id)
        budgetState.value = budgetState.value.filterNot { it.id == id } + stored
        AppResult.Success(id)
    }

    override suspend fun deleteBudget(budget: Budget): AppResult<Unit> = mutationMutex.withLock {
        budgetState.value = budgetState.value.filterNot { it.id == budget.id }
        AppResult.Success(Unit)
    }

    override suspend fun upsertReminder(reminder: Reminder): AppResult<String> = mutationMutex.withLock {
        val id = reminder.id.ifBlank { UUID.randomUUID().toString() }
        val stored = reminder.copy(id = id)
        reminderState.value = reminderState.value.filterNot { it.id == id } + stored
        AppResult.Success(id)
    }

    override suspend fun deleteReminder(reminder: Reminder): AppResult<Unit> = mutationMutex.withLock {
        reminderState.value = reminderState.value.filterNot { it.id == reminder.id }
        AppResult.Success(Unit)
    }

    override fun observeCurrentMonthSummary(): Flow<DashboardSummary> =
        transactionState.map { transactions ->
            val month = YearMonth.now()
            val inMonth = transactions.filter {
                YearMonth.from(it.date.atZone(ZoneId.systemDefault())) == month
            }
            val income = inMonth.filter { it.type == TransactionType.INCOME }.sumOf { it.amount.value }
            val expense = inMonth.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount.value }
            DashboardSummary(Money(income), Money(expense), income - expense)
        }

    override suspend fun addWithBalanceUpdate(transaction: FinanceTransaction): AppResult<String> =
        mutationMutex.withLock {
            val id = transaction.id.ifBlank { UUID.randomUUID().toString() }
            val stored = transaction.copy(id = id, createdAt = Instant.now(), updatedAt = Instant.now())
            val targetWallet = walletState.value.find { it.id == stored.walletId }
                ?: return@withLock AppResult.Error("Không tìm thấy ví")
            if (stored.type == TransactionType.EXPENSE && targetWallet.type != WalletType.CARD && targetWallet.balance.value < stored.amount.value) {
                return@withLock AppResult.Error("Số dư ví [${targetWallet.name}] không đủ để thực hiện chi tiêu")
            }
            if (!changeWalletBalance(stored.walletId, balanceDelta(stored))) {
                return@withLock AppResult.Error("Không tìm thấy ví")
            }
            transactionState.value = transactionState.value + stored
            AppResult.Success(id)
        }

    override suspend fun editWithBalanceUpdate(
        original: FinanceTransaction,
        updated: FinanceTransaction,
    ): AppResult<Unit> = mutationMutex.withLock {
        val current = transactionState.value.firstOrNull { it.id == original.id }
            ?: return@withLock AppResult.Error("Không tìm thấy giao dịch")
        val walletSnapshot = walletState.value
        if (!changeWalletBalance(current.walletId, -balanceDelta(current)) ||
            !changeWalletBalance(updated.walletId, balanceDelta(updated))
        ) {
            // Restore the in-memory snapshot to mirror Firestore transaction rollback semantics.
            walletState.value = walletSnapshot
            return@withLock AppResult.Error("Không tìm thấy ví")
        }
        transactionState.value = transactionState.value.map {
            if (it.id == current.id) updated.copy(updatedAt = Instant.now()) else it
        }
        AppResult.Success(Unit)
    }

    override suspend fun deleteWithBalanceUpdate(transaction: FinanceTransaction): AppResult<Unit> =
        mutationMutex.withLock {
            val current = transactionState.value.firstOrNull { it.id == transaction.id }
                ?: return@withLock AppResult.Error("Không tìm thấy giao dịch")
            if (!changeWalletBalance(current.walletId, -balanceDelta(current))) {
                return@withLock AppResult.Error("Không tìm thấy ví")
            }
            transactionState.value = transactionState.value.filterNot { it.id == current.id }
            AppResult.Success(Unit)
        }

    override suspend fun transferBetweenWallets(
        sourceWalletId: String,
        destinationWalletId: String,
        amount: Long,
        note: String,
        date: Instant,
    ): AppResult<Unit> = mutationMutex.withLock {
        if (sourceWalletId == destinationWalletId) return@withLock AppResult.Error("Hai ví phải khác nhau")
        if (amount <= 0L) return@withLock AppResult.Error("Số tiền phải lớn hơn 0")
        val snapshot = walletState.value
        val source = snapshot.find { it.id == sourceWalletId } ?: return@withLock AppResult.Error("Không tìm thấy ví nguồn")
        if (snapshot.none { it.id == destinationWalletId }) return@withLock AppResult.Error("Không tìm thấy ví đích")
        if (source.type != WalletType.CARD && source.balance.value < amount) {
            return@withLock AppResult.Error("Số dư ví nguồn không đủ để thực hiện chuyển tiền")
        }
        if (!changeWalletBalance(sourceWalletId, -amount) || !changeWalletBalance(destinationWalletId, amount)) {
            walletState.value = snapshot
            return@withLock AppResult.Error("Không tìm thấy ví")
        }
        val pairId = UUID.randomUUID().toString()
        val now = Instant.now()
        transactionState.value = transactionState.value + listOf(
            FinanceTransaction(
                id = "${pairId}_out",
                type = TransactionType.TRANSFER_OUT,
                amount = Money(amount),
                categoryId = null,
                walletId = sourceWalletId,
                relatedWalletId = destinationWalletId,
                note = note,
                date = date,
                createdAt = now,
                updatedAt = now,
            ),
            FinanceTransaction(
                id = "${pairId}_in",
                type = TransactionType.TRANSFER_IN,
                amount = Money(amount),
                categoryId = null,
                walletId = destinationWalletId,
                relatedWalletId = sourceWalletId,
                note = note,
                date = date,
                createdAt = now,
                updatedAt = now,
            ),
        )
        AppResult.Success(Unit)
    }
    override suspend fun executeSalaryRolloverAtomic(
        cycleKey: String,
        sourceWalletId: String,
        destinationWalletId: String,
        amount: Long,
        note: String,
        date: Instant,
    ): AppResult<Unit> = transferBetweenWallets(sourceWalletId, destinationWalletId, amount, note, date)

    private fun changeWalletBalance(walletId: String, delta: Long): Boolean {
        val target = walletState.value.find { it.id == walletId } ?: return false
        val newBalance = target.balance.value + delta
        if (target.type != WalletType.CARD && newBalance < 0) {
            return false
        }
        walletState.value = walletState.value.map { wallet ->
            if (wallet.id == walletId) wallet.copy(balance = Money(newBalance)) else wallet
        }
        return true
    }

    private fun balanceDelta(transaction: FinanceTransaction): Long = when (transaction.type) {
        TransactionType.INCOME, TransactionType.TRANSFER_IN -> transaction.amount.value
        TransactionType.EXPENSE, TransactionType.TRANSFER_OUT -> -transaction.amount.value
    }

    private fun demoUser(email: String): UserProfile {
        val normalizedEmail = email.trim()
        val savedEmail = profilePreferences.getString("email", null)
        val savedName = profilePreferences.getString("displayName", null)
        val displayName = savedName
            ?.takeIf { savedEmail.equals(normalizedEmail, ignoreCase = true) && it.isNotBlank() }
            ?: normalizedEmail.substringBefore('@').ifBlank { "Người dùng" }
        return UserProfile("demo-user", displayName, normalizedEmail)
    }

    private fun saveDemoProfile(user: UserProfile) {
        profilePreferences.edit()
            .putString("displayName", user.displayName)
            .putString("email", user.email)
            .apply()
    }

    private companion object {
        fun seedWallets() = listOf(
            Wallet("cash", "Tiền mặt", WalletType.CASH, Money(5_750_000), "#1F6FBF", true, Instant.now()),
            Wallet("bank", "MB Bank", WalletType.BANK, Money(18_420_000), "#3478F6", false, Instant.now()),
            Wallet("vietcombank", "Vietcombank", WalletType.BANK, Money(25_000_000), "#168A62", false, Instant.now()),
            Wallet("momo", "Ví MoMo", WalletType.EWALLET, Money(8_250_000), "#EC4899", false, Instant.now()),
            Wallet("card", "Thẻ tín dụng", WalletType.CARD, Money(-2_000_000), "#7758F6", false, Instant.now()),
            Wallet("investment", "Ví đầu tư", WalletType.INVESTMENT, Money(9_500_000), "#14B8A6", false, Instant.now()),
        )

        fun seedCategories() = listOf(
            Category("food", "Ăn uống", CategoryType.EXPENSE, "restaurant", "#D94B5B", true, Instant.now(), isEssential = true),
            Category("transport", "Di chuyển", CategoryType.EXPENSE, "directions_car", "#E6A23C", true, Instant.now(), isEssential = true),
            Category("shopping", "Mua sắm", CategoryType.EXPENSE, "shopping_bag", "#7758F6", true, Instant.now(), isEssential = false),
            Category("bills", "Hóa đơn", CategoryType.EXPENSE, "receipt_long", "#3478F6", true, Instant.now(), isEssential = true),
            Category("home", "Nhà ở", CategoryType.EXPENSE, "home", "#14B8A6", true, Instant.now(), isEssential = true),
            Category("health", "Sức khỏe", CategoryType.EXPENSE, "health", "#EC4899", true, Instant.now(), isEssential = true),
            Category("travel", "Du lịch", CategoryType.EXPENSE, "flight", "#47C8FF", true, Instant.now(), isEssential = false),
            Category("debt_payment", "Trả nợ & Tín dụng", CategoryType.EXPENSE, "credit_card", "#E11D48", true, Instant.now(), isEssential = true),
            Category("savings", "Tích lũy & Mục tiêu", CategoryType.EXPENSE, "savings", "#8B5CF6", true, Instant.now(), isEssential = true),
            Category("salary", "Lương", CategoryType.INCOME, "payments", "#168A62", true, Instant.now(), isEssential = true),
            Category("bonus", "Thưởng", CategoryType.INCOME, "workspace_premium", "#47C8FF", true, Instant.now(), isEssential = true),
            Category("freelance", "Freelance", CategoryType.INCOME, "work", "#7758F6", true, Instant.now(), isEssential = true),
            Category("interest", "Lãi ngân hàng", CategoryType.INCOME, "account_balance", "#3478F6", true, Instant.now(), isEssential = true),
            Category("refund", "Hoàn tiền", CategoryType.INCOME, "payments", "#E6A23C", true, Instant.now(), isEssential = true),
            Category("investment-income", "Đầu tư", CategoryType.INCOME, "show_chart", "#14B8A6", true, Instant.now(), isEssential = true),
        )

        fun seedTransactions() = listOf(
            FinanceTransaction("demo-1", TransactionType.EXPENSE, Money(350_000), "food", "cash", note = "Siêu thị WinMart", date = Instant.now()),
            FinanceTransaction("demo-2", TransactionType.INCOME, Money(15_000_000), "salary", "bank", note = "Lương công ty", date = Instant.now().minus(1, ChronoUnit.DAYS)),
            FinanceTransaction("demo-3", TransactionType.EXPENSE, Money(450_000), "food", "card", note = "Cafe Highlands", date = Instant.now().minus(2, ChronoUnit.DAYS)),
            FinanceTransaction("demo-4", TransactionType.EXPENSE, Money(2_000_000), "shopping", "bank", note = "Mua sắm", date = Instant.now().minus(3, ChronoUnit.DAYS)),
            FinanceTransaction("demo-5", TransactionType.INCOME, Money(3_000_000), "bonus", "bank", note = "Tiền thưởng", date = Instant.now().minus(4, ChronoUnit.DAYS)),
            FinanceTransaction("demo-6", TransactionType.EXPENSE, Money(820_000), "bills", "bank", note = "Điện, nước", date = Instant.now().minus(5, ChronoUnit.DAYS)),
            FinanceTransaction("demo-7", TransactionType.EXPENSE, Money(240_000), "transport", "cash", note = "Di chuyển", date = Instant.now().minus(6, ChronoUnit.DAYS)),
            FinanceTransaction("demo-8", TransactionType.EXPENSE, Money(1_200_000), "home", "bank", note = "Đồ dùng gia đình", date = Instant.now().minus(14, ChronoUnit.DAYS)),
            FinanceTransaction("demo-9", TransactionType.EXPENSE, Money(680_000), "health", "card", note = "Khám sức khỏe", date = Instant.now().minus(22, ChronoUnit.DAYS)),
            FinanceTransaction("demo-10", TransactionType.INCOME, Money(15_000_000), "salary", "bank", note = "Lương tháng trước", date = Instant.now().minus(35, ChronoUnit.DAYS)),
            FinanceTransaction("demo-11", TransactionType.EXPENSE, Money(3_200_000), "travel", "bank", note = "Chuyến đi Đà Nẵng", date = Instant.now().minus(40, ChronoUnit.DAYS)),
            FinanceTransaction("demo-12", TransactionType.EXPENSE, Money(2_450_000), "food", "cash", note = "Ăn uống tháng trước", date = Instant.now().minus(48, ChronoUnit.DAYS)),
            FinanceTransaction("demo-13", TransactionType.INCOME, Money(14_500_000), "salary", "bank", note = "Lương hai tháng trước", date = Instant.now().minus(70, ChronoUnit.DAYS)),
            FinanceTransaction("demo-14", TransactionType.INCOME, Money(5_000_000), "freelance", "vietcombank", note = "Freelance thiết kế", date = Instant.now().minus(7, ChronoUnit.DAYS)),
            FinanceTransaction("demo-15", TransactionType.INCOME, Money(850_000), "interest", "bank", note = "Lãi tiền gửi", date = Instant.now().minus(9, ChronoUnit.DAYS)),
            FinanceTransaction("demo-16", TransactionType.INCOME, Money(500_000), "refund", "momo", note = "Hoàn tiền mua sắm", date = Instant.now().minus(11, ChronoUnit.DAYS)),
            FinanceTransaction("demo-17", TransactionType.INCOME, Money(1_200_000), "investment-income", "investment", note = "Cổ tức đầu tư", date = Instant.now().minus(13, ChronoUnit.DAYS)),
        )

        fun seedBudgets() = listOf(
            Budget(id = "food_MONTHLY_${YearMonth.now()}", categoryId = "food", periodKey = "MONTHLY_${YearMonth.now()}", limitAmount = Money(3_000_000), spentAmount = Money(1_850_000), notified80 = false, notified100 = false),
            Budget(id = "shopping_MONTHLY_${YearMonth.now()}", categoryId = "shopping", periodKey = "MONTHLY_${YearMonth.now()}", limitAmount = Money(4_000_000), spentAmount = Money(2_000_000), notified80 = false, notified100 = false),
            Budget(id = "transport_MONTHLY_${YearMonth.now()}", categoryId = "transport", periodKey = "MONTHLY_${YearMonth.now()}", limitAmount = Money(1_500_000), spentAmount = Money(240_000), notified80 = false, notified100 = false),
            Budget(id = "bills_MONTHLY_${YearMonth.now()}", categoryId = "bills", periodKey = "MONTHLY_${YearMonth.now()}", limitAmount = Money(1_200_000), spentAmount = Money(820_000), notified80 = false, notified100 = false),
            Budget(id = "food_MONTHLY_${YearMonth.now().minusMonths(1)}", categoryId = "food", periodKey = "MONTHLY_${YearMonth.now().minusMonths(1)}", limitAmount = Money(2_800_000), spentAmount = Money(2_450_000), notified80 = true, notified100 = false),
            Budget(id = "shopping_MONTHLY_${YearMonth.now().minusMonths(1)}", categoryId = "shopping", periodKey = "MONTHLY_${YearMonth.now().minusMonths(1)}", limitAmount = Money(3_500_000), spentAmount = Money(3_120_000), notified80 = true, notified100 = false),
            Budget(id = "food_MONTHLY_${YearMonth.now().minusMonths(2)}", categoryId = "food", periodKey = "MONTHLY_${YearMonth.now().minusMonths(2)}", limitAmount = Money(2_500_000), spentAmount = Money(2_680_000), notified80 = true, notified100 = true),
        )

        fun seedReminders() = listOf(
            Reminder(
                id = "rent-reminder",
                title = "Tiền thuê nhà",
                amount = Money(5_000_000),
                categoryId = "bills",
                walletId = "bank",
                recurrence = ReminderRecurrence.MONTHLY,
                startDate = Instant.now().plus(5, ChronoUnit.DAYS),
                enabled = true,
                nextTriggerDate = Instant.now().plus(5, ChronoUnit.DAYS),
            ),
        )

        fun seedNotifications() = listOf(
            AppNotification(
                id = "budget-alert-noti",
                title = "Cảnh báo ngân sách Ăn uống",
                body = "Hạng mục Ăn uống đã tiêu 1.850.000 đ / 3.000.000 đ (61.7% hạn mức). Hãy chú ý chi tiêu nhé!",
                type = com.finlux.app.domain.model.NotificationType.BUDGET_ALERT,
                amount = Money(1_850_000L),
                targetRoute = "budget",
                timestamp = Instant.now().minus(2, ChronoUnit.HOURS),
                isRead = false,
            ),
            AppNotification(
                id = "goal-milestone-noti",
                title = "Chúc mừng! Cột mốc tiết kiệm",
                body = "Mục tiêu 'Quỹ khẩn cấp' đã tích lũy đạt mốc 50% tiến độ (15.000.000 đ). Tuyệt vời!",
                type = com.finlux.app.domain.model.NotificationType.GOAL_MILESTONE,
                amount = Money(15_000_000L),
                targetRoute = "goals",
                timestamp = Instant.now().minus(1, ChronoUnit.DAYS),
                isRead = false,
            ),
            AppNotification(
                id = "reminder-bill-noti",
                title = "Hóa đơn tiền nhà",
                body = "Hóa đơn tiền thuê nhà tháng này sắp tới hạn (5.000.000 đ).",
                type = com.finlux.app.domain.model.NotificationType.REMINDER,
                amount = Money(5_000_000L),
                reminderId = "rent-reminder",
                categoryId = "bills",
                walletId = "bank",
                targetRoute = "reminders",
                timestamp = Instant.now().minus(3, ChronoUnit.DAYS),
                isRead = true,
                isPaid = false,
            ),
            AppNotification(
                id = "tx-summary-noti",
                title = "Báo cáo tài chính tuần qua",
                body = "Tuần qua bạn đã chi tiêu 3.410.000 đ, tiết kiệm được 2.500.000 đ. Bấm để xem chi tiết biểu đồ.",
                type = com.finlux.app.domain.model.NotificationType.TRANSACTION_SUMMARY,
                amount = Money(0L),
                targetRoute = "reports",
                timestamp = Instant.now().minus(5, ChronoUnit.DAYS),
                isRead = true,
            ),
            AppNotification(
                id = "welcome-noti",
                title = "Chào mừng bạn đến với FinLux",
                body = "Hệ thống quản lý tài chính cá nhân đã sẵn sàng đồng hành cùng bạn.",
                type = com.finlux.app.domain.model.NotificationType.SYSTEM,
                amount = Money(0L),
                timestamp = Instant.now().minus(10, ChronoUnit.DAYS),
                isRead = true,
            ),
        )

        fun seedDebts() = listOf(
            DebtAccount(
                id = "debt-vcb-credit",
                userId = "demo-user",
                name = "Thẻ tín dụng VCB Signature",
                type = DebtType.CREDIT_CARD,
                totalAmount = Money(50_000_000L),
                remainingBalance = Money(18_500_000L),
                interestRateApr = 24.0,
                minimumPayment = Money(1_200_000L),
                dueDate = 25,
                statementDate = 10,
                colorHex = "#E11D48",
                isSettled = false,
            ),
            DebtAccount(
                id = "debt-vpbank-auto",
                userId = "demo-user",
                name = "Vay mua ô tô VPBank",
                type = DebtType.BANK_LOAN,
                totalAmount = Money(120_000_000L),
                remainingBalance = Money(65_000_000L),
                interestRateApr = 11.5,
                minimumPayment = Money(3_800_000L),
                dueDate = 15,
                statementDate = null,
                colorHex = "#2563EB",
                isSettled = false,
            ),
            DebtAccount(
                id = "debt-iphone-installment",
                userId = "demo-user",
                name = "Trả góp iPhone 16 Pro Max",
                type = DebtType.INSTALLMENT,
                totalAmount = Money(34_000_000L),
                remainingBalance = Money(14_000_000L),
                interestRateApr = 0.0,
                minimumPayment = Money(2_833_000L),
                dueDate = 5,
                statementDate = null,
                colorHex = "#7C3AED",
                isSettled = false,
            ),
        )

        fun seedPaymentHistory() = listOf(
            DebtPaymentHistory(
                id = "pay-hist-1",
                debtId = "debt-vcb-credit",
                walletId = "bank",
                amount = Money(2_500_000L),
                principalPaid = Money(2_130_000L),
                interestPaid = Money(370_000L),
                paymentDate = Instant.now().minus(5, ChronoUnit.DAYS),
                note = "Thanh toán sao kê tháng trước",
            ),
            DebtPaymentHistory(
                id = "pay-hist-2",
                debtId = "debt-vpbank-auto",
                walletId = "vietcombank",
                amount = Money(3_800_000L),
                principalPaid = Money(3_176_000L),
                interestPaid = Money(624_000L),
                paymentDate = Instant.now().minus(15, ChronoUnit.DAYS),
                note = "Đóng tiền gốc & lãi kỳ 12",
            ),
            DebtPaymentHistory(
                id = "pay-hist-3",
                debtId = "debt-iphone-installment",
                walletId = "momo",
                amount = Money(2_833_000L),
                principalPaid = Money(2_833_000L),
                interestPaid = Money(0L),
                paymentDate = Instant.now().minus(20, ChronoUnit.DAYS),
                note = "Trả góp kỳ 7/12",
            ),
        )

        fun seedGoals() = listOf(
            FinancialGoal(
                id = "goal-emergency-fund",
                name = "Quỹ khẩn cấp 6 tháng",
                targetAmount = Money(30_000_000L),
                savedAmount = Money(15_000_000L),
                deadline = Instant.now().plus(180, ChronoUnit.DAYS),
                category = "An toàn",
                monthlyContribution = Money(2_500_000L),
            ),
            FinancialGoal(
                id = "goal-macbook-m4",
                name = "Macbook Pro M4 Pro",
                targetAmount = Money(45_000_000L),
                savedAmount = Money(18_000_000L),
                deadline = Instant.now().plus(90, ChronoUnit.DAYS),
                category = "Công nghệ",
                monthlyContribution = Money(4_500_000L),
            ),
        )
    }
}
