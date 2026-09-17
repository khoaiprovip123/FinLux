# FinLux Prism UI Consolidation & Redesign Master Plan

> **For agentic workers:** Implement this plan phase-by-phase. Do not delete Classic/Modern UI until Prism feature parity, migration safety, build verification, and regression checks are complete.

**Goal:** Consolidate FinLux Android into one official UI language based on Prism, remove `CLASSIC_LIQUID` and `MODERN_LUXURY`, standardize the FinLux Design System, refactor oversized Compose screens, and preserve all existing business functionality.

**Architecture:** Prism stops being an optional theme and becomes the canonical FinLux presentation layer. Runtime UI branching is removed, while `Light / Dark / System` remains as appearance modes. Migration must remain backward-compatible for users who previously stored Classic or Modern UI preferences.

**Tech Stack:** Kotlin, Jetpack Compose, Android Architecture Components, DataStore, existing FinLux domain/data layers.

**Primary existing files/components:**
- `app/src/main/java/com/finlux/app/domain/model/FinanceModels.kt`
- `app/src/main/java/com/finlux/app/presentation/home/HomeScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/transaction/TransactionsScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/transaction/prism/PrismTransactionsScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/wallet/WalletsScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/wallet/prism/PrismWalletsScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/budget/BudgetScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/budget/prism/PrismBudgetScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/reports/ReportsScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/reports/prism/PrismReportsScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/settings/prism/PrismSettingsScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/components/MainBottomBar.kt`
- `app/src/main/java/com/finlux/app/core/designsystem/theme/FinluxTokens.kt`
- `app/src/main/java/com/finlux/app/core/designsystem/theme/FinluxTheme.kt`

---

# 1. Product decision

FinLux currently supports three UI families:

```text
AppUiStyle
├── CLASSIC_LIQUID
├── MODERN_LUXURY
└── PRISM
```

Target state:

```text
FinLux
└── FinLux Design System
    ├── System appearance
    ├── Light appearance
    └── Dark appearance

Presentation
├── Home
├── Transactions
├── Wallets
├── Budget
├── Reports
├── Saving
├── Debt
└── Settings
```

Prism is no longer a theme option. Prism becomes the official FinLux design language.

## Product principles

FinLux must let users answer these questions within a few seconds:

1. How much money do I currently have?
2. How much have I earned in the current period?
3. How much have I spent in the current period?
4. Where is the money going?
5. Am I financially safe at the current spending rate?

Every major UI decision should support one or more of these questions.

---

# 2. Non-negotiable constraints

- Do not remove Classic/Modern source code before Prism reaches feature parity.
- Do not change business rules just to simplify UI implementation.
- Do not introduce a fourth UI family.
- Do not treat transfers as normal expense/income.
- Salary-cycle selection must remain consistent between Home, Reports, Budget, and related analytics.
- Preserve stored user data during upgrade.
- Preserve navigation destinations and deep links unless explicitly migrated.
- New presentation code must use shared design-system tokens instead of raw hard-coded colors where possible.
- Every phase must be independently reviewable.
- Build/test verification is required before destructive cleanup.

---

# 3. Target user experience

## Visual direction

FinLux Prism should feel:

- clear;
- financial-first;
- data-first;
- modern;
- bank-like;
- calm rather than decorative;
- readable at a glance;
- consistent across screens;
- usable in both light and dark mode.

## Information hierarchy

Priority order:

```text
Money state
→ Period performance
→ Action
→ Financial warning / recommendation
→ Detailed analytics
→ Engagement features
```

Saving Spin and other engagement features must not visually outrank core financial information.

---

# 4. Phase 0 — Baseline and safety

## Objective

Create a known baseline before changing UI architecture.

## Tasks

- [ ] Record current app version and commit SHA.
- [ ] Tag or otherwise preserve the pre-consolidation state.
- [ ] Run a clean debug build.
- [ ] Run existing unit tests.
- [ ] Record any failures that already exist before migration.
- [ ] Capture screenshots of the current Prism Home, Transactions, Wallets, Budget, Reports, and Settings screens in Light and Dark mode.
- [ ] Record supported navigation destinations.

