# FINLUX — UI/UX & USER FLOW BLUEPRINT FOR PRODUCT WEBSITE 2026

> This document translates the FinLux Android product into a detailed interaction model and then into a public website information architecture.
>
> It is intentionally more detailed than a marketing landing-page outline so that UI/UX designers, developers and AI coding agents can work from the same source.

---

## 1. UX principles

1. **Money first** — amount and financial meaning have higher hierarchy than decoration.
2. **One financial period everywhere** — Home, History and Reports never disagree about the selected period.
3. **Progressive disclosure** — daily actions are simple; advanced analysis appears only when requested.
4. **Traceability** — every KPI should drill down to the underlying wallet/category/transaction.
5. **No hidden accounting tricks** — internal transfers, savings reclassification, debt and deal principal have explicit semantics.
6. **Theme is presentation, not business logic** — Classic/Modern/Prism share state and navigation.
7. **Mobile ergonomics** — large touch targets, minimal cramped forms, safe IME/bottom-nav spacing.
8. **Immediate feedback** — saving, validation, sync and error states are visible.
9. **Non-destructive defaults** — destructive actions require clear confirmation.
10. **Vietnamese money behavior** — VND-first formatting, readable denomination entry, salary-cycle awareness.

---

## 2. Global app shell

### 2.1 Splash

Purpose:

- Determine authenticated session.
- Load local appearance.
- Initialize financial preferences.
- Avoid showing a false Home state before period/wallet data is ready.

States:

```text
Booting
→ Authenticated
→ Unauthenticated
→ Recoverable initialization error
```

### 2.2 Main navigation

Primary bottom bar:

- Home.
- Transactions.
- Reports.
- More/Profile.

Global behavior:

- Bottom bar remains fixed while root content transitions.
- Root swipe may move between main tabs if enabled.
- Child gestures take precedence when already consumed.
- Content includes bottom-bar safe padding.

---

## 3. Authentication flow

### Login

Layout hierarchy:

1. FinLux identity/logo.
2. Welcome title.
3. Email field.
4. Password field.
5. Primary Login CTA.
6. Google sign-in.
7. Forgot password.
8. Register link.

Validation:

- Email syntax.
- Empty password.
- Loading state.
- Firebase error mapping.
- Prevent double-submit.

### Register

1. Name.
2. Email.
3. Password.
4. Confirm password if used by current screen.
5. Create account.
6. Optional Google path.
7. Return/login link.

Post-register:

```text
Register success
→ Initialize user defaults
→ Default categories
→ Financial preferences
→ Home
```

---

## 4. Home screen detailed flow

### 4.1 Header

Left:

- Avatar.
- Contextual greeting.
- User display name.

Right:

- Notification button.
- Optional unread indicator.

Header should not be wrapped in a visually heavy container in Prism direction.

### 4.2 Primary financial pager

Four pages:

#### Page A — Balance

Shows:

- Current owned liquid balance / defined balance metric.
- Period/status context as appropriate.
- Distinct balance vector illustration.

Primary question:

> “I have how much money now?”

#### Page B — Income

Shows:

- Income in current selected financial period.
- Comparison if available.

Question:

> “How much came in this period?”

#### Page C — Expense

Shows:

- Expense in current period.
- Comparison/indicator.

Question:

> “How much went out?”

#### Page D — Net Cash Flow

Formula:

```text
True period cash inflow - true period cash outflow
```

Does not inflate values using internal transfer.

Question:

> “Did my available money grow or shrink this period?”

### Interaction

- Horizontal swipe.
- Page indicator inside lower card region.
- Auto-advance after inactivity where enabled.
- Any manual interaction resets auto-advance.
- Amount must not collide with decorative vector graphic.

### 4.3 Quick actions

Recommended actions:

- Add expense.
- Add income.
- Transfer.
- Additional actions through overflow/expanded add menu.

Rules:

- Expense/Income are most frequent and must remain fastest.
- Transfer is visually distinct.
- Advanced actions such as goal creation should not crowd primary CTA space.

### 4.4 Budget status

Card can show:

- Category or overall alert.
- Used percentage.
- Remaining amount.
- Threshold state.

Tap:

```text
Home budget card
→ Budget screen
→ Selected period preserved
```

### 4.5 Saving Spin launcher

Only visible if feature enabled.

