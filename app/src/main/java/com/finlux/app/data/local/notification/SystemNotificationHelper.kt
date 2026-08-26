package com.finlux.app.data.local.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.finlux.app.MainActivity
import com.finlux.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

const val CHANNEL_REMINDERS = "finlux_reminders_v2"
const val CHANNEL_BUDGET_ALERTS = "finlux_budget_alerts"
const val CHANNEL_SYSTEM = "finlux_system_notifications"

@Singleton
class SystemNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val notificationManager: NotificationManager? =
        context.getSystemService(NotificationManager::class.java)

    init {
        createChannels()
    }

    private fun createChannels() {
        val remindersChannel = NotificationChannel(
            CHANNEL_REMINDERS,
            "Nhắc nhở tài chính",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Thông báo nhắc nhở thanh toán tài chính Finlux"
            enableVibration(true)
            enableLights(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }

        val budgetChannel = NotificationChannel(
            CHANNEL_BUDGET_ALERTS,
            "Cảnh báo ngân sách",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Cảnh báo khi chi tiêu chạm hoặc vượt hạn mức ngân sách"
            enableVibration(true)
            enableLights(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }

        val systemChannel = NotificationChannel(
            CHANNEL_SYSTEM,
            "Thông báo Finlux",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Thông báo hệ thống và tin tức từ Finlux"
            enableVibration(true)
            enableLights(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }

        notificationManager?.createNotificationChannel(remindersChannel)
        notificationManager?.createNotificationChannel(budgetChannel)
        notificationManager?.createNotificationChannel(systemChannel)
    }

    fun postBudgetAlertNotification(
        categoryId: String,
        categoryName: String,
        spentAmount: Long,
        limitAmount: Long,
        isExceeded: Boolean,
    ) {
        val percent = if (limitAmount > 0) (spentAmount * 100) / limitAmount else 100
        val title = if (isExceeded) "⚠️ Đã vượt ngân sách!" else "⚡ Sắp chạm hạn mức ngân sách"
        val body = if (isExceeded) {
            "Danh mục [$categoryName] đã chi ${formatVnd(spentAmount)} trên hạn mức ${formatVnd(limitAmount)} ($percent%)."
        } else {
            "Danh mục [$categoryName] đã sử dụng $percent% ngân sách (${formatVnd(spentAmount)} / ${formatVnd(limitAmount)})."
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("destination", "budget")
            putExtra("categoryId", categoryId)
        }
        val notiId = ("budget_${categoryId}_${if (isExceeded) 100 else 80}").hashCode()
        val pendingIntent = PendingIntent.getActivity(
            context,
            notiId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_BUDGET_ALERTS)
            .setSmallIcon(R.drawable.ic_finlux)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        notificationManager?.notify(notiId, notification)
    }

    fun postGeneralNotification(
        title: String,
        body: String,
        targetRoute: String? = null,
        notificationId: Int = title.hashCode(),
    ) {
        val channelId = when (targetRoute) {
            "budget" -> CHANNEL_BUDGET_ALERTS
            "reminders" -> CHANNEL_REMINDERS
            else -> CHANNEL_SYSTEM
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (!targetRoute.isNullOrBlank()) {
                putExtra("destination", targetRoute)
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_finlux)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        notificationManager?.notify(notificationId, notification)
    }

    private fun formatVnd(amount: Long): String =
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")).format(amount)
}
