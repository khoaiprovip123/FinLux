package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.BudgetPeriodBasis
import com.finlux.app.domain.model.PaydayRuleType
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.model.SavingSpinFrequency
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class ResolveSavingSpinScheduleKeyUseCaseTest {
    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private val resolver = ResolveSavingSpinScheduleKeyUseCase(
        DefaultFinancialPeriodResolver(DefaultSalaryCycleCalculator()),
    )
    private val salaryConfig = SalaryCycleConfig(financeTimeZone = zone.id)

    @Test
    fun `daily uses one key per local day`() {
        val config = SavingSpinConfig(enabled = true, frequency = SavingSpinFrequency.DAILY)
        val morning = instant(2026, 8, 31, 7)
        val evening = instant(2026, 8, 31, 22)
        val nextDay = instant(2026, 9, 1, 1)

        assertEquals("day:2026-08-31", resolver(config, salaryConfig, morning)?.value)
        assertEquals(resolver(config, salaryConfig, morning), resolver(config, salaryConfig, evening))
        assertNotEquals(resolver(config, salaryConfig, morning), resolver(config, salaryConfig, nextDay))
    }

    @Test
    fun `weekly uses ISO week including across calendar year boundary`() {
        val config = SavingSpinConfig(enabled = true, frequency = SavingSpinFrequency.WEEKLY)
        val monday = instant(2025, 12, 29, 9)
        val sunday = instant(2026, 1, 4, 9)

        assertEquals("week:2026-W01", resolver(config, salaryConfig, monday)?.value)
        assertEquals(resolver(config, salaryConfig, monday), resolver(config, salaryConfig, sunday))
    }

    @Test
    fun `selected weekdays has no active schedule outside selection`() {
        val config = SavingSpinConfig(
            enabled = true,
            frequency = SavingSpinFrequency.SELECTED_WEEKDAYS,
            selectedWeekdays = setOf(1, 3, 5),
        )

        assertNull(resolver(config, salaryConfig, instant(2026, 9, 1, 9)))
        assertEquals("day:2026-09-02", resolver(config, salaryConfig, instant(2026, 9, 2, 9))?.value)
    }

    @Test
    fun `salary cycle delegates to financial period resolver`() {
        val config = SavingSpinConfig(enabled = true, frequency = SavingSpinFrequency.SALARY_CYCLE)
        val salaryCycle = SalaryCycleConfig(
            enabled = true,
            paydayRuleType = PaydayRuleType.DAY_OF_MONTH,
            paydayDay = 25,
            budgetPeriodBasis = BudgetPeriodBasis.SALARY_CYCLE,
            financeTimeZone = zone.id,
        )

        val result = resolver(config, salaryCycle, instant(2026, 8, 31, 9))

        assertEquals("salary:2026-08-25_2026-09-24", result?.value)
        assertEquals("salary_2026-08-25_2026-09-24", result?.sessionId)
    }

    @Test
    fun `disabled feature has no active schedule`() {
        assertNull(resolver(SavingSpinConfig(enabled = false), salaryConfig, instant(2026, 8, 31, 9)))
    }

    private fun instant(year: Int, month: Int, day: Int, hour: Int) =
        ZonedDateTime.of(year, month, day, hour, 0, 0, 0, zone).toInstant()
}
