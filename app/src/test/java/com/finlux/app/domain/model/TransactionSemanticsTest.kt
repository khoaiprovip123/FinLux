package com.finlux.app.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
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

    private fun transaction(
        id: String,
        type: TransactionType,
        walletId: String = "wallet",
        relatedWalletId: String? = null,
        categoryId: String? = null,
        amount: Long = 100_000L,
    ) = FinanceTransaction(
        id = id,
        type = type,
        amount = Money(amount),
        categoryId = categoryId,
        walletId = walletId,
        relatedWalletId = relatedWalletId,
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
