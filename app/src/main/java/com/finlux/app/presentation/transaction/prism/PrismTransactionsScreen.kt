package com.finlux.app.presentation.transaction.prism

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.component.FinluxDialog
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.component.FinluxFilterChip
import com.finlux.app.core.designsystem.component.FinluxScreenHeader
import com.finlux.app.core.designsystem.component.FinluxSoftCard
import com.finlux.app.core.designsystem.component.FinluxTransactionRow
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.navigation.Route
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.presentation.components.MainBottomBar
import com.finlux.app.presentation.transaction.TransactionDetailSheet
import com.finlux.app.presentation.transaction.TransactionFilter
import com.finlux.app.presentation.transaction.TransactionsViewModel

@Composable
fun PrismTransactionsScreen(
    onNavigate: ((String) -> Unit)? = null,
    onAdd: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    onEditTransaction: ((FinanceTransaction) -> Unit)? = null,
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    val transactions = viewModel.transactions.collectAsStateWithLifecycle().value
    val categories = viewModel.categories.collectAsStateWithLifecycle().value
    val wallets = viewModel.wallets.collectAsStateWithLifecycle().value
    val filter = viewModel.filter.collectAsStateWithLifecycle().value
    val total = transactions.sumOf { it.amount.value }
    val snackbar = remember { SnackbarHostState() }
    val tokens = LocalFinluxTokens.current

    var viewingTransaction by remember { mutableStateOf<FinanceTransaction?>(null) }
    var actionTransaction by remember { mutableStateOf<FinanceTransaction?>(null) }
    var pendingDelete by remember { mutableStateOf<FinanceTransaction?>(null) }

    LaunchedEffect(Unit) { viewModel.messages.collect { snackbar.showSnackbar(it) } }

    val isRootTab = onNavigate != null && onAdd != null

    Scaffold(
        topBar = {
            FinluxScreenHeader(
                title = if (isRootTab) "Lịch sử thu chi" else "Giao dịch",
                subtitle = "${transactions.size} giao dịch",
                onBack = if (!isRootTab) onBack else null,
            )
        },
        bottomBar = {
            if (isRootTab) {
                MainBottomBar(Route.Transactions.value, onNavigate!!, onAdd!!)
            }
        },
        containerColor = tokens.background,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Filter Chips (All, Income, Expense)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.spacing.lg, vertical = tokens.spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(tokens.spacing.xs),
            ) {
                FinluxFilterChip(
                    label = "Tất cả",
                    isSelected = filter == TransactionFilter.ALL,
                    onClick = { viewModel.filter.value = TransactionFilter.ALL },
                    modifier = Modifier.weight(1f),
                )
                FinluxFilterChip(
                    label = "Thu nhập",
                    isSelected = filter == TransactionFilter.INCOME,
                    onClick = { viewModel.filter.value = TransactionFilter.INCOME },
                    modifier = Modifier.weight(1f),
                )
                FinluxFilterChip(
                    label = "Chi tiêu",
                    isSelected = filter == TransactionFilter.EXPENSE,
                    onClick = { viewModel.filter.value = TransactionFilter.EXPENSE },
                    modifier = Modifier.weight(1f),
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = tokens.spacing.lg,
                    end = tokens.spacing.lg,
                    top = tokens.spacing.sm,
                    bottom = if (isRootTab) 112.dp else 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(tokens.spacing.md),
            ) {
                // Summary Card
                item {
                    val summaryTitle = when (filter) {
                        TransactionFilter.ALL -> "Tổng giá trị giao dịch"
                        TransactionFilter.INCOME -> "Tổng thu trong kỳ"
                        TransactionFilter.EXPENSE -> "Tổng chi trong kỳ"
                    }
                    val amountColor = when (filter) {
                        TransactionFilter.ALL -> tokens.primary
                        TransactionFilter.INCOME -> FinluxColors.IncomeGreen
                        TransactionFilter.EXPENSE -> FinluxColors.ExpenseRed
                    }

                    FinluxSoftCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = amountColor.copy(alpha = 0.35f),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = summaryTitle,
                                style = FinluxTextStyles.Caption,
                                color = tokens.onSurfaceVariant,
                            )
                            Text(
                                text = formatVndAmount(total),
                                style = FinluxTextStyles.DisplayAmount.copy(
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = amountColor,
                            )
                            Text(
                                text = "${transactions.size} giao dịch hiển thị",
                                style = FinluxTextStyles.MicroLabel.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = tokens.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Transaction Items or Empty State
                if (transactions.isEmpty()) {
                    item {
                        FinluxEmptyState(
                            title = "Không có giao dịch nào",
                            description = "Không tìm thấy giao dịch phù hợp với bộ lọc hiện tại.",
                        )
                    }
                } else {
                    items(
                        items = transactions,
                        key = { it.id },
                    ) { tx ->
                        val category = tx.categoryId?.let { categories[it] }

                        FinluxTransactionRow(
                            transaction = tx,
                            category = category,
                            onClick = { viewingTransaction = tx },
                            onLongClick = { actionTransaction = tx },
                        )
                    }
                }
            }
        }
    }

    // Detail Bottom Sheet
    viewingTransaction?.let { tx ->
        val category = tx.categoryId?.let { categories[it] }
        val wallet = tx.walletId.let { wallets[it] }

        TransactionDetailSheet(
            transaction = tx,
            category = category,
            wallet = wallet,
            onDismiss = { viewingTransaction = null },
            onEdit = {
                viewingTransaction = null
                onEditTransaction?.invoke(tx)
            },
            onDelete = {
                viewingTransaction = null
                pendingDelete = tx
            },
        )
    }

    // Action Menu Dialog (Edit / Delete)
    actionTransaction?.let { tx ->
        FinluxDialog(
            onDismissRequest = { actionTransaction = null },
            title = "Tùy chọn giao dịch",
            dismissLabel = "Đóng",
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        onClick = {
                            actionTransaction = null
                            onEditTransaction?.invoke(tx)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Chỉnh sửa giao dịch", style = FinluxTextStyles.Body, color = tokens.primary)
                    }
                    TextButton(
                        onClick = {
                            actionTransaction = null
                            pendingDelete = tx
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Xóa giao dịch", style = FinluxTextStyles.Body, color = FinluxColors.ExpenseRed)
                    }
                }
            },
        )
    }

    // Delete Confirmation Dialog
    pendingDelete?.let { tx ->
        FinluxDialog(
            onDismissRequest = { pendingDelete = null },
            title = "Xóa giao dịch?",
            message = "Giao dịch trị giá ${formatVndAmount(tx.amount.value)} sẽ bị xóa vĩnh viễn và số dư ví sẽ được hoàn lại tự động.",
            confirmLabel = "Xác nhận xóa",
            dismissLabel = "Hủy",
            onConfirm = {
                viewModel.delete(tx)
                pendingDelete = null
            },
        )
    }
}
