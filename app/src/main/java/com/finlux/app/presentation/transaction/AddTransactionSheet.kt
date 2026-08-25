package com.finlux.app.presentation.transaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinanceAccentHexes
import com.finlux.app.core.designsystem.FinanceCategoryIcons
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.component.FinluxDialog
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.designsystem.walletIcon
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.WalletType
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Format raw digit string into Vietnamese localized display (e.g. 728000 -> 728.000)
 */
private fun formatNumberWithDots(rawInput: String): String {
    val digits = rawInput.filter { it.isDigit() }
    if (digits.isEmpty()) return ""
    val number = digits.toLongOrNull() ?: 0L
    val formatter = DecimalFormat("#,###")
    return formatter.format(number).replace(',', '.')
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    onDismiss: () -> Unit,
    initialType: TransactionType? = null,
    initialReceiptUri: String? = null,
    initialTransaction: FinanceTransaction? = null,
    viewModel: AddTransactionViewModel = hiltViewModel(),
) {
    val tokens = LocalFinluxTokens.current
    val context = LocalContext.current
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showWalletPicker by remember { mutableStateOf(false) }

    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    LaunchedEffect(initialTransaction, initialType) {
        if (initialTransaction != null) {
            viewModel.setEditingTransaction(initialTransaction)
        } else {
            viewModel.resetForNewTransaction(initialType)
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetForNewTransaction()
        }
    }
    LaunchedEffect(initialReceiptUri) {
        if (initialReceiptUri != null) viewModel.setReceipt(initialReceiptUri)
    }
    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.consumeSaved()
            onDismiss()
        }
    }

    val isExpense = state.type == TransactionType.EXPENSE
    val amountColor = if (isExpense) Color(0xFFDC2626) else Color(0xFF16A34A)
    val activeCategory = state.categories.firstOrNull { it.id == state.categoryId }
    val activeWallet = state.wallets.firstOrNull { it.id == state.walletId }
    val enteredAmountValue = state.amountInput.toLongOrNull() ?: 0L
    val isCreditCard = activeWallet?.type == WalletType.CARD

    val isInsufficientBalance = isExpense && !isCreditCard && activeWallet != null && (
        (state.editingTransaction == null && (activeWallet.balance.value <= 0L || (enteredAmountValue > 0L && enteredAmountValue > activeWallet.balance.value))) ||
        (state.editingTransaction != null && run {
            val original = state.editingTransaction
            val available = if (original.walletId == activeWallet.id) {
                val refund = if (original.type == TransactionType.EXPENSE) original.amount.value else -original.amount.value
                activeWallet.balance.value + refund
            } else {
                activeWallet.balance.value
            }
            available <= 0L || (enteredAmountValue > 0L && enteredAmountValue > available)
        })
    )

    val balanceErrorMessage = if (isInsufficientBalance && activeWallet != null) {
        if (activeWallet.balance.value <= 0L && state.editingTransaction == null) {
            "Ví [${activeWallet.name}] đã hết số dư (${formatVndAmount(activeWallet.balance.value)})"
        } else {
            "Số dư ví [${activeWallet.name}] không đủ để chi tiêu (Khả dụng: ${formatVndAmount(activeWallet.balance.value)})"
        }
    } else null

    // Date formatting with "Hôm nay" / "Hôm qua" smart labels
    val localDate = state.date.atZone(ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now()
    val dayPrefix = when (localDate) {
        today -> "Hôm nay, "
        today.minusDays(1) -> "Hôm qua, "
        else -> ""
    }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm") }
    val formattedDate = dayPrefix + state.date.atZone(ZoneId.systemDefault()).format(dateFormatter)

    // Formatted amount display
    val formattedAmount = remember(state.amountInput) {
        formatNumberWithDots(state.amountInput)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = tokens.surface,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // 1. Header Bar: Back Button + Title + Save Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(38.dp)
                        .background(tokens.surfaceSoft, CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = tokens.onSurface,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Text(
                    text = if (state.editingTransaction != null) "Sửa giao dịch" else if (isExpense) "Thêm chi" else "Thêm thu",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = tokens.onSurface,
                )

                // Save Button (Active / Disabled based on validation)
                val canSave = !state.isSaving && !isInsufficientBalance && enteredAmountValue > 0L
                Surface(
                    shape = CircleShape,
                    color = if (canSave) Color(0xFF3B5DF8) else (if (tokens.isDark) Color(0xFF2A2A3C) else Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            enabled = canSave,
                            onClick = viewModel::save,
                        ),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Lưu",
                            tint = if (canSave) Color.White else (if (tokens.isDark) Color(0xFF64748B) else Color(0xFF94A3B8)),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            // 2. Segmented Transaction Type Tabs (Clean 2-Tab Switch)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TransactionTypePill(
                    label = "Chi tiêu",
                    isSelected = isExpense,
                    activeBg = if (tokens.isDark) Color(0xFF3B1E2B) else Color(0xFFFFE4E6),
                    activeText = Color(0xFFE11D48),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setType(TransactionType.EXPENSE) },
                )
                TransactionTypePill(
                    label = "Thu nhập",
                    isSelected = !isExpense,
                    activeBg = if (tokens.isDark) Color(0xFF1E3A2B) else Color(0xFFDCFCE7),
                    activeText = Color(0xFF16A34A),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setType(TransactionType.INCOME) },
                )
            }

            // 3. Amount Display & Quick Chips Box (Pixel-Perfect Typography & Alignment)
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = if (tokens.isDark) Color(0xFF1E1E2D) else Color(0xFFF9FAFB),
                border = BorderStroke(1.dp, tokens.onSurface.copy(alpha = 0.06f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Số tiền",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = Color(0xFF6B7280),
                    )

                    // Formatted Amount Row (Grouped Together with ₫ Symbol)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            BasicTextField(
                                value = TextFieldValue(
                                    text = formattedAmount,
                                    selection = TextRange(formattedAmount.length),
                                ),
                                onValueChange = { tfv ->
                                    val digitsOnly = tfv.text.filter { it.isDigit() }
                                    if (digitsOnly.length <= 12) {
                                        viewModel.setAmount(digitsOnly)
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = amountColor,
                                    letterSpacing = (-0.5).sp,
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                cursorBrush = SolidColor(tokens.primary),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    if (formattedAmount.isEmpty()) {
                                        Text(
                                            text = "0",
                                            style = TextStyle(
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF9CA3AF),
                                            ),
                                        )
                                    }
                                    innerTextField()
                                },
                            )

                            Text(
                                text = " ₫",
                                style = TextStyle(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = amountColor,
                                ),
                                modifier = Modifier.padding(start = 2.dp),
                            )
                        }

                        // Clear Textbox Button [x]
                        if (state.amountInput.isNotEmpty() && state.amountInput != "0") {
                            Surface(
                                shape = CircleShape,
                                color = tokens.surfaceSoft,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true),
                                        onClick = { viewModel.setAmount("") },
                                    ),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Xóa số tiền",
                                        tint = tokens.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }

                    // Quick Amount Chips (+10k, +50k, +100k, +500k)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(10_000L to "+10k", 50_000L to "+50k", 100_000L to "+100k", 500_000L to "+500k").forEach { (amount, label) ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = tokens.surface,
                                border = BorderStroke(1.dp, tokens.onSurface.copy(alpha = 0.08f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        val currentVal = state.amountInput.toLongOrNull() ?: 0L
                                        viewModel.setAmount((currentVal + amount).toString())
                                    },
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = tokens.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 7.dp),
                                )
                            }
                        }
                    }
                }
            }

            // Warning Banner for Insufficient Wallet Balance
            if (balanceErrorMessage != null) {
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
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = balanceErrorMessage,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = Color(0xFFE11D48),
                        )
                    }
                }
            }

            // 4. Ergonomic Form Rows (Clean 2-line Label/Value Layout)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Hàng 1: Danh mục
                val categoryAccent = activeCategory?.let { colorFromHex(it.colorHex) } ?: Color(0xFFF43F5E)
                ErgonomicFormRow(
                    label = "DANH MỤC",
                    primaryValue = activeCategory?.name ?: "Chưa chọn danh mục",
                    secondaryValue = if (isExpense) "Khoản chi tiêu" else "Khoản thu nhập",
                    icon = activeCategory?.let { categoryIcon(it.icon) } ?: Icons.Default.Info,
                    iconBgColor = categoryAccent.copy(alpha = 0.14f),
                    iconTintColor = categoryAccent,
                    onClick = { showCategoryPicker = true },
                )

                // Hàng 2: Ví thanh toán / Tài khoản
                val walletIcon = activeWallet?.type?.let { walletIcon(it) } ?: Icons.Default.AccountBalanceWallet
                ErgonomicFormRow(
                    label = if (isExpense) "VÍ THANH TOÁN" else "VÍ NHẬN TIỀN",
                    primaryValue = activeWallet?.name ?: "Chưa chọn ví",
                    secondaryValue = activeWallet?.balance?.let { "Số dư: ${formatVndAmount(it.value)}" },
                    icon = walletIcon,
                    iconBgColor = Color(0xFF3B82F6).copy(alpha = 0.14f),
                    iconTintColor = Color(0xFF3B82F6),
                    onClick = { showWalletPicker = true },
                )

                // Hàng 3: Thời gian giao dịch
                ErgonomicFormRow(
                    label = "THỜI GIAN GIAO DỊCH",
                    primaryValue = formattedDate,
                    secondaryValue = null,
                    icon = Icons.Default.CalendarMonth,
                    iconBgColor = Color(0xFF6366F1).copy(alpha = 0.14f),
                    iconTintColor = Color(0xFF6366F1),
                    onClick = { showDatePicker = true },
                )

                // Hàng 4: Ghi chú
                ErgonomicInputRow(
                    label = "GHI CHÚ GIAO DỊCH",
                    value = state.note,
                    onValueChange = viewModel::setNote,
                    placeholder = if (isExpense) "Nhập ghi chú chi tiêu..." else "Nhập nguồn tiền, lý do...",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    iconBgColor = Color(0xFF06B6D4).copy(alpha = 0.14f),
                    iconTintColor = Color(0xFF0891B2),
                    onClear = { viewModel.setNote("") },
                )

                // Hàng 5: Đính kèm hóa đơn / chứng từ
                ErgonomicFormRow(
                    label = "HÓA ĐƠN & CHỨNG TỪ",
                    primaryValue = if (state.receiptUri == null) "Chưa có hóa đơn" else "Đã đính kèm ảnh hóa đơn ✓",
                    secondaryValue = if (state.receiptUri == null) "Chạm để quét hoặc tải ảnh" else "Ảnh được lưu cùng giao dịch",
                    icon = Icons.Default.DocumentScanner,
                    iconBgColor = Color(0xFF9333EA).copy(alpha = 0.14f),
                    iconTintColor = Color(0xFF9333EA),
                    onClick = { /* Scan receipt action */ },
                )
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    // ==========================================
    // 5. Category Picker Sheet (Screen 3)
    // ==========================================
    if (showCategoryPicker) {
        val desiredType = if (isExpense) CategoryType.EXPENSE else CategoryType.INCOME
        val filteredCategories = state.categories.filter { it.type == desiredType }

        CategoryPickerBottomSheet(
            categories = filteredCategories,
            selectedCategoryId = state.categoryId,
            onSelect = { cat ->
                viewModel.setCategory(cat.id)
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false },
            onAddNew = {
                showCategoryPicker = false
                showCreateCategoryDialog = true
            },
            onLongPressCategory = { cat ->
                if (cat.isDefault) {
                    android.widget.Toast.makeText(context, "Danh mục mặc định không thể sửa/xóa", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    categoryToEdit = cat
                }
            },
        )
    }

    // ==========================================
    // 6. Wallet Picker Sheet
    // ==========================================
    if (showWalletPicker) {
        ModalBottomSheet(
            onDismissRequest = { showWalletPicker = false },
            containerColor = tokens.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Chọn ví tài khoản",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )

                state.wallets.forEach { wallet ->
                    val isSelected = state.walletId == wallet.id
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) tokens.primary.copy(alpha = 0.08f) else tokens.surfaceSoft,
                        border = if (isSelected) BorderStroke(1.5.dp, tokens.primary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.setWallet(wallet.id)
                                showWalletPicker = false
                            },
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF3B82F6).copy(alpha = 0.12f),
                                    modifier = Modifier.size(38.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(walletIcon(wallet.type), null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                                    }
                                }
                                Column {
                                    Text(wallet.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                    Text(formatVndAmount(wallet.balance.value), style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B7280))
                                }
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, null, tint = tokens.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    // Date & Time Picker Dialog
    if (showDatePicker) {
        val currentZoned = remember(state.date) { state.date.atZone(ZoneId.systemDefault()) }
        val initialDateUtcMillis = remember(currentZoned) {
            currentZoned.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateUtcMillis,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                    if (selectedMillis != null) {
                        val selectedLocalDate = Instant.ofEpochMilli(selectedMillis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()

                        val timePickerDialog = android.app.TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                val newDateTime = selectedLocalDate.atTime(hourOfDay, minute)
                                val newInstant = newDateTime.atZone(ZoneId.systemDefault()).toInstant()
                                viewModel.setDate(newInstant)
                            },
                            currentZoned.hour,
                            currentZoned.minute,
                            true, // 24-hour format
                        )
                        timePickerDialog.setOnCancelListener {
                            val newDateTime = selectedLocalDate.atTime(currentZoned.hour, currentZoned.minute)
                            val newInstant = newDateTime.atZone(ZoneId.systemDefault()).toInstant()
                            viewModel.setDate(newInstant)
                        }
                        timePickerDialog.show()
                    }
                }) {
                    Text("Tiếp tục (Chọn giờ)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Category Creation Dialog
    if (showCreateCategoryDialog) {
        CategoryEditorDialog(
            initialType = if (isExpense) CategoryType.EXPENSE else CategoryType.INCOME,
            onDismiss = { showCreateCategoryDialog = false },
            onSave = { name, icon, color, _ ->
                viewModel.createCategory(name, icon, color, onCreated = { showCreateCategoryDialog = false })
            },
        )
    }

    // Category Edit Dialog
    categoryToEdit?.let { cat ->
        CategoryEditorDialog(
            category = cat,
            onDismiss = { categoryToEdit = null },
            onSave = { name, icon, color, type ->
                viewModel.updateCategory(cat.copy(name = name, icon = icon, colorHex = color, type = type), onUpdated = { categoryToEdit = null })
            },
            onDelete = {
                categoryToDelete = cat
                categoryToEdit = null
            },
        )
    }

    // Category Delete Confirmation
    categoryToDelete?.let { cat ->
        FinluxDialog(
            onDismissRequest = { categoryToDelete = null },
            title = "Xóa danh mục?",
            message = "Bạn có chắc chắn muốn xóa danh mục '${cat.name}'? Các giao dịch đã tạo sẽ không bị mất.",
            confirmLabel = "Xóa",
            dismissLabel = "Hủy",
            onConfirm = {
                viewModel.deleteCategory(cat, onDeleted = { categoryToDelete = null })
            },
        )
    }
}

/**
 * Segmented Pill Tab
 */
@Composable
private fun TransactionTypePill(
    label: String,
    isSelected: Boolean,
    activeBg: Color,
    activeText: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) activeBg else if (tokens.isDark) Color(0xFF1E1E2D) else Color(0xFFF3F4F6),
        border = if (isSelected) BorderStroke(1.dp, activeText.copy(alpha = 0.3f)) else null,
        modifier = modifier
            .heightIn(min = 42.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                ),
                color = if (isSelected) activeText else Color(0xFF6B7280),
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Ergonomic Form Row (2-Line label + value with chevron)
 */
@Composable
private fun ErgonomicFormRow(
    label: String,
    primaryValue: String,
    secondaryValue: String? = null,
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    onClick: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
        border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFF3F4F6)),
        shadowElevation = 1.5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconBgColor,
                modifier = Modifier.size(42.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconTintColor, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    ),
                    color = Color(0xFF9CA3AF),
                )
                Text(
                    text = primaryValue,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = tokens.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (secondaryValue != null) {
                    Text(
                        text = secondaryValue,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                        color = Color(0xFF6B7280),
                        maxLines = 1,
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

/**
 * Ergonomic Input Row for Notes
 */
@Composable
private fun ErgonomicInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    onClear: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
        border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFF3F4F6)),
        shadowElevation = 1.5.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconBgColor,
                modifier = Modifier.size(42.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconTintColor, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    ),
                    color = Color(0xFF9CA3AF),
                )
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = tokens.onSurface,
                    ),
                    cursorBrush = SolidColor(tokens.primary),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = Color(0xFF9CA3AF),
                                ),
                            )
                        }
                        innerTextField()
                    },
                )
            }

            if (value.isNotEmpty()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Xóa",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

/**
 * Category Picker Bottom Sheet (Matching Screen 3)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPickerBottomSheet(
    categories: List<Category>,
    selectedCategoryId: String?,
    onSelect: (Category) -> Unit,
    onDismiss: () -> Unit,
    onAddNew: () -> Unit,
    onLongPressCategory: (Category) -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(categories, searchQuery) {
        if (searchQuery.isBlank()) categories
        else categories.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = tokens.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header: Title + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Chọn danh mục",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = tokens.onSurface,
                )

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

            // Search Field (Rounded soft gray bar)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (tokens.isDark) Color(0xFF1E1E2D) else Color(0xFFF3F4F6),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Tìm",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(18.dp),
                    )
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = tokens.onSurface,
                        ),
                        cursorBrush = SolidColor(tokens.primary),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Tìm danh mục",
                                    style = TextStyle(fontSize = 14.sp, color = Color(0xFF9CA3AF)),
                                )
                            }
                            innerTextField()
                        },
                    )
                }
            }

            // 4-Column Grid of Categories
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp),
            ) {
                items(filtered, key = { it.id }) { cat ->
                    val isSelected = cat.id == selectedCategoryId
                    val accent = colorFromHex(cat.colorHex)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .combinedClickable(
                                onClick = { onSelect(cat) },
                                onLongClick = { onLongPressCategory(cat) },
                            ),
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = accent.copy(alpha = if (tokens.isDark) 0.20f else 0.12f),
                                border = if (isSelected) BorderStroke(1.8.dp, Color(0xFFEF4444)) else null,
                                modifier = Modifier.size(54.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = categoryIcon(cat.icon),
                                        contentDescription = cat.name,
                                        tint = accent,
                                        modifier = Modifier.size(26.dp),
                                    )
                                }
                            }

                            // Selected Red Checkmark Badge
                            if (isSelected) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFEF4444),
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(1.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp),
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = cat.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            ),
                            color = if (isSelected) Color(0xFFEF4444) else tokens.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // Bottom "+ Thêm danh mục mới" button
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFF1F2),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onAddNew),
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 13.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Thêm danh mục mới",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Color(0xFFEF4444),
                    )
                }
            }
        }
    }
}

/**
 * Category Editor Dialog for adding/editing a category
 */
@Composable
private fun CategoryEditorDialog(
    category: Category? = null,
    initialType: CategoryType = CategoryType.EXPENSE,
    onDismiss: () -> Unit,
    onSave: (String, String, String, CategoryType) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val tokens = LocalFinluxTokens.current
    var name by remember { mutableStateOf(category?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(category?.icon ?: FinanceCategoryIcons.first().key) }
    var selectedColor by remember { mutableStateOf(category?.colorHex ?: FinanceAccentHexes.first()) }
    var selectedType by remember { mutableStateOf(category?.type ?: initialType) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = tokens.surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = if (category == null) "Tạo danh mục mới" else "Chỉnh sửa danh mục",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên danh mục") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text("Loại danh mục", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransactionTypePill(
                        label = "Chi tiêu",
                        isSelected = selectedType == CategoryType.EXPENSE,
                        activeBg = Color(0xFFFFE4E6),
                        activeText = Color(0xFFE11D48),
                        modifier = Modifier.weight(1f),
                        onClick = { selectedType = CategoryType.EXPENSE },
                    )
                    TransactionTypePill(
                        label = "Thu nhập",
                        isSelected = selectedType == CategoryType.INCOME,
                        activeBg = Color(0xFFDCFCE7),
                        activeText = Color(0xFF16A34A),
                        modifier = Modifier.weight(1f),
                        onClick = { selectedType = CategoryType.INCOME },
                    )
                }

                Text("Chọn biểu tượng", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(FinanceCategoryIcons) { iconOption ->
                        val isSelected = selectedIcon == iconOption.key
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) tokens.primary.copy(alpha = 0.15f) else tokens.surfaceSoft,
                            border = if (isSelected) BorderStroke(1.5.dp, tokens.primary) else null,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedIcon = iconOption.key },
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(iconOption.icon, null, tint = if (isSelected) tokens.primary else tokens.onSurface, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                Text("Chọn màu sắc", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(FinanceAccentHexes) { colorHex: String ->
                        val isSelected = selectedColor == colorHex
                        val color = colorFromHex(colorHex)
                        Surface(
                            shape = CircleShape,
                            color = color,
                            border = if (isSelected) BorderStroke(2.5.dp, tokens.onSurface) else null,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { selectedColor = colorHex },
                        ) {}
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onDelete != null) {
                        TextButton(onClick = onDelete) {
                            Text("Xóa", color = Color(0xFFEF4444))
                        }
                        Spacer(Modifier.weight(1f))
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Hủy")
                    }
                    TextButton(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(name.trim(), selectedIcon, selectedColor, selectedType)
                            }
                        },
                        enabled = name.isNotBlank(),
                    ) {
                        Text("Lưu", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
