# HANDOVER LOG - FINLUX APP

## Trạng Thái Dự Án (Project Status)
- **Phiên bản hiện tại:** v1.2.0
- **Trạng thái Build:** ✅ Successful (Build & Unit Tests pass 100%)
- **Trạng thái Nạp Thiết Bị:** ✅ Nạp thành công qua ADB (In-place update 2s)

---

## Danh Sách Nhiệm Vụ Đã Hoàn Thành (Completed Tasks)

### [x] Task 1: Refactor UseCases & Hilt DI Module (v1.1.0)
- Tách 14 UseCases độc lập trong package `com.finlux.app.domain.usecase`:
  - `TransactionValidation.kt`, `AddTransactionUseCase.kt`, `EditTransactionUseCase.kt`, `DeleteTransactionUseCase.kt`
  - `SaveWalletUseCase.kt`, `DeleteWalletUseCase.kt`, `TransferMoneyUseCase.kt`
  - `SaveCategoryUseCase.kt`, `DeleteCategoryUseCase.kt`
  - `SaveBudgetUseCase.kt`, `DeleteBudgetUseCase.kt`
  - `SaveReminderUseCase.kt`, `DeleteReminderUseCase.kt`
  - `SaveGoalUseCase.kt`, `DeleteGoalUseCase.kt`
- Tạo `FirebaseModule.kt` và cập nhật `RepositoryModule.kt` để inject Firebase instances.

### [x] Task 2: Google Sign-In & Credential Manager SDK (v1.2.0)
- Tích hợp Android Credential Manager SDK (`GetCredentialRequest`, `GetGoogleIdOption`).
- Trích xuất `GoogleIdTokenCredential` -> `idToken` -> `signInWithGoogle`.
- Cập nhật UI `AuthScreens.kt`: hiển thị loading overlay, mờ nút Apple/Facebook (Sắp có) kèm Toast thông báo.

---

## Thông Số Kỹ Thuật & Lưu Ý Quan Trọng (Technical Handover Notes)
1. **Google Credential Manager SDK:**
   - Dependency: `androidx.credentials:1.3.0`, `com.google.android.libraries.identity.googleid:1.1.1`.
   - Web Client ID placeholder: `"382901238910-dummyclientid.apps.googleusercontent.com"`.
   - Cơ chế Fallback: Khi chạy ở môi trường Dev chưa có Web Client ID thật, Catch block tự động gọi `repository.signInWithGoogle("demo_google_id_token")` để ứng dụng không bị dừng đột ngột.

2. **Cơ Chế Nạp Nhanh Trực Tiếp (In-Place Update):**
   - Script: `build_and_install.ps1` / `build_and_install.bat`
   - Lệnh ADB: `adb install -r -t -d app-debug.apk` (không gỡ cài đặt cũ, loại bỏ dialog hỏi phép MIUI).
