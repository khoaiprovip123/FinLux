package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.CycleRolloverRule
import com.finlux.app.domain.model.PaydayRuleType
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.SalaryScheduleType
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.abs

class ValidateSalaryCycleConfigUseCase @Inject constructor() {
    operator fun invoke(config: SalaryCycleConfig): AppResult<Unit> {
        if (!config.enabled) return AppResult.Success(Unit)

        if (config.paydayRuleType == PaydayRuleType.DAY_OF_MONTH && config.paydayDay !in 1..31) {
            return AppResult.Error("Ngày nhận lương phải từ 1 đến 31")
        }

        if (config.scheduleType == SalaryScheduleType.SEMI_MONTHLY) {
            val d2 = config.secondPaydayDay
            if (d2 == null || d2 !in 1..31) {
                return AppResult.Error("Ngày nhận lương đợt 2 phải từ 1 đến 31")
            }

            if (d2 == config.paydayDay) {
                return AppResult.Error("Hai ngày nhận lương không được trùng nhau")
            }

            val diff = abs(config.paydayDay - d2)
            val circularDiff = minOf(diff, 30 - diff)
            if (circularDiff < 5) {
                return AppResult.Error("Hai đợt nhận lương phải cách nhau tối thiểu 5 ngày")
            }

            val p1 = config.expectedSalary?.value
            val p2 = config.secondExpectedSalary?.value
            if (p1 == null || p1 <= 0L) {
                return AppResult.Error("Mức lương dự kiến đợt 1 phải lớn hơn 0")
            }
            if (p2 == null || p2 <= 0L) {
                return AppResult.Error("Mức lương dự kiến đợt 2 phải lớn hơn 0")
            }
        } else {
            val expectedSalary = config.expectedSalary?.value
            if (expectedSalary != null && expectedSalary <= 0L) {
                return AppResult.Error("Mức lương dự kiến phải lớn hơn 0")
            }
        }

        if (
            config.rolloverRule == CycleRolloverRule.MOVE_TO_SAVINGS &&
            config.savingsWalletId.isNullOrBlank()
        ) {
            return AppResult.Error("Vui lòng chọn ví tiết kiệm nhận tiền dư")
        }

        if (config.financeTimeZone.isBlank() || config.financeTimeZone.length > 64) {
            return AppResult.Error("Múi giờ tài chính không hợp lệ")
        }

        if (runCatching { ZoneId.of(config.financeTimeZone) }.isFailure) {
            return AppResult.Error("Múi giờ tài chính không hợp lệ")
        }

        return AppResult.Success(Unit)
    }
}

