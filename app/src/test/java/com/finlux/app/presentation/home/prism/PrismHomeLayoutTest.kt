package com.finlux.app.presentation.home.prism

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PrismHomeLayoutTest {

    @Test
    fun `home profile header stays compact`() {
        assertEquals(52, PRISM_HOME_HEADER_HEIGHT_DP)
    }

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

    @Test
    fun `financial overview advances through four themed pages and wraps`() {
        assertEquals(1, nextPrismOverviewPage(0))
        assertEquals(2, nextPrismOverviewPage(1))
        assertEquals(3, nextPrismOverviewPage(2))
        assertEquals(0, nextPrismOverviewPage(3))
        assertEquals(10_000L, PRISM_SUMMARY_AUTO_ADVANCE_MS)
    }

    @Test
    fun `financial overview amount keeps a readable single line size`() {
        assertEquals(180, PRISM_FINANCIAL_HERO_HEIGHT_DP)
        assertEquals(38.0f, prismOverviewAmountFontSizeSp("6.463.435 ₫"))
        assertEquals(35.0f, prismOverviewAmountFontSizeSp("125.450.000 ₫"))
        assertEquals(32.0f, prismOverviewAmountFontSizeSp("+1.250.450.000 ₫"))
        assertEquals(28.0f, prismOverviewAmountFontSizeSp("+12.250.450.000 ₫"))
    }

    @Test
    fun `computePeriodBars handles empty and normal transactions correctly`() {
        val emptyBars = computePeriodBars(emptyList(), null, 5)
        assertEquals(listOf(0L, 0L, 0L, 0L, 0L), emptyBars)
    }
}
