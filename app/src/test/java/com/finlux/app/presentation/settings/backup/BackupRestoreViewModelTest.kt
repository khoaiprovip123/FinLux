package com.finlux.app.presentation.settings.backup

import android.content.Context
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.UserProfile
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.model.backup.BackupPreviewSummary
import com.finlux.app.domain.model.backup.RestoreReport
import com.finlux.app.domain.model.backup.RestoreStrategy
import com.finlux.app.domain.repository.AuthRepository
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.DealRepository
import com.finlux.app.domain.repository.DebtRepository
import com.finlux.app.domain.repository.GoalRepository
import com.finlux.app.domain.repository.ReminderRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.backup.ExportBackupUseCase
import com.finlux.app.domain.usecase.backup.RestoreBackupUseCase
import com.finlux.app.domain.usecase.backup.ValidateBackupUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import androidx.core.content.FileProvider
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class BackupRestoreViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val mockAuthRepo = mockk<AuthRepository>(relaxed = true)
    private val mockWalletRepo = mockk<WalletRepository>(relaxed = true)
    private val mockCategoryRepo = mockk<CategoryRepository>(relaxed = true)
    private val mockTransactionRepo = mockk<TransactionRepository>(relaxed = true)
    private val mockBudgetRepo = mockk<BudgetRepository>(relaxed = true)
    private val mockDebtRepo = mockk<DebtRepository>(relaxed = true)
    private val mockGoalRepo = mockk<GoalRepository>(relaxed = true)
    private val mockReminderRepo = mockk<ReminderRepository>(relaxed = true)
    private val mockDealRepo = mockk<DealRepository>(relaxed = true)
    private val mockExportUseCase = mockk<ExportBackupUseCase>(relaxed = true)
    private val mockValidateUseCase = mockk<ValidateBackupUseCase>(relaxed = true)
    private val mockRestoreUseCase = mockk<RestoreBackupUseCase>(relaxed = true)
    private val mockContext = mockk<Context>(relaxed = true)

    private lateinit var viewModel: BackupRestoreViewModel

    @BeforeEach
    fun setUp() {
        mockkStatic(FileProvider::class)
        every { FileProvider.getUriForFile(any(), any(), any()) } returns mockk(relaxed = true)

        Dispatchers.setMain(testDispatcher)

        val user = UserProfile(uid = "uid_123", displayName = "Test User", email = "test@finlux.app")
        every { mockAuthRepo.currentUser } returns flowOf(user)
        every { mockWalletRepo.observeWallets() } returns flowOf(
            listOf(
                Wallet(id = "w1", name = "Cash", type = WalletType.CASH, balance = Money(100_000L), colorHex = "#000", isDefault = false, createdAt = Instant.now())
            )
        )
        every { mockCategoryRepo.observeCategories() } returns flowOf(
            listOf(
                Category(id = "c1", name = "Food", type = CategoryType.EXPENSE, icon = "food", colorHex = "#000", isDefault = false, createdAt = Instant.now())
            )
        )
        every { mockTransactionRepo.observePeriod(any(), any()) } returns flowOf(
            listOf(
                FinanceTransaction(id = "tx1", walletId = "w1", amount = Money(50_000L), type = TransactionType.EXPENSE, categoryId = "c1", date = Instant.now())
            )
        )
        every { mockBudgetRepo.observeBudgets(any()) } returns flowOf(emptyList())
        every { mockDebtRepo.observeDebts() } returns flowOf(emptyList())
        every { mockGoalRepo.observeGoals() } returns flowOf(emptyList())
        every { mockReminderRepo.observeReminders() } returns flowOf(emptyList())
        every { mockDealRepo.observeDeals() } returns flowOf(emptyList())
        every { mockContext.packageName } returns "com.finlux.app"
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(FileProvider::class)
        Dispatchers.resetMain()
    }

    private fun createViewModel(): BackupRestoreViewModel {
        return BackupRestoreViewModel(
            authRepository = mockAuthRepo,
            walletRepository = mockWalletRepo,
            categoryRepository = mockCategoryRepo,
            transactionRepository = mockTransactionRepo,
            budgetRepository = mockBudgetRepo,
            debtRepository = mockDebtRepo,
            goalRepository = mockGoalRepo,
            reminderRepository = mockReminderRepo,
            dealRepository = mockDealRepo,
            exportBackupUseCase = mockExportUseCase,
            validateBackupUseCase = mockValidateUseCase,
            restoreBackupUseCase = mockRestoreUseCase,
            context = mockContext,
            ioDispatcher = testDispatcher,
        )
    }

    @Test
    fun `init loads current user and calculates data stats`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("test@finlux.app", state.currentUserEmail)
        assertEquals("uid_123", state.currentUserId)
        assertTrue(state.isCloudSyncing)
        assertEquals(1, state.dataStats.walletCount)
        assertEquals(1, state.dataStats.categoryCount)
        assertEquals(1, state.dataStats.transactionCount)
        assertFalse(state.isStatsLoading)
    }

    @Test
    fun `exportBackup success updates state and notifies caller`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        val dummyFile = File("dummy.finlux")
        coEvery { mockExportUseCase("uid_123") } returns AppResult.Success(dummyFile)

        var shareCalled = false
        viewModel.exportBackup { shareCalled = true }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isExporting)
        assertEquals("Tạo bản sao lưu thành công!", state.exportSuccessMessage)
        assertNull(state.exportError)
        assertTrue(shareCalled)
    }

    @Test
    fun `exportBackup failure sets exportError in state`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        coEvery { mockExportUseCase("uid_123") } returns AppResult.Error("Dung lượng bộ nhớ đầy")

        viewModel.exportBackup()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isExporting)
        assertEquals("Dung lượng bộ nhớ đầy", state.exportError)
        assertNull(state.exportSuccessMessage)
    }

    @Test
    fun `selectStrategy updates selectedStrategy in state`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(RestoreStrategy.SMART_MERGE, viewModel.uiState.value.selectedStrategy)

        viewModel.selectStrategy(RestoreStrategy.FULL_OVERWRITE)
        assertEquals(RestoreStrategy.FULL_OVERWRITE, viewModel.uiState.value.selectedStrategy)
    }

    @Test
    fun `requestRestore with FULL_OVERWRITE triggers confirmation dialog`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.selectStrategy(RestoreStrategy.FULL_OVERWRITE)
        viewModel.requestRestore()

        assertTrue(viewModel.uiState.value.showFullOverwriteConfirmDialog)

        viewModel.dismissConfirmDialog()
        assertFalse(viewModel.uiState.value.showFullOverwriteConfirmDialog)
    }

    @Test
    fun `confirmAndRestore with valid JSON executes restore successfully`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        val report = RestoreReport(
            strategy = RestoreStrategy.SMART_MERGE,
            walletsRestored = 2,
            transactionsRestored = 10,
            categoriesRestored = 5,
            budgetsRestored = 1,
            debtsRestored = 0,
            debtPaymentsRestored = 0,
            goalsRestored = 0,
            remindersRestored = 0,
            dealsRestored = 0,
            skippedCount = 0,
            conflictsResolved = 0,
            balanceAuditPassed = true,
            balanceDiscrepancies = emptyList(),
            durationMs = 120L,
        )
        coEvery { mockRestoreUseCase(any(), any(), any()) } returns AppResult.Success(report)

        val json = """{"schemaVersion":1,"wallets":[],"transactions":[]}"""
        val mockPreview = mockk<BackupPreviewSummary>(relaxed = true)
        every { mockValidateUseCase(content = json, fileSizeBytes = any(), currentUserId = any()) } returns AppResult.Success(mockPreview)

        viewModel.onFileContentReady("test.finlux", 1024L, json)
        advanceUntilIdle()

        assertEquals(mockPreview, viewModel.uiState.value.previewSummary)
        assertEquals("test.finlux", viewModel.uiState.value.selectedFileName)

        viewModel.confirmAndRestore()
        advanceUntilIdle()

        assertEquals(report, viewModel.uiState.value.restoreReport)
        assertFalse(viewModel.uiState.value.isRestoring)
        assertNull(viewModel.uiState.value.restoreError)
    }

    @Test
    fun `confirmAndRestore without cached JSON sets restoreError`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.confirmAndRestore()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.restoreError)
        assertNull(viewModel.uiState.value.restoreReport)
    }

    @Test
    fun `clearSelectedFile clears preview summary and cached data`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.clearSelectedFile()

        assertNull(viewModel.uiState.value.previewSummary)
        assertNull(viewModel.uiState.value.validationError)
        assertNull(viewModel.uiState.value.cachedJsonForRestore)
        assertNull(viewModel.uiState.value.selectedFileName)
    }
}
