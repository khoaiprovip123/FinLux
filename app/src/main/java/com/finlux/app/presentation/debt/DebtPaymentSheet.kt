package com.finlux.app.presentation.debt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.FinluxBlue
import com.finlux.app.core.designsystem.LiquidGlassSurface
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.walletIcon
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.Wallet
import com.finlux.app.presentation.home.toShortVnd
import com.finlux.app.presentation.home.toVnd
import java.time.Instant
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Header
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
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = debt.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = FinluxBlue,
                        ),
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng")
                }
            }

            Spacer(Modifier.height(14.dp))

            // Debt Summary Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Dư nợ còn lại",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = debt.remainingBalance.value.toVnd(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                    }
                    if (debt.minimumPayment.value > 0L) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Mức tối thiểu",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = debt.minimumPayment.value.toVnd(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFEF4444),
                                ),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Select Source Wallet
            Text(
                text = "Chọn ví nguồn thanh toán",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                wallets.forEach { wallet ->
                    val isSelected = wallet.id == selectedWalletId
                    val walletColor = colorFromHex(wallet.colorHex, FinluxBlue)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) walletColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, walletColor) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedWalletId = wallet.id },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(walletColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = walletIcon(wallet.type),
                                        contentDescription = null,
                                        tint = walletColor,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = wallet.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            Text(
                                text = "Số dư: ${wallet.balance.value.toVnd()}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) walletColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Payment Amount
            com.finlux.app.core.designsystem.component.FinluxAmountInputCard(
                label = "Tổng số tiền trả",
                amountDigits = amountText,
                onAmountChange = { amountText = it },
                showQuickChips = false,
                primaryColor = tokens.primary,
            )

            Spacer(Modifier.height(8.dp))

            // Quick Amount Chips
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

            Spacer(Modifier.height(14.dp))

            // Split: Principal vs Interest
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = currentPrincipal.toString(),
                    onValueChange = { /* Auto-computed */ },
                    label = { Text("Trừ tiền gốc") },
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                )

                OutlinedTextField(
                    value = interestText,
                    onValueChange = { interestText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Tiền lãi phát sinh") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                )
            }

            Spacer(Modifier.height(12.dp))

            // Note
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Ghi chú (tùy chọn)") },
                placeholder = { Text("vd: Thanh toán sao kê tháng 8") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            )

            if (validationError != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = validationError!!,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }

            Spacer(Modifier.height(24.dp))

            // Confirm Button
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
                    if (wallet.type != com.finlux.app.domain.model.WalletType.CARD && wallet.balance.value < currentAmount) {
                        validationError = "Số dư ví không đủ (${wallet.balance.value.toVnd()})"
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
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FinluxBlue),
            ) {
                Icon(imageVector = Icons.Default.Payments, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Xác nhận thanh toán (${currentAmount.toVnd()})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuickChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}
