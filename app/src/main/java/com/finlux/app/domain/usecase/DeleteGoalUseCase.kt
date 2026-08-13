package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinancialGoal
import com.finlux.app.domain.repository.GoalRepository
import javax.inject.Inject

class DeleteGoalUseCase @Inject constructor(private val repository: GoalRepository) {
    suspend operator fun invoke(goal: FinancialGoal): AppResult<Unit> = repository.deleteGoal(goal)
}
