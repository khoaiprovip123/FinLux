package com.finlux.app.presentation.home.prism

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
        bottomBar = { MainBottomBar(Route.Home.value, onNavigate, onAdd) },
        containerColor = tokens.background,
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 10.dp,
                bottom = 140.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // 1. Top Header: "Xin chào 👋" + "Văn Khoai" + Bell notification + Avatar
            item {
                PrismHomeTopHeader(
                    displayName = state.user?.displayName?.ifBlank { "Văn Khoai" } ?: "Văn Khoai",
                    photoUrl = state.user?.photoUrl,
                    unreadCount = state.unreadNotificationsCount,
                    onProfile = { onNavigate(Route.Settings.value) },
                    onNotifications = onNotifications,
                )
            }

            // 2. Main Hero Card (Tổng tài sản - 6.110.000 đ - Tài sản ròng + 3D Wallet illustration)
            item {
                PrismHeroNetWorthCard(
                    totalBalance = totalBalance,
                    showBalance = showBalance,
                    onToggleShowBalance = { showBalance = !showBalance },
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

            // 5. "Chi tiêu theo danh mục" Section with Donut Chart
            item {
                PrismCategoryExpenseBreakdownCard(
                    monthTransactions = state.monthTransactions.ifEmpty { state.transactions },
                    categories = state.categories,
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
                    items = state.transactions.take(6),
                    key = { it.id },
                ) { tx ->
                    val category = tx.categoryId?.let { categoriesMap[it] }
                    val wallet = tx.walletId.let { walletsMap[it] }

                    PrismRecentTransactionItem(
                        transaction = tx,
                        category = category,
                        wallet = wallet,
                        showBalance = showBalance,
                        onClick = {
                            if (onSelectTransaction != null) {
                                onSelectTransaction(tx)
                            } else if (onEditTransaction != null) {
                                onEditTransaction(tx)
                            }
                        },
                        onLongClick = onActionTransaction?.let { { it(tx) } },
                    )
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
            .padding(top = 4.dp, bottom = 4.dp),
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
    totalBalance: Long,
    showBalance: Boolean,
    onToggleShowBalance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 14.dp,
                shape = shape,
                ambientColor = Color(0xFF4C68FF).copy(alpha = 0.28f),
                spotColor = Color(0xFF865BF9).copy(alpha = 0.36f),
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF4C66FF),
                        Color(0xFF6B58F8),
                        Color(0xFF865BF9),
                        Color(0xFF9F72FB),
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            )
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)), shape)
            .padding(22.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Top label + eye icon
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
                        text = "Tổng tài sản",
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

                // Display Amount
                Text(
                    text = if (showBalance) formatVndAmount(totalBalance) else "••••••••",
                    style = FinluxTextStyles.DisplayAmount.copy(
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp,
                    ),
                    color = Color.White,
                )

                Text(
                    text = "Tài sản ròng",
                    style = FinluxTextStyles.Caption.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = Color.White.copy(alpha = 0.85f),
                )

                Spacer(Modifier.height(14.dp))

                // Comparison badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.22f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "↑ +2,4%",
                                style = FinluxTextStyles.MicroLabel.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                ),
                                color = Color(0xFF4ADE80),
                            )
                        }
                    }

                    Text(
                        text = "so với tháng trước",
                        style = FinluxTextStyles.Caption.copy(fontSize = 12.sp),
                        color = Color.White.copy(alpha = 0.85f),
                    )
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
}

/**
 * Visual 3D Wallet & Coin Graphic
 */
