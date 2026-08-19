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
import java.util.Date

class FirebaseTransactionRepositoryTest {
    private val auth: FirebaseAuth = mockk()
    private val firestore: FirebaseFirestore = mockk()
    private val user: FirebaseUser = mockk()

    private lateinit var repository: FirebaseTransactionRepository

    // Fixed deterministic timestamp for tests (P0-T01)
    private val fixedInstant: Instant = Instant.parse("2026-08-15T03:00:00Z")
    private val fixedTimestamp: Timestamp = Timestamp(Date.from(fixedInstant))

    @BeforeEach
    fun setUp() {
        repository = FirebaseTransactionRepository(auth, firestore)
    }

    // ==========================================
    // ADD TRANSACTION TESTS
    // ==========================================

    @Test
    fun `addWithBalanceUpdate returns error when user not signed in`() = runTest {
        every { auth.currentUser } returns null

        val sampleTx = sampleTransaction()
        val result = repository.addWithBalanceUpdate(sampleTx)

        assertInstanceOf(AppResult.Error::class.java, result)
        assertEquals("Phiên đăng nhập đã hết hạn", (result as AppResult.Error).message)
    }

    @Test
    fun `addWithBalanceUpdate rejects zero or negative amount`() = runTest {
        every { auth.currentUser } returns user
        every { user.uid } returns "test_uid"

        val zeroTx = sampleTransaction(amount = 0L)
        val zeroResult = repository.addWithBalanceUpdate(zeroTx)
        assertInstanceOf(AppResult.Error::class.java, zeroResult)
        assertTrue((zeroResult as AppResult.Error).message.contains("Số tiền không hợp lệ"))

        val negativeTx = sampleTransaction(amount = -50_000L)
        val negResult = repository.addWithBalanceUpdate(negativeTx)
        assertInstanceOf(AppResult.Error::class.java, negResult)
    }

    @Test
    fun `addWithBalanceUpdate rejects amount exceeding max limit`() = runTest {
        every { auth.currentUser } returns user
        every { user.uid } returns "test_uid"

        val hugeTx = sampleTransaction(amount = 1_000_000_000_000_000L)
        val result = repository.addWithBalanceUpdate(hugeTx)
        assertInstanceOf(AppResult.Error::class.java, result)
        assertTrue((result as AppResult.Error).message.contains("Số tiền không hợp lệ"))
    }

    @Test
    fun `addWithBalanceUpdate executes atomic transaction successfully for EXPENSE`() = runTest {
        val uid = "test_uid"
        every { auth.currentUser } returns user
        every { user.uid } returns uid

        val userDocRef: DocumentReference = mockk()
        val transactionsColl: CollectionReference = mockk()
        val walletsColl: CollectionReference = mockk()
        val budgetsColl: CollectionReference = mockk()
        val transactionDocRef: DocumentReference = mockk(relaxed = true)
        val walletDocRef: DocumentReference = mockk(relaxed = true)
        val budgetDocRef: DocumentReference = mockk(relaxed = true)
        val walletSnapshot: DocumentSnapshot = mockk()
        val budgetSnapshot: DocumentSnapshot = mockk()

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
        every { budgetSnapshot.exists() } returns true

        val atomicTx: Transaction = mockk(relaxed = true)
        every { atomicTx.get(walletDocRef) } returns walletSnapshot
        every { atomicTx.get(budgetDocRef) } returns budgetSnapshot

        val transactionSlot = slot<Transaction.Function<Any?>>()
        every { firestore.runTransaction<Any?>(capture(transactionSlot)) } answers {
            transactionSlot.captured.apply(atomicTx)
            Tasks.forResult<Any?>(null)
        }

        val sampleTx = sampleTransaction(walletId = "wallet_1", amount = 200_000L)
        val result = repository.addWithBalanceUpdate(sampleTx)

        assertInstanceOf(AppResult.Success::class.java, result)
        assertEquals("new_tx_id", (result as AppResult.Success).value)
        // Verify balance deducted: 1_000_000 - 200_000 = 800_000
        verify { atomicTx.update(walletDocRef, "balance", 800_000L) }
    }

