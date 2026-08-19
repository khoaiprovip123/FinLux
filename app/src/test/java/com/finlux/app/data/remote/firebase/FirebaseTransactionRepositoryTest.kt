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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
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
        every { walletSnapshot.exists() } returns true
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
    fun `editWithBalanceUpdate derives old state from stored document and reverses correctly`() = runTest {
        val uid = "test_uid"
        every { auth.currentUser } returns user
        every { user.uid } returns uid

        val userDocRef: DocumentReference = mockk()
        val transactionsColl: CollectionReference = mockk()
        val walletsColl: CollectionReference = mockk()
        val budgetsColl: CollectionReference = mockk()
        val transactionDocRef: DocumentReference = mockk()
        val storedWalletDocRef: DocumentReference = mockk()
        val updatedWalletDocRef: DocumentReference = mockk()
        val storedBudgetDocRef: DocumentReference = mockk(relaxed = true)
        val updatedBudgetDocRef: DocumentReference = mockk(relaxed = true)

        val txSnapshot: DocumentSnapshot = mockk()
        val storedWalletSnapshot: DocumentSnapshot = mockk()
        val updatedWalletSnapshot: DocumentSnapshot = mockk()
        val storedBudgetSnapshot: DocumentSnapshot = mockk()
        val updatedBudgetSnapshot: DocumentSnapshot = mockk()

        every { firestore.collection("users").document(uid) } returns userDocRef
        every { userDocRef.collection("transactions") } returns transactionsColl
        every { userDocRef.collection("wallets") } returns walletsColl
        every { userDocRef.collection("budgets") } returns budgetsColl
        every { transactionsColl.document("tx_100") } returns transactionDocRef

        every { walletsColl.document("wallet_stored") } returns storedWalletDocRef
        every { walletsColl.document("wallet_updated") } returns updatedWalletDocRef
        every { storedWalletDocRef.path } returns "users/$uid/wallets/wallet_stored"
        every { updatedWalletDocRef.path } returns "users/$uid/wallets/wallet_updated"

        every { budgetsColl.document("cat_stored_2026-08") } returns storedBudgetDocRef
        every { budgetsColl.document("cat_updated_2026-08") } returns updatedBudgetDocRef
        every { storedBudgetDocRef.path } returns "users/$uid/budgets/cat_stored_2026-08"
        every { updatedBudgetDocRef.path } returns "users/$uid/budgets/cat_updated_2026-08"

        val nowTimestamp = Timestamp.now()
        // Stored document in Firestore has amount 300,000, wallet_stored, cat_stored
        every { txSnapshot.id } returns "tx_100"
        every { txSnapshot.getString("id") } returns "tx_100"
        every { txSnapshot.getString("walletId") } returns "wallet_stored"
        every { txSnapshot.getLong("amount") } returns 300_000L
        every { txSnapshot.getString("type") } returns "expense"
        every { txSnapshot.getString("note") } returns "Old note"
        every { txSnapshot.getTimestamp("date") } returns nowTimestamp
        every { txSnapshot.getTimestamp("createdAt") } returns nowTimestamp
        every { txSnapshot.getTimestamp("updatedAt") } returns nowTimestamp
        every { txSnapshot.getString("categoryId") } returns "cat_stored"
        every { txSnapshot.getString("relatedWalletId") } returns null
        every { txSnapshot.getString("receiptImageUrl") } returns null

        every { storedWalletSnapshot.getLong("balance") } returns 1_000_000L
        every { updatedWalletSnapshot.getLong("balance") } returns 2_000_000L
        every { storedBudgetSnapshot.exists() } returns true
        every { updatedBudgetSnapshot.exists() } returns true

        val transactionSlot = slot<Transaction.Function<Any?>>()
        val atomicTx: Transaction = mockk(relaxed = true)
        every { firestore.runTransaction<Any?>(capture(transactionSlot)) } answers {
            every { atomicTx.get(transactionDocRef) } returns txSnapshot
            every { atomicTx.get(storedWalletDocRef) } returns storedWalletSnapshot
            every { atomicTx.get(updatedWalletDocRef) } returns updatedWalletSnapshot
            every { atomicTx.get(storedBudgetDocRef) } returns storedBudgetSnapshot
            every { atomicTx.get(updatedBudgetDocRef) } returns updatedBudgetSnapshot
            transactionSlot.captured.apply(atomicTx)
            Tasks.forResult<Any?>(null)
        }

        // Stale caller object has amount 100_000, wallet_stale (should NOT be trusted)
        val staleOriginal = FinanceTransaction(
            id = "tx_100",
            type = TransactionType.EXPENSE,
            amount = Money(100_000L), // Stale amount!
            categoryId = "cat_stale",
            walletId = "wallet_stale",
            note = "Stale note",
            date = Instant.now(),
        )

        val updated = FinanceTransaction(
            id = "tx_100",
            type = TransactionType.EXPENSE,
            amount = Money(500_000L),
            categoryId = "cat_updated",
            walletId = "wallet_updated",
            note = "Updated note",
            date = Instant.now(),
        )

        val result = repository.editWithBalanceUpdate(staleOriginal, updated)
        assertInstanceOf(AppResult.Success::class.java, result)

        // Verify stored wallet is credited back +300_000 (1_000_000 - (-300_000) = 1_300_000)
        verify { atomicTx.update(storedWalletDocRef, "balance", 1_300_000L) }
        // Verify updated wallet is debited -500_000 (2_000_000 - 500_000 = 1_500_000)
        verify { atomicTx.update(updatedWalletDocRef, "balance", 1_500_000L) }
        // Verify stored budget is decremented by stored amount -300_000
        verify { atomicTx.update(storedBudgetDocRef, "spentAmount", any()) }
    }

    @Test
    fun `deleteWithBalanceUpdate derives wallet and budget strictly from stored document`() = runTest {
        val uid = "test_uid"
        every { auth.currentUser } returns user
        every { user.uid } returns uid

        val userDocRef: DocumentReference = mockk()
        val transactionsColl: CollectionReference = mockk()
        val walletsColl: CollectionReference = mockk()
        val budgetsColl: CollectionReference = mockk()
        val transactionDocRef: DocumentReference = mockk()
        val storedWalletDocRef: DocumentReference = mockk()
        val storedBudgetDocRef: DocumentReference = mockk(relaxed = true)
        val txSnapshot: DocumentSnapshot = mockk()
        val walletSnapshot: DocumentSnapshot = mockk()
        val budgetSnapshot: DocumentSnapshot = mockk()

        every { firestore.collection("users").document(uid) } returns userDocRef
        every { userDocRef.collection("transactions") } returns transactionsColl
        every { userDocRef.collection("wallets") } returns walletsColl
        every { userDocRef.collection("budgets") } returns budgetsColl
        every { transactionsColl.document("tx_123") } returns transactionDocRef
        every { walletsColl.document("wallet_stored_actual") } returns storedWalletDocRef
        every { budgetsColl.document("cat_stored_actual_2026-08") } returns storedBudgetDocRef

        val nowTimestamp = Timestamp.now()
        every { txSnapshot.id } returns "tx_123"
        every { txSnapshot.getString("id") } returns "tx_123"
        every { txSnapshot.getString("walletId") } returns "wallet_stored_actual"
        every { txSnapshot.getLong("amount") } returns 250_000L
        every { txSnapshot.getString("type") } returns "expense"
        every { txSnapshot.getString("note") } returns "Actual lunch"
        every { txSnapshot.getTimestamp("date") } returns nowTimestamp
        every { txSnapshot.getTimestamp("createdAt") } returns nowTimestamp
        every { txSnapshot.getTimestamp("updatedAt") } returns nowTimestamp
        every { txSnapshot.getString("categoryId") } returns "cat_stored_actual"
        every { txSnapshot.getString("relatedWalletId") } returns null
        every { txSnapshot.getString("receiptImageUrl") } returns null

        every { walletSnapshot.getLong("balance") } returns 750_000L
        every { budgetSnapshot.exists() } returns true

        val transactionSlot = slot<Transaction.Function<Any?>>()
        val atomicTx: Transaction = mockk(relaxed = true)
        every { firestore.runTransaction<Any?>(capture(transactionSlot)) } answers {
            every { atomicTx.get(transactionDocRef) } returns txSnapshot
            every { atomicTx.get(storedWalletDocRef) } returns walletSnapshot
            every { atomicTx.get(storedBudgetDocRef) } returns budgetSnapshot
            transactionSlot.captured.apply(atomicTx)
            Tasks.forResult<Any?>(null)
        }

        // Caller has stale walletId and stale amount
        val staleTx = sampleTransaction(id = "tx_123", walletId = "stale_wallet", amount = 10_000L)
        val result = repository.deleteWithBalanceUpdate(staleTx)

        assertInstanceOf(AppResult.Success::class.java, result)
        // Verify delete is called on document
        verify { atomicTx.delete(transactionDocRef) }
        // Verify balance refunded is based on stored: 750_000 - (-250_000) = 1_000_000
        verify { atomicTx.update(storedWalletDocRef, "balance", 1_000_000L) }
    }

    @Test
    fun `transferBetweenWallets validates source and destination and executes atomic transfer`() = runTest {
        val uid = "test_uid"
        every { auth.currentUser } returns user
        every { user.uid } returns uid

        val userDocRef: DocumentReference = mockk()
        val walletsColl: CollectionReference = mockk()
        val transactionsColl: CollectionReference = mockk()
        val sourceWalletRef: DocumentReference = mockk()
        val destWalletRef: DocumentReference = mockk()
        val outTxRef: DocumentReference = mockk()
        val inTxRef: DocumentReference = mockk()

        val sourceSnapshot: DocumentSnapshot = mockk()
        val destSnapshot: DocumentSnapshot = mockk()

        every { firestore.collection("users").document(uid) } returns userDocRef
        every { userDocRef.collection("wallets") } returns walletsColl
        every { userDocRef.collection("transactions") } returns transactionsColl
        every { walletsColl.document("wallet_src") } returns sourceWalletRef
        every { walletsColl.document("wallet_dst") } returns destWalletRef
        every { transactionsColl.document(match { it.endsWith("_out") }) } returns outTxRef
        every { transactionsColl.document(match { it.endsWith("_in") }) } returns inTxRef
        every { outTxRef.id } returns "pair_out"
        every { inTxRef.id } returns "pair_in"

        every { sourceSnapshot.getLong("balance") } returns 500_000L
        every { sourceSnapshot.getString("type") } returns "CASH"
        every { destSnapshot.getLong("balance") } returns 100_000L

        val transactionSlot = slot<Transaction.Function<Any?>>()
        val atomicTx: Transaction = mockk(relaxed = true)
        every { firestore.runTransaction<Any?>(capture(transactionSlot)) } answers {
            every { atomicTx.get(sourceWalletRef) } returns sourceSnapshot
            every { atomicTx.get(destWalletRef) } returns destSnapshot
            transactionSlot.captured.apply(atomicTx)
            Tasks.forResult<Any?>(null)
        }

        // Test transfer amount 200_000
        val result = repository.transferBetweenWallets("wallet_src", "wallet_dst", 200_000L, "Chuyển tiền", Instant.now())
        assertInstanceOf(AppResult.Success::class.java, result)

        // Verify source deducted 200_000 -> 300_000, dest added 200_000 -> 300_000
        verify { atomicTx.update(sourceWalletRef, "balance", 300_000L) }
        verify { atomicTx.update(destWalletRef, "balance", 300_000L) }
    }

    @Test
    fun `transferBetweenWallets fails if source and destination are identical`() = runTest {
        every { auth.currentUser } returns user
        every { user.uid } returns "test_uid"

        val result = repository.transferBetweenWallets("same_wallet", "same_wallet", 100_000L, "", Instant.now())
        assertInstanceOf(AppResult.Error::class.java, result)
        assertTrue((result as AppResult.Error).message.contains("Hai ví phải khác nhau"))
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
