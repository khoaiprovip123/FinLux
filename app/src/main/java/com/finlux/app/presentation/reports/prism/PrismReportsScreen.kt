package com.finlux.app.presentation.reports.prism

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.component.FinluxSoftCard
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.navigation.Route
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.presentation.reports.BudgetReportItem
import com.finlux.app.presentation.reports.CategoryExpense
import com.finlux.app.presentation.reports.DebtReportItem
import com.finlux.app.presentation.reports.ExportReportDialog
import com.finlux.app.presentation.reports.GoalReportItem
import com.finlux.app.presentation.reports.ReportPeriod
import com.finlux.app.presentation.reports.ReportsUiState
import com.finlux.app.presentation.reports.ReportsViewModel
import com.finlux.app.presentation.reports.WalletReportItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private enum class ReportPrimaryTab(val label: String, val icon: ImageVector) {
    OVERVIEW("Tổng quan", Icons.Default.PieChart),
    CASHFLOW("Thu & Chi", Icons.Default.SwapVert),
    CATEGORIES("Danh mục", Icons.Default.GridView),
    DEEP_DIVE("Chuyên sâu", Icons.Default.Payments),
}

private enum class DeepDiveSubTab(val label: String, val icon: ImageVector) {
    DEBTS("Vay nợ", Icons.Default.CreditCard),
    SAVINGS("Tiết kiệm", Icons.Default.Savings),
    DEALS("Đầu tư & Cho vay", Icons.Default.TrendingUp),
    BUDGETS("Ngân sách", Icons.Default.AccountBalanceWallet),
    WALLETS("Tài sản", Icons.Default.AccountBalance),
    TREND("Xu hướng", Icons.Default.TrendingUp),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrismReportsScreen(
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tokens = LocalFinluxTokens.current

    var selectedPrimaryTab by remember { mutableStateOf(ReportPrimaryTab.OVERVIEW) }
    var selectedDeepDiveTab by remember { mutableStateOf(DeepDiveSubTab.DEBTS) }
    var selectedChartIndex by remember { mutableIntStateOf(-1) }
    var showPeriodPickerSheet by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            PrismReportsHeader(
                onFilterClick = { showPeriodPickerSheet = true },
                onExportClick = { showExportDialog = true },
            )
        },
        containerColor = Color.Transparent,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 6.dp,
                bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 4 Primary Tabs Row (Tổng quan | Thu & Chi | Danh mục | Chuyên sâu)
            item {
                PrismReportPrimaryTabsRow(
                    selectedTab = selectedPrimaryTab,
                    onTabSelected = { selectedPrimaryTab = it },
                )
            }

            // Secondary Sub-tabs when "Chuyên sâu" is active
            if (selectedPrimaryTab == ReportPrimaryTab.DEEP_DIVE) {
                item {
                    PrismDeepDiveSubTabsRow(
                        selectedSubTab = selectedDeepDiveTab,
                        onSubTabSelected = { selectedDeepDiveTab = it },
                    )
                }
            }

