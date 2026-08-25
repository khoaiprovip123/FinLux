package com.finlux.app.domain.repository

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtPaymentHistory
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface DebtRepository {
    /** Lắng nghe danh sách toàn bộ các khoản nợ của người dùng theo thời gian thực. */
    fun observeDebts(): Flow<List<DebtAccount>>

    /** Lắng nghe lịch sử thanh toán của một khoản nợ cụ thể. */
    fun observePaymentHistory(debtId: String): Flow<List<DebtPaymentHistory>>

    /** Lắng nghe toàn bộ lịch sử thanh toán nợ của tất cả các khoản nợ. */
    fun observeAllPaymentHistory(): Flow<List<DebtPaymentHistory>>

    /** Thêm mới hoặc cập nhật thông tin khoản nợ. */
    suspend fun upsertDebt(debt: DebtAccount): AppResult<String>

    /** Xóa khoản nợ. */
    suspend fun deleteDebt(debt: DebtAccount): AppResult<Unit>

    /**
     * Thực hiện thanh toán nợ nguyên tử (Firestore Atomic Transaction):
     * 1. Trừ tiền ví nguồn ([walletId]).
     * 2. Giảm dư nợ khoản vay ([debtId]). Nếu dư nợ về 0 -> isSettled = true.
     * 3. Tạo bản ghi giao dịch chi tiêu trong sổ cái.
     * 4. Ghi log lịch sử trả nợ DebtPaymentHistory.
     */
    suspend fun processPayment(
        debtId: String,
        walletId: String,
        amount: Long,
        principalPaid: Long,
        interestPaid: Long,
        note: String = "",
        paymentDate: Instant = Instant.now(),
    ): AppResult<Unit>
}
