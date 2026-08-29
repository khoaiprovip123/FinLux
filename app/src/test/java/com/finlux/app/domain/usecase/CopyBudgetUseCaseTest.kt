package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.BudgetPeriodBasis
import com.finlux.app.domain.model.FinancialPeriod
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.repository.BudgetRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class CopyBudgetUseCaseTest {

    private val budgetRepository: BudgetRepository = mockk()
    private val saveBudget: SaveBudgetUseCase = mockk()
    private lateinit var useCase: CopyBudgetUseCase

    private val sourcePeriod = FinancialPeriod(
        key = "month:2026-08",
        start = Instant.parse("2026-08-01T00:00:00Z"),
        endExclusive = Instant.parse("2026-09-01T00:00:00Z"),
        displayLabel = "Tháng 08/2026",
        basis = BudgetPeriodBasis.CALENDAR_MONTH,
    )

    private val targetPeriod = FinancialPeriod(
        key = "month:2026-09",
        start = Instant.parse("2026-09-01T00:00:00Z"),
        endExclusive = Instant.parse("2026-10-01T00:00:00Z"),
        displayLabel = "Tháng 09/2026",
        basis = BudgetPeriodBasis.CALENDAR_MONTH,
    )

    @BeforeEach
    fun setUp() {
        useCase = CopyBudgetUseCase(budgetRepository, saveBudget)
    }

    @Test
    fun `returns error when source period has no budgets`() = runTest {
        every { budgetRepository.observeBudgets(sourcePeriod.key) } returns flowOf(emptyList())

        val result = useCase(sourcePeriod, targetPeriod)

        assertTrue(result is AppResult.Error)
        assertEquals("Kỳ Tháng 08/2026 chưa có ngân sách nào để sao chép", (result as AppResult.Error).message)
    }

    @Test
    fun `copies all budgets with reset spent amount when target is empty`() = runTest {
        val srcBudgets = listOf(
            Budget(
                id = "cat1_month:2026-08",
                categoryId = "cat1",
                periodKey = sourcePeriod.key,
                limitAmount = Money(5_000_000L),
                spentAmount = Money(3_000_000L),
                notified80 = true,
                notified100 = false,
            ),
            Budget(
                id = "cat2_month:2026-08",
                categoryId = "cat2",
                periodKey = sourcePeriod.key,
                limitAmount = Money(2_000_000L),
                spentAmount = Money(2_500_000L),
                notified80 = true,
                notified100 = true,
            ),
        )

        every { budgetRepository.observeBudgets(sourcePeriod.key) } returns flowOf(srcBudgets)
        every { budgetRepository.observeBudgets(targetPeriod.key) } returns flowOf(emptyList())

        val capturedBudgets = mutableListOf<Budget>()
        val slot = slot<Budget>()
        coEvery { saveBudget(capture(slot)) } answers {
            capturedBudgets.add(slot.captured)
            AppResult.Success(slot.captured.id)
        }

        val result = useCase(sourcePeriod, targetPeriod)

        assertTrue(result is AppResult.Success)
        assertEquals(2, (result as AppResult.Success).value)
        assertEquals(2, capturedBudgets.size)

        val first = capturedBudgets[0]
        assertEquals("cat1", first.categoryId)
        assertEquals("month:2026-09", first.periodKey)
        assertEquals(5_000_000L, first.limitAmount.value)
        assertEquals(0L, first.spentAmount.value) // reset
        assertEquals(false, first.notified80)
        assertEquals(false, first.notified100)

        val second = capturedBudgets[1]
        assertEquals("cat2", second.categoryId)
        assertEquals("month:2026-09", second.periodKey)
        assertEquals(2_000_000L, second.limitAmount.value)
        assertEquals(0L, second.spentAmount.value) // reset
    }

    @Test
    fun `skips existing categories in target period when overwriteExisting is false`() = runTest {
        val srcBudgets = listOf(
            Budget(id = "cat1_src", categoryId = "cat1", periodKey = sourcePeriod.key, limitAmount = Money(5_000_000L)),
            Budget(id = "cat2_src", categoryId = "cat2", periodKey = sourcePeriod.key, limitAmount = Money(2_000_000L)),
        )
        val targetBudgets = listOf(
            Budget(id = "cat1_tgt", categoryId = "cat1", periodKey = targetPeriod.key, limitAmount = Money(4_000_000L)),
        )

        every { budgetRepository.observeBudgets(sourcePeriod.key) } returns flowOf(srcBudgets)
        every { budgetRepository.observeBudgets(targetPeriod.key) } returns flowOf(targetBudgets)
        coEvery { saveBudget(any()) } returns AppResult.Success("ok")

        val result = useCase(sourcePeriod, targetPeriod, overwriteExisting = false)

        assertTrue(result is AppResult.Success)
        assertEquals(1, (result as AppResult.Success).value)
        coVerify(exactly = 1) { saveBudget(match { it.categoryId == "cat2" }) }
        coVerify(exactly = 0) { saveBudget(match { it.categoryId == "cat1" }) }
    }

    @Test
    fun `overwrites existing categories in target period when overwriteExisting is true`() = runTest {
        val srcBudgets = listOf(
            Budget(id = "cat1_src", categoryId = "cat1", periodKey = sourcePeriod.key, limitAmount = Money(5_000_000L)),
            Budget(id = "cat2_src", categoryId = "cat2", periodKey = sourcePeriod.key, limitAmount = Money(2_000_000L)),
        )
        val targetBudgets = listOf(
            Budget(id = "cat1_tgt", categoryId = "cat1", periodKey = targetPeriod.key, limitAmount = Money(4_000_000L)),
        )

        every { budgetRepository.observeBudgets(sourcePeriod.key) } returns flowOf(srcBudgets)
        every { budgetRepository.observeBudgets(targetPeriod.key) } returns flowOf(targetBudgets)
        coEvery { saveBudget(any()) } returns AppResult.Success("ok")

        val result = useCase(sourcePeriod, targetPeriod, overwriteExisting = true)

        assertTrue(result is AppResult.Success)
        assertEquals(2, (result as AppResult.Success).value)
        coVerify(exactly = 1) { saveBudget(match { it.categoryId == "cat1" }) }
        coVerify(exactly = 1) { saveBudget(match { it.categoryId == "cat2" }) }
    }
}
