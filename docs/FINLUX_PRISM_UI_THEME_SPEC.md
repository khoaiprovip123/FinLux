# FINLUX — NEW UI THEME SPEC & CONSISTENCY FIX PLAN

> **Mục đích:** Tài liệu này mô tả giao diện mới cho FinLux theo từng màn hình, đồng thời định nghĩa cơ chế lựa chọn theme/UI style trong Cài đặt để áp dụng toàn bộ giao diện một cách nhất quán.
>
> **Định hướng:** Hiện đại, mềm mại, mượt mà, data-first, premium fintech, giảm cảm giác "glass everywhere", tăng khả năng đọc, hierarchy và consistency.
>
> **Phạm vi:** UI/UX + Design System + Theme Switching + State Consistency + Visual Bug Fixes.
>
> **Không thay đổi:** Business logic tài chính, Firestore schema, transaction behavior, applicationId, authentication flow.

---

# 1. TÊN GIAO DIỆN MỚI

Tên đề xuất:

```text
FINLUX PRISM
```

Tên kỹ thuật:

```kotlin
AppUiStyle.PRISM
```

Nếu muốn giữ naming hiện có:

```kotlin
enum class AppUiStyle {
    CLASSIC_LIQUID,
    MODERN_LUXURY,
    PRISM
}
```

## Mục tiêu thị giác

```text
Bento + Spatial Finance
+
Editorial Typography
+
Soft Surface
+
Minimal Glass
+
Dynamic Data Visualization
+
Smooth Motion
```

Không dùng:

```text
glass card ở mọi nơi
blur quá mạnh
gradient quá nhiều
border phát sáng quá nhiều
```

Nguyên tắc:

> Dữ liệu là thành phần thị giác chính. Hiệu ứng chỉ hỗ trợ dữ liệu.

---

# 2. DESIGN SYSTEM TOÀN APP

## 2.1 Color Tokens

### Primary

```text
PrimaryBlue      #3A7BFF
PrimaryViolet    #6F52F5
PrimaryCyan      #23C7E8
```

### Semantic

```text
IncomeGreen      #20B486
ExpenseRed       #EB5C6E
TransferBlue     #3985F5
BudgetViolet     #7052F5
WarningAmber     #F2A43A
NeutralGray      #7A8496
```

### Surface

```text
BackgroundLight  #F6F8FC
SurfacePrimary   #FFFFFF
SurfaceSoft      #F2F5FB
SurfaceGlass     rgba(255,255,255,0.78)
BorderSoft       rgba(90,110,160,0.10)
```

### Dark Theme

```text
BackgroundDark   #0E1118
SurfaceDark      #171B25
SurfaceSoftDark  #1E2430
TextPrimaryDark  #F7F9FC
TextSecondaryDark#A8B0C0
```

---

# 3. TYPOGRAPHY

Chuẩn hóa toàn bộ app.

```text
Display Amount      34–38sp / SemiBold
Screen Title        28–30sp / SemiBold
Section Title       20–22sp / SemiBold
Card Title          16–18sp / Medium
Body                14–16sp / Regular
Caption             12–13sp / Regular
Micro Label         11–12sp / Medium
```

Không để mỗi screen tự khai báo font size riêng nếu có thể dùng token.

---

# 4. SPACING SYSTEM

Chỉ dùng:

```text
4dp
8dp
12dp
16dp
20dp
24dp
32dp
40dp
```

Quy ước:

```text
Screen horizontal padding = 24dp
Section gap = 24dp
Card gap = 12–16dp
Card inner padding = 16–20dp
Bottom safe padding = 24dp
```

---

# 5. CORNER RADIUS

```text
Small chip       12dp
Input            14–16dp
Standard card    20dp
Hero card        28dp
Bottom sheet     28–32dp
Dialog           28dp
Bottom dock      28–32dp
```

Không dùng radius khác nhau tùy screen nếu không có lý do.

---

# 6. GLASS RULE

## Strong Glass

Chỉ dùng cho:

```text
Hero card
Bottom navigation
Bottom sheet
Modal/Dialog
Selected floating controls
```

## Soft Surface

Dùng cho:

```text
Transaction card
Settings list
Report card
Budget card
Category card
Standard content panels
```

Mục tiêu:

```text
80% data clarity
20% visual effect
```

---

# 7. MOTION SYSTEM

Animation phải mềm và nhanh.

```text
Fast interaction       120–160ms
Standard transition    180–240ms
Sheet/Dialog           240–320ms
Chart animation        350–500ms
```

Easing:

```text
FastOutSlowIn
EaseOutCubic
Spring medium-low bounce
```

Không dùng animation quá nảy.

---

# 8. MÀN HÌNH 1 — TRANG CHỦ

## Mục tiêu

Trang chủ là:

```text
Financial Command Center
```

Người dùng mở app phải thấy ngay:

```text
Tài sản hiện tại
Biến động tháng
Thu
Chi
Dòng tiền
Budget
Category
Recent Transactions
```

---

## 8.1 Header

Bố cục:

```text
Logo FinLux
Xin chào 👋
Tên user

                        Bell
                        Avatar
```

Yêu cầu:

- Header cao vừa phải.
- Avatar 40–44dp.
- Bell có unread badge.
- Không để header quá nhiều gradient.

---

## 8.2 Hero — Tài sản ròng

Card chính:

```text
TÀI SẢN RÒNG

6.110.000 đ

▲ +4.2% so với tháng trước
```

### Style

```text
Gradient:
Blue → Violet → Cyan
```

Nhưng:

- text phải contrast cao;
- không dùng quá nhiều bubble/decor;
- chỉ 1–2 accent shape mờ.

### Action

Icon Eye:

```text
show/hide balance
```

---

## 8.3 KPI Row

3 card:

```text
Thu tháng này
Chi tháng này
Dòng tiền / Ngân sách còn lại
```

### Semantic

```text
Income → green
Expense → red
Cashflow → blue/violet
```

Không dùng red/green saturation quá mạnh.

---

## 8.4 Bento Overview

4 mini tile:

```text
Cash Flow
Budget
Spending
Goals
```

Ví dụ:

```text
Cash Flow
mini sparkline

Budget
74%

Spending
97% chi tiêu lớn nhất

Goals
68%
```

Mỗi tile có role rõ.

---

## 8.5 Category Spending

Có thể dùng donut hoặc horizontal bar.

Ưu tiên:

```text
Donut + text list
```

Yêu cầu:

```text
Percentage phải tổng ≈ 100%
```

Nếu rounding:

```text
show 97.1%
2.9%
```

hoặc điều chỉnh remainder hợp lý.

---

## 8.6 Recent Transactions

Mỗi row:

```text
Icon
Category
Note
Date

                 Amount
```

Không hiển thị Edit/Delete trực tiếp.

Action:

```text
tap → detail
long press → action menu
swipe → quick actions
```

---

# 9. MÀN HÌNH 2 — LỊCH SỬ THU CHI

## Header

```text
Lịch sử thu chi
Search
Filter
```

---

## Filter Chips

```text
Tất cả
Thu
Chi
Chuyển tiền
```

Selected chip:

```text
PrimaryBlue / soft gradient
```

Unselected:

```text
SurfaceSoft
```

---

## Summary Card

Không ghi mơ hồ:

```text
Tổng giao dịch trong kỳ
```

Nên động theo filter.

### Khi filter Tất cả

```text
Tổng giá trị giao dịch
2.265.000 đ
3 giao dịch
```

Optional:

```text
Thu: ...
Chi: ...
```

### Khi filter Chi

```text
Tổng chi trong kỳ
```

### Khi filter Thu

```text
Tổng thu trong kỳ
```

---

## Transaction Row

Semantic color:

```text
INCOME       green
EXPENSE      red
TRANSFER     blue
```

Không để:

```text
-2.200.000 đ
```

màu green.

---

## Actions

Không để Edit/Delete icon luôn hiện.

Dùng:

```text
⋮
```

hoặc long press/swipe.

---

# 10. MÀN HÌNH 3 — BÁO CÁO

## Header

```text
Báo cáo
Filter icon
```

---

## Period Selector

