package com.finlux.app.core.designsystem.component

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * VND Currency Formatter (FinLux Prism Spec 23 / UI-FIX-09)
 */
fun formatVndAmount(amount: Long, isCompact: Boolean = false): String {
    return if (isCompact && amount >= 1_000_000) {
        val millions = amount.toDouble() / 1_000_000.0
        val df = DecimalFormat("#.#", DecimalFormatSymbols(Locale("vi", "VN")))
        "${df.format(millions)} tr"
    } else {
        val symbols = DecimalFormatSymbols(Locale("vi", "VN")).apply {
            groupingSeparator = '.'
        }
        val df = DecimalFormat("#,###", symbols)
        "${df.format(amount)} đ"
    }
}

/**
 * Standard Semantic Color Resolver (FinLux Prism Spec 23 / UI-FIX-01)
 * Strictly ensures Income is green, Expense is red, Transfer is blue.
 */
fun getTransactionSemanticColor(type: TransactionType): Color = when (type) {
    TransactionType.INCOME -> FinluxColors.IncomeGreen
    TransactionType.EXPENSE -> FinluxColors.ExpenseRed
    TransactionType.TRANSFER_OUT, TransactionType.TRANSFER_IN -> FinluxColors.TransferBlue
}

/**
 * Standard Amount Text Component (FinLux Prism Spec 24 & UI-FIX-01)
 */
@Composable
fun FinluxAmountText(
    amount: Long,
    type: TransactionType? = null,
    modifier: Modifier = Modifier,
    explicitColor: Color? = null,
    isCompact: Boolean = false,
    textStyle: androidx.compose.ui.text.TextStyle = FinluxTextStyles.CardTitle,
    showSign: Boolean = true,
) {
    val resolvedColor = explicitColor ?: (type?.let { getTransactionSemanticColor(it) } ?: LocalFinluxTokens.current.onSurface)
    val sign = if (showSign && type != null) {
        when (type) {
            TransactionType.INCOME -> "+"
            TransactionType.EXPENSE -> "-"
            TransactionType.TRANSFER_OUT -> "-"
            TransactionType.TRANSFER_IN -> "+"
        }
    } else ""

    Text(
        text = "$sign${formatVndAmount(amount, isCompact)}",
        style = textStyle,
        color = resolvedColor,
        fontWeight = FontWeight.Bold,
        modifier = modifier,
    )
}

/**
 * Standard Filter Chip (FinLux Prism Spec 9 & UI-FIX-12)
 */
@Composable
fun FinluxFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val tokens = LocalFinluxTokens.current
    val shape = RoundedCornerShape(tokens.radius.smallChip)

    val containerColor = if (isSelected) {
        tokens.primary.copy(alpha = if (tokens.isDark) 0.26f else 0.14f)
    } else {
        tokens.surfaceSoft
    }

    val contentColor = if (isSelected) {
        tokens.primary
    } else {
        tokens.onSurfaceVariant
    }

    val borderColor = if (isSelected) {
        tokens.primary.copy(alpha = 0.85f)
    } else {
        tokens.onSurface.copy(alpha = 0.08f)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .border(BorderStroke(1.dp, borderColor), shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = FinluxTextStyles.Caption,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            )
        }
    }
}

/**
 * Standard Transaction Item Row (FinLux Prism Spec 8.6, 9 & 24)
 * Solves UI-FIX-01 (Semantic Color) and UI-FIX-03 (Over-Glass).
 */
@Composable
fun FinluxTransactionRow(
    transaction: FinanceTransaction,
    category: Category?,
    modifier: Modifier = Modifier,
    wallet: Wallet? = null,
    relatedWallet: Wallet? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
) {
    val tokens = LocalFinluxTokens.current
    val isIncome = transaction.type == TransactionType.INCOME
    val semanticAmountColor = getTransactionSemanticColor(transaction.type)
    val categoryAccent = category?.let { colorFromHex(it.colorHex) } ?: tokens.primary
    val icon = category?.let { categoryIcon(it.icon) } ?: when (transaction.type) {
        TransactionType.INCOME -> Icons.Default.ArrowDownward
        TransactionType.EXPENSE -> Icons.Default.Payments
        TransactionType.TRANSFER_OUT, TransactionType.TRANSFER_IN -> Icons.Default.SwapHoriz
    }

    val title = transaction.note.ifBlank {
        category?.name ?: when (transaction.type) {
            TransactionType.INCOME -> "Thu nhập"
            TransactionType.EXPENSE -> "Chi tiêu"
            TransactionType.TRANSFER_OUT -> if (relatedWallet != null) "Chuyển tiền đến ${relatedWallet.name}" else "Chuyển tiền đi"
            TransactionType.TRANSFER_IN -> if (relatedWallet != null) "Nhận tiền từ ${relatedWallet.name}" else "Nhận tiền chuyển"
        }
    }

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm")
    }
    val dateText = remember(transaction.date) {
        dateFormatter.format(transaction.date.atZone(ZoneId.systemDefault()))
    }

    val walletDisplayName = when (transaction.type) {
        TransactionType.TRANSFER_OUT -> if (relatedWallet != null) "${wallet?.name ?: "Ví"} ➔ ${relatedWallet.name}" else wallet?.name
        TransactionType.TRANSFER_IN -> if (relatedWallet != null) "${relatedWallet.name} ➔ ${wallet?.name ?: "Ví"}" else wallet?.name
        else -> wallet?.name
    }

    val subtitle = if (!walletDisplayName.isNullOrBlank()) {
        "$dateText · $walletDisplayName"
    } else {
        dateText
    }

    FinluxSoftCard(
        modifier = modifier.fillMaxWidth(),
        radius = tokens.radius.standardCard,
        padding = 14.dp,
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Column 1: Category Icon with subtle soft container (Fixed 42dp)
            Surface(
                shape = RoundedCornerShape(tokens.radius.smallChip),
                color = categoryAccent.copy(alpha = if (tokens.isDark) 0.18f else 0.12f),
                modifier = Modifier.size(42.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = categoryAccent,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            // Column 2: Note & Subtitle (weight 1f, padding start 12dp, end 8dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = FinluxTextStyles.CardTitle,
                    color = tokens.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = FinluxTextStyles.Caption,
                    color = tokens.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Column 3: Semantic Amount Text ONLY (wrapContentWidth, End)
            FinluxAmountText(
                amount = transaction.amount.value,
                type = transaction.type,
                explicitColor = semanticAmountColor,
                textStyle = FinluxTextStyles.CardTitle,
            )

            if (onMoreClick != null) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Tùy chọn",
                        tint = tokens.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
