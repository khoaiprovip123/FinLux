package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.core.time.FinanceTime
import com.finlux.app.data.local.notification.SystemNotificationHelper
import com.finlux.app.domain.model.AppNotification
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.NotificationType
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.NotificationRepository
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import kotlinx.coroutines.flow.firstOrNull
import java.text.NumberFormat
import java.time.Instant
import java.util.Locale
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val budgetRepository: BudgetRepository? = null,
    private val notificationRepository: NotificationRepository? = null,
    private val categoryRepository: CategoryRepository? = null,
    private val salaryCycleRepository: SalaryCycleRepository? = null,
    private val financialPeriodResolver: FinancialPeriodResolver? = null,
    private val systemNotificationHelper: SystemNotificationHelper? = null,
) {
    suspend operator fun invoke(transaction: FinanceTransaction): AppResult<String> {
        val validation = validateTransaction(transaction)
        if (validation is AppResult.Error) return validation

        if (transaction.type == TransactionType.EXPENSE) {
            val wallets = walletRepository.observeWallets().firstOrNull().orEmpty()
            val wallet = wallets.firstOrNull { it.id == transaction.walletId }
            if (wallet != null && wallet.type != WalletType.CARD) {
                if (wallet.balance.value < transaction.amount.value) {
                    return AppResult.Error("Số dư ví [${wallet.name}] không đủ để thực hiện chi tiêu")
                }
            }
        }

        val result = repository.addWithBalanceUpdate(transaction)
        if (result is AppResult.Success && transaction.type == TransactionType.EXPENSE && !transaction.categoryId.isNullOrBlank()) {
            checkAndTriggerBudgetAlert(transaction)
        }
        return result
    }

    private suspend fun checkAndTriggerBudgetAlert(transaction: FinanceTransaction) {
        runCatching {
            val catId = transaction.categoryId ?: return
            val budgetRepo = budgetRepository ?: return
            val notiRepo = notificationRepository ?: return

            val config = salaryCycleRepository?.observeConfig()?.firstOrNull() ?: SalaryCycleConfig()
            val period = financialPeriodResolver?.resolvePeriodContaining(transaction.date, config)
            val periodKey = period?.key ?: "month:${FinanceTime.financialMonth(transaction.date)}"

            val budgets = budgetRepo.observeBudgets(periodKey).firstOrNull().orEmpty()
            val budget = budgets.firstOrNull { it.categoryId == catId } ?: return
            if (budget.limitAmount.value <= 0) return

            val newSpent = budget.spentAmount.value + transaction.amount.value
            val limit = budget.limitAmount.value
            val reached100 = newSpent >= limit
            val reached80 = newSpent >= (limit * 80) / 100

            val categories = categoryRepository?.observeCategories()?.firstOrNull().orEmpty()
            val categoryName = categories.firstOrNull { it.id == catId }?.name ?: "Danh mục"

            if (reached100 && !budget.notified100) {
                val title = "Đã vượt ngân sách"
                val body = "Danh mục [$categoryName] đã chi ${formatVnd(newSpent)} trên hạn mức ${formatVnd(limit)}."

                systemNotificationHelper?.postBudgetAlertNotification(
                    categoryId = catId,
                    categoryName = categoryName,
                    spentAmount = newSpent,
                    limitAmount = limit,
                    isExceeded = true,
                )

                notiRepo.saveNotification(
                    AppNotification(
                        id = "budget_${catId}_${periodKey}_100",
                        title = title,
                        body = body,
                        type = NotificationType.BUDGET_ALERT,
                        amount = Money(newSpent),
                        categoryId = catId,
                        targetRoute = "budget",
                        timestamp = Instant.now(),
                        isRead = false,
                        isPaid = false,
                    )
                )

                budgetRepo.upsertBudget(
                    budget.copy(
                        spentAmount = Money(newSpent),
                        notified80 = true,
                        notified100 = true,
                    )
                )
            } else if (reached80 && !budget.notified80) {
                val percent = (newSpent * 100) / limit
                val title = "Sắp chạm hạn mức ngân sách"
                val body = "Danh mục [$categoryName] đã sử dụng ${percent}% hạn mức (${formatVnd(newSpent)} / ${formatVnd(limit)})."

                systemNotificationHelper?.postBudgetAlertNotification(
                    categoryId = catId,
                    categoryName = categoryName,
                    spentAmount = newSpent,
                    limitAmount = limit,
                    isExceeded = false,
                )

                notiRepo.saveNotification(
                    AppNotification(
                        id = "budget_${catId}_${periodKey}_80",
                        title = title,
                        body = body,
                        type = NotificationType.BUDGET_ALERT,
                        amount = Money(newSpent),
                        categoryId = catId,
                        targetRoute = "budget",
                        timestamp = Instant.now(),
                        isRead = false,
                        isPaid = false,
                    )
                )

                budgetRepo.upsertBudget(
                    budget.copy(
                        spentAmount = Money(newSpent),
                        notified80 = true,
                    )
                )
            }
        }
    }

    private fun formatVnd(amount: Long): String =
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")).format(amount)
}
