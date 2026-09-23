package com.finlux.app.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.LocalAppUiStyle
import com.finlux.app.core.designsystem.LocalUiPreferences
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.AppUiStyle
import com.finlux.app.domain.model.CardDensity

/**
 * Universal Adaptive Glass Card for Component-driven Architecture.
 * Automatically adapts surface texture, shadow, and border according to [LocalAppUiStyle]:
 * - [AppUiStyle.CLASSIC_LIQUID]: Deep shadow, classic refraction border and glow.
 * - [AppUiStyle.MODERN_LUXURY]: Ultra-smooth diffusion, chromatic rim, and modern elevation.
 * - [AppUiStyle.PRISM]: Bento Frosted Surface, subtle border, and crisp typography.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FinluxAdaptiveCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    shape: Shape? = null,
    border: BorderStroke? = null,
    elevation: Dp? = null,
    padding: PaddingValues? = null,
    tint: Color? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val uiStyle = LocalAppUiStyle.current
    val tokens = LocalFinluxTokens.current
    val preferences = LocalUiPreferences.current

    when (uiStyle) {
        AppUiStyle.CLASSIC_LIQUID -> {
            GlassCard(
                modifier = modifier,
                onClick = onClick,
                onLongClick = onLongClick,
                content = content,
            )
        }
        AppUiStyle.MODERN_LUXURY -> {
            com.finlux.app.core.designsystem.modern.GlassCard(
                modifier = modifier,
                onClick = onClick,
                onLongClick = onLongClick,
                tint = tint,
                content = content,
            )
        }
        AppUiStyle.PRISM -> {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed && preferences.animationsEnabled) 0.98f else 1f,
                animationSpec = spring(stiffness = 650f, dampingRatio = 0.72f),
                label = "prism_card_press",
            )

            val resolvedShape = shape ?: RoundedCornerShape(24.dp)
            val resolvedElevation = elevation ?: 8.dp
            val resolvedPadding = padding ?: PaddingValues(
                if (preferences.cardDensity == CardDensity.COMPACT) 12.dp else 16.dp,
            )
            val resolvedBorder = border ?: BorderStroke(
                width = 1.dp,
                color = tokens.border.copy(alpha = if (tokens.isDark) 0.40f else 0.25f),
            )

            val clickableModifier = if (onClick != null || onLongClick != null) {
                Modifier.combinedClickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true),
                    onClick = onClick ?: {},
                    onLongClick = onLongClick,
                )
            } else {
                Modifier
            }

            Box(
                modifier = modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .shadow(
                        elevation = resolvedElevation,
                        shape = resolvedShape,
                        ambientColor = tokens.border.copy(alpha = if (tokens.isDark) 0.25f else 0.10f),
                        spotColor = tokens.border.copy(alpha = if (tokens.isDark) 0.35f else 0.18f),
                    )
                    .clip(resolvedShape)
                    .background(tokens.surface)
                    .border(resolvedBorder, resolvedShape)
                    .then(clickableModifier),
            ) {
                CompositionLocalProvider(LocalContentColor provides tokens.onSurface) {
                    Box(
                        modifier = Modifier.padding(resolvedPadding),
                        content = content,
                    )
                }
            }
        }
    }
}
