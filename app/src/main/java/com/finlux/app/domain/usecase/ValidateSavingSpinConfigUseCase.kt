package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.model.SavingSpinFrequency
import javax.inject.Inject

enum class SavingSpinValidationCode {
    MIN_NOT_POSITIVE,
    MAX_BELOW_MIN,
    MAX_TOO_LARGE,
    MIN_NOT_MULTIPLE,
    MAX_NOT_MULTIPLE,
    UNSUPPORTED_SLOT_COUNT,
    INSUFFICIENT_CANDIDATES,
    INVALID_WEEKDAYS,
    INVALID_WEEKLY_DAY,
    INVALID_REMINDER_TIME,
}

data class SavingSpinValidationError(
    val code: SavingSpinValidationCode,
    val message: String,
)

data class SavingSpinValidationResult(
    val errors: List<SavingSpinValidationError>,
) {
    val isValid: Boolean = errors.isEmpty()
}

class ValidateSavingSpinConfigUseCase @Inject constructor() {
    operator fun invoke(config: SavingSpinConfig): SavingSpinValidationResult {
        val errors = buildList {
            val min = config.minAmount.value
            val max = config.maxAmount.value
            val step = config.step.amount

            if (min <= 0L) add(error(SavingSpinValidationCode.MIN_NOT_POSITIVE, "Số tiền tối thiểu phải lớn hơn 0"))
            if (max < min) add(error(SavingSpinValidationCode.MAX_BELOW_MIN, "Số tiền tối đa phải từ mức tối thiểu trở lên"))
            if (max > MAX_AMOUNT) add(error(SavingSpinValidationCode.MAX_TOO_LARGE, "Số tiền tối đa không vượt quá 15 chữ số"))
            if (min > 0L && min % step != 0L) add(error(SavingSpinValidationCode.MIN_NOT_MULTIPLE, "Mức tối thiểu phải chia hết cho bước tiền"))
            if (max > 0L && max % step != 0L) add(error(SavingSpinValidationCode.MAX_NOT_MULTIPLE, "Mức tối đa phải chia hết cho bước tiền"))
            if (config.slotCount !in ALLOWED_SLOT_COUNTS) add(error(SavingSpinValidationCode.UNSUPPORTED_SLOT_COUNT, "Số ô chỉ hỗ trợ 6, 8, 10 hoặc 12"))

            if (min > 0L && max >= min) {
                val candidateCount = ((max - min) / step) + 1L
                if (candidateCount < config.slotCount) {
                    add(error(SavingSpinValidationCode.INSUFFICIENT_CANDIDATES, "Khoảng tiền không đủ mệnh giá cho số ô đã chọn"))
                }
            }

            if (config.frequency == SavingSpinFrequency.SELECTED_WEEKDAYS &&
                (config.selectedWeekdays.isEmpty() || config.selectedWeekdays.any { it !in 1..7 })
            ) {
                add(error(SavingSpinValidationCode.INVALID_WEEKDAYS, "Hãy chọn ít nhất một thứ hợp lệ trong tuần"))
            }
            if (config.weeklyDay !in 1..7) add(error(SavingSpinValidationCode.INVALID_WEEKLY_DAY, "Ngày quay hằng tuần không hợp lệ"))
            if (config.reminderHour !in 0..23 || config.reminderMinute !in 0..59) {
                add(error(SavingSpinValidationCode.INVALID_REMINDER_TIME, "Giờ nhắc phải nằm trong một ngày"))
            }
        }
        return SavingSpinValidationResult(errors)
    }

    private fun error(code: SavingSpinValidationCode, message: String) = SavingSpinValidationError(code, message)

    companion object {
        const val MAX_AMOUNT = 999_999_999_999_999L
        val ALLOWED_SLOT_COUNTS = setOf(6, 8, 10, 12)
    }
}
