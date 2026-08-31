package com.finlux.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.time.FinanceClock
import com.finlux.app.core.time.FinanceTime
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.DashboardSummary
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.UserProfile
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.collapseInternalTransferPairs
import com.finlux.app.domain.model.totalAssetBalance
import com.finlux.app.domain.repository.AuthRepository
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.DashboardRepository
import com.finlux.app.domain.repository.DebtRepository
import com.finlux.app.domain.repository.NotificationRepository
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.UiPreferencesRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.FinancialPeriodResolver
import com.finlux.app.domain.usecase.SalaryCycleCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HomeUiState(
    val user: UserProfile? = null,
    val summary: DashboardSummary = DashboardSummary(),
    val wallets: List<Wallet> = emptyList(),
    val debts: List<DebtAccount> = emptyList(),
    val grossAssets: Long = 0L,
    val totalDebt: Long = 0L,
    val netWorth: Long = 0L,
    val transactions: List<FinanceTransaction> = emptyList(),
    val monthTransactions: List<FinanceTransaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val totalBudgetLimit: Long = 0L,
    val totalBudgetSpent: Long = 0L,
    val totalBudgetPercent: Int = 0,
    val budgetRemaining: Long = 0L,
    val budgetRemainingPercent: Int = 0,
    val unreadNotificationsCount: Int = 0,
    val salaryCycleLabel: String? = null,
    val showBalance: Boolean = true,
)

