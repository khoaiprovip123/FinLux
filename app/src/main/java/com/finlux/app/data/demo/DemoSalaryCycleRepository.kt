package com.finlux.app.data.demo

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.repository.SalaryCycleRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class DemoSalaryCycleRepository @Inject constructor() : SalaryCycleRepository {
    private val config = MutableStateFlow(SalaryCycleConfig())

    override fun observeConfig(): Flow<SalaryCycleConfig> = config.asStateFlow()

    override suspend fun saveConfig(config: SalaryCycleConfig): AppResult<Unit> {
        this.config.value = config
        return AppResult.Success(Unit)
    }
}
