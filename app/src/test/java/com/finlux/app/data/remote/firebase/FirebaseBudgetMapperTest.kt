package com.finlux.app.data.remote.firebase

import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.Money
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.YearMonth
import java.util.Date

class FirebaseBudgetMapperTest {
    private val start = Instant.parse("2026-08-24T17:00:00Z")
    private val end = Instant.parse("2026-09-24T17:00:00Z")

    @Test
    fun `client write map uses timestamps and excludes server-owned aggregates`() {
        val map = budget().toBudgetClientMap()

        assertEquals(Timestamp(Date.from(start)), map["periodStart"])
        assertEquals(Timestamp(Date.from(end)), map["periodEndExclusive"])
        assertFalse("spentAmount" in map)
        assertFalse("notified80" in map)
        assertFalse("notified100" in map)
    }

    @Test
    fun `legacy client update keeps legacy shape without null modern fields`() {
        val map = budget().copy(
            periodStart = null,
            periodEndExclusive = null,
            periodBasis = null,
            month = YearMonth.of(2026, 9),
        ).toBudgetClientMap()

        assertEquals("2026-09", map["month"])
        assertFalse("periodKey" in map)
        assertFalse("periodStart" in map)
        assertFalse("periodEndExclusive" in map)
        assertFalse("periodBasis" in map)
    }

    @Test
    fun `mapper reads modern timestamp period boundaries`() {
        val mapped = snapshot(
            periodStartTimestamp = Timestamp(Date.from(start)),
            periodEndTimestamp = Timestamp(Date.from(end)),
        ).toBudget()!!

        assertEquals(start, mapped.periodStart)
        assertEquals(end, mapped.periodEndExclusive)
        assertEquals("SALARY_CYCLE", mapped.periodBasis)
    }

    @Test
    fun `mapper keeps legacy epoch millis boundaries compatible`() {
        val mapped = snapshot(
            periodStartMillis = start.toEpochMilli(),
            periodEndMillis = end.toEpochMilli(),
        ).toBudget()!!

        assertEquals(start, mapped.periodStart)
        assertEquals(end, mapped.periodEndExclusive)
    }

    private fun budget() = Budget(
        id = "food_salary:2026-08-25",
        categoryId = "food",
        periodKey = "salary:2026-08-25",
        periodStart = start,
        periodEndExclusive = end,
        periodBasis = "SALARY_CYCLE",
        limitAmount = Money(1_000_000),
        spentAmount = Money(850_000),
        notified80 = true,
    )

    private fun snapshot(
        periodStartTimestamp: Timestamp? = null,
        periodEndTimestamp: Timestamp? = null,
        periodStartMillis: Long? = null,
        periodEndMillis: Long? = null,
    ): DocumentSnapshot = mockk<DocumentSnapshot>().also { snapshot ->
        every { snapshot.id } returns "food_salary:2026-08-25"
        every { snapshot.getString("categoryId") } returns "food"
        every { snapshot.getString("periodKey") } returns "salary:2026-08-25"
        every { snapshot.getString("periodBasis") } returns "SALARY_CYCLE"
        every { snapshot.getString("month") } returns null
        every { snapshot.getTimestamp("periodStart") } returns periodStartTimestamp
        every { snapshot.getTimestamp("periodEndExclusive") } returns periodEndTimestamp
        every { snapshot.getLong("periodStart") } returns periodStartMillis
        every { snapshot.getLong("periodEndExclusive") } returns periodEndMillis
        every { snapshot.getLong("limitAmount") } returns 1_000_000
        every { snapshot.getLong("spentAmount") } returns 850_000
        every { snapshot.getBoolean("notified80") } returns true
        every { snapshot.getBoolean("notified100") } returns false
    }
}
