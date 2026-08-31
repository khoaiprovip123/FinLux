package com.finlux.app.presentation.deal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.FinancialDeal
import com.finlux.app.domain.model.Money
import java.time.Instant

@Composable
fun CreateDealDialog(
    initialDeal: FinancialDeal? = null,
    onDismiss: () -> Unit,
    onConfirm: (FinancialDeal) -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    var title by remember { mutableStateOf(initialDeal?.title.orEmpty()) }
    var description by remember { mutableStateOf(initialDeal?.description.orEmpty()) }
    var targetAmountText by remember {
        mutableStateOf(if ((initialDeal?.targetAmount?.value ?: 0L) > 0) initialDeal?.targetAmount?.value.toString() else "")
    }

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
                    Text(
                        text = if (initialDeal == null) "Tạo Thương Vụ Mới" else "Chỉnh Sửa Thương Vụ",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = tokens.textPrimary,
                        ),
                    )
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

                // Tên thương vụ
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tên thương vụ / Dự án *") },
                    placeholder = { Text("Ví dụ: Lướt sóng iPhone, Góp vốn lô hàng...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = tokens.primary,
                        unfocusedBorderColor = tokens.border,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                // Mô tả
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả / Đối tác liên kết (tùy chọn)") },
                    placeholder = { Text("Ghi chú đối tác, thời hạn...") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = tokens.primary,
                        unfocusedBorderColor = tokens.border,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                // Mục tiêu kỳ vọng
                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { targetAmountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Mục tiêu thu về dự kiến (VNĐ)") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            val targetVal = targetAmountText.toLongOrNull() ?: 0L
                            val newDeal = initialDeal?.copy(
                                title = title.trim(),
                                description = description.trim(),
                                targetAmount = Money(targetVal),
                                updatedAt = Instant.now(),
                            ) ?: FinancialDeal(
                                title = title.trim(),
                                description = description.trim(),
                                targetAmount = Money(targetVal),
                            )
                            onConfirm(newDeal)
                        },
                        enabled = title.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tokens.primary,
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(if (initialDeal == null) "Tạo Deal" else "Lưu Thay Đổi")
                    }
                }
            }
        }
    }
}
