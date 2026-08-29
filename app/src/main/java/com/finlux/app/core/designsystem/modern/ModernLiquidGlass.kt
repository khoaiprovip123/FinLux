package com.finlux.app.core.designsystem.modern

import com.finlux.app.core.designsystem.*

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.finlux.app.domain.model.CardDensity

/**
 * Visual Effect modes matching @callstack/liquid-glass:
 * - CLEAR: Hyper-translucent, crystal glass with high backdrop aura transmission.
 * - REGULAR: Standard frosted glass material with soft diffusion.
 * - NONE: Completely dematerialized / transparent.
 */
enum class LiquidGlassMode {
    CLEAR,
    REGULAR,
    NONE,
}

val DefaultGlassShape = RoundedCornerShape(24.dp)
val CapsuleGlassShape = RoundedCornerShape(36.dp)

/**
 * Core Liquid Glass surface implementing Callstack Liquid Glass optics in Jetpack Compose:
 * - Chromatic prism refraction rim (White -> Cyan -> Purple -> White)
 * - Multi-stop translucent liquid gradient fill
 * - Dynamic ambient & spot shadow colored by material tint
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    mode: LiquidGlassMode = LiquidGlassMode.CLEAR,
    tint: Color? = null,
    shape: Shape = DefaultGlassShape,
    elevation: Dp = 10.dp,
    padding: PaddingValues? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val preferences = LocalUiPreferences.current
    val tokens = LocalGlassTokens.current
    val resolvedPadding = padding ?: PaddingValues(
        if (preferences.cardDensity == CardDensity.COMPACT) 12.dp else 16.dp,
    )
    if (mode == LiquidGlassMode.NONE) {
        Box(modifier = modifier) {
            Box(modifier = Modifier.padding(resolvedPadding), content = content)
        }
        return
    }

    val dark = MaterialTheme.colorScheme.background.luminance() < 0.4f
    val supportsRealtimeEffects = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val surfaceFill = when (mode) {
        LiquidGlassMode.CLEAR -> {
            if (tint != null) {
                if (dark) {
                    Brush.linearGradient(
                        listOf(
                            tint.copy(alpha = if (supportsRealtimeEffects) 0.42f else 0.62f),
                            Color(0x730A1526),
                            tint.copy(alpha = 0.18f),
                        ),
                    )
                } else {
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = if (supportsRealtimeEffects) 0.58f else 0.76f),
                            tint.copy(alpha = 0.16f),
                            Color.White.copy(alpha = if (supportsRealtimeEffects) 0.42f else 0.62f),
                        ),
                    )
                }
            } else {
                tokens.fill
            }
        }
        LiquidGlassMode.REGULAR -> {
            if (dark) {
                Brush.linearGradient(
                    listOf(
                        Color(0x951C2C4E),
                        Color(0x75101C32),
                        tokens.accent.copy(alpha = .24f),
                    ),
                )
            } else {
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.85f),
                        Color.White.copy(alpha = 0.68f),
                        tokens.accent.copy(alpha = .18f),
                    ),
                )
            }
        }
        LiquidGlassMode.NONE -> Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
    }

    val rimBorder = BorderStroke(
        width = 1.2.dp,
        brush = if (tint != null) {
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = if (dark) 0.65f else 0.95f),
                    FinluxCyan.copy(alpha = 0.48f),
                    tint.copy(alpha = 0.46f),
                    Color.White.copy(alpha = if (dark) 0.15f else 0.35f),
                ),
            )
        } else {
            tokens.border
        },
    )

    val ambientGlow = tint ?: tokens.accent
    val shadowAmbientAlpha = if (dark) 0.35f else 0.12f
    val shadowSpotAlpha = if (dark) 0.45f else 0.18f

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = ambientGlow.copy(alpha = shadowAmbientAlpha),
                spotColor = ambientGlow.copy(alpha = shadowSpotAlpha),
            )
            .clip(shape)
            .background(surfaceFill)
            .border(rimBorder, shape),
    ) {
        // Optical layers remain behind content: text and icons are never blurred or tinted.
        // Decorative optical layers must not participate in measuring wrap-content cards.
        // fillMaxSize() can consume a loose Scaffold topBar height and stretch a compact
        // glass header across the screen; matchParentSize() follows the content-measured box.
        Box(modifier = Modifier.matchParentSize().background(surfaceFill))
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            (tint ?: tokens.accent).copy(alpha = if (dark) .16f else .11f),
                            Color.Transparent,
                        ),
                        center = Offset(80f, 28f),
                        radius = 420f,
                    ),
                ),
        )
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
        this
    }

/**
 * Interactive Liquid Glass Card with spring physics haptic feel.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    mode: LiquidGlassMode = LiquidGlassMode.CLEAR,
    tint: Color? = null,
    shape: Shape = DefaultGlassShape,
    elevation: Dp = 10.dp,
    padding: PaddingValues? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val preferences = LocalUiPreferences.current
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && preferences.animationsEnabled) 0.975f else 1f,
        animationSpec = spring(stiffness = 650f, dampingRatio = 0.72f),
        label = "glass-card-press",
    )
    val interactive = if (onClick != null || onLongClick != null) {
        Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {
                onClick?.let {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    it()
                }
            },
            onLongClick = onLongClick?.let { action ->
                {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    action()
                }
            },
        )
    } else Modifier

    LiquidGlassSurface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(interactive),
        mode = mode,
        tint = tint,
        shape = shape,
        elevation = elevation,
        padding = padding,
        content = content,
    )
}

/**
 * Liquid Glass Pill / Capsule component for filter chips, active badges, status indicators.
 */
