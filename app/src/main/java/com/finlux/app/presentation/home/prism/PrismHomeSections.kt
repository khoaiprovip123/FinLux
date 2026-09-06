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
internal fun PrismHomeTopHeader(
    displayName: String,
    photoUrl: String?,
    unreadCount: Int,
    onProfile: () -> Unit,
    onNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 6.dp, bottom = 8.dp)
            .height(PRISM_HOME_HEADER_HEIGHT_DP.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FinluxUserAvatar(
            photoUrl = photoUrl,
            displayName = displayName,
            size = 44.dp,
            editable = false,
            onClick = onProfile,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 11.dp, end = 10.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onProfile,
                ),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(
                text = "Xin chào 👋",
                style = FinluxTextStyles.Caption.copy(fontSize = 11.sp),
                color = tokens.onSurfaceVariant,
            )
            Text(
                text = displayName,
                style = FinluxTextStyles.ScreenTitle.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = tokens.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(tokens.surfaceSoft)
                .border(BorderStroke(1.dp, tokens.border), RoundedCornerShape(14.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true),
                    onClick = onNotifications,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = "Thông báo",
                tint = tokens.onSurface,
                modifier = Modifier.size(21.dp),
            )
            if (unreadCount > 0) {
                Surface(
                    shape = CircleShape,
                    color = FinluxColors.ExpenseRed,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 3.dp, end = 3.dp)
                        .size(if (unreadCount > 9) 18.dp else 16.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                            style = FinluxTextStyles.Caption.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                            color = tokens.onHero,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

internal const val PRISM_HOME_HEADER_HEIGHT_DP = 52
internal const val PRISM_FINANCIAL_HERO_HEIGHT_DP = 180

internal const val PRISM_FINANCIAL_OVERVIEW_PAGE_COUNT = 4

internal fun nextPrismOverviewPage(currentPage: Int): Int =
    (currentPage + 1).mod(PRISM_FINANCIAL_OVERVIEW_PAGE_COUNT)

internal enum class PrismCardTheme {
    WALLET, INCOME, EXPENSE, CASH_FLOW
}

internal data class PrismOverviewPageUi(
    val title: String,
    val periodLabel: String,
    val value: String,
    val subtitle: String,
    val contextInfo: String,
    val chartValues: List<Long>,
    val theme: PrismCardTheme,
    val backgroundColors: List<Color>,
    val onClick: () -> Unit,
)

/**
 * Unified FinLux Prism overview with four pages: balance, income, expense and net cash flow.
 * Professional Data-First banking layout with real mini-bar charts, clear period scopes, and named indicators.
 */
@Composable
internal fun PrismFinancialOverviewCard(
    netWorth: Long,
    grossAssets: Long,
    totalDebt: Long,
    income: Long,
    expense: Long,
    net: Long,
    monthTransactions: List<FinanceTransaction>,
    wallets: List<Wallet>,
    salaryCycleLabel: String?,
    showBalance: Boolean,
    onToggleShowBalance: () -> Unit,
    onDebtsClick: () -> Unit,
    onWalletsClick: () -> Unit,
    onIncomeClick: () -> Unit,
    onExpenseClick: () -> Unit,
    onNetClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { PRISM_FINANCIAL_OVERVIEW_PAGE_COUNT })
    val hiddenAmount = "••••••••"

    val periodDateRange = salaryCycleLabel ?: remember {
        val now = LocalDate.now()
        val firstDay = now.withDayOfMonth(1)
        val lastDay = now.withDayOfMonth(now.lengthOfMonth())
        val fmt = DateTimeFormatter.ofPattern("dd/MM")
        "${firstDay.format(fmt)} – ${lastDay.format(fmt)}"
    }

    val incomeTransactions = remember(monthTransactions) {
        monthTransactions.filter { it.type == TransactionType.INCOME }
    }
    val expenseTransactions = remember(monthTransactions) {
        monthTransactions.filter { it.type == TransactionType.EXPENSE }
    }
    val incomeCount = incomeTransactions.size
    val expenseCount = expenseTransactions.size

    val incomeBars = remember(monthTransactions) {
        computePeriodBars(monthTransactions, TransactionType.INCOME)
    }
    val expenseBars = remember(monthTransactions) {
        computePeriodBars(monthTransactions, TransactionType.EXPENSE)
    }
    val netBars = remember(monthTransactions) {
        computePeriodBars(monthTransactions, null)
    }
    val walletBars = remember(wallets) {
        val nonZero = wallets.map { it.balance.value }
        if (nonZero.isEmpty()) List(5) { 0L } else nonZero.take(5)
    }

    val pages = listOf(
        PrismOverviewPageUi(
            title = "Số dư hiện có",
            periodLabel = if (wallets.isNotEmpty()) "${wallets.size} ví hoạt động" else "Tất cả ví",
            value = if (showBalance) formatVndAmount(grossAssets).replace("đ", "₫") else hiddenAmount,
            subtitle = "Tài sản ròng: ${if (showBalance) formatVndAmount(netWorth).replace("đ", "₫") else "••••"}",
            contextInfo = if (totalDebt > 0L) {
                if (showBalance) "Nợ: ${formatVndAmount(totalDebt).replace("đ", "₫")}" else "Nợ: ••••"
            } else {
                "Không có dư nợ"
            },
            chartValues = walletBars,
            theme = PrismCardTheme.WALLET,
            backgroundColors = listOf(
                FinluxPalette.CFF0A192F,
                FinluxPalette.CFF1E3A8A,
                FinluxPalette.CFF1D4ED8,
                FinluxPalette.CFF2563EB,
            ),
            onClick = onWalletsClick,
        ),
        PrismOverviewPageUi(
            title = "Thu kỳ này",
            periodLabel = periodDateRange,
            value = if (showBalance) formatVndAmount(income).replace("đ", "₫") else hiddenAmount,
            subtitle = if (incomeCount > 0) "$incomeCount khoản thu" else "Chưa có khoản thu",
            contextInfo = if (incomeCount > 0) {
                if (showBalance) "TB ${formatVndAmount(income / incomeCount).replace("đ", "₫")}/khoản" else "TB ••••/khoản"
            } else {
                "Chưa phát sinh"
            },
            chartValues = incomeBars,
            theme = PrismCardTheme.INCOME,
            backgroundColors = listOf(
                FinluxPalette.CFF04382B,
                FinluxPalette.CFF065F46,
                FinluxPalette.CFF047857,
                FinluxPalette.CFF0D9488,
            ),
            onClick = onIncomeClick,
        ),
        PrismOverviewPageUi(
            title = "Chi kỳ này",
            periodLabel = periodDateRange,
            value = if (showBalance) formatVndAmount(expense).replace("đ", "₫") else hiddenAmount,
            subtitle = if (expenseCount > 0) "$expenseCount khoản chi" else "Chưa có khoản chi",
            contextInfo = if (expenseCount > 0) {
                if (showBalance) "TB ${formatVndAmount(expense / expenseCount).replace("đ", "₫")}/khoản" else "TB ••••/khoản"
            } else {
                "Chưa phát sinh"
            },
            chartValues = expenseBars,
            theme = PrismCardTheme.EXPENSE,
            backgroundColors = listOf(
                FinluxPalette.CFF6B0E27,
                FinluxPalette.CFF881337,
                FinluxPalette.CFF9F1239,
                FinluxPalette.CFFBE123C,
            ),
            onClick = onExpenseClick,
        ),
        PrismOverviewPageUi(
            title = "Dòng tiền kỳ này",
            periodLabel = periodDateRange,
            value = if (showBalance) {
                if (net < 0L) "-${formatVndAmount(-net).replace("đ", "₫")}" else "+${formatVndAmount(net).replace("đ", "₫")}"
            } else {
                hiddenAmount
            },
            subtitle = "${incomeCount + expenseCount} giao dịch trong kỳ",
            contextInfo = if (showBalance) {
                if (net > 0L) "Thu vượt chi ${formatVndAmount(net).replace("đ", "₫")}"
                else if (net < 0L) "Chi vượt thu ${formatVndAmount(-net).replace("đ", "₫")}"
                else "Thu chi cân bằng"
            } else {
                "Dòng tiền trong kỳ"
            },
            chartValues = netBars,
            theme = PrismCardTheme.CASH_FLOW,
            backgroundColors = listOf(
                FinluxPalette.CFF19163F,
                FinluxPalette.CFF2E236C,
                FinluxPalette.CFF3730A3,
                FinluxPalette.CFF4338CA,
            ),
            onClick = onNetClick,
        ),
    )

    HorizontalPager(
        state = pagerState,
        modifier = modifier
            .fillMaxWidth()
            .height(PRISM_FINANCIAL_HERO_HEIGHT_DP.dp),
    ) { pageIndex ->
        val page = pages[pageIndex]

        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = page.backgroundColors.first().copy(alpha = if (tokens.isDark) 0.40f else 0.25f),
                    ambientColor = FinluxPalette.Black.copy(alpha = if (tokens.isDark) 0.25f else 0.10f),
                )
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = page.backgroundColors,
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    ),
                )
                .border(
                    BorderStroke(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                tokens.onHero.copy(alpha = 0.28f),
                                tokens.onHero.copy(alpha = 0.08f),
                                tokens.onHero.copy(alpha = 0.20f),
                            ),
                        ),
                    ),
                    RoundedCornerShape(24.dp),
                ),
        ) {
            // Distinctive bank-grade security watermark pattern per card theme
            PrismCardBackdropTexture(
                theme = page.theme,
                tintColor = tokens.onHero,
                modifier = Modifier.matchParentSize(),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 26.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // 1. Top row: Title + Scope/Date Range
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = page.title,
                            style = FinluxTextStyles.Caption.copy(fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold),
                            color = tokens.onHeroMuted,
                        )
                        if (pageIndex == 0) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = if (showBalance) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Ẩn/Hiện số dư",
                                tint = tokens.onHero,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable(onClick = onToggleShowBalance),
                            )
                        }
                    }
                    Text(
                        text = page.periodLabel,
                        style = FinluxTextStyles.Caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                        color = tokens.onHeroMuted,
                    )
                }

                // 2. Middle row: Amount + Value Subtitle on Left, Mini Bar Chart on Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = page.value,
                            style = FinluxTextStyles.DisplayAmount.copy(
                                fontFamily = FontFamily.Default,
                                fontSize = prismOverviewAmountFontSizeSp(page.value).sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.3).sp,
                            ),
                            color = tokens.onHero,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = page.subtitle,
                            style = FinluxTextStyles.Caption.copy(fontSize = 12.5.sp, fontWeight = FontWeight.Medium),
                            color = tokens.onHeroMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    // Real data mini bar chart
                    PrismMiniBarChart(
                        values = page.chartValues,
                        barColor = tokens.onHero,
                    )
                }

                // 3. Bottom row: Context info on Left, "Xem chi tiết ›" on Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = page.contextInfo,
                        style = FinluxTextStyles.Caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                        color = tokens.onHeroMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = page.onClick)
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "Xem chi tiết",
                            style = FinluxTextStyles.Caption.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                            color = tokens.onHero,
                        )
                        Spacer(Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = tokens.onHero,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }

            // Morphing Named Capsule Indicator
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val titles = listOf("Ví", "Thu", "Chi", "Dòng tiền")
                titles.forEachIndexed { index, title ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) tokens.onHero.copy(alpha = 0.28f)
                                else tokens.onHero.copy(alpha = 0.10f)
                            )
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                            .padding(horizontal = if (isSelected) 8.dp else 4.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSelected) {
                            Text(
                                text = title,
                                style = FinluxTextStyles.Caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = tokens.onHero,
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(tokens.onHero.copy(alpha = 0.45f))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun PrismMiniBarChart(
    values: List<Long>,
    barColor: Color,
    modifier: Modifier = Modifier,
) {
    val barCount = 5
    val paddedValues = if (values.size >= barCount) values.take(barCount) else values + List(barCount - values.size) { 0L }
    val maxVal = paddedValues.maxOfOrNull { kotlin.math.abs(it) }?.coerceAtLeast(1L) ?: 1L
    val hasData = paddedValues.any { it != 0L }

    Row(
        modifier = modifier
            .width(56.dp)
            .height(40.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        paddedValues.forEach { rawVal ->
            val absVal = kotlin.math.abs(rawVal)
            val heightFraction = if (hasData && absVal > 0L) {
                (absVal.toFloat() / maxVal).coerceIn(0.18f, 1.0f)
            } else {
                0.08f
            }
            val isMax = hasData && absVal == maxVal && absVal > 0L

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(heightFraction)
                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp, bottomStart = 1.dp, bottomEnd = 1.dp))
                    .background(
                        if (isMax) barColor.copy(alpha = 0.95f)
                        else if (absVal > 0L) barColor.copy(alpha = 0.45f)
                        else barColor.copy(alpha = 0.14f)
                    ),
            )
        }
    }
}