States:

- READY → prominent “Spin”.
- SPUN_PENDING → show selected amount and confirm context.
- SNOOZED → next reminder time.
- COMPLETED → saved amount + streak.

Tap opens game sheet directly.

### 4.6 Recent transactions

Grouped/compact list:

- Category icon.
- Note/title.
- Wallet.
- Time.
- Signed amount.
- Transfer-specific semantics.

Tap → transaction detail/edit.

“See all” → Transactions screen.

---

## 5. Add transaction flow

### 5.1 Entry

From:

- Home quick action.
- FAB.
- Transactions.
- Notification quick-pay where supported.

### 5.2 Sheet/screen structure

1. Transaction type selector.
2. Large amount input.
3. Category picker.
4. Wallet picker.
5. Date/time.
6. Note.
7. Receipt/attachment optional.
8. Save CTA.

For Transfer:

1. Source wallet.
2. Destination wallet.
3. Amount.
4. Date/note.
5. Confirm.

### 5.3 Amount input

- Largest form element.
- Numeric keyboard.
- Clear VND grouping.
- Reject invalid/zero/negative according to transaction semantics.
- Avoid tiny inline controls.

### 5.4 Picker behavior

Wallet picker:

- Search if list is long.
- Institution logo where available.
- Name + current balance.
- Selected state obvious.

Category picker:

- Filter based on Income/Expense.
- Icon + name.
- User categories and defaults.
- Add category path where appropriate.

### 5.5 Save lifecycle

```text
Input
→ Local validation
→ Saving state
→ Repository/domain validation
→ Firebase transaction/write
→ Success
→ UI state refreshed
```

On failure:

- Do not dismiss silently.
- Preserve user input.
- Show actionable error.

---

## 6. Transactions screen

### Header

- Title.
- Period/filter access.
- Search.

### Filters

- Period.
- Type.
- Wallet.
- Category.
- Search text.

### List

Group by date:

```text
TODAY
  transaction
  transaction
YESTERDAY
  transaction
...
```

Row hierarchy:

1. Icon/category.
2. Main text/note.
3. Secondary wallet/time.
4. Amount.
5. Optional action affordance.

Layout rule:

- Left information column uses flexible width.
- Right amount/action column remains stable.
- Long text max-lines and ellipsis.
- Dark/light contrast parity.

---

## 7. Wallet screen

### Overview

Hero:

- Total wallet balance.
- Number of active wallets.

Wallet cards:

- Institution/type.
- Wallet name.
- Balance.
- Status.
- Quick transfer/detail.

### Create wallet

Fields:

- Name.
- Type.
- Institution.
- Opening balance.
- Optional icon/visual metadata.

### Wallet detail

- Current balance.
- Period movement.
- Income.
- Expense.
- Transfer in/out.
- Transaction list.

This same data should power wallet reports.

---

## 8. Salary Cycle settings

### Entry

Settings → Financial Preferences → Salary Cycle.

### Screen/sheet

1. Enable switch.
2. Payday day.
3. Payday rule type.
4. Current period preview.
5. Previous period preview.
6. Save.

### UX requirement

A user must be able to understand exactly what date range will be used before saving.

Example preview:

```text
Current cycle: 25 Aug → 24 Sep
Previous cycle: 25 Jul → 24 Aug
```

When enabled, all period-aware screens must surface “Salary Cycle” clearly.

---

## 9. Budget screen

### Summary

- Current period.
- Total planned.
- Total spent.
- Remaining.
- Budget health.

### Category budget card

- Category.
- Limit.
- Spent.
- Remaining.
- Progress bar.
- Warning/exceeded state.

### Add/edit budget

- Category.
- Limit amount.
- Period.
- Optional copy previous.

### Drill down

Tap category budget:

```text
Budget card
→ Category detail
→ Transactions contributing to spend
```

---

## 10. Reports UX

Reports use progressive disclosure.

### 10.1 Report top

- Period selector.
- Current period label.
- Previous period comparison.

### 10.2 Executive summary

Cards:

- Opening balance.
- Closing balance.
- Income.
- Expense.
- Net cash flow.

### 10.3 Trend

Line/area visualization:

- Inflow.
- Outflow.
- Net movement.

Interaction:

- Tap/drag tooltip.
- Selected date reveals values.

