package com.finlux.app.data.remote.firebase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SavingDestination
import com.finlux.app.domain.model.SavingMethod
import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.SavingSpinStatus
import com.finlux.app.domain.repository.SavingSpinRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.util.Date
import java.util.UUID

class FirebaseSavingSpinRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : SavingSpinRepository {
    override fun observeConfig(): Flow<SavingSpinConfig> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(SavingSpinConfig())
            close()
            return@callbackFlow
        }
        val registration = configRef(uid).addSnapshotListener { snapshot, error ->
            if (error != null) close(error)
            else trySend(SavingSpinFirestoreMapper.configFromMap(snapshot?.data))
        }
        awaitClose { registration.remove() }
    }

    override suspend fun saveConfig(config: SavingSpinConfig): AppResult<Unit> =
        firebaseResult("Không thể lưu cấu hình vòng quay tiết kiệm") {
            val now = Instant.now()
            val payload = SavingSpinFirestoreMapper.configToMap(config.copy(updatedAt = now))
            configRef(requireUid()).set(payload).await()
            Unit
        }

    override fun observeDestinations(): Flow<List<SavingDestination>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = userRef(uid).collection(DESTINATIONS)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) close(error)
                else trySend(snapshot?.documents.orEmpty().mapNotNull(SavingSpinFirestoreMapper::destinationFromDocument))
            }
        awaitClose { registration.remove() }
    }

    override suspend fun upsertDestination(destination: SavingDestination): AppResult<String> =
        firebaseResult("Không thể lưu nơi tiết kiệm") {
            val id = destination.id.ifBlank { UUID.randomUUID().toString() }
            val now = Instant.now()
            val ref = userRef(requireUid()).collection(DESTINATIONS).document(id)
            val existing = ref.get().await()
            val persisted = destination.copy(
                id = id,
                createdAt = existing.getTimestamp("createdAt")?.toDate()?.toInstant() ?: destination.createdAt,
                updatedAt = now,
            )
            ref.set(SavingSpinFirestoreMapper.destinationToMap(persisted)).await()
            id
        }

    override suspend fun deleteDestination(id: String): AppResult<Unit> =
        firebaseResult("Không thể xóa nơi tiết kiệm") {
            require(id.isNotBlank()) { "Nơi tiết kiệm không hợp lệ" }
            val uid = requireUid()
            val destinationRef = userRef(uid).collection(DESTINATIONS).document(id)
            val configRef = configRef(uid)
            val now = Instant.now()
            firestore.runTransaction { transaction ->
                val configSnapshot = transaction.get(configRef)
                if (configSnapshot.exists() && configSnapshot.getString("defaultDestinationId") == id) {
                    transaction.update(
                        configRef,
                        mapOf(
                            "defaultDestinationId" to null,
                            "updatedAt" to now.toTimestamp(),
                        ),
                    )
                }
                transaction.delete(destinationRef)
            }.await()
            Unit
        }

    override fun observeSession(scheduleKey: String): Flow<SavingSpinSession?> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val registration = sessionRef(uid, scheduleKey).addSnapshotListener { snapshot, error ->
            if (error != null) close(error)
            else trySend(snapshot?.takeIf { it.exists() }?.let(SavingSpinFirestoreMapper::sessionFromDocument))
        }
        awaitClose { registration.remove() }
    }

    override suspend fun getOrCreateSession(
        scheduleKey: String,
        wheelValues: List<Money>,
    ): AppResult<SavingSpinSession> = firebaseResult("Không thể khởi tạo lượt quay") {
        require(wheelValues.isNotEmpty()) { "Vòng quay chưa có mệnh giá" }
        val uid = requireUid()
        val ref = sessionRef(uid, scheduleKey)
        val now = Instant.now()
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            if (snapshot.exists()) {
                return@runTransaction requireNotNull(SavingSpinFirestoreMapper.sessionFromDocument(snapshot))
            }
            val session = SavingSpinSession(
                id = ref.id,
                scheduleKey = scheduleKey,
                wheelValues = wheelValues,
                createdAt = now,
                updatedAt = now,
            )
            transaction.set(ref, SavingSpinFirestoreMapper.sessionToMap(session))
            session
        }.await()
    }

    override suspend fun lockSpinResult(
        scheduleKey: String,
        selectedIndex: Int,
    ): AppResult<SavingSpinSession> = firebaseResult("Không thể chốt kết quả vòng quay") {
        val ref = sessionRef(requireUid(), scheduleKey)
        val now = Instant.now()
        firestore.runTransaction { transaction ->
            val current = requireNotNull(SavingSpinFirestoreMapper.sessionFromDocument(transaction.get(ref))) {
                "Lượt quay không tồn tại"
            }
            if (current.selectedIndex != null) return@runTransaction current
            require(current.status == SavingSpinStatus.READY && selectedIndex in current.wheelValues.indices) {
                "Không thể chốt kết quả lượt quay"
            }
            val updated = current.copy(
                selectedIndex = selectedIndex,
                selectedAmount = current.wheelValues[selectedIndex],
                status = SavingSpinStatus.SPUN_PENDING,
                spunAt = now,
                updatedAt = now,
            )
            transaction.set(ref, SavingSpinFirestoreMapper.sessionToMap(updated))
            updated
        }.await()
    }

    override suspend fun completeSession(
        scheduleKey: String,
        destinationId: String,
        method: SavingMethod,
        transactionId: String?,
    ): AppResult<Unit> = firebaseResult("Không thể xác nhận khoản tiết kiệm") {
        val uid = requireUid()
        val sessionRef = sessionRef(uid, scheduleKey)
        val now = Instant.now()
        firestore.runTransaction { transaction ->
            val current = requireNotNull(SavingSpinFirestoreMapper.sessionFromDocument(transaction.get(sessionRef)))
            if (current.status == SavingSpinStatus.COMPLETED) {
                return@runTransaction
            }
            require(current.status in setOf(SavingSpinStatus.SPUN_PENDING, SavingSpinStatus.SNOOZED) && current.selectedAmount != null) {
                "Lượt quay chưa có kết quả để hoàn tất"
            }
            val updates = mutableMapOf<String, Any?>(
                "status" to SavingSpinStatus.COMPLETED.name,
                "destinationId" to destinationId,
                "method" to method.name,
                "completedAt" to now.toTimestamp(),
                "updatedAt" to now.toTimestamp(),
            )
            if (transactionId != null) {
                updates["transactionId"] = transactionId
            }
            transaction.update(sessionRef, updates)
        }.await()
        Unit
    }

    override suspend fun snoozeSession(scheduleKey: String, until: Instant): AppResult<Unit> =
        transitionSession(
            scheduleKey = scheduleKey,
            allowedStatuses = setOf(SavingSpinStatus.SPUN_PENDING, SavingSpinStatus.SNOOZED),
            status = SavingSpinStatus.SNOOZED,
            timestampField = "snoozedUntil",
            timestamp = until,
        )

    override suspend fun skipSession(scheduleKey: String): AppResult<Unit> =
        transitionSession(
            scheduleKey = scheduleKey,
            allowedStatuses = setOf(SavingSpinStatus.READY, SavingSpinStatus.SPUN_PENDING, SavingSpinStatus.SNOOZED),
            status = SavingSpinStatus.SKIPPED,
            timestampField = "skippedAt",
            timestamp = Instant.now(),
        )

    override fun observeSessions(fromInclusive: Instant, toExclusive: Instant): Flow<List<SavingSpinSession>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = userRef(uid).collection(SESSIONS)
            .whereGreaterThanOrEqualTo("createdAt", fromInclusive.toTimestamp())
            .whereLessThan("createdAt", toExclusive.toTimestamp())
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) close(error)
                else trySend(snapshot?.documents.orEmpty().mapNotNull(SavingSpinFirestoreMapper::sessionFromDocument))
            }
        awaitClose { registration.remove() }
    }

    private suspend fun transitionSession(
        scheduleKey: String,
        allowedStatuses: Set<SavingSpinStatus>,
        status: SavingSpinStatus,
        timestampField: String,
        timestamp: Instant,
    ): AppResult<Unit> = firebaseResult("Không thể cập nhật lượt quay") {
        val ref = sessionRef(requireUid(), scheduleKey)
        val now = Instant.now()
        firestore.runTransaction { transaction ->
            val current = requireNotNull(SavingSpinFirestoreMapper.sessionFromDocument(transaction.get(ref)))
            require(current.status in allowedStatuses) {
                "Lượt quay đã được xử lý"
            }
            transaction.update(ref, mapOf(
                "status" to status.name,
                timestampField to timestamp.toTimestamp(),
                "updatedAt" to now.toTimestamp(),
            ))
        }.await()
        Unit
    }

    private fun configRef(uid: String) = userRef(uid).collection(CONFIGS).document(DEFAULT_CONFIG)
    private fun sessionRef(uid: String, scheduleKey: String): DocumentReference =
        userRef(uid).collection(SESSIONS).document(sanitizeKey(scheduleKey))
    private fun userRef(uid: String) = firestore.collection("users").document(uid)
    private fun requireUid(): String = auth.currentUser?.uid ?: error("Phiên đăng nhập đã hết hạn")
    private fun sanitizeKey(key: String) = key.replace(':', '_').replace('/', '_').replace('.', '_')
    private fun Instant.toTimestamp() = Timestamp(Date.from(this))

    companion object {
        private const val CONFIGS = "savingSpinConfigs"
        private const val DESTINATIONS = "savingSpinDestinations"
        private const val SESSIONS = "savingSpinSessions"
        private const val DEFAULT_CONFIG = "default"
    }
}
