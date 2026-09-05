package com.finlux.app.domain.model

/**
 * Canonical semantic breakdown of ledger movements for reporting.
 *
 * Cash movement and profit/expense semantics are intentionally separated:
 * - Goal allocation/release moves cash but is not operating income/expense.
 * - Debt principal moves cash and reduces a liability; only interest is operating expense.
 * - Deal capital/principal movements are investing/financing flows.
 */
data class FinancialFlowBreakdown(
    val operatingIncome: Long = 0L,
    val operatingExpense: Long = 0L,
    val transferIn: Long = 0L,
    val transferOut: Long = 0L,
    val goalAllocation: Long = 0L,
    val goalRelease: Long = 0L,
    val debtPrincipalOutflow: Long = 0L,
    val debtInterestExpense: Long = 0L,
    val investmentOutlay: Long = 0L,
    val investmentPrincipalRecovery: Long = 0L,
    val lendingOutlay: Long = 0L,
    val lendingPrincipalRecovery: Long = 0L,
    val dealGain: Long = 0L,
    val dealLoss: Long = 0L,
) {
    val operatingNet: Long get() = operatingIncome - operatingExpense

    val netGoalAllocation: Long get() = goalAllocation - goalRelease

    val netCashMovement: Long
        get() = operatingIncome +
            transferIn +
            goalRelease +
            investmentPrincipalRecovery +
            lendingPrincipalRecovery +
            dealGain -
            operatingExpense -
            transferOut -
            goalAllocation -
            debtPrincipalOutflow -
            investmentOutlay -
            lendingOutlay

    val dealProfitAndLoss: Long get() = dealGain - dealLoss
}
