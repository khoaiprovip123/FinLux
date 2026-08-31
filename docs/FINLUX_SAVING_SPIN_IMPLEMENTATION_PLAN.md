# FinLux — Vòng Quay Tiết Kiệm (Saving Spin) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Xây dựng mini game **Vòng quay tiết kiệm** cho FinLux: người dùng tự cấu hình khoảng tiền và bước mệnh giá 5.000đ/10.000đ, quay theo lịch, xác nhận đã tiết kiệm vào heo/ví/tài khoản, theo dõi streak/lịch sử/báo cáo, nhận nhắc lúc 09:00 hoặc giờ tùy chọn, và có launcher trực tiếp trên Trang chủ.

**Architecture:** Giữ nguyên Clean Architecture + MVVM hiện có. Feature mới nằm trong `domain / data / presentation`, dữ liệu đồng bộ Firestore, lịch nhắc dùng `AlarmManager` theo pattern reminder hiện hữu, UI dùng Jetpack Compose và design system/tokens hiện hữu. Không đặt business logic trong Composable và không gọi Firestore trực tiếp từ ViewModel.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Navigation Compose, Hilt, Coroutines/Flow, Firebase Auth/Firestore, AlarmManager, JUnit5/Turbine/Compose UI Test.

**Spec:** Tài liệu này là spec + implementation plan tự chứa. Visual target là bộ 5 mockup đã duyệt trong cuộc trao đổi: Home launcher → Spin bottom sheet → Result/confirm → Report → Settings.

---

# 0. MỆNH LỆNH THỰC THI CHO AI

AI thực hiện phải đọc trước:

1. `AGENTS.md`
2. `docs/CONTEXT.md`
3. `docs/BA_SPEC.md`
4. `docs/UI_SPEC.md`
5. `docs/DATA_SPEC.md`
6. `docs/PLAN.md`
7. `docs/BACKLOG.md`
8. `HANDOVER_LOG.md`

Quy tắc bắt buộc:

- Không hard-code màu trong feature.
- Dùng `LocalFinluxTokens.current` và `MaterialTheme.colorScheme`.
- Màn Liquid Glass dùng component/design-system hiện hữu; Prism không được ép Liquid Glass.
- Không tự tạo formatter tiền nếu project đã có `toVnd()`, `toShortVnd()`, `formatVndAmount()` hoặc utility tương đương.
- Không gọi Firestore trực tiếp từ ViewModel.
- Ghi dữ liệu ảnh hưởng số dư ví phải dùng Firestore transaction / use case hiện hữu.
- Không tự sửa công thức Home/Report/kỳ lương ngoài phạm vi feature.
- PRE-EXECUTION phải ghi `[IN PROGRESS]` vào `HANDOVER_LOG.md`.
- Chỉ cập nhật CHANGELOG sau khi test + build thành công.
- Không commit secret.
- Không đổi layout tổng thể Home ngoài việc chèn launcher mini game ở vị trí quy định.
- Không cho phép quay lại để đổi kết quả sau khi lượt quay đã được khóa.

---

# 1. PHẠM VI NGHIỆP VỤ ĐÃ CHỐT

## 1.1. Tên feature

Tên hiển thị chính:

**Vòng quay tiết kiệm**

Tên kỹ thuật:

- Feature/package: `savingspin`
- Domain prefix: `SavingSpin`
- Firestore collection prefix: `savingSpin...`

Không sử dụng từ “gambling”, “bet”, “jackpot”, “win money”. Đây là công cụ tạo thói quen tiết kiệm, không phải trò chơi tiền thưởng.

---

## 1.2. Bật/tắt hoàn toàn

Feature là **opt-in**.

Mặc định:

```text
enabled = false
```

Khi chưa bật:

- Không hiện launcher trên Home.
- Không tạo alarm.
- Không tạo session tự động.
- Không hiện notification.
- Route settings vẫn có thể truy cập từ Cài đặt để bật feature.

Khi tắt sau khi từng sử dụng:

- Giữ lịch sử cũ.
- Hủy alarm đang chờ.
- Không xóa session/report.
- Không xóa saving destination.

---

# 2. BUSINESS RULE — KHÓA CỨNG

## BR-SS-01 — Bước mệnh giá

Chỉ hỗ trợ:

```text
5.000đ
10.000đ
```

Domain:

```kotlin
enum class SavingSpinStep(val amount: Long) {
    FIVE_THOUSAND(5_000L),
    TEN_THOUSAND(10_000L),
}
```

Không cho phép:

```text
1.000
2.000
11.000
12.000
13.000
14.000
16.000
17.000
18.000
19.000
```

---

## BR-SS-02 — Mức tối thiểu / tối đa

Người dùng tự nhập:

```text
minAmount
maxAmount
```

Không hard-code maximum 100K, 1M, 10M...

**“Tối đa không giới hạn” = không có trần nghiệp vụ do FinLux áp đặt.**

Tuy nhiên hệ thống hiện lưu VND bằng `Long` và rule dự án cho phép tối đa 15 chữ số. Vì vậy:

```text
1 <= amount <= 999_999_999_999_999
```

`maxAmount` phải là một giá trị hữu hạn để thuật toán random hoạt động.

UI KHÔNG dùng `maxAmount = null` để random “vô hạn”.

Trong Settings:

```text
Mức tối đa
[ 5.000.000đ ]

helper:
Không giới hạn mức tối đa cố định của ứng dụng.
```

Nếu mockup cũ đang ghi `Không giới hạn`, khi code thật phải đổi thành **giá trị người dùng đã cấu hình**.

---

## BR-SS-03 — Validation số tiền

```kotlin
minAmount > 0
maxAmount >= minAmount
minAmount % stepAmount == 0L
maxAmount % stepAmount == 0L
```

Ngoài ra số lượng mệnh giá khả dụng phải đủ cho số ô:

```kotlin
candidateCount = ((maxAmount - minAmount) / stepAmount) + 1
candidateCount >= slotCount
```

Nếu không đủ:

```text
Khoảng tiền hiện tại chỉ tạo được 4 mệnh giá.
Hãy tăng mức tối đa hoặc giảm số ô vòng quay.
```

---

## BR-SS-04 — Số ô vòng quay

Cho phép:

```text
6
8
10
12
```

Default:

```text
8
```

Không render hàng trăm mệnh giá lên vòng quay.

---

## BR-SS-05 — Không tạo toàn bộ range trong RAM

Ví dụ:

```text
min = 5.000
max = 999.999.999.995.000
step = 5.000
```

Không được:

```kotlin
(min..max step step).toList()
```

Phải sample theo index.

Thuật toán:

```kotlin
candidateCount = ((max - min) / step) + 1

amountAt(index) = min + index * step
```

Chọn `slotCount` index duy nhất mà không materialize toàn bộ range.

---

## BR-SS-06 — Một lượt không được reroll

Default:

```text
1 lượt / ngày
```

Khi người dùng nhấn QUAY và kết quả đã được lưu:

```text
SPUN_PENDING
```

Đóng popup / kill app / đổi thiết bị / mở lại:

- Vẫn phải thấy đúng kết quả cũ.
- Không tạo wheel mới.
- Không cho quay lần hai để tìm số nhỏ hơn.

---

## BR-SS-07 — Wheel values cũng phải được khóa

Ngay lần đầu mở mini game trong một schedule period:

```text
wheelValues = [...]
```

được tạo và persist vào session.

Nếu mở lại trước khi quay:

- Hiện đúng các ô cũ.
- Không random lại danh sách denomination.

---

## BR-SS-08 — Trạng thái session

```kotlin
enum class SavingSpinStatus {
    READY,
    SPUN_PENDING,
    COMPLETED,
    SNOOZED,
    SKIPPED,
}
```

