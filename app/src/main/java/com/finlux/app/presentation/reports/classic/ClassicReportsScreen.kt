package com.finlux.app.presentation.reports.classic

import com.finlux.app.presentation.reports.ExportReportDialog

import com.finlux.app.presentation.reports.*

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
            ReportPeriodSelector(selectedPeriod) { option ->
                viewModel.selectPeriod(option)
                if (option == ReportPeriod.CUSTOM) showRangePicker = true
            }
            if (selectedPeriod == ReportPeriod.CUSTOM) {
                Button(onClick = { showRangePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("${state.range.start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}  →  ${state.range.end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                }
            }
            ReportPanel {
                Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Tổng quan ${reportRangeLabel(state)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.Visibility, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
                    }
                    Row(Modifier.fillMaxWidth().height(70.dp), verticalAlignment = Alignment.CenterVertically) {
                        ReportAmount("Thu nhập", state.summary.income.value, state.previousIncome, IncomeGreen, Modifier.weight(1f))
                        VerticalDivider(Modifier.height(54.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .55f))
                        ReportAmount("Chi tiêu", state.summary.expense.value, state.previousExpense, ExpenseRed, Modifier.weight(1f))
                        VerticalDivider(Modifier.height(54.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .55f))
                        ReportAmount("Tiết kiệm", state.summary.net, state.previousNet, FinluxBlue, Modifier.weight(1f))
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
                        Text("Tất cả ví ▾", Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelMedium)
                    }
                    WalletReport(state.walletActivity)
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
        val rangeState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = state.range.start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            initialSelectedEndDateMillis = state.range.end.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showRangePicker = false },
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
                }) { Text("Áp dụng") }
            },
            dismissButton = { TextButton(onClick = { showRangePicker = false }) { Text("Hủy") } },
        ) { DateRangePicker(rangeState) }
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
private fun ReportPeriodSelector(selected: ReportPeriod, onSelected: (ReportPeriod) -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .72f), RoundedCornerShape(22.dp)).padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        ReportPeriod.entries.forEach { option ->
            Box(
                Modifier.weight(1f).height(38.dp)
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
    val visible = items.takeLast(20)
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
private fun WalletReport(items: List<WalletActivity>) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(38.dp).background(accent.copy(alpha = .16f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    wallet?.let { Icon(walletIcon(it.type), null, Modifier.size(21.dp), tint = accent) }
                }
                Column(Modifier.weight(1f).padding(horizontal = 10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(wallet?.name ?: "Ví", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
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
