package com.finlux.app.core.designsystem

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import com.finlux.app.domain.model.VisualStyle

/**
 * Color field placed below transparent glass. The blobs themselves are hardware blurred, so the
 * material above them retains a frosted, spatial look without blurring text or interactive content.
 */
@Composable
fun LiquidAuraBackdrop(modifier: Modifier = Modifier) {
    val dark = MaterialTheme.colorScheme.background.luminance() < .4f
    val preferences = LocalUiPreferences.current
    val transition = rememberInfiniteTransition(label = "liquid-aura")
    val driftX by transition.animateFloat(
        initialValue = -18f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(tween(9_000), RepeatMode.Reverse),
        label = "aura-x",
    )
    val driftY by transition.animateFloat(
        initialValue = -24f,
        targetValue = 38f,
        animationSpec = infiniteRepeatable(tween(11_000), RepeatMode.Reverse),
        label = "aura-y",
    )
    val motion = if (preferences.animationsEnabled) 1f else 0f
    Box(
        modifier.fillMaxSize().background(
            Brush.verticalGradient(
                if (dark) {
                    listOf(Color(0xFF07101F), Color(0xFF10162B), Color(0xFF07111D))
                } else {
                    listOf(Color(0xFFF2F7FF), Color(0xFFF9F5FF), Color(0xFFECF9FF))
                },
            ),
        ),
    ) {
        AuraBlob(
            color = FinluxPurple.copy(alpha = if (dark) .34f else .25f),
            size = 330,
            x = 178 + driftX * motion,
            y = -92 + driftY * motion,
        )
        AuraBlob(
            color = FinluxCyan.copy(alpha = if (dark) .28f else .30f),
            size = 300,
            x = -115 - driftX * .55f * motion,
            y = 360 + driftY * .35f * motion,
        )
        AuraBlob(
            color = FinluxBlue.copy(alpha = if (dark) .24f else .18f),
            size = 280,
            x = 175 + driftX * .4f * motion,
            y = 760 - driftY * .45f * motion,
        )
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(listOf(Color.White.copy(alpha = .2f), Color.Transparent)),
                radius = size.minDimension * .32f,
                center = Offset(size.width * .18f, size.height * .12f),
            )
        }
    }
}

@Composable
private fun BoxScope.AuraBlob(color: Color, size: Int, x: Float, y: Float) {
    Box(
        Modifier
            .offset { androidx.compose.ui.unit.IntOffset(x.roundToInt(), y.roundToInt()) }
            .size(size.dp)
            .blur(74.dp)
            .background(color, CircleShape),
    )
}

/**
 * Transparent multi-layer material for the iOS 26-inspired home screen. It combines a low-alpha
 * tint, bright refractive rim, inner water highlight and organic press response.
 */
@Composable
fun WaterGlassCard(
    modifier: Modifier = Modifier,
    tint: Color = FinluxBlue,
    onClick: (() -> Unit)? = null,
    padding: PaddingValues = PaddingValues(16.dp),
    cornerRadius: Int = 26,
    content: @Composable BoxScope.() -> Unit,
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < .4f
    val preferences = LocalUiPreferences.current
    val style = preferences.visualStyle
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed && preferences.animationsEnabled) .968f else 1f,
        spring(dampingRatio = .68f, stiffness = 520f),
        label = "water-glass-press",
    )
    val glassShape = RoundedCornerShape(cornerRadius.dp)
    val interactive = if (onClick == null) Modifier else Modifier.clickable(
        interactionSource = interaction,
        indication = null,
        onClick = onClick,
    )
    Box(
        modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = 18.dp.toPx()
                shape = glassShape
                clip = false
                ambientShadowColor = tint.copy(alpha = .22f)
                spotShadowColor = tint.copy(alpha = .18f)
            }
            .clip(glassShape)
            .background(waterGlassFill(style, dark, tint))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = when (style) {
                            VisualStyle.MODERN_DARK -> .20f
                            VisualStyle.GLASSMORPHISM -> .72f
                            VisualStyle.DYNAMIC_GRADIENT -> if (dark) .68f else .94f
                        }),
                        tint.copy(alpha = if (style == VisualStyle.GLASSMORPHISM) .52f else .36f),
                        Color.White.copy(alpha = if (style == VisualStyle.MODERN_DARK) .04f else .16f),
                    ),
                ),
                shape = glassShape,
            )
            .drawWithCache {
                val topGlow = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = if (dark) .20f else .62f), Color.Transparent),
                    center = Offset(size.width * .18f, 0f),
                    radius = size.width * .62f,
                )
                val bottomTint = Brush.radialGradient(
                    colors = listOf(tint.copy(alpha = .16f), Color.Transparent),
                    center = Offset(size.width * .88f, size.height * 1.08f),
                    radius = size.width * .58f,
                )
                onDrawWithContent {
                    drawRoundRect(topGlow, cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.dp.toPx()))
                    drawRoundRect(bottomTint, cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.dp.toPx()))
                    drawContent()
                    drawOval(
                        color = Color.White.copy(alpha = if (dark) .08f else .20f),
                        topLeft = Offset(size.width * .06f, size.height * .03f),
                        size = Size(size.width * .46f, 2.dp.toPx()),
                        blendMode = BlendMode.Screen,
                    )
                }
            }
            .then(interactive)
            .padding(padding),
    ) {
        val contentColor = if (style == VisualStyle.DYNAMIC_GRADIENT && !dark) {
            MaterialTheme.colorScheme.onSurface
        } else {
            Color.White
        }
        CompositionLocalProvider(LocalContentColor provides contentColor) { content() }
    }
}

private fun waterGlassFill(style: VisualStyle, dark: Boolean, tint: Color): Brush = when (style) {
    VisualStyle.MODERN_DARK -> Brush.linearGradient(
        listOf(Color(0xEE07172A), tint.copy(alpha = .06f), Color(0xF0051222)),
    )
    VisualStyle.GLASSMORPHISM -> Brush.linearGradient(
        listOf(Color.White.copy(alpha = .22f), tint.copy(alpha = .16f), Color.White.copy(alpha = .08f)),
    )
    VisualStyle.DYNAMIC_GRADIENT -> Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = if (dark) .12f else .72f),
            tint.copy(alpha = if (dark) .12f else .09f),
            Color.White.copy(alpha = if (dark) .06f else .42f),
        ),
    )
}
