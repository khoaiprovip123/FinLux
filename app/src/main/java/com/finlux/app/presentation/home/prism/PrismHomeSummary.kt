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
internal fun PrismSummaryTrioCard(
    income: Long,
    expense: Long,
    net: Long,
    showBalance: Boolean,
    onIncomeClick: () -> Unit,
    onExpenseClick: () -> Unit,
    onNetClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val preferences = LocalUiPreferences.current
    val pagerState = rememberPagerState(pageCount = { PRISM_SUMMARY_PAGE_COUNT })
    val coroutineScope = rememberCoroutineScope()
    val metrics = listOf(
        PrismSummaryMetricUi(
            type = PrismMetricWatermarkType.INCOME,
            tabTitle = "Thu nhập",
            title = "Thu tháng này",
            subtitle = "Tổng tiền vào trong tháng",
            value = if (showBalance) formatVndAmount(income) else "••••••••",
            trendText = if (income == 0L) "Chưa có thu nhập" else "▲ Dòng tiền vào",
            isTrendPositive = if (income == 0L) null else true,
            accentColor = FinluxColors.IncomeGreen,
            onClick = onIncomeClick,
        ),
        PrismSummaryMetricUi(
            type = PrismMetricWatermarkType.EXPENSE,
            tabTitle = "Chi tiêu",
            title = "Chi tháng này",
            subtitle = "Tổng tiền đã chi trong tháng",
            value = if (showBalance) formatVndAmount(expense) else "••••••••",
            trendText = if (expense == 0L) "Chưa có chi tiêu" else "▼ Dòng tiền ra",
            isTrendPositive = if (expense == 0L) null else false,
            accentColor = FinluxColors.ExpenseRed,
            onClick = onExpenseClick,
        ),
        PrismSummaryMetricUi(
            type = PrismMetricWatermarkType.NET,
            tabTitle = "Dòng tiền",
            title = "Dòng tiền ròng",
            subtitle = "Thu nhập sau khi trừ chi tiêu",
            value = if (showBalance) {
                if (net < 0) "-${formatVndAmount(-net)}" else "+${formatVndAmount(net)}"
            } else {
                "••••••••"
            },
            trendText = when {
                net > 0 -> "▲ Đang dương"
                net < 0 -> "▼ Đang âm"
                else -> "Đang cân bằng"
            },
            isTrendPositive = when {
                net > 0 -> true
                net < 0 -> false
                else -> null
            },
            accentColor = if (net < 0) FinluxColors.ExpenseRed else tokens.primary,
            onClick = onNetClick,
        ),
    )

    LaunchedEffect(
        pagerState.currentPage,
        pagerState.isScrollInProgress,
        preferences.animationsEnabled,
    ) {
        if (!pagerState.isScrollInProgress) {
            delay(PRISM_SUMMARY_AUTO_ADVANCE_MS)
            if (!pagerState.isScrollInProgress) {
                val nextPage = nextPrismSummaryPage(pagerState.currentPage)
                if (preferences.animationsEnabled) {
                    pagerState.animateScrollToPage(
                        page = nextPage,
                        animationSpec = tween(durationMillis = 620),
                    )
                } else {
                    pagerState.scrollToPage(nextPage)
                }
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = tokens.surface,
        border = BorderStroke(1.dp, tokens.border),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Tình hình tháng này",
                style = FinluxTextStyles.SectionTitle.copy(fontSize = 15.sp),
                color = tokens.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = tokens.primary.copy(alpha = if (tokens.isDark) 0.18f else 0.10f),
                border = BorderStroke(0.75.dp, tokens.primary.copy(alpha = 0.22f)),
            ) {
                Text(
                    text = "Tự động · 10s",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = FinluxTextStyles.MicroLabel.copy(fontSize = 9.5.sp),
                    color = tokens.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            metrics.forEachIndexed { index, metric ->
                val selected = pagerState.currentPage == index
                val tabColor by animateColorAsState(
                    targetValue = if (selected) {
                        metric.accentColor.copy(alpha = if (tokens.isDark) 0.22f else 0.12f)
                    } else {
                        tokens.surfaceSoft.copy(alpha = if (tokens.isDark) 0.58f else 0.76f)
                    },
                    animationSpec = tween(200),
                    label = "summary_tab_color_$index",
                )
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            coroutineScope.launch {
                                if (preferences.animationsEnabled) {
                                    pagerState.animateScrollToPage(index, animationSpec = tween(420))
                                } else {
                                    pagerState.scrollToPage(index)
                                }
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = tabColor,
                    border = BorderStroke(
                        width = if (selected) 1.dp else 0.75.dp,
                        color = if (selected) metric.accentColor.copy(alpha = 0.38f) else tokens.border,
                    ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PrismMetricMiniBadge(type = metric.type, accentColor = metric.accentColor)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = metric.tabTitle,
                            style = FinluxTextStyles.MicroLabel.copy(fontSize = 10.sp),
                            color = if (selected) metric.accentColor else tokens.onSurfaceVariant,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                        )
                    }
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp),
            pageSpacing = 10.dp,
        ) { page ->
            val metric = metrics[page]
            PrismTrioMetricCard(
                metric = metric,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            metrics.indices.forEach { index ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.5.dp)
                        .height(4.dp)
                        .width(if (selected) 22.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (selected) metrics[index].accentColor else tokens.border),
                )
            }
        }
        }
    }
}

