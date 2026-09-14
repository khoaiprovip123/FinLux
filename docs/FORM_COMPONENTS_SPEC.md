# TÀI LIỆU QUY CHUẨN: BỘ COMPONENT FORM & PICKER TIÊU CHUẨN (FINLUX DESIGN SYSTEM)

> **Mục đích:** Tài liệu này đặc tả chi tiết toàn bộ các component biểu mẫu (Form Rows, Input Cards) và Modal chọn lựa (Wallet Picker, Category Picker) tiêu chuẩn trong ứng dụng **Finlux**. Mọi Developer và AI Coding Agent khi tham gia phát triển tính năng mới **BẮT BUỘC** phải kế thừa các component này, không tự ý tạo lại từ đầu (Tuân thủ Nguyên tắc cốt lõi #2: Tái sử dụng & tránh phân mảnh code).

---

## 📍 1. Vị Trí Lưu Trữ Mã Nguồn

Toàn bộ component tiêu chuẩn được đặt tập trung tại:
- **Unified Form Controls Contract:** [`app/src/main/java/com/finlux/app/core/designsystem/component/form/FinluxFormControls.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/core/designsystem/component/form/FinluxFormControls.kt)
- **Amount Card & Formatter Components:** [`app/src/main/java/com/finlux/app/core/designsystem/component/FinluxTransactionComponents.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/core/designsystem/component/FinluxTransactionComponents.kt)
- **Package:** `com.finlux.app.core.designsystem.component.form.*` & `com.finlux.app.core.designsystem.component.*`

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

### 3️⃣ `FinluxWalletSelector` & `FinluxTransferWalletPair` — Thẻ Chọn Ví Tích Hợp Thẩm Định Số Dư Khả Dụng (Liquid Glass Validation)
Thẻ chọn ví nguồn / ví nhận tiền chuẩn Liquid Glass tích hợp cơ chế bảo vệ số dư khả dụng thời gian thực (`WalletValidationResult`), tự động chuyển viền đỏ và bung banner cảnh báo Liquid Glass khi thiếu số dư hoặc vượt hạn mức thẻ tín dụng.

* **Đặc điểm thiết kế:**
  - Surface bo góc 18dp với nền `tokens.surfaceSoft`, viền mảnh `BorderStroke(1.dp, tokens.border)`.
  - **Tự động đổi trạng thái vi phạm (`hasViolation = true`):**
    * Khi `isError = true`, `validationResult !is WalletValidationResult.Valid` hoặc `warningMessage != null`:
      - Viền thẻ tự động chuyển sang màu đỏ cảnh báo `BorderStroke(1.dp, tokens.error.copy(alpha = 0.6f))`.
      - Phụ đề số dư chuyển sang màu đỏ `tokens.error`.
  - **Logo thương hiệu & Phân loại ví:** Hiển thị `FinancialInstitutionLogo` (42dp) nhận diện ngân hàng/ví điện tử (MB, VCB, MoMo...) hoặc badge icon tròn `Tokens.primary` (42dp bo góc 12dp).
  - **Column 3 tầng:**
    * Dòng 1: Label viết hoa nhỏ gọn (`10.5sp Bold`, chữ xám nhạt `tokens.onSurfaceVariant`).
    * Dòng 2: Tên ví (`15sp SemiBold`, màu `tokens.onSurface`).
    * Dòng 3: Số dư khả dụng (`12sp Medium`, định dạng chuẩn `formatVndAmount(it.balance.value)`).
  - Icon mũi tên điều hướng Chevron `>` bên phải.
  - **Banner Cảnh Báo Liquid Glass Tự Động Bung (`AnimatedVisibility`):**
    * Tự động mở rộng với hiệu ứng `expandVertically() + fadeIn()` bên dưới thẻ khi có lỗi số dư.
    * Nền kính mờ `tokens.error.copy(alpha = 0.08f)` với viền đỏ tán sắc `tokens.error.copy(alpha = 0.25f)` bo góc 12dp.
    * Icon cảnh báo `Icons.Default.Warning` (16dp, màu `tokens.error`) đi kèm thông điệp chi tiết:
      - `WalletValidationResult.InsufficientFunds`:
        * Hết số dư: *"Ví [Tên ví] đã hết số dư (Hiện có: 0 ₫)"*.
        * Không đủ: *"Số dư ví [Tên ví] không đủ (Hiện có: X ₫ - Cần: Y ₫)"*.
      - `WalletValidationResult.CreditLimitExceeded`:
        * *"Vượt hạn mức thẻ tín dụng (Hạn mức: X ₫ - Dự kiến: Y ₫)"*.

* **Khởi tạo & Sử dụng:**
```kotlin
import com.finlux.app.core.designsystem.component.form.FinluxWalletSelector
import com.finlux.app.domain.validation.WalletBalanceValidator

val validationResult = remember(selectedWallet, amount) {
    WalletBalanceValidator.validate(
        wallet = selectedWallet,
        amount = amount,
        isExpense = isExpense,
        creditLimit = selectedWallet?.creditLimit,
    )
}

FinluxWalletSelector(
    label = "VÍ THANH TOÁN",
    selectedWallet = selectedWallet,
    onClick = { showWalletPicker = true },
    validationResult = validationResult,
)
```

* **Các màn hình đang kế thừa:**
  - [`AddTransactionSheet.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/transaction/AddTransactionSheet.kt) (Chọn ví chi tiêu/thu nhập, khóa nút Lưu khi thiếu tiền).
  - [`RecordDealOutlaySheet.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/deal/RecordDealOutlaySheet.kt) (Chọn ví xuất vốn đầu tư / cho vay).
  - [`RecordDealInflowSheet.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/deal/RecordDealInflowSheet.kt) (Chọn ví nhận tiền thu hồi vốn / lợi nhuận).
  - [`GoalsScreen.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/goal/GoalsScreen.kt) (Chọn ví nguồn nạp tiền tích lũy mục tiêu).

---

### 4️⃣ `ErgonomicFormRow` — Hàng Chọn Dữ Liệu 2 Dòng Tiêu Chuẩn
Hàng chọn thông tin (Selector Row) chuẩn Liquid Glass cho các trường cần bấm để mở picker (Danh mục, Ngày tháng...).

* **Đặc điểm thiết kế:**
  - Thẻ Surface bo góc 18dp, viền mảnh `BorderStroke(1.dp, tokens.border)`.
  - Icon badge 42dp bo góc 12dp bên trái với màu nền trong suốt alpha 14%.
  - Column 2 dòng:
    * Dòng 1: Label viết hoa nhỏ gọn (`10.5sp Bold`, chữ xám nhạt).
    * Dòng 2: Tên giá trị chính (`15sp SemiBold`, màu `tokens.onSurface`).
    * Dòng phụ: Phụ đề / Ghi chú (`12sp Medium`).
  - Mũi tên điều hướng `>` (Chevron) bên phải.

* **Khởi tạo & Sử dụng:**
```kotlin
import com.finlux.app.core.designsystem.component.ErgonomicFormRow

ErgonomicFormRow(
    label = "DANH MỤC",
    primaryValue = activeCategory?.name ?: "Chưa chọn danh mục",
    icon = categoryIcon(activeCategory?.iconName),
    iconBgColor = categoryColor.copy(alpha = 0.14f),
    iconTintColor = categoryColor,
    onClick = { showCategoryPicker = true },
)
```

---

### 5️⃣ `ErgonomicInputRow` — Thẻ Nhập Liệu Phẳng Bo Góc
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

### 6️⃣ `FinluxAmountInput` & `ErgonomicCompactAmountCard` — Ô Nhập Liệu Tiền Tệ Thông Minh (Decimal Magnitude Scaling & Focus-driven Reveal)
Bộ đôi Component nhập số tiền chuẩn mực của Finlux Design System:
- **`FinluxAmountInput` (Hero Size):** Dành cho form chính (Thêm/Sửa giao dịch, Chuyển tiền) với typography to nổi bật (32sp), tự động co giãn kích thước font chống tràn số (Auto-scaling), định dạng phân tách hàng nghìn VNĐ trong thời gian thực, nút xóa nhanh `[x]`, hậu tố `₫` inline, và dải chip gợi ý nhân cấp tự bung theo focus.
- **`ErgonomicCompactAmountCard` (Compact Size):** Dành cho các form phụ có nhiều trường tiền tệ (Thanh toán nợ, Phân bổ gốc/lãi, Ngân sách, Mục tiêu tài chính) với typography 16sp Bold, hỗ trợ dải chip rút gọn hoặc ẩn khi nằm trong layout 2 cột.

* **Thuật toán Decimal Magnitude Scaling ($V = N \times 10^k$):**
  - Tự động sinh dải chip gợi ý nhân cấp tiền tệ từ 1.000đ đến 1.000.000.000đ (1 tỷ VNĐ) dựa trên các chữ số người dùng đang gõ:
    * **Ô rỗng hoặc giá trị $\le 0$:** Hiển thị 8 mốc mặc định: `[50k, 100k, 200k, 500k, 1M, 2M, 5M, 10M]`.
    * **Gõ `"3"`:** $\rightarrow$ `[3.000, 30.000, 300.000, 3.000.000, 30.000.000]`.
    * **Gõ `"35"`:** $\rightarrow$ `[3.500, 35.000, 350.000, 3.500.000, 35.000.000]`.
    * **Gõ `"356"`:** $\rightarrow$ `[3.560, 35.600, 356.000, 3.560.000, 35.600.000]`.
    * **Gõ `"3568"`:** $\rightarrow$ `[35.680, 356.800, 3.568.000, 35.680.000]`.
  - Giới hạn tối đa 5 chip gợi ý và kẹp trần an toàn `maxLimit = 1_000_000_000L` chống tràn số.
  - Khi bấm vào chip: Số tiền được cập nhật ngay lập tức vào state của ô nhập.

* **Cơ chế Tự Bung Chip Theo Focus (Focus-driven Quick Chips Reveal):**
  - **Khi chưa focus (`isFocused = false`):** Thẻ duy trì kích thước tối giản, ẩn hoàn toàn dải chip gợi ý để tiết kiệm không gian màn hình.
  - **Khi người dùng tap/focus (`isFocused = true`):** Dải chip mở rộng mượt mà với hiệu ứng:
    `enter = expandVertically(expandFrom = Alignment.Top) + fadeIn()`
    `exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()`.
  - Viền thẻ tự động phát sáng highlight theo màu ngữ nghĩa `amountColor.copy(alpha = 0.5f)`.

* **Thuật toán Co Giãn Cỡ Chữ Tự Động (Dynamic Auto-scaling Typography):**
  - Ngăn chặn hoàn toàn lỗi tràn layout khi nhập số tiền lớn (hàng trăm triệu đến chục tỷ):
    * Số chữ số $< 9$: Giữ nguyên kích thước `amountFontSize` (mặc định 32sp).
    * Số chữ số $\ge 9$ (từ 100.000.000đ): Co font xuống $82\%$ (`amountFontSize * 0.82f` $\approx 26.2\text{sp}$).
    * Số chữ số $\ge 11$ (từ 10.000.000.000đ): Co font xuống $70\%$ (`amountFontSize * 0.70f` $\approx 22.4\text{sp}$).

* **Các tiện ích công thái học chuẩn:**
  - **Inline `₫` suffix:** Sử dụng `VndSuffixVisualTransformation` hiển thị ký hiệu `₫` sau số tiền mà không làm bẩn dữ liệu raw digits.
  - **Nút xóa nhanh `[x]`:** Hình tròn bo góc `28dp` xuất hiện khi có số, bấm vào xóa trắng ô nhập ngay lập tức.
  - **Dynamic Semantic Tinting:** Tự động suy diễn màu dải chip từ `amountColor` (Container `amountColor.copy(alpha = 0.12f)`, Border `amountColor.copy(alpha = 0.30f)`, Content `amountColor`) đảm bảo sự hài hòa thị giác.

* **Khởi tạo & Sử dụng:**
```kotlin
import com.finlux.app.core.designsystem.component.form.FinluxAmountInput
import com.finlux.app.core.designsystem.component.ErgonomicCompactAmountCard

// 1. FinluxAmountInput (Hero size cho form chính)
FinluxAmountInput(
    label = "SỐ TIỀN CHI TIÊU",
    amountText = amountInput,
    onAmountChange = { amountInput = it },
    amountColor = LocalFinluxTokens.current.expenseRed,
    showQuickChipsOnFocusOnly = true,
)

// 2. ErgonomicCompactAmountCard (Compact size cho form phụ)
ErgonomicCompactAmountCard(
    label = "HẠN MỨC CHI TIÊU THÁNG",
    amountText = limitInput,
    onAmountChange = { limitInput = it },
    amountColor = tokens.primary,
    showSuggestions = true,
)
```

* **Các màn hình đang kế thừa:**
  - [`AddTransactionSheet.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/transaction/AddTransactionSheet.kt) (FinluxAmountInput chính với ExpenseRed / IncomeGreen).
  - [`PrismBudgetScreen.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/budget/prism/PrismBudgetScreen.kt), [`ClassicBudgetScreen.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/budget/classic/ClassicBudgetScreen.kt), [`ModernBudgetScreen.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/budget/modern/ModernBudgetScreen.kt) (Hạn mức chi tiêu tháng).
  - [`PrismWalletsScreen.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/wallet/prism/PrismWalletsScreen.kt), [`ModernWalletsScreen.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/wallet/modern/ModernWalletsScreen.kt), [`ClassicWalletsScreen.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/wallet/classic/ClassicWalletsScreen.kt) (Số dư ví & Số tiền chuyển liên ví).
  - [`GoalsScreen.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/goal/GoalsScreen.kt) (Nạp/Rút tiền mục tiêu, Mục tiêu cần đạt & Tích lũy tháng).
  - [`DebtPaymentSheet.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/debt/DebtPaymentSheet.kt) (Tổng số tiền trả & PrincipalInterestSplitCard).
  - [`AddEditDebtSheet.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/debt/AddEditDebtSheet.kt) (Hạn mức/Vay gốc, Dư nợ hiện tại, Trả tối thiểu).
  - [`SalaryCycleSettingsSheet.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/settings/salary/SalaryCycleSettingsSheet.kt) (Mức lương dự kiến mỗi kỳ).
  - [`RemindersScreen.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/reminders/RemindersScreen.kt) (Số tiền nhắc nhở chi tiêu).
  - [`NotificationsScreen.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/notifications/NotificationsScreen.kt) (Số tiền thanh toán nhanh).

---

### 7️⃣ `PrincipalInterestSplitCard` — Card Đôi Phân Bổ Gốc & Lãi
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

### 8️⃣ `FinluxSnackbarHost` & `FinluxGlassSnackbar` — Floating Liquid Glass Toast / Snackbar Chuẩn
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

