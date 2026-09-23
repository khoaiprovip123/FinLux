package com.finlux.app.domain.model.backup

import java.time.Instant

/**
 * Concise summary of a validated backup file shown in the Restore UI Preview Card
 * (Khu vực 3 — BackupRestoreSheet) before the user commits to restoring.
 *
 * Produced by [ValidateBackupUseCase] on a successful validation pass.
 */
data class BackupPreviewSummary(

    /** When the snapshot was created. */
    val exportedAt: Instant,

    /** App versionName at export time, e.g. "1.25.6". */
    val appVersion: String,

    /** App versionCode at export time, e.g. 180. */
    val appVersionCode: Int,

    /** Schema version of the backup file. */
    val schemaVersion: Int,

    /**
     * True when [exportedByUid] != the currently signed-in UID.
     * UI shows a yellow warning banner and triggers ID-remapping during restore.
     */
    val isCrossAccount: Boolean,

    /** Firebase UID that created this backup. */
    val exportedByUid: String,

    // ── Entity counts (displayed in the preview card) ─────────────────────────
    val walletCount: Int,
    val transactionCount: Int,
    val categoryCount: Int,
    val budgetCount: Int,
    val debtCount: Int,
    val goalCount: Int,
    val reminderCount: Int,
    val dealCount: Int,

    /** Timestamp of the oldest transaction in the snapshot (null if no transactions). */
    val dateRangeStart: Instant?,

    /** Timestamp of the newest transaction in the snapshot (null if no transactions). */
    val dateRangeEnd: Instant?,

    /** Human-readable size label, e.g. "~2.4 MB". */
    val estimatedSizeLabel: String,

    /**
     * True when the SHA-256 checksum inside the file matches the recomputed checksum.
     * A false value means the file was tampered with and should be rejected.
     */
    val isChecksumValid: Boolean,

    /** True when a [SalaryCycleConfigSnapshot] is present in the backup. */
    val hasSalaryCycleConfig: Boolean,

    /** True when a [SavingSpinConfigSnapshot] is present in the backup. */
    val hasSavingSpinConfig: Boolean,
)
