package com.finlux.app.presentation.debt.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import com.finlux.app.core.designsystem.LiquidGlassSurface
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
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
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
        ) {
            EmbeddedDebtBurndownChart(
                payoffPlan = payoffPlan,
                initialDebtAmount = initialDebtAmount,
            )
        }
    }
}

@Composable
fun EmbeddedDebtBurndownChart(
    payoffPlan: DebtPayoffPlan?,
    initialDebtAmount: Long,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    Column(modifier = modifier.fillMaxWidth()) {
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
                        fontSize = 15.5.sp,
                    ),
                    color = tokens.onSurface,
                )
                Text(
                    text = "Mô phỏng số dư nợ theo thời gian",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = tokens.onSurfaceVariant,
                )
            }

            if (payoffPlan != null && payoffPlan.totalMonths > 0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FinluxColors.PrimaryViolet.copy(alpha = 0.14f),
                    border = BorderStroke(0.8.dp, FinluxColors.PrimaryViolet.copy(alpha = 0.35f)),
                ) {
                    Text(
                        text = "${payoffPlan.totalMonths} tháng",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = FinluxColors.PrimaryViolet,
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        if (payoffPlan == null || payoffPlan.totalMonths == 0 || initialDebtAmount <= 0L) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Không có dư nợ hoặc đã tất toán hoàn tất 🎉",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.onSurfaceVariant,
                )
            }
        } else {
            val schedule = payoffPlan.paymentSchedule
            val totalSteps = payoffPlan.totalMonths
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

            val primaryColor = FinluxColors.PrimaryBlue
            val secondaryColor = FinluxColors.PrimaryCyan

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
            ) {
                if (monthlyBalances.size < 2) return@Canvas
                val width = size.width
                val height = size.height
                val maxBalance = monthlyBalances.maxOrNull()?.coerceAtLeast(1L) ?: 1L

                val points = monthlyBalances.mapIndexed { index, balance ->
                    val x = (index.toFloat() / (monthlyBalances.size - 1).toFloat()) * width
                    val ratio = (balance.toFloat() / maxBalance.toFloat()).coerceIn(0f, 1f)
                    val y = height - (ratio * (height - 26.dp.toPx())) - 12.dp.toPx()
                    Offset(x, y)
                }

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

                // Vùng gradient đổ bóng
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.30f * animatedProgress.value),
                            primaryColor.copy(alpha = 0.01f),
                        ),
                        startY = 0f,
                        endY = height,
                    ),
                )

                // Đường nét viền
                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(listOf(primaryColor, secondaryColor)),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                )

                // Điểm bắt đầu
                drawCircle(
                    color = primaryColor,
                    radius = 4.5.dp.toPx(),
                    center = points.first(),
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5.dp.toPx(),
                    center = points.first(),
                )

                // Điểm kết thúc
                drawCircle(
                    color = FinluxColors.IncomeGreen,
                    radius = 5.dp.toPx(),
                    center = points.last(),
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
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
                    color = tokens.onSurfaceVariant,
                )
                val debtFreeText = payoffPlan.estimatedDebtFreeDate?.format(DateTimeFormatter.ofPattern("'T'MM/yyyy")) ?: "Hoàn tất"
                Text(
                    text = "Sạch nợ: $debtFreeText (0 đ)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FinluxColors.IncomeGreen,
                    ),
                )
            }
        }
    }
}
