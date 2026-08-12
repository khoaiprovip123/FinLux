package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.repository.TransactionRepository
import javax.inject.Inject

private const val MAX_AMOUNT = 999_999_999_999_999L

/** Shared validation for UC-07/08, kept in domain so every UI entry point behaves identically. */
internal fun validateTransaction(transaction: FinanceTransaction): AppResult<Unit> {
    if (transaction.amount.value <= 0L) {
        return AppResult.Error("Số tiền phải lớn hơn 0")
    }
    if (transaction.amount.value > MAX_AMOUNT) {
        return AppResult.Error("Số tiền không được vượt quá 15 chữ số")
    }
    if (transaction.walletId.isBlank()) {
        return AppResult.Error("Vui lòng chọn ví")
    }
    val isTransfer = transaction.type == TransactionType.TRANSFER_IN ||
        transaction.type == TransactionType.TRANSFER_OUT
    if (!isTransfer && transaction.categoryId.isNullOrBlank()) {
        return AppResult.Error("Vui lòng chọn danh mục")
    }
    if (isTransfer && transaction.relatedWalletId.isNullOrBlank()) {
        return AppResult.Error("Vui lòng chọn ví đối ứng")
    }
    return AppResult.Success(Unit)
}

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke(transaction: FinanceTransaction): AppResult<String> {
        val validation = validateTransaction(transaction)
        if (validation is AppResult.Error) return validation
        return repository.addWithBalanceUpdate(transaction)
    }
}

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

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke(transaction: FinanceTransaction): AppResult<Unit> {
        if (transaction.id.isBlank()) return AppResult.Error("Giao dịch cần xóa không hợp lệ")
        return repository.deleteWithBalanceUpdate(transaction)
    }
}
