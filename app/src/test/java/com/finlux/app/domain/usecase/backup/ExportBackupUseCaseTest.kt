package com.finlux.app.domain.usecase.backup

import com.finlux.app.core.common.AppResult
import com.finlux.app.core.common.BackupChecksumHelper
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.BudgetPeriodBasis
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.CycleRolloverRule
import com.finlux.app.domain.model.DealCategory
import com.finlux.app.domain.model.DealStatus
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtPaymentHistory
import com.finlux.app.domain.model.DebtType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.FinancialDeal
import com.finlux.app.domain.model.FinancialGoal
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.PaydayRuleType
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.model.ReminderRecurrence
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.SalaryScheduleType
import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.model.SavingSpinFrequency
import com.finlux.app.domain.model.SavingSpinStep
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.model.backup.BackupPreviewSummary
import com.finlux.app.domain.model.backup.FinluxBackupSnapshot
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.DealRepository
import com.finlux.app.domain.repository.DebtRepository
import com.finlux.app.domain.repository.GoalRepository
import com.finlux.app.domain.repository.ReminderRepository
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.SavingSpinRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.FileInputStream
import java.time.Instant

/**
 * Unit tests for [ExportBackupUseCase] — covers spec cases T-EXP-01 to T-EXP-07
 * plus end-to-end roundtrip validation with [ValidateBackupUseCase].
 */
class ExportBackupUseCaseTest {

    @TempDir
    lateinit var tempCacheDir: File

    private val walletRepository: WalletRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()
    private val transactionRepository: TransactionRepository = mockk()
    private val budgetRepository: BudgetRepository = mockk()
    private val debtRepository: DebtRepository = mockk()
    private val goalRepository: GoalRepository = mockk()
    private val reminderRepository: ReminderRepository = mockk()
    private val salaryCycleRepository: SalaryCycleRepository = mockk()
    private val dealRepository: DealRepository = mockk()
    private val savingSpinRepository: SavingSpinRepository = mockk()

    private val currentUserId = "user-techlead-2026"
    private val appVersionName = "1.25.6"
    private val appVersionCode = 180

    private lateinit var useCase: ExportBackupUseCase

    @BeforeEach
    fun setUp() {
        useCase = ExportBackupUseCase(
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
            appVersionName = appVersionName,
            appVersionCode = appVersionCode,
            cacheDir = tempCacheDir,
        )
    }

    // ─── Test Fixture Helpers ───────────────────────────────────────────────

