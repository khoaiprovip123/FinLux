package com.finlux.app.presentation.budget.prism

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.component.FinluxBottomSheet
import com.finlux.app.core.designsystem.component.FinluxDialog
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.component.FinluxHeroCard
import com.finlux.app.core.designsystem.component.FinluxScreenHeader
import com.finlux.app.core.designsystem.component.FinluxSoftCard
import com.finlux.app.core.designsystem.component.ErgonomicCompactAmountCard
import com.finlux.app.core.designsystem.component.ErgonomicFormRow
import com.finlux.app.core.designsystem.component.FinluxCategoryPickerBottomSheet
import com.finlux.app.core.designsystem.component.FinluxTransactionRow
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.presentation.budget.BudgetItemUi
import com.finlux.app.presentation.budget.BudgetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrismBudgetScreen(
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: BudgetViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val snackbar = remember { SnackbarHostState() }
    val tokens = LocalFinluxTokens.current

    var editingBudget by remember { mutableStateOf<BudgetItemUi?>(null) }
    var viewingHistoryBudget by remember { mutableStateOf<BudgetItemUi?>(null) }
    var isCreatingBudget by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Budget?>(null) }

    val totalLimit = state.items.sumOf { it.budget.limitAmount.value }
    val totalSpent = state.items.sumOf { it.budget.spentAmount.value }
    val totalRemaining = (totalLimit - totalSpent).coerceAtLeast(0L)
    val overallPercent = if (totalLimit > 0L) ((totalSpent * 100L) / totalLimit).toInt() else 0

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            FinluxScreenHeader(
                title = "Ngân sách chi tiêu",
                subtitle = state.period?.displayLabel ?: "Đang tải...",
                onBack = onBack,
            )
        },
        containerColor = tokens.background,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = tokens.spacing.lg,
                end = tokens.spacing.lg,
                top = tokens.spacing.sm,
                bottom = 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(tokens.spacing.md),
        ) {
            // Month Selector Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { viewModel.previousMonth() }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Tháng trước", tint = tokens.onSurface)
                    }

                    Text(
                        text = state.period?.displayLabel ?: "Đang tải...",
                        style = FinluxTextStyles.SectionTitle,
                        fontWeight = FontWeight.Bold,
                        color = tokens.onSurface,
                    )

                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Tháng sau", tint = tokens.onSurface)
                    }
                }
            }

            // Overview Hero Card
            item {
                FinluxHeroCard(
                    title = "Còn lại trong ngân sách",
                    amountText = formatVndAmount(totalRemaining),
                    deltaText = "$overallPercent% đã dùng (${formatVndAmount(totalSpent, isCompact = true)} / ${formatVndAmount(totalLimit, isCompact = true)})",
                    isPositiveDelta = overallPercent < 90,
                )
            }

            // Add Budget Button
            item {
                Button(
                    onClick = { isCreatingBudget = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(tokens.radius.input),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.primary,
                        contentColor = if (tokens.isDark) Color(0xFF002B3D) else Color.White,
                    ),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Thiết lập ngân sách mới", style = FinluxTextStyles.CardTitle.copy(fontSize = 14.sp))
                }
            }

            // Budget list
            if (state.items.isEmpty()) {
                item {
                    FinluxEmptyState(
                        title = "Chưa có ngân sách",
                        description = "Tạo ngân sách theo từng danh mục (Ăn uống, Tiền trọ, Mua sắm...) để kiểm soát chi tiêu.",
                    )
                }
            } else {
                items(state.items, key = { it.budget.id }) { item ->
                    val budget = item.budget
                    val cat = item.category
                    val spent = budget.spentAmount.value
                    val limit = budget.limitAmount.value
                    val percent = if (limit > 0L) ((spent * 100L) / limit).toInt() else 0
                    val isExceeded = spent > limit
                    val progressFloat = if (limit > 0L) (spent.toFloat() / limit.toFloat()).coerceIn(0f, 1f) else 0f

                    val statusColor = when {
                        isExceeded -> FinluxColors.ExpenseRed
                        percent >= 80 -> FinluxColors.WarningAmber
                        else -> FinluxColors.PrimaryBlue
                    }

                    var showMenu by remember { mutableStateOf(false) }

                    FinluxSoftCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = if (isExceeded) FinluxColors.ExpenseRed.copy(alpha = 0.5f) else null,
                        onClick = { viewingHistoryBudget = item },
                        onLongClick = { editingBudget = item },
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    val catIcon = cat?.icon ?: "Category"
                                    val catColor = cat?.colorHex?.let { colorFromHex(it) } ?: FinluxColors.PrimaryBlue

                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(catColor.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = categoryIcon(catIcon),
                                            contentDescription = null,
                                            tint = catColor,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = cat?.name ?: "Khác",
                                            style = FinluxTextStyles.CardTitle.copy(fontWeight = FontWeight.Bold),
                                            color = tokens.onSurface,
                                        )
                                        Text(
                                            text = if (isExceeded) "Vượt ${formatVndAmount(spent - limit, isCompact = true)}" else "Còn lại ${formatVndAmount(limit - spent, isCompact = true)}",
                                            style = FinluxTextStyles.Caption.copy(
                                                color = statusColor,
                                                fontWeight = FontWeight.SemiBold,
                                            ),
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = "$percent%",
                                        style = FinluxTextStyles.CardTitle.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                        ),
                                        color = statusColor,
                                    )
                                    Box {
                                        IconButton(onClick = { showMenu = true }) {
                                            Icon(Icons.Default.MoreVert, contentDescription = "Tùy chọn", tint = tokens.onSurfaceVariant)
                                        }
                                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                            DropdownMenuItem(
                                                text = { Text("Lịch sử chi tiêu") },
                                                leadingIcon = { Icon(Icons.Default.History, contentDescription = null) },
                                                onClick = {
                                                    showMenu = false
                                                    viewingHistoryBudget = item
                                                },
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Chỉnh sửa") },
                                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                                onClick = {
                                                    showMenu = false
                                                    editingBudget = item
                                                },
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Xóa ngân sách", color = FinluxColors.ExpenseRed) },
                                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = FinluxColors.ExpenseRed) },
                                                onClick = {
                                                    showMenu = false
                                                    pendingDelete = budget
                                                },
                                            )
                                        }
                                    }
                                }
                            }

                            // Progress Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(tokens.surfaceSoft),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = progressFloat)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(statusColor),
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "Đã chi: ${formatVndAmount(spent)}",
                                    style = FinluxTextStyles.MicroLabel,
                                    color = tokens.onSurfaceVariant,
                                )
                                Text(
                                    text = "Hạn mức: ${formatVndAmount(limit)}",
                                    style = FinluxTextStyles.MicroLabel,
                                    color = tokens.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Budget Bottom Sheet
    if (isCreatingBudget || editingBudget != null) {
        val target = editingBudget
        val expenseCategories = state.categories.filter { it.type == CategoryType.EXPENSE }
        var selectedCategoryId by remember(target) {
            mutableStateOf(target?.budget?.categoryId ?: expenseCategories.firstOrNull()?.id ?: "")
        }
        var limitInput by remember(target) {
            mutableStateOf(target?.budget?.limitAmount?.value?.toString() ?: "")
        }
        var showCategoryPicker by remember { mutableStateOf(false) }

        FinluxBottomSheet(
            onDismissRequest = {
                isCreatingBudget = false
                editingBudget = null
            },
            title = if (target == null) "Thêm ngân sách" else "Sửa ngân sách",
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.spacing.lg, vertical = tokens.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(tokens.spacing.md),
            ) {
                val activeCategory = expenseCategories.firstOrNull { it.id == selectedCategoryId }
                val catAccent = activeCategory?.let { colorFromHex(it.colorHex, tokens.primary) } ?: tokens.primary
                val catIcon = activeCategory?.let { categoryIcon(it.icon) } ?: Icons.Default.Category

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
                    label = "HẠN MỨC CHI TIÊU THÁNG",
                    amountText = limitInput,
                    onAmountChange = { limitInput = it },
                    placeholder = "0",
                    amountColor = tokens.primary,
                    modifier = Modifier.fillMaxWidth(),
                )

                Button(
                    onClick = {
                        val limit = limitInput.toLongOrNull() ?: 0L
                        if (limit > 0L && selectedCategoryId.isNotBlank()) {
                            viewModel.save(
                                categoryId = selectedCategoryId,
                                limit = limit,
                                existing = target?.budget,
                                onSaved = {
                                    isCreatingBudget = false
                                    editingBudget = null
                                },
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(tokens.radius.input),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.primary,
                        contentColor = if (tokens.isDark) Color(0xFF002B3D) else Color.White,
                    ),
                    enabled = (limitInput.toLongOrNull() ?: 0L) > 0L && selectedCategoryId.isNotBlank(),
                ) {
                    Text("Lưu ngân sách", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        if (showCategoryPicker) {
            FinluxCategoryPickerBottomSheet(
                categories = expenseCategories,
                selectedCategoryId = selectedCategoryId,
                onSelectCategory = { cat ->
                    selectedCategoryId = cat.id
                    showCategoryPicker = false
                },
                onDismiss = { showCategoryPicker = false },
            )
        }
    }

    // Category Transactions History Bottom Sheet (Opened on Single Tap)
    viewingHistoryBudget?.let { itemUi ->
        val cat = itemUi.category
        val catId = itemUi.budget.categoryId
        val catNameLower = cat?.name?.lowercase()?.trim()
        val catIcon = cat?.icon ?: "Category"
        val catColor = cat?.colorHex?.let { colorFromHex(it) } ?: tokens.primary
        val limit = itemUi.budget.limitAmount.value
        val spent = itemUi.budget.spentAmount.value
        val isExceeded = spent > limit
        val progressFloat = if (limit > 0L) (spent.toFloat() / limit.toFloat()).coerceIn(0f, 1f) else 0f
        val percent = if (limit > 0L) ((spent * 100L) / limit).toInt() else 0
        val statusColor = when {
            isExceeded -> FinluxColors.ExpenseRed
            percent >= 80 -> FinluxColors.WarningAmber
            else -> FinluxColors.PrimaryBlue
        }

        val categoryTransactions = state.transactions.filter { tx ->
            tx.categoryId == catId || (catNameLower != null && tx.categoryId?.lowercase()?.trim() == catNameLower)
        }.sortedByDescending { it.date }

        FinluxBottomSheet(
            onDismissRequest = { viewingHistoryBudget = null },
            title = "Lịch sử chi tiêu: ${cat?.name ?: "Danh mục"}",
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.spacing.lg, vertical = tokens.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Header summary card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = tokens.surfaceSoft,
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(catColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = categoryIcon(catIcon),
                                        contentDescription = null,
                                        tint = catColor,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                                Column {
                                    Text(
                                        text = cat?.name ?: "Khác",
                                        style = FinluxTextStyles.CardTitle.copy(fontWeight = FontWeight.Bold),
                                        color = tokens.onSurface,
                                    )
                                    Text(
                                        text = state.period?.displayLabel ?: "Kỳ này",
                                        style = FinluxTextStyles.MicroLabel,
                                        color = tokens.onSurfaceVariant,
                                    )
                                }
                            }

                            Text(
                                text = "$percent%",
                                style = FinluxTextStyles.CardTitle.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                ),
                                color = statusColor,
                            )
                        }

                        // Progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = progressFloat)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(statusColor),
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "Đã chi: ${formatVndAmount(spent)}",
                                style = FinluxTextStyles.MicroLabel,
                                color = tokens.onSurfaceVariant,
                            )
                            Text(
                                text = "Hạn mức: ${formatVndAmount(limit)}",
                                style = FinluxTextStyles.MicroLabel,
                                color = tokens.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Transaction list header
                Text(
                    text = "CÁC KHOẢN GIAO DỊCH (${categoryTransactions.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    ),
                    color = Color(0xFF9CA3AF),
                )

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
                            FinluxTransactionRow(
                                transaction = tx,
                                category = cat,
                                onClick = null,
                            )
                        }
                    }
                }

                // Bottom Edit Budget Quick Button
                Button(
                    onClick = {
                        viewingHistoryBudget = null
                        editingBudget = itemUi
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(tokens.radius.input),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.surfaceSoft,
                        contentColor = tokens.primary,
                    ),
                    border = BorderStroke(1.dp, tokens.border),
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Chỉnh sửa ngân sách này", fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Delete confirmation dialog
    pendingDelete?.let { budget ->
        FinluxDialog(
            onDismissRequest = { pendingDelete = null },
            title = "Xóa ngân sách?",
            message = "Ngân sách hạn mức ${formatVndAmount(budget.limitAmount.value)} sẽ bị gỡ bỏ khỏi ${state.period?.displayLabel ?: "kỳ này"}.",
            confirmLabel = "Xác nhận xóa",
            dismissLabel = "Hủy",
            onConfirm = {
                viewModel.delete(budget)
                pendingDelete = null
            },
        )
    }
}
