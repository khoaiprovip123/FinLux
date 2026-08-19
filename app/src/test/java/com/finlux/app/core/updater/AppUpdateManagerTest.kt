package com.finlux.app.core.updater

import android.content.Context
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AppUpdateManagerTest {

    private val context: Context = mockk(relaxed = true)
    private lateinit var appUpdateManager: AppUpdateManager

    @BeforeEach
    fun setUp() {
        appUpdateManager = AppUpdateManager(context)
    }

    @Test
    fun `isNewerVersion returns true when remote version is higher`() {
        assertTrue(appUpdateManager.isNewerVersion("1.8.4", "1.8.3"))
        assertTrue(appUpdateManager.isNewerVersion("1.9.0", "1.8.3"))
        assertTrue(appUpdateManager.isNewerVersion("2.0.0", "1.8.3"))
        assertTrue(appUpdateManager.isNewerVersion("1.8.3.1", "1.8.3"))
    }

    @Test
    fun `isNewerVersion returns false when remote version is lower or equal`() {
        assertFalse(appUpdateManager.isNewerVersion("1.8.3", "1.8.3"))
        assertFalse(appUpdateManager.isNewerVersion("1.8.2", "1.8.3"))
        assertFalse(appUpdateManager.isNewerVersion("1.7.9", "1.8.3"))
        assertFalse(appUpdateManager.isNewerVersion("1.8.3", "1.8.4"))
    }

    @Test
    fun `isNewerVersion returns false for blank inputs`() {
        assertFalse(appUpdateManager.isNewerVersion("", "1.8.3"))
        assertFalse(appUpdateManager.isNewerVersion("1.8.4", ""))
        assertFalse(appUpdateManager.isNewerVersion("", ""))
    }
}
