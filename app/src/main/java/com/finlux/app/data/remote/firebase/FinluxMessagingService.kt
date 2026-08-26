package com.finlux.app.data.remote.firebase

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.finlux.app.BuildConfig
import com.finlux.app.MainActivity
import com.finlux.app.R
import com.finlux.app.data.local.notification.CHANNEL_BUDGET_ALERTS
import com.finlux.app.data.local.notification.CHANNEL_REMINDERS
import com.finlux.app.data.local.notification.CHANNEL_SYSTEM
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/** Keeps the current device token attached to the authenticated FinLux user and handles foreground FCM messages. */
class FinluxMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        if (!BuildConfig.FIREBASE_CONFIGURED || token.isBlank()) return
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .set(mapOf("fcmTokens" to FieldValue.arrayUnion(token)), SetOptions.merge())
            .addOnFailureListener { error ->
                android.util.Log.w("FirebaseMessaging", "Không thể cập nhật FCM token", error)
            }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Thông báo FinLux"
        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: ""
        val destination = remoteMessage.data["destination"]
            ?: remoteMessage.data["targetRoute"]
            ?: "notifications"
        val reminderId = remoteMessage.data["reminderId"]
        val categoryId = remoteMessage.data["categoryId"]

        val channelId = when (destination) {
            "budget" -> CHANNEL_BUDGET_ALERTS
            "reminders" -> CHANNEL_REMINDERS
            else -> CHANNEL_SYSTEM
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("destination", destination)
            if (!reminderId.isNullOrBlank()) putExtra("reminder_id", reminderId)
            if (!categoryId.isNullOrBlank()) putExtra("categoryId", categoryId)
        }

        val notiId = (title + body).hashCode()
        val pendingIntent = PendingIntent.getActivity(
            this,
            notiId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, channelId)
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

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.notify(notiId, notification)
    }
}
