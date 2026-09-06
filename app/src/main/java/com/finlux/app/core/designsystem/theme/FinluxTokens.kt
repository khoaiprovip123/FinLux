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
 * Centralized exact visual-reference palette for charts, status accents and decorative artwork.
 *
 * Presentation code must not declare raw Color(0x...) literals. Theme-dependent surfaces/text
 * continue to use [FinluxDesignTokens]; this palette preserves exact reference artwork values
 * without scattering magic colors across feature screens.
 */
object FinluxPalette {
    val White = Color.White
    val Black = Color.Black
    val Transparent = Color.Transparent
    val C203B82F6 = Color(0x203B82F6)
    val C304F46E5 = Color(0x304F46E5)
    val C307C3AED = Color(0x307C3AED)
    val C405B21B6 = Color(0x405B21B6)
    val CFF002B3D = Color(0xFF002B3D)
    val CFF0072FF = Color(0xFF0072FF)
    val CFF00C6FF = Color(0xFF00C6FF)
    val CFF020D1E = Color(0xFF020D1E)
    val CFF0284C7 = Color(0xFF0284C7)
    val CFF0369A1 = Color(0xFF0369A1)
    val CFF04382B = Color(0xFF04382B)
    val CFF047857 = Color(0xFF047857)
    val CFF059669 = Color(0xFF059669)
    val CFF065F46 = Color(0xFF065F46)
    val CFF06B6D4 = Color(0xFF06B6D4)
    val CFF071B32 = Color(0xFF071B32)
    val CFF08182B = Color(0xFF08182B)
    val CFF087FE6 = Color(0xFF087FE6)
    val CFF0891B2 = Color(0xFF0891B2)
    val CFF0A192F = Color(0xFF0A192F)
    val CFF0A4380 = Color(0xFF0A4380)
    val CFF0B2848 = Color(0xFF0B2848)
    val CFF0D9488 = Color(0xFF0D9488)
    val CFF0EA5E9 = Color(0xFF0EA5E9)
    val CFF0F172A = Color(0xFF0F172A)
    val CFF10B981 = Color(0xFF10B981)
    val CFF131D2E = Color(0xFF131D2E)
    val CFF14B8A6 = Color(0xFF14B8A6)
    val CFF14D1D0 = Color(0xFF14D1D0)
    val CFF16A34A = Color(0xFF16A34A)
    val CFF1749D9 = Color(0xFF1749D9)
    val CFF176BDF = Color(0xFF176BDF)
    val CFF181824 = Color(0xFF181824)
    val CFF19163F = Color(0xFF19163F)
    val CFF1D4ED8 = Color(0xFF1D4ED8)
    val CFF1E1B4B = Color(0xFF1E1B4B)
    val CFF1E1E2D = Color(0xFF1E1E2D)
    val CFF1E1E34 = Color(0xFF1E1E34)
    val CFF1E2235 = Color(0xFF1E2235)
    val CFF1E2438 = Color(0xFF1E2438)
    val CFF1E293B = Color(0xFF1E293B)
    val CFF1E2E2A = Color(0xFF1E2E2A)
    val CFF1E3A2B = Color(0xFF1E3A2B)
    val CFF1E3A8A = Color(0xFF1E3A8A)
    val CFF201B3E = Color(0xFF201B3E)
    val CFF23C7E8 = Color(0xFF23C7E8)
    val CFF2563EB = Color(0xFF2563EB)
    val CFF261C38 = Color(0xFF261C38)
    val CFF261E38 = Color(0xFF261E38)
    val CFF264990 = Color(0xFF264990)
    val CFF28283E = Color(0xFF28283E)
    val CFF28293D = Color(0xFF28293D)
    val CFF2A2A3C = Color(0xFF2A2A3C)
    val CFF2E1E24 = Color(0xFF2E1E24)
    val CFF2E236C = Color(0xFF2E236C)
    val CFF311042 = Color(0xFF311042)
    val CFF312E81 = Color(0xFF312E81)
    val CFF33B7F8 = Color(0xFF33B7F8)
    val CFF3478F6 = Color(0xFF3478F6)
    val CFF356DFF = Color(0xFF356DFF)
    val CFF3730A3 = Color(0xFF3730A3)
    val CFF37C7F4 = Color(0xFF37C7F4)
    val CFF387BFA = Color(0xFF387BFA)
    val CFF38BDF8 = Color(0xFF38BDF8)
    val CFF3A5FFF = Color(0xFF3A5FFF)
    val CFF3A7BFF = Color(0xFF3A7BFF)
    val CFF3B1E2B = Color(0xFF3B1E2B)
    val CFF3B5DF8 = Color(0xFF3B5DF8)
    val CFF3B82F6 = Color(0xFF3B82F6)
    val CFF416EF8 = Color(0xFF416EF8)
    val CFF4338CA = Color(0xFF4338CA)
    val CFF475569 = Color(0xFF475569)
    val CFF47AEEA = Color(0xFF47AEEA)
    val CFF47C8FF = Color(0xFF47C8FF)
    val CFF4ADE80 = Color(0xFF4ADE80)
    val CFF4C68FF = Color(0xFF4C68FF)
    val CFF4D58C8 = Color(0xFF4D58C8)
    val CFF4E56FF = Color(0xFF4E56FF)
    val CFF4F46E5 = Color(0xFF4F46E5)
    val CFF54B5E8 = Color(0xFF54B5E8)
    val CFF5B21B6 = Color(0xFF5B21B6)
    val CFF5B2BFF = Color(0xFF5B2BFF)
    val CFF5B32F4 = Color(0xFF5B32F4)
    val CFF5B38FD = Color(0xFF5B38FD)
    val CFF5B4DFF = Color(0xFF5B4DFF)
    val CFF5B50EC = Color(0xFF5B50EC)
    val CFF5E50F8 = Color(0xFF5E50F8)
    val CFF6366F1 = Color(0xFF6366F1)
    val CFF64748B = Color(0xFF64748B)
    val CFF6B0E27 = Color(0xFF6B0E27)
    val CFF6B7280 = Color(0xFF6B7280)
    val CFF6D28D9 = Color(0xFF6D28D9)
    val CFF6F52F5 = Color(0xFF6F52F5)
    val CFF714CF6 = Color(0xFF714CF6)
    val CFF7457CE = Color(0xFF7457CE)
    val CFF7755D8 = Color(0xFF7755D8)
    val CFF7758F6 = Color(0xFF7758F6)
    val CFF77FFB3 = Color(0xFF77FFB3)
    val CFF7C2CFF = Color(0xFF7C2CFF)
    val CFF7C3AED = Color(0xFF7C3AED)
    val CFF7C3CFF = Color(0xFF7C3CFF)
    val CFF7C5AF9 = Color(0xFF7C5AF9)
    val CFF818CF8 = Color(0xFF818CF8)
    val CFF84CC16 = Color(0xFF84CC16)
    val CFF865BF9 = Color(0xFF865BF9)
    val CFF86EFAC = Color(0xFF86EFAC)
    val CFF881337 = Color(0xFF881337)
    val CFF8B28F7 = Color(0xFF8B28F7)
    val CFF8B5CF6 = Color(0xFF8B5CF6)
    val CFF8B5CFF = Color(0xFF8B5CFF)
    val CFF8E9EB5 = Color(0xFF8E9EB5)
    val CFF9333EA = Color(0xFF9333EA)
    val CFF94A3B8 = Color(0xFF94A3B8)
    val CFF9B51E0 = Color(0xFF9B51E0)
    val CFF9B5CFF = Color(0xFF9B5CFF)
    val CFF9B6EFB = Color(0xFF9B6EFB)
    val CFF9CA3AF = Color(0xFF9CA3AF)
    val CFF9F1239 = Color(0xFF9F1239)
    val CFFA855F7 = Color(0xFFA855F7)
    val CFFB45309 = Color(0xFFB45309)
    val CFFBE123C = Color(0xFFBE123C)
    val CFFC4B5FD = Color(0xFFC4B5FD)
    val CFFCBD5E1 = Color(0xFFCBD5E1)
    val CFFCCFBF1 = Color(0xFFCCFBF1)
    val CFFD97706 = Color(0xFFD97706)
    val CFFDBEAFE = Color(0xFFDBEAFE)
    val CFFDC2626 = Color(0xFFDC2626)
    val CFFDCFCE7 = Color(0xFFDCFCE7)
    val CFFDDD6FE = Color(0xFFDDD6FE)
    val CFFE0F2FE = Color(0xFFE0F2FE)
    val CFFE11D48 = Color(0xFFE11D48)
    val CFFE2E8F0 = Color(0xFFE2E8F0)
    val CFFE5E7EB = Color(0xFFE5E7EB)
    val CFFEA580C = Color(0xFFEA580C)
    val CFFEAB308 = Color(0xFFEAB308)
    val CFFEC4899 = Color(0xFFEC4899)
    val CFFEDE9FE = Color(0xFFEDE9FE)
    val CFFEEEEEE = Color(0xFFEEEEEE)
    val CFFEF4444 = Color(0xFFEF4444)
    val CFFEFF6FF = Color(0xFFEFF6FF)
    val CFFF0F6FF = Color(0xFFF0F6FF)
    val CFFF0F9FF = Color(0xFFF0F9FF)
    val CFFF0FDF4 = Color(0xFFF0FDF4)
    val CFFF1F5F9 = Color(0xFFF1F5F9)
    val CFFF3E8FF = Color(0xFFF3E8FF)
    val CFFF3F4F6 = Color(0xFFF3F4F6)
    val CFFF43F5E = Color(0xFFF43F5E)
    val CFFF59E0B = Color(0xFFF59E0B)
    val CFFF5F3FF = Color(0xFFF5F3FF)
    val CFFF87171 = Color(0xFFF87171)
    val CFFF97316 = Color(0xFFF97316)
    val CFFF9FAFB = Color(0xFFF9FAFB)
    val CFFFAF5FF = Color(0xFFFAF5FF)
    val CFFFAFAFA = Color(0xFFFAFAFA)
    val CFFFBBF24 = Color(0xFFFBBF24)
    val CFFFCA5A5 = Color(0xFFFCA5A5)
    val CFFFDE047 = Color(0xFFFDE047)
    val CFFFEE2E2 = Color(0xFFFEE2E2)
    val CFFFEF08A = Color(0xFFFEF08A)
    val CFFFEF2F2 = Color(0xFFFEF2F2)
    val CFFFEF3C7 = Color(0xFFFEF3C7)
    val CFFFF405B = Color(0xFFFF405B)
    val CFFFF6680 = Color(0xFFFF6680)
    val CFFFF7A45 = Color(0xFFFF7A45)
    val CFFFF7B91 = Color(0xFFFF7B91)
    val CFFFF8A42 = Color(0xFFFF8A42)
    val CFFFFB347 = Color(0xFFFFB347)
    val CFFFFD700 = Color(0xFFFFD700)
    val CFFFFE4E6 = Color(0xFFFFE4E6)
    val CFFFFEDD5 = Color(0xFFFFEDD5)
    val CFFFFF1F2 = Color(0xFFFFF1F2)
    val CFFFFFBEB = Color(0xFFFFFBEB)
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
    val onHero: Color = FinluxColors.SurfacePrimaryLight,
    val brandLogoSurface: Color = FinluxColors.SurfacePrimaryLight,
    val brandLogoBorder: Color = FinluxColors.BorderSoftLight,
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
    val onHeroMuted: Color get() = onHero.copy(alpha = 0.82f)
    val heroGlassSurface: Color get() = onHero.copy(alpha = 0.18f)
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
