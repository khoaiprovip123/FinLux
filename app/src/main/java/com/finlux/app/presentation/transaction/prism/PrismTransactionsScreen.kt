package com.finlux.app.presentation.transaction.prism

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.component.FinluxTransactionGroup
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.presentation.transaction.DeleteTransactionConfirmDialog
import com.finlux.app.presentation.transaction.InsightIconType
import com.finlux.app.presentation.transaction.SmartInsightUiModel
import com.finlux.app.presentation.transaction.TimePeriodFilter
import com.finlux.app.presentation.transaction.TransactionActionDialog
import com.finlux.app.presentation.transaction.TransactionDetailSheet
import com.finlux.app.presentation.transaction.TransactionFilter
import com.finlux.app.presentation.transaction.TransactionFilterBottomSheet
import com.finlux.app.presentation.transaction.TransactionViewMode
import com.finlux.app.presentation.transaction.TransactionsViewModel
import com.finlux.app.presentation.transaction.prism.PrismSpendingCalendarView
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
    val totalIncome = viewModel.totalIncome.collectAsStateWithLifecycle().value
    val totalExpense = viewModel.totalExpense.collectAsStateWithLifecycle().value
    val netCashFlow = viewModel.netCashFlow.collectAsStateWithLifecycle().value
    val activeFilterCount = viewModel.activeFilterCount.collectAsStateWithLifecycle().value
    val financeZone = viewModel.financeZone.collectAsStateWithLifecycle().value
    val viewMode = viewModel.viewMode.collectAsStateWithLifecycle().value
    val selectedCalendarDate = viewModel.selectedCalendarDate.collectAsStateWithLifecycle().value
    val dailySummaries = viewModel.dailySummaries.collectAsStateWithLifecycle().value
    val smartInsight = viewModel.smartInsight.collectAsStateWithLifecycle().value

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

    val incomeCount = remember(transactions) { transactions.count { it.type == TransactionType.INCOME } }
    val expenseCount = remember(transactions) { transactions.count { it.type == TransactionType.EXPENSE } }

    Scaffold(
        topBar = {
            // Fixed Top Header matching mockup
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (isRootTab) "Lịch sử thu chi" else "Giao dịch",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = tokens.onSurface,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // View Mode Switcher Button (List ⟷ Calendar)
                    IconButton(
                        onClick = {
                            viewModel.setViewMode(
                                if (viewMode == TransactionViewMode.LIST) TransactionViewMode.CALENDAR else TransactionViewMode.LIST
                            )
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (viewMode == TransactionViewMode.CALENDAR) tokens.primary.copy(alpha = 0.16f) else tokens.surfaceSoft,
                                CircleShape,
                            ),
                    ) {
                        Icon(
                            imageVector = if (viewMode == TransactionViewMode.LIST) Icons.Default.CalendarMonth else Icons.AutoMirrored.Filled.FormatListBulleted,
                            contentDescription = if (viewMode == TransactionViewMode.LIST) "Xem dạng lịch" else "Xem dạng danh sách",
                            tint = if (viewMode == TransactionViewMode.CALENDAR) tokens.primary else tokens.onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    // Filter Button with Badge
                    Box {
                        IconButton(
                            onClick = { showFilterSheet = true },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (activeFilterCount > 0) tokens.primary.copy(alpha = 0.15f) else tokens.surfaceSoft,
                                    CircleShape,
                                ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Bộ lọc",
                                tint = if (activeFilterCount > 0) tokens.primary else tokens.onSurface,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        if (activeFilterCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = tokens.primary,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(18.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = activeFilterCount.toString(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
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
        },
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // 1. Smart Micro-Insights Greeting Banner
            PrismSmartInsightGreeting(
                insight = smartInsight,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )

            // 2. Inline Instant Live Search Bar
            PrismInlineSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                onClear = { viewModel.setSearchQuery("") },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )

            // 3. Segmented Filter Tabs (Tất cả, Thu nhập, Chi tiêu)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PrismFilterTabPill(
                    label = "Tất cả",
                    icon = Icons.Default.GridView,
                    badgeCount = transactions.size,
                    isSelected = filter == TransactionFilter.ALL,
                    activeColor = tokens.primary,
                    onClick = { viewModel.filter.value = TransactionFilter.ALL },
                    modifier = Modifier.weight(1f),
                )

                PrismFilterTabPill(
                    label = "Thu nhập",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    badgeCount = incomeCount,
                    isSelected = filter == TransactionFilter.INCOME,
                    activeColor = FinluxColors.IncomeGreen,
                    onClick = { viewModel.filter.value = TransactionFilter.INCOME },
                    modifier = Modifier.weight(1f),
                )

                PrismFilterTabPill(
                    label = "Chi tiêu",
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    badgeCount = expenseCount,
                    isSelected = filter == TransactionFilter.EXPENSE,
                    activeColor = FinluxColors.ExpenseRed,
                    onClick = { viewModel.filter.value = TransactionFilter.EXPENSE },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(4.dp))

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
                val today = remember(financeZone) { java.time.LocalDate.now(financeZone) }
                val yesterday = remember(today) { today.minusDays(1) }

                // 4. Transaction List View
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 4.dp,
                        bottom = if (isRootTab) 96.dp else 24.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Summary Bento Banner Cards (Thiết kế hoàn toàn mới chuẩn Prism)
                    item {
                        PrismFinancialBentoBanner(
                            filter = filter,
                            periodFilter = periodFilter,
                            totalIncome = totalIncome,
                            totalExpense = totalExpense,
                            netCashFlow = netCashFlow,
                            incomeCount = incomeCount,
                            expenseCount = expenseCount,
                        )
                    }

                    // Transaction Items or Empty State
                    if (transactions.isEmpty()) {
                        item {
                            FinluxEmptyState(
                                title = "Không có giao dịch nào",
                                description = if (searchQuery.isNotBlank()) "Không tìm thấy giao dịch với từ khóa \"$searchQuery\"." else "Không tìm thấy giao dịch phù hợp với bộ lọc hiện tại.",
                            )
                        }
                    } else {
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
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = tokens.primary.copy(alpha = 0.8f),
                                            modifier = Modifier.size(13.dp),
                                        )
                                        Text(
                                            text = headerTitle,
                                            style = FinluxTextStyles.SectionTitle.copy(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            color = tokens.onSurface,
                                        )
                                        Text(
                                            text = "(${txList.size})",
                                            style = FinluxTextStyles.Caption.copy(fontSize = 12.sp),
                                            color = tokens.onSurfaceVariant,
                                        )
                                    }

                                    if (dayExpense > 0L || dayIncome > 0L) {
                                        Text(
                                            text = if (dayNet >= 0) "+${formatVndAmount(dayNet, isCompact = true)}" else "-${formatVndAmount(-dayNet, isCompact = true)}",
                                            style = FinluxTextStyles.Caption.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                            ),
                                            color = if (dayNet >= 0) FinluxColors.IncomeGreen else FinluxColors.ExpenseRed,
                                        )
                                    }
                                }
                            }

                            item(key = "group_$date") {
                                FinluxTransactionGroup(
                                    transactions = txList,
                                    categories = categories,
                                    wallets = wallets,
                                    onTransactionClick = { tx -> viewingTransaction = tx },
                                    onTransactionLongClick = { tx -> actionTransaction = tx },
                                )
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

/**
 * High-End Prism Filter Tab Pill with Badge Count and Luminous Glow
 */
@Composable
private fun PrismFilterTabPill(
    label: String,
    icon: ImageVector,
    badgeCount: Int,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else tokens.surfaceSoft,
        animationSpec = tween(220),
        label = "pill_container_anim",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else tokens.onSurface,
        animationSpec = tween(220),
        label = "pill_content_anim",
    )
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else activeColor,
        animationSpec = tween(220),
        label = "pill_icon_anim",
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = if (isSelected) {
            BorderStroke(1.2.dp, activeColor.copy(alpha = 0.8f))
        } else {
            BorderStroke(1.dp, tokens.border)
        },
        shadowElevation = if (isSelected) 4.dp else 0.dp,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                ),
                color = contentColor,
                maxLines = 1,
            )
            if (badgeCount > 0) {
                Spacer(Modifier.width(4.dp))
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) Color.White.copy(alpha = 0.25f) else tokens.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(17.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = if (isSelected) Color.White else activeColor,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Prism Financial Bento Banner — Liquid Glass Hero + Dual Sub-Cards (Finlux History Redesign 2.1)
 */
@Composable
private fun PrismFinancialBentoBanner(
    filter: TransactionFilter,
    periodFilter: TimePeriodFilter,
    totalIncome: Long,
    totalExpense: Long,
    netCashFlow: Long,
    incomeCount: Int,
    expenseCount: Int,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val periodSuffix = if (periodFilter == TimePeriodFilter.ALL) "" else " (${periodFilter.label})"

    val isSurplus = netCashFlow >= 0
    val totalVolume = (totalIncome + totalExpense).coerceAtLeast(1L)
    val incomeRatio = (totalIncome.toFloat() / totalVolume).coerceIn(0f, 1f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // 1. HERO MAIN CARD: Liquid Glass Net Cashflow Card with Prism Chromatic Rim
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = if (tokens.isDark) Color(0xFF1E1E34).copy(alpha = 0.75f) else Color.White.copy(alpha = 0.85f),
            border = BorderStroke(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        tokens.primary.copy(alpha = 0.65f),
                        Color(0xFF67E8F9).copy(alpha = 0.45f),
                        Color(0xFFA855F7).copy(alpha = 0.35f),
                        tokens.primary.copy(alpha = 0.55f),
                    ),
                ),
            ),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                (if (isSurplus) tokens.primary else Color(0xFFF43F5E)).copy(alpha = if (tokens.isDark) 0.16f else 0.08f),
                                Color.Transparent,
                            ),
                            radius = 600f,
                        ),
                    )
                    .padding(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Header Tag Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = tokens.primary.copy(alpha = 0.15f),
                                modifier = Modifier.size(32.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = tokens.primary,
                                        modifier = Modifier.size(17.dp),
                                    )
                                }
                            }

                            Text(
                                text = "DÒNG TIỀN RÒNG$periodSuffix",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                ),
                                color = tokens.onSurfaceVariant,
                            )
                        }

                        // Surplus / Deficit Badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = (if (isSurplus) FinluxColors.IncomeGreen else FinluxColors.ExpenseRed).copy(alpha = 0.14f),
                            border = BorderStroke(
                                1.dp,
                                (if (isSurplus) FinluxColors.IncomeGreen else FinluxColors.ExpenseRed).copy(alpha = 0.28f),
                            ),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = if (isSurplus) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = if (isSurplus) FinluxColors.IncomeGreen else FinluxColors.ExpenseRed,
                                    modifier = Modifier.size(13.dp),
                                )
                                Text(
                                    text = if (isSurplus) "Thặng dư" else "Bội chi",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = if (isSurplus) FinluxColors.IncomeGreen else FinluxColors.ExpenseRed,
                                )
                            }
                        }
                    }

                    // Main Big Net Amount
                    Text(
                        text = (if (netCashFlow > 0) "+" else "") + formatVndAmount(netCashFlow),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp,
                        ),
                        color = if (netCashFlow >= 0) tokens.onSurface else FinluxColors.ExpenseRed,
                    )

                    // Prism Visual Flow Ratio Bar
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.5.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(tokens.border.copy(alpha = 0.5f)),
                        ) {
                            if (totalIncome > 0L) {
                                Box(
                                    modifier = Modifier
                                        .weight(incomeRatio.coerceAtLeast(0.01f))
                                        .height(6.5.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(FinluxColors.IncomeGreen, Color(0xFF34D399))
                                            ),
                                        ),
                                )
                            }
                            if (totalExpense > 0L) {
                                Box(
                                    modifier = Modifier
                                        .weight((1f - incomeRatio).coerceAtLeast(0.01f))
                                        .height(6.5.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFFFB7185), FinluxColors.ExpenseRed)
                                            ),
                                        ),
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "Thu: ${(incomeRatio * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold),
                                color = FinluxColors.IncomeGreen,
                            )
                            Text(
                                text = "Chi: ${((1f - incomeRatio) * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold),
                                color = FinluxColors.ExpenseRed,
                            )
                        }
                    }
                }
            }
        }

        // 2. DUAL BENTO SUB-CARDS: Income & Expense Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Income Sub-Card
            PrismSubBentoCard(
                title = "Tổng thu nhập",
                amount = "+${formatVndAmount(totalIncome, isCompact = true)}",
                subtitle = "$incomeCount khoản thu",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                accentColor = FinluxColors.IncomeGreen,
                modifier = Modifier.weight(1f),
            )

            // Expense Sub-Card
            PrismSubBentoCard(
                title = "Tổng chi tiêu",
                amount = "-${formatVndAmount(totalExpense, isCompact = true)}",
                subtitle = "$expenseCount khoản chi",
                icon = Icons.AutoMirrored.Filled.TrendingDown,
                accentColor = FinluxColors.ExpenseRed,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Sub Bento Glass Card for Income & Expense Breakdown
 */
@Composable
private fun PrismSubBentoCard(
    title: String,
    amount: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (tokens.isDark) Color(0xFF1E1E34).copy(alpha = 0.65f) else Color.White.copy(alpha = 0.80f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = if (tokens.isDark) 0.25f else 0.18f)),
        shadowElevation = 3.dp,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.16f),
                    modifier = Modifier.size(28.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = tokens.onSurfaceVariant,
                    maxLines = 1,
                )
            }

            Text(
                text = amount,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = accentColor,
                maxLines = 1,
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.5.sp,
                ),
                color = tokens.onSurfaceVariant.copy(alpha = 0.75f),
            )
        }
    }
}

