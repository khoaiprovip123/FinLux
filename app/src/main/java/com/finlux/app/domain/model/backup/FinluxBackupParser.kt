package com.finlux.app.domain.model.backup

import org.json.JSONArray
import org.json.JSONObject

/**
 * Robust JSON parser to deserialize a raw `.finlux` snapshot JSON string
 * into [FinluxBackupSnapshot] for restoration.
 */
object FinluxBackupParser {

    fun parse(jsonString: String): FinluxBackupSnapshot {
        val root = JSONObject(jsonString)

        val schemaVersion = root.optInt("schemaVersion", 1)
        val appVersion = root.optString("appVersion", "")
        val appVersionCode = root.optInt("appVersionCode", 0)
        val exportedAt = root.optLong("exportedAt", 0L)
        val exportedByUid = root.optString("exportedByUid", "")
        val checksum = root.optString("checksum", "")
        val payloadSizeBytes = root.optLong("payloadSizeBytes", 0L)

        val wallets = root.optJSONArray("wallets")?.let { parseWallets(it) } ?: emptyList()
        val categories = root.optJSONArray("categories")?.let { parseCategories(it) } ?: emptyList()
        val transactions = root.optJSONArray("transactions")?.let { parseTransactions(it) } ?: emptyList()
        val budgets = root.optJSONArray("budgets")?.let { parseBudgets(it) } ?: emptyList()
        val debts = root.optJSONArray("debts")?.let { parseDebts(it) } ?: emptyList()
        val debtPayments = root.optJSONArray("debtPayments")?.let { parseDebtPayments(it) } ?: emptyList()
        val goals = root.optJSONArray("goals")?.let { parseGoals(it) } ?: emptyList()
        val reminders = root.optJSONArray("reminders")?.let { parseReminders(it) } ?: emptyList()
        val deals = root.optJSONArray("deals")?.let { parseDeals(it) } ?: emptyList()

        val salaryCycleConfig = root.optJSONObject("salaryCycleConfig")?.let { parseSalaryCycle(it) }
        val savingSpinConfig = root.optJSONObject("savingSpinConfig")?.let { parseSavingSpin(it) }

        return FinluxBackupSnapshot(
            schemaVersion = schemaVersion,
            appVersion = appVersion,
            appVersionCode = appVersionCode,
            exportedAt = exportedAt,
            exportedByUid = exportedByUid,
            checksum = checksum,
            payloadSizeBytes = payloadSizeBytes,
            wallets = wallets,
            categories = categories,
            transactions = transactions,
            budgets = budgets,
            debts = debts,
            debtPayments = debtPayments,
            goals = goals,
            reminders = reminders,
            deals = deals,
            salaryCycleConfig = salaryCycleConfig,
            savingSpinConfig = savingSpinConfig,
        )
    }

