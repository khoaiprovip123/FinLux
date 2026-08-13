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
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.model.ReminderRecurrence
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.AppNotification
import com.finlux.app.domain.repository.NotificationRepository
import java.util.UUID
import com.finlux.app.domain.repository.ReminderScheduler
import com.finlux.app.domain.usecase.AddTransactionUseCase
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private const val ReminderChannelId = "finlux_reminders"
private const val ACTION_TRIGGER = "com.finlux.app.ACTION_TRIGGER_REMINDER"
private const val ACTION_PAY = "com.finlux.app.ACTION_PAY_REMINDER"
private const val ACTION_SNOOZE = "com.finlux.app.ACTION_SNOOZE_REMINDER"

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
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_TRIGGER
            putExtra("id", reminderId)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        context.getSystemService(AlarmManager::class.java).cancel(pending)
    }
}

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {
    @Inject
    lateinit var addTransactionUseCase: AddTransactionUseCase

    @Inject
    lateinit var notificationRepository: NotificationRepository

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra("id") ?: return
        val title = intent.getStringExtra("title") ?: "Nhắc nhở Finlux"
        val amount = intent.getLongExtra("amount", 0L)
        val categoryId = intent.getStringExtra("categoryId").orEmpty()
        val walletId = intent.getStringExtra("walletId").orEmpty()
        val recurrenceName = intent.getStringExtra("recurrence")
        val recurrence = recurrenceName?.let { runCatching { ReminderRecurrence.valueOf(it) }.getOrNull() } ?: ReminderRecurrence.MONTHLY

        val notifications = context.getSystemService(NotificationManager::class.java)

        when (intent.action) {
            ACTION_PAY -> {
                notifications.cancel(id.hashCode())
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (amount > 0 && walletId.isNotBlank()) {
                            addTransactionUseCase(
                                FinanceTransaction(
                                    type = TransactionType.EXPENSE,
                                    amount = Money(amount),
                                    categoryId = categoryId.ifBlank { null },
                                    walletId = walletId,
                                    note = "Thanh toán nhắc nhở: $title",
                                    date = Instant.now(),
                                )
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            ACTION_SNOOZE -> {
                notifications.cancel(id.hashCode())
                val snoozeTime = System.currentTimeMillis() + 60 * 60 * 1000L // 1 giờ sau
                val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
                    action = ACTION_TRIGGER
                    putExtra("id", id)
                    putExtra("title", title)
                    putExtra("amount", amount)
                    putExtra("categoryId", categoryId)
                    putExtra("walletId", walletId)
                    putExtra("recurrence", recurrence.name)
                }
                val pendingSnooze = PendingIntent.getBroadcast(
                    context,
                    id.hashCode(),
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                context.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime,
                    pendingSnooze
                )
            }

            else -> {
                notifications.createNotificationChannel(
                    NotificationChannel(ReminderChannelId, "Nhắc nhở tài chính", NotificationManager.IMPORTANCE_HIGH),
                )
                val body = if (amount > 0) {
                    "Đến hạn thanh toán khoản ${NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")).format(amount)}"
                } else {
                    "Đến hạn xác nhận giao dịch"
                }

                val notiPendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        notificationRepository.saveNotification(
                            AppNotification(
                                id = UUID.randomUUID().toString(),
                                title = title,
                                body = body,
                                amount = Money(amount),
                                reminderId = id,
                                categoryId = categoryId.ifBlank { null },
                                walletId = walletId.ifBlank { null },
                                timestamp = Instant.now(),
                                isRead = false,
                                isPaid = false,
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        notiPendingResult.finish()
                    }
                }

                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("destination", "notifications")
                    putExtra("reminder_id", id)
                }
                val openApp = PendingIntent.getActivity(
                    context,
                    id.hashCode(),
                    openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

                val payIntent = Intent(context, ReminderReceiver::class.java).apply {
                    action = ACTION_PAY
                    putExtra("id", id)
                    putExtra("title", title)
                    putExtra("amount", amount)
                    putExtra("categoryId", categoryId)
                    putExtra("walletId", walletId)
                }
                val payPending = PendingIntent.getBroadcast(
                    context,
                    (id + "_pay").hashCode(),
                    payIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
                    action = ACTION_SNOOZE
                    putExtra("id", id)
                    putExtra("title", title)
                    putExtra("amount", amount)
                    putExtra("categoryId", categoryId)
                    putExtra("walletId", walletId)
                    putExtra("recurrence", recurrence.name)
                }
                val snoozePending = PendingIntent.getBroadcast(
                    context,
                    (id + "_snooze").hashCode(),
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                notifications.notify(
                    id.hashCode(),
                    NotificationCompat.Builder(context, ReminderChannelId)
                        .setSmallIcon(R.drawable.ic_finlux)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setContentIntent(openApp)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .addAction(R.drawable.ic_finlux, "Đã thanh toán", payPending)
                        .addAction(R.drawable.ic_finlux, "Nhắc lại sau 1h", snoozePending)
                        .build(),
                )

                val next = nextTrigger(Instant.now(), recurrence)
                val nextIntent = Intent(context, ReminderReceiver::class.java).apply {
                    action = ACTION_TRIGGER
                    putExtra("id", id)
                    putExtra("title", title)
                    putExtra("amount", amount)
                    putExtra("categoryId", categoryId)
                    putExtra("walletId", walletId)
                    putExtra("recurrence", recurrence.name)
                }
                val nextPending = PendingIntent.getBroadcast(
                    context,
                    id.hashCode(),
                    nextIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                context.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    next.toEpochMilli(),
                    nextPending
                )
            }
        }
    }
}

private fun Reminder.pendingIntent(context: Context): PendingIntent {
    val intent = Intent(context, ReminderReceiver::class.java).apply {
        action = ACTION_TRIGGER
        putExtra("id", id)
        putExtra("title", title)
        putExtra("amount", amount.value)
        putExtra("categoryId", categoryId)
        putExtra("walletId", walletId)
        putExtra("recurrence", recurrence.name)
    }
    return PendingIntent.getBroadcast(
        context,
        id.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

private fun nextTrigger(from: Instant, recurrence: ReminderRecurrence): Instant {
    val dateTime = from.atZone(ZoneId.systemDefault())
    return when (recurrence) {
        ReminderRecurrence.DAILY -> dateTime.plusDays(1)
        ReminderRecurrence.WEEKLY -> dateTime.plusWeeks(1)
        ReminderRecurrence.MONTHLY -> dateTime.plusMonths(1)
    }.toInstant()
}
