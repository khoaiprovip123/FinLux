package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

class AnalyzeDebtCashflowUseCaseTest {

    private val useCase = AnalyzeDebtCashflowUseCase()
    private val zone = ZoneId.systemDefault()

    private val foodCategory = Category(
        id = "c_food",
        name = "Ăn uống",
        type = CategoryType.EXPENSE,
        icon = "restaurant",
        colorHex = "#FF5722",
        isDefault = true,
        createdAt = Instant.now(),
        isEssential = true,
    )

    private val shoppingCategory = Category(
        id = "c_shopping",
        name = "Mua sắm",
        type = CategoryType.EXPENSE,
        icon = "shopping_bag",
        colorHex = "#06B6D4",
        isDefault = false,
        createdAt = Instant.now(),
        isEssential = false,
    )

    private val salaryCategory = Category(
        id = "c_salary",
        name = "Lương",
        type = CategoryType.INCOME,
        icon = "payments",
        colorHex = "#10B981",
        isDefault = true,
        createdAt = Instant.now(),
        isEssential = true,
    )

    private val testDebts = listOf(
        DebtAccount(
            id = "d1",
            name = "Thẻ HSBC",
            type = DebtType.CREDIT_CARD,
            totalAmount = Money(50_000_000L),
            remainingBalance = Money(20_000_000L),
            interestRateApr = 24.0,
            minimumPayment = Money(1_000_000L),
        ),
        DebtAccount(
            id = "d2",
            name = "Vay VPBank",
            type = DebtType.BANK_LOAN,
            totalAmount = Money(100_000_000L),
            remainingBalance = Money(60_000_000L),
            interestRateApr = 12.0,
            minimumPayment = Money(3_000_000L),
        ),
    )

    @Test
    fun `calculates positive cashflow and generates 3 scenarios accurately`() {
        val currentMonth = YearMonth.of(2026, 8)
        val m1 = currentMonth.atDay(10).atStartOfDay(zone).toInstant()
        val m2 = currentMonth.minusMonths(1).atDay(10).atStartOfDay(zone).toInstant()
        val m3 = currentMonth.minusMonths(2).atDay(10).atStartOfDay(zone).toInstant()

        val transactions = listOf(
            // Income 30tr each month -> avg 30tr
            FinanceTransaction(id = "tx1", type = TransactionType.INCOME, amount = Money(30_000_000L), categoryId = salaryCategory.id, walletId = "w1", date = m1),
            FinanceTransaction(id = "tx2", type = TransactionType.INCOME, amount = Money(30_000_000L), categoryId = salaryCategory.id, walletId = "w1", date = m2),
            FinanceTransaction(id = "tx3", type = TransactionType.INCOME, amount = Money(30_000_000L), categoryId = salaryCategory.id, walletId = "w1", date = m3),

            // Essential expense 10tr each month -> avg 10tr
            FinanceTransaction(id = "tx4", type = TransactionType.EXPENSE, amount = Money(10_000_000L), categoryId = foodCategory.id, walletId = "w1", date = m1),
            FinanceTransaction(id = "tx5", type = TransactionType.EXPENSE, amount = Money(10_000_000L), categoryId = foodCategory.id, walletId = "w1", date = m2),
            FinanceTransaction(id = "tx6", type = TransactionType.EXPENSE, amount = Money(10_000_000L), categoryId = foodCategory.id, walletId = "w1", date = m3),

            // Non-essential expense (Shopping 5tr) -> should NOT count towards essential living cost
            FinanceTransaction(id = "tx7", type = TransactionType.EXPENSE, amount = Money(5_000_000L), categoryId = shoppingCategory.id, walletId = "w1", date = m1),
        )

        val analysis = useCase(
            transactions = transactions,
            categories = listOf(foodCategory, shoppingCategory, salaryCategory),
            debts = testDebts,
            referenceMonth = currentMonth,
            monthsToLookBack = 3,
        )

        assertEquals(30_000_000L, analysis.averageMonthlyIncome.value)
        assertEquals(10_000_000L, analysis.averageEssentialExpense.value)
        assertEquals(4_000_000L, analysis.totalMonthlyMinimumDebt.value) // 1tr + 3tr

        // FCF = 30tr - 10tr - 4tr = 16tr
        assertEquals(16_000_000L, analysis.freeCashFlow.value)
        assertFalse(analysis.isDeficit)

        // Weighted APR: (20tr * 24% + 60tr * 12%) / 80tr = (480 + 720) / 80 = 1200 / 80 = 15.0%
        assertEquals(15.0, analysis.weightedApr, 0.01)

        // Scenarios:
        // Conservative: 16tr * 30% = 4.8tr
        // Balanced: 16tr * 60% = 9.6tr
        // Aggressive: 16tr * 85% = 13.6tr
        assertEquals(3, analysis.scenarios.size)
        assertEquals(4_800_000L, analysis.scenarios[0].extraMonthlyAmount.value)
        assertEquals(9_600_000L, analysis.scenarios[1].extraMonthlyAmount.value)
        assertTrue(analysis.scenarios[1].isRecommended)
        assertEquals(13_600_000L, analysis.scenarios[2].extraMonthlyAmount.value)
    }

