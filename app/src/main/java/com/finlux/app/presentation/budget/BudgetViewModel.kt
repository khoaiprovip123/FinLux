package com.finlux.app.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.usecase.BudgetStatus
import com.finlux.app.domain.usecase.GetBudgetStatusUseCase
import com.finlux.app.domain.usecase.SaveBudgetUseCase
import com.finlux.app.domain.usecase.DeleteBudgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

import com.finlux.app.domain.model.FinancialPeriod
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.usecase.FinancialPeriodResolver
import java.time.Instant

import com.finlux.app.domain.repository.UiPreferencesRepository
import com.finlux.app.domain.usecase.CopyBudgetUseCase
import kotlinx.coroutines.flow.firstOrNull

data class BudgetItemUi(val budget: Budget, val category: Category?, val status: BudgetStatus)
data class BudgetUiState(
    val period: FinancialPeriod? = null,
    val items: List<BudgetItemUi> = emptyList(),
    val categories: List<Category> = emptyList(),
    val transactions: List<FinanceTransaction> = emptyList(),
    val busy: Boolean = false,
    val message: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    categoryRepository: CategoryRepository,
    salaryCycleRepository: SalaryCycleRepository,
    private val financialPeriodResolver: FinancialPeriodResolver,
    private val transactionRepository: TransactionRepository,
    private val getBudgetStatus: GetBudgetStatusUseCase,
    private val saveBudget: SaveBudgetUseCase,
    private val deleteBudget: DeleteBudgetUseCase,
    private val copyBudgetUseCase: CopyBudgetUseCase,
) : ViewModel() {
    private val configFlow = salaryCycleRepository.observeConfig().stateIn(viewModelScope, SharingStarted.Eagerly, SalaryCycleConfig())
    private val selectedTime = MutableStateFlow(Instant.now())
    private val action = MutableStateFlow(false to null as String?)

    private val currentPeriod = combine(selectedTime, configFlow) { time, config ->
        financialPeriodResolver.resolvePeriodContaining(time, config)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val state = combine(
        currentPeriod,
        currentPeriod.flatMapLatest { p -> if (p == null) kotlinx.coroutines.flow.flowOf(emptyList()) else budgetRepository.observeBudgets(p.key) },
        categoryRepository.observeCategories(),
        currentPeriod.flatMapLatest { p ->
            if (p == null) kotlinx.coroutines.flow.flowOf(emptyList()) else transactionRepository.observePeriod(p.start, p.endExclusive)
        },
        action,
    ) { period, budgets, categories, monthTransactions, actionState ->
        val byId = categories.associateBy(Category::id)
        val byName = categories.associateBy { it.name.lowercase().trim() }

        val spentByCategoryId = monthTransactions
            .filter {
                it.type == TransactionType.EXPENSE &&
                it.dealFlowType != com.finlux.app.domain.model.DealFlowType.OUTLAY_CAPITAL &&
                it.date >= (period?.start ?: Instant.MIN) && it.date < (period?.endExclusive ?: Instant.MAX)
            }
            .groupBy { tx -> tx.categoryId?.takeIf { it.isNotBlank() } }
            .filterKeys { it != null }
            .mapKeys { it.key!! }
            .mapValues { (_, txs) -> txs.sumOf { it.amount.value } }

        val spentByCategoryName = monthTransactions
            .filter {
                it.type == TransactionType.EXPENSE &&
                it.dealFlowType != com.finlux.app.domain.model.DealFlowType.OUTLAY_CAPITAL &&
                it.categoryId != null && it.date >= (period?.start ?: Instant.MIN) && it.date < (period?.endExclusive ?: Instant.MAX)
            }
            .groupBy { tx -> tx.categoryId!!.lowercase().trim() }
            .mapValues { (_, txs) -> txs.sumOf { it.amount.value } }

        val items = budgets.map { budget ->
            val cat = byId[budget.categoryId]
            val byIdAmount = spentByCategoryId[budget.categoryId] ?: 0L
            val catNameLower = cat?.name?.lowercase()?.trim()
            val byNameAmount = if (catNameLower != null && catNameLower != budget.categoryId.lowercase().trim()) {
                spentByCategoryName[catNameLower] ?: 0L
            } else 0L
            val dynamicSpent = byIdAmount + byNameAmount
            val dynamicBudget = budget.copy(spentAmount = Money(dynamicSpent))
            BudgetItemUi(dynamicBudget, cat, getBudgetStatus(dynamicBudget))
        }.sortedByDescending { it.status.progress }

        BudgetUiState(
            period = period,
            items = items,
            categories = categories.filter { it.type == CategoryType.EXPENSE },
            transactions = monthTransactions.filter {
                it.type == TransactionType.EXPENSE &&
                it.dealFlowType != com.finlux.app.domain.model.DealFlowType.OUTLAY_CAPITAL
            },
            busy = actionState.first,
            message = actionState.second,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BudgetUiState())

    fun previousMonth() {
        val p = currentPeriod.value ?: return
        selectedTime.value = p.start.minusMillis(1)
    }
    fun nextMonth() {
        val p = currentPeriod.value ?: return
        selectedTime.value = p.endExclusive.plusMillis(1)
    }
    fun currentMonth() { selectedTime.value = Instant.now() }

    fun copyBudgetsToNextPeriod(
        overwriteExisting: Boolean = false,
        onSuccess: ((Int, FinancialPeriod) -> Unit)? = null,
    ) = viewModelScope.launch {
        action.value = true to null
        val current = currentPeriod.value
        val config = configFlow.value
        if (current == null) {
            action.value = false to "Chưa xác định kỳ chi tiêu hiện tại"
            return@launch
        }
        val nextPeriod = financialPeriodResolver.resolveNextPeriodOf(current, config)
        when (val result = copyBudgetUseCase(current, nextPeriod, overwriteExisting)) {
            is AppResult.Success -> {
                val count = result.value
                if (count > 0) {
                    action.value = false to "Đã sao chép $count ngân sách sang ${nextPeriod.displayLabel}"
                    onSuccess?.invoke(count, nextPeriod)
                } else {
                    action.value = false to "Tất cả ngân sách đã tồn tại ở ${nextPeriod.displayLabel}"
                }
            }
            is AppResult.Error -> {
                action.value = false to result.message
            }
        }
    }

    fun copyBudgetsFromPreviousPeriod(
        overwriteExisting: Boolean = false,
        onSuccess: ((Int, FinancialPeriod) -> Unit)? = null,
    ) = viewModelScope.launch {
        action.value = true to null
        val current = currentPeriod.value
        val config = configFlow.value
        if (current == null) {
            action.value = false to "Chưa xác định kỳ chi tiêu hiện tại"
            return@launch
        }
        val previousPeriod = financialPeriodResolver.resolvePreviousPeriodOf(current, config)
        when (val result = copyBudgetUseCase(previousPeriod, current, overwriteExisting)) {
            is AppResult.Success -> {
                val count = result.value
                if (count > 0) {
                    action.value = false to "Đã sao chép $count ngân sách từ ${previousPeriod.displayLabel}"
                    onSuccess?.invoke(count, current)
                } else {
                    action.value = false to "Kỳ trước (${previousPeriod.displayLabel}) không có ngân sách mới để sao chép"
                }
            }
            is AppResult.Error -> {
                action.value = false to result.message
            }
        }
    }

    fun save(categoryId: String, limit: Long, existing: Budget?, onSaved: () -> Unit) = viewModelScope.launch {
        action.value = true to null
        val period = currentPeriod.value ?: return@launch
        val currentSpent = state.value.items.find { it.budget.categoryId == categoryId }?.budget?.spentAmount?.value
            ?: existing?.spentAmount?.value ?: 0L

        val budget = existing?.copy(
            limitAmount = Money(limit),
            spentAmount = Money(currentSpent),
        ) ?: Budget(
            id = "${categoryId}_${period.key}", categoryId = categoryId,
            periodKey = period.key, periodStart = period.start, periodEndExclusive = period.endExclusive,
            periodBasis = period.basis.name,
            limitAmount = Money(limit), spentAmount = Money(currentSpent),
            notified80 = false, notified100 = false,
        )
        when (val result = saveBudget(budget)) {
            is AppResult.Success -> { action.value = false to "Đã lưu ngân sách"; onSaved() }
            is AppResult.Error -> action.value = false to result.message
        }
    }

    fun delete(budget: Budget) = viewModelScope.launch {
        when (val result = deleteBudget(budget)) {
            is AppResult.Success -> action.value = false to "Đã xóa ngân sách"
            is AppResult.Error -> action.value = false to result.message
        }
    }

    fun consumeMessage() { action.value = action.value.first to null }
}

