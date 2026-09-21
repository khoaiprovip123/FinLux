package com.finlux.app.domain.usecase.backup

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtPaymentHistory
import com.finlux.app.domain.model.DebtType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.FinancialDeal
import com.finlux.app.domain.model.FinancialGoal
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.model.ReminderRecurrence
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.model.SystemCategories
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.model.backup.BudgetSnapshot
import com.finlux.app.domain.model.backup.CategorySnapshot
import com.finlux.app.domain.model.backup.DealSnapshot
import com.finlux.app.domain.model.backup.DebtPaymentSnapshot
import com.finlux.app.domain.model.backup.DebtSnapshot
import com.finlux.app.domain.model.backup.FinluxBackupSnapshot
import com.finlux.app.domain.model.backup.GoalSnapshot
import com.finlux.app.domain.model.backup.ReminderSnapshot
import com.finlux.app.domain.model.backup.RestoreReport
import com.finlux.app.domain.model.backup.RestoreStrategy
import com.finlux.app.domain.model.backup.SalaryCycleConfigSnapshot
import com.finlux.app.domain.model.backup.SavingSpinConfigSnapshot
import com.finlux.app.domain.model.backup.TransactionSnapshot
import com.finlux.app.domain.model.backup.WalletSnapshot
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.DealRepository
import com.finlux.app.domain.repository.DebtRepository
import com.finlux.app.domain.repository.GoalRepository
import com.finlux.app.domain.repository.ReminderRepository
import com.finlux.app.domain.repository.ReminderScheduler
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.SavingSpinRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit test suite for [RestoreBackupUseCase] covering T-RST-01 through T-RST-09.
 */
class RestoreBackupUseCaseTest {

    private val walletRepository: WalletRepository = mockk(relaxed = true)
    private val categoryRepository: CategoryRepository = mockk(relaxed = true)
    private val transactionRepository: TransactionRepository = mockk(relaxed = true)
    private val budgetRepository: BudgetRepository = mockk(relaxed = true)
    private val debtRepository: DebtRepository = mockk(relaxed = true)
    private val goalRepository: GoalRepository = mockk(relaxed = true)
    private val reminderRepository: ReminderRepository = mockk(relaxed = true)
    private val salaryCycleRepository: SalaryCycleRepository = mockk(relaxed = true)
    private val dealRepository: DealRepository = mockk(relaxed = true)
    private val savingSpinRepository: SavingSpinRepository = mockk(relaxed = true)
    private val reminderScheduler: ReminderScheduler = mockk(relaxed = true)

    private val currentUserId = "user-alice-current"

    private lateinit var useCase: RestoreBackupUseCase

    @BeforeEach
    fun setUp() {
        useCase = RestoreBackupUseCase(
            walletRepository = walletRepository,
            categoryRepository = categoryRepository,
            transactionRepository = transactionRepository,
            budgetRepository = budgetRepository,
            debtRepository = debtRepository,
            goalRepository = goalRepository,
            reminderRepository = reminderRepository,
            salaryCycleRepository = salaryCycleRepository,
            dealRepository = dealRepository,
            savingSpinRepository = savingSpinRepository,
            reminderScheduler = reminderScheduler,
        )

        // Default empty responses for observe queries
        every { walletRepository.observeWallets() } returns flowOf(emptyList())
        every { categoryRepository.observeCategories() } returns flowOf(emptyList())
        every { transactionRepository.observePeriod(any(), any()) } returns flowOf(emptyList())
        every { budgetRepository.observeBudgets(any()) } returns flowOf(emptyList())
        every { debtRepository.observeDebts() } returns flowOf(emptyList())
        every { debtRepository.observeAllPaymentHistory() } returns flowOf(emptyList())
        every { goalRepository.observeGoals() } returns flowOf(emptyList())
        every { reminderRepository.observeReminders() } returns flowOf(emptyList())
        every { salaryCycleRepository.observeConfig() } returns flowOf(SalaryCycleConfig())
        every { dealRepository.observeDeals() } returns flowOf(emptyList())
        every { savingSpinRepository.observeConfig() } returns flowOf(SavingSpinConfig())

        coEvery { walletRepository.upsertWallet(any()) } returns AppResult.Success("wallet-id")
        coEvery { walletRepository.deleteWallet(any()) } returns AppResult.Success(Unit)
        coEvery { walletRepository.deleteWalletRaw(any()) } returns AppResult.Success(Unit)
        coEvery { walletRepository.restoreWalletRaw(any()) } returns AppResult.Success("wallet-id")
        coEvery { categoryRepository.upsertCategory(any()) } returns AppResult.Success("cat-id")
        coEvery { categoryRepository.deleteCategory(any()) } returns AppResult.Success(Unit)
        coEvery { transactionRepository.restoreTransaction(any()) } returns AppResult.Success("tx-id")
        coEvery { transactionRepository.restoreTransactionRaw(any()) } returns AppResult.Success("tx-id")
        coEvery { transactionRepository.deleteTransactionRaw(any<String>()) } returns AppResult.Success(Unit)
        coEvery { transactionRepository.deleteTransactionRaw(any<FinanceTransaction>()) } returns AppResult.Success(Unit)
        coEvery { budgetRepository.upsertBudget(any()) } returns AppResult.Success("b-id")
        coEvery { budgetRepository.deleteBudget(any()) } returns AppResult.Success(Unit)
        coEvery { debtRepository.upsertDebt(any()) } returns AppResult.Success("debt-id")
        coEvery { debtRepository.deleteDebt(any()) } returns AppResult.Success(Unit)
        coEvery { debtRepository.upsertPaymentHistory(any()) } returns AppResult.Success("pay-id")
        coEvery { debtRepository.deletePaymentHistory(any(), any()) } returns AppResult.Success(Unit)
        coEvery { goalRepository.upsertGoal(any()) } returns AppResult.Success("goal-id")
        coEvery { goalRepository.deleteGoal(any()) } returns AppResult.Success(Unit)
        coEvery { reminderRepository.upsertReminder(any()) } returns AppResult.Success("rem-id")
        coEvery { reminderRepository.deleteReminder(any()) } returns AppResult.Success(Unit)
        coEvery { dealRepository.upsertDeal(any()) } returns AppResult.Success("deal-id")
        coEvery { dealRepository.deleteDeal(any()) } returns AppResult.Success(Unit)
        coEvery { salaryCycleRepository.saveConfig(any()) } returns AppResult.Success(Unit)
        coEvery { savingSpinRepository.saveConfig(any()) } returns AppResult.Success(Unit)
    }

