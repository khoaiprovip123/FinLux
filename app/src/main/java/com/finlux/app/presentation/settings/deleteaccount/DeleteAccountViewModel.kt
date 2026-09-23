package com.finlux.app.presentation.settings.deleteaccount

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.repository.AuthRepository
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.DebtRepository
import com.finlux.app.domain.repository.GoalRepository
import com.finlux.app.domain.repository.ReminderRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.account.DeleteAccountUseCase
import com.finlux.app.domain.usecase.backup.ExportBackupUseCase
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

enum class PurgingStage(val message: String) {
    IDLE(""),
    CANCELLING_ALARMS("Đang hủy các nhắc nhở trên thiết bị..."),
    PURGING_STORAGE("Đang xóa ảnh hóa đơn và hồ sơ..."),
    PURGING_FIRESTORE("Đang dọn dẹp sổ cái tài chính trên đám mây..."),
    DELETING_AUTH("Đang hủy tài khoản xác thực..."),
    COMPLETED("Hoàn tất. Tạm biệt bạn!"),
}

data class DamageStats(
    val walletCount: Int = 0,
    val transactionCount: Int = 0,
    val debtCount: Int = 0,
    val goalCount: Int = 0,
    val reminderCount: Int = 0,
    val budgetCount: Int = 0,
) {
    val totalEntities: Int
        get() = walletCount + transactionCount + debtCount + goalCount + reminderCount + budgetCount
}

data class DeleteAccountUiState(
    val isLoadingStats: Boolean = false,
    val damageStats: DamageStats = DamageStats(),
    val currentStep: Int = 1,
    val providerId: String = "password",
    val passwordInput: String = "",
    val confirmationInput: String = "",
    val googleIdToken: String? = null,
    val isGoogleReauthenticated: Boolean = false,
    val isExportingBackup: Boolean = false,
    val exportedBackupUri: Uri? = null,
    val exportBackupSuccessMessage: String? = null,
    val exportBackupError: String? = null,
    val isPurging: Boolean = false,
    val purgingStage: PurgingStage = PurgingStage.IDLE,
    val errorMessage: String? = null,
    val isPurgeCompleted: Boolean = false,
) {
    val isConfirmationTextValid: Boolean
        get() = confirmationInput.trim() == "XÓA TÀI KHOẢN"

    val isReauthValid: Boolean
        get() = if (providerId == "google.com") {
            isGoogleReauthenticated || !googleIdToken.isNullOrBlank()
        } else {
            passwordInput.isNotBlank()
        }

    val canExecuteDelete: Boolean
        get() = isConfirmationTextValid && isReauthValid && !isPurging && !isLoadingStats
}

