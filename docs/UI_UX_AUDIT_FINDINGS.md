# BÁO CÁO TỔNG HỢP KIỂM TOÁN UI/UX TOÀN DIỆN CẢ 4 CỤM (UI/UX AUDIT MASTER FINDINGS)

**Dự án:** FinLux Android App (Personal Finance Management)  
**Công nghệ:** Kotlin, Jetpack Compose, Material 3, Liquid Glass Architecture  
**Ngày thực hiện kiểm toán:** 29/08/2026  
**Môi trường kiểm thử:** Thiết bị thật (Redmi Note kết nối ADB, Android 14, Display 1080x2400, Density ~2.75x) + Android Emulator  
**Phiên bản gốc kiểm thử:** `v1.12.2` (Build 149)  
**Tài liệu tham chiếu:** `docs/CONTEXT.md`, `docs/BA_SPEC.md`, `docs/UI_SPEC.md`, `docs/DATA_SPEC.md`, `AGENTS.md`, `callstack-liquid-glass`

---

## 1. TIẾN ĐỘ & DANH MỤC MÀN HÌNH ĐÃ QUÉT TRÊN THỰC TẾ (PROGRESS TRACKER)

| Cụm | Tên Màn hình / Component | File mã nguồn chính | Trạng thái quét | Kết quả kiểm thử trực tiếp trên thiết bị |
|---|---|---|---|---|
| **CỤM 1** | **Trang chủ Modern Luxury & Classic** | `home/modern/ModernHomeScreen.kt`<br>`home/classic/ClassicHomeScreen.kt` | 🟢 **100% HOÀN TẤT** | Test Dark/Light Mode, số dư cực lớn (> 11 chữ số), Hero card, cuộn danh sách. |
| **CỤM 1** | **Lịch sử giao dịch (Transactions)** | `transaction/modern/ModernTransactionsScreen.kt`<br>`transaction/classic/ClassicTransactionsScreen.kt` | 🟢 **100% HOÀN TẤT** | Phát hiện lỗi nghiêm trọng: Nền trắng xóa trong Dark Mode, chữ nhóm ngày bị tàng hình, cột ghi chú va chạm số tiền. |
| **CỤM 1** | **Form Thêm/Sửa GD (AddTransactionSheet)** | `transaction/AddTransactionSheet.kt` | 🟢 **100% HOÀN TẤT** | Test nhập số tiền lớn, bàn phím IME, bộ chọn danh mục, bộ chọn ví. |
| **CỤM 1** | **Sheet Chi tiết GD (TransactionDetailSheet)** | `transaction/TransactionDetailSheet.kt` | 🟢 **100% HOÀN TẤT** | Test hiển thị chi tiết, Dialog xác nhận xóa, subtext nút hành động có dấu phẩy thừa. |
| **CỤM 1** | **Bộ chọn danh mục (CategoryPicker)** | `designsystem/component/FinluxFormComponents.kt` | 🟢 **100% HOÀN TẤT** | Test grid 4 cột, phát hiện lỗi cắt ngắn chữ cụt ngủn (`maxLines = 1`). |
| **CỤM 2** | **Màn hình Ví & Tài khoản (WalletsScreen)** | `wallet/modern/ModernWalletsScreen.kt`<br>`wallet/classic/ClassicWalletsScreen.kt` | 🟢 **100% HOÀN TẤT** | Test thẻ tổng số dư, danh sách thẻ ví, form Thêm/Sửa ví, chuyển tiền giữa các ví (`TransferEditor`). |
| **CỤM 2** | **Màn hình Ngân sách (BudgetScreen)** | `budget/modern/ModernBudgetScreen.kt`<br>`budget/classic/ClassicBudgetScreen.kt` | 🟢 **100% HOÀN TẤT** | Test thanh tiến độ %, cảnh báo ngưỡng tự động 80%-100%, form tạo/sửa ngân sách, selector kỳ ngân sách. |
| **CỤM 3** | **Màn hình Báo cáo & Phân tích (ReportsScreen)** | `reports/modern/ModernReportsScreen.kt`<br>`reports/classic/ClassicReportsScreen.kt` | 🟢 **100% HOÀN TẤT** | Test biểu đồ xu hướng Line Chart + Tooltip, Treemap phân bổ chi tiêu, Báo cáo theo ví, Nút Xuất báo cáo bị che khuất. |
| **CỤM 4** | **Quản lý danh mục (CategoriesScreen)** | `category/CategoriesScreen.kt` | 🟢 **100% HOÀN TẤT** | Test lọc danh mục Thu/Chi, Thêm/Sửa/Xóa danh mục, Icon & Color picker, nhãn dài bị ellipsis. |
| **CỤM 4** | **Nhắc nhở thanh toán (RemindersScreen)** | `reminders/RemindersScreen.kt` | 🟢 **100% HOÀN TẤT** | Test danh sách nhắc nhở, bật/tắt Switch, Sheet tạo/sửa nhắc nhở, chọn chu kỳ lặp lại và giờ báo. |
| **CỤM 4** | **Cài đặt, Chu kỳ lương & Theme (Settings)** | `settings/SettingsScreen.kt`<br>`settings/SalaryCycleSheet.kt` | 🟢 **100% HOÀN TẤT** | Test đổi Theme Sáng/Tối/Hệ thống, tùy biến Liquid Glass, phát hiện lỗi text dính liền chu kỳ tài chính. |
| **HỆ THỐNG** | **Design System & Nền Kính (ModernLiquidGlass)** | `designsystem/modern/ModernLiquidGlass.kt` | 🟢 **100% HOÀN TẤT** | Phát hiện lỗi cốt lõi `luminance() < 0.4f` ép nền trắng cho toàn bộ `GlassBottomSheet` & `GlassDialogSurface` trong Dark Mode. |

