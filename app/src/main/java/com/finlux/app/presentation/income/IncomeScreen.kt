package com.finlux.app.presentation.income

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinluxBlue
import com.finlux.app.core.designsystem.FinluxPurple
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.GlassTopBar
import com.finlux.app.core.designsystem.FinluxStyleBackdrop
import com.finlux.app.core.designsystem.GradientHeroCard
import com.finlux.app.core.designsystem.IncomeGreen
import com.finlux.app.core.designsystem.WarningAmber
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.navigation.Route
import com.finlux.app.presentation.components.MainBottomBar
import com.finlux.app.presentation.home.toShortVnd
import com.finlux.app.presentation.home.toVnd
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun IncomeScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onAddIncome: () -> Unit,
    viewModel: IncomeViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    Box(Modifier.fillMaxSize()) {
    FinluxStyleBackdrop(Modifier.fillMaxSize())
    Scaffold(
        topBar = {
            GlassTopBar(
                title = { Text("Thu nhập", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Quay lại") } },
                actions = {
                    IconButton(onClick = onAddIncome) { Icon(Icons.Default.Add, "Thêm thu nhập", tint = MaterialTheme.colorScheme.primary) }
                },
            )
        },
        bottomBar = { MainBottomBar(Route.Income.value, onNavigate, onAddIncome) },
        containerColor = Color.Transparent,
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                GlassCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        IconButton(viewModel::previousMonth) { Icon(Icons.Default.ArrowBack, "Tháng trước") }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(
                                state.month.format(DateTimeFormatter.ofPattern("'Tháng' M, yyyy", Locale.forLanguageTag("vi-VN"))),
                                Modifier.padding(start = 8.dp),
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        IconButton(viewModel::nextMonth, enabled = state.month < java.time.YearMonth.now()) { Icon(Icons.Default.ArrowForwardIos, "Tháng sau") }
                    }
                }
            }
            item {
                GradientHeroCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(50.dp).background(Color.White.copy(alpha = .16f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ArrowDownward, null, tint = Color(0xFF70FFA9))
                        }
                        Column(Modifier.weight(1f).padding(start = 14.dp)) {
                            Text("Tổng thu nhập tháng này", color = Color.White.copy(alpha = .82f))
                            Text(state.total.toVnd(), color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("▲ Dòng tiền vào trong tháng", color = Color(0xFF77FFB3), style = MaterialTheme.typography.bodyMedium)
                        }
                        Icon(Icons.Default.ShowChart, null, Modifier.size(52.dp), tint = Color.White.copy(alpha = .22f))
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IncomeStatistic("Thu bình quân/ngày", state.dailyAverage.toShortVnd(), Icons.Default.ShowChart, IncomeGreen, Modifier.weight(1f))
                        IncomeStatistic("Số giao dịch", state.transactions.size.toString(), Icons.Default.ReceiptLong, FinluxBlue, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IncomeStatistic("Thu cao nhất", state.highest.toShortVnd(), Icons.Default.ArrowUpward, WarningAmber, Modifier.weight(1f))
                        IncomeStatistic("Thu thấp nhất", state.lowest.toShortVnd(), Icons.Default.ArrowDownward, FinluxPurple, Modifier.weight(1f))
                    }
                }
            }
            item {
                GlassCard(Modifier.fillMaxWidth().animateContentSize()) {
                    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Theo danh mục", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${state.categoryStats.size} nguồn thu", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                        }
                        if (state.categoryStats.isEmpty()) Text("Chưa có nguồn thu trong tháng này", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        state.categoryStats.forEach { item ->
                            val accent = item.category?.let { colorFromHex(it.colorHex) } ?: IncomeGreen
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(34.dp).background(accent.copy(alpha = .14f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                                    Icon(item.category?.let { categoryIcon(it.icon) } ?: Icons.Default.ArrowDownward, null, Modifier.size(18.dp), tint = accent)
                                }
                                Column(Modifier.weight(1f).padding(horizontal = 10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text(item.category?.name ?: "Thu nhập khác", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                    LinearProgressIndicator(
                                        progress = { item.percent / 100f },
                                        modifier = Modifier.fillMaxWidth().height(4.dp),
                                        color = accent,
                                        trackColor = accent.copy(alpha = .12f),
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(item.amount.toShortVnd(), fontWeight = FontWeight.Bold)
                                    Text("${item.percent}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Danh sách thu nhập", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${state.transactions.size} giao dịch", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                }
            }
            if (state.transactions.isEmpty()) {
                item {
                    GlassCard(Modifier.fillMaxWidth(), onClick = onAddIncome) {
                        Text("Chưa có thu nhập. Chạm để thêm giao dịch đầu tiên.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(state.transactions, key = { it.id }) { transaction ->
                    val category = state.categories[transaction.categoryId]
                    val accent = category?.let { colorFromHex(it.colorHex) } ?: IncomeGreen
                    GlassCard(Modifier.fillMaxWidth().animateContentSize()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(42.dp).background(accent.copy(alpha = .14f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                Icon(category?.let { categoryIcon(it.icon) } ?: Icons.Default.ArrowDownward, null, tint = accent)
                            }
                            Column(Modifier.weight(1f).padding(horizontal = 11.dp)) {
                                Text(transaction.note.ifBlank { category?.name ?: "Thu nhập" }, fontWeight = FontWeight.Bold)
                                Text(
                                    transaction.date.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text("+${transaction.amount.value.toVnd()}", color = IncomeGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
    }
}

@Composable
private fun IncomeStatistic(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    modifier: Modifier,
) {
    GlassCard(modifier.height(86.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(34.dp).background(accent.copy(alpha = .13f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, Modifier.size(18.dp), tint = accent)
            }
            Column(Modifier.padding(start = 9.dp)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Text(value, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}
