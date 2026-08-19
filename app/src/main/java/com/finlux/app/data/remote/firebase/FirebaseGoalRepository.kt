package com.finlux.app.data.remote.firebase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinancialGoal
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.repository.GoalRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.util.Date
import java.util.UUID

class FirebaseGoalRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : GoalRepository {

    override fun observeGoals(): Flow<List<FinancialGoal>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("users").document(uid).collection("goals")
            .addSnapshotListener { snapshot, error ->
                if (error != null) close(error)
                else trySend(snapshot?.documents.orEmpty().mapNotNull { it.toGoal() })
            }
        awaitClose { registration.remove() }
    }

    override suspend fun upsertGoal(goal: FinancialGoal): AppResult<String> = firebaseResult("Không thể lưu mục tiêu") {
        val uid = requireUid()
        val id = goal.id.ifBlank { UUID.randomUUID().toString() }
        firestore.collection("users").document(uid).collection("goals").document(id)
            .set(goal.copy(id = id).toGoalMap()).await()
        id
    }

    override suspend fun deleteGoal(goal: FinancialGoal): AppResult<Unit> = firebaseResult("Không thể xóa mục tiêu") {
        val uid = requireUid()
        firestore.collection("users").document(uid).collection("goals").document(goal.id).delete().await()
        Unit
    }

    private fun requireUid(): String = auth.currentUser?.uid ?: error("Phiên đăng nhập đã hết hạn")
}

internal fun FinancialGoal.toGoalMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "targetAmount" to targetAmount.value,
    "savedAmount" to savedAmount.value,
    "deadline" to Timestamp(Date.from(deadline)),
    "category" to category,
    "monthlyContribution" to monthlyContribution.value,
    "imageUri" to imageUri,
    "createdAt" to Timestamp(Date.from(createdAt)),
)

internal fun DocumentSnapshot.toGoal(): FinancialGoal? = runCatching {
    FinancialGoal(
        id = id,
        name = requireNotNull(getString("name")),
        targetAmount = Money(getLong("targetAmount") ?: 0L),
        savedAmount = Money(getLong("savedAmount") ?: 0L),
        deadline = requireNotNull(getTimestamp("deadline")).toDate().toInstant(),
        category = getString("category") ?: "Khác",
        monthlyContribution = Money(getLong("monthlyContribution") ?: 0L),
        imageUri = getString("imageUri"),
        createdAt = getTimestamp("createdAt")?.toDate()?.toInstant() ?: Instant.now(),
    )
}.getOrNull()