```text
Tháng
Quý
Năm
Tùy chọn
```

Selected rõ ràng.

---

## Overview Card

```text
Tổng quan tháng 8, 2026

Thu nhập
0 đ

Chi tiêu
2,3 tr

Dòng tiền
-2,3 tr
```

### Delta

Chỉ show:

```text
+12%
-8%
```

khi có kỳ so sánh.

Nếu chưa có:

```text
—
```

Không show:

```text
▲0%
```

một cách vô nghĩa.

---

## Trend Chart

### Nếu data sparse

Ưu tiên:

```text
Bar Chart
```

### Nếu data liên tục

Có thể:

```text
Line Chart
```

AI agent có thể chọn dựa vào count/data density.

---

## Tooltip

Compact.

Ví dụ:

```text
17/08
Thu  0 đ
Chi  2,3 tr
```

Không chiếm quá nhiều chart.

---

## Expense Breakdown

Có thể dùng:

```text
Bento category blocks
```

Ví dụ:

```text
Tiền trọ    97%
Ăn uống      2%
Khác         1%
```

Card màu phải semantic/brand-consistent.

---

## Insight Section

Thêm:

```text
Insight
```

Ví dụ:

```text
Chi tiêu tháng này tập trung chủ yếu vào Tiền trọ,
chiếm 97% tổng chi.
```

Chưa cần AI.

Có thể rule-based.

---

# 11. MÀN HÌNH 4 — HỒ SƠ & CÀI ĐẶT

## Header

```text
Hồ sơ & Cài đặt
```

---

## Profile Hero

Hiển thị:

```text
Avatar
Tên
Premium badge
Email
```

Card phụ:

```text
Tài sản ròng
6.110.000 đ
```

Không làm Hero quá cao.

---

## Menu

Nhóm:

```text
Tài khoản
Tài chính
Giao diện
Bảo mật
Ứng dụng
```

### Items

```text
Thông tin cá nhân
Ví & tài khoản
Ngân sách cá nhân
Quản lý danh mục
Nhắc nhở thanh toán
Giao diện & tùy chỉnh
Bảo mật
Trợ giúp
Giới thiệu FinLux
```

---

# 12. MÀN HÌNH 5 — TẠO NHANH

## Trigger

Tap nút center FAB.

---

## Bottom Sheet

Title:

```text
Tạo nhanh
```

Subtitle:

```text
Chọn nghiệp vụ tài chính cần thực hiện
```

---

## Actions

```text
Thêm thu
Thêm chi
Chuyển tiền
Scan hóa đơn
Nhắc nhở / Mục tiêu
```

Layout:

```text
2-column grid
+
1 full-width row
```

---

## Semantic

```text
Thu          green
Chi          red
Transfer     blue
Scan         violet
Reminder     orange/purple
```

Bottom sheet:

```text
maxHeight ~ 75–80%
```

Không full-screen nếu không cần.

---

# 13. MÀN HÌNH 6 — THÊM THU / THÊM CHI

## Header

```text
Thêm chi
Back
```

---

## Amount Input

Phải là visual focus:

```text
2.200.000 đ
```

Large typography.

---

## Form

```text
Danh mục
Ví
Ngày
Ghi chú
Hóa đơn
```

### Input style

Không card glass mạnh.

Dùng:

```text
surface soft
border 1dp
radius 16dp
```

---

## Save Button

Full width.

Semantic:

```text
Thêm chi → coral/red gradient
Thêm thu → green gradient
```

---

# 14. MÀN HÌNH 7 — CHI TIÊU THEO DANH MỤC

## Visual mới

Bubble chart / spatial circles.

Ví dụ:

```text
          Tiền Trọ
            97%
         2.200.000

   Ăn uống
      2%

           Khác
            1%
```

### Lợi ích

- khác lạ;
- mềm mại;
- dễ nhìn;
- phù hợp "Prism".

---

## Fallback accessibility

Nếu user bật:

```text
Reduce Motion
High Contrast
```

thì chuyển sang:

```text
horizontal bar chart
```

---

# 15. MÀN HÌNH 8 — GIAO DIỆN & TÙY CHỈNH

