package com.finlux.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val rememberMe: Boolean = true,
    val agreeTerms: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val completed: Boolean = false,
) {
    val passwordStrengthScore: Int
        get() {
            if (password.isEmpty()) return 0
            var score = 0
            if (password.length >= 8) score++
            if (password.any { it.isDigit() }) score++
            if (password.any { it.isUpperCase() } || password.any { it.isLowerCase() }) score++
            if (password.any { !it.isLetterOrDigit() }) score++
            return score.coerceIn(1, 4)
        }

    val passwordStrengthText: String
        get() = when (passwordStrengthScore) {
            1 -> "Yếu"
            2 -> "Trung bình"
            3 -> "Mạnh"
            4 -> "Rất mạnh"
            else -> ""
        }
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = mutableState.asStateFlow()

    fun updateDisplayName(value: String) = mutableState.update { it.copy(displayName = value, error = null) }
    fun updateEmail(value: String) = mutableState.update { it.copy(email = value, error = null) }
    fun updatePhone(value: String) = mutableState.update { it.copy(phone = value, error = null) }
    fun updatePassword(value: String) = mutableState.update { it.copy(password = value, error = null) }
    fun updateConfirmPassword(value: String) = mutableState.update { it.copy(confirmPassword = value, error = null) }
    fun toggleRememberMe(value: Boolean) = mutableState.update { it.copy(rememberMe = value) }
    fun toggleAgreeTerms(value: Boolean) = mutableState.update { it.copy(agreeTerms = value) }

    fun signIn() {
        val snapshot = state.value
        val error = when {
            !EMAIL_REGEX.matches(snapshot.email.trim()) -> "Email không hợp lệ"
            snapshot.password.isBlank() -> "Vui lòng nhập mật khẩu"
            else -> null
        }
        if (error != null) return mutableState.update { it.copy(error = error) }
        submit { repository.signIn(snapshot.email, snapshot.password) }
    }

    fun register() {
        val snapshot = state.value
        val error = when {
            snapshot.displayName.isBlank() -> "Vui lòng nhập họ tên"
            !EMAIL_REGEX.matches(snapshot.email.trim()) -> "Email không hợp lệ"
            snapshot.password.length < 8 || snapshot.password.none(Char::isLetter) || snapshot.password.none(Char::isDigit) ->
                "Mật khẩu cần ít nhất 8 ký tự, gồm chữ và số"
            snapshot.password != snapshot.confirmPassword -> "Mật khẩu xác nhận không khớp"
            !snapshot.agreeTerms -> "Vui lòng đồng ý với Điều khoản sử dụng & Chính sách bảo mật"
            else -> null
        }
        if (error != null) return mutableState.update { it.copy(error = error) }
        submit { repository.register(snapshot.displayName, snapshot.email, snapshot.password) }
    }

    fun resetPassword() {
        val email = state.value.email
        if (!EMAIL_REGEX.matches(email.trim())) {
            return mutableState.update { it.copy(error = "Email không hợp lệ") }
        }
        submit { repository.sendPasswordReset(email) }
    }

    fun signInWithGoogle(context: android.content.Context) {
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, error = null) }
            val webClientId = try {
                val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                if (resId != 0) context.getString(resId).trim() else "927751753962-04paon2termkbeanbsv7m8t9a8m6tk5h.apps.googleusercontent.com"
            } catch (e: Exception) {
                "927751753962-04paon2termkbeanbsv7m8t9a8m6tk5h.apps.googleusercontent.com"
            }.ifBlank { "927751753962-04paon2termkbeanbsv7m8t9a8m6tk5h.apps.googleusercontent.com" }

            android.util.Log.d("GoogleSignIn", "Attempting Google Sign-In with serverClientId: $webClientId")

            val targetContext = context.findActivity() ?: context

            try {
                val timedOut = kotlinx.coroutines.withTimeoutOrNull(25_000L) {
                    val credentialManager = androidx.credentials.CredentialManager.create(targetContext)

                    val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(webClientId)
                        .setAutoSelectEnabled(false)
                        .build()

                    val request = androidx.credentials.GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    val result = credentialManager.getCredential(context = targetContext, request = request)
                    val credential = result.credential

                    val idToken = when {
                        credential is androidx.credentials.CustomCredential &&
                            credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                            val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                            googleIdTokenCredential.idToken
                        }
                        credential is androidx.credentials.CustomCredential -> {
                            try {
                                val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                                googleIdTokenCredential.idToken
                            } catch (e: Exception) {
                                null
                            }
                        }
                        else -> null
                    }

                    if (!idToken.isNullOrBlank()) {
                        android.util.Log.d("GoogleSignIn", "Successfully retrieved Google ID Token")
                        when (val authResult = repository.signInWithGoogle(idToken)) {
                            is AppResult.Success -> mutableState.update { it.copy(completed = true) }
                            is AppResult.Error -> mutableState.update { it.copy(error = authResult.message) }
                        }
                    } else {
                        android.util.Log.e("GoogleSignIn", "Unexpected credential type or empty token: ${credential.type}")
                        mutableState.update { it.copy(error = "Không thể lấy thông tin xác thực từ Google") }
                    }
                    true
                }

                if (timedOut == null) {
                    android.util.Log.e("GoogleSignIn", "Google Sign-In timed out after 25s")
                    mutableState.update { it.copy(error = "Kết nối Google quá thời gian, vui lòng thử lại") }
                }
            } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
                android.util.Log.d("GoogleSignIn", "User cancelled Google Sign-In picker")
                mutableState.update { it.copy(error = null) }
            } catch (e: androidx.credentials.exceptions.NoCredentialException) {
                android.util.Log.e("GoogleSignIn", "NoCredentialException: ${e.message}", e)
                mutableState.update { it.copy(error = "Không tìm thấy tài khoản Google nào trên thiết bị. Vui lòng đăng nhập tài khoản Google vào máy.") }
            } catch (e: androidx.credentials.exceptions.GetCredentialException) {
                android.util.Log.e("GoogleSignIn", "GetCredentialException: ${e.message}", e)
                mutableState.update { it.copy(error = e.localizedMessage ?: "Đăng nhập Google thất bại. Vui lòng thử lại.") }
            } catch (e: Exception) {
                android.util.Log.e("GoogleSignIn", "Google Sign-In Exception: ${e.message}", e)
                mutableState.update { it.copy(error = e.localizedMessage ?: "Đăng nhập Google thất bại") }
            } finally {
                mutableState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun android.content.Context.findActivity(): android.app.Activity? {
        var current = this
        while (current is android.content.ContextWrapper) {
            if (current is android.app.Activity) return current
            current = current.baseContext
        }
        return null
    }

    private fun submit(operation: suspend () -> AppResult<*>) {
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, error = null) }
            when (val result = operation()) {
                is AppResult.Success -> mutableState.update { it.copy(isLoading = false, completed = true) }
                is AppResult.Error -> mutableState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    private companion object {
        val EMAIL_REGEX = Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE)
    }
}

