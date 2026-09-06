package com.finlux.app.presentation.transaction.prism

import com.finlux.app.core.designsystem.theme.FinluxPalette

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.filled.Tune
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
import com.finlux.app.core.designsystem.component.FinluxSnackbarHost
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.presentation.transaction.DeleteTransactionConfirmDialog
import com.finlux.app.presentation.transaction.TimePeriodFilter
import com.finlux.app.presentation.transaction.TransactionActionDialog
import com.finlux.app.presentation.transaction.TransactionDetailSheet
import com.finlux.app.presentation.transaction.TransactionFilter
import com.finlux.app.presentation.transaction.TransactionFilterBottomSheet
import com.finlux.app.presentation.transaction.TransactionViewMode
import com.finlux.app.presentation.transaction.TransactionsViewModel
import com.finlux.app.presentation.transaction.prism.PrismSpendingCalendarView
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
            // 1. Header: Tiêu đề "Giao dịch" lớn, rõ ràng + Nút chuyển chế độ Xem Lịch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Giao dịch",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                    ),
                    color = tokens.onSurface,
                )

                // Nút chuyển chế độ Lịch / Danh sách
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (viewMode == TransactionViewMode.CALENDAR) tokens.primary.copy(alpha = 0.14f) else (if (tokens.isDark) tokens.surfaceSoft else FinluxPalette.White),
                    border = BorderStroke(
                        1.dp,
                        if (viewMode == TransactionViewMode.CALENDAR) tokens.primary.copy(alpha = 0.35f) else tokens.border.copy(alpha = 0.6f),
                    ),
                    shadowElevation = if (tokens.isDark) 0.dp else 2.dp,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = {
                                viewModel.setViewMode(
                                    if (viewMode == TransactionViewMode.LIST) TransactionViewMode.CALENDAR else TransactionViewMode.LIST
                                )
                            },
                        ),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (viewMode == TransactionViewMode.LIST) Icons.Default.Tune else Icons.AutoMirrored.Filled.FormatListBulleted,
                            contentDescription = "Tùy chọn hiển thị",
                            tint = if (viewMode == TransactionViewMode.CALENDAR) tokens.primary else tokens.onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        },
        containerColor = FinluxPalette.Transparent,
        snackbarHost = { FinluxSnackbarHost(snackbar, hasBottomBar = isRootTab) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // 2. Thanh tìm kiếm toàn màn hình tích hợp nút lọc bên tay phải
            PrismTransactionSearchBarWithFilter(
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                onClear = { viewModel.setSearchQuery("") },
                activeFilterCount = activeFilterCount,
                onOpenFilter = { showFilterSheet = true },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )

            // 3. Chuẩn hóa 4 mục lọc nhanh [ Tất cả | Thu | Chi | Chuyển ] kèm Icon mới và chống tràn chữ
            PrismQuickSegmentedTabsWithIcons(
                selectedFilter = filter,
                onFilterSelect = { viewModel.filter.value = it },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )

            if (viewMode == TransactionViewMode.CALENDAR) {
                // Calendar Heatmap View
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 6.dp,
                        bottom = if (isRootTab) 96.dp else 24.dp,
                    ),
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
                val today = remember(financeZone) { LocalDate.now(financeZone) }
                val yesterday = remember(today) { today.minusDays(1) }

                // 4. Danh sách giao dịch nhóm theo ngày (Transaction Explorer)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 4.dp,
                        bottom = if (isRootTab) 100.dp else 24.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (transactions.isEmpty()) {
                        item {
                            PrismTransactionEmptyState(
                                searchQuery = searchQuery,
                                filter = filter,
                                onAddClick = onAdd,
                            )
                        }
                    } else {
                        groupedTransactions.forEach { (date, txList) ->
                            // 5. Header nhóm ngày rõ ràng (Hôm nay, 30/08 | Hôm qua, 29/08 | 28/08/2026)
                            item(key = "header_$date") {
                                val headerTitle = when (date) {
                                    today -> "Hôm nay, ${date.format(DateTimeFormatter.ofPattern("dd/MM"))}"
                                    yesterday -> "Hôm qua, ${date.format(DateTimeFormatter.ofPattern("dd/MM"))}"
                                    else -> date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                }

                                Text(
                                    text = headerTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 15.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = tokens.onSurface,
                                    modifier = Modifier.padding(start = 2.dp, top = 14.dp, bottom = 4.dp),
                                )
                            }

                            // 6. Từng dòng giao dịch với Số tiền To & Rõ ràng
                            items(
                                items = txList,
                                key = { it.id },
                            ) { transaction ->
                                val category = transaction.categoryId?.let { categories[it] }
                                val wallet = wallets[transaction.walletId]

                                PrismExplorerTransactionCard(
                                    transaction = transaction,
                                    category = category,
                                    wallet = wallet,
                                    onClick = { viewingTransaction = transaction },
                                    onLongClick = { actionTransaction = transaction },
                                    zone = financeZone,
                                )
                            }
                        }

                        // 7. Thẻ bảo mật chân trang (Dữ liệu giao dịch được mã hóa và bảo mật tuyệt đối)
                        item(key = "security_footer_card") {
                            Spacer(Modifier.height(8.dp))
                            PrismSecurityFooterCard()
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

/**
 * 2. Thanh tìm kiếm toàn màn hình: Đưa icon Lọc vào bên tay phải kèm Badge
 */
