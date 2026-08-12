package com.finlux.app.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.AddTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class AddTransactionUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val amountInput: String = "",
    val categoryId: String? = null,
    val walletId: String? = null,
    val note: String = "",
    val date: Instant = Instant.now(),
    val wallets: List<Wallet> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    walletRepository: WalletRepository,
    categoryRepository: CategoryRepository,
    private val addTransaction: AddTransactionUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(AddTransactionUiState())
    val state: StateFlow<AddTransactionUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(walletRepository.observeWallets(), categoryRepository.observeCategories(), ::Pair)
                .collect { (wallets, categories) ->
                    mutableState.update { current ->
                        val categoryType = current.type.toCategoryType()
                        current.copy(
                            wallets = wallets,
                            categories = categories,
                            walletId = current.walletId ?: wallets.firstOrNull()?.id,
                            categoryId = current.categoryId ?: categories.firstOrNull { it.type == categoryType }?.id,
                        )
                    }
                }
        }
    }

    fun setType(type: TransactionType) = mutableState.update { current ->
        current.copy(
            type = type,
            categoryId = current.categories.firstOrNull { it.type == type.toCategoryType() }?.id,
            error = null,
        )
    }
    fun setAmount(value: String) = mutableState.update { it.copy(amountInput = value.filter(Char::isDigit).take(15), error = null) }
    fun setCategory(id: String) = mutableState.update { it.copy(categoryId = id, error = null) }
    fun setWallet(id: String) = mutableState.update { it.copy(walletId = id, error = null) }
    fun setNote(value: String) = mutableState.update { it.copy(note = value, error = null) }
    fun setDate(value: Instant) = mutableState.update { it.copy(date = value, error = null) }

    fun save() {
        val snapshot = state.value
        val transaction = FinanceTransaction(
            type = snapshot.type,
            amount = Money(snapshot.amountInput.toLongOrNull() ?: 0L),
            categoryId = snapshot.categoryId,
            walletId = snapshot.walletId.orEmpty(),
            note = snapshot.note.trim(),
            date = snapshot.date,
        )
        viewModelScope.launch {
            mutableState.update { it.copy(isSaving = true, error = null) }
            when (val result = addTransaction(transaction)) {
                is AppResult.Success -> mutableState.update { it.copy(isSaving = false, saved = true) }
                is AppResult.Error -> mutableState.update { it.copy(isSaving = false, error = result.message) }
            }
        }
    }

    fun consumeSaved() = mutableState.update { it.copy(saved = false, amountInput = "", note = "", date = Instant.now()) }

    private fun TransactionType.toCategoryType() =
        if (this == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
}
