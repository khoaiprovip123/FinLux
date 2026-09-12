package com.finlux.app.presentation.deal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.component.form.FinluxWalletPickerBottomSheet
import com.finlux.app.core.designsystem.component.form.FinluxAmountInput
import com.finlux.app.core.designsystem.component.form.FinluxDateTimePicker
import com.finlux.app.core.designsystem.component.form.FinluxNoteInput
import com.finlux.app.core.designsystem.component.form.FinluxWalletSelector
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.DealCategory
import com.finlux.app.domain.model.FinancialDeal
import com.finlux.app.domain.model.Wallet
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDealOutlaySheet(
    deal: FinancialDeal,
    wallets: List<Wallet>,
    onDismiss: () -> Unit,
    onConfirm: (walletId: String, amount: Long, date: Instant, note: String) -> Unit,
    isSubmitting: Boolean = false,
) {
    val tokens = LocalFinluxTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isLending = deal.category == DealCategory.LENDING

    var selectedWalletId by remember {
        mutableStateOf(wallets.firstOrNull { it.isDefault }?.id ?: wallets.firstOrNull()?.id.orEmpty())
    }
    var showWalletPicker by remember { mutableStateOf(false) }

    var amountDigits by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Instant.now()) }

    val cleanDigits = amountDigits.filter { it.isDigit() }.trimStart('0')
    val amount = cleanDigits.toLongOrNull() ?: 0L
    val selectedWallet = wallets.find { it.id == selectedWalletId }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = tokens.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = if (isLending) "Cho Vay Thêm" else "Ghi Nhận Xuất Vốn",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = tokens.onSurface,
                    )
                    Text(
                        text = deal.title,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = tokens.onSurfaceVariant,
                        ),
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(34.dp)
                        .background(tokens.surfaceSoft, CircleShape),
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = tokens.onSurface,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            // 1. Chọn ví nguồn trích tiền (Standard FinluxWalletSelector)
            FinluxWalletSelector(
                label = "VÍ NGUỒN TRÍCH TIỀN",
                selectedWallet = selectedWallet,
                subtitle = selectedWallet?.let { "Số dư khả dụng: ${formatVndAmount(it.balance.value)}" },
                onClick = { showWalletPicker = true },
            )

            // 2. Nhập số tiền xuất vốn (Standard FinluxAmountInput)
            FinluxAmountInput(
                label = if (isLending) "SỐ TIỀN CHO VAY THÊM" else "SỐ TIỀN XUẤT VỐN",
                amountText = amountDigits,
                onAmountChange = { amountDigits = it },
                amountColor = if (isLending) Color(0xFFD97706) else tokens.primary,
            )

            // 3. Ghi chú (Standard FinluxNoteInput)
            FinluxNoteInput(
                label = "GHI CHÚ (TÙY CHỌN)",
                note = note,
                onNoteChange = { note = it },
                placeholder = if (isLending) "Ví dụ: Cho mượn thêm đợt 2, Góp vốn bổ sung..." else "Ví dụ: Đặt cọc lô hàng, Xuất vốn đợt 2...",
                icon = Icons.Default.Description,
                iconBgColor = (if (isLending) Color(0xFFD97706) else tokens.primary).copy(alpha = 0.12f),
                iconTintColor = if (isLending) Color(0xFFD97706) else tokens.primary,
            )

            // 4. Thời gian giao dịch (Standard FinluxDateTimePicker)
            FinluxDateTimePicker(
                label = if (isLending) "THỜI GIAN CHO VAY" else "THỜI GIAN XUẤT VỐN",
                selectedDateTime = selectedDate,
                onDateTimeChange = { selectedDate = it },
                iconBgColor = (if (isLending) Color(0xFFD97706) else Color(0xFF6366F1)).copy(alpha = 0.14f),
                iconTintColor = if (isLending) Color(0xFFD97706) else Color(0xFF6366F1),
            )

            Spacer(Modifier.height(8.dp))

            // Action Button
            Button(
                onClick = {
                    if (amount > 0 && selectedWalletId.isNotBlank()) {
                        onConfirm(selectedWalletId, amount, selectedDate, note.trim())
                    }
                },
                enabled = amount > 0 && selectedWalletId.isNotBlank() && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLending) Color(0xFFD97706) else tokens.primary,
                    contentColor = Color.White,
                ),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (isLending) "Xác Nhận Cho Vay" else "Xác Nhận Xuất Vốn",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }
        }
    }

    // Modal chọn ví tài khoản dùng chung
    if (showWalletPicker) {
        FinluxWalletPickerBottomSheet(
            wallets = wallets,
            selectedWalletId = selectedWalletId,
            onSelectWallet = {
                selectedWalletId = it.id
                showWalletPicker = false
            },
            onDismiss = { showWalletPicker = false },
        )
    }
}
