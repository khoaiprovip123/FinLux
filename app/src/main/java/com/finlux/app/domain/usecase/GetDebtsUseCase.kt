package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.repository.DebtRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDebtsUseCase @Inject constructor(
    private val repository: DebtRepository,
) {
    operator fun invoke(): Flow<List<DebtAccount>> = repository.observeDebts()
}
