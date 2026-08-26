package com.finlux.app.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.time.FinanceTime
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.DashboardSummary
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtPaymentHistory
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.FinancialGoal
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.DebtRepository
import com.finlux.app.domain.repository.GoalRepository
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.TransactionRangeRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.FinancialPeriodResolver
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
import kotlin.math.roundToInt

enum class ReportPeriod(val label: String) {
    SALARY_CYCLE("Kỳ lương"),
    MONTH("Tháng"),
    QUARTER("Quý"),
    YEAR("Năm"),
    CUSTOM("Tùy chọn"),
}

data class ReportRange(val start: LocalDate, val end: LocalDate)
data class CategoryExpense(val category: Category?, val amount: Long, val percentage: Float = 0f, val transactionCount: Int = 0)
data class DailyExpense(val date: LocalDate, val amount: Long)
data class CashFlowPoint(val date: LocalDate, val income: Long, val expense: Long)
data class WalletActivity(val wallet: Wallet?, val income: Long, val expense: Long, val total: Long = income + expense)

data class DebtReportItem(
    val debt: DebtAccount,
    val totalPaid: Long,
    val remaining: Long,
    val progress: Float,
)

data class GoalReportItem(
    val goal: FinancialGoal,
    val target: Long,
    val saved: Long,
    val progress: Float,
)

data class BudgetReportItem(
    val budget: Budget,
    val category: Category?,
    val limit: Long,
    val spent: Long,
    val percent: Float,
    val remaining: Long,
    val isOverBudget: Boolean,
)

data class WalletReportItem(
    val wallet: Wallet,
    val balance: Long,
    val percentageOfTotal: Float,
    val incomeInPeriod: Long,
    val expenseInPeriod: Long,
)

