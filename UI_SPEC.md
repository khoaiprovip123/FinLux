# UI SPEC — Finlux

## 0. Design System — "Liquid Glass" (giống iOS)

Yêu cầu áp dụng **toàn app** (nav bar, top bar, card, dialog, bottom sheet, FAB):

- **Vật liệu:** lớp nền mờ (frosted glass) — dùng `Modifier.graphicsLayer { renderEffect = RenderEffect.createBlurEffect(...) }` (API 31+) hoặc `Modifier.blur()` của Compose (API 12+ trong Compose 1.6+ dùng `androidx.compose.ui.blur` cho toàn bộ background content phía sau component).
- **Độ trong suốt:** nền kính alpha ~0.55-0.75, viền `1dp` gradient trắng mờ (light) / trắng 10-15% (dark) tạo hiệu ứng "rim light" giống iOS.
- **Bóng & độ sâu (depth):** shadow nhẹ (elevation thấp, blur radius lớn) để tạo cảm giác nổi trên nền, không dùng shadow cứng kiểu Material cũ.
- **Chuyển động:** khi cuộn (scroll) nội dung phía sau, lớp kính phải blur theo thời gian thực (không phải ảnh tĩnh) — dùng `RenderEffect` gắn vào layer chứa nội dung cuộn phía sau top bar/bottom nav.
- **Theme sáng/tối:** xem BR-04 (BA_SPEC) — light: kính sáng + viền trắng; dark: kính tối (đen mờ) + viền sáng nhẹ + glow màu accent nhạt.
- **Fallback thiết bị < Android 12:** thay blur động bằng lớp overlay `Brush.verticalGradient` bán trong suốt tĩnh, giữ đúng bố cục — không blur real-time (giới hạn kỹ thuật `RenderEffect`).
- **System bar insets:** mọi màn edge-to-edge phải chừa `statusBars`/`navigationBars` theo thiết bị. `GlassBottomNav`
  không ép chiều cao tổng; vùng nội dung 80dp được cộng thêm navigation inset để không bị ba phím Android che.
- **Component chuẩn cần build:** `GlassCard`, `GlassTopBar`, `GlassBottomNav`, `GlassBottomSheet`, `GlassDialog`, `GlassFAB` — dùng chung 1 base `LiquidGlassSurface` composable.

> **Đã xác nhận theo visual reference 12/08/2026:** màu chủ đạo xanh `#3478F6`, phối tím
> `#7758F6` và cyan `#47C8FF` trong hero/CTA gradient. Nền sáng `#F5F7FC`, card sáng gần đặc
> để bảo đảm tương phản; dark theme dùng cùng brand color với surface tối.

### Visual hierarchy refresh (12/08/2026)
- Dashboard ưu tiên một hero card "Tổng số dư", bên dưới là 3 metric tile Thu/Chi/Số ví.
- Card nội dung dùng bo góc 18-22dp, shadow mềm và border mảnh; không dùng blur dày làm giảm độ đọc.
- Giao dịch hiển thị icon nền màu theo loại, tên/ghi chú ở giữa và số tiền canh phải.
- Báo cáo donut và bar chart chỉ render từ dữ liệu repository thật, không dùng số minh họa hard-code.
- Bottom navigation giữ đúng 5 action của V1: Trang chủ, Báo cáo, Thêm, Ngân sách, Cài đặt.

### Interaction & customization refresh (12/08/2026)
- Mọi `GlassCard` tương tác dùng phản hồi co nhẹ khi nhấn; có thể tắt trong Cài đặt.
- Viền kính dùng gradient rim-light đa sắc, độ glow theo 3 mức Nhẹ/Cân bằng/Rực rỡ.
- Mật độ thẻ có 2 mức Thoáng/Gọn; padding được điều khiển tập trung từ design system.
- Ví, danh mục và thẻ ngân sách có chiều cao/padding thống nhất trong cùng một danh sách.
- Icon danh mục dùng bộ icon tài chính/đời sống mở rộng và màu nhận diện do người dùng chọn.

### Home Liquid Glass refresh (12/08/2026)
- Dashboard dùng nền aura xanh/tím/cyan được blur phần cứng và trôi nhẹ phía dưới nội dung.
- Thẻ Home là vật liệu kính bán trong suốt nhiều lớp: tint nhẹ, rim-light, highlight khúc xạ,
  bóng màu và phản hồi co mềm khi chạm; chữ/nội dung không bị blur.