internal fun computePeriodBars(
    transactions: List<FinanceTransaction>,
    type: TransactionType?,
    barCount: Int = 5,
): List<Long> {
    if (transactions.isEmpty()) return List(barCount) { 0L }
    val filtered = if (type == null) transactions else transactions.filter { it.type == type }
    if (filtered.isEmpty()) return List(barCount) { 0L }

    val sorted = filtered.sortedBy { it.date }
    val minEpoch = sorted.first().date.toEpochMilli()
    val maxEpoch = sorted.last().date.toEpochMilli()
    val timeSpan = (maxEpoch - minEpoch).coerceAtLeast(1L)

    val buckets = LongArray(barCount)
    for (tx in sorted) {
        val fraction = ((tx.date.toEpochMilli() - minEpoch).toFloat() / timeSpan).coerceIn(0f, 0.999f)
        val bucketIndex = (fraction * barCount).toInt().coerceIn(0, barCount - 1)
        val amount = if (type == null) {
            if (tx.type == TransactionType.INCOME) tx.amount.value else -tx.amount.value
        } else {
            tx.amount.value
        }
        buckets[bucketIndex] += amount
    }
    return buckets.toList()
}

internal fun prismOverviewAmountFontSizeSp(value: String): Float = when {
    value.length >= 17 -> 28.0f
    value.length >= 15 -> 32.0f
    value.length >= 13 -> 35.0f
    else -> 38.0f
}

