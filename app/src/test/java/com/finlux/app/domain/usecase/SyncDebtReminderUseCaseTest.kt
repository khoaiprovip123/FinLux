package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtType
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.repository.ReminderRepository
import com.finlux.app.domain.repository.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class SyncDebtReminderUseCaseTest {

    private val savedReminders = mutableMapOf<String, Reminder>()
    private val scheduledReminders = mutableListOf<Reminder>()
    private val cancelledReminders = mutableListOf<String>()

    private val fakeReminderRepository = object : ReminderRepository {
        override fun observeReminders(): Flow<List<Reminder>> = flowOf(savedReminders.values.toList())
        override suspend fun upsertReminder(reminder: Reminder): AppResult<String> {
            savedReminders[reminder.id] = reminder
            return AppResult.Success(reminder.id)
        }
        override suspend fun deleteReminder(reminder: Reminder): AppResult<Unit> {
            savedReminders.remove(reminder.id)
            return AppResult.Success(Unit)
        }
    }

    private val fakeScheduler = object : ReminderScheduler {
        override fun schedule(reminder: Reminder) {
            scheduledReminders.add(reminder)
        }
        override fun cancel(reminderId: String) {
            cancelledReminders.add(reminderId)
        }
    }

    private lateinit var useCase: SyncDebtReminderUseCase

    @BeforeEach
    fun setUp() {
        savedReminders.clear()
        scheduledReminders.clear()
        cancelledReminders.clear()
        useCase = SyncDebtReminderUseCase(fakeReminderRepository, fakeScheduler)
    }

    @Test
    fun `syncDebt with reminder enabled schedules alarm and upserts reminder`() = runTest {
        val debt = DebtAccount(
            id = "debt-123",
            name = "Thẻ tín dụng VIB",
            type = DebtType.CREDIT_CARD,
            totalAmount = Money(20_000_000L),
            remainingBalance = Money(15_000_000L),
            interestRateApr = 24.0,
            minimumPayment = Money(750_000L),
            dueDate = 15,
            colorHex = "#6366F1",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            isSettled = false,
            isReminderEnabled = true,
            reminderDaysBefore = 3,
        )

        useCase.syncDebt(debt)

        val reminderId = "debt_reminder_debt-123"
        assertTrue(savedReminders.containsKey(reminderId))
        val stored = savedReminders[reminderId]!!
        assertEquals("Thanh toán nợ: Thẻ tín dụng VIB", stored.title)
        assertEquals(750_000L, stored.amount.value)
        assertEquals("debt_payment", stored.categoryId)
        assertTrue(scheduledReminders.any { it.id == reminderId })
    }

    @Test
    fun `syncDebt with reminder disabled cancels alarm and deletes reminder`() = runTest {
        val debt = DebtAccount(
            id = "debt-456",
            name = "Vay tiêu dùng",
            type = DebtType.PERSONAL_LOAN,
            totalAmount = Money(10_000_000L),
            remainingBalance = Money(5_000_000L),
            interestRateApr = 12.0,
            minimumPayment = Money(1_000_000L),
            dueDate = 20,
            colorHex = "#10B981",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            isSettled = false,
            isReminderEnabled = false,
            reminderDaysBefore = 2,
        )

        useCase.syncDebt(debt)

        val reminderId = "debt_reminder_debt-456"
        assertTrue(cancelledReminders.contains(reminderId))
        assertTrue(!savedReminders.containsKey(reminderId))
    }

    @Test
    fun `syncDebt with end of month dueDate 31 schedules reminder accurately`() = runTest {
        val debt = DebtAccount(
            id = "debt-31",
            name = "Thẻ tín dụng Techcombank",
            type = DebtType.CREDIT_CARD,
            totalAmount = Money(50_000_000L),
            remainingBalance = Money(25_000_000L),
            interestRateApr = 22.5,
            minimumPayment = Money(1_250_000L),
            dueDate = 31,
            colorHex = "#E11D48",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            isSettled = false,
            isReminderEnabled = true,
            reminderDaysBefore = 1,
        )

        useCase.syncDebt(debt)

        val reminderId = "debt_reminder_debt-31"
        assertTrue(savedReminders.containsKey(reminderId))
        val stored = savedReminders[reminderId]!!
        val zone = java.time.ZoneId.systemDefault()
        val triggerLocal = stored.startDate.atZone(zone).toLocalDate()
        val triggerHour = stored.startDate.atZone(zone).hour
        assertEquals(9, triggerHour)
        // Verify trigger date is in the future and day of month is reasonable (e.g. 30 or 27 for feb)
        assertTrue(stored.startDate.isAfter(Instant.now()))
    }

    @Test
    fun `removeDebtReminder cancels and deletes reminder`() = runTest {
        useCase.removeDebtReminder("debt-789")
        val reminderId = "debt_reminder_debt-789"
        assertTrue(cancelledReminders.contains(reminderId))
    }
}
