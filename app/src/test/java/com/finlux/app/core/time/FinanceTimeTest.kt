package com.finlux.app.core.time

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

class FinanceTimeTest {
    private val vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh")

    @Test
    fun `default finance zone is stable Vietnam timezone`() {
        assertEquals(vietnamZone, FinanceTime.defaultZone)
    }

    @Test
    fun `zoneOf returns requested valid zone and falls back to Vietnam for invalid id`() {
        assertEquals(ZoneId.of("Asia/Tokyo"), FinanceTime.zoneOf("Asia/Tokyo"))
        assertEquals(vietnamZone, FinanceTime.zoneOf("Not/A_Zone"))
        assertEquals(vietnamZone, FinanceTime.zoneOf(""))
    }

    @Test
    fun `financialMonth maps boundary dates correctly in Asia_Ho_Chi_Minh`() {
        val sepInstant = LocalDateTime.of(2026, 9, 1, 0, 30, 0)
            .atZone(vietnamZone)
            .toInstant()
        val sepMonth = FinanceTime.financialMonth(sepInstant, vietnamZone)
        assertEquals(YearMonth.of(2026, 9), sepMonth)

        val augInstant = LocalDateTime.of(2026, 8, 31, 23, 30, 0)
            .atZone(vietnamZone)
            .toInstant()
        val augMonth = FinanceTime.financialMonth(augInstant, vietnamZone)
        assertEquals(YearMonth.of(2026, 8), augMonth)
    }

    @Test
    fun `monthStart and monthEnd boundary computation`() {
        val ym = YearMonth.of(2026, 8)
        val start = FinanceTime.monthStart(ym, vietnamZone)
        val end = FinanceTime.monthEnd(ym, vietnamZone)

        assertEquals(
            LocalDateTime.of(2026, 8, 1, 0, 0, 0).atZone(vietnamZone).toInstant(),
            start,
        )
        assertEquals(
            LocalDateTime.of(2026, 9, 1, 0, 0, 0).atZone(vietnamZone).toInstant(),
            end,
        )
    }
}
