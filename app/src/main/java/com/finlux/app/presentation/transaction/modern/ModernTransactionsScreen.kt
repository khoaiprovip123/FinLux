package com.finlux.app.presentation.transaction.modern

import com.finlux.app.presentation.transaction.*
import com.finlux.app.core.designsystem.modern.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import com.finlux.app.core.designsystem.theme.FinluxColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.ExpenseRed
import com.finlux.app.core.designsystem.FinluxTextSecondary
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.GlassTopBar
import com.finlux.app.core.designsystem.IncomeGreen
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.presentation.home.toVnd
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.finlux.app.core.navigation.Route

@Composable
fun ModernTransactionsScreen(
    onNavigate: ((String) -> Unit)? = null,
    onAdd: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    onEditTransaction: ((FinanceTransaction) -> Unit)? = null,
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    val transactions = viewModel.transactions.collectAsStateWithLifecycle().value
    val categories = viewModel.categories.collectAsStateWithLifecycle().value
    val wallets = viewModel.wallets.collectAsStateWithLifecycle().value
    val allCategories = viewModel.allCategoriesList.collectAsStateWithLifecycle().value
    val allWallets = viewModel.allWalletsList.collectAsStateWithLifecycle().value
    val filter = viewModel.filter.collectAsStateWithLifecycle().value
    val periodFilter = viewModel.periodFilter.collectAsStateWithLifecycle().value
    val selectedWalletId = viewModel.walletFilter.collectAsStateWithLifecycle().value
    val selectedCategoryId = viewModel.categoryFilter.collectAsStateWithLifecycle().value
    val totalIncome = viewModel.totalIncome.collectAsStateWithLifecycle().value
    val totalExpense = viewModel.totalExpense.collectAsStateWithLifecycle().value
    val netCashFlow = viewModel.netCashFlow.collectAsStateWithLifecycle().value
    val activeFilterCount = viewModel.activeFilterCount.collectAsStateWithLifecycle().value
    val snackbar = remember { SnackbarHostState() }

    var viewingTransaction by remember { mutableStateOf<FinanceTransaction?>(null) }
    var actionTransaction by remember { mutableStateOf<FinanceTransaction?>(null) }
    var pendingDelete by remember { mutableStateOf<FinanceTransaction?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.messages.collect { snackbar.showSnackbar(it) } }

    val isRootTab = onNavigate != null && onAdd != null

    Box(Modifier.fillMaxSize()) {
        com.finlux.app.core.designsystem.modern.FinluxStyleBackdrop(Modifier.fillMaxSize())
        Scaffold(
            topBar = {
                GlassTopBar(
                    title = { Text(if (isRootTab) "Lịch sử thu chi" else "Giao dịch", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        if (!isRootTab) {
                            IconButton(onClick = { onBack?.invoke() ?: onNavigate?.invoke(Route.Home.value) }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                            }
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showFilterSheet = true }) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Bộ lọc",
                                    tint = if (activeFilterCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            if (activeFilterCount > 0) {
                                Surface(
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 4.dp, end = 4.dp)
                                        .size(16.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = activeFilterCount.toString(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    },
                )
            },
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbar) },
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    item {
                        val periodSuffix = if (periodFilter == TimePeriodFilter.ALL) "" else " (${periodFilter.label})"
                        val displayAmount = when (filter) {
                            TransactionFilter.ALL -> (if (netCashFlow > 0) "+" else "") + netCashFlow.toVnd()
                            TransactionFilter.INCOME -> "+" + totalIncome.toVnd()
                            TransactionFilter.EXPENSE -> "-" + totalExpense.toVnd()
                        }
                        val heading = when (filter) {
                            TransactionFilter.ALL -> "Dòng tiền ròng$periodSuffix"
                            TransactionFilter.INCOME -> "Tổng thu nhập$periodSuffix"
                            TransactionFilter.EXPENSE -> "Tổng chi tiêu$periodSuffix"
                        }
                        GlassCard(
                            Modifier.fillMaxWidth(),
                            mode = com.finlux.app.core.designsystem.modern.LiquidGlassMode.CLEAR,
                            tint = MaterialTheme.colorScheme.primary,
                            padding = PaddingValues(18.dp),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(heading, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(displayAmount, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                if (filter == TransactionFilter.ALL) {
                                    Text("Thu: +${totalIncome.toVnd()}  •  Chi: -${totalExpense.toVnd()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    Text("${transactions.size} giao dịch", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    items(transactions, key = { it.id }) { transaction ->
                        val isTransfer = transaction.type == TransactionType.TRANSFER_OUT || transaction.type == TransactionType.TRANSFER_IN
                        val cat = categories[transaction.categoryId]
                        val relWallet = wallets[transaction.relatedWalletId]
                        val curWallet = wallets[transaction.walletId]
                        val rowAccent = when (transaction.type) {
                            TransactionType.INCOME -> cat?.let { colorFromHex(it.colorHex) } ?: IncomeGreen
                            TransactionType.EXPENSE -> cat?.let { colorFromHex(it.colorHex) } ?: ExpenseRed
                            TransactionType.TRANSFER_OUT, TransactionType.TRANSFER_IN -> FinluxColors.TransferBlue
                        }
                        val rowIcon = when (transaction.type) {
                            TransactionType.INCOME -> cat?.let { categoryIcon(it.icon) } ?: Icons.Default.Payments
                            TransactionType.EXPENSE -> cat?.let { categoryIcon(it.icon) } ?: Icons.Default.Payments
                            TransactionType.TRANSFER_OUT, TransactionType.TRANSFER_IN -> Icons.Default.SwapHoriz
                        }
                        val title = transaction.note.ifBlank {
                            when (transaction.type) {
                                TransactionType.INCOME -> cat?.name ?: "Thu nhập"
                                TransactionType.EXPENSE -> cat?.name ?: "Chi tiêu"
                                TransactionType.TRANSFER_OUT -> if (relWallet != null) "Chuyển đến ${relWallet.name}" else "Chuyển tiền đi"
                                TransactionType.TRANSFER_IN -> if (relWallet != null) "Nhận từ ${relWallet.name}" else "Nhận tiền chuyển"
                            }
                        }
                        val amountPrefix = when (transaction.type) {
                            TransactionType.INCOME, TransactionType.TRANSFER_IN -> "+"
                            TransactionType.EXPENSE, TransactionType.TRANSFER_OUT -> "-"
                        }

                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            mode = com.finlux.app.core.designsystem.modern.LiquidGlassMode.REGULAR,
                            tint = rowAccent,
                            padding = PaddingValues(14.dp),
                            onClick = { viewingTransaction = transaction },
                            onLongClick = { actionTransaction = transaction },
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(12.dp), color = rowAccent.copy(alpha = .12f)) {
                                    Icon(
                                        rowIcon,
                                        null,
                                        Modifier.padding(9.dp).size(20.dp),
                                        tint = rowAccent,
                                    )
                                }
                                Column(Modifier.weight(1f).padding(horizontal = 11.dp)) {
                                    Text(title, fontWeight = FontWeight.Bold)
                                    Text(
                                        DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm").format(transaction.date.atZone(ZoneId.systemDefault())),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = FinluxTextSecondary,
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(amountPrefix + transaction.amount.value.toVnd(), color = rowAccent, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (!isTransfer) {
                                            IconButton(onClick = { onEditTransaction?.invoke(transaction) }) {
                                                Icon(Icons.Default.Edit, "Sửa giao dịch", tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                        IconButton(onClick = { pendingDelete = transaction }) {
                                            Icon(Icons.Default.DeleteOutline, "Xóa giao dịch", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        TransactionFilterBottomSheet(
            currentPeriod = periodFilter,
            selectedWalletId = selectedWalletId,
            selectedCategoryId = selectedCategoryId,
            wallets = allWallets,
            categories = allCategories,
            onApply = { period, walletId, categoryId ->
                viewModel.setPeriod(period)
                viewModel.setWalletFilter(walletId)
                viewModel.setCategoryFilter(categoryId)
            },
            onReset = { viewModel.resetFilters() },
            onDismiss = { showFilterSheet = false },
        )
    }

    viewingTransaction?.let { tx ->
        TransactionDetailSheet(
            transaction = tx,
            category = categories[tx.categoryId],
            wallet = wallets[tx.walletId],
            relatedWallet = wallets[tx.relatedWalletId],
            onDismiss = { viewingTransaction = null },
            onEdit = {
                if (it.type != TransactionType.TRANSFER_OUT && it.type != TransactionType.TRANSFER_IN) {
                    onEditTransaction?.invoke(it)
                }
            },
            onDelete = { pendingDelete = it },
        )
    }

    actionTransaction?.let { tx ->
        TransactionActionDialog(
            transaction = tx,
            category = categories[tx.categoryId],
            wallet = wallets[tx.walletId],
            relatedWallet = wallets[tx.relatedWalletId],
            onDismiss = { actionTransaction = null },
            onViewDetails = { viewingTransaction = it },
            onEdit = {
                if (it.type != TransactionType.TRANSFER_OUT && it.type != TransactionType.TRANSFER_IN) {
                    onEditTransaction?.invoke(it)
                }
            },
            onDelete = { pendingDelete = it },
        )
    }

    pendingDelete?.let { transaction ->
        DeleteTransactionConfirmDialog(
            transaction = transaction,
            relatedWallet = wallets[transaction.relatedWalletId],
            onDismiss = { pendingDelete = null },
            onConfirm = {
                viewModel.delete(it)
                pendingDelete = null
            },
        )
    }
}

private val TransactionFilter.label: String
    get() = when (this) {
        TransactionFilter.ALL -> "Tất cả"
        TransactionFilter.INCOME -> "Thu"
        TransactionFilter.EXPENSE -> "Chi"
    }
