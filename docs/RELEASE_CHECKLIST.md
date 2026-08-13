# RELEASE CHECKLIST (RELEASE_CHECKLIST.md)

Danh sách các bước kiểm tra bắt buộc trước khi đóng gói phát hành bản APK/AAB cho UAT hoặc Google Play Store:

## 1. Environment & Build Configuration
- [ ] Cập nhật `versionCode` và `versionName` trong `app/build.gradle.kts`.
- [ ] Chắc chắn rằng `JAVA_HOME` trỏ đúng JDK 17.
- [ ] Chạy lệnh làm sạch và kiểm thử tự động:
  ```powershell
  .\gradlew.bat clean
  .\gradlew.bat testDebugUnitTest
  ```

## 2. Security & Obfuscation Verification
- [ ] File `google-services.json` chính thức của môi trường Production đã được đặt vào `app/`.
- [ ] Bật `minifyEnabled true` và `shrinkResources true` cho release build variant.
- [ ] Đảm bảo không có signing key hay service account credentials nào bị lộ trong APK assets.

## 3. Production Deployment Commands
- Build file Android App Bundle (AAB) cho Google Play Store:
  ```powershell
  .\gradlew.bat bundleRelease
  ```
- Build file APK Release trực tiếp:
  ```powershell
  .\gradlew.bat assembleRelease
  ```

## 4. Post-Release Verification
- [ ] Tải file AAB / APK phát hành lên môi trường thử nghiệm Android Vitals / Firebase App Distribution.
- [ ] Đăng nhập và thực hiện 1 giao dịch mẫu trên bản Release để xác nhận Obfuscation không làm hỏng Reflection (Hilt/Kotlinx Serialization).
- [ ] Ghi nhãn Release Tag trên Git (`git tag -a vX.Y.Z -m "Release vX.Y.Z"`).
