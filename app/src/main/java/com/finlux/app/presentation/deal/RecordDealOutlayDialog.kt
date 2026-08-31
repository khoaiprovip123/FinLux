package com.finlux.app.presentation.deal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.FinancialDeal
import com.finlux.app.domain.model.Wallet

@Composable
fun RecordDealOutlayDialog(
    deal: FinancialDeal,
    wallets: List<Wallet>,
    onDismiss: () -> Unit,
    onConfirm: (walletId: String, amount: Long, note: String) -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedWalletId by remember {
        mutableStateOf(wallets.firstOrNull { it.isDefault }?.id ?: wallets.firstOrNull()?.id.orEmpty())
    }

    val amount = amountText.toLongOrNull() ?: 0L

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = tokens.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
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
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = tokens.textPrimary,
                            ),
                        )
                        Text(
                            text = deal.title,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = tokens.textSecondary,
                            ),
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Đóng",
                            tint = tokens.textSecondary,
                        )
                    }
                }

                // Chọn ví nguồn
                Text(
                    text = "Ví nguồn trích tiền",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = tokens.textPrimary,
                    ),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    wallets.take(3).forEach { wallet ->
                        val isSelected = wallet.id == selectedWalletId
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) tokens.primary.copy(alpha = 0.15f)
                                    else tokens.surfaceSoft
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) tokens.primary else tokens.border,
                                    shape = RoundedCornerShape(12.dp),
                                )
                                .clickable { selectedWalletId = wallet.id }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = wallet.name,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) tokens.primary else tokens.textPrimary,
                                ),
                                maxLines = 1,
                            )
                        }
                    }
                }

                // Số tiền xuất vốn
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Số tiền xuất vốn (VNĐ) *") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = tokens.primary,
                        unfocusedBorderColor = tokens.border,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                // Ghi chú
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú (tùy chọn)") },
                    placeholder = { Text("Ví dụ: Đặt cọc lô hàng, Xuất vốn đợt 2...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = tokens.primary,
                        unfocusedBorderColor = tokens.border,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy", color = tokens.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (amount > 0 && selectedWalletId.isNotBlank()) {
                                onConfirm(selectedWalletId, amount, note.trim())
                            }
                        },
                        enabled = amount > 0 && selectedWalletId.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tokens.primary,
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Xác Nhận Xuất Vốn")
                    }
                }
            }
        }
    }
}