- Hero tài sản có ẩn/hiện số dư, chip kỳ báo cáo và trạng thái dòng tiền.
- Ba thao tác nhanh Thu/Chi/Chuyển, ba metric tile và các thẻ ví dùng kích thước đồng đều.
- Bottom navigation giảm độ đặc để hòa vào nền nhưng vẫn giữ tương phản chữ/icon.

### Home visual alignment — supplied FinLux board (12/08/2026)
- Dashboard bám đúng reference: header chào hỏi tối giản, hero gradient xanh-tím, 3 KPI nhỏ,
  tiêu đề giao dịch và danh sách phẳng ngay bên dưới.
- Aura động toàn màn hình được loại bỏ; nền trắng-xanh sạch với ambient light rất nhẹ.
- Liquid Glass chỉ dùng có chủ đích ở KPI/bottom bar; hero giữ water highlight và lensing tinh tế.
- Khoảng cách, bo góc và chiều cao navigation được thu gọn theo tỷ lệ màn hình mẫu.

---

## 1. SCREEN: Splash / Auto-login
```
Route: /splash
Trigger: Mở app

LAYOUT:
┌─────────────────────────────┐
│      Logo + tên app          │
│      (nền gradient/blur)     │
└─────────────────────────────┘

STATES:
  - Kiểm tra session Firebase Auth (1-2s) → điều hướng Home (đã login) hoặc Login (chưa login)
```

## 2. SCREEN: Đăng nhập (Login)
```
Route: /login
LAYOUT:
┌─────────────────────────────────────────┐
│ Header (Top Gradient):                  │
│  - Logo: "FinLux" (Fin đen, Lux xanh)   │
│  - Title: "Chào mừng trở lại! 👋"        │
│  - Subtitle: "Đăng nhập để tiếp tục..." │
│  - 3D Illustration: Glass Wallet + Shield│
├─────────────────────────────────────────┤
│ Form Card (Bottom White Container):     │
│  - Tab Switcher: [Đăng nhập] | Đăng ký  │
│  - Field: Email hoặc số điện thoại      │
│  - Field: Mật khẩu (ẩn/hiện)            │
│  - Checkbox: "Ghi nhớ đăng nhập" | Link │
│  - Button: "Đăng nhập" (Gradient + ->)  │
│  - Divider: "hoặc đăng nhập với"        │
│  - Social Cards: [Google] [Apple] [FB]  │
│  - Footer: "Bằng việc đăng nhập..."     │
└─────────────────────────────────────────┘

VISUAL ALIGNMENT (12/08/2026):
  - Header dùng nền trắng-xanh rất nhạt, logo chữ FinLux bên trái và minh họa ví 3D bên phải.
  - Form đăng nhập là surface trắng bo riêng hai góc trên 32dp; tab trải đều toàn chiều rộng.
  - Social Cards cao 76dp, icon thương hiệu ở trên và tên provider ở dưới, ba thẻ có cùng kích thước.
  - Google/Apple/Facebook có contract callback riêng để kết nối provider SDK sau này; chưa tự chạy OAuth
    khi chưa có Client ID, redirect URI và cấu hình Firebase/Meta/Apple hợp lệ.

VALIDATION:
  - Email: đúng định dạng → "Email không hợp lệ"
  - Mật khẩu: không rỗng → "Vui lòng nhập mật khẩu"
```

## 3. SCREEN: Đăng ký (Register)
```
Route: /register
LAYOUT:
┌─────────────────────────────────────────┐
│ Header (Top Gradient):                  │
│  - Button: Back (<)                      │
│  - Logo: "FinLux"                       │
│  - Title: "Tạo tài khoản mới"           │
│  - Subtitle: "Bắt đầu hành trình..."   │
│  - 3D Illustration: Clipboard + Shield │
├─────────────────────────────────────────┤
│ Form Area (liền với nền sáng):          │
│  - Field: Họ và tên                     │
│  - Field: Email                         │
│  - Field: Số điện thoại                 │
│  - Field: Mật khẩu                      │
│  - Progress Bar: Độ mạnh mật khẩu       │
│  - Field: Xác nhận mật khẩu             │
│  - Checkbox: Đồng ý điều khoản          │
│  - Button: "Tạo tài khoản" (Gradient)   │
│  - Link Footer: "Đã có tài khoản?"      │
└─────────────────────────────────────────┘
VALIDATION: theo BR-01 (≥8 ký tự, có chữ+số), mật khẩu xác nhận phải khớp, bắt buộc tích chọn đồng ý điều khoản
```
ACTIONS: "Tạo tài khoản" → Firebase createUser + seed dữ liệu mặc định (UC-01) → Home
```

## 4. SCREEN: Home / Dashboard
```
Route: /home
LAYOUT:
┌───────────────────────────────────────────┐
│ Logo FinLux | "Xin chào" + {tên} | Thông báo | Avatar │
├───────────────────────────────────────────┤
│ GradientHeroCard "Tổng tài sản": số dư, xu hướng, nút ẩn/hiện │
├───────────────────────────────────────────┤
│ 3 KPI đều nhau: Thu tháng này | Chi tháng này | Ngân sách còn lại │
├───────────────────────────────────────────┤
│ GlassCard donut "Chi tiêu theo danh mục" + chú giải │
│ Danh sách giao dịch gần đây                 │
│   - Row: [icon danh mục] Tên danh mục | ghi chú | số tiền (+/- màu xanh/đỏ) │
├───────────────────────────────────────────┤
│ GlassBottomNav: Trang chủ | Ví | [FAB +] | Báo cáo | Hồ sơ │
└───────────────────────────────────────────┘

