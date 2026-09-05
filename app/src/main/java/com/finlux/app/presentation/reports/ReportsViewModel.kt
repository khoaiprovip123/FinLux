package com.finlux.app.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.time.FinanceClock
import com.finlux.app.core.time.FinanceTime
import com.finlux.app.core.time.SystemFinanceClock
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.DashboardSummary
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtPaymentHistory
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.FinancialGoal
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.model.assetWallets
import com.finlux.app.domain.model.netGoalContribution
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.DebtRepository
import com.finlux.app.domain.repository.GoalRepository
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.TransactionRangeRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.CalculateSavingSpinStreakUseCase
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
import com.finlux.app.domain.model.CashMovementStatement
import com.finlux.app.domain.model.CumulativeFinancialMetrics
import com.finlux.app.domain.model.DailyComparisonMetric
import com.finlux.app.domain.model.DailyFinancialStatement
import com.finlux.app.domain.usecase.DailyStatementCalculator
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class CategoryExpense(val category: Category?, val amount: Long, val percentage: Float = 0f, val transactionCount: Int = 0)
data class DailyExpense(val date: LocalDate, val amount: Long)
data class CashFlowPoint(val date: LocalDate, val income: Long, val expense: Long)
data class WalletActivity(val wallet: Wallet?, val income: Long, val expense: Long, val total: Long = income + expense)

data class WalletSpendingDetail(
    val wallet: Wallet,
    val balance: Long,
    val percentageOfTotalAssets: Float,
    val incomeInPeriod: Long,
    val expenseInPeriod: Long,
    val transferInInPeriod: Long = 0L,
    val transferOutInPeriod: Long = 0L,
    val netCashflowInPeriod: Long,
    val expenseShareOfTotal: Float,
    val transactionCount: Int,
    val expensesByCategory: List<CategoryExpense>,
    val incomeByCategory: List<CategoryExpense>,
    val transactions: List<FinanceTransaction>,
) {
    val totalMoneyIn: Long get() = incomeInPeriod + transferInInPeriod
    val totalMoneyOut: Long get() = expenseInPeriod + transferOutInPeriod
    val netWalletChange: Long get() = totalMoneyIn - totalMoneyOut
}

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
    val transferInInPeriod: Long = 0L,
    val transferOutInPeriod: Long = 0L,
    val expenseShareOfTotal: Float = 0f,
    val spendingDetail: WalletSpendingDetail? = null,
) {
    val totalMoneyIn: Long get() = incomeInPeriod + transferInInPeriod
    val totalMoneyOut: Long get() = expenseInPeriod + transferOutInPeriod
    val netWalletChange: Long get() = totalMoneyIn - totalMoneyOut
}

data class DealReportItem(
    val deal: com.finlux.app.domain.model.FinancialDeal,
    val capitalOutlay: Long,
    val recovered: Long,
    val netProfitLoss: Long,
    val remainingCapital: Long,
    val roiPercentage: Double,
    val recoveryProgress: Float,
    val isFullyRecovered: Boolean,
)

data class DealsSummaryReport(
    val totalActiveCapitalOutlay: Long = 0L,
    val totalHistoricalCapitalOutlay: Long = 0L,
    val totalRecovered: Long = 0L,
    val totalNetProfit: Long = 0L,
    val totalInvestmentOutlay: Long = 0L,
    val totalLendingOutlay: Long = 0L,
    val totalLendingOutstanding: Long = 0L,
    val overallRoi: Double = 0.0,
    val investmentRatio: Float = 0.5f,
    val activeDealsCount: Int = 0,
    val completedDealsCount: Int = 0,
)

