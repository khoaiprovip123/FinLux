# FINLUX — PRODUCT MASTER DESCRIPTION 2026

> Source of truth: repository `khoaiprovip123/FinLux` at Android version **1.22.0** (versionCode 165) plus the approved product direction accumulated through the project.
>
> Purpose of this document: provide a single, implementation-ready description of FinLux for product, BA, UI/UX, engineering, documentation, and public website content.

---

## 1. Product identity

**FinLux** is an open-source Android personal finance application focused on helping an individual record, understand, and control their money in one consistent financial model.

FinLux is not designed as a generic accounting ERP or a bank replacement. It is a personal financial operating layer that connects daily transactions, wallets, budgets, salary cycles, savings, debts, financial deals, reminders, and analytical reports into one coherent user experience.

### Product principle

```text
RECORD → STRUCTURE → UNDERSTAND → PLAN → FORECAST → AI
```

The current product concentrates on the foundation from **Record** through **Plan/Understand**, while keeping the architecture ready for later forecasting and AI features.

---

## 2. Core user

Primary actor:

- Individual user managing personal money.
- Uses one account across one or more Android devices.
- Needs simple daily input but wants deeper financial visibility when required.
- May have multiple cash/bank/e-wallet/credit/investment-like accounts.
- May be paid on a salary cycle that does not align with calendar month boundaries.

Current product does not position itself as a shared-family or multi-user accounting system.

---

## 3. Product goals

FinLux must let the user answer these questions quickly and correctly:

1. How much money do I actually have now?
2. Where is the money located?
3. How much did I earn and spend in the current financial period?
4. How much did my cash position increase or decrease?
5. What changed compared with the previous period?
6. Which categories are driving spending?
7. Am I staying within budget?
8. What are my current savings goals?
9. How much debt remains and what is the payoff trajectory?
10. What capital is tied up in lending/investment-style deals?
11. What was my opening balance and closing balance for a selected day or period?
12. What recurring commitments are approaching?
13. What amount have I saved through the Saving Spin mini game?

Accuracy is more important than decorative charts. Every major KPI should be traceable to a consistent financial event/ledger model.

---

## 4. Current technology baseline

### Android

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Coroutines + Flow / StateFlow
- Hilt
- DataStore

### Architecture

- Clean Architecture
- MVVM
- Presentation / Domain / Data layers
- Repository abstraction
- Reusable design system and shared finance semantics

### Backend

- Firebase Authentication
- Cloud Firestore
- Firebase Storage
- Firebase Cloud Messaging
- Cloud Functions where server-side logic is required
- Firestore offline persistence / realtime listeners

### Quality

- JUnit
- MockK / Turbine where applicable
- Compose UI tests
- CI/CD workflows
- GitHub release workflow
- Excel/PDF export support

---

## 5. Financial integrity model

FinLux treats financial correctness as a P0 requirement.

Critical invariant:

```text
Opening Balance
+ Income
+ Transfer In
- Expense
- Transfer Out
= Closing Balance
```

More complex business events such as goals, debts, deals and saving activities must not silently distort Income/Expense or Net Worth semantics.

### Required consistency

- Wallet balance changes must be atomic where needed.
- Internal transfers must not be counted as lifestyle income or expense.
- Edit/delete operations must reverse/recalculate the correct financial effect.
- Reporting and Home KPIs must use the same financial period and the same formulas.
- Salary Cycle must be treated as a real period, not only as a renamed month label.
- Export must consume the same report DTO/use-case semantics as the in-app report.

---

## 6. Functional domains

### 6.1 Authentication & user profile

Capabilities:

- Email/password sign-in.
- Google sign-in where configured.
- Session management.
- Profile details.
- Avatar upload/crop via Firebase Storage.
- Sign-out and account-oriented settings.

User flow:

```text
Launch
→ Splash / session check
→ Login or authenticated app
→ Load user finance configuration
→ Load financial period
→ Home
```

---

### 6.2 Wallet & account management

FinLux supports multiple money containers, for example:

- Cash.
- Bank account.
- E-wallet.
- Credit-card-like account.
- Investment-related account where supported by the model.

Each wallet should expose:

- Name.
- Institution / type.
- Current balance.
- Currency semantics used by the app.
- Status.
- Related transaction history.
- Transfer actions.

Internal transfer:

```text
Source Wallet
→ Enter amount
→ Select Destination Wallet
→ Validate
→ Atomic transfer
→ Ledger entries / transaction pair
→ Update both balances
→ Reports exclude transfer from true income/expense
```

---

### 6.3 Daily transactions

Core transaction types:

