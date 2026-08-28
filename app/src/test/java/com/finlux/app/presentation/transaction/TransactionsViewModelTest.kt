package com.finlux.app.presentation.transaction

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.DeleteTransactionUseCase
import com.finlux.app.domain.usecase.AddTransactionUseCase
import com.finlux.app.domain.usecase.DefaultFinancialPeriodResolver
import com.finlux.app.domain.usecase.DefaultSalaryCycleCalculator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val transactionRepository: TransactionRepository = mockk(relaxed = true)
    private val categoryRepository: CategoryRepository = mockk(relaxed = true)
    private val walletRepository: WalletRepository = mockk(relaxed = true)
    private val salaryCycleRepository: SalaryCycleRepository = mockk(relaxed = true)
    private val deleteTransactionUseCase: DeleteTransactionUseCase = mockk(relaxed = true)
    private val addTransactionUseCase: AddTransactionUseCase = mockk(relaxed = true)

    private val sampleWallet = Wallet(id = "w1", name = "Ví tiền mặt", type = WalletType.CASH, balance = Money(1_000_000), colorHex = "#FFFFFF", isDefault = true, createdAt = Instant.now())
    private val sampleCat = Category(id = "c1", name = "Ăn uống", type = CategoryType.EXPENSE, icon = "food", colorHex = "#FF5722", isDefault = true, createdAt = Instant.now())
    private val sampleTx = FinanceTransaction(
        id = "tx1",
        type = TransactionType.EXPENSE,
        amount = Money(50_000),
        categoryId = "c1",
        walletId = "w1",
        note = "Ăn phở",
        date = Instant.now(),
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { transactionRepository.observeRecent(any()) } returns flowOf(listOf(sampleTx))
        coEvery { categoryRepository.observeCategories() } returns flowOf(listOf(sampleCat))
        coEvery { walletRepository.observeWallets() } returns flowOf(listOf(sampleWallet))
        coEvery { salaryCycleRepository.observeConfig() } returns flowOf(SalaryCycleConfig())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = TransactionsViewModel(
        repository = transactionRepository,
        categoryRepository = categoryRepository,
        walletRepository = walletRepository,
        salaryCycleRepository = salaryCycleRepository,
        financialPeriodResolver = DefaultFinancialPeriodResolver(DefaultSalaryCycleCalculator()),
        deleteTransaction = deleteTransactionUseCase,
        addTransaction = addTransactionUseCase,
    )

    @Test
    fun `initial state loads transactions categories and wallets`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.transactions.collect()
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.categories.collect()
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.wallets.collect()
        }
        advanceUntilIdle()

        assertEquals(1, viewModel.transactions.value.size)
        assertEquals("tx1", viewModel.transactions.value.first().id)
        assertTrue(viewModel.categories.value.containsKey("c1"))
        assertTrue(viewModel.wallets.value.containsKey("w1"))
    }

    @Test
    fun `delete transaction calls deleteTransactionUseCase`() = runTest(testDispatcher) {
        coEvery { deleteTransactionUseCase.invoke(any()) } returns AppResult.Success(Unit)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.delete(sampleTx)
        advanceUntilIdle()

        coVerify(exactly = 1) { deleteTransactionUseCase.invoke(sampleTx) }
    }

    @Test
    fun `search ignores Vietnamese diacritics and amount range is applied`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.transactions.collect() }

        viewModel.setSearchQuery("an pho")
        viewModel.setAmountRange(40_000L, 60_000L)
        advanceUntilIdle()

        assertEquals(listOf("tx1"), viewModel.transactions.value.map(FinanceTransaction::id))

        viewModel.setAmountRange(60_001L, 100_000L)
        advanceUntilIdle()
        assertTrue(viewModel.transactions.value.isEmpty())
    }

    @Test
    fun `restore transaction delegates to atomic add use case`() = runTest(testDispatcher) {
        coEvery { addTransactionUseCase.invoke(sampleTx) } returns AppResult.Success(sampleTx.id)
        val viewModel = createViewModel()

        viewModel.restore(sampleTx)
        advanceUntilIdle()

        coVerify(exactly = 1) { addTransactionUseCase.invoke(sampleTx) }
    }
}
