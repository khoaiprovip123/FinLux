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
import com.finlux.app.domain.usecase.EditTransactionUseCase
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
    val editingTransaction: FinanceTransaction? = null,
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
    private val editTransaction: EditTransactionUseCase,
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
                        val defaultCatId = if (current.editingTransaction?.dealId != null) current.categoryId else (current.categoryId ?: categories.firstOrNull { it.type == categoryType }?.id)
                        current.copy(
                            wallets = wallets,
                            categories = categories,
                            walletId = current.walletId ?: wallets.firstOrNull()?.id,
                            categoryId = defaultCatId,
                        )
                    }
                }
        }
    }

    fun resetForNewTransaction(type: TransactionType? = null) {
        mutableState.update { current ->
            val targetType = type ?: current.type
            val categoryType = targetType.toCategoryType()
            current.copy(
                editingTransaction = null,
                type = targetType,
                amountInput = "",
                note = "",
                receiptUri = null,
                date = Instant.now(),
                walletId = current.wallets.firstOrNull { it.isDefault }?.id ?: current.wallets.firstOrNull()?.id,
                categoryId = current.categories.firstOrNull { it.type == categoryType }?.id,
                error = null,
                isSaving = false,
                saved = false,
            )
        }
    }

    fun setEditingTransaction(tx: FinanceTransaction?) {
        if (tx == null) {
            resetForNewTransaction()
            return
        }
        mutableState.update { current ->
            val isDeal = !tx.dealId.isNullOrBlank()
            val effectiveCategoryId = if (isDeal) tx.categoryId else (tx.categoryId ?: current.categories.firstOrNull { it.type == tx.type.toCategoryType() }?.id)
            current.copy(
                editingTransaction = tx,
                type = tx.type,
                amountInput = if (tx.amount.value > 0) tx.amount.value.toString() else "",
                categoryId = effectiveCategoryId,
                walletId = tx.walletId.ifBlank { current.wallets.firstOrNull()?.id },
                note = tx.note,
                date = tx.date,
                receiptUri = tx.receiptImageUrl,
                error = null,
                isSaving = false,
                saved = false,
            )
        }
    }

    fun setType(type: TransactionType) = mutableState.update { current ->
        val isDeal = !current.editingTransaction?.dealId.isNullOrBlank()
        val nextCatId = if (isDeal) current.categoryId else current.categories.firstOrNull { it.type == type.toCategoryType() }?.id
        current.copy(
            type = type,
            categoryId = nextCatId,
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
        val original = snapshot.editingTransaction
        val baseTransaction = FinanceTransaction(
            id = original?.id.orEmpty(),
            type = snapshot.type,
            amount = Money(snapshot.amountInput.toLongOrNull() ?: 0L),
            categoryId = snapshot.categoryId,
            walletId = snapshot.walletId.orEmpty(),
            dealId = original?.dealId,
            dealFlowType = original?.dealFlowType,
            relatedWalletId = original?.relatedWalletId,
            note = snapshot.note.trim(),
            date = snapshot.date,
            receiptImageUrl = original?.receiptImageUrl,
            createdAt = original?.createdAt ?: Instant.now(),
        )
        viewModelScope.launch {
            mutableState.update { it.copy(isSaving = true, error = null) }
            val receiptUrl = if (snapshot.receiptUri != null && snapshot.receiptUri != original?.receiptImageUrl) {
                when (val upload = receiptStorageRepository.uploadReceipt(snapshot.receiptUri)) {
                    is AppResult.Success -> upload.value
                    is AppResult.Error -> {
                        mutableState.update { it.copy(isSaving = false, error = upload.message) }
                        return@launch
                    }
                }
            } else {
                snapshot.receiptUri ?: original?.receiptImageUrl
            }
            val transactionToSave = baseTransaction.copy(receiptImageUrl = receiptUrl)
            val result = if (original != null) {
                editTransaction(original, transactionToSave)
            } else {
                addTransaction(transactionToSave)
            }
            when (result) {
                is AppResult.Success -> mutableState.update { it.copy(isSaving = false, saved = true) }
                is AppResult.Error -> mutableState.update { it.copy(isSaving = false, error = result.message) }
            }
        }
    }

    fun consumeSaved() {
        resetForNewTransaction()
    }

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