## Recommended commands

```bash
./gradlew clean
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

If instrumentation is configured:

```bash
./gradlew connectedDebugAndroidTest
```

## Acceptance criteria

- Existing baseline is reproducible.
- Pre-existing errors are documented separately from migration regressions.
- Current Prism screenshots exist for visual comparison.

---

# 5. Phase 1 — Feature parity audit

## Objective

Verify that removing Classic/Modern will not remove a user capability.

## Build a parity matrix

For every feature, compare:

| Module | Classic | Modern | Prism | Action |
|---|---:|---:|---:|---|
| Home | ✓/✗ | ✓/✗ | ✓/✗ | Keep/Port/Fix |
| Transactions | ✓/✗ | ✓/✗ | ✓/✗ | Keep/Port/Fix |
| Wallets | ✓/✗ | ✓/✗ | ✓/✗ | Keep/Port/Fix |
| Transfer | ✓/✗ | ✓/✗ | ✓/✗ | Keep/Port/Fix |
| Budget | ✓/✗ | ✓/✗ | ✓/✗ | Keep/Port/Fix |
| Reports | ✓/✗ | ✓/✗ | ✓/✗ | Keep/Port/Fix |
| Settings | ✓/✗ | ✓/✗ | ✓/✗ | Keep/Port/Fix |
| Saving | ✓/✗ | ✓/✗ | ✓/✗ | Keep/Port/Fix |
| Debt | ✓/✗ | ✓/✗ | ✓/✗ | Keep/Port/Fix |

## Audit scope

### Navigation
- Splash
- Authentication
- Main scaffold
- Bottom navigation
- Back handling
- Dialogs
- Bottom sheets

### Home
- Greeting/header
- Balance visibility
- Financial overview
- Income/expense/net cashflow
- Salary cycle
- Quick actions
- Saving Spin entry
- Budget health
- Category breakdown
- Recent transactions
- Empty/loading/error state

### Transactions
- List
- Date grouping
- Search
- Filters
- Income
- Expense
- Transfer
- Calendar mode
- Detail
- Create/Edit/Delete
- Wallet/category selection
- Date/time
- Balance validation

### Wallets
- Wallet summary
- Wallet types
- Add/Edit/Delete wallet
- Transfer source/destination
- Swap wallets
- Insufficient balance handling
- Wallet detail

### Budget
- Budget summary
- Progress
- Remaining amount
- Days remaining
- Overspending state
- Create/Edit/Delete

### Reports
- Overview
- Income/Expense
- Cashflow
- Category
- Wallet
- Daily/Weekly/Monthly
- Salary cycle
- Custom period
- Opening/closing balance
- Previous-period comparison
- Drill-down

### Settings
- Profile/account
- Financial settings
- Security
- Notifications
- Theme/appearance
- Balance visibility
- About/update

## Rule

If Classic/Modern contains behavior missing from Prism, port the behavior to Prism. Do not keep an old UI family solely because Prism is incomplete.

## Acceptance criteria

- Parity matrix exists.
- Every missing Prism capability has an explicit migration task.
- No old UI family is removed during this phase.

---

# 6. Phase 2 — Make Prism the default UI

## Objective

New installations and fallback behavior must resolve to Prism.

## Tasks

- [ ] Change root/default UI style from `CLASSIC_LIQUID` to `PRISM`.
- [ ] Change DataStore fallback from Classic to Prism.
- [ ] Update previews/tests that assume Classic default.
- [ ] Verify first-run launch goes directly to Prism.

## Existing risk

The current architecture can initialize users to Classic even though most recent polish work is concentrated in Prism.

## Acceptance criteria

- Fresh install opens Prism.
- Missing preference opens Prism.
- Corrupted/unknown style preference safely falls back to Prism.

---

# 7. Phase 3 — Backward-compatible preference migration

## Objective

Users upgrading from older FinLux versions must not crash or become stuck because they previously selected Classic/Modern.

## Desired migration semantics

```kotlin
when (storedStyle) {
    "CLASSIC_LIQUID",
    "MODERN_LUXURY",
    "PRISM" -> AppUiStyle.PRISM
    else -> AppUiStyle.PRISM
}
```

This is an intermediate migration step. Do not remove the enum until runtime dependencies are gone.

## Test cases

- Stored `CLASSIC_LIQUID` → Prism.
- Stored `MODERN_LUXURY` → Prism.
- Stored `PRISM` → Prism.
- Missing value → Prism.
- Unknown value → Prism.

## Acceptance criteria

- Upgrade path is safe.
- No blank screen.
- No `IllegalArgumentException`/enum parsing crash.
- No user data is reset just to migrate appearance.

---

# 8. Phase 4 — Remove UI-family selector from Settings

## Objective

Stop presenting Prism/Modern/Classic as user-selectable UI families.

## Remove

```text
FinLux Prism
Modern Luxury
Liquid Glass Classic
```

## Keep/replace with

```text
Appearance
○ System
○ Light
○ Dark
```

Optional settings that can remain if already supported and useful:

- animation level;
- content/card density;
- hide/show balances.

## Acceptance criteria

- Settings no longer allows selecting Classic/Modern.
- Appearance modes continue to work.
- Existing settings layout remains understandable and compact.

---

# 9. Phase 5 — Remove runtime UI dispatchers

## Objective

Stop branching every primary screen by `AppUiStyle`.

## Current pattern to remove

```kotlin
when (LocalAppUiStyle.current) {
    AppUiStyle.CLASSIC_LIQUID -> ClassicHomeScreen(...)
    AppUiStyle.MODERN_LUXURY -> ModernHomeScreen(...)
    AppUiStyle.PRISM -> PrismHomeScreen(...)
}
```

## Intermediate target

```kotlin
PrismHomeScreen(...)
```

Later target after rename:

```kotlin
HomeScreen(...)
```

## Apply to

- Home
- Transactions
- Wallets
- Budget
- Reports
- Settings where applicable
- Main bottom bar
- shared route-level UI components

## Acceptance criteria

- No main screen requires runtime selection among three UI implementations.
- App still builds while old source code remains available for parity/reference.

---

# 10. Phase 6 — FinLux Design System 3.0

## Objective

Turn Prism into a reusable, centralized design system rather than a collection of screen-specific styling decisions.

## 10.1 Color tokens

Avoid presentation-level raw values such as:

```kotlin
Color(0xFF2563EB)
```

Preferred semantic tokens:

```kotlin
FinluxTheme.colors.primary
FinluxTheme.colors.secondary
FinluxTheme.colors.background
FinluxTheme.colors.surface
FinluxTheme.colors.surfaceElevated
FinluxTheme.colors.onSurface
FinluxTheme.colors.onSurfaceVariant
FinluxTheme.colors.income
FinluxTheme.colors.expense
FinluxTheme.colors.transfer
FinluxTheme.colors.success
FinluxTheme.colors.warning
FinluxTheme.colors.error
FinluxTheme.colors.info
```

### Financial semantics

- Income → green semantic token.
- Expense → red semantic token.
- Transfer → blue semantic token.
- Warning → amber semantic token.
- Error/overspending → error token.

Do not use semantic colors decoratively in ways that change their meaning.

## 10.2 Spacing tokens

Recommended scale:

```text
4 / 8 / 12 / 16 / 20 / 24 / 32 / 40 dp
```

Expose tokens such as:

```kotlin
FinluxTheme.spacing.xs
FinluxTheme.spacing.sm
FinluxTheme.spacing.md
FinluxTheme.spacing.lg
FinluxTheme.spacing.xl
```

## 10.3 Radius tokens

Recommended scale:

```text
8 / 12 / 16 / 20 / 24 / 28 dp
```

Guideline:

- Input: 12–16dp
- Button: 14–18dp
- Card: 18–24dp
- Hero card: 24–28dp
- Bottom sheet: 24–28dp top corners

## 10.4 Typography

Define reusable roles:

```text
Display
Headline
Title
Body
Label
Caption
Micro
```

Avoid arbitrary font sizes in individual screens unless required by a documented component.

## 10.5 Shared components

Create/standardize:

```text
FinluxScreen
FinluxHeader
FinluxSectionHeader
FinluxCard
FinluxHeroCard
FinluxMetricCard
FinluxButton
FinluxIconButton
FinluxTextField
FinluxAmountInput
FinluxChip
FinluxFilterChip
FinluxTransactionRow
FinluxWalletCard
FinluxBudgetCard
FinluxEmptyState
FinluxLoadingState
FinluxErrorState
FinluxBottomSheet
FinluxDialog
```

## Acceptance criteria

- Major Prism screens no longer own duplicated visual primitives.
- Light and Dark mode use semantic token sets.
- Hard-coded presentation colors are substantially eliminated and remaining exceptions are documented.

---

# 11. Phase 7 — Home redesign/refactor

## Target information order

```text
Home
├── Header
├── Financial Hero
├── Period Health
├── Quick Actions
├── Budget Health
├── Category Spending
├── Saving Widget
└── Recent Transactions
```

## 11.1 Header

Show only high-value information:

- user greeting/name if available;
- active financial/salary period;
- notification entry;
- balance visibility control if required.

Keep header height and padding consistent with other primary screens.

## 11.2 Financial hero

Primary data:

```text
TOTAL ASSETS
52,850,000 ₫

