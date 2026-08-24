# FinLux Salary Cycle Design

**Date:** 2026-08-24
**Status:** Proposed
**Target repository:** `khoaiprovip123/FinLux`

## 1. Goal

Add a salary-cycle financial period that starts from the user's configured payday instead of being tied to calendar months. The cycle becomes an optional primary financial lens for budget tracking, dashboard summaries, period comparisons, and end-of-cycle savings handling while preserving existing calendar-based reports.

## 2. Core product decision

The configured payday is the canonical boundary for financial cycles.

Example for payday = day 25:

- 2026-08-25 -> 2026-09-24
- 2026-09-25 -> 2026-10-24
- 2026-10-25 -> 2026-11-24

Actual salary transactions may arrive early or late, but they do not move historical cycle boundaries. They are associated with the cycle that contains their transaction timestamp.

Rationale:

- reports remain stable after transactions sync across devices;
- historical budgets do not shift when a salary transaction is edited;
- comparison between cycles remains deterministic;
- Firebase and Android can calculate identical boundaries.

## 3. Scope

### Included in first implementation

- one active salary-cycle configuration per user;
- supported payday rules:
  - fixed day of month `1..31`;
  - first day of month;
  - last day of month;
- automatic handling of short months;
- salary-cycle date range calculation;
- cycle-aware dashboard and reports;
- cycle-aware budget period option;
- comparison with previous salary cycle;
- salary wallet association;
- optional expected salary amount;
- configurable end-of-cycle leftover behavior;
- DataStore/demo/Firebase support;
- unit tests for date boundaries and business rules.

### Not included in first implementation

- multiple simultaneous salary sources with independent cycles;
- payroll-provider integration;
- automatic detection of salary deposits by bank description;
- server-side automatic money transfer without explicit user confirmation;
- moving legacy transaction dates or rewriting historical transactions.

These can be added later without changing the core cycle engine.

## 4. Domain model

Create a focused salary-cycle model instead of embedding date logic in ViewModels.

```kotlin
enum class PaydayRuleType {
    DAY_OF_MONTH,
    FIRST_DAY_OF_MONTH,
    LAST_DAY_OF_MONTH,
}

enum class CycleRolloverRule {
    KEEP_IN_WALLET,
    MOVE_TO_SAVINGS,
    ASK_EACH_CYCLE,
}

data class SalaryCycleConfig(
    val enabled: Boolean = false,
    val paydayRuleType: PaydayRuleType = PaydayRuleType.DAY_OF_MONTH,
    val paydayDay: Int = 1,
    val salaryWalletId: String? = null,
    val savingsWalletId: String? = null,
    val expectedSalary: Money? = null,
    val rolloverRule: CycleRolloverRule = CycleRolloverRule.KEEP_IN_WALLET,
)

data class FinancialCycle(
    val start: Instant,
    val endExclusive: Instant,
    val label: String,
)
```

Validation:

- `paydayDay` must be `1..31` when `DAY_OF_MONTH` is selected;
- `salaryWalletId` is optional;
- `savingsWalletId` is required only when `MOVE_TO_SAVINGS` is selected;
- expected salary is optional and must be positive if provided.

## 5. Cycle boundary rules

All cycle calculations must be centralized in a domain service/use case and use one finance timezone contract.

Recommended interface:

```kotlin
interface SalaryCycleCalculator {
    fun cycleContaining(
        instant: Instant,
        config: SalaryCycleConfig,
        zoneId: ZoneId,
    ): FinancialCycle

    fun previousCycle(
        cycle: FinancialCycle,
        config: SalaryCycleConfig,
        zoneId: ZoneId,
    ): FinancialCycle
}
```

### Fixed day behavior

If the selected payday does not exist in a month, use that month's last valid day.

Examples:

- payday 31 in April -> April 30;
- payday 31 in February 2027 -> February 28;
- payday 30 in February 2028 -> February 29.

This rule must be deterministic on Android and Cloud Functions.

### Boundary semantics

Use half-open intervals:

```text
[start, endExclusive)
```

This avoids double-counting a transaction exactly at midnight of the next cycle.

## 6. Timezone policy

Current project behavior is inconsistent: Android frequently relies on `ZoneId.systemDefault()` while Cloud Functions uses `Asia/Ho_Chi_Minh`.

Salary-cycle implementation must introduce one explicit finance-zone policy.

Initial rule:

- default finance timezone: `Asia/Ho_Chi_Minh`;
- store timezone identifier in user financial preferences for future internationalization;
- all reports, budgets, salary cycles and scheduled finance jobs consume the same timezone source.

The Android `FinanceClock`/`FinanceTime` contract should be extended rather than implementing separate salary-specific timezone logic.

