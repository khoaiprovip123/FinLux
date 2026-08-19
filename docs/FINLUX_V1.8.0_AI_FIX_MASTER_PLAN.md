# FINLUX v1.8.0 — AI FIX MASTER PLAN

> **Mục đích:** Tài liệu này được dùng để giao trực tiếp cho AI Coding Agent / Codex / Claude Code / Gemini Code Assist thực hiện sửa và hardening dự án FinLux theo đúng thứ tự ưu tiên.
>
> **Nguyên tắc quan trọng nhất:** Không rewrite toàn bộ dự án. Không thêm feature mới trước khi hoàn thành P0. Ưu tiên tuyệt đối tính đúng đắn dữ liệu tài chính (data integrity), bảo mật và khả năng kiểm thử.

---

## 0. BASELINE ĐÃ XÁC MINH

- Repository: `khoaiprovip123/FinLux`
- Branch chuẩn để đối chiếu: `main`
- App module: `app`
- `applicationId`: `com.finlux.app`
- `compileSdk`: `36`
- `targetSdk`: `36`
- `minSdk`: `26`
- `versionName`: **`1.8.0`**
- `versionCode`: **`94`**
- Kiến trúc hiện tại:
  - Kotlin
  - Jetpack Compose
  - MVVM
  - Domain / Data / Presentation / Core
  - Hilt
  - Firebase Auth
  - Firestore
  - Firebase Storage
  - DataStore
  - JUnit 5
  - MockK
  - Turbine

### Các file đã audit trực tiếp

```text
app/build.gradle.kts
firestore.rules

app/src/main/java/com/finlux/app/
├── core/navigation/FinluxNavHost.kt
├── data/di/RepositoryModule.kt
├── data/remote/firebase/FirebaseReadRepository.kt
├── data/remote/firebase/FirebaseTransactionRepository.kt
├── domain/model/FinanceModels.kt
└── ...

app/src/test/java/com/finlux/app/
└── data/remote/firebase/FirebaseTransactionRepositoryTest.kt
```

---

# 1. MỤC TIÊU RELEASE

## Release mục tiêu

Không thêm tính năng mới trong đợt này.

Tên nội bộ của phase:

```text
FinLux v1.8.x
Architecture & Data Integrity Hardening
```

Mục tiêu:

1. Không để transaction làm sai `wallet.balance`.
2. Không để transaction làm sai `budget.spentAmount`.
3. Mọi edit/delete phải dùng dữ liệu thực tế đang lưu trong Firestore làm source of truth.
4. Không có lỗi tháng ngân sách do UTC/local timezone.
5. Firestore Rules không cho client tùy ý ghi dữ liệu tài chính không hợp lệ.
6. Code dễ test và dễ refactor hơn.
7. Không phá UI hiện tại.
8. Không làm mất backward compatibility với dữ liệu Firestore hiện có.
9. Build, unit test, lint phải pass trước khi merge.
10. Chỉ sau P0 mới được thực hiện P1/P2.

---

# 2. QUY TẮC BẮT BUỘC CHO AI CODING AGENT

AI phải tuân thủ toàn bộ các điều sau.

## 2.1 Không được tự ý thay đổi

KHÔNG tự ý:

- đổi `applicationId = "com.finlux.app"`;
- đổi Firebase project;
- đổi Firestore collection path;
- xóa dữ liệu hoặc migration destructive;
- chuyển `Money` từ `Long` sang `Double` / `Float`;
- rewrite toàn bộ app;
- chuyển sang Flutter / React Native;
- thay Compose bằng XML;
- thay Firebase bằng backend khác;
- thêm Room chỉ vì muốn "clean architecture";
- thay toàn bộ UI;
- xóa Classic/Liquid/Modern UI trước khi có phase riêng;
- bump versionName/versionCode trước khi hoàn thành phase;
- thay signing config;
- thay `minSdk`, `targetSdk`, `compileSdk`;
- xóa Demo repository fallback;
- phá `BuildConfig.FIREBASE_CONFIGURED`;
- đổi schema Firestore theo cách không backward compatible.

## 2.2 Quy trình thực thi cho từng task

Mỗi task phải theo thứ tự:

```text
READ
→ UNDERSTAND
→ WRITE/UPDATE TEST
→ PATCH CODE
→ RUN TEST
→ RUN BUILD
→ REPORT RESULT
```

Không được:

```text
PATCH 10 task cùng lúc
→ cuối cùng mới test
```

## 2.3 Commit

Một nhóm logic = một commit.

Ví dụ:

```text
fix(finance): use stored transaction as source of truth on edit
fix(finance): derive delete budget reference from stored transaction
fix(time): standardize finance month timezone
test(finance): add stale transaction integrity coverage
security(firestore): validate finance document writes
```