    private fun mockStandardData(
        walletCount: Int = 3,
        txCount: Int = 10,
        categoryCount: Int = 5,
        debtCount: Int = 2,
        paymentsPerDebt: Int = 3,
        goalCount: Int = 2,
        reminderCount: Int = 2,
        dealCount: Int = 1,
        includeSalaryCycle: Boolean = true,
        includeSavingSpin: Boolean = true,
    ) {
        val wallets = (1..walletCount).map { i ->
            Wallet(
                id = "wallet-$i",
                name = "Ví $i",
                type = WalletType.CASH,
                balance = Money(1_000_000L * i),
                colorHex = "#10B981",
                isDefault = i == 1,
                createdAt = Instant.parse("2026-01-01T00:00:00Z"),
                status = "active",
            )
        }

        val categories = (1..categoryCount).map { i ->
            Category(
                id = "cat-$i",
                name = "Danh mục $i",
                type = if (i % 2 == 0) CategoryType.INCOME else CategoryType.EXPENSE,
                icon = "ic_category_$i",
                colorHex = "#F97316",
                isDefault = i <= 2,
                createdAt = Instant.parse("2026-01-01T00:00:00Z"),
                isEssential = true,
            )
        }

        val transactions = (1..txCount).map { i ->
            FinanceTransaction(
                id = "tx-$i",
                type = TransactionType.EXPENSE,
                amount = Money(50_000L * i),
                categoryId = "cat-1",
                walletId = "wallet-1",
                note = "Giao dịch $i",
                date = Instant.parse("2026-09-0${(i % 9) + 1}T12:00:00Z"),
                createdAt = Instant.parse("2026-09-01T12:00:00Z"),
                updatedAt = Instant.parse("2026-09-01T12:00:00Z"),
            )
        }

        val budgets = listOf(
            Budget(
                id = "cat-1_month:2026-09",
                categoryId = "cat-1",
                periodKey = "month:2026-09",
                limitAmount = Money(5_000_000L),
                spentAmount = Money(1_500_000L),
                notified80 = false,
                notified100 = false,
            )
        )

        val debts = (1..debtCount).map { i ->
            DebtAccount(
                id = "debt-$i",
                userId = currentUserId,
                name = "Khoản nợ $i",
                type = DebtType.CREDIT_CARD,
                totalAmount = Money(20_000_000L),
                remainingBalance = Money(10_000_000L),
                interestRateApr = 18.5,
                minimumPayment = Money(1_000_000L),
                dueDate = 15,
                statementDate = 25,
                linkedWalletId = "wallet-1",
                gracePeriodDays = 45,
                colorHex = "#E11D48",
                isReminderEnabled = true,
                reminderDaysBefore = 3,
                isSettled = false,
                createdAt = Instant.parse("2026-01-01T00:00:00Z"),
                updatedAt = Instant.parse("2026-09-01T00:00:00Z"),
            )
        }

        val payments = debts.flatMap { debt ->
            (1..paymentsPerDebt).map { p ->
                DebtPaymentHistory(
                    id = "pay-${debt.id}-$p",
                    debtId = debt.id,
                    walletId = "wallet-1",
                    amount = Money(1_000_000L),
                    principalPaid = Money(900_000L),
                    interestPaid = Money(100_000L),
                    paymentDate = Instant.parse("2026-09-0${(p % 9) + 1}T10:00:00Z"),
                    note = "Thanh toán $p cho ${debt.name}",
                    isCreditCardPayment = true,
                )
            }
        }

        val goals = (1..goalCount).map { i ->
            FinancialGoal(
                id = "goal-$i",
                name = "Mục tiêu $i",
                targetAmount = Money(50_000_000L),
                savedAmount = Money(10_000_000L * i),
                deadline = Instant.parse("2026-12-31T00:00:00Z"),
                category = "savings",
                monthlyContribution = Money(5_000_000L),
                imageUri = null,
                createdAt = Instant.parse("2026-01-01T00:00:00Z"),
            )
        }

        val reminders = (1..reminderCount).map { i ->
            Reminder(
                id = "rem-$i",
                title = "Nhắc nhở $i",
                amount = Money(2_000_000L),
                categoryId = "cat-1",
                walletId = "wallet-1",
                recurrence = ReminderRecurrence.MONTHLY,
                startDate = Instant.parse("2026-01-01T00:00:00Z"),
                enabled = true,
                nextTriggerDate = Instant.parse("2026-10-01T08:00:00Z"),
            )
        }

        val deals = (1..dealCount).map { i ->
            FinancialDeal(
                id = "deal-$i",
                userId = currentUserId,
                title = "Thương vụ $i",
                description = "Mô tả $i",
                category = DealCategory.INVESTMENT,
                targetAmount = Money(100_000_000L),
                totalCapitalOutlay = Money(80_000_000L),
                totalRecovered = Money(20_000_000L),
                writtenOffCapital = Money(0L),
                netProfitLoss = Money(5_000_000L),
                status = DealStatus.ACTIVE,
                startDate = Instant.parse("2026-03-01T00:00:00Z"),
                endDate = null,
                createdAt = Instant.parse("2026-03-01T00:00:00Z"),
                updatedAt = Instant.parse("2026-09-01T00:00:00Z"),
            )
        }

        val salaryConfig = if (includeSalaryCycle) {
            SalaryCycleConfig(
                enabled = true,
                scheduleType = SalaryScheduleType.MONTHLY_ONCE,
                paydayRuleType = PaydayRuleType.DAY_OF_MONTH,
                paydayDay = 5,
                salaryWalletId = "wallet-1",
                expectedSalary = Money(25_000_000L),
                savingsWalletId = "wallet-1",
                rolloverRule = CycleRolloverRule.KEEP_IN_WALLET,
                budgetPeriodBasis = BudgetPeriodBasis.SALARY_CYCLE,
                financeTimeZone = "Asia/Ho_Chi_Minh",
            )
        } else {
            SalaryCycleConfig(enabled = false)
        }

        val spinConfig = if (includeSavingSpin) {
            SavingSpinConfig(
                enabled = true,
                showOnHome = true,
                minAmount = Money(10_000L),
                maxAmount = Money(100_000L),
                step = SavingSpinStep.FIVE_THOUSAND,
                slotCount = 8,
                frequency = SavingSpinFrequency.DAILY,
                selectedWeekdays = setOf(1, 2, 3, 4, 5),
                weeklyDay = 1,
                reminderHour = 9,
                reminderMinute = 0,
                reminderEnabled = true,
                snoozeEnabled = true,
                allowSkip = true,
                defaultDestinationId = "dest-1",
                createdAt = Instant.parse("2026-01-01T00:00:00Z"),
                updatedAt = Instant.parse("2026-09-01T00:00:00Z"),
            )
        } else {
            SavingSpinConfig(enabled = false)
        }

        every { walletRepository.observeWallets() } returns flowOf(wallets)
        every { categoryRepository.observeCategories() } returns flowOf(categories)
        every { transactionRepository.observePeriod(any(), any()) } returns flowOf(transactions)
        every { budgetRepository.observeBudgets(any()) } returns flowOf(budgets)
        every { debtRepository.observeDebts() } returns flowOf(debts)
        every { debtRepository.observeAllPaymentHistory() } returns flowOf(payments)
        every { goalRepository.observeGoals() } returns flowOf(goals)
        every { reminderRepository.observeReminders() } returns flowOf(reminders)
        every { salaryCycleRepository.observeConfig() } returns flowOf(salaryConfig)
        every { dealRepository.observeDeals() } returns flowOf(deals)
        every { savingSpinRepository.observeConfig() } returns flowOf(spinConfig)
    }

