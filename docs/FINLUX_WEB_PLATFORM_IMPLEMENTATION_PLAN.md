# FinLux Web Platform Implementation Plan

> **For agentic workers:** triển khai theo từng phase/task, ưu tiên TDD, commit nhỏ, có checkpoint review sau mỗi phase. Không tự ý đổi business rule tài chính nếu chưa cập nhật đặc tả tương ứng.

**Goal:** Mở rộng FinLux từ Android Native thành nền tảng đa client gồm Android + Web/PWA, dùng chung Firebase backend, dữ liệu, business rules, financial period semantics và security model.

**Architecture:** Web không phải hệ thống tài chính độc lập. Web là một client mới của FinLux Platform, phản chiếu Clean Architecture hiện tại theo hướng Presentation → Application/Use Cases → Domain → Repository → Firebase Infrastructure. Không tạo database thứ hai, không sync chéo Android DB ↔ Web DB. Các nghiệp vụ tài chính quan trọng phải dùng cùng contract/semantics và atomic mutation.

**Tech Stack:** Next.js + TypeScript + React + Tailwind CSS + shadcn/ui + Firebase Auth/Firestore/Storage/FCM + Cloud Functions + TanStack Query + Zustand + React Hook Form + Zod + Vitest + Testing Library + Playwright + GitHub Actions.

**Primary repository:** `khoaiprovip123/FinLux`

**Target branch:** `main`

**Existing Web baseline:** repo đã có `website/index.html` là landing page tĩnh. Kế hoạch này nâng `website/` thành FinLux Web App thay vì tạo thêm thư mục `web/`.

**Source specs cần đọc trước khi code:**
- `README.md`
- `docs/PROJECT_PROFILE.md`
- `docs/BA_SPEC.md`
- `docs/UI_SPEC.md`
- `docs/DATA_SPEC.md`
- `docs/CONTEXT.md`
- `docs/PLAN.md`
- `docs/PENDING_BACKLOG_PLAN.md`
- `docs/FINLUX_SAVING_SPIN_IMPLEMENTATION_PLAN.md`
- `docs/audit-2026-09-fix-roadmap/*`

---

## 1. Baseline hiện tại cần giữ

FinLux hiện tại:
- Android Native.
- Kotlin 2.0.21.
- Jetpack Compose + Material 3.
- Clean Architecture + MVVM.
- Firebase Authentication.
- Cloud Firestore.
- Firebase Storage.
- Firebase Cloud Messaging.
- Cloud Functions Node.js/TypeScript.
- DataStore cho local preferences.
- Firestore Transaction cho mutation tài chính quan trọng.
- Có `FinancialPeriodResolver` và `FinanceClock` cho Month/Salary Cycle.
- Hỗ trợ Income / Expense / Transfer.
- Hỗ trợ Wallet, Budget, Report, Goal, Debt, Deal, Reminder, Saving Spin.
- Có 3 visual styles: Classic / Modern / Prism.
- Có offline/realtime sync trên Android.

### Nguyên tắc tương thích bắt buộc

Web phải:
1. Đọc/ghi cùng dữ liệu Firebase với Android.
2. Không định nghĩa lại financial semantics trái với Android/spec.
3. Không coi internal transfer là Income/Expense.
4. Không làm sai `wallet.balance`.
5. Không tự dùng calendar month khi Salary Cycle đang bật.
6. Không hard-delete dữ liệu tài chính nếu Android đang dùng soft-delete/void/audit semantics.
7. Không tự tạo collection mới nếu chưa map vào `DATA_SPEC.md`.
8. Không lưu secret Firebase Admin/Service Account trong frontend.
9. Không đưa privileged financial mutation về client nếu nghiệp vụ cần Cloud Function/transaction.
10. Không thay đổi `website/` theo hướng làm mất landing page hiện tại.

---

# 2. Kiến trúc đích

```text
                           FINLUX PLATFORM
                                 │
               ┌─────────────────┴─────────────────┐
               │                                   │
         Android Client                       Web / PWA Client
     Kotlin + Compose + MVVM                 Next.js + TypeScript
               │                                   │
               └─────────────────┬─────────────────┘
                                 │
                     Shared Domain Semantics
                                 │
          ┌──────────────────────┼──────────────────────┐
          │                      │                      │
      Firebase Auth          Firestore             Storage
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                         Cloud Functions / FCM
```

