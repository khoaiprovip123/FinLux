package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import java.time.Instant

class TransactionUseCasesTest {
    private val repository = RecordingTransactionRepository()

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
        val result = TransferMoneyUseCase(repository)("cash", "cash", 100_000, "")

        assertInstanceOf(AppResult.Error::class.java, result)
        assertEquals(0, repository.transferCalls)
    }

    @Test
    fun `transfer delegates valid pair to atomic repository method`() = runTest {
        val result = TransferMoneyUseCase(repository)("cash", "bank", 100_000, "Tiết kiệm")

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
