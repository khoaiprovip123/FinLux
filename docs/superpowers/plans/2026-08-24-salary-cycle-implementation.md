# FinLux Salary Cycle Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a deterministic payday-based financial cycle to FinLux so Home, Reports and Budget can operate on salary periods such as `25/08 -> 24/09`, while preserving calendar-month behavior and the existing atomic wallet transaction flow.

**Architecture:** Introduce a pure domain salary-cycle engine and a user-owned configuration repository, then add a generic transaction range query as the data foundation. Presentation modules consume one shared cycle contract; salary-cycle UI uses shared components/tokens rather than new Classic/Modern/Prism screen forks. Budget records gain explicit period metadata while legacy monthly documents remain readable.

**Tech Stack:** Kotlin 2.3.21, Jetpack Compose / Material 3, Hilt, Kotlin Coroutines/Flow, Firebase Auth/Firestore, JUnit 5, Android API 26-36.

**Spec:** `docs/superpowers/specs/2026-08-24-salary-cycle-design.md`

## Global Constraints

- Default finance timezone is exactly `Asia/Ho_Chi_Minh`.
- Salary-cycle intervals are half-open: `[start, endExclusive)`.
- Fixed payday supports integers `1..31`; invalid days are clamped to the last valid day of that month.
- Existing users start with salary-cycle mode disabled and existing budgets remain `CALENDAR_MONTH`.
- No transaction timestamp migration is allowed.
- No server/background process may transfer money automatically; user confirmation is required before the existing atomic transfer path is called.
- Every wallet balance mutation must continue through the existing Firestore Transaction flow.
- New UI must use `LocalFinluxTokens.current`, `MaterialTheme.colorScheme`, existing shared components and `FinluxStyleBackdrop`; do not add hardcoded UI colors.
- Do not create separate salary-cycle Classic/Modern/Prism implementations.
- Before implementation code, add a `[IN PROGRESS]` PRE-EXECUTION entry to `HANDOVER_LOG.md`; after implementation, record tests/files and mark it `[DONE]`.
- Do not update `CHANGELOG.md` or version metadata until the full unit test suite and APK build succeed.

---

## Task 1: Salary-cycle domain engine and finance timezone contract

**Files:**
- Create: `app/src/main/java/com/finlux/app/domain/model/SalaryCycleModels.kt`
- Create: `app/src/main/java/com/finlux/app/domain/usecase/SalaryCycleCalculator.kt`
- Create: `app/src/main/java/com/finlux/app/domain/usecase/ValidateSalaryCycleConfigUseCase.kt`
- Create: `app/src/main/java/com/finlux/app/domain/usecase/SalaryCycleUseCases.kt`
- Modify: `app/src/main/java/com/finlux/app/core/time/FinanceTime.kt`
- Create: `app/src/test/java/com/finlux/app/domain/usecase/SalaryCycleCalculatorTest.kt`
- Create: `app/src/test/java/com/finlux/app/domain/usecase/ValidateSalaryCycleConfigUseCaseTest.kt`
- Modify: `app/src/test/java/com/finlux/app/core/time/FinanceTimeTest.kt`
- Modify first, before Kotlin source edits: `HANDOVER_LOG.md`

**Interfaces:**
- Produces `SalaryCycleConfig`, `FinancialCycle`, `PaydayRuleType`, `CycleRolloverRule`, `BudgetPeriodBasis`.
- Produces `SalaryCycleCalculator.cycleContaining(...)` and `previousCycle(...)`.
- Produces `GetCurrentSalaryCycleUseCase` and `GetPreviousSalaryCycleUseCase` for presentation modules.

- [ ] **Step 1: Record PRE-EXECUTION state in HANDOVER_LOG**

Add a new top section with status `[IN PROGRESS]`, goal `Salary Cycle / Kỳ tài chính theo ngày nhận lương`, branch name, and the planned file groups from this implementation plan. Do not edit `CHANGELOG.md` yet.

- [ ] **Step 2: Write failing calculator and validation tests**

Use fixed Vietnam-zone instants. The test suite must include payday 1, 10, 15, 20, 25, 28, 29, 30, 31; leap/non-leap February; FIRST/LAST day rules; exact boundary; one millisecond before boundary; previous cycle; and midnight timezone behavior.