@Composable
private fun PrismWallet3DIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        // Glowing halo sphere in background
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.45f), Color.Transparent),
                    center = center,
                    radius = size.minDimension / 1.5f,
                )
            )
        }

        // Layer 1: Back Card
        Surface(
            modifier = Modifier
                .size(width = 68.dp, height = 46.dp)
                .align(Alignment.TopEnd)
                .padding(top = 4.dp, end = 6.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF38BDF8).copy(alpha = 0.85f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
        ) {}

        // Layer 2: Main Wallet Front Body (Translucent glass style)
        Surface(
            modifier = Modifier
                .size(width = 82.dp, height = 58.dp)
                .align(Alignment.Center),
            shape = RoundedCornerShape(14.dp),
            color = Color.White.copy(alpha = 0.35f),
            border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.85f)),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterEnd,
            ) {
                // Wallet clasp
                Surface(
                    modifier = Modifier
                        .size(width = 24.dp, height = 20.dp)
                        .padding(end = 4.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White.copy(alpha = 0.70f),
                ) {}
            }
        }

        // Layer 3: Floating Glowing Coin
        Surface(
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.BottomEnd),
            shape = CircleShape,
            color = Color(0xFFC084FC).copy(alpha = 0.90f),
            border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.95f)),
            shadowElevation = 6.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "đ",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
            }
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
    FinluxSoftCard(
        modifier = modifier.fillMaxWidth(),
        radius = 22.dp,
        padding = 16.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Column 1: Thu tháng này
            PrismTrioMetricColumn(
                icon = Icons.Default.ArrowDownward,
                iconColor = FinluxColors.IncomeGreen,
                iconBgColor = FinluxColors.IncomeGreen.copy(alpha = 0.12f),
                title = "Thu tháng này",
                value = if (showBalance) formatVndAmount(income) else "••••",
                subtext = "— 0% so với trước",
                subtextColor = LocalFinluxTokens.current.onSurfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false),
                        onClick = onIncomeClick,
                    ),
            )

            Spacer(Modifier.width(8.dp))

            // Column 2: Chi tháng này
            PrismTrioMetricColumn(
                icon = Icons.Default.ArrowUpward,
                iconColor = FinluxColors.ExpenseRed,
                iconBgColor = FinluxColors.ExpenseRed.copy(alpha = 0.12f),
                title = "Chi tháng này",
                value = if (showBalance) formatVndAmount(expense) else "••••",
                subtext = "▲ 18,7% so với trước",
                subtextColor = FinluxColors.ExpenseRed,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false),
                        onClick = onExpenseClick,
                    ),
            )

            Spacer(Modifier.width(8.dp))

            // Column 3: Dòng tiền (ròng)
            PrismTrioMetricColumn(
                icon = Icons.Default.PieChart,
                iconColor = Color(0xFF3B82F6),
                iconBgColor = Color(0xFF3B82F6).copy(alpha = 0.12f),
                title = "Dòng tiền (ròng)",
                value = if (showBalance) (if (net < 0) "-${formatVndAmount(-net)}" else formatVndAmount(net)) else "••••",
                subtext = if (net < 0) "▼ 18,7% so với trước" else "▲ 0% so với trước",
                subtextColor = if (net < 0) FinluxColors.ExpenseRed else FinluxColors.IncomeGreen,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false),
                        onClick = onNetClick,
                    ),
            )
        }
    }
}

@Composable
private fun PrismTrioMetricColumn(
    icon: ImageVector,
    iconColor: Color,
    iconBgColor: Color,
    title: String,
    value: String,
    subtext: String,
    subtextColor: Color,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Top Icon
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(18.dp),
            )
        }

        Spacer(Modifier.height(2.dp))

        // Title
        Text(
            text = title,
            style = FinluxTextStyles.Caption.copy(fontSize = 11.sp),
            color = tokens.onSurfaceVariant,
            maxLines = 1,
        )

        // Value
        Text(
            text = value,
            style = FinluxTextStyles.CardTitle.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
            ),
            color = tokens.onSurface,
            maxLines = 1,
        )

        // Subtext (Trend)
        Text(
            text = subtext,
            style = FinluxTextStyles.MicroLabel.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = subtextColor,
            maxLines = 1,
        )
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
 * 5. "Chi tiêu theo danh mục" Section with Donut Chart and Category Shares
 */
