package com.finlux.app.domain.validation

import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType

/**
 * Result of validating whether a wallet has sufficient funds or credit limit
 * for a planned money outflow (Expense, Transfer, Debt Payment, Goal Deposit, Deal Outlay).
 */
sealed interface WalletValidationResult {
    /** Valid: Sufficient funds or credit limit available. */
    data object Valid : WalletValidationResult

    /** Standard wallet (Cash, Bank, E-wallet, Investment, Other) has insufficient balance. */
    data class InsufficientFunds(
        val available: Long,
        val required: Long,
        val walletName: String,
    ) : WalletValidationResult {
        val shortage: Long get() = (required - available).coerceAtLeast(0L)
    }

    /** Credit card expense exceeds total credit limit. */
    data class CreditLimitExceeded(
        val creditLimit: Long,
        val currentDebt: Long,
        val newDebt: Long,
    ) : WalletValidationResult {
        val excessAmount: Long get() = (newDebt - creditLimit).coerceAtLeast(0L)
    }
}

/**
 * Shared validator for money outflow transactions across the domain layer.
 */
object WalletBalanceValidator {

    /**
     * Validates if the selected [wallet] can afford the specified [amount].
     *
     * @param wallet The source wallet to deduct funds from.
     * @param amount The money outflow amount (in VND).
     * @param isExpense True if this is an outflow (expense/transfer-out/payment), false if income/deposit.
     * @param creditLimit The maximum allowed credit limit if wallet is a [WalletType.CARD].
     * @param rollbackAmount Any refunded amount from an existing transaction being edited (to restore balance preview).
     */
    fun validate(
        wallet: Wallet?,
        amount: Long,
        isExpense: Boolean = true,
        creditLimit: Long? = null,
        rollbackAmount: Long = 0L,
    ): WalletValidationResult {
        // If not an expense/outflow or amount <= 0, no balance check required
        if (wallet == null || !isExpense || amount <= 0L) {
            return WalletValidationResult.Valid
        }

        // Credit Card logic
        if (wallet.type == WalletType.CARD) {
            if (creditLimit != null && creditLimit > 0L) {
                // In Finlux, credit card balance can be negative (representing debt) or positive
                val currentDebt = (kotlin.math.abs(wallet.balance.value) - rollbackAmount).coerceAtLeast(0L)
                val newDebt = currentDebt + amount
                if (newDebt > creditLimit) {
                    return WalletValidationResult.CreditLimitExceeded(
                        creditLimit = creditLimit,
                        currentDebt = currentDebt,
                        newDebt = newDebt,
                    )
                }
            }
            return WalletValidationResult.Valid
        }

        // Standard wallet logic: Cash, Bank, E-wallet, Investment, Other
        val effectiveBalance = wallet.balance.value + rollbackAmount
        if (effectiveBalance <= 0L || amount > effectiveBalance) {
            return WalletValidationResult.InsufficientFunds(
                available = effectiveBalance,
                required = amount,
                walletName = wallet.name,
            )
        }

        return WalletValidationResult.Valid
    }
}

/**
 * Convenience Kotlin Extension function on [Wallet] for validating sufficient funds.
 */
fun Wallet.validateSufficientFunds(
    amount: Long,
    isExpense: Boolean = true,
    creditLimit: Long? = null,
    rollbackAmount: Long = 0L,
): WalletValidationResult = WalletBalanceValidator.validate(
    wallet = this,
    amount = amount,
    isExpense = isExpense,
    creditLimit = creditLimit,
    rollbackAmount = rollbackAmount,
)