- Income.
- Expense.
- Transfer.

Transaction information can include:

- Amount.
- Date/time.
- Category.
- Wallet.
- Note.
- Optional receipt/attachment.
- Related domain references where required.

Core flow:

```text
Home / Transactions
→ Add
→ Choose Income / Expense / Transfer
→ Enter Amount
→ Choose Category
→ Choose Wallet
→ Date / Note / Optional Receipt
→ Validate
→ Save
→ Update financial state
→ Refresh Home + History + Reports
```

Edit/delete must preserve ledger correctness and wallet balances.

---

## 7. Categories

Category management supports:

- Income categories.
- Expense categories.
- Default categories.
- User-created categories.
- Icon.
- Semantic color.
- Edit/delete rules.
- Filtering by transaction type.

Category semantics feed:

- Transaction entry.
- Budgeting.
- Reports.
- Search/filter.
- Category rankings.

---

## 8. Financial period & Salary Cycle

FinLux supports a unified financial period concept.

Supported period semantics include:

- Today.
- Yesterday.
- Specific day.
- Week.
- Last 7 days.
- Month.
- Salary Cycle.
- Quarter.
- Year.
- Custom range.

Queries use a canonical half-open time window:

```text
[start, endExclusive)
```

### Salary Cycle

Salary Cycle exists because many users think in terms of payday-to-payday rather than calendar month.

Requirements:

- Configurable payday.
- Defined rollover behavior.
- Current salary period resolver.
- Previous salary period resolver.
- Same period state used by Home, History, Income, Expense and Reports.
- Labels and query boundaries must match.

Example:

```text
Salary Cycle enabled
→ User opens Home
→ Resolver determines current cycle
→ Home KPI uses cycle boundaries
→ User opens Reports
→ Reports receives same cycle
→ Comparison uses previous equivalent cycle
```

---

## 9. Home / Financial dashboard

Home is the main financial command surface, not a chart dumping page.

### Current direction

Header:

- Avatar.
- Greeting / user name.
- Notification entry.

Primary financial overview:

- Horizontal pager with four finance perspectives:
  1. Balance.
  2. Income.
  3. Expense.
  4. Net Cash Flow.

Behavior:

- Swipeable.
- Auto-advance after inactivity where configured.
- User gesture resets auto-advance timer.
- Visual identity per metric using semantic gradients/vector decoration.
- Amount remains the main hierarchy.

Secondary content:

- Quick actions.
- Recent transactions.
- Budget status/warnings.
- Optional Saving Spin launcher.
- Financial period context.

Home values must be derived from the same financial semantics used by reports.

---

## 10. Income & Expense views

Dedicated analysis views provide:

- Period summary.
- Category breakdown.
- Trend by day/time.
- Largest transaction.
- Average per day.
- Previous-period comparison.
- Drill-down to transactions.

Important rules:

- Transfer does not inflate income/expense.
- Deal principal flows do not distort daily lifestyle spending.
- Salary Cycle remains consistent when enabled.

---

## 11. Budget management

Budget allows spending control by category and period.

Capabilities:

- Create/update/delete budget.
- Category-specific limit.
- Period-aware usage.
- Spent.
- Remaining.
- Usage percentage.
- Over-budget state.
- Warning thresholds, including near-limit and exceeded states.
- Previous-period comparison.
- Salary-cycle awareness.

Flow:

```text
Budget
→ Select/Create Category Budget
→ Set Limit
→ Select period semantics
→ Save
→ Transactions update usage
→ Warning when threshold reached
→ Drill down to category transactions
```

---

## 12. Reports & analytics

Reports are structured as progressive disclosure instead of one overloaded screen.

### Layer 1 — Executive summary

- Opening balance.
- Closing balance.
- Total income.
- Total expense.
- Net cash flow.
- Comparison with previous period.

### Layer 2 — Trend

- Time-series trend.
- Daily movement.
- Inflow/outflow context.

### Layer 3 — Category analysis

- Spending category ranking.
- Share of total.
- Trend/drill-down.

### Layer 4 — Wallet statement

For each wallet:

- Opening balance.
- Closing/current balance.
- Income.
- Expense.
- Transfer in.
- Transfer out.
- Net change.
- Transaction count.
- Category breakdown.
- Statement transaction list.

### Layer 5+ — Domain reports

- Budget.
- Goal.
- Debt.
- Deal.
- Saving Spin.

### Daily statement

```text
Opening Balance
+ Cash In
- Cash Out
= Closing Balance
```

The closing balance of day N should reconcile to the opening balance of day N+1 within the same wallet scope unless an explicit reconciliation adjustment exists.

