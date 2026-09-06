package com.finlux.app.presentation.home.prism

import com.finlux.app.core.designsystem.theme.FinluxPalette

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.finlux.app.core.designsystem.component.FinluxLazyColumn
import com.finlux.app.core.designsystem.component.FinluxListType
import com.finlux.app.core.designsystem.component.FinluxScreenScaffold
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.FinluxUserAvatar
import com.finlux.app.core.designsystem.LocalUiPreferences
import com.finlux.app.core.designsystem.NotificationPermissionHandler
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.component.FinluxTransactionGroup
import com.finlux.app.core.designsystem.component.FinluxSoftCard
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.navigation.Route
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.presentation.components.MainBottomBar
import com.finlux.app.presentation.home.HomeViewModel
import com.finlux.app.presentation.savingspin.SavingSpinAction
import com.finlux.app.presentation.savingspin.SavingSpinUiState
import com.finlux.app.presentation.savingspin.components.SavingSpinHomeCard
import com.finlux.app.presentation.transaction.TransactionActionDialog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun PrismRecentTransactionItem(
    transaction: FinanceTransaction,
    category: Category?,
    wallet: Wallet?,
    relatedWallet: Wallet? = null,
    showBalance: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val isTransfer = transaction.type == TransactionType.TRANSFER_OUT || transaction.type == TransactionType.TRANSFER_IN
    val isIncome = transaction.type == TransactionType.INCOME

    val accentColor = when (transaction.type) {
        TransactionType.INCOME -> category?.let { colorFromHex(it.colorHex) } ?: FinluxColors.IncomeGreen
        TransactionType.EXPENSE -> category?.let { colorFromHex(it.colorHex) } ?: tokens.primary
        TransactionType.TRANSFER_OUT, TransactionType.TRANSFER_IN -> FinluxColors.TransferBlue
    }

    val icon = when (transaction.type) {
        TransactionType.INCOME -> category?.let { categoryIcon(it.icon) } ?: Icons.Default.ArrowDownward
        TransactionType.EXPENSE -> category?.let { categoryIcon(it.icon) } ?: Icons.Default.LocalOffer
        TransactionType.TRANSFER_OUT, TransactionType.TRANSFER_IN -> Icons.Default.SwapHoriz
    }

    val title = transaction.note.ifBlank {
        when (transaction.type) {
            TransactionType.INCOME -> category?.name ?: "Thu nhập"
            TransactionType.EXPENSE -> category?.name ?: "Chi tiêu"
            TransactionType.TRANSFER_OUT -> if (relatedWallet != null) "Chuyển tiền đến ${relatedWallet.name}" else "Chuyển tiền đi"
            TransactionType.TRANSFER_IN -> if (relatedWallet != null) "Nhận tiền từ ${relatedWallet.name}" else "Nhận tiền chuyển"
        }
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm") }
    val dateText = remember(transaction.date) {
        dateFormatter.format(transaction.date.atZone(ZoneId.systemDefault()))
    }

    val walletDisplayName = when (transaction.type) {
        TransactionType.TRANSFER_OUT -> if (relatedWallet != null) "${wallet?.name ?: "Ví"} ➔ ${relatedWallet.name}" else wallet?.name ?: "Ví chính"
        TransactionType.TRANSFER_IN -> if (relatedWallet != null) "${relatedWallet.name} ➔ ${wallet?.name ?: "Ví"}" else wallet?.name ?: "Ví chính"
        else -> wallet?.name ?: "Ví chính"
    }
    val subtitleText = "$dateText · $walletDisplayName"

    FinluxSoftCard(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        radius = 18.dp,
        padding = 14.dp,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Column 1: Category / Transfer Icon (Fixed 42dp)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = accentColor.copy(alpha = if (tokens.isDark) 0.18f else 0.12f),
                modifier = Modifier.size(42.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            // Column 2: Note + Subtitle (weight 1f, padding start 12dp, end 8dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = FinluxTextStyles.CardTitle.copy(fontSize = 15.sp),
                    color = tokens.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitleText,
                    style = FinluxTextStyles.Caption.copy(fontSize = 12.sp),
                    color = tokens.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Column 3: Amount ONLY (wrapContentWidth, End)
            val amountFormatted = when (transaction.type) {
                TransactionType.INCOME -> "+${formatVndAmount(transaction.amount.value)}"
                TransactionType.EXPENSE -> "-${formatVndAmount(transaction.amount.value)}"
                TransactionType.TRANSFER_OUT -> "-${formatVndAmount(transaction.amount.value)}"
                TransactionType.TRANSFER_IN -> "+${formatVndAmount(transaction.amount.value)}"
            }
            val amountColor = when (transaction.type) {
                TransactionType.INCOME -> FinluxColors.IncomeGreen
                TransactionType.EXPENSE -> FinluxColors.ExpenseRed
                TransactionType.TRANSFER_OUT, TransactionType.TRANSFER_IN -> FinluxColors.TransferBlue
            }

            Text(
                text = if (showBalance) amountFormatted else "••••",
                style = FinluxTextStyles.CardTitle.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = amountColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                maxLines = 1,
            )
        }
    }
}

/**
 * Thẻ giao dịch đồng bộ chuẩn 3 cột với màn hình Giao dịch (Transaction Explorer)
 */
@Composable
internal fun PrismHomeExplorerTransactionCard(
    transaction: FinanceTransaction,
    category: Category?,
    wallet: Wallet?,
    showBalance: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val zone = remember { java.time.ZoneId.systemDefault() }

    val isIncome = transaction.type == TransactionType.INCOME
    val isTransfer = transaction.type == TransactionType.TRANSFER_OUT || transaction.type == TransactionType.TRANSFER_IN

    // Amount Sign & Color
    val amountPrefix = if (isIncome) "+" else "−"
    val amountColor = when {
        isIncome -> FinluxPalette.CFF059669 // Deep Emerald Green
        isTransfer -> FinluxPalette.CFF2563EB // Blue
        else -> FinluxPalette.CFFE11D48 // Vibrant Crimson Red
    }

    val displayAmount = if (showBalance) {
        amountPrefix + formatVndAmount(transaction.amount.value).replace("đ", "₫")
    } else {
        "••••••••"
    }

    // Title & Subtitle text
    val mainTitle = transaction.note.ifBlank {
        category?.name ?: if (isTransfer) "Chuyển tiền" else "Giao dịch"
    }

    val subTitle = when {
        isTransfer -> "Chuyển tiền"
        transaction.note.isNotBlank() && category != null -> category.name
        else -> if (isIncome) "Thu nhập" else "Chi tiêu"
    }

    // Time text (HH:mm)
    val timeFormatter = remember { java.time.format.DateTimeFormatter.ofPattern("HH:mm") }
    val timeText = remember(transaction.date, zone) {
        transaction.date.atZone(zone).format(timeFormatter)
    }

    // Icon Container Styling
    val iconColorHex = category?.colorHex
    val parsedColor = remember(iconColorHex) {
        if (!iconColorHex.isNullOrBlank()) colorFromHex(iconColorHex) else null
    }

    val iconBackgroundBrush = remember(transaction.type, parsedColor) {
        when {
            isIncome -> Brush.linearGradient(listOf(FinluxPalette.CFF10B981, FinluxPalette.CFF059669))
            isTransfer -> Brush.linearGradient(listOf(FinluxPalette.CFF3B82F6, FinluxPalette.CFF6366F1))
            parsedColor != null -> Brush.linearGradient(listOf(parsedColor, parsedColor.copy(alpha = 0.85f)))
            else -> Brush.linearGradient(listOf(FinluxPalette.CFFEF4444, FinluxPalette.CFFDC2626))
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (tokens.isDark) FinluxPalette.CFF1E1E34.copy(alpha = 0.75f) else FinluxPalette.White,
        border = BorderStroke(
            1.dp,
            if (tokens.isDark) tokens.border else FinluxPalette.CFFE2E8F0.copy(alpha = 0.7f),
        ),
        shadowElevation = if (tokens.isDark) 0.dp else 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // 1. CỘT TRÁI: Container Icon 50dp
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(iconBackgroundBrush),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isTransfer) Icons.Default.SwapHoriz else categoryIcon(category?.icon.orEmpty()),
                    contentDescription = category?.name,
                    tint = FinluxPalette.White,
                    modifier = Modifier.size(24.dp),
                )
            }

            // 2. CỘT GIỮA: Tên giao dịch + Danh mục
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = mainTitle,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = tokens.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = subTitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    color = tokens.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // 3. CỘT PHẢI: Số tiền To & Rõ nét + Giờ (Canh phải tuyệt đối)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = displayAmount,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.4).sp,
                    ),
                    color = amountColor,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                )

                Text(
                    text = timeText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    color = FinluxPalette.CFF94A3B8,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}
