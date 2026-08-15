package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import java.time.Instant

class TransactionUseCasesTest {
    private val repository = RecordingTransactionRepository()
    private val walletRepository = FakeWalletRepository(
        listOf(
            Wallet("cash", "Tiền mặt", WalletType.CASH, Money(500_000), "#1F6FBF", true, Instant.now()),
            Wallet("bank", "Ngân hàng", WalletType.BANK, Money(2_000_000), "#3478F6", false, Instant.now()),
            Wallet("card", "Thẻ tín dụng", WalletType.CARD, Money(0), "#7758F6", false, Instant.now()),
        )
    )

    @Test
    fun `add rejects zero amount before touching repository`() = runTest {
        val result = AddTransactionUseCase(repository)(validTransaction().copy(amount = Money(0)))

        assertInstanceOf(AppResult.Error::class.java, result)
        assertEquals(0, repository.addCalls)
    }

    @Test
    fun `add delegates valid expense to atomic repository method`() = runTest {
        val result = AddTransactionUseCase(repository)(validTransaction())

        assertEquals(AppResult.Success("generated-id"), result)
        assertEquals(1, repository.addCalls)
    }

    @Test
    fun `edit requires the original stable id`() = runTest {
        val original = validTransaction(id = "tx-1")
        val result = EditTransactionUseCase(repository)(original, original.copy(id = "tx-2"))

        assertInstanceOf(AppResult.Error::class.java, result)
        assertEquals(0, repository.editCalls)
    }

    @Test
    fun `delete delegates to balance restoring repository method`() = runTest {
        val result = DeleteTransactionUseCase(repository)(validTransaction(id = "tx-1"))

        assertEquals(AppResult.Success(Unit), result)
        assertEquals(1, repository.deleteCalls)
    }

    @Test
    fun `transfer rejects identical wallets before touching repository`() = runTest {
        val result = TransferMoneyUseCase(repository, walletRepository)("cash", "cash", 100_000, "")

        assertInstanceOf(AppResult.Error::class.java, result)
        assertEquals(0, repository.transferCalls)
    }

    @Test
    fun `transfer rejects insufficient funds for cash wallet`() = runTest {
        val result = TransferMoneyUseCase(repository, walletRepository)("cash", "bank", 600_000, "Vượt quá")

        assertInstanceOf(AppResult.Error::class.java, result)
        assertEquals("Số dư ví nguồn không đủ để thực hiện chuyển tiền", (result as AppResult.Error).message)
        assertEquals(0, repository.transferCalls)
    }

    @Test
    fun `transfer allows credit card even with zero balance`() = runTest {
        val result = TransferMoneyUseCase(repository, walletRepository)("card", "bank", 1_000_000, "Rút thẻ")

        assertEquals(AppResult.Success(Unit), result)
        assertEquals(1, repository.transferCalls)
    }

    @Test
    fun `transfer delegates valid pair to atomic repository method`() = runTest {
        val result = TransferMoneyUseCase(repository, walletRepository)("cash", "bank", 100_000, "Tiết kiệm")

        assertEquals(AppResult.Success(Unit), result)
        assertEquals(1, repository.transferCalls)
    }

    private fun validTransaction(id: String = "") = FinanceTransaction(
        id = id,
        type = TransactionType.EXPENSE,
        amount = Money(125_000),
        categoryId = "food",
        walletId = "cash",
        note = "Bữa trưa",
        date = Instant.parse("2026-08-11T05:00:00Z"),
    )
}

private class RecordingTransactionRepository : TransactionRepository {
    var addCalls = 0
    var editCalls = 0
    var deleteCalls = 0
    var transferCalls = 0

    override fun observeRecent(limit: Int): Flow<List<FinanceTransaction>> = flowOf(emptyList())

    override fun observeMonth(month: java.time.YearMonth): Flow<List<FinanceTransaction>> = flowOf(emptyList())

    override suspend fun addWithBalanceUpdate(transaction: FinanceTransaction): AppResult<String> {
        addCalls++
        return AppResult.Success("generated-id")
    }

    override suspend fun editWithBalanceUpdate(
        original: FinanceTransaction,
        updated: FinanceTransaction,
    ): AppResult<Unit> {
        editCalls++
        return AppResult.Success(Unit)
    }

    override suspend fun deleteWithBalanceUpdate(
        transaction: FinanceTransaction,
    ): AppResult<Unit> {
        deleteCalls++
        return AppResult.Success(Unit)
    }

    override suspend fun transferBetweenWallets(
        sourceWalletId: String,
        destinationWalletId: String,
        amount: Long,
        note: String,
        date: Instant,
    ): AppResult<Unit> {
        transferCalls++
        return AppResult.Success(Unit)
    }
}

private class FakeWalletRepository(
    private val wallets: List<Wallet>
) : WalletRepository {
    override fun observeWallets(): Flow<List<Wallet>> = flowOf(wallets)
    override suspend fun upsertWallet(wallet: Wallet): AppResult<String> = AppResult.Success(wallet.id)
    override suspend fun deleteWallet(wallet: Wallet): AppResult<Unit> = AppResult.Success(Unit)
}
