package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val walletRepository: WalletRepository,
) {
    suspend operator fun invoke(transaction: FinanceTransaction): AppResult<String> {
        val validation = validateTransaction(transaction)
        if (validation is AppResult.Error) return validation

        if (transaction.type == TransactionType.EXPENSE) {
            val wallets = walletRepository.observeWallets().firstOrNull().orEmpty()
            val wallet = wallets.firstOrNull { it.id == transaction.walletId }
            if (wallet != null && wallet.type != WalletType.CARD) {
                if (wallet.balance.value < transaction.amount.value) {
                    return AppResult.Error("Số dư ví [${wallet.name}] không đủ để thực hiện chi tiêu")
                }
            }
        }

        return repository.addWithBalanceUpdate(transaction)
    }
}

