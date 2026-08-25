package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.CycleRolloverRule
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.TransactionRepository
import java.time.Instant
import javax.inject.Inject

sealed interface SalaryRolloverResult {
    data class Transferred(
        val cycleKey: String,
        val amount: Long,
        val fromWalletId: String,
        val toWalletId: String,
    ) : SalaryRolloverResult

    data class AlreadyProcessed(val cycleKey: String) : SalaryRolloverResult
    data class ZeroBalance(val cycleKey: String) : SalaryRolloverResult
    data class Skipped(val reason: String) : SalaryRolloverResult
}

class ExecuteSalaryRolloverUseCase @Inject constructor(
    private val salaryCycleRepository: SalaryCycleRepository,
    private val financialPeriodResolver: FinancialPeriodResolver,
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(
        config: SalaryCycleConfig,
        wallets: List<Wallet>,
        now: Instant = Instant.now(),
    ): AppResult<SalaryRolloverResult> {
        if (!config.enabled || config.rolloverRule != CycleRolloverRule.MOVE_TO_SAVINGS) {
            return AppResult.Success(SalaryRolloverResult.Skipped("Rollover rule is not MOVE_TO_SAVINGS or feature disabled"))
        }

        val salaryWalletId = config.salaryWalletId
            ?: return AppResult.Error("Chưa chọn ví nhận lương trong cấu hình")
        val savingsWalletId = config.savingsWalletId
            ?: return AppResult.Error("Chưa chọn ví tích lũy trong cấu hình")

        if (salaryWalletId == savingsWalletId) {
            return AppResult.Error("Ví nhận lương và ví tích lũy không được trùng nhau")
        }

        val previousPeriod = financialPeriodResolver.resolvePreviousPeriod(config, now)
        val cycleKey = previousPeriod.key

        if (salaryCycleRepository.isRolloverProcessed(cycleKey)) {
            return AppResult.Success(SalaryRolloverResult.AlreadyProcessed(cycleKey))
        }

        val salaryWallet = wallets.find { it.id == salaryWalletId }
            ?: return AppResult.Error("Không tìm thấy ví nhận lương")

        val balanceToMove = salaryWallet.balance.value
        if (balanceToMove <= 0L) {
            salaryCycleRepository.markRolloverProcessed(cycleKey)
            return AppResult.Success(SalaryRolloverResult.ZeroBalance(cycleKey))
        }

        val note = "Tích lũy kết chuyển chu kỳ lương ($cycleKey)"
        return when (
            val transferResult = transactionRepository.transferBetweenWallets(
                sourceWalletId = salaryWalletId,
                destinationWalletId = savingsWalletId,
                amount = balanceToMove,
                note = note,
                date = now,
            )
        ) {
            is AppResult.Success -> {
                salaryCycleRepository.markRolloverProcessed(cycleKey)
                AppResult.Success(
                    SalaryRolloverResult.Transferred(
                        cycleKey = cycleKey,
                        amount = balanceToMove,
                        fromWalletId = salaryWalletId,
                        toWalletId = savingsWalletId,
                    )
                )
            }
            is AppResult.Error -> {
                AppResult.Error("Không thể chuyển tiền sang ví tích lũy: ${transferResult.message}", transferResult.cause)
            }
        }
    }
}
