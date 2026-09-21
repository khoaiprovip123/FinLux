package com.finlux.app.domain.usecase.account

import com.finlux.app.core.common.AppResult
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PurgeUserDataUseCase @Inject constructor(
    private val firestore: FirebaseFirestore?,
    private val storage: FirebaseStorage?,
) {
    suspend operator fun invoke(uid: String): AppResult<Unit> = runCatching {
        require(uid.isNotBlank()) { "User ID không được để trống" }

        // 1. Firebase Storage: Purge receipts & avatar
        purgeStorage(uid)

        // 2. Cloud Firestore: Reverse wipe from leaf to root
        purgeFirestore(uid)

        Unit
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(it.localizedMessage ?: "Lỗi khi xóa dữ liệu người dùng", it) }
    )

    private suspend fun purgeStorage(uid: String) {
        val s = storage ?: return
        // Avatars
        runCatching {
            s.reference.child("avatars/$uid.jpg").delete().await()
        }

        // Receipts
        runCatching {
            val receiptsRef = s.reference.child("receipts/$uid")
            val listResult = receiptsRef.listAll().await()
            listResult.items.forEach { item ->
                runCatching { item.delete().await() }
            }
            listResult.prefixes.forEach { prefix ->
                runCatching {
                    val subList = prefix.listAll().await()
                    subList.items.forEach { subItem ->
                        runCatching { subItem.delete().await() }
                    }
                }
            }
        }
    }

    private suspend fun purgeFirestore(uid: String) {
        val f = firestore ?: return
        val userDoc = f.collection("users").document(uid)

        // Step 1: Deals
        deleteCollectionInBatches(userDoc.collection("deals"))

        // Step 2: Debts & Payments
        val debtsCollection = userDoc.collection("debts")
        val debtDocs = debtsCollection.get().await()
        for (debtDoc in debtDocs) {
            deleteCollectionInBatches(debtDoc.reference.collection("payments"))
        }
        deleteInBatches(debtDocs.map { it.reference })

        // Step 3: Transactions
        deleteCollectionInBatches(userDoc.collection("transactions"))

        // Step 4: Budgets
        deleteCollectionInBatches(userDoc.collection("budgets"))

        // Step 5: Goals
        deleteCollectionInBatches(userDoc.collection("goals"))

        // Step 6: Reminders
        deleteCollectionInBatches(userDoc.collection("reminders"))

        // Step 7: Notifications
        deleteCollectionInBatches(userDoc.collection("notifications"))

        // Step 8: Salary Rollovers
        deleteCollectionInBatches(userDoc.collection("salaryRollovers"))

        // Step 9: Salary Cycle Timeline
        deleteCollectionInBatches(userDoc.collection("salaryCycleTimeline"))

        // Step 10: Financial Preferences
        deleteCollectionInBatches(userDoc.collection("financialPreferences"))

        // Step 11: Saving Spin (configs, destinations, sessions)
        deleteCollectionInBatches(userDoc.collection("savingSpinConfigs"))
        deleteCollectionInBatches(userDoc.collection("savingSpinDestinations"))
        deleteCollectionInBatches(userDoc.collection("savingSpinSessions"))

        // Step 12: Categories
        deleteCollectionInBatches(userDoc.collection("categories"))

        // Step 13: Wallets
        deleteCollectionInBatches(userDoc.collection("wallets"))

        // Step 14: Root user profile document
        userDoc.delete().await()
    }

    private suspend fun deleteCollectionInBatches(collectionRef: CollectionReference) {
        val snapshot = collectionRef.get().await()
        deleteInBatches(snapshot.map { it.reference })
    }

    private suspend fun deleteInBatches(refs: List<DocumentReference>) {
        if (refs.isEmpty()) return
        // Chunk by 400 (Firestore maximum batch size is 500)
        refs.chunked(400).forEach { chunk ->
            val batch = firestore?.batch() ?: return
            chunk.forEach { ref -> batch.delete(ref) }
            batch.commit().await()
        }
    }
}
