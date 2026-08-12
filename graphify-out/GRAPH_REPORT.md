# Graph Report - .  (2026-08-12)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 653 nodes · 1736 edges · 26 communities (23 shown, 3 thin omitted)
- Extraction: 99% EXTRACTED · 1% INFERRED · 0% AMBIGUOUS · INFERRED: 23 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `4f2854c7`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- RoundedCornerShape
- FinanceTransaction
- UserProfile
- RepositoryModule.kt
- HomeScreen.kt
- Route
- AuthScreens.kt
- DemoFinluxRepository
- ReportsScreen.kt
- FirebaseReadRepository.kt
- BudgetViewModel.kt
- FinanceRepositories.kt
- AppResult
- Category
- Reminder
- AddTransactionViewModel.kt
- mainRouteAfterSwipe
- WalletsViewModel.kt
- SplashViewModel.kt
- gradlew
- FinluxApplication.kt
- FinanceRepositoryModule

## God Nodes (most connected - your core abstractions)
1. `AppResult` - 94 edges
2. `DemoFinluxRepository` - 51 edges
3. `FinanceTransaction` - 46 edges
4. `Category` - 39 edges
5. `Wallet` - 31 edges
6. `Money` - 31 edges
7. `UserProfile` - 29 edges
8. `Reminder` - 27 edges
9. `GlassCard()` - 26 edges
10. `FinluxNavHost()` - 26 edges

## Surprising Connections (you probably didn't know these)
- `SettingsLink()` --calls--> `GlassCard()`  [EXTRACTED]
  app/src/main/java/com/finlux/app/presentation/settings/SettingsScreen.kt → app/src/main/java/com/finlux/app/core/designsystem/LiquidGlass.kt
- `CategoryEditor()` --references--> `CategoryType`  [EXTRACTED]
  app/src/main/java/com/finlux/app/presentation/category/CategoriesScreen.kt → app/src/main/java/com/finlux/app/domain/model/FinanceModels.kt
- `walletIcon()` --references--> `WalletType`  [EXTRACTED]
  app/src/main/java/com/finlux/app/core/designsystem/FinanceIcons.kt → app/src/main/java/com/finlux/app/domain/model/FinanceModels.kt
- `ExpenseAnalytics()` --references--> `FinanceTransaction`  [EXTRACTED]
  app/src/main/java/com/finlux/app/presentation/home/HomeScreen.kt → app/src/main/java/com/finlux/app/domain/model/FinanceModels.kt
- `referenceTransactionIcon()` --references--> `FinanceTransaction`  [EXTRACTED]
  app/src/main/java/com/finlux/app/presentation/home/HomeScreen.kt → app/src/main/java/com/finlux/app/domain/model/FinanceModels.kt

## Import Cycles
- None detected.

## Communities (26 total, 3 thin omitted)

### Community 0 - "RoundedCornerShape"
Cohesion: 0.08
Nodes (68): categoryIcon(), colorFromHex(), FinanceIconOption, Color, ImageVector, walletIcon(), GradientHeroCard(), Color (+60 more)

### Community 1 - "FinanceTransaction"
Cohesion: 0.06
Nodes (36): balanceDelta(), FirebaseTransactionRepository, Flow, toFinanceTransaction(), toFirestoreMap(), userTransactions(), userWallets(), FinanceTransaction (+28 more)

### Community 2 - "UserProfile"
Cohesion: 0.06
Nodes (20): FirebaseAuthRepository, ByteArray, Flow, Uri, UserProfile, AuthRepository, ByteArray, Flow (+12 more)

### Community 3 - "RepositoryModule.kt"
Cohesion: 0.05
Nodes (47): FinluxTheme(), GlassTokens, bindReminderScheduler(), bindThemeRepository(), bindUiPreferencesRepository(), DataStoreThemePreferenceRepository, Flow, AlarmReminderScheduler (+39 more)

### Community 4 - "HomeScreen.kt"
Cohesion: 0.08
Nodes (47): FinluxBrandMark(), Dp, Modifier, FinluxUserAvatar(), Dp, Modifier, DynamicGradientBackdrop(), FinluxStyleBackdrop() (+39 more)

### Community 5 - "Route"
Cohesion: 0.11
Nodes (21): GlassFab(), Budget, Categories, Expense, ForgotPassword, Home, Income, Login (+13 more)

### Community 6 - "AuthScreens.kt"
Cohesion: 0.09
Nodes (27): AuthHeaderSection(), AuthMode, FORGOT, LOGIN, REGISTER, AuthModeTabs(), AuthScreen(), FinluxInput() (+19 more)

