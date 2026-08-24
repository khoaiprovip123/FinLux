package com.finlux.app.data.remote.firebase

import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.repository.TransactionRangeRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.time.Instant
import java.util.Date
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseTransactionRangeRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : TransactionRangeRepository {
    override fun observeRange(
        startInclusive: Instant,
        endExclusive: Instant,
    ): Flow<List<FinanceTransaction>> = callbackFlow {
        require(startInclusive < endExclusive) { "Khoảng thời gian giao dịch không hợp lệ" }
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.userTransactions(uid)
            .whereGreaterThanOrEqualTo("date", Timestamp(Date.from(startInclusive)))
            .whereLessThan("date", Timestamp(Date.from(endExclusive)))
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) close(error)
                else trySend(snapshot?.documents.orEmpty().mapNotNull { it.toFinanceTransaction() })
            }
        awaitClose { registration.remove() }
    }
}
