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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SpinSavingWheelUseCaseTest {
    private val repository = mockk<SavingSpinRepository>(relaxed = true)
    private lateinit var useCase: SpinSavingWheelUseCase

    @BeforeEach
    fun setUp() {
        useCase = SpinSavingWheelUseCase(repository)
    }

    @Test
    fun `ready session executes lock spin result`() = runTest {
        val session = SavingSpinSession(
            id = "day_2026-09-04",
            scheduleKey = "day:2026-09-04",
            wheelValues = listOf(Money(10_000), Money(20_000), Money(50_000)),
            status = SavingSpinStatus.READY,
        )
        val expected = session.copy(
            selectedIndex = 1,
            selectedAmount = Money(20_000),
            status = SavingSpinStatus.SPUN_PENDING,
        )
        coEvery { repository.lockSpinResult("day:2026-09-04", any()) } returns AppResult.Success(expected)

        val result = useCase(session)

        assertTrue(result is AppResult.Success)
        assertEquals(expected, (result as AppResult.Success).value)
        coVerify(exactly = 1) { repository.lockSpinResult("day:2026-09-04", any()) }
    }

    @Test
    fun `spun pending session rejects re-spin`() = runTest {
        val session = SavingSpinSession(
            id = "day_2026-09-04",
            scheduleKey = "day:2026-09-04",
            wheelValues = listOf(Money(10_000), Money(20_000)),
            selectedIndex = 0,
            selectedAmount = Money(10_000),
            status = SavingSpinStatus.SPUN_PENDING,
        )

        val result = useCase(session)

        assertTrue(result is AppResult.Error)
        assertEquals("Lượt tiết kiệm này đã được quay", (result as AppResult.Error).message)
        coVerify(exactly = 0) { repository.lockSpinResult(any(), any()) }
    }

    @Test
    fun `completed session rejects re-spin`() = runTest {
        val session = SavingSpinSession(
            id = "day_2026-09-04",
            scheduleKey = "day:2026-09-04",
            wheelValues = listOf(Money(10_000)),
            selectedIndex = 0,
            selectedAmount = Money(10_000),
            status = SavingSpinStatus.COMPLETED,
        )

        val result = useCase(session)

        assertTrue(result is AppResult.Error)
        assertEquals("Lượt tiết kiệm này đã được quay", (result as AppResult.Error).message)
    }
}
