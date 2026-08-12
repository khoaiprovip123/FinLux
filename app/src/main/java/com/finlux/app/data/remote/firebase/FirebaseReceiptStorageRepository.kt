package com.finlux.app.data.remote.firebase

import androidx.core.net.toUri
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.repository.ReceiptStorageRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseReceiptStorageRepository(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
) : ReceiptStorageRepository {
    override suspend fun uploadReceipt(localUri: String): AppResult<String> = firebaseResult("Không thể tải ảnh hóa đơn") {
        val uid = auth.currentUser?.uid ?: error("Phiên đăng nhập đã hết hạn")
        val ref = storage.reference.child("receipts/$uid/${UUID.randomUUID()}.jpg")
        ref.putFile(localUri.toUri()).await()
        ref.downloadUrl.await().toString()
    }
}
