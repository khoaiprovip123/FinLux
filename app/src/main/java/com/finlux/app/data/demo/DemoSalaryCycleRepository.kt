package com.finlux.app.data.demo

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.SalaryCycleConfigRecord
import com.finlux.app.domain.repository.SalaryCycleRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class DemoSalaryCycleRepository @Inject constructor() : SalaryCycleRepository {
    private val config = MutableStateFlow(SalaryCycleConfig())
    private val timeline = MutableStateFlow<List<SalaryCycleConfigRecord>>(emptyList())
    private val processedRollovers = mutableSetOf<String>()

    override fun observeConfig(): Flow<SalaryCycleConfig> = config.asStateFlow()

    override suspend fun saveConfig(config: SalaryCycleConfig): AppResult<Unit> {
        this.config.value = config
        return AppResult.Success(Unit)
    }

    override suspend fun isRolloverProcessed(cycleKey: String): Boolean {
        return synchronized(processedRollovers) {
            processedRollovers.contains(cycleKey)
        }
    }

    override suspend fun markRolloverProcessed(cycleKey: String): AppResult<Unit> {
        synchronized(processedRollovers) {
            processedRollovers.add(cycleKey)
        }
        return AppResult.Success(Unit)
    }

    override fun observeTimeline(): Flow<List<SalaryCycleConfigRecord>> = timeline.asStateFlow()

    override suspend fun getConfigAt(instant: Instant): SalaryCycleConfig {
        val records = timeline.value
        val matched = records.firstOrNull { it.isEffectiveAt(instant) }
        return matched?.config ?: config.value
    }

    override suspend fun saveConfigRecord(record: SalaryCycleConfigRecord): AppResult<Unit> {
        val docId = record.id.ifBlank { "rec_${record.effectiveFromDate}" }
        val currentList = timeline.value.filterNot { it.id == docId }.toMutableList()
        currentList.add(record.copy(id = docId))
        timeline.value = currentList.sortedByDescending { it.effectiveFromDate }

        if (record.effectiveToDate == null) {
            config.value = record.config
        }
        return AppResult.Success(Unit)
    }
}
