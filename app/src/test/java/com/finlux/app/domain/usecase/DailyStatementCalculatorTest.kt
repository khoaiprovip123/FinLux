package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.CashMovementStatement
import com.finlux.app.domain.model.CumulativeFinancialMetrics
import com.finlux.app.domain.model.DailyComparisonMetric
import com.finlux.app.domain.model.DailyFinancialStatement
import com.finlux.app.domain.model.DealCategory
import com.finlux.app.domain.model.DealFlowType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.FinancialDeal
import com.finlux.app.domain.model.GoalFlowType
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DailyStatementCalculatorTest {
    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private val calculator = DailyStatementCalculator()

    private val walletA = Wallet(
        id = "w1",
        name = "Tiền mặt",
        type = WalletType.CASH,
        balance = Money(20_250_000L), // Số dư hiện tại
        colorHex = "#4CAF50",
        isDefault = true,
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
    )

    @Test
    fun `calculateDailyStatements satisfies financial invariants across consecutive days`() {
        // Kịch bản từ spec:
        // Ngày 01/09: Đầu ngày 15.2M | Thu 2M | Chi 800K | Ròng +1.2M | Cuối ngày 16.4M
        // Ngày 02/09: Đầu ngày 16.4M | Thu 0 | Chi 1.1M | Ròng -1.1M | Cuối ngày 15.3M
        // Ngày 03/09: Đầu ngày 15.3M | Thu 4M | Chi 800K | Ròng +3.2M | Cuối ngày 18.5M
        // Ngày 04/09: Đầu ngày 18.5M | Thu 3M | Chi 1.25M | Ròng +1.75M | Cuối ngày 20.25M

        val txs = listOf(
            // 01/09
            FinanceTransaction(
                id = "tx1",
                type = TransactionType.INCOME,
                amount = Money(2_000_000L),
                categoryId = "salary",
                walletId = "w1",
                date = LocalDate.of(2026, 9, 1).atTime(10, 0).atZone(zone).toInstant(),
            ),
            FinanceTransaction(
                id = "tx2",
                type = TransactionType.EXPENSE,
                amount = Money(800_000L),
                categoryId = "food",
                walletId = "w1",
                date = LocalDate.of(2026, 9, 1).atTime(12, 0).atZone(zone).toInstant(),
            ),
            // 02/09
            FinanceTransaction(
                id = "tx3",
                type = TransactionType.EXPENSE,
                amount = Money(1_100_000L),
                categoryId = "bill",
                walletId = "w1",
                date = LocalDate.of(2026, 9, 2).atTime(9, 0).atZone(zone).toInstant(),
            ),
            // 03/09
            FinanceTransaction(
                id = "tx4",
                type = TransactionType.INCOME,
                amount = Money(4_000_000L),
                categoryId = "bonus",
                walletId = "w1",
                date = LocalDate.of(2026, 9, 3).atTime(14, 0).atZone(zone).toInstant(),
            ),
            FinanceTransaction(
                id = "tx5",
                type = TransactionType.EXPENSE,
                amount = Money(800_000L),
                categoryId = "shopping",
                walletId = "w1",
                date = LocalDate.of(2026, 9, 3).atTime(19, 0).atZone(zone).toInstant(),
            ),
            // 04/09
            FinanceTransaction(
                id = "tx6",
                type = TransactionType.INCOME,
                amount = Money(3_000_000L),
                categoryId = "freelance",
                walletId = "w1",
                date = LocalDate.of(2026, 9, 4).atTime(11, 0).atZone(zone).toInstant(),
            ),
            FinanceTransaction(
                id = "tx7",
                type = TransactionType.EXPENSE,
                amount = Money(1_250_000L),
                categoryId = "dining",
                walletId = "w1",
                date = LocalDate.of(2026, 9, 4).atTime(18, 30).atZone(zone).toInstant(),
            ),
        )

        val statements = calculator.calculateDailyStatements(
            wallets = listOf(walletA),
            allTransactions = txs,
            startDate = LocalDate.of(2026, 9, 1),
            endDate = LocalDate.of(2026, 9, 4),
            zone = zone,
        )

        assertEquals(4, statements.size)

        // Ngày 01/09
        val day1 = statements[0]
        assertEquals(LocalDate.of(2026, 9, 1), day1.date)
        assertEquals(15_200_000L, day1.openingBalance)
        assertEquals(2_000_000L, day1.totalIncome)
        assertEquals(800_000L, day1.totalExpense)
        assertEquals(1_200_000L, day1.operatingNet)
        assertEquals(16_400_000L, day1.closingBalance)

        // Invariant check: Closing(Day 1) == Opening(Day 2)
        val day2 = statements[1]
        assertEquals(day1.closingBalance, day2.openingBalance)
        assertEquals(16_400_000L, day2.openingBalance)
        assertEquals(0L, day2.totalIncome)
        assertEquals(1_100_000L, day2.totalExpense)
        assertEquals(-1_100_000L, day2.operatingNet)
        assertEquals(15_300_000L, day2.closingBalance)

        // Invariant check: Closing(Day 2) == Opening(Day 3)
        val day3 = statements[2]
        assertEquals(day2.closingBalance, day3.openingBalance)
        assertEquals(15_300_000L, day3.openingBalance)
        assertEquals(4_000_000L, day3.totalIncome)
        assertEquals(800_000L, day3.totalExpense)
        assertEquals(3_200_000L, day3.operatingNet)
        assertEquals(18_500_000L, day3.closingBalance)

        // Invariant check: Closing(Day 3) == Opening(Day 4)
        val day4 = statements[3]
        assertEquals(day3.closingBalance, day4.openingBalance)
        assertEquals(18_500_000L, day4.openingBalance)
        assertEquals(3_000_000L, day4.totalIncome)
        assertEquals(1_250_000L, day4.totalExpense)
        assertEquals(1_750_000L, day4.operatingNet)
        assertEquals(20_250_000L, day4.closingBalance)
        // Cuối cùng bằng đúng wallet.balance
        assertEquals(walletA.balance.value, day4.closingBalance)
    }

    @Test
    fun `goal allocation and debt principal preserve daily cash identity`() {
        val day = LocalDate.of(2026, 9, 5)
        val wallet = walletA.copy(balance = Money(8_300_000L))
        val txs = listOf(
            FinanceTransaction(
                id = "goal-allocation",
                type = TransactionType.EXPENSE,
                amount = Money(1_000_000L),
                categoryId = "savings",
                walletId = "w1",
                date = day.atTime(9, 0).atZone(zone).toInstant(),
                goalId = "g1",
                goalFlowType = GoalFlowType.ALLOCATION,
            ),
            FinanceTransaction(
                id = "debt-payment",
                type = TransactionType.EXPENSE,
                amount = Money(1_200_000L),
                categoryId = "debt_payment",
                walletId = "w1",
                date = day.atTime(10, 0).atZone(zone).toInstant(),
                debtId = "d1",
                debtPrincipalAmount = Money(1_000_000L),
                debtInterestAmount = Money(200_000L),
            ),
            FinanceTransaction(
                id = "goal-release",
                type = TransactionType.INCOME,
                amount = Money(500_000L),
                categoryId = "savings",
                walletId = "w1",
                date = day.atTime(11, 0).atZone(zone).toInstant(),
                goalId = "g1",
                goalFlowType = GoalFlowType.RELEASE,
            ),
        )

        val statements = calculator.calculateDailyStatements(
            wallets = listOf(wallet),
            allTransactions = txs,
            startDate = day,
            endDate = day,
            zone = zone,
        )
        val statement = statements.single()

        assertEquals(10_000_000L, statement.openingBalance)
        assertEquals(0L, statement.totalIncome)
        assertEquals(200_000L, statement.totalExpense)
        assertEquals(500_000L, statement.nonOperatingInflow)
        assertEquals(2_000_000L, statement.nonOperatingOutflow)
        assertEquals(8_300_000L, statement.closingBalance)

        val cash = calculator.calculateCashMovement(
            openingBalance = 10_000_000L,
            transactionsInPeriod = txs,
            deals = emptyList(),
            targetWalletIds = setOf("w1"),
        )
        assertEquals(1_000_000L, cash.goalAllocation)
        assertEquals(500_000L, cash.goalRelease)
        assertEquals(1_000_000L, cash.debtPrincipalOutflow)
        assertEquals(200_000L, cash.debtInterestExpense)
        assertEquals(200_000L, cash.expense)
        assertEquals(8_300_000L, cash.closingBalance)
    }

    @Test
    fun `cumulative metrics properly splits before and current day`() {
        val txs = listOf(
            // Ngày 01-03: Thu 6M, Chi 2.7M
            FinanceTransaction(
                id = "tx1",
                type = TransactionType.INCOME,
                amount = Money(6_000_000L),
                categoryId = "cat",
                walletId = "w1",
                date = LocalDate.of(2026, 9, 2).atTime(10, 0).atZone(zone).toInstant(),
            ),
            FinanceTransaction(
                id = "tx2",
                type = TransactionType.EXPENSE,
                amount = Money(2_700_000L),
                categoryId = "cat",
                walletId = "w1",
                date = LocalDate.of(2026, 9, 2).atTime(12, 0).atZone(zone).toInstant(),
            ),
            // Ngày 04: Thu 3M, Chi 1.25M
            FinanceTransaction(
                id = "tx3",
                type = TransactionType.INCOME,
                amount = Money(3_000_000L),
                categoryId = "cat",
                walletId = "w1",
                date = LocalDate.of(2026, 9, 4).atTime(10, 0).atZone(zone).toInstant(),
            ),
            FinanceTransaction(
                id = "tx4",
                type = TransactionType.EXPENSE,
                amount = Money(1_250_000L),
                categoryId = "cat",
                walletId = "w1",
                date = LocalDate.of(2026, 9, 4).atTime(12, 0).atZone(zone).toInstant(),
            ),
        )

        val statements = calculator.calculateDailyStatements(
            wallets = listOf(walletA),
            allTransactions = txs,
            startDate = LocalDate.of(2026, 9, 1),
            endDate = LocalDate.of(2026, 9, 4),
            zone = zone,
        )

        val cumulative = calculator.calculateCumulativeMetrics(statements, asOfDate = LocalDate.of(2026, 9, 4))
        assertEquals(6_000_000L, cumulative.incomeBefore)
        assertEquals(3_000_000L, cumulative.incomeCurrent)
        assertEquals(9_000_000L, cumulative.totalCumulativeIncome)

        assertEquals(2_700_000L, cumulative.expenseBefore)
        assertEquals(1_250_000L, cumulative.expenseCurrent)
        assertEquals(3_950_000L, cumulative.totalCumulativeExpense)

        assertEquals(3_300_000L, cumulative.netBefore)
        assertEquals(1_750_000L, cumulative.netCurrent)
        assertEquals(5_050_000L, cumulative.totalCumulativeNet)
    }

    @Test
    fun `yesterday comparison computes delta correctly`() {
        val statements = listOf(
            DailyFinancialStatement(
                date = LocalDate.of(2026, 9, 3),
                openingBalance = 15_000_000L,
                totalIncome = 2_000_000L,
                totalExpense = 1_200_000L,
                operatingNet = 800_000L,
                closingBalance = 15_800_000L,
            ),
            DailyFinancialStatement(
                date = LocalDate.of(2026, 9, 4),
                openingBalance = 15_800_000L,
                totalIncome = 3_000_000L,
                totalExpense = 1_250_000L,
                operatingNet = 1_750_000L,
                closingBalance = 17_550_000L,
            ),
        )

        val comparison = calculator.calculateYesterdayComparison(statements, today = LocalDate.of(2026, 9, 4))
        assertEquals(800_000L, comparison.yesterdayNet)
        assertEquals(1_750_000L, comparison.todayNet)
        assertEquals(950_000L, comparison.netDifference) // +950.000đ
    }

    @Test
    fun `calculateCashMovement separates operating cash flow from deals and transfers`() {
        val deals = listOf(
            FinancialDeal(
                id = "deal1",
                title = "Khoản đầu tư A",
                category = DealCategory.INVESTMENT,
                totalCapitalOutlay = Money(4_000_000L),
                totalRecovered = Money(2_000_000L),
            )
        )

        val txs = listOf(
            FinanceTransaction(
                id = "tx1",
                type = TransactionType.INCOME,
                amount = Money(5_000_000L),
                categoryId = "salary",
                walletId = "w1",
                date = Instant.now(),
            ),
            FinanceTransaction(
                id = "tx2",
                type = TransactionType.EXPENSE,
                amount = Money(2_000_000L),
                categoryId = "food",
                walletId = "w1",
                date = Instant.now(),
            ),
            FinanceTransaction(
                id = "tx3",
                type = TransactionType.TRANSFER_IN,
                amount = Money(3_000_000L),
                categoryId = null,
                walletId = "w1",
                date = Instant.now(),
            ),
            FinanceTransaction(
                id = "tx4",
                type = TransactionType.TRANSFER_OUT,
                amount = Money(1_000_000L),
                categoryId = null,
                walletId = "w1",
                date = Instant.now(),
            ),
            FinanceTransaction(
                id = "tx5",
                type = TransactionType.EXPENSE,
                dealFlowType = DealFlowType.OUTLAY_CAPITAL,
                dealId = "deal1",
                amount = Money(4_000_000L),
                categoryId = null,
                walletId = "w1",
                date = Instant.now(),
            ),
            FinanceTransaction(
                id = "tx6",
                type = TransactionType.INCOME,
                dealFlowType = DealFlowType.PRINCIPAL_RECOVERY,
                dealId = "deal1",
                amount = Money(2_000_000L),
                categoryId = null,
                walletId = "w1",
                date = Instant.now(),
            ),
        )

        val cashMovement = calculator.calculateCashMovement(
            openingBalance = 20_000_000L,
            transactionsInPeriod = txs,
            deals = deals,
            targetWalletIds = setOf("w1"),
        )

        assertEquals(20_000_000L, cashMovement.openingBalance)
        assertEquals(5_000_000L, cashMovement.income)
        assertEquals(2_000_000L, cashMovement.expense)
        assertEquals(3_000_000L, cashMovement.transferIn)
        assertEquals(1_000_000L, cashMovement.transferOut)
        assertEquals(4_000_000L, cashMovement.investmentOutlay)
        assertEquals(2_000_000L, cashMovement.principalRecovery)
        // Inflows: 20M + 5M + 3M + 2M = 30M
        // Outflows: 2M + 1M + 4M = 7M
        // Closing: 23M
        assertEquals(23_000_000L, cashMovement.closingBalance)
    }
}
