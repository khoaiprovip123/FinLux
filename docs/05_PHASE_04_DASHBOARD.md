# PHASE 04 — DASHBOARD & ANALYTICS MODULE (05_PHASE_04_DASHBOARD.md)

## 1. Objective (Mục tiêu)
Xây dựng màn hình Trang chủ (Dashboard) phân cấp số liệu tài chính rõ ràng, tích hợp màn hình Báo cáo chuyên sâu (Reports & Analytics) với các biểu đồ tương tác (Line Chart, Donut Chart, Treemap Chart) và tính năng Xuất báo cáo Excel / PDF.

## 2. Key Capabilities & Scope
- **Dashboard KPI**: Tổng tài sản, Thu nhập tháng, Chi tiêu tháng, Tiết kiệm ròng, Danh sách giao dịch gần đây.
- **Biểu đồ Analytics (UC-16)**:
  - **Line Chart**: Dòng tiền Thu - Chi theo thời gian (Tuần / Tháng / Quý / Năm).
  - **Donut Chart**: Tỷ trọng chi tiêu theo từng danh mục.
  - **Treemap Chart**: Phân bổ chi tiêu dạng ô tỷ trọng bất đối xứng.
- **Quản lý Ngân sách (UC-14/15)**: Đặt hạn mức tháng, thanh tiến độ % đã chi, cảnh báo khi chạm 80% / 100%.
- **Xuất dữ liệu (UC-17)**: Sinh file `.xlsx` (2 sheet) và file `.pdf` tóm tắt báo cáo.

## 3. Business Rules Matrix
- **BR-08**: Ngân sách áp dụng theo chu kỳ tháng lịch (`yyyy-MM`), không cộng dồn tháng trước.
- **BR-10**: Báo cáo tính toán dựa trên `transaction.date` (ngày giao dịch thực tế), không tính theo ngày khởi tạo record.
- **BR-11**: File Excel gồm 2 sheet (Chi tiết giao dịch + Tổng hợp danh mục); File PDF chứa biểu đồ báo cáo tóm tắt.

## 4. UI/UX & Responsive Panels
- Màn hình Dashboard tự thích ứng theo 3 phong cách thiết kế (`MODERN_DARK`, `GLASSMORPHISM`, `DYNAMIC_GRADIENT`).
- Panel compact thu gọn khoảng cách, padding cân đối trên mọi độ phân giải màn hình.

## 5. Exit Criteria & DoD
- [x] Biểu đồ hiển thị chính xác theo dữ liệu giao dịch thật từ Repository.
- [x] Tính năng đặt hạn mức ngân sách và xuất file Excel/PDF hoạt động thành công.
- [x] UI Dashboard & Báo cáo đáp ứng chuẩn Material 3 Fintech.
