package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.repository.TransactionRepository
import java.time.Instant
import javax.inject.Inject

class TransferMoneyUseCase @Inject constructor(private val repository: TransactionRepository) {
    suspend operator fun invoke(sourceId: String, destinationId: String, amount: Long, note: String): AppResult<Unit> {
        if (sourceId.isBlank() || destinationId.isBlank()) return AppResult.Error("Vui lòng chọn đủ hai ví")
        if (sourceId == destinationId) return AppResult.Error("Hai ví phải khác nhau")
        if (amount <= 0L) return AppResult.Error("Số tiền phải lớn hơn 0")
        return repository.transferBetweenWallets(sourceId, destinationId, amount, note.trim(), Instant.now())
    }
}
