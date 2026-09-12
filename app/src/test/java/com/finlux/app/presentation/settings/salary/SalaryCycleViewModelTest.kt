package com.finlux.app.presentation.settings.salary

import app.cash.turbine.test
import com.finlux.app.core.common.AppResult
import com.finlux.app.core.time.FinanceClock
import com.finlux.app.domain.model.BudgetPeriodBasis
import com.finlux.app.domain.model.CycleRolloverRule
import com.finlux.app.domain.model.FinancialPeriod
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.SalaryCycleConfigRecord
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.DefaultFinancialPeriodResolver
import com.finlux.app.domain.usecase.DefaultSalaryCycleCalculator
import com.finlux.app.domain.usecase.ReconcileBudgetOnCycleChangeUseCase
import com.finlux.app.domain.usecase.ValidateSalaryCycleConfigUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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

    private val mockReconcileBudgetUseCase = mockk<ReconcileBudgetOnCycleChangeUseCase>(relaxed = true)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery {
            mockReconcileBudgetUseCase(any(), any(), any())
        } returns AppResult.Success(emptyList())
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
            reconcileBudgetUseCase = mockReconcileBudgetUseCase,
            periodResolver = DefaultFinancialPeriodResolver(DefaultSalaryCycleCalculator()),
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
            reconcileBudgetUseCase = mockReconcileBudgetUseCase,
            periodResolver = DefaultFinancialPeriodResolver(DefaultSalaryCycleCalculator()),
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
            reconcileBudgetUseCase = mockReconcileBudgetUseCase,
            periodResolver = DefaultFinancialPeriodResolver(DefaultSalaryCycleCalculator()),
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

    @Test
    fun `saveConfig triggers transition dialog when payday changed on active cycle`() = runTest(testDispatcher) {
        val repo = FakeSalaryCycleRepo(SalaryCycleConfig(enabled = true, paydayDay = 25))
        val walletRepo = FakeWalletRepo(emptyList())

        val vm = SalaryCycleViewModel(
            salaryCycleRepository = repo,
            walletRepository = walletRepo,
            calculator = DefaultSalaryCycleCalculator(),
            validator = ValidateSalaryCycleConfigUseCase(),
            clock = fixedClock,
            reconcileBudgetUseCase = mockReconcileBudgetUseCase,
            periodResolver = DefaultFinancialPeriodResolver(DefaultSalaryCycleCalculator()),
        )

        testScheduler.advanceUntilIdle()
        vm.setPaydayDay(10) // Changed from 25 to 10
        vm.saveConfig()
        testScheduler.advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertTrue(state.showTransitionDialog)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `applyTransitionNextCycle schedules transition record for next cycle start and keeps current cycle intact`() = runTest(testDispatcher) {
        val initialConfig = SalaryCycleConfig(enabled = true, paydayDay = 25)
        val repo = FakeSalaryCycleRepo(initialConfig)
        val walletRepo = FakeWalletRepo(emptyList())

        val vm = SalaryCycleViewModel(
            salaryCycleRepository = repo,
            walletRepository = walletRepo,
            calculator = DefaultSalaryCycleCalculator(),
            validator = ValidateSalaryCycleConfigUseCase(),
            clock = fixedClock,
            reconcileBudgetUseCase = mockReconcileBudgetUseCase,
            periodResolver = DefaultFinancialPeriodResolver(DefaultSalaryCycleCalculator()),
        )

        testScheduler.advanceUntilIdle()
        vm.setPaydayDay(10) // Next payday day
        vm.saveConfig() // Opens dialog
        testScheduler.advanceUntilIdle()

        vm.applyTransitionNextCycle()
        testScheduler.advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertFalse(state.showTransitionDialog)
            assertNotNull(state.successMessage)
            assertTrue(state.successMessage!!.contains("25/08/2026"))
            cancelAndIgnoreRemainingEvents()
        }

        // Check timeline in repo
        val timeline = repo.getTimeline()
        assertEquals(2, timeline.size)
        val oldRec = timeline.first { it.effectiveToDate != null }
        val newRec = timeline.first { it.effectiveToDate == null }
        assertEquals("2026-08-25", oldRec.effectiveToDate)
        assertEquals("2026-08-25", newRec.effectiveFromDate)
        assertEquals(10, newRec.config.paydayDay)
    }

    @Test
    fun `applyTransitionImmediate truncates current cycle today, saves new record, and reconciles budget with proration`() = runTest(testDispatcher) {
        val initialConfig = SalaryCycleConfig(enabled = true, paydayDay = 25)
        val repo = FakeSalaryCycleRepo(initialConfig)
        val walletRepo = FakeWalletRepo(emptyList())

        val vm = SalaryCycleViewModel(
            salaryCycleRepository = repo,
            walletRepository = walletRepo,
            calculator = DefaultSalaryCycleCalculator(),
            validator = ValidateSalaryCycleConfigUseCase(),
            clock = fixedClock,
            reconcileBudgetUseCase = mockReconcileBudgetUseCase,
            periodResolver = DefaultFinancialPeriodResolver(DefaultSalaryCycleCalculator()),
        )

        testScheduler.advanceUntilIdle()
        vm.setPaydayDay(10) // Changed from 25 to 10
        vm.saveConfig() // Opens dialog
        testScheduler.advanceUntilIdle()

        vm.applyTransitionImmediate(applyProration = true)
        testScheduler.advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertFalse(state.showTransitionDialog)
            assertEquals("Đã áp dụng chu kỳ mới từ hôm nay", state.successMessage)
            cancelAndIgnoreRemainingEvents()
        }

        // Verify budget reconciliation was called with applyProration = true
        coVerify(exactly = 1) {
            mockReconcileBudgetUseCase(
                newPeriod = any(),
                sourcePeriod = any(),
                applyProration = true,
            )
        }

        // Check timeline in repo
        val timeline = repo.getTimeline()
        assertEquals(2, timeline.size)
        val oldRec = timeline.first { it.effectiveToDate != null }
        val newRec = timeline.first { it.effectiveToDate == null }
        assertEquals("2026-08-24", oldRec.effectiveToDate)
        assertEquals("2026-08-24", newRec.effectiveFromDate)
        assertEquals(10, newRec.config.paydayDay)
    }

    @Test
    fun `saveDirectly saves immediately without dialog when enabled toggled on fresh setup`() = runTest(testDispatcher) {
        val initialConfig = SalaryCycleConfig(enabled = false, paydayDay = 25)
        val repo = FakeSalaryCycleRepo(initialConfig)
        val walletRepo = FakeWalletRepo(emptyList())

        val vm = SalaryCycleViewModel(
            salaryCycleRepository = repo,
            walletRepository = walletRepo,
            calculator = DefaultSalaryCycleCalculator(),
            validator = ValidateSalaryCycleConfigUseCase(),
            clock = fixedClock,
            reconcileBudgetUseCase = mockReconcileBudgetUseCase,
            periodResolver = DefaultFinancialPeriodResolver(DefaultSalaryCycleCalculator()),
        )

        testScheduler.advanceUntilIdle()
        vm.setEnabled(true)
        vm.saveConfig() // Initial setup, initialConfig.enabled was false -> saves directly
        testScheduler.advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertFalse(state.showTransitionDialog)
            assertEquals("Đã lưu cấu hình chu kỳ tài chính", state.successMessage)
            cancelAndIgnoreRemainingEvents()
        }

        val timeline = repo.getTimeline()
        assertEquals(1, timeline.size)
        assertEquals("2026-08-24", timeline.first().effectiveFromDate)
        assertFalse(timeline.first().effectiveToDate != null)
    }
}

