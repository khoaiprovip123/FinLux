# HANDOVER LOG - FINLUX APP

## [DONE] Task: Fix Navigation Swipe Stutter, Prevent Home Reload & Safe Budget Transaction Handling (v1.6.7 - Code 86)

**Ngày hoàn thành:** 2026-08-15

### Mục tiêu
- Khắc phục triệt để lỗi crash Firestore atomic update `NOT_FOUND` khi ghi nhận giao dịch vào danh mục không có sẵn ngân sách tháng.
- Tinh chỉnh `FinluxNavHost.kt`: dọn dẹp các khai báo trùng lặp, tối ưu nhận diện cử chỉ vuốt ngang chuyển tab (Trang chủ ↔ Ví ↔ Báo cáo ↔ Hồ sơ) không giật khựng với cuộn dọc.
- Ngăn chặn việc tải lại / render lại khi người dùng bấm vào tab Home đang mở hoặc chuyển qua lại giữa các tab (`saveState = true`, `restoreState = true`, `if (route != currentRoute)`).

### Scope và file thực tế chỉnh sửa
- `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseTransactionRepository.kt`
- `app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt`
- `app/build.gradle.kts`
- `CHANGELOG.md`
- `HANDOVER_LOG.md`

### Kết quả kiểm thử
- `gradlew testDebugUnitTest`: 100% PASS (BUILD SUCCESSFUL).
- Phiên bản: `v1.6.7` - versionCode `86`.

---

## [DONE] Task: Audit & Complete App-Wide Callstack Liquid Glass Migration

**Ngày bắt đầu:** 2026-08-14

### Mục tiêu
- Đối chiếu implementation hiện tại với upstream `callstack/liquid-glass` và quy chuẩn Android Native trong skill `callstack-liquid-glass`.
- Chuẩn hóa toàn bộ UI/UX FinLux về một bộ primitive dùng chung: `CLEAR/REGULAR/NONE`, tint, color scheme, spring interaction, chromatic prism rim, ambient glow và fallback API thấp.
- Loại bỏ các bề mặt kính/blur riêng lẻ trong feature; bảo đảm chữ/icon không bị blur, bottom navigation là floating capsule dock và toàn bộ màn hình thích ứng Light/Dark.
- Giữ nguyên nghiệp vụ, repository và các thao tác Firestore Transaction; phạm vi thay đổi chỉ thuộc presentation/design system.

### Scope và file dự kiến chỉnh sửa
- `app/src/main/java/com/finlux/app/core/designsystem/FinluxTheme.kt`
- `app/src/main/java/com/finlux/app/core/designsystem/LiquidGlass.kt`
- `app/src/main/java/com/finlux/app/core/designsystem/WaterGlass.kt`
- `app/src/main/java/com/finlux/app/core/designsystem/StyleBackdrop.kt`
- `app/src/main/java/com/finlux/app/core/designsystem/ReferenceComponents.kt`
- `app/src/main/java/com/finlux/app/presentation/**/**Screen.kt`
- `app/src/main/java/com/finlux/app/presentation/components/MainBottomBar.kt`
- `UI_SPEC.md`, `HANDOVER_LOG.md`; `CHANGELOG.md` chỉ cập nhật sau khi test và build thành công.

### Trạng thái
`[DONE]`

### Kết quả triển khai
- Port contract của `callstack/liquid-glass` sang Jetpack Compose native: `CLEAR/REGULAR/NONE`,
  prism rim 1.2dp, tint/glow token, fallback API < 31, press spring 0.975 và haptic feedback.
- Đồng bộ backdrop, top bar, floating dock, FAB, card/panel, alert dialog và bottom sheet qua
  `core/designsystem`; lớp quang học luôn nằm sau chữ/icon.
- Khôi phục ba phong cách người dùng chọn với token sáng/tối riêng; lựa chọn theme và phong cách
  áp dụng thống nhất toàn ứng dụng.
- Nối gesture thật vào `NavHost`: Trang chủ ↔ Ví ↔ Báo cáo ↔ Hồ sơ, kéo bám ngón tay,
  edge resistance, directional glow và spring settle.
- Hồ sơ hiển thị Giới thiệu FinLux ở đầu, sau đó là ảnh đại diện/tên; Auth có ba thẻ Google,
  Apple, Facebook và callback sẵn để cấu hình provider.