```kotlin
private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
private val calculator = DefaultSalaryCycleCalculator()

@Test
fun `payday 25 creates 25 Aug through before 25 Sep`() {
    val config = SalaryCycleConfig(enabled = true, paydayDay = 25)
    val instant = LocalDateTime.of(2026, 9, 10, 12, 0).atZone(zone).toInstant()
    val cycle = calculator.cycleContaining(instant, config, zone)

    assertEquals(LocalDate.of(2026, 8, 25), cycle.start.atZone(zone).toLocalDate())
    assertEquals(LocalDate.of(2026, 9, 25), cycle.endExclusive.atZone(zone).toLocalDate())
}

@Test
fun `payday 31 clamps February to final valid day`() {
    val config = SalaryCycleConfig(enabled = true, paydayDay = 31)
    val instant = LocalDateTime.of(2027, 2, 28, 12, 0).atZone(zone).toInstant()
    val cycle = calculator.cycleContaining(instant, config, zone)
    assertEquals(LocalDate.of(2027, 2, 28), cycle.start.atZone(zone).toLocalDate())
}

@Test
fun `move to savings requires destination wallet`() {
    val result = ValidateSalaryCycleConfigUseCase()(
        SalaryCycleConfig(enabled = true, rolloverRule = CycleRolloverRule.MOVE_TO_SAVINGS)
    )
    assertTrue(result is AppResult.Error)
}
```

- [ ] **Step 3: Run only the new domain tests and confirm RED**

Run:

```bash
./gradlew testDebugUnitTest --tests "*SalaryCycleCalculatorTest" --tests "*ValidateSalaryCycleConfigUseCaseTest"
```

Expected: compilation/test failure because the new salary-cycle types do not exist.

- [ ] **Step 4: Implement focused models and calculator**

Create the model with finance timezone stored in the config:

```kotlin
enum class PaydayRuleType { DAY_OF_MONTH, FIRST_DAY_OF_MONTH, LAST_DAY_OF_MONTH }
enum class CycleRolloverRule { KEEP_IN_WALLET, MOVE_TO_SAVINGS, ASK_EACH_CYCLE }
enum class BudgetPeriodBasis { CALENDAR_MONTH, SALARY_CYCLE }

data class SalaryCycleConfig(
    val enabled: Boolean = false,
    val paydayRuleType: PaydayRuleType = PaydayRuleType.DAY_OF_MONTH,
    val paydayDay: Int = 1,
    val salaryWalletId: String? = null,
    val savingsWalletId: String? = null,
    val expectedSalary: Money? = null,
    val rolloverRule: CycleRolloverRule = CycleRolloverRule.KEEP_IN_WALLET,
    val budgetPeriodBasis: BudgetPeriodBasis = BudgetPeriodBasis.CALENDAR_MONTH,
    val financeTimeZone: String = "Asia/Ho_Chi_Minh",
)

data class FinancialCycle(
    val start: Instant,
    val endExclusive: Instant,
    val label: String,
)
```

Implement one boundary helper and reuse it for current/previous cycle calculation:

```kotlin
private fun boundary(month: YearMonth, config: SalaryCycleConfig, zoneId: ZoneId): Instant {
    val day = when (config.paydayRuleType) {
        PaydayRuleType.FIRST_DAY_OF_MONTH -> 1
        PaydayRuleType.LAST_DAY_OF_MONTH -> month.lengthOfMonth()
        PaydayRuleType.DAY_OF_MONTH -> config.paydayDay.coerceIn(1, month.lengthOfMonth())
    }
    return month.atDay(day).atStartOfDay(zoneId).toInstant()
}
```

Change `FinanceTime.defaultZone` so its stable default is `VIETNAM_ZONE`; code that needs another zone must pass it explicitly. Add `FinanceTime.zoneOf(id: String)` that safely falls back to `VIETNAM_ZONE`.

- [ ] **Step 5: Run tests and commit**

Run:

```bash
./gradlew testDebugUnitTest --tests "*SalaryCycleCalculatorTest" --tests "*ValidateSalaryCycleConfigUseCaseTest" --tests "*FinanceTimeTest"
```

Expected: PASS.

Commit:

```bash
git add HANDOVER_LOG.md app/src/main/java/com/finlux/app/domain/model/SalaryCycleModels.kt app/src/main/java/com/finlux/app/domain/usecase/SalaryCycleCalculator.kt app/src/main/java/com/finlux/app/domain/usecase/ValidateSalaryCycleConfigUseCase.kt app/src/main/java/com/finlux/app/domain/usecase/SalaryCycleUseCases.kt app/src/main/java/com/finlux/app/core/time/FinanceTime.kt app/src/test/java/com/finlux/app/domain/usecase app/src/test/java/com/finlux/app/core/time/FinanceTimeTest.kt
git commit -m "feat(salary-cycle): add payday period domain engine"
```

---

