package com.finlux.app.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.collapseInternalTransferPairs
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.DeleteTransactionUseCase
import com.finlux.app.domain.usecase.AddTransactionUseCase
import com.finlux.app.domain.usecase.FinancialPeriodResolver
import com.finlux.app.core.common.AppResult
import com.finlux.app.core.time.FinanceTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.temporal.ChronoUnit
import java.text.Normalizer
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

enum class TransactionFilter(val label: String, val heading: String) {
    ALL("Tất cả", "Dòng tiền"),
    INCOME("Thu nhập", "Thu nhập"),
    EXPENSE("Chi tiêu", "Chi tiêu"),
}

enum class TransactionViewMode {
    LIST,
    CALENDAR,
}

enum class InsightIconType {
    POSITIVE,
    INFO,
    ATTENTION,
}

data class SmartInsightUiModel(
    val title: String,
    val description: String,
    val iconType: InsightIconType = InsightIconType.INFO,
)

data class DayFinancialSummary(
    val date: LocalDate,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val transactionCount: Int = 0,
) {
    val netAmount: Long get() = totalIncome - totalExpense
}

enum class TimePeriodFilter(val label: String) {
    ALL("Tất cả"),
    TODAY("Hôm nay"),
    CURRENT_PERIOD("Kỳ này"),
    PREVIOUS_PERIOD("Kỳ trước"),
    THIS_WEEK("Tuần này"),
    THIS_MONTH("Tháng này"),
    LAST_MONTH("Tháng trước"),
    LAST_30_DAYS("30 ngày"),
    LAST_3_MONTHS("3 tháng"),
    LAST_6_MONTHS("6 tháng"),
    THIS_YEAR("Năm nay"),
}

private data class TransactionLookup(
    val categories: Map<String, Category>,
    val wallets: Map<String, Wallet>,
)

private data class AmountRange(val minimum: Long?, val maximum: Long?)

data class TransactionUiEvent(
    val message: String,
    val undoTransaction: FinanceTransaction? = null,
)

