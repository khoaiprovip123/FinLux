# FinLux — Master Fix Roadmap sau Audit 09/2026

> Baseline: `main` tại commit `4eece8c6cce660fb9ae8e3aafa8f953e72d50989` — FinLux v1.22.0 (versionCode 165).  
> Mục tiêu: đưa FinLux từ trạng thái feature-rich nhưng còn rủi ro contract/data sang production-ready, ưu tiên tuyệt đối tính đúng đắn tài chính và khả năng kiểm chứng.

> **RE-AUDIT 2026-09-07:** sign-off trong `12_FINAL_AUDIT_EXECUTION_REPORT.md` chưa được chấp nhận.
> Các blocker và thứ tự sửa mới được quản lý tại `13_POST_AUDIT_REMEDIATION_PLAN.md`;
> tài liệu 13 là source of truth cho trạng thái production-readiness hiện tại.

## 1. Nguyên tắc thực hiện

1. Không thêm feature lớn trước khi hoàn tất P0 Financial Integrity.
2. Không merge một PR quá lớn. Mỗi PR chỉ giải quyết một bounded-context hoặc một invariant rõ ràng.
3. Mọi thay đổi số dư ví phải có ledger entry tương ứng và chạy atomically.
4. Android Domain/Data, Firestore Rules và Cloud Functions phải dùng cùng một contract.
5. Không bump version trước khi toàn bộ gate của phase hiện tại xanh.
6. Demo repository không được dùng làm bằng chứng rằng Firebase production hoạt động.
7. Mọi fix P0 phải có regression test.
8. Classic/Modern/Prism dùng chung business state và semantics; khác nhau chủ yếu ở presentation tokens/layout.

## 2. Thứ tự Phase tổng thể

| Phase | Nhóm | Mức | Mục tiêu |
|---|---|---|---|
| 0 | Baseline & Freeze | P0 | Chốt baseline, invariant, danh sách contract |
| 1 | Financial Data Integrity | P0 | Sửa Budget/Wallet/Goal/Debt/Deal/Transaction |
| 2 | Firebase & Security | P0 | Rules, Functions, Storage, Receiver, App hardening |
| 3 | Business Rules & Use Cases | P0/P1 | Chuẩn hóa nghiệp vụ, edge case, transaction semantics |
| 4 | Reporting & Analytics | P1 | Một period contract, daily statement, opening/closing balance |
| 5 | UI/UX & Design System | P1 | Đồng bộ giao diện, giảm drift giữa 3 theme |
| 6 | Code Architecture & Refactor | P1 | Tách file lớn, giảm duplicate, bounded context rõ |
| 7 | Test/QA/Regression | P0/P1 | Contract test + UI smoke + regression matrix |
| 8 | CI/CD & Release Governance | P0/P1 | Fail-closed release, required checks, artifact integrity |
| 9 | Performance & Observability | P1/P2 | App Check, Crashlytics, indexes, scale Functions |
| 10 | Docs & Repo Hygiene | P2 | Đồng bộ docs, dọn generated output, source of truth |
| 11 | Release Candidate | Gate | UAT, migration, release, rollback |

## 3. Dependency bắt buộc

```
Phase 0
  ↓
Phase 1 Financial Integrity
  ↓
Phase 2 Firebase/Security
  ↓
Phase 3 Business Rules
  ├──────────────┐
  ↓              ↓
Phase 4 Reports  Phase 6 Architecture
  ↓              ↓
Phase 5 UI/UX    └──────┐
  └──────────────┬──────┘
                 ↓
         Phase 7 Test/QA
                 ↓
         Phase 8 CI/CD
                 ↓
      Phase 9 Observability
                 ↓
      Phase 10 Docs/Hygiene
                 ↓
      Phase 11 Release Candidate
```

## 4. Blocker P0 đã xác định

- **P0-A — Budget schema drift:** **Đã xử lý local ở PR-02 (2026-09-07)** — Rules chấp nhận schema kỳ mới, giữ legacy compatibility và khóa aggregate server-owned.
- **P0-B — Salary Cycle contract drift:** **Đã xử lý local ở PR-01 (2026-09-07)** — Functions đọc path/field hiện hành, resolve theo timezone và dùng chung 11 vector contract với Kotlin.
- **P0-C — Deal/Investment Rules thiếu contract:** Repository dùng collection `deals`, transaction có `dealId/dealFlowType`; Rules main chưa chấp nhận.
- **P0-D — Wallet mutation invariant:** **Đã xử lý local ở PR-03 (2026-09-07)** — Mọi mutation `wallet.balance` phải kèm `lastTransactionId` có ledger transition thật trong batch.
- **P0-E — Deal loss settlement:** không dùng fake wallet như `DEAL_SETTLEMENT`.
- **P0-F — Release signing:** production release phải fail-closed, tuyệt đối không fallback debug key/APK.
- **P0-G — SalaryCycleReceiver:** khóa production receiver và debug-only force hook.
- **P0-H — Storage privacy:** avatar/receipt owner-only, tách read/write/delete.

