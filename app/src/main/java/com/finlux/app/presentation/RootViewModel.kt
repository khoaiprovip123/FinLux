package com.finlux.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
) : ViewModel() {
    val theme = themeRepository.preference.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ThemePreference.SYSTEM,
    )

    val uiPreferences = uiPreferencesRepository.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiPreferences(),
    )

    fun setTheme(preference: ThemePreference) {
        viewModelScope.launch { themeRepository.setPreference(preference) }
    }

    fun setUiPreferences(preferences: UiPreferences) {
        viewModelScope.launch { uiPreferencesRepository.setPreferences(preferences) }
    }
}
