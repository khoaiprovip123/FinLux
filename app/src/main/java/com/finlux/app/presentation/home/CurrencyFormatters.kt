package com.finlux.app.presentation.home

import java.text.NumberFormat
import java.util.Locale

fun Long.toVnd(): String = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")).format(this)

fun Long.toShortVnd(): String = when {
    kotlin.math.abs(this) >= 1_000_000 -> String.format(Locale.forLanguageTag("vi-VN"), "%.1f tr", this / 1_000_000.0)
    kotlin.math.abs(this) >= 1_000 -> String.format(Locale.forLanguageTag("vi-VN"), "%.0fK", this / 1_000.0)
    else -> "$this đ"
}
