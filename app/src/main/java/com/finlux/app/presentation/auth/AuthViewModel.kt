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

