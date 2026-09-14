package com.finlux.app.presentation.deal

import app.cash.turbine.test
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.DealCategory
import com.finlux.app.domain.model.DealFlowType
import com.finlux.app.domain.model.DealStatus
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.FinancialDeal
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.DealRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.CloseDealUseCase
import com.finlux.app.domain.usecase.CloseDealWithLossUseCase
import com.finlux.app.domain.usecase.DeleteDealUseCase
import com.finlux.app.domain.usecase.GetDealsUseCase
import com.finlux.app.domain.usecase.RecordDealInflowUseCase
import com.finlux.app.domain.usecase.RecordDealOutlayUseCase
import com.finlux.app.domain.usecase.ReopenDealUseCase
import com.finlux.app.domain.usecase.RevertDealLossUseCase
import com.finlux.app.domain.usecase.SaveDealUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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
class DealsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDealRepo: FakeDealRepository
    private lateinit var fakeWalletRepo: FakeWalletRepository
    private lateinit var fakeTxRepo: FakeTransactionRepository
    private lateinit var viewModel: DealsViewModel

    private val sampleWallet = Wallet(
        id = "w_bank",
        name = "MB Bank",
        type = WalletType.BANK,
        balance = Money(50_000_000L),
        colorHex = "#1F6FBF",
        isDefault = false,
        createdAt = Instant.now(),
    )

    private val activeInvestmentDeal = FinancialDeal(
        id = "deal_1",
        title = "Góp vốn Cửa hàng Cà phê",
        category = DealCategory.INVESTMENT,
        targetAmount = Money(20_000_000L),
        totalCapitalOutlay = Money(10_000_000L),
        totalRecovered = Money(4_000_000L),
        netProfitLoss = Money(0L),
        status = DealStatus.ACTIVE,
    )

    private val activeLendingDeal = FinancialDeal(
        id = "deal_2",
        title = "Cho Nam mượn tiền sửa nhà",
        category = DealCategory.LENDING,
        targetAmount = Money(5_000_000L),
        totalCapitalOutlay = Money(5_000_000L),
        totalRecovered = Money(2_000_000L),
        netProfitLoss = Money(0L),
        status = DealStatus.ACTIVE,
    )

    private val completedDeal = FinancialDeal(
        id = "deal_3",
        title = "Đầu tư Chứng khoán FPT",
        category = DealCategory.INVESTMENT,
        targetAmount = Money(15_000_000L),
        totalCapitalOutlay = Money(15_000_000L),
        totalRecovered = Money(15_000_000L),
        netProfitLoss = Money(3_000_000L),
        status = DealStatus.COMPLETED,
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeWalletRepo = FakeWalletRepository(listOf(sampleWallet))
        fakeDealRepo = FakeDealRepository(
            initialDeals = listOf(activeInvestmentDeal, activeLendingDeal, completedDeal),
            walletRepo = fakeWalletRepo,
        )
        fakeTxRepo = FakeTransactionRepository()

        val getDealsUseCase = GetDealsUseCase(fakeDealRepo)
        val saveDealUseCase = SaveDealUseCase(fakeDealRepo)
        val deleteDealUseCase = DeleteDealUseCase(fakeDealRepo)
        val recordDealOutlayUseCase = RecordDealOutlayUseCase(fakeDealRepo)
        val recordDealInflowUseCase = RecordDealInflowUseCase(fakeDealRepo)
        val closeDealWithLossUseCase = CloseDealWithLossUseCase(fakeDealRepo)
        val closeDealUseCase = CloseDealUseCase(fakeDealRepo)
        val revertDealLossUseCase = RevertDealLossUseCase(fakeDealRepo)
        val reopenDealUseCase = ReopenDealUseCase(fakeDealRepo)

        viewModel = DealsViewModel(
            getDealsUseCase = getDealsUseCase,
            walletRepository = fakeWalletRepo,
            transactionRepository = fakeTxRepo,
            saveDealUseCase = saveDealUseCase,
            deleteDealUseCase = deleteDealUseCase,
            recordDealOutlayUseCase = recordDealOutlayUseCase,
            recordDealInflowUseCase = recordDealInflowUseCase,
            closeDealWithLossUseCase = closeDealWithLossUseCase,
            closeDealUseCase = closeDealUseCase,
            revertDealLossUseCase = revertDealLossUseCase,
            reopenDealUseCase = reopenDealUseCase,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads deals and categorizes active vs completed with accurate financial aggregates`() = runTest(testDispatcher) {
        viewModel.state.test {
            val initial = awaitItem()
            val state = if (initial.isLoading) awaitItem() else initial
            // Active deals: deal_1 (10M outlay, 4M recovered => 6M remaining) and deal_2 (5M outlay, 2M recovered => 3M remaining)
            assertEquals(2, state.activeDeals.size)
            assertEquals(1, state.completedDeals.size)

            // Total remaining capital for active deals = 6M + 3M = 9M
            assertEquals(9_000_000L, state.totalActiveRemainingCapital.value)
            // Total outlay for active deals = 10M + 5M = 15M
            assertEquals(15_000_000L, state.totalActiveOutlay.value)
            // Total accumulated profit across all deals = 0 + 0 + 3M = 3M
            assertEquals(3_000_000L, state.totalAccumulatedProfit.value)

            // Overall ROI percentage: total profit 3M / total outlay 30M (10M + 5M + 15M) = 10.0%
            assertEquals(10.0, state.overallRoiPercentage, 0.01)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectTab switches between ACTIVE and COMPLETED tabs`() = runTest(testDispatcher) {
        viewModel.state.test {
            val initial = awaitItem()
            val ready = if (initial.isLoading) awaitItem() else initial
            assertEquals(DealTab.ACTIVE, ready.selectedTab)

            viewModel.selectTab(DealTab.COMPLETED)
            val updated = awaitItem()
            assertEquals(DealTab.COMPLETED, updated.selectedTab)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectDeal sets selectedDeal and filters transactions for that deal`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        val dealTx = FinanceTransaction(
            id = "tx_deal_1",
            amount = Money(2_000_000L),
            type = TransactionType.EXPENSE,
            categoryId = "investment",
            walletId = sampleWallet.id,
            dealId = "deal_1",
            dealFlowType = DealFlowType.OUTLAY_CAPITAL,
            date = Instant.now(),
        )
        fakeTxRepo.emitTransactions(listOf(dealTx))
        advanceUntilIdle()

        viewModel.selectDeal(activeInvestmentDeal)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("deal_1", state.selectedDeal?.id)
        assertEquals(1, state.transactions.size)
        assertEquals("tx_deal_1", state.transactions.first().id)
    }

    @Test
    fun `creates new deal successfully and updates state`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        var callbackCalled = false
        val newDeal = FinancialDeal(
            id = "",
            title = "Dự án Nuôi tôm sinh thái",
            category = DealCategory.INVESTMENT,
            targetAmount = Money(50_000_000L),
        )

        viewModel.createOrUpdateDeal(newDeal) { callbackCalled = true }
        advanceUntilIdle()

        assertTrue(callbackCalled)
        assertEquals("Đã tạo thương vụ thành công", viewModel.state.value.successMessage)
        assertFalse(viewModel.state.value.isSubmitting)
        assertTrue(fakeDealRepo.deals.any { it.title == "Dự án Nuôi tôm sinh thái" })
    }

    @Test
    fun `validates deal title cannot be blank on save`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        var callbackCalled = false
        val blankDeal = FinancialDeal(
            id = "",
            title = "   ",
            category = DealCategory.INVESTMENT,
        )

        viewModel.createOrUpdateDeal(blankDeal) { callbackCalled = true }
        advanceUntilIdle()

        assertFalse(callbackCalled)
        assertEquals("Tiêu đề thương vụ không được để trống", viewModel.state.value.errorMessage)
    }

    @Test
    fun `recordOutlay successfully deducts wallet balance and increases deal outlay`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        var callbackCalled = false
        val outlayAmount = 5_000_000L

        viewModel.recordOutlay(
            deal = activeInvestmentDeal,
            walletId = sampleWallet.id,
            amount = outlayAmount,
            note = "Rót vốn vòng 2",
        ) { callbackCalled = true }
        advanceUntilIdle()

        assertTrue(callbackCalled)
        assertEquals("Đã ghi nhận xuất vốn thành công", viewModel.state.value.successMessage)

        val updatedDeal = fakeDealRepo.deals.find { it.id == "deal_1" }
        assertNotNull(updatedDeal)
        assertEquals(15_000_000L, updatedDeal!!.totalCapitalOutlay.value)
        assertEquals(45_000_000L, fakeWalletRepo.currentWallets.first { it.id == sampleWallet.id }.balance.value)
    }

    @Test
    fun `recordOutlay validates non-empty wallet and positive amount`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        var callbackCalled = false

        // Test amount <= 0
        viewModel.recordOutlay(
            deal = activeInvestmentDeal,
            walletId = sampleWallet.id,
            amount = 0L,
        ) { callbackCalled = true }
        advanceUntilIdle()

        assertFalse(callbackCalled)
        assertEquals("Số tiền xuất vốn phải lớn hơn 0", viewModel.state.value.errorMessage)

        // Test blank walletId
        viewModel.recordOutlay(
            deal = activeInvestmentDeal,
            walletId = "",
            amount = 1_000_000L,
        ) { callbackCalled = true }
        advanceUntilIdle()

        assertFalse(callbackCalled)
        assertEquals("Vui lòng chọn ví xuất vốn", viewModel.state.value.errorMessage)
    }

    @Test
    fun `recordInflow decomposes into principal recovery and capital gain updating ROI`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        var callbackCalled = false
        // activeInvestmentDeal: outlay 10M, recovered 4M => remaining capital is 6M.
        // Inflow 8M: 6M recovers remaining capital, 2M is capital gain!
        val inflowAmount = 8_000_000L

        viewModel.recordInflow(
            deal = activeInvestmentDeal,
            walletId = sampleWallet.id,
            amount = inflowAmount,
            note = "Thu hồi vốn và lợi nhuận vòng cuối",
        ) { callbackCalled = true }
        advanceUntilIdle()

        assertTrue(callbackCalled)
        assertEquals("Đã thu hồi vốn và phân tách dòng tiền thành công", viewModel.state.value.successMessage)

        val updatedDeal = fakeDealRepo.deals.find { it.id == "deal_1" }
        assertNotNull(updatedDeal)
        // Capital recovered should now be 4M + 6M = 10M (fully recovered)
        assertEquals(10_000_000L, updatedDeal!!.totalRecovered.value)
        // Net profit should be 2M
        assertEquals(2_000_000L, updatedDeal.netProfitLoss.value)
        // Deal ROI = 2M / 10M = 20.0%
        assertEquals(20.0, updatedDeal.roiPercentage, 0.01)
        assertTrue(updatedDeal.isFullyRecovered)
    }

    @Test
    fun `recordInflow validates positive amount and non-empty wallet`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        var callbackCalled = false

        // Test negative amount
        viewModel.recordInflow(
            deal = activeInvestmentDeal,
            walletId = sampleWallet.id,
            amount = -500_000L,
        ) { callbackCalled = true }
        advanceUntilIdle()

        assertFalse(callbackCalled)
        assertEquals("Số tiền thu hồi phải lớn hơn 0", viewModel.state.value.errorMessage)

        // Test blank walletId
        viewModel.recordInflow(
            deal = activeInvestmentDeal,
            walletId = "",
            amount = 1_000_000L,
        ) { callbackCalled = true }
        advanceUntilIdle()

        assertFalse(callbackCalled)
        assertEquals("Vui lòng chọn ví nhận tiền", viewModel.state.value.errorMessage)
    }

    @Test
    fun `closeDealWithLoss writes off remaining capital and marks deal completed`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        var callbackCalled = false

        viewModel.closeDealWithLoss(
            deal = activeLendingDeal,
            note = "Chốt lỗ không thu hồi được",
        ) { callbackCalled = true }
        advanceUntilIdle()

        assertTrue(callbackCalled)
        assertEquals("Đã chốt lỗ và đóng thương vụ", viewModel.state.value.successMessage)

        val updated = fakeDealRepo.deals.find { it.id == "deal_2" }
        assertEquals(DealStatus.COMPLETED, updated?.status)
        assertEquals(3_000_000L, updated?.writtenOffCapital?.value)
    }

    @Test
    fun `closeDeal and reopenDeal transition deal status properly`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        var callbackCalled = false

        // Close deal_1
        viewModel.closeDeal("deal_1") { callbackCalled = true }
        advanceUntilIdle()

        assertTrue(callbackCalled)
        assertEquals("Đã tất toán và đóng thương vụ thành công", viewModel.state.value.successMessage)
        assertEquals(DealStatus.COMPLETED, fakeDealRepo.deals.find { it.id == "deal_1" }?.status)

        // Reopen deal_1
        callbackCalled = false
        viewModel.reopenDeal("deal_1") { callbackCalled = true }
        advanceUntilIdle()

        assertTrue(callbackCalled)
        assertEquals("Đã mở lại thương vụ / khoản vay", viewModel.state.value.successMessage)
        assertEquals(DealStatus.ACTIVE, fakeDealRepo.deals.find { it.id == "deal_1" }?.status)
    }

    @Test
    fun `deleteDeal removes deal and clears selection`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        viewModel.selectDeal(activeInvestmentDeal)
        advanceUntilIdle()
        assertEquals("deal_1", viewModel.state.value.selectedDeal?.id)

        var callbackCalled = false
        viewModel.deleteDeal("deal_1") { callbackCalled = true }
        advanceUntilIdle()

        assertTrue(callbackCalled)
        assertEquals("Đã xóa thương vụ", viewModel.state.value.successMessage)
        assertFalse(fakeDealRepo.deals.any { it.id == "deal_1" })
        assertNull(viewModel.state.value.selectedDeal)
    }

    @Test
    fun `clearMessages resets error and success messages`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        viewModel.deleteDeal("")
        advanceUntilIdle()
        assertEquals("ID thương vụ không hợp lệ", viewModel.state.value.errorMessage)

        viewModel.clearMessages()
        advanceUntilIdle()

        assertNull(viewModel.state.value.errorMessage)
        assertNull(viewModel.state.value.successMessage)
    }

    // --- Fake Implementations ---

    private class FakeDealRepository(
        initialDeals: List<FinancialDeal> = emptyList(),
        private val walletRepo: FakeWalletRepository? = null,
    ) : DealRepository {
        private val _dealsFlow = MutableStateFlow(initialDeals)
        val deals: List<FinancialDeal> get() = _dealsFlow.value

        override fun observeDeals(): Flow<List<FinancialDeal>> = _dealsFlow

        override fun observeDeal(dealId: String): Flow<FinancialDeal?> =
            flowOf(_dealsFlow.value.find { it.id == dealId })

        override suspend fun upsertDeal(deal: FinancialDeal): AppResult<String> {
            val id = deal.id.ifBlank { "deal_${System.currentTimeMillis()}" }
            val stored = deal.copy(id = id)
            val current = _dealsFlow.value.toMutableList()
            val index = current.indexOfFirst { it.id == id }
            if (index >= 0) current[index] = stored else current.add(stored)
            _dealsFlow.value = current
            return AppResult.Success(id)
        }

        override suspend fun deleteDeal(dealId: String): AppResult<Unit> {
            _dealsFlow.value = _dealsFlow.value.filterNot { it.id == dealId }
            return AppResult.Success(Unit)
        }

        override suspend fun recordDealOutlay(
            deal: FinancialDeal,
            walletId: String,
            amount: Long,
            date: Instant,
            note: String,
        ): AppResult<Unit> {
            walletRepo?.let { repo ->
                val w = repo.currentWallets.find { it.id == walletId }
                if (w != null) {
                    repo.upsertWallet(w.copy(balance = Money(w.balance.value - amount)))
                }
            }
            val current = _dealsFlow.value.toMutableList()
            val index = current.indexOfFirst { it.id == deal.id }
            if (index >= 0) {
                val existing = current[index]
                current[index] = existing.copy(
                    totalCapitalOutlay = Money(existing.totalCapitalOutlay.value + amount)
                )
                _dealsFlow.value = current
            }
            return AppResult.Success(Unit)
        }

        override suspend fun recordDealInflow(
            deal: FinancialDeal,
            walletId: String,
            amount: Long,
            date: Instant,
            note: String,
        ): AppResult<Unit> {
            walletRepo?.let { repo ->
                val w = repo.currentWallets.find { it.id == walletId }
                if (w != null) {
                    repo.upsertWallet(w.copy(balance = Money(w.balance.value + amount)))
                }
            }
            val current = _dealsFlow.value.toMutableList()
            val index = current.indexOfFirst { it.id == deal.id }
            if (index >= 0) {
                val existing = current[index]
                val rem = existing.remainingCapital.value
                val recoveredPortion = minOf(amount, rem)
                val gainPortion = maxOf(0L, amount - rem)
                current[index] = existing.copy(
                    totalRecovered = Money(existing.totalRecovered.value + recoveredPortion),
                    netProfitLoss = Money(existing.netProfitLoss.value + gainPortion),
                )
                _dealsFlow.value = current
            }
            return AppResult.Success(Unit)
        }

        override suspend fun closeDealWithLoss(
            deal: FinancialDeal,
            date: Instant,
            note: String,
        ): AppResult<Unit> {
            val current = _dealsFlow.value.toMutableList()
            val index = current.indexOfFirst { it.id == deal.id }
            if (index >= 0) {
                val existing = current[index]
                current[index] = existing.copy(
                    writtenOffCapital = Money(existing.remainingCapital.value),
                    status = DealStatus.COMPLETED,
                )
                _dealsFlow.value = current
            }
            return AppResult.Success(Unit)
        }

        override suspend fun closeDeal(dealId: String, date: Instant): AppResult<Unit> {
            val current = _dealsFlow.value.toMutableList()
            val index = current.indexOfFirst { it.id == dealId }
            if (index >= 0) {
                current[index] = current[index].copy(status = DealStatus.COMPLETED)
                _dealsFlow.value = current
            }
            return AppResult.Success(Unit)
        }

        override suspend fun revertDealLoss(dealId: String): AppResult<Unit> {
            val current = _dealsFlow.value.toMutableList()
            val index = current.indexOfFirst { it.id == dealId }
            if (index >= 0) {
                current[index] = current[index].copy(
                    writtenOffCapital = Money(0),
                    status = DealStatus.ACTIVE,
                )
                _dealsFlow.value = current
            }
            return AppResult.Success(Unit)
        }

        override suspend fun reopenDeal(dealId: String): AppResult<Unit> {
            val current = _dealsFlow.value.toMutableList()
            val index = current.indexOfFirst { it.id == dealId }
            if (index >= 0) {
                current[index] = current[index].copy(status = DealStatus.ACTIVE)
                _dealsFlow.value = current
            }
            return AppResult.Success(Unit)
        }
    }

    private class FakeWalletRepository(initialWallets: List<Wallet>) : WalletRepository {
        private val _walletsFlow = MutableStateFlow(initialWallets)
        val currentWallets: List<Wallet> get() = _walletsFlow.value

        override fun observeWallets(): Flow<List<Wallet>> = _walletsFlow

        override suspend fun upsertWallet(wallet: Wallet): AppResult<String> {
            val current = _walletsFlow.value.toMutableList()
            val index = current.indexOfFirst { it.id == wallet.id }
            if (index >= 0) current[index] = wallet else current.add(wallet)
            _walletsFlow.value = current
            return AppResult.Success(wallet.id)
        }

        override suspend fun deleteWallet(wallet: Wallet): AppResult<Unit> {
            _walletsFlow.value = _walletsFlow.value.filterNot { it.id == wallet.id }
            return AppResult.Success(Unit)
        }
    }

    private class FakeTransactionRepository : TransactionRepository {
        private val _txFlow = MutableStateFlow<List<FinanceTransaction>>(emptyList())

        fun emitTransactions(txs: List<FinanceTransaction>) {
            _txFlow.value = txs
        }

        override fun observeRecent(limit: Int): Flow<List<FinanceTransaction>> = _txFlow
        override fun observeMonth(month: YearMonth): Flow<List<FinanceTransaction>> = _txFlow
        override fun observePeriod(start: Instant, endExclusive: Instant): Flow<List<FinanceTransaction>> = _txFlow
        override suspend fun addWithBalanceUpdate(transaction: FinanceTransaction): AppResult<String> = AppResult.Success("tx-1")
        override suspend fun editWithBalanceUpdate(original: FinanceTransaction, updated: FinanceTransaction): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun deleteWithBalanceUpdate(transaction: FinanceTransaction): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun transferBetweenWallets(sourceWalletId: String, destinationWalletId: String, amount: Long, note: String, date: Instant): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun executeSalaryRolloverAtomic(cycleKey: String, sourceWalletId: String, destinationWalletId: String, amount: Long, note: String, date: Instant): AppResult<Unit> = AppResult.Success(Unit)
    }
}
