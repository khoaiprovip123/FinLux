@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

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
@Composable
internal fun PrismOverviewMultiCards(
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
                color = if (tokens.isDark) FinluxPalette.CFF1E1E2D else FinluxPalette.White,
                border = BorderStroke(1.dp, if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFE5E7EB),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onNavigateToDeepDive(DeepDiveSubTab.WALLETS) },
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.AccountBalance, null, tint = FinluxPalette.CFF0EA5E9, modifier = Modifier.size(16.dp))
                        Text("Tài sản ròng", style = MaterialTheme.typography.labelSmall, color = FinluxPalette.CFF6B7280)
                    }
                    Text(
                        formatVndAmount(state.totalNetWorth),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                        color = tokens.onSurface,
                    )
                    Text("${state.walletReportItems.size} ví hoạt động", fontSize = 11.sp, color = FinluxPalette.CFF0EA5E9)
                }
            }

            // Card 2: Dư nợ (Debts)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (tokens.isDark) FinluxPalette.CFF1E1E2D else FinluxPalette.White,
                border = BorderStroke(1.dp, if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFE5E7EB),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onNavigateToDeepDive(DeepDiveSubTab.DEBTS) },
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.CreditCard, null, tint = FinluxPalette.CFFEF4444, modifier = Modifier.size(16.dp))
                        Text("Tổng nợ còn lại", style = MaterialTheme.typography.labelSmall, color = FinluxPalette.CFF6B7280)
                    }
                    Text(
                        formatVndAmount(state.totalDebtRemaining),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                        color = if (state.totalDebtRemaining > 0) FinluxPalette.CFFEF4444 else tokens.onSurface,
                    )
                    Text("${state.debts.filter { !it.isSettled }.size} khoản nợ", fontSize = 11.sp, color = FinluxPalette.CFFEF4444)
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
                color = if (tokens.isDark) FinluxPalette.CFF1E1E2D else FinluxPalette.White,
                border = BorderStroke(1.dp, if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFE5E7EB),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onNavigateToDeepDive(DeepDiveSubTab.BUDGETS) },
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.AccountBalanceWallet, null, tint = FinluxPalette.CFFF59E0B, modifier = Modifier.size(16.dp))
                        Text("Hạn mức ngân sách", style = MaterialTheme.typography.labelSmall, color = FinluxPalette.CFF6B7280)
                    }
                    Text(
                        "${state.budgetUsagePercent}% đã dùng",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                        color = if (state.overBudgetCount > 0) FinluxPalette.CFFEF4444 else tokens.onSurface,
                    )
                    Text(
                        if (state.overBudgetCount > 0) "${state.overBudgetCount} danh mục vượt mức" else "Đang trong tầm kiểm soát",
                        fontSize = 11.sp,
                        color = if (state.overBudgetCount > 0) FinluxPalette.CFFEF4444 else FinluxPalette.CFF10B981,
                    )
                }
            }

            // Card 4: Tiết kiệm (Goals)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (tokens.isDark) FinluxPalette.CFF1E1E2D else FinluxPalette.White,
                border = BorderStroke(1.dp, if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFE5E7EB),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onNavigateToDeepDive(DeepDiveSubTab.SAVINGS) },
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Savings, null, tint = FinluxPalette.CFF10B981, modifier = Modifier.size(16.dp))
                        Text("Mục tiêu tích lũy", style = MaterialTheme.typography.labelSmall, color = FinluxPalette.CFF6B7280)
                    }
                    Text(
                        formatVndAmount(state.totalGoalSaved),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                        color = tokens.onSurface,
                    )
                    Text("${(state.overallGoalProgress * 100).roundToInt()}% tiến độ mục tiêu", fontSize = 11.sp, color = FinluxPalette.CFF10B981)
                }
            }
        }

        // Row 3: Đầu tư & Cho vay (Deals)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (tokens.isDark) FinluxPalette.CFF1E1E2D else FinluxPalette.White,
            border = BorderStroke(1.dp, if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFE5E7EB),
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
                    Surface(shape = RoundedCornerShape(10.dp), color = FinluxPalette.CFF6366F1.copy(alpha = 0.12f)) {
                        Icon(Icons.Default.TrendingUp, null, tint = FinluxPalette.CFF6366F1, modifier = Modifier.padding(6.dp).size(18.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Vốn đầu tư & Cho vay", style = MaterialTheme.typography.labelSmall, color = FinluxPalette.CFF6B7280)
                        Text(
                            formatVndAmount(state.dealsSummary.totalActiveCapitalOutlay),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                            color = tokens.onSurface,
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (state.dealsSummary.overallRoi >= 0) FinluxPalette.CFF10B981.copy(alpha = 0.12f) else FinluxPalette.CFFEF4444.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = "ROI: ${if (state.dealsSummary.overallRoi >= 0) "+" else ""}${String.format(java.util.Locale.US, "%.1f", state.dealsSummary.overallRoi)}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.dealsSummary.overallRoi >= 0) FinluxPalette.CFF10B981 else FinluxPalette.CFFEF4444,
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
internal fun PrismCategoryOverviewCard(
    state: ReportsUiState,
    onViewDetail: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val totalExpense = state.summary.expense.value

    val chartColors = listOf(
        FinluxPalette.CFFEF4444,
        FinluxPalette.CFF8B5CF6,
        FinluxPalette.CFF3B82F6,
        FinluxPalette.CFF10B981,
        FinluxPalette.CFFF59E0B,
        FinluxPalette.CFF06B6D4,
        FinluxPalette.CFFEC4899,
        FinluxPalette.CFF94A3B8,
    )

    val displayCategories = remember(state.expensesByCategory) { state.expensesByCategory.take(8) }
    val totalDisplayExpense = totalExpense.coerceAtLeast(1L)

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
                        color = FinluxPalette.CFF6B7280,
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
                                color = FinluxPalette.CFF6B7280,
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
internal fun PrismIncomeCategoryCard(state: ReportsUiState) {
    val tokens = LocalFinluxTokens.current
    val totalIncome = state.summary.income.value.coerceAtLeast(1L)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (tokens.isDark) FinluxPalette.CFF1E1E2D else FinluxPalette.White,
        border = BorderStroke(1.dp, if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFE5E7EB),
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
                            color = FinluxPalette.CFF10B981,
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFF3F4F6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(pct / 100f)
                                .fillMaxHeight()
                                .background(FinluxPalette.CFF10B981, RoundedCornerShape(4.dp)),
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
