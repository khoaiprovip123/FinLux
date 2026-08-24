package com.finlux.app.domain.usecase

import com.finlux.app.core.time.FinanceClock
import com.finlux.app.core.time.FinanceTime
import com.finlux.app.domain.model.FinancialCycle
import com.finlux.app.domain.model.SalaryCycleConfig
import javax.inject.Inject

class GetCurrentSalaryCycleUseCase @Inject constructor(
    private val calculator: DefaultSalaryCycleCalculator,
    private val financeClock: FinanceClock,
) {
    operator fun invoke(config: SalaryCycleConfig): FinancialCycle? {
        if (!config.enabled) return null
        return calculator.cycleContaining(
            instant = financeClock.now(),
            config = config,
            zoneId = FinanceTime.zoneOf(config.financeTimeZone),
        )
    }
}

class GetPreviousSalaryCycleUseCase @Inject constructor(
    private val calculator: DefaultSalaryCycleCalculator,
) {
    operator fun invoke(
        currentCycle: FinancialCycle,
        config: SalaryCycleConfig,
    ): FinancialCycle = calculator.previousCycle(
        cycle = currentCycle,
        config = config,
        zoneId = FinanceTime.zoneOf(config.financeTimeZone),
    )
}
