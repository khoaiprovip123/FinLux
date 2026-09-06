package com.finlux.app.presentation.wallet.modern

import com.finlux.app.core.designsystem.theme.FinluxPalette

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.finlux.app.core.designsystem.component.ErgonomicCompactAmountCard
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.platform.LocalContext
import com.finlux.app.core.designsystem.component.ErgonomicFormRow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import com.finlux.app.core.designsystem.component.FinluxSnackbarHost
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
import com.finlux.app.core.designsystem.FinancialInstitutionLogo
import com.finlux.app.core.designsystem.InstitutionSelectorSection
import com.finlux.app.core.designsystem.findInstitutionForWallet
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
import com.finlux.app.presentation.wallet.WalletTransactionsBottomSheet

import com.finlux.app.domain.model.FinanceTransaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernWalletsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: ((String) -> Unit)? = null,
    onAdd: (() -> Unit)? = null,
    onSelectWallet: ((Wallet) -> Unit)? = null,
    onSelectTransaction: ((FinanceTransaction) -> Unit)? = null,
    transferRequestKey: Int = 0,
    viewModel: WalletsViewModel = hiltViewModel(),
) {
    val wallets = viewModel.wallets.collectAsStateWithLifecycle().value
    val categories = viewModel.categories.collectAsStateWithLifecycle().value
    val recentTransactions = viewModel.recentTransactions.collectAsStateWithLifecycle().value
    val financeZone = viewModel.financeZone.collectAsStateWithLifecycle().value
    val action = viewModel.actionState.collectAsStateWithLifecycle().value
    var selectedFilter by remember { mutableStateOf<WalletType?>(null) }
    val displayedWallets = if (selectedFilter == null) wallets else wallets.filter { it.type == selectedFilter }
    val snackbar = remember { SnackbarHostState() }
    var viewingWalletTransactions by remember { mutableStateOf<Wallet?>(null) }
    var editing by remember { mutableStateOf<Wallet?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var showTransfer by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Wallet?>(null) }
    var deleteCountdown by remember(pendingDelete) { mutableStateOf(5) }

    LaunchedEffect(pendingDelete) {
        if (pendingDelete != null) {
            deleteCountdown = 5
            while (deleteCountdown > 0) {
                kotlinx.coroutines.delay(1000)
                deleteCountdown--
            }
        }
    }

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
            snackbarHost = { FinluxSnackbarHost(snackbar, hasBottomBar = onBack == null) },
            containerColor = Color.Transparent,
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    val total = wallets.sumOf { it.balance.value }
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        GradientHeroCard(Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Tổng số dư", color = FinluxPalette.White.copy(alpha = .85f), style = MaterialTheme.typography.bodySmall)
                                Text(total.toVnd(), color = FinluxPalette.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                Text("${wallets.size} ví · quản lý tập trung và an toàn", color = FinluxPalette.White.copy(alpha = .78f), style = MaterialTheme.typography.labelSmall)
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
                    val ratio = if (total > 0 && wallet.balance.value > 0) ((wallet.balance.value * 100) / total).toInt() else 0
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
                                onClick = {
                                    viewingWalletTransactions = wallet
                                },
                                onLongClick = {
                                    editing = wallet
                                    showEditor = true
                                },
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    FinancialInstitutionLogo(
                                        institution = findInstitutionForWallet(wallet.name),
                                        walletType = wallet.type,
                                        customColorHex = wallet.colorHex,
                                        size = 46.dp,
                                        shape = RoundedCornerShape(14.dp),
                                    )
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

    // Wallet Transactions Bottom Sheet (Nhấn vào thẻ ví)
    viewingWalletTransactions?.let { targetWallet ->
        val currentWallet = wallets.find { it.id == targetWallet.id } ?: targetWallet
        WalletTransactionsBottomSheet(
            wallet = currentWallet,
            allTransactions = recentTransactions,
            categories = categories,
            financeZone = financeZone,
            onDismiss = { viewingWalletTransactions = null },
            onEditWallet = { walletToEdit ->
                viewingWalletTransactions = null
                editing = walletToEdit
                showEditor = true
            },
            onTransferFromWallet = {
                viewingWalletTransactions = null
                showTransfer = true
            },
            onSelectTransaction = onSelectTransaction,
        )
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
        TransferEditor(wallets, action.busy, { showTransfer = false }) { source, destination, amount, note, date ->
            viewModel.transfer(source, destination, amount, note, date) { showTransfer = false }
        }
    }
    pendingDelete?.let { wallet ->
        GlassAlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Xóa ví ${wallet.name}?") },
            text = { Text("Bạn có chắc chắn muốn xóa ví này? Tất cả giao dịch thuộc ví này sẽ bị ảnh hưởng. Thao tác này không thể hoàn tác!") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(wallet)
                        pendingDelete = null
                    },
                    enabled = deleteCountdown == 0,
                ) {
                    Text(
                        if (deleteCountdown > 0) "Xác nhận xóa (${deleteCountdown}s)" else "Xóa Vĩnh Viễn",
                        color = if (deleteCountdown == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold,
                    )
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
