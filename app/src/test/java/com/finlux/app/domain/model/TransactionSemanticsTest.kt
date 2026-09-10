package com.finlux.app.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class TransactionSemanticsTest {

    @Test
    fun `paired transfer is presented as one logical outgoing row`() {
        val outgoing = transaction("transfer-1_out", TransactionType.TRANSFER_OUT, walletId = "cash", relatedWalletId = "bank")
        val incoming = transaction("transfer-1_in", TransactionType.TRANSFER_IN, walletId = "bank", relatedWalletId = "cash")

        assertEquals(listOf(outgoing), listOf(outgoing, incoming).collapseInternalTransferPairs())
    }

    @Test
    fun `orphan incoming transfer remains visible`() {
        val incoming = transaction("transfer-1_in", TransactionType.TRANSFER_IN, walletId = "bank", relatedWalletId = "cash")

        assertEquals(listOf(incoming), listOf(incoming).collapseInternalTransferPairs())
    }

    @Test
    fun `asset total excludes cards and archived wallets`() {
        val wallets = listOf(
            wallet("cash", WalletType.CASH, 2_000_000L),
            wallet("bank", WalletType.BANK, 8_000_000L),
            wallet("card", WalletType.CARD, 20_000_000L),
            wallet("archived", WalletType.CASH, 5_000_000L, status = "archived"),
        )

        assertEquals(10_000_000L, wallets.totalAssetBalance())
    }

    @Test
    fun `goal contribution is deposits minus withdrawals and never negative`() {
        val transactions = listOf(
            transaction("deposit", TransactionType.EXPENSE, categoryId = "savings", amount = 3_000_000L),
            transaction("withdraw", TransactionType.INCOME, categoryId = "savings", amount = 1_000_000L),
            transaction("food", TransactionType.EXPENSE, categoryId = "food", amount = 500_000L),
        )

        assertEquals(2_000_000L, transactions.netGoalContribution())
        assertEquals(0L, listOf(transaction("withdraw", TransactionType.INCOME, categoryId = "savings", amount = 5_000_000L)).netGoalContribution())
    }

    @Test
    fun `goal contribution supports custom category matcher`() {
        val transactions = listOf(
            transaction("dep-custom", TransactionType.EXPENSE, categoryId = "custom-203", amount = 1_500_000L),
            transaction("dep-other", TransactionType.EXPENSE, categoryId = "food", amount = 200_000L),
        )

        assertEquals(1_500_000L, transactions.netGoalContribution { it == "custom-203" })
    }

    @Test
    fun `debt principal settlement recognizes debt_principal and default debt_payment without interest keyword`() {
        val legacyPrincipalTx = transaction("tx-1", TransactionType.EXPENSE, categoryId = DEBT_PRINCIPAL_CATEGORY_ID)
        val standardizedPrincipalTx = transaction("tx-2", TransactionType.EXPENSE, categoryId = DEBT_PAYMENT_CATEGORY_ID, note = "Thanh toán nợ: Vay mua nhà")
        val interestPaymentTx = transaction("tx-3", TransactionType.EXPENSE, categoryId = DEBT_PAYMENT_CATEGORY_ID, note = "Trả lãi khoản vay: Vay mua nhà")
        val normalExpenseTx = transaction("tx-4", TransactionType.EXPENSE, categoryId = "food")

        assertTrue(legacyPrincipalTx.isDebtPrincipalSettlement())
        assertTrue(standardizedPrincipalTx.isDebtPrincipalSettlement())
        assertFalse(interestPaymentTx.isDebtPrincipalSettlement())
        assertFalse(normalExpenseTx.isDebtPrincipalSettlement())
    }

    @Test
    fun `debt interest recognizes debt_interest and debt_payment with interest keyword`() {
        val legacyInterestTx = transaction("tx-1", TransactionType.EXPENSE, categoryId = DEBT_INTEREST_CATEGORY_ID)
        val interestPaymentTx = transaction("tx-2", TransactionType.EXPENSE, categoryId = DEBT_PAYMENT_CATEGORY_ID, note = "Trả tiền lãi tháng 9")
        val principalTx = transaction("tx-3", TransactionType.EXPENSE, categoryId = DEBT_PAYMENT_CATEGORY_ID, note = "Thanh toán nợ: Vay mua xe")

        assertTrue(legacyInterestTx.isDebtInterest())
        assertTrue(interestPaymentTx.isDebtInterest())
        assertFalse(principalTx.isDebtInterest())
    }

    @Test
    fun `isLivingExpense correctly includes daily spending and loan interest, but excludes principal and capital outlay`() {
        val foodExpense = transaction("food", TransactionType.EXPENSE, categoryId = "food", amount = 200_000L)
        val loanInterestExpense = transaction("interest", TransactionType.EXPENSE, categoryId = DEBT_PAYMENT_CATEGORY_ID, note = "Trả tiền lãi vay", amount = 500_000L)
        val loanPrincipalPayment = transaction("principal", TransactionType.EXPENSE, categoryId = DEBT_PAYMENT_CATEGORY_ID, note = "Thanh toán nợ: Vay mua xe", amount = 5_000_000L)
        val legacyPrincipalPayment = transaction("legacy_principal", TransactionType.EXPENSE, categoryId = DEBT_PRINCIPAL_CATEGORY_ID, amount = 3_000_000L)
        val capitalOutlayDeal = transaction("outlay", TransactionType.EXPENSE, dealFlowType = DealFlowType.OUTLAY_CAPITAL, amount = 10_000_000L)
        val incomeSalary = transaction("salary", TransactionType.INCOME, categoryId = "salary", amount = 20_000_000L)
        val transferOut = transaction("transfer", TransactionType.TRANSFER_OUT, amount = 1_000_000L)

        assertTrue(foodExpense.isLivingExpense(), "Food should be living expense")
        assertTrue(loanInterestExpense.isLivingExpense(), "Loan interest should be living expense (financial expense)")
        assertFalse(loanPrincipalPayment.isLivingExpense(), "Debt principal should be excluded from living expenses")
        assertFalse(legacyPrincipalPayment.isLivingExpense(), "Legacy principal should be excluded from living expenses")
        assertFalse(capitalOutlayDeal.isLivingExpense(), "Capital outlay should be excluded from living expenses")
        assertFalse(incomeSalary.isLivingExpense(), "Income is not living expense")
        assertFalse(transferOut.isLivingExpense(), "Transfer is not living expense")
    }

    private fun transaction(
        id: String,
        type: TransactionType,
        walletId: String = "wallet",
        relatedWalletId: String? = null,
        categoryId: String? = null,
        amount: Long = 100_000L,
        note: String = "",
        dealFlowType: DealFlowType? = null,
    ) = FinanceTransaction(
        id = id,
        type = type,
        amount = Money(amount),
        categoryId = categoryId,
        walletId = walletId,
        relatedWalletId = relatedWalletId,
        note = note,
        dealFlowType = dealFlowType,
        date = Instant.parse("2026-08-28T00:00:00Z"),
    )

    private fun wallet(id: String, type: WalletType, balance: Long, status: String = "active") = Wallet(
        id = id,
        name = id,
        type = type,
        balance = Money(balance),
        colorHex = "#3366FF",
        isDefault = false,
        createdAt = Instant.parse("2026-08-28T00:00:00Z"),
        status = status,
    )
}