    // ─── Fixture Helpers ───────────────────────────────────────────────────

    private fun createStandardSnapshot(
        exportedByUid: String = currentUserId,
        walletBalance: Long = 1_000_000L,
        txAmount: Long = 1_000_000L,
        isConsistentCashflow: Boolean = true,
    ): FinluxBackupSnapshot {
        val wallet = WalletSnapshot(
            id = "w-1",
            name = "Ví Tiền Mặt",
            type = "cash",
            balance = walletBalance,
            colorHex = "#10B981",
            isDefault = true,
            createdAt = "2026-01-01T00:00:00Z",
            status = "active",
        )

        val category = CategorySnapshot(
            id = "cat-custom-1",
            name = "Tiệc tùng",
            type = "expense",
            icon = "ic_party",
            colorHex = "#EC4899",
            isDefault = false,
            isEssential = false,
            createdAt = "2026-01-01T00:00:00Z",
        )

        val tx = TransactionSnapshot(
            id = "tx-1",
            type = "income",
            amount = if (isConsistentCashflow) walletBalance else txAmount,
            categoryId = "cat-custom-1",
            walletId = "w-1",
            relatedWalletId = null,
            dealId = null,
            dealFlowType = null,
            note = "Lương tháng",
            receiptImageUrl = null,
            date = "2026-09-01T12:00:00Z",
            createdAt = "2026-09-01T12:00:00Z",
            updatedAt = "2026-09-01T12:00:00Z",
        )

        val budget = BudgetSnapshot(
            id = "cat-custom-1_month:2026-09",
            categoryId = "cat-custom-1",
            periodKey = "month:2026-09",
            limitAmount = 5_000_000L,
            spentAmount = 0L,
            notified80 = false,
            notified100 = false,
        )

        val debt = DebtSnapshot(
            id = "debt-1",
            name = "Thẻ HSBC",
            type = "CREDIT_CARD",
            totalAmount = 20_000_000L,
            remainingBalance = 10_000_000L,
            interestRateApr = 18.0,
            minimumPayment = 1_000_000L,
            dueDate = 15,
            statementDate = 25,
            linkedWalletId = "w-1",
            gracePeriodDays = 45,
            colorHex = "#E11D48",
            isReminderEnabled = true,
            reminderDaysBefore = 3,
            isSettled = false,
            createdAt = "2026-01-01T00:00:00Z",
            updatedAt = "2026-09-01T00:00:00Z",
        )

        val payment = DebtPaymentSnapshot(
            id = "pay-1",
            debtId = "debt-1",
            walletId = "w-1",
            amount = 1_000_000L,
            principalPaid = 900_000L,
            interestPaid = 100_000L,
            paymentDate = "2026-09-05T10:00:00Z",
            note = "Trả nợ",
            isCreditCardPayment = true,
        )

        val goal = GoalSnapshot(
            id = "goal-1",
            name = "Mua xe",
            targetAmount = 50_000_000L,
            savedAmount = 10_000_000L,
            deadline = "2026-12-31T00:00:00Z",
            category = "savings",
            monthlyContribution = 5_000_000L,
            imageUri = null,
            createdAt = "2026-01-01T00:00:00Z",
        )

        val reminder = ReminderSnapshot(
            id = "rem-1",
            title = "Tiền phòng",
            amount = 3_000_000L,
            categoryId = "cat-custom-1",
            walletId = "w-1",
            recurrence = "monthly",
            startDate = "2026-01-01T00:00:00Z",
            enabled = true,
            nextTriggerDate = "2026-10-01T08:00:00Z",
        )

        val deal = DealSnapshot(
            id = "deal-1",
            title = "Dự án A",
            description = "Góp vốn",
            category = "investment",
            targetAmount = 100_000_000L,
            totalCapitalOutlay = 50_000_000L,
            totalRecovered = 10_000_000L,
            writtenOffCapital = 0L,
            netProfitLoss = 2_000_000L,
            status = "ACTIVE",
            startDate = "2026-03-01T00:00:00Z",
            endDate = null,
            createdAt = "2026-03-01T00:00:00Z",
            updatedAt = "2026-09-01T00:00:00Z",
        )

        val salaryConfig = SalaryCycleConfigSnapshot(
            enabled = true,
            scheduleType = "MONTHLY_ONCE",
            paydayRuleType = "DAY_OF_MONTH",
            paydayDay = 5,
            salaryWalletId = "w-1",
            expectedSalary = 20_000_000L,
            secondPaydayDay = null,
            secondSalaryWalletId = null,
            secondExpectedSalary = null,
            savingsWalletId = "w-1",
            rolloverRule = "KEEP_IN_WALLET",
            budgetPeriodBasis = "CALENDAR_MONTH",
            financeTimeZone = "Asia/Ho_Chi_Minh",
            updatedAt = "2026-09-01T00:00:00Z",
        )

        val spinConfig = SavingSpinConfigSnapshot(
            enabled = true,
            showOnHome = true,
            minAmount = 10_000L,
            maxAmount = 100_000L,
            stepAmount = 5_000L,
            slotCount = 8,
            frequency = "DAILY",
            selectedWeekdays = listOf(1, 2, 3),
            weeklyDay = 1,
            reminderHour = 9,
            reminderMinute = 0,
            reminderEnabled = true,
            snoozeEnabled = true,
            allowSkip = true,
            defaultDestinationId = null,
            schemaVersion = 1,
            updatedAt = "2026-09-01T00:00:00Z",
        )

        return FinluxBackupSnapshot(
            schemaVersion = 1,
            appVersion = "1.25.6",
            appVersionCode = 180,
            exportedAt = 1_758_441_600_000L,
            exportedByUid = exportedByUid,
            checksum = "valid-checksum",
            payloadSizeBytes = 1024L,
            wallets = listOf(wallet),
            categories = listOf(category),
            transactions = listOf(tx),
            budgets = listOf(budget),
            debts = listOf(debt),
            debtPayments = listOf(payment),
            goals = listOf(goal),
            reminders = listOf(reminder),
            deals = listOf(deal),
            salaryCycleConfig = salaryConfig,
            savingSpinConfig = spinConfig,
        )
    }

