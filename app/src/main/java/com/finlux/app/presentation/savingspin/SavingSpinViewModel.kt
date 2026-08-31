package com.finlux.app.presentation.savingspin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.common.AppResult
import com.finlux.app.core.time.FinanceClock
import com.finlux.app.domain.model.SavingSpinStatus
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.SavingSpinRepository
import com.finlux.app.domain.repository.SavingSpinScheduler
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.CompleteSavingSpinUseCase
import com.finlux.app.domain.usecase.GetOrCreateSavingSpinSessionUseCase
import com.finlux.app.domain.usecase.ResolveSavingSpinScheduleKeyUseCase
import com.finlux.app.domain.usecase.SpinSavingWheelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
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
    private val scheduler: SavingSpinScheduler,
    private val clock: FinanceClock,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(SavingSpinUiState())
    val uiState = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            walletRepository.observeWallets().collect { wallets ->
                mutableUiState.update { state ->
                    // Ví nguồn: ưu tiên ví Tiền mặt (CASH) hoặc ví mặc định
                    val defaultSource = wallets.firstOrNull { it.type == com.finlux.app.domain.model.WalletType.CASH }?.id
                        ?: wallets.firstOrNull { it.isDefault }?.id
                        ?: wallets.firstOrNull()?.id
                    val source = state.sourceWalletId
                        ?.takeIf { id -> wallets.any { it.id == id } }
                        ?: defaultSource

                    // Ví đích tiết kiệm: ưu tiên ví khác ví nguồn (ví dụ: heo đất hoặc ngân hàng)
                    val otherWallet = wallets.firstOrNull { it.id != source }?.id
                    val destination = state.selectedWalletId
                        ?.takeIf { id -> wallets.any { it.id == id } }
                        ?: otherWallet
                        ?: source

                    state.copy(
                        wallets = wallets,
                        sourceWalletId = source,
                        selectedWalletId = destination,
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
            // Quan sát lịch sử phiên để tính chuỗi số lần nạp (streak)
            repository.observeSessions(clock.now().minusSeconds(180L * 86400L), clock.now().plusSeconds(86400L))
                .collect { sessions ->
                    val completedCount = sessions.count { it.status == SavingSpinStatus.COMPLETED }
                    // Streak = số lần đã nạp thành công + 1 (lần hiện tại đang cất)
                    mutableUiState.update { it.copy(streakCount = (completedCount + 1).coerceAtLeast(1)) }
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
                            mutableUiState.update { state -> state.copy(
                                isLoading = false,
                                session = session ?: result.value,
                            ) }
                        }
                    }
                }
        }
    }

    fun onAction(action: SavingSpinAction) {
        when (action) {
            SavingSpinAction.OpenGame -> mutableUiState.update { it.copy(isGameOpen = true) }
            SavingSpinAction.CloseGame -> mutableUiState.update { it.copy(isGameOpen = false) }
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
            is SavingSpinAction.SelectWallet -> mutableUiState.update {
                it.copy(selectedWalletId = action.id, errorMessage = null)
            }
            SavingSpinAction.ConfirmDeposit -> confirmDeposit()
            is SavingSpinAction.Snooze -> snooze(action.until)
            SavingSpinAction.Skip -> skip()
            SavingSpinAction.ResetGame -> resetGame()
            SavingSpinAction.DismissError -> mutableUiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun resetGame() = viewModelScope.launch {
        val session = uiState.value.session ?: return@launch
        mutableUiState.update { it.copy(isLoading = true, errorMessage = null, isWheelAnimating = false) }
        when (val result = repository.resetSession(session.scheduleKey)) {
            is AppResult.Success -> {
                mutableUiState.update { it.copy(
                    isLoading = false,
                    isWheelAnimating = false,
                    session = session.copy(
                        selectedIndex = null,
                        selectedAmount = null,
                        status = SavingSpinStatus.READY,
                        destinationId = null,
                        method = null,
                        spunAt = null,
                        completedAt = null,
                        skippedAt = null,
                        snoozedUntil = null,
                    ),
                    isGameOpen = true,
                ) }
            }
            is AppResult.Error -> mutableUiState.update { it.copy(isLoading = false, errorMessage = result.message) }
        }
    }

    private fun spin() = viewModelScope.launch {
        val session = uiState.value.session ?: return@launch
        mutableUiState.update { it.copy(isSpinning = true, isWheelAnimating = true, errorMessage = null) }
        when (val result = spinWheel(session)) {
            is AppResult.Success -> mutableUiState.update { it.copy(isSpinning = false, session = result.value) }
            is AppResult.Error -> mutableUiState.update { it.copy(isSpinning = false, isWheelAnimating = false, errorMessage = result.message) }
        }
    }

    private fun confirmDeposit() = viewModelScope.launch {
        val state = uiState.value
        val session = state.session ?: return@launch
        val destWallet = state.wallets.firstOrNull { it.id == state.selectedWalletId }
            ?: state.wallets.firstOrNull()

        if (destWallet == null) {
            mutableUiState.update { it.copy(errorMessage = "Bạn chưa chọn ví tiết kiệm") }
            return@launch
        }

        val sourceWalletId = state.sourceWalletId

        when (val result = completeSavingSpin(
            session = session,
            walletId = destWallet.id,
            walletName = destWallet.name,
            sourceWalletId = sourceWalletId,
        )) {
            is AppResult.Success -> {
                scheduler.cancel()
                mutableUiState.update { it.copy(
                    session = session.copy(
                        status = SavingSpinStatus.COMPLETED,
                        destinationId = destWallet.id,
                        completedAt = clock.now(),
                    ),
                    isGameOpen = false,
                ) }
            }
            is AppResult.Error -> mutableUiState.update { it.copy(errorMessage = result.message) }
        }
    }

    private fun snooze(until: java.time.Instant) = viewModelScope.launch {
        val session = uiState.value.session ?: return@launch
        when (val result = repository.snoozeSession(session.scheduleKey, until)) {
            is AppResult.Success -> {
                scheduler.snooze(until)
                mutableUiState.update { it.copy(
                    session = session.copy(status = SavingSpinStatus.SNOOZED, snoozedUntil = until),
                    isGameOpen = false,
                ) }
            }
            is AppResult.Error -> mutableUiState.update { it.copy(errorMessage = result.message) }
        }
    }

    private fun skip() = viewModelScope.launch {
        val session = uiState.value.session ?: return@launch
        when (val result = repository.skipSession(session.scheduleKey)) {
            is AppResult.Success -> {
                scheduler.cancel()
                mutableUiState.update { it.copy(
                    session = session.copy(status = SavingSpinStatus.SKIPPED, skippedAt = clock.now()),
                    isGameOpen = false,
                ) }
            }
            is AppResult.Error -> mutableUiState.update { it.copy(errorMessage = result.message) }
        }
    }
}
