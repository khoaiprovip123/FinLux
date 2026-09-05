package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.time.FinanceClock
import com.finlux.app.domain.model.SavingDestination
import com.finlux.app.domain.model.SavingMethod
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.SavingSpinStatus
import com.finlux.app.domain.repository.SavingSpinRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class CompleteSavingSpinUseCase @Inject constructor(
    private val repository: SavingSpinRepository,
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val clock: FinanceClock,
) {
    suspend operator fun invoke(
        session: SavingSpinSession,
        destination: SavingDestination,
        sourceWalletId: String? = null,
    ): AppResult<Unit> {
        // Idempotency: nếu session đã hoàn thành thì bỏ qua không tạo lại giao dịch
        if (session.status == SavingSpinStatus.COMPLETED) {
            return AppResult.Success(Unit)
        }

        if (session.status !in setOf(SavingSpinStatus.SPUN_PENDING, SavingSpinStatus.SNOOZED) || session.selectedAmount == null) {
            return AppResult.Error("Hãy quay và chốt mệnh giá trước khi xác nhận")
        }

        if (!destination.enabled) {
            return AppResult.Error("Nơi tiết kiệm đã bị tắt")
        }

        val amount = session.selectedAmount
        val now = clock.now()

        val operationId = "saving_spin_" + session.id
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .take(150)
        val outgoingTransactionId = "${operationId}_out"

        suspend fun transferToLinkedWallet(targetWalletId: String): AppResult<Unit> {
            if (sourceWalletId.isNullOrBlank()) {
                return AppResult.Error("Bạn chưa chọn ví nguồn")
            }
            if (sourceWalletId == targetWalletId) {
                return AppResult.Error("Ví nguồn và ví nhận không được trùng nhau")
            }

            val wallets = walletRepository.observeWallets().firstOrNull().orEmpty()
            val sourceWallet = wallets.firstOrNull { it.id == sourceWalletId }
                ?: return AppResult.Error("Ví nguồn không tồn tại")
            val targetWallet = wallets.firstOrNull { it.id == targetWalletId }
                ?: return AppResult.Error("Ví nhận không tồn tại")

            if (sourceWallet.type != com.finlux.app.domain.model.WalletType.CARD &&
                sourceWallet.balance.value < amount.value
            ) {
                return AppResult.Error("Ví nguồn không đủ số dư để cất ${formatVndAmount(amount.value)}")
            }

            return transactionRepository.transferBetweenWalletsIdempotent(
                sourceWalletId = sourceWalletId,
                destinationWalletId = targetWallet.id,
                amount = amount.value,
                note = "Vòng quay tiết kiệm: Cất vào ${destination.name}",
                date = now,
                operationId = operationId,
            )
        }

        return when (destination.method) {
            SavingMethod.CASH -> {
                // CASH without a linked wallet remains a manual/off-ledger piggy-bank confirmation.
                // If a cash savings wallet is linked, use the same durable ledger flow as bank transfer.
                val targetWalletId = destination.linkedWalletId
                if (targetWalletId.isNullOrBlank()) {
                    repository.completeSession(
                        scheduleKey = session.scheduleKey,
                        destinationId = destination.id,
                        method = SavingMethod.CASH,
                        transactionId = null,
                    )
                } else {
                    when (val transferResult = transferToLinkedWallet(targetWalletId)) {
                        is AppResult.Error -> transferResult
                        is AppResult.Success -> repository.completeSession(
                            scheduleKey = session.scheduleKey,
                            destinationId = destination.id,
                            method = SavingMethod.CASH,
                            transactionId = outgoingTransactionId,
                        )
                    }
                }
            }

            SavingMethod.BANK_TRANSFER -> {
                val targetWalletId = destination.linkedWalletId
                if (targetWalletId.isNullOrBlank()) {
                    AppResult.Error("Nơi tiết kiệm chưa liên kết ví nhận")
                } else {
                    when (val transferResult = transferToLinkedWallet(targetWalletId)) {
                        is AppResult.Error -> transferResult
                        is AppResult.Success -> repository.completeSession(
                            scheduleKey = session.scheduleKey,
                            destinationId = destination.id,
                            method = SavingMethod.BANK_TRANSFER,
                            transactionId = outgoingTransactionId,
                        )
                    }
                }
            }
        }
    }
}