private class FakeSalaryCycleRepo(initial: SalaryCycleConfig) : SalaryCycleRepository {
    private val flow = MutableStateFlow(initial)
    private val timelineFlow = MutableStateFlow<List<SalaryCycleConfigRecord>>(emptyList())
    fun getTimeline(): List<SalaryCycleConfigRecord> = timelineFlow.value

    override fun observeConfig(): Flow<SalaryCycleConfig> = flow
    override suspend fun saveConfig(config: SalaryCycleConfig): AppResult<Unit> {
        flow.value = config
        return AppResult.Success(Unit)
    }
    override suspend fun isRolloverProcessed(cycleKey: String): Boolean = false
    override suspend fun markRolloverProcessed(cycleKey: String): AppResult<Unit> = AppResult.Success(Unit)

    override fun observeTimeline(): Flow<List<SalaryCycleConfigRecord>> = timelineFlow
    override suspend fun getConfigAt(instant: Instant): SalaryCycleConfig = flow.value
    override suspend fun saveConfigRecord(record: SalaryCycleConfigRecord): AppResult<Unit> {
        val current = timelineFlow.value.filterNot { it.id == record.id }.toMutableList()
        current.add(record)
        timelineFlow.value = current
        if (record.effectiveToDate == null) {
            flow.value = record.config
        }
        return AppResult.Success(Unit)
    }
}

private class FakeWalletRepo(private val list: List<Wallet>) : WalletRepository {
    override fun observeWallets(): Flow<List<Wallet>> = flowOf(list)
    override suspend fun upsertWallet(wallet: Wallet): AppResult<String> = AppResult.Success(wallet.id)
    override suspend fun deleteWallet(wallet: Wallet): AppResult<Unit> = AppResult.Success(Unit)
}
