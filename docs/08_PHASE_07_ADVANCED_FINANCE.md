# PHASE 07 — ADVANCED FINANCIAL ENGINE (08_PHASE_07_ADVANCED_FINANCE.md)

## 1. Objective (Mục tiêu)
Mở rộng động cơ tài chính FinLux sang quản lý toàn diện Tài sản (Assets), Nợ (Liabilities), Khoản vay (Loans), Thẻ tín dụng (Credit Cards), Đầu tư (Investments) và Tính toán Giá trị tài sản ròng (Net Worth Tracking).

## 2. Business & Technical Scope
- **Net Worth Calculation**: `Giá trị ròng = Tổng Tài sản (Ví + Đầu tư + Bất động sản) - Tổng Nợ (Vay + Hạn mức thẻ tín dụng đã dùng)`.
- **Credit Card Billing Cycle**: Theo dõi chu kỳ sao kê, ngày hạn thanh toán thẻ và tính toán dư nợ.
- **Loan Repayment Scheduler**: Lịch trả nợ định kỳ (Gốc + Lãi) và tự động tạo nhắc nhở thanh toán.

## 3. Data Architecture Extension
```text
users/{uid}/
  ├── assets/{assetId}          (name, type, value, valuationDate)
  ├── liabilities/{debtId}      (creditor, totalAmount, remainingAmount, dueDate, interestRate)
  └── investments/{portfolioId} (assetType, quantity, buyPrice, currentPrice)
```

## 4. Financial Invariants for Advanced Finance
- Tổng giá trị ròng (Net Worth) tự động cập nhật mỗi khi có biến động về giá trị ví, khoản nợ hoặc danh mục đầu tư.
- Thao tác thanh toán nợ / trả thẻ tín dụng tạo cặp giao dịch `TRANSFER` từ Ví thanh toán sang Khoản nợ/Thẻ.

## 5. Exit Criteria & DoD
- [ ] Schema Tài sản & Nợ được tích hợp vào Lớp Domain và Repositories.
- [ ] Màn hình Quản lý Tài sản ròng (Net Worth Dashboard) hiển thị chính xác tổng quan tài chính.
- [ ] Unit tests cho UseCases tính toán Net Worth & Lịch nợ đạt 100% PASS.