Đây là phần quan trọng nhất của UI mới.

Path:

```text
Hồ sơ
→ Giao diện & Tùy chỉnh
```

---

# 16. THEME SWITCHING

## Appearance

```text
Sáng
Tối
Tự động
```

Maps:

```kotlin
ThemePreference.LIGHT
ThemePreference.DARK
ThemePreference.SYSTEM
```

---

# 17. UI STYLE

Thêm:

```text
Phong cách giao diện
```

Options:

```text
Classic Liquid
Modern Luxury
Prism
```

### Mapping

```kotlin
enum class AppUiStyle {
    CLASSIC_LIQUID,
    MODERN_LUXURY,
    PRISM
}
```

---

# 18. UI STYLE PREVIEW

Mỗi style có preview card.

### Classic Liquid

```text
Liquid Glass
Mềm mại + trong suốt
```

### Modern Luxury

```text
Luxury
Tối giản + sang trọng
```

### Prism

```text
Prism
Data-first + spatial + bento
```

---

# 19. APPLY THEME

Khi user chọn:

```text
Prism
```

phải apply toàn app ngay.

Không cần restart.

Flow:

```text
Settings
↓
select Prism
↓
UiPreferencesRepository.save(...)
↓
RootViewModel
↓
StateFlow
↓
FinluxRoot
↓
Theme tokens
↓
all screens update
```

---

# 20. KIẾN TRÚC THEME ĐỀ XUẤT

Không duplicate business screen.

Không làm:

```text
ClassicHomeScreen
ModernHomeScreen
PrismHomeScreen
```

mỗi bản có logic riêng.

Target:

```text
HomeRoute
↓
HomeState
↓
HomeContent
↓
FinluxDesignTokens
```

---

# 21. DESIGN TOKENS

```kotlin
data class FinluxDesignTokens(
    val background: Color,
    val surface: Color,
    val surfaceSoft: Color,
    val heroGradient: List<Color>,
    val cardRadius: Dp,
    val contentRadius: Dp,
    val glassAlpha: Float,
    val borderAlpha: Float,
    val elevation: Dp,
    val spacingScale: FinluxSpacing,
)
```

---

# 22. STYLE TOKENS

```kotlin
val ClassicLiquidTokens = ...
val ModernLuxuryTokens = ...
val PrismTokens = ...
```

Screen không tự quyết định:

```text
radius
alpha
gradient
spacing
shadow
```

---

# 23. FIX CÁC LỖI ĐỒNG NHẤT HIỆN TẠI

## UI-FIX-01 — Semantic Color

Fix:

```text
Expense hiển thị green
```

Rule:

```text
INCOME   → IncomeGreen
EXPENSE  → ExpenseRed
TRANSFER → TransferBlue
```

---

## UI-FIX-02 — Vertical Background Artifact

Hiện có dải sáng vertical trên nhiều screen.

Phải bỏ.

Không dùng:

```text
rectangular translucent overlay
```

Thay bằng:

```text
radial gradient blob
large blur
low alpha
```

---

## UI-FIX-03 — Over Glass

Giảm glass card trên:

```text
History
Settings
Reports
Menu list
```

---

## UI-FIX-04 — Typography

Screen title phải đồng nhất:

```text
Home
History
Reports
Profile
```

không dùng size khác nhau.

---

## UI-FIX-05 — Bottom Navigation

Cùng:

```text
height
radius
icon size
label size
active state
FAB size
```

trên tất cả screen.

---

## UI-FIX-06 — Dialog

Toàn bộ dialog dùng:

```text
FinluxDialog
```

Không dùng Material Dialog mặc định nếu không style.

---

## UI-FIX-07 — Bottom Sheet

Toàn bộ bottom sheet dùng:

```text
FinluxBottomSheet
```

với:

```text
radius
drag handle
padding
background
scrim
animation
```

giống nhau.

---

## UI-FIX-08 — Card Padding

Không để mỗi card tự chọn padding.

Standard:

```text
16dp
20dp
24dp
```

theo loại card.

---

## UI-FIX-09 — Amount Formatting

Chuẩn hóa:

```text
6.110.000 đ
2,3 tr
65.000 đ
```