            when (selectedPrimaryTab) {
                ReportPrimaryTab.OVERVIEW -> {
                    item { PrismReportsHeroBanner(state, onPickMonth = { showPeriodPickerSheet = true }) }
                    item {
                        PrismOverviewMultiCards(
                            state = state,
                            onNavigateToDeepDive = { subTab ->
                                selectedPrimaryTab = ReportPrimaryTab.DEEP_DIVE
                                selectedDeepDiveTab = subTab
                            },
                        )
                    }
                    if (state.expensesByCategory.isNotEmpty()) {
                        item {
                            PrismCategoryOverviewCard(
                                state = state,
                                onViewDetail = { selectedPrimaryTab = ReportPrimaryTab.CATEGORIES },
                            )
                        }
                    }
                    item { PrismDailyAveragesRow(state) }
                }

                ReportPrimaryTab.CASHFLOW -> {
                    item { PrismReportsHeroBanner(state, onPickMonth = { showPeriodPickerSheet = true }) }
                    item {
                        PrismCashflowChartCard(
                            state = state,
                            selectedIndex = selectedChartIndex,
                            onSelectIndex = { selectedChartIndex = it },
                            onPickMonth = { showPeriodPickerSheet = true },
                        )
                    }
                    item { PrismDailyAveragesRow(state) }
                    if (state.filteredTransactions.isNotEmpty()) {
                        item { PrismLargestTransactionsCard(state) }
                    }
                }

                ReportPrimaryTab.CATEGORIES -> {
                    item { PrismCategoryOverviewCard(state = state, onViewDetail = {}) }
                    if (state.incomeByCategory.isNotEmpty()) {
                        item { PrismIncomeCategoryCard(state = state) }
                    }
                }

                ReportPrimaryTab.DEEP_DIVE -> {
                    when (selectedDeepDiveTab) {
                        DeepDiveSubTab.DEBTS -> {
                            item { PrismDebtsHeroCard(state) }
                            if (state.debtReportItems.isEmpty()) {
                                item {
                                    FinluxSoftCard(Modifier.fillMaxWidth()) {
                                        FinluxEmptyState(
                                            title = "Không có khoản vay nợ nào",
                                            description = "Quản lý thẻ tín dụng, khoản vay ngân hàng và trả góp dễ dàng tại đây.",
                                            actionLabel = "+ Thêm khoản vay / thẻ tín dụng",
                                            onActionClick = { onNavigate(Route.Debt.value) },
                                        )
                                    }
                                }
                            } else {
                                items(state.debtReportItems, key = { it.debt.id }) { debtItem ->
                                    PrismDebtItemCard(debtItem)
                                }
                            }
                        }

                        DeepDiveSubTab.SAVINGS -> {
                            item { PrismSavingsHeroCard(state) }
                            if (state.savingSpinSummary.completedCount > 0 || state.savingSpinSummary.totalSaved > 0L) {
                                item {
                                    PrismSavingSpinReportCard(
                                        summary = state.savingSpinSummary,
                                        onViewDetails = { onNavigate(Route.SavingSpinReport.value) },
                                    )
                                }
                            }
                            if (state.goalReportItems.isEmpty()) {
                                item {
                                    FinluxSoftCard(Modifier.fillMaxWidth()) {
                                        FinluxEmptyState(
                                            title = "Chưa có mục tiêu tiết kiệm",
                                            description = "Tạo các mục tiêu tài chính như Mua nhà, Mua xe, Du lịch để theo dõi tích lũy.",
                                            actionLabel = "+ Tạo mục tiêu tài chính",
                                            onActionClick = { onNavigate(Route.Goals.value) },
                                        )
                                    }
                                }
                            } else {
                                items(state.goalReportItems, key = { it.goal.id }) { goalItem ->
                                    PrismGoalItemCard(goalItem)
                                }
                            }
                        }

                        DeepDiveSubTab.DEALS -> {
                            item { PrismDealsHeroCard(state.dealsSummary) }
                            if (state.dealReportItems.isEmpty()) {
                                item {
                                    FinluxSoftCard(Modifier.fillMaxWidth()) {
                                        FinluxEmptyState(
                                            title = "Chưa có thương vụ hoặc khoản cho vay",
                                            description = "Tạo thương vụ đầu tư kinh doanh, lướt sóng hoặc quản lý tiền cho vay sinh lời tại đây.",
                                            actionLabel = "+ Tạo thương vụ / Cho vay",
                                            onActionClick = { onNavigate(Route.Deals.value) },
                                        )
                                    }
                                }
                            } else {
                                items(state.dealReportItems, key = { it.deal.id }) { dealItem ->
                                    PrismDealReportCard(dealItem)
                                }
                            }
                        }

                        DeepDiveSubTab.BUDGETS -> {
                            item { PrismBudgetsHeroCard(state) }
                            if (state.budgetReportItems.isEmpty()) {
                                item {
                                    FinluxSoftCard(Modifier.fillMaxWidth()) {
                                        FinluxEmptyState(
                                            title = "Chưa thiết lập ngân sách",
                                            description = "Đặt hạn mức chi tiêu cho từng danh mục để kiểm soát tài chính tối ưu.",
                                            actionLabel = "Thiết lập ngân sách",
                                            onActionClick = { onNavigate(Route.Budget.value) },
                                        )
                                    }
                                }
                            } else {
                                items(state.budgetReportItems, key = { it.budget.id }) { budgetItem ->
                                    PrismBudgetItemCard(budgetItem)
                                }
                            }
                        }

                        DeepDiveSubTab.WALLETS -> {
                            item { PrismWalletsHeroCard(state) }
                            if (state.walletReportItems.isEmpty()) {
                                item {
                                    FinluxSoftCard(Modifier.fillMaxWidth()) {
                                        FinluxEmptyState(
                                            title = "Chưa có ví hoạt động",
                                            description = "Thêm ví tiền mặt, tài khoản ngân hàng hoặc thẻ để xem phân bổ tài sản.",
                                            actionLabel = "+ Thêm ví",
                                            onActionClick = { onNavigate(Route.Wallets.value) },
                                        )
                                    }
                                }
                            } else {
                                items(state.walletReportItems, key = { it.wallet.id }) { walletItem ->
                                    PrismWalletReportCard(walletItem)
                                }
                            }
                        }

                        DeepDiveSubTab.TREND -> {
                            item {
                                PrismCashflowChartCard(
                                    state = state,
                                    selectedIndex = selectedChartIndex,
                                    onSelectIndex = { selectedChartIndex = it },
                                    onPickMonth = { showPeriodPickerSheet = true },
                                )
                            }
                            item { PrismDailyAveragesRow(state) }
                            item { PrismTrendAnalysisCard(state) }
                        }
                    }
                }
            }
        }
    }

    // Period Picker Bottom Sheet
    if (showPeriodPickerSheet) {
        PrismPeriodPickerBottomSheet(
            currentPeriod = state.period,
            availablePeriods = state.availablePeriods,
            onSelectPeriod = { period ->
                viewModel.selectPeriod(period)
                showPeriodPickerSheet = false
            },
            onDismiss = { showPeriodPickerSheet = false },
            onExportClick = {
                showPeriodPickerSheet = false
                showExportDialog = true
            },
        )
    }

    // Export Dialog
    if (showExportDialog) {
        ExportReportDialog(
            state = state,
            onDismiss = { showExportDialog = false },
        )
    }
}

/**
 * 1. Top Header Bar
 */
