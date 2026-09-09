# Phase Plan — Firebase Backend & Security

## 1. Mục tiêu

Đưa Firestore/Storage/Functions/Android components về mô hình least privilege, deny-by-default, idempotent và kiểm thử được bằng emulator.

## 2. Phase FB-1 — Firestore Rules

- Bổ sung schema đầy đủ cho Budget period mới.
- Bổ sung match/rules cho Deals.
- Cho transaction fields cần thiết như `dealId/dealFlowType/correlationId` theo contract.
- Server-owned fields không cho client sửa trực tiếp.
- Wallet balance chỉ đổi cùng transaction hợp lệ.
- Immutable fields phải được khóa.
- Validate enum casing thống nhất.
- Deny unknown keys ở entity tài chính trọng yếu.

### Rules tests
owner access, cross-user deny, valid create, invalid field deny, invalid wallet delta deny, valid atomic mutation pass, spoofed aggregate deny.

## 3. Phase FB-2 — Cloud Functions

- Sửa salary config path/field.
- Tách `financialPeriod.ts`.
- Tách `transactionSemantics.ts`.
- Scheduled/event functions idempotent.
- Budget notification 80/100 không duplicate.
- Retry-safe event processing.
- Tránh scan toàn bộ users khi có thể query due entities.

## 4. Phase FB-3 — Storage Rules

### Avatar
- read: owner only;
- create/update: owner + image + size limit;
- delete: owner.

### Receipt
- read: owner;
- create/update: owner + image MIME + size;
- delete: owner.

Không dùng `request.resource` cho read/delete condition.

## 5. Phase FB-4 — Android Manifest hardening

### SalaryCycleReceiver
- `android:exported=false`.
- force flag chỉ debug.
- External intent không được kích hoạt money mutation.

### Permissions
Rà soát REQUEST_INSTALL_PACKAGES, SCHEDULE_EXACT_ALARM, USE_EXACT_ALARM, CAMERA, POST_NOTIFICATIONS; chỉ giữ quyền thực sự cần.

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
