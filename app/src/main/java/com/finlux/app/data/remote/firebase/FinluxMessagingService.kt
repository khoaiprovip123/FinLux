package com.finlux.app.data.remote.firebase

import com.finlux.app.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService

/** Keeps the current device token attached to the authenticated FinLux user. */
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
}