@Composable
internal fun PrismCardBackdropTexture(
    theme: PrismCardTheme,
    tintColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Common soft ambient bloom & specular rim
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    tintColor.copy(alpha = 0.14f),
                    tintColor.copy(alpha = 0.03f),
                    FinluxPalette.Transparent,
                ),
                center = Offset(w * 0.85f, h * 0.35f),
                radius = w * 0.45f,
            ),
            center = Offset(w * 0.85f, h * 0.35f),
            radius = w * 0.45f,
        )

        // Top edge specular accent line
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    FinluxPalette.Transparent,
                    tintColor.copy(alpha = 0.35f),
                    tintColor.copy(alpha = 0.10f),
                    FinluxPalette.Transparent,
                ),
                startX = w * 0.15f,
                endX = w * 0.85f,
            ),
            start = Offset(w * 0.15f, 1.dp.toPx()),
            end = Offset(w * 0.85f, 1.dp.toPx()),
            strokeWidth = 1.dp.toPx(),
        )

        // 2. Distinctive bank-grade security watermark patterns per card theme
        when (theme) {
            PrismCardTheme.WALLET -> {
                // --- THẺ VÍ: HỌA TIẾT KHO BẢO MẬT & VÒNG ĐỒNG TÂM KỸ THUẬT SỐ ---
                val vaultCenter = Offset(w * 0.88f, h * 0.50f)

                // Concentric dashed security rings
                drawCircle(
                    color = tintColor.copy(alpha = 0.10f),
                    radius = 48.dp.toPx(),
                    center = vaultCenter,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f),
                    ),
                )
                drawCircle(
                    color = tintColor.copy(alpha = 0.07f),
                    radius = 72.dp.toPx(),
                    center = vaultCenter,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
                    ),
                )
                drawCircle(
                    color = tintColor.copy(alpha = 0.05f),
                    radius = 96.dp.toPx(),
                    center = vaultCenter,
                    style = Stroke(width = 0.8.dp.toPx()),
                )

                // Geometric security lattice lines across bottom
                val latticePath = Path().apply {
                    moveTo(w * 0.40f, h)
                    cubicTo(w * 0.60f, h * 0.70f, w * 0.80f, h * 0.95f, w, h * 0.65f)
                }
                drawPath(
                    path = latticePath,
                    color = tintColor.copy(alpha = 0.12f),
                    style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round),
                )
                val latticePath2 = Path().apply {
                    moveTo(w * 0.50f, h)
                    cubicTo(w * 0.70f, h * 0.78f, w * 0.85f, h * 0.98f, w, h * 0.80f)
                }
                drawPath(
                    path = latticePath2,
                    color = tintColor.copy(alpha = 0.08f),
                    style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round),
                )

                // Subtle security dots
                drawCircle(color = tintColor.copy(alpha = 0.18f), radius = 2.dp.toPx(), center = Offset(w * 0.72f, h * 0.28f))
                drawCircle(color = tintColor.copy(alpha = 0.12f), radius = 1.5.dp.toPx(), center = Offset(w * 0.60f, h * 0.80f))
            }

            PrismCardTheme.INCOME -> {
                // --- THẺ THU: HỌA TIẾT CỰC QUANG TĂNG TRƯỞNG & DẢI SÓNG THỊNH VƯỢNG ---
                val auroraPath1 = Path().apply {
                    moveTo(w * 0.35f, h * 0.95f)
                    cubicTo(w * 0.55f, h * 0.75f, w * 0.75f, h * 0.45f, w * 0.98f, h * 0.20f)
                }
                drawPath(
                    path = auroraPath1,
                    brush = Brush.linearGradient(
                        colors = listOf(tintColor.copy(alpha = 0.05f), tintColor.copy(alpha = 0.22f)),
                    ),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                )

                val auroraPath2 = Path().apply {
                    moveTo(w * 0.45f, h)
                    cubicTo(w * 0.65f, h * 0.82f, w * 0.82f, h * 0.55f, w, h * 0.32f)
                }
                drawPath(
                    path = auroraPath2,
                    color = tintColor.copy(alpha = 0.10f),
                    style = Stroke(
                        width = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f),
                        cap = StrokeCap.Round,
                    ),
                )

                // Subtle ascending chevron markers in top-right
                for (i in 0..2) {
                    val cx = w * 0.86f + i * 14.dp.toPx()
                    val cy = h * 0.22f - i * 6.dp.toPx()
                    val chevron = Path().apply {
                        moveTo(cx - 5.dp.toPx(), cy + 4.dp.toPx())
                        lineTo(cx, cy)
                        lineTo(cx + 5.dp.toPx(), cy + 4.dp.toPx())
                    }
                    drawPath(
                        path = chevron,
                        color = tintColor.copy(alpha = 0.12f + i * 0.04f),
                        style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round),
                    )
                }

                // Prosperity sparkle accents
                drawCircle(color = tintColor.copy(alpha = 0.25f), radius = 2.dp.toPx(), center = Offset(w * 0.94f, h * 0.18f))
                drawCircle(color = tintColor.copy(alpha = 0.15f), radius = 1.5.dp.toPx(), center = Offset(w * 0.68f, h * 0.32f))
            }

            PrismCardTheme.EXPENSE -> {
                // --- THẺ CHI: HỌA TIẾT CUNG ĐO NGÂN SÁCH & QUỸ ĐẠO CHUẨN XÁC ---
                val radarCenter = Offset(w * 0.88f, h * 0.48f)

                // Radar budget arc with graduation marks
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            FinluxPalette.Transparent,
                            tintColor.copy(alpha = 0.08f),
                            tintColor.copy(alpha = 0.22f),
                        ),
                        center = radarCenter,
                    ),
                    startAngle = 135f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(radarCenter.x - 56.dp.toPx(), radarCenter.y - 56.dp.toPx()),
                    size = Size(112.dp.toPx(), 112.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                )

                drawArc(
                    color = tintColor.copy(alpha = 0.09f),
                    startAngle = 110f,
                    sweepAngle = 210f,
                    useCenter = false,
                    topLeft = Offset(radarCenter.x - 76.dp.toPx(), radarCenter.y - 76.dp.toPx()),
                    size = Size(152.dp.toPx(), 152.dp.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f),
                    ),
                )

                // Precision calibration ticks
                for (deg in listOf(150, 180, 210, 240, 270)) {
                    val rad = Math.toRadians(deg.toDouble())
                    val r1 = 70.dp.toPx()
                    val r2 = 75.dp.toPx()
                    drawLine(
                        color = tintColor.copy(alpha = 0.15f),
                        start = Offset(radarCenter.x + (r1 * Math.cos(rad)).toFloat(), radarCenter.y + (r1 * Math.sin(rad)).toFloat()),
                        end = Offset(radarCenter.x + (r2 * Math.cos(rad)).toFloat(), radarCenter.y + (r2 * Math.sin(rad)).toFloat()),
                        strokeWidth = 1.dp.toPx(),
                    )
                }

                // Smooth expenditure guideline curve
                val expCurve = Path().apply {
                    moveTo(w * 0.42f, h * 0.38f)
                    cubicTo(w * 0.62f, h * 0.65f, w * 0.74f, h * 0.88f, w * 0.95f, h * 0.82f)
                }
                drawPath(
                    path = expCurve,
                    color = tintColor.copy(alpha = 0.10f),
                    style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round),
                )
            }

            PrismCardTheme.CASH_FLOW -> {
                // --- THẺ DÒNG TIỀN: HỌA TIẾT SÓNG ĐIỀU HÒA ĐÔI & MA TRẬN VECTOR ---
                val gridX = w * 0.62f
                val gridY = h * 0.18f
                val spacing = 13.dp.toPx()
                for (col in 0..3) {
                    for (row in 0..2) {
                        val alpha = (0.05f + col * 0.03f + row * 0.02f).coerceAtMost(0.18f)
                        drawCircle(
                            color = tintColor.copy(alpha = alpha),
                            radius = 1.3.dp.toPx(),
                            center = Offset(gridX + col * spacing, gridY + row * spacing),
                        )
                    }
                }

                // Harmonic Wave 1 (Upper Inflow Crest)
                val harmonicWave1 = Path().apply {
                    moveTo(w * 0.38f, h * 0.68f)
                    cubicTo(w * 0.58f, h * 0.32f, w * 0.75f, h * 0.78f, w * 0.98f, h * 0.40f)
                }
                drawPath(
                    path = harmonicWave1,
                    brush = Brush.linearGradient(
                        colors = listOf(tintColor.copy(alpha = 0.06f), tintColor.copy(alpha = 0.20f)),
                    ),
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round),
                )

                // Harmonic Wave 2 (Lower Counter Valley)
                val harmonicWave2 = Path().apply {
                    moveTo(w * 0.44f, h * 0.48f)
                    cubicTo(w * 0.64f, h * 0.82f, w * 0.80f, h * 0.42f, w, h * 0.74f)
                }
                drawPath(
                    path = harmonicWave2,
                    color = tintColor.copy(alpha = 0.11f),
                    style = Stroke(
                        width = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
                        cap = StrokeCap.Round,
                    ),
                )

                // Crossing equilibrium node
                drawCircle(color = tintColor.copy(alpha = 0.22f), radius = 3.dp.toPx(), center = Offset(w * 0.70f, h * 0.56f))
                drawCircle(color = tintColor.copy(alpha = 0.08f), radius = 7.dp.toPx(), center = Offset(w * 0.70f, h * 0.56f))
            }
        }
    }
}

