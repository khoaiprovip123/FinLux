package com.finlux.app.domain.usecase

import com.finlux.app.core.time.FinanceClock
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SavingDestination
import com.finlux.app.domain.model.SavingMethod
import com.finlux.app.domain.model.SavingSpinReportRange
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.SavingSpinStatus
import com.finlux.app.domain.repository.SavingSpinRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId

class GetSavingSpinReportUseCaseTest {
    private val repository = mockk<SavingSpinRepository>(relaxed = true)
    private val now = Instant.parse("2026-09-04T10:00:00Z")
    private val clock = object : FinanceClock {
        override val zoneId: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh")
        override fun now(): Instant = now
    }
    private val financialResolver = DefaultFinancialPeriodResolver(DefaultSalaryCycleCalculator())
    private val calculateStreak = CalculateSavingSpinStreakUseCase(financialResolver, clock)

    private lateinit var useCase: GetSavingSpinReportUseCase

    @BeforeEach
    fun setUp() {
        useCase = GetSavingSpinReportUseCase(repository, calculateStreak)
    }

    @Test
    fun `calculates report summary with correct totals and averages`() = runTest {
        every { repository.observeDestinations() } returns flowOf(
            listOf(
                SavingDestination("piggy", "Heo đất", SavingMethod.CASH),
                SavingDestination("mb", "MB Bank", SavingMethod.BANK_TRANSFER),
            )
        )

        val sessions = listOf(
            SavingSpinSession(
                id = "1",
                scheduleKey = "day:2026-09-04",
                wheelValues = listOf(Money(10_000)),
                selectedIndex = 0,
                selectedAmount = Money(10_000),
                destinationId = "piggy",
                status = SavingSpinStatus.COMPLETED,
                completedAt = now,
                createdAt = now,
            ),
            SavingSpinSession(
                id = "2",
                scheduleKey = "day:2026-09-03",
                wheelValues = listOf(Money(50_000)),
                selectedIndex = 0,
                selectedAmount = Money(50_000),
                destinationId = "mb",
                status = SavingSpinStatus.COMPLETED,
                completedAt = now.minusSeconds(86400),
                createdAt = now.minusSeconds(86400),
            ),
            SavingSpinSession(
                id = "3",
                scheduleKey = "day:2026-09-02",
                wheelValues = listOf(Money(20_000)),
                selectedIndex = 0,
                selectedAmount = Money(20_000),
                status = SavingSpinStatus.SKIPPED,
                createdAt = now.minusSeconds(86400 * 2),
            ),
        )

        val start = now.minusSeconds(86400 * 7)
        every { repository.observeSessions(start, now) } returns flowOf(sessions)

        val range = SavingSpinReportRange(start, now)
        val report = useCase(range, clock.zoneId).first()

        assertEquals(Money(60_000), report.summary.savedAmount)
        assertEquals(2, report.summary.completedCount)
        assertEquals(1, report.summary.skippedCount)
        assertEquals(66, report.summary.completionRate) // 2 / 3 = 66.6% -> 66% integer
        assertEquals(Money(30_000), report.summary.averageAmount)
        assertEquals(Money(50_000), report.summary.highestAmount)
        assertEquals(Money(10_000), report.summary.lowestAmount)
        assertEquals(2, report.summary.currentStreak)
    }
}
