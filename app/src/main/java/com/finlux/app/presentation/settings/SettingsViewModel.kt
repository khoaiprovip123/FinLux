package com.finlux.app.presentation.settings

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.UserProfile
import com.finlux.app.domain.repository.AuthRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.UpdateDisplayNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AvatarUpdateState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
)

data class NameUpdateState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val updateDisplayNameUseCase: UpdateDisplayNameUseCase,
    walletRepository: WalletRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val mutableUser = MutableStateFlow<UserProfile?>(null)
    val user = mutableUser.asStateFlow()
    val wallets = walletRepository.observeWallets().stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000), emptyList())

    private val mutableAvatarState = MutableStateFlow(AvatarUpdateState())
    val avatarState = mutableAvatarState.asStateFlow()
    private val mutableNameState = MutableStateFlow(NameUpdateState())
    val nameState = mutableNameState.asStateFlow()

    init {
        viewModelScope.launch { authRepository.currentUser.collect { mutableUser.value = it } }
    }

    fun updateAvatar(uri: Uri) {
        if (mutableAvatarState.value.isLoading) return
        viewModelScope.launch {
            mutableAvatarState.value = AvatarUpdateState(isLoading = true)
            val bytes = withContext(Dispatchers.IO) { prepareAvatar(uri) }
            if (bytes == null) {
                mutableAvatarState.value = AvatarUpdateState(message = "Không thể đọc ảnh đã chọn", isError = true)
                return@launch
            }
            when (val result = authRepository.updateAvatar(bytes)) {
                is AppResult.Success -> {
                    mutableUser.value = result.value
                    mutableAvatarState.value = AvatarUpdateState(message = "Đã cập nhật ảnh đại diện")
                }
                is AppResult.Error -> mutableAvatarState.value = AvatarUpdateState(message = result.message, isError = true)
            }
        }
    }

    fun clearAvatarMessage() = mutableAvatarState.update { it.copy(message = null) }

    fun updateDisplayName(displayName: String) {
        if (mutableNameState.value.isLoading) return
        viewModelScope.launch {
            mutableNameState.value = NameUpdateState(isLoading = true)
            when (val result = updateDisplayNameUseCase(displayName)) {
                is AppResult.Success -> {
                    mutableUser.value = result.value
                    mutableNameState.value = NameUpdateState(message = "Đã cập nhật tên người dùng")
                }
                is AppResult.Error -> mutableNameState.value = NameUpdateState(message = result.message, isError = true)
            }
        }
    }

    fun clearNameMessage() = mutableNameState.update { it.copy(message = null) }

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onDone()
        }
    }

    /** UC-05/BR-03: center-crop 1:1, cap dimensions and compress toward a 500 KB upload. */
    private fun prepareAvatar(uri: Uri): ByteArray? = runCatching {
        val bitmap = context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
            ?: error("Ảnh không hợp lệ")
        val edge = minOf(bitmap.width, bitmap.height)
        val square = Bitmap.createBitmap(bitmap, (bitmap.width - edge) / 2, (bitmap.height - edge) / 2, edge, edge)
        val scaled = if (edge > 1_024) Bitmap.createScaledBitmap(square, 1_024, 1_024, true) else square
        var quality = 88
        var output: ByteArray
        do {
            output = ByteArrayOutputStream().use { stream ->
                scaled.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                stream.toByteArray()
            }
            quality -= 8
        } while (output.size > 500 * 1_024 && quality >= 52)
        output
    }.getOrNull()
}