/**
 * 2. Main Hero Net Worth Card with 3D Wallet & Glowing Accents
 */
@Composable
internal fun PrismHeroNetWorthCard(
    netWorth: Long,
    grossAssets: Long,
    totalDebt: Long,
    showBalance: Boolean,
    onToggleShowBalance: () -> Unit,
    onDebtsClick: () -> Unit = {},
    onWalletsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val shape = RoundedCornerShape(24.dp)
    val pageCount = if (totalDebt > 0L) 2 else 1
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 18.dp,
                shape = shape,
                ambientColor = FinluxPalette.CFF4C68FF.copy(alpha = 0.32f),
                spotColor = FinluxPalette.CFF865BF9.copy(alpha = 0.42f),
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        FinluxPalette.CFF3A5FFF,
                        FinluxPalette.CFF5E50F8,
                        FinluxPalette.CFF7C5AF9,
                        FinluxPalette.CFF9B6EFB,
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            )
            .border(BorderStroke(1.dp, tokens.onHero.copy(alpha = 0.22f)), shape),
    ) {
        // ── Decorative Background Layer ──────────────────────────────
        Canvas(modifier = Modifier.matchParentSize()) {
            // Large top-right glowing orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(tokens.onHero.copy(alpha = 0.18f), FinluxPalette.Transparent),
                    center = Offset(size.width * 0.82f, size.height * 0.0f),
                    radius = size.width * 0.52f,
                ),
                center = Offset(size.width * 0.82f, 0f),
                radius = size.width * 0.52f,
            )

            // Bottom-left secondary orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(FinluxPalette.CFF38BDF8.copy(alpha = 0.22f), FinluxPalette.Transparent),
                    center = Offset(size.width * 0.15f, size.height * 1.1f),
                    radius = size.width * 0.45f,
                ),
                center = Offset(size.width * 0.15f, size.height * 1.1f),
                radius = size.width * 0.45f,
            )

            // Decorative large arc line (top-right)
            drawArc(
                color = tokens.onHero.copy(alpha = 0.08f),
                startAngle = 160f,
                sweepAngle = 100f,
                useCenter = false,
                topLeft = Offset(size.width * 0.45f, -size.width * 0.42f),
                size = Size(size.width * 0.85f, size.width * 0.85f),
                style = Stroke(width = 1.5.dp.toPx()),
            )

            // Decorative smaller arc
            drawArc(
                color = tokens.onHero.copy(alpha = 0.12f),
                startAngle = 165f,
                sweepAngle = 80f,
                useCenter = false,
                topLeft = Offset(size.width * 0.52f, -size.width * 0.28f),
                size = Size(size.width * 0.62f, size.width * 0.62f),
                style = Stroke(width = 1.dp.toPx()),
            )

            // Dots grid pattern (bottom-right area)
            val dotRadius = 1.8.dp.toPx()
            val dotSpacing = 14.dp.toPx()
            val gridStartX = size.width * 0.60f
            val gridStartY = size.height * 0.55f
            for (row in 0..3) {
                for (col in 0..4) {
                    val cx = gridStartX + col * dotSpacing
                    val cy = gridStartY + row * dotSpacing
                    if (cx < size.width - 8.dp.toPx()) {
                        drawCircle(
                            color = tokens.onHero.copy(alpha = 0.18f),
                            radius = dotRadius,
                            center = Offset(cx, cy),
                        )
                    }
                }
            }
        }

        // ── Pager Content ────────────────────────────────────────────
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 22.dp,
                        top = 22.dp,
                        end = 22.dp,
                        bottom = if (pageCount > 1) 28.dp else 22.dp,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (page == 0) {
                        // ── Page 0: Số dư hiện có (Tổng tiền các ví) ─────────────
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onToggleShowBalance,
                            ),
                        ) {
                            Text(
                                text = "Số dư hiện có",
                                style = FinluxTextStyles.Caption.copy(fontSize = 13.sp),
                                color = tokens.onHero.copy(alpha = 0.94f),
                                fontWeight = FontWeight.Medium,
                            )
                            Icon(
                                imageVector = if (showBalance) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Ẩn/Hiện số dư",
                                tint = tokens.onHero.copy(alpha = 0.90f),
                                modifier = Modifier.size(16.dp),
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Display Amount (Gross Assets)
                        Text(
                            text = if (showBalance) formatVndAmount(grossAssets) else "••••••••",
                            style = FinluxTextStyles.DisplayAmount.copy(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp,
                            ),
                            color = tokens.onHero,
                        )

                        Text(
                            text = "Tổng số dư từ tất cả các ví",
                            style = FinluxTextStyles.Caption.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                            ),
                            color = tokens.onHeroMuted,
                        )

                        Spacer(Modifier.height(14.dp))

                        // Breakdown pills
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            // Gross Assets Chip
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = tokens.heroGlassSurface,
                                modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onWalletsClick() },
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = "Ví: " + if (showBalance) formatVndAmount(grossAssets) else "•••",
                                        style = FinluxTextStyles.MicroLabel.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                        ),
                                        color = tokens.onHero,
                                    )
                                }
                            }

                            // Total Debt Chip
                            if (totalDebt > 0L) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = FinluxPalette.CFFE11D48.copy(alpha = 0.35f),
                                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onDebtsClick() },
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Text(
                                            text = "Nợ: " + if (showBalance) formatVndAmount(totalDebt) else "•••",
                                            style = FinluxTextStyles.MicroLabel.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                            ),
                                            color = tokens.onHero,
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // ── Page 1: Tài sản ròng (Net Worth = Ví - Nợ) ───────────
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onToggleShowBalance,
                            ),
                        ) {
                            Text(
                                text = "Tài sản ròng (Net Worth)",
                                style = FinluxTextStyles.Caption.copy(fontSize = 13.sp),
                                color = tokens.onHero.copy(alpha = 0.94f),
                                fontWeight = FontWeight.Medium,
                            )
                            Icon(
                                imageVector = if (showBalance) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Ẩn/Hiện số dư",
                                tint = tokens.onHero.copy(alpha = 0.90f),
                                modifier = Modifier.size(16.dp),
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Display Amount (Net Worth)
                        Text(
                            text = if (showBalance) formatVndAmount(netWorth) else "••••••••",
                            style = FinluxTextStyles.DisplayAmount.copy(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp,
                            ),
                            color = tokens.onHero,
                        )

                        Text(
                            text = "Tổng ví trừ tổng dư nợ",
                            style = FinluxTextStyles.Caption.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                            ),
                            color = tokens.onHeroMuted,
                        )

                        Spacer(Modifier.height(14.dp))

                        // Breakdown pills
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = tokens.heroGlassSurface,
                                modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onWalletsClick() },
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = "Ví: " + if (showBalance) formatVndAmount(grossAssets) else "•••",
                                        style = FinluxTextStyles.MicroLabel.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                        ),
                                        color = tokens.onHero,
                                    )
                                }
                            }

                            if (totalDebt > 0L) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = FinluxPalette.CFFE11D48.copy(alpha = 0.35f),
                                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onDebtsClick() },
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Text(
                                            text = "Nợ: " + if (showBalance) formatVndAmount(totalDebt) else "•••",
                                            style = FinluxTextStyles.MicroLabel.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                            ),
                                            color = tokens.onHero,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3D Glowing Wallet Graphic Composition
                PrismWallet3DIllustration(
                    modifier = Modifier
                        .size(105.dp)
                        .padding(start = 6.dp),
                )
            }
        }

        // ── Page Indicator Dots ──────────────────────────────────────
        if (pageCount > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(pageCount) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .height(4.dp)
                            .width(if (isSelected) 14.dp else 4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) tokens.onHero else tokens.onHero.copy(alpha = 0.35f)),
                    )
                }
            }
        }
    }
}