data class ReportsUiState(
    val period: ReportPeriod = ReportPeriod.MONTH,
    val range: ReportRange = ReportRange(LocalDate.now().withDayOfMonth(1), LocalDate.now()),
    val summary: DashboardSummary = DashboardSummary(),
    val expensesByCategory: List<CategoryExpense> = emptyList(),
    val incomeByCategory: List<CategoryExpense> = emptyList(),
    val dailyExpenses: List<DailyExpense> = emptyList(),
    val cashFlow: List<CashFlowPoint> = emptyList(),
    val walletActivity: List<WalletActivity> = emptyList(),
    val transactionCount: Int = 0,
    val averageExpense: Long = 0,
    val averageIncome: Long = 0,
    val largestExpense: FinanceTransaction? = null,
    val largestIncome: FinanceTransaction? = null,
    val savingsRatePercent: Int = 0,
    val previousIncome: Long = 0,
    val previousExpense: Long = 0,
    val previousNet: Long = 0,
    val filteredTransactions: List<FinanceTransaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    // Vay nợ (Debts & Loans)
    val debts: List<DebtAccount> = emptyList(),
    val debtReportItems: List<DebtReportItem> = emptyList(),
    val totalDebtRemaining: Long = 0L,
    val totalDebtOriginal: Long = 0L,
    val totalDebtPaid: Long = 0L,
    val totalDebtInterestPaidInPeriod: Long = 0L,
    val totalDebtPrincipalPaidInPeriod: Long = 0L,
    // Tiết kiệm & Mục tiêu (Savings & Goals)
    val goals: List<FinancialGoal> = emptyList(),
    val goalReportItems: List<GoalReportItem> = emptyList(),
    val totalGoalTarget: Long = 0L,
    val totalGoalSaved: Long = 0L,
    val overallGoalProgress: Float = 0f,
    // Ngân sách (Budgets)
    val budgets: List<Budget> = emptyList(),
    val budgetReportItems: List<BudgetReportItem> = emptyList(),
    val totalBudgetLimit: Long = 0L,
    val totalBudgetSpent: Long = 0L,
    val totalBudgetRemaining: Long = 0L,
    val budgetUsagePercent: Int = 0,
    val overBudgetCount: Int = 0,
    // Tài sản & Ví (Wallets & Net Worth)
    val walletReportItems: List<WalletReportItem> = emptyList(),
    val totalAssets: Long = 0L,
    val totalNetWorth: Long = 0L,
    val assetsByType: Map<WalletType, Long> = emptyMap(),
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
    private val debtRepository: DebtRepository,
    private val goalRepository: GoalRepository,
    private val budgetRepository: BudgetRepository,
    private val financialPeriodResolver: FinancialPeriodResolver,
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

        val financialPeriod = financialPeriodResolver.resolvePeriodContaining(window.currentStart, salaryConfig)
        val budgetsFlow = if (financialPeriod != null) {
            budgetRepository.observeBudgets(financialPeriod.key)
        } else {
            flowOf(emptyList())
        }

        combine(
            transactionsFlow,
            categoryRepository.observeCategories(),
            walletRepository.observeWallets(),
            debtRepository.observeDebts(),
            debtRepository.observeAllPaymentHistory(),
            goalRepository.observeGoals(),
            budgetsFlow,
        ) { args: Array<Any> ->
            @Suppress("UNCHECKED_CAST")
            val transactions = args[0] as List<FinanceTransaction>
            @Suppress("UNCHECKED_CAST")
            val categories = args[1] as List<Category>
            @Suppress("UNCHECKED_CAST")
            val wallets = args[2] as List<Wallet>
            @Suppress("UNCHECKED_CAST")
            val debts = args[3] as List<DebtAccount>
            @Suppress("UNCHECKED_CAST")
            val debtPayments = args[4] as List<DebtPaymentHistory>
            @Suppress("UNCHECKED_CAST")
            val goals = args[5] as List<FinancialGoal>
            @Suppress("UNCHECKED_CAST")
            val budgets = args[6] as List<Budget>

            buildState(
                transactions = transactions,
                categories = categories,
                wallets = wallets,
                debts = debts,
                debtPayments = debtPayments,
                goals = goals,
                budgets = budgets,
                window = window,
                salaryConfig = salaryConfig,
                requestedPeriod = period,
            )
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
        debts: List<DebtAccount>,
        debtPayments: List<DebtPaymentHistory>,
        goals: List<FinancialGoal>,
        budgets: List<Budget>,
        window: ReportQueryWindow,
        salaryConfig: SalaryCycleConfig,
        requestedPeriod: ReportPeriod,
    ): ReportsUiState {
        val zone = FinanceTime.VIETNAM_ZONE
        val range = window.range

        fun inRange(date: Instant, startInclusive: Instant, endExclusive: Instant): Boolean {
            return date >= startInclusive && date < endExclusive
        }

        val filtered = transactions.filter { inRange(it.date, window.currentStart, window.currentEndExclusive) }
        val incomeItems = filtered.filter { it.type == TransactionType.INCOME }
        val expenseItems = filtered.filter { it.type == TransactionType.EXPENSE }
        val income = incomeItems.sumOf { it.amount.value }
        val expense = expenseItems.sumOf { it.amount.value }
        val categoryMap = categories.associateBy(Category::id)
        val walletMap = wallets.associateBy(Wallet::id)

        // Expense by Category
        val byCategoryExpense = expenseItems.groupBy { it.categoryId }.map { (id, items) ->
            val sum = items.sumOf { it.amount.value }
            val pct = if (expense > 0) (sum.toFloat() / expense.toFloat()) else 0f
            CategoryExpense(categoryMap[id], sum, pct, items.size)
        }.sortedByDescending(CategoryExpense::amount)

        // Income by Category
        val byCategoryIncome = incomeItems.groupBy { it.categoryId }.map { (id, items) ->
            val sum = items.sumOf { it.amount.value }
            val pct = if (income > 0) (sum.toFloat() / income.toFloat()) else 0f
            CategoryExpense(categoryMap[id], sum, pct, items.size)
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

        val previous = transactions.filter { inRange(it.date, window.previousStart, window.previousEndExclusive) }
        val previousIncome = previous.filter { it.type == TransactionType.INCOME }.sumOf { it.amount.value }
        val previousExpense = previous.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount.value }

        // Vay nợ (Debts & Loans)
        val totalDebtRemaining = debts.filter { !it.isSettled }.sumOf { it.remainingBalance.value }
        val totalDebtOriginal = debts.sumOf { it.totalAmount.value }
        val totalDebtPaid = (totalDebtOriginal - totalDebtRemaining).coerceAtLeast(0L)
        val debtPaymentsInPeriod = debtPayments.filter { inRange(it.paymentDate, window.currentStart, window.currentEndExclusive) }
        val totalDebtInterestPaidInPeriod = debtPaymentsInPeriod.sumOf { it.interestPaid.value }
        val totalDebtPrincipalPaidInPeriod = debtPaymentsInPeriod.sumOf { it.principalPaid.value }
        val debtReportItems = debts.map { d ->
            DebtReportItem(
                debt = d,
                totalPaid = d.paidAmount.value,
                remaining = d.remainingBalance.value,
                progress = d.progress,
            )
        }

        // Tiết kiệm & Mục tiêu (Goals & Savings)
        val totalGoalTarget = goals.sumOf { it.targetAmount.value }
        val totalGoalSaved = goals.sumOf { it.savedAmount.value }
        val overallGoalProgress = if (totalGoalTarget > 0) (totalGoalSaved.toFloat() / totalGoalTarget.toFloat()).coerceIn(0f, 1f) else 0f
        val goalReportItems = goals.map { g ->
            val p = if (g.targetAmount.value > 0) (g.savedAmount.value.toFloat() / g.targetAmount.value.toFloat()).coerceIn(0f, 1f) else 0f
            GoalReportItem(g, g.targetAmount.value, g.savedAmount.value, p)
        }
        val savingsRatePercent = if (income > 0) (((income - expense).toFloat() / income.toFloat()) * 100).roundToInt().coerceIn(-100, 100) else 0

        // Ngân sách (Budgets)
        val totalBudgetLimit = budgets.sumOf { it.limitAmount.value }
        val totalBudgetSpent = budgets.sumOf { it.spentAmount.value }
        val totalBudgetRemaining = (totalBudgetLimit - totalBudgetSpent).coerceAtLeast(0L)
        val budgetUsagePercent = if (totalBudgetLimit > 0) ((totalBudgetSpent.toFloat() / totalBudgetLimit.toFloat()) * 100).roundToInt() else 0
        val overBudgetCount = budgets.count { it.spentAmount.value > it.limitAmount.value }
        val budgetReportItems = budgets.map { b ->
            val cat = categoryMap[b.categoryId]
            val lim = b.limitAmount.value
            val spent = b.spentAmount.value
            val pct = if (lim > 0) (spent.toFloat() / lim.toFloat()) else 0f
            BudgetReportItem(
                budget = b,
                category = cat,
                limit = lim,
                spent = spent,
                percent = pct,
                remaining = (lim - spent).coerceAtLeast(0L),
                isOverBudget = spent > lim,
            )
        }

        // Tài sản & Ví (Wallets & Net Worth)
        val activeWallets = wallets.filter { it.status == "active" }
        val totalAssets = activeWallets.sumOf { it.balance.value }
        val totalNetWorth = totalAssets - totalDebtRemaining
        val assetsByType = activeWallets.groupBy { it.type }.mapValues { (_, list) -> list.sumOf { it.balance.value } }
        val walletReportItems = activeWallets.map { w ->
            val act = walletActivity.find { it.wallet?.id == w.id }
            val pct = if (totalAssets > 0) (w.balance.value.toFloat() / totalAssets.toFloat()) else 0f
            WalletReportItem(
                wallet = w,
                balance = w.balance.value,
                percentageOfTotal = pct,
                incomeInPeriod = act?.income ?: 0L,
                expenseInPeriod = act?.expense ?: 0L,
            )
        }.sortedByDescending { it.balance }

        // Tính trung bình ngày dựa trên số ngày thực tế đã trôi qua trong kỳ (đến ngày hôm nay)
        val effectiveEndDate = if (range.end.isAfter(today)) today else range.end
        val daysElapsedInPeriod = maxOf(1, ChronoUnit.DAYS.between(range.start, effectiveEndDate).toInt() + 1)
        val avgExpense = if (daysElapsedInPeriod > 0) expense / daysElapsedInPeriod else 0
        val avgIncome = if (daysElapsedInPeriod > 0) income / daysElapsedInPeriod else 0

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
            expensesByCategory = byCategoryExpense,
            incomeByCategory = byCategoryIncome,
            dailyExpenses = cashFlow.filter { it.expense > 0 }.map { DailyExpense(it.date, it.expense) },
            cashFlow = cashFlow,
            walletActivity = walletActivity,
            transactionCount = incomeItems.size + expenseItems.size,
            averageExpense = avgExpense,
            averageIncome = avgIncome,
            largestExpense = expenseItems.maxByOrNull { it.amount.value },
            largestIncome = incomeItems.maxByOrNull { it.amount.value },
            savingsRatePercent = savingsRatePercent,
            previousIncome = previousIncome,
            previousExpense = previousExpense,
            previousNet = previousIncome - previousExpense,
            filteredTransactions = filtered,
            categories = categories,
            wallets = wallets,
            // Debts
            debts = debts,
            debtReportItems = debtReportItems,
            totalDebtRemaining = totalDebtRemaining,
            totalDebtOriginal = totalDebtOriginal,
            totalDebtPaid = totalDebtPaid,
            totalDebtInterestPaidInPeriod = totalDebtInterestPaidInPeriod,
            totalDebtPrincipalPaidInPeriod = totalDebtPrincipalPaidInPeriod,
            // Goals & Savings
            goals = goals,
            goalReportItems = goalReportItems,
            totalGoalTarget = totalGoalTarget,
            totalGoalSaved = totalGoalSaved,
            overallGoalProgress = overallGoalProgress,
            // Budgets
            budgets = budgets,
            budgetReportItems = budgetReportItems,
            totalBudgetLimit = totalBudgetLimit,
            totalBudgetSpent = totalBudgetSpent,
            totalBudgetRemaining = totalBudgetRemaining,
            budgetUsagePercent = budgetUsagePercent,
            overBudgetCount = overBudgetCount,
            // Wallets & Net worth
            walletReportItems = walletReportItems,
            totalAssets = totalAssets,
            totalNetWorth = totalNetWorth,
            assetsByType = assetsByType,
            isSalaryCycleEnabled = salaryConfig.enabled,
            availablePeriods = availablePeriods,
        )
    }
}
