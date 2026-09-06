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
fun PrismHomeScreen(
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit,
    onNotifications: () -> Unit,
    onSelectTransaction: ((FinanceTransaction) -> Unit)? = null,
    onActionTransaction: ((FinanceTransaction) -> Unit)? = null,
    onEditTransaction: ((FinanceTransaction) -> Unit)? = null,
    viewModel: HomeViewModel = hiltViewModel(),
    savingSpinState: SavingSpinUiState = SavingSpinUiState(),
    onSavingSpinAction: (SavingSpinAction) -> Unit = {},
) {
    NotificationPermissionHandler()
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val totalBalance = state.wallets.sumOf { it.balance.value }
    val categoriesMap = remember(state.categories) { state.categories.associateBy(Category::id) }
    val walletsMap = remember(state.wallets) { state.wallets.associateBy(Wallet::id) }
    val showBalance = state.showBalance
    val tokens = LocalFinluxTokens.current
    val today = remember { java.time.LocalDate.now() }
    val todayTransactions = remember(state.transactions) {
        state.transactions.filter { tx ->
            tx.date.atZone(java.time.ZoneId.systemDefault()).toLocalDate() == today
        }
    }
    val displayTransactions = remember(state.transactions, todayTransactions) {
        if (todayTransactions.isNotEmpty()) todayTransactions.take(5) else state.transactions.take(5)
    }
    val totalRecentCount = if (todayTransactions.isNotEmpty()) todayTransactions.size else state.transactions.size
    val hasMoreTransactions = (todayTransactions.isNotEmpty() && todayTransactions.size > 5) || (todayTransactions.isEmpty() && state.transactions.size > 5)
    val isTodaySection = todayTransactions.isNotEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(tokens.background),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 0.dp,
                bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                PrismHomeTopHeader(
                    displayName = state.user?.displayName?.ifBlank { "Văn Khoai" } ?: "Văn Khoai",
                    photoUrl = state.user?.photoUrl,
                    unreadCount = state.unreadNotificationsCount,
                    onProfile = { onNavigate(Route.Settings.value) },
                    onNotifications = onNotifications,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // 1. Unified financial overview: balance hero + auto Thu/Chi/Dòng tiền carousel
            item {
                PrismFinancialOverviewCard(
                    netWorth = state.netWorth,
                    grossAssets = state.grossAssets,
                    totalDebt = state.totalDebt,
                    income = state.summary.income.value,
                    expense = state.summary.expense.value,
                    net = state.summary.net,
                    monthTransactions = state.monthTransactions.ifEmpty { state.transactions },
                    wallets = state.wallets,
                    salaryCycleLabel = state.salaryCycleLabel,
                    showBalance = showBalance,
                    onToggleShowBalance = viewModel::toggleBalanceVisibility,
                    onDebtsClick = { onNavigate(Route.Debt.value) },
                    onWalletsClick = { onNavigate(Route.Wallets.value) },
                    onIncomeClick = { onNavigate(Route.Income.value) },
                    onExpenseClick = { onNavigate(Route.Expense.value) },
                    onNetClick = { onNavigate(Route.Reports.value) },
                )
            }

            if (savingSpinState.config.enabled && savingSpinState.config.showOnHome && savingSpinState.session != null) {
                item {
                    SavingSpinHomeCard(
                        state = savingSpinState,
                        onOpen = { onSavingSpinAction(SavingSpinAction.OpenGame) },
                    )
                }
            }

            // 4. Quick 5-Action Buttons Row (Ví của tôi, Ngân sách, Danh mục, Mục tiêu, Xem thêm)
            item {
                PrismQuickActionsRow(
                    onWallets = { onNavigate(Route.Wallets.value) },
                    onBudget = { onNavigate(Route.Budget.value) },
                    onCategories = { onNavigate(Route.Categories.value) },
                    onGoals = { onNavigate(Route.Goals.value) },
                    onMore = { onNavigate(Route.Settings.value) },
                )
            }

            // 5. "Chi tiêu theo danh mục" Section with Donut Chart Horizontal Pager
            item {
                PrismCategoryExpenseBreakdownCard(
                    monthTransactions = state.monthTransactions.ifEmpty { state.transactions },
                    categories = state.categories,
                    wallets = state.wallets,
                    budgets = state.budgets,
                    totalBudgetPercent = state.totalBudgetPercent,
                    showBalance = showBalance,
                    onViewDetail = { onNavigate(Route.Reports.value) },
                    onNavigateToBudget = { onNavigate(Route.Budget.value) },
                )
            }

            // 6. "Giao dịch gần nhất" Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (isTodaySection) "Giao dịch hôm nay" else "Giao dịch gần nhất",
                        style = FinluxTextStyles.SectionTitle,
                        fontWeight = FontWeight.Bold,
                        color = tokens.onSurface,
                        fontSize = 18.sp,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onNavigate(Route.Transactions.value) }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "Xem tất cả ($totalRecentCount)",
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
            }

            // 7. Recent Transactions List (Tối đa 5 giao dịch của ngày hôm nay, vượt quá chuyển qua Lịch sử)
            if (displayTransactions.isEmpty()) {
                item {
                    FinluxEmptyState(
                        title = "Chưa có giao dịch nào",
                        description = "Bấm '+' để ghi lại khoản thu chi đầu tiên của bạn.",
                    )
                }
            } else {
                items(
                    items = displayTransactions,
                    key = { it.id },
                ) { transaction ->
                    val category = transaction.categoryId?.let { categoriesMap[it] }
                    val wallet = walletsMap[transaction.walletId]

                    PrismHomeExplorerTransactionCard(
                        transaction = transaction,
                        category = category,
                        wallet = wallet,
                        showBalance = showBalance,
                        onClick = { onSelectTransaction?.invoke(transaction) },
                        onLongClick = { onActionTransaction?.invoke(transaction) },
                    )
                }

                // Nút chuyển sang Lịch sử để xem thêm khi vượt quá 5 giao dịch
                if (hasMoreTransactions) {
                    item(key = "see_more_home_tx_btn") {
                        val remainingCount = if (todayTransactions.isNotEmpty()) todayTransactions.size - 5 else state.transactions.size - 5
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (tokens.isDark) tokens.surfaceSoft else FinluxPalette.White,
                            border = BorderStroke(1.dp, if (tokens.isDark) tokens.border else FinluxPalette.CFFE2E8F0.copy(alpha = 0.8f)),
                            shadowElevation = if (tokens.isDark) 0.dp else 1.5.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = { onNavigate(Route.Transactions.value) },
                                ),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 13.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Xem thêm $remainingCount giao dịch trong Lịch sử",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = tokens.primary,
                                )
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = tokens.primary,
                                    modifier = Modifier.size(17.dp),
                                )
                            }
                        }
                    }
                }
            }

        }
    }
}

