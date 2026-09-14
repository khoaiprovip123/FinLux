package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.BudgetPeriodBasis
import com.finlux.app.domain.model.PaydayRuleType
import com.finlux.app.domain.model.SalaryCycleConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Test Suite kiểm thử sức chống chịu múi giờ đa quốc gia (Timezone Resilience) của FinancialPeriodResolver:
 * - Múi giờ âm lớn: New York (America/New_York - GMT-5 / GMT-4 EDT)
 * - Múi giờ dương lớn: Tokyo (Asia/Tokyo - GMT+9)
 * - Ca biên thời gian: 23:55 ngày cuối kỳ và 00:05 ngày đầu kỳ mới
 * - Tính đa múi giờ: Cùng 1 Instant UTC thuộc các chu kỳ khác nhau tùy múi giờ kế toán
 */
class FinancialPeriodTimezoneTest {

    private val calculator = DefaultSalaryCycleCalculator()
    private val resolver = DefaultFinancialPeriodResolver(calculator)

    private val newYorkZone = ZoneId.of("America/New_York")
    private val tokyoZone = ZoneId.of("Asia/Tokyo")

    @Test
    fun `calendar month in New York at 23-55 on last day resolves to current month despite UTC being next day`() {
        val config = SalaryCycleConfig(
            enabled = false,
            budgetPeriodBasis = BudgetPeriodBasis.CALENDAR_MONTH,
            financeTimeZone = "America/New_York",
        )

        // 2026-08-31 23:55:00 in New York (EDT is UTC-4 => UTC is 2026-09-01 03:55:00)
        val endOfAugustNY = ZonedDateTime.of(2026, 8, 31, 23, 55, 0, 0, newYorkZone).toInstant()
        val period = resolver.resolvePeriodContaining(endOfAugustNY, config)

        assertEquals("month:2026-08", period.key)
        assertEquals("Tháng 08/2026", period.displayLabel)
        assertEquals("month:2026-08", resolver.resolvePeriodKey(endOfAugustNY, config))
    }

    @Test
    fun `calendar month in New York at 00-05 on first day resolves to new month`() {
        val config = SalaryCycleConfig(
            enabled = false,
            budgetPeriodBasis = BudgetPeriodBasis.CALENDAR_MONTH,
            financeTimeZone = "America/New_York",
        )

        // 2026-09-01 00:05:00 in New York
        val startOfSeptemberNY = ZonedDateTime.of(2026, 9, 1, 0, 5, 0, 0, newYorkZone).toInstant()
        val period = resolver.resolvePeriodContaining(startOfSeptemberNY, config)

        assertEquals("month:2026-09", period.key)
        assertEquals("Tháng 09/2026", period.displayLabel)
        assertEquals("month:2026-09", resolver.resolvePeriodKey(startOfSeptemberNY, config))
    }

    @Test
    fun `calendar month in Tokyo at 23-55 on last day resolves to current month`() {
        val config = SalaryCycleConfig(
            enabled = false,
            budgetPeriodBasis = BudgetPeriodBasis.CALENDAR_MONTH,
            financeTimeZone = "Asia/Tokyo",
        )

        // 2026-08-31 23:55:00 in Tokyo (JST is UTC+9 => UTC is 2026-08-31 14:55:00)
        val endOfAugustTokyo = ZonedDateTime.of(2026, 8, 31, 23, 55, 0, 0, tokyoZone).toInstant()
        val period = resolver.resolvePeriodContaining(endOfAugustTokyo, config)

        assertEquals("month:2026-08", period.key)
        assertEquals("Tháng 08/2026", period.displayLabel)
        assertEquals("month:2026-08", resolver.resolvePeriodKey(endOfAugustTokyo, config))
    }

    @Test
    fun `calendar month in Tokyo at 00-05 on first day resolves to new month despite UTC being previous day`() {
        val config = SalaryCycleConfig(
            enabled = false,
            budgetPeriodBasis = BudgetPeriodBasis.CALENDAR_MONTH,
            financeTimeZone = "Asia/Tokyo",
        )

        // 2026-09-01 00:05:00 in Tokyo (UTC is 2026-08-31 15:05:00 => still August in UTC!)
        val startOfSeptemberTokyo = ZonedDateTime.of(2026, 9, 1, 0, 5, 0, 0, tokyoZone).toInstant()
        val period = resolver.resolvePeriodContaining(startOfSeptemberTokyo, config)

        assertEquals("month:2026-09", period.key)
        assertEquals("Tháng 09/2026", period.displayLabel)
        assertEquals("month:2026-09", resolver.resolvePeriodKey(startOfSeptemberTokyo, config))
    }

