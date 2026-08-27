package com.finlux.app.presentation.expense

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
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
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.GlassTopBar
import com.finlux.app.core.designsystem.FinluxPanel
import com.finlux.app.core.designsystem.IncomeGreen
import com.finlux.app.core.designsystem.WarningAmber
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.FinluxStyleBackdrop
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.presentation.home.toShortVnd
import com.finlux.app.presentation.home.toVnd
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ExpenseChartColors = listOf(
    FinluxColors.ExpenseRed,
    FinluxColors.WarningAmber,
    FinluxColors.PrimaryBlue,
    FinluxColors.PrimaryViolet,
    FinluxColors.PrimaryCyan,
    FinluxColors.IncomeGreen,
    FinluxColors.NeutralGray,
)

@Composable
fun ExpenseScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onAddExpense: () -> Unit,
    onSelectTransaction: ((FinanceTransaction) -> Unit)? = null,
    onActionTransaction: ((FinanceTransaction) -> Unit)? = null,
    onEditTransaction: ((FinanceTransaction) -> Unit)? = null,
    viewModel: ExpenseViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val tokens = LocalFinluxTokens.current

    Box(modifier = Modifier.fillMaxSize()) {
        FinluxStyleBackdrop(Modifier.fillMaxSize())

        Scaffold(
            topBar = {
                GlassTopBar(
                    title = { Text("Chi tiêu", fontWeight = FontWeight.Bold) },
                    navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") } },
                    actions = {
                        TextButton(onAddExpense) {
                            Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                            Text("Thêm", Modifier.padding(start = 3.dp), fontWeight = FontWeight.Bold)
                        }
                    },
                )
            },
            containerColor = Color.Transparent,
        ) { padding ->
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                item { MonthPicker(state.month.toString(), viewModel::previousMonth, viewModel::nextMonth, state.month < java.time.YearMonth.now()) }
                item { ExpenseHero(state.total, state.changePercent) }
                item { ExpenseCategoryCard(state) }
                item { DailyExpenseCard(state.dailyStats) }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Giao dịch gần đây", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Xem tất cả", color = tokens.primary, style = MaterialTheme.typography.labelLarge)
                    }
                }
                if (state.transactions.isEmpty()) {
                    item {
                        FinluxEmptyState(
                            title = "Chưa có chi tiêu trong tháng này",
                            description = "Chạm vào nút bên dưới để thêm khoản chi tiêu mới.",
                            icon = Icons.Default.TrendingDown,
                            actionLabel = "+ Thêm chi tiêu",
                            onActionClick = onAddExpense,
                            modifier = Modifier.padding(top = 16.dp),
                        )
                    }
                } else {
                    items(state.transactions.take(12), key = { it.id }) { transaction ->
                        val category = state.categories[transaction.categoryId]
                        val accent = category?.let { colorFromHex(it.colorHex) } ?: FinluxColors.ExpenseRed
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onSelectTransaction?.invoke(transaction) },
                            onLongClick = { onActionTransaction?.invoke(transaction) ?: onEditTransaction?.invoke(transaction) },
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(42.dp).background(accent.copy(alpha = .14f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Icon(category?.let { categoryIcon(it.icon) } ?: Icons.Default.TrendingDown, null, tint = accent) }
                                Column(Modifier.weight(1f).padding(start = 12.dp, end = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = transaction.note.ifBlank { category?.name ?: "Chi tiêu" },
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = "${category?.name ?: "Khác"} · ${transaction.date.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = tokens.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    )
                                }
                                Text("-${transaction.amount.value.toVnd()}", color = FinluxColors.ExpenseRed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthPicker(month: String, previous: () -> Unit, next: () -> Unit, canNext: Boolean) {
    val parsed = java.time.YearMonth.parse(month)
    FinluxPanel(Modifier.fillMaxWidth().height(48.dp), cornerRadius = 24.dp, padding = PaddingValues(horizontal = 5.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(previous) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Tháng trước") }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Text(parsed.format(DateTimeFormatter.ofPattern("'Tháng' M, yyyy", Locale.forLanguageTag("vi-VN"))), Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
            }
            IconButton(next, enabled = canNext) { Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, "Tháng sau") }
        }
    }
}

@Composable
private fun ExpenseHero(total: Long, changePercent: Int) {
    FinluxPanel(
        modifier = Modifier.fillMaxWidth().height(128.dp),
        containerColor = Color.Transparent,
        borderColor = Color.White.copy(alpha = .30f),
        cornerRadius = 20.dp,
        padding = PaddingValues(0.dp),
    ) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.linearGradient(listOf(Color(0xFFFF405B), Color(0xFFFF6680), Color(0xFFFF7B91))),
                RoundedCornerShape(20.dp),
            ).padding(16.dp),
        ) {
            Column(Modifier.align(Alignment.CenterStart), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Tổng chi", color = Color.White.copy(alpha = .90f))
                Text(total.toVnd(), color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("${if (changePercent <= 0) "▼" else "▲"} ${kotlin.math.abs(changePercent)}% so với tháng trước", color = Color.White.copy(alpha = .92f), style = MaterialTheme.typography.bodyMedium)
            }
            Box(
                Modifier.align(Alignment.CenterEnd).size(68.dp).background(Color.White.copy(alpha = .15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${if (changePercent > 0) "+" else ""}$changePercent%", color = Color.White, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.TrendingDown, null, Modifier.size(19.dp), tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ExpenseCategoryCard(state: ExpenseUiState) {
    val total = state.total.coerceAtLeast(1L)
    FinluxPanel(Modifier.fillMaxWidth(), padding = PaddingValues(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Chi tiêu theo danh mục", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (state.categoryStats.isEmpty()) Text("Chưa có dữ liệu", color = MaterialTheme.colorScheme.onSurfaceVariant)
            else Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(142.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        val stroke = 20.dp.toPx(); var start = -90f
                        state.categoryStats.take(7).forEachIndexed { index, stat ->
                            val sweep = stat.amount.toFloat() / total * 360f
                            drawArc(ExpenseChartColors[index % ExpenseChartColors.size], start, (sweep - 2f).coerceAtLeast(1f), false, Offset(stroke / 2, stroke / 2), Size(size.width - stroke, size.height - stroke), style = Stroke(stroke, cap = StrokeCap.Butt))
                            start += sweep
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(state.total.toShortVnd(), fontWeight = FontWeight.Bold); Text("Tổng chi", style = MaterialTheme.typography.labelSmall) }
                }
                Column(Modifier.weight(1f).padding(start = 10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    state.categoryStats.take(7).forEachIndexed { index, stat ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).background(ExpenseChartColors[index % ExpenseChartColors.size], CircleShape))
                            Text(stat.category?.name ?: "Khác", Modifier.weight(1f).padding(horizontal = 6.dp), style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            Column(horizontalAlignment = Alignment.End) {
                                Text(stat.amount.toShortVnd(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                Text("${stat.percent}%", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyExpenseCard(days: List<ExpenseDayStat>) {
    val max = days.maxOfOrNull { it.amount }?.coerceAtLeast(1L) ?: 1L
    FinluxPanel(Modifier.fillMaxWidth(), padding = PaddingValues(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Chi tiêu theo ngày", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Canvas(Modifier.fillMaxWidth().height(142.dp)) {
                val slot = size.width / days.size.coerceAtLeast(1)
                days.forEachIndexed { index, item ->
                    val height = item.amount.toFloat() / max * size.height * .88f
                    drawRoundRect(ExpenseRed.copy(alpha = if (item.amount > 0) .82f else .10f), Offset(index * slot + slot * .19f, size.height - height), Size(slot * .58f, height.coerceAtLeast(2.dp.toPx())), androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()))
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf(1, 5, 10, 15, 20, 25, days.size).distinct().forEach { day -> Text("$day", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}