## Task 2: Salary-cycle persistence, Firebase mapping and security rules

**Files:**
- Create: `app/src/main/java/com/finlux/app/domain/repository/SalaryCycleRepository.kt`
- Create: `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseSalaryCycleMapper.kt`
- Create: `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseSalaryCycleRepository.kt`
- Modify: `app/src/main/java/com/finlux/app/data/demo/DemoFinluxRepository.kt`
- Modify: `app/src/main/java/com/finlux/app/data/di/RepositoryModule.kt`
- Modify: `firestore.rules`
- Create: `app/src/test/java/com/finlux/app/data/remote/firebase/FirebaseSalaryCycleMapperTest.kt`

**Interfaces:**
- Produces `SalaryCycleRepository.observeConfig(): Flow<SalaryCycleConfig>`.
- Produces `SalaryCycleRepository.saveConfig(config): AppResult<Unit>`.
- Firestore path is exactly `users/{uid}/financialPreferences/salaryCycle`.

- [ ] **Step 1: Write mapper and repository contract tests first**

```kotlin
@Test
fun `mapper round trips salary cycle config`() {
    val source = SalaryCycleConfig(
        enabled = true,
        paydayRuleType = PaydayRuleType.DAY_OF_MONTH,
        paydayDay = 25,
        salaryWalletId = "salary-wallet",
        savingsWalletId = "saving-wallet",
        expectedSalary = Money(20_000_000),
        rolloverRule = CycleRolloverRule.ASK_EACH_CYCLE,
        budgetPeriodBasis = BudgetPeriodBasis.SALARY_CYCLE,
    )
    assertEquals(source, SalaryCycleFirestoreMapper.fromMap(SalaryCycleFirestoreMapper.toMap(source)))
}
```

Also verify missing fields map to disabled/default config for existing users.

- [ ] **Step 2: Run mapper test and confirm RED**

```bash
./gradlew testDebugUnitTest --tests "*FirebaseSalaryCycleMapperTest"
```

Expected: FAIL because mapper/repository do not exist.

- [ ] **Step 3: Implement repository, Firebase adapter, demo parity and DI**

Repository contract:

```kotlin
interface SalaryCycleRepository {
    fun observeConfig(): Flow<SalaryCycleConfig>
    suspend fun saveConfig(config: SalaryCycleConfig): AppResult<Unit>
}
```

The Firebase repository listens to the single `salaryCycle` document and maps a missing document to `SalaryCycleConfig()`. Saving uses `set(..., SetOptions.merge())` plus `FieldValue.serverTimestamp()` for `updatedAt`. `DemoFinluxRepository` stores the same config in a `MutableStateFlow` so Firebase-disabled mode has identical behavior.

Add a `provideSalaryCycleRepository(...)` provider in `FinanceRepositoryModule` following the same Firebase/demo switch used for wallet/budget repositories.

- [ ] **Step 4: Add restrictive Firestore rules for financialPreferences**

Add a specific rule under `users/{uid}` for `financialPreferences/{preferenceId}` and only allow `preferenceId == "salaryCycle"`. Validate allowed keys, enum values, `paydayDay`, positive integer expected salary when present, string wallet IDs when present, boolean enabled, and timezone string length `1..64`. Do not change wallet/transaction permissions in this task.

- [ ] **Step 5: Run tests and commit**

```bash
./gradlew testDebugUnitTest --tests "*FirebaseSalaryCycleMapperTest"
```

Expected: PASS.

Commit:

```bash
git add app/src/main/java/com/finlux/app/domain/repository/SalaryCycleRepository.kt app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseSalaryCycleMapper.kt app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseSalaryCycleRepository.kt app/src/main/java/com/finlux/app/data/demo/DemoFinluxRepository.kt app/src/main/java/com/finlux/app/data/di/RepositoryModule.kt app/src/test/java/com/finlux/app/data/remote/firebase/FirebaseSalaryCycleMapperTest.kt firestore.rules
git commit -m "feat(salary-cycle): persist user payday configuration"
```

---

## Task 3: Generic transaction range query and removal of the 5,000-report cap

**Files:**
- Modify: `app/src/main/java/com/finlux/app/domain/repository/FinanceRepositories.kt`
- Modify: `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseTransactionRepository.kt`
- Modify: `app/src/main/java/com/finlux/app/data/demo/DemoFinluxRepository.kt`
- Modify: `app/src/test/java/com/finlux/app/data/remote/firebase/FirebaseTransactionRepositoryTest.kt`

