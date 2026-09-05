package com.finlux.app.presentation.reports.classic

import com.finlux.app.presentation.reports.ExportReportDialog

import com.finlux.app.presentation.reports.*
import com.finlux.app.core.designsystem.component.formatVndAmount

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.ExpenseRed
import com.finlux.app.core.designsystem.FinluxBlue
import com.finlux.app.core.designsystem.FinluxCyan
import com.finlux.app.core.designsystem.FinluxPurple
import com.finlux.app.core.designsystem.FinluxTextSecondary
import com.finlux.app.core.designsystem.GlassTopBar
import com.finlux.app.core.designsystem.FinluxPanel
import com.finlux.app.core.designsystem.IncomeGreen
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.walletIcon
import com.finlux.app.core.navigation.Route
import com.finlux.app.presentation.components.MainBottomBar
import com.finlux.app.presentation.home.toShortVnd
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

import androidx.compose.material.icons.automirrored.filled.ArrowBack

private val ChartColors = listOf(FinluxBlue, FinluxPurple, FinluxCyan, IncomeGreen, Color(0xFFFFB347), ExpenseRed)

@Composable
fun ClassicReportsScreen(
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val selectedPeriod = viewModel.selectedPeriod.collectAsStateWithLifecycle().value
    var showRangePicker by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            GlassTopBar(
                title = { Text("Báo cáo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onBack?.invoke() ?: onNavigate(Route.Home.value) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                },
                actions = { IconButton(onClick = { showRangePicker = true }) { Icon(Icons.Default.FilterAlt, "Lọc báo cáo") } },
            )
        },
        containerColor = Color.Transparent,
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ReportPeriodSelector(
                selected = state.period,
                availablePeriods = state.availablePeriods,
            ) { option ->
                viewModel.selectPeriod(option)
                if (option == ReportPeriod.CUSTOM) showRangePicker = true
            }
            if (state.period == ReportPeriod.CUSTOM) {
                Button(onClick = { showRangePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("${state.range.start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}  →  ${state.range.end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                }
            }
            if (state.selectedWalletId != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.FilterAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Đang lọc: ${state.selectedWallet?.name ?: "Ví"}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    TextButton(
                        onClick = { viewModel.selectWallet(null) },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Xem tất cả", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
            ReportPanel {
                Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Tổng quan ${reportRangeLabel(state)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.Visibility, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = if (state.selectedWallet != null) "Số dư ví ${state.selectedWallet?.name}:" else "Tổng tiền hiện có:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatVndAmount(state.currentDisplayBalance),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(Modifier.fillMaxWidth().height(70.dp), verticalAlignment = Alignment.CenterVertically) {
                        ReportAmount("Thu nhập", state.summary.income.value, state.previousIncome, IncomeGreen, Modifier.weight(1f))
                        VerticalDivider(Modifier.height(54.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .55f))
                        ReportAmount("Chi tiêu", state.summary.expense.value, state.previousExpense, ExpenseRed, Modifier.weight(1f))
                        VerticalDivider(Modifier.height(54.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .55f))
                        ReportAmount("Tiết kiệm", state.summary.net, state.previousNet, FinluxBlue, Modifier.weight(1f))
                    }
                    if (state.selectedWallet != null && (state.totalTransferOut > 0 || state.totalTransferIn > 0)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Chuyển tiền: -${formatVndAmount(state.totalTransferOut)}" + if (state.totalTransferIn > 0) " | Nhận: +${formatVndAmount(state.totalTransferIn)}" else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFF97316)
                            )
                            Text(
                                text = "Biến động ví: ${if (state.currentWalletNetChange >= 0) "+" else ""}${formatVndAmount(state.currentWalletNetChange)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (state.currentWalletNetChange >= 0) IncomeGreen else ExpenseRed
                            )
                        }
                    }
                }
            }
            ReportPanel {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Xu hướng thu – chi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${selectedPeriod.label} ▾", Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 5.dp), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelLarge)
                    }
                    if (state.cashFlow.none { it.income > 0 || it.expense > 0 }) EmptyChartText() else CashFlowChart(state.cashFlow)
                }
            }
            ReportPanel {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Phân bổ chi tiêu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Xem chi tiết", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                    }
                    if (state.expensesByCategory.isEmpty()) EmptyChartText() else ExpenseDistribution(state.expensesByCategory)
                }
            }
            ReportPanel {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Báo cáo theo ví", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Box {
                            var showWalletDropdown by remember { mutableStateOf(false) }
                            Text(
                                text = (state.selectedWallet?.name ?: "Tất cả ví") + " ▾",
                                modifier = Modifier
                                    .background(
                                        if (state.selectedWalletId != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { showWalletDropdown = true }
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                color = if (state.selectedWalletId != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelMedium
                            )
                            androidx.compose.material3.DropdownMenu(
                                expanded = showWalletDropdown,
                                onDismissRequest = { showWalletDropdown = false }
                            ) {
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("Tất cả ví", fontWeight = if (state.selectedWalletId == null) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        viewModel.selectWallet(null)
                                        showWalletDropdown = false
                                    }
                                )
                                state.wallets.forEach { wallet ->
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text(wallet.name, fontWeight = if (state.selectedWalletId == wallet.id) FontWeight.Bold else FontWeight.Normal) },
                                        leadingIcon = {
                                            Icon(
                                                walletIcon(wallet.type),
                                                contentDescription = null,
                                                tint = colorFromHex(wallet.colorHex),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        onClick = {
                                            viewModel.selectWallet(wallet.id)
                                            showWalletDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    WalletReport(
                        items = state.walletActivity,
                        spendingDetails = state.walletSpendingDetails,
                        selectedWalletId = state.selectedWalletId,
                        onWalletClick = { walletId ->
                            viewModel.selectWallet(if (state.selectedWalletId == walletId) null else walletId)
                        }
                    )
                }
            }
            Button(
                onClick = { showExportDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
            ) { Text("Xuất báo cáo", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(96.dp))
        }
    }
    if (showExportDialog) {
        ExportReportDialog(state = state, onDismiss = { showExportDialog = false })
    }
    if (showRangePicker) {
        val tokens = com.finlux.app.core.designsystem.theme.LocalFinluxTokens.current
        val rangeState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = state.range.start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            initialSelectedEndDateMillis = state.range.end.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showRangePicker = false },
            colors = androidx.compose.material3.DatePickerDefaults.colors(
                containerColor = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
            ),
            shape = RoundedCornerShape(28.dp),
            confirmButton = {
                TextButton(onClick = {
                    val start = rangeState.selectedStartDateMillis
                    val end = rangeState.selectedEndDateMillis
                    if (start != null && end != null) {
                        viewModel.setCustomRange(
                            Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault()).toLocalDate(),
                            Instant.ofEpochMilli(end).atZone(ZoneId.systemDefault()).toLocalDate(),
                        )
                    }
                    showRangePicker = false
                }) { Text("Áp dụng", color = tokens.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showRangePicker = false }) { Text("Hủy", color = tokens.onSurfaceVariant) } },
        ) {
            DateRangePicker(
                state = rangeState,
                colors = androidx.compose.material3.DatePickerDefaults.colors(
                    containerColor = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
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
}

@Composable
private fun ReportPanel(content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit) {
    FinluxPanel(
        Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .96f),
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .72f),
        cornerRadius = 16.dp,
        content = content,
    )
}

@Composable
private fun ReportPeriodSelector(
    selected: ReportPeriod,
    availablePeriods: List<ReportPeriod>,
    onSelected: (ReportPeriod) -> Unit,
) {
    androidx.compose.foundation.lazy.LazyRow(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .72f), RoundedCornerShape(22.dp)).padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        items(
            count = availablePeriods.size,
            key = { availablePeriods[it].name },
        ) { index ->
            val option = availablePeriods[index]
            Box(
                Modifier.height(38.dp).padding(horizontal = 14.dp)
                    .background(if (selected == option) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(18.dp))
                    .clickable { onSelected(option) },
                contentAlignment = Alignment.Center,
            ) {
                Text(option.label, color = if (selected == option) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun ReportAmount(label: String, amount: Long, previous: Long, color: Color, modifier: Modifier = Modifier) {
    val change = if (previous == 0L) 0 else (((amount - previous) * 100) / kotlin.math.abs(previous)).toInt()
    Column(modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
        Text(amount.toShortVnd(), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(
            "${if (change >= 0) "▲" else "▼"} ${kotlin.math.abs(change)}%",
            color = if (change >= 0) color else ExpenseRed,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun reportRangeLabel(state: ReportsUiState): String = when (state.period) {
    ReportPeriod.TODAY -> "hôm nay (${state.range.start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))})"
    ReportPeriod.YESTERDAY -> "hôm qua (${state.range.start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))})"
    ReportPeriod.DAY -> "ngày ${state.range.start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
    ReportPeriod.WEEK -> "tuần ${state.range.start.format(DateTimeFormatter.ofPattern("dd/MM"))}–${state.range.end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
    ReportPeriod.LAST_7_DAYS -> "7 ngày (${state.range.start.format(DateTimeFormatter.ofPattern("dd/MM"))}–${state.range.end.format(DateTimeFormatter.ofPattern("dd/MM"))})"
    ReportPeriod.SALARY_CYCLE -> "kỳ ${state.range.start.format(DateTimeFormatter.ofPattern("dd/MM"))}–${state.range.end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
    ReportPeriod.MONTH -> state.range.start.format(DateTimeFormatter.ofPattern("'tháng' M, yyyy", Locale.forLanguageTag("vi-VN")))
    ReportPeriod.QUARTER -> "quý ${(state.range.start.monthValue - 1) / 3 + 1}, ${state.range.start.year}"
    ReportPeriod.YEAR -> "năm ${state.range.start.year}"
    ReportPeriod.CUSTOM -> "${state.range.start.format(DateTimeFormatter.ofPattern("dd/MM"))}–${state.range.end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
}

@Composable
private fun ExpenseDistribution(items: List<CategoryExpense>) {
    val total = items.sumOf { it.amount }.coerceAtLeast(1L)
    val visible = items.take(6)
    Row(Modifier.fillMaxWidth().height(164.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        visible.getOrNull(0)?.let { CategoryBlock(it, total, 0, Modifier.weight(1.05f).fillMaxSize()) }
        Column(Modifier.weight(1.55f).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                visible.getOrNull(1)?.let { CategoryBlock(it, total, 1, Modifier.weight(1f).fillMaxSize()) }
                visible.getOrNull(2)?.let { CategoryBlock(it, total, 2, Modifier.weight(1f).fillMaxSize()) }
            }
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (3..5).forEach { index ->
                    visible.getOrNull(index)?.let { CategoryBlock(it, total, index, Modifier.weight(1f).fillMaxSize()) }
                        ?: Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryBlock(item: CategoryExpense, total: Long, index: Int, modifier: Modifier) {
    val accent = ChartColors[index % ChartColors.size]
    val isSmall = index >= 3
    val percent = (item.amount * 100 / total).coerceAtLeast(1)
    Column(
        modifier
            .background(accent, RoundedCornerShape(10.dp))
            .padding(if (index == 0) 10.dp else if (isSmall) 6.dp else 8.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        if (isSmall) {
            Icon(
                imageVector = com.finlux.app.core.designsystem.categoryIcon(item.category?.icon.orEmpty()),
                contentDescription = item.category?.name,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        } else {
            Text(
                text = item.category?.name ?: "Khác",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                style = if (index == 0) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.labelSmall,
            )
        }
        Column {
            Text(
                text = "$percent%",
                color = Color.White,
                style = if (index == 0) MaterialTheme.typography.titleLarge else if (isSmall) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            if (!isSmall) {
                Text(
                    text = item.amount.toShortVnd(),
                    color = Color.White.copy(alpha = .88f),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun CashFlowChart(items: List<CashFlowPoint>) {
    val visible = if (items.size <= 31) items else items.takeLast(31)
    val max = visible.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(1) ?: 1
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .36f)
    val focusLineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .55f)
    val focusIndex = visible.indices.maxByOrNull { visible[it].income + visible[it].expense } ?: 0
    val focus = visible.getOrNull(focusIndex)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("— Thu nhập", color = FinluxBlue, style = MaterialTheme.typography.bodyMedium)
            Text("— Chi tiêu", color = ExpenseRed, style = MaterialTheme.typography.bodyMedium)
        }
        BoxWithConstraints(Modifier.fillMaxWidth().height(205.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                if (visible.isEmpty()) return@Canvas
                val chartTop = 14.dp.toPx()
                val chartBottom = size.height - 10.dp.toPx()
                repeat(4) { line ->
                    val y = chartTop + (chartBottom - chartTop) * line / 3f
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                }
                val step = if (visible.size <= 1) size.width else size.width / (visible.size - 1)
                fun point(index: Int, amount: Long) = Offset(index * step, chartBottom - amount.toFloat() / max * (chartBottom - chartTop) * .88f)
                fun linePath(selector: (CashFlowPoint) -> Long): Path = Path().apply {
                    visible.forEachIndexed { index, item ->
                        val point = point(index, selector(item))
                        if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
                    }
                }
                drawPath(linePath { it.income }, FinluxBlue, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                drawPath(linePath { it.expense }, ExpenseRed, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                visible.forEachIndexed { index, item ->
                    val income = point(index, item.income); val expense = point(index, item.expense)
                    drawCircle(FinluxBlue, 3.dp.toPx(), income); drawCircle(ExpenseRed, 3.dp.toPx(), expense)
                }
                if (focus != null) {
                    val x = focusIndex * step
                    drawLine(focusLineColor, Offset(x, chartTop), Offset(x, chartBottom), 1.dp.toPx())
                    drawCircle(Color.White, 5.dp.toPx(), point(focusIndex, focus.income)); drawCircle(FinluxBlue, 3.dp.toPx(), point(focusIndex, focus.income))
                    drawCircle(Color.White, 5.dp.toPx(), point(focusIndex, focus.expense)); drawCircle(ExpenseRed, 3.dp.toPx(), point(focusIndex, focus.expense))
                }
            }
            focus?.let { point ->
                val ratio = if (visible.size <= 1) 0f else focusIndex.toFloat() / (visible.size - 1)
                val popupWidth = 128.dp
                FinluxPanel(
                    modifier = Modifier.width(popupWidth).offset(x = (maxWidth * ratio - popupWidth / 2).coerceIn(0.dp, maxWidth - popupWidth), y = 8.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .98f),
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    cornerRadius = 11.dp,
                    padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 9.dp, vertical = 7.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(point.date.format(DateTimeFormatter.ofPattern("dd/MM")), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        Text("● Thu: ${point.income.toShortVnd()}", color = FinluxBlue, style = MaterialTheme.typography.labelSmall)
                        Text("● Chi: ${point.expense.toShortVnd()}", color = ExpenseRed, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            visible.firstOrNull()?.let { Text(it.date.format(DateTimeFormatter.ofPattern("dd/MM")), style = MaterialTheme.typography.bodyMedium, color = FinluxTextSecondary) }
            visible.lastOrNull()?.let { Text(it.date.format(DateTimeFormatter.ofPattern("dd/MM")), style = MaterialTheme.typography.bodyMedium, color = FinluxTextSecondary) }
        }
    }
}

@Composable
private fun WalletReport(
    items: List<WalletActivity>,
    spendingDetails: List<WalletSpendingDetail> = emptyList(),
    selectedWalletId: String? = null,
    onWalletClick: ((String) -> Unit)? = null,
) {
    if (items.isEmpty()) {
        EmptyChartText()
        return
    }
    val total = items.sumOf { it.total }.coerceAtLeast(1L)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.take(5).forEach { item ->
            val wallet = item.wallet
            val accent = wallet?.let { colorFromHex(it.colorHex) } ?: FinluxBlue
            val percent = (item.total * 100 / total).toInt()
            val isSelected = wallet != null && wallet.id == selectedWalletId
            val spending = spendingDetails.find { it.wallet.id == wallet?.id }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isSelected) {
                            Modifier
                                .background(accent.copy(alpha = .12f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        } else {
                            Modifier.padding(vertical = 3.dp)
                        }
                    )
                    .clickable(enabled = onWalletClick != null && wallet != null) {
                        wallet?.let { onWalletClick?.invoke(it.id) }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(38.dp).background(accent.copy(alpha = .16f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    wallet?.let { Icon(walletIcon(it.type), null, Modifier.size(21.dp), tint = accent) }
                }
                Column(Modifier.weight(1f).padding(horizontal = 10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(wallet?.name ?: "Ví", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        if (spending != null) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (spending.expenseInPeriod > 0L) {
                                    Text(
                                        "Chi: ${spending.expenseInPeriod.toShortVnd()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ExpenseRed
                                    )
                                }
                                if (spending.transferOutInPeriod > 0L) {
                                    Text(
                                        "Chuyển: -${spending.transferOutInPeriod.toShortVnd()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFF97316)
                                    )
                                }
                            }
                        }
                    }
                    LinearProgressIndicator(
                        progress = { percent / 100f },
                        modifier = Modifier.fillMaxWidth().height(5.dp),
                        color = accent,
                        trackColor = accent.copy(alpha = .15f),
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(item.total.toShortVnd(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text("$percent%", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun EmptyChartText() {
    Text("Chưa có dữ liệu trong khoảng thời gian này", color = FinluxTextSecondary, modifier = Modifier.padding(vertical = 34.dp))
}