    private fun parseWallets(array: JSONArray): List<WalletSnapshot> {
        val list = mutableListOf<WalletSnapshot>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                WalletSnapshot(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    type = obj.getString("type"),
                    balance = obj.getLong("balance"),
                    colorHex = obj.getString("colorHex"),
                    isDefault = obj.optBoolean("isDefault", false),
                    createdAt = obj.optString("createdAt", ""),
                    status = obj.optString("status", "active"),
                )
            )
        }
        return list
    }

    private fun parseCategories(array: JSONArray): List<CategorySnapshot> {
        val list = mutableListOf<CategorySnapshot>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                CategorySnapshot(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    type = obj.getString("type"),
                    icon = obj.getString("icon"),
                    colorHex = obj.getString("colorHex"),
                    isDefault = obj.optBoolean("isDefault", false),
                    isEssential = obj.optBoolean("isEssential", true),
                    createdAt = obj.optString("createdAt", ""),
                )
            )
        }
        return list
    }

    private fun parseTransactions(array: JSONArray): List<TransactionSnapshot> {
        val list = mutableListOf<TransactionSnapshot>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                TransactionSnapshot(
                    id = obj.getString("id"),
                    type = obj.getString("type"),
                    amount = obj.getLong("amount"),
                    categoryId = if (obj.has("categoryId") && !obj.isNull("categoryId")) obj.getString("categoryId") else null,
                    walletId = obj.getString("walletId"),
                    relatedWalletId = if (obj.has("relatedWalletId") && !obj.isNull("relatedWalletId")) obj.getString("relatedWalletId") else null,
                    dealId = if (obj.has("dealId") && !obj.isNull("dealId")) obj.getString("dealId") else null,
                    dealFlowType = if (obj.has("dealFlowType") && !obj.isNull("dealFlowType")) obj.getString("dealFlowType") else null,
                    note = obj.optString("note", ""),
                    receiptImageUrl = if (obj.has("receiptImageUrl") && !obj.isNull("receiptImageUrl")) obj.getString("receiptImageUrl") else null,
                    date = obj.getString("date"),
                    createdAt = obj.optString("createdAt", ""),
                    updatedAt = obj.optString("updatedAt", ""),
                )
            )
        }
        return list
    }

    private fun parseBudgets(array: JSONArray): List<BudgetSnapshot> {
        val list = mutableListOf<BudgetSnapshot>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                BudgetSnapshot(
                    id = obj.getString("id"),
                    categoryId = obj.getString("categoryId"),
                    periodKey = obj.getString("periodKey"),
                    limitAmount = obj.getLong("limitAmount"),
                    spentAmount = obj.optLong("spentAmount", 0L),
                    notified80 = obj.optBoolean("notified80", false),
                    notified100 = obj.optBoolean("notified100", false),
                )
            )
        }
        return list
    }

    private fun parseDebts(array: JSONArray): List<DebtSnapshot> {
        val list = mutableListOf<DebtSnapshot>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                DebtSnapshot(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    type = obj.getString("type"),
                    totalAmount = obj.getLong("totalAmount"),
                    remainingBalance = obj.getLong("remainingBalance"),
                    interestRateApr = obj.optDouble("interestRateApr", 0.0),
                    minimumPayment = obj.optLong("minimumPayment", 0L),
                    dueDate = if (obj.has("dueDate") && !obj.isNull("dueDate")) obj.getInt("dueDate") else null,
                    statementDate = if (obj.has("statementDate") && !obj.isNull("statementDate")) obj.getInt("statementDate") else null,
                    linkedWalletId = if (obj.has("linkedWalletId") && !obj.isNull("linkedWalletId")) obj.getString("linkedWalletId") else null,
                    gracePeriodDays = obj.optInt("gracePeriodDays", 0),
                    colorHex = obj.optString("colorHex", ""),
                    isReminderEnabled = obj.optBoolean("isReminderEnabled", false),
                    reminderDaysBefore = obj.optInt("reminderDaysBefore", 0),
                    isSettled = obj.optBoolean("isSettled", false),
                    createdAt = obj.optString("createdAt", ""),
                    updatedAt = obj.optString("updatedAt", ""),
                )
            )
        }
        return list
    }

    private fun parseDebtPayments(array: JSONArray): List<DebtPaymentSnapshot> {
        val list = mutableListOf<DebtPaymentSnapshot>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                DebtPaymentSnapshot(
                    id = obj.getString("id"),
                    debtId = obj.getString("debtId"),
                    walletId = obj.getString("walletId"),
                    amount = obj.getLong("amount"),
                    principalPaid = obj.optLong("principalPaid", 0L),
                    interestPaid = obj.optLong("interestPaid", 0L),
                    paymentDate = obj.getString("paymentDate"),
                    note = obj.optString("note", ""),
                    isCreditCardPayment = obj.optBoolean("isCreditCardPayment", false),
                )
            )
        }
        return list
    }

    private fun parseGoals(array: JSONArray): List<GoalSnapshot> {
        val list = mutableListOf<GoalSnapshot>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                GoalSnapshot(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    targetAmount = obj.getLong("targetAmount"),
                    savedAmount = obj.optLong("savedAmount", 0L),
                    deadline = obj.getString("deadline"),
                    category = obj.optString("category", ""),
                    monthlyContribution = obj.optLong("monthlyContribution", 0L),
                    imageUri = if (obj.has("imageUri") && !obj.isNull("imageUri")) obj.getString("imageUri") else null,
                    createdAt = obj.optString("createdAt", ""),
                )
            )
        }
        return list
    }

    private fun parseReminders(array: JSONArray): List<ReminderSnapshot> {
        val list = mutableListOf<ReminderSnapshot>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                ReminderSnapshot(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    amount = obj.getLong("amount"),
                    categoryId = obj.getString("categoryId"),
                    walletId = obj.getString("walletId"),
                    recurrence = obj.getString("recurrence"),
                    startDate = obj.getString("startDate"),
                    enabled = obj.optBoolean("enabled", true),
                    nextTriggerDate = obj.getString("nextTriggerDate"),
                )
            )
        }
        return list
    }

    private fun parseDeals(array: JSONArray): List<DealSnapshot> {
        val list = mutableListOf<DealSnapshot>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                DealSnapshot(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    description = obj.optString("description", ""),
                    category = obj.getString("category"),
                    targetAmount = obj.optLong("targetAmount", 0L),
                    totalCapitalOutlay = obj.optLong("totalCapitalOutlay", 0L),
                    totalRecovered = obj.optLong("totalRecovered", 0L),
                    writtenOffCapital = obj.optLong("writtenOffCapital", 0L),
                    netProfitLoss = obj.optLong("netProfitLoss", 0L),
                    status = obj.optString("status", "ACTIVE"),
                    startDate = obj.getString("startDate"),
                    endDate = if (obj.has("endDate") && !obj.isNull("endDate")) obj.getString("endDate") else null,
                    createdAt = obj.optString("createdAt", ""),
                    updatedAt = obj.optString("updatedAt", ""),
                )
            )
        }
        return list
    }

    private fun parseSalaryCycle(obj: JSONObject): SalaryCycleConfigSnapshot {
        return SalaryCycleConfigSnapshot(
            enabled = obj.optBoolean("enabled", false),
            scheduleType = obj.optString("scheduleType", "MONTHLY_ONCE"),
            paydayRuleType = obj.optString("paydayRuleType", "DAY_OF_MONTH"),
            paydayDay = obj.optInt("paydayDay", 1),
            salaryWalletId = if (obj.has("salaryWalletId") && !obj.isNull("salaryWalletId")) obj.getString("salaryWalletId") else null,
            expectedSalary = if (obj.has("expectedSalary") && !obj.isNull("expectedSalary")) obj.getLong("expectedSalary") else null,
            secondPaydayDay = if (obj.has("secondPaydayDay") && !obj.isNull("secondPaydayDay")) obj.getInt("secondPaydayDay") else null,
            secondSalaryWalletId = if (obj.has("secondSalaryWalletId") && !obj.isNull("secondSalaryWalletId")) obj.getString("secondSalaryWalletId") else null,
            secondExpectedSalary = if (obj.has("secondExpectedSalary") && !obj.isNull("secondExpectedSalary")) obj.getLong("secondExpectedSalary") else null,
            savingsWalletId = if (obj.has("savingsWalletId") && !obj.isNull("savingsWalletId")) obj.getString("savingsWalletId") else null,
            rolloverRule = obj.optString("rolloverRule", "KEEP_IN_WALLET"),
            budgetPeriodBasis = obj.optString("budgetPeriodBasis", "CALENDAR_MONTH"),
            financeTimeZone = obj.optString("financeTimeZone", com.finlux.app.domain.model.FinanceBusinessConstants.Timezone.DEFAULT_ZONE_NAME),
            updatedAt = obj.optString("updatedAt", ""),
        )
    }

    private fun parseSavingSpin(obj: JSONObject): SavingSpinConfigSnapshot {
        val selectedDays = mutableListOf<Int>()
        val daysArray = obj.optJSONArray("selectedWeekdays")
        if (daysArray != null) {
            for (i in 0 until daysArray.length()) {
                selectedDays.add(daysArray.getInt(i))
            }
        }

        return SavingSpinConfigSnapshot(
            enabled = obj.optBoolean("enabled", false),
            showOnHome = obj.optBoolean("showOnHome", false),
            minAmount = obj.optLong("minAmount", 10_000L),
            maxAmount = obj.optLong("maxAmount", 100_000L),
            stepAmount = obj.optLong("stepAmount", 10_000L),
            slotCount = obj.optInt("slotCount", 8),
            frequency = obj.optString("frequency", "DAILY"),
            selectedWeekdays = selectedDays,
            weeklyDay = obj.optInt("weeklyDay", 1),
            reminderHour = obj.optInt("reminderHour", 20),
            reminderMinute = obj.optInt("reminderMinute", 0),
            reminderEnabled = obj.optBoolean("reminderEnabled", false),
            snoozeEnabled = obj.optBoolean("snoozeEnabled", false),
            allowSkip = obj.optBoolean("allowSkip", false),
            defaultDestinationId = if (obj.has("defaultDestinationId") && !obj.isNull("defaultDestinationId")) obj.getString("defaultDestinationId") else null,
            schemaVersion = obj.optInt("schemaVersion", 1),
            updatedAt = obj.optString("updatedAt", ""),
        )
    }
}
