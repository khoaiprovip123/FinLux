# KẾ HOẠCH THIẾT KẾ & TRIỂN KHAI `FinluxDateTimePickerSheet`
## (Liquid Glass DateTime Picker Sheet & Double Protection Chặn Giao Dịch Tương Lai)

> **Mã kế hoạch:** PLAN-DATETIME-PICKER-2026-09-24  
> **Người lập:** Tech Lead / AI Coding Agent  
> **Trạng thái:** ✅ ĐÃ PHÊ DUYỆT (Tech Lead Approved - Executing)  
> **Mục tiêu:** Thay thế triệt để `DatePickerDialog` & `TimePickerDialog` mặc định của Android bằng BottomSheet Liquid Glass đồng bộ, gộp 2 bước thành 1 chạm, và thiết lập chốt chặn giao dịch tương lai (Double Protection: UI + Domain).

---

## 1. KHẢO SÁT HIỆN TRẠNG MÃ NGUỒN (INVESTIGATION & REALITY CHECK)

### 1.1. Hiện trạng lỗi & Bằng chứng thực tế
- **Triệu chứng:** Người dùng có thể chọn ngày giao dịch là `26/09/2026` trong khi thời gian thực tế của hệ thống là `11:24 AM 24/09/2026`.
- **Hệ quả kế toán:**
  1. *Làm sai lệch báo cáo dòng tiền (Cashflow Reports):* Thu/Chi phát sinh ở tương lai nhưng đã bị trừ/cộng ngay vào số dư ví hiện tại.
  2. *Biến dạng chu kỳ lương & Ngân sách (Budget & Salary Cycle):* Giao dịch tương lai bị gán vào chu kỳ ngân sách không đúng thời điểm, gây cảnh báo ảo hoặc tính sai hạn mức chi tiêu còn lại.
  3. *Trải nghiệm người dùng vỡ vụn:* Giao diện chọn ngày hiện là popup hộp trắng (`DatePickerDialog` M3 thô), sau khi bấm "Tiếp tục (Chọn giờ)" lại bật tiếp một native Android dialog thứ hai (`TimePickerDialog`), hoàn toàn phá vỡ trải nghiệm Liquid Glass sang trọng của Finlux.

### 1.2. Rà soát các vị trí gọi DatePicker / TimePicker
Qua rà soát toàn bộ codebase Finlux, các vị trí đang can thiệp chọn thời gian gồm:
1. **`com.finlux.app.core.designsystem.component.form.FinluxFormControls.kt`** (Dòng 131 - 270):
   - Đang chứa composable dùng chung: `FinluxDateTimePicker(selectedDateTime, onDateTimeChange, ...)`.
   - **Bản chất hiện tại:** Click vào card form -> Bật `DatePickerDialog` (Material 3) -> Chọn ngày -> Nhấn "Tiếp tục (Chọn giờ)" -> Bật tiếp native `TimePickerDialog` (Android context).
   - **Khiếm khuyết:**
     + Dùng `rememberDatePickerState(initialSelectedDateMillis)` mà **không truyền `selectableDates`**, dẫn đến chọn được mọi ngày trong tương lai.
     + Sử dụng `ZoneId.systemDefault()` và `ZoneOffset.UTC` rải rác, không quy chuẩn về `FinanceTime.defaultZone`.
2. **`AddTransactionSheet.kt`** (Dòng 403):
   - Dùng cho cả 2 luồng: **Tạo giao dịch mới** (`initialTransaction == null`) và **Sửa giao dịch** (`initialTransaction != null`, header "Sửa giao dịch").
   - Đang gọi trực tiếp `FinluxDateTimePicker`.
3. **`TransferMoneyScreen.kt`** (Dòng 269):
   - Chọn thời gian chuyển tiền giữa các ví, gọi `FinluxDateTimePicker`.
4. **`RecordDealOutlaySheet.kt`** (Dòng 144) & **`RecordDealInflowSheet.kt`** (Dòng 241):
   - Ghi nhận chi xuất vốn và thu hồi vốn/lời của Thương Vụ Đầu Tư, gọi `FinluxDateTimePicker`.
