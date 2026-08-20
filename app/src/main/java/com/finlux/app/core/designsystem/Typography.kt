package com.finlux.app.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * FinLux Typography Tokens (FinLux Prism Spec 3)
 */
object FinluxTextStyles {
    val DisplayAmount = TextStyle(
        fontSize = 36.sp,
        lineHeight = 44.sp,
        fontWeight = FontWeight.SemiBold,
    )

    val ScreenTitle = TextStyle(
        fontSize = 28.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.SemiBold,
    )

    val SectionTitle = TextStyle(
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.SemiBold,
    )

    val CardTitle = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Medium,
    )

    val Body = TextStyle(
        fontSize = 15.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Normal,
    )

    val Caption = TextStyle(
        fontSize = 12.5.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal,
    )

    val MicroLabel = TextStyle(
        fontSize = 11.5.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Medium,
    )
}

val FinluxTypography = Typography(
    displayLarge = FinluxTextStyles.DisplayAmount,
    displayMedium = TextStyle(fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.SemiBold),
    headlineLarge = TextStyle(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = FinluxTextStyles.ScreenTitle,
    headlineSmall = TextStyle(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = FinluxTextStyles.SectionTitle,
    titleMedium = FinluxTextStyles.CardTitle,
    titleSmall = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    bodyLarge = FinluxTextStyles.Body,
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
    bodySmall = FinluxTextStyles.Caption,
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    labelSmall = FinluxTextStyles.MicroLabel,
)