internal const val PRISM_SUMMARY_PAGE_COUNT = 3
internal const val PRISM_SUMMARY_AUTO_ADVANCE_MS = 10_000L

internal fun nextPrismSummaryPage(currentPage: Int): Int =
    (currentPage + 1).mod(PRISM_SUMMARY_PAGE_COUNT)

internal data class PrismSummaryMetricUi(
    val type: PrismMetricWatermarkType,
    val tabTitle: String,
    val title: String,
    val subtitle: String,
    val value: String,
    val trendText: String,
    val isTrendPositive: Boolean?,
    val accentColor: Color,
    val onClick: () -> Unit,
)

internal enum class PrismMetricWatermarkType {
    INCOME, EXPENSE, NET
}

@Composable
internal fun PrismMetricMiniBadge(
    type: PrismMetricWatermarkType,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = accentColor.copy(alpha = 0.14f),
        border = BorderStroke(0.75.dp, accentColor.copy(alpha = 0.35f)),
        modifier = modifier.size(18.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(10.dp)) {
                val w = size.width
                val h = size.height
                when (type) {
                    PrismMetricWatermarkType.INCOME -> {
                        val p = Path().apply {
                            moveTo(w * 0.18f, h * 0.82f)
                            lineTo(w * 0.82f, h * 0.18f)
                            moveTo(w * 0.38f, h * 0.18f)
                            lineTo(w * 0.82f, h * 0.18f)
                            lineTo(w * 0.82f, h * 0.62f)
                        }
                        drawPath(
                            path = p,
                            color = accentColor,
                            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round),
                        )
                    }
                    PrismMetricWatermarkType.EXPENSE -> {
                        val p = Path().apply {
                            moveTo(w * 0.18f, h * 0.18f)
                            lineTo(w * 0.82f, h * 0.82f)
                            moveTo(w * 0.38f, h * 0.82f)
                            lineTo(w * 0.82f, h * 0.82f)
                            lineTo(w * 0.82f, h * 0.38f)
                        }
                        drawPath(
                            path = p,
                            color = accentColor,
                            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round),
                        )
                    }
                    PrismMetricWatermarkType.NET -> {
                        val p = Path().apply {
                            moveTo(w * 0.50f, 0f)
                            lineTo(w * 0.64f, h * 0.36f)
                            lineTo(w, h * 0.50f)
                            lineTo(w * 0.64f, h * 0.64f)
                            lineTo(w * 0.50f, h)
                            lineTo(w * 0.36f, h * 0.64f)
                            lineTo(0f, h * 0.50f)
                            lineTo(w * 0.36f, h * 0.36f)
                            close()
                        }
                        drawPath(
                            path = p,
                            color = accentColor,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun PrismTrioMetricCard(
    metric: PrismSummaryMetricUi,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val shape = RoundedCornerShape(18.dp)
    Surface(
        modifier = modifier
            .clip(shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = metric.accentColor),
                onClick = metric.onClick,
            ),
        shape = shape,
        color = metric.accentColor.copy(alpha = if (tokens.isDark) 0.12f else 0.065f),
        border = BorderStroke(1.dp, metric.accentColor.copy(alpha = if (tokens.isDark) 0.28f else 0.18f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = metric.title,
                    style = FinluxTextStyles.Caption.copy(fontSize = 12.sp),
                    color = tokens.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = metric.value,
                    style = FinluxTextStyles.CardTitle.copy(
                        fontSize = prismMetricAmountFontSizeSp(metric.value).sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.25).sp,
                    ),
                    color = metric.accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(7.dp),
                        color = when (metric.isTrendPositive) {
                            true -> FinluxColors.IncomeGreen.copy(alpha = if (tokens.isDark) 0.20f else 0.12f)
                            false -> FinluxColors.ExpenseRed.copy(alpha = if (tokens.isDark) 0.20f else 0.12f)
                            null -> tokens.surfaceSoft
                        },
                    ) {
                        Text(
                            text = metric.trendText,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            style = FinluxTextStyles.MicroLabel.copy(fontSize = 9.5.sp),
                            color = when (metric.isTrendPositive) {
                                true -> FinluxColors.IncomeGreen
                                false -> FinluxColors.ExpenseRed
                                null -> tokens.onSurfaceVariant
                            },
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = metric.subtitle,
                        style = FinluxTextStyles.MicroLabel.copy(fontSize = 9.sp),
                        color = tokens.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(metric.accentColor.copy(alpha = if (tokens.isDark) 0.20f else 0.12f))
                    .border(
                        BorderStroke(1.dp, metric.accentColor.copy(alpha = 0.26f)),
                        RoundedCornerShape(17.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = when (metric.type) {
                        PrismMetricWatermarkType.INCOME -> Icons.Default.ArrowDownward
                        PrismMetricWatermarkType.EXPENSE -> Icons.Default.ArrowUpward
                        PrismMetricWatermarkType.NET -> Icons.Default.SwapHoriz
                    },
                    contentDescription = metric.title,
                    tint = metric.accentColor,
                    modifier = Modifier.size(27.dp),
                )
            }
        }
    }
}

internal fun prismMetricAmountFontSizeSp(value: String): Float = when {
    value.length >= 18 -> 18.0f
    value.length >= 16 -> 20.0f
    value.length >= 14 -> 22.0f
    value.length >= 12 -> 24.0f
    else -> 27.0f
}

/**
 * 4. Quick Action 5-Tile Row (Ví của tôi, Ngân sách, Danh mục, Mục tiêu, Xem thêm)
 */
