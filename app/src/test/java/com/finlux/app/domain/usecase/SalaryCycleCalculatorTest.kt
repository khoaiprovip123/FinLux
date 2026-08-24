package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.FinancialCycle
import com.finlux.app.domain.model.PaydayRuleType
import com.finlux.app.domain.model.SalaryCycleConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class SalaryCycleCalculatorTest {
    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private val calculator = DefaultSalaryCycleCalculator()

    @Test
    fun `payday 25 creates 25 Aug through before 25 Sep`() {
        val cycle = cycleFor(day = 25, localDateTime = LocalDateTime.of(2026, 9, 10, 12, 0))
        assertDates(cycle, LocalDate.of(2026, 8, 25), LocalDate.of(2026, 9, 25))
    }

    @Test
    fun `configured fixed paydays produce deterministic boundaries`() {
        listOf(1, 10, 15, 20, 25, 28, 29, 30, 31).forEach { payday ->
            val instant = LocalDateTime.of(2026, 5, payday.coerceAtMost(30), 12, 0).atZone(zone).toInstant()
            val cycle = calculator.cycleContaining(instant, SalaryCycleConfig(enabled = true, paydayDay = payday), zone)
            val expectedStartDay = payday.coerceAtMost(31)
            assertEquals(expectedStartDay, cycle.start.atZone(zone).dayOfMonth)
        }
    }

    @Test
    fun `payday 31 clamps non leap February to final valid day`() {
        val cycle = cycleFor(day = 31, localDateTime = LocalDateTime.of(2027, 2, 28, 12, 0))
        assertEquals(LocalDate.of(2027, 2, 28), cycle.start.atZone(zone).toLocalDate())
    }

    @Test
    fun `payday 30 clamps leap February to February 29`() {
        val cycle = cycleFor(day = 30, localDateTime = LocalDateTime.of(2028, 2, 29, 12, 0))
        assertEquals(LocalDate.of(2028, 2, 29), cycle.start.atZone(zone).toLocalDate())
    }

    @Test
    fun `first day rule always starts on first of month`() {
        val config = SalaryCycleConfig(enabled = true, paydayRuleType = PaydayRuleType.FIRST_DAY_OF_MONTH, paydayDay = 25)
        val instant = LocalDateTime.of(2026, 8, 14, 9, 0).atZone(zone).toInstant()
        val cycle = calculator.cycleContaining(instant, config, zone)
        assertDates(cycle, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 9, 1))
    }

    @Test
    fun `last day rule uses each month final day`() {
        val config = SalaryCycleConfig(enabled = true, paydayRuleType = PaydayRuleType.LAST_DAY_OF_MONTH)
        val instant = LocalDateTime.of(2026, 9, 15, 9, 0).atZone(zone).toInstant()
        val cycle = calculator.cycleContaining(instant, config, zone)
        assertDates(cycle, LocalDate.of(2026, 8, 31), LocalDate.of(2026, 9, 30))
    }

    @Test
    fun `instant exactly on boundary belongs to new cycle`() {
        val boundary = LocalDateTime.of(2026, 9, 25, 0, 0).atZone(zone).toInstant()
        val cycle = calculator.cycleContaining(boundary, SalaryCycleConfig(enabled = true, paydayDay = 25), zone)
        assertDates(cycle, LocalDate.of(2026, 9, 25), LocalDate.of(2026, 10, 25))
    }

    @Test
    fun `instant one millisecond before boundary belongs to previous cycle`() {
        val instant = LocalDateTime.of(2026, 9, 25, 0, 0).atZone(zone).toInstant().minusMillis(1)
        val cycle = calculator.cycleContaining(instant, SalaryCycleConfig(enabled = true, paydayDay = 25), zone)
        assertDates(cycle, LocalDate.of(2026, 8, 25), LocalDate.of(2026, 9, 25))
    }

    @Test
    fun `previous cycle is immediately preceding derived cycle`() {
        val current = cycleFor(day = 25, localDateTime = LocalDateTime.of(2026, 9, 30, 12, 0))
        val previous = calculator.previousCycle(current, SalaryCycleConfig(enabled = true, paydayDay = 25), zone)
        assertDates(previous, LocalDate.of(2026, 8, 25), LocalDate.of(2026, 9, 25))
    }

    @Test
    fun `Vietnam midnight boundary is evaluated in finance timezone`() {
        val instant = LocalDateTime.of(2026, 10, 1, 0, 0).atZone(zone).toInstant()
        val cycle = calculator.cycleContaining(
            instant,
            SalaryCycleConfig(enabled = true, paydayRuleType = PaydayRuleType.FIRST_DAY_OF_MONTH),
            zone,
        )
        assertDates(cycle, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 11, 1))
    }

    private fun cycleFor(day: Int, localDateTime: LocalDateTime): FinancialCycle = calculator.cycleContaining(
        localDateTime.atZone(zone).toInstant(),
        SalaryCycleConfig(enabled = true, paydayDay = day),
        zone,
    )

    private fun assertDates(cycle: FinancialCycle, expectedStart: LocalDate, expectedEndExclusive: LocalDate) {
        assertEquals(expectedStart, cycle.start.atZone(zone).toLocalDate())
        assertEquals(expectedEndExclusive, cycle.endExclusive.atZone(zone).toLocalDate())
    }
}
