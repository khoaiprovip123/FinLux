package com.finlux.app.presentation

import app.cash.turbine.test
import com.finlux.app.domain.model.AppUiStyle
import com.finlux.app.domain.model.ThemePreference
import com.finlux.app.domain.model.UiPreferences
import com.finlux.app.domain.repository.ThemePreferenceRepository
import com.finlux.app.domain.repository.UiPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RootViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeThemePreferenceRepository : ThemePreferenceRepository {
        val preferenceFlow = MutableStateFlow(ThemePreference.SYSTEM)
        val uiStyleFlow = MutableStateFlow(AppUiStyle.CLASSIC_LIQUID)

        override val preference: Flow<ThemePreference> = preferenceFlow
        override val uiStyle: Flow<AppUiStyle> = uiStyleFlow

        override suspend fun setPreference(preference: ThemePreference) {
            preferenceFlow.value = preference
        }

        override suspend fun setUiStyle(uiStyle: AppUiStyle) {
            uiStyleFlow.value = uiStyle
        }
    }

    private class FakeUiPreferencesRepository : UiPreferencesRepository {
        val preferencesFlow = MutableStateFlow(UiPreferences())
        override val preferences: Flow<UiPreferences> = preferencesFlow

        override suspend fun setPreferences(preferences: UiPreferences) {
            preferencesFlow.value = preferences
        }
    }

    private lateinit var themeRepository: FakeThemePreferenceRepository
    private lateinit var uiPreferencesRepository: FakeUiPreferencesRepository
    private lateinit var reminderSyncObserver: com.finlux.app.data.local.reminder.ReminderSyncObserver
    private lateinit var viewModel: RootViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        themeRepository = FakeThemePreferenceRepository()
        uiPreferencesRepository = FakeUiPreferencesRepository()
        reminderSyncObserver = io.mockk.mockk(relaxed = true)
        viewModel = RootViewModel(themeRepository, uiPreferencesRepository, reminderSyncObserver)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial ui style is CLASSIC_LIQUID`() = runTest {
        viewModel.uiStyle.test {
            assertEquals(AppUiStyle.CLASSIC_LIQUID, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setting ui style updates repository and state`() = runTest {
        viewModel.uiStyle.test {
            assertEquals(AppUiStyle.CLASSIC_LIQUID, awaitItem())

            viewModel.setUiStyle(AppUiStyle.MODERN_LUXURY)
            advanceUntilIdle()

            assertEquals(AppUiStyle.MODERN_LUXURY, awaitItem())
            assertEquals(AppUiStyle.MODERN_LUXURY, themeRepository.uiStyleFlow.value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `switching back to CLASSIC_LIQUID works correctly`() = runTest {
        viewModel.uiStyle.test {
            assertEquals(AppUiStyle.CLASSIC_LIQUID, awaitItem())

            viewModel.setUiStyle(AppUiStyle.MODERN_LUXURY)
            advanceUntilIdle()
            assertEquals(AppUiStyle.MODERN_LUXURY, awaitItem())

            viewModel.setUiStyle(AppUiStyle.CLASSIC_LIQUID)
            advanceUntilIdle()
            assertEquals(AppUiStyle.CLASSIC_LIQUID, awaitItem())
            assertEquals(AppUiStyle.CLASSIC_LIQUID, themeRepository.uiStyleFlow.value)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
