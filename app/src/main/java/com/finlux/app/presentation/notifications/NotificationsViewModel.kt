package com.finlux.app.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.AppNotification
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.NotificationRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.AddTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.Instant
import java.util.Locale
import javax.inject.Inject


enum class NotificationFilter(val label: String) {
    ALL("Tất cả"),
    REMINDER("Hóa đơn"),
    BUDGET("Ngân sách"),
    GOAL("Mục tiêu"),
    SUMMARY("Báo cáo"),
    SYSTEM("Hệ thống")
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val addTransactionUseCase: AddTransactionUseCase,
) : ViewModel() {

    val selectedFilter = MutableStateFlow(NotificationFilter.ALL)


    val notifications: StateFlow<List<AppNotification>> = notificationRepository
        .observeNotifications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val wallets: StateFlow<List<Wallet>> = walletRepository
        .observeWallets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val expenseCategories: StateFlow<List<Category>> = categoryRepository
        .observeCategories()
        .map { list -> list.filter { it.type == CategoryType.EXPENSE } }
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
        payNotificationWithCustomAmount(
            notification = notification,
            customAmount = notification.amount.value,
        )
    }

    fun payNotificationWithCustomAmount(
        notification: AppNotification,
        customAmount: Long,
        walletId: String? = null,
        categoryId: String? = null,
    ) {
        if (notification.isPaid) return
        viewModelScope.launch {
            val availableWallets = wallets.value.ifEmpty { walletRepository.observeWallets().firstOrNull().orEmpty() }
            val targetWallet = availableWallets.firstOrNull { it.id == (walletId ?: notification.walletId) }
                ?: availableWallets.firstOrNull { it.isDefault }
                ?: availableWallets.firstOrNull()

            if (targetWallet == null) {
                _userMessage.emit("Chưa có ví nào để thực hiện thanh toán!")
                return@launch
            }

            val finalAmount = if (customAmount > 0) customAmount else notification.amount.value
            val targetCategoryId = categoryId ?: notification.categoryId

            val result = addTransactionUseCase(
                FinanceTransaction(
                    type = TransactionType.EXPENSE,
                    amount = Money(finalAmount),
                    categoryId = targetCategoryId,
                    walletId = targetWallet.id,
                    note = "Thanh toán: ${notification.title}",
                    date = Instant.now(),
                )
            )

            when (result) {
                is AppResult.Success -> {
                    val formatted = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")).format(finalAmount)
                    notificationRepository.markAsPaidWithAmount(
                        id = notification.id,
                        amount = Money(finalAmount),
                        newBody = "Đã thanh toán $formatted",
                    )
                    _userMessage.emit("Đã ghi nhận thanh toán ${notification.title}: $formatted!")
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

    fun selectFilter(filter: NotificationFilter) {
        selectedFilter.value = filter
    }

}
