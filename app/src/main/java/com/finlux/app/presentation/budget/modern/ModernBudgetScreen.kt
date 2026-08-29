package com.finlux.app.presentation.budget.modern

import com.finlux.app.presentation.budget.*
import com.finlux.app.core.designsystem.modern.*

import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
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
import androidx.compose.material.icons.filled.Category
import com.finlux.app.core.designsystem.ExpenseRed
import com.finlux.app.core.designsystem.modern.GlassCard
import com.finlux.app.core.designsystem.GlassDialogSurface
import com.finlux.app.core.designsystem.modern.GlassTopBar
import com.finlux.app.core.designsystem.GradientHeroCard
import com.finlux.app.core.designsystem.IncomeGreen
import com.finlux.app.core.designsystem.WarningAmber
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.component.ErgonomicCompactAmountCard
import com.finlux.app.core.designsystem.component.ErgonomicFormRow
import com.finlux.app.core.designsystem.component.FinluxBottomSheet
import com.finlux.app.core.designsystem.component.FinluxCategoryPickerBottomSheet
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.component.FinluxTransactionRow
import com.finlux.app.core.navigation.Route
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.usecase.BudgetLevel
import com.finlux.app.presentation.components.MainBottomBar
import com.finlux.app.presentation.home.toShortVnd
import com.finlux.app.presentation.home.toVnd
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ModernBudgetScreen(
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    com.finlux.app.core.designsystem.NotificationPermissionHandler()
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val snackbar = remember { SnackbarHostState() }
    var editing by remember { mutableStateOf<BudgetItemUi?>(null) }
    var viewingHistory by remember { mutableStateOf<BudgetItemUi?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    LaunchedEffect(state.message) { state.message?.let { snackbar.showSnackbar(it); viewModel.consumeMessage() } }
    val spent = state.items.sumOf { it.budget.spentAmount.value }
    val limit = state.items.sumOf { it.budget.limitAmount.value }
    Box(Modifier.fillMaxSize()) {
        com.finlux.app.core.designsystem.modern.FinluxStyleBackdrop(Modifier.fillMaxSize())
        Scaffold(
            topBar = {
                GlassTopBar(
                    title = { Text("Ngân sách", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        IconButton(onClick = { onBack?.invoke() ?: onNavigate(Route.Home.value) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                        }
                    },
                    actions = { IconButton(onClick = { editing = null; showEditor = true }) { Icon(Icons.Default.Add, "Thêm ngân sách") } },
                )
            },
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
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            IconButton(onClick = viewModel::previousMonth) { Icon(Icons.Default.ChevronLeft, "Kỳ trước") }
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                Text(state.period?.displayLabel ?: "Đang tải...", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                val isPast = state.period?.let { it.endExclusive <= java.time.Instant.now() } == true
                                if (isPast) Text("Chạm để về kỳ hiện tại", Modifier.padding(top = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = viewModel::nextMonth, enabled = state.period?.let { it.start < java.time.Instant.now() } == true) { Icon(Icons.Default.ChevronRight, "Kỳ sau") }
                        }
                    }
                }
                val isPast = state.period?.let { it.endExclusive <= java.time.Instant.now() } == true
                if (isPast) item {
                    Button(onClick = viewModel::currentMonth, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.CalendarMonth, null); Text("Về kỳ hiện tại", Modifier.padding(start = 8.dp)) }
                }
                item {
                    GlassCard(
                        Modifier.fillMaxWidth(),
                        mode = com.finlux.app.core.designsystem.modern.LiquidGlassMode.CLEAR,
                        tint = com.finlux.app.core.designsystem.FinluxPurple,
                        padding = PaddingValues(18.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Tổng ngân sách", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(limit.toVnd(), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Đã chi ${spent.toShortVnd()}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                Text("Còn lại ${(limit - spent).toVnd()}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                if (state.items.isEmpty()) item { GlassCard(Modifier.fillMaxWidth(), onClick = { showEditor = true }) { Text("Chưa có ngân sách trong tháng này · Chạm để thêm", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                items(state.items, key = { it.budget.id }) { item ->
                    val color = when (item.status.level) { BudgetLevel.SAFE -> IncomeGreen; BudgetLevel.WARNING -> WarningAmber; BudgetLevel.EXCEEDED -> ExpenseRed }
                    GlassCard(
                        Modifier.fillMaxWidth(),
                        tint = color,
                        onClick = { viewingHistory = item },
                        onLongClick = { editing = item; showEditor = true },
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Text(item.category?.name ?: "Danh mục", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Text("${(item.status.progress * 100).toInt()}%", color = color, fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { editing = item; showEditor = true }) { Icon(Icons.Default.Edit, "Sửa") }
                                    IconButton(onClick = { viewModel.delete(item.budget) }) { Icon(Icons.Default.DeleteOutline, "Xóa") }
                                }
                            }
                            LinearProgressIndicator({ item.status.progress.coerceIn(0f, 1f) }, Modifier.fillMaxWidth(), color = color, trackColor = color.copy(alpha = .13f))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Đã chi ${item.budget.spentAmount.value.toVnd()}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                Text(item.budget.limitAmount.value.toVnd(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
    viewingHistory?.let { itemUi ->
        val cat = itemUi.category
        val catId = itemUi.budget.categoryId
        val catNameLower = cat?.name?.lowercase()?.trim()
        val categoryTransactions = state.transactions.filter { tx ->
            tx.categoryId == catId || (catNameLower != null && tx.categoryId?.lowercase()?.trim() == catNameLower)
        }.sortedByDescending { it.date }

        FinluxBottomSheet(
            onDismissRequest = { viewingHistory = null },
            title = "Lịch sử chi tiêu: ${cat?.name ?: "Danh mục"}",
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (categoryTransactions.isEmpty()) {
                    FinluxEmptyState(
                        title = "Chưa có giao dịch",
                        description = "Chưa có giao dịch chi tiêu nào trong danh mục ${cat?.name ?: ""} vào ${state.period?.displayLabel ?: "kỳ này"}.",
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(categoryTransactions, key = { it.id }) { tx ->
                            FinluxTransactionRow(transaction = tx, category = cat, onClick = null)
                        }
                    }
                }
                Button(
                    onClick = {
                        val toEdit = viewingHistory
                        viewingHistory = null
                        editing = toEdit
                        showEditor = true
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Chỉnh sửa ngân sách này", fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
    if (showEditor) BudgetEditor(state.categories, state.period, editing, state.busy, { showEditor = false }) { categoryId, amount ->
        viewModel.save(categoryId, amount, editing?.budget) { showEditor = false }
    }
}

@Composable
private fun BudgetEditor(categories: List<Category>, period: com.finlux.app.domain.model.FinancialPeriod?, initial: BudgetItemUi?, busy: Boolean, onDismiss: () -> Unit, onSave: (String, Long) -> Unit) {
    var categoryId by remember(initial) { mutableStateOf(initial?.budget?.categoryId ?: categories.firstOrNull()?.id.orEmpty()) }
    var amount by remember(initial) { mutableStateOf(initial?.budget?.limitAmount?.value?.toString().orEmpty()) }
    var showCategoryPicker by remember { mutableStateOf(false) }

    val activeCategory = categories.firstOrNull { it.id == categoryId }
    val catAccent = activeCategory?.let { colorFromHex(it.colorHex) } ?: MaterialTheme.colorScheme.primary
    val catIcon = activeCategory?.let { categoryIcon(it.icon) } ?: Icons.Default.Category

    Dialog(onDismissRequest = onDismiss) {
        GlassDialogSurface {
            Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Text(if (initial == null) "Thêm ngân sách" else "Sửa ngân sách", style = MaterialTheme.typography.titleLarge)
                Text("Áp dụng: ${period?.displayLabel ?: "Đang tải..."}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                ErgonomicFormRow(
                    label = "DANH MỤC CHI TIÊU",
                    primaryValue = activeCategory?.name ?: "Chưa chọn danh mục",
                    secondaryValue = "Khoản chi tiêu ngân sách",
                    icon = catIcon,
                    iconBgColor = catAccent.copy(alpha = 0.14f),
                    iconTintColor = catAccent,
                    onClick = { showCategoryPicker = true },
                )

                ErgonomicCompactAmountCard(
                    label = "HẠN MỨC THÁNG",
                    amountText = amount,
                    onAmountChange = { amount = it },
                    placeholder = "0",
                    amountColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Smart Threshold Alert Info Card
                val limitValue = amount.toLongOrNull() ?: 0L
                val warn80Amount = if (limitValue > 0L) (limitValue * 80L) / 100L else 0L

                com.finlux.app.core.designsystem.component.FinluxSoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = "Cảnh Báo Vượt Ngưỡng Tự Động",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "🟡 Cảnh báo vàng (80%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = if (warn80Amount > 0) com.finlux.app.core.designsystem.component.formatVndAmount(warn80Amount) else "0 đ",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = WarningAmber,
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "🔴 Cảnh báo đỏ (100%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = if (limitValue > 0) com.finlux.app.core.designsystem.component.formatVndAmount(limitValue) else "0 đ",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = ExpenseRed,
                            )
                        }

                        Text(
                            text = "Hệ thống sẽ tự động gửi thông báo khi chi tiêu chạm 80% và vượt 100% hạn mức theo chuẩn BR-09.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        )
                    }
                }

                Button({ onSave(categoryId, amount.toLongOrNull() ?: 0) }, Modifier.fillMaxWidth(), enabled = categoryId.isNotBlank() && amount.toLongOrNull()?.let { it > 0 } == true && !busy) { Text(if (busy) "Đang lưu…" else "Lưu ngân sách") }
            }
        }
    }

    if (showCategoryPicker) {
        FinluxCategoryPickerBottomSheet(
            categories = categories,
            selectedCategoryId = categoryId,
            onSelectCategory = { cat ->
                categoryId = cat.id
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false },
        )
    }
}
