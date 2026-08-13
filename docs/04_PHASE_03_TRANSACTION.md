# PHASE 03 — WALLET & TRANSACTION MODULE (04_PHASE_03_TRANSACTION.md)

## 1. Objective (Mục tiêu)
Phát triển hoàn thiện module Quản lý Đa ví (Multi-Wallet), Quản lý Danh mục (Categories) và Vòng đời Giao dịch Thu/Chi/Chuyển tiền (Transaction CRUD & Internal Transfer Engine), đảm bảo tính đồng bộ thời gian thực và trải nghiệm offline mượt mà.

## 2. Scope & Features
- **UC-07/08/09/10**: Thêm, Sửa, Xóa và Tìm kiếm / Lọc danh sách giao dịch.
- **UC-11**: Quản lý danh mục (Khởi tạo seed data + tự tạo danh mục riêng).
- **UC-12/13**: Quản lý ví tài chính (Tiền mặt, Ngân hàng, Ví điện tử, Đầu tư) & Chuyển tiền nội bộ.

## 3. Data Architecture & Firestore Schema
```text
users/{uid}/
  ├── wallets/{walletId}          (name, type, balance, color, isDefault)
  ├── categories/{categoryId}    (name, type, icon, color, isDefault)
  └── transactions/{txnId}       (type, amount, categoryId, walletId, relatedWalletId, note, date)
```

## 4. Business Rules Application
- **BR-06**: Khi sửa hoặc xóa 1 giao dịch, hệ thống tự động tính toán hoàn bù số dư ví tương ứng (`wallet.balance`) qua Firestore Transaction.
- **BR-07**: Giao dịch `TRANSFER` có 2 bản ghi nội bộ (chuyển đi & chuyển đến), ảnh hưởng số dư ví nhưng bị loại trừ khỏi tổng Thu/Chi báo cáo.

## 5. Offline & Realtime Sync Protocol
- Bật `FirebaseFirestore.setPersistenceEnabled(true)`.
- Sử dụng `addSnapshotListener` trong Repositories để lắng nghe biến động dữ liệu tức thì cả ở bộ nhớ đệm (cache) và trên cloud server.

## 6. Definition of Done (DoD) & Exit Criteria
- [x] CRUD Giao dịch, Danh mục và Ví hoạt động mượt mà trên UI.
- [x] Chuyển tiền giữa 2 ví cập nhật đúng số dư cả 2 ví nguyên tử.
- [x] Unit test cho `AddTransactionUseCase`, `DeleteTransactionUseCase`, `TransferMoneyUseCase` đạt 100% PASS.
