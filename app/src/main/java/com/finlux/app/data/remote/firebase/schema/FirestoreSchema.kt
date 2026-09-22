package com.finlux.app.data.remote.firebase.schema

/**
 * Single Source of Truth for Firestore collection paths, document names, and field keys.
 */
object FirestoreSchema {
    const val USERS = "users"

    object Collections {
        const val WALLETS = "wallets"
        const val TRANSACTIONS = "transactions"
        const val BUDGETS = "budgets"
        const val DEBTS = "debts"
        const val DEBT_PAYMENTS = "payments"
        const val GOALS = "goals"
        const val REMINDERS = "reminders"
        const val NOTIFICATIONS = "notifications"
        const val SALARY_ROLLOVERS = "salaryRollovers"
        const val SALARY_TIMELINE = "salaryCycleTimeline"
        const val FINANCIAL_PREFERENCES = "financialPreferences"
        const val DEALS = "deals"
        const val DEAL_INSTALLMENTS = "dealInstallments"
        const val SAVING_SPIN_CONFIGS = "savingSpinConfigs"
        const val SAVING_SPIN_DESTINATIONS = "savingSpinDestinations"
        const val SAVING_SPIN_SESSIONS = "savingSpinSessions"
        const val CATEGORIES = "categories"
    }

    object Documents {
        const val SALARY_CYCLE = "salaryCycle"
        const val DEFAULT = "default"
        const val DEFAULT_CONFIG = "default"
    }

    /**
     * Complete list of User subcollections for clean purge (GDPR) and backup export/restore.
     */
    val ALL_USER_SUBCOLLECTIONS = listOf(
        Collections.DEALS,
        Collections.DEBTS,
        Collections.TRANSACTIONS,
        Collections.BUDGETS,
        Collections.GOALS,
        Collections.REMINDERS,
        Collections.NOTIFICATIONS,
        Collections.SALARY_ROLLOVERS,
        Collections.SALARY_TIMELINE,
        Collections.FINANCIAL_PREFERENCES,
        Collections.SAVING_SPIN_CONFIGS,
        Collections.SAVING_SPIN_DESTINATIONS,
        Collections.SAVING_SPIN_SESSIONS,
        Collections.CATEGORIES,
        Collections.WALLETS,
    )

    object Fields {
        const val ID = "id"
        const val AMOUNT = "amount"
        const val SPENT_AMOUNT = "spentAmount"
        const val LIMIT_AMOUNT = "limitAmount"
        const val PERIOD_KEY = "periodKey"
        const val MONTH = "month"
        const val PERIOD_START = "periodStart"
        const val PERIOD_END_EXCLUSIVE = "periodEndExclusive"
        const val PERIOD_BASIS = "periodBasis"
        const val IS_DEFAULT = "isDefault"
        const val NOTIFIED_80 = "notified80"
        const val NOTIFIED_100 = "notified100"
        const val CATEGORY_ID = "categoryId"
        const val WALLET_ID = "walletId"
        const val TYPE = "type"
        const val DATE = "date"
        const val NOTE = "note"
        const val FINANCE_TIME_ZONE = "financeTimeZone"
    }
}
