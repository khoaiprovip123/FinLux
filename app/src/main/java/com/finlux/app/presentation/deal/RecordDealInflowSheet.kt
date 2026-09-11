package com.finlux.app.presentation.deal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Payments
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
import com.finlux.app.presentation.home.toVnd
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDealInflowSheet(
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
    val remainingCapital = deal.remainingCapital.value
    val principalPortion = if (amount <= remainingCapital) amount else remainingCapital
    val gainPortion = if (amount > remainingCapital) amount - remainingCapital else 0L

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
                        text = if (isLending) "Thu Hồi Nợ Gốc / Thu Lãi" else "Ghi Nhận Thu Tiền Về",
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
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = tokens.onSurface,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            // 1. Chọn ví nhận tiền (Standard FinluxWalletSelector)
            FinluxWalletSelector(
                label = "VÍ NHẬN TIỀN",
                selectedWallet = selectedWallet,
                subtitle = selectedWallet?.let { "Số dư hiện tại: ${formatVndAmount(it.balance.value)}" },
                onClick = { showWalletPicker = true },
            )

            // 2. Nhập số tiền thu về (Standard FinluxAmountInput)
            FinluxAmountInput(
                label = "SỐ TIỀN THỰC NHẬN VỀ",
                amountText = amountDigits,
                onAmountChange = { amountDigits = it },
                amountColor = Color(0xFF10B981),
            )

            // 3. LIVE DYNAMIC DECOMPOSITION CARD
            AnimatedVisibility(visible = amount > 0) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = tokens.surfaceSoft,
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Phân Rã Dòng Tiền Thông Minh",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = tokens.onSurfaceVariant,
                                ),
                            )
                            val headerTag = if (isLending) {
                                if (gainPortion > 0) "+${gainPortion.toVnd()} tiền lãi" else "Thu nợ gốc"
                            } else {
                                val estimatedNewRoi = if (deal.totalCapitalOutlay.value > 0) {
                                    val newRecovered = (deal.totalRecovered.value + principalPortion).coerceAtMost(deal.totalCapitalOutlay.value)
                                    val newGain = deal.netProfitLoss.value + gainPortion
                                    val totalReturn = newRecovered + newGain
                                    ((totalReturn - deal.totalCapitalOutlay.value).toDouble() / deal.totalCapitalOutlay.value) * 100.0
                                } else 0.0
                                String.format(java.util.Locale.US, "ROI mới: %+.1f%%", estimatedNewRoi)
                            }
                            Text(
                                text = headerTag,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLending) Color(0xFF8B5CF6) else Color(0xFF10B981),
                                ),
                            )
                        }

                        HorizontalDivider(color = tokens.border, thickness = 0.5.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = if (isLending) "• Thu hồi nợ gốc (không tính Thu nhập):" else "• Hoàn vốn gốc (không tính Thu nhập):",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = tokens.onSurface,
                                ),
                            )
                            Text(
                                text = principalPortion.toVnd(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2563EB),
                                ),
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = if (isLending) "• Tiền lãi phát sinh (Ghi nhận Thu nhập):" else "• Lợi nhuận ròng (Ghi nhận Thu nhập):",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = tokens.onSurface,
                                ),
                            )
                            Text(
                                text = if (gainPortion > 0) "+${gainPortion.toVnd()}" else "0 ₫",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (gainPortion > 0) (if (isLending) Color(0xFF8B5CF6) else Color(0xFF10B981)) else tokens.onSurfaceVariant,
                                ),
                            )
                        }
                    }
                }
            }

            // 4. Ghi chú (Standard FinluxNoteInput)
            FinluxNoteInput(
                label = "GHI CHÚ (TÙY CHỌN)",
                note = note,
                onNoteChange = { note = it },
                placeholder = if (isLending) "Ví dụ: Trả đợt 1, Tiền lãi tháng 8..." else "Ví dụ: Thu đợt 1, Tiền lời bán xe...",
                icon = Icons.Default.Description,
                iconBgColor = Color(0xFF10B981).copy(alpha = 0.12f),
                iconTintColor = Color(0xFF10B981),
            )

            // 5. Thời gian giao dịch (Standard FinluxDateTimePicker)
            FinluxDateTimePicker(
                label = if (isLending) "THỜI GIAN THU NỢ" else "THỜI GIAN THU TIỀN",
                selectedDateTime = selectedDate,
                onDateTimeChange = { selectedDate = it },
                iconBgColor = Color(0xFF10B981).copy(alpha = 0.14f),
                iconTintColor = Color(0xFF10B981),
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
                    containerColor = if (isLending) Color(0xFFD97706) else Color(0xFF10B981),
                    contentColor = Color.White,
                ),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (isLending) "Ghi Nhận Thu Nợ" else "Ghi Nhận Thu Tiền",
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
