package com.finlux.app.domain.repository

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observeNotifications(): Flow<List<AppNotification>>
    suspend fun saveNotification(notification: AppNotification): AppResult<String>
    suspend fun markAsRead(id: String): AppResult<Unit>
    suspend fun markAsPaid(id: String): AppResult<Unit>
    suspend fun markAsPaidByReminderId(reminderId: String): AppResult<Unit>
    suspend fun clearAll(): AppResult<Unit>
}
