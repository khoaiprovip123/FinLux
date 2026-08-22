package com.finlux.app.core.security

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.finlux.app.domain.model.BiometricLockTimeout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the application-level lock state based on Biometric settings
 * and ProcessLifecycleOwner (background / foreground transitions).
 */
object AppLockManager : DefaultLifecycleObserver {

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private var lastBackgroundTimestamp: Long = 0L
    private var isBiometricEnabled: Boolean = false
    private var currentTimeout: BiometricLockTimeout = BiometricLockTimeout.IMMEDIATE
    private var isObserverAttached = false

    fun init(biometricEnabled: Boolean, timeout: BiometricLockTimeout) {
        isBiometricEnabled = biometricEnabled
        currentTimeout = timeout
        if (biometricEnabled) {
            _isLocked.value = true
        }
        attachLifecycleObserver()
    }

    fun attachLifecycleObserver() {
        if (!isObserverAttached) {
            try {
                ProcessLifecycleOwner.get().lifecycle.addObserver(this)
                isObserverAttached = true
            } catch (_: Exception) {
                // Ignore in isolated unit test environments
            }
        }
    }

    fun updatePreferences(biometricEnabled: Boolean, timeout: BiometricLockTimeout) {
        isBiometricEnabled = biometricEnabled
        currentTimeout = timeout
        if (!biometricEnabled) {
            _isLocked.value = false
            lastBackgroundTimestamp = 0L
        }
        attachLifecycleObserver()
    }

    override fun onStop(owner: LifecycleOwner) {
        if (isBiometricEnabled && !_isLocked.value) {
            lastBackgroundTimestamp = System.currentTimeMillis()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        if (isBiometricEnabled && lastBackgroundTimestamp > 0L) {
            val elapsedMillis = System.currentTimeMillis() - lastBackgroundTimestamp
            val timeoutMillis = currentTimeout.minutes * 60 * 1000L
            if (elapsedMillis >= timeoutMillis) {
                _isLocked.value = true
            }
        }
    }

    /**
     * Helper for unit tests and direct verification.
     */
    fun checkAutoLock(currentTimeMillis: Long) {
        if (isBiometricEnabled && lastBackgroundTimestamp > 0L) {
            val elapsedMillis = currentTimeMillis - lastBackgroundTimestamp
            val timeoutMillis = currentTimeout.minutes * 60 * 1000L
            if (elapsedMillis >= timeoutMillis) {
                _isLocked.value = true
            }
        }
    }

    fun recordBackground(timestamp: Long = System.currentTimeMillis()) {
        if (isBiometricEnabled && !_isLocked.value) {
            lastBackgroundTimestamp = timestamp
        }
    }

    fun unlock() {
        _isLocked.value = false
        lastBackgroundTimestamp = 0L
    }

    fun lock() {
        if (isBiometricEnabled) {
            _isLocked.value = true
        }
    }

    fun resetForTest() {
        _isLocked.value = false
        lastBackgroundTimestamp = 0L
        isBiometricEnabled = false
        currentTimeout = BiometricLockTimeout.IMMEDIATE
    }
}
