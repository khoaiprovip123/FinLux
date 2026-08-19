package com.finlux.app.data.remote.firebase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.WalletRepository
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

class FirebaseWalletRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : WalletRepository {

    override fun observeWallets(): Flow<List<Wallet>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            close()
            return@callbackFlow
        }
        val registration = firestore.userWallets(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
            } else {
                val wallets = snapshot?.documents.orEmpty().mapNotNull { it.toWallet() }
                trySend(wallets)
            }
        }
        awaitClose { registration.remove() }
    }

    override suspend fun upsertWallet(wallet: Wallet): AppResult<String> = firebaseResult("Không thể lưu ví") {
        val uid = requireUid()
        val id = wallet.id.ifBlank { UUID.randomUUID().toString() }
        val targetDoc = firestore.userWallets(uid).document(id)
        if (wallet.isDefault) {
            val allWalletsSnapshot = firestore.userWallets(uid).get().await()
            firestore.runBatch { batch ->
                allWalletsSnapshot.documents.forEach { doc ->
                    if (doc.id != id) {
                        batch.update(doc.reference, "isDefault", false)
                    }
                }
                batch.set(targetDoc, wallet.copy(id = id, isDefault = true).toWalletMap())
            }.await()
        } else {
            targetDoc.set(wallet.copy(id = id).toWalletMap()).await()
        }
        id
    }

    override suspend fun deleteWallet(wallet: Wallet): AppResult<Unit> = firebaseResult("Không thể xóa ví") {
        require(!wallet.isDefault) { "Không thể xóa ví mặc định" }
        val uid = requireUid()
        val usedAsSource = firestore.userTransactions(uid).whereEqualTo("walletId", wallet.id).limit(1).get().await()
        val usedAsRelated = firestore.userTransactions(uid).whereEqualTo("relatedWalletId", wallet.id).limit(1).get().await()
        require(usedAsSource.isEmpty && usedAsRelated.isEmpty) { "Ví đã có giao dịch, không thể xóa" }
        firestore.userWallets(uid).document(wallet.id).delete().await()
        Unit
    }

    private fun requireUid(): String = auth.currentUser?.uid ?: error("Phiên đăng nhập đã hết hạn")
}

internal fun Wallet.toWalletMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "type" to type.name.lowercase(),
    "balance" to balance.value,
    "color" to colorHex,
    "isDefault" to isDefault,
    "createdAt" to Timestamp(Date.from(createdAt)),
)

internal fun DocumentSnapshot.toWallet(): Wallet? = runCatching {
    val name = getString("name") ?: getString("walletName") ?: "Tiền mặt"
    val rawType = getString("type") ?: getString("walletType") ?: "cash"
    val type = parseWalletType(rawType)
    val balanceVal = getLong("balance") ?: getDouble("balance")?.toLong() ?: 0L
    val color = getString("color") ?: getString("colorHex") ?: "#1F6FBF"
    val isDef = getBoolean("isDefault") ?: (id == "cash")
    val created = getTimestamp("createdAt")?.toDate()?.toInstant() ?: Instant.now()
    Wallet(
        id = id,
        name = name,
        type = type,
        balance = Money(balanceVal),
        colorHex = color,
        isDefault = isDef,
        createdAt = created,
    )
}.getOrNull()

private fun parseWalletType(raw: String?): WalletType {
    if (raw.isNullOrBlank()) return WalletType.CASH
    return when (raw.trim().uppercase()) {
        "CASH", "TIEN_MAT", "TIỀN MẶT", "TIENMAT", "VI_TIEN_MAT", "VÍ TIỀN MẶT", "GENERAL" -> WalletType.CASH
        "BANK", "NGAN_HANG", "NGÂN HÀNG", "NGANHANG", "ACCOUNT" -> WalletType.BANK
        "EWALLET", "E_WALLET", "VI_DIEN_TU", "VÍ ĐIỆN TỬ", "MOMO", "ZALOPAY" -> WalletType.EWALLET
        "CARD", "CREDIT", "CREDIT_CARD", "DEBIT", "THE_TIN_DUNG", "THẺ TÍN DỤNG" -> WalletType.CARD
        "INVESTMENT", "DAU_TU", "ĐẦU TƯ", "SAVINGS", "TIET_KIEM", "TIẾT KIỆM" -> WalletType.INVESTMENT
        else -> try {
            WalletType.valueOf(raw.trim().uppercase())
        } catch (_: Exception) {
            WalletType.CASH
        }
    }
}
