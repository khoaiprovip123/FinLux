# KẾ HOẠCH TRIỂN KHAI PHASE 3: SÁP NHẬP CÁC MÀN HÌNH TRÙNG LẶP
## (COMPONENT-DRIVEN ADAPTIVE ARCHITECTURE MASTER PLAN)

---

## 1. Bối Cảnh & Đặt Vấn Đề (Architecture Assessment)

Hiện tại, mã nguồn tầng Presentation của FinLux đang bị **phân mảnh mã nguồn nghiêm trọng (Code Defragmentation)** do sao chép nguyên màn hình để phục vụ 3 phong cách giao diện (`CLASSIC_LIQUID`, `MODERN_LUXURY`, `PRISM`).

Cụ thể, có **5 cụm màn hình chính** đang bị nhân bản thành 3 file riêng biệt (tổng cộng **15 màn hình** với hơn **650 KB mã nguồn**), vi phạm trực tiếp:
- **Điều II.13 (`AGENTS.md`):** Quy tắc Anti-Code Defragmentation & Single Source of Truth (SSoT).
- **Điều II.14 (`AGENTS.md`):** Quy tắc Strict DRY & Chống sao chép nguyên màn hình (CẤM nhân bản `Classic*Screen`, `Modern*Screen`, `Prism*Screen`).
- **Điều II.15 (`AGENTS.md`):** Dead Code & Zombie Logic Elimination.

Mục tiêu của Phase 3 là: Chuyển đổi toàn diện sang **Component-driven Adaptive Architecture** — **Chỉ duy trì đúng 1 Screen duy nhất** cho mỗi tính năng quản lý ViewModel/State, các phần tử hiển thị tự động thích ứng bề mặt qua Design Tokens mà không cần nhân bản file màn hình.

---

## 2. Danh Sách 5 Cụm Màn Hình Bị Duplicate & Tỷ Lệ Trùng Lặp

| STT | Cụm Màn Hình | 3 File Nhân Bản Hiện Tại | File Router Bọc Ngoài | Dung Lượng | Tỷ Lệ Duplicate Logic |
|---|---|---|---|---|---|
| **1** | **Wallets** | `ClassicWalletsScreen.kt`<br>`ModernWalletsScreen.kt`<br>`PrismWalletsScreen.kt` | `WalletsScreen.kt` | ~113 KB | **92%** (Classic vs Modern trùng 96%; Prism dùng chung 100% Dialogs & UseCases) |
| **2** | **Budget** | `ClassicBudgetScreen.kt`<br>`ModernBudgetScreen.kt`<br>`PrismBudgetScreen.kt` | `BudgetScreen.kt` | ~87 KB | **88%** (Classic vs Modern trùng 90%; cấu trúc CRUD & Period Switcher giống nhau) |
| **3** | **Transactions** | `ClassicTransactionsScreen.kt`<br>`ModernTransactionsScreen.kt`<br>`PrismTransactionsScreen.kt` | `TransactionsScreen.kt` | ~92 KB | **85%** (Filter chips, transaction list item, swipe to delete, edit sheet) |
| **4** | **Reports** | `ClassicReportsScreen.kt`<br>`ModernReportsScreen.kt`<br>`PrismReportsScreen.kt` | `ReportsScreen.kt` | ~236 KB | **80%** (Date range selector, category breakdown, cash flow charts, stat cards) |
| **5** | **Home** | `ClassicHomeScreen.kt`<br>`ModernHomeScreen.kt`<br>`PrismHomeScreen.kt` | `HomeScreen.kt` | ~191 KB | **82%** (Greeting bar, quick action pills, mini wallet carousel, recent transactions) |
| **TỔNG** | **5 Cụm Màn Hình** | **15 Files trùng lặp** | **5 Routers mỏng** | **~720 KB** | **Xóa bỏ hoàn toàn 15 file cũ, tiết kiệm >400 KB code** |

---

## 3. Kiến Trúc Đích: Component-Driven Adaptive Architecture

```
[UI Style Preference: LocalAppUiStyle / LocalFinluxTokens]
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│               Unified Screen (1 File duy nhất)          │
│               Ví dụ: WalletsScreen.kt                    │
│  - ViewModel state collection (uiState)                 │
│  - Dialogs (Delete, Add, Edit, Archive)                 │
│  - BottomSheet controllers                              │
│  - Scaffold & Scroll state                              │
└─────────────────────────┬───────────────────────────────┘
                          │
         ┌────────────────┴────────────────┐
         ▼                                 ▼
┌─────────────────────────┐       ┌─────────────────────────┐
│     AdaptiveCard        │       │    AdaptiveTopBar       │
│ - Classic: LiquidGlass  │       │ - Classic: ClassicBar   │
│ - Modern: ModernGlass   │       │ - Modern: ModernBar     │
│ - Prism: BentoSurface   │       │ - Prism: PrismHeader    │
└─────────────────────────┘       └─────────────────────────┘
```