State machine:

```text
READY
  │
  └── QUAY ───────────────> SPUN_PENDING
                                │
               ┌────────────────┼────────────────┐
               │                │                │
          Xác nhận          Nhắc sau         Bỏ qua
               │                │                │
               ▼                ▼                ▼
          COMPLETED         SNOOZED          SKIPPED
                                │
                                └── mở lại --> SPUN_PENDING
```

`SKIPPED` sau khi đã spin phải giữ `selectedAmount` để audit nhưng không tính vào tổng tiết kiệm.

---

## BR-SS-09 — Completed mới tính tiết kiệm

Không tính:

```text
READY
SPUN_PENDING
SNOOZED
SKIPPED
```

Chỉ:

```text
COMPLETED
```

được tính vào:

- tổng tiết kiệm
- chart
- theo ví
- streak
- completion rate numerator

---

## BR-SS-10 — Hai hình thức tiết kiệm

```kotlin
enum class SavingMethod {
    CASH,
    BANK_TRANSFER,
}
```

### CASH

Ví dụ:

- Heo đất
- Hộp tiết kiệm
- Quỹ tiền mặt

Người dùng bỏ tiền thực tế rồi bấm:

```text
XÁC NHẬN ĐÃ NẠP
```

### BANK_TRANSFER

Ví dụ:

- MB Bank - Tiết kiệm
- Techcombank - Quỹ dự phòng
- Ví ngân hàng khác

FinLux v1 **không được giả vờ đã chuyển khoản ngân hàng**.

Flow:

```text
Quay
→ kết quả
→ người dùng tự chuyển tiền
→ bấm xác nhận
→ FinLux ghi COMPLETED
```

---

## BR-SS-11 — Không làm sai số liệu thu/chi

Một lượt Saving Spin **không tự động tạo INCOME/EXPENSE** vì sẽ làm sai báo cáo thu/chi.

Mặc định:

```text
SavingSpinSession là ledger riêng của minigame.
```

Nếu destination có `linkedWalletId`:

- Chỉ dùng để hiển thị/liên kết.
- Không tự cộng/trừ `Wallet.balance` ở v1.

Nếu sau này triển khai chuyển nội bộ giữa 2 ví FinLux, phải dùng `TransferMoneyUseCase` hiện hữu và liên kết transaction IDs vào session.

---

## BR-SS-12 — Reminder

Default khi bật feature:

```text
09:00
```

Cho phép người dùng đổi giờ.

Notification actions:

```text
QUAY NGAY
NHẮC LẠI
```

Snooze presets:

```text
30 phút
1 giờ
12:00
18:00
```

Nếu thời điểm preset đã qua thì không hiển thị preset đó.

Tối đa 3 notification/lượt/ngày để tránh spam.

---

## BR-SS-13 — Tần suất

V1 hỗ trợ:

```kotlin
enum class SavingSpinFrequency {
    DAILY,
    SELECTED_WEEKDAYS,
    WEEKLY,
    SALARY_CYCLE,
}
```

### DAILY

1 session / ngày.

### SELECTED_WEEKDAYS

Ví dụ:

```text
T2, T4, T6
```

Ngày không chọn không tạo lượt.

### WEEKLY

1 lượt/tuần, ngày trong tuần do user chọn.

### SALARY_CYCLE

Tạo lịch theo `FinancialPeriodResolver`/Salary Cycle hiện hữu.

Không tự tính “tháng” khi kỳ lương đang bật.

---

## BR-SS-14 — Bỏ qua

Nếu:

```text
allowSkip = true
```

hiện:

```text
Bỏ qua hôm nay
```

Nếu `false`, không render action này.

Không ép người dùng phải nạp tiền.

---

# 3. DOMAIN MODEL

Tạo:

`app/src/main/java/com/finlux/app/domain/model/SavingSpinModels.kt`

```kotlin
package com.finlux.app.domain.model

import java.time.Instant

enum class SavingSpinStep(val amount: Long) {
    FIVE_THOUSAND(5_000L),
    TEN_THOUSAND(10_000L),
}

enum class SavingSpinStatus {
    READY,
    SPUN_PENDING,
    COMPLETED,
    SNOOZED,
    SKIPPED,
}

enum class SavingMethod {
    CASH,
    BANK_TRANSFER,
}

enum class SavingSpinFrequency {
    DAILY,
    SELECTED_WEEKDAYS,
    WEEKLY,
    SALARY_CYCLE,
}

data class SavingSpinConfig(
    val enabled: Boolean = false,
    val showOnHome: Boolean = true,
    val minAmount: Money = Money(10_000L),
    val maxAmount: Money = Money(100_000L),
    val step: SavingSpinStep = SavingSpinStep.FIVE_THOUSAND,
    val slotCount: Int = 8,
    val frequency: SavingSpinFrequency = SavingSpinFrequency.DAILY,
    val selectedWeekdays: Set<Int> = emptySet(),
    val weeklyDay: Int = 1,
    val reminderEnabled: Boolean = true,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val snoozeEnabled: Boolean = true,
    val allowSkip: Boolean = true,
    val defaultDestinationId: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)

data class SavingDestination(
    val id: String = "",
    val name: String,
    val method: SavingMethod,
    val linkedWalletId: String? = null,
    val institutionId: String? = null,
    val accountHint: String? = null,
    val enabled: Boolean = true,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)

data class SavingSpinSession(
    val id: String,
    val scheduleKey: String,
    val wheelValues: List<Money>,
    val selectedIndex: Int? = null,
    val selectedAmount: Money? = null,
    val status: SavingSpinStatus = SavingSpinStatus.READY,
    val destinationId: String? = null,
    val method: SavingMethod? = null,
    val spunAt: Instant? = null,
    val completedAt: Instant? = null,
    val skippedAt: Instant? = null,
    val snoozedUntil: Instant? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)

data class SavingSpinReportSummary(
    val savedAmount: Money = Money(0L),
    val completedCount: Int = 0,
    val skippedCount: Int = 0,
    val scheduledCount: Int = 0,
    val completionRate: Int = 0,
    val currentStreak: Int = 0,
)

data class SavingSpinDailyTotal(
    val epochDay: Long,
    val amount: Money,
)

data class SavingSpinDestinationTotal(
    val destinationId: String,
    val destinationName: String,
    val amount: Money,
)
```

Không thêm `SAVING` vào `TransactionType` trong phase này.

---

# 4. REPOSITORY CONTRACT

Tạo:

`app/src/main/java/com/finlux/app/domain/repository/SavingSpinRepository.kt`

```kotlin
interface SavingSpinRepository {
    fun observeConfig(): Flow<SavingSpinConfig>
    suspend fun saveConfig(config: SavingSpinConfig): AppResult<Unit>

    fun observeDestinations(): Flow<List<SavingDestination>>
    suspend fun upsertDestination(destination: SavingDestination): AppResult<String>
    suspend fun deleteDestination(id: String): AppResult<Unit>

    fun observeSession(scheduleKey: String): Flow<SavingSpinSession?>
    suspend fun getOrCreateSession(
        scheduleKey: String,
        wheelValues: List<Money>,
    ): AppResult<SavingSpinSession>

    suspend fun lockSpinResult(
        scheduleKey: String,
        selectedIndex: Int,
    ): AppResult<SavingSpinSession>

    suspend fun completeSession(
        scheduleKey: String,
        destinationId: String,
        method: SavingMethod,
    ): AppResult<Unit>

    suspend fun snoozeSession(
        scheduleKey: String,
        until: Instant,
    ): AppResult<Unit>

    suspend fun skipSession(
        scheduleKey: String,
    ): AppResult<Unit>

    fun observeSessions(
        fromInclusive: Instant,
        toExclusive: Instant,
    ): Flow<List<SavingSpinSession>>
}
```