    // ─── T-RST-01: Full overwrite success with complete report ────────────

    @Test
    fun `T-RST-01 Full overwrite success returns complete RestoreReport with accurate counts`() = runTest {
        val snapshot = createStandardSnapshot()

        val result = useCase(snapshot, RestoreStrategy.FULL_OVERWRITE, currentUserId)

        assertInstanceOf(AppResult.Success::class.java, result)
        val report = (result as AppResult.Success<RestoreReport>).value
        assertEquals(RestoreStrategy.FULL_OVERWRITE, report.strategy)
        assertEquals(1, report.walletsRestored)
        assertEquals(1, report.transactionsRestored)
        assertEquals(1, report.categoriesRestored)
        assertEquals(1, report.budgetsRestored)
        assertEquals(1, report.debtsRestored)
        assertEquals(1, report.debtPaymentsRestored)
        assertEquals(1, report.goalsRestored)
        assertEquals(1, report.remindersRestored)
        assertEquals(1, report.dealsRestored)
        assertEquals(0, report.skippedCount)
        assertEquals(0, report.conflictsResolved)
        assertTrue(report.balanceAuditPassed)
        assertTrue(report.balanceDiscrepancies.isEmpty())
    }

    // ─── T-RST-02: Full overwrite deletes old records first ─────────────────

    @Test
    fun `T-RST-02 Full overwrite deletes existing records in reverse dependency order before writing`() = runTest {
        val oldWallet = Wallet(
            id = "w-old-1",
            name = "Ví Cũ",
            type = WalletType.CASH,
            balance = Money(500_000L),
            colorHex = "#10B981",
            isDefault = false,
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        )
        val oldTx = FinanceTransaction(
            id = "tx-old-1",
            type = TransactionType.EXPENSE,
            amount = Money(200_000L),
            categoryId = "cat-custom-old",
            walletId = "w-old-1",
            note = "Cà phê",
            date = Instant.parse("2026-08-01T00:00:00Z"),
        )
        val oldCategory = Category(
            id = "cat-custom-old",
            name = "Custom Cũ",
            type = CategoryType.EXPENSE,
            icon = "ic_old",
            colorHex = "#999999",
            isDefault = false,
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        )

        every { walletRepository.observeWallets() } returns flowOf(listOf(oldWallet))
        every { transactionRepository.observePeriod(any(), any()) } returns flowOf(listOf(oldTx))
        every { categoryRepository.observeCategories() } returns flowOf(listOf(oldCategory))

        val snapshot = createStandardSnapshot()
        val result = useCase(snapshot, RestoreStrategy.FULL_OVERWRITE, currentUserId)

        assertInstanceOf(AppResult.Success::class.java, result)
        // Verify delete calls occurred
        coVerify { transactionRepository.deleteTransactionRaw(oldTx.id) }
        coVerify { categoryRepository.deleteCategory(oldCategory) }
        coVerify { walletRepository.deleteWalletRaw(oldWallet.id) }
    }

    // ─── T-RST-03: Smart Merge updates record when snapshot is newer ──────

    @Test
    fun `T-RST-03 Smart merge updates record when snapshot has newer updatedAt`() = runTest {
        val existingDebt = DebtAccount(
            id = "debt-1",
            userId = currentUserId,
            name = "Thẻ HSBC Cũ",
            type = DebtType.CREDIT_CARD,
            totalAmount = Money(20_000_000L),
            remainingBalance = Money(15_000_000L),
            interestRateApr = 18.0,
            minimumPayment = Money(1_000_000L),
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-08-01T00:00:00Z"), // older than snapshot's 2026-09-01
        )
        every { debtRepository.observeDebts() } returns flowOf(listOf(existingDebt))

        val snapshot = createStandardSnapshot()
        val result = useCase(snapshot, RestoreStrategy.SMART_MERGE, currentUserId)

        assertInstanceOf(AppResult.Success::class.java, result)
        val report = (result as AppResult.Success<RestoreReport>).value
        assertTrue(report.conflictsResolved > 0, "Conflict must be resolved in favor of newer record")
        coVerify { debtRepository.upsertDebt(match { it.id == "debt-1" && it.name == "Thẻ HSBC" }) }
    }

