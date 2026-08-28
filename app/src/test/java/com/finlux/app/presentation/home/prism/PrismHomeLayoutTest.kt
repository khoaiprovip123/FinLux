package com.finlux.app.presentation.home.prism

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PrismHomeLayoutTest {

    @Test
    fun `metric amount font size scales down at stable length thresholds`() {
        assertEquals(27.0f, prismMetricAmountFontSizeSp("798.565 đ"))
        assertEquals(27.0f, prismMetricAmountFontSizeSp("7.500.000 đ"))
        assertEquals(24.0f, prismMetricAmountFontSizeSp("125.450.000 đ"))
        assertEquals(20.0f, prismMetricAmountFontSizeSp("+1.250.450.000 đ"))
    }

    @Test
    fun `summary carousel advances and wraps after third page`() {
        assertEquals(1, nextPrismSummaryPage(0))
        assertEquals(2, nextPrismSummaryPage(1))
        assertEquals(0, nextPrismSummaryPage(2))
        assertEquals(10_000L, PRISM_SUMMARY_AUTO_ADVANCE_MS)
    }
}
