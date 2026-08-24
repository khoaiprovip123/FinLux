package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.CycleRolloverRule
import com.finlux.app.domain.model.PaydayRuleType
import com.finlux.app.domain.model.SalaryCycleConfig
import java.time.ZoneId
import javax.inject.Inject

class ValidateSalaryCycleConfigUseCase @Inject constructor() {
    operator fun invoke(config: SalaryCycleConfig): AppResult<Unit> {
        if (!config.enabled) return AppResult.Success(Unit)

        if (config.paydayRuleType == PaydayRuleType.DAY_OF_MONTH && config.paydayDay !in 1..31) {
            return AppResult.Error("Ngày nhận lương phải từ 1 đến 31")
        }

        val expectedSalary = config.expectedSalary?.value
        if (expectedSalary != null && expectedSalary <= 0L) {
            return AppResult.Error("Mức lương dự kiến phải lớn hơn 0")
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