    // ─── T-RST-04: Smart Merge skips record when snapshot is older ────────

    @Test
    fun `T-RST-04 Smart merge skips record when snapshot is older than existing record`() = runTest {
        val existingDebt = DebtAccount(
            id = "debt-1",
            userId = currentUserId,
            name = "Thẻ HSBC Đã Cập Nhật Mới Nhất",
            type = DebtType.CREDIT_CARD,
            totalAmount = Money(20_000_000L),
            remainingBalance = Money(5_000_000L),
            interestRateApr = 18.0,
            minimumPayment = Money(1_000_000L),
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-09-20T00:00:00Z"), // newer than snapshot's 2026-09-01
        )
        every { debtRepository.observeDebts() } returns flowOf(listOf(existingDebt))

        val snapshot = createStandardSnapshot()
        val result = useCase(snapshot, RestoreStrategy.SMART_MERGE, currentUserId)

        assertInstanceOf(AppResult.Success::class.java, result)
        val report = (result as AppResult.Success<RestoreReport>).value
        assertTrue(report.skippedCount > 0, "Older record must be skipped")
        // Verify debt was NOT overwritten
        coVerify(exactly = 0) { debtRepository.upsertDebt(match { it.id == "debt-1" && it.name == "Thẻ HSBC" }) }
    }

    // ─── T-RST-05: SystemCategories are protected in both strategies ──────

    @Test
    fun `T-RST-05 SystemCategories are NEVER deleted or overwritten in either strategy`() = runTest {
        val foodCategory = Category(
            id = SystemCategories.FOOD,
            name = "Ăn uống Hệ Thống",
            type = CategoryType.EXPENSE,
            icon = "ic_food",
            colorHex = "#F97316",
            isDefault = true,
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
            isEssential = true,
        )
        every { categoryRepository.observeCategories() } returns flowOf(listOf(foodCategory))

        val snapshotWithFood = createStandardSnapshot().copy(
            categories = listOf(
                CategorySnapshot(
                    id = SystemCategories.FOOD,
                    name = "Tên Giả Mạo Từ Backup",
                    type = "expense",
                    icon = "ic_fake",
                    colorHex = "#000000",
                    isDefault = true,
                    isEssential = true,
                    createdAt = "2026-09-01T00:00:00Z",
                )
            )
        )

        // 1. Check Full Overwrite
        val overwriteResult = useCase(snapshotWithFood, RestoreStrategy.FULL_OVERWRITE, currentUserId)
        assertInstanceOf(AppResult.Success::class.java, overwriteResult)
        coVerify(exactly = 0) { categoryRepository.deleteCategory(foodCategory) }
        coVerify(exactly = 0) { categoryRepository.upsertCategory(match { it.id == SystemCategories.FOOD }) }

        // 2. Check Smart Merge
        val mergeResult = useCase(snapshotWithFood, RestoreStrategy.SMART_MERGE, currentUserId)
        assertInstanceOf(AppResult.Success::class.java, mergeResult)
        coVerify(exactly = 0) { categoryRepository.upsertCategory(match { it.id == SystemCategories.FOOD }) }
    }

    // ─── T-RST-06: Cross-account import remaps IDs cleanly ────────────────

    @Test
    fun `T-RST-06 Cross-account Full Overwrite keeps original entity IDs while updating owner to currentUserId`() = runTest {
        val foreignUserId = "user-bob-another-account"
        val snapshot = createStandardSnapshot(exportedByUid = foreignUserId)

        val capturedWallet = slot<Wallet>()
        val capturedTx = slot<FinanceTransaction>()
        val capturedDebt = slot<DebtAccount>()
        coEvery { walletRepository.restoreWalletRaw(capture(capturedWallet)) } returns AppResult.Success("ok")
        coEvery { transactionRepository.restoreTransactionRaw(capture(capturedTx)) } returns AppResult.Success("ok")
        coEvery { debtRepository.upsertDebt(capture(capturedDebt)) } returns AppResult.Success("ok")

        val result = useCase(snapshot, RestoreStrategy.FULL_OVERWRITE, currentUserId)

        assertInstanceOf(AppResult.Success::class.java, result)

        // 1. Full Overwrite keeps original wallet ID "w-1" (no duplicate random UUID)
        assertEquals("w-1", capturedWallet.captured.id)

        // 2. Transaction walletId points to original wallet ID "w-1"
        assertEquals("w-1", capturedTx.captured.walletId)
        assertEquals("tx-1", capturedTx.captured.id)

        // 3. Debt owner UID is updated to currentUserId
        assertEquals(currentUserId, capturedDebt.captured.userId)
        assertNotEquals(foreignUserId, capturedDebt.captured.userId)
        assertEquals("w-1", capturedDebt.captured.linkedWalletId)
    }

    // ─── T-RST-07: WalletBalanceAudit PASS when cashflow matches ──────────

    @Test
    fun `T-RST-07 WalletBalanceAudit PASS when wallet balance matches transaction cashflow`() = runTest {
        // wallet balance 1,000,000 = 1,000,000 income tx
        val snapshot = createStandardSnapshot(walletBalance = 1_000_000L, isConsistentCashflow = true)

        val result = useCase(snapshot, RestoreStrategy.FULL_OVERWRITE, currentUserId)

        assertInstanceOf(AppResult.Success::class.java, result)
        val report = (result as AppResult.Success<RestoreReport>).value
        assertTrue(report.balanceAuditPassed, "Audit should pass when cashflow equals balance")
        assertTrue(report.balanceDiscrepancies.isEmpty())
    }

    // ─── T-RST-08: WalletBalanceAudit detects discrepancy ──────────────────

