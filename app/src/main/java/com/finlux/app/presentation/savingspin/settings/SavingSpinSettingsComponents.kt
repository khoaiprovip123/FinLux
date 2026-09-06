@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.finlux.app.presentation.savingspin.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.component.FinluxAmountInputCard
import com.finlux.app.core.designsystem.component.FinluxBottomSheet
import com.finlux.app.core.designsystem.component.FinluxDialog
import com.finlux.app.core.designsystem.component.formatAmountDigitsWithDots
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SavingSpinFrequency
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.SavingSpinStatus
import com.finlux.app.domain.model.SavingSpinStep
import com.finlux.app.presentation.savingspin.SavingSpinUiState
import com.finlux.app.presentation.savingspin.components.SavingSpinHomeCard
import java.time.Instant

@Composable
internal fun SavingSpinAmountInputSheet(
    title: String,
    subtitle: String,
    initialAmount: Long,
    stepAmount: Long,
    presetAmounts: List<Long>,
    onDismissRequest: () -> Unit,
    onApply: (Long) -> Unit,
    minRequiredAmount: Long = stepAmount,
) {
    val tokens = LocalFinluxTokens.current
    var inputDigits by remember { mutableStateOf(if (initialAmount > 0) initialAmount.toString() else "") }
    val parsedAmount = inputDigits.toLongOrNull() ?: 0L

    // Tự động căn chỉnh theo step
    val isStepAligned = parsedAmount > 0 && (parsedAmount % stepAmount == 0L)
    val isMinSatisfied = parsedAmount >= minRequiredAmount

    FinluxBottomSheet(
        onDismissRequest = onDismissRequest,
        title = title,
        subtitle = subtitle,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Input Card đồng bộ
            FinluxAmountInputCard(
                amountDigits = inputDigits,
                onAmountChange = { inputDigits = it },
                label = "Số tiền (VNĐ)",
                showQuickChips = false,
            )

            // Preset Amount Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Mốc chọn nhanh:",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.onSurfaceVariant,
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    presetAmounts.forEach { preset ->
                        val isCurrent = parsedAmount == preset
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCurrent) tokens.primary else tokens.surfaceSoft,
                            border = BorderStroke(1.dp, if (isCurrent) tokens.primary else tokens.border),
                            modifier = Modifier.clickable { inputDigits = preset.toString() },
                        ) {
                            Text(
                                text = formatVndAmount(preset),
                                fontSize = 12.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) tokens.onHero else tokens.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            )
                        }
                    }
                }
            }

            // Quick increment chips (+5k, +10k, +50k, +100k)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Cộng thêm:",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(5_000L, 10_000L, 50_000L, 100_000L).forEach { inc ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = tokens.surfaceSoft,
                            border = BorderStroke(1.dp, tokens.border),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val current = inputDigits.toLongOrNull() ?: 0L
                                    inputDigits = (current + inc).toString()
                                },
                        ) {
                            Text(
                                text = "+${formatVndAmount(inc)}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                color = tokens.primary,
                                modifier = Modifier.padding(vertical = 7.dp),
                            )
                        }
                    }
                }
            }

            // Validation status hint
            if (parsedAmount > 0 && !isStepAligned) {
                Text(
                    text = "💡 Số tiền sẽ được tự động làm tròn về bội số của ${formatVndAmount(stepAmount)} khi áp dụng.",
                    fontSize = 12.sp,
                    color = tokens.onSurfaceVariant,
                )
            } else if (parsedAmount < minRequiredAmount && parsedAmount > 0) {
                Text(
                    text = "⚠️ Mức tối thiểu cần đạt ít nhất ${formatVndAmount(minRequiredAmount)} để đủ số ô.",
                    fontSize = 12.sp,
                    color = FinluxColors.ExpenseRed,
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = onDismissRequest,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, tokens.border),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.onSurfaceVariant),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                ) {
                    Text("Hủy", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        val rounded = if (parsedAmount % stepAmount != 0L) {
                            (parsedAmount / stepAmount) * stepAmount
                        } else parsedAmount
                        val finalAmount = rounded.coerceAtLeast(minRequiredAmount)
                        onApply(finalAmount)
                    },
                    enabled = parsedAmount > 0,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.primary,
                        contentColor = tokens.onHero,
                    ),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp),
                ) {
                    Text("Áp dụng", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
internal fun SettingsCard(content: @Composable () -> Unit) {
    val tokens = LocalFinluxTokens.current
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = tokens.surface,
        border = BorderStroke(1.dp, tokens.border),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column { content() }
    }
}

@Composable
internal fun SettingSwitchRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            Text(title, fontSize = 14.5.sp, fontWeight = FontWeight.Medium, color = tokens.onSurface)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = tokens.onHero,
                checkedTrackColor = tokens.primary,
                uncheckedTrackColor = tokens.surfaceSoft,
                uncheckedThumbColor = tokens.onSurfaceVariant,
            ),
        )
    }
}

@Composable
internal fun SettingActionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    onClick: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            Text(title, fontSize = 14.5.sp, fontWeight = FontWeight.Medium, color = tokens.onSurface)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(value, fontSize = 14.sp, color = tokens.onSurfaceVariant)
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = tokens.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}
