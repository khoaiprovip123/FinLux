package com.finlux.app.presentation.savingspin.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.component.FinluxScreenHeader
import com.finlux.app.core.designsystem.component.FinluxScreenScaffold
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SavingSpinFrequency
import com.finlux.app.domain.model.SavingSpinStep
import com.finlux.app.presentation.savingspin.components.SavingDestinationCard
import com.finlux.app.presentation.savingspin.components.SavingSpinWheel

@Composable
fun SavingSpinSettingsScreen(
    onBack: () -> Unit,
    onManageDestinations: () -> Unit,
    viewModel: SavingSpinSettingsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val tokens = LocalFinluxTokens.current
    FinluxScreenScaffold(
        topBar = { FinluxScreenHeader(title = "Vòng quay tiết kiệm", subtitle = "Thiết lập", onBack = onBack) },
    ) { padding ->
        if (state.isLoading) {
            Column(Modifier.fillMaxSize().padding(padding), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = tokens.primary)
            }
            return@FinluxScreenScaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(tokens.spacing.base),
            verticalArrangement = Arrangement.spacedBy(tokens.spacing.md),
        ) {
            item {
                SettingGroup {
                    SettingSwitch("Bật vòng quay tiết kiệm", "Tính năng tự nguyện, mặc định tắt", state.config.enabled, viewModel::setEnabled)
                    SettingSwitch("Hiển thị trên Trang chủ", "Xem nhanh trạng thái lượt hiện tại", state.config.showOnHome, viewModel::setShowOnHome)
                }
            }
            item {
                SettingGroup(title = "Mệnh giá") {
                    Text("Bước mệnh giá", style = FinluxTextStyles.Caption, color = tokens.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm)) {
                        SavingSpinStep.entries.forEach { step ->
                            FilterChip(
                                selected = state.config.step == step,
                                onClick = { viewModel.setStep(step) },
                                label = { Text(if (step == SavingSpinStep.FIVE_THOUSAND) "5.000đ" else "10.000đ") },
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm)) {
                        OutlinedTextField(
                            value = state.minAmountInput,
                            onValueChange = viewModel::setMinAmount,
                            label = { Text("Tối thiểu") },
                            suffix = { Text("đ") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = state.maxAmountInput,
                            onValueChange = viewModel::setMaxAmount,
                            label = { Text("Tối đa") },
                            suffix = { Text("đ") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Text("FinLux hỗ trợ tối đa 15 chữ số và không tự áp đặt hạn mức nhỏ.", style = FinluxTextStyles.Caption, color = tokens.onSurfaceVariant)
                    Text("Số ô vòng quay", style = FinluxTextStyles.Caption, color = tokens.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm)) {
                        listOf(6, 8, 10, 12).forEach { count ->
                            FilterChip(
                                selected = state.config.slotCount == count,
                                onClick = { viewModel.setSlotCount(count) },
                                label = { Text(count.toString()) },
                            )
                        }
                    }
                }
            }
            item {
                SettingGroup(title = "Lịch quay") {
                    SavingSpinFrequency.entries.forEach { frequency ->
                        FilterChip(
                            selected = state.config.frequency == frequency,
                            onClick = { viewModel.setFrequency(frequency) },
                            label = { Text(frequency.label()) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (state.config.frequency == SavingSpinFrequency.SELECTED_WEEKDAYS) {
                        Text("Các ngày được quay", style = FinluxTextStyles.Caption, color = tokens.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(tokens.spacing.xs)) {
                            (1..7).forEach { day ->
                                FilterChip(
                                    selected = day in state.config.selectedWeekdays,
                                    onClick = { viewModel.toggleSelectedWeekday(day) },
                                    label = { Text(day.dayLabel()) },
                                )
                            }
                        }
                    }
                    if (state.config.frequency == SavingSpinFrequency.WEEKLY) {
                        Text("Ngày quay hằng tuần", style = FinluxTextStyles.Caption, color = tokens.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(tokens.spacing.xs)) {
                            (1..7).forEach { day ->
                                FilterChip(
                                    selected = day == state.config.weeklyDay,
                                    onClick = { viewModel.setWeeklyDay(day) },
                                    label = { Text(day.dayLabel()) },
                                )
                            }
                        }
                    }
                }
            }
            item {
                SettingGroup(title = "Nhắc nhở") {
                    SettingSwitch("Bật nhắc nhở", "Mặc định lúc 09:00", state.config.reminderEnabled, viewModel::setReminderEnabled)
                    Row(horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm)) {
                        OutlinedTextField(
                            value = state.config.reminderHour.toString(),
                            onValueChange = { viewModel.setReminderHour(it.toIntOrNull() ?: 0) },
                            label = { Text("Giờ") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = state.config.reminderMinute.toString(),
                            onValueChange = { viewModel.setReminderMinute(it.toIntOrNull() ?: 0) },
                            label = { Text("Phút") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    SettingSwitch("Cho phép nhắc sau", "30 phút, 1 giờ, 12:00 hoặc 18:00", state.config.snoozeEnabled, viewModel::setSnoozeEnabled)
                    SettingSwitch("Cho phép bỏ qua", "Lượt bỏ qua không tính vào tổng tiết kiệm", state.config.allowSkip, viewModel::setAllowSkip)
                }
            }
            item {
                SettingGroup(title = "Nơi tiết kiệm mặc định") {
                    if (state.destinations.isEmpty()) {
                        Button(onClick = onManageDestinations, modifier = Modifier.fillMaxWidth()) { Text("+ Thêm nơi tiết kiệm") }
                    } else {
                        state.destinations.filter { it.enabled }.forEach { destination ->
                            SavingDestinationCard(
                                destination = destination,
                                selected = destination.id == state.config.defaultDestinationId,
                                onClick = { viewModel.setDefaultDestination(destination.id) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        Button(onClick = onManageDestinations, modifier = Modifier.fillMaxWidth()) { Text("Quản lý nơi tiết kiệm") }
                    }
                }
            }
            item {
                SettingGroup(title = "Xem trước") {
                    SavingSpinWheel(
                        values = previewValues(state.config.slotCount, state.config.step.amount),
                        selectedIndex = null,
                        isSpinning = false,
                        modifier = Modifier.size(280.dp).align(Alignment.CenterHorizontally),
                    )
                }
            }
            item {
                state.validationMessage?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
                Button(
                    onClick = viewModel::save,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.primary, contentColor = tokens.onHero),
                ) {
                    if (state.isSaving) CircularProgressIndicator(Modifier.size(20.dp), color = tokens.onHero)
                    else Text(if (state.saved) "Đã lưu" else "Lưu cài đặt", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SettingGroup(title: String? = null, content: @Composable ColumnScope.() -> Unit) {
    val tokens = LocalFinluxTokens.current
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(tokens.spacing.md)) {
            title?.let { Text(it, style = FinluxTextStyles.CardTitle, color = tokens.onSurface, fontWeight = FontWeight.Bold) }
            content()
        }
    }
}

@Composable
private fun SettingSwitch(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val tokens = LocalFinluxTokens.current
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = FinluxTextStyles.Body, color = tokens.onSurface)
            Text(subtitle, style = FinluxTextStyles.Caption, color = tokens.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun SavingSpinFrequency.label() = when (this) {
    SavingSpinFrequency.DAILY -> "Mỗi ngày"
    SavingSpinFrequency.SELECTED_WEEKDAYS -> "Theo thứ đã chọn"
    SavingSpinFrequency.WEEKLY -> "Mỗi tuần"
    SavingSpinFrequency.SALARY_CYCLE -> "Theo chu kỳ lương"
}

private fun Int.dayLabel() = if (this == 7) "CN" else "T${this + 1}"
private fun previewValues(count: Int, step: Long) = List(count) { Money((it + 1L) * step) }
