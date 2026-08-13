# HANDOVER LOG - FINLUX APP

## Trạng Thái Dự Án (Project Status)
- **Phiên bản hiện tại:** v1.4.3 (versionCode 37 — auto-bumped bởi build_and_install.ps1)
- **Trạng thái Build:** ✅ BUILD SUCCESSFUL — 7/7 BudgetViewModelTest PASS, toàn bộ suite PASS 100%
- **Trạng thái Nạp Thiết Bị:** ✅ Nạp thành công qua ADB (Streamed Install - Success, App tự động mở)

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

