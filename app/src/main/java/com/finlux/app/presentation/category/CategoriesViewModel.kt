package com.finlux.app.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.usecase.DeleteCategoryUseCase
import com.finlux.app.domain.usecase.SaveCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryActionState(
    val isSaving: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    repository: CategoryRepository,
    private val saveCategory: SaveCategoryUseCase,
    private val deleteCategory: DeleteCategoryUseCase,
) : ViewModel() {
    val categories = repository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val mutableActionState = MutableStateFlow(CategoryActionState())
    val actionState = mutableActionState.asStateFlow()

    fun save(category: Category, onSaved: () -> Unit) = viewModelScope.launch {
        mutableActionState.value = CategoryActionState(isSaving = true)
        when (val result = saveCategory(category)) {
            is AppResult.Success -> {
                mutableActionState.value = CategoryActionState(message = "Đã lưu danh mục")
                onSaved()
            }
            is AppResult.Error -> mutableActionState.value = CategoryActionState(message = result.message)
        }
    }

    fun delete(category: Category) = viewModelScope.launch {
        when (val result = deleteCategory(category)) {
            is AppResult.Success -> mutableActionState.value = CategoryActionState(message = "Đã xóa danh mục")
            is AppResult.Error -> mutableActionState.value = CategoryActionState(message = result.message)
        }
    }

    fun consumeMessage() = mutableActionState.update { it.copy(message = null) }
}
