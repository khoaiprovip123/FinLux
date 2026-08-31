package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SavingSpinStep
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GenerateSavingSpinWheelUseCaseTest {
    private val generate = GenerateSavingSpinWheelUseCase()

    @Test
    fun `generates unique values in range and aligned to step`() {
        val values = generate(
            minAmount = Money(10_000),
            maxAmount = Money(100_000),
            step = SavingSpinStep.FIVE_THOUSAND,
            slotCount = 8,
            seed = 20260831L,
        )

        assertEquals(8, values.size)
        assertEquals(8, values.distinct().size)
        assertTrue(values.all { it.value in 10_000L..100_000L })
        assertTrue(values.all { it.value % 5_000L == 0L })
        assertTrue(Money(10_000) in values)
        assertTrue(Money(100_000) in values)
    }

    @Test
    fun `same seed produces the same wheel`() {
        val first = generate(Money(10_000), Money(500_000), SavingSpinStep.TEN_THOUSAND, 10, 42L)
        val second = generate(Money(10_000), Money(500_000), SavingSpinStep.TEN_THOUSAND, 10, 42L)

        assertEquals(first, second)
    }

    @Test
    fun `samples a huge range without materializing all candidates`() {
        val values = generate(
            minAmount = Money(5_000),
            maxAmount = Money(999_999_999_995_000L),
            step = SavingSpinStep.FIVE_THOUSAND,
            slotCount = 12,
            seed = 99L,
        )

        assertEquals(12, values.size)
        assertEquals(12, values.distinct().size)
        assertTrue(values.all { it.value % 5_000L == 0L })
    }
}
