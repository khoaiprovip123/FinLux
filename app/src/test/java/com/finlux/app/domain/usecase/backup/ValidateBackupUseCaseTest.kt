package com.finlux.app.domain.usecase.backup

import com.finlux.app.core.common.AppResult
import com.finlux.app.core.common.BackupChecksumHelper
import com.finlux.app.domain.model.backup.BackupPreviewSummary
import com.finlux.app.domain.model.backup.FinluxBackupSnapshot
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

/**
 * Unit tests for [ValidateBackupUseCase] — covers spec cases T-VAL-01 to T-VAL-09.
 *
 * ValidateBackupUseCase takes a plain [InputStream] (no Android context) so all
 * tests run on the JVM without Robolectric or instrumentation.
 */
class ValidateBackupUseCaseTest {

    // ─── Test fixture helpers ──────────────────────────────────────────────

    private val currentUserId = "user-abc-123"
    private val useCase = ValidateBackupUseCase(currentUserId)

    /**
     * Builds a minimal, valid snapshot JSON string with correct SHA-256 checksum.
     */
    private fun buildValidJson(
        schemaVersion: Int = FinluxBackupSnapshot.CURRENT_SCHEMA_VERSION,
        exportedByUid: String = currentUserId,
        walletCount: Int = 3,
        txCount: Int = 5,
        categoryCount: Int = 4,
        includeSalaryCycle: Boolean = false,
        includeSavingSpin: Boolean = false,
        corruptChecksum: Boolean = false,
        dropField: String? = null,
    ): String {
        val wallets = JSONArray().apply {
            repeat(walletCount) { i ->
                put(JSONObject().apply {
                    put("id", "wallet-$i")
                    put("name", "Ví $i")
                    put("type", "cash")
                    put("balance", 1_000_000L * (i + 1))
                    put("colorHex", "#10B981")
                    put("isDefault", i == 0)
                    put("createdAt", "2026-01-01T00:00:00Z")
                    put("status", "active")
                })
            }
        }

        val categories = JSONArray().apply {
            repeat(categoryCount) { i ->
                put(JSONObject().apply {
                    put("id", "cat-$i")
                    put("name", "Danh mục $i")
                    put("type", "expense")
                    put("icon", "ic_food")
                    put("colorHex", "#F97316")
                    put("isDefault", true)
                    put("isEssential", true)
                    put("createdAt", "2026-01-01T00:00:00Z")
                })
            }
        }

        val transactions = JSONArray().apply {
            repeat(txCount) { i ->
                put(JSONObject().apply {
                    put("id", "tx-$i")
                    put("type", "expense")
                    put("amount", 50_000L)
                    put("walletId", "wallet-0")
                    put("note", "Giao dịch $i")
                    put("date", "2026-09-0${(i % 9) + 1}T12:00:00Z")
                    put("createdAt", "2026-09-01T12:00:00Z")
                    put("updatedAt", "2026-09-01T12:00:00Z")
                })
            }
        }

        val root = JSONObject().apply {
            put("schemaVersion", schemaVersion)
            put("appVersion", "1.25.6")
            put("appVersionCode", 180)
            put("exportedAt", 1_758_441_600_000L) // 2026-09-21T17:00:00Z
            put("exportedByUid", exportedByUid)
            put("checksum", "") // placeholder for checksum computation
            put("payloadSizeBytes", 18_432L)
            put("wallets", wallets)
            put("categories", categories)
            put("transactions", transactions)
            put("budgets", JSONArray())
            put("debts", JSONArray())
            put("debtPayments", JSONArray())
            put("goals", JSONArray())
            put("reminders", JSONArray())
            put("deals", JSONArray())
            if (includeSalaryCycle) {
                put("salaryCycleConfig", JSONObject().apply {
                    put("enabled", true)
                    put("scheduleType", "MONTHLY_ONCE")
                    put("paydayDay", 25)
                    put("financeTimeZone", "Asia/Ho_Chi_Minh")
                })
            }
            if (includeSavingSpin) {
                put("savingSpinConfig", JSONObject().apply {
                    put("enabled", true)
                    put("minAmount", 50_000L)
                    put("maxAmount", 500_000L)
                })
            }
        }

        // Drop a field if requested (for T-VAL-04)
        dropField?.let { root.remove(it) }

        // Compute checksum over payload with checksum = ""
        val payloadForChecksum = root.toString()
        val actualChecksum = BackupChecksumHelper.computeChecksum(payloadForChecksum)
        root.put("checksum", if (corruptChecksum) "aaaa1111bbbb2222cccc3333dddd4444ffff5555eeee6666aaaa1111bbbb2222" else actualChecksum)

        return root.toString()
    }

    private fun jsonToStream(json: String) =
        ByteArrayInputStream(json.toByteArray(Charsets.UTF_8))

    // ─── T-VAL-01: Valid file passes all checks ────────────────────────────