## 2.1 Luồng kiến trúc Web

```text
Presentation
  ↓
Application / Use Cases
  ↓
Domain
  ↓
Repository Interfaces
  ↓
Infrastructure / Firebase
```

### Quy tắc dependency
- Domain không import React/Firebase.
- Application gọi Domain + Repository contract.
- Infrastructure implement repository.
- Presentation không gọi Firestore trực tiếp.
- Component không chứa business calculation.
- Report UI không tự tính lại công thức riêng.

---

# 3. Cấu trúc thư mục đích

```text
FinLux/
├── app/                              # Android hiện tại
├── website/                          # FinLux Web/PWA
│   ├── src/
│   │   ├── app/
│   │   │   ├── (public)/
│   │   │   │   └── page.tsx         # Landing page
│   │   │   ├── (auth)/
│   │   │   │   ├── login/
│   │   │   │   ├── register/
│   │   │   │   ├── forgot-password/
│   │   │   │   └── verify-email/
│   │   │   └── app/
│   │   │       ├── dashboard/
│   │   │       ├── transactions/
│   │   │       ├── wallets/
│   │   │       ├── budgets/
│   │   │       ├── reports/
│   │   │       ├── goals/
│   │   │       ├── debts/
│   │   │       ├── deals/
│   │   │       ├── salary-cycle/
│   │   │       ├── saving-spin/
│   │   │       ├── reminders/
│   │   │       └── settings/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   ├── repositories/
│   │   │   ├── services/
│   │   │   └── rules/
│   │   ├── application/
│   │   │   ├── use-cases/
│   │   │   ├── dto/
│   │   │   └── mappers/
│   │   ├── infrastructure/
│   │   │   ├── firebase/
│   │   │   ├── repositories/
│   │   │   ├── storage/
│   │   │   └── notifications/
│   │   ├── presentation/
│   │   │   ├── components/
│   │   │   ├── layouts/
│   │   │   ├── hooks/
│   │   │   └── stores/
│   │   ├── core/
│   │   │   ├── finance/
│   │   │   ├── errors/
│   │   │   ├── validation/
│   │   │   ├── constants/
│   │   │   └── utils/
│   │   └── shared/
│   ├── public/
│   ├── tests/
│   ├── package.json
│   ├── tsconfig.json
│   ├── next.config.ts
│   └── .env.example
├── functions/
├── firestore.rules
├── firestore.indexes.json
├── storage.rules
├── docs/
└── .github/workflows/
```

---

# 4. Shared contracts bắt buộc

## 4.1 Transaction semantics

Các loại giao dịch chuẩn:
- `INCOME`
- `EXPENSE`
- `TRANSFER`

Bất biến:
```text
INCOME:
  wallet.balance += amount

EXPENSE:
  wallet.balance -= amount

TRANSFER:
  source.balance -= amount
  destination.balance += amount
```

Internal transfer:
- Không tính vào total Income.
- Không tính vào total Expense.
- Phải truy nguyên được source/destination.
- Mutation phải atomic.

## 4.2 Financial period

Web phải triển khai tương đương semantics của:
- `FinanceClock`
- `FinancialPeriodResolver`

Mọi module sau phải dùng cùng period:
- Dashboard.
- History.
- Income.
- Expense.
- Reports.
- Budget.
- Saving Spin report.
- Salary Cycle.
- Comparison previous period.

Query window chuẩn:
```text
[start, endExclusive)
```

Không được:
```text
if salaryCycleEnabled => chỉ đổi label "Kỳ lương"
nhưng vẫn query ngày 01 -> cuối tháng
```

## 4.3 Balance invariant

```text
Opening Balance
+ Income
+ Transfer In
- Expense
- Transfer Out
= Closing Balance
```

Mọi báo cáo phải drill-down được tới ledger/giao dịch tạo ra số liệu.

## 4.4 Saving Spin

