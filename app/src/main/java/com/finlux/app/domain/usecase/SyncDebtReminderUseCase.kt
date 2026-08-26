package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.model.ReminderRecurrence
import com.finlux.app.domain.repository.ReminderRepository
import com.finlux.app.domain.repository.ReminderScheduler
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

/**
 * Automatically synchronizes Debt Due Date Reminders with the Reminder Scheduler and Repository,
 * ensuring AlarmManager triggers exact push notifications at 09:00 AM on the scheduled reminder day.
 */
class SyncDebtReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val scheduler: ReminderScheduler,
) {
    suspend fun syncDebt(debt: DebtAccount) {
        val reminderId = "debt_reminder_${debt.id}"

        if (!debt.isReminderEnabled || debt.isSettled) {
            // Cancel and remove existing debt reminder if disabled or already settled
            scheduler.cancel(reminderId)
            reminderRepository.deleteReminder(stubReminder(reminderId))
            return
        }

        val zone = ZoneId.systemDefault()
        val now = LocalDate.now(zone)
        val dueDay = debt.dueDate.coerceIn(1, 28) // Safe day cap
        val remindDay = (dueDay - debt.reminderDaysBefore).coerceAtLeast(1)

        // Calculate next trigger date at 09:00 AM
        var targetDate = now.withDayOfMonth(minOf(remindDay, now.lengthOfMonth()))
        var triggerDateTime = targetDate.atTime(LocalTime.of(9, 0))

        if (triggerDateTime.atZone(zone).toInstant().isBefore(Instant.now())) {
            targetDate = targetDate.plusMonths(1)
            targetDate = targetDate.withDayOfMonth(minOf(remindDay, targetDate.lengthOfMonth()))
            triggerDateTime = targetDate.atTime(LocalTime.of(9, 0))
        }

        val triggerInstant = triggerDateTime.atZone(zone).toInstant()
        val expectedAmount = if (debt.minimumPayment.value > 0L) debt.minimumPayment else debt.remainingBalance

        val reminder = Reminder(
            id = reminderId,
            title = "Thanh toán nợ: ${debt.name}",
            amount = expectedAmount,
            categoryId = "debt_payment",
            walletId = "",
            recurrence = ReminderRecurrence.MONTHLY,
            startDate = triggerInstant,
            enabled = true,
            nextTriggerDate = triggerInstant,
        )

        reminderRepository.upsertReminder(reminder)
        scheduler.schedule(reminder)
    }

    suspend fun removeDebtReminder(debtId: String) {
        val reminderId = "debt_reminder_$debtId"
        scheduler.cancel(reminderId)
        reminderRepository.deleteReminder(stubReminder(reminderId))
    }

    private fun stubReminder(reminderId: String) = Reminder(
        id = reminderId,
        title = "",
        amount = Money(0),
        categoryId = "",
        walletId = "",
        recurrence = ReminderRecurrence.MONTHLY,
        startDate = Instant.now(),
        enabled = false,
        nextTriggerDate = Instant.now(),
    )
}