---

## 2. BẢNG TỔNG HỢP TOÀN BỘ 14 LỖI & ĐIỂM BẤT THƯỜNG TRÊN 4 CỤM

| STT | Cụm | Màn hình / Component | File Path & Vị trí Code | Theme & Chế độ bị lỗi | Mô tả chi tiết hiện trạng & Lỗi thị giác | Mức độ | Đề xuất giải pháp sửa chữa chi tiết |
|---|---|---|---|---|---|---|---|
| **01** | **Cụm 1** | **Lịch sử giao dịch (`ModernTransactionsScreen`)** | `presentation/transaction/modern/ModernTransactionsScreen.kt`<br>`Line: 160-205, 270-360` | **Modern Luxury & Classic — Dark Mode** | **NỀN TRẮNG BỊ LẠC QUẺ TRONG DARK MODE & MỜ CHỮ TIÊU ĐỀ NGÀY:**<br>Khi người dùng bật Dark Mode trong Cài đặt, màn hình Lịch sử giao dịch vẫn hiển thị nền trắng xóa và thẻ giao dịch màu trắng tinh.<br>Chữ tiêu đề nhóm ngày ("*Hôm nay (1)*", "*Hôm qua (2)*") có màu xám trắng (`onSurfaceVariant`) bị **chìm hoàn toàn/vô hình** trên nền trắng. | 🔴 **CRITICAL** | Cập nhật container `Scaffold` và `GlassCard` sử dụng `LocalFinluxTokens.current.surface` / `surfaceSoft` thay vì hardcode màu sáng. Bọc nền bằng `FinluxStyleBackdrop` đồng bộ như `ModernHomeScreen`. |
| **02** | **Cụm 1** | **Thẻ giao dịch (`ModernTransactionsScreen` & `ClassicTransactionsScreen`)** | `presentation/transaction/modern/ModernTransactionsScreen.kt`<br>`Line: 310-360` | **Tất cả các Theme & Mode** | **XUNG ĐỘT BỐ CỤC TEXT VỚI CỘT SỐ TIỀN & ACTION ICONS:**<br>Cột nội dung bên trái (Ghi chú + Danh mục + Ví + Ngày giờ) không được bọc ràng buộc co giãn hợp lý, dẫn đến khi ghi chú hoặc tên ví dài, chữ bị ép xuống 3 dòng và dính sát sàn sạt vào cột số tiền (`-15.000 đ`) và icon ✏️/🗑️ bên phải. | 🟠 **MAJOR** | Thiết lập cột thông tin bên trái với `Modifier.weight(1f).padding(end = 10.dp)`. Tách số tiền và cụm icon hành động thành cột cố định riêng biệt bên phải với căn lề `Alignment.End`. |
| **03** | **Cụm 1** | **Thanh điều hướng đáy (`MainBottomBar`)** | `presentation/components/MainBottomBar.kt`<br>`Line: 35-85` | **Tất cả các Theme (Đặc biệt là Light Mode)** | **XUYÊN THẤU VÀ CHỒNG CHÉO TEXT KHI CUỘN DANH SÁCH:**<br>Do `MainBottomBar` dùng kính bán trong suốt nhưng không có lớp scrim/frost mờ bảo vệ phía sau, khi cuộn danh sách giao dịch hoặc trang chủ xuống dưới cùng, text của các item bên dưới chạy xuyên qua đáy thanh điều hướng, đè chồng lên nhãn chữ ("Trang chủ", "Lịch sử", "Báo cáo", "Hồ sơ"), gây rối mắt. | 🟡 **MINOR** | Thêm một lớp nền mờ gradient nhẹ (`Brush.verticalGradient(listOf(Color.Transparent, tokens.background.copy(alpha = 0.85f)))`) hoặc tăng độ mờ `surfaceSoft` cho thanh điều hướng đáy. |
| **04** | **Cụm 1** | **Bộ chọn danh mục (`CategoryPickerBottomSheet`)** | `core/designsystem/component/FinluxFormComponents.kt`<br>`Line: 350-410` | **Tất cả các Theme & Mode** | **CẮT CHỮ CỤT NGỦN TRONG LƯỚI 4 CỘT:**<br>Tên danh mục tiếng Việt (ví dụ: "Mua sắm Tik Tok", "Đầu tư khác", "Chi phí sinh hoạt", "Ăn vặt ở Cty", "Trả nợ & Tín dụng") bị cắt cụt ngủn thành `🛒 Mua sắm .`, `⚠️ Ăn vặt ở C.`, `🏆 Tích lũy &..` do cấu hình cứng `maxLines = 1`. | 🟡 **MINOR** | Cho phép nhãn danh mục hiển thị 2 dòng: `maxLines = 2`, `fontSize = 11.sp`, `lineHeight = 13.sp`, `textAlign = TextAlign.Center` để đọc trọn vẹn tên danh mục. |
| **05** | **Cụm 1** | **Sheet Chi tiết giao dịch (`TransactionDetailSheet`)** | `presentation/transaction/TransactionDetailSheet.kt`<br>`Line: 230-265` | **Tất cả các Theme & Mode** | **SUBTEXT NÚT HÀNH ĐỘNG BỊ DÍNH DẤU PHẨY LƠ LỬNG & THIẾU DARK THEME:**<br>1. Nút "Chỉnh sửa" có dòng phụ: "*Thay đổi số tiền, danh mục, ví,*" kết thúc bằng một dấu phẩy lơ lửng do chuỗi string template bị cắt ngắn.<br>2. Sheet hiển thị nền trắng trong Dark Mode thay vì nền kính tối sang trọng. | 🟡 **MINOR** | 1. Đổi subtext nút Chỉnh sửa thành: "*Chỉnh sửa thông tin giao dịch*".<br>2. Gán `containerColor = tokens.surface` cho `ModalBottomSheet` và các card con. |
| **06** | **Cụm 1 & 4** | **Sheet Chu kỳ lương (`SalaryCycleSheet`)** | `presentation/settings/SalaryCycleSheet.kt`<br>`Line: 160-220` | **Tất cả các Theme & Mode** | **DÍNH LIỀN TEXT TRONG BANNER XEM TRƯỚC DẢI CHU KỲ TÀI CHÍNH:**<br>Dải ngày chu kỳ hiện tại và chu kỳ tiếp theo bị in đè dính liền nhau thành một chuỗi duy nhất: `10/08/2026 - 09/09/202610/09/2026 - 09/10/2026` do thiếu khoảng cách hoặc cấu trúc Row không chia cột. | 🟡 **MINOR** | Tách banner xem trước thành 2 khối/hàng rõ ràng: "*Kỳ hiện tại: 10/08 - 09/09/2026*" và "*Kỳ tiếp theo: 10/09 - 09/10/2026*" hoặc dùng 2 Pill card độc lập. |
| **07** | **Cụm 1** | **Khối số dư Hero Trang chủ (`ReferenceBalanceHero`)** | `presentation/home/modern/ModernHomeScreen.kt`<br>`Line: 358-364` | **Modern Luxury & Classic** | **NGUY CƠ CO ÉP/TRÀN VIỀN VỚI SỐ DƯ > 11 CHỮ SỐ:**<br>Khi số dư tài sản đạt hàng chục/hàng trăm tỷ đồng (ví dụ: `120.500.000.000 đ`), font `headlineMedium` (28sp) cố định `maxLines = 1` không có cơ chế auto-scale có thể bị tràn viền hoặc che mất ký hiệu đơn vị tiền tệ trên màn hình nhỏ. | 🔵 **POLISH** | Bổ sung logic điều chỉnh cỡ chữ động: Nếu độ dài chuỗi tiền tệ > 14 ký tự, tự động giảm xuống `22.sp` hoặc dùng `TextOverflow.Ellipsis`. |
| **08** | **Cụm 2** | **Form Thêm/Sửa Ví (`WalletEditor`)** | `presentation/wallet/modern/ModernWalletsScreen.kt`<br>`Line: 473-477` | **Tất cả các Theme — Light Mode** | **MÀU VIỀN THẺ COLOR PALETTE BỊ HARDCODE TRẮNG:**<br>Trong bộ chọn màu nhận diện thẻ ví, viền của các ô màu chưa chọn được hardcode `Color.White.copy(alpha = 0.4f)`. Trong chế độ Light Mode (nền trắng/sáng), viền trắng này hoàn toàn biến mất hoặc bị lóa, làm các ô màu nhạt (như vàng, xanh nhạt) bị hòa lẫn vào nền sheet. | 🟠 **MAJOR** | Thay `Color.White.copy(alpha = 0.4f)` bằng `tokens.border.copy(alpha = 0.35f)` hoặc `MaterialTheme.colorScheme.outlineVariant` để hiển thị sắc nét trên cả Light Mode & Dark Mode. |
| **09** | **Cụm 2** | **Filter Chips Mẫu Ví & Loại Tài Khoản (`WalletEditor`)** | `presentation/wallet/modern/ModernWalletsScreen.kt`<br>`Line: 414-450` | **Tất cả các Theme & Mode** | **CẮT CỤT NHÃN FILTER CHIP:**<br>Hàng chip chọn mẫu ngân hàng và loại tài khoản bị cắt ngắn chữ khi cuộn ngang: `Tiền mặt & Tiê` (thay vì "Tiền mặt & Tiết kiệm"), `Thẻ tí` (thay vì "Thẻ tín dụng") do giới hạn chiều rộng và không bật wrap content hoặc padding không đều. | 🟡 **MINOR** | Cập nhật các Chip sử dụng `Modifier.wrapContentWidth()` kèm `PaddingValues(horizontal = 12.dp, vertical = 6.dp)` và không ép `widthIn` cố định quá hẹp. |
| **10** | **Cụm 2** | **Dialog Thêm/Sửa Ngân Sách (`ModernBudgetScreen`)** | `presentation/budget/modern/ModernBudgetScreen.kt`<br>`Line: 294-298` | **Tất cả các Theme & Mode** | **DÙNG SAI ICON TRONG KHỐI THÔNG TIN CẢNH BÁO:**<br>Khối "Cảnh Báo Vượt Ngưỡng Tự Động (80% - 100%)" đang sử dụng icon dấu cộng `Icons.Default.Add` (vốn là icon Thêm mới) thay vì icon Chuông báo động hoặc Cảnh báo an toàn, gây hiểu lầm là nút bấm thêm ngưỡng. | 🟡 **MINOR** | Đổi `Icons.Default.Add` thành `Icons.Default.NotificationsActive` hoặc `Icons.Default.Shield` hoặc `Icons.Default.WarningAmber` với tint `MaterialTheme.colorScheme.primary`. |
| **11** | **Cụm 2** | **Bộ chọn kỳ Ngân Sách (`ModernBudgetScreen`)** | `presentation/budget/modern/ModernBudgetScreen.kt`<br>`Line: 125-135` | **Tất cả các Theme & Mode** | **VẾT CẮT TRÒN TRÊN THANH CHỌN KỲ THÁNG:**<br>Thanh điều hướng kỳ tháng (`10/08 - 09/09`) ở trên cùng có hiệu ứng highlight tròn màu trắng cắt đè vào viền `GlassCard` xung quanh, làm mất tính liên tục và viền cong của capsule. | 🔵 **POLISH** | Làm phẳng và chuẩn hóa `GlassCard` bộ chọn kỳ thành một capsule đồng nhất với `GlassCapsule` hoặc `FinluxSoftCard`. |
| **12** | **Cụm 3** | **Màn hình Báo cáo (`ModernReportsScreen` & `ClassicReportsScreen`)** | `presentation/reports/modern/ModernReportsScreen.kt`<br>`Line: 164-170`<br>`ClassicReportsScreen.kt: 160-165` | **Tất cả các Theme & Mode** | **NÚT "XUẤT BÁO CÁO" BỊ ĐÈ KẸT PHÍA DƯỚI THANH BOTTOM BAR:**<br>Ở cuối màn hình Báo cáo, nút "Xuất báo cáo" (Export Report) nằm trong `Column` có `Spacer(Modifier.height(18.dp))`. Khi cuộn trang xuống hết cỡ, chiều cao ~80dp của `MainBottomBar` đè hoàn toàn lên nút này, khiến người dùng **không thể nhìn thấy trọn vẹn và không bấm được nút Xuất báo cáo**. | 🟠 **MAJOR** | Tăng chiều cao khoảng đệm đáy từ `Spacer(Modifier.height(18.dp))` lên `Spacer(Modifier.height(96.dp))` hoặc thêm `contentPadding = PaddingValues(bottom = 88.dp)` cho toàn bộ container cuộn. |
| **13** | **Cụm 3** | **Treemap Phân bổ chi tiêu (`ExpenseDistribution`)** | `presentation/reports/modern/ModernReportsScreen.kt`<br>`Line: 280-340` | **Tất cả các Theme & Mode** | **CẮT CỤT TÊN DANH MỤC TRONG KHỐI TREEMAP NHỎ:**<br>Khi danh mục chi tiêu chiếm tỷ trọng nhỏ (1% - 3%), các ô vuông Treemap nhỏ bị cắt cụt tên danh mục thành 1 chữ duy nhất: `🍴 Ăn` (cắt mất chữ "uống"), `Đóng` (cắt mất chữ "Wifi"). | 🟡 **MINOR** | Bổ sung logic hiển thị thông minh: Với ô treemap có diện tích nhỏ (< 80dp), chỉ hiển thị Emoji Icon + Tỷ lệ % (ví dụ: `🍴 2%`, `📶 1%`), khi chạm vào thì mở Tooltip chi tiết thay vì cố in nhãn text dài bị cắt cụt. |
| **14** | **Hệ thống** | **Toàn bộ BottomSheet & Dialog Kính (`ModernLiquidGlass.kt`)** | `core/designsystem/modern/ModernLiquidGlass.kt`<br>`Line: 538-570` | **Modern Luxury & Prism — Dark Mode** | **LỖI CỐT LÕI LÀM ÉP NỀN TRẮNG TRONG DARK MODE CHO TẤT CẢ SHEETS:**<br>`GlassBottomSheet` và `GlassDialogSurface` kiểm tra chế độ tối bằng công thức: `val dark = MaterialTheme.colorScheme.background.luminance() < 0.4f`. Khi theme sử dụng background tùy biến hoặc Prism token có luminance >= 0.4f, biểu thức trả về `false` và tự động gán `containerBg = Color(0xFFFFFFFF)` (màu trắng tinh) cho tất cả Dialog và BottomSheet (khiến toàn bộ form thêm/sửa ví, sửa ngân sách, chi tiết GD bị biến thành nền trắng toát giữa Dark Mode). | 🔴 **CRITICAL** | Loại bỏ hoàn toàn công thức `luminance() < 0.4f`. Đọc trực tiếp từ `LocalFinluxTokens.current.isDark` hoặc sử dụng `LocalFinluxTokens.current.surface` / `tokens.surfaceSoft` theo đúng 3 Nguyên Tắc Cốt Lõi của `AGENTS.md`. |

