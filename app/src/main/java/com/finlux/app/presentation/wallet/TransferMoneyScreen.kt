package com.finlux.app.presentation.wallet

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.component.FinluxDialog
import com.finlux.app.core.designsystem.component.FinluxWalletPickerBottomSheet
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private fun formatNumberWithDots(input: String): String {
    val digits = input.filter { it.isDigit() }
    if (digits.isEmpty()) return ""
    val number = digits.toLongOrNull() ?: 0L
    val formatter = DecimalFormat("#,###")
    return formatter.format(number).replace(',', '.')
}

/**
 * Full-screen dedicated Transfer Screen for transferring funds between wallets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferMoneyScreen(
    onDismiss: () -> Unit,
    initialSourceWalletId: String? = null,
    viewModel: WalletsViewModel = hiltViewModel(),
) {
    val tokens = LocalFinluxTokens.current
    val context = LocalContext.current
    val wallets by viewModel.wallets.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()

    var sourceWalletId by remember(wallets, initialSourceWalletId) {
        mutableStateOf(initialSourceWalletId ?: wallets.firstOrNull()?.id.orEmpty())
    }
    var destWalletId by remember(wallets) {
        val fallbackDest = wallets.firstOrNull { it.id != sourceWalletId }?.id.orEmpty()
        mutableStateOf(fallbackDest)
    }
    var transferAmount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Instant.now()) }

    var showSourcePicker by remember { mutableStateOf(false) }
    var showDestPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val sourceWallet = wallets.find { it.id == sourceWalletId }
    val destWallet = wallets.find { it.id == destWalletId }
    val parsedAmount = transferAmount.toLongOrNull() ?: 0L

    val isInsufficientFunds = sourceWallet != null &&
        sourceWallet.type != WalletType.CARD &&
        parsedAmount > sourceWallet.balance.value

    val isSameWallet = sourceWalletId.isNotBlank() && sourceWalletId == destWalletId
    val canSubmit = parsedAmount > 0L &&
        sourceWalletId.isNotBlank() &&
        destWalletId.isNotBlank() &&
        !isSameWallet &&
        !isInsufficientFunds &&
        !actionState.busy

    val hasUnsavedChanges = parsedAmount > 0L || note.isNotBlank()

    fun handleBack() {
        if (hasUnsavedChanges) {
            showDiscardDialog = true
        } else {
            onDismiss()
        }
    }

    BackHandler(onBack = ::handleBack)

    if (showDiscardDialog) {
        FinluxDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = "Hủy chuyển tiền?",
            message = "Thông tin chuyển tiền đang nhập sẽ không được lưu lại. Bạn có chắc muốn thoát?",
            confirmLabel = "Thoát",
            dismissLabel = "Tiếp tục nhập",
            onConfirm = {
                showDiscardDialog = false
                onDismiss()
            },
        )
    }

    LaunchedEffect(actionState.message) {
        actionState.message?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.consumeMessage()
        }
    }

    // Date smart label
    val localDate = selectedDate.atZone(ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now()
    val dayPrefix = when (localDate) {
        today -> "Hôm nay, "
        today.minusDays(1) -> "Hôm qua, "
        else -> ""
    }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm") }
    val formattedDate = dayPrefix + selectedDate.atZone(ZoneId.systemDefault()).format(dateFormatter)

    val formattedAmount = remember(transferAmount) {
        formatNumberWithDots(transferAmount)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = tokens.background,
    ) {
        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
            com.finlux.app.core.designsystem.FinluxStyleBackdrop(modifier = Modifier.fillMaxSize())
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // 1. Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = ::handleBack,
                        modifier = Modifier
                            .size(38.dp)
                            .background(tokens.surfaceSoft, CircleShape),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = tokens.onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Text(
                        text = "Chuyển tiền giữa các ví",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = tokens.onSurface,
                    )

                    // Top Check Button
                    Surface(
                        shape = CircleShape,
                        color = if (canSubmit) tokens.primary else tokens.surfaceSoft,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable(
                                enabled = canSubmit,
                                onClick = {
                                    viewModel.transfer(sourceWalletId, destWalletId, parsedAmount, note, selectedDate) {
                                        onDismiss()
                                    }
                                },
                            ),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Chuyển",
                                tint = if (canSubmit) tokens.onHero else tokens.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }

                // 2. Wallets Transfer Bento Box (From Wallet -> Swap -> To Wallet)
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
                    border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // Source Wallet Row
                        Text(
                            text = "TỪ VÍ NGUỒN",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = tokens.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(tokens.surfaceSoft)
                                .clickable { showSourcePicker = true }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFF3B82F6).copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color(0xFF3B82F6),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sourceWallet?.name ?: "Chọn ví chuyển đi",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = tokens.onSurface,
                                )
                                Text(
                                    text = "Khả dụng: ${formatVndAmount(sourceWallet?.balance?.value ?: 0L)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    color = if (isInsufficientFunds) Color(0xFFEF4444) else tokens.onSurfaceVariant,
                                )
                            }
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
                                    .background(tokens.border.copy(alpha = 0.4f)),
                            )
                            IconButton(
                                onClick = {
                                    if (sourceWalletId.isNotBlank() && destWalletId.isNotBlank()) {
                                        val temp = sourceWalletId
                                        sourceWalletId = destWalletId
                                        destWalletId = temp
                                    }
                                },
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(tokens.primary.copy(alpha = 0.12f)),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = "Đổi chiều",
                                    tint = tokens.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(tokens.border.copy(alpha = 0.4f)),
                            )
                        }

                        // Destination Wallet Row
                        Text(
                            text = "ĐẾN VÍ NHẬN",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = tokens.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(tokens.surfaceSoft)
                                .clickable { showDestPicker = true }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = destWallet?.name ?: "Chọn ví nhận tiền",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = tokens.onSurface,
                                )
                                Text(
                                    text = "Hiện tại: ${formatVndAmount(destWallet?.balance?.value ?: 0L)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    color = tokens.onSurfaceVariant,
                                )
                            }
                        }

                        if (isSameWallet) {
                            Text(
                                text = "⚠️ Ví gửi và ví nhận không được trùng nhau",
                                color = Color(0xFFEF4444),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                            )
                        }
                    }
                }

                // 3. Amount Input Card
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
                    border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "SỐ TIỀN CHUYỂN",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = tokens.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BasicTextField(
                                value = formattedAmount,
                                onValueChange = { newText ->
                                    val digitsOnly = newText.filter { it.isDigit() }
                                    if (digitsOnly.length <= 13) {
                                        transferAmount = digitsOnly
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isInsufficientFunds) Color(0xFFEF4444) else tokens.primary,
                                ),
                                cursorBrush = SolidColor(tokens.primary),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                decorationBox = { innerTextField ->
                                    if (formattedAmount.isEmpty()) {
                                        Text(
                                            text = "0",
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = tokens.onSurfaceVariant.copy(alpha = 0.35f),
                                            ),
                                        )
                                    }
                                    innerTextField()
                                },
                            )
                            Text(
                                text = "đ",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tokens.onSurfaceVariant,
                                ),
                            )
                        }

                        if (isInsufficientFunds && sourceWallet != null) {
                            Text(
                                text = "Số dư ví [${sourceWallet.name}] không đủ để chuyển (Khả dụng: ${formatVndAmount(sourceWallet.balance.value)})",
                                color = Color(0xFFEF4444),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                            )
                        }

                        // Quick Chips
                        val quickAmounts = listOf(50_000L, 100_000L, 200_000L, 500_000L, 1_000_000L, 2_000_000L)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp),
                        ) {
                            if (sourceWallet != null && sourceWallet.balance.value > 0L) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = tokens.primary.copy(alpha = 0.12f),
                                        modifier = Modifier.clickable {
                                            transferAmount = sourceWallet.balance.value.toString()
                                        },
                                    ) {
                                        Text(
                                            text = "Tất cả",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = tokens.primary,
                                                fontSize = 11.5.sp,
                                            ),
                                        )
                                    }
                                }
                            }
                            items(quickAmounts) { amt ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = tokens.surfaceSoft,
                                    modifier = Modifier.clickable {
                                        transferAmount = amt.toString()
                                    },
                                ) {
                                    Text(
                                        text = "+${formatNumberWithDots(amt.toString())}",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = tokens.onSurfaceVariant,
                                            fontSize = 11.5.sp,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Date & Note Cards
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
                    border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Date picker row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showDatePicker = true }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(tokens.primary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = tokens.primary, modifier = Modifier.size(18.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Thời gian giao dịch", style = MaterialTheme.typography.labelSmall, color = tokens.onSurfaceVariant)
                                Text(formattedDate, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = tokens.onSurface)
                            }
                        }

                        Box(Modifier.fillMaxWidth().height(1.dp).background(tokens.border.copy(alpha = 0.3f)))

                        // Note input row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(tokens.surfaceSoft, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.EditNote, contentDescription = null, tint = tokens.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            }
                            BasicTextField(
                                value = note,
                                onValueChange = { note = it },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = tokens.onSurface),
                                cursorBrush = SolidColor(tokens.primary),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                decorationBox = { innerTextField ->
                                    if (note.isEmpty()) {
                                        Text(
                                            text = "Ghi chú chuyển tiền (tùy chọn)",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = tokens.onSurfaceVariant.copy(alpha = 0.5f)),
                                        )
                                    }
                                    innerTextField()
                                },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // 5. Submit Button
                Button(
                    onClick = {
                        viewModel.transfer(sourceWalletId, destWalletId, parsedAmount, note, selectedDate) {
                            onDismiss()
                        }
                    },
                    enabled = canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.primary,
                        disabledContainerColor = tokens.primary.copy(alpha = 0.35f),
                    ),
                ) {
                    Text(
                        text = if (actionState.busy) "Đang thực hiện chuyển tiền..." else "Xác nhận chuyển tiền",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                }
            }
        }
    }

    // Wallet Pickers
    if (showSourcePicker) {
        FinluxWalletPickerBottomSheet(
            wallets = wallets,
            selectedWalletId = sourceWalletId,
            onSelectWallet = { w ->
                sourceWalletId = w.id
                showSourcePicker = false
            },
            onDismiss = { showSourcePicker = false },
        )
    }

    if (showDestPicker) {
        FinluxWalletPickerBottomSheet(
            wallets = wallets,
            selectedWalletId = destWalletId,
            onSelectWallet = { w ->
                destWalletId = w.id
                showDestPicker = false
            },
            onDismiss = { showDestPicker = false },
        )
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            colors = DatePickerDefaults.colors(
                containerColor = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
            ),
            shape = RoundedCornerShape(28.dp),
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            selectedDate = Instant.ofEpochMilli(millis)
                        }
                        showDatePicker = false
                    },
                ) {
                    Text("Chọn", color = tokens.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy", color = tokens.onSurfaceVariant)
                }
            },
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
                    titleContentColor = tokens.onSurface,
                    headlineContentColor = tokens.onSurface,
                    weekdayContentColor = tokens.onSurfaceVariant,
                    subheadContentColor = tokens.onSurfaceVariant,
                    yearContentColor = tokens.onSurface,
                    currentYearContentColor = tokens.primary,
                    selectedYearContentColor = tokens.onHero,
                    selectedYearContainerColor = tokens.primary,
                    dayContentColor = tokens.onSurface,
                    selectedDayContentColor = tokens.onHero,
                    selectedDayContainerColor = tokens.primary,
                    todayContentColor = tokens.primary,
                    todayDateBorderColor = tokens.primary,
                ),
            )
        }
    }
}
