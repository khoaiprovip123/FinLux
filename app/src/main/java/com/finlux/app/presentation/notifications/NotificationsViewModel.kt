package com.finlux.app.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.AppNotification
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.repository.NotificationRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.AddTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val walletRepository: WalletRepository,
    private val addTransactionUseCase: AddTransactionUseCase,
) : ViewModel() {

    val notifications: StateFlow<List<AppNotification>> = notificationRepository
        .observeNotifications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage

    fun markAsRead(id: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
        }
    }

    fun payNotification(notification: AppNotification) {
        if (notification.isPaid) return
        viewModelScope.launch {
            val wallets = walletRepository.observeWallets().firstOrNull().orEmpty()
            val targetWallet = wallets.firstOrNull { it.id == notification.walletId }
                ?: wallets.firstOrNull { it.isDefault }
                ?: wallets.firstOrNull()

            if (targetWallet == null) {
                _userMessage.emit("Chưa có ví nào để thực hiện thanh toán!")
                return@launch
            }

            val result = addTransactionUseCase(
                FinanceTransaction(
                    type = TransactionType.EXPENSE,
                    amount = notification.amount,
                    categoryId = notification.categoryId,
                    walletId = targetWallet.id,
                    note = "Thanh toán thông báo: ${notification.title}",
                    date = Instant.now(),
                )
            )

            when (result) {
                is AppResult.Success -> {
                    notificationRepository.markAsPaid(notification.id)
                    _userMessage.emit("Đã ghi nhận thanh toán ${notification.title}!")
                }
                is AppResult.Error -> {
                    _userMessage.emit("Thanh toán thất bại: ${result.message}")
                }
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            notificationRepository.clearAll()
        }
    }
}
