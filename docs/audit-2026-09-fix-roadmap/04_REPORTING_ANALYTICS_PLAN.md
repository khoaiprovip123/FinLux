# Phase Plan — Reporting & Analytics

## 1. Mục tiêu

Biến Reports thành hệ thống báo cáo tài chính có semantics nhất quán, không chỉ là tập hợp biểu đồ.

## 2. Phase RP-1 — Unified Financial Period

Một resolver duy nhất về semantics:
- Today
- Yesterday
- Day
- Week
- Last 7 Days
- Month
- Salary Cycle
- Quarter
- Year
- Custom

### Quy tắc
- Query window dùng `[start, endExclusive)`.
- Timezone lấy từ finance config.
- Salary Cycle là period thực, không đổi label nhưng vẫn query tháng.
- Previous period phải có semantics rõ và test được.

## 3. Phase RP-2 — Daily Statement

Cho mỗi ngày:
```
Opening Balance
+ Cash In
- Cash Out
= Closing Balance
```

Tách semantics:
- income;
- expense;
- transfer in/out;
- goal reclassification;
- deal principal;
- deal gain/loss;
- debt principal/interest.

### Acceptance
Closing ngày N = Opening ngày N+1 trong cùng phạm vi ví nếu không có reconciliation adjustment.

## 4. Phase RP-3 — Wallet Statement

Mỗi ví:
- opening balance;
- current/closing balance;
- income;
- expense;
- transfer in;
- transfer out;
- net change;
- transaction count;
- category breakdown;
- statement transaction list.

Drill-down:
```
Tổng quan → Ví → Danh mục → Giao dịch
```

## 5. Phase RP-4 — Income/Expense

- Summary card.
- Category ranking.
- Daily trend.
- Average/day.
- Largest transaction.
- Previous-period comparison.
- Internal transfer không tính income/expense.
- Deal principal recovery/outlay không làm méo lifestyle cashflow.

## 6. Phase RP-5 — Budget

- limit;
- spent;
- remaining;
- usage %;
- over-budget amount;
- previous-period comparison;
- category drill-down;
- salary-cycle aware.

## 7. Phase RP-6 — Net Worth

Chốt một accounting model duy nhất:
```
Liquid Wallet Assets
+ Goal-held Assets (chỉ nếu tách khỏi wallet)
+ Recoverable Deal Principal
- Debt Liability
= True Net Worth
```

Không double-count Goal hoặc Deal.

## 8. Phase RP-7 — Debt

- total outstanding;
- original debt;
- principal paid;
- interest paid;
- payoff forecast;
- debt by type;
- payment timeline;
- Snowball/Avalanche comparison.

## 9. Phase RP-8 — Deal

- capital outlay;
- recovered principal;
- outstanding principal;
- realized gain/loss;
- ROI;
- active/completed;
- Investment vs Lending.

## 10. Phase RP-9 — Saving Spin

- total saved;
- streak;
- completion rate;
- skipped;
- destination breakdown;
- trend;
- salary-cycle filter nếu schedule theo lương.

## 11. Phase RP-10 — Export

Excel:
- Summary
- Transactions
- Wallet Statement
- Category
- Budget
- Debt
- Deal

PDF:
- executive summary;
- selected period;
- KPI;
- charts;
- definition/disclaimer.

Export phải dùng cùng Report DTO/use case với UI, không tự tính lại.

## 12. Architecture

```
ReportsViewModel
  ↓
ReportQueryCoordinator
  ├─ DailyStatementUseCase
  ├─ WalletStatementUseCase
  ├─ BudgetReportUseCase
  ├─ NetWorthUseCase
  ├─ DebtReportUseCase
  ├─ DealReportUseCase
  └─ SavingSpinReportUseCase
```

## 13. Acceptance

- Cùng dataset → cùng KPI giữa Home/Reports/Export.
- Salary Cycle filter đúng start/end.
- Transfer không làm sai total income/expense.
- Opening/closing balance truy nguyên được tới ledger.
