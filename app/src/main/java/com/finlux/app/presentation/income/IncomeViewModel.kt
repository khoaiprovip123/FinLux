package com.finlux.app.presentation.income

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

data class IncomeCategoryStat(val category: Category?, val amount: Long, val percent: Int)

data class IncomeUiState(
    val month: YearMonth = YearMonth.now(),
    val transactions: List<FinanceTransaction> = emptyList(),
    val categories: Map<String, Category> = emptyMap(),
    val categoryStats: List<IncomeCategoryStat> = emptyList(),
    val total: Long = 0,
    val dailyAverage: Long = 0,
    val highest: Long = 0,
    val lowest: Long = 0,
)

@HiltViewModel
class IncomeViewModel @Inject constructor(
    repository: TransactionRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {
    private val selectedMonth = MutableStateFlow(YearMonth.now())

    val state = combine(
        repository.observeRecent(200),
        categoryRepository.observeCategories(),
        selectedMonth,
    ) { transactions, categories, month ->
        val categoryMap = categories.associateBy(Category::id)
        val rows = transactions.filter { transaction ->
            transaction.type == TransactionType.INCOME &&
                YearMonth.from(transaction.date.atZone(ZoneId.systemDefault())) == month
        }.sortedByDescending(FinanceTransaction::date)
        val total = rows.sumOf { it.amount.value }
        val groups = rows.groupBy(FinanceTransaction::categoryId).map { (categoryId, items) ->
            val amount = items.sumOf { it.amount.value }
            IncomeCategoryStat(categoryMap[categoryId], amount, if (total <= 0L) 0 else (amount * 100L / total).toInt())
        }.sortedByDescending(IncomeCategoryStat::amount)
        IncomeUiState(
            month = month,
            transactions = rows,
            categories = categoryMap,
            categoryStats = groups,
            total = total,
            dailyAverage = if (total == 0L) 0L else total / month.lengthOfMonth(),
            highest = rows.maxOfOrNull { it.amount.value } ?: 0L,
            lowest = rows.minOfOrNull { it.amount.value } ?: 0L,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), IncomeUiState())

    fun previousMonth() { selectedMonth.value = selectedMonth.value.minusMonths(1) }
    fun nextMonth() { selectedMonth.value = selectedMonth.value.plusMonths(1).coerceAtMost(YearMonth.now()) }
}
