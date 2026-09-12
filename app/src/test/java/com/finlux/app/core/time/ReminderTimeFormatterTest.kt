package com.finlux.app.core.time

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId

class ReminderTimeFormatterTest {

    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")

    private fun instantOf(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        second: Int = 0,
    ) = LocalDateTime.of(year, month, day, hour, minute, second).atZone(zone).toInstant()

    @Test
    fun `under 1 minute returns sap dien ra and is urgent`() {
        val now = instantOf(2026, 9, 12, 10, 0, 0)
        val target = instantOf(2026, 9, 12, 10, 0, 45)

        val result = formatReminderCountdown(target = target, now = now, zone = zone)

        assertEquals("sắp diễn ra", result.label)
        assertEquals(ReminderCountdownTier.TODAY, result.tier)
        assertTrue(result.isUrgent)
        assertFalse(result.isOverdue)
    }

    @Test
    fun `under 1 hour returns con X phut and is urgent`() {
        val now = instantOf(2026, 9, 12, 10, 0, 0)
        val target = instantOf(2026, 9, 12, 10, 45, 0)

        val result = formatReminderCountdown(target = target, now = now, zone = zone)

        assertEquals("còn 45 phút", result.label)
        assertEquals(ReminderCountdownTier.TODAY, result.tier)
        assertTrue(result.isUrgent)
        assertFalse(result.isOverdue)
    }

    @Test
    fun `under 1 hour across midnight returns con X phut and is urgent`() {
        val now = instantOf(2026, 9, 12, 23, 40, 0)
        val target = instantOf(2026, 9, 13, 0, 15, 0) // 35 minutes later, tomorrow

        val result = formatReminderCountdown(target = target, now = now, zone = zone)

        assertEquals("còn 35 phút", result.label)
        assertEquals(ReminderCountdownTier.TODAY, result.tier)
        assertTrue(result.isUrgent)
        assertFalse(result.isOverdue)
    }

    @Test
    fun `same day with multiple hours returns con X gio`() {
        val now = instantOf(2026, 9, 12, 9, 15, 0)
        val target = instantOf(2026, 9, 12, 15, 30, 0) // 6 hours later, same day

        val result = formatReminderCountdown(target = target, now = now, zone = zone)

        assertEquals("còn 6 giờ", result.label)
        assertEquals(ReminderCountdownTier.TODAY, result.tier)
        assertFalse(result.isOverdue)
    }

    @Test
    fun `tomorrow returns ngay mai`() {
        val now = instantOf(2026, 9, 12, 9, 15, 0)
        val target = instantOf(2026, 9, 13, 7, 50, 0) // Next day

        val result = formatReminderCountdown(target = target, now = now, zone = zone)

        assertEquals("ngày mai", result.label)
        assertEquals(ReminderCountdownTier.TOMORROW, result.tier)
        assertFalse(result.isUrgent)
        assertFalse(result.isOverdue)
    }

    @Test
    fun `two days later returns con 2 ngay`() {
        val now = instantOf(2026, 9, 12, 9, 15, 0)
        val target = instantOf(2026, 9, 14, 9, 0, 0) // 2 days later

        val result = formatReminderCountdown(target = target, now = now, zone = zone)

        assertEquals("còn 2 ngày", result.label)
        assertEquals(ReminderCountdownTier.FUTURE, result.tier)
        assertFalse(result.isUrgent)
        assertFalse(result.isOverdue)
    }

    @Test
    fun `two weeks later returns con 14 ngay`() {
        val now = instantOf(2026, 9, 12, 9, 15, 0)
        val target = instantOf(2026, 9, 26, 16, 10, 0) // 14 days later

        val result = formatReminderCountdown(target = target, now = now, zone = zone)

        assertEquals("còn 14 ngày", result.label)
        assertEquals(ReminderCountdownTier.FUTURE, result.tier)
        assertFalse(result.isUrgent)
        assertFalse(result.isOverdue)
    }

    @Test
    fun `overdue within same day by minutes returns qua han X phut`() {
        val now = instantOf(2026, 9, 12, 10, 30, 0)
        val target = instantOf(2026, 9, 12, 10, 15, 0) // 15 mins ago

        val result = formatReminderCountdown(target = target, now = now, zone = zone)

        assertEquals("quá hạn 15 phút", result.label)
        assertEquals(ReminderCountdownTier.OVERDUE, result.tier)
        assertTrue(result.isOverdue)
        assertFalse(result.isUrgent)
    }

    @Test
    fun `overdue within same day by hours returns qua han X gio`() {
        val now = instantOf(2026, 9, 12, 14, 0, 0)
        val target = instantOf(2026, 9, 12, 10, 0, 0) // 4 hours ago

        val result = formatReminderCountdown(target = target, now = now, zone = zone)

        assertEquals("quá hạn 4 giờ", result.label)
        assertEquals(ReminderCountdownTier.OVERDUE, result.tier)
        assertTrue(result.isOverdue)
        assertFalse(result.isUrgent)
    }

    @Test
    fun `overdue by 1 day returns qua han 1 ngay`() {
        val now = instantOf(2026, 9, 12, 10, 0, 0)
        val target = instantOf(2026, 9, 11, 10, 0, 0) // 1 day ago

        val result = formatReminderCountdown(target = target, now = now, zone = zone)

        assertEquals("quá hạn 1 ngày", result.label)
        assertEquals(ReminderCountdownTier.OVERDUE, result.tier)
        assertTrue(result.isOverdue)
        assertFalse(result.isUrgent)
    }

    @Test
    fun `overdue by multiple days returns qua han X ngay`() {
        val now = instantOf(2026, 9, 12, 10, 0, 0)
        val target = instantOf(2026, 9, 7, 9, 0, 0) // 5 days ago

        val result = formatReminderCountdown(target = target, now = now, zone = zone)

        assertEquals("quá hạn 5 ngày", result.label)
        assertEquals(ReminderCountdownTier.OVERDUE, result.tier)
        assertTrue(result.isOverdue)
        assertFalse(result.isUrgent)
    }

    @Test
    fun `overdue by seconds returns vua qua han`() {
        val now = instantOf(2026, 9, 12, 10, 0, 30)
        val target = instantOf(2026, 9, 12, 10, 0, 0) // 30 seconds ago

        val result = formatReminderCountdown(target = target, now = now, zone = zone)

        assertEquals("vừa quá hạn", result.label)
        assertEquals(ReminderCountdownTier.OVERDUE, result.tier)
        assertTrue(result.isOverdue)
        assertFalse(result.isUrgent)
    }
}