### 10.4 Category

- Donut/treemap/ranking depending theme/component.
- Percent and absolute value.
- Tap category → filtered transaction list.

### 10.5 Wallet

- Wallet ranking.
- Movement.
- Tap → wallet statement.

### 10.6 Budget

- Limit vs spent.
- Over-budget categories.
- Tap → budget drill-down.

### 10.7 Goal / Debt / Deal / Saving Spin

Each appears as a focused section or sub-report, not mixed into one dense chart wall.

### 10.8 Daily statement

For a selected day:

- Opening.
- Income/inflow.
- Expense/outflow.
- Closing.

Tap number → supporting events.

---

## 11. Goal UX

### Goal list

Card:

- Goal name.
- Target.
- Saved.
- Progress.
- Deadline/status.

### Goal detail

- Progress hero.
- Contribution history.
- Source wallet relationship.
- Add contribution.

### Contribution

```text
Goal
→ Contribute
→ Amount
→ Source
→ Confirm
→ Progress updated
```

UI copy should clarify that this is saving/reallocation, not new income.

---

## 12. Debt UX

### Dashboard

Summary:

- Total outstanding.
- Monthly/period payment context.
- Payoff trajectory.

Debt cards:

- Lender/account.
- Debt type.
- Outstanding.
- Interest/terms where modeled.
- Next relevant payment.

### Detail

- Original debt.
- Outstanding.
- Principal paid.
- Interest paid.
- Payment history.
- Burndown chart.
- Payoff strategy.

### Payment

```text
Debt
→ Pay
→ Amount
→ Wallet
→ Principal/interest semantics
→ Confirm
→ Wallet + debt ledger updated
```

---

## 13. Deal UX

### Deal list

- Name/counterparty description.
- Type.
- Capital committed.
- Principal outstanding.
- Gain/loss.
- Status.

### Detail

- Timeline.
- Capital outlay.
- Principal recovery.
- Realized gain/loss.
- ROI.
- Related transaction events.

### Settlement

Never mask a loss using an artificial wallet.

The user must see what money left, what returned, and what financial result was realized.

---

## 14. Saving Spin UX

### Home launcher

Compact card under financial overview:

- Title.
- Status text.
- Wheel preview.
- Primary action.

### Game sheet

Visual hierarchy:

1. Wheel.
2. Pointer.
3. Result/CTA.
4. Range/step context.
5. Snooze/close secondary actions.

Wheel supports configured slots (for example 6/8/10/12 according to current feature spec).

### Spin behavior

```text
READY
→ Spin
→ Deterministic persisted selected result
→ SPUN_PENDING
→ User chooses destination
→ Confirm
→ COMPLETED
```

No re-spin loophole.

### Result confirmation

Largest text: selected amount.

Then:

- Cash saving destination.
- Bank-transfer destination.
- Selected card state.
- Confirm full-width CTA.
- Streak.
- Skip as tertiary if allowed.

### Saving Spin report

- Total saved.
- Streak.
- Completion rate.
- Skips.
- Trend.
- Destination breakdown.
- Period filter.

---

## 15. Reminders UX

### Reminder list

- Title.
- Amount if relevant.
- Due schedule.
- Enabled switch.
- Category/wallet context.

### Editor

- Title.
- Amount.
- Category.
- Wallet.
- Recurrence.
- Time.
- Notification behavior.

### Notification action

Tap notification:

- Open relevant FinLux context.
- Apply deep link.
- Quick pay only when enough context is available.

---

## 16. Settings UX

Grouped list:

### Account

- Profile.
- Avatar.
- Authentication/account actions.

### Financial Preferences

- Salary Cycle.
- Currency/period preferences if available.

### Security

- Biometric/app lock where enabled by current product.

### Appearance

- Light/Dark/System.
- Classic / Modern / Prism.

### Notifications

- Reminder preferences.
- Saving Spin reminder.

### Data & Export

- Excel.
- PDF.
- Data-related actions.

### About

- Version.
- Open-source link.
- License.
- Project information.

Settings should look like a mobile banking settings list, not a dense admin form.

---

## 17. Design system conversion

### Shared components

