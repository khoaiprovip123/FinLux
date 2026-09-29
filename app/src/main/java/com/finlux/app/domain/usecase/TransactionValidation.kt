package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinanceBusinessConstants
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import java.time.Instant

private const val MAX_AMOUNT = 999_999_999_999_999L

/** Shared validation for UC-07/08, kept in domain so every UI entry point behaves identically. */
internal fun validateTransaction(
    transaction: FinanceTransaction,
    allowFutureDates: Boolean = false,
): AppResult<Unit> {
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
    val isDeal = !transaction.dealId.isNullOrBlank() || transaction.dealFlowType != null
    if (!isTransfer && !isDeal && transaction.categoryId.isNullOrBlank()) {
        return AppResult.Error("Vui lòng chọn danh mục")
    }
    if (isTransfer && transaction.relatedWalletId.isNullOrBlank()) {
        return AppResult.Error("Vui lòng chọn ví đối ứng")
    }
    if (!allowFutureDates) {
        val nowWithTolerance = Instant.now().plusSeconds(FinanceBusinessConstants.TRANSACTION_CLOCK_SKEW_TOLERANCE_SECONDS)
        if (transaction.date.isAfter(nowWithTolerance)) {
            return AppResult.Error("Thời gian giao dịch không được vượt quá thời điểm hiện tại")
        }
    }
    return AppResult.Success(Unit)
}
