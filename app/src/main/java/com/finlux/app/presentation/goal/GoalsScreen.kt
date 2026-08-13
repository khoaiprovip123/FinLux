package com.finlux.app.presentation.goal

import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinluxPurple
import com.finlux.app.core.designsystem.FinluxStyleBackdrop
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.GlassTopBar
import com.finlux.app.core.designsystem.WaterGlassCard
import com.finlux.app.domain.model.FinancialGoal
import com.finlux.app.presentation.home.toVnd
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private data class GoalCategory(val label: String, val icon: ImageVector)
private val goalCategories = listOf(
    GoalCategory("Ô tô", Icons.Default.DirectionsCar), GoalCategory("Nhà ở", Icons.Default.Home),
    GoalCategory("Du lịch", Icons.Default.Flight), GoalCategory("Học tập", Icons.Default.School),
    GoalCategory("Khác", Icons.Default.MoreHoriz),
)

@Composable
fun GoalsScreen(onBack: () -> Unit, viewModel: GoalsViewModel = hiltViewModel()) {
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    var showEditor by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize()) {
        FinluxStyleBackdrop(Modifier.fillMaxSize())
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { GlassTopBar(title = { Text("Mục tiêu tài chính", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.Close, "Đóng") } }, actions = { IconButton({ showEditor = true }) { Icon(Icons.Default.Add, "Thêm mục tiêu") } }) },
        ) { padding ->
            if (goals.isEmpty()) {
                Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.Savings, null, Modifier.size(58.dp), tint = FinluxPurple)
                    Text("Chưa có mục tiêu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Tạo kế hoạch tích lũy đầu tiên của anh.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button({ showEditor = true }, Modifier.padding(top = 18.dp)) { Icon(Icons.Default.Add, null); Text("Thêm mục tiêu") }
                }
            } else LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(goals, key = { it.id }) { GoalCard(it, onDelete = { viewModel.delete(it) }) }
            }
        }
        if (showEditor) GoalEditor(onDismiss = { showEditor = false }, viewModel = viewModel)
    }
}

@Composable
fun GoalEditor(onDismiss: () -> Unit, viewModel: GoalsViewModel = hiltViewModel()) {
    val state by viewModel.editor.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    val imagePicker = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> viewModel.setImage(uri?.toString()) }
    BackHandler(onBack = onDismiss)
    LaunchedEffect(state.saved) { if (state.saved) { viewModel.consumeSaved(); onDismiss() } }
    Box(Modifier.fillMaxSize()) {
        FinluxStyleBackdrop(Modifier.fillMaxSize())
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { GlassTopBar(title = { Text("Thêm mục tiêu", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onDismiss) { Icon(Icons.Default.Close, "Đóng") } }, actions = { TextButton(viewModel::save, enabled = !state.saving) { Text("Lưu") } }) },
        ) { padding ->
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).imePadding(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Box(Modifier.size(78.dp).background(FinluxPurple.copy(alpha = .18f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Savings, null, Modifier.size(40.dp), tint = FinluxPurple) } } }
                item { OutlinedTextField(state.name, viewModel::setName, Modifier.fillMaxWidth(), label = { Text("Tên mục tiêu") }, placeholder = { Text("VD: Mua ô tô") }, singleLine = true) }
                item { OutlinedTextField(state.targetInput, viewModel::setTarget, Modifier.fillMaxWidth(), label = { Text("Mục tiêu cần đạt") }, supportingText = { Text((state.targetInput.toLongOrNull() ?: 0L).toVnd()) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true) }
                item { WaterGlassCard(Modifier.fillMaxWidth(), tint = MaterialTheme.colorScheme.primary, onClick = { showDatePicker = true }) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CalendarMonth, null); Text("Hạn hoàn thành", Modifier.weight(1f).padding(horizontal = 12.dp)); Text(state.deadline.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), fontWeight = FontWeight.Bold) } } }
                item { Text("Danh mục", fontWeight = FontWeight.Bold) }
                item { LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) { items(goalCategories) { option -> GoalCategoryChip(option, state.category == option.label) { viewModel.setCategory(option.label) } } } }
                item { OutlinedTextField(state.monthlyInput, viewModel::setMonthly, Modifier.fillMaxWidth(), label = { Text("Số tiền tích lũy mỗi tháng") }, supportingText = { Text((state.monthlyInput.toLongOrNull() ?: 0L).toVnd()) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true) }
                item { Text("Ảnh mục tiêu", fontWeight = FontWeight.Bold) }
                item { WaterGlassCard(Modifier.fillMaxWidth().height(108.dp), tint = FinluxPurple, onClick = { imagePicker.launch("image/*") }) { Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(Icons.Default.Image, null, tint = FinluxPurple); Text(if (state.imageUri == null) "Chọn ảnh minh họa" else "Đã chọn ảnh mục tiêu", fontWeight = FontWeight.Medium); if (state.imageUri != null) Text("Chạm để đổi ảnh", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
                state.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
                item { Button(viewModel::save, Modifier.fillMaxWidth().height(54.dp), enabled = !state.saving) { Text(if (state.saving) "Đang lưu…" else "Lưu mục tiêu", fontWeight = FontWeight.Bold) } }
            }
        }
    }
    if (showDatePicker) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = state.deadline.toEpochMilli())
        DatePickerDialog({ showDatePicker = false }, confirmButton = { TextButton({ picker.selectedDateMillis?.let { viewModel.setDeadline(Instant.ofEpochMilli(it)) }; showDatePicker = false }) { Text("Chọn") } }, dismissButton = { TextButton({ showDatePicker = false }) { Text("Hủy") } }) { DatePicker(picker) }
    }
}

@Composable private fun GoalCategoryChip(option: GoalCategory, selected: Boolean, onClick: () -> Unit) {
    GlassCard(Modifier.size(width = 74.dp, height = 74.dp), onClick = onClick) { Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(option.icon, null, tint = if (selected) FinluxPurple else MaterialTheme.colorScheme.onSurfaceVariant); Text(option.label, style = MaterialTheme.typography.labelSmall, color = if (selected) FinluxPurple else MaterialTheme.colorScheme.onSurfaceVariant) } }
}

@Composable private fun GoalCard(goal: FinancialGoal, onDelete: () -> Unit) {
    WaterGlassCard(Modifier.fillMaxWidth(), tint = FinluxPurple) { Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(9.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Savings, null, tint = FinluxPurple); Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(goal.name, fontWeight = FontWeight.Bold); Text(goal.category, color = MaterialTheme.colorScheme.onSurfaceVariant) }; IconButton(onDelete) { Icon(Icons.Default.DeleteOutline, "Xóa") } }; val progress=(goal.savedAmount.value.toFloat()/goal.targetAmount.value.coerceAtLeast(1)).coerceIn(0f,1f); androidx.compose.material3.LinearProgressIndicator({ progress }, Modifier.fillMaxWidth()); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(goal.savedAmount.value.toVnd()); Text(goal.targetAmount.value.toVnd(), fontWeight = FontWeight.Bold) } } }
}
