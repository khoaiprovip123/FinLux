package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtPayoffPlan
import com.finlux.app.domain.model.DebtPaymentStep
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.PayoffStrategy
import java.time.YearMonth
import javax.inject.Inject
import kotlin.math.roundToLong

class CalculatePayoffStrategyUseCase @Inject constructor() {

    operator fun invoke(
        debts: List<DebtAccount>,
        strategy: PayoffStrategy = PayoffStrategy.SNOWBALL,
        extraMonthlyPayment: Long = 0L,
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
            )
        }

        // Tính tổng mức trả tối thiểu hàng tháng
        val totalMinPayment = activeDebts.sumOf { debt ->
            if (debt.minimumPayment.value > 0) debt.minimumPayment.value
            else (debt.remainingBalance.value * 0.03).roundToLong().coerceAtLeast(50_000L)
        }
        val monthlyBudget = (totalMinPayment + extraMonthlyPayment.coerceAtLeast(0L)).coerceAtLeast(10_000L)

        // Mô phỏng chiến lược hiện tại
        val strategySimulation = simulate(
            debts = activeDebts,
            strategy = strategy,
            monthlyBudget = monthlyBudget,
            startMonth = startMonth,
        )

        // Mô phỏng baseline (chỉ trả mức tối thiểu, không có tiền trả thêm)
        val baselineSimulation = simulate(
            debts = activeDebts,
            strategy = strategy,
            monthlyBudget = totalMinPayment,
            startMonth = startMonth,
        )

        val interestSaved = (baselineSimulation.totalInterest - strategySimulation.totalInterest).coerceAtLeast(0L)

        return DebtPayoffPlan(
            strategy = strategy,
            monthlyBudgetForDebt = Money(monthlyBudget),
            estimatedDebtFreeDate = strategySimulation.debtFreeDate,
            totalMonths = strategySimulation.totalMonths,
            totalInterestPayable = Money(strategySimulation.totalInterest),
            totalInterestSaved = Money(interestSaved),
            paymentSchedule = strategySimulation.steps,
        )
    }

    private data class SimulationResult(
        val totalMonths: Int,
        val totalInterest: Long,
        val debtFreeDate: YearMonth?,
        val steps: List<DebtPaymentStep>,
    )

    private fun simulate(
        debts: List<DebtAccount>,
        strategy: PayoffStrategy,
        monthlyBudget: Long,
        startMonth: YearMonth,
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
        val steps = mutableListOf<DebtPaymentStep>()
        val maxMonths = 360 // Giới hạn tối đa 30 năm chống lặp vô hạn

        while (simDebts.any { it.balance > 0 } && monthIndex < maxMonths) {
            monthIndex++
            currentMonth = currentMonth.plusMonths(1)

            var availableCash = monthlyBudget
            val activeInMonth = simDebts.filter { it.balance > 0 }

            // 1. Tính lãi phát sinh tháng này cho từng khoản nợ
            val monthlyInterestMap = mutableMapOf<String, Long>()
            for (debt in activeInMonth) {
                val interest = if (debt.apr > 0) {
                    ((debt.balance.toDouble() * (debt.apr / 100.0)) / 12.0).roundToLong()
                } else 0L
                monthlyInterestMap[debt.account.id] = interest
                debt.balance += interest
                totalInterestAccrued += interest
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

        val allSettled = simDebts.all { it.balance <= 0 }
        val debtFreeDate = if (allSettled) currentMonth else null

        return SimulationResult(
            totalMonths = monthIndex,
            totalInterest = totalInterestAccrued,
            debtFreeDate = debtFreeDate,
            steps = steps,
        )
    }
}
