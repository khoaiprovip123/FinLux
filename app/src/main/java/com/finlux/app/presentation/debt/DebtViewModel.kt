package com.finlux.app.presentation.debt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.PayoffStrategy
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.DebtPreferenceRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.AnalyzeDebtCashflowUseCase
import com.finlux.app.domain.usecase.CalculatePayoffStrategyUseCase
import com.finlux.app.domain.usecase.DeleteDebtAccountUseCase
import com.finlux.app.domain.usecase.GetDebtPaymentHistoryUseCase
import com.finlux.app.domain.usecase.GetDebtsUseCase
import com.finlux.app.domain.usecase.ProcessDebtPaymentUseCase
import com.finlux.app.domain.usecase.SaveDebtAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class DebtViewModel @Inject constructor(
    getDebtsUseCase: GetDebtsUseCase,
    getDebtPaymentHistoryUseCase: GetDebtPaymentHistoryUseCase,
    walletRepository: WalletRepository,
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    private val analyzeDebtCashflowUseCase: AnalyzeDebtCashflowUseCase,
    private val calculatePayoffStrategyUseCase: CalculatePayoffStrategyUseCase,
    private val saveDebtAccountUseCase: SaveDebtAccountUseCase,
    private val deleteDebtAccountUseCase: DeleteDebtAccountUseCase,
    private val processDebtPaymentUseCase: ProcessDebtPaymentUseCase,
    private val debtPreferenceRepository: DebtPreferenceRepository,
) : ViewModel() {

    private data class FormState(
        val isSubmitting: Boolean = false,
        val errorMessage: String? = null,
        val successMessage: String? = null,
    )

    private val _formState = MutableStateFlow(FormState())

    private val cashflowFlow = combine(
        transactionRepository.observeRecent(5_000),
        categoryRepository.observeCategories(),
        getDebtsUseCase(),
    ) { transactions, categories, debts ->
        analyzeDebtCashflowUseCase(
            transactions = transactions,
            categories = categories,
            debts = debts,
        )
    }

    private val preferencesFlow = combine(
        debtPreferenceRepository.observePayoffStrategy(),
        debtPreferenceRepository.observeExtraMonthlyPayment(),
        _formState,
    ) { strategy, extraPayment, formState ->
        Triple(strategy, extraPayment, formState)
    }

    private val coreDataFlow = combine(
        getDebtsUseCase(),
        walletRepository.observeWallets(),
        getDebtPaymentHistoryUseCase(),
    ) { debts, wallets, history ->
        Triple(debts, wallets, history)
    }

    val uiState: StateFlow<DebtUiState> = combine(
        coreDataFlow,
        cashflowFlow,
        preferencesFlow,
    ) { (debts, wallets, history), cashflow, (strategy, extraPayment, formState) ->
        val plan = calculatePayoffStrategyUseCase(
            debts = debts,
            strategy = strategy,
            extraMonthlyPayment = extraPayment,
        )
        DebtUiState(
            debts = debts,
            wallets = wallets,
            paymentHistory = history,
            strategy = strategy,
            extraMonthlyPayment = extraPayment,
            payoffPlan = plan,
            cashflowAnalysis = cashflow,
            isLoading = false,
            isSubmitting = formState.isSubmitting,
            errorMessage = formState.errorMessage,
            successMessage = formState.successMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DebtUiState(isLoading = true),
    )

    fun setStrategy(strategy: PayoffStrategy) {
        viewModelScope.launch {
            debtPreferenceRepository.savePayoffStrategy(strategy)
        }
    }

    fun setExtraMonthlyPayment(amount: Long) {
        viewModelScope.launch {
            debtPreferenceRepository.saveExtraMonthlyPayment(amount.coerceAtLeast(0L))
        }
    }

    fun saveDebt(debt: DebtAccount, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _formState.update { it.copy(isSubmitting = true, errorMessage = null) }
            when (val result = saveDebtAccountUseCase(debt)) {
                is AppResult.Success -> {
                    _formState.update { it.copy(isSubmitting = false, successMessage = "Lưu khoản nợ thành công") }
                    onSuccess()
                }
                is AppResult.Error -> {
                    _formState.update { it.copy(isSubmitting = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun deleteDebt(debt: DebtAccount, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _formState.update { it.copy(isSubmitting = true, errorMessage = null) }
            when (val result = deleteDebtAccountUseCase(debt)) {
                is AppResult.Success -> {
                    _formState.update { it.copy(isSubmitting = false, successMessage = "Đã xóa khoản nợ") }
                    onSuccess()
                }
                is AppResult.Error -> {
                    _formState.update { it.copy(isSubmitting = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun payDebt(
        debtId: String,
        walletId: String,
        amount: Long,
        principalPaid: Long,
        interestPaid: Long = 0L,
        note: String = "",
        paymentDate: Instant = Instant.now(),
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            _formState.update { it.copy(isSubmitting = true, errorMessage = null) }
            when (val result = processDebtPaymentUseCase(
                debtId = debtId,
                walletId = walletId,
                amount = amount,
                principalPaid = principalPaid,
                interestPaid = interestPaid,
                note = note,
                paymentDate = paymentDate,
            )) {
                is AppResult.Success -> {
                    _formState.update { it.copy(isSubmitting = false, successMessage = "Thanh toán nợ thành công!") }
                    onSuccess()
                }
                is AppResult.Error -> {
                    _formState.update { it.copy(isSubmitting = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun clearMessages() {
        _formState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
