package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtType
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.PayoffStrategy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
        assertTrue(plan.isZeroAprOnly)
        // Baseline without extra: 12M / 2M = 6 cycles. With 2M extra: 3 cycles -> saved 3 cycles
        assertEquals(3, plan.timeSavedCycles)
    }

    @Test
    fun `generates payday allocation plan and detects cashflow mismatch warnings`() {
        val debt1 = DebtAccount(
            id = "d1",
            name = "Thẻ HSBC",
            type = DebtType.CREDIT_CARD,
            totalAmount = Money(20_000_000L),
            remainingBalance = Money(10_000_000L),
            interestRateApr = 24.0,
            minimumPayment = Money(1_000_000L),
            dueDate = 5, // Due on day 5 (before payday 10!) -> MISMATCH!
        )

        val debt2 = DebtAccount(
            id = "d2",
            name = "Vay VPBank",
            type = DebtType.BANK_LOAN,
            totalAmount = Money(50_000_000L),
            remainingBalance = Money(30_000_000L),
            interestRateApr = 12.0,
            minimumPayment = Money(2_000_000L),
            dueDate = 20, // Due on day 20 (after payday 10) -> OK
        )

        val salaryConfig = com.finlux.app.domain.model.SalaryCycleConfig(
            enabled = true,
            paydayDay = 10,
            salaryWalletId = "w-salary",
            expectedSalary = Money(15_000_000L),
        )

        val salaryWallet = com.finlux.app.domain.model.Wallet(
            id = "w-salary",
            name = "Ví Lương VCB",
            type = com.finlux.app.domain.model.WalletType.BANK,
            balance = Money(15_000_000L),
            colorHex = "#3478F6",
            isDefault = true,
            createdAt = java.time.Instant.now(),
        )

        val plan = useCase(
            debts = listOf(debt1, debt2),
            strategy = PayoffStrategy.AVALANCHE, // debt1 has 24% APR -> Target debt
            extraMonthlyPayment = 3_000_000L,
            salaryCycleConfig = salaryConfig,
            wallets = listOf(salaryWallet),
            startMonth = startMonth,
        )

        val paydayPlan = plan.paydayPlan
        assertNotNull(paydayPlan)
        assertEquals(10, paydayPlan!!.paydayDay)
        assertEquals("Ví Lương VCB", paydayPlan.salaryWalletName)
        assertEquals(15_000_000L, paydayPlan.expectedSalary.value)

        // Total deduction = Min (1M + 2M) + Extra (3M) = 6M
        assertEquals(6_000_000L, paydayPlan.totalDebtDeduction.value)
        assertEquals(9_000_000L, paydayPlan.remainingIncomeAfterDebt.value)

        // Target item (HSBC) gets min (1M) + extra (3M) = 4M
        val hsbcItem = paydayPlan.items.find { it.debtId == "d1" }
        assertNotNull(hsbcItem)
        assertTrue(hsbcItem!!.isTarget)
        assertEquals(1_000_000L, hsbcItem.minimumPayment.value)
        assertEquals(3_000_000L, hsbcItem.extraPayment.value)
        assertEquals(4_000_000L, hsbcItem.totalPaydayPayment.value)
        assertTrue(hsbcItem.isMismatchedWithPayday)

        // Other item (VPBank) gets min only (2M)
        val vpItem = paydayPlan.items.find { it.debtId == "d2" }
        assertNotNull(vpItem)
        assertEquals(2_000_000L, vpItem!!.minimumPayment.value)
        assertEquals(0L, vpItem.extraPayment.value)
        assertFalse(vpItem.isMismatchedWithPayday)

        // Mismatch warnings should flag HSBC due on day 5 before payday day 10
        assertEquals(1, paydayPlan.mismatchedWarnings.size)
        assertTrue(paydayPlan.mismatchedWarnings.first().contains("Thẻ HSBC"))
        assertTrue(paydayPlan.mismatchedWarnings.first().contains("ngày 5"))
    }

    @Test
    fun `dual simulation computes comparison metrics when apr differs`() {
        val smallLoan = DebtAccount(
            id = "d-small",
            name = "Vay bạn 500k",
            type = DebtType.PERSONAL_LOAN,
            totalAmount = Money(500_000L),
            remainingBalance = Money(500_000L),
            interestRateApr = 0.0,
            minimumPayment = Money(50_000L),
        )

        val creditCard = DebtAccount(
            id = "d-card",
            name = "Thẻ VPBank 36%",
            type = DebtType.CREDIT_CARD,
            totalAmount = Money(20_000_000L),
            remainingBalance = Money(15_000_000L),
            interestRateApr = 36.0,
            minimumPayment = Money(750_000L),
        )

        val plan = useCase(
            debts = listOf(smallLoan, creditCard),
            strategy = PayoffStrategy.AVALANCHE,
            extraMonthlyPayment = 1_000_000L,
            startMonth = startMonth,
        )

        val comparison = plan.comparison
        assertNotNull(comparison)
        assertFalse(comparison!!.isZeroAprOnly)
        assertTrue(comparison.hasMeaningfulDifference)
        assertTrue(comparison.interestSavedWithAvalanche.value > 0L)

        // Bảng trích lương của Avalanche phải đặt thẻ tín dụng lãi 36% lên đầu tiên
        val paydayItems = plan.paydayPlan?.items
        assertNotNull(paydayItems)
        assertEquals("d-card", paydayItems!!.first().debtId)
        assertTrue(paydayItems.first().isTarget)
    }

    @Test
    fun `dual simulation flags zero apr only when all debts have 0 percent apr`() {
        val debt1 = DebtAccount(
            id = "d1",
            name = "Nợ 1",
            type = DebtType.PERSONAL_LOAN,
            totalAmount = Money(1_000_000L),
            remainingBalance = Money(1_000_000L),
            interestRateApr = 0.0,
            minimumPayment = Money(50_000L),
        )
        val debt2 = DebtAccount(
            id = "d2",
            name = "Nợ 2",
            type = DebtType.PERSONAL_LOAN,
            totalAmount = Money(6_000_000L),
            remainingBalance = Money(6_000_000L),
            interestRateApr = 0.0,
            minimumPayment = Money(200_000L),
        )

        val plan = useCase(
            debts = listOf(debt1, debt2),
            strategy = PayoffStrategy.SNOWBALL,
            extraMonthlyPayment = 500_000L,
            startMonth = startMonth,
        )

        val comparison = plan.comparison
        assertNotNull(comparison)
        assertTrue(comparison!!.isZeroAprOnly)
        assertFalse(comparison.hasMeaningfulDifference)
        assertEquals(0L, comparison.interestSavedWithAvalanche.value)
    }

    @Test
    fun `negative amortization trap stops baseline loop and sets isBaselineTrap without math overflow`() {
        val trappedDebt = DebtAccount(
            id = "d-trap",
            name = "Thẻ tín dụng lãi cắt cổ",
            type = DebtType.CREDIT_CARD,
            totalAmount = Money(10_000_000L),
            remainingBalance = Money(10_000_000L),
            interestRateApr = 36.0, // Monthly interest = 300_000 đ
            minimumPayment = Money(100_000L), // 100k < 300k interest -> Negative Amortization!
        )

        val plan = useCase(
            debts = listOf(trappedDebt),
            strategy = PayoffStrategy.AVALANCHE,
            extraMonthlyPayment = 1_000_000L, // With extra 1M, total monthly = 1.1M > 300k, so it pays off
            startMonth = startMonth,
        )

        // Phải phát hiện bẫy nợ baseline
        assertTrue(plan.isBaselineTrap)
        // Không được tràn số Long hoặc sinh số tiền lãi vô lý (+20 triệu tỷ)
        assertTrue(plan.totalInterestSaved.value <= 20_000_000L)
        assertTrue(plan.timeSavedCycles in 0..120)
        assertNotNull(plan.estimatedDebtFreeDate)
    }

    @Test
    fun `null dueDate does not trigger cashflow mismatch warnings`() {
        val debtWithNullDueDate = DebtAccount(
            id = "d-null-date",
            name = "Vay người quen",
            type = DebtType.PERSONAL_LOAN,
            totalAmount = Money(5_000_000L),
            remainingBalance = Money(5_000_000L),
            interestRateApr = 0.0,
            minimumPayment = Money(500_000L),
            dueDate = null, // Không thiết lập ngày đến hạn
        )

        val salaryConfig = com.finlux.app.domain.model.SalaryCycleConfig(
            enabled = true,
            paydayDay = 10,
        )

        val plan = useCase(
            debts = listOf(debtWithNullDueDate),
            strategy = PayoffStrategy.SNOWBALL,
            extraMonthlyPayment = 500_000L,
            salaryCycleConfig = salaryConfig,
            startMonth = startMonth,
        )

        val paydayPlan = plan.paydayPlan
        assertNotNull(paydayPlan)
        // Khi dueDate là null, TUYỆT ĐỐI KHÔNG sinh cảnh báo lệch pha
        assertTrue(paydayPlan!!.mismatchedWarnings.isEmpty())
        assertFalse(paydayPlan.items.first().isMismatchedWithPayday)
    }

    @Test
    fun `personal loan even with dueDate 1 never triggers cashflow mismatch warnings`() {
        val personalDebt = DebtAccount(
            id = "d-friend",
            name = "Đen",
            type = DebtType.PERSONAL_LOAN,
            totalAmount = Money(1_000_000L),
            remainingBalance = Money(1_000_000L),
            interestRateApr = 0.0,
            minimumPayment = Money(30_000L),
            dueDate = 1, // Legacy value
        )

        val salaryConfig = com.finlux.app.domain.model.SalaryCycleConfig(
            enabled = true,
            paydayDay = 10,
        )

        val plan = useCase(
            debts = listOf(personalDebt),
            strategy = PayoffStrategy.SNOWBALL,
            salaryCycleConfig = salaryConfig,
            startMonth = startMonth,
        )

        val paydayPlan = plan.paydayPlan
        assertNotNull(paydayPlan)
        // Nợ cá nhân (PERSONAL_LOAN) TUYỆT ĐỐI KHÔNG sinh cảnh báo lệch pha lương
        assertTrue(paydayPlan!!.mismatchedWarnings.isEmpty())
        assertFalse(paydayPlan.items.first().isMismatchedWithPayday)
    }
}
