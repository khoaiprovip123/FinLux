package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.DealStatus
import com.finlux.app.domain.model.FinancialDeal
import com.finlux.app.domain.repository.DealRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

/**
 * UseCase lắng nghe danh sách toàn bộ các Deal của người dùng theo thời gian thực.
 */
class GetDealsUseCase @Inject constructor(
    private val dealRepository: DealRepository,
) {
    operator fun invoke(): Flow<List<FinancialDeal>> = dealRepository.observeDeals()
}

/**
 * UseCase lắng nghe chi tiết một Deal theo ID.
 */
class GetDealDetailUseCase @Inject constructor(
    private val dealRepository: DealRepository,
) {
    operator fun invoke(dealId: String): Flow<FinancialDeal?> = dealRepository.observeDeal(dealId)
}

/**
 * UseCase tạo mới hoặc cập nhật thông tin Thương vụ (Deal).
 */
class SaveDealUseCase @Inject constructor(
    private val dealRepository: DealRepository,
) {
    suspend operator fun invoke(deal: FinancialDeal): AppResult<String> {
        if (deal.title.isBlank()) {
            return AppResult.Error("Tiêu đề thương vụ không được để trống")
        }
        return dealRepository.upsertDeal(deal)
    }
}

/**
 * UseCase xóa thương vụ.
 */
class DeleteDealUseCase @Inject constructor(
    private val dealRepository: DealRepository,
) {
    suspend operator fun invoke(dealId: String): AppResult<Unit> {
        if (dealId.isBlank()) {
            return AppResult.Error("ID thương vụ không hợp lệ")
        }
        return dealRepository.deleteDeal(dealId)
    }
}

/**
 * UseCase ghi nhận khoản chi Xuất Vốn (Capital Outlay) vào Deal.
 */
class RecordDealOutlayUseCase @Inject constructor(
    private val dealRepository: DealRepository,
) {
    suspend operator fun invoke(
        deal: FinancialDeal,
        walletId: String,
        amount: Long,
        date: Instant = Instant.now(),
        note: String = "",
    ): AppResult<Unit> {
        if (deal.id.isBlank()) return AppResult.Error("Vui lòng chọn thương vụ")
        if (deal.status == DealStatus.COMPLETED) return AppResult.Error("Thương vụ đã hoàn tất đóng sổ, không thể xuất thêm vốn")
        if (walletId.isBlank()) return AppResult.Error("Vui lòng chọn ví xuất vốn")
        if (amount <= 0) return AppResult.Error("Số tiền xuất vốn phải lớn hơn 0")
        return dealRepository.recordDealOutlay(deal, walletId, amount, date, note)
    }
}

/**
 * UseCase ghi nhận khoản Thu Hồi Vốn & Lợi Nhuận (Atomic Decomposition).
 */
class RecordDealInflowUseCase @Inject constructor(
    private val dealRepository: DealRepository,
) {
    suspend operator fun invoke(
        deal: FinancialDeal,
        walletId: String,
        amount: Long,
        date: Instant = Instant.now(),
        note: String = "",
    ): AppResult<Unit> {
        if (deal.id.isBlank()) return AppResult.Error("Vui lòng chọn thương vụ")
        if (deal.status == DealStatus.COMPLETED) return AppResult.Error("Thương vụ đã hoàn tất đóng sổ, không thể thu hồi thêm")
        if (walletId.isBlank()) return AppResult.Error("Vui lòng chọn ví nhận tiền")
        if (amount <= 0) return AppResult.Error("Số tiền thu hồi phải lớn hơn 0")
        return dealRepository.recordDealInflow(deal, walletId, amount, date, note)
    }
}

/**
 * UseCase Chốt Lỗ & Đóng Thương Vụ (Stop-loss Settlement).
 */
class CloseDealWithLossUseCase @Inject constructor(
    private val dealRepository: DealRepository,
) {
    suspend operator fun invoke(
        deal: FinancialDeal,
        date: Instant = Instant.now(),
        note: String = "",
    ): AppResult<Unit> {
        if (deal.id.isBlank()) return AppResult.Error("ID thương vụ không hợp lệ")
        if (deal.status == DealStatus.COMPLETED) return AppResult.Error("Thương vụ đã hoàn tất đóng sổ")
        return dealRepository.closeDealWithLoss(deal, date, note)
    }
}

/**
 * UseCase Tất Toán & Đóng Thương Vụ (Close / Settle Deal).
 */
class CloseDealUseCase @Inject constructor(
    private val dealRepository: DealRepository,
) {
    suspend operator fun invoke(
        dealId: String,
        date: Instant = Instant.now(),
    ): AppResult<Unit> {
        if (dealId.isBlank()) return AppResult.Error("ID thương vụ không hợp lệ")
        return dealRepository.closeDeal(dealId, date)
    }
}

/**
 * UseCase Thu Hồi Chốt Lỗ & Mở Lại Thương Vụ (Revert Stop-loss & Reopen Deal).
 */
class RevertDealLossUseCase @Inject constructor(
    private val dealRepository: DealRepository,
) {
    suspend operator fun invoke(dealId: String): AppResult<Unit> {
        if (dealId.isBlank()) return AppResult.Error("ID thương vụ không hợp lệ")
        return dealRepository.revertDealLoss(dealId)
    }
}

/**
 * UseCase Mở Lại Deal đã hoàn tất (Chuyển trạng thái Deal về ACTIVE).
 */
class ReopenDealUseCase @Inject constructor(
    private val dealRepository: DealRepository,
) {
    suspend operator fun invoke(dealId: String): AppResult<Unit> {
        if (dealId.isBlank()) return AppResult.Error("ID thương vụ không hợp lệ")
        return dealRepository.reopenDeal(dealId)
    }
}
