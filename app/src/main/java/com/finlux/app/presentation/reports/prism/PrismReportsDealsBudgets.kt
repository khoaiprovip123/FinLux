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
internal fun PrismDealsHeroCard(summary: com.finlux.app.presentation.reports.DealsSummaryReport) {
    val tokens = LocalFinluxTokens.current
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = FinluxPalette.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = FinluxPalette.CFF6366F1.copy(alpha = 0.3f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            FinluxPalette.CFF4F46E5,
                            FinluxPalette.CFF6366F1,
                            FinluxPalette.CFF4338CA,
                        ),
                    ),
                )
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("BÁO CÁO ĐẦU TƯ & CHO VAY", style = MaterialTheme.typography.labelSmall, color = FinluxPalette.White.copy(alpha = 0.85f))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = FinluxPalette.White.copy(alpha = 0.2f),
                    ) {
                        Text(
                            text = "ROI: ${if (summary.overallRoi >= 0) "+" else ""}${String.format(java.util.Locale.US, "%.1f", summary.overallRoi)}%",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (summary.overallRoi >= 0) FinluxPalette.CFF86EFAC else FinluxPalette.CFFFCA5A5,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
                Text(
                    text = formatVndAmount(summary.totalActiveCapitalOutlay),
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold),
                    color = FinluxPalette.White,
                )
                Text("Vốn đang lưu động ngoài thị trường", style = MaterialTheme.typography.bodySmall, color = FinluxPalette.White.copy(alpha = 0.9f))

                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Lãi ròng đã thu", fontSize = 11.5.sp, color = FinluxPalette.White.copy(alpha = 0.75f))
                        Text(
                            formatVndAmount(summary.totalNetProfit),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (summary.totalNetProfit >= 0) FinluxPalette.CFF4ADE80 else FinluxPalette.CFFF87171,
                        )
                    }
                    Column {
                        Text("Gốc đã thu hồi", fontSize = 11.5.sp, color = FinluxPalette.White.copy(alpha = 0.75f))
                        Text(formatVndAmount(summary.totalRecovered), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FinluxPalette.White)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Dư nợ cho vay", fontSize = 11.5.sp, color = FinluxPalette.White.copy(alpha = 0.75f))
                        Text(formatVndAmount(summary.totalLendingOutstanding), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FinluxPalette.CFFFDE047)
                    }
                }

                // Tỷ lệ phân bổ Đầu tư vs Cho vay
                if (summary.totalHistoricalCapitalOutlay > 0L) {
                    Spacer(Modifier.height(2.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Phân bổ: ${(summary.investmentRatio * 100).roundToInt()}% Đầu tư", fontSize = 11.sp, color = FinluxPalette.White.copy(alpha = 0.8f))
                            Text("${((1f - summary.investmentRatio) * 100).roundToInt()}% Cho vay", fontSize = 11.sp, color = FinluxPalette.White.copy(alpha = 0.8f))
                        }
                        Surface(
                            shape = RoundedCornerShape(3.dp),
                            color = FinluxPalette.White.copy(alpha = 0.2f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                        ) {
                            Row(Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(summary.investmentRatio.coerceAtLeast(0.01f))
                                        .background(FinluxPalette.CFF38BDF8),
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight((1f - summary.investmentRatio).coerceAtLeast(0.01f))
                                        .background(FinluxPalette.CFFFBBF24),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun PrismDealReportCard(item: com.finlux.app.presentation.reports.DealReportItem) {
    val tokens = LocalFinluxTokens.current
    val deal = item.deal
    val isInvestment = deal.category == com.finlux.app.domain.model.DealCategory.INVESTMENT
    val categoryLabel = if (isInvestment) "ĐẦU TƯ" else "CHO VAY"
    val categoryColor = if (isInvestment) FinluxPalette.CFF38BDF8 else FinluxPalette.CFFFBBF24
    val statusLabel = when (deal.status) {
        com.finlux.app.domain.model.DealStatus.ACTIVE -> "Đang chạy"
        com.finlux.app.domain.model.DealStatus.COMPLETED -> "Đã chốt"
        com.finlux.app.domain.model.DealStatus.CANCELLED -> "Đã hủy"
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (tokens.isDark) FinluxPalette.CFF1E1E2D else FinluxPalette.White,
        border = BorderStroke(1.dp, if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFE5E7EB),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = categoryColor.copy(alpha = 0.15f)) {
                        Text(
                            text = categoryLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = categoryColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        )
                    }
                    Text(
                        text = deal.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = tokens.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (item.roiPercentage >= 0) FinluxPalette.CFF10B981.copy(alpha = 0.12f) else FinluxPalette.CFFEF4444.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = "${if (item.roiPercentage >= 0) "+" else ""}${String.format(java.util.Locale.US, "%.1f", item.roiPercentage)}% ROI",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.roiPercentage >= 0) FinluxPalette.CFF10B981 else FinluxPalette.CFFEF4444,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Vốn xuất: ${formatVndAmount(item.capitalOutlay)}", fontSize = 12.sp, color = FinluxPalette.CFF6B7280)
                    Text("Còn lại: ${formatVndAmount(item.remainingCapital)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = tokens.onSurface)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Đã thu hồi: ${formatVndAmount(item.recovered)}", fontSize = 12.sp, color = FinluxPalette.CFF10B981)
                    Text("Lời ròng: ${formatVndAmount(item.netProfitLoss)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (item.netProfitLoss >= 0) FinluxPalette.CFF10B981 else FinluxPalette.CFFEF4444)
                }
            }

            // Tiến độ thu hồi vốn
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tiến độ hoàn vốn: ${(item.recoveryProgress * 100).roundToInt()}%", fontSize = 11.sp, color = FinluxPalette.CFF6B7280)
                    Text(statusLabel, fontSize = 11.sp, color = if (item.deal.status == com.finlux.app.domain.model.DealStatus.ACTIVE) FinluxPalette.CFF38BDF8 else FinluxPalette.CFF10B981)
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
                            .fillMaxWidth(item.recoveryProgress)
                            .fillMaxHeight()
                            .background(if (item.isFullyRecovered) FinluxPalette.CFF10B981 else categoryColor, RoundedCornerShape(4.dp)),
                    )
                }
            }
        }
    }
}

