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

    @Test
    fun `semi monthly requires valid second payday day in 1 to 31`() {
        val cfg1 = SalaryCycleConfig(
            enabled = true,
            scheduleType = com.finlux.app.domain.model.SalaryScheduleType.SEMI_MONTHLY,
            paydayDay = 25,
            secondPaydayDay = null,
        )
        assertTrue(validate(cfg1) is AppResult.Error)

        val cfg2 = cfg1.copy(secondPaydayDay = 0)
        assertTrue(validate(cfg2) is AppResult.Error)

        val cfg3 = cfg1.copy(secondPaydayDay = 32)
        assertTrue(validate(cfg3) is AppResult.Error)
    }

    @Test
    fun `semi monthly rejects identical paydays`() {
        val cfg = SalaryCycleConfig(
            enabled = true,
            scheduleType = com.finlux.app.domain.model.SalaryScheduleType.SEMI_MONTHLY,
            paydayDay = 15,
            secondPaydayDay = 15,
        )
        val result = validate(cfg)
        assertTrue(result is AppResult.Error)
        org.junit.jupiter.api.Assertions.assertEquals("Hai ngày nhận lương không được trùng nhau", (result as AppResult.Error).message)
    }

    @Test
    fun `semi monthly rejects paydays closer than 5 days`() {
        val cfg1 = SalaryCycleConfig(
            enabled = true,
            scheduleType = com.finlux.app.domain.model.SalaryScheduleType.SEMI_MONTHLY,
            paydayDay = 10,
            secondPaydayDay = 12,
        )
        assertTrue(validate(cfg1) is AppResult.Error)

        // Circular check: day 29 and day 2 (diff 27, circular diff 30-27 = 3 < 5)
        val cfg2 = SalaryCycleConfig(
            enabled = true,
            scheduleType = com.finlux.app.domain.model.SalaryScheduleType.SEMI_MONTHLY,
            paydayDay = 29,
            secondPaydayDay = 2,
        )
        assertTrue(validate(cfg2) is AppResult.Error)
    }

    @Test
    fun `semi monthly requires positive salary for both paydays`() {
        val cfg1 = SalaryCycleConfig(
            enabled = true,
            scheduleType = com.finlux.app.domain.model.SalaryScheduleType.SEMI_MONTHLY,
            paydayDay = 25,
            secondPaydayDay = 10,
            expectedSalary = null,
            secondExpectedSalary = Money(7_500_000),
        )
        assertTrue(validate(cfg1) is AppResult.Error)

        val cfg2 = cfg1.copy(expectedSalary = Money(6_000_000), secondExpectedSalary = Money(0))
        assertTrue(validate(cfg2) is AppResult.Error)

        val validCfg = cfg1.copy(expectedSalary = Money(6_000_000), secondExpectedSalary = Money(7_500_000))
        assertTrue(validate(validCfg) is AppResult.Success<*>)
    }
}