`lockSpinResult()` phải atomic:

- Chỉ cho transition `READY -> SPUN_PENDING`.
- Nếu đã `selectedIndex != null`, trả session hiện hữu, không ghi kết quả mới.

---

# 5. FIRESTORE SCHEMA

Dùng subcollection dưới user hiện tại.

```text
users/{uid}/savingSpinConfigs/default
users/{uid}/savingSpinDestinations/{destinationId}
users/{uid}/savingSpinSessions/{scheduleKey}
```

## Config document

```json
{
  "enabled": true,
  "showOnHome": true,
  "minAmount": 10000,
  "maxAmount": 5000000,
  "stepAmount": 5000,
  "slotCount": 8,
  "frequency": "DAILY",
  "selectedWeekdays": [1,3,5],
  "weeklyDay": 1,
  "reminderEnabled": true,
  "reminderHour": 9,
  "reminderMinute": 0,
  "snoozeEnabled": true,
  "allowSkip": true,
  "defaultDestinationId": null,
  "createdAt": "Timestamp",
  "updatedAt": "Timestamp"
}
```

## Session document

```json
{
  "scheduleKey": "day:2026-08-31",
  "wheelValues": [10000,15000,20000,25000,30000,35000,40000,50000],
  "selectedIndex": 5,
  "selectedAmount": 35000,
  "status": "COMPLETED",
  "destinationId": "piggy_cash_01",
  "method": "CASH",
  "spunAt": "Timestamp",
  "completedAt": "Timestamp",
  "snoozedUntil": null,
  "skippedAt": null,
  "createdAt": "Timestamp",
  "updatedAt": "Timestamp"
}
```

---

# 6. SCHEDULE KEY — CHỐNG TRÙNG LƯỢT

Tạo:

`app/src/main/java/com/finlux/app/domain/usecase/ResolveSavingSpinScheduleKeyUseCase.kt`

Kết quả:

```text
DAILY:
day:2026-08-31

SELECTED_WEEKDAYS:
day:2026-08-31

WEEKLY:
week:2026-W36

SALARY_CYCLE:
salary:2026-08-25_2026-09-24
```

Với `SALARY_CYCLE`, gọi abstraction kỳ tài chính hiện có; không duplicate logic.

Session ID chính là `scheduleKey` đã sanitize cho Firestore:

```text
day_2026-08-31
week_2026-W36
salary_2026-08-25_2026-09-24
```

---

# 7. THUẬT TOÁN SINH Ô VÒNG QUAY

Tạo:

`app/src/main/java/com/finlux/app/domain/usecase/GenerateSavingSpinWheelUseCase.kt`

Signature:

```kotlin
class GenerateSavingSpinWheelUseCase @Inject constructor() {
    operator fun invoke(
        minAmount: Long,
        maxAmount: Long,
        stepAmount: Long,
        slotCount: Int,
        seed: Long,
    ): List<Money>
}
```

Algorithm:

1. Validate.
2. Tính `candidateCount`.
3. Nếu `candidateCount < slotCount` → error.
4. Luôn đưa `minAmount`.
5. Nếu `slotCount >= 2`, luôn đưa `maxAmount`.
6. Sample unique index cho số slot còn lại.
7. Convert index → amount.
8. Shuffle bằng `Random(seed)`.
9. Không tạo full range.

Pseudo implementation:

```kotlin
val candidateCount = ((maxAmount - minAmount) / stepAmount) + 1L
require(candidateCount >= slotCount)

val random = Random(seed)
val selected = linkedSetOf<Long>()

selected += 0L
if (slotCount > 1) selected += candidateCount - 1L

while (selected.size < slotCount) {
    selected += random.nextLong(candidateCount)
}

return selected
    .map { index -> Money(minAmount + index * stepAmount) }
    .shuffled(random)
```

---

# 8. THUẬT TOÁN CHỌN KẾT QUẢ

V1:

```text
UNIFORM RANDOM
```

Không bí mật ưu tiên số thấp/cao.

```kotlin
selectedIndex = random.nextInt(wheelValues.size)
```

Kết quả phải được gọi qua repository `lockSpinResult()` trước khi animation kết thúc.

Flow:

```text
Tap QUAY
→ ViewModel chọn index
→ repository lockSpinResult()
→ success
→ UI animate đến index đã persist
→ show result
```

Không:

```text
animate trước
→ save sau
```

vì app bị kill giữa animation có thể mất kết quả.

---

# 9. UI STATE / VIEWMODEL

Tạo:

```text
presentation/savingspin/SavingSpinUiState.kt
presentation/savingspin/SavingSpinViewModel.kt
```

State:

```kotlin
data class SavingSpinUiState(
    val isLoading: Boolean = true,
    val config: SavingSpinConfig = SavingSpinConfig(),
    val session: SavingSpinSession? = null,
    val destinations: List<SavingDestination> = emptyList(),
    val selectedDestinationId: String? = null,
    val isSpinning: Boolean = false,
    val errorMessage: String? = null,
)
```

Events:

```kotlin
sealed interface SavingSpinAction {
    data object OpenGame : SavingSpinAction
    data object Spin : SavingSpinAction
    data class SelectDestination(val id: String) : SavingSpinAction
    data object ConfirmDeposit : SavingSpinAction
    data class Snooze(val until: Instant) : SavingSpinAction
    data object Skip : SavingSpinAction
    data object DismissError : SavingSpinAction
}
```

ViewModel không chứa code Canvas/animation.

---

# 10. VISUAL SYSTEM — PHẢI BÁM MOCKUP

## 10.1. Mục tiêu hình ảnh

Phong cách:

```text
Premium fintech
Light / clean
Liquid Glass nhẹ
Rounded cards
Soft shadow
Typography lớn, rõ
Không nhiều chữ nhỏ
Không cartoon hóa toàn màn hình
Mini game vui nhưng vẫn là app tài chính
```

Mockup có màu vàng/cam cho launcher/wheel nhưng code không hard-code mã HEX.

Dùng:

```kotlin
MaterialTheme.colorScheme.primary
MaterialTheme.colorScheme.secondary
MaterialTheme.colorScheme.tertiary
MaterialTheme.colorScheme.error
MaterialTheme.colorScheme.primaryContainer
MaterialTheme.colorScheme.secondaryContainer
MaterialTheme.colorScheme.tertiaryContainer
LocalFinluxTokens.current
```

Nếu thiếu semantic color cần thiết:

- mở rộng design token ở `core/designsystem`;
- không khai báo `Color(0xFF...)` trong package `presentation/savingspin`.

---

# 11. SCREEN 01 — HOME MINI LAUNCHER

Component:

`app/src/main/java/com/finlux/app/presentation/savingspin/components/SavingSpinHomeCard.kt`

Chèn vào:

```text
ClassicHomeScreen
ModernHomeScreen
PrismHomeScreen
```

## Vị trí

Sau cụm tổng quan/KPI tài chính và trước phần analytics/giao dịch gần đây.

Không che FAB/nút Thêm giao dịch.

## Kích thước mục tiêu

```text
horizontal padding: theo Home hiện hữu, thường 16dp
min height: 132dp
corner radius: 24dp
internal padding: 18dp
content gap: 12dp
```

Left zone:

```text
🎯 Vòng quay tiết kiệm
Quay thử xem hôm nay
để dành bao nhiêu nhé

[ clock ] 09:00 sáng
```

Right zone:

```text
mini wheel 96–112dp
button QUAY 96x46dp
```

Typography:

```text
title: titleMedium/titleLarge, Bold
subtitle: bodyMedium
time badge: labelMedium
CTA: titleMedium, Bold
```

