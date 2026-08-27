package com.finlux.app.data.local.salary

import android.content.Context
import com.finlux.app.core.time.FinanceClock
import com.finlux.app.core.time.FinanceTime
import com.finlux.app.domain.model.PaydayRuleType
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.usecase.DefaultSalaryCycleCalculator
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class AlarmSalaryCycleSchedulerTest {

    private val context: Context = mockk(relaxed = true)
    private val calculator = DefaultSalaryCycleCalculator()
    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")

    private fun createScheduler(nowInstant: Instant): AlarmSalaryCycleScheduler {
        val clock = object : FinanceClock {
            override val zoneId: ZoneId = zone
            override fun now(): Instant = nowInstant
        }
        return AlarmSalaryCycleScheduler(context, calculator, clock)
    }

    @Test
    fun `calculateNextPaydayTrigger schedules at 09_00 on upcoming payday of current cycle`() {
        // Today is 2026-08-05 10:00, Payday is 10
        // Current cycle containing today: 2026-07-10 to 2026-08-10 (endExclusive)
        // Next payday is 2026-08-10 at 09:00
        val now = LocalDateTime.of(2026, 8, 5, 10, 0).atZone(zone).toInstant()
        val scheduler = createScheduler(now)
        val config = SalaryCycleConfig(enabled = true, paydayDay = 10)

        val trigger = scheduler.calculateNextPaydayTrigger(config, now)
        val triggerLdt = trigger.atZone(zone).toLocalDateTime()

        assertEquals(LocalDateTime.of(2026, 8, 10, 9, 0), triggerLdt)
    }

    @Test
    fun `calculateNextPaydayTrigger schedules at 09_00 on next month payday when today is after payday`() {
        // Today is 2026-08-15 14:00, Payday is 10
        // Current cycle containing today: 2026-08-10 to 2026-09-10 (endExclusive)
        // Next payday is 2026-09-10 at 09:00
        val now = LocalDateTime.of(2026, 8, 15, 14, 0).atZone(zone).toInstant()
        val scheduler = createScheduler(now)
        val config = SalaryCycleConfig(enabled = true, paydayDay = 10)

        val trigger = scheduler.calculateNextPaydayTrigger(config, now)
        val triggerLdt = trigger.atZone(zone).toLocalDateTime()

        assertEquals(LocalDateTime.of(2026, 9, 10, 9, 0), triggerLdt)
    }

    @Test
    fun `calculateNextPaydayTrigger clamps day 31 to 30 for September`() {
        // Today is 2026-08-31 10:00, Payday is 31
        // Current cycle: 2026-08-31 to 2026-09-30 (September has 30 days, clamped)
        val now = LocalDateTime.of(2026, 8, 31, 10, 0).atZone(zone).toInstant()
        val scheduler = createScheduler(now)
        val config = SalaryCycleConfig(enabled = true, paydayDay = 31)

        val trigger = scheduler.calculateNextPaydayTrigger(config, now)
        val triggerLdt = trigger.atZone(zone).toLocalDateTime()

        assertEquals(LocalDateTime.of(2026, 9, 30, 9, 0), triggerLdt)
    }
}
