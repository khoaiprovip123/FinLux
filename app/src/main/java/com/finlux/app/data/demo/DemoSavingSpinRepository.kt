package com.finlux.app.data.demo

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SavingDestination
import com.finlux.app.domain.model.SavingMethod
import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.SavingSpinStatus
import com.finlux.app.domain.repository.SavingSpinRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

@Singleton
class DemoSavingSpinRepository @Inject constructor() : SavingSpinRepository {
    private val config = MutableStateFlow(SavingSpinConfig())
    private val destinations = MutableStateFlow<Map<String, SavingDestination>>(emptyMap())
    private val sessions = MutableStateFlow<Map<String, SavingSpinSession>>(emptyMap())

    override fun observeConfig(): Flow<SavingSpinConfig> = config

    override suspend fun saveConfig(config: SavingSpinConfig): AppResult<Unit> {
        this.config.value = config.copy(updatedAt = Instant.now())
        return AppResult.Success(Unit)
    }

    override fun observeDestinations(): Flow<List<SavingDestination>> =
        destinations.map { items -> items.values.sortedBy { it.createdAt } }

    override suspend fun upsertDestination(destination: SavingDestination): AppResult<String> = synchronized(this) {
        val id = destination.id.ifBlank { UUID.randomUUID().toString() }
        val existing = destinations.value[id]
        destinations.value = destinations.value + (id to destination.copy(
            id = id,
            createdAt = existing?.createdAt ?: destination.createdAt,
            updatedAt = Instant.now(),
        ))
        AppResult.Success(id)
    }

    override suspend fun deleteDestination(id: String): AppResult<Unit> = synchronized(this) {
        destinations.value = destinations.value - id
        AppResult.Success(Unit)
    }

    override fun observeSession(scheduleKey: String): Flow<SavingSpinSession?> =
        sessions.map { it[scheduleKey] }

    override suspend fun getOrCreateSession(
        scheduleKey: String,
        wheelValues: List<Money>,
    ): AppResult<SavingSpinSession> = synchronized(this) {
        sessions.value[scheduleKey]?.let { return@synchronized AppResult.Success(it) }
        if (wheelValues.isEmpty()) return@synchronized AppResult.Error("Vòng quay chưa có mệnh giá")
        val now = Instant.now()
        val session = SavingSpinSession(
            id = sanitizeKey(scheduleKey),
            scheduleKey = scheduleKey,
            wheelValues = wheelValues,
            createdAt = now,
            updatedAt = now,
        )
        sessions.value = sessions.value + (scheduleKey to session)
        AppResult.Success(session)
    }

    override suspend fun lockSpinResult(
        scheduleKey: String,
        selectedIndex: Int,
    ): AppResult<SavingSpinSession> = synchronized(this) {
        val current = sessions.value[scheduleKey]
            ?: return@synchronized AppResult.Error("Lượt quay không tồn tại")
        current.selectedIndex?.let { return@synchronized AppResult.Success(current) }
        if (current.status != SavingSpinStatus.READY || selectedIndex !in current.wheelValues.indices) {
            return@synchronized AppResult.Error("Không thể chốt kết quả lượt quay")
        }
        val updated = current.copy(
            selectedIndex = selectedIndex,
            selectedAmount = current.wheelValues[selectedIndex],
            status = SavingSpinStatus.SPUN_PENDING,
            spunAt = Instant.now(),
            updatedAt = Instant.now(),
        )
        sessions.value = sessions.value + (scheduleKey to updated)
        AppResult.Success(updated)
    }

    override suspend fun completeSession(
        scheduleKey: String,
        destinationId: String,
        method: SavingMethod,
    ): AppResult<Unit> = updateSession(scheduleKey) { current ->
        require(current.status in setOf(SavingSpinStatus.SPUN_PENDING, SavingSpinStatus.SNOOZED) && current.selectedAmount != null) {
            "Lượt quay chưa có kết quả để hoàn tất"
        }
        require(destinations.value[destinationId]?.enabled == true) { "Nơi tiết kiệm không hợp lệ" }
        current.copy(
            status = SavingSpinStatus.COMPLETED,
            destinationId = destinationId,
            method = method,
            completedAt = Instant.now(),
            updatedAt = Instant.now(),
        )
    }

    override suspend fun snoozeSession(scheduleKey: String, until: Instant): AppResult<Unit> =
        updateSession(scheduleKey) { current ->
            require(current.status == SavingSpinStatus.SPUN_PENDING || current.status == SavingSpinStatus.SNOOZED)
            current.copy(status = SavingSpinStatus.SNOOZED, snoozedUntil = until, updatedAt = Instant.now())
        }

    override suspend fun skipSession(scheduleKey: String): AppResult<Unit> = updateSession(scheduleKey) { current ->
        require(current.status in setOf(SavingSpinStatus.READY, SavingSpinStatus.SPUN_PENDING, SavingSpinStatus.SNOOZED))
        current.copy(status = SavingSpinStatus.SKIPPED, skippedAt = Instant.now(), updatedAt = Instant.now())
    }

    override fun observeSessions(fromInclusive: Instant, toExclusive: Instant): Flow<List<SavingSpinSession>> =
        combine(sessions, config) { items, _ ->
            items.values.filter { it.createdAt >= fromInclusive && it.createdAt < toExclusive }
                .sortedByDescending { it.createdAt }
        }

    private fun updateSession(
        scheduleKey: String,
        transform: (SavingSpinSession) -> SavingSpinSession,
    ): AppResult<Unit> = synchronized(this) {
        val current = sessions.value[scheduleKey] ?: return@synchronized AppResult.Error("Lượt quay không tồn tại")
        runCatching { transform(current) }.fold(
            onSuccess = { updated ->
                sessions.value = sessions.value + (scheduleKey to updated)
                AppResult.Success(Unit)
            },
            onFailure = { AppResult.Error(it.message ?: "Không thể cập nhật lượt quay", it) },
        )
    }

    private fun sanitizeKey(key: String) = key.replace(':', '_').replace('/', '_').replace('.', '_')
}