Không render emoji nếu font/device hiển thị lệch; ưu tiên Material Icon `TrackChanges`/icon asset đồng nhất.

## State launcher

### Chưa quay

```text
Vòng quay tiết kiệm
Quay thử xem hôm nay...
[QUAY]
```

### Đã quay chưa nạp

```text
35.000đ đang chờ tiết kiệm
[HOÀN TẤT]
```

### Snoozed

```text
Nhắc lại lúc 12:00
[MỞ]
```

### Completed

```text
✓ Hôm nay đã tiết kiệm
35.000đ
🔥 Chuỗi 8 ngày
```

Tap completed → mở report/history, không mở spin mới.

### Disabled

Không render card.

---

# 12. SCREEN 02 — MINI GAME BOTTOM SHEET

File:

`app/src/main/java/com/finlux/app/presentation/savingspin/components/SavingSpinGameSheet.kt`

Dùng modal component hiện hữu nếu đáp ứng:

```text
FinluxModalComponents
```

Nếu dùng `ModalBottomSheet`:

```text
containerColor = Color.Transparent
scrim theo design system
shape top corners ~30dp
```

Không tự viết modal system mới nếu core đã có.

## Layout

```text
drag handle
✨ Vòng quay tiết kiệm ✨
[clock] 1 lượt quay hôm nay

        pointer
      ┌─────────┐
      │  WHEEL  │
      └─────────┘

[        QUAY NGAY        ]

Khoảng tiền: 10.000đ - 100.000đ | Bước 5.000đ

[Nhắc tôi sau]       [Đóng]
```

## Wheel size

Responsive:

```text
<= 340dp screen width: 230–240dp
341–399dp: 250–270dp
>= 400dp: max 280dp
```

Không fixed 300dp gây overflow.

---

# 13. WHEEL CANVAS — KỸ THUẬT VẼ

File:

`app/src/main/java/com/finlux/app/presentation/savingspin/components/SavingSpinWheel.kt`

Dùng Compose `Canvas`.

## Cấu trúc

1. Shadow/rim
2. Segment arcs
3. Divider
4. Amount labels
5. Inner circle
6. Center medallion
7. Pointer nằm ngoài Canvas wheel hoặc layer trên cùng

## Segment angle

```kotlin
val segmentAngle = 360f / values.size
```

Start:

```text
-90°
```

Mỗi wedge:

```kotlin
drawArc(
    color = segmentColor,
    startAngle = -90f + index * segmentAngle,
    sweepAngle = segmentAngle,
    useCenter = true,
)
```

## Text angle

Label nằm khoảng:

```text
radius * 0.68
```

Không để text sát viền.

Amount formatter trên wheel:

```text
10K
15K
100K
1M
1.5M
```

Không render:

```text
1.500.000đ
```

trong sector vì dễ tràn.

---

# 14. WHEEL ANIMATION

Dùng:

```kotlin
Animatable(initialValue = 0f)
```

Duration mục tiêu:

```text
3.2s – 4.2s
```

Default:

```text
3600ms
```

Full rotations:

```text
5–8 vòng
```

Giảm tốc cuối.

Nếu `LocalUiPreferences.current.animationsEnabled == false`:

- Không animation dài.
- Chuyển đến kết quả trong ~150–250ms hoặc ngay lập tức.

## Tính rotation

Giả sử pointer ở top `-90°`.

```kotlin
val segment = 360f / itemCount
val selectedCenterFromTop = (selectedIndex + 0.5f) * segment
val normalizedTarget =
    (360f - (selectedCenterFromTop % 360f)) % 360f

val targetRotation =
    currentBaseRotation + fullTurns * 360f + normalizedTarget
```

Sau animation:

- normalize rotation để state không tăng vô hạn.
- trigger result content.
- haptic 1 lần khi bắt đầu, 1 lần khi dừng.
- không rung liên tục từng sector.

---

# 15. SCREEN 03 — RESULT / CONFIRM

Sau spin, không push route mới.

Chuyển nội dung trong cùng bottom sheet bằng:

```text
AnimatedContent / Crossfade
```

Layout bám mockup:

```text
🎉 Hôm nay bạn tiết kiệm         🔥 Chuỗi 8 ngày

              35.000đ

           Chọn nơi nạp tiền

[🐷 Heo tiền mặt] [🏦 MB Bank] [💳 Dự phòng]

[        XÁC NHẬN ĐÃ NẠP        ]

[           Nhắc tôi sau          ]

             Bỏ qua hôm nay
```

## Amount

```text
fontWeight = ExtraBold
size responsive khoảng 38–52sp
primary color
```

## Destination cards

Phone hẹp:

- Horizontal scroll hoặc 3 item co đều.
- Touch target >= 48dp.
- Selected có border primary + check badge.

Không để text bank account đầy đủ; chỉ:

```text
MB Bank
**** 1234
```

---

# 16. SAVING DESTINATION

Tạo màn/manage bottom sheet nhỏ từ Settings:

```text
Thêm nơi tiết kiệm
```

Fields:

### CASH

```text
Tên: Heo đất
Loại: Tiền mặt
```

### BANK_TRANSFER

```text
Tên: Ví tiết kiệm MB Bank
Ngân hàng: MB Bank
Gợi nhớ tài khoản: **** 1234
```

Không lưu thông tin nhạy cảm như:

- PIN
- mật khẩu
- OTP
- full credential

Có thể dùng catalog ngân hàng hiện hữu của FinLux để hiện logo.

---

# 17. SCREEN 04 — REPORT

Files:

```text
presentation/savingspin/report/SavingSpinReportScreen.kt
presentation/savingspin/report/SavingSpinReportViewModel.kt
presentation/savingspin/report/SavingSpinReportUiState.kt
```

Route:

```text
saving-spin/report
```

Header:

```text
Báo cáo vòng quay
```

Filters:

```text
7 ngày
30 ngày
Tháng
Kỳ lương
```

Có thể thêm:

```text
Tùy chỉnh
```

sau khi core 4 filter hoàn tất.

## Summary card

```text
Đã tiết kiệm
1.245.000đ

23 lượt hoàn thành
5 lượt bỏ qua
87% hoàn thành
```

Công thức:

```kotlin
savedAmount = completed.sumOf { selectedAmount }
completedCount = completed.size
skippedCount = skipped.size

completionRate =
    if (scheduledCount == 0) 0
    else completedCount * 100 / scheduledCount
```

## Daily chart

Chỉ COMPLETED.

Group theo local finance day/timezone helper hiện hữu.

Không dùng `Instant.now()` rải rác nếu project có `FinanceClock`.

## Theo ví

```text
Heo đất      450.000đ   36%
MB Bank      520.000đ   42%
Quỹ du lịch  275.000đ   22%
```

Percentage:

```kotlin
destinationAmount * 100 / totalCompleted
```

## History

```text
31/08/2026   35.000đ   Đã hoàn thành
30/08/2026   50.000đ   Đã hoàn thành
29/08/2026        0đ   Đã bỏ qua
```

Tap row → detail bottom sheet.

---

# 18. STREAK

Tạo:

`CalculateSavingSpinStreakUseCase.kt`

Rule:

- Tính theo các **scheduled period** liên tiếp.
- COMPLETED = nối streak.
- SKIPPED = break.
- READY quá hạn = break.
- SNOOZED trong cùng period chưa kết luận cho đến khi period hết.
- Ngày không nằm lịch SELECTED_WEEKDAYS không làm đứt streak.

Không đơn giản tính `date - 1 day` nếu frequency không phải DAILY.

---

# 19. SCREEN 05 — SETTINGS

Files:

```text
presentation/savingspin/settings/SavingSpinSettingsScreen.kt
presentation/savingspin/settings/SavingSpinSettingsViewModel.kt
presentation/savingspin/settings/SavingSpinSettingsUiState.kt
```

