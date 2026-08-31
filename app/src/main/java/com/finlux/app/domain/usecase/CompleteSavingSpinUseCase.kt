package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.SavingDestination
import com.finlux.app.domain.model.SavingMethod
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
        walletId: String,
        walletName: String,
        sourceWalletId: String? = null,
    ): AppResult<Unit> {
        if (session.status !in setOf(SavingSpinStatus.SPUN_PENDING, SavingSpinStatus.SNOOZED) || session.selectedAmount == null) {
            return AppResult.Error("Hãy quay và chốt mệnh giá trước khi xác nhận")
        }

        val completeResult = repository.completeSession(session.scheduleKey, walletId, SavingMethod.BANK_TRANSFER)
        if (completeResult is AppResult.Error) return completeResult

        val amount = session.selectedAmount
        val now = Instant.now()

        // Nếu có ví nguồn và khác ví đích -> thực hiện chuyển tiền (transfer)
        if (!sourceWalletId.isNullOrBlank() && sourceWalletId != walletId) {
            val transferResult = transactionRepository.transferBetweenWallets(
                sourceWalletId = sourceWalletId,
                destinationWalletId = walletId,
                amount = amount.value,
                note = "Vòng quay tiết kiệm: Cất vào $walletName",
                date = now,
            )
            return when (transferResult) {
                is AppResult.Success -> AppResult.Success(Unit)
                is AppResult.Error -> transferResult
            }
        }

        // Trường hợp chỉ có 1 ví hoặc ví nguồn trùng ví đích: ghi nhận chi phí EXPENSE
        val transaction = FinanceTransaction(
            id = UUID.randomUUID().toString(),
            type = TransactionType.EXPENSE,
            amount = amount,
            categoryId = "savings",
            walletId = walletId,
            note = "Vòng quay tiết kiệm: Cất vào $walletName",
            date = now,
            createdAt = now,
            updatedAt = now,
        )
        return when (val txResult = transactionRepository.addWithBalanceUpdate(transaction)) {
            is AppResult.Success -> AppResult.Success(Unit)
            is AppResult.Error -> txResult
        }
    }

    suspend operator fun invoke(
        session: SavingSpinSession,
        destination: SavingDestination,
        sourceWalletId: String? = null,
    ): AppResult<Unit> {
        if (session.status !in setOf(SavingSpinStatus.SPUN_PENDING, SavingSpinStatus.SNOOZED) || session.selectedAmount == null) {
            return AppResult.Error("Hãy quay và chốt mệnh giá trước khi xác nhận")
        }
        if (!destination.enabled) return AppResult.Error("Nơi tiết kiệm đã bị tắt")

        val completeResult = repository.completeSession(session.scheduleKey, destination.id, destination.method)
        if (completeResult is AppResult.Error) return completeResult

        val amount = session.selectedAmount
        val targetWalletId = destination.linkedWalletId
            ?: walletRepository.observeWallets().firstOrNull()?.firstOrNull()?.id

        if (targetWalletId != null) {
            val now = Instant.now()
            if (!sourceWalletId.isNullOrBlank() && sourceWalletId != targetWalletId) {
                return transactionRepository.transferBetweenWallets(
                    sourceWalletId = sourceWalletId,
                    destinationWalletId = targetWalletId,
                    amount = amount.value,
                    note = "Vòng quay tiết kiệm: Cất vào ${destination.name}",
                    date = now,
                )
            }

            val transaction = FinanceTransaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = amount,
                categoryId = "savings",
                walletId = targetWalletId,
                note = "Vòng quay tiết kiệm: Cất vào ${destination.name}",
                date = now,
                createdAt = now,
                updatedAt = now,
            )
            transactionRepository.addWithBalanceUpdate(transaction)
        }

        return AppResult.Success(Unit)
    }
}