---

## 13. Financial goals

Goal management is used for planned saving objectives.

Typical information:

- Goal name.
- Target amount.
- Current contributed amount.
- Progress.
- Target date where applicable.
- Funding wallet / financial source semantics.

Goal contribution should not be misclassified as new income if it is only a reclassification of money already owned by the user.

User flow:

```text
Goals
→ Create Goal
→ Set target
→ Contribute
→ Select source
→ Confirm
→ Update goal progress
→ Preserve correct wallet/net-worth accounting
```

---

## 14. Debt management

Debt is a first-class finance domain.

Capabilities visible in the codebase/direction:

- Debt account.
- Debt type.
- Outstanding amount.
- Payment flow.
- Payment history.
- Payoff strategy.
- Payoff plan.
- Burndown visualization.
- Cashflow analysis/advisor.
- Filtering by debt.
- Principal/interest semantics.

Report direction:

- Total outstanding.
- Original debt.
- Principal paid.
- Interest paid.
- Payoff forecast.
- Debt by type.
- Payment timeline.
- Snowball/Avalanche comparison.

Debt payments must be linked to wallet movements and cannot update balances inconsistently.

---

## 15. Deals / capital tracking

FinLux contains a domain for personal financial deals such as lending or investment-like capital placements.

Core semantics include:

- Capital outlay.
- Principal recovery.
- Outstanding principal.
- Realized gain/loss.
- ROI.
- Active/completed status.
- Investment vs lending behavior where supported.

The design specifically avoids fake settlement wallets that hide accounting effects.

Deal principal and realized return must be separated so lifestyle cashflow and net worth remain meaningful.

---

## 16. Saving Spin mini game

Saving Spin is an opt-in gamified savings feature.

### Objective

Encourage the user to save small/medium amounts through a configurable spin challenge.

### Configuration

- Enable/disable.
- Minimum amount.
- Maximum amount.
- Step denomination: typically 5,000 or 10,000 VND according to the current spec.
- Slot count.
- Frequency.
- Default reminder at 09:00 or user-selected time.
- Allow skip.
- Snooze.

### State flow

```text
OFF
READY
SPUN_PENDING
SNOOZED
COMPLETED
```

### Important rules

- No infinite denomination enumeration.
- Large maximum amount must not cause memory problems.
- One challenge/session cannot be rerolled for a better amount.
- Result persists after app kill/reopen.
- A spin is not counted as saved until the user confirms the destination.
- Destination can include cash-style saving or bank-transfer saving.
- Saving Spin uses its own saving ledger semantics and must not be recorded as fake Income.

### Reporting

- Total saved.
- Streak.
- Completion rate.
- Skipped count.
- Destination breakdown.
- Trend.
- Salary-cycle filter where relevant.

---

## 17. Reminders & notifications

FinLux supports recurring reminders and in-app notifications.

Use cases:

- Recurring bill reminder.
- Reminder to record a transaction.
- Budget warning.
- Saving Spin reminder.
- Quick action from notification where supported.
- Deep link to relevant screen.

Scheduling is designed around the existing reminder scheduler pattern.

---

## 18. Search, filters and transaction history

Transaction history should support:

- Grouped display by day.
- Search.
- Type filter.
- Category filter.
- Wallet filter.
- Period filter.
- Salary Cycle-aware period.
- Clear visual distinction for transfer.
- Long note truncation without breaking layout.
- Drill-down/edit/delete actions.

The list must remain readable on narrow devices and large font scales.

---

## 19. Export

FinLux supports export for user-controlled data portability and reporting.

### Excel

Planned/implemented report structures include:

- Summary.
- Transactions.
- Wallet Statement.
- Category.
- Budget.
- Debt.
- Deal.

### PDF

- Executive summary.
- Selected period.
- KPI.
- Charts.
- Definitions/disclaimer where relevant.

Export must reuse report calculations rather than duplicate finance formulas.

---

## 20. Appearance & design system

FinLux has multiple visual styles while keeping one business state.

Current design direction includes:

- Classic / Liquid Glass.
- Modern Luxury.
- Prism.
- Light / Dark / System appearance.

Principle:

```text
same state
same use case
same navigation
same feature coverage
different tokens/layout skin
```

### Semantic design tokens

Examples:

- income.
- expense.
- transfer.
- saving.
- investment.
- debt.
- warning.
- danger.
- success.
- info.
- surface.
- elevated surface.
- primary/secondary text.
- divider.
- chart palette.

Hard-coded presentation colors should be minimized or explicitly documented.

---