Income period   +12,500,000
Expense period   -7,250,000
Net cashflow     +5,250,000
```

Do not overload the hero with secondary KPIs.

## 11.3 Period health

Add actionable context:

```text
Spent:        7,250,000 ₫
Budget:      10,000,000 ₫
Remaining:    2,750,000 ₫
Days left:    12
Approx/day:     229,000 ₫
```

## 11.4 Quick actions

Maximum four top-level actions:

```text
Expense | Income | Transfer | More
```

Lower-frequency actions go to `More`.

## 11.5 Saving Spin

Use a compact engagement card, not a dominant financial hero.

Example:

```text
Saving Spin
Not spun today
[Spin now]
```

## 11.6 Recent transactions

Show approximately 5–7 recent rows, then `View all`.

## Recommended file structure

```text
presentation/home/
├── HomeScreen.kt
├── HomeViewModel.kt
├── HomeUiState.kt
└── components/
    ├── HomeHeader.kt
    ├── FinancialHero.kt
    ├── PeriodHealthCard.kt
    ├── QuickActions.kt
    ├── SavingSpinCard.kt
    ├── BudgetHealthCard.kt
    ├── CategoryBreakdown.kt
    └── RecentTransactions.kt
```

## Acceptance criteria

- Home answers the five primary financial questions quickly.
- Saving Spin does not overpower financial information.
- Screen logic is decomposed into reusable sections.
- Loading/empty/error states exist.

---

# 12. Phase 8 — Transactions redesign/refactor

## Target layout

```text
Transactions
├── Search
├── Primary filters
├── List / Calendar toggle
└── Date-grouped transactions
```

## Primary filters

```text
All | Expense | Income | Transfer
```

## Secondary filters

- date/period;
- wallet;
- category;
- amount range.

These should open an advanced filter surface rather than overcrowding the main row.

## Date grouping

Maintain clear groups such as:

```text
Today
Yesterday
14/09/2026
```

Per-group summary may include transaction count and net amount.

## Transfer semantics

A transfer must visually communicate:

```text
Wallet A → Wallet B
```

Do not present it as a normal expense.

## Recommended structure

```text
presentation/transaction/
├── TransactionsScreen.kt
├── TransactionsViewModel.kt
└── components/
    ├── TransactionSearch.kt
    ├── TransactionFilterBar.kt
    ├── TransactionGroup.kt
    ├── TransactionRow.kt
    └── TransactionCalendar.kt
