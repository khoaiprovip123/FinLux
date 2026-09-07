package com.finlux.app.data.remote.firebase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.repository.BudgetRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.YearMonth
import java.util.Date

class FirebaseBudgetRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : BudgetRepository {

    override fun observeBudgets(periodKey: String): Flow<List<Budget>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            close()
            return@callbackFlow
        }
        val keysToMatch = listOfNotNull(
            periodKey,
            if (periodKey.startsWith("month:")) periodKey.removePrefix("month:") else null,
            if (periodKey.startsWith("salary:")) periodKey.removePrefix("salary:") else null,
            if (!periodKey.startsWith("month:") && !periodKey.startsWith("salary:") && periodKey.isNotBlank()) "month:$periodKey" else null,
        ).distinct()

        val budgets = firestore.collection("users").document(uid).collection("budgets")
        var modernBudgets = emptyMap<String, Budget>()
        var legacyBudgets = emptyMap<String, Budget>()

        fun emitMerged() {
            trySend((legacyBudgets + modernBudgets).values.toList())
        }

        val modernRegistration = budgets
            .whereIn("periodKey", keysToMatch)
            .addSnapshotListener { snapshot, error ->
                if (error != null) close(error)
                else {
                    modernBudgets = snapshot?.documents.orEmpty()
                        .mapNotNull { it.toBudget() }
                        .associateBy(Budget::id)
                    emitMerged()
                }
            }

        val legacyMonth = when {
            periodKey.startsWith("month:") -> periodKey.removePrefix("month:")
            periodKey.matches(Regex("\\d{4}-\\d{2}")) -> periodKey
            else -> null
        }
        val legacyRegistration = legacyMonth?.let { month ->
            budgets.whereEqualTo("month", month)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) close(error)
                    else {
                        legacyBudgets = snapshot?.documents.orEmpty()
                            .mapNotNull { it.toBudget() }
                            .associateBy(Budget::id)
                        emitMerged()
                    }
                }
        }
        awaitClose {
            modernRegistration.remove()
            legacyRegistration?.remove()
        }
    }

    override suspend fun upsertBudget(budget: Budget): AppResult<String> = firebaseResult("Không thể lưu ngân sách") {
        val uid = requireUid()
        val id = budget.id.ifBlank { "${budget.categoryId}_${budget.periodKey}" }
        firestore.collection("users").document(uid).collection("budgets").document(id)
            .set(budget.copy(id = id).toBudgetClientMap(), SetOptions.merge()).await()
        id
    }

    override suspend fun deleteBudget(budget: Budget): AppResult<Unit> = firebaseResult("Không thể xóa ngân sách") {
        val uid = requireUid()
        firestore.collection("users").document(uid).collection("budgets").document(budget.id).delete().await()
        Unit
    }

    private fun requireUid(): String = auth.currentUser?.uid ?: error("Phiên đăng nhập đã hết hạn")
}

internal fun Budget.toBudgetClientMap(): Map<String, Any?> = buildMap {
    put("categoryId", categoryId)
    put("limitAmount", limitAmount.value)
    if (periodStart != null && periodEndExclusive != null && periodBasis != null) {
        put("periodKey", periodKey)
        put("periodStart", Timestamp(Date.from(periodStart)))
        put("periodEndExclusive", Timestamp(Date.from(periodEndExclusive)))
        put("periodBasis", periodBasis)
        month?.let { put("month", it.toString()) }
    } else {
        // Preserve update compatibility for unmigrated calendar-month documents.
        month?.let { put("month", it.toString()) }
    }
}

private fun DocumentSnapshot.getInstant(field: String): Instant? =
    getTimestamp(field)?.toDate()?.toInstant()
        ?: getLong(field)?.let(Instant::ofEpochMilli)

internal fun DocumentSnapshot.toBudget(): Budget? = runCatching {
    val legacyMonthString = getString("month")
    val parsedMonth = if (!legacyMonthString.isNullOrEmpty()) YearMonth.parse(legacyMonthString) else null
    val rawPeriodKey = getString("periodKey")
    val pKey = when {
        !rawPeriodKey.isNullOrBlank() -> rawPeriodKey
        !legacyMonthString.isNullOrBlank() -> "month:$legacyMonthString"
        else -> ""
    }
    Budget(
        id = id,
        categoryId = requireNotNull(getString("categoryId")),
        periodKey = pKey,
        periodStart = getInstant("periodStart"),
        periodEndExclusive = getInstant("periodEndExclusive"),
        periodBasis = getString("periodBasis"),
        month = parsedMonth,
        limitAmount = Money(getLong("limitAmount") ?: 0L),
        spentAmount = Money(getLong("spentAmount") ?: 0L),
        notified80 = getBoolean("notified80") ?: false,
        notified100 = getBoolean("notified100") ?: false,
    )
}.getOrNull()
