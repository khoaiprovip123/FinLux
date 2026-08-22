package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.repository.GoalRepository
import java.time.Instant
import javax.inject.Inject

class DepositToGoalUseCase @Inject constructor(
    private val goalRepository: GoalRepository,
) {
    suspend operator fun invoke(
        goalId: String,
        walletId: String,
        amount: Long,
        note: String = "",
        date: Instant = Instant.now(),
    ): AppResult<Unit> {
        if (goalId.isBlank()) return AppResult.Error("Mã mục tiêu không hợp lệ")
        if (walletId.isBlank()) return AppResult.Error("Vui lòng chọn ví nguồn")
        if (amount <= 0L) return AppResult.Error("Số tiền nạp phải lớn hơn 0")
        return goalRepository.depositToGoal(
            goalId = goalId,
            walletId = walletId,
            amount = amount,
            note = note,
            date = date,
        )
    }
}