@Composable
fun LiquidGlassCapsule(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val preferences = LocalUiPreferences.current
    val haptics = LocalHapticFeedback.current
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.4f
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && preferences.animationsEnabled) 0.95f else 1f,
        animationSpec = spring(stiffness = 650f, dampingRatio = 0.70f),
        label = "capsule-press",
    )

    val capsuleFill = if (selected) {
        if (dark) {
            Brush.linearGradient(
                listOf(
                    accentColor.copy(alpha = 0.42f),
                    Color(0x301E3558),
                    accentColor.copy(alpha = 0.25f),
                ),
            )
        } else {
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.88f),
                    accentColor.copy(alpha = 0.20f),
                    Color.White.copy(alpha = 0.65f),
                ),
            )
        }
    } else {
        if (dark) {
            Brush.linearGradient(
                listOf(
                    Color(0x40182846),
                    Color(0x250C172B),
                ),
            )
        } else {
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.50f),
                    Color.White.copy(alpha = 0.25f),
                ),
            )
        }
    }

    val rimBrush = if (selected) {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = if (dark) 0.75f else 0.98f),
                accentColor.copy(alpha = 0.60f),
                Color.White.copy(alpha = if (dark) 0.20f else 0.40f),
            ),
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = if (dark) 0.35f else 0.70f),
                Color.White.copy(alpha = 0.10f),
            ),
        )
    }

    val shape = CapsuleGlassShape
    val clickModifier = if (onClick != null) {
        Modifier.clickable(interactionSource = interactionSource, indication = null) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        }
    } else Modifier

    Box(
        modifier = modifier
            .heightIn(min = 44.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (selected) 8.dp else 4.dp,
                shape = shape,
                ambientColor = (if (selected) accentColor else Color.Black).copy(alpha = if (dark) 0.25f else 0.10f),
                spotColor = (if (selected) accentColor else Color.Black).copy(alpha = if (dark) 0.35f else 0.14f),
            )
            .clip(shape)
            .background(capsuleFill)
            .border(BorderStroke(1.1.dp, rimBrush), shape)
            .then(clickModifier)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            content = content,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassTopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 4.dp),
    ) {
        LiquidGlassSurface(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            mode = LiquidGlassMode.CLEAR,
            shape = RoundedCornerShape(28.dp),
            elevation = 8.dp,
            padding = PaddingValues(0.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) { navigationIcon() }
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) { title() }
                Row(
                    modifier = Modifier.widthIn(min = 48.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions,
                )
            }
        }
    }
}

/**
 * Floating Liquid Glass Dock (Capsule Navigation Bar).
 */
@Composable
fun GlassBottomNav(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val dockShape = RoundedCornerShape(36.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        LiquidGlassSurface(
            modifier = Modifier.fillMaxWidth().height(66.dp),
            mode = LiquidGlassMode.REGULAR,
            shape = dockShape,
            elevation = 14.dp,
            padding = PaddingValues(0.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

/**
 * Liquid Glass FAB (+) Orb Button.
 */
@Composable
fun GlassFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val preferences = LocalUiPreferences.current
    val tokens = LocalGlassTokens.current
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && preferences.animationsEnabled) 0.90f else 1f,
        animationSpec = spring(stiffness = 650f, dampingRatio = 0.65f),
        label = "glass-fab-press",
    )

    val orbGradient = Brush.linearGradient(
        listOf(tokens.accent, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary),
    )

    Box(
        modifier = modifier
            .size(52.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = tokens.accent.copy(alpha = .28f),
                spotColor = MaterialTheme.colorScheme.tertiary.copy(alpha = .34f),
            )
            .clip(CircleShape)
            .background(orbGradient)
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.85f),
                        Color.White.copy(alpha = 0.25f),
                    ),
                ),
                shape = CircleShape,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassBottomSheet(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.4f
    val containerBg = if (dark) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = containerBg,
        contentColor = MaterialTheme.colorScheme.onSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        content = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
            ) {
                content()
            }
        },
    )
}

@Composable
fun GlassDialogSurface(content: @Composable BoxScope.() -> Unit) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.4f
    val bgFill = if (dark) Color(0xFF121E33).copy(alpha = 0.98f) else Color(0xFFF8FAFD).copy(alpha = 0.98f)
    val rimBorder = BorderStroke(
        width = 1.2.dp,
        brush = Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = if (dark) 0.60f else 0.95f),
                FinluxCyan.copy(alpha = 0.45f),
                FinluxPurple.copy(alpha = 0.35f),
                Color.White.copy(alpha = if (dark) 0.15f else 0.40f),
            ),
        ),
    )
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .shadow(
                elevation = 24.dp,
                shape = shape,
                ambientColor = Color(0xFF3B82F6).copy(alpha = 0.30f),
                spotColor = Color(0xFF6366F1).copy(alpha = 0.30f),
            )
            .clip(shape)
            .background(bgFill)
            .border(rimBorder, shape)
            .padding(20.dp),
        content = content,
    )
}

/** Dialog host that guarantees the same glass material and contrast across the app. */
@Composable
fun GlassDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        GlassDialogSurface {
            Box(modifier = modifier.fillMaxWidth(), content = content)
        }
    }
}

/** Material-compatible alert content rendered on the shared regular glass surface. */
@Composable
fun GlassAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
) {
    GlassDialog(onDismissRequest = onDismissRequest, modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            icon?.invoke()
            title?.invoke()
            text?.invoke()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                dismissButton?.invoke()
                confirmButton()
            }
        }
    }
}

/**
 * iOS 26 Liquid Glass container pattern that batches glass children to optimize performance,
 * coordinate spacing, and avoid redundant render layers across multiple sibling glass elements.
 */
@Composable
fun GlassEffectContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(DefaultGlassShape),
        content = content,
    )
}
