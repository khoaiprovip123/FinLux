package com.finlux.app.presentation.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.FinancialPeriod
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.usecase.FinancialPeriodResolver
import com.finlux.app.core.time.FinanceTime
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ExpenseCategoryStat(val category: Category?, val amount: Long, val percent: Int)
data class ExpenseDayStat(val date: LocalDate, val amount: Long)
data class ExpenseUiState(
    val period: FinancialPeriod? = null,
    val canNavigateNext: Boolean = false,
    val total: Long = 0,
    val changePercent: Int = 0,
    val transactions: List<FinanceTransaction> = emptyList(),
    val categories: Map<String, Category> = emptyMap(),
    val categoryStats: List<ExpenseCategoryStat> = emptyList(),
    val dailyStats: List<ExpenseDayStat> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExpenseViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    salaryCycleRepository: SalaryCycleRepository,
    financialPeriodResolver: FinancialPeriodResolver,
) : ViewModel() {
    private data class PeriodSelection(
        val current: FinancialPeriod,
        val previous: FinancialPeriod,
        val zoneId: java.time.ZoneId,
    )

    private val selectedTime = MutableStateFlow(Instant.now())
    private val periodSelection = combine(selectedTime, salaryCycleRepository.observeConfig()) { time, config ->
        val current = financialPeriodResolver.resolvePeriodContaining(time, config)
        PeriodSelection(
            current = current,
            previous = financialPeriodResolver.resolvePeriodContaining(current.start.minusMillis(1), config),
            zoneId = FinanceTime.zoneOf(config.financeTimeZone),
        )
    }

    val state = periodSelection.flatMapLatest { selection ->
        combine(
            transactionRepository.observePeriod(selection.current.start, selection.current.endExclusive),
            transactionRepository.observePeriod(selection.previous.start, selection.previous.endExclusive),
            categoryRepository.observeCategories(),
        ) { currentTransactions, previousTransactions, categories ->
        val current = currentTransactions.filter { it.type == TransactionType.EXPENSE }.sortedByDescending { it.date }
        val previousTotal = previousTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount.value }
        val total = current.sumOf { it.amount.value }
        val categoryMap = categories.associateBy { it.id }
        val categoryStats = current.groupBy { it.categoryId }.map { (id, rows) ->
            val amount = rows.sumOf { it.amount.value }
            ExpenseCategoryStat(categoryMap[id], amount, if (total == 0L) 0 else (amount * 100 / total).toInt())
        }.sortedByDescending { it.amount }
        val startDate = selection.current.start.atZone(selection.zoneId).toLocalDate()
        val endDateExclusive = selection.current.endExclusive.atZone(selection.zoneId).toLocalDate()
        val periodDays = ChronoUnit.DAYS.between(startDate, endDateExclusive).toInt().coerceAtLeast(1)
        ExpenseUiState(
            period = selection.current,
            canNavigateNext = selection.current.endExclusive < Instant.now(),
            total = total,
            changePercent = if (previousTotal == 0L) 0 else (((total - previousTotal) * 100) / previousTotal).toInt(),
            transactions = current,
            categories = categoryMap,
            categoryStats = categoryStats,
            dailyStats = (0 until periodDays).map { offset ->
                val date = startDate.plusDays(offset.toLong())
                ExpenseDayStat(date, current.filter { it.date.atZone(selection.zoneId).toLocalDate() == date }.sumOf { it.amount.value })
            },
        )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpenseUiState())

    fun previousPeriod() {
        state.value.period?.let { selectedTime.value = it.start.minusMillis(1) }
    }

    fun nextPeriod() {
        val period = state.value.period ?: return
        if (state.value.canNavigateNext) selectedTime.value = period.endExclusive.plusMillis(1)
    }
}
