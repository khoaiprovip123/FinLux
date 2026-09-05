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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingSpinSettingsScreen(
    onBack: () -> Unit,
    onManageDestinations: () -> Unit,
    viewModel: SavingSpinSettingsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val tokens = LocalFinluxTokens.current

    var showMinBottomSheet by remember { mutableStateOf(false) }
    var showMaxBottomSheet by remember { mutableStateOf(false) }
    var showSlotCountSheet by remember { mutableStateOf(false) }
    var showFrequencySheet by remember { mutableStateOf(false) }
    var showTimePickerSheet by remember { mutableStateOf(false) }

    // Success Popup Dialog
    if (state.saved) {
        FinluxDialog(
            onDismissRequest = viewModel::dismissSaved,
            title = "Thiết lập thành công!",
            message = "Cài đặt vòng quay tiết kiệm của bạn đã được lưu và cập nhật ngay vào hệ thống.",
            confirmLabel = "Đã hiểu",
            onConfirm = viewModel::dismissSaved,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = CircleShape,
                    color = tokens.primary.copy(alpha = if (tokens.isDark) 0.20f else 0.12f),
                    modifier = Modifier.size(64.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = tokens.primary,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet: Nhập Mức tối thiểu
    if (showMinBottomSheet) {
        SavingSpinAmountInputSheet(
            title = "Mức tiết kiệm tối thiểu",
            subtitle = "Số tiền nhỏ nhất xuất hiện trên các ô vòng quay",
            initialAmount = state.config.minAmount.value,
            stepAmount = state.config.step.amount,
            presetAmounts = listOf(10_000L, 20_000L, 30_000L, 50_000L, 100_000L, 200_000L),
            onDismissRequest = { showMinBottomSheet = false },
            onApply = { newAmount ->
                viewModel.setMinAmount(newAmount.toString())
                showMinBottomSheet = false
            },
        )
    }

    // Modal Sheet: Nhập Mức tối đa
    if (showMaxBottomSheet) {
        val minRequired = state.config.minAmount.value + (state.config.slotCount * state.config.step.amount)
        SavingSpinAmountInputSheet(
            title = "Mức tiết kiệm tối đa",
            subtitle = "Số tiền lớn nhất xuất hiện trên các ô vòng quay",
            initialAmount = state.config.maxAmount.value.coerceAtLeast(minRequired),
            stepAmount = state.config.step.amount,
            minRequiredAmount = minRequired,
            presetAmounts = listOf(50_000L, 100_000L, 200_000L, 300_000L, 500_000L, 1_000_000L),
            onDismissRequest = { showMaxBottomSheet = false },
            onApply = { newAmount ->
                viewModel.setMaxAmount(newAmount.toString())
                showMaxBottomSheet = false
            },
        )
    }

    // Modal Sheet: Chọn Số ô vòng quay (6/8/10/12 ô)
    if (showSlotCountSheet) {
        FinluxBottomSheet(
            onDismissRequest = { showSlotCountSheet = false },
            title = "Số ô vòng quay",
            subtitle = "Chọn số lượng ô chia đều trên vòng quay tiết kiệm",
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val slotOptions = listOf(
                    SlotOption(6, "6 ô vòng quay", "Nhanh gọn • Thích hợp cho mức tiền tập trung"),
                    SlotOption(8, "8 ô vòng quay", "Tiêu chuẩn • Cân bằng & phổ biến nhất (Khuyên dùng)"),
                    SlotOption(10, "10 ô vòng quay", "Đa dạng • Nhiều mức tiền tích lũy khác nhau"),
                    SlotOption(12, "12 ô vòng quay", "Tối đa • Nhiều cơ hội bất ngờ và phong phú nhất"),
                )

                slotOptions.forEach { opt ->
                    val isSelected = state.config.slotCount == opt.count
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) tokens.primary.copy(alpha = if (tokens.isDark) 0.18f else 0.10f) else tokens.surfaceSoft,
                        border = BorderStroke(
                            1.5.dp,
                            if (isSelected) tokens.primary else tokens.border,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setSlotCount(opt.count)
                                showSlotCountSheet = false
                            },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = opt.title,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) tokens.primary else tokens.onSurface,
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = opt.description,
                                    fontSize = 12.5.sp,
                                    color = tokens.onSurfaceVariant,
                                )
                            }
                            if (isSelected) {
                                Surface(
                                    shape = CircleShape,
                                    color = tokens.primary,
                                    modifier = Modifier.size(24.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = tokens.onHero,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet: Chọn Tần suất
    if (showFrequencySheet) {
        FinluxBottomSheet(
            onDismissRequest = { showFrequencySheet = false },
            title = "Tần suất vòng quay",
            subtitle = "Lịch xuất hiện và nhắc nhở quay vòng quay",
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val freqOptions = listOf(
                    Triple(SavingSpinFrequency.DAILY, "Mỗi ngày", "Quay đều đặn mỗi ngày để duy trì thói quen tiết kiệm"),
                    Triple(SavingSpinFrequency.SELECTED_WEEKDAYS, "Theo thứ trong tuần", "Tùy chọn các ngày cụ thể trong tuần"),
                    Triple(SavingSpinFrequency.WEEKLY, "Hằng tuần", "Quay 1 lần vào ngày cố định mỗi tuần"),
                    Triple(SavingSpinFrequency.SALARY_CYCLE, "Theo kỳ lương", "Quay vào đầu mỗi kỳ nhận lương mới"),
                )

                freqOptions.forEach { (freq, label, desc) ->
                    val isSelected = state.config.frequency == freq
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) tokens.primary.copy(alpha = if (tokens.isDark) 0.18f else 0.10f) else tokens.surfaceSoft,
                        border = BorderStroke(
                            1.5.dp,
                            if (isSelected) tokens.primary else tokens.border,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setFrequency(freq)
                                if (freq != SavingSpinFrequency.SELECTED_WEEKDAYS && freq != SavingSpinFrequency.WEEKLY) {
                                    showFrequencySheet = false
                                }
                            },
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = label,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSelected) tokens.primary else tokens.onSurface,
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = desc,
                                        fontSize = 12.5.sp,
                                        color = tokens.onSurfaceVariant,
                                    )
                                }
                                if (isSelected) {
                                    Surface(
                                        shape = CircleShape,
                                        color = tokens.primary,
                                        modifier = Modifier.size(24.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = tokens.onHero,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    }
                                }
                            }

                            // Sub-selector for weekdays if selected
                            if (isSelected && freq == SavingSpinFrequency.SELECTED_WEEKDAYS) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Chọn các ngày:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = tokens.primary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    val days = listOf("T2" to 1, "T3" to 2, "T4" to 3, "T5" to 4, "T6" to 5, "T7" to 6, "CN" to 7)
                                    days.forEach { (name, value) ->
                                        val daySelected = value in state.config.selectedWeekdays
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (daySelected) tokens.primary else tokens.surface,
                                            border = BorderStroke(1.dp, if (daySelected) tokens.primary else tokens.border),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { viewModel.toggleSelectedWeekday(value) },
                                        ) {
                                            Text(
                                                text = name,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                color = if (daySelected) tokens.onHero else tokens.onSurface,
                                                modifier = Modifier.padding(vertical = 8.dp),
                                            )
                                        }
                                    }
                                }
                            }

                            // Sub-selector for single weekly day
                            if (isSelected && freq == SavingSpinFrequency.WEEKLY) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Chọn ngày trong tuần:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = tokens.primary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    val days = listOf("T2" to 1, "T3" to 2, "T4" to 3, "T5" to 4, "T6" to 5, "T7" to 6, "CN" to 7)
                                    days.forEach { (name, value) ->
                                        val daySelected = value == state.config.weeklyDay
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (daySelected) tokens.primary else tokens.surface,
                                            border = BorderStroke(1.dp, if (daySelected) tokens.primary else tokens.border),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { viewModel.setWeeklyDay(value) },
                                        ) {
                                            Text(
                                                text = name,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                color = if (daySelected) tokens.onHero else tokens.onSurface,
                                                modifier = Modifier.padding(vertical = 8.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet: Chọn Giờ nhắc
    if (showTimePickerSheet) {
        var tempHour by remember { mutableIntStateOf(state.config.reminderHour) }
        var tempMinute by remember { mutableIntStateOf(state.config.reminderMinute) }

        FinluxBottomSheet(
            onDismissRequest = { showTimePickerSheet = false },
            title = "Giờ nhắc nhở",
            subtitle = "Hẹn giờ thông báo quay tiết kiệm hàng ngày",
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Quick preset pills
                Text("Gợi ý khung giờ:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = tokens.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val presets = listOf("08:00" to (8 to 0), "12:00" to (12 to 0), "18:00" to (18 to 0), "20:00" to (20 to 0), "21:30" to (21 to 30))
                    presets.forEach { (label, pair) ->
                        val isPreset = tempHour == pair.first && tempMinute == pair.second
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isPreset) tokens.primary else tokens.surfaceSoft,
                            border = BorderStroke(1.dp, if (isPreset) tokens.primary else tokens.border),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    tempHour = pair.first
                                    tempMinute = pair.second
                                },
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = if (isPreset) tokens.onHero else tokens.onSurface,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }
                    }
                }

                // Time adjustment controls
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = tokens.surfaceSoft,
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        // Hour
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { tempHour = (tempHour + 1) % 24 }) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = tokens.primary)
                            }
                            Text(
                                text = String.format("%02d", tempHour),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = tokens.onSurface,
                            )
                            IconButton(onClick = { tempHour = (tempHour - 1 + 24) % 24 }) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = tokens.primary)
                            }
                        }

                        Text(
                            text = " : ",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = tokens.primary,
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )

                        // Minute
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { tempMinute = (tempMinute + 5) % 60 }) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = tokens.primary)
                            }
                            Text(
                                text = String.format("%02d", tempMinute),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = tokens.onSurface,
                            )
                            IconButton(onClick = { tempMinute = (tempMinute - 5 + 60) % 60 }) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = tokens.primary)
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.setReminderHour(tempHour)
                        viewModel.setReminderMinute(tempMinute)
                        showTimePickerSheet = false
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.primary, contentColor = tokens.onHero),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Text("Xác nhận giờ nhắc", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = tokens.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = tokens.surfaceSoft,
                        border = BorderStroke(1.dp, tokens.border),
                        modifier = Modifier.size(38.dp),
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = tokens.onSurface,
                            )
                        }
                    }
                    Text(
                        text = "Cài đặt vòng quay",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = tokens.onSurface,
                    )
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = tokens.primary)
                }
                return@Surface
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Card 1: Bật vòng quay & Hiển thị trên Trang chủ
                item {
                    SettingsCard {
                        SettingSwitchRow(
                            icon = Icons.Filled.TrackChanges,
                            iconTint = tokens.primary,
                            title = "Bật vòng quay tiết kiệm",
                            checked = state.config.enabled,
                            onCheckedChange = viewModel::setEnabled,
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(tokens.border))
                        SettingSwitchRow(
                            icon = Icons.Filled.Home,
                            iconTint = tokens.primary,
                            title = "Hiển thị trên Trang chủ",
                            checked = state.config.showOnHome,
                            onCheckedChange = viewModel::setShowOnHome,
                        )
                    }
                }

                // Card 2: Giờ nhắc & Nhắc lại khi chưa thực hiện
                item {
                    SettingsCard {
                        SettingActionRow(
                            icon = Icons.Filled.AccessTime,
                            iconTint = tokens.primary,
                            title = "Giờ nhắc",
                            value = String.format("%02d:%02d", state.config.reminderHour, state.config.reminderMinute),
                            onClick = { showTimePickerSheet = true },
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(tokens.border))
                        SettingSwitchRow(
                            icon = Icons.Filled.Notifications,
                            iconTint = tokens.primary,
                            title = "Nhắc lại khi chưa thực hiện",
                            checked = state.config.snoozeEnabled,
                            onCheckedChange = viewModel::setSnoozeEnabled,
                        )
                    }
                }

                // Card 3: Bước mệnh giá & Mức tối thiểu / tối đa
                item {
                    SettingsCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Filled.Savings, contentDescription = null, tint = tokens.primary, modifier = Modifier.size(22.dp))
                                Text("Bước mệnh giá", fontSize = 14.5.sp, fontWeight = FontWeight.Medium, color = tokens.onSurface)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val is5k = state.config.step == SavingSpinStep.FIVE_THOUSAND
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (is5k) tokens.primary else tokens.surfaceSoft,
                                    border = BorderStroke(1.dp, if (is5k) tokens.primary else tokens.border),
                                    modifier = Modifier.clickable { viewModel.setStep(SavingSpinStep.FIVE_THOUSAND) },
                                ) {
                                    Text(
                                        text = "5.000đ",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (is5k) tokens.onHero else tokens.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    )
                                }
                                val is10k = state.config.step == SavingSpinStep.TEN_THOUSAND
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (is10k) tokens.primary else tokens.surfaceSoft,
                                    border = BorderStroke(1.dp, if (is10k) tokens.primary else tokens.border),
                                    modifier = Modifier.clickable { viewModel.setStep(SavingSpinStep.TEN_THOUSAND) },
                                ) {
                                    Text(
                                        text = "10.000đ",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (is10k) tokens.onHero else tokens.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(tokens.border))

                        SettingActionRow(
                            icon = Icons.Filled.AttachMoney,
                            iconTint = tokens.primary,
                            title = "Mức tối thiểu",
                            value = formatVndAmount(state.config.minAmount.value),
                            onClick = { showMinBottomSheet = true },
                        )

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(tokens.border))

                        SettingActionRow(
                            icon = Icons.Filled.AttachMoney,
                            iconTint = tokens.primary,
                            title = "Mức tối đa",
                            value = formatVndAmount(state.config.maxAmount.value),
                            onClick = { showMaxBottomSheet = true },
                        )
                    }
                }

                // Card 4: Số ô vòng quay (6/8/10/12), Tần suất, Cho phép bỏ qua hôm nay
                item {
                    SettingsCard {
                        SettingActionRow(
                            icon = Icons.Filled.GridOn,
                            iconTint = tokens.primary,
                            title = "Số ô vòng quay",
                            value = "${state.config.slotCount} ô",
                            onClick = { showSlotCountSheet = true },
                        )

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(tokens.border))

                        SettingActionRow(
                            icon = Icons.Filled.Repeat,
                            iconTint = tokens.primary,
                            title = "Tần suất",
                            value = when (state.config.frequency) {
                                SavingSpinFrequency.DAILY -> "Mỗi ngày"
                                SavingSpinFrequency.SELECTED_WEEKDAYS -> "Theo thứ trong tuần"
                                SavingSpinFrequency.WEEKLY -> "Hằng tuần"
                                SavingSpinFrequency.SALARY_CYCLE -> "Theo kỳ lương"
                            },
                            onClick = { showFrequencySheet = true },
                        )

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(tokens.border))

                        SettingSwitchRow(
                            icon = Icons.Filled.SkipNext,
                            iconTint = tokens.primary,
                            title = "Cho phép bỏ qua hôm nay",
                            checked = state.config.allowSkip,
                            onCheckedChange = viewModel::setAllowSkip,
                        )
                    }
                }

                // Card 5: Nơi tiết kiệm / ví đích
                item {
                    SettingsCard {
                        SettingActionRow(
                            icon = Icons.Filled.Savings,
                            iconTint = tokens.primary,
                            title = "Nơi tiết kiệm",
                            value = if (state.destinations.isEmpty()) {
                                "Chưa thiết lập"
                            } else {
                                "${state.destinations.size} nơi"
                            },
                            onClick = onManageDestinations,
                        )
                    }
                }

                // Section 5: Xem trước trên Trang chủ
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.Visibility, contentDescription = null, tint = tokens.primary, modifier = Modifier.size(16.dp))
                            Text("Xem trước trên Trang chủ", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = tokens.primary)
                        }

                        SavingSpinHomeCard(
                            state = SavingSpinUiState(
                                config = state.config.copy(enabled = true, showOnHome = true),
                                session = SavingSpinSession(
                                    id = "preview",
                                    scheduleKey = "preview",
                                    status = SavingSpinStatus.READY,
                                    wheelValues = (1..state.config.slotCount).map { i ->
                                        Money(state.config.minAmount.value + (i - 1) * state.config.step.amount)
                                    },
                                    createdAt = Instant.now(),
                                ),
                            ),
                            onOpen = {},
                        )
                    }
                }

                // Hiển thị lỗi xác thực nếu có
                state.validationMessage?.let { errorMsg ->
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = FinluxColors.ExpenseRed.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, FinluxColors.ExpenseRed.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "⚠️ $errorMsg",
                                fontSize = 13.sp,
                                color = FinluxColors.ExpenseRed,
                                modifier = Modifier.padding(14.dp),
                            )
                        }
                    }
                }

                // Nút Lưu thay đổi
                item {
                    Button(
                        onClick = viewModel::save,
                        enabled = !state.isSaving,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tokens.primary,
                            contentColor = tokens.onHero,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    ) {
                        Text(
                            text = if (state.isSaving) "Đang lưu..." else "Lưu cài đặt",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

private data class SlotOption(val count: Int, val title: String, val description: String)

/**
 * Modal BottomSheet nhập số tiền đồng bộ hệ thống FinLux Tokens & Liquid Glass
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SavingSpinAmountInputSheet(
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
private fun SettingsCard(content: @Composable () -> Unit) {
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
private fun SettingSwitchRow(
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
private fun SettingActionRow(
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
