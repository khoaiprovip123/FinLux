package com.finlux.app.presentation.goal

import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinluxPurple
import com.finlux.app.core.designsystem.FinluxStyleBackdrop
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.GlassTopBar
import com.finlux.app.core.designsystem.WaterGlassCard
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.FinancialGoal
import com.finlux.app.domain.model.Wallet
import com.finlux.app.presentation.home.toVnd
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private data class GoalCategory(val label: String, val icon: ImageVector)
private val goalCategories = listOf(
    GoalCategory("Ô tô", Icons.Default.DirectionsCar),
    GoalCategory("Nhà ở", Icons.Default.Home),
    GoalCategory("Du lịch", Icons.Default.Flight),
    GoalCategory("Học tập", Icons.Default.School),
    GoalCategory("Khác", Icons.Default.MoreHoriz),
)

@Composable
fun GoalsScreen(onBack: () -> Unit, viewModel: GoalsViewModel = hiltViewModel()) {
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val wallets by viewModel.wallets.collectAsStateWithLifecycle()
    val transactionSheetState by viewModel.transactionSheet.collectAsStateWithLifecycle()
    var showEditor by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        FinluxStyleBackdrop(Modifier.fillMaxSize())
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassTopBar(
                    title = { Text("Mục tiêu tài chính", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onBack) {
                            Icon(Icons.Default.Close, "Đóng")
                        }
                    },
                    actions = {
                        IconButton({ showEditor = true }) {
                            Icon(Icons.Default.Add, "Thêm mục tiêu")
                        }
                    },
                )
            },
        ) { padding ->
            if (goals.isEmpty()) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Default.Savings, null, Modifier.size(58.dp), tint = FinluxPurple)
                    Text("Chưa có mục tiêu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Tạo kế hoạch tích lũy đầu tiên của anh.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button({ showEditor = true }, Modifier.padding(top = 18.dp)) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Thêm mục tiêu")
                    }
                }
            } else {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(goals, key = { it.id }) { goal ->
                        GoalCard(
                            goal = goal,
                            onDelete = { viewModel.delete(goal) },
                            onDeposit = { viewModel.openDeposit(goal) },
                            onWithdraw = { viewModel.openWithdraw(goal) },
                        )
                    }
                }
            }
        }

        if (showEditor) {
            GoalEditor(onDismiss = { showEditor = false }, viewModel = viewModel)
        }

        if (transactionSheetState.isOpen && transactionSheetState.goal != null) {
            GoalDepositWithdrawSheet(
                state = transactionSheetState,
                wallets = wallets,
                onDismiss = { viewModel.closeTransactionSheet() },
                onSelectWallet = { viewModel.setTransactionWallet(it) },
                onAmountChange = { viewModel.setTransactionAmount(it) },
                onNoteChange = { viewModel.setTransactionNote(it) },
                onSubmit = { viewModel.submitGoalTransaction() },
            )
        }
    }
}

