package com.finlux.app.domain.model

/**
 * Canonical operating P&L semantics used consistently by Home, Reports and Budget views.
 *
 * Internal asset allocation, liability principal and investment capital movements affect cash
 * but are not operating income/expense.
 */
fun FinanceTransaction.operatingIncomeAmount(): Long = when {
    type != TransactionType.INCOME -> 0L
    goalFlowType == GoalFlowType.RELEASE -> 0L
    dealFlowType == DealFlowType.PRINCIPAL_RECOVERY -> 0L
    dealFlowType == DealFlowType.CAPITAL_GAIN -> 0L
    else -> amount.value
}

fun FinanceTransaction.operatingExpenseAmount(): Long = when {
    type != TransactionType.EXPENSE -> 0L
    goalFlowType == GoalFlowType.ALLOCATION -> 0L
    debtId != null &&
        debtPrincipalAmount != null &&
        debtInterestAmount != null -> debtInterestAmount.value
    dealFlowType == DealFlowType.OUTLAY_CAPITAL -> 0L
    dealFlowType == DealFlowType.CAPITAL_LOSS -> 0L
    else -> amount.value
}
