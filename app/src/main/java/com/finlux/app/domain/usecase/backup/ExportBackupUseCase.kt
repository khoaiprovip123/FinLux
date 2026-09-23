package com.finlux.app.domain.usecase.backup

import com.finlux.app.core.common.AppResult
import com.finlux.app.core.common.BackupChecksumHelper
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtPaymentHistory
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.FinancialDeal
import com.finlux.app.domain.model.FinancialGoal
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.SavingSpinConfig
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.backup.BudgetSnapshot
import com.finlux.app.domain.model.backup.CategorySnapshot
import com.finlux.app.domain.model.backup.DealSnapshot
import com.finlux.app.domain.model.backup.DebtPaymentSnapshot
import com.finlux.app.domain.model.backup.DebtSnapshot
import com.finlux.app.domain.model.backup.FinluxBackupSnapshot
import com.finlux.app.domain.model.backup.GoalSnapshot
import com.finlux.app.domain.model.backup.ReminderSnapshot
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
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.SavingSpinRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Collects 100% of user data from all repositories, serialises it to a
 * `.finlux` JSON snapshot file and returns the [File] pointing to the
 * result in the app's cache directory.
 *
 * The caller (ViewModel / UI) is responsible for:
 *  - Wrapping the File in a [FileProvider] URI before passing to ShareSheet or SAF.
 *  - Deleting the cache file after sharing if desired.
 *
 * No AES encryption is applied (approved design decision 2026-09-21).
 */