### File thực tế đã chỉnh sửa
- Design system: `FinluxBrand.kt`, `FinluxComponents.kt`, `FinluxTheme.kt`, `LiquidGlass.kt`,
  `NotificationPermissionHandler.kt`, `ReferenceComponents.kt`, `StyleBackdrop.kt`, `WaterGlass.kt`.
- Navigation/components: `FinluxNavHost.kt`, `MainBottomBar.kt`.
- Presentation: `AuthScreens.kt`, `CategoriesScreen.kt`, `ExpenseScreen.kt`, `IncomeScreen.kt`,
  `NotificationsScreen.kt`, `RemindersScreen.kt`, `SettingsScreen.kt`, `AddTransactionSheet.kt`,
  `TransactionsScreen.kt`, `WalletsScreen.kt`.
- Version/docs: `app/build.gradle.kts`, `UI_SPEC.md`, `CHANGELOG.md`, `HANDOVER_LOG.md`.

### Kiểm thử, build và cài đặt
- `gradlew testDebugUnitTest`: **36/36 PASS**, 0 failure, 0 error, 0 skipped.
- `gradlew assembleDebug`: **BUILD SUCCESSFUL**.
- `gradlew lintDebug`: **BUILD SUCCESSFUL**, 0 lint error (34 warning dependency/tooling hiện hữu).
- APK: `app/build/outputs/apk/debug/app-debug.apk` (30,016,107 bytes), SHA-256
  `5A951FDDBC2A2999D9155C62A7D02CF28172D1FCBE27BB9B4A226C850C849B51`.
- ADB tại thời điểm bàn giao: không có thiết bị ở trạng thái `device`, vì vậy chưa thể cài lại
  và chưa ghi nhận QA trực quan trên máy thật trong lượt này.

## Trạng Thái Dự Án (Project Status)
- **Phiên bản hiện tại:** v1.6.6 (versionCode 83)
- **Trạng thái Build:** ✅ BUILD SUCCESSFUL — Callstack Liquid Glass native Compose, 36/36 unit test pass. APK debug đã tạo; thiết bị ADB hiện chưa online nên chưa cài lại trong lượt này.

---

## [DONE] Task v1.6.6: Comprehensive App-Wide Callstack Liquid Glass Implementation

**Ngày:** 2026-08-14

### Mục tiêu
- Tái cấu trúc và nâng cấp toàn diện giao diện ứng dụng FinLux trên nền tảng Jetpack Compose, tuân thủ 100% quy chuẩn Callstack Liquid Glass (iOS 26 / VisionOS UIGlassEffect).
- Chuẩn hóa các primitive Liquid Glass (`LiquidGlassMode`, `LiquidGlassSurface`, `LiquidGlassCard`, `LiquidGlassCapsule`, `LiquidGlassOrbButton`, `LiquidGlassTextField`).
- Cập nhật toàn bộ các màn hình chức năng: `HomeScreen`, `TransactionsScreen`, `WalletsScreen`, `BudgetScreen`, `ReportsScreen`, `SettingsScreen`.
- Chạy kiểm thử unit test 100% pass, build APK và nạp vào thiết bị Android.

### Kết quả & Danh sách file đã chỉnh sửa
| File | Thay đổi |
|---|---|
| `LiquidGlass.kt` | ✅ Bổ sung `LiquidGlassMode` (CLEAR, REGULAR, NONE), `LiquidGlassCapsule` cho tab/filter, viền lăng kính tán sắc `1.2.dp` và hiệu ứng tương tác đàn hồi spring physics |
| `WaterGlass.kt` | ✅ Chuẩn hóa `WaterGlassCard` sang `LiquidGlassCard(mode = CLEAR)` với ambient colored glow |
| `FinluxTheme.kt` | ✅ Tối ưu tokens phản quang và tương thích ánh sáng tự nhiên Light/Dark mode |
| `MainBottomBar.kt` | ✅ Tinh giản active indicator thành viên nang nước phát sáng mềm mại |
| `HomeScreen.kt` | ✅ Thẻ Hero Balance phát quang, quick metrics lăng kính, danh sách giao dịch phân nhóm rõ nét |
| `TransactionsScreen.kt` | ✅ Bộ lọc `LiquidGlassCapsule`, thẻ tổng quan trong suốt, nền LiquidAuraBackdrop |
| `WalletsScreen.kt` | ✅ Thẻ tổng tài sản kính lỏng, bảo toàn nền kính vuốt thao tác |
| `BudgetScreen.kt` | ✅ Tiến độ ngân sách viên nang nước, thẻ kính cảnh báo chi tiêu |
| `ReportsScreen.kt` | ✅ Biểu đồ dòng tiền và cơ cấu phân bổ trên nền kính pha lê trong suốt |
| `SettingsScreen.kt` | ✅ Thẻ hồ sơ ProfileHero kính mờ, capsule tùy biến độ nổi kính |