```

## Acceptance criteria

- Search/filter behavior is obvious.
- Transfer is distinct from income/expense.
- List/calendar switch is stable.
- Create/Edit/Delete and transfer flows remain functional.

---

# 13. Phase 9 — Wallet redesign/refactor

## Page structure

```text
Wallets & Accounts

TOTAL ASSETS
52,850,000 ₫

Cash
Bank
E-wallet
Credit Card
Savings
Investment
Other
```

## Wallet card standard

Every wallet card should expose a consistent set:

- icon;
- wallet name;
- wallet type;
- balance;
- optional account/card suffix.

Avoid entirely different card architecture for each wallet type.

## Wallet detail

```text
Vietcombank
30,500,000 ₫

[Income] [Expense] [Transfer]

Cashflow
Recent transactions
```

## Transfer requirements

- source wallet;
- destination wallet;
- swap source/destination;
- amount;
- insufficient-balance validation;
- clear success/error state.

## Acceptance criteria

- Wallet cards share a common visual grammar.
- Transfer source/destination selection is explicit.
- Wallet balance and transaction history remain accurate.

---

# 14. Phase 10 — Budget redesign/refactor

## Budget card target

```text
Food
2,820,000 / 3,000,000
████████████████░
94%

Remaining: 180,000 ₫
12 days left
≈ 15,000 ₫/day
```

## Risk states

```text
0–70%    Stable
70–90%   Attention
90–100%  Near limit
>100%    Over budget
```

Do not represent status by color alone; include text/icon semantics for accessibility.

## Acceptance criteria

- Remaining money is visible.
- Days remaining are visible.
- Approximate daily safe spending is visible when meaningful.
- Create/Edit/Delete remains intact.

---

# 15. Phase 11 — Reports redesign/refactor

## Keep primary Prism tabs

```text
Overview
Income & Expense
Categories
Deep Dive
```

## Global period filter

Use one period model across all report tabs:

```text
Today
This week
This month
Salary cycle
Custom
```

Changing the period must update every tab consistently.

## Core financial statement

Every relevant cashflow report should be able to express:

```text
Opening balance
+ Income
- Expense
± Adjustments
= Closing balance
```

Example:

```text
Opening       12,300,000
Income        +8,500,000
Expense       -4,200,000
Closing       16,600,000
```

Transfers between owned wallets must not falsely inflate income/expense totals.

## Daily report

For `Today`:

```text
Opening balance of day
Income today
Expense today
Closing balance of day
```

## Period comparison

Example:

```text
Compared with previous period
Income       +6%
Expense     -12%
Net cashflow +21%
Savings      +8%
```

## Drill-down flow

```text
Reports
→ Category
→ Category Detail
→ Transactions
→ Transaction Detail
```

Do not force users to manually leave Reports and recreate filters.

## Recommended structure

```text
presentation/reports/
├── ReportsScreen.kt
├── ReportsViewModel.kt
├── components/
│   ├── ReportPeriodSelector.kt
│   ├── OpeningClosingCard.kt
│   ├── CashflowChart.kt
│   ├── IncomeExpenseSummary.kt
│   ├── CategoryBreakdown.kt
│   ├── PeriodComparison.kt
│   └── ReportEmptyState.kt
└── screens/
    ├── OverviewReport.kt
    ├── CashflowReport.kt
    ├── CategoryReport.kt
    └── DeepDiveReport.kt
