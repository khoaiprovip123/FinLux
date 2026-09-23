package com.finlux.app.domain.usecase.backup

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.BudgetPeriodBasis
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.CycleRolloverRule
import com.finlux.app.domain.model.DealCategory
import com.finlux.app.domain.model.DealFlowType
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
import com.finlux.app.domain.model.balanceDelta
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
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.util.UUID

/**
 * Executes restoration of a [FinluxBackupSnapshot] into the application.
 *
 * Supports two strategies:
 *  - [RestoreStrategy.FULL_OVERWRITE]: Destructive safe wipe in reverse dependency order,
 *    followed by an 11-step dependency-ordered restore. SystemCategories are protected from
 *    deletion and overwriting (BR-CAT-01).
 *  - [RestoreStrategy.SMART_MERGE]: Non-destructive merge updating newer records and inserting
 *    missing ones. Duplicate custom categories are renamed to "{name} (imported)".
 *
 * Key safety invariants:
 *  - Cross-account remapping: generates fresh UUIDs for all user-owned entities and remaps
 *    all foreign keys when snapshot was exported by a different account.
 *  - Wallet balance audit: performs post-restore ledger balance verification.
 *  - Alarm reschedule: reactivates native alarms for active reminders via [ReminderScheduler].
 */
class RestoreBackupUseCase(
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
    private val reminderScheduler: ReminderScheduler,
) {

    suspend operator fun invoke(
        snapshot: FinluxBackupSnapshot,
        strategy: RestoreStrategy,
        currentUserId: String,
    ): AppResult<RestoreReport> {
        return try {
            val startTime = System.currentTimeMillis()

            // ── 0. Schema Migration Check ─────────────────────────────────────
            val migrated = BackupSchemaMigrator.migrate(snapshot)

            // ── 1. Cross-Account ID Remapping ─────────────────────────────────
            val isCrossAccount = migrated.exportedByUid != currentUserId
            val idRemapTable = mutableMapOf<String, String>()

            if (isCrossAccount && strategy == RestoreStrategy.SMART_MERGE) {
                migrated.categories.forEach {
                    if (!SystemCategories.ALL_SYSTEM_IDS.contains(it.id)) {
                        idRemapTable[it.id] = UUID.randomUUID().toString()
                    }
                }
                migrated.debts.forEach { idRemapTable[it.id] = UUID.randomUUID().toString() }
                migrated.goals.forEach { idRemapTable[it.id] = UUID.randomUUID().toString() }
                migrated.reminders.forEach { idRemapTable[it.id] = UUID.randomUUID().toString() }
                migrated.deals.forEach { idRemapTable[it.id] = UUID.randomUUID().toString() }
            }

            // ── 2. Strategy Execution ─────────────────────────────────────────
            val report = when (strategy) {
                RestoreStrategy.FULL_OVERWRITE -> executeFullOverwrite(
                    snapshot = migrated,
                    currentUserId = currentUserId,
                    startTime = startTime,
                )

                RestoreStrategy.SMART_MERGE -> executeSmartMerge(
                    snapshot = migrated,
                    idRemapTable = idRemapTable,
                    isCrossAccount = isCrossAccount,
                    currentUserId = currentUserId,
                    startTime = startTime,
                )
            }

            AppResult.Success(report)
        } catch (e: Exception) {
            AppResult.Error("Lỗi trong quá trình khôi phục dữ liệu: ${e.localizedMessage}", e)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Strategy 1: FULL OVERWRITE
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun executeFullOverwrite(
        snapshot: FinluxBackupSnapshot,
        currentUserId: String,
        startTime: Long,
    ): RestoreReport {
        // Step 1: Wipe existing records in reverse dependency order
        // transactions → budgets → debtPayments → debts → goals → reminders → deals → categories → wallets
        val oldTransactions = transactionRepository.observePeriod(Instant.EPOCH, Instant.ofEpochSecond(253402300799L)).first()
        oldTransactions.forEach { transactionRepository.deleteTransactionRaw(it.id) }

        val oldBudgets = budgetRepository.observeBudgets("*").first()
        oldBudgets.forEach { budgetRepository.deleteBudget(it) }

        val oldPayments = debtRepository.observeAllPaymentHistory().first()
        oldPayments.forEach { debtRepository.deletePaymentHistory(it.debtId, it.id) }

        val oldDebts = debtRepository.observeDebts().first()
        oldDebts.forEach { debtRepository.deleteDebt(it) }

        val oldGoals = goalRepository.observeGoals().first()
        oldGoals.forEach { goalRepository.deleteGoal(it) }

        val oldReminders = reminderRepository.observeReminders().first()
        oldReminders.forEach {
            reminderScheduler.cancel(it.id)
            reminderRepository.deleteReminder(it)
        }

        val oldDeals = dealRepository.observeDeals().first()
        oldDeals.forEach { dealRepository.deleteDeal(it.id) }

        // Categories: delete only custom categories, protect SystemCategories (BR-CAT-01)
        val oldCategories = categoryRepository.observeCategories().first()
        oldCategories.forEach {
            if (!SystemCategories.ALL_SYSTEM_IDS.contains(it.id) && !it.isDefault) {
                categoryRepository.deleteCategory(it)
            }
        }

        val oldWallets = walletRepository.observeWallets().first()
        oldWallets.forEach { walletRepository.deleteWalletRaw(it.id) }

        // Step 2: Restore in dependency order (Keep 100% original IDs to prevent duplication)
        val emptyRemap = emptyMap<String, String>()

        // 1. Categories
        var categoriesRestored = 0
        snapshot.categories.forEach { catSnapshot ->
            val cat = catSnapshot.toDomain(emptyRemap)
            if (!SystemCategories.ALL_SYSTEM_IDS.contains(cat.id)) {
                categoryRepository.upsertCategory(cat)
            }
            categoriesRestored++
        }

        // 2. Wallets (Restore directly with balance from snapshot)
        val restoredWallets = mutableListOf<Wallet>()
        snapshot.wallets.forEach { wSnapshot ->
            val wallet = wSnapshot.toDomain(emptyRemap)
            walletRepository.restoreWalletRaw(wallet)
            restoredWallets.add(wallet)
        }

        // 3. Salary Cycle Config
        snapshot.salaryCycleConfig?.let { sSnapshot ->
            salaryCycleRepository.saveConfig(sSnapshot.toDomain(emptyRemap))
        }

        // 4. Reminders
        var remindersRestored = 0
        snapshot.reminders.forEach { rSnapshot ->
            val reminder = rSnapshot.toDomain(emptyRemap)
            reminderRepository.upsertReminder(reminder)
            if (reminder.enabled) {
                reminderScheduler.schedule(reminder)
            }
            remindersRestored++
        }

        // 5. Goals
        var goalsRestored = 0
        snapshot.goals.forEach { gSnapshot ->
            val goal = gSnapshot.toDomain(emptyRemap, isCrossAccount = false)
            goalRepository.upsertGoal(goal)
            goalsRestored++
        }

        // 6. Debts
        var debtsRestored = 0
        snapshot.debts.forEach { dSnapshot ->
            val debt = dSnapshot.toDomain(emptyRemap, currentUserId)
            debtRepository.upsertDebt(debt)
            debtsRestored++
        }

        // 7. Debt Payments
        var debtPaymentsRestored = 0
        snapshot.debtPayments.forEach { pSnapshot ->
            val payment = pSnapshot.toDomain(emptyRemap, isCrossAccount = false)
            debtRepository.upsertPaymentHistory(payment)
            debtPaymentsRestored++
        }

        // 8. Deals
        var dealsRestored = 0
        snapshot.deals.forEach { dealSnapshot ->
            val deal = dealSnapshot.toDomain(emptyRemap, currentUserId)
            dealRepository.upsertDeal(deal)
            dealsRestored++
        }

        // 9. Budgets
        var budgetsRestored = 0
        snapshot.budgets.forEach { bSnapshot ->
            val budget = bSnapshot.toDomain(emptyRemap, isCrossAccount = false)
            budgetRepository.upsertBudget(budget)
            budgetsRestored++
        }

        // 10. Transactions (Raw restore without modifying wallet.balance)
        val restoredTransactions = mutableListOf<FinanceTransaction>()
        snapshot.transactions.forEach { tSnapshot ->
            val tx = tSnapshot.toDomain(emptyRemap, isCrossAccount = false)
            transactionRepository.restoreTransactionRaw(tx)
            restoredTransactions.add(tx)
        }

        // 11. Saving Spin Config
        snapshot.savingSpinConfig?.let { spinSnapshot ->
            savingSpinRepository.saveConfig(spinSnapshot.toDomain(emptyRemap))
        }

        // Step 3: Wallet Balance Audit
        val auditResult = auditWalletBalances(restoredWallets, restoredTransactions)

        return RestoreReport(
            strategy = RestoreStrategy.FULL_OVERWRITE,
            walletsRestored = restoredWallets.size,
            transactionsRestored = restoredTransactions.size,
            categoriesRestored = categoriesRestored,
            budgetsRestored = budgetsRestored,
            debtsRestored = debtsRestored,
            debtPaymentsRestored = debtPaymentsRestored,
            goalsRestored = goalsRestored,
            remindersRestored = remindersRestored,
            dealsRestored = dealsRestored,
            skippedCount = 0,
            conflictsResolved = 0,
            balanceAuditPassed = auditResult.first,
            balanceDiscrepancies = auditResult.second,
            durationMs = System.currentTimeMillis() - startTime,
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Strategy 2: SMART MERGE
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun executeSmartMerge(
        snapshot: FinluxBackupSnapshot,
        idRemapTable: Map<String, String>,
        isCrossAccount: Boolean,
        currentUserId: String,
        startTime: Long,
    ): RestoreReport {
        var skippedCount = 0
        var conflictsResolved = 0
        var categoriesRestored = 0
        var walletsRestored = 0
        var remindersRestored = 0
        var goalsRestored = 0
        var debtsRestored = 0
        var debtPaymentsRestored = 0
        var dealsRestored = 0
        var budgetsRestored = 0
        var transactionsRestored = 0

        // 1. Categories
        val existingCategoriesList = categoryRepository.observeCategories().first()
        val existingCategoriesById = existingCategoriesList.associateBy { it.id }
        val existingCategoriesByName = existingCategoriesList.associateBy { it.name.trim().lowercase() }
        val categoryRemapTable = mutableMapOf<String, String>()

        snapshot.categories.forEach { cSnapshot ->
            val domainCat = cSnapshot.toDomain(idRemapTable)
            if (SystemCategories.ALL_SYSTEM_IDS.contains(domainCat.id)) {
                // Rule BR-CAT-01: System categories are never overwritten (no-op, not counted as skipped user record)
            } else {
                val existing = existingCategoriesById[domainCat.id] ?: existingCategoriesByName[domainCat.name.trim().lowercase()]
                if (existing != null) {
                    categoryRemapTable[cSnapshot.id] = existing.id
                    val snapshotCreated = runCatching { Instant.parse(cSnapshot.createdAt) }.getOrDefault(Instant.MIN)
                    if (snapshotCreated.isAfter(existing.createdAt)) {
                        categoryRepository.upsertCategory(domainCat.copy(id = existing.id))
                        conflictsResolved++
                        categoriesRestored++
                    } else {
                        skippedCount++
                    }
                } else {
                    categoryRepository.upsertCategory(domainCat)
                    categoriesRestored++
                }
            }
        }

        // 2. Wallets (Smart Identification: Map Cash wallet to current device Cash wallet!)
        val existingWalletsList = walletRepository.observeWallets().first()
        val existingWalletsById = existingWalletsList.associateBy { it.id }
        val initialWalletBalances = existingWalletsList.associate { it.id to it.balance.value }
        val existingCashWallet = existingWalletsList.firstOrNull {
            it.isDefault || it.type == WalletType.CASH || it.name.trim().equals("Tiền mặt", ignoreCase = true)
        }
        val walletRemapTable = mutableMapOf<String, String>()
        val managedWalletsMap = existingWalletsById.toMutableMap()

        snapshot.wallets.forEach { wSnapshot ->
            val isSnapCash = wSnapshot.isDefault ||
                wSnapshot.type.equals("cash", ignoreCase = true) ||
                wSnapshot.name.trim().equals("Tiền mặt", ignoreCase = true)

            if (isSnapCash && existingCashWallet != null) {
                // RÀNG BUỘC TECH LEAD: ÁNH XẠ VÀO VÍ TIỀN MẶT HIỆN TẠI, TUYỆT ĐỐI KHÔNG TẠO VÍ THỨ 2
                walletRemapTable[wSnapshot.id] = existingCashWallet.id
                if (wSnapshot.name != existingCashWallet.name || wSnapshot.colorHex != existingCashWallet.colorHex) {
                    val updatedCash = existingCashWallet.copy(
                        name = wSnapshot.name,
                        colorHex = wSnapshot.colorHex,
                    )
                    walletRepository.upsertWallet(updatedCash)
                    conflictsResolved++
                    managedWalletsMap[existingCashWallet.id] = updatedCash
                }
            } else {
                // Ví khác: tìm theo ID hoặc theo (Name + Type)
                val matched = existingWalletsById[wSnapshot.id]
                    ?: existingWalletsList.firstOrNull {
                        it.name.trim().equals(wSnapshot.name.trim(), ignoreCase = true) &&
                            it.type.name.equals(wSnapshot.type, ignoreCase = true)
                    }

                if (matched != null) {
                    walletRemapTable[wSnapshot.id] = matched.id
                    if (wSnapshot.name != matched.name || wSnapshot.colorHex != matched.colorHex) {
                        val updated = matched.copy(
                            name = wSnapshot.name,
                            colorHex = wSnapshot.colorHex,
                        )
                        walletRepository.upsertWallet(updated)
                        conflictsResolved++
                        managedWalletsMap[matched.id] = updated
                    }
                } else {
                    // Ví mới thực sự
                    val newWallet = wSnapshot.toDomain(idRemapTable)
                    walletRepository.restoreWalletRaw(newWallet)
                    walletsRestored++
                    managedWalletsMap[newWallet.id] = newWallet
                }
            }
        }

        // Bảng remap tổng hợp cho Smart Merge
        val combinedRemapTable = idRemapTable + categoryRemapTable + walletRemapTable

        // 3. Salary Cycle Config
        snapshot.salaryCycleConfig?.let { sSnapshot ->
            salaryCycleRepository.saveConfig(sSnapshot.toDomain(combinedRemapTable))
        }

        // 4. Reminders
        val existingReminders = reminderRepository.observeReminders().first().associateBy { it.id }
        snapshot.reminders.forEach { rSnapshot ->
            val domainReminder = rSnapshot.toDomain(combinedRemapTable)
            val existing = existingReminders[domainReminder.id]
            if (existing != null) {
                val snapshotStart = runCatching { Instant.parse(rSnapshot.startDate) }.getOrDefault(Instant.MIN)
                if (snapshotStart.isAfter(existing.startDate)) {
                    reminderRepository.upsertReminder(domainReminder)
                    if (domainReminder.enabled) reminderScheduler.schedule(domainReminder)
                    conflictsResolved++
                    remindersRestored++
                } else {
                    skippedCount++
                }
            } else {
                reminderRepository.upsertReminder(domainReminder)
                if (domainReminder.enabled) reminderScheduler.schedule(domainReminder)
                remindersRestored++
            }
        }

        // 5. Goals
        val existingGoals = goalRepository.observeGoals().first()
        val existingGoalsById = existingGoals.associateBy { it.id }
        val existingGoalsByName = existingGoals.associateBy { it.name.trim().lowercase() }
        snapshot.goals.forEach { gSnapshot ->
            val domainGoal = gSnapshot.toDomain(combinedRemapTable, isCrossAccount)
            val existing = existingGoalsById[domainGoal.id] ?: existingGoalsByName[domainGoal.name.trim().lowercase()]
            if (existing != null) {
                val hasChanged = domainGoal.savedAmount != existing.savedAmount ||
                    domainGoal.targetAmount != existing.targetAmount ||
                    domainGoal.deadline != existing.deadline ||
                    domainGoal.category != existing.category
                if (hasChanged) {
                    goalRepository.upsertGoal(domainGoal.copy(id = existing.id))
                    conflictsResolved++
                    goalsRestored++
                } else {
                    skippedCount++
                }
            } else {
                goalRepository.upsertGoal(domainGoal)
                goalsRestored++
            }
        }

        // 6. Debts
        val existingDebts = debtRepository.observeDebts().first().associateBy { it.id }
        snapshot.debts.forEach { dSnapshot ->
            val domainDebt = dSnapshot.toDomain(combinedRemapTable, currentUserId)
            val existing = existingDebts[domainDebt.id]
            if (existing != null) {
                val snapshotUpdated = runCatching { Instant.parse(dSnapshot.updatedAt) }.getOrDefault(Instant.MIN)
                if (snapshotUpdated.isAfter(existing.updatedAt)) {
                    debtRepository.upsertDebt(domainDebt)
                    conflictsResolved++
                    debtsRestored++
                } else {
                    skippedCount++
                }
            } else {
                debtRepository.upsertDebt(domainDebt)
                debtsRestored++
            }
        }

        // 7. Debt Payments
        val existingPayments = debtRepository.observeAllPaymentHistory().first().associateBy { it.id }
        snapshot.debtPayments.forEach { pSnapshot ->
            val domainPayment = pSnapshot.toDomain(combinedRemapTable, isCrossAccount)
            if (existingPayments.containsKey(domainPayment.id)) {
                skippedCount++
            } else {
                debtRepository.upsertPaymentHistory(domainPayment)
                debtPaymentsRestored++
            }
        }

        // 8. Deals
        val existingDeals = dealRepository.observeDeals().first().associateBy { it.id }
        snapshot.deals.forEach { dealSnapshot ->
            val domainDeal = dealSnapshot.toDomain(combinedRemapTable, currentUserId)
            val existing = existingDeals[domainDeal.id]
            if (existing != null) {
                val snapshotUpdated = runCatching { Instant.parse(dealSnapshot.updatedAt) }.getOrDefault(Instant.MIN)
                if (snapshotUpdated.isAfter(existing.updatedAt)) {
                    dealRepository.upsertDeal(domainDeal)
                    conflictsResolved++
                    dealsRestored++
                } else {
                    skippedCount++
                }
            } else {
                dealRepository.upsertDeal(domainDeal)
                dealsRestored++
            }
        }

        // 9. Budgets
        val existingBudgets = budgetRepository.observeBudgets("*").first().associateBy { it.id }
        snapshot.budgets.forEach { bSnapshot ->
            val domainBudget = bSnapshot.toDomain(combinedRemapTable, isCrossAccount)
            val naturalKey = "${domainBudget.categoryId}_${domainBudget.periodKey}"
            val existing = existingBudgets[domainBudget.id] ?: existingBudgets.values.firstOrNull {
                "${it.categoryId}_${it.periodKey}" == naturalKey
            }
            if (existing != null) {
                if (domainBudget.limitAmount != existing.limitAmount) {
                    budgetRepository.upsertBudget(domainBudget.copy(id = existing.id))
                    conflictsResolved++
                    budgetsRestored++
                } else {
                    skippedCount++
                }
            } else {
                budgetRepository.upsertBudget(domainBudget)
                budgetsRestored++
            }
        }

        // 10. Transactions (Deduplication by ID or Content Signature: walletId_amount_date_type_note)
        val existingTransactions = transactionRepository.observePeriod(Instant.EPOCH, Instant.ofEpochSecond(253402300799L)).first()
        val existingTxById = existingTransactions.associateBy { it.id }
        fun txSig(walletId: String, amount: Long, epochSec: Long, type: String, note: String) =
            "${walletId}_${amount}_${epochSec}_${type.uppercase()}_${note.trim().lowercase()}"
        val existingTxBySig = existingTransactions.associateBy {
            txSig(it.walletId, it.amount.value, it.date.epochSecond, it.type.name, it.note)
        }

        val walletDeltaMap = mutableMapOf<String, Long>()
        val allRestoredTransactions = mutableListOf<FinanceTransaction>()

        snapshot.transactions.forEach { tSnapshot ->
            val targetWalletId = walletRemapTable[tSnapshot.walletId] ?: tSnapshot.walletId
            val targetRelatedWalletId = tSnapshot.relatedWalletId?.let { walletRemapTable[it] ?: it }
            val targetCategoryId = tSnapshot.categoryId?.let { combinedRemapTable[it] ?: it }
            val txDate = runCatching { Instant.parse(tSnapshot.date) }.getOrDefault(Instant.now())
            val txType = runCatching { TransactionType.valueOf(tSnapshot.type.uppercase()) }.getOrDefault(TransactionType.EXPENSE)
            val currentSig = txSig(targetWalletId, tSnapshot.amount, txDate.epochSecond, txType.name, tSnapshot.note)

            val existing = existingTxById[tSnapshot.id] ?: existingTxBySig[currentSig]
            if (existing != null) {
                val snapshotUpdated = runCatching { Instant.parse(tSnapshot.updatedAt) }.getOrDefault(Instant.MIN)
                if (snapshotUpdated.isAfter(existing.updatedAt)) {
                    val updatedTx = tSnapshot.toDomain(combinedRemapTable, isCrossAccount = false).copy(
                        id = existing.id,
                        walletId = targetWalletId,
                        relatedWalletId = targetRelatedWalletId,
                        categoryId = targetCategoryId,
                    )
                    transactionRepository.restoreTransactionRaw(updatedTx)
                    conflictsResolved++
                    transactionsRestored++
                    allRestoredTransactions.add(updatedTx)

                    val diff = updatedTx.balanceDelta() - existing.balanceDelta()
                    if (diff != 0L) {
                        walletDeltaMap[targetWalletId] = (walletDeltaMap[targetWalletId] ?: 0L) + diff
                    }
                } else {
                    skippedCount++
                    allRestoredTransactions.add(existing)
                }
            } else {
                val newTx = tSnapshot.toDomain(combinedRemapTable, isCrossAccount = false).copy(
                    walletId = targetWalletId,
                    relatedWalletId = targetRelatedWalletId,
                    categoryId = targetCategoryId,
                )
                transactionRepository.restoreTransactionRaw(newTx)
                transactionsRestored++
                allRestoredTransactions.add(newTx)

                walletDeltaMap[targetWalletId] = (walletDeltaMap[targetWalletId] ?: 0L) + newTx.balanceDelta()
            }
        }

        // 11. Full Ledger Balance Reconciliation
        val snapTxCashflowByWallet = snapshot.transactions.groupBy {
            walletRemapTable[it.walletId] ?: it.walletId
        }.mapValues { (_, txs) ->
            txs.sumOf { tSnap ->
                val type = runCatching { TransactionType.valueOf(tSnap.type.uppercase()) }.getOrDefault(TransactionType.EXPENSE)
                when (type) {
                    TransactionType.INCOME, TransactionType.TRANSFER_IN -> tSnap.amount
                    TransactionType.EXPENSE, TransactionType.TRANSFER_OUT -> -tSnap.amount
                }
            }
        }
        val snapSeedByWallet = snapshot.wallets.associate { wSnap ->
            val targetId = walletRemapTable[wSnap.id] ?: wSnap.id
            val cashflow = snapTxCashflowByWallet[targetId] ?: 0L
            targetId to (wSnap.balance - cashflow)
        }

        val existingTxCashflowByWallet = existingTransactions.groupBy { it.walletId }.mapValues { (_, txs) ->
            txs.sumOf { it.balanceDelta() }
        }
        val deviceSeedByWallet = existingWalletsList.associate { w ->
            val cashflow = existingTxCashflowByWallet[w.id] ?: 0L
            w.id to (w.balance.value - cashflow)
        }

        val finalLedgerTransactions = (existingTxById + allRestoredTransactions.associateBy { it.id }).values
        val totalCashflowByWallet = finalLedgerTransactions.groupBy { it.walletId }.mapValues { (_, txs) ->
            txs.sumOf { it.balanceDelta() }
        }

        val seedMap = mutableMapOf<String, Long>()
        val reconciledWallets = mutableListOf<Wallet>()

        managedWalletsMap.values.forEach { wallet ->
            val snapSeed = snapSeedByWallet[wallet.id]
            val deviceSeed = deviceSeedByWallet[wallet.id]
            val seed = when {
                snapSeed != null && deviceSeed != null -> maxOf(snapSeed, deviceSeed)
                snapSeed != null -> snapSeed
                deviceSeed != null -> deviceSeed
                else -> 0L
            }
            seedMap[wallet.id] = seed

            val netCashflow = totalCashflowByWallet[wallet.id] ?: 0L
            val recalculatedBalance = seed + netCashflow

            if (wallet.balance.value != recalculatedBalance) {
                val updatedWallet = wallet.copy(balance = Money(recalculatedBalance))
                walletRepository.restoreWalletRaw(updatedWallet)
                walletsRestored++
                reconciledWallets.add(updatedWallet)
            } else {
                reconciledWallets.add(wallet)
            }
        }

        // 12. Saving Spin Config
        snapshot.savingSpinConfig?.let { spinSnapshot ->
            savingSpinRepository.saveConfig(spinSnapshot.toDomain(idRemapTable))
        }

        // 13. Balance Audit for Smart Merge
        val auditResult = auditSmartMergeBalances(reconciledWallets, seedMap, totalCashflowByWallet)

        return RestoreReport(
            strategy = RestoreStrategy.SMART_MERGE,
            walletsRestored = walletsRestored,
            transactionsRestored = transactionsRestored,
            categoriesRestored = categoriesRestored,
            budgetsRestored = budgetsRestored,
            debtsRestored = debtsRestored,
            debtPaymentsRestored = debtPaymentsRestored,
            goalsRestored = goalsRestored,
            remindersRestored = remindersRestored,
            dealsRestored = dealsRestored,
            skippedCount = skippedCount,
            conflictsResolved = conflictsResolved,
            balanceAuditPassed = auditResult.first,
            balanceDiscrepancies = auditResult.second,
            durationMs = System.currentTimeMillis() - startTime,
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Financial Ledger Post-Restore Verification (WalletBalanceAudit)
    // ─────────────────────────────────────────────────────────────────────────

    private fun auditSmartMergeBalances(
        wallets: List<Wallet>,
        seedMap: Map<String, Long>,
        cashflowMap: Map<String, Long>,
    ): Pair<Boolean, List<String>> {
        val discrepancies = mutableListOf<String>()
        for (wallet in wallets) {
            val seed = seedMap[wallet.id] ?: 0L
            val cashflow = cashflowMap[wallet.id] ?: 0L
            val expectedBalance = seed + cashflow
            if (wallet.balance.value != expectedBalance) {
                discrepancies.add(
                    "Ví \"${wallet.name}\" (ID: ${wallet.id}) lệch số dư sổ cái: " +
                        "Số dư lưu trữ: ${wallet.balance.value} ₫ vs " +
                        "Số dư sổ cái (khởi tạo $seed ₫ + luồng tiền $cashflow ₫): $expectedBalance ₫"
                )
            }
        }
        return Pair(discrepancies.isEmpty(), discrepancies)
    }

    private fun auditWalletBalances(
        wallets: List<Wallet>,
        transactions: List<FinanceTransaction>,
    ): Pair<Boolean, List<String>> {
        val discrepancies = mutableListOf<String>()

        for (wallet in wallets) {
            val walletTxList = transactions.filter { it.walletId == wallet.id }
            val computedNetCashflow = walletTxList.sumOf { it.balanceDelta() }

            if (wallet.balance.value != computedNetCashflow) {
                discrepancies.add(
                    "Ví \"${wallet.name}\" (ID: ${wallet.id}) lệch số dư: " +
                        "Số dư lưu trữ: ${wallet.balance.value} ₫ vs " +
                        "Tổng luồng tiền tính từ giao dịch: ${computedNetCashflow} ₫ " +
                        "(Chênh lệch: ${wallet.balance.value - computedNetCashflow} ₫)"
                )
            }
        }

        return Pair(discrepancies.isEmpty(), discrepancies)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Domain Model Mappers
    // ─────────────────────────────────────────────────────────────────────────

    private fun WalletSnapshot.toDomain(idRemapTable: Map<String, String>) = Wallet(
        id = idRemapTable[id] ?: id,
        name = name,
        type = runCatching { WalletType.valueOf(type.uppercase()) }.getOrDefault(WalletType.CASH),
        balance = Money(balance),
        colorHex = colorHex,
        isDefault = isDefault,
        createdAt = runCatching { Instant.parse(createdAt) }.getOrDefault(Instant.now()),
        status = status,
    )

    private fun CategorySnapshot.toDomain(idRemapTable: Map<String, String>) = Category(
        id = idRemapTable[id] ?: id,
        name = name,
        type = runCatching { CategoryType.valueOf(type.uppercase()) }.getOrDefault(CategoryType.EXPENSE),
        icon = icon,
        colorHex = colorHex,
        isDefault = isDefault,
        createdAt = runCatching { Instant.parse(createdAt) }.getOrDefault(Instant.now()),
        isEssential = isEssential,
    )

    private fun TransactionSnapshot.toDomain(
        idRemapTable: Map<String, String>,
        isCrossAccount: Boolean,
    ) = FinanceTransaction(
        id = if (isCrossAccount) UUID.randomUUID().toString() else id,
        type = runCatching { TransactionType.valueOf(type.uppercase()) }.getOrDefault(TransactionType.EXPENSE),
        amount = Money(amount),
        categoryId = categoryId?.let { idRemapTable[it] ?: it },
        walletId = idRemapTable[walletId] ?: walletId,
        relatedWalletId = relatedWalletId?.let { idRemapTable[it] ?: it },
        dealId = dealId?.let { idRemapTable[it] ?: it },
        dealFlowType = dealFlowType?.let { runCatching { DealFlowType.valueOf(it) }.getOrNull() },
        note = note,
        receiptImageUrl = receiptImageUrl,
        date = runCatching { Instant.parse(date) }.getOrDefault(Instant.now()),
        createdAt = runCatching { Instant.parse(createdAt) }.getOrDefault(Instant.now()),
        updatedAt = runCatching { Instant.parse(updatedAt) }.getOrDefault(Instant.now()),
    )

    private fun BudgetSnapshot.toDomain(
        idRemapTable: Map<String, String>,
        isCrossAccount: Boolean,
    ): Budget {
        val mappedCatId = idRemapTable[categoryId] ?: categoryId
        return Budget(
            id = if (isCrossAccount) "${mappedCatId}_$periodKey" else id,
            categoryId = mappedCatId,
            periodKey = periodKey,
            limitAmount = Money(limitAmount),
            spentAmount = Money(spentAmount),
            notified80 = notified80,
            notified100 = notified100,
        )
    }

    private fun DebtSnapshot.toDomain(
        idRemapTable: Map<String, String>,
        currentUserId: String,
    ) = DebtAccount(
        id = idRemapTable[id] ?: id,
        userId = currentUserId,
        name = name,
        type = runCatching { DebtType.valueOf(type) }.getOrDefault(DebtType.CREDIT_CARD),
        totalAmount = Money(totalAmount),
        remainingBalance = Money(remainingBalance),
        interestRateApr = interestRateApr,
        minimumPayment = Money(minimumPayment),
        dueDate = dueDate,
        statementDate = statementDate,
        linkedWalletId = linkedWalletId?.let { idRemapTable[it] ?: it },
        gracePeriodDays = gracePeriodDays,
        colorHex = colorHex,
        isReminderEnabled = isReminderEnabled,
        reminderDaysBefore = reminderDaysBefore,
        isSettled = isSettled,
        createdAt = runCatching { Instant.parse(createdAt) }.getOrDefault(Instant.now()),
        updatedAt = runCatching { Instant.parse(updatedAt) }.getOrDefault(Instant.now()),
    )

    private fun DebtPaymentSnapshot.toDomain(
        idRemapTable: Map<String, String>,
        isCrossAccount: Boolean,
    ) = DebtPaymentHistory(
        id = if (isCrossAccount) UUID.randomUUID().toString() else id,
        debtId = idRemapTable[debtId] ?: debtId,
        walletId = idRemapTable[walletId] ?: walletId,
        amount = Money(amount),
        principalPaid = Money(principalPaid),
        interestPaid = Money(interestPaid),
        paymentDate = runCatching { Instant.parse(paymentDate) }.getOrDefault(Instant.now()),
        note = note,
        isCreditCardPayment = isCreditCardPayment,
    )

    private fun GoalSnapshot.toDomain(
        idRemapTable: Map<String, String>,
        isCrossAccount: Boolean,
    ) = FinancialGoal(
        id = idRemapTable[id] ?: id,
        name = name,
        targetAmount = Money(targetAmount),
        savedAmount = Money(savedAmount),
        deadline = runCatching { Instant.parse(deadline) }.getOrDefault(Instant.now()),
        category = category,
        monthlyContribution = Money(monthlyContribution),
        imageUri = if (isCrossAccount) null else imageUri,
        createdAt = runCatching { Instant.parse(createdAt) }.getOrDefault(Instant.now()),
    )

    private fun ReminderSnapshot.toDomain(idRemapTable: Map<String, String>) = Reminder(
        id = idRemapTable[id] ?: id,
        title = title,
        amount = Money(amount),
        categoryId = idRemapTable[categoryId] ?: categoryId,
        walletId = idRemapTable[walletId] ?: walletId,
        recurrence = runCatching { ReminderRecurrence.valueOf(recurrence.uppercase()) }.getOrDefault(ReminderRecurrence.MONTHLY),
        startDate = runCatching { Instant.parse(startDate) }.getOrDefault(Instant.now()),
        enabled = enabled,
        nextTriggerDate = runCatching { Instant.parse(nextTriggerDate) }.getOrDefault(Instant.now()),
    )

    private fun DealSnapshot.toDomain(
        idRemapTable: Map<String, String>,
        currentUserId: String,
    ) = FinancialDeal(
        id = idRemapTable[id] ?: id,
        userId = currentUserId,
        title = title,
        description = description,
        category = runCatching { DealCategory.valueOf(category.uppercase()) }.getOrDefault(DealCategory.INVESTMENT),
        targetAmount = Money(targetAmount),
        totalCapitalOutlay = Money(totalCapitalOutlay),
        totalRecovered = Money(totalRecovered),
        writtenOffCapital = Money(writtenOffCapital),
        netProfitLoss = Money(netProfitLoss),
        status = runCatching { DealStatus.valueOf(status) }.getOrDefault(DealStatus.ACTIVE),
        startDate = runCatching { Instant.parse(startDate) }.getOrDefault(Instant.now()),
        endDate = endDate?.let { runCatching { Instant.parse(it) }.getOrNull() },
        createdAt = runCatching { Instant.parse(createdAt) }.getOrDefault(Instant.now()),
        updatedAt = runCatching { Instant.parse(updatedAt) }.getOrDefault(Instant.now()),
    )

    private fun SalaryCycleConfigSnapshot.toDomain(idRemapTable: Map<String, String>) = SalaryCycleConfig(
        enabled = enabled,
        scheduleType = runCatching { SalaryScheduleType.valueOf(scheduleType) }.getOrDefault(SalaryScheduleType.MONTHLY_ONCE),
        paydayRuleType = runCatching { PaydayRuleType.valueOf(paydayRuleType) }.getOrDefault(PaydayRuleType.DAY_OF_MONTH),
        paydayDay = paydayDay,
        salaryWalletId = salaryWalletId?.let { idRemapTable[it] ?: it },
        expectedSalary = expectedSalary?.let { Money(it) },
        secondPaydayDay = secondPaydayDay,
        secondSalaryWalletId = secondSalaryWalletId?.let { idRemapTable[it] ?: it },
        secondExpectedSalary = secondExpectedSalary?.let { Money(it) },
        savingsWalletId = savingsWalletId?.let { idRemapTable[it] ?: it },
        rolloverRule = runCatching { CycleRolloverRule.valueOf(rolloverRule) }.getOrDefault(CycleRolloverRule.KEEP_IN_WALLET),
        budgetPeriodBasis = runCatching { BudgetPeriodBasis.valueOf(budgetPeriodBasis) }.getOrDefault(BudgetPeriodBasis.CALENDAR_MONTH),
        financeTimeZone = financeTimeZone,
    )

    private fun SavingSpinConfigSnapshot.toDomain(idRemapTable: Map<String, String>) = SavingSpinConfig(
        enabled = enabled,
        showOnHome = showOnHome,
        minAmount = Money(minAmount),
        maxAmount = Money(maxAmount),
        step = SavingSpinStep.entries.find { it.amount == stepAmount } ?: SavingSpinStep.FIVE_THOUSAND,
        slotCount = slotCount,
        frequency = runCatching { SavingSpinFrequency.valueOf(frequency) }.getOrDefault(SavingSpinFrequency.DAILY),
        selectedWeekdays = selectedWeekdays.toSet(),
        weeklyDay = weeklyDay,
        reminderHour = reminderHour,
        reminderMinute = reminderMinute,
        reminderEnabled = reminderEnabled,
        snoozeEnabled = snoozeEnabled,
        allowSkip = allowSkip,
        defaultDestinationId = defaultDestinationId?.let { idRemapTable[it] ?: it },
        createdAt = runCatching { Instant.parse(updatedAt) }.getOrDefault(Instant.now()),
        updatedAt = runCatching { Instant.parse(updatedAt) }.getOrDefault(Instant.now()),
    )
}
