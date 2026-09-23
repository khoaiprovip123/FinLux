package com.finlux.app.domain.usecase.account

import com.finlux.app.core.common.AppResult
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PurgeUserDataUseCaseTest {

    private val firestore: FirebaseFirestore = mockk(relaxed = true)
    private val storage: FirebaseStorage = mockk(relaxed = true)
    private val storageRootRef: StorageReference = mockk(relaxed = true)
    private val avatarRef: StorageReference = mockk(relaxed = true)
    private val receiptsRef: StorageReference = mockk(relaxed = true)
    private val emptyListResult: ListResult = mockk(relaxed = true)

    private val usersCollection: CollectionReference = mockk(relaxed = true)
    private val userDoc: DocumentReference = mockk(relaxed = true)
    private val writeBatch: WriteBatch = mockk(relaxed = true)

    private lateinit var useCase: PurgeUserDataUseCase

    @BeforeEach
    fun setUp() {
        every { storage.reference } returns storageRootRef
        every { storageRootRef.child("avatars/test-uid.jpg") } returns avatarRef
        every { storageRootRef.child("receipts/test-uid") } returns receiptsRef
        every { avatarRef.delete() } returns Tasks.forResult(null)
        every { emptyListResult.items } returns emptyList()
        every { emptyListResult.prefixes } returns emptyList()
        every { receiptsRef.listAll() } returns Tasks.forResult(emptyListResult)

        every { firestore.collection("users") } returns usersCollection
        every { usersCollection.document("test-uid") } returns userDoc
        every { firestore.batch() } returns writeBatch
        every { writeBatch.delete(any()) } returns writeBatch
        every { writeBatch.commit() } returns Tasks.forResult(null)
        every { userDoc.delete() } returns Tasks.forResult(null)

        val emptySnapshot: QuerySnapshot = mockk(relaxed = true)
        every { emptySnapshot.iterator() } returns mutableListOf<QueryDocumentSnapshot>().iterator()
        every { userDoc.collection(any()).get() } returns Tasks.forResult(emptySnapshot)

        useCase = PurgeUserDataUseCase(firestore, storage)
    }

    @Test
    fun `T-PURGE-01 Complete reverse purge deletes all subcollections and user doc`() = runTest {
        val sampleDoc: QueryDocumentSnapshot = mockk(relaxed = true)
        val sampleDocRef: DocumentReference = mockk(relaxed = true)
        every { sampleDoc.reference } returns sampleDocRef

        val singleDocSnapshot: QuerySnapshot = mockk(relaxed = true)
        every { singleDocSnapshot.iterator() } returns mutableListOf(sampleDoc).iterator()

        val emptySnapshot: QuerySnapshot = mockk(relaxed = true)
        every { emptySnapshot.iterator() } returns mutableListOf<QueryDocumentSnapshot>().iterator()

        every { userDoc.collection("deals").get() } returns Tasks.forResult(singleDocSnapshot)
        every { userDoc.collection(neq("deals")).get() } returns Tasks.forResult(emptySnapshot)

        val result = useCase("test-uid")

        assertTrue(result is AppResult.Success)
        verify { writeBatch.delete(sampleDocRef) }
        verify { writeBatch.commit() }
        verify { userDoc.delete() }
    }

    @Test
    fun `T-PURGE-02 Storage file deletion purges avatar and receipts`() = runTest {
        val receiptItem: StorageReference = mockk(relaxed = true)
        every { receiptItem.delete() } returns Tasks.forResult(null)

        val receiptListResult: ListResult = mockk(relaxed = true)
        every { receiptListResult.items } returns listOf(receiptItem)
        every { receiptListResult.prefixes } returns emptyList()
        every { receiptsRef.listAll() } returns Tasks.forResult(receiptListResult)

        val result = useCase("test-uid")

        assertTrue(result is AppResult.Success)
        verify { avatarRef.delete() }
        verify { receiptItem.delete() }
    }

    @Test
    fun `T-PURGE-03 Empty storage resilience handles storage exception gracefully`() = runTest {
        every { avatarRef.delete() } returns Tasks.forException(RuntimeException("File not found"))
        every { receiptsRef.listAll() } returns Tasks.forException(RuntimeException("Folder not found"))

        val result = useCase("test-uid")

        assertTrue(result is AppResult.Success)
    }

    @Test
    fun `T-PURGE-04 Network error propagation returns AppResult Error when firestore fails`() = runTest {
        every { userDoc.delete() } returns Tasks.forException(RuntimeException("Network timeout"))

        val result = useCase("test-uid")

        assertTrue(result is AppResult.Error)
        assertEquals("Network timeout", (result as AppResult.Error).message)
    }
}
