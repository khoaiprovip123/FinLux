package com.finlux.app.presentation.savingspin.settings

import com.finlux.app.core.time.FinanceClock
import com.finlux.app.data.demo.DemoSalaryCycleRepository
import com.finlux.app.data.demo.DemoSavingSpinRepository
import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.model.SavingSpinStep
import com.finlux.app.domain.repository.SavingSpinScheduler
import com.finlux.app.domain.usecase.DefaultFinancialPeriodResolver
import com.finlux.app.domain.usecase.DefaultSalaryCycleCalculator
import com.finlux.app.domain.usecase.ValidateSavingSpinConfigUseCase
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SavingSpinSettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val now = Instant.parse("2026-08-31T02:00:00Z")
    private val clock = object : FinanceClock {
        override val zoneId = ZoneId.of("Asia/Ho_Chi_Minh")
        override fun now() = now
    }

    @BeforeEach fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterEach fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `turning feature off cancels reminder`() = runTest(dispatcher) {
        val fixture = fixture(SavingSpinConfig(enabled = true))
        advanceUntilIdle()

        fixture.viewModel.setEnabled(false)
        advanceUntilIdle()

        assertFalse(fixture.viewModel.uiState.value.config.enabled)
        verify(atLeast = 1) { fixture.scheduler.cancel() }
    }

    @Test
    fun `turning feature on schedules next reminder`() = runTest(dispatcher) {
        val fixture = fixture(SavingSpinConfig(enabled = false))
        advanceUntilIdle()

        fixture.viewModel.setEnabled(true)
        advanceUntilIdle()

        assertTrue(fixture.viewModel.uiState.value.config.enabled)
        verify(exactly = 1) { fixture.scheduler.schedule(any(), any()) }
    }

    @Test
    fun `invalid amount cannot be saved`() = runTest(dispatcher) {
        val fixture = fixture()
        advanceUntilIdle()
        fixture.viewModel.setStep(SavingSpinStep.TEN_THOUSAND)
        fixture.viewModel.setMinAmount("11000")
        fixture.viewModel.save()
        advanceUntilIdle()

        assertNotNull(fixture.viewModel.uiState.value.validationMessage)
        assertFalse(fixture.viewModel.uiState.value.saved)
    }

    @Test
    fun `unsupported slot count cannot be saved`() = runTest(dispatcher) {
        val fixture = fixture()
        advanceUntilIdle()
        fixture.viewModel.setSlotCount(7)
        fixture.viewModel.save()
        advanceUntilIdle()

        assertNotNull(fixture.viewModel.uiState.value.validationMessage)
        assertFalse(fixture.viewModel.uiState.value.saved)
    }

    private suspend fun fixture(config: SavingSpinConfig = SavingSpinConfig()): Fixture {
        val repository = DemoSavingSpinRepository().also { it.saveConfig(config) }
        val scheduler = mockk<SavingSpinScheduler>(relaxed = true)
        return Fixture(
            SavingSpinSettingsViewModel(
                repository = repository,
                salaryCycleRepository = DemoSalaryCycleRepository(),
                scheduler = scheduler,
                validator = ValidateSavingSpinConfigUseCase(),
                financialPeriodResolver = DefaultFinancialPeriodResolver(DefaultSalaryCycleCalculator()),
                clock = clock,
            ),
            scheduler,
        )
    }

    private data class Fixture(
        val viewModel: SavingSpinSettingsViewModel,
        val scheduler: SavingSpinScheduler,
    )
}
