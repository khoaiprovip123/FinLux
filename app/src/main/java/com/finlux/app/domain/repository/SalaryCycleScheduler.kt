package com.finlux.app.domain.repository

import com.finlux.app.domain.model.SalaryCycleConfig

interface SalaryCycleScheduler {
    fun scheduleNextPayday(config: SalaryCycleConfig)
    fun cancel()
}