private data class FinancialOverview(
    val summary: DashboardSummary,
    val budgets: List<Budget>,
    val totalBudgetLimit: Long,
    val totalBudgetSpent: Long,
    val totalBudgetPercent: Int,
    val budgetRemaining: Long,
    val budgetRemainingPercent: Int,
    val monthTransactions: List<FinanceTransaction>,
    val unreadCount: Int,
    val salaryCycleLabel: String?,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    authRepository: AuthRepository,
    dashboardRepository: DashboardRepository,
    walletRepository: WalletRepository,
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    budgetRepository: BudgetRepository,
    notificationRepository: NotificationRepository,
    debtRepository: DebtRepository,
    salaryCycleRepository: SalaryCycleRepository,
    financialPeriodResolver: FinancialPeriodResolver,
    calculator: SalaryCycleCalculator,
    clock: FinanceClock,
    private val uiPreferencesRepository: UiPreferencesRepository,
) : ViewModel() {

    private val financialOverviewFlow = salaryCycleRepository.observeConfig().flatMapLatest { cycleConfig ->
        val now = clock.now()
        val zone = FinanceTime.zoneOf(cycleConfig.financeTimeZone)

        val transactionsFlow: Flow<List<FinanceTransaction>>
        val budgetsFlow: Flow<List<Budget>>
        val cycleLabel: String?
        val isSalaryCycleActive: Boolean

        if (cycleConfig.enabled) {
            val cycle = calculator.cycleContaining(now, cycleConfig, zone)
            val fmt = DateTimeFormatter.ofPattern("dd/MM")
            cycleLabel = "${cycle.start.atZone(zone).format(fmt)} - ${cycle.endExclusive.atZone(zone).minusDays(1).format(fmt)}"
            val period = financialPeriodResolver.resolvePeriodContaining(now, cycleConfig)
            transactionsFlow = transactionRepository.observePeriod(cycle.start, cycle.endExclusive)
            budgetsFlow = budgetRepository.observeBudgets(period.key)
            isSalaryCycleActive = true
        } else {
            val month = YearMonth.from(now.atZone(zone))
            val period = financialPeriodResolver.resolvePeriodContaining(now, cycleConfig)
            transactionsFlow = transactionRepository.observeMonth(month)
            budgetsFlow = budgetRepository.observeBudgets(period.key)
            cycleLabel = null
            isSalaryCycleActive = false
        }

        combine(
            dashboardRepository.observeCurrentMonthSummary(),
            budgetsFlow,
            transactionsFlow,
            notificationRepository.observeNotifications(),
        ) { defaultSummary, budgets, periodTransactions, notifications ->
            val spentByCategory = periodTransactions
                .filter {
                    it.type == TransactionType.EXPENSE &&
                    it.dealFlowType != com.finlux.app.domain.model.DealFlowType.OUTLAY_CAPITAL &&
                    it.categoryId != null
                }
                .groupBy { it.categoryId!! }
                .mapValues { (_, txs) -> txs.sumOf { it.amount.value } }
            val limit = budgets.sumOf { it.limitAmount.value }
            val spent = budgets.sumOf { spentByCategory[it.categoryId] ?: 0L }
            val remaining = (limit - spent).coerceAtLeast(0L)
            val percent = if (limit > 0) ((spent.toDouble() / limit.toDouble()) * 100).toInt() else 0
            val unread = notifications.count { !it.isRead }

            val effectiveSummary = if (isSalaryCycleActive) {
                val inc = periodTransactions
                    .filter { it.type == TransactionType.INCOME && it.dealFlowType != com.finlux.app.domain.model.DealFlowType.PRINCIPAL_RECOVERY }
                    .sumOf { it.amount.value }
                val exp = periodTransactions
                    .filter { it.type == TransactionType.EXPENSE && it.dealFlowType != com.finlux.app.domain.model.DealFlowType.OUTLAY_CAPITAL }
                    .sumOf { it.amount.value }
                DashboardSummary(
                    income = Money(inc),
                    expense = Money(exp),
                    net = inc - exp,
                )
            } else {
                defaultSummary
            }

            FinancialOverview(
                summary = effectiveSummary,
                budgets = budgets,
                totalBudgetLimit = limit,
                totalBudgetSpent = spent,
                totalBudgetPercent = percent,
                budgetRemaining = remaining,
                budgetRemainingPercent = percent,
                monthTransactions = periodTransactions,
                unreadCount = unread,
                salaryCycleLabel = cycleLabel,
            )
        }
    }

    private val assetsAndDebtsFlow = combine(
        walletRepository.observeWallets(),
        debtRepository.observeDebts(),
    ) { wallets, debts ->
        val gross = wallets.totalAssetBalance()
        val totalDebt = debts.filterNot { it.isSettled }.sumOf { it.remainingBalance.value }
        val netWorth = gross - totalDebt
        Triple(wallets, debts, Triple(gross, totalDebt, netWorth))
    }

    val state = combine(
        authRepository.currentUser,
        financialOverviewFlow,
        assetsAndDebtsFlow,
        transactionRepository.observeRecent(),
        combine(categoryRepository.observeCategories(), uiPreferencesRepository.preferences) { categories, uiPrefs ->
            categories to uiPrefs
        },
    ) { user, overview, assetsAndDebts, transactions, (categories, uiPrefs) ->
        val (wallets, debts, balances) = assetsAndDebts
        val (gross, totalDebt, netWorth) = balances

        HomeUiState(
            user = user,
            summary = overview.summary,
            wallets = wallets,
            debts = debts,
            grossAssets = gross,
            totalDebt = totalDebt,
            netWorth = netWorth,
            transactions = transactions.collapseInternalTransferPairs(),
            monthTransactions = overview.monthTransactions,
            categories = categories,
            budgets = overview.budgets,
            totalBudgetLimit = overview.totalBudgetLimit,
            totalBudgetSpent = overview.totalBudgetSpent,
            totalBudgetPercent = overview.totalBudgetPercent,
            budgetRemaining = overview.budgetRemaining,
            budgetRemainingPercent = overview.budgetRemainingPercent,
            unreadNotificationsCount = overview.unreadCount,
            salaryCycleLabel = overview.salaryCycleLabel,
            showBalance = uiPrefs.isBalanceVisible,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun toggleBalanceVisibility() {
        viewModelScope.launch {
            val current = uiPreferencesRepository.preferences.first()
            uiPreferencesRepository.setPreferences(current.copy(isBalanceVisible = !current.isBalanceVisible))
        }
    }
}
