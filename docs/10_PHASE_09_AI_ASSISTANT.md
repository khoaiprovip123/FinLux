# PHASE 09 — AI FINANCIAL ASSISTANT & MCP (10_PHASE_09_AI_ASSISTANT.md)

## 1. Objective (Mục tiêu)
Tích hợp Trợ lý Trí tuệ nhân tạo (AI Financial Advisor) vào ứng dụng FinLux, cung cấp phân tích tài chính thông minh, gợi ý tối ưu hóa ngân sách, dự báo dòng tiền tương lai (Cash Flow Forecasting) và tương tác hội thoại qua giao thức MCP (Model Context Protocol) / Gemini Flash API.

## 2. Key Features & AI Capabilities
- **Financial Health Score**: Đánh giá sức khỏe tài chính dựa trên tỷ lệ Tiết kiệm / Thu nhập và mức độ tuân thủ Ngân sách.
- **Spending Insights & Anomalies**: Gợi ý các danh mục chi tiêu bất thường (ví dụ: "Chi tiêu ăn uống tuần này tăng 45% so với tuần trước").
- **Cash Flow Forecasting**: Dự báo số dư ví đến cuối tháng dựa trên lịch sử giao dịch.
- **AI Financial Chatbot**: Trả lời câu hỏi và thực hiện truy vấn tài chính ("Tôi đã chi bao nhiêu cho di chuyển tháng này?").

## 3. Privacy-First Architecture
- **Context Anonymization**: Trước khi gửi prompt đến AI API, toàn bộ thông tin nhạy cảm (Tên người dùng, Email, Số điện thoại) đều được loại bỏ hoặc mã hóa.
- **Local Prompting**: Sử dụng Gemini Nano / On-Device AI đối với các tác vụ phân tích cơ bản.

## 4. MCP Protocol Integration
```text
Client Application ──(MCP Tool Call)──> FinLux Server / Cloud Functions ──> Gemini 1.5 / 2.0 Flash
```

## 5. Exit Criteria & DoD
- [ ] AI Insights Card hiển thị khuyến nghị hữu ích trên Dashboard.
- [ ] Giao diện Trợ lý AI (Chatbot) phản hồi mượt mượt trong dưới 2 giây.
- [ ] Đạt chuẩn bảo mật dữ liệu riêng tư (No PII leaked to LLM providers).
