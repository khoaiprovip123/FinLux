package com.finlux.app.presentation.reminders

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.NotificationPermissionHandler
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.component.ErgonomicCompactAmountCard
import com.finlux.app.core.designsystem.component.ErgonomicFormRow
import com.finlux.app.core.designsystem.component.ErgonomicInputRow
import com.finlux.app.core.designsystem.component.FinluxCategoryPickerBottomSheet
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.component.FinluxScreenHeader
import com.finlux.app.core.designsystem.component.FinluxSoftCard
import com.finlux.app.core.designsystem.component.FinluxWalletPickerBottomSheet
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.designsystem.walletIcon
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.model.ReminderRecurrence
import com.finlux.app.domain.model.Wallet
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun RemindersScreen(
    onBack: () -> Unit,
    viewModel: RemindersViewModel = hiltViewModel(),
) {
    NotificationPermissionHandler()
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val snackbar = remember { SnackbarHostState() }
    val tokens = LocalFinluxTokens.current
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

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            FinluxScreenHeader(
                title = "Nhắc nhở định kỳ",
                subtitle = "Lên lịch thanh toán & thông báo đúng giờ",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = tokens.onSurface,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = openNewReminder) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Thêm nhắc nhở",
                            tint = tokens.primary,
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = Color.Transparent,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.reminders.isEmpty()) {
                item {
                    FinluxEmptyState(
                        title = "Chưa có lịch nhắc nhở",
                        description = "Chạm vào nút bên dưới để tạo lịch thanh toán & nhắc nhở tự động.",
                        icon = Icons.Default.NotificationsActive,
                        actionLabel = "Tạo nhắc nhở mới",
                        onActionClick = openNewReminder,
                        modifier = Modifier.padding(top = 40.dp),
                    )
                }
            }

            items(state.reminders, key = { it.id }) { reminder ->
                val category = state.categories.firstOrNull { it.id == reminder.categoryId }
                val wallet = state.wallets.firstOrNull { it.id == reminder.walletId }
                val catIcon = category?.let { categoryIcon(it.icon) } ?: Icons.Default.Category
                val catColor = category?.let { colorFromHex(it.colorHex, tokens.primary) } ?: tokens.primary
                val zone = ZoneId.systemDefault()
                val triggerZdt = reminder.nextTriggerDate.atZone(zone)
                val triggerTimeText = triggerZdt.format(DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy"))

                FinluxSoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        editing = reminder
                        showEditor = true
                    },
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            // Category Icon Badge
                            Surface(
                                shape = RoundedCornerShape(tokens.radius.smallChip),
                                color = catColor.copy(alpha = if (tokens.isDark) 0.18f else 0.12f),
                                modifier = Modifier.size(42.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = catIcon,
                                        contentDescription = null,
                                        tint = catColor,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = reminder.title,
                                    style = FinluxTextStyles.CardTitle.copy(fontWeight = FontWeight.Bold),
                                    color = tokens.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "${category?.name ?: "Danh mục"} • ${wallet?.name ?: "Ví"}",
                                    style = FinluxTextStyles.Caption,
                                    color = tokens.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            Switch(
                                checked = reminder.enabled,
                                onCheckedChange = { viewModel.toggle(reminder) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = tokens.primary,
                                ),
                            )
                        }

                        // Amount & Schedule details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = formatVndAmount(reminder.amount.value),
                                style = FinluxTextStyles.CardTitle.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                ),
                                color = if (reminder.enabled) tokens.primary else tokens.onSurfaceVariant,
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = tokens.primary.copy(alpha = 0.08f),
                                    border = BorderStroke(0.5.dp, tokens.primary.copy(alpha = 0.2f)),
                                ) {
                                    Text(
                                        text = reminder.recurrence.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                        ),
                                        color = tokens.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.delete(reminder) },
                                    modifier = Modifier.size(28.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Xóa nhắc nhở",
                                        tint = FinluxColors.ExpenseRed,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }

                        // Next trigger time banner
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = tokens.surface,
                            border = BorderStroke(0.5.dp, tokens.border),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = if (reminder.enabled) "Kỳ tiếp theo: $triggerTimeText" else "Đang tạm dừng nhắc nhở",
                                    style = FinluxTextStyles.MicroLabel.copy(fontSize = 11.sp),
                                    color = if (reminder.enabled) tokens.onSurfaceVariant else Color(0xFF9CA3AF),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        ReminderEditorSheet(
            initial = editing,
            categories = state.categories.filter { it.type == CategoryType.EXPENSE },
            wallets = state.wallets,
            busy = state.busy,
            onDismiss = { showEditor = false },
            onSave = { reminder ->
                viewModel.save(reminder) { showEditor = false }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderEditorSheet(
    initial: Reminder?,
    categories: List<Category>,
    wallets: List<Wallet>,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val tokens = LocalFinluxTokens.current
    val context = LocalContext.current
    val zone = ZoneId.systemDefault()

    val initialZdt = remember(initial) {
        (initial?.startDate ?: Instant.now()).atZone(zone)
    }

    var title by remember(initial) { mutableStateOf(initial?.title.orEmpty()) }
    var amountDigits by remember(initial) { mutableStateOf(initial?.amount?.value?.toString().orEmpty()) }
    var categoryId by remember(initial) { mutableStateOf(initial?.categoryId ?: categories.firstOrNull()?.id.orEmpty()) }
    var walletId by remember(initial) { mutableStateOf(initial?.walletId ?: wallets.firstOrNull()?.id.orEmpty()) }
    var recurrence by remember(initial) { mutableStateOf(initial?.recurrence ?: ReminderRecurrence.MONTHLY) }

    var selectedDate by remember(initial) { mutableStateOf(initialZdt.toLocalDate()) }
    var selectedTime by remember(initial) { mutableStateOf(if (initial != null) initialZdt.toLocalTime() else LocalTime.of(9, 0)) }

    var showCategoryPicker by remember { mutableStateOf(false) }
    var showWalletPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val activeCategory = categories.firstOrNull { it.id == categoryId }
    val catIcon = activeCategory?.let { categoryIcon(it.icon) } ?: Icons.Default.Category
    val catAccent = activeCategory?.let { colorFromHex(it.colorHex, tokens.primary) } ?: tokens.primary

    val activeWallet = wallets.firstOrNull { it.id == walletId }
    val walletIcon = activeWallet?.type?.let { walletIcon(it) } ?: Icons.Default.AccountBalanceWallet
    val walletAccent = activeWallet?.let { colorFromHex(it.colorHex, tokens.primary) } ?: tokens.primary

    val isFormValid = title.isNotBlank() &&
            (amountDigits.toLongOrNull() ?: 0L) > 0L &&
            categoryId.isNotBlank() &&
            walletId.isNotBlank() &&
            !busy

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (tokens.isDark) Color(0xFF181824) else Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (initial == null) "Thêm nhắc nhở mới" else "Sửa lịch nhắc nhở",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    ),
                    color = tokens.onSurface,
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = tokens.onSurfaceVariant,
                    )
                }
            }

            // 1. Title Input Row
            ErgonomicInputRow(
                label = "TÊN NHẮC NHỞ / HÓA ĐƠN",
                value = title,
                onValueChange = { title = it.take(48) },
                placeholder = "vd: Tiền điện, Internet, Tiền nhà...",
                icon = Icons.Default.NotificationsActive,
                iconBgColor = tokens.primary.copy(alpha = 0.14f),
                iconTintColor = tokens.primary,
                onClear = { title = "" },
            )

            // 2. Compact Amount Card with .000 suggestion chips
            ErgonomicCompactAmountCard(
                label = "SỐ TIỀN DỰ KIẾN",
                amountText = amountDigits,
                onAmountChange = { amountDigits = it },
                placeholder = "0",
                amountColor = tokens.primary,
                showSuggestions = true,
            )

            // 3. Category Selector Row
            ErgonomicFormRow(
                label = "DANH MỤC CHI TIÊU",
                primaryValue = activeCategory?.name ?: "Chọn danh mục",
                icon = catIcon,
                iconBgColor = catAccent.copy(alpha = 0.14f),
                iconTintColor = catAccent,
                onClick = { showCategoryPicker = true },
            )

            // 4. Wallet Selector Row
            ErgonomicFormRow(
                label = "VÍ NGUỒN THANH TOÁN",
                primaryValue = activeWallet?.name ?: "Chọn ví thanh toán",
                secondaryValue = activeWallet?.let { "Số dư khả dụng: ${formatVndAmount(it.balance.value)}" },
                icon = walletIcon,
                iconBgColor = walletAccent.copy(alpha = 0.14f),
                iconTintColor = walletAccent,
                onClick = { showWalletPicker = true },
            )

            // 5. Recurrence Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "CHU KỲ LẶP LẠI",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    ),
                    color = Color(0xFF9CA3AF),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ReminderRecurrence.entries.forEach { item ->
                        val isSelected = recurrence == item
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) tokens.primary else tokens.surfaceSoft,
                            border = BorderStroke(1.dp, if (isSelected) tokens.primary else tokens.border),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { recurrence = item },
                        ) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                ),
                                color = if (isSelected) (if (tokens.isDark) Color(0xFF002B3D) else Color.White) else tokens.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp),
                            )
                        }
                    }
                }
            }

            // 6. Date & Time Selection (2 Columns)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "THỜI GIAN THÔNG BÁO CHÍNH XÁC",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    ),
                    color = Color(0xFF9CA3AF),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Date Picker Box
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = tokens.surfaceSoft,
                        border = BorderStroke(1.dp, tokens.border),
                        modifier = Modifier
                            .weight(1.1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showDatePicker = true },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = tokens.primary,
                                modifier = Modifier.size(20.dp),
                            )
                            Column {
                                Text(
                                    text = "NGÀY BẮT ĐẦU",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = Color(0xFF9CA3AF),
                                )
                                Text(
                                    text = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                    ),
                                    color = tokens.onSurface,
                                )
                            }
                        }
                    }

                    // Time Picker Box (Triggers Android Native TimePickerDialog)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = tokens.surfaceSoft,
                        border = BorderStroke(1.dp, tokens.border),
                        modifier = Modifier
                            .weight(0.9f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                val timePickerDialog = android.app.TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        selectedTime = LocalTime.of(hourOfDay, minute)
                                    },
                                    selectedTime.hour,
                                    selectedTime.minute,
                                    true, // 24-hour format
                                )
                                timePickerDialog.show()
                            },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp),
                            )
                            Column {
                                Text(
                                    text = "GIỜ NHẮC",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = Color(0xFF9CA3AF),
                                )
                                Text(
                                    text = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                    ),
                                    color = tokens.onSurface,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Sticky Save Button
            Button(
                onClick = {
                    val amountLong = amountDigits.toLongOrNull() ?: 0L
                    val combinedStartInstant = selectedDate.atTime(selectedTime).atZone(zone).toInstant()

                    // Compute next trigger instant ensuring it is in the future
                    val triggerInstant = if (combinedStartInstant.isBefore(Instant.now())) {
                        when (recurrence) {
                            ReminderRecurrence.DAILY -> {
                                var dt = selectedDate.atTime(selectedTime)
                                while (dt.atZone(zone).toInstant().isBefore(Instant.now())) {
                                    dt = dt.plusDays(1)
                                }
                                dt.atZone(zone).toInstant()
                            }
                            ReminderRecurrence.WEEKLY -> {
                                var dt = selectedDate.atTime(selectedTime)
                                while (dt.atZone(zone).toInstant().isBefore(Instant.now())) {
                                    dt = dt.plusWeeks(1)
                                }
                                dt.atZone(zone).toInstant()
                            }
                            ReminderRecurrence.MONTHLY -> {
                                var dt = selectedDate.atTime(selectedTime)
                                while (dt.atZone(zone).toInstant().isBefore(Instant.now())) {
                                    dt = dt.plusMonths(1)
                                }
                                dt.atZone(zone).toInstant()
                            }
                        }
                    } else {
                        combinedStartInstant
                    }

                    val reminder = Reminder(
                        id = initial?.id.orEmpty(),
                        title = title.trim(),
                        amount = Money(amountLong),
                        categoryId = categoryId,
                        walletId = walletId,
                        recurrence = recurrence,
                        startDate = combinedStartInstant,
                        enabled = initial?.enabled ?: true,
                        nextTriggerDate = triggerInstant,
                    )
                    onSave(reminder)
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.primary,
                    disabledContainerColor = if (tokens.isDark) Color(0xFF2A2A3C) else Color(0xFFE2E8F0),
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.EditCalendar,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (busy) "Đang lưu..." else if (initial == null) "Tạo nhắc nhở" else "Cập nhật nhắc nhở",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isFormValid) (if (tokens.isDark) Color(0xFF002B3D) else Color.White) else Color(0xFF94A3B8),
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // Category Picker Bottom Sheet
    if (showCategoryPicker) {
        FinluxCategoryPickerBottomSheet(
            categories = categories,
            selectedCategoryId = categoryId,
            onSelectCategory = { cat ->
                categoryId = cat.id
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false },
        )
    }

    // Wallet Picker Bottom Sheet
    if (showWalletPicker) {
        FinluxWalletPickerBottomSheet(
            wallets = wallets,
            selectedWalletId = walletId,
            onSelectWallet = { w ->
                walletId = w.id
                showWalletPicker = false
            },
            onDismiss = { showWalletPicker = false },
        )
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                        }
                        showDatePicker = false
                    },
                ) {
                    Text("Chọn", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy")
                }
            },
        ) {
            DatePicker(pickerState)
        }
    }
}

private val ReminderRecurrence.label: String
    get() = when (this) {
        ReminderRecurrence.DAILY -> "Hàng ngày"
        ReminderRecurrence.WEEKLY -> "Hàng tuần"
        ReminderRecurrence.MONTHLY -> "Hàng tháng"
    }
