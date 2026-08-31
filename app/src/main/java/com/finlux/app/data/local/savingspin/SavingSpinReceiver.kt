package com.finlux.app.data.local.savingspin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.finlux.app.R
import com.finlux.app.domain.repository.SavingSpinScheduler
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

@AndroidEntryPoint
class SavingSpinReceiver : BroadcastReceiver() {
    @Inject lateinit var scheduler: SavingSpinScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_SAVING_SPIN_SNOOZE) {
            context.getSystemService(NotificationManager::class.java)?.cancel(NOTIFICATION_ID)
            scheduler.snooze(Instant.now().plusSeconds(SNOOZE_SECONDS))
            return
        }
        if (intent.action != ACTION_SAVING_SPIN_REMINDER || !canNotify(context)) return

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(NotificationChannel(
            CHANNEL_ID,
            "Vòng quay tiết kiệm",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply { description = "Nhắc bạn hoàn thành lượt tiết kiệm đã lên lịch" })

        val openIntent = PendingIntent.getActivity(
            context,
            SAVING_SPIN_ALARM_REQUEST_CODE,
            AlarmSavingSpinScheduler.openGameIntent(context),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val snoozeIntent = PendingIntent.getBroadcast(
            context,
            SAVING_SPIN_ALARM_REQUEST_CODE + 1,
            Intent(context, SavingSpinReceiver::class.java).apply { action = ACTION_SAVING_SPIN_SNOOZE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        manager.notify(
            NOTIFICATION_ID,
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_finlux)
                .setContentTitle("Đến lượt tiết kiệm hôm nay")
                .setContentText("Quay một lần, chọn nơi cất tiền và duy trì chuỗi của bạn.")
                .setContentIntent(openIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(R.drawable.ic_finlux, "Nhắc sau 30 phút", snoozeIntent)
                .build(),
        )
        recordNotification(context)
    }

    private fun canNotify(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val epochDay = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
        return prefs.getInt("count_$epochDay", 0) < MAX_NOTIFICATIONS_PER_DAY
    }

    private fun recordNotification(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val epochDay = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
        val key = "count_$epochDay"
        prefs.edit().putInt(key, prefs.getInt(key, 0) + 1).apply()
    }

    companion object {
        private const val CHANNEL_ID = "finlux_saving_spin_v1"
        private const val PREFS = "saving_spin_notifications"
        private const val NOTIFICATION_ID = 73_092
        private const val MAX_NOTIFICATIONS_PER_DAY = 3
        private const val SNOOZE_SECONDS = 30L * 60L
    }
}
