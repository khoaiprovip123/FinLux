package com.finlux.app.presentation.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.ExpenseRed
import com.finlux.app.core.designsystem.GlassBottomSheet
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.IncomeGreen
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.walletIcon
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.presentation.home.toVnd
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AddTransactionSheet(
    onDismiss: () -> Unit,
    initialType: TransactionType? = null,
    initialReceiptUri: String? = null,
    viewModel: AddTransactionViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    var showDatePicker by remember { mutableStateOf(false) }
    LaunchedEffect(initialType) { initialType?.let(viewModel::setType) }
    LaunchedEffect(initialReceiptUri) { if (initialReceiptUri != null) viewModel.setReceipt(initialReceiptUri) }
    LaunchedEffect(state.saved) { if (state.saved) { viewModel.consumeSaved(); onDismiss() } }
    GlassBottomSheet(onDismiss = onDismiss) {
        Column(
            Modifier.fillMaxWidth().heightIn(max = 760.dp).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("Thêm giao dịch", style = MaterialTheme.typography.titleLarge); Text("Ghi nhận đầy đủ để báo cáo chính xác", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Icon(Icons.Default.ReceiptLong, null, tint = MaterialTheme.colorScheme.primary)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TypeCard("Chi tiêu", ExpenseRed, state.type == TransactionType.EXPENSE, Modifier.weight(1f)) { viewModel.setType(TransactionType.EXPENSE) }
                TypeCard("Thu nhập", IncomeGreen, state.type == TransactionType.INCOME, Modifier.weight(1f)) { viewModel.setType(TransactionType.INCOME) }
            }
            OutlinedTextField(
                state.amountInput, viewModel::setAmount, Modifier.fillMaxWidth(), label = { Text("Số tiền") },
                supportingText = { state.amountInput.toLongOrNull()?.let { Text(it.toVnd()) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf(50_000L, 100_000L, 500_000L, 1_000_000L)) { amount -> FilterChip(false, { viewModel.setAmount(amount.toString()) }, { Text(amount.toVnd()) }) }
            }
            Text("Danh mục", fontWeight = FontWeight.Bold)
            val desired = if (state.type == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
            LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                items(state.categories.filter { it.type == desired }, key = { it.id }) { category ->
                    val accent = colorFromHex(category.colorHex)
                    val selected = state.categoryId == category.id
                    GlassCard(Modifier.size(width = 108.dp, height = 82.dp), onClick = { viewModel.setCategory(category.id) }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Icon(categoryIcon(category.icon), null, tint = accent)
                            Text(category.name, maxLines = 1, style = MaterialTheme.typography.labelMedium, color = if (selected) accent else MaterialTheme.colorScheme.onSurface)
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
                Text(if (state.isSaving) "Đang lưu…" else "Lưu giao dịch", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
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
