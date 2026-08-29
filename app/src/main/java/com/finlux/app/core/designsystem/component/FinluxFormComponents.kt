package com.finlux.app.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.FinancialInstitutionLogo
import com.finlux.app.core.designsystem.findInstitutionForWallet
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.designsystem.walletIcon
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.Wallet

/**
 * Reusable Ergonomic Form Row (2-Line label + value with icon badge and chevron).
 * Adheres 100% to Directive #1 (Dynamic Tokens) and Directive #2 (Reusability).
 */
@Composable
fun ErgonomicFormRow(
    label: String,
    primaryValue: String,
    secondaryValue: String? = null,
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = tokens.surfaceSoft,
        border = BorderStroke(1.dp, tokens.border),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconBgColor,
                modifier = Modifier.size(42.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconTintColor, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    ),
                    color = tokens.onSurfaceVariant,
                )
                Text(
                    text = primaryValue,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = tokens.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (secondaryValue != null) {
                    Text(
                        text = secondaryValue,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = tokens.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

/**
 * Reusable Ergonomic Input Row (Icon badge + Label + BasicTextField + Clear button [x]).
 * Flat, seamless design with no Material 3 outline notch background cuts.
 */
@Composable
fun ErgonomicInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    onClear: () -> Unit = {},
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 17.5.sp,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
        border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFF3F4F6)),
        shadowElevation = 1.5.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = iconBgColor,
                modifier = Modifier.size(46.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconTintColor, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    ),
                    color = Color(0xFF9CA3AF),
                )
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        fontSize = fontSize,
                        fontWeight = FontWeight.SemiBold,
                        color = tokens.onSurface,
                    ),
                    cursorBrush = SolidColor(tokens.primary),
                    keyboardOptions = keyboardOptions,
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = TextStyle(
                                    fontSize = fontSize,
                                    color = Color(0xFF9CA3AF),
                                ),
                            )
                        }
                        innerTextField()
                    },
                )
            }

            if (value.isNotEmpty()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Xóa",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

/**
 * Helper to generate smart amount multiplier suggestions based on Decimal Magnitude Scaling.
 * - Empty / 0: Standard preset list [50.000, 100.000, 200.000, 500.000, 1.000.000, 2.000.000, 5.000.000, 10.000.000]
 * - Non-zero N: Dynamically scales N * (10^k) within realistic bounds (1.000đ to 1.000.000.000đ), up to 5-6 chips.
 *   e.g. 3 -> [3.000, 30.000, 300.000, 3.000.000, 30.000.000]
 *   e.g. 35 -> [3.500, 35.000, 350.000, 3.500.000, 35.000.000]
 *   e.g. 356 -> [3.560, 35.600, 356.000, 3.560.000, 35.600.000]
 *   e.g. 3568 -> [35.680, 356.800, 3.568.000, 35.680.000]
 */
internal fun generateAmountSuggestions(rawInput: String): List<Pair<String, String>> {
    val digitsOnly = rawInput.filter { it.isDigit() }
    val baseNumber = digitsOnly.toLongOrNull() ?: 0L

    if (baseNumber <= 0L) {
        return listOf(
            "50.000" to "50000",
            "100.000" to "100000",
            "200.000" to "200000",
            "500.000" to "500000",
            "1.000.000" to "1000000",
            "2.000.000" to "2000000",
            "5.000.000" to "5000000",
            "10.000.000" to "10000000",
        )
    }

    val minTarget = 1_000L
    val maxLimit = 1_000_000_000L // 1 tỷ VNĐ
    val maxChips = 5

    // Starting power of 10 based on digit length to scale from thousands up to millions/billions
    val startK = when {
        baseNumber < 10L -> 3      // e.g. 3 -> 3.000 (3 * 10^3)
        baseNumber < 100L -> 2     // e.g. 35 -> 3.500 (35 * 10^2)
        baseNumber < 1_000L -> 1   // e.g. 356 -> 3.560 (356 * 10^1)
        else -> 1                  // e.g. 3568 -> 35.680 (3568 * 10^1)
    }

    val list = mutableListOf<Pair<String, String>>()
    var currentMultiplier = 1L
    for (i in 0 until startK) {
        currentMultiplier *= 10L
    }

    while (list.size < maxChips) {
        // Prevent overflow before multiplying
        if (maxLimit / currentMultiplier < baseNumber) break
        val targetVal = baseNumber * currentMultiplier
        if (targetVal in minTarget..maxLimit) {
            val formatted = formatAmountDigitsWithDots(targetVal.toString())
            list.add(formatted to targetVal.toString())
        }
        if (maxLimit / 10L < currentMultiplier) break
        currentMultiplier *= 10L
    }

    if (list.isEmpty() && baseNumber in minTarget..maxLimit) {
        val formatted = formatAmountDigitsWithDots(baseNumber.toString())
        list.add(formatted to baseNumber.toString())
    }

    return list.distinctBy { it.second }
}

/**
 * VisualTransformation that formats numbers with thousand separators and appends a " ₫" suffix
 * as part of the text itself, ensuring the " ₫" is ALWAYS attached right after the digits.
 */
class VndSuffixVisualTransformation(
    private val suffix: String = " ₫"
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val formattedText = formatAmountDigitsWithDots(originalText)
        val transformedText = formattedText + suffix

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val safeOffset = offset.coerceAtMost(originalText.length)
                val digitsBefore = originalText.take(safeOffset)
                val formattedBefore = formatAmountDigitsWithDots(digitsBefore)
                return formattedBefore.length.coerceAtMost(transformedText.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                val safeOffset = offset.coerceAtMost(transformedText.length)
                val textBefore = formattedText.take(safeOffset)
                val digitsCount = textBefore.count { it.isDigit() }
                return digitsCount.coerceAtMost(originalText.length)
            }
        }

        return TransformedText(AnnotatedString(transformedText), offsetMapping)
    }
}

