package com.finlux.app.presentation.home

import app.cash.turbine.test
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.AppNotification
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.DashboardSummary
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtPaymentHistory
import com.finlux.app.domain.model.DebtType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.FinancialGoal
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.UserProfile
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.AuthRepository
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.DashboardRepository
import com.finlux.app.domain.repository.DebtRepository
import com.finlux.app.domain.repository.GoalRepository
import com.finlux.app.domain.repository.NotificationRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `state computes netWorth as grossAssets minus totalDebt accurately`() = runTest(testDispatcher) {
        val wallets = listOf(
            Wallet("w1", "Tiền mặt", WalletType.CASH, Money(15_000_000L), "#1F6FBF", true, Instant.now()),
            Wallet("w2", "MB Bank", WalletType.BANK, Money(25_000_000L), "#3478F6", false, Instant.now()),
            Wallet("card", "Thẻ tín dụng", WalletType.CARD, Money(20_000_000L), "#3478F6", false, Instant.now()),
        ) // Total Gross = 40,000,000

        val debts = listOf(
            DebtAccount(
                id = "d1",
                userId = "u1",
                name = "Thẻ tín dụng",
                type = DebtType.CREDIT_CARD,
                totalAmount = Money(30_000_000L),
                remainingBalance = Money(12_000_000L),
                interestRateApr = 20.0,
                minimumPayment = Money(1_000_000L),
                dueDate = 20,
                isSettled = false,
            ),
            DebtAccount(
                id = "d2",
                userId = "u1",
                name = "Vay đã tất toán",
                type = DebtType.PERSONAL_LOAN,
                totalAmount = Money(10_000_000L),
                remainingBalance = Money(0L),
                interestRateApr = 10.0,
                minimumPayment = Money(0L),
                dueDate = 5,
                isSettled = true,
            ),
        ) // Total Debt = 12,000,000

        val calculator = com.finlux.app.domain.usecase.DefaultSalaryCycleCalculator()
        val periodResolver = com.finlux.app.domain.usecase.DefaultFinancialPeriodResolver(calculator)

        val viewModel = HomeViewModel(
            authRepository = FakeAuthRepository(),
            dashboardRepository = FakeDashboardRepository(),
            walletRepository = FakeHomeWalletRepository(wallets),
            transactionRepository = FakeHomeTransactionRepository(),
            categoryRepository = FakeHomeCategoryRepository(),
            budgetRepository = FakeHomeBudgetRepository(),
            notificationRepository = FakeHomeNotificationRepository(),
            debtRepository = FakeHomeDebtRepository(debts),
            goalRepository = FakeHomeGoalRepository(emptyList()),
            salaryCycleRepository = FakeHomeSalaryCycleRepository(),
            financialPeriodResolver = periodResolver,
            calculator = calculator,
            clock = com.finlux.app.core.time.SystemFinanceClock(),
            uiPreferencesRepository = FakeUiPreferencesRepository(),
        )

        viewModel.state.test {
            val initial = awaitItem()
            val state = if (initial.grossAssets == 0L && initial.totalDebt == 0L) awaitItem() else initial
            assertEquals(40_000_000L, state.grossAssets)
            assertEquals(12_000_000L, state.totalDebt)
            assertEquals(28_000_000L, state.netWorth) // 40tr - 12tr = 28tr
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state keeps goal-held savings inside net worth after wallet deposit`() = runTest(testDispatcher) {
        val wallets = listOf(
            Wallet("w1", "Tiền mặt", WalletType.CASH, Money(10_000_000L), "#1F6FBF", true, Instant.now()),
        )
        val goals = listOf(
            FinancialGoal(
                id = "g1",
                name = "Quỹ dự phòng",
                targetAmount = Money(20_000_000L),
                savedAmount = Money(5_000_000L),
                deadline = Instant.now().plusSeconds(86_400),
                category = "savings",
                monthlyContribution = Money(1_000_000L),
            ),
        )
        val debts = listOf(
            DebtAccount(
                id = "d1",
                userId = "u1",
                name = "Khoản vay",
                type = DebtType.PERSONAL_LOAN,
                totalAmount = Money(5_000_000L),
                remainingBalance = Money(2_000_000L),
                interestRateApr = 10.0,
                minimumPayment = Money(500_000L),
                dueDate = 5,
                isSettled = false,
            ),
        )
        val calculator = com.finlux.app.domain.usecase.DefaultSalaryCycleCalculator()
        val periodResolver = com.finlux.app.domain.usecase.DefaultFinancialPeriodResolver(calculator)

        val viewModel = HomeViewModel(
            authRepository = FakeAuthRepository(),
            dashboardRepository = FakeDashboardRepository(),
            walletRepository = FakeHomeWalletRepository(wallets),
            transactionRepository = FakeHomeTransactionRepository(),
            categoryRepository = FakeHomeCategoryRepository(),
            budgetRepository = FakeHomeBudgetRepository(),
            notificationRepository = FakeHomeNotificationRepository(),
            debtRepository = FakeHomeDebtRepository(debts),
            goalRepository = FakeHomeGoalRepository(goals),
            salaryCycleRepository = FakeHomeSalaryCycleRepository(),
            financialPeriodResolver = periodResolver,
            calculator = calculator,
            clock = com.finlux.app.core.time.SystemFinanceClock(),
            uiPreferencesRepository = FakeUiPreferencesRepository(),
        )

        viewModel.state.test {
            val initial = awaitItem()
            val state = if (initial.grossAssets == 0L && initial.goalAssets == 0L) awaitItem() else initial
            assertEquals(10_000_000L, state.grossAssets)
            assertEquals(5_000_000L, state.goalAssets)
            assertEquals(2_000_000L, state.totalDebt)
            assertEquals(13_000_000L, state.netWorth)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleBalanceVisibility updates showBalance in state and repository`() = runTest(testDispatcher) {
        val calculator = com.finlux.app.domain.usecase.DefaultSalaryCycleCalculator()
        val periodResolver = com.finlux.app.domain.usecase.DefaultFinancialPeriodResolver(calculator)
        val uiRepo = FakeUiPreferencesRepository()

        val viewModel = HomeViewModel(
            authRepository = FakeAuthRepository(),
            dashboardRepository = FakeDashboardRepository(),
            walletRepository = FakeHomeWalletRepository(emptyList()),
            transactionRepository = FakeHomeTransactionRepository(),
            categoryRepository = FakeHomeCategoryRepository(),
            budgetRepository = FakeHomeBudgetRepository(),
            notificationRepository = FakeHomeNotificationRepository(),
            debtRepository = FakeHomeDebtRepository(emptyList()),
            goalRepository = FakeHomeGoalRepository(emptyList()),
            salaryCycleRepository = FakeHomeSalaryCycleRepository(),
            financialPeriodResolver = periodResolver,
            calculator = calculator,
            clock = com.finlux.app.core.time.SystemFinanceClock(),
            uiPreferencesRepository = uiRepo,
        )

        viewModel.state.test {
            val state1 = awaitItem()
            assertEquals(true, state1.showBalance)

            viewModel.toggleBalanceVisibility()
            testScheduler.advanceUntilIdle()

            val state2 = awaitItem()
            assertEquals(false, state2.showBalance)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state reflects salary cycle transactions and summary when salary cycle is enabled`() = runTest(testDispatcher) {
        val calculator = com.finlux.app.domain.usecase.DefaultSalaryCycleCalculator()
        val periodResolver = com.finlux.app.domain.usecase.DefaultFinancialPeriodResolver(calculator)
        val salaryConfig = com.finlux.app.domain.model.SalaryCycleConfig(
            enabled = true,
            paydayDay = 10,
        )

        val periodTxs = listOf(
            FinanceTransaction(
                id = "tx1",
                type = TransactionType.INCOME,
                amount = Money(25_000_000L),
                categoryId = null,
                walletId = "w1",
                date = Instant.now(),
            ),
            FinanceTransaction(
                id = "tx2",
                type = TransactionType.EXPENSE,
                amount = Money(5_000_000L),
                categoryId = "cat1",
                walletId = "w1",
                date = Instant.now(),
            ),
        )

        val transactionRepo = object : TransactionRepository by FakeHomeTransactionRepository() {
            override fun observePeriod(start: Instant, endExclusive: Instant): Flow<List<FinanceTransaction>> =
                flowOf(periodTxs)
        }

        val salaryRepo = object : com.finlux.app.domain.repository.SalaryCycleRepository by FakeHomeSalaryCycleRepository() {
            override fun observeConfig(): Flow<com.finlux.app.domain.model.SalaryCycleConfig> = flowOf(salaryConfig)
        }

        val viewModel = HomeViewModel(
            authRepository = FakeAuthRepository(),
            dashboardRepository = FakeDashboardRepository(),
            walletRepository = FakeHomeWalletRepository(emptyList()),
            transactionRepository = transactionRepo,
            categoryRepository = FakeHomeCategoryRepository(),
            budgetRepository = FakeHomeBudgetRepository(),
            notificationRepository = FakeHomeNotificationRepository(),
            debtRepository = FakeHomeDebtRepository(emptyList()),
            goalRepository = FakeHomeGoalRepository(emptyList()),
            salaryCycleRepository = salaryRepo,
            financialPeriodResolver = periodResolver,
            calculator = calculator,
            clock = com.finlux.app.core.time.SystemFinanceClock(),
            uiPreferencesRepository = FakeUiPreferencesRepository(),
        )

        viewModel.state.test {
            val initial = awaitItem()
            val state = if (initial.summary.income.value == 0L) awaitItem() else initial
            assertEquals(25_000_000L, state.summary.income.value)
            assertEquals(5_000_000L, state.summary.expense.value)
            assertEquals(20_000_000L, state.summary.net)
            assertEquals(2, state.monthTransactions.size)
            org.junit.jupiter.api.Assertions.assertNotNull(state.salaryCycleLabel)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state reflects active budgets and total budget metrics accurately`() = runTest(testDispatcher) {
        val calculator = com.finlux.app.domain.usecase.DefaultSalaryCycleCalculator()
        val periodResolver = com.finlux.app.domain.usecase.DefaultFinancialPeriodResolver(calculator)

        val activeBudgets = listOf(
            Budget(
                id = "b1",
                categoryId = "cat1",
                periodKey = "2026-08",
                limitAmount = Money(5_000_000L),
                spentAmount = Money(0L),
                notified80 = false,
                notified100 = false,
            ),
            Budget(
                id = "b2",
                categoryId = "cat2",
                periodKey = "2026-08",
                limitAmount = Money(2_000_000L),
                spentAmount = Money(0L),
                notified80 = false,
                notified100 = false,
            ),
        )

        val txs = listOf(
            FinanceTransaction(
                id = "tx1",
                type = TransactionType.EXPENSE,
                amount = Money(3_500_000L),
                categoryId = "cat1",
                walletId = "w1",
                date = Instant.now(),
            ),
            FinanceTransaction(
                id = "tx2",
                type = TransactionType.EXPENSE,
                amount = Money(1_000_000L),
                categoryId = "cat2",
                walletId = "w1",
                date = Instant.now(),
            ),
        )

        val budgetRepo = object : BudgetRepository by FakeHomeBudgetRepository() {
            override fun observeBudgets(periodKey: String): Flow<List<Budget>> = flowOf(activeBudgets)
        }

        val txRepo = object : TransactionRepository by FakeHomeTransactionRepository() {
            override fun observeMonth(month: YearMonth): Flow<List<FinanceTransaction>> = flowOf(txs)
        }

        val viewModel = HomeViewModel(
            authRepository = FakeAuthRepository(),
            dashboardRepository = FakeDashboardRepository(),
            walletRepository = FakeHomeWalletRepository(emptyList()),
            transactionRepository = txRepo,
            categoryRepository = FakeHomeCategoryRepository(),
            budgetRepository = budgetRepo,
            notificationRepository = FakeHomeNotificationRepository(),
            debtRepository = FakeHomeDebtRepository(emptyList()),
            goalRepository = FakeHomeGoalRepository(emptyList()),
            salaryCycleRepository = FakeHomeSalaryCycleRepository(),
            financialPeriodResolver = periodResolver,
            calculator = calculator,
            clock = com.finlux.app.core.time.SystemFinanceClock(),
            uiPreferencesRepository = FakeUiPreferencesRepository(),
        )

        viewModel.state.test {
            val initial = awaitItem()
            val state = if (initial.budgets.isEmpty()) awaitItem() else initial
            assertEquals(2, state.budgets.size)
            assertEquals(7_000_000L, state.totalBudgetLimit)
            assertEquals(4_500_000L, state.totalBudgetSpent)
            assertEquals(64, state.totalBudgetPercent) // (4.5 / 7) * 100 = 64%
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private class FakeAuthRepository : AuthRepository {
    override val currentUser: Flow<UserProfile?> = flowOf(UserProfile("u1", "Văn Khoai", "khoai@finlux.app"))
    override suspend fun signIn(email: String, password: String) = AppResult.Success(UserProfile("u1", "Văn Khoai", email))
    override suspend fun register(displayName: String, email: String, password: String) = AppResult.Success(UserProfile("u1", displayName, email))
    override suspend fun signInWithGoogle(idToken: String) = AppResult.Success(UserProfile("u1", "Văn Khoai", "g@finlux.app"))
    override suspend fun sendPasswordReset(email: String) = AppResult.Success(Unit)
    override suspend fun updateDisplayName(displayName: String) = AppResult.Success(UserProfile("u1", displayName, ""))
    override suspend fun updateAvatar(jpegBytes: ByteArray) = AppResult.Success(UserProfile("u1", "Văn Khoai", ""))
    override suspend fun signOut() {}
}

private class FakeDashboardRepository : DashboardRepository {
    override fun observeCurrentMonthSummary(): Flow<DashboardSummary> = flowOf(DashboardSummary())
}

private class FakeHomeWalletRepository(private val list: List<Wallet>) : WalletRepository {
    override fun observeWallets(): Flow<List<Wallet>> = flowOf(list)
    override suspend fun upsertWallet(wallet: Wallet): AppResult<String> = AppResult.Success(wallet.id)
    override suspend fun deleteWallet(wallet: Wallet): AppResult<Unit> = AppResult.Success(Unit)
}

private class FakeHomeTransactionRepository : TransactionRepository {
    override fun observeRecent(limit: Int): Flow<List<FinanceTransaction>> = flowOf(emptyList())
    override fun observeMonth(month: YearMonth): Flow<List<FinanceTransaction>> = flowOf(emptyList())
    override fun observePeriod(start: Instant, endExclusive: Instant): Flow<List<FinanceTransaction>> = flowOf(emptyList())
    override suspend fun executeSalaryRolloverAtomic(cycleKey: String, sourceWalletId: String, destinationWalletId: String, amount: Long, note: String, date: Instant): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun addWithBalanceUpdate(transaction: FinanceTransaction) = AppResult.Success("tx-1")
    override suspend fun editWithBalanceUpdate(original: FinanceTransaction, updated: FinanceTransaction) = AppResult.Success(Unit)
    override suspend fun deleteWithBalanceUpdate(transaction: FinanceTransaction) = AppResult.Success(Unit)
    override suspend fun transferBetweenWallets(sourceWalletId: String, destinationWalletId: String, amount: Long, note: String, date: Instant) = AppResult.Success(Unit)
}

private class FakeHomeCategoryRepository : CategoryRepository {
    override fun observeCategories(): Flow<List<Category>> = flowOf(emptyList())
    override suspend fun upsertCategory(category: Category) = AppResult.Success(category.id)
    override suspend fun deleteCategory(category: Category) = AppResult.Success(Unit)
}

private class FakeHomeBudgetRepository : BudgetRepository {
    override fun observeBudgets(periodKey: String): Flow<List<Budget>> = flowOf(emptyList())
    override suspend fun upsertBudget(budget: Budget) = AppResult.Success(budget.id)
    override suspend fun deleteBudget(budget: Budget) = AppResult.Success(Unit)
}

private class FakeHomeNotificationRepository : NotificationRepository {
    override fun observeNotifications(): Flow<List<AppNotification>> = flowOf(emptyList())
    override suspend fun saveNotification(notification: AppNotification): AppResult<String> = AppResult.Success("n-1")
    override suspend fun markAsRead(id: String): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun markAllAsRead(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun markAsPaid(id: String): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun markAsPaidWithAmount(id: String, amount: Money, newBody: String?): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun markAsPaidByReminderId(reminderId: String): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun deleteNotification(id: String): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun clearAll(): AppResult<Unit> = AppResult.Success(Unit)
}

private class FakeHomeDebtRepository(private val list: List<DebtAccount>) : DebtRepository {
    override fun observeDebts(): Flow<List<DebtAccount>> = flowOf(list)
    override fun observePaymentHistory(debtId: String): Flow<List<DebtPaymentHistory>> = flowOf(emptyList())
    override fun observeAllPaymentHistory(): Flow<List<DebtPaymentHistory>> = flowOf(emptyList())
    override suspend fun upsertDebt(debt: DebtAccount): AppResult<String> = AppResult.Success(debt.id)
    override suspend fun deleteDebt(debt: DebtAccount): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun processPayment(debtId: String, walletId: String, amount: Long, principalPaid: Long, interestPaid: Long, note: String, paymentDate: Instant): AppResult<Unit> = AppResult.Success(Unit)
}

private class FakeHomeGoalRepository(private val list: List<FinancialGoal>) : GoalRepository {
    override fun observeGoals(): Flow<List<FinancialGoal>> = flowOf(list)
    override suspend fun upsertGoal(goal: FinancialGoal): AppResult<String> = AppResult.Success(goal.id)
    override suspend fun deleteGoal(goal: FinancialGoal): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun depositToGoal(goalId: String, walletId: String, amount: Long, note: String, date: Instant): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun withdrawFromGoal(goalId: String, walletId: String, amount: Long, note: String, date: Instant): AppResult<Unit> = AppResult.Success(Unit)
}

private class FakeHomeSalaryCycleRepository : com.finlux.app.domain.repository.SalaryCycleRepository {
    override fun observeConfig(): Flow<com.finlux.app.domain.model.SalaryCycleConfig> = flowOf(com.finlux.app.domain.model.SalaryCycleConfig())
    override suspend fun saveConfig(config: com.finlux.app.domain.model.SalaryCycleConfig): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun isRolloverProcessed(cycleKey: String): Boolean = false
    override suspend fun markRolloverProcessed(cycleKey: String): AppResult<Unit> = AppResult.Success(Unit)
}

private class FakeUiPreferencesRepository : com.finlux.app.domain.repository.UiPreferencesRepository {
    private val state = kotlinx.coroutines.flow.MutableStateFlow(com.finlux.app.domain.model.UiPreferences())
    override val preferences: Flow<com.finlux.app.domain.model.UiPreferences> = state
    override suspend fun setPreferences(preferences: com.finlux.app.domain.model.UiPreferences) {
        state.value = preferences
    }
}
