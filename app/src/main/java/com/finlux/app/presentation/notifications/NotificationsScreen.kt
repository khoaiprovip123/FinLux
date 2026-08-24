package com.finlux.app.presentation.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CreditScore
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.ExpenseRed
import com.finlux.app.core.designsystem.FinluxBlue
import com.finlux.app.core.designsystem.FinluxCyan
import com.finlux.app.core.designsystem.FinluxPurple
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.GlassTopBar
import com.finlux.app.core.designsystem.IncomeGreen
import com.finlux.app.core.designsystem.WarningAmber
import com.finlux.app.domain.model.AppNotification
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.NotificationType
import com.finlux.app.domain.model.Wallet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: ((String) -> Unit)? = null,
    payNotificationIdFlow: MutableStateFlow<String?>? = null,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val wallets by viewModel.wallets.collectAsStateWithLifecycle()
    val categories by viewModel.expenseCategories.collectAsStateWithLifecycle()
    val currentFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val accent = MaterialTheme.colorScheme.primary
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedNotificationForPay by remember { mutableStateOf<AppNotification?>(null) }

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
                    Text("Thông báo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                        }
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearAll() }) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = "Xóa tất cả",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                filter.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
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
                                .background(accent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(36.dp),
                            )
                        }
                        Text(
                            text = "Chưa có thông báo nào",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Các lời nhắc hóa đơn, biến động tài chính & cảnh báo sẽ xuất hiện ở đây.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(
                        items = filteredList,
                        key = { it.id },
                    ) { noti ->
                        NotificationItemCard(
                            notification = noti,
                            onCardClick = {
                                viewModel.markAsRead(noti.id)
                                if (noti.type == NotificationType.REMINDER && !noti.isPaid && noti.amount.value > 0L) {
                                    selectedNotificationForPay = noti
                                } else if (!noti.targetRoute.isNullOrBlank()) {
                                    onNavigate?.invoke(noti.targetRoute)
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
}

@Composable
private fun NotificationItemCard(
    notification: AppNotification,
    onCardClick: () -> Unit,
    onPayClick: () -> Unit,
) {
    val (badgeIcon, badgeColor, typeLabel) = when (notification.type) {
        NotificationType.REMINDER -> Triple(Icons.Default.ReceiptLong, FinluxPurple, "Nhắc hóa đơn")
        NotificationType.BUDGET_ALERT -> Triple(Icons.Default.Warning, ExpenseRed, "Cảnh báo ngân sách")
        NotificationType.GOAL_MILESTONE -> Triple(Icons.Default.EmojiEvents, WarningAmber, "Cột mốc mục tiêu")
        NotificationType.TRANSACTION_SUMMARY -> Triple(Icons.Default.Insights, FinluxCyan, "Báo cáo")
        NotificationType.DEBT_DUE_ALERT -> Triple(Icons.Default.CreditScore, ExpenseRed, "Hạn nợ / Thẻ")
        NotificationType.SYSTEM -> Triple(Icons.Default.Campaign, FinluxBlue, "Hệ thống")
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onCardClick,
    ) {
        Box(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(badgeColor.copy(alpha = 0.15f)),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold,
                        )

                        val timeStr = DateTimeFormatter.ofPattern("dd/MM HH:mm")
                            .withZone(ZoneId.systemDefault())
                            .format(notification.timestamp)
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    }

                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Text(
                        text = notification.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (notification.type == NotificationType.REMINDER && notification.amount.value > 0L) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val formatted = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))
                                .format(notification.amount.value)

                            Text(
                                text = "Số tiền: $formatted",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (notification.isPaid) IncomeGreen else MaterialTheme.colorScheme.primary,
                            )

                            if (notification.isPaid) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = IncomeGreen,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        "Đã thanh toán",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = IncomeGreen,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            } else {
                                Button(
                                    onClick = onPayClick,
                                    shape = RoundedCornerShape(10.dp),
                                ) {
                                    Text("Thanh toán ngay", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun QuickPayBottomSheet(
    notification: AppNotification,
    wallets: List<Wallet>,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirmPay: (walletId: String, categoryId: String?, amount: Long) -> Unit,
) {
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column {
                    Text(
                        text = "Xác nhận thanh toán",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Số tiền thực tế",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        amountText = input.filter { it.isDigit() }.take(15)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    supportingText = {
                        val formatted = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")).format(parsedAmount)
                        Text(
                            text = formatted,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    trailingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    placeholder = { Text("Nhập số tiền...") },
                )
            }

            if (wallets.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Ví nguồn thanh toán",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        wallets.forEach { wallet ->
                            val isSelected = wallet.id == selectedWalletId
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedWalletId = wallet.id },
                                label = { Text(wallet.name) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                            )
                        }
                    }
                }
            }

            if (categories.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Danh mục chi tiêu",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        categories.take(6).forEach { cat ->
                            val isSelected = cat.id == selectedCategoryId
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategoryId = cat.id },
                                label = { Text(cat.name) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Hủy")
                }

                Button(
                    onClick = {
                        if (selectedWalletId.isNotBlank() && parsedAmount > 0) {
                            onConfirmPay(selectedWalletId, selectedCategoryId, parsedAmount)
                        }
                    },
                    modifier = Modifier.weight(2f),
                    enabled = selectedWalletId.isNotBlank() && parsedAmount > 0,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Ghi nhận thanh toán", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
