package com.finlux.app.presentation.reports

import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.usecase.SalaryCycleCalculator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class ReportQueryWindow(
    val range: ReportRange,
    val currentStart: Instant,
    val currentEndExclusive: Instant,
    val previousStart: Instant,
    val previousEndExclusive: Instant,
)

class ReportQueryWindowResolver @Inject constructor(
    private val salaryCycleCalculator: SalaryCycleCalculator,
) {
    fun resolve(
        period: ReportPeriod,
        custom: ReportRange,
        now: Instant,
        salaryConfig: SalaryCycleConfig,
        zone: ZoneId,
    ): ReportQueryWindow {
        val effectivePeriod = if (period == ReportPeriod.SALARY_CYCLE && !salaryConfig.enabled) {
            ReportPeriod.MONTH
        } else {
            period
        }

        return when (effectivePeriod) {
            ReportPeriod.SALARY_CYCLE -> {
                val currentCycle = salaryCycleCalculator.cycleContaining(now, salaryConfig, zone)
                val previousCycle = salaryCycleCalculator.previousCycle(currentCycle, salaryConfig, zone)
                val rangeStart = currentCycle.start.atZone(zone).toLocalDate()
                val rangeEnd = currentCycle.endExclusive.atZone(zone).minusDays(1).toLocalDate()
                ReportQueryWindow(
                    range = ReportRange(rangeStart, rangeEnd),
                    currentStart = currentCycle.start,
                    currentEndExclusive = currentCycle.endExclusive,
                    previousStart = previousCycle.start,
                    previousEndExclusive = previousCycle.endExclusive,
                )
            }
            ReportPeriod.MONTH -> {
                val today = now.atZone(zone).toLocalDate()
                val range = ReportRange(today.with(TemporalAdjusters.firstDayOfMonth()), today)
                createWindowFromRange(range, zone)
            }
            ReportPeriod.QUARTER -> {
                val today = now.atZone(zone).toLocalDate()
                val firstMonth = ((today.monthValue - 1) / 3) * 3 + 1
                val range = ReportRange(today.withMonth(firstMonth).withDayOfMonth(1), today)
                createWindowFromRange(range, zone)
            }
            ReportPeriod.YEAR -> {
                val today = now.atZone(zone).toLocalDate()
                val range = ReportRange(today.with(TemporalAdjusters.firstDayOfYear()), today)
                createWindowFromRange(range, zone)
            }
            ReportPeriod.CUSTOM -> {
                createWindowFromRange(custom, zone)
            }
        }
    }

    private fun createWindowFromRange(range: ReportRange, zone: ZoneId): ReportQueryWindow {
        val currentStart = range.start.atStartOfDay(zone).toInstant()
        val currentEndExclusive = range.end.plusDays(1).atStartOfDay(zone).toInstant()
        val dayCount = ChronoUnit.DAYS.between(range.start, range.end) + 1
        val previousEndExclusive = currentStart
        val previousStart = range.start.minusDays(dayCount).atStartOfDay(zone).toInstant()
        return ReportQueryWindow(
            range = range,
            currentStart = currentStart,
            currentEndExclusive = currentEndExclusive,
            previousStart = previousStart,
            previousEndExclusive = previousEndExclusive,
        )
    }
}