Giữ semantics hiện tại:
- Ledger riêng.
- Kết quả spin phải persist.
- Không cho reroll gian lận.
- `SKIPPED` giữ audit data nhưng không tính tiết kiệm.
- Không tự làm thay đổi Income/Expense/Net Worth/wallet.balance nếu specification hiện hành vẫn quy định như vậy.
- Nếu sau này kết nối Saving Spin → Wallet transaction thì phải tạo migration/spec riêng.

---

# 5. Phase roadmap

## PHASE P0 — Architecture & Contract Freeze

**Mục tiêu:** khóa contract trước khi code Web.

### Tasks
- [ ] P0.1 Audit `docs/BA_SPEC.md`.
- [ ] P0.2 Audit `docs/DATA_SPEC.md`.
- [ ] P0.3 Audit Firestore collections/subcollections thực tế.
- [ ] P0.4 Audit `firestore.rules`.
- [ ] P0.5 Audit `storage.rules`.
- [ ] P0.6 Audit Cloud Functions.
- [ ] P0.7 Map Android Domain Model → TypeScript Domain Model.
- [ ] P0.8 Map Android Repository Interfaces → Web Repository Interfaces.
- [ ] P0.9 Map `FinanceClock` / `FinancialPeriodResolver`.
- [ ] P0.10 Khóa transaction invariant.
- [ ] P0.11 Khóa report formulas.
- [ ] P0.12 Khóa Salary Cycle semantics.
- [ ] P0.13 Khóa Saving Spin semantics.
- [ ] P0.14 Lập migration matrix cho mọi field khác nhau giữa Android/Web.

### Deliverables
- `docs/web/WEB_ARCHITECTURE.md`
- `docs/web/WEB_DATA_CONTRACT.md`
- `docs/web/WEB_BUSINESS_RULES.md`
- `docs/web/WEB_SECURITY_MODEL.md`
- `docs/web/WEB_TEST_STRATEGY.md`

### Exit criteria
- Không còn business rule quan trọng chỉ tồn tại trong UI Android.
- Mọi field Web cần đều map được tới source of truth.
- Không có collection phụ riêng cho Web chỉ để tránh hiểu schema hiện tại.

---

## PHASE P1 — Web Foundation

**Mục tiêu:** chuyển `website/` từ static HTML thành Next.js app mà vẫn giữ landing page.

### Tasks
- [ ] P1.1 Khởi tạo Next.js + TypeScript trong `website/`.
- [ ] P1.2 Chuyển landing page hiện tại thành route public `/`.
- [ ] P1.3 Thiết lập Tailwind.
- [ ] P1.4 Thiết lập shadcn/ui.
- [ ] P1.5 Thiết lập ESLint + Prettier.
- [ ] P1.6 Thiết lập Vitest + Testing Library.
- [ ] P1.7 Thiết lập Playwright.
- [ ] P1.8 Thiết lập env schema.
- [ ] P1.9 Tạo Firebase client adapter.
- [ ] P1.10 Tạo global error boundary.
- [ ] P1.11 Tạo logger abstraction.
- [ ] P1.12 Tạo app shell rỗng tại `/app`.

### Environment
```text
development
staging
production
```

### Exit criteria
- Landing page cũ không mất.
- `npm run lint` pass.
- `npm run typecheck` pass.
- `npm test` pass.
- `npm run build` pass.

---

## PHASE P2 — Authentication & User Session

### Routes
```text
/login
/register
/forgot-password
/verify-email
/app
```

### Tasks
- [ ] P2.1 Firebase Auth provider.
- [ ] P2.2 Session observer.
- [ ] P2.3 Protected route.
- [ ] P2.4 Login.
- [ ] P2.5 Register.
- [ ] P2.6 Forgot password.
- [ ] P2.7 Verify email.
- [ ] P2.8 Logout.
- [ ] P2.9 Load user profile sau auth.
- [ ] P2.10 Load financial preferences sau auth.
- [ ] P2.11 Handle deleted/disabled account.
- [ ] P2.12 Auth E2E tests.

### Exit criteria
- User A không thấy dữ liệu User B.
- Refresh browser không làm mất session hợp lệ.
- Unauthenticated user không vào được `/app/*`.

---

## PHASE P3 — Design System & Responsive App Shell

