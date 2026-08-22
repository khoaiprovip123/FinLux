package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.repository.DebtRepository
import javax.inject.Inject

class SaveDebtAccountUseCase @Inject constructor(
    private val repository: DebtRepository,
) {
    suspend operator fun invoke(debt: DebtAccount): AppResult<String> {
        if (debt.name.isBlank()) {
            return AppResult.Error("Tên khoản nợ không được để trống")
        }
        if (debt.totalAmount.value <= 0L) {
            return AppResult.Error("Tổng hạn mức / số tiền vay phải lớn hơn 0")
        }
        if (debt.remainingBalance.value < 0L) {
            return AppResult.Error("Dư nợ hiện tại không hợp lệ")
        }
        if (debt.interestRateApr < 0.0 || debt.interestRateApr > 100.0) {
            return AppResult.Error("Lãi suất APR phải từ 0% đến 100%")
        }
        if (debt.dueDate !in 1..31) {
            return AppResult.Error("Ngày đến hạn phải từ 1 đến 31")
        }

        return repository.upsertDebt(debt)
    }
}
