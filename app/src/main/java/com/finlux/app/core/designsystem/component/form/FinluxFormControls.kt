package com.finlux.app.core.designsystem.component.form

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.text.AnnotatedString
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
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.findInstitutionForWallet
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.Wallet
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Format Instant into Vietnamese smart human-readable date & time:
 * - Today: "Hôm nay, dd/MM/yyyy • HH:mm"
 * - Yesterday: "Hôm qua, dd/MM/yyyy • HH:mm"
 * - Other: "dd/MM/yyyy • HH:mm"
 */
fun formatSmartDateTime(
    instant: Instant,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String {
    val localDateTime = instant.atZone(zoneId)
    val localDate = localDateTime.toLocalDate()
    val today = LocalDate.now(zoneId)
    val prefix = when (localDate) {
        today -> "Hôm nay, "
        today.minusDays(1) -> "Hôm qua, "
        else -> ""
    }
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm")
    return prefix + localDateTime.format(formatter)
}

/**
 * Standard Finlux Date & Time Picker Control.
 * Integrated 2-in-1 DatePicker + TimePicker (24-hour mode).
 * Adheres 100% to Directive #1 (Dynamic Tokens) and Directive #2 (Reusability).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinluxDateTimePicker(
    selectedDateTime: Instant,
    onDateTimeChange: (Instant) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "THỜI GIAN GIAO DỊCH",
    icon: ImageVector = Icons.Default.CalendarMonth,
    iconBgColor: Color = Color(0xFF6366F1).copy(alpha = 0.14f),
    iconTintColor: Color = Color(0xFF6366F1),
    enabled: Boolean = true,
) {
    val tokens = LocalFinluxTokens.current
    val context = LocalContext.current
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val formattedText = remember(selectedDateTime) {
        formatSmartDateTime(selectedDateTime)
    }

    // Row Clickable Form Card
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = tokens.surfaceSoft,
        border = BorderStroke(1.dp, tokens.border),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = { showDatePickerDialog = true },
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
                    Icon(icon, contentDescription = null, tint = iconTintColor, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    ),
                    color = tokens.onSurfaceVariant,
                )
                Text(
                    text = formattedText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = tokens.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(14.dp),
            )
        }
    }

    // Two-step DatePicker -> TimePicker Dialogs
    if (showDatePickerDialog) {
        val currentZoned = remember(selectedDateTime) { selectedDateTime.atZone(ZoneId.systemDefault()) }
        val initialUtcMillis = remember(currentZoned) {
            currentZoned.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialUtcMillis,
        )

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    showDatePickerDialog = false
                    if (selectedMillis != null) {
                        val selectedLocalDate = Instant.ofEpochMilli(selectedMillis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()

                        val timePickerDialog = TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                val newDateTime = selectedLocalDate.atTime(hourOfDay, minute)
                                val newInstant = newDateTime.atZone(ZoneId.systemDefault()).toInstant()
                                onDateTimeChange(newInstant)
                            },
                            currentZoned.hour,
                            currentZoned.minute,
                            true, // 24-hour format
                        )
                        timePickerDialog.setOnCancelListener {
                            // Preserve selected date with existing time on dialog dismiss
                            val newDateTime = selectedLocalDate.atTime(currentZoned.hour, currentZoned.minute)
                            val newInstant = newDateTime.atZone(ZoneId.systemDefault()).toInstant()
                            onDateTimeChange(newInstant)
                        }
                        timePickerDialog.show()
                    }
                }) {
                    Text("Tiếp tục (Chọn giờ)", color = tokens.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Hủy", color = tokens.onSurfaceVariant)
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

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
 * Helper to generate smart amount multiplier suggestions based on Decimal Magnitude Scaling.
 */
fun generateAmountSuggestions(rawInput: String): List<Pair<String, String>> {
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

    val startK = when {
        baseNumber < 10L -> 3
        baseNumber < 100L -> 2
        baseNumber < 1_000L -> 1
        else -> 1
    }

    val list = mutableListOf<Pair<String, String>>()
    var currentMultiplier = 1L
    for (i in 0 until startK) {
        currentMultiplier *= 10L
    }

    while (list.size < maxChips) {
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
 * Standard Finlux Amount Input Control.
 * Realtime Vietnamese Dot Separator Formatting (e.g. 100.000), inline ₫ symbol,
 * dynamic responsive font size downscaling, clear button, and quick suggestion chips.
 */
@Composable
fun FinluxAmountInput(
    amountText: String,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "SỐ TIỀN",
    placeholder: String = "0",
    amountColor: Color = LocalFinluxTokens.current.primary,
    amountFontSize: TextUnit = 32.sp,
    quickAmounts: List<Long>? = null,
    leadingActionChip: Pair<String, () -> Unit>? = null,
    warningMessage: String? = null,
    showQuickChips: Boolean = true,
    isReadOnly: Boolean = false,
    enabled: Boolean = true,
    maxDigits: Int = 13,
) {
    val tokens = LocalFinluxTokens.current
    var isFocused by remember { mutableStateOf(false) }
    val cleanDigits = amountText.filter { it.isDigit() }.take(maxDigits)
    val formattedDisplay = formatAmountDigitsWithDots(cleanDigits)

    // Dynamic auto-scaling font size to avoid overflow on big numbers
    val effectiveFontSize = when {
        cleanDigits.length >= 11 -> (amountFontSize.value * 0.70f).sp
        cleanDigits.length >= 9 -> (amountFontSize.value * 0.82f).sp
        else -> amountFontSize
    }

    val defaultQuickAmounts = remember {
        listOf(50_000L, 100_000L, 200_000L, 500_000L, 1_000_000L, 2_000_000L)
    }
    val effectiveQuickAmounts = quickAmounts ?: defaultQuickAmounts

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = tokens.surfaceSoft,
        border = BorderStroke(
            1.dp,
            when {
                warningMessage != null -> Color(0xFFEF4444).copy(alpha = 0.6f)
                isFocused -> amountColor.copy(alpha = 0.5f)
                else -> tokens.border
            },
        ),
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Label
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

            // Input Row: Value + Inline ₫ + Clear Button [x]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isReadOnly) {
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
                            val digits = tfv.text.filter { it.isDigit() }.take(maxDigits)
                            onAmountChange(digits)
                        },
                        textStyle = TextStyle(
                            fontSize = effectiveFontSize,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (warningMessage != null) Color(0xFFEF4444) else amountColor,
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
                                        fontWeight = FontWeight.ExtraBold,
                                        color = tokens.onSurfaceVariant.copy(alpha = 0.35f),
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

                // Clear button [x]
                if (!isReadOnly && cleanDigits.isNotEmpty() && enabled) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (tokens.isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE5E7EB))
                            .clickable { onAmountChange("") },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Xóa số tiền",
                            tint = if (tokens.isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF6B7280),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }

            // Warning message banner if present
            if (warningMessage != null) {
                Text(
                    text = warningMessage,
                    color = Color(0xFFEF4444),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            // Quick suggestion chips
            if (showQuickChips && !isReadOnly && enabled) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                ) {
                    // Optional Leading Action Chip (e.g. "Tất cả")
                    if (leadingActionChip != null) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = tokens.primary.copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, tokens.primary.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(onClick = leadingActionChip.second),
                            ) {
                                Text(
                                    text = leadingActionChip.first,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = tokens.primary,
                                        fontSize = 12.sp,
                                    ),
                                )
                            }
                        }
                    }

                    // Incremental Quick Amount Chips
                    items(effectiveQuickAmounts) { amt ->
                        val chipText = when {
                            amt >= 1_000_000L && amt % 1_000_000L == 0L -> "+${amt / 1_000_000L}tr"
                            amt >= 1_000_000L -> "+${amt / 1_000_000f}tr"
                            amt >= 1_000L -> "+${amt / 1_000L}k"
                            else -> "+$amt"
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = tokens.surface,
                            border = BorderStroke(1.dp, tokens.border),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    val currentVal = cleanDigits.toLongOrNull() ?: 0L
                                    val updated = currentVal + amt
                                    onAmountChange(updated.toString())
                                },
                        ) {
                            Text(
                                text = chipText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = tokens.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Standard Finlux Note Input Control.
 * Ergonomic card with icon badge, clean label, placeholder, and clear [x] button.
 */
@Composable
fun FinluxNoteInput(
    note: String,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "GHI CHÚ GIAO DỊCH",
    placeholder: String = "Nhập ghi chú giao dịch (tùy chọn)",
    icon: ImageVector = Icons.Default.EditNote,
    iconBgColor: Color = Color(0xFF06B6D4).copy(alpha = 0.14f),
    iconTintColor: Color = Color(0xFF0891B2),
    maxLength: Int = 200,
    enabled: Boolean = true,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = tokens.surfaceSoft,
        border = BorderStroke(1.dp, tokens.border),
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
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
                    Icon(icon, contentDescription = null, tint = iconTintColor, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    ),
                    color = tokens.onSurfaceVariant,
                )
                BasicTextField(
                    value = note,
                    onValueChange = { if (it.length <= maxLength) onNoteChange(it) },
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = tokens.onSurface,
                    ),
                    cursorBrush = SolidColor(tokens.primary),
                    singleLine = true,
                    enabled = enabled,
                    decorationBox = { innerTextField ->
                        if (note.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = TextStyle(
                                    fontSize = 14.5.sp,
                                    color = tokens.onSurfaceVariant.copy(alpha = 0.6f),
                                ),
                            )
                        }
                        innerTextField()
                    },
                )
            }

            if (note.isNotEmpty() && enabled) {
                IconButton(
                    onClick = { onNoteChange("") },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Xóa ghi chú",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

/**
 * Standard Finlux Wallet Selector Row Card.
 * Displays financial institution logo / wallet badge, wallet name, balance, and chevron.
 */
@Composable
fun FinluxWalletSelector(
    label: String,
    selectedWallet: Wallet?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Chọn ví tài khoản",
    subtitle: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = tokens.surfaceSoft,
        border = BorderStroke(
            1.dp,
            if (isError) Color(0xFFEF4444).copy(alpha = 0.5f) else tokens.border,
        ),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                enabled = enabled,
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
            // Wallet Logo or Badge
            if (selectedWallet != null) {
                FinancialInstitutionLogo(
                    institution = findInstitutionForWallet(selectedWallet.name),
                    walletType = selectedWallet.type,
                    customColorHex = selectedWallet.colorHex,
                    size = 42.dp,
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = tokens.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(42.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = tokens.primary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    ),
                    color = tokens.onSurfaceVariant,
                )
                Text(
                    text = selectedWallet?.name ?: placeholder,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = if (selectedWallet != null) tokens.onSurface else tokens.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val effectiveSubtitle = subtitle ?: selectedWallet?.let {
                    "Số dư: ${formatVndAmount(it.balance.value)}"
                }
                if (effectiveSubtitle != null) {
                    Text(
                        text = effectiveSubtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = if (isError) Color(0xFFEF4444) else tokens.onSurfaceVariant,
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
 * Standard Bento Box for paired Transfer Source and Destination Wallet selection.
 * With an interactive Swap button in the divider.
 */
@Composable
fun FinluxTransferWalletPair(
    sourceWallet: Wallet?,
    destWallet: Wallet?,
    onSelectSource: () -> Unit,
    onSelectDest: () -> Unit,
    onSwap: () -> Unit,
    modifier: Modifier = Modifier,
    sourceSubtitle: String? = null,
    destSubtitle: String? = null,
    isSourceError: Boolean = false,
    errorMessage: String? = null,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = tokens.surfaceSoft,
        border = BorderStroke(1.dp, if (errorMessage != null) Color(0xFFEF4444).copy(alpha = 0.4f) else tokens.border),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Source Wallet
            Text(
                text = "TỪ VÍ NGUỒN",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                ),
                color = tokens.onSurfaceVariant,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(tokens.surface)
                    .clickable(onClick = onSelectSource)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (sourceWallet != null) {
                    FinancialInstitutionLogo(
                        institution = findInstitutionForWallet(sourceWallet.name),
                        walletType = sourceWallet.type,
                        customColorHex = sourceWallet.colorHex,
                        size = 40.dp,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(tokens.primary.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = tokens.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sourceWallet?.name ?: "Chọn ví chuyển đi",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = tokens.onSurface,
                    )
                    Text(
                        text = sourceSubtitle ?: "Khả dụng: ${formatVndAmount(sourceWallet?.balance?.value ?: 0L)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        color = if (isSourceError) Color(0xFFEF4444) else tokens.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(14.dp),
                )
            }

            // Swap Button Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(tokens.border.copy(alpha = 0.6f)),
                )
                IconButton(
                    onClick = onSwap,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(tokens.primary.copy(alpha = 0.14f)),
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Đổi chiều chuyển tiền",
                        tint = tokens.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(tokens.border.copy(alpha = 0.6f)),
                )
            }

            // Destination Wallet
            Text(
                text = "ĐẾN VÍ NHẬN",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                ),
                color = tokens.onSurfaceVariant,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(tokens.surface)
                    .clickable(onClick = onSelectDest)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (destWallet != null) {
                    FinancialInstitutionLogo(
                        institution = findInstitutionForWallet(destWallet.name),
                        walletType = destWallet.type,
                        customColorHex = destWallet.colorHex,
                        size = 40.dp,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = destWallet?.name ?: "Chọn ví nhận tiền",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = tokens.onSurface,
                    )
                    Text(
                        text = destSubtitle ?: "Hiện tại: ${formatVndAmount(destWallet?.balance?.value ?: 0L)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        color = tokens.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(14.dp),
                )
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFEF4444),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}

/**
 * Shared, reusable Pixel-Perfect Hero Amount Input Card.
 * Ergonomically delegates to FinluxAmountInput for unified state & styling.
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
    FinluxAmountInput(
        amountText = amountDigits,
        onAmountChange = onAmountChange,
        label = label,
        amountColor = primaryColor,
        quickAmounts = quickAmounts,
        showQuickChips = showQuickChips,
        modifier = modifier,
    )
}

/**
 * Reusable Ergonomic Compact Amount Input / Display Card.
 * Ergonomically delegates to FinluxAmountInput.
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
    FinluxAmountInput(
        amountText = amountText,
        onAmountChange = onAmountChange ?: {},
        label = label,
        placeholder = placeholder,
        amountColor = amountColor,
        amountFontSize = amountFontSize,
        showQuickChips = showSuggestions,
        isReadOnly = isReadOnly,
        enabled = enabled,
        modifier = modifier,
    )
}

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
                                    fontWeight = FontWeight.Normal,
                                ),
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (value.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onValueChange("")
                        onClear()
                    },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(16.dp),
                    )
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