@Composable
private fun PrismReportsHeader(
    onFilterClick: () -> Unit,
    onExportClick: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Báo cáo",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                ),
                color = tokens.onSurface,
            )
            Text(
                text = "Tình hình tài chính của bạn",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Normal,
                ),
                color = Color(0xFF6B7280),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // "Bộ lọc" Pill Button
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
                border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = onFilterClick,
                    ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Bộ lọc",
                        tint = Color(0xFF5B4DFF),
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Kỳ báo cáo",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = tokens.onSurface,
                    )
                }
            }

            // Export Button
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
                border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = onExportClick,
                    ),
            ) {
                Box(
                    modifier = Modifier.padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Xuất báo cáo",
                        tint = tokens.onSurface,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

/**
 * 2. Navigation 4 Primary Tabs Row (Tổng quan | Thu & Chi | Danh mục | Chuyên sâu)
 */
@Composable
private fun PrismReportPrimaryTabsRow(
    selectedTab: ReportPrimaryTab,
    onTabSelected: (ReportPrimaryTab) -> Unit,
) {
    val tokens = LocalFinluxTokens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(tokens.surfaceSoft)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ReportPrimaryTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) tokens.primary else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = { onTabSelected(tab) },
                    ),
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 9.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        tint = if (isSelected) tokens.onHero else tokens.onSurfaceVariant,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = if (isSelected) tokens.onHero else tokens.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/**
 * 2b. Secondary Sub-tabs for "Chuyên sâu" (Vay nợ, Tiết kiệm, Ngân sách, Tài sản, Xu hướng)
 */
@Composable
private fun PrismDeepDiveSubTabsRow(
    selectedSubTab: DeepDiveSubTab,
    onSubTabSelected: (DeepDiveSubTab) -> Unit,
) {
    val tokens = LocalFinluxTokens.current

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(DeepDiveSubTab.entries) { subTab ->
            val isSelected = selectedSubTab == subTab
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) tokens.primary.copy(alpha = if (tokens.isDark) 0.28f else 0.15f) else tokens.surfaceSoft,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) tokens.primary.copy(alpha = 0.5f) else tokens.border.copy(alpha = 0.3f),
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = { onSubTabSelected(subTab) },
                    ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = subTab.icon,
                        contentDescription = null,
                        tint = if (isSelected) tokens.primary else tokens.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = subTab.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        ),
                        color = if (isSelected) tokens.primary else tokens.onSurface,
                    )
                }
            }
        }
    }
}

/**
 * 3. Hero Bento Banner (Purple/Indigo Gradient Card)
 */
@Composable
private fun PrismReportsHeroBanner(
    state: ReportsUiState,
    onPickMonth: () -> Unit,
) {
    val net = state.summary.net
    val income = state.summary.income.value
    val expense = state.summary.expense.value
    val savingRatePct = state.savingsRatePercent.coerceIn(0, 100)

    val deltaPercent = if (state.previousNet != 0L) {
        val diff = net - state.previousNet
        (((diff.toDouble() / Math.abs(state.previousNet).toDouble()) * 100.0)).roundToInt()
    } else 0

    val monthLabel = remember(state.range) {
        "Kỳ: ${state.range.start.format(DateTimeFormatter.ofPattern("dd/MM"))} - ${state.range.end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF5B4DFF).copy(alpha = 0.4f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF5B4DFF),
                            Color(0xFF6366F1),
                            Color(0xFF7C3AED),
                        ),
                    ),
                )
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left Column: Total Net & Delta
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onPickMonth),
                    ) {
                        Text(
                            text = monthLabel,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }

                    Text(
                        text = (if (net >= 0) "+" else "") + formatVndAmount(net),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp,
                        ),
                        color = Color.White,
                    )

                    // Comparison Pill / Subtitle
                    Text(
                        text = if (deltaPercent >= 0) "Tăng $deltaPercent% so với kỳ trước" else "Giảm ${-deltaPercent}% so với kỳ trước",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                        color = Color.White.copy(alpha = 0.90f),
                    )

                    Spacer(Modifier.height(4.dp))

                    // Mini Income & Expense Sub-stats
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text("Tổng thu", fontSize = 11.5.sp, color = Color.White.copy(alpha = 0.75f))
                            Text(
                                formatVndAmount(income),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4ADE80), // Mint Green
                            )
                        }
                        Column {
                            Text("Tổng chi", fontSize = 11.5.sp, color = Color.White.copy(alpha = 0.75f))
                            Text(
                                formatVndAmount(expense),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDE047), // Golden Yellow
                            )
                        }
                    }
                }

                // Right Column: Circular Savings Rate Ring
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(76.dp),
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 7.dp.toPx()
                        val arcSize = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                        // Track
                        drawArc(
                            color = Color.White.copy(alpha = 0.22f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )

                        // Progress
                        val progressSweep = (savingRatePct / 100f) * 360f
                        drawArc(
                            color = Color.White,
                            startAngle = -90f,
                            sweepAngle = progressSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "$savingRatePct%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                            ),
                            color = Color.White,
                        )
                        Text(
                            text = "Tiết kiệm",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                }
            }
        }
    }
}

/**
 * 4. Multi-dimensional Overview Cards (Net worth, Savings, Budgets, Debts)
 */
