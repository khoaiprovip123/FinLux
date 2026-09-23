package com.finlux.app.presentation.transaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinluxStyleBackdrop
import com.finlux.app.core.designsystem.GradientHeroCard
import com.finlux.app.core.designsystem.LocalAppUiStyle
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.component.FinluxAdaptiveCard
import com.finlux.app.core.designsystem.component.FinluxAdaptiveTopBar
import com.finlux.app.core.designsystem.component.FinluxSnackbarHost
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.component.getTransactionAmountPrefix
import com.finlux.app.core.designsystem.component.getTransactionIconBrush
import com.finlux.app.core.designsystem.component.getTransactionSemanticColor
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.time.FinanceTime
import com.finlux.app.domain.model.AppUiStyle
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.presentation.home.toVnd
import java.time.LocalDate
import java.time.ZoneId

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
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
    val activeFilterCount = viewModel.activeFilterCount.collectAsStateWithLifecycle().value
    val financeZone = viewModel.financeZone.collectAsStateWithLifecycle().value
    val viewMode = viewModel.viewMode.collectAsStateWithLifecycle().value
    val selectedCalendarDate = viewModel.selectedCalendarDate.collectAsStateWithLifecycle().value
    val dailySummaries = viewModel.dailySummaries.collectAsStateWithLifecycle().value

    val snackbar = remember { SnackbarHostState() }
    val tokens = LocalFinluxTokens.current
    val uiStyle = LocalAppUiStyle.current

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (uiStyle == AppUiStyle.PRISM) Modifier.background(tokens.background)
                else Modifier
            ),
    ) {
        if (uiStyle != AppUiStyle.PRISM) {
            FinluxStyleBackdrop(Modifier.fillMaxSize())
        }

        Scaffold(
            topBar = {
                FinluxAdaptiveTopBar(
                    title = {
                        Text(
                            text = "Giao dịch",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                            color = tokens.onSurface,
                        )
                    },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Quay lại",
                                    tint = tokens.onSurface,
                                )
                            }
                        }
                    },
                    actions = {
                        // Nút chuyển chế độ Lịch / Danh sách
                        IconButton(
                            onClick = {
                                viewModel.setViewMode(
                                    if (viewMode == TransactionViewMode.LIST) TransactionViewMode.CALENDAR else TransactionViewMode.LIST,
                                )
                            },
                        ) {
                            Icon(
                                imageVector = if (viewMode == TransactionViewMode.LIST) Icons.Default.CalendarMonth else Icons.AutoMirrored.Filled.FormatListBulleted,
                                contentDescription = "Chuyển chế độ xem",
                                tint = if (viewMode == TransactionViewMode.CALENDAR) tokens.primary else tokens.onSurface,
                            )
                        }

                        // Nút mở bộ lọc
                        IconButton(onClick = { showFilterSheet = true }) {
                            Box {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = "Bộ lọc",
                                    tint = if (activeFilterCount > 0) tokens.primary else tokens.onSurface,
                                )
                                if (activeFilterCount > 0) {
                                    Surface(
                                        shape = CircleShape,
                                        color = tokens.primary,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(12.dp),
                                    ) {}
                                }
                            }
                        }
                    },
                )
            },
            containerColor = Color.Transparent,
            snackbarHost = { FinluxSnackbarHost(snackbar, hasBottomBar = isRootTab) },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                // Thanh tìm kiếm
                TransactionSearchBarWithFilter(
                    query = searchQuery,
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    onClear = { viewModel.setSearchQuery("") },
                    activeFilterCount = activeFilterCount,
                    onOpenFilter = { showFilterSheet = true },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )

                // 4 mục lọc nhanh [ Tất cả | Thu | Chi | Chuyển ]
                QuickSegmentedTabsWithIcons(
                    selectedFilter = filter,
                    onFilterSelect = { viewModel.filter.value = it },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )

                if (viewMode == TransactionViewMode.CALENDAR) {
                    // Chế độ Lịch Heatmap
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 6.dp,
                            bottom = if (isRootTab) 96.dp else 24.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            SpendingCalendarView(
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
                    // Chế độ Danh sách nhóm theo ngày
                    val groupedTransactions = remember(transactions, financeZone) {
                        transactions.groupBy { tx ->
                            tx.date.atZone(financeZone).toLocalDate()
                        }
                    }
                    val today = remember(financeZone) { LocalDate.now(financeZone) }
                    val yesterday = remember(today) { today.minusDays(1) }

                    val totalIncome = remember(transactions) {
                        transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount.value }
                    }
                    val totalExpense = remember(transactions) {
                        transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount.value }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 4.dp,
                            bottom = if (isRootTab) 100.dp else 24.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // Thẻ tóm tắt Gradient Hero Card cho phong cách Classic/Modern
                        if (uiStyle != AppUiStyle.PRISM && transactions.isNotEmpty()) {
                            item {
                                GradientHeroCard(Modifier.fillMaxWidth()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = when (filter) {
                                                TransactionFilter.ALL -> "Dòng tiền ròng"
                                                TransactionFilter.INCOME -> "Tổng thu nhập"
                                                TransactionFilter.EXPENSE -> "Tổng chi tiêu"
                                                TransactionFilter.TRANSFER -> "Tổng chuyển tiền"
                                            },
                                            color = tokens.onHeroMuted,
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                        val net = totalIncome - totalExpense
                                        Text(
                                            text = if (filter == TransactionFilter.ALL) {
                                                if (net >= 0) "+${net.toVnd()}" else "-${(-net).toVnd()}"
                                            } else if (filter == TransactionFilter.INCOME) "+${totalIncome.toVnd()}"
                                            else "-${totalExpense.toVnd()}",
                                            color = tokens.onHero,
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Text(
                                            text = "Thu: +${totalIncome.toVnd()}  •  Chi: -${totalExpense.toVnd()}",
                                            color = tokens.onHero.copy(alpha = 0.9f),
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                            }
                        }

                        if (transactions.isEmpty()) {
                            item {
                                TransactionEmptyState(
                                    searchQuery = searchQuery,
                                    filter = filter,
                                    onAddClick = onAdd,
                                )
                            }
                        } else {
                            groupedTransactions.forEach { (date, txList) ->
                                // Tiêu đề nhóm ngày
                                item(key = "header_$date") {
                                    val headerTitle = when (date) {
                                        today -> "Hôm nay, ${FinanceTime.formatDateMonth(date)}"
                                        yesterday -> "Hôm qua, ${FinanceTime.formatDateMonth(date)}"
                                        else -> FinanceTime.formatDate(date)
                                    }
                                    val dayIncome = txList.filter { it.type == TransactionType.INCOME }.sumOf { it.amount.value }
                                    val dayExpense = txList.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount.value }
                                    val dayNet = dayIncome - dayExpense

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 2.dp, end = 2.dp, top = 14.dp, bottom = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Text(
                                                text = headerTitle,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                ),
                                                color = tokens.onSurface,
                                            )
                                            Text(
                                                text = "(${txList.size})",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = tokens.textSecondary,
                                            )
                                        }

                                        if (dayExpense > 0L || dayIncome > 0L) {
                                            Text(
                                                text = if (dayNet >= 0) "+${dayNet.toVnd()}" else "-${(-dayNet).toVnd()}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (dayNet >= 0) FinluxColors.IncomeGreen else FinluxColors.ExpenseRed,
                                            )
                                        }
                                    }
                                }

                                // Từng thẻ giao dịch
                                items(
                                    items = txList,
                                    key = { it.id },
                                ) { transaction ->
                                    val category = transaction.categoryId?.let { categories[it] }
                                    val wallet = wallets[transaction.walletId]

                                    AdaptiveTransactionCard(
                                        transaction = transaction,
                                        category = category,
                                        wallet = wallet,
                                        onClick = { viewingTransaction = transaction },
                                        onLongClick = { actionTransaction = transaction },
                                        zone = financeZone,
                                    )
                                }
                            }

                            // Thẻ bảo mật chân trang
                            item(key = "security_footer_card") {
                                Spacer(Modifier.height(8.dp))
                                SecurityFooterCard()
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Bottom Sheet
    viewingTransaction?.let { tx ->
        val category = tx.categoryId?.let { categories[it] }
        val wallet = tx.walletId.let { wallets[it] }
        val relatedWallet = tx.relatedWalletId?.let { wallets[it] }

        TransactionDetailSheet(
            transaction = tx,
            category = category,
            wallet = wallet,
            relatedWallet = relatedWallet,
            onDismiss = { viewingTransaction = null },
            onEdit = {
                viewingTransaction = null
                if (tx.type != TransactionType.TRANSFER_OUT && tx.type != TransactionType.TRANSFER_IN) {
                    onEditTransaction?.invoke(tx)
                }
            },
            onDelete = {
                viewingTransaction = null
                pendingDelete = tx
            },
        )
    }

    // Action Menu Dialog (Edit / Delete)
    actionTransaction?.let { tx ->
        val category = tx.categoryId?.let { categories[it] }
        val wallet = tx.walletId.let { wallets[it] }
        val relatedWallet = tx.relatedWalletId?.let { wallets[it] }

        TransactionActionDialog(
            transaction = tx,
            category = category,
            wallet = wallet,
            relatedWallet = relatedWallet,
            onDismiss = { actionTransaction = null },
            onEdit = {
                actionTransaction = null
                if (tx.type != TransactionType.TRANSFER_OUT && tx.type != TransactionType.TRANSFER_IN) {
                    onEditTransaction?.invoke(tx)
                }
            },
            onDelete = {
                actionTransaction = null
                pendingDelete = tx
            },
        )
    }

    // Filter Bottom Sheet
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

    // Delete Confirmation Dialog
    pendingDelete?.let { tx ->
        DeleteTransactionConfirmDialog(
            transaction = tx,
            relatedWallet = tx.relatedWalletId?.let { wallets[it] },
            onDismiss = { pendingDelete = null },
            onConfirm = {
                viewModel.delete(tx)
                pendingDelete = null
            },
        )
    }
}

@Composable
private fun TransactionSearchBarWithFilter(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    activeFilterCount: Int,
    onOpenFilter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val focusManager = LocalFocusManager.current

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (tokens.isDark) tokens.surfaceSoft else tokens.surface,
        border = BorderStroke(1.dp, tokens.border),
        shadowElevation = if (tokens.isDark) 0.dp else 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Tìm kiếm",
                tint = if (query.isNotBlank()) tokens.primary else tokens.textSecondary,
                modifier = Modifier.size(20.dp),
            )

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.5.sp,
                    color = tokens.onSurface,
                    fontWeight = FontWeight.Medium,
                ),
                singleLine = true,
                cursorBrush = SolidColor(tokens.primary),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Tìm kiếm giao dịch...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                color = tokens.textSecondary.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Normal,
                            ),
                        )
                    }
                    innerTextField()
                },
            )

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onClear()
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Xóa tìm kiếm",
                        tint = tokens.textSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = onOpenFilter,
                    )
                    .padding(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Bộ lọc",
                    tint = if (activeFilterCount > 0) tokens.primary else tokens.textSecondary,
                    modifier = Modifier.size(22.dp),
                )

                if (activeFilterCount > 0) {
                    Surface(
                        shape = CircleShape,
                        color = tokens.primary,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(14.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = activeFilterCount.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tokens.onHero,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickSegmentedTabsWithIcons(
    selectedFilter: TransactionFilter,
    onFilterSelect: (TransactionFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    data class TabItem(
        val filter: TransactionFilter,
        val label: String,
        val icon: ImageVector,
        val accentColor: Color,
    )

    val filters = listOf(
        TabItem(TransactionFilter.ALL, "Tất cả", Icons.Default.GridView, tokens.primary),
        TabItem(TransactionFilter.INCOME, "Thu", Icons.AutoMirrored.Filled.TrendingUp, FinluxColors.IncomeGreen),
        TabItem(TransactionFilter.EXPENSE, "Chi", Icons.AutoMirrored.Filled.TrendingDown, FinluxColors.ExpenseRed),
        TabItem(TransactionFilter.TRANSFER, "Chuyển", Icons.Default.SwapHoriz, FinluxColors.TransferBlue),
    )

    val tabShape = RoundedCornerShape(14.dp)
    val activeGradient = Brush.horizontalGradient(tokens.heroGradient)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        filters.forEach { item ->
            val isSelected = selectedFilter == item.filter

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .then(
                        if (isSelected) {
                            Modifier
                                .shadow(elevation = 4.dp, shape = tabShape, spotColor = tokens.primary.copy(alpha = 0.35f))
                                .background(brush = activeGradient, shape = tabShape)
                        } else {
                            Modifier
                                .shadow(elevation = if (tokens.isDark) 0.dp else 1.dp, shape = tabShape, spotColor = tokens.onSurface.copy(alpha = 0.05f))
                                .background(color = if (tokens.isDark) tokens.surfaceSoft else tokens.surface, shape = tabShape)
                                .border(width = 1.dp, color = tokens.border, shape = tabShape)
                        }
                    )
                    .clip(tabShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = { onFilterSelect(item.filter) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) tokens.onHero else item.accentColor,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(4.5.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        ),
                        color = if (isSelected) tokens.onHero else tokens.onSurface,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun AdaptiveTransactionCard(
    transaction: FinanceTransaction,
    category: Category?,
    wallet: Wallet?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    zone: ZoneId,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val isIncome = transaction.type == TransactionType.INCOME
    val isTransfer = transaction.type == TransactionType.TRANSFER_OUT || transaction.type == TransactionType.TRANSFER_IN

    val amountPrefix = getTransactionAmountPrefix(transaction.type)
    val amountColor = getTransactionSemanticColor(transaction.type)
    val displayAmount = amountPrefix + formatVndAmount(transaction.amount.value).replace("đ", "₫")

    val mainTitle = transaction.note.ifBlank {
        category?.name ?: if (isTransfer) "Chuyển tiền" else "Giao dịch"
    }

    val subTitle = when {
        isTransfer -> "Chuyển tiền"
        transaction.note.isNotBlank() && category != null -> category.name
        else -> if (isIncome) "Thu nhập" else "Chi tiêu"
    }

    val timeText = remember(transaction.date, zone) {
        FinanceTime.formatTime(transaction.date, zone)
    }

    val iconBackgroundBrush = remember(transaction.type, category?.colorHex) {
        getTransactionIconBrush(transaction.type, category?.colorHex)
    }

    FinluxAdaptiveCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Icon tròn 46dp
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconBackgroundBrush),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isTransfer) Icons.Default.SwapHoriz else categoryIcon(category?.icon.orEmpty()),
                    contentDescription = category?.name,
                    tint = tokens.onHero,
                    modifier = Modifier.size(22.dp),
                )
            }

            // Tên giao dịch + Danh mục / Ví
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = mainTitle,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = tokens.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = subTitle + (wallet?.let { " • ${it.name}" } ?: ""),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    color = tokens.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Số tiền + Giờ
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = displayAmount,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.4).sp,
                    ),
                    color = amountColor,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                )

                Text(
                    text = timeText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    color = tokens.textSecondary,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

@Composable
private fun SecurityFooterCard(
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (tokens.isDark) tokens.surfaceSoft.copy(alpha = 0.5f) else tokens.surface.copy(alpha = 0.8f),
        border = BorderStroke(1.dp, tokens.border),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f),
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = tokens.primary.copy(alpha = 0.14f),
                    modifier = Modifier.size(30.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = tokens.primary,
                            modifier = Modifier.size(17.dp),
                        )
                    }
                }

                Text(
                    text = "Dữ liệu giao dịch được mã hóa và bảo mật tuyệt đối.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = tokens.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = tokens.textSecondary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun TransactionEmptyState(
    searchQuery: String,
    filter: TransactionFilter,
    onAddClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    val title = when {
        searchQuery.isNotBlank() -> "Không tìm thấy kết quả"
        filter != TransactionFilter.ALL -> "Không có giao dịch ${filter.label.lowercase()}"
        else -> "Chưa có giao dịch nào"
    }

    val description = when {
        searchQuery.isNotBlank() -> "Không tìm thấy giao dịch nào khớp với \"$searchQuery\"."
        filter != TransactionFilter.ALL -> "Bạn chưa ghi nhận khoản ${filter.label.lowercase()} nào trong danh mục này."
        else -> "Bắt đầu ghi lại các khoản thu chi đầu tiên của bạn để quản lý tài chính dễ dàng hơn."
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = tokens.primary.copy(alpha = 0.10f),
            modifier = Modifier.size(72.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = tokens.primary,
                    modifier = Modifier.size(32.dp),
                )
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = tokens.onSurface,
            textAlign = TextAlign.Center,
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.5.sp,
                lineHeight = 19.sp,
            ),
            color = tokens.textSecondary,
            textAlign = TextAlign.Center,
        )

        if (onAddClick != null && searchQuery.isBlank()) {
            Spacer(Modifier.height(6.dp))
            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tokens.primary),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Thêm giao dịch",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    ),
                )
            }
        }
    }
}
