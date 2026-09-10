package com.finlux.app.domain.model

import java.time.Instant
import java.time.YearMonth

enum class DebtType {
    CREDIT_CARD,    // Thẻ tín dụng
    BANK_LOAN,      // Vay ngân hàng
    PERSONAL_LOAN,  // Vay cá nhân / người thân
    INSTALLMENT     // Trả góp / Mua trước trả sau (BNPL)
}

enum class PayoffStrategy {
    SNOWBALL,   // Trả nợ nhỏ nhất trước (tạo động lực tâm lý)
    AVALANCHE,  // Trả nợ lãi suất cao nhất trước (tiết kiệm tối đa lãi)
    CUSTOM      // Tùy chỉnh người dùng
}

data class DebtAccount(
    val id: String = "",
    val userId: String = "",
    val name: String,
    val type: DebtType,
    val totalAmount: Money,           // Hạn mức / Khoản vay gốc
    val remainingBalance: Money,      // Dư nợ hiện tại
    val interestRateApr: Double,      // Lãi suất năm (% APR, vd: 18.5)
    val minimumPayment: Money,        // Thanh toán tối thiểu hàng tháng
    val dueDate: Int? = null,         // Ngày đến hạn hàng tháng (1..31, null nếu không cố định)
    val statementDate: Int? = null,   // Ngày chốt sao kê hàng tháng (thẻ tín dụng)
    val linkedWalletId: String? = null, // ID ví thẻ tín dụng liên kết (WalletType.CARD)
    val gracePeriodDays: Int = 45,    // Thời gian miễn lãi (thẻ tín dụng, vd: 45 ngày)
    val colorHex: String = "#E11D48",
    val isReminderEnabled: Boolean = true,
    val reminderDaysBefore: Int = 3,
    val isSettled: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
) {
    /** Tỷ lệ % nợ đã thanh toán hoàn tất (0.0 .. 1.0) */
    val progress: Float
        get() {
            if (totalAmount.value <= 0L) return if (isSettled) 1f else 0f
            val paid = (totalAmount.value - remainingBalance.value).coerceAtLeast(0L)
            return (paid.toFloat() / totalAmount.value.toFloat()).coerceIn(0f, 1f)
        }

    /** Số tiền đã thanh toán */
    val paidAmount: Money
        get() = Money((totalAmount.value - remainingBalance.value).coerceAtLeast(0L))

    /** Khoản nợ có kỳ hạn thanh toán định kỳ hàng tháng bắt buộc (Thẻ tín dụng, Ngân hàng, Trả góp) */
    val isMonthlyRecurring: Boolean
        get() = type != DebtType.PERSONAL_LOAN
}

data class DebtPaymentStep(
    val monthIndex: Int,
    val targetMonth: YearMonth,
    val debtId: String,
    val debtName: String,
    val paymentAmount: Money,
    val principalPaid: Money,
    val interestPaid: Money,
    val remainingBalanceAfter: Money,
)

/**
 * Từng khoản nợ được phân bổ trích lương trong kỳ lương tới.
 */
data class PaydayAllocationItem(
    val debtId: String,
    val debtName: String,
    val debtType: DebtType,
    val isTarget: Boolean,
    val minimumPayment: Money,
    val extraPayment: Money,
    val totalPaydayPayment: Money,
    val dueDate: Int? = null,
    val isMismatchedWithPayday: Boolean, // true nếu dueDate != null && dueDate < paydayDay
    val colorHex: String,
    val assignedPaydayDay: Int? = null,
    val sponsorLabel: String? = null,
) {
    val isTargetDebt: Boolean get() = isTarget
    val totalPayment: Money get() = totalPaydayPayment
}

/**
 * Bảng kế hoạch trích lương trả nợ cho kỳ nhận lương tiếp theo.
 */
