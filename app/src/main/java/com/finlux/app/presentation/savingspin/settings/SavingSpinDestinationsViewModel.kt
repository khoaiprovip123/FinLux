package com.finlux.app.presentation.savingspin.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.common.AppResult
import com.finlux.app.core.time.FinanceClock
import com.finlux.app.domain.model.SavingDestination
import com.finlux.app.domain.model.SavingMethod
import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.repository.SavingSpinRepository
import com.finlux.app.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SavingSpinDestinationsUiState(
    val isLoading: Boolean = true,
    val destinations: List<SavingDestination> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val config: SavingSpinConfig = SavingSpinConfig(),
    val isBusy: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class SavingSpinDestinationsViewModel @Inject constructor(
    private val savingSpinRepository: SavingSpinRepository,
    walletRepository: WalletRepository,
    private val clock: FinanceClock,
) : ViewModel() {
    private val mutableState = MutableStateFlow(SavingSpinDestinationsUiState())
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                savingSpinRepository.observeDestinations(),
                savingSpinRepository.observeConfig(),
                walletRepository.observeWallets(),
            ) { destinations, config, wallets ->
                Triple(destinations, config, wallets)
            }.collect { (destinations, config, wallets) ->
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        destinations = destinations,
                        config = config,
                        wallets = wallets,
                    )
                }
            }
        }
    }

    fun saveDestination(
        existing: SavingDestination?,
        name: String,
        method: SavingMethod,
        linkedWalletId: String?,
    ) = viewModelScope.launch {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) {
            mutableState.update { it.copy(message = "Tên nơi tiết kiệm không được để trống") }
            return@launch
        }
        if (method == SavingMethod.BANK_TRANSFER && linkedWalletId.isNullOrBlank()) {
            mutableState.update { it.copy(message = "Chuyển khoản cần liên kết một ví nhận") }
            return@launch
        }

        val now = clock.now()
        val destination = SavingDestination(
            id = existing?.id ?: UUID.randomUUID().toString(),
            name = normalizedName,
            method = method,
            linkedWalletId = linkedWalletId,
            institutionId = existing?.institutionId,
            accountHint = existing?.accountHint,
            enabled = existing?.enabled ?: true,
            icon = existing?.icon ?: if (method == SavingMethod.CASH) "savings" else "account_balance",
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
        )

        mutableState.update { it.copy(isBusy = true, message = null) }
        when (val result = savingSpinRepository.upsertDestination(destination)) {
            is AppResult.Success -> mutableState.update {
                it.copy(isBusy = false, message = "Đã lưu nơi tiết kiệm")
            }
            is AppResult.Error -> mutableState.update {
                it.copy(isBusy = false, message = result.message)
            }
        }
    }

    fun deleteDestination(destination: SavingDestination) = viewModelScope.launch {
        mutableState.update { it.copy(isBusy = true, message = null) }
        when (val result = savingSpinRepository.deleteDestination(destination.id)) {
            is AppResult.Success -> mutableState.update {
                it.copy(isBusy = false, message = "Đã xóa nơi tiết kiệm")
            }
            is AppResult.Error -> mutableState.update {
                it.copy(isBusy = false, message = result.message)
            }
        }
    }

    fun setDefaultDestination(destination: SavingDestination?) = viewModelScope.launch {
        val config = state.value.config.copy(
            defaultDestinationId = destination?.id,
            updatedAt = clock.now(),
        )
        mutableState.update { it.copy(isBusy = true, message = null) }
        when (val result = savingSpinRepository.saveConfig(config)) {
            is AppResult.Success -> mutableState.update {
                it.copy(
                    isBusy = false,
                    config = config,
                    message = if (destination == null) {
                        "Đã bỏ nơi tiết kiệm mặc định"
                    } else {
                        "Đã đặt ${destination.name} làm mặc định"
                    },
                )
            }
            is AppResult.Error -> mutableState.update {
                it.copy(isBusy = false, message = result.message)
            }
        }
    }

    fun consumeMessage() = mutableState.update { it.copy(message = null) }
}
