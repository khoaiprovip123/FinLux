package com.finlux.app.core.designsystem

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.LocalContentColor
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.finlux.app.domain.model.CardDensity
import com.finlux.app.domain.model.VisualStyle

private val GlassShape = RoundedCornerShape(20.dp)

/**
 * Base material for every glass component in UI_SPEC section 0. The alpha and border come from
 * theme tokens, so light/dark behavior cannot diverge between feature screens.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = GlassShape,
    padding: PaddingValues? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val tokens = LocalGlassTokens.current
    val preferences = LocalUiPreferences.current
    val resolvedPadding = padding ?: PaddingValues(
        if (preferences.cardDensity == CardDensity.COMPACT) 12.dp else 16.dp,
    )
    Box(
        modifier = modifier
            .shadow(9.dp, shape, ambientColor = tokens.shadow, spotColor = tokens.shadow)
            .clip(shape)
            .border(BorderStroke(1.dp, tokens.border), shape)
            .graphicsLayer { alpha = .999f }
            .then(Modifier),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = .999f }
                .then(Modifier)
                .border(0.dp, Color.Transparent, shape),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .then(Modifier),
        ) {
            androidx.compose.foundation.Canvas(Modifier.matchParentSize()) {
                drawRect(tokens.fill)
                if (tokens.glow.alpha > 0f) drawCircle(tokens.glow, radius = size.minDimension * .8f)
            }
        }
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            Box(modifier = Modifier.padding(resolvedPadding), content = content)
        }
    }
}

/** Blur the scrolling background layer that sits behind glass chrome on Android 12+. */
fun Modifier.finluxBackgroundBlur(radius: Dp = 18.dp): Modifier =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        graphicsLayer {
            renderEffect = RenderEffect.createBlurEffect(
                radius.toPx(),
                radius.toPx(),
                Shader.TileMode.CLAMP,
            ).asComposeRenderEffect()
        }
    } else {
        // UI_SPEC requires a static translucent fallback; LiquidGlassSurface supplies it.
        this
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val preferences = LocalUiPreferences.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && preferences.animationsEnabled) .975f else 1f,
        animationSpec = spring(stiffness = 650f, dampingRatio = .72f),
        label = "glass-card-press",
    )
    val interactive = if (onClick != null || onLongClick != null) {
        Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick ?: {},
            onLongClick = onLongClick,
        )
    } else Modifier
    LiquidGlassSurface(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }.then(interactive),
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassTopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {},
) {
    TopAppBar(
        modifier = modifier.statusBarsPadding(),
        title = title,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
    )
}

@Composable
fun GlassBottomNav(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    val tokens = LocalGlassTokens.current
    val style = LocalUiPreferences.current.visualStyle
    val navColor = when (style) {
        VisualStyle.MODERN_DARK -> Color(0xF207172A)
        VisualStyle.GLASSMORPHISM -> Color(0xB83C4F86)
        VisualStyle.DYNAMIC_GRADIENT -> MaterialTheme.colorScheme.surface.copy(alpha = .94f)
    }
    BottomAppBar(
        modifier = modifier
            .shadow(12.dp, RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp), ambientColor = tokens.shadow)
            .border(BorderStroke(1.dp, tokens.border), RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)),
        containerColor = navColor,
        content = content,
    )
}

@Composable
fun GlassFab(onClick: () -> Unit, content: @Composable () -> Unit) {
    val style = LocalUiPreferences.current.visualStyle
    val gradient = when (style) {
        VisualStyle.MODERN_DARK -> Brush.linearGradient(listOf(Color(0xFF075DB8), Color(0xFF13A3FF)))
        VisualStyle.GLASSMORPHISM -> Brush.linearGradient(listOf(Color(0xFF6B43E8), Color(0xFF9B66FF), Color(0xFF42C8FF)))
        VisualStyle.DYNAMIC_GRADIENT -> Brush.linearGradient(listOf(Color(0xFF8C2CFF), Color(0xFF4C55FF), Color(0xFF24C9DD)))
    }
    val rim = if (style == VisualStyle.GLASSMORPHISM) Color.White.copy(alpha = .68f) else Color.White.copy(alpha = .28f)
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.background(gradient, CircleShape).border(1.dp, rim, CircleShape),
        shape = CircleShape,
        containerColor = Color.Transparent,
        contentColor = Color.White,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassBottomSheet(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.4f
    val containerBg = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = containerBg,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        content = { content() },
    )
}

@Composable
fun GlassDialogSurface(content: @Composable BoxScope.() -> Unit) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.4f
    val bgFill = if (isDark) Color(0xFF141F32).copy(alpha = 0.98f) else Color(0xFFF9FAFD).copy(alpha = 0.98f)
    val borderBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isDark) 0.35f else 0.90f),
            FinluxBlue.copy(alpha = if (isDark) 0.20f else 0.30f),
            Color.White.copy(alpha = if (isDark) 0.10f else 0.40f),
        ),
    )
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .shadow(elevation = 24.dp, shape = shape, ambientColor = FinluxBlue.copy(alpha = 0.25f), spotColor = FinluxPurple.copy(alpha = 0.25f))
            .clip(shape)
            .background(bgFill)
            .border(1.2.dp, borderBrush, shape)
            .padding(20.dp),
        content = content,
    )
}