Không gom P0 + P1 + UI vào một commit lớn.

---

# 3. PHÂN LOẠI ƯU TIÊN

| Priority | Ý nghĩa | Được phép release nếu chưa xong? |
|---|---|---|
| P0 | Data integrity / security / financial correctness | **KHÔNG** |
| P1 | Architecture hardening / maintainability | Có thể beta, chưa nên 1.0 ổn định |
| P2 | UI/UX / design system / quality | Có |
| P3 | Scale / AI / advanced product | Có |

Thứ tự bắt buộc:

```text
P0
↓
P1
↓
P2
↓
P3
```

---

# 4. P0 — DATA INTEGRITY & SECURITY

---

## P0-01 — FIX EDIT TRANSACTION DÙNG STALE `original`

### Severity

```text
CRITICAL
```

### File

```text
app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseTransactionRepository.kt
```

### Hàm

```kotlin
editWithBalanceUpdate(
    original: FinanceTransaction,
    updated: FinanceTransaction
)
```

### Vấn đề hiện tại

Trong transaction:

```kotlin
val stored = atomic.get(transactionRef).toFinanceTransaction()
    ?: error("Không tìm thấy giao dịch")
```

Code đã đọc transaction thực tế từ Firestore.

Nhưng phần reverse budget hiện đang dựa vào object caller:

```kotlin
FieldValue.increment(-original.amount.value)
```

`original` có thể stale.

Ví dụ:

```text
Firestore amount = 1,000,000
UI cached original = 800,000
updated = 1,200,000
```

Wallet có thể được reverse đúng theo `stored`, nhưng budget lại reverse sai theo `original`.

### Yêu cầu sửa

Sau khi đọc `stored`, toàn bộ trạng thái "old" phải derive từ `stored`.

Không dùng các field business quan trọng của `original` sau khi đã có `stored`, ngoại trừ ID để tìm document nếu interface hiện tại chưa đổi.

Tối thiểu:

```kotlin
val stored = atomic.get(transactionRef).toFinanceTransaction()
    ?: error("Không tìm thấy giao dịch")

val oldWalletRef = firestore.userWallets(uid).document(stored.walletId)
val oldBudgetRef = stored.budgetRef(firestore, uid)
```

Reverse budget:

```kotlin
FieldValue.increment(-stored.amount.value)
```

### Yêu cầu kiến trúc tốt hơn

Nếu không gây breaking lớn, repository API về lâu dài nên hướng tới:

```kotlin
suspend fun editWithBalanceUpdate(
    transactionId: String,
    updated: FinanceTransaction,
): AppResult<Unit>
```

Source of truth của old transaction phải là Firestore.

### Acceptance Criteria

- [ ] Edit amount cùng wallet/category đúng.
- [ ] Edit đổi wallet đúng.
- [ ] Edit đổi category đúng.
- [ ] Edit đổi tháng đúng.
- [ ] Edit stale caller object vẫn đúng.
- [ ] `wallet.balance` đúng.
- [ ] `budget.spentAmount` của budget cũ đúng.
- [ ] `budget.spentAmount` của budget mới đúng.
- [ ] Không dùng `original.amount`, `original.categoryId`, `original.date`, `original.walletId` làm authoritative data sau khi `stored` đã được đọc.

---

## P0-02 — FIX DELETE TRANSACTION DÙNG STALE OBJECT

### Severity

```text
CRITICAL
```

### File

```text
app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseTransactionRepository.kt
```

### Hàm

```kotlin
deleteWithBalanceUpdate(
    transaction: FinanceTransaction
)
```

### Vấn đề hiện tại

`budgetRef` được tính trước:

```kotlin
val budgetRef = transaction.budgetRef(firestore, uid)
```

Sau đó mới đọc:

```kotlin
val stored = atomic.get(transactionRef).toFinanceTransaction()
```

Nếu transaction caller đã stale thì có thể:

```text
xóa đúng Firestore transaction
+
reverse đúng wallet
+
reverse SAI budget
```

### Yêu cầu sửa

Chỉ dùng:

```text
transaction.id
```

để lấy document.

Sau đó:

```kotlin
val stored = atomic.get(transactionRef).toFinanceTransaction()
    ?: error("Không tìm thấy giao dịch")

val walletRef = firestore.userWallets(uid).document(stored.walletId)
val budgetRef = stored.budgetRef(firestore, uid)
```

Mọi reverse phải dựa vào `stored`.

### API mục tiêu

Ưu tiên:

```kotlin
suspend fun deleteWithBalanceUpdate(
    transactionId: String
): AppResult<Unit>
```

