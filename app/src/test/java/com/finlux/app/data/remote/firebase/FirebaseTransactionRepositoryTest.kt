package com.finlux.app.data.remote.firebase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class FirebaseTransactionRepositoryTest {
    private val auth: FirebaseAuth = mockk()
    private val firestore: FirebaseFirestore = mockk()
    private val user: FirebaseUser = mockk()

    private lateinit var repository: FirebaseTransactionRepository

    @BeforeEach
    fun setUp() {
        repository = FirebaseTransactionRepository(auth, firestore)
    }

    @Test
    fun `addWithBalanceUpdate returns error when user not signed in`() = runTest {
        every { auth.currentUser } returns null

        val sampleTx = sampleTransaction()
        val result = repository.addWithBalanceUpdate(sampleTx)

        assertInstanceOf(AppResult.Error::class.java, result)
        assertEquals("Phiên đăng nhập đã hết hạn", (result as AppResult.Error).message)
    }

    @Test
    fun `addWithBalanceUpdate executes atomic transaction successfully`() = runTest {
        val uid = "test_uid"
        every { auth.currentUser } returns user
        every { user.uid } returns uid

        val userDocRef: DocumentReference = mockk()
        val transactionsColl: CollectionReference = mockk()
        val walletsColl: CollectionReference = mockk()
        val budgetsColl: CollectionReference = mockk()
        val transactionDocRef: DocumentReference = mockk()
        val walletDocRef: DocumentReference = mockk()
        val budgetDocRef: DocumentReference = mockk(relaxed = true)
        val walletSnapshot: DocumentSnapshot = mockk()

        every { firestore.collection("users").document(uid) } returns userDocRef
        every { userDocRef.collection("transactions") } returns transactionsColl
        every { userDocRef.collection("wallets") } returns walletsColl
        every { userDocRef.collection("budgets") } returns budgetsColl
        every { transactionsColl.document() } returns transactionDocRef
        every { transactionDocRef.id } returns "new_tx_id"
        every { walletsColl.document("wallet_1") } returns walletDocRef
        every { budgetsColl.document(any()) } returns budgetDocRef
        every { walletSnapshot.getLong("balance") } returns 1_000_000L

        val transactionSlot = slot<Transaction.Function<Any?>>()
        every { firestore.runTransaction<Any?>(capture(transactionSlot)) } answers {
            val atomicTx: Transaction = mockk(relaxed = true)
            every { atomicTx.get(walletDocRef) } returns walletSnapshot
            transactionSlot.captured.apply(atomicTx)
            Tasks.forResult<Any?>(null)
        }

        val sampleTx = sampleTransaction(walletId = "wallet_1", amount = 200_000L)
        val result = repository.addWithBalanceUpdate(sampleTx)

        assertInstanceOf(AppResult.Success::class.java, result)
        assertEquals("new_tx_id", (result as AppResult.Success).value)
        verify { firestore.runTransaction<Any?>(any()) }
    }

    @Test
    fun `deleteWithBalanceUpdate executes atomic transaction successfully`() = runTest {
        val uid = "test_uid"
        every { auth.currentUser } returns user
        every { user.uid } returns uid

        val userDocRef: DocumentReference = mockk()
        val transactionsColl: CollectionReference = mockk()
        val walletsColl: CollectionReference = mockk()
        val budgetsColl: CollectionReference = mockk()
        val transactionDocRef: DocumentReference = mockk()
        val walletDocRef: DocumentReference = mockk()
        val budgetDocRef: DocumentReference = mockk(relaxed = true)
        val txSnapshot: DocumentSnapshot = mockk()
        val walletSnapshot: DocumentSnapshot = mockk()

        every { firestore.collection("users").document(uid) } returns userDocRef
        every { userDocRef.collection("transactions") } returns transactionsColl
        every { userDocRef.collection("wallets") } returns walletsColl
        every { userDocRef.collection("budgets") } returns budgetsColl
        every { transactionsColl.document("tx_123") } returns transactionDocRef
        every { walletsColl.document("wallet_1") } returns walletDocRef
        every { budgetsColl.document(any()) } returns budgetDocRef

        val nowTimestamp = Timestamp.now()
        every { txSnapshot.id } returns "tx_123"
        every { txSnapshot.getString("id") } returns "tx_123"
        every { txSnapshot.getString("walletId") } returns "wallet_1"
        every { txSnapshot.getLong("amount") } returns 150_000L
        every { txSnapshot.getString("type") } returns "expense"
        every { txSnapshot.getString("note") } returns "Ăn trưa"
        every { txSnapshot.getTimestamp("date") } returns nowTimestamp
        every { txSnapshot.getTimestamp("createdAt") } returns nowTimestamp
        every { txSnapshot.getTimestamp("updatedAt") } returns nowTimestamp
        every { txSnapshot.getString("categoryId") } returns "cat_food"
        every { txSnapshot.getString("relatedWalletId") } returns null
        every { txSnapshot.getString("receiptImageUrl") } returns null

        every { walletSnapshot.getLong("balance") } returns 500_000L

        val transactionSlot = slot<Transaction.Function<Any?>>()
        every { firestore.runTransaction<Any?>(capture(transactionSlot)) } answers {
            val atomicTx: Transaction = mockk(relaxed = true)
            every { atomicTx.get(transactionDocRef) } returns txSnapshot
            every { atomicTx.get(walletDocRef) } returns walletSnapshot
            transactionSlot.captured.apply(atomicTx)
            Tasks.forResult<Any?>(null)
        }

        val sampleTx = sampleTransaction(id = "tx_123", walletId = "wallet_1", amount = 150_000L)
        val result = repository.deleteWithBalanceUpdate(sampleTx)

        assertInstanceOf(AppResult.Success::class.java, result)
        verify { firestore.runTransaction<Any?>(any()) }
    }

    private fun sampleTransaction(
        id: String = "",
        walletId: String = "wallet_1",
        amount: Long = 100_000L,
    ) = FinanceTransaction(
        id = id,
        amount = Money(amount),
        type = TransactionType.EXPENSE,
        categoryId = "cat_food",
        walletId = walletId,
        note = "Ăn trưa",
        date = Instant.now(),
    )
}
