package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.repository.TransactionRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke(transaction: FinanceTransaction): AppResult<String> {
        val validation = validateTransaction(transaction)
        if (validation is AppResult.Error) return validation
        return repository.addWithBalanceUpdate(transaction)
    }
}