Nếu đổi interface ảnh hưởng quá rộng, có thể giữ API cũ ở phase này nhưng implementation **chỉ tin `transaction.id`**.

### Acceptance Criteria

- [ ] Delete normal expense đúng.
- [ ] Delete income đúng.
- [ ] Delete stale amount đúng.
- [ ] Delete stale category đúng.
- [ ] Delete stale date/month đúng.
- [ ] Delete stale wallet đúng.
- [ ] Transaction không tồn tại trả error.
- [ ] Budget không tồn tại không crash.
- [ ] Wallet không tồn tại trả error rõ ràng.

---

## P0-03 — STANDARDIZE FINANCE TIMEZONE

### Severity

```text
CRITICAL / HIGH
```

### File hiện có lỗi

```text
app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseTransactionRepository.kt
app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseReadRepository.kt
```

### Vấn đề

Query month đang dùng:

```kotlin
ZoneId.systemDefault()
```

nhưng `budgetRef()` đang dùng:

```kotlin
date.atZone(ZoneOffset.UTC)
```

Điều này gây lỗi boundary month.

Ví dụ Việt Nam:

```text
01/09/2026 01:00 Asia/Ho_Chi_Minh
=
31/08/2026 18:00 UTC
```

UI có thể coi là tháng 9 nhưng budgetRef lại coi là tháng 8.

### Yêu cầu

Tạo một source duy nhất.

Khuyến nghị:

```kotlin
interface FinanceClock {
    fun now(): Instant
    val zoneId: ZoneId
}
```

Implementation mặc định:

```kotlin
@Singleton
class SystemFinanceClock @Inject constructor() : FinanceClock {
    override fun now(): Instant = Instant.now()
    override val zoneId: ZoneId = ZoneId.systemDefault()
}
```

Hoặc nếu business rule hiện tại xác định FinLux chỉ hoạt động theo Việt Nam:

```kotlin
ZoneId.of("Asia/Ho_Chi_Minh")
```

Nhưng phải chọn **một rule duy nhất**.

### Bắt buộc thay

Không còn code tự phát:

```kotlin
ZoneId.systemDefault()
ZoneOffset.UTC
Instant.now()
YearMonth.now(...)
```

ở các logic tháng tài chính quan trọng nếu đã có injected clock/zone.

### Acceptance test bắt buộc

Test transaction:

```text
Asia/Ho_Chi_Minh
2026-09-01 00:30
```

phải thuộc:

```text
2026-09 budget
```

Test:

```text
2026-08-31 23:30
```

phải thuộc:

```text
2026-08 budget
```

---

## P0-04 — DEFINE & TEST FINANCIAL INVARIANTS

Các invariant phải được xem như contract của hệ thống.

### INV-01 — Wallet

```text
wallet.balance
=
opening balance
+ tổng balanceDelta của transaction hợp lệ
```

### INV-02 — Budget

```text
budget.spentAmount
=
SUM(
    EXPENSE.amount
    WHERE categoryId = budget.categoryId
    AND financialMonth(date) = budget.month
)
```

### INV-03 — Transfer amount

```text
TRANSFER_OUT.amount == TRANSFER_IN.amount
```

### INV-04 — Transfer relation

```text
out.relatedWalletId == in.walletId
in.relatedWalletId == out.walletId
```

### INV-05 — Transaction amount

```text
amount > 0
```

### INV-06 — Non-card transfer

```text
sourceWallet.type != CARD
→ amount <= sourceWallet.balance
```

### INV-07 — Edit

Edit phải:

```text
reverse stored old state
+
apply new state
```

### INV-08 — Delete

Delete phải:

```text
read stored
→ reverse stored
→ delete stored
```

---

## P0-05 — MỞ RỘNG `FirebaseTransactionRepositoryTest`

### File

```text
app/src/test/java/com/finlux/app/data/remote/firebase/FirebaseTransactionRepositoryTest.kt
```

### Test hiện tại chưa đủ

Hiện test chủ yếu kiểm tra:

```text
runTransaction được gọi
AppResult.Success
```

Cần verify delta cụ thể.

### Bắt buộc bổ sung test

#### ADD

- [ ] add income updates wallet `+amount`.
- [ ] add expense updates wallet `-amount`.
- [ ] expense budget increment đúng.
- [ ] expense không có budget không crash.
- [ ] unauthenticated trả error.
- [ ] wallet missing trả error.

#### EDIT

- [ ] edit same wallet, same category.
- [ ] edit amount.
- [ ] edit wallet.
- [ ] edit category.
- [ ] edit month.
- [ ] edit EXPENSE → INCOME.
- [ ] edit INCOME → EXPENSE.
- [ ] stale caller amount.
- [ ] stale caller category.
- [ ] stale caller wallet.
- [ ] stale caller date.
- [ ] stored document missing.

