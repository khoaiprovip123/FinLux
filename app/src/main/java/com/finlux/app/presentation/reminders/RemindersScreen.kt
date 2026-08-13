package com.finlux.app.presentation.reminders

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinluxPurple
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.GlassDialogSurface
import com.finlux.app.core.designsystem.GlassTopBar
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.model.ReminderRecurrence
import com.finlux.app.domain.model.Wallet
import com.finlux.app.presentation.home.toVnd
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun RemindersScreen(onBack: () -> Unit, viewModel: RemindersViewModel = hiltViewModel()) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val snackbar = remember { SnackbarHostState() }
    var editing by remember { mutableStateOf<Reminder?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val openNewReminder = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        editing = null
        showEditor = true
    }
    LaunchedEffect(state.message) { state.message?.let { snackbar.showSnackbar(it); viewModel.consumeMessage() } }
    Scaffold(
        topBar = {
            GlassTopBar(
                title = { Text("Nhắc nhở định kỳ") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Quay lại") } },
                actions = { IconButton(onClick = openNewReminder) { Icon(Icons.Default.Add, "Thêm nhắc nhở") } },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = Color.Transparent,
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.reminders.isEmpty()) item {
                GlassCard(Modifier.fillMaxWidth(), onClick = openNewReminder) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.NotificationsActive, null, tint = FinluxPurple)
                        Text("Chưa có nhắc nhở", style = MaterialTheme.typography.titleMedium)
                        Text("Chạm để tạo lịch thanh toán đầu tiên", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            items(state.reminders, key = { it.id }) { reminder ->
                val category = state.categories.firstOrNull { it.id == reminder.categoryId }
                val wallet = state.wallets.firstOrNull { it.id == reminder.walletId }
                GlassCard(Modifier.fillMaxWidth(), onClick = { editing = reminder; showEditor = true }) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EditCalendar, null, tint = FinluxPurple)
                            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text(reminder.title, style = MaterialTheme.typography.titleMedium)
                                Text("${category?.name ?: "Danh mục"} · ${wallet?.name ?: "Ví"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(reminder.enabled, { viewModel.toggle(reminder) })
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(reminder.amount.value.toVnd(), fontWeight = FontWeight.Bold)
                            Text("${reminder.recurrence.label} · ${reminder.nextTriggerDate.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            IconButton(onClick = { viewModel.delete(reminder) }) { Icon(Icons.Default.DeleteOutline, "Xóa") }
                        }
                    }
                }
            }
        }
    }
    if (showEditor) ReminderEditor(editing, state.categories, state.wallets, state.busy, { showEditor = false }) { viewModel.save(it) { showEditor = false } }
}

@Composable
private fun ReminderEditor(initial: Reminder?, categories: List<Category>, wallets: List<Wallet>, busy: Boolean, onDismiss: () -> Unit, onSave: (Reminder) -> Unit) {
    var title by remember(initial) { mutableStateOf(initial?.title.orEmpty()) }
    var amount by remember(initial) { mutableStateOf(initial?.amount?.value?.toString().orEmpty()) }
    var categoryId by remember(initial) { mutableStateOf(initial?.categoryId ?: categories.firstOrNull()?.id.orEmpty()) }
    var walletId by remember(initial) { mutableStateOf(initial?.walletId ?: wallets.firstOrNull()?.id.orEmpty()) }
    var recurrence by remember(initial) { mutableStateOf(initial?.recurrence ?: ReminderRecurrence.MONTHLY) }
    var selectedDate by remember(initial) { mutableStateOf(initial?.startDate ?: Instant.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    Dialog(onDismissRequest = onDismiss) {
        GlassDialogSurface {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (initial == null) "Thêm nhắc nhở" else "Sửa nhắc nhở", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(title, { title = it.take(48) }, Modifier.fillMaxWidth(), label = { Text("Tên hóa đơn / khoản chi") }, singleLine = true)
                OutlinedTextField(
                    amount,
                    { amount = it.filter(Char::isDigit).take(15) },
                    Modifier.fillMaxWidth(),
                    label = { Text("Số tiền dự kiến") },
                    supportingText = { Text((amount.toLongOrNull() ?: 0L).toVnd()) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Text("Danh mục", fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(categories) { item -> FilterChip(categoryId == item.id, { categoryId = item.id }, { Text(item.name) }) } }
                Text("Ví thanh toán", fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(wallets) { item -> FilterChip(walletId == item.id, { walletId = item.id }, { Text(item.name) }) } }
                Text("Chu kỳ", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { ReminderRecurrence.entries.forEach { item -> FilterChip(recurrence == item, { recurrence = item }, { Text(item.label) }) } }
                Button(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.EditCalendar, null); Text("Bắt đầu ${selectedDate.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}", Modifier.padding(start = 8.dp)) }
                Button(
                    onClick = { onSave(Reminder(initial?.id.orEmpty(), title.trim(), Money(amount.toLongOrNull() ?: 0), categoryId, walletId, recurrence, selectedDate, initial?.enabled ?: true, selectedDate)) },
                    modifier = Modifier.fillMaxWidth(), enabled = title.isNotBlank() && amount.toLongOrNull()?.let { it > 0 } == true && categoryId.isNotBlank() && walletId.isNotBlank() && !busy,
                ) { Text(if (busy) "Đang lưu…" else "Lưu nhắc nhở") }
            }
        }
    }
    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { pickerState.selectedDateMillis?.let { selectedDate = Instant.ofEpochMilli(it) }; showDatePicker = false }) { Text("Chọn") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Hủy") } },
        ) { DatePicker(pickerState) }
    }
}

private val ReminderRecurrence.label: String get() = when (this) {
    ReminderRecurrence.DAILY -> "Hàng ngày"
    ReminderRecurrence.WEEKLY -> "Hàng tuần"
    ReminderRecurrence.MONTHLY -> "Hàng tháng"
}