## 21. Navigation architecture

Recommended primary bottom navigation:

1. Home.
2. Transactions.
3. Reports.
4. Profile / More.

Secondary modules:

- Wallet.
- Budget.
- Goal.
- Debt.
- Deal.
- Saving Spin.
- Reminder.
- Settings.

Rationale: preserve a simple daily workflow while keeping advanced financial modules accessible without crowding the main navigation.

---

## 22. Settings information architecture

Settings are grouped into:

- Account.
- Financial Preferences.
- Security.
- Appearance.
- Notifications.
- Data / Export.
- About.

Salary Cycle and Saving Spin use dedicated screens/sheets because they contain domain-specific configuration.

---

## 23. Accessibility & device support

The UI must be validated against:

- 360dp class widths.
- 412dp class widths.
- Portrait/landscape.
- Gesture navigation.
- 3-button navigation.
- Font scale 1.0.
- Font scale 1.3.
- Font scale 1.5.
- Light/dark.
- TalkBack semantics.
- Minimum touch target around 48dp.

No essential content may be obscured by bottom navigation, keyboard or system insets.

---

## 24. Reliability & non-functional requirements

### Security

- Firebase rules aligned with Android schema.
- User-owned data isolation.
- Storage privacy for avatar/receipt.
- Production signing must fail closed.
- No secrets committed to source.
- Security-sensitive receiver/debug hooks separated from production.

### Performance

- Avoid unbounded list generation.
- Efficient Firestore queries/indexes.
- Realtime listeners limited to necessary scopes.
- Charts should not recompute expensive aggregates unnecessarily.

### Observability

- Crash/error monitoring.
- Actionable logs around sync, finance mutations and failed operations.
- Release/build traceability.

### Offline behavior

- Firestore offline persistence where applicable.
- Clear UI for pending/error states.
- Avoid showing success when remote write is rejected by rules.

---

## 25. Product status

FinLux is currently a **feature-rich open-source Android application under active development**, not a finished banking product.

Repository baseline reviewed for this document:

```text
versionName = 1.22.0
versionCode = 165
```

The current priority is production hardening:

1. Financial data integrity.
2. Firebase/security contract alignment.
3. Business-rule consistency.
4. Reporting semantics.
5. UI/UX unification.
6. Architecture/refactor.
7. Regression tests.
8. CI/CD governance.
9. Observability/performance.
10. Documentation/repository hygiene.
11. Release candidate.

---

## 26. Public positioning

Recommended public description:

> **FinLux is an open-source Android personal finance app that brings transactions, wallets, budgets, salary-cycle tracking, goals, debts, financial deals and savings into one consistent financial view. Built with Kotlin, Jetpack Compose, Clean Architecture and Firebase, FinLux focuses on clear daily money tracking, traceable reporting and a modern adaptive UI.**

### Short tagline options

Primary:

> **See your money clearly. Control every financial period.**

Alternative:

> **Personal finance, structured around how you actually get paid and spend.**

---

## 27. Website conversion map

This document maps to the public website as follows:

| Product content | Website section |
|---|---|
| Product identity | Hero |
| Core questions | Problem / Value |
| Transactions + wallets | Everyday Money |
| Salary Cycle | Salary Cycle highlight |
| Reports | Analytics section |
| Goal / Debt / Deal | Advanced Finance |
| Saving Spin | Gamified Savings |
| UI themes | Design Experience |
| Architecture | Built for developers |
| Security / data integrity | Trust & Engineering |
| Roadmap | Development Status |
| Open source | GitHub CTA |

---

## 28. Product boundaries for the website

Do not claim:

- Bank connectivity if it is not implemented.
- iOS or web app availability.
- AI financial advice as a current production feature.
- Guaranteed financial outcomes.
- Commercial banking-grade certification.
- Features not proven by current code/docs.

Website wording must clearly indicate that FinLux is an open-source project under active development.

---

## 29. Final product architecture summary

```text
USER
  ↓
ANDROID UI — Jetpack Compose
  ↓
Presentation / ViewModel / UiState
  ↓
Domain Use Cases + Financial Rules
  ↓
Repository Interfaces
  ↓
Data Layer
  ├── Firebase Auth
  ├── Firestore
  ├── Storage
  ├── FCM
  ├── Cloud Functions
  └── DataStore
  ↓
Shared finance semantics
  ├── Financial Period Resolver
  ├── Wallet / Ledger invariants
  ├── Transfer semantics
  ├── Goal / Debt / Deal semantics
  └── Report DTOs
```

This is the baseline that the FinLux website and future product documentation should follow.
