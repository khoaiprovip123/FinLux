package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.BudgetPeriodBasis
import com.finlux.app.domain.model.FinancialPeriod
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.SalaryCycleConfigRecord
import com.finlux.app.domain.model.configAt
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

interface FinancialPeriodResolver {
    fun resolveCurrentPeriod(
        config: SalaryCycleConfig,
        now: Instant = Instant.now(),
    ): FinancialPeriod

    fun resolvePreviousPeriod(
        config: SalaryCycleConfig,
        now: Instant = Instant.now(),
    ): FinancialPeriod

    fun resolveNextPeriod(
        config: SalaryCycleConfig,
        now: Instant = Instant.now(),
    ): FinancialPeriod

    fun resolveNextPeriodOf(
        period: FinancialPeriod,
        config: SalaryCycleConfig,
    ): FinancialPeriod

    fun resolvePreviousPeriodOf(
        period: FinancialPeriod,
        config: SalaryCycleConfig,
    ): FinancialPeriod

    fun resolvePeriodContaining(
        instant: Instant,
        config: SalaryCycleConfig,
    ): FinancialPeriod

    fun resolvePeriodKey(
        instant: Instant,
        config: SalaryCycleConfig,
    ): String

    // --- Timeline-aware overloads ---

    fun resolvePeriodContaining(
        instant: Instant,
        timeline: List<SalaryCycleConfigRecord>,
        fallbackConfig: SalaryCycleConfig = SalaryCycleConfig(),
    ): FinancialPeriod = resolvePeriodContaining(instant, timeline.configAt(instant, fallbackConfig))

    fun resolvePeriodKey(
        instant: Instant,
        timeline: List<SalaryCycleConfigRecord>,
        fallbackConfig: SalaryCycleConfig = SalaryCycleConfig(),
    ): String = resolvePeriodContaining(instant, timeline, fallbackConfig).key

    fun resolveCurrentPeriod(
        timeline: List<SalaryCycleConfigRecord>,
        now: Instant = Instant.now(),
        fallbackConfig: SalaryCycleConfig = SalaryCycleConfig(),
    ): FinancialPeriod = resolvePeriodContaining(now, timeline, fallbackConfig)

    fun resolvePreviousPeriod(
        timeline: List<SalaryCycleConfigRecord>,
        now: Instant = Instant.now(),
        fallbackConfig: SalaryCycleConfig = SalaryCycleConfig(),
    ): FinancialPeriod {
        val currentPeriod = resolveCurrentPeriod(timeline, now, fallbackConfig)
        return resolvePreviousPeriodOf(currentPeriod, timeline, fallbackConfig)
    }

    fun resolveNextPeriod(
        timeline: List<SalaryCycleConfigRecord>,
        now: Instant = Instant.now(),
        fallbackConfig: SalaryCycleConfig = SalaryCycleConfig(),
    ): FinancialPeriod {
        val currentPeriod = resolveCurrentPeriod(timeline, now, fallbackConfig)
        return resolveNextPeriodOf(currentPeriod, timeline, fallbackConfig)
    }

    fun resolveNextPeriodOf(
        period: FinancialPeriod,
        timeline: List<SalaryCycleConfigRecord>,
        fallbackConfig: SalaryCycleConfig = SalaryCycleConfig(),
    ): FinancialPeriod {
        return resolvePeriodContaining(period.endExclusive.plusMillis(1), timeline, fallbackConfig)
    }

    fun resolvePreviousPeriodOf(
        period: FinancialPeriod,
        timeline: List<SalaryCycleConfigRecord>,
        fallbackConfig: SalaryCycleConfig = SalaryCycleConfig(),
    ): FinancialPeriod {
        return resolvePeriodContaining(period.start.minusMillis(1), timeline, fallbackConfig)
    }
}

@Singleton
class DefaultFinancialPeriodResolver @Inject constructor(
    private val salaryCycleCalculator: SalaryCycleCalculator,
) : FinancialPeriodResolver {

    override fun resolveCurrentPeriod(
        config: SalaryCycleConfig,
        now: Instant,
    ): FinancialPeriod = resolvePeriodContaining(now, config)

    override fun resolvePreviousPeriod(
        config: SalaryCycleConfig,
        now: Instant,
    ): FinancialPeriod {
        val currentPeriod = resolveCurrentPeriod(config, now)
        return resolvePreviousPeriodOf(currentPeriod, config)
    }

    override fun resolveNextPeriod(
        config: SalaryCycleConfig,
        now: Instant,
    ): FinancialPeriod {
        val currentPeriod = resolveCurrentPeriod(config, now)
        return resolveNextPeriodOf(currentPeriod, config)
    }

    override fun resolveNextPeriodOf(
        period: FinancialPeriod,
        config: SalaryCycleConfig,
    ): FinancialPeriod {
        return resolvePeriodContaining(period.endExclusive.plusMillis(1), config)
    }

    override fun resolvePreviousPeriodOf(
        period: FinancialPeriod,
        config: SalaryCycleConfig,
    ): FinancialPeriod {
        return resolvePeriodContaining(period.start.minusMillis(1), config)
    }

    override fun resolvePeriodContaining(
        instant: Instant,
        config: SalaryCycleConfig,
    ): FinancialPeriod {
        val zone = resolveZone(config)
        return if (!config.enabled || config.budgetPeriodBasis == BudgetPeriodBasis.CALENDAR_MONTH) {
            val month = YearMonth.from(instant.atZone(zone))
            val start = month.atDay(1).atStartOfDay(zone).toInstant()
            val endExclusive = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant()
            val monthStr = month.monthValue.toString().padStart(2, '0')
            FinancialPeriod(
                key = "month:$month",
                start = start,
                endExclusive = endExclusive,
                displayLabel = "Tháng $monthStr/${month.year}",
                basis = BudgetPeriodBasis.CALENDAR_MONTH,
            )
        } else {
            val cycle = salaryCycleCalculator.cycleContaining(instant, config, zone)
            val startDate = cycle.start.atZone(zone).toLocalDate()
            FinancialPeriod(
                key = "salary:$startDate",
                start = cycle.start,
                endExclusive = cycle.endExclusive,
                displayLabel = cycle.label,
                basis = BudgetPeriodBasis.SALARY_CYCLE,
            )
        }
    }

    override fun resolvePeriodKey(
        instant: Instant,
        config: SalaryCycleConfig,
    ): String = resolvePeriodContaining(instant, config).key

    private fun resolveZone(config: SalaryCycleConfig): ZoneId =
        runCatching { ZoneId.of(config.financeTimeZone) }
            .getOrDefault(ZoneId.of("Asia/Ho_Chi_Minh"))
}
