package com.finlux.app.data.remote.firebase

import com.finlux.app.core.time.FinanceTime
import com.finlux.app.domain.model.DashboardSummary
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.repository.DashboardRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.YearMonth
import java.util.Date

class FirebaseDashboardRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : DashboardRepository {

    override fun observeCurrentMonthSummary(): Flow<DashboardSummary> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            close()
            return@callbackFlow
        }
        val month = YearMonth.now(FinanceTime.defaultZone)
        val start = FinanceTime.monthStart(month)
        val end = FinanceTime.monthEnd(month)
        val registration = firestore.userTransactions(uid)
            .whereGreaterThanOrEqualTo("date", Timestamp(Date.from(start)))
            .whereLessThan("date", Timestamp(Date.from(end)))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else {
                    val items = snapshot?.documents.orEmpty().mapNotNull { it.toFinanceTransaction() }
                    val income = items.filter { it.type == TransactionType.INCOME }.sumOf { it.amount.value }
                    val expense = items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount.value }
                    trySend(DashboardSummary(Money(income), Money(expense), income - expense))
                }
            }
        awaitClose { registration.remove() }
    }
}