---

## 3. ĐÁNH GIÁ KIẾN TRÚC & NGUYÊN NHÂN GỐC RỄ (ROOT CAUSE ANALYSIS)

1. **Nguyên nhân gốc của lỗi Nền Trắng Dark Mode (STT 01, 05, 14)**:
   - File `ModernLiquidGlass.kt` (dòng 539 & 564) đang tự tính toán biến `dark` thông qua `luminance()` của background thay vì đọc trực tiếp cờ `LocalFinluxTokens.current.isDark` hoặc `tokens.surface`. Khi palette màu biến thiên, `luminance` vượt ngưỡng 0.4f khiến hệ thống hiểu nhầm là Light Mode và ép cứng `Color(0xFFFFFFFF)` cho toàn bộ Dialog và Sheet.
2. **Nguyên nhân gốc của lỗi Nút Bị Che Khuất & Xuyên Thấu (STT 03, 12)**:
   - `MainBottomBar` được thiết kế dạng Floating Docked (~80dp height) nhưng các màn hình con (`ModernReportsScreen`, `ModernTransactionsScreen`) lại chỉ sử dụng padding đáy thông thường (`16.dp` đến `18.dp`), dẫn đến các thành phần ở đáy màn hình luôn bị cản trở bởi BottomBar.
3. **Nguyên nhân gốc của lỗi Cắt Chữ Cụt Ngủn (STT 04, 09, 13)**:
   - Lạm dụng `maxLines = 1` mà không thiết lập `minLines`, `lineHeight` hoặc logic thu gọn thích ứng (adaptive representation: Icon + % cho ô nhỏ, 2 dòng cho grid).

