package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.SavingSpinStatus
import com.finlux.app.domain.repository.SavingSpinRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SpinSavingWheelUseCaseTest {
    private val repository = mockk<SavingSpinRepository>()
    private val spin = SpinSavingWheelUseCase(repository)

    @Test
    fun `returns persisted result without randomizing or writing again`() = runTest {
        val persisted = session().copy(
            selectedIndex = 1,
            selectedAmount = Money(10_000),
            status = SavingSpinStatus.SPUN_PENDING,
        )

        assertEquals(AppResult.Success(persisted), spin(persisted))
        coVerify(exactly = 0) { repository.lockSpinResult(any(), any()) }
    }

    @Test
    fun `ready session locks exactly one valid index`() = runTest {
        val session = session()
        coEvery { repository.lockSpinResult(session.scheduleKey, any()) } answers {
            val index = secondArg<Int>()
            AppResult.Success(session.copy(
                selectedIndex = index,
                selectedAmount = session.wheelValues[index],
                status = SavingSpinStatus.SPUN_PENDING,
            ))
        }

        val result = spin(session)

        assertTrue(result is AppResult.Success)
        coVerify(exactly = 1) { repository.lockSpinResult(session.scheduleKey, match { it in session.wheelValues.indices }) }
    }

    @Test
    fun `non-ready session is rejected`() = runTest {
        val result = spin(session().copy(status = SavingSpinStatus.SKIPPED))

        assertTrue(result is AppResult.Error)
        coVerify(exactly = 0) { repository.lockSpinResult(any(), any()) }
    }

    private fun session() = SavingSpinSession(
        id = "day_2026-08-31",
        scheduleKey = "day:2026-08-31",
        wheelValues = listOf(Money(5_000), Money(10_000), Money(15_000), Money(20_000), Money(25_000), Money(30_000)),
    )
}
