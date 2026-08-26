package com.finlux.app.data.remote.firebase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.AppNotification
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.repository.NotificationRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.util.Date
import java.util.UUID

class FirebaseNotificationRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : NotificationRepository {

    override fun observeNotifications(): Flow<List<AppNotification>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("users").document(uid).collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) close(error)
                else trySend(snapshot?.documents.orEmpty().mapNotNull { it.toAppNotification() })
            }
        awaitClose { registration.remove() }
    }

    override suspend fun saveNotification(notification: AppNotification): AppResult<String> = firebaseResult("Không thể lưu thông báo") {
        val uid = requireUid()
        val id = notification.id.ifBlank { UUID.randomUUID().toString() }
        firestore.collection("users").document(uid).collection("notifications").document(id)
            .set(notification.copy(id = id).toNotificationMap()).await()
        id
    }

    override suspend fun markAsRead(id: String): AppResult<Unit> = firebaseResult("Không thể cập nhật thông báo") {
        val uid = requireUid()
        firestore.collection("users").document(uid).collection("notifications").document(id)
            .update("isRead", true).await()
        Unit
    }

    override suspend fun markAsPaid(id: String): AppResult<Unit> = firebaseResult("Không thể cập nhật thông báo") {
        val uid = requireUid()
        firestore.collection("users").document(uid).collection("notifications").document(id)
            .update(mapOf("isRead" to true, "isPaid" to true)).await()
        Unit
    }

    override suspend fun markAsPaidWithAmount(
        id: String,
        amount: Money,
        newBody: String?,
    ): AppResult<Unit> = firebaseResult("Không thể cập nhật thông báo") {
        val uid = requireUid()
        val updates = mutableMapOf<String, Any>(
            "isRead" to true,
            "isPaid" to true,
            "amount" to amount.value,
        )
        if (!newBody.isNullOrBlank()) {
            updates["body"] = newBody
        }
        firestore.collection("users").document(uid).collection("notifications").document(id)
            .update(updates).await()
        Unit
    }

    override suspend fun markAsPaidByReminderId(reminderId: String): AppResult<Unit> = firebaseResult("Không thể cập nhật thông báo") {
        val uid = requireUid()
        val snapshot = firestore.collection("users").document(uid).collection("notifications")
            .whereEqualTo("reminderId", reminderId)
            .get().await()
        if (snapshot.documents.isNotEmpty()) {
            firestore.runBatch { batch ->
                snapshot.documents.forEach { doc ->
                    batch.update(doc.reference, mapOf("isRead" to true, "isPaid" to true))
                }
            }.await()
        }
        Unit
    }

    override suspend fun deleteNotification(id: String): AppResult<Unit> = firebaseResult("Không thể xóa thông báo") {
        val uid = requireUid()
        firestore.collection("users").document(uid).collection("notifications").document(id).delete().await()
        Unit
    }

    override suspend fun clearAll(): AppResult<Unit> = firebaseResult("Không thể xóa thông báo") {
        val uid = requireUid()
        val snapshot = firestore.collection("users").document(uid).collection("notifications").get().await()
        firestore.runBatch { batch ->
            snapshot.documents.forEach { doc -> batch.delete(doc.reference) }
        }.await()
        Unit
    }

    private fun requireUid(): String = auth.currentUser?.uid ?: error("Phiên đăng nhập đã hết hạn")
}

internal fun AppNotification.toNotificationMap(): Map<String, Any?> = mapOf(
    "title" to title,
    "body" to body,
    "type" to type.name.lowercase(),
    "amount" to amount.value,
    "reminderId" to reminderId,
    "categoryId" to categoryId,
    "walletId" to walletId,
    "targetRoute" to targetRoute,
    "targetId" to targetId,
    "actionUrl" to actionUrl,
    "timestamp" to Timestamp(Date.from(timestamp)),
    "isRead" to isRead,
    "isPaid" to isPaid,
)

internal fun DocumentSnapshot.toAppNotification(): AppNotification? = runCatching {
    AppNotification(
        id = id,
        title = requireNotNull(getString("title")),
        body = getString("body").orEmpty(),
        type = getString("type")?.let { raw ->
            com.finlux.app.domain.model.NotificationType.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
        } ?: com.finlux.app.domain.model.NotificationType.REMINDER,
        amount = Money(getLong("amount") ?: 0L),
        reminderId = getString("reminderId"),
        categoryId = getString("categoryId"),
        walletId = getString("walletId"),
        targetRoute = getString("targetRoute"),
        targetId = getString("targetId"),
        actionUrl = getString("actionUrl"),
        timestamp = getTimestamp("timestamp")?.toDate()?.toInstant() ?: Instant.now(),
        isRead = getBoolean("isRead") ?: false,
        isPaid = getBoolean("isPaid") ?: false,
    )
}.getOrNull()