```

## Acceptance criteria

- Period selection is unified.
- Salary cycle works in Reports.
- Daily opening/closing balance is available.
- Drill-down is direct and predictable.

---

# 16. Phase 12 — Settings redesign/refactor

## Target groups

```text
Profile

Financial
├── Wallets
├── Budget
├── Salary Cycle
├── Savings
└── Reminders

Security
├── Biometrics
└── App Lock

Appearance
├── System / Light / Dark
├── Animation
├── Density
└── Hide Balances

Application
├── Backup
├── Update
└── About
```

## Acceptance criteria

- No Classic/Modern/Prism style selector.
- Financial and security settings remain easy to find.
- Appearance contains only appearance-level settings.

---

# 17. Phase 13 — Split oversized Prism screens

## Objective

Reduce maintenance cost and UI regression risk.

## Guideline

- Prefer screen files under roughly 400–500 lines.
- Prefer focused component files under roughly 300 lines.
- Do not split merely for line count; split by clear responsibility.

## Priority files

- Prism Home
- Prism Reports
- Prism Transactions
- Prism Settings
- other oversized Prism screens discovered during audit

## State boundary

Composable screens should primarily render UI state.

Avoid large financial calculations directly in Composables, including:

- opening balance;
- closing balance;
- period comparison;
- budget projections;
- large cashflow aggregations.

Prepare these in ViewModel/domain use cases and expose stable UI state.

## Example state

```kotlin
data class HomeUiState(
    val isLoading: Boolean,
    val financialSummary: FinancialSummary,
    val budgetHealth: BudgetHealth,
    val recentTransactions: List<TransactionItem>,
    val error: UiError?
)
```

## Acceptance criteria

- Large screen responsibilities are separated.
- Reusable visual components do not duplicate business logic.
- ViewModels/domain logic own heavy calculations.

---

# 18. Phase 14 — Delete Classic and Modern source code

## Preconditions

All must be true:

- Prism feature parity confirmed.
- Preference migration verified.
- Runtime dispatchers removed.
- Core flows tested.
- Clean debug build succeeds.
- Old UI code no longer has required unique business logic.

## Tasks

- [ ] Delete Classic screen implementations.
- [ ] Delete Modern screen implementations.
- [ ] Delete dead shared components used only by Classic/Modern.
- [ ] Delete dead resources used only by Classic/Modern.
- [ ] Remove dead imports.
- [ ] Run static search for old class names.

## Acceptance criteria

- App builds without Classic/Modern sources.
- No user capability disappears.

---

# 19. Phase 15 — Remove obsolete AppUiStyle architecture

After old implementations are gone and preference migration is no longer required at runtime, remove obsolete architecture where safe:

```text
AppUiStyle
LocalAppUiStyle
UiStyleKey
setUiStyle()
uiStyle Flow
```

Migration code may be retained temporarily if needed for an upgrade window, but it must not continue driving runtime presentation selection.

## Acceptance criteria

- No presentation decision depends on `AppUiStyle`.
- Theme selection is handled through appearance mode only.

---

# 20. Phase 16 — Rename Prism to canonical FinLux screen names

Once Prism is the only UI:

```text
PrismHomeScreen         → HomeScreen
PrismTransactionsScreen → TransactionsScreen
PrismWalletsScreen      → WalletsScreen
PrismBudgetScreen       → BudgetScreen
PrismReportsScreen      → ReportsScreen
PrismSettingsScreen     → SettingsScreen
```

Flatten Prism-only packages when practical:

```text
presentation/home/prism/
→ presentation/home/
```

Do the same for other canonical screens.

## Acceptance criteria

- `prism` no longer implies an optional parallel UI architecture.
- Public screen naming matches product terminology.

---

# 21. Phase 17 — UI state, accessibility, responsive behavior, performance

## 21.1 Standard states

Every primary screen should support:

```text
Loading
Content
Empty
Error
```

### Example empty state

```text
No transactions yet

