# BÁO CÁO KIỂM TOÁN UI/UX CHUYÊN SÂU — CỤM 1: CÁC MÀN HÌNH CỐT LÕI (CORE FLOWS)
**Dự án:** FinLux Android App (Personal Finance Management)  
**Công nghệ:** Kotlin, Jetpack Compose, Material 3, Liquid Glass Architecture  
**Ngày thực hiện kiểm toán:** 29/08/2026  
**Môi trường kiểm thử:** Thiết bị thật (Redmi Note kết nối ADB, Android 14, Display 1080x2400, Density ~2.75x)  
**Phiên bản gốc kiểm thử:** `v1.12.2` (Build 149)  
**Tài liệu tham chiếu:** `docs/CONTEXT.md`, `docs/BA_SPEC.md`, `docs/UI_SPEC.md`, `docs/DATA_SPEC.md`, `AGENTS.md`

---

## 1. TIẾN ĐỘ & DANH MỤC MÀN HÌNH ĐÃ / CHƯA QUÉT (PROGRESS TRACKER)

### 📌 Phân loại Cụm 1: Các màn hình cốt lõi (Core Flows)

| Tên Màn hình / Component | Đường dẫn File mã nguồn | Trạng thái quét | Ghi chú kiểm thử trên thiết bị |
|---|---|---|---|
| **Trang chủ Modern Luxury** | `app/src/main/java/com/finlux/app/presentation/home/modern/ModernHomeScreen.kt` | 🟢 **ĐÃ XONG** | Đã test cả Light Mode & Dark Mode, test số dư lớn, cuộn danh sách. |
| **Trang chủ Liquid Glass Classic** | `app/src/main/java/com/finlux/app/presentation/home/classic/ClassicHomeScreen.kt` | 🟢 **ĐÃ XONG** | Đã test cả Light Mode & Dark Mode, kiểm tra tính đồng bộ với Modern Luxury. |
| **Lịch sử GD Modern Luxury** | `app/src/main/java/com/finlux/app/presentation/transaction/modern/ModernTransactionsScreen.kt` | 🟢 **ĐÃ XONG** | Đã test Light/Dark Mode, lọc ngày/tháng, thao tác vuốt, nút sửa/xóa. Phát hiện lỗi nghiêm trọng Dark Mode. |
| **Lịch sử GD Classic** | `app/src/main/java/com/finlux/app/presentation/transaction/classic/ClassicTransactionsScreen.kt` | 🟢 **ĐÃ XONG** | Đã rà soát cấu trúc render thẻ và layout so sánh với Modern. |
| **Form Thêm/Sửa GD (Quick Add & Full Sheet)** | `app/src/main/java/com/finlux/app/presentation/transaction/AddTransactionSheet.kt` | 🟢 **ĐÃ XONG** | Đã test nhập số tiền lớn (`99.999.999.999 đ`), ghi chú dài, mở bộ chọn danh mục, bộ chọn ví, bàn phím IME. |
| **Sheet Chi tiết GD (Transaction Detail)** | `app/src/main/java/com/finlux/app/presentation/transaction/TransactionDetailSheet.kt` | 🟢 **ĐÃ XONG** | Đã test hiển thị chi tiết, Dialog xác nhận xóa, subtext nút hành động. |
| **Bộ chọn danh mục (Category Picker Modal)** | `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxFormComponents.kt` | 🟢 **ĐÃ XONG** | Đã test grid 4 cột, phát hiện lỗi cắt chữ cụt ngủn (`maxLines = 1`). |
| **Cụm 2: Ví & Ngân sách (Wallets, Budget)** | `app/src/main/java/com/finlux/app/presentation/wallet/*`, `budget/*` | 🟡 **CHƯA QUÉT (CỤM 2)** | Chờ thực hiện sau khi hoàn tất Cụm 1. |
| **Cụm 3: Báo cáo & Phân tích (Reports)** | `app/src/main/java/com/finlux/app/presentation/reports/*` | 🟡 **CHƯA QUÉT (CỤM 3)** | Chờ thực hiện sau khi hoàn tất Cụm 1 & 2. |
| **Cụm 4: Cài đặt, Nợ & Nhắc nhở (Settings, Debt, Reminders)** | `app/src/main/java/com/finlux/app/presentation/settings/*`, `debt/*`, `reminders/*` | 🟡 **CHƯA QUÉT (CỤM 4)** | Đã phát hiện sớm lỗi chu kỳ lương trong quá trình điều hướng. |

