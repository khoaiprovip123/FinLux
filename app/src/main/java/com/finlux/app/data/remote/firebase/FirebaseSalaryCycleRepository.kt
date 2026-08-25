package com.finlux.app.data.remote.firebase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseSalaryCycleRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : SalaryCycleRepository {
    override fun observeConfig(): Flow<SalaryCycleConfig> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(SalaryCycleConfig())
            close()
            return@callbackFlow
        }

        val document = firestore.collection("users").document(uid)
            .collection("financialPreferences").document("salaryCycle")
        val registration = document.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
            } else {
                trySend(
                    if (snapshot?.exists() == true) {
                        SalaryCycleFirestoreMapper.fromMap(snapshot.data)
                    } else {
                        SalaryCycleConfig()
                    }
                )
            }
        }
        awaitClose { registration.remove() }
    }

    override suspend fun saveConfig(config: SalaryCycleConfig): AppResult<Unit> =
        firebaseResult("Không thể lưu cấu hình kỳ lương") {
            val uid = requireUid()
            val payload = SalaryCycleFirestoreMapper.toMap(config).toMutableMap()
            payload["updatedAt"] = FieldValue.serverTimestamp()
            firestore.collection("users").document(uid)
                .collection("financialPreferences").document("salaryCycle")
                .set(payload, SetOptions.merge())
                .await()
            Unit
        }

    override suspend fun isRolloverProcessed(cycleKey: String): Boolean = runCatching {
        val uid = auth.currentUser?.uid ?: return false
        val docId = sanitizeKey(cycleKey)
        val snapshot = firestore.collection("users").document(uid)
            .collection("salaryRollovers").document(docId).get().await()
        snapshot.exists()
    }.getOrDefault(false)

    override suspend fun markRolloverProcessed(cycleKey: String): AppResult<Unit> =
        firebaseResult("Không thể ghi nhận trạng thái chuyển chu kỳ lương") {
            val uid = requireUid()
            val docId = sanitizeKey(cycleKey)
            val data = mapOf(
                "cycleKey" to cycleKey,
                "processedAt" to FieldValue.serverTimestamp(),
            )
            firestore.collection("users").document(uid)
                .collection("salaryRollovers").document(docId)
                .set(data, SetOptions.merge())
                .await()
            Unit
        }

    private fun sanitizeKey(key: String): String =
        key.replace(":", "_").replace("/", "_").replace(".", "_")

    private fun requireUid(): String = auth.currentUser?.uid ?: error("Phiên đăng nhập đã hết hạn")
}
