package com.finlux.app.data.remote.firebase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.model.ReminderRecurrence
import com.finlux.app.domain.repository.ReminderRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

class FirebaseReminderRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : ReminderRepository {

    override fun observeReminders(): Flow<List<Reminder>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("users").document(uid).collection("reminders")
            .addSnapshotListener { snapshot, error ->
                if (error != null) close(error)
                else trySend(snapshot?.documents.orEmpty().mapNotNull { it.toReminder() })
            }
        awaitClose { registration.remove() }
    }

    override suspend fun upsertReminder(reminder: Reminder): AppResult<String> = firebaseResult("Không thể lưu nhắc nhở") {
        val uid = requireUid()
        val id = reminder.id.ifBlank { UUID.randomUUID().toString() }
        firestore.collection("users").document(uid).collection("reminders").document(id)
            .set(reminder.copy(id = id).toReminderMap()).await()
        id
    }

    override suspend fun deleteReminder(reminder: Reminder): AppResult<Unit> = firebaseResult("Không thể xóa nhắc nhở") {
        val uid = requireUid()
        firestore.collection("users").document(uid).collection("reminders").document(reminder.id).delete().await()
        Unit
    }

    private fun requireUid(): String = auth.currentUser?.uid ?: error("Phiên đăng nhập đã hết hạn")
}

internal fun Reminder.toReminderMap(): Map<String, Any?> = mapOf(
    "title" to title,
    "amount" to amount.value,
    "categoryId" to categoryId,
    "walletId" to walletId,
    "recurrence" to recurrence.name.lowercase(),
    "startDate" to Timestamp(Date.from(startDate)),
    "enabled" to enabled,
    "nextTriggerDate" to Timestamp(Date.from(nextTriggerDate)),
)

internal fun DocumentSnapshot.toReminder(): Reminder? = runCatching {
    Reminder(
        id = id,
        title = requireNotNull(getString("title")),
        amount = Money(getLong("amount") ?: 0L),
        categoryId = requireNotNull(getString("categoryId")),
        walletId = requireNotNull(getString("walletId")),
        recurrence = ReminderRecurrence.valueOf(requireNotNull(getString("recurrence")).uppercase()),
        startDate = requireNotNull(getTimestamp("startDate")).toDate().toInstant(),
        enabled = getBoolean("enabled") ?: true,
        nextTriggerDate = requireNotNull(getTimestamp("nextTriggerDate")).toDate().toInstant(),
    )
}.getOrNull()
