# KẾ HOẠCH KỸ THUẬT CHI TIẾT PHASE 2: CHUẨN HÓA DESIGN SYSTEM TOKENS & TRIỆT TIÊU MÃ MÀU/KÍCH THƯỚC THÔ
*(FINLUX PHASE 2: DESIGN SYSTEM TOKEN AUDIT, STANDARDIZATION & REFACTORING PLAN)*

- **Dự án:** FinLux — Quản lý Tài chính Cá nhân Thông minh (Android / Jetpack Compose / Liquid Glass)
- **Mục tiêu:** Triệt tiêu hoàn toàn 918 mã màu hex thô, 366 màu tĩnh (`Color.White`/`Color.Black`), cùng các kích thước font/spacing tùy tiện trong toàn bộ tầng `presentation/`, đưa 100% giao diện về `LocalFinluxTokens.current`, `FinluxColors`, `FinluxSpacing`, và `FinluxTypography`.
- **Nguyên tắc thực thi:** Áp dụng nghiêm ngặt **PLANNING-FIRST GATE** theo Điều II trong `AGENTS.md`. Tuyệt đối không sửa code khi chưa được Tech Lead phê duyệt kế hoạch.

---

## 🧭 TRỤ CỘT 1: BẢNG QUY CHUẨN ÁNH XẠ TOKEN (TOKEN MAPPING MATRIX)

### 1.1. Bảng Quy Đổi Mã Màu Hex Sang Token Chuẩn

