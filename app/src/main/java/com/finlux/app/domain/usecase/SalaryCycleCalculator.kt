package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.FinancialCycle
import com.finlux.app.domain.model.PaydayRuleType
import com.finlux.app.domain.model.SalaryCycleConfig
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

interface SalaryCycleCalculator {
    fun cycleContaining(
        instant: Instant,
        config: SalaryCycleConfig,
        zoneId: ZoneId,
    ): FinancialCycle

    fun previousCycle(
        cycle: FinancialCycle,
        config: SalaryCycleConfig,
        zoneId: ZoneId,
    ): FinancialCycle
}

class DefaultSalaryCycleCalculator @Inject constructor() : SalaryCycleCalculator {
    override fun cycleContaining(
        instant: Instant,
        config: SalaryCycleConfig,
        zoneId: ZoneId,
    ): FinancialCycle {
        val month = YearMonth.from(instant.atZone(zoneId))
        val currentMonthBoundary = boundary(month, config, zoneId)
        val startMonth = if (instant >= currentMonthBoundary) month else month.minusMonths(1)
        return cycleForStartMonth(startMonth, config, zoneId)
    }

    override fun previousCycle(
        cycle: FinancialCycle,
        config: SalaryCycleConfig,
        zoneId: ZoneId,
    ): FinancialCycle = cycleContaining(cycle.start.minusMillis(1), config, zoneId)

    private fun cycleForStartMonth(
        startMonth: YearMonth,
        config: SalaryCycleConfig,
        zoneId: ZoneId,
    ): FinancialCycle {
        val start = boundary(startMonth, config, zoneId)
        val endExclusive = boundary(startMonth.plusMonths(1), config, zoneId)
        val formatter = DateTimeFormatter.ofPattern("dd/MM")
        val startDate = start.atZone(zoneId).toLocalDate()
        val inclusiveEndDate = endExclusive.atZone(zoneId).toLocalDate().minusDays(1)
        return FinancialCycle(
            start = start,
            endExclusive = endExclusive,
            label = "${formatter.format(startDate)} - ${formatter.format(inclusiveEndDate)}",
        )
    }

    private fun boundary(
        month: YearMonth,
        config: SalaryCycleConfig,
        zoneId: ZoneId,
    ): Instant {
        val day = when (config.paydayRuleType) {
            PaydayRuleType.FIRST_DAY_OF_MONTH -> 1
            PaydayRuleType.LAST_DAY_OF_MONTH -> month.lengthOfMonth()
            PaydayRuleType.DAY_OF_MONTH -> config.paydayDay.coerceIn(1, month.lengthOfMonth())
        }
        return month.atDay(day).atStartOfDay(zoneId).toInstant()
    }
}
