package com.finlux.app.domain.repository

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.SalaryCycleConfigRecord
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface SalaryCycleRepository {
    fun observeConfig(): Flow<SalaryCycleConfig>
    suspend fun saveConfig(config: SalaryCycleConfig): AppResult<Unit>
    suspend fun isRolloverProcessed(cycleKey: String): Boolean
    suspend fun markRolloverProcessed(cycleKey: String): AppResult<Unit>

    fun observeTimeline(): Flow<List<SalaryCycleConfigRecord>> = kotlinx.coroutines.flow.flowOf(emptyList())
    suspend fun getConfigAt(instant: Instant): SalaryCycleConfig = SalaryCycleConfig()
    suspend fun getConfigAt(date: LocalDate, zoneId: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh")): SalaryCycleConfig {
        val instant = date.atStartOfDay(zoneId).toInstant()
        return getConfigAt(instant)
    }
    suspend fun saveConfigRecord(record: SalaryCycleConfigRecord): AppResult<Unit> = AppResult.Success(Unit)
}