/**
 * 1. Top Header: "Xin chào 👋" + Name + Bell + Avatar
 */
internal const val PRISM_HOME_HEADER_HEIGHT_DP = 52
internal const val PRISM_FINANCIAL_HERO_HEIGHT_DP = 180

private const val PRISM_FINANCIAL_OVERVIEW_PAGE_COUNT = 4

internal fun nextPrismOverviewPage(currentPage: Int): Int =
    (currentPage + 1).mod(PRISM_FINANCIAL_OVERVIEW_PAGE_COUNT)

private enum class PrismCardTheme {
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

/**
 * 2. Main Hero Net Worth Card with 3D Wallet & Glowing Accents
 */
,
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
/**
 * 3. Auto-advancing summary carousel (Thu nhập | Chi tiêu | Dòng tiền ròng).
 * Một KPI lớn tại một thời điểm để giữ số tiền dễ đọc trên màn hình hẹp.
 */
private const val PRISM_SUMMARY_PAGE_COUNT = 3
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

private enum class PrismMetricWatermarkType {
    INCOME, EXPENSE, NET
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
/**
 * 5. "Chi tiêu theo danh mục" Section with 5-Page Interactive Carousel HorizontalPager
 */
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
/**
 * Reusable Page Content for Carousel Breakdown
 */
/**
 * Donut Chart Canvas Composable
 */
/**
 * 7. Recent Transaction Item
 * - Tap → xem chi tiết
 * - Long press → hiển thị pop-up Sửa / Xóa
 */
/**
 * Thẻ giao dịch đồng bộ chuẩn 3 cột với màn hình Giao dịch (Transaction Explorer)
 */
