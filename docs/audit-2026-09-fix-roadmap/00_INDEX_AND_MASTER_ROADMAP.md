# FinLux — Master Fix Roadmap sau Audit 09/2026

> Baseline: `main` tại commit `4eece8c6cce660fb9ae8e3aafa8f953e72d50989` — FinLux v1.22.0 (versionCode 165).  
> Mục tiêu: đưa FinLux từ trạng thái feature-rich nhưng còn rủi ro contract/data sang production-ready, ưu tiên tuyệt đối tính đúng đắn tài chính và khả năng kiểm chứng.

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

- **P0-A — Budget schema drift:** Android ghi `periodKey/periodStart/periodEndExclusive/periodBasis` nhưng Rules trên main chưa chấp nhận đầy đủ schema mới.
- **P0-B — Salary Cycle contract drift:** Android/Rules dùng `financialPreferences/salaryCycle`, `paydayDay`, `paydayRuleType`; Cloud Functions trên main đọc path/field cũ.
- **P0-C — Deal/Investment Rules thiếu contract:** Repository dùng collection `deals`, transaction có `dealId/dealFlowType`; Rules main chưa chấp nhận.
- **P0-D — Wallet mutation invariant:** một số flow Goal/Debt/Deal thay đổi `balance` nhưng chưa đồng bộ `lastTransactionId`.
- **P0-E — Deal loss settlement:** không dùng fake wallet như `DEAL_SETTLEMENT`.
- **P0-F — Release signing:** production release phải fail-closed, tuyệt đối không fallback debug key/APK.
- **P0-G — SalaryCycleReceiver:** khóa production receiver và debug-only force hook.
- **P0-H — Storage privacy:** avatar/receipt owner-only, tách read/write/delete.

## 5. Chiến lược PR đề xuất

- PR-01: Salary Cycle shared contract + tests
- PR-02: Budget period schema/rules/functions
- PR-03: Transaction/Wallet invariant
- PR-04: Goal + Debt financial mutation
- PR-05: Deal contract + settlement + cascade
- PR-06: Storage + Receiver security
- PR-07: Release fail-closed
- PR-08: Reporting period + daily statement
- PR-09+: UI/UX theo từng screen cluster
- PR tiếp theo: architecture refactor từng bounded-context

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
