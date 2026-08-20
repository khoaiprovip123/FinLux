package com.finlux.app.presentation.reports.prism

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.component.FinluxFilterChip
import com.finlux.app.core.designsystem.component.FinluxHeroCard
import com.finlux.app.core.designsystem.component.FinluxInsightCard
import com.finlux.app.core.designsystem.component.FinluxMetricCard
import com.finlux.app.core.designsystem.component.FinluxScreenHeader
import com.finlux.app.core.designsystem.component.FinluxSectionHeader
import com.finlux.app.core.designsystem.component.FinluxSoftCard
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.navigation.Route
import com.finlux.app.presentation.components.MainBottomBar
import com.finlux.app.presentation.reports.ExportReportDialog
import com.finlux.app.presentation.reports.ReportPeriod
import com.finlux.app.presentation.reports.ReportsViewModel

@Composable
fun PrismReportsScreen(
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val tokens = LocalFinluxTokens.current
    var showExportDialog by remember { mutableStateOf(false) }

    val isRootTab = onBack == null

    Scaffold(
        topBar = {
            FinluxScreenHeader(
                title = "Báo cáo tài chính",
                subtitle = "Phân tích thu chi & dòng tiền",
                onBack = onBack,
                actions = {
                    Surface(
                        shape = CircleShape,
                        color = tokens.surfaceSoft,
                        modifier = Modifier.size(40.dp),
                    ) {
                        IconButton(onClick = { showExportDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Xuất báo cáo",
                                tint = tokens.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (isRootTab) {
                MainBottomBar(Route.Reports.value, onNavigate, onAdd)
            }
        },
        containerColor = tokens.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = tokens.spacing.lg,
                end = tokens.spacing.lg,
                top = tokens.spacing.xs,
                bottom = if (isRootTab) 112.dp else 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(tokens.spacing.md),
        ) {
            // Period Selector Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.spacing.xs),
                ) {
                    ReportPeriod.entries.forEach { option ->
                        FinluxFilterChip(
                            label = option.label,
                            isSelected = state.period == option,
                            onClick = { viewModel.selectPeriod(option) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // Overview Hero Card
            item {
                val net = state.summary.net
                val deltaText = if (state.previousNet != 0L) {
                    val diff = net - state.previousNet
                    val pct = if (state.previousNet != 0L) ((diff.toDouble() / Math.abs(state.previousNet)) * 100).toInt() else 0
                    if (pct > 0) "+$pct% so với kỳ trước" else "$pct% so với kỳ trước"
                } else null

                FinluxHeroCard(
                    title = "Dòng tiền ròng (${state.period.label.lowercase()})",
                    amountText = formatVndAmount(net),
                    deltaText = deltaText,
                    isPositiveDelta = net >= 0,
                )
            }

            // Bento Metrics (Income vs Expense)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
                ) {
                    FinluxMetricCard(
                        title = "Tổng thu nhập",
                        value = formatVndAmount(state.summary.income.value, isCompact = true),
                        supportingText = if (state.previousIncome > 0L) {
                            val pct = (((state.summary.income.value - state.previousIncome) * 100.0) / state.previousIncome).toInt()
                            if (pct >= 0) "+$pct%" else "$pct%"
                        } else null,
                        accentColor = FinluxColors.IncomeGreen,
                        modifier = Modifier.weight(1f),
                    )
                    FinluxMetricCard(
                        title = "Tổng chi tiêu",
                        value = formatVndAmount(state.summary.expense.value, isCompact = true),
                        supportingText = if (state.previousExpense > 0L) {
                            val pct = (((state.summary.expense.value - state.previousExpense) * 100.0) / state.previousExpense).toInt()
                            if (pct <= 0) "$pct% (tốt)" else "+$pct%"
                        } else null,
                        accentColor = FinluxColors.ExpenseRed,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Intelligent Insight Section
            item {
                val totalExpense = state.summary.expense.value
                val topCat = state.expensesByCategory.maxByOrNull { it.amount }
                val insightMsg = if (topCat != null && totalExpense > 0L) {
                    val percent = ((topCat.amount * 100.0) / totalExpense).toInt()
                    val name = topCat.category?.name ?: "Khác"
                    "Chi tiêu trong kỳ tập trung chủ yếu vào $name, chiếm $percent% tổng chi phí."
                } else if (state.summary.income.value > 0L && totalExpense == 0L) {
                    "Tuyệt vời! Không có khoản chi nào được ghi nhận trong kỳ này."
                } else {
                    "Chưa đủ dữ liệu giao dịch trong kỳ để tổng hợp phân tích chi tiết."
                }

                FinluxInsightCard(
                    title = "Phân tích chuyên sâu",
                    description = insightMsg,
                    accentColor = FinluxColors.PrimaryBlue,
                )
            }

            // Category Breakdown Section Header
            item {
                FinluxSectionHeader(
                    title = "Cơ cấu chi tiêu theo danh mục",
                )
            }

            // Category Breakdown Bento Blocks
            if (state.expensesByCategory.isEmpty()) {
                item {
                    FinluxEmptyState(
                        title = "Chưa có chi tiêu trong kỳ",
                        description = "Các khoản chi phát sinh trong kỳ sẽ được tổng hợp tự động tại đây.",
                    )
                }
            } else {
                val totalExpense = state.summary.expense.value
                items(state.expensesByCategory, key = { it.category?.id ?: "other" }) { item ->
                    val cat = item.category
                    val amount = item.amount
                    val percent = if (totalExpense > 0L) ((amount * 100.0) / totalExpense).toInt() else 0
                    val progressFloat = if (totalExpense > 0L) (amount.toFloat() / totalExpense.toFloat()).coerceIn(0f, 1f) else 0f
                    val catColor = cat?.colorHex?.let { colorFromHex(it) } ?: FinluxColors.PrimaryBlue
                    val catIcon = cat?.icon ?: "Category"

                    FinluxSoftCard(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(catColor.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = categoryIcon(catIcon),
                                            contentDescription = null,
                                            tint = catColor,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = cat?.name ?: "Khác",
                                            style = FinluxTextStyles.CardTitle.copy(fontWeight = FontWeight.Bold),
                                            color = tokens.onSurface,
                                        )
                                        Text(
                                            text = "$percent% tổng chi",
                                            style = FinluxTextStyles.MicroLabel,
                                            color = tokens.onSurfaceVariant,
                                        )
                                    }
                                }

                                Text(
                                    text = formatVndAmount(amount),
                                    style = FinluxTextStyles.CardTitle.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                    ),
                                    color = tokens.onSurface,
                                )
                            }

                            LinearProgressIndicator(
                                progress = { progressFloat },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = catColor,
                                trackColor = tokens.surfaceSoft,
                                strokeCap = StrokeCap.Round,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        ExportReportDialog(
            state = state,
            onDismiss = { showExportDialog = false },
        )
    }
}
