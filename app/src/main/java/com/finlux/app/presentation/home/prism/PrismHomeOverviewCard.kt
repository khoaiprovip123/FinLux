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
