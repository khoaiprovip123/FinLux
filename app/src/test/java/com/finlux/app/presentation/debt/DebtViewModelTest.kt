package com.finlux.app.presentation.debt

import app.cash.turbine.test
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtType
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.PayoffStrategy
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.DebtRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.CalculatePayoffStrategyUseCase
import com.finlux.app.domain.usecase.DeleteDebtAccountUseCase
import com.finlux.app.domain.usecase.GetDebtsUseCase
import com.finlux.app.domain.usecase.ProcessDebtPaymentUseCase
import com.finlux.app.domain.usecase.SaveDebtAccountUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class DebtViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeDebtRepository = FakeDebtRepo()
    private val fakeWalletRepository = FakeWalletRepo()
    private val fakeTransactionRepository = FakeTransactionRepo()
    private val fakeCategoryRepository = FakeCategoryRepo()
    private val fakeDebtPreferenceRepository = FakeDebtPreferenceRepo()

    private val getDebtsUseCase = GetDebtsUseCase(fakeDebtRepository)
    private val getDebtPaymentHistoryUseCase = com.finlux.app.domain.usecase.GetDebtPaymentHistoryUseCase(fakeDebtRepository)
    private val analyzeDebtCashflowUseCase = com.finlux.app.domain.usecase.AnalyzeDebtCashflowUseCase()
    private val calculatePayoffStrategyUseCase = CalculatePayoffStrategyUseCase()
    private val saveDebtAccountUseCase = SaveDebtAccountUseCase(fakeDebtRepository)
    private val deleteDebtAccountUseCase = DeleteDebtAccountUseCase(fakeDebtRepository)
    private val processDebtPaymentUseCase = ProcessDebtPaymentUseCase(fakeDebtRepository)

    private lateinit var viewModel: DebtViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DebtViewModel(
            getDebtsUseCase = getDebtsUseCase,
            getDebtPaymentHistoryUseCase = getDebtPaymentHistoryUseCase,
            walletRepository = fakeWalletRepository,
            transactionRepository = fakeTransactionRepository,
            categoryRepository = fakeCategoryRepository,
            analyzeDebtCashflowUseCase = analyzeDebtCashflowUseCase,
            calculatePayoffStrategyUseCase = calculatePayoffStrategyUseCase,
            saveDebtAccountUseCase = saveDebtAccountUseCase,
            deleteDebtAccountUseCase = deleteDebtAccountUseCase,
            processDebtPaymentUseCase = processDebtPaymentUseCase,
            debtPreferenceRepository = fakeDebtPreferenceRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState loads debts and computes payoff plan`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            val initial = awaitItem()
            val state = if (initial.isLoading) awaitItem() else initial

            assertEquals(1, state.debts.size)
            assertEquals("Thẻ tín dụng VCB", state.debts.first().name)
            assertEquals(18_500_000L, state.totalRemainingDebt.value)
            assertNotNull(state.payoffPlan)
            assertEquals(PayoffStrategy.SNOWBALL, state.strategy)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setStrategy updates strategy and recomputes plan`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            val initial = awaitItem()
            if (initial.isLoading) awaitItem()

            viewModel.setStrategy(PayoffStrategy.AVALANCHE)
            val updated = awaitItem()

            assertEquals(PayoffStrategy.AVALANCHE, updated.strategy)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setExtraMonthlyPayment increases payoff speed`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            val initial = awaitItem()
            val loadedState = if (initial.isLoading) awaitItem() else initial
            val initialMonths = loadedState.payoffPlan?.totalMonths ?: 0

            viewModel.setExtraMonthlyPayment(5_000_000L)
            val updated = awaitItem()

            val newMonths = updated.payoffPlan?.totalMonths ?: 0
            assertEquals(5_000_000L, updated.extraMonthlyPayment)
            assert(newMonths <= initialMonths)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `payDebt succeeds and updates message`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            val initial = awaitItem()
            if (initial.isLoading) awaitItem()

            var successCallbackCalled = false

            viewModel.payDebt(
                debtId = "debt-1",
                walletId = "wallet-1",
                amount = 1_000_000L,
                principalPaid = 800_000L,
                interestPaid = 200_000L,
                onSuccess = { successCallbackCalled = true },
            )

            val state = awaitItem()
            val finalState = if (state.successMessage == null) awaitItem() else state
            assertEquals("Thanh toán nợ thành công!", finalState.successMessage)
            assertEquals(1, fakeDebtRepository.paymentCalls)
            assert(successCallbackCalled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveDebt succeeds and updates message`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            val initial = awaitItem()
            if (initial.isLoading) awaitItem()

            var successCallbackCalled = false
            val newDebt = DebtAccount(
                name = "Vay Mua Xe",
                type = DebtType.BANK_LOAN,
                totalAmount = Money(200_000_000L),
                remainingBalance = Money(150_000_000L),
                interestRateApr = 9.5,
                minimumPayment = Money(5_000_000L),
                dueDate = 10,
            )

            viewModel.saveDebt(newDebt, onSuccess = { successCallbackCalled = true })

            val state = awaitItem()
            val finalState = if (state.successMessage == null) awaitItem() else state
            assertEquals("Lưu khoản nợ thành công", finalState.successMessage)
            assertEquals(1, fakeDebtRepository.saveCalls)
            assert(successCallbackCalled)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private class FakeDebtRepo : DebtRepository {
    var paymentCalls = 0
    var saveCalls = 0
    var deleteCalls = 0

    private val sampleDebt = DebtAccount(
        id = "debt-1",
        name = "Thẻ tín dụng VCB",
        type = DebtType.CREDIT_CARD,
        totalAmount = Money(50_000_000L),
        remainingBalance = Money(18_500_000L),
        interestRateApr = 20.0,
        minimumPayment = Money(1_500_000L),
        dueDate = 20,
    )

    override fun observeDebts(): Flow<List<DebtAccount>> = flowOf(listOf(sampleDebt))
    override fun observePaymentHistory(debtId: String) = flowOf(emptyList<com.finlux.app.domain.model.DebtPaymentHistory>())
    override fun observeAllPaymentHistory() = flowOf(emptyList<com.finlux.app.domain.model.DebtPaymentHistory>())
    override suspend fun upsertDebt(debt: DebtAccount): AppResult<String> {
        saveCalls++
        return AppResult.Success(debt.id)
    }
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
    ): AppResult<Unit> {
        paymentCalls++
        return AppResult.Success(Unit)
    }
}

