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
    val dueDate: Int = 15,            // Ngày đến hạn hàng tháng (1..31)
    val statementDate: Int? = null,   // Ngày chốt sao kê hàng tháng (thẻ tín dụng)
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

data class DebtPayoffPlan(
    val strategy: PayoffStrategy,
    val monthlyBudgetForDebt: Money,
    val estimatedDebtFreeDate: YearMonth?,
    val totalMonths: Int,
    val totalInterestPayable: Money,
    val totalInterestSaved: Money,
    val paymentSchedule: List<DebtPaymentStep> = emptyList(),
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
)

data class PayoffScenario(
    val name: String,
    val description: String,
    val percentageOfFcf: Double,
    val extraMonthlyAmount: Money,
    val isRecommended: Boolean = false,
)

data class DebtCashflowAnalysis(
    val averageMonthlyIncome: Money,
    val averageEssentialExpense: Money,
    val totalMonthlyMinimumDebt: Money,
    val freeCashFlow: Money,
    val isDeficit: Boolean,
    val weightedApr: Double,
    val scenarios: List<PayoffScenario>,
)