@Composable
private fun PrismOverviewMultiCards(
    state: ReportsUiState,
    onNavigateToDeepDive: (DeepDiveSubTab) -> Unit,
) {
    val tokens = LocalFinluxTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Card 1: Tài sản ròng (Net Worth)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
                border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onNavigateToDeepDive(DeepDiveSubTab.WALLETS) },
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.AccountBalance, null, tint = Color(0xFF0EA5E9), modifier = Modifier.size(16.dp))
                        Text("Tài sản ròng", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B7280))
                    }
                    Text(
                        formatVndAmount(state.totalNetWorth),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                        color = tokens.onSurface,
                    )
                    Text("${state.walletReportItems.size} ví hoạt động", fontSize = 11.sp, color = Color(0xFF0EA5E9))
                }
            }

            // Card 2: Dư nợ (Debts)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
                border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onNavigateToDeepDive(DeepDiveSubTab.DEBTS) },
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.CreditCard, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        Text("Tổng nợ còn lại", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B7280))
                    }
                    Text(
                        formatVndAmount(state.totalDebtRemaining),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                        color = if (state.totalDebtRemaining > 0) Color(0xFFEF4444) else tokens.onSurface,
                    )
                    Text("${state.debts.filter { !it.isSettled }.size} khoản nợ", fontSize = 11.sp, color = Color(0xFFEF4444))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Card 3: Ngân sách (Budgets)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
                border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onNavigateToDeepDive(DeepDiveSubTab.BUDGETS) },
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.AccountBalanceWallet, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                        Text("Hạn mức ngân sách", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B7280))
                    }
                    Text(
                        "${state.budgetUsagePercent}% đã dùng",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                        color = if (state.overBudgetCount > 0) Color(0xFFEF4444) else tokens.onSurface,
                    )
                    Text(
                        if (state.overBudgetCount > 0) "${state.overBudgetCount} danh mục vượt mức" else "Đang trong tầm kiểm soát",
                        fontSize = 11.sp,
                        color = if (state.overBudgetCount > 0) Color(0xFFEF4444) else Color(0xFF10B981),
                    )
                }
            }

            // Card 4: Tiết kiệm (Goals)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
                border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onNavigateToDeepDive(DeepDiveSubTab.SAVINGS) },
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Savings, null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                        Text("Mục tiêu tích lũy", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B7280))
                    }
                    Text(
                        formatVndAmount(state.totalGoalSaved),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                        color = tokens.onSurface,
                    )
                    Text("${(state.overallGoalProgress * 100).roundToInt()}% tiến độ mục tiêu", fontSize = 11.sp, color = Color(0xFF10B981))
                }
            }
        }

        // Row 3: Đầu tư & Cho vay (Deals)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
            border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable { onNavigateToDeepDive(DeepDiveSubTab.DEALS) },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF6366F1).copy(alpha = 0.12f)) {
                        Icon(Icons.Default.TrendingUp, null, tint = Color(0xFF6366F1), modifier = Modifier.padding(6.dp).size(18.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Vốn đầu tư & Cho vay", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B7280))
                        Text(
                            formatVndAmount(state.dealsSummary.totalActiveCapitalOutlay),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                            color = tokens.onSurface,
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (state.dealsSummary.overallRoi >= 0) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f),
                ) {
                    Text(
                        text = "ROI: ${if (state.dealsSummary.overallRoi >= 0) "+" else ""}${String.format(java.util.Locale.US, "%.1f", state.dealsSummary.overallRoi)}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.dealsSummary.overallRoi >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

/**
 * 5. Section: Category Expense Overview Card (Donut Chart & Legend List)
 */
@Composable
private fun PrismCategoryOverviewCard(
    state: ReportsUiState,
    onViewDetail: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val totalExpense = state.summary.expense.value

    val chartColors = listOf(
        Color(0xFFEF4444),
        Color(0xFF8B5CF6),
        Color(0xFF3B82F6),
        Color(0xFF10B981),
        Color(0xFFF59E0B),
        Color(0xFF06B6D4),
        Color(0xFFEC4899),
        Color(0xFF94A3B8),
    )

    val displayCategories = remember(state.expensesByCategory) { state.expensesByCategory.take(8) }
    val totalDisplayExpense = totalExpense.coerceAtLeast(1L)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
        border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Cơ cấu chi tiêu theo danh mục",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = tokens.onSurface,
                )
            }

            // Donut Chart + Center Total Label
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
            ) {
                Canvas(modifier = Modifier.size(150.dp)) {
                    val strokeWidth = 24.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                    var startAngle = -90f
                    displayCategories.forEachIndexed { index, item ->
                        val sweep = (item.amount.toFloat() / totalDisplayExpense.toFloat()) * 360f
                        val color = chartColors[index % chartColors.size]
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(diameter, diameter),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                        )
                        startAngle += sweep
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Tổng chi",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = Color(0xFF6B7280),
                    )
                    Text(
                        text = formatVndAmount(totalExpense),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = tokens.onSurface,
                    )
                }
            }

            // Category list
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                displayCategories.forEachIndexed { index, item ->
                    val color = chartColors[index % chartColors.size]
                    val pct = (item.amount.toDouble() / totalDisplayExpense.toDouble() * 100.0).roundToInt()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(color, CircleShape),
                            )
                            Text(
                                text = item.category?.name ?: "Khác",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                                color = tokens.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = formatVndAmount(item.amount),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = tokens.onSurface,
                            )
                            Text(
                                text = "$pct%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = Color(0xFF6B7280),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 6. Income by Category Card
 */
@Composable
private fun PrismIncomeCategoryCard(state: ReportsUiState) {
    val tokens = LocalFinluxTokens.current
    val totalIncome = state.summary.income.value.coerceAtLeast(1L)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
        border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Nguồn thu nhập trong kỳ",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = tokens.onSurface,
            )

            state.incomeByCategory.forEach { item ->
                val pct = (item.amount.toDouble() / totalIncome.toDouble() * 100.0).roundToInt()
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = item.category?.name ?: "Thu nhập khác",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Medium),
                            color = tokens.onSurface,
                        )
                        Text(
                            text = "+${formatVndAmount(item.amount)} ($pct%)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Bold),
                            color = Color(0xFF10B981),
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF3F4F6),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(pct / 100f)
                                .fillMaxHeight()
                                .background(Color(0xFF10B981), RoundedCornerShape(4.dp)),
                        )
                    }
                }
            }
        }
    }
}

/**
 * 7. Section: Debts & Loans Report Card
 */
