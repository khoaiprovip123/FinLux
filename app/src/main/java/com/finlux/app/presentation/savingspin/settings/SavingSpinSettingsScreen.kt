package com.finlux.app.presentation.savingspin.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.domain.model.SavingSpinStep
import com.finlux.app.presentation.savingspin.components.SavingSpinHomeCard
import com.finlux.app.presentation.savingspin.SavingSpinUiState
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SavingSpinStatus
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import com.finlux.app.core.designsystem.component.formatVndAmount
import java.time.Instant

@Composable
fun SavingSpinSettingsScreen(
    onBack: () -> Unit,
    onManageDestinations: () -> Unit,
    viewModel: SavingSpinSettingsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    var showMinDialog by remember { mutableStateOf(false) }
    var showMaxDialog by remember { mutableStateOf(false) }
    var tempMinInput by remember(state.config.minAmount) { mutableStateOf(state.config.minAmount.value.toString()) }
    var tempMaxInput by remember(state.config.maxAmount) { mutableStateOf(state.config.maxAmount.value.toString()) }

    if (showMinDialog) {
        AlertDialog(
            onDismissRequest = { showMinDialog = false },
            title = { Text("Mức tối thiểu", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Nhập mức tiền tối thiểu mỗi ô (phải chia hết cho ${formatVndAmount(state.config.step.amount)}):",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                    )
                    OutlinedTextField(
                        value = tempMinInput,
                        onValueChange = { tempMinInput = it.filter(Char::isDigit).take(15) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        label = { Text("Số tiền (VNĐ)") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    val parsed = tempMinInput.toLongOrNull() ?: 0L
                    Text(
                        "Xem trước: ${formatVndAmount(parsed)}",
                        fontSize = 13.sp,
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.Medium,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = tempMinInput.toLongOrNull() ?: state.config.minAmount.value
                        val step = state.config.step.amount
                        // Làm tròn chia hết cho step
                        val rounded = if (parsed % step != 0L) (parsed / step) * step else parsed
                        val finalMin = rounded.coerceAtLeast(step)
                        viewModel.setMinAmount(finalMin.toString())
                        viewModel.save()
                        showMinDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                ) {
                    Text("Áp dụng")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMinDialog = false }) {
                    Text("Hủy", color = Color(0xFF64748B))
                }
            },
        )
    }

    if (showMaxDialog) {
        AlertDialog(
            onDismissRequest = { showMaxDialog = false },
            title = { Text("Mức tối đa", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Nhập mức tiền tối đa mỗi ô (phải chia hết cho ${formatVndAmount(state.config.step.amount)}):",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                    )
                    OutlinedTextField(
                        value = tempMaxInput,
                        onValueChange = { tempMaxInput = it.filter(Char::isDigit).take(15) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        label = { Text("Số tiền (VNĐ)") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    val parsed = tempMaxInput.toLongOrNull() ?: 0L
                    Text(
                        "Xem trước: ${formatVndAmount(parsed)}",
                        fontSize = 13.sp,
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.Medium,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = tempMaxInput.toLongOrNull() ?: state.config.maxAmount.value
                        val step = state.config.step.amount
                        // Làm tròn chia hết cho step và đảm bảo >= min + (slots * step)
                        val minRequired = state.config.minAmount.value + (state.config.slotCount * step)
                        val rounded = if (parsed % step != 0L) (parsed / step) * step else parsed
                        val finalMax = rounded.coerceAtLeast(minRequired)
                        viewModel.setMaxAmount(finalMax.toString())
                        viewModel.save()
                        showMaxDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                ) {
                    Text("Áp dụng")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMaxDialog = false }) {
                    Text("Hủy", color = Color(0xFF64748B))
                }
            },
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8FAFC),
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 1.dp,
                        modifier = Modifier.size(38.dp),
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color(0xFF1E293B))
                        }
                    }
                    Text("Cài đặt vòng quay", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = CircleShape, color = Color.White, shadowElevation = 1.dp, modifier = Modifier.size(38.dp)) {
                        Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.padding(9.dp))
                    }
                    Surface(shape = CircleShape, color = Color(0xFFFEF3C7), modifier = Modifier.size(38.dp)) {
                        Text("🥔", fontSize = 20.sp, modifier = Modifier.padding(top = 4.dp, start = 8.dp))
                    }
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2563EB))
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
                            iconTint = Color(0xFF2563EB),
                            title = "Bật vòng quay tiết kiệm",
                            checked = state.config.enabled,
                            onCheckedChange = viewModel::setEnabled,
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1F5F9)))
                        SettingSwitchRow(
                            icon = Icons.Filled.Home,
                            iconTint = Color(0xFF2563EB),
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
                            iconTint = Color(0xFF2563EB),
                            title = "Giờ nhắc",
                            value = String.format("%02d:%02d", state.config.reminderHour, state.config.reminderMinute),
                            onClick = {
                                val nextHour = (state.config.reminderHour + 1) % 24
                                viewModel.setReminderHour(nextHour)
                            },
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1F5F9)))
                        SettingSwitchRow(
                            icon = Icons.Filled.Notifications,
                            iconTint = Color(0xFF2563EB),
                            title = "Nhắc lại khi chưa thực hiện",
                            checked = state.config.snoozeEnabled,
                            onCheckedChange = viewModel::setSnoozeEnabled,
                        )
                    }
                }

                // Card 3: Bước mệnh giá (Pills 5.000đ / 10.000đ) & Mức tối thiểu / tối đa
                item {
                    SettingsCard {
                        // Bước mệnh giá
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Filled.Savings, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(22.dp))
                                Text("Bước mệnh giá", fontSize = 14.5.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val is5k = state.config.step == SavingSpinStep.FIVE_THOUSAND
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (is5k) Color(0xFF2563EB) else Color(0xFFF1F5F9),
                                    modifier = Modifier
                                        .clickable { viewModel.setStep(SavingSpinStep.FIVE_THOUSAND) },
                                ) {
                                    Text(
                                        "5.000đ",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (is5k) Color.White else Color(0xFF475569),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    )
                                }
                                val is10k = state.config.step == SavingSpinStep.TEN_THOUSAND
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (is10k) Color(0xFF2563EB) else Color(0xFFF1F5F9),
                                    modifier = Modifier
                                        .clickable { viewModel.setStep(SavingSpinStep.TEN_THOUSAND) },
                                ) {
                                    Text(
                                        "10.000đ",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (is10k) Color.White else Color(0xFF475569),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1F5F9)))

                        SettingActionRow(
                            icon = Icons.Filled.AttachMoney,
                            iconTint = Color(0xFF2563EB),
                            title = "Mức tối thiểu",
                            value = formatVndAmount(state.config.minAmount.value),
                            onClick = {
                                tempMinInput = state.config.minAmount.value.toString()
                                showMinDialog = true
                            },
                        )

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1F5F9)))

                        SettingActionRow(
                            icon = Icons.Filled.AttachMoney,
                            iconTint = Color(0xFF2563EB),
                            title = "Mức tối đa",
                            value = formatVndAmount(state.config.maxAmount.value),
                            onClick = {
                                tempMaxInput = state.config.maxAmount.value.toString()
                                showMaxDialog = true
                            },
                        )
                    }
                }

                // Card 4: Số ô vòng quay (8), Tần suất (Mỗi ngày), Cho phép bỏ qua hôm nay
                item {
                    SettingsCard {
                        SettingActionRow(
                            icon = Icons.Filled.GridOn,
                            iconTint = Color(0xFF8B5CF6),
                            title = "Số ô vòng quay",
                            value = state.config.slotCount.toString(),
                            onClick = {
                                val nextCount = if (state.config.slotCount == 8) 10 else if (state.config.slotCount == 10) 12 else 8
                                viewModel.setSlotCount(nextCount)
                            },
                        )

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1F5F9)))

                        SettingActionRow(
                            icon = Icons.Filled.Repeat,
                            iconTint = Color(0xFF8B5CF6),
                            title = "Tần suất",
                            value = "Mỗi ngày",
                            onClick = {},
                        )

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1F5F9)))

                        SettingSwitchRow(
                            icon = Icons.Filled.SkipNext,
                            iconTint = Color(0xFF8B5CF6),
                            title = "Cho phép bỏ qua hôm nay",
                            checked = state.config.allowSkip,
                            onCheckedChange = viewModel::setAllowSkip,
                        )
                    }
                }

                // Section 5: Xem trước trên Trang chủ
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.Visibility, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                            Text("Xem trước trên Trang chủ", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2563EB))
                        }

                        SavingSpinHomeCard(
                            state = SavingSpinUiState(
                                config = state.config.copy(enabled = true, showOnHome = true),
                                session = SavingSpinSession(
                                    id = "preview",
                                    scheduleKey = "preview",
                                    status = SavingSpinStatus.READY,
                                    wheelValues = listOf(Money(10000), Money(15000), Money(20000), Money(25000), Money(30000), Money(35000), Money(40000), Money(50000)),
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
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEE2E2),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "⚠️ $errorMsg",
                                fontSize = 13.sp,
                                color = Color(0xFFB91C1C),
                                modifier = Modifier.padding(12.dp),
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                    ) {
                        Text(
                            when {
                                state.isSaving -> "Đang lưu..."
                                state.saved -> "Đã lưu thành công"
                                else -> "Lưu cài đặt"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            Text(title, fontSize = 14.5.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF2563EB),
                uncheckedTrackColor = Color(0xFFE2E8F0),
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            Text(title, fontSize = 14.5.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(value, fontSize = 14.sp, color = Color(0xFF64748B))
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
        }
    }
}
