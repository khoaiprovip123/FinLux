package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.model.SavingSpinFrequency
import com.finlux.app.domain.model.SavingSpinStep
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ValidateSavingSpinConfigUseCaseTest {
    private val validate = ValidateSavingSpinConfigUseCase()

    @Test
    fun `accepts a valid configuration`() {
        val result = validate(SavingSpinConfig())

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `rejects non-positive and reversed ranges`() {
        assertFalse(validate(SavingSpinConfig(minAmount = Money(0))).isValid)
        assertFalse(
            validate(
                SavingSpinConfig(
                    minAmount = Money(100_000),
                    maxAmount = Money(50_000),
                ),
            ).isValid,
        )
    }

    @Test
    fun `requires min and max to be exact multiples of step`() {
        val result = validate(
            SavingSpinConfig(
                minAmount = Money(12_000),
                maxAmount = Money(101_000),
                step = SavingSpinStep.FIVE_THOUSAND,
            ),
        )

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.code == SavingSpinValidationCode.MIN_NOT_MULTIPLE })
        assertTrue(result.errors.any { it.code == SavingSpinValidationCode.MAX_NOT_MULTIPLE })
    }

    @Test
    fun `rejects unsupported slot count and insufficient candidates`() {
        assertFalse(validate(SavingSpinConfig(slotCount = 7)).isValid)
        assertFalse(
            validate(
                SavingSpinConfig(
                    minAmount = Money(10_000),
                    maxAmount = Money(30_000),
                    slotCount = 8,
                ),
            ).isValid,
        )
    }

    @Test
    fun `limits amount to fifteen digits`() {
        val result = validate(
            SavingSpinConfig(maxAmount = Money(1_000_000_000_000_000L)),
        )

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.code == SavingSpinValidationCode.MAX_TOO_LARGE })
    }

    @Test
    fun `validates selected weekdays and reminder time`() {
        val result = validate(
            SavingSpinConfig(
                frequency = SavingSpinFrequency.SELECTED_WEEKDAYS,
                selectedWeekdays = setOf(0, 8),
                reminderHour = 24,
                reminderMinute = 60,
            ),
        )

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.code == SavingSpinValidationCode.INVALID_WEEKDAYS })
        assertTrue(result.errors.any { it.code == SavingSpinValidationCode.INVALID_REMINDER_TIME })
    }
}