STATES:
  - Loading: shimmer skeleton dạng kính mờ
  - Error: "Không tải được dữ liệu, thử lại" + nút Retry

ACTIONS:
  - FAB "+" → menu tạo nhanh: Thêm Thu, Thêm Chi, Chuyển tiền, Quét hóa đơn, Thêm mục tiêu
  - Tap KPI "Thu tháng này" → /income; tap KPI "Ngân sách còn lại" → /budget
  - Tap giao dịch → /transaction/{id} (sửa)
  - Tap avatar → /profile
  - Tap thông báo → /notifications
  - Swipe trái trên item giao dịch → hiện nút Xóa
```

## 5. SCREEN: Thêm/Sửa giao dịch (Bottom Sheet hoặc full screen)
```
Route: /transaction/add | /transaction/{id}/edit
LAYOUT:
┌─────────────────────────────────────┐
│ GlassBottomSheet                     │
│  - Toggle: Chi | Thu (segmented)     │
│  - Field: Số tiền | number | required, numpad lớn│
│  - Field: Danh mục | picker (grid icon) | required │
│  - Field: Ví | dropdown | required   │
│  - Field: Ngày | date picker | default = hôm nay │
│  - Field: Ghi chú | text | optional  │
│  - Field: Ảnh hóa đơn | image picker | optional │
│  - Button: "Lưu"                     │
│  - (nếu edit) Button: "Xóa" (màu đỏ) │
└─────────────────────────────────────┘

VALIDATION: theo BR-05 (số tiền > 0), Danh mục & Ví bắt buộc chọn
ACTIONS:
  - "Lưu" → ghi Firestore transaction (batch cập nhật wallet.balance) → đóng sheet, refresh Home
  - "Xóa" → dialog xác nhận → xóa + hoàn lại số dư ví (BR-06)
```

## 6. SCREEN: Danh sách giao dịch (đầy đủ, có lọc/tìm kiếm)
```
Route: /transactions
LAYOUT:
┌───────────────────────────────────────────┐
│ GlassTopBar: "Giao dịch" + icon Lọc + icon Tìm kiếm │
│ Filter chip row: Tất cả | Thu | Chi | [Danh mục ▾] | [Ví ▾] | [Khoảng thời gian ▾] │
│ Danh sách nhóm theo ngày (giống Home nhưng đầy đủ, có phân trang) │
└───────────────────────────────────────────┘
STATES: Empty (không có kết quả lọc) | Loading (paging) | Error
ACTIONS: Tap item → sửa; Filter chip → mở GlassBottomSheet chọn điều kiện
```

## 7. SCREEN: Danh mục (Categories)
```
Route: /categories
LAYOUT:
┌─────────────────────────────────────┐
│ Tab: Danh mục Chi | Danh mục Thu     │
│ Grid icon danh mục (icon + tên + màu)│
│ FAB "+" → thêm danh mục mới          │
└─────────────────────────────────────┘
ACTIONS:
  - Tap danh mục → sửa (đổi tên/icon/màu) — danh mục mặc định không cho xóa nếu đã có giao dịch gắn vào
  - Long-press → Xóa (chỉ khi không có giao dịch nào dùng danh mục đó, nếu có → cảnh báo)
