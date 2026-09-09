package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.DealStatus
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.FinancialDeal
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TrueNetWorth
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.totalAssetBalance
import com.finlux.app.domain.repository.DealRepository
import com.finlux.app.domain.repository.DebtRepository
import com.finlux.app.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

import com.finlux.app.domain.model.WalletType

/**
 * UseCase tính toán Tài Sản Ròng Toàn Diện (True Net Worth Engine).
 *
 * Thống nhất công thức tính trên toàn bộ ứng dụng:
 * True Net Worth = (Tổng số dư các Ví) + (Tổng vốn Deal đang lưu động) - (Tổng nợ chưa tất toán).
 */
class GetTrueNetWorthUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
    private val debtRepository: DebtRepository,
    private val dealRepository: DealRepository,
) {
    operator fun invoke(): Flow<TrueNetWorth> {
        return combine(
            walletRepository.observeWallets(),
            debtRepository.observeDebts(),
            dealRepository.observeDeals(),
        ) { wallets, debts, deals ->
            calculate(wallets, debts, deals)
        }
    }

    fun calculate(
        wallets: List<Wallet>,
        debts: List<DebtAccount>,
        deals: List<FinancialDeal>,
    ): TrueNetWorth {
        val totalAssets = wallets.totalAssetBalance()

        // 1. Dư nợ từ các tài khoản nợ chưa tất toán
        val explicitDebt = debts.filterNot { it.isSettled }.sumOf { it.remainingBalance.value }

        // 2. Dư nợ từ các ví thẻ tín dụng (WalletType.CARD) âm tiền nhưng chưa liên kết với bất kỳ DebtAccount nào
        val linkedWalletIds = debts.mapNotNull { it.linkedWalletId }.toSet()
        val unlinkedCardDebt = wallets
            .filter { it.type == WalletType.CARD && it.id !in linkedWalletIds && it.balance.value < 0L }
            .sumOf { -it.balance.value }

        val totalDebtRemaining = explicitDebt + unlinkedCardDebt
        val activeCapitalOutlay = deals.filter { it.status == DealStatus.ACTIVE }.sumOf { it.remainingCapital.value }
        val trueNetWorth = totalAssets + activeCapitalOutlay - totalDebtRemaining
        val standardNetWorth = totalAssets - totalDebtRemaining

        return TrueNetWorth(
            totalWalletAssets = Money(totalAssets),
            activeDealCapitalOutlay = Money(activeCapitalOutlay),
            totalDebtRemaining = Money(totalDebtRemaining),
            trueNetWorth = Money(trueNetWorth),
            standardNetWorth = Money(standardNetWorth),
        )
    }
}

