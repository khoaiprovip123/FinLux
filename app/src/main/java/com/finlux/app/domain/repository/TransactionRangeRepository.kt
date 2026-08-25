package com.finlux.app.domain.repository

import com.finlux.app.domain.model.FinanceTransaction
import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface TransactionRangeRepository {
    fun observeRange(
        startInclusive: Instant,
        endExclusive: Instant,
    ): Flow<List<FinanceTransaction>>
}
