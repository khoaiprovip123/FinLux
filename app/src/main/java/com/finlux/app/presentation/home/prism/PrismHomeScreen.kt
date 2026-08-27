package com.finlux.app.presentation.home.prism

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.filled.CalendarMonth
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.FinluxUserAvatar
import com.finlux.app.core.designsystem.NotificationPermissionHandler
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.component.FinluxSoftCard
import com.finlux.app.core.designsystem.component.formatVndAmount
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

    Scaffold(
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
        containerColor = tokens.background,
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 8.dp,
                bottom = 140.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
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

            state.salaryCycleLabel?.let { cycleLabel ->
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF10B981).copy(alpha = if (tokens.isDark) 0.16f else 0.10f),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.30f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onNavigate(Route.Settings.value) },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = "Kỳ tài chính: $cycleLabel",
                                    style = FinluxTextStyles.Caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                                    color = Color(0xFF10B981),
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
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
                items(
                    items = state.transactions.take(10),
                    key = { it.id },
                ) { tx ->
                    val category = tx.categoryId?.let { categoriesMap[it] }
                    val wallet = tx.walletId.let { walletsMap[it] }
                    val relatedWallet = tx.relatedWalletId?.let { walletsMap[it] }

                    PrismRecentTransactionItem(
                        transaction = tx,
                        category = category,
                        wallet = wallet,
                        relatedWallet = relatedWallet,
                        showBalance = showBalance,
                        onClick = { onSelectTransaction?.invoke(tx) },
                        onLongClick = onActionTransaction?.let { { it(tx) } },
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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 6.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onProfile,
            ),
        ) {
            Text(
                text = "Xin chào 👋",
                style = FinluxTextStyles.Caption.copy(fontSize = 13.sp),
                color = tokens.onSurfaceVariant,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = displayName,
                style = FinluxTextStyles.ScreenTitle.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = tokens.onSurface,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Notification Bell with badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(tokens.surfaceSoft)
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
                            .padding(6.dp)
                            .size(9.dp),
                    ) {}
                }
            }

            // User Avatar
            FinluxUserAvatar(
                photoUrl = photoUrl,
                displayName = displayName,
                size = 44.dp,
                editable = false,
                onClick = onProfile,
            )
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
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)), shape),
    ) {
        // ── Decorative Background Layer ──────────────────────────────
        Canvas(modifier = Modifier.matchParentSize()) {
            // Large top-right glowing orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.18f), Color.Transparent),
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
                color = Color.White.copy(alpha = 0.08f),
                startAngle = 160f,
                sweepAngle = 100f,
                useCenter = false,
                topLeft = Offset(size.width * 0.45f, -size.width * 0.42f),
                size = Size(size.width * 0.85f, size.width * 0.85f),
                style = Stroke(width = 1.5.dp.toPx()),
            )

            // Decorative smaller arc
            drawArc(
                color = Color.White.copy(alpha = 0.12f),
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
                            color = Color.White.copy(alpha = 0.18f),
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
                                color = Color.White.copy(alpha = 0.90f),
                                fontWeight = FontWeight.Medium,
                            )
                            Icon(
                                imageVector = if (showBalance) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Ẩn/Hiện số dư",
                                tint = Color.White.copy(alpha = 0.85f),
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
                            color = Color.White,
                        )

                        Text(
                            text = "Tổng số dư từ tất cả các ví",
                            style = FinluxTextStyles.Caption.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                            ),
                            color = Color.White.copy(alpha = 0.80f),
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
                                color = Color.White.copy(alpha = 0.22f),
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
                                        color = Color(0xFF4ADE80),
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
                                            color = Color(0xFFFCA5A5),
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
                                color = Color.White.copy(alpha = 0.90f),
                                fontWeight = FontWeight.Medium,
                            )
                            Icon(
                                imageVector = if (showBalance) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Ẩn/Hiện số dư",
                                tint = Color.White.copy(alpha = 0.85f),
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
                            color = Color.White,
                        )

                        Text(
                            text = "Tổng ví trừ tổng dư nợ",
                            style = FinluxTextStyles.Caption.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                            ),
                            color = Color.White.copy(alpha = 0.80f),
                        )

                        Spacer(Modifier.height(14.dp))

                        // Breakdown pills
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.22f),
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
                                        color = Color(0xFF4ADE80),
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
                                            color = Color(0xFFFCA5A5),
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
                            .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.35f)),
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
                .size(18.dp)
                .align(Alignment.TopStart),
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val rayLength = size.width * 0.45f
            drawLine(
                color = Color.White.copy(alpha = 0.90f),
                start = Offset(center.x, center.y - rayLength),
                end = Offset(center.x, center.y + rayLength),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = Color.White.copy(alpha = 0.90f),
                start = Offset(center.x - rayLength, center.y),
                end = Offset(center.x + rayLength, center.y),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = center,
            )
        }
    }
}

