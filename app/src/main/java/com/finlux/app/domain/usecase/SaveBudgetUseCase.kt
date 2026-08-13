package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.repository.BudgetRepository
import javax.inject.Inject

class SaveBudgetUseCase @Inject constructor(private val repository: BudgetRepository) {
    suspend operator fun invoke(budget: Budget): AppResult<String> {
        if (budget.categoryId.isBlank()) return AppResult.Error("Vui lòng chọn danh mục")
        if (budget.limitAmount.value <= 0L) return AppResult.Error("Hạn mức phải lớn hơn 0")
        return repository.upsertBudget(budget)
    }
}
