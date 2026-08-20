package com.finlux.app.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.finlux.app.domain.model.AppUiStyle
import com.finlux.app.domain.model.GlassIntensity
import com.finlux.app.domain.model.ThemePreference
import com.finlux.app.domain.model.UiPreferences
import com.finlux.app.domain.model.VisualStyle

/** Brand colors confirmed from the FinanceOS visual reference supplied for the Finlux refresh. */
val FinluxBlue = Color(0xFF3478F6)
val FinluxPurple = Color(0xFF7758F6)
val FinluxCyan = Color(0xFF47C8FF)
val IncomeGreen = Color(0xFF20B982)
val ExpenseRed = Color(0xFFF05B68)
val WarningAmber = Color(0xFFF2A63B)
val FinluxTextSecondary = Color(0xFF768197)
val FinluxCardLight = Color(0xFFFDFEFF)

// Classic v1.5.9 Color Palettes
private val LightColors = lightColorScheme(
    primary = FinluxBlue,
    onPrimary = Color.White,
    secondary = FinluxPurple,
    tertiary = FinluxCyan,
    background = Color(0xFFF5F7FC),
    surface = FinluxCardLight,
    surfaceVariant = Color(0xFFEFF3FA),
    onSurface = Color(0xFF172033),
    onSurfaceVariant = FinluxTextSecondary,
    error = ExpenseRed,
)

private val DarkColors = darkColorScheme(
    primary = FinluxCyan,
    onPrimary = Color(0xFF003548),
    secondary = Color(0xFFBBAAFF),
    tertiary = FinluxCyan,
    background = Color(0xFF090E1A),
    surface = Color(0xFF141C2A),
    surfaceVariant = Color(0xFF202A3B),
    onSurface = Color(0xFFE7F1FA),
    onSurfaceVariant = Color(0xFF94A3B8),
    outlineVariant = Color(0xFF2B3A52),
    error = Color(0xFFFFB2BB),
)

private val ModernDarkColors = darkColorScheme(
    primary = Color(0xFF168BFF),
    onPrimary = Color.White,
    secondary = Color(0xFF5FD7FF),
    tertiary = Color(0xFF7557FF),
    background = Color(0xFF020D1E),
    surface = Color(0xFF08182B),
    surfaceVariant = Color(0xFF0D2038),
    onSurface = Color(0xFFF3F8FF),
    onSurfaceVariant = Color(0xFF8EA3BD),
    outlineVariant = Color(0xFF203852),
    error = Color(0xFFFF7080),
)

private val GlassColors = darkColorScheme(
    primary = Color(0xFF8B5CFF),
    onPrimary = Color.White,
    secondary = Color(0xFF52CCFF),
    tertiary = Color(0xFFA99BFF),
    background = Color(0xFF131D40),
    surface = Color(0xFF243466),
    surfaceVariant = Color(0xFF354885),
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFCBD5F6),
    outlineVariant = Color.White.copy(alpha = .32f),
    error = Color(0xFFFFA6B0),
)

// Modern v1.6.6+ Color Palettes
private val ModernLightColors = lightColorScheme(
    primary = Color(0xFF176BDF),
    onPrimary = Color.White,
    secondary = Color(0xFF0891B2),
    tertiary = Color(0xFF4F46E5),
    background = Color(0xFFF3F8FE),
    surface = Color(0xFFF9FCFF),
    surfaceVariant = Color(0xFFE7F0FA),
    onSurface = Color(0xFF071A2E),
    onSurfaceVariant = Color(0xFF476078),
    outlineVariant = Color(0xFFB9CADB),
    error = ExpenseRed,
)

private val GlassLightColors = lightColorScheme(
    primary = Color(0xFF6D4AFF),
    onPrimary = Color.White,
    secondary = Color(0xFF2DAFE8),
    tertiary = Color(0xFF8B5CFF),
    background = Color(0xFFF5F3FF),
    surface = Color(0xFFFCFAFF),
    surfaceVariant = Color(0xFFEDE8FF),
    onSurface = Color(0xFF211A3A),
    onSurfaceVariant = Color(0xFF5D5677),
    outlineVariant = Color(0xFFC9BFF1),
    error = ExpenseRed,
)

@Immutable
data class GlassTokens(
    val fill: Brush,
    val border: Brush,
    val shadow: Color,
    val glow: Color,
    val accent: Color = FinluxBlue,
    val backdrop: List<Color> = listOf(Color(0xFFF5F7FC), Color.White),
)