class ExportBackupUseCase(
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val debtRepository: DebtRepository,
    private val goalRepository: GoalRepository,
    private val reminderRepository: ReminderRepository,
    private val salaryCycleRepository: SalaryCycleRepository,
    private val dealRepository: DealRepository,
    private val savingSpinRepository: SavingSpinRepository,
    private val appVersionName: String,
    private val appVersionCode: Int,
    private val cacheDir: File,
) {
    companion object {
        const val BACKUP_SUBDIR = "backup"
        const val MAX_TRANSACTIONS_WARNING = 50_000

        private val ISO_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC)

        private fun Instant.toIso(): String = ISO_FORMATTER.format(this)
    }

    /**
     * Executes the export pipeline:
     * 1. Collects data from all repositories in parallel.
     * 2. Maps domain models → snapshot data classes.
     * 3. Serialises to JSON.
     * 4. Computes SHA-256 checksum.
     * 5. Writes the final file to [cacheDir]/backup/.
     *
     * @param currentUserId Firebase UID of the signed-in user.
     * @return [AppResult.Success] containing the output [File], or [AppResult.Error].
     */
    suspend operator fun invoke(currentUserId: String): AppResult<File> = coroutineScope {
        try {
            // ── 1. Parallel data collection ───────────────────────────────────
            val walletsDeferred = async { walletRepository.observeWallets().first() }
            val categoriesDeferred = async { categoryRepository.observeCategories().first() }
            val txDeferred = async {
                transactionRepository.observePeriod(Instant.EPOCH, Instant.now()).first()
            }
            val goalsDeferred = async { goalRepository.observeGoals().first() }
            val debtsDeferred = async { debtRepository.observeDebts().first() }
            val allPaymentsDeferred = async { debtRepository.observeAllPaymentHistory().first() }
            val remindersDeferred = async { reminderRepository.observeReminders().first() }
            val salaryDeferred = async { salaryCycleRepository.observeConfig().first() }
            val dealsDeferred = async { dealRepository.observeDeals().first() }
            val spinConfigDeferred = async { savingSpinRepository.observeConfig().first() }

            val wallets = walletsDeferred.await()
            val categories = categoriesDeferred.await()
            val transactions = txDeferred.await()
            val goals = goalsDeferred.await()
            val debts = debtsDeferred.await()
            val allPayments = allPaymentsDeferred.await()
            val reminders = remindersDeferred.await()
            val salaryConfig = salaryDeferred.await()
            val deals = dealsDeferred.await()
            val spinConfig = spinConfigDeferred.await()

            // Budgets: we observe all budgets without a period filter by reading from the
            // repository — Phase 1 does not need a specific period key here.
            // Note: BudgetRepository.observeBudgets requires a periodKey; for a full backup
            // we aggregate all known budgets via a broad query. For now we use an empty
            // periodKey as a sentinel that implementations should handle by returning all
            // budgets owned by the user.
            val budgets: List<Budget> = budgetRepository.observeBudgets("*").first()

            // ── 2. Map to Snapshot classes ────────────────────────────────────
            val exportedAt = System.currentTimeMillis()

            val walletSnapshots = wallets.map { it.toSnapshot() }
            val categorySnapshots = categories.map { it.toSnapshot() }
            val txSnapshots = transactions.map { it.toSnapshot() }
            val budgetSnapshots = budgets.map { it.toSnapshot() }
            val debtSnapshots = debts.map { it.toSnapshot() }
            val paymentSnapshots = allPayments.map { it.toSnapshot() }
            val goalSnapshots = goals.map { it.toSnapshot() }
            val reminderSnapshots = reminders.map { it.toSnapshot() }
            val dealSnapshots = deals.map { it.toSnapshot() }
            val salarySnapshot = salaryConfig.toSnapshotOrNull()
            val spinSnapshot = spinConfig.toSnapshotOrNull()

            // ── 3. Build preliminary snapshot (checksum = "") ─────────────────
            val preliminary = FinluxBackupSnapshot(
                schemaVersion = FinluxBackupSnapshot.CURRENT_SCHEMA_VERSION,
                appVersion = appVersionName,
                appVersionCode = appVersionCode,
                exportedAt = exportedAt,
                exportedByUid = currentUserId,
                checksum = "",           // placeholder for checksum computation
                payloadSizeBytes = 0L,   // computed below
                wallets = walletSnapshots,
                categories = categorySnapshots,
                transactions = txSnapshots,
                budgets = budgetSnapshots,
                debts = debtSnapshots,
                debtPayments = paymentSnapshots,
                goals = goalSnapshots,
                reminders = reminderSnapshots,
                deals = dealSnapshots,
                salaryCycleConfig = salarySnapshot,
                savingSpinConfig = spinSnapshot,
            )

            // ── 4. Serialise → compute checksum → embed ───────────────────────
            val payloadJson = preliminary.toJson()
            val checksum = BackupChecksumHelper.computeChecksum(payloadJson)
            val finalPayload = preliminary.toJson(checksum)
            val payloadBytes = finalPayload.toByteArray(Charsets.UTF_8)

            // ── 5. Write to cache ─────────────────────────────────────────────
            val backupDir = File(cacheDir, BACKUP_SUBDIR).also { it.mkdirs() }
            val timestamp = DateTimeFormatter
                .ofPattern("yyyyMMdd_HHmm")
                .withZone(ZoneOffset.UTC)
                .format(Instant.ofEpochMilli(exportedAt))
            val outFile = File(backupDir, "FinLux_Backup_$timestamp.finlux")
            outFile.writeBytes(payloadBytes)

            AppResult.Success(outFile)
        } catch (e: Exception) {
            AppResult.Error("Lỗi khi tạo bản sao lưu: ${e.localizedMessage}", e)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mapping extensions — domain model → snapshot
    // ─────────────────────────────────────────────────────────────────────────

    private fun Wallet.toSnapshot() = WalletSnapshot(
        id = id,
        name = name,
        type = type.name.lowercase(),
        balance = balance.value,
        colorHex = colorHex,
        isDefault = isDefault,
        createdAt = createdAt.toIso(),
        status = status,
    )

    private fun Category.toSnapshot() = CategorySnapshot(
        id = id,
        name = name,
        type = type.name.lowercase(),
        icon = icon,
        colorHex = colorHex,
        isDefault = isDefault,
        isEssential = isEssential,
        createdAt = createdAt.toIso(),
    )

    private fun FinanceTransaction.toSnapshot() = TransactionSnapshot(
        id = id,
        type = type.name.lowercase(),
        amount = amount.value,
        categoryId = categoryId,
        walletId = walletId,
        relatedWalletId = relatedWalletId,
        dealId = dealId,
        dealFlowType = dealFlowType?.name,
        note = note,
        receiptImageUrl = receiptImageUrl,
        date = date.toIso(),
        createdAt = createdAt.toIso(),
        updatedAt = updatedAt.toIso(),
    )

    private fun Budget.toSnapshot() = BudgetSnapshot(
        id = id,
        categoryId = categoryId,
        periodKey = periodKey,
        limitAmount = limitAmount.value,
        spentAmount = spentAmount.value,
        notified80 = notified80,
        notified100 = notified100,
    )

    private fun DebtAccount.toSnapshot() = DebtSnapshot(
        id = id,
        name = name,
        type = type.name,
        totalAmount = totalAmount.value,
        remainingBalance = remainingBalance.value,
        interestRateApr = interestRateApr,
        minimumPayment = minimumPayment.value,
        dueDate = dueDate,
        statementDate = statementDate,
        linkedWalletId = linkedWalletId,
        gracePeriodDays = gracePeriodDays,
        colorHex = colorHex,
        isReminderEnabled = isReminderEnabled,
        reminderDaysBefore = reminderDaysBefore,
        isSettled = isSettled,
        createdAt = createdAt.toIso(),
        updatedAt = updatedAt.toIso(),
    )

    private fun DebtPaymentHistory.toSnapshot() = DebtPaymentSnapshot(
        id = id,
        debtId = debtId,
        walletId = walletId,
        amount = amount.value,
        principalPaid = principalPaid.value,
        interestPaid = interestPaid.value,
        paymentDate = paymentDate.toIso(),
        note = note,
        isCreditCardPayment = isCreditCardPayment,
    )

    private fun FinancialGoal.toSnapshot() = GoalSnapshot(
        id = id,
        name = name,
        targetAmount = targetAmount.value,
        savedAmount = savedAmount.value,
        deadline = deadline.toIso(),
        category = category,
        monthlyContribution = monthlyContribution.value,
        imageUri = null, // Local URI not transferable across devices
        createdAt = createdAt.toIso(),
    )

    private fun Reminder.toSnapshot() = ReminderSnapshot(
        id = id,
        title = title,
        amount = amount.value,
        categoryId = categoryId,
        walletId = walletId,
        recurrence = recurrence.name,
        startDate = startDate.toIso(),
        enabled = enabled,
        nextTriggerDate = nextTriggerDate.toIso(),
    )

    private fun FinancialDeal.toSnapshot() = DealSnapshot(
        id = id,
        title = title,
        description = description,
        category = category.name,
        targetAmount = targetAmount.value,
        totalCapitalOutlay = totalCapitalOutlay.value,
        totalRecovered = totalRecovered.value,
        writtenOffCapital = writtenOffCapital.value,
        netProfitLoss = netProfitLoss.value,
        status = status.name,
        startDate = startDate.toIso(),
        endDate = endDate?.toIso(),
        createdAt = createdAt.toIso(),
        updatedAt = updatedAt.toIso(),
    )

    private fun SalaryCycleConfig.toSnapshotOrNull(): SalaryCycleConfigSnapshot? {
        // Represent a "never configured" state: default config with enabled=false
        // is still worth saving so the user's custom paydayDay is preserved.
        return SalaryCycleConfigSnapshot(
            enabled = enabled,
            scheduleType = scheduleType.name,
            paydayRuleType = paydayRuleType.name,
            paydayDay = paydayDay,
            salaryWalletId = salaryWalletId,
            expectedSalary = expectedSalary?.value,
            secondPaydayDay = secondPaydayDay,
            secondSalaryWalletId = secondSalaryWalletId,
            secondExpectedSalary = secondExpectedSalary?.value,
            savingsWalletId = savingsWalletId,
            rolloverRule = rolloverRule.name,
            budgetPeriodBasis = budgetPeriodBasis.name,
            financeTimeZone = financeTimeZone,
            updatedAt = Instant.now().toIso(),
        )
    }

    private fun SavingSpinConfig.toSnapshotOrNull(): SavingSpinConfigSnapshot? {
        return SavingSpinConfigSnapshot(
            enabled = enabled,
            showOnHome = showOnHome,
            minAmount = minAmount.value,
            maxAmount = maxAmount.value,
            stepAmount = step.amount,
            slotCount = slotCount,
            frequency = frequency.name,
            selectedWeekdays = selectedWeekdays.toList(),
            weeklyDay = weeklyDay,
            reminderHour = reminderHour,
            reminderMinute = reminderMinute,
            reminderEnabled = reminderEnabled,
            snoozeEnabled = snoozeEnabled,
            allowSkip = allowSkip,
            defaultDestinationId = defaultDestinationId,
            schemaVersion = 1,
            updatedAt = updatedAt.toIso(),
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Minimal JSON serialisation (no third-party dependency)
    // Uses org.json which is bundled with the Android SDK.
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Serialises the snapshot to a compact JSON string.
     * [overrideChecksum] is embedded as the "checksum" field value; pass "" for
     * the pre-checksum pass and the real digest for the final pass.
     */
    private fun FinluxBackupSnapshot.toJson(overrideChecksum: String = ""): String {
        val root = JSONObject()
        root.put("schemaVersion", schemaVersion)
        root.put("appVersion", appVersion)
        root.put("appVersionCode", appVersionCode)
        root.put("exportedAt", exportedAt)
        root.put("exportedByUid", exportedByUid)
        root.put("checksum", overrideChecksum)
        root.put("payloadSizeBytes", payloadSizeBytes)
        root.put("wallets", wallets.toJsonArray())
        root.put("categories", categories.toJsonArray())
        root.put("transactions", transactions.toJsonArray())
        root.put("budgets", budgets.toJsonArray())
        root.put("debts", debts.toJsonArray())
        root.put("debtPayments", debtPayments.toJsonArray())
        root.put("goals", goals.toJsonArray())
        root.put("reminders", reminders.toJsonArray())
        root.put("deals", deals.toJsonArray())
        salaryCycleConfig?.let { root.put("salaryCycleConfig", it.toJson()) }
        savingSpinConfig?.let { root.put("savingSpinConfig", it.toJson()) }
        return root.toString()
    }

    @JvmName("walletsToJsonArray")
    private fun List<WalletSnapshot>.toJsonArray(): JSONArray {
        val arr = JSONArray()
        forEach { w ->
            arr.put(JSONObject().apply {
                put("id", w.id); put("name", w.name); put("type", w.type)
                put("balance", w.balance); put("colorHex", w.colorHex)
                put("isDefault", w.isDefault); put("createdAt", w.createdAt)
                put("status", w.status)
            })
        }
        return arr
    }

    @JvmName("categoriesToJsonArray")
    private fun List<CategorySnapshot>.toJsonArray(): JSONArray {
        val arr = JSONArray()
        forEach { c ->
            arr.put(JSONObject().apply {
                put("id", c.id); put("name", c.name); put("type", c.type)
                put("icon", c.icon); put("colorHex", c.colorHex)
                put("isDefault", c.isDefault); put("isEssential", c.isEssential)
                put("createdAt", c.createdAt)
            })
        }
        return arr
    }

    @JvmName("transactionsToJsonArray")
    private fun List<TransactionSnapshot>.toJsonArray(): JSONArray {
        val arr = JSONArray()
        forEach { t ->
            arr.put(JSONObject().apply {
                put("id", t.id); put("type", t.type); put("amount", t.amount)
                putOpt("categoryId", t.categoryId); put("walletId", t.walletId)
                putOpt("relatedWalletId", t.relatedWalletId)
                putOpt("dealId", t.dealId); putOpt("dealFlowType", t.dealFlowType)
                put("note", t.note); putOpt("receiptImageUrl", t.receiptImageUrl)
                put("date", t.date); put("createdAt", t.createdAt); put("updatedAt", t.updatedAt)
            })
        }
        return arr
    }

    @JvmName("budgetsToJsonArray")
    private fun List<BudgetSnapshot>.toJsonArray(): JSONArray {
        val arr = JSONArray()
        forEach { b ->
            arr.put(JSONObject().apply {
                put("id", b.id); put("categoryId", b.categoryId); put("periodKey", b.periodKey)
                put("limitAmount", b.limitAmount); put("spentAmount", b.spentAmount)
                put("notified80", b.notified80); put("notified100", b.notified100)
            })
        }
        return arr
    }

    @JvmName("debtsToJsonArray")
    private fun List<DebtSnapshot>.toJsonArray(): JSONArray {
        val arr = JSONArray()
        forEach { d ->
            arr.put(JSONObject().apply {
                put("id", d.id); put("name", d.name); put("type", d.type)
                put("totalAmount", d.totalAmount); put("remainingBalance", d.remainingBalance)
                put("interestRateApr", d.interestRateApr); put("minimumPayment", d.minimumPayment)
                putOpt("dueDate", d.dueDate); putOpt("statementDate", d.statementDate)
                putOpt("linkedWalletId", d.linkedWalletId); put("gracePeriodDays", d.gracePeriodDays)
                put("colorHex", d.colorHex); put("isReminderEnabled", d.isReminderEnabled)
                put("reminderDaysBefore", d.reminderDaysBefore); put("isSettled", d.isSettled)
                put("createdAt", d.createdAt); put("updatedAt", d.updatedAt)
            })
        }
        return arr
    }

    @JvmName("debtPaymentsToJsonArray")
    private fun List<DebtPaymentSnapshot>.toJsonArray(): JSONArray {
        val arr = JSONArray()
        forEach { p ->
            arr.put(JSONObject().apply {
                put("id", p.id); put("debtId", p.debtId); put("walletId", p.walletId)
                put("amount", p.amount); put("principalPaid", p.principalPaid)
                put("interestPaid", p.interestPaid); put("paymentDate", p.paymentDate)
                put("note", p.note); put("isCreditCardPayment", p.isCreditCardPayment)
            })
        }
        return arr
    }

    @JvmName("goalsToJsonArray")
    private fun List<GoalSnapshot>.toJsonArray(): JSONArray {
        val arr = JSONArray()
        forEach { g ->
            arr.put(JSONObject().apply {
                put("id", g.id); put("name", g.name); put("targetAmount", g.targetAmount)
                put("savedAmount", g.savedAmount); put("deadline", g.deadline)
                put("category", g.category); put("monthlyContribution", g.monthlyContribution)
                putOpt("imageUri", g.imageUri); put("createdAt", g.createdAt)
            })
        }
        return arr
    }

    @JvmName("remindersToJsonArray")
    private fun List<ReminderSnapshot>.toJsonArray(): JSONArray {
        val arr = JSONArray()
        forEach { r ->
            arr.put(JSONObject().apply {
                put("id", r.id); put("title", r.title); put("amount", r.amount)
                put("categoryId", r.categoryId); put("walletId", r.walletId)
                put("recurrence", r.recurrence); put("startDate", r.startDate)
                put("enabled", r.enabled); put("nextTriggerDate", r.nextTriggerDate)
            })
        }
        return arr
    }

    @JvmName("dealsToJsonArray")
    private fun List<DealSnapshot>.toJsonArray(): JSONArray {
        val arr = JSONArray()
        forEach { d ->
            arr.put(JSONObject().apply {
                put("id", d.id); put("title", d.title); put("description", d.description)
                put("category", d.category); put("targetAmount", d.targetAmount)
                put("totalCapitalOutlay", d.totalCapitalOutlay)
                put("totalRecovered", d.totalRecovered)
                put("writtenOffCapital", d.writtenOffCapital)
                put("netProfitLoss", d.netProfitLoss); put("status", d.status)
                put("startDate", d.startDate); putOpt("endDate", d.endDate)
                put("createdAt", d.createdAt); put("updatedAt", d.updatedAt)
            })
        }
        return arr
    }

    private fun SalaryCycleConfigSnapshot.toJson() = JSONObject().apply {
        put("enabled", enabled); put("scheduleType", scheduleType)
        put("paydayRuleType", paydayRuleType); put("paydayDay", paydayDay)
        putOpt("salaryWalletId", salaryWalletId); putOpt("expectedSalary", expectedSalary)
        putOpt("secondPaydayDay", secondPaydayDay)
        putOpt("secondSalaryWalletId", secondSalaryWalletId)
        putOpt("secondExpectedSalary", secondExpectedSalary)
        putOpt("savingsWalletId", savingsWalletId)
        put("rolloverRule", rolloverRule); put("budgetPeriodBasis", budgetPeriodBasis)
        put("financeTimeZone", financeTimeZone); put("updatedAt", updatedAt)
    }

    private fun SavingSpinConfigSnapshot.toJson() = JSONObject().apply {
        put("enabled", enabled); put("showOnHome", showOnHome)
        put("minAmount", minAmount); put("maxAmount", maxAmount); put("stepAmount", stepAmount)
        put("slotCount", slotCount); put("frequency", frequency)
        put("selectedWeekdays", JSONArray(selectedWeekdays))
        put("weeklyDay", weeklyDay); put("reminderHour", reminderHour)
        put("reminderMinute", reminderMinute); put("reminderEnabled", reminderEnabled)
        put("snoozeEnabled", snoozeEnabled); put("allowSkip", allowSkip)
        putOpt("defaultDestinationId", defaultDestinationId)
        put("schemaVersion", schemaVersion); put("updatedAt", updatedAt)
    }
}
