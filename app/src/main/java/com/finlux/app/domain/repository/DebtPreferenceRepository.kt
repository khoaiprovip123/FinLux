package com.finlux.app.domain.repository

import com.finlux.app.domain.model.PayoffStrategy
import kotlinx.coroutines.flow.Flow

/**
 * Repository for persisting Debt Freedom Hub user preferences (Payoff strategy & Extra monthly payment).
 */
interface DebtPreferenceRepository {
    fun observePayoffStrategy(): Flow<PayoffStrategy>
    suspend fun savePayoffStrategy(strategy: PayoffStrategy)

    fun observeExtraMonthlyPayment(): Flow<Long>
    suspend fun saveExtraMonthlyPayment(amount: Long)
}