@HiltViewModel
class DeleteAccountViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val debtRepository: DebtRepository,
    private val goalRepository: GoalRepository,
    private val budgetRepository: BudgetRepository,
    private val reminderRepository: ReminderRepository,
    private val exportBackupUseCase: ExportBackupUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    @ApplicationContext private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = kotlinx.coroutines.Dispatchers.IO,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeleteAccountUiState())
    val uiState: StateFlow<DeleteAccountUiState> = _uiState.asStateFlow()

    init {
        detectProvider()
        loadDamageStats()
    }

    private fun detectProvider() {
        val provider = authRepository.getAuthProviderId()
        _uiState.update { it.copy(providerId = provider) }
    }

    fun loadDamageStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStats = true) }
            try {
                val stats = withContext(ioDispatcher) {
                    val walletsDeferred = async { walletRepository.observeWallets().firstOrNull()?.size ?: 0 }
                    val txDeferred = async { transactionRepository.observePeriod(Instant.EPOCH, Instant.now()).firstOrNull()?.size ?: 0 }
                    val debtsDeferred = async { debtRepository.observeDebts().firstOrNull()?.size ?: 0 }
                    val goalsDeferred = async { goalRepository.observeGoals().firstOrNull()?.size ?: 0 }
                    val budgetsDeferred = async { budgetRepository.observeBudgets("*").firstOrNull()?.size ?: 0 }
                    val remindersDeferred = async { reminderRepository.observeReminders().firstOrNull()?.size ?: 0 }

                    DamageStats(
                        walletCount = walletsDeferred.await(),
                        transactionCount = txDeferred.await(),
                        debtCount = debtsDeferred.await(),
                        goalCount = goalsDeferred.await(),
                        budgetCount = budgetsDeferred.await(),
                        reminderCount = remindersDeferred.await(),
                    )
                }
                _uiState.update { it.copy(isLoadingStats = false, damageStats = stats) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoadingStats = false) }
            }
        }
    }

    fun goToStep(step: Int) {
        _uiState.update { it.copy(currentStep = step.coerceIn(1, 3), errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(passwordInput = password, errorMessage = null) }
    }

    fun onConfirmationInputChanged(input: String) {
        _uiState.update { it.copy(confirmationInput = input, errorMessage = null) }
    }

    fun onGoogleIdTokenReceived(token: String) {
        _uiState.update {
            it.copy(
                googleIdToken = token,
                isGoogleReauthenticated = true,
                errorMessage = null,
            )
        }
    }

    fun reauthenticateWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            val webClientId = try {
                val resId = activityContext.resources.getIdentifier("default_web_client_id", "string", activityContext.packageName)
                if (resId != 0) activityContext.getString(resId).trim() else "927751753962-04paon2termkbeanbsv7m8t9a8m6tk5h.apps.googleusercontent.com"
            } catch (_: Exception) {
                "927751753962-04paon2termkbeanbsv7m8t9a8m6tk5h.apps.googleusercontent.com"
            }.ifBlank { "927751753962-04paon2termkbeanbsv7m8t9a8m6tk5h.apps.googleusercontent.com" }

            val targetContext = activityContext.findActivity() ?: activityContext

            try {
                val credentialManager = CredentialManager.create(targetContext)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context = targetContext, request = request)
                val credential = result.credential

                val idToken = when {
                    credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        googleIdTokenCredential.idToken
                    }
                    credential is CustomCredential -> {
                        try {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            googleIdTokenCredential.idToken
                        } catch (_: Exception) {
                            null
                        }
                    }
                    else -> null
                }

                if (!idToken.isNullOrBlank()) {
                    val reauthResult = authRepository.reauthenticateWithGoogle(idToken)
                    when (reauthResult) {
                        is AppResult.Success -> {
                            _uiState.update {
                                it.copy(
                                    googleIdToken = idToken,
                                    isGoogleReauthenticated = true,
                                    errorMessage = null,
                                )
                            }
                        }
                        is AppResult.Error -> {
                            _uiState.update { it.copy(errorMessage = reauthResult.message) }
                        }
                    }
                } else {
                    _uiState.update { it.copy(errorMessage = "Không thể lấy thông tin xác thực từ Google") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Lỗi xác thực Google: ${e.localizedMessage ?: "Vui lòng thử lại"}") }
            }
        }
    }

    fun exportBackup(onShareReady: ((Uri) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExportingBackup = true, exportBackupError = null, exportBackupSuccessMessage = null) }
            val currentUser = authRepository.currentUser.firstOrNull()
            val uid = currentUser?.uid ?: "anonymous_user"

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
                            isExportingBackup = false,
                            exportedBackupUri = uri,
                            exportBackupSuccessMessage = "Tạo bản sao lưu thành công!",
                        )
                    }
                    onShareReady?.invoke(uri)
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isExportingBackup = false,
                            exportBackupError = result.message,
                        )
                    }
                }
            }
        }
    }

    fun executeDeleteAccount() {
        val currentState = _uiState.value
        if (!currentState.isConfirmationTextValid) {
            _uiState.update { it.copy(errorMessage = "Vui lòng gõ chính xác: XÓA TÀI KHOẢN") }
            return
        }

        if (currentState.providerId == "password" && currentState.passwordInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập mật khẩu hiện tại để xác thực") }
            return
        }

        if (currentState.providerId == "google.com" && !currentState.isGoogleReauthenticated && currentState.googleIdToken.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng hoàn thành xác thực lại với Google") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isPurging = true,
                    purgingStage = PurgingStage.CANCELLING_ALARMS,
                    errorMessage = null,
                )
            }

            delay(300)
            _uiState.update { it.copy(purgingStage = PurgingStage.PURGING_STORAGE) }
            delay(300)
            _uiState.update { it.copy(purgingStage = PurgingStage.PURGING_FIRESTORE) }

            val result = deleteAccountUseCase(
                password = if (currentState.providerId == "password") currentState.passwordInput else null,
                googleIdToken = if (currentState.providerId == "google.com") currentState.googleIdToken else null,
            )

            when (result) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(purgingStage = PurgingStage.DELETING_AUTH) }
                    delay(300)
                    _uiState.update {
                        it.copy(
                            purgingStage = PurgingStage.COMPLETED,
                            isPurgeCompleted = true,
                            isPurging = false,
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isPurging = false,
                            purgingStage = PurgingStage.IDLE,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, exportBackupError = null) }
    }

    fun resetState() {
        _uiState.update {
            DeleteAccountUiState(
                damageStats = it.damageStats,
                providerId = it.providerId,
            )
        }
    }
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
