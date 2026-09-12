package com.finlux.app.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class PaydayRuleType {
    DAY_OF_MONTH,
    FIRST_DAY_OF_MONTH,
    LAST_DAY_OF_MONTH,
}

enum class SalaryScheduleType {
    MONTHLY_ONCE,
    SEMI_MONTHLY,
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

data class PaydayEntry(
    val id: String = "primary",
    val dayOfMonth: Int = 1,
    val expectedAmount: Money? = null,
    val targetWalletId: String? = null,
    val label: String = "Đợt chính",
)

data class SalaryCycleConfig(
    val enabled: Boolean = false,
    val scheduleType: SalaryScheduleType = SalaryScheduleType.MONTHLY_ONCE,
    val paydayRuleType: PaydayRuleType = PaydayRuleType.DAY_OF_MONTH,
    val paydayDay: Int = 1,
    val salaryWalletId: String? = null,
    val expectedSalary: Money? = null,

    // Khối đợt 2 (khi scheduleType == SEMI_MONTHLY)
    val secondPaydayDay: Int? = null,
    val secondSalaryWalletId: String? = null,
    val secondExpectedSalary: Money? = null,

    val savingsWalletId: String? = null,
    val rolloverRule: CycleRolloverRule = CycleRolloverRule.KEEP_IN_WALLET,
    val budgetPeriodBasis: BudgetPeriodBasis = BudgetPeriodBasis.CALENDAR_MONTH,
    val financeTimeZone: String = "Asia/Ho_Chi_Minh",
) {
    val totalExpectedSalary: Money
        get() = when (scheduleType) {
            SalaryScheduleType.MONTHLY_ONCE -> expectedSalary ?: Money(0L)
            SalaryScheduleType.SEMI_MONTHLY -> {
                val p1 = expectedSalary?.value ?: 0L
                val p2 = secondExpectedSalary?.value ?: 0L
                Money(p1 + p2)
            }
        }

    val activePaydays: List<PaydayEntry>
        get() = when (scheduleType) {
            SalaryScheduleType.MONTHLY_ONCE -> listOf(
                PaydayEntry(
                    id = "first",
                    dayOfMonth = paydayDay,
                    expectedAmount = expectedSalary,
                    targetWalletId = salaryWalletId,
                    label = "Lương hàng tháng",
                )
            )
            SalaryScheduleType.SEMI_MONTHLY -> {
                val list = mutableListOf(
                    PaydayEntry(
                        id = "first",
                        dayOfMonth = paydayDay,
                        expectedAmount = expectedSalary,
                        targetWalletId = salaryWalletId,
                        label = "Lương Đợt 1",
                    )
                )
                if (secondPaydayDay != null && secondPaydayDay in 1..31) {
                    list.add(
                        PaydayEntry(
                            id = "second",
                            dayOfMonth = secondPaydayDay,
                            expectedAmount = secondExpectedSalary,
                            targetWalletId = secondSalaryWalletId,
                            label = "Lương Đợt 2",
                        )
                    )
                }
                list
            }
        }
}

data class PaydaySubCycle(
    val id: String,
    val paydayEntry: PaydayEntry,
    val start: Instant,
    val endExclusive: Instant,
    val label: String,
    val expectedIncome: Money?,
)

data class FinancialCycle(
    val start: Instant,
    val endExclusive: Instant,
    val label: String,
    val subCycles: List<PaydaySubCycle> = emptyList(),
)

data class FinancialPeriod(
    val key: String,
    val start: Instant,
    val endExclusive: Instant,
    val displayLabel: String,
    val basis: BudgetPeriodBasis,
)

/**
 * Bản ghi dòng thời gian cấu hình chu kỳ lương (Time-Versioned Salary Cycle Record).
 * Đảm bảo tính bất biến của dữ liệu quá khứ khi người dùng thay đổi ngày nhận lương hoặc chu kỳ lương.
 */
data class SalaryCycleConfigRecord(
    val id: String = "",
    val effectiveFromDate: String, // YYYY-MM-DD
    val effectiveToDate: String? = null, // YYYY-MM-DD (null nếu đang là cấu hình hiện hành)
    val config: SalaryCycleConfig = SalaryCycleConfig(),
    val createdAt: Instant = Instant.now(),
) {
    fun isEffectiveAt(date: LocalDate): Boolean {
        val from = runCatching { LocalDate.parse(effectiveFromDate) }.getOrDefault(LocalDate.MIN)
        val to = effectiveToDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: LocalDate.MAX
        return !date.isBefore(from) && date.isBefore(to)
    }

    fun isEffectiveAt(instant: Instant, zone: ZoneId = ZoneId.of(config.financeTimeZone)): Boolean {
        val date = instant.atZone(zone).toLocalDate()
        return isEffectiveAt(date)
    }
}

/**
 * Tìm cấu hình chu kỳ lương có hiệu lực tại mốc thời gian [instant] từ dòng thời gian [List<SalaryCycleConfigRecord>].
 */
fun List<SalaryCycleConfigRecord>.configAt(
    instant: Instant,
    fallbackConfig: SalaryCycleConfig = SalaryCycleConfig(),
): SalaryCycleConfig {
    if (isEmpty()) return fallbackConfig
    val matching = firstOrNull { it.isEffectiveAt(instant) }
    if (matching != null) return matching.config

    val sorted = sortedBy { it.effectiveFromDate }
    val earliest = sorted.first()
    val earliestFrom = runCatching { LocalDate.parse(earliest.effectiveFromDate) }.getOrNull()
    val zone = runCatching { ZoneId.of(fallbackConfig.financeTimeZone) }.getOrDefault(ZoneId.of("Asia/Ho_Chi_Minh"))
    val instantDate = instant.atZone(zone).toLocalDate()

    if (earliestFrom != null && instantDate.isBefore(earliestFrom)) {
        return earliest.config
    }

    val active = firstOrNull { it.effectiveToDate == null }
    if (active != null) return active.config

    return sorted.last().config
}

/**
 * Tìm cấu hình chu kỳ lương có hiệu lực tại ngày [date] từ dòng thời gian [List<SalaryCycleConfigRecord>].
 */
fun List<SalaryCycleConfigRecord>.configAt(
    date: LocalDate,
    fallbackConfig: SalaryCycleConfig = SalaryCycleConfig(),
): SalaryCycleConfig {
    if (isEmpty()) return fallbackConfig
    val matching = firstOrNull { it.isEffectiveAt(date) }
    if (matching != null) return matching.config

    val sorted = sortedBy { it.effectiveFromDate }
    val earliest = sorted.first()
    val earliestFrom = runCatching { LocalDate.parse(earliest.effectiveFromDate) }.getOrNull()

    if (earliestFrom != null && date.isBefore(earliestFrom)) {
        return earliest.config
    }

    val active = firstOrNull { it.effectiveToDate == null }
    if (active != null) return active.config

    return sorted.last().config
}



