package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.SavingSpinStatus
import com.finlux.app.domain.repository.SavingSpinRepository
import java.security.SecureRandom
import javax.inject.Inject

class SpinSavingWheelUseCase @Inject constructor(
    private val repository: SavingSpinRepository,
) {
    suspend operator fun invoke(session: SavingSpinSession): AppResult<SavingSpinSession> {
        if (session.status != SavingSpinStatus.READY) {
            return AppResult.Error("Lượt tiết kiệm này đã được quay")
        }
        if (session.wheelValues.isEmpty()) {
            return AppResult.Error("Vòng quay chưa có mệnh giá")
        }
        return repository.lockSpinResult(
            scheduleKey = session.scheduleKey,
            selectedIndex = secureRandom.nextInt(session.wheelValues.size),
        )
    }

    companion object {
        private val secureRandom = SecureRandom()
    }
}
