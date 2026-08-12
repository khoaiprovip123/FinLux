package com.finlux.app.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.finlux.app.domain.model.CardDensity
import com.finlux.app.domain.model.GlassIntensity
import com.finlux.app.domain.model.ThemePreference
import com.finlux.app.domain.model.UiPreferences
import com.finlux.app.domain.model.VisualStyle
import com.finlux.app.domain.repository.ThemePreferenceRepository
import com.finlux.app.domain.repository.UiPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.finluxDataStore by preferencesDataStore(name = "finlux_preferences")
private val ThemeKey = stringPreferencesKey("theme_preference")
private val GlassIntensityKey = stringPreferencesKey("glass_intensity")
private val CardDensityKey = stringPreferencesKey("card_density")
private val AnimationsKey = booleanPreferencesKey("animations_enabled")
private val VisualStyleKey = stringPreferencesKey("visual_style")

@Singleton
class DataStoreThemePreferenceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : ThemePreferenceRepository, UiPreferencesRepository {
    override val preference: Flow<ThemePreference> = context.finluxDataStore.data.map { preferences ->
        preferences[ThemeKey]
            ?.let { stored -> ThemePreference.entries.firstOrNull { it.name == stored } }
            ?: ThemePreference.SYSTEM
    }

    override suspend fun setPreference(preference: ThemePreference) {
        context.finluxDataStore.edit { it[ThemeKey] = preference.name }
    }

    override val preferences: Flow<UiPreferences> = context.finluxDataStore.data.map { stored ->
        UiPreferences(
            visualStyle = stored[VisualStyleKey]
                ?.let { value -> VisualStyle.entries.firstOrNull { it.name == value } }
                ?: VisualStyle.DYNAMIC_GRADIENT,
            glassIntensity = stored[GlassIntensityKey]
                ?.let { value -> GlassIntensity.entries.firstOrNull { it.name == value } }
                ?: GlassIntensity.BALANCED,
            cardDensity = stored[CardDensityKey]
                ?.let { value -> CardDensity.entries.firstOrNull { it.name == value } }
                ?: CardDensity.COMFORTABLE,
            animationsEnabled = stored[AnimationsKey] ?: true,
        )
    }

    override suspend fun setPreferences(preferences: UiPreferences) {
        context.finluxDataStore.edit {
            it[VisualStyleKey] = preferences.visualStyle.name
            it[GlassIntensityKey] = preferences.glassIntensity.name
            it[CardDensityKey] = preferences.cardDensity.name
            it[AnimationsKey] = preferences.animationsEnabled
        }
    }
}
