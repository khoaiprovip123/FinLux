package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.DealCategory
import com.finlux.app.domain.model.DealFlowType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.FinancialDeal
import com.finlux.app.domain.model.GoalFlowType
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CalculateFinancialFlowBreakdownUseCaseTest {
    private val calculate = CalculateFinancialFlowBreakdownUseCase()
    private val now = Instant.parse("2026-09-05T10:00:00Z")

    @Test
    fun `separates operating savings debt investment and transfer flows`() {
        val deals = listOf(
            FinancialDeal(id = "invest", title = "Đầu tư", category = DealCategory.INVESTMENT),
            FinancialDeal(id = "loan", title = "Cho vay", category = DealCategory.LENDING),
        )
        val transactions = listOf(
            tx("salary", TransactionType.INCOME, 10_000_000L),
            tx("food", TransactionType.EXPENSE, 2_000_000L),
            tx("goal-in", TransactionType.EXPENSE, 1_000_000L).copy(
                goalId = "g1",
                goalFlowType = GoalFlowType.ALLOCATION,
            ),
            tx("goal-out", TransactionType.INCOME, 300_000L).copy(
                goalId = "g1",
                goalFlowType = GoalFlowType.RELEASE,
            ),
            tx("debt", TransactionType.EXPENSE, 1_200_000L).copy(
                debtId = "d1",
                debtPrincipalAmount = Money(1_000_000L),
                debtInterestAmount = Money(200_000L),
            ),
            tx("invest-out", TransactionType.EXPENSE, 3_000_000L).copy(
                dealId = "invest",
                dealFlowType = DealFlowType.OUTLAY_CAPITAL,
            ),
            tx("invest-principal", TransactionType.INCOME, 1_500_000L).copy(
                dealId = "invest",
                dealFlowType = DealFlowType.PRINCIPAL_RECOVERY,
            ),
            tx("loan-out", TransactionType.EXPENSE, 2_000_000L).copy(
                dealId = "loan",
                dealFlowType = DealFlowType.OUTLAY_CAPITAL,
            ),
            tx("loan-principal", TransactionType.INCOME, 500_000L).copy(
                dealId = "loan",
                dealFlowType = DealFlowType.PRINCIPAL_RECOVERY,
            ),
            tx("gain", TransactionType.INCOME, 400_000L).copy(
                dealId = "invest",
                dealFlowType = DealFlowType.CAPITAL_GAIN,
            ),
            tx("transfer-in", TransactionType.TRANSFER_IN, 700_000L),
            tx("transfer-out", TransactionType.TRANSFER_OUT, 600_000L),
        )

        val result = calculate(transactions, deals)

        assertEquals(10_000_000L, result.operatingIncome)
        assertEquals(2_200_000L, result.operatingExpense)
        assertEquals(1_000_000L, result.goalAllocation)
        assertEquals(300_000L, result.goalRelease)
        assertEquals(1_000_000L, result.debtPrincipalOutflow)
        assertEquals(200_000L, result.debtInterestExpense)
        assertEquals(3_000_000L, result.investmentOutlay)
        assertEquals(1_500_000L, result.investmentPrincipalRecovery)
        assertEquals(2_000_000L, result.lendingOutlay)
        assertEquals(500_000L, result.lendingPrincipalRecovery)
        assertEquals(400_000L, result.dealGain)
        assertEquals(700_000L, result.transferIn)
        assertEquals(600_000L, result.transferOut)

        // 10M + .7 + .3 + 1.5 + .5 + .4 - 2.2 - .6 - 1 - 1 - 3 - 2 = 3.6M
        assertEquals(3_600_000L, result.netCashMovement)
    }

    private fun tx(id: String, type: TransactionType, amount: Long) = FinanceTransaction(
        id = id,
        type = type,
        amount = Money(amount),
        categoryId = null,
        walletId = "w1",
        date = now,
    )
}