    @Test
    fun `T-VAL-01 valid file returns Success with correct preview summary`() {
        val json = buildValidJson(walletCount = 5, txCount = 100, categoryCount = 8)
        val result = useCase(jsonToStream(json))

        assertInstanceOf(AppResult.Success::class.java, result)
        val summary = (result as AppResult.Success<BackupPreviewSummary>).value
        assertTrue(summary.isChecksumValid, "Checksum should be valid")
        assertEquals(5, summary.walletCount)
        assertEquals(100, summary.transactionCount)
        assertEquals(8, summary.categoryCount)
        assertEquals(FinluxBackupSnapshot.CURRENT_SCHEMA_VERSION, summary.schemaVersion)
        assertEquals("1.25.6", summary.appVersion)
        assertFalse(summary.isCrossAccount, "Same UID should not be cross-account")
    }

    // ─── T-VAL-02: Malformed JSON is rejected ─────────────────────────────

    @Test
    fun `T-VAL-02 malformed JSON returns Error with format message`() {
        val badJson = "{not valid json at all {{{"
        val result = useCase(jsonToStream(badJson))

        assertInstanceOf(AppResult.Error::class.java, result)
        val error = result as AppResult.Error
        assertTrue(
            error.message.contains("hợp lệ", ignoreCase = true) ||
                error.message.contains("JSON", ignoreCase = true),
            "Error should mention invalid format, got: ${error.message}"
        )
    }

    // ─── T-VAL-03: Tampered checksum is rejected ──────────────────────────

    @Test
    fun `T-VAL-03 tampered checksum returns Error with integrity message`() {
        val json = buildValidJson(corruptChecksum = true)
        val result = useCase(jsonToStream(json))

        assertInstanceOf(AppResult.Error::class.java, result)
        val error = result as AppResult.Error
        assertTrue(
            error.message.contains("SHA-256", ignoreCase = true) ||
                error.message.contains("chữ ký", ignoreCase = true) ||
                error.message.contains("chỉnh sửa", ignoreCase = true),
            "Error should mention checksum/tampering, got: ${error.message}"
        )
    }

    // ─── T-VAL-04: Missing required field "wallets" is rejected ───────────

    @Test
    fun `T-VAL-04 missing required field wallets returns Error`() {
        // Drop 'wallets' BEFORE computing checksum so the checksum itself is still consistent
        // but the required-field gate fires first.
        val json = buildValidJson(dropField = "wallets")
        val result = useCase(jsonToStream(json))

        assertInstanceOf(AppResult.Error::class.java, result)
        val error = result as AppResult.Error
        assertTrue(
            error.message.contains("wallets", ignoreCase = true) ||
                error.message.contains("thiếu", ignoreCase = true),
            "Error should mention missing field, got: ${error.message}"
        )
    }

    // ─── T-VAL-05: Schema version too new → requires app upgrade ──────────

    @Test
    fun `T-VAL-05 schemaVersion newer than supported returns Error requesting upgrade`() {
        val futureSchema = FinluxBackupSnapshot.CURRENT_SCHEMA_VERSION + 1
        // Build without checksum guard since schemaVersion check fires before checksum
        val root = JSONObject().apply {
            put("schemaVersion", futureSchema)
            put("appVersion", "9.9.9")
            put("appVersionCode", 9999)
            put("exportedAt", System.currentTimeMillis())
            put("exportedByUid", currentUserId)
            put("checksum", "")
            put("payloadSizeBytes", 0L)
            put("wallets", JSONArray())
            put("transactions", JSONArray())
        }
        val raw = root.toString()
        root.put("checksum", BackupChecksumHelper.computeChecksum(raw))

        val result = useCase(jsonToStream(root.toString()))

        assertInstanceOf(AppResult.Error::class.java, result)
        val error = result as AppResult.Error
        assertTrue(
            error.message.contains("nâng cấp", ignoreCase = true) ||
                error.message.contains("schema", ignoreCase = true) ||
                error.message.contains("mới hơn", ignoreCase = true),
            "Error should recommend upgrading, got: ${error.message}"
        )
    }

    // ─── T-VAL-06: Schema version below minimum → too old ─────────────────

