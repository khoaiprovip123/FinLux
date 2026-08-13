package com.finlux.app.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.usecase.BudgetStatus
import com.finlux.app.domain.usecase.GetBudgetStatusUseCase
import com.finlux.app.domain.usecase.SaveBudgetUseCase
import com.finlux.app.domain.usecase.DeleteBudgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
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
    private val transactionRepository: TransactionRepository,
    private val getBudgetStatus: GetBudgetStatusUseCase,
    private val saveBudget: SaveBudgetUseCase,
    private val deleteBudget: DeleteBudgetUseCase,
) : ViewModel() {
    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val action = MutableStateFlow(false to null as String?)

    val state = combine(
        selectedMonth.flatMapLatest { month -> budgetRepository.observeBudgets(month) },
        categoryRepository.observeCategories(),
        selectedMonth.flatMapLatest { month -> transactionRepository.observeMonth(month) },
        selectedMonth,
        action,
    ) { budgets, categories, monthTransactions, month, actionState ->
        val byId = categories.associateBy(Category::id)
        val byName = categories.associateBy { it.name.lowercase().trim() }

        // Build spent map: primary key = categoryId, secondary fallback = category name (for legacy txs)
        val spentByCategoryId = monthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { tx ->
                when {
                    tx.categoryId != null && tx.categoryId.isNotBlank() -> tx.categoryId
                    // Legacy: some old transactions may have stored category name as categoryId
                    else -> null
                }
            }
            .filterKeys { it != null }
            .mapKeys { it.key!! }
            .mapValues { (_, txs) -> txs.sumOf { it.amount.value } }

        // Name-based spent map for legacy transactions whose categoryId was a name string
        val spentByCategoryName = monthTransactions
            .filter { it.type == TransactionType.EXPENSE && it.categoryId != null }
            .groupBy { tx -> tx.categoryId!!.lowercase().trim() }
            .mapValues { (_, txs) -> txs.sumOf { it.amount.value } }

        val items = budgets.map { budget ->
            val cat = byId[budget.categoryId]
            // Dynamic spentAmount: SUM of both ID-matched and name-matched (legacy fallback) transactions
            // Using addition (not ?:) so both modern and legacy transactions are always accumulated.
            // Guard: only add name-based amount if name ≠ categoryId (avoids double-count when they happen to be equal)
            val byIdAmount = spentByCategoryId[budget.categoryId] ?: 0L
            val catNameLower = cat?.name?.lowercase()?.trim()
            val byNameAmount = if (catNameLower != null &&
                catNameLower != budget.categoryId.lowercase().trim()
            ) {
                spentByCategoryName[catNameLower] ?: 0L
            } else 0L
            val dynamicSpent = byIdAmount + byNameAmount
            val dynamicBudget = budget.copy(spentAmount = Money(dynamicSpent))
            BudgetItemUi(dynamicBudget, cat, getBudgetStatus(dynamicBudget))
        }.sortedByDescending { it.status.progress }

        BudgetUiState(
            month = month,
            items = items,
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
        // spentAmount stored = 0; real value computed dynamically from transactions above
        val budget = existing?.copy(limitAmount = Money(limit)) ?: Budget(
            id = "${categoryId}_${month}", categoryId = categoryId, month = month,
            limitAmount = Money(limit), spentAmount = Money(0),
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

