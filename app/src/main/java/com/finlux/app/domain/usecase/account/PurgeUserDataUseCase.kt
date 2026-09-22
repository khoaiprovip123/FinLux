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
        val userDoc = f.collection(com.finlux.app.data.remote.firebase.schema.FirestoreSchema.USERS).document(uid)

        // Leaf collections under debts (e.g. debt payments)
        val debtsCollection = userDoc.collection(com.finlux.app.data.remote.firebase.schema.FirestoreSchema.Collections.DEBTS)
        val debtDocs = debtsCollection.get().await()
        for (debtDoc in debtDocs) {
            deleteCollectionInBatches(debtDoc.reference.collection(com.finlux.app.data.remote.firebase.schema.FirestoreSchema.Collections.DEBT_PAYMENTS))
        }

        // Delete all standard user subcollections from leaf to root
        for (collectionName in com.finlux.app.data.remote.firebase.schema.FirestoreSchema.ALL_USER_SUBCOLLECTIONS) {
            deleteCollectionInBatches(userDoc.collection(collectionName))
        }

        // Root user profile document
        userDoc.delete().await()
    }

    private suspend fun deleteCollectionInBatches(collectionRef: CollectionReference) {
        val snapshot = collectionRef.get().await()
        deleteInBatches(snapshot.map { it.reference })
    }

    private suspend fun deleteInBatches(refs: List<DocumentReference>) {
        if (refs.isEmpty()) return
        refs.chunked(com.finlux.app.core.common.AppSystemConfig.Firestore.BATCH_WRITE_CHUNK_SIZE).forEach { chunk ->
            val batch = firestore?.batch() ?: return
            chunk.forEach { ref -> batch.delete(ref) }
            batch.commit().await()
        }
    }
}
