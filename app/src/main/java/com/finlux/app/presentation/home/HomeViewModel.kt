package com.finlux.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.DashboardSummary
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.UserProfile
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.repository.AuthRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.DashboardRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import java.time.YearMonth

data class HomeUiState(
    val user: UserProfile? = null,
    val summary: DashboardSummary = DashboardSummary(),
    val wallets: List<Wallet> = emptyList(),
    val transactions: List<FinanceTransaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val budgetRemaining: Long = 0L,
    val budgetRemainingPercent: Int = 0,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    authRepository: AuthRepository,
    dashboardRepository: DashboardRepository,
    walletRepository: WalletRepository,
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    budgetRepository: BudgetRepository,
) : ViewModel() {
    private val summaryAndBudget = combine(
        dashboardRepository.observeCurrentMonthSummary(),
        budgetRepository.observeBudgets(YearMonth.now()),
    ) { summary, budgets ->
        val limit = budgets.sumOf { it.limitAmount.value }
        val spent = budgets.sumOf { it.spentAmount.value }
        val remaining = limit - spent
        val percent = if (limit <= 0L) 0 else ((remaining.coerceAtLeast(0L) * 100L) / limit).toInt()
        Triple(summary, remaining, percent)
    }
    val state = combine(
        authRepository.currentUser,
        summaryAndBudget,
        walletRepository.observeWallets(),
        transactionRepository.observeRecent(),
        categoryRepository.observeCategories(),
    ) { user, financial, wallets, transactions, categories ->
        HomeUiState(
            user = user,
            summary = financial.first,
            wallets = wallets,
            transactions = transactions,
            categories = categories,
            budgetRemaining = financial.second,
            budgetRemainingPercent = financial.third,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