@Composable
private fun GoalCard(
    goal: FinancialGoal,
    onDelete: () -> Unit,
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val progress = (goal.savedAmount.value.toFloat() / goal.targetAmount.value.coerceAtLeast(1L)).coerceIn(0f, 1f)
    val percentInt = (progress * 100).toInt()
    val isCompleted = goal.savedAmount.value >= goal.targetAmount.value && goal.targetAmount.value > 0L

    WaterGlassCard(
        modifier = Modifier.fillMaxWidth(),
        tint = if (isCompleted) Color(0xFF10B981) else FinluxPurple,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header: Icon + Info + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            (if (isCompleted) Color(0xFF10B981) else FinluxPurple).copy(alpha = 0.16f),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Savings,
                        contentDescription = null,
                        tint = if (isCompleted) Color(0xFF10B981) else FinluxPurple,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                ) {
                    Text(
                        text = goal.name,
                        style = FinluxTextStyles.SectionTitle.copy(fontSize = 16.sp),
                        fontWeight = FontWeight.Bold,
                        color = tokens.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${goal.category} • Hạn: ${goal.deadline.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                        style = FinluxTextStyles.Caption.copy(fontSize = 12.sp),
                        color = tokens.onSurfaceVariant,
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Xóa",
                        tint = tokens.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
            }

            // Progress Section
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Tiến độ: $percentInt%",
                        style = FinluxTextStyles.Caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                        color = if (isCompleted) Color(0xFF10B981) else FinluxPurple,
                    )
                    Text(
                        text = "${formatVndAmount(goal.savedAmount.value)} / ${formatVndAmount(goal.targetAmount.value)}",
                        style = FinluxTextStyles.Caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                        color = tokens.onSurface,
                    )
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = if (isCompleted) Color(0xFF10B981) else FinluxPurple,
                    trackColor = tokens.surfaceSoft,
                )
            }

            if (goal.monthlyContribution.value > 0L) {
                Text(
                    text = "Mục tiêu tích lũy: +${formatVndAmount(goal.monthlyContribution.value)}/tháng",
                    style = FinluxTextStyles.MicroLabel.copy(fontSize = 11.sp),
                    color = tokens.onSurfaceVariant,
                )
            }

            // Bottom Atomic Actions: NẠP TIỀN & RÚT TIỀN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = onDeposit,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FinluxPurple,
                        contentColor = Color.White,
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Nạp tiền", style = FinluxTextStyles.Caption.copy(fontWeight = FontWeight.Bold))
                }

                OutlinedButton(
                    onClick = onWithdraw,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = goal.savedAmount.value > 0L,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                ) {
                    Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Rút tiền", style = FinluxTextStyles.Caption.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalDepositWithdrawSheet(
    state: GoalTransactionSheetState,
    wallets: List<Wallet>,
    onDismiss: () -> Unit,
    onSelectWallet: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val goal = state.goal ?: return
    val isDeposit = state.mode == GoalTransactionMode.DEPOSIT
    val tokens = LocalFinluxTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = tokens.surface,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header Sheet
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                (if (isDeposit) FinluxPurple else Color(0xFFE11D48)).copy(alpha = 0.16f),
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (isDeposit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = if (isDeposit) FinluxPurple else Color(0xFFE11D48),
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Column {
                        Text(
                            text = if (isDeposit) "Nạp tiền vào mục tiêu" else "Rút tiền về ví",
                            style = FinluxTextStyles.SectionTitle.copy(fontSize = 18.sp),
                            fontWeight = FontWeight.Bold,
                            color = tokens.onSurface,
                        )
                        Text(
                            text = "${goal.name} (Đang có: ${formatVndAmount(goal.savedAmount.value)})",
                            style = FinluxTextStyles.Caption.copy(fontSize = 12.sp),
                            color = tokens.onSurfaceVariant,
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng", tint = tokens.onSurfaceVariant)
                }
            }

            // Wallet Selection
            Text(
                text = if (isDeposit) "Trích tiền từ ví" else "Chuyển tiền về ví",
                style = FinluxTextStyles.Caption.copy(fontWeight = FontWeight.Bold),
                color = tokens.onSurface,
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(wallets, key = { it.id }) { wallet ->
                    val isSelected = wallet.id == state.selectedWalletId
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) FinluxPurple.copy(alpha = 0.16f) else tokens.surfaceSoft,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) FinluxPurple else tokens.onSurface.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(14.dp),
                            )
                            .clickable { onSelectWallet(wallet.id) },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = if (isSelected) FinluxPurple else tokens.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                            Column {
                                Text(
                                    text = wallet.name,
                                    style = FinluxTextStyles.Caption.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) FinluxPurple else tokens.onSurface,
                                )
                                Text(
                                    text = formatVndAmount(wallet.balance.value),
                                    style = FinluxTextStyles.MicroLabel.copy(fontSize = 10.sp),
                                    color = tokens.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            // Amount Input
            OutlinedTextField(
                value = state.amountInput,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Số tiền thực hiện (đ)") },
                placeholder = { Text("0") },
                supportingText = {
                    val amountLong = state.amountInput.toLongOrNull() ?: 0L
                    Text(formatVndAmount(amountLong))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isDeposit) FinluxPurple else Color(0xFFE11D48),
                    focusedLabelColor = if (isDeposit) FinluxPurple else Color(0xFFE11D48),
                ),
            )

            // Quick Amount Suggestion Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                val suggestions = if (isDeposit) {
                    listOf(
                        500_000L to "500k",
                        1_000_000L to "1 triệu",
                        2_000_000L to "2 triệu",
                        5_000_000L to "5 triệu",
                    )
                } else {
                    listOf(
                        500_000L to "500k",
                        1_000_000L to "1 triệu",
                        goal.savedAmount.value to "Toàn bộ (${formatVndAmount(goal.savedAmount.value)})",
                    )
                }

                items(suggestions) { (amt, label) ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = tokens.surfaceSoft,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onAmountChange(amt.toString()) },
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = FinluxTextStyles.MicroLabel.copy(fontWeight = FontWeight.SemiBold),
                            color = tokens.onSurface,
                        )
                    }
                }
            }

            // Note Input
            OutlinedTextField(
                value = state.note,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Ghi chú (Tùy chọn)") },
                placeholder = { Text(if (isDeposit) "VD: Thưởng lương tháng này" else "VD: Rút tiền chi tiêu") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = FinluxTextStyles.Caption,
                )
            }

            // Submit Button
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = !state.isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDeposit) FinluxPurple else Color(0xFFE11D48),
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = if (state.isSubmitting) "Đang xử lý..." else if (isDeposit) "Xác nhận Nạp tiền" else "Xác nhận Rút tiền",
                    style = FinluxTextStyles.SectionTitle.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(8.dp))
        }
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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
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

@Composable
private fun GoalCategoryChip(option: GoalCategory, selected: Boolean, onClick: () -> Unit) {
    GlassCard(Modifier.size(width = 74.dp, height = 74.dp), onClick = onClick) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(option.icon, null, tint = if (selected) FinluxPurple else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(option.label, style = MaterialTheme.typography.labelSmall, color = if (selected) FinluxPurple else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
