package com.finlux.app.presentation.budget.prism

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.CategoryType
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
                subtitle = "Tháng ${state.month.monthValue}/${state.month.year}",
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
                        text = "Tháng ${state.month.monthValue}, ${state.month.year}",
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
                        contentColor = Color.White,
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
                        onClick = { editingBudget = item },
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
                Text("Chọn danh mục chi tiêu", style = FinluxTextStyles.SectionTitle, color = tokens.onSurface)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(expenseCategories, key = { it.id }) { cat ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.radius.smallChip))
                                .clickable { selectedCategoryId = cat.id },
                            color = if (selectedCategoryId == cat.id) tokens.primary.copy(alpha = 0.15f) else tokens.surfaceSoft,
                            shape = RoundedCornerShape(tokens.radius.smallChip),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    imageVector = categoryIcon(cat.icon),
                                    contentDescription = null,
                                    tint = colorFromHex(cat.colorHex),
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(
                                    text = cat.name,
                                    style = FinluxTextStyles.Body.copy(
                                        fontWeight = if (selectedCategoryId == cat.id) FontWeight.Bold else FontWeight.Normal,
                                    ),
                                    color = if (selectedCategoryId == cat.id) tokens.primary else tokens.onSurface,
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = limitInput,
                    onValueChange = { limitInput = it.filter(Char::isDigit) },
                    label = { Text("Hạn mức chi tiêu tháng (VNĐ)") },
                    placeholder = { Text("Ví dụ: 3000000") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.radius.input),
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
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.primary),
                    enabled = (limitInput.toLongOrNull() ?: 0L) > 0L && selectedCategoryId.isNotBlank(),
                ) {
                    Text("Lưu ngân sách", fontWeight = FontWeight.Bold, color = Color.White)
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
            message = "Ngân sách hạn mức ${formatVndAmount(budget.limitAmount.value)} sẽ bị gỡ bỏ khỏi tháng ${state.month.monthValue}/${state.month.year}.",
            confirmLabel = "Xác nhận xóa",
            dismissLabel = "Hủy",
            onConfirm = {
                viewModel.delete(budget)
                pendingDelete = null
            },
        )
    }
}
