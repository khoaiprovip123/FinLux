package com.finlux.app.domain.model

import java.time.ZoneId

/**
 * Single Source of Truth for core financial business rules and invariants in FinLux.
 */
object FinanceBusinessConstants {
    object Budget {
        const val WARNING_PERCENT: Long = 80L
        const val EXCEEDED_PERCENT: Long = 100L
        const val WARNING_RATIO: Float = 0.8f
        const val EXCEEDED_RATIO: Float = 1.0f
    }

    object Goals {
        const val DEFAULT_DEADLINE_DAYS: Long = 180L
    }

    object Debt {
        const val MIN_PAYMENT_RATE: Double = 0.03
        const val MIN_PAYMENT_AMOUNT: Long = 50_000L
    }

    object Timezone {
        const val DEFAULT_ZONE_NAME: String = "Asia/Ho_Chi_Minh"
        val DEFAULT_ZONE_ID: ZoneId = ZoneId.of(DEFAULT_ZONE_NAME)
    }
}
