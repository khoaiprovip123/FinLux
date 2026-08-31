package com.finlux.app.presentation.savingspin

import com.finlux.app.domain.model.SavingDestination
import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.Wallet
import java.time.Instant

data class SavingSpinUiState(
    val isLoading: Boolean = true,
    val config: SavingSpinConfig = SavingSpinConfig(),
    val scheduleKey: String? = null,
    val session: SavingSpinSession? = null,
    val destinations: List<SavingDestination> = emptyList(),
    val selectedDestinationId: String? = null,
    val wallets: List<Wallet> = emptyList(),
    val sourceWalletId: String? = null,
    val selectedWalletId: String? = null,
    val streakCount: Int = 1,
    val isSpinning: Boolean = false,
    val isWheelAnimating: Boolean = false,
    val isGameOpen: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface SavingSpinAction {
    data object OpenGame : SavingSpinAction
    data object CloseGame : SavingSpinAction
    data object Spin : SavingSpinAction
    data object WheelAnimationFinished : SavingSpinAction
    data class SelectDestination(val id: String) : SavingSpinAction
    data class SelectSourceWallet(val id: String) : SavingSpinAction
    data class SelectWallet(val id: String) : SavingSpinAction
    data object ConfirmDeposit : SavingSpinAction
    data class Snooze(val until: Instant) : SavingSpinAction
    data object Skip : SavingSpinAction
    data object ResetGame : SavingSpinAction
    data object DismissError : SavingSpinAction
}
