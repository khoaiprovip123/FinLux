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
}
