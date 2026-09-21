package com.finlux.app.presentation.settings.backup

import android.net.Uri
import com.finlux.app.domain.model.backup.BackupPreviewSummary
import com.finlux.app.domain.model.backup.RestoreReport
import com.finlux.app.domain.model.backup.RestoreStrategy

/**
 * Quick summary of current local dataset for the Export card.
 */
data class DataStats(
    val walletCount: Int = 0,
    val transactionCount: Int = 0,
    val categoryCount: Int = 0,
    val budgetCount: Int = 0,
    val debtCount: Int = 0,
    val goalCount: Int = 0,
    val reminderCount: Int = 0,
    val dealCount: Int = 0,
    val estimatedSizeLabel: String = "~1.5 MB",
)

/**
 * UI State for [BackupRestoreSheet].
 */
data class BackupRestoreUiState(
    val currentUserEmail: String? = null,
    val currentUserId: String = "",
    val isCloudSyncing: Boolean = true,
    val dataStats: DataStats = DataStats(),
    val isStatsLoading: Boolean = false,

    // Export flow
    val isExporting: Boolean = false,
    val exportedFileUri: Uri? = null,
    val exportSuccessMessage: String? = null,
    val exportError: String? = null,

    // File selection & Validation flow
    val selectedFileName: String? = null,
    val selectedFileSizeBytes: Long = 0L,
    val isValidating: Boolean = false,
    val previewSummary: BackupPreviewSummary? = null,
    val validationError: String? = null,
    val cachedJsonForRestore: String? = null,

    // Restore flow
    val selectedStrategy: RestoreStrategy = RestoreStrategy.SMART_MERGE,
    val isRestoring: Boolean = false,
    val showFullOverwriteConfirmDialog: Boolean = false,
    val restoreReport: RestoreReport? = null,
    val restoreError: String? = null,
)
