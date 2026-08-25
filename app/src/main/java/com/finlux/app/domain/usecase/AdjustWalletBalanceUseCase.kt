package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.repository.TransactionRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class AdjustWalletBalanceUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(
        wallet: Wallet,
        targetBalance: Long,
        note: String = "Điều chỉnh số dư ví",
        date: Instant = Instant.now(),
    ): AppResult<Unit> {
        val currentBalance = wallet.balance.value
        val delta = targetBalance - currentBalance

        if (delta == 0L) {
            return AppResult.Success(Unit)
        }

        val isIncrease = delta > 0L
        val adjustmentAmount = if (isIncrease) delta else -delta

        val transaction = FinanceTransaction(
            id = UUID.randomUUID().toString(),
            type = if (isIncrease) TransactionType.INCOME else TransactionType.EXPENSE,
            amount = Money(adjustmentAmount),
            categoryId = "balance_adjustment",
            walletId = wallet.id,
            note = note.ifBlank { "Điều chỉnh số dư ví: ${wallet.name}" },
            date = date,
            createdAt = date,
            updatedAt = date,
        )

        return when (val result = transactionRepository.addWithBalanceUpdate(transaction)) {
            is AppResult.Success -> AppResult.Success(Unit)
            is AppResult.Error -> AppResult.Error(result.message, result.cause)
        }
    }
}
