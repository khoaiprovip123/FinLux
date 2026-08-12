package com.finlux.app.data.local.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.finlux.app.MainActivity
import com.finlux.app.R
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.model.ReminderRecurrence
import com.finlux.app.domain.repository.ReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

private const val ReminderChannelId = "finlux_reminders"

@Singleton
class AlarmReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : ReminderScheduler {
    override fun schedule(reminder: Reminder) {
        if (!reminder.enabled || reminder.id.isBlank()) return
        val manager = context.getSystemService(AlarmManager::class.java)
        val triggerAt = maxOf(reminder.nextTriggerDate.toEpochMilli(), System.currentTimeMillis() + 5_000)
        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, reminder.pendingIntent(context))
    }

    override fun cancel(reminderId: String) {
        if (reminderId.isBlank()) return
        val intent = Intent(context, ReminderReceiver::class.java).putExtra("id", reminderId)
        val pending = PendingIntent.getBroadcast(context, reminderId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        context.getSystemService(AlarmManager::class.java).cancel(pending)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra("id") ?: return
        val title = intent.getStringExtra("title") ?: "Nhắc nhở Finlux"
        val amount = intent.getLongExtra("amount", 0L)
        val recurrence = intent.getStringExtra("recurrence")
            ?.let { runCatching { ReminderRecurrence.valueOf(it) }.getOrNull() }
            ?: ReminderRecurrence.MONTHLY
        val notifications = context.getSystemService(NotificationManager::class.java)
        notifications.createNotificationChannel(
            NotificationChannel(ReminderChannelId, "Nhắc nhở tài chính", NotificationManager.IMPORTANCE_HIGH),
        )
        val openApp = PendingIntent.getActivity(
            context, id.hashCode(), Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val body = if (amount > 0) "Đến hạn xác nhận khoản ${java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("vi-VN")).format(amount)}" else "Đến hạn xác nhận giao dịch"
        notifications.notify(
            id.hashCode(),
            NotificationCompat.Builder(context, ReminderChannelId)
                .setSmallIcon(R.drawable.ic_finlux)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(openApp)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build(),
        )
        val next = nextTrigger(Instant.now(), recurrence)
        val nextIntent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("id", id); putExtra("title", title); putExtra("amount", amount); putExtra("recurrence", recurrence.name)
        }
        val nextPending = PendingIntent.getBroadcast(context, id.hashCode(), nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        context.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.toEpochMilli(), nextPending)
    }
}

private fun Reminder.pendingIntent(context: Context): PendingIntent {
    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("id", id); putExtra("title", title); putExtra("amount", amount.value); putExtra("recurrence", recurrence.name)
    }
    return PendingIntent.getBroadcast(context, id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

private fun nextTrigger(from: Instant, recurrence: ReminderRecurrence): Instant {
    val dateTime = from.atZone(ZoneId.systemDefault())
    return when (recurrence) {
        ReminderRecurrence.DAILY -> dateTime.plusDays(1)
        ReminderRecurrence.WEEKLY -> dateTime.plusWeeks(1)
        ReminderRecurrence.MONTHLY -> dateTime.plusMonths(1)
    }.toInstant()
}
