# Phase Plan — Firebase Backend & Security

## 1. Mục tiêu

Đưa Firestore/Storage/Functions/Android components về mô hình least privilege, deny-by-default, idempotent và kiểm thử được bằng emulator.

## 2. Phase FB-1 — Firestore Rules

**Tiến độ một phần 2026-09-07:**
- Budget period contract và aggregate ownership hoàn tất (PR-02).
- Wallet balance ledger transition và atomic delta hoàn tất (PR-03).
- Goal & Debt mutation invariant và rules validation hoàn tất (PR-04).
- Deal rules & contract tiếp tục ở PR-05.

- [x] Bổ sung schema đầy đủ cho Budget period mới + legacy transition (PR-02).
- [x] Bổ sung match/rules cho Deals (PR-05).
- [x] Cho transaction fields cần thiết như `dealId/dealFlowType/counterpartTransactionId` theo contract (PR-05).
- [x] Budget server-owned fields không cho client sửa trực tiếp (PR-02).
- [x] Wallet balance chỉ đổi cùng transaction hợp lệ (PR-03).
- [x] Goal & Debt invariants: cấm xóa Goal khi còn số dư, khóa debt payment principal/interest, cascade delete debt payments (PR-04).
- Immutable fields phải được khóa.
- Validate enum casing thống nhất.
- Deny unknown keys ở entity tài chính trọng yếu.


### Rules tests
owner access, cross-user deny, valid create, invalid field deny, invalid wallet delta deny, valid atomic mutation pass, spoofed aggregate deny.

## 3. Phase FB-2 — Cloud Functions

- [x] Sửa salary config path/field (PR-01, 2026-09-07).
- [x] Tách `financialPeriod.ts` và khóa parity bằng fixture Android–Functions (PR-01, 2026-09-07).
- [x] Thêm `budgetContract.ts`, `onBudgetWrite` và reconcile Budget theo exact period/document (PR-02, 2026-09-07).
- Tách `transactionSemantics.ts`.
- Scheduled/event functions idempotent.
- [x] Budget notification 80/100 dùng ID idempotent và cờ server-owned.
- Retry-safe event processing.
- Tránh scan toàn bộ users khi có thể query due entities.

## 4. Phase FB-3 — Storage Rules

**Trạng thái: DONE local 2026-09-07 (PR-06).**

### Avatar
- [x] read: owner only (`request.auth.uid == uid`);
- [x] create/update: owner + image + size limit <= 5MB;
- [x] delete: owner.
- [x] hỗ trợ `.jpg`, `.png`, `.webp`.

### Receipt
- [x] read: owner only;
- [x] create/update: owner + image MIME + size limit <= 5MB;
- [x] delete: owner.

- [x] Không dùng `request.resource` cho read/delete condition (tách biệt read, delete khỏi create, update).

## 5. Phase FB-4 — Android Manifest hardening

**Trạng thái: DONE local 2026-09-07 (PR-06).**

### SalaryCycleReceiver
- [x] `android:exported=false`.
- [x] force flag chỉ cho phép khi `BuildConfig.DEBUG == true`.
- [x] External intent không thể kích hoạt receiver.

### Permissions
- [x] Rà soát và loại bỏ `REQUEST_INSTALL_PACKAGES`.
- [x] Chỉ giữ các quyền cần thiết: `INTERNET`, `POST_NOTIFICATIONS`, `CAMERA`, `RECEIVE_BOOT_COMPLETED`, `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM`, `WAKE_LOCK`.

## 6. Phase FB-5 — Firebase App Check

- Play Integrity provider production.
- Debug provider chỉ dev.
- Rollout monitor trước enforcement.
- Enforce Firestore/Functions/Storage sau khi xác nhận traffic hợp lệ.

## 7. Phase FB-6 — Secrets & signing hygiene

- Production keystore chỉ GitHub Secrets.
- Debug keystore không dùng cho release.
- Không embed secret/service account.
- Production environment config có controlled source.

## 8. Phase FB-7 — Firestore indexes

Thêm `firestore.indexes.json` và rà index:
- transactions date/category/type;
- reminders enabled/nextTriggerDate;
- budgets periodKey/categoryId;
- debt payments;
- deals;
- savingSpin sessions.

## 9. Acceptance

- Emulator Rules PASS.
- Cross-user access deny.
- Production debug hooks bị khóa.
- App Check rollout thành công.
- Firebase deploy reproducible từ repo.
