package com.finlux.app.domain.usecase

import com.finlux.app.core.time.FinanceClock
import com.finlux.app.core.time.FinanceTime
import com.finlux.app.domain.model.FinancialPeriod
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.model.SavingSpinFrequency
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.SavingSpinStatus
import com.finlux.app.domain.model.SavingSpinStreakResult
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalculateSavingSpinStreakUseCase @Inject constructor(
    private val financialPeriodResolver: FinancialPeriodResolver,
    private val clock: FinanceClock,
) {
    operator fun invoke(
        config: SavingSpinConfig,
        sessions: List<SavingSpinSession>,
        now: Instant = clock.now(),
        salaryCycleConfig: SalaryCycleConfig = SalaryCycleConfig(),
    ): SavingSpinStreakResult {
        if (sessions.isEmpty()) return SavingSpinStreakResult(0, 0)

        val zone = FinanceTime.zoneOf(salaryCycleConfig.financeTimeZone)
        val today = now.atZone(zone).toLocalDate()

        val sessionsByKey = sessions.associateBy { it.scheduleKey }

        val currentStreak = calculateCurrentStreak(config, sessionsByKey, today, now, zone, salaryCycleConfig)
        val longestStreak = calculateLongestStreak(config, sessions, zone, salaryCycleConfig)

        return SavingSpinStreakResult(
            currentStreak = currentStreak,
            longestStreak = maxOf(currentStreak, longestStreak),
        )
    }

    /** Overload for quick evaluation or fallback */
    operator fun invoke(
        sessions: List<SavingSpinSession>,
        activeScheduleKey: String? = null,
    ): Int {
        var streak = 0
        for (session in sessions.sortedByDescending { it.createdAt }) {
            val isCurrentPending = session.scheduleKey == activeScheduleKey &&
                session.status in setOf(SavingSpinStatus.READY, SavingSpinStatus.SPUN_PENDING, SavingSpinStatus.SNOOZED)
            if (isCurrentPending) continue
            if (session.status == SavingSpinStatus.COMPLETED) streak++ else break
        }
        return streak
    }

    private fun calculateCurrentStreak(
        config: SavingSpinConfig,
        sessionsByKey: Map<String, SavingSpinSession>,
        today: LocalDate,
        now: Instant,
        zone: ZoneId,
        salaryCycleConfig: SalaryCycleConfig,
    ): Int {
        return when (config.frequency) {
            SavingSpinFrequency.DAILY -> calculateDailyStreak(sessionsByKey, today)
            SavingSpinFrequency.SELECTED_WEEKDAYS -> calculateWeekdayStreak(config.selectedWeekdays, sessionsByKey, today)
            SavingSpinFrequency.WEEKLY -> calculateWeeklyStreak(sessionsByKey, today)
            SavingSpinFrequency.SALARY_CYCLE -> calculateSalaryCycleStreak(sessionsByKey, now, salaryCycleConfig)
        }
    }

    private fun calculateDailyStreak(
        sessionsByKey: Map<String, SavingSpinSession>,
        today: LocalDate,
    ): Int {
        var streak = 0
        var checkDate = today
        val todaySession = sessionsByKey["day:$today"]

        if (todaySession != null) {
            when (todaySession.status) {
                SavingSpinStatus.COMPLETED -> {
                    streak++
                    checkDate = checkDate.minusDays(1)
                }
                SavingSpinStatus.SKIPPED -> return 0
                else -> {
                    // READY, SPUN_PENDING, SNOOZED: ngày hôm nay chưa hoàn tất -> kiểm tra từ hôm qua
                    checkDate = checkDate.minusDays(1)
                }
            }
        } else {
            checkDate = checkDate.minusDays(1)
        }

        while (true) {
            val session = sessionsByKey["day:$checkDate"]
            if (session != null && session.status == SavingSpinStatus.COMPLETED) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    private fun calculateWeekdayStreak(
        selectedWeekdays: Set<Int>,
        sessionsByKey: Map<String, SavingSpinSession>,
        today: LocalDate,
    ): Int {
        if (selectedWeekdays.isEmpty()) return 0

        var streak = 0
        var checkDate = today

        if (today.dayOfWeek.value in selectedWeekdays) {
            val todaySession = sessionsByKey["day:$today"]
            if (todaySession != null) {
                when (todaySession.status) {
                    SavingSpinStatus.COMPLETED -> {
                        streak++
                        checkDate = previousScheduledWeekday(checkDate, selectedWeekdays)
                    }
                    SavingSpinStatus.SKIPPED -> return 0
                    else -> {
                        checkDate = previousScheduledWeekday(checkDate, selectedWeekdays)
                    }
                }
            } else {
                checkDate = previousScheduledWeekday(checkDate, selectedWeekdays)
            }
        } else {
            checkDate = previousScheduledWeekday(checkDate, selectedWeekdays)
        }

        while (true) {
            val session = sessionsByKey["day:$checkDate"]
            if (session != null && session.status == SavingSpinStatus.COMPLETED) {
                streak++
                checkDate = previousScheduledWeekday(checkDate, selectedWeekdays)
            } else {
                break
            }
        }
        return streak
    }

    private fun previousScheduledWeekday(date: LocalDate, selectedWeekdays: Set<Int>): LocalDate {
        var d = date.minusDays(1)
        while (d.dayOfWeek.value !in selectedWeekdays) {
            d = d.minusDays(1)
        }
        return d
    }

    private fun calculateWeeklyStreak(
        sessionsByKey: Map<String, SavingSpinSession>,
        today: LocalDate,
    ): Int {
        val weekFields = WeekFields.ISO
        var streak = 0
        var checkDate = today

        val currentKey = formatWeekKey(checkDate, weekFields)
        val currentSession = sessionsByKey[currentKey]

        if (currentSession != null) {
            when (currentSession.status) {
                SavingSpinStatus.COMPLETED -> {
                    streak++
                    checkDate = checkDate.minusWeeks(1)
                }
                SavingSpinStatus.SKIPPED -> return 0
                else -> {
                    checkDate = checkDate.minusWeeks(1)
                }
            }
        } else {
            checkDate = checkDate.minusWeeks(1)
        }

        while (true) {
            val key = formatWeekKey(checkDate, weekFields)
            val session = sessionsByKey[key]
            if (session != null && session.status == SavingSpinStatus.COMPLETED) {
                streak++
                checkDate = checkDate.minusWeeks(1)
            } else {
                break
            }
        }
        return streak
    }

    private fun formatWeekKey(date: LocalDate, weekFields: WeekFields): String {
        val weekYear = date.get(weekFields.weekBasedYear())
        val week = date.get(weekFields.weekOfWeekBasedYear())
        return "week:$weekYear-W${week.toString().padStart(2, '0')}"
    }

    private fun calculateSalaryCycleStreak(
        sessionsByKey: Map<String, SavingSpinSession>,
        now: Instant,
        salaryCycleConfig: SalaryCycleConfig,
    ): Int {
        var streak = 0
        val currentPeriod = financialPeriodResolver.resolveCurrentPeriod(salaryCycleConfig, now)
        val currentKey = formatPeriodKey(currentPeriod, salaryCycleConfig)
        val currentSession = sessionsByKey[currentKey]

        var checkPeriod: FinancialPeriod
        if (currentSession != null) {
            when (currentSession.status) {
                SavingSpinStatus.COMPLETED -> {
                    streak++
                    checkPeriod = financialPeriodResolver.resolvePreviousPeriodOf(currentPeriod, salaryCycleConfig)
                }
                SavingSpinStatus.SKIPPED -> return 0
                else -> {
                    checkPeriod = financialPeriodResolver.resolvePreviousPeriodOf(currentPeriod, salaryCycleConfig)
                }
            }
        } else {
            checkPeriod = financialPeriodResolver.resolvePreviousPeriodOf(currentPeriod, salaryCycleConfig)
        }

        while (true) {
            val key = formatPeriodKey(checkPeriod, salaryCycleConfig)
            val session = sessionsByKey[key]
            if (session != null && session.status == SavingSpinStatus.COMPLETED) {
                streak++
                checkPeriod = financialPeriodResolver.resolvePreviousPeriodOf(checkPeriod, salaryCycleConfig)
            } else {
                break
            }
        }
        return streak
    }

    private fun formatPeriodKey(period: FinancialPeriod, salaryCycleConfig: SalaryCycleConfig): String {
        val zone = FinanceTime.zoneOf(salaryCycleConfig.financeTimeZone)
        val start = period.start.atZone(zone).toLocalDate()
        val inclusiveEnd = period.endExclusive.atZone(zone).toLocalDate().minusDays(1)
        return "salary:${start}_${inclusiveEnd}"
    }

    private fun calculateLongestStreak(
        config: SavingSpinConfig,
        sessions: List<SavingSpinSession>,
        zone: ZoneId,
        salaryCycleConfig: SalaryCycleConfig,
    ): Int {
        val completed = sessions.filter { it.status == SavingSpinStatus.COMPLETED }
            .sortedBy { it.completedAt ?: it.createdAt }
        if (completed.isEmpty()) return 0

        var maxStreak = 0
        var currentRunning = 0
        var lastDate: LocalDate? = null

        for (session in completed) {
            val date = (session.completedAt ?: session.createdAt).atZone(zone).toLocalDate()
            if (lastDate == null) {
                currentRunning = 1
            } else {
                val isConsecutive = when (config.frequency) {
                    SavingSpinFrequency.DAILY -> date == lastDate.plusDays(1)
                    SavingSpinFrequency.SELECTED_WEEKDAYS -> {
                        var next = lastDate.plusDays(1)
                        while (next.dayOfWeek.value !in config.selectedWeekdays && next <= date) {
                            next = next.plusDays(1)
                        }
                        date == next
                    }
                    SavingSpinFrequency.WEEKLY -> {
                        val wf = WeekFields.ISO
                        date.get(wf.weekOfWeekBasedYear()) == lastDate.get(wf.weekOfWeekBasedYear()) + 1 ||
                            date.get(wf.weekBasedYear()) > lastDate.get(wf.weekBasedYear())
                    }
                    SavingSpinFrequency.SALARY_CYCLE -> true
                }
                if (isConsecutive) {
                    currentRunning++
                } else if (date != lastDate) {
                    currentRunning = 1
                }
            }
            lastDate = date
            maxStreak = maxOf(maxStreak, currentRunning)
        }
        return maxOf(maxStreak, currentRunning)
    }
}
