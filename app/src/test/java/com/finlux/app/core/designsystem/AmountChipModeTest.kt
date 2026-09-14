package com.finlux.app.core.designsystem

import com.finlux.app.core.designsystem.component.form.AmountChipMode
import com.finlux.app.core.designsystem.component.form.formatChipAmountLabel
import com.finlux.app.core.designsystem.component.form.generateMagnitudeSuggestions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AmountChipModeTest {

    @Test
    fun `formatChipAmountLabel formats incremental labels with plus prefix`() {
        assertEquals("+50k", formatChipAmountLabel(50_000L, isIncremental = true))
        assertEquals("+100k", formatChipAmountLabel(100_000L, isIncremental = true))
        assertEquals("+500k", formatChipAmountLabel(500_000L, isIncremental = true))
        assertEquals("+1tr", formatChipAmountLabel(1_000_000L, isIncremental = true))
        assertEquals("+2tr", formatChipAmountLabel(2_000_000L, isIncremental = true))
        assertEquals("+2.5tr", formatChipAmountLabel(2_500_000L, isIncremental = true))
        assertEquals("+1 tỷ", formatChipAmountLabel(1_000_000_000L, isIncremental = true))
    }

    @Test
    fun `formatChipAmountLabel formats absolute labels without plus prefix`() {
        assertEquals("50k", formatChipAmountLabel(50_000L, isIncremental = false))
        assertEquals("100k", formatChipAmountLabel(100_000L, isIncremental = false))
        assertEquals("1tr", formatChipAmountLabel(1_000_000L, isIncremental = false))
        assertEquals("10tr", formatChipAmountLabel(10_000_000L, isIncremental = false))
        assertEquals("2 tỷ", formatChipAmountLabel(2_000_000_000L, isIncremental = false))
    }

    @Test
    fun `generateMagnitudeSuggestions returns valid scales for single digit 3`() {
        val suggestions = generateMagnitudeSuggestions("3")
        assertTrue(suggestions.isNotEmpty())
        assertEquals(3_000L, suggestions[0])
        assertEquals(30_000L, suggestions[1])
        assertEquals(300_000L, suggestions[2])
        assertEquals(3_000_000L, suggestions[3])
    }

    @Test
    fun `generateMagnitudeSuggestions with empty or zero input returns standard presets`() {
        val emptyPresets = generateMagnitudeSuggestions("")
        assertEquals(8, emptyPresets.size)
        assertEquals(50_000L, emptyPresets[0])
        assertEquals(10_000_000L, emptyPresets[7])

        val zeroPresets = generateMagnitudeSuggestions("0")
        assertEquals(emptyPresets, zeroPresets)
    }

    @Test
    fun `AmountChipMode enum values are properly defined`() {
        val modes = AmountChipMode.values()
        assertEquals(3, modes.size)
        assertTrue(modes.contains(AmountChipMode.INCREMENTAL))
        assertTrue(modes.contains(AmountChipMode.REPLACE_VALUE))
        assertTrue(modes.contains(AmountChipMode.MAGNITUDE_SCALING))
    }
}
