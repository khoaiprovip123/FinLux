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
