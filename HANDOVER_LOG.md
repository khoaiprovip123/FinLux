# HANDOVER LOG - FINLUX APP

## Trạng Thái Dự Án (Project Status)
- **Phiên bản hiện tại:** v1.8.8 (versionCode 107) - Ready for Testing
- **Trạng thái Build:** 🟢 Đã hoàn tất và kiểm thử thành công trên máy cục bộ.

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

