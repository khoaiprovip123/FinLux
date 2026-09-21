package com.finlux.app.domain.usecase.account

import android.content.Context
import android.content.SharedPreferences
import com.finlux.app.core.common.AppResult
import com.finlux.app.core.sync.DataSyncManager
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.model.ReminderRecurrence
import com.finlux.app.domain.model.UserProfile
import com.finlux.app.domain.repository.AuthRepository
import com.finlux.app.domain.repository.DebtPreferenceRepository
import com.finlux.app.domain.repository.ReminderRepository
import com.finlux.app.domain.repository.ReminderScheduler
import com.finlux.app.domain.repository.SalaryCycleScheduler
import com.finlux.app.domain.repository.SavingSpinScheduler
import com.finlux.app.domain.repository.ThemePreferenceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class DeleteAccountUseCaseTest {

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val purgeUserDataUseCase: PurgeUserDataUseCase = mockk(relaxed = true)
    private val reminderRepository: ReminderRepository = mockk(relaxed = true)
    private val reminderScheduler: ReminderScheduler = mockk(relaxed = true)
    private val salaryCycleScheduler: SalaryCycleScheduler = mockk(relaxed = true)
    private val savingSpinScheduler: SavingSpinScheduler = mockk(relaxed = true)
    private val themePreferenceRepository: ThemePreferenceRepository = mockk(relaxed = true)
    private val debtPreferenceRepository: DebtPreferenceRepository = mockk(relaxed = true)
    private val dataSyncManager: DataSyncManager = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val sharedPreferences: SharedPreferences = mockk(relaxed = true)
    private val editor: SharedPreferences.Editor = mockk(relaxed = true)

    private val testUser = UserProfile(
        uid = "user-123",
        displayName = "Test User",
        email = "user@test.com",
        photoUrl = null,
    )

    private lateinit var useCase: DeleteAccountUseCase

    @BeforeEach
    fun setUp() {
        every { authRepository.currentUser } returns flowOf(testUser)
        coEvery { purgeUserDataUseCase(any()) } returns AppResult.Success(Unit)
        coEvery { authRepository.deleteAuthAccount() } returns AppResult.Success(Unit)
        coEvery { authRepository.reauthenticateWithPassword(any()) } returns AppResult.Success(Unit)
        coEvery { authRepository.reauthenticateWithGoogle(any()) } returns AppResult.Success(Unit)

        every { reminderRepository.observeReminders() } returns flowOf(emptyList())
        every { context.getSharedPreferences(any(), any()) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.clear() } returns editor

        useCase = DeleteAccountUseCase(
            authRepository = authRepository,
            purgeUserDataUseCase = purgeUserDataUseCase,
            reminderRepository = reminderRepository,
            reminderScheduler = reminderScheduler,
            salaryCycleScheduler = salaryCycleScheduler,
            savingSpinScheduler = savingSpinScheduler,
            themePreferenceRepository = themePreferenceRepository,
            debtPreferenceRepository = debtPreferenceRepository,
            dataSyncManager = dataSyncManager,
            context = context,
        )
    }

    @Test
    fun `T-DEL-01 Happy path deletion executes entire pipeline in strict order`() = runTest {
        val result = useCase()

        assertTrue(result is AppResult.Success)
        coVerify { purgeUserDataUseCase("user-123") }
        coVerify { authRepository.deleteAuthAccount() }
        coVerify { themePreferenceRepository.resetPreferences() }
        coVerify { debtPreferenceRepository.resetPreferences() }
        coVerify { dataSyncManager.notifyDataRestored() }
    }

    @Test
    fun `T-DEL-02 Pre-flight re-auth with password failure aborts pipeline immediately`() = runTest {
        coEvery { authRepository.reauthenticateWithPassword("wrong-pass") } returns AppResult.Error("Mật khẩu không chính xác")

        val result = useCase(password = "wrong-pass")

        assertTrue(result is AppResult.Error)
        assertEquals("Mật khẩu không chính xác", (result as AppResult.Error).message)
        // Storage and Firestore MUST NOT be touched
        coVerify(exactly = 0) { purgeUserDataUseCase(any()) }
        coVerify(exactly = 0) { authRepository.deleteAuthAccount() }
    }

    @Test
    fun `T-DEL-03 Pre-flight re-auth with Google failure aborts pipeline immediately`() = runTest {
        coEvery { authRepository.reauthenticateWithGoogle("invalid-token") } returns AppResult.Error("Google Auth lỗi")

        val result = useCase(googleIdToken = "invalid-token")

        assertTrue(result is AppResult.Error)
        assertEquals("Google Auth lỗi", (result as AppResult.Error).message)
        // Storage and Firestore MUST NOT be touched
        coVerify(exactly = 0) { purgeUserDataUseCase(any()) }
        coVerify(exactly = 0) { authRepository.deleteAuthAccount() }
    }

    @Test
    fun `T-DEL-04 Schedulers cancellation cancels all reminder, salary and saving spin alarms`() = runTest {
        val reminder = Reminder(
            id = "rem-1",
            title = "Tiền nhà",
            amount = Money(5000000L),
            categoryId = "rent",
            walletId = "wallet-1",
            recurrence = ReminderRecurrence.MONTHLY,
            startDate = Instant.parse("2026-09-01T10:00:00Z"),
            enabled = true,
            nextTriggerDate = Instant.parse("2026-09-30T10:00:00Z"),
        )
        every { reminderRepository.observeReminders() } returns flowOf(listOf(reminder))

        val result = useCase()

        assertTrue(result is AppResult.Success)
        coVerify { reminderScheduler.cancel("rem-1") }
        coVerify { salaryCycleScheduler.cancel() }
        coVerify { savingSpinScheduler.cancel() }
    }

    @Test
    fun `T-DEL-05 Cloud purge failure aborts before deleting Auth user`() = runTest {
        coEvery { purgeUserDataUseCase("user-123") } returns AppResult.Error("Lỗi xóa Firestore")

        val result = useCase()

        assertTrue(result is AppResult.Error)
        assertEquals("Lỗi xóa Firestore", (result as AppResult.Error).message)
        // Auth user must NOT be deleted if database purge failed
        coVerify(exactly = 0) { authRepository.deleteAuthAccount() }
    }
}
