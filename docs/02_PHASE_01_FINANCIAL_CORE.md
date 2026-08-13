# PHASE 01 — FINANCIAL CORE ENGINE (02_PHASE_01_FINANCIAL_CORE.md)

## 1. Objective (Mục tiêu)
Thiết kế và triển khai động cơ tài chính cốt lõi (Financial Core Engine), định nghĩa các Value Objects nguyên tử (`Money`, `Currency`, `Ledger`, `TransactionType`), bảo đảm tính bất biến (Financial Invariants) và các quy tắc giao dịch không bao giờ được vi phạm trong toàn bộ ứng dụng.

## 2. Business & Technical Goals
- **Business Goal**: Quản lý chính xác dòng tiền Thu, Chi, Chuyển khoản (Transfer); hỗ trợ số dư chính xác đến từng đơn vị tiền tệ mà không bị lỗi làm tròn floating-point.
- **Technical Goal**: Triển khai `Money` Value Object sử dụng `Long` (tính theo đơn vị nhỏ nhất hoặc VND nguyên integer); 100% phép tính số dư diễn ra qua Pure Functions.

## 3. Financial Engine Invariants (Quy tắc Tối thượng)
1. **BR-05**: Số tiền giao dịch (`amount`) luôn luôn lớn hơn 0.
2. **BR-07**: Giao dịch loại `TRANSFER_OUT` và `TRANSFER_IN` không được cộng dồn vào Tổng Thu nhập hay Tổng Chi tiêu trong Báo cáo.
3. **BR-14**: Mọi thao tác ghi giao dịch làm thay đổi số dư ví bắt buộc phải thực hiện trong 1 `Firestore Transaction` nguyên tử.
4. **Ledger Equality**: `Tổng số dư các ví = Tổng Thu nhập - Tổng Chi tiêu + Điều chỉnh khởi tạo`.

## 4. Domain Data Models & Value Objects
```kotlin
data class Money(
    val amount: Long, // Đơn vị VND (Long chống tràn số)
    val currency: String = "VND"
) {
    operator fun plus(other: Money): Money = Money(this.amount + other.amount, currency)
    operator fun minus(other: Money): Money = Money(this.amount - other.amount, currency)
}

enum class TransactionType {
    INCOME,       // Thu nhập
    EXPENSE,      // Chi tiêu
    TRANSFER_OUT, // Chuyển tiền đi (Ví nguồn)
    TRANSFER_IN   // Chuyển tiền đến (Ví đích)
}
```

## 5. Sequence Diagram — Atomic Transaction Flow
```text
Client (UseCase)              Firestore Remote Adapter             Firestore Database
      │                                   │                                 │
      │─── Execute AddTransaction ───────>│                                 │
      │                                   │─── runTransaction { ----------->│
      │                                   │      Read Wallet Current Balance│
      │                                   │      Calculate New Balance      │
      │                                   │      Write Transaction Doc      │
      │                                   │      Update Wallet Doc          │
      │                                   │   } --------------------------->│
      │<── Return AppResult.Success ──────│                                 │
```

## 6. Test Cases & Verification Suite
- `MoneyTest`: Kiểm tra cộng/trừ số tiền lớn không tràn số Long, không có lỗi rounding.
- `TransactionInvariantTest`: Kiểm tra giao dịch âm hoặc bằng 0 bị chặn ngay từ domain validation.
- `AtomicBalanceTransactionTest`: Mock Firestore Transaction đảm bảo cả wallet và transaction document cùng thành công hoặc cùng rollback.

## 7. Definition of Done (DoD) & Exit Criteria
- [x] Lớp Domain hoàn thiện `Money`, `TransactionType`, `Transaction`, `Wallet`.
- [x] Unit test cho Financial Engine đạt 100% code coverage.
- [x] Đã bọc `FirebaseFirestore.runTransaction` trong data layer.