Route:

```text
saving-spin/settings
```

UI bám mockup:

## Group 1

```text
Bật vòng quay tiết kiệm        [ON/OFF]
Hiển thị trên Trang chủ        [ON/OFF]
```

## Group 2

```text
Giờ nhắc                        09:00 >
Nhắc lại khi chưa thực hiện    [ON/OFF]
```

## Group 3

```text
Bước mệnh giá        [5.000đ] [10.000đ]
Mức tối thiểu        10.000đ >
Mức tối đa           5.000.000đ >
```

Helper:

```text
FinLux không áp đặt mức tối đa cố định.
```

## Group 4

```text
Số ô vòng quay       8 >
Tần suất              Mỗi ngày >
Cho phép bỏ qua       [ON/OFF]
```

## Group 5

```text
Nơi tiết kiệm mặc định
Quản lý nơi tiết kiệm
```

## Preview

```text
Xem trước trên Trang chủ
```

Render chính component `SavingSpinHomeCard` ở preview mode.

Không viết component giả riêng cho preview.

---

# 20. SETTINGS VALIDATION UX

Amount editor:

- Numeric keyboard.
- Format VND realtime.
- Không cho decimal.
- Không cho số âm.
- Không overflow Long.

Error examples:

```text
Mức tối thiểu phải là bội số của 5.000đ.
```

```text
Mức tối đa phải lớn hơn hoặc bằng mức tối thiểu.
```

```text
Khoảng tiền chưa đủ 8 mệnh giá cho vòng quay.
```

```text
Số tiền vượt giới hạn dữ liệu của FinLux.
```

Save disabled khi invalid.

---

# 21. NOTIFICATION / ALARM

Không đưa WorkManager vào chỉ để làm feature này.

Tạo theo pattern hiện hữu:

```text
data/local/savingspin/AlarmSavingSpinScheduler.kt
data/local/savingspin/SavingSpinReceiver.kt
domain/repository/SavingSpinScheduler.kt
```

Interface:

```kotlin
interface SavingSpinScheduler {
    fun schedule(config: SavingSpinConfig, nextTrigger: Instant)
    fun cancel()
    fun snooze(until: Instant)
}
```

Notification channel:

```text
finlux_saving_spin
Vòng quay tiết kiệm
```

Copy:

```text
🎯 Đến giờ quay tiết kiệm
Quay một vòng xem hôm nay mình để dành bao nhiêu nhé.
```

Actions:

```text
QUAY NGAY
NHẮC LẠI
```

Tap notification:

```text
MainActivity
destination = "home"
open_saving_spin = true
```

Sau Home render xong:

```text
SavingSpinGameSheet open automatically
```

Không auto spin.

---

# 22. SNOOZE

Khi người dùng chọn:

```text
30 phút
```

Update:

```text
status = SNOOZED
snoozedUntil = ...
```

Schedule exact alarm.

Khi alarm snooze trigger:

- Session chuyển UI trở lại pending context.
- Notification mở mini game.
- Nếu session đã SPUN_PENDING thì mở result, không wheel.
- Nếu session READY thì mở wheel.

---

# 23. HOME DEEP LINK / INTENT

Cần mở mini game từ notification.

Không thêm navigation route chỉ để modal.

Flow:

```text
Intent extra
→ MainActivity
→ NavHost home
→ one-shot SavingSpinOpenRequest
→ HomeScreen
→ show SavingSpinGameSheet
→ consume request
```

Không để recomposition tự mở popup liên tục.

---

# 24. NAVIGATION

Sửa:

`app/src/main/java/com/finlux/app/core/navigation/Routes.kt`

Thêm:

```kotlin
data object SavingSpinReport : Route("saving-spin/report")
data object SavingSpinSettings : Route("saving-spin/settings")
```

Sửa:

`app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt`

Add composables cho 2 route trên.

Game chính là modal trên Home, không cần `saving-spin/game` route.

---

# 25. SETTINGS MAIN ENTRY

Trong Settings hiện tại thêm row:

```text
🎯 Vòng quay tiết kiệm
Tạo thói quen tiết kiệm mỗi ngày
```

Tap:

```text
Route.SavingSpinSettings
```

Entry vẫn hiện dù feature OFF.

---

# 26. HOME — 3 UI STYLES

Project hiện có:

```text
ClassicHomeScreen
ModernHomeScreen
PrismHomeScreen
```

Yêu cầu:

## MODERN_LUXURY

Bám mockup gần nhất:

- soft glass card
- pastel/primary container
- wheel illustration bên phải
- rounded 24dp
- shadow nhẹ
- CTA nổi rõ

## CLASSIC_LIQUID

- dùng Liquid Glass component chuẩn
- cùng layout/content
- không thay đổi business state

## PRISM

- dùng Material `Surface` + Finlux tokens
- không blur
- không glow Liquid Glass
- giữ cùng information hierarchy

Business component/state dùng chung, chỉ visual shell khác.

---

# 27. DESIGN COMPONENT TREE

```text
SavingSpinFeature
│
├── SavingSpinHomeCard
│   ├── SavingSpinHomeCardModern
│   ├── SavingSpinHomeCardClassic
│   └── SavingSpinHomeCardPrism
│
├── SavingSpinGameSheet
│   ├── SavingSpinWheel
│   ├── SavingSpinPrimaryButton
│   └── SavingSpinSnoozeMenu
│
├── SavingSpinResultContent
│   ├── SavingDestinationCard
│   └── SavingSpinStreakBadge
│
├── SavingSpinReportScreen
│   ├── SavingSpinSummaryCard
│   ├── SavingSpinDailyChart
│   ├── SavingSpinDestinationBreakdown
│   └── SavingSpinHistoryList
│
└── SavingSpinSettingsScreen
    ├── SavingSpinConfigGroup
    ├── SavingSpinAmountEditor
    ├── SavingSpinFrequencyEditor
    └── SavingSpinHomeCard(previewMode)
```

---

# 28. DATA MAPPING

Tạo mapper trong:

```text
data/remote/firebase/SavingSpinFirestoreMapper.kt
```

Không map Firestore trực tiếp trong ViewModel.

Handle:

- missing field → default an toàn
- enum unknown → fallback
- timestamp null
- numeric Long
- legacy config nếu schema thay đổi

Schema version:

```text
schemaVersion = 1
```

đặt trong config/session nếu project convention cho phép.

---

# 29. FIRESTORE REPOSITORY

Tạo:

`app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseSavingSpinRepository.kt`

Yêu cầu:

### getOrCreateSession

Dùng transaction hoặc create-idempotent logic.

Pseudo:

```kotlin
firestore.runTransaction { tx ->
    val ref = sessionRef(scheduleKey)
    val snapshot = tx.get(ref)

    if (snapshot.exists()) {
        return@runTransaction snapshot.toSavingSpinSession()
    }

    tx.set(ref, newSessionMap)
    newSession
}
```

### lockSpinResult

```kotlin
firestore.runTransaction { tx ->
    val snapshot = tx.get(ref)
    val current = snapshot.toSavingSpinSession()

    if (current.selectedIndex != null) {
        return@runTransaction current
    }

    require(current.status == READY)

    val amount = current.wheelValues[selectedIndex]

    tx.update(
        ref,
        mapOf(
            "selectedIndex" to selectedIndex,
            "selectedAmount" to amount.value,
            "status" to "SPUN_PENDING",
            "spunAt" to serverTimestamp,
            "updatedAt" to serverTimestamp,
        )
    )
}
```

### completeSession

Chỉ:

```text
SPUN_PENDING
SNOOZED
```

được complete.

Không complete READY.

