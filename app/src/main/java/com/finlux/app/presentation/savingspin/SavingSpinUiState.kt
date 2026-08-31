package com.finlux.app.presentation.savingspin

import com.finlux.app.domain.model.SavingDestination
import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.model.SavingSpinSession
import java.time.Instant

data class SavingSpinUiState(
    val isLoading: Boolean = true,
    val config: SavingSpinConfig = SavingSpinConfig(),
    val scheduleKey: String? = null,
    val session: SavingSpinSession? = null,
    val destinations: List<SavingDestination> = emptyList(),
    val selectedDestinationId: String? = null,
    val isSpinning: Boolean = false,
    val isGameOpen: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface SavingSpinAction {
    data object OpenGame : SavingSpinAction
    data object CloseGame : SavingSpinAction
    data object Spin : SavingSpinAction
    data class SelectDestination(val id: String) : SavingSpinAction
    data object ConfirmDeposit : SavingSpinAction
    data class Snooze(val until: Instant) : SavingSpinAction
    data object Skip : SavingSpinAction
    data object ResetGame : SavingSpinAction
    data object DismissError : SavingSpinAction
}
