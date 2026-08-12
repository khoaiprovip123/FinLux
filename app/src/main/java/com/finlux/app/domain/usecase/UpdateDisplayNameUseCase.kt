package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.UserProfile
import com.finlux.app.domain.repository.AuthRepository
import javax.inject.Inject

/** Updates the canonical profile name used by Home, avatar and Settings. */
class UpdateDisplayNameUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(displayName: String): AppResult<UserProfile> {
        val normalizedName = displayName.trim()
        if (normalizedName.isBlank()) return AppResult.Error("Tên người dùng không được để trống")
        return repository.updateDisplayName(normalizedName)
    }
}