#### DELETE

- [ ] delete expense.
- [ ] delete income.
- [ ] delete stale caller amount.
- [ ] delete stale caller category.
- [ ] delete stale caller month.
- [ ] delete stale caller wallet.
- [ ] stored document missing.

#### TRANSFER

- [ ] normal transfer.
- [ ] source == destination blocked.
- [ ] amount <= 0 blocked.
- [ ] insufficient non-card blocked.
- [ ] CARD behavior giữ đúng business rule.
- [ ] source missing.
- [ ] destination missing.
- [ ] verify OUT/IN amount bằng nhau.
- [ ] verify relatedWalletId đối xứng.
- [ ] verify source balance decrement.
- [ ] verify destination balance increment.

---

## P0-06 — SAFE MONEY ARITHMETIC / OVERFLOW

`Money(Long)` phải tiếp tục được giữ.

### Không được

```kotlin
Double
Float
BigDecimal
```

chỉ vì đây là money.

### Yêu cầu

Kiểm tra business rule tối đa hiện tại.

Nếu BR cho tối đa 15 chữ số, tạo validation tập trung:

```kotlin
object MoneyRules {
    const val MAX_ABS_VALUE = 999_999_999_999_999L
}
```

Khi tạo amount:

```text
0 < amount <= MAX
```

Khi balance update:

- không overflow `Long`;
- không vượt business max nếu rule yêu cầu.

Khuyến nghị dùng checked arithmetic:

```kotlin
Math.addExact(...)
Math.subtractExact(...)
```

và convert exception thành `AppResult.Error` có message business.

---

## P0-07 — FIRESTORE SECURITY RULES

### File

```text
firestore.rules
```

### Hiện trạng

Hiện rule về cơ bản là:

```text
auth uid đúng
→ read/write mọi field trong users/{uid}/...
```

Đây chưa đủ cho ứng dụng tài chính.

### Mục tiêu

Mỗi collection phải có schema validation tối thiểu.

### Helpers gợi ý

```javascript
function isOwner(uid) {
  return request.auth != null && request.auth.uid == uid;
}

function isPositiveMoney(value) {
  return value is int && value > 0;
}

function isNonNegativeMoney(value) {
  return value is int && value >= 0;
}
```

### transactions

Validate:

- required keys;
- amount là integer;
- amount > 0;
- type thuộc allowed set;
- walletId là string;
- categoryId nullable/string;
- relatedWalletId nullable/string;
- date timestamp;
- createdAt timestamp;
- updatedAt timestamp;
- không cho field lạ nếu có thể enforce an toàn.

Allowed types:

```text
income
expense
transfer_out
transfer_in
```

### wallet

Validate:

```text
name string
type allowed
balance integer
color string
isDefault boolean
createdAt timestamp
```

### budget

Validate:

```text
categoryId string
month string YYYY-MM
limitAmount integer >= 0
spentAmount integer >= 0
notified80 boolean
notified100 boolean
```

### goal/reminder/notification

Thêm schema validation tương ứng.

### CẢNH BÁO

Không viết Rules theo kiểu quá chặt khiến Firestore transaction hiện tại không chạy.

AI phải:

1. đọc toàn bộ mapper hiện tại;
2. xác định exact document schema;
3. viết rule backward compatible;
4. test với Firebase Emulator nếu repo có thể hỗ trợ.

### P0 nâng cao

Đánh giá khả năng dùng:

```text
getAfter()
```

để enforce cross-document invariant trong atomic write.

Nếu Rules quá phức tạp hoặc impossible do client architecture, ghi rõ limitation trong báo cáo. Không tạo rule giả vờ bảo vệ nhưng thực tế không đảm bảo invariant.

---

## P0-08 — OFFLINE WRITE BEHAVIOR

Firestore transaction không nên được quảng bá như offline-safe write.

### Quyết định phase hiện tại

**Không triển khai Room/Outbox trong P0.**

Behavior mục tiêu:

```text
Offline:
- đọc cache: cho phép
- tạo/sửa/xóa/chuyển tiền: fail gracefully
- UI thông báo rõ cần kết nối mạng
```

Message gợi ý:

```text
"Đang ngoại tuyến. Vui lòng kết nối mạng để thực hiện giao dịch và đảm bảo số dư chính xác."
```

### Không được

- queue transaction tài chính tự phát bằng memory;
- giả vờ success rồi sync sau;
- tự thêm Room/outbox mà chưa có thiết kế idempotency/reconciliation.

---

