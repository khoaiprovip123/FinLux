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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class SaveDebtAccountUseCaseTest {

    private val savedDebts = mutableMapOf<String, DebtAccount>()

    private val fakeDebtRepository = object : DebtRepository {
        override fun observeDebts(): Flow<List<DebtAccount>> = flowOf(savedDebts.values.toList())
        override fun observePaymentHistory(debtId: String): Flow<List<DebtPaymentHistory>> = flowOf(emptyList())
        override fun observeAllPaymentHistory(): Flow<List<DebtPaymentHistory>> = flowOf(emptyList())
        override suspend fun upsertDebt(debt: DebtAccount): AppResult<String> {
            val id = debt.id.ifBlank { "generated-id" }
            savedDebts[id] = debt.copy(id = id)
            return AppResult.Success(id)
        }
        override suspend fun deleteDebt(debt: DebtAccount): AppResult<Unit> {
            savedDebts.remove(debt.id)
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

    private val fakeReminderRepository = object : ReminderRepository {
        override fun observeReminders(): Flow<List<Reminder>> = flowOf(emptyList())
        override suspend fun upsertReminder(reminder: Reminder): AppResult<String> = AppResult.Success(reminder.id)
        override suspend fun deleteReminder(reminder: Reminder): AppResult<Unit> = AppResult.Success(Unit)
    }

    private val fakeScheduler = object : ReminderScheduler {
        override fun schedule(reminder: Reminder) {}
        override fun cancel(reminderId: String) {}
    }

    private lateinit var syncDebtReminderUseCase: SyncDebtReminderUseCase
    private lateinit var useCase: SaveDebtAccountUseCase

    @BeforeEach
    fun setUp() {
        savedDebts.clear()
        syncDebtReminderUseCase = SyncDebtReminderUseCase(fakeReminderRepository, fakeScheduler)
        useCase = SaveDebtAccountUseCase(fakeDebtRepository, syncDebtReminderUseCase)
    }

    @Test
    fun `save personal loan with null dueDate succeeds`() = runTest {
        val personalDebt = DebtAccount(
            id = "debt-den",
            name = "Đen",
            type = DebtType.PERSONAL_LOAN,
            totalAmount = Money(1_000_000L),
            remainingBalance = Money(1_000_000L),
            interestRateApr = 0.0,
            minimumPayment = Money(30_000L),
            dueDate = null, // Hợp lệ cho nợ cá nhân linh hoạt
        )

        val result = useCase(personalDebt)
        assertTrue(result is AppResult.Success)
        assertEquals("debt-den", (result as AppResult.Success).value)
        assertEquals(null, savedDebts["debt-den"]?.dueDate)
    }

    @Test
    fun `save credit card with null dueDate fails validation`() = runTest {
        val creditCard = DebtAccount(
            id = "debt-card",
            name = "Thẻ HSBC",
            type = DebtType.CREDIT_CARD,
            totalAmount = Money(20_000_000L),
            remainingBalance = Money(10_000_000L),
            interestRateApr = 24.0,
            minimumPayment = Money(1_000_000L),
            dueDate = null, // Nợ định kỳ bắt buộc phải có dueDate
        )

        val result = useCase(creditCard)
        assertTrue(result is AppResult.Error)
        assertEquals("Ngày đến hạn hàng tháng phải từ 1 đến 31", (result as AppResult.Error).message)
    }

    @Test
    fun `save credit card with valid dueDate succeeds`() = runTest {
        val creditCard = DebtAccount(
            id = "debt-card",
            name = "Thẻ HSBC",
            type = DebtType.CREDIT_CARD,
            totalAmount = Money(20_000_000L),
            remainingBalance = Money(10_000_000L),
            interestRateApr = 24.0,
            minimumPayment = Money(1_000_000L),
            dueDate = 15,
        )

        val result = useCase(creditCard)
        assertTrue(result is AppResult.Success)
        assertEquals(15, savedDebts["debt-card"]?.dueDate)
    }

    @Test
    fun `save debt with blank name fails`() = runTest {
        val debt = DebtAccount(
            name = "   ",
            type = DebtType.PERSONAL_LOAN,
            totalAmount = Money(1_000_000L),
            remainingBalance = Money(1_000_000L),
            interestRateApr = 0.0,
            minimumPayment = Money(30_000L),
        )

        val result = useCase(debt)
        assertTrue(result is AppResult.Error)
        assertEquals("Tên khoản nợ không được để trống", (result as AppResult.Error).message)
    }

    @Test
    fun `save recurring debt with out-of-range dueDate fails`() = runTest {
        val debt = DebtAccount(
            name = "Vay mua xe",
            type = DebtType.BANK_LOAN,
            totalAmount = Money(50_000_000L),
            remainingBalance = Money(50_000_000L),
            interestRateApr = 10.0,
            minimumPayment = Money(2_000_000L),
            dueDate = 32,
        )

        val result = useCase(debt)
        assertTrue(result is AppResult.Error)
        assertEquals("Ngày đến hạn hàng tháng phải từ 1 đến 31", (result as AppResult.Error).message)
    }
}