    @Test
    fun `addWithBalanceUpdate executes atomic transaction successfully for INCOME`() = runTest {
        val uid = "test_uid"
        every { auth.currentUser } returns user
        every { user.uid } returns uid

        val userDocRef: DocumentReference = mockk()
        val transactionsColl: CollectionReference = mockk()
        val walletsColl: CollectionReference = mockk()
        val transactionDocRef: DocumentReference = mockk(relaxed = true)
        val walletDocRef: DocumentReference = mockk(relaxed = true)
        val walletSnapshot: DocumentSnapshot = mockk()

        every { firestore.collection("users").document(uid) } returns userDocRef
        every { userDocRef.collection("transactions") } returns transactionsColl
        every { userDocRef.collection("wallets") } returns walletsColl
        every { transactionsColl.document() } returns transactionDocRef
        every { transactionDocRef.id } returns "income_tx_id"
        every { walletsColl.document("wallet_1") } returns walletDocRef
        every { walletSnapshot.exists() } returns true
        every { walletSnapshot.getLong("balance") } returns 500_000L

        val atomicTx: Transaction = mockk(relaxed = true)
        every { atomicTx.get(walletDocRef) } returns walletSnapshot

        val transactionSlot = slot<Transaction.Function<Any?>>()
        every { firestore.runTransaction<Any?>(capture(transactionSlot)) } answers {
            transactionSlot.captured.apply(atomicTx)
            Tasks.forResult<Any?>(null)
        }

        val incomeTx = sampleTransaction(walletId = "wallet_1", amount = 300_000L).copy(type = TransactionType.INCOME)
        val result = repository.addWithBalanceUpdate(incomeTx)

        assertInstanceOf(AppResult.Success::class.java, result)
        // Verify balance added: 500_000 + 300_000 = 800_000
        verify { atomicTx.update(walletDocRef, "balance", 800_000L) }
    }

    // ==========================================
    // EDIT TRANSACTION TESTS
    // ==========================================

    @Test
    fun `editWithBalanceUpdate derives old state from stored document and reverses correctly`() = runTest {
        val uid = "test_uid"
        every { auth.currentUser } returns user
        every { user.uid } returns uid

        val userDocRef: DocumentReference = mockk()
        val transactionsColl: CollectionReference = mockk()
        val walletsColl: CollectionReference = mockk()
        val budgetsColl: CollectionReference = mockk()
        val transactionDocRef: DocumentReference = mockk(relaxed = true)
        val oldWalletDocRef: DocumentReference = mockk(relaxed = true)
        val newWalletDocRef: DocumentReference = mockk(relaxed = true)
        val oldBudgetDocRef: DocumentReference = mockk(relaxed = true)

        every { oldWalletDocRef.path } returns "users/test_uid/wallets/wallet_old"
        every { newWalletDocRef.path } returns "users/test_uid/wallets/wallet_new"
        every { oldBudgetDocRef.path } returns "users/test_uid/budgets/budget_old"

        val txSnapshot: DocumentSnapshot = mockk(relaxed = true)
        val oldWalletSnapshot: DocumentSnapshot = mockk(relaxed = true)
        val newWalletSnapshot: DocumentSnapshot = mockk(relaxed = true)
        val oldBudgetSnapshot: DocumentSnapshot = mockk(relaxed = true)

        every { firestore.collection("users").document(uid) } returns userDocRef
        every { userDocRef.collection("transactions") } returns transactionsColl
        every { userDocRef.collection("wallets") } returns walletsColl
        every { userDocRef.collection("budgets") } returns budgetsColl
        every { transactionsColl.document("tx_123") } returns transactionDocRef
        every { walletsColl.document("wallet_old") } returns oldWalletDocRef
        every { walletsColl.document("wallet_new") } returns newWalletDocRef
        every { budgetsColl.document(any()) } returns oldBudgetDocRef

        // Stored document in Firestore
        every { txSnapshot.id } returns "tx_123"
        every { txSnapshot.getLong("amount") } returns 1_000_000L
        every { txSnapshot.getString("type") } returns "expense"
        every { txSnapshot.getString("categoryId") } returns "cat_food"
        every { txSnapshot.getString("walletId") } returns "wallet_old"
        every { txSnapshot.getString("note") } returns "Stored Note"
        every { txSnapshot.getString("receiptImageUrl") } returns null
        every { txSnapshot.getString("relatedWalletId") } returns null
        every { txSnapshot.getTimestamp("date") } returns fixedTimestamp
        every { txSnapshot.getTimestamp("createdAt") } returns fixedTimestamp
        every { txSnapshot.getTimestamp("updatedAt") } returns fixedTimestamp

        every { oldWalletSnapshot.exists() } returns true
        every { oldWalletSnapshot.getLong("balance") } returns 2_000_000L

        every { newWalletSnapshot.exists() } returns true
        every { newWalletSnapshot.getLong("balance") } returns 500_000L

        every { oldBudgetSnapshot.exists() } returns true

        val atomicTx: Transaction = mockk(relaxed = true)
        every { atomicTx.get(transactionDocRef) } returns txSnapshot
        every { atomicTx.get(oldWalletDocRef) } returns oldWalletSnapshot
        every { atomicTx.get(newWalletDocRef) } returns newWalletSnapshot
        every { atomicTx.get(oldBudgetDocRef) } returns oldBudgetSnapshot

        val transactionSlot = slot<Transaction.Function<Any?>>()
        every { firestore.runTransaction<Any?>(capture(transactionSlot)) } answers {
            transactionSlot.captured.apply(atomicTx)
            Tasks.forResult<Any?>(null)
        }

        // Caller sends stale original (amount: 800_000 instead of 1_000_000)
        val staleOriginal = sampleTransaction(id = "tx_123", walletId = "wallet_old", amount = 800_000L)
        val updated = sampleTransaction(id = "tx_123", walletId = "wallet_new", amount = 300_000L)

        val result = repository.editWithBalanceUpdate(staleOriginal, updated)

        assertInstanceOf(AppResult.Success::class.java, result)

        // Verify old wallet reversed with stored amount 1_000_000 -> 2_000_000 + 1_000_000 = 3_000_000
        verify { atomicTx.update(oldWalletDocRef, "balance", 3_000_000L) }
        // Verify new wallet deducted 300_000 -> 500_000 - 300_000 = 200_000
        verify { atomicTx.update(newWalletDocRef, "balance", 200_000L) }
        // Verify budget updated with net delta
        verify { atomicTx.update(oldBudgetDocRef, "spentAmount", any<FieldValue>()) }
    }