Không mix format tùy screen nếu không có context.

---

## UI-FIX-10 — Percentage

Tránh:

```text
97% + 2% = 99%
```

Nếu rounding:

```text
97.1%
2.9%
```

hoặc normalize.

---

## UI-FIX-11 — Empty State

Mỗi screen phải có:

```text
Loading
Content
Empty
Error
Offline
```

---

## UI-FIX-12 — Filter State

Selected/unselected style phải thống nhất:

```text
History
Reports
Transactions
Budget
```

---

# 24. UI COMPONENT LIBRARY

Tạo/reuse:

```text
FinluxScreenHeader
FinluxHeroCard
FinluxMetricCard
FinluxSectionHeader
FinluxTransactionRow
FinluxFilterChip
FinluxBottomDock
FinluxQuickActionCard
FinluxBottomSheet
FinluxDialog
FinluxEmptyState
FinluxErrorState
FinluxOfflineState
FinluxAmountText
FinluxInsightCard
```

---

# 25. COMPONENT RULE

Business screen chỉ compose components.

Không copy/paste UI.

Ví dụ:

```kotlin
FinluxTransactionRow(
    transaction = tx,
    category = category,
    onClick = ...
)
```

---

# 26. BOTTOM NAVIGATION SPEC

Items:

```text
Trang chủ
Lịch sử
+
Báo cáo
Hồ sơ
```

### Dimensions

```text
Height            ~72–76dp
Icon              22–24dp
FAB               52–56dp
Label             11–12sp
```

FAB không quá lớn.

---

# 27. ACCESSIBILITY

Bắt buộc:

- minimum touch target 48dp;
- contrast đủ;
- không dùng chỉ màu để truyền trạng thái;
- contentDescription;
- hỗ trợ font scale;
- Reduce Motion;
- High Contrast nếu có;
- chart phải có text equivalent.

---

# 28. DARK MODE

Prism Dark không chỉ invert màu.

Target:

```text
Background   #0E1118
Surface      #171B25
SurfaceSoft  #1E2430
```

Hero vẫn gradient nhưng tối hơn.

Glass:

```text
lower alpha
higher contrast border
```

---

# 29. SETTINGS — CẤU TRÚC ĐỀ XUẤT

```text
Giao diện & tùy chỉnh

1. Chế độ
   [Sáng] [Tối] [Tự động]

2. Phong cách giao diện
   [Classic Liquid]
   [Modern Luxury]
   [Prism]

3. Màu chủ đạo
   ● Purple
   ● Blue
   ● Cyan
   ● Green
   ● Orange
   ● Rose

4. Hiệu ứng
   Animation ON/OFF
   Glass intensity
   Reduce motion

5. Mật độ
   Comfortable
   Compact
```

---

# 30. PERSISTENCE

Store bằng DataStore.

```text
themePreference
uiStyle
accentColor
glassIntensity
cardDensity
animationsEnabled
reduceMotion
```

---

# 31. MIGRATION

User hiện tại:

```text
CLASSIC_LIQUID
```

giữ nguyên.

Không tự chuyển Prism.

Prism chỉ apply nếu user chọn.

---

# 32. IMPLEMENTATION PHASES

## PHASE UI-1 — Tokens

```text
Color
Typography
Spacing
Radius
Elevation
Motion
```

---

## PHASE UI-2 — Shared Components

Tạo component library.

---

## PHASE UI-3 — Theme Switching

```text
Classic
Modern
Prism
```

---

## PHASE UI-4 — Home + Bottom Nav

Triển khai Prism Home trước.

---

## PHASE UI-5 — History + Reports

Fix semantic + chart + transaction row.

---

## PHASE UI-6 — Profile + Settings

Tạo UI Theme Selector.

---

## PHASE UI-7 — Quick Add + Forms

Quick actions + transaction editor.

---

## PHASE UI-8 — Dialog + Sheet + States

Đồng nhất modal/bottomsheet/loading/error/offline.

---

# 33. ACCEPTANCE CRITERIA TOÀN UI

