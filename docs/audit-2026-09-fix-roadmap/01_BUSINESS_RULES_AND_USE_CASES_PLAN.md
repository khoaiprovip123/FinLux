# Phase Plan — Business Rules & Use Cases

## 1. Mục tiêu

Chuẩn hóa toàn bộ nghiệp vụ FinLux thành business contract rõ ràng trước khi tiếp tục mở rộng feature. Mọi ViewModel/Repository chỉ triển khai contract đã được xác định ở Domain.

## 2. Bounded Context cần khóa

- Transaction Ledger
- Wallet & Transfer
- Budget
- Salary Cycle / Financial Period
- Goals & Savings
- Debt
- Deals / Investment / Lending
- Saving Spin
- Reminder / Notification
- Reporting

## 3. Phase BR-0 — Business invariant catalogue

| ID | Invariant |
|---|---|
| BR-FIN-001 | Expense hợp lệ làm giảm wallet balance trừ credit-card semantics |
| BR-FIN-002 | Income hợp lệ làm tăng wallet balance |
| BR-FIN-003 | Transfer phải có OUT + IN cùng correlationId |
| BR-FIN-004 | Tổng tài sản không thay đổi vì transfer nội bộ |
| BR-FIN-005 | Xóa/sửa transaction phải reverse/reapply balance chính xác |
| BR-FIN-006 | Goal deposit không được làm mất giá trị Net Worth |
| BR-FIN-007 | Debt principal và interest phải tách semantics |
| BR-FIN-008 | Deal principal recovery không phải income thực |
| BR-FIN-009 | Deal capital gain mới là profit/income |
| BR-FIN-010 | Capital loss không cần fake wallet |
| BR-PERIOD-001 | Một instant chỉ thuộc đúng một financial period |
| BR-PERIOD-002 | DAY_OF_MONTH 29-31 phải clamp theo tháng |
| BR-BUDGET-001 | spentAmount là derived/aggregate, client không được tự sửa tùy ý |
| BR-SS-001 | Saving Spin spin result bất biến sau khi chọn |
| BR-SS-002 | Complete Saving Spin phải idempotent |

## 4. Phase BR-1 — Transaction & Wallet

### Tasks
- Chuẩn hóa enum/type casing giữa domain và Firestore.
- Thêm `correlationId` cho transfer pair nếu chưa có.
- Xác định transaction nào là cashflow, transaction nào chỉ là ledger reclassification.
- Chặn expense với ví thường không đủ số dư.
- Card/Credit Wallet dùng rule riêng.
- Edit transaction: reverse old delta → validate new state → apply new delta → update ledger atomically.
- Delete transaction phải reverse delta atomically.
- Chặn tự chuyển cùng một ví.
- Amount phải > 0 và trong money policy.

### Acceptance
- Không tạo được trạng thái ví lệch ledger.
- Multi-device concurrent write không phá balance.

## 5. Phase BR-2 — Salary Cycle

### Tasks
- Một model duy nhất cho `enabled/paydayRuleType/paydayDay/budgetPeriodBasis/financeTimeZone/rolloverRule/salaryWalletId/savingsWalletId`.
- Xử lý FIRST_DAY_OF_MONTH, LAST_DAY_OF_MONTH, DAY_OF_MONTH, tháng thiếu ngày 29/30/31, leap year, timezone.
- Rollover idempotent theo `cycleKey`.
- ASK_EACH_CYCLE không tự mutation tiền.
- MOVE_TO_SAVINGS phải có source/destination valid.

### Acceptance
Android và Functions resolve cùng `periodKey/start/endExclusive` cho cùng input.

## 6. Phase BR-3 — Budget

- Budget identity = category + financial period.
- `spentAmount` server-owned derived aggregate.
- Không hard-code month nếu basis = SALARY_CYCLE.
- Copy budget reset spent + threshold flags.
- Threshold 80/100 idempotent.
- Expense edit/delete phải reconcile lại budget.

## 7. Phase BR-4 — Goals

- Goal deposit = asset reclassification, không làm giảm true net worth.
- Withdraw = reverse reclassification.
- Goal savedAmount phải atomically khớp ledger.
- Không rút quá savedAmount.
- Xóa goal: cấm khi còn số dư hoặc bắt buộc hoàn tiền về ví trước.

## 8. Phase BR-5 — Debt

- `amount = principalPaid + interestPaid`.
- principal không vượt remaining principal.
- interest không làm giảm principal.
- wallet + debt + payment history + ledger phải atomic.
- Settled chỉ khi principal remaining = 0.
- Xóa debt có payment history phải có cascade/deny policy rõ.

## 9. Phase BR-6 — Deal / Investment / Lending

- OUTLAY_CAPITAL: asset reclassification, không tính lifestyle expense.
- PRINCIPAL_RECOVERY: không tính income.
- CAPITAL_GAIN: income/profit.
- CAPITAL_LOSS: asset write-off, không fake wallet.
- Chặn outlay nếu ví thường thiếu tiền.
- Deal close/reopen/revert phải có state machine.
- Delete deal có ledger entries cần atomic cascade hoặc deny + archive.

## 10. Phase BR-7 — Saving Spin

- Config validation ở Domain + Rules.
- Spin result immutable.
- Complete phải chọn destination valid.
- Tiền mặt/bank transfer có semantics rõ.
- Snooze/skip theo state machine.
- Reminder scheduling không duplicate.
- Streak dựa trên schedule key.

## 11. Gate hoàn tất

- Business rules được phản ánh ở tests.
- Không còn business calculation nằm rải rác trong Composable.
- Không có hai implementation khác semantics cho cùng use case.