---

## [DONE] Task v1.6.5: Full Callstack Liquid Glass UI Overhaul

**Ngày:** 2026-08-14

### Mục tiêu
- **Floating Liquid Glass Dock (`LiquidGlass.kt` & `MainBottomBar.kt`):** Tái thiết kế Bottom Navigation thành viên nang kính nổi lơ lửng (`floating capsule dock`), bo góc 34.dp, cách đáy và 2 cạnh 14.dp, viền tán sắc quang học Chromatic Prism Rim, nền kính mờ xuyên thấu vệt sáng nền.
- **Liquid Glass Hero Balance Card (`HomeScreen.kt`):** Chuyển đổi thẻ tài sản từ khối màu phẳng sang thẻ kính lỏng đa tầng với vệt sáng cong phản xạ (specular glossy sheen), viền vát 3D phát quang và nút ẩn/hiện số dư bằng kính mờ.
- **Translucent Refractive Metric Cards (`HomeScreen.kt` & `WaterGlass.kt`):** Thêm viền tán sắc đa tầng, đổ bóng môi trường màu sắc (Ambient Colored Glow) cho các thẻ Thu/Chi/Ngân sách.
- **Bump Version:** Nâng `versionName` lên `1.6.5` và `versionCode` `77`.

### Kết quả & Danh sách file đã chỉnh sửa
| File | Thay đổi |
|---|---|
| `LiquidGlass.kt` | ✅ Triển khai `GlassBottomNav` dạng Floating Capsule Dock (bo góc 34dp, cách viền) + Refraction Rim & Specular Sheen; nâng cấp `GlassFab` Orb |
| `WaterGlass.kt` | ✅ Tối ưu `WaterGlassCard` với viền quang phổ tán sắc và lớp specular highlight mép trên |
| `MainBottomBar.kt` | ✅ Tinh giản Destination items thành custom tab fluid, tích hợp specular tab capsule |
| `HomeScreen.kt` | ✅ Tái thiết kế `ReferenceBalanceHero` đa tầng kính, nút ẩn/hiện Frosted Pill và nâng cấp thẻ chỉ số |
| `app/build.gradle.kts` | ✅ Bump `versionCode 77`, `versionName 1.6.5` |
| `HANDOVER_LOG.md` | ✅ Cập nhật log PRE/POST-EXECUTION |
| `CHANGELOG.md` | ✅ Thêm mục phiên bản release v1.6.5 |

### Kết quả Kiểm thử & Nạp thiết bị
- **Unit Tests & Build:** `./gradlew testDebugUnitTest assembleDebug` ➡️ **BUILD SUCCESSFUL**.
- **ADB Install:** Đã nạp thành công vào thiết bị `7f4ca06a`.

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.6.4: Fix Blurry Bottom Navigation Bar

**Ngày:** 2026-08-14

### Mục tiêu
- **Khắc phục lỗi mờ đục toàn bộ thanh menu:** Gỡ bỏ `finluxBackgroundBlur` trên thẻ `BottomAppBar` trong `GlassBottomNav` (RenderEffect áp dụng trực tiếp lên container làm mờ cả icon, text và FAB con); thay thế bằng màu nền kính mờ `surface` sắc nét kèm viền phản quang vát cạnh và bóng đổ nổi.
- **Bump Version:** Nâng `versionName` lên `1.6.4` và `versionCode` `74`.

### Kết quả & Danh sách file đã chỉnh sửa
| File | Thay đổi |
|---|---|
| `LiquidGlass.kt` | ✅ Loại bỏ `finluxBackgroundBlur` trực tiếp trên `BottomAppBar`, giữ màu kính frosted sắc nét |
| `app/build.gradle.kts` | ✅ Bump `versionCode 74`, `versionName 1.6.4` |
| `HANDOVER_LOG.md` | ✅ Cập nhật log PRE/POST-EXECUTION |
| `CHANGELOG.md` | ✅ Thêm mục phiên bản release v1.6.4 |

