package com.finlux.app.domain.repository

import com.finlux.app.domain.model.SavingSpinConfig
import java.time.Instant

interface SavingSpinScheduler {
    fun schedule(config: SavingSpinConfig, nextTrigger: Instant)
    fun cancel()
    fun snooze(until: Instant)
}
