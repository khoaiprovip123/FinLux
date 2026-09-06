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

internal data class PrismBudgetShareUi(
    val category: Category,
    val spent: Long,
    val limit: Long,
    val percent: Int,
)

/**
 * Specialized Page Content for Budget Breakdown in Carousel
 */
@Composable
internal fun PrismBudgetBreakdownPageContent(
    budgetShares: List<PrismBudgetShareUi>,
    totalSpentPercent: Int,
    showBalance: Boolean,
    onNavigateToBudget: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    if (budgetShares.isEmpty()) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(0.40f)
                    .height(130.dp),
                contentAlignment = Alignment.Center,
            ) {
                PrismDonutChart(
                    percentages = listOf(100),
                    colors = listOf(tokens.border.copy(alpha = 0.45f)),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "0%",
                        style = FinluxTextStyles.CardTitle.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                        ),
                        color = tokens.onSurfaceVariant,
                    )
                    Text(
                        text = "Chưa có hạn mức",
                        style = FinluxTextStyles.MicroLabel.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = tokens.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(0.60f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Chưa đặt ngân sách",
                    style = FinluxTextStyles.CardTitle.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                    color = tokens.onSurface,
                )
                Text(
                    text = "Thiết lập hạn mức chi tiêu để kiểm soát chi tiêu tối ưu.",
                    style = FinluxTextStyles.Caption.copy(fontSize = 11.sp),
                    color = tokens.onSurfaceVariant,
                    maxLines = 2,
                )
                Surface(
                    onClick = onNavigateToBudget,
                    shape = RoundedCornerShape(10.dp),
                    color = tokens.primary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, tokens.primary.copy(alpha = 0.35f)),
                    modifier = Modifier.padding(top = 2.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = tokens.primary,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "Thiết lập ngay ›",
                            style = FinluxTextStyles.Caption.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = tokens.primary,
                        )
                    }
                }
            }
        }
    } else {
        val colors = budgetShares.map { item ->
            when {
                item.percent >= 100 -> FinluxPalette.CFFEF4444
                item.percent >= 80 -> FinluxPalette.CFFF59E0B
                else -> FinluxPalette.CFF10B981
            }
        }

        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left: Donut Chart
            Box(
                modifier = Modifier
                    .weight(0.40f)
                    .height(130.dp),
                contentAlignment = Alignment.Center,
            ) {
                PrismDonutChart(
                    percentages = budgetShares.map { it.percent },
                    colors = colors,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (showBalance) "$totalSpentPercent%" else "••••",
                        style = FinluxTextStyles.CardTitle.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                        ),
                        color = when {
                            totalSpentPercent >= 100 -> FinluxPalette.CFFEF4444
                            totalSpentPercent >= 80 -> FinluxPalette.CFFF59E0B
                            else -> tokens.onSurface
                        },
                    )
                    Text(
                        text = "Đã chi tiêu",
                        style = FinluxTextStyles.MicroLabel.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = tokens.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            // Right: Budget legend list
            Column(
                modifier = Modifier.weight(0.60f),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                budgetShares.take(3).forEachIndexed { index, item ->
                    val color = colors.getOrElse(index) { FinluxPalette.CFF6366F1 }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            modifier = Modifier.size(26.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = color.copy(alpha = 0.14f),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = categoryIcon(item.category.icon),
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(15.dp),
                                )
                            }
                        }

                        Spacer(Modifier.width(7.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(1.dp),
                        ) {
                            Text(
                                text = item.category.name,
                                style = FinluxTextStyles.Caption.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = tokens.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            val spentText = if (showBalance) formatVndAmount(item.spent, isCompact = true) else "••••"
                            val limitText = if (showBalance) formatVndAmount(item.limit, isCompact = true) else "••••"
                            Text(
                                text = "$spentText / $limitText",
                                style = FinluxTextStyles.MicroLabel.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                                color = tokens.onSurface.copy(alpha = if (tokens.isDark) 0.76f else 0.70f),
                                maxLines = 1,
                            )
                        }

                        Spacer(Modifier.width(6.dp))

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = color.copy(alpha = if (tokens.isDark) 0.22f else 0.12f),
                        ) {
                            Text(
                                text = "${item.percent}%",
                                style = FinluxTextStyles.MicroLabel.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = color,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reusable Page Content for Carousel Breakdown
 */
@Composable
internal fun PrismBreakdownPageContent(
    shares: List<Triple<Category, Long, Int>>,
    colors: List<Color>,
    centerAmount: String,
    centerLabel: String,
    showBalance: Boolean,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left: Donut Chart
        Box(
            modifier = Modifier
                .weight(0.40f)
                .height(130.dp),
            contentAlignment = Alignment.Center,
        ) {
            PrismDonutChart(
                percentages = shares.map { it.third },
                colors = colors,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (showBalance) centerAmount else "••••",
                    style = FinluxTextStyles.CardTitle.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                    ),
                    color = tokens.onSurface,
                )
                Text(
                    text = centerLabel,
                    style = FinluxTextStyles.MicroLabel.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = tokens.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        // Right: Category legend list
        Column(
            modifier = Modifier.weight(0.60f),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            shares.forEachIndexed { index, (cat, sum, percent) ->
                val color = colors.getOrElse(index) { FinluxPalette.CFF6366F1 }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Category icon
                    Surface(
                        modifier = Modifier.size(26.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = color.copy(alpha = 0.14f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = categoryIcon(cat.icon),
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(15.dp),
                            )
                        }
                    }

                    Spacer(Modifier.width(7.dp))

                    // Name and amount use separate lines so long Vietnamese labels never
                    // compete with the financial value or percentage badge.
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                    ) {
                        Text(
                            text = cat.name,
                            style = FinluxTextStyles.Caption.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = tokens.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = if (showBalance) formatVndAmount(sum, isCompact = true) else "••••",
                            style = FinluxTextStyles.MicroLabel.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                            color = tokens.onSurface.copy(alpha = if (tokens.isDark) 0.76f else 0.70f),
                            maxLines = 1,
                        )
                    }

                    Spacer(Modifier.width(6.dp))

                    // Percentage badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = color.copy(alpha = if (tokens.isDark) 0.18f else 0.10f),
                    ) {
                        Text(
                            text = "$percent%",
                            style = FinluxTextStyles.MicroLabel.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = tokens.onSurface,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Donut Chart Canvas Composable
 */
@Composable
internal fun PrismDonutChart(
    percentages: List<Int>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(120.dp)) {
        val strokeWidth = 14.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2f
        val topLeft = Offset((size.width - radius * 2) / 2f, (size.height - radius * 2) / 2f)
        val arcSize = Size(radius * 2, radius * 2)

        var startAngle = -90f
        val sumPercents = percentages.sum().coerceAtLeast(1)

        percentages.forEachIndexed { index, pct ->
            val sweep = if (pct > 0) (pct.toFloat() / sumPercents.toFloat()) * 360f else 0f
            if (sweep > 0f) {
                drawArc(
                    color = colors.getOrElse(index) { Color.Gray },
                    startAngle = startAngle,
                    sweepAngle = sweep - 2f, // 2 degree gap for clean visual
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
                startAngle += sweep
            }
        }

        // If all 0%, draw a soft default ring
        if (percentages.all { it == 0 }) {
            drawArc(
                color = FinluxPalette.CFF6366F1.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }
    }
}

/**
 * 7. Recent Transaction Item
 * - Tap → xem chi tiết
 * - Long press → hiển thị pop-up Sửa / Xóa
 */
