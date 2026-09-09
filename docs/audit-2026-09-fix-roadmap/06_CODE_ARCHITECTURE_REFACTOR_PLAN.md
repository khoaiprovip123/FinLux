# Phase Plan — Code Architecture & Refactor

## 1. Mục tiêu

Giữ Clean Architecture nhưng giảm God files, duplicate theme implementations và business logic nằm ở presentation.

## 2. Vấn đề hiện tại

Một số file rất lớn:
- `PrismReportsScreen.kt` ~180 KB
- `PrismHomeScreen.kt` ~139 KB
- `ReportsViewModel.kt` ~43 KB
- nhiều Settings/Wallet/SavingSpin screen >40 KB

Ba UI style tạo nguy cơ sửa một feature ba lần.

## 3. Phase AR-1 — Package by bounded context

Đề xuất dài hạn:
```
feature/
  transaction/
  wallet/
  budget/
  reporting/
  salarycycle/
  goal/
  debt/
  deal/
  savingspin/
```

Chưa bắt buộc multi-module. Trước tiên refactor package/file boundaries.

## 4. Phase AR-2 — Tách Reports

Tách:
- ReportsRoute
- ReportsScreen
- ReportsUiState
- ReportPeriodController
- ReportSummarySection
- CashFlowSection
- CategorySection
- WalletSection
- BudgetSection
- GoalSection
- DebtSection
- DealSection
- SavingSpinSection

Business calculation chuyển vào use case.

Target:
- screen orchestration <400 lines;
- section file <300–400 lines.

## 5. Phase AR-3 — Tách Home

- HomeHeader
- NetWorthCard
- PeriodSummary
- QuickActions
- BudgetSnapshot
- RecentTransactions
- SavingSpinEntry
- Insights

## 6. Phase AR-4 — ViewModel responsibility

ViewModel chỉ:
- collect domain flows;
- dispatch intent;
- expose immutable UI state;
- emit one-off effects.

Không:
- accounting formula lớn;
- export calculation;
- Firestore query trực tiếp;
- theme-specific business behavior.

## 7. Phase AR-5 — Repository contract

- interface ở Domain;
- implementation ở Data;
- mapper tách riêng;
- business validation thuộc UseCase;
- Firebase/Demo pass cùng contract tests.

## 8. Phase AR-6 — Money & Time

- Money dùng Long minor unit.
- Không Double cho amount.
- ROI/rate có thể Double/BigDecimal.
- Instant + finance ZoneId.
- Logic testable inject FinanceClock, tránh `LocalDate.now()` trực tiếp.

## 9. Phase AR-7 — Error model

Chuẩn hóa:
```
DomainError
ValidationError
AuthError
PermissionError
NetworkError
ConflictError
NotFoundError
UnknownError
```

UI không parse exception message.

## 10. Phase AR-8 — Demo parity

Demo mode phải cùng:
- validation;
- transaction semantics;
- period semantics;
- state machine.

Không để Demo pass còn Firebase fail.

## 11. Phase AR-9 — Multi-module sau stabilization

Candidate:
- core:model
- core:designsystem
- core:firebase
- feature:transactions
- feature:reports

Chỉ làm sau khi semantics/package boundaries ổn.

## 12. Acceptance

- Không God ViewModel.
- Không God Screen.
- Core calculation có unit tests.
- Demo/Firebase parity.
- Dependency direction không vi phạm Domain.
