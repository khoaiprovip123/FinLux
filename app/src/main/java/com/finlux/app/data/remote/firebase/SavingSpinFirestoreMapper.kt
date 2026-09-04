package com.finlux.app.data.remote.firebase

import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.SavingDestination
import com.finlux.app.domain.model.SavingMethod
import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.model.SavingSpinFrequency
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.SavingSpinStatus
import com.finlux.app.domain.model.SavingSpinStep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.time.Instant
import java.util.Date

internal object SavingSpinFirestoreMapper {
    fun configToMap(config: SavingSpinConfig): Map<String, Any?> = mapOf(
        "enabled" to config.enabled,
        "showOnHome" to config.showOnHome,
        "minAmount" to config.minAmount.value,
        "maxAmount" to config.maxAmount.value,
        "stepAmount" to config.step.amount,
        "slotCount" to config.slotCount,
        "frequency" to config.frequency.name,
        "selectedWeekdays" to config.selectedWeekdays.sorted(),
        "weeklyDay" to config.weeklyDay,
        "reminderEnabled" to config.reminderEnabled,
        "reminderHour" to config.reminderHour,
        "reminderMinute" to config.reminderMinute,
        "snoozeEnabled" to config.snoozeEnabled,
        "allowSkip" to config.allowSkip,
        "defaultDestinationId" to config.defaultDestinationId,
        "createdAt" to config.createdAt.toTimestamp(),
        "updatedAt" to config.updatedAt.toTimestamp(),
    )

    fun configFromMap(data: Map<String, Any?>?): SavingSpinConfig {
        if (data.isNullOrEmpty()) return SavingSpinConfig()
        val stepAmount = (data["stepAmount"] as? Number)?.toLong()
        return SavingSpinConfig(
            enabled = data["enabled"] as? Boolean ?: false,
            showOnHome = data["showOnHome"] as? Boolean ?: true,
            minAmount = Money((data["minAmount"] as? Number)?.toLong() ?: 10_000L),
            maxAmount = Money((data["maxAmount"] as? Number)?.toLong() ?: 100_000L),
            step = SavingSpinStep.entries.firstOrNull { it.amount == stepAmount } ?: SavingSpinStep.FIVE_THOUSAND,
            slotCount = (data["slotCount"] as? Number)?.toInt() ?: 8,
            frequency = enumOrDefault(data["frequency"] as? String, SavingSpinFrequency.DAILY),
            selectedWeekdays = (data["selectedWeekdays"] as? List<*>)
                .orEmpty().mapNotNull { (it as? Number)?.toInt() }.toSet(),
            weeklyDay = (data["weeklyDay"] as? Number)?.toInt() ?: 1,
            reminderEnabled = data["reminderEnabled"] as? Boolean ?: true,
            reminderHour = (data["reminderHour"] as? Number)?.toInt() ?: 9,
            reminderMinute = (data["reminderMinute"] as? Number)?.toInt() ?: 0,
            snoozeEnabled = data["snoozeEnabled"] as? Boolean ?: true,
            allowSkip = data["allowSkip"] as? Boolean ?: true,
            defaultDestinationId = data["defaultDestinationId"] as? String,
            createdAt = data.instant("createdAt"),
            updatedAt = data.instant("updatedAt"),
        )
    }

    fun destinationToMap(destination: SavingDestination): Map<String, Any?> = mapOf(
        "name" to destination.name,
        "method" to destination.method.name,
        "linkedWalletId" to destination.linkedWalletId,
        "institutionId" to destination.institutionId,
        "accountHint" to destination.accountHint,
        "enabled" to destination.enabled,
        "icon" to destination.icon,
        "createdAt" to destination.createdAt.toTimestamp(),
        "updatedAt" to destination.updatedAt.toTimestamp(),
    )

    fun destinationFromDocument(document: DocumentSnapshot): SavingDestination? = runCatching {
        SavingDestination(
            id = document.id,
            name = requireNotNull(document.getString("name")),
            method = enumOrDefault(document.getString("method"), SavingMethod.CASH),
            linkedWalletId = document.getString("linkedWalletId"),
            institutionId = document.getString("institutionId"),
            accountHint = document.getString("accountHint"),
            enabled = document.getBoolean("enabled") ?: true,
            icon = document.getString("icon"),
            createdAt = document.instant("createdAt"),
            updatedAt = document.instant("updatedAt"),
        )
    }.getOrNull()

    fun sessionToMap(session: SavingSpinSession): Map<String, Any?> = mapOf(
        "scheduleKey" to session.scheduleKey,
        "wheelValues" to session.wheelValues.map { it.value },
        "selectedIndex" to session.selectedIndex,
        "selectedAmount" to session.selectedAmount?.value,
        "status" to session.status.name,
        "destinationId" to session.destinationId,
        "method" to session.method?.name,
        "spunAt" to session.spunAt?.toTimestamp(),
        "completedAt" to session.completedAt?.toTimestamp(),
        "skippedAt" to session.skippedAt?.toTimestamp(),
        "snoozedUntil" to session.snoozedUntil?.toTimestamp(),
        "transactionId" to session.transactionId,
        "createdAt" to session.createdAt.toTimestamp(),
        "updatedAt" to session.updatedAt.toTimestamp(),
    )

    fun sessionFromDocument(document: DocumentSnapshot): SavingSpinSession? = runCatching {
        val values = (document.get("wheelValues") as? List<*>)
            .orEmpty().mapNotNull { (it as? Number)?.toLong()?.let(::Money) }
        SavingSpinSession(
            id = document.id,
            scheduleKey = requireNotNull(document.getString("scheduleKey")),
            wheelValues = values,
            selectedIndex = document.getLong("selectedIndex")?.toInt(),
            selectedAmount = document.getLong("selectedAmount")?.let(::Money),
            status = enumOrDefault(document.getString("status"), SavingSpinStatus.READY),
            destinationId = document.getString("destinationId"),
            method = document.getString("method")?.let { enumOrDefault(it, SavingMethod.CASH) },
            spunAt = document.optionalInstant("spunAt"),
            completedAt = document.optionalInstant("completedAt"),
            skippedAt = document.optionalInstant("skippedAt"),
            snoozedUntil = document.optionalInstant("snoozedUntil"),
            transactionId = document.getString("transactionId"),
            createdAt = document.instant("createdAt"),
            updatedAt = document.instant("updatedAt"),
        )
    }.getOrNull()

    private inline fun <reified T : Enum<T>> enumOrDefault(raw: String?, fallback: T): T =
        raw?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: fallback

    private fun Instant.toTimestamp() = Timestamp(Date.from(this))
    private fun Map<String, Any?>.instant(field: String): Instant =
        (this[field] as? Timestamp)?.toDate()?.toInstant() ?: Instant.EPOCH
    private fun DocumentSnapshot.instant(field: String): Instant = optionalInstant(field) ?: Instant.EPOCH
    private fun DocumentSnapshot.optionalInstant(field: String): Instant? = getTimestamp(field)?.toDate()?.toInstant()
}
