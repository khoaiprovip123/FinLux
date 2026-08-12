package com.finlux.app.presentation.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinancialGoal
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.repository.GoalRepository
import com.finlux.app.domain.usecase.DeleteGoalUseCase
import com.finlux.app.domain.usecase.SaveGoalUseCase
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

@HiltViewModel
class GoalsViewModel @Inject constructor(
    repository: GoalRepository,
    private val saveGoal: SaveGoalUseCase,
    private val deleteGoal: DeleteGoalUseCase,
) : ViewModel() {
    val goals = repository.observeGoals().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val mutableEditor = MutableStateFlow(GoalEditorState())
    val editor = mutableEditor.asStateFlow()

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
}
