package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtCashflowAnalysis
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.PayoffScenario
import com.finlux.app.domain.model.DEBT_INTEREST_CATEGORY_ID
import com.finlux.app.domain.model.DEBT_PAYMENT_CATEGORY_ID
import com.finlux.app.domain.model.DEBT_PRINCIPAL_CATEGORY_ID
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.SalaryScheduleType
import com.finlux.app.domain.model.SubCycleCashflow
import com.finlux.app.domain.model.TransactionType
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.roundToLong

class AnalyzeDebtCashflowUseCase @Inject constructor() {

    operator fun invoke(
        transactions: List<FinanceTransaction>,
        categories: List<Category>,
        debts: List<DebtAccount>,
        salaryCycleConfig: SalaryCycleConfig? = null,
        referenceMonth: YearMonth = YearMonth.now(),
        monthsToLookBack: Int = 3,
    ): DebtCashflowAnalysis {
        val categoryMap = categories.associateBy { it.id }
        val zone = ZoneId.systemDefault()

        // 1. Lọc giao dịch trong N tháng gần nhất tính từ referenceMonth
        val startMonth = referenceMonth.minusMonths((monthsToLookBack - 1).toLong())
        val inRangeTransactions = transactions.filter { tx ->
            val txMonth = YearMonth.from(tx.date.atZone(zone))
            !txMonth.isBefore(startMonth) && !txMonth.isAfter(referenceMonth)
        }

        // Nhóm theo từng tháng
        val byMonth = inRangeTransactions.groupBy { YearMonth.from(it.date.atZone(zone)) }
        val distinctMonthsCount = byMonth.keys.size.coerceAtLeast(1)

        // Tính tổng thu nhập và chi tiêu thiết yếu từ lịch sử giao dịch
        val totalIncome = inRangeTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount.value }

        val totalEssentialExpense = inRangeTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .filter { tx ->
                val cat = tx.categoryId?.let { categoryMap[it] }
                val isDebt = isDebtRelated(cat?.name.orEmpty(), tx.categoryId)
                if (isDebt) {
                    false // Loại trừ các khoản liên quan nợ để tránh trừ trùng nghĩa vụ nợ
                } else {
                    cat?.isEssential ?: isDefaultEssentialCategory(cat?.name.orEmpty())
                }
            }
            .sumOf { it.amount.value }

        val avgIncome = totalIncome / distinctMonthsCount
        val avgEssentialExpense = totalEssentialExpense / distinctMonthsCount

        // ƯU TIÊN THU NHẬP TỪ CHU KỲ LƯƠNG: Nếu người dùng bật chu kỳ lương và có mức lương dự kiến
        val hasValidSalary = salaryCycleConfig != null &&
            salaryCycleConfig.enabled &&
            salaryCycleConfig.totalExpectedSalary.value > 0L

        val effectiveIncome = if (hasValidSalary && salaryCycleConfig != null) {
            salaryCycleConfig.totalExpectedSalary.value
        } else {
            avgIncome
        }

        // Với chế độ SEMI_MONTHLY: Áp dụng quy tắc chia đôi (50/50) chi phí thiết yếu cho 2 đợt
        val isSemiMonthly = salaryCycleConfig != null &&
            salaryCycleConfig.enabled &&
            salaryCycleConfig.scheduleType == SalaryScheduleType.SEMI_MONTHLY

        val subCycleCashflows = if (isSemiMonthly && salaryCycleConfig != null) {
            val halfEssential = avgEssentialExpense / 2L
            val p1Expected = salaryCycleConfig.expectedSalary?.value ?: 0L
            val p2Expected = salaryCycleConfig.secondExpectedSalary?.value ?: 0L
            listOf(
                SubCycleCashflow(
                    paydayDay = salaryCycleConfig.paydayDay,
                    expectedSalary = Money(p1Expected),
                    allocatedEssentialExpense = Money(halfEssential),
                    availableCashflow = Money((p1Expected - halfEssential).coerceAtLeast(0L)),
                    label = "Đợt ${salaryCycleConfig.paydayDay}",
                ),
                SubCycleCashflow(
                    paydayDay = salaryCycleConfig.secondPaydayDay ?: salaryCycleConfig.paydayDay,
                    expectedSalary = Money(p2Expected),
                    allocatedEssentialExpense = Money(avgEssentialExpense - halfEssential),
                    availableCashflow = Money((p2Expected - (avgEssentialExpense - halfEssential)).coerceAtLeast(0L)),
                    label = "Đợt ${salaryCycleConfig.secondPaydayDay ?: salaryCycleConfig.paydayDay}",
                ),
            )
        } else {
            emptyList()
        }

