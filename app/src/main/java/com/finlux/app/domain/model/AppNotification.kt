package com.finlux.app.domain.model

import java.time.Instant

data class AppNotification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val type: NotificationType = NotificationType.REMINDER,
    val amount: Money = Money(0L),
    val reminderId: String? = null,
    val categoryId: String? = null,
    val walletId: String? = null,
    val targetRoute: String? = null,
    val targetId: String? = null,
    val actionUrl: String? = null,
    val timestamp: Instant = Instant.now(),
    val isRead: Boolean = false,
    val isPaid: Boolean = false,
)
