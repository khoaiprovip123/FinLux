package com.finlux.app.presentation.deal

import androidx.compose.animation.AnimatedVisibility
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
import com.finlux.app.presentation.home.toVnd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDealInflowDialog(
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
    val remainingCapital = deal.remainingCapital.value
    val principalPortion = if (amount <= remainingCapital) amount else remainingCapital
    val gainPortion = if (amount > remainingCapital) amount - remainingCapital else 0L

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
                            text = "Thu Hồi Vốn & Lợi Nhuận",
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

                // Chọn ví nhận tiền
                Text(
                    text = "Ví nhận tiền",
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

                // Nhập số tiền thu về
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Số tiền thực nhận về (VNĐ) *") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = tokens.primary,
                        unfocusedBorderColor = tokens.border,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                // LIVE DYNAMIC DECOMPOSITION PREVIEW
                AnimatedVisibility(visible = amount > 0) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = tokens.surfaceSoft,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Phân rã dòng tiền thông minh:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = tokens.textSecondary,
                                ),
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "• Hoàn vốn gốc (không tính Thu nhập):",
                                    style = MaterialTheme.typography.bodySmall.copy(color = tokens.textPrimary),
                                )
                                Text(
                                    text = principalPortion.toVnd(),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = tokens.textPrimary,
                                    ),
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "• Lợi nhuận ròng (ghi vào Báo cáo):",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (gainPortion > 0) Color(0xFF10B981) else tokens.textSecondary,
                                        fontWeight = if (gainPortion > 0) FontWeight.Bold else FontWeight.Normal,
                                    ),
                                )
                                Text(
                                    text = if (gainPortion > 0) "+${gainPortion.toVnd()}" else "0 ₫",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (gainPortion > 0) Color(0xFF10B981) else tokens.textSecondary,
                                    ),
                                )
                            }
                        }
                    }
                }

                // Ghi chú
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú (tùy chọn)") },
                    placeholder = { Text("Ví dụ: Thu đợt 1, Tiền lời bán xe...") },
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
                        Text("Ghi Nhận Thu Tiền")
                    }
                }
            }
        }
    }
}
