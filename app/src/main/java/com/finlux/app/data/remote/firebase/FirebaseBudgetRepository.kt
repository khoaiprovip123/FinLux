package com.finlux.app.data.remote.firebase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.repository.BudgetRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.YearMonth

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

        val registration = firestore.collection("users").document(uid).collection("budgets")
            .whereIn("periodKey", keysToMatch)
            .addSnapshotListener { snapshot, error ->
                if (error != null) close(error)
                else trySend(snapshot?.documents.orEmpty().mapNotNull { it.toBudget() })
            }
        awaitClose { registration.remove() }
    }

    override suspend fun upsertBudget(budget: Budget): AppResult<String> = firebaseResult("Không thể lưu ngân sách") {
        val uid = requireUid()
        val id = budget.id.ifBlank { "${budget.categoryId}_${budget.periodKey}" }
        firestore.collection("users").document(uid).collection("budgets").document(id)
            .set(budget.copy(id = id).toBudgetMap()).await()
        id
    }

    override suspend fun deleteBudget(budget: Budget): AppResult<Unit> = firebaseResult("Không thể xóa ngân sách") {
        val uid = requireUid()
        firestore.collection("users").document(uid).collection("budgets").document(budget.id).delete().await()
        Unit
    }

    private fun requireUid(): String = auth.currentUser?.uid ?: error("Phiên đăng nhập đã hết hạn")
}

internal fun Budget.toBudgetMap(): Map<String, Any?> = mapOf(
    "categoryId" to categoryId,
    "periodKey" to periodKey,
    "periodStart" to periodStart?.toEpochMilli(),
    "periodEndExclusive" to periodEndExclusive?.toEpochMilli(),
    "periodBasis" to periodBasis,
    "month" to month?.toString(), // Deprecated but keep for old clients
    "limitAmount" to limitAmount.value,
    "spentAmount" to spentAmount.value,
    "notified80" to notified80,
    "notified100" to notified100,
)

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
        month = parsedMonth,
        limitAmount = Money(getLong("limitAmount") ?: 0L),
        spentAmount = Money(getLong("spentAmount") ?: 0L),
        notified80 = getBoolean("notified80") ?: false,
        notified100 = getBoolean("notified100") ?: false,
    )
}.getOrNull()
