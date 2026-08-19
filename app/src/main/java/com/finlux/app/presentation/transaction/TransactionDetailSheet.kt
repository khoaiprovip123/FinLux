package com.finlux.app.presentation.transaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.finlux.app.core.designsystem.ExpenseRed
import com.finlux.app.core.designsystem.FinluxBlue
import com.finlux.app.core.designsystem.FinluxTextSecondary
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.IncomeGreen
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.presentation.home.toVnd
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailSheet(
    transaction: FinanceTransaction,
    category: Category? = null,
    wallet: Wallet? = null,
    onDismiss: () -> Unit,
    onEdit: (FinanceTransaction) -> Unit,
    onDelete: (FinanceTransaction) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showEditConfirm by remember { mutableStateOf(false) }

    val isIncome = transaction.type == TransactionType.INCOME
    val accentColor = category?.let { colorFromHex(it.colorHex) } ?: if (isIncome) IncomeGreen else ExpenseRed
    val formattedDate = transaction.date.atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy"))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Chi tiết giao dịch",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Amount Hero Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(accentColor.copy(alpha = 0.10f))
                    .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                    .padding(vertical = 18.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isIncome) "KHOẢN THU NHẬP" else "KHOẢN CHI TIÊU",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        letterSpacing = 1.sp,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = (if (isIncome) "+" else "-") + transaction.amount.value.toVnd(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transaction Info Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    DetailRow(
                        icon = category?.let { categoryIcon(it.icon) } ?: Icons.Default.Info,
                        iconTint = accentColor,
                        label = "Danh mục",
                        value = category?.name ?: if (isIncome) "Thu nhập" else "Chi tiêu",
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    DetailRow(
                        icon = Icons.Default.AccountBalanceWallet,
                        iconTint = FinluxBlue,
                        label = "Ví thanh toán",
                        value = wallet?.name ?: "Ví tiền mặt",
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    DetailRow(
                        icon = Icons.Default.CalendarToday,
                        iconTint = MaterialTheme.colorScheme.primary,
                        label = "Thời gian",
                        value = formattedDate,
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    DetailRow(
                        icon = Icons.Default.Notes,
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "Ghi chú",
                        value = transaction.note.ifBlank { "Không có ghi chú" },
                    )

                    if (!transaction.receiptImageUrl.isNullOrBlank()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        DetailRow(
                            icon = Icons.Default.ReceiptLong,
                            iconTint = FinluxBlue,
                            label = "Hóa đơn đính kèm",
                            value = "Có 1 hóa đơn đính kèm",
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { showEditConfirm = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sửa", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed),
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Xóa", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showEditConfirm) {
        AlertDialog(
            onDismissRequest = { showEditConfirm = false },
            title = { Text("Chỉnh sửa giao dịch?", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có muốn mở cửa sổ chỉnh sửa thông tin giao dịch này?") },
            confirmButton = {
                TextButton(onClick = {
                    showEditConfirm = false
                    onDismiss()
                    onEdit(transaction)
                }) {
                    Text("Đồng ý", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditConfirm = false }) {
                    Text("Hủy")
                }
            },
        )
    }

    if (showDeleteConfirm) {
        DeleteTransactionConfirmDialog(
            transaction = transaction,
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDismiss()
                onDelete(transaction)
            },
        )
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(iconTint.copy(alpha = 0.12f), RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = FinluxTextSecondary)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun TransactionActionDialog(
    transaction: FinanceTransaction,
    category: Category? = null,
    onDismiss: () -> Unit,
    onViewDetails: (FinanceTransaction) -> Unit,
    onEdit: (FinanceTransaction) -> Unit,
    onDelete: (FinanceTransaction) -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val isIncome = transaction.type == TransactionType.INCOME
    val accentColor = category?.let { colorFromHex(it.colorHex) } ?: if (isIncome) IncomeGreen else ExpenseRed

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Tùy chọn giao dịch",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${transaction.note.ifBlank { category?.name ?: "Giao dịch" }} (${if (isIncome) "+" else "-"}${transaction.amount.value.toVnd()})",
                    style = MaterialTheme.typography.bodySmall,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ActionItemRow(
                    icon = Icons.Default.Visibility,
                    iconTint = FinluxBlue,
                    title = "Xem chi tiết",
                    subtitle = "Xem đầy đủ thông tin giao dịch",
                    onClick = {
                        onDismiss()
                        onViewDetails(transaction)
                    },
                )

                ActionItemRow(
                    icon = Icons.Default.Edit,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Sửa giao dịch",
                    subtitle = "Điều chỉnh số tiền, danh mục, ví, ngày...",
                    onClick = {
                        onDismiss()
                        onEdit(transaction)
                    },
                )

                ActionItemRow(
                    icon = Icons.Default.DeleteOutline,
                    iconTint = ExpenseRed,
                    title = "Xóa giao dịch",
                    subtitle = "Xóa và hoàn lại số dư ví tự động",
                    onClick = {
                        showDeleteConfirm = true
                    },
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        },
    )

    if (showDeleteConfirm) {
        DeleteTransactionConfirmDialog(
            transaction = transaction,
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDismiss()
                onDelete(transaction)
            },
        )
    }
}

@Composable
private fun ActionItemRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(iconTint.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = FinluxTextSecondary)
        }
    }
}

@Composable
fun DeleteTransactionConfirmDialog(
    transaction: FinanceTransaction,
    onDismiss: () -> Unit,
    onConfirm: (FinanceTransaction) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Xác nhận xóa giao dịch?", fontWeight = FontWeight.Bold) },
        text = {
            Text("Giao dịch sẽ bị xóa khỏi hệ thống. Số dư ví liên quan sẽ được tự động hoàn lại tương ứng. Thao tác này không thể hoàn tác.")
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(transaction) }) {
                Text("Xóa", color = ExpenseRed, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        },
    )
}