@Composable
private fun PrismDebtsHeroCard(state: ReportsUiState) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFFEF4444).copy(alpha = 0.3f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFDC2626),
                            Color(0xFFE11D48),
                            Color(0xFFBE123C),
                        ),
                    ),
                )
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("BÁO CÁO DƯ NỢ & VAY NỢ", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                Text(
                    text = formatVndAmount(state.totalDebtRemaining),
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                )
                Text("Tổng dư nợ gốc cần chi trả", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))

                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Tổng nợ ban đầu", fontSize = 11.5.sp, color = Color.White.copy(alpha = 0.75f))
                        Text(formatVndAmount(state.totalDebtOriginal), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column {
                        Text("Đã thanh toán", fontSize = 11.5.sp, color = Color.White.copy(alpha = 0.75f))
                        Text(formatVndAmount(state.totalDebtPaid), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4ADE80))
                    }
                    Column {
                        Text("Lãi trả trong kỳ", fontSize = 11.5.sp, color = Color.White.copy(alpha = 0.75f))
                        Text(formatVndAmount(state.totalDebtInterestPaidInPeriod), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFDE047))
                    }
                }
            }
        }
    }
}

@Composable
private fun PrismDebtItemCard(item: DebtReportItem) {
    val tokens = LocalFinluxTokens.current
    val debt = item.debt
    val typeLabel = when (debt.type) {
        DebtType.CREDIT_CARD -> "Thẻ tín dụng"
        DebtType.BANK_LOAN -> "Vay ngân hàng"
        DebtType.PERSONAL_LOAN -> "Vay người thân"
        DebtType.INSTALLMENT -> "Trả góp"
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
        border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.12f),
                    ) {
                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = Color(0xFFEF4444),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                    Text(debt.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = tokens.onSurface)
                }

                if (debt.isSettled) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF10B981).copy(alpha = 0.12f)) {
                        Text("Đã tất toán", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                    }
                } else {
                    Text("Đến hạn ngày ${debt.dueDate}", fontSize = 12.sp, color = Color(0xFF6B7280))
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Dư nợ hiện tại", fontSize = 11.sp, color = Color(0xFF6B7280))
                    Text(formatVndAmount(item.remaining), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Hạn mức / Nợ gốc", fontSize = 11.sp, color = Color(0xFF6B7280))
                    Text(formatVndAmount(debt.totalAmount.value), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = tokens.onSurface)
                }
            }

            // Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Đã thanh toán: ${formatVndAmount(item.totalPaid)}", fontSize = 11.5.sp, color = Color(0xFF10B981))
                    Text("${(item.progress * 100).roundToInt()}%", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = tokens.onSurface)
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF3F4F6),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(item.progress)
                            .fillMaxHeight()
                            .background(Color(0xFF10B981), RoundedCornerShape(4.dp)),
                    )
                }
            }
        }
    }
}

/**
 * 8. Section: Savings & Goals Report Card
 */
@Composable
private fun PrismSavingsHeroCard(state: ReportsUiState) {
    val tokens = LocalFinluxTokens.current
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = tokens.primary.copy(alpha = 0.3f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = tokens.heroGradient,
                    ),
                )
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("BÁO CÁO TIẾT KIỆM & TÍCH LŨY", style = MaterialTheme.typography.labelSmall, color = tokens.onHeroMuted)
                Text(
                    text = formatVndAmount(state.totalGoalSaved),
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold),
                    color = tokens.onHero,
                )
                Text("Tổng đã tích lũy vào các mục tiêu", style = MaterialTheme.typography.bodySmall, color = tokens.onHeroMuted)

                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Dòng tiền còn lại", fontSize = 11.5.sp, color = tokens.onHeroMuted)
                        Text(formatVndAmount(state.unspentCashFlow), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = tokens.onHero)
                    }
                    Column {
                        Text("Tỷ lệ giữ lại", fontSize = 11.5.sp, color = tokens.onHeroMuted)
                        Text("${state.savingsRatePercent}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = tokens.onHero)
                    }
                    Column {
                        Text("Vào mục tiêu kỳ này", fontSize = 11.5.sp, color = tokens.onHeroMuted)
                        Text(formatVndAmount(state.goalContributionInPeriod), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = tokens.onHero)
                    }
                }
            }
        }
    }
}

@Composable
private fun PrismGoalItemCard(item: GoalReportItem) {
    val tokens = LocalFinluxTokens.current
    val goal = item.goal

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
        border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF10B981).copy(alpha = 0.12f)) {
                        Icon(Icons.Default.Savings, null, tint = Color(0xFF10B981), modifier = Modifier.padding(8.dp).size(20.dp))
                    }
                    Column {
                        Text(goal.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = tokens.onSurface)
                        Text(goal.category.ifBlank { "Mục tiêu tài chính" }, style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
                    }
                }
                Text("${(item.progress * 100).roundToInt()}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Đã tích lũy", fontSize = 11.sp, color = Color(0xFF6B7280))
                    Text(formatVndAmount(item.saved), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Mục tiêu cần đạt", fontSize = 11.sp, color = Color(0xFF6B7280))
                    Text(formatVndAmount(item.target), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = tokens.onSurface)
                }
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF3F4F6),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(item.progress)
                        .fillMaxHeight()
                        .background(Color(0xFF10B981), RoundedCornerShape(4.dp)),
                )
            }
        }
    }
}