    @Test
    fun `editWithBalanceUpdate fails if stored transaction not found`() = runTest {
        val uid = "test_uid"
        every { auth.currentUser } returns user
        every { user.uid } returns uid

        val userDocRef: DocumentReference = mockk()
        val transactionsColl: CollectionReference = mockk()
        val transactionDocRef: DocumentReference = mockk(relaxed = true)
        val txSnapshot: DocumentSnapshot = mockk(relaxed = true)

        every { firestore.collection("users").document(uid) } returns userDocRef
        every { userDocRef.collection("transactions") } returns transactionsColl
        every { transactionsColl.document("missing_tx") } returns transactionDocRef
        every { txSnapshot.getLong("amount") } returns null

        val atomicTx: Transaction = mockk(relaxed = true)
        every { atomicTx.get(transactionDocRef) } returns txSnapshot

        val transactionSlot = slot<Transaction.Function<Any?>>()
        every { firestore.runTransaction<Any?>(capture(transactionSlot)) } answers {
            transactionSlot.captured.apply(atomicTx)
            Tasks.forResult<Any?>(null)
        }

        val original = sampleTransaction(id = "missing_tx")
        val updated = sampleTransaction(id = "missing_tx", amount = 50_000L)

        val result = repository.editWithBalanceUpdate(original, updated)
        assertInstanceOf(AppResult.Error::class.java, result)
    }

    // ==========================================
    // DELETE TRANSACTION TESTS
    // ==========================================

