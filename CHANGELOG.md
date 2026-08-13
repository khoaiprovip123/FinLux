# Changelog

Tất cả những thay đổi quan trọng của dự án FinLux sẽ được ghi lại tại đây.
Định dạng dựa trên [Keep a Changelog](https://keepachangelog.com/vi/1.0.0/) và tuân thủ [Semantic Versioning](https://semver.org/).

## [1.2.0] - 2026-08-13

### Added
- **Đăng nhập Google Sign-In thật:** Tích hợp Android Credential Manager SDK (`GetCredentialRequest` & `GetGoogleIdOption`) để lấy `GoogleIdTokenCredential` và xác thực với Firebase Authentication.
- **Trạng thái Loading & Toast thông báo:** Bổ sung CircularProgressIndicator phủ overlay khi authenticate và Toast thông báo "Sắp ra mắt" cho nút Đăng nhập Apple / Facebook.
- **Phương thức AuthRepository mới:** Bổ sung `signInWithGoogle(idToken: String)` trong domain model, `FirebaseAuthRepository`, và `DemoFinluxRepository`.

### Changed
- **Vô hiệu hóa tạm thời các phương thức Social chưa hỗ trợ:** Nút Apple và Facebook trong `SocialCard` được hiển thị mờ 50% kèm mác `(Sắp có)`.

---

## [1.1.0] - 2026-08-13

### Added
- **FirebaseModule Hilt Provider:** Tạo mới `FirebaseModule.kt` cung cấp `@Singleton` cho `FirebaseAuth?`, `FirebaseFirestore?`, `FirebaseStorage?` kèm cơ chế fallback an toàn sang `DemoFinluxRepository` khi chưa có `google-services.json`.

### Changed
- **Refactor UseCases (SRP):** Tách 14 UseCases độc lập từ `TransactionUseCases.kt` và `ManagementUseCases.kt` ra từng file Kotlin riêng trong package `domain/usecase/`.
- **Tối ưu Hilt DI:** Cập nhật `RepositoryModule.kt` để tự động inject Firebase instances từ `FirebaseModule`.

### Fixed
- **Lỗi hiển thị chữ đen trên nền tối:** Chuẩn hóa `FinluxTheme.kt` và `HomeScreen.kt` sử dụng `MaterialTheme.colorScheme.onSurface` và `LocalContentColor`.
- **Lỗi Navigation SettingsScreen:** Sửa điều hướng tab chính qua `navigateMain()` giữ lại backstack sạch sẽ.
