package com.finlux.app.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import java.text.DecimalFormat

/**
 * Format raw number string with Vietnamese thousand dot separators (e.g. 50000 -> 50.000)
 */
fun formatAmountDigitsWithDots(rawInput: String): String {
    val digits = rawInput.filter { it.isDigit() }.trimStart('0')
    if (digits.isEmpty()) return ""
    val number = digits.toLongOrNull() ?: 0L
    val formatter = DecimalFormat("#,###")
    return formatter.format(number).replace(',', '.')
}

/**
 * Shared, reusable Pixel-Perfect Hero Amount Input Card.
 * Adheres 100% to Directive #1 (Dynamic Tokens) and Directive #2 (Reusability).
 */
@Composable
fun FinluxAmountInputCard(
    amountDigits: String,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Số tiền",
    primaryColor: Color = LocalFinluxTokens.current.primary,
    quickAmounts: List<Long> = listOf(500_000L, 1_000_000L, 2_000_000L, 5_000_000L, 10_000_000L),
    showQuickChips: Boolean = true,
    showCalculator: Boolean = false,
    onCalculatorClick: (() -> Unit)? = null,
) {
    val tokens = LocalFinluxTokens.current
    val cleanDigits = amountDigits.filter { it.isDigit() }.trimStart('0')
    val formatted = formatAmountDigitsWithDots(cleanDigits)

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = tokens.surfaceSoft,
        border = BorderStroke(1.dp, tokens.border),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = tokens.onSurfaceVariant,
            )

            // Formatted Amount Row (Grouped Together with ₫ Symbol)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    BasicTextField(
                        value = TextFieldValue(
                            text = formatted,
                            selection = TextRange(formatted.length),
                        ),
                        onValueChange = { tfv ->
                            val digitsOnly = tfv.text.filter { it.isDigit() }.trimStart('0')
                            if (digitsOnly.length <= 15) {
                                onAmountChange(digitsOnly)
                            }
                        },
                        textStyle = TextStyle(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryColor,
                            letterSpacing = (-0.5).sp,
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        cursorBrush = SolidColor(primaryColor),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (formatted.isEmpty()) {
                                Text(
                                    text = "0",
                                    style = TextStyle(
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = tokens.onSurfaceVariant.copy(alpha = 0.4f),
                                    ),
                                )
                            }
                            innerTextField()
                        },
                    )

                    Text(
                        text = " ₫",
                        style = TextStyle(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryColor,
                        ),
                        modifier = Modifier.padding(start = 2.dp),
                    )
                }

                if (cleanDigits.isNotEmpty()) {
                    Surface(
                        shape = CircleShape,
                        color = tokens.surface,
                        border = BorderStroke(1.dp, tokens.border),
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { onAmountChange("") },
                            ),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Close,
                                contentDescription = "Xóa số tiền",
                                tint = tokens.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            // Quick Add Chips
            if (showQuickChips && quickAmounts.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(quickAmounts) { chipAmount ->
                        val chipText = when {
                            chipAmount >= 1_000_000L && chipAmount % 1_000_000L == 0L -> "+${chipAmount / 1_000_000L}tr"
                            chipAmount >= 1_000_000L -> "+${chipAmount / 1_000_000f}tr"
                            chipAmount >= 1_000L -> "+${chipAmount / 1_000L}k"
                            else -> "+$chipAmount"
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = tokens.surface,
                            border = BorderStroke(1.dp, tokens.border),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = {
                                        val cur = cleanDigits.toLongOrNull() ?: 0L
                                        val updated = cur + chipAmount
                                        onAmountChange(updated.toString())
                                    },
                                ),
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = chipText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                    ),
                                    color = tokens.onSurface,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