## 7. Data architecture

### Firestore

Add one user-owned configuration document:

```text
users/{uid}/financialPreferences/salaryCycle
```

Suggested fields:

```json
{
  "enabled": true,
  "paydayRuleType": "DAY_OF_MONTH",
  "paydayDay": 25,
  "salaryWalletId": "wallet-id",
  "savingsWalletId": "savings-wallet-id",
  "expectedSalary": 20000000,
  "rolloverRule": "ASK_EACH_CYCLE",
  "financeTimeZone": "Asia/Ho_Chi_Minh",
  "updatedAt": "server timestamp"
}
```

Do not persist generated monthly cycle documents unless a future requirement needs immutable period snapshots. The initial implementation derives cycle boundaries from configuration and transaction timestamps.

### Local/demo mode

The existing demo adapter must expose the same `SalaryCycleRepository` contract so Firebase remains optional.

## 8. Repository and use-case boundaries

New repository contract:

```kotlin
interface SalaryCycleRepository {
    fun observeConfig(): Flow<SalaryCycleConfig>
    suspend fun saveConfig(config: SalaryCycleConfig)
}
```

Recommended use cases:

- `GetCurrentSalaryCycleUseCase`
- `GetPreviousSalaryCycleUseCase`
- `SaveSalaryCycleConfigUseCase`
- `CalculateCycleSummaryUseCase`
- `CalculateCycleRolloverUseCase`

Business rules stay in domain/use-case code. Composables and Firebase repositories must not calculate payday boundaries themselves.

## 9. Dashboard behavior

When salary-cycle mode is enabled, Home adds a clear cycle context such as:

```text
Kỳ lương hiện tại
25/08 - 24/09
```

Primary salary-cycle metrics:

- income received in cycle;
- expenses in cycle;
- remaining cash flow;
- days remaining in cycle;
- budget consumed percentage;
- comparison with previous cycle;
- projected remaining balance based on current spending pace.

Calendar-month analytics remain available and are not deleted.

## 10. Reports

Extend report period options with:

```kotlin
SALARY_CYCLE
```

Recommended report selector:

```text
Kỳ lương | Tháng | Quý | Năm | Tùy chọn
```

For salary-cycle reporting:

- current range comes from `SalaryCycleCalculator`;
- previous comparison uses the immediately preceding derived cycle;
- category, wallet and cash-flow aggregation reuse the same report pipeline;
- report queries must not be limited to an arbitrary recent transaction count.

### Existing issue to fix during implementation

`ReportsViewModel` currently builds reports from `observeRecent(5_000)`. This can silently truncate historical report data. Salary-cycle work must replace this with a range-based repository query or equivalent unbounded-by-count period query.

## 11. Budget integration

Budget gets a period basis:

```kotlin
enum class BudgetPeriodBasis {
    CALENDAR_MONTH,
    SALARY_CYCLE,
}
```

When `SALARY_CYCLE` is selected:

- budget start/end comes from the salary-cycle calculator;
- 80% / 100% thresholds apply to the active salary cycle;
- reset logic occurs at salary-cycle boundary, not first day of month;
- previous cycle values remain historical and must not be overwritten.

Existing monthly budget behavior remains the default until users explicitly enable salary-cycle budgeting.

## 12. End-of-cycle leftover handling

Define:

```text
leftover = cycle income - cycle expenses - confirmed debt/goal outflows
```

Rollover options:

### KEEP_IN_WALLET
No financial transaction is generated. The next cycle simply starts with the wallet's existing balance.

### ASK_EACH_CYCLE
At or after the cycle boundary, Home shows a non-blocking action card:

```text
Kỳ vừa rồi còn dư 3.250.000 đ
[Để lại trong ví] [Chuyển sang tiết kiệm]
```

### MOVE_TO_SAVINGS
For the first release, automatic transfer must still require explicit user confirmation before creating the wallet transfer. This prevents unexpected balance mutations and avoids background financial writes.

The transfer must reuse the existing atomic wallet-transfer use case and Firestore transaction path.

## 13. Settings/Profile UX

Add a Financial Cycle section under Settings/Profile:

Fields:

- Enable salary-cycle mode;
- Payday rule;
- Payday day if applicable;
- Salary receiving wallet;
- Expected salary (optional);
- End-of-cycle handling;
- Savings wallet when needed;
- Preview of current and next cycle.

Preview example:

```text
Kỳ hiện tại: 25/08/2026 - 24/09/2026
Kỳ tiếp theo: 25/09/2026 - 24/10/2026
```

All UI must use existing FinLux design tokens/components and avoid hardcoded colors.

## 14. Theme/UI architecture constraint

