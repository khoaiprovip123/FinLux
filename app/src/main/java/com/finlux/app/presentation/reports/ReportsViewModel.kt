package com.finlux.app.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.time.FinanceTime
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.DashboardSummary
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.TransactionRangeRepository
import com.finlux.app.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

enum class ReportPeriod(val label: String) {
    SALARY_CYCLE("Kỳ lương"),
    MONTH("Tháng"),
    QUARTER("Quý"),
    YEAR("Năm"),
    CUSTOM("Tùy chọn"),
}

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
    val filteredTransactions: List<FinanceTransaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val isSalaryCycleEnabled: Boolean = false,
    val availablePeriods: List<ReportPeriod> = listOf(ReportPeriod.MONTH, ReportPeriod.QUARTER, ReportPeriod.YEAR, ReportPeriod.CUSTOM),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val transactionRangeRepository: TransactionRangeRepository,
    private val categoryRepository: CategoryRepository,
    private val walletRepository: WalletRepository,
    private val salaryCycleRepository: SalaryCycleRepository,
    private val windowResolver: ReportQueryWindowResolver,
) : ViewModel() {
    val selectedPeriod = MutableStateFlow(ReportPeriod.MONTH)
    private val today = LocalDate.now(FinanceTime.VIETNAM_ZONE)
    private val customRange = MutableStateFlow(ReportRange(today.minusDays(29), today))

    private val windowFlow = combine(
        selectedPeriod,
        customRange,
        salaryCycleRepository.observeConfig(),
    ) { period, custom, salaryConfig ->
        val now = Instant.now()
        val zone = FinanceTime.VIETNAM_ZONE
        val window = windowResolver.resolve(period, custom, now, salaryConfig, zone)
        Triple(window, salaryConfig, period)
    }

    val state = windowFlow.flatMapLatest { (window, salaryConfig, period) ->
        val queryStart = minOf(window.currentStart, window.previousStart)
        val queryEnd = maxOf(window.currentEndExclusive, window.previousEndExclusive)
        val transactionsFlow = if (queryStart < queryEnd) {
            transactionRangeRepository.observeRange(queryStart, queryEnd)
        } else {
            flowOf(emptyList())
        }

        combine(
            transactionsFlow,
            categoryRepository.observeCategories(),
            walletRepository.observeWallets(),
        ) { transactions, categories, wallets ->
            buildState(transactions, categories, wallets, window, salaryConfig, period)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsUiState())

    fun selectPeriod(period: ReportPeriod) {
        selectedPeriod.value = period
    }

    fun setCustomRange(start: LocalDate, end: LocalDate) {
        customRange.value = if (start <= end) ReportRange(start, end) else ReportRange(end, start)
        selectedPeriod.value = ReportPeriod.CUSTOM
    }

    private fun buildState(
        transactions: List<FinanceTransaction>,
        categories: List<Category>,
        wallets: List<Wallet>,
        window: ReportQueryWindow,
        salaryConfig: SalaryCycleConfig,
        requestedPeriod: ReportPeriod,
    ): ReportsUiState {
        val zone = FinanceTime.VIETNAM_ZONE
        val range = window.range

        fun inRange(item: FinanceTransaction, startInclusive: Instant, endExclusive: Instant): Boolean {
            return item.date >= startInclusive && item.date < endExclusive
        }

        val filtered = transactions.filter { inRange(it, window.currentStart, window.currentEndExclusive) }
        val incomeItems = filtered.filter { it.type == TransactionType.INCOME }
        val expenseItems = filtered.filter { it.type == TransactionType.EXPENSE }
        val income = incomeItems.sumOf { it.amount.value }
        val expense = expenseItems.sumOf { it.amount.value }
        val categoryMap = categories.associateBy(Category::id)
        val walletMap = wallets.associateBy(Wallet::id)

        val byCategory = expenseItems.groupBy { it.categoryId }.map { (id, items) ->
            CategoryExpense(categoryMap[id], items.sumOf { it.amount.value })
        }.sortedByDescending(CategoryExpense::amount)

        val dayCount = ChronoUnit.DAYS.between(range.start, range.end).coerceAtMost(365)
        val allDates = (0..dayCount).map { range.start.plusDays(it) }
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

        val previous = transactions.filter { inRange(it, window.previousStart, window.previousEndExclusive) }
        val previousIncome = previous.filter { it.type == TransactionType.INCOME }.sumOf { it.amount.value }
        val previousExpense = previous.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount.value }

        val availablePeriods = if (salaryConfig.enabled) {
            listOf(ReportPeriod.SALARY_CYCLE, ReportPeriod.MONTH, ReportPeriod.QUARTER, ReportPeriod.YEAR, ReportPeriod.CUSTOM)
        } else {
            listOf(ReportPeriod.MONTH, ReportPeriod.QUARTER, ReportPeriod.YEAR, ReportPeriod.CUSTOM)
        }

        val effectivePeriod = if (requestedPeriod == ReportPeriod.SALARY_CYCLE && !salaryConfig.enabled) {
            ReportPeriod.MONTH
        } else {
            requestedPeriod
        }

        return ReportsUiState(
            period = effectivePeriod,
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
            filteredTransactions = filtered,
            categories = categories,
            wallets = wallets,
            isSalaryCycleEnabled = salaryConfig.enabled,
            availablePeriods = availablePeriods,
        )
    }
}