# 5. P0 — DEFINITION OF DONE

P0 chỉ DONE khi tất cả đều đúng:

- [ ] P0-01 stale edit fixed.
- [ ] P0-02 stale delete fixed.
- [ ] P0-03 timezone standardized.
- [ ] P0-04 invariant defined.
- [ ] P0-05 tests added.
- [ ] P0-06 overflow/amount validation.
- [ ] P0-07 Firestore Rules hardened.
- [ ] P0-08 offline write behavior rõ ràng.
- [ ] Unit tests pass.
- [ ] Lint pass hoặc có report rõ warning existing.
- [ ] Debug build pass.
- [ ] Không regression UI chính.
- [ ] Không đổi version.
- [ ] Không đổi Firestore schema destructive.

---

# 6. P1 — ARCHITECTURE HARDENING

Chỉ bắt đầu sau P0.

---

## P1-01 — SPLIT `FirebaseReadRepository`

### File hiện tại

```text
app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseReadRepository.kt
```

Hiện class implement:

```kotlin
WalletRepository
CategoryRepository
BudgetRepository
ReminderRepository
GoalRepository
DashboardRepository
NotificationRepository
```

### Vấn đề

Một class chịu trách nhiệm:

```text
wallet
category
budget
reminder
goal
dashboard
notification
mapping
Firestore query
CRUD
listener
```

### Target

```text
data/remote/firebase/
├── wallet/
│   ├── FirebaseWalletRepository.kt
│   └── WalletFirestoreMapper.kt
├── category/
│   ├── FirebaseCategoryRepository.kt
│   └── CategoryFirestoreMapper.kt
├── budget/
│   ├── FirebaseBudgetRepository.kt
│   └── BudgetFirestoreMapper.kt
├── reminder/
│   └── FirebaseReminderRepository.kt
├── goal/
│   └── FirebaseGoalRepository.kt
├── notification/
│   └── FirebaseNotificationRepository.kt
├── dashboard/
│   └── FirebaseDashboardRepository.kt
└── transaction/
    └── FirebaseTransactionRepository.kt
```

Có thể chưa cần folder sâu nếu project muốn giữ đơn giản; quan trọng là **mỗi repository một trách nhiệm**.

### RepositoryModule

Update:

```text
app/src/main/java/com/finlux/app/data/di/RepositoryModule.kt
```

Không tạo `FirebaseReadRepository(auth, firestore)` cho từng binding nữa.

### Acceptance Criteria

- [ ] Mỗi repository có responsibility rõ.
- [ ] Không thay interface domain nếu không cần.
- [ ] Demo fallback giữ nguyên.
- [ ] Firebase configured/non-configured behavior giữ nguyên.
- [ ] Tests pass.

---

## P1-02 — ĐƯA UI PREFERENCE RA KHỎI FINANCIAL DOMAIN

### File hiện tại

```text
domain/model/FinanceModels.kt
```

Hiện chứa cả:

```kotlin
ThemePreference
AppUiStyle
GlassIntensity
CardDensity
VisualStyle
UiPreferences
```

### Target

Financial domain:

```text
domain/model/
├── Money.kt
├── Wallet.kt
├── Category.kt
├── FinanceTransaction.kt
├── Budget.kt
├── Reminder.kt
└── FinancialGoal.kt
```

UI/design:

```text
core/designsystem/model/
├── AppUiStyle.kt
├── GlassIntensity.kt
├── CardDensity.kt
└── VisualStyle.kt

core/preferences/
├── ThemePreference.kt
└── UiPreferences.kt
```

### Lưu ý

Không refactor package hàng loạt nếu chỉ để đẹp.

AI phải sửa import có kiểm soát và compile sau từng nhóm.

---

## P1-03 — SPLIT `FinluxNavHost`

### File

```text
core/navigation/FinluxNavHost.kt
```

Hiện đang xử lý:

```text
navigation
auth
main routes
animation
swipe
quick add
transaction sheet
receipt
transfer request
external destination StateFlow
```

### Target

```text
core/navigation/
├── FinluxNavHost.kt
├── AuthNavGraph.kt
├── MainNavGraph.kt
├── FinanceNavGraph.kt
├── NavigationEvent.kt
└── MainSwipeNavigation.kt
```

Không bắt buộc đúng tên trên; mục tiêu là responsibility rõ.

### Typed navigation

Thay raw String event dần bằng:

```kotlin
sealed interface NavigationEvent {
    data object Notifications : NavigationEvent
    data class TransactionDetail(val id: String) : NavigationEvent
    data class PayNotification(val id: String) : NavigationEvent
}
```

### Không được

Rewrite toàn bộ navigation library nếu current Compose Navigation hoạt động tốt.

