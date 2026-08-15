package com.finlux.app.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.finlux.app.domain.model.VisualStyle

@Composable
fun FinluxStyleBackdrop(
    modifier: Modifier = Modifier,
    auth: Boolean = false,
) {
    when (LocalUiPreferences.current.visualStyle) {
        VisualStyle.MODERN_DARK -> ModernDarkBackdrop(modifier)
        VisualStyle.GLASSMORPHISM -> LiquidAuraBackdrop(modifier)
        VisualStyle.DYNAMIC_GRADIENT -> DynamicGradientBackdrop(modifier, auth)
    }
}

@Composable
private fun ModernDarkBackdrop(modifier: Modifier) {
    Box(
        modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF020B19), Color(0xFF031329), Color(0xFF020A16))),
        ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            repeat(7) { index ->
                val y = size.height * (.64f + index * .025f)
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, y)
                    cubicTo(size.width * .28f, y - 90f - index * 6f, size.width * .62f, y + 100f, size.width, y - 38f)
                }
                drawPath(path, Color(0xFF168BFF).copy(alpha = .16f - index * .014f), style = androidx.compose.ui.graphics.drawscope.Stroke(1.2f))
            }
            drawCircle(
                brush = Brush.radialGradient(listOf(Color(0xFF006BFF).copy(alpha = .16f), Color.Transparent)),
                radius = size.width * .65f,
                center = Offset(size.width * .60f, size.height * .50f),
            )
        }
    }
}

@Composable
private fun DynamicGradientBackdrop(modifier: Modifier, auth: Boolean) {
    val colors = if (auth) {
        listOf(Color(0xFF922EFF), Color(0xFF5A22FF), Color(0xFF168BFF), Color(0xFF18D4C2))
    } else {
        listOf(Color(0xFFF9FBFF), Color(0xFFF3F1FF), Color(0xFFEFFAFF), Color.White)
    }
    Box(modifier.fillMaxSize().background(Brush.linearGradient(colors))) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color.White.copy(alpha = if (auth) .22f else .54f), Color.Transparent),
                ),
                radius = size.minDimension * .55f,
                center = Offset(size.width * .18f, size.height * .15f),
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(FinluxCyan.copy(alpha = .16f), Color.Transparent)),
                radius = size.minDimension * .48f,
                center = Offset(size.width * .90f, size.height * .76f),
            )
        }
    }
}
