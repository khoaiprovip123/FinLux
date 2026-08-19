package com.finlux.app.presentation.updater

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.common.AppResult
import com.finlux.app.core.updater.AppUpdateInfo
import com.finlux.app.core.updater.AppUpdateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed interface UpdateUiState {
    data object Idle : UpdateUiState
    data object Checking : UpdateUiState
    data class NoUpdateAvailable(val currentVersion: String) : UpdateUiState
    data class UpdateAvailable(val info: AppUpdateInfo) : UpdateUiState
    data class Downloading(val info: AppUpdateInfo, val progress: Float) : UpdateUiState
    data class ReadyToInstall(val info: AppUpdateInfo, val apkFile: File) : UpdateUiState
    data class Error(val message: String) : UpdateUiState
}

@HiltViewModel
class AppUpdateViewModel @Inject constructor(
    private val updateManager: AppUpdateManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    fun checkForUpdates(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) _uiState.value = UpdateUiState.Checking
            when (val result = updateManager.checkForUpdates()) {
                is AppResult.Success -> {
                    val info = result.value
                    if (info.isUpdateAvailable) {
                        _uiState.value = UpdateUiState.UpdateAvailable(info)
                    } else {
                        if (!silent) {
                            _uiState.value = UpdateUiState.NoUpdateAvailable(info.latestVersionName)
                        }
                    }
                }
                is AppResult.Error -> {
                    if (!silent) {
                        _uiState.value = UpdateUiState.Error(result.message)
                    }
                }
            }
        }
    }

    fun downloadAndInstall(info: AppUpdateInfo) {
        viewModelScope.launch {
            _downloadProgress.value = 0f
            _uiState.value = UpdateUiState.Downloading(info, 0f)
            val downloadResult = updateManager.downloadApk(info.downloadUrl) { progress ->
                _downloadProgress.value = progress
                _uiState.value = UpdateUiState.Downloading(info, progress)
            }
            when (downloadResult) {
                is AppResult.Success -> {
                    val apkFile = downloadResult.value
                    _uiState.value = UpdateUiState.ReadyToInstall(info, apkFile)
                    updateManager.installApk(apkFile)
                }
                is AppResult.Error -> {
                    _uiState.value = UpdateUiState.Error(downloadResult.message)
                }
            }
        }
    }

    fun installDownloadedApk(apkFile: File) {
        updateManager.installApk(apkFile)
    }

    fun dismissUpdate() {
        _uiState.value = UpdateUiState.Idle
    }
}