5. **`GoalsScreen.kt`** (Dòng 530):
   - Chọn hạn hoàn thành mục tiêu (Deadline).
   - ⚠️ **Phát hiện quan trọng (Domain Invariant):** Deadline của mục tiêu tài chính **BẮT BUỘC PHẢI Ở TƯƠNG LAI**. Nếu chặn cứng ngày tương lai toàn cục thì màn hình Mục Tiêu sẽ bị gãy. Do đó component mới bắt buộc phải có cờ cấu hình `allowFutureDates: Boolean = false`.
6. **Các màn hình đặc thù khác:**
   - `ReportsScreen.kt`: Dùng `DateRangePicker` chọn khoảng lọc báo cáo (hợp lệ cho việc lọc dữ liệu quá khứ).
   - `RemindersScreen.kt`: Dùng cho nhắc nhở định kỳ (cho phép ngày tương lai).

### 1.3. Hạ tầng xử lý thời gian hiện tại
- **`FinanceTime.kt`**:
  - `FinanceTime.VIETNAM_ZONE` = `ZoneId.of("Asia/Ho_Chi_Minh")`.
  - `FinanceTime.defaultZone`: Single Source of Truth cho toàn bộ nghiệp vụ ngày/tháng/chu kỳ.
  - Các hàm tiện ích: `formatSmartDateTime`, `formatDate`, `formatTime`, `financialMonth`.
- **`FinanceBusinessConstants.Timezone.DEFAULT_ZONE_ID`**: Zone chuẩn hệ thống.
- **Vấn đề lệch múi giờ:** `FinluxDateTimePicker` cũ tự convert sang `ZoneOffset.UTC` rồi lại dùng `ZoneId.systemDefault()`, dễ gây lệch ngày khi chạy trên máy có múi giờ khác GMT+7 hoặc khi chọn mốc 00:00 - 07:00 sáng.

### 1.4. Tầng Domain & Chốt chặn validation
- Rà soát `AddTransactionUseCase.kt` và `EditTransactionUseCase.kt`:
  - Cả hai đều ủy quyền kiểm tra tính hợp lệ qua hàm `validateTransaction(transaction)` tại `TransactionValidation.kt`.
  - Kiểm tra `TransactionValidation.kt`:
    ```kotlin
    // Hiện chỉ có:
    // - amount.value <= 0L
    // - amount.value > MAX_AMOUNT
    // - walletId.isBlank()
    // - categoryId.isNullOrBlank()
    // - relatedWalletId.isNullOrBlank()
    // ==> HOÀN TOÀN KHÔNG CÓ BẤT KỲ DÒNG NÀO KIỂM TRA transaction.date!
    ```
  - Kiểm tra `TransferMoneyUseCase.kt`:
    - Nhận tham số `date: Instant = Instant.now()`.
    - Hoàn toàn chưa kiểm tra `date` có vượt quá thời điểm hiện tại hay không.

---

## 2. THIẾT KẾ KIẾN TRÚC `FinluxDateTimePickerSheet` (LIQUID GLASS SPEC)

### 2.1. Vị trí & Quy hoạch mã nguồn
- Tạo mới file: `app/src/main/java/com/finlux/app/core/designsystem/component/form/FinluxDateTimePickerSheet.kt`.
- Refactor `FinluxDateTimePicker` trong `FinluxFormControls.kt`:
  - Giữ nguyên component `FinluxDateTimePicker` làm Form Card Trigger (để 100% màn hình hiện tại không bị gãy API).
  - Thay thế khối `if (showDatePickerDialog)` cũ bằng `FinluxDateTimePickerSheet`.
  - Thêm tham số `allowFutureDates: Boolean = false` vào `FinluxDateTimePicker` (mặc định là `false` cho các giao dịch Thu/Chi/Chuyển/Deal; `GoalsScreen` truyền `allowFutureDates = true`).

### 2.2. Chi tiết UX & Giao diện Liquid Glass (All-in-one Sheet)
Component được thiết kế dạng `ModalBottomSheet` Liquid Glass, tích hợp toàn bộ thao tác trong 1 màn hình cuộn/xem duy nhất:

