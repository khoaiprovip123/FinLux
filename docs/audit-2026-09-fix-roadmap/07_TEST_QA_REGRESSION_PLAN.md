# Phase Plan — Test, QA & Regression

## 1. Mục tiêu

Chuyển từ “unit tests xanh” sang “có bằng chứng flow production hoạt động đúng”.

## 2. Test Pyramid

```
          UI/E2E smoke
        Integration tests
      Repository contract tests
    Domain/ViewModel unit tests
  Firestore/Storage Rules tests
```

## 3. Phase QA-1 — P0 Financial Contract Tests

Firebase Emulator + repository thật:

### Transaction
- add income;
- add expense;
- insufficient balance deny;
- edit same wallet;
- edit move wallet;
- delete;
- concurrent mutations.

### Transfer
- atomic OUT/IN;
- same-wallet deny;
- insufficient balance deny;
- correlation invariant;
- delete/reversal.

### Goal
- deposit;
- withdraw;
- over-withdraw deny;
- wallet/goal/ledger consistent.

### Debt
- principal + interest;
- insufficient wallet;
- settle;
- payment history;
- delete policy.

### Deal
- outlay;
- principal recovery;
- gain;
- loss;
- reopen/revert;
- cascade/delete policy.

### Budget
- calendar month;
- salary cycle;
- edit/delete expense reconcile;
- threshold 80/100 idempotent.

## 4. Phase QA-2 — Resolver Golden Tests

Dùng cùng bảng expected vectors cho Kotlin + TypeScript:
- leap years;
- day 28/29/30/31;
- FIRST_DAY;
- LAST_DAY;
- before/at/after payday;
- timezone boundary.

## 5. Phase QA-3 — Repository Contract Tests

Một suite dùng chung cho Demo repository và Firebase repository để đảm bảo cùng behavior.

## 6. Phase QA-4 — ViewModel Tests

Mỗi ViewModel test:
- initial loading;
- data success;
- data error;
- user intent;
- validation;
- period change;
- retry;
- empty state.

## 7. Phase QA-5 — Compose UI Smoke

Tối thiểu:
1. Login/Register validation.
2. Add Expense.
3. Add Income.
4. Transfer.
5. Switch report period.
6. Open wallet detail.
7. Create budget.
8. Salary Cycle settings.
9. Saving Spin core flow.
10. Theme switch.

## 8. Phase QA-6 — Visual/Device QA

Matrix:
- Classic/Modern/Prism;
- light/dark;
- 360/412dp;
- portrait/landscape;
- font scaling;
- gesture/3-button nav.

Checklist:
- clipping;
- overlap;
- invisible text;
- bottom-bar collision;
- keyboard collision;
- chart labels;
- large currency values.

## 9. Phase QA-7 — Migration Tests

Fixture datasets:
- legacy month budgets;
- salary budgets;
- old transaction casing;
- existing goal/debt/deal data.

Verify:
- document count;
- sum amount;
- wallet balance;
- references;
- no data loss.

## 10. Phase QA-8 — Release Regression

Before tag:
```
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
Firebase Functions build
Firestore emulator tests
Compose androidTest compile/run
Release assemble with production signing
```

## 11. Acceptance

- P0 regression suite bắt được các lỗi contract từng tồn tại.
- Không merge nếu contract test fail.
- Có test evidence cho Firebase mode, không chỉ Demo.
