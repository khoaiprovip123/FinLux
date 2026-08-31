package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.repository.SavingSpinRepository
import javax.inject.Inject

class GetOrCreateSavingSpinSessionUseCase @Inject constructor(
    private val repository: SavingSpinRepository,
    private val generateWheel: GenerateSavingSpinWheelUseCase,
) {
    suspend operator fun invoke(
        scheduleKey: String,
        config: SavingSpinConfig,
    ): AppResult<SavingSpinSession> {
        val wheel = generateWheel(
            minAmount = config.minAmount,
            maxAmount = config.maxAmount,
            step = config.step,
            slotCount = config.slotCount,
            seed = stableSeed(scheduleKey),
        )
        return repository.getOrCreateSession(scheduleKey, wheel)
    }

    private fun stableSeed(value: String): Long = value.fold(1_125_899_906_842_597L) { hash, char ->
        31L * hash + char.code
    }
}