---

## 2. BẢNG CHI TIẾT TẤT CẢ LỖI & ĐIỂM BẤT THƯỜNG ĐÃ PHÁT HIỆN (CỤM 1)

| STT | Màn hình / Component | File Path & Dòng Code | Theme & Mode bị lỗi | Mô tả chi tiết hiện trạng & Lỗi thị giác | Mức độ | Đề xuất giải pháp sửa chữa cụ thể |
|---|---|---|---|---|---|---|
| **01** | **Lịch sử giao dịch (`ModernTransactionsScreen`)** | `app/src/main/java/com/finlux/app/presentation/transaction/modern/ModernTransactionsScreen.kt`<br>`Line: 160-205, 270-360` | **Modern Luxury & Classic — Dark Mode** | **NỀN TRẮNG BỊ LẠC QUẺ TRONG DARK MODE & MỜ CHỮ TIÊU ĐỀ NGÀY:**<br>Khi người dùng bật Dark Mode trong Cài đặt, màn hình Lịch sử giao dịch vẫn hiển thị nền trắng xóa và thẻ giao dịch màu trắng tinh.<br>Chữ tiêu đề nhóm ngày ("*Hôm nay (1)*", "*Hôm qua (2)*") có màu xám trắng (`onSurfaceVariant`) bị **chìm hoàn toàn/vô hình** trên nền trắng. | 🔴 **CRITICAL** | Cập nhật container `Scaffold` và `GlassCard` sử dụng `LocalFinluxTokens.current.surface` / `surfaceSoft` thay vì hardcode màu sáng. Bọc nền bằng `FinluxStyleBackdrop` đồng bộ như `ModernHomeScreen`. |
| **02** | **Thẻ giao dịch (`ModernTransactionsScreen` & `ClassicTransactionsScreen`)** | `app/src/main/java/com/finlux/app/presentation/transaction/modern/ModernTransactionsScreen.kt`<br>`Line: 310-360` | **Tất cả các Theme & Mode** | **XUNG ĐỘT BỐ CỤC TEXT VỚI CỘT SỐ TIỀN & ACTION ICONS:**<br>Cột nội dung bên trái (Ghi chú + Danh mục + Ví + Ngày giờ) không được bọc ràng buộc co giãn hợp lý, dẫn đến khi ghi chú hoặc tên ví dài, chữ bị ép xuống 3 dòng và dính sát sàn sạt vào cột số tiền (`-15.000 đ`) và icon ✏️/🗑️ bên phải. | 🟠 **MAJOR** | Thiết lập cột thông tin bên trái với `Modifier.weight(1f).padding(end = 10.dp)`. Tách số tiền và cụm icon hành động thành cột cố định riêng biệt bên phải với căn lề `Alignment.End`. |
| **03** | **Thanh điều hướng đáy (`MainBottomBar`)** | `app/src/main/java/com/finlux/app/presentation/components/MainBottomBar.kt`<br>`Line: 35-85`<br>`FinluxNavHost.kt: 220-235` | **Tất cả các Theme (Đặc biệt là Light Mode)** | **XUYÊN THẤU VÀ CHỒNG CHÉO TEXT KHI CUỘN DANH SÁCH:**<br>Do `MainBottomBar` dùng kính bán trong suốt nhưng không có lớp scrim/frost mờ phía sau, khi cuộn danh sách giao dịch hoặc trang chủ xuống dưới cùng, text của các item bên dưới chạy xuyên qua đáy thanh điều hướng, đè chồng lên nhãn chữ ("Trang chủ", "Lịch sử", "Báo cáo", "Hồ sơ"), gây rối mắt và khó đọc nhãn tab. | 🟡 **MINOR** | Thêm một lớp nền mờ gradient nhẹ (`Brush.verticalGradient(listOf(Color.Transparent, tokens.background.copy(alpha = 0.85f)))`) hoặc tăng độ mờ `surfaceSoft` cho thanh điều hướng đáy. |
| **04** | **Bộ chọn danh mục (`CategoryPickerBottomSheet`)** | `app/src/main/java/com/finlux/app/core/designsystem/component/FinluxFormComponents.kt`<br>`Line: 350-410` | **Tất cả các Theme & Mode** | **CẮT CHỮ CỤT NGỦN TRONG LƯỚI 4 CỘT:**<br>Tên danh mục tiếng Việt (ví dụ: "Mua sắm Tik Tok", "Đầu tư khác", "Chi phí sinh hoạt", "Ăn vặt ở Cty", "Trả nợ & Tín dụng") bị cắt cụt ngủn thành `🛒 Mua sắm .`, `⚠️ Ăn vặt ở C.`, `🏆 Tích lũy &..` do cấu hình cứng `maxLines = 1`. | 🟡 **MINOR** | Cho phép nhãn danh mục hiển thị 2 dòng: `maxLines = 2`, `fontSize = 11.sp`, `lineHeight = 13.sp`, `textAlign = TextAlign.Center` để đọc trọn vẹn tên danh mục. |
| **05** | **Sheet Chi tiết giao dịch (`TransactionDetailSheet`)** | `app/src/main/java/com/finlux/app/presentation/transaction/TransactionDetailSheet.kt`<br>`Line: 230-265` | **Tất cả các Theme & Mode** | **SUBTEXT NÚT HÀNH ĐỘNG BỊ DÍNH DẤU PHẨY LƠ LỬNG & THIẾU DARK THEME:**<br>1. Nút "Chỉnh sửa" có dòng phụ: "*Thay đổi số tiền, danh mục, ví,*" kết thúc bằng một dấu phẩy lơ lửng do chuỗi string template bị cắt ngắn.<br>2. Sheet hiển thị nền trắng trong Dark Mode thay vì nền kính tối sang trọng. | 🟡 **MINOR** | 1. Đổi subtext nút Chỉnh sửa thành: "*Chỉnh sửa thông tin giao dịch*".<br>2. Gán `containerColor = tokens.surface` cho `ModalBottomSheet` và các card con. |
| **06** | **Sheet Chu kỳ lương (`SalaryCycleSheet`)** | `app/src/main/java/com/finlux/app/presentation/settings/SalaryCycleSheet.kt`<br>`Line: 160-220` | **Tất cả các Theme & Mode** | **DÍNH LIỀN TEXT TRONG BANNER XEM TRƯỚC DẢI CHU KỲ TÀI CHÍNH:**<br>Dải ngày chu kỳ hiện tại và chu kỳ tiếp theo bị in đè dính liền nhau thành một chuỗi duy nhất: `10/08/2026 - 09/09/202610/09/2026 - 09/10/2026` do thiếu khoảng cách hoặc cấu trúc Row không chia cột. | 🟡 **MINOR** | Tách banner xem trước thành 2 khối/hàng rõ ràng: "*Kỳ hiện tại: 10/08 - 09/09/2026*" và "*Kỳ tiếp theo: 10/09 - 09/10/2026*" hoặc dùng 2 Pill card độc lập. |
| **07** | **Khối số dư Hero Trang chủ (`ReferenceBalanceHero`)** | `app/src/main/java/com/finlux/app/presentation/home/modern/ModernHomeScreen.kt`<br>`Line: 358-364` | **Modern Luxury & Classic** | **NGUY CƠ CO ÉP/TRÀN VIỀN VỚI SỐ DƯ > 11 CHỮ SỐ:**<br>Khi số dư tài sản đạt hàng chục/hàng trăm tỷ đồng (ví dụ: `120.500.000.000 đ`), font `headlineMedium` (28sp) cố định `maxLines = 1` không có cơ chế auto-scale có thể bị tràn viền hoặc che mất ký hiệu đơn vị tiền tệ trên màn hình nhỏ. | 🔵 **POLISH** | Bổ sung logic điều chỉnh cỡ chữ động: Nếu độ dài chuỗi tiền tệ > 14 ký tự, tự động giảm xuống `22.sp` hoặc dùng `TextOverflow.Ellipsis`. |

