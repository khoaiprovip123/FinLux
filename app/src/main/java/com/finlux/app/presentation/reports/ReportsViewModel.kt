package com.finlux.app.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.DashboardSummary
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

enum class ReportPeriod(val label: String) { MONTH("Tháng"), QUARTER("Quý"), YEAR("Năm"), CUSTOM("Tùy chọn") }
data class ReportRange(val start: LocalDate, val end: LocalDate)
data class CategoryExpense(val category: Category?, val amount: Long)
data class DailyExpense(val date: LocalDate, val amount: Long)
data class CashFlowPoint(val date: LocalDate, val income: Long, val expense: Long)
data class WalletActivity(val wallet: Wallet?, val income: Long, val expense: Long, val total: Long = income + expense)

data class ReportsUiState(
    val period: ReportPeriod = ReportPeriod.MONTH,
    val range: ReportRange = ReportRange(LocalDate.now().withDayOfMonth(1), LocalDate.now()),
    val summary: DashboardSummary = DashboardSummary(),
    val expensesByCategory: List<CategoryExpense> = emptyList(),
    val dailyExpenses: List<DailyExpense> = emptyList(),
    val cashFlow: List<CashFlowPoint> = emptyList(),
    val walletActivity: List<WalletActivity> = emptyList(),
    val transactionCount: Int = 0,
    val averageExpense: Long = 0,
    val largestExpense: FinanceTransaction? = null,
    val previousIncome: Long = 0,
    val previousExpense: Long = 0,
    val previousNet: Long = 0,
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    walletRepository: WalletRepository,
) : ViewModel() {
    val selectedPeriod = MutableStateFlow(ReportPeriod.MONTH)
    private val today = LocalDate.now()
    private val customRange = MutableStateFlow(ReportRange(today.minusDays(29), today))

    val state = combine(
        transactionRepository.observeRecent(5_000), categoryRepository.observeCategories(),
        walletRepository.observeWallets(), selectedPeriod, customRange,
    ) { transactions, categories, wallets, period, custom -> buildState(transactions, categories, wallets, period, custom) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsUiState())

    fun selectPeriod(period: ReportPeriod) { selectedPeriod.value = period }
    fun setCustomRange(start: LocalDate, end: LocalDate) {
        customRange.value = if (start <= end) ReportRange(start, end) else ReportRange(end, start)
        selectedPeriod.value = ReportPeriod.CUSTOM
    }

    private fun buildState(
        transactions: List<FinanceTransaction>, categories: List<Category>, wallets: List<Wallet>,
        period: ReportPeriod, custom: ReportRange,
    ): ReportsUiState {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val range = when (period) {
            ReportPeriod.MONTH -> ReportRange(today.with(TemporalAdjusters.firstDayOfMonth()), today)
            ReportPeriod.QUARTER -> {
                val firstMonth = ((today.monthValue - 1) / 3) * 3 + 1
                ReportRange(today.withMonth(firstMonth).withDayOfMonth(1), today)
            }
            ReportPeriod.YEAR -> ReportRange(today.with(TemporalAdjusters.firstDayOfYear()), today)
            ReportPeriod.CUSTOM -> custom
        }
        fun inRange(item: FinanceTransaction, target: ReportRange): Boolean {
            val date = item.date.atZone(zone).toLocalDate()
            return !date.isBefore(target.start) && !date.isAfter(target.end)
        }
        val filtered = transactions.filter { inRange(it, range) }
        val incomeItems = filtered.filter { it.type == TransactionType.INCOME }
        val expenseItems = filtered.filter { it.type == TransactionType.EXPENSE }
        val income = incomeItems.sumOf { it.amount.value }
        val expense = expenseItems.sumOf { it.amount.value }
        val categoryMap = categories.associateBy(Category::id)
        val walletMap = wallets.associateBy(Wallet::id)
        val byCategory = expenseItems.groupBy { it.categoryId }.map { (id, items) ->
            CategoryExpense(categoryMap[id], items.sumOf { it.amount.value })
        }.sortedByDescending(CategoryExpense::amount)
        val allDates = (0..java.time.temporal.ChronoUnit.DAYS.between(range.start, range.end).coerceAtMost(365)).map { range.start.plusDays(it) }
        val byDate = filtered.groupBy { it.date.atZone(zone).toLocalDate() }
        val cashFlow = allDates.map { date ->
            val items = byDate[date].orEmpty()
            CashFlowPoint(
                date,
                items.filter { it.type == TransactionType.INCOME }.sumOf { it.amount.value },
                items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount.value },
            )
        }
        val walletActivity = filtered.filter { it.type == TransactionType.INCOME || it.type == TransactionType.EXPENSE }
            .groupBy(FinanceTransaction::walletId).map { (id, items) ->
                val walletIncome = items.filter { it.type == TransactionType.INCOME }.sumOf { it.amount.value }
                val walletExpense = items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount.value }
                WalletActivity(walletMap[id], walletIncome, walletExpense)
            }.sortedByDescending(WalletActivity::total)
        val duration = java.time.temporal.ChronoUnit.DAYS.between(range.start, range.end) + 1
        val previousRange = ReportRange(range.start.minusDays(duration), range.start.minusDays(1))
        val previous = transactions.filter { inRange(it, previousRange) }
        val previousIncome = previous.filter { it.type == TransactionType.INCOME }.sumOf { it.amount.value }
        val previousExpense = previous.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount.value }
        return ReportsUiState(
            period = period,
            range = range,
            summary = DashboardSummary(Money(income), Money(expense), income - expense),
            expensesByCategory = byCategory,
            dailyExpenses = cashFlow.filter { it.expense > 0 }.map { DailyExpense(it.date, it.expense) },
            cashFlow = cashFlow,
            walletActivity = walletActivity,
            transactionCount = incomeItems.size + expenseItems.size,
            averageExpense = if (expenseItems.isEmpty()) 0 else expense / expenseItems.size,
            largestExpense = expenseItems.maxByOrNull { it.amount.value },
            previousIncome = previousIncome,
            previousExpense = previousExpense,
            previousNet = previousIncome - previousExpense,
        )
    }
}
