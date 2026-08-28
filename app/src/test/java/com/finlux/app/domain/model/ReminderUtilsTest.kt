package com.finlux.app.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class ReminderUtilsTest {

    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")

    @Test
    fun computeNextTriggerDate_whenFutureDate_returnsSameInstant() {
        val targetDate = LocalDate.of(2026, 9, 1)
        val targetTime = LocalTime.of(7, 30)
        val startInstant = ZonedDateTime.of(targetDate, targetTime, zone).toInstant()
        val referenceInstant = ZonedDateTime.of(LocalDate.of(2026, 8, 28), targetTime, zone).toInstant()

        val nextTrigger = ReminderUtils.computeNextTriggerDate(
            startDate = startInstant,
            recurrence = ReminderRecurrence.DAILY,
            afterInstant = referenceInstant,
            zoneId = zone,
        )

        assertEquals(startInstant, nextTrigger)
    }

    @Test
    fun computeNextTriggerDate_daily_preservesLocalTime() {
        val targetTime = LocalTime.of(8, 15, 0)
        val startDate = LocalDate.of(2026, 8, 1)
        val startInstant = ZonedDateTime.of(startDate, targetTime, zone).toInstant()

        // Reference is 2026-08-28 10:00 (after today's 08:15)
        val referenceInstant = ZonedDateTime.of(
            LocalDate.of(2026, 8, 28),
            LocalTime.of(10, 0),
            zone,
        ).toInstant()

        val nextTrigger = ReminderUtils.computeNextTriggerDate(
            startDate = startInstant,
            recurrence = ReminderRecurrence.DAILY,
            afterInstant = referenceInstant,
            zoneId = zone,
        )

        val nextZdt = nextTrigger.atZone(zone)
        assertEquals(targetTime, nextZdt.toLocalTime())
        assertEquals(LocalDate.of(2026, 8, 29), nextZdt.toLocalDate())
        assertTrue(nextTrigger.isAfter(referenceInstant))
    }

    @Test
    fun computeNextTriggerDate_weekly_preservesDayOfWeekAndLocalTime() {
        val targetTime = LocalTime.of(9, 0, 0)
        // 2026-08-03 is Monday
        val startDate = LocalDate.of(2026, 8, 3)
        val startInstant = ZonedDateTime.of(startDate, targetTime, zone).toInstant()

        // Reference is 2026-08-28 (Friday)
        val referenceInstant = ZonedDateTime.of(
            LocalDate.of(2026, 8, 28),
            LocalTime.of(12, 0),
            zone,
        ).toInstant()

        val nextTrigger = ReminderUtils.computeNextTriggerDate(
            startDate = startInstant,
            recurrence = ReminderRecurrence.WEEKLY,
            afterInstant = referenceInstant,
            zoneId = zone,
        )

        val nextZdt = nextTrigger.atZone(zone)
        assertEquals(targetTime, nextZdt.toLocalTime())
        // Next Monday is 2026-08-31
        assertEquals(LocalDate.of(2026, 8, 31), nextZdt.toLocalDate())
        assertTrue(nextTrigger.isAfter(referenceInstant))
    }

    @Test
    fun computeNextTriggerDate_monthly_preservesDayOfMonthAndLocalTime() {
        val targetTime = LocalTime.of(20, 0, 0)
        val startDate = LocalDate.of(2026, 1, 15)
        val startInstant = ZonedDateTime.of(startDate, targetTime, zone).toInstant()

        // Reference is 2026-08-28 (past August 15th)
        val referenceInstant = ZonedDateTime.of(
            LocalDate.of(2026, 8, 28),
            LocalTime.of(12, 0),
            zone,
        ).toInstant()

        val nextTrigger = ReminderUtils.computeNextTriggerDate(
            startDate = startInstant,
            recurrence = ReminderRecurrence.MONTHLY,
            afterInstant = referenceInstant,
            zoneId = zone,
        )

        val nextZdt = nextTrigger.atZone(zone)
        assertEquals(targetTime, nextZdt.toLocalTime())
        // Next monthly trigger should be 2026-09-15
        assertEquals(LocalDate.of(2026, 9, 15), nextZdt.toLocalDate())
        assertTrue(nextTrigger.isAfter(referenceInstant))
    }

    @Test
    fun computeNextTriggerDate_monthlyEndDay_doesNotDegradeAfterShortMonths() {
        val targetTime = LocalTime.of(18, 30, 0)
        // 31st of January
        val startDate = LocalDate.of(2026, 1, 31)
        val startInstant = ZonedDateTime.of(startDate, targetTime, zone).toInstant()

        // Reference is 2026-02-28 (after Feb trigger)
        val refAfterFeb = ZonedDateTime.of(
            LocalDate.of(2026, 2, 28),
            LocalTime.of(19, 0),
            zone,
        ).toInstant()

        val nextAfterFeb = ReminderUtils.computeNextTriggerDate(
            startDate = startInstant,
            recurrence = ReminderRecurrence.MONTHLY,
            afterInstant = refAfterFeb,
            zoneId = zone,
        )

        // March has 31 days, so it should restore to day 31!
        val marchZdt = nextAfterFeb.atZone(zone)
        assertEquals(LocalDate.of(2026, 3, 31), marchZdt.toLocalDate())
        assertEquals(targetTime, marchZdt.toLocalTime())
    }
}
