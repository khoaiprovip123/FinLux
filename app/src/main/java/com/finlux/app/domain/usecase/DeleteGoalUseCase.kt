package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinancialGoal
import com.finlux.app.domain.repository.GoalRepository
import javax.inject.Inject

class DeleteGoalUseCase @Inject constructor(private val repository: GoalRepository) {
    suspend operator fun invoke(goal: FinancialGoal): AppResult<Unit> {
        if (goal.id.isBlank()) {
            return AppResult.Error("Mã mục tiêu không hợp lệ")
        }
        if (goal.savedAmount.value > 0L) {
            return AppResult.Error("Không thể xóa mục tiêu khi vẫn còn số dư tích lũy (${goal.savedAmount.value} ₫). Vui lòng rút hết tiền về ví trước khi xóa.")
        }
        return repository.deleteGoal(goal)
    }
}
