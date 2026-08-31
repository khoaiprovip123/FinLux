# TÀI LIỆU QUY CHUẨN: BỘ COMPONENT FORM & PICKER TIÊU CHUẨN (FINLUX DESIGN SYSTEM)

> **Mục đích:** Tài liệu này đặc tả chi tiết toàn bộ các component biểu mẫu (Form Rows, Input Cards) và Modal chọn lựa (Wallet Picker, Category Picker) tiêu chuẩn trong ứng dụng **Finlux**. Mọi Developer và AI Coding Agent khi tham gia phát triển tính năng mới **BẮT BUỘC** phải kế thừa các component này, không tự ý tạo lại từ đầu (Tuân thủ Nguyên tắc cốt lõi #2: Tái sử dụng & tránh phân mảnh code).

---

## 📍 1. Vị Trí Lưu Trữ Mã Nguồn

Toàn bộ component tiêu chuẩn được đặt tập trung tại:
- **Form & Pickers Component:** [`app/src/main/java/com/finlux/app/core/designsystem/component/FinluxFormComponents.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/core/designsystem/component/FinluxFormComponents.kt)
- **Amount Hero Component:** [`app/src/main/java/com/finlux/app/core/designsystem/component/FinluxTransactionComponents.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/core/designsystem/component/FinluxTransactionComponents.kt)
- **Package:** `com.finlux.app.core.designsystem.component.*`

---

## 🧱 2. Danh Sách & Đặc Tả Chi Tiết Các Component Tiêu Chuẩn

### 1️⃣ `FinluxCategoryPickerBottomSheet` — Bộ Chọn Danh Mục Chuẩn
Modal Bottom Sheet chọn danh mục chi tiêu / thu nhập dạng lưới Grid 4 cột có thanh tìm kiếm.

* **Đặc điểm thiết kế:**
  - Header có tiêu đề "Chọn danh mục" + nút đóng `[x]`.
  - Thanh tìm kiếm danh mục (Search Bar) bo tròn 14dp tích hợp nút xóa tìm kiếm.
  - Lưới **Grid 4 cột** (`LazyVerticalGrid(columns = GridCells.Fixed(4))`) hiển thị icon badge màu sắc động theo token (`tokens.primary` / `cat.colorHex`).
  - Badge checkmark đỏ/accent nổi bật ở góc trên bên phải của danh mục đang được chọn.
  - Nút `+ Thêm danh mục mới` ở đáy sheet (khi có truyền callback `onAddNew`).
  - Hỗ trợ nhấn giữ (`onLongPressCategory`) để chỉnh sửa/xóa danh mục tùy chỉnh.

* **Khởi tạo & Sử dụng:**
```kotlin
import com.finlux.app.core.designsystem.component.FinluxCategoryPickerBottomSheet

if (showCategoryPicker) {
    FinluxCategoryPickerBottomSheet(
        categories = categoriesList,
        selectedCategoryId = selectedCategoryId,
        onSelectCategory = { category ->
            selectedCategoryId = category.id
            showCategoryPicker = false
        },
        onDismiss = { showCategoryPicker = false },
        onAddNew = { /* Mở dialog tạo danh mục mới nếu cần */ },
        onLongPressCategory = { category -> /* Chỉnh sửa danh mục nếu cần */ },
    )
}
```

* **Các màn hình đang kế thừa:**
  - [`AddTransactionSheet.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/transaction/AddTransactionSheet.kt) (Thêm chi tiêu / thu nhập).
  - [`NotificationsScreen.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/notifications/NotificationsScreen.kt) (Modal QuickPay xác nhận thanh toán).

---

### 2️⃣ `FinluxWalletPickerBottomSheet` — Bộ Chọn Ví Tài Khoản Chuẩn
Modal Bottom Sheet chọn ví nguồn / ví nhận tiền dạng danh sách thẻ kính.

* **Đặc điểm thiết kế:**
  - Header có tiêu đề "Chọn ví tài khoản" + nút đóng `[x]`.
  - Danh sách thẻ Surface bo góc 16dp với viền kính `tokens.border`.
  - Icon badge tròn đại diện cho từng loại ví (`walletIcon(wallet.type)`: Tiền mặt, Ngân hàng, Thẻ tín dụng, Ví điện tử...).
  - Tên ví hiển thị 15sp SemiBold; phụ đề hiển thị **Số dư khả dụng** định dạng phân tách hàng nghìn VNĐ (`formatVndAmount`).
  - Checkmark xanh/accent bên phải khi ví được chọn.

* **Khởi tạo & Sử dụng:**
```kotlin
import com.finlux.app.core.designsystem.component.FinluxWalletPickerBottomSheet

if (showWalletPicker) {
    FinluxWalletPickerBottomSheet(
        wallets = walletsList,
        selectedWalletId = selectedWalletId,
        onSelectWallet = { wallet ->
            selectedWalletId = wallet.id
            showWalletPicker = false
        },
        onDismiss = { showWalletPicker = false },
    )
}
```

* **Các màn hình đang kế thừa:**
  - [`AddTransactionSheet.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/transaction/AddTransactionSheet.kt) (Chọn ví thanh toán / nhận tiền).
  - [`DebtPaymentSheet.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/debt/DebtPaymentSheet.kt) (Chọn ví nguồn trích tiền trả nợ).
  - [`NotificationsScreen.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/notifications/NotificationsScreen.kt) (Chọn ví thanh toán từ thông báo).

---

### 3️⃣ `ErgonomicFormRow` — Hàng Chọn Dữ Liệu 2 Dòng Tiêu Chuẩn
Hàng chọn thông tin (Selector Row) chuẩn Liquid Glass cho các trường cần bấm để mở picker (Danh mục, Ví, Ngày tháng...).

* **Đặc điểm thiết kế:**
  - Thẻ Surface bo góc 18dp, viền mảnh `BorderStroke(1.dp, tokens.border)`.
  - Icon badge 42dp bo góc 12dp bên trái với màu nền trong suốt alpha 14%.
  - Column 2 dòng:
    * Dòng 1: Label viết hoa nhỏ gọn (`10.5sp Bold`, chữ xám nhạt).
    * Dòng 2: Tên giá trị chính (`15sp SemiBold`, màu `tokens.onSurface`).
    * Dòng phụ: Phụ đề / Số dư khả dụng (`12sp Medium`).
  - Mũi tên điều hướng `>` (Chevron) bên phải.

* **Khởi tạo & Sử dụng:**
```kotlin
import com.finlux.app.core.designsystem.component.ErgonomicFormRow

ErgonomicFormRow(
    label = "VÍ THANH TOÁN",
    primaryValue = activeWallet?.name ?: "Chưa chọn ví",
    secondaryValue = activeWallet?.let { "Số dư: ${formatVndAmount(it.balance.value)}" },
    icon = walletIcon,
    iconBgColor = walletAccent.copy(alpha = 0.14f),
    iconTintColor = walletAccent,
    onClick = { showWalletPicker = true },
)
```

---

### 4️⃣ `ErgonomicInputRow` — Thẻ Nhập Liệu Phẳng Bo Góc
Hàng nhập text/số thay thế hoàn toàn cho `OutlinedTextField` của Material 3 (giải quyết triệt để lỗi label notch cutout đè lên viền kính).

* **Đặc điểm thiết kế:**
  - Thẻ Surface bo góc 18dp phẳng và sạch sẽ.
  - Icon badge 42dp bên trái.
  - Label nhỏ viết hoa ở trên + Ô nhập bằng `BasicTextField` mượt mà bên dưới.
  - Nút `[x]` xóa nhanh xuất hiện khi có nội dung.

* **Khởi tạo & Sử dụng:**
```kotlin
import com.finlux.app.core.designsystem.component.ErgonomicInputRow

ErgonomicInputRow(
    label = "GHI CHÚ GIAO DỊCH",
    value = noteText,
    onValueChange = { noteText = it },
    placeholder = "Nhập ghi chú chi tiêu...",
    icon = Icons.AutoMirrored.Filled.ReceiptLong,
    iconBgColor = Color(0xFF06B6D4).copy(alpha = 0.14f),
    iconTintColor = Color(0xFF0891B2),
    onClear = { noteText = "" },
)
```

---

### 5️⃣ `ErgonomicCompactAmountCard` — Thẻ Ô Nhập / Hiển Thị Tiền Gọn Gàng (Custom Color & Focus-driven Quick Chips)
Thẻ Surface bo góc 18dp độc lập, dùng để hiển thị hoặc nhập số tiền thu gọn với typography 16sp Bold, tự động preview định dạng phân tách hàng nghìn VNĐ trong thời gian thực, có chữ mờ (placeholder), dải chip gợi ý nhân số tiền thông minh (vd: gõ 3 -> [3k, 30k, 300k, 3000k]) **chỉ hiển thị mượt mà khi người dùng focus vào ô nhập**, viền sáng highlight tinh tế khi active và cho phép tùy biến màu sắc số tiền theo từng màn hình. Không có dòng chữ phụ bên dưới.

* **Đặc điểm thiết kế:**
  - Thẻ Surface bo góc 18dp, viền mảnh `BorderStroke(1.dp, tokens.border)` (tự động chuyển sang viền sáng `amountColor` khi ô nhập được focus).
  - **Label ở trên:** Chữ viết hoa nhỏ gọn (`10.5sp Bold`, chữ xám nhạt).
  - **Ô nhập / hiển thị tiền:** Typography 16sp Bold, tự format VNĐ khi gõ (vd: `15.000 ₫`), có placeholder xám mờ khi chưa nhập.
  - **Dải Chip Gợi Ý Tiền Tệ Thông Minh Decimal Magnitude Scaling (`AnimatedVisibility` khi `isFocused = true`):**
    * Khi chưa focus vào ô nhập: Card giữ kích thước siêu gọn gàng, không hiển thị dải chip chiếm diện tích.
    * Khi người dùng tap/focus vào ô nhập: Dải chip mượt mà mở rộng với hiệu ứng fade & expand.
    * **Thuật toán Decimal Magnitude Scaling**: Tự động sinh dải gợi ý $V = N \times 10^k$ từ 1.000đ đến 1.000.000.000đ:
      - Ô rỗng/số 0: Danh sách 8 mốc mặc định chuẩn `[50k, 100k, 200k, 500k, 1M, 2M, 5M, 10M]`.
      - Gõ `"3"` $\rightarrow$ `[3.000, 30.000, 300.000, 3.000.000, 30.000.000]`.
      - Gõ `"35"` $\rightarrow$ `[3.500, 35.000, 350.000, 3.500.000, 35.000.000]` (bao gồm mốc x100 = 3.500).
      - Gõ `"356"` $\rightarrow$ `[3.560, 35.600, 356.000, 3.560.000, 35.600.000]` (bao gồm mốc x10 = 3.560, x100 = 35.600).
      - Gõ `"3568"` $\rightarrow$ `[35.680, 356.800, 3.568.000, 35.680.000]`.
    * Khi bấm vào chip, số tiền trong ô nhập được cập nhật trực tiếp (`onValueChange`).
    * Cho phép tùy biến ẩn hoàn toàn gợi ý (`showSuggestions = false` khi nằm trong layout hẹp 2 cột).
  - **Tùy biến màu (`amountColor: Color`):** Hỗ trợ truyền bất kỳ màu nào (Xanh dương cho gốc, Tím cho lãi, Đỏ cho chi tiêu, Xanh lá cho thu nhập...).
  - **Chế độ Read-only hoặc Input:** Tự động chuyển sang chế độ chỉ đọc nếu không truyền `onAmountChange` hoặc set `isReadOnly = true`.
  - **Linh hoạt bố cục:** Dùng độc lập 1 card full width hoặc xếp 2 card cạnh nhau trong `Row` bằng `Modifier.weight(1f)`.

* **Khởi tạo & Sử dụng:**
```kotlin
import com.finlux.app.core.designsystem.component.ErgonomicCompactAmountCard

// 1. Chế độ Nhập liệu (Editable) có màu tùy biến & dải chip gợi ý (mặc định bật)
ErgonomicCompactAmountCard(
    label = "HẠN MỨC CHI TIÊU THÁNG",
    amountText = limitInput,
    onAmountChange = { limitInput = it },
    placeholder = "0",
    amountColor = tokens.primary,
    showSuggestions = true, // Mặc định là true
    modifier = Modifier.fillMaxWidth(),
)

// 2. Chế độ Chỉ đọc (Read-only) tự động tính toán
ErgonomicCompactAmountCard(
    label = "TRỪ TIỀN GỐC",
    amountText = principalAmount.toString(),
    isReadOnly = true,
    amountColor = tokens.primary,
    modifier = Modifier.weight(1f),
)
```

* **Các màn hình đang kế thừa:**
  - [`AddTransactionSheet.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/transaction/AddTransactionSheet.kt) (Ô nhập số tiền giao dịch chính, hỗ trợ màu động `ExpenseRed` / `IncomeGreen`).
  - [`PrismBudgetScreen.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/budget/prism/PrismBudgetScreen.kt), [`ClassicBudgetScreen.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/budget/classic/ClassicBudgetScreen.kt), [`ModernBudgetScreen.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/budget/modern/ModernBudgetScreen.kt) (Ô nhập hạn mức chi tiêu tháng).
  - [`PrismWalletsScreen.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/wallet/prism/PrismWalletsScreen.kt), [`ModernWalletsScreen.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/wallet/modern/ModernWalletsScreen.kt), [`ClassicWalletsScreen.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/wallet/classic/ClassicWalletsScreen.kt) (Ô nhập số dư ví ban đầu/hiện tại & Ô nhập số tiền chuyển liên ví).
  - [`GoalsScreen.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/goal/GoalsScreen.kt) (Nạp/Rút tiền mục tiêu, Mục tiêu cần đạt & Tích lũy tháng trong `GoalEditor`).
  - [`DebtPaymentSheet.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/debt/DebtPaymentSheet.kt) (Tổng số tiền trả & Ô trừ tiền gốc / Ô nhập tiền lãi phát sinh qua `PrincipalInterestSplitCard`).
  - [`AddEditDebtSheet.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/debt/AddEditDebtSheet.kt) (Hạn mức/Vay gốc, Dư nợ hiện tại, Trả tối thiểu hàng tháng).
  - [`SalaryCycleSettingsSheet.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/settings/salary/SalaryCycleSettingsSheet.kt) (Mức lương dự kiến mỗi kỳ).
  - [`RemindersScreen.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/reminders/RemindersScreen.kt) (Số tiền dự kiến trong nhắc nhở chi tiêu).
  - [`NotificationsScreen.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/notifications/NotificationsScreen.kt) (Số tiền thanh toán nhanh từ thông báo).

---

### 6️⃣ `PrincipalInterestSplitCard` — Card Đôi Phân Bổ Gốc & Lãi
Thẻ đôi chia 2 cột liền mạch được cấu thành từ 2 `ErgonomicCompactAmountCard` độc lập, dành riêng cho các nghiệp vụ thanh toán nợ / vay tài chính.

* **Đặc điểm thiết kế:**
  - **Cột 1 (Trừ tiền gốc):** Tự động tính toán và hiển thị số tiền gốc to rõ màu `principalColor` (mặc định `tokens.primary`).
  - **Cột 2 (Tiền lãi phát sinh):** Ô nhập số tiền lãi hỗ trợ tự động định dạng phân tách hàng nghìn VNĐ màu `interestColor` (mặc định `Color(0xFF6366F1)`).

* **Khởi tạo & Sử dụng:**
```kotlin
import com.finlux.app.core.designsystem.component.PrincipalInterestSplitCard

PrincipalInterestSplitCard(
    principalAmount = computedPrincipal,
    interestText = interestDigits,
    onInterestChange = { interestDigits = it },
    principalColor = tokens.primary,
    interestColor = Color(0xFF6366F1),
)
```

---

### 7️⃣ `FinluxSnackbarHost` & `FinluxGlassSnackbar` — Floating Liquid Glass Toast / Snackbar Chuẩn
Bộ đôi Component hiển thị thông báo Toast / Snackbar tiêu chuẩn lấy cảm hứng từ Native Toast HyperOS cao cấp, tích hợp hiệu ứng Liquid Glass, tự động né thanh điều hướng (Bottom Bar) và hỗ trợ nút hành động ("Hoàn tác" / Undo).

* **Đặc điểm thiết kế:**
  - Phom dáng **Floating Capsule** (viên nang nổi) bo góc `24dp` đặt ở chính giữa màn hình (`Alignment.BottomCenter`).
  - Nền kính mờ `LocalFinluxTokens.current.surface.copy(alpha = 0.94f..0.96f)` viền sáng tán sắc mảnh `1dp`.
  - Icon badge thương hiệu `ic_finlux` (hoặc icon trạng thái) bên trái.
  - Text thông điệp rõ ràng, độ tương phản tuyệt đối trên nền kính.
  - Tích hợp nút hành động / *"Hoàn tác"* (`actionLabel`) màu Accent phát sáng bên phải khi người dùng xóa hoặc sửa dữ liệu.
  - Tự động nhận diện insets và né thanh Bottom Navigation Bar (`hasBottomBar = true` nâng cao thêm `bottomBarClearance + 12dp` = ~108dp, chống bị che khuất).

* **Khởi tạo & Sử dụng:**
```kotlin
import com.finlux.app.core.designsystem.component.FinluxSnackbarHost

val snackbarHostState = remember { SnackbarHostState() }

// Trong Scaffold:
Scaffold(
    snackbarHost = { FinluxSnackbarHost(snackbarHostState, hasBottomBar = isRootTab) },
) { padding -> ... }

// Kích hoạt thông báo:
snackbarHostState.showSnackbar(
    message = "Đã lưu thông tin thành công",
    actionLabel = "Hoàn tác", // Hoặc null nếu chỉ là thông báo nhanh
)
```

---

## 📱 4. DANH SÁCH MÀN HÌNH ĐÃ KẾ THỪA BỘ COMPONENT CHUẨN

| Màn hình / Modal | Component được áp dụng |
| :--- | :--- |
| **Giao Dịch (`PrismTransactionsScreen`, `ModernTransactionsScreen`, `ClassicTransactionsScreen`)** | `FinluxSnackbarHost` (né BottomBar), `FinluxCategoryPickerBottomSheet`, `ErgonomicFormRow` |
| **Quản Lý Ví (`PrismWalletsScreen`, `ModernWalletsScreen`, `ClassicWalletsScreen`)** | `FinluxSnackbarHost` (né BottomBar), `ErgonomicCompactAmountCard`, `FinluxWalletPickerBottomSheet` |
| **Ngân Sách (`PrismBudgetScreen`, `ClassicBudgetScreen`, `ModernBudgetScreen`)** | `FinluxSnackbarHost` (né BottomBar), `FinluxCategoryPickerBottomSheet`, `ErgonomicCompactAmountCard` |
| **Quản Lý Nợ & Tín Dụng (`DebtDashboardScreen.kt`, `DebtPaymentSheet.kt`, `AddEditDebtSheet.kt`)** | `FinluxSnackbarHost`, `FinluxWalletPickerBottomSheet`, `PrincipalInterestSplitCard`, `ErgonomicCompactAmountCard`, `ErgonomicFormRow`, `ErgonomicInputRow` |
| **Thương Vụ Đầu Tư (`DealsScreen.kt`, `CreateDealDialog.kt`, `RecordDealInflowDialog.kt`)** | `FinluxSnackbarHost`, `FinluxStyleBackdrop`, `GlassTopBar`, `DealDetailBottomSheet` |
| **Trung Tâm Thông Báo (`NotificationsScreen.kt`)** | `FinluxSnackbarHost`, `FinluxCategoryPickerBottomSheet`, `FinluxWalletPickerBottomSheet`, `ErgonomicCompactAmountCard` |
| **Nhắc Nhở Định Kỳ (`RemindersScreen.kt`)** | `FinluxSnackbarHost`, `FinluxCategoryPickerBottomSheet`, `FinluxWalletPickerBottomSheet`, `ErgonomicCompactAmountCard`, `ErgonomicInputRow` |
| **Quản Lý Danh Mục (`CategoriesScreen.kt`)** | `FinluxSnackbarHost`, `GlassTopBar`, `FinluxLazyColumn` |
| **Hồ Sơ & Cài Đặt (`PrismSettingsScreen.kt`, `SettingsScreen.kt`)** | `FinluxSnackbarHost` (né BottomBar), `ProfileCard`, `BiometricSwitch` |
| **Thêm Giao Dịch (`AddTransactionSheet.kt`)** | `FinluxCategoryPickerBottomSheet`, `FinluxWalletPickerBottomSheet`, `ErgonomicCompactAmountCard`, `ErgonomicInputRow` |
| **Mục Tiêu Tài Chính (`GoalsScreen.kt`)** | `ErgonomicCompactAmountCard` (Nạp/Rút & Mục tiêu / Tích lũy tháng) |
| **Cài Đặt Lương (`SalaryCycleSettingsSheet.kt`)** | `ErgonomicCompactAmountCard` (Mức lương dự kiến) |

