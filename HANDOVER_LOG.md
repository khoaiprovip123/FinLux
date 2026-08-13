# HANDOVER LOG - FINLUX APP

## Trạng Thái Dự Án (Project Status)
- **Phiên bản hiện tại:** v1.5.2 (versionCode 55)
- **Trạng thái Build:** ✅ BUILD SUCCESSFUL — 100% Unit Tests PASS, Fix triệt để Bug trừ tiền 2 lần khi thanh toán thông báo
- **Trạng thái Nạp Thiết Bị:** ✅ Nạp APK v1.5.2 thành công cho cả 2 thiết bị (Máy thật + Máy giả lập)

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