Add your first transaction so FinLux can begin tracking your cashflow.

[Add transaction]
```

### Example error state

```text
Unable to load data
Please try again.

[Retry]
```

Do not expose raw exception messages to users.

## 21.2 Accessibility

Verify:

- touch target >= 48dp where applicable;
- readable contrast;
- dynamic font scaling;
- meaningful `contentDescription`;
- state is not indicated by color alone;
- dark mode remains legible.

## 21.3 Responsive widths

Test at least:

```text
360dp
393dp
412dp
480dp+
```

Check:

- line wrapping;
- KPI cards;
- tabs;
- chips;
- charts;
- keyboard overlap;
- bottom sheets.

## 21.4 Keyboard/IME

Forms such as Add/Edit Transaction, Transfer, Wallet, Budget must not allow the keyboard to cover primary actions.

Use appropriate Compose insets such as:

```kotlin
imePadding()
navigationBarsPadding()
```

where required.

## 21.5 Animation

Use animation for functional transitions only:

- navigation;
- expand/collapse;
- value changes;
- progress;
- state transitions.

Avoid decorative animation that interferes with readability.

## 21.6 Performance

Audit Compose for:

- unnecessary recomposition;
- expensive calculations inside Composables;
- missing Lazy list keys;
- incorrect `remember` usage;
- opportunities for `derivedStateOf`;
- chart recomputation;
- unstable state objects.

Priority: Home, Reports, Transactions.

---

# 22. Shared financial period model

## Objective

Prevent Home, Budget, and Reports from using different period interpretations.

Recommended domain concept:

```kotlin
sealed interface FinancialPeriod {
    data object Today : FinancialPeriod
    data object ThisWeek : FinancialPeriod
    data object CalendarMonth : FinancialPeriod
    data class SalaryCycle(/* resolved start/end */) : FinancialPeriod
    data class CustomRange(/* start/end */) : FinancialPeriod
}
```

The exact type should follow existing FinLux date/domain conventions rather than introducing duplicate date models.

## Acceptance criteria

- Salary cycle uses the same resolved dates across modules.
- Period labels and calculations agree across Home, Budget, Reports, and related features.

---

# 23. QA matrix

## Fresh install

- [ ] Launch app.
- [ ] Prism/FinLux canonical UI is displayed.
- [ ] No Classic/Modern selector appears.
- [ ] System appearance works.
- [ ] Light appearance works.
- [ ] Dark appearance works.

## Upgrade tests

- [ ] Stored Classic preference upgrades to Prism.
- [ ] Stored Modern preference upgrades to Prism.
- [ ] Stored Prism preference remains Prism.
- [ ] Unknown preference falls back safely.

## Navigation

- [ ] Home.
- [ ] Transactions.
- [ ] Reports.
- [ ] Settings.
- [ ] Wallet.
- [ ] Budget.
- [ ] Debt.
- [ ] Saving.
- [ ] Back navigation.
- [ ] Bottom navigation.
- [ ] Supported deep links.

## Transactions

- [ ] Create income.
- [ ] Create expense.
- [ ] Edit transaction.
- [ ] Delete transaction.
- [ ] Transfer between wallets.
- [ ] Insufficient-balance handling.
- [ ] Wallet selection.
- [ ] Category selection.
- [ ] Salary-period behavior.

## Reports

- [ ] Today.
- [ ] This week.
- [ ] Month.
- [ ] Salary cycle.
- [ ] Custom date range.
- [ ] Opening balance.
- [ ] Closing balance.
- [ ] Category drill-down.
- [ ] Wallet drill-down where supported.

## Budget

- [ ] Create/edit/delete.
- [ ] Risk state.
- [ ] Remaining amount.
- [ ] Remaining days.
- [ ] Daily spendable estimate.

## Wallet

- [ ] Add/edit/delete wallet.
- [ ] Transfer source/destination.
- [ ] Swap source/destination.
- [ ] Balance validation.

---

# 24. Visual regression set

Capture and compare at minimum in Light and Dark mode:

- Home
- Transactions
- Wallets
- Budget
- Reports
- Settings
- Add Transaction
- Transfer

Repeat after major design-system refactors.

---

# 25. Static cleanup searches

Before final removal, search for:

```text
Classic
Modern
CLASSIC_LIQUID
MODERN_LUXURY
AppUiStyle
LocalAppUiStyle
PrismHomeScreen
PrismTransactionsScreen
PrismWalletsScreen
PrismBudgetScreen
PrismReportsScreen
```

Final runtime target:

```text
0 obsolete UI-family runtime references
```

Historical docs/changelog and migration tests may intentionally retain legacy names.

---

# 26. Build gate

Before considering consolidation complete, run:

```bash
./gradlew clean
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

