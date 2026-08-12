package com.finlux.app.presentation.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.GlassTopBar
import com.finlux.app.core.designsystem.GradientHeroCard
import com.finlux.app.core.designsystem.IncomeGreen
import com.finlux.app.core.navigation.Route
import com.finlux.app.presentation.components.MainBottomBar
import com.finlux.app.presentation.home.toShortVnd
import com.finlux.app.presentation.home.toVnd
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.ZoneId

private val ChartColors = listOf(FinluxBlue, FinluxPurple, FinluxCyan, IncomeGreen, Color(0xFFFFB347), ExpenseRed)

@Composable
fun ReportsScreen(
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit,
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val selectedPeriod = viewModel.selectedPeriod.collectAsStateWithLifecycle().value
    var showRangePicker by remember { mutableStateOf(false) }
    Scaffold(
        topBar = { GlassTopBar(title = { Text("Báo cáo", style = MaterialTheme.typography.titleLarge) }) },
        bottomBar = { MainBottomBar(Route.Reports.value, onNavigate, onAdd) },
        containerColor = Color.Transparent,
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(ReportPeriod.entries, key = { it.name }) { option ->
                    FilterChip(
                        selected = selectedPeriod == option,
                        onClick = { viewModel.selectPeriod(option); if (option == ReportPeriod.CUSTOM) showRangePicker = true },
                        label = { Text(option.label, maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = .13f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            }
            if (selectedPeriod == ReportPeriod.CUSTOM) {
                Button(onClick = { showRangePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("${state.range.start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}  →  ${state.range.end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                }
            }
            GradientHeroCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ReportAmount("Tổng thu", state.summary.income.value, Color.White)
                    ReportAmount("Tổng chi", state.summary.expense.value, Color.White)
                    ReportAmount("Còn lại", state.summary.net, Color.White)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                ReportMetric("Giao dịch", state.transactionCount.toString(), Modifier.weight(1f))
                ReportMetric("Chi TB", state.averageExpense.toShortVnd(), Modifier.weight(1f))
                ReportMetric("So kỳ trước", (state.summary.net - state.previousNet).toShortVnd(), Modifier.weight(1f))
            }
            GlassCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Chi tiêu theo danh mục", style = MaterialTheme.typography.titleMedium)
                    if (state.expensesByCategory.isEmpty()) {
                        EmptyChartText()
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ExpenseDonut(state.expensesByCategory, Modifier.weight(1f).height(190.dp))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                                state.expensesByCategory.take(6).forEachIndexed { index, item ->
                                    LegendRow(item.category?.name ?: "Khác", item.amount, ChartColors[index % ChartColors.size])
                                }
                            }
                        }
                    }
                }
            }
            GlassCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Dòng tiền Thu – Chi", style = MaterialTheme.typography.titleMedium)
                    if (state.cashFlow.none { it.income > 0 || it.expense > 0 }) EmptyChartText() else CashFlowChart(state.cashFlow)
                }
            }
            GlassCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Hoạt động theo ví", style = MaterialTheme.typography.titleMedium)
                    if (state.walletActivity.isEmpty()) EmptyChartText() else state.walletActivity.take(5).forEach { item ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(item.wallet?.name ?: "Ví", fontWeight = FontWeight.Bold)
                                Text(item.total.toShortVnd(), color = FinluxBlue)
                            }
                            Text("Thu ${item.income.toShortVnd()}  ·  Chi ${item.expense.toShortVnd()}", style = MaterialTheme.typography.bodyMedium, color = FinluxTextSecondary)
                        }
                    }
                }
            }
            Button(
                onClick = { /* UC-17 exporter remains isolated for the export sprint. */ },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
            ) { Text("Xuất báo cáo", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(18.dp))
        }
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
private fun ReportMetric(label: String, value: String, modifier: Modifier = Modifier) {
    GlassCard(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = FinluxTextSecondary)
            Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, maxLines = 1)
        }
    }
}

@Composable
private fun ReportAmount(label: String, amount: Long, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = color.copy(alpha = .76f), style = MaterialTheme.typography.bodyMedium)
        Text(amount.toShortVnd(), color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ExpenseDonut(items: List<CategoryExpense>, modifier: Modifier = Modifier) {
    val total = items.sumOf(CategoryExpense::amount).coerceAtLeast(1)
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 25.dp.toPx()
            val diameter = size.minDimension * .68f
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            var start = -90f
            items.forEachIndexed { index, item ->
                val sweep = item.amount.toFloat() / total * 360f
                drawArc(
                    color = ChartColors[index % ChartColors.size],
                    startAngle = start,
                    sweepAngle = (sweep - 2f).coerceAtLeast(1f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
                start += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(total.toShortVnd(), fontWeight = FontWeight.Bold)
            Text("Tổng chi", style = MaterialTheme.typography.bodyMedium, color = FinluxTextSecondary)
        }
    }
}

@Composable
private fun LegendRow(label: String, amount: Long, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).background(color, CircleShape))
        Text(label, Modifier.weight(1f).padding(start = 7.dp), style = MaterialTheme.typography.bodyMedium, maxLines = 1)
        Text(amount.toShortVnd(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CashFlowChart(items: List<CashFlowPoint>) {
    val visible = items.takeLast(12)
    val max = visible.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(1) ?: 1
    Column {
        Canvas(Modifier.fillMaxWidth().height(170.dp)) {
            val slot = size.width / visible.size.coerceAtLeast(1)
            visible.forEachIndexed { index, item ->
                val incomeHeight = item.income.toFloat() / max * size.height * .82f
                val expenseHeight = item.expense.toFloat() / max * size.height * .82f
                drawRoundRect(
                    color = IncomeGreen,
                    topLeft = Offset(index * slot + slot * .15f, size.height - incomeHeight),
                    size = Size(slot * .28f, incomeHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
                )
                drawRoundRect(
                    color = ExpenseRed,
                    topLeft = Offset(index * slot + slot * .52f, size.height - expenseHeight),
                    size = Size(slot * .28f, expenseHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("● Thu", color = IncomeGreen, style = MaterialTheme.typography.bodyMedium)
            Text("● Chi", color = ExpenseRed, style = MaterialTheme.typography.bodyMedium)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            visible.firstOrNull()?.let { Text(it.date.format(DateTimeFormatter.ofPattern("dd/MM")), style = MaterialTheme.typography.bodyMedium, color = FinluxTextSecondary) }
            visible.lastOrNull()?.let { Text(it.date.format(DateTimeFormatter.ofPattern("dd/MM")), style = MaterialTheme.typography.bodyMedium, color = FinluxTextSecondary) }
        }
    }
}

@Composable
private fun EmptyChartText() {
    Text("Chưa có dữ liệu trong khoảng thời gian này", color = FinluxTextSecondary, modifier = Modifier.padding(vertical = 34.dp))
}
