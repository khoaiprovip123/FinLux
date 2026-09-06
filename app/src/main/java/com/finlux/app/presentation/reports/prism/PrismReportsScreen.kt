package com.finlux.app.presentation.reports.prism

import com.finlux.app.core.designsystem.theme.FinluxPalette

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import com.finlux.app.core.time.FinanceTime
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
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
import com.finlux.app.presentation.reports.WalletSpendingDetail
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

internal enum class ReportPrimaryTab(val label: String, val icon: ImageVector) {
    OVERVIEW("Tổng quan", Icons.Default.PieChart),
    CASHFLOW("Thu & Chi", Icons.Default.SwapVert),
    CATEGORIES("Danh mục", Icons.Default.GridView),
    DEEP_DIVE("Chuyên sâu", Icons.Default.Payments),
}

internal enum class DeepDiveSubTab(val label: String, val icon: ImageVector) {
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
    var selectedWalletForDetail by remember { mutableStateOf<WalletSpendingDetail?>(null) }

    Scaffold(
        topBar = {
            PrismReportsHeader(
                onFilterClick = { showPeriodPickerSheet = true },
                onExportClick = { showExportDialog = true },
            )
        },
        containerColor = FinluxPalette.Transparent,
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

            // Thanh lọc theo ví (Wallet Filter Chips)
            if (state.wallets.isNotEmpty()) {
                item {
                    PrismWalletFilterSelector(
                        wallets = state.wallets,
                        selectedWalletId = state.selectedWalletId,
                        onSelectWallet = { viewModel.selectWallet(it) },
                    )
                }
            }

            // Secondary Sub-tabs when "Chuyên sâu" is active
            if (selectedPrimaryTab == ReportPrimaryTab.DEEP_DIVE) {
                item {
                    PrismDeepDiveSubTabsRow(
                        selectedSubTab = selectedDeepDiveTab,
                        onSubTabSelected = { selectedDeepDiveTab = it },
                    )
                }
                item {
                    PrismPeriodIndicatorBanner(
                        state = state,
                        onPickPeriod = { showPeriodPickerSheet = true },
                    )
                }
            }

            when (selectedPrimaryTab) {
                ReportPrimaryTab.OVERVIEW -> {
                    item { PrismReportsHeroBanner(state, onPickMonth = { showPeriodPickerSheet = true }) }
                    item { PrismDailyStatementCard(state) }
                    item { PrismFinancialFlowBreakdownCard(state) }
                    item { PrismCumulativeMetricsCard(state) }
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
                    if (state.dailyStatements.isNotEmpty()) {
                        item { PrismDailyStatementsTable(state.dailyStatements) }
                    }
                }

                ReportPrimaryTab.CASHFLOW -> {
                    item { PrismReportsHeroBanner(state, onPickMonth = { showPeriodPickerSheet = true }) }
                    item { PrismFinancialFlowBreakdownCard(state) }
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
                    item {
                        PrismPeriodIndicatorBanner(
                            state = state,
                            onPickPeriod = { showPeriodPickerSheet = true },
                        )
                    }
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
                                item {
                                    PrismWalletSpendingDistributionCard(
                                        state = state,
                                        onSelectWallet = { walletId -> viewModel.selectWallet(walletId) },
                                    )
                                }
                                items(state.walletReportItems, key = { it.wallet.id }) { walletItem ->
                                    PrismWalletReportCard(
                                        item = walletItem,
                                        isSelected = state.selectedWalletId == walletItem.wallet.id,
                                        onClick = { selectedWalletForDetail = walletItem.spendingDetail },
                                    )
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

    var showCustomRangePicker by remember { mutableStateOf(false) }

    // Period Picker Bottom Sheet
    if (showPeriodPickerSheet) {
        PrismPeriodPickerBottomSheet(
            currentPeriod = state.period,
            availablePeriods = state.availablePeriods,
            onSelectPeriod = { period ->
                viewModel.selectPeriod(period)
                showPeriodPickerSheet = false
                if (period == ReportPeriod.CUSTOM) {
                    showCustomRangePicker = true
                }
            },
            onDismiss = { showPeriodPickerSheet = false },
            onExportClick = {
                showPeriodPickerSheet = false
                showExportDialog = true
            },
        )
    }

    // Custom Date Range Picker Dialog
    if (showCustomRangePicker) {
        val rangeState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = state.range.start.atStartOfDay(com.finlux.app.core.time.FinanceTime.VIETNAM_ZONE).toInstant().toEpochMilli(),
            initialSelectedEndDateMillis = state.range.end.atStartOfDay(com.finlux.app.core.time.FinanceTime.VIETNAM_ZONE).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showCustomRangePicker = false },
            colors = DatePickerDefaults.colors(
                containerColor = if (tokens.isDark) FinluxPalette.CFF1E1E2D else FinluxPalette.White,
            ),
            shape = RoundedCornerShape(28.dp),
            confirmButton = {
                TextButton(onClick = {
                    val start = rangeState.selectedStartDateMillis
                    val end = rangeState.selectedEndDateMillis
                    if (start != null && end != null) {
                        viewModel.setCustomRange(
                            java.time.Instant.ofEpochMilli(start).atZone(com.finlux.app.core.time.FinanceTime.VIETNAM_ZONE).toLocalDate(),
                            java.time.Instant.ofEpochMilli(end).atZone(com.finlux.app.core.time.FinanceTime.VIETNAM_ZONE).toLocalDate(),
                        )
                    }
                    showCustomRangePicker = false
                }) {
                    Text("Áp dụng", color = tokens.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomRangePicker = false }) {
                    Text("Hủy", color = tokens.onSurfaceVariant)
                }
            },
        ) {
            DateRangePicker(
                state = rangeState,
                colors = DatePickerDefaults.colors(
                    containerColor = if (tokens.isDark) FinluxPalette.CFF1E1E2D else FinluxPalette.White,
                    titleContentColor = tokens.onSurface,
                    headlineContentColor = tokens.onSurface,
                    weekdayContentColor = tokens.onSurfaceVariant,
                    subheadContentColor = tokens.onSurfaceVariant,
                    yearContentColor = tokens.onSurface,
                    currentYearContentColor = tokens.primary,
                    selectedYearContentColor = tokens.onHero,
                    selectedYearContainerColor = tokens.primary,
                    dayContentColor = tokens.onSurface,
                    selectedDayContentColor = tokens.onHero,
                    selectedDayContainerColor = tokens.primary,
                    todayContentColor = tokens.primary,
                    todayDateBorderColor = tokens.primary,
                    dayInSelectionRangeContentColor = tokens.primary,
                    dayInSelectionRangeContainerColor = tokens.primary.copy(alpha = 0.15f),
                ),
            )
        }
    }

    // Export Dialog
    if (showExportDialog) {
        ExportReportDialog(
            state = state,
            onDismiss = { showExportDialog = false },
        )
    }

    // Wallet Spending Detail Bottom Sheet
    selectedWalletForDetail?.let { detail ->
        PrismWalletDetailBottomSheet(
            detail = detail,
            isFilterActive = state.selectedWalletId == detail.wallet.id,
            onFilterWallet = {
                viewModel.selectWallet(detail.wallet.id)
                selectedWalletForDetail = null
            },
            onClearFilter = {
                viewModel.selectWallet(null)
                selectedWalletForDetail = null
            },
            onDismiss = { selectedWalletForDetail = null },
        )
    }
}

/**
 * 1. Top Header Bar
 */
