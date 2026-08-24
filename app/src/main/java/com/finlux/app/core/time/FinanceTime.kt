package com.finlux.app.core.time

import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface contract for time and timezone querying across financial modules.
 */
interface FinanceClock {
    val zoneId: ZoneId
    fun now(): Instant
}

@Singleton
class SystemFinanceClock @Inject constructor() : FinanceClock {
    override val zoneId: ZoneId
        get() = FinanceTime.defaultZone

    override fun now(): Instant = Instant.now()
}

/**
 * Standardized Financial Timezone and Month conversion for FinLux.
 * Ensures consistent YearMonth and boundary computation across transactions,
 * budgets, reports, dashboards, and salary cycles.
 */
object FinanceTime {
    val VIETNAM_ZONE: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh")

    /** Stable default used by all financial calculations unless an explicit user zone is supplied. */
    val defaultZone: ZoneId
        get() = VIETNAM_ZONE

    fun zoneOf(id: String): ZoneId = runCatching {
        if (id.isBlank()) error("Blank timezone")
        ZoneId.of(id)
    }.getOrDefault(VIETNAM_ZONE)

    fun financialMonth(instant: Instant, zone: ZoneId = defaultZone): YearMonth {
        return YearMonth.from(instant.atZone(zone))
    }

    fun monthStart(month: YearMonth, zone: ZoneId = defaultZone): Instant {
        return month.atDay(1).atStartOfDay(zone).toInstant()
    }

    fun monthEnd(month: YearMonth, zone: ZoneId = defaultZone): Instant {
        return month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant()
    }
}
