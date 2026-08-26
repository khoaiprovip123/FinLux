package com.finlux.app.presentation.debt

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.component.ErgonomicFormRow
import com.finlux.app.core.designsystem.component.ErgonomicInputRow
import com.finlux.app.core.designsystem.component.FinluxAmountInputCard
import com.finlux.app.core.designsystem.component.PrincipalInterestSplitCard
import com.finlux.app.core.designsystem.component.SimpleWalletPickerSheet
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.designsystem.walletIcon
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.core.designsystem.component.formatVndAmount
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtPaymentSheet(
    debt: DebtAccount,
    wallets: List<Wallet>,
    onDismiss: () -> Unit,
    onConfirmPayment: (walletId: String, amount: Long, principalPaid: Long, interestPaid: Long, note: String) -> Unit,
    isSubmitting: Boolean = false,
) {
    val tokens = LocalFinluxTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedWalletId by remember {
        mutableStateOf(wallets.firstOrNull { it.isDefault }?.id ?: wallets.firstOrNull()?.id.orEmpty())
    }
    var showWalletPicker by remember { mutableStateOf(false) }

    val defaultAmount = if (debt.minimumPayment.value > 0L) debt.minimumPayment.value
    else (debt.remainingBalance.value * 0.05).roundToLong().coerceAtLeast(100_000L).coerceAtMost(debt.remainingBalance.value)

    var amountText by remember { mutableStateOf(defaultAmount.toString()) }
    var interestText by remember {
        val estMonthlyInterest = if (debt.interestRateApr > 0) {
            ((debt.remainingBalance.value.toDouble() * (debt.interestRateApr / 100.0)) / 12.0).roundToLong()
        } else 0L
        mutableStateOf(estMonthlyInterest.toString())
    }
    var note by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val currentAmount = amountText.toLongOrNull() ?: 0L
    val currentInterest = interestText.toLongOrNull() ?: 0L
    val currentPrincipal = (currentAmount - currentInterest).coerceAtLeast(0L)

    val selectedWallet = wallets.find { it.id == selectedWalletId }
    val isInsufficientBalance = selectedWallet != null &&
        selectedWallet.type != WalletType.CARD &&
        selectedWallet.balance.value < currentAmount

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
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // 1. Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Thanh toán khoản nợ",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                        ),
                        color = tokens.onSurface,
                    )
                    Text(
                        text = debt.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = tokens.primary,
                        ),
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .background(tokens.surfaceSoft, CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = tokens.onSurface,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            // 2. Compact Debt Summary Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = tokens.surfaceSoft,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Dư nợ hiện tại",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                            color = tokens.onSurfaceVariant,
                        )
                        Text(
                            text = formatVndAmount(debt.remainingBalance.value),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = tokens.onSurface,
                            ),
                        )
                    }
                    if (debt.minimumPayment.value > 0L) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Mức tối thiểu",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                color = tokens.onSurfaceVariant,
                            )
                            Text(
                                text = formatVndAmount(debt.minimumPayment.value),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444),
                                ),
                            )
                        }
                    }
                }
            }

            // 3. Hero Amount Input Card & Quick Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FinluxAmountInputCard(
                    label = "TỔNG SỐ TIỀN TRẢ",
                    amountDigits = amountText,
                    onAmountChange = { amountText = it },
                    showQuickChips = false,
                    primaryColor = tokens.primary,
                )

                // Quick Chips: Tối thiểu | 50% nợ | Tất toán hết
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (debt.minimumPayment.value > 0L) {
                        QuickChip(
                            label = "Tối thiểu",
                            onClick = { amountText = debt.minimumPayment.value.toString() },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    QuickChip(
                        label = "50% nợ",
                        onClick = { amountText = (debt.remainingBalance.value / 2).toString() },
                        modifier = Modifier.weight(1f),
                    )
                    QuickChip(
                        label = "Tất toán hết",
                        onClick = { amountText = debt.remainingBalance.value.toString() },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // 4. Note (Placed right beneath Amount Input)
            ErgonomicInputRow(
                label = "GHI CHÚ",
                value = note,
                onValueChange = { note = it },
                placeholder = "vd: Thanh toán sao kê thẻ tín dụng",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                iconBgColor = Color(0xFF6366F1).copy(alpha = 0.14f),
                iconTintColor = Color(0xFF6366F1),
                onClear = { note = "" },
            )

            // 5. Source Wallet Selector Row (Compact & Ergonomic)
            val walletIcon = selectedWallet?.type?.let { walletIcon(it) } ?: Icons.Default.AccountBalanceWallet
            val walletAccent = selectedWallet?.let { colorFromHex(it.colorHex, tokens.primary) } ?: tokens.primary

            ErgonomicFormRow(
                label = "VÍ NGUỒN THANH TOÁN",
                primaryValue = selectedWallet?.name ?: "Chưa chọn ví",
                secondaryValue = selectedWallet?.let { "Số dư khả dụng: ${formatVndAmount(it.balance.value)}" },
                icon = walletIcon,
                iconBgColor = walletAccent.copy(alpha = 0.14f),
                iconTintColor = walletAccent,
                onClick = { showWalletPicker = true },
            )

            // 6. Principal vs Interest Split Inputs (Ergonomic 2-Column Card)
            PrincipalInterestSplitCard(
                principalAmount = currentPrincipal,
                interestText = interestText,
                onInterestChange = { interestText = it },
            )

            // Warning for Insufficient Balance or Custom Errors
            val effectiveError = validationError ?: if (isInsufficientBalance) {
                "Số dư ví [${selectedWallet?.name}] không đủ để thanh toán"
            } else null

            if (effectiveError != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (tokens.isDark) Color(0xFF3B1E2B) else Color(0xFFFFE4E6),
                    border = BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFE11D48),
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = effectiveError,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = Color(0xFFE11D48),
                        )
                    }
                }
            }

            // 7. Sticky Bottom Action Button
            val canConfirm = !isSubmitting && selectedWallet != null && currentAmount > 0L && !isInsufficientBalance

            Button(
                onClick = {
                    val wallet = wallets.find { it.id == selectedWalletId }
                    if (wallet == null) {
                        validationError = "Vui lòng chọn ví thanh toán"
                        return@Button
                    }
                    if (currentAmount <= 0L) {
                        validationError = "Số tiền thanh toán phải lớn hơn 0"
                        return@Button
                    }
                    if (wallet.type != WalletType.CARD && wallet.balance.value < currentAmount) {
                        validationError = "Số dư ví không đủ (${formatVndAmount(wallet.balance.value)})"
                        return@Button
                    }

                    validationError = null
                    onConfirmPayment(
                        selectedWalletId,
                        currentAmount,
                        currentPrincipal,
                        currentInterest,
                        note,
                    )
                },
                enabled = canConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.primary,
                    disabledContainerColor = if (tokens.isDark) Color(0xFF2A2A3C) else Color(0xFFE2E8F0),
                ),
            ) {
                Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (currentAmount > 0L) "Xác nhận thanh toán • ${formatVndAmount(currentAmount)}" else "Xác nhận thanh toán",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (canConfirm) Color.White else (if (tokens.isDark) Color(0xFF64748B) else Color(0xFF94A3B8)),
                )
            }

            Spacer(Modifier.height(12.dp))
        }
    }

    if (showWalletPicker) {
        SimpleWalletPickerSheet(
            wallets = wallets,
            selectedWalletId = selectedWalletId,
            onSelectWallet = { wallet -> selectedWalletId = wallet.id },
            onDismiss = { showWalletPicker = false },
        )
    }
}

@Composable
private fun QuickChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = tokens.surfaceSoft,
        border = BorderStroke(1.dp, tokens.border),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = tokens.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}
