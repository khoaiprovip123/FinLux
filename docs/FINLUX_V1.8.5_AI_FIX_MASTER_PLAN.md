# FINLUX v1.8.5 — SECURITY & RELEASE HARDENING MASTER PLAN

**Baseline:** FinLux v1.8.4 / versionCode 103  
**Target đề xuất:** v1.8.5 / versionCode 104 sau khi toàn bộ quality gate PASS.  
**Mục tiêu:** Security → Release/OTA Integrity → Test Reliability → Finance Timezone.  
**Quy tắc:** Không thêm feature mới, không redesign UI, không rewrite project.

---

## 1. NGUYÊN TẮC BẮT BUỘC

1. Không đổi `applicationId = "com.finlux.app"`.
2. Không đổi Firebase project.
3. Không migration destructive dữ liệu Firestore.
4. Không đổi `Money(Long)` sang `Double`/`Float`.
5. Không commit production keystore hoặc password lên git.
6. Không dùng Debug APK làm production OTA release.
7. Không tự bump version trước khi toàn bộ phase PASS.
8. Mỗi task phải theo chu trình:
   `READ → UNDERSTAND → TEST → PATCH → TEST → BUILD → REPORT`
9. Một nhóm logic = một commit rõ ràng.

---

## 2. THỨ TỰ XỬ LÝ (EXECUTION ORDER)

```text
P0-S01: Firestore Rules (Default Deny + Explicit Schema Validation)
  ↓
P0-S02: Release Signing Configuration
  ↓
P0-S03: Split CI Workflow & Tagged Release Workflow
  ↓
P0-S04: OTA Integrity Verification (versionCode, SHA-256, Package & Cert Digest)
  ↓
P0-T01: Fix Flaky Tests & Time Determinism
  ↓
P0-T02: Complete Transaction Integrity Test Matrix
  ↓
P1-TZ01: Account Finance Timezone Strategy
  ↓
FINAL QUALITY GATE (Test 100% PASS, Lint, Debug/Release Build)
  ↓
Release v1.8.5 / versionCode 104
```

---

## 3. CHI TIẾT TỪNG TASK

### P0-S01 — Fix Firestore Rules Wildcard Write Bypass
- **Severity:** CRITICAL
- **File:** `firestore.rules`
- **Vấn đề:** Tránh wildcard `match /{subcollection}/{docId}` cho phép bypass validation riêng của transactions/wallets/budgets.
- **Yêu cầu:**
  - Mô hình **Default Deny + Explicit Allow** cho từng collection (`wallets`, `categories`, `transactions`, `budgets`, `goals`, `reminders`, `notifications`).
  - Validation: Transaction (`amount > 0`, `type` hợp lệ, timestamp), Wallet (`balance` int, `type` hợp lệ), Budget (`limitAmount >= 0`, `spentAmount >= 0`, `month` định dạng `YYYY-MM`).
  - Bảo đảm cô lập tuyệt đối dữ liệu người dùng (`isOwner(uid)`).

### P0-S02 — Production Release Signing
- **Severity:** CRITICAL
- **Yêu cầu:**
  - Cấu hình ký số Release APK bằng Private Release Keystore qua GitHub Secrets (`FINLUX_KEYSTORE_BASE64`, `FINLUX_KEYSTORE_PASSWORD`, `FINLUX_KEY_ALIAS`, `FINLUX_KEY_PASSWORD`).
  - Không lưu password/keystore thật trong repository.

### P0-S03 — Tách Biệt CI & Tagged Release Workflow
- **File:** `.github/workflows/ci.yml` & `.github/workflows/release.yml`
- **CI Workflow (`ci.yml`):** Chạy trên pull request và push `main` (chạy tests, lint, debug build; không tạo release).
- **Release Workflow (`release.yml`):** Chỉ trigger khi push tag `v*` (ví dụ `v1.8.5`) hoặc workflow dispatch. Kiểm tra tag khớp `versionName`, chạy unit test, build release APK, tạo SHA-256 checksum và phát hành GitHub Release.

### P0-S04 — OTA Integrity & Trust Verification
- **File:** `app/src/main/java/com/finlux/app/core/updater/AppUpdateManager.kt`
- **Yêu cầu:**
  - So khớp phiên bản dựa trên `versionCode` làm authority (`remoteVersionCode > BuildConfig.VERSION_CODE`).
  - Kiểm tra Package Name (`com.finlux.app`).
  - Xác thực mã băm **SHA-256** của APK đã tải về khớp với checksum công bố trên Release.
  - Xác thực Signing Certificate Digest của file APK tải về trùng khớp với chứng chỉ của ứng dụng đang cài đặt.
  - Tự động xóa file và báo lỗi nếu phát hiện file hỏng hoặc bị giả mạo.

### P0-T01 & P0-T02 — Test Determinism & Complete Matrix
- **File:** `app/src/test/java/com/finlux/app/data/remote/firebase/FirebaseTransactionRepositoryTest.kt`
- **Yêu cầu:**
  - Dùng fixed clock (`Instant.parse("2026-08-15T03:00:00Z")`) loại bỏ hoàn toàn tính phụ thuộc thời gian thực tế.
  - Bao phủ toàn bộ ma trận test: Add / Edit / Delete / Transfer cho các trường hợp biên (số tiền tối đa, số tiền 0/âm, tràn số, mất kết nối, stale object).

### P1-TZ01 — Account Finance Timezone Strategy
- **File:** `app/src/main/java/com/finlux/app/core/time/FinanceTime.kt`
- **Yêu cầu:**
  - Đảm bảo tính toán tháng tài chính (`financialMonth`, ranh giới tháng `23:30` và `00:30`) luôn nhất quán và không bị sai lệch khi người dùng di chuyển múi giờ.

---

## 4. DEFINITION OF DONE & QUALITY GATE

Trước khi phát hành phiên bản v1.8.5:
- [ ] Firestore Rules: Default deny, không còn wildcard bypass.
- [ ] CI tách biệt khỏi Release workflow; tag `v*` mới kích hoạt release.
- [ ] Release APK được ký bằng private release key qua GitHub Actions.
- [ ] OTA Update Manager xác thực toàn vẹn: `versionCode`, Package ID, SHA-256, Certificate Digest.
- [ ] 100% Unit Tests PASS với thời gian giả lập cố định.
- [ ] Ma trận kiểm thử Transaction hoàn thiện đầy đủ các trường hợp biên.
- [ ] `assembleDebug` và `assembleRelease` build thành công.
- [ ] Cập nhật `CHANGELOG.md` và `HANDOVER_LOG.md`.