/**
 * 3. Metric 3-Column Card (Thu tháng này | Chi tháng này | Dòng tiền (ròng))
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
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Column 1: Thu tháng này
        PrismTrioMetricCard(
            icon = Icons.Default.ArrowDownward,
            accentColor = Color(0xFF10B981),
            title = "Thu tháng này",
            value = if (showBalance) formatVndAmount(income) else "••••",
            trendText = if (income == 0L) "— 0%" else "▲ Thu nhập",
            trendSubtext = "so với trước",
            isTrendPositive = if (income == 0L) null else true,
            onClick = onIncomeClick,
            modifier = Modifier.weight(1f),
        )

        // Column 2: Chi tháng này
        PrismTrioMetricCard(
            icon = Icons.Default.ArrowUpward,
            accentColor = Color(0xFFF43F5E),
            title = "Chi tháng này",
            value = if (showBalance) formatVndAmount(expense) else "••••",
            trendText = if (expense == 0L) "— 0%" else "▼ Chi tiêu",
            trendSubtext = "so với trước",
            isTrendPositive = if (expense == 0L) null else false,
            onClick = onExpenseClick,
            modifier = Modifier.weight(1f),
        )

        // Column 3: Dòng tiền (ròng)
        PrismTrioMetricCard(
            icon = Icons.Default.PieChart,
            accentColor = if (net < 0) Color(0xFFF43F5E) else Color(0xFF6366F1),
            title = "Dòng tiền (ròng)",
            value = if (showBalance) (if (net < 0) "-${formatVndAmount(-net)}" else "+${formatVndAmount(net)}") else "••••",
            trendText = if (net == 0L) "— 0%" else (if (net < 0) "▼ Âm ví" else "▲ Dương"),
            trendSubtext = "so với trước",
            isTrendPositive = if (net == 0L) null else (net >= 0),
            onClick = onNetClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PrismTrioMetricCard(
    icon: ImageVector,
    accentColor: Color,
    title: String,
    value: String,
    trendText: String,
    trendSubtext: String,
    isTrendPositive: Boolean?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val shape = RoundedCornerShape(20.dp)

    Surface(
        shape = shape,
        color = tokens.surface,
        border = BorderStroke(
            1.2.dp,
            if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0x1A5A6EA0),
        ),
        shadowElevation = if (tokens.isDark) 0.dp else 2.dp,
        modifier = modifier
            .clip(shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = if (tokens.isDark) 0.14f else 0.07f),
                            Color.Transparent,
                        ),
                    ),
                )
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Top: Circular Icon
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = if (tokens.isDark) 0.22f else 0.14f))
                    .border(BorderStroke(1.dp, accentColor.copy(alpha = 0.30f)), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Title
            Text(
                text = title,
                style = FinluxTextStyles.Caption.copy(
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = tokens.onSurfaceVariant,
                maxLines = 1,
            )

            // Amount Value
            Text(
                text = value,
                style = FinluxTextStyles.CardTitle.copy(
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.3).sp,
                ),
                color = tokens.onSurface,
                maxLines = 1,
            )

            // Bottom Trend Pill
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (isTrendPositive) {
                    true -> Color(0xFF10B981).copy(alpha = if (tokens.isDark) 0.20f else 0.12f)
                    false -> Color(0xFFF43F5E).copy(alpha = if (tokens.isDark) 0.20f else 0.12f)
                    null -> tokens.surfaceSoft
                },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = trendText,
                        style = FinluxTextStyles.MicroLabel.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = when (isTrendPositive) {
                            true -> Color(0xFF10B981)
                            false -> Color(0xFFF43F5E)
                            null -> tokens.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PrismRoundTile(
            title = "Ví của tôi",
            icon = Icons.Default.AccountBalanceWallet,
            accentColor = Color(0xFF0D9488),
            bgColor = Color(0xFFCCFBF1),
            onClick = onWallets,
        )
        PrismRoundTile(
            title = "Ngân sách",
            icon = Icons.Default.Savings,
            accentColor = Color(0xFF8B5CF6),
            bgColor = Color(0xFFEDE9FE),
            onClick = onBudget,
        )
        PrismRoundTile(
            title = "Danh mục",
            icon = Icons.Default.LocalOffer,
            accentColor = Color(0xFF3B82F6),
            bgColor = Color(0xFFDBEAFE),
            onClick = onCategories,
        )
        PrismRoundTile(
            title = "Mục tiêu",
            icon = Icons.Default.TrackChanges,
            accentColor = Color(0xFFF97316),
            bgColor = Color(0xFFFFEDD5),
            onClick = onGoals,
        )
        PrismRoundTile(
            title = "Xem thêm",
            icon = Icons.Default.MoreHoriz,
            accentColor = Color(0xFFA855F7),
            bgColor = Color(0xFFF3E8FF),
            onClick = onMore,
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
) {
    val tokens = LocalFinluxTokens.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            )
            .padding(horizontal = 4.dp, vertical = 4.dp),
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
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = tokens.onSurface,
            maxLines = 1,
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
        FinluxSoftCard(
            modifier = Modifier.fillMaxWidth(),
            radius = 22.dp,
            padding = 18.dp,
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
                .weight(0.44f)
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

        Spacer(Modifier.width(12.dp))

        // Right: Category legend list
        Column(
            modifier = Modifier.weight(0.56f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            shares.forEachIndexed { index, (cat, sum, percent) ->
                val color = colors.getOrElse(index) { Color(0xFF6366F1) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Color Dot
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(color),
                    )
                    Spacer(Modifier.width(6.dp))

                    // Category Icon
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = color.copy(alpha = 0.12f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = categoryIcon(cat.icon),
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(13.dp),
                            )
                        }
                    }

                    Spacer(Modifier.width(6.dp))

                    // Category Name
                    Text(
                        text = cat.name,
                        style = FinluxTextStyles.Caption.copy(fontSize = 12.5.sp),
                        color = tokens.onSurface,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )

                    // Amount
                    Text(
                        text = if (showBalance) formatVndAmount(sum) else "••••",
                        style = FinluxTextStyles.Caption.copy(
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = tokens.onSurface,
                    )

                    Spacer(Modifier.width(6.dp))

                    // Percentage badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = tokens.background,
                    ) {
                        Text(
                            text = "$percent%",
                            style = FinluxTextStyles.MicroLabel.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = tokens.onSurfaceVariant,
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
            // Category / Transfer Icon
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = accentColor.copy(alpha = if (tokens.isDark) 0.18f else 0.12f),
                modifier = Modifier.size(44.dp),
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

            Spacer(Modifier.width(12.dp))

            // Center: Note + Date
            Column(
                modifier = Modifier.weight(1f),
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
                    text = dateText,
                    style = FinluxTextStyles.Caption.copy(fontSize = 12.sp),
                    color = tokens.onSurfaceVariant,
                    maxLines = 1,
                )
            }

            Spacer(Modifier.width(8.dp))

            // Right: Amount + Wallet
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
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
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    color = amountColor,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = walletDisplayName,
                        style = FinluxTextStyles.MicroLabel.copy(fontSize = 11.sp),
                        color = tokens.onSurfaceVariant,
                        maxLines = 1,
                    )
                    Icon(
                        imageVector = if (isTransfer) Icons.Default.SwapHoriz else Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = tokens.onSurfaceVariant,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
    }
}
