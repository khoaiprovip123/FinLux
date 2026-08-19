package com.finlux.app.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.DeleteTransactionUseCase
import com.finlux.app.core.common.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TransactionFilter { ALL, INCOME, EXPENSE }

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    repository: TransactionRepository,
    categoryRepository: CategoryRepository,
    walletRepository: WalletRepository,
    private val deleteTransaction: DeleteTransactionUseCase,
) : ViewModel() {
    val filter = MutableStateFlow(TransactionFilter.ALL)
    val transactions = combine(repository.observeRecent(100), filter) { items, selected ->
        items.filter { item ->
            when (selected) {
                TransactionFilter.ALL -> true
                TransactionFilter.INCOME -> item.type == TransactionType.INCOME
                TransactionFilter.EXPENSE -> item.type == TransactionType.EXPENSE
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories = categoryRepository.observeCategories()
        .map { list -> list.associateBy { it.id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val wallets = walletRepository.observeWallets()
        .map { list -> list.associateBy { it.id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val mutableMessages = MutableSharedFlow<String>()
    val messages = mutableMessages.asSharedFlow()

    fun delete(transaction: FinanceTransaction) = viewModelScope.launch {
        when (val result = deleteTransaction(transaction)) {
            is AppResult.Success -> mutableMessages.emit("Đã xóa giao dịch và hoàn lại số dư ví")
            is AppResult.Error -> mutableMessages.emit(result.message)
        }
    }
}