Salary-cycle screens must not create three independent Classic/Modern/Prism implementations.

New UI should use:

```text
shared screen structure
        |
        v
design tokens / visual primitives
        |
        +-- Classic adapter
        +-- Modern adapter
        +-- Prism adapter
```

This feature becomes the first new module implemented with the target shared-UI architecture. A separate theme-refactor spec should migrate legacy screens incrementally instead of rewriting all three themes during salary-cycle work.

## 15. Firebase security requirements

Salary-cycle settings are user-owned configuration data. Rules must validate:

- authenticated owner only;
- allowed field set;
- valid enum strings;
- `paydayDay` integer `1..31`;
- expected salary integer and positive when present;
- wallet IDs strings when present;
- timezone string length/format bounds.

Salary-cycle implementation must not weaken wallet or transaction rules.

A separate Firebase-hardening workstream must address the existing ability for a modified client to write wallet balances directly.

## 16. Cloud Functions

Do not create a scheduler per user.

If server-side cycle notifications are added, use one periodic job that:

1. reads users with salary-cycle enabled;
2. derives boundaries from the same rule semantics;
3. identifies newly closed cycles idempotently;
4. creates at most one cycle-end notification per cycle;
5. never performs an automatic wallet transfer without the user-confirmed transaction flow.

For first implementation, cycle summaries can be calculated client-side from Firestore range queries. Server scheduling is optional and should be added only if required for push notifications.

## 17. Reminder interaction

Salary-cycle notifications must be distinct from bill reminders.

The project currently has both local AlarmManager reminders and Cloud Function push reminders. Before adding salary-cycle push notifications, the implementation plan must define one ownership model per notification type to prevent duplicate notifications.

## 18. Migration and backward compatibility

Existing users:

- salary-cycle feature defaults to disabled;
- existing monthly reports continue unchanged;
- existing budgets remain `CALENDAR_MONTH`;
- no transaction migration is required;
- enabling salary-cycle immediately derives the active cycle from existing transaction dates.

Changing payday later:

- affects newly calculated current/future cycle views;
- does not mutate transaction timestamps;
- historical exported reports remain immutable files;
- if persistent budget-cycle snapshots are introduced later, configuration changes require versioned cycle definitions.

## 19. Testing strategy

### Unit tests

Required cycle calculator cases:

- payday 1;
- payday 10/15/20/25;
- payday 28/29/30/31;
- February leap year;
- February non-leap year;
- first-day rule;
- last-day rule;
- instant exactly on boundary;
- instant one millisecond before boundary;
- previous-cycle calculation;
- timezone boundary around midnight.

Required business tests:

- config validation;
- disabled cycle fallback;
- current vs previous cycle summary;
- salary-cycle budget range;
- rollover amount;
- no rollover transfer when amount <= 0.

### Repository tests

- Firebase config mapping;
- demo repository parity;
- report range query does not truncate at 5,000 recent transactions.

### UI smoke tests

- enable salary cycle and preview dates;
- change payday and preview recalculates;
- Home renders salary-cycle range;
- Reports switches between salary cycle and calendar month;
- end-of-cycle savings action uses existing transfer flow.

## 20. Implementation decomposition

This design should be implemented as independent reviewable workstreams:

1. **Salary cycle domain engine** — model, calculator, validation, tests.
2. **Persistence** — repository, Firebase/demo/DataStore integration, rules.
3. **Range-query reporting foundation** — remove `observeRecent(5_000)` dependency and add period queries.
4. **Settings/Profile configuration UI** — shared component tree.
5. **Home + Reports integration** — active cycle and previous-cycle comparison.
6. **Budget integration** — calendar vs salary-cycle basis.
7. **Rollover/savings workflow** — confirmation + existing transfer use case.
8. **Docs/test synchronization** — BA_SPEC, DATA_SPEC, UI_SPEC, CONTEXT, PLAN, BACKLOG, HANDOVER.

Theme-wide refactor, Firebase wallet hardening, reminder deduplication and general bug audit are separate architectural workstreams and should receive their own specs/plans after this salary-cycle subsystem is accepted.

## 21. Acceptance criteria

The feature is accepted when:

- a user can enable salary-cycle mode and configure a payday;
- FinLux calculates correct cycles for all month lengths;
- Home and Reports can display the active salary cycle;
- previous-cycle comparison is deterministic;
- calendar-month reports remain available;
- salary-cycle budgets can use the same boundaries;
- leftover savings handling never changes balances without the existing atomic transfer path;
- Android and backend use consistent timezone/boundary semantics;
- report results are not capped by `observeRecent(5_000)`;
- all new domain rules have unit tests;
- all modified project documentation is synchronized before release.
