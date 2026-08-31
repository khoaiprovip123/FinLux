package com.finlux.app.presentation.savingspin.report

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.domain.model.SavingSpinDestinationTotal
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.SavingSpinStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SavingSpinReportScreen(
    onBack: () -> Unit,
    viewModel: SavingSpinReportViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8FAFC), // Nền xám nhạt cao cấp giống mockup
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            // Top Bar: Back button, Tiêu đề "Báo cáo vòng quay", Bell + Avatar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 1.dp,
                        modifier = Modifier.size(38.dp),
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color(0xFF1E293B))
                        }
                    }
                    Text("Báo cáo vòng quay", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = CircleShape, color = Color.White, shadowElevation = 1.dp, modifier = Modifier.size(38.dp)) {
                        Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.padding(9.dp))
                    }
                    Surface(shape = CircleShape, color = Color(0xFFFEF3C7), modifier = Modifier.size(38.dp)) {
                        Text("🥔", fontSize = 20.sp, modifier = Modifier.padding(top = 4.dp, start = 8.dp))
                    }
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2563EB))
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
                                color = if (isSelected) Color(0xFF2563EB) else Color.White,
                                shadowElevation = if (isSelected) 2.dp else 1.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clickable { viewModel.selectFilter(filter) },
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        filter.label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFF64748B),
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
                    item {
                        DailyBarChartCard(report)
                    }

                    // 4. Cơ cấu theo ví
                    item {
                        DestinationBreakdownCard(report.destinationTotals, report.summary.savedAmount.value)
                    }

                    // 5. Lịch sử vòng quay
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Lịch sử vòng quay", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Xem tất cả", fontSize = 12.5.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.SemiBold)
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    if (report.sessions.isEmpty()) {
                        item {
                            Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                                Text("Chưa có lượt quay nào trong kỳ này.", fontSize = 13.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(20.dp), textAlign = TextAlign.Center)
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
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Tổng quan", fontSize = 12.5.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text("Đã tiết kiệm", fontSize = 13.5.sp, color = Color(0xFF475569))
            Text(
                formatVndAmount(savedAmount),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E293B),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3 Stat Pills ngang
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Hoàn thành
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.weight(1f).height(62.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Surface(shape = CircleShape, color = Color(0xFF2563EB), modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.padding(4.dp))
                        }
                        Column {
                            Text(completedCount.toString(), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Text("lượt hoàn thành", fontSize = 10.sp, color = Color(0xFF64748B), lineHeight = 12.sp)
                        }
                    }
                }

                // Bỏ qua
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.weight(1f).height(62.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Surface(shape = CircleShape, color = Color(0xFFEF4444), modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White, modifier = Modifier.padding(4.dp))
                        }
                        Column {
                            Text(skippedCount.toString(), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Text("lượt bỏ qua", fontSize = 10.sp, color = Color(0xFF64748B), lineHeight = 12.sp)
                        }
                    }
                }

                // Tỷ lệ
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.weight(1f).height(62.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Surface(shape = CircleShape, color = Color(0xFF10B981), modifier = Modifier.size(24.dp)) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Color.White, modifier = Modifier.padding(4.dp))
                        }
                        Column {
                            Text("$completionRate%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Text("hoàn thành", fontSize = 10.sp, color = Color(0xFF64748B), lineHeight = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyBarChartCard(report: com.finlux.app.domain.model.SavingSpinReport) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Tiết kiệm theo ngày", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Xem chi tiết", fontSize = 12.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(15.dp))
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Mô phỏng 7 cột biểu đồ thanh thanh lịch theo mockup
            val mockData = listOf(
                Pair("25/08", 120_000L),
                Pair("26/08", 180_000L),
                Pair("27/08", 90_000L),
                Pair("28/08", 210_000L),
                Pair("29/08", 150_000L),
                Pair("30/08", 240_000L),
                Pair("31/08", 245_000L),
            )
            val maxAmount = 300_000L

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                mockData.forEach { (date, amount) ->
                    val ratio = (amount.toFloat() / maxAmount).coerceIn(0.1f, 1f)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.fillMaxHeight(),
                    ) {
                        Text("${amount / 1000}K", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .fillMaxHeight(ratio * 0.72f)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(Color(0xFF3B82F6)),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(date, fontSize = 9.sp, color = Color(0xFF94A3B8))
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
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Theo ví", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Xem chi tiết", fontSize = 12.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(15.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val mockDestList = listOf(
                Triple("Heo đất", 450_000L, "🐷"),
                Triple("MB Bank", 520_000L, "🏦"),
                Triple("Quỹ du lịch", 275_000L, "🏖️"),
            )
            val sum = 1_245_000L

            mockDestList.forEach { (name, amount, icon) ->
                val pct = (amount * 100 / sum).toInt()
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(icon, fontSize = 20.sp)
                            Text(name, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(formatVndAmount(amount), fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Text("$pct%", fontSize = 11.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { pct / 100f },
                        color = Color(0xFF2563EB),
                        trackColor = Color(0xFFF1F5F9),
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
    val isCompleted = session.status == SavingSpinStatus.COMPLETED
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = CircleShape,
                    color = if (isCompleted) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                    modifier = Modifier.size(26.dp),
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Filled.Check else Icons.Filled.Close,
                        contentDescription = null,
                        tint = if (isCompleted) Color(0xFF16A34A) else Color(0xFFEF4444),
                        modifier = Modifier.padding(4.dp),
                    )
                }
                val dateStr = (session.completedAt ?: session.createdAt)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                Text(dateStr, fontSize = 13.sp, color = Color(0xFF475569))
            }

            Text(
                formatVndAmount(session.selectedAmount?.value ?: 0L),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    if (isCompleted) "Đã hoàn thành" else "Đã bỏ qua",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isCompleted) Color(0xFF16A34A) else Color(0xFFEF4444),
                )
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(16.dp))
            }
        }
    }
}
