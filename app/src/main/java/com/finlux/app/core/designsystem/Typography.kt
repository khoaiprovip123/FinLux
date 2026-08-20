package com.finlux.app.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Global Default Font Family: Roboto (Native SansSerif on Android)
 */
val RobotoFontFamily = FontFamily.SansSerif

/**
 * FinLux Typography Tokens with Roboto Font Family
 */
object FinluxTextStyles {
    val DisplayAmount = TextStyle(
        fontFamily = RobotoFontFamily,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        fontWeight = FontWeight.Bold,
    )

    val ScreenTitle = TextStyle(
        fontFamily = RobotoFontFamily,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.Bold,
    )

    val SectionTitle = TextStyle(
        fontFamily = RobotoFontFamily,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.Bold,
    )

    val CardTitle = TextStyle(
        fontFamily = RobotoFontFamily,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold,
    )

    val Body = TextStyle(
        fontFamily = RobotoFontFamily,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Normal,
    )

    val Caption = TextStyle(
        fontFamily = RobotoFontFamily,
        fontSize = 13.sp,
        lineHeight = 17.sp,
        fontWeight = FontWeight.Normal,
    )

    val MicroLabel = TextStyle(
        fontFamily = RobotoFontFamily,
        fontSize = 11.5.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Medium,
    )
}

val FinluxTypography = Typography(
    displayLarge = FinluxTextStyles.DisplayAmount,
    displayMedium = TextStyle(fontFamily = RobotoFontFamily, fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold),
    displaySmall = TextStyle(fontFamily = RobotoFontFamily, fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontFamily = RobotoFontFamily, fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),
    headlineMedium = FinluxTextStyles.ScreenTitle,
    headlineSmall = TextStyle(fontFamily = RobotoFontFamily, fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold),
    titleLarge = FinluxTextStyles.SectionTitle,
    titleMedium = FinluxTextStyles.CardTitle,
    titleSmall = TextStyle(fontFamily = RobotoFontFamily, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = FinluxTextStyles.Body,
    bodyMedium = TextStyle(fontFamily = RobotoFontFamily, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
    bodySmall = FinluxTextStyles.Caption,
    labelLarge = TextStyle(fontFamily = RobotoFontFamily, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold),
    labelMedium = TextStyle(fontFamily = RobotoFontFamily, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold),
    labelSmall = FinluxTextStyles.MicroLabel,
)