    @Test
    fun `T-RST-08 WalletBalanceAudit detects balance discrepancy when transactions differ`() = runTest {
        // wallet balance 1,000,000 but tx is only 500,000 -> discrepancy 500,000
        val snapshot = createStandardSnapshot(
            walletBalance = 1_000_000L,
            txAmount = 500_000L,
            isConsistentCashflow = false,
        )

        val result = useCase(snapshot, RestoreStrategy.FULL_OVERWRITE, currentUserId)

        assertInstanceOf(AppResult.Success::class.java, result)
        val report = (result as AppResult.Success<RestoreReport>).value
        assertFalse(report.balanceAuditPassed, "Audit must fail when discrepancy exists")
        assertEquals(1, report.balanceDiscrepancies.size)
        assertTrue(report.balanceDiscrepancies[0].contains("Ví Tiền Mặt"))
        assertTrue(report.balanceDiscrepancies[0].contains("1000000"))
        assertTrue(report.balanceDiscrepancies[0].contains("500000"))
    }

    // ─── T-RST-09: Restoring active reminders schedules native alarms ──────

    @Test
    fun `T-RST-09 Restoring enabled reminders schedules them via ReminderScheduler`() = runTest {
        val snapshot = createStandardSnapshot()

        val result = useCase(snapshot, RestoreStrategy.FULL_OVERWRITE, currentUserId)

        assertInstanceOf(AppResult.Success::class.java, result)
        // Verify reminderScheduler was called for the active reminder
        verify(atLeast = 1) { reminderScheduler.schedule(match { it.enabled && it.title == "Tiền phòng" }) }
    }

    // ─── T-RST-10: Full Overwrite wipes all old records and preserves exact counts ─

    @Test
    fun `T-RST-10 Full Overwrite completely wipes old records and restores exact snapshot counts without duplication`() = runTest {
        val oldWalletCash = Wallet("cash-old", "Tiền mặt", WalletType.CASH, Money(0L), "#1F6FBF", isDefault = true, createdAt = Instant.now())
        val oldWalletVcb = Wallet("vcb-old", "Vietcombank", WalletType.BANK, Money(20_000L), "#1F6FBF", isDefault = false, createdAt = Instant.now())
        val oldSalaryTx = FinanceTransaction("tx-old-1", TransactionType.INCOME, Money(100_000L), "cat-salary", "vcb-old", date = Instant.now(), note = "Lương cũ")

        every { walletRepository.observeWallets() } returns flowOf(listOf(oldWalletCash, oldWalletVcb))
        every { transactionRepository.observePeriod(any(), any()) } returns flowOf(listOf(oldSalaryTx))

        val snapshot = createStandardSnapshot(walletBalance = 110_000L)

        val result = useCase(snapshot, RestoreStrategy.FULL_OVERWRITE, currentUserId)

        assertInstanceOf(AppResult.Success::class.java, result)
        val report = (result as AppResult.Success<RestoreReport>).value

        // Verify old records wiped
        coVerify(exactly = 1) { walletRepository.deleteWalletRaw("cash-old") }
        coVerify(exactly = 1) { walletRepository.deleteWalletRaw("vcb-old") }
        coVerify(exactly = 1) { transactionRepository.deleteTransactionRaw("tx-old-1") }

        // Verify new snapshot records restored without duplicate counts
        assertEquals(snapshot.wallets.size, report.walletsRestored)
        assertEquals(snapshot.transactions.size, report.transactionsRestored)
        coVerify(exactly = snapshot.wallets.size) { walletRepository.restoreWalletRaw(any()) }
        coVerify(exactly = snapshot.transactions.size) { transactionRepository.restoreTransactionRaw(any()) }
    }

    // ─── T-RST-11: Smart Merge maps Cash wallet to existing Cash wallet ───

    @Test
    fun `T-RST-11 Smart Merge maps Cash wallet from snapshot to existing Cash wallet without creating duplicate`() = runTest {
        val existingCash = Wallet("cash-device", "Tiền mặt", WalletType.CASH, Money(0L), "#1F6FBF", isDefault = true, createdAt = Instant.parse("2026-01-01T00:00:00Z"))
        every { walletRepository.observeWallets() } returns flowOf(listOf(existingCash))

        val snapshotWithForeignCash = createStandardSnapshot().copy(
            wallets = listOf(
                WalletSnapshot("cash-from-backup", "Tiền mặt", "cash", 0L, "#1F6FBF", isDefault = true, createdAt = "2026-01-02T00:00:00Z")
            ),
            transactions = listOf(
                TransactionSnapshot(
                    id = "tx-snap-1",
                    type = "income",
                    amount = 100_000L,
                    categoryId = "cat-1",
                    walletId = "cash-from-backup",
                    relatedWalletId = null,
                    dealId = null,
                    dealFlowType = null,
                    note = "Lương",
                    receiptImageUrl = null,
                    date = "2026-09-01T10:00:00Z",
                    createdAt = "2026-09-01T10:00:00Z",
                    updatedAt = "2026-09-01T10:00:00Z",
                )
            )
        )

        val capturedTx = slot<FinanceTransaction>()
        coEvery { transactionRepository.restoreTransactionRaw(capture(capturedTx)) } returns AppResult.Success("ok")

        val result = useCase(snapshotWithForeignCash, RestoreStrategy.SMART_MERGE, currentUserId)

        assertInstanceOf(AppResult.Success::class.java, result)
        val report = (result as AppResult.Success<RestoreReport>).value

        // NO new wallet was raw-restored (not duplicated)
        coVerify(exactly = 0) { walletRepository.restoreWalletRaw(match { it.id == "cash-from-backup" }) }

        // Transaction linked to "cash-from-backup" was remapped to existing "cash-device"
        assertEquals("cash-device", capturedTx.captured.walletId)
    }

