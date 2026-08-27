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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class ProcessDebtPaymentUseCaseTest {

    private val fakeRepository = FakeDebtRepository()
    private val deletedReminders = mutableListOf<String>()
    private val cancelledReminders = mutableListOf<String>()

    private val fakeReminderRepo = object : ReminderRepository {
        override fun observeReminders(): Flow<List<Reminder>> = flowOf(emptyList())
        override suspend fun upsertReminder(reminder: Reminder): AppResult<String> = AppResult.Success(reminder.id)
        override suspend fun deleteReminder(reminder: Reminder): AppResult<Unit> {
            deletedReminders.add(reminder.id)
            return AppResult.Success(Unit)
        }
    }

    private val fakeScheduler = object : ReminderScheduler {
        override fun schedule(reminder: Reminder) {}
        override fun cancel(reminderId: String) {
            cancelledReminders.add(reminderId)
        }
    }

    private val syncDebtReminderUseCase = SyncDebtReminderUseCase(fakeReminderRepo, fakeScheduler)
    private lateinit var useCase: ProcessDebtPaymentUseCase

    @BeforeEach
    fun setUp() {
        deletedReminders.clear()
        cancelledReminders.clear()
        useCase = ProcessDebtPaymentUseCase(fakeRepository, syncDebtReminderUseCase)
    }

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
    fun `valid payment delegates to repository and removes reminder if debt is settled`() = runTest {
        val settledDebt = DebtAccount(
            id = "debt-1",
            name = "Thẻ tín dụng",
            type = DebtType.CREDIT_CARD,
            totalAmount = Money(5_000_000L),
            remainingBalance = Money(0L),
            interestRateApr = 20.0,
            minimumPayment = Money(0L),
            dueDate = 15,
            isSettled = true,
            isReminderEnabled = true,
        )
        fakeRepository.debtsFlow.value = listOf(settledDebt)

        val result = useCase(
            debtId = "debt-1",
            walletId = "wallet-1",
            amount = 1_000_000L,
            principalPaid = 800_000L,
            interestPaid = 200_000L,
            note = "Tất toán",
        )
        assertEquals(AppResult.Success(Unit), result)
        assertEquals(1, fakeRepository.processCalls)
        assertEquals(1_000_000L, fakeRepository.lastAmount)
        assertEquals(800_000L, fakeRepository.lastPrincipal)
        assertEquals(200_000L, fakeRepository.lastInterest)

        // Verify reminder cancelled and deleted
        val reminderId = "debt_reminder_debt-1"
        assertTrue(cancelledReminders.contains(reminderId))
        assertTrue(deletedReminders.contains(reminderId))
    }
}

private class FakeDebtRepository : DebtRepository {
    var processCalls = 0
    var lastAmount = 0L
    var lastPrincipal = 0L
    var lastInterest = 0L
    val debtsFlow = MutableStateFlow<List<DebtAccount>>(emptyList())

    override fun observeDebts(): Flow<List<DebtAccount>> = debtsFlow
    override fun observePaymentHistory(debtId: String): Flow<List<DebtPaymentHistory>> = flowOf(emptyList())
    override fun observeAllPaymentHistory(): Flow<List<DebtPaymentHistory>> = flowOf(emptyList())
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
