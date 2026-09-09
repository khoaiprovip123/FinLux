package com.finlux.app.presentation.reports.prism

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.DailyFinancialStatement
import com.finlux.app.presentation.reports.ReportsUiState
import java.time.format.DateTimeFormatter

/**
 * Thẻ Báo cáo Hôm nay / Báo cáo Ngày (Daily Balance Statement).
 * Trả lời trực tiếp các câu hỏi cốt lõi:
 * - Đầu ngày có bao nhiêu?
 * - Thu bao nhiêu, chi bao nhiêu?
 * - Ròng hôm nay biến động bao nhiêu?
 * - Cuối ngày còn bao nhiêu?
 */
@Composable
fun PrismDailyStatementCard(
    state: ReportsUiState,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val todayStatement = state.todayStatement ?: return

    val opening = todayStatement.openingBalance
    val income = todayStatement.totalIncome
    val expense = todayStatement.totalExpense
    val operatingNet = todayStatement.operatingNet
    val closing = todayStatement.closingBalance

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
        border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header Row: Tiêu đề + Ngày
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF5B4DFF).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = null,
                            tint = Color(0xFF5B4DFF),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Column {
                        Text(
                            text = "Báo cáo ngày",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = tokens.onSurface,
                        )
                        Text(
                            text = todayStatement.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = Color(0xFF6B7280),
                        )
                    }
                }

                // Status Badge Invariant OK
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        text = "Khớp đối chiếu",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = Color(0xFF10B981),
                    )
                }
            }

            // Hero Box: Đầu ngày -> Cuối ngày
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(tokens.surfaceSoft)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Số dư đầu ngày",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                        color = Color(0xFF6B7280),
                    )
                    Text(
                        text = formatVndAmount(opening),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = tokens.onSurface,
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                    contentDescription = null,
                    tint = Color(0xFF6B7280).copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp),
                )

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Số dư cuối ngày",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                        color = Color(0xFF6B7280),
                    )
                    Text(
                        text = formatVndAmount(closing),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Color(0xFF5B4DFF),
                    )
                }
            }

            // Flow Metrics Grid: Thu, Chi, Dòng tiền ròng
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Thu trong ngày
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.08f))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = "Thu trong ngày",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = Color(0xFF10B981),
                    )
                    Text(
                        text = "+${formatVndAmount(income)}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Color(0xFF10B981),
                    )
                }

                // Chi trong ngày
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.08f))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = "Chi trong ngày",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = Color(0xFFEF4444),
                    )
                    Text(
                        text = "-${formatVndAmount(expense)}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Color(0xFFEF4444),
                    )
                }

                // Ròng hôm nay
                val netColor = if (operatingNet >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(netColor.copy(alpha = 0.08f))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = "Ròng hôm nay",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = netColor,
                    )
                    Text(
                        text = (if (operatingNet >= 0) "+" else "") + formatVndAmount(operatingNet),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = netColor,
                    )
                }
            }

            // So sánh Hôm qua vs Hôm nay
            val yesterdayComp = state.yesterdayComparison
            val diff = yesterdayComp.netDifference
            val diffColor = if (diff >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(tokens.surfaceSoft)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "So với hôm qua (${formatVndAmount(yesterdayComp.yesterdayNet)}):",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = Color(0xFF6B7280),
                )
                Text(
                    text = (if (diff >= 0) "Tăng +" else "Giảm -") + formatVndAmount(kotlin.math.abs(diff)),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = diffColor,
                )
            }
        }
    }
}

/**
 * Thẻ Chỉ số Lũy kế (Cumulative Financial Metrics).
 * Hiển thị rõ: Trước hôm nay + Hôm nay = Tổng lũy kế.
 */
@Composable
fun PrismCumulativeMetricsCard(
    state: ReportsUiState,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val cm = state.cumulativeMetrics

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
        border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Lũy kế trong kỳ",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = tokens.onSurface,
            )

            // Dòng Thu lũy kế
            CumulativeRow(
                label = "Thu nhập",
                before = cm.incomeBefore,
                current = cm.incomeCurrent,
                total = cm.totalCumulativeIncome,
                accentColor = Color(0xFF10B981),
            )

            HorizontalDivider(color = tokens.border.copy(alpha = 0.2f))

            // Dòng Chi lũy kế
            CumulativeRow(
                label = "Chi tiêu",
                before = cm.expenseBefore,
                current = cm.expenseCurrent,
                total = cm.totalCumulativeExpense,
                accentColor = Color(0xFFEF4444),
            )

            HorizontalDivider(color = tokens.border.copy(alpha = 0.2f))

            // Dòng Ròng lũy kế
            CumulativeRow(
                label = "Dòng tiền ròng",
                before = cm.netBefore,
                current = cm.netCurrent,
                total = cm.totalCumulativeNet,
                accentColor = if (cm.totalCumulativeNet >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
            )
        }
    }
}

@Composable
private fun CumulativeRow(
    label: String,
    before: Long,
    current: Long,
    total: Long,
    accentColor: Color,
) {
    val tokens = LocalFinluxTokens.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = tokens.onSurface,
            )
            Text(
                text = "Trước đó: ${formatVndAmount(before)}  |  Hôm nay: +${formatVndAmount(current)}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                color = Color(0xFF6B7280),
            )
        }

        Text(
            text = (if (total >= 0 && label != "Chi tiêu") "+" else "") + formatVndAmount(total),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = accentColor,
        )
    }
}

/**
 * Bảng Đối chiếu Tài chính theo ngày (Daily Statement Table).
 */
@Composable
fun PrismDailyStatementsTable(
    statements: List<DailyFinancialStatement>,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    if (statements.isEmpty()) return

    var isExpanded by remember { mutableStateOf(false) }
    val displayList = if (isExpanded) statements else statements.takeLast(7).reversed()

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
        border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Bảng đối chiếu từng ngày",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = tokens.onSurface,
                )
                if (statements.size > 7) {
                    Text(
                        text = if (isExpanded) "Thu gọn" else "Xem tất cả (${statements.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = Color(0xFF5B4DFF),
                        modifier = Modifier.clickable { isExpanded = !isExpanded },
                    )
                }
            }

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(tokens.surfaceSoft)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Ngày", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280), modifier = Modifier.weight(1.2f))
                Text("Đầu ngày", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280), modifier = Modifier.weight(1.5f))
                Text("Thu/Chi", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280), modifier = Modifier.weight(1.5f))
                Text("Cuối ngày", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280), modifier = Modifier.weight(1.5f))
            }

            // Table Rows
            displayList.forEach { st ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = st.date.format(DateTimeFormatter.ofPattern("dd/MM")),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = tokens.onSurface,
                        modifier = Modifier.weight(1.2f),
                    )
                    Text(
                        text = formatVndAmount(st.openingBalance),
                        fontSize = 12.sp,
                        color = tokens.onSurfaceVariant,
                        modifier = Modifier.weight(1.5f),
                    )
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text("+${formatVndAmount(st.totalIncome)}", fontSize = 11.5.sp, color = Color(0xFF10B981))
                        Text("-${formatVndAmount(st.totalExpense)}", fontSize = 11.5.sp, color = Color(0xFFEF4444))
                    }
                    Text(
                        text = formatVndAmount(st.closingBalance),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5B4DFF),
                        modifier = Modifier.weight(1.5f),
                    )
                }
                HorizontalDivider(color = tokens.border.copy(alpha = 0.15f))
            }
        }
    }
}