---

# 30. DEMO REPOSITORY

Vì FinLux hiện có fallback demo mode, thêm:

```text
data/demo/DemoSavingSpinRepository.kt
```

Dùng StateFlow/in-memory tương tự demo repositories hiện hữu.

DI phải chọn:

```text
FirebaseSavingSpinRepository
```

khi Firebase configured,

ngược lại:

```text
DemoSavingSpinRepository
```

Không để feature crash trong demo/offline development mode.

---

# 31. HILT DI

Sửa:

`app/src/main/java/com/finlux/app/data/di/RepositoryModule.kt`

Thêm provider:

```kotlin
@Provides
@Singleton
fun provideSavingSpinRepository(
    demo: DemoSavingSpinRepository,
    auth: FirebaseAuth?,
    firestore: FirebaseFirestore?,
): SavingSpinRepository =
    if (BuildConfig.FIREBASE_CONFIGURED && auth != null && firestore != null) {
        FirebaseSavingSpinRepository(auth, firestore)
    } else {
        demo
    }
```

Bind local scheduler:

```kotlin
@Binds
@Singleton
abstract fun bindSavingSpinScheduler(
    implementation: AlarmSavingSpinScheduler,
): SavingSpinScheduler
```

---

# 32. REPORT QUERY RANGE

Tạo:

```text
GetSavingSpinReportUseCase.kt
```

Inputs:

```kotlin
data class SavingSpinReportRange(
    val fromInclusive: Instant,
    val toExclusive: Instant,
)
```

Không load toàn bộ lịch sử user rồi filter client nếu Firestore query có thể range theo `createdAt/spunAt/completedAt`.

Nếu cần composite index, ghi vào docs và Firebase index config hiện hữu.

---

# 33. EMPTY / ERROR / OFFLINE STATE

## No destination

Sau spin:

```text
Bạn chưa có nơi tiết kiệm.
[+ Thêm nơi tiết kiệm]
```

Không cho confirm khi chưa chọn destination.

## Offline trước spin

Nếu session đã cache:

- cho xem.
- nếu repository không thể lock result, không animation giả.

Show:

```text
Không thể khóa kết quả lúc này.
Kiểm tra kết nối và thử lại.
```

## Offline sau result đã persist

Nếu local/cache có SPUN_PENDING:

- hiển thị result.
- confirm retry được.

## Repository error

Không mất `selectedAmount`.

---

# 34. ACCESSIBILITY

- touch target >= 48dp
- contentDescription cho pointer, wheel, close, result
- amount không chỉ phân biệt bằng màu
- selected destination có check icon + semantic selected state
- hỗ trợ font scale 1.3x tối thiểu
- wheel label nếu quá dài dùng short formatter
- nếu TalkBack, button đọc:
  - `Quay vòng tiết kiệm`
  - sau result: `Kết quả 35.000 đồng`

---

# 35. PERFORMANCE

- Wheel Canvas không allocate list mỗi frame.
- `wheelValues` immutable.
- `remember(values)` cho geometry nếu cần.
- Animation chỉ đổi rotation transform, không recompute Firestore/state.
- Chart report không recompute groupBy mỗi recomposition; tính ở ViewModel/use case.
- Không enumerate range mệnh giá lớn.
- Không tạo alarm khi feature disabled.

---

# 36. SECURITY / PRIVACY

Firestore rules:

- chỉ `request.auth.uid == uid`
- không cho user khác đọc config/session
- accountHint chỉ lưu masked text
- không lưu PIN/OTP/password/token ngân hàng

Session result locking:

- khi `resource.data.selectedAmount != null`, update mới không được thay đổi `selectedAmount`/`selectedIndex`.
- status transition chỉ theo state machine nếu rules hiện hữu đủ khả năng kiểm tra.
- Nếu Firestore Rules quá phức tạp, repository vẫn phải enforce và docs ghi rõ trust boundary.

---

# 37. FILE MAP — TẠO MỚI

```text
app/src/main/java/com/finlux/app/domain/model/SavingSpinModels.kt
app/src/main/java/com/finlux/app/domain/repository/SavingSpinRepository.kt
app/src/main/java/com/finlux/app/domain/repository/SavingSpinScheduler.kt

app/src/main/java/com/finlux/app/domain/usecase/ValidateSavingSpinConfigUseCase.kt
app/src/main/java/com/finlux/app/domain/usecase/GenerateSavingSpinWheelUseCase.kt
app/src/main/java/com/finlux/app/domain/usecase/ResolveSavingSpinScheduleKeyUseCase.kt
app/src/main/java/com/finlux/app/domain/usecase/GetOrCreateSavingSpinSessionUseCase.kt
app/src/main/java/com/finlux/app/domain/usecase/SpinSavingWheelUseCase.kt
app/src/main/java/com/finlux/app/domain/usecase/CompleteSavingSpinUseCase.kt
app/src/main/java/com/finlux/app/domain/usecase/CalculateSavingSpinStreakUseCase.kt
app/src/main/java/com/finlux/app/domain/usecase/GetSavingSpinReportUseCase.kt
app/src/main/java/com/finlux/app/domain/usecase/SyncSavingSpinScheduleUseCase.kt

app/src/main/java/com/finlux/app/data/demo/DemoSavingSpinRepository.kt
app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseSavingSpinRepository.kt
app/src/main/java/com/finlux/app/data/remote/firebase/SavingSpinFirestoreMapper.kt
app/src/main/java/com/finlux/app/data/local/savingspin/AlarmSavingSpinScheduler.kt
app/src/main/java/com/finlux/app/data/local/savingspin/SavingSpinReceiver.kt

app/src/main/java/com/finlux/app/presentation/savingspin/SavingSpinUiState.kt
app/src/main/java/com/finlux/app/presentation/savingspin/SavingSpinViewModel.kt

app/src/main/java/com/finlux/app/presentation/savingspin/components/SavingSpinHomeCard.kt
app/src/main/java/com/finlux/app/presentation/savingspin/components/SavingSpinWheel.kt
app/src/main/java/com/finlux/app/presentation/savingspin/components/SavingSpinGameSheet.kt
app/src/main/java/com/finlux/app/presentation/savingspin/components/SavingSpinResultContent.kt
app/src/main/java/com/finlux/app/presentation/savingspin/components/SavingDestinationCard.kt

app/src/main/java/com/finlux/app/presentation/savingspin/report/SavingSpinReportUiState.kt
app/src/main/java/com/finlux/app/presentation/savingspin/report/SavingSpinReportViewModel.kt
app/src/main/java/com/finlux/app/presentation/savingspin/report/SavingSpinReportScreen.kt

app/src/main/java/com/finlux/app/presentation/savingspin/settings/SavingSpinSettingsUiState.kt
app/src/main/java/com/finlux/app/presentation/savingspin/settings/SavingSpinSettingsViewModel.kt
app/src/main/java/com/finlux/app/presentation/savingspin/settings/SavingSpinSettingsScreen.kt
```

---

# 38. FILE MAP — SỬA

```text
app/src/main/java/com/finlux/app/data/di/RepositoryModule.kt

app/src/main/java/com/finlux/app/core/navigation/Routes.kt
app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt

app/src/main/java/com/finlux/app/presentation/home/HomeScreen.kt
app/src/main/java/com/finlux/app/presentation/home/classic/ClassicHomeScreen.kt
app/src/main/java/com/finlux/app/presentation/home/modern/ModernHomeScreen.kt
app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt

app/src/main/java/com/finlux/app/presentation/settings/SettingsScreen.kt
app/src/main/AndroidManifest.xml

firestore.rules

docs/BA_SPEC.md
docs/UI_SPEC.md
docs/DATA_SPEC.md
docs/CONTEXT.md
docs/PLAN.md
docs/BACKLOG.md
HANDOVER_LOG.md
CHANGELOG.md
```

