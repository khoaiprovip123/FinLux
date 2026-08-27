package com.finlux.app.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * FinLux Core Color Tokens (FinLux Prism Spec 2.1)
 */
object FinluxColors {
    // Primary Brand
    val PrimaryBlue = Color(0xFF3A7BFF)
    val PrimaryViolet = Color(0xFF6F52F5)
    val PrimaryCyan = Color(0xFF23C7E8)

    // Semantic Colors
    val IncomeGreen = Color(0xFF20B486)
    val ExpenseRed = Color(0xFFEB5C6E)
    val TransferBlue = Color(0xFF3985F5)
    val BudgetViolet = Color(0xFF7052F5)
    val WarningAmber = Color(0xFFF2A43A)
    val NeutralGray = Color(0xFF7A8496)

    // Surface & Background (Light)
    val BackgroundLight = Color(0xFFF6F8FC)
    val SurfacePrimaryLight = Color(0xFFFFFFFF)
    val SurfaceSoftLight = Color(0xFFF2F5FB)
    val SurfaceGlassLight = Color(0xC7FFFFFF) // rgba(255, 255, 255, 0.78)
    val BorderSoftLight = Color(0x1A5A6EA0)    // rgba(90, 110, 160, 0.10)
    val TextPrimaryLight = Color(0xFF172033)
    val TextSecondaryLight = Color(0xFF768197)

    // Surface & Background (Dark)
    val BackgroundDark = Color(0xFF0E1118)
    val SurfacePrimaryDark = Color(0xFF171B25)
    val SurfaceSoftDark = Color(0xFF1E2430)
    val SurfaceGlassDark = Color(0xC7171B25)
    val BorderSoftDark = Color(0x1AFFFFFF)
    val TextPrimaryDark = Color(0xFFF7F9FC)
    val TextSecondaryDark = Color(0xFFA8B0C0)
}

/**
 * Spacing Token System (FinLux Design System Core Spec 4 — v1.11.0)
 *
 * Quy tắc sử dụng:
 *  - screenHorizontal  → Header/TopBar padding ngang (FinluxScreenHeader, GlassTopBar)
 *  - contentHorizontal → LazyColumn contentPadding ngang (FinluxLazyColumn)
 *  - screenTop         → LazyColumn contentPadding top
 *  - cardGap           → verticalArrangement.spacedBy() trong FinluxLazyColumn
 *  - itemGap           → khoảng cách giữa các item nhỏ trong card
 *  - bottomBarClearance → contentPadding bottom cho 4 Tab chính (TAB_MAIN)
 *  - compactClearance  → contentPadding bottom cho màn hình con (DETAIL)
 *
 *  TUYỆT ĐỐI KHÔNG hardcode các con số padding/spacing rải rác trong màn hình.
 */
@Immutable
data class FinluxSpacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val base: Dp = 16.dp,
    val lg: Dp = 20.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 40.dp,
    // ── Screen-level layout tokens ───────────────────────────────────────────
    /** Padding ngang của Header/TopBar và FinluxScreenHeader */
    val screenHorizontal: Dp = 20.dp,
    /** Padding ngang của nội dung trong LazyColumn (FinluxLazyColumn) */
    val contentHorizontal: Dp = 16.dp,
    /** Padding top của item đầu tiên trong LazyColumn */
    val screenTop: Dp = 8.dp,
    /** Khoảng cách giữa các Section block */
    val sectionGap: Dp = 24.dp,
    /** Khoảng cách verticalArrangement giữa các card/item trong danh sách */
    val cardGap: Dp = 12.dp,
    /** Khoảng cách giữa các item nhỏ bên trong card */
    val itemGap: Dp = 8.dp,
    /** Padding bên trong GlassCard */
    val cardInnerPadding: Dp = 18.dp,
    /** contentPadding bottom cho 4 Tab chính có BottomBar (FinluxListType.TAB_MAIN) */
    val bottomBarClearance: Dp = 96.dp,
    /** contentPadding bottom cho màn hình con/detail (FinluxListType.DETAIL) */
    val compactClearance: Dp = 24.dp,
)

/**
 * Corner Radius Token System (FinLux Prism Spec 5)
 */
@Immutable
data class FinluxRadius(
    val smallChip: Dp = 12.dp,
    val input: Dp = 16.dp,
    val standardCard: Dp = 20.dp,
    val heroCard: Dp = 28.dp,
    val bottomSheet: Dp = 28.dp,
    val dialog: Dp = 28.dp,
    val bottomDock: Dp = 28.dp,
)

/**
 * Motion System Duration Tokens (FinLux Prism Spec 7)
 */
object FinluxMotion {
    const val FastInteractionMs = 140
    const val StandardTransitionMs = 200
    const val SheetDialogMs = 280
    const val ChartAnimationMs = 400
}

/**
 * Design Tokens Contract per UI Style (FinLux Prism Spec 21 & 22)
 */
@Immutable
data class FinluxDesignTokens(
    val background: Color,
    val surface: Color,
    val surfaceSoft: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val primary: Color,
    val primaryGradient: List<Color>,
    val heroGradient: List<Color>,
    val cardRadius: Dp = 20.dp,
    val contentRadius: Dp = 16.dp,
    val glassAlpha: Float = 0.78f,
    val borderAlpha: Float = 0.10f,
    val elevation: Dp = 8.dp,
    val spacing: FinluxSpacing = FinluxSpacing(),
    val radius: FinluxRadius = FinluxRadius(),
    val isDark: Boolean = false,
) {
    val heroBrush: Brush get() = Brush.linearGradient(heroGradient)
    val primaryBrush: Brush get() = Brush.linearGradient(primaryGradient)
    val border: Color get() = if (isDark) FinluxColors.BorderSoftDark else FinluxColors.BorderSoftLight
    val textPrimary: Color get() = onSurface
    val textSecondary: Color get() = onSurfaceVariant
}

