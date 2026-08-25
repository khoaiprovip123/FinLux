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
data class IncomeDayStat(val day: Int, val amount: Long)

data class IncomeUiState(
    val month: YearMonth = YearMonth.now(),
    val total: Long = 0,
    val changePercent: Int = 0,
    val transactions: List<FinanceTransaction> = emptyList(),
    val categories: Map<String, Category> = emptyMap(),
    val categoryStats: List<IncomeCategoryStat> = emptyList(),
    val dailyAverage: Long = 0,
    val highest: Long = 0,
    val lowest: Long = 0,
    val dailyStats: List<IncomeDayStat> = emptyList(),
)

@HiltViewModel
class IncomeViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {
    private val selectedMonth = MutableStateFlow(YearMonth.now())

    val state = combine(
        transactionRepository.observeRecent(5_000),
        categoryRepository.observeCategories(),
        selectedMonth,
    ) { transactions, categories, month ->
        val zone = ZoneId.systemDefault()
        fun incomes(target: YearMonth) = transactions.filter {
            it.type == TransactionType.INCOME && YearMonth.from(it.date.atZone(zone)) == target
        }
        val current = incomes(month).sortedByDescending { it.date }
        val previousTotal = incomes(month.minusMonths(1)).sumOf { it.amount.value }
        val total = current.sumOf { it.amount.value }
        val categoryMap = categories.associateBy { it.id }
        val categoryStats = current.groupBy { it.categoryId }.map { (id, rows) ->
            val amount = rows.sumOf { it.amount.value }
            IncomeCategoryStat(categoryMap[id], amount, if (total == 0L) 0 else (amount * 100 / total).toInt())
        }.sortedByDescending { it.amount }

        IncomeUiState(
            month = month,
            total = total,
            changePercent = if (previousTotal == 0L) 0 else (((total - previousTotal) * 100) / previousTotal).toInt(),
            transactions = current,
            categories = categoryMap,
            categoryStats = categoryStats,
            dailyAverage = if (total == 0L) 0L else total / month.lengthOfMonth(),
            highest = current.maxOfOrNull { it.amount.value } ?: 0L,
            lowest = current.minOfOrNull { it.amount.value } ?: 0L,
            dailyStats = (1..month.lengthOfMonth()).map { day ->
                IncomeDayStat(day, current.filter { it.date.atZone(zone).dayOfMonth == day }.sumOf { it.amount.value })
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), IncomeUiState())

    fun previousMonth() { selectedMonth.value = selectedMonth.value.minusMonths(1) }
    fun nextMonth() { if (selectedMonth.value < YearMonth.now()) selectedMonth.value = selectedMonth.value.plusMonths(1) }
}
