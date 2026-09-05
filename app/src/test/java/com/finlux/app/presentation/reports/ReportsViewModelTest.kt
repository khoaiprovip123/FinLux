package com.finlux.app.presentation.reports

import app.cash.turbine.test
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.DealCategory
import com.finlux.app.domain.model.DealFlowType
import com.finlux.app.domain.model.DealStatus
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.FinancialDeal
import com.finlux.app.domain.model.FinancialGoal
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.SavingDestination
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.SavingSpinStatus
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.DealRepository
import com.finlux.app.domain.repository.DebtRepository
import com.finlux.app.domain.repository.GoalRepository
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.SavingSpinRepository
import com.finlux.app.domain.repository.TransactionRangeRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.DefaultFinancialPeriodResolver
import com.finlux.app.domain.usecase.DefaultSalaryCycleCalculator
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private val transactionRangeRepository: TransactionRangeRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()
    private val walletRepository: WalletRepository = mockk()
    private val salaryCycleRepository: SalaryCycleRepository = mockk()
    private val debtRepository: DebtRepository = mockk()
    private val goalRepository: GoalRepository = mockk()
    private val budgetRepository: BudgetRepository = mockk()
    private val dealRepository: DealRepository = mockk()
    private val savingSpinRepository: SavingSpinRepository = mockk()

    private val salaryCalculator = DefaultSalaryCycleCalculator()
    private val financialPeriodResolver = DefaultFinancialPeriodResolver(salaryCalculator)
    private val windowResolver = ReportQueryWindowResolver(salaryCalculator)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { transactionRangeRepository.observeRange(any(), any()) } returns flowOf(emptyList())
        every { categoryRepository.observeCategories() } returns flowOf(emptyList())
        every { walletRepository.observeWallets() } returns flowOf(emptyList())
        every { salaryCycleRepository.observeConfig() } returns flowOf(SalaryCycleConfig())
        every { debtRepository.observeDebts() } returns flowOf(emptyList())
        every { debtRepository.observeAllPaymentHistory() } returns flowOf(emptyList())
        every { goalRepository.observeGoals() } returns flowOf(emptyList())
        every { budgetRepository.observeBudgets(any()) } returns flowOf(emptyList())
        every { dealRepository.observeDeals() } returns flowOf(emptyList())
        every { savingSpinRepository.observeSessions(any(), any()) } returns flowOf(emptyList())
        every { savingSpinRepository.observeDestinations() } returns flowOf(emptyList())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ReportsViewModel {
        return ReportsViewModel(
            transactionRangeRepository = transactionRangeRepository,
            categoryRepository = categoryRepository,
            walletRepository = walletRepository,
            salaryCycleRepository = salaryCycleRepository,
            debtRepository = debtRepository,
            goalRepository = goalRepository,
            budgetRepository = budgetRepository,
            dealRepository = dealRepository,
            savingSpinRepository = savingSpinRepository,
            financialPeriodResolver = financialPeriodResolver,
            windowResolver = windowResolver,
            dailyStatementCalculator = com.finlux.app.domain.usecase.DailyStatementCalculator(),
        )
    }

    @Test
    fun `reports state correctly computes DealsSummaryReport and DealReportItems`() = runTest(testDispatcher) {
        val deal1 = FinancialDeal(
            id = "deal-1",
            title = "Dự án Bất Động Sản",
            category = DealCategory.INVESTMENT,
            totalCapitalOutlay = Money(100_000_000L),
            totalRecovered = Money(50_000_000L),
            netProfitLoss = Money(20_000_000L),
            status = DealStatus.ACTIVE,
        )

        val deal2 = FinancialDeal(
            id = "deal-2",
            title = "Cho vay kinh doanh",
            category = DealCategory.LENDING,
            totalCapitalOutlay = Money(30_000_000L),
            totalRecovered = Money(30_000_000L),
            netProfitLoss = Money(3_000_000L),
            status = DealStatus.COMPLETED,
        )

        every { dealRepository.observeDeals() } returns flowOf(listOf(deal1, deal2))

        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // skip initial empty state
            advanceUntilIdle()
            val state = awaitItem()
            assertEquals(2, state.dealReportItems.size)
            assertEquals(50_000_000L, state.dealsSummary.totalActiveCapitalOutlay)
            assertEquals(130_000_000L, state.dealsSummary.totalHistoricalCapitalOutlay)
            assertEquals(80_000_000L, state.dealsSummary.totalRecovered)
            assertEquals(23_000_000L, state.dealsSummary.totalNetProfit)
            assertEquals(100_000_000L, state.dealsSummary.totalInvestmentOutlay)
            assertEquals(30_000_000L, state.dealsSummary.totalLendingOutlay)
            assertEquals(0L, state.dealsSummary.totalLendingOutstanding)
            assertEquals(1, state.dealsSummary.activeDealsCount)
            assertEquals(1, state.dealsSummary.completedDealsCount)
            assertTrue(state.dealsSummary.overallRoi > 17.6 && state.dealsSummary.overallRoi < 17.7)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `reports state correctly computes true net worth with active deals and debts`() = runTest(testDispatcher) {
        val wallet1 = Wallet(
            id = "w1",
            name = "Tài khoản VCB",
            type = WalletType.BANK,
            balance = Money(200_000_000L),
            colorHex = "#0EA5E9",
            isDefault = true,
            createdAt = Instant.now(),
        )

        val debt1 = DebtAccount(
            id = "d1",
            name = "Vay mua xe",
            type = com.finlux.app.domain.model.DebtType.BANK_LOAN,
            totalAmount = Money(100_000_000L),
            remainingBalance = Money(40_000_000L),
            interestRateApr = 8.5,
            minimumPayment = Money(3_000_000L),
            isSettled = false,
        )

        val activeDeal = FinancialDeal(
            id = "deal-1",
            title = "Cổ phần startup",
            category = DealCategory.INVESTMENT,
            totalCapitalOutlay = Money(50_000_000L),
            totalRecovered = Money(0L),
            status = DealStatus.ACTIVE,
        )
        val goal = FinancialGoal(
            id = "goal-1",
            name = "Quỹ dự phòng",
            targetAmount = Money(30_000_000L),
            savedAmount = Money(15_000_000L),
            deadline = Instant.now().plusSeconds(86_400),
            category = "savings",
            monthlyContribution = Money(2_000_000L),
        )

        every { walletRepository.observeWallets() } returns flowOf(listOf(wallet1))
        every { debtRepository.observeDebts() } returns flowOf(listOf(debt1))
        every { dealRepository.observeDeals() } returns flowOf(listOf(activeDeal))
        every { goalRepository.observeGoals() } returns flowOf(listOf(goal))

        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // skip initial empty state
            advanceUntilIdle()
            val state = awaitItem()
            assertEquals(200_000_000L, state.totalAssets)
            assertEquals(40_000_000L, state.totalDebtRemaining)
            // totalNetWorth = 200M - 40M = 160M
            assertEquals(160_000_000L, state.totalNetWorth)
            // trueNetWorth = 200M wallets + 15M goals + 50M active deal capital - 40M debt = 225M
            assertEquals(225_000_000L, state.trueNetWorth)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `reports state correctly computes SavingSpinSummaryReport`() = runTest(testDispatcher) {
        val session1 = SavingSpinSession(
            id = "sess-1",
            scheduleKey = "2026-08-30",
            wheelValues = listOf(Money(50_000L)),
            status = SavingSpinStatus.COMPLETED,
            selectedAmount = Money(50_000L),
            destinationId = "piggy-bank",
            createdAt = Instant.now(),
        )

        val session2 = SavingSpinSession(
            id = "sess-2",
            scheduleKey = "2026-08-31",
            wheelValues = listOf(Money(100_000L)),
            status = SavingSpinStatus.COMPLETED,
            selectedAmount = Money(100_000L),
            destinationId = "piggy-bank",
            createdAt = Instant.now(),
        )

        val destination = SavingDestination(
            id = "piggy-bank",
            name = "Heo đất",
            method = com.finlux.app.domain.model.SavingMethod.CASH,
        )

        every { savingSpinRepository.observeSessions(any(), any()) } returns flowOf(listOf(session1, session2))
        every { savingSpinRepository.observeDestinations() } returns flowOf(listOf(destination))

        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // skip initial empty state
            advanceUntilIdle()
            val state = awaitItem()
            assertEquals(150_000L, state.savingSpinSummary.totalSaved)
            assertEquals(2, state.savingSpinSummary.completedCount)
            assertEquals(100, state.savingSpinSummary.completionRate)
            assertEquals(1, state.savingSpinSummary.destinationBreakdown.size)
            assertEquals("Heo đất", state.savingSpinSummary.destinationBreakdown.first().destinationName)
            assertEquals(150_000L, state.savingSpinSummary.destinationBreakdown.first().amount.value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `reports state correctly computes Budgets with dynamic spent from expense transactions`() = runTest(testDispatcher) {
        val catFood = Category(
            id = "cat_food",
            name = "Ăn uống",
            type = CategoryType.EXPENSE,
            icon = "food",
            colorHex = "#FF5722",
            isDefault = true,
            createdAt = Instant.now(),
        )
        val catShopping = Category(
            id = "cat_shopping",
            name = "Mua sắm",
            type = CategoryType.EXPENSE,
            icon = "shopping",
            colorHex = "#9C27B0",
            isDefault = true,
            createdAt = Instant.now(),
        )

        val budgetFood = Budget(
            id = "b1",
            categoryId = "cat_food",
            periodKey = "month:2026-08",
            limitAmount = Money(5_000_000L),
            spentAmount = Money(0L), // Stale repository value, will be dynamic
        )
        val budgetShopping = Budget(
            id = "b2",
            categoryId = "Mua sắm", // Fallback by name
            periodKey = "month:2026-08",
            limitAmount = Money(2_000_000L),
            spentAmount = Money(0L),
        )

        val tx1 = FinanceTransaction(
            id = "tx1",
            type = TransactionType.EXPENSE,
            amount = Money(3_000_000L),
            categoryId = "cat_food",
            walletId = "w1",
            date = Instant.now(),
        )
        val tx2 = FinanceTransaction(
            id = "tx2",
            type = TransactionType.EXPENSE,
            amount = Money(1_000_000L),
            categoryId = "Ăn uống", // Saved by name
            walletId = "w1",
            date = Instant.now(),
        )
        val tx3 = FinanceTransaction(
            id = "tx3",
            type = TransactionType.EXPENSE,
            amount = Money(2_500_000L),
            categoryId = "cat_shopping",
            walletId = "w1",
            date = Instant.now(),
        )
        val txCapitalOutlay = FinanceTransaction(
            id = "tx4",
            type = TransactionType.EXPENSE,
            amount = Money(10_000_000L),
            categoryId = "cat_food",
            walletId = "w1",
            dealFlowType = DealFlowType.OUTLAY_CAPITAL, // Capital investment outlay, must be excluded from expense
            date = Instant.now(),
        )

        every { categoryRepository.observeCategories() } returns flowOf(listOf(catFood, catShopping))
        every { budgetRepository.observeBudgets(any()) } returns flowOf(listOf(budgetFood, budgetShopping))
        every { transactionRangeRepository.observeRange(any(), any()) } returns flowOf(listOf(tx1, tx2, tx3, txCapitalOutlay))

        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // initial
            advanceUntilIdle()
            val state = awaitItem()
            assertEquals(2, state.budgetReportItems.size)
            assertEquals(7_000_000L, state.totalBudgetLimit)
            assertEquals(6_500_000L, state.totalBudgetSpent) // 4M food + 2.5M shopping
            assertEquals(500_000L, state.totalBudgetRemaining)
            assertEquals(93, state.budgetUsagePercent) // 6.5M / 7M = 92.85% -> 93%
            assertEquals(1, state.overBudgetCount) // shopping is over budget (2.5M > 2M)

            val foodItem = state.budgetReportItems.find { it.category?.name == "Ăn uống" }
            assertTrue(foodItem != null)
            assertEquals(4_000_000L, foodItem!!.spent)
            assertEquals(5_000_000L, foodItem.limit)
            assertEquals(1_000_000L, foodItem.remaining)
            assertEquals(false, foodItem.isOverBudget)

            val shoppingItem = state.budgetReportItems.find { it.category?.name == "Mua sắm" }
            assertTrue(shoppingItem != null)
            assertEquals(2_500_000L, shoppingItem!!.spent)
            assertEquals(2_000_000L, shoppingItem.limit)
            assertEquals(0L, shoppingItem.remaining)
            assertEquals(true, shoppingItem.isOverBudget)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `walletSpendingDetails calculates per-wallet expense share and category breakdown accurately`() = runTest {
        val now = Instant.now()
        val w1 = Wallet(id = "w1", name = "Ví Tiền mặt", type = WalletType.CASH, balance = Money(5_000_000L), colorHex = "#3B82F6", isDefault = true, createdAt = now)
        val w2 = Wallet(id = "w2", name = "Tài khoản VCB", type = WalletType.BANK, balance = Money(20_000_000L), colorHex = "#10B981", isDefault = false, createdAt = now)

        val catFood = Category(id = "cat_food", name = "Ăn uống", type = CategoryType.EXPENSE, icon = "fastfood", colorHex = "#EF4444", isDefault = true, createdAt = now)
        val catShopping = Category(id = "cat_shopping", name = "Mua sắm", type = CategoryType.EXPENSE, icon = "shopping_cart", colorHex = "#8B5CF6", isDefault = true, createdAt = now)
        val catSalary = Category(id = "cat_salary", name = "Lương", type = CategoryType.INCOME, icon = "payments", colorHex = "#10B981", isDefault = true, createdAt = now)

        val tx1 = FinanceTransaction(
            id = "tx1",
            type = TransactionType.EXPENSE,
            amount = Money(3_000_000L),
            categoryId = "cat_food",
            walletId = "w1",
            date = now,
        )
        val tx2 = FinanceTransaction(
            id = "tx2",
            type = TransactionType.EXPENSE,
            amount = Money(7_000_000L),
            categoryId = "cat_shopping",
            walletId = "w2",
            date = now,
        )
        val tx3 = FinanceTransaction(
            id = "tx3",
            type = TransactionType.INCOME,
            amount = Money(25_000_000L),
            categoryId = "cat_salary",
            walletId = "w2",
            date = now,
        )

        every { walletRepository.observeWallets() } returns flowOf(listOf(w1, w2))
        every { categoryRepository.observeCategories() } returns flowOf(listOf(catFood, catShopping, catSalary))
        every { transactionRangeRepository.observeRange(any(), any()) } returns flowOf(listOf(tx1, tx2, tx3))

        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // initial
            advanceUntilIdle()
            val state = awaitItem()

            assertEquals(2, state.walletSpendingDetails.size)
            val w1Detail = state.walletSpendingDetails.find { it.wallet.id == "w1" }
            val w2Detail = state.walletSpendingDetails.find { it.wallet.id == "w2" }

            assertTrue(w1Detail != null)
            assertEquals(3_000_000L, w1Detail!!.expenseInPeriod)
            assertEquals(0L, w1Detail.incomeInPeriod)
            assertEquals(-3_000_000L, w1Detail.netCashflowInPeriod)
            assertEquals(0.3f, w1Detail.expenseShareOfTotal, 0.01f) // 3M / 10M = 30%
            assertEquals(1, w1Detail.expensesByCategory.size)
            assertEquals("Ăn uống", w1Detail.expensesByCategory[0].category?.name)
            assertEquals(3_000_000L, w1Detail.expensesByCategory[0].amount)

            assertTrue(w2Detail != null)
            assertEquals(7_000_000L, w2Detail!!.expenseInPeriod)
            assertEquals(25_000_000L, w2Detail.incomeInPeriod)
            assertEquals(18_000_000L, w2Detail.netCashflowInPeriod)
            assertEquals(0.7f, w2Detail.expenseShareOfTotal, 0.01f) // 7M / 10M = 70%
            assertEquals(1, w2Detail.expensesByCategory.size)
            assertEquals("Mua sắm", w2Detail.expensesByCategory[0].category?.name)
            assertEquals(7_000_000L, w2Detail.expensesByCategory[0].amount)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectWallet filters report summary, expensesByCategory, and daily statements to selected wallet`() = runTest {
        val now = Instant.now()
        val w1 = Wallet(id = "w1", name = "Ví Tiền mặt", type = WalletType.CASH, balance = Money(5_000_000L), colorHex = "#3B82F6", isDefault = true, createdAt = now)
        val w2 = Wallet(id = "w2", name = "Tài khoản VCB", type = WalletType.BANK, balance = Money(20_000_000L), colorHex = "#10B981", isDefault = false, createdAt = now)

        val catFood = Category(id = "cat_food", name = "Ăn uống", type = CategoryType.EXPENSE, icon = "food", colorHex = "#EF4444", isDefault = true, createdAt = now)
        val catShopping = Category(id = "cat_shopping", name = "Mua sắm", type = CategoryType.EXPENSE, icon = "shopping", colorHex = "#8B5CF6", isDefault = true, createdAt = now)

        val tx1 = FinanceTransaction(
            id = "tx1",
            type = TransactionType.EXPENSE,
            amount = Money(2_000_000L),
            categoryId = "cat_food",
            walletId = "w1",
            date = now,
        )
        val tx2 = FinanceTransaction(
            id = "tx2",
            type = TransactionType.EXPENSE,
            amount = Money(8_000_000L),
            categoryId = "cat_shopping",
            walletId = "w2",
            date = now,
        )

        every { walletRepository.observeWallets() } returns flowOf(listOf(w1, w2))
        every { categoryRepository.observeCategories() } returns flowOf(listOf(catFood, catShopping))
        every { transactionRangeRepository.observeRange(any(), any()) } returns flowOf(listOf(tx1, tx2))

        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // initial
            advanceUntilIdle()
            val allState = awaitItem()
            assertEquals(10_000_000L, allState.summary.expense.value)
            assertEquals(2, allState.expensesByCategory.size)

            viewModel.selectWallet("w1")
            advanceUntilIdle()
            val w1State = awaitItem()
            assertEquals("w1", w1State.selectedWalletId)
            assertEquals("Ví Tiền mặt", w1State.selectedWallet?.name)
            assertEquals(2_000_000L, w1State.summary.expense.value)
            assertEquals(1, w1State.expensesByCategory.size)
            assertEquals("cat_food", w1State.expensesByCategory[0].category?.id)
            assertEquals(2, w1State.walletSpendingDetails.size)

            viewModel.selectWallet(null)
            advanceUntilIdle()
            val resetState = awaitItem()
            assertEquals(null, resetState.selectedWalletId)
            assertEquals(10_000_000L, resetState.summary.expense.value)
            assertEquals(2, resetState.expensesByCategory.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `reports correctly tracks inter-wallet transfers and updates net wallet change`() = runTest {
        val now = Instant.now()
        val mbBank = Wallet(id = "w_mb", name = "MB Bank", type = WalletType.BANK, balance = Money(7_000_000L), colorHex = "#1D4ED8", isDefault = true, createdAt = now)
        val cashWallet = Wallet(id = "w_cash", name = "Tiền mặt", type = WalletType.CASH, balance = Money(3_000_000L), colorHex = "#10B981", isDefault = false, createdAt = now)

        val catSalary = Category(id = "cat_salary", name = "Lương", type = CategoryType.INCOME, icon = "salary", colorHex = "#10B981", isDefault = true, createdAt = now)

        // MB Bank có thu nhập 7.000.000
        val txIncome = FinanceTransaction(
            id = "tx_inc",
            type = TransactionType.INCOME,
            amount = Money(7_000_000L),
            categoryId = "cat_salary",
            walletId = "w_mb",
            date = now,
        )
        // MB Bank phát sinh chuyển tiền thành tiền mặt 3.000.000 (TRANSFER_OUT từ MB, TRANSFER_IN vào Tiền mặt)
        val txTransferOut = FinanceTransaction(
            id = "tx_tf_out",
            type = TransactionType.TRANSFER_OUT,
            amount = Money(3_000_000L),
            categoryId = null,
            walletId = "w_mb",
            relatedWalletId = "w_cash",
            note = "Chuyển tiền mặt tiêu dùng",
            date = now,
        )
        val txTransferIn = FinanceTransaction(
            id = "tx_tf_in",
            type = TransactionType.TRANSFER_IN,
            amount = Money(3_000_000L),
            categoryId = null,
            walletId = "w_cash",
            relatedWalletId = "w_mb",
            note = "Nhận từ MB Bank",
            date = now,
        )

        every { walletRepository.observeWallets() } returns flowOf(listOf(mbBank, cashWallet))
        every { categoryRepository.observeCategories() } returns flowOf(listOf(catSalary))
        every { transactionRangeRepository.observeRange(any(), any()) } returns flowOf(listOf(txIncome, txTransferOut, txTransferIn))

        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // initial
            advanceUntilIdle()
            val state = awaitItem()

            // 1. Kiểm tra chi tiết ví MB Bank
            val mbDetail = state.walletSpendingDetails.find { it.wallet.id == "w_mb" }
            assertTrue(mbDetail != null)
            assertEquals(7_000_000L, mbDetail!!.incomeInPeriod)
            assertEquals(0L, mbDetail.expenseInPeriod)
            assertEquals(3_000_000L, mbDetail.transferOutInPeriod)
            assertEquals(0L, mbDetail.transferInInPeriod)
            assertEquals(7_000_000L, mbDetail.totalMoneyIn)
            assertEquals(3_000_000L, mbDetail.totalMoneyOut)
            // Biến động thực tế của ví MB Bank = 7M vào - 3M ra = +4M
            assertEquals(4_000_000L, mbDetail.netWalletChange)

            // 2. Kiểm tra chi tiết ví Tiền mặt
            val cashDetail = state.walletSpendingDetails.find { it.wallet.id == "w_cash" }
            assertTrue(cashDetail != null)
            assertEquals(0L, cashDetail!!.incomeInPeriod)
            assertEquals(0L, cashDetail.expenseInPeriod)
            assertEquals(3_000_000L, cashDetail.transferInInPeriod)
            assertEquals(0L, cashDetail.transferOutInPeriod)
            assertEquals(3_000_000L, cashDetail.totalMoneyIn)
            assertEquals(0L, cashDetail.totalMoneyOut)
            // Biến động thực tế của ví Tiền mặt = +3M
            assertEquals(3_000_000L, cashDetail.netWalletChange)

            // 3. Khi lọc theo ví MB Bank
            viewModel.selectWallet("w_mb")
            advanceUntilIdle()
            val mbState = awaitItem()
            assertEquals("w_mb", mbState.selectedWalletId)
            assertEquals(7_000_000L, mbState.currentDisplayBalance)
            assertEquals(3_000_000L, mbState.totalTransferOut)
            assertEquals(0L, mbState.totalTransferIn)
            assertEquals(4_000_000L, mbState.currentWalletNetChange)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