// Prism Tokens (Soft Surface + Data-First + Minimal Glass)
val PrismLightTokens = FinluxDesignTokens(
    background = FinluxColors.BackgroundLight,
    surface = FinluxColors.SurfacePrimaryLight,
    surfaceSoft = FinluxColors.SurfaceSoftLight,
    onSurface = FinluxColors.TextPrimaryLight,
    onSurfaceVariant = FinluxColors.TextSecondaryLight,
    primary = FinluxColors.PrimaryBlue,
    primaryGradient = listOf(FinluxColors.PrimaryBlue, FinluxColors.PrimaryViolet),
    heroGradient = listOf(FinluxColors.PrimaryBlue, FinluxColors.PrimaryViolet, FinluxColors.PrimaryCyan),
    cardRadius = 20.dp,
    contentRadius = 16.dp,
    glassAlpha = 0.78f,
    borderAlpha = 0.10f,
    elevation = 6.dp,
    isDark = false,
)

val PrismDarkTokens = FinluxDesignTokens(
    background = FinluxColors.BackgroundDark,
    surface = FinluxColors.SurfacePrimaryDark,
    surfaceSoft = FinluxColors.SurfaceSoftDark,
    onSurface = FinluxColors.TextPrimaryDark,
    onSurfaceVariant = FinluxColors.TextSecondaryDark,
    primary = FinluxColors.PrimaryCyan,
    primaryGradient = listOf(FinluxColors.PrimaryBlue, FinluxColors.PrimaryViolet),
    heroGradient = listOf(Color(0xFF2856B6), Color(0xFF4C36AD), Color(0xFF1B8A9E)),
    cardRadius = 20.dp,
    contentRadius = 16.dp,
    glassAlpha = 0.65f,
    borderAlpha = 0.12f,
    elevation = 8.dp,
    isDark = true,
)

// Classic Liquid Tokens
val ClassicLiquidLightTokens = FinluxDesignTokens(
    background = Color(0xFFF5F7FC),
    surface = Color(0xFFFDFEFF),
    surfaceSoft = Color(0xFFEFF3FA),
    onSurface = Color(0xFF172033),
    onSurfaceVariant = FinluxColors.TextSecondaryLight,
    primary = Color(0xFF3478F6),
    primaryGradient = listOf(Color(0xFF7758F6), Color(0xFF3478F6), Color(0xFF47C8FF)),
    heroGradient = listOf(Color(0xFF7758F6), Color(0xFF3478F6), Color(0xFF47C8FF)),
    cardRadius = 22.dp,
    contentRadius = 16.dp,
    glassAlpha = 0.85f,
    borderAlpha = 0.18f,
    elevation = 10.dp,
    isDark = false,
)

val ClassicLiquidDarkTokens = FinluxDesignTokens(
    background = Color(0xFF090E1A),
    surface = Color(0xFF141C2A),
    surfaceSoft = Color(0xFF202A3B),
    onSurface = Color(0xFFE7F1FA),
    onSurfaceVariant = Color(0xFF94A3B8),
    primary = Color(0xFF47C8FF),
    primaryGradient = listOf(Color(0xFF7758F6), Color(0xFF3478F6), Color(0xFF47C8FF)),
    heroGradient = listOf(Color(0xFF4F39AA), Color(0xFF1E52B3), Color(0xFF22789E)),
    cardRadius = 22.dp,
    contentRadius = 16.dp,
    glassAlpha = 0.70f,
    borderAlpha = 0.20f,
    elevation = 12.dp,
    isDark = true,
)

// Modern Luxury Tokens
val ModernLuxuryLightTokens = FinluxDesignTokens(
    background = Color(0xFFF3F8FE),
    surface = Color(0xFFF9FCFF),
    surfaceSoft = Color(0xFFE7F0FA),
    onSurface = Color(0xFF071A2E),
    onSurfaceVariant = Color(0xFF476078),
    primary = Color(0xFF176BDF),
    primaryGradient = listOf(Color(0xFF176BDF), Color(0xFF0891B2)),
    heroGradient = listOf(Color(0xFF176BDF), Color(0xFF0891B2), Color(0xFF4F46E5)),
    cardRadius = 24.dp,
    contentRadius = 18.dp,
    glassAlpha = 0.80f,
    borderAlpha = 0.14f,
    elevation = 12.dp,
    isDark = false,
)

val ModernLuxuryDarkTokens = FinluxDesignTokens(
    background = Color(0xFF020D1E),
    surface = Color(0xFF08182B),
    surfaceSoft = Color(0xFF0D2038),
    onSurface = Color(0xFFF3F8FF),
    onSurfaceVariant = Color(0xFF8EA3BD),
    primary = Color(0xFF168BFF),
    primaryGradient = listOf(Color(0xFF168BFF), Color(0xFF5FD7FF)),
    heroGradient = listOf(Color(0xFF114F94), Color(0xFF0A5873), Color(0xFF332F85)),
    cardRadius = 24.dp,
    contentRadius = 18.dp,
    glassAlpha = 0.75f,
    borderAlpha = 0.16f,
    elevation = 14.dp,
    isDark = true,
)

val LocalFinluxTokens = staticCompositionLocalOf { PrismLightTokens }
val LocalFinluxSpacing = staticCompositionLocalOf { FinluxSpacing() }
val LocalFinluxRadius = staticCompositionLocalOf { FinluxRadius() }
