package com.finlux.app.presentation.notifications

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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import com.finlux.app.core.designsystem.component.ErgonomicFormRow
import com.finlux.app.core.designsystem.component.FinluxAmountInputCard
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
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                                    color = if (isSwiping) ExpenseRed else Color.Transparent,
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
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp),
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "Xóa",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                            ),
                                            color = Color.White,
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
@Composable
private fun NotificationItemCard(
    notification: AppNotification,
    onCardClick: () -> Unit,
    onPayClick: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current

    val (badgeIcon, badgeColor, typeLabel) = when (notification.type) {
        NotificationType.REMINDER -> Triple(Icons.Default.ReceiptLong, FinluxPurple, "Nhắc hóa đơn")
        NotificationType.BUDGET_ALERT -> Triple(Icons.Default.Warning, ExpenseRed, "Cảnh báo ngân sách")
        NotificationType.GOAL_MILESTONE -> Triple(Icons.Default.EmojiEvents, WarningAmber, "Cột mốc mục tiêu")
        NotificationType.TRANSACTION_SUMMARY -> Triple(Icons.Default.Insights, FinluxCyan, "Báo cáo")
        NotificationType.DEBT_DUE_ALERT -> Triple(Icons.Default.CreditScore, ExpenseRed, "Hạn nợ / Thẻ")
        NotificationType.SYSTEM -> Triple(Icons.Default.Campaign, FinluxBlue, "Hệ thống")
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = tokens.surfaceSoft,
        border = BorderStroke(1.dp, tokens.border),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onCardClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = if (tokens.isDark) 0.20f else 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    badgeIcon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(22.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Header: Type Label + Timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = badgeColor,
                    )

                    val timeStr = DateTimeFormatter.ofPattern("dd/MM HH:mm")
                        .withZone(ZoneId.systemDefault())
                        .format(notification.timestamp)
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = tokens.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }

                // Title
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = if (notification.isRead) FontWeight.SemiBold else FontWeight.ExtraBold,
                    ),
                    color = tokens.onSurface,
                )

                // Body
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                    color = tokens.onSurfaceVariant,
                )

                // Amount & Pay Button for REMINDER
                if (notification.type == NotificationType.REMINDER && notification.amount.value > 0L) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Số tiền: ${formatVndAmount(notification.amount.value)}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = if (notification.isPaid) IncomeGreen else tokens.primary,
                        )

                        if (notification.isPaid) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = IncomeGreen.copy(alpha = if (tokens.isDark) 0.20f else 0.12f),
                                border = BorderStroke(1.dp, IncomeGreen.copy(alpha = 0.35f)),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = IncomeGreen,
                                        modifier = Modifier.size(15.dp),
                                    )
                                    Text(
                                        text = "Đã thanh toán",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = IncomeGreen,
                                    )
                                }
                            }
                        } else {
                            // Compact Glass Action Button
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = tokens.primary,
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(onClick = onPayClick),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text = "Thanh toán ngay",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * BottomSheet displaying paid reminder details when user taps on a completed notification.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaidNotificationDetailSheet(
    notification: AppNotification,
    wallets: List<Wallet>,
    categories: List<Category>,
    onDismiss: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val wallet = wallets.firstOrNull { it.id == notification.walletId }
    val category = categories.firstOrNull { it.id == notification.categoryId }
    val catIcon = category?.let { categoryIcon(it.icon) } ?: Icons.Default.Category
    val catColor = category?.let { colorFromHex(it.colorHex, tokens.primary) } ?: tokens.primary
    val walletIcon = wallet?.type?.let { walletIcon(it) } ?: Icons.Default.AccountBalanceWallet
    val walletColor = wallet?.let { colorFromHex(it.colorHex, tokens.primary) } ?: tokens.primary

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (tokens.isDark) Color(0xFF181824) else Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Chi tiết thanh toán",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    ),
                    color = tokens.onSurface,
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng", tint = tokens.onSurfaceVariant)
                }
            }

            // Success Hero Badge Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = IncomeGreen.copy(alpha = if (tokens.isDark) 0.16f else 0.10f),
                border = BorderStroke(1.dp, IncomeGreen.copy(alpha = 0.30f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(IncomeGreen.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = IncomeGreen,
                            modifier = Modifier.size(28.dp),
                        )
                    }

                    Text(
                        text = "Đã thanh toán thành công",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = IncomeGreen,
                    )

                    Text(
                        text = formatVndAmount(notification.amount.value),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                        ),
                        color = IncomeGreen,
                    )
                }
            }

            // Information Rows
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = tokens.surfaceSoft,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Khoản chi / Hóa đơn
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Khoản chi", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF9CA3AF))
                        Text(notification.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = tokens.onSurface)
                    }

                    // Danh mục
                    if (category != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Danh mục", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF9CA3AF))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(catIcon, null, tint = catColor, modifier = Modifier.size(16.dp))
                                Text(category.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = tokens.onSurface)
                            }
                        }
                    }

                    // Ví thanh toán
                    if (wallet != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Ví thanh toán", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF9CA3AF))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(walletIcon, null, tint = walletColor, modifier = Modifier.size(16.dp))
                                Text(wallet.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = tokens.onSurface)
                            }
                        }
                    }

                    // Thời gian
                    val timeStr = DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy")
                        .withZone(ZoneId.systemDefault())
                        .format(notification.timestamp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Thời gian", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF9CA3AF))
                        Text(timeStr, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = tokens.onSurface)
                    }

                    // Ghi chú Sổ cái
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Ghi chú sổ cái", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF9CA3AF))
                        Text("Thanh toán: ${notification.title}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = tokens.primary)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.primary,
                ),
            ) {
                Text(
                    text = "Đóng",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (tokens.isDark) Color(0xFF002B3D) else Color.White,
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * Redesigned QuickPayBottomSheet with standard Selector Rows and Hero Amount Input.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickPayBottomSheet(
    notification: AppNotification,
    wallets: List<Wallet>,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirmPay: (walletId: String, categoryId: String?, amount: Long) -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedWalletId by remember {
        mutableStateOf(
            wallets.firstOrNull { it.id == notification.walletId }?.id
                ?: wallets.firstOrNull { it.isDefault }?.id
                ?: wallets.firstOrNull()?.id.orEmpty()
        )
    }

    var selectedCategoryId by remember {
        mutableStateOf(
            categories.firstOrNull { it.id == notification.categoryId }?.id
                ?: categories.firstOrNull()?.id
        )
    }

    var amountText by remember { mutableStateOf(notification.amount.value.toString()) }
    val parsedAmount = amountText.toLongOrNull() ?: 0L

    var showWalletPicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }

    val activeWallet = wallets.find { it.id == selectedWalletId }
    val activeCategory = categories.find { it.id == selectedCategoryId }

    val isInsufficientBalance = activeWallet != null &&
        activeWallet.type != WalletType.CARD &&
        activeWallet.balance.value < parsedAmount

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = tokens.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(FinluxPurple.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = FinluxPurple,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Column {
                        Text(
                            text = "Xác nhận thanh toán",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = tokens.onSurface,
                        )
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.onSurfaceVariant,
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = tokens.onSurfaceVariant,
                    )
                }
            }

            // 1. Amount Input Card
            FinluxAmountInputCard(
                label = "SỐ TIỀN THANH TOÁN",
                amountDigits = amountText,
                onAmountChange = { amountText = it },
                showQuickChips = true,
                primaryColor = tokens.primary,
            )

            // 2. Source Wallet Selector Row
            val walletIcon = activeWallet?.type?.let { walletIcon(it) } ?: Icons.Default.AccountBalanceWallet
            val walletAccent = activeWallet?.let { colorFromHex(it.colorHex, tokens.primary) } ?: tokens.primary

            ErgonomicFormRow(
                label = "VÍ NGUỒN THANH TOÁN",
                primaryValue = activeWallet?.name ?: "Chưa chọn ví",
                secondaryValue = activeWallet?.let { "Số dư khả dụng: ${formatVndAmount(it.balance.value)}" },
                icon = walletIcon,
                iconBgColor = walletAccent.copy(alpha = 0.14f),
                iconTintColor = walletAccent,
                onClick = { showWalletPicker = true },
            )

            // 3. Category Selector Row
            val catIcon = activeCategory?.let { categoryIcon(it.icon) } ?: Icons.Default.Category
            val catAccent = activeCategory?.let { colorFromHex(it.colorHex, tokens.primary) } ?: tokens.primary

            ErgonomicFormRow(
                label = "DANH MỤC CHI TIÊU",
                primaryValue = activeCategory?.name ?: "Chưa gán danh mục",
                icon = catIcon,
                iconBgColor = catAccent.copy(alpha = 0.14f),
                iconTintColor = catAccent,
                onClick = { showCategoryPicker = true },
            )

            // Balance Warning Banner
            if (isInsufficientBalance) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ExpenseRed.copy(alpha = if (tokens.isDark) 0.20f else 0.10f),
                    border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ExpenseRed,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "Số dư ví [${activeWallet.name}] không đủ (${formatVndAmount(activeWallet.balance.value)}). Hãy chọn ví khác.",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = ExpenseRed,
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, tokens.border),
                ) {
                    Text(
                        text = "Hủy",
                        fontWeight = FontWeight.SemiBold,
                        color = tokens.onSurfaceVariant,
                    )
                }

                val canConfirm = parsedAmount > 0L && activeWallet != null && !isInsufficientBalance

                Button(
                    onClick = {
                        if (canConfirm) {
                            onConfirmPay(selectedWalletId, selectedCategoryId, parsedAmount)
                        }
                    },
                    modifier = Modifier
                        .weight(2f)
                        .height(50.dp),
                    enabled = canConfirm,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.primary,
                        disabledContainerColor = if (tokens.isDark) Color(0xFF2A2A3C) else Color(0xFFE2E8F0),
                    ),
                ) {
                    Text(
                        text = if (parsedAmount > 0) "Ghi nhận • ${formatVndAmount(parsedAmount)}" else "Ghi nhận thanh toán",
                        fontWeight = FontWeight.Bold,
                        color = if (canConfirm) (if (tokens.isDark) Color(0xFF002B3D) else Color.White) else Color(0xFF94A3B8),
                    )
                }
            }
        }
    }

    if (showWalletPicker) {
        FinluxWalletPickerBottomSheet(
            wallets = wallets,
            selectedWalletId = selectedWalletId,
            onSelectWallet = { wallet ->
                selectedWalletId = wallet.id
                showWalletPicker = false
            },
            onDismiss = { showWalletPicker = false },
        )
    }

    if (showCategoryPicker) {
        FinluxCategoryPickerBottomSheet(
            categories = categories,
            selectedCategoryId = selectedCategoryId.orEmpty(),
            onSelectCategory = { cat ->
                selectedCategoryId = cat.id
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false },
        )
    }
}
