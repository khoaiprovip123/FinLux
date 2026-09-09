package com.finlux.app.presentation.savingspin.report

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.SavingSpinDailyTotal
import com.finlux.app.domain.model.SavingSpinDestinationTotal
import com.finlux.app.domain.model.SavingSpinReport
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.SavingSpinStatus
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SavingSpinReportScreen(
    onBack: () -> Unit,
    viewModel: SavingSpinReportViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val tokens = LocalFinluxTokens.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = tokens.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = tokens.surfaceSoft,
                        border = BorderStroke(1.dp, tokens.border),
                        modifier = Modifier.size(38.dp),
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = tokens.onSurface,
                            )
                        }
                    }
                    Text(
                        text = "Báo cáo vòng quay",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = tokens.onSurface,
                    )
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = tokens.primary)
                }
                return@Surface
            }

            val report = state.report
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // 1. Horizontal Filter Chips (7 ngày, 30 ngày, Tháng, Kỳ lương)
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        SavingSpinReportFilter.entries.forEach { filter ->
                            val isSelected = state.selectedFilter == filter
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) tokens.primary else tokens.surfaceSoft,
                                border = BorderStroke(1.dp, if (isSelected) tokens.primary else tokens.border),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clickable { viewModel.selectFilter(filter) },
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = filter.label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) tokens.onHero else tokens.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }

                if (report != null) {
                    // 2. Thẻ Tổng quan (Đã tiết kiệm + 3 stats pills)
                    item {
                        ReportOverviewCard(
                            savedAmount = report.summary.savedAmount.value,
                            completedCount = report.summary.completedCount,
                            skippedCount = report.summary.skippedCount,
                            completionRate = report.summary.completionRate,
                        )
                    }

                    // 3. Biểu đồ cột: Tiết kiệm theo ngày
                    if (report.dailyTotals.isNotEmpty()) {
                        item {
                            DailyBarChartCard(report.dailyTotals)
                        }
                    }

                    // 4. Cơ cấu theo nơi tiết kiệm
                    if (report.destinationTotals.isNotEmpty()) {
                        item {
                            DestinationBreakdownCard(
                                destinations = report.destinationTotals,
                                totalAmount = report.summary.savedAmount.value,
                            )
                        }
                    }

                    // 5. Lịch sử vòng quay
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Lịch sử vòng quay",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = tokens.onSurface,
                            )
                        }
                    }

                    if (report.sessions.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = tokens.surfaceSoft,
                                border = BorderStroke(1.dp, tokens.border),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = "Chưa có lượt quay nào trong kỳ này.",
                                    fontSize = 13.sp,
                                    color = tokens.onSurfaceVariant,
                                    modifier = Modifier.padding(20.dp),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    } else {
                        items(report.sessions) { session ->
                            HistoryRow(session)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportOverviewCard(
    savedAmount: Long,
    completedCount: Int,
    skippedCount: Int,
    completionRate: Int,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = tokens.surface,
        border = BorderStroke(1.dp, tokens.border),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Tổng quan", fontSize = 12.5.sp, color = tokens.onSurfaceVariant, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text("Đã tiết kiệm", fontSize = 13.5.sp, color = tokens.onSurfaceVariant)
            Text(
                text = formatVndAmount(savedAmount),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = tokens.primary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Hoàn thành
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = tokens.surfaceSoft,
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier
                        .weight(1f)
                        .height(62.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Surface(shape = CircleShape, color = tokens.primary, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = tokens.onHero, modifier = Modifier.padding(4.dp))
                        }
                        Column {
                            Text(completedCount.toString(), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = tokens.onSurface)
                            Text("lượt hoàn thành", fontSize = 10.sp, color = tokens.onSurfaceVariant, lineHeight = 12.sp)
                        }
                    }
                }

                // Bỏ qua
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = tokens.surfaceSoft,
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier
                        .weight(1f)
                        .height(62.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Surface(shape = CircleShape, color = FinluxColors.ExpenseRed, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = null, tint = tokens.onHero, modifier = Modifier.padding(4.dp))
                        }
                        Column {
                            Text(skippedCount.toString(), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = tokens.onSurface)
                            Text("lượt bỏ qua", fontSize = 10.sp, color = tokens.onSurfaceVariant, lineHeight = 12.sp)
                        }
                    }
                }

                // Tỷ lệ
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = tokens.surfaceSoft,
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier
                        .weight(1f)
                        .height(62.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Surface(shape = CircleShape, color = FinluxColors.IncomeGreen, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = tokens.onHero, modifier = Modifier.padding(4.dp))
                        }
                        Column {
                            Text("$completionRate%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = tokens.onSurface)
                            Text("hoàn thành", fontSize = 10.sp, color = tokens.onSurfaceVariant, lineHeight = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyBarChartCard(dailyTotals: List<SavingSpinDailyTotal>) {
    val tokens = LocalFinluxTokens.current

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = tokens.surface,
        border = BorderStroke(1.dp, tokens.border),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Tiết kiệm theo ngày", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = tokens.onSurface)
            Spacer(modifier = Modifier.height(18.dp))

            val maxAmount = dailyTotals.maxOfOrNull { it.amount.value }?.coerceAtLeast(1L) ?: 1L

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                dailyTotals.takeLast(7).forEach { item ->
                    val ratio = (item.amount.value.toFloat() / maxAmount).coerceIn(0.1f, 1f)
                    val date = LocalDate.ofEpochDay(item.epochDay)
                    val dateLabel = "${date.dayOfMonth}/${date.monthValue}"

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.fillMaxHeight(),
                    ) {
                        Text(
                            text = formatVndAmount(item.amount.value, isCompact = true),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = tokens.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .fillMaxHeight(ratio * 0.72f)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(tokens.primary),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(dateLabel, fontSize = 9.sp, color = tokens.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun DestinationBreakdownCard(
    destinations: List<SavingSpinDestinationTotal>,
    totalAmount: Long,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = tokens.surface,
        border = BorderStroke(1.dp, tokens.border),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Theo nơi tiết kiệm", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = tokens.onSurface)
            Spacer(modifier = Modifier.height(14.dp))

            val safeTotal = if (totalAmount > 0L) totalAmount else destinations.sumOf { it.amount.value }.coerceAtLeast(1L)

            destinations.forEach { item ->
                val pct = ((item.amount.value * 100) / safeTotal).toInt()
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(item.destinationName, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = tokens.onSurface)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(formatVndAmount(item.amount.value), fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = tokens.onSurface)
                            Text("$pct%", fontSize = 11.sp, color = tokens.primary, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { pct / 100f },
                        color = tokens.primary,
                        trackColor = tokens.surfaceSoft,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(session: SavingSpinSession) {
    val tokens = LocalFinluxTokens.current
    val isCompleted = session.status == SavingSpinStatus.COMPLETED

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = tokens.surface,
        border = BorderStroke(1.dp, tokens.border),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isCompleted) FinluxColors.IncomeGreen.copy(alpha = 0.14f) else FinluxColors.ExpenseRed.copy(alpha = 0.14f),
                    modifier = Modifier.size(26.dp),
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Filled.Check else Icons.Filled.Close,
                        contentDescription = null,
                        tint = if (isCompleted) FinluxColors.IncomeGreen else FinluxColors.ExpenseRed,
                        modifier = Modifier.padding(4.dp),
                    )
                }
                val dateStr = (session.completedAt ?: session.createdAt)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                Text(dateStr, fontSize = 13.sp, color = tokens.onSurfaceVariant)
            }

            Text(
                text = formatVndAmount(session.selectedAmount?.value ?: 0L),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = tokens.onSurface,
            )

            Text(
                text = if (isCompleted) "Đã hoàn thành" else "Đã bỏ qua",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isCompleted) FinluxColors.IncomeGreen else FinluxColors.ExpenseRed,
            )
        }
    }
}
