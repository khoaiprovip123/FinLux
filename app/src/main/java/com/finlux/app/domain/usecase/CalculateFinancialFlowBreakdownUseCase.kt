package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.DealCategory
import com.finlux.app.domain.model.DealFlowType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.FinancialDeal
import com.finlux.app.domain.model.FinancialFlowBreakdown
import com.finlux.app.domain.model.GoalFlowType
import com.finlux.app.domain.model.TransactionType
import javax.inject.Inject

class CalculateFinancialFlowBreakdownUseCase @Inject constructor() {
    operator fun invoke(
        transactions: List<FinanceTransaction>,
        deals: List<FinancialDeal> = emptyList(),
    ): FinancialFlowBreakdown {
        val dealMap = deals.associateBy { it.id }

        var operatingIncome = 0L
        var operatingExpense = 0L
        var transferIn = 0L
        var transferOut = 0L
        var goalAllocation = 0L
        var goalRelease = 0L
        var debtPrincipalOutflow = 0L
        var debtInterestExpense = 0L
        var investmentOutlay = 0L
        var investmentPrincipalRecovery = 0L
        var lendingOutlay = 0L
        var lendingPrincipalRecovery = 0L
        var dealGain = 0L
        var dealLoss = 0L

        for (tx in transactions) {
            when (tx.type) {
                TransactionType.TRANSFER_IN -> transferIn += tx.amount.value
                TransactionType.TRANSFER_OUT -> transferOut += tx.amount.value

                TransactionType.INCOME -> when {
                    tx.goalFlowType == GoalFlowType.RELEASE -> {
                        goalRelease += tx.amount.value
                    }

                    tx.dealFlowType == DealFlowType.PRINCIPAL_RECOVERY -> {
                        val deal = tx.dealId?.let(dealMap::get)
                        if (deal?.category == DealCategory.LENDING) {
                            lendingPrincipalRecovery += tx.amount.value
                        } else {
                            investmentPrincipalRecovery += tx.amount.value
                        }
                    }

                    tx.dealFlowType == DealFlowType.CAPITAL_GAIN -> {
                        dealGain += tx.amount.value
                    }

                    else -> operatingIncome += tx.amount.value
                }

                TransactionType.EXPENSE -> when {
                    tx.goalFlowType == GoalFlowType.ALLOCATION -> {
                        goalAllocation += tx.amount.value
                    }

                    tx.debtId != null &&
                        tx.debtPrincipalAmount != null &&
                        tx.debtInterestAmount != null -> {
                        debtPrincipalOutflow += tx.debtPrincipalAmount.value
                        debtInterestExpense += tx.debtInterestAmount.value
                        operatingExpense += tx.debtInterestAmount.value
                    }

                    tx.dealFlowType == DealFlowType.OUTLAY_CAPITAL -> {
                        val deal = tx.dealId?.let(dealMap::get)
                        if (deal?.category == DealCategory.LENDING) {
                            lendingOutlay += tx.amount.value
                        } else {
                            investmentOutlay += tx.amount.value
                        }
                    }

                    tx.dealFlowType == DealFlowType.CAPITAL_LOSS -> {
                        // Settlement loss is P&L, not a new wallet cash outflow.
                        dealLoss += tx.amount.value
                    }

                    else -> operatingExpense += tx.amount.value
                }
            }
        }

        return FinancialFlowBreakdown(
            operatingIncome = operatingIncome,
            operatingExpense = operatingExpense,
            transferIn = transferIn,
            transferOut = transferOut,
            goalAllocation = goalAllocation,
            goalRelease = goalRelease,
            debtPrincipalOutflow = debtPrincipalOutflow,
            debtInterestExpense = debtInterestExpense,
            investmentOutlay = investmentOutlay,
            investmentPrincipalRecovery = investmentPrincipalRecovery,
            lendingOutlay = lendingOutlay,
            lendingPrincipalRecovery = lendingPrincipalRecovery,
            dealGain = dealGain,
            dealLoss = dealLoss,
        )
    }
}
