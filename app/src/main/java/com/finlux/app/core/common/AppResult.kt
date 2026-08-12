package com.finlux.app.core.common

/** Explicit operation result so presentation code never has to interpret raw exceptions. */
sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Error(val message: String, val cause: Throwable? = null) : AppResult<Nothing>
}