    @Test
    fun `T-VAL-06 schemaVersion below minimum returns Error for outdated backup`() {
        val tooOldSchema = FinluxBackupSnapshot.MIN_SUPPORTED_SCHEMA_VERSION - 1
        val root = JSONObject().apply {
            put("schemaVersion", tooOldSchema)
            put("appVersion", "0.1.0")
            put("appVersionCode", 1)
            put("exportedAt", System.currentTimeMillis())
            put("exportedByUid", currentUserId)
            put("checksum", "")
            put("payloadSizeBytes", 0L)
            put("wallets", JSONArray())
            put("transactions", JSONArray())
        }
        val raw = root.toString()
        root.put("checksum", BackupChecksumHelper.computeChecksum(raw))

        val result = useCase(jsonToStream(root.toString()))

        // tooOldSchema = 0 → MIN_SUPPORTED_SCHEMA_VERSION is 1, so 0 < 1 → Error
        if (tooOldSchema < FinluxBackupSnapshot.MIN_SUPPORTED_SCHEMA_VERSION) {
            assertInstanceOf(AppResult.Error::class.java, result)
            val error = result as AppResult.Error
            assertTrue(
                error.message.contains("cũ", ignoreCase = true) ||
                    error.message.contains("hỗ trợ", ignoreCase = true),
                "Error should say backup too old, got: ${error.message}"
            )
        } else {
            // If MIN_SUPPORTED_SCHEMA_VERSION == 1 and tooOldSchema == 0 we expect Error
            // But guard for the edge case where MIN_SUPPORTED_SCHEMA_VERSION == 1 already
            // and 0 < 1 is true (this always applies with current constants)
            assertInstanceOf(AppResult.Error::class.java, result)
        }
    }

    // ─── T-VAL-07: Cross-account export is flagged but still Success ───────

    @Test
    fun `T-VAL-07 cross-account backup is Success with isCrossAccount true`() {
        val foreignUid = "user-different-xyz"
        val json = buildValidJson(exportedByUid = foreignUid)
        val result = useCase(jsonToStream(json))

        assertInstanceOf(AppResult.Success::class.java, result)
        val summary = (result as AppResult.Success<BackupPreviewSummary>).value
        assertTrue(summary.isCrossAccount, "Should flag as cross-account")
        assertEquals(foreignUid, summary.exportedByUid)
    }

    // ─── T-VAL-08: Empty file returns Error ───────────────────────────────

    @Test
    fun `T-VAL-08 empty file content returns Error`() {
        val result = useCase(jsonToStream(""))

        assertInstanceOf(AppResult.Error::class.java, result)
        val error = result as AppResult.Error
        assertTrue(
            error.message.contains("rỗng", ignoreCase = true) ||
                error.message.contains("đọc được", ignoreCase = true),
            "Error should mention empty file, got: ${error.message}"
        )
    }

    // ─── T-VAL-09: Preview counts match snapshot contents ─────────────────

    @Test
    fun `T-VAL-09 preview entity counts match snapshot exactly`() {
        val json = buildValidJson(walletCount = 5, txCount = 100, categoryCount = 8)
        val result = useCase(jsonToStream(json))

        assertInstanceOf(AppResult.Success::class.java, result)
        val summary = (result as AppResult.Success<BackupPreviewSummary>).value
        assertEquals(5, summary.walletCount, "walletCount mismatch")
        assertEquals(100, summary.transactionCount, "transactionCount mismatch")
        assertEquals(8, summary.categoryCount, "categoryCount mismatch")
        assertEquals(0, summary.budgetCount, "budgetCount mismatch")
        assertEquals(0, summary.debtCount, "debtCount mismatch")
        assertEquals(0, summary.goalCount, "goalCount mismatch")
    }

    // ─── Bonus: hasSalaryCycleConfig and hasSavingSpinConfig flags ─────────

    @Test
    fun `optional configs are detected in preview summary`() {
        val withBoth = buildValidJson(includeSalaryCycle = true, includeSavingSpin = true)
        val result = useCase(jsonToStream(withBoth))

        assertInstanceOf(AppResult.Success::class.java, result)
        val summary = (result as AppResult.Success<BackupPreviewSummary>).value
        assertTrue(summary.hasSalaryCycleConfig, "Should detect salaryCycleConfig")
        assertTrue(summary.hasSavingSpinConfig, "Should detect savingSpinConfig")
    }

    @Test
    fun `optional configs absent when not included`() {
        val withoutBoth = buildValidJson(includeSalaryCycle = false, includeSavingSpin = false)
        val result = useCase(jsonToStream(withoutBoth))

        assertInstanceOf(AppResult.Success::class.java, result)
        val summary = (result as AppResult.Success<BackupPreviewSummary>).value
        assertFalse(summary.hasSalaryCycleConfig)
        assertFalse(summary.hasSavingSpinConfig)
    }

    // ─── Transaction date range ────────────────────────────────────────────

    @Test
    fun `transaction date range is computed from transaction dates`() {
        val json = buildValidJson(txCount = 5)
        val result = useCase(jsonToStream(json))

        assertInstanceOf(AppResult.Success::class.java, result)
        val summary = (result as AppResult.Success<BackupPreviewSummary>).value
        assertNotNull(summary.dateRangeStart)
        assertNotNull(summary.dateRangeEnd)
    }

    @Test
    fun `date range is null when no transactions`() {
        val json = buildValidJson(txCount = 0)
        val result = useCase(jsonToStream(json))

        assertInstanceOf(AppResult.Success::class.java, result)
        val summary = (result as AppResult.Success<BackupPreviewSummary>).value
        assertNull(summary.dateRangeStart)
        assertNull(summary.dateRangeEnd)
    }
}
