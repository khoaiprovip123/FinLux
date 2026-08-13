# Changelog

Tất cả những thay đổi quan trọng của dự án FinLux sẽ được ghi lại tại đây.
Định dạng dựa trên [Keep a Changelog](https://keepachangelog.com/vi/1.0.0/) và tuân thủ [Semantic Versioning](https://semver.org/).

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
