package com.finlux.app.domain.repository

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.DashboardSummary
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.FinancialGoal
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.Reminder
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.YearMonth

interface TransactionRepository {
    fun observeRecent(limit: Int = 20): Flow<List<FinanceTransaction>>

    /** Observes all transactions for the given [month] in real-time. Used by HomeViewModel
     *  to compute dynamic budget.spentAmount per category without relying on stored Firestore field. */
    fun observeMonth(month: YearMonth): Flow<List<FinanceTransaction>>

    /** Creates the transaction and changes wallet.balance in one Firestore transaction (BR-14). */
    suspend fun addWithBalanceUpdate(transaction: FinanceTransaction): AppResult<String>

    /** Reverses [original], applies [updated], and writes both wallet balances atomically (BR-06). */
    suspend fun editWithBalanceUpdate(
        original: FinanceTransaction,
        updated: FinanceTransaction,
    ): AppResult<Unit>

    /** Deletes [transaction] and restores its wallet balance atomically (BR-06). */
    suspend fun deleteWithBalanceUpdate(transaction: FinanceTransaction): AppResult<Unit>

    /** Creates the transfer pair and changes both wallet balances atomically (BR-07/BR-14). */
    suspend fun transferBetweenWallets(
        sourceWalletId: String,
        destinationWalletId: String,
        amount: Long,
        note: String,
        date: Instant,
    ): AppResult<Unit>
}

interface WalletRepository {
    fun observeWallets(): Flow<List<Wallet>>
    suspend fun upsertWallet(wallet: Wallet): AppResult<String>
    suspend fun deleteWallet(wallet: Wallet): AppResult<Unit>
}

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>
    suspend fun upsertCategory(category: Category): AppResult<String>
    suspend fun deleteCategory(category: Category): AppResult<Unit>
}

interface BudgetRepository {
    fun observeBudgets(month: YearMonth): Flow<List<Budget>>
    suspend fun upsertBudget(budget: Budget): AppResult<String>
    suspend fun deleteBudget(budget: Budget): AppResult<Unit>
}

interface ReminderRepository {
    fun observeReminders(): Flow<List<Reminder>>
    suspend fun upsertReminder(reminder: Reminder): AppResult<String>
    suspend fun deleteReminder(reminder: Reminder): AppResult<Unit>
}

interface GoalRepository {
    fun observeGoals(): Flow<List<FinancialGoal>>
    suspend fun upsertGoal(goal: FinancialGoal): AppResult<String>
    suspend fun deleteGoal(goal: FinancialGoal): AppResult<Unit>
}

interface ReceiptStorageRepository {
    /** Returns a durable URL when Firebase is active, or the local URI in demo mode. */
    suspend fun uploadReceipt(localUri: String): AppResult<String>
}

interface ReminderScheduler {
    fun schedule(reminder: Reminder)
    fun cancel(reminderId: String)
}

interface DashboardRepository {
    fun observeCurrentMonthSummary(): Flow<DashboardSummary>
}
