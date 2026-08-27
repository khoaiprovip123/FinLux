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

import com.finlux.app.core.designsystem.theme.LocalFinluxTokens

@Composable
fun FinluxStyleBackdrop(
    modifier: Modifier = Modifier,
    auth: Boolean = false,
) {
    val tokens = LocalFinluxTokens.current
    if (!tokens.isDark) {
        when (LocalUiPreferences.current.visualStyle) {
            VisualStyle.MODERN_DARK -> LiquidAuraBackdrop(modifier)
            VisualStyle.GLASSMORPHISM -> LiquidAuraBackdrop(modifier)
            VisualStyle.DYNAMIC_GRADIENT -> DynamicGradientBackdrop(modifier, auth = auth, isDark = false)
        }
    } else {
        when (LocalUiPreferences.current.visualStyle) {
            VisualStyle.MODERN_DARK -> ModernDarkBackdrop(modifier)
            VisualStyle.GLASSMORPHISM -> LiquidAuraBackdrop(modifier)
            VisualStyle.DYNAMIC_GRADIENT -> DynamicGradientBackdrop(modifier, auth = auth, isDark = true)
        }
    }
}

@Composable
private fun ModernDarkBackdrop(modifier: Modifier) {
    Box(
        modifier = modifier.fillMaxSize().background(
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
private fun DynamicGradientBackdrop(
    modifier: Modifier,
    auth: Boolean,
    isDark: Boolean,
) {
    val colors = if (auth) {
        listOf(Color(0xFF922EFF), Color(0xFF5A22FF), Color(0xFF168BFF), Color(0xFF18D4C2))
    } else if (isDark) {
        listOf(Color(0xFF07101F), Color(0xFF10162B), Color(0xFF07111D))
    } else {
        listOf(Color(0xFFF2F7FF), Color(0xFFF9F5FF), Color(0xFFECF9FF))
    }
    Box(modifier = modifier.fillMaxSize().background(Brush.linearGradient(colors))) {
        Canvas(Modifier.fillMaxSize()) {
            if (isDark) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFF6F52F5).copy(alpha = if (auth) 0.25f else 0.18f), Color.Transparent),
                    ),
                    radius = size.minDimension * 0.55f,
                    center = Offset(size.width * 0.18f, size.height * 0.15f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(FinluxCyan.copy(alpha = if (auth) 0.20f else 0.14f), Color.Transparent),
                    ),
                    radius = size.minDimension * 0.48f,
                    center = Offset(size.width * 0.90f, size.height * 0.76f),
                )
            } else {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color.White.copy(alpha = if (auth) 0.22f else 0.54f), Color.Transparent),
                    ),
                    radius = size.minDimension * 0.55f,
                    center = Offset(size.width * 0.18f, size.height * 0.15f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(FinluxCyan.copy(alpha = 0.12f), Color.Transparent),
                    ),
                    radius = size.minDimension * 0.48f,
                    center = Offset(size.width * 0.90f, size.height * 0.76f),
                )
            }
        }
    }
}
