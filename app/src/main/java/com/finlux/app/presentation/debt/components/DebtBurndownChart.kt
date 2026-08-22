package com.finlux.app.presentation.debt.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.FinluxBlue
import com.finlux.app.core.designsystem.FinluxCyan
import com.finlux.app.core.designsystem.FinluxPurple
import com.finlux.app.core.designsystem.LiquidGlassSurface
import com.finlux.app.domain.model.DebtPayoffPlan
import com.finlux.app.presentation.home.toShortVnd
import java.time.format.DateTimeFormatter

@Composable
fun DebtBurndownChart(
    payoffPlan: DebtPayoffPlan?,
    initialDebtAmount: Long,
    modifier: Modifier = Modifier,
) {
    LiquidGlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Lộ trình giảm nợ",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Mô phỏng số dư nợ theo thời gian",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (payoffPlan != null && payoffPlan.totalMonths > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FinluxPurple.copy(alpha = 0.14f),
                    ) {
                        Text(
                            text = "${payoffPlan.totalMonths} tháng",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = FinluxPurple,
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            if (payoffPlan == null || payoffPlan.totalMonths == 0 || initialDebtAmount <= 0L) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Không có dư nợ hoặc đã hoàn tất trả nợ 🎉",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                // Xây dựng chuỗi điểm số dư theo từng tháng
                val schedule = payoffPlan.paymentSchedule
                val totalSteps = payoffPlan.totalMonths
                // Gom nhóm số dư còn lại cuối mỗi tháng
                val monthlyBalances = mutableListOf<Long>()
                monthlyBalances.add(initialDebtAmount)

                for (m in 1..totalSteps) {
                    val stepInMonth = schedule.filter { it.monthIndex == m }
                    if (stepInMonth.isNotEmpty()) {
                        val remainingInMonth = stepInMonth.sumOf { it.remainingBalanceAfter.value }
                        monthlyBalances.add(remainingInMonth)
                    } else {
                        val last = monthlyBalances.lastOrNull() ?: 0L
                        monthlyBalances.add((last * 0.9).toLong())
                    }
                }
                if (monthlyBalances.lastOrNull() != 0L) {
                    monthlyBalances.add(0L)
                }

                val animatedProgress = remember { Animatable(0f) }
                LaunchedEffect(payoffPlan) {
                    animatedProgress.snapTo(0f)
                    animatedProgress.animateTo(1f, animationSpec = tween(1000))
                }

                val primaryColor = FinluxBlue
                val secondaryColor = FinluxCyan

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                ) {
                    if (monthlyBalances.size < 2) return@Canvas
                    val width = size.width
                    val height = size.height
                    val maxBalance = monthlyBalances.maxOrNull()?.coerceAtLeast(1L) ?: 1L

                    val points = monthlyBalances.mapIndexed { index, balance ->
                        val x = (index.toFloat() / (monthlyBalances.size - 1).toFloat()) * width
                        val ratio = (balance.toFloat() / maxBalance.toFloat()).coerceIn(0f, 1f)
                        val y = height - (ratio * (height - 30.dp.toPx())) - 15.dp.toPx()
                        Offset(x, y)
                    }

                    // Đường cong đồ thị (Bezier Curve)
                    val path = Path()
                    val fillPath = Path()

                    path.moveTo(points.first().x, points.first().y)
                    fillPath.moveTo(points.first().x, height)
                    fillPath.lineTo(points.first().x, points.first().y)

                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val controlPoint1 = Offset((p0.x + p1.x) / 2f, p0.y)
                        val controlPoint2 = Offset((p0.x + p1.x) / 2f, p1.y)

                        path.cubicTo(
                            controlPoint1.x, controlPoint1.y,
                            controlPoint2.x, controlPoint2.y,
                            p1.x, p1.y
                        )
                        fillPath.cubicTo(
                            controlPoint1.x, controlPoint1.y,
                            controlPoint2.x, controlPoint2.y,
                            p1.x, p1.y
                        )
                    }

                    fillPath.lineTo(points.last().x, height)
                    fillPath.close()

                    // Vẽ vùng gradient đổ bóng
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.35f * animatedProgress.value),
                                primaryColor.copy(alpha = 0.02f),
                            ),
                            startY = 0f,
                            endY = height,
                        ),
                    )

                    // Vẽ đường nét viền
                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(listOf(primaryColor, secondaryColor)),
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round),
                    )

                    // Vẽ điểm đầu và điểm kết thúc
                    drawCircle(
                        color = primaryColor,
                        radius = 5.dp.toPx(),
                        center = points.first(),
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = points.first(),
                    )

                    drawCircle(
                        color = Color(0xFF10B981),
                        radius = 6.dp.toPx(),
                        center = points.last(),
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.5.dp.toPx(),
                        center = points.last(),
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Hiện tại: ${initialDebtAmount.toShortVnd()}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val debtFreeText = payoffPlan.estimatedDebtFreeDate?.format(DateTimeFormatter.ofPattern("'T'MM/yyyy")) ?: "Hoàn tất"
                    Text(
                        text = "Sạch nợ: $debtFreeText (0 đ)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                        ),
                    )
                }
            }
        }
    }
}
