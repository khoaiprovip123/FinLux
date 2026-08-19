package com.finlux.app.core.updater

import android.content.Context
import com.finlux.app.core.common.AppResult
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class AppUpdateManagerTest {

    private val context: Context = mockk(relaxed = true)
    private lateinit var appUpdateManager: AppUpdateManager

    @TempDir
    lateinit var tempDir: File

    @BeforeEach
    fun setUp() {
        appUpdateManager = AppUpdateManager(context)
    }

    @Test
    fun `isNewerVersion returns true when remote version is higher`() {
        assertTrue(appUpdateManager.isNewerVersion("1.8.5", "1.8.4"))
        assertTrue(appUpdateManager.isNewerVersion("1.9.0", "1.8.4"))
        assertTrue(appUpdateManager.isNewerVersion("2.0.0", "1.8.4"))
        assertTrue(appUpdateManager.isNewerVersion("1.8.4.1", "1.8.4"))
    }

    @Test
    fun `isNewerVersion returns false when remote version is lower or equal`() {
        assertFalse(appUpdateManager.isNewerVersion("1.8.4", "1.8.4"))
        assertFalse(appUpdateManager.isNewerVersion("1.8.3", "1.8.4"))
        assertFalse(appUpdateManager.isNewerVersion("1.7.9", "1.8.4"))
        assertFalse(appUpdateManager.isNewerVersion("1.8.4", "1.8.5"))
    }

    @Test
    fun `isNewerVersion returns false for blank inputs`() {
        assertFalse(appUpdateManager.isNewerVersion("", "1.8.4"))
        assertFalse(appUpdateManager.isNewerVersion("1.8.4", ""))
        assertFalse(appUpdateManager.isNewerVersion("", ""))
    }

    @Test
    fun `calculateFileSha256 computes correct digest`() {
        val testFile = File(tempDir, "test.txt").apply {
            writeText("FinLux Secure OTA Update Test")
        }
        val sha256 = appUpdateManager.calculateFileSha256(testFile)
        assertTrue(sha256.isNotBlank())
        assertEquals(64, sha256.length)
    }

    @Test
    fun `verifyApkIntegrity fails on non-existent or empty file`() {
        val nonExistent = File(tempDir, "missing.apk")
        val result = appUpdateManager.verifyApkIntegrity(nonExistent)
        assertTrue(result is AppResult.Error)

        val emptyFile = File(tempDir, "empty.apk").apply { createNewFile() }
        val emptyResult = appUpdateManager.verifyApkIntegrity(emptyFile)
        assertTrue(emptyResult is AppResult.Error)
    }

    @Test
    fun `verifyApkIntegrity fails on sha256 checksum mismatch`() {
        val testApk = File(tempDir, "test.apk").apply {
            writeText("dummy apk content")
        }
        val wrongSha = "0000000000000000000000000000000000000000000000000000000000000000"
        val result = appUpdateManager.verifyApkIntegrity(testApk, expectedSha256 = wrongSha)
        assertTrue(result is AppResult.Error)
        assertTrue((result as AppResult.Error).message.contains("SHA-256"))
    }
}
