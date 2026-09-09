package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.DealCategory
import com.finlux.app.domain.model.DealStatus
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtType
import com.finlux.app.domain.model.FinancialDeal
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.DealRepository
import com.finlux.app.domain.repository.DebtRepository
import com.finlux.app.domain.repository.WalletRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class GetTrueNetWorthUseCaseTest {

    private val walletRepository: WalletRepository = mockk()
    private val debtRepository: DebtRepository = mockk()
    private val dealRepository: DealRepository = mockk()

    private val useCase = GetTrueNetWorthUseCase(
        walletRepository = walletRepository,
        debtRepository = debtRepository,
        dealRepository = dealRepository,
    )

    @Test
    fun `calculate returns zero when wallets, debts and deals are empty`() {
        val result = useCase.calculate(emptyList(), emptyList(), emptyList())

        assertEquals(0L, result.totalWalletAssets.value)
        assertEquals(0L, result.activeDealCapitalOutlay.value)
        assertEquals(0L, result.totalDebtRemaining.value)
        assertEquals(0L, result.trueNetWorth.value)
        assertEquals(0L, result.standardNetWorth.value)
    }

    @Test
    fun `calculate returns standard net worth when no deals exist`() {
        val wallets = listOf(
            Wallet("w1", "Tiền mặt", WalletType.CASH, Money(10_000_000L), "#FFF", true, Instant.now()),
            Wallet("w2", "Ngân hàng", WalletType.BANK, Money(25_000_000L), "#FFF", false, Instant.now()),
        )
        val debts = listOf(
            DebtAccount(
                id = "d1",
                userId = "u1",
                name = "Khoản vay",
                type = DebtType.PERSONAL_LOAN,
                totalAmount = Money(15_000_000L),
                remainingBalance = Money(5_000_000L),
                interestRateApr = 10.0,
                minimumPayment = Money(1_000_000L),
                dueDate = 15,
                isSettled = false,
            ),
            DebtAccount(
                id = "d2",
                userId = "u1",
                name = "Vay đã trả xong",
                type = DebtType.PERSONAL_LOAN,
                totalAmount = Money(20_000_000L),
                remainingBalance = Money(0L),
                interestRateApr = 10.0,
                minimumPayment = Money(0L),
                dueDate = 10,
                isSettled = true,
            ),
        )

        val result = useCase.calculate(wallets, debts, emptyList())

        assertEquals(35_000_000L, result.totalWalletAssets.value)
        assertEquals(0L, result.activeDealCapitalOutlay.value)
        assertEquals(5_000_000L, result.totalDebtRemaining.value)
        assertEquals(30_000_000L, result.standardNetWorth.value)
        assertEquals(30_000_000L, result.trueNetWorth.value)
    }

    @Test
    fun `calculate includes active deal remaining capital in trueNetWorth`() {
        val wallets = listOf(
            Wallet("w1", "Tiền mặt", WalletType.CASH, Money(10_000_000L), "#FFF", true, Instant.now()),
        )
        val debts = listOf(
            DebtAccount(
                id = "d1",
                userId = "u1",
                name = "Thẻ tín dụng",
                type = DebtType.CREDIT_CARD,
                totalAmount = Money(10_000_000L),
                remainingBalance = Money(4_000_000L),
                interestRateApr = 20.0,
                minimumPayment = Money(1_000_000L),
                dueDate = 20,
                isSettled = false,
            ),
        )
        val deals = listOf(
            FinancialDeal(
                id = "deal1",
                userId = "u1",
                title = "Đầu tư bất động sản",
                category = DealCategory.INVESTMENT,
                totalCapitalOutlay = Money(50_000_000L),
                totalRecovered = Money(10_000_000L),
                writtenOffCapital = Money(0L),
                status = DealStatus.ACTIVE,
            ),
            FinancialDeal(
                id = "deal2",
                userId = "u1",
                title = "Cho vay bạn bè",
                category = DealCategory.LENDING,
                totalCapitalOutlay = Money(20_000_000L),
                totalRecovered = Money(5_000_000L),
                writtenOffCapital = Money(0L),
                status = DealStatus.ACTIVE,
            ),
        )

        // Deal 1 remaining = 40M, Deal 2 remaining = 15M -> Total Active Capital = 55M
        // Total Wallet Assets = 10M
        // Total Debt = 4M
        // True Net Worth = 10M + 55M - 4M = 61M
        // Standard Net Worth = 10M - 4M = 6M
        val result = useCase.calculate(wallets, debts, deals)

        assertEquals(10_000_000L, result.totalWalletAssets.value)
        assertEquals(55_000_000L, result.activeDealCapitalOutlay.value)
        assertEquals(4_000_000L, result.totalDebtRemaining.value)
        assertEquals(6_000_000L, result.standardNetWorth.value)
        assertEquals(61_000_000L, result.trueNetWorth.value)
    }

    @Test
    fun `calculate excludes COMPLETED and CANCELLED deals from active capital`() {
        val wallets = listOf(
            Wallet("w1", "Tiền mặt", WalletType.CASH, Money(10_000_000L), "#FFF", true, Instant.now()),
        )
        val deals = listOf(
            FinancialDeal(
                id = "deal1",
                userId = "u1",
                title = "Deal đang chạy",
                totalCapitalOutlay = Money(30_000_000L),
                totalRecovered = Money(10_000_000L),
                status = DealStatus.ACTIVE,
            ),
            FinancialDeal(
                id = "deal2",
                userId = "u1",
                title = "Deal đã hoàn thành",
                totalCapitalOutlay = Money(50_000_000L),
                totalRecovered = Money(60_000_000L),
                status = DealStatus.COMPLETED,
            ),
            FinancialDeal(
                id = "deal3",
                userId = "u1",
                title = "Deal đã hủy",
                totalCapitalOutlay = Money(10_000_000L),
                totalRecovered = Money(0L),
                status = DealStatus.CANCELLED,
            ),
        )

        val result = useCase.calculate(wallets, emptyList(), deals)

        // Only deal1 is active: remaining = 20M
        assertEquals(20_000_000L, result.activeDealCapitalOutlay.value)
        assertEquals(30_000_000L, result.trueNetWorth.value)
    }

    @Test
    fun `calculate handles partially recovered deals and capital write off correctly`() {
        val wallets = listOf(
            Wallet("w1", "Tiền mặt", WalletType.CASH, Money(5_000_000L), "#FFF", true, Instant.now()),
        )
        val deals = listOf(
            FinancialDeal(
                id = "deal1",
                userId = "u1",
                title = "Deal bị lỗ vốn một phần",
                totalCapitalOutlay = Money(30_000_000L),
                totalRecovered = Money(15_000_000L),
                writtenOffCapital = Money(5_000_000L), // Chốt lỗ 5M
                status = DealStatus.ACTIVE,
            ),
        )

        // Remaining capital = 30M - 15M - 5M = 10M
        val result = useCase.calculate(wallets, emptyList(), deals)

        assertEquals(10_000_000L, result.activeDealCapitalOutlay.value)
        assertEquals(15_000_000L, result.trueNetWorth.value)
    }

    @Test
    fun `calculate handles negative net worth when debt exceeds total assets`() {
        val wallets = listOf(
            Wallet("w1", "Tiền mặt", WalletType.CASH, Money(5_000_000L), "#FFF", true, Instant.now()),
        )
        val debts = listOf(
            DebtAccount(
                id = "d1",
                userId = "u1",
                name = "Vay ngân hàng",
                type = DebtType.PERSONAL_LOAN,
                totalAmount = Money(50_000_000L),
                remainingBalance = Money(40_000_000L),
                interestRateApr = 12.0,
                minimumPayment = Money(2_000_000L),
                dueDate = 10,
                isSettled = false,
            ),
        )
        val deals = listOf(
            FinancialDeal(
                id = "deal1",
                userId = "u1",
                title = "Deal nhỏ",
                totalCapitalOutlay = Money(10_000_000L),
                totalRecovered = Money(0L),
                status = DealStatus.ACTIVE,
            ),
        )

        // Assets = 5M, Deal = 10M -> Gross Wealth = 15M
        // Debt = 40M
        // True Net Worth = 15M - 40M = -25M
        // Standard Net Worth = 5M - 40M = -35M
        val result = useCase.calculate(wallets, debts, deals)

        assertEquals(5_000_000L, result.totalWalletAssets.value)
        assertEquals(10_000_000L, result.activeDealCapitalOutlay.value)
        assertEquals(40_000_000L, result.totalDebtRemaining.value)
        assertEquals(-25_000_000L, result.trueNetWorth.value)
        assertEquals(-35_000_000L, result.standardNetWorth.value)
    }

    @Test
    fun `calculate captures unlinked negative card wallet balances in totalDebtRemaining`() {
        val wallets = listOf(
            Wallet("w1", "Tiền mặt", WalletType.CASH, Money(10_000_000L), "#FFF", true, Instant.now()),
            // Negative card wallet balance (-4M) not linked to any debt account
            Wallet("w-card", "Thẻ Tín Dụng VCB", WalletType.CARD, Money(-4_000_000L), "#FFF", false, Instant.now()),
        )
        val debts = listOf(
            DebtAccount(
                id = "d1",
                name = "Vay Ngân Hàng",
                type = DebtType.BANK_LOAN,
                totalAmount = Money(30_000_000L),
                remainingBalance = Money(20_000_000L),
                interestRateApr = 10.0,
                minimumPayment = Money(2_000_000L),
            ),
        )

        val result = useCase.calculate(wallets, debts, emptyList())

        // Assets = 10M (w1 only, negative card wallet asset is clamped to 0)
        assertEquals(10_000_000L, result.totalWalletAssets.value)
        // Debt = 20M (from debts) + 4M (from unlinked negative card wallet) = 24M
        assertEquals(24_000_000L, result.totalDebtRemaining.value)
        // True Net Worth = 10M - 24M = -14M
        assertEquals(-14_000_000L, result.trueNetWorth.value)
    }

    @Test
    fun `observe emits Flow combining wallets, debts, and deals in real-time`() = runTest {
        val wallets = listOf(Wallet("w1", "Ví", WalletType.CASH, Money(20_000_000L), "#FFF", true, Instant.now()))
        val debts = listOf(
            DebtAccount(
                id = "d1",
                userId = "u1",
                name = "Nợ",
                type = DebtType.PERSONAL_LOAN,
                totalAmount = Money(10_000_000L),
                remainingBalance = Money(5_000_000L),
                interestRateApr = 10.0,
                minimumPayment = Money(1_000_000L),
                dueDate = 5,
                isSettled = false,
            ),
        )
        val deals = listOf(
            FinancialDeal(
                id = "deal1",
                userId = "u1",
                title = "Deal",
                totalCapitalOutlay = Money(15_000_000L),
                totalRecovered = Money(5_000_000L),
                status = DealStatus.ACTIVE,
            ),
        )

        every { walletRepository.observeWallets() } returns flowOf(wallets)
        every { debtRepository.observeDebts() } returns flowOf(debts)
        every { dealRepository.observeDeals() } returns flowOf(deals)

        val emission = useCase().first()

        assertEquals(20_000_000L, emission.totalWalletAssets.value)
        assertEquals(10_000_000L, emission.activeDealCapitalOutlay.value)
        assertEquals(5_000_000L, emission.totalDebtRemaining.value)
        assertEquals(25_000_000L, emission.trueNetWorth.value)
    }
}