### Mục tiêu
Giữ DNA FinLux nhưng tối ưu desktop/tablet, không copy pixel UI Android.

### Components
- `FinluxAppShell`
- `FinluxSidebar`
- `FinluxTopBar`
- `FinluxPageHeader`
- `FinluxCard`
- `MoneyText`
- `MetricCard`
- `PeriodSelector`
- `EmptyState`
- `LoadingState`
- `ErrorState`
- `ConfirmDialog`

### Breakpoints
- Mobile Web.
- Tablet.
- Laptop.
- Desktop.
- Wide desktop.

### Theme
- Classic.
- Modern.
- Prism.
- Light.
- Dark.
- System.

### Exit criteria
- Không duplicate business logic theo theme.
- Theme chỉ thay presentation.
- Sidebar desktop + bottom navigation mobile hoạt động cùng route model.
- Đạt accessibility tối thiểu cho keyboard/focus/contrast.

---

## PHASE P4 — Wallet & Account Management

### Features
- [ ] Danh sách ví.
- [ ] Tổng số dư.
- [ ] Tạo ví.
- [ ] Sửa ví.
- [ ] Archive ví.
- [ ] Wallet detail.
- [ ] Wallet statement.
- [ ] Opening balance.
- [ ] Transfer editor.
- [ ] Reconciliation view.

### Critical rules
- Không xóa ví đang còn transaction nếu spec không cho phép.
- Transfer phải atomic.
- Không tính transfer vào Income/Expense.
- Không mutate balance bằng nhiều client write rời.

### Exit criteria
- Balance Android/Web nhất quán.
- Transfer rollback an toàn khi mutation fail.
- User không thể thao tác ví không thuộc UID của mình.

---

## PHASE P5 — Transaction Engine

**Đây là phase core và phải được review kỹ nhất.**

### Routes
```text
/app/transactions
/app/transactions/new
/app/transactions/[id]
```

### Features
- [ ] Income.
- [ ] Expense.
- [ ] Transfer.
- [ ] Add transaction.
- [ ] Edit transaction.
- [ ] Void/Delete theo semantics hiện tại.
- [ ] Category selector.
- [ ] Wallet selector.
- [ ] Date/time.
- [ ] Note/payee/tag nếu schema hỗ trợ.
- [ ] Receipt upload nếu schema hỗ trợ.
- [ ] Search/filter/sort.
- [ ] Pagination/infinite query.
- [ ] Keyboard-friendly quick entry.

### Data integrity
Mutation ảnh hưởng nhiều document phải dùng:
- Firestore Transaction; hoặc
- Cloud Function.

Không làm:
```text
write transaction
then update wallet A
then update wallet B
```
bằng ba request độc lập.

### Exit criteria
- Không double submit.
- Có idempotency strategy cho operation nhạy cảm.
- Edit/Delete hoàn tác đúng balance delta.
- Cross-device verification Android ↔ Web pass.

---

## PHASE P6 — Dashboard

### Desktop layout
```text
┌──────────────────────────────────────────────────┐
│ Total Assets | Income | Expense | Net Cashflow   │
├────────────────────────┬─────────────────────────┤
│ Cashflow Trend         │ Expense Breakdown       │
├────────────────────────┼─────────────────────────┤
│ Wallet Balances        │ Budget Progress         │
├────────────────────────┴─────────────────────────┤
│ Recent Transactions / Alerts                     │
└──────────────────────────────────────────────────┘
```

### Period filter
- Today.
- 7 days.
- 30 days.
- Month.
- Quarter.
- Year.
- Salary Cycle.
- Custom range.

### Exit criteria
- KPI và chart dùng cùng report/application layer.
- Không có công thức riêng trong component.
- Salary Cycle đổi thật query window, không chỉ đổi label.

---

## PHASE P7 — Reporting 2.0 Web

### Reports
- Financial Overview.
- Income Analysis.
- Expense Analysis.
- Cashflow.
- Net Worth.
- Wallet Analysis.
- Category Analysis.
- Budget Performance.
- Salary Cycle Comparison.
- Goal Progress.
- Debt Report.
- Deal Report.
- Saving Spin Report.

