package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtPaymentHistory
import com.finlux.app.domain.model.DebtType
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.repository.DebtRepository
import com.finlux.app.domain.repository.ReminderRepository
import com.finlux.app.domain.repository.ReminderScheduler
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class DeleteDebtAccountUseCaseTest {
    private val repository = RecordingDeleteDebtRepository()
    private val reminderRepository = object : ReminderRepository {
        override fun observeReminders(): Flow<List<Reminder>> = flowOf(emptyList())
        override suspend fun upsertReminder(reminder: Reminder): AppResult<String> = AppResult.Success(reminder.id)
        override suspend fun deleteReminder(reminder: Reminder): AppResult<Unit> = AppResult.Success(Unit)
    }
    private val scheduler = object : ReminderScheduler {
        override fun schedule(reminder: Reminder) = Unit
        override fun cancel(reminderId: String) = Unit
    }
    private val useCase = DeleteDebtAccountUseCase(
        repository,
        SyncDebtReminderUseCase(reminderRepository, scheduler),
    )

    @Test
    fun `rejects debt with outstanding principal`() = runTest {
        val result = useCase(validDebt().copy(remainingBalance = Money(2_000_000L), isSettled = false))
        assertInstanceOf(AppResult.Error::class.java, result)
        assertEquals(0, repository.deleteCalls)
    }

    @Test
    fun `deletes fully settled debt`() = runTest {
        val result = useCase(validDebt().copy(remainingBalance = Money(0L), isSettled = true))
        assertEquals(AppResult.Success(Unit), result)
        assertEquals(1, repository.deleteCalls)
    }

    private fun validDebt() = DebtAccount(
        id = "debt-1",
        name = "Khoản vay",
        type = DebtType.PERSONAL_LOAN,
        totalAmount = Money(5_000_000L),
        remainingBalance = Money(0L),
        interestRateApr = 10.0,
        minimumPayment = Money(500_000L),
        isSettled = true,
    )
}

private class RecordingDeleteDebtRepository : DebtRepository {
    var deleteCalls = 0

    override fun observeDebts(): Flow<List<DebtAccount>> = flowOf(emptyList())
    override fun observePaymentHistory(debtId: String): Flow<List<DebtPaymentHistory>> = flowOf(emptyList())
    override fun observeAllPaymentHistory(): Flow<List<DebtPaymentHistory>> = flowOf(emptyList())
    override suspend fun upsertDebt(debt: DebtAccount): AppResult<String> = AppResult.Success(debt.id)
    override suspend fun deleteDebt(debt: DebtAccount): AppResult<Unit> {
        deleteCalls++
        return AppResult.Success(Unit)
    }
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
