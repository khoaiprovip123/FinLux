package com.finlux.app.presentation.savingspin.report

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.component.FinluxScreenHeader
import com.finlux.app.core.designsystem.component.FinluxScreenScaffold
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.SavingSpinDestinationTotal
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

    FinluxScreenScaffold(
        topBar = {
            FinluxScreenHeader(
                title = "Báo cáo vòng quay",
                subtitle = "Thống kê thói quen tích lũy",
                onBack = onBack,
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(color = tokens.primary)
            }
            return@FinluxScreenScaffold
        }

        val report = state.report
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(tokens.spacing.base),
            verticalArrangement = Arrangement.spacedBy(tokens.spacing.md),
        ) {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(SavingSpinReportFilter.entries) { filter ->
                        FilterChip(
                            selected = state.selectedFilter == filter,
                            onClick = { viewModel.selectFilter(filter) },
                            label = { Text(filter.label) },
                        )
                    }
                }
            }

            if (report != null) {
                item {
                    ReportSummaryCard(report.summary.savedAmount.value, report.summary.completedCount, report.summary.skippedCount, report.summary.completionRate, report.summary.currentStreak)
                }

                if (report.destinationTotals.isNotEmpty()) {
                    item {
                        DestinationBreakdownCard(report.destinationTotals, report.summary.savedAmount.value)
                    }
                }

                item {
                    Text(
                        "Lịch sử lượt quay",
                        style = FinluxTextStyles.SectionTitle,
                        color = tokens.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = tokens.spacing.sm),
                    )
                }

                if (report.sessions.isEmpty()) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Chưa có lượt quay nào trong khoảng thời gian này.",
                                style = FinluxTextStyles.Caption,
                                color = tokens.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    items(report.sessions) { session ->
                        SessionHistoryRow(session)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportSummaryCard(
    savedAmount: Long,
    completedCount: Int,
    skippedCount: Int,
    completionRate: Int,
    currentStreak: Int,
) {
    val tokens = LocalFinluxTokens.current
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(tokens.spacing.sm)) {
            Text("Tổng đã tiết kiệm", style = FinluxTextStyles.Caption, color = tokens.onSurfaceVariant)
            Text(
                formatVndAmount(savedAmount),
                style = FinluxTextStyles.DisplayAmount,
                color = tokens.primary,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(modifier = Modifier.height(tokens.spacing.xs))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("Hoàn thành", style = FinluxTextStyles.Caption, color = tokens.onSurfaceVariant)
                    Text("$completedCount lượt", style = FinluxTextStyles.Body, fontWeight = FontWeight.Bold, color = tokens.onSurface)
                }
                Column {
                    Text("Bỏ qua", style = FinluxTextStyles.Caption, color = tokens.onSurfaceVariant)
                    Text("$skippedCount lượt", style = FinluxTextStyles.Body, fontWeight = FontWeight.Bold, color = tokens.onSurface)
                }
                Column {
                    Text("Tỷ lệ", style = FinluxTextStyles.Caption, color = tokens.onSurfaceVariant)
                    Text("$completionRate%", style = FinluxTextStyles.Body, fontWeight = FontWeight.Bold, color = tokens.onSurface)
                }
                Column {
                    Text("Chuỗi", style = FinluxTextStyles.Caption, color = tokens.onSurfaceVariant)
                    Text("$currentStreak kỳ", style = FinluxTextStyles.Body, fontWeight = FontWeight.Bold, color = tokens.primary)
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
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(tokens.spacing.md)) {
            Text("Theo nơi cất tiền", style = FinluxTextStyles.CardTitle, color = tokens.onSurface, fontWeight = FontWeight.Bold)
            destinations.forEach { item ->
                val ratio = if (totalAmount > 0) item.amount.value.toFloat() / totalAmount else 0f
                Column(verticalArrangement = Arrangement.spacedBy(tokens.spacing.xs)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(item.destinationName, style = FinluxTextStyles.Body, color = tokens.onSurface)
                        Text(formatVndAmount(item.amount.value), style = FinluxTextStyles.Body, fontWeight = FontWeight.SemiBold, color = tokens.onSurface)
                    }
                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = tokens.primary,
                        trackColor = tokens.surfaceSoft,
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionHistoryRow(session: SavingSpinSession) {
    val tokens = LocalFinluxTokens.current
    val (statusLabel, statusColor, icon) = when (session.status) {
        SavingSpinStatus.COMPLETED -> Triple("Đã hoàn thành", tokens.primary, Icons.Filled.CheckCircle)
        SavingSpinStatus.SKIPPED -> Triple("Đã bỏ qua", tokens.onSurfaceVariant, Icons.Filled.Close)
        SavingSpinStatus.SNOOZED -> Triple("Nhắc lại", tokens.primary, Icons.Filled.Schedule)
        SavingSpinStatus.SPUN_PENDING -> Triple("Chờ nạp tiền", tokens.primary, Icons.Filled.Schedule)
        SavingSpinStatus.READY -> Triple("Chưa quay", tokens.onSurfaceVariant, Icons.Filled.Schedule)
    }
    val dateText = (session.completedAt ?: session.createdAt)
        .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm"))

    Surface(
        shape = RoundedCornerShape(tokens.radius.standardCard),
        color = tokens.surfaceSoft,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(tokens.spacing.base),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md),
        ) {
            Surface(
                shape = CircleShape,
                color = statusColor.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.padding(8.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    session.selectedAmount?.let { formatVndAmount(it.value) } ?: "0đ",
                    style = FinluxTextStyles.Body,
                    fontWeight = FontWeight.Bold,
                    color = tokens.onSurface,
                )
                Text(dateText, style = FinluxTextStyles.Caption, color = tokens.onSurfaceVariant)
            }
            Text(statusLabel, style = FinluxTextStyles.Caption, fontWeight = FontWeight.SemiBold, color = statusColor)
        }
    }
}