**Interfaces:**
- Produces `TransactionRepository.observeRange(startInclusive: Instant, endExclusive: Instant)`.
- Keeps `observeRecent()` and `observeMonth()` for existing callers.

- [ ] **Step 1: Add a failing repository-contract test**

Add a test proving the half-open range semantics: a transaction at `startInclusive` is returned; a transaction at `endExclusive` is excluded. Also add a 5,001-item fake-data case so downstream reporting can consume more than 5,000 entries without calling `observeRecent(5_000)`.

- [ ] **Step 2: Run the repository test and confirm RED**

```bash
./gradlew testDebugUnitTest --tests "*FirebaseTransactionRepositoryTest"
```

Expected: compilation failure because `observeRange` is absent.

- [ ] **Step 3: Implement the range query**

Contract:

```kotlin
fun observeRange(
    startInclusive: Instant,
    endExclusive: Instant,
): Flow<List<FinanceTransaction>>
```

Firebase query uses the existing `date` field:

```kotlin
collection
    .whereGreaterThanOrEqualTo("date", Timestamp(Date.from(startInclusive)))
    .whereLessThan("date", Timestamp(Date.from(endExclusive)))
    .orderBy("date", Query.Direction.DESCENDING)
```

Demo mode filters with:

```kotlin
it.date >= startInclusive && it.date < endExclusive
```

- [ ] **Step 4: Run tests and commit**

```bash
./gradlew testDebugUnitTest --tests "*FirebaseTransactionRepositoryTest"
```

Expected: PASS.

Commit:

```bash
git add app/src/main/java/com/finlux/app/domain/repository/FinanceRepositories.kt app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseTransactionRepository.kt app/src/main/java/com/finlux/app/data/demo/DemoFinluxRepository.kt app/src/test/java/com/finlux/app/data/remote/firebase/FirebaseTransactionRepositoryTest.kt
git commit -m "fix(reports): add uncapped transaction range query"
```

---

## Task 4: Reports salary-cycle period and previous-cycle comparison

**Files:**
- Create: `app/src/main/java/com/finlux/app/presentation/reports/ReportPeriodResolver.kt`
- Modify: `app/src/main/java/com/finlux/app/presentation/reports/ReportsViewModel.kt`
- Create: `app/src/main/java/com/finlux/app/presentation/reports/SalaryCycleReportPeriodSelector.kt`
- Modify: `app/src/main/java/com/finlux/app/presentation/reports/classic/ClassicReportsScreen.kt`
- Modify: `app/src/main/java/com/finlux/app/presentation/reports/modern/ModernReportsScreen.kt`
- Modify: `app/src/main/java/com/finlux/app/presentation/reports/prism/PrismReportsScreen.kt`
- Create: `app/src/test/java/com/finlux/app/presentation/reports/ReportPeriodResolverTest.kt`
- Create: `app/src/test/java/com/finlux/app/presentation/reports/ReportsViewModelTest.kt`

**Interfaces:**
- `ReportPeriod` gains `SALARY_CYCLE("Kỳ lương")`.
- Resolver produces current and previous half-open query windows.
- ReportsViewModel must use `observeRange()` rather than `observeRecent(5_000)`.

- [ ] **Step 1: Write resolver and ViewModel tests**

Test payday 25 at 2026-09-10 resolves current `2026-08-25..2026-09-25` and previous `2026-07-25..2026-08-25`. In the ViewModel test, use a fake `TransactionRepository` whose `observeRecent()` throws `AssertionError`; emit 5,001 transactions from `observeRange()` and assert the report sees all qualifying rows.

- [ ] **Step 2: Run Reports tests and confirm RED**

```bash
./gradlew testDebugUnitTest --tests "*ReportPeriodResolverTest" --tests "*ReportsViewModelTest"
```

Expected: FAIL before SALARY_CYCLE/range resolver exist.

- [ ] **Step 3: Implement report window resolution and reactive range query**

Resolve one combined query window from `previous.start` through `current.endExclusive`, then split returned transactions into previous/current groups. This avoids two Firestore listeners for one report screen.

For calendar periods, derive explicit start/end instants using the configured finance timezone rather than `ZoneId.systemDefault()`.

- [ ] **Step 4: Use one shared period selector in all three report styles**

`SalaryCycleReportPeriodSelector` receives only `selected`, `salaryCycleEnabled`, and `onSelect`. It renders the available period labels from domain-neutral presentation data using `LocalFinluxTokens.current`; all three legacy report screens call this shared component instead of creating salary-cycle-specific controls.

- [ ] **Step 5: Run tests and commit**

