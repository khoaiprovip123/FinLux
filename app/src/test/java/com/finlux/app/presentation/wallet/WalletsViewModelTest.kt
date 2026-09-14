package com.finlux.app.presentation.wallet

import app.cash.turbine.test
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.DeleteWalletUseCase
import com.finlux.app.domain.usecase.SaveWalletUseCase
import com.finlux.app.domain.usecase.TransferMoneyUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class WalletsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var walletRepo: FakeWalletRepository
    private lateinit var categoryRepo: FakeCategoryRepository
    private lateinit var transactionRepo: FakeTransactionRepository
    private lateinit var salaryCycleRepo: FakeSalaryCycleRepository
    private lateinit var saveWalletUseCase: SaveWalletUseCase
    private lateinit var deleteWalletUseCase: DeleteWalletUseCase
    private lateinit var transferMoneyUseCase: TransferMoneyUseCase
    private lateinit var viewModel: WalletsViewModel

    private val sampleWallets = listOf(
        Wallet(
            id = "w_cash",
            name = "Tiền mặt trong ví",
            type = WalletType.CASH,
            balance = Money(2_000_000L),
            colorHex = "#1F6FBF",
            isDefault = true,
            createdAt = Instant.now(),
            status = "active",
        ),
        Wallet(
            id = "w_bank",
            name = "Techcombank",
            type = WalletType.BANK,
            balance = Money(10_000_000L),
            colorHex = "#3478F6",
            isDefault = false,
            createdAt = Instant.now(),
            status = "active",
        ),
        Wallet(
            id = "w_card",
            name = "HSBC Visa Credit",
            type = WalletType.CARD,
            balance = Money(-3_000_000L),
            colorHex = "#7758F6",
            isDefault = false,
            createdAt = Instant.now(),
            status = "active",
        ),
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        walletRepo = FakeWalletRepository(sampleWallets)
        categoryRepo = FakeCategoryRepository()
        transactionRepo = FakeTransactionRepository(walletRepo)
        salaryCycleRepo = FakeSalaryCycleRepository()
        saveWalletUseCase = SaveWalletUseCase(walletRepo)
        deleteWalletUseCase = DeleteWalletUseCase(walletRepo)
        transferMoneyUseCase = TransferMoneyUseCase(transactionRepo, walletRepo)

        viewModel = WalletsViewModel(
            walletRepository = walletRepo,
            categoryRepository = categoryRepo,
            transactionRepository = transactionRepo,
            salaryCycleRepository = salaryCycleRepo,
            saveWallet = saveWalletUseCase,
            deleteWallet = deleteWalletUseCase,
            transferMoney = transferMoneyUseCase,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadWallets success and categorizeWallets correctly groups Cash, Bank, and Card`() = runTest(testDispatcher) {
        viewModel.wallets.test {
            val initial = awaitItem()
            val list = if (initial.isEmpty()) awaitItem() else initial
            assertEquals(3, list.size)

            val categorized = viewModel.categorizeWallets(list)
            val cashWallets = categorized[WalletType.CASH] ?: emptyList()
            val bankWallets = categorized[WalletType.BANK] ?: emptyList()
            val cardWallets = categorized[WalletType.CARD] ?: emptyList()

            assertEquals(1, cashWallets.size)
            assertEquals("w_cash", cashWallets.first().id)

            assertEquals(1, bankWallets.size)
            assertEquals("w_bank", bankWallets.first().id)

            assertEquals(1, cardWallets.size)
            assertEquals("w_card", cardWallets.first().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `calculateNetWorth properly sums regular assets and deducts credit card debt`() = runTest(testDispatcher) {
        viewModel.wallets.test {
            val initial = awaitItem()
            val list = if (initial.isEmpty()) awaitItem() else initial

            // Gross regular assets = 2,000,000 (cash) + 10,000,000 (bank) = 12,000,000
            // Credit card debt = 3,000,000
            // Net worth = 12,000,000 - 3,000,000 = 9,000,000
            val netWorth = viewModel.calculateNetWorth(list)
            assertEquals(9_000_000L, netWorth)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `transfer valid amount updates both source and destination wallet balances`() = runTest(testDispatcher) {
        var onSavedCalled = false
        viewModel.transfer(
            sourceId = "w_bank",
            destinationId = "w_cash",
            amount = 1_000_000L,
            note = "Rút tiền ATM chi tiêu",
            date = Instant.now(),
            onSaved = { onSavedCalled = true },
        )
        advanceUntilIdle()

        assertTrue(onSavedCalled)
        assertEquals("Chuyển tiền thành công", viewModel.actionState.value.message)
        assertEquals(1, transactionRepo.transferCalls)

        val updatedWallets = walletRepo.currentWallets
        val updatedSource = updatedWallets.first { it.id == "w_bank" }
        val updatedDest = updatedWallets.first { it.id == "w_cash" }

        // Source: 10,000,000 - 1,000,000 = 9,000,000
        assertEquals(9_000_000L, updatedSource.balance.value)
        // Dest: 2,000,000 + 1,000,000 = 3,000,000
        assertEquals(3_000_000L, updatedDest.balance.value)
    }

    @Test
    fun `transfer with insufficient balance rejects operation and sets error in actionState`() = runTest(testDispatcher) {
        var onSavedCalled = false
        viewModel.transfer(
            sourceId = "w_cash",
            destinationId = "w_bank",
            amount = 5_000_000L, // Ví tiền mặt chỉ có 2,000,000
            note = "Chuyển tiền vượt số dư",
            date = Instant.now(),
            onSaved = { onSavedCalled = true },
        )
        advanceUntilIdle()

        assertFalse(onSavedCalled)
        assertEquals("Số dư ví nguồn không đủ để thực hiện chuyển tiền", viewModel.actionState.value.message)
        assertEquals(0, transactionRepo.transferCalls)

        // Số dư 2 ví không thay đổi
        val wallets = walletRepo.currentWallets
        assertEquals(2_000_000L, wallets.first { it.id == "w_cash" }.balance.value)
        assertEquals(10_000_000L, wallets.first { it.id == "w_bank" }.balance.value)
    }

    @Test
    fun `toggleHideBalance updates balance visibility state correctly`() {
        assertFalse(viewModel.isBalanceHidden.value)

        viewModel.toggleHideBalance()
        assertTrue(viewModel.isBalanceHidden.value)

        viewModel.toggleHideBalance()
        assertFalse(viewModel.isBalanceHidden.value)
    }

    @Test
    fun `archiveWallet archives wallet successfully and updates status`() = runTest(testDispatcher) {
        var onArchivedCalled = false
        val targetWallet = sampleWallets.first { it.id == "w_cash" }

        viewModel.archiveWallet(targetWallet) {
            onArchivedCalled = true
        }
        advanceUntilIdle()

        assertTrue(onArchivedCalled)
        assertEquals("Đã lưu ví", viewModel.actionState.value.message)
        assertNotNull(walletRepo.lastUpsertedWallet)
        assertEquals("archived", walletRepo.lastUpsertedWallet?.status)
        assertNotNull(walletRepo.lastUpsertedWallet?.archivedAt)
    }

    @Test
    fun `delete wallet removes item and updates actionState message`() = runTest(testDispatcher) {
        val targetWallet = sampleWallets.first { it.id == "w_card" }
        viewModel.delete(targetWallet)
        advanceUntilIdle()

        assertEquals("Đã xóa ví", viewModel.actionState.value.message)
        assertEquals("w_card", walletRepo.deletedWallet?.id)
        assertFalse(walletRepo.currentWallets.any { it.id == "w_card" })
    }

    @Test
    fun `consumeMessage clears action message from state`() = runTest(testDispatcher) {
        viewModel.delete(sampleWallets.first())
        advanceUntilIdle()
        assertEquals("Đã xóa ví", viewModel.actionState.value.message)

        viewModel.consumeMessage()
        assertNull(viewModel.actionState.value.message)
    }

    // --- Fake Repository Implementations ---

    private class FakeWalletRepository(initialWallets: List<Wallet>) : WalletRepository {
        private val _walletsFlow = MutableStateFlow(initialWallets)
        val currentWallets: List<Wallet> get() = _walletsFlow.value

        var lastUpsertedWallet: Wallet? = null
        var deletedWallet: Wallet? = null

        override fun observeWallets(): Flow<List<Wallet>> = _walletsFlow

        override suspend fun upsertWallet(wallet: Wallet): AppResult<String> {
            lastUpsertedWallet = wallet
            val current = _walletsFlow.value.toMutableList()
            val index = current.indexOfFirst { it.id == wallet.id }
            if (index >= 0) {
                current[index] = wallet
            } else {
                current.add(wallet)
            }
            _walletsFlow.value = current
            return AppResult.Success(wallet.id)
        }

        override suspend fun deleteWallet(wallet: Wallet): AppResult<Unit> {
            deletedWallet = wallet
            _walletsFlow.value = _walletsFlow.value.filterNot { it.id == wallet.id }
            return AppResult.Success(Unit)
        }

        fun updateBalance(walletId: String, newBalance: Long) {
            val current = _walletsFlow.value.toMutableList()
            val index = current.indexOfFirst { it.id == walletId }
            if (index >= 0) {
                current[index] = current[index].copy(balance = Money(newBalance))
                _walletsFlow.value = current
            }
        }
    }

    private class FakeCategoryRepository : CategoryRepository {
        override fun observeCategories(): Flow<List<Category>> = flowOf(emptyList())
        override suspend fun upsertCategory(category: Category): AppResult<String> = AppResult.Success(category.id)
        override suspend fun deleteCategory(category: Category): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class FakeTransactionRepository(private val walletRepo: FakeWalletRepository) : TransactionRepository {
        var transferCalls = 0

        override fun observeRecent(limit: Int): Flow<List<FinanceTransaction>> = flowOf(emptyList())
        override fun observeMonth(month: YearMonth): Flow<List<FinanceTransaction>> = flowOf(emptyList())
        override fun observePeriod(start: Instant, endExclusive: Instant): Flow<List<FinanceTransaction>> = flowOf(emptyList())

        override suspend fun addWithBalanceUpdate(transaction: FinanceTransaction): AppResult<String> = AppResult.Success("tx-1")
        override suspend fun editWithBalanceUpdate(original: FinanceTransaction, updated: FinanceTransaction): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun deleteWithBalanceUpdate(transaction: FinanceTransaction): AppResult<Unit> = AppResult.Success(Unit)

        override suspend fun transferBetweenWallets(
            sourceWalletId: String,
            destinationWalletId: String,
            amount: Long,
            note: String,
            date: Instant,
        ): AppResult<Unit> {
            transferCalls++
            val source = walletRepo.currentWallets.first { it.id == sourceWalletId }
            val dest = walletRepo.currentWallets.first { it.id == destinationWalletId }
            walletRepo.updateBalance(sourceWalletId, source.balance.value - amount)
            walletRepo.updateBalance(destinationWalletId, dest.balance.value + amount)
            return AppResult.Success(Unit)
        }

        override suspend fun executeSalaryRolloverAtomic(
            cycleKey: String,
            sourceWalletId: String,
            destinationWalletId: String,
            amount: Long,
            note: String,
            date: Instant,
        ): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class FakeSalaryCycleRepository : SalaryCycleRepository {
        override fun observeConfig(): Flow<SalaryCycleConfig> = flowOf(SalaryCycleConfig(financeTimeZone = "Asia/Ho_Chi_Minh"))
        override suspend fun saveConfig(config: SalaryCycleConfig): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun isRolloverProcessed(cycleKey: String): Boolean = false
        override suspend fun markRolloverProcessed(cycleKey: String): AppResult<Unit> = AppResult.Success(Unit)
    }
}
