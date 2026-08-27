package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.data.local.notification.SystemNotificationHelper
import com.finlux.app.domain.model.AppNotification
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.NotificationType
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.firstOrNull
import java.text.NumberFormat
import java.time.Instant
import java.util.Locale
import javax.inject.Inject

class SaveBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository,
    private val categoryRepository: CategoryRepository? = null,
    private val notificationRepository: NotificationRepository? = null,
    private val systemNotificationHelper: SystemNotificationHelper? = null,
) {
    suspend operator fun invoke(budget: Budget): AppResult<String> {
        if (budget.categoryId.isBlank()) return AppResult.Error("Vui lòng chọn danh mục")
        if (budget.limitAmount.value <= 0L) return AppResult.Error("Hạn mức phải lớn hơn 0")

        val spent = budget.spentAmount.value
        val limit = budget.limitAmount.value
        val reached100 = spent >= limit
        val reached80 = spent >= (limit * 80L) / 100L

        val normalizedBudget = budget.copy(
            notified80 = reached80,
            notified100 = reached100,
        )

        // Trigger immediate notification if newly saved budget limit causes threshold to be reached immediately
        if (reached100 && !budget.notified100) {
            triggerAlert(normalizedBudget, isExceeded = true)
        } else if (reached80 && !budget.notified80 && !reached100) {
            triggerAlert(normalizedBudget, isExceeded = false)
        }

        return repository.upsertBudget(normalizedBudget)
    }

    private suspend fun triggerAlert(budget: Budget, isExceeded: Boolean) {
        val notiRepo = notificationRepository ?: return
        val categories = categoryRepository?.observeCategories()?.firstOrNull().orEmpty()
        val categoryName = categories.firstOrNull { it.id == budget.categoryId }?.name ?: "Danh mục"
        val spent = budget.spentAmount.value
        val limit = budget.limitAmount.value
        val percent = if (limit > 0) (spent * 100) / limit else 100

        val title = if (isExceeded) "Đã vượt ngân sách" else "Sắp chạm hạn mức ngân sách"
        val body = if (isExceeded) {
            "Danh mục [$categoryName] đã chi ${formatVnd(spent)} trên hạn mức ${formatVnd(limit)} ($percent%)."
        } else {
            "Danh mục [$categoryName] đã sử dụng $percent% ngân sách (${formatVnd(spent)} / ${formatVnd(limit)})."
        }

        systemNotificationHelper?.postBudgetAlertNotification(
            categoryId = budget.categoryId,
            categoryName = categoryName,
            spentAmount = spent,
            limitAmount = limit,
            isExceeded = isExceeded,
        )

        notiRepo.saveNotification(
            AppNotification(
                id = "budget_${budget.categoryId}_${budget.periodKey}_${if (isExceeded) 100 else 80}",
                title = title,
                body = body,
                type = NotificationType.BUDGET_ALERT,
                amount = Money(spent),
                categoryId = budget.categoryId,
                targetRoute = "budget",
                timestamp = Instant.now(),
                isRead = false,
                isPaid = false,
            )
        )
    }

    private fun formatVnd(amount: Long): String =
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")).format(amount)
}
