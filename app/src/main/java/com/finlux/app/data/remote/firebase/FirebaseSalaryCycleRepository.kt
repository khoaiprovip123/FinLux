package com.finlux.app.data.remote.firebase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.SalaryCycleConfigRecord
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.finlux.app.data.remote.firebase.schema.FirestoreSchema
import java.time.Instant
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await

class FirebaseSalaryCycleRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : SalaryCycleRepository {

    private fun userFinancialPrefs(uid: String) =
        firestore.collection(FirestoreSchema.USERS).document(uid).collection(FirestoreSchema.Collections.FINANCIAL_PREFERENCES)

    private fun userSalaryRollovers(uid: String) =
        firestore.collection(FirestoreSchema.USERS).document(uid).collection(FirestoreSchema.Collections.SALARY_ROLLOVERS)

    private fun userSalaryTimeline(uid: String) =
        firestore.collection(FirestoreSchema.USERS).document(uid).collection(FirestoreSchema.Collections.SALARY_TIMELINE)

    override fun observeConfig(): Flow<SalaryCycleConfig> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(SalaryCycleConfig())
            close()
            return@callbackFlow
        }

        val document = userFinancialPrefs(uid).document(FirestoreSchema.Documents.SALARY_CYCLE)
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
            userFinancialPrefs(uid).document(FirestoreSchema.Documents.SALARY_CYCLE)
                .set(payload, SetOptions.merge())
                .await()
            Unit
        }

    override suspend fun isRolloverProcessed(cycleKey: String): Boolean = runCatching {
        val uid = auth.currentUser?.uid ?: return false
        val docId = sanitizeKey(cycleKey)
        val snapshot = userSalaryRollovers(uid).document(docId).get().await()
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
            userSalaryRollovers(uid).document(docId)
                .set(data, SetOptions.merge())
                .await()
            Unit
        }

    override fun observeTimeline(): Flow<List<SalaryCycleConfigRecord>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val collection = userSalaryTimeline(uid)
        val registration = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
            } else {
                val records = snapshot?.documents?.mapNotNull { doc ->
                    SalaryCycleFirestoreMapper.recordFromMap(doc.id, doc.data)
                }.orEmpty().sortedByDescending { it.effectiveFromDate }
                trySend(records)
            }
        }
        awaitClose { registration.remove() }
    }

    override suspend fun getConfigAt(instant: Instant): SalaryCycleConfig {
        val records = observeTimeline().firstOrNull().orEmpty()
        val matched = records.firstOrNull { it.isEffectiveAt(instant) }
        return matched?.config ?: (observeConfig().firstOrNull() ?: SalaryCycleConfig())
    }

    override suspend fun saveConfigRecord(record: SalaryCycleConfigRecord): AppResult<Unit> =
        firebaseResult("Không thể lưu bản ghi lịch sử chu kỳ lương") {
            val uid = requireUid()
            val docId = record.id.ifBlank { "rec_${record.effectiveFromDate}" }
            val recordToSave = record.copy(id = docId)
            val payload = SalaryCycleFirestoreMapper.recordToMap(recordToSave).toMutableMap()
            payload["updatedAt"] = FieldValue.serverTimestamp()

            userSalaryTimeline(uid).document(docId)
                .set(payload, SetOptions.merge())
                .await()

            // Nếu đây là cấu hình hiện hành (effectiveToDate == null) và đã đến thời điểm có hiệu lực
            if (record.effectiveToDate == null && record.isEffectiveAt(Instant.now())) {
                saveConfig(record.config)
            }
            Unit
        }

    private fun sanitizeKey(key: String): String =
        key.replace(":", "_").replace("/", "_").replace(".", "_")

    private fun requireUid(): String = auth.currentUser?.uid ?: error("Phiên đăng nhập đã hết hạn")
}