```
┌──────────────────────────────────────────────────────────┐
│                         ─── (Drag Handle)                 │
│  [📅 Icon] Chọn ngày & giờ giao dịch                [✕]  │
├──────────────────────────────────────────────────────────┤
│  ⚡ CHỌN NHANH (QUICK CHIPS)                              │
│  [ Hôm nay ]  [ Hôm qua ]  [ 2 ngày trước ]  [ Tùy chọn ] │
├──────────────────────────────────────────────────────────┤
│  📅 LỊCH THÁNG (LIQUID GLASS CALENDAR)                    │
│      ⟨               Tháng 9, 2026               ⟩*       │
│      T2    T3    T4    T5    T6    T7    CN               │
│             1     2     3     4     5     6               │
│       7     8     9    10    11    12    13               │
│      14    15    16    17    18    19    20               │
│      21    22    23   (24)  [25]  [26]  [27]  <-- Mờ &    │
│      28    29    30                               Disable │
│  (* Nút Next tháng bị disable nếu tháng sau ở tương lai)   │
├──────────────────────────────────────────────────────────┤
│  ⏰ GIỜ GIAO DỊCH (INTEGRATED TIME PICKER)                │
│  Chips: [ Hiện tại 11:24 ] [ Sáng 08:00 ] [ Trưa 12:00 ]  │
│                                                          │
│     ┌─────────┐   :   ┌─────────┐                        │
│     │   11    │       │   24    │  (Cuộn/Chỉnh Giờ-Phút) │
│     └─────────┘       └─────────┘                        │
├──────────────────────────────────────────────────────────┤
│  [  ✓ Áp dụng: Hôm nay, 24/09/2026 • 11:24            ]  │
└──────────────────────────────────────────────────────────┘
```

#### Quy chuẩn Visual Liquid Glass:
1. **Container:** `ModalBottomSheet` với `containerColor = tokens.surface`, bo góc trên `tokens.radius.bottomSheet` (28.dp), viền kính mờ `tokens.border`.
2. **Quick Chips (Chọn nhanh 1 chạm):**
   - Hàng chip ngang gồm: `Hôm nay`, `Hôm qua`, `2 ngày trước`.
   - Bấm vào chip -> Tự động cập nhật ngày tương ứng theo múi giờ `FinanceTime.defaultZone`.
3. **Calendar Month Grid:**
   - Tự dựng bằng Compose (7 cột tương ứng Thứ 2 -> Chủ Nhật theo chuẩn Việt Nam).
   - Ô ngày bình thường: Text màu `tokens.onSurface`.
   - Ô ngày hôm nay: Viền outline `tokens.primary`, chữ semi-bold.
   - Ô ngày được chọn: Nền `tokens.primary`, chữ trắng đậm, hiệu ứng bóng kính.
   - **Chặn tương lai:** Nếu `allowFutureDates == false`, mọi ngày có `date > today` (theo múi giờ Việt Nam):
     * Độ mờ: `alpha = 0.25f`.
     * Tương tác: `enabled = false` (click không có tác dụng, ripple tắt).
   - Nút sang tháng sau (`>`): Bị disable nếu tháng kế tiếp thuộc về tương lai.
4. **Tích hợp Giờ & Phút (No Second Dialog):**
   - Đặt ngay bên dưới lịch tháng.
   - Hàng chip giờ nhanh: `[ Hiện tại ]`, `[ Sáng 08:00 ]`, `[ Trưa 12:00 ]`, `[ Tối 19:00 ]`.
   - Bộ chọn Giờ (00-23) và Phút (00-59) tối giản, hiện đại, hỗ trợ nút bấm tăng giảm `+ / -` hoặc picker cuộn kính mờ.
   - **Ràng buộc an toàn:** Nếu người dùng chọn ngày "Hôm nay", giờ:phút được chọn không được vượt quá giờ:phút hiện tại (nếu vượt quá sẽ tự động clamp về thời điểm hiện tại).
5. **Nút bấm xác nhận:**
   - Full-width button ở chân sheet với nhãn thông minh: `"Áp dụng: dd/MM/yyyy • HH:mm"`.
   - Nhấn nút sẽ trả về `Instant` chuẩn và đóng sheet.

### 2.3. Hợp đồng API Composable (API Contract)
```kotlin
@Composable
fun FinluxDateTimePickerSheet(
    selectedDateTime: Instant,
    onDateTimeSelected: (Instant) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Thời gian giao dịch",
    allowFutureDates: Boolean = false,
    zoneId: ZoneId = FinanceTime.defaultZone,
)
```

