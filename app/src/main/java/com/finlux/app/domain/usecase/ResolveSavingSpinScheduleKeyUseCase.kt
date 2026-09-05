package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.model.SavingSpinFrequency
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import javax.inject.Inject

data class SavingSpinScheduleKey(
    val value: String,
    val sessionId: String = value.replace(':', '_'),
)

class ResolveSavingSpinScheduleKeyUseCase @Inject constructor(
    private val financialPeriodResolver: FinancialPeriodResolver,
) {
    operator fun invoke(
        config: SavingSpinConfig,
        salaryCycleConfig: SalaryCycleConfig,
        now: Instant = Instant.now(),
    ): SavingSpinScheduleKey? {
        if (!config.enabled) return null

        val zone = resolveZone(salaryCycleConfig.financeTimeZone)
        val date = now.atZone(zone).toLocalDate()
        val key = when (config.frequency) {
            SavingSpinFrequency.DAILY -> "day:$date"
            SavingSpinFrequency.SELECTED_WEEKDAYS -> {
                if (date.dayOfWeek.value !in config.selectedWeekdays) return null
                "day:$date"
            }
            SavingSpinFrequency.WEEKLY -> {
                val weekFields = WeekFields.ISO
                val weekYear = date.get(weekFields.weekBasedYear())
                val week = date.get(weekFields.weekOfWeekBasedYear())
                "week:$weekYear-W${week.toString().padStart(2, '0')}"
            }
            SavingSpinFrequency.SALARY_CYCLE -> {
                val period = financialPeriodResolver.resolveReportingPeriodContaining(now, salaryCycleConfig)
                val start = period.start.atZone(zone).toLocalDate()
                val inclusiveEnd = period.endExclusive.atZone(zone).toLocalDate().minusDays(1)
                "salary:${DATE_FORMATTER.format(start)}_${DATE_FORMATTER.format(inclusiveEnd)}"
            }
        }
        return SavingSpinScheduleKey(key)
    }

    private fun resolveZone(raw: String): ZoneId = runCatching { ZoneId.of(raw) }
        .getOrDefault(ZoneId.of(DEFAULT_TIME_ZONE))

    companion object {
        private const val DEFAULT_TIME_ZONE = "Asia/Ho_Chi_Minh"
        private val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE
    }
}
