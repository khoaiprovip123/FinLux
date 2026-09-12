package com.finlux.app.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class BudgetProrationCalculatorTest {

    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")

    @Test
    fun `calculateProratedLimit correctly scales half-period`() {
        val standard = Money(3_000_000L)
        val result = BudgetProrationCalculator.calculateProratedLimit(
            standardLimit = standard,
            actualDays = 15L,
            standardDays = 30L,
        )
        assertEquals(1_500_000L, result.value)
    }

    @Test
    fun `calculateProratedLimit correctly scales extended period`() {
        val standard = Money(3_000_000L)
        val result = BudgetProrationCalculator.calculateProratedLimit(
            standardLimit = standard,
            actualDays = 40L,
            standardDays = 30L,
        )
        assertEquals(4_000_000L, result.value)
    }

    @Test
    fun `calculateProratedLimit correctly rounds amounts`() {
        val standard = Money(1_000_000L)
        // 1,000,000 * 7 / 30 = 233,333.333 -> 233,333
        val result = BudgetProrationCalculator.calculateProratedLimit(
            standardLimit = standard,
            actualDays = 7L,
            standardDays = 30L,
        )
        assertEquals(233_333L, result.value)
    }

    @Test
    fun `calculateProratedLimit handles edge cases gracefully`() {
        assertEquals(0L, BudgetProrationCalculator.calculateProratedLimit(Money(0L), 15L).value)
        assertEquals(0L, BudgetProrationCalculator.calculateProratedLimit(Money(1_000_000L), 0L).value)
        assertEquals(0L, BudgetProrationCalculator.calculateProratedLimit(Money(-100_000L), 15L).value)

        // Non-positive standardDays defaults to 30
        val result = BudgetProrationCalculator.calculateProratedLimit(
            standardLimit = Money(3_000_000L),
            actualDays = 15L,
            standardDays = 0L,
        )
        assertEquals(1_500_000L, result.value)
    }

    @Test
    fun `calculateProratedLimitForPeriod calculates days correctly from FinancialPeriod`() {
        val start = ZonedDateTime.of(2026, 7, 5, 0, 0, 0, 0, zone).toInstant()
        val endExclusive = ZonedDateTime.of(2026, 7, 20, 0, 0, 0, 0, zone).toInstant()

        val period = FinancialPeriod(
            key = "salary:2026-07-05",
            start = start,
            endExclusive = endExclusive,
            displayLabel = "05/07 - 19/07",
            basis = BudgetPeriodBasis.SALARY_CYCLE,
        )

        val standard = Money(6_000_000L)
        // Days: 15 days (from July 5 to July 20)
        // 6,000,000 * 15 / 30 = 3,000,000
        val prorated = BudgetProrationCalculator.calculateProratedLimitForPeriod(
            standardLimit = standard,
            period = period,
            standardDays = 30L,
            zoneId = zone,
        )
        assertEquals(3_000_000L, prorated.value)
    }
}
