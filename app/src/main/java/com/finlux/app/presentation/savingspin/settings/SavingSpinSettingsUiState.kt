package com.finlux.app.presentation.savingspin.settings

import com.finlux.app.domain.model.SavingDestination
import com.finlux.app.domain.model.SavingSpinConfig

data class SavingSpinSettingsUiState(
    val isLoading: Boolean = true,
    val config: SavingSpinConfig = SavingSpinConfig(),
    val minAmountInput: String = "10000",
    val maxAmountInput: String = "100000",
    val destinations: List<SavingDestination> = emptyList(),
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val validationMessage: String? = null,
)