    @Test
    fun `deleteWithBalanceUpdate derives state from stored document and reverses correctly`() = runTest {
        val uid = "test_uid"
        every { auth.currentUser } returns user
        every { user.uid } returns uid

        val userDocRef: DocumentReference = mockk()
        val transactionsColl: CollectionReference = mockk()
        val walletsColl: CollectionReference = mockk()
        val budgetsColl: CollectionReference = mockk()
        val transactionDocRef: DocumentReference = mockk(relaxed = true)
        val walletDocRef: DocumentReference = mockk(relaxed = true)
        val budgetDocRef: DocumentReference = mockk(relaxed = true)

        val txSnapshot: DocumentSnapshot = mockk(relaxed = true)
        val walletSnapshot: DocumentSnapshot = mockk(relaxed = true)
        val budgetSnapshot: DocumentSnapshot = mockk(relaxed = true)

        every { firestore.collection("users").document(uid) } returns userDocRef
        every { userDocRef.collection("transactions") } returns transactionsColl
        every { userDocRef.collection("wallets") } returns walletsColl
        every { userDocRef.collection("budgets") } returns budgetsColl
        every { transactionsColl.document("tx_del") } returns transactionDocRef
        every { walletsColl.document("wallet_del") } returns walletDocRef
        every { budgetsColl.document(any()) } returns budgetDocRef

        // Stored document in Firestore
        every { txSnapshot.id } returns "tx_del"
        every { txSnapshot.getLong("amount") } returns 500_000L
        every { txSnapshot.getString("type") } returns "expense"
        every { txSnapshot.getString("categoryId") } returns "cat_food"
        every { txSnapshot.getString("walletId") } returns "wallet_del"
        every { txSnapshot.getString("note") } returns ""
        every { txSnapshot.getString("receiptImageUrl") } returns null
        every { txSnapshot.getString("relatedWalletId") } returns null
        every { txSnapshot.getTimestamp("date") } returns fixedTimestamp
        every { txSnapshot.getTimestamp("createdAt") } returns fixedTimestamp
        every { txSnapshot.getTimestamp("updatedAt") } returns fixedTimestamp

        every { walletSnapshot.exists() } returns true
        every { walletSnapshot.getLong("balance") } returns 1_000_000L

        every { budgetSnapshot.exists() } returns true

        val atomicTx: Transaction = mockk(relaxed = true)
        every { atomicTx.get(transactionDocRef) } returns txSnapshot
        every { atomicTx.get(walletDocRef) } returns walletSnapshot
        every { atomicTx.get(budgetDocRef) } returns budgetSnapshot

        val transactionSlot = slot<Transaction.Function<Any?>>()
        every { firestore.runTransaction<Any?>(capture(transactionSlot)) } answers {
            transactionSlot.captured.apply(atomicTx)
            Tasks.forResult<Any?>(null)
        }

        // Caller passes stale transaction with wrong amount (200_000 instead of 500_000)
        val staleTx = sampleTransaction(id = "tx_del", walletId = "wallet_del", amount = 200_000L)
        val result = repository.deleteWithBalanceUpdate(staleTx)

        assertInstanceOf(AppResult.Success::class.java, result)

        // Verify balance refunded with stored amount: 1_000_000 + 500_000 = 1_500_000
        verify { atomicTx.update(walletDocRef, "balance", 1_500_000L) }
        // Verify budget reversed with stored amount: -500_000
        verify { atomicTx.update(budgetDocRef, "spentAmount", any<FieldValue>()) }
        // Verify document deleted
        verify { atomicTx.delete(transactionDocRef) }
    }

    // ==========================================
    // TRANSFER BETWEEN WALLETS TESTS
    // ==========================================

    @Test
    fun `transferBetweenWallets executes atomic OUT and IN transactions successfully`() = runTest {
        val uid = "test_uid"
        every { auth.currentUser } returns user
        every { user.uid } returns uid

        val userDocRef: DocumentReference = mockk()
        val transactionsColl: CollectionReference = mockk()
        val walletsColl: CollectionReference = mockk()
        val outTxDocRef: DocumentReference = mockk(relaxed = true)
        val inTxDocRef: DocumentReference = mockk(relaxed = true)
        val sourceWalletRef: DocumentReference = mockk(relaxed = true)
        val destWalletRef: DocumentReference = mockk(relaxed = true)

        val sourceWalletSnapshot: DocumentSnapshot = mockk(relaxed = true)
        val destWalletSnapshot: DocumentSnapshot = mockk(relaxed = true)

        every { firestore.collection("users").document(uid) } returns userDocRef
        every { userDocRef.collection("transactions") } returns transactionsColl
        every { userDocRef.collection("wallets") } returns walletsColl
        every { transactionsColl.document(any()) } returns outTxDocRef andThen inTxDocRef
        every { outTxDocRef.id } returns "out_id"
        every { inTxDocRef.id } returns "in_id"

        every { walletsColl.document("wallet_src") } returns sourceWalletRef
        every { walletsColl.document("wallet_dst") } returns destWalletRef

        every { sourceWalletSnapshot.exists() } returns true
        every { sourceWalletSnapshot.getString("type") } returns "cash"
        every { sourceWalletSnapshot.getLong("balance") } returns 500_000L

        every { destWalletSnapshot.exists() } returns true
        every { destWalletSnapshot.getLong("balance") } returns 100_000L

        val atomicTx: Transaction = mockk(relaxed = true)
        every { atomicTx.get(sourceWalletRef) } returns sourceWalletSnapshot
        every { atomicTx.get(destWalletRef) } returns destWalletSnapshot

        val transactionSlot = slot<Transaction.Function<Any?>>()
        every { firestore.runTransaction<Any?>(capture(transactionSlot)) } answers {
            transactionSlot.captured.apply(atomicTx)
            Tasks.forResult<Any?>(null)
        }

        val result = repository.transferBetweenWallets("wallet_src", "wallet_dst", 200_000L, "Chuyển tiền", fixedInstant)
        assertInstanceOf(AppResult.Success::class.java, result)

        // Verify source deducted 200_000 -> 300_000, dest added 200_000 -> 300_000
        verify { atomicTx.update(sourceWalletRef, "balance", 300_000L) }
        verify { atomicTx.update(destWalletRef, "balance", 300_000L) }
    }

