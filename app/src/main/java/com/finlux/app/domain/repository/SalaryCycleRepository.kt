package com.finlux.app.domain.repository

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.SalaryCycleConfig
import kotlinx.coroutines.flow.Flow

interface SalaryCycleRepository {
    fun observeConfig(): Flow<SalaryCycleConfig>
    suspend fun saveConfig(config: SalaryCycleConfig): AppResult<Unit>
}