/**
 * Reusable Compact Amount Input / Display Card.
 * Compact Surface card with top label, live formatted VNĐ amount with inline 'đ' suffix,
 * quick clear button [x], and dynamic rounded .000 suggestion chips to fast-fill numbers.
 * No bottom subtitle. Can be used standalone (full width) or in a multi-column Row.
 */
@Composable
fun ErgonomicCompactAmountCard(
    label: String,
    amountText: String,
    onAmountChange: ((String) -> Unit)? = null,
    placeholder: String = "0",
    amountColor: Color = LocalFinluxTokens.current.primary,
    showSuggestions: Boolean = true,
    amountFontSize: TextUnit = 24.sp,
    modifier: Modifier = Modifier,
    isReadOnly: Boolean = onAmountChange == null,
    enabled: Boolean = true,
) {
    val tokens = LocalFinluxTokens.current
    var isFocused by remember { mutableStateOf(false) }
    val cleanDigits = amountText.filter { it.isDigit() }.take(12)
    val formattedDisplay = formatAmountDigitsWithDots(cleanDigits)
    val suggestions = remember(cleanDigits) { generateAmountSuggestions(cleanDigits) }

    val effectiveFontSize = when {
        cleanDigits.length >= 11 -> (amountFontSize.value * 0.70f).sp
        cleanDigits.length >= 9 -> (amountFontSize.value * 0.82f).sp
        else -> amountFontSize
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = tokens.surfaceSoft,
        border = BorderStroke(
            1.dp,
            if (isFocused) amountColor.copy(alpha = 0.5f) else tokens.border,
        ),
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Label in uppercase
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                ),
                color = tokens.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            // Amount Input / Display with inline VNĐ suffix & Clear button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Left: Value with VndSuffixVisualTransformation (Suffix is drawn inside the exact text run)
                if (isReadOnly || onAmountChange == null) {
                    Text(
                        text = if (formattedDisplay.isNotEmpty()) "$formattedDisplay ₫" else "$placeholder ₫",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = effectiveFontSize,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = if (formattedDisplay.isNotEmpty()) amountColor else Color(0xFF9CA3AF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                } else {
                    BasicTextField(
                        value = TextFieldValue(
                            text = cleanDigits,
                            selection = TextRange(cleanDigits.length),
                        ),
                        onValueChange = { tfv ->
                            val digits = tfv.text.filter { it.isDigit() }.take(12)
                            onAmountChange(digits)
                        },
                        textStyle = TextStyle(
                            fontSize = effectiveFontSize,
                            fontWeight = FontWeight.Bold,
                            color = amountColor,
                        ),
                        cursorBrush = SolidColor(amountColor),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = enabled,
                        visualTransformation = remember { VndSuffixVisualTransformation() },
                        decorationBox = { innerTextField ->
                            if (cleanDigits.isEmpty()) {
                                Text(
                                    text = "$placeholder ₫",
                                    style = TextStyle(
                                        fontSize = effectiveFontSize,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF9CA3AF),
                                    ),
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { isFocused = it.isFocused },
                    )
                }

                // Right: Clear button [x]
                if (!isReadOnly && onAmountChange != null && cleanDigits.isNotEmpty() && enabled) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(if (tokens.isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE5E7EB))
                            .clickable { onAmountChange("") },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Xóa số tiền",
                            tint = if (tokens.isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF6B7280),
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }
            }

            // Dynamic quick suggestion chips (.000 format up to millions) - Only shown when input field is focused
            AnimatedVisibility(
                visible = isFocused && showSuggestions && !isReadOnly && onAmountChange != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column {
                    Spacer(Modifier.height(3.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items(suggestions) { (chipLabel, chipValue) ->
                            Surface(
                                shape = CircleShape,
                                color = amountColor.copy(alpha = 0.08f),
                                border = BorderStroke(
                                    0.75.dp,
                                    amountColor.copy(alpha = 0.22f),
                                ),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        onAmountChange?.invoke(chipValue)
                                    },
                            ) {
                                Text(
                                    text = chipLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = amountColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.5.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Ergonomic 2-Column Card for Debt Principal and Interest split.
 * Clean, seamless Liquid Glass card composed of 2 ErgonomicCompactAmountCards.
 */
@Composable
fun PrincipalInterestSplitCard(
    principalAmount: Long,
    interestText: String,
    onInterestChange: (String) -> Unit,
    principalColor: Color = LocalFinluxTokens.current.primary,
    interestColor: Color = Color(0xFF6366F1),
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ErgonomicCompactAmountCard(
            label = "TRỪ TIỀN GỐC",
            amountText = principalAmount.toString(),
            isReadOnly = true,
            amountColor = principalColor,
            modifier = Modifier.weight(1f),
        )

        ErgonomicCompactAmountCard(
            label = "TIỀN LÃI PHÁT SINH",
            amountText = interestText,
            onAmountChange = onInterestChange,
            placeholder = "0",
            amountColor = interestColor,
            showSuggestions = false,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Standard Unified Wallet Picker Bottom Sheet across the entire Finlux app.
 * Reused in AddTransactionSheet, DebtPaymentSheet, NotificationsScreen, etc.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinluxWalletPickerBottomSheet(
    wallets: List<Wallet>,
    selectedWalletId: String?,
    onSelectWallet: (Wallet) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val tokens = LocalFinluxTokens.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = tokens.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Chọn ví tài khoản",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = tokens.onSurface,
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(34.dp)
                        .background(tokens.surfaceSoft, CircleShape),
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng", tint = tokens.onSurface, modifier = Modifier.size(16.dp))
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(wallets, key = { it.id }) { wallet ->
                    val isSelected = wallet.id == selectedWalletId
                    val walletColor = colorFromHex(wallet.colorHex, tokens.primary)

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) walletColor.copy(alpha = if (tokens.isDark) 0.20f else 0.12f) else tokens.surfaceSoft,
                        border = if (isSelected) BorderStroke(1.5.dp, walletColor) else BorderStroke(1.dp, tokens.border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                onSelectWallet(wallet)
                                onDismiss()
                            },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                FinancialInstitutionLogo(
                                    institution = findInstitutionForWallet(wallet.name),
                                    walletType = wallet.type,
                                    customColorHex = wallet.colorHex,
                                    size = 38.dp,
                                )

                                Column {
                                    Text(
                                        text = wallet.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 15.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        ),
                                        color = tokens.onSurface,
                                    )
                                    Text(
                                        text = "Số dư: ${formatVndAmount(wallet.balance.value)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) walletColor else tokens.onSurfaceVariant,
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = walletColor,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Backward compatibility alias for FinluxWalletPickerBottomSheet */
@Composable
fun SimpleWalletPickerSheet(
    wallets: List<Wallet>,
    selectedWalletId: String?,
    onSelectWallet: (Wallet) -> Unit,
    onDismiss: () -> Unit,
) = FinluxWalletPickerBottomSheet(wallets, selectedWalletId, onSelectWallet, onDismiss)

/**
 * Standard Unified 4-Column Grid Category Picker Bottom Sheet.
 * Reused in AddTransactionSheet, NotificationsScreen, and any category selection modal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinluxCategoryPickerBottomSheet(
    categories: List<Category>,
    selectedCategoryId: String?,
    onSelectCategory: (Category) -> Unit,
    onDismiss: () -> Unit,
    onAddNew: (() -> Unit)? = null,
    onLongPressCategory: ((Category) -> Unit)? = null,
) {
    val tokens = LocalFinluxTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(categories, searchQuery) {
        if (searchQuery.isBlank()) categories
        else categories.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = tokens.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header: Title + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Chọn danh mục",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = tokens.onSurface,
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(34.dp)
                        .background(tokens.surfaceSoft, CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = tokens.onSurface,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            // Search Field (Rounded soft gray bar)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (tokens.isDark) Color(0xFF1E1E2D) else Color(0xFFF3F4F6),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(18.dp),
                    )
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = tokens.onSurface,
                        ),
                        cursorBrush = SolidColor(tokens.primary),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Tìm danh mục",
                                    style = TextStyle(fontSize = 14.sp, color = Color(0xFF9CA3AF)),
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.weight(1f),
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(20.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Xóa tìm kiếm",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }
            }

            // 4-Column Grid of Categories
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp),
            ) {
                items(filtered, key = { it.id }) { cat ->
                    val isSelected = cat.id == selectedCategoryId
                    val accent = colorFromHex(cat.colorHex, tokens.primary)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .combinedClickable(
                                onClick = {
                                    onSelectCategory(cat)
                                    onDismiss()
                                },
                                onLongClick = { onLongPressCategory?.invoke(cat) },
                            ),
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = accent.copy(alpha = if (tokens.isDark) 0.20f else 0.12f),
                                border = if (isSelected) BorderStroke(1.8.dp, tokens.primary) else null,
                                modifier = Modifier.size(54.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = categoryIcon(cat.icon),
                                        contentDescription = cat.name,
                                        tint = accent,
                                        modifier = Modifier.size(26.dp),
                                    )
                                }
                            }

                            // Selected Checkmark Badge
                            if (isSelected) {
                                Surface(
                                    shape = CircleShape,
                                    color = tokens.primary,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(1.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp),
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = cat.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                lineHeight = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            ),
                            color = if (isSelected) tokens.primary else tokens.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 28.dp),
                        )
                    }
                }
            }

            // Optional Bottom "+ Thêm danh mục mới" button
            if (onAddNew != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = tokens.primary.copy(alpha = if (tokens.isDark) 0.15f else 0.10f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onAddNew),
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 13.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = tokens.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Thêm danh mục mới",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = tokens.primary,
                        )
                    }
                }
            }
        }
    }
}

/** Backward compatibility alias for FinluxCategoryPickerBottomSheet */
@Composable
fun SimpleCategoryPickerSheet(
    categories: List<Category>,
    selectedCategoryId: String?,
    onSelectCategory: (Category) -> Unit,
    onDismiss: () -> Unit,
) = FinluxCategoryPickerBottomSheet(categories, selectedCategoryId, onSelectCategory, onDismiss)
