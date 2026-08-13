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
import com.finlux.app.domain.repository.ReceiptStorageRepository
import com.finlux.app.domain.usecase.AddTransactionUseCase
import com.finlux.app.domain.usecase.DeleteCategoryUseCase
import com.finlux.app.domain.usecase.SaveCategoryUseCase
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
    val receiptUri: String? = null,
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
    private val receiptStorageRepository: ReceiptStorageRepository,
    private val addTransaction: AddTransactionUseCase,
    private val saveCategory: SaveCategoryUseCase,
    private val deleteCategory: DeleteCategoryUseCase,
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
    fun setReceipt(uri: String?) = mutableState.update { it.copy(receiptUri = uri, error = null) }

    fun save() {
        val snapshot = state.value
        val baseTransaction = FinanceTransaction(
            type = snapshot.type,
            amount = Money(snapshot.amountInput.toLongOrNull() ?: 0L),
            categoryId = snapshot.categoryId,
            walletId = snapshot.walletId.orEmpty(),
            note = snapshot.note.trim(),
            date = snapshot.date,
        )
        viewModelScope.launch {
            mutableState.update { it.copy(isSaving = true, error = null) }
            val receiptUrl = snapshot.receiptUri?.let { uri ->
                when (val upload = receiptStorageRepository.uploadReceipt(uri)) {
                    is AppResult.Success -> upload.value
                    is AppResult.Error -> {
                        mutableState.update { it.copy(isSaving = false, error = upload.message) }
                        return@launch
                    }
                }
            }
            when (val result = addTransaction(baseTransaction.copy(receiptImageUrl = receiptUrl))) {
                is AppResult.Success -> mutableState.update { it.copy(isSaving = false, saved = true) }
                is AppResult.Error -> mutableState.update { it.copy(isSaving = false, error = result.message) }
            }
        }
    }

    fun consumeSaved() = mutableState.update { it.copy(saved = false, amountInput = "", note = "", receiptUri = null, date = Instant.now()) }

    fun createCategory(name: String, iconKey: String, colorHex: String, onCreated: (String) -> Unit) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val desiredType = state.value.type.toCategoryType()
            val newCategory = Category(
                id = java.util.UUID.randomUUID().toString(),
                name = name.trim(),
                type = desiredType,
                icon = iconKey,
                colorHex = colorHex,
                isDefault = false,
                createdAt = Instant.now(),
            )
            when (val result = saveCategory(newCategory)) {
                is AppResult.Success -> {
                    mutableState.update { it.copy(categoryId = result.value, error = null) }
                    onCreated(result.value)
                }
                is AppResult.Error -> {
                    mutableState.update { it.copy(error = result.message) }
                }
            }
        }
    }

    fun updateCategory(category: Category, onUpdated: () -> Unit) {
        if (category.name.isBlank()) return
        viewModelScope.launch {
            when (val result = saveCategory(category)) {
                is AppResult.Success -> {
                    mutableState.update { it.copy(error = null) }
                    onUpdated()
                }
                is AppResult.Error -> {
                    mutableState.update { it.copy(error = result.message) }
                }
            }
        }
    }

    fun deleteCategory(category: Category, onDeleted: () -> Unit) {
        viewModelScope.launch {
            when (val result = deleteCategory.invoke(category)) {
                is AppResult.Success -> {
                    mutableState.update { current ->
                        val nextCategoryId = if (current.categoryId == category.id) {
                            current.categories.firstOrNull { it.id != category.id && it.type == category.type }?.id
                        } else current.categoryId
                        current.copy(categoryId = nextCategoryId, error = null)
                    }
                    onDeleted()
                }
                is AppResult.Error -> {
                    mutableState.update { it.copy(error = result.message) }
                }
            }
        }
    }

    private fun TransactionType.toCategoryType() =
        if (this == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
}
