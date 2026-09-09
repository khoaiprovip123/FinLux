package com.finlux.app.domain.model

import java.time.Instant

enum class SavingSpinStep(val amount: Long) {
    FIVE_THOUSAND(5_000L),
    TEN_THOUSAND(10_000L),
}

enum class SavingSpinStatus {
    READY,
    SPUN_PENDING,
    COMPLETED,
    SNOOZED,
    SKIPPED,
}

enum class SavingMethod {
    CASH,
    BANK_TRANSFER,
}

enum class SavingSpinFrequency {
    DAILY,
    SELECTED_WEEKDAYS,
    WEEKLY,
    SALARY_CYCLE,
}

data class SavingSpinConfig(
    val enabled: Boolean = false,
    val showOnHome: Boolean = true,
    val minAmount: Money = Money(10_000L),
    val maxAmount: Money = Money(100_000L),
    val step: SavingSpinStep = SavingSpinStep.FIVE_THOUSAND,
    val slotCount: Int = 8,
    val frequency: SavingSpinFrequency = SavingSpinFrequency.DAILY,
    val selectedWeekdays: Set<Int> = emptySet(),
    val weeklyDay: Int = 1,
    val reminderEnabled: Boolean = true,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val snoozeEnabled: Boolean = true,
    val allowSkip: Boolean = true,
    val defaultDestinationId: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)

data class SavingDestination(
    val id: String = "",
    val name: String,
    val method: SavingMethod,
    val linkedWalletId: String? = null,
    val institutionId: String? = null,
    val accountHint: String? = null,
    val enabled: Boolean = true,
    val icon: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)

data class SavingSpinSession(
    val id: String,
    val scheduleKey: String,
    val wheelValues: List<Money>,
    val selectedIndex: Int? = null,
    val selectedAmount: Money? = null,
    val status: SavingSpinStatus = SavingSpinStatus.READY,
    val destinationId: String? = null,
    val method: SavingMethod? = null,
    val spunAt: Instant? = null,
    val completedAt: Instant? = null,
    val skippedAt: Instant? = null,
    val snoozedUntil: Instant? = null,
    val transactionId: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)

data class SavingSpinStreakResult(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
)

data class SavingSpinReportSummary(
    val savedAmount: Money = Money(0L),
    val completedCount: Int = 0,
    val skippedCount: Int = 0,
    val scheduledCount: Int = 0,
    val completionRate: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val averageAmount: Money = Money(0L),
    val highestAmount: Money = Money(0L),
    val lowestAmount: Money = Money(0L),
)

data class SavingSpinDailyTotal(
    val epochDay: Long,
    val amount: Money,
)

data class SavingSpinDestinationTotal(
    val destinationId: String,
    val destinationName: String,
    val amount: Money,
)

data class SavingSpinReportRange(
    val fromInclusive: Instant,
    val toExclusive: Instant,
)

data class SavingSpinReport(
    val summary: SavingSpinReportSummary,
    val dailyTotals: List<SavingSpinDailyTotal>,
    val destinationTotals: List<SavingSpinDestinationTotal>,
    val sessions: List<SavingSpinSession>,
)

