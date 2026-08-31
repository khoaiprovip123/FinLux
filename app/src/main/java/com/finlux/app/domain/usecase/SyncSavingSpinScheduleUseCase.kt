package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.SavingSpinStatus
import com.finlux.app.domain.repository.SavingSpinScheduler
import java.time.Instant
import javax.inject.Inject

class SyncSavingSpinScheduleUseCase @Inject constructor(
    private val scheduler: SavingSpinScheduler,
) {
    operator fun invoke(
        config: SavingSpinConfig,
        currentSession: SavingSpinSession?,
        nextTrigger: Instant,
    ) {
        val finished = currentSession?.status in setOf(SavingSpinStatus.COMPLETED, SavingSpinStatus.SKIPPED)
        if (!config.enabled || !config.reminderEnabled || finished) scheduler.cancel()
        else scheduler.schedule(config, nextTrigger)
    }
}
