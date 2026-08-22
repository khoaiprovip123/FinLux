package com.finlux.app.presentation.goal

import app.cash.turbine.test
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinancialGoal
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.GoalRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.DeleteGoalUseCase
import com.finlux.app.domain.usecase.DepositToGoalUseCase
import com.finlux.app.domain.usecase.SaveGoalUseCase
import com.finlux.app.domain.usecase.WithdrawFromGoalUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
class GoalsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var goalRepository: FakeGoalRepository
    private lateinit var walletRepository: FakeWalletRepository
    private lateinit var viewModel: GoalsViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        goalRepository = FakeGoalRepository()
        walletRepository = FakeWalletRepository()
        viewModel = GoalsViewModel(
            repository = goalRepository,
            walletRepository = walletRepository,
            saveGoal = SaveGoalUseCase(goalRepository),
            deleteGoal = DeleteGoalUseCase(goalRepository),
            depositToGoalUseCase = DepositToGoalUseCase(goalRepository),
            withdrawFromGoalUseCase = WithdrawFromGoalUseCase(goalRepository),
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `openDeposit initializes transactionSheet state with DEPOSIT mode`() = runTest(testDispatcher) {
        val sampleGoal = FinancialGoal(
            id = "g-1",
            name = "Mua xe",
            targetAmount = Money(50_000_000L),
            savedAmount = Money(10_000_000L),
            deadline = Instant.now(),
            category = "Ô tô",
            monthlyContribution = Money(5_000_000L),
        )
        viewModel.wallets.test {
            val initial = awaitItem()
            if (initial.isEmpty()) awaitItem()
            viewModel.openDeposit(sampleGoal)

            val state = viewModel.transactionSheet.value
            assertTrue(state.isOpen)
            assertEquals(GoalTransactionMode.DEPOSIT, state.mode)
            assertEquals(sampleGoal, state.goal)
            assertEquals("w-default", state.selectedWalletId)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `openWithdraw initializes transactionSheet state with WITHDRAW mode`() = runTest(testDispatcher) {
        val sampleGoal = FinancialGoal(
            id = "g-1",
            name = "Mua xe",
            targetAmount = Money(50_000_000L),
            savedAmount = Money(10_000_000L),
            deadline = Instant.now(),
            category = "Ô tô",
            monthlyContribution = Money(5_000_000L),
        )
        viewModel.wallets.test {
            val initial = awaitItem()
            if (initial.isEmpty()) awaitItem()
            viewModel.openWithdraw(sampleGoal)

            val state = viewModel.transactionSheet.value
            assertTrue(state.isOpen)
            assertEquals(GoalTransactionMode.WITHDRAW, state.mode)
            assertEquals(sampleGoal, state.goal)
            assertEquals("w-default", state.selectedWalletId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `submitGoalTransaction fails when amount is zero`() = runTest(testDispatcher) {
        val sampleGoal = FinancialGoal(
            id = "g-1",
            name = "Mua xe",
            targetAmount = Money(50_000_000L),
            savedAmount = Money(10_000_000L),
            deadline = Instant.now(),
            category = "Ô tô",
            monthlyContribution = Money(5_000_000L),
        )
        viewModel.openDeposit(sampleGoal, defaultWalletId = "w-default")
        viewModel.setTransactionAmount("0")
        viewModel.submitGoalTransaction()
        advanceUntilIdle()

        assertEquals("Số tiền phải lớn hơn 0", viewModel.transactionSheet.value.error)
    }

    @Test
    fun `submitGoalTransaction executes deposit and closes sheet on success`() = runTest(testDispatcher) {
        val sampleGoal = FinancialGoal(
            id = "g-1",
            name = "Mua xe",
            targetAmount = Money(50_000_000L),
            savedAmount = Money(10_000_000L),
            deadline = Instant.now(),
            category = "Ô tô",
            monthlyContribution = Money(5_000_000L),
        )
        viewModel.openDeposit(sampleGoal, defaultWalletId = "w-default")
        viewModel.setTransactionAmount("2000000")
        viewModel.setTransactionNote("Thưởng tháng")
        viewModel.submitGoalTransaction()
        advanceUntilIdle()

        assertEquals(1, goalRepository.depositCalls)
        assertEquals("g-1", goalRepository.lastDepositGoalId)
        assertEquals(2_000_000L, goalRepository.lastDepositAmount)
        assertFalse(viewModel.transactionSheet.value.isOpen)
    }
}

private class FakeGoalRepository : GoalRepository {
    var depositCalls = 0
    var lastDepositGoalId: String? = null
    var lastDepositAmount: Long? = null

    override fun observeGoals(): Flow<List<FinancialGoal>> = flowOf(emptyList())
    override suspend fun upsertGoal(goal: FinancialGoal): AppResult<String> = AppResult.Success(goal.id)
    override suspend fun deleteGoal(goal: FinancialGoal): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun depositToGoal(
        goalId: String,
        walletId: String,
        amount: Long,
        note: String,
        date: Instant,
    ): AppResult<Unit> {
        depositCalls++
        lastDepositGoalId = goalId
        lastDepositAmount = amount
        return AppResult.Success(Unit)
    }

    override suspend fun withdrawFromGoal(
        goalId: String,
        walletId: String,
        amount: Long,
        note: String,
        date: Instant,
    ): AppResult<Unit> = AppResult.Success(Unit)
}

private class FakeWalletRepository : WalletRepository {
    override fun observeWallets(): Flow<List<Wallet>> = flowOf(
        listOf(
            Wallet("w-default", "Tiền mặt", WalletType.CASH, Money(10_000_000L), "#1F6FBF", true, Instant.now())
        )
    )
    override suspend fun upsertWallet(wallet: Wallet): AppResult<String> = AppResult.Success(wallet.id)
    override suspend fun deleteWallet(wallet: Wallet): AppResult<Unit> = AppResult.Success(Unit)
}
