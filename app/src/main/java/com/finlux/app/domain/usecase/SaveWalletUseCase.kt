package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.repository.WalletRepository
import javax.inject.Inject

class SaveWalletUseCase @Inject constructor(private val repository: WalletRepository) {
    suspend operator fun invoke(wallet: Wallet): AppResult<String> =
        if (wallet.name.isBlank()) AppResult.Error("Vui lòng nhập tên ví") else repository.upsertWallet(wallet)
}
