package com.finlux.app.data.remote.firebase

import com.finlux.app.domain.model.BudgetPeriodBasis
import com.finlux.app.domain.model.CycleRolloverRule
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.PaydayRuleType
import com.finlux.app.domain.model.SalaryCycleConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FirebaseSalaryCycleMapperTest {
    @Test
    fun `mapper round trips salary cycle config`() {
        val source = SalaryCycleConfig(
            enabled = true,
            paydayRuleType = PaydayRuleType.DAY_OF_MONTH,
            paydayDay = 25,
            salaryWalletId = "salary-wallet",
            savingsWalletId = "saving-wallet",
            expectedSalary = Money(20_000_000),
            rolloverRule = CycleRolloverRule.ASK_EACH_CYCLE,
            budgetPeriodBasis = BudgetPeriodBasis.SALARY_CYCLE,
            financeTimeZone = "Asia/Ho_Chi_Minh",
        )

        val mapped = SalaryCycleFirestoreMapper.fromMap(SalaryCycleFirestoreMapper.toMap(source))

        assertEquals(source, mapped)
    }

    @Test
    fun `missing document fields map to disabled defaults for existing users`() {
        assertEquals(SalaryCycleConfig(), SalaryCycleFirestoreMapper.fromMap(emptyMap()))
        assertEquals(SalaryCycleConfig(), SalaryCycleFirestoreMapper.fromMap(null))
    }

    @Test
    fun `unknown enum values fall back safely instead of crashing`() {
        val mapped = SalaryCycleFirestoreMapper.fromMap(
            mapOf(
                "enabled" to true,
                "paydayRuleType" to "UNKNOWN",
                "rolloverRule" to "UNKNOWN",
                "budgetPeriodBasis" to "UNKNOWN",
            )
        )

        assertEquals(PaydayRuleType.DAY_OF_MONTH, mapped.paydayRuleType)
        assertEquals(CycleRolloverRule.KEEP_IN_WALLET, mapped.rolloverRule)
        assertEquals(BudgetPeriodBasis.CALENDAR_MONTH, mapped.budgetPeriodBasis)
    }

    @Test
    fun `mapper round trips semi monthly salary cycle config`() {
        val source = SalaryCycleConfig(
            enabled = true,
            scheduleType = com.finlux.app.domain.model.SalaryScheduleType.SEMI_MONTHLY,
            paydayRuleType = PaydayRuleType.DAY_OF_MONTH,
            paydayDay = 25,
            salaryWalletId = "wallet-1",
            expectedSalary = Money(6_000_000),
            secondPaydayDay = 10,
            secondSalaryWalletId = "wallet-2",
            secondExpectedSalary = Money(7_500_000),
            savingsWalletId = "saving-wallet",
            rolloverRule = CycleRolloverRule.KEEP_IN_WALLET,
            budgetPeriodBasis = BudgetPeriodBasis.SALARY_CYCLE,
            financeTimeZone = "Asia/Ho_Chi_Minh",
        )

        val mapped = SalaryCycleFirestoreMapper.fromMap(SalaryCycleFirestoreMapper.toMap(source))

        assertEquals(source, mapped)
        assertEquals(Money(13_500_000), mapped.totalExpectedSalary)
        assertEquals(2, mapped.activePaydays.size)
    }

    @Test
    fun `legacy v1 document without semi monthly fields maps to monthly once defaults`() {
        val legacyData = mapOf(
            "enabled" to true,
            "paydayRuleType" to "DAY_OF_MONTH",
            "paydayDay" to 15,
            "salaryWalletId" to "wallet-old",
            "expectedSalary" to 15_000_000L,
            "rolloverRule" to "KEEP_IN_WALLET",
            "budgetPeriodBasis" to "CALENDAR_MONTH",
            "financeTimeZone" to "Asia/Ho_Chi_Minh",
        )

        val mapped = SalaryCycleFirestoreMapper.fromMap(legacyData)

        assertEquals(com.finlux.app.domain.model.SalaryScheduleType.MONTHLY_ONCE, mapped.scheduleType)
        assertEquals(15, mapped.paydayDay)
        org.junit.jupiter.api.Assertions.assertNull(mapped.secondPaydayDay)
        org.junit.jupiter.api.Assertions.assertNull(mapped.secondExpectedSalary)
        assertEquals(Money(15_000_000L), mapped.totalExpectedSalary)
        assertEquals(1, mapped.activePaydays.size)
    }
}