@Composable
private fun PrismCategoryExpenseBreakdownCard(
    monthTransactions: List<FinanceTransaction>,
    categories: List<Category>,
    showBalance: Boolean,
    onViewDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val expenseTransactions = remember(monthTransactions) {
        monthTransactions.filter { it.type == TransactionType.EXPENSE }
    }
    val totalExpense = remember(expenseTransactions) {
        expenseTransactions.sumOf { it.amount.value }
    }

    // Default sample palette if no real transactions yet
    val chartPalette = listOf(
        Color(0xFF6366F1), // Indigo Purple
        Color(0xFFEC4899), // Pink
        Color(0xFFF97316), // Orange
        Color(0xFF14B8A6), // Teal
        Color(0xFF06B6D4), // Cyan
    )

    val categoryShares = remember(expenseTransactions, categories, totalExpense) {
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
            }.sortedByDescending { it.second }.take(5)
        } else {
            // Default sample items if user hasn't added expenses this month yet
            listOf(
                Triple(Category("1", "Nhà ở", CategoryType.EXPENSE, "Home", "#6366F1", true, Instant.now()), 2_200_000L, 97),
                Triple(Category("2", "Ăn uống", CategoryType.EXPENSE, "Restaurant", "#EC4899", true, Instant.now()), 65_000L, 2),
                Triple(Category("3", "Đi lại", CategoryType.EXPENSE, "DirectionsCar", "#F97316", true, Instant.now()), 0L, 0),
                Triple(Category("4", "Mua sắm", CategoryType.EXPENSE, "ShoppingBag", "#14B8A6", true, Instant.now()), 0L, 0),
                Triple(Category("5", "Giải trí", CategoryType.EXPENSE, "SportsEsports", "#06B6D4", true, Instant.now()), 0L, 0),
            )
        }
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
                text = "Chi tiêu theo danh mục",
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

        // Breakdown Card
        FinluxSoftCard(
            modifier = Modifier.fillMaxWidth(),
            radius = 22.dp,
            padding = 18.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            percentages = categoryShares.map { it.third },
                            colors = chartPalette,
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (showBalance) {
                                    if (totalExpense > 0L) formatVndAmount(totalExpense, isCompact = true) else "2,3 tr"
                                } else "••••",
                                style = FinluxTextStyles.CardTitle.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                ),
                                color = tokens.onSurface,
                            )
                            Text(
                                text = "Tổng chi",
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
                        categoryShares.forEachIndexed { index, (cat, sum, percent) ->
                            val color = chartPalette.getOrElse(index) { Color(0xFF6366F1) }
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

                // Carousel Dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(5) { idx ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(width = if (idx == 1) 14.dp else 5.dp, height = 5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (idx == 1) tokens.primary else tokens.onSurfaceVariant.copy(alpha = 0.25f)),
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
                color = Color(0xFF6366F1),
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
 * 7. Recent Transaction Item (matching screenshot)
 */
@Composable
private fun PrismRecentTransactionItem(
    transaction: FinanceTransaction,
    category: Category?,
    wallet: Wallet?,
    showBalance: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val isIncome = transaction.type == TransactionType.INCOME
    val accentColor = category?.let { colorFromHex(it.colorHex) } ?: if (isIncome) FinluxColors.IncomeGreen else tokens.primary
    val icon = category?.let { categoryIcon(it.icon) } ?: if (isIncome) Icons.Default.ArrowDownward else Icons.Default.LocalOffer

    val title = transaction.note.ifBlank {
        category?.name ?: if (isIncome) "Thu nhập" else "Chi tiêu"
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm") }
    val dateText = remember(transaction.date) {
        dateFormatter.format(transaction.date.atZone(ZoneId.systemDefault()))
    }

    val walletName = wallet?.name ?: "Ví chính"

    FinluxSoftCard(
        modifier = modifier.fillMaxWidth(),
        radius = 18.dp,
        padding = 14.dp,
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Category Icon in soft rounded square
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

            // Center Column: Note + Pill badge + Date
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = title,
                        style = FinluxTextStyles.CardTitle.copy(fontSize = 15.sp),
                        color = tokens.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                    // Sample "Nhắc nhở" badge if note contains reminder
                    if (transaction.note.contains("nhắc", ignoreCase = true) || transaction.note.contains("trọ", ignoreCase = true)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = tokens.background,
                        ) {
                            Text(
                                text = "Nhắc nhở",
                                style = FinluxTextStyles.MicroLabel.copy(fontSize = 10.sp),
                                color = tokens.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                            )
                        }
                    }
                }
                Text(
                    text = dateText,
                    style = FinluxTextStyles.Caption.copy(fontSize = 12.sp),
                    color = tokens.onSurfaceVariant,
                    maxLines = 1,
                )
            }

            Spacer(Modifier.width(8.dp))

            // Right Column: Amount + Wallet Name
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                val amountFormatted = if (isIncome) "+${formatVndAmount(transaction.amount.value)}" else "-${formatVndAmount(transaction.amount.value)}"
                Text(
                    text = if (showBalance) amountFormatted else "••••",
                    style = FinluxTextStyles.CardTitle.copy(
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    color = if (isIncome) FinluxColors.IncomeGreen else FinluxColors.ExpenseRed,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = walletName,
                        style = FinluxTextStyles.MicroLabel.copy(fontSize = 11.sp),
                        color = tokens.onSurfaceVariant,
                    )
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = tokens.onSurfaceVariant,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
    }
}
