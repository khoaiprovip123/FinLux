package com.finlux.app.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.finlux.app.domain.model.ThemePreference
import com.finlux.app.domain.model.GlassIntensity
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
    background = Color(0xFF18295B),
    surface = Color(0xFF354D8A),
    surfaceVariant = Color(0xFF4B5F9A),
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFD6DDF8),
    outlineVariant = Color.White.copy(alpha = .22f),
    error = Color(0xFFFFA6B0),
)

@Immutable
data class GlassTokens(
    val fill: Brush,
    val border: Brush,
    val shadow: Color,
    val glow: Color,
)

val LocalGlassTokens = staticCompositionLocalOf {
    GlassTokens(
        fill = Brush.verticalGradient(listOf(Color.White, Color.White)),
        border = Brush.linearGradient(listOf(Color.White, Color.Transparent)),
        shadow = Color.Black,
        glow = Color.Transparent,
    )
}

val LocalUiPreferences = staticCompositionLocalOf { UiPreferences() }

@Composable
fun FinluxTheme(
    preference: ThemePreference,
    uiPreferences: UiPreferences = UiPreferences(),
    content: @Composable () -> Unit,
) {
    val dark = when (preference) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }
    val vividness = when (uiPreferences.glassIntensity) {
        GlassIntensity.SOFT -> .65f
        GlassIntensity.BALANCED -> .82f
        GlassIntensity.VIVID -> 1f
    }
    val style = uiPreferences.visualStyle
    val tokens = when (style) {
        VisualStyle.MODERN_DARK -> GlassTokens(
            fill = Brush.verticalGradient(listOf(Color(0xE60A1B31), Color(0xF0061426))),
            border = Brush.linearGradient(listOf(Color(0xFF1A9BFF).copy(alpha = .46f), Color.White.copy(alpha = .07f))),
            shadow = Color.Black.copy(alpha = .46f),
            glow = FinluxBlue.copy(alpha = .08f * vividness),
        )
        VisualStyle.GLASSMORPHISM -> GlassTokens(
            fill = Brush.linearGradient(listOf(Color.White.copy(alpha = .20f), Color(0xFF6E57D9).copy(alpha = .18f), Color.White.copy(alpha = .08f))),
            border = Brush.linearGradient(listOf(Color.White.copy(alpha = .58f), Color(0xFF8C6CFF).copy(alpha = .48f), Color.White.copy(alpha = .12f))),
            shadow = Color(0xFF091434).copy(alpha = .42f),
            glow = Color(0xFF9B7CFF).copy(alpha = .18f * vividness),
        )
        VisualStyle.DYNAMIC_GRADIENT -> if (dark) {
        GlassTokens(
            fill = Brush.verticalGradient(listOf(Color(0xF01A2332), Color(0xE6151C29), FinluxPurple.copy(alpha = .08f * vividness))),
            border = Brush.linearGradient(listOf(Color.White.copy(alpha = .18f * vividness), FinluxPurple.copy(alpha = .16f * vividness), FinluxCyan.copy(alpha = .10f * vividness))),
            shadow = Color.Black.copy(alpha = .32f),
            glow = FinluxPurple.copy(alpha = .10f * vividness),
        )
        } else {
        GlassTokens(
            fill = Brush.verticalGradient(listOf(Color.White.copy(alpha = .94f), Color(0xEDF9FBFF), FinluxCyan.copy(alpha = .035f * vividness))),
            border = Brush.linearGradient(listOf(Color.White, FinluxBlue.copy(alpha = .18f * vividness), Color(0xFFDDE7F5))),
            shadow = Color(0x24284A78),
            glow = FinluxBlue.copy(alpha = .05f * vividness),
        )
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalGlassTokens provides tokens,
        LocalUiPreferences provides uiPreferences,
    ) {
        MaterialTheme(
            colorScheme = when (style) {
                VisualStyle.MODERN_DARK -> ModernDarkColors
                VisualStyle.GLASSMORPHISM -> GlassColors
                VisualStyle.DYNAMIC_GRADIENT -> if (dark) DarkColors else LightColors
            },
            typography = FinluxTypography,
            content = content,
        )
    }
}
