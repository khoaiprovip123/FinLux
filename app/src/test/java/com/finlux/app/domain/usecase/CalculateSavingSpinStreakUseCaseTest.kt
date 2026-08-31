package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.SavingSpinStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class CalculateSavingSpinStreakUseCaseTest {
    private val calculate = CalculateSavingSpinStreakUseCase()

    @Test
    fun `counts consecutive completed scheduled periods`() {
        val sessions = listOf(
            session("week:2026-W36", SavingSpinStatus.COMPLETED, 3),
            session("week:2026-W35", SavingSpinStatus.COMPLETED, 2),
            session("week:2026-W34", SavingSpinStatus.SKIPPED, 1),
            session("week:2026-W33", SavingSpinStatus.COMPLETED, 0),
        )

        assertEquals(2, calculate(sessions))
    }

    @Test
    fun `current pending period does not break prior streak`() {
        val sessions = listOf(
            session("day:2026-08-31", SavingSpinStatus.SNOOZED, 3),
            session("day:2026-08-30", SavingSpinStatus.COMPLETED, 2),
            session("day:2026-08-29", SavingSpinStatus.COMPLETED, 1),
        )

        assertEquals(2, calculate(sessions, activeScheduleKey = "day:2026-08-31"))
    }

    @Test
    fun `overdue ready period breaks streak`() {
        val sessions = listOf(
            session("day:2026-08-30", SavingSpinStatus.READY, 2),
            session("day:2026-08-29", SavingSpinStatus.COMPLETED, 1),
        )

        assertEquals(0, calculate(sessions, activeScheduleKey = "day:2026-08-31"))
    }

    private fun session(key: String, status: SavingSpinStatus, day: Long) = SavingSpinSession(
        id = key.replace(':', '_'),
        scheduleKey = key,
        wheelValues = listOf(Money(5_000)),
        status = status,
        selectedIndex = if (status == SavingSpinStatus.COMPLETED) 0 else null,
        selectedAmount = if (status == SavingSpinStatus.COMPLETED) Money(5_000) else null,
        createdAt = Instant.EPOCH.plusSeconds(day * 86_400),
    )
}
