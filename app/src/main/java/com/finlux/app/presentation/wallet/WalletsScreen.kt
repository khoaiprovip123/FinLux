package com.finlux.app.presentation.wallet

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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinanceAccentHexes
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.GlassAlertDialog
import com.finlux.app.core.designsystem.GlassDialogSurface
import com.finlux.app.core.designsystem.GlassTopBar
import com.finlux.app.core.designsystem.GradientHeroCard
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.walletIcon
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.presentation.home.toVnd
import com.finlux.app.presentation.home.toShortVnd
import com.finlux.app.presentation.components.MainBottomBar
import com.finlux.app.core.navigation.Route
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: ((String) -> Unit)? = null,
    onAdd: (() -> Unit)? = null,
    transferRequestKey: Int = 0,
    viewModel: WalletsViewModel = hiltViewModel(),
) {
    val wallets = viewModel.wallets.collectAsStateWithLifecycle().value
    val action = viewModel.actionState.collectAsStateWithLifecycle().value
    val snackbar = remember { SnackbarHostState() }
    var editing by remember { mutableStateOf<Wallet?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var showTransfer by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf<WalletType?>(null) }
    var pendingDelete by remember { mutableStateOf<Wallet?>(null) }
    LaunchedEffect(transferRequestKey) {
        if (transferRequestKey > 0) showTransfer = true
    }
    val filteredWallets = wallets.filter { filter == null || it.type == filter }
    val totalBalance = wallets.sumOf { it.balance.value }
    LaunchedEffect(action.message) { action.message?.let { snackbar.showSnackbar(it); viewModel.consumeMessage() } }
    Box(Modifier.fillMaxSize()) {
        com.finlux.app.core.designsystem.FinluxStyleBackdrop(Modifier.fillMaxSize())
        Scaffold(
            topBar = {
                GlassTopBar(
                    title = { Text("Ví của tôi", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = { IconButton(onClick = { onBack?.invoke() ?: onNavigate?.invoke(Route.Home.value) }) { Icon(Icons.Default.ArrowBack, "Quay lại") } },
                    actions = { IconButton(onClick = { editing = null; showEditor = true }) { Icon(Icons.Default.Add, "Thêm ví") } },
                )
            },
            bottomBar = { if (onNavigate != null && onAdd != null) MainBottomBar(Route.Wallets.value, onNavigate, onAdd) },
            snackbarHost = { SnackbarHost(snackbar) },
            containerColor = Color.Transparent,
        ) { padding ->
            LazyColumn(
                Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    GlassCard(
                        Modifier.fillMaxWidth(),
                        mode = com.finlux.app.core.designsystem.LiquidGlassMode.CLEAR,
                        tint = MaterialTheme.colorScheme.primary,
                        padding = PaddingValues(18.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Tổng số dư", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(totalBalance.toVnd(), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("${wallets.size} ví · quản lý tập trung và an toàn", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            com.finlux.app.core.designsystem.LiquidGlassCapsule(
                                selected = filter == null,
                                onClick = { filter = null },
                                accentColor = MaterialTheme.colorScheme.primary,
                            ) {
                                Text("Tất cả", style = MaterialTheme.typography.labelMedium, fontWeight = if (filter == null) FontWeight.Bold else FontWeight.Medium)
                            }
                        }
                        items(WalletType.entries) { type ->
                            com.finlux.app.core.designsystem.LiquidGlassCapsule(
                                selected = filter == type,
                                onClick = { filter = type },
                                accentColor = MaterialTheme.colorScheme.primary,
                            ) {
                                Text(type.label, style = MaterialTheme.typography.labelMedium, fontWeight = if (filter == type) FontWeight.Bold else FontWeight.Medium)
                            }
                        }
                    }
                }
                if (filteredWallets.isEmpty()) {
                    item { GlassCard(Modifier.fillMaxWidth()) { Text("Chưa có ví thuộc nhóm này", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                }
            items(filteredWallets, key = { it.id }) { wallet ->
                val accent = colorFromHex(wallet.colorHex)
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        when (value) {
                            SwipeToDismissBoxValue.StartToEnd -> { editing = wallet; showEditor = true }
                            SwipeToDismissBoxValue.EndToStart -> pendingDelete = wallet
                            else -> Unit
                        }
                        false
                    },
                )
                SwipeToDismissBox(
                    state = dismissState,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)),
                    backgroundContent = {
                        val direction = dismissState.dismissDirection
                        if (direction != SwipeToDismissBoxValue.Settled) {
                            val editSide = direction == SwipeToDismissBoxValue.StartToEnd
                            Row(
                                Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (editSide) accent.copy(alpha = .22f) else MaterialTheme.colorScheme.error.copy(alpha = .20f),
                                        RoundedCornerShape(20.dp),
                                    )
                                    .padding(horizontal = 22.dp),
                                horizontalArrangement = if (editSide) Arrangement.Start else Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    if (editSide) Icons.Default.Edit else Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    tint = if (editSide) accent else MaterialTheme.colorScheme.error,
                                )
                                Text(
                                    if (editSide) "Sửa" else "Xóa",
                                    modifier = Modifier.padding(start = 7.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (editSide) accent else MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    },
                    content = {
                        GlassCard(Modifier.fillMaxWidth().heightIn(min = 88.dp), onClick = { editing = wallet; showEditor = true }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(48.dp).background(accent.copy(alpha = .14f), RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
                                    Icon(walletIcon(wallet.type), null, tint = accent)
                                }
                                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                        Text(wallet.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        if (wallet.isDefault) Text("Mặc định", style = MaterialTheme.typography.labelSmall, color = accent)
                                    }
                                    Text(wallet.type.label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(wallet.balance.value.toVnd(), fontWeight = FontWeight.Bold)
                                    val percent = if (totalBalance <= 0L) 0 else (wallet.balance.value.coerceAtLeast(0L) * 100 / totalBalance).toInt()
                                    Text("$percent%", color = accent, style = MaterialTheme.typography.labelMedium)
                                }
                                Spacer(Modifier.width(6.dp))
                                IconButton(onClick = { showTransfer = true }) { Icon(Icons.Default.SwapHoriz, "Chuyển tiền", tint = accent) }
                            }
                        }
                    },
                )
            }
            item {
                GlassCard(Modifier.fillMaxWidth(), onClick = { editing = null; showEditor = true }) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Bạn có thể thêm nhiều ví khác", fontWeight = FontWeight.Bold)
                        Text("Tiền mặt, ngân hàng, ví điện tử, thẻ và đầu tư", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = { editing = null; showEditor = true }) { Text("+ Thêm ví mới") }
                    }
                }
            }
        }
    }
    }
    if (showEditor) WalletEditor(editing, action.busy, { showEditor = false }) { viewModel.save(it) { showEditor = false } }
    if (showTransfer) TransferEditor(wallets, action.busy, { showTransfer = false }) { source, destination, amount, note ->
        viewModel.transfer(source, destination, amount, note) { showTransfer = false }
    }
    pendingDelete?.let { wallet ->
        GlassAlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Xóa ${wallet.name}?") },
            text = { Text("Chỉ có thể xóa ví không còn số dư và không có giao dịch liên quan.") },
            confirmButton = { TextButton(onClick = { viewModel.delete(wallet); pendingDelete = null }) { Text("Xóa", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Hủy") } },
        )
    }
}

@Composable
private fun WalletEditor(initial: Wallet?, busy: Boolean, onDismiss: () -> Unit, onSave: (Wallet) -> Unit) {
    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    var type by remember(initial) { mutableStateOf(initial?.type ?: WalletType.CASH) }
    var balance by remember(initial) { mutableStateOf(initial?.balance?.value?.toString().orEmpty()) }
    var color by remember(initial) { mutableStateOf(initial?.colorHex ?: FinanceAccentHexes.first()) }
    Dialog(onDismissRequest = onDismiss) {
        GlassDialogSurface {
            Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Text(if (initial == null) "Thêm ví mới" else "Chỉnh sửa ví", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(name, { name = it.take(36) }, Modifier.fillMaxWidth(), label = { Text("Tên ví / ngân hàng") }, singleLine = true)
                Text("Loại tài khoản", fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(WalletType.entries) { option -> FilterChip(type == option, { type = option }, { Text(option.label) }) }
                }
                OutlinedTextField(
                    balance, { balance = it.filter(Char::isDigit).take(15) }, Modifier.fillMaxWidth(),
                    label = { Text(if (initial == null) "Số dư ban đầu" else "Số dư hiện tại") },
                    supportingText = { Text((balance.toLongOrNull() ?: 0L).toVnd()) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
                )
                Text("Màu thẻ", fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(FinanceAccentHexes) { hex ->
                        Box(Modifier.size(if (hex == color) 35.dp else 30.dp).background(colorFromHex(hex), CircleShape).clickable { color = hex })
                    }
                }
                Button(
                    onClick = { onSave(Wallet(initial?.id.orEmpty(), name.trim(), type, Money(balance.toLongOrNull() ?: 0), color, initial?.isDefault ?: false, initial?.createdAt ?: Instant.now())) },
                    modifier = Modifier.fillMaxWidth(), enabled = name.isNotBlank() && !busy,
                ) { Text(if (busy) "Đang lưu…" else "Lưu ví") }
            }
        }
    }
}

@Composable
private fun TransferEditor(wallets: List<Wallet>, busy: Boolean, onDismiss: () -> Unit, onTransfer: (String, String, Long, String) -> Unit) {
    var source by remember { mutableStateOf(wallets.firstOrNull()?.id.orEmpty()) }
    var destination by remember { mutableStateOf(wallets.getOrNull(1)?.id.orEmpty()) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        GlassDialogSurface {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Chuyển tiền", style = MaterialTheme.typography.titleLarge)
                Text("Ví nguồn", fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(wallets) { wallet -> FilterChip(source == wallet.id, { source = wallet.id; if (destination == wallet.id) destination = wallets.firstOrNull { it.id != wallet.id }?.id.orEmpty() }, { Text(wallet.name) }) } }
                Text("Ví nhận", fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(wallets.filter { it.id != source }) { wallet -> FilterChip(destination == wallet.id, { destination = wallet.id }, { Text(wallet.name) }) } }
                OutlinedTextField(
                    amount, { amount = it.filter(Char::isDigit).take(15) }, Modifier.fillMaxWidth(),
                    label = { Text("Số tiền") },
                    supportingText = { Text((amount.toLongOrNull() ?: 0L).toVnd()) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
                )
                OutlinedTextField(note, { note = it.take(120) }, Modifier.fillMaxWidth(), label = { Text("Ghi chú") })
                Button({ onTransfer(source, destination, amount.toLongOrNull() ?: 0, note.trim()) }, Modifier.fillMaxWidth(), enabled = amount.toLongOrNull()?.let { it > 0 } == true && source != destination && !busy) {
                    Text(if (busy) "Đang chuyển…" else "Xác nhận chuyển tiền")
                }
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
