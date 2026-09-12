package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.BudgetPeriodBasis
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.DealFlowType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.FinancialPeriod
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SystemCategories
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class ReconcileBudgetOnCycleChangeUseCaseTest {

    private val budgetRepository: BudgetRepository = mockk()
    private val transactionRepository: TransactionRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()

    private lateinit var useCase: ReconcileBudgetOnCycleChangeUseCase

    private val sourcePeriod = FinancialPeriod(
        key = "salary:2026-06-05",
        start = Instant.parse("2026-06-05T00:00:00Z"),
        endExclusive = Instant.parse("2026-07-05T00:00:00Z"),
        displayLabel = "05/06 - 04/07",
        basis = BudgetPeriodBasis.SALARY_CYCLE,
    )

    private val newPeriod = FinancialPeriod(
        key = "salary:2026-07-15",
        start = Instant.parse("2026-07-15T00:00:00Z"),
        endExclusive = Instant.parse("2026-08-15T00:00:00Z"),
        displayLabel = "15/07 - 14/08",
        basis = BudgetPeriodBasis.SALARY_CYCLE,
    )

    @BeforeEach
    fun setUp() {
        useCase = ReconcileBudgetOnCycleChangeUseCase(
            budgetRepository = budgetRepository,
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
        )
        every { categoryRepository.observeCategories() } returns flowOf(
            listOf(
                Category(
                    id = "cat_food",
                    name = "Ăn uống",
                    type = com.finlux.app.domain.model.CategoryType.EXPENSE,
                    icon = "food",
                    colorHex = "#FF0000",
                    isDefault = true,
                    createdAt = Instant.parse("2026-01-01T00:00:00Z"),
                ),
                Category(
                    id = "cat_transport",
                    name = "Di chuyển",
                    type = com.finlux.app.domain.model.CategoryType.EXPENSE,
                    icon = "car",
                    colorHex = "#0000FF",
                    isDefault = true,
                    createdAt = Instant.parse("2026-01-01T00:00:00Z"),
                ),
            )
        )
    }

    @Test
    fun `inherits category limits and accurately reconciles spent living expenses`() = runTest {
        // Source period has budgets
        val sourceBudgets = listOf(
            Budget(
                id = "cat_food_${sourcePeriod.key}",
                categoryId = "cat_food",
                periodKey = sourcePeriod.key,
                limitAmount = Money(5_000_000L),
                spentAmount = Money(4_500_000L),
            ),
            Budget(
                id = "cat_transport_${sourcePeriod.key}",
                categoryId = "cat_transport",
                periodKey = sourcePeriod.key,
                limitAmount = Money(2_000_000L),
                spentAmount = Money(1_000_000L),
            ),
        )

        every { budgetRepository.observeBudgets(sourcePeriod.key) } returns flowOf(sourceBudgets)
        every { budgetRepository.observeBudgets(newPeriod.key) } returns flowOf(emptyList())

        // Actual transactions in newPeriod
        val transactions = listOf(
            // Living expenses for cat_food
            FinanceTransaction(
                id = "tx1",
                walletId = "w1",
                categoryId = "cat_food",
                amount = Money(1_000_000L),
                type = TransactionType.EXPENSE,
                date = Instant.parse("2026-07-16T10:00:00Z"),
            ),
            FinanceTransaction(
                id = "tx2",
                walletId = "w1",
                categoryId = "cat_food",
                amount = Money(500_000L),
                type = TransactionType.EXPENSE,
                date = Instant.parse("2026-07-20T12:00:00Z"),
            ),
            // Living expense for cat_transport: 1,800,000 (90% of 2M limit)
            FinanceTransaction(
                id = "tx3",
                walletId = "w1",
                categoryId = "cat_transport",
                amount = Money(1_800_000L),
                type = TransactionType.EXPENSE,
                date = Instant.parse("2026-07-22T08:00:00Z"),
            ),
            // Outlay capital -> NOT living expense, must be ignored!
            FinanceTransaction(
                id = "tx4",
                walletId = "w1",
                categoryId = "cat_food",
                amount = Money(3_000_000L),
                type = TransactionType.EXPENSE,
                dealFlowType = DealFlowType.OUTLAY_CAPITAL,
                date = Instant.parse("2026-07-25T14:00:00Z"),
            ),
            // Savings category -> NOT living expense, must be ignored!
            FinanceTransaction(
                id = "tx5",
                walletId = "w1",
                categoryId = SystemCategories.SAVINGS,
                amount = Money(2_000_000L),
                type = TransactionType.EXPENSE,
                date = Instant.parse("2026-07-26T15:00:00Z"),
            ),
            // Income -> must be ignored!
            FinanceTransaction(
                id = "tx6",
                walletId = "w1",
                categoryId = "cat_food",
                amount = Money(500_000L),
                type = TransactionType.INCOME,
                date = Instant.parse("2026-07-27T09:00:00Z"),
            ),
        )

        every { transactionRepository.observePeriod(newPeriod.start, newPeriod.endExclusive) } returns flowOf(transactions)

        val capturedBudgets = slot<List<Budget>>()
        coEvery { budgetRepository.upsertBudgets(capture(capturedBudgets)) } returns AppResult.Success(Unit)

        val result = useCase(
            newPeriod = newPeriod,
            sourcePeriod = sourcePeriod,
            applyProration = false,
        )

        assertTrue(result is AppResult.Success)
        val reconciled = (result as AppResult.Success).value
        assertEquals(2, reconciled.size)

        val foodBudget = reconciled.first { it.categoryId == "cat_food" }
        assertEquals(5_000_000L, foodBudget.limitAmount.value)
        assertEquals(1_500_000L, foodBudget.spentAmount.value) // 1M + 500k
        assertFalse(foodBudget.notified80)
        assertFalse(foodBudget.notified100)

        val transportBudget = reconciled.first { it.categoryId == "cat_transport" }
        assertEquals(2_000_000L, transportBudget.limitAmount.value)
        assertEquals(1_800_000L, transportBudget.spentAmount.value) // 1.8M (90%)
        assertTrue(transportBudget.notified80)
        assertFalse(transportBudget.notified100)

        coVerify(exactly = 1) { budgetRepository.upsertBudgets(any()) }
    }

    @Test
    fun `correctly flags notified100 when spent exceeds limit`() = runTest {
        val sourceBudgets = listOf(
            Budget(
                id = "cat_food_${sourcePeriod.key}",
                categoryId = "cat_food",
                periodKey = sourcePeriod.key,
                limitAmount = Money(1_000_000L),
            ),
        )

        every { budgetRepository.observeBudgets(sourcePeriod.key) } returns flowOf(sourceBudgets)
        every { budgetRepository.observeBudgets(newPeriod.key) } returns flowOf(emptyList())

        val transactions = listOf(
            FinanceTransaction(
                id = "tx1",
                walletId = "w1",
                categoryId = "cat_food",
                amount = Money(1_200_000L),
                type = TransactionType.EXPENSE,
                date = Instant.parse("2026-07-18T10:00:00Z"),
            ),
        )

        every { transactionRepository.observePeriod(newPeriod.start, newPeriod.endExclusive) } returns flowOf(transactions)
        coEvery { budgetRepository.upsertBudgets(any()) } returns AppResult.Success(Unit)

        val result = useCase(newPeriod = newPeriod, sourcePeriod = sourcePeriod)
        assertTrue(result is AppResult.Success)

        val foodBudget = (result as AppResult.Success).value.first()
        assertEquals(1_200_000L, foodBudget.spentAmount.value)
        assertTrue(foodBudget.notified80)
        assertTrue(foodBudget.notified100)
    }

    @Test
    fun `retains target limits when already configured by user`() = runTest {
        val sourceBudgets = listOf(
            Budget(
                id = "cat_food_${sourcePeriod.key}",
                categoryId = "cat_food",
                periodKey = sourcePeriod.key,
                limitAmount = Money(5_000_000L),
            ),
        )

        val existingTargetBudgets = listOf(
            Budget(
                id = "cat_food_${newPeriod.key}",
                categoryId = "cat_food",
                periodKey = newPeriod.key,
                limitAmount = Money(7_000_000L), // User explicitly set higher limit for new period
            ),
        )

        every { budgetRepository.observeBudgets(sourcePeriod.key) } returns flowOf(sourceBudgets)
        every { budgetRepository.observeBudgets(newPeriod.key) } returns flowOf(existingTargetBudgets)
        every { transactionRepository.observePeriod(newPeriod.start, newPeriod.endExclusive) } returns flowOf(emptyList())
        coEvery { budgetRepository.upsertBudgets(any()) } returns AppResult.Success(Unit)

        val result = useCase(newPeriod = newPeriod, sourcePeriod = sourcePeriod)
        assertTrue(result is AppResult.Success)

        val foodBudget = (result as AppResult.Success).value.first()
        assertEquals(7_000_000L, foodBudget.limitAmount.value)
    }

    @Test
    fun `applies proration when requested for transition period`() = runTest {
        // Transition period of 15 days
        val transitionPeriod = FinancialPeriod(
            key = "salary:2026-07-05",
            start = Instant.parse("2026-07-05T00:00:00Z"),
            endExclusive = Instant.parse("2026-07-20T00:00:00Z"),
            displayLabel = "05/07 - 19/07",
            basis = BudgetPeriodBasis.SALARY_CYCLE,
        )

        val sourceBudgets = listOf(
            Budget(
                id = "cat_food_${sourcePeriod.key}",
                categoryId = "cat_food",
                periodKey = sourcePeriod.key,
                limitAmount = Money(6_000_000L),
            ),
        )

        every { budgetRepository.observeBudgets(sourcePeriod.key) } returns flowOf(sourceBudgets)
        every { budgetRepository.observeBudgets(transitionPeriod.key) } returns flowOf(emptyList())
        every { transactionRepository.observePeriod(transitionPeriod.start, transitionPeriod.endExclusive) } returns flowOf(emptyList())
        coEvery { budgetRepository.upsertBudgets(any()) } returns AppResult.Success(Unit)

        // Proration: 6,000,000 * 15 / 30 = 3,000,000
        val result = useCase(
            newPeriod = transitionPeriod,
            sourcePeriod = sourcePeriod,
            applyProration = true,
            standardDays = 30L,
        )

        assertTrue(result is AppResult.Success)
        val proratedBudget = (result as AppResult.Success).value.first()
        assertEquals(3_000_000L, proratedBudget.limitAmount.value)
    }

    @Test
    fun `returns empty list when no budgets to reconcile`() = runTest {
        every { budgetRepository.observeBudgets(sourcePeriod.key) } returns flowOf(emptyList())
        every { budgetRepository.observeBudgets(newPeriod.key) } returns flowOf(emptyList())

        val result = useCase(newPeriod = newPeriod, sourcePeriod = sourcePeriod)
        assertTrue(result is AppResult.Success)
        assertTrue((result as AppResult.Success).value.isEmpty())
        coVerify(exactly = 0) { budgetRepository.upsertBudgets(any()) }
    }
}
