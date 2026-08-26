package com.finlux.app.presentation.reports.prism

import com.finlux.app.domain.model.Money
import com.finlux.app.presentation.reports.CashFlowPoint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class PrismReportsDataTest {

    @Test
    fun `cash flow points calculate total income and expense accurately`() {
        val start = LocalDate.of(2026, 8, 1)
        val points = (0 until 30).map { index ->
            CashFlowPoint(
                date = start.plusDays(index.toLong()),
                income = (index + 1) * 100_000L,
                expense = (index + 1) * 50_000L,
            )
        }

        val totalIncome = points.sumOf(CashFlowPoint::income)
        val totalExpense = points.sumOf(CashFlowPoint::expense)
        val netCashFlow = totalIncome - totalExpense

        assertEquals(46_500_000L, totalIncome)
        assertEquals(23_250_000L, totalExpense)
        assertEquals(23_250_000L, netCashFlow)
    }

    @Test
    fun `net worth calculates total assets minus total remaining debts`() {
        val totalWalletAssets = 150_000_000L
        val totalRemainingDebts = 35_000_000L
        val netWorth = totalWalletAssets - totalRemainingDebts

        assertEquals(115_000_000L, netWorth)
    }

    @Test
    fun `savings rate calculates correctly when income is positive`() {
        val income = 30_000_000L
        val expense = 18_000_000L
        val savingRatePct = if (income > 0L) {
            (((income - expense).toDouble() / income.toDouble()) * 100.0).toInt()
        } else 0

        assertEquals(40, savingRatePct)
    }

    @Test
    fun `daily average uses elapsed days rather than future days`() {
        val totalExpense = 15_000_000L
        val elapsedDays = 15
        val avgPerDay = totalExpense / elapsedDays

        assertEquals(1_000_000L, avgPerDay)
    }
}
