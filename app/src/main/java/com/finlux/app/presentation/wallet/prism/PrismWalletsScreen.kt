package com.finlux.app.presentation.wallet.prism

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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.platform.LocalContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import com.finlux.app.core.designsystem.component.FinluxSnackbarHost
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinanceAccentHexes
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.FinancialInstitutionLogo
import com.finlux.app.core.designsystem.InstitutionSelectorSection
import com.finlux.app.core.designsystem.findInstitutionForWallet
import com.finlux.app.core.designsystem.GlassBottomSheet
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.component.ErgonomicCompactAmountCard
import com.finlux.app.core.designsystem.component.ErgonomicFormRow
import com.finlux.app.core.designsystem.component.SimpleWalletPickerSheet
import com.finlux.app.core.designsystem.component.FinluxBottomSheet
import com.finlux.app.core.designsystem.component.FinluxDialog
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.component.FinluxHeroCard
import com.finlux.app.core.designsystem.component.FinluxScreenHeader
import com.finlux.app.core.designsystem.component.FinluxSoftCard
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.designsystem.walletIcon
import com.finlux.app.core.navigation.Route
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.presentation.home.toShortVnd
import com.finlux.app.presentation.home.toVnd
import com.finlux.app.presentation.wallet.WalletsViewModel
import com.finlux.app.presentation.wallet.WalletTransactionsBottomSheet

