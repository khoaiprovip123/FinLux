package com.finlux.app.presentation.settings.deleteaccount

import android.content.Context
import androidx.core.content.FileProvider
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.FinancialGoal
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.UserProfile
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.AuthRepository
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.DebtRepository
import com.finlux.app.domain.repository.GoalRepository
import com.finlux.app.domain.repository.ReminderRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.account.DeleteAccountUseCase
import com.finlux.app.domain.usecase.backup.ExportBackupUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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
class DeleteAccountViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val mockAuthRepo = mockk<AuthRepository>(relaxed = true)
    private val mockWalletRepo = mockk<WalletRepository>(relaxed = true)
    private val mockTransactionRepo = mockk<TransactionRepository>(relaxed = true)
    private val mockDebtRepo = mockk<DebtRepository>(relaxed = true)
    private val mockGoalRepo = mockk<GoalRepository>(relaxed = true)
    private val mockBudgetRepo = mockk<BudgetRepository>(relaxed = true)
    private val mockReminderRepo = mockk<ReminderRepository>(relaxed = true)
    private val mockExportBackupUseCase = mockk<ExportBackupUseCase>(relaxed = true)
    private val mockDeleteAccountUseCase = mockk<DeleteAccountUseCase>(relaxed = true)
    private val mockContext = mockk<Context>(relaxed = true)

    private lateinit var viewModel: DeleteAccountViewModel

    @BeforeEach
    fun setUp() {
        mockkStatic(FileProvider::class)
        every { FileProvider.getUriForFile(any(), any(), any()) } returns mockk(relaxed = true)

        Dispatchers.setMain(testDispatcher)

        val user = UserProfile(uid = "uid_test_123", displayName = "User A", email = "test@finlux.app")
        every { mockAuthRepo.currentUser } returns flowOf(user)
        every { mockAuthRepo.getAuthProviderId() } returns "password"

        every { mockWalletRepo.observeWallets() } returns flowOf(
            listOf(
                Wallet(id = "w1", name = "Cash", type = WalletType.CASH, balance = Money(50_000L), colorHex = "#000", isDefault = true, createdAt = Instant.now()),
                Wallet(id = "w2", name = "Bank", type = WalletType.BANK, balance = Money(200_000L), colorHex = "#000", isDefault = false, createdAt = Instant.now()),
            )
        )
        every { mockTransactionRepo.observePeriod(any(), any()) } returns flowOf(
            listOf(
                FinanceTransaction(id = "tx1", walletId = "w1", amount = Money(20_000L), type = TransactionType.EXPENSE, categoryId = "cat_food", date = Instant.now()),
                FinanceTransaction(id = "tx2", walletId = "w2", amount = Money(50_000L), type = TransactionType.INCOME, categoryId = "cat_salary", date = Instant.now()),
                FinanceTransaction(id = "tx3", walletId = "w1", amount = Money(10_000L), type = TransactionType.EXPENSE, categoryId = "cat_cafe", date = Instant.now()),
            )
        )
        every { mockDebtRepo.observeDebts() } returns flowOf(listOf(mockk<DebtAccount>(relaxed = true)))
        every { mockGoalRepo.observeGoals() } returns flowOf(listOf(mockk<FinancialGoal>(relaxed = true)))
        every { mockBudgetRepo.observeBudgets(any()) } returns flowOf(listOf(mockk<Budget>(relaxed = true)))
        every { mockReminderRepo.observeReminders() } returns flowOf(
            listOf(
                mockk<Reminder>(relaxed = true),
                mockk<Reminder>(relaxed = true),
            )
        )

        viewModel = DeleteAccountViewModel(
            authRepository = mockAuthRepo,
            walletRepository = mockWalletRepo,
            transactionRepository = mockTransactionRepo,
            debtRepository = mockDebtRepo,
            goalRepository = mockGoalRepo,
            budgetRepository = mockBudgetRepo,
            reminderRepository = mockReminderRepo,
            exportBackupUseCase = mockExportBackupUseCase,
            deleteAccountUseCase = mockDeleteAccountUseCase,
            ioDispatcher = testDispatcher,
            context = mockContext,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(FileProvider::class)
    }

    @Test
    fun `T-DEL-VM-01 should load damage stats correctly on initialization`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingStats)
        assertEquals(2, state.damageStats.walletCount)
        assertEquals(3, state.damageStats.transactionCount)
        assertEquals(1, state.damageStats.debtCount)
        assertEquals(1, state.damageStats.goalCount)
        assertEquals(1, state.damageStats.budgetCount)
        assertEquals(2, state.damageStats.reminderCount)
        assertEquals(10, state.damageStats.totalEntities)
        assertEquals("password", state.providerId)
    }

    @Test
    fun `T-DEL-VM-02 should block deletion when confirmation text is invalid or incomplete`() = runTest {
        advanceUntilIdle()

        // Empty confirmation text
        viewModel.onPasswordChanged("secret123")
        viewModel.onConfirmationInputChanged("")
        assertFalse(viewModel.uiState.value.isConfirmationTextValid)
        assertFalse(viewModel.uiState.value.canExecuteDelete)

        // Wrong casing or misspelled
        viewModel.onConfirmationInputChanged("xoa tai khoan")
        assertFalse(viewModel.uiState.value.isConfirmationTextValid)
        assertFalse(viewModel.uiState.value.canExecuteDelete)

        viewModel.onConfirmationInputChanged("XOA TAI KHOAN")
        assertFalse(viewModel.uiState.value.isConfirmationTextValid)
        assertFalse(viewModel.uiState.value.canExecuteDelete)

        // Exact match with extra whitespace trimmed
        viewModel.onConfirmationInputChanged("  XÓA TÀI KHOẢN  ")
        assertTrue(viewModel.uiState.value.isConfirmationTextValid)
        assertTrue(viewModel.uiState.value.canExecuteDelete)
    }

    @Test
    fun `T-DEL-VM-03 should evaluate canExecuteDelete based on provider type`() = runTest {
        advanceUntilIdle()

        // 1. Password provider: requires non-empty password + exact phrase
        viewModel.onConfirmationInputChanged("XÓA TÀI KHOẢN")
        viewModel.onPasswordChanged("")
        assertFalse(viewModel.uiState.value.isReauthValid)
        assertFalse(viewModel.uiState.value.canExecuteDelete)

        viewModel.onPasswordChanged("myPassword")
        assertTrue(viewModel.uiState.value.isReauthValid)
        assertTrue(viewModel.uiState.value.canExecuteDelete)

        // 2. Google provider: switch provider and check reauth requirements
        every { mockAuthRepo.getAuthProviderId() } returns "google.com"
        val googleViewModel = DeleteAccountViewModel(
            authRepository = mockAuthRepo,
            walletRepository = mockWalletRepo,
            transactionRepository = mockTransactionRepo,
            debtRepository = mockDebtRepo,
            goalRepository = mockGoalRepo,
            budgetRepository = mockBudgetRepo,
            reminderRepository = mockReminderRepo,
            exportBackupUseCase = mockExportBackupUseCase,
            deleteAccountUseCase = mockDeleteAccountUseCase,
            ioDispatcher = testDispatcher,
            context = mockContext,
        )
        advanceUntilIdle()

        assertEquals("google.com", googleViewModel.uiState.value.providerId)
        googleViewModel.onConfirmationInputChanged("XÓA TÀI KHOẢN")
        assertFalse(googleViewModel.uiState.value.isGoogleReauthenticated)
        assertFalse(googleViewModel.uiState.value.canExecuteDelete)

        // Provide Google ID Token
        googleViewModel.onGoogleIdTokenReceived("sample_google_token")
        assertTrue(googleViewModel.uiState.value.isGoogleReauthenticated)
        assertTrue(googleViewModel.uiState.value.isReauthValid)
        assertTrue(googleViewModel.uiState.value.canExecuteDelete)
    }

    @Test
    fun `T-DEL-VM-04 should successfully execute account deletion and trigger isPurgeCompleted`() = runTest {
        advanceUntilIdle()

        coEvery { mockDeleteAccountUseCase(password = "secret123", googleIdToken = null) } returns AppResult.Success(Unit)

        viewModel.onConfirmationInputChanged("XÓA TÀI KHOẢN")
        viewModel.onPasswordChanged("secret123")
        assertTrue(viewModel.uiState.value.canExecuteDelete)

        viewModel.executeDeleteAccount()

        // Let coroutine and simulated delays progress
        advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertFalse(finalState.isPurging)
        assertEquals(PurgingStage.COMPLETED, finalState.purgingStage)
        assertTrue(finalState.isPurgeCompleted)
        assertNull(finalState.errorMessage)
    }

    @Test
    fun `T-DEL-VM-05 should set error message when deleteAccountUseCase fails`() = runTest {
        advanceUntilIdle()

        coEvery { mockDeleteAccountUseCase(any(), any()) } returns AppResult.Error("Mật khẩu không chính xác")

        viewModel.onConfirmationInputChanged("XÓA TÀI KHOẢN")
        viewModel.onPasswordChanged("wrongPassword")

        viewModel.executeDeleteAccount()
        advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertFalse(finalState.isPurging)
        assertFalse(finalState.isPurgeCompleted)
        assertEquals("Mật khẩu không chính xác", finalState.errorMessage)
    }

    @Test
    fun `T-DEL-VM-06 should handle export backup before deletion`() = runTest {
        advanceUntilIdle()

        val tempFile = File.createTempFile("finlux-backup-test", ".finlux")
        coEvery { mockExportBackupUseCase(any()) } returns AppResult.Success(tempFile)

        var callbackCalled = false
        viewModel.exportBackup {
            callbackCalled = true
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isExportingBackup)
        assertEquals("Tạo bản sao lưu thành công!", state.exportBackupSuccessMessage)
        assertNotNull(state.exportedBackupUri)
        assertTrue(callbackCalled)

        tempFile.delete()
    }
}
