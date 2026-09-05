package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.BudgetPeriodBasis
import com.finlux.app.domain.model.PaydayRuleType
import com.finlux.app.domain.model.SalaryCycleConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class FinancialPeriodResolverTest {

    private val calculator = DefaultSalaryCycleCalculator()
    private val resolver = DefaultFinancialPeriodResolver(calculator)
    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")

    @Test
    fun `calendar month returns expected boundaries and key`() {
        val config = SalaryCycleConfig(
            enabled = false,
            budgetPeriodBasis = BudgetPeriodBasis.CALENDAR_MONTH,
            financeTimeZone = "Asia/Ho_Chi_Minh",
        )
        val now = ZonedDateTime.of(2026, 8, 25, 14, 30, 0, 0, zone).toInstant()
        val period = resolver.resolveCurrentPeriod(config, now)

        assertEquals("month:2026-08", period.key)
        assertEquals(BudgetPeriodBasis.CALENDAR_MONTH, period.basis)
        assertEquals("Tháng 08/2026", period.displayLabel)

        val expectedStart = ZonedDateTime.of(2026, 8, 1, 0, 0, 0, 0, zone).toInstant()
        val expectedEnd = ZonedDateTime.of(2026, 9, 1, 0, 0, 0, 0, zone).toInstant()
        assertEquals(expectedStart, period.start)
        assertEquals(expectedEnd, period.endExclusive)

        val prevPeriod = resolver.resolvePreviousPeriod(config, now)
        assertEquals("month:2026-07", prevPeriod.key)
        assertEquals("Tháng 07/2026", prevPeriod.displayLabel)
    }

    @Test
    fun `reporting period remains salary cycle when budget basis is calendar month`() {
        val config = SalaryCycleConfig(
            enabled = true,
            paydayRuleType = PaydayRuleType.DAY_OF_MONTH,
            paydayDay = 25,
            budgetPeriodBasis = BudgetPeriodBasis.CALENDAR_MONTH,
            financeTimeZone = "Asia/Ho_Chi_Minh",
        )
        val now = ZonedDateTime.of(2026, 9, 5, 12, 0, 0, 0, zone).toInstant()

        val budgetPeriod = resolver.resolvePeriodContaining(now, config)
        val reportingPeriod = resolver.resolveReportingPeriodContaining(now, config)

        assertEquals("month:2026-09", budgetPeriod.key)
        assertEquals(BudgetPeriodBasis.CALENDAR_MONTH, budgetPeriod.basis)
        assertEquals("salary:2026-08-25", reportingPeriod.key)
        assertEquals(BudgetPeriodBasis.SALARY_CYCLE, reportingPeriod.basis)
        assertEquals("25/08 - 24/09", reportingPeriod.displayLabel)
    }

    @Test
    fun `next reporting period stays salary based when budget basis is calendar`() {
        val config = SalaryCycleConfig(
            enabled = true,
            paydayRuleType = PaydayRuleType.DAY_OF_MONTH,
            paydayDay = 25,
            budgetPeriodBasis = BudgetPeriodBasis.CALENDAR_MONTH,
            financeTimeZone = "Asia/Ho_Chi_Minh",
        )
        val now = ZonedDateTime.of(2026, 9, 5, 12, 0, 0, 0, zone).toInstant()
        val current = resolver.resolveReportingPeriodContaining(now, config)
        val next = resolver.resolveNextReportingPeriodOf(current, config)

        assertEquals("salary:2026-08-25", current.key)
        assertEquals("salary:2026-09-25", next.key)
        assertEquals("25/09 - 24/10", next.displayLabel)
    }

    @Test
    fun `salary cycle with payday 10 returns period starting on 10th`() {
        val config = SalaryCycleConfig(
            enabled = true,
            paydayRuleType = PaydayRuleType.DAY_OF_MONTH,
            paydayDay = 10,
            budgetPeriodBasis = BudgetPeriodBasis.SALARY_CYCLE,
            financeTimeZone = "Asia/Ho_Chi_Minh",
        )
        // 25th Aug -> within cycle 10/08 to 10/09
        val now = ZonedDateTime.of(2026, 8, 25, 14, 0, 0, 0, zone).toInstant()
        val period = resolver.resolveCurrentPeriod(config, now)

        assertEquals("salary:2026-08-10", period.key)
        assertEquals(BudgetPeriodBasis.SALARY_CYCLE, period.basis)
        assertEquals("10/08 - 09/09", period.displayLabel)

        val expectedStart = ZonedDateTime.of(2026, 8, 10, 0, 0, 0, 0, zone).toInstant()
        val expectedEnd = ZonedDateTime.of(2026, 9, 10, 0, 0, 0, 0, zone).toInstant()
        assertEquals(expectedStart, period.start)
        assertEquals(expectedEnd, period.endExclusive)

        val prevPeriod = resolver.resolvePreviousPeriod(config, now)
        assertEquals("salary:2026-07-10", prevPeriod.key)
        assertEquals("10/07 - 09/08", prevPeriod.displayLabel)
    }

    @Test
    fun `salary cycle with payday 10 before payday returns previous month cycle`() {
        val config = SalaryCycleConfig(
            enabled = true,
            paydayRuleType = PaydayRuleType.DAY_OF_MONTH,
            paydayDay = 10,
            budgetPeriodBasis = BudgetPeriodBasis.SALARY_CYCLE,
            financeTimeZone = "Asia/Ho_Chi_Minh",
        )
        // 5th Aug -> within cycle 10/07 to 10/08
        val now = ZonedDateTime.of(2026, 8, 5, 10, 0, 0, 0, zone).toInstant()
        val period = resolver.resolveCurrentPeriod(config, now)

        assertEquals("salary:2026-07-10", period.key)
        assertEquals("10/07 - 09/08", period.displayLabel)
    }

    @Test
    fun `salary cycle handles payday 31 in months with fewer days`() {
        val config = SalaryCycleConfig(
            enabled = true,
            paydayRuleType = PaydayRuleType.DAY_OF_MONTH,
            paydayDay = 31,
            budgetPeriodBasis = BudgetPeriodBasis.SALARY_CYCLE,
            financeTimeZone = "Asia/Ho_Chi_Minh",
        )
        // In Feb 2026 (non-leap year, 28 days) -> payday clamped to Feb 28
        val febMid = ZonedDateTime.of(2026, 2, 15, 0, 0, 0, 0, zone).toInstant()
        val febPeriod = resolver.resolveCurrentPeriod(config, febMid)

        // Started Jan 31, ends Feb 28
        val expectedStart = ZonedDateTime.of(2026, 1, 31, 0, 0, 0, 0, zone).toInstant()
        val expectedEnd = ZonedDateTime.of(2026, 2, 28, 0, 0, 0, 0, zone).toInstant()
        assertEquals(expectedStart, febPeriod.start)
        assertEquals(expectedEnd, febPeriod.endExclusive)
        assertEquals("salary:2026-01-31", febPeriod.key)
    }

    @Test
    fun `salary cycle LAST_DAY_OF_MONTH rule`() {
        val config = SalaryCycleConfig(
            enabled = true,
            paydayRuleType = PaydayRuleType.LAST_DAY_OF_MONTH,
            budgetPeriodBasis = BudgetPeriodBasis.SALARY_CYCLE,
            financeTimeZone = "Asia/Ho_Chi_Minh",
        )
        // In April (30 days) -> April 30 is payday
        val apr15 = ZonedDateTime.of(2026, 4, 15, 0, 0, 0, 0, zone).toInstant()
        val period = resolver.resolveCurrentPeriod(config, apr15)

        val expectedStart = ZonedDateTime.of(2026, 3, 31, 0, 0, 0, 0, zone).toInstant()
        val expectedEnd = ZonedDateTime.of(2026, 4, 30, 0, 0, 0, 0, zone).toInstant()
        assertEquals(expectedStart, period.start)
        assertEquals(expectedEnd, period.endExclusive)
    }

    @Test
    fun `salary cycle FIRST_DAY_OF_MONTH rule behaves identical to calendar month`() {
        val config = SalaryCycleConfig(
            enabled = true,
            paydayRuleType = PaydayRuleType.FIRST_DAY_OF_MONTH,
            budgetPeriodBasis = BudgetPeriodBasis.SALARY_CYCLE,
            financeTimeZone = "Asia/Ho_Chi_Minh",
        )
        val now = ZonedDateTime.of(2026, 8, 15, 0, 0, 0, 0, zone).toInstant()
        val period = resolver.resolveCurrentPeriod(config, now)

        val expectedStart = ZonedDateTime.of(2026, 8, 1, 0, 0, 0, 0, zone).toInstant()
        val expectedEnd = ZonedDateTime.of(2026, 9, 1, 0, 0, 0, 0, zone).toInstant()
        assertEquals(expectedStart, period.start)
        assertEquals(expectedEnd, period.endExclusive)
        assertEquals("salary:2026-08-01", period.key)
    }
}
