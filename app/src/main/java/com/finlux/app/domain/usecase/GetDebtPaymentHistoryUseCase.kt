package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.DebtPaymentHistory
import com.finlux.app.domain.repository.DebtRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDebtPaymentHistoryUseCase @Inject constructor(
    private val debtRepository: DebtRepository,
) {
    operator fun invoke(debtId: String? = null): Flow<List<DebtPaymentHistory>> {
        return if (debtId.isNullOrBlank()) {
            debtRepository.observeAllPaymentHistory()
        } else {
            debtRepository.observePaymentHistory(debtId)
        }
    }
}