data class SavingSpinSummaryReport(
    val totalSaved: Long = 0L,
    val completedCount: Int = 0,
    val skippedCount: Int = 0,
    val currentStreak: Int = 0,
    val completionRate: Int = 0,
    val destinationBreakdown: List<com.finlux.app.domain.model.SavingSpinDestinationTotal> = emptyList(),
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
    /** Phần thu nhập còn lại sau chi tiêu trong kỳ, chưa đồng nghĩa với tiền đã gửi tiết kiệm. */
    val unspentCashFlow: Long = 0L,
    /** Tỷ lệ thu nhập còn được giữ lại trong kỳ. */
    val savingsRatePercent: Int = 0,
    /** Số tiền ròng đã nạp vào các mục tiêu qua danh mục `savings` trong kỳ. */
    val goalContributionInPeriod: Long = 0L,
    val trendInsights: List<String> = emptyList(),
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
    // Vòng quay tiết kiệm (Saving Spin)
    val savingSpinSummary: SavingSpinSummaryReport = SavingSpinSummaryReport(),
    // Thương vụ & Cho vay (Deals & Investments)
    val deals: List<com.finlux.app.domain.model.FinancialDeal> = emptyList(),
    val dealReportItems: List<DealReportItem> = emptyList(),
    val dealsSummary: DealsSummaryReport = DealsSummaryReport(),
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
    val walletSpendingDetails: List<WalletSpendingDetail> = emptyList(),
    val selectedWalletId: String? = null,
    val selectedWalletSpendingDetail: WalletSpendingDetail? = null,
    val totalAssets: Long = 0L,
    val totalNetWorth: Long = 0L,
    /**
     * Tài sản ròng toàn diện (True Net Worth):
     * = (Tổng tài sản ví) + (Vốn lưu động từ Deal & Cho vay đang chờ thu hồi) - (Tổng dư nợ phải trả)
     */
    val trueNetWorth: Long = 0L,
    val assetsByType: Map<WalletType, Long> = emptyMap(),
    val isSalaryCycleEnabled: Boolean = false,
    val availablePeriods: List<ReportPeriod> = listOf(
        ReportPeriod.TODAY,
        ReportPeriod.YESTERDAY,
        ReportPeriod.WEEK,
        ReportPeriod.LAST_7_DAYS,
        ReportPeriod.MONTH,
        ReportPeriod.QUARTER,
        ReportPeriod.YEAR,
        ReportPeriod.CUSTOM,
    ),
    // Reporting 2.0 Foundation
    val dailyStatements: List<DailyFinancialStatement> = emptyList(),
    val todayStatement: DailyFinancialStatement? = null,
    val cumulativeMetrics: CumulativeFinancialMetrics = CumulativeFinancialMetrics(0L, 0L, 0L, 0L, 0L, 0L),
    val yesterdayComparison: DailyComparisonMetric = DailyComparisonMetric(0L, 0L, 0L),
    val cashMovementStatement: CashMovementStatement? = null,
    val openingBalance: Long = 0L,
    val closingBalance: Long = 0L,
    val totalTransferIn: Long = 0L,
    val totalTransferOut: Long = 0L,
) {
    val selectedWallet: Wallet? get() = wallets.find { it.id == selectedWalletId }
    val currentDisplayBalance: Long get() = selectedWallet?.balance?.value ?: totalAssets
    val currentWalletNetChange: Long get() = selectedWalletSpendingDetail?.netWalletChange ?: summary.net
}

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
    private val dealRepository: com.finlux.app.domain.repository.DealRepository,
    private val savingSpinRepository: com.finlux.app.domain.repository.SavingSpinRepository,
    private val financialPeriodResolver: FinancialPeriodResolver,
    private val windowResolver: ReportQueryWindowResolver,
    private val dailyStatementCalculator: DailyStatementCalculator,
    private val clock: FinanceClock = SystemFinanceClock(),
    private val calculateSavingSpinStreakUseCase: CalculateSavingSpinStreakUseCase = CalculateSavingSpinStreakUseCase(financialPeriodResolver, clock),
) : ViewModel() {
    private val userSelectedPeriod = MutableStateFlow<ReportPeriod?>(null)
    private val today = clock.now().atZone(FinanceTime.VIETNAM_ZONE).toLocalDate()
    private val customRange = MutableStateFlow(ReportRange(today.minusDays(29), today))
    val selectedWalletId = MutableStateFlow<String?>(null)

    val selectedPeriod = MutableStateFlow(ReportPeriod.MONTH)

    init {
        viewModelScope.launch {
            salaryCycleRepository.observeConfig().collect { config ->
                if (userSelectedPeriod.value == null) {
                    val defaultPeriod = if (config.enabled) ReportPeriod.SALARY_CYCLE else ReportPeriod.MONTH
                    selectedPeriod.value = defaultPeriod
                }
            }
        }
    }

    private data class ReportWindowParams(
        val window: ReportQueryWindow,
        val salaryConfig: SalaryCycleConfig,
        val period: ReportPeriod,
        val selectedWalletId: String?,
    )

    private val windowFlow = combine(
        selectedPeriod,
        customRange,
        salaryCycleRepository.observeConfig(),
        selectedWalletId,
    ) { period, custom, salaryConfig, walletId ->
        val now = clock.now()
        val zone = FinanceTime.zoneOf(salaryConfig.financeTimeZone)
        val window = windowResolver.resolve(period, custom, now, salaryConfig, zone)
        ReportWindowParams(window, salaryConfig, period, walletId)
    }

    val state = windowFlow.flatMapLatest { params ->
        val window = params.window
        val salaryConfig = params.salaryConfig
        val period = params.period
        val selectedWalletId = params.selectedWalletId

        val queryStart = minOf(window.currentStart, window.previousStart)
        // Historical balance reconciliation starts from the current wallet balance and reverses
        // every ledger movement since the requested period. Therefore a historical report must
        // include the transaction tail from its earliest comparison boundary through "now".
        val ledgerTailEndExclusive = clock.now().plusMillis(1)
        val queryEnd = maxOf(
            maxOf(window.currentEndExclusive, window.previousEndExclusive),
            ledgerTailEndExclusive,
        )
        val transactionsFlow = if (queryStart < queryEnd) {
            transactionRangeRepository.observeRange(queryStart, queryEnd)
        } else {
            flowOf(emptyList())
        }

        val startPeriod = financialPeriodResolver.resolvePeriodContaining(window.currentStart, salaryConfig)
        val nowPeriod = financialPeriodResolver.resolvePeriodContaining(clock.now(), salaryConfig)
        val budgetsFlow = if (startPeriod.key == nowPeriod.key) {
            budgetRepository.observeBudgets(startPeriod.key)
        } else {
            combine(
                budgetRepository.observeBudgets(startPeriod.key),
                budgetRepository.observeBudgets(nowPeriod.key),
            ) { startBudgets, nowBudgets ->
                if (startBudgets.isNotEmpty()) startBudgets else nowBudgets
            }
        }

        combine(
            transactionsFlow,
            categoryRepository.observeCategories(),
            walletRepository.observeWallets(),
            debtRepository.observeDebts(),
            debtRepository.observeAllPaymentHistory(),
            goalRepository.observeGoals(),
            budgetsFlow,
            dealRepository.observeDeals(),
            savingSpinRepository.observeSessions(window.currentStart, window.currentEndExclusive),
            savingSpinRepository.observeDestinations(),
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
            @Suppress("UNCHECKED_CAST")
            val deals = args[7] as List<com.finlux.app.domain.model.FinancialDeal>
            @Suppress("UNCHECKED_CAST")
            val spinSessions = args[8] as List<com.finlux.app.domain.model.SavingSpinSession>
            @Suppress("UNCHECKED_CAST")
            val spinDestinations = args[9] as List<com.finlux.app.domain.model.SavingDestination>

            buildState(
                transactions = transactions,
                categories = categories,
                wallets = wallets,
                debts = debts,
                debtPayments = debtPayments,
                goals = goals,
                budgets = budgets,
                deals = deals,
                spinSessions = spinSessions,
                spinDestinations = spinDestinations,
                window = window,
                salaryConfig = salaryConfig,
                requestedPeriod = period,
                selectedWalletId = selectedWalletId,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsUiState())

    fun selectPeriod(period: ReportPeriod) {
        selectedPeriod.value = period
    }

    fun selectWallet(walletId: String?) {
        selectedWalletId.value = walletId
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
        deals: List<com.finlux.app.domain.model.FinancialDeal>,
        spinSessions: List<com.finlux.app.domain.model.SavingSpinSession>,
        spinDestinations: List<com.finlux.app.domain.model.SavingDestination>,
        window: ReportQueryWindow,
        salaryConfig: SalaryCycleConfig,
        requestedPeriod: ReportPeriod,
        selectedWalletId: String? = null,
    ): ReportsUiState {
        val zone = FinanceTime.zoneOf(salaryConfig.financeTimeZone)
        val range = window.range
        val today = clock.now().atZone(zone).toLocalDate()
        val effectiveEndDate = if (range.end.isAfter(today)) today.coerceAtLeast(range.start) else range.end

        fun inRange(date: Instant, startInclusive: Instant, endExclusive: Instant): Boolean {
            return date >= startInclusive && date < endExclusive
        }

        val allPeriodTransactions = transactions.filter { inRange(it.date, window.currentStart, window.currentEndExclusive) }
        val allPeriodExpenseItems = allPeriodTransactions.filter {
            it.type == TransactionType.EXPENSE && it.dealFlowType != com.finlux.app.domain.model.DealFlowType.OUTLAY_CAPITAL
        }
        val totalAllPeriodExpense = allPeriodExpenseItems.sumOf { it.amount.value }

        val filtered = if (selectedWalletId != null) {
            allPeriodTransactions.filter { it.walletId == selectedWalletId }
        } else {
            allPeriodTransactions
        }
        val incomeItems = filtered.filter {
            it.type == TransactionType.INCOME && it.dealFlowType != com.finlux.app.domain.model.DealFlowType.PRINCIPAL_RECOVERY
        }
        val expenseItems = filtered.filter {
            it.type == TransactionType.EXPENSE && it.dealFlowType != com.finlux.app.domain.model.DealFlowType.OUTLAY_CAPITAL
        }
        val transferInItems = filtered.filter { it.type == TransactionType.TRANSFER_IN }
        val transferOutItems = filtered.filter { it.type == TransactionType.TRANSFER_OUT }
        val income = incomeItems.sumOf { it.amount.value }
        val expense = expenseItems.sumOf { it.amount.value }
        val totalTransferIn = transferInItems.sumOf { it.amount.value }
        val totalTransferOut = transferOutItems.sumOf { it.amount.value }
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

        val dayCount = ChronoUnit.DAYS.between(range.start, effectiveEndDate).coerceAtMost(365)
        val allDates = (0..dayCount).map { range.start.plusDays(it) }
        val byDate = filtered.groupBy { it.date.atZone(zone).toLocalDate() }
        val cashFlow = allDates.map { date ->
            val items = byDate[date].orEmpty()
            CashFlowPoint(
                date,
                items.filter { it.type == TransactionType.INCOME && it.dealFlowType != com.finlux.app.domain.model.DealFlowType.PRINCIPAL_RECOVERY }.sumOf { it.amount.value },
                items.filter { it.type == TransactionType.EXPENSE && it.dealFlowType != com.finlux.app.domain.model.DealFlowType.OUTLAY_CAPITAL }.sumOf { it.amount.value },
            )
        }

        val allPrevious = transactions.filter { inRange(it.date, window.previousStart, window.previousEndExclusive) }
        val previous = if (selectedWalletId != null) {
            allPrevious.filter { it.walletId == selectedWalletId }
        } else {
            allPrevious
        }
        val previousIncome = previous.filter { it.type == TransactionType.INCOME && it.dealFlowType != com.finlux.app.domain.model.DealFlowType.PRINCIPAL_RECOVERY }.sumOf { it.amount.value }
        val previousExpense = previous.filter { it.type == TransactionType.EXPENSE && it.dealFlowType != com.finlux.app.domain.model.DealFlowType.OUTLAY_CAPITAL }.sumOf { it.amount.value }

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
        val savingsCategoryIds = categories.filter {
            it.id.equals("savings", ignoreCase = true) ||
                it.id == "203" ||
                it.name.contains("tiết kiệm", ignoreCase = true) ||
                it.name.contains("saving", ignoreCase = true) ||
                it.name.contains("tích lũy", ignoreCase = true)
        }.map { it.id }.toSet()
        val goalContributionInPeriod = filtered.netGoalContribution { catId ->
            catId != null && (catId in savingsCategoryIds || catId.equals("savings", ignoreCase = true))
        }

        // Thương vụ & Cho vay (Deals & Investments)
        val dealReportItems = deals.map { d ->
            DealReportItem(
                deal = d,
                capitalOutlay = d.totalCapitalOutlay.value,
                recovered = d.totalRecovered.value,
                netProfitLoss = d.netProfitLoss.value,
                remainingCapital = d.remainingCapital.value,
                roiPercentage = d.roiPercentage,
                recoveryProgress = d.recoveryProgress,
                isFullyRecovered = d.isFullyRecovered,
            )
        }
        val activeDeals = deals.filter { it.status == com.finlux.app.domain.model.DealStatus.ACTIVE }
        val completedDeals = deals.filter { it.status == com.finlux.app.domain.model.DealStatus.COMPLETED }
        val totalActiveCapitalOutlay = activeDeals.sumOf { it.remainingCapital.value }
        val totalHistoricalCapitalOutlay = deals.sumOf { it.totalCapitalOutlay.value }
        val totalRecoveredDeals = deals.sumOf { it.totalRecovered.value }
        val totalNetProfitDeals = deals.sumOf { it.netProfitLoss.value }

        val investmentDeals = deals.filter { it.category == com.finlux.app.domain.model.DealCategory.INVESTMENT }
        val lendingDeals = deals.filter { it.category == com.finlux.app.domain.model.DealCategory.LENDING }
        val totalInvestmentOutlay = investmentDeals.sumOf { it.totalCapitalOutlay.value }
        val totalLendingOutlay = lendingDeals.sumOf { it.totalCapitalOutlay.value }
        val totalLendingOutstanding = lendingDeals.filter { it.status == com.finlux.app.domain.model.DealStatus.ACTIVE }.sumOf { it.remainingCapital.value }
        val totalOutlayAll = totalInvestmentOutlay + totalLendingOutlay
        val investmentRatio = if (totalOutlayAll > 0) (totalInvestmentOutlay.toFloat() / totalOutlayAll.toFloat()) else 0.5f

        val overallDealRoi = if (totalHistoricalCapitalOutlay > 0) {
            (totalNetProfitDeals.toDouble() / totalHistoricalCapitalOutlay.toDouble()) * 100.0
        } else 0.0

        val dealsSummary = DealsSummaryReport(
            totalActiveCapitalOutlay = totalActiveCapitalOutlay,
            totalHistoricalCapitalOutlay = totalHistoricalCapitalOutlay,
            totalRecovered = totalRecoveredDeals,
            totalNetProfit = totalNetProfitDeals,
            totalInvestmentOutlay = totalInvestmentOutlay,
            totalLendingOutlay = totalLendingOutlay,
            totalLendingOutstanding = totalLendingOutstanding,
            overallRoi = overallDealRoi,
            investmentRatio = investmentRatio,
            activeDealsCount = activeDeals.size,
            completedDealsCount = completedDeals.size,
        )

        // Vòng quay tiết kiệm (Saving Spin)
        val completedSpins = spinSessions.filter { it.status == com.finlux.app.domain.model.SavingSpinStatus.COMPLETED }
        val totalSavedSpin = completedSpins.sumOf { it.selectedAmount?.value ?: 0L }
        val skippedSpins = spinSessions.count { it.status == com.finlux.app.domain.model.SavingSpinStatus.SKIPPED }
        val destMap = spinDestinations.associate { it.id to it.name }
        val destBreakdown = completedSpins
            .filter { it.destinationId != null }
            .groupBy { requireNotNull(it.destinationId) }
            .map { (id, items) ->
                com.finlux.app.domain.model.SavingSpinDestinationTotal(
                    destinationId = id,
                    destinationName = destMap[id] ?: "Nơi tiết kiệm",
                    amount = Money(items.sumOf { it.selectedAmount?.value ?: 0L }),
                )
            }
            .sortedByDescending { it.amount.value }

        val spinStreakResult = calculateSavingSpinStreakUseCase(
            config = SavingSpinConfig(),
            sessions = spinSessions,
            now = clock.now(),
            salaryCycleConfig = salaryConfig,
        )
        val spinStreak = spinStreakResult.currentStreak
        val spinCompletionRate = if (spinSessions.isEmpty()) 0 else (completedSpins.size * 100 / spinSessions.size)

        val savingSpinSummary = SavingSpinSummaryReport(
            totalSaved = totalSavedSpin,
            completedCount = completedSpins.size,
            skippedCount = skippedSpins,
            currentStreak = spinStreak,
            completionRate = spinCompletionRate,
            destinationBreakdown = destBreakdown,
        )

        // Ngân sách (Budgets)
        val byCategoryName = categories.associateBy { it.name.lowercase().trim() }
        val spentByCategoryId = expenseItems
            .groupBy { tx -> tx.categoryId?.takeIf { it.isNotBlank() } }
            .filterKeys { it != null }
            .mapKeys { it.key!! }
            .mapValues { (_, txs) -> txs.sumOf { it.amount.value } }

        val spentByCategoryName = expenseItems
            .filter { !it.categoryId.isNullOrBlank() }
            .groupBy { tx -> tx.categoryId!!.lowercase().trim() }
            .mapValues { (_, txs) -> txs.sumOf { it.amount.value } }

        val budgetReportItems = budgets.map { b ->
            val cat = categoryMap[b.categoryId] ?: byCategoryName[b.categoryId.lowercase().trim()]
            val targetIds = listOfNotNull(b.categoryId, cat?.id).distinct()
            val targetNames = listOfNotNull(b.categoryId.lowercase().trim(), cat?.name?.lowercase()?.trim()).distinct()

            val spentFromIds = targetIds.sumOf { id -> spentByCategoryId[id] ?: 0L }
            val spentFromNames = targetNames
                .filterNot { name -> targetIds.any { id -> id.lowercase().trim() == name } }
                .sumOf { name -> spentByCategoryName[name] ?: 0L }

            val dynamicSpent = spentFromIds + spentFromNames
            val lim = b.limitAmount.value
            val pct = if (lim > 0) (dynamicSpent.toFloat() / lim.toFloat()) else 0f
            BudgetReportItem(
                budget = b.copy(spentAmount = Money(dynamicSpent)),
                category = cat,
                limit = lim,
                spent = dynamicSpent,
                percent = pct,
                remaining = (lim - dynamicSpent).coerceAtLeast(0L),
                isOverBudget = dynamicSpent > lim,
            )
        }.sortedByDescending { it.percent }

        val totalBudgetLimit = budgetReportItems.sumOf { it.limit }
        val totalBudgetSpent = budgetReportItems.sumOf { it.spent }
        val totalBudgetRemaining = (totalBudgetLimit - totalBudgetSpent).coerceAtLeast(0L)
        val budgetUsagePercent = if (totalBudgetLimit > 0) ((totalBudgetSpent.toFloat() / totalBudgetLimit.toFloat()) * 100).roundToInt() else 0
        val overBudgetCount = budgetReportItems.count { it.isOverBudget }

        // Tài sản & Ví (Wallets & Net Worth)
        val assetWallets = wallets.assetWallets()
        val totalAssets = assetWallets.sumOf { it.balance.value }
        val totalNetWorth = totalAssets - totalDebtRemaining
        // True Net Worth = tài sản ví + tiền đang giữ trong Mục tiêu + vốn Deal còn thu hồi - dư nợ.
        // Goal deposits reduce wallet.balance, so savedAmount must stay inside assets to avoid understating net worth.
        val trueNetWorth = totalAssets + totalGoalSaved + totalActiveCapitalOutlay - totalDebtRemaining

        val assetsByType = assetWallets.groupBy { it.type }.mapValues { (_, list) -> list.sumOf { it.balance.value } }

        // Tính toán chi tiết chi tiêu của từng ví dựa trên toàn bộ giao dịch trong kỳ (allPeriodTransactions)
        val walletSpendingDetails = assetWallets.map { w ->
            val wTxList = allPeriodTransactions.filter { it.walletId == w.id }
            val wIncomeItems = wTxList.filter {
                it.type == TransactionType.INCOME && it.dealFlowType != com.finlux.app.domain.model.DealFlowType.PRINCIPAL_RECOVERY
            }
            val wExpenseItems = wTxList.filter {
                it.type == TransactionType.EXPENSE && it.dealFlowType != com.finlux.app.domain.model.DealFlowType.OUTLAY_CAPITAL
            }
            val wTransferInItems = wTxList.filter { it.type == TransactionType.TRANSFER_IN }
            val wTransferOutItems = wTxList.filter { it.type == TransactionType.TRANSFER_OUT }

            val wIncome = wIncomeItems.sumOf { it.amount.value }
            val wExpense = wExpenseItems.sumOf { it.amount.value }
            val wTransferIn = wTransferInItems.sumOf { it.amount.value }
            val wTransferOut = wTransferOutItems.sumOf { it.amount.value }
            val wPctAssets = if (totalAssets > 0) (w.balance.value.toFloat() / totalAssets.toFloat()) else 0f
            val wExpenseShare = if (totalAllPeriodExpense > 0) (wExpense.toFloat() / totalAllPeriodExpense.toFloat()) else 0f

            val wExpensesByCategory = wExpenseItems.groupBy { it.categoryId }.map { (id, items) ->
                val sum = items.sumOf { it.amount.value }
                val pct = if (wExpense > 0) (sum.toFloat() / wExpense.toFloat()) else 0f
                CategoryExpense(categoryMap[id], sum, pct, items.size)
            }.sortedByDescending(CategoryExpense::amount)

            val wIncomeByCategory = wIncomeItems.groupBy { it.categoryId }.map { (id, items) ->
                val sum = items.sumOf { it.amount.value }
                val pct = if (wIncome > 0) (sum.toFloat() / wIncome.toFloat()) else 0f
                CategoryExpense(categoryMap[id], sum, pct, items.size)
            }.sortedByDescending(CategoryExpense::amount)

            WalletSpendingDetail(
                wallet = w,
                balance = w.balance.value,
                percentageOfTotalAssets = wPctAssets,
                incomeInPeriod = wIncome,
                expenseInPeriod = wExpense,
                transferInInPeriod = wTransferIn,
                transferOutInPeriod = wTransferOut,
                netCashflowInPeriod = wIncome - wExpense,
                expenseShareOfTotal = wExpenseShare,
                transactionCount = wTxList.size,
                expensesByCategory = wExpensesByCategory,
                incomeByCategory = wIncomeByCategory,
                transactions = wTxList.sortedByDescending { it.date },
            )
        }.sortedByDescending { it.expenseInPeriod }

        val walletReportItems = walletSpendingDetails.map { detail ->
            WalletReportItem(
                wallet = detail.wallet,
                balance = detail.balance,
                percentageOfTotal = detail.percentageOfTotalAssets,
                incomeInPeriod = detail.incomeInPeriod,
                expenseInPeriod = detail.expenseInPeriod,
                transferInInPeriod = detail.transferInInPeriod,
                transferOutInPeriod = detail.transferOutInPeriod,
                expenseShareOfTotal = detail.expenseShareOfTotal,
                spendingDetail = detail,
            )
        }

        val walletActivity = walletSpendingDetails.map { detail ->
            WalletActivity(detail.wallet, detail.incomeInPeriod, detail.expenseInPeriod, detail.incomeInPeriod + detail.expenseInPeriod)
        }.sortedByDescending { it.total }

        val selectedWalletSpendingDetail = walletSpendingDetails.find { it.wallet.id == selectedWalletId }

        // Tính trung bình ngày dựa trên số ngày thực tế đã trôi qua trong kỳ (đến ngày hôm nay)
        val daysElapsedInPeriod = maxOf(1, ChronoUnit.DAYS.between(range.start, effectiveEndDate).toInt() + 1)
        val avgExpense = if (daysElapsedInPeriod > 0) expense / daysElapsedInPeriod else 0
        val avgIncome = if (daysElapsedInPeriod > 0) income / daysElapsedInPeriod else 0

        // Daily Statements & Balance Reconciliation
        val dailyStatementsWallets = if (selectedWalletId != null) {
            wallets.filter { it.id == selectedWalletId }
        } else {
            wallets
        }
        val dailyStatements = dailyStatementCalculator.calculateDailyStatements(
            wallets = dailyStatementsWallets,
            allTransactions = if (selectedWalletId != null) transactions.filter { it.walletId == selectedWalletId } else transactions,
            deals = if (selectedWalletId != null) emptyList() else deals,
            startDate = range.start,
            endDate = effectiveEndDate,
            zone = zone,
        )
        val todayStatement = dailyStatements.find { it.date == today } ?: dailyStatements.lastOrNull()
        val cumulativeMetrics = dailyStatementCalculator.calculateCumulativeMetrics(
            dailyStatements = dailyStatements,
            asOfDate = if (range.end.isBefore(today)) range.end else today,
        )
        val yesterdayComparison = dailyStatementCalculator.calculateYesterdayComparison(
            dailyStatements = dailyStatements,
            today = today,
        )
        val openingBalance = dailyStatements.firstOrNull()?.openingBalance ?: (if (selectedWalletId != null) (wallets.find { it.id == selectedWalletId }?.balance?.value ?: 0L) else totalAssets)
        val closingBalance = dailyStatements.lastOrNull()?.closingBalance ?: (if (selectedWalletId != null) (wallets.find { it.id == selectedWalletId }?.balance?.value ?: 0L) else totalAssets)

        val targetWalletIds = if (selectedWalletId != null) setOf(selectedWalletId) else assetWallets.map { it.id }.toSet()
        val cashMovementStatement = dailyStatementCalculator.calculateCashMovement(
            openingBalance = openingBalance,
            transactionsInPeriod = filtered,
            deals = if (selectedWalletId != null) emptyList() else deals,
            targetWalletIds = targetWalletIds,
        )

        val availablePeriods = if (salaryConfig.enabled) {
            listOf(
                ReportPeriod.TODAY,
                ReportPeriod.YESTERDAY,
                ReportPeriod.DAY,
                ReportPeriod.WEEK,
                ReportPeriod.LAST_7_DAYS,
                ReportPeriod.SALARY_CYCLE,
                ReportPeriod.MONTH,
                ReportPeriod.QUARTER,
                ReportPeriod.YEAR,
                ReportPeriod.CUSTOM,
            )
        } else {
            listOf(
                ReportPeriod.TODAY,
                ReportPeriod.YESTERDAY,
                ReportPeriod.DAY,
                ReportPeriod.WEEK,
                ReportPeriod.LAST_7_DAYS,
                ReportPeriod.MONTH,
                ReportPeriod.QUARTER,
                ReportPeriod.YEAR,
                ReportPeriod.CUSTOM,
            )
        }

        val effectivePeriod = if (requestedPeriod == ReportPeriod.SALARY_CYCLE && !salaryConfig.enabled) {
            ReportPeriod.MONTH
        } else {
            requestedPeriod
        }

        val currentNet = income - expense
        val previousNet = previousIncome - previousExpense
        val netChange = currentNet - previousNet
        val topExpense = byCategoryExpense.firstOrNull()
        val trendInsights = buildList {
            add(
                when {
                    previousNet == 0L && currentNet > 0L -> "Dòng tiền kỳ này dương ${currentNet} đ; kỳ trước chưa có số dư ròng để so sánh."
                    netChange > 0L -> "Dòng tiền ròng tăng ${netChange} đ so với kỳ trước."
                    netChange < 0L -> "Dòng tiền ròng giảm ${-netChange} đ so với kỳ trước."
                    else -> "Dòng tiền ròng không thay đổi so với kỳ trước."
                },
            )
            topExpense?.let {
                add("${it.category?.name ?: "Chưa phân loại"} là nhóm chi lớn nhất, chiếm ${(it.percentage * 100).roundToInt()}% tổng chi.")
            }
            if (goalContributionInPeriod > 0L) {
                add("Đã phân bổ ròng ${goalContributionInPeriod} đ vào mục tiêu tài chính trong kỳ.")
            }
            if (totalNetProfitDeals > 0L) {
                add("Đã thu về ${totalNetProfitDeals} đ lợi nhuận ròng & lãi từ các thương vụ / cho vay.")
            }
            if (totalSavedSpin > 0L) {
                add("Đã tích lũy ${totalSavedSpin} đ qua Vòng quay tiết kiệm (chuỗi ${spinStreak} ngày).")
            }
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
            unspentCashFlow = income - expense,
            savingsRatePercent = savingsRatePercent,
            goalContributionInPeriod = goalContributionInPeriod,
            trendInsights = trendInsights,
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
            savingSpinSummary = savingSpinSummary,
            // Deals & Lending
            deals = deals,
            dealReportItems = dealReportItems,
            dealsSummary = dealsSummary,
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
            walletSpendingDetails = walletSpendingDetails,
            selectedWalletId = selectedWalletId,
            selectedWalletSpendingDetail = selectedWalletSpendingDetail,
            totalAssets = totalAssets,
            totalNetWorth = totalNetWorth,
            trueNetWorth = trueNetWorth,
            assetsByType = assetsByType,
            isSalaryCycleEnabled = salaryConfig.enabled,
            availablePeriods = availablePeriods,
            // Reporting 2.0 Foundation
            dailyStatements = dailyStatements,
            todayStatement = todayStatement,
            cumulativeMetrics = cumulativeMetrics,
            yesterdayComparison = yesterdayComparison,
            cashMovementStatement = cashMovementStatement,
            openingBalance = openingBalance,
            closingBalance = closingBalance,
            totalTransferIn = totalTransferIn,
            totalTransferOut = totalTransferOut,
        )
    }
}
