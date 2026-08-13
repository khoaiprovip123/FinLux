# Changelog

Tất cả những thay đổi quan trọng của dự án FinLux sẽ được ghi lại tại đây.
Định dạng dựa trên [Keep a Changelog](https://keepachangelog.com/vi/1.0.0/) và tuân thủ [Semantic Versioning](https://semver.org/).

## [1.4.6] - 2026-08-13

### Fixed
- **Google Auth CredentialProvider Compatibility:** Khai báo trực tiếp dependency `com.google.android.gms:play-services-auth:21.3.0` giúp `CredentialManager` của Android định vị thành công Play Auth Provider, khắc phục triệt để ngoại lệ `GetCredentialProviderConfigurationException` trên Android Emulator và thiết bị Android 13 trở xuống.

### Added
- **Hỗ trợ nạp APK đa thiết bị trong `build_and_install.ps1`:** Nâng cấp script tự động lọc danh sách tất cả các thiết bị ADB đang kết nối (Wireless / USB / Emulator) và cài đè APK song song thành công cho toàn bộ thiết bị.

---

## [1.4.5] - 2026-08-13

### Changed
- **Tên hiển thị ứng dụng (App Launcher Display Name):** Đổi nhãn hiển thị icon ứng dụng trên màn hình điện thoại từ `Finlux` thành **`Finance Luxury`** (`app/src/main/res/values/strings.xml`).

---

## [1.4.4] - 2026-08-13

### Added
- **Shared Project Debug Keystore (`app/debug.keystore`):** Đưa file keystore cố định vào repository tại đường dẫn `app/debug.keystore` để tất cả thành viên trong dự án dùng chung 1 chữ ký debug duy nhất.
- **Cấu hình Gradle `signingConfigs.debug`:** Cập nhật `app/build.gradle.kts` đảm bảo kiểu build `debug` tự động ký bằng `app/debug.keystore`.

### Changed
- Đồng bộ hóa mã SHA-1 Google Auth trên Firebase Console cho tất cả môi trường phát triển của nhóm.

---

## [1.4.3] - 2026-08-13

### Fixed
- **BudgetViewModel: spentAmount cộng dồn sai khi có cả giao dịch modern + legacy:** Sửa logic từ `?:` (short-circuit OR) sang `+` (cộng dồn) — nay gom tất cả giao dịch chi tiêu khớp theo `categoryId` (modern) **VÀ** khớp theo `category.name` (legacy fallback cho giao dịch phiên bản cũ), không bỏ sót bên nào. Guard tránh double-count khi `categoryId.lowercase() == name.lowercase()`.
- **Màn hình Ngân sách: "Còn lại" hiển thị số rút gọn:** Đổi sang `toVnd()` (ví dụ `Còn lại 1.225.000 ₫`) thay vì `toShortVnd()` (làm tròn thô như `1,2tr ₫`).

### Added
- **Unit Tests — BudgetViewModelTest (7 test cases):** Thêm 2 test mới cho kịch bản fallback:
  - `legacyTransactionWithCategoryNameFallbackCalculatesSpentAmountCorrectly`: giao dịch cũ lưu `categoryId = "An uong"` (tên danh mục) vẫn được tính đúng vào ngân sách qua name fallback.
  - `mixedModernAndLegacyTransactionsAccumulateSpentAmountCorrectly`: cộng dồn đúng cả 2 loại tx trong cùng 1 budget.
- **AGENTS.md — Document Management SOP:** Bổ sung quy tắc bắt buộc HANDOVER_LOG PRE/POST-EXECUTION và CHANGELOG chỉ được ghi sau khi test PASS + build thành công.

---

## [1.4.2] - 2026-08-13

### Fixed
- **"Ngân sách còn lại" không cập nhật sau giao dịch:** `HomeViewModel` nay tính `spentAmount` động từ `observeMonth(transactions)` grouped by `categoryId`, thay vì đọc trường `spentAmount` stored trong Firestore. Card cập nhật ngay lập tức khi bất kỳ giao dịch nào được thêm/sửa/xóa.
- **Item giao dịch gần nhất thiếu ngày giờ:** `ReferenceTransactionRow` nay luôn hiển thị cả 2 dòng: dòng Ghi chú (nếu có) + dòng Thời gian ("Hôm nay, HH:mm" / "Hôm qua, HH:mm" / "dd/MM/yyyy, HH:mm").

### Added
- `TransactionRepository.observeMonth(month: YearMonth)` — Flow real-time tất cả transactions trong tháng, dùng cho HomeViewModel và có thể tái sử dụng.

---

## [1.4.1] - 2026-08-13


### Fixed
- **Budget spentAmount không cập nhật real-time (BR-06):** `addWithBalanceUpdate`, `editWithBalanceUpdate`, `deleteWithBalanceUpdate` trong `FirebaseTransactionRepository` nay cập nhật `budget.spentAmount` ngay trong cùng Firestore atomic transaction khi giao dịch là `EXPENSE`. Khắc phục lỗi bấm `[Đã thanh toán]` trên Push Notification không phản ánh lên thanh tiến độ ngân sách.

### Added
- **Unit Tests BudgetViewModel (5 tests):** `BudgetViewModelTest` kiểm tra các kịch bản: `SAFE (0%)`, `SAFE (40%)` sau pay action từ notification, `WARNING (84%)`, `EXCEEDED (100%)`, và nhiều pay action liên tiếp dẫn đến `EXCEEDED`. Tổng 33/33 tests PASS.

---

## [1.4.0] - 2026-08-13


### Added
- **Push Notification Quick Actions (UC-18):** Thêm 2 nút bấm thao tác nhanh ngay trên thông báo Android khi đến hạn nhắc nhở:
  - `[Đã thanh toán]`: Tự động gọi `AddTransactionUseCase` tạo giao dịch chi tiêu mới và trừ số dư ví gán sẵn trong Firestore, sau đó đóng thông báo.
  - `[Nhắc lại sau 1h]`: Đặt lại lịch `AlarmManager` lùi 60 phút.
- **Khôi Phục Báo Thức Sau Reboot (BootReceiver):** Tạo `BootReceiver` lắng nghe `android.intent.action.BOOT_COMPLETED` để tự động đọc danh sách nhắc nhở từ Firestore/Local và khôi phục báo thức `AlarmManager` khi thiết bị khởi động lại.

---

## [1.3.1] - 2026-08-13

### Added
- **Định Dạng Tiền Tệ Tự Động (Currency Format Preview):** Tất cả các ô nhập số tiền (`AddTransactionSheet`, `BudgetEditor`, `WalletEditor`, `TransferEditor`, `ReminderEditor`, `GoalsScreen`) được bổ sung dòng Text Preview định dạng phân cách hàng nghìn (`x.xxx.xxx đ`). Nếu rỗng hoặc 0 sẽ hiển thị `0 đ`.
- **Nút Quay Lại (Back Button) Trên TopBar:** Bổ sung nút Quay lại (`ArrowBack`) ở góc trái Header cho toàn bộ các màn hình phụ (`BudgetScreen`, `WalletsScreen`, `ReportsScreen`, `TransactionsScreen`, `NotificationsScreen`...) gọi `navController.popBackStack()`.

---

## [1.3.0] - 2026-08-13

### Added
- **Modal Lưới Chọn Danh Mục (UC-12):** Thêm ModalBottomSheet chứa lưới 3 cột hiển thị toàn bộ danh mục theo loại Thu nhập/Chi tiêu trong `AddTransactionSheet.kt`.
- **Tạo Danh Mục Tùy Chỉnh (UC-12):** Nút `+ Tạo mới` cho phép nhập Tên, chọn Biểu tượng (Icon) và Màu sắc (Color Hex), gọi `SaveCategoryUseCase` để lưu vào Firestore/Database.
- **Quản lý (Sửa/Xóa) Danh Mục Bằng Nhấn Giữ (Long-Click):** Sự kiện `combinedClickable(onLongClick)` trên từng Card danh mục tùy chỉnh cho phép mở menu Chỉnh sửa hoặc Xóa danh mục (kèm AlertDialog xác nhận và `DeleteCategoryUseCase`).
- **Khóa Bảo Vệ Danh Mục Mặc Định:** Hiển thị Toast thông báo *"Danh mục mặc định không thể sửa/xóa"* khi người dùng nhấn giữ danh mục hệ thống.

---

## [1.2.1] - 2026-08-13

### Fixed
- **Xử lý An Toàn Firestore Security Rules (PERMISSION_DENIED):** Bọc khối `try-catch (e: FirebaseFirestoreException)` cho các tác vụ Firestore (`seedNewUser`, `register`, `signInWithGoogle`, `updateDisplayName`, `updateAvatar`). Log cảnh báo `Log.w("Firestore", ...)` và cho phép đăng nhập Firebase Auth thành công ngay cả khi Firestore chưa gán quyền.
- **Tối Ưu Unblock UI & Timeout 15 giây:** Khối `finally { mutableState.update { it.copy(isLoading = false) } }` trong `AuthViewModel.kt` đảm bảo UI loading overlay không bao giờ bị xoay vô hạn. Bọc tiến trình xác thực bằng `withTimeoutOrNull(15000)` tự động ngắt sau 15s.
- **Tự Động Lấy Client ID Động:** Lấy `serverClientId` trực tiếp từ `R.string.default_web_client_id` do Google Services Plugin tự động sinh từ `google-services.json`.
- **Bảo Vệ Layout Chống Bể Giao Diện:** Bổ sung `maxLines = 1`, `overflow = TextOverflow.Ellipsis`, và `Modifier.weight(1f, fill = false)` trên `HomeScreen.kt` và `SettingsScreen.kt` chống vỡ layout với tên/email dài.
- **Cập Nhật [firestore.rules](file:///d:/Sources/FinLux/firestore.rules):** Cập nhật bộ quy tắc `allow read, write: if request.auth != null;`.

---

## [1.2.0] - 2026-08-13

### Added
- **Đăng nhập Google Sign-In thật (UC-03):** Tích hợp Android Credential Manager SDK (`GetCredentialRequest` & `GetGoogleIdOption`) để lấy `GoogleIdTokenCredential` và xác thực với Firebase Authentication.
- **Trạng thái Loading & Overlay:** Bổ sung CircularProgressIndicator phủ overlay khi ứng dụng đang trong quá trình authenticate.
- **Thông báo cho các phương thức Social chưa hỗ trợ:** Hiển thị Toast thông báo *"Tính năng đăng nhập qua Apple/Facebook sắp ra mắt!"* khi nhấp vào.
- **Phương thức AuthRepository mới:** Bổ sung `signInWithGoogle(idToken: String)` trong domain model, `FirebaseAuthRepository`, và `DemoFinluxRepository`.

### Changed
- **Vô hiệu hóa tạm thời nút Apple & Facebook:** Nút Apple và Facebook trong `SocialCard` được hiển thị mờ 50% kèm mác `(Sắp có)`.

---

## [1.1.0] - 2026-08-13

### Added
- **FirebaseModule Hilt Provider:** Tạo mới `FirebaseModule.kt` cung cấp `@Singleton` cho `FirebaseAuth?`, `FirebaseFirestore?`, `FirebaseStorage?` kèm cơ chế fallback an toàn sang `DemoFinluxRepository` khi chạy ở môi trường Dev chưa cấu hình `google-services.json`.

### Changed
- **Refactor UseCases (SRP):** Tách 14 UseCases độc lập từ `TransactionUseCases.kt` và `ManagementUseCases.kt` ra từng file Kotlin riêng trong package `domain/usecase/` (`AddTransactionUseCase`, `EditTransactionUseCase`, `DeleteTransactionUseCase`, `SaveWalletUseCase`, `DeleteWalletUseCase`, `TransferMoneyUseCase`, `SaveCategoryUseCase`, `DeleteCategoryUseCase`, `SaveBudgetUseCase`, `DeleteBudgetUseCase`, `SaveReminderUseCase`, `DeleteReminderUseCase`, `SaveGoalUseCase`, `DeleteGoalUseCase`).
- **Tối ưu Hilt DI:** Cập nhật `RepositoryModule.kt` để tự động inject Firebase instances từ `FirebaseModule`.

### Fixed
- **Lỗi hiển thị chữ đen trên nền tối:** Chuẩn hóa `FinluxTheme.kt` và `HomeScreen.kt` sử dụng `MaterialTheme.colorScheme.onSurface` và `LocalContentColor`.
- **Lỗi Navigation SettingsScreen:** Sửa điều hướng tab chính qua `navigateMain()` giữ lại backstack sạch sẽ.

---

## [1.0.0] - 2026-08-12

### Added
- **Khởi tạo Dự án FinLux:** Xây dựng ứng dụng quản lý tài chính cá nhân Clean Architecture (3 layers: Domain, Data, Presentation) sử dụng Kotlin, Jetpack Compose, Hilt, DataStore, và Firebase.
- **Giao diện Liquid Glass Design:** Thiết kế hệ thống UI Liquid Glass với hiệu ứng kính làm mờ (glassmorphism), màu sắc dark mode/light mode tự động điều chỉnh.
- **Quản lý Giao dịch (UC-07 -> UC-10):** Thêm/Sửa/Xóa giao dịch thu chi cá nhân với **Firestore Atomic Transactions** tự động cập nhật số dư ví tức thì (BR-06, BR-14).
- **Quản lý Ví & Danh mục (UC-11 -> UC-13):** Tạo ví tài khoản, danh mục thu chi, chuyển tiền giữa các ví.
- **Báo cáo & Thống kê (UC-16, UC-17):** Hiển thị biểu đồ phân tích chi tiêu theo tháng, danh mục.
- **Hệ thống Demo Repository:** `DemoFinluxRepository` cho phép chạy và kiểm thử ứng dụng đầy đủ tính năng ngay cả khi chưa kết nối Firebase backend.
