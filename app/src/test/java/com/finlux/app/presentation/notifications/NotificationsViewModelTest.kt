package com.finlux.app.presentation.notifications

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.AppNotification
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.NotificationRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.AddTransactionUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val notificationRepository: NotificationRepository = mockk(relaxed = true)
    private val walletRepository: WalletRepository = mockk(relaxed = true)
    private val addTransactionUseCase: AddTransactionUseCase = mockk(relaxed = true)

    private lateinit var viewModel: NotificationsViewModel

    private val testWallet = Wallet(
        id = "wallet_001",
        name = "Ví chính",
        type = WalletType.CASH,
        balance = Money(5_000_000L),
        colorHex = "#1F6FBF",
        isDefault = true,
        createdAt = Instant.now(),
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { walletRepository.observeWallets() } returns flowOf(listOf(testWallet))
        coEvery { addTransactionUseCase.invoke(any()) } returns AppResult.Success("tx_001")
        coEvery { notificationRepository.observeNotifications() } returns flowOf(emptyList())

        viewModel = NotificationsViewModel(
            notificationRepository = notificationRepository,
            walletRepository = walletRepository,
            addTransactionUseCase = addTransactionUseCase,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun payNotification_whenNotPaid_executesPaymentAndMarksPaid() = runTest {
        val notification = AppNotification(
            id = "noti_001",
            title = "Tiền điện tháng 8",
            amount = Money(500_000L),
            reminderId = "rem_001",
            isPaid = false,
        )

        viewModel.payNotification(notification)
        advanceUntilIdle()

        coVerify(exactly = 1) { addTransactionUseCase.invoke(any()) }
        coVerify(exactly = 1) { notificationRepository.markAsPaid("noti_001") }
    }

    @Test
    fun payNotification_whenAlreadyPaid_isIdempotentAndDoesNotAddTransaction() = runTest {
        val notification = AppNotification(
            id = "noti_002",
            title = "Tiền nước tháng 8",
            amount = Money(200_000L),
            reminderId = "rem_002",
            isPaid = true, // Already paid!
        )

        viewModel.payNotification(notification)
        advanceUntilIdle()

        // MUST NOT execute transaction or mark as paid again!
        coVerify(exactly = 0) { addTransactionUseCase.invoke(any()) }
        coVerify(exactly = 0) { notificationRepository.markAsPaid(any()) }
    }
}
