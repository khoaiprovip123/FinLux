package com.finlux.app.data.demo

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SavingDestination
import com.finlux.app.domain.model.SavingMethod
import com.finlux.app.domain.model.SavingSpinStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DemoSavingSpinRepositoryTest {
    @Test
    fun `get or create is idempotent and preserves wheel`() = runTest {
        val repository = DemoSavingSpinRepository()
        val initial = repository.getOrCreateSession("day:2026-08-31", listOf(Money(5_000), Money(10_000)))
        val repeated = repository.getOrCreateSession("day:2026-08-31", listOf(Money(50_000), Money(100_000)))

        assertTrue(initial is AppResult.Success)
        assertEquals(initial, repeated)
    }

    @Test
    fun `spin result is locked once and completion needs an enabled destination`() = runTest {
        val repository = DemoSavingSpinRepository()
        repository.getOrCreateSession("day:2026-08-31", listOf(Money(5_000), Money(10_000)))

        val first = repository.lockSpinResult("day:2026-08-31", 1) as AppResult.Success
        val repeated = repository.lockSpinResult("day:2026-08-31", 0) as AppResult.Success
        assertEquals(Money(10_000), first.value.selectedAmount)
        assertEquals(first.value, repeated.value)

        val destinationId = (repository.upsertDestination(
            SavingDestination(name = "Heo đất", method = SavingMethod.CASH),
        ) as AppResult.Success).value
        assertTrue(repository.completeSession("day:2026-08-31", destinationId, SavingMethod.CASH) is AppResult.Success)
        assertEquals(SavingSpinStatus.COMPLETED, repository.observeSession("day:2026-08-31").first()?.status)
    }
}