    // ─── T-EXP-01: Basic successful export ─────────────────────────────────

    @Test
    fun `T-EXP-01 basic successful export produces valid finlux file in cache`() = runTest {
        mockStandardData(walletCount = 3, txCount = 10, categoryCount = 5)

        val result = useCase(currentUserId)

        assertInstanceOf(AppResult.Success::class.java, result)
        val file = (result as AppResult.Success<File>).value
        assertTrue(file.exists(), "Output file must exist on disk")
        assertTrue(file.name.endsWith(".finlux"), "File extension must be .finlux")
        assertTrue(file.name.startsWith("FinLux_Backup_"), "File name must start with FinLux_Backup_")
        assertTrue(file.length() > 0, "File must not be empty")
    }

    // ─── T-EXP-02: Checksum SHA-256 integrity ──────────────────────────────

    @Test
    fun `T-EXP-02 embedded SHA-256 checksum matches recomputed digest of payload`() = runTest {
        mockStandardData()

        val result = useCase(currentUserId)
        val file = (result as AppResult.Success<File>).value
        val jsonContent = file.readText(Charsets.UTF_8)
        val root = JSONObject(jsonContent)

        val embeddedChecksum = root.getString("checksum")
        assertTrue(embeddedChecksum.isNotBlank(), "Embedded checksum must not be blank")
        assertEquals(64, embeddedChecksum.length, "SHA-256 checksum must be 64 hex chars")

        // Recompute with checksum = ""
        root.put("checksum", "")
        val expectedChecksum = BackupChecksumHelper.computeChecksum(root.toString())
        assertEquals(expectedChecksum, embeddedChecksum, "Checksum must match recomputed hash")
    }

    // ─── T-EXP-03: exportedAt metadata timestamp accuracy ─────────────────

    @Test
    fun `T-EXP-03 exportedAt timestamp is within 5 seconds of invocation time`() = runTest {
        mockStandardData()
        val beforeMillis = System.currentTimeMillis()

        val result = useCase(currentUserId)
        val afterMillis = System.currentTimeMillis()

        val file = (result as AppResult.Success<File>).value
        val root = JSONObject(file.readText(Charsets.UTF_8))
        val exportedAt = root.getLong("exportedAt")

        assertTrue(
            exportedAt in (beforeMillis - 1000)..(afterMillis + 1000),
            "exportedAt ($exportedAt) must be close to current time ($beforeMillis..$afterMillis)"
        )
    }

    // ─── T-EXP-04: Empty dataset export ───────────────────────────────────

