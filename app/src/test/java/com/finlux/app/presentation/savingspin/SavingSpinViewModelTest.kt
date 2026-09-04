package com.finlux.app.presentation.savingspin

import app.cash.turbine.test
import com.finlux.app.core.common.AppResult
import com.finlux.app.core.time.FinanceClock
import com.finlux.app.data.demo.DemoSalaryCycleRepository
import com.finlux.app.data.demo.DemoSavingSpinRepository
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SavingDestination
import com.finlux.app.domain.model.SavingMethod
import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.model.SavingSpinStatus
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.SavingSpinScheduler
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.CalculateSavingSpinStreakUseCase
import com.finlux.app.domain.usecase.CompleteSavingSpinUseCase
import com.finlux.app.domain.usecase.DefaultFinancialPeriodResolver
import com.finlux.app.domain.usecase.DefaultSalaryCycleCalculator
import com.finlux.app.domain.usecase.GenerateSavingSpinWheelUseCase
import com.finlux.app.domain.usecase.GetOrCreateSavingSpinSessionUseCase
import com.finlux.app.domain.usecase.ResolveSavingSpinScheduleKeyUseCase
import com.finlux.app.domain.usecase.SpinSavingWheelUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class SavingSpinViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val now = Instant.parse("2026-08-31T02:00:00Z")
    private val clock = object : FinanceClock {
        override val zoneId: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh")
        override fun now(): Instant = now
    }

    @BeforeEach fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterEach fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `disabled config loads without a session`() = runTest(dispatcher) {
        val fixture = fixture(enabled = false)
        advanceUntilIdle()

        fixture.viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertFalse(state.config.enabled)
            assertEquals(null, state.session)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `enabled config loads ready session and spin is locked`() = runTest(dispatcher) {
        val fixture = fixture(enabled = true)
        advanceUntilIdle()
        assertEquals(SavingSpinStatus.READY, fixture.viewModel.uiState.value.session?.status)

        fixture.viewModel.onAction(SavingSpinAction.Spin)
        advanceUntilIdle()

        val session = fixture.viewModel.uiState.value.session
        assertEquals(SavingSpinStatus.SPUN_PENDING, session?.status)
        assertNotNull(session?.selectedAmount)
        assertFalse(fixture.viewModel.uiState.value.isSpinning)
    }

    @Test
    fun `confirm destination completes and cancels reminder`() = runTest(dispatcher) {
        val fixture = fixture(enabled = true, withDestination = true)
        advanceUntilIdle()
        fixture.viewModel.onAction(SavingSpinAction.Spin)
        advanceUntilIdle()
        fixture.viewModel.onAction(SavingSpinAction.ConfirmDeposit)
        advanceUntilIdle()

        assertEquals(SavingSpinStatus.COMPLETED, fixture.viewModel.uiState.value.session?.status)
        verify(exactly = 1) { fixture.scheduler.cancel() }
    }

    @Test
    fun `snooze persists instant and schedules it`() = runTest(dispatcher) {
        val fixture = fixture(enabled = true)
        advanceUntilIdle()
        fixture.viewModel.onAction(SavingSpinAction.Spin)
        advanceUntilIdle()
        val until = now.plusSeconds(1_800)
        fixture.viewModel.onAction(SavingSpinAction.Snooze(until))
        advanceUntilIdle()

        assertEquals(SavingSpinStatus.SNOOZED, fixture.viewModel.uiState.value.session?.status)
        verify(exactly = 1) { fixture.scheduler.snooze(until) }
    }

    @Test
    fun `skip marks session skipped and closes sheet`() = runTest(dispatcher) {
        val fixture = fixture(enabled = true)
        advanceUntilIdle()
        fixture.viewModel.onAction(SavingSpinAction.OpenGame)
        fixture.viewModel.onAction(SavingSpinAction.Spin)
        advanceUntilIdle()
        fixture.viewModel.onAction(SavingSpinAction.Skip)
        advanceUntilIdle()

        assertEquals(SavingSpinStatus.SKIPPED, fixture.viewModel.uiState.value.session?.status)
        assertFalse(fixture.viewModel.uiState.value.isGameOpen)
    }

    private suspend fun fixture(enabled: Boolean, withDestination: Boolean = false): Fixture {
        val repository = DemoSavingSpinRepository()
        repository.saveConfig(SavingSpinConfig(enabled = enabled, minAmount = Money(10_000), maxAmount = Money(100_000)))
        if (withDestination) repository.upsertDestination(SavingDestination("piggy", "Heo đất", SavingMethod.CASH))
        val scheduler = mockk<SavingSpinScheduler>(relaxed = true)
        val transactionRepository = mockk<TransactionRepository>(relaxed = true)
        coEvery { transactionRepository.addWithBalanceUpdate(any()) } returns AppResult.Success("tx-id")
        coEvery { transactionRepository.transferBetweenWallets(any(), any(), any(), any(), any()) } returns AppResult.Success(Unit)
        val walletRepository = mockk<WalletRepository>(relaxed = true)
        every { walletRepository.observeWallets() } returns flowOf(
            listOf(
                Wallet(
                    id = "test-wallet-1",
                    name = "Ví Tiền Mặt",
                    balance = Money(1_000_000),
                    type = WalletType.CASH,
                    colorHex = "#2563EB",
                    isDefault = true,
                    createdAt = now,
                )
            )
        )
        val financialResolver = DefaultFinancialPeriodResolver(DefaultSalaryCycleCalculator())
        return Fixture(
            viewModel = SavingSpinViewModel(
                repository = repository,
                salaryCycleRepository = DemoSalaryCycleRepository(),
                walletRepository = walletRepository,
                resolveScheduleKey = ResolveSavingSpinScheduleKeyUseCase(financialResolver),
                getOrCreateSession = GetOrCreateSavingSpinSessionUseCase(repository, GenerateSavingSpinWheelUseCase()),
                spinWheel = SpinSavingWheelUseCase(repository),
                completeSavingSpin = CompleteSavingSpinUseCase(repository, transactionRepository, walletRepository, clock),
                calculateStreak = CalculateSavingSpinStreakUseCase(financialResolver, clock),
                scheduler = scheduler,
                clock = clock,
            ),
            scheduler = scheduler,
        )
    }

    private data class Fixture(val viewModel: SavingSpinViewModel, val scheduler: SavingSpinScheduler)
}