---

## 4. MA TRẬN MỨC ĐỘ & KẾ HOẠCH BÀN GIAO SỬA ĐỒNG LOẠT

| Mức độ ưu tiên | Số lượng lỗi | Danh sách STT | Trạng thái thực thi |
|---|---|---|---|
| 🔴 **CRITICAL** | **2 lỗi** | **STT 01, STT 14** | 🟢 **100% ĐÃ SỬA DỨT ĐIỂM [DONE]** |
| 🟠 **MAJOR** | **3 lỗi** | **STT 02, STT 08, STT 12** | 🟢 **100% ĐÃ SỬA DỨT ĐIỂM [DONE]** |
| 🟡 **MINOR** | **7 lỗi** | **STT 03, 04, 05, 06, 09, 10, 13** | 🟢 **100% ĐÃ SỬA DỨT ĐIỂM [DONE]** |
| 🔵 **POLISH** | **2 lỗi** | **STT 07, STT 11** | 🟢 **100% ĐÃ SỬA DỨT ĐIỂM [DONE]** |

---

## 5. KẾT QUẢ NGHIỆM THU THỰC TẾ TRÊN THIẾT BỊ VẬT LÝ (VERIFICATION REPORT)

- **Unit Tests:** `./gradlew testDebugUnitTest` ➔ **100% PASS** (0 failed tests).
- **Build APK:** `./gradlew assembleDebug` ➔ **BUILD SUCCESSFUL**.
- **Cài đặt & Nghiệm thu trực tiếp trên Xiaomi / Redmi Note:**
  1. `ModernTransactionsScreen.kt`: Nền Liquid Glass tối sâu, thẻ GD tối chuẩn tokens, chữ tiêu đề ngày tương phản rõ ràng.
  2. Cột ghi chú giao dịch và cột số tiền: Đã tách độc lập `weight(1f)` và `Alignment.End`, không còn hiện tượng chèn ép chữ.
  3. `MainBottomBar`: Đã có Scrim gradient bảo vệ phía sau, ngăn chặn hoàn toàn hiện tượng xuyên thấu chữ khi cuộn.
  4. `CategoryPicker`: Nhãn danh mục hiển thị 2 dòng trọn vẹn, không còn bị cắt cụt dấu ba chấm.
  5. `TransactionDetailSheet`: Subtext chuẩn chỉnh, nền Dark Glass hoàn hảo.
  6. `SalaryCycleSheet`: Dải ngày chu kỳ được tách card riêng biệt, dễ đọc.
  7. `ModernHomeScreen` & `ClassicHomeScreen`: Số dư tự động co giãn font size thích ứng theo độ dài.
  8. `ModernWalletsScreen`: Bộ chọn màu thẻ có viền đa tầng sắc nét trên cả Light/Dark Mode.
  9. Bộ chọn mẫu ví: Wrap content linh hoạt, không còn bị cắt chữ.
  10. Khối Cảnh báo ngân sách: Sử dụng icon `NotificationsActive` trực quan, đúng nghiệp vụ.
  11. Capsule chọn kỳ ngân sách: Làm phẳng và liền mạch viền ngoài.
  12. `ReportsScreen`: Đáy màn hình đệm 120dp, nút "Xuất báo cáo" nổi cao hoàn toàn phía trên thanh điều hướng đáy.
  13. Treemap chi tiêu: Tự động chuyển đổi sang Icon + Tỷ lệ % cho các danh mục nhỏ.
  14. `ModernLiquidGlass.kt`: Đã xóa bỏ công thức `luminance() < 0.4f`, đọc trực tiếp `tokens.isDark`, giải quyết triệt để lỗi nền trắng Dark Mode.

---

*Tài liệu này được cập nhật tự động bởi AI Coding Agent và đã được đồng bộ 100% với hiện trạng thiết bị vật lý.*