@Composable
internal fun PrismBudgetsHeroCard(state: ReportsUiState) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = FinluxPalette.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = FinluxPalette.CFFF59E0B.copy(alpha = 0.3f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            FinluxPalette.CFFD97706,
                            FinluxPalette.CFFF59E0B,
                            FinluxPalette.CFFB45309,
                        ),
                    ),
                )
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("BÁO CÁO NGÂN SÁCH CHI TIÊU", style = MaterialTheme.typography.labelSmall, color = FinluxPalette.White.copy(alpha = 0.8f))
                Text(
                    text = "${state.budgetUsagePercent}%",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold),
                    color = FinluxPalette.White,
                )
                Text(
                    if (state.overBudgetCount > 0) "Có ${state.overBudgetCount} danh mục đã vượt hạn mức" else "Tất cả danh mục trong tầm kiểm soát an toàn",
                    style = MaterialTheme.typography.bodySmall,
                    color = FinluxPalette.White.copy(alpha = 0.9f),
                )

                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Tổng hạn mức", fontSize = 11.5.sp, color = FinluxPalette.White.copy(alpha = 0.75f))
                        Text(formatVndAmount(state.totalBudgetLimit), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FinluxPalette.White)
                    }
                    Column {
                        Text("Đã chi tiêu", fontSize = 11.5.sp, color = FinluxPalette.White.copy(alpha = 0.75f))
                        Text(formatVndAmount(state.totalBudgetSpent), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FinluxPalette.CFFFDE047)
                    }
                    Column {
                        Text("Hạn mức còn lại", fontSize = 11.5.sp, color = FinluxPalette.White.copy(alpha = 0.75f))
                        Text(formatVndAmount(state.totalBudgetRemaining), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FinluxPalette.CFF4ADE80)
                    }
                }
            }
        }
    }
}

@Composable
internal fun PrismBudgetItemCard(item: BudgetReportItem) {
    val tokens = LocalFinluxTokens.current
    val progressColor = when {
        item.isOverBudget -> FinluxPalette.CFFEF4444
        item.percent >= 0.8f -> FinluxPalette.CFFF59E0B
        else -> FinluxPalette.CFF10B981
    }
    val cat = item.category
    val catColor = cat?.colorHex?.let { colorFromHex(it) } ?: tokens.primary
    val catIcon = cat?.let { categoryIcon(it.icon) } ?: Icons.Default.AccountBalanceWallet
    val statusLabel = when {
        item.isOverBudget -> "Vượt hạn mức"
        item.percent >= 0.8f -> "Cảnh báo (${(item.percent * 100).roundToInt()}%)"
        else -> "An toàn (${(item.percent * 100).roundToInt()}%)"
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (tokens.isDark) FinluxPalette.CFF1E1E2D else FinluxPalette.White,
        border = BorderStroke(1.dp, if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFE5E7EB),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                            .size(38.dp)
                            .background(catColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = catIcon,
                            contentDescription = null,
                            tint = catColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Column {
                        Text(
                            text = cat?.name ?: item.budget.categoryId.ifBlank { "Danh mục khác" },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = tokens.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "Hạn mức: ${formatVndAmount(item.limit)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = FinluxPalette.CFF6B7280,
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = progressColor.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = progressColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Đã chi", fontSize = 11.5.sp, color = FinluxPalette.CFF6B7280)
                    Text(
                        formatVndAmount(item.spent),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (item.isOverBudget) progressColor else tokens.onSurface,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        if (item.isOverBudget) "Vượt quá" else "Còn lại",
                        fontSize = 11.5.sp,
                        color = FinluxPalette.CFF6B7280,
                    )
                    Text(
                        formatVndAmount(if (item.isOverBudget) item.spent - item.limit else item.remaining),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isOverBudget) progressColor else FinluxPalette.CFF10B981,
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFF3F4F6,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(item.percent.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(progressColor, RoundedCornerShape(4.dp)),
                )
            }
        }
    }
}