```bash
./gradlew testDebugUnitTest --tests "*ReportPeriodResolverTest" --tests "*ReportsViewModelTest"
```

Expected: PASS, including 5,001-item case.

Commit:

```bash
git add app/src/main/java/com/finlux/app/presentation/reports app/src/test/java/com/finlux/app/presentation/reports
git commit -m "feat(reports): add salary-cycle reporting"
```

---

## Task 5: Shared Settings/Profile configuration flow

**Files:**
- Create: `app/src/main/java/com/finlux/app/presentation/settings/salarycycle/SalaryCycleSettingsViewModel.kt`
- Create: `app/src/main/java/com/finlux/app/presentation/settings/salarycycle/SalaryCycleSettingsScreen.kt`
- Modify: `app/src/main/java/com/finlux/app/core/navigation/Routes.kt`
- Modify: `app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt`
- Modify: `app/src/main/java/com/finlux/app/presentation/settings/SettingsScreen.kt`
- Modify: `app/src/main/java/com/finlux/app/presentation/settings/prism/PrismSettingsScreen.kt`
- Create: `app/src/test/java/com/finlux/app/presentation/settings/SalaryCycleSettingsViewModelTest.kt`

**Interfaces:**
- Adds `Route.SalaryCycleSettings` with value `salary-cycle-settings`.
- Produces one shared SalaryCycleSettingsScreen for every UI style.

- [ ] **Step 1: Write ViewModel tests**

Verify initial config loads, payday edits recalculate current/next previews, invalid day cannot save, MOVE_TO_SAVINGS requires a savings wallet, and successful save calls `SalaryCycleRepository.saveConfig()` once.

- [ ] **Step 2: Run the Settings test and confirm RED**

```bash
./gradlew testDebugUnitTest --tests "*SalaryCycleSettingsViewModelTest"
```

Expected: FAIL because the ViewModel does not exist.

- [ ] **Step 3: Implement ViewModel and shared screen**

The ViewModel combines `SalaryCycleRepository.observeConfig()` and `WalletRepository.observeWallets()`, then calculates preview cycles with `FinanceClock.now()` and `FinanceTime.zoneOf(config.financeTimeZone)`.

Screen fields:

```text
Bật kỳ lương
Quy tắc ngày nhận lương
Ngày nhận lương (1-31, only for DAY_OF_MONTH)
Ví nhận lương
Thu nhập dự kiến
Chu kỳ ngân sách: Tháng / Kỳ lương
Xử lý tiền dư
Ví tiết kiệm (when needed)
Kỳ hiện tại preview
Kỳ tiếp theo preview
```

Use `FinluxStyleBackdrop`, existing header/input/card components and tokens. Do not add a second/third themed screen.

- [ ] **Step 4: Wire navigation from both existing Settings containers**

Add one row labeled `Kỳ tài chính theo lương` in the non-Prism settings and Prism settings; both call `onNavigate(Route.SalaryCycleSettings.value)`. Add the destination to `FinluxNavHost` with normal back navigation.

- [ ] **Step 5: Run tests and commit**

```bash
./gradlew testDebugUnitTest --tests "*SalaryCycleSettingsViewModelTest" --tests "*MainSwipeNavigationTest"
```

Expected: PASS.

Commit:

```bash
git add app/src/main/java/com/finlux/app/presentation/settings app/src/main/java/com/finlux/app/core/navigation app/src/test/java/com/finlux/app/presentation/settings
git commit -m "feat(settings): add salary-cycle configuration"
```

---

## Task 6: Home salary-cycle summary and comparison

**Files:**
- Create: `app/src/main/java/com/finlux/app/domain/usecase/CalculateCycleSummaryUseCase.kt`
- Create: `app/src/main/java/com/finlux/app/presentation/home/SalaryCycleContextCard.kt`
- Modify: `app/src/main/java/com/finlux/app/presentation/home/HomeViewModel.kt`
- Modify: `app/src/main/java/com/finlux/app/presentation/home/classic/ClassicHomeScreen.kt`
- Modify: `app/src/main/java/com/finlux/app/presentation/home/modern/ModernHomeScreen.kt`
- Modify: `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`
- Modify: `app/src/test/java/com/finlux/app/presentation/home/HomeViewModelTest.kt`
- Create: `app/src/test/java/com/finlux/app/domain/usecase/CalculateCycleSummaryUseCaseTest.kt`

**Interfaces:**
- HomeUiState gains `salaryCycleEnabled`, `financialCycle`, `previousCycleIncome`, `previousCycleExpense`, `daysRemaining`, and `projectedRemaining`.
- `CalculateCycleSummaryUseCase` counts only INCOME/EXPENSE; transfers remain excluded.

