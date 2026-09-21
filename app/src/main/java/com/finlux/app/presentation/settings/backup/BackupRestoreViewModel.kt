package com.finlux.app.presentation.settings.backup

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.backup.FinluxBackupParser
import com.finlux.app.domain.model.backup.RestoreStrategy
import com.finlux.app.domain.repository.AuthRepository
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.DealRepository
import com.finlux.app.domain.repository.DebtRepository
import com.finlux.app.domain.repository.GoalRepository
import com.finlux.app.domain.repository.ReminderRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.backup.ExportBackupUseCase
import com.finlux.app.domain.usecase.backup.RestoreBackupUseCase
import com.finlux.app.domain.usecase.backup.ValidateBackupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val debtRepository: DebtRepository,
    private val goalRepository: GoalRepository,
    private val reminderRepository: ReminderRepository,
    private val dealRepository: DealRepository,
    private val exportBackupUseCase: ExportBackupUseCase,
    private val validateBackupUseCase: ValidateBackupUseCase,
    private val restoreBackupUseCase: RestoreBackupUseCase,
    @ApplicationContext private val context: Context,
    private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO,
    private val dataSyncManager: com.finlux.app.core.sync.DataSyncManager? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupRestoreUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeCurrentUser()
        loadDataStats()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { current ->
                    current.copy(
                        currentUserEmail = user?.email,
                        currentUserId = user?.uid ?: "",
                        isCloudSyncing = user != null,
                    )
                }
            }
        }
    }

    fun loadDataStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isStatsLoading = true) }
            try {
                val stats = withContext(ioDispatcher) {
                    val walletsDeferred = async { walletRepository.observeWallets().first().size }
                    val categoriesDeferred = async { categoryRepository.observeCategories().first().size }
                    val txDeferred = async { transactionRepository.observePeriod(Instant.EPOCH, Instant.now()).first().size }
                    val budgetsDeferred = async { budgetRepository.observeBudgets("*").first().size }
                    val debtsDeferred = async { debtRepository.observeDebts().first().size }
                    val goalsDeferred = async { goalRepository.observeGoals().first().size }
                    val remindersDeferred = async { reminderRepository.observeReminders().first().size }
                    val dealsDeferred = async { dealRepository.observeDeals().first().size }

                    val wCount = walletsDeferred.await()
                    val cCount = categoriesDeferred.await()
                    val txCount = txDeferred.await()
                    val bCount = budgetsDeferred.await()
                    val dCount = debtsDeferred.await()
                    val gCount = goalsDeferred.await()
                    val rCount = remindersDeferred.await()
                    val dealCount = dealsDeferred.await()

                    val estimatedBytes = (txCount * 320L) + (wCount * 200L) + (cCount * 180L) + 4096L
                    val sizeLabel = when {
                        estimatedBytes < 1024L -> "$estimatedBytes B"
                        estimatedBytes < 1_048_576L -> "~%.1f KB".format(estimatedBytes / 1024.0)
                        else -> "~%.1f MB".format(estimatedBytes / 1_048_576.0)
                    }

                    DataStats(
                        walletCount = wCount,
                        categoryCount = cCount,
                        transactionCount = txCount,
                        budgetCount = bCount,
                        debtCount = dCount,
                        goalCount = gCount,
                        reminderCount = rCount,
                        dealCount = dealCount,
                        estimatedSizeLabel = sizeLabel,
                    )
                }

                _uiState.update { it.copy(dataStats = stats, isStatsLoading = false) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isStatsLoading = false) }
            }
        }
    }

    fun exportBackup(onShareReady: ((Uri) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportError = null, exportSuccessMessage = null) }
            val uid = _uiState.value.currentUserId.ifBlank { "anonymous_user" }
            when (val result = exportBackupUseCase(uid)) {
                is AppResult.Success -> {
                    val file = result.value
                    val uri = try {
                        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    } catch (_: Throwable) {
                        try {
                            Uri.fromFile(file)
                        } catch (_: Throwable) {
                            Uri.EMPTY
                        }
                    }
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportedFileUri = uri,
                            exportSuccessMessage = "Tạo bản sao lưu thành công!",
                        )
                    }
                    onShareReady?.invoke(uri)
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportError = result.message,
                        )
                    }
                }
            }
        }
    }

    fun onFileSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isValidating = true,
                    validationError = null,
                    previewSummary = null,
                    cachedJsonForRestore = null,
                )
            }

            try {
                var fileName = "FinLux_Backup.finlux"
                var fileSize = 0L

                val jsonContent = withContext(ioDispatcher) {
                    try {
                        context.contentResolver?.query(uri, null, null, null, null)?.use { cursor ->
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                            if (cursor.moveToFirst()) {
                                if (nameIndex != -1) cursor.getString(nameIndex)?.let { fileName = it }
                                if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                            }
                        }
                    } catch (_: Exception) {}

                    context.contentResolver?.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use {
                        it.readText()
                    } ?: throw IllegalStateException("Không thể đọc tệp sao lưu đã chọn")
                }

                onFileContentReady(fileName, fileSize, jsonContent)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isValidating = false,
                        previewSummary = null,
                        validationError = e.localizedMessage ?: "Lỗi khi đọc file sao lưu",
                        cachedJsonForRestore = null,
                    )
                }
            }
        }
    }

    fun onFileContentReady(fileName: String, fileSize: Long, jsonContent: String) {
        val uid = _uiState.value.currentUserId.ifBlank { "anonymous_user" }
        val result = validateBackupUseCase(
            content = jsonContent,
            fileSizeBytes = fileSize,
            currentUserId = uid,
        )

        when (result) {
            is AppResult.Success -> {
                _uiState.update {
                    it.copy(
                        isValidating = false,
                        selectedFileName = fileName,
                        selectedFileSizeBytes = fileSize,
                        previewSummary = result.value,
                        validationError = null,
                        cachedJsonForRestore = jsonContent,
                    )
                }
            }
            is AppResult.Error -> {
                _uiState.update {
                    it.copy(
                        isValidating = false,
                        selectedFileName = fileName,
                        previewSummary = null,
                        validationError = result.message,
                        cachedJsonForRestore = null,
                    )
                }
            }
        }
    }

    fun selectStrategy(strategy: RestoreStrategy) {
        _uiState.update { it.copy(selectedStrategy = strategy) }
    }

    fun requestRestore() {
        if (_uiState.value.selectedStrategy == RestoreStrategy.FULL_OVERWRITE) {
            _uiState.update { it.copy(showFullOverwriteConfirmDialog = true) }
        } else {
            confirmAndRestore()
        }
    }

    fun dismissConfirmDialog() {
        _uiState.update { it.copy(showFullOverwriteConfirmDialog = false) }
    }

    fun confirmAndRestore() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showFullOverwriteConfirmDialog = false,
                    isRestoring = true,
                    restoreError = null,
                    restoreReport = null,
                )
            }

            val json = _uiState.value.cachedJsonForRestore
            if (json.isNullOrBlank()) {
                _uiState.update {
                    it.copy(
                        isRestoring = false,
                        restoreError = "Không tìm thấy dữ liệu tệp sao lưu để khôi phục.",
                    )
                }
                return@launch
            }

            try {
                val snapshot = withContext(ioDispatcher) {
                    FinluxBackupParser.parse(json)
                }
                val uid = _uiState.value.currentUserId.ifBlank { "anonymous_user" }
                val result = restoreBackupUseCase(
                    snapshot = snapshot,
                    strategy = _uiState.value.selectedStrategy,
                    currentUserId = uid,
                )

                when (result) {
                    is AppResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isRestoring = false,
                                restoreReport = result.value,
                                restoreError = null,
                            )
                        }
                        loadDataStats()
                        dataSyncManager?.notifyDataRestored()
                    }
                    is AppResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isRestoring = false,
                                restoreError = result.message,
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRestoring = false,
                        restoreError = "Lỗi phục hồi dữ liệu: ${e.localizedMessage}",
                    )
                }
            }
        }
    }

    fun dismissReportDialog() {
        dataSyncManager?.notifyDataRestored()
        _uiState.update { it.copy(restoreReport = null) }
    }

    fun clearSelectedFile() {
        _uiState.update {
            it.copy(
                previewSummary = null,
                validationError = null,
                cachedJsonForRestore = null,
                selectedFileName = null,
                selectedFileSizeBytes = 0L,
            )
        }
    }
}