    @Test
    fun `transferBetweenWallets fails if source and destination are identical`() = runTest {
        every { auth.currentUser } returns user
        every { user.uid } returns "test_uid"

        val result = repository.transferBetweenWallets("same_wallet", "same_wallet", 100_000L, "", fixedInstant)
        assertInstanceOf(AppResult.Error::class.java, result)
        assertTrue((result as AppResult.Error).message.contains("Hai ví phải khác nhau"))
    }

    @Test
    fun `transferBetweenWallets fails if amount is zero or negative`() = runTest {
        every { auth.currentUser } returns user
        every { user.uid } returns "test_uid"

        val zeroResult = repository.transferBetweenWallets("src", "dst", 0L, "", fixedInstant)
        assertInstanceOf(AppResult.Error::class.java, zeroResult)

        val negResult = repository.transferBetweenWallets("src", "dst", -50_000L, "", fixedInstant)
        assertInstanceOf(AppResult.Error::class.java, negResult)
    }

    @Test
    fun `transferBetweenWallets fails if non-card source has insufficient balance`() = runTest {
        val uid = "test_uid"
        every { auth.currentUser } returns user
        every { user.uid } returns uid

        val userDocRef: DocumentReference = mockk()
        val walletsColl: CollectionReference = mockk()
        val transactionsColl: CollectionReference = mockk()
        val txDocRef: DocumentReference = mockk(relaxed = true)
        val sourceWalletRef: DocumentReference = mockk(relaxed = true)
        val destWalletRef: DocumentReference = mockk(relaxed = true)
        val sourceWalletSnapshot: DocumentSnapshot = mockk(relaxed = true)
        val destWalletSnapshot: DocumentSnapshot = mockk(relaxed = true)

        every { firestore.collection("users").document(uid) } returns userDocRef
        every { userDocRef.collection("wallets") } returns walletsColl
        every { userDocRef.collection("transactions") } returns transactionsColl
        every { transactionsColl.document(any()) } returns txDocRef
        every { walletsColl.document("wallet_src") } returns sourceWalletRef
        every { walletsColl.document("wallet_dst") } returns destWalletRef

        every { sourceWalletSnapshot.exists() } returns true
        every { sourceWalletSnapshot.getString("type") } returns "cash"
        every { sourceWalletSnapshot.getLong("balance") } returns 50_000L // only 50k

        every { destWalletSnapshot.exists() } returns true
        every { destWalletSnapshot.getLong("balance") } returns 100_000L

        val atomicTx: Transaction = mockk(relaxed = true)
        every { atomicTx.get(sourceWalletRef) } returns sourceWalletSnapshot
        every { atomicTx.get(destWalletRef) } returns destWalletSnapshot

        val transactionSlot = slot<Transaction.Function<Any?>>()
        every { firestore.runTransaction<Any?>(capture(transactionSlot)) } answers {
            transactionSlot.captured.apply(atomicTx)
            Tasks.forResult<Any?>(null)
        }

        // Try to transfer 200_000 from wallet with only 50_000
        val result = repository.transferBetweenWallets("wallet_src", "wallet_dst", 200_000L, "", fixedInstant)
        assertInstanceOf(AppResult.Error::class.java, result)
        assertTrue((result as AppResult.Error).message.contains("Số dư ví nguồn không đủ"))
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
        date = fixedInstant,
    )
}
