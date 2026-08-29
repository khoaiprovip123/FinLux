package com.finlux.app.domain.repository

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.AppNotification
import com.finlux.app.domain.model.Money
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observeNotifications(): Flow<List<AppNotification>>
    suspend fun saveNotification(notification: AppNotification): AppResult<String>
    suspend fun markAsRead(id: String): AppResult<Unit>
    suspend fun markAllAsRead(): AppResult<Unit>
    suspend fun markAsPaid(id: String): AppResult<Unit>
    suspend fun markAsPaidWithAmount(id: String, amount: Money, newBody: String? = null): AppResult<Unit>
    suspend fun markAsPaidByReminderId(reminderId: String): AppResult<Unit>
    suspend fun deleteNotification(id: String): AppResult<Unit>
    suspend fun clearAll(): AppResult<Unit>
}
