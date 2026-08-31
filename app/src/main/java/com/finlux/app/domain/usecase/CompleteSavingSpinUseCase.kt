package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.SavingDestination
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.SavingSpinStatus
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.repository.SavingSpinRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class CompleteSavingSpinUseCase @Inject constructor(
    private val repository: SavingSpinRepository,
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
) {
    suspend operator fun invoke(
        session: SavingSpinSession,
        destination: SavingDestination,
    ): AppResult<Unit> {
        if (session.status !in setOf(SavingSpinStatus.SPUN_PENDING, SavingSpinStatus.SNOOZED) || session.selectedAmount == null) {
            return AppResult.Error("Hãy quay và chốt mệnh giá trước khi xác nhận")
        }
        if (!destination.enabled) return AppResult.Error("Nơi tiết kiệm đã bị tắt")

        val completeResult = repository.completeSession(session.scheduleKey, destination.id, destination.method)
        if (completeResult is AppResult.Error) return completeResult

        // Ghi nhận giao dịch vào DB & biến động số dư ví (Trừ tiền ví nguồn chuyển vào nơi tiết kiệm)
        val amount = session.selectedAmount
        val targetWalletId = destination.linkedWalletId
            ?: walletRepository.observeWallets().firstOrNull()?.firstOrNull()?.id

        if (targetWalletId != null) {
            val transaction = FinanceTransaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = amount,
                categoryId = "savings",
                walletId = targetWalletId,
                note = "Vòng quay tiết kiệm: Cất vào ${destination.name}",
                date = Instant.now(),
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )
            // Gọi addWithBalanceUpdate dùng Firestore Transaction (BR-14) để trừ tiền ví và ghi lịch sử
            transactionRepository.addWithBalanceUpdate(transaction)
        }

        return AppResult.Success(Unit)
    }
}
