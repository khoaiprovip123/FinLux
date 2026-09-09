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

        when (destination.method) {
            SavingMethod.CASH -> {
                // Tiết kiệm tiền mặt (heo đất / két sắt ngoại hệ thống): ghi nhận hoàn thành mà không trừ ví / không tạo transaction
                return repository.completeSession(
                    scheduleKey = session.scheduleKey,
                    destinationId = destination.id,
                    method = SavingMethod.CASH,
                    transactionId = null,
                )
            }

            SavingMethod.BANK_TRANSFER -> {
                val targetWalletId = destination.linkedWalletId
                if (targetWalletId.isNullOrBlank()) {
                    return AppResult.Error("Nơi tiết kiệm chưa liên kết ví nhận")
                }

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

                if (sourceWallet.balance.value < amount.value) {
                    return AppResult.Error("Ví nguồn không đủ số dư để cất ${formatVndAmount(amount.value)}")
                }

                val idempotencyTxId = "saving-spin:${session.id}"

                // Thực hiện chuyển khoản nội bộ nguyên tử giữa ví nguồn và ví đích
                val transferResult = transactionRepository.transferBetweenWallets(
                    sourceWalletId = sourceWalletId,
                    destinationWalletId = targetWalletId,
                    amount = amount.value,
                    note = "Vòng quay tiết kiệm: Cất vào ${destination.name}",
                    date = now,
                )

                if (transferResult is AppResult.Error) {
                    return transferResult
                }

                // Chỉ hoàn tất session khi giao dịch tài chính đã thành công
                return repository.completeSession(
                    scheduleKey = session.scheduleKey,
                    destinationId = destination.id,
                    method = SavingMethod.BANK_TRANSFER,
                    transactionId = idempotencyTxId,
                )
            }
        }
    }
}