/**
 * Visual 3D Spatial Holographic Cards & Golden Coin Graphic
 */
@Composable
internal fun PrismWallet3DIllustration(modifier: Modifier = Modifier) {
    val tokens = LocalFinluxTokens.current
    Box(
        modifier = modifier.size(110.dp),
        contentAlignment = Alignment.Center,
    ) {
        // 1. Radial Aura Glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        FinluxPalette.CFF38BDF8.copy(alpha = 0.35f),
                        FinluxPalette.CFF818CF8.copy(alpha = 0.18f),
                        FinluxPalette.Transparent,
                    ),
                    center = center,
                    radius = size.minDimension * 0.70f,
                ),
            )
        }

        // 2. Back Card: Cyber Blue Hologram Card
        Surface(
            modifier = Modifier
                .size(width = 68.dp, height = 44.dp)
                .graphicsLayer {
                    rotationZ = -13f
                    translationX = -6f
                    translationY = -12f
                }
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(10.dp),
                    ambientColor = FinluxPalette.CFF0284C7.copy(alpha = 0.4f),
                    spotColor = FinluxPalette.CFF0284C7.copy(alpha = 0.6f),
                ),
            shape = RoundedCornerShape(10.dp),
            color = FinluxPalette.Transparent,
            border = BorderStroke(1.dp, FinluxPalette.White.copy(alpha = 0.65f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                FinluxPalette.CFF00C6FF,
                                FinluxPalette.CFF0072FF,
                                FinluxPalette.CFF4F46E5,
                            ),
                        ),
                    )
                    .padding(5.dp),
            ) {
                // Micro EMV Chip
                Surface(
                    shape = RoundedCornerShape(2.5.dp),
                    color = FinluxPalette.CFFFDE047,
                    modifier = Modifier.size(width = 11.dp, height = 8.dp),
                ) {}

                // Contactless Signal Waves
                Row(
                    modifier = Modifier.align(Alignment.TopEnd),
                    horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                ) {
                    Box(modifier = Modifier.size(width = 1.5.dp, height = 5.dp).background(FinluxPalette.White.copy(alpha = 0.7f), CircleShape))
                    Box(modifier = Modifier.size(width = 1.5.dp, height = 8.dp).background(FinluxPalette.White.copy(alpha = 0.7f), CircleShape))
                }
            }
        }

        // 3. Front Card: Frosted Platinum Liquid Glass Card
        Surface(
            modifier = Modifier
                .size(width = 82.dp, height = 54.dp)
                .graphicsLayer {
                    rotationZ = 6f
                    translationX = 4f
                    translationY = 6f
                }
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = FinluxPalette.CFF4338CA.copy(alpha = 0.35f),
                    spotColor = FinluxPalette.CFF4338CA.copy(alpha = 0.5f),
                ),
            shape = RoundedCornerShape(12.dp),
            color = FinluxPalette.Transparent,
            border = BorderStroke(
                1.5.dp,
                Brush.linearGradient(
                    listOf(
                        FinluxPalette.White.copy(alpha = 0.95f),
                        FinluxPalette.White.copy(alpha = 0.35f),
                    ),
                ),
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                FinluxPalette.White.copy(alpha = 0.40f),
                                FinluxPalette.White.copy(alpha = 0.15f),
                            ),
                        ),
                    )
                    .padding(6.dp),
            ) {
                // Gold Chip
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = FinluxPalette.CFFF59E0B,
                    border = BorderStroke(0.5.dp, FinluxPalette.CFFFEF08A),
                    modifier = Modifier
                        .size(width = 14.dp, height = 10.dp)
                        .align(Alignment.TopStart),
                ) {}

                // Dual VIP Intersecting Circles
                Row(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    horizontalArrangement = Arrangement.spacedBy((-5).dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .background(FinluxPalette.CFFEC4899.copy(alpha = 0.80f), CircleShape),
                    )
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .background(FinluxPalette.CFFFBBF24.copy(alpha = 0.80f), CircleShape),
                    )
                }

                // Embossed card numbers placeholder
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 2.dp)
                        .size(width = 22.dp, height = 3.dp)
                        .background(FinluxPalette.White.copy(alpha = 0.75f), CircleShape),
                )
            }
        }

        // 4. Floating 3D Gold Coin with ₫ symbol
        Surface(
            modifier = Modifier
                .size(34.dp)
                .align(Alignment.BottomEnd)
                .graphicsLayer {
                    translationX = 10f
                    translationY = 10f
                }
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    ambientColor = FinluxPalette.CFFF59E0B.copy(alpha = 0.6f),
                    spotColor = FinluxPalette.CFFF59E0B.copy(alpha = 0.8f),
                ),
            shape = CircleShape,
            color = FinluxPalette.Transparent,
            border = BorderStroke(1.5.dp, FinluxPalette.White.copy(alpha = 0.95f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                FinluxPalette.CFFFFFBEB,
                                FinluxPalette.CFFFBBF24,
                                FinluxPalette.CFFD97706,
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "₫",
                    color = FinluxPalette.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                )
            }
        }

        // 5. Sparkle Accent (Top Left)
        Canvas(
            modifier = Modifier
                .size(12.dp)
                .align(Alignment.TopStart),
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val rayLength = size.width * 0.40f
            drawLine(
                color = tokens.onHero.copy(alpha = 0.58f),
                start = Offset(center.x, center.y - rayLength),
                end = Offset(center.x, center.y + rayLength),
                strokeWidth = 1.2.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = tokens.onHero.copy(alpha = 0.58f),
                start = Offset(center.x - rayLength, center.y),
                end = Offset(center.x + rayLength, center.y),
                strokeWidth = 1.2.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawCircle(
                color = tokens.onHero.copy(alpha = 0.72f),
                radius = 1.2.dp.toPx(),
                center = center,
            )
        }
    }
}

