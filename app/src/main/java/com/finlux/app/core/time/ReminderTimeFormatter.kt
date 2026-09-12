package com.finlux.app.core.time

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Urgency tier for visual semantic color categorization.
 */
enum class ReminderCountdownTier {
    OVERDUE,   // Cấp 4: Quá hạn
    TODAY,     // Cấp 1: Trong hôm nay (sắp diễn ra, còn X phút, còn X giờ)
    TOMORROW,  // Cấp 2: Ngày mai
    FUTURE,    // Cấp 3: Từ 2 ngày trở lên (còn X ngày)
}

/**
 * Information model representing the countdown / relative time for a scheduled reminder.
 */
data class ReminderCountdownInfo(
    val label: String,
    val tier: ReminderCountdownTier = ReminderCountdownTier.FUTURE,
    val isOverdue: Boolean = (tier == ReminderCountdownTier.OVERDUE),
    val isUrgent: Boolean = (tier == ReminderCountdownTier.TODAY),
)

/**
 * Calculates human-friendly remaining time badge text and tier for scheduled reminders.
 *
 * Rules:
 * - Overdue: "quá hạn X ngày" (or "quá hạn X giờ / X phút" if within today) -> OVERDUE tier
 * - Under 1 minute: "sắp diễn ra" -> TODAY tier
 * - Under 1 hour: "còn X phút" -> TODAY tier
 * - Same day (today, >= 1 hour): "còn X giờ" -> TODAY tier
 * - Tomorrow: "ngày mai" -> TOMORROW tier
 * - Beyond tomorrow: "còn X ngày" -> FUTURE tier
 */
fun formatReminderCountdown(
    target: Instant,
    now: Instant = Instant.now(),
    zone: ZoneId = FinanceTime.defaultZone,
): ReminderCountdownInfo {
    val targetZdt = target.atZone(zone)
    val nowZdt = now.atZone(zone)
    val targetDate = targetZdt.toLocalDate()
    val today = nowZdt.toLocalDate()

    // 1. Quá hạn (target < now)
    if (target.isBefore(now)) {
        val overdueDays = ChronoUnit.DAYS.between(targetDate, today)
        val duration = Duration.between(target, now)
        val overdueLabel = when {
            overdueDays >= 1 -> "quá hạn $overdueDays ngày"
            duration.toHours() >= 1 -> "quá hạn ${duration.toHours()} giờ"
            duration.toMinutes() >= 1 -> "quá hạn ${duration.toMinutes()} phút"
            else -> "vừa quá hạn"
        }
        return ReminderCountdownInfo(
            label = overdueLabel,
            tier = ReminderCountdownTier.OVERDUE,
            isOverdue = true,
            isUrgent = false,
        )
    }

    // 2. Sắp tới / Tương lai (target >= now)
    val duration = Duration.between(now, target)
    val totalMinutes = duration.toMinutes()
    val totalHours = duration.toHours()
    val daysBetween = ChronoUnit.DAYS.between(today, targetDate)

    return when {
        // Dưới 1 phút
        totalMinutes < 1 -> ReminderCountdownInfo(
            label = "sắp diễn ra",
            tier = ReminderCountdownTier.TODAY,
            isOverdue = false,
            isUrgent = true,
        )

        // Dưới 1 giờ
        totalMinutes < 60 -> ReminderCountdownInfo(
            label = "còn $totalMinutes phút",
            tier = ReminderCountdownTier.TODAY,
            isOverdue = false,
            isUrgent = true,
        )

        // Cùng ngày (hôm nay) và >= 1 giờ
        targetDate == today -> ReminderCountdownInfo(
            label = "còn $totalHours giờ",
            tier = ReminderCountdownTier.TODAY,
            isOverdue = false,
            isUrgent = true,
        )

        // Ngày mai (targetDate == today + 1)
        targetDate == today.plusDays(1) -> ReminderCountdownInfo(
            label = "ngày mai",
            tier = ReminderCountdownTier.TOMORROW,
            isOverdue = false,
            isUrgent = false,
        )

        // Trên 1 ngày
        else -> ReminderCountdownInfo(
            label = "còn $daysBetween ngày",
            tier = ReminderCountdownTier.FUTURE,
            isOverdue = false,
            isUrgent = false,
        )
    }
}