val LocalGlassTokens = staticCompositionLocalOf {
    GlassTokens(
        fill = Brush.verticalGradient(listOf(Color.White, Color.White)),
        border = Brush.linearGradient(listOf(Color.White, Color.Transparent)),
        shadow = Color.Black,
        glow = Color.Transparent,
        accent = FinluxBlue,
        backdrop = listOf(Color(0xFFF5F7FC), Color.White),
    )
}

val LocalAppUiStyle = staticCompositionLocalOf { AppUiStyle.CLASSIC_LIQUID }
val LocalUiPreferences = staticCompositionLocalOf { UiPreferences() }

@Composable
fun FinluxTheme(
    preference: ThemePreference,
    uiStyle: AppUiStyle = AppUiStyle.CLASSIC_LIQUID,
    uiPreferences: UiPreferences = UiPreferences(),
    content: @Composable () -> Unit,
) {
    val dark = when (preference) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }
    val vividness = when (uiPreferences.glassIntensity) {
        GlassIntensity.SOFT -> if (uiStyle == AppUiStyle.MODERN_LUXURY) .72f else .65f
        GlassIntensity.BALANCED -> if (uiStyle == AppUiStyle.MODERN_LUXURY) .88f else .82f
        GlassIntensity.VIVID -> 1f
    }

    val styleAccent = when (uiPreferences.visualStyle) {
        VisualStyle.MODERN_DARK -> if (dark) Color(0xFF168BFF) else Color(0xFF176BDF)
        VisualStyle.GLASSMORPHISM -> Color(0xFF8B5CFF)
        VisualStyle.DYNAMIC_GRADIENT -> if (dark) FinluxCyan else FinluxBlue
    }

    val colorScheme = when (uiStyle) {
        AppUiStyle.MODERN_LUXURY -> when (uiPreferences.visualStyle) {
            VisualStyle.MODERN_DARK -> if (dark) ModernDarkColors else ModernLightColors
            VisualStyle.GLASSMORPHISM -> if (dark) GlassColors else GlassLightColors
            VisualStyle.DYNAMIC_GRADIENT -> if (dark) DarkColors else LightColors
        }
        AppUiStyle.CLASSIC_LIQUID -> when (uiPreferences.visualStyle) {
            VisualStyle.MODERN_DARK -> ModernDarkColors
            VisualStyle.GLASSMORPHISM -> GlassColors
            VisualStyle.DYNAMIC_GRADIENT -> if (dark) DarkColors else LightColors
        }
    }

    val tokens = if (uiStyle == AppUiStyle.MODERN_LUXURY) {
        if (dark) {
            GlassTokens(
                fill = Brush.linearGradient(
                    listOf(
                        Color(0x8A1B2C4E),
                        Color(0x64101B30),
                        styleAccent.copy(alpha = .18f * vividness),
                    ),
                ),
                border = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = .55f),
                        FinluxCyan.copy(alpha = .34f * vividness),
                        styleAccent.copy(alpha = .42f * vividness),
                        Color.White.copy(alpha = .12f),
                    ),
                ),
                shadow = Color(0xFF040A18).copy(alpha = .45f),
                glow = styleAccent.copy(alpha = .22f * vividness),
                accent = styleAccent,
                backdrop = when (uiPreferences.visualStyle) {
                    VisualStyle.MODERN_DARK -> listOf(Color(0xFF020B18), Color(0xFF071426), Color(0xFF020D1E))
                    VisualStyle.GLASSMORPHISM -> listOf(Color(0xFF111938), Color(0xFF1B1942), Color(0xFF08172B))
                    VisualStyle.DYNAMIC_GRADIENT -> listOf(Color(0xFF07101F), Color(0xFF10162B), Color(0xFF07111D))
                },
            )
        } else {
            GlassTokens(
                fill = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = .58f),
                        Color.White.copy(alpha = .42f),
                        styleAccent.copy(alpha = .12f * vividness),
                    ),
                ),
                border = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = .98f),
                        FinluxCyan.copy(alpha = .30f * vividness),
                        styleAccent.copy(alpha = .38f * vividness),
                        Color.White.copy(alpha = .42f),
                    ),
                ),
                shadow = Color(0x18183258),
                glow = styleAccent.copy(alpha = .14f * vividness),
                accent = styleAccent,
                backdrop = when (uiPreferences.visualStyle) {
                    VisualStyle.MODERN_DARK -> listOf(Color(0xFFEAF4FF), Color(0xFFF8FBFF), Color(0xFFEAF7FB))
                    VisualStyle.GLASSMORPHISM -> listOf(Color(0xFFF1EFFF), Color(0xFFFAF8FF), Color(0xFFEAF8FF))
                    VisualStyle.DYNAMIC_GRADIENT -> listOf(Color(0xFFF2F7FF), Color(0xFFF9F5FF), Color(0xFFECF9FF))
                },
            )
        }
    } else {
        // Classic Liquid Glass Tokens (v1.5.9 100% exact)
        when (uiPreferences.visualStyle) {
            VisualStyle.MODERN_DARK -> GlassTokens(
                fill = Brush.verticalGradient(listOf(Color(0xE60A1B31), Color(0xF0061426))),
                border = Brush.linearGradient(listOf(Color(0xFF1A9BFF).copy(alpha = .46f), Color.White.copy(alpha = .07f))),
                shadow = Color.Black.copy(alpha = .46f),
                glow = FinluxBlue.copy(alpha = .08f * vividness),
                accent = FinluxBlue,
            )
            VisualStyle.GLASSMORPHISM -> GlassTokens(
                fill = Brush.linearGradient(listOf(Color.White.copy(alpha = .20f), Color(0xFF6E57D9).copy(alpha = .18f), Color.White.copy(alpha = .08f))),
                border = Brush.linearGradient(listOf(Color.White.copy(alpha = .58f), Color(0xFF8C6CFF).copy(alpha = .48f), Color.White.copy(alpha = .12f))),
                shadow = Color(0xFF091434).copy(alpha = .42f),
                glow = Color(0xFF9B7CFF).copy(alpha = .18f * vividness),
                accent = Color(0xFF8B5CFF),
            )
            VisualStyle.DYNAMIC_GRADIENT -> if (dark) {
                GlassTokens(
                    fill = Brush.verticalGradient(listOf(Color(0xF01A2332), Color(0xE6151C29), FinluxPurple.copy(alpha = .08f * vividness))),
                    border = Brush.linearGradient(listOf(Color.White.copy(alpha = .18f * vividness), FinluxPurple.copy(alpha = .16f * vividness), FinluxCyan.copy(alpha = .10f * vividness))),
                    shadow = Color.Black.copy(alpha = .32f),
                    glow = FinluxPurple.copy(alpha = .10f * vividness),
                    accent = FinluxCyan,
                )
            } else {
                GlassTokens(
                    fill = Brush.verticalGradient(listOf(Color.White.copy(alpha = .94f), Color(0xEDF9FBFF), FinluxCyan.copy(alpha = .035f * vividness))),
                    border = Brush.linearGradient(listOf(Color.White, FinluxBlue.copy(alpha = .18f * vividness), Color(0xFFDDE7F5))),
                    shadow = Color(0x24284A78),
                    glow = FinluxBlue.copy(alpha = .05f * vividness),
                    accent = FinluxBlue,
                )
            }
        }
    }

    val finluxTokens = when (uiStyle) {
        AppUiStyle.CLASSIC_LIQUID -> if (dark) com.finlux.app.core.designsystem.theme.ClassicLiquidDarkTokens else com.finlux.app.core.designsystem.theme.ClassicLiquidLightTokens
        AppUiStyle.MODERN_LUXURY -> if (dark) com.finlux.app.core.designsystem.theme.ModernLuxuryDarkTokens else com.finlux.app.core.designsystem.theme.ModernLuxuryLightTokens
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FinluxTypography,
    ) {
        CompositionLocalProvider(
            LocalGlassTokens provides tokens,
            com.finlux.app.core.designsystem.theme.LocalFinluxTokens provides finluxTokens,
            com.finlux.app.core.designsystem.theme.LocalFinluxSpacing provides finluxTokens.spacing,
            com.finlux.app.core.designsystem.theme.LocalFinluxRadius provides finluxTokens.radius,
            LocalAppUiStyle provides uiStyle,
            LocalUiPreferences provides uiPreferences,
            LocalContentColor provides MaterialTheme.colorScheme.onSurface,
            content = content,
        )
    }
}
