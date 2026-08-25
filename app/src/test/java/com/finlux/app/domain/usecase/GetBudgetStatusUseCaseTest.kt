package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.YearMonth

class GetBudgetStatusUseCaseTest {
    private val useCase = GetBudgetStatusUseCase()

    @Test
    fun `less than 80 percent is safe`() {
        assertEquals(BudgetLevel.SAFE, useCase(budget(spent = 799)).level)
    }

    @Test
    fun `80 percent is warning`() {
        assertEquals(BudgetLevel.WARNING, useCase(budget(spent = 800)).level)
    }

    @Test
    fun `100 percent is exceeded`() {
        assertEquals(BudgetLevel.EXCEEDED, useCase(budget(spent = 1_000)).level)
    }

    private fun budget(spent: Long) = Budget(
        id = "food_202608",
        categoryId = "food",
        month = YearMonth.of(2026, 8),
        periodKey = "2026-08",
        limitAmount = Money(1_000),
        spentAmount = Money(spent),
        notified80 = false,
        notified100 = false,
    )
}
