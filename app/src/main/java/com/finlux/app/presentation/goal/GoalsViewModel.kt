package com.finlux.app.presentation.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinancialGoal
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.repository.GoalRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.DeleteGoalUseCase
import com.finlux.app.domain.usecase.DepositToGoalUseCase
import com.finlux.app.domain.usecase.SaveGoalUseCase
import com.finlux.app.domain.usecase.WithdrawFromGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class GoalEditorState(
    val name: String = "",
    val targetInput: String = "",
    val monthlyInput: String = "",
    val deadline: Instant = Instant.now().plusSeconds(180L * 24 * 60 * 60),
    val category: String = "Khác",
    val imageUri: String? = null,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
)

enum class GoalTransactionMode { DEPOSIT, WITHDRAW }

data class GoalTransactionSheetState(
    val isOpen: Boolean = false,
    val goal: FinancialGoal? = null,
    val mode: GoalTransactionMode = GoalTransactionMode.DEPOSIT,
    val selectedWalletId: String = "",
    val amountInput: String = "",
    val note: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    repository: GoalRepository,
    walletRepository: WalletRepository,
    private val saveGoal: SaveGoalUseCase,
    private val deleteGoal: DeleteGoalUseCase,
    private val depositToGoalUseCase: DepositToGoalUseCase,
    private val withdrawFromGoalUseCase: WithdrawFromGoalUseCase,
) : ViewModel() {

    val goals = repository.observeGoals().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val wallets = walletRepository.observeWallets().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val mutableEditor = MutableStateFlow(GoalEditorState())
    val editor = mutableEditor.asStateFlow()

    private val mutableTransactionSheet = MutableStateFlow(GoalTransactionSheetState())
    val transactionSheet = mutableTransactionSheet.asStateFlow()

    fun setName(value: String) = mutableEditor.update { it.copy(name = value.take(80), error = null) }
    fun setTarget(value: String) = mutableEditor.update { it.copy(targetInput = value.filter(Char::isDigit).take(15), error = null) }
    fun setMonthly(value: String) = mutableEditor.update { it.copy(monthlyInput = value.filter(Char::isDigit).take(15), error = null) }
    fun setDeadline(value: Instant) = mutableEditor.update { it.copy(deadline = value, error = null) }
    fun setCategory(value: String) = mutableEditor.update { it.copy(category = value, error = null) }
    fun setImage(uri: String?) = mutableEditor.update { it.copy(imageUri = uri) }

    fun save() = viewModelScope.launch {
        val value = editor.value
        mutableEditor.update { it.copy(saving = true, error = null) }
        val goal = FinancialGoal(
            name = value.name,
            targetAmount = Money(value.targetInput.toLongOrNull() ?: 0L),
            deadline = value.deadline,
            category = value.category,
            monthlyContribution = Money(value.monthlyInput.toLongOrNull() ?: 0L),
            imageUri = value.imageUri,
        )
        when (val result = saveGoal(goal)) {
            is AppResult.Success -> mutableEditor.update { it.copy(saving = false, saved = true) }
            is AppResult.Error -> mutableEditor.update { it.copy(saving = false, error = result.message) }
        }
    }

    fun consumeSaved() { mutableEditor.value = GoalEditorState() }
    fun delete(goal: FinancialGoal) = viewModelScope.launch { deleteGoal(goal) }

    // ── NẠP / RÚT TIỀN CHO MỤC TIÊU ───────────────────────────────────────────

    fun openDeposit(goal: FinancialGoal, defaultWalletId: String? = null) {
        val selectedWallet = defaultWalletId
            ?: wallets.value.firstOrNull { it.isDefault }?.id
            ?: wallets.value.firstOrNull()?.id
            ?: ""
        mutableTransactionSheet.value = GoalTransactionSheetState(
            isOpen = true,
            goal = goal,
            mode = GoalTransactionMode.DEPOSIT,
            selectedWalletId = selectedWallet,
            amountInput = "",
            note = "",
        )
    }

    fun openWithdraw(goal: FinancialGoal, defaultWalletId: String? = null) {
        val selectedWallet = defaultWalletId
            ?: wallets.value.firstOrNull { it.isDefault }?.id
            ?: wallets.value.firstOrNull()?.id
            ?: ""
        mutableTransactionSheet.value = GoalTransactionSheetState(
            isOpen = true,
            goal = goal,
            mode = GoalTransactionMode.WITHDRAW,
            selectedWalletId = selectedWallet,
            amountInput = "",
            note = "",
        )
    }

    fun closeTransactionSheet() {
        mutableTransactionSheet.value = GoalTransactionSheetState(isOpen = false)
    }

    fun setTransactionWallet(walletId: String) {
        mutableTransactionSheet.update { it.copy(selectedWalletId = walletId, error = null) }
    }

    fun setTransactionAmount(amountStr: String) {
        val digits = amountStr.filter(Char::isDigit).take(15)
        mutableTransactionSheet.update { it.copy(amountInput = digits, error = null) }
    }

    fun setTransactionNote(note: String) {
        mutableTransactionSheet.update { it.copy(note = note.take(120)) }
    }

    fun submitGoalTransaction() = viewModelScope.launch {
        val state = transactionSheet.value
        val goal = state.goal ?: return@launch
        val amount = state.amountInput.toLongOrNull() ?: 0L
        if (amount <= 0L) {
            mutableTransactionSheet.update { it.copy(error = "Số tiền phải lớn hơn 0") }
            return@launch
        }
        if (state.selectedWalletId.isBlank()) {
            mutableTransactionSheet.update { it.copy(error = "Vui lòng chọn ví thực hiện") }
            return@launch
        }

        mutableTransactionSheet.update { it.copy(isSubmitting = true, error = null) }

        val result = if (state.mode == GoalTransactionMode.DEPOSIT) {
            depositToGoalUseCase(
                goalId = goal.id,
                walletId = state.selectedWalletId,
                amount = amount,
                note = state.note,
            )
        } else {
            withdrawFromGoalUseCase(
                goalId = goal.id,
                walletId = state.selectedWalletId,
                amount = amount,
                note = state.note,
            )
        }

        when (result) {
            is AppResult.Success -> {
                mutableTransactionSheet.update { it.copy(isSubmitting = false, isSuccess = true) }
                closeTransactionSheet()
            }
            is AppResult.Error -> {
                mutableTransactionSheet.update { it.copy(isSubmitting = false, error = result.message) }
            }
        }
    }
}