```

## 8. SCREEN: Ví / Tài khoản (Wallets)
```
Route: /wallets
LAYOUT:
┌─────────────────────────────────────┐
│ GlassTopBar: "Ví của tôi" | + Thêm ví │
│ GradientHeroCard: Tổng số dư toàn bộ ví │
│ Filter: Tất cả | Tiền mặt | Ngân hàng | Ví điện tử | Thẻ | Đầu tư │
│ Danh sách GlassCard: icon, tên, loại, số dư, tỷ trọng % │
│ CTA "Thêm ví mới" ở cuối danh sách    │
│ GlassBottomNav giống Dashboard          │
└─────────────────────────────────────┘
ACTIONS: Tap hoặc vuốt phải → sửa; vuốt trái → xác nhận xóa; icon chuyển tiền → UC-13.
Xóa vẫn chặn với ví mặc định hoặc ví đã có giao dịch để bảo toàn lịch sử.
```

## 8A. SCREEN: Thu nhập (Income)
```
Route: /income
LAYOUT:
┌─────────────────────────────────────┐
│ GlassTopBar: "Thu nhập" | + Thêm    │
│ Bộ chọn tháng trước/sau             │
│ GradientHeroCard: Tổng thu nhập tháng │
│ 4 StatisticCard: bình quân/ngày, số giao dịch, cao nhất, thấp nhất │
│ Phân tích nguồn thu theo danh mục + progress │
│ Danh sách thu nhập: icon, tên, ngày, số tiền xanh │
│ GlassBottomNav giống Dashboard      │
└─────────────────────────────────────┘
STATES: tháng không có dữ liệu hiển thị empty state có CTA thêm thu nhập.
ACTIONS: đổi tháng; nhấn "+ Thêm" mở form giao dịch được chọn sẵn loại Thu.
```

## 9. SCREEN: Ngân sách (Budget)
```
Route: /budget
LAYOUT:
┌─────────────────────────────────────┐
│ Chọn tháng (mặc định tháng hiện tại) │
│ Danh sách GlassCard theo danh mục:   │
│   - Tên danh mục, icon               │
│   - Progress bar (đã chi / hạn mức)  │
│   - Màu: xanh (<80%), vàng (80-100%), đỏ (>100%) │
│ FAB "+" → đặt ngân sách cho danh mục chưa có │
└─────────────────────────────────────┘
```

## 10. SCREEN: Báo cáo (Reports)
```
Route: /reports
THEME: luôn kế thừa `ThemePreference` + `VisualStyle` từ root; không ép nền tối cục bộ.
  LAYOUT:
  ┌─────────────────────────────────────┐
  │ Segmented: Tháng | Quý | Năm | Tùy chọn │
  │ GlassCard tổng quan: Thu nhập / Chi tiêu / Tiết kiệm + % so với kỳ trước │
  │ Line chart: hai đường Thu-Chi, điểm dữ liệu, đường focus và tooltip theo ngày │
  │ Treemap bất đối xứng: phân bổ Chi theo danh mục, số tiền và tỷ trọng │
  │ Báo cáo theo ví: icon, số tiền, tỷ trọng và thanh tiến độ │
  │ Button: "Xuất báo cáo" (icon) → mở dialog chọn Excel/PDF (UC-17) │
  └─────────────────────────────────────┘