import com.finlux.app.domain.model.FinanceTransaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrismWalletsScreen(
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
    val actionState = viewModel.actionState.collectAsStateWithLifecycle().value
    val totalBalance = wallets.sumOf { it.balance.value }
    val snackbar = remember { SnackbarHostState() }
    val tokens = LocalFinluxTokens.current

    var viewingWalletTransactions by remember { mutableStateOf<Wallet?>(null) }
    var editingWallet by remember { mutableStateOf<Wallet?>(null) }
    var isCreatingWallet by remember { mutableStateOf(false) }
    var isTransferring by remember { mutableStateOf(false) }
    var initialTransferSourceId by remember { mutableStateOf<String?>(null) }
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
        if (transferRequestKey > 0) isTransferring = true
    }

    LaunchedEffect(actionState.message) {
        actionState.message?.let {
            snackbar.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            FinluxScreenHeader(
                title = "Ví & Tài khoản",
                subtitle = "Tổng ${wallets.size} ví • ${formatVndAmount(totalBalance, isCompact = true)}",
                onBack = onBack,
            )
        },
        containerColor = tokens.background,
        snackbarHost = { FinluxSnackbarHost(snackbar, hasBottomBar = onBack == null) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = tokens.spacing.lg,
                end = tokens.spacing.lg,
                top = tokens.spacing.sm,
                bottom = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(tokens.spacing.md),
        ) {
            // Total balance Hero card
            item {
                FinluxHeroCard(
                    title = "Tổng tài sản khả dụng",
                    amountText = formatVndAmount(totalBalance),
                    deltaText = "${wallets.size} nguồn tiền liên kết",
                    isPositiveDelta = true,
                )
            }

            // Quick Actions: Add Wallet + Transfer Money
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
                ) {
                    Button(
                        onClick = { isCreatingWallet = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(tokens.radius.input),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tokens.primary,
                            contentColor = Color.White,
                        ),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Thêm ví", style = FinluxTextStyles.CardTitle.copy(fontSize = 14.sp))
                    }

                    Button(
                        onClick = { isTransferring = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(tokens.radius.input),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tokens.surfaceSoft,
                            contentColor = tokens.onSurface,
                        ),
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = tokens.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Chuyển tiền", style = FinluxTextStyles.CardTitle.copy(fontSize = 14.sp))
                    }
                }
            }

            // Wallet list
            if (wallets.isEmpty()) {
                item {
                    FinluxEmptyState(
                        title = "Chưa có ví nào",
                        description = "Nhấn Thêm ví để tạo tài khoản tiền mặt hoặc ngân hàng.",
                    )
                }
            } else {
                items(wallets, key = { it.id }) { wallet ->
                    var showMenu by remember { mutableStateOf(false) }

                    FinluxSoftCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewingWalletTransactions = wallet
                        },
                        onLongClick = {
                            editingWallet = wallet
                        },
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                FinancialInstitutionLogo(
                                    institution = findInstitutionForWallet(wallet.name),
                                    walletType = wallet.type,
                                    customColorHex = wallet.colorHex,
                                    size = 46.dp,
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = wallet.name,
                                        style = FinluxTextStyles.CardTitle.copy(fontWeight = FontWeight.Bold),
                                        color = tokens.onSurface,
                                    )
                                    Text(
                                        text = getWalletTypeLabel(wallet.type),
                                        style = FinluxTextStyles.Caption,
                                        color = tokens.onSurfaceVariant,
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = formatVndAmount(wallet.balance.value),
                                    style = FinluxTextStyles.CardTitle.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                    ),
                                    color = tokens.onSurface,
                                )
                                Box {
                                    IconButton(onClick = { showMenu = true }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Tùy chọn",
                                            tint = tokens.onSurfaceVariant,
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false },
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Chỉnh sửa") },
                                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                            onClick = {
                                                showMenu = false
                                                editingWallet = wallet
                                            },
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Xóa ví", color = FinluxColors.ExpenseRed) },
                                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = FinluxColors.ExpenseRed) },
                                            onClick = {
                                                showMenu = false
                                                pendingDelete = wallet
                                            },
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

    // Add / Edit Wallet Bottom Sheet
    if (isCreatingWallet || editingWallet != null) {
        PrismWalletEditor(
            initial = editingWallet,
            walletsCount = wallets.size,
            busy = actionState.busy,
            onDismiss = {
                isCreatingWallet = false
                editingWallet = null
            },
            onSave = { wallet ->
                viewModel.save(wallet) {
                    isCreatingWallet = false
                    editingWallet = null
                }
            },
            onDelete = { wallet ->
                viewModel.delete(wallet)
                isCreatingWallet = false
                editingWallet = null
            },
        )
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
                editingWallet = walletToEdit
            },
            onTransferFromWallet = { walletToTransfer ->
                viewingWalletTransactions = null
                initialTransferSourceId = walletToTransfer.id
                isTransferring = true
            },
            onSelectTransaction = onSelectTransaction,
        )
    }

    // Transfer Money Bottom Sheet
    if (isTransferring && wallets.size >= 2) {
        var sourceWalletId by remember(wallets, initialTransferSourceId) {
            mutableStateOf(initialTransferSourceId ?: wallets.firstOrNull()?.id.orEmpty())
        }
        var destWalletId by remember(wallets) { mutableStateOf(wallets.getOrNull(1)?.id.orEmpty()) }
        var transferAmount by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }
        var selectedDate by remember { mutableStateOf(Instant.now()) }
        var showDatePicker by remember { mutableStateOf(false) }
        val context = LocalContext.current

        val sourceWallet = wallets.find { it.id == sourceWalletId }
        val destWallet = wallets.find { it.id == destWalletId }
        val parsedAmount = transferAmount.toLongOrNull() ?: 0L
        val isInsufficientFunds = sourceWallet != null && sourceWallet.type != WalletType.CARD && parsedAmount > sourceWallet.balance.value

        val localDate = selectedDate.atZone(ZoneId.systemDefault()).toLocalDate()
        val today = LocalDate.now()
        val dayPrefix = when (localDate) {
            today -> "Hôm nay, "
            today.minusDays(1) -> "Hôm qua, "
            else -> ""
        }
        val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm") }
        val formattedDate = dayPrefix + selectedDate.atZone(ZoneId.systemDefault()).format(dateFormatter)

        FinluxBottomSheet(
            onDismissRequest = { isTransferring = false },
            title = "Chuyển tiền giữa các ví",
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.spacing.lg, vertical = tokens.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(tokens.spacing.md),
            ) {
                // Header description & Swap button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Dịch chuyển số dư nhanh chóng và an toàn",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = {
                            if (destWalletId.isNotBlank() && sourceWalletId.isNotBlank()) {
                                val temp = sourceWalletId
                                sourceWalletId = destWalletId
                                destWalletId = temp
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(tokens.primary.copy(alpha = 0.12f)),
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Đổi chiều chuyển tiền",
                            tint = tokens.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                // Ví nguồn (Chuyển đi)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Ví nguồn (Chuyển đi)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = tokens.onSurface,
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp),
                    ) {
                        items(wallets, key = { it.id }) { wallet ->
                            val isSelected = wallet.id == sourceWalletId
                            val accentColor = colorFromHex(wallet.colorHex)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) accentColor.copy(alpha = 0.18f) else tokens.surfaceSoft,
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) accentColor else tokens.border.copy(alpha = 0.5f),
                                ),
                                modifier = Modifier.clickable {
                                    sourceWalletId = wallet.id
                                    if (destWalletId == wallet.id) {
                                        destWalletId = wallets.firstOrNull { it.id != wallet.id }?.id.orEmpty()
                                    }
                                },
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    val icon = if (wallet.type == WalletType.CARD) Icons.Default.CreditCard else Icons.Default.AccountBalanceWallet
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) accentColor else tokens.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Column {
                                        Text(
                                            text = wallet.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) tokens.onSurface else tokens.onSurfaceVariant,
                                        )
                                        Text(
                                            text = formatVndAmount(wallet.balance.value),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) accentColor else tokens.onSurfaceVariant.copy(alpha = 0.7f),
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Ví nhận (Chuyển đến)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Ví nhận (Chuyển đến)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = tokens.onSurface,
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp),
                    ) {
                        items(wallets.filter { it.id != sourceWalletId }, key = { it.id }) { wallet ->
                            val isSelected = wallet.id == destWalletId
                            val accentColor = colorFromHex(wallet.colorHex)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) accentColor.copy(alpha = 0.18f) else tokens.surfaceSoft,
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) accentColor else tokens.border.copy(alpha = 0.5f),
                                ),
                                modifier = Modifier.clickable {
                                    destWalletId = wallet.id
                                },
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    val icon = if (wallet.type == WalletType.CARD) Icons.Default.CreditCard else Icons.Default.AccountBalanceWallet
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) accentColor else tokens.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Column {
                                        Text(
                                            text = wallet.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) tokens.onSurface else tokens.onSurfaceVariant,
                                        )
                                        Text(
                                            text = formatVndAmount(wallet.balance.value),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) accentColor else tokens.onSurfaceVariant.copy(alpha = 0.7f),
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Nhập số tiền chuyển (ErgonomicCompactAmountCard)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ErgonomicCompactAmountCard(
                        label = "SỐ TIỀN CHUYỂN",
                        amountText = transferAmount,
                        onAmountChange = { transferAmount = it },
                        placeholder = "0",
                        amountColor = tokens.primary,
                        showSuggestions = true,
                    )
                    if (isInsufficientFunds && sourceWallet != null) {
                        Text(
                            text = "⚠️ Số dư ví nguồn không đủ (Khả dụng: ${formatVndAmount(sourceWallet.balance.value)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = FinluxColors.ExpenseRed,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp),
                        )
                    }
                }

                // Ergonomic Note Input Row
                com.finlux.app.core.designsystem.component.ErgonomicInputRow(
                    label = "Ghi chú chuyển khoản",
                    value = note,
                    onValueChange = { note = it.take(120) },
                    placeholder = "Chuyển tiền tiết kiệm...",
                    icon = Icons.Default.Edit,
                    iconTintColor = tokens.primary,
                    iconBgColor = tokens.primary.copy(alpha = 0.12f),
                    onClear = { note = "" },
                )

                // Ergonomic Date/Time Row
                ErgonomicFormRow(
                    label = "THỜI GIAN CHUYỂN TIỀN",
                    primaryValue = formattedDate,
                    secondaryValue = null,
                    icon = Icons.Default.CalendarMonth,
                    iconTintColor = tokens.primary,
                    iconBgColor = tokens.primary.copy(alpha = 0.12f),
                    onClick = { showDatePicker = true },
                )

                Button(
                    onClick = {
                        if (parsedAmount > 0L && sourceWalletId.isNotBlank() && destWalletId.isNotBlank() && sourceWalletId != destWalletId && !isInsufficientFunds) {
                            viewModel.transfer(
                                sourceId = sourceWalletId,
                                destinationId = destWalletId,
                                amount = parsedAmount,
                                note = note.trim(),
                                date = selectedDate,
                            ) {
                                isTransferring = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(tokens.radius.input),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.primary,
                        disabledContainerColor = tokens.primary.copy(alpha = 0.35f),
                    ),
                    enabled = parsedAmount > 0L && sourceWalletId.isNotBlank() && destWalletId.isNotBlank() && sourceWalletId != destWalletId && !isInsufficientFunds,
                ) {
                    Text(
                        text = "Xác nhận chuyển tiền",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        // Dialog chọn ngày & giờ
        if (showDatePicker) {
            val currentZoned = selectedDate.atZone(ZoneId.systemDefault())
            val initialDateUtcMillis = currentZoned.toLocalDate()
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = initialDateUtcMillis,
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                        if (selectedMillis != null) {
                            val selectedLocalDate = Instant.ofEpochMilli(selectedMillis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()

                            val timePickerDialog = android.app.TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    val newDateTime = selectedLocalDate.atTime(hourOfDay, minute)
                                    selectedDate = newDateTime.atZone(ZoneId.systemDefault()).toInstant()
                                },
                                currentZoned.hour,
                                currentZoned.minute,
                                true,
                            )
                            timePickerDialog.setOnCancelListener {
                                val newDateTime = selectedLocalDate.atTime(currentZoned.hour, currentZoned.minute)
                                selectedDate = newDateTime.atZone(ZoneId.systemDefault()).toInstant()
                            }
                            timePickerDialog.show()
                        }
                    }) {
                        Text("Tiếp tục (Chọn giờ)")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Hủy")
                    }
                },
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }

    // Delete Wallet Dialog
    pendingDelete?.let { wallet ->
        FinluxDialog(
            onDismissRequest = { pendingDelete = null },
            title = "Xóa ví ${wallet.name}?",
            message = "Ví này đang có số dư ${formatVndAmount(wallet.balance.value)}. Tất cả giao dịch thuộc ví này sẽ bị ảnh hưởng. Thao tác này không thể hoàn tác!",
            confirmLabel = if (deleteCountdown > 0) "Xác nhận xóa (${deleteCountdown}s)" else "Xóa Vĩnh Viễn",
            confirmEnabled = deleteCountdown == 0,
            isConfirmDestructive = true,
            dismissLabel = "Hủy",
            onConfirm = {
                viewModel.delete(wallet)
                pendingDelete = null
            },
        )
    }
}

