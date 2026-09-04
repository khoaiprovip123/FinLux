package com.finlux.app.presentation.savingspin.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.common.AppResult
import com.finlux.app.core.time.FinanceClock
import com.finlux.app.core.time.FinanceTime
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.model.SavingSpinFrequency
import com.finlux.app.domain.model.SavingSpinStep
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.SavingSpinRepository
import com.finlux.app.domain.repository.SavingSpinScheduler
import com.finlux.app.domain.usecase.FinancialPeriodResolver
import com.finlux.app.domain.usecase.ValidateSavingSpinConfigUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SavingSpinSettingsViewModel @Inject constructor(
    private val repository: SavingSpinRepository,
    private val salaryCycleRepository: SalaryCycleRepository,
    private val scheduler: SavingSpinScheduler,
    private val validator: ValidateSavingSpinConfigUseCase,
    private val financialPeriodResolver: FinancialPeriodResolver,
    private val clock: FinanceClock,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(SavingSpinSettingsUiState())
    val uiState = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.observeConfig(), repository.observeDestinations(), ::Pair).collect { (config, destinations) ->
                mutableUiState.update {
                    it.copy(
                        isLoading = false,
                        config = config,
                        minAmountInput = config.minAmount.value.toString(),
                        maxAmountInput = config.maxAmount.value.toString(),
                        destinations = destinations,
                    )
                }
            }
        }
    }

    fun setEnabled(enabled: Boolean) {
        val config = uiState.value.config.copy(enabled = enabled)
        mutableUiState.update { it.copy(config = config) }
        viewModelScope.launch { persist(config) }
    }

    fun setShowOnHome(value: Boolean) = updateConfig { it.copy(showOnHome = value) }
    fun setReminderEnabled(value: Boolean) = updateConfig { it.copy(reminderEnabled = value) }
    fun setSnoozeEnabled(value: Boolean) = updateConfig { it.copy(snoozeEnabled = value) }
    fun setAllowSkip(value: Boolean) = updateConfig { it.copy(allowSkip = value) }
    fun setStep(value: SavingSpinStep) = updateConfig { it.copy(step = value) }
    fun setSlotCount(value: Int) = updateConfig { it.copy(slotCount = value) }
    fun setFrequency(value: SavingSpinFrequency) = updateConfig { it.copy(frequency = value) }
    fun setWeeklyDay(value: Int) = updateConfig { it.copy(weeklyDay = value.coerceIn(1, 7)) }
    fun toggleSelectedWeekday(value: Int) = updateConfig { config ->
        val selected = config.selectedWeekdays.toMutableSet().apply {
            if (!add(value)) remove(value)
        }
        config.copy(selectedWeekdays = selected)
    }
    fun setReminderHour(value: Int) = updateConfig { it.copy(reminderHour = value.coerceIn(0, 23)) }
    fun setReminderMinute(value: Int) = updateConfig { it.copy(reminderMinute = value.coerceIn(0, 59)) }
    fun setDefaultDestination(id: String?) = updateConfig { it.copy(defaultDestinationId = id) }
    fun setMinAmount(value: String) = mutableUiState.update {
        val digits = value.filter(Char::isDigit).take(15)
        val amount = digits.toLongOrNull() ?: 0L
        it.copy(
            minAmountInput = digits,
            config = it.config.copy(minAmount = Money(amount)),
            validationMessage = null,
            saved = false,
        )
    }

    fun setMaxAmount(value: String) = mutableUiState.update {
        val digits = value.filter(Char::isDigit).take(15)
        val amount = digits.toLongOrNull() ?: 0L
        it.copy(
            maxAmountInput = digits,
            config = it.config.copy(maxAmount = Money(amount)),
            validationMessage = null,
            saved = false,
        )
    }

    fun dismissSaved() = mutableUiState.update { it.copy(saved = false) }
    fun clearValidationMessage() = mutableUiState.update { it.copy(validationMessage = null) }

    fun save() = viewModelScope.launch {
        val state = uiState.value
        val min = state.minAmountInput.toLongOrNull() ?: state.config.minAmount.value
        val max = state.maxAmountInput.toLongOrNull() ?: state.config.maxAmount.value
        val config = state.config.copy(
            minAmount = Money(min),
            maxAmount = Money(max),
            updatedAt = clock.now(),
        )
        val validation = validator(config)
        if (!validation.isValid) {
            mutableUiState.update { it.copy(validationMessage = validation.errors.first().message, saved = false) }
            return@launch
        }
        persist(config)
    }

    private suspend fun persist(config: SavingSpinConfig) {
        val validation = validator(config)
        if (!validation.isValid) {
            mutableUiState.update { it.copy(validationMessage = validation.errors.first().message, saved = false) }
            return
        }
        mutableUiState.update { it.copy(isSaving = true, validationMessage = null, saved = false) }
        when (val result = repository.saveConfig(config)) {
            is AppResult.Error -> mutableUiState.update {
                it.copy(isSaving = false, validationMessage = result.message, saved = false)
            }
            is AppResult.Success -> {
                if (!config.enabled || !config.reminderEnabled) scheduler.cancel()
                else scheduler.schedule(config, nextTrigger(config, salaryCycleRepository.observeConfig().first(), clock.now()))
                mutableUiState.update {
                    it.copy(isSaving = false, saved = true, config = config, validationMessage = null)
                }
            }
        }
    }

    private fun nextTrigger(config: SavingSpinConfig, salaryConfig: SalaryCycleConfig, now: Instant): Instant {
        val zone = FinanceTime.zoneOf(salaryConfig.financeTimeZone)
        if (config.frequency == SavingSpinFrequency.SALARY_CYCLE) {
            val current = financialPeriodResolver.resolvePeriodContaining(now, salaryConfig)
            val currentTrigger = current.start.atZone(zone).toLocalDate()
                .atTime(config.reminderHour, config.reminderMinute).atZone(zone).toInstant()
            if (currentTrigger.isAfter(now)) return currentTrigger
            val next = financialPeriodResolver.resolveNextPeriodOf(current, salaryConfig)
            return next.start.atZone(zone).toLocalDate()
                .atTime(config.reminderHour, config.reminderMinute).atZone(zone).toInstant()
        }

        var date = now.atZone(zone).toLocalDate()
        repeat(MAX_SCAN_DAYS) {
            val candidate = date.atTime(config.reminderHour, config.reminderMinute).atZone(zone).toInstant()
            if (candidate.isAfter(now) && isScheduledDate(date, config)) return candidate
            date = date.plusDays(1)
        }
        return now.plusSeconds(86_400)
    }

    private fun isScheduledDate(date: LocalDate, config: SavingSpinConfig): Boolean = when (config.frequency) {
        SavingSpinFrequency.DAILY -> true
        SavingSpinFrequency.SELECTED_WEEKDAYS -> date.dayOfWeek.value in config.selectedWeekdays
        SavingSpinFrequency.WEEKLY -> date.dayOfWeek.value == config.weeklyDay
        SavingSpinFrequency.SALARY_CYCLE -> false
    }

    private fun updateConfig(transform: (SavingSpinConfig) -> SavingSpinConfig) = mutableUiState.update {
        it.copy(config = transform(it.config), validationMessage = null, saved = false)
    }

    companion object { private const val MAX_SCAN_DAYS = 370 }
}
