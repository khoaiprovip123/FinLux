package com.finlux.app.presentation.reports

import java.time.LocalDate

enum class ReportPeriod(val label: String) {
    TODAY("Hôm nay"),
    YESTERDAY("Hôm qua"),
    DAY("Ngày"),
    WEEK("Tuần này"),
    LAST_7_DAYS("7 ngày qua"),
    SALARY_CYCLE("Kỳ lương"),
    MONTH("Tháng"),
    QUARTER("Quý"),
    YEAR("Năm"),
    CUSTOM("Tùy chọn"),
}

data class ReportRange(val start: LocalDate, val end: LocalDate)