### Blocker mở lại sau re-audit

- **RA-P0-01 — Stale ledger reuse:** `walletHasLedgerTransition` chưa chứng minh transaction thực sự đổi trong cùng request.
- **RA-P0-02 — Goal/Debt direct mutation:** financial aggregates và payment contract chưa được Rules khóa đủ chặt.
- **RA-P0-03 — Deal authority/cascade:** aggregate và delete vẫn có thể bypass repository contract.
- **RA-P0-04 — Release state:** fixes chưa commit/push; `v1.22.0` đã tồn tại và artifact release hiện có ký debug.
- **Release Gate:** `[BLOCKED]` cho đến khi R-01..R-08 trong remediation plan hoàn tất.

## 5. Chiến lược PR đề xuất

- PR-01: Salary Cycle shared contract + tests — **DONE local 2026-09-07**
- PR-02: Budget period schema/rules/functions — **DONE local 2026-09-07**
- PR-03: Transaction/Wallet invariant — **DONE local 2026-09-07**
- PR-04: Goal + Debt financial mutation — **DONE local 2026-09-07**
- PR-05: Deal contract + settlement + cascade
- PR-06: Storage + Receiver security
- PR-07: Release fail-closed
- PR-08: Reporting period + daily statement
- PR-09+: UI/UX theo từng screen cluster
- PR tiếp theo: architecture refactor từng bounded-context
- Remediation R-00..R-09: theo `13_POST_AUDIT_REMEDIATION_PLAN.md` — **READY FOR EXECUTION, release BLOCKED**

### Tiến độ thực thi preliminary trước re-audit

> Các dòng dưới đây ghi lại implementation local đã thực hiện, không còn là bằng chứng sign-off.
> Trạng thái acceptance hiện hành phải đọc theo RA-P0 và R-00..R-09 phía trên.

- **P0-B đã xử lý ở PR-01:** Functions đọc path/field hiện hành, resolve theo timezone và dùng chung
  11 vector contract với Kotlin. Fallback schema cũ chỉ đọc trong giai đoạn chuyển tiếp.
- **P0-A đã xử lý ở PR-02:** Budget ghi biên kỳ bằng Timestamp, Android không còn mutation aggregate;
  Rules khóa `spentAmount/notified80/notified100`, Functions reconcile từ ledger khi transaction hoặc
  contract/hạn mức Budget thay đổi. Legacy `month` và epoch-millis vẫn được đọc trong giai đoạn chuyển tiếp.
- **P0-D đã xử lý ở PR-03:** Mọi mutation `wallet.balance` bắt buộc kèm `lastTransactionId` bằng helper
  chuẩn `walletLedgerUpdate(balance, transactionId)`; Firestore Rules `walletHasLedgerTransition` đảm bảo
  không thể gian lận hoặc gửi fake transactionId; transfer delete trỏ đúng transaction leg (`_out`/`_in`).
- **PR-04 đã xử lý:** Khóa chặt invariant cho Goal & Debt: cấm xóa Goal khi còn số dư tích lũy (`savedAmount > 0`),
  atomic deposit/withdraw với ledger transition; chặn thanh toán nợ vượt dư nợ còn lại, bắt buộc `amount == principal + interest`,
  cascade delete subcollection `payments` khi xóa Debt. Đạt 37/37 Rules emulator tests và 100% Android Unit tests pass.
- **PR-05 đã xử lý:** Hoàn thiện Deal contract & cascade rules: loại bỏ fake wallet `DEAL_SETTLEMENT` (P0-E),
  `CAPITAL_LOSS` là non-cash accounting ledger (`walletId` null/blank, không ảnh hưởng số dư ví tiền mặt);
  bổ sung Firestore Rules `validDeal`, hỗ trợ split inflow (`counterpartTransactionId` liên kết tổng delta ví);
  toàn bộ 42/42 Rules emulator tests và 100% Android Unit tests pass.
