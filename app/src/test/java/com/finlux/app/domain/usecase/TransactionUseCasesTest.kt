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
        val result = AddTransactionUseCase(repository, walletRepository)(validTransaction().copy(amount = Money(0)))

        assertInstanceOf(AppResult.Error::class.java, result)
        assertEquals(0, repository.addCalls)
    }

    @Test
    fun `add rejects expense when cash wallet balance is insufficient`() = runTest {
        val result = AddTransactionUseCase(repository, walletRepository)(
            validTransaction().copy(amount = Money(600_000), walletId = "cash")
        )

        assertInstanceOf(AppResult.Error::class.java, result)
        assertEquals("Số dư ví [Tiền mặt] không đủ để thực hiện chi tiêu", (result as AppResult.Error).message)
        assertEquals(0, repository.addCalls)
    }

    @Test
    fun `add allows expense for credit card even with zero balance`() = runTest {
        val result = AddTransactionUseCase(repository, walletRepository)(
            validTransaction().copy(amount = Money(1_000_000), walletId = "card")
        )

        assertEquals(AppResult.Success("generated-id"), result)
        assertEquals(1, repository.addCalls)
    }

    @Test
    fun `add delegates valid expense to atomic repository method`() = runTest {
        val result = AddTransactionUseCase(repository, walletRepository)(validTransaction())

        assertEquals(AppResult.Success("generated-id"), result)
        assertEquals(1, repository.addCalls)
    }

    @Test
    fun `edit requires the original stable id`() = runTest {
        val original = validTransaction(id = "tx-1")
        val result = EditTransactionUseCase(repository, walletRepository)(original, original.copy(id = "tx-2"))

        assertInstanceOf(AppResult.Error::class.java, result)
        assertEquals(0, repository.editCalls)
    }

    @Test
    fun `edit rejects expense when new amount exceeds available balance`() = runTest {
        val original = validTransaction(id = "tx-1").copy(amount = Money(100_000), walletId = "cash")
        val updated = original.copy(amount = Money(800_000)) // cash has 500k + 100k refund = 600k < 800k
        val result = EditTransactionUseCase(repository, walletRepository)(original, updated)

        assertInstanceOf(AppResult.Error::class.java, result)
        assertEquals("Số dư ví [Tiền mặt] không đủ để thực hiện chi tiêu", (result as AppResult.Error).message)
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

    @Test
    fun `add expense triggers 80 percent budget warning notification`() = runTest {
        val fakeBudgetRepo = FakeBudgetRepository(
            mutableListOf(
                com.finlux.app.domain.model.Budget(
                    id = "food_month:2026-08",
                    categoryId = "food",
                    periodKey = "month:2026-08",
                    limitAmount = Money(1_000_000),
                    spentAmount = Money(750_000),
                    notified80 = false,
                    notified100 = false,
                )
            )
        )
        val fakeNotiRepo = FakeNotificationRepository()
        val useCase = AddTransactionUseCase(
            repository = repository,
            walletRepository = walletRepository,
            budgetRepository = fakeBudgetRepo,
            notificationRepository = fakeNotiRepo,
        )

        val tx = validTransaction().copy(amount = Money(100_000)) // 750k + 100k = 850k (85% >= 80%)
        val result = useCase(tx)

        assertEquals(AppResult.Success("generated-id"), result)
        assertEquals(1, fakeNotiRepo.savedNotifications.size)
        assertEquals(com.finlux.app.domain.model.NotificationType.BUDGET_ALERT, fakeNotiRepo.savedNotifications.first().type)
        assertEquals(true, fakeBudgetRepo.budgets.first().notified80)
        assertEquals(false, fakeBudgetRepo.budgets.first().notified100)
    }

    @Test
    fun `add expense triggers 100 percent budget exceeded notification`() = runTest {
        val fakeBudgetRepo = FakeBudgetRepository(
            mutableListOf(
                com.finlux.app.domain.model.Budget(
                    id = "food_month:2026-08",
                    categoryId = "food",
                    periodKey = "month:2026-08",
                    limitAmount = Money(1_000_000),
                    spentAmount = Money(950_000),
                    notified80 = true,
                    notified100 = false,
                )
            )
        )
        val fakeNotiRepo = FakeNotificationRepository()
        val useCase = AddTransactionUseCase(
            repository = repository,
            walletRepository = walletRepository,
            budgetRepository = fakeBudgetRepo,
            notificationRepository = fakeNotiRepo,
        )

        val tx = validTransaction().copy(amount = Money(100_000)) // 950k + 100k = 1050k (105% >= 100%)
        val result = useCase(tx)

        assertEquals(AppResult.Success("generated-id"), result)
        assertEquals(1, fakeNotiRepo.savedNotifications.size)
        assertEquals(com.finlux.app.domain.model.NotificationType.BUDGET_ALERT, fakeNotiRepo.savedNotifications.first().type)
        assertEquals(true, fakeBudgetRepo.budgets.first().notified100)
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

private class FakeBudgetRepository(
    val budgets: MutableList<com.finlux.app.domain.model.Budget> = mutableListOf()
) : com.finlux.app.domain.repository.BudgetRepository {
    override fun observeBudgets(periodKey: String): Flow<List<com.finlux.app.domain.model.Budget>> =
        flowOf(budgets.filter { it.periodKey == periodKey })

    override suspend fun upsertBudget(budget: com.finlux.app.domain.model.Budget): AppResult<String> {
        budgets.removeIf { it.id == budget.id }
        budgets.add(budget)
        return AppResult.Success(budget.id)
    }

    override suspend fun deleteBudget(budget: com.finlux.app.domain.model.Budget): AppResult<Unit> {
        budgets.removeIf { it.id == budget.id }
        return AppResult.Success(Unit)
    }
}

private class FakeNotificationRepository : com.finlux.app.domain.repository.NotificationRepository {
    val savedNotifications = mutableListOf<com.finlux.app.domain.model.AppNotification>()

    override fun observeNotifications(): Flow<List<com.finlux.app.domain.model.AppNotification>> =
        flowOf(savedNotifications)

    override suspend fun saveNotification(notification: com.finlux.app.domain.model.AppNotification): AppResult<String> {
        savedNotifications.add(notification)
        return AppResult.Success(notification.id)
    }

    override suspend fun markAsRead(id: String): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun markAsPaid(id: String): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun markAsPaidWithAmount(id: String, amount: Money, newBody: String?): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun markAsPaidByReminderId(reminderId: String): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun deleteNotification(id: String): AppResult<Unit> {
        savedNotifications.removeIf { it.id == id }
        return AppResult.Success(Unit)
    }
    override suspend fun clearAll(): AppResult<Unit> {
        savedNotifications.clear()
        return AppResult.Success(Unit)
    }
}

private class RecordingTransactionRepository : TransactionRepository {
    var addCalls = 0
    var editCalls = 0
    var deleteCalls = 0
    var transferCalls = 0

    override fun observeRecent(limit: Int): Flow<List<FinanceTransaction>> = flowOf(emptyList())

    override fun observeMonth(month: java.time.YearMonth): Flow<List<FinanceTransaction>> = flowOf(emptyList())

    override fun observePeriod(start: Instant, endExclusive: Instant): Flow<List<FinanceTransaction>> = flowOf(emptyList())

    override suspend fun executeSalaryRolloverAtomic(
        cycleKey: String,
        sourceWalletId: String,
        destinationWalletId: String,
        amount: Long,
        note: String,
        date: Instant
    ): AppResult<Unit> = AppResult.Success(Unit)

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
