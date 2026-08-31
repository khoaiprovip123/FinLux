package com.finlux.app.data.local.savingspin

import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.SavingSpinStatus
import com.finlux.app.domain.repository.SavingSpinScheduler
import com.finlux.app.domain.usecase.SyncSavingSpinScheduleUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant

class AlarmSavingSpinSchedulerTest {
    private val scheduler = mockk<SavingSpinScheduler>(relaxed = true)
    private val sync = SyncSavingSpinScheduleUseCase(scheduler)
    private val trigger = Instant.parse("2026-09-01T02:00:00Z")

    @Test
    fun `disabled feature cancels alarm`() {
        sync(SavingSpinConfig(enabled = false), null, trigger)

        verify(exactly = 1) { scheduler.cancel() }
        verify(exactly = 0) { scheduler.schedule(any(), any()) }
    }

    @Test
    fun `enabled reminder schedules exact requested trigger`() {
        val config = SavingSpinConfig(enabled = true, reminderEnabled = true)

        sync(config, session(SavingSpinStatus.READY), trigger)

        verify(exactly = 1) { scheduler.schedule(config, trigger) }
    }

    @Test
    fun `completed or skipped current session cancels reminder`() {
        sync(SavingSpinConfig(enabled = true), session(SavingSpinStatus.COMPLETED), trigger)
        sync(SavingSpinConfig(enabled = true), session(SavingSpinStatus.SKIPPED), trigger)

        verify(exactly = 2) { scheduler.cancel() }
    }

    @Test
    fun `snooze delegates a new exact instant`() {
        every { scheduler.snooze(trigger) } returns Unit

        scheduler.snooze(trigger)

        verify(exactly = 1) { scheduler.snooze(trigger) }
    }

    private fun session(status: SavingSpinStatus) = SavingSpinSession(
        id = "day_2026-08-31",
        scheduleKey = "day:2026-08-31",
        wheelValues = listOf(Money(5_000)),
        status = status,
    )
}