### Community 7 - "DemoFinluxRepository"
Cohesion: 0.14
Nodes (6): DemoFinluxRepository, Budget, ByteArray, Flow, YearMonth, Money

### Community 8 - "ReportsScreen.kt"
Cohesion: 0.14
Nodes (25): CashFlowChart(), CategoryBlock(), EmptyChartText(), ExpenseDistribution(), Color, Modifier, ReportAmount(), ReportPanel() (+17 more)

### Community 9 - "FirebaseReadRepository.kt"
Cohesion: 0.17
Nodes (13): FirebaseReadRepository, Budget, Flow, YearMonth, toBudget(), toBudgetMap(), toCategory(), toCategoryMap() (+5 more)

### Community 10 - "BudgetViewModel.kt"
Cohesion: 0.10
Nodes (14): Budget, BudgetLevel, EXCEEDED, SAFE, WARNING, BudgetStatus, GetBudgetStatusUseCase, Budget (+6 more)

### Community 11 - "FinanceRepositories.kt"
Cohesion: 0.15
Nodes (9): DashboardSummary, BudgetRepository, DashboardRepository, Budget, Flow, YearMonth, HomeUiState, HomeViewModel (+1 more)

### Community 12 - "AppResult"
Cohesion: 0.12
Nodes (12): AppResult, Error, T, Success, toWallet(), Wallet, WalletRepository, DeleteBudgetUseCase (+4 more)

### Community 13 - "Category"
Cohesion: 0.19
Nodes (7): Category, CategoryRepository, DeleteCategoryUseCase, SaveCategoryUseCase, CategoriesViewModel, CategoryActionState, ViewModel

### Community 14 - "Reminder"
Cohesion: 0.18
Nodes (7): Reminder, ReminderRepository, DeleteReminderUseCase, SaveReminderUseCase, ViewModel, RemindersUiState, RemindersViewModel

### Community 15 - "AddTransactionViewModel.kt"
Cohesion: 0.15
Nodes (7): CategoryType, EXPENSE, INCOME, AddTransactionUiState, AddTransactionViewModel, StateFlow, ViewModel

### Community 17 - "WalletsViewModel.kt"
Cohesion: 0.24
Nodes (4): TransferMoneyUseCase, ViewModel, WalletActionState, WalletsViewModel

### Community 18 - "SplashViewModel.kt"
Cohesion: 0.33
Nodes (6): ViewModel, SessionState, AUTHENTICATED, CHECKING, GUEST, SplashViewModel

### Community 19 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **62 isolated node(s):** `FinanceIconOption`, `Error`, `Budget`, `Categories`, `Expense` (+57 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **3 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `AppResult` connect `AppResult` to `FinanceTransaction`, `UserProfile`, `AuthScreens.kt`, `DemoFinluxRepository`, `FirebaseReadRepository.kt`, `BudgetViewModel.kt`, `FinanceRepositories.kt`, `Category`, `Reminder`, `AddTransactionViewModel.kt`, `WalletsViewModel.kt`?**
  _High betweenness centrality (0.186) - this node is a cross-community bridge._
- **Why does `Category` connect `Category` to `RoundedCornerShape`, `FinanceTransaction`, `RepositoryModule.kt`, `HomeScreen.kt`, `DemoFinluxRepository`, `ReportsScreen.kt`, `FirebaseReadRepository.kt`, `BudgetViewModel.kt`, `FinanceRepositories.kt`, `AppResult`, `Reminder`, `AddTransactionViewModel.kt`?**
  _High betweenness centrality (0.089) - this node is a cross-community bridge._
- **Why does `FinanceTransaction` connect `FinanceTransaction` to `RoundedCornerShape`, `RepositoryModule.kt`, `HomeScreen.kt`, `DemoFinluxRepository`, `ReportsScreen.kt`, `FinanceRepositories.kt`, `AppResult`, `AddTransactionViewModel.kt`?**
  _High betweenness centrality (0.087) - this node is a cross-community bridge._
- **What connects `FinanceIconOption`, `Error`, `Budget` to the rest of the system?**
  _62 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `RoundedCornerShape` be split into smaller, more focused modules?**
  _Cohesion score 0.07669703203056127 - nodes in this community are weakly interconnected._
- **Should `FinanceTransaction` be split into smaller, more focused modules?**
  _Cohesion score 0.05875251509054326 - nodes in this community are weakly interconnected._
- **Should `UserProfile` be split into smaller, more focused modules?**
  _Cohesion score 0.058699101004759384 - nodes in this community are weakly interconnected._