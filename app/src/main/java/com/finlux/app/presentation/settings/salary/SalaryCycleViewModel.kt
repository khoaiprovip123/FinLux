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
import com.finlux.app.domain.model.SalaryCycleConfigRecord
import com.finlux.app.domain.usecase.FinancialPeriodResolver
import com.finlux.app.domain.usecase.ReconcileBudgetOnCycleChangeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class SalaryCycleUiState(
    val config: SalaryCycleConfig = SalaryCycleConfig(),
    val initialConfig: SalaryCycleConfig = SalaryCycleConfig(),
    val timeline: List<SalaryCycleConfigRecord> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val currentCyclePreview: String = "",
    val nextCyclePreview: String = "",
    val isSaving: Boolean = false,
    val showTransitionDialog: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    val hasCycleOrPaydayChanged: Boolean
        get() = (config.paydayDay != initialConfig.paydayDay ||
                config.scheduleType != initialConfig.scheduleType ||
                config.secondPaydayDay != initialConfig.secondPaydayDay ||
                config.paydayRuleType != initialConfig.paydayRuleType ||
                config.budgetPeriodBasis != initialConfig.budgetPeriodBasis) &&
                (config.enabled || initialConfig.enabled)
}

@HiltViewModel
class SalaryCycleViewModel @Inject constructor(
    private val salaryCycleRepository: SalaryCycleRepository,
    private val walletRepository: WalletRepository,
    private val calculator: SalaryCycleCalculator,
    private val validator: ValidateSalaryCycleConfigUseCase,
    private val clock: FinanceClock,
    private val reconcileBudgetUseCase: ReconcileBudgetOnCycleChangeUseCase,
    private val periodResolver: FinancialPeriodResolver,
    private val scheduler: SalaryCycleScheduler? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalaryCycleUiState())
    val uiState = _uiState.asStateFlow()

    private var isConfigInitialized = false

    init {
        viewModelScope.launch {
            combine(
                salaryCycleRepository.observeConfig(),
                walletRepository.observeWallets(),
                salaryCycleRepository.observeTimeline(),
            ) { config, wallets, timeline ->
                Triple(config, wallets, timeline)
            }.collect { (config, wallets, timeline) ->
                _uiState.update { current ->
                    if (!isConfigInitialized) {
                        isConfigInitialized = true
                        current.copy(
                            config = config,
                            initialConfig = config,
                            timeline = timeline,
                            wallets = wallets,
                        ).withPreviews(calculator, clock)
                    } else {
                        current.copy(
                            timeline = timeline,
                            wallets = wallets,
                        )
                    }
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

    fun setScheduleType(scheduleType: com.finlux.app.domain.model.SalaryScheduleType) {
        _uiState.update { current ->
            val updatedConfig = current.config.copy(
                scheduleType = scheduleType,
                // Default second payday day to 10 if not set
                secondPaydayDay = if (scheduleType == com.finlux.app.domain.model.SalaryScheduleType.SEMI_MONTHLY) {
                    current.config.secondPaydayDay ?: 10
                } else current.config.secondPaydayDay,
            )
            current.copy(config = updatedConfig).withPreviews(calculator, clock)
        }
    }

    fun setSecondPaydayDay(day: Int) {
        val clamped = day.coerceIn(1, 31)
        _uiState.update { it.copy(config = it.config.copy(secondPaydayDay = clamped)).withPreviews(calculator, clock) }
    }

    fun setSecondSalaryWalletId(walletId: String?) {
        _uiState.update { it.copy(config = it.config.copy(secondSalaryWalletId = walletId)) }
    }

    fun setSecondExpectedSalary(amount: Long?) {
        _uiState.update {
            it.copy(
                config = it.config.copy(
                    secondExpectedSalary = if (amount != null && amount > 0) Money(amount) else null,
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

    fun dismissTransitionDialog() {
        _uiState.update { it.copy(showTransitionDialog = false) }
    }

    fun onSaveClicked(onSuccess: (() -> Unit)? = null) {
        val currentConfig = _uiState.value.config
        when (val validationResult = validator(currentConfig)) {
            is com.finlux.app.core.common.AppResult.Error -> {
                _uiState.update { it.copy(errorMessage = validationResult.message) }
                return
            }
            is com.finlux.app.core.common.AppResult.Success -> Unit
        }

        if (_uiState.value.hasCycleOrPaydayChanged && _uiState.value.initialConfig.enabled) {
            _uiState.update { it.copy(showTransitionDialog = true) }
        } else {
            saveDirectly(onSuccess)
        }
    }

    fun saveConfig(onSuccess: (() -> Unit)? = null) {
        onSaveClicked(onSuccess)
    }

    fun saveDirectly(onSuccess: (() -> Unit)? = null) {
        val currentConfig = _uiState.value.config
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            when (val result = salaryCycleRepository.saveConfig(currentConfig)) {
                is com.finlux.app.core.common.AppResult.Success -> {
                    val zone = FinanceTime.zoneOf(currentConfig.financeTimeZone)
                    val now = clock.now()
                    val todayDate = now.atZone(zone).toLocalDate()
                    val todayStr = todayDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

                    if (currentConfig.enabled) {
                        val timeline = _uiState.value.timeline.ifEmpty {
                            salaryCycleRepository.observeTimeline().firstOrNull().orEmpty()
                        }
                        val activeRecord = timeline.firstOrNull { it.effectiveToDate == null }
                        val recordToSave = activeRecord?.copy(config = currentConfig) ?: SalaryCycleConfigRecord(
                            id = "rec_$todayStr",
                            effectiveFromDate = todayStr,
                            effectiveToDate = null,
                            config = currentConfig,
                            createdAt = now,
                        )
                        salaryCycleRepository.saveConfigRecord(recordToSave)
                        scheduler?.scheduleNextPayday(currentConfig)
                    } else {
                        scheduler?.cancel()
                    }
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            initialConfig = currentConfig,
                            showTransitionDialog = false,
                            successMessage = "Đã lưu cấu hình chu kỳ tài chính",
                        )
                    }
                    onSuccess?.invoke()
                }
                is com.finlux.app.core.common.AppResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun applyTransitionNextCycle(onSuccess: (() -> Unit)? = null) {
        val currentConfig = _uiState.value.config
        val initialConfig = _uiState.value.initialConfig
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val now = clock.now()
            val zone = FinanceTime.zoneOf(currentConfig.financeTimeZone)
            val currentCycle = calculator.cycleContaining(now, initialConfig, zone)
            val nextCycleStartDate = currentCycle.endExclusive.atZone(zone).toLocalDate()
            val nextCycleStartStr = nextCycleStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

            val timeline = _uiState.value.timeline.ifEmpty {
                salaryCycleRepository.observeTimeline().firstOrNull().orEmpty()
            }
            val activeRecord = timeline.firstOrNull { it.effectiveToDate == null }

            if (activeRecord != null) {
                val updatedOldRecord = activeRecord.copy(effectiveToDate = nextCycleStartStr)
                salaryCycleRepository.saveConfigRecord(updatedOldRecord)
            } else {
                val oldCycleStartDate = currentCycle.start.atZone(zone).toLocalDate()
                val oldCycleStartStr = oldCycleStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val oldRecord = SalaryCycleConfigRecord(
                    id = "rec_$oldCycleStartStr",
                    effectiveFromDate = oldCycleStartStr,
                    effectiveToDate = nextCycleStartStr,
                    config = initialConfig,
                    createdAt = now,
                )
                salaryCycleRepository.saveConfigRecord(oldRecord)
            }

            val newRecord = SalaryCycleConfigRecord(
                id = "rec_$nextCycleStartStr",
                effectiveFromDate = nextCycleStartStr,
                effectiveToDate = null,
                config = currentConfig,
                createdAt = now,
            )
            salaryCycleRepository.saveConfigRecord(newRecord)

            if (initialConfig.enabled) {
                scheduler?.scheduleNextPayday(initialConfig)
            } else {
                scheduler?.cancel()
            }

            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            _uiState.update {
                it.copy(
                    isSaving = false,
                    showTransitionDialog = false,
                    initialConfig = currentConfig,
                    successMessage = "Đã lên lịch áp dụng chu kỳ mới từ ngày ${nextCycleStartDate.format(formatter)}",
                )
            }
            onSuccess?.invoke()
        }
    }

    fun applyTransitionImmediate(applyProration: Boolean = true, onSuccess: (() -> Unit)? = null) {
        val currentConfig = _uiState.value.config
        val initialConfig = _uiState.value.initialConfig
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val now = clock.now()
            val zone = FinanceTime.zoneOf(currentConfig.financeTimeZone)
            val todayDate = now.atZone(zone).toLocalDate()
            val todayStr = todayDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

            val timeline = _uiState.value.timeline.ifEmpty {
                salaryCycleRepository.observeTimeline().firstOrNull().orEmpty()
            }
            val activeRecord = timeline.firstOrNull { it.effectiveToDate == null }

            if (activeRecord != null) {
                val updatedOldRecord = activeRecord.copy(effectiveToDate = todayStr)
                salaryCycleRepository.saveConfigRecord(updatedOldRecord)
            } else {
                val currentCycle = calculator.cycleContaining(now, initialConfig, zone)
                val oldCycleStartDate = currentCycle.start.atZone(zone).toLocalDate()
                val oldCycleStartStr = oldCycleStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val oldRecord = SalaryCycleConfigRecord(
                    id = "rec_$oldCycleStartStr",
                    effectiveFromDate = oldCycleStartStr,
                    effectiveToDate = todayStr,
                    config = initialConfig,
                    createdAt = now,
                )
                salaryCycleRepository.saveConfigRecord(oldRecord)
            }

            val newRecord = SalaryCycleConfigRecord(
                id = "rec_$todayStr",
                effectiveFromDate = todayStr,
                effectiveToDate = null,
                config = currentConfig,
                createdAt = now,
            )
            salaryCycleRepository.saveConfigRecord(newRecord)
            salaryCycleRepository.saveConfig(currentConfig)

            val sourcePeriod = periodResolver.resolvePeriodContaining(now, initialConfig)
            val newPeriod = periodResolver.resolvePeriodContaining(now, currentConfig)
            reconcileBudgetUseCase(
                newPeriod = newPeriod,
                sourcePeriod = sourcePeriod,
                applyProration = applyProration,
            )

            if (currentConfig.enabled) {
                scheduler?.scheduleNextPayday(currentConfig)
            } else {
                scheduler?.cancel()
            }

            _uiState.update {
                it.copy(
                    isSaving = false,
                    showTransitionDialog = false,
                    initialConfig = currentConfig,
                    successMessage = "Đã áp dụng chu kỳ mới từ hôm nay",
                )
            }
            onSuccess?.invoke()
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

        val currentMacro = "$currentStartStr - $currentEndStr"
        val previewWithSubCycles = if (config.scheduleType == com.finlux.app.domain.model.SalaryScheduleType.SEMI_MONTHLY && current.subCycles.size == 2) {
            val s1 = current.subCycles[0]
            val s2 = current.subCycles[1]
            val s1Start = s1.start.atZone(zone).format(formatter)
            val s1End = s1.endExclusive.atZone(zone).minusDays(1).format(formatter)
            val s2Start = s2.start.atZone(zone).format(formatter)
            val s2End = s2.endExclusive.atZone(zone).minusDays(1).format(formatter)
            "$currentMacro\n• ${s1.label}: $s1Start - $s1End\n• ${s2.label}: $s2Start - $s2End"
        } else {
            currentMacro
        }

        return copy(
            currentCyclePreview = previewWithSubCycles,
            nextCyclePreview = "$nextStartStr - $nextEndStr",
        )
    }
}