    @Test
    fun `salary cycle in New York with payday 10 cleanly splits at 23-55 and 00-05`() {
        val config = SalaryCycleConfig(
            enabled = true,
            paydayRuleType = PaydayRuleType.DAY_OF_MONTH,
            paydayDay = 10,
            budgetPeriodBasis = BudgetPeriodBasis.SALARY_CYCLE,
            financeTimeZone = "America/New_York",
        )

        // 23:55 on 9th September in NY -> still belongs to August cycle (10/08 - 09/09)
        val lateNightOldCycle = ZonedDateTime.of(2026, 9, 9, 23, 55, 0, 0, newYorkZone).toInstant()
        val oldPeriod = resolver.resolvePeriodContaining(lateNightOldCycle, config)
        assertEquals("salary:2026-08-10", oldPeriod.key)
        assertEquals("salary:2026-08-10", resolver.resolvePeriodKey(lateNightOldCycle, config))

        // 00:05 on 10th September in NY -> belongs to new cycle (10/09 - 09/10)
        val earlyMorningNewCycle = ZonedDateTime.of(2026, 9, 10, 0, 5, 0, 0, newYorkZone).toInstant()
        val newPeriod = resolver.resolvePeriodContaining(earlyMorningNewCycle, config)
        assertEquals("salary:2026-09-10", newPeriod.key)
        assertEquals("salary:2026-09-10", resolver.resolvePeriodKey(earlyMorningNewCycle, config))
    }

    @Test
    fun `salary cycle in Tokyo with payday 25 cleanly splits at 23-55 and 00-05`() {
        val config = SalaryCycleConfig(
            enabled = true,
            paydayRuleType = PaydayRuleType.DAY_OF_MONTH,
            paydayDay = 25,
            budgetPeriodBasis = BudgetPeriodBasis.SALARY_CYCLE,
            financeTimeZone = "Asia/Tokyo",
        )

        // 23:55 on 24th August in Tokyo -> belongs to cycle started on 25/07
        val lateNightOldCycle = ZonedDateTime.of(2026, 8, 24, 23, 55, 0, 0, tokyoZone).toInstant()
        val oldPeriod = resolver.resolvePeriodContaining(lateNightOldCycle, config)
        assertEquals("salary:2026-07-25", oldPeriod.key)
        assertEquals("salary:2026-07-25", resolver.resolvePeriodKey(lateNightOldCycle, config))

        // 00:05 on 25th August in Tokyo -> belongs to cycle started on 25/08
        val earlyMorningNewCycle = ZonedDateTime.of(2026, 8, 25, 0, 5, 0, 0, tokyoZone).toInstant()
        val newPeriod = resolver.resolvePeriodContaining(earlyMorningNewCycle, config)
        assertEquals("salary:2026-08-25", newPeriod.key)
        assertEquals("salary:2026-08-25", resolver.resolvePeriodKey(earlyMorningNewCycle, config))
    }

    @Test
    fun `same UTC instant correctly resolves to different period keys depending on local finance timezone`() {
        // Instant: 2026-08-31 20:00:00 UTC
        // In New York (EDT, UTC-4): 2026-08-31 16:00 -> Month 2026-08
        // In Tokyo (JST, UTC+9): 2026-09-01 05:00 -> Month 2026-09
        val globalInstant = Instant.parse("2026-08-31T20:00:00Z")

        val nyConfig = SalaryCycleConfig(
            enabled = false,
            budgetPeriodBasis = BudgetPeriodBasis.CALENDAR_MONTH,
            financeTimeZone = "America/New_York",
        )
        val tokyoConfig = SalaryCycleConfig(
            enabled = false,
            budgetPeriodBasis = BudgetPeriodBasis.CALENDAR_MONTH,
            financeTimeZone = "Asia/Tokyo",
        )

        val nyPeriodKey = resolver.resolvePeriodKey(globalInstant, nyConfig)
        val tokyoPeriodKey = resolver.resolvePeriodKey(globalInstant, tokyoConfig)

        assertEquals("month:2026-08", nyPeriodKey)
        assertEquals("month:2026-09", tokyoPeriodKey)
    }

    @Test
    fun `invalid timezone safely falls back to Asia_Ho_Chi_Minh without throwing`() {
        val config = SalaryCycleConfig(
            enabled = false,
            budgetPeriodBasis = BudgetPeriodBasis.CALENDAR_MONTH,
            financeTimeZone = "Invalid/Invalid_Zone_123",
        )

        val vnZone = ZoneId.of("Asia/Ho_Chi_Minh")
        val testInstant = ZonedDateTime.of(2026, 8, 15, 12, 0, 0, 0, vnZone).toInstant()
        val period = resolver.resolvePeriodContaining(testInstant, config)

        assertEquals("month:2026-08", period.key)
    }
}
