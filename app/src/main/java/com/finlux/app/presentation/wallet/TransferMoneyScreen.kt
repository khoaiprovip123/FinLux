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
import com.finlux.app.core.designsystem.component.form.FinluxWalletPickerBottomSheet
import com.finlux.app.core.designsystem.component.form.FinluxAmountInput
import com.finlux.app.core.designsystem.component.form.FinluxDateTimePicker
import com.finlux.app.core.designsystem.component.form.FinluxNoteInput
import com.finlux.app.core.designsystem.component.form.FinluxTransferWalletPair
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import java.time.Instant

/**
 * Full-screen dedicated Transfer Screen for transferring funds between wallets.
 * Adheres 100% to standard Finlux Form Controls (FinluxTransferWalletPair, FinluxAmountInput,
 * FinluxDateTimePicker, FinluxNoteInput) and Design System directives.
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

                // 2. Wallets Transfer Bento Box (Standard FinluxTransferWalletPair)
                FinluxTransferWalletPair(
                    sourceWallet = sourceWallet,
                    destWallet = destWallet,
                    onSelectSource = { showSourcePicker = true },
                    onSelectDest = { showDestPicker = true },
                    onSwap = {
                        if (sourceWalletId.isNotBlank() && destWalletId.isNotBlank()) {
                            val temp = sourceWalletId
                            sourceWalletId = destWalletId
                            destWalletId = temp
                        }
                    },
                    sourceSubtitle = "Khả dụng: ${formatVndAmount(sourceWallet?.balance?.value ?: 0L)}",
                    destSubtitle = "Hiện tại: ${formatVndAmount(destWallet?.balance?.value ?: 0L)}",
                    isSourceError = isInsufficientFunds,
                    errorMessage = if (isSameWallet) "⚠️ Ví gửi và ví nhận không được trùng nhau" else null,
                )

                // 3. Amount Input (Standard FinluxAmountInput with Quick Suggestions & "Tất cả" chip)
                FinluxAmountInput(
                    label = "SỐ TIỀN CHUYỂN",
                    amountText = transferAmount,
                    onAmountChange = { transferAmount = it },
                    leadingActionChip = if (sourceWallet != null && sourceWallet.balance.value > 0L) {
                        "Tất cả" to { transferAmount = sourceWallet.balance.value.toString() }
                    } else null,
                    warningMessage = if (isInsufficientFunds && sourceWallet != null) {
                        "Số dư ví [${sourceWallet.name}] không đủ để chuyển (Khả dụng: ${formatVndAmount(sourceWallet.balance.value)})"
                    } else null,
                )

                // 4. Standard Finlux Date & Time Picker
                FinluxDateTimePicker(
                    label = "THỜI GIAN GIAO DỊCH",
                    selectedDateTime = selectedDate,
                    onDateTimeChange = { selectedDate = it },
                )

                // 5. Standard Finlux Note Input
                FinluxNoteInput(
                    label = "GHI CHÚ CHUYỂN TIỀN",
                    placeholder = "Ghi chú chuyển tiền (tùy chọn)",
                    note = note,
                    onNoteChange = { note = it },
                )

                Spacer(Modifier.height(4.dp))

                // 6. Submit Button
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

    // Wallet Pickers Bottom Sheet
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
}
