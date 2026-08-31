package com.finlux.app.data.local.savingspin

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.finlux.app.MainActivity
import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.repository.SavingSpinScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

const val ACTION_SAVING_SPIN_REMINDER = "com.finlux.app.ACTION_SAVING_SPIN_REMINDER"
const val ACTION_SAVING_SPIN_SNOOZE = "com.finlux.app.ACTION_SAVING_SPIN_SNOOZE"
const val EXTRA_OPEN_SAVING_SPIN = "open_saving_spin"
const val SAVING_SPIN_ALARM_REQUEST_CODE = 73_091

@Singleton
class AlarmSavingSpinScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : SavingSpinScheduler {
    override fun schedule(config: SavingSpinConfig, nextTrigger: Instant) {
        if (!config.enabled || !config.reminderEnabled) {
            cancel()
            return
        }
        setExact(nextTrigger)
    }

    override fun cancel() {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        val pending = reminderPendingIntent()
        manager.cancel(pending)
        pending.cancel()
    }

    override fun snooze(until: Instant) = setExact(until)

    private fun setExact(trigger: Instant) {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        val triggerAt = maxOf(trigger.toEpochMilli(), System.currentTimeMillis() + MIN_TRIGGER_DELAY_MS)
        val pending = reminderPendingIntent()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            } else {
                manager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            }
        } catch (_: SecurityException) {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    private fun reminderPendingIntent(): PendingIntent = PendingIntent.getBroadcast(
        context,
        SAVING_SPIN_ALARM_REQUEST_CODE,
        Intent(context, SavingSpinReceiver::class.java).apply { action = ACTION_SAVING_SPIN_REMINDER },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    companion object {
        private const val MIN_TRIGGER_DELAY_MS = 5_000L

        fun openGameIntent(context: Context): Intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_OPEN_SAVING_SPIN, true)
            putExtra("destination", "saving-spin")
        }
    }
}
