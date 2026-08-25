package com.finlux.app.data.demo

import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.repository.TransactionRangeRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class DemoTransactionRangeRepository @Inject constructor(
    private val transactions: DemoFinluxRepository,
) : TransactionRangeRepository {
    override fun observeRange(
        startInclusive: Instant,
        endExclusive: Instant,
    ): Flow<List<FinanceTransaction>> {
        require(startInclusive < endExclusive) { "Khoảng thời gian giao dịch không hợp lệ" }
        return transactions.observeRecent(Int.MAX_VALUE).map { items ->
            filterTransactionsForRange(items, startInclusive, endExclusive)
        }
    }
}

internal fun filterTransactionsForRange(
    items: List<FinanceTransaction>,
    startInclusive: Instant,
    endExclusive: Instant,
): List<FinanceTransaction> = items
    .asSequence()
    .filter { transaction -> transaction.date >= startInclusive && transaction.date < endExclusive }
    .sortedBy { it.date }
    .toList()
