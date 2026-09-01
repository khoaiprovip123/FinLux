package com.finlux.app.presentation.wallet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.FinancialInstitutionLogo
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.component.FinluxBottomSheet
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.component.FinluxSoftCard
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.findInstitutionForWallet
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletTransactionsBottomSheet(
    wallet: Wallet,
    allTransactions: List<FinanceTransaction>,
    categories: Map<String, Category>,
    wallets: Map<String, Wallet> = emptyMap(),
    financeZone: ZoneId = ZoneId.systemDefault(),
    onDismiss: () -> Unit,
    onEditWallet: ((Wallet) -> Unit)? = null,
    onTransferFromWallet: ((Wallet) -> Unit)? = null,
    onSelectTransaction: ((FinanceTransaction) -> Unit)? = null,
) {
    val tokens = LocalFinluxTokens.current

    // Lọc các giao dịch liên quan đến ví này (nguồn hoặc đích chuyển tiền)
    val walletTransactions = remember(wallet.id, allTransactions) {
        allTransactions.filter { tx ->
            tx.walletId == wallet.id || tx.relatedWalletId == wallet.id
        }
    }

    val totalIncome = remember(walletTransactions) {
        walletTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount.value }
    }
    val totalExpense = remember(walletTransactions) {
        walletTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount.value }
    }

    val groupedTransactions = remember(walletTransactions, financeZone) {
        walletTransactions.groupBy { tx ->
            tx.date.atZone(financeZone).toLocalDate()
        }
    }

    val today = remember(financeZone) { LocalDate.now(financeZone) }
    val yesterday = remember(today) { today.minusDays(1) }

    FinluxBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 760.dp)
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. Header: Thông tin ví & Nút đóng
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    FinancialInstitutionLogo(
                        institution = findInstitutionForWallet(wallet.name),
                        walletType = wallet.type,
                        customColorHex = wallet.colorHex,
                        size = 46.dp,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = wallet.name,
                                style = FinluxTextStyles.SectionTitle.copy(fontWeight = FontWeight.Bold),
                                color = tokens.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (wallet.isDefault) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = tokens.primary.copy(alpha = 0.14f),
                                ) {
                                    Text(
                                        text = "Mặc định",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = tokens.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }
                        }
                        Text(
                            text = getWalletTypeDetailLabel(wallet.type),
                            style = FinluxTextStyles.Caption,
                            color = tokens.onSurfaceVariant,
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(tokens.surfaceSoft),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = tokens.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            // 2. Thẻ Số Dư & Thống Kê Nhanh Của Ví
            FinluxSoftCard(
                modifier = Modifier.fillMaxWidth(),
                padding = 16.dp,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "SỐ DƯ HIỆN TẠI",
                            style = FinluxTextStyles.Caption.copy(
                                letterSpacing = 0.5.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = tokens.onSurfaceVariant,
                        )
                        Text(
                            text = "${walletTransactions.size} giao dịch",
                            style = FinluxTextStyles.Caption,
                            color = tokens.onSurfaceVariant,
                        )
                    }

                    Text(
                        text = formatVndAmount(wallet.balance.value),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp,
                        ),
                        color = tokens.primary,
                    )

                    // 2 Hộp Thống Kê Thu / Chi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // Tổng Thu
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = FinluxColors.IncomeGreen.copy(alpha = if (tokens.isDark) 0.15f else 0.08f),
                            border = BorderStroke(1.dp, FinluxColors.IncomeGreen.copy(alpha = 0.20f)),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = FinluxColors.IncomeGreen,
                                    modifier = Modifier.size(16.dp),
                                )
                                Column {
                                    Text(
                                        text = "Tổng Thu",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                        color = tokens.onSurfaceVariant,
                                    )
                                    Text(
                                        text = "+${formatVndAmount(totalIncome)}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = FinluxColors.IncomeGreen,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }

                        // Tổng Chi
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = FinluxColors.ExpenseRed.copy(alpha = if (tokens.isDark) 0.15f else 0.08f),
                            border = BorderStroke(1.dp, FinluxColors.ExpenseRed.copy(alpha = 0.20f)),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = FinluxColors.ExpenseRed,
                                    modifier = Modifier.size(16.dp),
                                )
                                Column {
                                    Text(
                                        text = "Tổng Chi",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                        color = tokens.onSurfaceVariant,
                                    )
                                    Text(
                                        text = "−${formatVndAmount(totalExpense)}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = FinluxColors.ExpenseRed,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Tiêu đề danh sách giao dịch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Lịch sử giao dịch ví",
                    style = FinluxTextStyles.SectionTitle.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = tokens.onSurface,
                )
            }

            // 4. Danh sách giao dịch nhóm theo ngày
            if (walletTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                ) {
                    FinluxEmptyState(
                        title = "Chưa có giao dịch",
                        description = "Chưa có giao dịch thu, chi hoặc chuyển tiền nào thuộc ví ${wallet.name}.",
                        actionLabel = null,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                ) {
                    groupedTransactions.forEach { (date, txList) ->
                        item(key = "header_$date") {
                            val headerTitle = when (date) {
                                today -> "Hôm nay, ${date.format(DateTimeFormatter.ofPattern("dd/MM"))}"
                                yesterday -> "Hôm qua, ${date.format(DateTimeFormatter.ofPattern("dd/MM"))}"
                                else -> date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            }

                            Text(
                                text = headerTitle,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = tokens.onSurfaceVariant,
                                modifier = Modifier.padding(start = 2.dp, top = 8.dp, bottom = 2.dp),
                            )
                        }

                        items(
                            items = txList,
                            key = { it.id },
                        ) { transaction ->
                            val category = transaction.categoryId?.let { categories[it] }

                            WalletTransactionCard(
                                transaction = transaction,
                                category = category,
                                currentWalletId = wallet.id,
                                zone = financeZone,
                                onClick = { onSelectTransaction?.invoke(transaction) },
                            )
                        }
                    }
                }
            }

            // 5. Thanh Thao Tác Nhanh (Sửa ví / Chuyển tiền)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (onTransferFromWallet != null) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onTransferFromWallet(wallet)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(tokens.radius.input),
                        border = BorderStroke(1.dp, tokens.primary.copy(alpha = 0.40f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = tokens.primary,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Chuyển tiền",
                            style = FinluxTextStyles.Caption.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }

                if (onEditWallet != null) {
                    Button(
                        onClick = {
                            onDismiss()
                            onEditWallet(wallet)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(tokens.radius.input),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tokens.surfaceSoft,
                            contentColor = tokens.onSurface,
                        ),
                        border = BorderStroke(1.dp, tokens.border),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Chỉnh sửa ví",
                            style = FinluxTextStyles.Caption.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Thẻ giao dịch đơn lẻ dành riêng cho Sheet ví
 */
@Composable
private fun WalletTransactionCard(
    transaction: FinanceTransaction,
    category: Category?,
    currentWalletId: String,
    zone: ZoneId,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val isIncome = transaction.type == TransactionType.INCOME
    val isTransfer = transaction.type == TransactionType.TRANSFER_OUT || transaction.type == TransactionType.TRANSFER_IN
    val isTransferOut = transaction.type == TransactionType.TRANSFER_OUT || (isTransfer && transaction.walletId == currentWalletId)

    // Xác định dấu và màu số tiền
    val (amountPrefix, amountColor) = when {
        isIncome -> "+" to FinluxColors.IncomeGreen
        isTransfer -> if (isTransferOut) "−" to Color(0xFF3B82F6) else "+" to FinluxColors.IncomeGreen
        else -> "−" to FinluxColors.ExpenseRed
    }

    val displayAmount = amountPrefix + formatVndAmount(transaction.amount.value).replace("đ", "₫")

    val mainTitle = transaction.note.ifBlank {
        category?.name ?: if (isTransfer) "Chuyển tiền" else "Giao dịch"
    }

    val subTitle = when {
        isTransfer -> if (isTransferOut) "Chuyển đi" else "Chuyển đến"
        transaction.note.isNotBlank() && category != null -> category.name
        else -> if (isIncome) "Thu nhập" else "Chi tiêu"
    }

    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val timeText = remember(transaction.date, zone) {
        transaction.date.atZone(zone).format(timeFormatter)
    }

    val iconColorHex = category?.colorHex
    val parsedColor = remember(iconColorHex) {
        if (!iconColorHex.isNullOrBlank()) colorFromHex(iconColorHex) else null
    }

    val iconBackgroundBrush = remember(transaction.type, parsedColor) {
        when {
            isIncome -> Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
            isTransfer -> Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF6366F1)))
            parsedColor != null -> Brush.linearGradient(listOf(parsedColor, parsedColor.copy(alpha = 0.85f)))
            else -> Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626)))
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (tokens.isDark) tokens.surfaceSoft else Color.White,
        border = BorderStroke(1.dp, tokens.border),
        shadowElevation = if (tokens.isDark) 0.dp else 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBackgroundBrush),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = when {
                        isTransfer -> Icons.Default.SwapHoriz
                        isIncome -> Icons.AutoMirrored.Filled.TrendingUp
                        category != null -> Icons.AutoMirrored.Filled.ReceiptLong
                        else -> Icons.AutoMirrored.Filled.TrendingDown
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Title & Subtitle
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = mainTitle,
                    style = FinluxTextStyles.CardTitle.copy(
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = tokens.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = subTitle,
                        style = FinluxTextStyles.Caption.copy(fontSize = 11.5.sp),
                        color = tokens.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "•",
                        style = FinluxTextStyles.Caption,
                        color = tokens.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                    Text(
                        text = timeText,
                        style = FinluxTextStyles.Caption.copy(fontSize = 11.5.sp),
                        color = tokens.onSurfaceVariant,
                    )
                }
            }

            // Amount
            Text(
                text = displayAmount,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                ),
                color = amountColor,
            )
        }
    }
}

private fun getWalletTypeDetailLabel(type: WalletType): String = when (type) {
    WalletType.CASH -> "Ví Tiền mặt"
    WalletType.BANK -> "Tài khoản Ngân hàng"
    WalletType.EWALLET -> "Ví điện tử"
    WalletType.CARD -> "Thẻ tín dụng"
    WalletType.INVESTMENT -> "Tài khoản đầu tư"
    WalletType.OTHER -> "Ví thanh toán khác"
}
