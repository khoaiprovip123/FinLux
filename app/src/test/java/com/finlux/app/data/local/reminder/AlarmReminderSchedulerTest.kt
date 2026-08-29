package com.finlux.app.data.local.reminder

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.model.ReminderRecurrence
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class AlarmReminderSchedulerTest {

    private val context: Context = mockk(relaxed = true)
    private val alarmManager: AlarmManager = mockk(relaxed = true)
    private val notificationManager: NotificationManager = mockk(relaxed = true)
    private val mockPendingIntent: PendingIntent = mockk(relaxed = true)
    private lateinit var scheduler: AlarmReminderScheduler

    @BeforeEach
    fun setUp() {
        mockkStatic(PendingIntent::class)
        every { PendingIntent.getActivity(any(), any(), any(), any()) } returns mockPendingIntent
        every { PendingIntent.getBroadcast(any(), any(), any(), any()) } returns mockPendingIntent
        every { context.getSystemService(AlarmManager::class.java) } returns alarmManager
        every { context.getSystemService(NotificationManager::class.java) } returns notificationManager
        scheduler = AlarmReminderScheduler(context)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(PendingIntent::class)
    }

    @Test
    fun `schedule does not invoke alarm manager if reminder is disabled`() {
        val reminder = Reminder(
            id = "rem_1",
            title = "Tiền điện",
            amount = Money(500_000L),
            categoryId = "cat_bill",
            walletId = "wal_cash",
            recurrence = ReminderRecurrence.MONTHLY,
            startDate = Instant.now(),
            enabled = false,
            nextTriggerDate = Instant.now().plusSeconds(3600),
        )

        scheduler.schedule(reminder)

        verify(exactly = 0) {
            alarmManager.setAlarmClock(any(), any())
        }
    }

    @Test
    fun `schedule invokes setAlarmClock when reminder is enabled`() {
        val triggerInstant = Instant.now().plusSeconds(7200)
        val reminder = Reminder(
            id = "rem_2",
            title = "Tiền nước",
            amount = Money(200_000L),
            categoryId = "cat_bill",
            walletId = "wal_cash",
            recurrence = ReminderRecurrence.MONTHLY,
            startDate = Instant.now(),
            enabled = true,
            nextTriggerDate = triggerInstant,
        )

        scheduler.schedule(reminder)

        verify(atLeast = 1) {
            alarmManager.setAlarmClock(any(), any())
        }
    }

    @Test
    fun `cancel invokes alarmManager cancel with broadcast intent`() {
        scheduler.cancel("rem_2")

        verify(atLeast = 1) {
            alarmManager.cancel(any<PendingIntent>())
        }
        verify(atLeast = 1) {
            notificationManager.cancel("rem_2".hashCode())
        }
    }

    @Test
    fun `schedule advances expired nextTriggerDate to future before setAlarmClock`() {
        val pastInstant = Instant.now().minusSeconds(86400 * 2)
        val reminder = Reminder(
            id = "rem_expired",
            title = "Ăn sáng",
            amount = Money(15_000L),
            categoryId = "cat_food",
            walletId = "wal_cash",
            recurrence = ReminderRecurrence.DAILY,
            startDate = pastInstant,
            enabled = true,
            nextTriggerDate = pastInstant,
        )

        scheduler.schedule(reminder)

        verify(atLeast = 1) {
            alarmManager.setAlarmClock(any(), any())
        }
    }
}
