package com.finlux.app.domain.model

enum class NotificationType {
    REMINDER,            // Nhắc nhở thanh toán hóa đơn / khoản chi định kỳ
    BUDGET_ALERT,        // Cảnh báo chạm ngưỡng ngân sách (80%, 100%)
    GOAL_MILESTONE,      // Cột mốc mục tiêu tiết kiệm (25%, 50%, 75%, 100%)
    TRANSACTION_SUMMARY, // Tóm tắt biến động tài chính tuần/tháng
    SYSTEM               // Thông báo hệ thống, mẹo tài chính, cập nhật app
}