---

## 3. CÁC ĐẶC ĐIỂM BẤT HỢP LÝ CỦA HỆ THỐNG DESIGN SYSTEM ĐÃ THU THẬP ĐƯỢC

1. **Sự không nhất quán giữa màn hình Trang chủ và Màn hình Lịch sử**:
   - Trong khi `HomeScreen` đã được tích hợp `FinluxStyleBackdrop` và `MaterialTheme.colorScheme.background` linh hoạt theo theme, thì `ModernTransactionsScreen` và `ClassicTransactionsScreen` vẫn còn giữ các mã màu container tĩnh (`Color.White` hoặc alpha tĩnh) từ phiên bản cũ.
2. **ModalBottomSheet chưa tự động thừa hưởng Dark/Light Tokens**:
   - Các `ModalBottomSheet` (như `AddTransactionSheet`, `TransactionDetailSheet`, `SalaryCycleSheet`) khi khởi tạo ở tầng ngoài chưa luôn được bọc trong `CompositionLocalProvider(LocalFinluxTokens provides ...)` hoặc sử dụng `tokens.surface`, dẫn đến việc sheet luôn mặc định lấy nền sáng của hệ thống Compose Material3 nếu không set `containerColor`.
3. **Thanh điều hướng đáy (MainBottomBar) thiếu Scrim Protection**:
   - `Scaffold` toàn cục render `MainBottomBar` dạng trôi (floating/docked). Tuy nhiên, nội dung danh sách dài bên trong `NavHost` khi cuộn qua đáy thanh bar không có lớp chắn bảo vệ độ tương phản (Contrast Scrim), làm giảm trải nghiệm đọc nhãn tab.

