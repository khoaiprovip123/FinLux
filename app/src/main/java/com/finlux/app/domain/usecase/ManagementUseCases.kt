package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.FinancialGoal
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.GoalRepository
import com.finlux.app.domain.repository.ReminderRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.repository.ReminderScheduler
import java.time.Instant
import javax.inject.Inject

class SaveWalletUseCase @Inject constructor(private val repository: WalletRepository) {
    suspend operator fun invoke(wallet: Wallet): AppResult<String> =
        if (wallet.name.isBlank()) AppResult.Error("Vui lòng nhập tên ví") else repository.upsertWallet(wallet)
}

class DeleteWalletUseCase @Inject constructor(private val repository: WalletRepository) {
    suspend operator fun invoke(wallet: Wallet): AppResult<Unit> = repository.deleteWallet(wallet)
}

class TransferMoneyUseCase @Inject constructor(private val repository: TransactionRepository) {
    suspend operator fun invoke(sourceId: String, destinationId: String, amount: Long, note: String): AppResult<Unit> {
        if (sourceId.isBlank() || destinationId.isBlank()) return AppResult.Error("Vui lòng chọn đủ hai ví")
        if (sourceId == destinationId) return AppResult.Error("Hai ví phải khác nhau")
        if (amount <= 0L) return AppResult.Error("Số tiền phải lớn hơn 0")
        return repository.transferBetweenWallets(sourceId, destinationId, amount, note.trim(), Instant.now())
    }
}

class SaveCategoryUseCase @Inject constructor(private val repository: CategoryRepository) {
    suspend operator fun invoke(category: Category): AppResult<String> =
        if (category.name.isBlank()) AppResult.Error("Vui lòng nhập tên danh mục") else repository.upsertCategory(category)
}

class DeleteCategoryUseCase @Inject constructor(private val repository: CategoryRepository) {
    suspend operator fun invoke(category: Category): AppResult<Unit> = repository.deleteCategory(category)
}

class SaveBudgetUseCase @Inject constructor(private val repository: BudgetRepository) {
    suspend operator fun invoke(budget: Budget): AppResult<String> {
        if (budget.categoryId.isBlank()) return AppResult.Error("Vui lòng chọn danh mục")
        if (budget.limitAmount.value <= 0L) return AppResult.Error("Hạn mức phải lớn hơn 0")
        return repository.upsertBudget(budget)
    }
}

class DeleteBudgetUseCase @Inject constructor(private val repository: BudgetRepository) {
    suspend operator fun invoke(budget: Budget): AppResult<Unit> = repository.deleteBudget(budget)
}

class SaveReminderUseCase @Inject constructor(
    private val repository: ReminderRepository,
    private val scheduler: ReminderScheduler,
) {
    suspend operator fun invoke(reminder: Reminder): AppResult<String> {
        if (reminder.title.isBlank()) return AppResult.Error("Vui lòng nhập tên nhắc nhở")
        if (reminder.amount.value <= 0L) return AppResult.Error("Số tiền phải lớn hơn 0")
        if (reminder.categoryId.isBlank() || reminder.walletId.isBlank()) return AppResult.Error("Vui lòng chọn danh mục và ví")
        val result = repository.upsertReminder(reminder)
        if (result is AppResult.Success) {
            val stored = reminder.copy(id = result.value)
            if (stored.enabled) scheduler.schedule(stored) else scheduler.cancel(stored.id)
        }
        return result
    }
}

class DeleteReminderUseCase @Inject constructor(
    private val repository: ReminderRepository,
    private val scheduler: ReminderScheduler,
) {
    suspend operator fun invoke(reminder: Reminder): AppResult<Unit> {
        val result = repository.deleteReminder(reminder)
        if (result is AppResult.Success) scheduler.cancel(reminder.id)
        return result
    }
}

class SaveGoalUseCase @Inject constructor(private val repository: GoalRepository) {
    suspend operator fun invoke(goal: FinancialGoal): AppResult<String> {
        if (goal.name.isBlank()) return AppResult.Error("Vui lòng nhập tên mục tiêu")
        if (goal.targetAmount.value <= 0L) return AppResult.Error("Số tiền mục tiêu phải lớn hơn 0")
        if (goal.monthlyContribution.value < 0L) return AppResult.Error("Số tiền tích lũy không hợp lệ")
        if (goal.category.isBlank()) return AppResult.Error("Vui lòng chọn danh mục")
        return repository.upsertGoal(goal.copy(name = goal.name.trim()))
    }
}

class DeleteGoalUseCase @Inject constructor(private val repository: GoalRepository) {
    suspend operator fun invoke(goal: FinancialGoal): AppResult<Unit> = repository.deleteGoal(goal)
}
