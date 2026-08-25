package com.finlux.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.time.FinanceClock
import com.finlux.app.core.time.FinanceTime
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.DashboardSummary
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.UserProfile
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.repository.AuthRepository
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.DashboardRepository
import com.finlux.app.domain.repository.DebtRepository
import com.finlux.app.domain.repository.NotificationRepository
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.SalaryCycleCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
    val budgetRemaining: Long = 0L,
    val budgetRemainingPercent: Int = 0,
    val unreadNotificationsCount: Int = 0,
    val salaryCycleLabel: String? = null,
)

private data class FinancialOverview(
    val summary: DashboardSummary,
    val budgetRemaining: Long,
    val budgetRemainingPercent: Int,
    val monthTransactions: List<FinanceTransaction>,
    val unreadCount: Int,
    val salaryCycleLabel: String?,
)

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
    calculator: SalaryCycleCalculator,
    clock: FinanceClock,
) : ViewModel() {
    private val currentMonth = YearMonth.now()

    private val financialOverviewFlow = combine(
        dashboardRepository.observeCurrentMonthSummary(),
        budgetRepository.observeBudgets("MONTHLY_${currentMonth}"),
        transactionRepository.observeMonth(currentMonth),
        notificationRepository.observeNotifications(),
        salaryCycleRepository.observeConfig(),
    ) { summary, budgets, monthTransactions, notifications, cycleConfig ->
        val spentByCategory = monthTransactions
            .filter { it.type == TransactionType.EXPENSE && it.categoryId != null }
            .groupBy { it.categoryId!! }
            .mapValues { (_, txs) -> txs.sumOf { it.amount.value } }
        val limit = budgets.sumOf { it.limitAmount.value }
        val spent = budgets.sumOf { spentByCategory[it.categoryId] ?: 0L }
        val remaining = limit - spent
        val percent = if (limit <= 0L) 0 else ((remaining.coerceAtLeast(0L) * 100L) / limit).toInt()
        val unread = notifications.count { !it.isRead }

        val cycleLabel = if (cycleConfig.enabled) {
            val zone = FinanceTime.zoneOf(cycleConfig.financeTimeZone)
            val cycle = calculator.cycleContaining(clock.now(), cycleConfig, zone)
            val fmt = DateTimeFormatter.ofPattern("dd/MM")
            "${cycle.start.atZone(zone).format(fmt)} - ${cycle.endExclusive.atZone(zone).minusDays(1).format(fmt)}"
        } else null

        FinancialOverview(
            summary = summary,
            budgetRemaining = remaining,
            budgetRemainingPercent = percent,
            monthTransactions = monthTransactions,
            unreadCount = unread,
            salaryCycleLabel = cycleLabel,
        )
    }

    private val assetsAndDebtsFlow = combine(
        walletRepository.observeWallets(),
        debtRepository.observeDebts(),
    ) { wallets, debts ->
        val gross = wallets.sumOf { it.balance.value }
        val totalDebt = debts.filterNot { it.isSettled }.sumOf { it.remainingBalance.value }
        val netWorth = gross - totalDebt
        Triple(wallets, debts, Triple(gross, totalDebt, netWorth))
    }

    val state = combine(
        authRepository.currentUser,
        financialOverviewFlow,
        assetsAndDebtsFlow,
        transactionRepository.observeRecent(),
        categoryRepository.observeCategories(),
    ) { user, overview, assetsAndDebts, transactions, categories ->
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
            transactions = transactions,
            monthTransactions = overview.monthTransactions,
            categories = categories,
            budgetRemaining = overview.budgetRemaining,
            budgetRemainingPercent = overview.budgetRemainingPercent,
            unreadNotificationsCount = overview.unreadCount,
            salaryCycleLabel = overview.salaryCycleLabel,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
