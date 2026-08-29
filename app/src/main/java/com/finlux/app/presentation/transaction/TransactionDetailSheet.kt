package com.finlux.app.presentation.transaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.ExpenseRed
import com.finlux.app.core.designsystem.IncomeGreen
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.core.designsystem.component.formatVndAmount
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Bottom Sheet Modal for Transaction Details & Actions (matching UI spec image)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailSheet(
    transaction: FinanceTransaction,
    category: Category? = null,
    wallet: Wallet? = null,
    relatedWallet: Wallet? = null,
    onDismiss: () -> Unit,
    onEdit: (FinanceTransaction) -> Unit = {},
    onDelete: (FinanceTransaction) -> Unit = {},
) {
    val tokens = LocalFinluxTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isTransfer = transaction.type == TransactionType.TRANSFER_OUT || transaction.type == TransactionType.TRANSFER_IN
    val isIncome = transaction.type == TransactionType.INCOME

    val accentColor = when (transaction.type) {
        TransactionType.INCOME -> category?.let { colorFromHex(it.colorHex) } ?: IncomeGreen
        TransactionType.EXPENSE -> category?.let { colorFromHex(it.colorHex) } ?: ExpenseRed
        TransactionType.TRANSFER_OUT, TransactionType.TRANSFER_IN -> FinluxColors.TransferBlue
    }

    val headerIcon = when (transaction.type) {
        TransactionType.INCOME -> category?.let { categoryIcon(it.icon) } ?: Icons.Default.ArrowDownward
        TransactionType.EXPENSE -> category?.let { categoryIcon(it.icon) } ?: Icons.Default.LocalOffer
        TransactionType.TRANSFER_OUT, TransactionType.TRANSFER_IN -> Icons.Default.SwapHoriz
    }

    val badgeLabel = when (transaction.type) {
        TransactionType.INCOME -> "Khoản thu nhập"
        TransactionType.EXPENSE -> "Khoản chi tiêu"
        TransactionType.TRANSFER_OUT -> "Chuyển tiền đi"
        TransactionType.TRANSFER_IN -> "Nhận tiền chuyển"
    }

    val amountPrefix = when (transaction.type) {
        TransactionType.INCOME, TransactionType.TRANSFER_IN -> "+"
        TransactionType.EXPENSE, TransactionType.TRANSFER_OUT -> "-"
    }

    val formattedDate = transaction.date.atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm · dd/MM/yyyy"))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 6.dp)
                    .size(width = 44.dp, height = 4.5.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // 1. Header: Icon Category + Title + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = accentColor.copy(alpha = 0.12f),
                        modifier = Modifier.size(42.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = headerIcon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                    Text(
                        text = "Chi tiết giao dịch",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), CircleShape),
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            // 2. Hero Amount Card (with subtle gradient, pill tag, large amount)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.09f),
                                accentColor.copy(alpha = 0.03f),
                            ),
                        ),
                    )
                    .border(BorderStroke(1.dp, accentColor.copy(alpha = 0.22f)), RoundedCornerShape(22.dp))
                    .padding(vertical = 18.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // Pill Badge with Dot
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(accentColor),
                            )
                            Text(
                                text = badgeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = accentColor,
                                fontSize = 11.5.sp,
                            )
                        }
                    }

                    // Display Amount
                    Text(
                        text = "$amountPrefix${formatVndAmount(transaction.amount.value)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        letterSpacing = (-0.5).sp,
                    )
                }
            }

            // 3. Information Section (Rounded Glass Card with 4 rows)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    // Danh mục / Loại giao dịch
                    DetailItemRow(
                        icon = headerIcon,
                        iconTint = accentColor,
                        iconBg = accentColor.copy(alpha = 0.12f),
                        label = if (isTransfer) "Loại giao dịch" else "Danh mục",
                        value = if (isTransfer) "Chuyển tiền giữa các ví" else (category?.name ?: if (isIncome) "Thu nhập" else "Chi tiêu"),
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    )

                    // Ví thanh toán / Định tuyến ví
                    val walletLabel = if (isTransfer) "Định tuyến ví" else "Ví thanh toán"
                    val walletValue = when (transaction.type) {
                        TransactionType.TRANSFER_OUT -> if (relatedWallet != null) "${wallet?.name ?: "Ví nguồn"} ➔ ${relatedWallet.name}" else wallet?.name ?: "Ví nguồn"
                        TransactionType.TRANSFER_IN -> if (relatedWallet != null) "${relatedWallet.name} ➔ ${wallet?.name ?: "Ví nhận"}" else wallet?.name ?: "Ví nhận"
                        else -> wallet?.name ?: "Ví tiền mặt"
                    }
                    DetailItemRow(
                        icon = if (isTransfer) Icons.Default.SwapHoriz else Icons.Default.AccountBalanceWallet,
                        iconTint = Color(0xFF2563EB),
                        iconBg = Color(0xFF2563EB).copy(alpha = 0.12f),
                        label = walletLabel,
                        value = walletValue,
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    )

                    // Thời gian
                    DetailItemRow(
                        icon = Icons.Default.CalendarToday,
                        iconTint = Color(0xFF0284C7),
                        iconBg = Color(0xFF0284C7).copy(alpha = 0.12f),
                        label = "Thời gian",
                        value = formattedDate,
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    )

                    // Ghi chú
                    DetailItemRow(
                        icon = Icons.AutoMirrored.Filled.Notes,
                        iconTint = Color(0xFF7C3AED),
                        iconBg = Color(0xFF7C3AED).copy(alpha = 0.12f),
                        label = "Ghi chú",
                        value = transaction.note.ifBlank { "Không có ghi chú" },
                    )

                    if (!transaction.receiptImageUrl.isNullOrBlank()) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        )
                        DetailItemRow(
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            iconTint = Color(0xFF2563EB),
                            iconBg = Color(0xFF2563EB).copy(alpha = 0.12f),
                            label = "Hóa đơn đính kèm",
                            value = "Có 1 hóa đơn đính kèm",
                        )
                    }
                }
            }

            // 4. Action Cards (Khóa Sửa với Chuyển tiền / Đầy đủ Sửa & Xóa với Thu & Chi)
            if (isTransfer) {
                // Info Banner giải thích tính toàn vẹn 2 đầu ví
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = FinluxColors.TransferBlue.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, FinluxColors.TransferBlue.copy(alpha = 0.22f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = FinluxColors.TransferBlue.copy(alpha = 0.16f),
                            modifier = Modifier.size(32.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = FinluxColors.TransferBlue,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Giao dịch chuyển tiền liên kết 2 ví",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = FinluxColors.TransferBlue,
                            )
                            Text(
                                text = "Không thể chỉnh sửa đơn lẻ để bảo toàn số dư hai đầu ví. Bạn có thể xóa để hoàn tác cả cặp.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    lineHeight = 14.sp,
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Nút Xóa toàn chiều rộng cho Chuyển tiền
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = ExpenseRed.copy(alpha = 0.06f),
                    border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.18f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                        ) {
                            onDismiss()
                            onDelete(transaction)
                        },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ExpenseRed.copy(alpha = 0.14f),
                            modifier = Modifier.size(36.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Xóa",
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Xóa giao dịch chuyển tiền này",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = ExpenseRed,
                                maxLines = 1,
                            )
                            Text(
                                text = "Xóa cả cặp giao dịch và hoàn lại số dư cho cả hai ví",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    lineHeight = 13.sp,
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }
            } else {
                // Giao dịch Thu / Chi thông thường: Hiển thị đầy đủ cả 2 nút Sửa và Xóa
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Sửa Card
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = tokens.primary.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, tokens.primary.copy(alpha = 0.22f)),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                            ) {
                                onDismiss()
                                onEdit(transaction)
                            },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = tokens.primary.copy(alpha = 0.14f),
                                modifier = Modifier.size(36.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Sửa",
                                        tint = tokens.primary,
                                        modifier = Modifier.size(17.dp),
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Chỉnh sửa giao dịch",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = tokens.primary,
                                    maxLines = 1,
                                )
                                Text(
                                    text = "Chỉnh sửa thông tin giao dịch",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        lineHeight = 13.sp,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(15.dp),
                            )
                        }
                    }

                    // Xóa Card
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = ExpenseRed.copy(alpha = 0.06f),
                        border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.18f)),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                            ) {
                                onDismiss()
                                onDelete(transaction)
                            },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = ExpenseRed.copy(alpha = 0.14f),
                                modifier = Modifier.size(36.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Xóa",
                                        tint = ExpenseRed,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Xóa giao dịch này",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = ExpenseRed,
                                    maxLines = 1,
                                )
                                Text(
                                    text = "Xóa vĩnh viễn giao dịch khỏi FinLux",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        lineHeight = 13.sp,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(15.dp),
                            )
                        }
                    }
                }
            }

            // 5. Security Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(15.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Giao dịch được bảo mật tuyệt đối",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DetailItemRow(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = iconBg,
            modifier = Modifier.size(38.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(19.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/**
 * Centered Action Dialog for long-press
 */
@Composable
fun TransactionActionDialog(
    transaction: FinanceTransaction,
    category: Category? = null,
    wallet: Wallet? = null,
    relatedWallet: Wallet? = null,
    onDismiss: () -> Unit,
    onViewDetails: ((FinanceTransaction) -> Unit)? = null,
    onEdit: (FinanceTransaction) -> Unit,
    onDelete: (FinanceTransaction) -> Unit,
) {
    TransactionDetailSheet(
        transaction = transaction,
        category = category,
        wallet = wallet,
        relatedWallet = relatedWallet,
        onDismiss = onDismiss,
        onEdit = onEdit,
        onDelete = onDelete,
    )
}

/**
 * Confirmation dialog before deleting a transaction (with atomic cascade support for transfers)
 */
@Composable
fun DeleteTransactionConfirmDialog(
    transaction: FinanceTransaction,
    relatedWallet: Wallet? = null,
    onDismiss: () -> Unit,
    onConfirm: (FinanceTransaction) -> Unit,
) {
    val isTransfer = transaction.type == TransactionType.TRANSFER_OUT || transaction.type == TransactionType.TRANSFER_IN
    val titleText = if (isTransfer) "Xóa giao dịch chuyển tiền?" else "Xác nhận xóa giao dịch?"
    val messageText = if (isTransfer) {
        val destName = relatedWallet?.name ?: "ví liên kết"
        "Bạn có chắc muốn xóa giao dịch chuyển tiền này? Thao tác này sẽ đồng thời xóa giao dịch đối ứng tại ví $destName và hoàn lại số dư cho cả hai ví."
    } else {
        "Giao dịch sẽ bị xóa khỏi hệ thống. Số dư ví liên quan sẽ được tự động hoàn lại tương ứng. Thao tác này không thể hoàn tác."
    }
    val confirmText = if (isTransfer) "Xóa cả cặp giao dịch" else "Xóa"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titleText, fontWeight = FontWeight.Bold) },
        text = { Text(messageText) },
        confirmButton = {
            TextButton(onClick = { onConfirm(transaction) }) {
                Text(confirmText, color = ExpenseRed, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        },
    )
}
