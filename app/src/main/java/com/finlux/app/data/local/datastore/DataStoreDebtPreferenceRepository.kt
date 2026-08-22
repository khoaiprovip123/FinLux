package com.finlux.app.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.finlux.app.domain.model.PayoffStrategy
import com.finlux.app.domain.repository.DebtPreferenceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.debtPreferencesDataStore by preferencesDataStore(name = "finlux_debt_preferences")
private val DebtStrategyKey = stringPreferencesKey("debt_payoff_strategy")
private val DebtExtraPaymentKey = longPreferencesKey("debt_extra_monthly_payment")

@Singleton
class DataStoreDebtPreferenceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : DebtPreferenceRepository {

    override fun observePayoffStrategy(): Flow<PayoffStrategy> = context.debtPreferencesDataStore.data.map { pref ->
        pref[DebtStrategyKey]
            ?.let { stored -> PayoffStrategy.entries.firstOrNull { it.name == stored } }
            ?: PayoffStrategy.SNOWBALL
    }

    override suspend fun savePayoffStrategy(strategy: PayoffStrategy) {
        context.debtPreferencesDataStore.edit { it[DebtStrategyKey] = strategy.name }
    }

    override fun observeExtraMonthlyPayment(): Flow<Long> = context.debtPreferencesDataStore.data.map { pref ->
        pref[DebtExtraPaymentKey] ?: 0L
    }

    override suspend fun saveExtraMonthlyPayment(amount: Long) {
        context.debtPreferencesDataStore.edit { it[DebtExtraPaymentKey] = amount }
    }
}
