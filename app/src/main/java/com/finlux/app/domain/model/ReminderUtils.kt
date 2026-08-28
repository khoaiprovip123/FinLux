package com.finlux.app.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Tiện ích tính toán chu kỳ nhắc nhở thanh toán chính xác, bảo toàn 100% mốc giờ/phút gốc (Zero Time Drift).
 */
object ReminderUtils {

    /**
     * Tính toán mốc thời gian kích hoạt kế tiếp ([Instant]) dựa trên [startDate] và [recurrence],
     * bảo đảm mốc thời gian trả về luôn nằm trong tương lai ([afterInstant]) và giữ nguyên [LocalTime] (giờ:phút:giây) gốc.
     *
     * @param startDate Mốc thời gian ban đầu khi người dùng thiết lập nhắc nhở (chứa ngày và giờ:phút gốc).
     * @param recurrence Chu kỳ lặp lại (Hàng ngày, Hàng tuần, Hàng tháng).
     * @param afterInstant Mốc thời gian tham chiếu so sánh (mặc định là [Instant.now]).
     * @param zoneId Múi giờ hệ thống để phân rã ngày và giờ (mặc định là [ZoneId.systemDefault]).
     */
    fun computeNextTriggerDate(
        startDate: Instant,
        recurrence: ReminderRecurrence,
        afterInstant: Instant = Instant.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Instant {
        val startZdt = startDate.atZone(zoneId)
        val targetTime = startZdt.toLocalTime()
        val startLocalDate = startZdt.toLocalDate()

        var step = 0L
        var candidateDate = startLocalDate
        var candidateInstant = candidateDate.atTime(targetTime).atZone(zoneId).toInstant()

        // Nếu candidateInstant chưa vượt qua afterInstant, tịnh tiến theo chu kỳ từ mốc gốc
        while (!candidateInstant.isAfter(afterInstant)) {
            step++
            candidateDate = when (recurrence) {
                ReminderRecurrence.DAILY -> startLocalDate.plusDays(step)
                ReminderRecurrence.WEEKLY -> startLocalDate.plusWeeks(step)
                ReminderRecurrence.MONTHLY -> startLocalDate.plusMonths(step)
            }
            candidateInstant = candidateDate.atTime(targetTime).atZone(zoneId).toInstant()
        }

        return candidateInstant
    }
}