---

## P1-04 — CLOCK / TIMEZONE INJECTION

Nếu P0 chỉ tạo minimal helper, P1 hoàn thiện DI.

Target:

```text
core/time/
├── FinanceClock.kt
└── SystemFinanceClock.kt
```

Inject vào:

```text
Transaction repository
Dashboard repository
UseCases liên quan month/date
Reminder nếu cần
```

### Test

Dùng fixed clock.

---

## P1-05 — FIRESTORE MAPPERS / DTO BOUNDARY

Hiện mapper đang nằm chung repository.

Target:

```text
Firestore DocumentSnapshot / Map
        ↓
Data DTO / Mapper
        ↓
Domain Model
```

Mục tiêu:

- repository ngắn;
- parsing fallback legacy tập trung;
- dễ test migration;
- domain không phụ thuộc Firestore.

---

## P1-06 — ERROR MODEL

Không để UI phải parse message string để biết loại lỗi.

Target gợi ý:

```kotlin
sealed interface AppError {
    data object Unauthenticated : AppError
    data object Offline : AppError
    data object InsufficientBalance : AppError
    data object TransactionNotFound : AppError
    data object WalletNotFound : AppError
    data class Validation(val message: String) : AppError
    data class Unknown(val cause: Throwable?) : AppError
}
```

Có thể giữ `AppResult.Error(message, cause)` backward-compatible trong phase đầu, nhưng thêm error code/type rõ dần.

---

## P1-07 — CI

Nếu chưa có pipeline chuẩn, thêm GitHub Actions.

Minimum:

```text
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

Nên cache Gradle.

Không cần build release signing nếu secret chưa cấu hình.

---

# 7. P1 — DEFINITION OF DONE

- [ ] FirebaseReadRepository không còn god repository.
- [ ] UI preference không còn trong financial domain.
- [ ] NavHost được chia responsibility.
- [ ] Clock/zone injectable.
- [ ] Mapper boundary rõ hơn.
- [ ] Error model có hướng chuẩn hóa.
- [ ] CI build/test/lint.
- [ ] Không regression feature.
- [ ] Không thay UI business behavior.

---

# 8. P2 — UI/UX & DESIGN SYSTEM QUALITY

Chỉ thực hiện sau khi P0 ổn và P1 core đã sạch.

---

## P2-01 — GIỮ VISUAL IDENTITY, KHÔNG REDESIGN TOÀN BỘ

Giữ định hướng:

```text
FinLux
Liquid Glass
Modern Luxury
light theme
financial dashboard
```

Không thay toàn bộ giao diện.

---

## P2-02 — KHÔNG DUPLICATE CLASSIC / MODERN BUSINESS SCREEN

Tránh:

```text
ClassicHomeScreen
ModernHomeScreen

ClassicBudgetScreen
ModernBudgetScreen

ClassicReportsScreen
ModernReportsScreen
```

mỗi bản tự chứa business logic.

Target:

```text
HomeRoute
↓
HomeContent
↓
Design Tokens
├── ClassicLiquidTokens
└── ModernLuxuryTokens
```

Một state, một event contract, nhiều skin.

---

## P2-03 — DESIGN TOKENS

Tạo token tập trung:

```kotlin
data class FinluxDesignTokens(
    val cardRadius: Dp,
    val panelAlpha: Float,
    val glassBlur: Dp,
    val spacingXs: Dp,
    val spacingSm: Dp,
    val spacingMd: Dp,
    val spacingLg: Dp,
)
```

Không hardcode tràn lan:

```text
16.dp
18.dp
20.dp
24.dp
alpha .12
alpha .14
...
```

---

## P2-04 — FINANCE STATES

Mọi feature chính phải có:

```text
Loading
Content
Empty
Error
Offline
```

Ưu tiên:

```text
Home
Transactions
Wallets
Budget
Reports
Goals
Notifications
```

---

## P2-05 — REPORTS

Reports không chỉ decorative chart.

Cần dần trả lời:

```text
Tháng này tăng/giảm bao nhiêu?
Danh mục nào tăng?
Ngày nào chi bất thường?
Ngân sách nào sắp vượt?
So với tháng trước?
```

Không bắt buộc làm AI ở P2.

---

## P2-06 — ACCESSIBILITY

Kiểm tra:

- contentDescription;
- contrast;
- touch target;
- font scale;
- screen reader;
- error state không chỉ dùng màu;
- currency đọc được.

---

# 9. P3 — SCALE & ADVANCED PRODUCT

Chưa triển khai khi chưa yêu cầu.

Các hướng tương lai:

```text
AI financial assistant
long-term financial planning
cashflow forecasting
financial health score
smart categorization
anomaly detection
subscription detection
goal recommendation
monthly summary
```

---

## P3-01 — LEDGER / MATERIALIZED BALANCE

Hiện FinLux lưu:

```text
transaction
wallet.balance
budget.spentAmount
```

Đây là denormalized state.

Khi scale lớn cần cân nhắc:

```text
immutable ledger
+
derived/materialized balance
+
reconciliation job
```

Không migration trong v1.8.x.

---

## P3-02 — MONTHLY SUMMARY

Khi số transaction lớn:

```text
users/{uid}/monthlySummaries/{yyyy-MM}
```

Có thể chứa:

```json
{
  "income": 0,
  "expense": 0,
  "transactionCount": 0
}
```

Đây là optimization phase tương lai, không phải P0.

---

# 10. TEST MATRIX TỐI THIỂU TRƯỚC MERGE

## Financial transaction

| Case | Expected |
|---|---|
| Add income | Wallet + |
| Add expense | Wallet -, Budget + |
| Edit amount | Reverse old, apply new |
| Edit wallet | Old reverse, new apply |
| Edit category | Old budget reverse, new budget apply |
| Edit month | Old month reverse, new month apply |
| Delete income | Wallet reverse |
| Delete expense | Wallet reverse, Budget reverse |
| Stale edit caller | Stored Firestore wins |
| Stale delete caller | Stored Firestore wins |
| Transfer | Atomic OUT/IN |
| Insufficient balance | Block |
| Same source/dest | Block |
| Offline write | Graceful error |
| Month boundary | Correct local financial month |

---

# 11. BUILD / VALIDATION COMMANDS

Unix/macOS:

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

Windows:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
```

