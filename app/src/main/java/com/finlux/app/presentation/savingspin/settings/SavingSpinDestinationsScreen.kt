package com.finlux.app.presentation.savingspin.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.SavingDestination
import com.finlux.app.domain.model.SavingMethod
import com.finlux.app.domain.model.Wallet
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingSpinDestinationsScreen(
    onBack: () -> Unit,
    viewModel: SavingSpinDestinationsViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val tokens = LocalFinluxTokens.current
    var editingDestination by remember { mutableStateOf<SavingDestination?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var deleteCandidate by remember { mutableStateOf<SavingDestination?>(null) }

    if (showEditor) {
        DestinationEditorDialog(
            destination = editingDestination,
            wallets = state.wallets,
            onDismiss = {
                showEditor = false
                editingDestination = null
            },
            onSave = { name, method, walletId ->
                viewModel.saveDestination(
                    existing = editingDestination,
                    name = name,
                    method = method,
                    linkedWalletId = walletId,
                )
                showEditor = false
                editingDestination = null
            },
        )
    }

    deleteCandidate?.let { destination ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Xóa nơi tiết kiệm?") },
            text = {
                Text(
                    if (state.config.defaultDestinationId == destination.id) {
                        "Đây đang là nơi mặc định. Khi xóa, FinLux sẽ tự bỏ lựa chọn mặc định."
                    } else {
                        "Các lượt quay cũ vẫn giữ tên/giá trị lịch sử, nhưng nơi này sẽ không còn dùng cho lượt mới."
                    },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDestination(destination)
                        deleteCandidate = null
                    },
                ) { Text("Xóa") }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) { Text("Hủy") }
            },
        )
    }

    Scaffold(
        containerColor = tokens.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Nơi tiết kiệm", fontWeight = FontWeight.Bold)
                        Text(
                            "${state.destinations.size} nơi đã thiết lập",
                            style = MaterialTheme.typography.labelMedium,
                            color = tokens.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = tokens.background,
                    titleContentColor = tokens.onSurface,
                    navigationIconContentColor = tokens.onSurface,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingDestination = null
                    showEditor = true
                },
                containerColor = tokens.primary,
                contentColor = tokens.onHero,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm nơi tiết kiệm")
            }
        },
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = tokens.primary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 100.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.message?.let { message ->
                item(key = "message") {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = tokens.primary.copy(alpha = if (tokens.isDark) 0.18f else 0.08f),
                        border = BorderStroke(1.dp, tokens.primary.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(viewModel::consumeMessage),
                    ) {
                        Text(
                            text = message,
                            color = tokens.onSurface,
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            item(key = "intro") {
                Text(
                    text = "FinLux có thể ghi nhận thủ công với heo đất/két tiền, hoặc liên kết một ví để khoản tiết kiệm được phản ánh trực tiếp trong sổ cái.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.onSurfaceVariant,
                )
            }

            if (state.destinations.isEmpty()) {
                item(key = "empty") {
                    FinluxEmptyState(
                        title = "Chưa có nơi tiết kiệm",
                        description = "Tạo nơi đầu tiên để dùng khi xác nhận kết quả vòng quay.",
                    )
                }
            } else {
                items(
                    items = state.destinations,
                    key = { it.id },
                ) { destination ->
                    DestinationRow(
                        destination = destination,
                        linkedWallet = destination.linkedWalletId?.let { id ->
                            state.wallets.firstOrNull { it.id == id }
                        },
                        isDefault = state.config.defaultDestinationId == destination.id,
                        busy = state.isBusy,
                        onSetDefault = {
                            if (state.config.defaultDestinationId == destination.id) {
                                viewModel.setDefaultDestination(null)
                            } else {
                                viewModel.setDefaultDestination(destination)
                            }
                        },
                        onToggleEnabled = { viewModel.setDestinationEnabled(destination, it) },
                        onEdit = {
                            editingDestination = destination
                            showEditor = true
                        },
                        onDelete = { deleteCandidate = destination },
                    )
                }
            }
        }
    }
}

@Composable
private fun DestinationRow(
    destination: SavingDestination,
    linkedWallet: Wallet?,
    isDefault: Boolean,
    busy: Boolean,
    onSetDefault: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = tokens.surfaceSoft,
        border = BorderStroke(
            1.dp,
            if (isDefault) tokens.primary.copy(alpha = 0.55f) else tokens.border,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CircleShape,
                    color = tokens.primary.copy(alpha = if (tokens.isDark) 0.20f else 0.10f),
                    modifier = Modifier.size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (destination.method == SavingMethod.CASH) {
                                Icons.Default.Savings
                            } else {
                                Icons.Default.AccountBalance
                            },
                            contentDescription = null,
                            tint = tokens.primary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = destination.name,
                            fontWeight = FontWeight.Bold,
                            color = tokens.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (isDefault) {
                            Text(
                                text = "Mặc định",
                                style = MaterialTheme.typography.labelSmall,
                                color = tokens.primary,
                            )
                        }
                    }
                    Text(
                        text = when {
                            destination.method == SavingMethod.CASH && linkedWallet == null ->
                                "Tiền mặt · Ghi nhận thủ công"
                            destination.method == SavingMethod.CASH ->
                                "Tiền mặt · ${linkedWallet?.name ?: "Ví liên kết không còn tồn tại"}"
                            else ->
                                "Chuyển khoản · ${linkedWallet?.name ?: "Chưa liên kết ví"}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Switch(
                    checked = destination.enabled,
                    enabled = !busy,
                    onCheckedChange = onToggleEnabled,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onSetDefault,
                    enabled = !busy && destination.enabled,
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(if (isDefault) "Bỏ mặc định" else "Đặt mặc định")
                }
                IconButton(onClick = onEdit, enabled = !busy) {
                    Icon(Icons.Default.Edit, contentDescription = "Sửa")
                }
                IconButton(onClick = onDelete, enabled = !busy) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa")
                }
            }
        }
    }
}

