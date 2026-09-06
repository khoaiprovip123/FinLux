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
internal fun PrismDebtsHeroCard(state: ReportsUiState) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = FinluxPalette.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = FinluxPalette.CFFEF4444.copy(alpha = 0.3f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            FinluxPalette.CFFDC2626,
                            FinluxPalette.CFFE11D48,
                            FinluxPalette.CFFBE123C,
                        ),
                    ),
                )
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("BÁO CÁO DƯ NỢ & VAY NỢ", style = MaterialTheme.typography.labelSmall, color = FinluxPalette.White.copy(alpha = 0.8f))
                Text(
                    text = formatVndAmount(state.totalDebtRemaining),
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold),
                    color = FinluxPalette.White,
                )
                Text("Tổng dư nợ gốc cần chi trả", style = MaterialTheme.typography.bodySmall, color = FinluxPalette.White.copy(alpha = 0.9f))

                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Tổng nợ ban đầu", fontSize = 11.5.sp, color = FinluxPalette.White.copy(alpha = 0.75f))
                        Text(formatVndAmount(state.totalDebtOriginal), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FinluxPalette.White)
                    }
                    Column {
                        Text("Đã thanh toán", fontSize = 11.5.sp, color = FinluxPalette.White.copy(alpha = 0.75f))
                        Text(formatVndAmount(state.totalDebtPaid), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FinluxPalette.CFF4ADE80)
                    }
                    Column {
                        Text("Lãi trả trong kỳ", fontSize = 11.5.sp, color = FinluxPalette.White.copy(alpha = 0.75f))
                        Text(formatVndAmount(state.totalDebtInterestPaidInPeriod), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FinluxPalette.CFFFDE047)
                    }
                }
            }
        }
    }
}

@Composable
internal fun PrismDebtItemCard(item: DebtReportItem) {
    val tokens = LocalFinluxTokens.current
    val debt = item.debt
    val typeLabel = when (debt.type) {
        DebtType.CREDIT_CARD -> "Thẻ tín dụng"
        DebtType.BANK_LOAN -> "Vay ngân hàng"
        DebtType.PERSONAL_LOAN -> "Vay người thân"
        DebtType.INSTALLMENT -> "Trả góp"
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
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = FinluxPalette.CFFEF4444.copy(alpha = 0.12f),
                    ) {
                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = FinluxPalette.CFFEF4444,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                    Text(debt.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = tokens.onSurface)
                }

                if (debt.isSettled) {
                    Surface(shape = RoundedCornerShape(8.dp), color = FinluxPalette.CFF10B981.copy(alpha = 0.12f)) {
                        Text("Đã tất toán", color = FinluxPalette.CFF10B981, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                    }
                } else {
                    Text("Đến hạn ngày ${debt.dueDate}", fontSize = 12.sp, color = FinluxPalette.CFF6B7280)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Dư nợ hiện tại", fontSize = 11.sp, color = FinluxPalette.CFF6B7280)
                    Text(formatVndAmount(item.remaining), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = FinluxPalette.CFFEF4444)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Hạn mức / Nợ gốc", fontSize = 11.sp, color = FinluxPalette.CFF6B7280)
                    Text(formatVndAmount(debt.totalAmount.value), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = tokens.onSurface)
                }
            }

            // Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Đã thanh toán: ${formatVndAmount(item.totalPaid)}", fontSize = 11.5.sp, color = FinluxPalette.CFF10B981)
                    Text("${(item.progress * 100).roundToInt()}%", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = tokens.onSurface)
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
                            .fillMaxWidth(item.progress)
                            .fillMaxHeight()
                            .background(FinluxPalette.CFF10B981, RoundedCornerShape(4.dp)),
                    )
                }
            }
        }
    }
}

/**
 * 8. Section: Savings & Goals Report Card
 */
