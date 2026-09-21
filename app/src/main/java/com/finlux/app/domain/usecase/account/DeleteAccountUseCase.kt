package com.finlux.app.domain.usecase.account

import android.content.Context
import com.finlux.app.core.common.AppResult
import com.finlux.app.core.security.AppLockManager
import com.finlux.app.core.sync.DataSyncManager
import com.finlux.app.domain.model.BiometricLockTimeout
import com.finlux.app.domain.repository.AuthRepository
import com.finlux.app.domain.repository.DebtPreferenceRepository
import com.finlux.app.domain.repository.ReminderRepository
import com.finlux.app.domain.repository.ReminderScheduler
import com.finlux.app.domain.repository.SalaryCycleScheduler
import com.finlux.app.domain.repository.SavingSpinScheduler
import com.finlux.app.domain.repository.ThemePreferenceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val purgeUserDataUseCase: PurgeUserDataUseCase,
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler,
    private val salaryCycleScheduler: SalaryCycleScheduler,
    private val savingSpinScheduler: SavingSpinScheduler,
    private val themePreferenceRepository: ThemePreferenceRepository,
    private val debtPreferenceRepository: DebtPreferenceRepository,
    private val dataSyncManager: DataSyncManager,
    @ApplicationContext private val context: Context,
) {
    suspend operator fun invoke(
        password: String? = null,
        googleIdToken: String? = null,
    ): AppResult<Unit> {
        // Step 1: Pre-flight Gate & Re-authentication check
        val currentUser = authRepository.currentUser.firstOrNull()
            ?: return AppResult.Error("Người dùng chưa đăng nhập hoặc phiên đã hết hạn")

        if (!password.isNullOrBlank()) {
            val reauthResult = authRepository.reauthenticateWithPassword(password)
            if (reauthResult is AppResult.Error) return reauthResult
        } else if (!googleIdToken.isNullOrBlank()) {
            val reauthResult = authRepository.reauthenticateWithGoogle(googleIdToken)
            if (reauthResult is AppResult.Error) return reauthResult
        }

        // Step 2: Cancel all native alarms (Alarms can be safely cancelled)
        cancelAllAlarms()

        // Step 3: Purge Cloud Storage & Cloud Firestore
        val purgeResult = purgeUserDataUseCase(currentUser.uid)
        if (purgeResult is AppResult.Error) {
            return purgeResult
        }

        // Step 4: Delete Auth User from Firebase Authentication
        val deleteAuthResult = authRepository.deleteAuthAccount()
        if (deleteAuthResult is AppResult.Error) {
            return deleteAuthResult
        }

        // Step 5: Reset local storage & cache
        resetLocalStorage()

        // Step 6: Reset memory state & broadcast sync
        resetMemoryState()

        return AppResult.Success(Unit)
    }

    private suspend fun cancelAllAlarms() {
        runCatching {
            withTimeoutOrNull(2000L) {
                reminderRepository.observeReminders().firstOrNull()?.forEach { reminder ->
                    reminderScheduler.cancel(reminder.id)
                }
            }
            salaryCycleScheduler.cancel()
            savingSpinScheduler.cancel()
        }
    }

    private suspend fun resetLocalStorage() {
        runCatching {
            // DataStore preferences
            themePreferenceRepository.resetPreferences()
            debtPreferenceRepository.resetPreferences()

            // SharedPreferences
            context.getSharedPreferences("saving_spin_preferences", Context.MODE_PRIVATE)
                .edit().clear().apply()
            context.getSharedPreferences("finlux_demo_profile", Context.MODE_PRIVATE)
                .edit().clear().apply()

            // Cache directory
            context.cacheDir?.deleteRecursively()
        }
    }

    private fun resetMemoryState() {
        runCatching {
            AppLockManager.updatePreferences(
                biometricEnabled = false,
                timeout = BiometricLockTimeout.IMMEDIATE,
            )
            dataSyncManager.notifyDataRestored()
        }
    }
}
