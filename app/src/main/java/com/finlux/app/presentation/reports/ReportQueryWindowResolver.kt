package com.finlux.app.presentation.reports

import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.usecase.SalaryCycleCalculator
import java.time.DayOfWeek
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

        val today = now.atZone(zone).toLocalDate()

        return when (effectivePeriod) {
            ReportPeriod.TODAY -> {
                val range = ReportRange(today, today)
                createWindowFromRange(range, zone)
            }
            ReportPeriod.YESTERDAY -> {
                val yesterday = today.minusDays(1)
                val range = ReportRange(yesterday, yesterday)
                createWindowFromRange(range, zone)
            }
            ReportPeriod.DAY -> {
                // Mặc định ngày được chọn từ custom hoặc hôm nay
                val targetDay = if (custom.start == custom.end) custom.start else today
                val range = ReportRange(targetDay, targetDay)
                createWindowFromRange(range, zone)
            }
            ReportPeriod.WEEK -> {
                val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val sunday = monday.plusDays(6)
                val range = ReportRange(monday, sunday)
                createWindowFromRange(range, zone)
            }
            ReportPeriod.LAST_7_DAYS -> {
                val range = ReportRange(today.minusDays(6), today)
                createWindowFromRange(range, zone)
            }
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
                val range = ReportRange(today.with(TemporalAdjusters.firstDayOfMonth()), today)
                createWindowFromRange(range, zone)
            }
            ReportPeriod.QUARTER -> {
                val firstMonth = ((today.monthValue - 1) / 3) * 3 + 1
                val range = ReportRange(today.withMonth(firstMonth).withDayOfMonth(1), today)
                createWindowFromRange(range, zone)
            }
            ReportPeriod.YEAR -> {
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
