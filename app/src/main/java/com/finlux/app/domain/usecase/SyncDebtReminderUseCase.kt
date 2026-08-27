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

        // Calculate reminder date in the current month:
        val curMonthDueDay = minOf(debt.dueDate, now.lengthOfMonth())
        val curRemindDate = now.withDayOfMonth(curMonthDueDay).minusDays(debt.reminderDaysBefore.toLong())
        val curTrigger = curRemindDate.atTime(LocalTime.of(9, 0)).atZone(zone).toInstant()

        val triggerInstant = if (curTrigger.isAfter(Instant.now())) {
            curTrigger
        } else {
            val nextMonth = now.plusMonths(1)
            val nextMonthDueDay = minOf(debt.dueDate, nextMonth.lengthOfMonth())
            val nextRemindDate = nextMonth.withDayOfMonth(nextMonthDueDay).minusDays(debt.reminderDaysBefore.toLong())
            nextRemindDate.atTime(LocalTime.of(9, 0)).atZone(zone).toInstant()
        }
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
