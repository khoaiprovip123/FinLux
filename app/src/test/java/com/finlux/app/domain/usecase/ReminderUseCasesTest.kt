package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.model.ReminderRecurrence
import com.finlux.app.domain.repository.ReminderRepository
import com.finlux.app.domain.repository.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class ReminderUseCasesTest {

    private val sampleReminder = Reminder(
        id = "rem-1",
        title = "Ăn sáng",
        amount = Money(15_000L),
        categoryId = "food",
        walletId = "cash",
        recurrence = ReminderRecurrence.DAILY,
        startDate = Instant.now(),
        enabled = true,
        nextTriggerDate = Instant.now().plusSeconds(3600),
    )

    private val fakeRepo = object : ReminderRepository {
        var upsertCalls = 0
        var deleteCalls = 0

        override fun observeReminders(): Flow<List<Reminder>> = flowOf(listOf(sampleReminder))

        override suspend fun upsertReminder(reminder: Reminder): AppResult<String> {
            upsertCalls++
            return AppResult.Success(reminder.id)
        }

        override suspend fun deleteReminder(reminder: Reminder): AppResult<Unit> {
            deleteCalls++
            return AppResult.Success(Unit)
        }
    }

    private val fakeScheduler = object : ReminderScheduler {
        val scheduledList = mutableListOf<Reminder>()
        val cancelledList = mutableListOf<String>()

        override fun schedule(reminder: Reminder) {
            scheduledList.add(reminder)
        }

        override fun cancel(reminderId: String) {
            cancelledList.add(reminderId)
        }
    }

    @Test
    fun `saveReminder schedules alarm when enabled`() = runTest {
        val saveUseCase = SaveReminderUseCase(fakeRepo, fakeScheduler)
        val result = saveUseCase(sampleReminder)

        assertEquals(AppResult.Success("rem-1"), result)
        assertEquals(1, fakeRepo.upsertCalls)
        assertEquals(1, fakeScheduler.scheduledList.size)
        assertEquals("rem-1", fakeScheduler.scheduledList.first().id)
    }

    @Test
    fun `saveReminder cancels alarm when disabled`() = runTest {
        val saveUseCase = SaveReminderUseCase(fakeRepo, fakeScheduler)
        val disabledReminder = sampleReminder.copy(enabled = false)
        val result = saveUseCase(disabledReminder)

        assertEquals(AppResult.Success("rem-1"), result)
        assertTrue(fakeScheduler.cancelledList.contains("rem-1"))
    }

    @Test
    fun `deleteReminder cancels scheduled alarm on deletion`() = runTest {
        val deleteUseCase = DeleteReminderUseCase(fakeRepo, fakeScheduler)
        val result = deleteUseCase(sampleReminder)

        assertEquals(AppResult.Success(Unit), result)
        assertEquals(1, fakeRepo.deleteCalls)
        assertTrue(fakeScheduler.cancelledList.contains("rem-1"))
    }
}
