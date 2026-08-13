package com.finlux.app.presentation.budget

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.ExpenseRed
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.GlassDialogSurface
import com.finlux.app.core.designsystem.GlassTopBar
import com.finlux.app.core.designsystem.GradientHeroCard
import com.finlux.app.core.designsystem.IncomeGreen
import com.finlux.app.core.designsystem.WarningAmber
import com.finlux.app.core.navigation.Route
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.usecase.BudgetLevel
import com.finlux.app.presentation.components.MainBottomBar
import com.finlux.app.presentation.home.toShortVnd
import com.finlux.app.presentation.home.toVnd
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun BudgetScreen(
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val snackbar = remember { SnackbarHostState() }
    var editing by remember { mutableStateOf<BudgetItemUi?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    LaunchedEffect(state.message) { state.message?.let { snackbar.showSnackbar(it); viewModel.consumeMessage() } }
    val spent = state.items.sumOf { it.budget.spentAmount.value }
    val limit = state.items.sumOf { it.budget.limitAmount.value }
    Scaffold(
        topBar = {
            GlassTopBar(
                title = { Text("Ngân sách", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onBack?.invoke() ?: onNavigate(Route.Home.value) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                },
                actions = { IconButton(onClick = { editing = null; showEditor = true }) { Icon(Icons.Default.Add, "Thêm ngân sách") } },
            )
        },
        bottomBar = { MainBottomBar(Route.Budget.value, onNavigate, onAdd) },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = Color.Transparent,
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            item {
                GlassCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        IconButton(onClick = viewModel::previousMonth) { Icon(Icons.Default.ChevronLeft, "Tháng trước") }
                        Column {
                            Text("Tháng ${state.month.format(DateTimeFormatter.ofPattern("MM/yyyy", Locale("vi", "VN")))}", fontWeight = FontWeight.Bold)
                            if (state.month != YearMonth.now()) Text("Chạm để về tháng hiện tại", Modifier.padding(top = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = viewModel::nextMonth, enabled = state.month < YearMonth.now()) { Icon(Icons.Default.ChevronRight, "Tháng sau") }
                    }
                }
            }
            if (state.month != YearMonth.now()) item {
                Button(onClick = viewModel::currentMonth, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.CalendarMonth, null); Text("Về tháng hiện tại", Modifier.padding(start = 8.dp)) }
            }
            item {
                GradientHeroCard(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Tổng ngân sách", color = Color.White.copy(alpha = .8f))
                        Text(limit.toVnd(), color = Color.White, style = MaterialTheme.typography.headlineMedium)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Đã chi ${spent.toShortVnd()}", color = Color.White.copy(alpha = .84f))
                            Text("Còn lại ${(limit - spent).toShortVnd()}", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            if (state.items.isEmpty()) item { GlassCard(Modifier.fillMaxWidth(), onClick = { showEditor = true }) { Text("Chưa có ngân sách trong tháng này · Chạm để thêm") } }
            items(state.items, key = { it.budget.id }) { item ->
                val color = when (item.status.level) { BudgetLevel.SAFE -> IncomeGreen; BudgetLevel.WARNING -> WarningAmber; BudgetLevel.EXCEEDED -> ExpenseRed }
                GlassCard(Modifier.fillMaxWidth(), onClick = { editing = item; showEditor = true }) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.category?.name ?: "Danh mục", style = MaterialTheme.typography.titleMedium)
                            Row { Text("${(item.status.progress * 100).toInt()}%", color = color, fontWeight = FontWeight.Bold); IconButton(onClick = { editing = item; showEditor = true }) { Icon(Icons.Default.Edit, "Sửa") }; IconButton(onClick = { viewModel.delete(item.budget) }) { Icon(Icons.Default.DeleteOutline, "Xóa") } }
                        }
                        LinearProgressIndicator({ item.status.progress.coerceIn(0f, 1f) }, Modifier.fillMaxWidth(), color = color, trackColor = color.copy(alpha = .13f))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Đã chi ${item.budget.spentAmount.value.toVnd()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(item.budget.limitAmount.value.toVnd(), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
    if (showEditor) BudgetEditor(state.categories, state.month, editing, state.busy, { showEditor = false }) { categoryId, amount ->
        viewModel.save(categoryId, amount, editing?.budget) { showEditor = false }
    }
}

@Composable
private fun BudgetEditor(categories: List<Category>, month: YearMonth, initial: BudgetItemUi?, busy: Boolean, onDismiss: () -> Unit, onSave: (String, Long) -> Unit) {
    var categoryId by remember(initial) { mutableStateOf(initial?.budget?.categoryId ?: categories.firstOrNull()?.id.orEmpty()) }
    var amount by remember(initial) { mutableStateOf(initial?.budget?.limitAmount?.value?.toString().orEmpty()) }
    Dialog(onDismissRequest = onDismiss) {
        GlassDialogSurface {
            Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Text(if (initial == null) "Thêm ngân sách" else "Sửa ngân sách", style = MaterialTheme.typography.titleLarge)
                Text("Áp dụng tháng ${month.monthValue}/${month.year}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Danh mục", fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(categories) { category -> FilterChip(categoryId == category.id, { categoryId = category.id }, { Text(category.name) }) } }
                OutlinedTextField(
                    amount,
                    { amount = it.filter(Char::isDigit).take(15) },
                    Modifier.fillMaxWidth(),
                    label = { Text("Hạn mức tháng") },
                    supportingText = { Text((amount.toLongOrNull() ?: 0L).toVnd()) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Button({ onSave(categoryId, amount.toLongOrNull() ?: 0) }, Modifier.fillMaxWidth(), enabled = categoryId.isNotBlank() && amount.toLongOrNull()?.let { it > 0 } == true && !busy) { Text(if (busy) "Đang lưu…" else "Lưu ngân sách") }
            }
        }
    }
}