data class PaydayAllocationPlan(
    val paydayDay: Int,
    val salaryWalletId: String? = null,
    val salaryWalletName: String? = null,
    val expectedSalary: Money = Money(0L),
    val totalDebtDeduction: Money = Money(0L),
    val remainingIncomeAfterDebt: Money = Money(0L),
    val items: List<PaydayAllocationItem> = emptyList(),
    val mismatchedWarnings: List<String> = emptyList(),
    val isSemiMonthly: Boolean = false,
    val upcomingPaydayDay: Int = paydayDay,
    val upcomingPaydayLabel: String = "Kế hoạch trích lương Đợt $paydayDay",
    val upcomingPaydaySalary: Money = expectedSalary,
    val upcomingPaydayDeduction: Money = totalDebtDeduction,
    val upcomingPaydayRemaining: Money = remainingIncomeAfterDebt,
    val upcomingItems: List<PaydayAllocationItem> = items,
) {
    val deductionRatioPercent: Double
        get() = if (expectedSalary.value > 0L) (totalDebtDeduction.value.toDouble() / expectedSalary.value.toDouble()) * 100.0 else 0.0

    val upcomingDeductionRatioPercent: Double
        get() = if (upcomingPaydaySalary.value > 0L) (upcomingPaydayDeduction.value.toDouble() / upcomingPaydaySalary.value.toDouble()) * 100.0 else 0.0
}

/**
 * Kết quả so sánh trực tiếp giữa hai chiến lược Snowball và Avalanche.
 */
data class StrategyComparison(
    val interestSavedWithAvalanche: Money = Money(0L), // Tiền lãi Avalanche tiết kiệm được so với Snowball
    val firstSettledMonthDifference: Int = 0,          // Số tháng Snowball tất toán chủ nợ đầu tiên nhanh hơn Avalanche
    val isZeroAprOnly: Boolean = false,                // Toàn bộ khoản nợ đều 0% APR
    val hasMeaningfulDifference: Boolean = true,       // Có sự khác biệt thực tế về tiền lãi hoặc thời gian
)

data class DebtPayoffPlan(
    val strategy: PayoffStrategy,
    val monthlyBudgetForDebt: Money,
    val estimatedDebtFreeDate: YearMonth?,
    val totalMonths: Int,
    val totalInterestPayable: Money,
    val totalInterestSaved: Money,
    val paymentSchedule: List<DebtPaymentStep> = emptyList(),
    val totalCycles: Int = totalMonths,
    val timeSavedCycles: Int = 0,
    val isZeroAprOnly: Boolean = false,
    val isBaselineTrap: Boolean = false,
    val paydayPlan: PaydayAllocationPlan? = null,
    val comparison: StrategyComparison? = null,
)

data class DebtPaymentHistory(
    val id: String = "",
    val debtId: String,
    val walletId: String,
    val amount: Money,
    val principalPaid: Money,
    val interestPaid: Money,
    val paymentDate: Instant = Instant.now(),
    val note: String = "",
    val isCreditCardPayment: Boolean = false,
)

data class PayoffScenario(
    val name: String,
    val description: String,
    val percentageOfFcf: Double,
    val extraMonthlyAmount: Money,
    val isRecommended: Boolean = false,
)

data class SubCycleCashflow(
    val paydayDay: Int,
    val expectedSalary: Money,
    val allocatedEssentialExpense: Money,
    val availableCashflow: Money,
    val label: String,
)

data class DebtCashflowAnalysis(
    val averageMonthlyIncome: Money,
    val averageEssentialExpense: Money,
    val totalMonthlyMinimumDebt: Money,
    val freeCashFlow: Money,
    val isDeficit: Boolean,
    val weightedApr: Double,
    val scenarios: List<PayoffScenario>,
    val isSalaryCycleBased: Boolean = false,
    val baseIncomeSource: String = "",
    val isSemiMonthly: Boolean = false,
    val subCycleCashflows: List<SubCycleCashflow> = emptyList(),
)
