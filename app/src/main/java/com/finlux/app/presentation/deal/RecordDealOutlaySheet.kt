package com.finlux.app.presentation.deal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.component.ErgonomicFormRow
import com.finlux.app.core.designsystem.component.ErgonomicInputRow
import com.finlux.app.core.designsystem.component.FinluxAmountInputCard
import com.finlux.app.core.designsystem.component.FinluxWalletPickerBottomSheet
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.designsystem.walletIcon
import com.finlux.app.domain.model.FinancialDeal
import com.finlux.app.domain.model.Wallet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDealOutlaySheet(
    deal: FinancialDeal,
    wallets: List<Wallet>,
    onDismiss: () -> Unit,
    onConfirm: (walletId: String, amount: Long, note: String) -> Unit,
    isSubmitting: Boolean = false,
) {
    val tokens = LocalFinluxTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedWalletId by remember {
        mutableStateOf(wallets.firstOrNull { it.isDefault }?.id ?: wallets.firstOrNull()?.id.orEmpty())
    }
    var showWalletPicker by remember { mutableStateOf(false) }

    var amountDigits by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

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
                        text = "Chi Xuất Thêm Vốn",
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
                            fontWeight = FontWeight.Medium,
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
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = tokens.onSurface,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            // 1. Chọn ví nguồn trích tiền (ErgonomicFormRow)
            ErgonomicFormRow(
                label = "VÍ NGUỒN TRÍCH TIỀN",
                primaryValue = selectedWallet?.name ?: "Chưa chọn ví",
                secondaryValue = selectedWallet?.let { "Số dư khả dụng: ${formatVndAmount(it.balance.value)}" },
                icon = walletIcon(selectedWallet?.type ?: com.finlux.app.domain.model.WalletType.CASH),
                iconBgColor = colorFromHex(selectedWallet?.colorHex.orEmpty(), tokens.primary).copy(alpha = 0.15f),
                iconTintColor = colorFromHex(selectedWallet?.colorHex.orEmpty(), tokens.primary),
                onClick = { showWalletPicker = true },
            )

            // 2. Nhập số tiền xuất vốn (FinluxAmountInputCard)
            FinluxAmountInputCard(
                amountDigits = amountDigits,
                onAmountChange = { amountDigits = it },
                label = "SỐ TIỀN XUẤT VỐN (VNĐ)",
                primaryColor = tokens.primary,
                quickAmounts = listOf(5_000_000L, 10_000_000L, 20_000_000L, 50_000_000L, 100_000_000L),
            )

            // 3. Ghi chú (ErgonomicInputRow)
            ErgonomicInputRow(
                label = "GHI CHÚ (TÙY CHỌN)",
                value = note,
                onValueChange = { note = it },
                placeholder = "Ví dụ: Đặt cọc lô hàng, Xuất vốn đợt 2...",
                icon = Icons.Default.Description,
                iconBgColor = tokens.primary.copy(alpha = 0.12f),
                iconTintColor = tokens.primary,
                onClear = { note = "" },
            )

            Spacer(Modifier.height(8.dp))

            // Action Button
            Button(
                onClick = {
                    if (amount > 0 && selectedWalletId.isNotBlank()) {
                        onConfirm(selectedWalletId, amount, note.trim())
                    }
                },
                enabled = amount > 0 && selectedWalletId.isNotBlank() && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.primary,
                    contentColor = Color.White,
                ),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = "Xác Nhận Xuất Vốn",
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
