package com.finlux.app.presentation.debt

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
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
import com.finlux.app.core.designsystem.component.form.ErgonomicCompactAmountCard
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtType
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.presentation.debt.components.debtTypeIcon
import com.finlux.app.presentation.debt.components.debtTypeName
import com.finlux.app.presentation.home.toVnd
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDebtSheet(
    debt: DebtAccount?,
    wallets: List<Wallet> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (DebtAccount) -> Unit,
    onDelete: ((DebtAccount) -> Unit)? = null,
    isSubmitting: Boolean = false,
) {
    val tokens = LocalFinluxTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEditing = debt != null

    var name by remember(debt) { mutableStateOf(debt?.name.orEmpty()) }
    var type by remember(debt) { mutableStateOf(debt?.type ?: DebtType.CREDIT_CARD) }
    var totalAmountText by remember(debt) { mutableStateOf(debt?.totalAmount?.value?.toString().orEmpty()) }
    var remainingBalanceText by remember(debt) { mutableStateOf(debt?.remainingBalance?.value?.toString().orEmpty()) }
    var aprText by remember(debt) { mutableStateOf(debt?.interestRateApr?.toString().orEmpty()) }
    var minimumPaymentText by remember(debt) { mutableStateOf(debt?.minimumPayment?.value?.toString().orEmpty()) }
    var dueDateText by remember(debt) { mutableStateOf(debt?.dueDate?.toString().orEmpty()) }
    var statementDateText by remember(debt) { mutableStateOf(debt?.statementDate?.toString().orEmpty()) }
    var gracePeriodDaysText by remember(debt) { mutableStateOf((debt?.gracePeriodDays ?: 45).toString()) }
    var selectedLinkedWalletId by remember(debt) { mutableStateOf(debt?.linkedWalletId) }
    var selectedColor by remember(debt) { mutableStateOf(debt?.colorHex ?: FinanceAccentHexes.first()) }
    var isReminderEnabled by remember(debt) { mutableStateOf(debt?.isReminderEnabled ?: true) }
    var reminderDaysBefore by remember(debt) { mutableIntStateOf(debt?.reminderDaysBefore ?: 3) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

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
                .padding(horizontal = 20.dp, vertical = 12.dp)
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
                    color = tokens.onSurface,
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
                color = tokens.onSurface,
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
                        color = if (isSelected) tokens.primary else tokens.surfaceSoft,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, tokens.primary) else null,
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
                                tint = if (isSelected) Color.White else tokens.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = debtTypeName(itemType),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else tokens.onSurfaceVariant,
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
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ErgonomicCompactAmountCard(
                    label = "Hạn mức / Vay gốc",
                    amountText = totalAmountText,
                    onAmountChange = { totalAmountText = it },
                    placeholder = "0",
                    amountColor = tokens.primary,
                    modifier = Modifier.weight(1f),
                )

                ErgonomicCompactAmountCard(
                    label = "Dư nợ hiện tại",
                    amountText = remainingBalanceText,
                    onAmountChange = { remainingBalanceText = it },
                    placeholder = "0",
                    amountColor = FinluxColors.ExpenseRed,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(12.dp))

            // APR & Minimum Payment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
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

                ErgonomicCompactAmountCard(
                    label = "Trả tối thiểu / tháng",
                    amountText = minimumPaymentText,
                    onAmountChange = { minimumPaymentText = it },
                    placeholder = "0",
                    amountColor = FinluxColors.WarningAmber,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(12.dp))

            // Due Date & Credit Card Specific Inputs
            if (type == DebtType.CREDIT_CARD) {
                // Statement Date & Due Date in a Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        value = statementDateText,
                        onValueChange = { statementDateText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Ngày sao kê (1 - 31)") },
                        placeholder = { Text("vd: 20") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                    )

                    OutlinedTextField(
                        value = dueDateText,
                        onValueChange = { dueDateText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Hạn thanh toán (1 - 31)") },
                        placeholder = { Text("vd: 5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = gracePeriodDaysText,
                    onValueChange = { gracePeriodDaysText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Thời gian miễn lãi (ngày)") },
                    placeholder = { Text("45") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                )

                // Linked Card Wallet Selector
                val cardWallets = wallets.filter { it.type == WalletType.CARD }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Ví thẻ tín dụng liên kết",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = tokens.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Khi thanh toán khoản nợ này, tiền sẽ được hoàn vào ví thẻ đã chọn để khôi phục hạn mức khả dụng (hoán đổi tài sản).",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = tokens.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val isNoneSelected = selectedLinkedWalletId == null
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isNoneSelected) tokens.primary.copy(alpha = 0.15f) else tokens.surfaceSoft,
                        border = BorderStroke(1.dp, if (isNoneSelected) tokens.primary else tokens.border),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedLinkedWalletId = null },
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Không liên kết",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isNoneSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.5.sp,
                                    color = if (isNoneSelected) tokens.primary else tokens.onSurface,
                                ),
                            )
                        }
                    }

                    cardWallets.forEach { cardWallet ->
                        val isSelected = selectedLinkedWalletId == cardWallet.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) tokens.primary.copy(alpha = 0.15f) else tokens.surfaceSoft,
                            border = BorderStroke(1.dp, if (isSelected) tokens.primary else tokens.border),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedLinkedWalletId = cardWallet.id },
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = cardWallet.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.5.sp,
                                        color = if (isSelected) tokens.primary else tokens.onSurface,
                                    ),
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = dueDateText,
                    onValueChange = { dueDateText = it.filter { ch -> ch.isDigit() } },
                    label = {
                        Text(
                            if (type == DebtType.PERSONAL_LOAN) "Hạn trả linh hoạt (tùy chọn 1 - 31)"
                            else "Ngày đến hạn hàng tháng (1 - 31)"
                        )
                    },
                    placeholder = {
                        Text(
                            if (type == DebtType.PERSONAL_LOAN) "Khoản nợ linh hoạt không cố định ngày"
                            else "Nhập ngày đến hạn hàng tháng (vd: 15)"
                        )
                    },
                    trailingIcon = if (dueDateText.isNotBlank()) {
                        {
                            IconButton(onClick = { dueDateText = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Xóa ngày đến hạn", modifier = Modifier.size(16.dp))
                            }
                        }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                )
            }

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
                color = tokens.surfaceSoft,
                border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border),
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
                                color = if (isReminderEnabled) tokens.primary.copy(alpha = 0.15f) else tokens.surface,
                                modifier = Modifier.size(36.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = if (isReminderEnabled) tokens.primary else tokens.onSurfaceVariant,
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
                                    color = tokens.onSurface,
                                )
                                Text(
                                    text = if (isReminderEnabled) "Gửi thông báo trước ngày đến hạn $reminderDaysBefore ngày" else "Đang tắt thông báo cho khoản nợ này",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = tokens.onSurfaceVariant,
                                )
                            }
                        }

                        Switch(
                            checked = isReminderEnabled,
                            onCheckedChange = { isReminderEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = tokens.primary,
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
                                    color = if (isSelected) tokens.primary else tokens.surface,
                                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, tokens.border),
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
                                        color = if (isSelected) Color.White else tokens.onSurface,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp),
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        val dueDayInt = dueDateText.toIntOrNull()?.takeIf { it in 1..31 }
                        if (dueDayInt != null) {
                            Spacer(Modifier.height(10.dp))
                            val remindDayInt = if (dueDayInt > reminderDaysBefore) {
                                dueDayInt - reminderDaysBefore
                            } else {
                                (30 + dueDayInt - reminderDaysBefore).coerceAtLeast(1)
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = tokens.primary.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, tokens.primary.copy(alpha = 0.20f)),
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
                                        tint = tokens.primary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text = "Thông báo sẽ gửi vào lúc 09:00 sáng ngày $remindDayInt hàng tháng (trước hạn thanh toán ngày $dueDayInt $reminderDaysBefore ngày).",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                        ),
                                        color = tokens.onSurface,
                                    )
                                }
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
                    val due = dueDateText.toIntOrNull()?.takeIf { it in 1..31 }

                    if (name.isBlank()) {
                        validationError = "Vui lòng nhập tên khoản nợ"
                        return@Button
                    }
                    if (total <= 0L) {
                        validationError = "Hạn mức / khoản vay gốc phải lớn hơn 0"
                        return@Button
                    }
                    if (type != DebtType.PERSONAL_LOAN && due == null) {
                        validationError = "Vui lòng nhập ngày đến hạn hàng tháng (1 - 31)"
                        return@Button
                    }

                    val stmtDate = if (type == DebtType.CREDIT_CARD) statementDateText.toIntOrNull()?.coerceIn(1, 31) else null
                    val grace = if (type == DebtType.CREDIT_CARD) gracePeriodDaysText.toIntOrNull() ?: 45 else 0
                    val linkedId = if (type == DebtType.CREDIT_CARD) selectedLinkedWalletId else null

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
                        statementDate = stmtDate,
                        gracePeriodDays = grace,
                        linkedWalletId = linkedId,
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
                colors = ButtonDefaults.buttonColors(containerColor = tokens.primary),
            ) {
                Text(
                    text = if (isEditing) "Lưu thay đổi" else "Thêm khoản nợ",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }

            if (isEditing && onDelete != null && debt != null) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = true },
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

    if (showDeleteConfirmDialog && debt != null && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Xóa khoản nợ",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = tokens.onSurface,
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn xóa khoản nợ \"${debt.name}\" không?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.onSurfaceVariant,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete(debt)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("Xóa", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Hủy", color = tokens.onSurfaceVariant)
                }
            },
            containerColor = tokens.surface,
        )
    }
}