### Daily statement
```text
Previous Closing
      ↓
Opening Balance
      ↓
Income
Expense
Transfer In/Out
      ↓
Closing Balance
```

### Drill-down
```text
Executive Summary
   ↓
Wallet
   ↓
Category
   ↓
Transaction
```

### Exit criteria
- Closing ngày N-1 khớp Opening ngày N.
- Export và UI dùng chung Report DTO/Use Case.
- Internal transfer không phình income/expense.
- Net Worth formula dùng source duy nhất.

---

## PHASE P8 — Salary Cycle / Financial Period

### Features
- [ ] Read salary cycle config.
- [ ] Edit salary cycle config nếu business cho phép.
- [ ] Current cycle.
- [ ] Previous equivalent cycle.
- [ ] Month mode.
- [ ] Salary Cycle mode.
- [ ] Custom range.
- [ ] Period comparison.

### Modules bắt buộc dùng resolver
- Dashboard.
- Transactions.
- Income.
- Expense.
- Reports.
- Budget.
- Saving Spin reports.

### Exit criteria
- Không còn màn hình nào tự suy luận đầu/cuối tháng.
- Test boundary quanh 28/29/30/31 và February.
- Test payday cuối tháng.
- Test timezone.

---

## PHASE P9 — Budget

### Features
- [ ] Budget list.
- [ ] Create/Edit.
- [ ] Category budget.
- [ ] Period-aware budget.
- [ ] 80% warning.
- [ ] 100% warning.
- [ ] Used/remaining.
- [ ] Drill-down đến transaction.
- [ ] Previous period comparison.

### Exit criteria
- Budget period khớp `FinancialPeriodResolver`.
- `spentAmount` không drift khỏi ledger.
- Cross-device consistency pass.

---

## PHASE P10 — Goals / Savings

### Features
- [ ] Goal list.
- [ ] Goal detail.
- [ ] Create/Edit goal.
- [ ] Contribution history.
- [ ] Progress.
- [ ] Target date.
- [ ] Source wallet mapping nếu hiện hành.
- [ ] Goal report.

### Rule
Contribution không được tự biến thành income mới.

---

## PHASE P11 — Debt & Credit

### Features
- [ ] Debt account list.
- [ ] Outstanding.
- [ ] Payment history.
- [ ] Principal/interest semantics.
- [ ] Due date.
- [ ] Payoff view.
- [ ] Snowball/Avalanche view nếu contract hiện tại hỗ trợ.
- [ ] Debt report.

### Exit criteria
- Payment mutation không tạo tiền giả.
- Net Worth trừ liability đúng một lần.

---

## PHASE P12 — Deals / Personal Capital

### Features
- [ ] Deal list.
- [ ] Active/closed.
- [ ] Capital deployed.
- [ ] Principal recovered.
- [ ] Gain/Loss.
- [ ] ROI.
- [ ] Deal detail.
- [ ] Deal report.

### Critical
Không tái tạo ví ảo kiểu `DEAL_SETTLEMENT`.
Phải bám contract mới nhất trong backlog/spec.

---

## PHASE P13 — Saving Spin Web

### Routes
```text
/app/saving-spin
/app/saving-spin/settings
/app/saving-spin/history
/app/saving-spin/reports
```

### Features
- [ ] Config mệnh giá.
- [ ] Min/max.
- [ ] Step.
- [ ] Wheel items.
- [ ] Spin.
- [ ] Persist result.
- [ ] Confirm/skip.
- [ ] History.
- [ ] Streak.
- [ ] Report.
- [ ] Salary-cycle-aware schedule/report.

### Exit criteria
- Không reroll sau khi result đã persist.
- Reload trang giữ đúng session.
- SKIPPED không tính vào saved total.
- Web/Android cùng thấy cùng ledger.

---

## PHASE P14 — Reminder & Notification

### Types
- Recurring payment.
- Budget threshold.
- Goal milestone.
- Salary event.
- Debt due.
- Saving Spin.
- Financial summary.

### Web
- Firebase Cloud Messaging Web nếu phù hợp.
- Browser Notification chỉ sau explicit permission.
- In-app notification center.
- Deep-link vào đúng feature.

