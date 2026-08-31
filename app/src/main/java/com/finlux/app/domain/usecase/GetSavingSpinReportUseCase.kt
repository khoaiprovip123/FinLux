package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SavingSpinDailyTotal
import com.finlux.app.domain.model.SavingSpinDestinationTotal
import com.finlux.app.domain.model.SavingSpinReport
import com.finlux.app.domain.model.SavingSpinReportRange
import com.finlux.app.domain.model.SavingSpinReportSummary
import com.finlux.app.domain.model.SavingSpinStatus
import com.finlux.app.domain.repository.SavingSpinRepository
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetSavingSpinReportUseCase @Inject constructor(
    private val repository: SavingSpinRepository,
    private val calculateStreak: CalculateSavingSpinStreakUseCase,
) {
    operator fun invoke(
        range: SavingSpinReportRange,
        zoneId: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh"),
        activeScheduleKey: String? = null,
    ): Flow<SavingSpinReport> = combine(
        repository.observeSessions(range.fromInclusive, range.toExclusive),
        repository.observeDestinations(),
    ) { sessions, destinations ->
        val completed = sessions.filter { it.status == SavingSpinStatus.COMPLETED }
        val savedAmount = completed.sumOf { it.selectedAmount?.value ?: 0L }
        val skippedCount = sessions.count { it.status == SavingSpinStatus.SKIPPED }
        val destinationNames = destinations.associate { it.id to it.name }
        val dailyTotals = completed
            .groupBy { (it.completedAt ?: it.createdAt).atZone(zoneId).toLocalDate().toEpochDay() }
            .map { (epochDay, items) -> SavingSpinDailyTotal(epochDay, Money(items.sumOf { it.selectedAmount?.value ?: 0L })) }
            .sortedBy { it.epochDay }
        val destinationTotals = completed
            .filter { it.destinationId != null }
            .groupBy { requireNotNull(it.destinationId) }
            .map { (id, items) ->
                SavingSpinDestinationTotal(
                    destinationId = id,
                    destinationName = destinationNames[id] ?: "Nơi tiết kiệm",
                    amount = Money(items.sumOf { it.selectedAmount?.value ?: 0L }),
                )
            }
            .sortedByDescending { it.amount.value }

        SavingSpinReport(
            summary = SavingSpinReportSummary(
                savedAmount = Money(savedAmount),
                completedCount = completed.size,
                skippedCount = skippedCount,
                scheduledCount = sessions.size,
                completionRate = if (sessions.isEmpty()) 0 else (completed.size * 100 / sessions.size),
                currentStreak = calculateStreak(sessions, activeScheduleKey),
            ),
            dailyTotals = dailyTotals,
            destinationTotals = destinationTotals,
            sessions = sessions.sortedByDescending { it.createdAt },
        )
    }
}