- [ ] **Step 1: Write summary and HomeViewModel failing tests**

Test transfer exclusion, previous-cycle comparison, and disabled salary-cycle fallback to the existing calendar-month behavior.

- [ ] **Step 2: Run Home tests and confirm RED**

```bash
./gradlew testDebugUnitTest --tests "*CalculateCycleSummaryUseCaseTest" --tests "*HomeViewModelTest"
```

- [ ] **Step 3: Refactor Home financial overview to consume a resolved active range**

When salary cycle is enabled, use `observeRange(current.start, current.endExclusive)` plus the previous cycle range. When disabled, preserve existing `observeMonth(YearMonth)` behavior. Continue using `observeRecent()` only for the recent-transactions list, not financial summary calculations.

Projection is conservative and division-safe:

```kotlin
projectedRemaining = if (elapsedDays <= 0 || totalDays <= 0) currentNet
else currentIncome - ((currentExpense.toDouble() / elapsedDays) * totalDays).toLong()
```

- [ ] **Step 4: Insert the same SalaryCycleContextCard in all Home styles**

The shared card renders `Kỳ lương hiện tại`, date range, days remaining, net flow and previous-cycle delta. Each existing Home style only decides placement; visual behavior lives in the shared component and its tokens.

- [ ] **Step 5: Run tests and commit**

```bash
./gradlew testDebugUnitTest --tests "*CalculateCycleSummaryUseCaseTest" --tests "*HomeViewModelTest"
```

Expected: PASS.

Commit:

```bash
git add app/src/main/java/com/finlux/app/domain/usecase/CalculateCycleSummaryUseCase.kt app/src/main/java/com/finlux/app/presentation/home app/src/test/java/com/finlux/app/domain/usecase/CalculateCycleSummaryUseCaseTest.kt app/src/test/java/com/finlux/app/presentation/home/HomeViewModelTest.kt
git commit -m "feat(home): show active salary-cycle cash flow"
```

---

## Task 7: Salary-cycle budget records and period materialization

**Files:**
- Modify: `app/src/main/java/com/finlux/app/domain/model/FinanceModels.kt`
- Modify: `app/src/main/java/com/finlux/app/domain/repository/FinanceRepositories.kt`
- Create: `app/src/main/java/com/finlux/app/domain/usecase/MaterializeSalaryCycleBudgetsUseCase.kt`
- Modify: `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseBudgetRepository.kt`
- Modify: `app/src/main/java/com/finlux/app/data/demo/DemoFinluxRepository.kt`
- Modify: `app/src/main/java/com/finlux/app/presentation/budget/BudgetViewModel.kt`
- Modify the existing Budget screen variants under `presentation/budget/classic`, `modern`, `prism` only to consume a shared period header/control.
- Create: `app/src/main/java/com/finlux/app/presentation/budget/BudgetPeriodHeader.kt`
- Modify: `app/src/test/java/com/finlux/app/presentation/budget/BudgetViewModelTest.kt`
- Create: `app/src/test/java/com/finlux/app/domain/usecase/MaterializeSalaryCycleBudgetsUseCaseTest.kt`

**Interfaces:**
- Budget gains `periodBasis`, `periodStart`, `periodEndExclusive` with backward-compatible defaults.
- BudgetRepository gains `observeSalaryCycleBudgets(cycleStart: Instant)`.

- [ ] **Step 1: Write migration/materialization tests**

Verify a legacy budget with no period fields maps to `CALENDAR_MONTH`; salary-cycle materialization copies the previous cycle limit to deterministic current-cycle IDs but resets `spentAmount`, `notified80`, and `notified100`.

Deterministic ID format:

```text
{categoryId}_salary_{yyyy-MM-dd}
```

where date is the cycle start in the configured finance timezone.

- [ ] **Step 2: Run budget tests and confirm RED**

```bash
./gradlew testDebugUnitTest --tests "*BudgetViewModelTest" --tests "*MaterializeSalaryCycleBudgetsUseCaseTest"
```

- [ ] **Step 3: Extend Budget mapping/repository without breaking monthly documents**

Add defaults:

```kotlin
val periodBasis: BudgetPeriodBasis = BudgetPeriodBasis.CALENDAR_MONTH,
val periodStart: Instant? = null,
val periodEndExclusive: Instant? = null,
```

Monthly query remains unchanged. Salary-cycle query filters `periodBasis == "salary_cycle"` and the deterministic cycle-start key/field. Existing docs missing fields parse as monthly.