Nếu Settings đang dispatch theo nhiều style thì chèn entry vào đúng layer chung hoặc từng screen tương ứng, không duplicate business logic.

---

# 39. TEST FILE MAP

```text
app/src/test/java/com/finlux/app/domain/usecase/ValidateSavingSpinConfigUseCaseTest.kt
app/src/test/java/com/finlux/app/domain/usecase/GenerateSavingSpinWheelUseCaseTest.kt
app/src/test/java/com/finlux/app/domain/usecase/ResolveSavingSpinScheduleKeyUseCaseTest.kt
app/src/test/java/com/finlux/app/domain/usecase/SpinSavingWheelUseCaseTest.kt
app/src/test/java/com/finlux/app/domain/usecase/CalculateSavingSpinStreakUseCaseTest.kt
app/src/test/java/com/finlux/app/presentation/savingspin/SavingSpinViewModelTest.kt
app/src/test/java/com/finlux/app/data/local/savingspin/AlarmSavingSpinSchedulerTest.kt

app/src/androidTest/java/com/finlux/app/presentation/savingspin/SavingSpinGameSheetTest.kt
app/src/androidTest/java/com/finlux/app/presentation/savingspin/SavingSpinSettingsScreenTest.kt
app/src/androidTest/java/com/finlux/app/presentation/savingspin/SavingSpinReportScreenTest.kt
```

---

# 40. IMPLEMENTATION TASKS

## Task 1 — Documentation + domain contract

**Files:**
- Modify `HANDOVER_LOG.md`
- Modify `docs/BA_SPEC.md`
- Modify `docs/UI_SPEC.md`
- Modify `docs/DATA_SPEC.md`
- Create `SavingSpinModels.kt`
- Create `SavingSpinRepository.kt`
- Create `SavingSpinScheduler.kt`

- [ ] Ghi PRE-EXECUTION `[IN PROGRESS]`.
- [ ] Add use case nghiệp vụ Vòng quay tiết kiệm vào BA spec.
- [ ] Add 5 visual states/screens vào UI spec.
- [ ] Add Firestore schema vào DATA spec.
- [ ] Add domain enums/models đúng section 3.
- [ ] Compile domain.

Run:

```bash
./gradlew :app:compileDebugKotlin
```

Expected:

```text
BUILD SUCCESSFUL
```

Commit:

```bash
git add HANDOVER_LOG.md docs app/src/main/java/com/finlux/app/domain
git commit -m "feat(saving-spin): define savings spin domain and specs"
```

---

## Task 2 — Config validation + wheel generator TDD

**Files:**
- Create `ValidateSavingSpinConfigUseCase.kt`
- Create `GenerateSavingSpinWheelUseCase.kt`
- Create matching tests

Tests bắt buộc:

