package com.finlux.app.core.designsystem

import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.component.getTransactionSemanticColor
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.PrismDarkTokens
import com.finlux.app.core.designsystem.theme.PrismLightTokens
import com.finlux.app.domain.model.TransactionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FinluxDesignSystemTest {

    @Test
    fun `formatVndAmount formats standard amounts correctly`() {
        assertEquals("0 đ", formatVndAmount(0L))
        assertEquals("65.000 đ", formatVndAmount(65_000L))
        assertEquals("2.265.000 đ", formatVndAmount(2_265_000L))
        assertEquals("6.110.000 đ", formatVndAmount(6_110_000L))
    }

    @Test
    fun `formatVndAmount formats compact amounts correctly for millions`() {
        assertEquals("2,3 tr", formatVndAmount(2_300_000L, isCompact = true).replace('.', ','))
        assertEquals("1 tr", formatVndAmount(1_000_000L, isCompact = true).replace('.', ','))
        assertEquals("500.000 đ", formatVndAmount(500_000L, isCompact = true))
    }

    @Test
    fun `getTransactionSemanticColor strictly adheres to semantic colors`() {
        assertEquals(FinluxColors.IncomeGreen, getTransactionSemanticColor(TransactionType.INCOME))
        assertEquals(FinluxColors.ExpenseRed, getTransactionSemanticColor(TransactionType.EXPENSE))
        assertEquals(FinluxColors.TransferBlue, getTransactionSemanticColor(TransactionType.TRANSFER_OUT))
        assertEquals(FinluxColors.TransferBlue, getTransactionSemanticColor(TransactionType.TRANSFER_IN))
    }

    @Test
    fun `Prism tokens provide correct light and dark attributes`() {
        assertFalse(PrismLightTokens.isDark)
        assertTrue(PrismDarkTokens.isDark)

        assertEquals(FinluxColors.BackgroundLight, PrismLightTokens.background)
        assertEquals(FinluxColors.BackgroundDark, PrismDarkTokens.background)

        assertEquals(FinluxColors.SurfacePrimaryLight, PrismLightTokens.surface)
        assertEquals(FinluxColors.SurfacePrimaryDark, PrismDarkTokens.surface)
    }
}
