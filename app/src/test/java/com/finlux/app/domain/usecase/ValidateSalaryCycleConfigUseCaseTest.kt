package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.BudgetPeriodBasis
import com.finlux.app.domain.model.CycleRolloverRule
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.PaydayRuleType
import com.finlux.app.domain.model.SalaryCycleConfig
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ValidateSalaryCycleConfigUseCaseTest {
    private val validate = ValidateSalaryCycleConfigUseCase()

    @Test
    fun `disabled config is accepted with defaults`() {
        assertTrue(validate(SalaryCycleConfig()) is AppResult.Success<*>)
    }

    @Test
    fun `day of month must be between 1 and 31`() {
        assertTrue(validate(SalaryCycleConfig(enabled = true, paydayDay = 0)) is AppResult.Error)
        assertTrue(validate(SalaryCycleConfig(enabled = true, paydayDay = 32)) is AppResult.Error)
    }

    @Test
    fun `first and last day rules ignore paydayDay validation`() {
        assertTrue(validate(SalaryCycleConfig(enabled = true, paydayRuleType = PaydayRuleType.FIRST_DAY_OF_MONTH, paydayDay = 0)) is AppResult.Success<*>)
        assertTrue(validate(SalaryCycleConfig(enabled = true, paydayRuleType = PaydayRuleType.LAST_DAY_OF_MONTH, paydayDay = 32)) is AppResult.Success<*>)
    }

    @Test
    fun `expected salary must be positive when provided`() {
        assertTrue(validate(SalaryCycleConfig(enabled = true, expectedSalary = Money(0))) is AppResult.Error)
        assertTrue(validate(SalaryCycleConfig(enabled = true, expectedSalary = Money(-1))) is AppResult.Error)
        assertTrue(validate(SalaryCycleConfig(enabled = true, expectedSalary = Money(1))) is AppResult.Success<*>)
    }

    @Test
    fun `move to savings requires destination wallet`() {
        val result = validate(
            SalaryCycleConfig(
                enabled = true,
                rolloverRule = CycleRolloverRule.MOVE_TO_SAVINGS,
            )
        )
        assertTrue(result is AppResult.Error)
    }

    @Test
    fun `move to savings accepts configured destination wallet`() {
        val result = validate(
            SalaryCycleConfig(
                enabled = true,
                savingsWalletId = "savings-wallet",
                rolloverRule = CycleRolloverRule.MOVE_TO_SAVINGS,
                budgetPeriodBasis = BudgetPeriodBasis.SALARY_CYCLE,
            )
        )
        assertTrue(result is AppResult.Success<*>)
    }

    @Test
    fun `finance timezone must resolve to a valid zone`() {
        assertTrue(validate(SalaryCycleConfig(enabled = true, financeTimeZone = "Asia/Ho_Chi_Minh")) is AppResult.Success<*>)
        assertTrue(validate(SalaryCycleConfig(enabled = true, financeTimeZone = "Not/A_Zone")) is AppResult.Error)
    }
}