```kotlin
@Test
fun `step only accepts 5000 or 10000`() { ... }

@Test
fun `min and max must be multiples of step`() { ... }

@Test
fun `range must contain at least slotCount unique values`() { ... }

@Test
fun `generator returns unique values`() { ... }

@Test
fun `generator keeps every amount inside range`() { ... }

@Test
fun `generator never returns non multiple denomination`() { ... }

@Test
fun `generator handles very large max without enumerating range`() { ... }

@Test
fun `same seed returns same wheel values`() { ... }
```

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "*SavingSpin*"
```

Commit:

```bash
git add app/src/main app/src/test
git commit -m "feat(saving-spin): validate config and generate wheel denominations"
```

---

## Task 3 — Schedule key + frequency

**Files:**
- Create `ResolveSavingSpinScheduleKeyUseCase.kt`
- Create test

Test:

```text
DAILY same day => same key
DAILY next day => new key
WEEKLY same ISO week => same key
SELECTED_WEEKDAYS invalid day => no active schedule
SALARY_CYCLE uses FinancialPeriodResolver
```

Không duplicate salary period calculation.

Commit:

```bash
git commit -am "feat(saving-spin): resolve schedule periods"
```

---

## Task 4 — Firebase repository + demo repository

**Files:**
- Create `FirebaseSavingSpinRepository.kt`
- Create `SavingSpinFirestoreMapper.kt`
- Create `DemoSavingSpinRepository.kt`
- Modify `RepositoryModule.kt`

- [ ] Observe config.
- [ ] CRUD destination.
- [ ] Idempotent get/create session.
- [ ] Atomic result lock.
- [ ] Complete/snooze/skip transitions.
- [ ] Date range session query.
- [ ] Demo fallback.

Run:

```bash
./gradlew :app:testDebugUnitTest
```

Commit:

```bash
git add app/src/main app/src/test
git commit -m "feat(saving-spin): persist saving spin sessions"
```

---

## Task 5 — Firestore rules

**File:**
- Modify `firestore.rules`

Add owner-only rules cho:

```text
savingSpinConfigs
savingSpinDestinations
savingSpinSessions
```

Test bằng emulator test suite nếu repo có.

Tối thiểu verify:

```text
owner can read/write own config
other uid denied
selectedAmount cannot be overwritten after result locked
```

Commit:

```bash
git add firestore.rules
git commit -m "security(saving-spin): protect saving spin firestore data"
```

---

## Task 6 — Core use cases

Create:

```text
GetOrCreateSavingSpinSessionUseCase
SpinSavingWheelUseCase
CompleteSavingSpinUseCase
CalculateSavingSpinStreakUseCase
GetSavingSpinReportUseCase
```

`SpinSavingWheelUseCase`:

```kotlin
suspend operator fun invoke(
    session: SavingSpinSession,
): AppResult<SavingSpinSession>
```

Nếu session đã có result:

```text
return persisted result
```

Không random lần 2.

Tests state transition đầy đủ.

Commit:

```bash
git commit -am "feat(saving-spin): add spin session business use cases"
```

---

## Task 7 — Alarm / notification

Create:

```text
SavingSpinScheduler
AlarmSavingSpinScheduler
SavingSpinReceiver
SyncSavingSpinScheduleUseCase
```

Modify Manifest.

Test:

```text
disabled => no alarm
enabled => alarm at configured time
snooze => new exact alarm
complete => no reminder for current schedule key
notification intent carries open_saving_spin=true
```

Reuse AlarmManager pattern hiện hữu.

Commit:

```bash
git commit -am "feat(saving-spin): schedule saving reminders and snooze"
```

---

## Task 8 — ViewModel/state

Create `SavingSpinViewModel`.

Scenarios:

```text
load disabled
load READY
load SPUN_PENDING
load COMPLETED
spin success
spin failure
confirm destination
snooze
skip
```

Turbine test Flow state.

Commit:

```bash
git commit -am "feat(saving-spin): implement minigame state management"
```

---

## Task 9 — Wheel Canvas + animation

Create `SavingSpinWheel.kt`.

Preview/test configurations:

```text
6 slots
8 slots
10 slots
12 slots
```

Check:

- no clipped label
- pointer aligned
- center readable
- 320dp width
- font scale
- animations disabled

Commit:

```bash
git commit -am "feat(saving-spin): build animated savings wheel"
```

---

## Task 10 — Game + result bottom sheet

Create:

```text
SavingSpinGameSheet
SavingSpinResultContent
SavingDestinationCard
```

Compose tests:

```text
READY shows QUAY NGAY
tap spin disables button while spinning
SPUN_PENDING shows selected amount
completed cannot show QUAY again
allowSkip=false hides skip
no destination disables confirm
```

Commit:

```bash
git commit -am "feat(saving-spin): add spin and deposit confirmation flow"
```

---

## Task 11 — Home launcher integration

Modify 3 Home variants.

Acceptance:

```text
OFF => no card
READY => QUAY
SPUN_PENDING => pending amount
SNOOZED => snooze time
COMPLETED => amount + streak
```

Visual target đặc biệt cho MODERN_LUXURY phải bám mockup.

Không sửa unrelated Home KPI.

Commit:

```bash
git commit -am "feat(saving-spin): add home savings launcher"
```

---

## Task 12 — Settings

Create settings feature + main Settings entry.

Tests:

```text
toggle OFF cancels schedule
toggle ON schedules next
step segmented control only 5k/10k
invalid amount cannot save
slot count validation
frequency editor
preview uses actual component
```

Commit:

```bash
git commit -am "feat(saving-spin): add configurable savings spin settings"
```

---

## Task 13 — Report

Create report screen/viewmodel/use case.

Test data:

```text
completed 35K
completed 50K
skipped
completed 20K destination B
```

Expected:

```text
saved = 105K
completed = 3
skipped = 1
destination totals correct
```

Check 7d/30d/month/salary-cycle ranges.

Commit:

```bash
git commit -am "feat(saving-spin): add saving spin reports and history"
```

---

## Task 14 — Navigation / notification deep link

Modify:

```text
Routes.kt
FinluxNavHost.kt
MainActivity intent handling if required
```

Verify:

```text
notification -> Home -> opens sheet once
report route works
settings route works
back navigation works
rotation/recomposition does not reopen consumed modal
```

Commit:

```bash
git commit -am "feat(saving-spin): wire navigation and notification entry points"
```

---

## Task 15 — Final QA / docs / release hygiene

Run:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Nếu project có lint:

```bash
./gradlew lintDebug
```

Manual QA matrix:

```text
Light
Dark
Classic Liquid
Modern Luxury
Prism
320dp
360dp
390dp
font scale 1.0
font scale 1.3
Firebase mode
Demo mode
offline reopen after result
app kill after spin
device reboot with reminder enabled
```

Sau PASS:

- cập nhật `docs/CONTEXT.md`
- cập nhật `docs/PLAN.md`
- cập nhật `docs/BACKLOG.md`
- cập nhật `HANDOVER_LOG.md` → `[DONE]`
- cập nhật `CHANGELOG.md` theo workflow release của repo

Commit cuối feature:

```bash
git add .
git commit -m "feat(saving-spin): complete savings wheel minigame"
```

---

# 41. UNIT TEST ACCEPTANCE MATRIX

| Case | Expected |
|---|---|
| step=5K, min=10K, max=100K | valid |
| step=10K, min=10K, max=100K | valid |
| step=5K, min=11K | invalid |
| step=10K, max=105K | invalid |
| max < min | invalid |
| range has 4 values, slots=8 | invalid |
| very large max | no OOM |
| same schedule key open twice | same session |
| same READY session open twice | same wheel values |
| spin twice | same selected result |
| confirm READY | rejected |
| complete SPUN_PENDING | success |
| skip COMPLETED | rejected |
| completed only report | correct |
| selected weekdays non-scheduled day | no active challenge |
| feature off | no reminder |
| completed current period | no repeated reminder |

---

# 42. UI ACCEPTANCE — SO SÁNH VỚI MOCKUP

## Home

- Card mini game nằm ngay dưới tổng quan tài chính.
- Title nổi bật.
- Wheel preview nằm bên phải.
- Nút `QUAY` rõ và lớn.
- Không làm Home chật.
- Không font nhỏ li ti.
- Không lấn bottom nav.

## Game sheet

- Background Home dim nhẹ.
- Sheet bo góc lớn.
- Wheel chiếm visual focus.
- Pointer đỏ/semantic accent ở top.
- 8 slot demo phải rõ.
- CTA xanh/primary dạng full width.
- Range + step hiển thị dưới CTA.
- Có Nhắc sau + Đóng.

## Result

- `35.000đ` là visual hierarchy cao nhất.
- Destination card dễ bấm.
- Selected card có border + check.
- Confirm full width.
- streak badge nhỏ gọn.
- skip là tertiary action.

## Report

- Summary card đầu.
- Chart ngay dưới.
- Breakdown theo destination.
- History cuối.
- Filter top dạng segmented/chips.

## Settings

- grouped list giống app ngân hàng.
- switch rõ.
- segmented 5K/10K.
- amount value căn phải.
- preview Home card ở cuối.
- không dùng form dày đặc kiểu web admin.

---

# 43. DEFINITION OF DONE

Feature chỉ được coi là DONE khi:

- [ ] Người dùng có thể bật/tắt.
- [ ] Có launcher Home.
- [ ] Tap launcher mở mini game ngay.
- [ ] Wheel render 6/8/10/12 slot.
- [ ] Step chỉ 5K hoặc 10K.
- [ ] User tự set min/max.
- [ ] Không hard-code trần max.
- [ ] Không OOM với range rất lớn.
- [ ] Mỗi period chỉ một result.
- [ ] Kill/reopen vẫn giữ result.
- [ ] Có CASH và BANK_TRANSFER destination.
- [ ] Confirm mới tính savings.
- [ ] Có snooze.
- [ ] Có 09:00 default.
- [ ] Có frequency.
- [ ] Có skip tùy config.
- [ ] Có report 7d/30d/month/salary-cycle.
- [ ] Có streak.
- [ ] Theme Classic/Modern/Prism không vỡ.
- [ ] Dark mode đọc được.
- [ ] Firestore rules đã thêm.
- [ ] Unit tests pass.
- [ ] Debug build pass.
- [ ] Docs sync.
- [ ] HANDOVER_LOG `[DONE]`.
- [ ] Không làm sai Thu/Chi/Net Worth.
- [ ] Không thêm secret.
- [ ] Không có reroll loophole.

---

# 44. GỢI Ý THỨ TỰ AI NÊN THỰC THI

```text
1. Docs + Model
2. Validation + Generator
3. Schedule Key
4. Repository + Firestore
5. Security Rules
6. Use Cases
7. Reminder Scheduler
8. ViewModel
9. Wheel Canvas
10. Game Sheet
11. Result Sheet
12. Home Launcher
13. Settings
14. Report
15. Navigation + Deep Link
16. QA + Docs + Build
```

Không làm UI trước repository/state machine, nếu không sẽ dễ tạo demo đẹp nhưng dữ liệu/flow sai.

---

# 45. QUYẾT ĐỊNH KIẾN TRÚC QUAN TRỌNG

## Không sử dụng “random vô hạn”

User được phép đặt max rất lớn, nhưng **mỗi config luôn có finite `maxAmount`**.

## Không enumerate denomination range

Sample bằng index.

## Không dùng INCOME để ghi “tiết kiệm”

Mini game ledger riêng.

## Không reroll

Session + result persistent.

## Không ép người dùng

Opt-in + allow skip + snooze.

## Không thêm WorkManager riêng

Theo AlarmManager pattern đang dùng trong FinLux.

## Không fork giao diện 3 lần

Business component/state dùng chung; shell theme-specific.

## Không hard-code màu để bắt chước ảnh

Bắt chước layout, hierarchy, spacing, animation bằng dynamic semantic tokens.

---

# 46. OUTPUT MONG MUỐN SAU KHI AI THỰC HIỆN

AI bàn giao phải trả:

```text
1. Danh sách file đã tạo
2. Danh sách file đã sửa
3. Business rules đã implement
4. Screenshot từng màn:
   - Home launcher
   - Game wheel
   - Result confirm
   - Report
   - Settings
5. Test output
6. Build output
7. Firestore schema/rules update
8. Các vấn đề còn tồn tại nếu có
9. Commit hash
10. Version/release nếu được yêu cầu
```

Không được chỉ trả “đã hoàn thành”.
