package com.finlux.app.data.local.reminder

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ReminderTriggerDeduplicatorTest {

    @BeforeEach
    fun setup() {
        ReminderTriggerDeduplicator.resetForTest()
    }

    @Test
    fun `first trigger for reminder is allowed`() {
        val allowed = ReminderTriggerDeduplicator.shouldTrigger("rem-1")
        assertTrue(allowed, "First trigger must be allowed")
    }

    @Test
    fun `immediate second trigger within window is blocked to prevent duplicates`() {
        ReminderTriggerDeduplicator.shouldTrigger("rem-1")
        val secondAllowed = ReminderTriggerDeduplicator.shouldTrigger("rem-1", triggerTimeWindowMs = 60_000L)
        assertFalse(secondAllowed, "Immediate subsequent trigger must be blocked")
    }

    @Test
    fun `triggers for different reminder IDs are independent`() {
        assertTrue(ReminderTriggerDeduplicator.shouldTrigger("rem-1"))
        assertTrue(ReminderTriggerDeduplicator.shouldTrigger("rem-2"), "Different reminder ID must be allowed")
    }
}
