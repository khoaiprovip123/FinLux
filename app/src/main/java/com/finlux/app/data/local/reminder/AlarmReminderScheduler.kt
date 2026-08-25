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
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.model.ReminderRecurrence
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.AppNotification
import com.finlux.app.domain.repository.NotificationRepository
import com.finlux.app.domain.repository.ReminderRepository
import java.util.UUID
import com.finlux.app.domain.repository.ReminderScheduler
import com.finlux.app.domain.usecase.AddTransactionUseCase
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private const val ReminderChannelId = "finlux_reminders_v2"
private const val ACTION_TRIGGER = "com.finlux.app.ACTION_TRIGGER_REMINDER"
private const val ACTION_PAY = "com.finlux.app.ACTION_PAY_REMINDER"
private const val ACTION_SNOOZE = "com.finlux.app.ACTION_SNOOZE_REMINDER"
private const val ACTION_EDIT_PAYMENT = "com.finlux.app.ACTION_EDIT_PAYMENT"

object ReminderTriggerDeduplicator {
    private val lastTriggeredMap = java.util.concurrent.ConcurrentHashMap<String, Long>()

    fun shouldTrigger(reminderId: String, triggerTimeWindowMs: Long = 60_000L): Boolean {
        val now = System.currentTimeMillis()
        val last = lastTriggeredMap[reminderId] ?: 0L
        if (now - last < triggerTimeWindowMs) {
            return false
        }
        lastTriggeredMap[reminderId] = now
        return true
    }

    fun resetForTest() {
        lastTriggeredMap.clear()
    }
}