### 2.4. 5 Đặc Tả Kỹ Thuật Bắt Buộc Được Tech Lead Phê Duyệt

#### 2.4.1. Chặn giờ tương lai trong ngày hiện tại (Intra-day Validation)
Khi `allowFutureDates == false`:
- **Ngày được chọn < Hôm nay:** Cho phép tự do chọn từ `00:00` đến `23:59`.
- **Ngày được chọn == Hôm nay:** Giờ và phút bắt buộc `<= LocalTime.now(FinanceTime.defaultZone)`.
- **Quick chips giờ:** Các mốc giờ định sẵn (ví dụ: Trưa 12:00, Tối 19:00) nếu vượt quá thời điểm hiện tại thì tự động disable (`enabled = false`, `alpha = 0.3f`).
- **Chỉnh tay:** Nếu người dùng chỉnh tay vượt quá giờ phút hiện tại, hệ thống tự động clamp về mốc giờ phút hiện tại.

#### 2.4.2. Quy hoạch hằng số chuẩn quản trị kiến trúc (Governance Rule II.11)
- Khai báo tập trung tại `app/src/main/java/com/finlux/app/domain/model/FinanceBusinessConstants.kt`:
  ```kotlin
  const val TRANSACTION_CLOCK_SKEW_TOLERANCE_SECONDS: Long = 60L
  ```
- File `TransactionValidation.kt` và `TransferMoneyUseCase.kt` đọc trực tiếp từ `FinanceBusinessConstants.TRANSACTION_CLOCK_SKEW_TOLERANCE_SECONDS`, tuyệt đối không dùng số ma thuật (magic numbers) hay literal cục bộ.

#### 2.4.3. Tối ưu giao diện chống tràn màn hình (Zero Overflow)
- **Kích thước ô ngày:** Duy trì mức `36.dp` đến `38.dp`, khoảng cách giữa các hàng `4.dp`.
- **Bộ chọn Giờ & Phút:** Thiết kế dạng thanh ngang nhỏ gọn (ô hiển thị số với nút tăng giảm hoặc bộ cuộn gọn). Tuyệt đối không nhúng mặt đồng hồ kim tròn to của Material 3.
- **Scroll Container:** Toàn bộ nội dung BottomSheet được bọc trong `Column` có `modifier = Modifier.verticalScroll(rememberScrollState())` để đảm bảo hiển thị hoàn hảo trên mọi kích thước màn hình mà không che khuất nút Áp dụng.

#### 2.4.4. Đồng bộ múi giờ toàn hệ thống
- Toàn bộ chuyển đổi giữa `Instant`, `LocalDate`, `LocalTime`, và `YearMonth` sử dụng duy nhất `FinanceTime.defaultZone` (`Asia/Ho_Chi_Minh`).
- Triệt tiêu hoàn toàn việc dùng `ZoneOffset.UTC` hoặc `ZoneId.systemDefault()` thủ công.

#### 2.4.5. Ma trận test biên (Boundary Test Cases)
Bổ sung tối thiểu 4 test case vào `TransactionUseCasesTest.kt`:
- **Test 1:** Tạo giao dịch với ngày mai -> Bị chặn, trả về `AppResult.Error`.
- **Test 2:** Tạo giao dịch lệch tương lai 30 giây (nhỏ hơn dung sai 60s) -> Hợp lệ, trả về `AppResult.Success`.
- **Test 3:** Tạo giao dịch lệch tương lai 65 giây (vượt dung sai 60s) -> Bị chặn, trả về `AppResult.Error`.
- **Test 4:** Sửa giao dịch đã có sang ngày mai -> Bị chặn, trả về `AppResult.Error`.

---

## 3. RÀNG BUỘC DOMAIN (DOUBLE PROTECTION ARCHITECTURE)