/**
 * 3. Auto-advancing summary carousel (Thu nhập | Chi tiêu | Dòng tiền ròng).
 * Một KPI lớn tại một thời điểm để giữ số tiền dễ đọc trên màn hình hẹp.
 */
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
@Composable
internal fun PrismQuickActionsRow(
    onWallets: () -> Unit,
    onBudget: () -> Unit,
    onCategories: () -> Unit,
    onGoals: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PrismRoundTile(
            title = "Ví của tôi",
            icon = Icons.Default.AccountBalanceWallet,
            accentColor = FinluxPalette.CFF0D9488,
            bgColor = FinluxPalette.CFFCCFBF1,
            onClick = onWallets,
            modifier = Modifier.weight(1f),
        )
        PrismRoundTile(
            title = "Ngân sách",
            icon = Icons.Default.Savings,
            accentColor = FinluxPalette.CFF8B5CF6,
            bgColor = FinluxPalette.CFFEDE9FE,
            onClick = onBudget,
            modifier = Modifier.weight(1f),
        )
        PrismRoundTile(
            title = "Danh mục",
            icon = Icons.Default.LocalOffer,
            accentColor = FinluxPalette.CFF3B82F6,
            bgColor = FinluxPalette.CFFDBEAFE,
            onClick = onCategories,
            modifier = Modifier.weight(1f),
        )
        PrismRoundTile(
            title = "Mục tiêu",
            icon = Icons.Default.TrackChanges,
            accentColor = FinluxPalette.CFFF97316,
            bgColor = FinluxPalette.CFFFFEDD5,
            onClick = onGoals,
            modifier = Modifier.weight(1f),
        )
        PrismRoundTile(
            title = "Xem thêm",
            icon = Icons.Default.MoreHoriz,
            accentColor = FinluxPalette.CFFA855F7,
            bgColor = FinluxPalette.CFFF3E8FF,
            onClick = onMore,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun PrismRoundTile(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            )
            .padding(horizontal = 2.dp, vertical = 5.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (tokens.isDark) accentColor.copy(alpha = 0.20f) else bgColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(24.dp),
            )
        }

        Text(
            text = title,
            style = FinluxTextStyles.Caption.copy(
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = tokens.onSurface,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * 5. "Chi tiêu theo danh mục" Section with 5-Page Interactive Carousel HorizontalPager
 */
@Composable
internal fun PrismCategoryExpenseBreakdownCard(
    monthTransactions: List<FinanceTransaction>,
    categories: List<Category>,
    wallets: List<Wallet>,
    budgets: List<Budget>,
    totalBudgetPercent: Int,
    showBalance: Boolean,
    onViewDetail: () -> Unit,
    onNavigateToBudget: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 5 })

    val expenseTransactions = remember(monthTransactions) {
        monthTransactions.filter { it.type == TransactionType.EXPENSE }
    }
    val incomeTransactions = remember(monthTransactions) {
        monthTransactions.filter { it.type == TransactionType.INCOME }
    }
    val totalExpense = remember(expenseTransactions) {
        expenseTransactions.sumOf { it.amount.value }
    }
    val totalIncome = remember(incomeTransactions) {
        incomeTransactions.sumOf { it.amount.value }
    }

    // Default sample palette for charts
    val expensePalette1 = listOf(
        FinluxPalette.CFF6366F1, // Indigo
        FinluxPalette.CFFEC4899, // Pink
        FinluxPalette.CFFF97316, // Orange
    )
    val expensePalette2 = listOf(
        FinluxPalette.CFF14B8A6, // Teal
        FinluxPalette.CFF06B6D4, // Cyan
        FinluxPalette.CFF8B5CF6, // Purple
    )
    val incomePalette = listOf(
        FinluxPalette.CFF10B981, // Emerald
        FinluxPalette.CFF06B6D4, // Cyan
        FinluxPalette.CFF3B82F6, // Blue
    )
    val walletPalette = listOf(
        FinluxPalette.CFF3B82F6, // Blue
        FinluxPalette.CFF10B981, // Emerald
        FinluxPalette.CFFF97316, // Orange
    )

    // Compute actual grouped expenses
    val allExpenseShares = remember(expenseTransactions, categories, totalExpense) {
        if (totalExpense > 0L) {
            val grouped = expenseTransactions.groupBy { it.categoryId }
            grouped.mapNotNull { (catId, txs) ->
                val cat = categories.find { it.id == catId } ?: Category(
                    id = catId ?: "other",
                    name = "Khác",
                    type = CategoryType.EXPENSE,
                    icon = "Category",
                    colorHex = "#6366F1",
                    isDefault = false,
                    createdAt = Instant.now(),
                )
                val sum = txs.sumOf { it.amount.value }
                val percent = ((sum * 100.0) / totalExpense).toInt()
                Triple(cat, sum, percent)
            }.sortedByDescending { it.second }
        } else {
            val expenseCats = categories.filter { it.type == CategoryType.EXPENSE }
            if (expenseCats.isNotEmpty()) {
                expenseCats.take(6).map { Triple(it, 0L, 0) }
            } else {
                listOf(
                    Triple(Category("1", "Ăn uống", CategoryType.EXPENSE, "Restaurant", "#EC4899", true, Instant.now()), 0L, 0),
                    Triple(Category("2", "Tiền nhà", CategoryType.EXPENSE, "Home", "#6366F1", true, Instant.now()), 0L, 0),
                    Triple(Category("3", "Mua sắm", CategoryType.EXPENSE, "ShoppingBag", "#06B6D4", true, Instant.now()), 0L, 0),
                )
            }
        }
    }

    // Page 0: Top 1-3 Expenses
    val page0Shares = remember(allExpenseShares) {
        allExpenseShares.take(3)
    }

    // Page 1: Next 4-6 Expenses (or fallback)
    val page1Shares = remember(allExpenseShares) {
        val next = allExpenseShares.drop(3).take(3)
        if (next.isNotEmpty()) next else listOf(
            Triple(Category("4", "Đi lại", CategoryType.EXPENSE, "DirectionsCar", "#14B8A6", true, Instant.now()), 0L, 0),
            Triple(Category("5", "Mua sắm", CategoryType.EXPENSE, "ShoppingBag", "#06B6D4", true, Instant.now()), 0L, 0),
            Triple(Category("6", "Giải trí", CategoryType.EXPENSE, "SportsEsports", "#8B5CF6", true, Instant.now()), 0L, 0),
        )
    }

    // Page 2: Income shares
    val incomeShares = remember(incomeTransactions, categories, totalIncome) {
        if (totalIncome > 0L) {
            val grouped = incomeTransactions.groupBy { it.categoryId }
            grouped.mapNotNull { (catId, txs) ->
                val cat = categories.find { it.id == catId } ?: Category(
                    id = catId ?: "income_other",
                    name = "Khác",
                    type = CategoryType.INCOME,
                    icon = "AccountBalance",
                    colorHex = "#10B981",
                    isDefault = false,
                    createdAt = Instant.now(),
                )
                val sum = txs.sumOf { it.amount.value }
                val percent = ((sum * 100.0) / totalIncome).toInt()
                Triple(cat, sum, percent)
            }.sortedByDescending { it.second }.take(3)
        } else {
            val incomeCats = categories.filter { it.type == CategoryType.INCOME }
            if (incomeCats.isNotEmpty()) {
                incomeCats.take(3).map { Triple(it, 0L, 0) }
            } else {
                listOf(
                    Triple(Category("101", "Lương chính", CategoryType.INCOME, "Work", "#10B981", true, Instant.now()), 0L, 0),
                    Triple(Category("102", "Thưởng", CategoryType.INCOME, "CardGiftcard", "#06B6D4", true, Instant.now()), 0L, 0),
                    Triple(Category("103", "Đầu tư", CategoryType.INCOME, "TrendingUp", "#3B82F6", true, Instant.now()), 0L, 0),
                )
            }
        }
    }

    // Page 3: Real Budget shares
    val budgetShares = remember(budgets, categories, monthTransactions) {
        if (budgets.isNotEmpty()) {
            val expenseMap = monthTransactions
                .filter { it.type == TransactionType.EXPENSE && it.categoryId != null }
                .groupBy { it.categoryId!! }
                .mapValues { (_, txs) -> txs.sumOf { it.amount.value } }

            budgets.mapNotNull { b ->
                val cat = categories.find { it.id == b.categoryId } ?: Category(
                    id = b.categoryId,
                    name = "Ngân sách",
                    type = CategoryType.EXPENSE,
                    icon = "Savings",
                    colorHex = "#8B5CF6",
                    isDefault = false,
                    createdAt = Instant.now(),
                )
                val limit = b.limitAmount.value
                val spent = expenseMap[b.categoryId] ?: 0L
                val pct = if (limit > 0) ((spent * 100.0) / limit).toInt() else 0
                PrismBudgetShareUi(
                    category = cat,
                    spent = spent,
                    limit = limit,
                    percent = pct,
                )
            }.sortedByDescending { it.percent }
        } else {
            emptyList()
        }
    }

    // Page 4: Wallets asset distribution
    val totalWalletBalance = wallets.sumOf { it.balance.value }
    val walletShares = remember(wallets, totalWalletBalance) {
        if (wallets.isNotEmpty()) {
            wallets.map { w ->
                val pct = if (totalWalletBalance > 0L) ((w.balance.value * 100.0) / totalWalletBalance).toInt() else 0
                Triple(
                    Category(id = w.id, name = w.name, type = CategoryType.EXPENSE, icon = "AccountBalanceWallet", colorHex = "#3B82F6", isDefault = false, createdAt = Instant.now()),
                    w.balance.value,
                    pct,
                )
            }.sortedByDescending { it.second }.take(3)
        } else {
            emptyList()
        }
    }

    val dynamicTitle = when (pagerState.currentPage) {
        0 -> "Chi tiêu theo danh mục"
        1 -> "Chi tiêu khác & phụ"
        2 -> "Nguồn thu nhập theo danh mục"
        3 -> "Tiến độ định mức ngân sách"
        4 -> "Cơ cấu tài sản theo ví"
        else -> "Chi tiêu theo danh mục"
    }

    val currentOnViewDetail = if (pagerState.currentPage == 3) onNavigateToBudget else onViewDetail

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = dynamicTitle,
                style = FinluxTextStyles.SectionTitle,
                fontWeight = FontWeight.Bold,
                color = tokens.onSurface,
                fontSize = 18.sp,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = currentOnViewDetail)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "Xem chi tiết",
                    style = FinluxTextStyles.Caption,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.primary,
                )
                Spacer(Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = tokens.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        // Prism analysis surface with HorizontalPager
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = tokens.surface,
            border = BorderStroke(1.dp, tokens.border),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                ) { page ->
                    when (page) {
                        0 -> PrismBreakdownPageContent(
                            shares = page0Shares,
                            colors = expensePalette1,
                            centerAmount = formatVndAmount(totalExpense, isCompact = true),
                            centerLabel = "Tổng chi",
                            showBalance = showBalance,
                        )
                        1 -> PrismBreakdownPageContent(
                            shares = page1Shares,
                            colors = expensePalette2,
                            centerAmount = formatVndAmount(totalExpense, isCompact = true),
                            centerLabel = "Nhóm 2",
                            showBalance = showBalance,
                        )
                        2 -> PrismBreakdownPageContent(
                            shares = incomeShares,
                            colors = incomePalette,
                            centerAmount = formatVndAmount(totalIncome, isCompact = true),
                            centerLabel = "Tổng thu",
                            showBalance = showBalance,
                        )
                        3 -> PrismBudgetBreakdownPageContent(
                            budgetShares = budgetShares,
                            totalSpentPercent = totalBudgetPercent,
                            showBalance = showBalance,
                            onNavigateToBudget = onNavigateToBudget,
                        )
                        4 -> PrismBreakdownPageContent(
                            shares = walletShares,
                            colors = walletPalette,
                            centerAmount = formatVndAmount(totalWalletBalance, isCompact = true),
                            centerLabel = "Tài sản ví",
                            showBalance = showBalance,
                        )
                    }
                }

                // Interactive Carousel Dots Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(5) { idx ->
                        val isSelected = pagerState.currentPage == idx
                        val dotWidth by animateDpAsState(
                            targetValue = if (isSelected) 18.dp else 6.dp,
                            animationSpec = tween(300),
                            label = "dot-w-$idx",
                        )
                        val dotColor by animateColorAsState(
                            targetValue = if (isSelected) tokens.primary else tokens.onSurfaceVariant.copy(alpha = 0.25f),
                            animationSpec = tween(300),
                            label = "dot-c-$idx",
                        )

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(width = dotWidth, height = 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(dotColor)
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(idx)
                                    }
                                },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Data Model for Budget Share Item in Carousel
 */
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
