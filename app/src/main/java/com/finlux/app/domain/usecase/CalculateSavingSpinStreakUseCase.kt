package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.SavingSpinStatus
import javax.inject.Inject

class CalculateSavingSpinStreakUseCase @Inject constructor() {
    operator fun invoke(
        sessions: List<SavingSpinSession>,
        activeScheduleKey: String? = null,
    ): Int {
        var streak = 0
        for (session in sessions.sortedByDescending { it.createdAt }) {
            val isCurrentPending = session.scheduleKey == activeScheduleKey &&
                session.status in setOf(SavingSpinStatus.READY, SavingSpinStatus.SNOOZED)
            if (isCurrentPending) continue
            if (session.status == SavingSpinStatus.COMPLETED) streak++ else break
        }
        return streak
    }
}