@Singleton
class AlarmReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : ReminderScheduler {
    override fun schedule(reminder: Reminder) {
        if (!reminder.enabled || reminder.id.isBlank()) return
        val manager = context.getSystemService(AlarmManager::class.java)
        val triggerAt = maxOf(reminder.nextTriggerDate.toEpochMilli(), System.currentTimeMillis() + 5_000)
        manager?.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, reminder.pendingIntent(context))
    }

    override fun cancel(reminderId: String) {
        if (reminderId.isBlank()) return
        val manager = context.getSystemService(AlarmManager::class.java)
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
        manager?.cancel(pending)
        pending.cancel()

        val notiManager = context.getSystemService(NotificationManager::class.java)
        notiManager?.cancel(reminderId.hashCode())
    }
}

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {
    @Inject
    lateinit var addTransactionUseCase: AddTransactionUseCase

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var reminderRepository: ReminderRepository

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra("id") ?: return
        val fallbackTitle = intent.getStringExtra("title") ?: "Nhắc nhở Finlux"
        val fallbackAmount = intent.getLongExtra("amount", 0L)
        val fallbackCategoryId = intent.getStringExtra("categoryId").orEmpty()
        val fallbackWalletId = intent.getStringExtra("walletId").orEmpty()
        val recurrenceName = intent.getStringExtra("recurrence")
        val fallbackRecurrence = recurrenceName?.let { runCatching { ReminderRecurrence.valueOf(it) }.getOrNull() } ?: ReminderRecurrence.MONTHLY
        val paymentActionId = intent.getStringExtra("paymentActionId")

        val notifications = context.getSystemService(NotificationManager::class.java)

        when (intent.action) {
            ACTION_PAY -> {
                notifications?.cancel(id.hashCode())
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (fallbackAmount > 0 && fallbackWalletId.isNotBlank()) {
                            val addResult = addTransactionUseCase(
                                FinanceTransaction(
                                    id = paymentActionId.orEmpty(),
                                    type = TransactionType.EXPENSE,
                                    amount = Money(fallbackAmount),
                                    categoryId = fallbackCategoryId.ifBlank { null },
                                    walletId = fallbackWalletId,
                                    note = "Thanh toán nhắc nhở: $fallbackTitle",
                                    date = Instant.now(),
                                )
                            )
                            if (addResult is AppResult.Success) {
                                notificationRepository.markAsPaidByReminderId(id)
                            }
                        } else {
                            notificationRepository.markAsPaidByReminderId(id)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            ACTION_SNOOZE -> {
                notifications?.cancel(id.hashCode())
                val snoozeTime = System.currentTimeMillis() + 60 * 60 * 1000L // 1 giờ sau
                val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
                    action = ACTION_TRIGGER
                    putExtra("id", id)
                    putExtra("title", fallbackTitle)
                    putExtra("amount", fallbackAmount)
                    putExtra("categoryId", fallbackCategoryId)
                    putExtra("walletId", fallbackWalletId)
                    putExtra("recurrence", fallbackRecurrence.name)
                }
                val pendingSnooze = PendingIntent.getBroadcast(
                    context,
                    id.hashCode(),
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                context.getSystemService(AlarmManager::class.java)?.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime,
                    pendingSnooze
                )
            }

            else -> {
                val notiPendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // 1. Validation Guard: Kiểm tra xem nhắc nhở có còn tồn tại và đang bật (enabled) trong Database không
                        val currentReminders = reminderRepository.observeReminders().firstOrNull().orEmpty()
                        val activeReminder = currentReminders.firstOrNull { it.id == id }

                        if (activeReminder == null || !activeReminder.enabled) {
                            // Nhắc nhở đã bị xóa hoặc tắt -> Dọn dẹp Alarm tiếp theo và KHÔNG tạo thông báo
                            val alarmManager = context.getSystemService(AlarmManager::class.java)
                            val cancelIntent = Intent(context, ReminderReceiver::class.java).apply {
                                action = ACTION_TRIGGER
                                putExtra("id", id)
                            }
                            val cancelPending = PendingIntent.getBroadcast(
                                context,
                                id.hashCode(),
                                cancelIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                            alarmManager?.cancel(cancelPending)
                            cancelPending.cancel()
                            notifications?.cancel(id.hashCode())
                            return@launch
                        }

                        // 2. Deduplication check: Tránh trigger lặp lại trong vòng 60 giây
                        if (!ReminderTriggerDeduplicator.shouldTrigger(id)) {
                            return@launch
                        }

                        // 3. Sử dụng thông tin chính xác mới nhất từ activeReminder
                        val effectiveTitle = activeReminder.title.ifBlank { fallbackTitle }
                        val effectiveAmount = activeReminder.amount.value
                        val effectiveCategoryId = activeReminder.categoryId
                        val effectiveWalletId = activeReminder.walletId
                        val effectiveRecurrence = activeReminder.recurrence

                        val channel = NotificationChannel(
                            ReminderChannelId,
                            "Nhắc nhở tài chính",
                            NotificationManager.IMPORTANCE_HIGH,
                        ).apply {
                            description = "Thông báo nhắc nhở thanh toán tài chính Finlux"
                            enableVibration(true)
                            enableLights(true)
                            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                        }
                        notifications?.createNotificationChannel(channel)

                        val body = if (effectiveAmount > 0) {
                            "Đến hạn thanh toán khoản ${NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")).format(effectiveAmount)}"
                        } else {
                            "Đến hạn xác nhận giao dịch"
                        }

                        // 4. Bỏ qua việc lưu thông báo vào Firestore (Cloud Function sẽ làm việc này)

                        // 5. Bắn thông báo Android System Notification
                        val currentEpochDay = Instant.now().toEpochMilli() / 86400000L
                        val generatedPaymentActionId = "pay_rem_${id}_$currentEpochDay"

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
                            putExtra("title", effectiveTitle)
                            putExtra("amount", effectiveAmount)
                            putExtra("categoryId", effectiveCategoryId)
                            putExtra("walletId", effectiveWalletId)
                            putExtra("paymentActionId", generatedPaymentActionId)
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
                            putExtra("title", effectiveTitle)
                            putExtra("amount", effectiveAmount)
                            putExtra("categoryId", effectiveCategoryId)
                            putExtra("walletId", effectiveWalletId)
                            putExtra("recurrence", effectiveRecurrence.name)
                        }
                        val snoozePending = PendingIntent.getBroadcast(
                            context,
                            (id + "_snooze").hashCode(),
                            snoozeIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )

                        val editIntent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            putExtra("destination", "notifications")
                            putExtra("pay_notification_id", id)
                            putExtra("reminder_id", id)
                        }
                        val editPending = PendingIntent.getActivity(
                            context,
                            (id + "_edit").hashCode(),
                            editIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                        )

                        notifications?.notify(
                            id.hashCode(),
                            NotificationCompat.Builder(context, ReminderChannelId)
                                .setSmallIcon(R.drawable.ic_finlux)
                                .setContentTitle(effectiveTitle)
                                .setContentText(body)
                                .setAutoCancel(true)
                                .setContentIntent(openApp)
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setDefaults(NotificationCompat.DEFAULT_ALL)
                                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .addAction(R.drawable.ic_finlux, "Đã thanh toán", payPending)
                                .addAction(R.drawable.ic_finlux, "Sửa số tiền", editPending)
                                .addAction(R.drawable.ic_finlux, "Nhắc lại 1h", snoozePending)
                                .build(),
                        )

                        // 6. Tính toán lịch báo thức kế tiếp cục bộ (việc ghi DB do Cloud Function đảm nhiệm)
                        val next = nextTrigger(Instant.now(), effectiveRecurrence)

                        val nextIntent = Intent(context, ReminderReceiver::class.java).apply {
                            action = ACTION_TRIGGER
                            putExtra("id", id)
                            putExtra("title", effectiveTitle)
                            putExtra("amount", effectiveAmount)
                            putExtra("categoryId", effectiveCategoryId)
                            putExtra("walletId", effectiveWalletId)
                            putExtra("recurrence", effectiveRecurrence.name)
                        }
                        val nextPending = PendingIntent.getBroadcast(
                            context,
                            id.hashCode(),
                            nextIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        context.getSystemService(AlarmManager::class.java)?.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            next.toEpochMilli(),
                            nextPending
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        notiPendingResult.finish()
                    }
                }
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
