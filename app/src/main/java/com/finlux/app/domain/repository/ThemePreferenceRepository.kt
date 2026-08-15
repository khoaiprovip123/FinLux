package com.finlux.app.domain.repository

import com.finlux.app.domain.model.AppUiStyle
import com.finlux.app.domain.model.ThemePreference
import com.finlux.app.domain.model.UiPreferences
import kotlinx.coroutines.flow.Flow

interface ThemePreferenceRepository {
    val preference: Flow<ThemePreference>
    suspend fun setPreference(preference: ThemePreference)
    val uiStyle: Flow<AppUiStyle>
    suspend fun setUiStyle(uiStyle: AppUiStyle)
}

interface UiPreferencesRepository {
    val preferences: Flow<UiPreferences>
    suspend fun setPreferences(preferences: UiPreferences)
}
