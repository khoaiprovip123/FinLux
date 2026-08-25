package com.finlux.app.presentation.settings.salary

import app.cash.turbine.test
import com.finlux.app.core.common.AppResult
import com.finlux.app.core.time.FinanceClock
import com.finlux.app.domain.model.CycleRolloverRule
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.PaydayRuleType
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.DefaultSalaryCycleCalculator
import com.finlux.app.domain.usecase.ValidateSalaryCycleConfigUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
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
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class SalaryCycleViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private val fixedNow = LocalDateTime.of(2026, 8, 24, 15, 0).atZone(zone).toInstant()

    private val fixedClock = object : FinanceClock {
        override val zoneId: ZoneId = zone
        override fun now(): Instant = fixedNow
    }

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads config, wallets and computes previews correctly`() = runTest(testDispatcher) {
        val repo = FakeSalaryCycleRepo(SalaryCycleConfig(enabled = true, paydayDay = 25))
        val walletRepo = FakeWalletRepo(
            listOf(
                Wallet("w1", "Ví chính", WalletType.BANK, Money(10_000_000L), "#123456", true, Instant.now()),
            ),
        )

        val vm = SalaryCycleViewModel(
            salaryCycleRepository = repo,
            walletRepository = walletRepo,
            calculator = DefaultSalaryCycleCalculator(),
            validator = ValidateSalaryCycleConfigUseCase(),
            clock = fixedClock,
        )

        testScheduler.advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertTrue(state.currentCyclePreview.isNotBlank())
            assertEquals(1, state.wallets.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setPaydayDay clamps values and updates previews`() = runTest(testDispatcher) {
        val repo = FakeSalaryCycleRepo(SalaryCycleConfig(enabled = true, paydayDay = 25))
        val walletRepo = FakeWalletRepo(emptyList())

        val vm = SalaryCycleViewModel(
            salaryCycleRepository = repo,
            walletRepository = walletRepo,
            calculator = DefaultSalaryCycleCalculator(),
            validator = ValidateSalaryCycleConfigUseCase(),
            clock = fixedClock,
        )

        testScheduler.advanceUntilIdle()
        vm.setPaydayDay(10)

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(10, state.config.paydayDay)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveConfig fails when rollover rule requires savings wallet but none provided`() = runTest(testDispatcher) {
        val repo = FakeSalaryCycleRepo(SalaryCycleConfig(enabled = true, paydayDay = 25))
        val walletRepo = FakeWalletRepo(emptyList())

        val vm = SalaryCycleViewModel(
            salaryCycleRepository = repo,
            walletRepository = walletRepo,
            calculator = DefaultSalaryCycleCalculator(),
            validator = ValidateSalaryCycleConfigUseCase(),
            clock = fixedClock,
        )

        testScheduler.advanceUntilIdle()
        vm.setEnabled(true)
        vm.setRolloverRule(CycleRolloverRule.MOVE_TO_SAVINGS)
        vm.setSavingsWalletId(null)
        vm.saveConfig()
        testScheduler.advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertNotNull(state.errorMessage)
            assertFalse(state.isSaving)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private class FakeSalaryCycleRepo(initial: SalaryCycleConfig) : SalaryCycleRepository {
    private val flow = MutableStateFlow(initial)
    override fun observeConfig(): Flow<SalaryCycleConfig> = flow
    override suspend fun saveConfig(config: SalaryCycleConfig): AppResult<Unit> {
        flow.value = config
        return AppResult.Success(Unit)
    }
    override suspend fun isRolloverProcessed(cycleKey: String): Boolean = false
    override suspend fun markRolloverProcessed(cycleKey: String): AppResult<Unit> = AppResult.Success(Unit)
}

private class FakeWalletRepo(private val list: List<Wallet>) : WalletRepository {
    override fun observeWallets(): Flow<List<Wallet>> = flowOf(list)
    override suspend fun upsertWallet(wallet: Wallet): AppResult<String> = AppResult.Success(wallet.id)
    override suspend fun deleteWallet(wallet: Wallet): AppResult<Unit> = AppResult.Success(Unit)
}
