package com.finlux.app.domain.model

import java.time.Instant

/**
 * Trạng thái của một Thương Vụ / Deal đầu tư.
 */
enum class DealStatus {
    ACTIVE,      // Đang hoạt động (đang chi xuất vốn hoặc đang thu hồi)
    COMPLETED,   // Đã hoàn tất (đã thu hồi xong, chốt lời hoặc chốt lỗ đóng deal)
    CANCELLED    // Đã hủy bỏ
}

/**
 * Phân loại Thương Vụ / Khoản Theo Dõi:
 * - INVESTMENT: Dự án đầu tư, kinh doanh, lướt sóng (tính ROI %, Lợi nhuận ròng, chốt lời/lỗ).
 * - LENDING: Cho vay, cho mượn tiền (tập trung thu hồi nợ gốc, tiền lãi, dư nợ còn lại).
 */
enum class DealCategory {
    INVESTMENT, // Đầu tư sinh lời
    LENDING     // Cho vay / Cho mượn
}

/**
 * Phân loại dòng tiền trong khuôn khổ Deal để cô lập và xử lý kế toán chính xác.
 */
enum class DealFlowType {
    OUTLAY_CAPITAL,      // Tiền xuất vốn ban đầu (Trừ ví, Tăng vốn Deal, KHÔNG tính vào Chi tiêu sinh hoạt)
    PRINCIPAL_RECOVERY,  // Tiền thu hồi vốn gốc (Cộng ví, Tăng thu hồi Deal, KHÔNG tính vào Thu nhập)
    CAPITAL_GAIN,        // Tiền Lợi nhuận ròng / Tiền Lời vượt vốn (Cộng ví, TÍNH VÀO Thu nhập thực tế)
    CAPITAL_LOSS         // Tiền Lỗ khi chốt đóng deal (TÍNH VÀO Chi tiêu thực tế)
}

/**
 * Đại diện cho một Thương vụ / Dự án đầu tư sinh lời ngắn hạn hoặc Khoản cho vay (Deal Tracking).
 */
data class FinancialDeal(
    val id: String = "",
    val userId: String = "",
    val title: String,
    val description: String = "",
    val category: DealCategory = DealCategory.INVESTMENT,
    val targetAmount: Money = Money(0),
    val totalCapitalOutlay: Money = Money(0),
    val totalRecovered: Money = Money(0),
    val writtenOffCapital: Money = Money(0),
    val netProfitLoss: Money = Money(0),
    val status: DealStatus = DealStatus.ACTIVE,
    val startDate: Instant = Instant.now(),
    val endDate: Instant? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
) {
    /** Vốn gốc còn lại chưa thu hồi (sau khi trừ vốn đã thu hồi và vốn đã chốt lỗ / xóa sổ) */
    val remainingCapital: Money
        get() = Money((totalCapitalOutlay.value - totalRecovered.value - writtenOffCapital.value).coerceAtLeast(0L))

    /** Tỷ suất sinh lời ROI (%) theo Lợi nhuận ròng thực nhận */
    val roiPercentage: Double
        get() = if (totalCapitalOutlay.value > 0) {
            (netProfitLoss.value.toDouble() / totalCapitalOutlay.value.toDouble()) * 100.0
        } else 0.0

    /** Tiến độ thu hồi vốn gốc (0.0 .. 1.0) */
    val recoveryProgress: Float
        get() = if (totalCapitalOutlay.value > 0) {
            (totalRecovered.value.toFloat() / totalCapitalOutlay.value.toFloat()).coerceIn(0f, 1f)
        } else 0f

    /** Kiểm tra xem Deal đã thu hồi xong 100% vốn chưa */
    val isFullyRecovered: Boolean
        get() = totalCapitalOutlay.value > 0 && totalRecovered.value >= totalCapitalOutlay.value
}
