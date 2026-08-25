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
}
