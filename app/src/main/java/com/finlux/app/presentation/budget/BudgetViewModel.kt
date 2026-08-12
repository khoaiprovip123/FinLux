package com.finlux.app.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.usecase.BudgetStatus
import com.finlux.app.domain.usecase.GetBudgetStatusUseCase
import com.finlux.app.domain.usecase.SaveBudgetUseCase
import com.finlux.app.domain.usecase.DeleteBudgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class BudgetItemUi(val budget: Budget, val category: Category?, val status: BudgetStatus)
data class BudgetUiState(
    val month: YearMonth = YearMonth.now(),
    val items: List<BudgetItemUi> = emptyList(),
    val categories: List<Category> = emptyList(),
    val busy: Boolean = false,
    val message: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetViewModel @Inject constructor(
    budgetRepository: BudgetRepository,
    categoryRepository: CategoryRepository,
    private val getBudgetStatus: GetBudgetStatusUseCase,
    private val saveBudget: SaveBudgetUseCase,
    private val deleteBudget: DeleteBudgetUseCase,
) : ViewModel() {
    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val action = MutableStateFlow(false to null as String?)

    val state = combine(
        selectedMonth.flatMapLatest { month -> budgetRepository.observeBudgets(month) },
        categoryRepository.observeCategories(),
        selectedMonth,
        action,
    ) { budgets, categories, month, actionState ->
        val byId = categories.associateBy(Category::id)
        BudgetUiState(
            month = month,
            items = budgets.map { BudgetItemUi(it, byId[it.categoryId], getBudgetStatus(it)) }.sortedByDescending { it.status.progress },
            categories = categories.filter { it.type == CategoryType.EXPENSE },
            busy = actionState.first,
            message = actionState.second,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BudgetUiState())

    fun previousMonth() { selectedMonth.value = selectedMonth.value.minusMonths(1) }
    fun nextMonth() { if (selectedMonth.value < YearMonth.now()) selectedMonth.value = selectedMonth.value.plusMonths(1) }
    fun currentMonth() { selectedMonth.value = YearMonth.now() }

    fun save(categoryId: String, limit: Long, existing: Budget?, onSaved: () -> Unit) = viewModelScope.launch {
        action.value = true to null
        val month = selectedMonth.value
        val budget = existing?.copy(limitAmount = com.finlux.app.domain.model.Money(limit)) ?: Budget(
            id = "${categoryId}_${month}", categoryId = categoryId, month = month,
            limitAmount = com.finlux.app.domain.model.Money(limit), spentAmount = com.finlux.app.domain.model.Money(0),
            notified80 = false, notified100 = false,
        )
        when (val result = saveBudget(budget)) {
            is AppResult.Success -> { action.value = false to "Đã lưu ngân sách"; onSaved() }
            is AppResult.Error -> action.value = false to result.message
        }
    }

    fun delete(budget: Budget) = viewModelScope.launch {
        when (val result = deleteBudget(budget)) {
            is AppResult.Success -> action.value = false to "Đã xóa ngân sách"
            is AppResult.Error -> action.value = false to result.message
        }
    }

    fun consumeMessage() { action.value = action.value.first to null }
}
