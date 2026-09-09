package com.finlux.app.data.remote.firebase

import com.finlux.app.domain.model.BudgetPeriodBasis
import com.finlux.app.domain.model.CycleRolloverRule
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.PaydayRuleType
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.SalaryScheduleType

internal object SalaryCycleFirestoreMapper {
    fun toMap(config: SalaryCycleConfig): Map<String, Any?> = mapOf(
        "enabled" to config.enabled,
        "scheduleType" to config.scheduleType.name,
        "paydayRuleType" to config.paydayRuleType.name,
        "paydayDay" to config.paydayDay,
        "salaryWalletId" to config.salaryWalletId,
        "savingsWalletId" to config.savingsWalletId,
        "expectedSalary" to config.expectedSalary?.value,
        "secondPaydayDay" to config.secondPaydayDay,
        "secondSalaryWalletId" to config.secondSalaryWalletId,
        "secondExpectedSalary" to config.secondExpectedSalary?.value,
        "rolloverRule" to config.rolloverRule.name,
        "budgetPeriodBasis" to config.budgetPeriodBasis.name,
        "financeTimeZone" to config.financeTimeZone,
    )

    fun fromMap(data: Map<String, Any?>?): SalaryCycleConfig {
        if (data.isNullOrEmpty()) return SalaryCycleConfig()

        return SalaryCycleConfig(
            enabled = data["enabled"] as? Boolean ?: false,
            scheduleType = enumOrDefault(
                raw = data["scheduleType"] as? String,
                fallback = SalaryScheduleType.MONTHLY_ONCE,
            ),
            paydayRuleType = enumOrDefault(
                raw = data["paydayRuleType"] as? String,
                fallback = PaydayRuleType.DAY_OF_MONTH,
            ),
            paydayDay = (data["paydayDay"] as? Number)?.toInt() ?: 1,
            salaryWalletId = data["salaryWalletId"] as? String,
            savingsWalletId = data["savingsWalletId"] as? String,
            expectedSalary = (data["expectedSalary"] as? Number)?.toLong()?.let(::Money),
            secondPaydayDay = (data["secondPaydayDay"] as? Number)?.toInt()?.takeIf { it in 1..31 },
            secondSalaryWalletId = data["secondSalaryWalletId"] as? String,
            secondExpectedSalary = (data["secondExpectedSalary"] as? Number)?.toLong()?.takeIf { it > 0 }?.let(::Money),
            rolloverRule = enumOrDefault(
                raw = data["rolloverRule"] as? String,
                fallback = CycleRolloverRule.KEEP_IN_WALLET,
            ),
            budgetPeriodBasis = enumOrDefault(
                raw = data["budgetPeriodBasis"] as? String,
                fallback = BudgetPeriodBasis.CALENDAR_MONTH,
            ),
            financeTimeZone = data["financeTimeZone"] as? String ?: "Asia/Ho_Chi_Minh",
        )
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(raw: String?, fallback: T): T =
        raw?.let { value -> runCatching { enumValueOf<T>(value) }.getOrNull() } ?: fallback
}

