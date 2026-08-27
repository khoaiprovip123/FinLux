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

const val ACTION_SALARY_PAYDAY = "com.finlux.app.ACTION_SALARY_PAYDAY"
const val SALARY_PAYDAY_ALARM_REQUEST_CODE = 9925

@Singleton
class AlarmSalaryCycleScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val calculator: SalaryCycleCalculator,
    private val clock: FinanceClock,
) : SalaryCycleScheduler {

    override fun scheduleNextPayday(config: SalaryCycleConfig) {
        if (!config.enabled) {
            cancel()
            return
        }

        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        val triggerInstant = calculateNextPaydayTrigger(config, clock.now())
        val triggerAtMillis = maxOf(triggerInstant.toEpochMilli(), System.currentTimeMillis() + 5_000L)

        val intent = Intent(context, SalaryCycleReceiver::class.java).apply {
            action = ACTION_SALARY_PAYDAY
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            SALARY_PAYDAY_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    override fun cancel() {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, SalaryCycleReceiver::class.java).apply {
            action = ACTION_SALARY_PAYDAY
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            SALARY_PAYDAY_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        manager.cancel(pendingIntent)
        pendingIntent.cancel()
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
