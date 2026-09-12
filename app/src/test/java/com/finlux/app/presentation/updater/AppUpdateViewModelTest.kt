package com.finlux.app.presentation.updater

import com.finlux.app.core.common.AppResult
import com.finlux.app.core.updater.AppUpdateInfo
import com.finlux.app.core.updater.AppUpdateManager
import com.finlux.app.domain.model.UiPreferences
import com.finlux.app.domain.repository.UiPreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppUpdateViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val updateManager: AppUpdateManager = mockk(relaxed = true)

    private class FakeUiPreferencesRepository(
        initialPreferences: UiPreferences = UiPreferences(),
    ) : UiPreferencesRepository {
        val preferencesFlow = MutableStateFlow(initialPreferences)
        override val preferences: Flow<UiPreferences> = preferencesFlow

        override suspend fun setPreferences(preferences: UiPreferences) {
            preferencesFlow.value = preferences
        }
    }

    private lateinit var uiPreferencesRepository: FakeUiPreferencesRepository
    private lateinit var viewModel: AppUpdateViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        uiPreferencesRepository = FakeUiPreferencesRepository()
        viewModel = AppUpdateViewModel(
            updateManager = updateManager,
            uiPreferencesRepository = uiPreferencesRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `silent check skips update check when autoCheckUpdates is disabled`() = runTest(testDispatcher) {
        uiPreferencesRepository.preferencesFlow.value = UiPreferences(autoCheckUpdates = false)

        viewModel.checkForUpdates(silent = true)
        advanceUntilIdle()

        coVerify(exactly = 0) { updateManager.checkForUpdates() }
        assertEquals(UpdateUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `silent check performs update check when autoCheckUpdates is enabled`() = runTest(testDispatcher) {
        uiPreferencesRepository.preferencesFlow.value = UiPreferences(autoCheckUpdates = true)
        val info = AppUpdateInfo(
            latestVersionName = "1.25.5",
            releaseTitle = "v1.25.5",
            releaseNotes = "New features",
            downloadUrl = "https://example.com/app.apk",
            isUpdateAvailable = true,
        )
        coEvery { updateManager.checkForUpdates() } returns AppResult.Success(info)

        viewModel.checkForUpdates(silent = true)
        advanceUntilIdle()

        coVerify(exactly = 1) { updateManager.checkForUpdates() }
        assertTrue(viewModel.uiState.value is UpdateUiState.UpdateAvailable)
    }

    @Test
    fun `manual check still performs update check even when autoCheckUpdates is disabled`() = runTest(testDispatcher) {
        uiPreferencesRepository.preferencesFlow.value = UiPreferences(autoCheckUpdates = false)
        val info = AppUpdateInfo(
            latestVersionName = "1.25.4",
            releaseTitle = "v1.25.4",
            releaseNotes = "Latest",
            downloadUrl = "",
            isUpdateAvailable = false,
        )
        coEvery { updateManager.checkForUpdates() } returns AppResult.Success(info)

        viewModel.checkForUpdates(silent = false)
        advanceUntilIdle()

        coVerify(exactly = 1) { updateManager.checkForUpdates() }
        assertTrue(viewModel.uiState.value is UpdateUiState.NoUpdateAvailable)
    }
}
