package com.finlux.app.core.designsystem.modern

import com.finlux.app.core.designsystem.*

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.shadow
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
fun LiquidAuraBackdrop(
    modifier: Modifier = Modifier,
    auth: Boolean = false,
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < .4f
    val preferences = LocalUiPreferences.current
    val tokens = LocalGlassTokens.current
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
            Brush.verticalGradient(tokens.backdrop),
        ),
    ) {
        AuraBlob(
            color = MaterialTheme.colorScheme.secondary.copy(alpha = if (dark) .34f else .25f),
            size = if (auth) 390 else 330,
            x = 178 + driftX * motion,
            y = -92 + driftY * motion,
        )
        AuraBlob(
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = if (dark) .28f else .30f),
            size = if (auth) 340 else 300,
            x = -115 - driftX * .55f * motion,
            y = 360 + driftY * .35f * motion,
        )
        AuraBlob(
            color = tokens.accent.copy(alpha = if (dark) .24f else .18f),
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

@Composable
fun WaterGlassCard(
    modifier: Modifier = Modifier,
    tint: Color = FinluxBlue,
    onClick: (() -> Unit)? = null,
    padding: PaddingValues = PaddingValues(16.dp),
    cornerRadius: Int = 24,
    content: @Composable BoxScope.() -> Unit,
) {
    GlassCard(
        modifier = modifier,
        mode = LiquidGlassMode.CLEAR,
        tint = tint,
        shape = RoundedCornerShape(cornerRadius.dp),
        elevation = 10.dp,
        padding = padding,
        onClick = onClick,
        content = content,
    )
}

fun waterGlassFill(dark: Boolean, tint: Color): Brush = if (dark) {
    Brush.linearGradient(
        listOf(
            Color(0x85203362),
            Color(0x55121F3E),
            tint.copy(alpha = 0.24f),
        ),
    )
} else {
    Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = 0.72f),
            Color.White.copy(alpha = 0.42f),
            tint.copy(alpha = 0.18f),
        ),
    )
}
