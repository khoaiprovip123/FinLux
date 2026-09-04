package com.finlux.app.presentation.savingspin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.common.AppResult
import com.finlux.app.core.time.FinanceClock
import com.finlux.app.domain.model.SavingMethod
import com.finlux.app.domain.model.SavingSpinStatus
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.SavingSpinRepository
import com.finlux.app.domain.repository.SavingSpinScheduler
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.CalculateSavingSpinStreakUseCase
import com.finlux.app.domain.usecase.CompleteSavingSpinUseCase
import com.finlux.app.domain.usecase.GetOrCreateSavingSpinSessionUseCase
import com.finlux.app.domain.usecase.ResolveSavingSpinScheduleKeyUseCase
import com.finlux.app.domain.usecase.SpinSavingWheelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SavingSpinViewModel @Inject constructor(
    private val repository: SavingSpinRepository,
    salaryCycleRepository: SalaryCycleRepository,
    private val walletRepository: WalletRepository,
    private val resolveScheduleKey: ResolveSavingSpinScheduleKeyUseCase,
    private val getOrCreateSession: GetOrCreateSavingSpinSessionUseCase,
    private val spinWheel: SpinSavingWheelUseCase,
    private val completeSavingSpin: CompleteSavingSpinUseCase,
    private val calculateStreak: CalculateSavingSpinStreakUseCase,
    private val scheduler: SavingSpinScheduler,
    private val clock: FinanceClock,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(SavingSpinUiState())
    val uiState = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            walletRepository.observeWallets().collect { wallets ->
                mutableUiState.update { state ->
                    val defaultSource = wallets.firstOrNull { it.type == WalletType.CASH }?.id
                        ?: wallets.firstOrNull { it.isDefault }?.id
                        ?: wallets.firstOrNull()?.id
                    val source = state.sourceWalletId
                        ?.takeIf { id -> wallets.any { it.id == id } }
                        ?: defaultSource

                    state.copy(
                        wallets = wallets,
                        sourceWalletId = source,
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.observeDestinations().collect { destinations ->
                mutableUiState.update { state ->
                    val selected = state.selectedDestinationId
                        ?.takeIf { id -> destinations.any { it.id == id && it.enabled } }
                        ?: state.config.defaultDestinationId?.takeIf { id -> destinations.any { it.id == id && it.enabled } }
                        ?: destinations.firstOrNull { it.enabled }?.id
                    state.copy(destinations = destinations, selectedDestinationId = selected)
                }
            }
        }
        viewModelScope.launch {
            combine(
                repository.observeSessions(clock.now().minusSeconds(180L * 86400L), clock.now().plusSeconds(86400L)),
                repository.observeConfig(),
                salaryCycleRepository.observeConfig(),
            ) { sessions, config, salaryConfig ->
                calculateStreak(config, sessions, clock.now(), salaryConfig)
            }.collect { streakResult ->
                mutableUiState.update {
                    it.copy(
                        streakCount = streakResult.currentStreak,
                        longestStreak = streakResult.longestStreak,
                    )
                }
            }
        }
        viewModelScope.launch {
            combine(repository.observeConfig(), salaryCycleRepository.observeConfig(), ::Pair)
                .collectLatest { (config, salaryConfig) ->
                    mutableUiState.update { it.copy(config = config, errorMessage = null) }
                    if (!config.enabled) {
                        mutableUiState.update { it.copy(isLoading = false, scheduleKey = null, session = null) }
                        return@collectLatest
                    }
                    val schedule = resolveScheduleKey(config, salaryConfig, clock.now())
                    if (schedule == null) {
                        mutableUiState.update { it.copy(isLoading = false, scheduleKey = null, session = null) }
                        return@collectLatest
                    }
                    mutableUiState.update { it.copy(isLoading = true, scheduleKey = schedule.value) }
                    when (val result = getOrCreateSession(schedule.value, config)) {
                        is AppResult.Error -> mutableUiState.update {
                            it.copy(isLoading = false, errorMessage = result.message)
                        }
                        is AppResult.Success -> repository.observeSession(schedule.value).collect { session ->
                            mutableUiState.update { state ->
                                state.copy(
                                    isLoading = false,
                                    session = session ?: result.value,
                                )
                            }
                        }
                    }
                }
        }
    }

    fun onAction(action: SavingSpinAction) {
        when (action) {
            SavingSpinAction.OpenGame -> mutableUiState.update { it.copy(isGameOpen = true) }
            SavingSpinAction.CloseGame -> mutableUiState.update { it.copy(isGameOpen = false, isSnoozeSheetOpen = false) }
            SavingSpinAction.Spin -> spin()
            SavingSpinAction.WheelAnimationFinished -> {
                mutableUiState.update { it.copy(isWheelAnimating = false) }
            }
            is SavingSpinAction.SelectDestination -> mutableUiState.update {
                it.copy(selectedDestinationId = action.id, errorMessage = null)
            }
            is SavingSpinAction.SelectSourceWallet -> mutableUiState.update {
                it.copy(sourceWalletId = action.id, errorMessage = null)
            }
            SavingSpinAction.OpenSnoozeSheet -> mutableUiState.update { it.copy(isSnoozeSheetOpen = true) }
            SavingSpinAction.CloseSnoozeSheet -> mutableUiState.update { it.copy(isSnoozeSheetOpen = false) }
            SavingSpinAction.ConfirmDeposit -> confirmDeposit()
            is SavingSpinAction.Snooze -> snooze(action.until)
            SavingSpinAction.Skip -> skip()
            SavingSpinAction.DismissError -> mutableUiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun spin() = viewModelScope.launch {
        val state = uiState.value
        if (state.isSpinning) return@launch
        val session = state.session ?: return@launch
        if (session.status != SavingSpinStatus.READY) {
            mutableUiState.update { it.copy(errorMessage = "Lượt tiết kiệm này đã được quay") }
            return@launch
        }
        mutableUiState.update { it.copy(isSpinning = true, isWheelAnimating = true, errorMessage = null) }
        when (val result = spinWheel(session)) {
            is AppResult.Success -> mutableUiState.update { it.copy(isSpinning = false, session = result.value) }
            is AppResult.Error -> mutableUiState.update {
                it.copy(isSpinning = false, isWheelAnimating = false, errorMessage = result.message)
            }
        }
    }

    private fun confirmDeposit() = viewModelScope.launch {
        val state = uiState.value
        if (state.isConfirming) return@launch
        val session = state.session ?: return@launch

        val destination = state.destinations.firstOrNull { it.id == state.selectedDestinationId }
        if (destination == null) {
            mutableUiState.update { it.copy(errorMessage = "Bạn chưa chọn nơi tiết kiệm") }
            return@launch
        }

        mutableUiState.update { it.copy(isConfirming = true, errorMessage = null) }

        when (val result = completeSavingSpin(
            session = session,
            destination = destination,
            sourceWalletId = state.sourceWalletId,
        )) {
            is AppResult.Success -> {
                scheduler.cancel()
                mutableUiState.update {
                    it.copy(
                        isConfirming = false,
                        session = session.copy(
                            status = SavingSpinStatus.COMPLETED,
                            destinationId = destination.id,
                            method = destination.method,
                            completedAt = clock.now(),
                        ),
                        isGameOpen = false,
                    )
                }
            }
            is AppResult.Error -> mutableUiState.update {
                it.copy(isConfirming = false, errorMessage = result.message)
            }
        }
    }

    private fun snooze(until: Instant) = viewModelScope.launch {
        val session = uiState.value.session ?: return@launch
        mutableUiState.update { it.copy(isLoading = true, errorMessage = null) }
        when (val result = repository.snoozeSession(session.scheduleKey, until)) {
            is AppResult.Success -> {
                scheduler.snooze(until)
                mutableUiState.update {
                    it.copy(
                        isLoading = false,
                        isSnoozeSheetOpen = false,
                        session = session.copy(status = SavingSpinStatus.SNOOZED, snoozedUntil = until),
                        isGameOpen = false,
                    )
                }
            }
            is AppResult.Error -> mutableUiState.update { it.copy(isLoading = false, errorMessage = result.message) }
        }
    }

    private fun skip() = viewModelScope.launch {
        val session = uiState.value.session ?: return@launch
        mutableUiState.update { it.copy(isLoading = true, errorMessage = null) }
        when (val result = repository.skipSession(session.scheduleKey)) {
            is AppResult.Success -> {
                scheduler.cancel()
                mutableUiState.update {
                    it.copy(
                        isLoading = false,
                        session = session.copy(status = SavingSpinStatus.SKIPPED, skippedAt = clock.now()),
                        isGameOpen = false,
                    )
                }
            }
            is AppResult.Error -> mutableUiState.update { it.copy(isLoading = false, errorMessage = result.message) }
        }
    }
}