private fun getWalletIcon(type: WalletType): ImageVector = when (type) {
    WalletType.CASH -> Icons.Default.Payments
    WalletType.BANK -> Icons.Default.AccountBalance
    WalletType.EWALLET -> Icons.Default.AccountBalanceWallet
    WalletType.CARD -> Icons.Default.CreditCard
    WalletType.INVESTMENT -> Icons.Default.Savings
    WalletType.OTHER -> Icons.Default.AccountBalanceWallet
}

private fun getWalletColor(type: WalletType): Color = when (type) {
    WalletType.CASH -> FinluxColors.IncomeGreen
    WalletType.BANK -> FinluxColors.PrimaryBlue
    WalletType.EWALLET -> FinluxColors.PrimaryViolet
    WalletType.CARD -> FinluxColors.PrimaryCyan
    WalletType.INVESTMENT -> FinluxColors.WarningAmber
    WalletType.OTHER -> FinluxColors.NeutralGray
}

private fun getWalletTypeLabel(type: WalletType): String = when (type) {
    WalletType.CASH -> "Tiền mặt"
    WalletType.BANK -> "Tài khoản ngân hàng"
    WalletType.EWALLET -> "Ví điện tử"
    WalletType.CARD -> "Thẻ tín dụng"
    WalletType.INVESTMENT -> "Tài khoản đầu tư"
    WalletType.OTHER -> "Khác"
}