| Mã Màu Hex Thô | Tần Suất | Ngữ Cảnh Sử Dụng Cũ | Token Chuẩn Thay Thế | Bản Chất Thích Ứng (Light/Dark) |
|---|:---:|---|---|---|
| `0xFF10B981`<br>`0xFF059669`<br>`0xFF20B982` | 170 | Thu nhập, Tiền vào, Thành công, Tích cực | `FinluxColors.IncomeGreen`<br>hoặc `tokens.primary` (nếu là accent) | Màu ngọc lục bảo chuẩn (#20B486), giữ độ tương phản cao trên cả nền sáng và nền tối. |
| `0xFFEF4444`<br>`0xFFDC2626`<br>`0xFFE11D48`<br>`0xFFF43F5E` | 108 | Chi tiêu, Tiền ra, Lỗ, Cảnh báo lỗi | `tokens.error`<br>(hoặc `FinluxColors.ExpenseRed`) | Đỏ hồng semantic (#EB5C6E / #F05B68), tự thích ứng an toàn qua tokens. |
| `0xFF6B7280`<br>`0xFF9CA3AF`<br>`0xFF94A3B8` | 88 | Văn bản phụ, Ghi chú phụ, Label mờ, Placeholder | `tokens.onSurfaceVariant`<br>(alias `tokens.textSecondary`) | Light: #768197, Dark: #A8B0C0. Triệt tiêu chữ tàng hình trên nền tối. |
| `0xFFE5E7EB`<br>`0xFFD1D5DB`<br>`0x1A5A6EA0` | 42 | Viền card, Divider, Đường kẻ phân cách | `tokens.border` | Light: BorderSoftLight (rgba 10%), Dark: BorderSoftDark (rgba 10%). |
| `0xFF1E1E2D`<br>`0xFF171B25`<br>`0xFFFDFEFF` | 40 | Bề mặt Card, Nền Container, Dialog, Sheet | `tokens.surface` | Light: #FFFFFF, Dark: #171B25. Tự động chuyển đổi màu nền theo theme. |
| `0xFFF3F4F6`<br>`0xFFF2F5FB`<br>`0xFF1E2430` | 24 | Chip nền, Nút phụ, Box trạng thái mềm | `tokens.surfaceSoft` | Light: #F2F5FB, Dark: #1E2430. Tạo tương phản phân tầng (Elevation). |
| `0xFFF59E0B`<br>`0xFFD97706`<br>`0xFFF2A43A` | 55 | Chờ xử lý, Cảnh báo hạn mức, Ngân sách 80% | `FinluxColors.WarningAmber` | Vàng cam ấm (#F2A43A), rõ nét cả trên nền tối lẫn nền sáng. |
| `0xFF3B82F6`<br>`0xFF2563EB`<br>`0xFF3478F6` | 34 | Nút chính, Tab được chọn, Icon hành động | `tokens.primary`<br>(hoặc `FinluxColors.PrimaryBlue`) | Light: #3A7BFF, Dark: #23C7E8 (Prism) / #47C8FF (Classic). |
| `0xFF6366F1`<br>`0xFF8B5CF6`<br>`0xFF7758F6` | 40 | Danh mục ngân sách, Tiết kiệm, Kỳ lương | `FinluxColors.PrimaryViolet`<br>hoặc `FinluxColors.BudgetViolet` | Tím nhận diện (#6F52F5 / #7052F5). |
| `0xFF0EA5E9`<br>`0xFF0284C7`<br>`0xFF47C8FF` | 20 | Chuyển khoản, Thống kê dòng tiền, Ví tín dụng | `FinluxColors.TransferBlue`<br>hoặc `FinluxColors.PrimaryCyan` | Xanh dương / Cyan hiện đại (#3985F5 / #23C7E8). |

---

### 1.2. Bảng Quy Đổi Mã Màu Tĩnh (`Color.White`, `Color.Black`)

| Mã Màu Tĩnh Cấm Kỵ | Tần Suất | Ngữ Cảnh Xuất Hiện | Token Chuẩn Thay Thế | Giải Pháp Chống Cháy / Tàng Hình Màu |
|---|:---:|---|---|---|
| `Color.White` | 193 | Tiêu đề, văn bản, icon trên Card/Surface thông thường | `tokens.onSurface`<br>(alias `tokens.textPrimary`) | Khi sang Light Mode tự chuyển thành `#172033` (đen đậm nét), không bị tàng hình trên nền trắng. |
| `Color.White` | — | Nền thẻ Card, Popup, Surface | `tokens.surface` | Khi sang Dark Mode tự chuyển thành `#171B25` (xám đen Liquid Glass), không làm chói mắt người dùng. |
| `Color.White` | — | Chữ, icon nằm TRÊN nền Hero Gradient hoặc Button Primary | `tokens.onHero`<br>(hoặc `tokens.onPrimary`) | Luôn giữ màu trắng sáng an toàn vì nền phía sau luôn là dải gradient màu tối/đậm. |
| `Color.White.copy(alpha = 0.7f..0.85f)` | 30 | Phụ đề, ngày giờ nằm TRÊN nền Hero Gradient | `tokens.onHeroMuted` | Giữ độ mờ 82% trên nền Hero, bảo toàn thẩm mỹ Liquid Glass. |
| `Color.White.copy(alpha = 0.05f..0.2f)` | 65 | Nền kính nhỏ, viền chip TRÊN nền Hero Gradient | `tokens.heroGlassSurface` | Kính mờ khúc xạ 18% trên nền Hero. |
| `Color.White.copy(alpha = 0.05f..0.15f)` | — | Nền kính trên bề mặt Card thông thường | `tokens.surfaceSoft`<br>hoặc `tokens.border` | Tự động chuyển thành độ mờ đen/trắng tương thích Dark/Light Mode. |
| `Color.Black` | 10 | Chữ trên nền sáng hoặc icon | `tokens.onSurface` | Đảm bảo tương thích động ngược lại khi chuyển Dark Mode. |

---

### 1.3. Bảng Quy Đổi Kích Thước Spacing & Typography Tùy Tiện

| Kích Thước Thô Hiện Tại | Tần Suất | Token Chuẩn Thay Thế (`tokens.spacing` / `MaterialTheme.typography`) | Quy Tắc Chuẩn Hóa |
|---|:---:|---|---|
| `6.dp`, `7.dp` | 300 | `tokens.spacing.sm` (8.dp) hoặc `tokens.spacing.xs` (4.dp) | Không dùng số lẻ ngoài hệ 4dp Grid System. |
| `10.dp`, `11.dp` | 275 | `tokens.spacing.md` (12.dp) hoặc `tokens.spacing.sm` (8.dp) | Đồng bộ khoảng cách giữa icon và nhãn text. |
| `14.dp`, `15.dp` | 240 | `tokens.spacing.base` (16.dp) hoặc `tokens.spacing.md` (12.dp) | Đệm viền trong card và khoảng cách form. |
| `18.dp`, `22.dp` | 168 | `tokens.spacing.lg` (20.dp) | Khoảng cách Section Title và Card Blocks. |
| `28.dp`, `30.dp` | 80 | `tokens.spacing.xxl` (32.dp) hoặc `tokens.spacing.xl` (24.dp) | Khoảng cách phân đoạn lớn. |
| `10.sp`, `10.5.sp`, `11.sp`, `11.5.sp` | 152 | `MaterialTheme.typography.labelSmall` (`FinluxTextStyles.MicroLabel`, 11.5sp Medium) | Nhãn phụ, badge, tag ngày nhỏ. |
| `12.sp`, `12.5.sp` | 159 | `MaterialTheme.typography.labelMedium` (12sp SemiBold) | Phụ đề danh mục, số tài khoản, tuyến ví. |
| `13.sp`, `13.5.sp` | 121 | `MaterialTheme.typography.bodySmall` (`FinluxTextStyles.Caption`, 13sp Normal) | Mô tả chi tiết giao dịch, ghi chú phụ. |
| `14.sp`, `14.5.sp` | 66 | `MaterialTheme.typography.bodyMedium` (14sp Normal) | Văn bản thân form, dropdown item. |
| `15.sp` | 47 | `MaterialTheme.typography.bodyLarge` (`FinluxTextStyles.Body`, 15sp Normal) | Tiêu đề giao dịch 3 cột chuẩn. |
| `16.sp`, `17.sp` | 40 | `MaterialTheme.typography.titleMedium` (`FinluxTextStyles.CardTitle`, 16sp SemiBold) | Tiêu đề Card, Tên ví, Tên khoản nợ. |
| `18.sp`, `20.sp` | 39 | `MaterialTheme.typography.titleLarge` (`FinluxTextStyles.SectionTitle`, 20sp Bold) | Tiêu đề phân mục lớn. |

---

## 📦 TRỤ CỘT 2: PHÂN KỲ DANH MỤC TỆP THEO BATCH (FILE-BY-FILE BATCH BREAKDOWN)

Toàn bộ 52 file trong `presentation/` được chia thành **10 Batches nhỏ (3-5 file/batch)**, sắp xếp theo mức độ phụ thuộc từ Sheet/Component dùng chung đến các màn hình lõi:

```
┌───────────────────────────────────────────────────────────────────────────────┐
│                    LỘ TRÌNH 10 BATCHES THỰC THI PHASE 2                       │
├─────────────────┬─────────────────────────────────────────────────────────────┤
│ 1. Batch 2.1    │ Core Sheets & Dialogs (5 files - 110 vị trí)                │
│ 2. Batch 2.2    │ Core Navigation & Scaffold Components (4 files - 20 vị trí) │
│ 3. Batch 2.3    │ Settings, Salary & Backup Sheets (5 files - 172 vị trí)     │
│ 4. Batch 2.4    │ Deal Management Module (6 files - 118 vị trí)               │
│ 5. Batch 2.5    │ Debt Management Module (6 files - 44 vị trí)                │
│ 6. Batch 2.6    │ Notifications, Spin & Goal Screens (5 files - 67 vị trí)    │
│ 7. Batch 2.7    │ Wallets & Transfer Screens (5 files - 66 vị trí)            │
│ 8. Batch 2.8    │ Transactions & Budget Screens (5 files - 28 vị trí)         │
│ 9. Batch 2.9    │ Reports Detail BottomSheets & Variants (4 files - 207 vị trí)│
│ 10. Batch 2.10  │ Super Heavyweight Flagship Screens (2 files - 442 vị trí)   │
└─────────────────┴─────────────────────────────────────────────────────────────┘
```

### Chi Tiết Từng Batch:

#### 🔹 Batch 2.1 — Core Sheets & Dialogs (5 files — 110 vị trí)
1. `presentation/updater/AppUpdateDialog.kt` (6 Hex, 3 Static = 9 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `tokens.primary`, `tokens.border`.
2. `presentation/components/QuickAddSheet.kt` (38 Hex, 2 Static = 40 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `FinluxColors.IncomeGreen`, `FinluxColors.ExpenseRed`, `tokens.surfaceSoft`.
3. `presentation/transaction/AddTransactionSheet.kt` (26 Hex, 2 Static = 28 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `FinluxColors.*`, `tokens.border`.
4. `presentation/wallet/WalletTransactionsBottomSheet.kt` (0 Hex, 1 Static, 28 Hex semantics = 29 vị trí) -> Tokens: `tokens.onSurface`, `tokens.surface`, `FinluxColors.IncomeGreen`.
5. `presentation/settings/deleteaccount/DeleteAccountBottomSheet.kt` (0 Hex, 4 Static = 4 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `tokens.error`.

#### 🔹 Batch 2.2 — Core Navigation & Scaffold Components (4 files — 20 vị trí)
1. `presentation/components/classic/ClassicMainBottomBar.kt` (5 Hex, 4 Static = 9 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `tokens.primary`.
2. `presentation/components/modern/ModernMainBottomBar.kt` (5 Hex, 1 Static = 6 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `tokens.primary`.
3. `presentation/transaction/prism/PrismSpendingCalendarView.kt` (2 Hex, 2 Static = 4 vị trí) -> Tokens: `FinluxColors.IncomeGreen`, `FinluxColors.ExpenseRed`, `tokens.onSurface`.
4. `presentation/receipt/ReceiptCaptureScreen.kt` (0 Hex, 1 Static = 1 vị trí) -> Tokens: `tokens.onSurface`.

#### 🔹 Batch 2.3 — Settings, Salary & Backup Sheets (5 files — 172 vị trí)
1. `presentation/settings/SettingsScreen.kt` (37 Hex, 18 Static = 55 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `tokens.surfaceSoft`, `tokens.onSurfaceVariant`, `tokens.border`.
2. `presentation/settings/prism/PrismSettingsScreen.kt` (19 Hex, 8 Static = 27 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `tokens.surfaceSoft`, `tokens.border`.
3. `presentation/settings/salary/SalaryCycleSettingsSheet.kt` (44 Hex, 8 Static = 52 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `FinluxColors.IncomeGreen`, `tokens.primary`, `tokens.border`.
4. `presentation/settings/backup/BackupRestoreSheet.kt` (0 Hex, 10 Static = 10 vị trí) -> Tokens: `FinluxColors.IncomeGreen`, `tokens.border`, `tokens.textSecondary`.
5. `presentation/reminders/RemindersScreen.kt` (24 Hex, 4 Static = 28 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `FinluxColors.WarningAmber`, `tokens.primary`.

#### 🔹 Batch 2.4 — Deal Management Module (6 files — 118 vị trí)
1. `presentation/deal/DealsScreen.kt` (23 Hex, 2 Static = 25 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `tokens.primary`, `FinluxColors.*`.
2. `presentation/deal/DealDetailBottomSheet.kt` (39 Hex, 5 Static = 44 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `FinluxColors.IncomeGreen`, `tokens.error`.
3. `presentation/deal/DealAllTransactionsBottomSheet.kt` (15 Hex, 2 Static = 17 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `tokens.border`.
4. `presentation/deal/CreateDealSheet.kt` (9 Hex, 2 Static = 11 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `tokens.primary`.
5. `presentation/deal/RecordDealInflowSheet.kt` (12 Hex, 2 Static = 14 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `FinluxColors.IncomeGreen`.
6. `presentation/deal/RecordDealOutlaySheet.kt` (8 Hex, 2 Static = 10 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `tokens.error`.

#### 🔹 Batch 2.5 — Debt Management Module (6 files — 44 vị trí)
1. `presentation/debt/DebtDashboardScreen.kt` (6 Hex, 3 Static = 9 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `tokens.error`.
2. `presentation/debt/DebtPaymentSheet.kt` (12 Hex, 1 Static = 13 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `FinluxColors.IncomeGreen`.
3. `presentation/debt/AddEditDebtSheet.kt` (0 Hex, 5 Static = 5 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `tokens.border`.
4. `presentation/debt/components/DebtPaymentHistorySheet.kt` (7 Hex, 2 Static = 9 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `FinluxColors.*`.
5. `presentation/debt/components/DebtCard.kt` (3 Hex, 0 Static = 3 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `tokens.border`.
6. `presentation/debt/components/StrategySelectorCard.kt` (2 Hex, 2 Static = 4 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `tokens.primary`.

#### 🔹 Batch 2.6 — Notifications, Saving Spin & Goals (5 files — 67 vị trí)
1. `presentation/notifications/NotificationsScreen.kt` (11 Hex, 7 Static = 18 vị trí) -> Tokens: `FinluxColors.IncomeGreen`, `tokens.primary`, `tokens.onSurface`, `tokens.surface`.
2. `presentation/savingspin/components/SavingSpinWheel.kt` (19 Hex, 3 Static = 22 vị trí) -> Tokens: `FinluxColors.*`, `tokens.surface`, `tokens.onHero`.
3. `presentation/goal/GoalsScreen.kt` (5 Hex, 2 Static = 7 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `FinluxColors.IncomeGreen`.
4. `presentation/income/IncomeScreen.kt` (4 Hex, 6 Static = 10 vị trí) -> Tokens: `FinluxColors.IncomeGreen`, `tokens.surface`, `tokens.onSurface`.
5. `presentation/expense/ExpenseScreen.kt` (3 Hex, 7 Static = 10 vị trí) -> Tokens: `tokens.error`, `tokens.surface`, `tokens.onSurface`.

#### 🔹 Batch 2.7 — Wallets & Transfer Screens (5 files — 66 vị trí)
1. `presentation/wallet/prism/PrismWalletsScreen.kt` (0 Hex, 3 Static = 3 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `tokens.primary`.
2. `presentation/wallet/classic/ClassicWalletsScreen.kt` (0 Hex, 5 Static = 5 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`.
3. `presentation/wallet/modern/ModernWalletsScreen.kt` (0 Hex, 4 Static = 4 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`.
4. `presentation/wallet/TransferMoneyScreen.kt` (0 Hex, 1 Static = 1 vị trí) -> Tokens: `tokens.onSurface`.
5. `presentation/auth/AuthScreens.kt` (44 Hex, 9 Static = 53 vị trí) -> Tokens: `tokens.background`, `tokens.surface`, `tokens.onSurface`, `tokens.primary`, `tokens.border`.

#### 🔹 Batch 2.8 — Transactions & Budget Screens (5 files — 28 vị trí)
1. `presentation/transaction/prism/PrismTransactionsScreen.kt` (0 Hex, 5 Static = 5 vị trí) -> Tokens: `tokens.onSurface`, `tokens.surface`, `tokens.primary`.
2. `presentation/transaction/classic/ClassicTransactionsScreen.kt` (0 Hex, 7 Static = 7 vị trí) -> Tokens: `tokens.onSurface`, `tokens.surface`.
3. `presentation/transaction/modern/ModernTransactionsScreen.kt` (0 Hex, 1 Static = 1 vị trí) -> Tokens: `tokens.onSurface`.
4. `presentation/budget/prism/PrismBudgetScreen.kt` (4 Hex, 2 Static = 6 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `tokens.error`, `tokens.border`.
5. `presentation/budget/classic/ClassicBudgetScreen.kt` (0 Hex, 4 Static = 4 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`.

#### 🔹 Batch 2.9 — Reports Detail BottomSheets & Variants (4 files — 207 vị trí)
1. `presentation/reports/WalletDetailBottomSheet.kt` (58 Hex, 23 Static = 81 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `FinluxColors.*`, `tokens.surfaceSoft`, `tokens.border`.
2. `presentation/reports/CategoryDetailBottomSheet.kt` (39 Hex, 15 Static = 54 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `FinluxColors.*`, `tokens.border`.
3. `presentation/reports/prism/PrismDailyStatementComponents.kt` (40 Hex, 6 Static = 46 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `FinluxColors.IncomeGreen`, `tokens.error`.
4. `presentation/reports/classic/ClassicReportsScreen.kt` & `modern/ModernReportsScreen.kt` (10 Hex, 16 Static = 26 vị trí) -> Tokens: `tokens.surface`, `tokens.onSurface`, `FinluxColors.*`.

#### 🔹 Batch 2.10 — Super Heavyweight Flagship Screens (2 files — 442 vị trí)
1. `presentation/home/prism/PrismHomeScreen.kt` (87 Hex, 14 Static = 101 vị trí) -> Tokens: `tokens.onHero`, `tokens.onHeroMuted`, `tokens.surface`, `tokens.onSurface`, `tokens.surfaceSoft`, `FinluxColors.*`.
2. `presentation/reports/prism/PrismReportsScreen.kt` (234 Hex, 107 Static = 341 vị trí) -> Tokens: Toàn diện hệ thống ChartColors chuẩn, tokens.surface, tokens.onSurface, tokens.onSurfaceVariant, FinluxColors.*.

---

## 🛡️ TRỤ CỘT 3: CHỐT CHẶN CHỐNG CHÁY MÀU & ĐẢM BẢO CONTRAST (THEME ADAPTATION)

### 3.1. Phân Định Rõ Ràng 2 Ngữ Cảnh Thị Giác (Context-Aware Color Mapping)
Để đảm bảo sau khi refactor, chữ không bao giờ bị "tàng hình" (chữ trắng trên nền trắng) hoặc bị "chìm đen" (chữ đen trên nền đen):

```
┌────────────────────────────────────────────────────────────────────────┐
│               PHÂN ĐỊNH 2 NGỮ CẢNH BẢO VỆ ĐỘ TƯƠNG PHẢN                │
├────────────────────────────────┬───────────────────────────────────────┤
│ 1. Bề Mặt Thích Ứng (Adaptive) │ 🎨 Text: tokens.onSurface             │
│    - GlassCard, Sheet, Dialog  │ 🎨 Muted: tokens.onSurfaceVariant     │
│    - Nền đổi theo Dark / Light │ 🎨 Border: tokens.border              │
│    - Tuyệt đối CẤM Color.White │ 🎨 Background: tokens.surface         │
├────────────────────────────────┼───────────────────────────────────────┤
│ 2. Khối Hero Cố Định (Fixed)   │ 🎨 Text: tokens.onHero (#FFFFFF)      │
│    - Hero Total Balance Card   │ 🎨 Muted: tokens.onHeroMuted (82%)    │
│    - Primary Gradient Header   │ 🎨 Glass: tokens.heroGlassSurface(18%)│
│    - Nền luôn là dải Blue/Cyan │ 🎨 An toàn 100% trên nền màu đậm      │
└────────────────────────────────┴───────────────────────────────────────┘
```

### 3.2. Tiêu Chuẩn Độ Tương Phản (WCAG AA Compliance)
- Độ tương phản giữa `tokens.onSurface` và `tokens.surface` đạt tối thiểu **7:1** (vượt chuẩn WCAG AAA).
- Độ tương phản giữa `tokens.onSurfaceVariant` và `tokens.surface` đạt tối thiểu **4.5:1** (chuẩn WCAG AA).
- Giữ nguyên hiệu ứng viền kính phản xạ khúc xạ `tokens.borderAlpha` (10% - 14%), đảm bảo chiều sâu Liquid Glass đặc trưng mà không làm vỡ phân tầng thị giác.

---

## 🧪 TRỤ CỘT 4: MA TRẬN BẢO TOÀN KIỂM THỬ (REGRESSION & TEST GATE)

### 4.1. Quy Trình Kiểm Thử Sau Mỗi Batch (Strict Per-Batch Gate)
Sau khi hoàn thành từng Batch, agent bắt buộc thực hiện đủ 3 bước kiểm tra:
1. **Compilation Check:** Biên dịch mã nguồn Kotlin không phát sinh bất kỳ warning / error nào:
   ```bash
   .\gradlew.bat compileDebugKotlin
   ```
2. **Unit Test Regression Gate:** Bảo toàn 100% số lượng unit tests đạt PASS:
   ```bash
   .\gradlew.bat testDebugUnitTest
   ```
   *Yêu cầu bắt buộc:* **521/521 tests PASS (0 failures, 0 regressions)**.
3. **Build APK Check (Sau mỗi 2-3 batches):**
   ```bash
   .\gradlew.bat assembleDebug
   ```

### 4.2. Chụp Ảnh Màn Hình Kiểm Tra Trực Quan Máy Thật (Physical Device ADB Inspection)
Sau các batch có can thiệp giao diện lớn (Batch 2.1, 2.3, 2.7, 2.10), thực hiện chụp ảnh màn hình cả 2 chế độ Dark và Light qua ADB để nghiệm thu trực quan:
```bash
# Bật Dark Mode và chụp ảnh
adb shell "cmd uimode night yes"
adb shell screencap -p /sdcard/finlux_dark_verify.png
adb pull /sdcard/finlux_dark_verify.png .

# Bật Light Mode và chụp ảnh
adb shell "cmd uimode night no"
adb shell screencap -p /sdcard/finlux_light_verify.png
adb pull /sdcard/finlux_light_verify.png .
```

---

## 📋 TIÊU CHÍ HOÀN THÀNH PHASE 2 (DEFINITION OF DONE - DOD)
- [ ] 100% (52/52) file trong `presentation/` được quét sạch các mã màu hex thô và màu tĩnh `Color.White`/`Color.Black`.
- [ ] Không còn bất kỳ kích thước font fractional nào (`11.5.sp`, `13.5.sp`...) ngoài `FinluxTypography`.
- [ ] Giao diện hiển thị sắc nét, tương phản hoàn hảo ở cả Light Mode và Dark Mode (Liquid Glass).
- [ ] Toàn bộ **521/521 unit tests PASS 100%**.
- [ ] Cập nhật nhật ký tiến độ đầy đủ vào `HANDOVER_LOG.md`.
- [ ] Chờ lệnh phê duyệt bằng văn bản từ Tech Lead trước khi thực hiện commit/push.
