package com.finlux.app.domain.repository

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<UserProfile?>
    suspend fun signIn(email: String, password: String): AppResult<UserProfile>
    suspend fun register(displayName: String, email: String, password: String): AppResult<UserProfile>
    suspend fun signInWithGoogle(idToken: String): AppResult<UserProfile>
    suspend fun sendPasswordReset(email: String): AppResult<Unit>
    suspend fun updateDisplayName(displayName: String): AppResult<UserProfile>
    suspend fun updateAvatar(jpegBytes: ByteArray): AppResult<UserProfile>
    suspend fun signOut()

    /** Kiểm tra loại Provider chính của tài khoản hiện tại ("password" hoặc "google.com") */
    fun getAuthProviderId(): String = "password"

    /** Xác thực lại bằng Email/Password trước khi thực hiện hành động nhạy cảm */
    suspend fun reauthenticateWithPassword(password: String): AppResult<Unit> = AppResult.Success(Unit)

    /** Xác thực lại bằng Google ID Token trước khi thực hiện hành động nhạy cảm */
    suspend fun reauthenticateWithGoogle(idToken: String): AppResult<Unit> = AppResult.Success(Unit)

    /** Xóa hoàn toàn tài khoản người dùng trên Firebase Authentication */
    suspend fun deleteAuthAccount(): AppResult<Unit> = AppResult.Success(Unit)
}
