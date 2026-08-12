package com.finlux.app.presentation.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ExpenseCategoryStat(val category: Category?, val amount: Long, val percent: Int)
data class ExpenseDayStat(val day: Int, val amount: Long)
data class ExpenseUiState(
    val month: YearMonth = YearMonth.now(),
    val total: Long = 0,
    val changePercent: Int = 0,
    val transactions: List<FinanceTransaction> = emptyList(),
    val categories: Map<String, Category> = emptyMap(),
    val categoryStats: List<ExpenseCategoryStat> = emptyList(),
    val dailyStats: List<ExpenseDayStat> = emptyList(),
)

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {
    private val selectedMonth = MutableStateFlow(YearMonth.now())
    val state = combine(transactionRepository.observeRecent(5_000), categoryRepository.observeCategories(), selectedMonth) { transactions, categories, month ->
        val zone = ZoneId.systemDefault()
        fun expenses(target: YearMonth) = transactions.filter {
            it.type == TransactionType.EXPENSE && YearMonth.from(it.date.atZone(zone)) == target
        }
        val current = expenses(month).sortedByDescending { it.date }
        val previousTotal = expenses(month.minusMonths(1)).sumOf { it.amount.value }
        val total = current.sumOf { it.amount.value }
        val categoryMap = categories.associateBy { it.id }
        val categoryStats = current.groupBy { it.categoryId }.map { (id, rows) ->
            val amount = rows.sumOf { it.amount.value }
            ExpenseCategoryStat(categoryMap[id], amount, if (total == 0L) 0 else (amount * 100 / total).toInt())
        }.sortedByDescending { it.amount }
        ExpenseUiState(
            month = month,
            total = total,
            changePercent = if (previousTotal == 0L) 0 else (((total - previousTotal) * 100) / previousTotal).toInt(),
            transactions = current,
            categories = categoryMap,
            categoryStats = categoryStats,
            dailyStats = (1..month.lengthOfMonth()).map { day ->
                ExpenseDayStat(day, current.filter { it.date.atZone(zone).dayOfMonth == day }.sumOf { it.amount.value })
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpenseUiState())

    fun previousMonth() { selectedMonth.value = selectedMonth.value.minusMonths(1) }
    fun nextMonth() { if (selectedMonth.value < YearMonth.now()) selectedMonth.value = selectedMonth.value.plusMonths(1) }
}
