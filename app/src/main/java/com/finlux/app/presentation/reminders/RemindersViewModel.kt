package com.finlux.app.presentation.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.ReminderRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.DeleteReminderUseCase
import com.finlux.app.domain.usecase.SaveReminderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RemindersUiState(
    val reminders: List<Reminder> = emptyList(),
    val categories: List<Category> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val busy: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class RemindersViewModel @Inject constructor(
    repository: ReminderRepository,
    categoryRepository: CategoryRepository,
    walletRepository: WalletRepository,
    private val saveReminder: SaveReminderUseCase,
    private val deleteReminder: DeleteReminderUseCase,
) : ViewModel() {
    private val action = MutableStateFlow(false to null as String?)
    val state = combine(
        repository.observeReminders(), categoryRepository.observeCategories(), walletRepository.observeWallets(), action,
    ) { reminders, categories, wallets, actionState ->
        RemindersUiState(
            reminders = reminders.sortedBy(Reminder::nextTriggerDate),
            categories = categories.filter { it.type == CategoryType.EXPENSE },
            wallets = wallets,
            busy = actionState.first,
            message = actionState.second,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RemindersUiState())

    fun save(reminder: Reminder, onSaved: () -> Unit) = viewModelScope.launch {
        action.value = true to null
        when (val result = saveReminder(reminder)) {
            is AppResult.Success -> { action.value = false to "Đã lưu nhắc nhở"; onSaved() }
            is AppResult.Error -> action.value = false to result.message
        }
    }

    fun toggle(reminder: Reminder) = save(reminder.copy(enabled = !reminder.enabled)) {}

    fun delete(reminder: Reminder) = viewModelScope.launch {
        when (val result = deleteReminder(reminder)) {
            is AppResult.Success -> action.value = false to "Đã xóa nhắc nhở"
            is AppResult.Error -> action.value = false to result.message
        }
    }

    fun consumeMessage() { action.value = action.value.first to null }
}