### Exit criteria
- Không spam duplicate notification.
- Notification read state sync.
- Permission denied không làm hỏng app.

---

## PHASE P15 — Settings

### Shared settings
- Profile.
- Currency.
- Timezone.
- Salary Cycle.
- Financial period mode.
- Notification preferences.
- Theme preference nếu muốn sync.
- Privacy/security.

### Web-only settings
- Sidebar collapsed.
- Table density.
- Desktop-specific preferences.

### Rule
Không đưa device-only setting vào shared financial profile nếu Android không cần.

---

## PHASE P16 — Global Search / Journal

### Search dimensions
- Date.
- Wallet.
- Transaction type.
- Category.
- Amount.
- Note.
- Payee.
- Status.

### Desktop capabilities
- Advanced filter.
- Multi-filter.
- Saved filters nếu cần.
- Keyboard navigation.
- Bulk selection chỉ cho operation an toàn.
- Export filtered dataset.

---

## PHASE P17 — Security Hardening

### Firebase
- [ ] UID isolation.
- [ ] Rule tests cho read/write.
- [ ] Storage ownership.
- [ ] Receipt access.
- [ ] Avatar access.
- [ ] Cloud Functions auth.
- [ ] App Check nếu áp dụng.
- [ ] Rate limit privileged operation.

### Web
- [ ] CSP.
- [ ] XSS review.
- [ ] CSRF review theo auth strategy.
- [ ] Secure headers.
- [ ] Không log token.
- [ ] Không expose admin credential.
- [ ] Validate input bằng Zod.
- [ ] File upload MIME/size validation.

### Exit criteria
User B không thể:
- read;
- write;
- query;
- download private file;
dữ liệu của User A.

---

## PHASE P18 — Data Integrity & Concurrency

### Operations bắt buộc audit
- Add transaction.
- Edit transaction.
- Delete/Void transaction.
- Transfer.
- Goal contribution.
- Debt payment.
- Deal settlement.
- Saving mutation nếu sau này kết nối wallet.
- Salary rollover.
- Budget aggregate update.

### Required patterns
- Firestore Transaction.
- Batch write khi phù hợp.
- Cloud Function cho privileged/server-controlled flow.
- Idempotency key.
- Retry có kiểm soát.
- Audit log khi cần.

### Exit criteria
- Multi-tab concurrent update test pass.
- Android + Web concurrent mutation test pass.
- Network retry không tạo duplicate transaction.

---

## PHASE P19 — Offline / PWA

### Features
- [ ] Installable PWA.
- [ ] Manifest.
- [ ] Service Worker.
- [ ] Offline shell.
- [ ] Cache static assets.
- [ ] Update prompt.
- [ ] Network state indicator.

### Financial mutation policy
Không tự queue mọi mutation tài chính offline nếu chưa chứng minh được:
- idempotency;
- conflict handling;
- balance consistency.

Ưu tiên:
1. Offline read/cache.
2. Draft local entry.
3. Sync có kiểm soát.
4. Financial mutation offline chỉ bật khi tests đủ mạnh.

---

## PHASE P20 — Testing Strategy

### Unit tests
Bắt buộc cho:
- Money.
- FinanceClock.
- FinancialPeriodResolver.
- Salary Cycle boundary.
- Transaction delta.
- Transfer semantics.
- Report calculation.
- Net Worth.
- Budget utilization.
- Saving Spin state.

### Integration tests
- Firebase Emulator.
- Repository.
- Firestore Rules.
- Storage Rules.
- Cloud Functions.

### E2E
```text
Register
→ Login
→ Create wallet
→ Add income
→ Add expense
→ Transfer
→ Open report
→ Verify closing balance
→ Switch Salary Cycle
→ Verify report period
→ Logout
```

### Cross-client contract test
```text
Android creates transaction
→ Web reads same result
→ balances/reports match

Web creates transaction
→ Android reads same result
→ balances/reports match
```

---

## PHASE P21 — CI/CD

### Branch model
```text
main
develop
feat/web-foundation
feat/web-auth
feat/web-wallet
feat/web-transactions
feat/web-dashboard
feat/web-reports
...
```

