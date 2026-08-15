package com.finlux.app.presentation.wallet.modern

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinanceAccentHexes
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.walletIcon
import com.finlux.app.core.designsystem.modern.FinluxStyleBackdrop
import com.finlux.app.core.designsystem.modern.GlassAlertDialog
import com.finlux.app.core.designsystem.modern.GlassBottomSheet
import com.finlux.app.core.designsystem.modern.GlassCard
import com.finlux.app.core.designsystem.modern.GlassTopBar
import com.finlux.app.core.designsystem.modern.GradientHeroCard
import com.finlux.app.core.designsystem.modern.LiquidGlassCapsule
import com.finlux.app.core.navigation.Route
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.presentation.components.MainBottomBar
import com.finlux.app.presentation.home.toShortVnd
import com.finlux.app.presentation.home.toVnd
import com.finlux.app.presentation.wallet.WalletsViewModel
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernWalletsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: ((String) -> Unit)? = null,
    onAdd: (() -> Unit)? = null,
    transferRequestKey: Int = 0,
    viewModel: WalletsViewModel = hiltViewModel(),
) {
    val wallets = viewModel.wallets.collectAsStateWithLifecycle().value
    val action = viewModel.actionState.collectAsStateWithLifecycle().value
    var selectedFilter by remember { mutableStateOf<WalletType?>(null) }
    val displayedWallets = if (selectedFilter == null) wallets else wallets.filter { it.type == selectedFilter }
    val snackbar = remember { SnackbarHostState() }
    var editing by remember { mutableStateOf<Wallet?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var showTransfer by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Wallet?>(null) }

    LaunchedEffect(transferRequestKey) {
        if (transferRequestKey > 0) {
            showTransfer = true
        }
    }

    LaunchedEffect(action.message) {
        action.message?.let {
            snackbar.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Box(Modifier.fillMaxSize()) {
        FinluxStyleBackdrop(Modifier.fillMaxSize())
        Scaffold(
            topBar = {
                GlassTopBar(
                    title = { Text("Ví của tôi", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") }
                        }
                    },
                    actions = {
                        if (wallets.size > 1) {
                            IconButton(onClick = { showTransfer = true }) {
                                Icon(Icons.Default.SwapHoriz, "Chuyển tiền", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        IconButton(onClick = { editing = null; showEditor = true }) {
                            Icon(Icons.Default.Add, "Thêm ví mới")
                        }
                    },
                )
            },
            bottomBar = {
                if (onNavigate != null && onAdd != null) {
                    MainBottomBar(Route.Wallets.value, onNavigate, onAdd)
                }
            },
            snackbarHost = { SnackbarHost(snackbar) },
            containerColor = Color.Transparent,
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    val total = wallets.sumOf { it.balance.value }
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        GradientHeroCard(Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Tổng số dư", color = Color.White.copy(alpha = .85f), style = MaterialTheme.typography.bodySmall)
                                Text(total.toVnd(), color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                Text("${wallets.size} ví · quản lý tập trung và an toàn", color = Color.White.copy(alpha = .78f), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item {
                            LiquidGlassCapsule(
                                selected = selectedFilter == null,
                                onClick = { selectedFilter = null },
                                accentColor = MaterialTheme.colorScheme.primary,
                            ) {
                                Text("Tất cả", style = MaterialTheme.typography.labelMedium, fontWeight = if (selectedFilter == null) FontWeight.Bold else FontWeight.Medium)
                            }
                        }
                        items(WalletType.entries) { type ->
                            LiquidGlassCapsule(
                                selected = selectedFilter == type,
                                onClick = { selectedFilter = type },
                                accentColor = MaterialTheme.colorScheme.primary,
                            ) {
                                Text(type.label, style = MaterialTheme.typography.labelMedium, fontWeight = if (selectedFilter == type) FontWeight.Bold else FontWeight.Medium)
                            }
                        }
                    }
                }
                items(displayedWallets, key = { it.id }) { wallet ->
                    val canDelete = !wallet.isDefault && wallets.size > 1
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart && canDelete) {
                                pendingDelete = wallet
                                false
                            } else false
                        },
                    )
                    val accent = colorFromHex(wallet.colorHex)
                    val total = wallets.sumOf { it.balance.value }
                    val ratio = if (total > 0) ((wallet.balance.value * 100) / total).toInt() else 0
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            enableDismissFromEndToStart = canDelete,
                            backgroundContent = {
                                val isSwiping = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart && canDelete
                                if (isSwiping) {
                                    val alpha = (dismissState.progress * 3f).coerceIn(0f, 1f)
                                    val scale = (0.75f + dismissState.progress * 0.45f).coerceIn(0.75f, 1.2f)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = alpha))
                                            .padding(end = 24.dp),
                                        contentAlignment = Alignment.CenterEnd,
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Xóa ví",
                                            tint = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = alpha),
                                            modifier = Modifier
                                                .size(26.dp)
                                                .graphicsLayer(scaleX = scale, scaleY = scale),
                                        )
                                    }
                                }
                            },
                        ) {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { editing = wallet; showEditor = true },
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(accent.copy(alpha = 0.18f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(walletIcon(wallet.type), wallet.name, tint = accent, modifier = Modifier.size(24.dp))
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(wallet.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            if (wallet.isDefault) {
                                                Text(
                                                    " Mặc định ",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier
                                                        .padding(start = 6.dp)
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 5.dp, vertical = 2.dp),
                                                )
                                            }
                                        }
                                        Text(wallet.type.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(wallet.balance.value.toVnd(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("$ratio%", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { editing = null; showEditor = true },
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                Text("Bạn có thể thêm nhiều ví khác", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text("Tiền mặt, ngân hàng, ví điện tử, thẻ và đầu tư", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(4.dp))
                                Button(
                                    onClick = { editing = null; showEditor = true },
                                    shape = RoundedCornerShape(14.dp),
                                ) {
                                    Text("+ Thêm ví mới", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        WalletEditor(
            initial = editing,
            walletsCount = wallets.size,
            busy = action.busy,
            onDismiss = { showEditor = false },
            onSave = { wallet ->
                viewModel.save(wallet) { showEditor = false }
            },
            onDelete = { wallet ->
                viewModel.delete(wallet)
                showEditor = false
            },
        )
    }
    if (showTransfer) {
        TransferEditor(wallets, action.busy, { showTransfer = false }) { source, destination, amount, note ->
            viewModel.transfer(source, destination, amount, note) { showTransfer = false }
        }
    }
    pendingDelete?.let { wallet ->
        GlassAlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Xóa ví ${wallet.name}?") },
            text = { Text("Bạn có chắc chắn muốn xóa ví này? Tất cả giao dịch thuộc ví này sẽ bị ảnh hưởng.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(wallet)
                    pendingDelete = null
                }) {
                    Text("Xóa vĩnh viễn", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Hủy")
                }
            },
        )
    }
}

@Composable
private fun WalletEditor(
    initial: Wallet?,
    walletsCount: Int,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSave: (Wallet) -> Unit,
    onDelete: ((Wallet) -> Unit)? = null,
) {
    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    var type by remember(initial) { mutableStateOf(initial?.type ?: WalletType.CASH) }
    var balance by remember(initial) { mutableStateOf(initial?.balance?.value?.toString().orEmpty()) }
    var color by remember(initial) { mutableStateOf(initial?.colorHex ?: FinanceAccentHexes.first()) }
    var isDefault by remember(initial) { mutableStateOf(initial?.isDefault ?: (walletsCount == 0)) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val isEditing = initial != null
    val isDefaultWallet = initial?.isDefault == true
    val isOnlyWallet = walletsCount <= 1 && isEditing

    GlassBottomSheet(onDismiss = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 760.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        if (!isEditing) "Thêm ví mới" else "Chi tiết & Chỉnh sửa ví",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Quản lý tài khoản và dòng tiền tập trung",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Box(
                    Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(colorFromHex(color).copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(walletIcon(type), null, tint = colorFromHex(color), modifier = Modifier.size(24.dp))
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(36) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tên ví / ngân hàng") },
                placeholder = { Text("Ví dụ: Vietcombank, Momo, Tiền mặt...") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Loại tài khoản", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(WalletType.entries) { option ->
                        LiquidGlassCapsule(
                            selected = type == option,
                            onClick = { type = option },
                            accentColor = MaterialTheme.colorScheme.primary,
                        ) {
                            Text(
                                option.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (type == option) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it.filter(Char::isDigit).take(15) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (!isEditing) "Số dư ban đầu" else "Số dư hiện tại") },
                    supportingText = { Text((balance.toLongOrNull() ?: 0L).toVnd(), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf(500_000L, 1_000_000L, 2_000_000L, 5_000_000L, 10_000_000L)) { preset ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                val current = balance.toLongOrNull() ?: 0L
                                balance = (current + preset).toString()
                            },
                            label = { Text("+${preset.toShortVnd()}") },
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Màu thẻ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(FinanceAccentHexes) { hex ->
                        val isSelected = hex == color
                        Box(
                            Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(colorFromHex(hex))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.4f),
                                    shape = CircleShape,
                                )
                                .clickable { color = hex },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            // Đặt làm ví mặc định Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Column {
                        Text("Đặt làm ví mặc định", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            if (isDefaultWallet) "Ví này đang là ví mặc định của bạn"
                            else "Tự động chọn cho các giao dịch mới",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Switch(
                    checked = isDefault,
                    onCheckedChange = { if (!isDefaultWallet) isDefault = it },
                    enabled = !isDefaultWallet,
                )
            }

            Button(
                onClick = {
                    onSave(
                        Wallet(
                            initial?.id.orEmpty(),
                            name.trim(),
                            type,
                            Money(balance.toLongOrNull() ?: 0),
                            color,
                            isDefault,
                            initial?.createdAt ?: Instant.now(),
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = name.isNotBlank() && !busy,
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    if (busy) "Đang lưu…" else (if (!isEditing) "Tạo ví mới" else "Lưu thay đổi"),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            if (isEditing) {
                if (isDefaultWallet || isOnlyWallet) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Text(
                            if (isDefaultWallet) "Không thể xóa ví mặc định. Vui lòng đặt ví khác làm mặc định trước khi xóa!"
                            else "Không thể xóa ví duy nhất còn lại trong ứng dụng.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(Icons.Default.DeleteOutline, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Xóa ví này", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && initial != null) {
        GlassAlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Xóa ví ${initial.name}?") },
            text = { Text("Bạn có chắc chắn muốn xóa ví này? Tất cả giao dịch thuộc ví sẽ bị ảnh hưởng.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete?.invoke(initial)
                }) {
                    Text("Xóa vĩnh viễn", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Hủy")
                }
            },
        )
    }
}

@Composable
private fun TransferEditor(
    wallets: List<Wallet>,
    busy: Boolean,
    onDismiss: () -> Unit,
    onTransfer: (String, String, Long, String) -> Unit,
) {
    var source by remember { mutableStateOf(wallets.firstOrNull()?.id.orEmpty()) }
    var destination by remember { mutableStateOf(wallets.getOrNull(1)?.id.orEmpty()) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    GlassBottomSheet(onDismiss = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 740.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Chuyển tiền giữa các ví", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Dịch chuyển số dư nhanh chóng và an toàn", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                Icon(Icons.Default.SwapHoriz, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ví nguồn (Chuyển đi)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(wallets) { wallet ->
                        LiquidGlassCapsule(
                            selected = source == wallet.id,
                            onClick = {
                                source = wallet.id
                                if (destination == wallet.id) {
                                    destination = wallets.firstOrNull { it.id != wallet.id }?.id.orEmpty()
                                }
                            },
                            accentColor = colorFromHex(wallet.colorHex),
                        ) {
                            Text("${wallet.name} (${wallet.balance.value.toShortVnd()})", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ví nhận (Chuyển đến)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(wallets.filter { it.id != source }) { wallet ->
                        LiquidGlassCapsule(
                            selected = destination == wallet.id,
                            onClick = { destination = wallet.id },
                            accentColor = colorFromHex(wallet.colorHex),
                        ) {
                            Text("${wallet.name} (${wallet.balance.value.toShortVnd()})", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter(Char::isDigit).take(15) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Số tiền chuyển") },
                    supportingText = { Text((amount.toLongOrNull() ?: 0L).toVnd(), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf(100_000L, 200_000L, 500_000L, 1_000_000L, 2_000_000L)) { preset ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                val current = amount.toLongOrNull() ?: 0L
                                amount = (current + preset).toString()
                            },
                            label = { Text("+${preset.toShortVnd()}") },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(120) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Ghi chú chuyển tiền (Tùy chọn)") },
                shape = RoundedCornerShape(16.dp),
            )

            Button(
                onClick = { onTransfer(source, destination, amount.toLongOrNull() ?: 0, note.trim()) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = (amount.toLongOrNull() ?: 0L) > 0L && source.isNotBlank() && destination.isNotBlank() && source != destination && !busy,
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    if (busy) "Đang chuyển…" else "Xác nhận chuyển tiền",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

private val WalletType.label: String get() = when (this) {
    WalletType.CASH -> "Tiền mặt"
    WalletType.BANK -> "Ngân hàng"
    WalletType.EWALLET -> "Ví điện tử"
    WalletType.CARD -> "Thẻ tín dụng"
    WalletType.INVESTMENT -> "Đầu tư"
    WalletType.OTHER -> "Ví khác"
}
