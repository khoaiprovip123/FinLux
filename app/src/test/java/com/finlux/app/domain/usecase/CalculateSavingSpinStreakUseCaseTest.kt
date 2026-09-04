package com.finlux.app.domain.usecase

import com.finlux.app.core.time.FinanceClock
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.model.SavingSpinFrequency
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.SavingSpinStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId

class CalculateSavingSpinStreakUseCaseTest {
    private val clock = object : FinanceClock {
        override val zoneId: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh")
        override fun now(): Instant = Instant.parse("2026-09-03T10:00:00Z")
    }
    private val financialResolver = DefaultFinancialPeriodResolver(DefaultSalaryCycleCalculator())
    private val calculate = CalculateSavingSpinStreakUseCase(financialResolver, clock)

    @Test
    fun `counts daily consecutive completed scheduled days`() {
        val config = SavingSpinConfig(frequency = SavingSpinFrequency.DAILY)
        val sessions = listOf(
            session("day:2026-09-03", SavingSpinStatus.COMPLETED, 0),
            session("day:2026-09-02", SavingSpinStatus.COMPLETED, 1),
            session("day:2026-09-01", SavingSpinStatus.COMPLETED, 2),
            session("day:2026-08-31", SavingSpinStatus.COMPLETED, 3),
            session("day:2026-08-30", SavingSpinStatus.SKIPPED, 4),
        )

        val result = calculate(config, sessions, clock.now())
        assertEquals(4, result.currentStreak)
        assertEquals(4, result.longestStreak)
    }

    @Test
    fun `daily streak when today is pending checks from yesterday`() {
        val config = SavingSpinConfig(frequency = SavingSpinFrequency.DAILY)
        val sessions = listOf(
            session("day:2026-09-03", SavingSpinStatus.SPUN_PENDING, 0),
            session("day:2026-09-02", SavingSpinStatus.COMPLETED, 1),
            session("day:2026-09-01", SavingSpinStatus.COMPLETED, 2),
            session("day:2026-08-31", SavingSpinStatus.COMPLETED, 3),
        )

        val result = calculate(config, sessions, clock.now())
        assertEquals(3, result.currentStreak)
    }

    @Test
    fun `daily streak breaks when previous day missed`() {
        val config = SavingSpinConfig(frequency = SavingSpinFrequency.DAILY)
        val sessions = listOf(
            session("day:2026-09-03", SavingSpinStatus.COMPLETED, 0),
            // Missing 2026-09-02
            session("day:2026-09-01", SavingSpinStatus.COMPLETED, 2),
        )

        val result = calculate(config, sessions, clock.now())
        assertEquals(1, result.currentStreak)
    }

    @Test
    fun `selected weekdays streak only checks scheduled days`() {
        // 2026-09-03 is Thursday (day 4).
        // Scheduled days: Monday (1), Wednesday (3), Friday (5)
        // 2026-09-02 (Wed - 3) completed, 2026-08-31 (Mon - 1) completed, 2026-08-28 (Fri - 5) completed
        val config = SavingSpinConfig(
            frequency = SavingSpinFrequency.SELECTED_WEEKDAYS,
            selectedWeekdays = setOf(1, 3, 5),
        )
        val sessions = listOf(
            session("day:2026-09-02", SavingSpinStatus.COMPLETED, 1),
            session("day:2026-08-31", SavingSpinStatus.COMPLETED, 3),
            session("day:2026-08-28", SavingSpinStatus.COMPLETED, 6),
        )

        val result = calculate(config, sessions, clock.now())
        assertEquals(3, result.currentStreak)
    }

    @Test
    fun `weekly streak checks consecutive ISO weeks`() {
        val config = SavingSpinConfig(frequency = SavingSpinFrequency.WEEKLY)
        val sessions = listOf(
            session("week:2026-W36", SavingSpinStatus.COMPLETED, 0),
            session("week:2026-W35", SavingSpinStatus.COMPLETED, 7),
            session("week:2026-W34", SavingSpinStatus.COMPLETED, 14),
        )

        val result = calculate(config, sessions, clock.now())
        assertEquals(3, result.currentStreak)
    }

    private fun session(key: String, status: SavingSpinStatus, daysAgo: Long) = SavingSpinSession(
        id = key.replace(':', '_'),
        scheduleKey = key,
        wheelValues = listOf(Money(10_000)),
        status = status,
        selectedIndex = if (status == SavingSpinStatus.COMPLETED) 0 else null,
        selectedAmount = if (status == SavingSpinStatus.COMPLETED) Money(10_000) else null,
        completedAt = if (status == SavingSpinStatus.COMPLETED) clock.now().minusSeconds(daysAgo * 86_400) else null,
        createdAt = clock.now().minusSeconds(daysAgo * 86_400),
    )
}
