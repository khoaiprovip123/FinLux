package com.finlux.app.presentation.transaction

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.ReceiptStorageRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.AddTransactionUseCase
import com.finlux.app.domain.usecase.DeleteCategoryUseCase
import com.finlux.app.domain.usecase.EditTransactionUseCase
import com.finlux.app.domain.usecase.SaveCategoryUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val walletRepository: WalletRepository = mockk(relaxed = true)
    private val categoryRepository: CategoryRepository = mockk(relaxed = true)
    private val receiptStorageRepository: ReceiptStorageRepository = mockk(relaxed = true)
    private val addTransactionUseCase: AddTransactionUseCase = mockk(relaxed = true)
    private val editTransactionUseCase: EditTransactionUseCase = mockk(relaxed = true)
    private val saveCategoryUseCase: SaveCategoryUseCase = mockk(relaxed = true)
    private val deleteCategoryUseCase: DeleteCategoryUseCase = mockk(relaxed = true)

    private val sampleWallet = Wallet("w1", "Ví tiền mặt", WalletType.CASH, Money(1_000_000), "#FFFFFF", true, Instant.now())
    private val sampleExpenseCat = Category("c_food", "Ăn uống", CategoryType.EXPENSE, "food", "#FF5722", true, Instant.now())
    private val sampleIncomeCat = Category("c_salary", "Lương", CategoryType.INCOME, "salary", "#4CAF50", true, Instant.now())

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { walletRepository.observeWallets() } returns flowOf(listOf(sampleWallet))
        coEvery { categoryRepository.observeCategories() } returns flowOf(listOf(sampleExpenseCat, sampleIncomeCat))
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): AddTransactionViewModel {
        return AddTransactionViewModel(
            walletRepository = walletRepository,
            categoryRepository = categoryRepository,
            receiptStorageRepository = receiptStorageRepository,
            addTransaction = addTransactionUseCase,
            editTransaction = editTransactionUseCase,
            saveCategory = saveCategoryUseCase,
            deleteCategory = deleteCategoryUseCase,
        )
    }

    @Test
    fun `save new transaction invokes addTransactionUseCase`() = runTest(testDispatcher) {
        coEvery { addTransactionUseCase.invoke(any()) } returns AppResult.Success("tx_new")
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setAmount("250000")
        viewModel.setNote("Ăn trưa bạn bè")
        viewModel.save()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.saved)
        assertFalse(state.isSaving)
        assertNull(state.error)

        coVerify(exactly = 1) {
            addTransactionUseCase.invoke(match {
                it.amount.value == 250_000L && it.note == "Ăn trưa bạn bè" && it.type == TransactionType.EXPENSE
            })
        }
        coVerify(exactly = 0) { editTransactionUseCase.invoke(any(), any()) }
    }

    @Test
    fun `setEditingTransaction populates state and save invokes editTransactionUseCase`() = runTest(testDispatcher) {
        val existingTx = FinanceTransaction(
            id = "tx_999",
            type = TransactionType.EXPENSE,
            amount = Money(150_000),
            categoryId = "c_food",
            walletId = "w1",
            note = "Ăn sáng",
            date = Instant.parse("2026-08-19T02:00:00Z"),
        )
        coEvery { editTransactionUseCase.invoke(any(), any()) } returns AppResult.Success(Unit)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setEditingTransaction(existingTx)
        val stateAfterSet = viewModel.state.value
        assertEquals("150000", stateAfterSet.amountInput)
        assertEquals("Ăn sáng", stateAfterSet.note)
        assertEquals(existingTx, stateAfterSet.editingTransaction)

        // Adjust amount and note
        viewModel.setAmount("180000")
        viewModel.setNote("Ăn sáng + cafe")
        viewModel.save()
        advanceUntilIdle()

        val stateAfterSave = viewModel.state.value
        assertTrue(stateAfterSave.saved)
        assertFalse(stateAfterSave.isSaving)

        coVerify(exactly = 1) {
            editTransactionUseCase.invoke(
                original = existingTx,
                updated = match {
                    it.id == "tx_999" && it.amount.value == 180_000L && it.note == "Ăn sáng + cafe"
                }
            )
        }
        coVerify(exactly = 0) { addTransactionUseCase.invoke(any()) }
    }

    @Test
    fun `consumeSaved resets editing state and clears fields`() = runTest(testDispatcher) {
        val existingTx = FinanceTransaction(
            id = "tx_999",
            type = TransactionType.EXPENSE,
            amount = Money(150_000),
            categoryId = "c_food",
            walletId = "w1",
            note = "Ăn sáng",
            date = Instant.now(),
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setEditingTransaction(existingTx)
        viewModel.consumeSaved()

        val state = viewModel.state.value
        assertFalse(state.saved)
        assertNull(state.editingTransaction)
        assertEquals("", state.amountInput)
        assertEquals("", state.note)
    }

    @Test
    fun `resetForNewTransaction clears previous editing state and resets fields`() = runTest(testDispatcher) {
        val existingTx = FinanceTransaction(
            id = "tx_999",
            type = TransactionType.INCOME,
            amount = Money(150_000),
            categoryId = "c_salary",
            walletId = "w1",
            note = "Lương",
            date = Instant.now(),
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setEditingTransaction(existingTx)
        assertEquals(existingTx, viewModel.state.value.editingTransaction)

        // Reset for new transaction with type EXPENSE
        viewModel.resetForNewTransaction(TransactionType.EXPENSE)
        val state = viewModel.state.value
        assertNull(state.editingTransaction)
        assertEquals("", state.amountInput)
        assertEquals("", state.note)
        assertEquals(TransactionType.EXPENSE, state.type)
        assertEquals("c_food", state.categoryId)
    }
}
