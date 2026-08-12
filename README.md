# Finlux Android

Finlux là ứng dụng quản lý thu chi cá nhân viết bằng Kotlin, Jetpack Compose và Firebase. Source được tổ chức theo Clean Architecture trong một Android module:

```text
app/src/main/java/com/finlux/app/
├── core/           # common result, design system, navigation
├── domain/         # model, repository contract, use case
├── data/           # DataStore, Firebase adapter, demo adapter, DI
└── presentation/   # screen, ViewModel và UiState theo từng tính năng
```

## Chạy project

1. Cài Android Studio hỗ trợ AGP 9.3, JDK 17 và Android SDK 36.
2. Mở thư mục project và chờ Gradle sync.
3. Chạy variant `debug`. Khi chưa cấu hình Firebase, app tự dùng data source demo để kiểm tra UI và luồng giao dịch.

## Kết nối Firebase

1. Xác nhận package name chính thức (hiện tạm là `com.finlux.app`).
2. Tạo Android app trong Firebase Console và tải `google-services.json` vào `app/`.
3. Deploy `firestore.rules` và `storage.rules` sau khi review trên Firebase Emulator.
4. Không commit `google-services.json`, service-account key hoặc signing key.

## Kiểm tra

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

Các UseCase thêm/sửa/xóa giao dịch và kiểm tra mức ngân sách có unit test. Mọi cập nhật `wallet.balance` trong Firebase adapter đều nằm trong `FirebaseFirestore.runTransaction`.
