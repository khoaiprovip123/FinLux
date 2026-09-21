package com.finlux.app.domain.usecase.backup

import android.net.Uri
import com.finlux.app.core.common.AppResult
import com.finlux.app.core.common.BackupChecksumHelper
import com.finlux.app.domain.model.backup.BackupPreviewSummary
import com.finlux.app.domain.model.backup.FinluxBackupSnapshot
import org.json.JSONObject
import java.io.InputStream
import java.time.Instant

/**
 * Reads and validates a `.finlux` backup file from the given [InputStream].
 *
 * Validation pipeline (spec T-VAL-01..09):
 *  1. Parse JSON — rejects malformed input.
 *  2. Assert required top-level fields present.
 *  3. Check schemaVersion compatibility.
 *  4. Verify SHA-256 checksum (anti-tampering).
 *  5. Build [BackupPreviewSummary] for display in the Restore UI.
 *
 * The use case receives an [InputStream] rather than an Android [Uri] so it is
 * fully testable in the JVM unit-test environment without requiring a Context.
 * The ViewModel/UI layer is responsible for resolving the Uri to an InputStream.
 */
class ValidateBackupUseCase(
    private val defaultUserId: String = "",
) {
    companion object {
        private val REQUIRED_FIELDS = listOf(
            "schemaVersion", "appVersion", "exportedAt",
            "exportedByUid", "checksum", "wallets", "transactions",
        )
    }

    /**
     * @param inputStream  Content of the `.finlux` file.
     * @param fileSizeBytes Byte length of the file (for size label in preview).
     * @param currentUserId Current user's UID (falls back to defaultUserId).
     * @return [AppResult.Success] with [BackupPreviewSummary] when the file is valid,
     *         or [AppResult.Error] describing the validation failure.
     */
    operator fun invoke(
        inputStream: InputStream,
        fileSizeBytes: Long = 0L,
        currentUserId: String = defaultUserId,
    ): AppResult<BackupPreviewSummary> {
        return try {
            // ── Step 1: Read content ──────────────────────────────────────────
            val content = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }

            if (content.isBlank()) {
                return AppResult.Error("File backup rỗng hoặc không đọc được.")
            }

            // ── Step 2: Parse JSON ────────────────────────────────────────────
            val root = try {
                JSONObject(content)
            } catch (e: Exception) {
                return AppResult.Error("Định dạng file không hợp lệ: không phải JSON hợp lệ.")
            }

            // ── Step 3: Required fields ───────────────────────────────────────
            for (field in REQUIRED_FIELDS) {
                if (!root.has(field)) {
                    return AppResult.Error("File backup bị thiếu trường bắt buộc: \"$field\".")
                }
            }

            // ── Step 4: Schema version ────────────────────────────────────────
            val schemaVersion = root.getInt("schemaVersion")
            when {
                schemaVersion > FinluxBackupSnapshot.CURRENT_SCHEMA_VERSION ->
                    return AppResult.Error(
                        "File được tạo bởi phiên bản FinLux mới hơn (schema v$schemaVersion). " +
                            "Vui lòng nâng cấp ứng dụng để mở file này."
                    )
                schemaVersion < FinluxBackupSnapshot.MIN_SUPPORTED_SCHEMA_VERSION ->
                    return AppResult.Error(
                        "File backup quá cũ (schema v$schemaVersion). " +
                            "Phiên bản tối thiểu được hỗ trợ là v${FinluxBackupSnapshot.MIN_SUPPORTED_SCHEMA_VERSION}."
                    )
            }

            // ── Step 5: SHA-256 checksum ──────────────────────────────────────
            val storedChecksum = root.getString("checksum")
            // Recompute over payload with checksum field set to ""
            root.put("checksum", "")
            val payloadForVerification = root.toString()
            val isChecksumValid = BackupChecksumHelper.verifyChecksum(payloadForVerification, storedChecksum)
            // Restore original checksum field (not critical here but good hygiene)
            root.put("checksum", storedChecksum)

            if (!isChecksumValid) {
                return AppResult.Error(
                    "⚠️ Chữ ký SHA-256 không khớp — file có thể đã bị chỉnh sửa. " +
                        "Từ chối khôi phục để bảo vệ dữ liệu của bạn."
                )
            }

            // ── Step 6: Build preview summary ────────────────────────────────
            val exportedAt = Instant.ofEpochMilli(root.getLong("exportedAt"))
            val appVersion = root.getString("appVersion")
            val appVersionCode = if (root.has("appVersionCode")) root.getInt("appVersionCode") else 0
            val exportedByUid = root.getString("exportedByUid")

            val wallets = root.getJSONArray("wallets")
            val transactions = root.getJSONArray("transactions")
            val categories = if (root.has("categories")) root.getJSONArray("categories") else null
            val budgets = if (root.has("budgets")) root.getJSONArray("budgets") else null
            val debts = if (root.has("debts")) root.getJSONArray("debts") else null
            val goals = if (root.has("goals")) root.getJSONArray("goals") else null
            val reminders = if (root.has("reminders")) root.getJSONArray("reminders") else null
            val deals = if (root.has("deals")) root.getJSONArray("deals") else null

            // Determine transaction date range
            var rangeStart: Instant? = null
            var rangeEnd: Instant? = null
            for (i in 0 until transactions.length()) {
                val tx = transactions.getJSONObject(i)
                if (tx.has("date")) {
                    try {
                        val d = Instant.parse(tx.getString("date"))
                        if (rangeStart == null || d.isBefore(rangeStart)) rangeStart = d
                        if (rangeEnd == null || d.isAfter(rangeEnd)) rangeEnd = d
                    } catch (_: Exception) { /* skip malformed dates */ }
                }
            }

            val sizeLabel = formatSizeLabel(fileSizeBytes.takeIf { it > 0L }
                ?: content.toByteArray(Charsets.UTF_8).size.toLong())

            val summary = BackupPreviewSummary(
                exportedAt = exportedAt,
                appVersion = appVersion,
                appVersionCode = appVersionCode,
                schemaVersion = schemaVersion,
                isCrossAccount = exportedByUid != currentUserId,
                exportedByUid = exportedByUid,
                walletCount = wallets.length(),
                transactionCount = transactions.length(),
                categoryCount = categories?.length() ?: 0,
                budgetCount = budgets?.length() ?: 0,
                debtCount = debts?.length() ?: 0,
                goalCount = goals?.length() ?: 0,
                reminderCount = reminders?.length() ?: 0,
                dealCount = deals?.length() ?: 0,
                dateRangeStart = rangeStart,
                dateRangeEnd = rangeEnd,
                estimatedSizeLabel = sizeLabel,
                isChecksumValid = true, // we already returned error if false
                hasSalaryCycleConfig = root.has("salaryCycleConfig") && !root.isNull("salaryCycleConfig"),
                hasSavingSpinConfig = root.has("savingSpinConfig") && !root.isNull("savingSpinConfig"),
            )

            AppResult.Success(summary)
        } catch (e: Exception) {
            AppResult.Error("Lỗi không xác định khi đọc file backup: ${e.localizedMessage}", e)
        }
    }

    operator fun invoke(
        content: String,
        fileSizeBytes: Long = 0L,
        currentUserId: String = defaultUserId,
    ): AppResult<BackupPreviewSummary> {
        return invoke(content.byteInputStream(Charsets.UTF_8), fileSizeBytes, currentUserId)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun formatSizeLabel(bytes: Long): String {
        return when {
            bytes < 1_024L -> "$bytes B"
            bytes < 1_048_576L -> "~%.1f KB".format(bytes / 1_024.0)
            else -> "~%.1f MB".format(bytes / 1_048_576.0)
        }
    }
}
