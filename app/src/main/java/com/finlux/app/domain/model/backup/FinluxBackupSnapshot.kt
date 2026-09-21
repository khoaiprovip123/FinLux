package com.finlux.app.domain.model.backup

/**
 * Root snapshot container for FinLux Full Data Backup.
 *
 * Design decisions (approved 2026-09-21):
 *  - No AES encryption: plain JSON + SHA-256 checksum for integrity verification.
 *  - receiptImageUrl: kept as-is (may be a broken URL on cross-account restore — accepted).
 *  - All Instant / Timestamp fields serialised as ISO 8601 UTC strings to avoid TZ drift.
 *  - Checksum is computed over the JSON produced when this field is set to "".
 *
 * Schema versioning:
 *  - CURRENT_SCHEMA_VERSION = 1 (initial release)
 *  - MIN_SUPPORTED_SCHEMA_VERSION = 1
 */
data class FinluxBackupSnapshot(

    // ── METADATA ─────────────────────────────────────────────────────────────
    /** JSON schema version. Increment when breaking changes are made to the snapshot structure. */
    val schemaVersion: Int,

    /** App versionName at export time, e.g. "1.25.6". */
    val appVersion: String,

    /** App versionCode at export time, e.g. 180. */
    val appVersionCode: Int,

    /** Unix epoch milliseconds (UTC) when this snapshot was created. */
    val exportedAt: Long,

    /** Firebase UID of the account that created this backup. Used to detect cross-account imports. */
    val exportedByUid: String,

    /**
     * SHA-256 hex digest of the payload JSON produced with this field set to "".
     * Allows ValidateBackupUseCase to detect file tampering.
     */
    val checksum: String,

    /** Uncompressed payload size in bytes — shown to the user in the Preview UI. */
    val payloadSizeBytes: Long,

    // ── PAYLOAD (9 entity collections + 2 singleton configs) ─────────────────
    val wallets: List<WalletSnapshot>,
    val categories: List<CategorySnapshot>,
    val transactions: List<TransactionSnapshot>,
    val budgets: List<BudgetSnapshot>,
    val debts: List<DebtSnapshot>,
    val debtPayments: List<DebtPaymentSnapshot>,
    val goals: List<GoalSnapshot>,
    val reminders: List<ReminderSnapshot>,
    val deals: List<DealSnapshot>,

    /** Null when the user has never configured salary cycle. */
    val salaryCycleConfig: SalaryCycleConfigSnapshot?,

    /** Null when the user has never opened the Saving Spin feature. */
    val savingSpinConfig: SavingSpinConfigSnapshot?,
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION: Int = 1
        const val MIN_SUPPORTED_SCHEMA_VERSION: Int = 1
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WALLET
// ─────────────────────────────────────────────────────────────────────────────

data class WalletSnapshot(
    val id: String,
    val name: String,
    /** "cash" | "bank" | "ewallet" | "card" | "investment" | "other" */
    val type: String,
    /** Balance in VNĐ (integer, no decimal). */
    val balance: Long,
    val colorHex: String,
    val isDefault: Boolean,
    /** ISO 8601 UTC, e.g. "2026-01-15T07:30:00Z" */
    val createdAt: String,
    /** "active" | "archived" */
    val status: String = "active",
)

// ─────────────────────────────────────────────────────────────────────────────
// CATEGORY
// ─────────────────────────────────────────────────────────────────────────────

data class CategorySnapshot(
    val id: String,
    val name: String,
    /** "income" | "expense" */
    val type: String,
    val icon: String,
    val colorHex: String,
    /** true = system-seeded default; must not be overwritten on restore. */
    val isDefault: Boolean,
    val isEssential: Boolean = true,
    val createdAt: String,
)

// ─────────────────────────────────────────────────────────────────────────────
// TRANSACTION
// ─────────────────────────────────────────────────────────────────────────────

data class TransactionSnapshot(
    val id: String,
    /** "income" | "expense" | "transfer_out" | "transfer_in" */
    val type: String,
    val amount: Long,
    val categoryId: String?,
    val walletId: String,
    val relatedWalletId: String?,
    val dealId: String?,
    /** "OUTLAY_CAPITAL" | "PRINCIPAL_RECOVERY" | "CAPITAL_GAIN" | "CAPITAL_LOSS" | null */
    val dealFlowType: String?,
    val note: String,
    /**
     * Firebase Storage URL. May be a broken URL if restored into a different account.
     * Kept as-is per design decision: no re-upload for performance / offline-first.
     */
    val receiptImageUrl: String?,
    /** ISO 8601 UTC — the user-selected transaction date. */
    val date: String,
    val createdAt: String,
    val updatedAt: String,
)

// ─────────────────────────────────────────────────────────────────────────────
// BUDGET
// ─────────────────────────────────────────────────────────────────────────────

data class BudgetSnapshot(
    /** Format: {categoryId}_{periodKey}, e.g. "food_month:2026-09" */
    val id: String,
    val categoryId: String,
    /** "month:2026-09" | "salary:2026-09-01" */
    val periodKey: String,
    val limitAmount: Long,
    /** Denormalised value — recalculated during restore via WalletBalanceAudit. */
    val spentAmount: Long,
    val notified80: Boolean,
    val notified100: Boolean,
)

// ─────────────────────────────────────────────────────────────────────────────
// DEBT
// ─────────────────────────────────────────────────────────────────────────────

data class DebtSnapshot(
    val id: String,
    val name: String,
    /** "CREDIT_CARD" | "BANK_LOAN" | "PERSONAL_LOAN" | "INSTALLMENT" */
    val type: String,
    val totalAmount: Long,
    val remainingBalance: Long,
    val interestRateApr: Double,
    val minimumPayment: Long,
    /** Nullable: day of month (1..31). */
    val dueDate: Int?,
    val statementDate: Int?,
    val linkedWalletId: String?,
    val gracePeriodDays: Int,
    val colorHex: String,
    val isReminderEnabled: Boolean,
    val reminderDaysBefore: Int,
    val isSettled: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

data class DebtPaymentSnapshot(
    val id: String,
    /** Foreign key used to rebuild the subcollection under debts/{debtId}/payments on restore. */
    val debtId: String,
    val walletId: String,
    val amount: Long,
    val principalPaid: Long,
    val interestPaid: Long,
    val paymentDate: String,
    val note: String,
    val isCreditCardPayment: Boolean = false,
)

// ─────────────────────────────────────────────────────────────────────────────
// GOAL
// ─────────────────────────────────────────────────────────────────────────────

data class GoalSnapshot(
    val id: String,
    val name: String,
    val targetAmount: Long,
    val savedAmount: Long,
    val deadline: String,
    val category: String,
    val monthlyContribution: Long,
    /**
     * Local device URI — not transferable across devices/accounts.
     * Set to null on restore to avoid broken file references.
     */
    val imageUri: String?,
    val createdAt: String,
)

// ─────────────────────────────────────────────────────────────────────────────
// REMINDER
// ─────────────────────────────────────────────────────────────────────────────

data class ReminderSnapshot(
    val id: String,
    val title: String,
    val amount: Long,
    val categoryId: String,
    val walletId: String,
    /** "DAILY" | "WEEKLY" | "MONTHLY" */
    val recurrence: String,
    val startDate: String,
    val enabled: Boolean,
    val nextTriggerDate: String,
)

// ─────────────────────────────────────────────────────────────────────────────
// DEAL
// ─────────────────────────────────────────────────────────────────────────────

data class DealSnapshot(
    val id: String,
    val title: String,
    val description: String,
    /** "INVESTMENT" | "LENDING" */
    val category: String,
    val targetAmount: Long,
    val totalCapitalOutlay: Long,
    val totalRecovered: Long,
    val writtenOffCapital: Long,
    val netProfitLoss: Long,
    /** "ACTIVE" | "COMPLETED" | "CANCELLED" */
    val status: String,
    val startDate: String,
    val endDate: String?,
    val createdAt: String,
    val updatedAt: String,
)

// ─────────────────────────────────────────────────────────────────────────────
// SALARY CYCLE CONFIG
// ─────────────────────────────────────────────────────────────────────────────

data class SalaryCycleConfigSnapshot(
    val enabled: Boolean,
    /** "MONTHLY_ONCE" | "SEMI_MONTHLY" */
    val scheduleType: String,
    /** "DAY_OF_MONTH" | "FIRST_DAY_OF_MONTH" | "LAST_DAY_OF_MONTH" */
    val paydayRuleType: String,
    val paydayDay: Int,
    val salaryWalletId: String?,
    val expectedSalary: Long?,
    val secondPaydayDay: Int?,
    val secondSalaryWalletId: String?,
    val secondExpectedSalary: Long?,
    val savingsWalletId: String?,
    /** "KEEP_IN_WALLET" | "MOVE_TO_SAVINGS" | "ASK_EACH_CYCLE" */
    val rolloverRule: String,
    /** "CALENDAR_MONTH" | "SALARY_CYCLE" */
    val budgetPeriodBasis: String,
    /** IANA timezone string, e.g. "Asia/Ho_Chi_Minh" */
    val financeTimeZone: String,
    val updatedAt: String,
)

// ─────────────────────────────────────────────────────────────────────────────
// SAVING SPIN CONFIG
// ─────────────────────────────────────────────────────────────────────────────

data class SavingSpinConfigSnapshot(
    val enabled: Boolean,
    val showOnHome: Boolean,
    val minAmount: Long,
    val maxAmount: Long,
    val stepAmount: Long,
    val slotCount: Int,
    /** "DAILY" | "SELECTED_WEEKDAYS" | "WEEKLY" | "SALARY_CYCLE" */
    val frequency: String,
    val selectedWeekdays: List<Int>,
    val weeklyDay: Int,
    val reminderHour: Int,
    val reminderMinute: Int,
    val reminderEnabled: Boolean,
    val snoozeEnabled: Boolean,
    val allowSkip: Boolean,
    val defaultDestinationId: String?,
    val schemaVersion: Int,
    val updatedAt: String,
)
