# PHASE 08 — OCR RECEIPT & TAX EXTRACTION (09_PHASE_08_OCR_TAX.md)

## 1. Objective (Mục tiêu)
Phát triển tính năng nhận diện tự động thông tin hóa đơn (Receipt Scanning & Intelligent Extraction) bằng công nghệ OCR (Google ML Kit Text Recognition / Vision AI), hỗ trợ người dùng nhập nhanh giao dịch từ ảnh chụp và phân loại danh mục chi tiêu / thuế.

## 2. Technical Pipeline & Data Flow
```text
  ┌─────────────────┐       ┌────────────────────┐       ┌─────────────────────┐
  │ Camera / Photo  │ ────> │ ML Kit Text Recogn │ ────> │ Intelligent Parser  │
  │ Capture Receipt │       │ OCR Extraction     │       │ Regex & Pattern Match│
  └─────────────────┘       └────────────────────┘       └──────────┬──────────┘
                                                                    │
                                                                    ▼
  ┌─────────────────┐       ┌────────────────────┐       ┌─────────────────────┐
  │ Created Transaction     │ Fill Form Auto-Fill│ <──── │ Extracted Fields:   │
  │ Document & Upload Image │ Review & Confirm   │       │ Amount, Date, Vendor│
  └─────────────────┘       └────────────────────┘       └─────────────────────┘
```

## 3. Extraction Capabilities
- **Tên cửa hàng / Nhà cung cấp (Vendor Name)**.
- **Tổng số tiền (Total Amount)**: Nhận diện chính xác định dạng VND/USD.
- **Ngày hóa đơn (Transaction Date)**.
- **Ảnh hóa đơn**: Tự động nén và tải lên Firebase Storage (`receipts/{uid}/{transactionId}.jpg`).

## 4. Business Rules & Privacy
- OCR xử lý hoàn toàn trên thiết bị (On-Device ML Kit) đảm bảo quyền riêng tư người dùng.
- Form thêm giao dịch hiển thị giao diện xem lại (Review & Confirm) cho phép người dùng điều chỉnh thông tin trước khi lưu chính thức.

## 5. Exit Criteria & DoD
- [ ] Tính năng chụp / chọn ảnh hóa đơn hoạt động mượt mà.
- [ ] OCR nhận diện chính xác >85% đối với hóa đơn phổ biến tại Việt Nam.
- [ ] Ảnh hóa đơn được liên kết chính xác với document giao dịch trong Firestore.