@Composable
private fun PrismSavingSpinReportCard(
    summary: com.finlux.app.presentation.reports.SavingSpinSummaryReport,
    onViewDetails: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (tokens.isDark) Color(0xFF261C38) else Color(0xFFFAF5FF),
        border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFA855F7).copy(alpha = 0.2f),
                ) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = Color(0xFFA855F7),
                        modifier = Modifier
                            .padding(10.dp)
                            .size(24.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "Vòng quay tiết kiệm",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = tokens.onSurface,
                        )
                        if (summary.currentStreak > 0) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF97316).copy(alpha = 0.15f),
                            ) {
                                Text(
                                    text = "🔥 ${summary.currentStreak} ngày",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF97316),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                    Text(
                        text = "Đã tích lũy: ${formatVndAmount(summary.totalSaved)} (${summary.completedCount} lượt)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA855F7),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Xem chi tiết",
                tint = Color(0xFFA855F7),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/**
 * 8b. Section: Deals & Investment Hero Card
 */
@Composable
private fun PrismDealsHeroCard(summary: com.finlux.app.presentation.reports.DealsSummaryReport) {
    val tokens = LocalFinluxTokens.current
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF6366F1).copy(alpha = 0.3f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF4F46E5),
                            Color(0xFF6366F1),
                            Color(0xFF4338CA),
                        ),
                    ),
                )
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("BÁO CÁO ĐẦU TƯ & CHO VAY", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f),
                    ) {
                        Text(
                            text = "ROI: ${if (summary.overallRoi >= 0) "+" else ""}${String.format(java.util.Locale.US, "%.1f", summary.overallRoi)}%",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (summary.overallRoi >= 0) Color(0xFF86EFAC) else Color(0xFFFCA5A5),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
                Text(
                    text = formatVndAmount(summary.totalActiveCapitalOutlay),
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                )
                Text("Vốn đang lưu động ngoài thị trường", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))

                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Lãi ròng đã thu", fontSize = 11.5.sp, color = Color.White.copy(alpha = 0.75f))
                        Text(
                            formatVndAmount(summary.totalNetProfit),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (summary.totalNetProfit >= 0) Color(0xFF4ADE80) else Color(0xFFF87171),
                        )
                    }
                    Column {
                        Text("Gốc đã thu hồi", fontSize = 11.5.sp, color = Color.White.copy(alpha = 0.75f))
                        Text(formatVndAmount(summary.totalRecovered), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Dư nợ cho vay", fontSize = 11.5.sp, color = Color.White.copy(alpha = 0.75f))
                        Text(formatVndAmount(summary.totalLendingOutstanding), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFDE047))
                    }
                }

                // Tỷ lệ phân bổ Đầu tư vs Cho vay
                if (summary.totalHistoricalCapitalOutlay > 0L) {
                    Spacer(Modifier.height(2.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Phân bổ: ${(summary.investmentRatio * 100).roundToInt()}% Đầu tư", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                            Text("${((1f - summary.investmentRatio) * 100).roundToInt()}% Cho vay", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                        Surface(
                            shape = RoundedCornerShape(3.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                        ) {
                            Row(Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(summary.investmentRatio.coerceAtLeast(0.01f))
                                        .background(Color(0xFF38BDF8)),
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight((1f - summary.investmentRatio).coerceAtLeast(0.01f))
                                        .background(Color(0xFFFBBF24)),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrismDealReportCard(item: com.finlux.app.presentation.reports.DealReportItem) {
    val tokens = LocalFinluxTokens.current
    val deal = item.deal
    val isInvestment = deal.category == com.finlux.app.domain.model.DealCategory.INVESTMENT
    val categoryLabel = if (isInvestment) "ĐẦU TƯ" else "CHO VAY"
    val categoryColor = if (isInvestment) Color(0xFF38BDF8) else Color(0xFFFBBF24)
    val statusLabel = when (deal.status) {
        com.finlux.app.domain.model.DealStatus.ACTIVE -> "Đang chạy"
        com.finlux.app.domain.model.DealStatus.COMPLETED -> "Đã chốt"
        com.finlux.app.domain.model.DealStatus.CANCELLED -> "Đã hủy"
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
        border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = categoryColor.copy(alpha = 0.15f)) {
                        Text(
                            text = categoryLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = categoryColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        )
                    }
                    Text(
                        text = deal.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = tokens.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (item.roiPercentage >= 0) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f),
                ) {
                    Text(
                        text = "${if (item.roiPercentage >= 0) "+" else ""}${String.format(java.util.Locale.US, "%.1f", item.roiPercentage)}% ROI",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.roiPercentage >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Vốn xuất: ${formatVndAmount(item.capitalOutlay)}", fontSize = 12.sp, color = Color(0xFF6B7280))
                    Text("Còn lại: ${formatVndAmount(item.remainingCapital)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = tokens.onSurface)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Đã thu hồi: ${formatVndAmount(item.recovered)}", fontSize = 12.sp, color = Color(0xFF10B981))
                    Text("Lời ròng: ${formatVndAmount(item.netProfitLoss)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (item.netProfitLoss >= 0) Color(0xFF10B981) else Color(0xFFEF4444))
                }
            }

            // Tiến độ thu hồi vốn
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tiến độ hoàn vốn: ${(item.recoveryProgress * 100).roundToInt()}%", fontSize = 11.sp, color = Color(0xFF6B7280))
                    Text(statusLabel, fontSize = 11.sp, color = if (item.deal.status == com.finlux.app.domain.model.DealStatus.ACTIVE) Color(0xFF38BDF8) else Color(0xFF10B981))
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF3F4F6),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(item.recoveryProgress)
                            .fillMaxHeight()
                            .background(if (item.isFullyRecovered) Color(0xFF10B981) else categoryColor, RoundedCornerShape(4.dp)),
                    )
                }
            }
        }
    }
}

/**
 * 9. Section: Budgets Report Card
 */
