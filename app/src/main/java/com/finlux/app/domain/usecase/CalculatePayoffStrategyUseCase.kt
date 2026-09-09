package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtPayoffPlan
import com.finlux.app.domain.model.DebtPaymentStep
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.PaydayAllocationItem
import com.finlux.app.domain.model.PaydayAllocationPlan
import com.finlux.app.domain.model.PayoffStrategy
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.StrategyComparison
import com.finlux.app.domain.model.Wallet
import java.time.YearMonth
import javax.inject.Inject
import kotlin.math.roundToLong

class CalculatePayoffStrategyUseCase @Inject constructor() {

    operator fun invoke(
        debts: List<DebtAccount>,
        strategy: PayoffStrategy = PayoffStrategy.SNOWBALL,
        extraMonthlyPayment: Long = 0L,
        salaryCycleConfig: SalaryCycleConfig? = null,
        wallets: List<Wallet> = emptyList(),
        startMonth: YearMonth = YearMonth.now(),
    ): DebtPayoffPlan {
        val activeDebts = debts.filter { !it.isSettled && it.remainingBalance.value > 0 }
        if (activeDebts.isEmpty()) {
            return DebtPayoffPlan(
                strategy = strategy,
                monthlyBudgetForDebt = Money(0L),
                estimatedDebtFreeDate = startMonth,
                totalMonths = 0,
                totalInterestPayable = Money(0L),
                totalInterestSaved = Money(0L),
                paymentSchedule = emptyList(),
                totalCycles = 0,
                timeSavedCycles = 0,
                isZeroAprOnly = true,
                paydayPlan = null,
            )
        }

        // 1. Tính tổng mức trả tối thiểu hàng tháng
        val totalMinPayment = activeDebts.sumOf { debt ->
            if (debt.minimumPayment.value > 0) debt.minimumPayment.value
            else (debt.remainingBalance.value * 0.03).roundToLong().coerceAtLeast(50_000L)
        }
        val monthlyBudget = (totalMinPayment + extraMonthlyPayment.coerceAtLeast(0L)).coerceAtLeast(10_000L)

        // 2. Mô phỏng chiến lược hiện tại
        val strategySimulation = simulate(
            debts = activeDebts,
            strategy = strategy,
            monthlyBudget = monthlyBudget,
            startMonth = startMonth,
            isBaseline = false,
        )

        // 3. Mô phỏng baseline (chỉ trả mức tối thiểu, không có tiền trả thêm)
        val baselineSimulation = simulate(
            debts = activeDebts,
            strategy = strategy,
            monthlyBudget = totalMinPayment,
            startMonth = startMonth,
            isBaseline = true,
        )

        // 4. Mô phỏng song song (Dual Simulation: Snowball vs Avalanche)
        val snowballSim = if (strategy == PayoffStrategy.SNOWBALL) strategySimulation else simulate(
            debts = activeDebts,
            strategy = PayoffStrategy.SNOWBALL,
            monthlyBudget = monthlyBudget,
            startMonth = startMonth,
            isBaseline = false,
        )
        val avalancheSim = if (strategy == PayoffStrategy.AVALANCHE) strategySimulation else simulate(
            debts = activeDebts,
            strategy = PayoffStrategy.AVALANCHE,
            monthlyBudget = monthlyBudget,
            startMonth = startMonth,
            isBaseline = false,
        )

        val hasAnyInterest = activeDebts.any { it.interestRateApr > 0.0 }
        val isZeroAprOnly = !hasAnyInterest
        val isBaselineTrap = baselineSimulation.isNegativeAmortizationTrap

        val totalActiveInitialDebt = activeDebts.sumOf { it.remainingBalance.value }
        // Kẹp trần số tiền lãi tiết kiệm tối đa 200% tổng nợ gốc ban đầu để triệt tiêu lỗi tràn số Long
        val maxReasonableInterestSaved = (totalActiveInitialDebt * 2L).coerceAtLeast(1_000_000L)

        val rawInterestSaved = if (hasAnyInterest && !isBaselineTrap) {
            (baselineSimulation.totalInterest - strategySimulation.totalInterest).coerceAtLeast(0L)
        } else {
            0L
        }
        val interestSaved = rawInterestSaved.coerceAtMost(maxReasonableInterestSaved)

        val timeSavedCycles = if (isBaselineTrap) {
            (120 - strategySimulation.totalMonths).coerceIn(0, 120)
        } else {
            (baselineSimulation.totalMonths - strategySimulation.totalMonths).coerceIn(0, 120)
        }

        // Chênh lệch giữa Snowball và Avalanche
        val rawInterestSavedWithAvalanche = (snowballSim.totalInterest - avalancheSim.totalInterest).coerceAtLeast(0L)
        val interestSavedWithAvalanche = rawInterestSavedWithAvalanche.coerceAtMost(maxReasonableInterestSaved)
        val snowballFirstSettled = snowballSim.firstDebtSettledMonth ?: snowballSim.totalMonths
        val avalancheFirstSettled = avalancheSim.firstDebtSettledMonth ?: avalancheSim.totalMonths
        val firstSettledDiffMonths = (avalancheFirstSettled - snowballFirstSettled).coerceAtLeast(0)

        val comparison = com.finlux.app.domain.model.StrategyComparison(
            interestSavedWithAvalanche = Money(interestSavedWithAvalanche),
            firstSettledMonthDifference = firstSettledDiffMonths,
            isZeroAprOnly = isZeroAprOnly,
            hasMeaningfulDifference = !isZeroAprOnly && (interestSavedWithAvalanche > 0L || firstSettledDiffMonths > 0),
        )

        // 5. Sinh Bảng Phân Bổ Trích Lương (Payday Allocation Matrix) & Cảnh báo lệch pha
        val paydayPlan = buildPaydayAllocationPlan(
            activeDebts = activeDebts,
            strategy = strategy,
            extraMonthlyPayment = extraMonthlyPayment,
            monthlyBudget = monthlyBudget,
            salaryCycleConfig = salaryCycleConfig,
            wallets = wallets,
        )

        return DebtPayoffPlan(
            strategy = strategy,
            monthlyBudgetForDebt = Money(monthlyBudget),
            estimatedDebtFreeDate = strategySimulation.debtFreeDate,
            totalMonths = strategySimulation.totalMonths,
            totalInterestPayable = Money(strategySimulation.totalInterest),
            totalInterestSaved = Money(interestSaved),
            paymentSchedule = strategySimulation.steps,
            totalCycles = strategySimulation.totalMonths,
            timeSavedCycles = timeSavedCycles,
            isZeroAprOnly = isZeroAprOnly,
            isBaselineTrap = isBaselineTrap,
            paydayPlan = paydayPlan,
            comparison = comparison,
        )
    }