- [ ] **Step 4: Make BudgetViewModel switch basis from SalaryCycleConfig**

When `budgetPeriodBasis == SALARY_CYCLE` and salary cycle is enabled, calculate the active cycle, materialize current limits once when needed, observe cycle budgets, and compute dynamic spent values from `TransactionRepository.observeRange`. When disabled/monthly, retain current YearMonth navigation and logic.

The shared `BudgetPeriodHeader` displays either month navigation or salary-cycle range; style variants reuse it.

- [ ] **Step 5: Run tests and commit**

```bash
./gradlew testDebugUnitTest --tests "*BudgetViewModelTest" --tests "*MaterializeSalaryCycleBudgetsUseCaseTest" --tests "*GetBudgetStatusUseCaseTest"
```

Expected: PASS.

Commit:

```bash
git add app/src/main/java/com/finlux/app/domain/model/FinanceModels.kt app/src/main/java/com/finlux/app/domain/repository/FinanceRepositories.kt app/src/main/java/com/finlux/app/domain/usecase/MaterializeSalaryCycleBudgetsUseCase.kt app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseBudgetRepository.kt app/src/main/java/com/finlux/app/data/demo/DemoFinluxRepository.kt app/src/main/java/com/finlux/app/presentation/budget app/src/test/java/com/finlux/app/presentation/budget app/src/test/java/com/finlux/app/domain/usecase/MaterializeSalaryCycleBudgetsUseCaseTest.kt
git commit -m "feat(budget): support payday-based budget periods"
```

---

## Task 8: End-of-cycle leftover and user-confirmed savings transfer

**Files:**
- Create: `app/src/main/java/com/finlux/app/domain/usecase/CalculateCycleRolloverUseCase.kt`
- Create: `app/src/main/java/com/finlux/app/presentation/home/SalaryCycleRolloverCard.kt`
- Modify: `app/src/main/java/com/finlux/app/presentation/home/HomeViewModel.kt`
- Modify: `app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt` only if an existing transfer request key must be exposed to Home.
- Create: `app/src/test/java/com/finlux/app/domain/usecase/CalculateCycleRolloverUseCaseTest.kt`
- Modify: `app/src/test/java/com/finlux/app/presentation/home/HomeViewModelTest.kt`

**Interfaces:**
- Reuses existing `TransferMoneyUseCase`; no direct wallet repository balance write is introduced.
- Goal deposits and debt payments are already represented as EXPENSE ledger transactions, so the rollover calculation must not subtract them a second time.

- [ ] **Step 1: Write rollover tests**

```kotlin
@Test
fun `positive completed cycle net becomes rollover candidate`() {
    val amount = CalculateCycleRolloverUseCase()(income = 20_000_000, expense = 15_500_000)
    assertEquals(4_500_000L, amount)
}

@Test
fun `negative cycle net does not create savings candidate`() {
    assertEquals(0L, CalculateCycleRolloverUseCase()(10_000_000, 12_000_000))
}
```

Also test KEEP_IN_WALLET produces no action and MOVE_TO_SAVINGS still requires explicit confirmation.

- [ ] **Step 2: Run rollover tests and confirm RED**

```bash
./gradlew testDebugUnitTest --tests "*CalculateCycleRolloverUseCaseTest"
```

- [ ] **Step 3: Implement candidate state and shared confirmation card**

Only the most recently completed cycle is eligible. Show a non-blocking card when amount > 0 and rule is ASK_EACH_CYCLE or MOVE_TO_SAVINGS. The action opens/reuses the current wallet transfer flow with source=`salaryWalletId`, destination=`savingsWalletId`, amount prefilled; no transfer happens until the user confirms that existing flow.

- [ ] **Step 4: Run tests and commit**

```bash
./gradlew testDebugUnitTest --tests "*CalculateCycleRolloverUseCaseTest" --tests "*HomeViewModelTest"
```

Expected: PASS.

Commit:

```bash
git add app/src/main/java/com/finlux/app/domain/usecase/CalculateCycleRolloverUseCase.kt app/src/main/java/com/finlux/app/presentation/home app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt app/src/test/java/com/finlux/app/domain/usecase/CalculateCycleRolloverUseCaseTest.kt app/src/test/java/com/finlux/app/presentation/home/HomeViewModelTest.kt
git commit -m "feat(salary-cycle): add confirmed savings rollover"
```

---

## Task 9: UI smoke coverage, documentation synchronization and release verification