@Composable
private fun PrismBudgetsHeroCard(state: ReportsUiState) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFFF59E0B).copy(alpha = 0.3f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFD97706),
                            Color(0xFFF59E0B),
                            Color(0xFFB45309),
                        ),
                    ),
                )
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("BÁO CÁO NGÂN SÁCH CHI TIÊU", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                Text(
                    text = "${state.budgetUsagePercent}%",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                )
                Text(
                    if (state.overBudgetCount > 0) "Có ${state.overBudgetCount} danh mục đã vượt hạn mức" else "Tất cả danh mục trong tầm kiểm soát an toàn",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f),
                )

                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Tổng hạn mức", fontSize = 11.5.sp, color = Color.White.copy(alpha = 0.75f))
                        Text(formatVndAmount(state.totalBudgetLimit), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column {
                        Text("Đã chi tiêu", fontSize = 11.5.sp, color = Color.White.copy(alpha = 0.75f))
                        Text(formatVndAmount(state.totalBudgetSpent), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFDE047))
                    }
                    Column {
                        Text("Hạn mức còn lại", fontSize = 11.5.sp, color = Color.White.copy(alpha = 0.75f))
                        Text(formatVndAmount(state.totalBudgetRemaining), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4ADE80))
                    }
                }
            }
        }
    }
}

@Composable
private fun PrismBudgetItemCard(item: BudgetReportItem) {
    val tokens = LocalFinluxTokens.current
    val progressColor = when {
        item.isOverBudget -> Color(0xFFEF4444)
        item.percent >= 0.8f -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }
    val cat = item.category
    val catColor = cat?.colorHex?.let { colorFromHex(it) } ?: tokens.primary
    val catIcon = cat?.let { categoryIcon(it.icon) } ?: Icons.Default.AccountBalanceWallet
    val statusLabel = when {
        item.isOverBudget -> "Vượt hạn mức"
        item.percent >= 0.8f -> "Cảnh báo (${(item.percent * 100).roundToInt()}%)"
        else -> "An toàn (${(item.percent * 100).roundToInt()}%)"
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
        border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(catColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = catIcon,
                            contentDescription = null,
                            tint = catColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Column {
                        Text(
                            text = cat?.name ?: item.budget.categoryId.ifBlank { "Danh mục khác" },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = tokens.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "Hạn mức: ${formatVndAmount(item.limit)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280),
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = progressColor.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = progressColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Đã chi", fontSize = 11.5.sp, color = Color(0xFF6B7280))
                    Text(
                        formatVndAmount(item.spent),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (item.isOverBudget) progressColor else tokens.onSurface,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        if (item.isOverBudget) "Vượt quá" else "Còn lại",
                        fontSize = 11.5.sp,
                        color = Color(0xFF6B7280),
                    )
                    Text(
                        formatVndAmount(if (item.isOverBudget) item.spent - item.limit else item.remaining),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isOverBudget) progressColor else Color(0xFF10B981),
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF3F4F6),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(item.percent.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(progressColor, RoundedCornerShape(4.dp)),
                )
            }
        }
    }
}

/**
 * 10. Section: Wallets & Net Worth Report Card
 */
@Composable
private fun PrismWalletsHeroCard(state: ReportsUiState) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF0EA5E9).copy(alpha = 0.3f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0284C7),
                            Color(0xFF0EA5E9),
                            Color(0xFF0369A1),
                        ),
                    ),
                )
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("BÁO CÁO TÀI SẢN & VÍ", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                Text(
                    text = formatVndAmount(state.totalNetWorth),
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                )
                Text("Tài sản ròng (Tổng số dư ví - Tổng dư nợ)", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))

                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Tổng tài sản ví", fontSize = 11.5.sp, color = Color.White.copy(alpha = 0.75f))
                        Text(formatVndAmount(state.totalAssets), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4ADE80))
                    }
                    Column {
                        Text("Tổng dư nợ", fontSize = 11.5.sp, color = Color.White.copy(alpha = 0.75f))
                        Text(formatVndAmount(state.totalDebtRemaining), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF87171))
                    }
                    Column {
                        Text("Số lượng ví", fontSize = 11.5.sp, color = Color.White.copy(alpha = 0.75f))
                        Text("${state.walletReportItems.size} ví", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun PrismWalletReportCard(item: WalletReportItem) {
    val tokens = LocalFinluxTokens.current
    val wallet = item.wallet
    val typeName = when (wallet.type) {
        WalletType.CASH -> "Tiền mặt"
        WalletType.BANK -> "Ngân hàng"
        WalletType.EWALLET -> "Ví điện tử"
        WalletType.CARD -> "Thẻ tín dụng"
        WalletType.INVESTMENT -> "Đầu tư / Tiết kiệm"
        WalletType.OTHER -> "Khác"
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
        border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(colorFromHex(wallet.colorHex), CircleShape),
                    )
                    Column {
                        Text(wallet.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = tokens.onSurface)
                        Text(typeName, style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
                    }
                }
                Text(
                    formatVndAmount(item.balance),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = tokens.onSurface,
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Chiếm ${(item.percentageOfTotal * 100).roundToInt()}% tổng tài sản",
                    fontSize = 11.5.sp,
                    color = Color(0xFF0EA5E9),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (item.incomeInPeriod > 0) Text("+${formatVndAmount(item.incomeInPeriod)}", fontSize = 11.5.sp, color = Color(0xFF10B981))
                    if (item.expenseInPeriod > 0) Text("-${formatVndAmount(item.expenseInPeriod)}", fontSize = 11.5.sp, color = Color(0xFFEF4444))
                }
            }
        }
    }
}

/**
 * 11. Largest Transactions Card
 */
@Composable
private fun PrismLargestTransactionsCard(state: ReportsUiState) {
    val tokens = LocalFinluxTokens.current
    val largest = state.largestExpense

    if (largest != null) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
            border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Giao dịch chi lớn nhất trong kỳ", fontSize = 11.5.sp, color = Color(0xFF6B7280))
                    Text(largest.note.ifBlank { "Khoản chi tiêu" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = tokens.onSurface)
                }
                Text(
                    "-${formatVndAmount(largest.amount.value)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444),
                )
            }
        }
    }
}

