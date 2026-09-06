package com.finlux.app.data.remote.firebase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.ManagedOperationType
import com.finlux.app.domain.model.SavingDestination
import com.finlux.app.domain.model.SavingMethod
import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.SavingSpinStatus
import com.finlux.app.domain.model.TransactionType
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

    override suspend fun completeSessionWithWalletTransfer(
        scheduleKey: String,
        destinationId: String,
        method: SavingMethod,
        sourceWalletId: String,
        destinationWalletId: String,
        amount: Long,
        note: String,
        date: Instant,
        operationId: String,
    ): AppResult<Unit> = firebaseResult("Không thể cất tiền Vòng quay vào ví") {
        require(sourceWalletId != destinationWalletId) { "Ví nguồn và ví nhận không được trùng nhau" }
        require(amount > 0L) { "Số tiền tiết kiệm không hợp lệ" }

        val uid = requireUid()
        val safeOperationId = operationId
            .trim()
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .take(180)
        require(safeOperationId.isNotBlank()) { "Mã thao tác tiết kiệm không hợp lệ" }

        val sessionRef = sessionRef(uid, scheduleKey)
        val sourceRef = userRef(uid).collection("wallets").document(sourceWalletId)
        val destinationRef = userRef(uid).collection("wallets").document(destinationWalletId)
        val outRef = userRef(uid).collection("transactions").document("${safeOperationId}_out")
        val inRef = userRef(uid).collection("transactions").document("${safeOperationId}_in")
        val now = Instant.now()

        firestore.runTransaction { transaction ->
            // All reads first.
            val sessionSnapshot = transaction.get(sessionRef)
            val current = requireNotNull(SavingSpinFirestoreMapper.sessionFromDocument(sessionSnapshot)) {
                "Lượt quay không tồn tại"
            }
            val sourceSnapshot = transaction.get(sourceRef)
            val destinationSnapshot = transaction.get(destinationRef)
            val existingOut = transaction.get(outRef)
            val existingIn = transaction.get(inRef)

            if (current.status == SavingSpinStatus.COMPLETED) {
                require(current.transactionId == outRef.id) {
                    "Lượt quay đã hoàn tất bằng một giao dịch khác"
                }
                return@runTransaction
            }

            require(
                current.status in setOf(SavingSpinStatus.SPUN_PENDING, SavingSpinStatus.SNOOZED) &&
                    current.selectedAmount?.value == amount
            ) {
                "Kết quả vòng quay không khớp số tiền cần cất"
            }
            require(sourceSnapshot.exists()) { "Ví nguồn không tồn tại" }
            require(destinationSnapshot.exists()) { "Ví nhận không tồn tại" }

            val sourceBalance = sourceSnapshot.getLong("balance") ?: 0L
            val destinationBalance = destinationSnapshot.getLong("balance") ?: 0L
            val sourceIsCard = sourceSnapshot.getString("type").equals("CARD", ignoreCase = true)

            // Recover a deterministic pair created by the previous two-phase implementation
            // without moving wallet balances a second time.
            if (existingOut.exists() || existingIn.exists()) {
                require(existingOut.exists() && existingIn.exists()) {
                    "Phát hiện cặp giao dịch Vòng quay không toàn vẹn"
                }
                require(
                    existingOut.getString("type").equals("transfer_out", ignoreCase = true) &&
                        existingIn.getString("type").equals("transfer_in", ignoreCase = true) &&
                        existingOut.getString("walletId") == sourceWalletId &&
                        existingIn.getString("walletId") == destinationWalletId &&
                        existingOut.getString("relatedWalletId") == destinationWalletId &&
                        existingIn.getString("relatedWalletId") == sourceWalletId &&
                        existingOut.getLong("amount") == amount &&
                        existingIn.getLong("amount") == amount
                ) {
                    "Mã thao tác Vòng quay đã được dùng cho giao dịch khác"
                }

                transaction.update(
                    sessionRef,
                    mapOf(
                        "status" to SavingSpinStatus.COMPLETED.name,
                        "destinationId" to destinationId,
                        "method" to method.name,
                        "transactionId" to outRef.id,
                        "completedAt" to now.toTimestamp(),
                        "updatedAt" to now.toTimestamp(),
                    ),
                )
                return@runTransaction
            }

            if (!sourceIsCard && sourceBalance < amount) {
                error("Ví nguồn không đủ số dư để cất tiền")
            }

            val managedId = sessionRef.id
            val common = mapOf(
                "amount" to amount,
                "categoryId" to null,
                "dealId" to null,
                "dealFlowType" to null,
                "goalId" to null,
                "goalFlowType" to null,
                "debtId" to null,
                "debtPrincipalAmount" to null,
                "debtInterestAmount" to null,
                "debtPaymentId" to null,
                "managedOperationType" to ManagedOperationType.SAVING_SPIN.name.lowercase(),
                "managedOperationId" to managedId,
                "note" to note,
                "receiptImageUrl" to null,
                "date" to date.toTimestamp(),
                "createdAt" to now.toTimestamp(),
                "updatedAt" to now.toTimestamp(),
            )

            transaction.update(
                sourceRef,
                mapOf(
                    "balance" to Math.subtractExact(sourceBalance, amount),
                    "lastTransactionId" to outRef.id,
                ),
            )
            transaction.update(
                destinationRef,
                mapOf(
                    "balance" to Math.addExact(destinationBalance, amount),
                    "lastTransactionId" to inRef.id,
                ),
            )
            transaction.set(
                outRef,
                common + mapOf(
                    "type" to TransactionType.TRANSFER_OUT.name.lowercase(),
                    "walletId" to sourceWalletId,
                    "relatedWalletId" to destinationWalletId,
                ),
            )
            transaction.set(
                inRef,
                common + mapOf(
                    "type" to TransactionType.TRANSFER_IN.name.lowercase(),
                    "walletId" to destinationWalletId,
                    "relatedWalletId" to sourceWalletId,
                ),
            )
            transaction.update(
                sessionRef,
                mapOf(
                    "status" to SavingSpinStatus.COMPLETED.name,
                    "destinationId" to destinationId,
                    "method" to method.name,
                    "transactionId" to outRef.id,
                    "completedAt" to now.toTimestamp(),
                    "updatedAt" to now.toTimestamp(),
                ),
            )
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
