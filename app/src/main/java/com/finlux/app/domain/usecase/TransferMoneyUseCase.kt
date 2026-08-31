package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.WalletType
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject

class TransferMoneyUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val walletRepository: WalletRepository,
) {
    suspend operator fun invoke(
        sourceId: String,
        destinationId: String,
        amount: Long,
        note: String,
        date: Instant = Instant.now(),
    ): AppResult<Unit> {
        if (sourceId.isBlank() || destinationId.isBlank()) return AppResult.Error("Vui lòng chọn đủ hai ví")
        if (sourceId == destinationId) return AppResult.Error("Hai ví phải khác nhau")
        if (amount <= 0L) return AppResult.Error("Số tiền phải lớn hơn 0")

        val wallets = walletRepository.observeWallets().first()
        val sourceWallet = wallets.find { it.id == sourceId } ?: return AppResult.Error("Không tìm thấy ví nguồn")
        if (wallets.none { it.id == destinationId }) return AppResult.Error("Không tìm thấy ví đích")

        if (sourceWallet.type != WalletType.CARD && amount > sourceWallet.balance.value) {
            return AppResult.Error("Số dư ví nguồn không đủ để thực hiện chuyển tiền")
        }

        return repository.transferBetweenWallets(sourceId, destinationId, amount, note.trim(), date)
    }
}
