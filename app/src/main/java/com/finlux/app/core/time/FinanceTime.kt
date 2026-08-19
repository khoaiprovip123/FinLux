package com.finlux.app.core.time

import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

/**
 * Standardized Financial Timezone and Month conversion for FinLux.
 * Ensures consistent YearMonth and boundary computation across transactions,
 * budgets, reports, and dashboards.
 */
object FinanceTime {
    val defaultZone: ZoneId
        get() = ZoneId.systemDefault()

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