private val WalletType.label: String get() = when (this) {
    WalletType.CASH -> "Tiền mặt"
    WalletType.BANK -> "Ngân hàng"
    WalletType.EWALLET -> "Ví điện tử"
    WalletType.CARD -> "Thẻ tín dụng"
    WalletType.INVESTMENT -> "Đầu tư"
    WalletType.OTHER -> "Ví khác"
}

@Composable
private fun PrismWalletEditor(
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
    var deleteCountdown by remember(showDeleteConfirm) { mutableStateOf(5) }

    LaunchedEffect(showDeleteConfirm) {
        if (showDeleteConfirm) {
            deleteCountdown = 5
            while (deleteCountdown > 0) {
                kotlinx.coroutines.delay(1000)
                deleteCountdown--
            }
        }
    }

    val isEditing = initial != null
    val isDefaultWallet = initial?.isDefault == true
    val isOnlyWallet = walletsCount <= 1 && isEditing
    val tokens = LocalFinluxTokens.current

    GlassBottomSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 760.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
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
                        text = if (!isEditing) "Thêm ví mới" else "Chi tiết & Chỉnh sửa ví",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = tokens.onSurface,
                    )
                    Text(
                        text = "Quản lý tài khoản và dòng tiền tập trung",
                        color = tokens.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                FinancialInstitutionLogo(
                    institution = findInstitutionForWallet(name),
                    walletType = type,
                    customColorHex = color,
                    size = 46.dp,
                )
            }

            // Chọn nhanh Mẫu Ngân hàng / Ví điện tử
            InstitutionSelectorSection(
                selectedInstitution = findInstitutionForWallet(name),
                onSelectInstitution = { inst ->
                    name = inst.shortName
                    type = inst.type
                    color = inst.colorHex
                },
            )

            // Tên ví / ngân hàng
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(36) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tên ví / ngân hàng") },
                placeholder = { Text("Ví dụ: Vietcombank, Momo, Tiền mặt...") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )

            // Loại tài khoản
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Loại tài khoản",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.onSurface,
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(WalletType.entries) { option ->
                        FilterChip(
                            selected = type == option,
                            onClick = { type = option },
                            label = {
                                Text(
                                    text = option.label,
                                    fontWeight = if (type == option) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                        )
                    }
                }
            }

            // Số dư ban đầu / Số dư hiện tại
            ErgonomicCompactAmountCard(
                label = if (!isEditing) "Số dư ban đầu" else "Số dư hiện tại",
                amountText = balance,
                onAmountChange = { balance = it },
                placeholder = "0",
                amountColor = tokens.primary,
                showSuggestions = true,
            )

            // Màu thẻ
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Màu thẻ",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.onSurface,
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(FinanceAccentHexes) { hex ->
                        val isSelected = hex == color
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(colorFromHex(hex, tokens.primary))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) tokens.onSurface else tokens.border,
                                    shape = CircleShape,
                                )
                                .clickable { color = hex },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            // Đặt làm ví mặc định
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(tokens.surfaceSoft)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = tokens.primary, modifier = Modifier.size(24.dp))
                    Column {
                        Text(
                            text = "Đặt làm ví mặc định",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = tokens.onSurface,
                        )
                        Text(
                            text = if (isDefaultWallet) "Ví này đang là ví mặc định của bạn"
                            else "Tự động chọn cho các giao dịch mới",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.onSurfaceVariant,
                        )
                    }
                }
                Switch(
                    checked = isDefault,
                    onCheckedChange = { if (!isDefaultWallet) isDefault = it },
                    enabled = !isDefaultWallet,
                )
            }

            // Nút Tạo ví mới / Lưu thay đổi
            Button(
                onClick = {
                    onSave(
                        Wallet(
                            id = initial?.id.orEmpty(),
                            name = name.trim(),
                            type = type,
                            balance = Money(balance.toLongOrNull() ?: 0L),
                            colorHex = color,
                            isDefault = isDefault,
                            createdAt = initial?.createdAt ?: Instant.now(),
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = name.isNotBlank() && !busy,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tokens.primary),
            ) {
                Text(
                    text = if (busy) "Đang lưu…" else (if (!isEditing) "Tạo ví mới" else "Lưu thay đổi"),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
            }

            // Nếu đang sửa ví: Cảnh báo hoặc nút xóa ví
            if (isEditing) {
                if (isDefaultWallet || isOnlyWallet) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(tokens.surfaceSoft)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = tokens.primary, modifier = Modifier.size(20.dp))
                        Text(
                            text = if (isDefaultWallet) "Không thể xóa ví mặc định. Vui lòng đặt ví khác làm mặc định trước khi xóa!"
                            else "Không thể xóa ví duy nhất còn lại trong ứng dụng.",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.onSurfaceVariant,
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
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Xóa ví này", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (showDeleteConfirm && initial != null) {
        FinluxDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = "Xóa ví ${initial.name}?",
            message = "Bạn có chắc chắn muốn xóa ví này? Tất cả giao dịch thuộc ví sẽ bị ảnh hưởng. Thao tác này không thể hoàn tác!",
            confirmLabel = if (deleteCountdown > 0) "Xác nhận xóa (${deleteCountdown}s)" else "Xóa Vĩnh Viễn",
            confirmEnabled = deleteCountdown == 0,
            isConfirmDestructive = true,
            dismissLabel = "Hủy",
            onConfirm = {
                showDeleteConfirm = false
                onDelete?.invoke(initial)
            },
        )
    }
}
