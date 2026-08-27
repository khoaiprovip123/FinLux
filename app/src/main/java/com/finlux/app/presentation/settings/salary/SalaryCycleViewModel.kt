package com.finlux.app.presentation.settings.salary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.time.FinanceClock
import com.finlux.app.core.time.FinanceTime
import com.finlux.app.domain.model.BudgetPeriodBasis
import com.finlux.app.domain.model.CycleRolloverRule
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.PaydayRuleType
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.SalaryCycleScheduler
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.SalaryCycleCalculator
import com.finlux.app.domain.usecase.ValidateSalaryCycleConfigUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class SalaryCycleUiState(
    val config: SalaryCycleConfig = SalaryCycleConfig(),
    val wallets: List<Wallet> = emptyList(),
    val currentCyclePreview: String = "",
    val nextCyclePreview: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class SalaryCycleViewModel @Inject constructor(
    private val salaryCycleRepository: SalaryCycleRepository,
    private val walletRepository: WalletRepository,
    private val calculator: SalaryCycleCalculator,
    private val validator: ValidateSalaryCycleConfigUseCase,
    private val clock: FinanceClock,
    private val scheduler: SalaryCycleScheduler? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalaryCycleUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                salaryCycleRepository.observeConfig(),
                walletRepository.observeWallets(),
            ) { config, wallets ->
                config to wallets
            }.collect { (config, wallets) ->
                _uiState.update { current ->
                    current.copy(
                        config = config,
                        wallets = wallets,
                    ).withPreviews(calculator, clock)
                }
            }
        }
    }

    fun setEnabled(enabled: Boolean) {
        _uiState.update { it.copy(config = it.config.copy(enabled = enabled)).withPreviews(calculator, clock) }
    }

    fun setPaydayRuleType(ruleType: PaydayRuleType) {
        _uiState.update { it.copy(config = it.config.copy(paydayRuleType = ruleType)).withPreviews(calculator, clock) }
    }

    fun setPaydayDay(day: Int) {
        val clamped = day.coerceIn(1, 31)
        _uiState.update { it.copy(config = it.config.copy(paydayDay = clamped)).withPreviews(calculator, clock) }
    }

    fun setSalaryWalletId(walletId: String?) {
        _uiState.update { it.copy(config = it.config.copy(salaryWalletId = walletId)) }
    }

    fun setSavingsWalletId(walletId: String?) {
        _uiState.update { it.copy(config = it.config.copy(savingsWalletId = walletId)) }
    }

    fun setExpectedSalary(amount: Long?) {
        _uiState.update {
            it.copy(
                config = it.config.copy(
                    expectedSalary = if (amount != null && amount > 0) Money(amount) else null,
                ),
            )
        }
    }

    fun setRolloverRule(rule: CycleRolloverRule) {
        _uiState.update { it.copy(config = it.config.copy(rolloverRule = rule)) }
    }

    fun setBudgetPeriodBasis(basis: BudgetPeriodBasis) {
        _uiState.update { it.copy(config = it.config.copy(budgetPeriodBasis = basis)) }
    }

    fun saveConfig(onSuccess: (() -> Unit)? = null) {
        val currentConfig = _uiState.value.config
        when (val validationResult = validator(currentConfig)) {
            is com.finlux.app.core.common.AppResult.Error -> {
                _uiState.update { it.copy(errorMessage = validationResult.message) }
                return
            }
            is com.finlux.app.core.common.AppResult.Success -> Unit
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            when (val result = salaryCycleRepository.saveConfig(currentConfig)) {
                is com.finlux.app.core.common.AppResult.Success -> {
                    if (currentConfig.enabled) {
                        scheduler?.scheduleNextPayday(currentConfig)
                    } else {
                        scheduler?.cancel()
                    }
                    _uiState.update { it.copy(isSaving = false, successMessage = "Đã lưu cấu hình chu kỳ tài chính") }
                    onSuccess?.invoke()
                }
                is com.finlux.app.core.common.AppResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun SalaryCycleUiState.withPreviews(
        calculator: SalaryCycleCalculator,
        clock: FinanceClock,
    ): SalaryCycleUiState {
        val now = clock.now()
        val zone = FinanceTime.zoneOf(config.financeTimeZone)
        val current = calculator.cycleContaining(now, config, zone)
        val next = calculator.cycleContaining(current.endExclusive.plusMillis(1), config, zone)

        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val currentStartStr = current.start.atZone(zone).format(formatter)
        val currentEndStr = current.endExclusive.atZone(zone).minusDays(1).format(formatter)
        val nextStartStr = next.start.atZone(zone).format(formatter)
        val nextEndStr = next.endExclusive.atZone(zone).minusDays(1).format(formatter)

        return copy(
            currentCyclePreview = "$currentStartStr - $currentEndStr",
            nextCyclePreview = "$nextStartStr - $nextEndStr",
        )
    }
}
