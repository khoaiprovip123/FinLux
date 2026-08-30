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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.text.style.TextOverflow
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

import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FormatListBulleted
import com.finlux.app.presentation.transaction.TransactionViewMode
import com.finlux.app.presentation.transaction.prism.PrismSpendingCalendarView

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
    val searchQuery = viewModel.searchQuery.collectAsStateWithLifecycle().value
    val minimumAmount = viewModel.minimumAmount.collectAsStateWithLifecycle().value
    val maximumAmount = viewModel.maximumAmount.collectAsStateWithLifecycle().value
    val totalIncome = viewModel.totalIncome.collectAsStateWithLifecycle().value
    val totalExpense = viewModel.totalExpense.collectAsStateWithLifecycle().value
    val netCashFlow = viewModel.netCashFlow.collectAsStateWithLifecycle().value
    val activeFilterCount = viewModel.activeFilterCount.collectAsStateWithLifecycle().value
    val financeZone = viewModel.financeZone.collectAsStateWithLifecycle().value
    val viewMode = viewModel.viewMode.collectAsStateWithLifecycle().value
    val selectedCalendarDate = viewModel.selectedCalendarDate.collectAsStateWithLifecycle().value
    val dailySummaries = viewModel.dailySummaries.collectAsStateWithLifecycle().value
    val snackbar = remember { SnackbarHostState() }

    var viewingTransaction by remember { mutableStateOf<FinanceTransaction?>(null) }
    var actionTransaction by remember { mutableStateOf<FinanceTransaction?>(null) }
    var pendingDelete by remember { mutableStateOf<FinanceTransaction?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { event ->
            val result = snackbar.showSnackbar(
                message = event.message,
                actionLabel = if (event.undoTransaction != null) "Hoàn tác" else null,
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed && event.undoTransaction != null) {
                viewModel.restore(event.undoTransaction)
            }
        }
    }

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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                viewModel.setViewMode(
                                    if (viewMode == TransactionViewMode.LIST) TransactionViewMode.CALENDAR else TransactionViewMode.LIST
                                )
                            }) {
                                Icon(
                                    imageVector = if (viewMode == TransactionViewMode.LIST) Icons.Default.CalendarMonth else Icons.Default.FormatListBulleted,
                                    contentDescription = "Chuyển chế độ xem",
                                    tint = if (viewMode == TransactionViewMode.CALENDAR) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                )
                            }

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
                        }
                    },
                )
            },
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbar) },
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                if (viewMode == TransactionViewMode.CALENDAR) {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            PrismSpendingCalendarView(
                                dailySummaries = dailySummaries,
                                selectedDate = selectedCalendarDate,
                                onSelectDate = { viewModel.setSelectedCalendarDate(it) },
                                transactions = transactions,
                                categories = categories,
                                wallets = wallets,
                                onTransactionClick = { tx -> viewingTransaction = tx },
                                onTransactionLongClick = { tx -> actionTransaction = tx },
                                zone = financeZone,
                            )
                        }
                    }
                } else {
                    val groupedTransactions = remember(transactions, financeZone) {
                        transactions.groupBy { tx ->
                            tx.date.atZone(financeZone).toLocalDate()
                        }
                    }
                    val today = remember(financeZone) { java.time.LocalDate.now(financeZone) }
                    val yesterday = remember(today) { today.minusDays(1) }

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
                            TransactionFilter.TRANSFER -> "${transactions.size} giao dịch"
                        }
                        val heading = when (filter) {
                            TransactionFilter.ALL -> "Dòng tiền ròng$periodSuffix"
                            TransactionFilter.INCOME -> "Tổng thu nhập$periodSuffix"
                            TransactionFilter.EXPENSE -> "Tổng chi tiêu$periodSuffix"
                            TransactionFilter.TRANSFER -> "Chuyển tiền$periodSuffix"
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

                    groupedTransactions.forEach { (date, txList) ->
                        item(key = "header_$date") {
                            val headerTitle = when (date) {
                                today -> "Hôm nay"
                                yesterday -> "Hôm qua"
                                else -> date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            }
                            val dayIncome = txList.filter { it.type == TransactionType.INCOME }.sumOf { it.amount.value }
                            val dayExpense = txList.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount.value }
                            val dayNet = dayIncome - dayExpense

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        text = headerTitle,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = com.finlux.app.core.designsystem.theme.LocalFinluxTokens.current.onSurface,
                                    )
                                    Text(
                                        text = "(${txList.size})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = com.finlux.app.core.designsystem.theme.LocalFinluxTokens.current.onSurfaceVariant,
                                    )
                                }

                                if (dayExpense > 0L || dayIncome > 0L) {
                                    Text(
                                        text = if (dayNet >= 0) "+${dayNet.toVnd()}" else "-${(-dayNet).toVnd()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dayNet >= 0) IncomeGreen else ExpenseRed,
                                    )
                                }
                            }
                        }

                        items(txList, key = { it.id }) { transaction ->
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
                                padding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                                onClick = { viewingTransaction = transaction },
                                onLongClick = { actionTransaction = transaction },
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Surface(shape = RoundedCornerShape(12.dp), color = rowAccent.copy(alpha = .12f)) {
                                        Icon(
                                            rowIcon,
                                            null,
                                            Modifier.padding(9.dp).size(20.dp),
                                            tint = rowAccent,
                                        )
                                    }
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 12.dp, end = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                    ) {
                                        Text(
                                            text = title,
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm").format(transaction.date.atZone(financeZone)),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                        )
                                    }
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                    ) {
                                        Text(
                                            text = amountPrefix + transaction.amount.value.toVnd(),
                                            color = rowAccent,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        ) {
                                            if (!isTransfer) {
                                                IconButton(
                                                    onClick = { onEditTransaction?.invoke(transaction) },
                                                    modifier = Modifier.size(28.dp),
                                                ) {
                                                    Icon(
                                                        Icons.Default.Edit,
                                                        contentDescription = "Sửa giao dịch",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp),
                                                    )
                                                }
                                            }
                                            IconButton(
                                                onClick = { pendingDelete = transaction },
                                                modifier = Modifier.size(28.dp),
                                            ) {
                                                Icon(
                                                    Icons.Default.DeleteOutline,
                                                    contentDescription = "Xóa giao dịch",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(16.dp),
                                                )
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
    }
}

    if (showFilterSheet) {
        TransactionFilterBottomSheet(
            currentPeriod = periodFilter,
            selectedWalletId = selectedWalletId,
            selectedCategoryId = selectedCategoryId,
            currentSearchQuery = searchQuery,
            currentMinimumAmount = minimumAmount,
            currentMaximumAmount = maximumAmount,
            wallets = allWallets,
            categories = allCategories,
            onApply = { period, walletId, categoryId, query, minimum, maximum ->
                viewModel.setPeriod(period)
                viewModel.setWalletFilter(walletId)
                viewModel.setCategoryFilter(categoryId)
                viewModel.setSearchQuery(query)
                viewModel.setAmountRange(minimum, maximum)
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


