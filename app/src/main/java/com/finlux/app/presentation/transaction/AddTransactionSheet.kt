package com.finlux.app.presentation.transaction

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.ExpenseRed
import com.finlux.app.core.designsystem.FinanceAccentHexes
import com.finlux.app.core.designsystem.FinanceCategoryIcons
import com.finlux.app.core.designsystem.GlassBottomSheet
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.GlassDialogSurface
import com.finlux.app.core.designsystem.IncomeGreen
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.walletIcon
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.presentation.home.toVnd
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddTransactionSheet(
    onDismiss: () -> Unit,
    initialType: TransactionType? = null,
    initialReceiptUri: String? = null,
    initialTransaction: FinanceTransaction? = null,
    viewModel: AddTransactionViewModel = hiltViewModel(),
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val state = viewModel.state.collectAsStateWithLifecycle().value
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var categoryActionTarget by remember { mutableStateOf<com.finlux.app.domain.model.Category?>(null) }
    var categoryToEdit by remember { mutableStateOf<com.finlux.app.domain.model.Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<com.finlux.app.domain.model.Category?>(null) }

    LaunchedEffect(initialTransaction) {
        if (initialTransaction != null) {
            viewModel.setEditingTransaction(initialTransaction)
        } else {
            initialType?.let(viewModel::setType)
        }
    }
    LaunchedEffect(initialReceiptUri) { if (initialReceiptUri != null) viewModel.setReceipt(initialReceiptUri) }
    LaunchedEffect(state.saved) { if (state.saved) { viewModel.consumeSaved(); onDismiss() } }
    GlassBottomSheet(onDismiss = onDismiss) {
        Column(
            Modifier.fillMaxWidth().heightIn(max = 760.dp).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        if (state.editingTransaction != null) "Sửa giao dịch" else "Thêm giao dịch",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        if (state.editingTransaction != null) "Cập nhật thông tin giao dịch chính xác" else "Ghi nhận đầy đủ để báo cáo chính xác",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(Icons.Default.ReceiptLong, null, tint = MaterialTheme.colorScheme.primary)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TypeCard("Chi tiêu", ExpenseRed, state.type == TransactionType.EXPENSE, Modifier.weight(1f)) { viewModel.setType(TransactionType.EXPENSE) }
                TypeCard("Thu nhập", IncomeGreen, state.type == TransactionType.INCOME, Modifier.weight(1f)) { viewModel.setType(TransactionType.INCOME) }
            }
            OutlinedTextField(
                state.amountInput, viewModel::setAmount, Modifier.fillMaxWidth(), label = { Text("Số tiền") },
                supportingText = { Text((state.amountInput.toLongOrNull() ?: 0L).toVnd()) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf(50_000L, 100_000L, 500_000L, 1_000_000L)) { amount -> FilterChip(false, { viewModel.setAmount(amount.toString()) }, { Text(amount.toVnd()) }) }
            }

            val desired = if (state.type == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
            val filteredCategories = state.categories.filter { it.type == desired }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Danh mục", fontWeight = FontWeight.Bold)
                TextButton(onClick = { showCategoryPicker = true }) {
                    Text("Xem tất cả (${filteredCategories.size})", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                items(filteredCategories, key = { it.id }) { category ->
                    val accent = colorFromHex(category.colorHex)
                    val selected = state.categoryId == category.id
                    GlassCard(
                        modifier = Modifier.size(width = 108.dp, height = 82.dp).combinedClickable(
                            onLongClick = {
                                if (category.isDefault) {
                                    android.widget.Toast.makeText(context, "Danh mục mặc định không thể sửa/xóa", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    categoryActionTarget = category
                                }
                            },
                            onClick = { viewModel.setCategory(category.id) }
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Icon(categoryIcon(category.icon), null, tint = accent)
                            Text(category.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium, color = if (selected) accent else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                item {
                    GlassCard(Modifier.size(width = 108.dp, height = 82.dp), onClick = { showCreateCategoryDialog = true }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Add, "Tạo mới", tint = MaterialTheme.colorScheme.primary)
                            Text("+ Tạo mới", maxLines = 1, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                item {
                    GlassCard(Modifier.size(width = 108.dp, height = 82.dp), onClick = { showCategoryPicker = true }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.MoreHoriz, "Xem thêm", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Xem tất cả", maxLines = 1, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Text("Thanh toán bằng", fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                items(state.wallets, key = { it.id }) { wallet ->
                    val selected = state.walletId == wallet.id
                    FilterChip(
                        selected, { viewModel.setWallet(wallet.id) },
                        label = { Text(wallet.name) }, leadingIcon = { Icon(walletIcon(wallet.type), null, Modifier.size(18.dp)) },
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp))
                    Text(state.date.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), Modifier.padding(start = 7.dp))
                }
                GlassCard(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text("Tính vào báo cáo", Modifier.padding(start = 7.dp), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            OutlinedTextField(state.note, viewModel::setNote, Modifier.fillMaxWidth(), label = { Text("Ghi chú / mô tả") }, minLines = 2, maxLines = 4)
            GlassCard(Modifier.fillMaxWidth(), onClick = { }) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ReceiptLong, null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(if (state.receiptUri == null) "Không có ảnh hóa đơn" else "Đã đính kèm hóa đơn", fontWeight = FontWeight.Bold)
                        Text(if (state.receiptUri == null) "Dùng Quét hóa đơn từ menu + để thêm ảnh" else "Ảnh sẽ được lưu cùng giao dịch", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(viewModel::save, Modifier.fillMaxWidth().height(54.dp), enabled = !state.isSaving) {
                Text(
                    if (state.isSaving) "Đang lưu…" else if (state.editingTransaction != null) "Lưu thay đổi" else "Lưu giao dịch",
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showCategoryPicker) {
        val desired = if (state.type == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
        val filteredCategories = state.categories.filter { it.type == desired }
        GlassBottomSheet(onDismiss = { showCategoryPicker = false }) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Tất cả danh mục (${if (state.type == TransactionType.INCOME) "Thu nhập" else "Chi tiêu"})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Nhấn giữ vào danh mục tùy chỉnh để Sửa / Xóa",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { showCategoryPicker = false; showCreateCategoryDialog = true }) {
                        Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                        Text("Tạo danh mục", Modifier.padding(start = 4.dp))
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp)
                ) {
                    item {
                        GlassCard(
                            Modifier.height(86.dp),
                            onClick = { showCategoryPicker = false; showCreateCategoryDialog = true }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                Text("+ Tạo mới", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    items(filteredCategories, key = { it.id }) { category ->
                        val accent = colorFromHex(category.colorHex)
                        val selected = state.categoryId == category.id
                        GlassCard(
                            modifier = Modifier.height(86.dp).combinedClickable(
                                onLongClick = {
                                    if (category.isDefault) {
                                        android.widget.Toast.makeText(context, "Danh mục mặc định không thể sửa/xóa", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        categoryActionTarget = category
                                    }
                                },
                                onClick = {
                                    viewModel.setCategory(category.id)
                                    showCategoryPicker = false
                                }
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(categoryIcon(category.icon), null, tint = accent, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    category.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) accent else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (categoryActionTarget != null) {
        val target = categoryActionTarget!!
        Dialog(onDismissRequest = { categoryActionTarget = null }) {
            GlassDialogSurface {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(categoryIcon(target.icon), null, tint = colorFromHex(target.colorHex), modifier = Modifier.size(26.dp))
                        Text(
                            target.name,
                            Modifier.padding(start = 10.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("Quản lý danh mục tùy chỉnh", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Button(
                        onClick = {
                            categoryToEdit = target
                            categoryActionTarget = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Default.Edit, null, Modifier.size(18.dp))
                        Text("Chỉnh sửa danh mục", Modifier.padding(start = 8.dp))
                    }

                    Button(
                        onClick = {
                            categoryToDelete = target
                            categoryActionTarget = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Default.DeleteOutline, null, Modifier.size(18.dp))
                        Text("Xóa danh mục", Modifier.padding(start = 8.dp))
                    }

                    TextButton(
                        onClick = { categoryActionTarget = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Hủy")
                    }
                }
            }
        }
    }

    if (categoryToDelete != null) {
        val target = categoryToDelete!!
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Xác nhận xóa danh mục") },
            text = { Text("Bạn có chắc muốn xóa danh mục \"${target.name}\" không?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCategory(target) {
                            categoryToDelete = null
                            android.widget.Toast.makeText(context, "Đã xóa danh mục ${target.name}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    if (categoryToEdit != null) {
        val target = categoryToEdit!!
        var editName by remember(target) { mutableStateOf(target.name) }
        var editIconKey by remember(target) { mutableStateOf(target.icon) }
        var editColorHex by remember(target) { mutableStateOf(target.colorHex) }

        Dialog(onDismissRequest = { categoryToEdit = null }) {
            GlassDialogSurface {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Chỉnh sửa danh mục", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it.take(32) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tên danh mục") },
                        singleLine = true
                    )
                    Text("Chọn biểu tượng", fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        items(FinanceCategoryIcons, key = { it.key }) { option ->
                            val selected = editIconKey == option.key
                            Box(
                                Modifier.size(44.dp)
                                    .background(
                                        if (selected) colorFromHex(editColorHex).copy(alpha = .22f) else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(13.dp)
                                    )
                                    .clickable { editIconKey = option.key },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(option.icon, option.label, tint = if (selected) colorFromHex(editColorHex) else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Text("Màu nhận diện", fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(FinanceAccentHexes) { hex ->
                            Box(
                                Modifier.size(if (hex == editColorHex) 34.dp else 30.dp)
                                    .background(colorFromHex(hex), CircleShape)
                                    .clickable { editColorHex = hex },
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { categoryToEdit = null }) {
                            Text("Hủy")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.updateCategory(target.copy(name = editName.trim(), icon = editIconKey, colorHex = editColorHex)) {
                                    categoryToEdit = null
                                    android.widget.Toast.makeText(context, "Đã cập nhật danh mục", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = editName.isNotBlank()
                        ) {
                            Text("Lưu thay đổi")
                        }
                    }
                }
            }
        }
    }

    if (showCreateCategoryDialog) {
        var newCategoryName by remember { mutableStateOf("") }
        var selectedIconKey by remember { mutableStateOf(FinanceCategoryIcons.first().key) }
        var selectedColorHex by remember { mutableStateOf(FinanceAccentHexes.first()) }

        Dialog(onDismissRequest = { showCreateCategoryDialog = false }) {
            GlassDialogSurface {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        "Tạo danh mục ${if (state.type == TransactionType.INCOME) "Thu nhập" else "Chi tiêu"}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it.take(32) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tên danh mục") },
                        singleLine = true
                    )
                    Text("Chọn biểu tượng", fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        items(FinanceCategoryIcons, key = { it.key }) { option ->
                            val selected = selectedIconKey == option.key
                            Box(
                                Modifier.size(44.dp)
                                    .background(
                                        if (selected) colorFromHex(selectedColorHex).copy(alpha = .22f) else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(13.dp)
                                    )
                                    .clickable { selectedIconKey = option.key },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(option.icon, option.label, tint = if (selected) colorFromHex(selectedColorHex) else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Text("Màu nhận diện", fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(FinanceAccentHexes) { hex ->
                            Box(
                                Modifier.size(if (hex == selectedColorHex) 34.dp else 30.dp)
                                    .background(colorFromHex(hex), CircleShape)
                                    .clickable { selectedColorHex = hex },
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { showCreateCategoryDialog = false }) {
                            Text("Hủy")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.createCategory(newCategoryName, selectedIconKey, selectedColorHex) { newCatId ->
                                    viewModel.setCategory(newCatId)
                                    showCreateCategoryDialog = false
                                    showCategoryPicker = false
                                }
                            },
                            enabled = newCategoryName.isNotBlank()
                        ) {
                            Text("Lưu danh mục")
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = state.date.toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { picker.selectedDateMillis?.let { viewModel.setDate(Instant.ofEpochMilli(it)) }; showDatePicker = false }) { Text("Chọn") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Hủy") } },
        ) { DatePicker(picker) }
    }
}

@Composable
private fun TypeCard(label: String, accent: Color, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier.background(if (selected) accent.copy(alpha = .14f) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(14.dp),
        contentAlignment = Alignment.Center,
    ) { Text(label, color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold) }
}

