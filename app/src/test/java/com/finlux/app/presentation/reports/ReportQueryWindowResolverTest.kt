package com.finlux.app.presentation.reports

import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.usecase.DefaultSalaryCycleCalculator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class ReportQueryWindowResolverTest {
    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private val resolver = ReportQueryWindowResolver(DefaultSalaryCycleCalculator())

    @Test
    fun `today resolves single day window`() {
        val now = LocalDateTime.of(2026, 9, 10, 14, 30).atZone(zone).toInstant()
        val window = resolver.resolve(
            period = ReportPeriod.TODAY,
            custom = ReportRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 1)),
            now = now,
            salaryConfig = SalaryCycleConfig(),
            zone = zone,
        )

        assertEquals(ReportRange(LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 10)), window.range)
        assertEquals(LocalDate.of(2026, 9, 10), window.currentStart.atZone(zone).toLocalDate())
        assertEquals(LocalDate.of(2026, 9, 11), window.currentEndExclusive.atZone(zone).toLocalDate())
        assertEquals(LocalDate.of(2026, 9, 9), window.previousStart.atZone(zone).toLocalDate())
        assertEquals(LocalDate.of(2026, 9, 10), window.previousEndExclusive.atZone(zone).toLocalDate())
    }

    @Test
    fun `yesterday resolves single day window of previous day`() {
        val now = LocalDateTime.of(2026, 9, 10, 14, 30).atZone(zone).toInstant()
        val window = resolver.resolve(
            period = ReportPeriod.YESTERDAY,
            custom = ReportRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 1)),
            now = now,
            salaryConfig = SalaryCycleConfig(),
            zone = zone,
        )

        assertEquals(ReportRange(LocalDate.of(2026, 9, 9), LocalDate.of(2026, 9, 9)), window.range)
        assertEquals(LocalDate.of(2026, 9, 9), window.currentStart.atZone(zone).toLocalDate())
        assertEquals(LocalDate.of(2026, 9, 10), window.currentEndExclusive.atZone(zone).toLocalDate())
        assertEquals(LocalDate.of(2026, 9, 8), window.previousStart.atZone(zone).toLocalDate())
    }

    @Test
    fun `week resolves monday to sunday of current week`() {
        // 2026-09-10 is Thursday -> Monday is 2026-09-07, Sunday is 2026-09-13
        val now = LocalDateTime.of(2026, 9, 10, 10, 0).atZone(zone).toInstant()
        val window = resolver.resolve(
            period = ReportPeriod.WEEK,
            custom = ReportRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 1)),
            now = now,
            salaryConfig = SalaryCycleConfig(),
            zone = zone,
        )

        assertEquals(ReportRange(LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 13)), window.range)
        assertEquals(LocalDate.of(2026, 9, 7), window.currentStart.atZone(zone).toLocalDate())
        assertEquals(LocalDate.of(2026, 9, 14), window.currentEndExclusive.atZone(zone).toLocalDate())
        assertEquals(LocalDate.of(2026, 8, 31), window.previousStart.atZone(zone).toLocalDate())
        assertEquals(LocalDate.of(2026, 9, 7), window.previousEndExclusive.atZone(zone).toLocalDate())
    }

    @Test
    fun `last 7 days resolves rolling 7 days up to today`() {
        val now = LocalDateTime.of(2026, 9, 10, 10, 0).atZone(zone).toInstant()
        val window = resolver.resolve(
            period = ReportPeriod.LAST_7_DAYS,
            custom = ReportRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 1)),
            now = now,
            salaryConfig = SalaryCycleConfig(),
            zone = zone,
        )

        assertEquals(ReportRange(LocalDate.of(2026, 9, 4), LocalDate.of(2026, 9, 10)), window.range)
        assertEquals(LocalDate.of(2026, 9, 4), window.currentStart.atZone(zone).toLocalDate())
        assertEquals(LocalDate.of(2026, 9, 11), window.currentEndExclusive.atZone(zone).toLocalDate())
    }

    @Test
    fun `salary cycle resolves full current and immediately previous cycle`() {
        val now = LocalDateTime.of(2026, 9, 10, 12, 0).atZone(zone).toInstant()
        val config = SalaryCycleConfig(enabled = true, paydayDay = 25)

        val window = resolver.resolve(
            period = ReportPeriod.SALARY_CYCLE,
            custom = ReportRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2)),
            now = now,
            salaryConfig = config,
            zone = zone,
        )

        assertEquals(LocalDate.of(2026, 8, 25), window.range.start)
        assertEquals(LocalDate.of(2026, 9, 24), window.range.end)
        assertEquals(LocalDate.of(2026, 8, 25), window.currentStart.atZone(zone).toLocalDate())
        assertEquals(LocalDate.of(2026, 9, 25), window.currentEndExclusive.atZone(zone).toLocalDate())
        assertEquals(LocalDate.of(2026, 7, 25), window.previousStart.atZone(zone).toLocalDate())
        assertEquals(LocalDate.of(2026, 8, 25), window.previousEndExclusive.atZone(zone).toLocalDate())
    }

    @Test
    fun `calendar month behavior remains month to today with equal previous duration`() {
        val now = LocalDateTime.of(2026, 9, 10, 12, 0).atZone(zone).toInstant()
        val window = resolver.resolve(
            period = ReportPeriod.MONTH,
            custom = ReportRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2)),
            now = now,
            salaryConfig = SalaryCycleConfig(),
            zone = zone,
        )

        assertEquals(ReportRange(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 10)), window.range)
        assertEquals(LocalDate.of(2026, 9, 11), window.currentEndExclusive.atZone(zone).toLocalDate())
        assertEquals(LocalDate.of(2026, 8, 22), window.previousStart.atZone(zone).toLocalDate())
        assertEquals(LocalDate.of(2026, 9, 1), window.previousEndExclusive.atZone(zone).toLocalDate())
    }

    @Test
    fun `salary period falls back to month when salary cycle is disabled`() {
        val now = LocalDateTime.of(2026, 9, 10, 12, 0).atZone(zone).toInstant()
        val window = resolver.resolve(
            period = ReportPeriod.SALARY_CYCLE,
            custom = ReportRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2)),
            now = now,
            salaryConfig = SalaryCycleConfig(enabled = false, paydayDay = 25),
            zone = zone,
        )

        assertEquals(ReportRange(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 10)), window.range)
    }
}
