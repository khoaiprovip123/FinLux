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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TrendingUp
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
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.component.FinluxSoftCard
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.navigation.Route
import com.finlux.app.presentation.components.MainBottomBar
import com.finlux.app.presentation.reports.CategoryExpense
import com.finlux.app.presentation.reports.ExportReportDialog
import com.finlux.app.presentation.reports.ReportPeriod
import com.finlux.app.presentation.reports.ReportsUiState
import com.finlux.app.presentation.reports.ReportsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private enum class ReportMainTab(val label: String, val icon: ImageVector) {
    OVERVIEW("Tổng quan", Icons.Default.PieChart),
    CASHFLOW("Thu chi", Icons.Default.SwapVert),
    CATEGORIES("Danh mục", Icons.Default.GridView),
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
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val tokens = LocalFinluxTokens.current

    var selectedTab by remember { mutableStateOf(ReportMainTab.OVERVIEW) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showPeriodPickerSheet by remember { mutableStateOf(false) }
    var selectedChartIndex by remember(state.range) { mutableIntStateOf(-1) }

    Scaffold(
        topBar = {
            // 1. Top Screen Header: "Báo cáo" + "Tình hình tài chính của bạn" + Nút "Bộ lọc"
            PrismReportsHeader(
                onFilterClick = { showPeriodPickerSheet = true },
                onExportClick = { showExportDialog = true },
            )
        },
        bottomBar = {
            MainBottomBar(Route.Reports.value, onNavigate, onAdd)
        },
        containerColor = tokens.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 6.dp,
                bottom = 120.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 2. Navigation Filter Tabs (Tổng quan, Thu chi, Danh mục, Xu hướng)
            item {
                PrismReportTabsRow(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                )
            }

            if (state.filteredTransactions.isEmpty()) {
                item {
                    FinluxSoftCard(Modifier.fillMaxWidth()) {
                        FinluxEmptyState(
                            title = "Chưa có dữ liệu báo cáo",
                            description = "Hãy thêm giao dịch trong kỳ đã chọn để xem phân tích chính xác.",
                            actionLabel = "Thêm giao dịch",
                            onActionClick = onAdd,
                        )
                    }
                }
            } else {
                when (selectedTab) {
                    ReportMainTab.OVERVIEW -> {
                        item { PrismReportsHeroBanner(state, onPickMonth = { showPeriodPickerSheet = true }) }
                        item {
                            PrismCategoryOverviewCard(
                                state = state,
                                onViewDetail = { selectedTab = ReportMainTab.CATEGORIES },
                            )
                        }
                        item { PrismDailyAveragesRow(state) }
                    }
                    ReportMainTab.CASHFLOW -> {
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
                    }
                    ReportMainTab.CATEGORIES -> item {
                        PrismCategoryOverviewCard(state = state, onViewDetail = {})
                    }
                    ReportMainTab.TREND -> {
                        item {
                            PrismCashflowChartCard(
                                state = state,
                                selectedIndex = selectedChartIndex,
                                onSelectIndex = { selectedChartIndex = it },
                                onPickMonth = { showPeriodPickerSheet = true },
                            )
                        }
                        item { PrismDailyAveragesRow(state) }
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
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Bộ lọc",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = Color(0xFF4F46E5),
                    )
                }
            }

            // Quick Export Icon Button
            Surface(
                shape = CircleShape,
                color = tokens.surfaceSoft,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = onExportClick,
                    ),
            ) {
                Box(contentAlignment = Alignment.Center) {
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
 * 2. Navigation Segmented Tabs (Tổng quan, Thu chi, Danh mục, Xu hướng)
 */
@Composable
private fun PrismReportTabsRow(
    selectedTab: ReportMainTab,
    onTabSelected: (ReportMainTab) -> Unit,
) {
    val tokens = LocalFinluxTokens.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ReportMainTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) Color(0xFF5B4DFF) else if (tokens.isDark) Color(0xFF1E1E2D) else Color(0xFFF3F4F6),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = { onTabSelected(tab) },
                    ),
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else Color(0xFF6B7280),
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = if (isSelected) Color.White else Color(0xFF6B7280),
                        maxLines = 1,
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

    // Calculate saving rate
    val savingRatePct = if (income > 0L) {
        (((income - expense).toDouble() / income.toDouble()) * 100.0).coerceIn(0.0, 100.0).roundToInt()
    } else 0

    // Compare with previous period
    val deltaPercent = if (state.previousNet != 0L) {
        val diff = net - state.previousNet
        (((diff.toDouble() / Math.abs(state.previousNet).toDouble()) * 100.0)).roundToInt()
    } else 18 // Fallback aesthetic

    val monthLabel = remember(state.range) {
        "Tháng ${state.range.start.format(DateTimeFormatter.ofPattern("MM/yyyy"))}"
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
                            text = "Tổng thu - chi ($monthLabel)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 12.5.sp,
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
                        text = if (deltaPercent >= 0) "Tăng $deltaPercent% so với tháng trước" else "Giảm ${-deltaPercent}% so với tháng trước",
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
 * 4. Section: Category Overview Card (Donut Chart & Legend List)
 */
@Composable
private fun PrismCategoryOverviewCard(
    state: ReportsUiState,
    onViewDetail: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val totalExpense = state.summary.expense.value

    // Palette for donut chart segments
    val chartColors = listOf(
        Color(0xFFEF4444), // Ăn uống (Red)
        Color(0xFF8B5CF6), // Tiền nhà (Purple)
        Color(0xFF3B82F6), // Di chuyển (Blue)
        Color(0xFF10B981), // Mua sắm (Green)
        Color(0xFFF59E0B), // Giải trí (Orange/Amber)
        Color(0xFF94A3B8), // Khác (Slate)
    )

    // Build top categories data
    val displayCategories = remember(state.expensesByCategory) { state.expensesByCategory.take(6) }
    val totalDisplayExpense = totalExpense.coerceAtLeast(1L)

    if (displayCategories.isEmpty()) {
        FinluxSoftCard(Modifier.fillMaxWidth()) {
            FinluxEmptyState(
                title = "Chưa có khoản chi",
                description = "Không có dữ liệu danh mục trong kỳ đã chọn.",
            )
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Tổng quan theo danh mục",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = tokens.onSurface,
            )

            Text(
                text = "Xem chi tiết >",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = Color(0xFF4F46E5),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onViewDetail)
                    .padding(4.dp),
            )
        }

        // Card Container
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
            border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFF3F4F6)),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Left: Donut Chart Canvas
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(130.dp),
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 24.dp.toPx()
                        val arcSize = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                        var currentAngle = -90f
                        displayCategories.forEachIndexed { index, item ->
                            val sweep = ((item.amount.toDouble() / totalDisplayExpense.toDouble()) * 360.0).toFloat()
                            val color = chartColors.getOrElse(index) { Color.Gray }

                            drawArc(
                                color = color,
                                startAngle = currentAngle,
                                sweepAngle = sweep.coerceAtLeast(4f),
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcSize, arcSize),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                            )
                            currentAngle += sweep
                        }
                    }

                    // Donut Hole Center Text
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = formatVndAmount(totalDisplayExpense),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                            ),
                            color = tokens.onSurface,
                            maxLines = 1,
                        )
                        Text(
                            text = "Tổng chi",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                            color = Color(0xFF6B7280),
                        )
                    }
                }

                // Right: Categories Legend Breakdown
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val defaultLabels = listOf("Ăn uống", "Tiền nhà", "Di chuyển", "Mua sắm", "Giải trí", "Khác")
                    val defaultIcons = listOf("restaurant", "home", "directions_car", "shopping_bag", "games", "auto_awesome")

                    displayCategories.forEachIndexed { index, item ->
                        val catName = item.category?.name ?: defaultLabels.getOrElse(index) { "Danh mục" }
                        val iconKey = item.category?.icon ?: defaultIcons.getOrElse(index) { "category" }
                        val color = chartColors.getOrElse(index) { Color.Gray }
                        val pct = ((item.amount.toDouble() / totalDisplayExpense.toDouble()) * 100.0)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = color.copy(alpha = 0.15f),
                                    modifier = Modifier.size(24.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = categoryIcon(iconKey),
                                            contentDescription = null,
                                            tint = color,
                                            modifier = Modifier.size(13.dp),
                                        )
                                    }
                                }
                                Text(
                                    text = catName,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                    ),
                                    color = tokens.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = formatVndAmount(item.amount),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = tokens.onSurface,
                                )
                                Text(
                                    text = String.format("%.1f%%", pct),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Normal,
                                    ),
                                    color = Color(0xFF6B7280),
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.End,
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
 * 5. Section: Cashflow Bar Chart (Biểu đồ thu chi)
 */