---

## 4. KẾ HOẠCH HÀNH ĐỘNG TIẾP THEO (NEXT ACTIONS & ROADMAP)

### Bước 1: Xử lý triệt để các lỗi Cụm 1 (Core Flows)
1. **Sửa lỗi Dark Mode & Layout trên `ModernTransactionsScreen.kt` & `ClassicTransactionsScreen.kt`** (Lỗi 01 & 02).
2. **Cập nhật `TransactionDetailSheet.kt` & `AddTransactionSheet.kt`** đồng bộ Dark Mode và sửa text nút Chỉnh sửa (Lỗi 05).
3. **Sửa nhãn 2 dòng trong `CategoryPickerBottomSheet`** (Lỗi 04).
4. **Sửa dải ngày bị dính chữ trong `SalaryCycleSheet.kt`** (Lỗi 06).
5. **Thêm Scrim Protection cho `MainBottomBar.kt`** (Lỗi 03).
6. **Thêm auto-scale cho số dư lớn trong `ReferenceBalanceHero`** (Lỗi 07).

### Bước 2: Kiểm thử hồi quy Cụm 1 trên điện thoại thật
- Chạy `gradlew testDebugUnitTest`.
- Build & Cài đặt APK lên máy.
- Chụp ảnh màn hình kiểm thử từng màn hình sau khi sửa ở cả 2 Theme (Modern Luxury & Liquid Glass Classic) trên cả Dark Mode & Light Mode.

### Bước 3: Bàn giao và chuyển sang Kiểm toán Cụm 2 (Ví & Ngân sách)
- Sau khi Cụm 1 đạt 100% độ hoàn thiện visual không tì vết, tiến hành kiểm toán chuyên sâu Cụm 2 (`WalletsScreen`, `BudgetScreen`, `AddWalletDialog`, `AddBudgetDialog`).
