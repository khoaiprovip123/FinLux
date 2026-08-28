package com.finlux.app.presentation.transaction.classic

import com.finlux.app.presentation.transaction.*

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.ExpenseRed
import com.finlux.app.core.designsystem.FinluxTextSecondary
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.GlassTopBar
import com.finlux.app.core.designsystem.GradientHeroCard
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
fun ClassicTransactionsScreen(
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
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TransactionFilter.entries.forEach { item ->
                    FilterChip(
                        selected = filter == item,
                        onClick = { viewModel.setFilter(item) },
                        label = { Text(item.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            }

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
                    }
                    val heading = when (filter) {
                        TransactionFilter.ALL -> "Dòng tiền ròng$periodSuffix"
                        TransactionFilter.INCOME -> "Tổng thu nhập$periodSuffix"
                        TransactionFilter.EXPENSE -> "Tổng chi tiêu$periodSuffix"
                    }
                    GradientHeroCard(Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(heading, color = Color.White.copy(alpha = .8f))
                            Text(displayAmount, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            if (filter == TransactionFilter.ALL) {
                                Text("Thu: +${totalIncome.toVnd()}  •  Chi: -${totalExpense.toVnd()}", color = Color.White.copy(alpha = .9f), style = MaterialTheme.typography.bodySmall)
                            } else {
                                Text("${transactions.size} giao dịch", color = Color.White.copy(alpha = .78f))
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
                                    color = Color.White,
                                )
                                Text(
                                    text = "(${txList.size})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f),
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
                        val title = when (transaction.type) {
                            TransactionType.TRANSFER_OUT -> "Chuyển tới ${relWallet?.name ?: "Ví khác"}"
                            TransactionType.TRANSFER_IN -> "Nhận từ ${relWallet?.name ?: "Ví khác"}"
                            else -> if (transaction.note.isNotBlank()) transaction.note else cat?.name ?: "Giao dịch"
                        }
                        val subtitle = buildString {
                            if (isTransfer) {
                                append(curWallet?.name ?: "Ví")
                            } else {
                                append(cat?.name ?: "Chưa phân loại")
                                curWallet?.let { append(" • ${it.name}") }
                            }
                            append(" • ")
                            append(transaction.date.atZone(financeZone).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                        }

                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { viewingTransaction = transaction },
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = rowAccent.copy(alpha = 0.14f),
                                        modifier = Modifier.size(42.dp),
                                    ) {
                                        Icon(
                                            imageVector = rowIcon,
                                            contentDescription = null,
                                            tint = rowAccent,
                                            modifier = Modifier.padding(10.dp),
                                        )
                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = FinluxTextSecondary)
                                    }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    val prefix = when (transaction.type) {
                                        TransactionType.INCOME -> "+"
                                        TransactionType.EXPENSE -> "-"
                                        TransactionType.TRANSFER_OUT -> "-"
                                        TransactionType.TRANSFER_IN -> "+"
                                    }
                                    Text(
                                        "$prefix${transaction.amount.value.toVnd()}",
                                        color = rowAccent,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    IconButton(onClick = { if (isTransfer) viewingTransaction = transaction else actionTransaction = transaction }, modifier = Modifier.size(28.dp)) {
                                        Icon(if (isTransfer) Icons.Default.Info else Icons.Default.Edit, contentDescription = if (isTransfer) "Chi tiết" else "Tùy chọn", modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { pendingDelete = transaction }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa", modifier = Modifier.size(16.dp))
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

private val TransactionFilter.label: String
    get() = when (this) {
        TransactionFilter.ALL -> "Tất cả"
        TransactionFilter.INCOME -> "Thu"
        TransactionFilter.EXPENSE -> "Chi"
    }
