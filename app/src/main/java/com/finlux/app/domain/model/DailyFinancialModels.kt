package com.finlux.app.domain.model

import java.time.LocalDate

/**
 * Phản ánh biến động của từng ví trong một ngày cụ thể.
 */
data class WalletDailyMovement(
    val walletId: String,
    val walletName: String,
    val walletType: WalletType,
    val openingBalance: Long,
    val inflow: Long,
    val outflow: Long,
    val netMovement: Long = inflow - outflow,
    val closingBalance: Long = openingBalance + netMovement,
)

/**
 * Bản đối chiếu tài chính hoàn chỉnh của 1 ngày (Daily Balance Statement).
 * Invariant: ClosingBalance(Day N) == OpeningBalance(Day N + 1)
 */
data class DailyFinancialStatement(
    val date: LocalDate,
    val openingBalance: Long,
    val totalIncome: Long,
    val totalExpense: Long,
    val operatingNet: Long = totalIncome - totalExpense,
    val nonOperatingInflow: Long = 0L,
    val nonOperatingOutflow: Long = 0L,
    val nonOperatingNet: Long = nonOperatingInflow - nonOperatingOutflow,
    val netLedgerMovement: Long = operatingNet + nonOperatingNet,
    val closingBalance: Long = openingBalance + netLedgerMovement,
    val walletMovements: List<WalletDailyMovement> = emptyList(),
    val transactionCount: Int = 0,
)

/**
 * Chỉ số lũy kế từ đầu kỳ tới thời điểm khảo sát.
 */
data class CumulativeFinancialMetrics(
    val incomeBefore: Long,
    val incomeCurrent: Long,
    val totalCumulativeIncome: Long = incomeBefore + incomeCurrent,
    val expenseBefore: Long,
    val expenseCurrent: Long,
    val totalCumulativeExpense: Long = expenseBefore + expenseCurrent,
    val netBefore: Long = incomeBefore - expenseBefore,
    val netCurrent: Long = incomeCurrent - expenseCurrent,
    val totalCumulativeNet: Long = netBefore + netCurrent,
)

/**
 * So sánh biến động tài chính hôm qua so với hôm nay.
 */
data class DailyComparisonMetric(
    val yesterdayNet: Long,
    val todayNet: Long,
    val netDifference: Long = todayNet - yesterdayNet,
    val yesterdayIncome: Long = 0L,
    val todayIncome: Long = 0L,
    val yesterdayExpense: Long = 0L,
    val todayExpense: Long = 0L,
)

/**
 * Phân định rạch ròi Cash Movement (Operating Cash Flow vs Ledger Movement).
 */
data class CashMovementStatement(
    val openingBalance: Long,
    val income: Long,
    val expense: Long,
    val transferIn: Long,
    val transferOut: Long,
    val investmentOutlay: Long,
    val principalRecovery: Long,
    val lendingOutlay: Long,
    val loanPrincipalRecovery: Long,
    val goalMovements: Long,
    val balanceAdjustments: Long,
    val closingBalance: Long,
    val isTransferBalanced: Boolean = transferIn == transferOut,
)