    @Test
    fun `detects deficit when expenses and debt minimum exceed income`() {
        val currentMonth = YearMonth.of(2026, 8)
        val m1 = currentMonth.atDay(10).atStartOfDay(zone).toInstant()

        val transactions = listOf(
            FinanceTransaction(id = "tx1", type = TransactionType.INCOME, amount = Money(10_000_000L), categoryId = salaryCategory.id, walletId = "w1", date = m1),
            FinanceTransaction(id = "tx2", type = TransactionType.EXPENSE, amount = Money(8_000_000L), categoryId = foodCategory.id, walletId = "w1", date = m1),
        )

        // Total Min Debt = 4tr. Income = 10tr, Essential = 8tr. FCF = 10tr - 8tr - 4tr = -2tr
        val analysis = useCase(
            transactions = transactions,
            categories = listOf(foodCategory, salaryCategory),
            debts = testDebts,
            referenceMonth = currentMonth,
        )

        assertTrue(analysis.isDeficit)
        assertEquals(-2_000_000L, analysis.freeCashFlow.value)
        assertTrue(analysis.scenarios.isEmpty())
    }

    @Test
    fun `handles empty transactions gracefully for new users`() {
        val analysis = useCase(
            transactions = emptyList(),
            categories = emptyList(),
            debts = emptyList(),
        )

        assertEquals(0L, analysis.averageMonthlyIncome.value)
        assertEquals(0L, analysis.averageEssentialExpense.value)
        assertEquals(0L, analysis.totalMonthlyMinimumDebt.value)
        assertEquals(0L, analysis.freeCashFlow.value)
        assertTrue(analysis.isDeficit)
        assertEquals(0.0, analysis.weightedApr, 0.0)
        assertTrue(analysis.scenarios.isEmpty())
    }

    @Test
    fun `prioritizes expectedSalary and excludes debt keywords when salary cycle is enabled`() {
        val currentMonth = YearMonth.of(2026, 8)
        val m1 = currentMonth.atDay(10).atStartOfDay(zone).toInstant()

        val debtPaymentCategory = Category(
            id = "c_debt_pay",
            name = "Trả nợ ngân hàng",
            type = CategoryType.EXPENSE,
            icon = "payments",
            colorHex = "#EF4444",
            isDefault = false,
            createdAt = Instant.now(),
            isEssential = true, // Marked essential by mistake, but contains keyword "nợ"
        )

        val transactions = listOf(
            // Historic income 6.2M (transactions only)
            FinanceTransaction(id = "tx1", type = TransactionType.INCOME, amount = Money(6_200_000L), categoryId = salaryCategory.id, walletId = "w1", date = m1),
            // Essential living: Food 3M
            FinanceTransaction(id = "tx2", type = TransactionType.EXPENSE, amount = Money(3_000_000L), categoryId = foodCategory.id, walletId = "w1", date = m1),
            // Debt repayment 2M: should be filtered out to avoid double counting
            FinanceTransaction(id = "tx3", type = TransactionType.EXPENSE, amount = Money(2_000_000L), categoryId = debtPaymentCategory.id, walletId = "w1", date = m1),
        )

        val salaryConfig = com.finlux.app.domain.model.SalaryCycleConfig(
            enabled = true,
            paydayDay = 10,
            expectedSalary = Money(13_000_000L),
        )

        val analysis = useCase(
            transactions = transactions,
            categories = listOf(foodCategory, salaryCategory, debtPaymentCategory),
            debts = testDebts, // total min debt = 4M
            salaryCycleConfig = salaryConfig,
            referenceMonth = currentMonth,
        )

        // Income should be 13M (from expectedSalary), not 6.2M
        assertEquals(13_000_000L, analysis.averageMonthlyIncome.value)
        assertTrue(analysis.isSalaryCycleBased)
        assertEquals("Lương dự kiến (Chu kỳ lương)", analysis.baseIncomeSource)

        // Essential expense should only be 3M (food), excluding the 2M debt payment
        assertEquals(3_000_000L, analysis.averageEssentialExpense.value)

        // Total Min Debt = 4M (1M + 3M)
        assertEquals(4_000_000L, analysis.totalMonthlyMinimumDebt.value)

        // FCF = 13M - 3M - 4M = 6M
        assertEquals(6_000_000L, analysis.freeCashFlow.value)
        assertFalse(analysis.isDeficit)
    }
}