- **PR-06 đã xử lý:** Hardening Firebase Storage Rules (owner-only cho avatar & receipt, tách biệt rõ ràng read/create/update/delete, không dùng `request.resource` ở read/delete); khóa `SalaryCycleReceiver` với `android:exported="false"`, bảo vệ cờ `force` bằng `BuildConfig.DEBUG`; loại bỏ quyền nguy hiểm không dùng `REQUEST_INSTALL_PACKAGES`.
- **PR-07 đã xử lý:** Enforce fail-closed release signing & CI/CD governance: cấm triệt để debug keystore (`gradle/debug.keystore`, `androiddebugkey`) trong release build, gỡ bỏ debug fallback ở `build.gradle.kts` và `.github/workflows/release.yml`, kiểm chứng fail-closed trên CI và test gates (100% Android tests PASS).
- **PR-08 đã xử lý:** Chuẩn hóa Reporting period & Daily financial statement: thống nhất query window `[start, endExclusive)`, đối chiếu số dư từng ngày (`Closing T = Opening T+1`), phân tách operating vs non-operating flow (deal principal, debt, transfer), fix lọc transaction theo ví (100% Android tests PASS).
- **PR-09 đã xử lý:** UI/UX Screen Clusters & Design System Parity: Rà soát và loại bỏ toàn bộ mã màu tĩnh hardcode trong `TransferMoneyScreen.kt`, `WalletTransactionsBottomSheet.kt`, `PrismTransactionsScreen.kt`, thay bằng semantic design tokens (`LocalFinluxTokens`, `FinluxColors`), đồng bộ theme parity & Liquid Glass (100% Android tests PASS).
- **PR-10 đã xử lý:** Performance, Build Hygiene & Full Release Gate Verification: Dọn dẹp debug logs khỏi git index và cập nhật `.gitignore` (`firestore-debug.log`, `firebase-debug.log`), tối ưu Compose recomposition & clean warnings, kiểm chứng toàn bộ release gates: `testDebugUnitTest` 100% PASS và `assembleDebug` APK đóng gói thành công 100% (43 actionable tasks). Toàn bộ 10 PRs của Master Roadmap đã hoàn thành xuất sắc.

> **Re-audit correction:** câu kết “10 PRs hoàn thành” ở trên là kết luận preliminary và đã bị supersede.
> Release vẫn `[BLOCKED]` cho đến khi `13_POST_AUDIT_REMEDIATION_PLAN.md` hoàn tất.

## 6. KPI hoàn tất

| KPI | Target |
|---|---|
| Financial invariant contract tests | 100% flow trọng yếu |
| Firestore emulator rules tests | PASS |
| Android unit tests | PASS |
| Compose UI smoke | Có luồng chính |
| Lint | PASS |
| Debug APK | PASS |
| Release APK signed production | PASS, không fallback |
| Direct schema drift | 0 |
| Hard-coded presentation colors | 0 hoặc exception documented |
| Crash reporting | enabled |
| Main release gates | bắt buộc |

## 7. Bộ tài liệu roadmap

1. `01_BUSINESS_RULES_AND_USE_CASES_PLAN.md`
2. `02_FINANCIAL_DATA_INTEGRITY_PLAN.md`
3. `03_FIREBASE_BACKEND_SECURITY_PLAN.md`
4. `04_REPORTING_ANALYTICS_PLAN.md`
5. `05_UI_UX_DESIGN_SYSTEM_PLAN.md`
6. `06_CODE_ARCHITECTURE_REFACTOR_PLAN.md`
7. `07_TEST_QA_REGRESSION_PLAN.md`
8. `08_CICD_RELEASE_GOVERNANCE_PLAN.md`
9. `09_PERFORMANCE_OBSERVABILITY_PLAN.md`
10. `10_DOCUMENTATION_REPO_HYGIENE_PLAN.md`
11. `11_EXECUTION_CHECKLIST_AND_DOD.md`
12. `12_FINAL_AUDIT_EXECUTION_REPORT.md`
13. `13_POST_AUDIT_REMEDIATION_PLAN.md`

## 8. Definition of Success

FinLux chỉ được xem là production-ready khi có thể chứng minh tự động:

```
Opening Balance
+ Income
+ Transfer In
- Expense
- Transfer Out
= Closing Balance
```

và tổng biến động mọi ví/goal/debt/deal đều truy nguyên được về ledger + business event hợp lệ, cùng một kết quả giữa Android, Firestore và Reports.