> Trong trường hợp chủ repo yêu cầu làm trực tiếp trên `main`, chỉ commit tài liệu/plan trực tiếp. Code feature lớn vẫn nên triển khai qua feature branch + PR để dễ review và rollback.

### Pipeline Web
```text
Push / PR
  ↓
Install
  ↓
Lint
  ↓
Typecheck
  ↓
Unit Tests
  ↓
Firebase Rule Tests
  ↓
Build
  ↓
Playwright E2E
  ↓
Preview Deploy
  ↓
Production Deploy
```

### Environments
- Development.
- Staging.
- Production.

### Secrets
Lưu ở GitHub/Firebase deployment environment, không commit vào repo.

---

# 6. Deployment architecture

## Khuyến nghị

### Public
```text
https://finlux.is-a.dev/
```

Landing page:
```text
/
```

Web App:
```text
/app
/app/dashboard
/app/transactions
...
```

Auth:
```text
/login
/register
```

### Hosting
Ưu tiên:
1. Firebase App Hosting nếu Next.js SSR/server capability cần dùng.
2. Firebase Hosting nếu dùng static/client-heavy architecture phù hợp.
3. Không khóa cứng vào GitHub Pages khi Web App cần Firebase Auth routing, middleware hoặc server feature.

---

# 7. UI/UX strategy

## Mobile Android
Ưu tiên:
- one-hand;
- quick add;
- bottom navigation;
- touch;
- compact workflow.

## Web Desktop
Ưu tiên:
- data density;
- sidebar;
- table;
- advanced filters;
- multi-column;
- drill-down;
- keyboard;
- reporting;
- comparison.

### Không làm
```text
Android Screen
→ phóng to
→ Desktop Screen
```

### Làm
```text
Shared FinLux Domain
       ↓
 ┌─────┴─────┐
Mobile UX   Desktop UX
```

---

# 8. Priority matrix

| Phase | Nội dung | Priority |
|---|---|---|
| P0 | Architecture & Contract Freeze | P0 Critical |
| P1 | Web Foundation | P0 Critical |
| P2 | Authentication | P0 Critical |
| P3 | Design System / Shell | P0 Critical |
| P4 | Wallet | P0 Critical |
| P5 | Transaction Engine | P0 Critical |
| P6 | Dashboard | P1 High |
| P7 | Reporting 2.0 | P0 Critical |
| P8 | Salary Cycle | P0 Critical |
| P9 | Budget | P1 High |
| P10 | Goals/Savings | P1 High |
| P11 | Debt | P1 High |
| P12 | Deals | P1 High |
| P13 | Saving Spin | P1 High |
| P14 | Notification | P2 Medium |
| P15 | Settings | P1 High |
| P16 | Search/Journal | P1 High |
| P17 | Security | P0 Critical |
| P18 | Data Integrity | P0 Critical |
| P19 | PWA/Offline | P2 Medium |
| P20 | Testing | P0 Critical |
| P21 | CI/CD/Production | P0 Critical |

---

# 9. Milestone release proposal

## Web Alpha
Bao gồm:
- P0 → P5.
- Auth.
- Wallet.
- Transaction.
- Basic responsive shell.

Mục tiêu:
- đăng nhập bằng cùng tài khoản;
- thấy cùng ví;
- tạo giao dịch Web;
- Android nhận đúng giao dịch/số dư.

## Web Beta
Bao gồm:
- P6 → P10.
- Dashboard.
- Reports.
- Salary Cycle.
- Budget.
- Goals.

Mục tiêu:
- dùng được FinLux Web hằng ngày;
- số liệu Android/Web đồng nhất.

## Web RC
Bao gồm:
- Debt.
- Deal.
- Saving Spin.
- Notification.
- Search.
- Security hardening.
- Data integrity.
- Full tests.

## Web 1.0
Bao gồm:
- PWA.
- CI/CD production.
- Domain `finlux.is-a.dev`.
- Production monitoring.
- Backup/recovery runbook.
- Release checklist.

---

# 10. Definition of Done chung

