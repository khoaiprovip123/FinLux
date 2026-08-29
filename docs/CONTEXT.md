# CONTEXT — Finlux

## Tổng quan
Ứng dụng Android quản lý thu chi cá nhân, viết bằng Kotlin + Jetpack Compose, backend Firebase,
giao diện theo phong cách **Liquid Glass** (giống iOS), hỗ trợ Sáng/Tối. Xem chi tiết nghiệp vụ
trong `BA_SPEC.md`, giao diện trong `UI_SPEC.md`, dữ liệu trong `DATA_SPEC.md`.

## Tech Stack
- **Ngôn ngữ:** Kotlin
- **UI:** Jetpack Compose + Material 3 (custom Liquid Glass theme layer)
- **Kiến trúc:** Clean Architecture 3 lớp (Presentation / Domain / Data) + MVVM
- **DI:** Hilt
- **Async:** Kotlin Coroutines + Flow
- **Navigation:** Navigation Compose
- **Backend:** Firebase Auth, Firestore, Storage, Cloud Messaging, Cloud Functions (Node.js/TS)
- **Chart:** Vico (Compose-native chart library) hoặc tương đương
- **Export:** Apache POI (Excel), `PdfDocument` (PDF)
- **Local pref:** DataStore (theme, cấu hình nhắc nhở local)
- **Testing:** JUnit5 + Turbine (Flow testing) + Compose UI Test

## Cấu trúc thư mục đề xuất
```
app/
 ├─ core/
 │   ├─ designsystem/        -- LiquidGlassSurface, theme tokens, FinancialInstitutions/VietQrBankCatalog
 │   ├─ navigation/
 │   └─ common/              -- Result wrapper, extensions, utils
 ├─ data/
 │   ├─ remote/firebase/     -- FirestoreDataSource, AuthDataSource, StorageDataSource
 │   ├─ local/datastore/
 │   └─ repository/          -- implement domain repository interfaces
 ├─ domain/
 │   ├─ model/                -- Transaction, Wallet, Category, Budget, Reminder
 │   ├─ repository/           -- interfaces
 │   └─ usecase/              -- AddTransactionUseCase, GetBudgetStatusUseCase, ...
 ├─ presentation/
 │   ├─ auth/                 -- Login, Register, ForgotPassword
 │   ├─ home/
 │   ├─ transaction/
 │   ├─ category/
 │   ├─ wallet/
 │   ├─ budget/
 │   ├─ report/
 │   ├─ notification/
 │   └─ settings/
 └─ FinluxApp.kt / MainActivity.kt

functions/                    -- Cloud Functions (TypeScript), xem DATA_SPEC.md mục 4
```

## Nguyên tắc code
- Mỗi màn hình = 1 package riêng dưới `presentation/`, gồm `Screen.kt` (Composable), `ViewModel.kt`, `UiState.kt`
- Không gọi trực tiếp Firestore từ ViewModel — luôn qua Repository (domain interface) để dễ test/mock
- Cập nhật số dư ví BẮT BUỘC qua `FirebaseFirestore.runTransaction {}` (xem BR-14, DATA_SPEC mục 1)
- Toàn bộ theme màu/kính (Liquid Glass) định nghĩa tập trung ở `core/designsystem`, không hard-code màu trong từng màn hình
- Logo ngân hàng/ví dùng catalog tập trung `FinancialInstitutions.kt` + snapshot VietQR trong
  `VietQrBankCatalog.kt`; tài nguyên nằm cục bộ ở `drawable-nodpi` và có thể làm mới bằng
  `tools/sync-financial-institution-icons.ps1`.
- Logo có tài nguyên thương hiệu dùng semantic token `brandLogoSurface/brandLogoBorder` để luôn có
  nền nhận diện trắng rõ ràng; chỉ monogram/fallback WalletType mới dùng nền accent gradient.
- Home Prism dùng Material `Surface` với `LocalFinluxTokens` ở KPI và thẻ phân tích; không dùng
  blur/Liquid Glass/glow. Số tiền dùng baseline cố định và co cỡ chữ theo độ dài để không tràn.
- Kỳ tài chính dùng `FinancialPeriodResolver` ở Home/History/Income/Expense/Reports; feature không tự
  suy diễn mốc tháng khi Salary Cycle đang bật.
- Các phép diễn giải dùng chung (`collapseInternalTransferPairs`, `assetWallets`,
  `netGoalContribution`) nằm trong domain model để Home, History và Reports không lệch công thức.
- Home/Lịch sử Prism dùng `core/designsystem/component/FinluxTransactionGroup` cho danh sách kiểu
  nhóm menu Hồ sơ; feature chỉ truyền dữ liệu và callback, không tự dựng lại row/card.
- Header Home Prism là Row 52dp không container, gồm avatar, lời chào/tên và nút thông báo soft surface.
- Tổng quan Home Prism là một `HorizontalPager` bốn trang Số dư/Thu nhập/Chi tiêu/Dòng tiền, không
  còn hero số dư tách rời, tiêu đề/badge/hàng tab. Hero cao 196dp, vuốt trực tiếp hoặc tự tiến sau
  10 giây; mọi thao tác tay đều đặt lại thời gian chờ và chỉ báo trang nằm trong đáy thẻ. Mỗi trang
  dùng gradient theo semantic của chỉ số và Canvas vector riêng đặt ở vùng phải để nhận diện nhanh,
  không chồng lên số tiền và không dùng Liquid Glass.
- Vuốt bốn tab chính được nhận ở root, phần nội dung bám theo ngón tay và có edge resistance; bottom
  navigation không dịch chuyển cùng trang. Component con đã consume gesture vẫn được ưu tiên.

## File tài liệu liên quan
| File | Nội dung |
|------|----------|
| `docs/PROJECT_PROFILE.md` | Phạm vi, scope, team, timeline |
| `docs/BA_SPEC.md` | Use case, business rule, NFR |
| `docs/UI_SPEC.md` | Đặc tả từng màn hình + design system Liquid Glass |
| `docs/DATA_SPEC.md` | Firestore schema, Security Rules, Cloud Functions |
| `docs/PLAN.md` | Kế hoạch sprint |
| `AGENTS.md` | Hướng dẫn cho AI coding agent |
| `CHANGELOG.md` | Lịch sử thay đổi |
| `HANDOVER_LOG.md` | Nhật ký bàn giao và quản lý task |