Nếu emulator/device setup sẵn:

```bash
./gradlew connectedDebugAndroidTest
```

Không tuyên bố task DONE nếu build chưa chạy được mà không ghi rõ lý do.

---

# 12. AI REPORT FORMAT SAU MỖI TASK

AI phải trả đúng format:

```markdown
## TASK RESULT

Task:
P0-XX

Status:
PASS / PARTIAL / FAIL

Files changed:
- path/file1
- path/file2

Logic changed:
- ...

Tests added:
- ...

Commands executed:
- ...

Result:
- testDebugUnitTest: PASS/FAIL
- lintDebug: PASS/FAIL/NOT RUN
- assembleDebug: PASS/FAIL

Risks remaining:
- ...

Next recommended task:
P0-YY
```

Nếu có fail:

```text
DỪNG
```

Không tự động nhảy sang task tiếp theo nếu failure có thể ảnh hưởng correctness.

---

# 13. THỨ TỰ GIAO AI CHẠY

## Batch 1 — Critical finance fixes

```text
P0-01
P0-02
P0-03
P0-04
P0-05
P0-06
```

Sau Batch 1:

```text
RUN FULL UNIT TEST
RUN LINT
RUN DEBUG BUILD
```

---

## Batch 2 — Security / offline

```text
P0-07
P0-08
```

Sau Batch 2:

```text
RUN FIREBASE RULE TEST / EMULATOR nếu khả thi
RUN UNIT TEST
RUN BUILD
```

---

## Batch 3 — Architecture

```text
P1-01
P1-02
P1-03
P1-04
P1-05
P1-06
P1-07
```

Không làm cả Batch 3 trong một commit.

---

## Batch 4 — UI Quality

```text
P2-01 → P2-06
```

---

# 14. PROMPT MASTER ĐỂ ĐƯA CHO AI CODING AGENT

Copy nguyên prompt dưới đây.

```text
Bạn là Senior Android/Kotlin Architect chịu trách nhiệm hardening dự án FinLux.

Repository:
khoaiprovip123/FinLux

Baseline bắt buộc:
- branch main
- versionName 1.8.0
- versionCode 94
- applicationId com.finlux.app
- Kotlin + Jetpack Compose + Hilt + Firebase
- Money dùng Long và PHẢI tiếp tục dùng Long

Hãy đọc file FINLUX_V1.8.0_AI_FIX_MASTER_PLAN.md trước khi chỉnh code.

QUY TẮC:
1. Không rewrite toàn bộ project.
2. Không thêm feature mới.
3. Không tự bump version.
4. Không đổi Firebase schema destructive.
5. Không thay UI hiện tại nếu task không yêu cầu.
6. Không làm P1/P2 trước khi P0 hoàn thành.
7. Một task logic = một commit rõ ràng.
8. Luôn đọc source hiện tại trước khi patch.
9. Source of truth của transaction cũ khi edit/delete là document thực tế trong Firestore.
10. Không tin object stale từ UI/caller.
11. Viết hoặc cập nhật test trước/đồng thời với fix.
12. Sau mỗi task phải chạy test phù hợp.
13. Trước khi kết luận Batch DONE phải chạy:
   - testDebugUnitTest
   - lintDebug
   - assembleDebug
14. Nếu fail, dừng và báo root cause. Không che lỗi.

Bắt đầu CHỈ với:
P0-01 — Fix edit transaction stale original.

Sau khi hoàn thành P0-01:
- báo file đã sửa,
- test đã thêm,
- command đã chạy,
- kết quả,
- risk còn lại.

KHÔNG tự động thực hiện P0-02 cho đến khi P0-01 pass.
```

