package com.finlux.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.data.local.reminder.ReminderSyncObserver
import com.finlux.app.domain.model.AppUiStyle
import com.finlux.app.domain.model.ThemePreference
import com.finlux.app.domain.model.UiPreferences
import com.finlux.app.domain.repository.ThemePreferenceRepository
import com.finlux.app.domain.repository.UiPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    private val themeRepository: ThemePreferenceRepository,
    private val uiPreferencesRepository: UiPreferencesRepository,
    private val reminderSyncObserver: ReminderSyncObserver,
) : ViewModel() {
    init {
        reminderSyncObserver.startObserving(viewModelScope)
    }

    val theme = themeRepository.preference.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ThemePreference.SYSTEM,
    )

    val uiStyle = themeRepository.uiStyle.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppUiStyle.CLASSIC_LIQUID,
    )

    val uiPreferences = uiPreferencesRepository.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiPreferences(),
    )

    fun setTheme(preference: ThemePreference) {
        viewModelScope.launch { themeRepository.setPreference(preference) }
    }

    fun setUiStyle(style: AppUiStyle) {
        viewModelScope.launch { themeRepository.setUiStyle(style) }
    }

    fun setUiPreferences(preferences: UiPreferences) {
        viewModelScope.launch { uiPreferencesRepository.setPreferences(preferences) }
    }
}
