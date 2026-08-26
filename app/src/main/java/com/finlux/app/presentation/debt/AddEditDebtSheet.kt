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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.finlux.app.core.designsystem.FinanceAccentHexes
import com.finlux.app.core.designsystem.FinluxBlue
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtType
import com.finlux.app.domain.model.Money
import com.finlux.app.presentation.debt.components.debtTypeIcon
import com.finlux.app.presentation.debt.components.debtTypeName
import com.finlux.app.presentation.home.toVnd
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDebtSheet(
    debt: DebtAccount?,
    onDismiss: () -> Unit,
    onSave: (DebtAccount) -> Unit,
    onDelete: ((DebtAccount) -> Unit)? = null,
    isSubmitting: Boolean = false,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEditing = debt != null

    var name by remember(debt) { mutableStateOf(debt?.name.orEmpty()) }
    var type by remember(debt) { mutableStateOf(debt?.type ?: DebtType.CREDIT_CARD) }
    var totalAmountText by remember(debt) { mutableStateOf(debt?.totalAmount?.value?.toString().orEmpty()) }
    var remainingBalanceText by remember(debt) { mutableStateOf(debt?.remainingBalance?.value?.toString().orEmpty()) }
    var aprText by remember(debt) { mutableStateOf(debt?.interestRateApr?.toString().orEmpty()) }
    var minimumPaymentText by remember(debt) { mutableStateOf(debt?.minimumPayment?.value?.toString().orEmpty()) }
    var dueDateText by remember(debt) { mutableStateOf((debt?.dueDate ?: 15).toString()) }
    var selectedColor by remember(debt) { mutableStateOf(debt?.colorHex ?: FinanceAccentHexes.first()) }
    var isReminderEnabled by remember(debt) { mutableStateOf(debt?.isReminderEnabled ?: true) }
    var reminderDaysBefore by remember(debt) { mutableIntStateOf(debt?.reminderDaysBefore ?: 3) }
    var validationError by remember { mutableStateOf<String?>(null) }

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
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (isEditing) "Chỉnh sửa khoản nợ" else "Thêm khoản nợ mới",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Debt Type Selector
            Text(
                text = "Loại khoản nợ",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DebtType.entries.forEach { itemType ->
                    val isSelected = type == itemType
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) FinluxBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, FinluxBlue) else null,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { type = itemType },
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = debtTypeIcon(itemType),
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = debtTypeName(itemType),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                                maxLines = 1,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên khoản nợ / Ngân hàng") },
                placeholder = { Text("vd: Thẻ tín dụng VCB, Vay mua xe...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            )

            Spacer(Modifier.height(12.dp))

            // Total Amount & Remaining Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = totalAmountText,
                    onValueChange = { totalAmountText = it.filter(Char::isDigit).trimStart('0') },
                    label = { Text("Hạn mức / Vay gốc") },
                    placeholder = { Text("0") },
                    supportingText = {
                        val amt = totalAmountText.toLongOrNull() ?: 0L
                        Text(amt.toVnd(), color = FinluxBlue, fontWeight = FontWeight.SemiBold)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                )

                OutlinedTextField(
                    value = remainingBalanceText,
                    onValueChange = { remainingBalanceText = it.filter(Char::isDigit).trimStart('0') },
                    label = { Text("Dư nợ hiện tại") },
                    placeholder = { Text("0") },
                    supportingText = {
                        val amt = remainingBalanceText.toLongOrNull() ?: 0L
                        Text(amt.toVnd(), color = FinluxBlue, fontWeight = FontWeight.SemiBold)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                )
            }

            Spacer(Modifier.height(12.dp))

            // APR & Minimum Payment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = aprText,
                    onValueChange = { aprText = it },
                    label = { Text("Lãi suất %/năm (APR)") },
                    placeholder = { Text("vd: 18.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                )

                OutlinedTextField(
                    value = minimumPaymentText,
                    onValueChange = { minimumPaymentText = it.filter(Char::isDigit).trimStart('0') },
                    label = { Text("Trả tối thiểu/tháng") },
                    placeholder = { Text("0") },
                    supportingText = {
                        val amt = minimumPaymentText.toLongOrNull() ?: 0L
                        Text(amt.toVnd(), color = FinluxBlue, fontWeight = FontWeight.SemiBold)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                )
            }

            Spacer(Modifier.height(12.dp))

            // Due Date
            OutlinedTextField(
                value = dueDateText,
                onValueChange = { dueDateText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Ngày đến hạn hàng tháng (1 - 31)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            )

            Spacer(Modifier.height(16.dp))

            // Color Palette
            Text(
                text = "Màu nhận diện",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FinanceAccentHexes.forEach { hex ->
                    val color = colorFromHex(hex)
                    val isColorSelected = selectedColor.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isColorSelected) 3.dp else 1.dp,
                                color = if (isColorSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                shape = CircleShape,
                            )
                            .clickable { selectedColor = hex },
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Due Date Reminder Section
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isReminderEnabled) FinluxBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(36.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = if (isReminderEnabled) FinluxBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Nhắc nhở thanh toán khi đến hạn",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = if (isReminderEnabled) "Gửi thông báo trước ngày đến hạn $reminderDaysBefore ngày" else "Đang tắt thông báo cho khoản nợ này",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Switch(
                            checked = isReminderEnabled,
                            onCheckedChange = { isReminderEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = FinluxBlue,
                            ),
                        )
                    }

                    if (isReminderEnabled) {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf(1 to "Trước 1 ngày", 2 to "Trước 2 ngày", 3 to "Trước 3 ngày", 5 to "Trước 5 ngày").forEach { (days, label) ->
                                val isSelected = reminderDaysBefore == days
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) FinluxBlue else MaterialTheme.colorScheme.surface,
                                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { reminderDaysBefore = days },
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        ),
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp),
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        val dueDayInt = dueDateText.toIntOrNull()?.coerceIn(1, 31) ?: 15
                        val remindDayInt = if (dueDayInt > reminderDaysBefore) dueDayInt - reminderDaysBefore else (30 + dueDayInt - reminderDaysBefore)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = FinluxBlue.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FinluxBlue.copy(alpha = 0.20f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = FinluxBlue,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = "Thông báo sẽ gửi vào lúc 09:00 sáng ngày $remindDayInt hàng tháng (trước hạn thanh toán ngày $dueDayInt $reminderDaysBefore ngày).",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }

            if (validationError != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = validationError!!,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }

            Spacer(Modifier.height(24.dp))

            // Action Buttons
            Button(
                onClick = {
                    val total = totalAmountText.toLongOrNull() ?: 0L
                    val remaining = remainingBalanceText.toLongOrNull() ?: total
                    val apr = aprText.toDoubleOrNull() ?: 0.0
                    val minPay = minimumPaymentText.toLongOrNull() ?: (remaining * 0.03).toLong()
                    val due = dueDateText.toIntOrNull()?.coerceIn(1, 31) ?: 15

                    if (name.isBlank()) {
                        validationError = "Vui lòng nhập tên khoản nợ"
                        return@Button
                    }
                    if (total <= 0L) {
                        validationError = "Hạn mức / khoản vay gốc phải lớn hơn 0"
                        return@Button
                    }

                    validationError = null
                    val newDebt = DebtAccount(
                        id = debt?.id.orEmpty(),
                        name = name.trim(),
                        type = type,
                        totalAmount = Money(total),
                        remainingBalance = Money(remaining),
                        interestRateApr = apr,
                        minimumPayment = Money(minPay),
                        dueDate = due,
                        colorHex = selectedColor,
                        isReminderEnabled = isReminderEnabled,
                        reminderDaysBefore = reminderDaysBefore,
                        isSettled = remaining <= 0L,
                        createdAt = debt?.createdAt ?: Instant.now(),
                        updatedAt = Instant.now(),
                    )
                    onSave(newDebt)
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FinluxBlue),
            ) {
                Text(
                    text = if (isEditing) "Lưu thay đổi" else "Thêm khoản nợ",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }

            if (isEditing && onDelete != null && debt != null) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { onDelete(debt) },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Xóa khoản nợ này")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