### Kết quả Kiểm thử & Nạp thiết bị
- **Unit Tests & Build:** `./gradlew testDebugUnitTest assembleDebug` ➡️ **BUILD SUCCESSFUL**.
- **ADB Install:** Đã nạp thành công vào thiết bị `7f4ca06a`.

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.6.3: Liquid Glass Bottom Navigation & Floating Orb FAB Redesign

**Ngày:** 2026-08-14

### Mục tiêu
- **Liquid Glass Bottom Navigation Bar (`GlassBottomNav`):** Cấu hình bề mặt kính mờ trong suốt (`finluxBackgroundBlur`), viền phản quang gradient vát cạnh (`top refraction rim`), bóng ambient mềm mại và góc bo 28.dp.
- **Tối ưu Tab Navigation Items:** Loại bỏ hoàn toàn viền squircle thô trên các tab không chọn (Ví, Báo cáo, Hồ sơ); bổ sung hiệu ứng chuyển đổi mượt mà với spring scale và gradient indicator cho tab đang chọn.
- **Liquid Glass Orb FAB:** Tái thiết kế nút Thêm (+) trung tâm thành quả cầu kính khúc xạ đa tầng (Liquid Orb) với dải gradient 3 màu (Purple -> Blue -> Cyan), đường phản quang ánh sáng trắng (specular crescent highlight) và bóng đổ phát sáng (radiant glow).
- **Bump Version:** Nâng `versionName` lên `1.6.3` và `versionCode` `72`.

### Kết quả & Danh sách file đã chỉnh sửa
| File | Thay đổi |
|---|---|
| `LiquidGlass.kt` | ✅ Nâng cấp `GlassBottomNav` với viền khúc xạ đa sắc & `finluxBackgroundBlur`; tái cấu trúc `GlassFab` thành Liquid Glass Orb đa lớp với specular light |
| `MainBottomBar.kt` | ✅ Tinh giản tab items (loại bỏ hộp thô trên tab inactive), thêm active liquid capsule và spring animation |
| `app/build.gradle.kts` | ✅ Bump `versionCode 72`, `versionName 1.6.3` |
| `HANDOVER_LOG.md` | ✅ Cập nhật log PRE/POST-EXECUTION |
| `CHANGELOG.md` | ✅ Thêm mục phiên bản release v1.6.3 |

### Kết quả Kiểm thử
- **Unit Tests:** `./gradlew testDebugUnitTest` ➡️ **BUILD SUCCESSFUL** (100% PASS).

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.6.2: Auth Screen Polish - Remove Unused Social Logins & Fill Bottom Surface Gap

**Ngày:** 2026-08-14

### Mục tiêu
- **Remove Apple & Facebook Logins:** Loại bỏ các nút placeholder không dùng (Apple, Facebook), chuyển đổi nút Google thành card đăng nhập chính toàn chiều ngang ("Tiếp tục với Google").
- **Lấp đầy khoảng trống đáy màn hình:** Sử dụng `BoxWithConstraints` và `defaultMinSize` để thẻ `Surface` form đăng nhập tự động kéo dãn lấp đầy toàn bộ phần đáy màn hình, loại bỏ khoảng hở màu nền phía dưới điều khoản sử dụng.
- **Bump Version:** Nâng `versionName` lên `1.6.2` và `versionCode` `71`.

### Kết quả & Danh sách file đã chỉnh sửa
| File | Thay đổi |
|---|---|
| `AuthScreens.kt` | ✅ Loại bỏ nút Apple & Facebook; chuẩn hóa Google Sign-In button tràn viền; áp dụng `BoxWithConstraints` + `defaultMinSize` cho thẻ Surface |
| `app/build.gradle.kts` | ✅ Bump `versionCode 71`, `versionName 1.6.2` |
| `HANDOVER_LOG.md` | ✅ Cập nhật log PRE/POST-EXECUTION |
| `CHANGELOG.md` | ✅ Thêm mục phiên bản release v1.6.2 |

