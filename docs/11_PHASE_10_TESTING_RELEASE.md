# PHASE 10 — TESTING & RELEASE PIPELINE (11_PHASE_10_TESTING_RELEASE.md)

## 1. Objective (Mục tiêu)
Xây dựng toàn bộ hệ thống kiểm thử tự động (Automated Testing Pipeline), kiểm thử hồi quy (Regression Testing), tối ưu hóa hiệu năng (Performance Optimization) và chuẩn bị quy trình phát hành ứng dụng (Release Pipeline) trên Google Play Store.

## 2. Testing Pyramid & Strategy
- **Unit Tests (70%)**: JUnit 5 + MockK + Turbine thử nghiệm 100% UseCases và ViewModels.
- **Integration Tests (20%)**: Thử nghiệm tích hợp Firestore Adapter với Firebase Emulator Suite.
- **UI Compose Tests (10%)**: Compose UI Test Suite kiểm thử các luồng người dùng cốt lõi (Login -> Home -> Add Transaction).

## 3. Performance & Benchmark Targets
- **Recomposition Optimization**: Đảm bảo 0 Recomposition thừa trong các màn hình danh sách dài (LazyColumn).
- **Startup Time**: Chế độ Cold Start dưới 1.2 giây; Warm Start dưới 400ms.
- **APK Size**: Kích thước file APK release tối ưu dưới 18MB nhờ ProGuard / R8 Shrinking & Resource Stripping.

## 4. Release Checklist & Play Store Compliance
- [ ] Cấu hình signing key chuẩn mã hóa SHA-256.
- [ ] Bật R8 / ProGuard minification & obfuscation (`minifyEnabled true`).
- [ ] Khai báo đầy đủ Google Play Data Safety form (Bảo mật thông tin Auth & Tài chính).
- [ ] Hoàn thiện Play Store App Listing (Screenshots, Feature Graphic, Description).

## 5. Exit Criteria & DoD
- [x] Unit test suite chạy thành công 100% (`.\gradlew.bat testDebugUnitTest`).
- [x] Build bản Release APK / AAB thành công (`.\gradlew.bat bundleRelease`).
- [x] Ứng dụng sẵn sàng phát hành UAT / Production.