    // ─── T-RST-12: Smart Merge deduplicates transactions by signature ─────

    @Test
    fun `T-RST-12 Smart Merge deduplicates transactions by signature and updates newer record without insert duplication`() = runTest {
        val existingTx = FinanceTransaction(
            id = "tx-local-1",
            type = TransactionType.INCOME,
            amount = Money(100_000L),
            categoryId = "cat-salary",
            walletId = "w-1",
            date = Instant.parse("2026-09-01T10:00:00Z"),
            note = "Lương tháng 9",
            updatedAt = Instant.parse("2026-09-01T10:00:00Z"),
        )
        every { transactionRepository.observePeriod(any(), any()) } returns flowOf(listOf(existingTx))

        // Snapshot has the same transaction (same wallet, amount, date, type, note) but different ID & newer updatedAt
        val snapshotWithSameTx = createStandardSnapshot().copy(
            transactions = listOf(
                TransactionSnapshot(
                    id = "tx-from-backup-diff-id",
                    type = "income",
                    amount = 100_000L,
                    categoryId = "cat-salary",
                    walletId = "w-1",
                    relatedWalletId = null,
                    dealId = null,
                    dealFlowType = null,
                    note = "Lương tháng 9",
                    receiptImageUrl = null,
                    date = "2026-09-01T10:00:00Z",
                    createdAt = "2026-09-01T10:00:00Z",
                    updatedAt = "2026-09-02T10:00:00Z", // newer
                )
            )
        )

        val capturedTx = slot<FinanceTransaction>()
        coEvery { transactionRepository.restoreTransactionRaw(capture(capturedTx)) } returns AppResult.Success("ok")

        val result = useCase(snapshotWithSameTx, RestoreStrategy.SMART_MERGE, currentUserId)

        assertInstanceOf(AppResult.Success::class.java, result)
        val report = (result as AppResult.Success<RestoreReport>).value

        // Verified conflict resolved, updated existing ID instead of inserting new
        assertEquals(1, report.conflictsResolved)
        assertEquals("tx-local-1", capturedTx.captured.id)
    }

    // ─── T-RST-13: Smart Merge Transaction Delta Reconciliation & Balance Audit ──

    @Test
    fun `T-RST-13 Smart merge performs transaction delta reconciliation and passes audit`() = runTest {
        val vcbWallet = Wallet(
            id = "w-vcb",
            name = "Vietcombank",
            type = WalletType.BANK,
            balance = Money(0L),
            colorHex = "#006633",
            isDefault = false,
            createdAt = Instant.parse("2026-09-01T16:00:00Z"),
        )
        val cashWallet = Wallet(
            id = "w-cash",
            name = "Tiền mặt",
            type = WalletType.CASH,
            balance = Money(3_000L),
            colorHex = "#10B981",
            isDefault = true,
            createdAt = Instant.parse("2026-09-01T16:00:00Z"),
        )
        every { walletRepository.observeWallets() } returns flowOf(listOf(vcbWallet, cashWallet))
        every { transactionRepository.observePeriod(any(), any()) } returns flowOf(emptyList())

        val capturedWallets = mutableListOf<Wallet>()
        coEvery { walletRepository.restoreWalletRaw(capture(capturedWallets)) } returns AppResult.Success("ok")
        coEvery { transactionRepository.restoreTransactionRaw(any()) } returns AppResult.Success("ok")

        // Snapshot contains: VCB wallet, 2 INCOME transactions (+5000 and +5000) for VCB; no transactions for Cash
        val snapshot = createStandardSnapshot().copy(
            wallets = listOf(
                WalletSnapshot(
                    id = "w-vcb",
                    name = "Vietcombank",
                    type = "bank",
                    balance = 10_000L,
                    colorHex = "#006633",
                    isDefault = false,
                    createdAt = "2026-09-01T15:00:00Z",
                    status = "active",
                )
            ),
            transactions = listOf(
                TransactionSnapshot(
                    id = "tx-1",
                    type = "income",
                    amount = 5_000L,
                    categoryId = "cat-salary",
                    walletId = "w-vcb",
                    relatedWalletId = null,
                    dealId = null,
                    dealFlowType = null,
                    note = "Thưởng 1",
                    receiptImageUrl = null,
                    date = "2026-09-01T10:00:00Z",
                    createdAt = "2026-09-01T10:00:00Z",
                    updatedAt = "2026-09-01T10:00:00Z",
                ),
                TransactionSnapshot(
                    id = "tx-2",
                    type = "income",
                    amount = 5_000L,
                    categoryId = "cat-salary",
                    walletId = "w-vcb",
                    relatedWalletId = null,
                    dealId = null,
                    dealFlowType = null,
                    note = "Thưởng 2",
                    receiptImageUrl = null,
                    date = "2026-09-01T11:00:00Z",
                    createdAt = "2026-09-01T11:00:00Z",
                    updatedAt = "2026-09-01T11:00:00Z",
                )
            )
        )

        val result = useCase(snapshot, RestoreStrategy.SMART_MERGE, currentUserId)

        assertInstanceOf(AppResult.Success::class.java, result)
        val report = (result as AppResult.Success<RestoreReport>).value

        // Assert report metrics
        assertEquals(1, report.walletsRestored, "VCB wallet balance was reconciled and counted")
        assertEquals(2, report.transactionsRestored)
        assertTrue(report.balanceAuditPassed, "Balance audit must pass after reconciliation")
        assertTrue(report.balanceDiscrepancies.isEmpty(), "No balance discrepancies should be reported")

        // Assert reconciled wallet balance sent to repository
        val reconciledVcb = capturedWallets.find { it.id == "w-vcb" }
        assertNotNull(reconciledVcb, "Reconciled VCB wallet must be restored")
        assertEquals(10_000L, reconciledVcb!!.balance.value, "VCB balance must be updated from 0 to 10,000")

        // Assert Cash wallet remained unchanged at 3,000 (not updated in repository)
        val reconciledCash = capturedWallets.find { it.id == "w-cash" }
        assertNull(reconciledCash, "Cash wallet without new transactions should not be overwritten")
    }

