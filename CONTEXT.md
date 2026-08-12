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
 │   ├─ designsystem/        -- LiquidGlassSurface, GlassCard, GlassTopBar, theme (Light/Dark tokens)
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

## File tài liệu liên quan
| File | Nội dung |
|------|----------|
| `PROJECT_PROFILE.md` | Phạm vi, scope, team, timeline |
| `BA_SPEC.md` | Use case, business rule, NFR |
| `UI_SPEC.md` | Đặc tả từng màn hình + design system Liquid Glass |
| `DATA_SPEC.md` | Firestore schema, Security Rules, Cloud Functions |
| `PLAN.md` | Kế hoạch sprint |
| `AGENTS.md` | Hướng dẫn cho AI coding agent |
| `CHANGELOG.md` | Lịch sử thay đổi |
