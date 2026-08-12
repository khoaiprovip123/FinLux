package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.Budget
import javax.inject.Inject

enum class BudgetLevel { SAFE, WARNING, EXCEEDED }

data class BudgetStatus(val progress: Float, val level: BudgetLevel)

/** Implements the 80% and 100% UI thresholds from BR-09. */
class GetBudgetStatusUseCase @Inject constructor() {
    operator fun invoke(budget: Budget): BudgetStatus {
        val limit = budget.limitAmount.value
        val progress = if (limit == 0L) 0f else budget.spentAmount.value.toFloat() / limit
        val level = when {
            progress >= 1f -> BudgetLevel.EXCEEDED
            progress >= .8f -> BudgetLevel.WARNING
            else -> BudgetLevel.SAFE
        }
        return BudgetStatus(progress = progress, level = level)
    }
}
