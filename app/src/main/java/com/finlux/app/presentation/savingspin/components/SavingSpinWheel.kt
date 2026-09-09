package com.finlux.app.presentation.savingspin.components

import android.graphics.Paint
import android.graphics.Path
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.finlux.app.core.designsystem.LocalUiPreferences
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.domain.model.Money
import kotlin.math.cos
import kotlin.math.sin

private val DISTINCT_PALETTE = listOf(
    Color(0xFF0EA5E9), // Sky Blue
    Color(0xFF3B82F6), // Royal Blue
    Color(0xFF8B5CF6), // Purple
    Color(0xFFEC4899), // Pink
    Color(0xFFF43F5E), // Rose
    Color(0xFFF97316), // Orange
    Color(0xFFEAB308), // Yellow Amber
    Color(0xFF84CC16), // Lime
    Color(0xFF10B981), // Emerald
    Color(0xFF06B6D4), // Cyan
    Color(0xFF6366F1), // Indigo
    Color(0xFFA855F7), // Fuchsia
)

@Composable
fun SavingSpinWheel(
    values: List<Money>,
    selectedIndex: Int?,
    isSpinning: Boolean,
    modifier: Modifier = Modifier,
    onAnimationFinished: () -> Unit = {},
) {
    if (values.isEmpty()) return
    val animationsEnabled = LocalUiPreferences.current.animationsEnabled
    val labels = remember(values) { values.map { formatVndAmount(it.value, isCompact = true) } }
    val colors = remember(values.size) {
        if (values.size <= DISTINCT_PALETTE.size) {
            DISTINCT_PALETTE.take(values.size)
        } else {
            List(values.size) { DISTINCT_PALETTE[it % DISTINCT_PALETTE.size] }
        }
    }

    val labelPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            color = android.graphics.Color.WHITE
            setShadowLayer(4f, 0f, 2f, android.graphics.Color.argb(120, 0, 0, 0))
        }
    }

    val rotation = remember { Animatable(0f) }
    val sliceAngle = 360f / values.size

    LaunchedEffect(selectedIndex, isSpinning, animationsEnabled, values.size) {
        val index = selectedIndex ?: return@LaunchedEffect
        // Kim chỉ ở vị trí 12 giờ (-90 độ), target landing tại tâm của slice
        val landing = -(index * sliceAngle + sliceAngle / 2f)
        val normalized = ((rotation.value % 360f) + 360f) % 360f
        val landingNormalized = ((landing % 360f) + 360f) % 360f
        val forwardDelta = (landingNormalized - normalized + 360f) % 360f
        val target = rotation.value + forwardDelta + if (animationsEnabled) 6f * 360f else 0f
        if (animationsEnabled) {
            rotation.animateTo(target, tween(durationMillis = 3_200, easing = FastOutSlowInEasing))
        } else {
            rotation.snapTo(target)
        }
        onAnimationFinished()
    }

    val accessibleDesc = "Bánh xe vòng quay tiết kiệm ${values.size} ô mệnh giá"

    Box(
        modifier = modifier.semantics { contentDescription = accessibleDesc },
        contentAlignment = Alignment.Center,
    ) {
        // Vòng quay Canvas (quay theo góc rotation)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotation.value },
        ) {
            val minDim = size.minDimension
            val rimThickness = minDim * 0.055f
            val diameter = minDim - rimThickness * 2f
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            // Adaptive font size based on slotCount
            labelPaint.textSize = when {
                values.size >= 12 -> minDim * 0.038f
                values.size >= 10 -> minDim * 0.044f
                else -> minDim * 0.052f
            }

            // 1. Vẽ các múi màu
            values.indices.forEach { index ->
                val sliceColor = colors[index % colors.size]
                val startAngle = -90f + index * sliceAngle

                // Múi quạt màu chính
                drawArc(
                    color = sliceColor,
                    startAngle = startAngle,
                    sweepAngle = sliceAngle,
                    useCenter = true,
                    topLeft = topLeft,
                    size = arcSize,
                )

                // Đường viền phân cách giữa các múi
                drawArc(
                    color = Color.White.copy(alpha = 0.65f),
                    startAngle = startAngle,
                    sweepAngle = sliceAngle,
                    useCenter = true,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = minDim * 0.012f),
                )

                // Vẽ text mệnh giá dạng xoay tròn
                val centerAngle = Math.toRadians((startAngle + sliceAngle / 2f).toDouble())
                val textRadius = diameter * 0.35f
                val x = center.x + cos(centerAngle).toFloat() * textRadius
                val y = center.y + sin(centerAngle).toFloat() * textRadius

                drawContext.canvas.nativeCanvas.save()
                drawContext.canvas.nativeCanvas.rotate(
                    (startAngle + sliceAngle / 2f + 90f),
                    x,
                    y,
                )
                val textYOffset = (labelPaint.descent() + labelPaint.ascent()) / 2f
                drawContext.canvas.nativeCanvas.drawText(labels[index], x, y - textYOffset, labelPaint)
                drawContext.canvas.nativeCanvas.restore()
            }

            // 2. Viền ngoài bằng vàng kim (Gold rim)
            drawCircle(
                color = Color(0xFFF59E0B),
                radius = diameter / 2f + rimThickness / 2f,
                style = Stroke(width = rimThickness),
            )
            drawCircle(
                color = Color(0xFFFEF3C7),
                radius = diameter / 2f,
                style = Stroke(width = 2f),
            )

            // Điểm hạt ngọc trang trí trên viền vàng
            val dotCount = 24
            val dotRadius = minDim * 0.009f
            for (i in 0 until dotCount) {
                val dotAngle = Math.toRadians((i * 360.0 / dotCount))
                val dX = center.x + cos(dotAngle).toFloat() * (diameter / 2f + rimThickness / 2f)
                val dY = center.y + sin(dotAngle).toFloat() * (diameter / 2f + rimThickness / 2f)
                drawCircle(color = Color.White, radius = dotRadius, center = Offset(dX, dY))
            }

            // 3. Khối tròn trung tâm (Gold medallion + Star)
            val centerRadius = diameter * 0.17f
            drawCircle(
                color = Color(0xFFFBBF24),
                radius = centerRadius,
            )
            drawCircle(
                color = Color(0xFFF59E0B),
                radius = centerRadius,
                style = Stroke(width = minDim * 0.016f),
            )
            drawCircle(
                color = Color(0xFFFEF08A),
                radius = centerRadius * 0.78f,
            )

            // Vẽ ngôi sao vàng ở tâm
            drawStar(
                cx = center.x,
                cy = center.y,
                radius = centerRadius * 0.55f,
                innerRadius = centerRadius * 0.25f,
                color = Color(0xFFD97706),
            )
        }

        // Kim chỉ cố định ở đỉnh 12h (Top Center) hướng xuống dưới
        Canvas(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(width = 28.dp, height = 30.dp)
                .offset(y = (-4).dp),
        ) {
            val path = Path().apply {
                moveTo(size.width / 2f, size.height)
                lineTo(0f, 0f)
                lineTo(size.width, 0f)
                close()
            }
            // Bóng kim
            drawContext.canvas.nativeCanvas.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.argb(60, 0, 0, 0)
                maskFilter = android.graphics.BlurMaskFilter(4f, android.graphics.BlurMaskFilter.Blur.NORMAL)
            })
            // Thân kim đỏ tươi
            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(size.width / 2f, size.height)
                    lineTo(0f, 0f)
                    lineTo(size.width, 0f)
                    close()
                },
                color = Color(0xFFEF4444),
            )
            // Viền kim trắng
            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(size.width / 2f, size.height)
                    lineTo(0f, 0f)
                    lineTo(size.width, 0f)
                    close()
                },
                color = Color.White,
                style = Stroke(width = 2.5f),
            )
        }
    }
}

private fun DrawScope.drawStar(
    cx: Float,
    cy: Float,
    radius: Float,
    innerRadius: Float,
    color: Color,
) {
    val starPath = androidx.compose.ui.graphics.Path()
    val points = 5
    var angle = -Math.PI / 2.0
    val step = Math.PI / points

    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) radius else innerRadius
        val x = cx + cos(angle).toFloat() * r
        val y = cy + sin(angle).toFloat() * r
        if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
        angle += step
    }
    starPath.close()
    drawPath(starPath, color = color, style = Fill)
}