### Cơ chế Hoạt Động Cốt Lõi:
1. **Single ViewModel Subscription:** Chỉ 1 Composable Screen duy nhất collect `viewModel.uiState.collectAsStateWithLifecycle()`. Triệt tiêu hoàn toàn race condition và logic rẽ nhánh ở Router.
2. **Adaptive Components Dùng Chung:**
   - `FinluxAdaptiveCard`: Tự động switch renderer bề mặt kính dựa trên `LocalAppUiStyle.current`:
     * `CLASSIC_LIQUID`: Đổ bóng sâu, bo góc chuẩn, viền phản quang classic.
     * `MODERN_LUXURY`: Đổ bóng mờ mịn, viền gradient siêu mỏng hiện đại.
     * `PRISM`: Bề mặt Bento phân tầng, glass frosted với highlight góc.
   - `FinluxAdaptiveTopBar`: Tự đổi typography và nút hành động tương ứng.
   - `FinluxMainBottomBar`: Sáp nhập `ClassicMainBottomBar` và `ModernMainBottomBar` thành 1 thanh điều hướng thích ứng duy nhất.

---

## 4. Lộ Trình Triển Khai Cuốn Chiếu (Rollout Roadmap)

Để bảo đảm an toàn tuyệt đối cho **521/521 unit tests PASS 100%** và ứng dụng hoạt động ổn định trên máy thật, Phase 3 được chia làm **6 Batch cuốn chiếu**:

### 🎯 Batch 3.0: Xây Dựng Bộ Adaptive Components Cơ Sở (Design System Core)
- **Scope:**
  1. Hợp nhất `ModernLiquidGlass.kt` và `LiquidGlass.kt` thành bộ `FinluxAdaptiveCard`, `FinluxAdaptiveTopBar`.
  2. Sáp nhập `ClassicMainBottomBar.kt` và `ModernMainBottomBar.kt` thành `FinluxBottomBar.kt` tự thích ứng style.
- **Verification:** Unit test gate + Build APK.

### 🎯 Batch 3.1: Sáp Nhập Cụm 1 — `WalletsScreen`
- **Scope:**
  1. Tái cấu trúc `WalletsScreen.kt` thành Screen hoàn chỉnh (gom toàn bộ State, Dialogs, BottomSheet từ `ClassicWalletsScreen`, `ModernWalletsScreen`, `PrismWalletsScreen`).
  2. Các thẻ ví hiển thị qua `FinluxAdaptiveCard`.
  3. **XÓA BỎ HOÀN TOÀN** 3 file: `ClassicWalletsScreen.kt`, `ModernWalletsScreen.kt`, `PrismWalletsScreen.kt`.
- **Verification:** 521/521 tests PASS + Test giao dịch/ví trên máy thật.

### 🎯 Batch 3.2: Sáp Nhập Cụm 2 — `BudgetScreen`
- **Scope:**
  1. Tái cấu trúc `BudgetScreen.kt` gom 100% logic từ 3 file con.
  2. Thẻ ngân sách, thanh tiến độ và bộ lọc chu kỳ tự thích ứng bề mặt.
  3. **XÓA BỎ HOÀN TOÀN** 3 file: `ClassicBudgetScreen.kt`, `ModernBudgetScreen.kt`, `PrismBudgetScreen.kt`.
- **Verification:** 521/521 tests PASS.

### 🎯 Batch 3.3: Sáp Nhập Cụm 3 — `TransactionsScreen`
- **Scope:**
  1. Tái cấu trúc `TransactionsScreen.kt` gom bộ lọc, swipe actions, nhóm ngày.
  2. **XÓA BỎ HOÀN TOÀN** 3 file: `ClassicTransactionsScreen.kt`, `ModernTransactionsScreen.kt`, `PrismTransactionsScreen.kt`.
- **Verification:** 521/521 tests PASS.

### 🎯 Batch 3.4: Sáp Nhập Cụm 4 — `ReportsScreen`
- **Scope:**
  1. Tái cấu trúc `ReportsScreen.kt` gom biểu đồ dòng tiền, phân tích danh mục, daily statements.
  2. **XÓA BỎ HOÀN TOÀN** 3 file: `ClassicReportsScreen.kt`, `ModernReportsScreen.kt`, `PrismReportsScreen.kt`.
- **Verification:** 521/521 tests PASS.

### 🎯 Batch 3.5: Sáp Nhập Cụm 5 — `HomeScreen` (Flagship Master Screen)
- **Scope:**
  1. Tái cấu trúc `HomeScreen.kt` gom Bento Grid (Prism) và Classic/Modern Layout vào 1 cấu trúc adaptive duy nhất.
  2. **XÓA BỎ HOÀN TOÀN** 3 file: `ClassicHomeScreen.kt`, `ModernHomeScreen.kt`, `PrismHomeScreen.kt`.
- **Verification:** 521/521 tests PASS + Test full flow app.

---

## 5. Tiêu Chuẩn Nghiệm Thu & An Toàn (Definition of Done)
- [ ] 100% 521/521 Unit tests PASS ở mọi batch.
- [ ] Xóa sạch 15 file màn hình rác không còn tồn tại trong repo (Zero Zombie Code).
- [ ] Chuyển đổi giữa 3 style giao diện (`Classic`, `Modern`, `Prism`) trong Cài đặt diễn ra mượt mà, tức thì không giật lag.
- [ ] Dung lượng mã nguồn tầng Presentation giảm ít nhất 350 KB - 400 KB.
- [ ] Cập nhật `CHANGELOG.md`, `HANDOVER_LOG.md` và `docs/` sau mỗi batch.
