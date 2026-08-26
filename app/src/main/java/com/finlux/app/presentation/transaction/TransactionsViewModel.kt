package com.finlux.app.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.DeleteTransactionUseCase
import com.finlux.app.core.common.AppResult
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
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

enum class TransactionFilter(val label: String, val heading: String) {
    ALL("Tất cả", "Dòng tiền"),
    INCOME("Thu nhập", "Thu nhập"),
    EXPENSE("Chi tiêu", "Chi tiêu"),
}

enum class TimePeriodFilter(val label: String) {
    ALL("Tất cả thời gian"),
    THIS_WEEK("Tuần này"),
    THIS_MONTH("Tháng này"),
    LAST_MONTH("Tháng trước"),
    THIS_YEAR("Năm nay"),
}

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    repository: TransactionRepository,
    categoryRepository: CategoryRepository,
    walletRepository: WalletRepository,
    private val deleteTransaction: DeleteTransactionUseCase,
) : ViewModel() {
    val filter = MutableStateFlow(TransactionFilter.ALL)
    val periodFilter = MutableStateFlow(TimePeriodFilter.ALL)
    val walletFilter = MutableStateFlow<String?>(null)
    val categoryFilter = MutableStateFlow<String?>(null)

    val transactions = combine(
        repository.observeRecent(500),
        filter,
        periodFilter,
        walletFilter,
        categoryFilter,
    ) { items, selectedType, selectedPeriod, selectedWallet, selectedCat ->
        val zone = ZoneId.systemDefault()
        val localNow = LocalDate.now(zone)

        val startInstant: Instant? = when (selectedPeriod) {
            TimePeriodFilter.ALL -> null
            TimePeriodFilter.THIS_WEEK -> {
                val startOfWeek = localNow.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                startOfWeek.atStartOfDay(zone).toInstant()
            }
            TimePeriodFilter.THIS_MONTH -> {
                val startOfMonth = localNow.with(TemporalAdjusters.firstDayOfMonth())
                startOfMonth.atStartOfDay(zone).toInstant()
            }
            TimePeriodFilter.LAST_MONTH -> {
                val startOfLastMonth = localNow.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth())
                startOfLastMonth.atStartOfDay(zone).toInstant()
            }
            TimePeriodFilter.THIS_YEAR -> {
                val startOfYear = localNow.with(TemporalAdjusters.firstDayOfYear())
                startOfYear.atStartOfDay(zone).toInstant()
            }
        }

        val endInstant: Instant? = when (selectedPeriod) {
            TimePeriodFilter.LAST_MONTH -> {
                val endOfLastMonth = localNow.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth())
                endOfLastMonth.atTime(LocalTime.MAX).atZone(zone).toInstant()
            }
            else -> null
        }

        items.filter { item ->
            val matchesType = when (selectedType) {
                TransactionFilter.ALL -> true
                TransactionFilter.INCOME -> item.type == TransactionType.INCOME
                TransactionFilter.EXPENSE -> item.type == TransactionType.EXPENSE
            }
            val matchesPeriod = (startInstant == null || item.date >= startInstant) && (endInstant == null || item.date <= endInstant)
            val matchesWallet = selectedWallet == null || item.walletId == selectedWallet || item.relatedWalletId == selectedWallet
            val matchesCategory = selectedCat == null || item.categoryId == selectedCat

            matchesType && matchesPeriod && matchesWallet && matchesCategory
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

    val activeFilterCount = combine(periodFilter, walletFilter, categoryFilter) { p, w, c ->
        var count = 0
        if (p != TimePeriodFilter.ALL) count++
        if (w != null) count++
        if (c != null) count++
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

    private val mutableMessages = MutableSharedFlow<String>()
    val messages = mutableMessages.asSharedFlow()

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

    fun resetFilters() {
        periodFilter.value = TimePeriodFilter.ALL
        walletFilter.value = null
        categoryFilter.value = null
    }

    fun delete(transaction: FinanceTransaction) = viewModelScope.launch {
        when (val result = deleteTransaction(transaction)) {
            is AppResult.Success -> mutableMessages.emit("Đã xóa giao dịch và hoàn lại số dư ví")
            is AppResult.Error -> mutableMessages.emit(result.message)
        }
    }
}
