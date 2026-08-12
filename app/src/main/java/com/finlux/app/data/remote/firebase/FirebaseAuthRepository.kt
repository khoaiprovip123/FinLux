package com.finlux.app.data.remote.firebase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.UserProfile
import com.finlux.app.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
) : AuthRepository {
    override val currentUser: Flow<UserProfile?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.toDomain())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signIn(email: String, password: String): AppResult<UserProfile> = runCatching {
        val user = auth.signInWithEmailAndPassword(email.trim(), password).await().user
            ?: error("Firebase không trả về người dùng")
        user.toDomain()
    }.fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Error(it.localizedMessage ?: "Đăng nhập thất bại", it) },
    )

    override suspend fun register(
        displayName: String,
        email: String,
        password: String,
    ): AppResult<UserProfile> = runCatching {
        val user = auth.createUserWithEmailAndPassword(email.trim(), password).await().user
            ?: error("Firebase không trả về người dùng")
        user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(displayName.trim()).build()).await()
        seedNewUser(user.uid, displayName.trim(), email.trim())
        user.toDomain().copy(displayName = displayName.trim())
    }.fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Error(it.localizedMessage ?: "Không thể tạo tài khoản", it) },
    )

    override suspend fun sendPasswordReset(email: String): AppResult<Unit> = runCatching {
        auth.sendPasswordResetEmail(email.trim()).await()
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(it.localizedMessage ?: "Không thể gửi email", it) },
    )

    override suspend fun updateAvatar(jpegBytes: ByteArray): AppResult<UserProfile> = runCatching {
        val user = auth.currentUser ?: error("Chưa đăng nhập")
        val reference = storage.reference.child("avatars/${user.uid}.jpg")
        reference.putBytes(jpegBytes).await()
        val downloadUrl = reference.downloadUrl.await()
        user.updateProfile(UserProfileChangeRequest.Builder().setPhotoUri(downloadUrl).build()).await()
        firestore.collection("users").document(user.uid).update("photoUrl", downloadUrl.toString()).await()
        user.toDomain().copy(photoUrl = downloadUrl.withCacheVersion().toString())
    }.fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Error(it.localizedMessage ?: "Không thể cập nhật ảnh đại diện", it) },
    )

    override suspend fun signOut() = auth.signOut()

    /** BR-02: profile, default wallet and categories are committed together. */
    private suspend fun seedNewUser(uid: String, displayName: String, email: String) {
        val user = firestore.collection("users").document(uid)
        firestore.batch().apply {
            set(user, mapOf(
                "displayName" to displayName,
                "email" to email,
                "photoUrl" to "",
                "createdAt" to FieldValue.serverTimestamp(),
            ))
            set(user.collection("wallets").document("cash"), mapOf(
                "name" to "Tiền mặt", "type" to "cash", "balance" to 0L,
                "color" to "#1F6FBF", "isDefault" to true,
                "createdAt" to FieldValue.serverTimestamp(),
            ))
            defaultCategories.forEach { (id, values) ->
                set(user.collection("categories").document(id), values + ("createdAt" to FieldValue.serverTimestamp()))
            }
        }.commit().await()
    }

    private fun com.google.firebase.auth.FirebaseUser.toDomain() = UserProfile(
        uid = uid,
        displayName = displayName.orEmpty().ifBlank { email.orEmpty().substringBefore('@') },
        email = email.orEmpty(),
        photoUrl = photoUrl?.toString(),
    )

    private fun Uri.withCacheVersion(): Uri = buildUpon()
        .appendQueryParameter("v", System.currentTimeMillis().toString())
        .build()

    private companion object {
        val defaultCategories = mapOf(
            "food" to mapOf("name" to "Ăn uống", "type" to "expense", "icon" to "restaurant", "color" to "#D94B5B", "isDefault" to true),
            "transport" to mapOf("name" to "Di chuyển", "type" to "expense", "icon" to "directions_car", "color" to "#E6A23C", "isDefault" to true),
            "salary" to mapOf("name" to "Lương", "type" to "income", "icon" to "payments", "color" to "#168A62", "isDefault" to true),
        )
    }
}
