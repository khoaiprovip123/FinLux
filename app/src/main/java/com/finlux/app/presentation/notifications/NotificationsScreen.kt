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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.GlassTopBar
import com.finlux.app.domain.model.AppNotification
import com.finlux.app.domain.model.Category
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
    payNotificationIdFlow: MutableStateFlow<String?>? = null,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val wallets by viewModel.wallets.collectAsStateWithLifecycle()
    val categories by viewModel.expenseCategories.collectAsStateWithLifecycle()
    val accent = MaterialTheme.colorScheme.primary
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedNotificationForPay by remember { mutableStateOf<AppNotification?>(null) }

    // Auto trigger payment sheet when deep-linked with pay_notification_id
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
        viewModel.userMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            GlassTopBar(
                title = { Text("Thông báo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                        }
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearAll) {
                            Icon(Icons.Default.DeleteSweep, "Xóa tất cả", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        Icons.Default.NotificationsNone,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                    Text(
                        "Không có thông báo mới",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(notifications, key = { it.id }) { item ->
                    NotificationItemCard(
                        item = item,
                        accent = accent,
                        onMarkAsRead = { viewModel.markAsRead(item.id) },
                        onOpenPaySheet = { selectedNotificationForPay = item },
                    )
                }
            }
        }
    }

    selectedNotificationForPay?.let { notification ->
        QuickPaymentSheet(
            notification = notification,
            wallets = wallets,
            categories = categories,
            accent = accent,
            onDismiss = { selectedNotificationForPay = null },
            onConfirmPay = { customAmount, walletId, categoryId ->
                viewModel.payNotificationWithCustomAmount(
                    notification = notification,
                    customAmount = customAmount,
                    walletId = walletId,
                    categoryId = categoryId,
                )
                selectedNotificationForPay = null
            }
        )
    }
}

@Composable
private fun NotificationItemCard(
    item: AppNotification,
    accent: Color,
    onMarkAsRead: () -> Unit,
    onOpenPaySheet: () -> Unit,
) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy", Locale.forLanguageTag("vi-VN"))
    val formattedTime = item.timestamp.atZone(ZoneId.systemDefault()).format(formatter)
    val isPaymentReminder = !item.reminderId.isNullOrBlank() || item.amount.value > 0L
    val successColor = Color(0xFF168A62)

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!item.isRead) onMarkAsRead() }
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            if (item.isPaid) successColor.copy(alpha = 0.18f)
                            else if (item.isRead) MaterialTheme.colorScheme.surfaceVariant
                            else accent.copy(alpha = 0.18f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (item.isPaid) Icons.Default.Check else Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (item.isPaid) successColor else if (item.isRead) MaterialTheme.colorScheme.onSurfaceVariant else accent,
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (item.isRead) FontWeight.Medium else FontWeight.Bold,
                        )
                        if (!item.isRead) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(accent)
                            )
                        }
                    }
                    if (item.body.isNotBlank()) {
                        Text(
                            text = item.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }

            if (isPaymentReminder) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (item.isPaid) {
                        val formattedAmount = if (item.amount.value > 0L) {
                            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")).format(item.amount.value)
                        } else ""
                        Box(
                            modifier = Modifier
                                .background(successColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = successColor,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (formattedAmount.isNotBlank()) "Đã thanh toán: $formattedAmount" else "Đã thanh toán",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = successColor,
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = onOpenPaySheet,
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accent,
                                contentColor = Color.White,
                            ),
                        ) {
                            Icon(
                                Icons.Default.CreditCard,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Xác nhận thanh toán", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun QuickPaymentSheet(
    notification: AppNotification,
    wallets: List<Wallet>,
    categories: List<Category>,
    accent: Color,
    onDismiss: () -> Unit,
    onConfirmPay: (customAmount: Long, walletId: String?, categoryId: String?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var amountInput by remember(notification) {
        mutableStateOf(if (notification.amount.value > 0) notification.amount.value.toString() else "")
    }
    var selectedWalletId by remember(notification, wallets) {
        mutableStateOf(notification.walletId ?: wallets.firstOrNull { it.isDefault }?.id ?: wallets.firstOrNull()?.id)
    }
    var selectedCategoryId by remember(notification, categories) {
        mutableStateOf(notification.categoryId ?: categories.firstOrNull()?.id)
    }

    val parsedAmount = amountInput.toLongOrNull() ?: 0L
    val formattedPreview = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")).format(parsedAmount)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Xác nhận & Điều chỉnh số tiền",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(24.dp),
                )
            }

            // Amount Input Field
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Số tiền thực tế (VND)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 12) {
                            amountInput = input
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("Nhập số tiền...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                    singleLine = true,
                )
                Text(
                    text = "Xem trước: $formattedPreview",
                    style = MaterialTheme.typography.bodySmall,
                    color = accent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }

            // Wallet Selector
            if (wallets.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Ví thanh toán", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
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
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accent,
                                    selectedLabelColor = Color.White,
                                ),
                                shape = RoundedCornerShape(12.dp),
                            )
                        }
                    }
                }
            }

            // Category Selector
            if (categories.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Danh mục chi tiêu", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        categories.take(6).forEach { category ->
                            val isSelected = category.id == selectedCategoryId
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategoryId = category.id },
                                label = { Text(category.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accent,
                                    selectedLabelColor = Color.White,
                                ),
                                shape = RoundedCornerShape(12.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Hủy")
                }
                Button(
                    onClick = { onConfirmPay(parsedAmount, selectedWalletId, selectedCategoryId) },
                    enabled = parsedAmount > 0L,
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Xác nhận trừ tiền", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