    private fun buildPaydayAllocationPlan(
        activeDebts: List<DebtAccount>,
        strategy: PayoffStrategy,
        extraMonthlyPayment: Long,
        monthlyBudget: Long,
        salaryCycleConfig: SalaryCycleConfig?,
        wallets: List<Wallet>,
    ): PaydayAllocationPlan? {
        if (activeDebts.isEmpty()) return null

        val isSalaryEnabled = salaryCycleConfig?.enabled == true
        val paydayDay = if (isSalaryEnabled) salaryCycleConfig.paydayDay else 15
        val salaryWallet = salaryCycleConfig?.salaryWalletId?.let { wid -> wallets.find { it.id == wid } }
        val expectedSalary = salaryCycleConfig?.expectedSalary ?: Money(0L)

        // Sắp xếp ưu tiên theo chiến lược để xác định khoản nợ mục tiêu (Target Debt)
        val sortedDebts = when (strategy) {
            PayoffStrategy.SNOWBALL -> activeDebts.sortedBy { it.remainingBalance.value }
            PayoffStrategy.AVALANCHE -> activeDebts.sortedWith(
                compareByDescending<DebtAccount> { it.interestRateApr }.thenBy { it.remainingBalance.value }
            )
            PayoffStrategy.CUSTOM -> activeDebts
        }

        val targetDebtId = sortedDebts.firstOrNull()?.id
        val allocationItems = mutableListOf<PaydayAllocationItem>()
        val warnings = mutableListOf<String>()

        for (debt in sortedDebts) {
            val isTarget = debt.id == targetDebtId
            val debtMin = if (debt.minimumPayment.value > 0) debt.minimumPayment.value
            else (debt.remainingBalance.value * 0.03).roundToLong().coerceAtLeast(50_000L)

            val debtExtra = if (isTarget) extraMonthlyPayment.coerceAtLeast(0L) else 0L
            val totalForDebt = (debtMin + debtExtra).coerceAtMost(debt.remainingBalance.value)

            // Cảnh báo lệch pha: chỉ xét khi nợ định kỳ (isMonthlyRecurring) && dueDate != null && dueDate in 1..31 && dueDate < paydayDay
            val isMismatched = isSalaryEnabled && debt.isMonthlyRecurring && debt.dueDate != null && debt.dueDate in 1..31 && debt.dueDate < paydayDay
            if (isMismatched) {
                warnings.add("Khoản nợ [${debt.name}] đến hạn ngày ${debt.dueDate}, trước ngày nhận lương (ngày $paydayDay). Cần chủ động dự phòng ngân sách từ kỳ trước!")
            }

            allocationItems.add(
                PaydayAllocationItem(
                    debtId = debt.id,
                    debtName = debt.name,
                    debtType = debt.type,
                    isTarget = isTarget,
                    minimumPayment = Money(debtMin),
                    extraPayment = Money(debtExtra),
                    totalPaydayPayment = Money(totalForDebt),
                    dueDate = debt.dueDate,
                    isMismatchedWithPayday = isMismatched,
                    colorHex = debt.colorHex,
                )
            )
        }

        val remainingSalary = if (expectedSalary.value > 0) {
            (expectedSalary.value - monthlyBudget).coerceAtLeast(0L)
        } else {
            0L
        }

        return PaydayAllocationPlan(
            paydayDay = paydayDay,
            salaryWalletId = salaryCycleConfig?.salaryWalletId,
            salaryWalletName = salaryWallet?.name,
            expectedSalary = expectedSalary,
            totalDebtDeduction = Money(monthlyBudget),
            remainingIncomeAfterDebt = Money(remainingSalary),
            items = allocationItems,
            mismatchedWarnings = warnings,
        )
    }

