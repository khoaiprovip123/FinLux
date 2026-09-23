package com.finlux.app.core.common

/**
 * System-wide operational and technical limits for FinLux.
 */
object AppSystemConfig {
    object Firestore {
        /** Maximum document references deleted or written in a single batch (safety limit <= 500) */
        const val BATCH_WRITE_CHUNK_SIZE = 400

        /** Threshold for large transaction volume warning in backup */
        const val MAX_BACKUP_TRANSACTIONS_WARNING = 50_000
    }

    object AlarmRequestCodes {
        const val SALARY_PAYDAY_PRIMARY = 9925
        const val SALARY_PAYDAY_SECONDARY = 9926
        const val SAVING_SPIN_PRIMARY = 73_091
        const val SAVING_SPIN_SECONDARY = 73_092
        const val SAVING_SPIN_DAILY_REMINDER = 73_091
        const val SAVING_SPIN_SNOOZE = 73_092
    }
}
