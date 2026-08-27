package com.finlux.app.core.designsystem

import com.finlux.app.core.designsystem.component.generateAmountSuggestions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AmountSuggestionsTest {

    @Test
    fun `generateAmountSuggestions with empty or zero input returns 8 standard presets`() {
        val emptyResult = generateAmountSuggestions("")
        assertEquals(8, emptyResult.size)
        assertEquals("50.000" to "50000", emptyResult[0])
        assertEquals("100.000" to "100000", emptyResult[1])
        assertEquals("200.000" to "200000", emptyResult[2])
        assertEquals("500.000" to "500000", emptyResult[3])
        assertEquals("1.000.000" to "1000000", emptyResult[4])
        assertEquals("2.000.000" to "2000000", emptyResult[5])
        assertEquals("5.000.000" to "5000000", emptyResult[6])
        assertEquals("10.000.000" to "10000000", emptyResult[7])

        val zeroResult = generateAmountSuggestions("0")
        assertEquals(emptyResult, zeroResult)
    }

    @Test
    fun `generateAmountSuggestions with single digit 3 generates 3k to 30M`() {
        val result = generateAmountSuggestions("3")
        val expected = listOf(
            "3.000" to "3000",
            "30.000" to "30000",
            "300.000" to "300000",
            "3.000.000" to "3000000",
            "30.000.000" to "30000000",
        )
        assertEquals(expected, result)
    }

    @Test
    fun `generateAmountSuggestions with 2 digits 35 generates 3_5k to 35M including x100`() {
        val result = generateAmountSuggestions("35")
        val expected = listOf(
            "3.500" to "3500",
            "35.000" to "35000",
            "350.000" to "350000",
            "3.500.000" to "3500000",
            "35.000.000" to "35000000",
        )
        assertEquals(expected, result)
    }

    @Test
    fun `generateAmountSuggestions with 3 digits 356 generates 3_56k to 35_6M including x10 and x100`() {
        val result = generateAmountSuggestions("356")
        val expected = listOf(
            "3.560" to "3560",
            "35.600" to "35600",
            "356.000" to "356000",
            "3.560.000" to "3560000",
            "35.600.000" to "35600000",
        )
        assertEquals(expected, result)
    }

    @Test
    fun `generateAmountSuggestions with 4 digits 3568 generates 35_68k to 356_8M`() {
        val result = generateAmountSuggestions("3568")
        val expected = listOf(
            "35.680" to "35680",
            "356.800" to "356800",
            "3.568.000" to "3568000",
            "35.680.000" to "35680000",
            "356.800.000" to "356800000",
        )
        assertEquals(expected, result)
    }

    @Test
    fun `generateAmountSuggestions with large number respects 1 billion limit`() {
        val result = generateAmountSuggestions("500000000") // 500 million
        assertEquals(1, result.size)
        assertEquals("500.000.000" to "500000000", result[0])
    }
}