### Kết quả Kiểm thử
- **Unit Tests:** `./gradlew testDebugUnitTest` ➡️ **BUILD SUCCESSFUL** (100% PASS).

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.6.1: Liquid Glass UI & Accessibility Polish

**Ngày:** 2026-08-14

### Mục tiêu
- **Container Batching (`GlassEffectContainer`):** Bổ sung composable gom nhóm `GlassEffectContainer` theo chuẩn Liquid Glass iOS 26, tối ưu hóa render pass và quản lý clipping/spacing cho các phần tử kính.
- **Độ tương phản Khả dụng (WCAG AA Compliance):** Cải thiện dải màu `FinluxTextSecondary` và `onSurfaceVariant` trên theme sáng (từ `#768197` lên `#475569`), đảm bảo tỷ lệ tương phản tối thiểu 4.5:1 trên nền kính mờ.
- **Tối ưu hóa Khúc xạ Vật liệu Kính:** Nâng cấp dải màu highlight và bóng khúc xạ trong `WaterGlassCard` & `LiquidGlassSurface` giúp chiều sâu không gian sắc nét hơn.
- **Bump Version:** Nâng `versionName` lên `1.6.1` và `versionCode` `70`.

### Kết quả & Danh sách file đã chỉnh sửa
| File | Thay đổi |
|---|---|
| `FinluxTheme.kt` | ✅ Tăng độ tương phản `FinluxTextSecondary` & `LightColors.onSurfaceVariant` lên `#475569` |
| `LiquidGlass.kt` | ✅ Thêm `GlassEffectContainer` composable theo pattern iOS 26 Liquid Glass |
| `WaterGlass.kt` | ✅ Chuẩn hóa dải phản quang và layer render cache |
| `app/build.gradle.kts` | ✅ Bump `versionCode 70`, `versionName 1.6.1` |
| `HANDOVER_LOG.md` | ✅ Cập nhật log PRE/POST-EXECUTION |
| `CHANGELOG.md` | ✅ Thêm mục phiên bản release v1.6.1 |

### Kết quả Kiểm thử
- **Unit Tests:** `./gradlew testDebugUnitTest` ➡️ **BUILD SUCCESSFUL** (100% PASS).

### Trạng thái
`[DONE]`

---

## [DONE] Task v1.6.0: Security Hardening & Authorization Fixes

**Ngày:** 2026-08-14

### Mục tiêu
- **Firestore Security Rules Isolation:** Khắc phục triệt để lỗ hổng IDOR/Multi-tenant leak bằng cách cấu hình rule `request.auth != null && request.auth.uid == uid` cho toàn bộ tài liệu gốc và subcollection con (`wallets`, `categories`, `transactions`, `budgets`, `reminders`, `goals`, `notifications`). Đảm bảo user toàn quyền quản lý dữ liệu của chính mình và cấm tuyệt đối truy cập chéo sang user khác.
- **Tắt ADB Backup:** Cập nhật `android:allowBackup="false"` trong `AndroidManifest.xml` nhằm chống trích xuất dữ liệu cache, session offline và DataStore qua kết nối ADB.
- **Network Security Config:** Tạo file `network_security_config.xml` chặn toàn bộ cleartext HTTP traffic, ép buộc giao tiếp HTTPS/TLS an toàn.
- **Bump Version:** Nâng `versionName` lên `1.6.0` và `versionCode` `69`.

### Kết quả & Danh sách file đã chỉnh sửa
| File | Thay đổi |
|---|---|
| `firestore.rules` | ✅ Áp dụng rule `request.auth.uid == uid` cho user root doc & all subcollections |
| `app/src/main/AndroidManifest.xml` | ✅ Đặt `allowBackup="false"` & khai báo `networkSecurityConfig` |
| `app/src/main/res/xml/network_security_config.xml` | ✅ Chặn cleartext HTTP traffic toàn diện |
| `app/build.gradle.kts` | ✅ Bump `versionCode 69`, `versionName 1.6.0` |
| `HANDOVER_LOG.md` | ✅ Cập nhật log PRE/POST-EXECUTION |
| `CHANGELOG.md` | ✅ Thêm mục phiên bản release v1.6.0 |

### Kết quả Kiểm thử
- **Unit Tests:** `./gradlew testDebugUnitTest` ➡️ **BUILD SUCCESSFUL** (100% PASS).

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

