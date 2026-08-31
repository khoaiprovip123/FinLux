package com.finlux.app.presentation.savingspin.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.domain.model.SavingSpinReportRange
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.SavingSpinRepository
import com.finlux.app.domain.usecase.FinancialPeriodResolver
import com.finlux.app.domain.usecase.GetSavingSpinReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class SavingSpinReportViewModel @Inject constructor(
    private val getReportUseCase: GetSavingSpinReportUseCase,
    private val salaryCycleRepository: SalaryCycleRepository,
    private val periodResolver: FinancialPeriodResolver,
) : ViewModel() {

    private val selectedFilter = MutableStateFlow(SavingSpinReportFilter.SEVEN_DAYS)
    private val zoneId = ZoneId.of("Asia/Ho_Chi_Minh")

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SavingSpinReportUiState> = combine(
        selectedFilter,
        salaryCycleRepository.observeConfig(),
    ) { filter, salaryConfig ->
        Pair(filter, salaryConfig)
    }.flatMapLatest { (filter, salaryConfig) ->
        val now = Instant.now()
        val range = when (filter) {
            SavingSpinReportFilter.SEVEN_DAYS -> SavingSpinReportRange(
                fromInclusive = now.minus(7, ChronoUnit.DAYS),
                toExclusive = now.plus(1, ChronoUnit.DAYS),
            )
            SavingSpinReportFilter.THIRTY_DAYS -> SavingSpinReportRange(
                fromInclusive = now.minus(30, ChronoUnit.DAYS),
                toExclusive = now.plus(1, ChronoUnit.DAYS),
            )
            SavingSpinReportFilter.THIS_MONTH -> {
                val startOfMonth = LocalDate.now(zoneId).withDayOfMonth(1).atStartOfDay(zoneId).toInstant()
                SavingSpinReportRange(
                    fromInclusive = startOfMonth,
                    toExclusive = now.plus(1, ChronoUnit.DAYS),
                )
            }
            SavingSpinReportFilter.SALARY_CYCLE -> {
                val period = periodResolver.resolveCurrentPeriod(salaryConfig, now)
                SavingSpinReportRange(
                    fromInclusive = period.start,
                    toExclusive = period.endExclusive,
                )
            }
        }
        getReportUseCase(range, zoneId)
    }.combine(selectedFilter) { report, filter ->
        SavingSpinReportUiState(
            isLoading = false,
            selectedFilter = filter,
            report = report,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SavingSpinReportUiState(isLoading = true),
    )

    fun selectFilter(filter: SavingSpinReportFilter) {
        selectedFilter.value = filter
    }
}
