# Phase Plan — UI/UX & Design System

## 1. Mục tiêu

Đồng bộ FinLux như một ứng dụng tài chính chuyên nghiệp: dễ đọc trên điện thoại nhỏ, hành vi nhất quán, theme chỉ thay style chứ không thay nghiệp vụ.

## 2. Phase UX-0 — Information Architecture

Bottom navigation ưu tiên:
- Home
- Transactions
- Reports
- Profile/More

Module phụ từ Home/More:
- Wallet
- Budget
- Goal
- Debt
- Deal
- Saving Spin
- Reminder
- Settings

Tránh đưa quá nhiều feature ngang hàng gây tải nhận thức.

## 3. Phase UX-1 — Design Tokens

Cấm hard-code presentation color không có exception documented.

Semantic tokens:
- income
- expense
- transfer
- saving
- investment
- debt
- warning/danger/success/info
- surface/elevatedSurface
- primaryText/secondaryText
- divider
- chart palette

Chuẩn hóa spacing, radius, elevation/glass, typography, icon size, touch target >=48dp, content padding, bottom-bar clearance.

## 4. Phase UX-2 — Shared Components

Chuẩn hóa:
- FinluxScreenScaffold
- FinluxTopBar
- FinluxBottomBar
- FinanceKpiCard
- AmountInput
- WalletPicker
- CategoryPicker
- PeriodSelector
- EmptyState/ErrorState/LoadingState
- ConfirmationDialog
- TransactionRow
- SectionHeader
- ChartCard

## 5. Phase UX-3 — Home

- số dư/tài sản dễ đọc;
- Thu kỳ / Chi kỳ / Net Flow gọn;
- quick actions rõ;
- recent transactions;
- budget warning;
- optional Saving Spin entry;
- KPI/label lấy cùng financial period state.

## 6. Phase UX-4 — Transactions

Fix:
- text không va chạm amount/actions;
- note dài max lines hợp lý;
- group header contrast;
- dark/light parity;
- transfer row semantics;
- filters dễ dùng;
- FAB/bottom bar không che content;
- empty/search state.

## 7. Phase UX-5 — Add/Edit Transaction

- Amount là primary focus.
- Type selector rõ Income/Expense/Transfer.
- Category/Wallet picker dễ chạm.
- Validation hiển thị tại chỗ.
- Không save khi invalid.
- Keyboard/insets/landscape hoạt động.
- Receipt là secondary optional action.

## 8. Phase UX-6 — Wallet/Budget

Wallet:
- hero total;
- card balance;
- wallet type;
- transfer flow;
- archived state.

Budget:
- period visible;
- spent/limit/remaining;
- over-budget state;
- copy previous period;
- salary period label.

## 9. Phase UX-7 — Reports

Progressive disclosure:
1. Executive Summary
2. Trend
3. Category
4. Wallet
5. Budget
6. Goal
7. Debt
8. Deal
9. Saving Spin

Không dồn tất cả vào một God Screen.

## 10. Phase UX-8 — Settings/Profile

Nhóm:
- Account
- Financial Preferences
- Security
- Appearance
- Notifications
- Data/Export
- About

Salary Cycle và Saving Spin dùng screen/sheet chuyên biệt.

## 11. Phase UX-9 — Classic / Modern / Prism

Mục tiêu:
```
same state
same use case
same navigation
same feature coverage
different tokens/layout skin
```

Giảm tối đa ba code tree độc lập.

## 12. Accessibility & Device Matrix

Test:
- contrast;
- dynamic font;
- TalkBack semantics;
- contentDescription;
- amount lớn;
- 360dp / 412dp;
- portrait/landscape;
- gesture / 3-button nav;
- light/dark;
- font scale 1.0 / 1.3 / 1.5.

## 13. Acceptance
- [x] Không content bị bottom bar/IME che.
- [x] Theme parity (Dark / Light / Prism / Classic / Modern).
- [x] Không hard-coded presentation colors trái design system (đã refactor TransferMoneyScreen, WalletTransactionsBottomSheet, PrismTransactionsScreen).
- [x] Core screens pass device/manual checklist & 100% Android Unit Tests PASS (34 tasks).
- **Status PR-09**: `[DONE]` (2026-09-07)
