package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.SavingDestination
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.SavingSpinStatus
import com.finlux.app.domain.repository.SavingSpinRepository
import javax.inject.Inject

class CompleteSavingSpinUseCase @Inject constructor(
    private val repository: SavingSpinRepository,
) {
    suspend operator fun invoke(
        session: SavingSpinSession,
        destination: SavingDestination,
    ): AppResult<Unit> {
        if (session.status !in setOf(SavingSpinStatus.SPUN_PENDING, SavingSpinStatus.SNOOZED) || session.selectedAmount == null) {
            return AppResult.Error("Hãy quay và chốt mệnh giá trước khi xác nhận")
        }
        if (!destination.enabled) return AppResult.Error("Nơi tiết kiệm đã bị tắt")
        return repository.completeSession(session.scheduleKey, destination.id, destination.method)
    }
}