### 3.1. Chốt chặn trong `TransactionValidation.kt`
Cập nhật hàm `validateTransaction`:
```kotlin
internal fun validateTransaction(
    transaction: FinanceTransaction,
    allowFutureDates: Boolean = false,
): AppResult<Unit> {
    // ... các kiểm tra tiền và ví hiện hữu ...
    
    if (!allowFutureDates) {
        val nowWithTolerance = Instant.now().plusSeconds(FinanceBusinessConstants.TRANSACTION_CLOCK_SKEW_TOLERANCE_SECONDS)
        if (transaction.date.isAfter(nowWithTolerance)) {
            return AppResult.Error("Thời gian giao dịch không được vượt quá thời điểm hiện tại")
        }
    }
    
    return AppResult.Success(Unit)
}
```

### 3.2. Chốt chặn trong `TransferMoneyUseCase.kt`
Thêm kiểm tra ngày chuyển tiền:
```kotlin
val nowWithTolerance = Instant.now().plusSeconds(FinanceBusinessConstants.TRANSACTION_CLOCK_SKEW_TOLERANCE_SECONDS)
if (date.isAfter(nowWithTolerance)) {
    return AppResult.Error("Thời gian chuyển tiền không được vượt quá thời điểm hiện tại")
}
```

---

## 4. LỘ TRÌNH THỰC THI (ACTION PLAN)

- **Bước 1:** Cập nhật tài liệu `docs/plan_custom_datetime_picker_sheet.md` (ĐÃ HOÀN TẤT).
- **Bước 2:** Tạo mới file `app/src/main/java/com/finlux/app/core/designsystem/component/form/FinluxDateTimePickerSheet.kt`.
- **Bước 3:** Nâng cấp `FinluxDateTimePicker` trong `FinluxFormControls.kt` để gọi Sheet mới (mặc định `allowFutureDates = false`), cập nhật `GoalsScreen.kt` truyền `allowFutureDates = true`.
- **Bước 4:** Cập nhật tầng Domain gồm `FinanceBusinessConstants.kt`, `TransactionValidation.kt` và `TransferMoneyUseCase.kt`.
- **Bước 5:** Thêm Unit Tests vào `TransactionUseCasesTest.kt`.
- **Bước 6:** Chạy lệnh kiểm thử `.\gradlew.bat compileDebugKotlin` và `.\gradlew.bat testDebugUnitTest` (Bắt buộc 100% test pass, tổng số test tăng lên vượt mốc 521).
- **Bước 7:** Miễn trừ đóng gói APK (không chạy assembleDebug) và không gọi bất kỳ lệnh ADB nào.
- **Bước 8:** Tăng version trong `app/build.gradle.kts` lên `v1.25.16 (versionCode 190)`, cập nhật `CHANGELOG.md`, `HANDOVER_LOG.md`, sau đó chạy `git add`, `git commit` và `git push` lên remote.

---

## 5. NÂNG CẤP PHASE 2: MODULAR PICKER SUITE & BÁNH XE CUỘN CUPERTINO iOS (ĐÃ HOÀN TẤT)

### 5.1. Mục tiêu nâng cấp
- Thay thế hoàn toàn nút stepper `+`/`-` bằng bánh xe cuộn mượt mà phong cách iOS (`FinluxWheelPicker`) sử dụng `LazyColumn` + `rememberSnapFlingBehavior`.
- Tách thành 3 Component Sheets độc lập:
  1. `FinluxDateTimePickerSheet`: Chọn cả Ngày & Giờ (AddTransaction, Transfer, Deals).
  2. `FinluxTimePickerSheet`: Chuyên chọn Giờ:Phút (SavingSpin, Reminders).
  3. `FinluxDatePickerSheet`: Chuyên chọn Ngày (Goals, Reminders).
- Mở rộng dải Quick Date Chips (`LazyRow`) và Quick Time Chips (`LazyRow`) tự động cuộn bánh xe về đúng số.
- Thêm `accentColor` thích ứng ngữ cảnh: Chi tiêu (`ExpenseRed`), Thu nhập (`IncomeGreen`), Nhắc nhở (`WarningAmber`), Mặc định (`tokens.primary`).
- Quét sạch toàn bộ Zombie Code (`DatePickerDialog`, `rememberDatePickerState`, `android.app.TimePickerDialog`).
- Đạt 100% Quality Gate: Compile sạch, 525/525 tests PASS, đóng gói v1.25.17 (191) và nạp thiết bị thật.

