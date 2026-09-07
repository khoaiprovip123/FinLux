# Phase Plan — Financial Data Integrity

## 1. Mục tiêu P0

Đảm bảo không có trường hợp UI báo thành công nhưng Firestore từ chối hoặc balance/ledger lệch nhau.

## 2. Phase FI-0 — Schema contract inventory

Tạo matrix cho Transaction, Wallet, Budget, Salary Cycle, Goal, Debt/Payment, Deal, Saving Spin. Mỗi field phải xác định:
- type;
- nullable;
- owner;
- mutable/immutable;
- server-owned/client-owned;
- default/migration.

## 3. Phase FI-1 — Budget contract P0

**Trạng thái: DONE local 2026-09-07 (PR-02).**

### Hiện trạng cần sửa
Repository dùng `periodKey/periodStart/periodEndExclusive/periodBasis`, trong khi Rules main chưa đồng bộ hoàn toàn.

### Tasks
- [x] Cập nhật Rules cho schema mới và deny unknown keys.
- [x] Migration compatibility cho legacy `month`/epoch-millis ở read path.
- [x] Thống nhất ghi mới bằng Firestore Timestamp.
- [x] Client không được tự thay `spentAmount/notified80/notified100`.
- [x] Functions reconcile aggregate khi transaction hoặc Budget contract/hạn mức thay đổi.
- [x] Test modern salary period, legacy calendar period, create/update/delete và spoof aggregate.

## 4. Phase FI-2 — Salary Period shared semantics

**Trạng thái: DONE local 2026-09-07 (PR-01).**

- [x] Sửa Functions đọc đúng path hiện hành.
- [x] Sửa field name theo Android contract.
- [x] Viết shared test vectors cho payday 1/5/25/31, February, leap year, LAST_DAY, FIRST_DAY, timezone.
- [x] Test Kotlin resolver và TypeScript resolver cùng expected output.

## 5. Phase FI-3 — Wallet mutation invariant

**Trạng thái: DONE local 2026-09-07 (PR-03).**

Mọi mutation phải thỏa:
```
wallet.before.balance + ledgerDelta = wallet.after.balance
```

### Tasks
- [x] Chuẩn hóa helper `walletLedgerUpdate(balance, transactionId)` cho wallet mutation.
- [x] Luôn set `lastTransactionId` khi balance đổi và xác thực transactionId không rỗng.
- [x] Firestore Rules `walletHasLedgerTransition` chỉ cho đổi balance khi transaction tương ứng tồn tại trong atomic batch.
- [x] Sửa transfer delete trỏ đúng transaction leg (`_out` / `_in`) cho từng ví.
- [x] Tích hợp helper cho Transaction, Goal, Debt và Deal đơn-ledger.
- [x] Emulator + unit tests xác minh 100% invariant.

## 6. Phase FI-4 — Goal/Debt/Deal

**Trạng thái: DONE local 2026-09-07 (Goal & Debt ở PR-04, Deal ở PR-05).**

### Goal
- [x] wallet mutation + goal mutation + ledger trong một Firestore Transaction.
- [x] Cấm xóa Goal khi còn số dư tích lũy (`savedAmount > 0`).

### Debt
- [x] wallet + debt + payment + ledger atomically; validate principal/interest.
- [x] Chặn thanh toán nợ vượt dư nợ còn lại (`principalPaid <= currentDebtRemaining`).
- [x] Cascade delete subcollection `payments` khi xóa Debt.

### Deal
- [x] Rules hỗ trợ `deals` (`validDeal`).
- [x] Transaction schema hỗ trợ `dealId/dealFlowType/counterpartTransactionId`.
- [x] Loại bỏ triệt để fake wallet `DEAL_SETTLEMENT` (P0-E, `CAPITAL_LOSS` là non-cash accounting ledger).
- [x] Hỗ trợ split deal inflow atomic với `counterpartTransactionId`.
- [x] Delete/cascade có contract rõ ràng hoàn tác ledger ví.

## 7. Phase FI-5 — Transfer double-entry

Đề xuất:
```
TRANSFER_OUT: walletId=A, relatedWalletId=B, correlationId=T123
TRANSFER_IN : walletId=B, relatedWalletId=A, correlationId=T123
```

Invariant:
```
delta(A) + delta(B) = 0
```

Test create/retry/delete/orphan detection.

## 8. Phase FI-6 — Reconciliation engine

Thêm debug/admin-only reconciliation:
- expected wallet balance from ledger;
- actual wallet balance;
- drift;
- orphan transfer;
- invalid deal/goal/debt reference.

Không tự sửa production data nếu chưa có audit trail.

## 9. Migration

1. Backup/export Firestore.
2. Dry-run migration.
3. Report legacy documents.
4. Migrate theo batch.
5. Verify counts + sums.
6. Deploy backward-compatible Rules/Functions.
7. Deploy Android.
8. Tighten contract sau adoption nếu cần.

## 10. Acceptance

- Không Firebase permission error với flow hợp lệ.
- Flow không hợp lệ bị deny.
- Ledger reconciliation drift = 0 trên test dataset.
- Retry không tạo double money movement.
