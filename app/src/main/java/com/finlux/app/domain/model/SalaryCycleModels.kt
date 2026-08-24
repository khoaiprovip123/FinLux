package com.finlux.app.domain.model

import java.time.Instant

enum class PaydayRuleType {
    DAY_OF_MONTH,
    FIRST_DAY_OF_MONTH,
    LAST_DAY_OF_MONTH,
}

enum class CycleRolloverRule {
    KEEP_IN_WALLET,
    MOVE_TO_SAVINGS,
    ASK_EACH_CYCLE,
}

enum class BudgetPeriodBasis {
    CALENDAR_MONTH,
    SALARY_CYCLE,
}

data class SalaryCycleConfig(
    val enabled: Boolean = false,
    val paydayRuleType: PaydayRuleType = PaydayRuleType.DAY_OF_MONTH,
    val paydayDay: Int = 1,
    val salaryWalletId: String? = null,
    val savingsWalletId: String? = null,
    val expectedSalary: Money? = null,
    val rolloverRule: CycleRolloverRule = CycleRolloverRule.KEEP_IN_WALLET,
    val budgetPeriodBasis: BudgetPeriodBasis = BudgetPeriodBasis.CALENDAR_MONTH,
    val financeTimeZone: String = "Asia/Ho_Chi_Minh",
)

data class FinancialCycle(
    val start: Instant,
    val endExclusive: Instant,
    val label: String,
)
