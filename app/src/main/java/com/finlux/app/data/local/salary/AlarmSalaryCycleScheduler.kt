package com.finlux.app.data.local.salary

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.finlux.app.core.time.FinanceClock
import com.finlux.app.core.time.FinanceTime
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.repository.SalaryCycleScheduler
import com.finlux.app.domain.usecase.SalaryCycleCalculator
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

import com.finlux.app.domain.model.SalaryScheduleType
import java.time.ZoneId

const val ACTION_SALARY_PAYDAY = "com.finlux.app.ACTION_SALARY_PAYDAY"
const val SALARY_PAYDAY_ALARM_REQUEST_CODE = 9925
const val SALARY_PAYDAY_ALARM_REQUEST_CODE_SECOND = 9926

@Singleton
class AlarmSalaryCycleScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val calculator: SalaryCycleCalculator,
    private val clock: FinanceClock,
) : SalaryCycleScheduler {

    override fun scheduleNextPayday(config: SalaryCycleConfig) {
        if (!config.enabled) {
            cancel()
            return
        }

        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        val now = clock.now()
        val zone = FinanceTime.zoneOf(config.financeTimeZone)

        // 1. Đặt mốc đợt 1
        val triggerInstant1 = calculateTriggerForDay(config.paydayDay, now, zone)
        scheduleAlarmForCode(manager, triggerInstant1, SALARY_PAYDAY_ALARM_REQUEST_CODE, config.paydayDay)

        // 2. Đặt mốc đợt 2 (nếu SEMI_MONTHLY)
        val secondDay = config.secondPaydayDay
        if (config.scheduleType == SalaryScheduleType.SEMI_MONTHLY && secondDay != null) {
            val triggerInstant2 = calculateTriggerForDay(secondDay, now, zone)
            scheduleAlarmForCode(manager, triggerInstant2, SALARY_PAYDAY_ALARM_REQUEST_CODE_SECOND, secondDay)
        } else {
            cancelAlarmForCode(manager, SALARY_PAYDAY_ALARM_REQUEST_CODE_SECOND)
        }
    }

    override fun cancel() {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        cancelAlarmForCode(manager, SALARY_PAYDAY_ALARM_REQUEST_CODE)
        cancelAlarmForCode(manager, SALARY_PAYDAY_ALARM_REQUEST_CODE_SECOND)
    }

    private fun scheduleAlarmForCode(manager: AlarmManager, triggerInstant: Instant, requestCode: Int, paydayDay: Int) {
        val triggerAtMillis = maxOf(triggerInstant.toEpochMilli(), System.currentTimeMillis() + 5_000L)
        val intent = Intent(context, SalaryCycleReceiver::class.java).apply {
            action = ACTION_SALARY_PAYDAY
            putExtra("paydayDay", paydayDay)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    private fun cancelAlarmForCode(manager: AlarmManager, requestCode: Int) {
        val intent = Intent(context, SalaryCycleReceiver::class.java).apply {
            action = ACTION_SALARY_PAYDAY
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        manager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    internal fun calculateTriggerForDay(dayOfMonth: Int, now: Instant, zone: ZoneId): Instant {
        val today = now.atZone(zone).toLocalDate()
        val candidateDate = today.withDayOfMonth(minOf(dayOfMonth.coerceIn(1, 31), today.lengthOfMonth()))
        val candidateInstant = candidateDate.atTime(9, 0, 0).atZone(zone).toInstant()

        return if (candidateInstant.isAfter(now)) {
            candidateInstant
        } else {
            val nextMonth = today.plusMonths(1)
            val nextDate = nextMonth.withDayOfMonth(minOf(dayOfMonth.coerceIn(1, 31), nextMonth.lengthOfMonth()))
            nextDate.atTime(9, 0, 0).atZone(zone).toInstant()
        }
    }

    internal fun calculateNextPaydayTrigger(config: SalaryCycleConfig, now: Instant): Instant {
        val zone = FinanceTime.zoneOf(config.financeTimeZone)
        val currentCycle = calculator.cycleContaining(now, config, zone)
        val nextPaydayDate = currentCycle.endExclusive.atZone(zone).toLocalDate()
        val triggerInstant = nextPaydayDate.atTime(9, 0, 0).atZone(zone).toInstant()

        return if (triggerInstant.isAfter(now)) {
            triggerInstant
        } else {
            val nextCycle = calculator.cycleContaining(currentCycle.endExclusive.plusMillis(1), config, zone)
            nextCycle.endExclusive.atZone(zone).toLocalDate().atTime(9, 0, 0).atZone(zone).toInstant()
        }
    }
}