    private data class SimulationResult(
        val totalMonths: Int,
        val totalInterest: Long,
        val debtFreeDate: YearMonth?,
        val steps: List<DebtPaymentStep>,
        val firstDebtSettledMonth: Int?,
        val isNegativeAmortizationTrap: Boolean = false,
    )

    private fun simulate(
        debts: List<DebtAccount>,
        strategy: PayoffStrategy,
        monthlyBudget: Long,
        startMonth: YearMonth,
        isBaseline: Boolean = false,
    ): SimulationResult {
        class SimDebt(
            val account: DebtAccount,
            var balance: Long,
            val apr: Double,
            var minPayment: Long,
        )

        val simDebts = debts.map { debt ->
            val minPay = if (debt.minimumPayment.value > 0) debt.minimumPayment.value
            else (debt.remainingBalance.value * 0.03).roundToLong().coerceAtLeast(50_000L)
            SimDebt(debt, debt.remainingBalance.value, debt.interestRateApr, minPay)
        }.toMutableList()

        var currentMonth = startMonth
        var monthIndex = 0
        var totalInterestAccrued = 0L
        var firstDebtSettledMonth: Int? = null
        val steps = mutableListOf<DebtPaymentStep>()
        val maxMonths = 120 // Kẹp trần tối đa 120 tháng (10 năm) chống lặp vô hạn và tràn số
        var isTrapDetected = false

        while (simDebts.any { it.balance > 0 } && monthIndex < maxMonths) {
            monthIndex++
            currentMonth = currentMonth.plusMonths(1)

            var availableCash = monthlyBudget
            val activeInMonth = simDebts.filter { it.balance > 0 }

            // 1. Kiểm tra tính khả thi & Tính lãi phát sinh tháng này cho từng khoản nợ
            val monthlyInterestMap = mutableMapOf<String, Long>()
            var hitNegativeAmortizationInMonth = false

            for (debt in activeInMonth) {
                val interest = if (debt.apr > 0) {
                    ((debt.balance.toDouble() * (debt.apr / 100.0)) / 12.0).roundToLong()
                } else 0L

                // Thuật toán bắt bẫy nợ âm (Negative Amortization):
                // Nếu chỉ trả tối thiểu (baseline) mà minPayment <= monthlyInterest: khoản nợ này sẽ không bao giờ trả hết
                if (isBaseline && debt.apr > 0 && debt.minPayment <= interest) {
                    hitNegativeAmortizationInMonth = true
                }

                // Chặn trần dư nợ không phình to quá 300% số dư nợ ban đầu để triệt tiêu bẫy lãi kép vô tận làm tràn số Long
                val maxAllowedBalance = (debt.account.remainingBalance.value.coerceAtLeast(500_000L) * 3L)
                val effectiveInterest = if (debt.balance >= maxAllowedBalance) {
                    0L // Đã chạm trần 300% nợ gốc ban đầu, ngừng tích lũy lãi kép vô hạn
                } else {
                    interest
                }

                monthlyInterestMap[debt.account.id] = effectiveInterest
                debt.balance = (debt.balance + effectiveInterest).coerceAtMost(maxAllowedBalance)
                totalInterestAccrued += effectiveInterest
            }

            if (isBaseline && hitNegativeAmortizationInMonth) {
                isTrapDetected = true
                // Dừng ngay vòng lặp mô phỏng baseline, TUYỆT ĐỐI KHÔNG để dư nợ tăng cấp số nhân gây tràn số
                break
            }

            // 2. Trả mức tối thiểu cho tất cả các khoản nợ đang active
            val paymentMap = mutableMapOf<String, Long>()
            val principalMap = mutableMapOf<String, Long>()

            for (debt in activeInMonth) {
                val interest = monthlyInterestMap[debt.account.id] ?: 0L
                val requiredPayment = debt.minPayment.coerceAtMost(debt.balance)
                val payment = requiredPayment.coerceAtMost(availableCash)
                
                availableCash = (availableCash - payment).coerceAtLeast(0L)
                paymentMap[debt.account.id] = payment
                debt.balance = (debt.balance - payment).coerceAtLeast(0L)

                val principal = (payment - interest).coerceAtLeast(0L)
                principalMap[debt.account.id] = principal
            }

            // 3. Nếu còn dư ngân sách, dồn tiền theo chiến lược (Snowball vs Avalanche)
            if (availableCash > 0) {
                val remainingWithBalance = simDebts.filter { it.balance > 0 }
                val sortedPriority = when (strategy) {
                    PayoffStrategy.SNOWBALL -> remainingWithBalance.sortedBy { it.balance }
                    PayoffStrategy.AVALANCHE -> remainingWithBalance.sortedWith(
                        compareByDescending<SimDebt> { it.apr }.thenBy { it.balance }
                    )
                    PayoffStrategy.CUSTOM -> remainingWithBalance
                }

                for (priorityDebt in sortedPriority) {
                    if (availableCash <= 0) break
                    val extraPayment = availableCash.coerceAtMost(priorityDebt.balance)
                    if (extraPayment > 0) {
                        priorityDebt.balance = (priorityDebt.balance - extraPayment).coerceAtLeast(0L)
                        availableCash = (availableCash - extraPayment).coerceAtLeast(0L)

                        val prevPayment = paymentMap[priorityDebt.account.id] ?: 0L
                        val prevPrincipal = principalMap[priorityDebt.account.id] ?: 0L
                        paymentMap[priorityDebt.account.id] = prevPayment + extraPayment
                        principalMap[priorityDebt.account.id] = prevPrincipal + extraPayment
                    }
                }
            }

            // Kiểm tra xem đã có khoản nợ nào được xóa sổ hoàn toàn chưa
            if (firstDebtSettledMonth == null && simDebts.any { it.balance <= 0L }) {
                firstDebtSettledMonth = monthIndex
            }

            // 4. Lưu lại bước thanh toán của tháng
            for (debt in activeInMonth) {
                val paid = paymentMap[debt.account.id] ?: 0L
                if (paid > 0) {
                    val interest = monthlyInterestMap[debt.account.id] ?: 0L
                    val principal = principalMap[debt.account.id] ?: (paid - interest).coerceAtLeast(0L)
                    steps.add(
                        DebtPaymentStep(
                            monthIndex = monthIndex,
                            targetMonth = currentMonth,
                            debtId = debt.account.id,
                            debtName = debt.account.name,
                            paymentAmount = Money(paid),
                            principalPaid = Money(principal),
                            interestPaid = Money(interest),
                            remainingBalanceAfter = Money(debt.balance),
                        )
                    )
                }
            }
        }

        val allSettled = !isTrapDetected && simDebts.all { it.balance <= 0 }
        val debtFreeDate = if (allSettled) currentMonth else null
        val effectiveMonths = if (isTrapDetected) maxMonths else monthIndex

        return SimulationResult(
            totalMonths = effectiveMonths,
            totalInterest = totalInterestAccrued,
            debtFreeDate = debtFreeDate,
            steps = steps,
            firstDebtSettledMonth = firstDebtSettledMonth,
            isNegativeAmortizationTrap = isTrapDetected,
        )
    }
}