internal data class CashFlowBucket(
    val start: LocalDate,
    val end: LocalDate,
    val income: Long,
    val expense: Long,
)

internal fun cashFlowBuckets(
    points: List<com.finlux.app.presentation.reports.CashFlowPoint>,
    maximumBuckets: Int = 31,
): List<CashFlowBucket> {
    if (points.isEmpty()) return emptyList()
    val chunkSize = kotlin.math.ceil(points.size.toDouble() / maximumBuckets.toDouble()).toInt().coerceAtLeast(1)
    return points.chunked(chunkSize).map { chunk ->
        CashFlowBucket(
            start = chunk.first().date,
            end = chunk.last().date,
            income = chunk.sumOf { it.income },
            expense = chunk.sumOf { it.expense },
        )
    }
}

private fun compactChartAmount(value: Long): String = when {
    value >= 1_000_000_000L -> String.format("%.1ftỷ", value / 1_000_000_000.0)
    value >= 1_000_000L -> String.format("%.1ftr", value / 1_000_000.0)
    value >= 1_000L -> String.format("%.0fk", value / 1_000.0)
    else -> value.toString()
}

private fun CashFlowBucket.label(): String =
    if (start == end) start.format(DateTimeFormatter.ofPattern("d/M"))
    else "${start.format(DateTimeFormatter.ofPattern("d/M"))}–${end.format(DateTimeFormatter.ofPattern("d/M"))}"

