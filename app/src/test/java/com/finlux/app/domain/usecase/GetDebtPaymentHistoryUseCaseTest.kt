package com.finlux.app.domain.usecase

import app.cash.turbine.test
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtPaymentHistory
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.repository.DebtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class GetDebtPaymentHistoryUseCaseTest {

    private val sampleHistory = listOf(
        DebtPaymentHistory(
            id = "p-1",
            debtId = "d-1",
            walletId = "w-1",
            amount = Money(1_000_000L),
            principalPaid = Money(800_000L),
            interestPaid = Money(200_000L),
            paymentDate = Instant.now(),
        ),
        DebtPaymentHistory(
            id = "p-2",
            debtId = "d-2",
            walletId = "w-1",
            amount = Money(2_000_000L),
            principalPaid = Money(1_500_000L),
            interestPaid = Money(500_000L),
            paymentDate = Instant.now(),
        ),
    )

    private val fakeRepo = object : DebtRepository {
        var observeAllCalls = 0
        var observeSingleCalls = 0

        override fun observeDebts(): Flow<List<DebtAccount>> = flowOf(emptyList())

        override fun observePaymentHistory(debtId: String): Flow<List<DebtPaymentHistory>> {
            observeSingleCalls++
            return flowOf(sampleHistory.filter { it.debtId == debtId })
        }

        override fun observeAllPaymentHistory(): Flow<List<DebtPaymentHistory>> {
            observeAllCalls++
            return flowOf(sampleHistory)
        }

        override suspend fun upsertDebt(debt: DebtAccount): AppResult<String> = AppResult.Success("d-1")
        override suspend fun deleteDebt(debt: DebtAccount): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun processPayment(
            debtId: String,
            walletId: String,
            amount: Long,
            principalPaid: Long,
            interestPaid: Long,
            note: String,
            paymentDate: Instant,
        ): AppResult<Unit> = AppResult.Success(Unit)
    }

    private val useCase = GetDebtPaymentHistoryUseCase(fakeRepo)

    @Test
    fun `invoke with null debtId calls observeAllPaymentHistory`() = runTest {
        useCase(null).test {
            val list = awaitItem()
            assertEquals(2, list.size)
            assertEquals(1, fakeRepo.observeAllCalls)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke with specific debtId filters correctly`() = runTest {
        useCase("d-1").test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("p-1", list.first().id)
            assertEquals(1, fakeRepo.observeSingleCalls)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