private class FakeWalletRepo : WalletRepository {
    private val sampleWallet = Wallet(
        id = "wallet-1",
        name = "Tiền mặt",
        type = WalletType.CASH,
        balance = Money(10_000_000L),
        colorHex = "#3478F6",
        isDefault = true,
        createdAt = Instant.now(),
    )

    override fun observeWallets(): Flow<List<Wallet>> = flowOf(listOf(sampleWallet))
    override suspend fun upsertWallet(wallet: Wallet): AppResult<String> = AppResult.Success(wallet.id)
    override suspend fun deleteWallet(wallet: Wallet): AppResult<Unit> = AppResult.Success(Unit)
}

private class FakeDebtPreferenceRepo : com.finlux.app.domain.repository.DebtPreferenceRepository {
    private val strategyFlow = kotlinx.coroutines.flow.MutableStateFlow(PayoffStrategy.SNOWBALL)
    private val extraPaymentFlow = kotlinx.coroutines.flow.MutableStateFlow(0L)

    override fun observePayoffStrategy(): Flow<PayoffStrategy> = strategyFlow
    override suspend fun savePayoffStrategy(strategy: PayoffStrategy) {
        strategyFlow.value = strategy
    }

    override fun observeExtraMonthlyPayment(): Flow<Long> = extraPaymentFlow
    override suspend fun saveExtraMonthlyPayment(amount: Long) {
        extraPaymentFlow.value = amount
    }
}

private class FakeTransactionRepo : com.finlux.app.domain.repository.TransactionRepository {
    override fun observeRecent(limit: Int): Flow<List<com.finlux.app.domain.model.FinanceTransaction>> = flowOf(emptyList())
    override fun observeMonth(month: java.time.YearMonth): Flow<List<com.finlux.app.domain.model.FinanceTransaction>> = flowOf(emptyList())
    override suspend fun addWithBalanceUpdate(transaction: com.finlux.app.domain.model.FinanceTransaction): AppResult<String> = AppResult.Success("tx-1")
    override suspend fun editWithBalanceUpdate(original: com.finlux.app.domain.model.FinanceTransaction, updated: com.finlux.app.domain.model.FinanceTransaction): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun deleteWithBalanceUpdate(transaction: com.finlux.app.domain.model.FinanceTransaction): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun transferBetweenWallets(sourceWalletId: String, destinationWalletId: String, amount: Long, note: String, date: Instant): AppResult<Unit> = AppResult.Success(Unit)
}

private class FakeCategoryRepo : com.finlux.app.domain.repository.CategoryRepository {
    override fun observeCategories(): Flow<List<com.finlux.app.domain.model.Category>> = flowOf(emptyList())
    override suspend fun upsertCategory(category: com.finlux.app.domain.model.Category): AppResult<String> = AppResult.Success(category.id)
    override suspend fun deleteCategory(category: com.finlux.app.domain.model.Category): AppResult<Unit> = AppResult.Success(Unit)
}
