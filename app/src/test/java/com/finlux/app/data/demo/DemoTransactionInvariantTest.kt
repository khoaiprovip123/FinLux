package com.finlux.app.data.demo

import android.content.Context
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class DemoTransactionInvariantTest {

    private lateinit var context: Context
    private lateinit var repository: DemoFinluxRepository

    @BeforeEach
    fun setup() = runTest {
        context = mockk(relaxed = true)
        repository = DemoFinluxRepository(context)

        // Seed with controlled test wallets
        repository.upsertWallet(
            Wallet("cash-w", "Tiền mặt Test", WalletType.CASH, Money(100_000), "#10B981", true, Instant.now())
        )
        repository.upsertWallet(
            Wallet("bank-w", "Ngân hàng Test", WalletType.BANK, Money(200_000), "#3B82F6", false, Instant.now())
        )
        repository.upsertWallet(
            Wallet("card-w", "Thẻ tín dụng Test", WalletType.CARD, Money(0), "#8B5CF6", false, Instant.now())
        )
    }

    @Test
    fun `add expense greater than balance fails for CASH wallet`() = runTest {
        val tx = FinanceTransaction(
            id = "tx-1",
            type = TransactionType.EXPENSE,
            amount = Money(150_000),
            categoryId = "food",
            walletId = "cash-w",
            date = Instant.now(),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )
        val result = repository.addWithBalanceUpdate(tx)
        assertInstanceOf(AppResult.Error::class.java, result)

        val wallets = repository.observeWallets().first()
        val cash = wallets.first { it.id == "cash-w" }
        assertEquals(100_000L, cash.balance.value, "Balance should remain untouched after rejected transaction")
    }

    @Test
    fun `add expense on CARD wallet allows negative balance`() = runTest {
        val tx = FinanceTransaction(
            id = "tx-card",
            type = TransactionType.EXPENSE,
            amount = Money(500_000),
            categoryId = "shopping",
            walletId = "card-w",
            date = Instant.now(),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )
        val result = repository.addWithBalanceUpdate(tx)
        assertInstanceOf(AppResult.Success::class.java, result)

        val wallets = repository.observeWallets().first()
        val card = wallets.first { it.id == "card-w" }
        assertEquals(-500_000L, card.balance.value, "Credit card wallet allows negative balance")
    }

    @Test
    fun `edit expense to amount exceeding balance fails and rolls back`() = runTest {
        // First add a valid 50k expense -> cash becomes 50k
        val original = FinanceTransaction(
            id = "tx-edit",
            type = TransactionType.EXPENSE,
            amount = Money(50_000),
            categoryId = "food",
            walletId = "cash-w",
            date = Instant.now(),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )
        repository.addWithBalanceUpdate(original)

        // Try editing to 150k -> initial 100k < 150k -> fails
        val updated = original.copy(amount = Money(150_000))
        val result = repository.editWithBalanceUpdate(original, updated)
        assertInstanceOf(AppResult.Error::class.java, result)

        val wallets = repository.observeWallets().first()
        val cash = wallets.first { it.id == "cash-w" }
        assertEquals(50_000L, cash.balance.value, "Balance should roll back to 50k")
    }

    @Test
    fun `edit expense moving to a wallet with insufficient balance fails`() = runTest {
        // cash-w has 100k, bank-w has 200k
        val original = FinanceTransaction(
            id = "tx-move",
            type = TransactionType.EXPENSE,
            amount = Money(250_000),
            categoryId = "food",
            walletId = "card-w", // originally on card
            date = Instant.now(),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )
        repository.addWithBalanceUpdate(original)

        // Move 250k expense to bank-w (only has 200k) -> fails
        val updated = original.copy(walletId = "bank-w")
        val result = repository.editWithBalanceUpdate(original, updated)
        assertInstanceOf(AppResult.Error::class.java, result)

        val wallets = repository.observeWallets().first()
        val bank = wallets.first { it.id == "bank-w" }
        assertEquals(200_000L, bank.balance.value, "Bank balance untouched")
    }

    @Test
    fun `transfer exceeding source balance fails`() = runTest {
        val result = repository.transferBetweenWallets(
            sourceWalletId = "cash-w",
            destinationWalletId = "bank-w",
            amount = 150_000,
            note = "Transfer test",
            date = Instant.now(),
        )
        assertInstanceOf(AppResult.Error::class.java, result)

        val wallets = repository.observeWallets().first()
        val cash = wallets.first { it.id == "cash-w" }
        val bank = wallets.first { it.id == "bank-w" }
        assertEquals(100_000L, cash.balance.value)
        assertEquals(200_000L, bank.balance.value)
    }

    @Test
    fun `concurrent expenses do not drive balance below zero`() = runTest {
        // cash-w has 100_000. Try 5 concurrent expenses of 30_000 each (total 150_000).
        val tasks = (1..5).map { i ->
            async {
                repository.addWithBalanceUpdate(
                    FinanceTransaction(
                        id = "tx-concurrent-$i",
                        type = TransactionType.EXPENSE,
                        amount = Money(30_000),
                        categoryId = "food",
                        walletId = "cash-w",
                        date = Instant.now(),
                        createdAt = Instant.now(),
                        updatedAt = Instant.now(),
                    )
                )
            }
        }
        val results = tasks.awaitAll()
        val successes = results.count { it is AppResult.Success }
        val failures = results.count { it is AppResult.Error }

        assertEquals(3, successes, "Exactly 3 transactions of 30k can fit in 100k")
        assertEquals(2, failures, "Remaining 2 transactions must fail")

        val wallets = repository.observeWallets().first()
        val cash = wallets.first { it.id == "cash-w" }
        assertEquals(10_000L, cash.balance.value, "Final balance is 10k >= 0")
        assertTrue(cash.balance.value >= 0L, "Non-card wallet balance is never negative")
    }
}