STATES: Empty (chưa có dữ liệu trong kỳ) → hiện minh họa + gợi ý thêm giao dịch
```

## 10A. SCREEN: Chi tiêu (Expense)
```
Route: /expense
LAYOUT:
┌─────────────────────────────────────┐
│ GlassTopBar: "Chi tiêu" | + Thêm    │
│ Bộ chọn tháng trước/sau             │
│ GradientHeroCard: Tổng chi + so sánh tháng trước │
│ Donut: chi tiêu theo danh mục + tỷ trọng │
│ Bar chart: chi tiêu từng ngày trong tháng │
│ Danh sách giao dịch chi gần đây     │
│ GlassBottomNav giống Dashboard      │
└─────────────────────────────────────┘
ACTIONS: đổi tháng; nhấn "+ Thêm" mở form ở trạng thái Chi; tap KPI "Chi tháng này" từ Home để mở.
```

## 11. SCREEN: Thông báo (Notifications)
```
Route: /notifications
LAYOUT: Danh sách thông báo (cảnh báo ngân sách, nhắc bill), nhóm theo ngày, đánh dấu đã đọc/chưa đọc
```

## 12. SCREEN: Cài đặt & Hồ sơ (Settings/Profile)
```
Route: /settings
LAYOUT:
┌─────────────────────────────────────┐
│ Hero gradient hồ sơ: Avatar (tap chọn Thư viện/Camera) | Tên (tap để đổi) | Email | Tổng tài sản │
│ 4 thẻ nhanh: Ví | Ngân sách | Danh mục | Nhắc nhở │
│ Menu quản lý và thông báo │
│ Mục: Thông tin cá nhân → dialog đổi tên hiển thị (UC-05A) │
│ Mục: Phong cách (Tối giản hiện đại/Glassmorphism/Gradient năng động) — UC-21 │
│ Mục: Giao diện (Sáng/Tối/Hệ thống) — UC-06  │
│ Mục: Liquid Glass (cường độ, mật độ thẻ, hiệu ứng chạm) — UC-21 │
│ GlassCard Giới thiệu FinLux ở cuối: logo | slogan | phiên bản | mô tả │
│ Mục: Quản lý Danh mục → /categories  │
│ Mục: Quản lý Ví → /wallets           │
│ Mục: Nhắc nhở định kỳ → /reminders (UC-18) │
│ Mục: Bảo mật (khóa sinh trắc học) `[Cần xác nhận]` │
│ Mục: Ngôn ngữ `[Cần xác nhận nếu có]` │
│ Button: "Đăng xuất"                  │
└─────────────────────────────────────┘
```

### 12.1. Phong cách giao diện toàn cục

- **Tối giản hiện đại:** nền navy đậm, viền xanh mảnh, bề mặt ít trong suốt và điểm nhấn electric blue.
- **Glassmorphism:** nền xanh-tím có aura, bề mặt kính trong nhiều lớp, rim-light trắng/tím và glow mềm.
- **Gradient năng động:** gradient tím-xanh-cyan rực rỡ ở màn hình nhận diện; màn hình nội dung dùng nền sáng và thẻ trắng rõ nét.
- Splash, Auth, Dashboard, thẻ dùng chung, FAB và bottom navigation phải đọc cùng `VisualStyle` từ `UiPreferences`; không tạo màu riêng rải rác trong feature.
- Đổi phong cách áp dụng tức thời, giữ lại sau khi mở lại ứng dụng và không thay đổi dữ liệu tài chính.

## 13. SCREEN: Nhắc nhở định kỳ (Reminders/Bill)
```

## 15. SCREEN: Quét hóa đơn
```
Route: lớp phủ toàn màn hình từ FAB
LAYOUT: top bar đóng/flash, khung quét bo 28dp, nút thư viện, nút chụp lớn; màu kính đọc từ theme chung.
ACTIONS: chụp/chọn ảnh → /transaction/add ở trạng thái Chi và hiển thị "Đã đính kèm hóa đơn".
```

## 16. SCREEN: Mục tiêu tài chính
```
Route: /goals
LAYOUT: danh sách GlassCard mục tiêu và form full screen: tên, số tiền, hạn, danh mục, tích lũy/tháng, ảnh.
STATES: Empty State có CTA; form có validation/loading/error. Toàn màn hình thích ứng Sáng/Tối và VisualStyle.
```
Route: /reminders
LAYOUT: Danh sách bill định kỳ (tên, số tiền, chu kỳ, ngày nhắc tiếp theo) + FAB thêm mới
Form thêm: Tên bill, Số tiền, Danh mục, Ví, Chu kỳ (ngày/tuần/tháng), Ngày bắt đầu, Bật/tắt nhắc
```

---

## 14. User Flow tổng quát

### Điều hướng màn hình chính

- Bốn màn hình chính `Home ↔ Wallets ↔ Reports ↔ Settings` hỗ trợ vuốt ngang hai chiều và bấm bottom navigation.
- Vuốt sang trái mở màn hình kế tiếp; vuốt sang phải quay về màn hình trước. Nội dung bám nhẹ theo ngón tay,
  xuất hiện ánh sáng cạnh và spring khi thả; chỉ đổi trang sau khi vượt ngưỡng để không xung đột cuộn dọc.
- Mỗi phong cách dùng hình thức icon riêng: Tối giản hiện đại dùng viền xanh, Glassmorphism dùng orb kính và Gradient năng động dùng ô gradient.
- Màn hình đăng nhập, form nhập liệu, dialog và bottom sheet không áp dụng cử chỉ chuyển tab.

```
[Chưa đăng nhập] → Splash → Login/Register → Home
[Đã đăng nhập] → Splash → Home
Home → FAB(+) → Thêm giao dịch → Lưu → Home (cập nhật realtime)
Home → tab Báo cáo → chọn kỳ → xem chart → Xuất file → chọn định dạng → Lưu/Share file
Home → tab Ngân sách → đặt hạn mức danh mục → (System) theo dõi → cảnh báo qua Notification
Settings → Avatar → Camera/Thư viện → Crop → Lưu → cập nhật khắp app
Settings → Giao diện → chọn Tối → toàn app đổi theme + đổi tông Liquid Glass ngay lập tức
```