---

# 15. PROMPT CHO P0-01

```text
Thực hiện P0-01 trong FINLUX_V1.8.0_AI_FIX_MASTER_PLAN.md.

Mục tiêu:
FirebaseTransactionRepository.editWithBalanceUpdate phải dùng transaction thực tế đọc từ Firestore (`stored`) làm authoritative old state.

Yêu cầu:
- Không dùng original.amount để reverse budget.
- Không dùng original.walletId/categoryId/date làm source of truth sau khi stored đã được đọc.
- oldWalletRef phải derive từ stored.walletId.
- oldBudgetRef phải derive từ stored.
- reverse old budget bằng stored.amount.value.
- giữ backward compatibility.
- thêm test stale caller:
  + caller original amount/category/date/wallet khác stored
  + verify logic vẫn reverse stored state.
- không đổi version.
- không làm task khác.

Sau khi sửa:
- chạy FirebaseTransactionRepositoryTest
- chạy testDebugUnitTest nếu khả thi
- báo TASK RESULT.
```

---

# 16. PROMPT CHO P0-02

```text
Thực hiện P0-02 trong FINLUX_V1.8.0_AI_FIX_MASTER_PLAN.md.

Mục tiêu:
deleteWithBalanceUpdate không được tin transaction object caller ngoại trừ ID.

Flow đúng:
transactionId
→ Firestore transaction
→ read stored transaction
→ derive walletRef từ stored.walletId
→ derive budgetRef từ stored category/date/type
→ reverse stored balance/budget
→ delete document

Thêm test:
- stale amount
- stale category
- stale date/month
- stale wallet

Không đổi version.
Không làm P0-03.
```

---

# 17. PROMPT CHO P0-03

```text
Thực hiện P0-03.

Audit toàn bộ nơi xác định YearMonth cho transaction/budget/dashboard.

Hiện có inconsistency giữa:
- ZoneId.systemDefault()
- ZoneOffset.UTC

Tạo một business finance timezone source duy nhất.

Không hardcode UTC cho budgetRef.

Thêm test boundary:
Asia/Ho_Chi_Minh:
- 2026-09-01 00:30 → September
- 2026-08-31 23:30 → August

Không đổi Firestore date storage dạng Timestamp/Instant.
Chỉ chuẩn hóa cách xác định financial month.
```

---

# 18. TIÊU CHÍ ĐỂ TĂNG VERSION

Không bump từ `1.8.0 / 94` trong lúc sửa lẻ.

Chỉ đề xuất version mới khi:

```text
P0 DONE
+
test pass
+
lint acceptable
+
assemble pass
```

Gợi ý sau P0:

```text
versionName = 1.8.1
versionCode = 95
```

Nhưng chỉ thay nếu Product Owner xác nhận.

Sau P1 hoàn chỉnh có thể cân nhắc:

```text
1.9.0
```

Không tự quyết.

---

# 19. FINAL QUALITY GATE

Trước khi coi nhánh hardening sẵn sàng merge:

```text
[ ] No stale transaction integrity bug
[ ] No month boundary budget bug
[ ] Wallet invariant maintained
[ ] Budget invariant maintained
[ ] Transfer invariant maintained
[ ] Firestore rules reviewed
[ ] Offline write behavior explicit
[ ] Unit tests green
[ ] Lint reviewed
[ ] Debug APK builds
[ ] No destructive schema migration
[ ] No version bump without approval
[ ] UI smoke check
```

---

# 20. QUAN ĐIỂM KIẾN TRÚC CHỐT

FinLux hiện không cần rewrite.

Chiến lược:

```text
CORRECTNESS
↓
SECURITY
↓
TESTABILITY
↓
ARCHITECTURE CLEANUP
↓
UI QUALITY
↓
ADVANCED FEATURES / AI
```

Không đảo thứ tự thành:

```text
AI feature
↓
UI animation
↓
new screens
↓
sau đó mới sửa data integrity
```

Đây là app tài chính.

**Data correctness phải là nền móng của toàn bộ sản phẩm.**
