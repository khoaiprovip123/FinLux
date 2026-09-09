package com.finlux.app.domain.model

/**
 * Đại diện cho cấu trúc Tài Sản Ròng Toàn Diện (True Net Worth).
 *
 * Thống nhất công thức:
 * - totalWalletAssets: Tổng số dư từ các ví tài sản (CASH, BANK, EWALLET, CARD...).
 * - activeDealCapitalOutlay: Tổng vốn lưu động đang nằm trong các deal ACTIVE (chờ thu hồi).
 * - totalDebtRemaining: Tổng dư nợ của các khoản vay/nợ chưa tất toán (!isSettled).
 * - trueNetWorth: Tài sản ròng thực tế = (totalWalletAssets + activeDealCapitalOutlay) - totalDebtRemaining.
 * - standardNetWorth: Tài sản ròng tiền mặt truyền thống = totalWalletAssets - totalDebtRemaining.
 */
data class TrueNetWorth(
    val totalWalletAssets: Money = Money(0L),
    val activeDealCapitalOutlay: Money = Money(0L),
    val totalDebtRemaining: Money = Money(0L),
    val trueNetWorth: Money = Money(0L),
    val standardNetWorth: Money = Money(0L),
) {
    companion object {
        val ZERO = TrueNetWorth()
    }
}