private data class TransactionSearchContext(
    val query: String,
    val amountRange: AmountRange,
    val lookup: TransactionLookup,
    val salaryConfig: com.finlux.app.domain.model.SalaryCycleConfig,
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    repository: TransactionRepository,
    categoryRepository: CategoryRepository,
    walletRepository: WalletRepository,
    salaryCycleRepository: SalaryCycleRepository,
    private val financialPeriodResolver: FinancialPeriodResolver,
    private val deleteTransaction: DeleteTransactionUseCase,
    private val addTransaction: AddTransactionUseCase,
) : ViewModel() {
    val filter = MutableStateFlow(TransactionFilter.ALL)
    val periodFilter = MutableStateFlow(TimePeriodFilter.ALL)
    val walletFilter = MutableStateFlow<String?>(null)
    val categoryFilter = MutableStateFlow<String?>(null)
    val searchQuery = MutableStateFlow("")
    val minimumAmount = MutableStateFlow<Long?>(null)
    val maximumAmount = MutableStateFlow<Long?>(null)
    val viewMode = MutableStateFlow(TransactionViewMode.LIST)
    val selectedCalendarDate = MutableStateFlow<LocalDate?>(null)

    private val lookups = combine(
        categoryRepository.observeCategories(),
        walletRepository.observeWallets(),
    ) { categoryList, walletList ->
        TransactionLookup(categoryList.associateBy(Category::id), walletList.associateBy(Wallet::id))
    }

    private val amountRange = combine(minimumAmount, maximumAmount, ::AmountRange)

    val transactions = combine(
        repository.observeRecent(500),
        filter,
        periodFilter,
        combine(walletFilter, categoryFilter) { wallet, category -> wallet to category },
        combine(searchQuery, amountRange, lookups, salaryCycleRepository.observeConfig(), ::TransactionSearchContext),
    ) { items, selectedType, selectedPeriod, entityFilters, searchContext ->
        val selectedWallet = entityFilters.first
        val selectedCat = entityFilters.second
        val query = searchContext.query
        val range = searchContext.amountRange
        val lookup = searchContext.lookup
        val salaryConfig = searchContext.salaryConfig
        val zone = FinanceTime.zoneOf(salaryConfig.financeTimeZone)
        val localNow = LocalDate.now(zone)
        val now = Instant.now()

        val periodBounds: Pair<Instant?, Instant?> = when (selectedPeriod) {
            TimePeriodFilter.ALL -> null to null
            TimePeriodFilter.TODAY -> {
                val startOfDay = localNow.atStartOfDay(zone).toInstant()
                val endOfDay = localNow.plusDays(1).atStartOfDay(zone).toInstant()
                startOfDay to endOfDay
            }
            TimePeriodFilter.CURRENT_PERIOD -> financialPeriodResolver.resolveCurrentPeriod(salaryConfig, now).let { it.start to it.endExclusive }
            TimePeriodFilter.PREVIOUS_PERIOD -> financialPeriodResolver.resolvePreviousPeriod(salaryConfig, now).let { it.start to it.endExclusive }
            TimePeriodFilter.THIS_WEEK -> {
                val startOfWeek = localNow.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                startOfWeek.atStartOfDay(zone).toInstant() to null
            }
            TimePeriodFilter.THIS_MONTH -> {
                val startOfMonth = localNow.with(TemporalAdjusters.firstDayOfMonth())
                startOfMonth.atStartOfDay(zone).toInstant() to null
            }
            TimePeriodFilter.LAST_MONTH -> {
                val startOfLastMonth = localNow.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth())
                val endOfLastMonth = localNow.with(TemporalAdjusters.firstDayOfMonth())
                startOfLastMonth.atStartOfDay(zone).toInstant() to endOfLastMonth.atStartOfDay(zone).toInstant()
            }
            TimePeriodFilter.LAST_30_DAYS -> now.minus(30, ChronoUnit.DAYS) to null
            TimePeriodFilter.LAST_3_MONTHS -> localNow.minusMonths(3).atStartOfDay(zone).toInstant() to null
            TimePeriodFilter.LAST_6_MONTHS -> localNow.minusMonths(6).atStartOfDay(zone).toInstant() to null
            TimePeriodFilter.THIS_YEAR -> {
                val startOfYear = localNow.with(TemporalAdjusters.firstDayOfYear())
                startOfYear.atStartOfDay(zone).toInstant() to null
            }
        }
        val startInstant = periodBounds.first
        val endExclusive = periodBounds.second
        val normalizedQuery = normalizeSearchText(query)

        items.collapseInternalTransferPairs().filter { item ->
            val matchesType = when (selectedType) {
                TransactionFilter.ALL -> true
                TransactionFilter.INCOME -> item.type == TransactionType.INCOME
                TransactionFilter.EXPENSE -> item.type == TransactionType.EXPENSE
            }
            val matchesPeriod = (startInstant == null || item.date >= startInstant) && (endExclusive == null || item.date < endExclusive)
            val matchesWallet = selectedWallet == null || item.walletId == selectedWallet || item.relatedWalletId == selectedWallet
            val matchesCategory = selectedCat == null || item.categoryId == selectedCat
            val matchesAmount = (range.minimum == null || item.amount.value >= range.minimum) &&
                (range.maximum == null || item.amount.value <= range.maximum)
            val searchableText = buildString {
                append(item.note)
                append(' ')
                append(item.amount.value)
                append(' ')
                append(item.categoryId?.let(lookup.categories::get)?.name.orEmpty())
                append(' ')
                append(lookup.wallets[item.walletId]?.name.orEmpty())
                append(' ')
                append(item.relatedWalletId?.let(lookup.wallets::get)?.name.orEmpty())
            }
            val matchesSearch = normalizedQuery.isBlank() || normalizeSearchText(searchableText).contains(normalizedQuery)

            matchesType && matchesPeriod && matchesWallet && matchesCategory && matchesAmount && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalIncome = transactions.map { list ->
        list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount.value }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val totalExpense = transactions.map { list ->
        list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount.value }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val netCashFlow = transactions.map { list ->
        val inc = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount.value }
        val exp = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount.value }
        inc - exp
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val activeFilterCount = combine(periodFilter, walletFilter, categoryFilter, amountRange, searchQuery) { p, w, c, amount, query ->
        var count = 0
        if (p != TimePeriodFilter.ALL) count++
        if (w != null) count++
        if (c != null) count++
        if (amount.minimum != null || amount.maximum != null) count++
        if (query.isNotBlank()) count++
        count
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val categories = categoryRepository.observeCategories()
        .map { list -> list.associateBy { it.id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val allCategoriesList = categoryRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val wallets = walletRepository.observeWallets()
        .map { list -> list.associateBy { it.id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val allWalletsList = walletRepository.observeWallets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val financeZone: StateFlow<ZoneId> = salaryCycleRepository.observeConfig()
        .map { FinanceTime.zoneOf(it.financeTimeZone) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ZoneId.systemDefault())

    val dailySummaries: StateFlow<Map<LocalDate, DayFinancialSummary>> = combine(
        transactions,
        financeZone,
    ) { txList, zone ->
        txList.groupBy { it.date.atZone(zone).toLocalDate() }
            .mapValues { (date, items) ->
                val income = items.filter { it.type == TransactionType.INCOME }.sumOf { it.amount.value }
                val expense = items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount.value }
                DayFinancialSummary(
                    date = date,
                    totalIncome = income,
                    totalExpense = expense,
                    transactionCount = items.size,
                )
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val smartInsight: StateFlow<SmartInsightUiModel> = combine(
        transactions,
        financeZone,
        netCashFlow,
        totalIncome,
        totalExpense,
    ) { txList, zone, netFlow, income, expense ->
        val today = LocalDate.now(zone)
        val todayTxs = txList.filter { it.date.atZone(zone).toLocalDate() == today }
        val todayExpense = todayTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount.value }
        val todayIncome = todayTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount.value }

        when {
            txList.isEmpty() -> SmartInsightUiModel(
                title = "Sẵn sàng ghi nhận",
                description = "Chưa có giao dịch nào phù hợp với bộ lọc hiện tại.",
                iconType = InsightIconType.INFO,
            )
            todayTxs.isNotEmpty() && todayExpense > 0L -> SmartInsightUiModel(
                title = "Hôm nay đã ghi nhận ${todayTxs.size} giao dịch",
                description = "Chi tiêu hôm nay là ${formatInsightAmount(todayExpense)} đ. Tiếp tục duy trì thói quen ghi chép nhé! 🌟",
                iconType = InsightIconType.POSITIVE,
            )
            todayTxs.isNotEmpty() && todayIncome > 0L -> SmartInsightUiModel(
                title = "Thu nhập hôm nay +${formatInsightAmount(todayIncome)} đ",
                description = "Một ngày đầy năng lượng tích cực với nguồn thu mới! 🚀",
                iconType = InsightIconType.POSITIVE,
            )
            netFlow > 0L && income > 0L -> {
                val savingsPercent = ((netFlow.toDouble() / income.toDouble()) * 100).toInt().coerceIn(1, 100)
                SmartInsightUiModel(
                    title = "Dòng tiền thặng dư +${formatInsightAmount(netFlow)} đ",
                    description = "Bạn đang giữ lại được $savingsPercent% tổng thu nhập. Quản lý tài chính rất xuất sắc! 👏",
                    iconType = InsightIconType.POSITIVE,
                )
            }
            netFlow < 0L -> SmartInsightUiModel(
                title = "Chi tiêu đang vượt thu nhập ${formatInsightAmount(-netFlow)} đ",
                description = "Hãy xem lại các danh mục chi lớn để cân đối lại dòng tiền kỳ này nhé.",
                iconType = InsightIconType.ATTENTION,
            )
            else -> SmartInsightUiModel(
                title = "Nhịp điệu chi tiêu ổn định",
                description = "Đã ghi nhận ${txList.size} giao dịch trong khoảng thời gian đã chọn.",
                iconType = InsightIconType.INFO,
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SmartInsightUiModel("FinLux đồng hành", "Theo dõi dòng tiền thông minh mỗi ngày", InsightIconType.INFO),
    )

    private val mutableMessages = MutableSharedFlow<TransactionUiEvent>()
    val messages = mutableMessages.asSharedFlow()

    fun setViewMode(mode: TransactionViewMode) {
        viewMode.value = mode
    }

    fun setSelectedCalendarDate(date: LocalDate?) {
        selectedCalendarDate.value = date
    }

    fun toggleQuickCategory(categoryId: String) {
        categoryFilter.value = if (categoryFilter.value == categoryId) null else categoryId
    }

    fun toggleQuickWallet(walletId: String) {
        walletFilter.value = if (walletFilter.value == walletId) null else walletId
    }

    fun setFilter(newFilter: TransactionFilter) {
        filter.value = newFilter
    }

    fun setPeriod(period: TimePeriodFilter) {
        periodFilter.value = period
    }

    fun setWalletFilter(walletId: String?) {
        walletFilter.value = walletId
    }

    fun setCategoryFilter(categoryId: String?) {
        categoryFilter.value = categoryId
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query.take(80)
    }

    fun setAmountRange(minimum: Long?, maximum: Long?) {
        val safeMinimum = minimum?.coerceAtLeast(0L)
        val safeMaximum = maximum?.coerceAtLeast(0L)
        if (safeMinimum != null && safeMaximum != null && safeMinimum > safeMaximum) {
            minimumAmount.value = safeMaximum
            maximumAmount.value = safeMinimum
        } else {
            minimumAmount.value = safeMinimum
            maximumAmount.value = safeMaximum
        }
    }

    fun resetFilters() {
        periodFilter.value = TimePeriodFilter.ALL
        walletFilter.value = null
        categoryFilter.value = null
        searchQuery.value = ""
        minimumAmount.value = null
        maximumAmount.value = null
        selectedCalendarDate.value = null
    }

    fun delete(transaction: FinanceTransaction) = viewModelScope.launch {
        when (val result = deleteTransaction(transaction)) {
            is AppResult.Success -> mutableMessages.emit(
                TransactionUiEvent(
                    message = "Đã xóa giao dịch và hoàn lại số dư ví",
                    undoTransaction = transaction,
                ),
            )
            is AppResult.Error -> mutableMessages.emit(TransactionUiEvent(result.message))
        }
    }

    fun restore(transaction: FinanceTransaction) = viewModelScope.launch {
        when (val result = addTransaction(transaction)) {
            is AppResult.Success -> mutableMessages.emit(TransactionUiEvent("Đã khôi phục giao dịch"))
            is AppResult.Error -> mutableMessages.emit(TransactionUiEvent("Không thể hoàn tác: ${result.message}"))
        }
    }
}

private fun normalizeSearchText(value: String): String = Normalizer
    .normalize(value.lowercase(), Normalizer.Form.NFD)
    .replace("\\p{Mn}+".toRegex(), "")
    .replace('đ', 'd')

private fun formatInsightAmount(amount: Long): String {
    val formatter = java.text.DecimalFormat("#,###")
    return formatter.format(amount).replace(',', '.')
}

