package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull

class EditTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val walletRepository: WalletRepository,
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
        if (!original.goalId.isNullOrBlank() || !original.debtId.isNullOrBlank()) {
            return AppResult.Error(
                if (!original.goalId.isNullOrBlank()) {
                    "Giao dịch mục tiêu phải được điều chỉnh trong mục Tiết kiệm & Mục tiêu"
                } else {
                    "Giao dịch trả nợ phải được điều chỉnh trong mục Nợ & Tín dụng"
                }
            )
        }
        if (!updated.goalId.isNullOrBlank() || !updated.debtId.isNullOrBlank()) {
            return AppResult.Error("Không thể gắn giao dịch thường vào ledger Goal/Debt")
        }

        if (updated.type == TransactionType.EXPENSE) {
            val wallets = walletRepository.observeWallets().firstOrNull().orEmpty()
            val wallet = wallets.firstOrNull { it.id == updated.walletId }
            if (wallet != null && wallet.type != WalletType.CARD) {
                val availableBalance = if (original.walletId == updated.walletId) {
                    val refund = if (original.type == TransactionType.EXPENSE) {
                        original.amount.value
                    } else {
                        -original.amount.value
                    }
                    wallet.balance.value + refund
                } else {
                    wallet.balance.value
                }
                if (availableBalance < updated.amount.value) {
                    return AppResult.Error("Số dư ví [${wallet.name}] không đủ để thực hiện chi tiêu")
                }
            }
        }

        // Budget aggregate reconciliation and threshold alerts happen after the ledger commit
        // in Cloud Functions, including edits that move category or financial period.
        return repository.editWithBalanceUpdate(original, updated)
    }
}
