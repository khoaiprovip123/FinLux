package com.finlux.app.presentation.reports.prism

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.presentation.reports.ReportsUiState
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
internal fun PrismReportsHeroBanner(
    state: ReportsUiState,
    onPickMonth: () -> Unit,
) {
    val net = state.operatingSummary.net
    val income = state.operatingSummary.income.value
    val expense = state.operatingSummary.expense.value
    val savingRatePct = state.savingsRatePercent.coerceIn(0, 100)

    val deltaPercent = if (state.previousOperatingNet != 0L) {
        val diff = net - state.previousOperatingNet
        (((diff.toDouble() / Math.abs(state.previousOperatingNet).toDouble()) * 100.0)).roundToInt()
    } else 0

    val monthLabel = remember(state.range) {
        "Kỳ: ${state.range.start.format(DateTimeFormatter.ofPattern("dd/MM"))} - ${state.range.end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF5B4DFF).copy(alpha = 0.4f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF5B4DFF),
                            Color(0xFF6366F1),
                            Color(0xFF7C3AED),
                        ),
                    ),
                )
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left Column: Total Net & Delta
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onPickMonth),
                    ) {
                        Text(
                            text = monthLabel,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }

                    // Báo cáo theo kỳ phải hiển thị số dư cuối kỳ lịch sử, không dùng số dư hiện tại.
                    val heroLabel = if (state.selectedWallet != null) {
                        "SỐ DƯ CUỐI KỲ · ${state.selectedWallet!!.name.uppercase()}"
                    } else {
                        "SỐ DƯ CUỐI KỲ"
                    }
                    val displayBalance = state.closingBalance

                    Text(
                        text = heroLabel,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp,
                        ),
                        color = Color.White.copy(alpha = 0.85f),
                    )

                    // Con số to nổi bật nhất: số dư tại đúng mốc cuối kỳ đang xem.
                    Text(
                        text = formatVndAmount(displayBalance),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp,
                        ),
                        color = Color.White,
                    )

                    // Phía dưới là Dòng tiền ròng (Thu – Chi)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "Thặng dư hoạt động:",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                            color = Color.White.copy(alpha = 0.90f),
                        )
                        Text(
                            text = (if (net >= 0) "+" else "") + formatVndAmount(net),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                            ),
                            color = if (net >= 0) Color(0xFF4ADE80) else Color(0xFFFCA5A5),
                        )
                    }

                    // Giải thích ngữ nghĩa rõ ràng: Chi vượt thu hoặc Thặng dư
                    Text(
                        text = if (net < 0) {
                            "Chi hoạt động vượt thu hoạt động trong kỳ"
                        } else {
                            if (deltaPercent >= 0) "Thặng dư hoạt động (+${deltaPercent}% so với kỳ trước)" else "Thặng dư hoạt động (${deltaPercent}% so với kỳ trước)"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                        color = Color.White.copy(alpha = 0.85f),
                    )

                    Spacer(Modifier.height(3.dp))

                    // Mini Income, Expense, Transfer Sub-stats
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Column {
                            Text("Thu hoạt động", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                            Text(
                                "+${formatVndAmount(income)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4ADE80), // Mint Green
                            )
                        }
                        Column {
                            Text("Chi hoạt động", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                            Text(
                                "-${formatVndAmount(expense)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDE047), // Golden Yellow
                            )
                        }
                        if (state.selectedWallet != null) {
                            if (state.totalTransferOut > 0) {
                                Column {
                                    Text("Chuyển đi", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                                    Text(
                                        "-${formatVndAmount(state.totalTransferOut)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFDBA74), // Orange
                                    )
                                }
                            }
                            if (state.totalTransferIn > 0) {
                                Column {
                                    Text("Nhận chuyển", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                                    Text(
                                        "+${formatVndAmount(state.totalTransferIn)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF93C5FD), // Light Blue
                                    )
                                }
                            }
                            Column {
                                Text("Biến động ví", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                                Text(
                                    "${if (state.currentWalletNetChange >= 0) "+" else ""}${formatVndAmount(state.currentWalletNetChange)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.currentWalletNetChange >= 0) Color(0xFF4ADE80) else Color(0xFFFCA5A5),
                                )
                            }
                        } else {
                            Column {
                                Text("Đầu kỳ", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                                Text(
                                    formatVndAmount(state.openingBalance),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }

                // Right Column: Circular Indicator (Savings Rate or Wallet Asset Share Ring)
                val rightCirclePct = if (state.selectedWallet != null) {
                    if (state.totalAssets > 0) {
                        ((state.selectedWallet!!.balance.value.toFloat() / state.totalAssets.toFloat()) * 100).roundToInt().coerceIn(0, 100)
                    } else 0
                } else {
                    savingRatePct
                }
                val rightCircleLabel = if (state.selectedWallet != null) "Tài sản" else "Giữ lại"

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(76.dp),
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 7.dp.toPx()
                        val arcSize = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                        // Track
                        drawArc(
                            color = Color.White.copy(alpha = 0.22f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )

                        // Progress
                        val progressSweep = (rightCirclePct / 100f) * 360f
                        drawArc(
                            color = Color.White,
                            startAngle = -90f,
                            sweepAngle = progressSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "$rightCirclePct%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                            ),
                            color = Color.White,
                        )
                        Text(
                            text = rightCircleLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                }
            }
        }
    }
}
