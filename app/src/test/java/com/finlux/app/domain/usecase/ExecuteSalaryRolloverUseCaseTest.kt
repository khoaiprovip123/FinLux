package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.BudgetPeriodBasis
import com.finlux.app.domain.model.CycleRolloverRule
import com.finlux.app.domain.model.FinancialPeriod
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.PaydayRuleType
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class ExecuteSalaryRolloverUseCaseTest {

    private val salaryCycleRepository: SalaryCycleRepository = mockk(relaxed = true)
    private val financialPeriodResolver: FinancialPeriodResolver = mockk(relaxed = true)
    private val transactionRepository: TransactionRepository = mockk(relaxed = true)
    private lateinit var useCase: ExecuteSalaryRolloverUseCase

    private val now = Instant.now()
    private val dummyPeriod = FinancialPeriod(
        key = "salary:2026-07-10",
        start = now.minusSeconds(86400 * 30),
        endExclusive = now,
        displayLabel = "10/07 - 09/08",
        basis = BudgetPeriodBasis.SALARY_CYCLE,
    )

    private val validConfig = SalaryCycleConfig(
        enabled = true,
        paydayRuleType = PaydayRuleType.DAY_OF_MONTH,
        paydayDay = 10,
        salaryWalletId = "salary-w",
        savingsWalletId = "savings-w",
        rolloverRule = CycleRolloverRule.MOVE_TO_SAVINGS,
        budgetPeriodBasis = BudgetPeriodBasis.SALARY_CYCLE,
    )

    private val wallets = listOf(
        Wallet("salary-w", "Ví Lương", WalletType.BANK, Money(2_500_000), "#3B82F6", true, Instant.now()),
        Wallet("savings-w", "Ví Tiết Kiệm", WalletType.INVESTMENT, Money(10_000_000), "#10B981", false, Instant.now()),
    )

    @BeforeEach
    fun setup() {
        useCase = ExecuteSalaryRolloverUseCase(
            salaryCycleRepository,
            financialPeriodResolver,
            transactionRepository,
        )
        coEvery { financialPeriodResolver.resolvePreviousPeriod(any(), any()) } returns dummyPeriod
    }

    @Test
    fun `MOVE_TO_SAVINGS executes transfer and marks cycle as processed`() = runTest {
        coEvery { salaryCycleRepository.isRolloverProcessed("salary:2026-07-10") } returns false
        coEvery {
            transactionRepository.transferBetweenWallets(any(), any(), any(), any(), any())
        } returns AppResult.Success(Unit)
        coEvery { salaryCycleRepository.markRolloverProcessed("salary:2026-07-10") } returns AppResult.Success(Unit)

        val result = useCase(validConfig, wallets, now)

        assertInstanceOf(AppResult.Success::class.java, result)
        val data = (result as AppResult.Success).value
        assertInstanceOf(SalaryRolloverResult.Transferred::class.java, data)
        val transferred = data as SalaryRolloverResult.Transferred
        assertEquals("salary:2026-07-10", transferred.cycleKey)
        assertEquals(2_500_000L, transferred.amount)
        assertEquals("salary-w", transferred.fromWalletId)
        assertEquals("savings-w", transferred.toWalletId)

        coVerify(exactly = 1) {
            transactionRepository.transferBetweenWallets(
                sourceWalletId = "salary-w",
                destinationWalletId = "savings-w",
                amount = 2_500_000L,
                note = "Tích lũy kết chuyển chu kỳ lương (salary:2026-07-10)",
                date = now,
            )
        }
        coVerify(exactly = 1) { salaryCycleRepository.markRolloverProcessed("salary:2026-07-10") }
    }

    @Test
    fun `idempotency - already processed cycle does not trigger transfer again`() = runTest {
        coEvery { salaryCycleRepository.isRolloverProcessed("salary:2026-07-10") } returns true

        val result = useCase(validConfig, wallets, now)

        assertInstanceOf(AppResult.Success::class.java, result)
        val data = (result as AppResult.Success).value
        assertInstanceOf(SalaryRolloverResult.AlreadyProcessed::class.java, data)
        assertEquals("salary:2026-07-10", (data as SalaryRolloverResult.AlreadyProcessed).cycleKey)

        coVerify(exactly = 0) {
            transactionRepository.transferBetweenWallets(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `salary wallet with zero balance marks processed without creating transfer`() = runTest {
        coEvery { salaryCycleRepository.isRolloverProcessed("salary:2026-07-10") } returns false
        val zeroBalanceWallets = listOf(
            Wallet("salary-w", "Ví Lương", WalletType.BANK, Money(0), "#3B82F6", true, Instant.now()),
            Wallet("savings-w", "Ví Tiết Kiệm", WalletType.INVESTMENT, Money(10_000_000), "#10B981", false, Instant.now()),
        )

        val result = useCase(validConfig, zeroBalanceWallets, now)

        assertInstanceOf(AppResult.Success::class.java, result)
        val data = (result as AppResult.Success).value
        assertInstanceOf(SalaryRolloverResult.ZeroBalance::class.java, data)
        assertEquals("salary:2026-07-10", (data as SalaryRolloverResult.ZeroBalance).cycleKey)

        coVerify(exactly = 0) {
            transactionRepository.transferBetweenWallets(any(), any(), any(), any(), any())
        }
        coVerify(exactly = 1) { salaryCycleRepository.markRolloverProcessed("salary:2026-07-10") }
    }

    @Test
    fun `KEEP_IN_WALLET skips execution without modifying balances`() = runTest {
        val keepConfig = validConfig.copy(rolloverRule = CycleRolloverRule.KEEP_IN_WALLET)

        val result = useCase(keepConfig, wallets, now)

        assertInstanceOf(AppResult.Success::class.java, result)
        val data = (result as AppResult.Success).value
        assertInstanceOf(SalaryRolloverResult.Skipped::class.java, data)

        coVerify(exactly = 0) {
            transactionRepository.transferBetweenWallets(any(), any(), any(), any(), any())
        }
        coVerify(exactly = 0) { salaryCycleRepository.markRolloverProcessed(any()) }
    }

    @Test
    fun `same salary and savings wallet returns Error`() = runTest {
        val sameWalletConfig = validConfig.copy(savingsWalletId = "salary-w")

        val result = useCase(sameWalletConfig, wallets, now)

        assertInstanceOf(AppResult.Error::class.java, result)
        assertEquals("Ví nhận lương và ví tích lũy không được trùng nhau", (result as AppResult.Error).message)
    }
}