Một phase chỉ được xem là hoàn thành khi:
- [ ] Business rule có source/spec rõ ràng.
- [ ] Không phá Android compatibility.
- [ ] Lint pass.
- [ ] Typecheck pass.
- [ ] Unit test pass.
- [ ] Integration test liên quan pass.
- [ ] Security test liên quan pass.
- [ ] Responsive check pass.
- [ ] Empty/loading/error state hoàn chỉnh.
- [ ] Không hardcode secret.
- [ ] Không duplicate financial formula trong UI.
- [ ] Docs cập nhật.
- [ ] Có commit rõ ràng.
- [ ] Có review trước khi merge feature lớn.

---

# 11. Quy tắc dành cho AI coding agent

AI/dev khi thực hiện plan này phải:
1. Đọc file plan này trước.
2. Đọc các spec có liên quan trước khi sửa code.
3. Search implementation hiện tại trước khi tạo class/type mới.
4. Không đoán Firestore field.
5. Không đổi schema ngầm.
6. Không tạo workaround làm sai financial invariant.
7. Viết test failing trước cho business logic quan trọng.
8. Chạy test sau mỗi task.
9. Commit nhỏ theo phase.
10. Nếu spec và code mâu thuẫn, dừng thay đổi business logic và ghi rõ conflict.
11. Không sửa Android chỉ để Web dễ implement nếu chưa có lý do kiến trúc rõ ràng.
12. Ưu tiên shared semantics qua docs/contracts/functions hơn copy logic thủ công giữa Kotlin và TypeScript.

---

# 12. Thứ tự thực hiện thực tế đề xuất

```text
P0 Contract Freeze
      ↓
P1 Web Foundation
      ↓
P2 Auth
      ↓
P3 Design System
      ↓
P4 Wallet
      ↓
P5 Transaction
      ↓
CROSS-CLIENT FINANCIAL TEST
      ↓
P6 Dashboard
      ↓
P7 Reports
      ↓
P8 Salary Cycle
      ↓
P9 Budget
      ↓
P10 Goals
      ↓
P11 Debt
      ↓
P12 Deals
      ↓
P13 Saving Spin
      ↓
P14 Notifications
      ↓
P15 Settings
      ↓
P16 Search
      ↓
P17 Security
      ↓
P18 Concurrency/Data Integrity
      ↓
P19 PWA
      ↓
P20 Full Regression
      ↓
P21 Production Release
```

---

# 13. Architecture decision chốt

**ADR-WEB-001**

FinLux Web sẽ:
- dùng `website/` hiện hữu;
- Next.js + TypeScript;
- Firebase là backend chung;
- không tạo SQL/MySQL/PostgreSQL backend thứ hai ở giai đoạn này;
- không duplicate user financial database;
- không sync database giữa mobile/web;
- dùng cùng UID/data/security model;
- giữ landing page tại root;
- đặt authenticated product dưới `/app/*`;
- dùng chung business semantics thông qua specification + shared backend contracts;
- không cố share trực tiếp source Kotlin sang TypeScript;
- ưu tiên Cloud Functions/Firestore Transaction cho financial mutation nhạy cảm.

Lý do:
- ít phá Android hiện tại;
- giảm rủi ro lệch số dư;
- giảm duplicate data;
- dễ mở rộng iOS/Desktop sau này;
- phù hợp kiến trúc Clean hiện có;
- tận dụng Firebase realtime/offline ecosystem;
- giữ Web và Android độc lập về UX nhưng thống nhất nghiệp vụ.

---

# 14. Kết quả cuối cùng mong muốn

```text
FinLux Platform
│
├── Android App
├── Web App
├── PWA
│
├── Shared Financial Semantics
├── Shared Firebase Data
├── Shared Security Model
├── Shared Cloud Functions
│
└── Documentation / Contracts / Tests
```

Người dùng có thể:
- tạo giao dịch trên Android và xem ngay trên Web;
- tạo giao dịch Web và xem lại trên Android;
- xem cùng Wallet Balance;
- xem cùng Salary Cycle;
- xem cùng Budget;
- xem cùng Reports;
- dùng cùng Saving Spin data;
- chuyển thiết bị mà không sai số liệu.

**Nguyên tắc quan trọng nhất:** Web là một client mới của FinLux Platform, không phải một FinLux thứ hai.
