package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtType
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.PayoffStrategy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.YearMonth

class CalculatePayoffStrategyUseCaseTest {

    private val useCase = CalculatePayoffStrategyUseCase()
    private val startMonth = YearMonth.of(2026, 8)

    @Test
    fun `empty debts returns zero plan`() {
        val plan = useCase(
            debts = emptyList(),
            strategy = PayoffStrategy.SNOWBALL,
            extraMonthlyPayment = 1_000_000L,
            startMonth = startMonth,
        )

        assertEquals(0, plan.totalMonths)
        assertEquals(0L, plan.totalInterestPayable.value)
        assertEquals(0L, plan.totalInterestSaved.value)
        assertEquals(startMonth, plan.estimatedDebtFreeDate)
    }

    @Test
    fun `snowball prioritizes smallest balance first`() {
        val smallDebt = DebtAccount(
            id = "debt-small",
            name = "Nợ nhỏ",
            type = DebtType.PERSONAL_LOAN,
            totalAmount = Money(10_000_000L),
            remainingBalance = Money(2_000_000L),
            interestRateApr = 10.0,
            minimumPayment = Money(500_000L),
        )

        val largeDebt = DebtAccount(
            id = "debt-large",
            name = "Nợ lớn",
            type = DebtType.BANK_LOAN,
            totalAmount = Money(50_000_000L),
            remainingBalance = Money(20_000_000L),
            interestRateApr = 20.0,
            minimumPayment = Money(1_000_000L),
        )

        val plan = useCase(
            debts = listOf(largeDebt, smallDebt),
            strategy = PayoffStrategy.SNOWBALL,
            extraMonthlyPayment = 1_500_000L,
            startMonth = startMonth,
        )

        assertNotNull(plan.estimatedDebtFreeDate)
        assertTrue(plan.totalMonths > 0)
        // Tháng đầu tiên số tiền trả nợ nhỏ phải lớn hơn mức tối thiểu vì được dồn extra payment
        val firstStepSmall = plan.paymentSchedule.firstOrNull { it.debtId == "debt-small" && it.monthIndex == 1 }
        assertNotNull(firstStepSmall)
        assertTrue(firstStepSmall!!.paymentAmount.value > 500_000L)
    }

    @Test
    fun `avalanche prioritizes highest interest rate first and saves interest`() {
        val highAprDebt = DebtAccount(
            id = "debt-credit",
            name = "Thẻ tín dụng 25%",
            type = DebtType.CREDIT_CARD,
            totalAmount = Money(30_000_000L),
            remainingBalance = Money(15_000_000L),
            interestRateApr = 25.0,
            minimumPayment = Money(1_000_000L),
        )

        val lowAprDebt = DebtAccount(
            id = "debt-bank",
            name = "Vay mua xe 8%",
            type = DebtType.BANK_LOAN,
            totalAmount = Money(30_000_000L),
            remainingBalance = Money(10_000_000L),
            interestRateApr = 8.0,
            minimumPayment = Money(1_000_000L),
        )

        val avalanchePlan = useCase(
            debts = listOf(highAprDebt, lowAprDebt),
            strategy = PayoffStrategy.AVALANCHE,
            extraMonthlyPayment = 2_000_000L,
            startMonth = startMonth,
        )

        val snowballPlan = useCase(
            debts = listOf(highAprDebt, lowAprDebt),
            strategy = PayoffStrategy.SNOWBALL,
            extraMonthlyPayment = 2_000_000L,
            startMonth = startMonth,
        )

        // Avalanche phải trả ít tổng tiền lãi hơn hoặc bằng Snowball
        assertTrue(avalanchePlan.totalInterestPayable.value <= snowballPlan.totalInterestPayable.value)
        assertTrue(avalanchePlan.totalInterestSaved.value > 0L)
    }

    @Test
    fun `single debt with zero interest simulation finishes accurately`() {
        val zeroInterestDebt = DebtAccount(
            id = "debt-0-apr",
            name = "Trả góp 0%",
            type = DebtType.INSTALLMENT,
            totalAmount = Money(12_000_000L),
            remainingBalance = Money(12_000_000L),
            interestRateApr = 0.0,
            minimumPayment = Money(2_000_000L),
        )

        val plan = useCase(
            debts = listOf(zeroInterestDebt),
            strategy = PayoffStrategy.SNOWBALL,
            extraMonthlyPayment = 2_000_000L, // Tổng trả 4 triệu/tháng -> 3 tháng xong
            startMonth = startMonth,
        )

        assertEquals(3, plan.totalMonths)
        assertEquals(0L, plan.totalInterestPayable.value)
        assertEquals(startMonth.plusMonths(3), plan.estimatedDebtFreeDate)
    }
}
