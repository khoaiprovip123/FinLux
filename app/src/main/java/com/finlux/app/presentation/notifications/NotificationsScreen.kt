package com.finlux.app.presentation.notifications

import com.finlux.app.core.designsystem.theme.FinluxPalette

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CreditScore
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import com.finlux.app.core.designsystem.component.FinluxSnackbarHost
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.ExpenseRed
import com.finlux.app.core.designsystem.FinluxBlue
import com.finlux.app.core.designsystem.FinluxCyan
import com.finlux.app.core.designsystem.FinluxPurple
import com.finlux.app.core.designsystem.GlassTopBar
import com.finlux.app.core.designsystem.IncomeGreen
import com.finlux.app.core.designsystem.WarningAmber
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.component.ErgonomicCompactAmountCard
import com.finlux.app.core.designsystem.component.ErgonomicFormRow
import com.finlux.app.core.designsystem.component.FinluxCategoryPickerBottomSheet
import com.finlux.app.core.designsystem.component.FinluxWalletPickerBottomSheet
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.designsystem.walletIcon
import com.finlux.app.domain.model.AppNotification
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.NotificationType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: ((String) -> Unit)? = null,
    payNotificationIdFlow: MutableStateFlow<String?>? = null,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val tokens = LocalFinluxTokens.current
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val wallets by viewModel.wallets.collectAsStateWithLifecycle()
    val categories by viewModel.expenseCategories.collectAsStateWithLifecycle()
    val currentFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedNotificationForPay by remember { mutableStateOf<AppNotification?>(null) }
    var selectedNotificationForDetail by remember { mutableStateOf<AppNotification?>(null) }

    val deepLinkPayId = payNotificationIdFlow?.collectAsStateWithLifecycle()?.value
    LaunchedEffect(deepLinkPayId, notifications) {
        if (!deepLinkPayId.isNullOrBlank() && notifications.isNotEmpty()) {
            val target = notifications.firstOrNull { it.id == deepLinkPayId || it.reminderId == deepLinkPayId }
            if (target != null && !target.isPaid) {
                selectedNotificationForPay = target
            }
            payNotificationIdFlow.value = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.markAllAsRead()
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val filteredList = remember(notifications, currentFilter) {
        when (currentFilter) {
            NotificationFilter.ALL -> notifications
            NotificationFilter.REMINDER -> notifications.filter { it.type == NotificationType.REMINDER }
            NotificationFilter.BUDGET -> notifications.filter { it.type == NotificationType.BUDGET_ALERT }
            NotificationFilter.GOAL -> notifications.filter { it.type == NotificationType.GOAL_MILESTONE }
            NotificationFilter.SUMMARY -> notifications.filter { it.type == NotificationType.TRANSACTION_SUMMARY }
            NotificationFilter.SYSTEM -> notifications.filter { it.type == NotificationType.SYSTEM }
        }
    }

    Scaffold(
        containerColor = FinluxPalette.Transparent,
        snackbarHost = { FinluxSnackbarHost(snackbarHostState, hasBottomBar = false) },
        topBar = {
            GlassTopBar(
                title = {
                    Text(
                        text = "Thông báo",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                        ),
                        color = tokens.onSurface,
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = tokens.onSurface)
                        }
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { viewModel.markAllAsRead(notifyUser = true) }) {
                            Icon(
                                Icons.Default.DoneAll,
                                contentDescription = "Đánh dấu tất cả đã đọc",
                                tint = if (notifications.any { !it.isRead }) tokens.primary else tokens.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { viewModel.clearAll() }) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = "Xóa tất cả",
                                tint = tokens.onSurfaceVariant,
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Filter Tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(NotificationFilter.entries.toTypedArray()) { filter ->
                    val isSelected = filter == currentFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectFilter(filter) },
                        label = {
                            Text(
                                text = filter.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = tokens.primary.copy(alpha = if (tokens.isDark) 0.25f else 0.15f),
                            selectedLabelColor = tokens.primary,
                            containerColor = tokens.surfaceSoft,
                            labelColor = tokens.onSurfaceVariant,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = tokens.border,
                            selectedBorderColor = tokens.primary,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            }

            // Quick Action & Status Row
            if (notifications.isNotEmpty()) {
                val unreadCount = notifications.count { !it.isRead }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (unreadCount > 0) "$unreadCount thông báo mới" else "${notifications.size} thông báo",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                        color = if (unreadCount > 0) tokens.primary else tokens.onSurfaceVariant,
                        fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Medium,
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (unreadCount > 0) tokens.primary.copy(alpha = if (tokens.isDark) 0.20f else 0.12f) else tokens.surfaceSoft,
                        border = BorderStroke(1.dp, if (unreadCount > 0) tokens.primary.copy(alpha = 0.4f) else tokens.border),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.markAllAsRead(notifyUser = true) },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Đánh dấu tất cả đã đọc",
                                tint = if (unreadCount > 0) tokens.primary else tokens.onSurfaceVariant,
                                modifier = Modifier.size(15.dp),
                            )
                            Text(
                                text = "Đánh dấu tất cả đã đọc",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = if (unreadCount > 0) tokens.primary else tokens.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(tokens.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = tokens.primary,
                                modifier = Modifier.size(36.dp),
                            )
                        }
                        Text(
                            text = "Chưa có thông báo nào",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = tokens.onSurface,
                        )
                        Text(
                            text = "Các lời nhắc hóa đơn, biến động tài chính & cảnh báo sẽ xuất hiện ở đây.",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = filteredList,
                        key = { it.id },
                    ) { noti ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            positionalThreshold = { distance -> distance * 0.30f },
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteNotification(noti.id)
                                    true
                                } else {
                                    false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            enableDismissFromEndToStart = true,
                            backgroundContent = {
                                val isSwiping = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = if (isSwiping) ExpenseRed else FinluxPalette.Transparent,
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 20.dp),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Xóa",
                                            tint = FinluxPalette.White,
                                            modifier = Modifier.size(24.dp),
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "Xóa",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                            ),
                                            color = FinluxPalette.White,
                                        )
                                    }
                                }
                            },
                        ) {
                            NotificationItemCard(
                                notification = noti,
                                onCardClick = {
                                    viewModel.markAsRead(noti.id)
                                    when (noti.type) {
                                        NotificationType.BUDGET_ALERT -> {
                                            onNavigate?.invoke("budget")
                                        }
                                        NotificationType.GOAL_MILESTONE -> {
                                            onNavigate?.invoke(noti.targetRoute?.ifBlank { "goals" } ?: "goals")
                                        }
                                        NotificationType.TRANSACTION_SUMMARY -> {
                                            onNavigate?.invoke("reports")
                                        }
                                        NotificationType.DEBT_DUE_ALERT -> {
                                            onNavigate?.invoke("debts")
                                        }
                                        NotificationType.REMINDER -> {
                                            if (noti.isPaid) {
                                                selectedNotificationForDetail = noti
                                            } else if (noti.amount.value > 0L) {
                                                selectedNotificationForPay = noti
                                            } else {
                                                onNavigate?.invoke("reminders")
                                            }
                                        }
                                        NotificationType.SYSTEM -> {
                                            if (!noti.targetRoute.isNullOrBlank()) {
                                                onNavigate?.invoke(noti.targetRoute)
                                            }
                                        }
                                    }
                                },
                                onPayClick = {
                                    selectedNotificationForPay = noti
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    selectedNotificationForPay?.let { noti ->
        QuickPayBottomSheet(
            notification = noti,
            wallets = wallets,
            categories = categories,
            onDismiss = { selectedNotificationForPay = null },
            onConfirmPay = { chosenWalletId, chosenCategoryId, finalAmount ->
                viewModel.payNotificationWithCustomAmount(
                    notification = noti,
                    customAmount = finalAmount,
                    walletId = chosenWalletId,
                    categoryId = chosenCategoryId,
                )
                selectedNotificationForPay = null
            },
        )
    }

    selectedNotificationForDetail?.let { noti ->
        PaidNotificationDetailSheet(
            notification = noti,
            wallets = wallets,
            categories = categories,
            onDismiss = { selectedNotificationForDetail = null },
        )
    }
}

/**
 * Redesigned Notification Card with Liquid Glass / Prism standard.
 */