@Composable
private fun DestinationEditorDialog(
    destination: SavingDestination?,
    wallets: List<Wallet>,
    onDismiss: () -> Unit,
    onSave: (String, SavingMethod, String?) -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    var name by remember(destination?.id) { mutableStateOf(destination?.name.orEmpty()) }
    var method by remember(destination?.id) {
        mutableStateOf(destination?.method ?: SavingMethod.CASH)
    }
    var linkedWalletId by remember(destination?.id) {
        mutableStateOf(destination?.linkedWalletId)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = tokens.surface,
            border = BorderStroke(1.dp, tokens.border),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    if (destination == null) "Thêm nơi tiết kiệm" else "Sửa nơi tiết kiệm",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = tokens.onSurface,
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(80) },
                    label = { Text("Tên nơi tiết kiệm") },
                    placeholder = { Text("Ví dụ: Heo đất, Quỹ MB...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    "Hình thức",
                    style = MaterialTheme.typography.labelLarge,
                    color = tokens.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = method == SavingMethod.CASH,
                        onClick = { method = SavingMethod.CASH },
                        label = { Text("Tiền mặt") },
                        leadingIcon = {
                            Icon(Icons.Default.Savings, null, Modifier.size(16.dp))
                        },
                    )
                    FilterChip(
                        selected = method == SavingMethod.BANK_TRANSFER,
                        onClick = { method = SavingMethod.BANK_TRANSFER },
                        label = { Text("Chuyển khoản") },
                        leadingIcon = {
                            Icon(Icons.Default.AccountBalance, null, Modifier.size(16.dp))
                        },
                    )
                }

                Text(
                    if (method == SavingMethod.CASH) {
                        "Ví liên kết (tùy chọn)"
                    } else {
                        "Ví nhận tiền tiết kiệm"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = tokens.onSurfaceVariant,
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 230.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (method == SavingMethod.CASH) {
                        item(key = "manual") {
                            WalletSelectionRow(
                                label = "Không liên kết · Ghi nhận thủ công",
                                selected = linkedWalletId == null,
                                onClick = { linkedWalletId = null },
                            )
                        }
                    }
                    items(wallets, key = { it.id }) { wallet ->
                        WalletSelectionRow(
                            label = wallet.name,
                            selected = linkedWalletId == wallet.id,
                            onClick = { linkedWalletId = wallet.id },
                        )
                    }
                }

                if (method == SavingMethod.BANK_TRANSFER && linkedWalletId == null) {
                    Text(
                        "Chuyển khoản bắt buộc chọn ví nhận.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("Hủy") }
                    Button(
                        onClick = { onSave(name, method, linkedWalletId) },
                        enabled = name.trim().isNotEmpty() &&
                            (method != SavingMethod.BANK_TRANSFER || linkedWalletId != null),
                    ) {
                        Text("Lưu")
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletSelectionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) {
            tokens.primary.copy(alpha = if (tokens.isDark) 0.20f else 0.10f)
        } else {
            tokens.surfaceSoft
        },
        border = BorderStroke(1.dp, if (selected) tokens.primary else tokens.border),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                color = tokens.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (selected) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = tokens.primary,
                    modifier = Modifier.size(17.dp),
                )
            }
        }
    }
}
