package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.core.time.FinanceClock
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SavingDestination
import com.finlux.app.domain.model.SavingMethod
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.SavingSpinStatus
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.SavingSpinRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId

class CompleteSavingSpinUseCaseTest {
    private val repository = mockk<SavingSpinRepository>(relaxed = true)
    private val transactionRepository = mockk<TransactionRepository>(relaxed = true)
    private val walletRepository = mockk<WalletRepository>(relaxed = true)
    private val now = Instant.parse("2026-09-04T08:00:00Z")
    private val clock = object : FinanceClock {
        override val zoneId: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh")
        override fun now(): Instant = now
    }

    private lateinit var useCase: CompleteSavingSpinUseCase

    @BeforeEach
    fun setUp() {
        useCase = CompleteSavingSpinUseCase(repository, transactionRepository, walletRepository, clock)
    }

    @Test
    fun `cash destination completes without wallet transfer or expense creation`() = runTest {
        val session = SavingSpinSession(
            id = "day_2026-09-04",
            scheduleKey = "day:2026-09-04",
            wheelValues = listOf(Money(50_000)),
            selectedIndex = 0,
            selectedAmount = Money(50_000),
            status = SavingSpinStatus.SPUN_PENDING,
        )
        val cashDestination = SavingDestination(
            id = "piggy",
            name = "Heo đất",
            method = SavingMethod.CASH,
            enabled = true,
        )
        coEvery { repository.completeSession("day:2026-09-04", "piggy", SavingMethod.CASH, null) } returns AppResult.Success(Unit)

        val result = useCase(session, cashDestination, sourceWalletId = null)

        assertTrue(result is AppResult.Success)
        coVerify(exactly = 1) { repository.completeSession("day:2026-09-04", "piggy", SavingMethod.CASH, null) }
        coVerify(exactly = 0) { transactionRepository.transferBetweenWalletsIdempotent(any(), any(), any(), any(), any(), any()) }
        coVerify(exactly = 0) { transactionRepository.addWithBalanceUpdate(any()) }
    }

    @Test
    fun `bank transfer with sufficient balance executes transfer and completes session`() = runTest {
        val session = SavingSpinSession(
            id = "day_2026-09-04",
            scheduleKey = "day:2026-09-04",
            wheelValues = listOf(Money(50_000)),
            selectedIndex = 0,
            selectedAmount = Money(50_000),
            status = SavingSpinStatus.SPUN_PENDING,
        )
        val bankDestination = SavingDestination(
            id = "mb_dest",
            name = "Quỹ Tiết Kiệm MB",
            method = SavingMethod.BANK_TRANSFER,
            linkedWalletId = "wallet_dest",
            enabled = true,
        )

        every { walletRepository.observeWallets() } returns flowOf(
            listOf(
                Wallet(
                    id = "wallet_src",
                    name = "Ví Chi Tiêu",
                    balance = Money(200_000),
                    type = WalletType.BANK,
                    colorHex = "#2563EB",
                    isDefault = true,
                    createdAt = now,
                ),
                Wallet(
                    id = "wallet_dest",
                    name = "Quỹ Tiết Kiệm MB",
                    balance = Money(500_000),
                    type = WalletType.BANK,
                    colorHex = "#10B981",
                    isDefault = false,
                    createdAt = now,
                ),
            )
        )
        coEvery {
            transactionRepository.transferBetweenWalletsIdempotent(
                "wallet_src", "wallet_dest", 50_000, any(), now, "saving_spin_day_2026-09-04"
            )
        } returns AppResult.Success(Unit)
        coEvery {
            repository.completeSession(
                "day:2026-09-04",
                "mb_dest",
                SavingMethod.BANK_TRANSFER,
                "saving_spin_day_2026-09-04_out",
            )
        } returns AppResult.Success(Unit)

        val result = useCase(session, bankDestination, sourceWalletId = "wallet_src")

        assertTrue(result is AppResult.Success)
        coVerify(exactly = 1) {
            transactionRepository.transferBetweenWalletsIdempotent(
                "wallet_src", "wallet_dest", 50_000, any(), now, "saving_spin_day_2026-09-04"
            )
        }
        coVerify(exactly = 1) {
            repository.completeSession(
                "day:2026-09-04",
                "mb_dest",
                SavingMethod.BANK_TRANSFER,
                "saving_spin_day_2026-09-04_out",
            )
        }
    }

    @Test
    fun `bank transfer with insufficient balance returns error and leaves session incomplete`() = runTest {
        val session = SavingSpinSession(
            id = "day_2026-09-04",
            scheduleKey = "day:2026-09-04",
            wheelValues = listOf(Money(500_000)),
            selectedIndex = 0,
            selectedAmount = Money(500_000),
            status = SavingSpinStatus.SPUN_PENDING,
        )
        val bankDestination = SavingDestination(
            id = "mb_dest",
            name = "Quỹ Tiết Kiệm MB",
            method = SavingMethod.BANK_TRANSFER,
            linkedWalletId = "wallet_dest",
            enabled = true,
        )

        every { walletRepository.observeWallets() } returns flowOf(
            listOf(
                Wallet(
                    id = "wallet_src",
                    name = "Ví Chi Tiêu",
                    balance = Money(100_000),
                    type = WalletType.BANK,
                    colorHex = "#2563EB",
                    isDefault = true,
                    createdAt = now,
                ),
                Wallet(
                    id = "wallet_dest",
                    name = "Quỹ Tiết Kiệm MB",
                    balance = Money(500_000),
                    type = WalletType.BANK,
                    colorHex = "#10B981",
                    isDefault = false,
                    createdAt = now,
                ),
            )
        )

        val result = useCase(session, bankDestination, sourceWalletId = "wallet_src")

        assertTrue(result is AppResult.Error)
        coVerify(exactly = 0) { transactionRepository.transferBetweenWalletsIdempotent(any(), any(), any(), any(), any(), any()) }
        coVerify(exactly = 0) { repository.completeSession(any(), any(), any(), any()) }
    }

