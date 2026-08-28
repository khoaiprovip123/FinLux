package com.finlux.app.data.local.reminder

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.model.ReminderRecurrence
import com.finlux.app.domain.repository.ReminderRepository
import com.finlux.app.domain.repository.ReminderScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class ReminderSyncObserverTest {

    private val reminderRepository: ReminderRepository = mockk(relaxed = true)
    private val reminderScheduler: ReminderScheduler = mockk(relaxed = true)
    private lateinit var syncObserver: ReminderSyncObserver

    @BeforeEach
    fun setUp() {
        coEvery { reminderRepository.upsertReminder(any()) } returns AppResult.Success("ok")
        syncObserver = ReminderSyncObserver(reminderRepository, reminderScheduler)
    }

    @Test
    fun `syncReminders schedules active reminder and cancels disabled reminder`() = runTest {
        val activeFutureReminder = Reminder(
            id = "rem_active",
            title = "Internet",
            amount = Money(300_000L),
            categoryId = "cat_bill",
            walletId = "wal_1",
            recurrence = ReminderRecurrence.MONTHLY,
            startDate = Instant.now(),
            enabled = true,
            nextTriggerDate = Instant.now().plusSeconds(86400),
        )

        val disabledReminder = Reminder(
            id = "rem_disabled",
            title = "Gym",
            amount = Money(500_000L),
            categoryId = "cat_gym",
            walletId = "wal_1",
            recurrence = ReminderRecurrence.MONTHLY,
            startDate = Instant.now(),
            enabled = false,
            nextTriggerDate = Instant.now().plusSeconds(86400),
        )

        syncObserver.syncReminders(listOf(activeFutureReminder, disabledReminder))

        verify { reminderScheduler.schedule(activeFutureReminder) }
        verify { reminderScheduler.cancel("rem_disabled") }
    }

    @Test
    fun `syncReminders auto advances expired active reminder into future`() = runTest {
        val expiredReminder = Reminder(
            id = "rem_expired",
            title = "Netflix",
            amount = Money(260_000L),
            categoryId = "cat_sub",
            walletId = "wal_1",
            recurrence = ReminderRecurrence.MONTHLY,
            startDate = Instant.now().minusSeconds(86400 * 40),
            enabled = true,
            nextTriggerDate = Instant.now().minusSeconds(86400 * 10),
        )

        syncObserver.syncReminders(listOf(expiredReminder))

        coVerify { reminderRepository.upsertReminder(match { it.nextTriggerDate.isAfter(Instant.now()) }) }
        verify { reminderScheduler.schedule(match { it.id == "rem_expired" && it.nextTriggerDate.isAfter(Instant.now()) }) }
    }
}