**Files:**
- Modify if Compose UI test dependencies are absent: `app/build.gradle.kts`
- Create: `app/src/androidTest/java/com/finlux/app/presentation/settings/SalaryCycleSettingsScreenTest.kt`
- Create: `app/src/androidTest/java/com/finlux/app/presentation/reports/SalaryCycleReportsSmokeTest.kt`
- Modify: `docs/BA_SPEC.md`
- Modify: `docs/DATA_SPEC.md`
- Modify: `docs/UI_SPEC.md`
- Modify: `docs/CONTEXT.md`
- Modify: `docs/PLAN.md`
- Modify: `docs/BACKLOG.md`
- Modify: `README.md`
- Modify after all tests/build pass: `HANDOVER_LOG.md`
- Modify after all tests/build pass: `CHANGELOG.md`
- Modify after all tests/build pass: `app/build.gradle.kts`

**Interfaces:**
- No new product API; this task proves and documents the feature.

- [ ] **Step 1: Add Compose smoke tests**

Cover exactly these flows:

1. enable salary cycle and preview dates;
2. change payday and preview recalculates;
3. Reports switch `Kỳ lương -> Tháng` without losing data;
4. disabled salary-cycle mode hides/disables salary-cycle period selection;
5. rollover card invokes the transfer callback, not a wallet mutation.

If missing, add Compose UI test dependencies using the existing Compose BOM rather than hardcoding a new version.

- [ ] **Step 2: Run complete unit suite**

```bash
./gradlew testDebugUnitTest
```

Expected: 100% PASS. Record the exact pass/fail count in `HANDOVER_LOG.md`; do not invent it.

- [ ] **Step 3: Run Android instrumentation smoke suite**

With an emulator/device connected:

```bash
./gradlew connectedDebugAndroidTest
```

Expected: PASS for the salary-cycle smoke tests.

- [ ] **Step 4: Build debug APK before release metadata changes**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL` and a generated debug APK.

- [ ] **Step 5: Synchronize product/data/UI docs**

Document:

- new salary-cycle use cases and business rules in `BA_SPEC.md`;
- `financialPreferences/salaryCycle`, new budget period fields and indexes/rules in `DATA_SPEC.md`;
- Settings, Home, Reports, Budget and rollover states in `UI_SPEC.md`;
- salary-cycle domain/repository/data-flow architecture in `CONTEXT.md`;
- completed/current tasks in `PLAN.md` and `BACKLOG.md`;
- README feature list and actual current dependency/version facts only.

- [ ] **Step 6: Final release verification and version bump**

Run again after docs/source are final:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Only after both succeed, bump `versionCode` from `109` to `110` and `versionName` from `1.9.1` to `1.10.0`, because salary-cycle is a backward-compatible feature release. Add `v1.10.0` to `CHANGELOG.md` with Added/Changed/Fixed entries and finish the HANDOVER entry as `[DONE]` with the actual test/build results.

- [ ] **Step 7: Commit the verified feature release**

```bash
git add app docs firestore.rules README.md HANDOVER_LOG.md CHANGELOG.md
git commit -m "bump(release): v1.10.0 - salary cycle financial periods"
```

---

## Deferred follow-up plans

The following findings remain intentionally outside this implementation so the salary-cycle feature can be reviewed and tested independently:

1. **Theme/UI architecture refactor:** collapse duplicated Classic/Modern/Prism screen trees toward one shared component tree plus token/style adapters; resolve overlap between `AppUiStyle` and `VisualStyle`; remove remaining hardcoded colors and split oversized UI files.
2. **Firebase hardening:** prevent a modified client from directly altering `wallet.balance` and strengthen integrity of derived budget fields.
3. **Reminder deduplication:** define one owner per notification type between local AlarmManager and Cloud Functions before adding salary-cycle push notifications.
4. **General bug audit:** systematic review of navigation, updater, auth, debt, goal, transfer, time handling, offline/retry and concurrency paths after the new feature lands.

Each deferred item should receive its own spec/plan and test gate rather than being mixed into the salary-cycle release.

## Plan self-review result

- Spec coverage: salary-cycle domain, persistence, range reporting, Settings, Home, Reports, Budget, rollover, Firebase rules, timezone, migration, tests and documentation are all mapped to a task.
- Placeholder scan: no implementation step relies on TBD/TODO/unspecified behavior.
- Type consistency: `SalaryCycleConfig`, `FinancialCycle`, `SalaryCycleRepository`, `SalaryCycleCalculator`, `BudgetPeriodBasis` and `TransactionRepository.observeRange` are introduced before consumers use them.
- Scope control: full theme refactor, wallet-balance rule hardening, reminder ownership and unrelated bug fixes remain separate workstreams.