    // ─── T-RST-15: Smart Merge updates Goal when savedAmount changed ──────────────

    @Test
    fun `T-RST-15 Smart Merge Goal updates savedAmount when snapshot has different progress`() = runTest {
        // Existing goal on device: savedAmount = 5,000,000
        val existingGoal = FinancialGoal(
            id = "goal-1",
            name = "Mua xe",
            targetAmount = Money(50_000_000L),
            savedAmount = Money(5_000_000L),   // ← Device has 5M saved
            deadline = Instant.parse("2026-12-31T00:00:00Z"),
            category = "savings",
            monthlyContribution = Money(5_000_000L),
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        )
        every { goalRepository.observeGoals() } returns flowOf(listOf(existingGoal))

        // Snapshot has same goal but savedAmount = 10,000,000 (user deposited more)
        val snapshotWithUpdatedGoal = createStandardSnapshot().copy(
            goals = listOf(
                GoalSnapshot(
                    id = "goal-1",
                    name = "Mua xe",
                    targetAmount = 50_000_000L,
                    savedAmount = 10_000_000L,  // ← Snapshot has 10M (more progress)
                    deadline = "2026-12-31T00:00:00Z",
                    category = "savings",
                    monthlyContribution = 5_000_000L,
                    imageUri = null,
                    createdAt = "2026-01-01T00:00:00Z",
                )
            )
        )

        val capturedGoal = slot<FinancialGoal>()
        coEvery { goalRepository.upsertGoal(capture(capturedGoal)) } returns AppResult.Success("goal-1")

        val result = useCase(snapshotWithUpdatedGoal, RestoreStrategy.SMART_MERGE, currentUserId)

        assertInstanceOf(AppResult.Success::class.java, result)
        val report = (result as AppResult.Success<RestoreReport>).value

        // Must update, not skip
        assertEquals(1, report.conflictsResolved, "Goal with different savedAmount must be resolved as conflict")
        assertEquals(1, report.goalsRestored, "Goal must be counted in goalsRestored")

        // Updated goal must preserve original ID and have new savedAmount
        assertEquals("goal-1", capturedGoal.captured.id)
        assertEquals(10_000_000L, capturedGoal.captured.savedAmount.value,
            "savedAmount must be updated to 10,000,000 from snapshot")
    }

    // ─── T-RST-16: Smart Merge updates Budget when limitAmount changed ────────────

    @Test
    fun `T-RST-16 Smart Merge Budget updates limitAmount when snapshot has different limit`() = runTest {
        // Existing budget on device: limitAmount = 3,000,000
        val existingBudget = Budget(
            id = "cat-custom-1_month:2026-09",
            categoryId = "cat-custom-1",
            periodKey = "month:2026-09",
            limitAmount = Money(3_000_000L),    // ← Device has 3M limit
            spentAmount = Money(0L),
            notified80 = false,
            notified100 = false,
        )
        every { budgetRepository.observeBudgets(any()) } returns flowOf(listOf(existingBudget))

        // Snapshot has same budget (same natural key) but limitAmount = 5,000,000
        val snapshotWithUpdatedBudget = createStandardSnapshot().copy(
            budgets = listOf(
                BudgetSnapshot(
                    id = "cat-custom-1_month:2026-09",
                    categoryId = "cat-custom-1",
                    periodKey = "month:2026-09",
                    limitAmount = 5_000_000L,   // ← Snapshot has 5M limit (changed)
                    spentAmount = 0L,
                    notified80 = false,
                    notified100 = false,
                )
            )
        )

        val capturedBudget = slot<Budget>()
        coEvery { budgetRepository.upsertBudget(capture(capturedBudget)) } returns AppResult.Success("b-id")

        val result = useCase(snapshotWithUpdatedBudget, RestoreStrategy.SMART_MERGE, currentUserId)

        assertInstanceOf(AppResult.Success::class.java, result)
        val report = (result as AppResult.Success<RestoreReport>).value

        // Must update, not skip
        assertEquals(1, report.conflictsResolved, "Budget with different limitAmount must be resolved as conflict")
        assertEquals(1, report.budgetsRestored, "Budget must be counted in budgetsRestored")

        // Updated budget must preserve original ID and have new limitAmount
        assertEquals("cat-custom-1_month:2026-09", capturedBudget.captured.id)
        assertEquals(5_000_000L, capturedBudget.captured.limitAmount.value,
            "limitAmount must be updated to 5,000,000 from snapshot")
    }

    // ─── T-RST-17: Smart Merge skips Goal when no field has changed ──────────────