    @Test
    fun `T-EXP-04 empty dataset exports successfully with empty entity arrays`() = runTest {
        mockStandardData(
            walletCount = 0,
            txCount = 0,
            categoryCount = 0,
            debtCount = 0,
            paymentsPerDebt = 0,
            goalCount = 0,
            reminderCount = 0,
            dealCount = 0,
            includeSalaryCycle = false,
            includeSavingSpin = false,
        )

        val result = useCase(currentUserId)

        assertInstanceOf(AppResult.Success::class.java, result)
        val file = (result as AppResult.Success<File>).value
        val root = JSONObject(file.readText(Charsets.UTF_8))

        assertEquals(0, root.getJSONArray("wallets").length())
        assertEquals(0, root.getJSONArray("categories").length())
        assertEquals(0, root.getJSONArray("transactions").length())
        assertEquals(0, root.getJSONArray("debts").length())
        assertEquals(0, root.getJSONArray("debtPayments").length())
        assertEquals(0, root.getJSONArray("goals").length())
        assertEquals(0, root.getJSONArray("reminders").length())
        assertEquals(0, root.getJSONArray("deals").length())
        assertFalse(root.getJSONObject("salaryCycleConfig").getBoolean("enabled"))
        assertFalse(root.getJSONObject("savingSpinConfig").getBoolean("enabled"))
    }

    // ─── T-EXP-05: exportedByUid equals currentUserId ─────────────────────

    @Test
    fun `T-EXP-05 exportedByUid matches currentUserId provided to use case`() = runTest {
        mockStandardData()
        val customUid = "user_specific_uid_987654"

        val result = useCase(customUid)

        val file = (result as AppResult.Success<File>).value
        val root = JSONObject(file.readText(Charsets.UTF_8))
        assertEquals(customUid, root.getString("exportedByUid"))
    }

    // ─── T-EXP-06: Schema version and app version match constants ─────────

    @Test
    fun `T-EXP-06 schemaVersion and appVersion match current app constants`() = runTest {
        mockStandardData()

        val result = useCase(currentUserId)

        val file = (result as AppResult.Success<File>).value
        val root = JSONObject(file.readText(Charsets.UTF_8))
        assertEquals(FinluxBackupSnapshot.CURRENT_SCHEMA_VERSION, root.getInt("schemaVersion"))
        assertEquals(appVersionName, root.getString("appVersion"))
        assertEquals(appVersionCode, root.getInt("appVersionCode"))
    }

    // ─── T-EXP-07: DebtPayments includes all payments across debts ────────

    @Test
    fun `T-EXP-07 debtPayments includes all payments across all debts`() = runTest {
        mockStandardData(debtCount = 2, paymentsPerDebt = 3) // 2 debts × 3 payments = 6

        val result = useCase(currentUserId)

        val file = (result as AppResult.Success<File>).value
        val root = JSONObject(file.readText(Charsets.UTF_8))
        val debts = root.getJSONArray("debts")
        val payments = root.getJSONArray("debtPayments")

        assertEquals(2, debts.length())
        assertEquals(6, payments.length(), "Must contain exactly 6 payments across 2 debts")
    }

    // ─── BONUS: End-to-end integration with ValidateBackupUseCase ──────────

    @Test
    fun `BONUS end-to-end export file passes ValidateBackupUseCase validation cleanly`() = runTest {
        mockStandardData(
            walletCount = 4,
            txCount = 25,
            categoryCount = 6,
            debtCount = 1,
            paymentsPerDebt = 2,
            goalCount = 3,
            reminderCount = 2,
            dealCount = 1,
            includeSalaryCycle = true,
            includeSavingSpin = true,
        )

        val exportResult = useCase(currentUserId)
        val file = (exportResult as AppResult.Success<File>).value

        // Feed exported file directly into ValidateBackupUseCase
        val validateUseCase = ValidateBackupUseCase(currentUserId)
        val validateResult = FileInputStream(file).use { stream ->
            validateUseCase(stream, file.length())
        }

        assertInstanceOf(AppResult.Success::class.java, validateResult)
        val summary = (validateResult as AppResult.Success<BackupPreviewSummary>).value
        assertTrue(summary.isChecksumValid)
        assertEquals(4, summary.walletCount)
        assertEquals(25, summary.transactionCount)
        assertEquals(6, summary.categoryCount)
        assertEquals(1, summary.debtCount)
        assertEquals(3, summary.goalCount)
        assertEquals(2, summary.reminderCount)
        assertEquals(1, summary.dealCount)
        assertTrue(summary.hasSalaryCycleConfig)
        assertTrue(summary.hasSavingSpinConfig)
        assertFalse(summary.isCrossAccount)
    }
}
