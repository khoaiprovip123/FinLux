package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtPaymentHistory
import com.finlux.app.domain.repository.DebtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import java.time.Instant

class ProcessDebtPaymentUseCaseTest {

    private val fakeRepository = FakeDebtRepository()
    private val useCase = ProcessDebtPaymentUseCase(fakeRepository)

    @Test
    fun `rejects empty debtId`() = runTest {
        val result = useCase(
            debtId = "",
            walletId = "wallet-1",
            amount = 1_000_000L,
            principalPaid = 1_000_000L,
        )
        assertInstanceOf(AppResult.Error::class.java, result)
        assertEquals(0, fakeRepository.processCalls)
    }

    @Test
    fun `rejects empty walletId`() = runTest {
        val result = useCase(
            debtId = "debt-1",
            walletId = "",
            amount = 1_000_000L,
            principalPaid = 1_000_000L,
        )
        assertInstanceOf(AppResult.Error::class.java, result)
        assertEquals(0, fakeRepository.processCalls)
    }

    @Test
    fun `rejects zero or negative amount`() = runTest {
        val result = useCase(
            debtId = "debt-1",
            walletId = "wallet-1",
            amount = 0L,
            principalPaid = 0L,
        )
        assertInstanceOf(AppResult.Error::class.java, result)
        assertEquals(0, fakeRepository.processCalls)
    }

    @Test
    fun `rejects mismatched principal and interest sum`() = runTest {
        val result = useCase(
            debtId = "debt-1",
            walletId = "wallet-1",
            amount = 1_000_000L,
            principalPaid = 800_000L,
            interestPaid = 100_000L, // 800k + 100k = 900k != 1000k
        )
        assertInstanceOf(AppResult.Error::class.java, result)
        assertEquals(0, fakeRepository.processCalls)
    }

    @Test
    fun `valid payment delegates to repository`() = runTest {
        val result = useCase(
            debtId = "debt-1",
            walletId = "wallet-1",
            amount = 1_000_000L,
            principalPaid = 800_000L,
            interestPaid = 200_000L,
            note = "Tháng 8",
        )
        assertEquals(AppResult.Success(Unit), result)
        assertEquals(1, fakeRepository.processCalls)
        assertEquals(1_000_000L, fakeRepository.lastAmount)
        assertEquals(800_000L, fakeRepository.lastPrincipal)
        assertEquals(200_000L, fakeRepository.lastInterest)
    }
}

private class FakeDebtRepository : DebtRepository {
    var processCalls = 0
    var lastAmount = 0L
    var lastPrincipal = 0L
    var lastInterest = 0L

    override fun observeDebts(): Flow<List<DebtAccount>> = flowOf(emptyList())
    override fun observePaymentHistory(debtId: String): Flow<List<DebtPaymentHistory>> = flowOf(emptyList())
    override suspend fun upsertDebt(debt: DebtAccount): AppResult<String> = AppResult.Success("debt-1")
    override suspend fun deleteDebt(debt: DebtAccount): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun processPayment(
        debtId: String,
        walletId: String,
        amount: Long,
        principalPaid: Long,
        interestPaid: Long,
        note: String,
        paymentDate: Instant,
    ): AppResult<Unit> {
        processCalls++
        lastAmount = amount
        lastPrincipal = principalPaid
        lastInterest = interestPaid
        return AppResult.Success(Unit)
    }
}
