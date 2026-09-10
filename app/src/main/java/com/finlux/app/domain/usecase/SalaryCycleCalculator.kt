package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.FinancialCycle
import com.finlux.app.domain.model.PaydayEntry
import com.finlux.app.domain.model.PaydayRuleType
import com.finlux.app.domain.model.PaydaySubCycle
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.SalaryScheduleType
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

interface SalaryCycleCalculator {
    fun cycleContaining(
        instant: Instant,
        config: SalaryCycleConfig,
        zoneId: ZoneId,
    ): FinancialCycle

    fun previousCycle(
        cycle: FinancialCycle,
        config: SalaryCycleConfig,
        zoneId: ZoneId,
    ): FinancialCycle

    fun subCycleContaining(
        instant: Instant,
        config: SalaryCycleConfig,
        zoneId: ZoneId,
    ): PaydaySubCycle?
}

class DefaultSalaryCycleCalculator @Inject constructor() : SalaryCycleCalculator {
    override fun cycleContaining(
        instant: Instant,
        config: SalaryCycleConfig,
        zoneId: ZoneId,
    ): FinancialCycle {
        val month = YearMonth.from(instant.atZone(zoneId))
        val currentMonthBoundary = boundary(month, config.paydayDay, config.paydayRuleType, zoneId)
        val startMonth = if (instant >= currentMonthBoundary) month else month.minusMonths(1)
        return cycleForStartMonth(startMonth, config, zoneId)
    }

    override fun previousCycle(
        cycle: FinancialCycle,
        config: SalaryCycleConfig,
        zoneId: ZoneId,
    ): FinancialCycle = cycleContaining(cycle.start.minusMillis(1), config, zoneId)

    override fun subCycleContaining(
        instant: Instant,
        config: SalaryCycleConfig,
        zoneId: ZoneId,
    ): PaydaySubCycle? {
        val cycle = cycleContaining(instant, config, zoneId)
        if (cycle.subCycles.isEmpty()) return null
        if (cycle.subCycles.size == 1) return cycle.subCycles.first()
        return if (instant < cycle.subCycles[1].start) {
            cycle.subCycles[0]
        } else {
            cycle.subCycles[1]
        }
    }

    private fun cycleForStartMonth(
        startMonth: YearMonth,
        config: SalaryCycleConfig,
        zoneId: ZoneId,
    ): FinancialCycle {
        val start = boundary(startMonth, config.paydayDay, config.paydayRuleType, zoneId)
        val endExclusive = boundary(startMonth.plusMonths(1), config.paydayDay, config.paydayRuleType, zoneId)
        val formatter = DateTimeFormatter.ofPattern("dd/MM")
        val startDate = start.atZone(zoneId).toLocalDate()
        val inclusiveEndDate = endExclusive.atZone(zoneId).toLocalDate().minusDays(1)
        val label = "${formatter.format(startDate)} - ${formatter.format(inclusiveEndDate)}"

        val subCycles = mutableListOf<PaydaySubCycle>()
        if (config.scheduleType == SalaryScheduleType.SEMI_MONTHLY &&
            config.secondPaydayDay != null && config.secondPaydayDay in 1..31
        ) {
            val d1 = config.paydayDay
            val d2 = config.secondPaydayDay
            val midMonth = if (d2 > d1) startMonth else startMonth.plusMonths(1)
            val midInstant = boundary(midMonth, d2, PaydayRuleType.DAY_OF_MONTH, zoneId)

            val sub1Start = start.atZone(zoneId).toLocalDate()
            val sub1End = midInstant.atZone(zoneId).toLocalDate().minusDays(1)
            val sub2Start = midInstant.atZone(zoneId).toLocalDate()
            val sub2End = endExclusive.atZone(zoneId).toLocalDate().minusDays(1)

            val p1 = PaydayEntry(
                id = "first",
                dayOfMonth = config.paydayDay,
                expectedAmount = config.expectedSalary,
                targetWalletId = config.salaryWalletId,
                label = "Lương Đợt 1",
            )
            val p2 = PaydayEntry(
                id = "second",
                dayOfMonth = config.secondPaydayDay,
                expectedAmount = config.secondExpectedSalary,
                targetWalletId = config.secondSalaryWalletId,
                label = "Lương Đợt 2",
            )

            subCycles.add(
                PaydaySubCycle(
                    id = "first",
                    paydayEntry = p1,
                    start = start,
                    endExclusive = midInstant,
                    label = "${formatter.format(sub1Start)} - ${formatter.format(sub1End)}",
                    expectedIncome = config.expectedSalary,
                )
            )
            subCycles.add(
                PaydaySubCycle(
                    id = "second",
                    paydayEntry = p2,
                    start = midInstant,
                    endExclusive = endExclusive,
                    label = "${formatter.format(sub2Start)} - ${formatter.format(sub2End)}",
                    expectedIncome = config.secondExpectedSalary,
                )
            )
        } else {
            subCycles.add(
                PaydaySubCycle(
                    id = "first",
                    paydayEntry = config.activePaydays.firstOrNull() ?: PaydayEntry(),
                    start = start,
                    endExclusive = endExclusive,
                    label = label,
                    expectedIncome = config.expectedSalary,
                )
            )
        }

        return FinancialCycle(
            start = start,
            endExclusive = endExclusive,
            label = label,
            subCycles = subCycles,
        )
    }

    private fun boundary(
        month: YearMonth,
        dayOfMonth: Int,
        ruleType: PaydayRuleType,
        zoneId: ZoneId,
    ): Instant {
        val day = when (ruleType) {
            PaydayRuleType.FIRST_DAY_OF_MONTH -> 1
            PaydayRuleType.LAST_DAY_OF_MONTH -> month.lengthOfMonth()
            PaydayRuleType.DAY_OF_MONTH -> dayOfMonth.coerceIn(1, month.lengthOfMonth())
        }
        return month.atDay(day).atStartOfDay(zoneId).toInstant()
    }
}

