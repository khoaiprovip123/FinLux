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
        val statementReminderId = "debt_statement_reminder_${debt.id}"

        if (!debt.isReminderEnabled || debt.isSettled) {
            // Cancel and remove existing debt reminders if disabled or already settled
            scheduler.cancel(reminderId)
            reminderRepository.deleteReminder(stubReminder(reminderId))
            scheduler.cancel(statementReminderId)
            reminderRepository.deleteReminder(stubReminder(statementReminderId))
            return
        }

        val zone = ZoneId.systemDefault()
        val now = LocalDate.now(zone)

        // 1. Lập lịch nhắc hạn thanh toán (Due Date Reminder) - Chỉ áp dụng cho nợ định kỳ hàng tháng
        if (debt.isMonthlyRecurring && debt.dueDate != null && debt.dueDate in 1..31) {
            val dueDay = debt.dueDate
            val curMonthDueDay = minOf(dueDay, now.lengthOfMonth())
            val curRemindDate = now.withDayOfMonth(curMonthDueDay).minusDays(debt.reminderDaysBefore.toLong())
            val curTrigger = curRemindDate.atTime(LocalTime.of(9, 0)).atZone(zone).toInstant()

            val triggerInstant = if (curTrigger.isAfter(Instant.now())) {
                curTrigger
            } else {
                val nextMonth = now.plusMonths(1)
                val nextMonthDueDay = minOf(dueDay, nextMonth.lengthOfMonth())
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
        } else {
            scheduler.cancel(reminderId)
            reminderRepository.deleteReminder(stubReminder(reminderId))
        }

        // 2. Lập lịch nhắc ngày chốt sao kê cho Thẻ tín dụng (Statement Date Reminder)
        if (debt.type == com.finlux.app.domain.model.DebtType.CREDIT_CARD && debt.statementDate != null) {
            val stmtDay = debt.statementDate.coerceIn(1, 31)
            val curMonthStmtDay = minOf(stmtDay, now.lengthOfMonth())
            val curStmtDate = now.withDayOfMonth(curMonthStmtDay)
            val curStmtTrigger = curStmtDate.atTime(LocalTime.of(9, 0)).atZone(zone).toInstant()

            val stmtTriggerInstant = if (curStmtTrigger.isAfter(Instant.now())) {
                curStmtTrigger
            } else {
                val nextMonth = now.plusMonths(1)
                val nextMonthStmtDay = minOf(stmtDay, nextMonth.lengthOfMonth())
                val nextStmtDate = nextMonth.withDayOfMonth(nextMonthStmtDay)
                nextStmtDate.atTime(LocalTime.of(9, 0)).atZone(zone).toInstant()
            }

            val stmtReminder = Reminder(
                id = statementReminderId,
                title = "Chốt sao kê thẻ: ${debt.name}",
                amount = debt.remainingBalance,
                categoryId = "debt_payment",
                walletId = "",
                recurrence = ReminderRecurrence.MONTHLY,
                startDate = stmtTriggerInstant,
                enabled = true,
                nextTriggerDate = stmtTriggerInstant,
            )
            reminderRepository.upsertReminder(stmtReminder)
            scheduler.schedule(stmtReminder)
        } else {
            scheduler.cancel(statementReminderId)
            reminderRepository.deleteReminder(stubReminder(statementReminderId))
        }
    }

    suspend fun removeDebtReminder(debtId: String) {
        val reminderId = "debt_reminder_$debtId"
        val statementReminderId = "debt_statement_reminder_$debtId"
        scheduler.cancel(reminderId)
        reminderRepository.deleteReminder(stubReminder(reminderId))
        scheduler.cancel(statementReminderId)
        reminderRepository.deleteReminder(stubReminder(statementReminderId))
    }

    suspend fun schedulePaydayAllocationReminder(
        paydayDay: Int,
        walletName: String?,
        totalAmount: Long,
    ) {
        val reminderId = "payday_debt_allocation_reminder"
        val zone = ZoneId.systemDefault()
        val now = LocalDate.now(zone)

        val curMonthPayday = minOf(paydayDay.coerceIn(1, 31), now.lengthOfMonth())
        val curDate = now.withDayOfMonth(curMonthPayday)
        val curTrigger = curDate.atTime(LocalTime.of(9, 0)).atZone(zone).toInstant()

        val triggerInstant = if (curTrigger.isAfter(Instant.now())) {
            curTrigger
        } else {
            val nextMonth = now.plusMonths(1)
            val nextMonthPayday = minOf(paydayDay.coerceIn(1, 31), nextMonth.lengthOfMonth())
            val nextDate = nextMonth.withDayOfMonth(nextMonthPayday)
            nextDate.atTime(LocalTime.of(9, 0)).atZone(zone).toInstant()
        }

        val sourceWalletText = if (!walletName.isNullOrBlank()) " từ ví $walletName" else ""
        val reminder = Reminder(
            id = reminderId,
            title = "Trích lương trả nợ: Ngày $paydayDay$sourceWalletText",
            amount = Money(totalAmount),
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

