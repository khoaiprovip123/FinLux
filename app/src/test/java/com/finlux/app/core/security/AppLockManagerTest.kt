package com.finlux.app.core.security

import com.finlux.app.domain.model.BiometricLockTimeout
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AppLockManagerTest {

    @BeforeEach
    fun setUp() {
        AppLockManager.resetForTest()
    }

    @Test
    fun `init with biometric disabled should not lock`() {
        AppLockManager.init(biometricEnabled = false, timeout = BiometricLockTimeout.IMMEDIATE)
        assertFalse(AppLockManager.isLocked.value)
    }

    @Test
    fun `init with biometric enabled should lock immediately`() {
        AppLockManager.init(biometricEnabled = true, timeout = BiometricLockTimeout.IMMEDIATE)
        assertTrue(AppLockManager.isLocked.value)

        AppLockManager.unlock()
        assertFalse(AppLockManager.isLocked.value)
    }

    @Test
    fun `immediate timeout locks on resume after background`() {
        AppLockManager.init(biometricEnabled = true, timeout = BiometricLockTimeout.IMMEDIATE)
        AppLockManager.unlock()
        assertFalse(AppLockManager.isLocked.value)

        val t0 = 1_000_000L
        AppLockManager.recordBackground(t0)

        // Resume 100ms later
        AppLockManager.checkAutoLock(t0 + 100L)
        assertTrue(AppLockManager.isLocked.value)
    }

    @Test
    fun `one minute timeout does not lock before 60s and locks after 60s`() {
        AppLockManager.init(biometricEnabled = true, timeout = BiometricLockTimeout.ONE_MINUTE)
        AppLockManager.unlock()

        val t0 = 1_000_000L
        AppLockManager.recordBackground(t0)

        // Resume after 30 seconds -> should still be unlocked
        AppLockManager.checkAutoLock(t0 + 30_000L)
        assertFalse(AppLockManager.isLocked.value)

        // Resume after 61 seconds -> should lock
        AppLockManager.checkAutoLock(t0 + 61_000L)
        assertTrue(AppLockManager.isLocked.value)
    }

    @Test
    fun `five minutes timeout does not lock before 300s and locks after 300s`() {
        AppLockManager.init(biometricEnabled = true, timeout = BiometricLockTimeout.FIVE_MINUTES)
        AppLockManager.unlock()

        val t0 = 1_000_000L
        AppLockManager.recordBackground(t0)

        // Resume after 2 minutes -> unlocked
        AppLockManager.checkAutoLock(t0 + 120_000L)
        assertFalse(AppLockManager.isLocked.value)

        // Resume after 5 minutes and 1 second -> locked
        AppLockManager.checkAutoLock(t0 + 301_000L)
        assertTrue(AppLockManager.isLocked.value)
    }

    @Test
    fun `disabling biometric clears lock state`() {
        AppLockManager.init(biometricEnabled = true, timeout = BiometricLockTimeout.IMMEDIATE)
        assertTrue(AppLockManager.isLocked.value)

        AppLockManager.updatePreferences(biometricEnabled = false, timeout = BiometricLockTimeout.IMMEDIATE)
        assertFalse(AppLockManager.isLocked.value)
    }
}
