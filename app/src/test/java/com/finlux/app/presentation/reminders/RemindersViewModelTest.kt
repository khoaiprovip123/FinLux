package com.finlux.app.presentation.reminders

import app.cash.turbine.test
import com.finlux.app.core.common.AppResult
import com.finlux.app.core.time.ReminderCountdownTier
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.model.ReminderRecurrence
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.ReminderRepository
import com.finlux.app.domain.repository.ReminderScheduler
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.DeleteReminderUseCase
import com.finlux.app.domain.usecase.SaveReminderUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class RemindersViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var reminderRepo: FakeReminderRepository
    private lateinit var categoryRepo: FakeCategoryRepository
    private lateinit var walletRepo: FakeWalletRepository
    private lateinit var scheduler: FakeReminderScheduler
    private lateinit var saveReminderUseCase: SaveReminderUseCase
    private lateinit var deleteReminderUseCase: DeleteReminderUseCase
    private lateinit var viewModel: RemindersViewModel

    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    // Mốc thời gian chuẩn: 14/09/2026 10:00:00 UTC (17:00:00 GMT+7)
    private val fixedNow = Instant.parse("2026-09-14T10:00:00Z")

    private val sampleReminder = Reminder(
        id = "rem_1",
        title = "Thanh toán cước Internet",
        amount = Money(350_000L),
        categoryId = "cat_bill",
        walletId = "w_bank",
        recurrence = ReminderRecurrence.MONTHLY,
        startDate = fixedNow,
        enabled = true,
        nextTriggerDate = fixedNow.plusSeconds(86400 * 3),
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        reminderRepo = FakeReminderRepository(listOf(sampleReminder))
        categoryRepo = FakeCategoryRepository(
            listOf(
                Category(
                    id = "cat_bill",
                    name = "Hóa đơn & Tiện ích",
                    type = CategoryType.EXPENSE,
                    icon = "receipt",
                    colorHex = "#FF5722",
                    isDefault = true,
                    createdAt = fixedNow,
                ),
                Category(
                    id = "cat_salary",
                    name = "Tiền lương",
                    type = CategoryType.INCOME,
                    icon = "payments",
                    colorHex = "#4CAF50",
                    isDefault = true,
                    createdAt = fixedNow,
                ),
            )
        )
        walletRepo = FakeWalletRepository(
            listOf(
                Wallet(
                    id = "w_bank",
                    name = "Vietcombank",
                    type = WalletType.BANK,
                    balance = Money(5_000_000L),
                    colorHex = "#1F6FBF",
                    isDefault = true,
                    createdAt = fixedNow,
                )
            )
        )
        scheduler = FakeReminderScheduler()
        saveReminderUseCase = SaveReminderUseCase(reminderRepo, scheduler)
        deleteReminderUseCase = DeleteReminderUseCase(reminderRepo, scheduler)

        viewModel = RemindersViewModel(
            repository = reminderRepo,
            categoryRepository = categoryRepo,
            walletRepository = walletRepo,
            saveReminder = saveReminderUseCase,
            deleteReminder = deleteReminderUseCase,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `toggleReminder switches enabled state and reschedules or cancels alarm accordingly`() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect() }
        // 1. Đang enabled = true -> toggle sang false: Hủy báo thức
        viewModel.toggle(sampleReminder)
        advanceUntilIdle()

        assertFalse(reminderRepo.lastUpsertedReminder?.enabled == true)
        assertTrue(scheduler.cancelCalls.contains("rem_1"))

        // 2. Đang enabled = false -> toggle sang true: Đặt lại báo thức
        val disabledReminder = sampleReminder.copy(enabled = false)
        viewModel.toggle(disabledReminder)
        advanceUntilIdle()

        assertTrue(reminderRepo.lastUpsertedReminder?.enabled == true)
        assertTrue(scheduler.scheduledReminders.any { it.id == "rem_1" && it.enabled })
    }

    @Test
    fun `deleteReminder removes item and cancels scheduled alarm`() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect() }
        viewModel.delete(sampleReminder)
        advanceUntilIdle()

        assertEquals("rem_1", reminderRepo.deletedReminder?.id)
        assertTrue(scheduler.cancelCalls.contains("rem_1"))
        assertEquals("Đã xóa nhắc nhở", viewModel.state.value.message)
        assertFalse(reminderRepo.currentReminders.any { it.id == "rem_1" })
    }

    @Test
    fun `countdown badge resolves correct tier for OVERDUE, TODAY, TOMORROW, and FUTURE reminders`() {
        // Tier 1: OVERDUE (Quá hạn 2 ngày)
        val overdueReminder = sampleReminder.copy(
            id = "rem_overdue",
            nextTriggerDate = Instant.parse("2026-09-12T10:00:00Z"),
        )
        val overdueInfo = viewModel.resolveCountdown(overdueReminder, now = fixedNow, zone = zone)
        assertEquals(ReminderCountdownTier.OVERDUE, overdueInfo.tier)
        assertTrue(overdueInfo.isOverdue)
        assertFalse(overdueInfo.isUrgent)
        assertTrue(overdueInfo.label.contains("quá hạn 2 ngày"))

        // Tier 2: TODAY (Còn 2 giờ trong cùng ngày 14/09)
        val todayReminder = sampleReminder.copy(
            id = "rem_today",
            nextTriggerDate = Instant.parse("2026-09-14T12:00:00Z"),
        )
        val todayInfo = viewModel.resolveCountdown(todayReminder, now = fixedNow, zone = zone)
        assertEquals(ReminderCountdownTier.TODAY, todayInfo.tier)
        assertFalse(todayInfo.isOverdue)
        assertTrue(todayInfo.isUrgent)
        assertEquals("còn 2 giờ", todayInfo.label)

        // Tier 3: TOMORROW (Ngày mai 15/09 lúc 03:00 UTC = 10:00 GMT+7)
        val tomorrowReminder = sampleReminder.copy(
            id = "rem_tomorrow",
            nextTriggerDate = Instant.parse("2026-09-15T03:00:00Z"),
        )
        val tomorrowInfo = viewModel.resolveCountdown(tomorrowReminder, now = fixedNow, zone = zone)
        assertEquals(ReminderCountdownTier.TOMORROW, tomorrowInfo.tier)
        assertFalse(tomorrowInfo.isOverdue)
        assertEquals("ngày mai", tomorrowInfo.label)

        // Tier 4: FUTURE (Còn 6 ngày nữa - ngày 20/09)
        val futureReminder = sampleReminder.copy(
            id = "rem_future",
            nextTriggerDate = Instant.parse("2026-09-20T10:00:00Z"),
        )
        val futureInfo = viewModel.resolveCountdown(futureReminder, now = fixedNow, zone = zone)
        assertEquals(ReminderCountdownTier.FUTURE, futureInfo.tier)
        assertFalse(futureInfo.isOverdue)
        assertEquals("còn 6 ngày", futureInfo.label)
    }

    @Test
    fun `remindersUiState loads reminders sorted by nextTriggerDate and filters expense categories only`() = runTest(testDispatcher) {
        viewModel.state.test {
            val state = awaitItem()
            val finalState = if (state.reminders.isEmpty() || state.categories.isEmpty()) awaitItem() else state

            assertEquals(1, finalState.reminders.size)
            assertEquals("rem_1", finalState.reminders.first().id)

            // Category salary (INCOME) bị loại bỏ, chỉ còn cat_bill (EXPENSE)
            assertEquals(1, finalState.categories.size)
            assertEquals("cat_bill", finalState.categories.first().id)
            assertEquals(CategoryType.EXPENSE, finalState.categories.first().type)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save reminder with blank title sets validation error in UiState`() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect() }
        var onSavedCalled = false
        val invalidReminder = sampleReminder.copy(title = "   ")

        viewModel.save(invalidReminder) { onSavedCalled = true }
        advanceUntilIdle()

        assertFalse(onSavedCalled)
        assertEquals("Vui lòng nhập tên nhắc nhở", viewModel.state.value.message)
    }

    @Test
    fun `consumeMessage clears action message from state`() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect() }
        viewModel.delete(sampleReminder)
        advanceUntilIdle()
        assertEquals("Đã xóa nhắc nhở", viewModel.state.value.message)

        viewModel.consumeMessage()
        advanceUntilIdle()
        assertNull(viewModel.state.value.message)
    }

    // --- Fake Implementations ---

    private class FakeReminderRepository(initialReminders: List<Reminder>) : ReminderRepository {
        private val _remindersFlow = MutableStateFlow(initialReminders)
        val currentReminders: List<Reminder> get() = _remindersFlow.value

        var lastUpsertedReminder: Reminder? = null
        var deletedReminder: Reminder? = null

        override fun observeReminders(): Flow<List<Reminder>> = _remindersFlow

        override suspend fun upsertReminder(reminder: Reminder): AppResult<String> {
            lastUpsertedReminder = reminder
            val current = _remindersFlow.value.toMutableList()
            val index = current.indexOfFirst { it.id == reminder.id }
            if (index >= 0) {
                current[index] = reminder
            } else {
                current.add(reminder)
            }
            _remindersFlow.value = current
            return AppResult.Success(reminder.id)
        }

        override suspend fun deleteReminder(reminder: Reminder): AppResult<Unit> {
            deletedReminder = reminder
            _remindersFlow.value = _remindersFlow.value.filterNot { it.id == reminder.id }
            return AppResult.Success(Unit)
        }
    }

    private class FakeReminderScheduler : ReminderScheduler {
        val scheduledReminders = mutableListOf<Reminder>()
        val cancelCalls = mutableListOf<String>()

        override fun schedule(reminder: Reminder) {
            scheduledReminders.add(reminder)
        }

        override fun cancel(reminderId: String) {
            cancelCalls.add(reminderId)
            scheduledReminders.removeAll { it.id == reminderId }
        }
    }

    private class FakeCategoryRepository(private val categories: List<Category>) : CategoryRepository {
        override fun observeCategories(): Flow<List<Category>> = flowOf(categories)
        override suspend fun upsertCategory(category: Category): AppResult<String> = AppResult.Success(category.id)
        override suspend fun deleteCategory(category: Category): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class FakeWalletRepository(private val wallets: List<Wallet>) : WalletRepository {
        override fun observeWallets(): Flow<List<Wallet>> = flowOf(wallets)
        override suspend fun upsertWallet(wallet: Wallet): AppResult<String> = AppResult.Success(wallet.id)
        override suspend fun deleteWallet(wallet: Wallet): AppResult<Unit> = AppResult.Success(Unit)
    }
}