/**
 * Smart Micro-Insights Greeting Banner (Finlux History 2.1)
 */
@Composable
private fun PrismSmartInsightGreeting(
    insight: SmartInsightUiModel,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val (accentColor, iconVector) = when (insight.iconType) {
        InsightIconType.POSITIVE -> FinluxColors.IncomeGreen to Icons.Default.AutoAwesome
        InsightIconType.ATTENTION -> Color(0xFFF59E0B) to Icons.Default.Warning
        InsightIconType.INFO -> tokens.primary to Icons.Default.TipsAndUpdates
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = accentColor.copy(alpha = if (tokens.isDark) 0.12f else 0.08f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = if (tokens.isDark) 0.28f else 0.18f)),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.18f),
                modifier = Modifier.size(34.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(17.dp),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.5.dp)) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = tokens.onSurface,
                )
                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp,
                    ),
                    color = tokens.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Inline Instant Search Bar with Live Filtering
 */
@Composable
private fun PrismInlineSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val focusManager = LocalFocusManager.current

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = tokens.surfaceSoft,
        border = BorderStroke(1.dp, tokens.border),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Tìm kiếm",
                tint = if (query.isNotBlank()) tokens.primary else tokens.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )

            androidx.compose.foundation.text.BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    color = tokens.onSurface,
                ),
                singleLine = true,
                cursorBrush = androidx.compose.ui.graphics.SolidColor(tokens.primary),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Tìm theo ghi chú, danh mục, số tiền...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                color = tokens.onSurfaceVariant.copy(alpha = 0.65f),
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
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Xóa tìm kiếm",
                        tint = tokens.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}
