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
import java.time.YearMonth

class TransactionValidationInsufficientFundsTest {

    private class FakeWalletRepo(private val wallets: List<Wallet>) : WalletRepository {
        override fun observeWallets(): Flow<List<Wallet>> = flowOf(wallets)
        override suspend fun upsertWallet(wallet: Wallet): AppResult<String> = AppResult.Success(wallet.id)
        override suspend fun deleteWallet(wallet: Wallet): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class RecordingTxRepo : TransactionRepository {
        var addCalls = 0
        var editCalls = 0
        var transferCalls = 0

        override fun observeRecent(limit: Int): Flow<List<FinanceTransaction>> = flowOf(emptyList())
        override fun observeMonth(month: YearMonth): Flow<List<FinanceTransaction>> = flowOf(emptyList())
        override fun observePeriod(start: Instant, endExclusive: Instant): Flow<List<FinanceTransaction>> = flowOf(emptyList())
        override suspend fun addWithBalanceUpdate(transaction: FinanceTransaction): AppResult<String> {
            addCalls++
            return AppResult.Success("tx-id")
        }
        override suspend fun editWithBalanceUpdate(original: FinanceTransaction, updated: FinanceTransaction): AppResult<Unit> {
            editCalls++
            return AppResult.Success(Unit)
        }
        override suspend fun deleteWithBalanceUpdate(transaction: FinanceTransaction): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun transferBetweenWallets(sourceWalletId: String, destWalletId: String, amount: Long, note: String, date: Instant): AppResult<Unit> {
            transferCalls++
            return AppResult.Success(Unit)
        }
        override suspend fun executeSalaryRolloverAtomic(
            cycleKey: String,
            sourceWalletId: String,
            destinationWalletId: String,
            amount: Long,
            note: String,
            date: Instant
        ): AppResult<Unit> = AppResult.Success(Unit)
    }

    private val testWallets = listOf(
        Wallet("wallet_zero", "Ví Rỗng", WalletType.CASH, Money(0L), "#1F6FBF", true, Instant.now()),
        Wallet("wallet_50k", "Ví Tiền lẻ", WalletType.CASH, Money(50_000L), "#1F6FBF", false, Instant.now()),
        Wallet("wallet_bank", "Ngân hàng Techcombank", WalletType.BANK, Money(2_000_000L), "#3478F6", false, Instant.now()),
        Wallet("wallet_credit", "Thẻ Tín dụng HSBC", WalletType.CARD, Money(0L), "#7758F6", false, Instant.now()),
    )

    private val walletRepo = FakeWalletRepo(testWallets)
    private val txRepo = RecordingTxRepo()

    private fun createTx(
        walletId: String,
        amount: Long,
        type: TransactionType = TransactionType.EXPENSE,
        categoryId: String = "cat_food",
    ) = FinanceTransaction(
        id = "tx_test",
        amount = Money(amount),
        type = type,
        categoryId = categoryId,
        walletId = walletId,
        date = Instant.now(),
        note = "Test transaction",
    )

    @Test
    fun `AddTransactionUseCase rejects expense when cash wallet has zero balance`() = runTest {
        val useCase = AddTransactionUseCase(txRepo, walletRepo)
        val tx = createTx(walletId = "wallet_zero", amount = 100_000L)

        val result = useCase(tx)

        assertInstanceOf(AppResult.Error::class.java, result)
        assertEquals("Số dư ví [Ví Rỗng] không đủ để thực hiện chi tiêu", (result as AppResult.Error).message)
        assertEquals(0, txRepo.addCalls)
    }

    @Test
    fun `AddTransactionUseCase rejects expense when cash wallet has insufficient balance`() = runTest {
        val useCase = AddTransactionUseCase(txRepo, walletRepo)
        val tx = createTx(walletId = "wallet_50k", amount = 100_000L)

        val result = useCase(tx)

        assertInstanceOf(AppResult.Error::class.java, result)
        assertEquals("Số dư ví [Ví Tiền lẻ] không đủ để thực hiện chi tiêu", (result as AppResult.Error).message)
        assertEquals(0, txRepo.addCalls)
    }

    @Test
    fun `AddTransactionUseCase allows expense when wallet balance is exactly equal to amount`() = runTest {
        val useCase = AddTransactionUseCase(txRepo, walletRepo)
        val tx = createTx(walletId = "wallet_50k", amount = 50_000L)

        val result = useCase(tx)

        assertInstanceOf(AppResult.Success::class.java, result)
        assertEquals(1, txRepo.addCalls)
    }

    @Test
    fun `AddTransactionUseCase allows income transaction even if wallet has zero balance`() = runTest {
        val useCase = AddTransactionUseCase(txRepo, walletRepo)
        val tx = createTx(walletId = "wallet_zero", amount = 500_000L, type = TransactionType.INCOME)

        val result = useCase(tx)

        assertInstanceOf(AppResult.Success::class.java, result)
        assertEquals(1, txRepo.addCalls)
    }

    @Test
    fun `AddTransactionUseCase allows expense on credit card wallet with zero balance`() = runTest {
        val useCase = AddTransactionUseCase(txRepo, walletRepo)
        val tx = createTx(walletId = "wallet_credit", amount = 1_500_000L, type = TransactionType.EXPENSE)

        val result = useCase(tx)

        assertInstanceOf(AppResult.Success::class.java, result)
        assertEquals(1, txRepo.addCalls)
    }

    @Test
    fun `TransferMoneyUseCase rejects transfer when source wallet has insufficient balance`() = runTest {
        val useCase = TransferMoneyUseCase(txRepo, walletRepo)

        val result = useCase(
            sourceId = "wallet_50k",
            destinationId = "wallet_bank",
            amount = 100_000L,
            note = "Chuyển tiền",
        )

        assertInstanceOf(AppResult.Error::class.java, result)
        assertEquals("Số dư ví nguồn không đủ để thực hiện chuyển tiền", (result as AppResult.Error).message)
        assertEquals(0, txRepo.transferCalls)
    }

    @Test
    fun `EditTransactionUseCase rejects when updated amount exceeds balance after rollback`() = runTest {
        val useCase = EditTransactionUseCase(txRepo, walletRepo)
        val original = createTx(walletId = "wallet_50k", amount = 30_000L)
        // Ví 50k + rollback 30k = 80k. Sửa thành 100k -> vượt quá!
        val updated = original.copy(amount = Money(100_000L))

        val result = useCase(original, updated)

        assertInstanceOf(AppResult.Error::class.java, result)
        assertEquals("Số dư ví [Ví Tiền lẻ] không đủ để thực hiện chi tiêu", (result as AppResult.Error).message)
        assertEquals(0, txRepo.editCalls)
    }
}
