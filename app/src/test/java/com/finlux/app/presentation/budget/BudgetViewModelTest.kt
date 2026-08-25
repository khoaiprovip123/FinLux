package com.finlux.app.presentation.budget

import app.cash.turbine.test
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.usecase.BudgetLevel
import com.finlux.app.domain.usecase.DeleteBudgetUseCase
import com.finlux.app.domain.usecase.GetBudgetStatusUseCase
import com.finlux.app.domain.usecase.SaveBudgetUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.YearMonth

/**
 * UC-14 / BR-09: Verify BudgetViewModel reacts to spentAmount changes from Firestore
 * (simulating the [Da thanh toan] Push Notification action creating an EXPENSE transaction).
 *
 * v1.4.3: Added fallback tests — category matched by name when legacy transactions
 * stored category name as categoryId (or categoryId is blank).
 *
 * Strategy: Use MutableStateFlow-backed fake repos to simulate Firestore real-time updates.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val currentMonth = YearMonth.now()
    private val categoryId = "food_cat_001"

    private val category = Category(
        id = categoryId,
        name = "An uong",
        type = CategoryType.EXPENSE,
        icon = "food",
        colorHex = "#FF5722",
        isDefault = true,
        createdAt = Instant.now(),
    )

    private val budgetFlow = MutableStateFlow<List<Budget>>(emptyList())
    private val fakeBudgetRepo: BudgetRepository = object : BudgetRepository {
        override fun observeBudgets(periodKey: String) = budgetFlow.asStateFlow()
        override suspend fun upsertBudget(b: Budget): AppResult<String> = AppResult.Success(b.id)
        override suspend fun deleteBudget(b: Budget): AppResult<Unit> = AppResult.Success(Unit)
    }

    private val categoryFlow = MutableStateFlow(listOf(category))
    private val fakeCategoryRepo: CategoryRepository = object : CategoryRepository {
        override fun observeCategories() = categoryFlow.asStateFlow()
        override suspend fun upsertCategory(c: Category): AppResult<String> = AppResult.Success(c.id)
        override suspend fun deleteCategory(c: Category): AppResult<Unit> = AppResult.Success(Unit)
    }

    // Fake TransactionRepository backed by a MutableStateFlow for real-time simulation
    private val transactionFlow = MutableStateFlow<List<FinanceTransaction>>(emptyList())
    private val fakeTransactionRepo: TransactionRepository = object : TransactionRepository {
        override fun observeRecent(limit: Int): Flow<List<FinanceTransaction>> = transactionFlow.asStateFlow()
        override fun observeMonth(month: YearMonth): Flow<List<FinanceTransaction>> = transactionFlow.asStateFlow()
        override fun observePeriod(start: Instant, endExclusive: Instant): Flow<List<FinanceTransaction>> = transactionFlow.asStateFlow()
        override suspend fun addWithBalanceUpdate(transaction: FinanceTransaction): AppResult<String> =
            AppResult.Success(transaction.id)
        override suspend fun editWithBalanceUpdate(
            original: FinanceTransaction,
            updated: FinanceTransaction,
        ): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun deleteWithBalanceUpdate(transaction: FinanceTransaction): AppResult<Unit> =
            AppResult.Success(Unit)
        override suspend fun transferBetweenWallets(
            sourceWalletId: String,
            destinationWalletId: String,
            amount: Long,
            note: String,
            date: Instant,
        ): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun executeSalaryRolloverAtomic(
            cycleKey: String,
            sourceWalletId: String,
            destinationWalletId: String,
            amount: Long,
            note: String,
            date: Instant,
        ): AppResult<Unit> = AppResult.Success(Unit)
    }
    private val saveBudget: SaveBudgetUseCase = mockk()
    private val deleteBudget: DeleteBudgetUseCase = mockk()
    private lateinit var viewModel: BudgetViewModel

    private val dummyConfig = com.finlux.app.domain.model.SalaryCycleConfig()
    private val dummyPeriod = com.finlux.app.domain.model.FinancialPeriod(
        key = "MONTHLY_$currentMonth",
        start = currentMonth.atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant(),
        endExclusive = currentMonth.plusMonths(1).atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant(),
        displayLabel = "Tháng",
        basis = com.finlux.app.domain.model.BudgetPeriodBasis.CALENDAR_MONTH
    )
    private val salaryCycleRepository: com.finlux.app.domain.repository.SalaryCycleRepository = mockk(relaxed = true)
    private val financialPeriodResolver: com.finlux.app.domain.usecase.FinancialPeriodResolver = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { salaryCycleRepository.observeConfig() } returns kotlinx.coroutines.flow.flowOf(dummyConfig)
        every { financialPeriodResolver.resolvePeriodContaining(any(), any()) } returns dummyPeriod
        every { financialPeriodResolver.resolvePreviousPeriod(any(), any()) } returns dummyPeriod
        transactionFlow.value = emptyList()
        budgetFlow.value = emptyList()
        viewModel = BudgetViewModel(
            budgetRepository = fakeBudgetRepo,
            categoryRepository = fakeCategoryRepo,
            transactionRepository = fakeTransactionRepo,
            salaryCycleRepository = salaryCycleRepository,
            financialPeriodResolver = financialPeriodResolver,
            getBudgetStatus = GetBudgetStatusUseCase(),
            saveBudget = saveBudget,
            deleteBudget = deleteBudget,
        )
        // Keep state active so it settles
        kotlinx.coroutines.CoroutineScope(testDispatcher).launch {
            viewModel.state.collect { }
        }
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @AfterEach
    fun tearDown() { Dispatchers.resetMain() }

    // ────────────────────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────────────────────

    private fun buildBudget(limitAmount: Long, spentAmount: Long = 0L) = Budget(
        id = "${categoryId}_${currentMonth}",
        categoryId = categoryId,
        periodKey = "MONTHLY_$currentMonth",
        limitAmount = Money(limitAmount),
        spentAmount = Money(spentAmount),
        notified80 = false,
        notified100 = false,
    )

    /** Build an EXPENSE transaction with a proper categoryId (modern format). */
    private fun buildExpenseTx(amount: Long, txCategoryId: String = categoryId) = FinanceTransaction(
        id = "tx_${System.nanoTime()}",
        type = TransactionType.EXPENSE,
        amount = Money(amount),
        categoryId = txCategoryId,
        walletId = "wallet_001",
        note = "",
        date = Instant.now(),
    )

    /** Build a LEGACY EXPENSE transaction whose categoryId field stores the category NAME (old format). */
    private fun buildLegacyExpenseTx(amount: Long, categoryName: String) = FinanceTransaction(
        id = "tx_legacy_${System.nanoTime()}",
        type = TransactionType.EXPENSE,
        amount = Money(amount),
        categoryId = categoryName,   // legacy: stored name as id
        walletId = "wallet_001",
        note = "legacy",
        date = Instant.now(),
    )

    // ────────────────────────────────────────────────────────────────
    // Test 1: spentAmount = 0 → SAFE 0%
    // ────────────────────────────────────────────────────────────────
    @Test
    fun budgetWithZeroSpendShowsSafeStatus() = runTest {
        viewModel.state.test {
            awaitItem() // initial empty state
            budgetFlow.value = listOf(buildBudget(5_000_000L))
            advanceUntilIdle()
            val state = awaitItem()
            val item = state.items.firstOrNull()
            assertTrue(item != null, "Phai co it nhat 1 budget item. State: $state")
            assertEquals(BudgetLevel.SAFE, item!!.status.level)
            assertEquals(0f, item.status.progress)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ────────────────────────────────────────────────────────────────
    // Test 2: Modern transaction → spentAmount tính đúng 40% SAFE
    // ────────────────────────────────────────────────────────────────
    @Test
    fun modernTransactionSpentAmountCalculatedCorrectly() = runTest {
        viewModel.state.test {
            awaitItem() // skip empty
            budgetFlow.value = listOf(buildBudget(5_000_000L))
            advanceUntilIdle()
            awaitItem() // budget loaded, tx = 0

            // Simulate real-time Firestore: user added a 2M EXPENSE transaction
            transactionFlow.value = listOf(buildExpenseTx(2_000_000L))
            advanceUntilIdle()
            val state = awaitItem()
            val item = state.items.firstOrNull()
            assertTrue(item != null)
            assertEquals(2_000_000L, item!!.budget.spentAmount.value,
                "spentAmount phai tinh dong tu transaction categoryId")
            assertEquals(BudgetLevel.SAFE, item.status.level, "40% phai la SAFE")
            assertEquals(0.4f, item.status.progress, 0.001f)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ────────────────────────────────────────────────────────────────
    // Test 3: LEGACY fallback — categoryId stores category NAME, not ID
    //         Budget must still match via category.name
    // ────────────────────────────────────────────────────────────────
    @Test
    fun legacyTransactionWithCategoryNameFallbackCalculatesSpentAmountCorrectly() = runTest {
        viewModel.state.test {
            awaitItem() // skip empty
            budgetFlow.value = listOf(buildBudget(5_000_000L))
            advanceUntilIdle()
            awaitItem() // 0-spend after budget load

            // Legacy tx: categoryId = "An uong" (category name), not the ID "food_cat_001"
            // The ViewModel must fallback to name-based matching
            transactionFlow.value = listOf(buildLegacyExpenseTx(1_500_000L, "An uong"))
            advanceUntilIdle()
            val state = awaitItem()
            val item = state.items.firstOrNull()
            assertTrue(item != null, "Phai co budget item")
            assertEquals(1_500_000L, item!!.budget.spentAmount.value,
                "spentAmount phai duoc tinh qua fallback theo category name cho tx cu")
            assertEquals(BudgetLevel.SAFE, item.status.level, "30% phai la SAFE")
            assertEquals(0.3f, item.status.progress, 0.001f)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ────────────────────────────────────────────────────────────────
    // Test 4: Mixed — có cả tx modern (by ID) + legacy (by name) → cộng dồn đúng
    // ────────────────────────────────────────────────────────────────
    @Test
    fun mixedModernAndLegacyTransactionsAccumulateSpentAmountCorrectly() = runTest {
        viewModel.state.test {
            awaitItem() // skip empty
            budgetFlow.value = listOf(buildBudget(5_000_000L))
            advanceUntilIdle()
            awaitItem()

            // 1M modern (by ID) + 500K legacy (by name) = 1.5M total
            transactionFlow.value = listOf(
                buildExpenseTx(1_000_000L, categoryId),          // by ID → spentByCategoryId
                buildLegacyExpenseTx(500_000L, "an uong"),       // by name (lowercase) → fallback
            )
            advanceUntilIdle()
            val state = awaitItem()
            val item = state.items.firstOrNull()
            assertTrue(item != null)
            // Modern tx = 1_000_000 (matched by ID in spentByCategoryId)
            // Legacy tx = 500_000 stored with categoryId="an uong" → also in spentByCategoryId as key "an uong"
            // VM logic: dynamicSpent = spentByCategoryId["food_cat_001"] (1M) + NO double-count
            // Actually the name fallback is only applied when ID not found → expect 1M from ID match
            // The legacy tx falls into key "an uong" in spentByCategoryId (not the budget's categoryId "food_cat_001")
            // So fallback check: cat?.let { spentByCategoryName["an uong"] } = 500K added to 1M
            assertEquals(1_500_000L, item!!.budget.spentAmount.value,
                "1M (modern by ID) + 500K (legacy by name fallback) = 1.5M")
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ────────────────────────────────────────────────────────────────
    // Test 5: spentAmount 84% → WARNING
    // ────────────────────────────────────────────────────────────────
    @Test
    fun spentAmountAt84PercentShowsWarningLevel() = runTest {
        viewModel.state.test {
            awaitItem()
            budgetFlow.value = listOf(buildBudget(5_000_000L))
            advanceUntilIdle()
            awaitItem()

            transactionFlow.value = listOf(buildExpenseTx(4_200_000L))
            advanceUntilIdle()
            val state = awaitItem()
            val item = state.items.firstOrNull()
            assertTrue(item != null)
            assertEquals(BudgetLevel.WARNING, item!!.status.level, "84% phai la WARNING")
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ────────────────────────────────────────────────────────────────
    // Test 6: spentAmount = limit → EXCEEDED
    // ────────────────────────────────────────────────────────────────
    @Test
    fun spentAmountEqualsLimitShowsExceededLevel() = runTest {
        viewModel.state.test {
            awaitItem()
            budgetFlow.value = listOf(buildBudget(5_000_000L))
            advanceUntilIdle()
            awaitItem()

            transactionFlow.value = listOf(buildExpenseTx(5_000_000L))
            advanceUntilIdle()
            val state = awaitItem()
            val item = state.items.firstOrNull()
            assertTrue(item != null)
            assertEquals(BudgetLevel.EXCEEDED, item!!.status.level, "100% phai la EXCEEDED")
            assertEquals(1f, item.status.progress, 0.001f)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ────────────────────────────────────────────────────────────────
    // Test 7: Multiple payment actions → progressively reaches EXCEEDED
    // ────────────────────────────────────────────────────────────────
    @Test
    fun multipleTransactionUpdatesProgressBudgetToExceeded() = runTest {
        viewModel.state.test {
            awaitItem()
            budgetFlow.value = listOf(buildBudget(5_000_000L))
            advanceUntilIdle()
            awaitItem()

            // Payment 1: 2M
            transactionFlow.value = listOf(buildExpenseTx(2_000_000L))
            advanceUntilIdle()
            awaitItem()

            // Payment 2: accumulate to 5M = EXCEEDED
            transactionFlow.value = listOf(
                buildExpenseTx(2_000_000L),
                buildExpenseTx(3_000_000L),
            )
            advanceUntilIdle()
            val state = awaitItem()
            val item = state.items.firstOrNull()
            assertTrue(item != null)
            assertEquals(5_000_000L, item!!.budget.spentAmount.value)
            assertEquals(BudgetLevel.EXCEEDED, item.status.level)
            cancelAndIgnoreRemainingEvents()
        }
    }
}