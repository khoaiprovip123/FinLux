package com.finlux.app.presentation.wallet.prism

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.component.FinluxBottomSheet
import com.finlux.app.core.designsystem.component.FinluxDialog
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.component.FinluxHeroCard
import com.finlux.app.core.designsystem.component.FinluxScreenHeader
import com.finlux.app.core.designsystem.component.FinluxSoftCard
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.presentation.wallet.WalletsViewModel
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrismWalletsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: ((String) -> Unit)? = null,
    onAdd: (() -> Unit)? = null,
    transferRequestKey: Int = 0,
    viewModel: WalletsViewModel = hiltViewModel(),
) {
    val wallets = viewModel.wallets.collectAsStateWithLifecycle().value
    val actionState = viewModel.actionState.collectAsStateWithLifecycle().value
    val totalBalance = wallets.sumOf { it.balance.value }
    val snackbar = remember { SnackbarHostState() }
    val tokens = LocalFinluxTokens.current

    var editingWallet by remember { mutableStateOf<Wallet?>(null) }
    var isCreatingWallet by remember { mutableStateOf(false) }
    var isTransferring by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Wallet?>(null) }

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
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = tokens.spacing.lg,
                end = tokens.spacing.lg,
                top = tokens.spacing.sm,
                bottom = 32.dp,
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
                        onClick = { editingWallet = wallet },
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
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(getWalletColor(wallet.type).copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = getWalletIcon(wallet.type),
                                        contentDescription = null,
                                        tint = getWalletColor(wallet.type),
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
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
        val target = editingWallet
        var name by remember(target) { mutableStateOf(target?.name ?: "") }
        var initialAmount by remember(target) { mutableStateOf(if (target != null && target.balance.value > 0L) target.balance.value.toString() else "") }
        var type by remember(target) { mutableStateOf(target?.type ?: WalletType.BANK) }

        FinluxBottomSheet(
            onDismissRequest = {
                isCreatingWallet = false
                editingWallet = null
            },
            title = if (target == null) "Thêm ví mới" else "Chỉnh sửa ví",
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.spacing.lg, vertical = tokens.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(tokens.spacing.md),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên ví") },
                    placeholder = { Text("Ví MoMo, Vietcombank, Tiền mặt...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.radius.input),
                )

                if (target == null) {
                    com.finlux.app.core.designsystem.component.FinluxAmountInputCard(
                        label = "Số dư ban đầu",
                        amountDigits = initialAmount,
                        onAmountChange = { initialAmount = it },
                        quickAmounts = listOf(500_000L, 1_000_000L, 2_000_000L, 5_000_000L, 10_000_000L, 50_000_000L),
                        primaryColor = tokens.primary,
                    )
                }

                Text("Loại ví", style = FinluxTextStyles.SectionTitle, color = tokens.onSurface)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(
                        WalletType.BANK to "Ngân hàng",
                        WalletType.EWALLET to "Ví điện tử",
                        WalletType.CASH to "Tiền mặt",
                    ).forEach { (t, l) ->
                        val isSelected = type == t
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 42.dp)
                                .clip(RoundedCornerShape(tokens.radius.smallChip))
                                .clickable { type = t },
                            color = if (isSelected) tokens.primary.copy(alpha = 0.15f) else tokens.surfaceSoft,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, tokens.primary) else null,
                            shape = RoundedCornerShape(tokens.radius.smallChip),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = l,
                                    style = FinluxTextStyles.Caption.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    ),
                                    color = if (isSelected) tokens.primary else tokens.onSurface,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        val amount = initialAmount.toLongOrNull() ?: 0L
                        val walletToSave = target?.copy(name = name, type = type)
                            ?: Wallet(
                                id = "",
                                name = name,
                                type = type,
                                balance = Money(amount),
                                colorHex = "#1D74F5",
                                isDefault = false,
                                createdAt = Instant.now(),
                            )
                        viewModel.save(walletToSave) {
                            isCreatingWallet = false
                            editingWallet = null
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(tokens.radius.input),
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.primary),
                    enabled = name.isNotBlank(),
                ) {
                    Text("Lưu ví", fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Transfer Money Bottom Sheet
    if (isTransferring && wallets.size >= 2) {
        var sourceWalletId by remember { mutableStateOf(wallets[0].id) }
        var destWalletId by remember { mutableStateOf(wallets[1].id) }
        var transferAmount by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }

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
                com.finlux.app.core.designsystem.component.FinluxAmountInputCard(
                    label = "Số tiền chuyển",
                    amountDigits = transferAmount,
                    onAmountChange = { transferAmount = it },
                    quickAmounts = listOf(100_000L, 500_000L, 1_000_000L, 2_000_000L, 5_000_000L),
                    primaryColor = tokens.primary,
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú chuyển khoản") },
                    placeholder = { Text("Chuyển tiền tiết kiệm...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.radius.input),
                )

                Button(
                    onClick = {
                        val amount = transferAmount.toLongOrNull() ?: 0L
                        if (amount > 0L && sourceWalletId != destWalletId) {
                            viewModel.transfer(
                                sourceId = sourceWalletId,
                                destinationId = destWalletId,
                                amount = amount,
                                note = note,
                            ) {
                                isTransferring = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(tokens.radius.input),
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.primary),
                    enabled = (transferAmount.toLongOrNull() ?: 0L) > 0L && sourceWalletId != destWalletId,
                ) {
                    Text("Xác nhận chuyển", fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Delete Wallet Dialog
    pendingDelete?.let { wallet ->
        FinluxDialog(
            onDismissRequest = { pendingDelete = null },
            title = "Xóa ví ${wallet.name}?",
            message = "Ví này đang có số dư ${formatVndAmount(wallet.balance.value)}. Khi xóa, ví sẽ bị gỡ bỏ khỏi danh sách.",
            confirmLabel = "Xác nhận xóa",
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
