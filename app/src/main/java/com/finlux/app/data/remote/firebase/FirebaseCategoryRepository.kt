package com.finlux.app.data.remote.firebase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.repository.CategoryRepository
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

class FirebaseCategoryRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : CategoryRepository {

    override fun observeCategories(): Flow<List<Category>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("users").document(uid).collection("categories")
            .addSnapshotListener { snapshot, error ->
                if (error != null) close(error)
                else trySend(snapshot?.documents.orEmpty().mapNotNull { it.toCategory() })
            }
        awaitClose { registration.remove() }
    }

    override suspend fun upsertCategory(category: Category): AppResult<String> = firebaseResult("Không thể lưu danh mục") {
        val uid = requireUid()
        val id = category.id.ifBlank { UUID.randomUUID().toString() }
        firestore.collection("users").document(uid).collection("categories").document(id)
            .set(category.copy(id = id).toCategoryMap()).await()
        id
    }

    override suspend fun deleteCategory(category: Category): AppResult<Unit> = firebaseResult("Không thể xóa danh mục") {
        require(!category.isDefault) { "Không thể xóa danh mục mặc định" }
        val uid = requireUid()
        val used = firestore.userTransactions(uid).whereEqualTo("categoryId", category.id).limit(1).get().await()
        require(used.isEmpty) { "Danh mục đã phát sinh giao dịch, không thể xóa" }
        firestore.collection("users").document(uid).collection("categories").document(category.id).delete().await()
        Unit
    }

    private fun requireUid(): String = auth.currentUser?.uid ?: error("Phiên đăng nhập đã hết hạn")
}

internal fun Category.toCategoryMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "type" to type.name.lowercase(),
    "icon" to icon,
    "color" to colorHex,
    "isDefault" to isDefault,
    "isEssential" to isEssential,
    "createdAt" to Timestamp(Date.from(createdAt)),
)

internal fun DocumentSnapshot.toCategory(): Category? = runCatching {
    Category(
        id = id,
        name = requireNotNull(getString("name")),
        type = CategoryType.valueOf(requireNotNull(getString("type")).uppercase()),
        icon = getString("icon").orEmpty(),
        colorHex = getString("color") ?: "#1F6FBF",
        isDefault = getBoolean("isDefault") ?: false,
        isEssential = getBoolean("isEssential") ?: true,
        createdAt = getTimestamp("createdAt")?.toDate()?.toInstant() ?: Instant.now(),
    )
}.getOrNull()
