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
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.model.ReminderRecurrence
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.UserProfile
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.AuthRepository
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.DashboardRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.repository.ReminderRepository
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
    DashboardRepository {

    private val mutationMutex = Mutex()
    private val userState = MutableStateFlow<UserProfile?>(null)
    private val walletState = MutableStateFlow(seedWallets())
    private val categoryState = MutableStateFlow(seedCategories())
    private val transactionState = MutableStateFlow(seedTransactions())
    private val budgetState = MutableStateFlow(seedBudgets())
    private val reminderState = MutableStateFlow(seedReminders())

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
        userState.value = user
        return AppResult.Success(user)
    }

    override suspend fun sendPasswordReset(email: String): AppResult<Unit> =
        if (email.contains('@')) AppResult.Success(Unit) else AppResult.Error("Email không hợp lệ")

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

    override fun observeWallets(): Flow<List<Wallet>> = walletState

    override fun observeCategories(): Flow<List<Category>> = categoryState

    override fun observeBudgets(month: YearMonth): Flow<List<Budget>> =
        budgetState.map { budgets -> budgets.filter { it.month == month } }

    override fun observeReminders(): Flow<List<Reminder>> = reminderState

    override suspend fun upsertWallet(wallet: Wallet): AppResult<String> = mutationMutex.withLock {
        val id = wallet.id.ifBlank { UUID.randomUUID().toString() }
        val stored = wallet.copy(id = id)
        walletState.value = if (walletState.value.any { it.id == id }) {
            walletState.value.map { if (it.id == id) stored else it }
        } else walletState.value + stored
        AppResult.Success(id)
    }

    override suspend fun deleteWallet(wallet: Wallet): AppResult<Unit> = mutationMutex.withLock {
        if (wallet.isDefault) return@withLock AppResult.Error("Không thể xóa ví mặc định")
        if (transactionState.value.any { it.walletId == wallet.id || it.relatedWalletId == wallet.id }) {
            return@withLock AppResult.Error("Ví đã có giao dịch, hãy lưu trữ thay vì xóa")
        }
        walletState.value = walletState.value.filterNot { it.id == wallet.id }
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

    private fun changeWalletBalance(walletId: String, delta: Long): Boolean {
        if (walletState.value.none { it.id == walletId }) return false
        walletState.value = walletState.value.map { wallet ->
            if (wallet.id == walletId) wallet.copy(balance = Money(wallet.balance.value + delta)) else wallet
        }
        return true
    }

    private fun balanceDelta(transaction: FinanceTransaction): Long = when (transaction.type) {
        TransactionType.INCOME, TransactionType.TRANSFER_IN -> transaction.amount.value
        TransactionType.EXPENSE, TransactionType.TRANSFER_OUT -> -transaction.amount.value
    }

    private fun demoUser(email: String) = UserProfile("demo-user", "Anh Khoa", email)

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
            Category("food", "Ăn uống", CategoryType.EXPENSE, "restaurant", "#D94B5B", true, Instant.now()),
            Category("transport", "Di chuyển", CategoryType.EXPENSE, "directions_car", "#E6A23C", true, Instant.now()),
            Category("shopping", "Mua sắm", CategoryType.EXPENSE, "shopping_bag", "#7758F6", true, Instant.now()),
            Category("bills", "Hóa đơn", CategoryType.EXPENSE, "receipt_long", "#3478F6", true, Instant.now()),
            Category("home", "Nhà ở", CategoryType.EXPENSE, "home", "#14B8A6", true, Instant.now()),
            Category("health", "Sức khỏe", CategoryType.EXPENSE, "health", "#EC4899", true, Instant.now()),
            Category("travel", "Du lịch", CategoryType.EXPENSE, "flight", "#47C8FF", true, Instant.now()),
            Category("salary", "Lương", CategoryType.INCOME, "payments", "#168A62", true, Instant.now()),
            Category("bonus", "Thưởng", CategoryType.INCOME, "workspace_premium", "#47C8FF", true, Instant.now()),
            Category("freelance", "Freelance", CategoryType.INCOME, "work", "#7758F6", true, Instant.now()),
            Category("interest", "Lãi ngân hàng", CategoryType.INCOME, "account_balance", "#3478F6", true, Instant.now()),
            Category("refund", "Hoàn tiền", CategoryType.INCOME, "payments", "#E6A23C", true, Instant.now()),
            Category("investment-income", "Đầu tư", CategoryType.INCOME, "show_chart", "#14B8A6", true, Instant.now()),
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
            Budget("food_${YearMonth.now()}", "food", YearMonth.now(), Money(3_000_000), Money(1_850_000), false, false),
            Budget("shopping_${YearMonth.now()}", "shopping", YearMonth.now(), Money(4_000_000), Money(2_000_000), false, false),
            Budget("transport_${YearMonth.now()}", "transport", YearMonth.now(), Money(1_500_000), Money(240_000), false, false),
            Budget("bills_${YearMonth.now()}", "bills", YearMonth.now(), Money(1_200_000), Money(820_000), false, false),
            Budget("food_${YearMonth.now().minusMonths(1)}", "food", YearMonth.now().minusMonths(1), Money(2_800_000), Money(2_450_000), true, false),
            Budget("shopping_${YearMonth.now().minusMonths(1)}", "shopping", YearMonth.now().minusMonths(1), Money(3_500_000), Money(3_120_000), true, false),
            Budget("food_${YearMonth.now().minusMonths(2)}", "food", YearMonth.now().minusMonths(2), Money(2_500_000), Money(2_680_000), true, true),
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
    }
}