@Composable
private fun PrismCashflowChartCard(
    state: ReportsUiState,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    onPickMonth: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val buckets = remember(state.cashFlow) { cashFlowBuckets(state.cashFlow) }
    if (buckets.isEmpty()) return

    val effectiveSelectedIndex = selectedIndex
        .takeIf { it in buckets.indices }
        ?: buckets.indexOfLast { it.income > 0L || it.expense > 0L }.coerceAtLeast(0)
    val selectedBucket = buckets[effectiveSelectedIndex]
    val maximumValue = buckets.maxOf { maxOf(it.income, it.expense) }.coerceAtLeast(1L)
    val axisMaximum = ((maximumValue * 1.15).toLong()).coerceAtLeast(1L)
    val rangeText = remember(state.range) {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        "${state.range.start.format(formatter)} – ${state.range.end.format(formatter)}"
    }
    val labelIndexes = remember(buckets) {
        val last = buckets.lastIndex
        (0..4).map { step -> ((last * step) / 4f).roundToInt().coerceIn(0, last) }.distinct()
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Biểu đồ thu chi",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                color = tokens.onSurface,
            )
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = tokens.surfaceSoft,
                border = BorderStroke(1.dp, tokens.onSurface.copy(alpha = tokens.borderAlpha)),
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onPickMonth),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(Icons.Default.CalendarMonth, null, tint = tokens.primary, modifier = Modifier.size(15.dp))
                    Text(rangeText, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = tokens.onSurface)
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = tokens.onSurfaceVariant, modifier = Modifier.size(15.dp))
                }
            }
        }

        FinluxSoftCard(
            modifier = Modifier.fillMaxWidth(),
            radius = 22.dp,
            padding = 16.dp,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    listOf("Thu" to Color(0xFF10B981), "Chi" to Color(0xFFEF4444)).forEachIndexed { index, (label, color) ->
                        if (index > 0) Spacer(Modifier.width(14.dp))
                        Surface(shape = CircleShape, color = color, modifier = Modifier.size(8.dp)) {}
                        Spacer(Modifier.width(4.dp))
                        Text(label, fontSize = 11.5.sp, color = tokens.onSurfaceVariant)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp),
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(buckets) {
                                detectTapGestures { tap ->
                                    val left = 34.dp.toPx()
                                    val usableWidth = (size.width - left).coerceAtLeast(1f)
                                    val index = (((tap.x - left) / usableWidth) * buckets.size)
                                        .toInt()
                                        .coerceIn(buckets.indices)
                                    onSelectIndex(index)
                                }
                            },
                    ) {
                        val chartLeft = 34.dp.toPx()
                        val chartTop = 36.dp.toPx()
                        val chartBottom = size.height - 26.dp.toPx()
                        val chartHeight = (chartBottom - chartTop).coerceAtLeast(1f)
                        val chartWidth = (size.width - chartLeft).coerceAtLeast(1f)
                        val slotWidth = chartWidth / buckets.size
                        val barWidth = (slotWidth * 0.28f).coerceIn(2.dp.toPx(), 10.dp.toPx())
                        val gridColor = tokens.onSurface.copy(alpha = if (tokens.isDark) 0.12f else 0.08f)
                        val selectedCenter = chartLeft + (effectiveSelectedIndex + 0.5f) * slotWidth

                        for (line in 0..4) {
                            val y = chartBottom - (line / 4f) * chartHeight
                            drawLine(
                                color = gridColor,
                                start = Offset(chartLeft, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
                            )
                        }

                        buckets.forEachIndexed { index, bucket ->
                            val center = chartLeft + (index + 0.5f) * slotWidth
                            val incomeHeight = (bucket.income.toFloat() / axisMaximum.toFloat()) * chartHeight
                            val expenseHeight = (bucket.expense.toFloat() / axisMaximum.toFloat()) * chartHeight
                            drawRoundRect(
                                color = Color(0xFF10B981),
                                topLeft = Offset(center - barWidth - 1.dp.toPx(), chartBottom - incomeHeight),
                                size = Size(barWidth, incomeHeight.coerceAtLeast(if (bucket.income > 0L) 2.dp.toPx() else 0f)),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
                            )
                            drawRoundRect(
                                color = Color(0xFFEF4444),
                                topLeft = Offset(center + 1.dp.toPx(), chartBottom - expenseHeight),
                                size = Size(barWidth, expenseHeight.coerceAtLeast(if (bucket.expense > 0L) 2.dp.toPx() else 0f)),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
                            )
                        }

                        drawLine(
                            color = tokens.primary.copy(alpha = 0.55f),
                            start = Offset(selectedCenter, chartTop - 4.dp.toPx()),
                            end = Offset(selectedCenter, chartBottom),
                            strokeWidth = 1.2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f)),
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(top = 28.dp, bottom = 26.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        listOf(axisMaximum, axisMaximum * 3 / 4, axisMaximum / 2, axisMaximum / 4, 0L).forEach {
                            Text(compactChartAmount(it), fontSize = 9.5.sp, color = tokens.onSurfaceVariant)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = tokens.surface,
                        border = BorderStroke(1.dp, tokens.onSurface.copy(alpha = tokens.borderAlpha)),
                        shadowElevation = 4.dp,
                        modifier = Modifier.align(Alignment.TopCenter),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(1.dp),
                        ) {
                            Text(selectedBucket.label(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tokens.onSurface)
                            Text(
                                "Thu: ${formatVndAmount(selectedBucket.income)}",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF10B981),
                            )
                            Text(
                                "Chi: ${formatVndAmount(selectedBucket.expense)}",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFEF4444),
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(start = 34.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        labelIndexes.forEach { index ->
                            val selected = index == effectiveSelectedIndex
                            Text(
                                text = buckets[index].label(),
                                fontSize = 9.5.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) tokens.primary else tokens.onSurfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSelectIndex(index) }
                                    .padding(horizontal = 3.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
internal fun periodChangeLabel(current: Long, previous: Long): Pair<String, Boolean?> {
    if (previous <= 0L) return "Chưa có dữ liệu kỳ trước" to null
    val change = (((current - previous) * 100.0) / previous.toDouble()).roundToInt()
    val prefix = if (change > 0) "+" else ""
    val arrow = if (change >= 0) "↗" else "↘"
    return "$prefix$change% so với kỳ trước $arrow" to (change >= 0)
}

@Composable
private fun PrismDailyAveragesRow(state: ReportsUiState) {
    val tokens = LocalFinluxTokens.current

    val daysInPeriod = (state.range.end.toEpochDay() - state.range.start.toEpochDay() + 1).coerceAtLeast(1)
    val avgIncome = state.summary.income.value / daysInPeriod
    val avgExpense = state.summary.expense.value / daysInPeriod
    val previousAvgIncome = state.previousIncome / daysInPeriod
    val previousAvgExpense = state.previousExpense / daysInPeriod
    val incomeTrend = periodChangeLabel(avgIncome, previousAvgIncome)
    val expenseTrend = periodChangeLabel(avgExpense, previousAvgExpense)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Left Card: Trung bình thu/ngày (Soft Mint)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (tokens.isDark) Color(0xFF064E3B).copy(alpha = 0.35f) else Color(0xFFECFDF5),
            border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
            modifier = Modifier.weight(1f),
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.20f),
                    modifier = Modifier.size(38.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Trung bình thu/ngày",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = Color(0xFF6B7280),
                    )
                    Text(
                        text = formatVndAmount(avgIncome),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                        ),
                        color = tokens.onSurface,
                    )
                    Text(
                        text = incomeTrend.first,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = when (incomeTrend.second) {
                            true -> Color(0xFF059669)
                            false -> Color(0xFFDC2626)
                            null -> tokens.onSurfaceVariant
                        },
                    )
                }
            }
        }

        // Right Card: Trung bình chi/ngày (Soft Coral)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (tokens.isDark) Color(0xFF881337).copy(alpha = 0.35f) else Color(0xFFFFF1F2),
            border = BorderStroke(1.dp, Color(0xFFFECDD3)),
            modifier = Modifier.weight(1f),
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.20f),
                    modifier = Modifier.size(38.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Trung bình chi/ngày",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = Color(0xFF6B7280),
                    )
                    Text(
                        text = formatVndAmount(avgExpense),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                        ),
                        color = tokens.onSurface,
                    )
                    Text(
                        text = expenseTrend.first,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = when (expenseTrend.second) {
                            true -> Color(0xFFDC2626)
                            false -> Color(0xFF059669)
                            null -> tokens.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}

/**
 * Period Selection Modal Bottom Sheet
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
        containerColor = tokens.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Chọn kỳ báo cáo",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )

            availablePeriods.forEach { option ->
                val isSelected = currentPeriod == option
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) Color(0xFF5B4DFF).copy(alpha = 0.10f) else tokens.surfaceSoft,
                    border = if (isSelected) BorderStroke(1.5.dp, Color(0xFF5B4DFF)) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectPeriod(option) },
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = option.label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 15.sp,
                            color = if (isSelected) Color(0xFF5B4DFF) else tokens.onSurface,
                        )
                        if (isSelected) {
                            Icon(Icons.Default.Check, null, tint = Color(0xFF5B4DFF))
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Action to Export Report
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFEEF2FF),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onExportClick),
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 13.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Xuất file Excel / PDF",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Color(0xFF4F46E5),
                    )
                }
            }
        }
    }
}
