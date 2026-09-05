package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.CashMovementStatement
import com.finlux.app.domain.model.CumulativeFinancialMetrics
import com.finlux.app.domain.model.DailyComparisonMetric
import com.finlux.app.domain.model.DailyFinancialStatement
import com.finlux.app.domain.model.DealCategory
import com.finlux.app.domain.model.DealFlowType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.FinancialDeal
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletDailyMovement
import com.finlux.app.domain.model.assetWallets
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyStatementCalculator @Inject constructor() {

    /**
     * Tính toán toàn bộ bảng đối chiếu số dư theo từng ngày trong dải [startDate, endDate].
     * Suy diễn số dư quá khứ dựa trên:
     * OpeningBalance(T) = CurrentBalance - NetLedgerMovement(>= T)
     */
    fun calculateDailyStatements(
        wallets: List<Wallet>,
        allTransactions: List<FinanceTransaction>,
        deals: List<FinancialDeal> = emptyList(),
        startDate: LocalDate,
        endDate: LocalDate,
        zone: ZoneId,
    ): List<DailyFinancialStatement> {
        val targetWallets = wallets.assetWallets()
        val walletMap = targetWallets.associateBy { it.id }
        val targetWalletIds = walletMap.keys

        // 1. Nhóm giao dịch liên quan tới assetWallets theo LocalDate
        val transactionsByDate = allTransactions
            .filter { tx ->
                tx.walletId in targetWalletIds || (tx.relatedWalletId != null && tx.relatedWalletId in targetWalletIds)
            }
            .groupBy { it.date.atZone(zone).toLocalDate() }

        // 2. Tính Net Ledger Movement sau ngày khảo sát để tìm opening balance của từng ngày
        val startInstant = startDate.atStartOfDay(zone).toInstant()
        val endExclusiveInstant = endDate.plusDays(1).atStartOfDay(zone).toInstant()

        // Tính tổng biến động của từng ví từ startInstant tới thời điểm hiện tại
        // Để từ wallet.balance hiện tại -> suy ra số dư đầu ngày startDate
        val postStartTxByWallet = mutableMapOf<String, Long>()
        for (tx in allTransactions) {
            if (tx.date >= startInstant) {
                applyTransactionDelta(tx, postStartTxByWallet, targetWalletIds)
            }
        }

        // Số dư của từng ví vào đầu ngày startDate:
        val runningWalletBalances = targetWallets.associate { w ->
            val movementSinceStart = postStartTxByWallet[w.id] ?: 0L
            w.id to (w.balance.value - movementSinceStart)
        }.toMutableMap()

        val dayCount = ChronoUnit.DAYS.between(startDate, endDate)
        val dailyStatements = ArrayList<DailyFinancialStatement>((dayCount + 1).toInt().coerceAtLeast(1))

        var curDate = startDate
        while (!curDate.isAfter(endDate)) {
            val dayTxs = transactionsByDate[curDate].orEmpty()
            val dayOpeningBalance = runningWalletBalances.values.sum()

            // Biến động theo từng ví trong ngày curDate
            val dayInflowByWallet = mutableMapOf<String, Long>()
            val dayOutflowByWallet = mutableMapOf<String, Long>()

            var totalIncome = 0L
            var totalExpense = 0L
            var nonOperatingInflow = 0L
            var nonOperatingOutflow = 0L

            for (tx in dayTxs) {
                when (tx.type) {
                    TransactionType.INCOME -> {
                        if (tx.dealFlowType == DealFlowType.PRINCIPAL_RECOVERY) {
                            nonOperatingInflow += tx.amount.value
                        } else {
                            totalIncome += tx.amount.value
                        }
                        if (tx.walletId in targetWalletIds) {
                            dayInflowByWallet[tx.walletId] = (dayInflowByWallet[tx.walletId] ?: 0L) + tx.amount.value
                        }
                    }
                    TransactionType.EXPENSE -> {
                        if (tx.dealFlowType == DealFlowType.OUTLAY_CAPITAL) {
                            nonOperatingOutflow += tx.amount.value
                        } else {
                            totalExpense += tx.amount.value
                        }
                        if (tx.walletId in targetWalletIds) {
                            dayOutflowByWallet[tx.walletId] = (dayOutflowByWallet[tx.walletId] ?: 0L) + tx.amount.value
                        }
                    }
                    TransactionType.TRANSFER_OUT -> {
                        if (tx.walletId in targetWalletIds) {
                            dayOutflowByWallet[tx.walletId] = (dayOutflowByWallet[tx.walletId] ?: 0L) + tx.amount.value
                        }
                        // Nếu chuyển ra ví ngoài không thuộc assetWallets, xem như outflow phi hoạt động
                        if (tx.relatedWalletId == null || tx.relatedWalletId !in targetWalletIds) {
                            nonOperatingOutflow += tx.amount.value
                        }
                    }
                    TransactionType.TRANSFER_IN -> {
                        if (tx.walletId in targetWalletIds) {
                            dayInflowByWallet[tx.walletId] = (dayInflowByWallet[tx.walletId] ?: 0L) + tx.amount.value
                        }
                        // Nếu chuyển từ ví ngoài vào assetWallets, xem như inflow phi hoạt động
                        if (tx.relatedWalletId == null || tx.relatedWalletId !in targetWalletIds) {
                            nonOperatingInflow += tx.amount.value
                        }
                    }
                }
            }

            // Xây dựng WalletDailyMovement cho ngày
            val walletMovements = targetWallets.map { w ->
                val openBal = runningWalletBalances[w.id] ?: 0L
                val inf = dayInflowByWallet[w.id] ?: 0L
                val outf = dayOutflowByWallet[w.id] ?: 0L
                val net = inf - outf
                val closeBal = openBal + net

                // Cập nhật running balance cho ngày tiếp theo
                runningWalletBalances[w.id] = closeBal

                WalletDailyMovement(
                    walletId = w.id,
                    walletName = w.name,
                    walletType = w.type,
                    openingBalance = openBal,
                    inflow = inf,
                    outflow = outf,
                    netMovement = net,
                    closingBalance = closeBal,
                )
            }

            val operatingNet = totalIncome - totalExpense
            val nonOpNet = nonOperatingInflow - nonOperatingOutflow
            val netLedgerMovement = operatingNet + nonOpNet
            val closingBalance = dayOpeningBalance + netLedgerMovement

            dailyStatements.add(
                DailyFinancialStatement(
                    date = curDate,
                    openingBalance = dayOpeningBalance,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    operatingNet = operatingNet,
                    nonOperatingInflow = nonOperatingInflow,
                    nonOperatingOutflow = nonOperatingOutflow,
                    nonOperatingNet = nonOpNet,
                    netLedgerMovement = netLedgerMovement,
                    closingBalance = closingBalance,
                    walletMovements = walletMovements,
                    transactionCount = dayTxs.size,
                )
            )

            curDate = curDate.plusDays(1)
        }

        return dailyStatements
    }

    /**
     * Tính toán chỉ số lũy kế (Cumulative Metrics) đối chiếu giữa giai đoạn trước ngày [asOfDate]
     * và ngày [asOfDate] trong cùng kỳ khảo sát.
     */
    fun calculateCumulativeMetrics(
        dailyStatements: List<DailyFinancialStatement>,
        asOfDate: LocalDate,
    ): CumulativeFinancialMetrics {
        if (dailyStatements.isEmpty()) {
            return CumulativeFinancialMetrics(0L, 0L, 0L, 0L, 0L, 0L)
        }

        var incomeBefore = 0L
        var expenseBefore = 0L
        var incomeCurrent = 0L
        var expenseCurrent = 0L

        for (st in dailyStatements) {
            when {
                st.date.isBefore(asOfDate) -> {
                    incomeBefore += st.totalIncome
                    expenseBefore += st.totalExpense
                }
                st.date == asOfDate -> {
                    incomeCurrent += st.totalIncome
                    expenseCurrent += st.totalExpense
                }
            }
        }

        return CumulativeFinancialMetrics(
            incomeBefore = incomeBefore,
            incomeCurrent = incomeCurrent,
            expenseBefore = expenseBefore,
            expenseCurrent = expenseCurrent,
        )
    }

    /**
     * So sánh dòng tiền ròng hôm qua vs hôm nay.
     */
    fun calculateYesterdayComparison(
        dailyStatements: List<DailyFinancialStatement>,
        today: LocalDate,
    ): DailyComparisonMetric {
        val yesterday = today.minusDays(1)
        val todayStatement = dailyStatements.find { it.date == today }
        val yesterdayStatement = dailyStatements.find { it.date == yesterday }

        val todayNet = todayStatement?.operatingNet ?: 0L
        val yesterdayNet = yesterdayStatement?.operatingNet ?: 0L

        return DailyComparisonMetric(
            yesterdayNet = yesterdayNet,
            todayNet = todayNet,
            yesterdayIncome = yesterdayStatement?.totalIncome ?: 0L,
            todayIncome = todayStatement?.totalIncome ?: 0L,
            yesterdayExpense = yesterdayStatement?.totalExpense ?: 0L,
            todayExpense = todayStatement?.totalExpense ?: 0L,
        )
    }

    /**
     * Tính toán Cash Movement Statement phân tách Operating Flow vs Non-Operating Ledger Movements.
     */
    fun calculateCashMovement(
        openingBalance: Long,
        transactionsInPeriod: List<FinanceTransaction>,
        deals: List<FinancialDeal>,
        targetWalletIds: Set<String>,
    ): CashMovementStatement {
        var income = 0L
        var expense = 0L
        var transferIn = 0L
        var transferOut = 0L
        var investmentOutlay = 0L
        var principalRecovery = 0L
        var lendingOutlay = 0L
        var loanPrincipalRecovery = 0L
        var goalMovements = 0L
        var balanceAdjustments = 0L

        val dealMap = deals.associateBy { it.id }

        for (tx in transactionsInPeriod) {
            if (tx.walletId !in targetWalletIds) continue

            when (tx.type) {
                TransactionType.INCOME -> {
                    if (tx.dealFlowType == DealFlowType.PRINCIPAL_RECOVERY) {
                        val deal = tx.dealId?.let { dealMap[it] }
                        if (deal?.category == DealCategory.LENDING) {
                            loanPrincipalRecovery += tx.amount.value
                        } else {
                            principalRecovery += tx.amount.value
                        }
                    } else {
                        income += tx.amount.value
                    }
                }
                TransactionType.EXPENSE -> {
                    if (tx.dealFlowType == DealFlowType.OUTLAY_CAPITAL) {
                        val deal = tx.dealId?.let { dealMap[it] }
                        if (deal?.category == DealCategory.LENDING) {
                            lendingOutlay += tx.amount.value
                        } else {
                            investmentOutlay += tx.amount.value
                        }
                    } else {
                        expense += tx.amount.value
                    }
                }
                TransactionType.TRANSFER_IN -> {
                    transferIn += tx.amount.value
                }
                TransactionType.TRANSFER_OUT -> {
                    transferOut += tx.amount.value
                }
            }
        }

        val netMovement = (income + principalRecovery + loanPrincipalRecovery + transferIn) -
            (expense + investmentOutlay + lendingOutlay + transferOut)
        val closingBalance = openingBalance + netMovement

        return CashMovementStatement(
            openingBalance = openingBalance,
            income = income,
            expense = expense,
            transferIn = transferIn,
            transferOut = transferOut,
            investmentOutlay = investmentOutlay,
            principalRecovery = principalRecovery,
            lendingOutlay = lendingOutlay,
            loanPrincipalRecovery = loanPrincipalRecovery,
            goalMovements = goalMovements,
            balanceAdjustments = balanceAdjustments,
            closingBalance = closingBalance,
            isTransferBalanced = transferIn == transferOut,
        )
    }

    private fun applyTransactionDelta(
        tx: FinanceTransaction,
        balanceMap: MutableMap<String, Long>,
        targetWalletIds: Set<String>,
    ) {
        val wid = tx.walletId
        if (wid in targetWalletIds) {
            val cur = balanceMap[wid] ?: 0L
            when (tx.type) {
                TransactionType.INCOME, TransactionType.TRANSFER_IN -> {
                    balanceMap[wid] = cur + tx.amount.value
                }
                TransactionType.EXPENSE, TransactionType.TRANSFER_OUT -> {
                    balanceMap[wid] = cur - tx.amount.value
                }
            }
        }
    }
}
