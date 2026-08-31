package com.finlux.app.domain.repository

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SavingDestination
import com.finlux.app.domain.model.SavingMethod
import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.model.SavingSpinSession
import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface SavingSpinRepository {
    fun observeConfig(): Flow<SavingSpinConfig>
    suspend fun saveConfig(config: SavingSpinConfig): AppResult<Unit>

    fun observeDestinations(): Flow<List<SavingDestination>>
    suspend fun upsertDestination(destination: SavingDestination): AppResult<String>
    suspend fun deleteDestination(id: String): AppResult<Unit>

    fun observeSession(scheduleKey: String): Flow<SavingSpinSession?>
    suspend fun getOrCreateSession(
        scheduleKey: String,
        wheelValues: List<Money>,
    ): AppResult<SavingSpinSession>

    suspend fun lockSpinResult(
        scheduleKey: String,
        selectedIndex: Int,
    ): AppResult<SavingSpinSession>

    suspend fun completeSession(
        scheduleKey: String,
        destinationId: String,
        method: SavingMethod,
    ): AppResult<Unit>

    suspend fun snoozeSession(
        scheduleKey: String,
        until: Instant,
    ): AppResult<Unit>

    suspend fun skipSession(scheduleKey: String): AppResult<Unit>

    fun observeSessions(
        fromInclusive: Instant,
        toExclusive: Instant,
    ): Flow<List<SavingSpinSession>>
}
