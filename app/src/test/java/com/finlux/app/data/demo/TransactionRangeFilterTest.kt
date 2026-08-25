package com.finlux.app.data.demo

import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class TransactionRangeFilterTest {
    private val start = Instant.parse("2026-08-25T00:00:00Z")
    private val endExclusive = Instant.parse("2026-09-25T00:00:00Z")

    @Test
    fun `range is half open and excludes exact end boundary`() {
        val items = listOf(
            transaction("before", start.minusMillis(1)),
            transaction("start", start),
            transaction("middle", start.plusSeconds(60)),
            transaction("end", endExclusive),
        )

        val result = filterTransactionsForRange(items, start, endExclusive)

        assertEquals(listOf("start", "middle"), result.map { it.id })
    }

    @Test
    fun `range filtering does not impose recent transaction cap`() {
        val items = (0 until 5_001).map { index ->
            transaction("tx-$index", start.plusSeconds(index.toLong()))
        }

        assertEquals(5_001, filterTransactionsForRange(items, start, endExclusive).size)
    }

    private fun transaction(id: String, date: Instant) = FinanceTransaction(
        id = id,
        type = TransactionType.EXPENSE,
        amount = Money(1),
        categoryId = "category",
        walletId = "wallet",
        date = date,
    )
}
