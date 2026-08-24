# FIX PLAN — FinLux Danh Sách Cần Sửa

> Tạo ngày 2026-08-22, cập nhật sau khi merge **v1.9.0** (PR #8 — Module Quản lý nợ, khóa sinh trắc học, xuất Excel/PDF). Dựa trên đánh giá audit toàn diện dự án (docs, code, Firestore Rules, Cloud Functions, test, git history).
> Trạng thái dự án tại thời điểm cập nhật: **v1.9.0 (versionCode 108)**, ~145 file Kotlin main / 24 file test.

---

## Nguyên tắc thực hiện

- Làm theo đúng thứ tự ưu tiên: **P0 → P1 → P2**. P0 là điều kiện chặn (blocker) — không thêm tính năng mới trước khi xong P0.
- Sau **mỗi file/cụm sửa** chạy `gradlew testDebugUnitTest` — không dồn kiểm tra到最后.
- Chỉ ghi CHANGELOG + bump version **sau khi** test 100% PASS và build APK thành công (đúng SOP trong `AGENTS.md`).
- Mỗi mục hoàn thành → cập nhật trạng thái `[DONE]` kèm ngày trong file này.

---

## 🔴 P0 — Vi phạm quy chuẩn nội bộ AGENTS.md (blocker)

### P0.1 — Gom 373 màu hard-code về designsystem token `[TODO]`

> ⚠️ Cập nhật v1.9.0: số chỗ tăng từ 338 → **373** (module Debt mới thêm ~19 chỗ, PrismHome/AddTransactionSheet phình thêm).

**Vi phạm:** Nguyên tắc 5 trong `AGENTS.md` — theme màu tập trung ở `core/designsystem`, không hard-code màu (`Color(0xFF...)`) trong từng màn hình.

**Phạm vi (theo số chỗ nặng → nhẹ):**

| # | File | Số chỗ |
|---|------|--------|
| 1 | `presentation/home/prism/PrismHomeScreen.kt` | 61 |
| 2 | `presentation/reports/prism/PrismReportsScreen.kt` | 51 |
| 3 | `presentation/transaction/AddTransactionSheet.kt` | 46 |
| 4 | `presentation/auth/AuthScreens.kt` | 35 |
| 5 | `presentation/components/QuickAddSheet.kt` | 34 |
| 6 | `presentation/transaction/prism/PrismTransactionsScreen.kt` | 33 |
| 7 | `presentation/settings/SettingsScreen.kt` | 24 |
| 8 | `presentation/debt/*` (6 file, mới trong v1.9.0) | ~19 |
| 9 | `presentation/transaction/TransactionDetailSheet.kt` | 14 |
| 10 | `presentation/settings/prism/PrismSettingsScreen.kt` | 13 |
| 11 | `presentation/updater/AppUpdateDialog.kt` + các file còn lại | ~43 |

**Cách làm:**
1. Rà soát và bổ sung đủ token màu (brand / surface / status / chart palette) vào theme ở `core/designsystem` (bao gồm variant Prism).
2. Thay thế từng file theo thứ tự bảng trên; sau mỗi file chạy `gradlew testDebugUnitTest`.
3. Verify bằng lệnh đếm lại: `grep -rc "Color(0xFF" app/src/main/java/com/finlux/app/presentation/` — mục tiêu chỉ còn lại những màu thực sự unique (nếu còn) hoặc bằng 0.

**Ước lượng:** 1–2 ngày.

---

### P0.2 — Tách 9 file UI >700 dòng `[TODO]`

> ⚠️ Cập nhật v1.9.0: `PrismHomeScreen` 1.704 → **1.732** dòng, `AddTransactionSheet` 1.198 → **1.214**. Ghi nhận tích cực: module Debt mới tuân thủ chuẩn tốt (file lớn nhất `DebtDashboardScreen` 476 dòng) — chứng minh chuẩn <500 dòng khả thi.

**Lý do:** File quá lớn khó review, khó test, compile chậm, khó tái sử dụng composable. Mục tiêu: **<400 dòng/file**, tách composable theo section.

| # | File | Dòng | Gợi ý tách |
|---|------|------|------------|
| 1 | `PrismHomeScreen.kt` | 1.732 | GreetingHeader, KpiCards, RecentTransactions, BudgetOverview |
| 2 | `PrismReportsScreen.kt` | 1.264 | FilterBar, ChartSection, CategoryBreakdown, ComparisonSection |
| 3 | `AddTransactionSheet.kt` | 1.214 | AmountInputField, CategoryPickerSection, WalletPickerSection, QuickAmountChips |
| 4 | `SettingsScreen.kt` | 1.066 | theo nhóm menu (Tài khoản / Tài chính / Ứng dụng / Hỗ trợ) |
| 5 | `AuthScreens.kt` | 970 | tách LoginScreen / RegisterScreen / shared components |
| 6 | `PrismTransactionsScreen.kt` | 821 | SearchBar, FilterChips, TransactionList, GroupHeader |
| 7 | `PrismSettingsScreen.kt` | 758 | theo nhóm menu |
| 8 | `ModernWalletsScreen.kt` | 744 | WalletCard, TransferSection |
| 9 | `ClassicWalletsScreen.kt` | 734 | WalletCard, TransferSection |

**Nguyên tắc tách:** chỉ di chuyển code + thay tham chiếu màu bằng token (kết hợp lợi ích P0.1), **không đổi behavior**. Composable private trong file riêng cùng package nếu chỉ dùng 1 chỗ.

**Ước lượng:** 1–2 ngày (làm sau/kết hợp P0.1 trong cùng file).

---

## 🟡 P1 — Chất lượng & nhất quán

### P1.1 — Default UI style lệch nơi đầu tư phát triển `[CẦN QUYẾT ĐỊNH]`

**Hiện trạng:** `AppUiStyle` có 3 giá trị `{CLASSIC_LIQUID, MODERN_LUXURY, PRISM}` (`domain/model/FinanceModels.kt:10`). Default đang là **CLASSIC_LIQUID**:
- `presentation/RootViewModel.kt:30` — `initialValue = AppUiStyle.CLASSIC_LIQUID`
- `data/local/datastore/DataStoreThemePreferenceRepository.kt:48` — fallback `?: AppUiStyle.CLASSIC_LIQUID`

Trong khi đó toàn bộ tính năng + polish gần đây (v1.8.5–1.8.8) đều dồn vào màn hình **PRISM**. Người dùng mới cài app sẽ thấy giao diện CLASSIC cũ.

**Hai phương án:**
- **(A) Khuyến nghị:** Đổi default sang `PRISM`, coi PRISM là chuẩn — sửa 2 dòng + test. (~30 phút)
- **(B)** Giữ nguyên default nhưng cam kết maintain parity thật sự cho cả 3 style (chi phí dài hạn cao hơn nhiều).

### P1.2 — UI test smoke cho luồng trọng yếu `[TODO]`

**Hiện trạng:** 24 file test (v1.9.0 thêm 8 file test chất lượng: Debt, PayoffStrategy, Goal, HomeViewModel, XlsxReportWriter, AppLockManager) — nhưng vẫn chỉ phủ UseCase / ViewModel / Data layer. Layer UI = 0 test, trong khi CHANGELOG cho thấy phần lớn bug gần đây là lỗi UI (gesture lộ splash screen, chữ dọc trong card, header bị status bar đè, crash null `MainBottomBar`...).

**Cần tối thiểu (Compose UI test):**
1. Thêm giao dịch Thu/Chi (mở QuickAdd → điền → lưu → xuất hiện trong danh sách)
2. Chuyển tiền giữa 2 ví (số dư 2 ví đổi đúng chiều)
3. Đăng nhập / Đăng ký form validation + navigation
4. Vuốt chuyển tab không lộ nền splash (regression cho bug v1.8.8)

**Ước lượng:** 2–3 ngày.

### P1.3 — Chuẩn bị tách string resource cho i18n `[TODO]`

**Hiện trạng:** `res/values/strings.xml` chỉ có **1 string** — toàn bộ text UI hardcode tiếng Việt trong Compose (vd: `PrismHomeScreen.kt` — "Xin chào 👋", "Tổng tài sản", "Xem tất cả"...).

**Đánh giá:** Chấp nhận được cho V1 (chỉ tiếng Việt theo NFR). Nhưng BA_SPEC còn để ngỏ `[Cần xác nhận]` thêm English — nếu có khả năng đó, tách string resource càng sớm càng rẻ (làm kết hợp khi chạm vào file ở P0.1/P0.2 để đỡ tốn công riêng).

### P1.4 — Đồng bộ đầy đủ tính năng form Chuyển tiền ví trên giao diện Prism (`PrismWalletsScreen.kt`) `[DONE 2026-08-24]`

**Hiện trạng & Kết quả:** Đã bổ sung dải chip chọn Ví nguồn (`LazyRow`), dải chip chọn Ví nhận, nút Swap đảo chiều `Icons.Default.SwapHoriz` ⇄, component `FinluxAmountInputCard` và validation kiểm tra số dư ví nguồn không đủ, đồng bộ hoàn toàn với trải nghiệm giao diện Cổ điển. Xem chi tiết tại [docs/BACKLOG.md](file:///d:/Sources/FinLux/docs/BACKLOG.md).

### P1.5 — Chuẩn hóa hiển thị giao dịch Chuyển tiền giữa các ví (Double-entry Transfer) `[DONE 2026-08-24]`

**Hiện trạng & Kết quả:** Sửa lỗi giao dịch kép `TRANSFER_OUT` và `TRANSFER_IN` bị nhận diện nhầm thành "Chi tiêu" với icon nhãn màu đỏ `-5.250.000 đ`. Đã chuẩn hóa hiển thị icon `SwapHoriz`, màu xanh `FinluxColors.TransferBlue`, tiêu đề rõ ràng `"Chuyển tiền đến [Ví B]"` / `"Nhận tiền từ [Ví A]"`, và định tuyến ví `[Ví A] ➔ [Ví B]`. Xem chi tiết tại [docs/BACKLOG.md](file:///d:/Sources/FinLux/docs/BACKLOG.md).

### P1.6 — Chặn tạo giao dịch chi tiêu khi nguồn tiền ví thanh toán <= 0 hoặc không đủ `[TODO]`

**Mô tả:** Thêm cảnh báo màu đỏ và vô hiệu hóa nút Lưu trong `AddTransactionSheet.kt` & `QuickAddSheet.kt` khi số dư ví thanh toán `<= 0` hoặc `< số tiền chi tiêu` (áp dụng với ví không phải Thẻ tín dụng `WalletType.CARD`). Bổ sung validation tại tầng Domain `AddTransactionUseCase`. Xem chi tiết tại [docs/BACKLOG.md](file:///d:/Sources/FinLux/docs/BACKLOG.md).

### P1.7 — Nâng cấp Quản lý Nợ: Lịch sử thanh toán & Nhắc nợ đến hạn `[TODO]`

**Mô tả:** Triển khai BottomSheet xem lịch sử thanh toán nợ (`DebtPaymentHistorySheet`) có bộ lọc theo từng khoản nợ và thống kê tổng hợp; bổ sung toggle bật/tắt nhắc nợ kèm chọn số ngày nhắc trước trong `AddEditDebtSheet` và tự động phát thông báo khi đến hạn. Xem chi tiết tại [docs/BACKLOG.md](file:///d:/Sources/FinLux/docs/BACKLOG.md).

### P1.8 — Trợ lý Phân bổ Dòng tiền Thoát nợ Tự động (Debt Cashflow Advisor) `[DONE 2026-08-24]`

**Hiện trạng & Kết quả:** Đã triển khai `AnalyzeDebtCashflowUseCase` tự động phân tích lịch sử thu chi trượt 3 tháng, bóc tách chi phí thiết yếu (`isEssential`), tính Dòng tiền tự do (FCF), tính Lãi suất trung bình có trọng số (Weighted APR), và tạo thẻ kính `CashflowAdvisorCard` với 3 kịch bản thông minh (Thư thái 30% / Cân bằng 60% / Thần tốc 85%) tích hợp tương tác 1-Touch Apply lên `StrategySelectorCard`. Đã kiểm thử 100% PASS và nạp thành công lên máy.

---

## 🟢 P2 — Dọn dẹp & chốt quyết định sản phẩm

### P2.1 — Giải quyết các `[Cần xác nhận]` còn treo `[CẦN XÁC NHẬN TỪ PO]`

| Mục | Vị trí | Ghi chú |
|-----|--------|---------|
| ~~Biometric lock khi mở app~~ | `core/security/AppLockManager.kt` | ✅ **ĐÃ GIẢI QUYẾT bởi v1.9.0** — có timeout tùy chọn + test riêng |
| Namespace / applicationId chính thức | `app/build.gradle.kts` (TODO trong build config) | `com.finlux.app` vẫn là provisional |
| Tên app chính thức | PROJECT_PROFILE.md | placeholder từ đầu dự án |
| Thêm English (i18n) | BA_SPEC mục 5 | quyết định trước khi làm P1.3 |

### P2.2 — Đóng sprint đúng SOP `[TODO]`

Sau khi hoàn tất P0 + P1:
1. Chạy `gradlew testDebugUnitTest` — 100% PASS.
2. Build APK thành công.
3. Ghi CHANGELOG + bump `versionCode`/`versionName` (Semantic Versioning — đợt refactor này đáng giá trị minor kế tiếp: **1.10.0**, vì 1.9.0 đã phát hành).
4. Commit: `bump(release): v1.10.0 - Refactor UI token mau, tach file lon, them UI test smoke`.

---

## Tóm tắt khối lượng & thứ tự

| Mục | Ưu tiên | Ước lượng | Phụ thuộc |
|-----|---------|-----------|-----------|
| P0.1 Gom màu → token | 🔴 | 1–2 ngày | — |
| P0.2 Tách file lớn | 🔴 | 1–2 ngày | kết hợp P0.1 cùng file |
| P1.1 Default UI style | 🟡 | 30 phút | quyết định PO |
| P1.2 UI test smoke | 🟡 | 2–3 ngày | nên làm sau P0.2 (file đã gọn) |
| P1.3 i18n string | 🟡 | +0.5–1 ngày | kết hợp P0.1/P0.2 |
| P2.1 Chốt `[Cần xác nhận]` | 🟢 | — | quyết định PO |
| P2.2 Đóng sprint | 🟢 | — | sau tất cả các mục trên |

**Tổng: ~5–7 ngày làm việc** cho P0 + P1.

---

*Lưu ý: Danh sách này là kế hoạch kỹ thuật — không thay thế `PLAN.md` (sprint plan) và `BACKLOG.md` (tính năng). Khi hoàn thành từng mục, cập nhật trạng thái `[DONE yyyy-MM-dd]` tại đây và ghi log vào `HANDOVER_LOG.md` theo SOP.*