/**
 * 12. Trend Analysis Card
 */
@Composable
private fun PrismTrendAnalysisCard(state: ReportsUiState) {
    val tokens = LocalFinluxTokens.current
    val netChange = state.summary.net - state.previousNet
    val topExpense = state.expensesByCategory.firstOrNull()
    val insights = buildList {
        add(
            when {
                netChange > 0L -> "Dòng tiền ròng tăng ${formatVndAmount(netChange)} so với kỳ trước."
                netChange < 0L -> "Dòng tiền ròng giảm ${formatVndAmount(-netChange)} so với kỳ trước."
                else -> "Dòng tiền ròng không thay đổi so với kỳ trước."
            },
        )
        topExpense?.let {
            add("${it.category?.name ?: "Chưa phân loại"} là nhóm chi lớn nhất, chiếm ${(it.percentage * 100).roundToInt()}% tổng chi.")
        }
        if (state.goalContributionInPeriod > 0L) {
            add("Đã phân bổ ròng ${formatVndAmount(state.goalContributionInPeriod)} vào mục tiêu tài chính trong kỳ.")
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
        border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Đánh giá xu hướng tài chính", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = tokens.onSurface)
            insights.forEach { insight ->
                Text(
                    text = "• $insight",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                    color = tokens.onSurface.copy(alpha = 0.85f),
                )
            }
        }
    }
}

/**
 * 13. Cashflow Interactive Chart Card
 */
@Composable
private fun PrismCashflowChartCard(
    state: ReportsUiState,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    onPickMonth: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val cashFlowPoints = state.cashFlow

    val maxVal = remember(cashFlowPoints) {
        val peak = cashFlowPoints.maxOfOrNull { maxOf(it.income, it.expense) } ?: 1L
        if (peak <= 0L) 1L else peak
    }

    val monthLabel = remember(state.range) {
        "Kỳ ${state.range.start.format(DateTimeFormatter.ofPattern("dd/MM"))} - ${state.range.end.format(DateTimeFormatter.ofPattern("dd/MM"))}"
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
        border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Biểu đồ thu chi",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = tokens.onSurface,
                    )
                    Text(
                        text = monthLabel,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Color(0xFF6B7280),
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(8.dp).background(Color(0xFF10B981), CircleShape))
                        Text("Thu", fontSize = 11.5.sp, color = Color(0xFF6B7280))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(8.dp).background(Color(0xFF5B4DFF), CircleShape))
                        Text("Chi", fontSize = 11.5.sp, color = Color(0xFF6B7280))
                    }
                }
            }

            // Chart area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
            ) {
                if (cashFlowPoints.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Không có dữ liệu biểu đồ", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        cashFlowPoints.takeLast(14).forEachIndexed { idx, point ->
                            val isSelected = selectedIndex == idx
                            val incomeFrac = (point.income.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f)
                            val expenseFrac = (point.expense.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { onSelectIndex(if (isSelected) -1 else idx) },
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.weight(1f, fill = false),
                                ) {
                                    // Income Bar
                                    Box(
                                        modifier = Modifier
                                            .width(6.dp)
                                            .fillMaxHeight(incomeFrac.coerceAtLeast(0.04f))
                                            .background(
                                                if (isSelected) Color(0xFF10B981) else Color(0xFF10B981).copy(alpha = 0.7f),
                                                RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp),
                                            ),
                                    )
                                    // Expense Bar
                                    Box(
                                        modifier = Modifier
                                            .width(6.dp)
                                            .fillMaxHeight(expenseFrac.coerceAtLeast(0.04f))
                                            .background(
                                                if (isSelected) Color(0xFF5B4DFF) else Color(0xFF5B4DFF).copy(alpha = 0.7f),
                                                RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp),
                                            ),
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = point.date.format(DateTimeFormatter.ofPattern("dd")),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = if (isSelected) Color(0xFF5B4DFF) else Color(0xFF6B7280),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
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
 * 14. Section: Daily Averages Row (Accurate Daily Averages Calculation)
 */
@Composable
private fun PrismDailyAveragesRow(state: ReportsUiState) {
    val tokens = LocalFinluxTokens.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Average Income / Day
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
            border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
            modifier = Modifier.weight(1f),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Thu nhập TB/ngày",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                        color = Color(0xFF6B7280),
                    )
                }
                Text(
                    text = formatVndAmount(state.averageIncome),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color(0xFF10B981),
                )
            }
        }

        // Average Expense / Day
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
            border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
            modifier = Modifier.weight(1f),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Chi tiêu TB/ngày",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                        color = Color(0xFF6B7280),
                    )
                }
                Text(
                    text = formatVndAmount(state.averageExpense),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color(0xFFEF4444),
                )
            }
        }
    }
}

/**
 * 15. Period Picker Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrismPeriodPickerBottomSheet(
    currentPeriod: ReportPeriod,
    availablePeriods: List<ReportPeriod>,
    onSelectPeriod: (ReportPeriod) -> Unit,
    onDismiss: () -> Unit,
    onExportClick: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Chọn kỳ báo cáo",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = tokens.onSurface,
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                availablePeriods.forEach { period ->
                    val isSelected = currentPeriod == period

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color(0xFF5B4DFF).copy(alpha = 0.12f) else Color.Transparent,
                        border = if (isSelected) BorderStroke(1.5.dp, Color(0xFF5B4DFF)) else BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectPeriod(period) },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = period.label,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                ),
                                color = if (isSelected) Color(0xFF5B4DFF) else tokens.onSurface,
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF5B4DFF),
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
