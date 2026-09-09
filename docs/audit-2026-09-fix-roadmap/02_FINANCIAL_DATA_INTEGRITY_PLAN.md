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

### Hiện trạng cần sửa
Repository dùng `periodKey/periodStart/periodEndExclusive/periodBasis`, trong khi Rules main chưa đồng bộ hoàn toàn.

### Tasks
- Cập nhật Rules cho schema mới.
- Migration compatibility cho legacy `month`.
- Thống nhất timestamp representation.
- Client không được tự thay `spentAmount` sau create.
- Functions reconcile spentAmount.
- Test salary-period budget create/update/delete.

## 4. Phase FI-2 — Salary Period shared semantics

- Sửa Functions đọc đúng path hiện hành.
- Sửa field name theo Android contract.
- Viết shared test vectors cho payday 1/5/25/31, February, leap year, LAST_DAY, FIRST_DAY, timezone.
- Test Kotlin resolver và TypeScript resolver cùng expected output.

## 5. Phase FI-3 — Wallet mutation invariant

Mọi mutation phải thỏa:
```
wallet.before.balance + ledgerDelta = wallet.after.balance
```

### Tasks
- Chuẩn hóa helper cho wallet mutation.
- Luôn set `lastTransactionId` khi balance đổi.
- Không update balance thủ công ngoài AdjustBalance use case có ledger adjustment.
- Add/Edit/Delete transaction cùng một invariant.

## 6. Phase FI-4 — Goal/Debt/Deal

### Goal
wallet mutation + goal mutation + ledger trong một Firestore Transaction.

### Debt
wallet + debt + payment + ledger atomically; validate principal/interest.

### Deal
- Rules hỗ trợ `deals`.
- Transaction schema hỗ trợ `dealId/dealFlowType`.
- Không dùng `DEAL_SETTLEMENT`.
- Delete/cascade có contract rõ.

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
