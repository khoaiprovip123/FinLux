package com.finlux.app.domain.repository

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinancialDeal
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Repository quản lý các Thương vụ & Đầu tư sinh lời (Deal Tracking).
 */
interface DealRepository {
    /**
     * Lắng nghe danh sách toàn bộ các Deal của người dùng theo thời gian thực.
     */
    fun observeDeals(): Flow<List<FinancialDeal>>

    /**
     * Lắng nghe thông tin chi tiết của 1 Deal cụ thể theo ID.
     */
    fun observeDeal(dealId: String): Flow<FinancialDeal?>

    /**
     * Thêm mới hoặc cập nhật thông tin Deal.
     */
    suspend fun upsertDeal(deal: FinancialDeal): AppResult<String>

    /**
     * Xóa Deal và các liên kết.
     */
    suspend fun deleteDeal(dealId: String): AppResult<Unit>

    /**
     * Ghi nhận Khoản Chi Xuất Vốn (Capital Outlay) vào Deal:
     * - Trừ số dư ví nguồn (walletId).
     * - Tăng totalCapitalOutlay của Deal.
     * - Tạo giao dịch FinanceTransaction với dealFlowType = OUTLAY_CAPITAL.
     * @param deal Object FinancialDeal đầy đủ để build ghi chú mặc định theo category và tên thương vụ.
     */
    suspend fun recordDealOutlay(
        deal: FinancialDeal,
        walletId: String,
        amount: Long,
        date: Instant = Instant.now(),
        note: String = "",
    ): AppResult<Unit>

    /**
     * Ghi nhận Khoản Tiền Thu Hồi & Lợi Nhuận (Recovery & Capital Gain):
     * - Thực thi Atomic Firestore Transaction.
     * - Tự động phân rã dòng tiền:
     *   + Nếu amount <= Remaining Capital: 100% Hoàn gốc (PRINCIPAL_RECOVERY), cộng ví, tăng totalRecovered.
     *   + Nếu amount > Remaining Capital: Tách C_rem Hoàn gốc (PRINCIPAL_RECOVERY) và phần vượt (amount - C_rem) là Lợi nhuận ròng (CAPITAL_GAIN).
     * - Cập nhật ví và Deal tương ứng.
     * @param deal Object FinancialDeal đầy đủ để build ghi chú mặc định theo category và tên thương vụ.
     */
    suspend fun recordDealInflow(
        deal: FinancialDeal,
        walletId: String,
        amount: Long,
        date: Instant = Instant.now(),
        note: String = "",
    ): AppResult<Unit>

    /**
     * Chốt Lỗ & Đóng Deal (Stop-loss Settlement):
     * - Nếu Deal kết thúc mà totalRecovered < totalCapitalOutlay:
     * - Tự động tạo giao dịch CAPITAL_LOSS cho phần vốn còn thiếu.
     * - Cập nhật netProfitLoss âm và chuyển trạng thái status = COMPLETED.
     * @param deal Object FinancialDeal đầy đủ để build ghi chú mặc định theo category và tên thương vụ.
     */
    suspend fun closeDealWithLoss(
        deal: FinancialDeal,
        date: Instant = Instant.now(),
        note: String = "",
    ): AppResult<Unit>

    /**
     * Thu Hồi Chốt Lỗ & Mở Lại Deal:
     * - Xóa giao dịch CAPITAL_LOSS đã phát sinh.
     * - Hoàn lại netProfitLoss.
     * - Chuyển trạng thái Deal về ACTIVE và xóa endDate.
     */
    suspend fun revertDealLoss(dealId: String): AppResult<Unit>

    /**
     * Mở Lại Deal đã hoàn tất (Chuyển trạng thái Deal về ACTIVE).
     */
    suspend fun reopenDeal(dealId: String): AppResult<Unit>

    /**
     * Tất toán & Đóng Deal (Chuyển trạng thái Deal về COMPLETED).
     */
    suspend fun closeDeal(dealId: String, date: Instant = Instant.now()): AppResult<Unit>
}