- FinluxScreenScaffold.
- FinluxTopBar.
- FinluxBottomBar.
- FinanceKpiCard.
- AmountInput.
- WalletPicker.
- CategoryPicker.
- PeriodSelector.
- EmptyState.
- ErrorState.
- LoadingState.
- ConfirmationDialog.
- TransactionRow.
- SectionHeader.
- ChartCard.

### Typography hierarchy

1. Hero financial amount.
2. Screen title.
3. KPI amount.
4. Section title.
5. Primary body.
6. Secondary metadata.
7. Caption.

Avoid overusing small text.

### Touch

- Target >= 48dp.
- Bottom sheet actions thumb-reachable.
- Important CTA full-width where a clear commit is required.

---

## 18. Error/edge-case states

Every major screen should support:

### Loading

- Skeleton or progress appropriate to context.
- No fake zero amounts while data is unknown.

### Empty

Examples:

- No transactions.
- No wallets.
- No budgets.
- No debts.
- No goals.

Each empty state provides one clear next action.

### Offline

- Existing local data remains visible where possible.
- Pending sync differentiated from confirmed state.

### Write rejected

- UI must not show permanent success if Firestore rules reject the mutation.

### Large values

- Amounts shrink/wrap safely.
- No overlap with decorative content.

---

## 19. Website information architecture

The public website should be a single high-quality responsive product page initially.

Navigation:

- Overview.
- Features.
- Flow.
- Reports.
- Saving Spin.
- Architecture.
- Roadmap.
- GitHub.

---

## 20. Website section-by-section blueprint

### Section 1 — Header

Desktop:

- FinLux logo left.
- Anchors center/right.
- GitHub CTA.

Mobile:

- Logo.
- Menu button.
- Collapsible navigation.

### Section 2 — Hero

Left:

- Eyebrow: Open-source Android personal finance.
- Title.
- Product positioning.
- Two CTA:
  - Explore features.
  - View GitHub.

Right:

- Layered mockup made from real screenshots.
- Small floating cards for Salary Cycle, Net Cash Flow and Saving Spin.

Trust strip:

- Kotlin.
- Jetpack Compose.
- Clean Architecture.
- Firebase.
- MIT.

### Section 3 — Value proposition

Three user problems:

- Daily money is fragmented across wallets.
- Calendar month does not match real payday behavior.
- Many finance apps show charts without explainable accounting.

FinLux answer:

- One financial period.
- Traceable balances.
- Domain-aware reports.

### Section 4 — Core feature grid

Cards:

1. Transactions.
2. Multi-wallet.
3. Salary Cycle.
4. Budget.
5. Reports.
6. Goals.
7. Debt.
8. Deals.
9. Saving Spin.
10. Reminders.
11. Export.
12. Themes/offline sync.

Each card has:

- Icon.
- One-sentence value.
- 2–3 concrete capabilities.

### Section 5 — “How money moves through FinLux”

Visual flow:

```text
Record
→ Categorize
→ Wallet/Ledger
→ Financial Period
→ Reports
→ Plan & Save
```

Below it: explain transfer and opening/closing balance semantics.

### Section 6 — Salary Cycle feature spotlight

Two-column section.

Left:

- Why calendar month can be misleading.

Right:

- Period card UI.

Key copy:

- Payday-aware periods.
- Previous equivalent period.
- Same state across Home, History and Reports.

### Section 7 — Reports showcase

Large screenshot/background panel.

KPI tiles:

- Opening.
- Income.
- Expense.
- Closing.

Drill-down stack:

```text
Summary → Wallet → Category → Transaction
```

### Section 8 — Advanced finance

Three cards:

- Goals.
- Debt.
- Deals.

Emphasize that these domains preserve money semantics instead of being decorative trackers.

### Section 9 — Saving Spin

Visually distinct playful section.

Elements:

- Wheel illustration built in CSS/SVG or screenshot.
- Configurable min/max.
- 5k/10k step.
- Opt-in.
- Persisted result.
- Cash/bank destination.
- Streak/report.

### Section 10 — Themes / UI

Show 3 visual styles:

- Classic.
- Modern.
- Prism.

Message:

> Different look, same financial state.

### Section 11 — Engineering architecture

Developer-focused dark section.

Diagram:

```text
Compose UI
↓
MVVM
↓
Domain Use Cases
↓
Repositories
↓
Firebase / DataStore
```

Engineering badges:

- Atomic wallet mutation.
- Offline-first.
- Firebase.
- CI/CD.
- Tests.
- Export.

### Section 12 — Development status

State clearly:

- Open-source.
- Active development.
- Current Android baseline v1.22.0.
- Production hardening in progress.

Roadmap timeline:

1. Financial integrity.
2. Firebase/security.
3. Reporting.
4. UX consistency.
5. QA/CI.
6. Release candidate.

### Section 13 — Final CTA

- Read source.
- Follow development.
- Explore GitHub.

Do not claim the app is a regulated financial service.

### Section 14 — Footer

- FinLux.
- Open-source Android project.
- MIT License.
- GitHub repository.
- Version status.
- “Not financial advice” style disclaimer only if wording is necessary and accurate.

---

## 21. Website responsive behavior

### >= 1200px

- 2-column hero.
- 3–4 column feature grid.
- Side-by-side product sections.

### 768–1199px

- 2-column where content remains readable.
- Reduced mockup layering.
- 2-column cards.

### < 768px

- Single column.
- Sticky/compact navigation.
- Screenshots full width.
- Horizontal overflow avoided.
- CTA buttons stack.
- Font sizes use clamp().

---

## 22. Website visual direction

Use a premium finance/developer aesthetic rather than a generic SaaS template.

Visual language:

- Near-black/navy foundation.
- Soft glass surfaces.
- Cool blue/violet accents.
- Controlled green for positive finance.
- Red only for expense/risk.
- Large rounded surfaces.
- Subtle gradients.
- Fine borders.
- Limited blur to avoid performance cost.
- Real FinLux screenshots wherever possible.

Do not reproduce the Android UI pixel-for-pixel. Website should communicate the same product language at desktop scale.

---

## 23. Website accessibility

- Semantic HTML.
- Keyboard navigation.
- Visible focus states.
- Sufficient color contrast.
- Alt text for app screenshots.
- Avoid motion when prefers-reduced-motion is enabled.
- Buttons/links clearly distinguishable.
- No critical information conveyed only by color.

---

## 24. SEO / metadata

Title:

```text
FinLux — Open-Source Android Personal Finance
```

Description:

```text
FinLux is an open-source Android personal finance app for transactions, wallets, salary-cycle tracking, budgets, goals, debts, reports and gamified saving.
```

Open Graph:

- Product title.
- Same description.
- Repository URL.
- A screenshot/social image later.

Keywords should remain natural and not be stuffed.

---

## 25. Website call-to-action strategy

Primary CTA:

- View project on GitHub.

Secondary CTA:

- Explore features.

Future CTA only when a stable release exists:

- Download latest APK.

Do not expose a broken download link before a verified release artifact is ready.

---

## 26. Domain deployment sequence

```text
1. Commit website
2. Deploy default GitHub Pages URL
3. Verify website is complete and reachable
4. Capture website screenshot
5. Fork is-a-dev/register
6. Add domains/finlux.json
7. Submit PR with live URL + screenshot
8. After merge, set GitHub Pages custom domain
9. Add CNAME file: finlux.is-a.dev
10. Verify HTTPS
```

Do not configure the custom domain before DNS exists.

---

## 27. Acceptance checklist for the website

- [ ] Mobile responsive.
- [ ] Desktop responsive.
- [ ] No dead primary CTA.
- [ ] GitHub link works.
- [ ] Real FinLux screenshots load.
- [ ] Features match repository.
- [ ] v1.22.0 status is labeled as development baseline.
- [ ] No unsupported AI/bank-sync/iOS/web claims.
- [ ] Salary Cycle explained clearly.
- [ ] Reports show opening/closing concept.
- [ ] Saving Spin explained as opt-in.
- [ ] Architecture is visible.
- [ ] Open-source/MIT visible.
- [ ] GitHub Pages deploy workflow exists.
- [ ] Ready to use as preview for is-a.dev registration.

---

## 28. Final website narrative

The visitor should understand FinLux in this order:

```text
What is FinLux?
↓
Why is it different?
↓
How do I use it daily?
↓
How does it keep financial periods consistent?
↓
What reporting depth does it provide?
↓
What advanced finance tools exist?
↓
What is Saving Spin?
↓
How is it built?
↓
Is it production-ready?
↓
Where is the source code?
```

That narrative should remain stable even as visual styling evolves.
