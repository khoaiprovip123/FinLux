package com.finlux.app.presentation.savingspin.report

import com.finlux.app.domain.model.SavingSpinReport

enum class SavingSpinReportFilter(val label: String) {
    SEVEN_DAYS("7 ngày"),
    THIRTY_DAYS("30 ngày"),
    THIS_MONTH("Tháng này"),
    SALARY_CYCLE("Kỳ lương"),
}

data class SavingSpinReportUiState(
    val isLoading: Boolean = true,
    val selectedFilter: SavingSpinReportFilter = SavingSpinReportFilter.SEVEN_DAYS,
    val report: SavingSpinReport? = null,
    val errorMessage: String? = null,
)
