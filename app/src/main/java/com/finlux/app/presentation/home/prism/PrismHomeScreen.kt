package com.finlux.app.presentation.home.prism

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.finlux.app.core.designsystem.modern.GlassCard as LiquidGlassCard
import com.finlux.app.core.designsystem.modern.LiquidGlassMode
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.navigation.Route
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.presentation.components.MainBottomBar
import com.finlux.app.presentation.home.HomeViewModel
import com.finlux.app.presentation.transaction.TransactionActionDialog
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue
import kotlinx.coroutines.delay

@Composable
fun PrismHomeScreen(
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit,
    onNotifications: () -> Unit,
    onSelectTransaction: ((FinanceTransaction) -> Unit)? = null,
    onActionTransaction: ((FinanceTransaction) -> Unit)? = null,
    onEditTransaction: ((FinanceTransaction) -> Unit)? = null,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    NotificationPermissionHandler()
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val totalBalance = state.wallets.sumOf { it.balance.value }
    val categoriesMap = remember(state.categories) { state.categories.associateBy(Category::id) }
    val walletsMap = remember(state.wallets) { state.wallets.associateBy(Wallet::id) }
    var showBalance by remember { mutableStateOf(true) }
    val tokens = LocalFinluxTokens.current

    FinluxScreenScaffold(
        showBackdrop = false,
        containerColor = tokens.background,
        topBar = {
            PrismHomeTopHeader(
                displayName = state.user?.displayName?.ifBlank { "Văn Khoai" } ?: "Văn Khoai",
                photoUrl = state.user?.photoUrl,
                unreadCount = state.unreadNotificationsCount,
                onProfile = { onNavigate(Route.Settings.value) },
                onNotifications = onNotifications,
                modifier = Modifier
                    .background(tokens.background)
                    .padding(horizontal = 20.dp),
            )
        },
    ) { scaffoldPadding ->
        FinluxLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
            listType = FinluxListType.TAB_MAIN,
        ) {
            // 1. Main Hero Card (Tài sản ròng = Tổng tài sản - Tổng dư nợ + 3D Wallet illustration)
            item {
                PrismHeroNetWorthCard(
                    netWorth = state.netWorth,
                    grossAssets = state.grossAssets,
                    totalDebt = state.totalDebt,
                    showBalance = showBalance,
                    onToggleShowBalance = { showBalance = !showBalance },
                    onDebtsClick = { onNavigate(Route.Debt.value) },
                    onWalletsClick = { onNavigate(Route.Wallets.value) },
                )
            }

            // 3. Metric 3-Column Card (Thu tháng này | Chi tháng này | Dòng tiền (ròng))
            item {
                PrismSummaryTrioCard(
                    income = state.summary.income.value,
                    expense = state.summary.expense.value,
                    net = state.summary.net,
                    showBalance = showBalance,
                    onIncomeClick = { onNavigate(Route.Income.value) },
                    onExpenseClick = { onNavigate(Route.Expense.value) },
                    onNetClick = { onNavigate(Route.Reports.value) },
                )
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
                    showBalance = showBalance,
                    onViewDetail = { onNavigate(Route.Reports.value) },
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
                        text = "Giao dịch gần nhất",
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
                            text = "Xem tất cả",
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

            // 7. Recent Transactions List
            if (state.transactions.isEmpty()) {
                item {
                    FinluxEmptyState(
                        title = "Chưa có giao dịch nào",
                        description = "Bấm '+' để ghi lại khoản thu chi đầu tiên của bạn.",
                    )
                }
            } else {
                item(key = "recent_transaction_group") {
                    FinluxTransactionGroup(
                        transactions = state.transactions.take(10),
                        categories = categoriesMap,
                        wallets = walletsMap,
                        showAmounts = showBalance,
                        onTransactionClick = { tx -> onSelectTransaction?.invoke(tx) },
                        onTransactionLongClick = onActionTransaction,
                    )
                }

                // "Xem thêm" button when there are more than 10 transactions
                if (state.transactions.size > 10) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = tokens.surfaceSoft,
                            border = BorderStroke(1.dp, tokens.primary.copy(alpha = 0.20f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = { onNavigate(Route.Transactions.value) },
                                ),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Xem tất cả ${state.transactions.size} giao dịch",
                                    style = FinluxTextStyles.Caption.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = tokens.primary,
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = tokens.primary,
                                    modifier = Modifier.size(18.dp),
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
@Composable
private fun PrismHomeTopHeader(
    displayName: String,
    photoUrl: String?,
    unreadCount: Int,
    onProfile: () -> Unit,
    onNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 8.dp, bottom = 10.dp),
    ) {
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            mode = LiquidGlassMode.CLEAR,
            tint = tokens.primary,
            shape = RoundedCornerShape(22.dp),
            elevation = if (tokens.isDark) 3.dp else 5.dp,
            padding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FinluxUserAvatar(
                    photoUrl = photoUrl,
                    displayName = displayName,
                    size = 48.dp,
                    editable = false,
                    onClick = onProfile,
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp, end = 10.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onProfile,
                        ),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    Text(
                        text = "Xin chào 👋",
                        style = FinluxTextStyles.Caption.copy(fontSize = 12.sp),
                        color = tokens.onSurfaceVariant,
                    )
                    Text(
                        text = displayName,
                        style = FinluxTextStyles.ScreenTitle.copy(
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = tokens.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(tokens.surfaceSoft.copy(alpha = if (tokens.isDark) 0.72f else 0.86f))
                        .border(BorderStroke(1.dp, tokens.border), RoundedCornerShape(15.dp))
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
                        modifier = Modifier.size(22.dp),
                    )
                    if (unreadCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = FinluxColors.ExpenseRed,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 4.dp, end = 4.dp)
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
    }
}

/**
 * 2. Main Hero Net Worth Card with 3D Wallet & Glowing Accents
 */
@Composable
private fun PrismHeroNetWorthCard(
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
                ambientColor = Color(0xFF4C68FF).copy(alpha = 0.32f),
                spotColor = Color(0xFF865BF9).copy(alpha = 0.42f),
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF3A5FFF),
                        Color(0xFF5E50F8),
                        Color(0xFF7C5AF9),
                        Color(0xFF9B6EFB),
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
                    colors = listOf(tokens.onHero.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(size.width * 0.82f, size.height * 0.0f),
                    radius = size.width * 0.52f,
                ),
                center = Offset(size.width * 0.82f, 0f),
                radius = size.width * 0.52f,
            )

            // Bottom-left secondary orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.22f), Color.Transparent),
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
                                    color = Color(0xFFE11D48).copy(alpha = 0.35f),
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
                                    color = Color(0xFFE11D48).copy(alpha = 0.35f),
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
private fun PrismWallet3DIllustration(modifier: Modifier = Modifier) {
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
                        Color(0xFF38BDF8).copy(alpha = 0.35f),
                        Color(0xFF818CF8).copy(alpha = 0.18f),
                        Color.Transparent,
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
                    ambientColor = Color(0xFF0284C7).copy(alpha = 0.4f),
                    spotColor = Color(0xFF0284C7).copy(alpha = 0.6f),
                ),
            shape = RoundedCornerShape(10.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF00C6FF),
                                Color(0xFF0072FF),
                                Color(0xFF4F46E5),
                            ),
                        ),
                    )
                    .padding(5.dp),
            ) {
                // Micro EMV Chip
                Surface(
                    shape = RoundedCornerShape(2.5.dp),
                    color = Color(0xFFFDE047),
                    modifier = Modifier.size(width = 11.dp, height = 8.dp),
                ) {}

                // Contactless Signal Waves
                Row(
                    modifier = Modifier.align(Alignment.TopEnd),
                    horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                ) {
                    Box(modifier = Modifier.size(width = 1.5.dp, height = 5.dp).background(Color.White.copy(alpha = 0.7f), CircleShape))
                    Box(modifier = Modifier.size(width = 1.5.dp, height = 8.dp).background(Color.White.copy(alpha = 0.7f), CircleShape))
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
                    ambientColor = Color(0xFF4338CA).copy(alpha = 0.35f),
                    spotColor = Color(0xFF4338CA).copy(alpha = 0.5f),
                ),
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent,
            border = BorderStroke(
                1.5.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.95f),
                        Color.White.copy(alpha = 0.35f),
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
                                Color.White.copy(alpha = 0.40f),
                                Color.White.copy(alpha = 0.15f),
                            ),
                        ),
                    )
                    .padding(6.dp),
            ) {
                // Gold Chip
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = Color(0xFFF59E0B),
                    border = BorderStroke(0.5.dp, Color(0xFFFEF08A)),
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
                            .background(Color(0xFFEC4899).copy(alpha = 0.80f), CircleShape),
                    )
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .background(Color(0xFFFBBF24).copy(alpha = 0.80f), CircleShape),
                    )
                }

                // Embossed card numbers placeholder
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 2.dp)
                        .size(width = 22.dp, height = 3.dp)
                        .background(Color.White.copy(alpha = 0.75f), CircleShape),
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
                    ambientColor = Color(0xFFF59E0B).copy(alpha = 0.6f),
                    spotColor = Color(0xFFF59E0B).copy(alpha = 0.8f),
                ),
            shape = CircleShape,
            color = Color.Transparent,
            border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.95f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFFBEB),
                                Color(0xFFFBBF24),
                                Color(0xFFD97706),
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "₫",
                    color = Color.White,
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
private fun PrismSummaryTrioCard(
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

    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Tình hình tháng này",
                style = FinluxTextStyles.SectionTitle.copy(fontSize = 16.sp),
                color = tokens.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = tokens.primary.copy(alpha = if (tokens.isDark) 0.18f else 0.10f),
                border = BorderStroke(0.75.dp, tokens.primary.copy(alpha = 0.22f)),
            ) {
                Text(
                    text = "Tự chuyển • 10 giây",
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
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
                .height(126.dp),
            pageSpacing = 10.dp,
        ) { page ->
            val pageOffset = (
                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            ).absoluteValue.coerceIn(0f, 1f)
            val metric = metrics[page]
            PrismTrioMetricCard(
                metric = metric,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 1f - (pageOffset * 0.035f)
                        scaleY = 1f - (pageOffset * 0.035f)
                        alpha = 1f - (pageOffset * 0.16f)
                    },
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

private const val PRISM_SUMMARY_PAGE_COUNT = 3
internal const val PRISM_SUMMARY_AUTO_ADVANCE_MS = 10_000L

internal fun nextPrismSummaryPage(currentPage: Int): Int =
    (currentPage + 1).mod(PRISM_SUMMARY_PAGE_COUNT)

private data class PrismSummaryMetricUi(
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

@Composable
private fun PrismMetricMiniBadge(
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
private fun PrismTrioMetricCard(
    metric: PrismSummaryMetricUi,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    LiquidGlassCard(
        modifier = modifier
            .shadow(
                elevation = if (tokens.isDark) 7.dp else 5.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = metric.accentColor.copy(alpha = if (tokens.isDark) 0.28f else 0.16f),
                ambientColor = tokens.primary.copy(alpha = if (tokens.isDark) 0.14f else 0.07f),
            ),
        mode = LiquidGlassMode.REGULAR,
        tint = metric.accentColor,
        shape = RoundedCornerShape(22.dp),
        elevation = if (tokens.isDark) 5.dp else 3.dp,
        padding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        onClick = metric.onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
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
                        shadow = Shadow(
                            color = metric.accentColor.copy(alpha = if (tokens.isDark) 0.36f else 0.14f),
                            offset = Offset(0f, 1.2f),
                            blurRadius = 3f,
                        ),
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
                    .size(62.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                metric.accentColor.copy(alpha = if (tokens.isDark) 0.30f else 0.16f),
                                metric.accentColor.copy(alpha = if (tokens.isDark) 0.12f else 0.06f),
                            ),
                        ),
                    )
                    .border(
                        BorderStroke(1.dp, metric.accentColor.copy(alpha = 0.30f)),
                        RoundedCornerShape(20.dp),
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
                    modifier = Modifier.size(30.dp),
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
private fun PrismQuickActionsRow(
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
            accentColor = Color(0xFF0D9488),
            bgColor = Color(0xFFCCFBF1),
            onClick = onWallets,
            modifier = Modifier.weight(1f),
        )
        PrismRoundTile(
            title = "Ngân sách",
            icon = Icons.Default.Savings,
            accentColor = Color(0xFF8B5CF6),
            bgColor = Color(0xFFEDE9FE),
            onClick = onBudget,
            modifier = Modifier.weight(1f),
        )
        PrismRoundTile(
            title = "Danh mục",
            icon = Icons.Default.LocalOffer,
            accentColor = Color(0xFF3B82F6),
            bgColor = Color(0xFFDBEAFE),
            onClick = onCategories,
            modifier = Modifier.weight(1f),
        )
        PrismRoundTile(
            title = "Mục tiêu",
            icon = Icons.Default.TrackChanges,
            accentColor = Color(0xFFF97316),
            bgColor = Color(0xFFFFEDD5),
            onClick = onGoals,
            modifier = Modifier.weight(1f),
        )
        PrismRoundTile(
            title = "Xem thêm",
            icon = Icons.Default.MoreHoriz,
            accentColor = Color(0xFFA855F7),
            bgColor = Color(0xFFF3E8FF),
            onClick = onMore,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PrismRoundTile(
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
private fun PrismCategoryExpenseBreakdownCard(
    monthTransactions: List<FinanceTransaction>,
    categories: List<Category>,
    wallets: List<Wallet>,
    showBalance: Boolean,
    onViewDetail: () -> Unit,
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
        Color(0xFF6366F1), // Indigo
        Color(0xFFEC4899), // Pink
        Color(0xFFF97316), // Orange
    )
    val expensePalette2 = listOf(
        Color(0xFF14B8A6), // Teal
        Color(0xFF06B6D4), // Cyan
        Color(0xFF8B5CF6), // Purple
    )
    val incomePalette = listOf(
        Color(0xFF10B981), // Emerald
        Color(0xFF06B6D4), // Cyan
        Color(0xFF3B82F6), // Blue
    )
    val budgetPalette = listOf(
        Color(0xFF8B5CF6), // Violet
        Color(0xFFEC4899), // Pink
        Color(0xFFF59E0B), // Amber
    )
    val walletPalette = listOf(
        Color(0xFF3B82F6), // Blue
        Color(0xFF10B981), // Emerald
        Color(0xFFF97316), // Orange
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

    // Page 3: Budget shares
    val budgetShares = listOf(
        Triple(Category("201", "Thiết yếu (50%)", CategoryType.EXPENSE, "Shield", "#8B5CF6", true, Instant.now()), 0L, 0),
        Triple(Category("202", "Mong muốn (30%)", CategoryType.EXPENSE, "Favorite", "#EC4899", true, Instant.now()), 0L, 0),
        Triple(Category("203", "Tiết kiệm (20%)", CategoryType.EXPENSE, "Savings", "#F59E0B", true, Instant.now()), 0L, 0),
    )

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
        3 -> "Phân bổ định mức ngân sách"
        4 -> "Cơ cấu tài sản theo ví"
        else -> "Chi tiêu theo danh mục"
    }

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
                    .clickable(onClick = onViewDetail)
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

        // Breakdown Card with HorizontalPager
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            mode = LiquidGlassMode.REGULAR,
            tint = tokens.primary,
            shape = RoundedCornerShape(22.dp),
            elevation = if (tokens.isDark) 4.dp else 6.dp,
            padding = PaddingValues(18.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
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
                        3 -> PrismBreakdownPageContent(
                            shares = budgetShares,
                            colors = budgetPalette,
                            centerAmount = "0%",
                            centerLabel = "Đã chi tiêu",
                            showBalance = showBalance,
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
 * Reusable Page Content for Carousel Breakdown
 */
@Composable
private fun PrismBreakdownPageContent(
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
                val color = colors.getOrElse(index) { Color(0xFF6366F1) }
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
private fun PrismDonutChart(
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
                color = Color(0xFF6366F1).copy(alpha = 0.2f),
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
private fun PrismRecentTransactionItem(
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
