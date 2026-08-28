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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinluxBlue
import com.finlux.app.core.designsystem.FinluxPanel
import com.finlux.app.core.designsystem.FinluxPurple
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.GlassTopBar
import com.finlux.app.core.designsystem.IncomeGreen
import com.finlux.app.core.designsystem.WarningAmber
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.component.FinluxLazyColumn
import com.finlux.app.core.designsystem.component.FinluxListType
import com.finlux.app.core.designsystem.component.FinluxScreenScaffold
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.FinanceTransaction
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
    onSelectTransaction: ((FinanceTransaction) -> Unit)? = null,
    onActionTransaction: ((FinanceTransaction) -> Unit)? = null,
    onEditTransaction: ((FinanceTransaction) -> Unit)? = null,
    viewModel: IncomeViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val tokens = LocalFinluxTokens.current

    FinluxScreenScaffold(
        topBar = {
            GlassTopBar(
                title = { Text("Thu nhập", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") } },
                actions = {
                    TextButton(onClick = onAddIncome) {
                        Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                        Text("Thêm", Modifier.padding(start = 3.dp), fontWeight = FontWeight.Bold)
                    }
                },
            )
        },
    ) { padding ->
        FinluxLazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            listType = FinluxListType.DETAIL,
        ) {
            item {
                PeriodPicker(
                    label = state.period?.displayLabel ?: "Đang tải kỳ tài chính…",
                    previous = viewModel::previousPeriod,
                    next = viewModel::nextPeriod,
                    canNext = state.canNavigateNext,
                )
            }
            item {
                IncomeHero(
                    total = state.total,
                    changePercent = state.changePercent,
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        IncomeStatistic("Thu bình quân/ngày", state.dailyAverage.toShortVnd(), Icons.Default.ShowChart, FinluxColors.IncomeGreen, Modifier.weight(1f))
                        IncomeStatistic("Số giao dịch", state.transactions.size.toString(), Icons.Default.ReceiptLong, tokens.primary, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        IncomeStatistic("Thu cao nhất", state.highest.toShortVnd(), Icons.Default.ArrowUpward, FinluxColors.WarningAmber, Modifier.weight(1f))
                        IncomeStatistic("Thu thấp nhất", state.lowest.toShortVnd(), Icons.Default.ArrowDownward, tokens.primary, Modifier.weight(1f))
                    }
                }
            }
            item {
                IncomeCategoryCard(state = state)
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Danh sách thu nhập",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = tokens.onSurface,
                    )
                    Text("${state.transactions.size} giao dịch", color = tokens.primary, style = MaterialTheme.typography.labelLarge)
                }
            }
            if (state.transactions.isEmpty()) {
                item {
                    FinluxEmptyState(
                        title = "Chưa có thu nhập trong tháng này",
                        description = "Chạm vào nút bên dưới để ghi nhận khoản thu nhập mới.",
                        icon = Icons.Default.ArrowDownward,
                        actionLabel = "+ Thêm thu nhập",
                        onActionClick = onAddIncome,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            } else {
                items(state.transactions, key = { it.id }) { transaction ->
                    val category = state.categories[transaction.categoryId]
                    val accent = category?.let { colorFromHex(it.colorHex) } ?: FinluxColors.IncomeGreen
                    GlassCard(
                        modifier = Modifier.fillMaxWidth().animateContentSize(),
                        onClick = { onSelectTransaction?.invoke(transaction) },
                        onLongClick = { onActionTransaction?.invoke(transaction) ?: onEditTransaction?.invoke(transaction) },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(42.dp).background(accent.copy(alpha = .14f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                Icon(category?.let { categoryIcon(it.icon) } ?: Icons.Default.ArrowDownward, null, tint = accent)
                            }
                            Column(Modifier.weight(1f).padding(start = 12.dp, end = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = transaction.note.ifBlank { category?.name ?: "Thu nhập" },
                                    fontWeight = FontWeight.Bold,
                                    color = tokens.onSurface,
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
                            Text("+${transaction.amount.value.toVnd()}", color = FinluxColors.IncomeGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodPicker(label: String, previous: () -> Unit, next: () -> Unit, canNext: Boolean) {
    FinluxPanel(
        modifier = Modifier.fillMaxWidth().height(48.dp),
        cornerRadius = 24.dp,
        padding = PaddingValues(horizontal = 5.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(previous) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Tháng trước") }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Text(label, Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
            }
            IconButton(next, enabled = canNext) { Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, "Tháng sau") }
        }
    }
}

@Composable
private fun IncomeHero(total: Long, changePercent: Int) {
    FinluxPanel(
        modifier = Modifier.fillMaxWidth().height(128.dp),
        containerColor = Color.Transparent,
        borderColor = Color.White.copy(alpha = .30f),
        cornerRadius = 20.dp,
        padding = PaddingValues(0.dp),
    ) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.linearGradient(listOf(Color(0xFF5B50EC), Color(0xFF3B82F6), Color(0xFF06B6D4))),
                RoundedCornerShape(20.dp),
            ).padding(16.dp),
        ) {
            Column(Modifier.align(Alignment.CenterStart), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Tổng thu nhập tháng này", color = Color.White.copy(alpha = .90f))
                Text(total.toVnd(), color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    if (changePercent == 0) "▲ Dòng tiền vào trong tháng"
                    else "${if (changePercent >= 0) "▲" else "▼"} ${kotlin.math.abs(changePercent)}% so với kỳ trước",
                    color = Color(0xFF77FFB3),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Box(
                Modifier.align(Alignment.CenterEnd).size(68.dp).background(Color.White.copy(alpha = .15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${if (changePercent > 0) "+" else ""}$changePercent%", color = Color.White, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.TrendingUp, null, Modifier.size(19.dp), tint = Color.White)
                }
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
    modifier: Modifier = Modifier,
) {
    FinluxPanel(
        modifier = modifier.height(86.dp),
        cornerRadius = 18.dp,
        padding = PaddingValues(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
            Box(
                Modifier.size(38.dp).background(accent.copy(alpha = .13f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, Modifier.size(20.dp), tint = accent)
            }
            Column(Modifier.padding(start = 10.dp), verticalArrangement = Arrangement.Center) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Spacer(Modifier.height(2.dp))
                Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            }
        }
    }
}

@Composable
private fun IncomeCategoryCard(state: IncomeUiState) {
    FinluxPanel(Modifier.fillMaxWidth(), padding = PaddingValues(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Theo danh mục", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${state.categoryStats.size} nguồn thu", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            }
            if (state.categoryStats.isEmpty()) {
                Text("Chưa có nguồn thu trong tháng này", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                state.categoryStats.forEach { item ->
                    val accent = item.category?.let { colorFromHex(it.colorHex) } ?: IncomeGreen
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).background(accent.copy(alpha = .14f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                            Icon(item.category?.let { categoryIcon(it.icon) } ?: Icons.Default.ArrowDownward, null, Modifier.size(18.dp), tint = accent)
                        }
                        Column(Modifier.weight(1f).padding(horizontal = 10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(item.category?.name ?: "Thu nhập khác", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            LinearProgressIndicator(
                                progress = { item.percent / 100f },
                                modifier = Modifier.fillMaxWidth().height(5.dp),
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
}
