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

@Composable
internal fun PrismLargestTransactionsCard(state: ReportsUiState) {
    val tokens = LocalFinluxTokens.current
    val largest = state.largestExpense

    if (largest != null) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (tokens.isDark) FinluxPalette.CFF1E1E2D else FinluxPalette.White,
            border = BorderStroke(1.dp, if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFE5E7EB),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Giao dịch chi lớn nhất trong kỳ", fontSize = 11.5.sp, color = FinluxPalette.CFF6B7280)
                    Text(largest.note.ifBlank { "Khoản chi tiêu" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = tokens.onSurface)
                }
                Text(
                    "-${formatVndAmount(largest.amount.value)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinluxPalette.CFFEF4444,
                )
            }
        }
    }
}

@Composable
internal fun PrismTrendAnalysisCard(state: ReportsUiState) {
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
        color = if (tokens.isDark) FinluxPalette.CFF1E1E2D else FinluxPalette.White,
        border = BorderStroke(1.dp, if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFE5E7EB),
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

@Composable
internal fun PrismCashflowChartCard(
    state: ReportsUiState,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    onPickMonth: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val cashFlowPoints = state.cashFlow

    val maxIncome = remember(cashFlowPoints) {
        val peak = cashFlowPoints.maxOfOrNull { it.income } ?: 1L
        if (peak <= 0L) 1L else peak
    }
    val maxExpense = remember(cashFlowPoints) {
        val peak = cashFlowPoints.maxOfOrNull { it.expense } ?: 1L
        if (peak <= 0L) 1L else peak
    }

    val monthLabel = remember(state.range, state.period) {
        "${state.period.label}: ${state.range.start.format(DateTimeFormatter.ofPattern("dd/MM"))} - ${state.range.end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (tokens.isDark) FinluxPalette.CFF1E1E2D else FinluxPalette.White,
        border = BorderStroke(1.dp, if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFE5E7EB),
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
                Column(
                    modifier = Modifier.clickable(onClick = onPickMonth),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "Biểu đồ thu chi",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = tokens.onSurface,
                        )
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = tokens.primary,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                    Text(
                        text = monthLabel,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = FinluxPalette.CFF6B7280,
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(8.dp).background(FinluxPalette.CFF10B981, CircleShape))
                        Text("Thu", fontSize = 11.5.sp, color = FinluxPalette.CFF6B7280)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(8.dp).background(FinluxPalette.CFF5B4DFF, CircleShape))
                        Text("Chi", fontSize = 11.5.sp, color = FinluxPalette.CFF6B7280)
                    }
                }
            }

            // Interactive Day Details Pill
            AnimatedVisibility(visible = selectedIndex in cashFlowPoints.indices) {
                val selectedPoint = cashFlowPoints.getOrNull(selectedIndex)
                if (selectedPoint != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (tokens.isDark) FinluxPalette.CFF28293D else FinluxPalette.CFFF3F4F6,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Ngày ${selectedPoint.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = tokens.onSurface,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "+${formatVndAmount(selectedPoint.income)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FinluxPalette.CFF10B981,
                                )
                                Text(
                                    text = "-${formatVndAmount(selectedPoint.expense)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FinluxPalette.CFF5B4DFF,
                                )
                            }
                        }
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
                        Text("Không có dữ liệu biểu đồ", style = MaterialTheme.typography.bodySmall, color = FinluxPalette.CFF6B7280)
                    }
                } else {
                    val scrollState = rememberScrollState()
                    val today = remember { LocalDate.now(FinanceTime.VIETNAM_ZONE) }
                    val todayIndex = remember(cashFlowPoints) {
                        val idx = cashFlowPoints.indexOfFirst { it.date == today }
                        if (idx >= 0) idx else cashFlowPoints.indexOfLast { it.income > 0 || it.expense > 0 }.coerceAtLeast(0)
                    }

                    val isScrollable = cashFlowPoints.size > 14
                    val density = androidx.compose.ui.platform.LocalDensity.current
                    LaunchedEffect(cashFlowPoints.size, todayIndex) {
                        if (isScrollable && todayIndex > 0) {
                            val targetIndex = (todayIndex - 3).coerceAtLeast(0)
                            val itemPx = with(density) { 40.dp.toPx() }
                            scrollState.scrollTo((targetIndex * itemPx).toInt().coerceAtLeast(0))
                        }
                    }

                    val rowModifier = if (isScrollable) {
                        Modifier
                            .fillMaxSize()
                            .horizontalScroll(scrollState)
                    } else {
                        Modifier.fillMaxSize()
                    }

                    Row(
                        modifier = rowModifier,
                        horizontalArrangement = if (isScrollable) Arrangement.spacedBy(8.dp) else Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        cashFlowPoints.forEachIndexed { idx, point ->
                            val isSelected = selectedIndex == idx
                            val isToday = point.date == today
                            val incomeFrac = if (point.income <= 0L) 0f else (0.12f + (point.income.toFloat() / maxIncome.toFloat()) * 0.88f).coerceIn(0.12f, 1f)
                            val expenseFrac = if (point.expense <= 0L) 0f else (0.12f + (point.expense.toFloat() / maxExpense.toFloat()) * 0.88f).coerceIn(0.12f, 1f)

                            val colModifier = if (isScrollable) {
                                Modifier
                                    .width(32.dp)
                                    .fillMaxHeight()
                                    .clickable { onSelectIndex(if (isSelected) -1 else idx) }
                            } else {
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { onSelectIndex(if (isSelected) -1 else idx) }
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = colModifier,
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    // Income Bar
                                    if (incomeFrac > 0f) {
                                        Box(
                                            modifier = Modifier
                                                .width(if (isScrollable) 7.dp else 6.dp)
                                                .fillMaxHeight(incomeFrac)
                                                .background(
                                                    if (isSelected) FinluxPalette.CFF10B981 else FinluxPalette.CFF10B981.copy(alpha = 0.85f),
                                                    RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp),
                                                ),
                                        )
                                    }
                                    // Expense Bar
                                    if (expenseFrac > 0f) {
                                        Box(
                                            modifier = Modifier
                                                .width(if (isScrollable) 7.dp else 6.dp)
                                                .fillMaxHeight(expenseFrac)
                                                .background(
                                                    if (isSelected) FinluxPalette.CFF5B4DFF else FinluxPalette.CFF5B4DFF.copy(alpha = 0.85f),
                                                    RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp),
                                                ),
                                        )
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                val labelText = if (idx == 0 || point.date.dayOfMonth == 1) {
                                    "${point.date.dayOfMonth}/${point.date.monthValue}"
                                } else {
                                    point.date.format(DateTimeFormatter.ofPattern("dd"))
                                }
                                Text(
                                    text = labelText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                    color = when {
                                        isSelected -> FinluxPalette.CFF5B4DFF
                                        isToday -> tokens.primary
                                        else -> FinluxPalette.CFF6B7280
                                    },
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                )
                                if (isToday) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .size(4.dp)
                                            .background(tokens.primary, CircleShape),
                                    )
                                } else {
                                    Spacer(Modifier.height(6.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
