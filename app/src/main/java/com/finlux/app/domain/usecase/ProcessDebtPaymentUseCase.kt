package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.repository.DebtRepository
import java.time.Instant
import javax.inject.Inject

class ProcessDebtPaymentUseCase @Inject constructor(
    private val repository: DebtRepository,
) {
    suspend operator fun invoke(
        debtId: String,
        walletId: String,
        amount: Long,
        principalPaid: Long,
        interestPaid: Long = 0L,
        note: String = "",
        paymentDate: Instant = Instant.now(),
    ): AppResult<Unit> {
        if (debtId.isBlank()) {
            return AppResult.Error("Khoản nợ không hợp lệ")
        }
        if (walletId.isBlank()) {
            return AppResult.Error("Vui lòng chọn ví thanh toán")
        }
        if (amount <= 0L) {
            return AppResult.Error("Số tiền thanh toán phải lớn hơn 0")
        }
        if (principalPaid < 0L || interestPaid < 0L) {
            return AppResult.Error("Tiền gốc hoặc tiền lãi không được âm")
        }
        if (principalPaid + interestPaid != amount) {
            return AppResult.Error("Tổng tiền gốc và tiền lãi phải bằng tổng số tiền thanh toán")
        }

        return repository.processPayment(
            debtId = debtId,
            walletId = walletId,
            amount = amount,
            principalPaid = principalPaid,
            interestPaid = interestPaid,
            note = note,
            paymentDate = paymentDate,
        )
    }
}