If instrumentation tests are configured and an emulator/device is available:

```bash
./gradlew connectedDebugAndroidTest
```

No phase should claim successful build/test status without fresh command output.

---

# 27. Definition of Done

The Prism consolidation is complete only when all of the following are true:

- [ ] Prism is the single canonical FinLux UI.
- [ ] Classic UI has no runtime implementation.
- [ ] Modern UI has no runtime implementation.
- [ ] UI-family selector is removed.
- [ ] System/Light/Dark appearance works.
- [ ] Upgrade from old style preferences is safe.
- [ ] Feature parity is confirmed.
- [ ] Home is refactored and consistent.
- [ ] Transactions are refactored and consistent.
- [ ] Wallets are refactored and consistent.
- [ ] Budget is refactored and consistent.
- [ ] Reports are refactored and consistent.
- [ ] Settings are refactored and consistent.
- [ ] Saving Spin remains functional.
- [ ] Debt features remain functional.
- [ ] Salary cycle remains functional.
- [ ] Transfer semantics remain correct.
- [ ] Navigation remains intact.
- [ ] Major screens have loading/content/empty/error states.
- [ ] Design tokens replace duplicated screen-level styling.
- [ ] Oversized Prism files are decomposed where appropriate.
- [ ] Unit tests pass.
- [ ] Debug APK build passes.
- [ ] Documentation/changelog is updated.

