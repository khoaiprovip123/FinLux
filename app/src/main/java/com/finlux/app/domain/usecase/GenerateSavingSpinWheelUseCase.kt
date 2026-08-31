package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SavingSpinStep
import javax.inject.Inject
import kotlin.random.Random

class GenerateSavingSpinWheelUseCase @Inject constructor() {
    operator fun invoke(
        minAmount: Money,
        maxAmount: Money,
        step: SavingSpinStep,
        slotCount: Int,
        seed: Long,
    ): List<Money> {
        require(minAmount.value > 0L)
        require(maxAmount.value >= minAmount.value)
        require(minAmount.value % step.amount == 0L && maxAmount.value % step.amount == 0L)
        require(slotCount in ValidateSavingSpinConfigUseCase.ALLOWED_SLOT_COUNTS)

        val candidateCount = ((maxAmount.value - minAmount.value) / step.amount) + 1L
        require(candidateCount >= slotCount)

        val random = Random(seed)
        val indexes = linkedSetOf(0L, candidateCount - 1L)
        while (indexes.size < slotCount) indexes += random.nextLong(candidateCount)

        return indexes
            .map { index -> Money(minAmount.value + index * step.amount) }
            .shuffled(random)
    }
}
