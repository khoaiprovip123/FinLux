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
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.UserProfile
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.AuthRepository
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.DashboardRepository
import com.finlux.app.domain.repository.DebtRepository
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

        val viewModel = HomeViewModel(
            authRepository = FakeAuthRepository(),
            dashboardRepository = FakeDashboardRepository(),
            walletRepository = FakeHomeWalletRepository(wallets),
            transactionRepository = FakeHomeTransactionRepository(),
            categoryRepository = FakeHomeCategoryRepository(),
            budgetRepository = FakeHomeBudgetRepository(),
            notificationRepository = FakeHomeNotificationRepository(),
            debtRepository = FakeHomeDebtRepository(debts),
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
    override fun observeBudgets(month: YearMonth): Flow<List<Budget>> = flowOf(emptyList())
    override suspend fun upsertBudget(budget: Budget) = AppResult.Success(budget.id)
    override suspend fun deleteBudget(budget: Budget) = AppResult.Success(Unit)
}

private class FakeHomeNotificationRepository : NotificationRepository {
    override fun observeNotifications(): Flow<List<AppNotification>> = flowOf(emptyList())
    override suspend fun saveNotification(notification: AppNotification): AppResult<String> = AppResult.Success("n-1")
    override suspend fun markAsRead(id: String): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun markAsPaid(id: String): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun markAsPaidWithAmount(id: String, amount: Money, newBody: String?): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun markAsPaidByReminderId(reminderId: String): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun clearAll(): AppResult<Unit> = AppResult.Success(Unit)
}

private class FakeHomeDebtRepository(private val list: List<DebtAccount>) : DebtRepository {
    override fun observeDebts(): Flow<List<DebtAccount>> = flowOf(list)
    override fun observePaymentHistory(debtId: String): Flow<List<DebtPaymentHistory>> = flowOf(emptyList())
    override suspend fun upsertDebt(debt: DebtAccount): AppResult<String> = AppResult.Success(debt.id)
    override suspend fun deleteDebt(debt: DebtAccount): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun processPayment(debtId: String, walletId: String, amount: Long, principalPaid: Long, interestPaid: Long, note: String, paymentDate: Instant): AppResult<Unit> = AppResult.Success(Unit)
}
