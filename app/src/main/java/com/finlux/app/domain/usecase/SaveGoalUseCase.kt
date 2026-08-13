package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinancialGoal
import com.finlux.app.domain.repository.GoalRepository
import javax.inject.Inject

class SaveGoalUseCase @Inject constructor(private val repository: GoalRepository) {
    suspend operator fun invoke(goal: FinancialGoal): AppResult<String> {
        if (goal.name.isBlank()) return AppResult.Error("Vui lòng nhập tên mục tiêu")
        if (goal.targetAmount.value <= 0L) return AppResult.Error("Số tiền mục tiêu phải lớn hơn 0")
        if (goal.monthlyContribution.value < 0L) return AppResult.Error("Số tiền tích lũy không hợp lệ")
        if (goal.category.isBlank()) return AppResult.Error("Vui lòng chọn danh mục")
        return repository.upsertGoal(goal.copy(name = goal.name.trim()))
    }
}
