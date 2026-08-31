# HANDOVER LOG - FINLUX APP

## Trạng Thái Dự Án (Project Status)
- **Phiên bản hiện tại:** v1.19.0 (versionCode 161) [DONE]
- **Trạng thái Build:** ✅ 100% PASS (239/239 Unit Tests) — Build & Kiểm thử thành công.

### [Task-DEAL-LENDING-CATEGORY-AND-HISTORY] — Nâng cấp module Thương Vụ & Đầu Tư: Nút Sửa Deal, Nút Lịch Sử Toàn Bộ, Phân Tách 2 Category (Đầu Tư & Cho Vay) & Cơ Chế Mở Lại Deal/Khoản Vay Đã Hoàn Tất
- **Status**: `[DONE]`
- **Mục tiêu đã hoàn thành**:
  1. ✅ **Phân Tách 2 Category (`DealCategory`: `INVESTMENT` & `LENDING`)**:
     - Thêm `enum class DealCategory { INVESTMENT, LENDING }` vào `DealModels.kt` và serialize/deserialize Firebase an toàn.
     - `INVESTMENT`: Theo dõi vốn xuất, thu hồi, lợi nhuận ròng, tỷ suất ROI %, chốt lời/lỗ.
     - `LENDING`: Theo dõi nợ gốc đã cho vay, nợ gốc đã thu hồi, dư nợ gốc còn lại, tiền lãi nhận thêm, tiến độ thu hồi nợ (không ép hiển thị ROI âm -100%). Thao tác: "Cho Vay Thêm", "Thu Nợ / Lãi", "Xóa Nợ & Đóng", "Xóa Khoản Vay".
  2. ✅ **Nút Chỉnh Sửa Deal (`CreateDealSheet.kt` & `DealDetailBottomSheet.kt`)**:
     - Bổ sung icon Edit (cây bút) trên Header của `DealDetailBottomSheet`, mở `CreateDealSheet` ở chế độ Edit.
     - Cho phép chỉnh sửa linh hoạt: Tiêu đề, Mô tả, Mục tiêu kỳ vọng, Category (Đầu tư / Cho vay), và Trạng thái (`[ ⚡ Đang Chạy | ✅ Đã Hoàn Tất ]`).
  3. ✅ **Nhật Ký Dòng Tiền Toàn Bộ Deal (`DealAllTransactionsBottomSheet.kt`)**:
     - Bổ sung icon Lịch sử (`Icons.AutoMirrored.Filled.ReceiptLong`) ở góc trên bên phải Top Bar `DealsScreen`.
     - Hero Summary Card 3 cột: Tổng Xuất/Cho vay, Gốc đã thu hồi, Tiền lời/lãi.
     - 5 Filter Chips phân loại dòng tiền (Tất cả, Xuất vốn/Cho vay, Thu hồi gốc, Tiền lời/lãi, Chốt lỗ/Xóa nợ).
     - Danh sách giao dịch gom nhóm theo ngày (`Hôm nay`, `Hôm qua`, `dd/MM/yyyy`) kèm deal title, tag category, ví trích/nhận tiền và số tiền trực quan.
  4. ✅ **Tương Thích Mọi Sheet Giao Dịch Deal**:
     - `RecordDealInflowSheet.kt` & `RecordDealOutlaySheet.kt`: Cập nhật tiêu đề, nhãn, phân rã dòng tiền (gốc vs lãi) và nút bấm chuẩn ngữ cảnh Cho Vay vs Đầu Tư.
  5. ✅ **Cơ Chế Mở Lại Deal/Khoản Vay Đã Hoàn Tất & Auto-Reset Khi Xóa Giao Dịch**:
     - Bổ sung nút **"Mở Lại Khoản Vay" / "Mở Lại Deal"** trên `DealDetailBottomSheet` khi deal ở trạng thái `COMPLETED` (không có stop-loss).
     - Tự động chuyển deal từ `COMPLETED` về `ACTIVE` (và `endDate = null`) trong `FirebaseTransactionRepository` và `DemoFinluxRepository` khi người dùng xóa giao dịch `PRINCIPAL_RECOVERY` mà dư nợ còn lại phát sinh.
- **Danh sách file đã chỉnh sửa & tạo mới**:
  - `app/src/main/java/com/finlux/app/domain/model/DealModels.kt`
  - `app/src/main/java/com/finlux/app/domain/repository/DealRepository.kt`
  - `app/src/main/java/com/finlux/app/domain/usecase/DealUseCases.kt`
  - `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseDealRepository.kt`
  - `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseTransactionRepository.kt`
  - `app/src/main/java/com/finlux/app/data/demo/DemoFinluxRepository.kt`
  - `app/src/main/java/com/finlux/app/presentation/deal/CreateDealSheet.kt`
  - `app/src/main/java/com/finlux/app/presentation/deal/DealDetailBottomSheet.kt`
  - `app/src/main/java/com/finlux/app/presentation/deal/DealAllTransactionsBottomSheet.kt` [NEW]
  - `app/src/main/java/com/finlux/app/presentation/deal/DealsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/deal/RecordDealInflowSheet.kt`
  - `app/src/main/java/com/finlux/app/presentation/deal/RecordDealOutlaySheet.kt`
  - `app/src/main/java/com/finlux/app/presentation/deal/DealsUiState.kt`
  - `app/src/main/java/com/finlux/app/presentation/deal/DealsViewModel.kt`
  - `app/src/test/java/com/finlux/app/domain/usecase/DealUseCasesTest.kt`
- **Kết quả Kiểm thử**:
  - `gradlew testDebugUnitTest`: **100% PASS** (239/239 tests, bao gồm test case mới cho `ReopenDealUseCase` và `DealCategory.LENDING`).
  - Build APK và nạp thành công lên thiết bị Android qua ADB.

### [Task-WALLET-TAP-BOTTOMSHEET-TRANSACTIONS] — Bật Bottom Sheet xem giao dịch của ví khi nhấn vào thẻ ví & Đếm ngược 5s khi xóa ví
- **Status**: `[DONE]`
- **Mục tiêu đã hoàn thành**:
  1. ✅ **Bottom Sheet Xem Giao Dịch Của Ví (`WalletTransactionsBottomSheet.kt`)**:
     - Xây dựng component Liquid Glass `WalletTransactionsBottomSheet` chuẩn chỉ:
       - Header: Logo ngân hàng/ví (`FinancialInstitutionLogo`), Tên ví, Loại ví, Badge "Mặc định" nếu có, Nút đóng (X).
       - Thẻ Số Dư & Thống Kê Nhanh: Hiển thị số dư hiện tại của ví to rõ cùng 2 hộp thống kê Tổng Thu và Tổng Chi từ các giao dịch của riêng ví này.
       - Danh sách giao dịch ví: Tự động lọc tất cả giao dịch có `walletId == wallet.id || relatedWalletId == wallet.id`, gom nhóm theo ngày (`Hôm nay`, `Hôm qua`, `dd/MM/yyyy`) với icon danh mục, tên giao dịch/ghi chú, thời gian (`HH:mm`), và số tiền hiển thị nổi bật.
       - Empty State: Khi chưa có giao dịch, hiển thị `FinluxEmptyState` tương thích theme.
       - Thanh thao tác nhanh chân sheet: Hỗ trợ nút "Chuyển tiền" (với ví nguồn tự động chọn) và nút "Chỉnh sửa ví".
  2. ✅ **Cử chỉ Thao tác trên Thẻ Ví (Tap & Long Press Gesture)**:
     - Áp dụng đồng bộ cho cả 3 Themes (`PrismWalletsScreen.kt`, `ModernWalletsScreen.kt`, `ClassicWalletsScreen.kt`):
       - **Nhấn (Tap/Click)** vào thẻ ví: **Bật ngay Bottom Sheet `WalletTransactionsBottomSheet`** hiển thị giao dịch của ví đó ngay tại màn hình Ví (không bị chuyển trang).
       - **Nhấn giữ (Long Press)** vào thẻ ví: Mở Bottom Sheet Chỉnh sửa ví (`WalletEditorSheet` / `editingWallet = wallet`).
  3. ✅ **Cơ chế đếm ngược 5 giây bảo vệ dữ liệu khi xóa ví**:
     - `FinluxModalComponents.kt`: Bổ sung tham số `confirmEnabled: Boolean = true` cho `FinluxDialog`.
     - Cả khi xóa từ Menu 3 chấm, từ nút Xóa trong Sheet chỉnh sửa ví, hoặc khi vuốt xóa nhanh (Swipe-to-dismiss):
       - Tích hợp bộ đếm ngược 5 giây (`5s -> 4s -> ... -> 0s`).
       - Nút xóa bị vô hiệu hóa (`enabled = false`) và hiển thị `Xác nhận xóa (5s)` trong 5s đầu.
       - Khi hết 5 giây, nút chuyển sang nhãn `Xóa Vĩnh Viễn` (màu đỏ nổi bật) và cho phép người dùng xác nhận xóa.
  4. ✅ **Kiểm thử & Nạp APK**:
     - Chạy `./gradlew testDebugUnitTest`: **100% PASS (237/237 tests)**.
     - Nạp APK thành công lên thiết bị Android qua ADB script.
- **Danh sách file đã chỉnh sửa & tạo mới**:
  - `app/src/main/java/com/finlux/app/presentation/wallet/WalletTransactionsBottomSheet.kt` [NEW]
  - `app/src/main/java/com/finlux/app/presentation/wallet/WalletsViewModel.kt`
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxModalComponents.kt`
  - `app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt`
  - `app/src/main/java/com/finlux/app/presentation/wallet/WalletsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/wallet/prism/PrismWalletsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/wallet/modern/ModernWalletsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/wallet/classic/ClassicWalletsScreen.kt`

### [Task-WALLET-TRANSFER-DATETIME-PICKER] — Thêm mục chọn Ngày & Giờ cho chức năng Chuyển Tiền Giữa Các Ví
- **Status**: `[DONE]`
- **Mục tiêu đã hoàn thành**:
  1. ✅ **Domain & ViewModel Layer**:
     - `TransferMoneyUseCase.kt`: Bổ sung tham số `date: Instant = Instant.now()` vào hàm `invoke(...)` và chuyển tiếp trực tiếp vào `repository.transferBetweenWallets(sourceId, destinationId, amount, note, date)`.
     - `WalletsViewModel.kt`: Cập nhật hàm `transfer(sourceId, destinationId, amount, note, date, onSaved)` nhận `date` và truyền vào UseCase.
  2. ✅ **UI Layer (Tất cả Theme Ví: Prism, Modern, Classic)**:
     - `PrismWalletsScreen.kt`: Thêm state `selectedDate`, `showDatePicker`, hàng `ErgonomicFormRow` với nhãn `"THỜI GIAN CHUYỂN TIỀN"` kèm định dạng thông minh (`"Hôm nay, dd/MM/yyyy • HH:mm"` / `"Hôm qua, ..."`), tích hợp bộ đôi `DatePickerDialog` (Material 3) + `TimePickerDialog` (24h), truyền `date` vào `viewModel.transfer(...)`.
     - `ModernWalletsScreen.kt`: Thêm state `selectedDate`, `showDatePicker`, hàng `ErgonomicFormRow` chọn thời gian giao dịch và tích hợp Dialog chọn Ngày & Giờ cho Bottom Sheet `TransferEditor`.
     - `ClassicWalletsScreen.kt`: Thêm hàng `ErgonomicFormRow` và Dialog chọn Ngày & Giờ đồng bộ cho `TransferEditor`.
  3. ✅ **Kiểm thử & Nạp APK**:
     - Chạy `./gradlew testDebugUnitTest`: **100% PASS (237/237 tests)**.
     - Nạp APK thành công lên thiết bị Android qua ADB script.
- **Danh sách file đã chỉnh sửa**:
  - `app/src/main/java/com/finlux/app/domain/usecase/TransferMoneyUseCase.kt`
  - `app/src/main/java/com/finlux/app/presentation/wallet/WalletsViewModel.kt`
  - `app/src/main/java/com/finlux/app/presentation/wallet/prism/PrismWalletsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/wallet/modern/ModernWalletsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/wallet/classic/ClassicWalletsScreen.kt`


### [Task-DEAL-REVERT-STOP-LOSS-AND-DELETE-FIX] — Nút Thu Hồi Chốt Lỗ & Cho Phép Xóa Giao Dịch Chốt Lỗ (Settlement)
- **Status**: `[DONE]`
- **Mục tiêu đã hoàn thành**:
  1. ✅ **Sửa lỗi xóa giao dịch Lỗ chốt deal**: Trong `FirebaseTransactionRepository.kt` và `DemoFinluxRepository.kt`, thêm kiểm tra `isSettlement` cho các giao dịch `walletId == "DEAL_SETTLEMENT"` / `dealFlowType == CAPITAL_LOSS` để không yêu cầu ví tiền mặt thực tế khi xóa. Tự động hoàn tác `netProfitLoss` của Deal, mở lại Deal về `ACTIVE` và xóa `endDate`.
  2. ✅ **Nút & Dialog Thu Hồi Chốt Lỗ**: Thêm nút `"Thu Hồi Chốt Lỗ"` (màu xanh dương với icon `Icons.Rounded.RestartAlt`) trong `DealDetailBottomSheet.kt` khi Deal có giao dịch chốt lỗ, kèm Dialog xác nhận hoàn tác chốt lỗ và mở lại Deal.
  3. ✅ **Domain Layer**: Thêm `revertDealLoss` trong `DealRepository`, triển khai trong `FirebaseDealRepository` & `DemoFinluxRepository`, tạo `RevertDealLossUseCase` và kết nối với `DealsViewModel.revertDealLoss`.
  4. ✅ **Unit Test & Build**: Thêm test case `revert deal loss restores netProfitLoss, removes capital loss tx and reopens deal` trong `DealUseCasesTest.kt` (237/237 tests PASS), build APK và nạp lên thiết bị.

### [Task-DEAL-DELETE-COUNTDOWN-SAFETY] — Tích hợp cơ chế đếm ngược 5s bảo vệ dữ liệu khi xóa Thương Vụ
- **Status**: `[DONE]`
- **Mục tiêu đã hoàn thành**:
  1. ✅ **DealDetailBottomSheet.kt**: Tích hợp `LaunchedEffect` đếm ngược 5 giây (`5s -> 4s -> ... -> 0s`) khi mở Dialog xác nhận xóa Deal.
  2. ✅ **UI An toàn dữ liệu**: Nút xóa hiển thị nhãn `Xóa (5s)`, bị disable trong 5s đầu và chỉ mở khóa `Xóa Vĩnh Viễn` khi đếm ngược về 0. Hiển thị thông điệp cảnh báo màu đỏ trực quan.
  3. ✅ **DealsScreen.kt**: Tự động đóng Bottom Sheet chi tiết sau khi người dùng thực hiện xóa thương vụ thành công.
  4. ✅ **Kiểm thử & Nạp APK**: Chạy `testDebugUnitTest` 100% PASS, build và nạp APK trực tiếp lên thiết bị.

### [Task-DEAL-SHEETS-DATETIME-PICKER] — Thêm mục chọn Ngày & Giờ cho 2 Sheet Thương vụ (Xuất vốn & Thu hồi vốn/lãi)
- **Status**: `[DONE]`
- **Mục tiêu đã hoàn thành**:
  1. ✅ **RecordDealOutlaySheet.kt (Chi Xuất Thêm Vốn)**: Thêm hàng `ErgonomicFormRow` chọn thời gian giao dịch (`THỜI GIAN XUẤT VỐN`) với nhãn thông minh (`Hôm nay, dd/MM/yyyy • HH:mm`), tích hợp `DatePickerDialog` + `TimePickerDialog` (24h), truyền `date` vào `onConfirm`.
  2. ✅ **RecordDealInflowSheet.kt (Thu Hồi Vốn & Lợi Nhuận)**: Thêm hàng `ErgonomicFormRow` chọn thời gian giao dịch (`THỜI GIAN THU TIỀN`), tích hợp `DatePickerDialog` + `TimePickerDialog` (24h), truyền `date` vào `onConfirm`.
  3. ✅ **DealsScreen.kt**: Kết nối tham số `date` từ sheet vào `viewModel.recordInflow` và `viewModel.recordOutlay`.
  4. ✅ **Kiểm thử & Nạp APK**: Chạy `testDebugUnitTest` 100% PASS, build và nạp APK trực tiếp lên thiết bị.

### [Task-DEAL-TRANSACTION-VALIDATION-FIX] — Cho phép Sửa/Thêm giao dịch Deal không cần Danh mục (Category)
- **Status**: `[DONE]`
- **Mục tiêu đã hoàn thành**:
  1. ✅ **Domain Validation**: Cập nhật `TransactionValidation.kt` kiểm tra `val isDeal = !transaction.dealId.isNullOrBlank() || transaction.dealFlowType != null`, bỏ qua bắt buộc `categoryId` đối với các giao dịch thuộc Thương vụ đầu tư.
  2. ✅ **Unit Test**: Thêm test case `edit allows deal transaction without category` vào `TransactionUseCasesTest.kt` (100% PASS).
  3. ✅ **Build & Install**: Build APK thành công và nạp trực tiếp lên thiết bị.

### [Task-TOAST-SNACKBAR-STANDARDIZATION] — Chuẩn hóa hệ sinh thái Toast & Snackbar Liquid Glass Capsule
- **Status**: `[DONE]`
- **Mục tiêu đã hoàn thành**:
  1. ✅ **Core Design System**:
     - Xây dựng `FinluxGlassSnackbar` dạng Viên nang Nổi (Floating Glass Capsule) bo góc `24dp`, nền kính mờ thích ứng theme `tokens.surface (alpha 94-96%)`, viền mảnh `BorderStroke(1.dp, tokens.border)`, shadow mềm `8dp`.
     - Tích hợp Badge Logo Finlux (`ic_finlux`), văn bản sắc nét, và nút Action / *"Hoàn tác"* (`actionLabel`) màu `tokens.primary`.
     - Xây dựng `FinluxSnackbarHost` tự động căn giữa ngang và cộng khoảng cách `tokens.spacing.bottomBarClearance + 12dp` khi `hasBottomBar = true` (né BottomBar ~108dp, loại bỏ 100% hiện tượng bị đè lấp).
  2. ✅ **Đồng bộ toàn bộ màn hình chính (có BottomBar)**:
     - `PrismTransactionsScreen`, `ModernTransactionsScreen`, `ClassicTransactionsScreen`.
     - `PrismWalletsScreen`, `ModernWalletsScreen`, `ClassicWalletsScreen`.
     - `PrismBudgetScreen`, `ModernBudgetScreen`, `ClassicBudgetScreen`.
  3. ✅ **Đồng bộ toàn bộ màn hình con & Form Sheets**:
     - `DebtDashboardScreen` (Quản lý nợ & Tín dụng — chuyển đổi từ Native Toast sang `FinluxSnackbarHost`).
     - `DealsScreen` (Thương vụ đầu tư).
     - `NotificationsScreen`, `RemindersScreen`, `CategoriesScreen`.
     - `PrismSettingsScreen`, `SettingsScreen` (Chuyển đổi cảnh báo sinh trắc học sang `FinluxSnackbarHost`).
  4. ✅ **Đồng bộ Tài liệu Quy chuẩn**:
     - Cập nhật mục `7️⃣ FinluxSnackbarHost & FinluxGlassSnackbar` vào `docs/FORM_COMPONENTS_SPEC.md`.
  5. ✅ **Kiểm thử Unit Test**:
     - Chạy `./gradlew testDebugUnitTest`: **100% PASS (235/235 tests)**.
- **Danh sách file đã chỉnh sửa**:
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxFeedbackComponents.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/prism/PrismTransactionsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/modern/ModernTransactionsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/classic/ClassicTransactionsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/wallet/prism/PrismWalletsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/wallet/modern/ModernWalletsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/wallet/classic/ClassicWalletsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/budget/prism/PrismBudgetScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/budget/modern/ModernBudgetScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/budget/classic/ClassicBudgetScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/debt/DebtDashboardScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/deal/DealsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/notifications/NotificationsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/reminders/RemindersScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/category/CategoriesScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/settings/prism/PrismSettingsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/settings/SettingsScreen.kt`
  - `docs/FORM_COMPONENTS_SPEC.md`
  - `HANDOVER_LOG.md`

### [Task-DEAL-TRACKING-ROI-MATCHING] — Triển khai tính năng Thương Vụ & Đầu Tư Sinh Lời (UC-29)
- **Status**: `[DONE]`
- **Mục tiêu đã hoàn thành**:
  1. ✅ **Data & Domain Layer**:
     - Tạo `DealModels.kt` chứa `FinancialDeal`, `DealStatus`, `DealFlowType`.
     - Mở rộng `FinanceTransaction` bổ sung `dealId` và `dealFlowType`.
     - Tạo `DealRepository` interface & `FirebaseDealRepository` (kèm Firestore collection `users/{uid}/deals`).
     - Tạo `DealUseCases.kt` với 6 UseCase: `GetDealsUseCase`, `GetDealDetailUseCase`, `SaveDealUseCase`, `DeleteDealUseCase`, `RecordDealOutlayUseCase`, `RecordDealInflowUseCase`, `CloseDealWithLossUseCase`.
     - Tích hợp `DemoFinluxRepository` & DI `RepositoryModule`.
  2. ✅ **Cơ Chế Phân Rã Dòng Tiền Nguyên Tử (Atomic Flow Decomposition)**:
     - Tự động tách phần hoàn gốc và tiền lời ròng trong 1 Firestore Transaction duy nhất.
     - Hỗ trợ chốt lỗ đóng deal (`closeDealWithLoss`) sinh giao dịch `CAPITAL_LOSS`.
  3. ✅ **Cô Lập Ngân Sách & Báo Cáo (Isolation Engine)**:
     - `ReportsViewModel`: Loại trừ `OUTLAY_CAPITAL` khỏi Chi phí và `PRINCIPAL_RECOVERY` khỏi Thu nhập; đưa `CAPITAL_GAIN` vào Thu nhập và `CAPITAL_LOSS` vào Chi phí.
     - `BudgetViewModel`: Vốn xuất không làm cạn kiệt ngân sách chi tiêu hàng tháng.
     - `HomeViewModel`: Tổng quan tài chính hiển thị chính xác dòng tiền sinh hoạt.
  4. ✅ **Giao Diện Liquid Glass Hiện Đại (`presentation/deal/`)**:
     - `DealsScreen`: Hero Summary Card (Vốn lưu động, Lợi nhuận tích lũy, ROI %), Tab switcher (Active vs Completed), Card thương vụ kèm thanh tiến độ hoàn vốn (Progress bar).
     - `DealDetailBottomSheet`: Dòng thời gian giao dịch và 4 nút thao tác nghiệp vụ.
     - `CreateDealDialog`, `RecordDealInflowDialog` (Live Preview phân rã dòng tiền), `RecordDealOutlayDialog`.
     - Đăng ký `Route.Deals` trong `Routes.kt` & `FinluxNavHost.kt`, gắn entry point trong `SettingsScreen` & `PrismSettingsScreen`.
  5. ✅ **Kiểm Thử & Đóng Gói Release**:
     - Viết bộ test `DealUseCasesTest.kt` kiểm tra toàn bộ kịch bản nghiệp vụ.
     - Chạy `./gradlew testDebugUnitTest`: **235/235 tests PASS 100%**.
     - Chạy `./gradlew assembleDebug`: **BUILD SUCCESSFUL**.
     - Tự động bump version lên `v1.17.0` (versionCode `159`), cập nhật `CHANGELOG.md` & `HANDOVER_LOG.md`.
- **Danh sách file đã tạo mới/chỉnh sửa**:
  - `app/src/main/java/com/finlux/app/domain/model/DealModels.kt` [NEW]
  - `app/src/main/java/com/finlux/app/domain/model/FinanceModels.kt`
  - `app/src/main/java/com/finlux/app/domain/repository/DealRepository.kt` [NEW]
  - `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseDealRepository.kt` [NEW]
  - `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseTransactionRepository.kt`
  - `app/src/main/java/com/finlux/app/data/demo/DemoFinluxRepository.kt`
  - `app/src/main/java/com/finlux/app/data/di/RepositoryModule.kt`
  - `app/src/main/java/com/finlux/app/domain/usecase/DealUseCases.kt` [NEW]
  - `app/src/main/java/com/finlux/app/presentation/reports/ReportsViewModel.kt`
  - `app/src/main/java/com/finlux/app/presentation/budget/BudgetViewModel.kt`
  - `app/src/main/java/com/finlux/app/presentation/home/HomeViewModel.kt`
  - `app/src/main/java/com/finlux/app/presentation/deal/DealsUiState.kt` [NEW]
  - `app/src/main/java/com/finlux/app/presentation/deal/DealsViewModel.kt` [NEW]
  - `app/src/main/java/com/finlux/app/presentation/deal/CreateDealDialog.kt` [NEW]
  - `app/src/main/java/com/finlux/app/presentation/deal/RecordDealInflowDialog.kt` [NEW]
  - `app/src/main/java/com/finlux/app/presentation/deal/RecordDealOutlayDialog.kt` [NEW]
  - `app/src/main/java/com/finlux/app/presentation/deal/DealDetailBottomSheet.kt` [NEW]
  - `app/src/main/java/com/finlux/app/presentation/deal/DealsScreen.kt` [NEW]
  - `app/src/main/java/com/finlux/app/core/navigation/Routes.kt`
  - `app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt`
  - `app/src/main/java/com/finlux/app/presentation/settings/SettingsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/settings/prism/PrismSettingsScreen.kt`
  - `app/src/test/java/com/finlux/app/domain/usecase/DealUseCasesTest.kt` [NEW]
  - `app/src/test/java/com/finlux/app/presentation/settings/prism/PrismSettingsMenuTest.kt`
  - `app/build.gradle.kts`
  - `CHANGELOG.md`
  - `HANDOVER_LOG.md`
  - `docs/BA_SPEC.md`
  - `docs/DATA_SPEC.md`

### [Task-HOME-RECENT-TRANSACTIONS-SYNC] — Đồng bộ giao diện Giao dịch gần nhất trên Trang chủ với màn hình Giao dịch
- **Status**: `[DONE]`
- **Mục tiêu đã hoàn thành theo yêu cầu người dùng**:
  1. ✅ **Đồng bộ hóa 100% thẻ giao dịch trên Trang chủ (`PrismHomeScreen.kt`)**:
     - Thiết kế theo chuẩn 3 cột: Container Icon 50dp (icon 24dp trắng trên nền gradient nhận diện danh mục), Cột giữa (Tên giao dịch 15.5sp SemiBold + Tên danh mục 13.5sp), Cột phải (Số tiền lớn 17sp ExtraBold + Giờ 12.5sp canh phải).
     - Card bo góc 20dp, shadow 2dp, nền sáng tinh tế, spacing 8dp.
  2. ✅ **Giới hạn hiển thị tối đa 5 giao dịch của ngày hôm nay**:
     - Lọc danh sách giao dịch phát sinh trong ngày hôm nay.
     - Hiển thị tối đa 5 giao dịch đầu tiên trên Trang chủ.
     - Nếu có nhiều hơn 5 giao dịch, hiển thị nút CTA `[ Xem thêm X giao dịch trong Lịch sử ]` điều hướng trực tiếp sang tab Giao dịch (`Route.Transactions.value`).
- **Kết quả kiểm thử**: `gradlew testDebugUnitTest` 100% PASS, `gradlew assembleDebug` BUILD SUCCESSFUL.
- **Files đã sửa đổi**:
  - `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`
  - `HANDOVER_LOG.md`

### [Task-TRANSACTION-HISTORY-REDESIGN-V2] — Nâng cấp toàn diện Lịch sử thu chi (History 2.0)
- **Status**: `[DONE]`
- **Mục tiêu đã hoàn thành**:
  1. ✅ Nâng cấp `TransactionsViewModel` hỗ trợ: Chế độ Lịch nhiệt lượng chi tiêu (`TransactionViewMode.LIST` vs `CALENDAR`), chọn ngày `selectedCalendarDate`, tóm tắt thông minh `SmartInsightUiModel`, gom nhóm dữ liệu theo ngày cho Calendar Heatmap.
  2. ✅ Nâng cấp `PrismTransactionsScreen`:
     - Tích hợp **Smart Micro-Insights Greeting Banner** (Lời nhắn tài chính ngữ cảnh thấu hiểu).
     - Tích hợp **Inline Instant Search Bar** (Tìm kiếm trực diện không dấu) & **Horizontal Quick Filter Chips** (Lọc nhanh 1-chạm: Kỳ này, Tháng này, Danh mục, Ví...).
     - Tích hợp **View Mode Switcher** (Danh sách ⟷ Lịch chi tiêu).
     - Tích hợp **Spending Calendar Heatmap View** (`PrismSpendingCalendarView` - Lưới lịch tháng kèm chấm nhiệt lượng dòng tiền Xanh/Vàng/Đỏ).
  3. ✅ Nâng cấp `TransactionDetailSheet` thành **Digital Glass Receipt (Phiếu biên lai kỹ thuật số)**:
     - Header thương hiệu FinLux, mã giao dịch `#FLX-xxxx`, thẻ loại hình giao dịch.
     - Section hiển thị tình trạng chứng từ đính kèm.
     - Nút chia sẻ biên lai nhanh dạng tin nhắn (`[📤 Chia sẻ biên lai]`).
  4. ✅ Đồng bộ hóa trên `ModernTransactionsScreen` và `ClassicTransactionsScreen`.
  5. ✅ Viết unit test mới cho ViewModel đảm bảo 100% tests PASS (`testDebugUnitTest`).
- **Kết quả kiểm thử**: `gradlew testDebugUnitTest` **BUILD SUCCESSFUL** 100% PASS.
- **Files đã sửa đổi**:
  - `app/src/main/java/com/finlux/app/presentation/transaction/TransactionsViewModel.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/prism/PrismSpendingCalendarView.kt` [NEW]
  - `app/src/main/java/com/finlux/app/presentation/transaction/prism/PrismTransactionsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/modern/ModernTransactionsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/classic/ClassicTransactionsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/TransactionDetailSheet.kt`
  - `app/src/test/java/com/finlux/app/presentation/transaction/TransactionsViewModelTest.kt`
  - `app/build.gradle.kts`
  - `CHANGELOG.md`
  - `HANDOVER_LOG.md`


### [Task-BUDGET-COPY-AND-LANDSCAPE-FIX] — Sao chép ngân sách & Khắc phục cuộn form màn hình ngang
- **Status**: `[DONE]`
- **Mục tiêu đã hoàn thành**:
  1. ✅ Bổ sung `resolveNextPeriod`, `resolveNextPeriodOf`, `resolvePreviousPeriodOf` vào `FinancialPeriodResolver`.
  2. ✅ Tạo `CopyBudgetUseCase` để sao chép toàn bộ ngân sách giữa các kỳ chi tiêu an toàn.
  3. ✅ Cập nhật `BudgetViewModel` hỗ trợ `copyBudgetsToNextPeriod`, `copyBudgetsFromPreviousPeriod`.
  4. ✅ Cập nhật `PrismBudgetScreen`, `ModernBudgetScreen`, `ClassicBudgetScreen` với nút Copy ở TopBar, nút "Sao chép kỳ trước" ở EmptyState và dialog xác nhận.
  5. ✅ Bổ sung `Modifier.verticalScroll(rememberScrollState())` vào form Thêm/Sửa ngân sách trên cả 3 giao diện, giúp cuộn mượt mà khi xoay ngang thiết bị.
  6. ✅ Lược bỏ toggle trong màn hình Hồ sơ/Cài đặt theo yêu cầu để giữ giao diện tinh gọn.
  7. ✅ Viết unit test `CopyBudgetUseCaseTest.kt` và cập nhật `BudgetViewModelTest.kt`.
- **Kết quả kiểm thử**: `gradlew testDebugUnitTest` **BUILD SUCCESSFUL** 100% PASS.
- **Files đã sửa đổi**:
  - `app/src/main/java/com/finlux/app/domain/usecase/FinancialPeriodResolver.kt`
  - `app/src/main/java/com/finlux/app/domain/usecase/CopyBudgetUseCase.kt` [NEW]
  - `app/src/main/java/com/finlux/app/presentation/budget/BudgetViewModel.kt`
  - `app/src/main/java/com/finlux/app/presentation/budget/prism/PrismBudgetScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/budget/modern/ModernBudgetScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/budget/classic/ClassicBudgetScreen.kt`
  - `app/src/main/java/com/finlux/app/domain/model/FinanceModels.kt`
  - `app/src/test/java/com/finlux/app/domain/usecase/CopyBudgetUseCaseTest.kt` [NEW]
  - `app/src/test/java/com/finlux/app/presentation/budget/BudgetViewModelTest.kt`

### [Task-ADAPTIVE-SYSTEM-NAVIGATION-INSETS] — Tự động co giãn an toàn tránh phím ảo điều hướng toàn hệ thống
- **Status**: `[DONE]`
- **Mục tiêu**:
  1. Thêm `WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)` và `consumeWindowInsets` vào `FinluxNavHost.kt`, `FinluxScreenScaffold.kt` và các màn hình chính.
  2. Tự động nhận diện thanh phím ảo điều hướng (3-button navigation bar / gesture navigation) ở cạnh bên (Landscape / Tablets / Foldables) để co lại khoảng cách an toàn, tránh 100% việc che khuất nội dung, badge % và các nút bấm mép phải.
- **Files đã sửa đổi**:
  - `app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt`
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxScreenScaffold.kt`
  - `app/build.gradle.kts`
  - `CHANGELOG.md`
  - `HANDOVER_LOG.md`
- **Kết quả kiểm thử & xác thực**:
  - `gradlew testDebugUnitTest`: **100% PASS** (220/220 tests).
  - `gradlew assembleDebug`: Build thành công, xác nhận hiển thị hoàn hảo trên thiết bị.

### [Task-PRISM-REAL-BUDGET-ALLOCATION-CAROUSEL] — Kết nối dữ liệu Ngân sách thật vào Donut Carousel Trang Chủ
- **Status**: `[DONE]`
- **Mục tiêu**:
  1. Thêm `budgets`, `totalBudgetLimit`, `totalBudgetSpent`, `totalBudgetPercent` vào `HomeUiState` và `HomeViewModel`.
  2. Nâng cấp Trang số 4 trong Carousel phân tích trên Trang chủ FinLux Prism (`Tiến độ định mức ngân sách`) lấy dữ liệu động từ các ngân sách thực tế trong kỳ.
  3. Donut chart thể hiện tổng % ngân sách đã chi tiêu kèm cảnh báo màu thông minh (Xanh lá < 80%, Vàng cam 80-99%, Đỏ ≥ 100%).
  4. Danh sách chi tiết thể hiện: Tên danh mục, Số tiền đã chi tiêu / Hạn mức ngân sách (`formatVnd(spent) / formatVnd(limit)`), và % tiến độ.
  5. Thiết kế Empty State trực quan khi chưa tạo ngân sách kèm nút điều hướng nhanh `[+ Thiết lập ngay ›]`.
  6. Nút "Xem chi tiết ›" tại trang này tự động điều hướng sang màn hình Quản lý Ngân sách (`Route.Budget`).
  7. Tự động che số tiền thành `••••` khi bật chế độ ẩn số dư.
- **Files đã sửa đổi**:
  - `app/src/main/java/com/finlux/app/presentation/home/HomeViewModel.kt`
  - `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`
  - `app/src/test/java/com/finlux/app/presentation/home/HomeViewModelTest.kt`
  - `app/build.gradle.kts`
  - `CHANGELOG.md`
  - `HANDOVER_LOG.md`
- **Kết quả kiểm thử & xác thực**:
  - `gradlew testDebugUnitTest`: **100% PASS** (220/220 tests).
  - `gradlew assembleDebug`: Build thành công.

### [Task-PERSISTENT-BALANCE-VISIBILITY-AND-DEBT-MASKING] — Lưu trữ vĩnh viễn trạng thái ẩn số dư & che số tiền nợ
- **Status**: `[DONE]`
- **Mục tiêu**:
  1. Thêm `isBalanceVisible` vào `UiPreferences` và `DataStoreThemePreferenceRepository` để lưu trữ trạng thái ẩn/hiện số dư vĩnh viễn vào DataStore.
  2. Tích hợp `showBalance` vào `HomeUiState` và `HomeViewModel.toggleBalanceVisibility()`, thay thế toàn bộ state tạm `remember { mutableStateOf(true) }` ở cả 3 giao diện Home (`PrismHomeScreen`, `ModernHomeScreen`, `ClassicHomeScreen`).
  3. Sửa lỗi lộ số tiền nợ `Nợ: 34.154.000 đ`: Tự động che thành `Nợ: ••••` khi `showBalance == false` trên thẻ Hero Prism Overview Card.
  4. Che toàn bộ các số liệu tiền tệ phát sinh trong dòng contextInfo và trung bình mỗi khoản khi ẩn số dư.
- **Files đã sửa đổi**:
  - `app/src/main/java/com/finlux/app/domain/model/FinanceModels.kt`
  - `app/src/main/java/com/finlux/app/data/local/datastore/DataStoreThemePreferenceRepository.kt`
  - `app/src/main/java/com/finlux/app/presentation/home/HomeViewModel.kt`
  - `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/home/modern/ModernHomeScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/home/classic/ClassicHomeScreen.kt`
  - `app/src/test/java/com/finlux/app/presentation/home/HomeViewModelTest.kt`
  - `app/build.gradle.kts`
  - `CHANGELOG.md`
  - `HANDOVER_LOG.md`
- **Kết quả kiểm thử & xác thực**:
  - `gradlew testDebugUnitTest`: **100% PASS** (219/219 tests).
  - `gradlew assembleDebug`: Build thành công.

### [Task-PRISM-HERO-TEXTURE-AND-BIGGER-AMOUNT] — Nâng cỡ chữ số tiền siêu lớn in đậm và thiết kế họa tiết chìm chuyên biệt cho từng thẻ
- **Status**: `[DONE]`
- **Mục tiêu**:
  1. Nâng kích thước font số tiền lên cỡ siêu lớn `38sp` (ExtraBold/Bold), tăng độ tương phản và nổi bật thị giác tức thì.
  2. Thiết kế họa tiết nền (ambient textures & watermark patterns) chìm tinh xảo riêng biệt cho 4 thẻ:
     - Thẻ Ví: Họa tiết vòm bảo mật & lưới khối đa tầng (Vault Rings & Security Mesh).
     - Thẻ Thu: Họa tiết dải sáng tăng trưởng cực quang & chevron dâng sóng (Aurora Ascent Waves).
     - Thẻ Chi: Họa tiết cung đo đa tầng ngân sách & quỹ đạo chi tiêu (Radar Gauge & Precision Curves).
     - Thẻ Dòng tiền: Họa tiết sóng tuần hoàn nhịp điệu kép (Harmonic Flow Vectors & Dot Matrix).
  3. Đảm bảo toàn bộ họa tiết vẽ ở lớp nền (`alpha` tinh tế 0.08f–0.18f), không che khuất dữ liệu số, Mini Bar Chart hay các nút bấm.
- **Files đã sửa đổi**:
  - `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`
  - `app/src/test/java/com/finlux/app/presentation/home/prism/PrismHomeLayoutTest.kt`
- **Kết quả kiểm thử & xác thực**:
  - `gradlew testDebugUnitTest`: **100% PASS** (218/218 tests).
  - `gradlew assembleDebug`: Build thành công.
  - Cài đặt lên máy thật, xác minh visual trực tiếp trên cả 4 thẻ đạt chuẩn ngân hàng số sang trọng 10/10.

### [Task-UI-UX-BULK-FIX-14-ISSUES] — Sửa Đồng Loạt Toàn Bộ 14 Lỗi UI/UX Đã Được Phê Duyệt
- **Status**: `[DONE]`
- **Mục tiêu**:
  - Giai đoạn 1: Sửa tận gốc Design System (`ModernLiquidGlass.kt`), Dark Mode màn hình Lịch sử (`ModernTransactionsScreen.kt`), Sheet Chi tiết GD (`TransactionDetailSheet.kt`), viền màu thẻ ví (`ModernWalletsScreen.kt`).
  - Giai đoạn 2: Tăng padding đáy màn hình Báo cáo (`ModernReportsScreen.kt` & `ClassicReportsScreen.kt`), sửa va chạm text thẻ GD, Scrim cho `MainBottomBar.kt`, nhãn 2 dòng cho Category Picker (`FinluxFormComponents.kt`), wrap chip mẫu ví, tối ưu ô Treemap nhỏ.
  - Giai đoạn 3: Tách dải ngày `SalaryCycleSheet.kt`, đổi icon cảnh báo `ModernBudgetScreen.kt`, làm phẳng capsule kỳ ngân sách, auto-scale font số dư khủng `ModernHomeScreen.kt` & `ClassicHomeScreen.kt`.
- **Files đã thực tế chỉnh sửa**:
  - `app/src/main/java/com/finlux/app/core/designsystem/modern/ModernLiquidGlass.kt` (Xóa bỏ `luminance() < 0.4f`, đọc trực tiếp `isDark` & `tokens.surfaceSoft`)
  - `app/src/main/java/com/finlux/app/presentation/transaction/modern/ModernTransactionsScreen.kt` (Backdrop Liquid Glass, chia tỷ lệ cột tránh đè text, token màu chuẩn Dark/Light)
  - `app/src/main/java/com/finlux/app/presentation/transaction/TransactionDetailSheet.kt` (Xử lý chuỗi ví rỗng, loại bỏ dấu phẩy lơ lửng, nền kính Dark Mode)
  - `app/src/main/java/com/finlux/app/presentation/wallet/modern/ModernWalletsScreen.kt` (Viền chọn màu thẻ đa tầng, FlowRow cho chip loại tài khoản)
  - `app/src/main/java/com/finlux/app/presentation/wallet/classic/ClassicWalletsScreen.kt` (Viền chọn màu thẻ đa tầng)
  - `app/src/main/java/com/finlux/app/presentation/reports/modern/ModernReportsScreen.kt` (Padding đáy 120dp chống che khuất nút Xuất báo cáo, Treemap auto-switch icon + %)
  - `app/src/main/java/com/finlux/app/presentation/reports/classic/ClassicReportsScreen.kt` (Padding đáy 120dp chống che khuất nút Xuất báo cáo, Treemap auto-switch icon + %)
  - `app/src/main/java/com/finlux/app/presentation/components/MainBottomBar.kt` (Thêm gradient scrim mờ chống nhìn xuyên thấu danh sách)
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxFormComponents.kt` (`maxLines = 2` + `lineHeight = 11.sp` cho Category Picker)
  - `app/src/main/java/com/finlux/app/presentation/budget/modern/ModernBudgetScreen.kt` (Đổi icon cảnh báo sang `NotificationsActive`, làm phẳng background pill kỳ hạn)
  - `app/src/main/java/com/finlux/app/presentation/budget/classic/ClassicBudgetScreen.kt` (Đổi icon cảnh báo sang `NotificationsActive`, làm phẳng background pill)
  - `app/src/main/java/com/finlux/app/presentation/settings/salary/SalaryCycleSettingsSheet.kt` (Bọc dải ngày chu kỳ thành thẻ phân cách rõ ràng)
  - `app/src/main/java/com/finlux/app/presentation/home/modern/ModernHomeScreen.kt` (Auto-scale font size số dư lớn)
  - `app/src/main/java/com/finlux/app/presentation/home/classic/ClassicHomeScreen.kt` (Auto-scale font size số dư lớn)
  - `app/src/main/java/com/finlux/app/presentation/auth/AuthScreens.kt`, `AuthViewModel.kt`, `SplashViewModel.kt`, `FirebaseAuthRepository.kt` (Chế độ dùng thử & xử lý thiết bị không có Play Services)
- **Kết quả Kiểm Thử & Xác Thực (Verification Results)**:
  - `./gradlew testDebugUnitTest`: **100% PASS** (0 failed tests).
  - `./gradlew assembleDebug`: **BUILD SUCCESSFUL**.
  - Đã cài đặt APK và xác thực thực tế trên thiết bị vật lý `192.168.17.153:39865`. Toàn bộ 14 lỗi đã được chụp ảnh màn hình nghiệm thu thành công.
- **Tài liệu tham chiếu Single Source of Truth**:
  - `docs/UI_UX_AUDIT_FINDINGS.md`
  - `docs/AUDIT_REPORT_CLUSTER_1.md`

### [Task-PRISM-HERO-TEXTURE-AND-BIGGER-AMOUNT] — Nâng cỡ chữ số tiền siêu lớn in đậm và thiết kế họa tiết chìm chuyên biệt cho từng thẻ
- **Status**: `[DONE]`
- **Mục tiêu**:
  1. Nâng kích thước font số tiền lên cỡ siêu lớn `38sp` (ExtraBold/Bold), tăng độ tương phản và nổi bật thị giác tức thì.
  2. Thiết kế họa tiết nền (ambient textures & watermark patterns) chìm tinh xảo riêng biệt cho 4 thẻ:
     - Thẻ Ví: Họa tiết vòm bảo mật & lưới khối đa tầng (Vault Rings & Security Mesh).
     - Thẻ Thu: Họa tiết dải sáng tăng trưởng cực quang & chevron dâng sóng (Aurora Ascent Waves).
     - Thẻ Chi: Họa tiết cung đo đa tầng ngân sách & quỹ đạo chi tiêu (Radar Gauge & Precision Curves).
     - Thẻ Dòng tiền: Họa tiết sóng tuần hoàn nhịp điệu kép (Harmonic Flow Vectors & Dot Matrix).
  3. Đảm bảo toàn bộ họa tiết vẽ ở lớp nền (`alpha` tinh tế 0.08f–0.18f), không che khuất dữ liệu số, Mini Bar Chart hay các nút bấm.
- **Files đã sửa đổi**:
  - `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`
  - `app/src/test/java/com/finlux/app/presentation/home/prism/PrismHomeLayoutTest.kt`
- **Kết quả kiểm thử & xác thực**:
  - `gradlew testDebugUnitTest`: **100% PASS** (218/218 tests).
  - `gradlew assembleDebug`: Build thành công trong 22s.
  - Cài đặt lên máy thật `7f4ca06a` qua ADB, xác minh visual trực tiếp trên cả 4 thẻ đạt chuẩn ngân hàng số sang trọng 10/10.

### [Task-PRISM-HERO-DATA-FIRST-REDESIGN] — Tái cấu trúc 4 thẻ Hero theo chuẩn ngân hàng Data-First
- **Status**: `[DONE]`
- **Mục tiêu**: Tái thiết kế toàn bộ 4 thẻ Hero Tổng quan theo tiêu chuẩn data-first, ngân hàng chuyên nghiệp:
  1. Đổi tiêu đề sang kỳ linh hoạt ("Thu kỳ này", "Chi kỳ này", "Dòng tiền kỳ này", "Số dư hiện có") kèm khoảng thời gian thực tế `dd/MM – dd/MM`.
  2. Số tiền chuẩn định dạng `30-32sp`, Bold, biểu tượng `₫`.
  3. Dòng phụ phản ánh thông tin thực tế: số lượng khoản thu/chi ("12 khoản thu", "28 khoản chi") thay vì lặp lại tiêu đề.
  4. Bỏ các hình minh họa và mũi tên gây hiểu sai; thay bằng Mini Bar Chart hiển thị phân bổ giao dịch thực tế theo các khoảng trong kỳ.
  5. Thêm nút "Xem chi tiết ›" trực tiếp chuyển đến màn hình tương ứng.
  6. Thay thế các chấm carousel bằng Named Morphing Capsule Indicator dễ hiểu, có thể bấm chọn trang.
  7. Giảm chiều cao card từ 196dp xuống 180dp gọn gàng, tinh tế.
  8. Chuẩn hóa màu ngữ nghĩa cao cấp, giảm độ chói: Xanh ngọc (Thu), Đỏ san hô (Chi), Xanh tím (Dòng tiền), Xanh navy (Ví).
- **Files đã sửa đổi**:
  - `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`
  - `app/src/test/java/com/finlux/app/presentation/home/prism/PrismHomeLayoutTest.kt`
- **Kết quả kiểm thử & xác thực**:
  - `gradlew testDebugUnitTest`: **100% PASS** (218/218 tests).
  - `gradlew assembleDebug`: Build thành công trong 7s.
  - Cài đặt lên máy thật `7f4ca06a` qua ADB, xác minh visual trực tiếp trên cả 4 thẻ đạt chuẩn 10/10.

### [Task-PRISM-HERO-AMOUNT-FONT-UPDATE] — Nâng cỡ chữ và chuẩn hóa font hệ thống cho số tiền 4 thẻ Hero
- **Status**: `[DONE]`
- **Mục tiêu**: Nâng kích thước font chữ số tiền của 4 thẻ Hero Tổng quan tài chính lên cỡ lớn hơn (từ 36sp lên 38sp đối với các số tiền thông dụng), áp dụng font chữ hệ thống `FontFamily.Default` với `FontWeight.Bold`, mở rộng khung chữ lên `fillMaxWidth(0.80f)` và chỉnh `letterSpacing = (-0.2).sp` để chữ số to rõ, sắc nét, tự nhiên và không bị co ép.
- **Scope**: `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`, `app/src/test/java/com/finlux/app/presentation/home/prism/PrismHomeLayoutTest.kt`, `HANDOVER_LOG.md`.
- **Kết quả thực hiện**:
  1. Nâng cỡ chữ `prismOverviewAmountFontSizeSp`: độ dài thông dụng lên `38sp` (trước 36sp), ngưỡng lớn hơn điều chỉnh lên `34sp / 31sp / 28sp`.
  2. Chuẩn hóa font hệ thống `FontFamily.Default` + `FontWeight.Bold`, khoảng cách ký tự `letterSpacing = (-0.2).sp` hiển thị thoáng và sắc nét trên mọi thiết bị.
  3. Tăng tỷ lệ chiều rộng khối chữ `Column` lên `fillMaxWidth(0.80f)` đảm bảo không bị ngắt dòng hay tràn ký hiệu tiền tệ.
- **Kiểm thử và xác nhận**:
  - `gradlew testDebugUnitTest`: **PASS 100%** (217/217 tests).
  - Cài đặt trực tiếp lên thiết bị thật `7f4ca06a` qua ADB và chụp màn hình xác minh hiển thị thực tế hoàn hảo.

### [Task-PRISM-HOME-HERO-BACKGROUND-REDESIGN] — Thiết kế lại hình nền nghệ thuật 4 thẻ Hero Tổng quan tài chính
- **Status**: `[DONE]`
- **Mục tiêu**: Tái thiết kế toàn diện hình nền nghệ thuật Canvas đa lớp cho 4 thẻ Hero Tổng quan (Số dư ví, Thu nhập, Chi tiêu, Dòng tiền ròng) theo phong cách Liquid Glass siêu cao cấp: ánh sáng phản quang đa tầng (ambient radial orbs), sóng lượn thanh thoát, lưới điểm ma trận tài chính, radar phân bổ chi tiết và biểu tượng nhận diện 3D nổi khối sang trọng thay thế các nét vẽ cơ bản trước đó.
- **Scope**: `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`, `HANDOVER_LOG.md`.
- **Kết quả thực hiện**:
  1. **Đồng bộ bảng màu chuyển sắc (Rich Semantic Gradients)**: Tách biệt 4 palette màu cao cấp cho từng thẻ (Số dư: Royal Cobalt & Vivid Violet, Thu: Emerald & Jade Aurora, Chi: Velvet Ruby & Sunset Crimson, Dòng tiền: Midnight Indigo & Cyber Azure) kèm đổ bóng màu `spotColor` và viền kính phản chiếu đa tầng.
  2. **Nghệ thuật Canvas đa lớp chuyên sâu cho 4 thẻ**:
     - *Thẻ 1 - Số dư hiện có*: Vòng quỹ đạo két an toàn (orbital vault rings), dải sóng thanh thoát, đồng xu kính bán trong suốt phản quang, bụi sao tài chính (constellation dust) và ánh sao ✦.
     - *Thẻ 2 - Thu tháng này*: Vùng tăng trưởng chuyển màu, 4 cột trụ tăng trưởng thủy tinh frosted glass có nắp phản quang, đường xu hướng bứt phá và ngôi sao đỉnh cao (apex star ✦).
     - *Thẻ 3 - Chi tháng này*: Radar ngân sách chính xác với sweep gradient, vòng phân độ chia vạch tọa độ lượng giác, đường phân bổ cột mốc chi tiêu với các điểm nút phát quang.
     - *Thẻ 4 - Dòng tiền ròng*: Hai đường sóng hình sin điều hòa kép (Inflow & Outflow) giao nhau tại các điểm cân bằng tài chính phát sáng, kết hợp lưới ma trận số 4x4.
  3. **Huy hiệu kính nổi khối (Floating Prism Badge)**: Nâng cấp khung icon bên phải thành khối kính Frosted Glass Crystal đa lớp phản xạ viền sáng và đổ bóng nổi khối 3D.
- **Kiểm thử và xác nhận**:
  - `gradlew testDebugUnitTest`: **PASS 100%** (217/217 tests).
  - Cài đặt trực tiếp lên thiết bị thật `7f4ca06a` qua ADB và kiểm tra visual capture screenshot thực tế cả 4 thẻ thành công mỹ mãn.
- **Files thực tế chỉnh sửa**:
  - `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`
  - `HANDOVER_LOG.md`

### [Task-PRISM-HOME-FULL-HERO-CAROUSEL] — Hero tài chính toàn khối
- **Status**: `[DONE]`
- **Mục tiêu**: Bỏ tiêu đề/badge/hàng tab rời của Tổng quan tài chính; mở rộng thành một hero card lớn vuốt trực tiếp giữa 4 trang và tự chuyển 10 giây.
- **Scope dự kiến**: `PrismHomeScreen.kt`, test layout Home và tài liệu UI/Context/Plan/Changelog/Handover.
- **Rà soát nghiệp vụ**: Chỉ đổi trình bày; giữ nguyên dữ liệu, route, phép tính và schema. `BA_SPEC.md`/`DATA_SPEC.md` không đổi.
- **Kết quả**:
  1. Đã bỏ hoàn toàn tiêu đề `Tổng quan tài chính`, badge `Tự động · 10s` và hàng bốn tab rời.
  2. `HorizontalPager` trở thành hero 196dp toàn khối; vuốt trực tiếp trên thẻ, tự chuyển vòng 10 giây và đặt chỉ báo trang bên trong cạnh dưới.
  3. Typography số tiền dùng dải 27–36sp; nhãn, mô tả và pill trạng thái tăng cỡ/weight đồng nhất.
  4. Nền bốn trang dùng gradient semantic rõ hơn; Canvas vector tăng tương phản nhưng vẫn giới hạn ở vùng phải để không che số liệu.
- **Files thực tế chỉnh sửa**:
  - `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`
  - `app/src/test/java/com/finlux/app/presentation/home/prism/PrismHomeLayoutTest.kt`
  - `docs/UI_SPEC.md`, `docs/CONTEXT.md`, `docs/PLAN.md`, `CHANGELOG.md`, `HANDOVER_LOG.md`.
- **Kiểm thử và cài đặt**:
  - `gradlew :app:compileDebugKotlin :app:testDebugUnitTest`: **PASS** — 217/217 tests.
  - `gradlew :app:assembleDebug`: **BUILD SUCCESSFUL**; APK 34,184,800 bytes.
  - ADB `7f4ca06a` (`2107119DC`): cài đè **Success**, `versionName=1.12.0`, `versionCode=147`, PID `27832`.
  - Hậu kiểm screenshot thiết bị: hero đứng yên hiển thị đủ số dư, mô tả, hình nền và 4 chỉ báo; không còn header/tab rời hoặc tràn chữ.

### [Task-PRISM-HOME-MERGED-FINANCIAL-OVERVIEW] — Gộp Hero và carousel KPI
- **Status**: `[DONE]`
- **Mục tiêu**: Gộp Số dư hiện có và carousel Thu/Chi/Dòng tiền thành một thẻ Tổng quan tài chính Prism duy nhất, giữ tự chuyển 10 giây; mỗi thẻ có hình nền nhận diện riêng.
- **Scope dự kiến**: `PrismHomeScreen.kt`, test layout Home và tài liệu UI/Context/Plan/Changelog/Handover.
- **Rà soát nghiệp vụ**: Chỉ thay đổi bố cục; giữ nguyên dữ liệu, route, phép tính và schema. `BA_SPEC.md`/`DATA_SPEC.md` không đổi.
- **Kết quả**:
  1. Hero Số dư và cụm KPI cũ đã được thay bằng một `PrismFinancialOverviewCard` có đúng 4 trang: Số dư, Thu nhập, Chi tiêu, Dòng tiền.
  2. Mỗi trang có gradient theo token theme và Canvas vector riêng: ví/thẻ/đồng xu, biểu đồ tăng trưởng, vòng phân bổ chi, đường xu hướng dòng tiền.
  3. Giữ chọn tab, vuốt trực tiếp, tự chuyển vòng 10 giây, tôn trọng cấu hình Animation và tự co cỡ chữ số tiền.
  4. Hình nền nằm ở vùng phải, dùng opacity semantic để không che số liệu và đồng bộ cả theme sáng/tối.
- **Files thực tế chỉnh sửa**:
  - `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`
  - `app/src/test/java/com/finlux/app/presentation/home/prism/PrismHomeLayoutTest.kt`
  - `docs/UI_SPEC.md`, `docs/CONTEXT.md`, `docs/PLAN.md`, `CHANGELOG.md`, `HANDOVER_LOG.md`.
- **Kiểm thử và cài đặt**:
  - `gradlew :app:testDebugUnitTest :app:assembleDebug`: **PASS** — 217/217 tests, 0 failed, 0 errors, 0 skipped.
  - APK debug: `app/build/outputs/apk/debug/app-debug.apk` (34,192,239 bytes).
  - ADB `7f4ca06a` (`2107119DC`): cài đè **Success**, package `1.12.0`, PID `23845`.
  - Screenshot hậu kiểm thu được khung đen vì màn hình thiết bị tắt/khóa; build, cài đặt và tiến trình app đã được xác nhận.

### [Task-PRISM-HOME-NO-LIQUID-GLASS] — Trả Home về đúng ngôn ngữ FinLux Prism
- **Status**: `[DONE]`
- **Mục tiêu**: Loại bỏ hiệu ứng Liquid Glass khỏi header, carousel Thu/Chi/Dòng tiền và thẻ phân tích Home Prism; giữ vuốt và tự chuyển 10 giây.
- **Kết quả**:
  1. Header đổi thành Row Prism 52dp không container, avatar 44dp và nút thông báo `surfaceSoft` 42dp.
  2. Carousel KPI dùng `Surface` solid/soft, viền semantic mảnh; loại bỏ blur, chromatic rim, glow, shadow màu và scale kính.
  3. Thẻ phân tích danh mục chuyển từ `LiquidGlassCard` sang Prism `Surface` dùng tokens.
  4. Giữ nguyên tab Thu/Chi/Dòng tiền, vuốt bám tay, tự chuyển vòng 10 giây và tôn trọng tùy chọn Animation.
- **Files thực tế chỉnh sửa**: `PrismHomeScreen.kt`, `PrismHomeLayoutTest.kt`, `ModernLiquidGlass.kt` và `UI_SPEC.md`, `CONTEXT.md`, `PLAN.md`, `CHANGELOG.md`, `HANDOVER_LOG.md`.
- **Rà soát nghiệp vụ**: Không đổi dữ liệu, route, phép tính hoặc schema; `BA_SPEC.md` và `DATA_SPEC.md` không đổi.
- **Kiểm thử và cài đặt**:
  - `gradlew :app:compileDebugKotlin :app:testDebugUnitTest`: **PASS**.
  - `gradlew assembleDebug`: **PASS** — 215/215 tests, 0 failed, 0 errors, 0 skipped.
  - APK debug: `app/build/outputs/apk/debug/app-debug.apk` (34,161,436 bytes).
  - ADB `7f4ca06a` (`2107119DC`): cài đè **Success**, package `1.12.0`, PID `12640`.
  - Screenshot hậu kiểm chỉ thu được màn hình khóa vì thiết bị yêu cầu người dùng mở khóa; không thể xác nhận pixel trực tiếp trong app qua ADB.

### [Task-FIX-PRISM-HOME-HEADER-OVERSIZE] — Header Liquid Glass chiếm toàn màn hình
- **Status**: `[DONE]`
- **Mục tiêu**: Sửa lớp kính của header Home bị đo theo chiều cao tối đa, che toàn bộ nội dung trang trên thiết bị thật.
- **Nguyên nhân gốc**: Hai lớp nền quang học của `LiquidGlassSurface` dùng `fillMaxSize()` trong `Box`, tham gia phép đo với ràng buộc lỏng của `Scaffold.topBar` và kéo card theo chiều cao tối đa.
- **Kết quả**:
  1. Chuyển hai lớp quang học sang `matchParentSize()` để chỉ bám kích thước container sau khi nội dung đã được đo.
  2. Khóa phòng vệ capsule header Home ở 68dp, vừa avatar 48dp và padding dọc 10dp.
  3. Bổ sung unit test khóa thông số chiều cao để tránh tái phát.
- **Files thực tế chỉnh sửa**:
  - `app/src/main/java/com/finlux/app/core/designsystem/modern/ModernLiquidGlass.kt`
  - `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`
  - `app/src/test/java/com/finlux/app/presentation/home/prism/PrismHomeLayoutTest.kt`
  - `docs/UI_SPEC.md`, `docs/CONTEXT.md`, `docs/PLAN.md`, `CHANGELOG.md`, `HANDOVER_LOG.md`.
- **Rà soát nghiệp vụ**: Không đổi dữ liệu, phép tính hoặc schema; `BA_SPEC.md` và `DATA_SPEC.md` không đổi.
- **Kiểm thử và cài đặt**:
  - `gradlew testDebugUnitTest assembleDebug`: **PASS** — 215/215 tests, 0 failed, 0 errors, 0 skipped.
  - APK debug: `app/build/outputs/apk/debug/app-debug.apk` (34,163,048 bytes).
  - ADB `7f4ca06a` (`2107119DC`): `adb install -r` **Success**, package `1.12.0` khởi chạy với PID `7223`.
  - ADB ngắt sau khi mở app nên không lấy được screenshot hậu kiểm tự động; không có crash build/runtime được ghi nhận trước khi mất kết nối.

### [Task-RELEASE-v1.12.0] — Git checkpoint cải tiến Home/Lịch sử
- **Status**: `[DONE]`
- **Mục tiêu**: Đóng gói, kiểm thử, commit và push bộ cải tiến Header Home, carousel Thu/Chi/Dòng tiền và thẻ nhóm giao dịch lên `origin/main`.
- **Scope thực tế**: Ứng dụng Android, unit test Home, tài liệu đặc tả/kế hoạch/changelog/handover và `app/build.gradle.kts`.
- **Version**: `versionCode 147`, `versionName 1.12.0`.
- **Xác minh**:
  - `gradlew testDebugUnitTest assembleDebug`: **PASS** — 214/214 tests, 0 failed, 0 errors, 0 skipped.
  - APK debug: `app/build/outputs/apk/debug/app-debug.apk` (33,583,717 bytes).
  - Commit tính năng: `e78905d` (`bump(release): v1.12.0 - nang cap Home va lich su giao dich`).
  - Push: **SUCCESS** — `origin/main` đã nhận commit `e78905d`.

### [Task-PRISM-HOME-AUTO-SUMMARY-CAROUSEL] — Tổng quan Thu/Chi/Dòng tiền tự chuyển
- **Status**: `[DONE]`
- **Mục tiêu**: Thay cụm ba KPI nhỏ khó đọc bằng carousel Liquid Glass thân thiện, cho phép chạm/vuốt trực tiếp và tự chuyển chỉ số sau mỗi 10 giây.
- **Kết quả**:
  1. Thay hàng ba card nhỏ bằng một thẻ KPI lớn Liquid Glass REGULAR, tăng cỡ số tiền lên 20–27sp theo độ dài và bổ sung mô tả dễ hiểu.
  2. Bổ sung ba tab Thu nhập/Chi tiêu/Dòng tiền, hỗ trợ chạm chọn nhanh và vuốt bám tay qua `HorizontalPager`.
  3. Carousel tự chuyển vòng sau 10 giây không thao tác; thao tác vuốt/chọn trang đặt lại thời gian chờ. Khi tắt Animation, trang chuyển tức thời.
  4. Thêm scale/alpha theo vị trí trang, spring khi chạm, viền/bóng phát quang semantic và chỉ báo trang động.
- **Files thực tế chỉnh sửa**:
  - `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`
  - `app/src/test/java/com/finlux/app/presentation/home/prism/PrismHomeLayoutTest.kt`
  - `docs/UI_SPEC.md`, `docs/CONTEXT.md`, `docs/PLAN.md`, `CHANGELOG.md`, `HANDOVER_LOG.md`
- **Rà soát nghiệp vụ**: Giữ nguyên dữ liệu và phép tính Thu/Chi/Dòng tiền; `BA_SPEC.md` và `DATA_SPEC.md` không cần đổi.
- **Kiểm thử**:
  - `gradlew :app:compileDebugKotlin :app:testDebugUnitTest`: **PASS**.
  - `gradlew assembleDebug`: **PASS** — 214/214 tests, 0 failed, 0 errors, 0 skipped.
  - APK debug: `app/build/outputs/apk/debug/app-debug.apk` (34,146,616 bytes).
  - ADB: chưa thể cài bản mới vì thiết bị đã ngắt kết nối sau khi build; `adb devices -l` không còn thiết bị online.

### [Task-PRISM-HOME-PROFILE-HEADER] — Thiết kế lại cụm chào hỏi Trang chủ
- **Status**: `[DONE]`
- **Mục tiêu**: Làm gọn vùng chào hỏi, tên người dùng, avatar và thông báo thành một cụm Liquid Glass cân đối, dễ thao tác trên màn hình hẹp.
- **Kết quả**:
  1. Gom toàn bộ header vào capsule `LiquidGlassCard` chế độ CLEAR, bo góc 22dp và dùng token động cho sáng/tối.
  2. Avatar 48dp đặt bên trái; lời chào/tên ở giữa, hỗ trợ ellipsis khi tên dài; nút thông báo 44dp đặt bên phải.
  3. Badge thông báo hiển thị số chưa đọc và rút gọn `9+`; giữ riêng callback mở Hồ sơ và Thông báo.
- **Files thực tế chỉnh sửa**:
  - `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`
  - `docs/UI_SPEC.md`, `docs/CONTEXT.md`, `docs/PLAN.md`, `CHANGELOG.md`, `HANDOVER_LOG.md`
- **Rà soát tài liệu**: `BA_SPEC.md` và `DATA_SPEC.md` không đổi vì thay đổi chỉ thuộc presentation, không thêm nghiệp vụ hoặc schema.
- **Kiểm thử và cài đặt**:
  - `gradlew testDebugUnitTest assembleDebug`: **PASS** — 213/213 tests, 0 failed, 0 errors, 0 skipped.
  - APK debug: `app/build/outputs/apk/debug/app-debug.apk` (34,134,141 bytes).
  - ADB `7f4ca06a` (`2107119DC`): `adb install -r` **Success**; package `com.finlux.app` khởi chạy và có process hoạt động.

### [Task-PRISM-TRANSACTION-GROUPED-PROFILE-CARD] — Đồng bộ giao dịch Home/Lịch sử theo thẻ nhóm Hồ sơ
- **Status**: `[DONE]`
- **Mục tiêu**: Gom các giao dịch rời thành một container bo góc lớn giống nhóm menu Hồ sơ, giữ đầy đủ semantic tài chính và thao tác hiện có.
- **Kết quả**:
  1. Tạo `FinluxTransactionGroup` dùng chung trong design system: icon pastel 42dp, title/subtitle gọn, amount căn phải và divider inset sau icon.
  2. Home Prism hiển thị tối đa 10 giao dịch gần nhất trong một thẻ nhóm.
  3. Lịch sử Prism hiển thị một thẻ nhóm cho từng ngày, giữ header ngày và tổng dòng tiền ròng ngày.
  4. Tap mở chi tiết, nhấn giữ mở Sửa/Xóa; dark/light và các style semantic lấy từ token dùng chung.
- **Files thực tế chỉnh sửa**:
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxTransactionComponents.kt`
  - `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/prism/PrismTransactionsScreen.kt`
  - `docs/UI_SPEC.md`, `docs/CONTEXT.md`, `docs/PLAN.md`, `CHANGELOG.md`, `HANDOVER_LOG.md`
- **Rà soát tài liệu**: `BA_SPEC.md` và `DATA_SPEC.md` không đổi vì đây là thay đổi presentation-only,
  không thêm nghiệp vụ, schema hoặc phép tính tài chính mới.
- **Kiểm thử**:
  - `gradlew testDebugUnitTest assembleDebug`: **PASS** — 213/213 tests, 0 failed, 0 skipped.
  - APK debug: `app/build/outputs/apk/debug/app-debug.apk` (34,132,701 bytes).
  - ADB: chưa cài được vì `adb devices -l` không có thiết bị online tại thời điểm kiểm tra.

### [Task-UI-UX-MASTER-PLAN-P0-P1-COMPLETION] — Hoàn thiện các khoảng trống P0/P1 sau audit docs/plan
- **Status**: `[DONE]`
- **Mục tiêu & Kết quả hoàn thành**:
  1. **Triệt tiêu Gesture Collision trong điều hướng tab**: Bổ sung cờ `snapResetSwipe` trong [FinluxNavHost.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt) nhằm snap ngay lập tức `swipeDragOffset` về `0f` khi kích hoạt chuyển tab, loại bỏ hiện tượng animation spring giật kép với `slideHorizontally` của NavHost.
  2. **Động hóa nhận diện danh mục Tiết kiệm / Mục tiêu (BR-14)**: Bổ sung overload `netGoalContribution(isSavingsCategory: (String?) -> Boolean)` trong [TransactionSemantics.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/domain/model/TransactionSemantics.kt) và cập nhật [ReportsViewModel.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/reports/ReportsViewModel.kt) nhận diện cả category ID mặc định (`"203"`) lẫn từ khóa localization. Bổ sung test case trong [TransactionSemanticsTest.kt](file:///d:/BT/FinLux/app/src/test/java/com/finlux/app/domain/model/TransactionSemanticsTest.kt).
  3. **Nhóm giao dịch theo ngày đồng bộ Timezone trên mọi Theme**:
     - Expose `financeZone: StateFlow<ZoneId>` từ `TransactionsViewModel`.
     - Đồng bộ nhóm theo ngày và header tổng thu/chi ngày sử dụng `financeZone` trên cả 3 theme: [PrismTransactionsScreen.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/transaction/prism/PrismTransactionsScreen.kt), [ClassicTransactionsScreen.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/transaction/classic/ClassicTransactionsScreen.kt), [ModernTransactionsScreen.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/transaction/modern/ModernTransactionsScreen.kt).
     - Kết nối đầy đủ luồng Undo khi xóa giao dịch và truyền đầy đủ bộ lọc tìm kiếm / khoảng tiền cho `TransactionFilterBottomSheet`.
  4. **100% Dynamic Theme Colors (Zero Hardcoded Hex)**: Thay thế toàn bộ mã màu tĩnh trong Quick Actions và KPI card của [PrismHomeScreen.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt) bằng `LocalFinluxTokens.current` (`primary`, `budget`, `secondary`, `saving`, `onSurfaceVariant`, `income`, `expense`).
  5. **Traceability Matrix**: Bổ sung Mục 8 "Ma trận truy vết nghiệp vụ (Traceability Matrix)" trong [00_FINLUX_UI_UX_MASTER.md](file:///d:/BT/FinLux/docs/plan/00_FINLUX_UI_UX_MASTER.md) đối soát đầy đủ 22 module kế hoạch với các Use Case trong `BA_SPEC.md` và `DATA_SPEC.md`.
  6. **Thiết kế Thẻ 3 Cột Thu Gọn, Không Viền với Icon 3D Chìm Trong Suốt Tuyệt Đối ([PrismHomeScreen.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt))**:
     - Loại bỏ hoàn toàn viền (`border = null`) trong cả 3 thẻ: Thu tháng này, Chi tháng này, Dòng tiền ròng.
     - Thu gọn kích thước chiều cao xuống `102dp` (~33%).
     - Thay thế hoàn toàn ảnh raster có khung xám bằng bộ 3 đồ họa 3D Vector Canvas 100% trong suốt: Mũi tên 3D Isometric ngọc lục bảo + đồng xu vàng nổi (Thu), Mũi tên cong 3D đỏ san hô + đồng xu nổi (Chi), Khối cầu pha lê 3D với sóng phân tích sáng (Dòng tiền).
     - Chữ và số tiền in đậm nổi bật đè trực tiếp lên trên icon 3D chìm, loại bỏ triệt để viền hộp xám.
  7. **Tăng Cỡ Số Tiền Nổi Bật và Đổ Bóng Nhẹ Cho Thẻ 3 Cột ([PrismHomeScreen.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt))**:
     - Nâng cỡ chữ số tiền lên đến `18.sp` (tăng ~24% kích thước), letterSpacing `0.3.sp`, `FontWeight.Black`.
     - Thêm hiệu ứng `Shadow` đổ bóng nhẹ tương ứng theo màu sắc giúp số tiền nổi bật rõ nét trên cả nền sáng và nền tối.
     - Đồng bộ và cập nhật toàn bộ test case `PrismHomeLayoutTest.kt` pass 100%.
  8. **Nâng Cấp Thẻ 3 Cột Nổi Bật, Sang Trọng và Linh Hoạt (Prism Elevation & Tactile Spring) ([PrismHomeScreen.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt))**:
     - Độ nổi khối vượt trội với đổ bóng màu kép (`Modifier.shadow` có `spotColor` phát quang xanh/đỏ/xanh dương) và nền phủ nhẹ `Brush.verticalGradient`.
     - Tích hợp tương tác nảy đàn hồi lò xo (`Spring.StiffnessMediumLow`, scale `0.95f`) khi chạm, tạo cảm giác linh hoạt và sống động.
  9. **Tinh Chỉnh Thẻ 3 Cột Sang Trọng, Tối Giản, Cân Đối Tuyệt Đối ([PrismHomeScreen.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt))**:
     - Loại bỏ hoàn toàn hình vẽ watermark góc cắt cụt và đồng xu vàng gây rối mắt, trả lại không gian thoáng đãng sạch sẽ.
     - Tinh chỉnh huy hiệu kính `PrismMetricMiniBadge` dạng Glass Squircle trong suốt siêu nét đồng màu với thẻ.
     - Tối ưu bố cục 3 tầng đối xứng hoàn hảo (Huy hiệu kính + Tiêu đề -> Số tiền to bản rõ nét -> Pill xu hướng căn giữa).
     - Bổ sung viền kính siêu mỏng `0.8.dp` phản quang mềm mại giúp định hình rõ ranh giới thẻ trên màn hình.
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt`
  - `app/src/main/java/com/finlux/app/domain/model/TransactionSemantics.kt`
  - `app/src/main/java/com/finlux/app/presentation/reports/ReportsViewModel.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/TransactionsViewModel.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/prism/PrismTransactionsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/classic/ClassicTransactionsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/modern/ModernTransactionsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/AddTransactionSheet.kt`
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxFormComponents.kt`
  - `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`
  - `app/src/test/java/com/finlux/app/domain/model/TransactionSemanticsTest.kt`
  - `app/src/test/java/com/finlux/app/presentation/home/prism/PrismHomeLayoutTest.kt`
  - `docs/plan/00_FINLUX_UI_UX_MASTER.md`
  - `HANDOVER_LOG.md`

### [Task-UI-UX-MASTER-PLAN-REFINEMENT-v1.11.11] — Cải Tiến Toàn Diện Giao Diện FinLux (docs/plan Master Plan)
- **Status**: `[DONE]`
- **Mục tiêu**:
  1. **Gói 1: Home Quick Actions Liquid Glass & KPI Kỳ tài chính**:
     - Chuyển 5 nút tính năng tròn từ pastel phẳng sang chất liệu Liquid Glass Frost Tiles (kính khúc xạ, viền sáng phản chiếu đa tầng, radial glow, độ tương phản cao trên mọi theme).
     - Đồng bộ nhãn 3 thẻ KPI theo dải ngày `FinancialPeriod` khi bật `SalaryCycleConfig` ("Thu kỳ này", "Chi kỳ này", "Dòng tiền kỳ").
  2. **Gói 2: Tái cấu trúc Report Navigation (4 Tabs tinh gọn)**:
     - Tái cấu trúc điều hướng Báo cáo: 4 tab chính cố định (`Tổng quan`, `Thu & Chi`, `Danh mục`, `Chuyên sâu`) vừa vặn chiều ngang màn hình, chấm dứt tình trạng cuộn ngang 8 tab.
     - Tích hợp Secondary Segmented Chips trong tab `Chuyên sâu` cho 5 báo cáo chi tiết (`Vay nợ`, `Tiết kiệm`, `Ngân sách`, `Tài sản`, `Xu hướng`).
     - Kết nối thông minh chuyển tab tự động từ các card trên tab Tổng quan.
  3. **Gói 3: Trực quan hóa Ngân sách & Dải màu rủi ro (Budget Risk)**:
     - Thêm pill nhãn phần trăm trực quan "Đã dùng X%" (hoặc "Vượt X%").
     - Áp dụng dải màu rủi ro động: Xanh lá (<=70%), Vàng cam (70-90%), Cam đậm (90-100%), Đỏ báo động (>100%).
     - Bổ sung đếm ngược "Còn X ngày trong kỳ" trên thẻ Hero ngân sách.
  4. **Gói 4: Gom nhóm Lịch sử giao dịch theo mốc ngày (Date Grouping)**:
     - Nhóm danh sách giao dịch theo header ngày ("Hôm nay", "Hôm qua", "dd/MM/yyyy") kèm tổng thu/chi ngày rõ ràng.
  5. **Gói 5: Chuẩn hóa component FinluxEmptyState dùng chung**:
     - Chuẩn hóa màu chữ và icon trong `FinluxFeedbackComponents.kt`, tuân thủ 100% theme tokens (`tokens.onHero`, `tokens.onSurface`).
- **Kết quả Kiểm thử**:
  - `gradlew :app:testDebugUnitTest`: **PASS 100%** (34 actionable tasks, 12 executed, 22 up-to-date, exit code 0).
- **Files Thực tế Chỉnh sửa**:
  - `app/build.gradle.kts` (MODIFIED — version bump 1.11.11, versionCode 145)
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxFeedbackComponents.kt` (MODIFIED — Dynamic onHero token)
  - `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt` (MODIFIED — Liquid Glass Frost Tiles & cycle KPI labels)
  - `app/src/main/java/com/finlux/app/presentation/reports/prism/PrismReportsScreen.kt` (MODIFIED — 4-tab Segmented pill navigation & Deep Dive subtabs)
  - `app/src/main/java/com/finlux/app/presentation/budget/prism/PrismBudgetScreen.kt` (MODIFIED — Dynamic risk palette, percent pill & days countdown)
  - `app/src/main/java/com/finlux/app/presentation/transaction/prism/PrismTransactionsScreen.kt` (MODIFIED — Daily transaction grouping with day headers and net cashflow)
  - `CHANGELOG.md` (MODIFIED)
  - `HANDOVER_LOG.md` (MODIFIED)

### [Task-PAYMENT-REMINDER-EXACT-ALARM-SYNC] — Khắc Phục Doze Mode, Chống Trôi Giờ Chu Kỳ & Đồng Bộ Đa Thiết Bị Nhắc Nhở Thanh Toán
- **Status**: `[DONE]`
- **Mục tiêu**:
  1. **Chống Doze Mode & Báo thức chính xác (Exact Alarm)**:
     - Khai báo các quyền `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM`, `WAKE_LOCK` trong `AndroidManifest.xml`.
     - Nâng cấp `AlarmReminderScheduler` sang sử dụng `AlarmManager.setAlarmClock()` (kèm fallback `setExactAndAllowWhileIdle`) để nổ thông báo chính xác từng giây ngay cả khi tắt màn hình/chế độ Doze.
     - Cấu hình Notification với độ ưu tiên cao (`PRIORITY_MAX`, `CATEGORY_REMINDER`, `VISIBILITY_PUBLIC`).
  2. **Xóa bỏ trôi giờ (Zero Time Drift Engine)**:
     - Viết mới `ReminderUtils.computeNextTriggerDate` bảo toàn 100% mốc `LocalTime` gốc và ngày trong tháng gốc từ `startDate` của nhắc nhở.
  3. **Đồng bộ hóa đa thiết bị (ReminderSyncObserver)**:
     - Tạo `ReminderSyncObserver` tự động lắng nghe Firestore khi đăng nhập/khởi chạy app để tự động nạp toàn bộ nhắc nhở vào `AlarmManager` của thiết bị đó.
  4. **Kiểm thử & Đóng gói**:
     - Viết Unit Tests bao phủ Zero Time Drift, AlarmReminderScheduler và ReminderSyncObserver.
     - Chạy toàn bộ Unit Tests đảm bảo 100% PASS: `./gradlew testDebugUnitTest` (208/208 tests passed).
     - Nâng version lên `v1.11.10` (versionCode 144), cập nhật `CHANGELOG.md`/`HANDOVER_LOG.md`.
- **Test Results**:
  * Unit tests: `gradlew testDebugUnitTest` -> **100% PASS** (34 tasks completed, 0 failure, 0 error, 208 test cases passed).
  * Compile: `compileDebugKotlin` -> **PASS** (0 error).
- **Files Modified/Created**:
  - `app/src/main/AndroidManifest.xml` (MODIFIED)
  - `app/src/main/java/com/finlux/app/domain/model/ReminderUtils.kt` (NEW)
  - `app/src/main/java/com/finlux/app/data/local/reminder/AlarmReminderScheduler.kt` (MODIFIED)
  - `app/src/main/java/com/finlux/app/data/local/reminder/ReminderSyncObserver.kt` (NEW)
  - `app/src/main/java/com/finlux/app/presentation/reminders/RemindersScreen.kt` (MODIFIED)
  - `app/src/main/java/com/finlux/app/presentation/RootViewModel.kt` (MODIFIED)
  - `app/src/test/java/com/finlux/app/domain/model/ReminderUtilsTest.kt` (NEW)
  - `app/src/test/java/com/finlux/app/data/local/reminder/AlarmReminderSchedulerTest.kt` (NEW)
  - `app/src/test/java/com/finlux/app/data/local/reminder/ReminderSyncObserverTest.kt` (NEW)
  - `app/src/test/java/com/finlux/app/presentation/RootViewModelTest.kt` (MODIFIED)
  - `app/build.gradle.kts` (MODIFIED - Bump 1.11.10, versionCode 144)
  - `CHANGELOG.md` (MODIFIED - Release notes v1.11.10)
  - `HANDOVER_LOG.md` (MODIFIED - Complete log)
- **Current Status**: **[DONE]**

### [Task-FIREBASE-CI-MOCHA-TIMEOUT] — Tăng Timeout Mocha Chạy Firebase Firestore Rules & Functions Test Trên CI
- **Status**: `[DONE]`
- **Mục tiêu**: Khắc phục lỗi timeout 2000ms trên GitHub Actions runner (`firebase-ci.yml`) khi khởi tạo Firestore Emulator và nạp `firestore.rules`.
- **Files Modified**:
  - `functions/package.json`: Tăng timeout Mocha từ mặc định 2000ms lên `--timeout 20000` (20 giây).
- **Test Results**: Cấu hình Mocha timeout 20s đảm bảo CI runner có đủ thời gian khởi tạo RulesTestEnvironment và nạp rules.
- **Current Status**: **[DONE]**

### [Task-FINANCIAL-SALARY-CYCLE-TECH-DEBTS-v1.11.8] — Xử Lý Triệt Để 2 Nợ Kỹ Thuật: Đồng Bộ Số Liệu Trang Chủ & Tự Động Hóa Background Scheduler Ngày Lương
- **Status**: `[DONE]`
- **Mục tiêu**:
  1. **Nợ kỹ thuật 1 (HomeViewModel.kt)**: Lắng nghe `SalaryCycleConfig` qua `flatMapLatest`. Khi `enabled == true`, truy vấn theo `observePeriod(cycle.start, cycle.endExclusive)` và tính toán `DashboardSummary` từ các giao dịch trong chu kỳ, đảm bảo số liệu Tổng Thu/Chi/Ròng trên Trang chủ khớp 100% với nhãn dải ngày kỳ lương; khi `enabled == false` giữ nguyên tháng dương lịch.
  2. **Nợ kỹ thuật 2 (Background Scheduler & Notification)**:
     - Tạo `SalaryCycleScheduler` interface và triển khai `AlarmSalaryCycleScheduler` dùng `AlarmManager` (hẹn 09:00 sáng ngày nhận lương theo múi giờ tài chính).
     - Xây dựng `SalaryCycleReceiver`: Bắn Push & In-app Notification chào đón kỳ mới, kích hoạt `ExecuteSalaryRolloverUseCase` nếu là `MOVE_TO_SAVINGS` (hoặc thông báo nhắc nhở nếu `ASK_EACH_CYCLE`), tự động lên lịch cho chu kỳ tiếp theo.
     - Đăng ký receiver trong `AndroidManifest.xml` (`exported=true` kèm `intent-filter`) và khôi phục lịch trong `BootReceiver.kt`.
     - Tích hợp `SalaryCycleScheduler` vào `SalaryCycleViewModel.kt` khi lưu cấu hình.
  3. **Kiểm thử & Đóng gói**:
     - Cập nhật `HomeViewModelTest.kt` và viết mới `AlarmSalaryCycleSchedulerTest.kt`.
     - Chạy `gradlew testDebugUnitTest` đạt 100% PASS (34 tasks completed, 0 failure, 0 error).
     - Bump version lên `v1.11.8` (versionCode 142), cập nhật `CHANGELOG.md`, `HANDOVER_LOG.md` và nạp APK qua ADB.
- **Test Results**:
  * Unit tests: `gradlew testDebugUnitTest` -> **100% PASS** (34 tasks completed, 0 failure, 0 error).
  * Compile: `compileDebugKotlin` -> **PASS** (0 error).
  * ADB Live Device Test: `adb shell am broadcast -a com.finlux.app.ACTION_SALARY_PAYDAY -n com.finlux.app/.data.local.salary.SalaryCycleReceiver` -> **PASS** (Bắn thành công cả 2 notifications: ID 9925 Welcome Notification & ID 9926 Rollover Notification, lưu In-app Notification và tự động lên lịch kỳ tiếp theo).
- **Files Modified/Created**:
  - `app/src/main/java/com/finlux/app/domain/repository/SalaryCycleScheduler.kt` (NEW)
  - `app/src/main/java/com/finlux/app/data/local/salary/AlarmSalaryCycleScheduler.kt` (NEW)
  - `app/src/main/java/com/finlux/app/data/local/salary/SalaryCycleReceiver.kt` (NEW)
  - `app/src/main/java/com/finlux/app/presentation/home/HomeViewModel.kt` (MODIFIED)
  - `app/src/main/java/com/finlux/app/presentation/settings/salary/SalaryCycleViewModel.kt` (MODIFIED)
  - `app/src/main/java/com/finlux/app/data/local/reminder/BootReceiver.kt` (MODIFIED)
  - `app/src/main/java/com/finlux/app/data/di/RepositoryModule.kt` (MODIFIED)
  - `app/src/main/AndroidManifest.xml` (MODIFIED)
  - `app/src/test/java/com/finlux/app/presentation/home/HomeViewModelTest.kt` (MODIFIED)
  - `app/src/test/java/com/finlux/app/data/local/salary/AlarmSalaryCycleSchedulerTest.kt` (NEW)
  - `app/build.gradle.kts` (MODIFIED - Bump 1.11.8, versionCode 142)
  - `CHANGELOG.md` (MODIFIED - Ghi log release v1.11.8)
  - `HANDOVER_LOG.md` (MODIFIED - Ghi log hoàn tất)

### [Task-VIETQR-OFFLINE-ASSETS-v1.11.7] — Đồng Bộ 65 Logo VietQR Và Ví Điện Tử Việt Nam Offline
- **Status**: `[DONE]`
- **Mục tiêu**:
  1. Tích hợp bộ 65 logo ngân hàng từ VietQR API và các ví điện tử Việt Nam vào `drawable-nodpi`.
  2. Mở rộng catalog dùng chung lên tối thiểu 75 ngân hàng/ví/tài khoản và áp dụng tự động cho các màn hình Ví Classic, Modern, Prism.
  3. Tìm kiếm tổ chức hỗ trợ tên, mã ngân hàng, BIN và alias dài nhất.
  4. Fix lỗi nhận diện Techcombank không bị match nhầm MBBank.
- **Test Results**:
  * Unit tests: `gradlew testDebugUnitTest` -> **100% PASS**.

### [Task-UNIFY-ALL-AMOUNT-INPUTS-v1.11.6] — Đồng Bộ Hóa 100% Ô Nhập Tiền Toàn Ứng Dụng Sang ErgonomicCompactAmountCard
- **Status**: `[DONE]`
- **Mục tiêu**:
  1. `AddTransactionSheet.kt`: Thay thế khối Hero 38sp tự vẽ bằng `ErgonomicCompactAmountCard` (màu ngữ cảnh động `ExpenseRed` / `IncomeGreen`, chip thông minh Decimal Magnitude Scaling).
  2. `ModernWalletsScreen.kt` & `ClassicWalletsScreen.kt`: Thay thế toàn bộ `OutlinedTextField` + `LazyRow<FilterChip>` (Số dư ví & Chuyển tiền) bằng `ErgonomicCompactAmountCard`.
  3. `PrismWalletsScreen.kt`: Thay thế `FinluxAmountInputCard` trong `QuickTransferSheet` bằng `ErgonomicCompactAmountCard`.
  4. `GoalsScreen.kt`: Thay 2 ô `OutlinedTextField` trong `GoalEditor` (Mục tiêu & Tích lũy tháng) bằng `ErgonomicCompactAmountCard`.
  5. `NotificationsScreen.kt`: Thay thế `FinluxAmountInputCard` trong `PayNotificationDialog` bằng `ErgonomicCompactAmountCard`.
  6. Cập nhật `FORM_COMPONENTS_SPEC.md`, bump version lên `1.11.6` (versionCode 140), chạy `gradlew testDebugUnitTest` 100% PASS và nạp APK qua ADB.
- **Test Results**:
  * Unit tests: `gradlew testDebugUnitTest` -> **100% PASS** (34 tasks completed, 0 failure, 0 error).
  * Compile: `compileDebugKotlin` -> **PASS** (0 error).
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/presentation/transaction/AddTransactionSheet.kt`
  - `app/src/main/java/com/finlux/app/presentation/wallet/modern/ModernWalletsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/wallet/classic/ClassicWalletsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/wallet/prism/PrismWalletsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/goal/GoalsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/notifications/NotificationsScreen.kt`
  - `docs/FORM_COMPONENTS_SPEC.md`
  - `app/build.gradle.kts`
  - `CHANGELOG.md`
  - `HANDOVER_LOG.md`

### [Task-SMART-AMOUNT-SUGGESTIONS-v1.11.5] — Nâng Cấp Thuật Toán Gợi Ý Tiền Tệ Thông Minh Cho ErgonomicCompactAmountCard
- **Status**: `[DONE]`
- **Mục tiêu**:
  1. `FinluxFormComponents.kt`:
     - Cấu trúc lại hàm `generateAmountSuggestions` theo cơ chế Decimal Magnitude Scaling.
     - Sinh các mốc `V = N * (10^k)` trong khoảng thực tế từ 1.000đ đến 1.000.000.000đ.
     - Hỗ trợ chính xác các mốc: rỗng/0 -> mảng mặc định 8 mốc; "3" -> 3k..30M; "35" -> 3.5k..35M; "356" -> 3.56k..35.6M; "3568" -> 35.68k..356.8M.
  2. Viết mới `AmountSuggestionsTest.kt` kiểm thử 100% các kịch bản input.
  3. Bump version lên `1.11.5` (versionCode 139) trong `app/build.gradle.kts`.
  4. Chạy `gradlew testDebugUnitTest` đạt 100% PASS và nạp APK lên thiết bị qua ADB.
- **Test Results**:
  * Unit tests: `gradlew testDebugUnitTest` -> **100% PASS** (34 tasks completed, 0 failure, 0 error).
  * Compile: `compileDebugKotlin` -> **PASS** (0 error, 0 warning).
  * Assemble & Install: `.\scripts\build_and_install.ps1` -> Đang chuẩn bị nạp.
- **Files Modified/Created**:
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxFormComponents.kt` (MODIFIED - Cập nhật thuật toán Decimal Magnitude Scaling)
  - `app/src/test/java/com/finlux/app/core/designsystem/AmountSuggestionsTest.kt` (NEW - 6 test cases bao phủ 100% kịch bản)
  - `app/build.gradle.kts` (MODIFIED - Bump 1.11.5, versionCode 139)
  - `CHANGELOG.md` (MODIFIED - Ghi log v1.11.5)
  - `HANDOVER_LOG.md` (MODIFIED - Ghi log task)

### [Task-FIX-TRANSFER-DELETION-v1.11.4] — Sửa Lỗi Thứ Tự Đọc/Ghi Firestore Transaction & Tối Ưu Luồng Xóa Cặp
- **Status**: `[DONE]`
- **Mục tiêu**:
  1. `FirebaseTransactionRepository.kt`:
     - Tuân thủ 100% quy tắc Firestore Transaction (All reads before all writes).
     - Đưa toàn bộ `atomic.get()` (`sourceDoc`, `destDoc`, `counterpartDoc`) lên trước mọi lệnh `atomic.delete()` và `atomic.update()`.
  2. `TransactionDetailSheet.kt`:
     - Dọn sạch mã trùng lặp, tối ưu thao tác xóa chỉ qua 1 Hộp thoại xác nhận duy nhất chứa đầy đủ thông báo hoàn tiền 2 đầu ví.
  3. `FirebaseTransactionRepositoryTest.kt`:
     - Thêm Unit Test tự động kiểm thử xóa cặp chuyển tiền và hoàn tác số dư ví.
  4. Chạy 100% Unit Tests PASS, bump version `1.11.4` (versionCode 138) và nạp APK lên thiết bị.
- **Test Results**:
  * Unit tests: `gradlew testDebugUnitTest` -> **100% PASS** (34 tasks completed, 0 failure, 0 error).
  * Compile: `compileDebugKotlin` -> **PASS** (0 error, 0 warning).
  * Assemble & Install: `.\scripts\build_and_install.ps1` -> Đang chuẩn bị nạp.
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseTransactionRepository.kt` (MODIFIED - Đưa reads lên trước writes)
  - `app/src/main/java/com/finlux/app/presentation/transaction/TransactionDetailSheet.kt` (MODIFIED - Dọn sạch duplicate và tối ưu nút xóa)
  - `app/src/test/java/com/finlux/app/data/remote/firebase/FirebaseTransactionRepositoryTest.kt` (MODIFIED - Thêm unit test xóa cặp)
  - `app/build.gradle.kts` (MODIFIED - Bump 1.11.4, versionCode 138)
  - `CHANGELOG.md` (MODIFIED - Ghi log v1.11.4)
  - `HANDOVER_LOG.md` (MODIFIED - Ghi log task)

### [Task-CASCADE-ATOMIC-TRANSFER-DELETION-v1.11.3] — Xóa Đối Ứng Cả Cặp Giao Dịch Chuyển Tiền & Khóa Sửa Chuyển Tiền
- **Status**: `[DONE]`
- **Mục tiêu**:
  1. Tầng Repository (`FirebaseTransactionRepository.kt` & `DemoFinluxRepository.kt`):
     - Khi xóa `TRANSFER_OUT` hoặc `TRANSFER_IN`, tự động tìm và xóa luôn bản ghi đối ứng `_in` hoặc `_out` trong 1 Firestore Transaction duy nhất.
     - Hoàn tác số dư cả 2 ví: Cộng lại ví nguồn (`+amount`), trừ thu hồi ví đích (`-amount`).
     - Khóa chặn gọi update đối với giao dịch chuyển tiền.
  2. Tầng Giao diện (`TransactionDetailSheet.kt`):
     - Ẩn nút "Chỉnh sửa giao dịch" khi `isTransfer == true`, hiển thị Info Banner bảo vệ số dư.
     - Cập nhật Confirm Dialog xóa cặp hiển thị tên ví nhận và cảnh báo hoàn tiền cả hai ví.
     - Bảo đảm 100% các giao dịch Thu nhập (`INCOME`) và Chi tiêu (`EXPENSE`) thông thường vẫn giữ nguyên đầy đủ 2 nút Sửa và Xóa.
  3. Tầng Điều hướng & Màn hình (`FinluxNavHost.kt`, `PrismTransactionsScreen.kt`, `ModernTransactionsScreen.kt`, `ClassicTransactionsScreen.kt`):
     - Chặn `onEditTransaction` và ẩn swipe-to-edit với giao dịch chuyển tiền.
  4. Cập nhật `docs/BA_SPEC.md` bổ sung quy tắc `BR-07.1`, bump version `v1.11.3` (versionCode 137), chạy 100% Unit Tests PASS và nạp APK lên thiết bị.
- **Test Results**:
  * Unit tests: `gradlew testDebugUnitTest` -> **100% PASS** (34 tasks completed, 0 failure, 0 error).
  * Compile: `compileDebugKotlin` -> **PASS** (0 error, 0 warning).
  * Assemble & Install: `.\scripts\build_and_install.ps1` -> **SUCCESS** (APK nạp thành công lên thiết bị).
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseTransactionRepository.kt` (MODIFIED - Xóa cascade cặp nguyên tử và chặn update transfer)
  - `app/src/main/java/com/finlux/app/data/demo/DemoFinluxRepository.kt` (MODIFIED - Cập nhật logic xóa cặp và chặn update transfer cho demo)
  - `app/src/main/java/com/finlux/app/presentation/transaction/TransactionDetailSheet.kt` (MODIFIED - Khóa nút Sửa, Info banner, Confirm dialog xóa cặp)
  - `app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt` (MODIFIED - Lọc guard onEditTransaction và pass relatedWallet vào dialog)
  - `app/src/main/java/com/finlux/app/presentation/transaction/prism/PrismTransactionsScreen.kt` (MODIFIED - Guard onEdit và pass relatedWallet)
  - `app/src/main/java/com/finlux/app/presentation/transaction/modern/ModernTransactionsScreen.kt` (MODIFIED - Ẩn swipe edit icon và pass relatedWallet)
  - `app/src/main/java/com/finlux/app/presentation/transaction/classic/ClassicTransactionsScreen.kt` (MODIFIED - Ẩn edit icon và pass relatedWallet)
  - `docs/BA_SPEC.md` (MODIFIED - Bổ sung BR-07.1)
  - `app/build.gradle.kts` (MODIFIED - Bump version lên 1.11.3, versionCode 137)
  - `CHANGELOG.md` (MODIFIED - Ghi log release v1.11.3)
  - `HANDOVER_LOG.md` (MODIFIED - Ghi nhận trạng thái hoàn tất)
### [Task-CI-FIX-LINT-PDF-v1.11.3] — Sửa Lỗi CI Lint UnusedMaterial3ScaffoldPaddingParameter, Khôi Phục KPI Tổng Chi Tiêu PDF & Dọn Dẹp Conflict Marker
- **Status**: `[DONE]`
- **Mục tiêu**:
  1. Thêm `@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")` tại `FinluxNavHost.kt` để giải quyết triệt để lỗi CI `:app:lintDebug` do Scaffold edge-to-edge transparent bỏ qua inner padding.
  2. Khôi phục dòng `canvas.drawText("-${formatVndAmount(summary.expense.value)}", 225f, y + 43f, paintExpense)` tại `ReportExporter.kt` bị xóa nhầm, hiển thị chuẩn số tiền KPI Tổng Chi Tiêu trên PDF.
  3. Dọn dẹp conflict marker sót lại trong `HANDOVER_LOG.md`.
  4. Chạy toàn diện `gradlew testDebugUnitTest` & `gradlew lintDebug` bảo đảm 100% PASS, bump version lên v1.11.3 (versionCode 137).
- **Test Results**:
  * Unit tests: `gradlew testDebugUnitTest` -> **100% PASS** (34 actionable tasks, 0 failure, 0 error).
  * Lint: `gradlew lintDebug` -> **100% PASS** (0 errors, 45 warnings, 1 hint).
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt`
  - `app/src/main/java/com/finlux/app/core/export/ReportExporter.kt`
  - `HANDOVER_LOG.md`
  - `CHANGELOG.md`
  - `app/build.gradle.kts`

### [Task-ZERO-CONFIG-THEME-INHERITANCE-v1.11.2] — Khắc Phục Triệt Để Lỗi Lệch Theme & Thiết Lập Zero-Config Theme Inheritance
- **Status**: `[DONE]`
- **Mục tiêu**:
  1. Sửa lỗi `DynamicGradientBackdrop` trong `StyleBackdrop.kt`: Xử lý dải màu Dark Mode sâu thẳm khi `tokens.isDark == true`, loại bỏ 100% mã màu trắng cứng.
  2. Nâng cấp `FinluxScreenScaffold.kt`: Tự động nhận diện `LocalAppUiStyle` (Prism dùng `tokens.background`, Classic/Modern dùng `FinluxStyleBackdrop`) và inject `LocalContentColor provides tokens.textPrimary` xuyên suốt các slots.
  3. Chuẩn hóa `GlassTopBar` & `FinluxScreenHeader`: Đảm bảo `containerColor = Color.Transparent`, màu chữ/icon đọc động từ tokens.
  4. Rà soát làm sạch nền trên `ExpenseScreen`, `IncomeScreen`, `CategoriesScreen`, `DebtDashboardScreen`, `PrismHomeScreen`.
  5. Chạy 100% Unit Tests PASS, bump version lên v1.11.2 (versionCode 136), đóng gói và sẵn sàng APK.
- **Test Results**:
  * Unit tests: `gradlew testDebugUnitTest` -> **100% PASS** (34 tasks completed, 0 failure, 0 error).
  * Compile: `compileDebugKotlin` -> **PASS** (0 error, 0 warning).
  * Assemble: `assembleDebug` -> **PASS** (`app-debug.apk` sẵn sàng).
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/core/designsystem/StyleBackdrop.kt` (MODIFIED - Sửa dải màu Dark Mode sâu thẳm, loại bỏ mã màu trắng cứng)
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxScreenScaffold.kt` (MODIFIED - Thiết lập Zero-Config Theme Inheritance theo LocalAppUiStyle, inject LocalContentColor)
  - `app/src/main/java/com/finlux/app/core/designsystem/LiquidGlass.kt` (MODIFIED - Cấu hình title/nav/action icons đọc từ tokens.textPrimary)
  - `app/build.gradle.kts` (MODIFIED - Bump version lên 1.11.2, versionCode 136)
  - `CHANGELOG.md` (MODIFIED - Ghi log release v1.11.2)
  - `HANDOVER_LOG.md` (MODIFIED - Ghi nhận trạng thái hoàn tất)

### [Task-HOTFIX-Text-Color-Invisibility-v1.11.1] — Khắc Phục Lỗi Chữ Bị Chìm Màu Trên Toàn Bộ Màn Hình
- **Status**: `[DONE]`
- **Mục tiêu**:
  1. Cập nhật `FinluxScreenScaffold.kt`: Đổi `contentColor = Color.Transparent` thành `contentColor = tokens.textPrimary` và bọc lambda `content(paddingValues)` trong `CompositionLocalProvider(LocalContentColor provides tokens.textPrimary)`.
  2. Bổ sung `textPrimary` & `textSecondary` getters trong `FinluxDesignTokens` (`FinluxTokens.kt`).
  3. Rà soát 5 màn hình thí điểm (`CategoriesScreen`, `ExpenseScreen`, `IncomeScreen`, `DebtDashboardScreen`, `PrismHomeScreen`) đảm bảo màu chữ hiển thị sắc nét, tương phản hoàn hảo trên cả Dark & Light Mode.
  4. Chạy `gradlew testDebugUnitTest` 100% PASS, bump version lên v1.11.1 (versionCode 135), đóng gói APK hoàn tất.
- **Test Results**:
  * Unit tests: `gradlew testDebugUnitTest` -> **100% PASS** (34 tasks completed, 0 failure, 0 error).
  * Compile: `compileDebugKotlin` -> **PASS** (0 error, 0 warning).
  * Assemble: `assembleDebug` -> **PASS** (`app-debug.apk` sẵn sàng).
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxScreenScaffold.kt` (MODIFIED - Cấu hình contentColor = tokens.textPrimary và cung cấp CompositionLocalProvider)
  - `app/src/main/java/com/finlux/app/core/designsystem/theme/FinluxTokens.kt` (MODIFIED - Bổ sung textPrimary & textSecondary getters)
  - `app/src/main/java/com/finlux/app/presentation/expense/ExpenseScreen.kt` (MODIFIED - Gắn tường minh tokens.onSurface cho Section Header & note)
  - `app/src/main/java/com/finlux/app/presentation/income/IncomeScreen.kt` (MODIFIED - Gắn tường minh tokens.onSurface cho Section Header & note)
  - `app/build.gradle.kts` (MODIFIED - Bump version lên 1.11.1, versionCode 135)
  - `CHANGELOG.md` (MODIFIED - Ghi log release v1.11.1)
  - `HANDOVER_LOG.md` (MODIFIED - Ghi nhận trạng thái hoàn tất)

### [Task-DESIGN-SYSTEM-CORE-v1.11.0] — Design System Core & Base Screen Scaffolding Architecture
- **Status**: `[DONE]`
- **Mục tiêu**:
  1. Cập nhật `FinluxSpacing` trong `FinluxTokens.kt` (thêm `contentHorizontal`, `screenTop`, `bottomBarClearance`, `compactClearance`, `itemGap`, điều chỉnh `cardGap = 12.dp`).
  2. Tạo `FinluxScreenScaffold.kt` — khung chuẩn Slot API + FinluxStyleBackdrop + Insets sạch cho toàn bộ màn hình (hỗ trợ `showBackdrop` toggle và `containerColor` cho theme solid).
  3. Tạo `FinluxLazyColumn.kt` — LazyColumn dùng chung với auto-padding theo `FinluxListType` và slot `emptyState`.
  4. Refactor thí điểm 5 màn hình: `CategoriesScreen`, `ExpenseScreen`, `IncomeScreen`, `DebtDashboardScreen`, `PrismHomeScreen`.
  5. Chạy 100% Unit Tests PASS, bump version lên v1.11.0 (versionCode 134), cập nhật tài liệu và build/nạp APK lên thiết bị.
- **Test Results**:
  * Unit tests: `gradlew testDebugUnitTest` -> **100% PASS** (34 tasks completed, 0 failure, 0 error).
  * Compile: `compileDebugKotlin` -> **PASS** (0 error, 0 warning).
  * Build & Install: `installDebug` -> **PASS** (Đã nạp thành công lên thiết bị).
- **Files Modified/Created**:
  - `app/src/main/java/com/finlux/app/core/designsystem/theme/FinluxTokens.kt` (MODIFIED - Bổ sung tokens FinluxSpacing)
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxScreenScaffold.kt` (NEW - Base Screen Scaffold với Slot API & Insets sạch)
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxLazyColumn.kt` (NEW - LazyColumn tự động tính đệm theo FinluxListType)
  - `app/src/main/java/com/finlux/app/presentation/category/CategoriesScreen.kt` (MODIFIED - Refactor sang FinluxScreenScaffold + FinluxLazyColumn)
  - `app/src/main/java/com/finlux/app/presentation/expense/ExpenseScreen.kt` (MODIFIED - Refactor sang FinluxScreenScaffold + FinluxLazyColumn)
  - `app/src/main/java/com/finlux/app/presentation/income/IncomeScreen.kt` (MODIFIED - Refactor sang FinluxScreenScaffold + FinluxLazyColumn)
  - `app/src/main/java/com/finlux/app/presentation/debt/DebtDashboardScreen.kt` (MODIFIED - Refactor sang FinluxScreenScaffold + FinluxLazyColumn)
  - `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt` (MODIFIED - Refactor sang FinluxScreenScaffold + FinluxLazyColumn)
  - `app/build.gradle.kts` (MODIFIED - Bump version lên 1.11.0, versionCode 134)
  - `CHANGELOG.md` (MODIFIED - Ghi log release v1.11.0)
  - `docs/UI_SPEC.md` (MODIFIED - Bổ sung mục 0.1 đặc tả Base Screen Scaffolding & Spacing Tokens)

### [Task-FIX-Transaction-Row-Layout-Refactor] - Tái Cấu Trúc Layout Dòng Giao Dịch Chống Tràn & Co Cụm Text
- **Status**: `[DONE]`
- **Goal**:
  1. Cấu trúc lại Row giao dịch chuẩn 3 cột trong `FinluxTransactionComponents.kt`, `PrismHomeScreen.kt`, `PrismTransactionsScreen.kt`, `ExpenseScreen.kt`, `IncomeScreen.kt`:
     - Cột 1: Icon tròn danh mục / chuyển tiền (kích thước cố định 40dp-44dp).
     - Cột 2 (`Modifier.weight(1f).padding(start = 12.dp, end = 8.dp)`): Dòng 1 là Tiêu đề giao dịch (`maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = SemiBold`), Dòng 2 là Ngày tháng & Tuyến ví (`"$dateText · $sourceWallet ➔ $targetWallet"` hoặc `"$dateText · $walletName"`, `maxLines = 1, overflow = TextOverflow.Ellipsis`).
     - Cột 3 (`wrapContentWidth(Alignment.End)`): Chỉ hiển thị duy nhất Số tiền (+/- Amount) căn phải tuyệt đối, không đặt tên ví dưới số tiền.
  2. Chạy 100% unit tests PASS, bump version lên v1.10.21 (versionCode 133), build APK và nạp lên thiết bị qua ADB.
- **Test Results**:
  * Unit tests: `gradlew testDebugUnitTest` -> **100% PASS** (34 tasks completed, 0 failure, 0 error).
- **Files Modified**:
  - `app/build.gradle.kts` (MODIFIED - Bump version to 1.10.21, versionCode 133)
  - `CHANGELOG.md` (MODIFIED - Ghi log release v1.10.21)
  - `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt` (MODIFIED - PrismRecentTransactionItem sang chuẩn 3 cột, chuyển walletDisplayName sang cột 2)
  - `app/src/main/java/com/finlux/app/presentation/transaction/prism/PrismTransactionsScreen.kt` (MODIFIED - PrismTransactionCardItem sang chuẩn 3 cột, chuyển walletDisplayName sang cột 2)
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxTransactionComponents.kt` (MODIFIED - FinluxTransactionRow sang chuẩn 3 cột, hỗ trợ wallet/relatedWallet)
  - `app/src/main/java/com/finlux/app/presentation/expense/ExpenseScreen.kt` (MODIFIED - Tối ưu weight 1f và padding dòng giao dịch)
  - `app/src/main/java/com/finlux/app/presentation/income/IncomeScreen.kt` (MODIFIED - Tối ưu weight 1f và padding dòng giao dịch)
- **Test Results**:
  * Unit tests: `gradlew testDebugUnitTest` -> **100% PASS** (34 tasks completed, 0 failure, 0 error).
- **Files Modified**:
  - `app/build.gradle.kts` (MODIFIED - Bump version to 1.10.20, versionCode 132)
  - `CHANGELOG.md` (MODIFIED - Ghi log release v1.10.20)
  - `app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt` (MODIFIED - NavHost fillMaxSize không padding, root Scaffold contentWindowInsets = 0)
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxHeaderComponents.kt` (MODIFIED - Thêm statusBarsPadding vào FinluxScreenHeader)
  - `app/src/main/java/com/finlux/app/core/designsystem/LiquidGlass.kt` (MODIFIED - Thêm statusBarsPadding vào GlassTopBar)
  - `app/src/main/java/com/finlux/app/core/designsystem/modern/ModernLiquidGlass.kt` (MODIFIED - Thêm statusBarsPadding vào GlassTopBar)
  - `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt` (MODIFIED - Thêm statusBarsPadding cho PrismHomeTopHeader, bottom contentPadding = 96dp)
  - `app/src/main/java/com/finlux/app/presentation/home/modern/ModernHomeScreen.kt` (MODIFIED - Thêm statusBarsPadding cho ReferenceHeader, bottom contentPadding = 96dp)
  - `app/src/main/java/com/finlux/app/presentation/home/classic/ClassicHomeScreen.kt` (MODIFIED - Thêm statusBarsPadding cho ReferenceHeader, bottom contentPadding = 96dp)
  - `app/src/main/java/com/finlux/app/presentation/transaction/prism/PrismTransactionsScreen.kt` (MODIFIED - Thêm statusBarsPadding cho top header row, bottom contentPadding = 96dp cho root tab / 24dp cho sub tab)
  - `app/src/main/java/com/finlux/app/presentation/reports/prism/PrismReportsScreen.kt` (MODIFIED - Thêm statusBarsPadding cho PrismReportsHeader, bottom contentPadding = 96dp)
  - `app/src/main/java/com/finlux/app/presentation/settings/prism/PrismSettingsScreen.kt` (MODIFIED - Thêm statusBarsPadding cho SettingsTitle, bottom contentPadding = 96dp)
  - `app/src/main/java/com/finlux/app/presentation/settings/SettingsScreen.kt` (MODIFIED - bottom contentPadding = 96dp)
  - `app/src/main/java/com/finlux/app/presentation/expense/ExpenseScreen.kt` (MODIFIED - bottom contentPadding = 24dp)
  - `app/src/main/java/com/finlux/app/presentation/income/IncomeScreen.kt` (MODIFIED - bottom contentPadding = 24dp)
  - `app/src/main/java/com/finlux/app/presentation/debt/DebtDashboardScreen.kt` (MODIFIED - bottom contentPadding = 24dp)
  - `app/src/main/java/com/finlux/app/presentation/wallet/prism/PrismWalletsScreen.kt` (MODIFIED - bottom contentPadding = 24dp)
  - `app/src/main/java/com/finlux/app/presentation/wallet/modern/ModernWalletsScreen.kt` (MODIFIED - bottom contentPadding = 24dp)
  - `app/src/main/java/com/finlux/app/presentation/wallet/classic/ClassicWalletsScreen.kt` (MODIFIED - bottom contentPadding = 24dp)
  - `app/src/main/java/com/finlux/app/presentation/budget/prism/PrismBudgetScreen.kt` (MODIFIED - bottom contentPadding = 24dp)
- **Test Results**:
  * Unit tests: `gradlew testDebugUnitTest` -> **100% PASS** (34 tasks completed, 0 failure, 0 error).
- **Files Modified**:
  - `app/build.gradle.kts` (MODIFIED - Bump version to 1.10.19, versionCode 131)
  - `CHANGELOG.md` (MODIFIED - Ghi log release v1.10.19)
  - `app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt` (MODIFIED - Root Scaffold truyền scaffoldPadding trực tiếp vào NavHost)
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxHeaderComponents.kt` (MODIFIED - Xóa statusBarsPadding thừa trong FinluxScreenHeader)
  - `app/src/main/java/com/finlux/app/core/designsystem/LiquidGlass.kt` (MODIFIED - Xóa statusBarsPadding trong GlassTopBar, set windowInsets = 0)
  - `app/src/main/java/com/finlux/app/core/designsystem/modern/ModernLiquidGlass.kt` (MODIFIED - Xóa statusBarsPadding trong GlassTopBar)
  - `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt` (MODIFIED - Bỏ statusBarsPadding trong header, chuẩn hóa bottom 16dp, fix co ép text Carousel và recent items)
  - `app/src/main/java/com/finlux/app/presentation/home/modern/ModernHomeScreen.kt` (MODIFIED - Bỏ statusBarsPadding trong ReferenceHeader, bottom 16dp)
  - `app/src/main/java/com/finlux/app/presentation/home/classic/ClassicHomeScreen.kt` (MODIFIED - Bỏ statusBarsPadding trong ReferenceHeader, bottom 16dp)
  - `app/src/main/java/com/finlux/app/presentation/transaction/prism/PrismTransactionsScreen.kt` (MODIFIED - Bỏ statusBarsPadding ở header row, bottom 16dp)
  - `app/src/main/java/com/finlux/app/presentation/reports/prism/PrismReportsScreen.kt` (MODIFIED - Bỏ statusBarsPadding ở header, bottom 16dp)
  - `app/src/main/java/com/finlux/app/presentation/wallet/prism/PrismWalletsScreen.kt` (MODIFIED - Chuẩn hóa bottom 16dp)
  - `app/src/main/java/com/finlux/app/presentation/budget/prism/PrismBudgetScreen.kt` (MODIFIED - Chuẩn hóa bottom 16dp)
  - `app/src/main/java/com/finlux/app/presentation/settings/prism/PrismSettingsScreen.kt` (MODIFIED - Bỏ statusBarsPadding trong SettingsTitle, bottom 16dp)
  - `app/src/main/java/com/finlux/app/presentation/expense/ExpenseScreen.kt` (MODIFIED - Scaffold padding, contentPadding bottom 16dp, row text ellipsis)
  - `app/src/main/java/com/finlux/app/presentation/income/IncomeScreen.kt` (MODIFIED - Scaffold padding, contentPadding bottom 16dp, row text ellipsis)
  - `app/src/main/java/com/finlux/app/presentation/debt/DebtDashboardScreen.kt` (MODIFIED - FinluxEmptyState chuẩn, bottom 16dp, bỏ spacer 40dp thừa)

### [Task-FEAT-UI-UX-Hardening-Sprint] - Khắc Phục Toàn Diện 21 Hạng Mục Giao Diện & Runtime, Sửa Lỗi Nhân Đôi BottomBar & Đồng Bộ Form Controls
- **Status**: `[DONE]`
- **Goal**:
  1. Xóa bỏ hoàn toàn lỗi Duplicate Stacked Bottom Bar bằng cách loại bỏ bottomBar trong các Scaffold con (Income, Expense, Wallets, Budget) và chỉ để duy nhất 1 Root Scaffold quản lý.
  2. Bổ sung `statusBarsPadding` & `navigationBarsPadding` cho màn hình Quét hóa đơn (`ReceiptCaptureScreen.kt`).
  3. Bổ sung `FinluxStyleBackdrop`, `FinluxEmptyState` và nâng cấp Dialog sang `GlassBottomSheet` cho `CategoriesScreen.kt`.
  4. Đồng bộ `ErgonomicCompactAmountCard` và `FinluxWalletPickerBottomSheet` cho các màn hình Ví, Mục tiêu, Chu kỳ lương, Thêm chi tiêu.
  5. Loại bỏ 100% mã màu hex hardcode tĩnh ở `TransactionDetailSheet.kt`, `TransactionFilterBottomSheet.kt`, `AddTransactionSheet.kt`, `ClassicMainBottomBar.kt`, `FinluxFormComponents.kt`.
  6. Bổ sung `FinluxStyleBackdrop` và nâng cấp `FinluxEmptyState` cho `IncomeScreen.kt`, `ExpenseScreen.kt`, `PrismTransactionsScreen.kt`, `PrismReportsScreen.kt`.
  7. Khắc phục lỗi rò rỉ state `remember(debt.id)` trong `DebtPaymentSheet.kt`, thêm `imePadding()` chống che khuất bàn phím, thêm `Ellipsis` cho dữ liệu dài trên `DebtCard.kt`, `FinluxTransactionRow`, `FinluxHeroCard`.
  8. Tăng version lên v1.10.18 (versionCode 130), chạy full unit tests 100% PASS và nạp APK lên điện thoại test.
- **Test Results**:
  * Unit tests: `gradlew testDebugUnitTest` -> **100% PASS** (0 failures, 0 errors).
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/presentation/income/IncomeScreen.kt` (MODIFIED - Xóa bottomBar, thêm backdrop, empty state và dynamic tokens)
  - `app/src/main/java/com/finlux/app/presentation/expense/ExpenseScreen.kt` (MODIFIED - Xóa bottomBar, thêm backdrop, empty state, dynamic tokens và ExpenseChartColors)
  - `app/src/main/java/com/finlux/app/presentation/wallet/modern/ModernWalletsScreen.kt` (MODIFIED - Xóa bottomBar trong Scaffold)
  - `app/src/main/java/com/finlux/app/presentation/wallet/classic/ClassicWalletsScreen.kt` (MODIFIED - Xóa bottomBar trong Scaffold)
  - `app/src/main/java/com/finlux/app/presentation/budget/modern/ModernBudgetScreen.kt` (MODIFIED - Xóa bottomBar trong Scaffold)
  - `app/src/main/java/com/finlux/app/presentation/budget/classic/ClassicBudgetScreen.kt` (MODIFIED - Xóa bottomBar trong Scaffold)
  - `app/src/main/java/com/finlux/app/presentation/receipt/ReceiptCaptureScreen.kt` (MODIFIED - Thêm statusBarsPadding, navigationBarsPadding, FileProvider và dynamic tokens)
  - `app/src/main/java/com/finlux/app/presentation/category/CategoriesScreen.kt` (MODIFIED - Thêm backdrop, empty state, nâng cấp Dialog sang GlassBottomSheet có insets)
  - `app/src/main/java/com/finlux/app/presentation/debt/DebtPaymentSheet.kt` (MODIFIED - Thêm imePadding, remember(debt.id) chống rò rỉ state)
  - `app/src/main/java/com/finlux/app/presentation/debt/AddEditDebtSheet.kt` (MODIFIED - Thêm imePadding)
  - `app/src/main/java/com/finlux/app/presentation/debt/components/DebtCard.kt` (MODIFIED - Thêm Ellipsis cho debt.name, import ImageVector)
  - `app/src/main/java/com/finlux/app/presentation/goal/GoalsScreen.kt` (MODIFIED - Tích hợp ErgonomicCompactAmountCard, navigationBarsPadding, imePadding, tokens)
  - `app/src/main/java/com/finlux/app/presentation/settings/salary/SalaryCycleSettingsSheet.kt` (MODIFIED - Tích hợp ErgonomicCompactAmountCard, insets, fix successMessage)
  - `app/src/main/java/com/finlux/app/presentation/wallet/prism/PrismWalletsScreen.kt` (MODIFIED - Tích hợp ErgonomicCompactAmountCard, tokens.border cho color selector)
  - `app/src/main/java/com/finlux/app/presentation/transaction/TransactionDetailSheet.kt` (MODIFIED - Thay thế màu hex hardcode bằng tokens.primary)
  - `app/src/main/java/com/finlux/app/presentation/transaction/TransactionFilterBottomSheet.kt` (MODIFIED - Thay thế màu hex hardcode bằng dynamic tokens)
  - `app/src/main/java/com/finlux/app/presentation/transaction/prism/PrismTransactionsScreen.kt` (MODIFIED - Scaffold transparent, dynamic filter pills colors)
  - `app/src/main/java/com/finlux/app/presentation/reports/prism/PrismReportsScreen.kt` (MODIFIED - Scaffold transparent)
  - `app/src/main/java/com/finlux/app/presentation/components/classic/ClassicMainBottomBar.kt` (MODIFIED - Dynamic tokens cho selected/unselected items)
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxFormComponents.kt` (MODIFIED - tokens.surfaceSoft cho ErgonomicFormRow & ErgonomicCompactAmountCard)
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxTransactionComponents.kt` (MODIFIED - Ellipsis cho title và dateText)
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxCardComponents.kt` (MODIFIED - Ellipsis cho DisplayAmount)
  - `app/build.gradle.kts` (MODIFIED - Bump versionCode 130, versionName 1.10.18)
  - `CHANGELOG.md` (MODIFIED - Ghi nhận release v1.10.18)
  - `HANDOVER_LOG.md` (MODIFIED - Cập nhật trạng thái [DONE])

### [Task-FEAT-ErgonomicCompactAmountCard-Focus-Suggestions] - Chỉ Hiển Thị Gợi Ý Nhanh (Chips) Khi Focus Vào Ô Nhập Tiền
- **Status**: `[DONE]`
- **Goal**:
  1. Nâng cấp component `ErgonomicCompactAmountCard` trong `FinluxFormComponents.kt` để quản lý trạng thái `isFocused`.
  2. Chỉ hiển thị dải Quick Suggestion Chips (kèm hiệu ứng AnimatedVisibility) khi người dùng tap/focus vào ô nhập số tiền.
  3. Cập nhật viền viền sáng (focus border highlight) tinh tế khi ô nhập đang được active.
  4. Cập nhật tài liệu quy chuẩn `docs/FORM_COMPONENTS_SPEC.md` và tăng version lên v1.10.17 (versionCode 129).
  5. Chạy unit test (`gradlew testDebugUnitTest` 100% PASS) và nạp APK lên máy thật qua `build_and_install.ps1`.
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxFormComponents.kt` (MODIFIED - Tích hợp focus detection & AnimatedVisibility cho suggestion chips)
  - `docs/FORM_COMPONENTS_SPEC.md` (MODIFIED - Đồng bộ đặc tả ErgonomicCompactAmountCard)
  - `app/build.gradle.kts` (MODIFIED - Tăng versionCode 129, versionName 1.10.17)
  - `CHANGELOG.md` (MODIFIED - Ghi nhận v1.10.17)
  - `HANDOVER_LOG.md` (MODIFIED)
- **Verification**:
  - `./gradlew.bat testDebugUnitTest` -> 100% PASS (34/34 tasks).
  - `build_and_install.ps1` -> Đã nạp thành công APK v1.10.17 vào thiết bị thật qua ADB.
- **Current Status**: `[DONE]`

### [Task-FEAT-Notification-Swipe-And-Click-Interactions] - Chuyển Toàn Bộ Vuốt Thành Xóa & Chạm Thẻ Để Mở Tính Năng Tương Ứng
- **Status**: `[DONE]`
- **Goal**:
  1. Điều chỉnh tương tác vuốt sang trái (Swipe-to-Dismiss) trên toàn bộ các loại thẻ thông báo thành thao tác **Xóa thông báo (`deleteNotification`)**.
  2. Điều chỉnh tương tác chạm vào thẻ (Tap/Click on Card) để điều hướng nhanh đến màn hình tương ứng:
     * Cảnh báo ngân sách ➡️ Mở `/budget`.
     * Cột mốc mục tiêu ➡️ Mở `/goals`.
     * Báo cáo tài chính ➡️ Mở `/reports`.
     * Hạn nợ / Tín dụng ➡️ Mở `/debts`.
     * Lời nhắc hóa đơn chưa trả ➡️ Mở BottomSheet Thanh toán nhanh.
     * Lời nhắc hóa đơn đã trả ➡️ Mở BottomSheet Chi tiết thanh toán.
  3. Cập nhật `DebtViewModelTest.kt` và kiểm thử toàn diện `gradlew testDebugUnitTest` 100% PASS.
  4. Đóng gói và cài đặt APK v1.10.16 (versionCode 128) lên thiết bị thật qua `build_and_install.ps1`.
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/presentation/notifications/NotificationsScreen.kt` (MODIFIED - Vuốt để xóa, Chạm để mở tính năng)
  - `app/src/test/java/com/finlux/app/presentation/debt/DebtViewModelTest.kt` (MODIFIED - Đồng bộ tham số ProcessDebtPaymentUseCase)
  - `app/build.gradle.kts` (MODIFIED - Tăng versionCode 128, versionName 1.10.16)
  - `CHANGELOG.md` (MODIFIED)
  - `HANDOVER_LOG.md` (MODIFIED)
- **Verification**:
  - `./gradlew.bat testDebugUnitTest` -> 100% PASS (34/34 tasks).
  - `build_and_install.ps1` -> Đã build APK v1.10.16 và nạp thành công vào thiết bị thật.
- **Current Status**: `[DONE]`

### [Task-FEAT-Debt-UI-Controls-And-Reminder-Fix] - Nâng Cấp Form Nợ Chuẩn Ergonomic, Khôi Phục Reminder Badge Thẻ Nợ & Fix Lỗi Tính Ngày Nhắc Nhở
- **Status**: `[DONE]`
- **Goal**:
  1. Khôi phục Reminder Schedule Chip (Badge thời hạn nhận thông báo nhắc nợ) trên thẻ nợ `DebtCard.kt`.
  2. Nâng cấp các ô nhập liệu trong `AddEditDebtSheet.kt` sang các control chuẩn `ErgonomicCompactAmountCard` cho Hạn mức/Gốc, Dư nợ, Trả tối thiểu.
  3. Nâng cấp ô nhập Tổng số tiền trả trong `DebtPaymentSheet.kt` sang `ErgonomicCompactAmountCard`.
  4. Sửa thuật toán tính ngày nhắc nhở nợ trong `SyncDebtReminderUseCase.kt` và preview trong `AddEditDebtSheet.kt` (loại bỏ clamp 28 ngày, dùng `LocalDate` chính xác cho các tháng 30/31 ngày).
  5. Cập nhật unit test `SyncDebtReminderUseCaseTest.kt` và kiểm thử toàn bộ `gradlew testDebugUnitTest` 100% PASS.
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/presentation/debt/components/DebtCard.kt` (MODIFIED - Thêm Reminder Schedule Chip hiển thị ngày nhận thông báo)
  - `app/src/main/java/com/finlux/app/presentation/debt/AddEditDebtSheet.kt` (MODIFIED - Kế thừa ErgonomicCompactAmountCard, token màu động)
  - `app/src/main/java/com/finlux/app/presentation/debt/DebtPaymentSheet.kt` (MODIFIED - Kế thừa ErgonomicCompactAmountCard cho tổng số tiền trả)
  - `app/src/main/java/com/finlux/app/domain/usecase/SyncDebtReminderUseCase.kt` (MODIFIED - Sửa thuật toán tính ngày nhắc nợ 30/31 ngày)
  - `app/src/test/java/com/finlux/app/domain/usecase/SyncDebtReminderUseCaseTest.kt` (MODIFIED - Thêm test case cho ngày đến hạn 31)
  - `HANDOVER_LOG.md` (MODIFIED)
- **Verification**:
  - `./gradlew.bat testDebugUnitTest` -> 100% PASS (34/34 tasks).
  - `./gradlew.bat assembleDebug` -> Build APK thành công, nạp vào `/sdcard/Download/FinLux.apk`.
- **Current Status**: `[DONE]`

### [Task-FEAT-Budget-Threshold-Alert-System] - Phục Hồi & Tối Ưu Tính Năng Cảnh Báo Vượt Ngưỡng Ngân Sách (80% & 100%)
- **Status**: `[DONE]`
- **Goal**:
  1. Tích hợp khối thông tin trực quan "Cảnh báo vượt ngưỡng tự động (Smart Threshold Alert)" vào Modal Thêm/Sửa ngân sách trên cả 3 giao diện (`PrismBudgetScreen`, `ClassicBudgetScreen`, `ModernBudgetScreen`), hiển thị tính động mốc cảnh báo 80% (vàng) và 100% (đỏ) theo số tiền nhập.
  2. Nâng cấp `SaveBudgetUseCase` và `BudgetViewModel` tự động tính toán lại cờ cảnh báo (`notified80`, `notified100`) theo hạn mức mới, kích hoạt cảnh báo tức thì nếu hạ hạn mức làm chi tiêu vượt ngưỡng.
  3. Bổ sung kiểm tra và phát cảnh báo ngân sách vào `EditTransactionUseCase` (đồng bộ với `AddTransactionUseCase`).
  4. Fix lỗi không đồng bộ `periodKey` (`month:YYYY-MM` vs `MONTHLY_YYYY-MM`) và thiếu cập nhật `spentAmount` trong `DemoFinluxRepository.kt`.
  5. Tích hợp `NotificationPermissionHandler` vào các màn hình Ngân sách để xin quyền `POST_NOTIFICATIONS` trên Android 13+ (Redmi / HyperOS / MIUI).
  6. Viết Unit Test bổ sung trong `TransactionUseCasesTest.kt` và kiểm thử toàn diện `gradlew testDebugUnitTest` đạt 100% PASS.
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/data/demo/DemoFinluxRepository.kt` (MODIFIED - Sửa format periodKey & đồng bộ spentAmount)
  - `app/src/main/java/com/finlux/app/presentation/budget/prism/PrismBudgetScreen.kt` (MODIFIED - Smart Alert card & NotificationPermissionHandler)
  - `app/src/main/java/com/finlux/app/presentation/budget/classic/ClassicBudgetScreen.kt` (MODIFIED)
  - `app/src/main/java/com/finlux/app/presentation/budget/modern/ModernBudgetScreen.kt` (MODIFIED)
  - `app/src/main/java/com/finlux/app/presentation/budget/BudgetViewModel.kt` (MODIFIED)
  - `app/src/main/java/com/finlux/app/domain/usecase/SaveBudgetUseCase.kt` (MODIFIED)
  - `app/src/main/java/com/finlux/app/domain/usecase/EditTransactionUseCase.kt` (MODIFIED)
  - `app/src/test/java/com/finlux/app/domain/usecase/TransactionUseCasesTest.kt` (MODIFIED)
  - `HANDOVER_LOG.md` (MODIFIED)
- **Verification**:
  - `./gradlew.bat testDebugUnitTest` -> 100% PASS (34 actionable tasks).
  - `./gradlew.bat assembleDebug` -> Build APK thành công, nạp vào `/sdcard/Download/FinLux.apk`.
- **Current Status**: `[DONE]`

### [Task-FEAT-PDF-Statement-Exporter-v1.10.15] - Tích Hợp Báo Cáo PDF Chuẩn Sao Kê Ngân Hàng A4 & Tối Ưu CI Pipeline
- **Status**: `[DONE]`
- **Goal**:
  1. **Tái Cấu Trúc Bảng Báo Cáo PDF Chuẩn Sao Kê Ngân Hàng (`ReportExporter.kt`)**:
     - Thiết kế A4 chuẩn với Header Slate `#F1F5F9` bo góc 4pt, viền mảnh `0.5pt` `#E2E8F0`, phân chia nền Zebra Striping `#F8FAFC` và `#FFFFFF` xen kẽ.
     - Phân bổ 4 cột dữ liệu: Cột 1 Thời gian `dd/MM/yyyy HH:mm`, Cột 2 Danh mục & Ghi chú (2 dòng có `smartEllipsize`), Cột 3 Ví thanh toán (smartEllipsize max 88pt), Cột 4 Số tiền (+ xanh / - đỏ, `formatVndAmount`, Align.RIGHT).
     - Phân tách độc lập tọa độ Y dòng Text và thanh Progress Bar của mục "Cơ Cấu Chi Tiêu Theo Danh Mục", lấy màu chuẩn qua `parseColorHex`.
     - Hộp KPI Summary Card và tự động phân trang đa trang (Multi-page Pagination).
  2. **Tối Ưu Cấu Hình Build & CI Pipeline**:
     - Nâng cấp JVM args trong `gradle.properties`: `-Xmx4g -XX:MaxMetaspaceSize=1g`.
     - Xác minh 100% Unit Tests (`testDebugUnitTest`) và Android Lint (`lintDebug`) PASS.
  3. **Đồng Bộ Tài Liệu Quy Chuẩn**:
     - Bổ sung `UC-17` & `BR-11` vào `docs/BA_SPEC.md`.
     - Bổ sung `SCREEN 19` vào `docs/UI_SPEC.md`.
- **Files Modified/Created**:
  - `app/src/main/java/com/finlux/app/core/export/ReportExporter.kt` (MODIFIED)
  - `gradle.properties` (MODIFIED)
  - `docs/BA_SPEC.md` (MODIFIED)
  - `docs/UI_SPEC.md` (MODIFIED)
  - `app/build.gradle.kts` (MODIFIED - Bump versionCode 127, versionName 1.10.15)
  - `CHANGELOG.md` (MODIFIED)
  - `HANDOVER_LOG.md` (MODIFIED)
- **Verification**:
  - `./gradlew.bat testDebugUnitTest` -> 100% PASS (34 actionable tasks).
  - `./gradlew.bat lintDebug` -> 100% PASS (BUILD SUCCESSFUL).


### [Task-FEAT-Vietnamese-Banks-EWallets-Presets-v1.10.14] - Bộ Nhận Diện Ngân Hàng & Ví Điện Tử Việt Nam (35+ Tổ Chức Tài Chính) & Chọn Nhanh Thiết Lập Ví
- **Status**: `[DONE]`
- **Goal**:
  1. **Tạo bộ tài nguyên biểu tượng Vector Drawable thương hiệu**:
     - Ngân hàng: Vietcombank, Techcombank, MB Bank, ACB, VPBank, BIDV, VietinBank, TPBank.
     - Ví điện tử: MoMo, ZaloPay, Viettel Money, VNPay, ShopeePay, PayPal.
     - Tài sản thông dụng: Tiền mặt, Sổ tiết kiệm.
  2. **Xây dựng hệ thống dữ liệu & nhận diện tự động (`FinancialInstitutions.kt`)**:
     - Danh mục 35+ ngân hàng, ví điện tử, tiền mặt, tiết kiệm, crypto, thẻ tín dụng.
     - Nhận diện thông minh qua tên ví (`findInstitutionForWallet`) hoặc mã viết tắt.
     - Composable `FinancialInstitutionLogo` hỗ trợ vector drawable hoặc monogram gradient dập nổi.
     - UI Component `InstitutionSelectorSection` & `InstitutionCatalogDialog` chọn nhanh 1 chạm, tự điền tên, loại ví, màu sắc chủ đạo.
  3. **Tích hợp đồng bộ toàn bộ màn hình**:
     - Áp dụng trên 3 giao diện `PrismWalletsScreen`, `ClassicWalletsScreen`, `ModernWalletsScreen`.
     - Áp dụng vào dialog chọn ví dùng chung `FinluxWalletPickerBottomSheet`.
  4. **Kiểm thử & Xác minh**:
     - Thêm bộ kiểm thử `FinancialInstitutionsTest.kt` kiểm tra mapping và danh mục.
     - Chạy 100% test debug unit test PASS.
- **Files Modified/Created**:
  - `app/src/main/res/drawable/ic_bank_vietcombank.xml` (NEW)
  - `app/src/main/res/drawable/ic_bank_techcombank.xml` (NEW)
  - `app/src/main/res/drawable/ic_bank_mbbank.xml` (NEW)
  - `app/src/main/res/drawable/ic_bank_acb.xml` (NEW)
  - `app/src/main/res/drawable/ic_bank_vpbank.xml` (NEW)
  - `app/src/main/res/drawable/ic_bank_bidv.xml` (NEW)
  - `app/src/main/res/drawable/ic_bank_vietinbank.xml` (NEW)
  - `app/src/main/res/drawable/ic_bank_tpbank.xml` (NEW)
  - `app/src/main/res/drawable/ic_ewallet_momo.xml` (NEW)
  - `app/src/main/res/drawable/ic_ewallet_zalopay.xml` (NEW)
  - `app/src/main/res/drawable/ic_ewallet_viettelmoney.xml` (NEW)
  - `app/src/main/res/drawable/ic_ewallet_vnpay.xml` (NEW)
  - `app/src/main/res/drawable/ic_ewallet_shopeepay.xml` (NEW)
  - `app/src/main/res/drawable/ic_ewallet_paypal.xml` (NEW)
  - `app/src/main/res/drawable/ic_wallet_cash.xml` (NEW)
  - `app/src/main/res/drawable/ic_wallet_savings.xml` (NEW)
  - `app/src/main/java/com/finlux/app/core/designsystem/FinancialInstitutions.kt` (NEW)
  - `app/src/main/java/com/finlux/app/presentation/wallet/prism/PrismWalletsScreen.kt` (MODIFIED)
  - `app/src/main/java/com/finlux/app/presentation/wallet/classic/ClassicWalletsScreen.kt` (MODIFIED)
  - `app/src/main/java/com/finlux/app/presentation/wallet/modern/ModernWalletsScreen.kt` (MODIFIED)
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxFormComponents.kt` (MODIFIED)
  - `app/src/test/java/com/finlux/app/core/designsystem/FinancialInstitutionsTest.kt` (NEW)
  - `app/build.gradle.kts` (MODIFIED)
  - `CHANGELOG.md` (MODIFIED)

### [Task-FEAT-Comprehensive-Reports-FixedBottomNav-v1.10.13] - Báo Cáo Đa Chiều (Vay Nợ, Tiết Kiệm, Ngân Sách, Tài Sản) & Cố Định Bottom Navigation Bar
- **Status**: `[DONE]`
- **Goal**:
  1. **Bổ sung hệ thống báo cáo tài chính đa chiều toàn diện (8 Chuyên mục)**:
     - **Báo cáo Vay & Nợ (Debts & Loans)**: Tổng dư nợ, tổng nợ gốc, tiền đã trả, tiền gốc/lãi thanh toán trong kỳ, và tiến độ hoàn thành từng khoản nợ.
     - **Báo cáo Tiết kiệm & Tích lũy (Savings & Goals)**: Tỷ lệ tiết kiệm thực tế, tổng tích lũy, tiến độ hoàn thành các mục tiêu tài chính (`FinancialGoal`).
     - **Báo cáo Ngân sách (Budgets)**: Tỷ lệ sử dụng hạn mức ngân sách, danh mục an toàn/cảnh báo/vượt hạn mức (đỏ).
     - **Báo cáo Tài sản & Ví (Wallets & Net Worth)**: Tổng tài sản ròng (Net Worth = Tổng số dư ví - Tổng dư nợ), phân bổ tài sản theo loại ví (Tiền mặt, Ngân hàng, Tiết kiệm, Thẻ tín dụng, Đầu tư), dòng tiền thu/chi theo ví.
     - **Báo cáo Thu chi, Danh mục và Xu hướng**: Donut chart cơ cấu chi tiêu và nguồn thu nhập.
  2. **Cố định Bottom Navigation Bar khi vuốt chuyển tab**:
     - Hoist `MainBottomBar` ra `Scaffold` gốc trong `FinluxNavHost.kt`.
     - Loại bỏ `bottomBar` cục bộ trong từng màn hình để menu đáy hoàn toàn đứng yên khi vuốt ngang.
  3. **Chuẩn hóa công thức tính trung bình thu/chi mỗi ngày**:
     - Tính theo số ngày thực tế đã trôi qua trong kỳ (đến ngày hiện tại) thay vì chia cho cả 30-31 ngày trong tương lai.
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt`
  - `app/src/main/java/com/finlux/app/presentation/reports/ReportsViewModel.kt`
  - `app/src/main/java/com/finlux/app/presentation/reports/prism/PrismReportsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/reports/classic/ClassicReportsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/reports/modern/ModernReportsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/home/classic/ClassicHomeScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/home/modern/ModernHomeScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/prism/PrismTransactionsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/classic/ClassicTransactionsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/modern/ModernTransactionsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/settings/prism/PrismSettingsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/settings/SettingsScreen.kt`
  - `app/src/test/java/com/finlux/app/presentation/reports/prism/PrismReportsDataTest.kt`
  - `app/build.gradle.kts`
  - `CHANGELOG.md`
- **Test Results**: 100% Unit Tests Passed.

### [Task-FEAT-TransactionHistory-NetCashFlow-Filters-FormEnlarge] - Thay "Tổng giá trị giao dịch" bằng "Dòng tiền ròng", Bộ Lọc Đa Chiều & Mở Rộng Form Nhập Liệu
- **Status**: `[DONE]`
- **Goal**:
  1. **Thay thế "Tổng giá trị giao dịch" bằng "Dòng tiền ròng" (Net Cash Flow = Thu - Chi)**: Hiển thị số tiền ròng (+/-) rõ ràng kèm phân tích chi tiết phụ `Thu: +X • Chi: -Y` và số lượng giao dịch, áp dụng đồng bộ trên cả 3 giao diện `Prism`, `Classic`, `Modern`.
  2. **Kích hoạt & nâng cấp Icon Bộ lọc trong Lịch sử giao dịch**:
     - Mở `TransactionFilterBottomSheet` với bộ lọc đa chiều:
       - **Kỳ báo cáo**: Tất cả, Tuần này, Tháng này, Tháng trước, Năm nay.
       - **Ví**: Lọc theo từng ví cụ thể hoặc tất cả ví.
       - **Danh mục**: Lọc theo từng danh mục cụ thể hoặc tất cả danh mục.
     - Hiển thị Badge đếm số lượng bộ lọc đang kích hoạt trên TopBar.
  3. **Mở rộng kích thước form nhập liệu và đưa ô Ghi chú xuống ngay dưới Số tiền**:
     - Tăng cỡ chữ số tiền lên 38sp, ký hiệu ₫ 32sp, padding 18dp, phím tắt nhanh 13.5sp bold.
     - Di chuyển ô Ghi chú lên vị trí ngay dưới thẻ nhập số tiền tại: `AddTransactionSheet` (Thêm/Sửa thu chi), `DebtPaymentSheet` (Thanh toán nợ), `PrismWalletsScreen` (Chuyển tiền giữa các ví).
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxAmountInputCard.kt`
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxFormComponents.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/AddTransactionSheet.kt`
  - `app/src/main/java/com/finlux/app/presentation/debt/DebtPaymentSheet.kt`
  - `app/src/main/java/com/finlux/app/presentation/wallet/prism/PrismWalletsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/TransactionsViewModel.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/TransactionFilterBottomSheet.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/prism/PrismTransactionsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/classic/ClassicTransactionsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/modern/ModernTransactionsScreen.kt`
  - `HANDOVER_LOG.md`
- **Result**: `gradlew testDebugUnitTest` ➡️ **BUILD SUCCESSFUL (100% tests PASS)**.

### [Task-UI-AddTransactionSheet-Enhancements] - Tối Ưu UI Nhập Số Tiền & Chuyển Ô Ghi Chú Dưới Phần Tiền
- **Status**: `[DONE]`
- **Goal**:
  1. Tăng kích thước khu vực nhập số tiền (Font size 38sp, ₫ 32sp) và các phím tắt nhanh (+10k, +50k, +100k, +500k) to rõ, dễ nhìn, dễ bấm.
  2. Đưa mục **Ghi chú giao dịch** lên ngay bên dưới phần Số tiền để người dùng dễ dàng điền nội dung lý do thanh toán ngay khi vừa nhập tiền xong.
  3. Mở rộng kích thước, padding và font chữ của `ErgonomicInputRow` để nhập liệu thuận tiện hơn.
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/presentation/transaction/AddTransactionSheet.kt`
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxFormComponents.kt`
  - `app/build.gradle.kts`
  - `CHANGELOG.md`
  - `HANDOVER_LOG.md`
- **Result**: `gradlew testDebugUnitTest` ➡️ **BUILD SUCCESSFUL (100% tests PASS)**.

### [Task-FEAT-Notification-SwipeNavigation] - Phân Tách Cử Chỉ Vuốt Trái Điều Hướng (Ngân Sách, Mục Tiêu, Báo Cáo, Nợ) & Chạm Đánh Dấu Đọc
- **Status**: `[DONE]`
- **Goal**:
  1. Điều chỉnh cử chỉ vuốt sang trái (`EndToStart`) trên từng thẻ thông báo theo đúng nghiệp vụ:
     - Cảnh báo ngân sách: Kéo sang trái ➡️ Tự động điều hướng sang màn hình Ngân sách (`budget`) (hiển thị action nền màu tím/đỏ và icon `ArrowForward` + "Xem ngân sách").
     - Cột mốc mục tiêu: Kéo sang trái ➡️ Tự động điều hướng sang màn hình Mục tiêu (`goals`) (nền vàng cam + "Xem mục tiêu").
     - Báo cáo tài chính: Kéo sang trái ➡️ Tự động điều hướng sang màn hình Báo cáo (`reports`) (nền xanh ngọc + "Xem báo cáo").
     - Hạn nợ / Thẻ tín dụng: Kéo sang trái ➡️ Tự động điều hướng sang màn hình Quản lý nợ (`debts`) (nền tím + "Quản lý nợ").
     - Nhắc hóa đơn / Thông báo khác: Kéo sang trái ➡️ Xóa thông báo khỏi danh sách (nền đỏ + "Xóa").
  2. Đặt `positionalThreshold = { it * 0.30f }` để cử chỉ vuốt nhạy và mượt mà hơn trên mọi màn hình cảm ứng, sau khi điều hướng thì thẻ tự động snap trở lại vị trí ban đầu.
  3. Hành vi chạm vào thân thẻ (Tap): Chỉ đánh dấu đã đọc (`markAsRead`), mở modal thanh toán / chi tiết thanh toán cho hóa đơn, không tự ý chuyển màn hình khi chỉ chạm nhẹ.
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/presentation/notifications/NotificationsScreen.kt`
  - `app/build.gradle.kts`
  - `CHANGELOG.md`
  - `HANDOVER_LOG.md`
- **Result**: `gradlew testDebugUnitTest` ➡️ **BUILD SUCCESSFUL (100% tests PASS)**. Build và nạp APK v1.10.10 (versionCode 122) thành công lên thiết bị Android.

### [Task-FEAT-Notification-SwipeDelete-PaidDetailSheet] - Vuốt Xóa Thông Báo, Modal Lịch Sử Thanh Toán & Điều Hướng Thông Minh
- **Status**: `[DONE]`
- **Goal**:
  1. Thêm cử chỉ vuốt từ phải sang trái (`SwipeToDismissBox`) để xóa thông báo kèm hiệu ứng nền đỏ và icon `Delete`.
  2. Bổ sung `PaidNotificationDetailSheet`: Khi chạm vào thẻ nhắc nhở đã thanh toán (`isPaid = true`), mở BottomSheet xem chi tiết lịch sử thanh toán (Tên, Số tiền, Danh mục, Ví, Thời gian, Ghi chú Sổ cái).
  3. Giữ nguyên chức năng chạm vào thân thẻ để đánh dấu đã đọc (`markAsRead`), đồng thời điều hướng chính xác theo loại thông báo (Ngân sách -> `budget`, Mục tiêu -> `goals`, Báo cáo -> `reports`, Nhắc nhở chưa trả -> mở `QuickPayBottomSheet`).
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/domain/repository/NotificationRepository.kt`
  - `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseNotificationRepository.kt`
  - `app/src/main/java/com/finlux/app/data/demo/DemoFinluxRepository.kt`
  - `app/src/main/java/com/finlux/app/presentation/notifications/NotificationsViewModel.kt`
  - `app/src/main/java/com/finlux/app/presentation/notifications/NotificationsScreen.kt`
  - `app/src/test/java/com/finlux/app/presentation/notifications/NotificationsViewModelTest.kt`
  - `app/src/test/java/com/finlux/app/presentation/home/HomeViewModelTest.kt`
  - `app/src/test/java/com/finlux/app/domain/usecase/TransactionUseCasesTest.kt`
  - `app/build.gradle.kts`
  - `CHANGELOG.md`
  - `HANDOVER_LOG.md`
- **Result**: `gradlew testDebugUnitTest` ➡️ **BUILD SUCCESSFUL (100% tests PASS)**. Build và nạp APK v1.10.9 (versionCode 121) thành công lên thiết bị Android.

### [Task-FEAT-Reminder-TimePicker-StandardControls-DebtSync] - Nâng Cấp Bộ Chọn Giờ Nhắc Nhở, 3 Form Control Tiêu Chuẩn & Đồng Bộ Nhắc Nợ
- **Status**: `[DONE]`
- **Goal**:
  1. Thêm bộ chọn Giờ/Phút (`TimePicker`) cho nhắc nhở định kỳ, kết hợp ngày + giờ chính xác từng phút theo múi giờ hệ thống `ZoneId.systemDefault()`.
  2. Thay thế toàn bộ control cũ trong `RemindersScreen.kt` bằng 3 Form Control Tiêu Chuẩn dùng chung: `FinluxCategoryPickerBottomSheet`, `FinluxWalletPickerBottomSheet` và `ErgonomicCompactAmountCard` (kèm dải chip `.000`).
  3. Đồng bộ ghi chú giao dịch khi bấm "Đã thanh toán" trên cả thanh thông báo hệ thống (`AlarmReminderScheduler.kt`) lẫn In-App thành chuẩn: `"Thanh toán: " + [Tên nhắc nhở]`.
  4. Bổ sung banner giải thích thời gian nhắc nợ trong `AddEditDebtSheet.kt` và tạo `SyncDebtReminderUseCase` tự động liên kết với `AlarmReminderScheduler` để bắn thông báo thực tế khi đến hạn.
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/data/local/reminder/AlarmReminderScheduler.kt`
  - `app/src/main/java/com/finlux/app/presentation/reminders/RemindersScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/debt/AddEditDebtSheet.kt`
  - `app/src/main/java/com/finlux/app/domain/usecase/SyncDebtReminderUseCase.kt`
  - `app/src/main/java/com/finlux/app/domain/usecase/SaveDebtAccountUseCase.kt`
  - `app/src/main/java/com/finlux/app/domain/usecase/DeleteDebtAccountUseCase.kt`
  - `app/src/test/java/com/finlux/app/domain/usecase/SyncDebtReminderUseCaseTest.kt`
  - `app/src/test/java/com/finlux/app/presentation/debt/DebtViewModelTest.kt`
  - `app/build.gradle.kts`
  - `CHANGELOG.md`
  - `HANDOVER_LOG.md`
- **Result**: `gradlew testDebugUnitTest` ➡️ **BUILD SUCCESSFUL (100% tests PASS)**. Build và nạp APK v1.10.8 (versionCode 120) thành công lên thiết bị Android.

### [Task-FEAT-Budget-History-Modal-LongPress-Edit] - Thêm Lịch Sử Chi Tiêu Danh Mục khi Chạm Thẻ & Nhấn Giữ để Sửa Ngân Sách
- **Status**: `[DONE]`
- **Goal**:
  1. Thay đổi hành vi tương tác trực quan trên thẻ ngân sách: **Nhấn 1 chạm (Single Tap)** mở BottomSheet xem chi tiết **Lịch sử các khoản giao dịch** thuộc danh mục đó trong kỳ; **Nhấn giữ (Long Press)** mở Modal **Sửa ngân sách**.
  2. Bổ sung luồng dữ liệu giao dịch chi tiêu (`state.transactions: List<FinanceTransaction>`) vào `BudgetUiState` & `BudgetViewModel.kt`.
  3. Thiết kế Modal Lịch sử chi tiêu: Thẻ tóm tắt tiến độ hạn mức (Icon + Tên danh mục + Thanh tiến độ + Tổng đã chi / Hạn mức) + Danh sách các khoản giao dịch chi tiết (`FinluxTransactionRow`) + Nút CTA "Chỉnh sửa ngân sách này".
  4. Bổ sung `onLongClick` (dùng `Modifier.combinedClickable`) cho `FinluxSoftCard` trong `FinluxCardComponents.kt`.
  5. Đồng bộ tính năng trên cả 3 giao diện: Prism (`PrismBudgetScreen.kt`), Classic (`ClassicBudgetScreen.kt`), Modern (`ModernBudgetScreen.kt`).
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/presentation/budget/BudgetViewModel.kt`
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxCardComponents.kt`
  - `app/src/main/java/com/finlux/app/presentation/budget/prism/PrismBudgetScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/budget/classic/ClassicBudgetScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/budget/modern/ModernBudgetScreen.kt`
  - `docs/FORM_COMPONENTS_SPEC.md`
- **Result**: `gradlew testDebugUnitTest` ➡️ **BUILD SUCCESSFUL (100% tests PASS)**. Build và nạp APK thành công lên thiết bị Android `192.168.17.153:41369`.

### [Task-UI-Compact-Sheet-Polish] - Tái Cấu Trúc UI/UX DebtPaymentSheet, QuickPayBottomSheet & Đồng Bộ Thẻ Thông Báo
- **Status**: `[DONE]`
- **Goal**: 
  1. Trích xuất `FinluxFormComponents.kt` (`ErgonomicFormRow`, `ErgonomicInputRow`, `PrincipalInterestSplitCard`, `FinluxWalletPickerBottomSheet`, `FinluxCategoryPickerBottomSheet`) làm component tiêu chuẩn dùng chung toàn dự án.
  2. Chuẩn hóa Bộ chọn danh mục (`FinluxCategoryPickerBottomSheet`): Grid 4 cột có thanh tìm kiếm, màu sắc icon động, badge checkmark khi chọn và nút tạo mới danh mục. Sử dụng đồng bộ cho "Thêm chi" và modal xác nhận thanh toán thông báo.
  3. Chuẩn hóa Bộ chọn ví tài khoản (`FinluxWalletPickerBottomSheet`): Dạng danh sách bo góc với icon tròn, tên ví, số dư khả dụng và checkmark chọn. Sử dụng đồng bộ cho "Thêm chi", "Thanh toán nợ" và modal xác nhận thanh toán thông báo.
  4. Tái cấu trúc `DebtPaymentSheet.kt`: Hero amount input 32sp + Quick Chips [Tối thiểu | 50% nợ | Tất toán hết] + Selector Row ví thu gọn + `PrincipalInterestSplitCard` phân tách Gốc/Lãi phẳng + `ErgonomicInputRow` cho Ghi chú.
  5. Đồng bộ style thẻ thông báo `NotificationItemCard` chuẩn Prism UI / Liquid Glass (commit `c4d17d9e151de0973998fc33b89be1113d2ad73a`), icon badge tròn sắc nét và nút [Thanh toán ngay] dạng Compact Glass Button.
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxFormComponents.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/AddTransactionSheet.kt`
  - `app/src/main/java/com/finlux/app/presentation/debt/DebtPaymentSheet.kt`
  - `app/src/main/java/com/finlux/app/presentation/notifications/NotificationsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/budget/prism/PrismBudgetScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/budget/classic/ClassicBudgetScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/budget/modern/ModernBudgetScreen.kt`
  - `docs/FORM_COMPONENTS_SPEC.md`
  - `docs/UI_SPEC.md`
- **Result**: `gradlew testDebugUnitTest` ➡️ **BUILD SUCCESSFUL (100% tests PASS)**. Build và nạp APK thành công lên thiết bị Android `192.168.17.153:41369`. Áp dụng `ErgonomicCompactAmountCard` cho ô nhập hạn mức ngân sách và tài liệu quy chuẩn `docs/FORM_COMPONENTS_SPEC.md`.

### [Task-BUGFIX-Notification-Budget-Sync] - Fix Budget Alert Key Mismatch & In-App Notification Center Sync
- **Status**: `[DONE]`
- **Goal**: 
  1. Đồng bộ key ID ngân sách `budgetRef` trong `FirebaseTransactionRepository.kt` theo đúng định dạng `"${catId}_month:${month}"` khớp 100% với Cloud Functions `reconcileBudget`.
  2. Bổ sung logic kiểm tra và phát cảnh báo ngân sách tức thì (In-App Local System Notification & `AppNotification` type `BUDGET_ALERT`) trong `AddTransactionUseCase.kt`.
  3. Khôi phục lưu `AppNotification` trong `AlarmReminderScheduler.kt` (`ReminderReceiver`) khi báo thức reo để hiển thị đầy đủ trong `NotificationsScreen`.
  4. Cập nhật `FinluxMessagingService.kt` để hiển thị System Notification khi nhận FCM Push từ Cloud Functions.
  5. Tạo `SystemNotificationHelper.kt` quản lý 3 channels (`finlux_reminders_v2`, `finlux_budget_alerts`, `finlux_system_notifications`).
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/data/local/notification/SystemNotificationHelper.kt`
  - `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseTransactionRepository.kt`
  - `app/src/main/java/com/finlux/app/domain/usecase/AddTransactionUseCase.kt`
  - `app/src/main/java/com/finlux/app/data/local/reminder/AlarmReminderScheduler.kt`
  - `app/src/main/java/com/finlux/app/data/remote/firebase/FinluxMessagingService.kt`
  - `app/src/test/java/com/finlux/app/domain/usecase/TransactionUseCasesTest.kt`
  - `app/src/test/java/com/finlux/app/data/remote/firebase/FirebaseTransactionRepositoryTest.kt`
- **Result**: `gradlew testDebugUnitTest` ➡️ **BUILD SUCCESSFUL (100% tests PASS)**. Build và Streamed Install APK thành công vào thiết bị Android `192.168.17.153:37989`.

### [Task-RELEASE-1.10.7] - Optimize Credential Manager Google Account Chooser
- **Status**: `[DONE]`
- **Goal**: Loại bỏ tùy chọn gây xung đột tài khoản ủy quyền `signInWithGoogleOption`, sử dụng chuẩn `GetGoogleIdOption(filterByAuthorizedAccounts = false)` để mở bottom sheet chọn bất kỳ tài khoản Google nào trên thiết bị.
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/presentation/auth/AuthViewModel.kt`
  - `app/build.gradle.kts`
  - `CHANGELOG.md`
- **Result**: `gradlew testDebugUnitTest` ➡️ **BUILD SUCCESSFUL (100% tests PASS)**.

### [Task-RELEASE-1.10.6] - Official Release Keystore & Google Sign-In CI Configuration
- **Status**: `[DONE]`
- **Goal**: Cấu hình chuẩn hóa Production Release Keystore cho CI/CD GitHub Actions, đăng ký SHA-1/SHA-256 fingerprint đồng bộ với Firebase Console & Google Cloud OAuth 2.0 Client IDs, tối ưu hóa fallback an toàn trong `release.yml`.
- **Files Modified**:
  - `app/build.gradle.kts`
  - `CHANGELOG.md`
  - `.github/workflows/release.yml`
  - `.gitignore`
- **Result**: `gradlew testDebugUnitTest` ➡️ **BUILD SUCCESSFUL (100% tests PASS)**. Phiên bản v1.10.6 sẵn sàng phát hành.

### [Task-HOTFIX-GoogleSignIn] - Fix Google Sign-In with Credential Manager & Fallbacks
- **Status**: `[DONE]`
- **Goal**: Khắc phục lỗi không đăng nhập được Google bằng cách unwrap Activity context cho CredentialManager, hỗ trợ song song `GetSignInWithGoogleOption` và `GetGoogleIdOption`, xử lý timeout 20s và chuẩn hóa thông báo lỗi.
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/presentation/auth/AuthViewModel.kt`
- **Result**: `gradlew testDebugUnitTest` ➡️ **BUILD SUCCESSFUL (100% tests PASS)**. Đăng nhập Google hoạt động ổn định trên cả tài khoản mới và tài khoản đã liên kết.

### [Task-UI-IncomeScreen-Style-Sync] - Đồng Bộ Giao Diện & Thẻ Thu Nhập Chuẩn FinluxPanel Khớp Chi Tiêu
- **Status**: `[DONE]`
- **Goal**: Cập nhật toàn bộ thẻ và component trong `IncomeScreen.kt` (MonthPicker, IncomeHero, 4 Statistic Cards, Theo danh mục, Danh sách thu nhập) sử dụng `FinluxPanel` với đường viền mỏng và đổ bóng (border & shadow elevation 5dp) đồng bộ 100% với giao diện màn hình `ExpenseScreen.kt`.
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/presentation/income/IncomeScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/income/IncomeViewModel.kt`
- **Result**: `gradlew testDebugUnitTest` ➡️ **BUILD SUCCESSFUL (100% tests PASS)**. Giao diện màn hình Thu nhập đã đồng bộ hoàn hảo phong cách thẻ nổi (border & shadow) với màn hình Chi tiêu.

### [Task-HOTFIX-CI] - Fix GitHub Actions CI & Chuẩn Bị Build Release
- **Status**: `[DONE]`
- **Goal**: Resolve CI failures caused by JDK version mismatch in `firebase-tools` and prepare release build.
- **Files Modified**:
  - `.github/workflows/firebase-ci.yml`: Updated `java-version` from `17` to `21`.
  - `firebase.json`: Thêm cấu hình `emulators` block để Firebase Emulator có thể khởi chạy `firestore` ở port 8080.
- **Result**: CI đã sẵn sàng chạy. Bản release sẽ tự động build trên GitHub Actions khi release keystore secrets được cấu hình.


### [Task-HOTFIX-BudgetViewModel] - Fix Unit Tests and Upgrade Version
- **Status**: `[DONE]`
- **Goal**: Resolve test failures in `BudgetViewModelTest` and bump version for release.
- **Files Modified**:
  - `app/src/test/java/com/finlux/app/presentation/budget/BudgetViewModelTest.kt`: Fixed mocked methods, setup test dispatcher properly to resolve state issues.
  - `app/build.gradle.kts`: Bumped version to 1.10.4.
  - `CHANGELOG.md`: Added release notes.
- **Result**: All tests pass. Version updated.

### [Task-HOTFIX-P0] - Hardening Sprint Phase 2 (HOTFIX-04 -> 06)
- **Status**: `[DONE]`
- **Goal**: Implement Reminder Single Source of Truth, Payment Action Idempotency, and Firebase Emulator tests for Firestore Rules.
- **Files Modified**:
  - `app/src/main/java/com/finlux/app/data/local/reminder/AlarmReminderScheduler.kt`: Removed local `nextTriggerDate` and `notifications` Firestore updates, inject `paymentActionId`.
  - `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseTransactionRepository.kt`: Enhanced `addWithBalanceUpdate` to support explicit transaction ID for idempotency check.
  - `.github/workflows/firebase-ci.yml`: Added GitHub action for emulator testing.
  - `functions/test/firestore.rules.test.ts`: Added mocha tests for Firestore Rules (`balance`, `salaryRollovers`, `budget.spentAmount`).
  - `functions/package.json`: Added `mocha` and `test` script.
- **Result**: Successfully enforced atomic properties for reminder payments and created CI coverage for critical rules. Version bumped to 1.10.3.

### [Task-HOTFIX-P0] - Hardening Sprint Phase 1 (HOTFIX-01 -> 03)
Hoàn thành Hardening Sprint P0 (P0.1 -> P0.7). Đã phát hành Release v1.10.2.

## [DONE] Task: [P0.6 & P0.7] Reminder Idempotency & Action "Đã thanh toán" Verification

**Ngày hoàn thành:** 2026-08-25
**Nhánh git:** `main`
**Mục tiêu & Kết quả thực hiện:**
- **Mục tiêu:**
  - Thống nhất cơ chế Reminder qua `AlarmReminderScheduler` (chính xác từng giây).
  - Tích hợp `ReminderTriggerDeduplicator` chống duplicate trigger trong khoảng thời gian ngắn (< 60s), ngăn chặn hoàn toàn hiện tượng bắn nhiều thông báo lặp lại cho cùng một trigger window.
  - Tự động cập nhật `nextTriggerDate` của chu kỳ kế tiếp vào Database (`reminderRepository.upsertReminder`) khi kích hoạt thông báo nhắc nhở định kỳ.
  - Khắc phục lỗi ở Action "Đã thanh toán" (`ACTION_PAY`): Bắt buộc kiểm tra `AppResult.Success` từ `addTransactionUseCase` trước khi gọi `markAsPaidByReminderId`. Nếu ví không đủ tiền hoặc giao dịch thất bại, không đánh dấu đã thanh toán sai lệch.
- **Kết quả kiểm thử:**
  - `gradlew testDebugUnitTest`: **170/170 PASS (100%)**.
- **Danh sách file chỉnh sửa:**
  - `app/src/main/java/com/finlux/app/data/local/reminder/AlarmReminderScheduler.kt`
  - `app/src/test/java/com/finlux/app/data/local/reminder/ReminderTriggerDeduplicatorTest.kt`
- **Trạng thái:** `[DONE]`

## [DONE] Task: [P0.5] Thực Thi Salary Rollover / Move To Savings (Idempotent Business Command)

**Ngày hoàn thành:** 2026-08-25
**Nhánh git:** `main`
**Mục tiêu & Kết quả thực hiện:**
- **Mục tiêu:**
  - Thiết kế `ExecuteSalaryRolloverUseCase` chuyển giao toàn bộ số dư còn lại của `salaryWallet` sang `savingsWallet` (`MOVE_TO_SAVINGS`) khi kết thúc chu kỳ lương.
  - Ghi nhận lịch sử giao dịch rõ ràng vào Sổ Cái (Ledger Transfer) với ghi chú gắn với `cycleKey`.
  - Đảm bảo tính Idempotent tuyệt đối: Lưu trữ trạng thái xử lý rollover theo `cycleKey` vào Firestore (`users/{uid}/salaryRollovers/{docId}`) và Demo Repository, đảm bảo chạy nhiều lần không bao giờ duplicate transfer hoặc duplicate ledger entries.
  - Xử lý các edge cases: số dư ví lương <= 0, chọn trùng ví, cấu hình `KEEP_IN_WALLET` (skip không sinh transaction thừa).
- **Kết quả kiểm thử:**
  - `gradlew testDebugUnitTest`: **167/167 PASS (100%)**.
- **Danh sách file chỉnh sửa:**
  - `app/src/main/java/com/finlux/app/domain/repository/SalaryCycleRepository.kt`
  - `app/src/main/java/com/finlux/app/data/demo/DemoSalaryCycleRepository.kt`
  - `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseSalaryCycleRepository.kt`
  - `app/src/main/java/com/finlux/app/domain/usecase/ExecuteSalaryRolloverUseCase.kt`
  - `app/src/test/java/com/finlux/app/domain/usecase/ExecuteSalaryRolloverUseCaseTest.kt`
  - `app/src/test/java/com/finlux/app/presentation/home/HomeViewModelTest.kt`
  - `app/src/test/java/com/finlux/app/presentation/settings/salary/SalaryCycleViewModelTest.kt`
- **Trạng thái:** `[DONE]`

## [DONE] Task: [P0.4] Hoàn Thiện Salary Cycle Thành Financial Period Engine (`FinancialPeriodResolver`)

**Ngày hoàn thành:** 2026-08-25
**Nhánh git:** `main`
**Mục tiêu & Kết quả thực hiện:**
- **Mục tiêu:**
  - Xây dựng abstraction thống nhất `FinancialPeriod` và `FinancialPeriodResolver` kết nối đồng bộ giữa `CALENDAR_MONTH` và `SALARY_CYCLE`.
  - Hỗ trợ đầy đủ các API: `resolveCurrentPeriod`, `resolvePreviousPeriod`, `resolvePeriodContaining`, `resolvePeriodKey`.
  - Xử lý triệt để toàn bộ boundary cases của Payday: các ngày 28/29/30/31, năm nhuận/không nhuận tháng 2, quy tắc `FIRST_DAY_OF_MONTH`, `LAST_DAY_OF_MONTH` và Timezone configuration.
  - Cung cấp kiến trúc nền tảng dùng chung cho Home, Dashboard, Reports, Budgets và Previous-period comparison.
- **Kết quả kiểm thử:**
  - `gradlew testDebugUnitTest`: **162/162 PASS (100%)**.
- **Danh sách file chỉnh sửa:**
  - `app/src/main/java/com/finlux/app/domain/model/SalaryCycleModels.kt`
  - `app/src/main/java/com/finlux/app/domain/usecase/FinancialPeriodResolver.kt`
  - `app/src/main/java/com/finlux/app/data/di/RepositoryModule.kt`
  - `app/src/test/java/com/finlux/app/domain/usecase/FinancialPeriodResolverTest.kt`
- **Trạng thái:** `[DONE]`

## [DONE] Task: [P0.3] Bảo Vệ Số Dư Ví Bằng Ledger & Chặn Set Balance Trực Tiếp

**Ngày hoàn thành:** 2026-08-25
**Nhánh git:** `main`
**Mục tiêu & Kết quả thực hiện:**
- **Mục tiêu:**
  - Tách biệt hoàn toàn metadata ví (`name`, `type`, `color`, `isDefault`, `status`, `archivedAt`) và Financial State (`balance`).
  - Chặn việc chỉnh sửa số dư ví trực tiếp qua client: Khi cập nhật ví đã tồn tại (`upsertWallet`), chỉ update metadata, không bao giờ ghi đè `balance`.
  - Xây dựng `AdjustWalletBalanceUseCase`: Mọi thao tác điều chỉnh số dư ví đều sinh ra giao dịch tương ứng (`INCOME` / `EXPENSE` category `balance_adjustment`) ghi vào Sổ Cái (Transaction Ledger) với đầy đủ audit trail.
  - Chuyển `deleteWallet` sang cơ chế Soft Delete / Archiving (`status = "archived"`, `archivedAt = Instant.now()`) nếu ví đã có lịch sử giao dịch, bảo toàn tính toàn vẹn dữ liệu tài chính lịch sử.
- **Kết quả kiểm thử:**
  - `gradlew testDebugUnitTest`: **156/156 PASS (100%)**.
- **Danh sách file chỉnh sửa:**
  - `app/src/main/java/com/finlux/app/domain/model/FinanceModels.kt`
  - `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseWalletRepository.kt`
  - `app/src/main/java/com/finlux/app/data/demo/DemoFinluxRepository.kt`
  - `app/src/main/java/com/finlux/app/domain/usecase/AdjustWalletBalanceUseCase.kt`
  - `app/src/test/java/com/finlux/app/domain/usecase/AdjustWalletBalanceUseCaseTest.kt`
- **Trạng thái:** `[DONE]`

## [DONE] Task: [P0.2] Chặn Ví Âm Ngay Trong Firestore & Repository Atomic Transactions

**Ngày hoàn thành:** 2026-08-25
**Nhánh git:** `main`
**Mục tiêu & Kết quả thực hiện:**
- **Mục tiêu:**
  - Bảo vệ bất biến số dư ví (Financial Invariant) ở cấp độ Atomic Transaction: Ví không phải thẻ tín dụng (`WalletType.CARD`) tuyệt đối không bao giờ được âm số dư (`balance >= 0`).
  - Áp dụng kiểm tra `finalBalance` ngay bên trong `runTransaction` của `FirebaseTransactionRepository` và mutex lock của `DemoFinluxRepository` cho toàn bộ các thao tác: `addWithBalanceUpdate`, `editWithBalanceUpdate`, `deleteWithBalanceUpdate` (hoàn tác thu nhập), `transferBetweenWallets`, `processPayment`.
  - Không dựa vào validation UI/ViewModel để bảo vệ dữ liệu chống race conditions hoặc can thiệp client.
  - Viết suite kiểm thử unit tests toàn diện: kiểm thử chi tiêu vượt quá số dư, sửa giao dịch làm âm ví, chuyển ví không đủ tiền, hoàn tác thu nhập làm âm ví, và kiểm thử concurrent mutations.
- **Kết quả kiểm thử:**
  - `gradlew testDebugUnitTest`: **153/153 PASS (100%)**.
- **Danh sách file chỉnh sửa:**
  - `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseTransactionRepository.kt`
  - `app/src/main/java/com/finlux/app/data/demo/DemoFinluxRepository.kt`
  - `app/src/test/java/com/finlux/app/data/remote/firebase/FirebaseTransactionRepositoryTest.kt`
  - `app/src/test/java/com/finlux/app/data/demo/DemoTransactionInvariantTest.kt`
- **Trạng thái:** `[DONE]`

## [DONE] Task: [P0.1] Harden Release Signing & Fail-Closed OTA Updater

**Ngày hoàn thành:** 2026-08-25
**Nhánh git:** `main`
**Mục tiêu & Kết quả thực hiện:**
- **Mục tiêu:**
  - Audit chữ ký bảo mật của bản Release hiện tại bằng `apksigner` (SHA-1: `eaa9eaabb7b99a1ff18164bf762ee175c5327f47`, SHA-256: `4ca0dab2a3d4947db40889d211a81303ab7705fd5ba0f587f7d8d41d0a769989`).
  - Đối chiếu 100% khớp với cấu hình Firebase OAuth Android Client để không phá vỡ Google Sign-In, Firebase Auth và update APK.
  - Xóa bỏ hoàn toàn hardcoded embedded release keystore và fallback password "android" khỏi `.github/workflows/release.yml`.
  - Thiết lập cơ chế Fail-Fast trên Release Pipeline: Bắt buộc đủ 4 GitHub Secrets (`FINLUX_KEYSTORE_BASE64`, `FINLUX_KEYSTORE_PASSWORD`, `FINLUX_KEY_ALIAS`, `FINLUX_KEY_PASSWORD`), thiếu bất kỳ secret nào pipeline sẽ fail ngay.
  - Nâng cấp `AppUpdateManager`: Áp dụng cơ chế Fail-Closed (chặn update nếu chữ ký chứng chỉ rỗng hoặc không xác minh được).
- **Kết quả kiểm thử:**
  - `gradlew testDebugUnitTest`: **100% PASS**.
  - `gradlew lintDebug`: **100% PASS** (0 errors).
- **Danh sách file chỉnh sửa:**
  - `.github/workflows/release.yml`
  - `app/src/main/java/com/finlux/app/core/updater/AppUpdateManager.kt`
- **Trạng thái:** `[DONE]`

## [DONE] Task: Đóng Gói & Phát Hành Phiên Bản Release v1.10.1 (versionCode 113)

**Ngày hoàn thành:** 2026-08-25
**Nhánh git:** `main`
**Mục tiêu & Kết quả thực hiện:**
- **Mục tiêu:**
  - Hợp nhất toàn bộ thay đổi từ PR #12 vào `main`: Lịch sử thanh toán nợ (`DebtPaymentHistorySheet`), Cài đặt nhắc nợ đến hạn (`AddEditDebtSheet`), Bảo vệ số dư ví (`Insufficient Balance Protection`), Fix Ghost Alarm trong `AlarmReminderScheduler`.
  - Nâng cấp `versionCode = 113`, `versionName = "1.10.1"`.
  - Chạy toàn bộ test suites `gradlew testDebugUnitTest` đảm bảo 100% PASS.
  - Đóng gói APK Release với R8/Proguard optimization.
  - Cài đặt APK trực tiếp lên thiết bị Android qua ADB và đẩy tag release lên Git.
- **Kết quả thực hiện:**
  - `versionCode` nâng lên 113, `versionName` nâng lên 1.10.1 trong `app/build.gradle.kts`.
  - Cập nhật `CHANGELOG.md` cho phiên bản `[1.10.1] - 2026-08-25`.
  - Kiểm thử `gradlew testDebugUnitTest`: **100% tests PASS**.
  - Đóng gói `gradlew assembleRelease`: **BUILD SUCCESSFUL**.
  - Nạp thành công vào thiết bị thật `7f4ca06a` qua ADB.
- **Trạng thái:** `[DONE]`

## [DONE] Task: Triển Khai Tính Năng Tháng Tài Chính / Chu Kỳ Lương (Salary Cycle & Financial Month)

**Ngày hoàn thành:** 2026-08-24
**Nhánh git:** `main`
**Mục tiêu & Kết quả thực hiện:**
- **Mục tiêu:**
  - Xây dựng tầng Domain: `SalaryCycleModels`, `SalaryCycleCalculator`, `ValidateSalaryCycleConfigUseCase`, `SalaryCycleUseCases`.
  - Xây dựng tầng Data: `SalaryCycleRepository`, `FirebaseSalaryCycleRepository`, `DemoSalaryCycleRepository`, `TransactionRangeRepository`, Firestore rules.
  - Xây dựng tầng UI: `SalaryCycleSettingsSheet`, `SalaryCycleViewModel`, Live Preview dải chu kỳ, Tích hợp Trang chủ (`PrismHomeScreen`), Báo cáo (`ReportsViewModel`, `PrismReportsScreen`, `ClassicReportsScreen`, `ModernReportsScreen`).
  - Kiểm thử unit test tự động và đóng gói nạp máy.
- **Kết quả kiểm thử:**
  - `gradlew testDebugUnitTest`: **136/136 tests PASS** (100% thành công).
  - `gradlew assembleDebug`: **BUILD SUCCESSFUL**.
- **Danh sách file đã tạo và chỉnh sửa:**
  - `app/src/main/java/com/finlux/app/core/time/FinanceTime.kt`
  - `app/src/main/java/com/finlux/app/domain/model/SalaryCycleModels.kt`
  - `app/src/main/java/com/finlux/app/domain/usecase/SalaryCycleCalculator.kt`
  - `app/src/main/java/com/finlux/app/domain/usecase/ValidateSalaryCycleConfigUseCase.kt`
  - `app/src/main/java/com/finlux/app/domain/usecase/SalaryCycleUseCases.kt`
  - `app/src/main/java/com/finlux/app/domain/repository/SalaryCycleRepository.kt`
  - `app/src/main/java/com/finlux/app/domain/repository/TransactionRangeRepository.kt`
  - `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseSalaryCycleRepository.kt`
  - `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseSalaryCycleMapper.kt`
  - `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseTransactionRangeRepository.kt`
  - `app/src/main/java/com/finlux/app/data/demo/DemoSalaryCycleRepository.kt`
  - `app/src/main/java/com/finlux/app/data/demo/DemoTransactionRangeRepository.kt`
  - `app/src/main/java/com/finlux/app/data/di/RepositoryModule.kt`
  - `app/src/main/java/com/finlux/app/presentation/settings/salary/SalaryCycleSettingsSheet.kt`
  - `app/src/main/java/com/finlux/app/presentation/settings/salary/SalaryCycleViewModel.kt`
  - `app/src/main/java/com/finlux/app/presentation/settings/SettingsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/settings/prism/PrismSettingsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/reports/ReportQueryWindowResolver.kt`
  - `app/src/main/java/com/finlux/app/presentation/reports/ReportsViewModel.kt`
  - `app/src/main/java/com/finlux/app/presentation/reports/prism/PrismReportsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/reports/classic/ClassicReportsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/reports/modern/ModernReportsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/home/HomeViewModel.kt`
  - `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`
  - `app/src/test/java/com/finlux/app/domain/usecase/SalaryCycleCalculatorTest.kt`
  - `app/src/test/java/com/finlux/app/presentation/reports/ReportQueryWindowResolverTest.kt`
  - `app/src/test/java/com/finlux/app/presentation/settings/salary/SalaryCycleViewModelTest.kt`
  - `app/src/test/java/com/finlux/app/presentation/home/HomeViewModelTest.kt`
  - `firestore.rules`
  - `docs/BA_SPEC.md`
  - `docs/DATA_SPEC.md`
  - `docs/UI_SPEC.md`
- **Trạng thái:** `[DONE]`

## [DONE] Task: Đóng Gói & Phát Hành Phiên Bản Release v1.9.3 (versionCode 111)

**Ngày hoàn thành:** 2026-08-24
**Nhánh git:** `main`
**Mục tiêu & Kết quả thực hiện:**
- **Mục tiêu:**
  - Nâng cấp `versionCode = 111`, `versionName = "1.9.3"`.
  - Chạy toàn bộ test suites `gradlew testDebugUnitTest` đảm bảo 100% PASS.
  - Đóng gói APK Release với R8/Proguard optimization.
  - Nạp bản release APK vào thiết bị Android thật và đẩy tag release lên Git.
- **Kết quả thực hiện:**
  - `versionCode` nâng lên 111, `versionName` nâng lên 1.9.3 trong `app/build.gradle.kts`.
  - Cập nhật `CHANGELOG.md` cho phiên bản `[1.9.3] - 2026-08-24`.
  - Kiểm thử `gradlew testDebugUnitTest`: **100% tests PASS** (34 tasks executed/up-to-date, BUILD SUCCESSFUL).
  - Đóng gói `gradlew assembleRelease`: **BUILD SUCCESSFUL**, sinh ra file `app-release.apk` (6.74 MB).
  - Nạp thành công vào thiết bị thật qua ADB (`Performing Streamed Install -> Success`).
- **Danh sách file đã chỉnh sửa:**
  - `app/build.gradle.kts`
  - `CHANGELOG.md`
  - `HANDOVER_LOG.md`
- **Trạng thái:** `[DONE]`

## [DONE] Task: Fix Triệt Để Lỗi Ghost Alarm / Nhắc Nhở Đã Xóa Vẫn Tự Bắn Thông Báo

**Ngày hoàn thành:** 2026-08-24
**Nhánh git:** `main`
**Mục tiêu & Kết quả thực hiện:**
- **Mục tiêu:**
  1. Fix lỗi `ReminderReceiver` tự động bắn thông báo và tự động lên lịch lặp lại cho các nhắc nhở đã bị xóa hoặc đã tắt trong database.
  2. Bổ sung validation guard truy vấn `ReminderRepository` trước khi kích hoạt notification/lên lịch `nextTrigger`.
  3. Cập nhật `AlarmReminderScheduler.cancel()` đảm bảo hủy sạch `PendingIntent` và notification trên Android OS.
- **Danh sách file đã chỉnh sửa & tạo mới:**
  - `app/src/main/java/com/finlux/app/data/local/reminder/AlarmReminderScheduler.kt` (Inject ReminderRepository, validate active reminder, dọn dẹp orphan alarms)
  - `app/src/test/java/com/finlux/app/domain/usecase/ReminderUseCasesTest.kt` (Bổ sung unit tests cho SaveReminderUseCase & DeleteReminderUseCase)
- **Kết quả kiểm thử:**
  - `.\gradlew testDebugUnitTest`: **100% PASS** (34 tasks executed, 0 fail).
  - `.\gradlew assembleDebug`: **BUILD SUCCESSFUL** (`app-debug.apk` ~31.7MB).
- **Trạng thái:** `[DONE]`

## [DONE] Task: Xử Lý Nhóm Nghiệp Vụ Cốt Lõi & Bug Critical (Chặn Chi Vượt Số Dư Ví, Lịch Sử Trả Nợ & Nhắc Nợ Đến Hạn)

**Ngày hoàn thành:** 2026-08-24
**Nhánh git:** `main`
**Mục tiêu & Kết quả thực hiện:**
- **Mục tiêu:**
  1. [BUG CRITICAL] Chặn tạo chi tiêu khi số dư ví thanh toán <= 0 hoặc không đủ tiền (áp dụng cho ví không phải Thẻ tín dụng) cả ở tầng Domain (`AddTransactionUseCase`, `EditTransactionUseCase`) lẫn UI (`AddTransactionSheet`).
  2. [FEATURE] Xây dựng `DebtPaymentHistorySheet` xem lịch sử trả nợ (thống kê tổng tiền, gốc, lãi, lọc theo từng khoản nợ hoặc xem tất cả).
  3. [FEATURE] Tích hợp cài đặt nhắc nợ đến hạn (`isReminderEnabled`, `reminderDaysBefore`) trong `AddEditDebtSheet`, gắn icon Lịch sử lên TopBar và DebtCard.
- **Danh sách file đã chỉnh sửa & tạo mới:**
  - `app/src/main/java/com/finlux/app/domain/usecase/AddTransactionUseCase.kt` (Inject WalletRepository, kiểm tra số dư ví khả dụng)
  - `app/src/main/java/com/finlux/app/domain/usecase/EditTransactionUseCase.kt` (Kiểm tra số dư khả dụng có tính khoản hoàn trả giao dịch cũ)
  - `app/src/main/java/com/finlux/app/presentation/transaction/AddTransactionSheet.kt` (Banner cảnh báo đỏ số dư ví không đủ + khóa nút Lưu)
  - `app/src/main/java/com/finlux/app/domain/model/DebtModels.kt` (Thêm isReminderEnabled, reminderDaysBefore vào DebtAccount)
  - `app/src/main/java/com/finlux/app/domain/model/NotificationType.kt` (Thêm DEBT_DUE_ALERT)
  - `app/src/main/java/com/finlux/app/domain/repository/DebtRepository.kt` (Thêm observeAllPaymentHistory)
  - `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseDebtRepository.kt` (Triển khai observeAllPaymentHistory, map reminder fields)
  - `app/src/main/java/com/finlux/app/data/demo/DemoFinluxRepository.kt` (Triển khai observeAllPaymentHistory, seedPaymentHistory)
  - `app/src/main/java/com/finlux/app/domain/usecase/GetDebtPaymentHistoryUseCase.kt` (UseCase lấy lịch sử trả nợ linh hoạt)
  - `app/src/main/java/com/finlux/app/presentation/debt/components/DebtPaymentHistorySheet.kt` (UI Liquid Glass xem lịch sử trả nợ, thống kê gốc/lãi, filter chips)
  - `app/src/main/java/com/finlux/app/presentation/debt/AddEditDebtSheet.kt` (Cài đặt switch nhắc nợ đến hạn + chọn số ngày nhắc trước)
  - `app/src/main/java/com/finlux/app/presentation/debt/DebtDashboardScreen.kt` (Nút Lịch sử trên TopBar, kết nối DebtPaymentHistorySheet)
  - `app/src/main/java/com/finlux/app/presentation/debt/components/DebtCard.kt` (Nút Lịch sử trả nợ nhanh trên từng thẻ nợ)
  - `app/src/main/java/com/finlux/app/presentation/debt/DebtViewModel.kt` & `DebtUiState.kt` (Cung cấp paymentHistory flow)
  - `app/src/main/java/com/finlux/app/presentation/notifications/NotificationsScreen.kt` (Xử lý icon/màu cho DEBT_DUE_ALERT)
  - `app/src/test/java/com/finlux/app/domain/usecase/TransactionUseCasesTest.kt` (Bổ sung test chặn chi vượt số dư ví)
  - `app/src/test/java/com/finlux/app/domain/usecase/GetDebtPaymentHistoryUseCaseTest.kt` (Unit test cho GetDebtPaymentHistoryUseCase)
  - `app/src/test/java/com/finlux/app/domain/usecase/ProcessDebtPaymentUseCaseTest.kt` (Cập nhật mock repo)
  - `app/src/test/java/com/finlux/app/presentation/debt/DebtViewModelTest.kt` (Cập nhật mock repo & usecase)
  - `app/src/test/java/com/finlux/app/presentation/home/HomeViewModelTest.kt` (Cập nhật mock repo)
- **Kết quả kiểm thử:**
  - `.\gradlew testDebugUnitTest`: **100% PASS** (34 tasks, 0 fail).
  - `.\gradlew assembleDebug`: **BUILD SUCCESSFUL** (`app-debug.apk` ~31.7MB).
- **Trạng thái:** `[DONE]`

## [DONE] Task: Đồng Bộ Giao Diện Thêm/Sửa Ví Của Phong Cách Prism Giống Chuẩn Liquid Glass Cổ Điển

**Ngày hoàn thành:** 2026-08-24
**Nhánh git:** `main`
**Mục tiêu & Kết quả thực hiện:**
- **Mục tiêu:**
  - Nâng cấp BottomSheet Thêm ví mới / Chỉnh sửa ví trong `PrismWalletsScreen.kt` để đồng bộ 100% đầy đủ tính năng và thẩm mỹ giống với Liquid Glass Cổ điển:
    1. Header: Tiêu đề + Subtitle "Quản lý tài khoản và dòng tiền tập trung" + Icon ví tròn linh động màu sắc bên phải.
    2. Ô nhập Tên ví / ngân hàng.
    3. Bộ chọn Loại tài khoản đầy đủ các loại ví (`WalletType.entries`).
    4. Ô nhập Số dư ban đầu / Số dư hiện tại với live format VND + Dải chip cộng tiền nhanh (+500K, +1tr, +2tr, +5tr, +10tr).
    5. Bộ chọn Màu thẻ (8 màu sắc trong `FinanceAccentHexes`).
    6. Switch Đặt làm ví mặc định với icon ngôi sao.
    7. Nút Tạo ví mới / Lưu thay đổi full-width 52dp và tùy chọn xóa ví khi chỉnh sửa.
- **Kết quả thực hiện:**
  - Đã triển khai `PrismWalletEditor` chuẩn `GlassBottomSheet` và đồng bộ hoàn toàn với giao diện Liquid Glass Cổ điển.
  - Tích hợp đầy đủ các tính năng: live format tiền tệ, quick chips cộng tiền, chọn màu thẻ, đặt ví mặc định, kiểm soát xóa ví an toàn.
  - **Kiểm thử & Nạp máy:**
    - Chạy `gradlew testDebugUnitTest`: **100% tests PASS**.
    - Đóng gói `gradlew assembleDebug` và nạp thành công lên thiết bị Android qua ADB (`Performing Streamed Install -> Success`).
- **Danh sách file đã chỉnh sửa:**
  - `app/src/main/java/com/finlux/app/presentation/wallet/prism/PrismWalletsScreen.kt`
  - `HANDOVER_LOG.md`
- **Trạng thái:** `[DONE]`

## [DONE] Task: Tối Ưu Thẻ Hero Trang Chủ (Hiển Thị Số Dư Ví Mặc Định + Vuốt Sang Tài Sản Ròng/Nợ) & Chuẩn Hóa Trend Chi Tháng Này

**Ngày hoàn thành:** 2026-08-24
**Nhánh git:** `main`
**Mục tiêu & Kết quả thực hiện:**
- **Mục tiêu:**
  1. Tối ưu thẻ Hero trang chủ (`PrismHeroNetWorthCard`): Mặc định hiển thị "Số dư hiện có" (Tổng số dư các ví khả dụng) thay vì trừ nợ trực tiếp ra số âm; hỗ trợ thao tác vuốt sang trái (HorizontalPager) với 2 dots indicator để xem chi tiết "Tài sản ròng (Net Worth)" và nợ.
  2. Sửa lỗi nhãn trend "%" trong `PrismSummaryTrioCard`: Khi không có phát sinh chi (chi tháng này = 0), hiển thị `— 0%` thay vì hiển thị số cứng `▲ 18,7%`. Đồng bộ trạng thái trung tính khi thu/chi/dòng tiền bằng 0.
- **Kết quả thực hiện:**
  - **PrismHeroNetWorthCard:** Chuyển đổi thành `HorizontalPager` 2 trang mượt mà kèm Page Indicator dots:
    - **Trang 0 (Mặc định khi vào app):** Hiển thị "Số dư hiện có" với tổng tiền các ví khả dụng (`grossAssets`), không trừ nợ làm âm số dư; phụ đề sạch gọn `"Tổng số dư từ tất cả các ví"` (đã bỏ dòng chữ gợi ý thừa).
    - **Trang 1 (Khi vuốt sang trái):** Hiển thị "Tài sản ròng (Net Worth)" (`netWorth` = Tổng ví - Tổng nợ) kèm các chip định tuyến nhanh sang Ví và Nợ.
  - **FinluxNavHost.kt (Fix cướp cử chỉ vuốt):**
    - Sửa `mainSwipeModifier`: chuyển sang `pass = PointerEventPass.Main` và kiểm tra `if (change.isConsumed) break`. Nhờ đó, các thao tác vuốt ngang trên `HorizontalPager` (thẻ Hero, biểu đồ tròn...) được xử lý trơn tru, không còn bị cướp touch nhảy nhầm sang màn hình Lịch sử thu chi.
  - **PrismSummaryTrioCard:**
    - Cập nhật logic trend text động: Khi Chi tháng này = 0 đ, hiển thị `— 0%` với màu trung tính, không còn hiển thị cứng `▲ 18,7%`.
    - Đồng bộ logic hiển thị cho Thu tháng này (`— 0%` khi = 0) và Dòng tiền (ròng) (`— 0%` khi = 0).
  - **Kiểm thử & Nạp máy:**
    - Chạy `gradlew testDebugUnitTest`: **100% tests PASS**.
    - Đóng gói `gradlew assembleDebug` và nạp thành công lên thiết bị Android qua ADB (`Performing Streamed Install -> Success`).
- **Danh sách file đã chỉnh sửa:**
  - `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`
  - `app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt`
  - `HANDOVER_LOG.md`
- **Trạng thái:** `[DONE]`

## [DONE] Task: Tái Cấu Trúc UI/UX Bento Grid & Liquid Glass Cho Module Quản Lý Nợ

**Ngày hoàn thành:** 2026-08-24
**Nhánh git:** `main`
**Mục tiêu & Kết quả thực hiện:**
- **Mục tiêu:** Tái cấu trúc giao diện Quản lý Nợ & Tín dụng theo phong cách Bento Grid & Liquid Glass cao cấp: fix lỗi ép chữ dọc trên nhãn APR, hợp nhất khối Chiến lược + Trợ lý AI + Slider + Burndown Chart thành khối Bento liền mạch, thiết kế lại `DebtCard` chuẩn Fintech hiện đại (Glass Action Button góc phải) và loại bỏ nút FAB (+) che khuất màn hình.
- **Kết quả thực hiện:**
  1. **CashflowAdvisorCard.kt:**
     - Tái cấu trúc Header: di chuyển nhãn `APR TB: XX%` thành Horizontal Badge đặt ngay cạnh tiêu đề "Trợ Lý Dòng Tiền AI" với `softWrap = false` và `maxLines = 1`, loại bỏ triệt để lỗi ép chữ rớt dòng theo chiều dọc.
     - Tối ưu kích thước 3 Metric Pills (Thu nhập TB, Chi thiết yếu, Dòng tiền FCF) và 3 chip kịch bản trả nợ 1-Touch.
  2. **StrategySelectorCard.kt & DebtBurndownChart.kt:**
     - Hợp nhất toàn bộ khối Chiến lược thoát nợ, Trợ lý dòng tiền AI, Thanh trượt trả thêm và Biểu đồ Burndown Chart (`EmbeddedDebtBurndownChart`) thành một Khối Bento Payoff Container liền mạch, giảm thiểu tối đa độ dài cuộn trang (scroll fatigue).
  3. **DebtCard.kt:**
     - Loại bỏ khối nút bấm full-width to bản màu sắc chói mắt ở đáy thẻ.
     - Thiết kế lại thẻ chuẩn Fintech gọn gàng: Header gồm Icon loại nợ + Tên khoản vay + APR% + Glass Action Button `[💳 Trả nợ]` bo tròn nhỏ gọn ở góc phải; Body hiển thị Dư nợ to rõ / Gốc ban đầu + Thanh tiến độ mỏng 4.5dp; Footer hiển thị % đã trả, mức trả tối thiểu và badge hạn ngày / quá hạn.
  4. **DebtDashboardScreen.kt:**
     - Xóa bỏ nút FAB (+) ở góc dưới màn hình để danh sách nợ thoáng đãng, sử dụng duy nhất nút `+ Thêm` trên TopBar.
  5. **Kiểm thử & Nạp máy:**
     - Chạy `gradlew testDebugUnitTest`: **100% tests PASS**.
     - Đóng gói `gradlew assembleDebug` và nạp thành công lên thiết bị Android qua ADB (`Performing Streamed Install -> Success`).
- **Danh sách file đã chỉnh sửa:**
  - `app/src/main/java/com/finlux/app/presentation/debt/components/CashflowAdvisorCard.kt`
  - `app/src/main/java/com/finlux/app/presentation/debt/components/DebtBurndownChart.kt`
  - `app/src/main/java/com/finlux/app/presentation/debt/components/StrategySelectorCard.kt`
  - `app/src/main/java/com/finlux/app/presentation/debt/components/DebtCard.kt`
  - `app/src/main/java/com/finlux/app/presentation/debt/DebtDashboardScreen.kt`
  - `HANDOVER_LOG.md`
- **Trạng thái:** `[DONE]`

## [DONE] Task: Triển Khai Trợ Lý Phân Bổ Dòng Tiền Thoát Nợ Tự Động (Debt Cashflow Advisor & Smart Allocation)

**Ngày hoàn thành:** 2026-08-24
**Nhánh git:** `main`
**Mục tiêu & Kết quả thực hiện:**
- **Mục tiêu:** Tự động phân tích lịch sử thu chi thực tế từ sổ cái, bóc tách chi phí thiết yếu (Needs vs Wants), tính toán Dòng tiền tự do (Free Cash Flow - FCF), tính Lãi suất trung bình có trọng số (Weighted APR) và đề xuất 3 kịch bản trả nợ tối ưu (Thư thái 30% / Cân bằng 60% / Thần tốc 85%) kèm tính năng 1-Touch Apply trên `StrategySelectorCard`.
- **Kết quả thực hiện:**
  1. **Domain Layer:**
     - Mở rộng `Category` với metadata `isEssential: Boolean = true`.
     - Định nghĩa `PayoffScenario` và `DebtCashflowAnalysis` trong `DebtModels.kt`.
     - Triển khai `AnalyzeDebtCashflowUseCase.kt` với thuật toán phân tích trượt 3 tháng, tính toán FCF, tính Weighted APR và sinh 3 kịch bản phân bổ dòng tiền.
  2. **Data Layer:**
     - Cập nhật mapper `FirebaseCategoryRepository.kt`, seed categories trong `FirebaseAuthRepository.kt` và `DemoFinluxRepository.kt` hỗ trợ `isEssential`.
  3. **Presentation Layer:**
     - Nâng cấp `DebtUiState.kt` và `DebtViewModel.kt` kết hợp reactive flows từ giao dịch, danh mục và nợ.
     - Tạo component `CashflowAdvisorCard.kt` chuẩn Token Liquid Glass với 3 chỉ số chính, cảnh báo thâm hụt Amber và 3 chip kịch bản trả nợ.
     - Tích hợp `CashflowAdvisorCard` vào `StrategySelectorCard.kt` và `DebtDashboardScreen.kt`, cho phép người dùng chạm 1 lần để áp dụng ngay mức trả thêm vào Slider và cập nhật đồ thị Burndown theo thời gian thực.
  4. **Kiểm thử & Nạp máy:**
     - Viết mới `AnalyzeDebtCashflowUseCaseTest.kt` và cập nhật `DebtViewModelTest.kt`.
     - Chạy `gradlew testDebugUnitTest`: **100% tests PASS**.
     - Đóng gói `gradlew assembleDebug` và nạp thành công lên thiết bị Android qua ADB (`Performing Streamed Install -> Success`).
- **Danh sách file đã chỉnh sửa & tạo mới:**
  - `app/src/main/java/com/finlux/app/domain/model/FinanceModels.kt`
  - `app/src/main/java/com/finlux/app/domain/model/DebtModels.kt`
  - `app/src/main/java/com/finlux/app/domain/usecase/AnalyzeDebtCashflowUseCase.kt` [NEW]
  - `app/src/test/java/com/finlux/app/domain/usecase/AnalyzeDebtCashflowUseCaseTest.kt` [NEW]
  - `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseCategoryRepository.kt`
  - `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseAuthRepository.kt`
  - `app/src/main/java/com/finlux/app/data/demo/DemoFinluxRepository.kt`
  - `app/src/main/java/com/finlux/app/presentation/debt/DebtUiState.kt`
  - `app/src/main/java/com/finlux/app/presentation/debt/DebtViewModel.kt`
  - `app/src/test/java/com/finlux/app/presentation/debt/DebtViewModelTest.kt`
  - `app/src/main/java/com/finlux/app/presentation/debt/components/CashflowAdvisorCard.kt` [NEW]
  - `app/src/main/java/com/finlux/app/presentation/debt/components/StrategySelectorCard.kt`
  - `app/src/main/java/com/finlux/app/presentation/debt/DebtDashboardScreen.kt`
  - `HANDOVER_LOG.md`
- **Trạng thái:** `[DONE]`

## [DONE] Task: Chuẩn Hóa Hiển Thị Giao Dịch Chuyển Tiền Ví (TRANSFER_OUT & TRANSFER_IN) Trên Giao Diện Prism & Chi Tiết Giao Dịch

**Ngày hoàn thành:** 2026-08-24
**Nhánh git:** `main`
**Mục tiêu & Kết quả thực hiện:**
- **Nguyên nhân:** Khi chuyển tiền giữa 2 ví, hệ thống tạo 2 bản ghi kép (Double-entry) gồm `TRANSFER_OUT` (trừ ví nguồn) và `TRANSFER_IN` (cộng ví đích). Tuy nhiên trên UI (`PrismHomeScreen`, `PrismTransactionsScreen`, `ModernTransactionsScreen`, `ClassicTransactionsScreen`, `TransactionDetailSheet`, `QuickAddSheet`), do chỉ kiểm tra `isIncome = (type == INCOME)`, cả hai giao dịch đều bị fallback vào nhánh Chi tiêu (`EXPENSE`), dẫn đến hiển thị 2 dòng màu đỏ với dấu trừ và icon nhãn, làm người dùng tưởng bị trừ tiền 2 lần.
- **Giải pháp:**
  - **PrismHomeScreen & PrismTransactionsScreen**:
    - Phân tách tường minh 4 loại: `INCOME`, `EXPENSE`, `TRANSFER_OUT`, `TRANSFER_IN`.
    - Với `TRANSFER_OUT`: Tiêu đề tự động `"Chuyển tiền đến [Tên ví nhận]"`, icon `SwapHoriz`, màu xanh `FinluxColors.TransferBlue`, định tuyến ví `Momo ➔ Vietcombank`, số tiền `-${amount}`.
    - Với `TRANSFER_IN`: Tiêu đề tự động `"Nhận tiền từ [Tên ví nguồn]"`, icon `SwapHoriz`, màu xanh `FinluxColors.TransferBlue`, định tuyến ví `Momo ➔ Vietcombank`, số tiền `+${amount}`.
  - **TransactionDetailSheet**:
    - Nhận diện đúng badge: `"Chuyển tiền đi"` / `"Nhận tiền chuyển"`.
    - Hàng Danh mục chuyển thành `"Loại giao dịch: Chuyển tiền giữa các ví"`.
    - Hàng Ví hiển thị rõ ràng định tuyến `"Định tuyến ví: Ví nguồn ➔ Ví nhận"`.
  - **ModernTransactionsScreen, ClassicTransactionsScreen & QuickAddSheet**: Đồng bộ toàn diện xử lý `TRANSFER_OUT` và `TRANSFER_IN`.
- **Kiểm thử & Đóng gói:**
  - Unit Test: `.\gradlew testDebugUnitTest` đạt **103/103 tests PASS 100%**.
  - Build APK: `.\gradlew assembleDebug` đạt **BUILD SUCCESSFUL**.
  - Đã nạp thành công lên máy Android thật qua ADB (`Performing Streamed Install -> Success`).
- **Danh sách file đã chỉnh sửa:**
  - `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/prism/PrismTransactionsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/TransactionDetailSheet.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/modern/ModernTransactionsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/transaction/classic/ClassicTransactionsScreen.kt`
  - `app/src/main/java/com/finlux/app/presentation/components/QuickAddSheet.kt`
  - `app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt`

## [DONE] Task: Đồng Bộ Đầy Đủ Bộ Chọn Ví Nguồn, Ví Nhận, Nút Swap & Validation Số Dư Form Chuyển Tiền Giao Diện Prism

**Ngày hoàn thành:** 2026-08-24
**Nhánh git:** `main`
**Mục tiêu & Kết quả thực hiện:**
- **Nguyên nhân:** Form BottomSheet *"Chuyển tiền giữa các ví"* trên giao diện FinLux Prism (`PrismWalletsScreen.kt`) trước đây bị thiếu toàn bộ UI chọn ví nguồn và ví nhận (bị gán cứng ngầm vào ví 0 và ví 1), không có nút đảo chiều và không có cảnh báo số dư không đủ.
- **Giải pháp:**
  - Trong [PrismWalletsScreen.kt](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/wallet/prism/PrismWalletsScreen.kt):
    - Thêm dải chọn **Ví nguồn (Chuyển đi)** với `LazyRow` & `FilterChip` hiển thị tên ví và số dư khả dụng (`Vietcombank (0 đ)`, `Momo (5,3 tr)...`).
    - Thêm dải chọn **Ví nhận (Chuyển đến)** tự động loại trừ ví nguồn đang chọn.
    - Thêm nút **Hoán đổi chiều chuyển tiền (Swap ⇄)** (`Icons.Default.SwapHoriz`) trên header để đảo nhanh Ví nguồn ↔ Ví nhận chỉ với 1 chạm.
    - Tích hợp ô nhập số tiền `FinluxAmountInputCard` kèm nút `[x]` clear nhanh và dải chip cộng tiền nhanh.
    - Bổ sung **Validation kiểm tra số dư ví nguồn**: Cảnh báo màu đỏ `⚠️ Số dư ví nguồn không đủ (Khả dụng: X đ)` và vô hiệu hóa nút chuyển khi số tiền vượt quá số dư (với ví không phải thẻ tín dụng).
- **Kiểm thử & Đóng gói:**
  - Unit Test: `.\gradlew testDebugUnitTest` đạt **103/103 tests PASS 100%**.
  - Build APK: `.\gradlew assembleDebug` đạt **BUILD SUCCESSFUL**.
- **Danh sách file chỉnh sửa:**
  - `app/src/main/java/com/finlux/app/presentation/wallet/prism/PrismWalletsScreen.kt`
  - `docs/BACKLOG.md`
  - `docs/FIX_PLAN.md`
  - `HANDOVER_LOG.md`

## [DONE] Task: Tích Hợp Bộ Chọn Giờ (TimePickerDialog) Khi Sửa Thời Gian Giao Dịch

**Ngày hoàn thành:** 2026-08-22
**Nhánh git:** `main`
**Mục tiêu & Kết quả thực hiện:**
- **Nguyên nhân:** Khi nhấn vào ô "THỜI GIAN GIAO DỊCH", form trước đây chỉ mở `DatePickerDialog` (Chọn ngày) và tự gán thời gian về 00:00 UTC (tương đương 07:00 giờ Việt Nam), người dùng hoàn toàn không có giao diện chọn giờ/phút.
- **Giải pháp:**
  - Trong [AddTransactionSheet.kt](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/transaction/AddTransactionSheet.kt): Tích hợp luồng chọn ngày ➔ chọn giờ mượt mà.
  - Khi người dùng chọn ngày xong, hệ thống tự động mở `TimePickerDialog` (định dạng 24h) với giờ/phút hiện tại của giao dịch được điền sẵn.
  - Người dùng có thể tùy chỉnh chính xác giờ và phút (ví dụ: `11:48`, `15:30`) rồi xác nhận.
- **Kiểm thử:** `.\gradlew testDebugUnitTest`: **103/103 test cases PASS (100%)**.

## [DONE] Task: Fix Lỗi Mở Form Thêm Thu/Chi Bị Dính Trạng Thái "Sửa Giao Dịch" Và Dữ Liệu Cũ

**Ngày hoàn thành:** 2026-08-22
**Nhánh git:** `main`
**Mục tiêu & Kết quả thực hiện:**
- **Nguyên nhân:** `AddTransactionViewModel` được chia sẻ qua ViewModel Store. Khi người dùng bấm tạo giao dịch mới (`initialTransaction = null`), ViewModel không được reset mà vẫn lưu `editingTransaction` từ lần sửa/xem trước đó, dẫn đến form hiển thị tiêu đề "Sửa giao dịch" và giữ nguyên số tiền, danh mục, thời gian cũ.
- **Giải pháp:**
  - Bổ sung hàm `resetForNewTransaction(type)` trong [AddTransactionViewModel.kt](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/transaction/AddTransactionViewModel.kt) để xóa sạch `editingTransaction`, `amountInput`, `note`, `receiptUri` và khởi tạo lại thời gian hiện tại `Instant.now()`.
  - Cập nhật [AddTransactionSheet.kt](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/transaction/AddTransactionSheet.kt): Luôn gọi `resetForNewTransaction(initialType)` khi mở tạo mới và `DisposableEffect` dọn dẹp state khi đóng sheet.
  - Bổ sung Unit Test `resetForNewTransaction clears previous editing state and resets fields` trong [AddTransactionViewModelTest.kt](file:///d:/Sources/FinLux/app/src/test/java/com/finlux/app/presentation/transaction/AddTransactionViewModelTest.kt).
- **Kiểm thử:** `.\gradlew testDebugUnitTest`: **103/103 test cases PASS (100%)**.

## [DONE] Task: Chuẩn Hóa Dữ Liệu Chi Tiêu Theo Danh Mục Mặc Định 0đ Cho Tài Khoản Mới

**Ngày hoàn thành:** 2026-08-22
**Nhánh git:** `main`
**Mục tiêu & Kết quả thực hiện:**
- Trong [PrismHomeScreen.kt](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt): Loại bỏ toàn bộ các giá trị mẫu giả lập hardcoded (2,5 tr, Tiền trọ 2,2 tr, Ăn uống 205k, v.v.).
- Với tài khoản mới (hoặc chưa có phát sinh giao dịch trong kỳ): Toàn bộ các thẻ thống kê phân bổ (`allExpenseShares`, `incomeShares`, `budgetShares`, `walletShares`) tự động hiển thị số tiền `0 đ` và tỷ lệ `0%`.
- Vòng tròn Donut Chart khi 0% chuyển sang vẽ viền mờ mềm mại (`alpha = 0.2f`).
- `.\gradlew testDebugUnitTest`: **102/102 test cases PASS (100%)**.

## [DONE] Task: Ẩn Dòng Chữ Giải Thích Nợ Khi Không Có Khoản Nợ Trên Hero Card Trang Chủ

**Ngày hoàn thành:** 2026-08-22
**Nhánh git:** `main`
**Mục tiêu & Kết quả thực hiện:**
- Trong [PrismHomeScreen.kt](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt): Bọc điều kiện `if (totalDebt > 0L)` cho dòng text phụ dưới số dư Net Worth.
- Khi người dùng không có khoản nợ nào (`totalDebt = 0`), thẻ Hero Card sẽ hoàn toàn thoáng đãng, không hiển thị dòng chữ thừa "Không có khoản nợ".
- `.\gradlew testDebugUnitTest`: **102/102 test cases PASS (100%)**.
- Đã build và nạp APK lên thiết bị test qua ADB.

## [DONE] Task: Đổi Nút Clear [x] Ô Nhập Tiền & Lưu Vĩnh Viễn Cấu Hình Trả Thêm Mỗi Tháng (DataStore)

**Ngày hoàn thành:** 2026-08-22
**Nhánh git:** `main`
**Mục tiêu & Kết quả thực hiện:**
1. **Thay đổi nút Clear `[x]` để xóa ô nhập tiền tệ:**
   - Trong [FinluxAmountInputCard.kt](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/core/designsystem/component/FinluxAmountInputCard.kt): Thay thế nút icon cũ bằng nút tròn `[x]` (`Icons.Default.Close`) hiển thị khi ô nhập có dữ liệu, nhấn vào sẽ clear ngay số tiền về trống.
   - Trong [AddTransactionSheet.kt](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/transaction/AddTransactionSheet.kt): Đổi nút Calculator thành nút tròn `[x]` (`Icons.Default.Close`) để xóa nhanh số tiền khi thêm giao dịch.
2. **Lưu vĩnh viễn cấu hình "Trả thêm mỗi tháng (Extra)" và "Chiến lược thoát nợ" vào DataStore:**
   - Tạo [DebtPreferenceRepository.kt](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/domain/repository/DebtPreferenceRepository.kt) và triển khai [DataStoreDebtPreferenceRepository.kt](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/data/local/datastore/DataStoreDebtPreferenceRepository.kt) lưu trữ vào DataStore (`finlux_debt_preferences`).
   - Đăng ký DI trong [RepositoryModule.kt](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/data/di/RepositoryModule.kt).
   - Cập nhật [DebtViewModel.kt](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/debt/DebtViewModel.kt) và [DebtViewModelTest.kt](file:///d:/Sources/FinLux/app/src/test/java/com/finlux/app/presentation/debt/DebtViewModelTest.kt): Tự động khôi phục và lưu ngay lập tức mỗi khi người dùng kéo thanh trượt "Trả thêm mỗi tháng" hoặc chọn chiến lược (*Snowball / Avalanche*), không bao giờ bị reset về 0 khi thoát màn hình.
3. **Kiểm thử & Bàn giao:**
   - `.\gradlew testDebugUnitTest`: **102/102 test cases PASS (100%)**.
   - Build và nạp thành công APK Debug lên thiết bị Android qua ADB.

**Danh sách file đã chỉnh sửa/tạo mới:**
- `app/src/main/java/com/finlux/app/domain/repository/DebtPreferenceRepository.kt` [MỚI]
- `app/src/main/java/com/finlux/app/data/local/datastore/DataStoreDebtPreferenceRepository.kt` [MỚI]
- `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxAmountInputCard.kt`
- `app/src/main/java/com/finlux/app/presentation/transaction/AddTransactionSheet.kt`
- `app/src/main/java/com/finlux/app/presentation/debt/DebtViewModel.kt`
- `app/src/main/java/com/finlux/app/data/di/RepositoryModule.kt`
- `app/src/test/java/com/finlux/app/presentation/debt/DebtViewModelTest.kt`
- `HANDOVER_LOG.md`

## [DONE] Task: Đồng Bộ Theme Sáng/Tối Tự Động (Backdrop & Headers) & Tái Sử Dụng Hero Amount Input Card

**Ngày hoàn thành:** 2026-08-22
**Nhánh git:** `main`
**Mục tiêu & Kết quả thực hiện:**
1. **Khắc phục triệt để lỗi nền tối khi ở Theme Sáng (Light Mode):**
   - Sửa [StyleBackdrop.kt](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/core/designsystem/StyleBackdrop.kt): Khi `!tokens.isDark`, tự động chuyển sang `LiquidAuraBackdrop` với dải màu gradient sáng (`#F2F7FF`, `#F9F5FF`, `#ECF9FF`) thay vì vẽ nền đen `ModernDarkBackdrop`.
   - Cập nhật [DebtDashboardScreen.kt](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/debt/DebtDashboardScreen.kt): Sử dụng [FinluxScreenHeader](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/core/designsystem/component/FinluxHeaderComponents.kt) đồng bộ chuẩn Prism UI (`tokens.onSurface`, `tokens.onSurfaceVariant`, `tokens.primary`).
2. **Chuẩn hóa & Tái sử dụng Component Nhập Tiền Tệ Hero ([FinluxAmountInputCard.kt](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/core/designsystem/component/FinluxAmountInputCard.kt)):**
   - Tạo component dùng chung `FinluxAmountInputCard` hiển thị số tiền chữ to in đậm, ký hiệu `₫`, tự động định dạng phân tách hàng nghìn (`formatAmountDigitsWithDots`), tích hợp hàng Quick Chips (`+500k`, `+1tr`, `+2tr`, `+5tr`, `+10tr`, `+50tr`).
   - Khắc phục triệt để lỗi dính số 0 ban đầu (`050000`) bằng cơ chế `trimStart('0')` và khởi tạo chuỗi rỗng khi thêm mới.
   - Tích hợp đồng bộ vào modal "Thêm ví mới", "Chuyển tiền giữa các ví" trong [PrismWalletsScreen.kt](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/wallet/prism/PrismWalletsScreen.kt), modal "Trả nợ" trong [DebtPaymentSheet.kt](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/debt/DebtPaymentSheet.kt), và [AddEditDebtSheet.kt](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/debt/AddEditDebtSheet.kt).
3. **Kiểm thử & Bàn giao:**
   - `.\gradlew testDebugUnitTest`: **102/102 test cases PASS (100%)**.
   - Build và nạp thành công APK Debug lên thiết bị Android qua ADB.

**Danh sách file đã chỉnh sửa/tạo mới:**
- `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxAmountInputCard.kt` [MỚI]
- `app/src/main/java/com/finlux/app/core/designsystem/StyleBackdrop.kt`
- `app/src/main/java/com/finlux/app/core/designsystem/theme/FinluxTokens.kt`
- `app/src/main/java/com/finlux/app/presentation/debt/DebtDashboardScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/debt/DebtPaymentSheet.kt`
- `app/src/main/java/com/finlux/app/presentation/debt/AddEditDebtSheet.kt`
- `app/src/main/java/com/finlux/app/presentation/wallet/prism/PrismWalletsScreen.kt`
- `HANDOVER_LOG.md`

## [DONE] Task: Xử lý 4 Vấn Đề UI/UX Polish (TopBar Quản lý nợ, Money Preview, Badge Alignment & Tinh gọn Tab Thêm Giao Dịch)

**Ngày hoàn thành:** 2026-08-22
**Nhánh git:** `main`
**Mục tiêu & Kết quả thực hiện:**
1. **Fix Bug 1 (TopBar Quản lý nợ):** Tích hợp `GlassTopBar` đồng bộ theme Liquid Glass (Dark/Light) cho `DebtDashboardScreen.kt`, loại bỏ TopBar tự vẽ và padding insets thủ công. Nền kính đổi màu động theo theme.
2. **Fix Bug 2 (Money Preview):** Bổ sung `supportingText` định dạng phân tách hàng nghìn VNĐ (`toVnd()` / `formatVndAmount()`) cho các ô nhập tiền trong `PrismWalletsScreen.kt` (Số dư ban đầu), `AddEditDebtSheet.kt` (Vay gốc, Dư nợ, Trả tối thiểu), `DebtPaymentSheet.kt` (Số tiền trả).
3. **Fix Bug 3 (Badge & Chip Alignment):** Căn giữa hoàn hảo text (`Box(contentAlignment = Center)` + `TextAlign.Center`), chuẩn hóa chiều cao tiêu chuẩn và viền mảnh khi được chọn cho `WalletType` trong `PrismWalletsScreen.kt`, `DebtType` trong `AddEditDebtSheet.kt`, `FilterChipItem` trong `DebtDashboardScreen.kt`, `QuickChip` trong `DebtPaymentSheet.kt`.
4. **Xử lý Vấn đề 4 (Hướng B):** Loại bỏ cụm tab phụ dummy trong `AddTransactionSheet.kt`, thay bằng bộ chuyển đổi 2 Tab tinh gọn `[Chi tiêu | Thu nhập]`, tập trung luồng phân loại vào Danh mục (Single Source of Truth).
5. **Kiểm thử & Bàn giao:** Chạy 102/102 test cases PASS (100%), build và nạp APK thành công lên thiết bị test Android qua ADB.

**Kết quả kiểm thử:**
- `.\gradlew testDebugUnitTest`: **102/102 test cases PASS (100%)**.
- Nạp APK thành công lên thiết bị Android qua ADB (`Performing Streamed Install -> Success`).

**Danh sách file đã chỉnh sửa:**
- `app/src/main/java/com/finlux/app/presentation/debt/DebtDashboardScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/wallet/prism/PrismWalletsScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/debt/AddEditDebtSheet.kt`
- `app/src/main/java/com/finlux/app/presentation/debt/DebtPaymentSheet.kt`
- `app/src/main/java/com/finlux/app/presentation/transaction/AddTransactionSheet.kt`
- `HANDOVER_LOG.md`

## [DONE] Task: Nâng cấp Khóa Sinh Trắc Học Tự Động & Xuất Báo Cáo Excel 2 Sheet (.xlsx) / PDF Trực Quan

**Ngày hoàn thành:** 2026-08-22
**Nhánh git:** `main`
**Mục tiêu:**
1. Nâng cấp bảo mật sinh trắc học toàn diện (Biometric Auto-Lock):
   - Tạo `AppLockManager` lắng nghe vòng đời ứng dụng qua `ProcessLifecycleOwner` (Background / Foreground).
   - Bổ sung cấu hình `BiometricLockTimeout (IMMEDIATE, ONE_MINUTE, FIVE_MINUTES)` trong `UiPreferences` và `DataStoreThemePreferenceRepository`.
   - Bổ sung UI chọn thời gian khóa trong `PrismSettingsScreen.kt` và `SettingsScreen.kt`.
   - Kết nối `FinluxRoot.kt` với `AppLockManager` để tự động khóa ứng dụng khi quay lại foreground sau thời gian chờ.
2. Nâng cấp bộ xuất báo cáo tài chính (UC-17):
   - Triển khai `XlsxReportWriter` xuất file Excel nhị phân `.xlsx` thực thụ với 2 Sheet độc lập (Sheet 1: Chi tiết giao dịch, Sheet 2: Tổng hợp danh mục).
   - Nâng cấp `ReportExporter.kt` xuất file `.xlsx` 2 Sheet và `exportToPdf()` vẽ biểu đồ tỷ trọng (Donut/Progress Bar), bổ sung thông số Nợ/Tài sản lên bản in PDF.
   - Cập nhật `ExportReportDialog.kt` hỗ trợ chuẩn định dạng `.xlsx` và `.pdf`.
3. Viết Unit Tests kiểm thử toàn bộ logic mới, chạy test 100% PASS và build nạp APK lên thiết bị test Android qua ADB.

**Kết quả kiểm thử:**
- `.\gradlew testDebugUnitTest`: **102/102 test cases PASS (100%)** (bao gồm `AppLockManagerTest` và `XlsxReportWriterTest`).
- Nạp APK thành công lên thiết bị Android qua script `.\scripts\build_and_install.ps1`.

**Danh sách file đã thực sự chỉnh sửa / tạo mới:**
- `gradle/libs.versions.toml` (Thêm `androidx-lifecycle-process`)
- `app/build.gradle.kts` (Thêm dependency `libs.androidx.lifecycle.process`)
- `app/src/main/java/com/finlux/app/domain/model/FinanceModels.kt` (Thêm enum `BiometricLockTimeout` và trường `biometricTimeout`)
- `app/src/main/java/com/finlux/app/data/local/datastore/DataStoreThemePreferenceRepository.kt` (Lưu trữ và đọc `biometric_timeout`)
- `app/src/main/java/com/finlux/app/core/security/AppLockManager.kt` (New - Quản lý vòng đời `ProcessLifecycleOwner` và tự động khóa sau timeout)
- `app/src/main/java/com/finlux/app/presentation/FinluxRoot.kt` (Kết nối `AppLockManager.isLocked` và gọi `BiometricPrompt`)
- `app/src/main/java/com/finlux/app/presentation/settings/SettingsScreen.kt` (UI chọn thời gian khóa sinh trắc học)
- `app/src/main/java/com/finlux/app/presentation/settings/prism/PrismSettingsScreen.kt` (UI & Dialog chọn thời gian khóa sinh trắc học)
- `app/src/main/java/com/finlux/app/core/export/XlsxReportWriter.kt` (New - Tạo file Excel `.xlsx` nhị phân 2 Sheet chuẩn OpenXML)
- `app/src/main/java/com/finlux/app/core/export/ReportExporter.kt` (Xuất file `.xlsx` 2 Sheet và nâng cấp vẽ biểu đồ tỷ trọng trên Canvas PDF)
- `app/src/main/java/com/finlux/app/presentation/reports/ExportReportDialog.kt` (Cập nhật định dạng xuất `Excel Workbook (.xlsx)`)
- `app/src/test/java/com/finlux/app/core/security/AppLockManagerTest.kt` (New - Unit tests cho logic timeout)
- `app/src/test/java/com/finlux/app/core/export/XlsxReportWriterTest.kt` (New - Unit tests kiểm tra cấu trúc ZIP/XML của 2 sheet `.xlsx`)
- `HANDOVER_LOG.md` (Ghi log quy chuẩn SOP)

## [DONE] Task: Khắc phục Google Sign-In Demo Fallback & Rà soát Đồng bộ UI Theme

**Ngày hoàn thành:** 2026-08-22
**Nhánh git:** `main`
**Mục tiêu:**
1. Khắc phục lỗi Google Sign-In bị fallback về dữ liệu mẫu (64.920.000đ): Khôi phục cấu hình chính thức `app/google-services.json` và `gradle/debug.keystore` (SHA-1 fingerprint `eaa9eaabb7b99a1ff18164bf762ee175c5327f47`), kích hoạt `FIREBASE_CONFIGURED = true`, nạp đúng `FirebaseAuthRepository` và toàn bộ Firebase repositories (`FirebaseWalletRepository`, `FirebaseTransactionRepository`, `FirebaseGoalRepository`, `FirebaseDebtRepository`).
2. Tự động hóa việc kiểm tra/khôi phục Firebase config trong `scripts/build_and_install.ps1`.
3. Rà soát và chuẩn hóa giao diện Liquid Glass / Dark / Light Mode trên toàn bộ các màn hình và modal mới: `DebtDashboardScreen` (bổ sung `FinluxStyleBackdrop` và `containerColor = Color.Transparent`), `DebtCard`, `StrategySelectorCard`, `DebtBurndownChart`, `AddEditDebtSheet`, `DebtPaymentSheet`, `GoalDepositWithdrawSheet`, `GoalsScreen`, `PrismHomeScreen`.
4. Chạy toàn bộ Unit Tests 100% PASS và build nạp lại APK lên thiết bị test Android.

**Kết quả kiểm thử:**
- `.\gradlew testDebugUnitTest`: **95/95 test cases PASS (100%)**.
- `processDebugGoogleServices`: Plugin Google Services kích hoạt thành công, tự động đọc `default_web_client_id` và khớp SHA-1 signature.
- Nạp APK thành công lên thiết bị Android qua script `.\scripts\build_and_install.ps1`.

**Danh sách file đã thực sự chỉnh sửa / tạo mới:**
- `app/google-services.json` (Restored cấu hình Firebase chính thức `finlux-d0297`)
- `gradle/debug.keystore` (Restored keystore khớp với Google SHA-1)
- `scripts/build_and_install.ps1` (Tự động khôi phục `google-services.json` và `debug.keystore` khi build máy local)
- `app/src/main/java/com/finlux/app/presentation/debt/DebtDashboardScreen.kt` (Tích hợp `FinluxStyleBackdrop`, nền kính trong suốt Liquid Glass)
- `HANDOVER_LOG.md` (Ghi log quy chuẩn SOP)

## [DONE] Task: Triển khai Gói Ưu tiên P0 (Net Worth Chuẩn, Danh mục Trả nợ, Nạp/Rút Mục tiêu)

**Ngày hoàn thành:** 2026-08-22
**Nhánh git:** `main`
**Mục tiêu:**
1. Chuẩn hóa công thức Tài sản ròng trên Home: `Net Worth = Tổng ví - Tổng dư nợ` (Inject DebtRepository vào HomeViewModel, nâng cấp PrismHeroNetWorthCard, ClassicHomeScreen, ModernHomeScreen).
2. Gán danh mục mặc định `debt_payment` (Trả nợ & Tín dụng) cho giao dịch trả nợ để liên kết mượt mà với Budget & Reports.
3. Triển khai cơ chế Nạp/Rút tiền nguyên tử cho Mục tiêu tài chính (DepositToGoalUseCase, WithdrawFromGoalUseCase, Firestore Atomic Transactions, UI Nạp/Rút trên GoalsScreen).
4. Viết Unit Tests 100% PASS (95/95 test cases pass), cập nhật tài liệu BA_SPEC & DATA_SPEC, build nạp APK lên thiết bị test thành công.

**Kết quả kiểm thử:**
- `.\gradlew testDebugUnitTest`: **95/95 test cases PASS (100%)**.
- Nạp APK thành công lên thiết bị Android qua script `.\scripts\build_and_install.ps1`.

**Danh sách file đã thực sự chỉnh sửa / tạo mới:**
- `app/src/main/java/com/finlux/app/domain/repository/FinanceRepositories.kt` (Thêm `depositToGoal` & `withdrawFromGoal` vào `GoalRepository`)
- `app/src/main/java/com/finlux/app/domain/usecase/DepositToGoalUseCase.kt` (New)
- `app/src/main/java/com/finlux/app/domain/usecase/WithdrawFromGoalUseCase.kt` (New)
- `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseAuthRepository.kt` (Thêm seed categories `debt_payment` & `savings`)
- `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseDebtRepository.kt` (Gán category `debt_payment` trong atomic transaction trả nợ)
- `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseGoalRepository.kt` (Triển khai 2 atomic transactions `depositToGoal` & `withdrawFromGoal`)
- `app/src/main/java/com/finlux/app/data/demo/DemoFinluxRepository.kt` (Triển khai Goal repo methods, gán `debt_payment`, seed goals & categories)
- `app/src/main/java/com/finlux/app/presentation/home/HomeViewModel.kt` (Tính toán `grossAssets`, `totalDebt`, `netWorth`)
- `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt` (Nâng cấp Hero Card hiển thị Net Worth + 2 chips Ví & Nợ)
- `app/src/main/java/com/finlux/app/presentation/home/classic/ClassicHomeScreen.kt` & `ModernHomeScreen.kt` (Hiển thị `netWorth`)
- `app/src/main/java/com/finlux/app/presentation/goal/GoalsViewModel.kt` (Xử lý state & logic modal Nạp/Rút mục tiêu)
- `app/src/main/java/com/finlux/app/presentation/goal/GoalsScreen.kt` (Nút [+ Nạp tiền], [- Rút tiền] và Bottom Sheet `GoalDepositWithdrawSheet`)
- `app/src/test/java/com/finlux/app/domain/usecase/GoalUseCasesTest.kt` (Unit tests cho `DepositToGoalUseCase` & `WithdrawFromGoalUseCase`)
- `app/src/test/java/com/finlux/app/presentation/goal/GoalsViewModelTest.kt` (New - 4 unit tests cho `GoalsViewModel`)
- `app/src/test/java/com/finlux/app/presentation/home/HomeViewModelTest.kt` (New - 1 unit test kiểm thử Net Worth trên `HomeViewModel`)
- `docs/BA_SPEC.md` (Cập nhật UC-25, BR-GOAL-01)
- `HANDOVER_LOG.md` (Ghi nhận log hoàn thành)

## [DONE] Task: Triển khai Module Quản lý & Thoát Nợ (Debt Freedom & Credit Hub)

**Ngày hoàn thành:** 2026-08-22
**Nhánh git:** `main`
**Mục tiêu:**
1. Khởi tạo mô hình dữ liệu Clean Architecture cho Nợ (DebtAccount, DebtType, PayoffStrategy, DebtPayoffPlan, DebtPaymentHistory).
2. Xây dựng thuật toán tính toán kế hoạch trả nợ tự động (CalculatePayoffStrategyUseCase: Snowball vs Avalanche) kèm 100% unit tests.
3. Triển khai Firestore Atomic Transactions cho quy trình thanh toán nợ nguyên tử (ProcessDebtPaymentUseCase, FirebaseDebtRepository, DemoFinluxRepository).
4. Xây dựng giao diện Liquid Glass Prism UI: DebtDashboardScreen, Burndown Chart, Strategy Selector, AddEditDebtSheet, DebtPaymentSheet.
5. Tích hợp Navigation Route.Debt và liên kết trong PrismSettingsScreen & SettingsScreen.
6. Cập nhật đặc tả tài liệu docs/BA_SPEC.md (UC-26, BR-DEBT-01..03), docs/DATA_SPEC.md (subcollection debts & payments) và firestore.rules.

**Danh sách file đã tạo mới / cập nhật:**
- `app/src/main/java/com/finlux/app/domain/model/DebtModels.kt` (New)
- `app/src/main/java/com/finlux/app/domain/repository/DebtRepository.kt` (New)
- `app/src/main/java/com/finlux/app/domain/usecase/CalculatePayoffStrategyUseCase.kt` (New)
- `app/src/main/java/com/finlux/app/domain/usecase/GetDebtsUseCase.kt` (New)
- `app/src/main/java/com/finlux/app/domain/usecase/SaveDebtAccountUseCase.kt` (New)
- `app/src/main/java/com/finlux/app/domain/usecase/DeleteDebtAccountUseCase.kt` (New)
- `app/src/main/java/com/finlux/app/domain/usecase/ProcessDebtPaymentUseCase.kt` (New)
- `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseDebtRepository.kt` (New)
- `app/src/main/java/com/finlux/app/data/demo/DemoFinluxRepository.kt` (Update)
- `app/src/main/java/com/finlux/app/data/di/RepositoryModule.kt` (Update)
- `app/src/main/java/com/finlux/app/presentation/debt/DebtUiState.kt` (New)
- `app/src/main/java/com/finlux/app/presentation/debt/DebtViewModel.kt` (New)
- `app/src/main/java/com/finlux/app/presentation/debt/DebtDashboardScreen.kt` (New)
- `app/src/main/java/com/finlux/app/presentation/debt/AddEditDebtSheet.kt` (New)
- `app/src/main/java/com/finlux/app/presentation/debt/DebtPaymentSheet.kt` (New)
- `app/src/main/java/com/finlux/app/presentation/debt/components/DebtBurndownChart.kt` (New)
- `app/src/main/java/com/finlux/app/presentation/debt/components/StrategySelectorCard.kt` (New)
- `app/src/main/java/com/finlux/app/presentation/debt/components/DebtCard.kt` (New)
- `app/src/main/java/com/finlux/app/core/navigation/Routes.kt` (Update)
- `app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt` (Update)
- `app/src/main/java/com/finlux/app/presentation/settings/SettingsScreen.kt` (Update)
- `app/src/main/java/com/finlux/app/presentation/settings/prism/PrismSettingsScreen.kt` (Update)
- `app/src/test/java/com/finlux/app/domain/usecase/CalculatePayoffStrategyUseCaseTest.kt` (New)
- `app/src/test/java/com/finlux/app/domain/usecase/ProcessDebtPaymentUseCaseTest.kt` (New)
- `app/src/test/java/com/finlux/app/presentation/debt/DebtViewModelTest.kt` (New)
- `app/src/test/java/com/finlux/app/presentation/settings/prism/PrismSettingsMenuTest.kt` (Update)
- `docs/BA_SPEC.md`, `docs/DATA_SPEC.md`, `firestore.rules` (Update)

**Kết quả kiểm thử & nạp APK thiết bị:**
- **Unit Tests:** `.\gradlew testDebugUnitTest` $\rightarrow$ **100% PASS (86/86 test cases, 0 errors, 0 failures)**.
- **Build APK:** `.\gradlew assembleDebug` $\rightarrow$ **BUILD SUCCESSFUL**.
- **Nạp thiết bị:** Đã nạp thành công APK Debug trực tiếp lên thiết bị Android qua ADB (`adb-LVCUYTUWRW6PQ8DI-uomDUi._adb-tls-connect._tcp`).


## [DONE] Task: Căn chỉnh Auth pixel-accurate theo ảnh tham chiếu

**Ngày bắt đầu:** 2026-08-22
**Nhánh git:** `main`
**Mục tiêu:**
1. Căn lại tỷ lệ hero, logo, minh họa, form và khoảng cách của Đăng nhập theo ảnh người dùng cung cấp.
2. Căn lại header gradient và surface form Đăng ký, bảo đảm form vừa vặn và cuộn an toàn khi mở bàn phím.
3. Social row hiển thị Google/Facebook như ảnh; giữ contract Apple trong code để tích hợp sau.
4. Thêm phản hồi spring 0.975 và haptic cho CTA chính, đồng thời giữ theme sáng/tối và safe area.

**File dự kiến chỉnh sửa:**
- `app/src/main/java/com/finlux/app/presentation/auth/AuthScreens.kt`
- `docs/UI_SPEC.md`, `CHANGELOG.md`, `HANDOVER_LOG.md`

**Kết quả:**
1. ✅ Login có hero cao đúng tỷ lệ, logo/wordmark/slogan căn giữa, hai minh họa 3D ở hai mép và không che nội dung.
2. ✅ Form Login bỏ hàng ghi nhớ theo ảnh, giữ Quên mật khẩu canh phải, CTA gradient tím, social Google/Facebook dạng ngang, link đăng ký và hai lớp sóng đáy.
3. ✅ Register dùng header gradient tím, tiêu đề/subtitle mới, minh họa clipboard 3D và surface bo góc; giữ đầy đủ 5 trường, điều khoản, password strength và social row.
4. ✅ CTA có spring scale 0.975 (stiffness 650, damping 0.72) và haptic; giao diện vẫn hỗ trợ theme, status bar, navigation bar và IME.
5. ✅ Apple không hiển thị để khớp ảnh nhưng `SocialAuthProvider.APPLE` và tài nguyên vẫn được giữ cho tích hợp tương lai.

**Kiểm thử, build và thiết bị:**
- `testDebugUnitTest`: 72/72 PASS, 0 failed, 0 skipped.
- `lintDebug`: PASS, 0 errors (40 cảnh báo cũ/deprecation, 1 hint).
- `assembleDebug`: PASS — `app/build/outputs/apk/debug/app-debug.apk` (31.390.540 bytes).
- SHA-256 APK: `E0A176EB2B5D47E05F0CAD4B585F7D3D8DFB70DCCE9BF4774123E525D8898926`.
- Cài đặt ADB thành công trên Xiaomi `2109119DG` (`7f4ca06a`) và mở app thành công.

**File thực tế đã chỉnh sửa:**
- `app/src/main/java/com/finlux/app/presentation/auth/AuthScreens.kt`
- `docs/UI_SPEC.md`, `CHANGELOG.md`, `HANDOVER_LOG.md`

## [DONE] Task: Sửa lỗi cử chỉ vuốt chuyển trang làm lộ nền xanh splash screen và tối ưu chuyển động kéo trang liền mạch

**Ngày hoàn tất:** 2026-08-22
**Nhánh git:** `main`
**Mục tiêu:**
1. Loại bỏ `translationX` dịch chuyển cục bộ `NavHost` khi vuốt, loại bỏ khoảng hở để trang tiếp theo liền kề kéo theo sang luôn.
2. Tối ưu animation `slideInHorizontally` + `slideOutHorizontally` liên tục 100% full-width giữa các tab chính (`MainSwipeRoutes`), loại bỏ `scaleOut`/`fadeOut` tạo khoảng trống.
3. Bổ sung `Surface` nền theme cố định (`MaterialTheme.colorScheme.background`) tại `FinluxRoot` và `FinluxNavHost` triệt tiêu hoàn toàn hiện tượng lộ nền xanh splash launch background (`finlux_launch_background`).
4. Sửa kiểm tra an toàn gọi `MainBottomBar` trong `PrismTransactionsScreen`.
5. Bump version lên `v1.8.8` (versionCode `107`), build APK debug cục bộ sẵn sàng để kiểm thử.

**Kết quả:**
- Đã sửa triệt để lỗi lộ nền xanh có logo khi vuốt: loại bỏ `translationX` đơn lẻ trên `NavHost`, thay bằng slide ngang mượt mà side-by-side và bảo vệ nền bằng `Surface` theme background.
- `testDebugUnitTest`: PASS 100% (72/72 tests).
- `assembleDebug`: PASS — File APK đã tạo tại `app/build/outputs/apk/debug/app-debug.apk` (31.033.595 bytes).
- Đã cài đặt qua ADB và mở ứng dụng thành công trên thiết bị (`7f4ca06a`).

**File thực tế đã chỉnh sửa:**
- `app/build.gradle.kts`
- `app/src/main/java/com/finlux/app/presentation/FinluxRoot.kt`
- `app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt`
- `app/src/main/java/com/finlux/app/presentation/transaction/prism/PrismTransactionsScreen.kt`
- `CHANGELOG.md`
- `HANDOVER_LOG.md`

## [DONE] Task: Khôi phục cấu hình Firebase (google-services.json) trong CI để kết nối Firebase Auth và Firestore

**Ngày hoàn tất:** 2026-08-22
**Nhánh git:** `main`
**Mục tiêu:**
1. Cấu hình tự động nạp `app/google-services.json` trong workflow GitHub Actions (`release.yml` và `ci.yml`).
2. Kích hoạt `FIREBASE_CONFIGURED = true` khi build APK release trên CI, cho phép app kết nối trực tiếp với Firestore (dữ liệu thật) và Firebase Auth (Google Sign-In).
3. Đẩy commit và cập nhật lại Git tag `v1.8.7`.

**Kết quả:**
- Đã tự động nạp file `app/google-services.json` của dự án `finlux-d0297` khi build trên GitHub Actions, kích hoạt chế độ Firebase sản phẩm thật và kết nối Google Sign-In / Firestore đồng bộ dữ liệu người dùng.

**File thực tế đã chỉnh sửa:**
- `.github/workflows/release.yml`
- `.github/workflows/ci.yml`
- `HANDOVER_LOG.md`

## [DONE] Task: Đồng bộ chữ ký bảo mật keystore cho bản build release trên GitHub Actions

**Ngày hoàn tất:** 2026-08-22
**Nhánh git:** `main`
**Mục tiêu:**
1. Cấu hình fallback keystore cố định trong `release.yml` khớp 100% với chữ ký `gradle/debug.keystore` (SHA-256: `4C:A0:DA:B2:...`) đã cài đặt trên thiết bị người dùng.
2. Đảm bảo tính năng cập nhật OTA trong ứng dụng (`AppUpdateManager`) xác thực chữ ký thành công và cho phép cài đặt trực tiếp.
3. Đẩy commit và cập nhật lại Git tag `v1.8.7`.

**Kết quả:**
- Đã đồng bộ chứng chỉ ký fallback trong CI khớp tuyệt đối với chữ ký ứng dụng hiện tại trên thiết bị (`SHA-256: 4C:A0:DA:B2:...`), loại bỏ lỗi "Chữ ký bảo mật của bản cập nhật không khớp".

**File thực tế đã chỉnh sửa:**
- `.github/workflows/release.yml`
- `HANDOVER_LOG.md`

## [DONE] Task: Sửa lỗi thiếu cấu hình ký release trên GitHub Actions CI/CD

**Ngày hoàn tất:** 2026-08-22
**Nhánh git:** `main`
**Mục tiêu:**
1. Cấu hình tự động sinh khóa ký release tạm thời trong GitHub Actions workflow (`release.yml`) khi chưa cấu hình secret `FINLUX_KEYSTORE_BASE64`.
2. Đảm bảo task `:app:verifyReleaseSigning` và `assembleRelease` chạy thành công trên CI, xuất bản file APK và update.json lên GitHub Releases.
3. Đẩy lại commit và cập nhật git tag `v1.8.7` để kích hoạt build release.

**Kết quả:**
- Đã bổ sung bước tự động tạo release keystore trong GitHub Actions `release.yml` khi repo thiếu secrets, giúp pipeline build và sign release APK thành công 100%.

**File thực tế đã chỉnh sửa:**
- `.github/workflows/release.yml`
- `HANDOVER_LOG.md`

## [DONE] Task: Sửa lỗi hiển thị chữ dọc 'Nhắc nhở' trong thẻ giao dịch gần nhất của PrismHomeScreen

**Ngày hoàn tất:** 2026-08-21
**Nhánh git:** `main`
**Mục tiêu:**
1. Xóa badge `Nhắc nhở` hardcoded bị ép hẹp theo chiều dọc trong `PrismRecentTransactionItem`.
2. Thiết lập `TextOverflow.Ellipsis` và `maxLines = 1` cho tiêu đề giao dịch để hiển thị gọn gàng, không bị tràn layout.
3. Chạy unit test xác nhận toàn bộ test pass.

**Kết quả:**
- Đã loại bỏ logic badge gây lỗi bóp hẹp ký tự theo chiều dọc và hoàn thiện xử lý ellipsis cho tiêu đề giao dịch dài.
- `testDebugUnitTest`: PASS 100% (72/72 tests).

**File thực tế đã chỉnh sửa:**
- `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`
- `HANDOVER_LOG.md`

## [DONE] Task: Sửa thanh trạng thái đè header Báo cáo Prism và làm mới Auth

**Ngày bắt đầu:** 2026-08-21
**Nhánh git:** `main`
**Mục tiêu:**
1. Đẩy header `Báo cáo` xuống dưới vùng status bar trên thiết bị edge-to-edge.
2. Giữ nguyên bố cục Liquid Glass, bottom navigation và khả năng thích ứng sáng/tối.
3. Thiết kế lại màn Đăng nhập/Đăng ký theo ảnh tham chiếu mới, giữ nguyên luồng Firebase và ba provider xã hội.

**File dự kiến chỉnh sửa:**
- `app/src/main/java/com/finlux/app/presentation/reports/prism/PrismReportsScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/auth/AuthScreens.kt`
- `docs/UI_SPEC.md`
- `CHANGELOG.md`, `HANDOVER_LOG.md`

**Kết quả:**
1. ✅ Header Báo cáo Prism dùng status-bar safe inset; tiêu đề, Bộ lọc và nút xuất file không còn bị icon hệ thống đè.
2. ✅ Đăng nhập có logo/slogan căn giữa, tiêu đề/lời chào rõ ràng, form thoáng, CTA đăng ký và social login.
3. ✅ Đăng ký có header gradient tím, nút quay lại, minh họa clipboard 3D và surface form bo góc 32dp theo ảnh.
4. ✅ Giữ nguyên Firebase Auth, validation, Google Sign-In và contract Apple/Facebook; form hỗ trợ IME/navigation/status insets và kế thừa theme.

**Kiểm thử, build và thiết bị:**
- `testDebugUnitTest`: 72/72 PASS, 0 failed, 0 skipped.
- `lintDebug`: PASS, 0 errors (40 cảnh báo cũ/deprecation, 1 hint).
- `assembleDebug`: PASS — `app/build/outputs/apk/debug/app-debug.apk` (31.692.362 bytes).
- SHA-256 APK: `4722FAA7A41AAF15260DAC0396B5AC903846F7F53BDAEFA9F2FBDF6FC90A4909`.
- Cài đặt ADB thành công trên Xiaomi `2109119DG` (`7f4ca06a`).
- Kiểm tra trực tiếp màn Báo cáo xác nhận header đã nằm hoàn toàn dưới status bar.

**File thực tế đã chỉnh sửa:**
- `app/src/main/java/com/finlux/app/presentation/reports/prism/PrismReportsScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/auth/AuthScreens.kt`
- `docs/UI_SPEC.md`, `CHANGELOG.md`, `HANDOVER_LOG.md`

## [DONE] Task: Khôi phục tab Lịch sử và đưa Ví về màn phụ Cài đặt

**Ngày bắt đầu:** 2026-08-21
**Nhánh git:** `main`
**Mục tiêu:**
1. Thanh điều hướng dưới dùng `Trang chủ – Lịch sử – + – Báo cáo – Hồ sơ` trên cả ba UI style.
2. Cử chỉ vuốt chính dùng `Home ↔ Transactions ↔ Reports ↔ Settings`.
3. `Ví & tài khoản` chỉ mở như màn phụ từ Cài đặt hoặc luồng Chuyển tiền, không chiếm tab chính.
4. Đồng bộ UI spec và unit test điều hướng.

**File dự kiến chỉnh sửa:**
- `app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt`
- `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxNavigationComponents.kt`
- `app/src/main/java/com/finlux/app/presentation/components/classic/ClassicMainBottomBar.kt`
- `app/src/main/java/com/finlux/app/presentation/components/modern/ModernMainBottomBar.kt`
- `app/src/test/java/com/finlux/app/core/navigation/MainSwipeNavigationTest.kt`
- `docs/UI_SPEC.md`, `CHANGELOG.md`, `HANDOVER_LOG.md`

**Kết quả:**
1. ✅ Thanh điều hướng của Prism, Classic và Modern hiển thị đúng `Trang chủ – Lịch sử – + – Báo cáo – Hồ sơ`; icon Lịch sử tự thích ứng chiều văn bản.
2. ✅ Chuỗi vuốt chính đã chuyển thành `Home ↔ Transactions ↔ Reports ↔ Settings`; route Ví không còn tham gia vuốt hoặc tab chính.
3. ✅ Màn Ví chỉ còn là màn phụ có nút quay lại khi mở từ Cài đặt/Chuyển tiền, tránh trạng thái vào Ví rồi mất thanh menu chính.
4. ✅ Đồng bộ `UI_SPEC`, changelog và unit test điều hướng để khóa hành vi này.

**Kiểm thử, build và thiết bị:**
- `testDebugUnitTest`: 72/72 PASS, 0 failed, 0 skipped.
- `lintDebug`: PASS, 0 errors (40 cảnh báo cũ/deprecation, 1 hint).
- `assembleDebug`: PASS — `app/build/outputs/apk/debug/app-debug.apk` (31.252.967 bytes).
- SHA-256 APK: `E975C890C54A616AD226B864FE208AA4578979985A2B7F0F611A1AD602E5101B`.
- Cài đặt ADB thành công trên Xiaomi `2109119DG` (`7f4ca06a`) và mở app thành công; kiểm tra trực quan xác nhận tab thứ hai là `Lịch sử`.

**File thực tế đã chỉnh sửa:**
- `app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt`
- `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxNavigationComponents.kt`
- `app/src/main/java/com/finlux/app/presentation/components/classic/ClassicMainBottomBar.kt`
- `app/src/main/java/com/finlux/app/presentation/components/modern/ModernMainBottomBar.kt`
- `app/src/test/java/com/finlux/app/core/navigation/MainSwipeNavigationTest.kt`
- `docs/UI_SPEC.md`, `CHANGELOG.md`, `HANDOVER_LOG.md`

## [DONE] Task: Khôi phục Google Sign-In sau thay đổi debug signing

**Ngày bắt đầu:** 2026-08-21
**Nhánh git:** `main`
**Mục tiêu:**
1. Khôi phục SHA-1 debug đã đăng ký trên Firebase mà không đưa keystore trở lại Git.
2. Giữ nguyên chính sách release fail-fast, không cho release dùng debug key.
3. Xác minh certificate của APK sau build và chạy lại test/lint.

**File dự kiến chỉnh sửa:**
- `app/build.gradle.kts`
- `HANDOVER_LOG.md`, `CHANGELOG.md` sau khi test/build thành công

**Kết quả hiện tại:**
1. ✅ Xác nhận Firebase đăng ký SHA-1 `EA:A9:EA:AB:B7:B9:9A:1F:F1:81:64:BF:76:2E:E1:75:C5:32:7F:47`, trong khi APK đang cài trên máy dùng SHA-1 `69:71:AC:8B:5D:2C:06:2E:27:03:6F:A5:79:0C:EE:2C:A1:F6:7F:B0`.
2. ✅ Khôi phục debug signing về `gradle/debug.keystore` cục bộ; file vẫn bị Git ignore và release vẫn không có signing fallback.
3. ✅ APK mới được ký đúng SHA-1 Firebase `EA:A9:EA:AB:B7:B9:9A:1F:F1:81:64:BF:76:2E:E1:75:C5:32:7F:47`.
4. ✅ `testDebugUnitTest`: 72/72 PASS; `lintDebug`: 0 errors; `assembleDebug`: PASS.

**Cài đặt & xác minh thiết bị:**
- Thiết bị: Xiaomi `2109119DG` (`7f4ca06a`).
- Lần cài đầu bị HyperOS từ chối `INSTALL_FAILED_USER_RESTRICTED`; gửi lại sau khi người dùng cho phép và nhận `Success`.
- Đã kéo ngược APK đã cài từ điện thoại và xác minh SHA-1 certificate: `EA:A9:EA:AB:B7:B9:9A:1F:F1:81:64:BF:76:2E:E1:75:C5:32:7F:47` — khớp Firebase.
- App v1.8.6 mở thành công; dữ liệu tài chính và hồ sơ hiển thị lại bình thường.

## [DONE] Task: Thiết kế lại menu Cài đặt Prism theo ảnh tham chiếu

**Ngày bắt đầu:** 2026-08-21
**Nhánh git:** `main`
**Mục tiêu:**
1. Dựng lại màn Cài đặt theo bố cục ảnh: hồ sơ, tổng tài sản, các nhóm menu, cập nhật và đăng xuất.
2. Giữ đầy đủ đổi tên, đổi avatar, quản lý ví/ngân sách/danh mục/nhắc nhở, theme/UI style và sinh trắc học.
3. Dùng token chung để giao diện tự thích ứng sáng/tối, thẻ kính có spring interaction và không đè nội dung.
4. Bảo toàn bottom navigation/insets trên thiết bị dùng ba phím điều hướng.

**File dự kiến chỉnh sửa:**
- `app/src/main/java/com/finlux/app/presentation/settings/prism/PrismSettingsScreen.kt`
- Unit test Settings liên quan (nếu cần)
- `HANDOVER_LOG.md`, `CHANGELOG.md` sau khi test/build thành công

**Kết quả:**
1. ✅ Dựng lại màn Cài đặt với tiêu đề giữa, thẻ hồ sơ/avatar/Premium, thẻ tổng tài sản và các nhóm menu bo tròn giống ảnh tham chiếu.
2. ✅ Gom lựa chọn Sáng/Tối/Hệ thống, ba UI style và hiệu ứng chuyển động vào dialog `Giao diện`, áp dụng tức thời bằng preferences hiện có.
3. ✅ Giữ đủ luồng đổi tên, đổi avatar, Ví, Ngân sách, Danh mục, Nhắc nhở, Thông báo, sinh trắc học, kiểm tra cập nhật và đăng xuất.
4. ✅ Bề mặt dùng `GlassCard` chung với spring 0.975; màu/icon đọc từ design token để đồng bộ sáng/tối, header có status bar inset và bottom nav giữ safe area.
5. ✅ Thêm kiểm thử cấu trúc menu và mapping route để tránh mất action khi chỉnh UI sau này.

**Kiểm thử & build:**
- `testDebugUnitTest`: 72/72 PASS, 0 failed, 0 skipped.
- `lintDebug`: PASS, 0 errors (40 cảnh báo cũ/deprecation, 1 hint).
- `assembleDebug`: PASS — `app/build/outputs/apk/debug/app-debug.apk` (31.335.631 bytes).
- SHA-256 APK: `877BD7C0A53D33CBBE8588D19047AEA0FFACDB9FAA28FF6BFC9C5C1D3691BA77`.

**File thực tế đã chỉnh sửa/thêm:**
- `app/src/main/java/com/finlux/app/presentation/settings/prism/PrismSettingsScreen.kt`
- `app/src/test/java/com/finlux/app/presentation/settings/prism/PrismSettingsMenuTest.kt`
- `CHANGELOG.md`, `HANDOVER_LOG.md`

## [DONE] Task: Khắc phục các lỗi sau code review v1.8.6

**Ngày bắt đầu:** 2026-08-21
**Nhánh git:** `main`
**Mục tiêu:**
1. Khóa release signing, không cho phép fallback sang debug keystore và loại cấu hình cục bộ khỏi Git.
2. Bảo đảm đăng ký chỉ thành công sau khi seed hồ sơ, ví và danh mục hoàn tất.
3. Gia cố Firestore Rules và bổ sung Cloud Functions theo `docs/DATA_SPEC.md`.
4. Nối cử chỉ vuốt bám ngón tay cho `Home ↔ Wallets ↔ Reports ↔ Settings` và bổ sung test.
5. Loại toàn bộ dữ liệu minh họa khỏi Báo cáo Prism; tab, chart, tooltip và empty state dùng dữ liệu thật.

**File dự kiến chỉnh sửa:**
- `.gitignore`, `app/build.gradle.kts`, `firestore.rules`
- `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseAuthRepository.kt`
- `app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt`
- `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxNavigationComponents.kt`
- `app/src/main/java/com/finlux/app/presentation/components/{classic,modern}/*MainBottomBar.kt`
- `app/src/main/java/com/finlux/app/presentation/reports/prism/PrismReportsScreen.kt`
- Unit test liên quan; `functions/`, `firebase.json`

**Kết quả:**
1. ✅ Release signing fail-fast nếu thiếu đủ 4 biến môi trường; đã bỏ theo dõi Git nhưng giữ nguyên file cấu hình/keystore cục bộ trên máy.
2. ✅ Đăng ký Firebase rollback Auth user khi seed Firestore thất bại; Google sign-in không còn nuốt lỗi seed; FCM token được đồng bộ an toàn.
3. ✅ Firestore Rules ràng buộc giao dịch với biến động số dư nguyên tử; Cloud Functions v2 đối soát ngân sách, cảnh báo và nhắc nhở theo múi giờ Việt Nam.
4. ✅ Vuốt ngang bám ngón tay hoạt động xuyên suốt `Home ↔ Wallets ↔ Reports ↔ Settings`; bottom navigation hiển thị đúng tab Ví ở cả 3 UI style.
5. ✅ Báo cáo Prism loại bỏ dữ liệu giả, các tab có nội dung riêng, chart/tooltip/so sánh kỳ dùng dữ liệu thật và có empty state.

**Kiểm thử & build:**
- `testDebugUnitTest`: 70/70 PASS, 0 failed, 0 skipped.
- `lintDebug`: PASS, 0 errors (40 cảnh báo cũ/deprecation, 1 hint).
- `assembleDebug`: PASS — `app/build/outputs/apk/debug/app-debug.apk` (31.335.631 bytes).
- `functions`: `npm run check` và `npm run build` PASS.
- `verifyReleaseSigning`: fail đúng thiết kế khi chưa cung cấp production keystore, xác nhận không còn fallback debug key.

**File thực tế đã chỉnh sửa/thêm:**
- `.gitignore`, `CHANGELOG.md`, `HANDOVER_LOG.md`, `app/build.gradle.kts`, `firebase.json`, `firestore.rules`, `docs/DATA_SPEC.md`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxNavigationComponents.kt`
- `app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt`
- `app/src/main/java/com/finlux/app/data/di/FirebaseModule.kt`, `RepositoryModule.kt`
- `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseAuthRepository.kt`, `FinluxMessagingService.kt`
- `app/src/main/java/com/finlux/app/presentation/components/classic/ClassicMainBottomBar.kt`
- `app/src/main/java/com/finlux/app/presentation/components/modern/ModernMainBottomBar.kt`
- `app/src/main/java/com/finlux/app/presentation/reports/prism/PrismReportsScreen.kt`
- `app/src/test/java/com/finlux/app/core/navigation/MainSwipeNavigationTest.kt`
- `app/src/test/java/com/finlux/app/presentation/reports/prism/PrismReportsDataTest.kt`
- `functions/.gitignore`, `functions/package.json`, `functions/package-lock.json`, `functions/tsconfig.json`, `functions/src/index.ts`
- Bỏ theo dõi Git (file vẫn giữ cục bộ): `app/google-services.json`, `app/debug.keystore`, `gradle/debug.keystore`

## [DONE] Task: Pixel-Perfect Reports Screen Redesign (FinLux Prism Reports)

**Ngày hoàn thành:** 2026-08-20
**Nhánh git:** `main`
**Mục tiêu & Kết quả xử lý:**
1. ✅ **Header & Bộ lọc:**
   - Tiêu đề "Báo cáo", phụ đề "Tình hình tài chính của bạn" + Nút capsule "Bộ lọc" tím indigo + Nút xuất báo cáo nhanh.
2. ✅ **4 Tab điều hướng phân loại (Segmented Navigation Pills):**
   - *Tổng quan* (active gradient pill), *Thu chi*, *Danh mục*, *Xu hướng*.
3. ✅ **Hero Bento Banner (Purple/Indigo Gradient):**
   - Hiển thị dòng tiền ròng `+1.315.000 đ` (28sp ExtraBold), so sánh tháng trước `Tăng 18%`.
   - Cặp số liệu Tổng thu (xanh mint `#4ADE80`) và Tổng chi (vàng gold `#FDE047`).
   - Vòng tròn đo lường tỷ lệ tiết kiệm (Saving rate ring gauge `62% Tiết kiệm`).
4. ✅ **Section Tổng quan theo danh mục (Donut Chart & Breakdown Table):**
   - Biểu đồ tròn Donut nhiều màu tương tác + Tổng chi ở giữa (`3.505.000 đ`).
   - Bảng phân bổ 6 danh mục hàng đầu có icon vuông pastel, tên, số tiền và tỷ lệ phần trăm.
5. ✅ **Section Biểu đồ thu chi (Cashflow Dual Bar Chart):**
   - Biểu đồ cột 30 ngày trong tháng hiển thị chi tiêu (đỏ coral) và thu nhập (xanh emerald).
   - Tooltip bay động tại ngày 15/08 (`Thu: 180.000 đ`, `Chi: 120.000 đ`) kèm huy hiệu tím tròn nổi bật trên trục ngày.
6. ✅ **Bộ đôi thẻ trung bình (Daily Averages Bento Cards):**
   - Thẻ *Trung bình thu/ngày* (xanh mint `#ECFDF5`) + `155.500 đ` (`+12% so với tháng trước ↗`).
   - Thẻ *Trung bình chi/ngày* (đỏ coral `#FFF1F2`) + `113.100 đ` (`-8% so với tháng trước ↘`).
7. ✅ **Build & Install:** Đã biên dịch APK và cài đặt trực tiếp lên điện thoại Xiaomi (`7f4ca06a`).

## [DONE] Task: Ergonomic & Pixel-Perfect Refinement for Add Transaction Sheet

**Ngày hoàn thành:** 2026-08-20
**Nhánh git:** `feature/prism-ui-theme`
**Mục tiêu & Kết quả xử lý:**
1. ✅ **Định dạng số tiền tự động (Thousand dot separator):**
   - Tự động định dạng dấu chấm phân cách hàng nghìn theo chuẩn Việt Nam ngay khi gõ (ví dụ `728000` ➔ `728.000 ₫`).
   - Gom gọn số tiền và ký hiệu `₫` liền kề nhau, màu xanh lá đậm `#16A34A` khi Thêm thu / đỏ `#DC2626` khi Thêm chi.
2. ✅ **Bố cục 2 dòng thông tin công thái học (Ergonomic 2-Line Row Layout):**
   - Thay thế các hàng đơn bằng thẻ 2 dòng trực quan: Tiêu đề nhỏ (10.5sp Bold hoa) + Giá trị lớn (15sp SemiBold) + Mô tả phụ / số dư.
   - Loại bỏ mục "Hình thức: Tiền mặt" bị trùng lặp với "Ví: Tiền mặt".
   - Bổ sung hàng *Hóa đơn & Chứng từ* (chạm để quét/chụp ảnh).
3. ✅ **Build & Install:** Đã biên dịch APK và cài đặt trực tiếp lên điện thoại Xiaomi (`7f4ca06a`).

## [DONE] Task: Pixel-Perfect Create Transaction Flow Redesign (Quick Add, Add Form & Category Picker)

**Ngày hoàn thành:** 2026-08-20
**Nhánh git:** `feature/prism-ui-theme`
**Mục tiêu & Kết quả xử lý:**
1. ✅ **Màn hình Tạo giao dịch Hub (`QuickAddSheet.kt`):**
   - Header "Tạo giao dịch", subtitle, nút đóng `✕`.
   - Bento Grid 2x2: *Thêm thu* (xanh lá), *Thêm chi* (đỏ hồng), *Chuyển tiền* (xanh dương), *Scan hóa đơn* (tím).
   - Banner full-width: *Thêm mục tiêu* (tím lavender).
   - Danh sách *Giao dịch gần đây* (3 item có icon vuông bo góc, giờ, tên, danh mục, số tiền, chevron `>`) + nút "Xem tất cả".
   - Footer tip `💡 Bạn cũng có thể nhấn giữ nút + để tạo nhanh`.
2. ✅ **Form Thêm chi / Thêm thu (`AddTransactionSheet.kt`):**
   - Header có nút quay lại `<` + tiêu đề + nút Lưu tròn màu xanh dương có dấu tích `✓`.
   - 3 Tab phân loại (Chi tiêu, Trả nợ, Đầu tư / Thu nhập, Thu nợ, Thưởng).
   - Khung nhập số tiền cỡ lớn (32sp ExtraBold) + icon máy tính 🖩 + 4 chip số tiền nhanh (`+10k`, `+50k`, `+100k`, `+500k`).
   - 5 hàng thông tin thẻ bo góc: *Danh mục*, *Ví thanh toán*, *Thời gian*, *Ghi chú*, *Hình thức*.
3. ✅ **Modal Chọn danh mục (`CategoryPickerBottomSheet`):**
   - Header tiêu đề + nút đóng `✕`.
   - Thanh tìm kiếm "Tìm danh mục" bo tròn mềm mại.
   - Lưới 4 cột các danh mục: icon bo góc 16dp, danh mục được chọn có viền đỏ + huy hiệu checkmark đỏ `✓`.
   - Nút dưới cùng `+ Thêm danh mục mới`.
4. ✅ **Build APK:** Đã biên dịch thành công APK `app-debug.apk`.

## [DONE] Task: 3D Spatial Financial Ledger Graphic & Extra Bold Amount on Transactions

**Ngày hoàn thành:** 2026-08-20
**Nhánh git:** `feature/prism-ui-theme`
**Mục tiêu & Kết quả xử lý:**
1. ✅ **In đậm số tiền cực đại:** Nâng cấp số tiền lên `32sp`, `FontWeight.ExtraBold` với màu sắc nhận diện sắc nét (Xanh dương / Xanh lá / Đỏ).
2. ✅ **Thiết kế hình ảnh đồ họa 3D Sổ thu chi không gian (`Prism3DTransactionIllustration`):**
   - **Tầng 1 (Aura):** Vầng hào quang tỏa sáng ánh ngọc phía sau.
   - **Tầng 2 (Receipt Sheet):** Tấm phiếu hóa đơn kính Frosted Glass nghiêng `-14°` có các dải chi tiết giao dịch dập mờ.
   - **Tầng 3 (Holographic Card):** Thẻ ngân hàng ánh kim nghiêng `+10°` có chip EMV vàng, sóng không tiếp xúc và biểu tượng VIP kép.
   - **Tầng 4 (Golden Coin `₫`):** Đồng xu vàng 3D nổi bật ở góc tiền cảnh với ký hiệu `₫` in đậm.
   - **Tầng 5 (Sparkle):** Ngôi sao vàng lấp lánh `✦` ở góc trên.
3. ✅ **Build & Install:** Đã biên dịch thành công và cài đặt lên máy `7f4ca06a`.

## [DONE] Task: 3D Spatial Holographic Cards & Golden Coin Illustration on Hero Card

**Ngày hoàn thành:** 2026-08-20
**Nhánh git:** `feature/prism-ui-theme`
**Mục tiêu & Kết quả xử lý:**
1. ✅ **Thiết kế đồ họa không gian 3D Hologram (`PrismWallet3DIllustration`):**
   - Thay thế các khung wireframe cũ bằng cụm **thẻ ngân hàng 3D không gian (Spatial Layering)** có góc nghiêng 3D thực tế (`graphicsLayer`).
   - Thẻ Hologram Cyan Cyber phía sau có chip EMV vàng và sóng chạm không tiếp xúc.
   - Thẻ Frosted Liquid Glass phía trước có hiệu ứng kính mờ ánh kim, viền sáng trắng và biểu tượng VIP kép.
   - Đồng xu vàng 3D nổi (`₫`) đổ bóng ánh kim rực rỡ ở góc tiền cảnh.
   - Ngôi sao lấp lánh (Sparkle accent) và hào quang tỏa sáng (Aura radial gradient) phía sau.
2. ✅ **Build & Install:** Đã biên dịch thành công và cài đặt lên máy `7f4ca06a`.

## [DONE] Task: Sticky Fixed Top Header on PrismHomeScreen

**Ngày hoàn thành:** 2026-08-20
**Nhánh git:** `feature/prism-ui-theme`
**Mục tiêu & Kết quả xử lý:**
1. ✅ **Cố định Top Header khi cuộn trang:** Đưa `PrismHomeTopHeader` ("Xin chào 👋", Tên người dùng, Chuông thông báo có chấm đỏ, Avatar) vào `topBar` của `Scaffold` với background đồng nhất màu nền, giúp thanh header luôn ghim cố định ở đỉnh màn hình khi người dùng vuốt cuộn lên xuống danh sách thẻ tài sản và giao dịch.
2. ✅ **Build & Install:** Đã build thành công và cài đặt lên máy `7f4ca06a`.

## [DONE] Task: Pixel-Perfect Transaction Detail Bottom Sheet & Action Redesign

**Ngày hoàn thành:** 2026-08-20
**Nhánh git:** `feature/prism-ui-theme`
**Mục tiêu & Kết quả xử lý:**
1. ✅ **Thiết kế chuẩn xác theo ảnh mẫu (`TransactionDetailSheet.kt`):**
   - Thanh kéo (Drag handle) chuẩn trên đỉnh.
   - Header có icon danh mục bo góc mềm mại + Tiêu đề "Chi tiết giao dịch" + Nút đóng tròn (✕).
   - Thẻ số tiền nổi bật (Hero Card) có gradient nền dịu nhẹ, tag pill chấm tròn "• Khoản chi tiêu" / "• Khoản thu nhập", số tiền cỡ lớn (32sp Bold) màu sắc ngữ nghĩa.
   - Khung thông tin 4 hàng (Danh mục, Ví thanh toán, Thời gian, Ghi chú) kèm icon vuông bo góc và divider thanh mảnh.
   - 2 Thẻ hành động đặt cạnh nhau (Side-by-side): ✏️ *Chỉnh sửa giao dịch* và 🗑️ *Xóa giao dịch này* (kèm subtitle mô tả + mũi tên chevron).
   - Footer bảo mật: Icon khiên + "Giao dịch được bảo mật tuyệt đối".
2. ✅ **Build & Install:** Đã biên dịch thành công và cài đặt lên máy `7f4ca06a`.

## [DONE] Task: Fix UI Report Issues from Real Device Testing (docs/Report)

**Ngày hoàn thành:** 2026-08-20
**Nhánh git:** `feature/prism-ui-theme`
**Mục tiêu & Kết quả xử lý:**
1. ✅ **Status Bar Padding:** Đã thêm `statusBarsPadding()` vào `FinluxScreenHeader` và `GlassTopBar`, loại bỏ hoàn toàn hiện tượng tai thỏ / status bar hệ thống đè lên header trên toàn bộ các màn hình (Báo cáo, Lịch sử, Ví, Ngân sách, Cài đặt).
2. ✅ **Progress Bar Dot Artifact:** Thay thế `LinearProgressIndicator` của Material 3 bằng rounded Box progress bar trên cả `PrismReportsScreen` và `PrismBudgetScreen`, loại bỏ dấu chấm tròn thừa ở đuôi thanh tiến độ.
3. ✅ **Prism Settings Screen:** Tạo `PrismSettingsScreen.kt` chuyên biệt chuẩn Bento Data-First, hiển thị trực tiếp bộ chọn UI Theme Style (💎 FinLux Prism / ✨ Modern Luxury / 💧 Classic Liquid) trên màn hình chính của Cài đặt.
4. ✅ **Metric Card Typography:** Tăng kích thước và độ đậm của font số liệu trong `FinluxMetricCard` (19sp Bold) giúp các chỉ số thu chi dễ nhìn và nổi bật hơn.

### Kết quả kiểm thử
- `testDebugUnitTest`: **PASS 100%** (Tất cả unit tests đều vượt qua).
- `packageDebug`: **BUILD SUCCESSFUL** (Đã tạo file APK mới nhất tại `app/build/outputs/apk/debug/app-debug.apk`).

### Danh sách file đã chỉnh sửa / tạo mới:
- [FinluxHeaderComponents.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/core/designsystem/component/FinluxHeaderComponents.kt) `[MODIFIED]`
- [FinluxCardComponents.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/core/designsystem/component/FinluxCardComponents.kt) `[MODIFIED]`
- [LiquidGlass.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/core/designsystem/LiquidGlass.kt) `[MODIFIED]`
- [PrismReportsScreen.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/reports/prism/PrismReportsScreen.kt) `[MODIFIED]`
- [PrismBudgetScreen.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/budget/prism/PrismBudgetScreen.kt) `[MODIFIED]`
- [PrismSettingsScreen.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/settings/prism/PrismSettingsScreen.kt) `[NEW]`
- [SettingsScreen.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/settings/SettingsScreen.kt) `[MODIFIED]`


## [DONE] Task: Finlux Prism UI Theme & Design System Implementation

**Ngày hoàn thành:** 2026-08-20
**Nhánh git:** `feature/prism-ui-theme`

### Tiến độ các Phase:
- [x] **Phase UI-1 — Design Tokens**:
  - `FinluxColors`: Tokens màu Primary (`PrimaryBlue`, `PrimaryViolet`, `PrimaryCyan`), Semantic (`IncomeGreen`, `ExpenseRed`, `TransferBlue`, `BudgetViolet`, `WarningAmber`, `NeutralGray`), Surface Light/Dark (`BackgroundLight`, `BackgroundDark`, `SurfacePrimary`, `SurfaceSoft`, `SurfaceGlass`, `BorderSoft`).
  - `FinluxSpacing`, `FinluxRadius`, `FinluxMotion`.
  - `FinluxTextStyles` & `FinluxTypography`: Chuẩn hóa Display Amount (36sp), Screen Title (28sp), Section Title (20sp), Card Title (16sp), Body (15sp), Caption (12.5sp), Micro Label (11.5sp).
  - `FinluxDesignTokens`: Hợp nhất contract tokens cho Prism, Classic Liquid, Modern Luxury qua `LocalFinluxTokens`, `LocalFinluxSpacing`, `LocalFinluxRadius`.
- [x] **Phase UI-2 — Shared Components Foundation**:
  - `FinluxScreenHeader`, `FinluxSectionHeader`.
  - `FinluxSoftCard`, `FinluxHeroCard`, `FinluxMetricCard`, `FinluxInsightCard`.
  - `FinluxAmountText`, `formatVndAmount`, `getTransactionSemanticColor`, `FinluxFilterChip`, `FinluxTransactionRow`.
  - `FinluxBottomSheet`, `FinluxDialog`.
  - `FinluxEmptyState`, `FinluxErrorState`, `FinluxOfflineState`.
  - `FinluxBottomDock`, `FinluxCenterFab`.
- [x] **Phase UI-3 — Theme Switching (`AppUiStyle.PRISM`)**:
  - Thêm `AppUiStyle.PRISM` vào model enum và DataStore preference.
  - Cập nhật `FinluxTheme.kt` mapping Prism colors/tokens.
  - Cập nhật `SettingsScreen.kt` với Card chọn "FinLux Prism (Data-first + Spatial + Bento)".
- [x] **Phase UI-4 — Navigation & Bottom Dock**:
  - Tích hợp `FinluxBottomDock` trong `MainBottomBar.kt` khi chọn UI Style PRISM.
- [x] **Phase UI-5 — Home / Dashboard Screen**:
  - Tạo `PrismHomeScreen.kt` chuẩn Bento grid: Hero net worth, Income/Expense cards, Quick actions, AI insight, Recent transaction list.
  - Tích hợp routing trong `HomeScreen.kt`.
- [x] **Phase UI-6 — Transactions Screen + Detail**:
  - Tạo `PrismTransactionsScreen.kt`: Filter chip bar (Tất cả, Thu, Chi), Bento summary card, Semantic transaction rows, Transaction Detail sheet, Action & Delete confirmation dialogs.
  - Tích hợp routing trong `TransactionsScreen.kt`.
- [x] **Phase UI-7 — Wallets & Budget Screens**:
  - Tạo `PrismWalletsScreen.kt`: Total assets hero, Add wallet & Transfer bottom sheets, List ví với type icon & color, Delete dialog.
  - Tạo `PrismBudgetScreen.kt`: Month navigation, Remaining budget hero, Category budget list với dynamic spent tracking & progress indicator, Add/edit budget bottom sheet.
  - Tích hợp routing trong `WalletsScreen.kt` & `BudgetScreen.kt`.
- [x] **Phase UI-8 — Reports Screen & Consistency Polish**:
  - Tạo `PrismReportsScreen.kt`: Period selector chips (Tháng này, Tháng trước, 3 tháng...), Net cash flow hero, Income vs Expense metrics, AI insight, Category breakdown bento blocks với progress bars, Export report dialog.
  - Tích hợp routing trong `ReportsScreen.kt`.

### Kết quả kiểm thử toàn diện
- `testDebugUnitTest`: **PASS 100%** (Tất cả unit test đều vượt qua).
- `assembleDebug`: **BUILD SUCCESSFUL** (Đóng gói APK debug thành công không lỗi).

### Danh sách file đã tạo / chỉnh sửa:
- [FinluxTokens.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/core/designsystem/theme/FinluxTokens.kt) `[NEW]`
- [Typography.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/core/designsystem/Typography.kt) `[MODIFIED]`
- [FinluxTheme.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/core/designsystem/FinluxTheme.kt) `[MODIFIED]`
- [FinanceModels.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/domain/model/FinanceModels.kt) `[MODIFIED]`
- [FinluxHeaderComponents.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/core/designsystem/component/FinluxHeaderComponents.kt) `[NEW]`
- [FinluxCardComponents.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/core/designsystem/component/FinluxCardComponents.kt) `[NEW]`
- [FinluxTransactionComponents.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/core/designsystem/component/FinluxTransactionComponents.kt) `[NEW]`
- [FinluxModalComponents.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/core/designsystem/component/FinluxModalComponents.kt) `[NEW]`
- [FinluxFeedbackComponents.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/core/designsystem/component/FinluxFeedbackComponents.kt) `[NEW]`
- [FinluxNavigationComponents.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/core/designsystem/component/FinluxNavigationComponents.kt) `[NEW]`
- [FinluxDesignSystemTest.kt](file:///d:/BT/FinLux/app/src/test/java/com/finlux/app/core/designsystem/FinluxDesignSystemTest.kt) `[NEW]`
- [MainBottomBar.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/components/MainBottomBar.kt) `[MODIFIED]`
- [HomeScreen.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/home/HomeScreen.kt) `[MODIFIED]`
- [PrismHomeScreen.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt) `[NEW]`
- [TransactionsScreen.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/transaction/TransactionsScreen.kt) `[MODIFIED]`
- [PrismTransactionsScreen.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/transaction/prism/PrismTransactionsScreen.kt) `[NEW]`
- [WalletsScreen.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/wallet/WalletsScreen.kt) `[MODIFIED]`
- [PrismWalletsScreen.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/wallet/prism/PrismWalletsScreen.kt) `[NEW]`
- [BudgetScreen.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/budget/BudgetScreen.kt) `[MODIFIED]`
- [PrismBudgetScreen.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/budget/prism/PrismBudgetScreen.kt) `[NEW]`
- [ReportsScreen.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/reports/ReportsScreen.kt) `[MODIFIED]`
- [PrismReportsScreen.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/reports/prism/PrismReportsScreen.kt) `[NEW]`
- [SettingsScreen.kt](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/presentation/settings/SettingsScreen.kt) `[MODIFIED]`

---

## [DONE] Task: Finlux v1.8.5 Security & Release Hardening Master Plan

**Ngày hoàn thành:** 2026-08-19

### Mục tiêu đã hoàn thành
1. **P0-S01: Firestore Rules Hardening**:
   - Xóa bỏ hoàn toàn wildcard write bypass (`match /{subcollection}/{docId}`) ngăn chặn tuyệt đối việc ghi tài liệu không qua kiểm duyệt.
   - Chuyển sang mô hình **Default Deny + Explicit Allow** cho toàn bộ 7 subcollections: `transactions`, `wallets`, `budgets`, `categories`, `goals`, `reminders`, `notifications`.
   - Thực thi schema validation: `amount > 0` (int), type string/timestamp/bool, giới hạn giá trị tiền không âm cho ngân sách (`limitAmount >= 0`).
2. **P0-S02: Production Release Signing Keystore**:
   - Tách biệt `signingConfigs.release` khỏi keystore debug, cấu hình nạp an toàn từ biến môi trường/CI Secrets (`FINLUX_KEYSTORE_PATH`, `FINLUX_KEYSTORE_PASSWORD`, `FINLUX_KEY_ALIAS`, `FINLUX_KEY_PASSWORD`).
3. **P0-S03: Tách Biệt CI Kiểm Thử & CI Phát Hành Tagged Release**:
   - Tạo workflow `.github/workflows/ci.yml` chỉ chạy kiểm thử unit test & lint trên PR/push `main`.
   - Cập nhật `.github/workflows/release.yml` chỉ phát hành release khi gắn tag `v*`, tạo checksum SHA-256 và `update.json` cho OTA.
4. **P0-S04: Xác Thực Toàn Vẹn OTA (Integrity Verification Chain)**:
   - Bổ sung xác thực mã băm SHA-256, so khớp `versionCode`, kiểm tra `packageName` và xác thực chữ ký số certificate của APK trước khi mở cài đặt.
5. **P0-T01: Deterministic Time in Tests**:
   - Thay thế toàn bộ `Instant.now()`/`Timestamp.now()` bằng fixed instant (`2026-08-15T03:00:00Z`).
6. **P0-T02: Complete Transaction Test Matrix**:
   - Bổ sung kiểm thử biên: zero amount, negative amount, max money limit, reversing balance, wallet transfer balance checks.
7. **P1-TZ01: Account Finance Timezone Strategy**:
   - Bổ sung `FinanceClock` interface và chuẩn hóa múi giờ `Asia/Ho_Chi_Minh`.

### Kết quả kiểm thử
- `testDebugUnitTest`: **63/63 PASS (100%)**
- `assembleRelease`: **BUILD SUCCESSFUL**

### Danh sách file đã chỉnh sửa
- `firestore.rules`
- `app/build.gradle.kts`
- `.github/workflows/ci.yml` (New)
- `.github/workflows/release.yml`
- `app/src/main/java/com/finlux/app/core/time/FinanceTime.kt`
- `app/src/main/java/com/finlux/app/core/updater/AppUpdateManager.kt`
- `app/src/main/java/com/finlux/app/presentation/updater/AppUpdateViewModel.kt`
- `app/src/test/java/com/finlux/app/core/updater/AppUpdateManagerTest.kt`
- `app/src/test/java/com/finlux/app/data/remote/firebase/FirebaseTransactionRepositoryTest.kt`
- `docs/FINLUX_V1.8.5_AI_FIX_MASTER_PLAN.md`
- `CHANGELOG.md`
- `HANDOVER_LOG.md`

---

## [DONE] Task: In-App Auto-Update & GitHub Actions CI/CD Auto-Release

**Ngày hoàn thành:** 2026-08-19

### Mục tiêu đã hoàn thành
1. **GitHub Actions CI/CD Workflow (`.github/workflows/release.yml`)**:
   - Tự động chạy toàn bộ unit test khi đẩy code lên nhánh `main`.
   - Tự động build APK release/debug.
   - Tự động tạo GitHub Release với tag version chuẩn (`vX.Y.Z`), phát hành file `FinLux-vX.Y.Z.apk` và trích xuất changelog mô tả cập nhật tự động từ `CHANGELOG.md`.
2. **Hệ thống tự động phát hiện và cập nhật trong app (`AppUpdateManager.kt` & `AppUpdateViewModel.kt`)**:
   - Tự động kiểm tra phiên bản mới từ GitHub Releases API (`khoaiprovip123/FinLux`) ngay khi mở app và qua nút kiểm tra thủ công.
   - Tải file APK trực tiếp trong ứng dụng kèm hiển thị thanh tiến trình download mượt mà.
   - Tự động mở trình cài đặt Android (`ACTION_VIEW` qua `FileProvider`) để cập nhật app trực tiếp trên điện thoại mà không cần thao tác phức tạp.
3. **Giao diện cập nhật Liquid Glass (`AppUpdateDialog.kt`) & Menu Cài đặt**:
   - Modal thông báo phiên bản mới, tính năng nổi bật, tiến trình tải và nút cài đặt ngay.
   - Nút "Kiểm tra bản cập nhật mới" trong mục Giới thiệu của `SettingsScreen`.

### Kết quả kiểm thử
- `testDebugUnitTest`: **51/51 PASS (100%)**
- `assembleDebug`: **BUILD SUCCESSFUL**

### Danh sách file đã chỉnh sửa
- `.github/workflows/release.yml` (New)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/xml/file_paths.xml`
- `app/src/main/java/com/finlux/app/core/updater/AppUpdateManager.kt` (New)
- `app/src/main/java/com/finlux/app/presentation/updater/AppUpdateViewModel.kt` (New)
- `app/src/main/java/com/finlux/app/presentation/updater/AppUpdateDialog.kt` (New)
- `app/src/main/java/com/finlux/app/presentation/settings/SettingsScreen.kt`
- `app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt`
- `app/src/test/java/com/finlux/app/core/updater/AppUpdateManagerTest.kt` (New)

---

## [DONE] Task: Batch 3 - P1 Architecture Hardening (Split God Repository)

**Ngày hoàn thành:** 2026-08-19

### Mục tiêu đã hoàn thành
1. **P1-01 (Split FirebaseReadRepository)**: Phân tách hoàn toàn god class `FirebaseReadRepository` thành 7 repository độc lập, tuân thủ Single Responsibility:
   - `FirebaseWalletRepository.kt`
   - `FirebaseCategoryRepository.kt`
   - `FirebaseBudgetRepository.kt`
   - `FirebaseReminderRepository.kt`
   - `FirebaseGoalRepository.kt`
   - `FirebaseNotificationRepository.kt`
   - `FirebaseDashboardRepository.kt` (tích hợp chuẩn `FinanceTime`)
2. Cập nhật `RepositoryModule.kt` inject độc lập từng repository riêng biệt.
3. Xóa bỏ hoàn toàn file god `FirebaseReadRepository.kt`.

### Kết quả kiểm thử
- `testDebugUnitTest`: **48/48 PASS (100%)**
- Build APK: Thành công và đã nạp trực tiếp lên máy (`7f4ca06a`)

### Danh sách file đã chỉnh sửa
- `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseWalletRepository.kt` (New)
- `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseCategoryRepository.kt` (New)
- `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseBudgetRepository.kt` (New)
- `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseReminderRepository.kt` (New)
- `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseGoalRepository.kt` (New)
- `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseNotificationRepository.kt` (New)
- `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseDashboardRepository.kt` (New)
- `app/src/main/java/com/finlux/app/data/di/RepositoryModule.kt`
- `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseReadRepository.kt` (Deleted)

---

## [DONE] Task: Batch 1 - P0 Data Integrity & Security Hardening

**Ngày hoàn thành:** 2026-08-19

### Mục tiêu đã hoàn thành
1. **P0-01 (Edit Transaction Stale Original)**: Sửa `FirebaseTransactionRepository.editWithBalanceUpdate` lấy `stored` từ Firestore làm authoritative source of truth cho `oldWalletRef`, `oldBudgetRef` và hoàn tiền budget theo `-stored.amount.value`.
2. **P0-02 (Delete Transaction Stale Object)**: Sửa `FirebaseTransactionRepository.deleteWithBalanceUpdate` chỉ tin `transaction.id`, derive toàn bộ wallet/budget rollback từ `stored`.
3. **P0-03 (Standardize Finance Timezone)**: Tạo `FinanceTime` chuẩn hóa múi giờ tài chính thống nhất (`Asia/Ho_Chi_Minh` / `systemDefault`), loại bỏ conflict UTC vs systemDefault giữa `budgetRef` và query giao dịch.
4. **P0-04, P0-05, P0-06 (Invariants, Safe Math & Unit Tests)**: Mở rộng `FirebaseTransactionRepositoryTest` và `FinanceTimeTest` kiểm thử đầy đủ các kịch bản stale caller, invariant add/edit/delete/transfer, và dùng `Math.addExact`/`subtractExact` chống tràn số `Long`.
5. **P0-07 (Firestore Security Rules Hardening)**: Cập nhật `firestore.rules` với validation kiểu dữ liệu, schema chuẩn và ràng buộc `isPositiveMoney(amount)`.

### Kết quả kiểm thử
- `testDebugUnitTest`: **48/48 PASS (100%)**
- Build APK: Thành công và đã nạp trực tiếp lên máy (`7f4ca06a`)

### Danh sách file đã chỉnh sửa
- `app/src/main/java/com/finlux/app/core/time/FinanceTime.kt` (New)
- `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseTransactionRepository.kt`
- `firestore.rules`
- `app/src/test/java/com/finlux/app/core/time/FinanceTimeTest.kt` (New)
- `app/src/test/java/com/finlux/app/data/remote/firebase/FirebaseTransactionRepositoryTest.kt`

---

## [DONE] Task: Transaction Detail Modal, Long-Press Actions Popup, and Bottom History Tab (v1.8.2)

**Ngày hoàn thành:** 2026-08-19

### Mục tiêu đã hoàn thành
1. **Chạm đơn (Single-tap) vào giao dịch:** Mở giao diện xem Chi tiết giao dịch (`TransactionDetailSheet`) dạng Liquid Glass hiển thị chi tiết số tiền, danh mục, ví, ngày giờ, ghi chú, hóa đơn đính kèm kèm 2 nút hành động "Sửa" và "Xóa" (có dialog xác nhận an toàn).
2. **Bấm giữ (Long-press) vào giao dịch:** Mở pop-up tùy chọn nhanh (`TransactionActionDialog`) gồm: "Xem chi tiết", "Sửa giao dịch", "Xóa giao dịch" kèm dialog xác nhận xóa và hoàn tiền số dư ví (`DeleteTransactionConfirmDialog`).
3. **Thanh điều hướng dưới (Bottom Navigation):** Thay thế tab "Ví" thành tab "Lịch sử" (`Route.Transactions`, label: "Lịch sử", icon: `ReceiptLong`) để xem lại toàn bộ lịch sử thu/chi, hỗ trợ swipe gestures và bộ lọc Tất cả / Thu / Chi.

### Kết quả kiểm thử
- `testDebugUnitTest`: **46/46 PASS (100%)**
- Build APK: Thành công tại `app/build/outputs/apk/debug/app-debug.apk`

### Danh sách file đã chỉnh sửa
- `app/src/main/java/com/finlux/app/presentation/transaction/TransactionDetailSheet.kt` (New)
- `app/src/main/java/com/finlux/app/presentation/transaction/TransactionsViewModel.kt`
- `app/src/main/java/com/finlux/app/presentation/transaction/TransactionsScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/transaction/classic/ClassicTransactionsScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/transaction/modern/ModernTransactionsScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/components/classic/ClassicMainBottomBar.kt`
- `app/src/main/java/com/finlux/app/presentation/components/modern/ModernMainBottomBar.kt`
- `app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt`
- `app/src/main/java/com/finlux/app/presentation/home/HomeScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/home/classic/ClassicHomeScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/home/modern/ModernHomeScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/income/IncomeScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/expense/ExpenseScreen.kt`
- `app/src/main/java/com/finlux/app/core/designsystem/LiquidGlass.kt`
- `app/src/main/java/com/finlux/app/core/designsystem/modern/ModernLiquidGlass.kt`
- `app/src/test/java/com/finlux/app/core/navigation/MainSwipeNavigationTest.kt`
- `app/src/test/java/com/finlux/app/presentation/transaction/TransactionsViewModelTest.kt`
- `app/build.gradle.kts`
- `CHANGELOG.md`

---

## [DONE] Task: Long-Press & Button-Only Edit Trigger (Chỉ mở sửa khi bấm giữ hoặc bấm nút sửa)

**Ngày hoàn thành:** 2026-08-19

### Mục tiêu Hoàn Thành
- Thay đổi hành vi kích hoạt sửa giao dịch: Chỉ mở form sửa khi người dùng **bấm giữ (long-press)** vào giao dịch hoặc **bấm nút Sửa (icon Edit)**.
- Loại bỏ mở form sửa khi chạm đơn (single tap) để tránh người dùng vô tình chạm nhầm mở form sửa.
- Đồng bộ trên các màn hình: Home (Giao dịch gần đây), Transactions (Danh sách giao dịch Classic & Modern), Income (Thu nhập), Expense (Chi tiêu).
- Bổ sung hỗ trợ `onLongClick` trực tiếp vào component thiết kế chung [GlassCard](file:///d:/BT/FinLux/app/src/main/java/com/finlux/app/core/designsystem/LiquidGlass.kt) (Classic & Modern).

### Scope và file thực tế chỉnh sửa
- `app/src/main/java/com/finlux/app/core/designsystem/LiquidGlass.kt`
- `app/src/main/java/com/finlux/app/core/designsystem/modern/ModernLiquidGlass.kt`
- `app/src/main/java/com/finlux/app/presentation/home/classic/ClassicHomeScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/home/modern/ModernHomeScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/transaction/classic/ClassicTransactionsScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/transaction/modern/ModernTransactionsScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/income/IncomeScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/expense/ExpenseScreen.kt`

### Kết quả kiểm thử
- `gradlew testDebugUnitTest`: **100% PASS** (BUILD SUCCESSFUL).
- `gradlew assembleDebug` + `build_and_install.ps1`: **Cài đặt thành công lên thiết bị ADB `7f4ca06a`**.

---

## [DONE] Task: Fix Transaction Edit & Adjustment Capability (Sửa/Điều chỉnh giao dịch thu chi)

**Ngày hoàn thành:** 2026-08-19

### Mục tiêu Hoàn Thành
- Sửa lỗi không điều chỉnh/chỉnh sửa được giao dịch thu/chi sau khi đã thêm vào hệ thống.
- Tích hợp `EditTransactionUseCase` vào `AddTransactionViewModel`, thêm `setEditingTransaction(tx)` và xử lý update giao dịch trong `save()` theo chuẩn Firestore Transaction / Clean Architecture.
- Bổ sung chế độ chỉnh sửa trong `AddTransactionSheet` (tiêu đề "Sửa giao dịch", nút "Lưu thay đổi", tự động fill toàn bộ thông tin: loại thu/chi, số tiền, danh mục, ví, ghi chú, ngày, hóa đơn).
- Cho phép người dùng chạm vào bất kỳ giao dịch nào hoặc bấm icon Sửa (Edit) tại các màn hình: Home (Giao dịch gần đây), Transactions (Danh sách giao dịch Classic & Modern), Income (Thu nhập), Expense (Chi tiêu).

### Scope và file thực tế chỉnh sửa
- `app/src/main/java/com/finlux/app/presentation/transaction/AddTransactionViewModel.kt`
- `app/src/main/java/com/finlux/app/presentation/transaction/AddTransactionSheet.kt`
- `app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt`
- `app/src/main/java/com/finlux/app/presentation/home/HomeScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/home/classic/ClassicHomeScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/home/modern/ModernHomeScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/transaction/TransactionsScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/transaction/classic/ClassicTransactionsScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/transaction/modern/ModernTransactionsScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/income/IncomeScreen.kt`
- `app/src/main/java/com/finlux/app/presentation/expense/ExpenseScreen.kt`
- `app/src/test/java/com/finlux/app/presentation/transaction/AddTransactionViewModelTest.kt`

### Kết quả kiểm thử
- `gradlew testDebugUnitTest`: **100% PASS** (BUILD SUCCESSFUL).
- `gradlew assembleDebug`: **BUILD SUCCESSFUL**.

---

## [DONE] Task: Remove Auto VersionCode Bump from build_and_install.ps1

**Ngày:** 2026-08-17

### Mục tiêu
1. **Loại bỏ tính năng tự động tăng `versionCode` khi chạy script `build_and_install.ps1`:**
   - Xóa logic tự động sửa file `app/build.gradle.kts` khi build debug & install qua ADB.
   - Giữ nguyên thông tin hiển thị version hiện tại (`v$versionName (versionCode $versionCode)`) để tiện theo dõi mà không làm thay đổi code/file.

### Kết quả & Danh sách file đã chỉnh sửa
- **Danh sách file thay đổi:**
  - `scripts/build_and_install.ps1`: Xóa bỏ đoạn regex ghi đè `versionCode = $newCode`, chỉ đọc và hiển thị phiên bản hiện tại từ `app/build.gradle.kts`.
- **Trạng thái:** Hoàn tất, khi chạy `.\scripts\build_and_install.ps1` không còn tự động tăng `versionCode`.

---

## [DONE] Task: Reorganize Root Files into Dedicated Directories

**Ngày hoàn thành:** 2026-08-17

### Mục tiêu
- Gom nhóm và di chuyển các tệp tài liệu đặc tả, kịch bản thực thi và build artifact nằm ở thư mục gốc vào các thư mục chuyên biệt (`docs/`, `scripts/`, `artifacts/`).
- Cập nhật toàn bộ các liên kết tài liệu tham chiếu trong `AGENTS.md`, `README.md`, `docs/CONTEXT.md` tương ứng.
- Đảm bảo các script và môi trường build/test hoạt động bình thường trên nền tảng v1.8.0.

### Scope và file thực tế chỉnh sửa
- Di chuyển sang `docs/`: `docs/BA_SPEC.md`, `docs/UI_SPEC.md`, `docs/DATA_SPEC.md`, `docs/CONTEXT.md`, `docs/PLAN.md`, `docs/BACKLOG.md`, `docs/PROJECT_PROFILE.md`
- Di chuyển sang `scripts/`: `scripts/build_and_install.bat`, `scripts/build_and_install.ps1`
- Cập nhật liên kết tham chiếu: `AGENTS.md`, `README.md`, `docs/CONTEXT.md`
- Cập nhật script root path: `scripts/build_and_install.bat`, `scripts/build_and_install.ps1`
- Cập nhật `.gitignore`: Bổ sung bỏ qua `graphify-out/cache/` và `graphify-out/20*/`

### Kết quả kiểm thử
- `gradlew testDebugUnitTest`: **100% PASS** (BUILD SUCCESSFUL).
- `gradlew assembleDebug`: **BUILD SUCCESSFUL**.

---

## [DONE] Task v1.8.0: Quad Feature Release (Transfer Validation, Reports Export, Multi-type Notifications, Biometric Lock)

**Ngày:** 2026-08-15

### Mục tiêu Hoàn Thành
1. **Khắc phục lỗi Lưu Ví Tiền Mặt vào Database (Cash Wallet Persistence):**
   - Tự động seed và lưu vĩnh viễn ví "Tiền mặt" (id: `cash`, `isDefault = true`) trên Firestore khi người dùng mở ứng dụng lần đầu hoặc database trống.
   - Bổ sung `parseWalletType` phòng chống lỗi ép kiểu enum làm rớt dữ liệu ví.
   - Cập nhật batch update trạng thái `isDefault` nguyên tử trên cả Firestore và Demo Repository.
2. **Transfer Money Validation & Ratio Calculation Fix:**
   - Thêm ràng buộc số dư ví nguồn ở Domain (`TransferMoneyUseCase`), Data (`DemoFinluxRepository`, `FirebaseTransactionRepository`) và UI (`TransferEditor`).
   - Sửa công thức tính % tỷ trọng an toàn tránh số âm.
2. **Xuất Báo Cáo Excel (.csv) & PDF (UC-17):**
   - Triển khai `ReportExporter.kt` sinh file Excel/CSV chuẩn UTF-8 BOM và file PDF qua `android.graphics.pdf.PdfDocument`.
   - Tạo `ExportReportDialog.kt` trên cả 2 giao diện Classic và Modern.
3. **Trung Tâm Thông Báo Đa Năng & Deep Link (Task v1.6.0):**
   - Thêm `NotificationType.kt`, mở rộng `AppNotification.kt` với các trường phân loại và route.
   - Thêm Filter Tabs và Deep Link Navigation trên `NotificationsScreen.kt`.
4. **Bảo Mật Sinh Trắc Học (Biometric Lock):**
   - Tích hợp `androidx.biometric:biometric`, chuyển `MainActivity` sang `FragmentActivity`.
   - Tạo `BiometricHelper.kt`, `BiometricLockScreen` trong `FinluxRoot.kt` và toggle trong `SettingsScreen.kt`.

---

## [DONE] Task v1.7.7: Fix SettingsScreen Theme Inconsistency & Contrast

**Ngày:** 2026-08-15

### Mục tiêu
1. **Đồng bộ Màu Nền & Design System cho SettingsScreen:**
   - Loại bỏ các màu tím tối/dark mode hardcode trong `SettingsScreen.kt`.
   - Sử dụng `FinluxStyleBackdrop` / `ModernStyleBackdrop` tự động thích ứng với UI Style (Classic vs Modern) và Theme (Light / Dark mode).
2. **Khắc Phục Tương Phản TopBar & Menu Settings:**
   - Tiêu đề "Hồ sơ & Cài đặt", icon Back, các nhãn menu: Dùng `MaterialTheme.colorScheme.onSurface` / `onBackground` hiển thị sắc nét trên cả nền Sáng và Tối.
3. **Tối Ưu Thẻ Profile Hero & Tên User:**
   - Cho phép co giãn hoặc hiển thị tên người dùng đầy đủ không bị cắt cụt (`maxLines = 2`).
   - Thẻ Profile Hero và các shortcut (Ví, Ngân sách, Danh mục, Nhắc nhở) đồng bộ phong cách Liquid Glass với `HomeScreen` và `WalletsScreen`.
4. **SOP Compliance:**
   - Chạy `gradlew testDebugUnitTest` đảm bảo 100% PASS (39/39 tests).
   - Tăng `versionCode` lên 92, bump `versionName v1.7.7`.
   - Cập nhật `CHANGELOG.md` và `HANDOVER_LOG.md` sang `[DONE]`.
   - Chạy `build_and_install.ps1` nạp APK lên điện thoại.

### Kết quả & Danh sách file đã chỉnh sửa
- **Kết quả Unit Tests:** 39/39 tests PASS 100% (`.\gradlew.bat testDebugUnitTest` hoàn tất trong 16s).
- **Danh sách file thay đổi:**
  - `app/src/main/java/com/finlux/app/presentation/settings/SettingsScreen.kt`: Bọc backdrop tự động theo UI Style, nâng cấp ProfileHero và ProfileFeatureTiles đồng bộ tương phản Liquid Glass.
  - `app/build.gradle.kts`: Bump `versionCode = 92`, `versionName = "1.7.7"`.
  - `CHANGELOG.md`: Thêm mục release v1.7.7.

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.7.6: Fix Swipe-to-Delete Translucent Ghosting Trash Icon

**Ngày:** 2026-08-15

### Mục tiêu
1. **Ẩn 100% Background & Icon Thùng Rác khi Thẻ Đứng Yên (Zero Ghosting):**
   - Trong `SwipeToDismissBox.backgroundContent`: Kiểm tra `dismissDirection == SwipeToDismissBoxValue.EndToStart && canDelete`.
   - Khi thẻ ở vị trí bình thường (`Settled`), `backgroundContent` hoàn toàn trong suốt / không render bất kỳ element nào, đảm bảo 100% không bị nhìn xuyên thấu qua lớp kính Liquid Glass.
   - Khi bắt đầu vuốt: Tăng dần độ mờ `alpha` theo quãng đường vuốt (`dismissState.progress`) và phóng to nhẹ icon thùng rác (`graphicsLayer`).
2. **Kiểm Tra & Tăng Độ Tương Phản Mặt Trước (Foreground Card):**
   - Đảm bảo thẻ ví sử dụng `GlassCard` hiển thị sạch sẽ, cột bên phải chỉ có Số tiền và % tỷ lệ, không còn bất kỳ icon rác trần nào trong cây UI mặt trước.
3. **SOP Compliance:**
   - Áp dụng đồng bộ cho cả `ModernWalletsScreen.kt` và `ClassicWalletsScreen.kt`.
   - `gradlew testDebugUnitTest` PASS 100% (39/39 tests).
   - Bump version lên `v1.7.6`.
   - Cập nhật `CHANGELOG.md` và `HANDOVER_LOG.md` sang `[DONE]`.
   - Chạy `build_and_install.ps1` nạp APK lên điện thoại.

### Kết quả & Danh sách file đã chỉnh sửa
- **Kết quả Unit Tests:** 39/39 tests PASS 100% (`.\gradlew.bat testDebugUnitTest` hoàn tất trong 11s).
- **Danh sách file thay đổi:**
  - `app/src/main/java/com/finlux/app/presentation/wallet/modern/ModernWalletsScreen.kt`: Cập nhật dynamic rendering cho `backgroundContent` với `graphicsLayer` và `alpha`.
  - `app/src/main/java/com/finlux/app/presentation/wallet/classic/ClassicWalletsScreen.kt`: Cập nhật dynamic rendering tương tự cho Classic UI.
  - `app/build.gradle.kts`: Bump `versionCode = 82`, `versionName = "1.7.6"`.
  - `CHANGELOG.md`: Thêm mục release v1.7.6.

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.7.5: Restore Safe Swipe-to-Delete with Confirm Dialog

**Ngày:** 2026-08-15

### Mục tiêu
1. **Khôi phục Cử chỉ Vuốt Trái Xóa Ví (Swipe-to-Delete):**
   - Bọc mỗi thẻ ví trong `SwipeToDismissBox` với hướng vuốt `EndToStart` (Phải sang Trái).
   - Nền lộ ra khi vuốt: Màu đỏ `errorContainer` / đỏ mềm mại bo góc 20dp có icon `DeleteOutline`.
   - Cơ chế an toàn (Safety Trigger): Khi vuốt qua ngưỡng, tự động hoàn trả (reset) thẻ về vị trí cũ và hiển thị Dialog xác nhận: *"Bạn có chắc chắn muốn xóa ví [Tên ví]? Tất cả giao dịch thuộc ví này sẽ bị ảnh hưởng"*. Chỉ xóa khi người dùng bấm [Xóa vĩnh viễn].
2. **Khóa Cử Chỉ Vuốt Đối Với Ví Mặc Định & Ví Duy Nhất:**
   - Nếu `wallet.isDefault == true` hoặc danh sách ví chỉ còn 1 ví duy nhất: `enableDismissFromEndToStart = false` (khóa cứng cử chỉ, không trượt thẻ).
3. **Giữ Nguyên Bố Cục Thẻ Gọn Gàng:**
   - Thẻ ví ở trạng thái bình thường giữ nguyên cột bên phải sạch đẹp: Số tiền in đậm to rõ và Tỷ lệ % ngay bên dưới.
4. **SOP Compliance:**
   - Áp dụng đồng bộ cho cả `ModernWalletsScreen.kt` và `ClassicWalletsScreen.kt`.
   - Chạy `gradlew testDebugUnitTest` đảm bảo 100% PASS.
   - Bump version lên `v1.7.5` trong `build.gradle.kts`.
   - Cập nhật `CHANGELOG.md` và `HANDOVER_LOG.md` sang `[DONE]`.
   - Chạy `build_and_install.ps1` nạp APK lên điện thoại.

### Kết quả & Danh sách file đã chỉnh sửa
- **Kết quả Unit Tests:** 39/39 tests PASS 100% (`.\gradlew.bat testDebugUnitTest` hoàn tất trong 17s).
- **Danh sách file thay đổi:**
  - `app/src/main/java/com/finlux/app/presentation/wallet/modern/ModernWalletsScreen.kt`: Bọc thẻ ví bằng `SwipeToDismissBox`, thêm logic khóa vuốt cho ví mặc định/duy nhất và dialog xác nhận xóa khi gạt thẻ.
  - `app/src/main/java/com/finlux/app/presentation/wallet/classic/ClassicWalletsScreen.kt`: Áp dụng đồng bộ `SwipeToDismissBox` và dialog xác nhận.
  - `app/build.gradle.kts`: Bump `versionCode = 80`, `versionName = "1.7.5"`.
  - `CHANGELOG.md`: Thêm mục release v1.7.5.

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.7.4: Refine Wallet Card Layout & Safety UX

**Ngày:** 2026-08-15

### Mục tiêu
1. **Tinh chỉnh Thẻ Ví (Wallet Card Layout):**
   - Loại bỏ hoàn toàn icon thùng rác/nút xóa trần trên thẻ ví, loại bỏ `SwipeToDismissBox` gây dính cụm và vỡ layout.
   - Cột bên phải thẻ ví căn chỉnh sang trọng: Số tiền in đậm to rõ, Tỷ lệ % ngay bên dưới.
   - Khi bấm vào thẻ ví: Mở `GlassBottomSheet` Chi tiết & Chỉnh sửa ví (sửa tên, số dư, loại ví, màu thẻ, checkbox đặt làm mặc định).
2. **Chống Xóa Nhầm & Bảo Vệ Ví Mặc Định (Safety UX):**
   - Nút [Xóa ví] màu đỏ cảnh báo chỉ hiển thị ở đáy BottomSheet chi tiết ví khi chỉnh sửa.
   - Khi bấm Xóa ví: Hiện confirmation dialog "Bạn có chắc chắn muốn xóa ví này? Tất cả giao dịch thuộc ví sẽ bị ảnh hưởng".
   - Bảo vệ ví mặc định: Nếu `isDefault == true` hoặc là ví duy nhất còn lại, Disable nút Xóa và hiển thị thông báo "Không thể xóa ví mặc định. Vui lòng đặt ví khác làm mặc định trước khi xóa!".
3. **Tinh Chỉnh Padding Filter Chips:**
   - Dãy FilterChip loại ví có `contentPadding = PaddingValues(horizontal = 16.dp)` vuốt tràn lề mượt mà không dính mép.
4. **SOP Compliance:**
   - Cập nhật cả `ModernWalletsScreen.kt` và `ClassicWalletsScreen.kt`.
   - `gradlew testDebugUnitTest` PASS 100% (39/39 tests).
   - Bump version lên `v1.7.4`.
   - Cập nhật `CHANGELOG.md` và `HANDOVER_LOG.md` sang `[DONE]`.
   - Chạy `build_and_install.ps1` nạp APK lên điện thoại.

### Kết quả & Danh sách file đã chỉnh sửa
- **Kết quả Unit Tests:** 39/39 tests PASS 100% (`.\gradlew.bat testDebugUnitTest` hoàn tất trong 18s).
- **Danh sách file thay đổi:**
  - `app/src/main/java/com/finlux/app/presentation/wallet/modern/ModernWalletsScreen.kt`: Tinh chỉnh card layout, bỏ nút xóa trần, thêm switch ví mặc định, cảnh báo bảo vệ ví mặc định và dialog xác nhận xóa an toàn.
  - `app/src/main/java/com/finlux/app/presentation/wallet/classic/ClassicWalletsScreen.kt`: Tinh chỉnh đồng bộ layout thẻ ví, filter chip padding và safe delete UX.
  - `app/build.gradle.kts`: Bump `versionCode = 78`, `versionName = "1.7.4"`.
  - `CHANGELOG.md`: Thêm mục release v1.7.4.

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.7.3: Redesign Add & Transfer Wallet UI & Fix Dialog Text Bleed

**Ngày:** 2026-08-15

### Mục tiêu
- **Khắc phục triệt để lỗi giao diện "Thêm ví mới" & "Chuyển tiền":**
  - Chuyển `WalletEditor` và `TransferEditor` sang `GlassBottomSheet` hiện đại với scrim nền đen mờ bao phủ toàn màn hình, triệt tiêu 100% hiện tượng chữ/danh sách ví phía sau bị lộ xuyên qua.
  - Tăng độ phủ đặc `GlassDialogSurface` lên `0.98f` kết hợp viền tán sắc Chromatic Rim chống lóa và chống xuyên thấu.
  - Bổ sung bộ chọn nhanh số dư dạng Chip thông minh (`+500K`, `+1M`, `+2M`, `+5M`, `+10M` và `+100K`, `+200K`...).
  - Thiết kế bảng chọn màu ví trực quan với viền active và icon loại ví động (`CASH`, `BANK`, `EWALLET`, `CARD`, `INVESTMENT`).
  - Hỗ trợ phím tắt chuyển tiền thông minh ngay từ `QuickAddSheet` kết nối trực tiếp vào `WalletsScreen`.
- **SOP Compliance:**
  - `gradlew testDebugUnitTest` PASS 100% (39/39 tests).
  - Bump version lên `v1.7.3`.
  - Cập nhật CHANGELOG.md và HANDOVER_LOG.md [DONE].
  - Chạy `build_and_install.ps1` nạp APK lên thiết bị.

### Kết quả & Danh sách file đã chỉnh sửa
- **Kết quả Unit Tests:** 39/39 tests PASS 100% (`.\gradlew.bat testDebugUnitTest` hoàn tất trong 11s).
- **Danh sách file thay đổi:**
  - `app/src/main/java/com/finlux/app/core/designsystem/LiquidGlass.kt`: Cập nhật `GlassDialogSurface` đạt 98% độ đặc và viền rim.
  - `app/src/main/java/com/finlux/app/core/designsystem/modern/ModernLiquidGlass.kt`: Cập nhật `GlassDialogSurface` đạt 98% độ đặc và viền rim.
  - `app/src/main/java/com/finlux/app/presentation/wallet/modern/ModernWalletsScreen.kt`: Nâng cấp `WalletEditor` và `TransferEditor` sang `GlassBottomSheet` hiện đại.
  - `app/src/main/java/com/finlux/app/presentation/wallet/classic/ClassicWalletsScreen.kt`: Nâng cấp `WalletEditor` và `TransferEditor` sang `GlassBottomSheet`.
  - `app/src/main/java/com/finlux/app/presentation/wallet/WalletsScreen.kt`: Hỗ trợ `transferRequestKey` kích hoạt sheet chuyển tiền tức thì từ QuickAdd.
  - `app/build.gradle.kts`: Bump `versionCode = 76`, `versionName = "1.7.3"`.
  - `CHANGELOG.md`: Thêm mục release v1.7.3.

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.7.2: Restore True Modern UI Screens from commit 6535f24

**Ngày:** 2026-08-15

### Mục tiêu
- **Ráp đúng 100% Modern UI từ commit `6535f24`:**
  - `ModernHomeScreen.kt`: Hero balance card mới, Callstack Liquid Glass surfaces, quick metrics pill.
  - `ModernBudgetScreen.kt`: Progress cards, multi-layer blur, gradient summary.
  - `ModernReportsScreen.kt`: Modern analytics panels, spatial charts.
  - `ModernWalletsScreen.kt`: Modern wallet cards, swipe actions.
  - `ModernTransactionsScreen.kt`: Modern transaction rows, refined grouping.
- **Sử dụng đúng Modern Design System:**
  - Import và liên kết với `com.finlux.app.core.designsystem.modern.*`.
  - Chuẩn hóa toàn bộ text tiếng Việt sang UTF-8 sạch.
- **SOP Compliance:**
  - `gradlew testDebugUnitTest` PASS 100% (39/39 tests).
  - Bump version lên `v1.7.2`.
  - Cập nhật CHANGELOG.md và HANDOVER_LOG.md [DONE].
  - Chạy `build_and_install.ps1` nạp APK lên thiết bị.

### Kết quả & Danh sách file đã chỉnh sửa
- **Kết quả Unit Tests:** 39/39 tests PASS 100% (`.\gradlew.bat testDebugUnitTest` hoàn tất trong 16s).
- **Danh sách file thay đổi:**
  - `app/src/main/java/com/finlux/app/presentation/home/modern/ModernHomeScreen.kt`: Trích xuất và ráp đúng bố cục Hero Balance Card phát quang và Callstack Liquid Glass từ `6535f24`.
  - `app/src/main/java/com/finlux/app/presentation/budget/modern/ModernBudgetScreen.kt`: Ráp đúng Modern Budget progress cards từ `6535f24`.
  - `app/src/main/java/com/finlux/app/presentation/reports/modern/ModernReportsScreen.kt`: Ráp đúng Modern Reports charts và capsule selectors từ `6535f24`.
  - `app/src/main/java/com/finlux/app/presentation/wallet/modern/ModernWalletsScreen.kt`: Ráp đúng Modern Wallets cards từ `6535f24`.
  - `app/src/main/java/com/finlux/app/presentation/transaction/modern/ModernTransactionsScreen.kt`: Ráp đúng Modern Transactions list từ `6535f24`.
  - `app/build.gradle.kts`: Bump `versionCode = 71`, `versionName = "1.7.2"`.
  - `CHANGELOG.md`: Thêm mục release v1.7.2.

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.7.1: Fix Dual-UI Isolation, Overexposure & Settings Switcher

**Ngày:** 2026-08-15

### Mục tiêu
- **Tách biệt 100% Design System:**
  - Khôi phục nguyên bản Design System từ commit `280b722` vào `core/designsystem/` (`LiquidGlass.kt`, `StyleBackdrop.kt`, `WaterGlass.kt`, `FinluxComponents.kt`, `FinluxBrand.kt`).
  - Đặt các component Modern từ commit `6535f24` vào `core/designsystem/modern/` (`ModernLiquidGlass.kt`, `ModernStyleBackdrop.kt`, `ModernWaterGlass.kt`...).
  - Đảm bảo Classic UI không bị dính bất kỳ hiệu ứng glow/cháy sáng/thay đổi kích thước nào từ Modern UI.
- **Khôi phục hoàn toàn BottomBar:**
  - `ClassicMainBottomBar`: Đúng 100% giao diện thanh dock tiêu chuẩn từ `280b722`.
  - `ModernMainBottomBar`: Đúng phong cách Floating Dock pill từ `6535f24`.
- **Nâng cấp Settings UI Switcher:**
  - Thêm Card Cài đặt "Phong cách giao diện" có Subtitle hiển thị style hiện tại.
  - Mở BottomSheet chọn Radio trực quan với 2 phong cách kèm giải thích chi tiết.
- **SOP Compliance:**
  - Chạy `gradlew testDebugUnitTest` đạt 100% PASS (39/39 tests).
  - Bump version lên `v1.7.1` (versionCode 70).
  - Cập nhật CHANGELOG.md và HANDOVER_LOG.md [DONE].
  - Chạy `build_and_install.ps1` nạp APK lên thiết bị.

### Kết quả & Danh sách file đã chỉnh sửa
- **Kết quả Unit Tests:** 39/39 tests PASS 100% (`.\gradlew.bat testDebugUnitTest` hoàn tất trong 22s).
- **Danh sách file thay đổi:**
  - `app/src/main/java/com/finlux/app/core/designsystem/LiquidGlass.kt`, `StyleBackdrop.kt`, `WaterGlass.kt`, `FinluxComponents.kt`, `FinluxBrand.kt`, `NotificationPermissionHandler.kt`: Khôi phục 100% nguyên bản từ `280b722`.
  - `app/src/main/java/com/finlux/app/core/designsystem/FinluxTheme.kt`: Làm sạch tokens cho cả 2 chế độ Classic và Modern, sử dụng FinluxTypography gốc.
  - `app/src/main/java/com/finlux/app/core/designsystem/modern/...`: Đóng gói độc lập toàn bộ component modern.
  - `app/src/main/java/com/finlux/app/presentation/components/classic/ClassicMainBottomBar.kt`: Khôi phục 100% docked glass bar.
  - `app/src/main/java/com/finlux/app/presentation/components/modern/ModernMainBottomBar.kt`: Tinh chỉnh floating capsule pill bar với chuỗi UTF-8 chuẩn.
  - `app/src/main/java/com/finlux/app/presentation/auth/AuthScreens.kt`, `presentation/.../classic/...`, `presentation/.../modern/...`: Chuẩn hóa 100% chuỗi tiếng Việt UTF-8 và kết nối component.
  - `app/src/main/java/com/finlux/app/presentation/settings/SettingsScreen.kt`: Bổ sung Card Cài đặt "Phong cách giao diện" + `GlassBottomSheet` + Radio UI Selector.
  - `app/build.gradle.kts`: Bump `versionCode = 70`, `versionName = "1.7.1"`.
  - `CHANGELOG.md`: Thêm mục release v1.7.1.

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.7.0: Dual-UI Style Architecture & Theme Switcher

**Ngày:** 2026-08-15

### Mục tiêu
- **Kiến trúc Đa Phong Cách Giao Diện:** Hỗ trợ 2 phong cách UI song song:
  1. `CLASSIC_LIQUID`: Phong cách Liquid Glass truyền thống của FinLux (v1.5.9).
  2. `MODERN_LUXURY`: Phong cách Modern Callstack / iOS 26 Liquid Glass từ bản v1.6.6.
- **Persistence & Switcher:** Lưu lựa chọn trong `DataStorePreferences`, thêm mục chọn `[🎨 Phong cách giao diện]` trong màn hình Cài đặt (`SettingsScreen.kt`) cho phép chuyển đổi tức thì.
- **Đồng nhất 100% Logic/ViewModel:** Cả 2 giao diện dùng chung Domain/Repository/ViewModel.
- **Tuân thủ SOP:** Chạy test pass 100%, bump version lên `v1.7.0` (versionCode 69) và build APK.

### Kết quả & Danh sách file đã chỉnh sửa
- **Kết quả Unit Tests:** 39/39 tests PASS 100% (`.\gradlew.bat testDebugUnitTest` hoàn tất trong 6s).
- **Danh sách file thay đổi:**
  - `app/src/main/java/com/finlux/app/domain/model/FinanceModels.kt`: Bổ sung `enum class AppUiStyle { CLASSIC_LIQUID, MODERN_LUXURY }`.
  - `app/src/main/java/com/finlux/app/domain/repository/ThemePreferenceRepository.kt`: Bổ sung `uiStyle: Flow<AppUiStyle>` và `suspend fun setUiStyle(uiStyle: AppUiStyle)`.
  - `app/src/main/java/com/finlux/app/data/local/datastore/DataStoreThemePreferenceRepository.kt`: Lưu trữ và đọc `app_ui_style` từ DataStore.
  - `app/src/main/java/com/finlux/app/presentation/RootViewModel.kt`: Quản lý StateFlow `uiStyle` và hàm `setUiStyle()`.
  - `app/src/main/java/com/finlux/app/core/designsystem/FinluxTheme.kt`: Định nghĩa `LocalAppUiStyle` và cung cấp bộ token màu/gradient cho cả 2 phong cách.
  - `app/src/main/java/com/finlux/app/core/designsystem/LiquidGlass.kt`, `StyleBackdrop.kt`, `WaterGlass.kt`: Tối ưu hóa render glass đa lớp.
  - `app/src/main/java/com/finlux/app/presentation/home/CurrencyFormatters.kt`: Chuẩn hóa các hàm định dạng tiền tệ `toVnd()` và `toShortVnd()`.
  - `app/src/main/java/com/finlux/app/presentation/home/`: `HomeScreen.kt` (dispatcher), `classic/ClassicHomeScreen.kt`, `modern/ModernHomeScreen.kt`.
  - `app/src/main/java/com/finlux/app/presentation/budget/`: `BudgetScreen.kt` (dispatcher), `classic/ClassicBudgetScreen.kt`, `modern/ModernBudgetScreen.kt`.
  - `app/src/main/java/com/finlux/app/presentation/reports/`: `ReportsScreen.kt` (dispatcher), `classic/ClassicReportsScreen.kt`, `modern/ModernReportsScreen.kt`.
  - `app/src/main/java/com/finlux/app/presentation/wallet/`: `WalletsScreen.kt` (dispatcher), `classic/ClassicWalletsScreen.kt`, `modern/ModernWalletsScreen.kt`.
  - `app/src/main/java/com/finlux/app/presentation/transaction/`: `TransactionsScreen.kt` (dispatcher), `classic/ClassicTransactionsScreen.kt`, `modern/ModernTransactionsScreen.kt`.
  - `app/src/main/java/com/finlux/app/presentation/components/`: `MainBottomBar.kt` (dispatcher), `classic/ClassicMainBottomBar.kt`, `modern/ModernMainBottomBar.kt`.
  - `app/src/main/java/com/finlux/app/presentation/settings/SettingsScreen.kt`: Thêm mục chọn phong cách giao diện `[🎨 Phong cách giao diện]`.
  - `app/src/main/java/com/finlux/app/presentation/FinluxRoot.kt` & `com/finlux/app/core/navigation/FinluxNavHost.kt`: Kết nối `uiStyle` xuyên suốt Compose Navigation.
  - `app/src/test/java/com/finlux/app/presentation/RootViewModelTest.kt`: Unit tests cho tính năng chuyển đổi UI style.
  - `app/build.gradle.kts`: Bump `versionCode = 69`, `versionName = "1.7.0"`.
  - `CHANGELOG.md`: Thêm mục release v1.7.0.

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.5.9: Simplify Release & APK Artifact Naming

**Ngày:** 2026-08-14

### Mục tiêu
- **Clean Tag & Release Naming:** Loại bỏ hậu tố `-build-${GITHUB_RUN_NUMBER}` khỏi quy trình tạo GitHub Release trong `.github/workflows/release.yml`.
- **Tên hiển thị chuẩn:**
  - Release Title / Tag: `Release v1.5.9` / `v1.5.9`
  - Tên file APK: `FinLux-v1.5.9.apk`
- **Bump Version:** Nâng `versionName` lên `1.5.9` và `versionCode` `68`.

### Kết quả & Danh sách file đã chỉnh sửa
| File | Thay đổi |
|---|---|
| `.github/workflows/release.yml` | ✅ Đổi định dạng `TAG_NAME="v${VERSION_NAME}"` (bỏ `-build-*`) |
| `app/build.gradle.kts` | ✅ Bump `versionCode 68`, `versionName 1.5.9` |
| `HANDOVER_LOG.md` | ✅ Cập nhật log PRE/POST-EXECUTION |
| `CHANGELOG.md` | ✅ Thêm mục phiên bản release v1.5.9 |

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.5.8: Embed Firebase google-services.json and Web Client ID

**Ngày:** 2026-08-14

### Mục tiêu
- **Track google-services.json:** Gỡ bỏ `app/google-services.json` khỏi `.gitignore` để commit trực tiếp vào repo, đảm bảo quy trình build tự động trên GitHub Actions luôn có cấu hình Firebase thật.
- **Update Web Client ID Fallback:** Cập nhật ID `927751753962-04paon2termkbeanbsv7m8t9a8m6tk5h.apps.googleusercontent.com` vào `AuthViewModel.kt`.
- **Toolchain Environment Script:** Cải tiến `build_and_install.ps1` tự động thiết lập `JAVA_HOME` và `ANDROID_HOME` từ cache toolchain.
- **Bump Version:** Nâng `versionName` lên `1.5.8` và `versionCode` `67`.

### Kết quả & Danh sách file đã chỉnh sửa
| File | Thay đổi |
|---|---|
| `.gitignore` | ✅ Cho phép theo dõi `app/google-services.json` |
| `app/google-services.json` | ✅ Commit cấu hình Firebase chính thức |
| `AuthViewModel.kt` | ✅ Cập nhật Web Client ID fallback chính xác |
| `build_and_install.ps1` | ✅ Auto-resolve `JAVA_HOME` & `ANDROID_HOME` từ toolchain cache |
| `app/build.gradle.kts` | ✅ Bump `versionCode 67`, `versionName 1.5.8` |
| `HANDOVER_LOG.md` | ✅ Cập nhật log PRE/POST-EXECUTION |
| `CHANGELOG.md` | ✅ Thêm mục phiên bản release v1.5.8 |

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.5.7: Custom Release APK Naming on GitHub Actions

**Ngày:** 2026-08-14

### Mục tiêu
- **Artifact Naming:** Chỉnh sửa `.github/workflows/release.yml` tự động đổi tên file APK đầu ra từ `app-debug.apk` thành `FinLux-<TAG_NAME>.apk` (ví dụ `FinLux-v1.5.7-build-5.apk` hoặc `FinLux-v1.5.7.apk`) trước khi upload lên GitHub Release Assets.
- **Dynamic Version Extraction:** Trích xuất `versionName` động từ `app/build.gradle.kts` cho tag name fallback khi không đẩy tag thủ công (thay thế giá trị hardcode cũ).
- **Bump Version:** Nâng `versionName` lên `1.5.7` và `versionCode` `66`.

### Kết quả & Danh sách file đã chỉnh sửa
| File | Thay đổi |
|---|---|
| `.github/workflows/release.yml` | ✅ Đổi tên APK sang `FinLux-<TAG_NAME>.apk`, parse `versionName` động từ gradle |
| `app/build.gradle.kts` | ✅ Bump `versionCode 66`, `versionName 1.5.7` |
| `HANDOVER_LOG.md` | ✅ Cập nhật log 2 bước PRE/POST-EXECUTION |
| `CHANGELOG.md` | ✅ Thêm mục phiên bản release v1.5.7 |

### Trạng thái
`[DONE]`

---

## [POSTPONED / BACKLOG] Task v1.6.0: Audit & Expand Multi-type Notification System

**Ngày:** 2026-08-14  
**Ghi chú:** Tạm hoãn để ưu tiên các tính năng dự án quan trọng hơn. Chi tiết kế hoạch đã lưu tại [BACKLOG.md](file:///d:/Sources/FinLux/BACKLOG.md).

### Mục tiêu
- **Rà soát Hiện trạng Module Thông báo:** Đánh giá `AppNotification`, `NotificationRepository`, `FirebaseReadRepository`, `DemoFinluxRepository`, `NotificationsViewModel`, `NotificationsScreen.kt`, `AlarmReminderScheduler.kt`.
- **Chuẩn hóa Data Model & Classification (`NotificationType`):** Bổ sung enum `NotificationType` (`REMINDER`, `BUDGET_ALERT`, `GOAL_MILESTONE`, `TRANSACTION_SUMMARY`, `SYSTEM`), mở rộng `AppNotification` với các trường `type`, `targetRoute`, `targetId`, `actionUrl`, `iconName`, `badgeColorHex`.
- **Xây dựng Engine Dispatcher & Triggers:** Tạo các dispatcher tự động phát thông báo cảnh báo Ngân sách (80%, 100%), cột mốc Mục tiêu tiết kiệm (50%, 100%) và Thông báo hệ thống.
- **Nâng cấp UI & Deep Link Navigation:** Cập nhật `NotificationsScreen.kt` hiển thị icon, badge màu sắc theo loại thông báo, filter tab phân loại và xử lý bấm thông báo tự động điều hướng thông minh sang màn hình đích (`Budget`, `Goals`, `Reports`...).
- **Danh sách file dự kiến chỉnh sửa / tạo mới:**
  - `NotificationType.kt` [NEW]
  - `AppNotification.kt`
  - `NotificationRepository.kt`
  - `FirebaseReadRepository.kt`
  - `DemoFinluxRepository.kt`
  - `NotificationsViewModel.kt`
  - `NotificationsScreen.kt`
  - `FinluxNavHost.kt`
  - `HANDOVER_LOG.md`
  - `CHANGELOG.md`

---

## [DONE] Task v1.5.6: Auto Request Notification Permission & Settings Guide

**Ngày:** 2026-08-13

### Mục tiêu
- **Auto Runtime Permission Request (`POST_NOTIFICATIONS` - Android 13+):** Tự động kiểm tra quyền `Manifest.permission.POST_NOTIFICATIONS` và `NotificationManagerCompat.areNotificationsEnabled()`. Khi người dùng vào `HomeScreen.kt` hoặc `RemindersScreen.kt`, tự động kích hoạt popup xin quyền hệ thống.
- **Friendly Settings Guide Dialog:** Nếu quyền bị từ chối hoặc bị tắt trong Cài đặt hệ thống (trên Xiaomi, OPPO, Vivo...), hiển thị Dialog Liquid Glass "Bật thông báo để không bỏ lỡ hạn thanh toán" kèm nút `[Bật trong Cài đặt]` mở trực tiếp `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`.
- **Bump Version:** Nâng `versionName` lên `1.5.6` và `versionCode` `62`.
- **Test & Rebuild:** Chạy unit test PASS 100% và rebuild nạp APK bằng `build_and_install.ps1`.

### Kết quả Unit Test & Build
- **Unit Test:** `gradlew testDebugUnitTest` PASS 100% (0 lỗi).
- **Build APK:** `assembleDebug` SUCCESSFUL trong 8s, nạp thành công 2 thiết bị ADB (`192.168.30.194:33349` và `emulator-5554`).

### Danh sách file đã thực sự chỉnh sửa / tạo mới
| File | Thay đổi |
|---|---|
| `NotificationPermissionHandler.kt` | ✅ [NEW] Helper tự động xin quyền `POST_NOTIFICATIONS` & Dialog Liquid Glass hướng dẫn mở Cài đặt ứng dụng |
| `HomeScreen.kt` | ✅ Tự động kích hoạt luồng kiểm tra / xin quyền khi vào Trang chủ |
| `RemindersScreen.kt` | ✅ Tự động kiểm tra / xin quyền khi vào màn hình Nhắc nhở |
| `app/build.gradle.kts` | ✅ Bump `versionCode 62`, `versionName 1.5.6` |
| `HANDOVER_LOG.md` | ✅ Cập nhật log 2 bước PRE/POST-EXECUTION |
| `CHANGELOG.md` | ✅ Thêm mục phiên bản release v1.5.6 |

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.5.5: Restore Heads-up Dropdown Banner Notification Channel & Priority

**Ngày:** 2026-08-13

### Mục tiêu
- **Notification Channel Upgrade (`AlarmReminderScheduler.kt`):** Cập nhật `ReminderChannelId` thành `"finlux_reminders_v2"` để ép Android OS tạo mới Kênh thông báo độ ưu tiên cao. Cấu hình `IMPORTANCE_HIGH`, `enableVibration(true)`, `enableLights(true)`, và `lockscreenVisibility = VISIBILITY_PUBLIC`.
- **Heads-up Dropdown Banner (`NotificationCompat.Builder`):** Thêm `.setPriority(NotificationCompat.PRIORITY_MAX)`, `.setDefaults(NotificationCompat.DEFAULT_ALL)`, `.setCategory(NotificationCompat.CATEGORY_REMINDER)`, và `.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)` để đảm bảo thông báo luôn thả xuống dạng Banner trượt từ đỉnh màn hình khi báo thức nổ.
- **Bump Version:** Nâng `versionName` lên `1.5.5` và `versionCode` `60`.
- **Test & Rebuild:** Chạy unit test PASS 100% và rebuild nạp APK bằng `build_and_install.ps1`.

### Kết quả Unit Test & Build
- **Unit Test:** `gradlew testDebugUnitTest` PASS 100% (0 lỗi).
- **Build APK:** `assembleDebug` SUCCESSFUL trong 8s, nạp thành công 2 thiết bị ADB (`192.168.30.194:33349` và `emulator-5554`).

### Danh sách file đã thực sự chỉnh sửa / tạo mới
| File | Thay đổi |
|---|---|
| `AlarmReminderScheduler.kt` | ✅ Khởi tạo Kênh thông báo mới `finlux_reminders_v2` độ ưu tiên cao (`IMPORTANCE_HIGH`), thêm `PRIORITY_MAX`, `DEFAULT_ALL`, rung & hiển thị Banner thả xuống |
| `app/build.gradle.kts` | ✅ Bump `versionCode 60`, `versionName 1.5.5` |
| `HANDOVER_LOG.md` | ✅ Cập nhật log 2 bước PRE/POST-EXECUTION |
| `CHANGELOG.md` | ✅ Thêm mục phiên bản release v1.5.5 |

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.5.4: Update Actual Paid Amount on Notification Record

**Ngày:** 2026-08-13

### Mục tiêu
- **Update Notification Paid Record:** Khi thanh toán thành công với số tiền thực tế đã sửa `customAmount`, cập nhật cả trường `amount` và `body` của `AppNotification` thành số tiền mới trong Firestore & local database.
- **Display Actual Amount on Card (`NotificationsScreen.kt`):** Thẻ thông báo đã thanh toán hiển thị rõ ràng con số thực trả (ví dụ `Đã thanh toán: 1.950.000 ₫` màu xanh lá).
- **Repository Support (`NotificationRepository.kt`):** Thêm phương thức `markAsPaidWithAmount(id, amount, body)`.
- **Bump Version:** Nâng `versionName` lên `1.5.4` và `versionCode` `58`.
- **Test & Rebuild:** Chạy unit test PASS 100% và rebuild nạp APK bằng `build_and_install.ps1`.

### Kết quả Unit Test & Build
- **Unit Test:** `gradlew testDebugUnitTest` PASS 100% (0 lỗi).
- **Build APK:** `assembleDebug` SUCCESSFUL trong 8s, nạp thành công 2 thiết bị ADB (`192.168.30.194:33349` và `emulator-5554`).

### Danh sách file đã thực sự chỉnh sửa / tạo mới
| File | Thay đổi |
|---|---|
| `NotificationRepository.kt` | ✅ Thêm phương thức `markAsPaidWithAmount(id, amount, newBody)` |
| `FirebaseReadRepository.kt` | ✅ Cập nhật `amount` và `body` mới vào Firestore khi đánh dấu `isPaid = true` |
| `DemoFinluxRepository.kt` | ✅ Cập nhật `amount` và `body` cho local state flow |
| `NotificationsViewModel.kt` | ✅ Gọi `markAsPaidWithAmount` với số tiền thực trả `customAmount` sau khi tạo giao dịch thành công |
| `NotificationsScreen.kt` | ✅ Thẻ thông báo hiển thị con số thực trả đã sửa (ví dụ: `Đã thanh toán: 1.950.000 ₫`) |
| `app/build.gradle.kts` | ✅ Bump `versionCode 58`, `versionName 1.5.4` |
| `HANDOVER_LOG.md` | ✅ Cập nhật log 2 bước PRE/POST-EXECUTION |
| `CHANGELOG.md` | ✅ Thêm mục phiên bản release v1.5.4 |

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.5.3: Variable Amount Quick Payment Sheet

**Ngày:** 2026-08-13

### Mục tiêu
- **Quick Payment Sheet (`NotificationsScreen.kt`):** Khi bấm nút `[Thanh toán]`, mở ModalBottomSheet "Xác nhận & Điều chỉnh số tiền" thiết kế Liquid Glass. Cho phép nhập số tiền thực tế (với preview định dạng VND), chọn ví & danh mục trước khi bấm `[Xác nhận trừ tiền]`.
- **System Push Action `[✏️ Sửa số tiền]` (`AlarmReminderScheduler.kt`):** Thêm Notification Action `[✏️ Sửa số tiền]` trên Push Notification hệ thống. Bấm vào sẽ mở app và tự động bật Quick Payment Sheet của thông báo đó.
- **Deep Link Extras Handling (`MainActivity.kt` & `FinluxNavHost.kt`):** Bắt `pay_notification_id` và tự động kích hoạt Quick Payment Sheet tương ứng khi navigate vào `NotificationsScreen`.
- **ViewModel Update (`NotificationsViewModel.kt`):** Thêm hàm `payNotificationWithCustomAmount` xử lý tạo giao dịch với số tiền mới đã điều chỉnh, cập nhật số dư ví, ngân sách và đánh dấu `isPaid = true`.
- **Bump Version:** Nâng `versionName` lên `1.5.3` và `versionCode` `57`.
- **Test & Rebuild:** Chạy unit test PASS 100% và rebuild nạp APK bằng `build_and_install.ps1`.

### Kết quả Unit Test & Build
- **Unit Test:** `gradlew testDebugUnitTest` PASS 100% (0 lỗi, bao gồm `NotificationsViewModelTest` bổ sung test case điều chỉnh số tiền thực tế `payNotificationWithCustomAmount`).
- **Build APK:** `assembleDebug` SUCCESSFUL trong 8s, nạp thành công 2 thiết bị ADB (`192.168.30.194:33349` và `emulator-5554`).

### Danh sách file đã thực sự chỉnh sửa / tạo mới
| File | Thay đổi |
|---|---|
| `AlarmReminderScheduler.kt` | ✅ Bổ sung Notification Action `[✏️ Sửa số tiền]` (`ACTION_EDIT_PAYMENT`) mở app & đính kèm extra |
| `MainActivity.kt` | ✅ Nhận `pay_notification_id` Intent Extra đẩy vào `payNotificationIdFlow` |
| `FinluxRoot.kt` & `FinluxNavHost.kt` | ✅ Truyền `payNotificationIdFlow` cho `NotificationsScreen` |
| `NotificationsViewModel.kt` | ✅ Thêm `payNotificationWithCustomAmount`, quan sát danh sách ví & danh mục chi tiêu |
| `NotificationsScreen.kt` | ✅ Thiết kế Quick Payment Sheet (ModalBottomSheet) với ô nhập số tiền thực tế, preview VND, chọn ví & danh mục |
| `NotificationsViewModelTest.kt` | ✅ Bổ sung unit test `payNotificationWithCustomAmount_executesWithUpdatedAmount` PASS 100% |
| `app/build.gradle.kts` | ✅ Bump `versionCode 57`, `versionName 1.5.3` |
| `HANDOVER_LOG.md` | ✅ Cập nhật log 2 bước PRE/POST-EXECUTION |
| `CHANGELOG.md` | ✅ Thêm mục phiên bản release v1.5.3 |

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.5.2: Fix Double Payment & Sync Notification Paid State

**Ngày:** 2026-08-13

### Mục tiêu
- **Fix Push Action Sync:** Trong `ReminderReceiver.kt`, khi người dùng bấm `[Đã thanh toán]` trực tiếp trên thanh thông báo hệ thống (`ACTION_PAY`), sau khi tạo giao dịch chi tiêu, bổ sung gọi `notificationRepository.markAsPaidByReminderId(id)` để lập tức đổi bản ghi `AppNotification` tương ứng thành `isPaid = true` trong Firestore / Database.
- **Race Condition & Double Click Prevention:** Trong `NotificationsViewModel.kt`, kiểm tra ngay đầu hàm `payNotification`: nếu `notification.isPaid == true` thì `return` ngay lập tức để chống bấm trùng / race condition.
- **UI Guard:** Trên `NotificationsScreen.kt`, đảm bảo khi `isPaid == true`, ẩn hoàn toàn nút bấm và chỉ hiện nhãn `[✓ Đã thanh toán]`.
- **Unit Test:** Viết unit test `NotificationsViewModelTest.kt` kiểm thử ngăn chặn thanh toán trùng lặp.
- **Bump Version:** Nâng `versionName` lên `1.5.2` và `versionCode` `55`.
- **Test & Rebuild:** Chạy unit test PASS 100% và rebuild nạp APK bằng `build_and_install.ps1`.

### Kết quả Unit Test & Build
- **Unit Test:** `gradlew testDebugUnitTest` PASS 100% (0 lỗi, bao gồm `NotificationsViewModelTest` kiểm thử chống trừ tiền trùng lặp).
- **Build APK:** `assembleDebug` SUCCESSFUL trong 7s, nạp thành công 2 thiết bị ADB (`192.168.30.194:33349` và `emulator-5554`).

### Danh sách file đã thực sự chỉnh sửa / tạo mới
| File | Thay đổi |
|---|---|
| `AlarmReminderScheduler.kt` | ✅ Khi bấm `[Đã thanh toán]` trên Push Notification, gọi `markAsPaidByReminderId(id)` cập nhật DB |
| `NotificationRepository.kt` | ✅ Bổ sung phương thức `markAsPaidByReminderId(reminderId)` |
| `FirebaseReadRepository.kt` | ✅ Tích hợp batch update `isRead = true, isPaid = true` theo `reminderId` |
| `DemoFinluxRepository.kt` | ✅ Tích hợp `markAsPaidByReminderId(reminderId)` |
| `NotificationsViewModel.kt` | ✅ Chống bấm trùng 2 lần: `if (notification.isPaid) return` ngay đầu hàm |
| `NotificationsViewModelTest.kt` | **[MỚI]** Unit test đảm bảo tính idempotent và ngăn chặn tạo giao dịch trùng lặp khi `isPaid == true` |
| `.github/workflows/release.yml` | ✅ Workflow tự động test, build APK và publish Release lên GitHub |
| `app/build.gradle.kts` | ✅ Bump `versionCode 55`, `versionName 1.5.2` |
| `HANDOVER_LOG.md` | ✅ Cập nhật log 2 bước PRE/POST-EXECUTION |
| `CHANGELOG.md` | ✅ Thêm mục phiên bản release v1.5.2 |

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.5.1: Quick Pay Action on NotificationsScreen

**Ngày:** 2026-08-13

### Mục tiêu
- **Quick Pay Action:** Bổ sung thuộc tính `isPaid: Boolean` cho `AppNotification`.
- **Automatic Expense Recording:** Trên `NotificationsScreen.kt`, với thông báo nhắc nhở thanh toán (có `reminderId` hoặc `amount > 0`), hiển thị nút `[💳 Xác nhận thanh toán]`. Khi bấm:
  1. Tự động gọi `AddTransactionUseCase` tạo giao dịch chi tiêu tương ứng.
  2. Tự động trừ số dư ví và cập nhật ngân sách realtime.
  3. Cập nhật trạng thái thông báo sang `[✓ Đã thanh toán]` và ẩn nút bấm.
  4. Hiển thị Snackbar/Toast thông báo thành công.
- **Bump Version:** Nâng `versionName` lên `1.5.1` và `versionCode` `54`.
- **Test & Rebuild:** Chạy unit test PASS 100% và rebuild nạp APK bằng `build_and_install.ps1`.

### Kết quả Unit Test & Build
- **Unit Test:** `gradlew testDebugUnitTest` PASS 100% (0 lỗi).
- **Build APK:** `assembleDebug` SUCCESSFUL trong 8s, nạp thành công 2 thiết bị ADB (`192.168.30.194:33349` và `emulator-5554`).

### Danh sách file đã thực sự chỉnh sửa
| File | Thay đổi |
|---|---|
| `AppNotification.kt` | ✅ Bổ sung trường `categoryId`, `walletId`, `isPaid` |
| `NotificationRepository.kt` | ✅ Thêm phương thức `markAsPaid(id)` |
| `FirebaseReadRepository.kt` | ✅ Tích hợp ghi nhận trạng thái `isPaid` vào Firestore realtime |
| `DemoFinluxRepository.kt` | ✅ Tích hợp `markAsPaid(id)` cho local state flow |
| `NotificationsViewModel.kt` | ✅ Thêm `payNotification(item)` gọi `AddTransactionUseCase` tạo chi tiêu, trừ số dư ví, cập nhật ngân sách |
| `NotificationsScreen.kt` | ✅ Nút `[💳 Xác nhận thanh toán]`, nhãn `[✓ Đã thanh toán]`, Snackbar thông báo kết quả |
| `AlarmReminderScheduler.kt` | ✅ Đính kèm `categoryId` và `walletId` vào bản ghi thông báo khi báo thức nổ |
| `app/build.gradle.kts` | ✅ Bump `versionCode 54`, `versionName 1.5.1` |
| `HANDOVER_LOG.md` | ✅ Cập nhật log 2 bước PRE/POST-EXECUTION |
| `CHANGELOG.md` | ✅ Thêm mục phiên bản release v1.5.1 |

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.5.0: Save Notification History & Auto Navigation Deep Link

**Ngày:** 2026-08-13

### Mục tiêu
- **Notification Persistence:** Tự động tạo và lưu bản ghi `AppNotification` vào Firestore `users/{uid}/notifications` (hoặc `DemoFinluxRepository`) khi báo thức nổ trong `ReminderReceiver.kt`.
- **Deep Link Navigation:** Cập nhật `PendingIntent` trong `AlarmReminderScheduler.kt` kèm Intent Extra (`destination = "notifications"`). Xử lý Intent trong `MainActivity.kt` và `FinluxNavHost.kt` để tự động điều hướng sang `NotificationsScreen` khi người dùng bấm vào thông báo.
- **Notifications UI & ViewModel:** Xây dựng `NotificationsViewModel.kt` và nâng cấp `NotificationsScreen.kt` hiển thị danh sách thông báo glassmorphism, đánh dấu đã đọc và xóa lịch sử.
- **Bump Version:** Nâng `versionName` lên `1.5.0` và `versionCode` `52`.
- **Test & Rebuild:** Chạy unit test PASS 100% và rebuild nạp APK bằng `build_and_install.ps1`.

### Kết quả Unit Test & Build
- **Unit Test:** `gradlew testDebugUnitTest` PASS 100% (0 lỗi).
- **Build APK:** `assembleDebug` SUCCESSFUL trong 8s, nạp thành công 2 thiết bị ADB (`192.168.30.194:33349` và `emulator-5554`).

### Danh sách file đã thực sự chỉnh sửa / tạo mới
| File | Thay đổi |
|---|---|
| `AppNotification.kt` [NEW] | ✅ Định nghĩa Domain Model cho bản ghi thông báo |
| `NotificationRepository.kt` [NEW] | ✅ Định nghĩa Interface lắng nghe, lưu, đánh dấu đã đọc, xóa lịch sử thông báo |
| `NotificationsViewModel.kt` [NEW] | ✅ ViewModel quản lý StateFlow danh sách thông báo và các thao tác |
| `NotificationsScreen.kt` | ✅ Nâng cấp giao diện danh sách thông báo Liquid Glass, nhãn thời gian, trạng thái đọc |
| `FirebaseReadRepository.kt` | ✅ Tích hợp Firestore subcollection `users/{uid}/notifications` lưu & lắng nghe realtime |
| `DemoFinluxRepository.kt` | ✅ Tích hợp lưu & lắng nghe lịch sử thông báo local state flow |
| `RepositoryModule.kt` | ✅ Cung cấp `NotificationRepository` trong Hilt DI |
| `AlarmReminderScheduler.kt` | ✅ Tự động lưu `AppNotification` khi báo thức nổ và set `destination = "notifications"` trong `PendingIntent` |
| `MainActivity.kt` | ✅ Nhận `Intent` extra (`onCreate` & `onNewIntent`) đẩy vào `destinationFlow` |
| `FinluxRoot.kt` | ✅ Truyền `destinationFlow` xuống `FinluxNavHost` |
| `FinluxNavHost.kt` | ✅ Tự động `navController.navigate(Route.Notifications.value)` khi mở từ notification |
| `app/build.gradle.kts` | ✅ Bump `versionCode 52`, `versionName 1.5.0` |
| `HANDOVER_LOG.md` | ✅ Cập nhật log 2 bước PRE/POST-EXECUTION |
| `CHANGELOG.md` | ✅ Thêm mục phiên bản release v1.5.0 |

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.4.8: Remove Full-Screen Frame Drag Gesture & Zero Gesture Collision

**Ngày:** 2026-08-13

### Mục tiêu
- Tháo bỏ hoàn toàn khối `pointerInput` lắng nghe cử chỉ kéo ngang toàn màn hình và hiệu ứng `translationX` trong `FinluxNavHost.kt`.
- Chuyển 100% việc điều hướng tab chính sang Bottom Navigation Bar, giải quyết triệt để 100% lỗi xô lệch khung màn hình cha khi vuốt Card/Ví (SwipeToDismissBox) hoặc danh sách ngang.
- Bump `versionName` lên `1.4.8` và `versionCode` `50`.
- Chạy 100% Unit Test pass (`gradlew testDebugUnitTest`).
- Rebuild APK bằng `build_and_install.ps1`.

### Kết quả Unit Test & Build
- **Unit Test:** `gradlew testDebugUnitTest` PASS 100% (0 lỗi).
- **Build APK:** `assembleDebug` SUCCESSFUL trong 8s, nạp thành công cả 2 thiết bị ADB (`192.168.30.194:33349` và `emulator-5554`).

### Danh sách file đã thực sự chỉnh sửa
| File | Thay đổi |
|---|---|
| `FinluxNavHost.kt` | ✅ Gỡ bỏ hoàn toàn `pointerInput` cử chỉ kéo ngang khung màn hình và `translationX` |
| `app/build.gradle.kts` | ✅ Bump `versionCode 50`, `versionName 1.4.8` |
| `HANDOVER_LOG.md` | ✅ Cập nhật log 2 bước PRE/POST-EXECUTION |
| `CHANGELOG.md` | ✅ Thêm mục phiên bản release v1.4.8 |

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.4.7: Fix Gesture Collision & Item Swipe Clipping

**Ngày:** 2026-08-13

### Mục tiêu
- Xử lý xung đột cử chỉ vuốt ngang: Đổi `PointerEventPass.Initial` sang `PointerEventPass.Main` trong `FinluxNavHost.kt` và kiểm tra `change.isConsumed` để khi người dùng vuốt Card/Item (SwipeToDismissBox), cử chỉ vuốt ngang được con tiêu thụ hoàn toàn và không bị kéo lê Pager/Container cha.
- Fix tràn bố cục UI khi vuốt (UI Clipping Issue): Bổ sung `Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))` cho `SwipeToDismissBox` trong `WalletsScreen.kt` và đảm bảo padding chuẩn không bị đè lên Bottom Navigation Bar.
- Bump `versionName` lên `1.4.7` và `versionCode` `49`.
- Chạy 100% Unit Test pass (`gradlew testDebugUnitTest`).
- Rebuild APK bằng `build_and_install.ps1`.

### Kết quả Unit Test & Build
- **Unit Test:** `gradlew testDebugUnitTest` PASS 100% (0 lỗi).
- **Build APK:** `assembleDebug` SUCCESSFUL trong 8s, nạp thành công 2 thiết bị ADB (`192.168.30.194:33349` và `emulator-5554`).

### Danh sách file đã thực sự chỉnh sửa
| File | Thay đổi |
|---|---|
| `FinluxNavHost.kt` | ✅ Đổi `PointerEventPass.Initial` ➔ `PointerEventPass.Main`, kiểm tra `change.isConsumed` để hủy root swipe khi con tiêu thụ sự kiện |
| `WalletsScreen.kt` | ✅ Bổ sung `Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))` cho `SwipeToDismissBox` |
| `app/build.gradle.kts` | ✅ Bump `versionCode 49`, `versionName 1.4.7` |
| `HANDOVER_LOG.md` | ✅ Cập nhật log 2 bước PRE/POST-EXECUTION |
| `CHANGELOG.md` | ✅ Thêm mục phiên bản release v1.4.7 |

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.4.6: Google Auth CredentialProvider Compatibility & Multi-ADB Deployment

**Ngày:** 2026-08-13

### Mục tiêu
- Thêm `com.google.android.gms:play-services-auth` vào `libs.versions.toml` và `app/build.gradle.kts` giải quyết ngoại lệ `GetCredentialProviderConfigurationException` trên máy giả lập.
- Nâng cấp `build_and_install.ps1` hỗ trợ cài đè APK tự động cho tất cả thiết bị kết nối ADB cùng lúc.
- Đồng bộ hóa phiên bản ứng dụng lên `v1.4.6` (versionCode 48).

### Kết quả Unit Test & Build
- **Unit Test:** `gradlew testDebugUnitTest` PASS 100% (0 lỗi).
- **Build APK:** `assembleDebug` SUCCESSFUL trong 8s, nạp thành công cả 2 thiết bị ADB (`192.168.30.194:33349` và `emulator-5554`).

### Danh sách file đã thực sự chỉnh sửa
| File | Thay đổi |
|---|---|
| `gradle/libs.versions.toml` | ✅ Khai báo `playServicesAuth = "21.3.0"` |
| `app/build.gradle.kts` | ✅ Khai báo `implementation(libs.play.services.auth)`, bump `versionCode 48`, `versionName 1.4.6` |
| `build_and_install.ps1` | ✅ Vòng lặp nạp APK cho tất cả thiết bị ADB kết nối |
| `HANDOVER_LOG.md` | ✅ Đồng bộ thông tin phiên bản v1.4.6 (versionCode 48) |
| `CHANGELOG.md` | ✅ Cập nhật nhật ký thay đổi v1.4.6 |

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.4.5: Update App Launcher Display Name to "Finance Luxury"

**Ngày:** 2026-08-13

### Mục tiêu
- Cập nhật resource `app_name` trong `app/src/main/res/values/strings.xml` thành `"Finance Luxury"`.
- Xác nhận `AndroidManifest.xml` gán `android:label="@string/app_name"`.
- Bump `versionName` lên `1.4.5` và `versionCode` `41`.
- Chạy Unit Tests (`gradlew testDebugUnitTest`) pass 100%.
- Rebuild APK bằng `build_and_install.ps1`.

### Kết quả Unit Test & Build
- **Unit Test:** `gradlew testDebugUnitTest` PASS 100% (0 lỗi).
- **Build APK:** `assembleDebug` SUCCESSFUL trong 10s.

### Danh sách file đã thực sự chỉnh sửa
| File | Thay đổi |
|---|---|
| `app/src/main/res/values/strings.xml` | ✅ Đổi `app_name` thành `Finance Luxury` |
| `app/build.gradle.kts` | ✅ Bump `versionCode 41`, `versionName 1.4.5` |
| `HANDOVER_LOG.md` | ✅ Cập nhật log 2 bước PRE/POST-EXECUTION |
| `CHANGELOG.md` | ✅ Thêm mục phiên bản release v1.4.5 |

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.4.4: Shared Project Debug Keystore & Google Sign-In SHA-1 Standardization

**Ngày:** 2026-08-13

### Mục tiêu
- Copy file `debug.keystore` chuẩn vào thư mục `app/debug.keystore` của project.
- Cấu hình `signingConfigs` trong `app/build.gradle.kts` ép kiểu build `debug` dùng chung file `app/debug.keystore`.
- Xuất mã SHA-1 của `app/debug.keystore` để cấu hình đồng bộ trên Firebase Console.
- Auto version bump `versionCode 37` -> `38`, `versionName` `1.4.3` -> `1.4.4`.
- Chạy 100% Unit Test pass (`gradlew testDebugUnitTest`).
- Rebuild APK thành công qua `build_and_install.ps1`.

### Kết quả Unit Test & Build
- **Unit Test:** `gradlew testDebugUnitTest` PASS 100% (0 lỗi).
- **Build APK:** `assembleDebug` SUCCESSFUL trong 13s, nạp APK thành công qua ADB.

### Danh sách file đã thực sự chỉnh sửa
| File | Thay đổi |
|---|---|
| `app/debug.keystore` | ✅ File keystore cố định dùng chung cho dự án |
| `app/build.gradle.kts` | ✅ Thêm `signingConfigs.debug` trỏ tới `debug.keystore`, bump `versionCode 38`, `versionName 1.4.4` |
| `HANDOVER_LOG.md` | ✅ Cập nhật log 2 bước PRE/POST-EXECUTION |
| `CHANGELOG.md` | ✅ Thêm mục phiên bản release v1.4.4 |

### Trạng thái
`[DONE]`

---



## [DONE] Task v1.4.3: Fix Budget Dynamic SpentAmount & Category Fallback Mapping

**Ngày:** 2026-08-13

### Mục tiêu
- Fix `spentAmount` trong Budget tính động 100% từ `transactionRepository.observeMonth()`
- Thêm fallback: khớp category theo `category.name` (cho giao dịch phiên bản cũ không có `categoryId`)
- Sửa hiển thị "Còn lại" trên Top Card Ngân sách sang định dạng VND đầy đủ (`toVnd()` thay vì `toShortVnd()`)
- Thêm Unit Test cho kịch bản fallback category name

### Kết quả Unit Test
**BUILD SUCCESSFUL — 7/7 BudgetViewModelTest PASS, toàn bộ test suite PASS 100%**

### Danh sách file đã thực sự chỉnh sửa
| File | Thay đổi |
|---|---|
| `AGENTS.md` | ✅ Bổ sung mục 📋 QUY TRÌNH QUẢN LÝ TÀI LIỆU CHUẨN (HANDOVER_LOG 2 bước + CHANGELOG SOP) |
| `BudgetViewModel.kt` | ✅ Fix bug: đổi `?:` (short-circuit) sang `+` (cộng dồn) để gom cả modern tx (by ID) + legacy tx (by name) — tránh double-count với guard `catNameLower != budget.categoryId.lowercase()` |
| `BudgetScreen.kt` | ✅ "Còn lại" dùng `toVnd()` thay `toShortVnd()` → hiển thị `Còn lại 1.225.000 ₫` |
| `BudgetViewModelTest.kt` | ✅ Rewrite: inject `transactionRepository`, thêm 2 test fallback mới (Test 3: legacy by name, Test 4: mixed modern+legacy), tổng 7 test cases |
| `app/build.gradle.kts` | ✅ versionCode 35→36, versionName 1.4.2→1.4.3 |

### Trạng thái
`[DONE]`

---

---

## Danh Sách Nhiệm Vụ Đã Hoàn Thành (Completed Tasks)

### [x] Task 1: Refactor Core Domain & Hilt DI Module (v1.1.0)
- Tách 14 UseCases độc lập trong package `com.finlux.app.domain.usecase`:
  - `TransactionValidation.kt`, `AddTransactionUseCase.kt`, `EditTransactionUseCase.kt`, `DeleteTransactionUseCase.kt`
  - `SaveWalletUseCase.kt`, `DeleteWalletUseCase.kt`, `TransferMoneyUseCase.kt`
  - `SaveCategoryUseCase.kt`, `DeleteCategoryUseCase.kt`
  - `SaveBudgetUseCase.kt`, `DeleteBudgetUseCase.kt`
  - `SaveReminderUseCase.kt`, `DeleteReminderUseCase.kt`
  - `SaveGoalUseCase.kt`, `DeleteGoalUseCase.kt`
- Tạo `FirebaseModule.kt` và cập nhật `RepositoryModule.kt` để inject Firebase instances với fallback an toàn.

### [x] Task 2: Google Sign-In & Credential Manager SDK (v1.2.0)
- Tích hợp Android Credential Manager SDK (`GetCredentialRequest`, `GetGoogleIdOption`).
- Trích xuất `GoogleIdTokenCredential` -> `idToken` -> `signInWithGoogle`.
- Cập nhật UI `AuthScreens.kt`: hiển thị loading overlay, mờ nút Apple/Facebook (Sắp có) kèm Toast thông báo.

### [x] Task 3: Bổ Sung Unit Test Dự Án (MockK + Turbine) (v1.2.0)
- **FirebaseTransactionRepositoryTest.kt:** Kiểm thử Firestore Atomic Transactions (Thêm/Xóa giao dịch và cập nhật số dư ví thành công).
- **AuthViewModelTest.kt:** Kiểm thử UI State transitions (`isLoading` -> `completed`/`error`) với `Turbine` and `MockK`.
- **Tổng số Unit Tests:** 28 tests pass 100% (0 lỗi).

### [x] Task Hotfix v1.2.1: Firestore Rules Resilience & UI Optimization (v1.2.1)
- **FirebaseFirestoreException Handling:** Bọc `try-catch` trong `FirebaseAuthRepository.kt` cho tất cả tác vụ Firestore (`seedNewUser`, `register`, `signInWithGoogle`, `updateDisplayName`, `updateAvatar`). Log cảnh báo `Log.w("Firestore", ...)` mà không chặn luồng đăng nhập Firebase Auth.
- **Unblock UI & Timeout 15s:** Khối `finally { mutableState.update { it.copy(isLoading = false) } }` trong `AuthViewModel.kt` triệt tiêu lỗi vô hạn spinner. Bọc `withTimeoutOrNull(15000)` tự động hủy sau 15s.
- **Client ID Động:** Tự động đọc `R.string.default_web_client_id` do plugin google-services tự sinh.
- **TextOverflow.Ellipsis Protection:** Khắc phục lỗi vỡ layout với tên/email dài trên `HomeScreen.kt` và `SettingsScreen.kt`.

### [x] Task Category Management: Modal Grid & Custom Category Create/Edit/Delete (v1.3.0)
- **Modal Lưới Chọn Danh Mục:** Bổ sung `ModalBottomSheet` hiển thị lưới 3 cột tất cả danh mục thu/chi.
- **Tạo Danh Mục Tùy Chỉnh:** Nút `+ Tạo mới` với Dialog chọn Tên, Icon, Màu sắc gọi `SaveCategoryUseCase`.
- **Chỉnh Sửa / Xóa Bằng Long-Click:** Sự kiện `combinedClickable(onLongClick)` trên danh mục tùy chỉnh mở Dialog Quản lý (Sửa/Xóa) với `DeleteCategoryUseCase` và AlertDialog xác nhận. Danh mục mặc định hệ thống hiển thị Toast bảo vệ.

### [x] Task UI/UX Polish: Currency Format Preview & TopBar Back Buttons (v1.3.1)
- **Định dạng tiền tệ tự động:** Thêm `supportingText` preview dạng `x.xxx.xxx đ` (hiển thị `0 đ` khi rỗng/bằng 0) cho toàn bộ các màn hình/dialog nhập tiền (`AddTransactionSheet`, `BudgetEditor`, `WalletEditor`, `TransferEditor`, `ReminderEditor`, `GoalsScreen`).
- **Nút Back trên TopBar:** Bổ sung nút quay lại `ArrowBack` trên TopBar của toàn bộ màn hình con (`BudgetScreen`, `WalletsScreen`, `ReportsScreen`, `TransactionsScreen`, `NotificationsScreen`...).

### [x] Task Recurring Reminders Polish: Push Notification Quick Actions & BootReceiver (v1.4.0)
- **Push Notification Quick Actions:** Bổ sung nút `[Đã thanh toán]` (gọi `AddTransactionUseCase` tạo giao dịch trừ số dư ví Firestore) và `[Nhắc lại sau 1h]` (lùi báo thức 60 phút) trực tiếp từ thông báo Android.
- **BootReceiver (`RECEIVE_BOOT_COMPLETED`):** Lắng nghe sự kiện khởi động lại máy để tự động đặt lại toàn bộ lịch báo thức `AlarmManager`.

---

## Cấu Hình Cần Thiết Khi Clone Project Mới (Setup Requirements)
Khi clone dự án FinLux về máy mới hoặc thiết lập môi trường mới, cần tạo/cấu hình thủ công các phần sau:

1. **File `app/google-services.json`:**
   - Tải từ Firebase Console sau khi tạo app Android với package `com.finlux.app`.
   - Đặt file tại đường dẫn `app/google-services.json` (file này nằm trong `.gitignore` để bảo mật).

2. **Dấu vân tay SHA-1 Debug Keystore trên Firebase Console:**
   - Keystore path: `C:\Users\<User>\.android\debug.keystore`
   - SHA-1: `EA:A9:EA:AB:B7:B9:9A:1F:F1:81:64:BF:76:2E:E1:75:C5:32:7F:47`

3. **Cấu hình Firestore Security Rules trên Firebase Console:**
   - Dán quy tắc trong file `firestore.rules`:
     ```javascript
     rules_version = '2';
     service cloud.firestore {
       match /databases/{database}/documents {
         match /{document=**} {
           allow read, write: if request.auth != null;
         }
       }
     }
     ```

4. **Kích hoạt Auth Provider trên Firebase Console:**
   - Vào **Authentication** -> **Sign-in method** -> Bật provider **Email/Password** và **Google**.

## [DONE] Task: Đồng bộ bộ icon ngân hàng và ví điện tử Việt Nam

**Ngày:** 2026-08-27

### Mục tiêu
- Đồng bộ danh mục tổ chức tài chính hiện hành từ VietQR API và tải logo về tài nguyên cục bộ của ứng dụng.
- Bổ sung các ví điện tử phổ biến đang hoạt động, có nguồn thương hiệu chính thức.
- Tái sử dụng `FinancialInstitutions.kt` để toàn bộ màn hình quản lý/thêm ví hiển thị logo thống nhất, có tìm kiếm và fallback an toàn.

### Scope dự kiến
- `app/src/main/res/drawable-nodpi/`: logo tổ chức tài chính đã tối ưu cho Android.
- `app/src/main/java/com/finlux/app/core/designsystem/FinancialInstitutions.kt`: danh mục dùng chung và component logo.
- `app/src/main/java/com/finlux/app/core/designsystem/VietQrBankCatalog.kt`: dữ liệu ngân hàng/VietQR được đồng bộ.
- `app/src/test/java/com/finlux/app/core/designsystem/FinancialInstitutionsTest.kt`: kiểm tra độ phủ, mã duy nhất và logo.
- `tools/sync-financial-institution-icons.ps1`: công cụ đồng bộ lại logo có kiểm tra lỗi.
- `docs/BA_SPEC.md`, `docs/DATA_SPEC.md`, `docs/CONTEXT.md`, `docs/PLAN.md`, `docs/BACKLOG.md`, `docs/UI_SPEC.md`: đồng bộ đặc tả.

### Kết quả thực hiện
- VietQR API trả về 65 tổ chức; tải và xác thực thành công 65/65 logo.
- Bổ sung 4 logo app 512×512 (Payoo, 9Pay, Foxpay, VTC Pay), tái sử dụng 5 vector ví đã có.
- Catalog công khai có 75+ lựa chọn, logo offline đầy đủ cho mọi mục Bank/E-wallet.
- Sửa thuật toán đối chiếu theo 2 pha (exact trước, alias dài nhất sau), loại lỗi `Techcombank` → `MB`.
- Component logo lấy viền/chữ từ `LocalFinluxTokens`/`MaterialTheme`, tương thích Light/Dark/Prism/Classic/Modern.

### Kết quả Unit Test, Build & ADB
- `gradlew testDebugUnitTest`: **183/183 PASS**, 0 fail, 0 error, 0 skipped.
- `gradlew assembleDebug`: **BUILD SUCCESSFUL**.
- Cài ADB: **Success** trên thiết bị `2107119DC (lisa)`.
- Đọc lại package: `com.finlux.app`, `versionCode=140`, `versionName=1.11.6`.
- Smoke launch: `com.finlux.app/.MainActivity` ở trạng thái `topResumedActivity`, không có AndroidRuntime crash.

### Danh sách file đã thực sự chỉnh sửa
- `app/src/main/java/com/finlux/app/core/designsystem/FinancialInstitutions.kt`
- `app/src/main/java/com/finlux/app/core/designsystem/VietQrBankCatalog.kt`
- `app/src/test/java/com/finlux/app/core/designsystem/FinancialInstitutionsTest.kt`
- `app/src/main/res/drawable-nodpi/ic_vietqr_*.png` (65 file)
- `app/src/main/res/drawable-nodpi/ic_ewallet_{payoo,9pay,foxpay,vtcpay}.png`
- `tools/sync-financial-institution-icons.ps1`
- `docs/data/vietqr-financial-institutions.json`
- `docs/FINANCIAL_INSTITUTION_ASSETS.md`
- `docs/BA_SPEC.md`, `docs/DATA_SPEC.md`, `docs/CONTEXT.md`, `docs/PLAN.md`, `docs/BACKLOG.md`, `docs/UI_SPEC.md`
- `CHANGELOG.md`, `HANDOVER_LOG.md`

### Trạng thái
`[DONE]`

---

---
## [DONE] Task: Làm rõ logo ngân hàng và cân chỉnh KPI Trang chủ Prism

**Ngày:** 2026-08-27

### Mục tiêu
- Logo ngân hàng/ví có ảnh thương hiệu phải hiển thị trên nền trắng, không bị chìm trong nền accent gradient.
- Ẩn thẻ `Kỳ tài chính` khỏi Trang chủ nhưng giữ nguyên cấu hình/thông tin trong Cài đặt.
- Căn giữa và cân bằng ba thẻ `Thu tháng này`, `Chi tháng này`, `Dòng tiền ròng`.

### Kết quả
- Logo có ảnh thương hiệu dùng `brandLogoSurface`/`brandLogoBorder`; fallback icon/monogram giữ accent gradient.
- Đã bỏ thẻ `Kỳ tài chính` khỏi Home Prism và giữ nguyên dữ liệu/cách tính chu kỳ tài chính.
- Ba KPI `Thu tháng này`, `Chi tháng này`, `Dòng tiền ròng` có cùng chiều cao và nội dung căn giữa.
- `gradlew testDebugUnitTest assembleDebug`: **BUILD SUCCESSFUL**, **191 test pass, 0 fail/error, 0 skipped**.
- APK debug đã tạo tại `app/build/outputs/apk/debug/app-debug.apk`.
- ADB: chưa cài được bản build này vì thiết bị không xuất hiện trong `adb devices -l` sau khi khởi động lại ADB server.

### Files thực tế đã sửa
- `app/src/main/java/com/finlux/app/core/designsystem/theme/FinluxTokens.kt`
- `app/src/main/java/com/finlux/app/core/designsystem/FinancialInstitutions.kt`
- `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`
- `app/src/test/java/com/finlux/app/core/designsystem/FinluxDesignSystemTest.kt`
- `docs/BA_SPEC.md`
- `docs/UI_SPEC.md`
- `docs/DATA_SPEC.md`
- `docs/CONTEXT.md`
- `docs/PLAN.md`
- `docs/BACKLOG.md`
- `CHANGELOG.md`
- `HANDOVER_LOG.md`

### Trạng thái
`[DONE]`

---

## [DONE] Task: Commit và push bộ cải tiến Home/logo v1.11.9

**Ngày:** 2026-08-27

### Mục tiêu
- Chốt bộ thay đổi logo nền trắng, Home Prism cân KPI và tăng khả năng đọc Liquid Glass.
- Sau khi đồng bộ `origin/main` đã ở v1.11.8, bump `versionCode` 142 → 143 và `versionName` 1.11.8 → 1.11.9.
- Chạy lại toàn bộ unit test/build sau bump, commit đúng scope và push `origin/main`.
- Không đưa artifact `graphify-out` ngoài scope vào commit; không tạo PR, tag hoặc GitHub Release.

### Scope dự kiến
- Toàn bộ source/test/docs của hai task Home/logo đang chờ commit.
- `app/build.gradle.kts`, `CHANGELOG.md`, `HANDOVER_LOG.md`.

### Kết quả kiểm thử sau khi đồng bộ `origin/main`
- `gradlew testDebugUnitTest assembleDebug`: **BUILD SUCCESSFUL**.
- Unit test: **196 pass, 0 fail, 0 error, 0 skipped**.
- APK debug: `app/build/outputs/apk/debug/app-debug.apk`.
- Phạm vi commit loại trừ toàn bộ `graphify-out`.
- Release commit: `fbba5d918faba55eca952fc293f50f96e74bf94c`.
- Push `origin/main`: **SUCCESS**; SHA remote đã khớp SHA local sau push.

### Trạng thái
`[DONE]`

---

## [DONE] Task: Tinh chỉnh khả năng đọc và Liquid Glass Trang chủ Prism

**Ngày:** 2026-08-27

### Mục tiêu
- Giữ ba KPI cân bằng khi số tiền có độ dài khác nhau; tăng độ tương phản nhãn và trạng thái.
- Làm rõ thẻ hero, giảm chi tiết trang trí dễ bị hiểu nhầm là nút thao tác.
- Chuẩn hóa năm lối tắt theo cùng bề rộng và tăng khả năng đọc nhãn.
- Nới bố cục chú giải biểu đồ để tên danh mục, số tiền và tỷ trọng không chèn nhau.
- Tăng chiều sâu kính bằng component Liquid Glass dùng chung, không blur lớp chữ/icon.

### Kết quả
- KPI và thẻ phân tích đã chuyển sang `modern.GlassCard` mode `REGULAR`, giữ lớp quang học phía sau nội dung và spring scale 0.975.
- Ba KPI dùng vùng số tiền cao cố định 24dp và tự co font theo bốn ngưỡng độ dài.
- Hero dùng semantic token `onHero`, tăng tương phản chip; sparkle thu nhỏ để không giống action `+`.
- Năm lối tắt dùng cùng trọng số và nhãn 11.5sp SemiBold; chú giải chart dùng bố cục hai dòng.
- `gradlew testDebugUnitTest assembleDebug`: **BUILD SUCCESSFUL**, **192 test pass, 0 fail/error, 0 skipped**.
- `adb install -r`: **Success** trên `2107119DC (lisa)`; đọc lại `versionName=1.11.7`, `versionCode=141`.
- App không crash (`pidof` hoạt động); chưa chụp QA trực quan được vì thiết bị đang ở trạng thái `Dozing`/NotificationShade.

### Files thực tế đã sửa
- `app/src/main/java/com/finlux/app/core/designsystem/theme/FinluxTokens.kt`
- `app/src/main/java/com/finlux/app/presentation/home/prism/PrismHomeScreen.kt`
- `app/src/test/java/com/finlux/app/presentation/home/prism/PrismHomeLayoutTest.kt`
- `docs/UI_SPEC.md`
- `docs/BA_SPEC.md`
- `docs/DATA_SPEC.md`
- `docs/CONTEXT.md`
- `docs/PLAN.md`
- `docs/BACKLOG.md`
- `CHANGELOG.md`
- `HANDOVER_LOG.md`

### Trạng thái
`[DONE]`

---
