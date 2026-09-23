package com.finlux.app.domain.model.backup

/**
 * Two restore strategies supported by RestoreBackupUseCase (Phase 2).
 *
 * FULL_OVERWRITE — Wipe & Replace:
 *   Deletes ALL existing data for the current user, then writes the entire snapshot.
 *   Requires a two-step confirmation dialog in the UI (destructive action).
 *
 * SMART_MERGE — Non-destructive merge:
 *   Inserts records not present locally; updates records whose snapshot.updatedAt
 *   is newer than the local copy; skips records that are already up-to-date.
 *   SystemCategories are NEVER overwritten regardless of strategy.
 */
enum class RestoreStrategy {
    FULL_OVERWRITE,
    SMART_MERGE,
}

/**
 * Summary report returned by RestoreBackupUseCase (Phase 2) after a restore completes.
 * Displayed to the user in a result dialog / snackbar.
 */
data class RestoreReport(

    val strategy: RestoreStrategy,

    // ── Restored entity counts ────────────────────────────────────────────────
    val walletsRestored: Int,
    val transactionsRestored: Int,
    val categoriesRestored: Int,
    val budgetsRestored: Int,
    val debtsRestored: Int,
    val debtPaymentsRestored: Int,
    val goalsRestored: Int,
    val remindersRestored: Int,
    val dealsRestored: Int,

    /**
     * SMART_MERGE only: number of records skipped because the local version
     * was already newer than (or equal to) the snapshot version.
     */
    val skippedCount: Int,

    /**
     * SMART_MERGE only: number of records that were merged (local record
     * replaced by the snapshot version because snapshot was newer).
     */
    val conflictsResolved: Int,

    /**
     * Result of WalletBalanceAudit run after restore.
     * True = all wallet balances match expectations.
     * False = at least one discrepancy detected (see [balanceDiscrepancies]).
     */
    val balanceAuditPassed: Boolean,

    /**
     * Human-readable descriptions of any wallet balance discrepancies found
     * by WalletBalanceAudit. Empty when [balanceAuditPassed] is true.
     */
    val balanceDiscrepancies: List<String>,

    /** Total duration of the restore operation in milliseconds. */
    val durationMs: Long,

    /**
     * Any non-fatal errors encountered (e.g. a specific Firestore batch chunk
     * that failed after 3 retries). The restore continues despite these errors.
     */
    val errors: List<String> = emptyList(),
)