    @Test
    fun `linked cash destination uses deterministic internal transfer`() = runTest {
        val session = SavingSpinSession(
            id = "day_2026-09-04",
            scheduleKey = "day:2026-09-04",
            wheelValues = listOf(Money(50_000)),
            selectedIndex = 0,
            selectedAmount = Money(50_000),
            status = SavingSpinStatus.SPUN_PENDING,
        )
        val destination = SavingDestination(
            id = "cash_savings",
            name = "Hũ tiền mặt",
            method = SavingMethod.CASH,
            linkedWalletId = "wallet_cash_savings",
            enabled = true,
        )
        every { walletRepository.observeWallets() } returns flowOf(
            listOf(
                Wallet("wallet_src", "Ví tiền mặt", WalletType.CASH, Money(200_000), "#000000", true, now),
                Wallet("wallet_cash_savings", "Hũ tiết kiệm", WalletType.CASH, Money(0), "#111111", false, now),
            )
        )
        coEvery {
            transactionRepository.transferBetweenWalletsIdempotent(
                "wallet_src", "wallet_cash_savings", 50_000, any(), now, "saving_spin_day_2026-09-04"
            )
        } returns AppResult.Success(Unit)
        coEvery {
            repository.completeSession(
                "day:2026-09-04", "cash_savings", SavingMethod.CASH, "saving_spin_day_2026-09-04_out"
            )
        } returns AppResult.Success(Unit)

        val result = useCase(session, destination, "wallet_src")

        assertTrue(result is AppResult.Success)
        coVerify(exactly = 1) {
            transactionRepository.transferBetweenWalletsIdempotent(
                "wallet_src", "wallet_cash_savings", 50_000, any(), now, "saving_spin_day_2026-09-04"
            )
        }
    }

    @Test
    fun `retry after session completion failure reuses same deterministic operation id`() = runTest {
        val session = SavingSpinSession(
            id = "day_2026-09-04",
            scheduleKey = "day:2026-09-04",
            wheelValues = listOf(Money(50_000)),
            selectedIndex = 0,
            selectedAmount = Money(50_000),
            status = SavingSpinStatus.SPUN_PENDING,
        )
        val destination = SavingDestination(
            id = "mb_dest",
            name = "Quỹ MB",
            method = SavingMethod.BANK_TRANSFER,
            linkedWalletId = "wallet_dest",
            enabled = true,
        )
        every { walletRepository.observeWallets() } returns flowOf(
            listOf(
                Wallet("wallet_src", "Chi tiêu", WalletType.BANK, Money(200_000), "#000000", true, now),
                Wallet("wallet_dest", "Tiết kiệm", WalletType.BANK, Money(0), "#111111", false, now),
            )
        )

        coEvery {
            transactionRepository.transferBetweenWalletsIdempotent(
                "wallet_src", "wallet_dest", 50_000, any(), now, "saving_spin_day_2026-09-04"
            )
        } returns AppResult.Success(Unit)
        coEvery {
            repository.completeSession(
                "day:2026-09-04", "mb_dest", SavingMethod.BANK_TRANSFER, "saving_spin_day_2026-09-04_out"
            )
        } returnsMany listOf(
            AppResult.Error("Mất kết nối sau khi chuyển tiền"),
            AppResult.Success(Unit),
        )

        val first = useCase(session, destination, "wallet_src")
        val retry = useCase(session, destination, "wallet_src")

        assertTrue(first is AppResult.Error)
        assertTrue(retry is AppResult.Success)
        coVerify(exactly = 2) {
            transactionRepository.transferBetweenWalletsIdempotent(
                "wallet_src", "wallet_dest", 50_000, any(), now, "saving_spin_day_2026-09-04"
            )
        }
        coVerify(exactly = 2) {
            repository.completeSession(
                "day:2026-09-04", "mb_dest", SavingMethod.BANK_TRANSFER, "saving_spin_day_2026-09-04_out"
            )
        }
    }

    @Test
    fun `idempotency returns success when session already completed`() = runTest {
        val session = SavingSpinSession(
            id = "day_2026-09-04",
            scheduleKey = "day:2026-09-04",
            wheelValues = listOf(Money(50_000)),
            selectedIndex = 0,
            selectedAmount = Money(50_000),
            status = SavingSpinStatus.COMPLETED,
        )
        val destination = SavingDestination(id = "piggy", name = "Heo đất", method = SavingMethod.CASH)

        val result = useCase(session, destination, null)

        assertTrue(result is AppResult.Success)
        coVerify(exactly = 0) { repository.completeSession(any(), any(), any(), any()) }
        coVerify(exactly = 0) { transactionRepository.transferBetweenWalletsIdempotent(any(), any(), any(), any(), any(), any()) }
    }
}
