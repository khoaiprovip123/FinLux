package com.finlux.app.presentation.reports.prism

import com.finlux.app.presentation.reports.CashFlowPoint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class PrismReportsDataTest {
    @Test
    fun `cash flow buckets preserve all real income and expense values`() {
        val start = LocalDate.of(2026, 8, 1)
        val points = (0 until 40).map { index ->
            CashFlowPoint(
                date = start.plusDays(index.toLong()),
                income = (index + 1) * 1_000L,
                expense = (index + 1) * 500L,
            )
        }

        val buckets = cashFlowBuckets(points, maximumBuckets = 10)

        assertEquals(10, buckets.size)
        assertEquals(points.sumOf(CashFlowPoint::income), buckets.sumOf(CashFlowBucket::income))
        assertEquals(points.sumOf(CashFlowPoint::expense), buckets.sumOf(CashFlowBucket::expense))
        assertEquals(start, buckets.first().start)
        assertEquals(start.plusDays(39), buckets.last().end)
    }

    @Test
    fun `period change uses calculated values and handles missing previous period`() {
        val increase = periodChangeLabel(current = 120L, previous = 100L)
        val decrease = periodChangeLabel(current = 80L, previous = 100L)
        val missing = periodChangeLabel(current = 80L, previous = 0L)

        assertEquals("+20% so với kỳ trước ↗", increase.first)
        assertTrue(increase.second == true)
        assertEquals("-20% so với kỳ trước ↘", decrease.first)
        assertFalse(decrease.second == true)
        assertEquals("Chưa có dữ liệu kỳ trước", missing.first)
        assertNull(missing.second)
    }
}
