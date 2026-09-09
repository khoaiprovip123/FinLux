package com.finlux.app.presentation.deal

import com.finlux.app.domain.model.DealStatus
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.FinancialDeal
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Wallet

enum class DealTab {
    ACTIVE,
    COMPLETED
}

data class DealsUiState(
    val deals: List<FinancialDeal> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val transactions: List<FinanceTransaction> = emptyList(),
    val allDealTransactions: List<FinanceTransaction> = emptyList(),
    val selectedTab: DealTab = DealTab.ACTIVE,
    val selectedDeal: FinancialDeal? = null,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    val activeDeals: List<FinancialDeal>
        get() = deals.filter { it.status == DealStatus.ACTIVE }

    val completedDeals: List<FinancialDeal>
        get() = deals.filter { it.status != DealStatus.ACTIVE }

    /** Tổng số vốn đang còn lưu động ngoài thị trường */
    val totalActiveRemainingCapital: Money
        get() = Money(activeDeals.sumOf { it.remainingCapital.value })

    /** Tổng vốn đã chi xuất cho các deal đang chạy */
    val totalActiveOutlay: Money
        get() = Money(activeDeals.sumOf { it.totalCapitalOutlay.value })

    /** Tổng lợi nhuận ròng tích lũy từ tất cả thương vụ */
    val totalAccumulatedProfit: Money
        get() = Money(deals.sumOf { it.netProfitLoss.value })

    /** Tỷ suất sinh lời bình quân (%) theo Lợi nhuận ròng thực nhận */
    val overallRoiPercentage: Double
        get() {
            val totalOutlay = deals.sumOf { it.totalCapitalOutlay.value }
            val totalProfitLoss = deals.sumOf { it.netProfitLoss.value }
            return if (totalOutlay > 0) {
                (totalProfitLoss.toDouble() / totalOutlay.toDouble()) * 100.0
            } else 0.0
        }
}
