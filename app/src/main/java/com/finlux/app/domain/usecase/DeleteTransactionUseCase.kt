package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke(transaction: FinanceTransaction): AppResult<Unit> {
        if (transaction.id.isBlank()) return AppResult.Error("Giao dịch cần xóa không hợp lệ")
        if (!transaction.goalId.isNullOrBlank()) {
            return AppResult.Error(
                "Không thể xóa trực tiếp giao dịch mục tiêu. Hãy điều chỉnh trong mục Tiết kiệm & Mục tiêu."
            )
        }
        if (!transaction.debtId.isNullOrBlank()) {
            return AppResult.Error(
                "Không thể xóa trực tiếp giao dịch trả nợ. Hãy điều chỉnh trong mục Nợ & Tín dụng."
            )
        }
        return repository.deleteWithBalanceUpdate(transaction)
    }
}