    @Test
    fun `T-RST-17 Smart Merge Goal skips update when all fields are identical to existing`() = runTest {
        val existingGoal = FinancialGoal(
            id = "goal-1",
            name = "Mua xe",
            targetAmount = Money(50_000_000L),
            savedAmount = Money(10_000_000L),
            deadline = Instant.parse("2026-12-31T00:00:00Z"),
            category = "savings",
            monthlyContribution = Money(5_000_000L),
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        )
        every { goalRepository.observeGoals() } returns flowOf(listOf(existingGoal))

        // Snapshot goal is IDENTICAL to device goal
        val snapshotIdentical = createStandardSnapshot() // goal-1 with same savedAmount = 10M

        val result = useCase(snapshotIdentical, RestoreStrategy.SMART_MERGE, currentUserId)

        assertInstanceOf(AppResult.Success::class.java, result)
        val report = (result as AppResult.Success<RestoreReport>).value

        // Must skip, not update
        assertTrue(report.skippedCount > 0, "Identical goal must be counted as skipped")
        assertEquals(0, report.goalsRestored, "Identical goal must NOT be counted in goalsRestored")
        coVerify(exactly = 0) { goalRepository.upsertGoal(match { it.id == "goal-1" }) }
    }

    // ─── T-RST-14: Smart Merge Full Ledger Reconciliation on Dirty State ──────────

    @Test
    fun `T-RST-14 Smart Merge performs full ledger balance reconciliation on dirty state where transactions exist but wallet balance was wrong`() = runTest {
        // Device state:
        // Vietcombank exists with balance = 0 (corrupted/desynced)
        val vcbWallet = Wallet(
            id = "w-vcb",
            name = "Vietcombank",
            type = WalletType.BANK,
            balance = Money(0L), // WRONG balance on device
            colorHex = "#006633",
            isDefault = false,
            createdAt = Instant.parse("2026-09-01T16:00:00Z"),
        )
        val cashWallet = Wallet(
            id = "w-cash",
            name = "Tiền mặt",
            type = WalletType.CASH,
            balance = Money(3_000L),
            colorHex = "#10B981",
            isDefault = true,
            createdAt = Instant.parse("2026-09-01T16:00:00Z"),
        )
        every { walletRepository.observeWallets() } returns flowOf(listOf(vcbWallet, cashWallet))

        // Device ALREADY has 2 income transactions of 5,000 for VCB in database
        val existingTx1 = FinanceTransaction(
            id = "tx-1",
            type = TransactionType.INCOME,
            amount = Money(5_000L),
            categoryId = "cat-salary",
            walletId = "w-vcb",
            date = Instant.parse("2026-09-01T10:00:00Z"),
            note = "Thưởng 1",
            updatedAt = Instant.parse("2026-09-01T10:00:00Z"),
        )
        val existingTx2 = FinanceTransaction(
            id = "tx-2",
            type = TransactionType.INCOME,
            amount = Money(5_000L),
            categoryId = "cat-salary",
            walletId = "w-vcb",
            date = Instant.parse("2026-09-01T11:00:00Z"),
            note = "Thưởng 2",
            updatedAt = Instant.parse("2026-09-01T11:00:00Z"),
        )
        every { transactionRepository.observePeriod(any(), any()) } returns flowOf(listOf(existingTx1, existingTx2))

        val capturedWallets = mutableListOf<Wallet>()
        coEvery { walletRepository.restoreWalletRaw(capture(capturedWallets)) } returns AppResult.Success("ok")
        coEvery { transactionRepository.restoreTransactionRaw(any()) } returns AppResult.Success("ok")

        // Snapshot has the same VCB wallet (balance = 10,000) and the same 2 transactions
        val snapshot = createStandardSnapshot().copy(
            wallets = listOf(
                WalletSnapshot(
                    id = "w-vcb",
                    name = "Vietcombank",
                    type = "bank",
                    balance = 10_000L,
                    colorHex = "#006633",
                    isDefault = false,
                    createdAt = "2026-09-01T15:00:00Z",
                    status = "active",
                )
            ),
            transactions = listOf(
                TransactionSnapshot(
                    id = "tx-1",
                    type = "income",
                    amount = 5_000L,
                    categoryId = "cat-salary",
                    walletId = "w-vcb",
                    relatedWalletId = null,
                    dealId = null,
                    dealFlowType = null,
                    note = "Thưởng 1",
                    receiptImageUrl = null,
                    date = "2026-09-01T10:00:00Z",
                    createdAt = "2026-09-01T10:00:00Z",
                    updatedAt = "2026-09-01T10:00:00Z",
                ),
                TransactionSnapshot(
                    id = "tx-2",
                    type = "income",
                    amount = 5_000L,
                    categoryId = "cat-salary",
                    walletId = "w-vcb",
                    relatedWalletId = null,
                    dealId = null,
                    dealFlowType = null,
                    note = "Thưởng 2",
                    receiptImageUrl = null,
                    date = "2026-09-01T11:00:00Z",
                    createdAt = "2026-09-01T11:00:00Z",
                    updatedAt = "2026-09-01T11:00:00Z",
                )
            )
        )

        val result = useCase(snapshot, RestoreStrategy.SMART_MERGE, currentUserId)

        assertInstanceOf(AppResult.Success::class.java, result)
        val report = (result as AppResult.Success<RestoreReport>).value

        // Transactions were already present -> skipped (0 inserted)
        assertEquals(0, report.transactionsRestored, "Duplicate transactions should be skipped")
        // But Full Ledger Reconciliation detected VCB stored balance (0) != ledger balance (10,000)
        assertEquals(1, report.walletsRestored, "VCB balance was repaired to 10,000")
        assertTrue(report.balanceAuditPassed, "Audit must pass after repair")
        assertTrue(report.balanceDiscrepancies.isEmpty(), "No discrepancies should remain")

        val reconciledVcb = capturedWallets.find { it.id == "w-vcb" }
        assertNotNull(reconciledVcb, "VCB wallet must be restored with correct balance")
        assertEquals(10_000L, reconciledVcb!!.balance.value, "VCB balance must jump from 0 to 10,000")
    }
}
