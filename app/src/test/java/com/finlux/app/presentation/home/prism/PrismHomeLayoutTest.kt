package com.finlux.app.presentation.home.prism

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PrismHomeLayoutTest {

    @Test
    fun `metric amount font size scales down at stable length thresholds`() {
        assertEquals(18.0f, prismMetricAmountFontSizeSp("798.565 đ"))
        assertEquals(18.0f, prismMetricAmountFontSizeSp("7.500.000 đ"))
        assertEquals(16.5f, prismMetricAmountFontSizeSp("125.450.000 đ"))
        assertEquals(13.5f, prismMetricAmountFontSizeSp("+1.250.450.000 đ"))
    }
}