---

# 28. Recommended execution order

```text
PHASE 0   Baseline & Safety
PHASE 1   Feature Parity Audit
PHASE 2   Prism Default
PHASE 3   Preference Migration
PHASE 4   Remove UI Style Selector
PHASE 5   Remove Runtime Dispatchers
PHASE 6   FinLux Design System 3.0
PHASE 7   Home Refactor
PHASE 8   Transactions Refactor
PHASE 9   Wallet Refactor
PHASE 10  Budget Refactor
PHASE 11  Reports Refactor
PHASE 12  Settings Refactor
PHASE 13  Split Oversized Screens
PHASE 14  Delete Classic/Modern
PHASE 15  Remove AppUiStyle Architecture
PHASE 16  Rename Prism to Canonical FinLux UI
PHASE 17  QA / Accessibility / Performance
PHASE 18  Release & Documentation
```

---

# 29. Priority model

## P0 — Blocker

- Baseline.
- Feature parity audit.
- Prism default.
- Preference migration.
- Runtime dispatcher removal.
- Critical navigation.
- Critical transaction operations.
- Build/test stability.

## P1 — High

- Design System 3.0.
- Home.
- Transactions.
- Reports.
- Wallets.
- Budget.
- Settings.
- Large-file refactoring.

## P2 — Medium

- Animation polish.
- Micro-interactions.
- Additional accessibility improvements.
- Visual polish.
- Wider responsive optimization.

---

# 30. Rules for AI/developer implementation

1. Do not silently change financial business rules while performing UI refactors.
2. Do not delete Classic/Modern before feature parity is documented.
3. Do not create another UI family.
4. Do not duplicate shared components without justification.
5. Do not duplicate financial calculations across screens.
6. Keep screen files focused and decomposed by responsibility.
7. Run verification after each meaningful phase.
8. Fix regressions before moving to destructive cleanup.
9. Update changelog/docs for architecture-impacting changes.
10. Do not commit unnecessary APKs or generated binaries.
11. Keep preference migration backward-compatible.
12. Verify report calculations whenever report presentation is modified.
13. Treat internal wallet transfer separately from normal expense/income.
14. Keep Salary Cycle consistent between Home, Budget, Reports, and related analytics.
15. Preserve existing business behavior unless a separate approved task explicitly changes it.

---

# 31. Final architecture

```text
FinLux
│
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
│
├── data/
│
├── core/
│   ├── designsystem/
│   │   ├── theme/
│   │   ├── tokens/
│   │   └── components/
│   ├── navigation/
│   └── common/
│
└── presentation/
    ├── home/
    ├── transaction/
    ├── wallet/
    ├── budget/
    ├── reports/
    ├── saving/
    ├── debt/
    ├── settings/
    └── auth/
```

Final presentation architecture should not need parallel `classic/`, `modern/`, and `prism/` screen families.

---

# 32. Final product statement

FinLux should no longer be an application that maintains three competing UI implementations.

It should be one financial product with one consistent design language.

**Prism becomes FinLux.**
