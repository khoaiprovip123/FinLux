package com.finlux.app.presentation.deal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinancialDeal
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.CloseDealUseCase
import com.finlux.app.domain.usecase.CloseDealWithLossUseCase
import com.finlux.app.domain.usecase.DeleteDealUseCase
import com.finlux.app.domain.usecase.GetDealsUseCase
import com.finlux.app.domain.usecase.RecordDealInflowUseCase
import com.finlux.app.domain.usecase.RecordDealOutlayUseCase
import com.finlux.app.domain.usecase.ReopenDealUseCase
import com.finlux.app.domain.usecase.RevertDealLossUseCase
import com.finlux.app.domain.usecase.SaveDealUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class DealsViewModel @Inject constructor(
    getDealsUseCase: GetDealsUseCase,
    walletRepository: WalletRepository,
    transactionRepository: TransactionRepository,
    private val saveDealUseCase: SaveDealUseCase,
    private val deleteDealUseCase: DeleteDealUseCase,
    private val recordDealOutlayUseCase: RecordDealOutlayUseCase,
    private val recordDealInflowUseCase: RecordDealInflowUseCase,
    private val closeDealWithLossUseCase: CloseDealWithLossUseCase,
    private val closeDealUseCase: CloseDealUseCase,
    private val revertDealLossUseCase: RevertDealLossUseCase,
    private val reopenDealUseCase: ReopenDealUseCase,
) : ViewModel() {

    private val selectedTab = MutableStateFlow(DealTab.ACTIVE)
    private val selectedDeal = MutableStateFlow<FinancialDeal?>(null)
    private val isSubmitting = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)
    private val successMessage = MutableStateFlow<String?>(null)

    private val dataFlow = combine(
        getDealsUseCase(),
        walletRepository.observeWallets(),
        transactionRepository.observeRecent(limit = 500),
    ) { deals, wallets, transactions ->
        Triple(deals, wallets, transactions)
    }

    private val controlFlow = combine(
        selectedTab,
        selectedDeal,
        isSubmitting,
        errorMessage,
        successMessage,
    ) { tab, currentDeal, submitting, error, success ->
        DealUiControls(tab, currentDeal, submitting, error, success)
    }

    val state: StateFlow<DealsUiState> = combine(
        dataFlow,
        controlFlow,
    ) { (deals, wallets, transactions), controls ->
        val updatedCurrentDeal = controls.currentDeal?.let { selected ->
            deals.find { it.id == selected.id } ?: selected
        }
        val dealTransactions = transactions.filter { tx ->
            updatedCurrentDeal != null && tx.dealId == updatedCurrentDeal.id
        }
        val allDealTransactions = transactions.filter { it.dealId != null }

        DealsUiState(
            deals = deals,
            wallets = wallets,
            transactions = dealTransactions,
            allDealTransactions = allDealTransactions,
            selectedTab = controls.tab,
            selectedDeal = updatedCurrentDeal,
            isSubmitting = controls.submitting,
            errorMessage = controls.error,
            successMessage = controls.success,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DealsUiState(isLoading = true),
    )

    fun selectTab(tab: DealTab) {
        selectedTab.value = tab
    }

    fun selectDeal(deal: FinancialDeal?) {
        selectedDeal.value = deal
    }

    fun createOrUpdateDeal(deal: FinancialDeal, onSuccess: () -> Unit = {}) = viewModelScope.launch {
        isSubmitting.value = true
        errorMessage.value = null
        when (val result = saveDealUseCase(deal)) {
            is AppResult.Success -> {
                isSubmitting.value = false
                successMessage.value = if (deal.id.isBlank()) "Đã tạo thương vụ thành công" else "Đã cập nhật thương vụ"
                onSuccess()
            }
            is AppResult.Error -> {
                isSubmitting.value = false
                errorMessage.value = result.message
            }
        }
    }

    fun recordOutlay(
        deal: FinancialDeal,
        walletId: String,
        amount: Long,
        date: Instant = Instant.now(),
        note: String = "",
        onSuccess: () -> Unit = {},
    ) = viewModelScope.launch {
        isSubmitting.value = true
        errorMessage.value = null
        when (val result = recordDealOutlayUseCase(deal, walletId, amount, date, note)) {
            is AppResult.Success -> {
                isSubmitting.value = false
                successMessage.value = "Đã ghi nhận xuất vốn thành công"
                onSuccess()
            }
            is AppResult.Error -> {
                isSubmitting.value = false
                errorMessage.value = result.message
            }
        }
    }

    fun recordInflow(
        deal: FinancialDeal,
        walletId: String,
        amount: Long,
        date: Instant = Instant.now(),
        note: String = "",
        onSuccess: () -> Unit = {},
    ) = viewModelScope.launch {
        isSubmitting.value = true
        errorMessage.value = null
        when (val result = recordDealInflowUseCase(deal, walletId, amount, date, note)) {
            is AppResult.Success -> {
                isSubmitting.value = false
                successMessage.value = "Đã thu hồi vốn và phân tách dòng tiền thành công"
                onSuccess()
            }
            is AppResult.Error -> {
                isSubmitting.value = false
                errorMessage.value = result.message
            }
        }
    }

    fun closeDealWithLoss(
        deal: FinancialDeal,
        date: Instant = Instant.now(),
        note: String = "",
        onSuccess: () -> Unit = {},
    ) = viewModelScope.launch {
        isSubmitting.value = true
        errorMessage.value = null
        when (val result = closeDealWithLossUseCase(deal, date, note)) {
            is AppResult.Success -> {
                isSubmitting.value = false
                successMessage.value = "Đã chốt lỗ và đóng thương vụ"
                onSuccess()
            }
            is AppResult.Error -> {
                isSubmitting.value = false
                errorMessage.value = result.message
            }
        }
    }

    fun revertDealLoss(dealId: String, onSuccess: () -> Unit = {}) = viewModelScope.launch {
        isSubmitting.value = true
        errorMessage.value = null
        when (val result = revertDealLossUseCase(dealId)) {
            is AppResult.Success -> {
                isSubmitting.value = false
                successMessage.value = "Đã thu hồi chốt lỗ và mở lại thương vụ"
                onSuccess()
            }
            is AppResult.Error -> {
                isSubmitting.value = false
                errorMessage.value = result.message
            }
        }
    }

    fun reopenDeal(dealId: String, onSuccess: () -> Unit = {}) = viewModelScope.launch {
        isSubmitting.value = true
        errorMessage.value = null
        when (val result = reopenDealUseCase(dealId)) {
            is AppResult.Success -> {
                isSubmitting.value = false
                successMessage.value = "Đã mở lại thương vụ / khoản vay"
                onSuccess()
            }
            is AppResult.Error -> {
                isSubmitting.value = false
                errorMessage.value = result.message
            }
        }
    }

    fun closeDeal(dealId: String, onSuccess: () -> Unit = {}) = viewModelScope.launch {
        isSubmitting.value = true
        errorMessage.value = null
        when (val result = closeDealUseCase(dealId)) {
            is AppResult.Success -> {
                isSubmitting.value = false
                successMessage.value = "Đã tất toán và đóng thương vụ thành công"
                onSuccess()
            }
            is AppResult.Error -> {
                isSubmitting.value = false
                errorMessage.value = result.message
            }
        }
    }

    fun deleteDeal(dealId: String, onSuccess: () -> Unit = {}) = viewModelScope.launch {
        isSubmitting.value = true
        errorMessage.value = null
        when (val result = deleteDealUseCase(dealId)) {
            is AppResult.Success -> {
                isSubmitting.value = false
                if (selectedDeal.value?.id == dealId) {
                    selectedDeal.value = null
                }
                successMessage.value = "Đã xóa thương vụ"
                onSuccess()
            }
            is AppResult.Error -> {
                isSubmitting.value = false
                errorMessage.value = result.message
            }
        }
    }

    fun clearMessages() {
        errorMessage.value = null
        successMessage.value = null
    }
}

private data class DealUiControls(
    val tab: DealTab,
    val currentDeal: FinancialDeal?,
    val submitting: Boolean,
    val error: String?,
    val success: String?,
)
