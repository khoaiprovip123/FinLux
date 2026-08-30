package com.finlux.app.presentation.transaction.prism

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
import androidx.compose.material3.SnackbarHost
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
                    color = if (viewMode == TransactionViewMode.CALENDAR) tokens.primary.copy(alpha = 0.14f) else (if (tokens.isDark) tokens.surfaceSoft else Color.White),
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
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbar) },
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
@Composable
private fun PrismTransactionSearchBarWithFilter(
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
        color = if (tokens.isDark) tokens.surfaceSoft else Color.White,
        border = BorderStroke(1.dp, if (tokens.isDark) tokens.border else Color(0xFFE2E8F0).copy(alpha = 0.75f)),
        shadowElevation = if (tokens.isDark) 0.dp else 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
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
                tint = if (query.isNotBlank()) tokens.primary else Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp),
            )

            androidx.compose.foundation.text.BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.5.sp,
                    color = tokens.onSurface,
                    fontWeight = FontWeight.Medium,
                ),
                singleLine = true,
                cursorBrush = androidx.compose.ui.graphics.SolidColor(tokens.primary),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Tìm kiếm giao dịch...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                color = Color(0xFF94A3B8),
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
                        tint = tokens.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            // Nút Lọc tích hợp bên trong thanh tìm kiếm
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
                    tint = if (activeFilterCount > 0) tokens.primary else Color(0xFF64748B),
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
                                    color = Color.White,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. Chuẩn hóa 4 mục lọc nhanh [ Tất cả | Thu | Chi | Chuyển ] kèm Icon và layout cân đối
 */
@Composable
private fun PrismQuickSegmentedTabsWithIcons(
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
        TabItem(TransactionFilter.INCOME, "Thu", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF10B981)),
        TabItem(TransactionFilter.EXPENSE, "Chi", Icons.AutoMirrored.Filled.TrendingDown, Color(0xFFEF4444)),
        TabItem(TransactionFilter.TRANSFER, "Chuyển", Icons.Default.SwapHoriz, Color(0xFF3B82F6)),
    )

    val tabShape = RoundedCornerShape(14.dp)
    val activeGradient = Brush.horizontalGradient(
        listOf(
            Color(0xFF00C6FF),
            Color(0xFF0072FF),
            Color(0xFF9B51E0),
        ),
    )

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
                                .shadow(elevation = 4.dp, shape = tabShape, spotColor = Color(0xFF0072FF).copy(alpha = 0.35f))
                                .background(brush = activeGradient, shape = tabShape)
                        } else {
                            Modifier
                                .shadow(elevation = if (tokens.isDark) 0.dp else 1.dp, shape = tabShape, spotColor = Color.Black.copy(alpha = 0.05f))
                                .background(color = if (tokens.isDark) tokens.surfaceSoft else Color.White, shape = tabShape)
                                .border(width = 1.dp, color = if (tokens.isDark) tokens.border else Color(0xFFE2E8F0).copy(alpha = 0.8f), shape = tabShape)
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
                        tint = if (isSelected) Color.White else item.accentColor,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(4.5.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        ),
                        color = if (isSelected) Color.White else tokens.onSurface,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/**
 * 6. Thẻ giao dịch theo 3 cột chuẩn: Số tiền To & Rõ ràng
 */
@Composable
private fun PrismExplorerTransactionCard(
    transaction: FinanceTransaction,
    category: Category?,
    wallet: Wallet?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    zone: java.time.ZoneId,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    val isIncome = transaction.type == TransactionType.INCOME
    val isTransfer = transaction.type == TransactionType.TRANSFER_OUT || transaction.type == TransactionType.TRANSFER_IN

    // Amount Sign & Color
    val amountPrefix = if (isIncome) "+" else "−"
    val amountColor = when {
        isIncome -> Color(0xFF059669) // Deep Emerald Green
        isTransfer -> Color(0xFF2563EB) // Blue
        else -> Color(0xFFE11D48) // Vibrant Crimson Red
    }

    val displayAmount = amountPrefix + formatVndAmount(transaction.amount.value).replace("đ", "₫")

    // Title & Subtitle text
    val mainTitle = transaction.note.ifBlank {
        category?.name ?: if (isTransfer) "Chuyển tiền" else "Giao dịch"
    }

    val subTitle = when {
        isTransfer -> "Chuyển tiền"
        transaction.note.isNotBlank() && category != null -> category.name
        else -> if (isIncome) "Thu nhập" else "Chi tiêu"
    }

    // Time text (HH:mm)
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val timeText = remember(transaction.date, zone) {
        transaction.date.atZone(zone).format(timeFormatter)
    }

    // Icon Container Styling
    val iconColorHex = category?.colorHex
    val parsedColor = remember(iconColorHex) {
        if (!iconColorHex.isNullOrBlank()) colorFromHex(iconColorHex) else null
    }

    val iconBackgroundBrush = remember(transaction.type, parsedColor) {
        when {
            isIncome -> Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
            isTransfer -> Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF6366F1)))
            parsedColor != null -> Brush.linearGradient(listOf(parsedColor, parsedColor.copy(alpha = 0.85f)))
            else -> Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626)))
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (tokens.isDark) Color(0xFF1E1E34).copy(alpha = 0.75f) else Color.White,
        border = BorderStroke(
            1.dp,
            if (tokens.isDark) tokens.border else Color(0xFFE2E8F0).copy(alpha = 0.7f),
        ),
        shadowElevation = if (tokens.isDark) 0.dp else 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // 1. CỘT TRÁI: Container Icon 50dp
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(iconBackgroundBrush),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isTransfer) Icons.Default.SwapHoriz else categoryIcon(category?.icon.orEmpty()),
                    contentDescription = category?.name,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }

            // 2. CỘT GIỮA: Tên giao dịch + Danh mục
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = mainTitle,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = tokens.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = subTitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    color = tokens.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // 3. CỘT PHẢI: Số tiền To & Rõ nét + Giờ (Canh phải tuyệt đối)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = displayAmount,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 17.sp,
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
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

/**
 * 7. Thẻ bảo mật cuối danh sách
 */
@Composable
private fun PrismSecurityFooterCard(
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (tokens.isDark) tokens.surfaceSoft.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.8f),
        border = BorderStroke(1.dp, if (tokens.isDark) tokens.border else Color(0xFFE2E8F0).copy(alpha = 0.6f)),
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
                    color = tokens.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/**
 * Empty States (Không có giao dịch / Không tìm thấy / Theo bộ lọc)
 */
@Composable
private fun PrismTransactionEmptyState(
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
            color = tokens.onSurfaceVariant,
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