        // 2. Tính tổng nghĩa vụ trả nợ tối thiểu & Dư nợ
        val activeDebts = debts.filter { !it.isSettled && it.remainingBalance.value > 0L }
        val totalMinDebt = activeDebts.sumOf { debt ->
            if (debt.minimumPayment.value > 0) debt.minimumPayment.value
            else (debt.remainingBalance.value * 0.03).roundToLong().coerceAtLeast(50_000L)
        }

        // 3. Tính Dòng tiền tự do chuẩn (Free Cash Flow = Thu nhập cơ sở - Chi thiết yếu - Nợ tối thiểu)
        val rawFcf = effectiveIncome - avgEssentialExpense - totalMinDebt
        val isDeficit = rawFcf <= 0L
        val freeCashFlowValue = rawFcf.coerceAtLeast(0L)

        // 4. Tính Lãi suất trung bình có trọng số (Weighted APR)
        val totalRemainingDebt = activeDebts.sumOf { it.remainingBalance.value }
        val weightedApr = if (totalRemainingDebt > 0L) {
            val totalWeightedAprSum = activeDebts.sumOf { debt ->
                debt.remainingBalance.value.toDouble() * debt.interestRateApr
            }
            totalWeightedAprSum / totalRemainingDebt.toDouble()
        } else {
            0.0
        }

        // 5. Sinh 3 kịch bản phân bổ dòng tiền
        val scenarios = if (!isDeficit && freeCashFlowValue > 0L) {
            listOf(
                PayoffScenario(
                    name = "Thư thái",
                    description = "Dành 30% FCF trả thêm, giữ 70% dự phòng khẩn cấp",
                    percentageOfFcf = 0.30,
                    extraMonthlyAmount = Money(roundToCleanAmount(freeCashFlowValue * 0.30)),
                    isRecommended = false,
                ),
                PayoffScenario(
                    name = "Cân bằng",
                    description = "Dành 60% FCF trả thêm, cân đối tối ưu giữa tốc độ và sinh hoạt",
                    percentageOfFcf = 0.60,
                    extraMonthlyAmount = Money(roundToCleanAmount(freeCashFlowValue * 0.60)),
                    isRecommended = true,
                ),
                PayoffScenario(
                    name = "Thần tốc",
                    description = "Dồn 85% FCF trả nợ nhanh nhất, triệt tiêu tiền lãi ngân hàng",
                    percentageOfFcf = 0.85,
                    extraMonthlyAmount = Money(roundToCleanAmount(freeCashFlowValue * 0.85)),
                    isRecommended = false,
                ),
            )
        } else {
            emptyList()
        }

        return DebtCashflowAnalysis(
            averageMonthlyIncome = Money(effectiveIncome),
            averageEssentialExpense = Money(avgEssentialExpense),
            totalMonthlyMinimumDebt = Money(totalMinDebt),
            freeCashFlow = Money(rawFcf),
            isDeficit = isDeficit,
            weightedApr = (weightedApr * 10.0).roundToLong() / 10.0, // 1 decimal place
            scenarios = scenarios,
            isSalaryCycleBased = hasValidSalary,
            baseIncomeSource = if (hasValidSalary) "Lương dự kiến (Chu kỳ lương)" else "Thu nhập TB 3 tháng",
            isSemiMonthly = isSemiMonthly,
            subCycleCashflows = subCycleCashflows,
        )
    }

    private fun isDefaultEssentialCategory(categoryName: String): Boolean {
        val normalized = categoryName.lowercase().trim()
        val essentialKeywords = listOf(
            "ăn uống", "food", "nhà ở", "tiền nhà", "home", "rent",
            "điện nước", "hóa đơn", "utility", "bill",
            "di chuyển", "xăng", "transport", "xe",
            "y tế", "sức khỏe", "thuốc", "medical", "health",
            "giáo dục", "học phí", "education",
        )
        return essentialKeywords.any { normalized.contains(it) }
    }

    private fun isDebtRelated(categoryName: String, categoryId: String?): Boolean {
        if (categoryId == DEBT_PAYMENT_CATEGORY_ID ||
            categoryId == DEBT_PRINCIPAL_CATEGORY_ID ||
            categoryId == DEBT_INTEREST_CATEGORY_ID
        ) {
            return true
        }
        val normalized = categoryName.lowercase().trim()
        val debtKeywords = listOf("nợ", "debt", "vay", "loan", "trả góp", "tín dụng", "credit")
        return debtKeywords.any { normalized.contains(it) }
    }

    private fun roundToCleanAmount(amount: Double): Long {
        if (amount <= 0) return 0L
        val raw = amount.roundToLong()
        return when {
            raw >= 1_000_000L -> ((raw + 50_000L) / 100_000L) * 100_000L
            raw >= 100_000L -> ((raw + 25_000L) / 50_000L) * 50_000L
            else -> ((raw + 5_000L) / 10_000L) * 10_000L
        }.coerceAtLeast(50_000L)
    }
}

