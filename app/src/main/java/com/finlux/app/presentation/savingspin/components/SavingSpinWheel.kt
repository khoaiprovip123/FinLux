package com.finlux.app.presentation.savingspin.components

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.finlux.app.core.designsystem.LocalUiPreferences
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.Money
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SavingSpinWheel(
    values: List<Money>,
    selectedIndex: Int?,
    isSpinning: Boolean,
    modifier: Modifier = Modifier,
    onAnimationFinished: () -> Unit = {},
) {
    if (values.isEmpty()) return
    val tokens = LocalFinluxTokens.current
    val animationsEnabled = LocalUiPreferences.current.animationsEnabled
    val labels = remember(values) { values.map { formatVndAmount(it.value, isCompact = true) } }
    val labelPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
    }
    val rotation = remember { Animatable(0f) }
    val sliceAngle = 360f / values.size

    LaunchedEffect(selectedIndex, isSpinning, animationsEnabled, values.size) {
        val index = selectedIndex ?: return@LaunchedEffect
        val landing = -(index * sliceAngle + sliceAngle / 2f)
        val normalized = ((rotation.value % 360f) + 360f) % 360f
        val landingNormalized = ((landing % 360f) + 360f) % 360f
        val forwardDelta = (landingNormalized - normalized + 360f) % 360f
        val target = rotation.value + forwardDelta + if (animationsEnabled) 5f * 360f else 0f
        if (animationsEnabled) {
            rotation.animateTo(target, tween(durationMillis = 2_400, easing = FastOutSlowInEasing))
        } else {
            rotation.snapTo(target)
        }
        onAnimationFinished()
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotation.value },
        ) {
            val strokeWidth = size.minDimension * .018f
            val inset = strokeWidth
            val diameter = size.minDimension - inset * 2f
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)
            val segmentColors = tokens.heroGradient
            labelPaint.color = tokens.onHero.toArgb()
            labelPaint.textSize = size.minDimension * .052f

            values.indices.forEach { index ->
                drawArc(
                    color = segmentColors[index % segmentColors.size],
                    startAngle = -90f + index * sliceAngle,
                    sweepAngle = sliceAngle,
                    useCenter = true,
                    topLeft = topLeft,
                    size = arcSize,
                )
                drawArc(
                    color = tokens.onHero.copy(alpha = .52f),
                    startAngle = -90f + index * sliceAngle,
                    sweepAngle = sliceAngle,
                    useCenter = true,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round),
                )
                val centerAngle = Math.toRadians((-90f + index * sliceAngle + sliceAngle / 2f).toDouble())
                val radius = diameter * .32f
                val x = center.x + cos(centerAngle).toFloat() * radius
                val y = center.y + sin(centerAngle).toFloat() * radius - (labelPaint.ascent() + labelPaint.descent()) / 2f
                drawContext.canvas.nativeCanvas.drawText(labels[index], x, y, labelPaint)
            }

            drawCircle(tokens.surface, radius = diameter * .155f)
            drawCircle(tokens.primary, radius = diameter * .12f)
            drawCircle(tokens.onHero.copy(alpha = .8f), radius = diameter * .035f)
        }

        Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = "Kim chỉ kết quả",
            tint = tokens.onSurface,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(42.dp),
        )
    }
}