```text
[ ] Prism có thể chọn trong Settings
[ ] Apply realtime toàn app
[ ] Không restart app
[ ] Classic vẫn hoạt động
[ ] Modern vẫn hoạt động
[ ] Business logic không duplicate
[ ] Semantic colors đúng
[ ] Bottom nav giống nhau mọi screen
[ ] Screen titles cùng typography
[ ] Card radius đồng nhất
[ ] Dialog đồng nhất
[ ] Bottom sheet đồng nhất
[ ] Không còn vertical background artifact
[ ] Percentage hợp lý
[ ] Amount format consistent
[ ] Loading/Empty/Error/Offline có state
[ ] Dark mode hợp lý
[ ] Accessibility cơ bản pass
```

---

# 34. KHÔNG ĐƯỢC LÀM

AI không được:

```text
rewrite ViewModel
rewrite Repository
thay Firestore schema
thay navigation toàn bộ
xóa Classic/Modern
duplicate business state per theme
tạo riêng 3 ViewModel cho 3 UI style
```

---

# 35. MASTER PROMPT CHO AI CODING AGENT

```text
Bạn là Senior Android UI Architect phụ trách triển khai FinLux Prism UI.

Repository:
khoaiprovip123/FinLux

Đọc toàn bộ tài liệu:
docs/FINLUX_PRISM_UI_THEME_SPEC.md

Mục tiêu:
- thêm UI style mới PRISM;
- giữ nguyên Classic Liquid và Modern Luxury;
- user chọn giao diện trong Settings;
- theme apply realtime toàn app;
- không duplicate business logic;
- chuẩn hóa Design System;
- fix các lỗi consistency được liệt kê trong tài liệu.

QUY TẮC:
1. Không rewrite business logic.
2. Không đổi Firestore schema.
3. Không xóa UI style hiện có.
4. Không duplicate ViewModel theo theme.
5. Một screen dùng cùng state/event contract.
6. Theme khác nhau phải chủ yếu khác token/component skin.
7. Mỗi phase phải compile và test trước khi chuyển phase tiếp.
8. Không làm toàn bộ app trong một commit.

Bắt đầu CHỈ với:
PHASE UI-1 — Design Tokens
và
PHASE UI-2 — Shared Components foundation.

Sau khi hoàn thành:
- báo file changed;
- báo component/token mới;
- chạy assembleDebug;
- không tự động làm PHASE UI-3 nếu chưa PASS.
```

---

# 36. PROMPT CHO THEME SWITCHING

```text
Thực hiện PHASE UI-3.

Thêm AppUiStyle.PRISM.

Settings:
Giao diện & tùy chỉnh
→ Phong cách giao diện
→ Classic Liquid / Modern Luxury / Prism

Yêu cầu:
- lưu lựa chọn bằng UiPreferencesRepository/DataStore hiện tại;
- RootViewModel observe;
- apply realtime không restart app;
- default user cũ không bị chuyển theme;
- không duplicate business logic;
- Classic/Modern không regression.

Tạo preview card cho Prism:
"Prism — Data-first + Spatial + Bento"

Sau khi hoàn thành chạy:
testDebugUnitTest
assembleDebug
```

---

# 37. PROMPT CHO UI CONSISTENCY FIX

```text
Thực hiện các UI-FIX trong FINLUX_PRISM_UI_THEME_SPEC.md:

P0:
- semantic color transaction;
- remove vertical background artifact;
- fix percentage normalization;
- fix wording transaction summary;
- hide meaningless 0% delta.

P1:
- typography tokens;
- spacing tokens;
- bottom nav consistency;
- dialog consistency;
- bottom sheet consistency;
- reduce over-glass.

Không đổi business logic.
Không redesign ngoài phạm vi.
```

---

# 38. KẾT LUẬN

FinLux Prism phải tạo cảm giác:

```text
Modern
Soft
Premium
Financial
Data-first
Different
Smooth
Consistent
```

Không phải:

```text
more glass
more glow
more gradient
```

Mục tiêu cuối cùng:

> **Một hệ thống giao diện tài chính hiện đại, mềm mại và có nhận diện riêng, nhưng dữ liệu luôn rõ ràng hơn hiệu ứng.**
