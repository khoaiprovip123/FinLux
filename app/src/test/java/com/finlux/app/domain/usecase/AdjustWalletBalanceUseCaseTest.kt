package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import java.time.Instant

class AdjustWalletBalanceUseCaseTest {

    private val transactionRepository: TransactionRepository = mockk(relaxed = true)
    private val useCase = AdjustWalletBalanceUseCase(transactionRepository)

    @Test
    fun `adjust balance with same amount returns Success without creating transaction`() = runTest {
        val wallet = Wallet("w1", "Tiền mặt", WalletType.CASH, Money(1_000_000), "#10B981", true, Instant.now())
        val result = useCase(wallet, targetBalance = 1_000_000)

        assertInstanceOf(AppResult.Success::class.java, result)
        coVerify(exactly = 0) { transactionRepository.addWithBalanceUpdate(any()) }
    }

    @Test
    fun `adjust balance upwards creates INCOME transaction in ledger`() = runTest {
        val wallet = Wallet("w1", "Tiền mặt", WalletType.CASH, Money(1_000_000), "#10B981", true, Instant.now())
        val txSlot = slot<FinanceTransaction>()
        coEvery { transactionRepository.addWithBalanceUpdate(capture(txSlot)) } returns AppResult.Success("tx_id")

        val result = useCase(wallet, targetBalance = 1_500_000, note = "Cân đối cuối tháng")

        assertInstanceOf(AppResult.Success::class.java, result)
        assertEquals(TransactionType.INCOME, txSlot.captured.type)
        assertEquals(500_000L, txSlot.captured.amount.value)
        assertEquals("w1", txSlot.captured.walletId)
        assertEquals("Cân đối cuối tháng", txSlot.captured.note)
    }

    @Test
    fun `adjust balance downwards creates EXPENSE transaction in ledger`() = runTest {
        val wallet = Wallet("w1", "Tiền mặt", WalletType.CASH, Money(1_000_000), "#10B981", true, Instant.now())
        val txSlot = slot<FinanceTransaction>()
        coEvery { transactionRepository.addWithBalanceUpdate(capture(txSlot)) } returns AppResult.Success("tx_id")

        val result = useCase(wallet, targetBalance = 700_000)

        assertInstanceOf(AppResult.Success::class.java, result)
        assertEquals(TransactionType.EXPENSE, txSlot.captured.type)
        assertEquals(300_000L, txSlot.captured.amount.value)
        assertEquals("w1", txSlot.captured.walletId)
    }
}
