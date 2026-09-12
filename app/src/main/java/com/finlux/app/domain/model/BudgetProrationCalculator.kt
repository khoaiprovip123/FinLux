package com.finlux.app.domain.model

import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.roundToLong

/**
 * Thuật toán phân bổ tỷ lệ hạn mức ngân sách khi chu kỳ lương bị co ngắn hoặc kéo dài (Proration Formula).
 *
 * Công thức:
 * ProratedLimit = StandardLimit * (ActualDays / StandardDays)
 *
 * Quy tắc:
 * - ActualDays: Số ngày thực tế giữa period.start và period.endExclusive.
 * - StandardDays: Số ngày chuẩn của chu kỳ thông thường (mặc định 30 ngày).
 * - Làm tròn chuẩn toán học đến đơn vị tiền VND nguyên.
 */
object BudgetProrationCalculator {
    const val DEFAULT_STANDARD_CYCLE_DAYS = 30L

    /**
     * Tính toán hạn mức ngân sách phân bổ theo tỷ lệ ngày thực tế.
     */
    fun calculateProratedLimit(
        standardLimit: Money,
        actualDays: Long,
        standardDays: Long = DEFAULT_STANDARD_CYCLE_DAYS,
    ): Money {
        if (standardLimit.value <= 0L) return Money(0L)
        if (actualDays <= 0L) return Money(0L)
        val validStandardDays = if (standardDays <= 0L) DEFAULT_STANDARD_CYCLE_DAYS else standardDays

        val proratedValue = (standardLimit.value.toDouble() * actualDays.toDouble() / validStandardDays.toDouble()).roundToLong()
        return Money(proratedValue.coerceAtLeast(0L))
    }

    /**
     * Tính số ngày lịch thực tế giữa [start] và [endExclusive] theo [zoneId].
     */
    fun calculateDaysBetween(
        start: Instant,
        endExclusive: Instant,
        zoneId: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh"),
    ): Long {
        val startDate = start.atZone(zoneId).toLocalDate()
        val endDate = endExclusive.atZone(zoneId).toLocalDate()
        return ChronoUnit.DAYS.between(startDate, endDate).coerceAtLeast(1L)
    }

    /**
     * Tính hạn mức phân bổ cho một [FinancialPeriod] cụ thể dựa trên [standardLimit].
     */
    fun calculateProratedLimitForPeriod(
        standardLimit: Money,
        period: FinancialPeriod,
        standardDays: Long = DEFAULT_STANDARD_CYCLE_DAYS,
        zoneId: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh"),
    ): Money {
        val actualDays = calculateDaysBetween(period.start, period.endExclusive, zoneId)
        return calculateProratedLimit(standardLimit, actualDays, standardDays)
    }
}