@Composable
internal fun PrismSavingsHeroCard(state: ReportsUiState) {
    val tokens = LocalFinluxTokens.current
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = FinluxPalette.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = tokens.primary.copy(alpha = 0.3f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = tokens.heroGradient,
                    ),
                )
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("BÁO CÁO TIẾT KIỆM & TÍCH LŨY", style = MaterialTheme.typography.labelSmall, color = tokens.onHeroMuted)
                Text(
                    text = formatVndAmount(state.totalGoalSaved),
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold),
                    color = tokens.onHero,
                )
                Text("Tổng đã tích lũy vào các mục tiêu", style = MaterialTheme.typography.bodySmall, color = tokens.onHeroMuted)

                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Dòng tiền còn lại", fontSize = 11.5.sp, color = tokens.onHeroMuted)
                        Text(formatVndAmount(state.unspentCashFlow), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = tokens.onHero)
                    }
                    Column {
                        Text("Tỷ lệ giữ lại", fontSize = 11.5.sp, color = tokens.onHeroMuted)
                        Text("${state.savingsRatePercent}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = tokens.onHero)
                    }
                    Column {
                        Text("Vào mục tiêu kỳ này", fontSize = 11.5.sp, color = tokens.onHeroMuted)
                        Text(formatVndAmount(state.goalContributionInPeriod), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = tokens.onHero)
                    }
                }
            }
        }
    }
}

@Composable
internal fun PrismGoalItemCard(item: GoalReportItem) {
    val tokens = LocalFinluxTokens.current
    val goal = item.goal

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
                    Surface(shape = RoundedCornerShape(10.dp), color = FinluxPalette.CFF10B981.copy(alpha = 0.12f)) {
                        Icon(Icons.Default.Savings, null, tint = FinluxPalette.CFF10B981, modifier = Modifier.padding(8.dp).size(20.dp))
                    }
                    Column {
                        Text(goal.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = tokens.onSurface)
                        Text(goal.category.ifBlank { "Mục tiêu tài chính" }, style = MaterialTheme.typography.bodySmall, color = FinluxPalette.CFF6B7280)
                    }
                }
                Text("${(item.progress * 100).roundToInt()}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = FinluxPalette.CFF10B981)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Đã tích lũy", fontSize = 11.sp, color = FinluxPalette.CFF6B7280)
                    Text(formatVndAmount(item.saved), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = FinluxPalette.CFF10B981)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Mục tiêu cần đạt", fontSize = 11.sp, color = FinluxPalette.CFF6B7280)
                    Text(formatVndAmount(item.target), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = tokens.onSurface)
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
                        .fillMaxWidth(item.progress)
                        .fillMaxHeight()
                        .background(FinluxPalette.CFF10B981, RoundedCornerShape(4.dp)),
                )
            }
        }
    }
}

@Composable
internal fun PrismSavingSpinReportCard(
    summary: com.finlux.app.presentation.reports.SavingSpinSummaryReport,
    onViewDetails: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (tokens.isDark) FinluxPalette.CFF261C38 else FinluxPalette.CFFFAF5FF,
        border = BorderStroke(1.dp, FinluxPalette.CFFA855F7.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = FinluxPalette.CFFA855F7.copy(alpha = 0.2f),
                ) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = FinluxPalette.CFFA855F7,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(24.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "Vòng quay tiết kiệm",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = tokens.onSurface,
                        )
                        if (summary.currentStreak > 0) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = FinluxPalette.CFFF97316.copy(alpha = 0.15f),
                            ) {
                                Text(
                                    text = "🔥 ${summary.currentStreak} ngày",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FinluxPalette.CFFF97316,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                    Text(
                        text = "Đã tích lũy: ${formatVndAmount(summary.totalSaved)} (${summary.completedCount} lượt)",
                        style = MaterialTheme.typography.bodySmall,
                        color = FinluxPalette.CFFA855F7,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Xem chi tiết",
                tint = FinluxPalette.CFFA855F7,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/**
 * 8b. Section: Deals & Investment Hero Card
 */
