package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.repository.TransactionRepository
import javax.inject.Inject

class EditTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke(
        original: FinanceTransaction,
        updated: FinanceTransaction,
    ): AppResult<Unit> {
        val validation = validateTransaction(updated)
        if (validation is AppResult.Error) return validation
        if (original.id.isBlank() || updated.id != original.id) {
            return AppResult.Error("Giao dịch cần sửa không hợp lệ")
        }
        return repository.editWithBalanceUpdate(original, updated)
    }
}
