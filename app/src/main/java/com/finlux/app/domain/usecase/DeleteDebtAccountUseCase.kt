package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.repository.DebtRepository
import javax.inject.Inject

class DeleteDebtAccountUseCase @Inject constructor(
    private val repository: DebtRepository,
    private val syncDebtReminderUseCase: SyncDebtReminderUseCase,
) {
    suspend operator fun invoke(debt: DebtAccount): AppResult<Unit> {
        if (debt.id.isBlank()) {
            return AppResult.Error("ID khoản nợ không hợp lệ")
        }
        if (debt.remainingBalance.value > 0L || !debt.isSettled) {
            return AppResult.Error("Khoản nợ vẫn còn dư nợ. Hãy tất toán trước khi xóa.")
        }
        val result = repository.deleteDebt(debt)
        if (result is AppResult.Success) {
            syncDebtReminderUseCase.removeDebtReminder(debt.id)
        }
        return result
    }
}
