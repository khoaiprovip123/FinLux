package com.finlux.app.presentation.debt

import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtCashflowAnalysis
import com.finlux.app.domain.model.DebtPayoffPlan
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.PayoffStrategy
import com.finlux.app.domain.model.Wallet

data class DebtUiState(
    val debts: List<DebtAccount> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val strategy: PayoffStrategy = PayoffStrategy.SNOWBALL,
    val extraMonthlyPayment: Long = 0L,
    val payoffPlan: DebtPayoffPlan? = null,
    val cashflowAnalysis: DebtCashflowAnalysis? = null,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    val totalRemainingDebt: Money
        get() = Money(debts.filter { !it.isSettled }.sumOf { it.remainingBalance.value })

    val totalOriginalDebt: Money
        get() = Money(debts.sumOf { it.totalAmount.value })

    val totalPaidDebt: Money
        get() = Money((totalOriginalDebt.value - totalRemainingDebt.value).coerceAtLeast(0L))

    val overallProgress: Float
        get() = if (totalOriginalDebt.value <= 0L) 0f
        else (totalPaidDebt.value.toFloat() / totalOriginalDebt.value.toFloat()).coerceIn(0f, 1f)

    val activeDebtsCount: Int
        get() = debts.count { !it.isSettled && it.remainingBalance.value > 0L }

    val totalMonthlyMinimum: Money
        get() = Money(debts.filter { !it.isSettled }.sumOf { it.minimumPayment.value })
}
