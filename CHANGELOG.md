# Changelog

## [1.14.0] - 2026-08-29
### Added
- **Chức năng Sao chép Ngân sách sang Kỳ Tiếp theo (Budget Period Rollover & Manual Copy)**:
  * Cho phép sao chép nhanh toàn bộ định mức hạn mức ngân sách từ kỳ hiện tại sang kỳ chi tiêu kế tiếp (hoặc sao chép từ kỳ trước sang kỳ hiện tại khi kỳ mới chưa có ngân sách).
  * Tích hợp nút sao chép chuyên dụng tại TopBar và Empty State trên cả 3 phong cách giao diện: `PrismBudgetScreen`, `ModernBudgetScreen`, `ClassicBudgetScreen`.
  * Hộp thoại xác nhận trực quan (`FinluxDialog`) hiển thị rõ số lượng danh mục và kỳ đích trước khi tiến hành sao chép.
  * Tự động khởi tạo lại số tiền đã chi (`spentAmount = 0đ`) và trạng thái cảnh báo hạn mức (`notified80 = false`, `notified100 = false`) cho kỳ mới.

### Fixed
- **Hỗ trợ Cuộn Dọc Khi Thêm/Sửa Ngân Sách Màn Hình Ngang (Landscape Modal Scroll Fix)**:
  * Bổ sung `Modifier.verticalScroll(rememberScrollState())` vào form Thêm/Sửa ngân sách trên cả 3 phong cách giao diện (`PrismBudgetScreen`, `ModernBudgetScreen`, `ClassicBudgetScreen`).
  * Khắc phục triệt để lỗi khi xoay màn hình ngang hoặc trên thiết bị màn hình thấp không thể cuộn xuống để nhấn nút "Lưu ngân sách".

### Changed
- **Nâng cấp Xử lý Kỳ Tài chính (Financial Period Resolver & Copy Use Case)**:
  * Mở rộng `FinancialPeriodResolver` hỗ trợ tính toán kỳ tiếp theo/kỳ trước (`resolveNextPeriod`, `resolveNextPeriodOf`, `resolvePreviousPeriodOf`) tương thích linh hoạt cho cả tháng dương lịch lẫn chu kỳ ngày nhận lương.
  * Xây dựng `CopyBudgetUseCase` độc lập tuân thủ Clean Architecture, kèm bộ unit test `CopyBudgetUseCaseTest.kt` kiểm thử 100% kịch bản (sao chép, bỏ qua danh mục đã có, hoặc ghi đè).

## [1.13.4] - 2026-08-29
### Fixed
- **Tự động Co Giãn An Toàn Tránh Phím Ảo Điều Hướng (Adaptive System Navigation Bar Insets)**:
  * Bổ sung cơ chế tự động co giãn vùng hiển thị `WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)` trên toàn bộ hệ thống (`FinluxNavHost`, `FinluxScreenScaffold`, `PrismHomeScreen`).
  * Khắc phục triệt để lỗi phím ảo (Back / Home / Recents) che khuất nội dung, badge % và các nút bấm bên cạnh phải khi xoay ngang hoặc trên các thiết bị có thanh điều hướng ảo.

## [1.13.3] - 2026-08-29
### Added
- **Kết nối Dữ liệu Ngân sách Thực tế vào Donut Carousel Trang Chủ (Real Budget Allocation)**:
  * Nâng cấp Trang số 4 trong Carousel phân tích trên Trang chủ FinLux Prism (`Tiến độ định mức ngân sách`) kết nối trực tiếp với dữ liệu ngân sách thực tế trong kỳ của người dùng.
  * Hiển thị Donut chart với tổng % ngân sách đã chi tiêu kèm cảnh báo màu thông minh (Xanh lá < 80%, Vàng cam 80-99%, Đỏ ≥ 100%).
  * Danh sách chi tiết thể hiện rõ: Tên danh mục, Số tiền đã chi tiêu / Hạn mức ngân sách, và % tiến độ.
  * Hỗ trợ Empty State trực quan khi chưa có ngân sách kèm nút điều hướng nhanh `[+ Thiết lập ngay ›]`.
  * Nút "Xem chi tiết ›" tại trang này tự động điều hướng trực tiếp sang màn hình Quản lý Ngân sách (`Route.Budget`).

## [1.13.2] - 2026-08-29
### Added
- **Lưu trữ Bền vững Trạng thái Ẩn/Hiện Số Dư (Persistent Balance Visibility)**:
  * Tích hợp `isBalanceVisible` vào hệ thống DataStore `UiPreferencesRepository` và `HomeViewModel`, giúp lưu nhớ vĩnh viễn trạng thái ẩn/hiện số dư qua các lần đóng mở app, chuyển màn hình hoặc đổi theme.

### Fixed
- **Khắc phục Lỗi Rò rỉ Dữ liệu Số Nợ khi Ẩn Số Dư (Debt Amount Masking Fix)**:
  * Che giấu toàn diện số tiền dư nợ `Nợ: ••••` trên thẻ Tổng quan Hero Prism khi người dùng kích hoạt chế độ ẩn số dư thay vì hiển thị số tiền thật.
  * Tự động che các số liệu thống kê trung bình và chênh lệch dòng tiền trên các thẻ Tổng quan khi ẩn số dư.

## [1.13.1] - 2026-08-29
### Added
- **Chế độ Trải nghiệm dùng thử tức thì (Demo Mode)**:
  * Bổ sung nút "⚡ Trải nghiệm ngay (Chế độ Dùng thử)" trên màn hình Đăng nhập giúp tester và người dùng trên máy ảo trải nghiệm đầy đủ tính năng ngay lập tức.
- **Hỗ trợ Máy ảo & Thiết bị không có Google Play Services**:
  * Bổ sung cơ chế bắt lỗi và hiển thị thông báo hướng dẫn thân thiện khi thiết bị thiếu Google Play Credential Provider thay vì crash hoặc văng lỗi kỹ thuật.

### Changed
- **Tối ưu hóa Luồng Khởi động Splash**:
  * Chuyển `SplashViewModel` sang `SharingStarted.Eagerly` và phát giá trị auth ban đầu đồng bộ trong `callbackFlow`, triệt tiêu hoàn toàn hiện tượng treo màn hình Splash.

### Fixed
- **Giải quyết toàn diện xung đột Remote Merge**:
  * Hợp nhất thành công thiết kế Hero Overview Data-First (`v1.13.0`) với bộ 14 bản vá lỗi UI/UX toàn diện (`v1.12.3`).
  * Chuẩn hóa chiều cao và padding Top Header trên `PrismHomeScreen`.

## [1.13.0] - 2026-08-29
### Added
- **Mini Bar Chart dữ liệu thật**: Tích hợp biểu đồ cột mini phân bổ giao dịch thực tế 5 mốc thời gian trong kỳ trên các thẻ Hero Tổng quan.
- **Họa tiết bảo mật watermark & texture chuyên biệt**: Mỗi thẻ sở hữu hình nền chuyển sắc độc lập kết hợp họa tiết Canvas chìm tinh xảo (Vòm bảo mật cho Ví, Cực quang tăng trưởng & chevrons cho Thu, Radar đo lường ngân sách cho Chi, Sóng điều hòa tuần hoàn kép cho Dòng tiền).
- **Named Morphing Capsule Indicator**: Chỉ báo trang dạng viên thuốc động hiển thị tên trang hiện tại (`[ Ví ] • • •`, `• [ Thu ] • •`, ...) hỗ trợ chạm chuyển trang trực tiếp.
- **Nút "Xem chi tiết ›"**: Lối tắt trực tiếp từ đáy thẻ dẫn đến trang danh sách chi tiết nghiệp vụ tương ứng.

### Changed
- **Chuẩn hóa Data-First ngân hàng số**: Tiêu đề theo chu kỳ lương linh hoạt ("Thu kỳ này", "Chi kỳ này", "Dòng tiền kỳ này", "Số dư hiện có") kèm khoảng ngày thực tế `dd/MM – dd/MM`.
- **Nâng cấp typography số tiền**: Cỡ chữ lớn lên tới `38sp`, in đậm `ExtraBold`, ký hiệu `₫` sắc nét, dễ đọc tức thì.
- **Dòng thông tin hữu ích**: Thay dòng mô tả lặp lại bằng số lượng khoản thu/chi thực tế và thống kê chi tiêu/thu nhập trung bình mỗi khoản.
- **Tối ưu chiều cao thẻ**: Thu gọn thẻ xuống `180dp` giúp bố cục trang chủ thoáng đãng và hiển thị nhiều giao dịch hơn.

### Fixed
- Khắc phục triệt để mũi tên chúc xuống gây hiểu lầm giảm tiền/chi tiền trên thẻ Thu nhập.
- Loại bỏ các hình minh họa trừu tượng chiếm diện tích và các chip trạng thái không mang lại giá trị thông tin.

## [1.12.3] - 2026-08-29
### Added
- **Hệ Thống Tự Động Co Giãn Font Số Dư Tài Sản (Auto-Scale Balance)**:
  * Tích hợp cơ chế tự động hạ kích thước chữ từ `28.sp` xuống `23.sp` hoặc `20.sp` cho các số dư tài sản lớn (trên 11 - 17 chữ số) trên cả `ModernHomeScreen` và `ClassicHomeScreen`.
- **Tối Ưu Hiển Thị Thông Minh Cho Ô Treemap Chi Tiêu Nhỏ**:
  * Tự động chuyển đổi sang Icon + Tỷ lệ % (`Icon` + `percent%`) khi diện tích ô danh mục nhỏ, tránh lỗi cắt cụt tên danh mục thành 1 ký tự.
- **Tách Khối Phân Đoạn Cho Dải Ngày Chu Kỳ Lương**:
  * Đóng gói dải ngày chu kỳ hiện tại và chu kỳ tiếp theo thành các pill card riêng biệt trong `SalaryCycleSettingsSheet`.

### Changed
- **Nâng Cấp Khoảng Đệm Đáy Toàn Diện Màn Hình Báo Cáo**:
  * Tăng khoảng đệm đáy lên `120.dp` trên `ModernReportsScreen` và `ClassicReportsScreen`, giải phóng hoàn toàn nút "Xuất báo cáo" nổi cao phía trên `MainBottomBar`.
- **Chuẩn Hóa Nhãn 2 Dòng Cho Bộ Chọn Danh Mục (`CategoryPicker`)**:
  * Cấu hình `maxLines = 2`, `lineHeight = 11.sp` trong `FinluxFormComponents` giúp tên danh mục dài tiếng Việt hiển thị đầy đủ, không bị cắt cụt.
- **Tái Cấu Trúc Bố Cục Thẻ Giao Dịch**:
  * Tách độc lập cột ghi chú/danh mục (`weight(1f)`) và cột số tiền + hành động (`Alignment.End`) trong `ModernTransactionsScreen` nhằm chống va chạm text khi ghi chú dài.
- **Thay Đổi Icon Cảnh Báo Ngân Sách**:
  * Thay icon dấu cộng `Add` bằng `Icons.Default.NotificationsActive` đúng nghiệp vụ cảnh báo vượt hạn mức 80% - 100%.

### Fixed
- **Khắc Phục Triệt Để Lỗi Ép Nền Trắng Dark Mode Cho Tất Cả Dialog & BottomSheet**:
  * Loại bỏ công thức `luminance() < 0.4f` trong `ModernLiquidGlass.kt`, chuyển sang đọc trực tiếp cờ `LocalFinluxTokens.current.isDark` và `tokens.surfaceSoft`.
- **Khắc Phục Lỗi Nền Trắng Màn Hình Lịch Sử Giao Dịch Trong Dark Mode**:
  * Bọc nền `ModernTransactionsScreen` bằng `FinluxStyleBackdrop` và chuẩn hóa màu text tiêu đề nhóm ngày với `tokens.onSurface` & `tokens.onSurfaceVariant`.
- **Sửa Lỗi Viền Màu Thẻ Ví Bị Chìm Trong Light Mode**:
  * Thay thế viền tĩnh `Color.White` bằng hệ thống viền 2 lớp tương thích động Light/Dark Mode trong `ModernWalletsScreen`.
- **Sửa Lỗi Subtext Nút Chỉnh Sửa Trong `TransactionDetailSheet`**:
  * Loại bỏ dấu phẩy thừa do chuỗi ví rỗng và áp dụng nền Dark Glass đồng bộ.

## [1.12.2] - 2026-08-29
### Added
- **Tự động đánh dấu đã đọc khi mở Hộp thư Thông báo (`markAllAsRead`)**:
  * Bổ sung API `markAllAsRead()` trong `NotificationRepository`, `FirebaseNotificationRepository` và `DemoFinluxRepository` để cập nhật batch toàn bộ thông báo chưa đọc sang `isRead = true`.
  * Thêm nút "Đánh dấu tất cả đã đọc" (`Icons.Default.DoneAll` và pill button dưới thanh lọc) trên màn hình Thông báo và chấm tròn chỉ báo trạng thái chưa đọc trực quan trên từng thẻ.

### Changed
- `NotificationsScreen`: Tự động kích hoạt `viewModel.markAllAsRead()` ngay khi người dùng mở màn hình Thông báo, giúp badge số lượng thông báo chưa đọc trên chuông Trang chủ lập tức được xóa sạch về trạng thái bình thường.
- `FinluxNavHost`: Nâng cấp `MainBottomBar` với `AnimatedVisibility` (fade + slide vertical transition).

### Fixed
- Sửa lỗi chuông thông báo trên Trang chủ (`HomeScreen` ở tất cả các theme: `FinLux Prism`, `Modern Luxury`, `Liquid Glass Classic`) bị hiển thị vĩnh viễn chấm đỏ do hardcode trong `ReferenceHeader`. Đã liên kết động 100% với `unreadNotificationsCount`.
- Khắc phục lỗi BottomBar xuất hiện sớm đè lên màn hình Splash lúc ứng dụng đang khởi động/chưa hoàn tất tải dữ liệu.

## [1.12.1] - 2026-08-29
### Added
- **Deterministic Idempotency Key cho Thông báo Nhắc nhở**:
  * Tự động sinh ID bản ghi thông báo định danh `reminder_${reminderId}_${triggerEpochDay}` khi báo thức nổ, bảo đảm Firestore tự động idempotent merge vào 1 document duy nhất.
  * Thêm Unit Tests bao phủ kịch bản Deduplication và Idempotent Payment trong `NotificationsViewModelTest.kt` và `AlarmReminderSchedulerTest.kt`.

### Changed
- **Nâng cấp `AlarmReminderScheduler.schedule` & `ReminderReceiver`**:
  * Tính toán mốc tương lai hợp lệ bằng `ReminderUtils.computeNextTriggerDate` khi `nextTriggerDate <= now`, chấm dứt hoàn toàn hiện tượng trigger lặp lại tức thì 1 giây sau.
  * Đưa `ReminderTriggerDeduplicator.shouldTrigger(id)` ra ngoài `onReceive()` đồng bộ (trước khi launch coroutine) để triệt tiêu race condition cấp OS.
- **Đồng bộ hóa Thanh toán Toàn diện (`NotificationsViewModel.kt`)**:
  * Khi thanh toán thành công, tự động gọi `markAsPaidByReminderId(notification.reminderId)` để đồng bộ toàn bộ các thông báo liên quan sang trạng thái `isPaid = true`.
  * Bổ sung bộ lọc Deduplication guard trong StateFlow `notifications` theo `(type, reminderId, epochDay)` để tự động làm sạch cả các bản ghi rác cũ còn lưu trong Firestore.

### Fixed
- Khắc phục triệt để lỗi nhân đôi thông báo nhắc nhở hóa đơn định kỳ trên `NotificationsScreen` và lỗi thanh toán 1 thẻ nhưng thẻ trùng lặp còn lại vẫn giữ nguyên nút thanh toán.
- Sửa lỗi màn hình Trang chủ Prism (`PrismHomeScreen`) bị trắng/không hiển thị nội dung thẻ KPI do xung đột layout giữa `Scaffold.topBar` lồng trong `NavHost`. Chuẩn hóa kiến trúc phẳng `Box` + `LazyColumn` với Top Header nằm ở item đầu tiên.

## [1.12.0] - 2026-08-28
### Added
- Component dùng chung `FinluxTransactionGroup` cho bố cục danh sách giao dịch kiểu nhóm menu Hồ sơ.
- Carousel tổng quan Home tự chuyển vòng Thu nhập → Chi tiêu → Dòng tiền sau mỗi 10 giây, có tab chọn nhanh và hỗ trợ vuốt trực tiếp.

### Changed
- Ba KPI nhỏ trên Home Prism được thay bằng một thẻ Liquid Glass REGULAR trọng tâm, số tiền lớn hơn, mô tả rõ hơn và tự đặt lại thời gian chờ sau thao tác tay.
- Header Home Prism chuyển thành capsule Liquid Glass CLEAR: avatar 48dp bên trái, lời chào/tên co giãn ở giữa và nút thông báo có badge số lượng bên phải.
- Trang chủ Prism gom tối đa 10 giao dịch gần nhất vào một thẻ bo góc lớn, dùng divider inset thay cho các card rời.
- Lịch sử Prism gom từng ngày vào một thẻ riêng; giữ header ngày, tổng ròng ngày, tap chi tiết và nhấn giữ thao tác.
- Màu/icon/surface/divider của nhóm giao dịch kế thừa hoàn toàn `LocalFinluxTokens` và semantic Thu/Chi/Chuyển tiền.

### Fixed
- Khắc phục cụm KPI ba cột khó đọc trên màn hình hẹp và danh sách giao dịch bị phân mảnh thành nhiều card rời.
- Header Home xử lý tên người dùng dài bằng ellipsis và badge thông báo nhiều hơn 9 mục bằng nhãn `9+`.

## [1.11.12] - 2026-08-28
### Added
- **Nâng cấp Thẻ 3 Cột Prism Liquid Glass (Thu tháng này, Chi tháng này, Dòng tiền ròng)**:
  * Tích hợp huy hiệu kính `PrismMetricMiniBadge` dạng Glass Squircle siêu nét với icon vector đồng màu cho từng loại chỉ số.
  * Tích hợp hiệu ứng nảy đàn hồi lò xo xúc giác (`Spring.StiffnessMediumLow`, scale 0.95f) khi chạm.
  * Đổ bóng màu phát quang mềm mại (`spotColor` xanh ngọc lục bảo, đỏ san hô, xanh sapphire) tạo độ nổi 3D tách biệt khỏi nền màn hình.

### Changed
- Bố cục 3 thẻ số liệu chuẩn hóa 3 tầng đối xứng hoàn hảo (Huy hiệu kính + Tiêu đề -> Số tiền lớn in đậm Black 18sp -> Pill xu hướng bo tròn căn giữa).
- Tăng kích thước số tiền lên 18sp (+24%), font weight Black, giãn cách ký tự và đổ bóng phát sáng màu giúp đọc số liệu tức thì.
- Chiều cao thẻ thu gọn tinh tế 104dp, viền kính siêu mảnh 0.8dp sắc sảo.

### Fixed
- Loại bỏ triệt để toàn bộ hình vẽ góc cụt, đồng xu vàng lạc quẻ và các khung viền/hộp xám không đồng bộ ở phiên bản trước.

## [1.11.11] - 2026-08-28
### Added
- **Gói hoàn thiện Master Plan P0/P1 (tranche 2026-08-28)**:
  * Lịch sử hỗ trợ search tiếng Việt không dấu theo ghi chú/danh mục/ví/số tiền, lọc kỳ tài chính hiện tại/kỳ trước, 30 ngày/3 tháng/6 tháng và khoảng tiền.
  * Thu nhập và Chi tiêu dùng chung `FinancialPeriod`; biểu đồ chi theo ngày có đường trung bình, mốc ngày thật và highlight ngày cao nhất.
  * Báo cáo có CTA thật cho Vay nợ/Mục tiêu/Ngân sách/Ví và insight dựa trên số liệu hiện tại so với kỳ trước.
  * Bổ sung unit test cho semantic chuyển tiền, tài sản, đóng góp mục tiêu và tìm kiếm/bộ lọc giao dịch.
- **Gói 1: Home Quick Actions Liquid Glass Frost Tiles & KPI Kỳ tài chính**:
  * Chuyển đổi 5 nút tính năng tròn (Chuyển khoản, Ngân sách, Quét mã QR, Báo cáo, Vay nợ) thành **Liquid Glass Frost Tiles** với viền sáng phản chiếu đa lớp `Brush.linearGradient`, nền kính khúc xạ `Brush.radialGradient`, ambient glow và độ tương phản cao trên cả theme sáng/tối.
  * Thêm hỗ trợ `salaryCycleLabel` trên `PrismSummaryTrioCard`: tự động đổi thành "Thu kỳ này", "Chi kỳ này", "Dòng tiền kỳ" khi bật chu kỳ lương.
- **Gói 2: Tái cấu trúc Report Navigation (4 Tabs tinh gọn)**:
  * Tái cấu trúc điều hướng Báo cáo Prism sang 4 tab chính cố định (`Tổng quan`, `Thu & Chi`, `Danh mục`, `Chuyên sâu`), loại bỏ thanh cuộn ngang 8 tab giúp thao tác một chạm nhanh chóng.
  * Tích hợp hàng secondary chips trong tab `Chuyên sâu` cho 5 phân hệ chi tiết: Vay nợ, Tiết kiệm, Ngân sách, Tài sản, Xu hướng.
  * Kết nối thông minh: click vào thẻ Tài sản ròng, Dư nợ, Ngân sách, Tiết kiệm trên tab Tổng quan tự động mở đúng tab con tương ứng.
- **Gói 3: Trực quan hóa Ngân sách & Dải màu cảnh báo rủi ro**:
  * Thêm nhãn pill tiến độ "Đã dùng X%" (hoặc "Vượt X%") trên từng danh mục ngân sách.
  * Áp dụng dải màu rủi ro động 4 cấp: Xanh lá (<=70%), Vàng hổ phách (70-90%), Cam đậm (90-100%), Đỏ cảnh báo (>100%).
  * Bổ sung đếm ngược số ngày còn lại trong kỳ ngân sách ("Còn X ngày trong kỳ" hoặc "Ngày cuối kỳ") trên thẻ Hero ngân sách.
- **Gói 4: Gom nhóm Lịch sử giao dịch theo mốc ngày**:
  * Tự động phân nhóm giao dịch theo mốc ngày ("Hôm nay", "Hôm qua", "dd/MM/yyyy").
  * Hiển thị header ngày với số lượng giao dịch và tổng thu/chi ròng trong ngày.

### Changed
- Vuốt giữa Trang chủ/Lịch sử/Báo cáo/Hồ sơ bám theo ngón tay, có edge resistance và spring trả vị trí; bottom navigation giữ cố định.
- Báo cáo Tiết kiệm tách rõ Dòng tiền còn lại, Tỷ lệ giữ lại thu nhập và Đã phân bổ vào mục tiêu trong kỳ.
- Tổng tài sản Home/Báo cáo chỉ cộng ví hoạt động không phải thẻ tín dụng; Net Worth tiếp tục trừ dư nợ.
- Chuẩn hóa màu sắc trong `FinluxFeedbackComponents.kt` tuân thủ 100% tokens theme động (`tokens.onHero`, `tokens.onSurface`), xóa bỏ hardcode `Color.White`.

### Fixed
- Cặp chuyển tiền nội bộ `_out`/`_in` được trình bày thành một dòng logic trên Home và Lịch sử, không còn cảm giác bị ghi nhận hai lần.
- Reset bộ lọc Lịch sử xóa cả query và khoảng tiền; badge phản ánh đầy đủ mọi điều kiện đang áp dụng.
- Khắc phục lỗi hiển thị nhãn kỳ tài chính không đồng bộ trên hàng KPI Trang chủ khi cấu hình chu kỳ lương tùy chỉnh.

## [1.11.10] - 2026-08-28
### Added & Enhanced (Payment Reminder Exact Alarm, Zero Time Drift & Multi-Device Sync)
- **Chống Doze Mode & Báo thức chính xác từng giây (Exact Alarm Engine)**:
  * Khai báo các quyền `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM`, `WAKE_LOCK` trong `AndroidManifest.xml`.
  * Nâng cấp `AlarmReminderScheduler` sang sử dụng `AlarmManager.setAlarmClock()` (vượt qua mọi rào cản Doze mode và Battery Optimization của Android 12+) kèm cơ chế fallback `setExactAndAllowWhileIdle()`.
  * Cấu hình Android Notification với mức ưu tiên tối đa (`PRIORITY_MAX`, `CATEGORY_REMINDER`, `VISIBILITY_PUBLIC`).
- **Xóa bỏ triệt để trôi giờ chu kỳ (Zero Time Drift Engine)**:
  * Xây dựng `ReminderUtils.computeNextTriggerDate` bảo toàn 100% mốc `LocalTime` (giờ, phút, giây) và ngày trong tháng gốc từ `startDate` của nhắc nhở qua mọi chu kỳ lặp lại (Hàng ngày, Hàng tuần, Hàng tháng).
  * Chuẩn hóa tính toán `triggerInstant` trong `RemindersScreen.kt` và `ReminderReceiver`.
- **Đồng bộ hóa đa thiết bị tự động (ReminderSyncObserver)**:
  * Xây dựng `ReminderSyncObserver` tự động lắng nghe `ReminderRepository.observeReminders()` khi mở app/đăng nhập, nạp toàn bộ danh sách nhắc nhở hợp lệ vào `AlarmManager` cục bộ và tự động tịnh tiến các nhắc nhở đã quá hạn.
  * Tích hợp `ReminderSyncObserver` vào `RootViewModel`.
- **Kiểm thử & Chất lượng**:
  * Viết mới `ReminderUtilsTest.kt`, `AlarmReminderSchedulerTest.kt`, `ReminderSyncObserverTest.kt`.
  * Toàn bộ Unit Tests đạt 100% PASS (208 tests passed, 0 failed).

## [1.11.9] - 2026-08-27
### Changed
- Logo ngân hàng/ví có ảnh thương hiệu dùng nền trắng semantic và viền trung tính ở cả theme sáng/tối; monogram và icon dự phòng vẫn dùng accent gradient.
- Bộ ba KPI Trang chủ Prism có cùng chiều cao, icon, nhãn, số tiền và trạng thái được căn giữa cân đối.
- KPI và thẻ phân tích Home Prism dùng primitive Liquid Glass REGULAR với chromatic rim, ambient glow và spring khi chạm.
- Số tiền KPI co cỡ chữ theo độ dài trên baseline cố định; năm lối tắt chia đều chiều rộng và tăng độ rõ của nhãn.
- Chú giải biểu đồ tách tên danh mục và số tiền thành hai dòng, tỷ trọng hiển thị trong badge riêng.

### Fixed
- Ẩn thẻ `Kỳ tài chính` khỏi Trang chủ; cách tính KPI theo chu kỳ đã cấu hình vẫn được giữ nguyên.
- Giảm chi tiết sparkle trên minh họa ví để không bị hiểu nhầm là nút thêm giao dịch.

## [1.11.8] - 2026-08-27
### Fixed & Enhanced (Financial Month / Salary Cycle Tech Debts & VietQR Offline Brand Catalog Integration)
- **Nợ kỹ thuật 1: Đồng bộ hóa 100% số liệu Trang chủ theo Kỳ tài chính (`HomeViewModel.kt`)**:
  * Chuyển đổi `financialOverviewFlow` sang `flatMapLatest` lắng nghe realtime cấu hình `SalaryCycleConfig`.
  * Khi `enabled == true`: Quan sát giao dịch theo cửa sổ chu kỳ `observePeriod(cycle.start, cycle.endExclusive)`, tính toán lại `DashboardSummary` (Tổng thu, Tổng chi, Dòng tiền ròng) và quan sát ngân sách `observeBudgets(period.key)` đồng bộ 100% với nhãn dải ngày hiển thị trên Badge.
  * Khi `enabled == false`: Giữ nguyên truy vấn theo tháng dương lịch hiện tại.
- **Nợ kỹ thuật 2: Tự động hóa Background Scheduler & Push Notification ngày lương**:
  * Xây dựng `SalaryCycleScheduler` và `AlarmSalaryCycleScheduler` sử dụng `AlarmManager.RTC_WAKEUP` định thời chính xác lúc 09:00 sáng ngày nhận lương theo múi giờ tài chính `Asia/Ho_Chi_Minh`.
  * Xây dựng `SalaryCycleReceiver`:
    - Bắn Push Notification & In-app Notification chào đón kỳ tài chính mới.
    - Tự động thực thi `ExecuteSalaryRolloverUseCase` kết chuyển tiền dư sang ví tích lũy nguyên tử nếu cấu hình `MOVE_TO_SAVINGS`.
    - Bắn Notification nhắc nhở nếu cấu hình `ASK_EACH_CYCLE`.
    - Tự động lên lịch kỳ nhận lương tháng tiếp theo.
  * Đăng ký receiver trong `AndroidManifest.xml` (`exported=true` kèm `intent-filter`) và khôi phục lịch báo trong `BootReceiver.kt`.
  * Tích hợp gọi `SalaryCycleScheduler` trong `SalaryCycleViewModel.kt` khi lưu cấu hình thành công.
- **Đồng bộ Catalog Thương hiệu & Logo VietQR Offline (Tích hợp từ upstream)**:
  * Bộ 65 logo tổ chức tài chính từ VietQR API, đóng gói offline trong `drawable-nodpi`.
  * Logo và preset cho Payoo, 9Pay, Foxpay, VTC Pay; giữ các preset ZaloPay, VNPAY, ShopeePay, Apple Pay và PayPal.
  * Mở rộng catalog dùng chung lên tối thiểu 75 ngân hàng/ví/tài khoản và áp dụng tự động cho các màn hình Ví Classic, Modern, Prism.
  * Tìm kiếm tổ chức hỗ trợ tên, mã ngân hàng, BIN và alias dài nhất; logo dùng màu gốc, viền/chữ kế thừa theme token.
  * Ngăn tên `Techcombank` bị nhận nhầm thành `MBBank` do alias ngắn `MB` xuất hiện bên trong chuỗi.
  * Script đồng bộ `tools/sync-financial-institution-icons.ps1` cùng manifest nguồn có ngày cập nhật.
- **Kiểm thử & Chất lượng**:
  * Cập nhật `HomeViewModelTest.kt` kiểm tra cả 2 kịch bản bật/tắt chu kỳ lương.
  * Viết mới `AlarmSalaryCycleSchedulerTest.kt` kiểm thử 100% logic tính toán mốc kích hoạt báo thức ngày lương.
  * Toàn bộ 34 task Unit Tests đạt 100% PASS.

## [1.11.7] - 2026-08-27
### Added & Changed (VietQR & Vietnam E-Wallets Offline Brand Catalog)
- Bộ 65 logo tổ chức tài chính từ VietQR API đóng gói offline trong `drawable-nodpi`.
- Mở rộng catalog dùng chung lên tối thiểu 75 ngân hàng/ví/tài khoản, hỗ trợ tìm kiếm theo tên, mã ngân hàng, BIN và alias.

## [1.11.6] - 2026-08-27
### Changed (Unified Standard Amount Input to ErgonomicCompactAmountCard)
- **Chuẩn hóa 100% các ô nhập tiền tệ sang `ErgonomicCompactAmountCard` trên toàn bộ ứng dụng**:
  * **Màn hình Thêm/Sửa Giao dịch chính (`AddTransactionSheet.kt`)**: Thay thế khối `Surface` Hero 38sp tự vẽ bằng `ErgonomicCompactAmountCard`, tích hợp màu sắc ngữ cảnh động (`ExpenseRed` / `IncomeGreen`) và dải chip gợi ý Decimal Magnitude Scaling.
  * **Hệ thống Quản lý Ví (`ModernWalletsScreen.kt`, `ClassicWalletsScreen.kt`, `PrismWalletsScreen.kt`)**: Thay thế triệt để các ô `OutlinedTextField` legacy và dải `FilterChip` thủ công (ở cả ô số dư ban đầu và modal chuyển tiền liên ví) sang `ErgonomicCompactAmountCard`.
  * **Mục tiêu Tài chính (`GoalsScreen.kt`)**: Nâng cấp 2 trường *"Mục tiêu cần đạt"* và *"Số tiền tích lũy mỗi tháng"* trong `GoalEditor` sang `ErgonomicCompactAmountCard`.
  * **Trung tâm Thông báo (`NotificationsScreen.kt`)**: Đồng bộ modal thanh toán nhanh sang `ErgonomicCompactAmountCard`.
- **Cập nhật tài liệu thiết kế (`FORM_COMPONENTS_SPEC.md`)**:
  * Đồng bộ danh sách kế thừa và quy chuẩn form input trên toàn hệ thống.

## [1.11.5] - 2026-08-27
### Enhanced (Smart Decimal Magnitude Scaling Amount Suggestions)
- **Nâng cấp thuật toán gợi ý tiền tệ thông minh (`generateAmountSuggestions` trong `FinluxFormComponents.kt`)**:
  * Chuyển đổi sang cơ chế **Decimal Magnitude Scaling**, tự động sinh dải gợi ý $V = N \times 10^k$ từ 1.000đ đến 1.000.000.000đ.
  * Phản ánh chính xác thói quen nhập tiền thực tế tại Việt Nam:
    - Ô rỗng/số 0: Danh sách 8 mốc mặc định chuẩn `[50k, 100k, 200k, 500k, 1M, 2M, 5M, 10M]`.
    - Gõ `"3"` $\rightarrow$ `[3.000, 30.000, 300.000, 3.000.000, 30.000.000]`.
    - Gõ `"35"` $\rightarrow$ `[3.500, 35.000, 350.000, 3.500.000, 35.000.000]` (bao gồm mốc x100 = 3.500).
    - Gõ `"356"` $\rightarrow$ `[3.560, 35.600, 356.000, 3.560.000, 35.600.000]` (bao gồm mốc x10 = 3.560, x100 = 35.600).
    - Gõ `"3568"` $\rightarrow$ `[35.680, 356.800, 3.568.000, 35.680.000]`.
- **Bổ sung bộ Unit Test tự động (`AmountSuggestionsTest.kt`)**:
  * Kiểm thử toàn diện 100% các kịch bản input từ rỗng, số lẻ, số hàng nghìn đến số lớn chạm ngưỡng 1 tỷ.

## [1.11.4] - 2026-08-27
### Fixed (Firestore Transaction Read/Write Order & Streamlined Deletion Flow)
- **Khắc phục triệt để lỗi không xóa được cặp chuyển tiền (`FirebaseTransactionRepository.kt`)**:
  * Sửa lỗi vi phạm quy tắc Firestore Transaction: Đưa 100% các lệnh đọc `atomic.get()` (ví nguồn, ví đích, bản ghi đối ứng `counterpartDoc`) lên trước toàn bộ các lệnh ghi `atomic.delete()` và `atomic.update()`, triệt tiêu lỗi runtime exception khiến Firestore rollback giao dịch.
- **Tối ưu luồng xác nhận xóa mượt mà (`TransactionDetailSheet.kt`)**:
  * Dọn sạch mã nguồn trùng lặp và loại bỏ việc hiện 2 lần hộp thoại xác nhận xóa.
  * Thao tác bấm nút xóa trên BottomSheet sẽ mở trực tiếp Hộp thoại xác nhận duy nhất với đầy đủ thông tin ví liên kết và hoàn tiền cả hai đầu.
- **Bổ sung Unit Test tự động (`FirebaseTransactionRepositoryTest.kt`)**:
  * Thêm test case `deleteWithBalanceUpdate cascades deletion of transfer pair and restores balances for both wallets` xác nhận toàn vẹn 100% logic xóa cặp.

## [1.11.3] - 2026-08-27
### Fixed (Cascade Atomic Transfer Deletion & CI Lint/PDF KPI Recovery)
- **Xóa đối ứng cả cặp giao dịch chuyển tiền nguyên tử (Cascade Atomic Deletion - `FirebaseTransactionRepository.kt` & `DemoFinluxRepository.kt`)**:
  * Khi xóa bất kỳ 1 trong 2 giao dịch chuyển tiền (`TRANSFER_OUT` hoặc `TRANSFER_IN`), hệ thống tự động tìm bản ghi đối ứng `_in` hoặc `_out` và thực hiện trong 1 Firestore Transaction duy nhất.
  * Tự động hoàn tác số dư cả 2 ví: Cộng lại ví nguồn (`+amount`), trừ thu hồi ví đích (`-amount`), và xóa sạch cả 2 bản ghi, triệt tiêu 100% rủi ro giao dịch mồ côi làm sai lệch tổng tài sản.
- **Khóa chỉnh sửa giao dịch chuyển tiền giữa các ví (Transfer Edit Lock)**:
  * Ẩn nút "Chỉnh sửa giao dịch" trong `TransactionDetailSheet.kt` khi xem chi tiết chuyển tiền và bổ sung thẻ thông báo bảo vệ dữ liệu 2 đầu ví.
  * Chặn gọi form sửa tại `FinluxNavHost.kt` và vô hiệu hóa các action vuốt sửa trên `PrismTransactionsScreen`, `ModernTransactionsScreen`, `ClassicTransactionsScreen`.
  * Khóa chặn cập nhật giao dịch chuyển tiền ở tầng repository (`updateWithBalanceAdjustment`).
- **Khắc phục lỗi CI Lint `UnusedMaterial3ScaffoldPaddingParameter` (`FinluxNavHost.kt`)**:
  * Thêm `@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")` do Root Scaffold sử dụng kiến trúc trong suốt tràn viền (edge-to-edge full viewport) có chủ đích bỏ qua inner padding.
- **Khôi phục hiển thị số tiền KPI Tổng Chi Tiêu trên PDF (`ReportExporter.kt`)**:
  * Khôi phục lệnh vẽ số tiền `-formatVndAmount(summary.expense.value)` trong ô KPI Tổng Chi Tiêu.
- **Dọn dẹp Git conflict marker (`HANDOVER_LOG.md`)**:
  * Loại bỏ dòng conflict marker còn sót lại từ commit merge upstream.

## [1.11.2] - 2026-08-27
### Fixed (Theme Synchronization & Zero-Config Theme Inheritance)
- **Khắc phục triệt để lỗi Header/TopBar bị biến thành màu trắng đục ở Dark Mode (`StyleBackdrop.kt`)**:
  * Sửa lỗi gốc rễ trong `DynamicGradientBackdrop`: Cung cấp dải màu Dark Mode sâu thẳm `listOf(Color(0xFF07101F), Color(0xFF10162B), Color(0xFF07111D))` khi `tokens.isDark == true`, loại bỏ 100% việc hardcode mã màu trắng `#FFFFFF` / `#F9FBFF`.
  * Đồng bộ toàn bộ 3 nhánh `VisualStyle` (`MODERN_DARK`, `GLASSMORPHISM`, `DYNAMIC_GRADIENT`) đều trả về nền Dark Mode thực thụ khi chuyển sang chế độ tối.
- **Thiết lập cơ chế "Zero-Config Theme Inheritance" (`FinluxScreenScaffold.kt`)**:
  * Tự động nhận diện `LocalAppUiStyle`: Khi ở `AppUiStyle.PRISM`, tự động sử dụng nền `tokens.background` (Dark `#0E1118` / Light `#F6F8FC`), khi ở `CLASSIC_LIQUID` hoặc `MODERN_LUXURY` tự động kích hoạt `FinluxStyleBackdrop`.
  * Gán `.background(tokens.background)` an toàn cho Box ngoài cùng, triệt tiêu 100% hiện tượng lộ nền trắng của Android Window khi ở Dark Mode.
  * Tự động inject `LocalContentColor provides tokens.textPrimary` cho toàn bộ các slots `topBar`, `bottomBar`, và `content`.
- **Chuẩn hóa màu chữ và icon TopBar (`LiquidGlass.kt`)**:
  * Cấu hình tường minh `titleContentColor`, `navigationIconContentColor`, và `actionIconContentColor` bằng `tokens.textPrimary` trong `GlassTopBar`.

## [1.11.1] - 2026-08-27
### Fixed (Hotfix LocalContentColor Invisibility in FinluxScreenScaffold)
- **Khắc phục triệt để lỗi chữ bị chìm màu / vô hình trên toàn bộ màn hình (`FinluxScreenScaffold.kt`)**:
  * Thay thế `contentColor = Color.Transparent` bằng `contentColor = tokens.textPrimary` (hoặc `tokens.onSurface`) trên Scaffold nội bộ.
  * Bọc nội dung bằng `CompositionLocalProvider(LocalContentColor provides tokens.textPrimary)` để đảm bảo chuỗi phân phối `LocalContentColor` của Jetpack Compose xuyên suốt toàn bộ cây Composable.
  * Bổ sung tiện ích `textPrimary` & `textSecondary` getters trong `FinluxDesignTokens` (`FinluxTokens.kt`).
- **Chuẩn hóa màu chữ trên 5 màn hình thí điểm (`CategoriesScreen`, `ExpenseScreen`, `IncomeScreen`, `DebtDashboardScreen`, `PrismHomeScreen`)**:
  * Thiết lập tường minh `color = tokens.onSurface` cho các tiêu đề Section và nội dung ghi chú giao dịch, bảo đảm độ tương phản và sắc nét 100% trên cả Dark Mode lẫn Light Mode.

## [1.11.0] - 2026-08-27
### Added (Design System Core & Base Screen Scaffolding Architecture)
- **Token Hóa Toàn Diện Khoảng Cách Semantic (`FinluxSpacing` trong `FinluxTokens.kt`)**:
  * Bổ sung `contentHorizontal = 16.dp` (khoảng đệm ngang LazyColumn nội dung), `screenTop = 8.dp` (khoảng cách trên danh sách), `bottomBarClearance = 96.dp` (khoảng trống đáy 4 Tab chính cuộn qua BottomBar), `compactClearance = 24.dp` (khoảng trống đáy các màn hình con/chi tiết), `itemGap = 8.dp`, và điều chỉnh `cardGap = 12.dp`.
  * Triệt tiêu 100% việc hardcode mã pixel/dp padding rải rác.
- **Khung Chuẩn Màn Hình Cơ Sở Duy Nhất (`FinluxScreenScaffold.kt`)**:
  * Cung cấp Slot API tiêu chuẩn: `topBar`, `bottomBar`, `floatingActionButton`, `fabPosition`, `snackbarHost`, `content`.
  * Tích hợp sẵn `FinluxStyleBackdrop` động thích ứng theo Theme với tham số bật/tắt `showBackdrop: Boolean = true` và `containerColor` cho các Theme đặc thù (như Prism Solid).
  * Khóa insets `contentWindowInsets = WindowInsets(0)` để đảm bảo `PaddingValues` trả về sạch 100%, không bị double statusBarsPadding.
- **Danh Sách Chuẩn Tự Động Tính Đệm (`FinluxLazyColumn.kt`)**:
  * Tự động áp dụng `contentPadding` chuẩn từ `FinluxSpacing` tokens dựa theo `listType`:
    + `FinluxListType.TAB_MAIN`: Đệm đáy `96.dp` để cuộn trôi qua BottomBar.
    + `FinluxListType.DETAIL`: Đệm đáy `24.dp` cho các màn hình chi tiết.
  * Tích hợp sẵn slot `emptyState` tự động render khi `isEmpty = true`, giảm boilerplate code tại các màn hình.

### Changed & Refactored (Pilot Screens Modernization)
- **Chuyển Đổi 5 Màn Hình Thí Điểm Sang Base Scaffolding Mới**:
  * `CategoriesScreen.kt`: Thay `Box + FinluxStyleBackdrop + Scaffold` bằng `FinluxScreenScaffold` + `FinluxLazyColumn(listType = DETAIL)`.
  * `ExpenseScreen.kt`: Thay `Box + FinluxStyleBackdrop + Scaffold` bằng `FinluxScreenScaffold` + `FinluxLazyColumn(listType = DETAIL)`.
  * `IncomeScreen.kt`: Thay `Box + FinluxStyleBackdrop + Scaffold` bằng `FinluxScreenScaffold` + `FinluxLazyColumn(listType = DETAIL)`.
  * `DebtDashboardScreen.kt`: Thay `Box + FinluxStyleBackdrop + Scaffold` bằng `FinluxScreenScaffold` + `FinluxLazyColumn(listType = DETAIL)` kết hợp `inner Box` cho loading overlay.
  * `PrismHomeScreen.kt`: Thay `Scaffold` bằng `FinluxScreenScaffold(showBackdrop = false, containerColor = tokens.background)` + `FinluxLazyColumn(listType = TAB_MAIN)`.

## [1.10.21] - 2026-08-27
### Fixed & Refactored (Transaction Row 3-Column Layout & Text Collision Fix)
- **Tái cấu trúc Chuẩn Layout 3 Cột cho Dòng Giao Dịch (`PrismHomeScreen`, `PrismTransactionsScreen`, `FinluxTransactionRow`)**:
  * **Cột 1 (Icon tròn 42dp-44dp)**: Icon danh mục / chuyển tiền với nền màu tương ứng.
  * **Cột 2 (`Modifier.weight(1f).padding(start = 12.dp, end = 8.dp)`)**:
    + Dòng 1: Tiêu đề giao dịch (`title`, `maxLines = 1`, `overflow = TextOverflow.Ellipsis`, `fontWeight = SemiBold`).
    + Dòng 2: Ngày tháng và Tuyến ví chuyển khoản (`"$dateText · $sourceWallet ➔ $targetWallet"` hoặc `"$dateText · $walletName"`, `maxLines = 1`, `overflow = TextOverflow.Ellipsis`, `fontSize = 12.sp`, màu `onSurfaceVariant`).
  * **Cột 3 (Số tiền - `wrapContentWidth`, căn phải `Alignment.End`)**:
    + Chỉ hiển thị duy nhất Chuỗi Số tiền (+/- Amount), font 15sp đậm, màu semantic (xanh cho Thu/Nhận, đỏ cho Chi/Chuyển).
    + Chuyển toàn bộ chuỗi tên ví dài ra khỏi cột bên phải, triệt tiêu 100% hiện tượng cột phải phình to đè lấn làm co cụm tiêu đề ("Chuyển ti...").
- **Tối ưu Row Giao Dịch tại `ExpenseScreen` & `IncomeScreen`**:
  * Chuyển `Column` tiêu đề sang `Modifier.weight(1f).padding(start = 12.dp, end = 8.dp)` kèm `verticalArrangement = Arrangement.spacedBy(2.dp)` giúp văn bản hiển thị thanh thoát và không bị ép chữ.

## [1.10.20] - 2026-08-27
### Fixed & Hardened (NavHost Full Viewport Edge-to-Edge & Insets Polish)
- **Gỡ bỏ Padding trên NavHost & Chuẩn hóa Viewport Edge-to-Edge (`FinluxNavHost.kt`)**:
  * Khai báo `NavHost(modifier = Modifier.fillMaxSize())` phủ kín 100% màn hình, gỡ bỏ hoàn toàn việc áp `scaffoldPadding` lên NavHost để triệt tiêu dứt điểm lỗi bóp nghẹt khung nhìn để lộ nền Window trắng ở đáy.
- **Tái tích hợp `.statusBarsPadding()` cho toàn bộ Header & TopBar**:
  * Đặt `.statusBarsPadding()` trên `FinluxScreenHeader`, `GlassTopBar` (`LiquidGlass.kt`, `ModernLiquidGlass.kt`), `PrismHomeTopHeader`, `ReferenceHeader`, `PrismReportsHeader`, `SettingsTitle` và Header Row tại `PrismTransactionsScreen.kt` để nội dung luôn bắt đầu an toàn dưới camera/tai thỏ trong khi nền kính `FinluxStyleBackdrop` trải dài Edge-to-Edge.
- **Chuẩn hóa Bottom Scroll Clearance chuẩn xác**:
  * 4 Tab chính có BottomBar (`Home`, `Transactions`, `Reports`, `Settings`): Đặt `contentPadding` đáy `bottom = 96.dp` để cuộn trôi hoàn toàn qua khỏi thanh điều hướng kính.
  * Toàn bộ màn hình con/chi tiết (`Expense`, `Income`, `DebtDashboard`, `Wallets`, `Budget`, `Categories`, `Goals`): Đặt `contentPadding` đáy `bottom = 24.dp` tự nhiên.
- **Triệt tiêu 100% mảng trắng rỗng & Đảm bảo Card co giãn tự nhiên**:
  * Rà soát `StrategySelectorCard.kt`, `DebtDashboardScreen.kt`, `ExpenseScreen.kt`, xóa sạch các khối nền trắng thừa, bảo đảm thẻ Chiến lược nợ và biểu đồ Burndown co giãn tự nhiên theo nội dung.

## [1.10.19] - 2026-08-27
### Fixed & Refactored (Root Scaffold & Insets Architecture Overhaul)
- **Chuẩn hóa Kiến trúc Root Scaffold & Insets (`FinluxNavHost.kt`)**:
  * Chuyển `scaffoldPadding` truyền trực tiếp vào `NavHost(Modifier.fillMaxSize().padding(scaffoldPadding))`, loại bỏ Box lồng trung gian và xử lý triệt để phân phối an toàn cho toàn bộ cây giao diện.
  * Thiết lập `containerColor = Color.Transparent` cho Root Scaffold để các hiệu ứng kính Liquid Glass hiển thị đồng nhất.
- **Triệt tiêu hoàn toàn lỗi Double Status Bars Insets (Đè Header & Hero Card)**:
  * Xóa bỏ `.statusBarsPadding()` thừa trong `FinluxScreenHeader.kt`, `GlassTopBar` (`LiquidGlass.kt`, `ModernLiquidGlass.kt`), `PrismHomeTopHeader` (`PrismHomeScreen.kt`), `PrismReportsHeader` (`PrismReportsScreen.kt`), `SettingsTitle` (`PrismSettingsScreen.kt`), `ReferenceHeader` (`ModernHomeScreen.kt`, `ClassicHomeScreen.kt`) và Header Row tại `PrismTransactionsScreen.kt`.
  * Top Bar và Greeting Header hiển thị thanh thoát, không còn bị xô lệch hay đè lên thẻ tổng tài sản.
- **Chuẩn hóa Bottom Scroll Clearance (16dp)**:
  * Loại bỏ toàn bộ khoảng đệm đáy cứng 140dp/120dp/40dp, đồng bộ `contentPadding` đáy của các LazyColumn trên toàn bộ màn hình (`PrismHomeScreen`, `PrismTransactionsScreen`, `PrismReportsScreen`, `PrismWalletsScreen`, `ModernHomeScreen`, `ClassicHomeScreen`, `ExpenseScreen`, `IncomeScreen`, `DebtDashboardScreen`, `PrismBudgetScreen`, `PrismSettingsScreen`) về chuẩn `PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)` (hoặc horizontal 20.dp).
  * Xóa sạch khoảng trắng khổng lồ cắt cụt ở chân trang.
- **Xóa mảng trắng rỗng cắt cụt & Chuẩn hóa Empty State**:
  * Nâng cấp `DebtDashboardScreen.kt`: Thay thế Box text trần bằng `FinluxEmptyState` bo góc hoàn chỉnh khi không có nợ, bảo đảm hiển thị trọn vẹn thẻ Chiến lược nợ (Slider + Burndown Chart).
  * Nâng cấp `ExpenseScreen.kt` và `IncomeScreen.kt` hiển thị `FinluxEmptyState` liền mạch.
- **Khắc phục triệt để lỗi co ép text trên các Row**:
  * Tối ưu layout Carousel danh mục (`PrismHomeScreen.kt` - `PrismBreakdownPageContent`): Sử dụng `Modifier.weight(1f, fill = false)` cho tên danh mục kèm `overflow = TextOverflow.Ellipsis` và compact amount format, giúp các chữ dài như "Ăn uống", "Phương tiện" hiển thị trọn vẹn, không bị bóp nghẹt.
  * Tối ưu text weights và ellipsis cho các dòng giao dịch tại `PrismHomeScreen`, `ExpenseScreen`, `IncomeScreen`.

## [1.10.18] - 2026-08-27
### Fixed & Hardened (Full UI/UX & Runtime Hardening Sprint)
- **Triệt tiêu hoàn toàn Bug Duplicate Stacked Bottom Bar (Critical)**:
  * Xóa bỏ triệt để các `bottomBar` lồng nhau trong Scaffold con tại `IncomeScreen`, `ExpenseScreen`, `ModernWalletsScreen`, `ClassicWalletsScreen`, `ModernBudgetScreen`, `ClassicBudgetScreen`.
  * Chuẩn hóa duy nhất Root NavHost (`FinluxNavHost.kt`) quản lý Bottom Navigation Bar cho 4 tab chính, các màn hình con có nút Back tự động ẩn thanh điều hướng đáy.
- **Xử lý Runtime & System Insets Toàn Diện**:
  * Bổ sung `.navigationBarsPadding()` và `.imePadding()` cho toàn bộ các ModalBottomSheet: `DebtPaymentSheet`, `AddEditDebtSheet`, `GoalDepositWithdrawSheet`, `SalaryCycleSettingsSheet`, `CategoriesScreen` (GlassBottomSheet).
  * Bổ sung `.statusBarsPadding()` cho Top Row và `.navigationBarsPadding()` cho Bottom Actions trên `ReceiptCaptureScreen.kt`, triệt tiêu lỗi nút chụp bị thanh điều hướng hệ thống che khuất.
- **Khắc phục triệt để State Leaks & Xung đột Runtime**:
  * Tích hợp `key(debt.id)` cho các state `remember` trong `DebtPaymentSheet.kt` chống rò rỉ số tiền thanh toán giữa các khoản nợ.
  * Bổ sung `TextOverflow.Ellipsis` và `maxLines` cho `DebtCard`, `FinluxTransactionRow` và `FinluxHeroCard` chống vỡ bố cục khi dữ liệu văn bản dài.
- **Đồng bộ Hệ thống Màu Động & Form Controls Chuẩn Liquid Glass**:
  * Nâng cấp `GoalsScreen.kt`, `PrismWalletsScreen.kt`, `SalaryCycleSettingsSheet.kt` sang `ErgonomicCompactAmountCard` đồng bộ chuẩn 100%.
  * Nâng cấp `CategoriesScreen.kt` bọc `FinluxStyleBackdrop`, bổ sung `FinluxEmptyState`, căn giữa chữ FilterChips và chuyển Dialog cũ sang `GlassBottomSheet`.
  * Loại bỏ toàn bộ mã màu tĩnh hardcode trong `TransactionDetailSheet.kt`, `TransactionFilterBottomSheet.kt`, `PrismTransactionsScreen.kt`, `PrismReportsScreen.kt`, `ClassicMainBottomBar.kt` và `FinluxFormComponents.kt` chuyển sang sử dụng `LocalFinluxTokens.current` và `FinluxColors`.
  * Set `containerColor = Color.Transparent` cho Scaffold tại các màn hình con để nền kính `FinluxStyleBackdrop` hiển thị đồng nhất.

## [1.10.17] - 2026-08-27
### Enhanced
- **Tối ưu trải nghiệm gợi ý số tiền ErgonomicCompactAmountCard (`FinluxFormComponents.kt`)**:
  * Tự động ẩn dải gợi ý nhân nhanh số tiền (Quick Suggestion Chips) ở trạng thái bình thường để form nhập liệu luôn siêu gọn gàng, tiết kiệm không gian màn hình.
  * Tích hợp hiệu ứng `AnimatedVisibility` (fade & expand) tự động mở rộng dải chip gợi ý khi người dùng tap/focus vào ô nhập số tiền.
  * Tự động làm sáng viền thẻ (`amountColor`) khi ô nhập đang được active focus.
  * Đồng bộ đặc tả chi tiết trong tài liệu quy chuẩn `docs/FORM_COMPONENTS_SPEC.md`.

## [1.10.16] - 2026-08-27
### Added
- **Khôi phục Reminder Schedule Chip trên thẻ nợ (`DebtCard.kt`)**: Hiển thị ngày nhận thông báo nhắc nợ định kỳ 09:00 sáng trước hạn thanh toán trực tiếp trên thẻ nợ.
- **Tự động xóa Reminder khi tất toán nợ (`ProcessDebtPaymentUseCase.kt`)**: Tích hợp `SyncDebtReminderUseCase` vào quy trình thanh toán nợ, tự động xóa lịch nhắc nhở trong `AlarmManager` & `ReminderRepository` ngay khi khoản nợ được tất toán thành công (`isSettled = true`).
- **Nâng cấp Form Nợ & Thanh Toán sang Control Ergonomic Chuẩn (`AddEditDebtSheet.kt`, `DebtPaymentSheet.kt`)**:
  * Kế thừa `ErgonomicCompactAmountCard` cho Hạn mức/Khoản vay gốc, Dư nợ hiện tại, Trả tối thiểu hàng tháng và Tổng số tiền thanh toán nợ.
  * Hỗ trợ tự động phân tách hàng nghìn VNĐ, bộ 3 chip gợi ý nhanh (+000, +00.000, +000.000) và 100% màu động theo `LocalFinluxTokens.current`.

### Fixed
- **Sửa thuật toán tính ngày nhắc nhở nợ (`SyncDebtReminderUseCase.kt`)**: Khắc phục lỗi ép cứng 28 ngày khiến ngày đến hạn 31 bị đặt nhầm vào ngày 27; hỗ trợ chuẩn xác các tháng 28, 29, 30 và 31 ngày.

## [1.10.15] - 2026-08-26
### Added & Enhanced
- **Tích Hợp & Nâng Cấp Báo Cáo PDF Chuẩn Sao Kê Ngân Hàng A4 (`ReportExporter.kt`)**:
  - **Bảng Sao Kê Tài Chính Tabular Layout**: Header Slate `#F1F5F9` bo góc 4pt, viền mảnh `0.5pt` `#E2E8F0`, phân chia nền Zebra Striping `#F8FAFC` và `#FFFFFF` xen kẽ.
  - **Phân Bổ 4 Cột Dữ Liệu Tối Ưu**:
    * Cột 1 (Thời gian): Hiển thị ngày và giờ chính xác `dd/MM/yyyy HH:mm` theo múi giờ hệ thống.
    * Cột 2 (Danh mục & Ghi chú): Bố cục 2 dòng trong 1 ô (Tên danh mục bold `#1E293B` + Ghi chú font nhỏ `#64748B` có `smartEllipsize` chống tràn chữ).
    * Cột 3 (Ví thanh toán): Căn trái kèm hàm `smartEllipsize` max 88pt chống lỗi cắt cụt chữ.
    * Cột 4 (Số tiền): Căn lề phải (Align.RIGHT) khớp lề bảng, màu xanh `#16A34A` cho Thu và đỏ `#DC2626` cho Chi với hàm `formatVndAmount`.
  - **Tối Ưu Phân Bổ Thanh Tiến Độ "Cơ Cấu Chi Tiêu Theo Danh Mục"**:
    * Tách biệt độc lập tọa độ Y dòng Text và thanh Progress Bar (cao 5pt bo góc 2.5pt), triệt tiêu hoàn toàn hiện tượng thanh bar đè lên chân chữ.
    * Tự động nhận diện và vẽ màu sắc chủ đạo theo danh mục (`parseColorHex`).
  - **Summary KPI Card**: Bo góc 8pt với viền mảnh 0.8pt, hiển thị Tổng Thu Nhập, Tổng Chi Tiêu, và Thu Ròng (Dư/Thâm hụt).
  - **Tự Động Phân Trang Đa Trang (Multi-page Pagination)**: Tự động ngắt trang khi vượt quá chiều cao A4 và vẽ lại Header bảng trên trang tiếp theo.
- **Tối Ưu Hóa Cấu Hình Build & CI Pipeline**:
  - Nâng cấp JVM args trong `gradle.properties`: `-Xmx4g -XX:MaxMetaspaceSize=1g`.
  - Đảm bảo 100% Unit Tests (`testDebugUnitTest`) và Android Lint (`lintDebug`) vượt qua tất cả kiểm thử trên CI GitHub Actions.
- **Đồng Bộ Tài Liệu Đặc Tả Quy Chuẩn**:
  - Bổ sung `UC-17: Xuất báo cáo tài chính Excel / PDF` và quy tắc nghiệp vụ `BR-11` vào `docs/BA_SPEC.md`.
  - Bổ sung `SCREEN 19: Bản In Báo Cáo PDF Chuẩn Sao Kê Tài Chính` vào `docs/UI_SPEC.md`.

## [1.10.14] - 2026-08-26
### Added
- **Bộ nhận diện Ngân hàng & Ví điện tử Việt Nam (35+ Tổ chức Tài chính)**:
  - Vector drawables & brand colors cho các ngân hàng: Vietcombank, Techcombank, MB Bank, ACB, VPBank, BIDV, VietinBank, TPBank...
  - Vector drawables cho ví điện tử phổ biến: MoMo, ZaloPay, Viettel Money, VNPay, ShopeePay, PayPal...
  - Bộ mẫu Tiền mặt, Sổ tiết kiệm, Thẻ tín dụng, Tài sản đầu tư, Tiền mã hóa (Crypto).
  - Thanh chọn nhanh mẫu tổ chức tài chính (`InstitutionSelectorSection`) với bộ lọc danh mục và 1-tap autofill thông minh (tên ví, loại ví, màu sắc chủ đạo).
  - Dialog tra cứu toàn diện 35+ ngân hàng và ví điện tử kèm công cụ tìm kiếm tức thì.
  - Composable `FinancialInstitutionLogo` tự động nhận diện tên ví để hiển thị logo vector chính hãng hoặc monogram dập nổi Liquid Glass cao cấp.
- **Tích hợp đồng bộ hệ sinh thái ví**:
  - Áp dụng trên toàn bộ 3 phong cách giao diện: Prism, Classic, Modern.
  - Áp dụng vào Modal Bottom Sheet chọn ví giao dịch dùng chung (`FinluxWalletPickerBottomSheet`).

## [1.10.13] - 2026-08-26
### Added
- **Hệ thống Báo cáo Tài chính Đa Chiều Toàn Diện (8 Chuyên Mục Chuyên Sâu)**:
  - **Báo cáo Vay & Nợ (Debts & Loans)**: Theo dõi tổng dư nợ gốc, tổng nợ ban đầu, tổng tiền đã trả, tiền lãi & gốc đã thanh toán trong kỳ, và tiến độ hoàn trả từng khoản nợ.
  - **Báo cáo Tiết kiệm & Tích lũy (Savings & Goals)**: Tỷ lệ tiết kiệm thực tế (`(Thu - Chi)/Thu`), tổng tài sản đã tích lũy vào các mục tiêu, tiến độ hoàn thành các mục tiêu tài chính.
  - **Báo cáo Ngân sách (Budgets)**: Tỷ lệ sử dụng hạn mức ngân sách, danh mục an toàn, cảnh báo vàng và cảnh báo đỏ vượt hạn mức.
  - **Báo cáo Tài sản & Ví (Wallets & Net Worth)**: Tính toán Tài sản ròng (Net Worth = Tổng số dư ví - Tổng dư nợ), phân bổ cơ cấu tài sản theo loại ví (Tiền mặt, Ngân hàng, Tiết kiệm, Thẻ tín dụng, Đầu tư), dòng tiền thu/chi theo ví.
  - **Báo cáo Thu & Chi & Danh mục**: Donut chart cơ cấu chi tiêu và nguồn thu nhập chi tiết.

### Fixed & Changed
- **Cố định Bottom Navigation Bar khi vuốt chuyển trang**:
  - Đưa `MainBottomBar` ra ngoài `Scaffold` gốc trong `FinluxNavHost.kt`.
  - Khi người dùng vuốt qua lại giữa Trang chủ, Giao dịch, Báo cáo, Cài đặt, thanh điều hướng đáy hoàn toàn đứng yên cố định, chỉ có nội dung trang trượt ngang mượt mà.
- **Sửa công thức tính trung bình thu/chi mỗi ngày**:
  - Tính dựa trên số ngày thực tế đã trôi qua trong kỳ (đến ngày hiện tại) thay vì chia cho cả 30-31 ngày trong tương lai làm giảm sai lệch số liệu.

## [1.10.12] - 2026-08-26
### Added
- **Modal Bộ Lọc Giao Dịch Đa Chiều (`TransactionFilterBottomSheet`)**:
  - Lọc theo kỳ thời gian linh hoạt: Tất cả, Tuần này, Tháng này, Tháng trước, Năm nay.
  - Lọc theo từng Ví tài chính hoặc toàn bộ ví.
  - Lọc theo từng Danh mục chi tiêu/thu nhập hoặc toàn bộ danh mục.
  - Hiển thị Badge số lượng bộ lọc đang kích hoạt trên nút Bộ lọc TopBar.

### Changed
- **Nâng cấp `versionCode = 124` và `versionName = "1.10.12"`.**
- **Thay thế "Tổng giá trị giao dịch" bằng "Dòng tiền ròng (Net Cash Flow)"**:
  - Tính toán và hiển thị chính xác Dòng tiền ròng (Thu nhập - Chi tiêu) với màu sắc trực quan (+/-).
  - Bổ sung thống kê chi tiết phụ: `Thu: +X • Chi: -Y` và tổng số giao dịch hiển thị, đồng bộ trên cả 3 giao diện `Prism`, `Classic`, `Modern`.
- **Tối ưu trải nghiệm Form Nhập Liệu Toàn Diện**:
  - Mở rộng kích thước số tiền (38sp, ₫ 32sp) và phím tắt nhanh trên mọi form (`FinluxAmountInputCard`).
  - Đưa ô Ghi chú lên ngay dưới phần Số tiền ở tất cả các luồng: Thêm/Sửa giao dịch (`AddTransactionSheet`), Thanh toán nợ (`DebtPaymentSheet`), và Chuyển tiền giữa các ví (`PrismWalletsScreen`).

## [1.10.11] - 2026-08-26
### Changed
- **Tối ưu trải nghiệm Form Thêm/Sửa Giao Dịch**:
  - Tăng kích thước vùng nhập số tiền (Font size 38sp, ₫ 32sp) và các phím tắt nhanh (+10k, +50k, +100k, +500k) to rõ, dễ quan sát và nhập liệu thuận tiện.
  - Chuyển ô **Ghi chú giao dịch** lên ngay bên dưới phần Số tiền để người dùng dễ dàng điền lý do/nội dung thanh toán tức thì.
  - Nâng cấp kích thước và padding ô nhập liệu `ErgonomicInputRow` thoáng đãng, dễ thao tác.
## [1.10.10] - 2026-08-26
### Changed
- **Nâng cấp `versionCode = 122` và `versionName = "1.10.10"`.**
- **Chuẩn Hóa Cử Chỉ Vuốt Trái Điều Hướng Màn Hình (Swipe-to-Navigate)**:
  - Cảnh báo ngân sách (`BUDGET_ALERT`): Kéo sang trái ➡️ Tự động điều hướng sang màn hình Ngân sách (`budget`) (hiển thị action nền đỏ/tím + icon `ArrowForward` + "Xem ngân sách").
  - Cột mốc mục tiêu (`GOAL_MILESTONE`): Kéo sang trái ➡️ Tự động điều hướng sang màn hình Mục tiêu (`goals`) (nền vàng cam + "Xem mục tiêu").
  - Báo cáo tài chính (`TRANSACTION_SUMMARY`): Kéo sang trái ➡️ Tự động điều hướng sang màn hình Báo cáo (`reports`) (nền xanh ngọc + "Xem báo cáo").
  - Hạn nợ / Thẻ tín dụng (`DEBT_DUE_ALERT`): Kéo sang trái ➡️ Tự động điều hướng sang màn hình Quản lý nợ (`debts`) (nền tím + "Quản lý nợ").
  - Nhắc hóa đơn & Khác: Kéo sang trái ➡️ Xóa thông báo khỏi danh sách (nền đỏ + icon `Delete` + "Xóa").
- **Tối Ưu Độ Nhạy Cử Chỉ Vuốt**: Đặt `positionalThreshold = 30%` giúp vuốt nhẹ là kích hoạt mượt mà, sau khi điều hướng thì thẻ tự động snap lại vị trí ban đầu.
- **Tách Biệt Hành Vi Chạm Thẻ**: Chạm vào thân thẻ chỉ đánh dấu đã đọc (`markAsRead`), mở modal thanh toán hóa đơn (`QuickPayBottomSheet`) hoặc chi tiết thanh toán (`PaidNotificationDetailSheet`), không tự ý nhảy màn hình khi chỉ chạm nhẹ.

## [1.10.9] - 2026-08-26
### Added
- **Cử Chỉ Vuốt Xóa Thông Báo (`SwipeToDismissBox`)**: Hỗ trợ vuốt thẻ thông báo từ phải sang trái để xóa tức thì với hiệu ứng nền đỏ và icon `Delete` thùng rác.
- **Modal Chi Tiết Thanh Toán Hóa Đơn (`PaidNotificationDetailSheet`)**: Khi chạm vào thẻ nhắc nhở đã thanh toán (`isPaid = true`), mở BottomSheet xem chi tiết số tiền, ví nguồn, danh mục, thời gian và ghi chú Sổ cái đã ghi.
- **Xóa Từng Thông Báo (`deleteNotification`)**: Bổ sung API xóa thông báo đơn lẻ trong `NotificationRepository`, `FirebaseNotificationRepository` và `NotificationsViewModel`.

### Changed
- **Nâng cấp `versionCode = 121` và `versionName = "1.10.9"`.**
- **Điều Hướng Thông Minh Khi Chạm Thẻ**: Chạm vào thẻ tự động đánh dấu đã đọc (`markAsRead`), đồng thời phân luồng hành động chuẩn xác:
  - Nhắc hóa đơn chưa thanh toán ➡️ Mở modal `QuickPayBottomSheet` (Thanh toán ngay).
  - Nhắc hóa đơn đã thanh toán ➡️ Mở modal `PaidNotificationDetailSheet` (Xem chi tiết).
  - Cảnh báo ngân sách ➡️ Điều hướng sang màn hình Ngân sách (`budget`).
  - Cột mốc mục tiêu ➡️ Điều hướng sang màn hình Mục tiêu (`goals`).
  - Báo cáo ➡️ Điều hướng sang màn hình Báo cáo (`reports`).
  - Hạn nợ / Thẻ ➡️ Điều hướng sang màn hình Quản lý nợ (`debts`).

## [1.10.8] - 2026-08-26
### Added
- **Bộ chọn Giờ/Phút (`TimePicker`) cho Nhắc nhở định kỳ**: Tích hợp chọn giờ nhắc cụ thể (ví dụ 09:00, 20:00) kết hợp chính xác Ngày + Giờ theo múi giờ hệ thống `ZoneId.systemDefault()`, ngăn chặn lệch giờ thông báo.
- **Tự Động Đồng Bộ & Lên Lịch Nhắc Hạn Nợ (`SyncDebtReminderUseCase`)**: Tự động liên kết các khoản nợ & thẻ tín dụng có bật nhắc nhở vào `AlarmReminderScheduler` và `ReminderRepository`. Thông báo sẽ tự động gửi vào lúc 09:00 sáng trước ngày đến hạn theo cấu hình.
- **Banner Mô Tả Lịch Nhắc Nợ**: Hiển thị trực quan ngày giờ cụ thể hệ thống sẽ gửi thông báo trong `AddEditDebtSheet.kt`.

### Changed
- **Nâng cấp `versionCode = 120` và `versionName = "1.10.8"`.**
- **Chuẩn Hóa 3 Form Control Tiêu Chuẩn Trong `RemindersScreen.kt`**:
  - `FinluxCategoryPickerBottomSheet`: Grid 4 cột có thanh tìm kiếm, icon màu động và checkmark chọn.
  - `FinluxWalletPickerBottomSheet`: Dạng danh sách bo góc với số dư khả dụng và checkmark.
  - `ErgonomicCompactAmountCard`: Typography 16sp Bold, tự format VNĐ thời gian thực, dải chip gợi ý `.000` thông minh và inline `₫` suffix.
  - `ErgonomicInputRow`: Tên nhắc nhở với icon `NotificationsActive` và nút xóa nhanh.
- **Đồng Bộ Ghi Chú Giao Dịch Khi Nhấn "Đã Thanh Toán"**: Thống nhất định dạng ghi chú trên cả thanh thông báo hệ thống Android (`AlarmReminderScheduler.kt`) và In-App Notification Center thành `"Thanh toán: " + [Tên nhắc nhở]`.

## [1.10.7] - 2026-08-26
### Fixed
- **Đồng bộ Key Ngân sách (`budgetRef`)**: Sửa format document ID trong `FirebaseTransactionRepository.kt` thành `"${catId}_month:${month}"` khớp 100% với Cloud Functions `reconcileBudget`, đảm bảo `spentAmount` được cập nhật chính xác trong Firestore Atomic Transaction.
- **Khôi phục Hộp thư Thông báo (`Notification Center Sync`)**: Khôi phục lưu `AppNotification` trong `AlarmReminderScheduler.kt` (`ReminderReceiver`) khi báo thức kích hoạt, giải quyết triệt để lỗi màn hình `NotificationsScreen` bị trống sau commit `431abd7`.
- **Cập nhật Báo thức Kế tiếp**: Cập nhật `nextTriggerDate` vào Database ngay khi báo thức reo để duy trì tính toàn vẹn của lịch nhắc nhở.
- **Tối ưu hóa Google Sign-In Credential Manager**: Loại bỏ xung đột cấu hình `signInWithGoogleOption` và sử dụng thuần túy `GetGoogleIdOption` với `setFilterByAuthorizedAccounts(false)` để hiển thị đầy đủ danh sách tất cả tài khoản Google trên thiết bị người dùng.
- **Thời gian chờ & Thông báo**: Mở rộng timeout kết nối Google lên 25s và làm rõ thông báo lỗi khi thiết bị chưa có tài khoản.

### Added
- **Cảnh báo Ngân sách Tức thì (Local Budget Alert)**: Tích hợp cơ chế kiểm tra ngân sách ngay trong `AddTransactionUseCase.kt`. Tự động bắn thông báo hệ thống (Status Bar Notification) và ghi `AppNotification` (loại `BUDGET_ALERT`) khi chi tiêu đạt 80% (cảnh báo) hoặc 100% (vượt hạn mức).
- **Hệ thống Quản lý Notification Channels (`SystemNotificationHelper`)**: Khởi tạo và quản lý 3 kênh thông báo riêng biệt (`finlux_budget_alerts`, `finlux_reminders_v2`, `finlux_system_notifications`).
- **Foreground FCM Push Handler**: Bổ sung `onMessageReceived` trong `FinluxMessagingService.kt` để hiển thị thông báo hệ thống ngay cả khi ứng dụng đang mở.

## [1.10.6] - 2026-08-26
### Fixed
- **Bảo mật & Xác thực Google Sign-In**: Cấu hình Release Keystore chính thức cho luồng CI/CD Release trên GitHub Actions, đăng ký SHA-1 và SHA-256 fingerprint đồng bộ với Firebase Console & Google Cloud Console.
- **An toàn CI Release Fallback**: Sửa fallback trong `.github/workflows/release.yml` để sử dụng `debug.keystore` chuẩn thay vì sinh keystore ngẫu nhiên trên máy ảo runner.
- **Dọn dẹp môi trường**: Khắc phục lỗi cú pháp `.gitignore` cho thư mục `jdk/`.

## [1.10.5] - 2026-08-25
### Changed
- **Nâng cấp `versionCode = 117` và `versionName = "1.10.5"`.
- **Đồng bộ giao diện màn hình Thu nhập (`IncomeScreen`)**: Chuyển đổi toàn bộ thẻ (MonthPicker, IncomeHero, 4 Statistic Cards, Theo danh mục, Danh sách thu nhập) sang chuẩn `FinluxPanel` với đường viền mỏng và đổ bóng (border & shadow elevation 5dp) đồng bộ hoàn hảo với màn hình Chi tiêu (`ExpenseScreen`).
- **Nâng cấp `IncomeViewModel`**: Hỗ trợ tính toán tỷ lệ tăng/giảm thu nhập theo tháng (`changePercent`), biểu đồ phân bổ theo ngày (`dailyStats`), và mở rộng phạm vi nạp giao dịch lên 5,000 bản ghi.

## [1.10.4] - 2026-08-25
### Changed
- **Nâng cấp `versionCode = 116` và `versionName = "1.10.4"`.
- Sửa lỗi mapping ngân sách trong `BudgetViewModel` và đồng bộ unit test `testDebugUnitTest` 100% PASS.
- Cập nhật cách mock `FinancialPeriodResolver` và xử lý state loading cho `BudgetUiState`.

## [1.10.3] - 2026-08-25
### Added
- **Hardening CI/CD & Firebase Rules**: Tích hợp Firebase Emulator Tests kiểm thử Firestore rules tự động trên GitHub Actions (bảo vệ ghi balance & salaryRollovers).

### Changed
- **Nâng cấp `versionCode = 115` và `versionName = "1.10.3"`.
- **Payment Action Idempotency**: Đảm bảo thanh toán nhắc nhở (Reminder) là nguyên tử (Atomic) và ngăn chặn bấm "Đã thanh toán" nhiều lần sinh ra giao dịch trùng lặp qua `paymentActionId`.
- Loại bỏ tính toán và lưu `nextTriggerDate` trên Android `AlarmReminderScheduler` để nhường Cloud Function làm Canonical Owner (Single Source of Truth).

## [1.10.2] - 2026-08-25
### Added
- **Hardening Sprint P0 (Bảo Mật & Tính Toàn Vẹn Dữ Liệu Tài Chính)**:
  - `AdjustWalletBalanceUseCase`: Chuyển toàn bộ thao tác điều chỉnh số dư thành giao dịch ghi Sổ Cái (Ledger transaction) kèm audit trail.
  - `FinancialPeriodResolver`: Động cơ giải quyết kỳ tài chính thống nhất (`CALENDAR_MONTH` & `SALARY_CYCLE`) bao phủ toàn diện các boundary payday (28..31, năm nhuận).
  - `ExecuteSalaryRolloverUseCase`: Thực thi chuyển tiền tích lũy cuối kỳ lương (`MOVE_TO_SAVINGS`) với cơ chế Idempotent chống duplicate transaction.
  - `ReminderTriggerDeduplicator`: Chống trùng lặp notification nhắc nhở trong các cửa sổ thời gian ngắn.

### Changed
- Nâng cấp `versionCode = 114` và `versionName = "1.10.2"`.
- Chuyển thao tác xóa ví có lịch sử giao dịch sang Soft Delete (Lưu trữ / Archived) để bảo toàn dữ liệu lịch sử.
- Tách biệt hoàn toàn metadata ví khỏi state số dư tài chính, ngăn chặn ghi đè trực tiếp.

### Fixed
- **Chặn ví âm ở cấp độ Atomic Transaction**: Thực thi kiểm tra bất biến số dư non-CARD `>= 0` ngay trong `runTransaction` Firestore và Repository Mutex.
- **Fail-Closed Release Signing & OTA Updater**: Loại bỏ hoàn toàn keystore base64 / fallback password khỏi CI/CD pipeline, tăng cường bảo mật xác minh chứng chỉ OTA.
- **Action "Đã thanh toán" trên Notification**: Bắt buộc kiểm tra `AppResult.Success` từ `addTransactionUseCase` trước khi cập nhật trạng thái đã thanh toán.
- Đạt 100% PASS cho toàn bộ 170 Unit Test suites.

## [1.10.1] - 2026-08-25
### Added
- **Hợp Nhất Bản Phát Hành Hoàn Chỉnh Release v1.10.1**:
  - Tích hợp Tháng Tài Chính & Chu Kỳ Lương (Salary Cycle & Financial Month) với Live Preview, tùy biến ngày nhận lương, liên kết ví, mức lương dự kiến và báo cáo theo chu kỳ.
  - Tích hợp Lịch Sử Thanh Toán Nợ Chi Tiết (`DebtPaymentHistorySheet` + `GetDebtPaymentHistoryUseCase`) với phân loại giảm gốc và lãi vay.
  - Tích hợp Cài Đặt Nhắc Nợ Đến Hạn (`Due Date Reminder`) trong `AddEditDebtSheet` với lựa chọn nhắc trước 1, 2, 3, 5 ngày.
  - Tích hợp Bảo Vệ Số Dư Ví (`Insufficient Balance Protection`) cảnh báo và chặn ghi chi tiêu khi số dư không đủ.

### Changed
- Nâng cấp `versionCode = 113` và `versionName = "1.10.1"`.
- Đồng bộ toàn diện hệ thống tài liệu đặc tả BA, DATA, UI và Handover Log.

### Fixed
- Sửa triệt để lỗi Ghost Alarm trong `AlarmReminderScheduler` (kiểm tra trạng thái enabled và hủy sạch PendingIntent khi xóa).
- Đảm bảo 100% Unit Test suites vượt qua kiểm thử thành công trước khi đóng gói Release.

## [1.10.0] - 2026-08-24
### Added
- **Tính năng Tháng Tài Chính & Chu Kỳ Lương (Salary Cycle & Financial Month)**:
  - Tùy biến chu kỳ tài chính theo ngày nhận lương thực tế (ví dụ: ngày 25 hàng tháng) thay vì tháng dương lịch cố định.
  - Thuật toán `SalaryCycleCalculator` tính dải ngày chính xác `[start, endExclusive)` và tự động xử lý các tháng ngắn ngày (28, 29, 30 ngày) cùng năm nhuận.
  - Giao diện `SalaryCycleSettingsSheet` chuẩn Liquid Glass & Prism với Live Preview dải ngày chu kỳ hiện tại / kế tiếp, bộ chọn ngày nhanh & Slider, chọn ví nhận lương, mức lương dự kiến, quy tắc tiền dư cuối kỳ và căn cứ kỳ ngân sách.
  - Tích hợp Trang chủ (`PrismHomeScreen`): Thẻ Hero hiển thị badge dải ngày chu kỳ tài chính trực quan.
  - Tích hợp Báo cáo (`ReportsViewModel`, `PrismReportsScreen`): Bổ sung `ReportPeriod.SALARY_CYCLE` truy vấn theo dải giao dịch thực tế `TransactionRangeRepository`.
  - Tầng dữ liệu Firestore: Subcollection `users/{uid}/financialPreferences/salaryCycle` kèm bảo mật Firestore Rules.

### Changed
- Nâng cấp `versionCode = 112` và `versionName = "1.10.0"`.
- Cập nhật toàn bộ các tài liệu đặc tả hệ thống: `BA_SPEC.md` (`UC-27`, `BR-SALARY-01..03`), `DATA_SPEC.md`, `UI_SPEC.md`, `HANDOVER_LOG.md`.

### Fixed
- Đảm bảo 100% Unit Tests (136/136 tests) vượt qua kiểm thử thành công trước khi đóng gói Release.

## [1.9.3] - 2026-08-24
### Added
- **Lịch Sử Thanh Toán Nợ Chi Tiết (`DebtPaymentHistorySheet`)**:
  - Thống kê toàn diện Tổng tiền đã trả (xanh lá), Đã giảm gốc (Finlux Blue) và Tiền lãi đã trả (đỏ cam).
  - Hỗ trợ bộ lọc linh hoạt theo từng khoản nợ hoặc xem toàn bộ danh sách.
  - Tích hợp nút xem Lịch sử trên Header Quản lý nợ (`DebtDashboardScreen`) và trực tiếp trên từng thẻ nợ (`DebtCard`).
  - UseCase mới: `GetDebtPaymentHistoryUseCase` kết nối đồng bộ giữa Domain, Firestore và Demo Repository.
- **Cài Đặt Nhắc Nợ Đến Hạn (Due Date Reminder Settings)**:
  - Bổ sung switch bật/tắt nhắc nợ kèm dải nút chọn nhanh số ngày nhắc trước (*Trước 1, 2, 3, 5 ngày*) trong `AddEditDebtSheet`.
  - Mở rộng model `DebtAccount` với `isReminderEnabled` & `reminderDaysBefore`, bổ sung loại thông báo `NotificationType.DEBT_DUE_ALERT`.
- Đóng gói và phát hành bản Release v1.9.3 chuẩn hóa với Proguard/R8 shrinking.

### Changed
- **Bảo Vệ Số Dư Ví Khi Chi Tiêu (Insufficient Balance Protection)**:
  - Chặn tạo/sửa giao dịch chi tiêu (`EXPENSE`) khi số dư ví thanh toán `<= 0` hoặc không đủ tiền (áp dụng cho các ví không phải Thẻ tín dụng `WalletType.CARD`).
  - Hiển thị banner cảnh báo đỏ `⚠️ Số dư ví không đủ` và vô hiệu hóa nút Lưu trong `AddTransactionSheet`.
- Nâng cấp `versionCode = 111` và `versionName = "1.9.3"`.

### Fixed
- **Sửa Triệt Để Lỗi Ghost Alarm / Nhắc Nhở Đã Xóa Vẫn Tự Bắn Thông Báo**:
  - Bổ sung Validation Guard trong `ReminderReceiver`: Luôn kiểm tra sự tồn tại và trạng thái `enabled` của nhắc nhở trong database trước khi hiển thị thông báo, trước khi ghi Firestore và trước khi lên lịch tiếp theo.
  - Hủy sạch `PendingIntent`, `AlarmManager` và notification treo khi người dùng xóa nhắc nhở (`AlarmReminderScheduler.cancel()`), chấm dứt hoàn toàn vòng lặp báo thức mồ côi.
- Đảm bảo 100% Unit Test suites vượt qua kiểm thử trước khi đóng gói Release.

## [1.9.2] - 2026-08-24
### Added
- **Trợ Lý Phân Bổ Dòng Tiền Thoát Nợ Tự Động (Debt Cashflow Advisor & Smart Allocation)**:
  - Tích hợp `AnalyzeDebtCashflowUseCase`: Tự động phân tích lịch sử thu chi 3 tháng trượt, tính toán Dòng tiền tự do (Free Cash Flow - FCF) và lãi suất bình quân gia quyền (Weighted APR).
  - Tự động sinh 3 kịch bản phân bổ dòng tiền trả nợ thông minh (*Thư thái 30% FCF*, *Cân bằng 60% FCF*, *Thần tốc 85% FCF*) kèm tính năng 1-Touch Apply áp dụng tức thì.
  - Phân loại danh mục thiết yếu (`isEssential: Boolean` - Needs vs Wants) hỗ trợ phân tích sức khỏe tài chính.
- **Thẻ Hero Trang Chủ Dạng Carousel (HorizontalPager)**:
  - Mặc định hiển thị "Số dư hiện có" theo tổng tiền các ví khả dụng thực tế, không trừ nợ làm âm số dư tài khoản.
  - Hỗ trợ vuốt sang trái xem "Tài sản ròng (Net Worth)" và nợ kèm 2 dots Page Indicator tinh tế.
- **Đồng Bộ Giao Diện Thêm/Sửa Ví Chuẩn Liquid Glass Cổ Điển**:
  - Nâng cấp `PrismWalletEditor` với đầy đủ tính năng: Header icon động theo loại ví và màu thẻ, dải chọn đầy đủ loại ví, live format VND, dải chip cộng tiền nhanh, chọn 8 màu thẻ, switch Đặt làm ví mặc định và quản lý xóa ví an toàn.

### Changed
- **Tái Cấu Trúc UI/UX Bento Grid Cho Module Quản Lý Nợ**:
  - Hợp nhất Tab Chiến lược, Trợ lý AI, Slider trả thêm và Biểu đồ Burndown Chart vào 1 khối Bento Payoff Container liền mạch, giảm triệt để tình trạng scroll fatigue.
  - Thiết kế lại `DebtCard` chuẩn Fintech hiện đại: Chuyển nút `[Trả nợ]` thành Glass Action Button nhỏ gọn ở góc phải Header, thanh tiến độ mỏng 4.5dp animated, loại bỏ FAB (+) che khuất danh sách.

### Fixed
- Sửa lỗi ép rớt dòng chữ dọc trên nhãn APR bằng Horizontal Badge gọn gàng.
- Sửa nhãn trend % của Chi tháng này, Thu tháng này và Dòng tiền hiển thị trung tính `— 0%` khi chưa phát sinh giao dịch.
- Sửa triệt để lỗi cướp cử chỉ vuốt trong `FinluxNavHost` (chuyển sang `PointerEventPass.Main` và kiểm tra `isConsumed`), giúp vuốt thẻ Hero và các chart không bị nhảy sang màn hình Lịch sử thu chi.

## [1.9.1] - 2026-08-24
### Added
- Thêm bộ chọn giờ (TimePicker) song song với bộ chọn ngày khi thêm/sửa giao dịch, cho phép ghi nhận chính xác mốc thời gian phát sinh giao dịch.

### Changed
- Tự động reset trạng thái form tạo giao dịch mới (`AddTransactionSheet`), ngăn chặn lưu vết dữ liệu từ giao dịch trước đó.
- Tối ưu hóa biểu đồ phân bổ chi tiêu và tỷ trọng thu chi trên Trang chủ Prism.

### Fixed
- Sửa lỗi giữ nguyên dữ liệu giao dịch cũ khi mở form thêm giao dịch mới.

## [1.9.0] - 2026-08-22
### Added
- **Module Quản Lý & Thoát Nợ (Debt Freedom & Credit Hub - UC-26)**:
  - Quản lý 4 loại công nợ: Thẻ tín dụng (Credit Card), Vay ngân hàng (Bank Loan), Vay cá nhân (Personal Loan), Mua trả góp (Installment).
  - Thuật toán mô phỏng thoát nợ tự động theo 2 chiến lược kinh điển: **Debt Snowball** (Cầu tuyết - nợ nhỏ trước) và **Debt Avalanche** (Lở tuyết - lãi cao trước).
  - Biểu đồ mô phỏng lộ trình giảm dư nợ theo thời gian (Burndown Chart) và tính toán số tháng dự kiến sạch nợ cùng số tiền lãi tiết kiệm được.
  - Thanh toán nợ nguyên tử (Firestore Atomic Transaction) trừ tiền ví nguồn, giảm dư nợ và tự động gán danh mục "Trả nợ & Tín dụng" cho sổ cái.
  - Lưu vĩnh viễn chiến lược thoát nợ và số tiền trả thêm mỗi tháng vào DataStore (`DebtPreferenceRepository`).
- **Khóa Ứng Dụng Bằng Sinh Trắc Học Tự Động (Biometric Auto-Lock)**:
  - Tích hợp `AppLockManager` với `ProcessLifecycleOwner` tự động hiển thị BiometricPrompt khi quay lại ứng dụng.
  - Tùy chọn cấu hình thời gian khóa: *Ngay lập tức*, *1 phút*, *5 phút*.
- **Xuất Báo Cáo Tài Chính Chuẩn XLSX 2 Sheet & PDF Trực Quan (UC-17)**:
  - Bộ xuất Excel `.xlsx` thực thụ với 2 Sheet độc lập (*Sheet 1: Bảng kê chi tiết giao dịch*, *Sheet 2: Tổng hợp theo danh mục*).
  - Xuất PDF vẽ biểu đồ tỷ trọng (Donut chart / Progress bar) kèm thống kê Tổng tài sản và Tổng dư nợ.
- **Component Nhập Tiền Tệ Dùng Chung (FinluxAmountInputCard)**:
  - Hiển thị số tiền chữ to in đậm (30sp), tự động định dạng phân tách hàng nghìn VNĐ (`50.000 ₫`), khử lỗi số 0 ban đầu, dải chip cộng tiền nhanh và nút Clear `[x]` tiện lợi.

### Changed
- **Chuẩn Hóa Công Thức Tài Sản Ròng (Net Worth)**:
  - Thẻ Hero trên Trang chủ hiển thị trực quan: `Tài sản ròng = Tổng tài sản khả dụng (Gross Assets) - Tổng dư nợ chưa trả (Liabilities)`.
- **Tinh Gọn Form Thêm Giao Dịch**:
  - Chuyển dải tab phụ sang bộ chuyển đổi 2 Tab chuẩn `[Chi tiêu | Thu nhập]`, đưa toàn bộ phân loại chi tiết về Grid Danh mục.

### Fixed
- Khắc phục lỗi Google Sign-In bị fallback sang dữ liệu Demo khi đăng nhập lần đầu.
- Sửa triệt để lỗi nền tối màn hình Quản lý nợ khi ở Theme Sáng (Light Mode).
- Ẩn dòng chữ thừa "Không có khoản nợ" trên thẻ Hero Card Trang chủ khi dư nợ bằng 0.
- Chuẩn hóa biểu đồ Donut Chart phân bổ danh mục hiển thị mặc định 0 đ và 0% cho tài khoản mới.
- Sửa lỗi giữ lại trạng thái cũ ("Sửa giao dịch") khi mở form Thêm thu / Thêm chi mới.
- Tích hợp bộ chọn giờ (`TimePickerDialog`) kết hợp với chọn ngày, cho phép tùy chỉnh chính xác giờ/phút giao dịch.

## [1.8.8] - 2026-08-22
### Changed
- Căn chỉnh lại Đăng nhập/Đăng ký theo ảnh tham chiếu: hero thương hiệu và minh họa 3D đúng tỷ lệ, form trắng thoáng, CTA gradient tím, social Google/Facebook dạng ngang và sóng trang trí ở đáy.
- CTA xác thực có phản hồi Liquid Glass spring 0.975 kèm haptic; contract Apple vẫn được giữ để tích hợp sau nhưng không hiển thị lệch mẫu.
- Cải tiến chuyển động vuốt chuyển tab (`Trang chủ ↔ Lịch sử ↔ Báo cáo ↔ Cài đặt`) sang slide ngang 100% full-width liên tục và mượt mà, đồng bộ giữa trang hiện tại và trang kế tiếp.

### Fixed
- Sửa triệt để lỗi cử chỉ vuốt làm lộ nền xanh đậm với logo splash screen (`finlux_launch_background`): loại bỏ `translationX` kéo lệch đơn lẻ trên `NavHost` và cố định `Surface` nền theme tại root.
- Sửa an toàn gọi `MainBottomBar` trong `PrismTransactionsScreen` tránh crash khi null.

## [1.8.7] - 2026-08-21
### Added
- Bổ sung Firebase Cloud Functions thế hệ 2 cho đối soát ngân sách, cảnh báo ngưỡng 80%/100%, reset hạn mức tháng và gửi nhắc nhở định kỳ qua FCM.
- Bổ sung empty state và unit test cho dữ liệu biểu đồ Báo cáo Prism.
- Bổ sung menu Cài đặt Prism theo nhóm Tài khoản, Quản lý tài chính, Ứng dụng, Hỗ trợ và Thông tin; có hộp tùy chỉnh giao diện riêng và thẻ tổng tài sản ẩn/hiện số dư.

### Changed
- Thiết kế lại màn Đăng nhập/Đăng ký theo bố cục premium trắng–tím: nhận diện thương hiệu căn giữa, header đăng ký gradient có minh họa 3D, form bo tròn, hỗ trợ bàn phím/safe area và giữ đủ contract Google/Apple/Facebook.
- Thanh điều hướng và cử chỉ vuốt chính dùng đúng luồng `Trang chủ ↔ Lịch sử ↔ Báo cáo ↔ Hồ sơ`, có hiệu ứng bám ngón tay và spring Liquid Glass thống nhất; quản lý Ví nằm trong Cài đặt.
- Báo cáo Prism dùng hoàn toàn dữ liệu giao dịch thực tế cho tab, biểu đồ, tooltip và so sánh kỳ; loại dữ liệu minh họa hard-code.
- Gia cố Firestore Rules để giao dịch và biến động số dư ví phải được ghi nguyên tử, đồng thời kiểm tra schema ví/ngân sách.
- Thiết kế lại hồ sơ/Cài đặt theo Liquid Glass thích ứng sáng-tối, giữ đầy đủ đổi avatar/tên, Ví, Ngân sách, Danh mục, Nhắc nhở, Thông báo, sinh trắc học, cập nhật và đăng xuất.

### Fixed
- Sửa lỗi hiển thị chữ dọc 'Nhắc nhở' trong thẻ giao dịch gần nhất của PrismHomeScreen: loại bỏ badge hardcoded bị chèn ép layout và áp dụng TextOverflow.Ellipsis cho tiêu đề giao dịch.
- Sửa header Báo cáo Prism bị thanh trạng thái hệ thống đè lên tiêu đề, nút Bộ lọc và nút xuất file trên thiết bị edge-to-edge.
- Đăng ký không còn báo thành công khi seed hồ sơ, ví hoặc danh mục thất bại; tài khoản Auth mới được rollback để người dùng thử lại.
- Đồng bộ FCM token sau đăng nhập/đăng ký và khi token thiết bị thay đổi; lỗi đồng bộ token không làm hỏng phiên đăng nhập.
- Sửa biên tháng của tác vụ ngân sách theo đúng múi giờ `Asia/Ho_Chi_Minh`.

## [1.8.6] - 2026-08-20
### Added
- **Bộ 3 Màn Hình Tạo Giao Dịch Thế Hệ Mới (FinLux Prism Quick Add Hub & Forms)**:
  - **Quick Add Hub Modal**: Bento grid 2x2 sống động (Thêm thu, Thêm chi, Chuyển tiền, Scan hóa đơn), banner Thêm mục tiêu, danh sách 3 giao dịch gần nhất và gợi ý tương tác thông minh.
  - **Form Thêm Chi / Thêm Thu Tinh Gọn & Công Thái Học**: Header nút Lưu tròn xanh `✓`, 3 tab phân loại pill, ô nhập số tiền siêu lớn 32sp in đậm tự động định dạng phân tách hàng nghìn (`728.000 ₫`) theo thời gian thực và 4 chip cộng tiền nhanh (`+10k`, `+50k`, `+100k`, `+500k`).
  - **Thẻ Thông Tin 2 Tầng Công Thái Học**: Danh mục, Ví nguồn kèm số dư thời gian thực, Thời gian giao dịch thông minh ("Hôm nay, dd/MM/yyyy • HH:mm"), Ghi chú và Đính kèm hóa đơn/chứng từ.
  - **Modal Chọn & Quản Lý Danh Mục (Category Picker Sheet)**: Tìm kiếm tức thì, lưới 4 cột icon bo góc với hiệu ứng viền và huy hiệu đỏ `✓` cho danh mục được chọn, hỗ trợ tạo danh mục mới.
- **Đồ Họa Minh Họa 3D Sổ Thu Chi Không Gian (Prism3DTransactionIllustration)**:
  - Tích hợp cụm đồ họa 3D Liquid Glass gồm Hóa đơn kính mờ nghiêng -14°, Thẻ ngân hàng Hologram ánh kim nghiêng +10°, Đồng xu vàng `₫` tiền cảnh và Ngôi sao lấp lánh trên thẻ tổng quan Lịch sử giao dịch.

### Changed
- **Nâng Cấp Thiết Kế Giao Diện Toàn Diện (FinLux Prism UI Theme)**: Chuẩn hóa toàn bộ hệ thống màu sắc, bo góc 22-28dp, đổ bóng mềm mại và kiểu chữ ExtraBold trên toàn bộ các màn hình chính (Trang chủ, Lịch sử giao dịch, Tạo giao dịch).
- **Tối Ưu Trải Nghiệm Nhập Liệu Tài Chính**: Bỏ các trường dữ liệu trùng lặp, tối ưu hóa kích thước và vị trí các nút bấm giúp thao tác bằng một tay nhanh chóng và chính xác.

### Fixed
- **Định Dạng Tiền Tệ Trực Tiếp (Real-time Currency Formatting)**: Loại bỏ triệt để hiện tượng ngắt quãng giữa số tiền và ký hiệu tiền tệ, tự động thêm dấu chấm hàng nghìn chuẩn tiếng Việt.

## [1.8.5] - 2026-08-19
### Added
- **Xác Thực Tính Toàn Vẹn Bản Cập Nhật Tự Động (Secure OTA Verification)**: Tự động kiểm tra mã băm bảo mật SHA-256, chữ ký số và gói cài đặt trước khi mở cập nhật, bảo vệ điện thoại khỏi file cài đặt hỏng hoặc không rõ nguồn gốc.
- **Quy Trình Tự Động Kiểm Thử & Phát Hành Độc Lập**: Tách biệt hệ thống kiểm tra chất lượng định kỳ và hệ thống phát hành bản dựng chính thức, nâng cao độ tin cậy và an toàn của phần mềm.

### Changed
- **Bảo Vệ Dữ Liệu Thu Chi Đám Mây Toàn Diện**: Tăng cường bảo mật cơ sở dữ liệu với chính sách kiểm duyệt chặt chẽ, ngăn chặn tuyệt đối các can thiệp bất hợp pháp vào dữ liệu tài chính của người dùng.
- **Tối Ưu Hóa Kích Thước Ứng Dụng**: Loại bỏ các tài nguyên ảnh dư thừa, giúp bộ cài đặt nhẹ hơn và khởi động nhanh hơn.

### Fixed
- **Kiểm Thử Số Dư & Giao Dịch Toàn Diện**: Mở rộng 100% kịch bản kiểm thử tự động cho mọi thao tác Thu, Chi, Chuyển tiền giữa các ví, đảm bảo số dư ví và ngân sách luôn chính xác tuyệt đối.

## [1.8.4] - 2026-08-19
### Added
- **Tự Động Cập Nhật Ứng Dụng Không Cần Cài Lại (In-App OTA Auto-Update)**: Tự động phát hiện khi có phiên bản mới từ GitHub, hiển thị pop-up thông báo tính năng mới kèm thanh tiến trình tải và nút "Cài đặt ngay" trực tiếp trên điện thoại.
- **Tích Hợp Tự Động Đóng Gói & Phát Hành APK (GitHub Actions CI/CD)**: Tự động chạy kiểm thử, đóng gói APK và phát hành GitHub Release mỗi khi cập nhật tính năng mới.

### Fixed
- **Kiểm Tra Bản Cập Nhật Thủ Công Trong Cài Đặt**: Bổ sung nút "Kiểm tra bản cập nhật mới" tại màn hình Hồ sơ & Cài đặt để người dùng chủ động làm mới ứng dụng bất kỳ lúc nào.

## [1.8.3] - 2026-08-19
### Fixed
- **Độ chính xác tuyệt đối khi Sửa / Xóa giao dịch**: Khắc phục triệt để lỗi sai lệch số dư ví và ngân sách khi người dùng điều chỉnh hoặc xóa một khoản thu chi; hệ thống luôn tự động hoàn tiền và cập nhật số dư chuẩn xác theo đúng giao dịch thực tế.
- **Chuẩn hóa tính toán theo múi giờ Việt Nam**: Đảm bảo các giao dịch phát sinh vào thời điểm giao mùa hoặc đêm muộn đầu tháng / cuối tháng luôn được xếp đúng vào tháng tài chính tương ứng và đồng bộ chuẩn xác với hạn mức ngân sách tháng đó.
- **Bảo vệ an toàn số dư**: Tự động kiểm tra và ngăn chặn các giao dịch có số tiền không hợp lệ hoặc vượt quá giới hạn an toàn.

### Changed
- **Tối ưu hóa tốc độ và độ ổn định hệ thống**: Phân tách độc lập các luồng xử lý ví, danh mục, ngân sách, nhắc nhở và mục tiêu giúp ứng dụng phản hồi nhanh hơn, tiết kiệm tài nguyên và hoạt động tin cậy hơn.

## [1.8.2] - 2026-08-19
### Added
- **Chi Tiết Giao Dịch Khi Chạm Đơn (TransactionDetailSheet)**: Chạm vào bất kỳ giao dịch nào sẽ mở modal Liquid Glass xem đầy đủ thông tin: số tiền kèm badge thu/chi, danh mục, ví thanh toán, ngày giờ chuẩn xác, ghi chú và hóa đơn đính kèm. Bên trong có nút "Sửa" và "Xóa" kèm xác nhận an toàn.
- **Pop-up Tùy Chọn Khi Bấm Giữ (TransactionActionDialog)**: Bấm giữ (Long-press) vào giao dịch sẽ hiển thị pop-up lựa chọn nhanh: "Xem chi tiết", "Sửa giao dịch", "Xóa giao dịch" kèm dialog xác nhận xóa (`DeleteTransactionConfirmDialog`).
- **Tab Lịch Sử Thu/Chi Mới Tại Bottom Navigation Bar**: Thay thế vị trí tab "Ví" ở thanh điều hướng dưới bằng tab "Lịch sử" (`Route.Transactions`) với icon `ReceiptLong` để truy cập trực tiếp toàn bộ lịch sử thu/chi, hỗ trợ lọc Tất cả / Thu / Chi và chuyển tab bằng cử chỉ vuốt ngang mượt mà.

### Changed
- **Điều Hướng & Vuốt Ngang (Main Swipe Navigation)**: Cập nhật luồng vuốt ngang chính: `Trang chủ` <-> `Lịch sử` <-> `Báo cáo` <-> `Hồ sơ`.

## [1.8.1] - 2026-08-19
### Added
- **Chỉnh Sửa & Điều Chỉnh Giao Dịch Thu Chi (UC-08)**: Hỗ trợ chạm vào bất kỳ giao dịch nào hoặc nhấn nút sửa (Edit) để mở form chỉnh sửa trực quan trên toàn bộ các màn hình (Home, Danh sách giao dịch Classic & Modern, Thu nhập, Chi tiêu).
- **Edit Mode trong AddTransactionSheet**: Tự động nhận diện và nạp dữ liệu giao dịch cũ (loại thu/chi, số tiền, danh mục, ví, ghi chú, ngày, hóa đơn đính kèm), cập nhật tiêu đề "Sửa giao dịch" và nút "Lưu thay đổi".

### Fixed
- **Đồng Bộ Hoàn Tiền & Cập Nhật Số Dư Ví Nguyên Tử**: Kết nối `EditTransactionUseCase` với `AddTransactionViewModel`, hoàn nguyên số dư ví cũ và cập nhật số dư ví mới + ngân sách qua Firestore Transaction an toàn tuyệt đối.

## [1.8.0] - 2026-08-15
### Added
- **Xuất Báo Cáo Tài Chính Excel (.csv) & PDF (UC-17)**: Hỗ trợ xuất dữ liệu thu chi chuẩn UTF-8 BOM cho Excel và tạo file PDF đa trang chuyên nghiệp kèm biểu đồ KPI, thống kê danh mục và bảng giao dịch chi tiết qua `ReportExporter` và `ExportReportDialog`.
- **Trung Tâm Thông Báo Đa Năng (Task v1.6.0)**: Bổ sung 5 phân loại thông báo (`REMINDER`, `BUDGET_ALERT`, `GOAL_MILESTONE`, `TRANSACTION_SUMMARY`, `SYSTEM`), thanh Filter Chips và điều hướng Deep Link trực tiếp sang các màn hình chức năng.
- **Bảo Mật Sinh Trắc Học (Biometric Lock)**: Tích hợp `androidx.biometric:biometric` hỗ trợ khóa mở app bằng Vân tay / Face ID / Mã PIN thiết bị, thiết lập bật/tắt trong `SettingsScreen`.

### Fixed
- **Lưu & Đồng Bộ Ví Tiền Mặt (Cash Wallet Database Persistence)**: Tự động khởi tạo và lưu trữ vĩnh viễn ví mặc định "Tiền mặt" vào Firestore Database ngay khi tài khoản mới hoặc tài khoản hiện có chưa có ví; bổ sung bộ phân giải đa định dạng `parseWalletType` chống mất dữ liệu ví tiền mặt; cập nhật logic nguyên tử (atomic batch) duy trì trạng thái ví mặc định.
- **Chuyển Tiền Giữa Các Ví (Transfer Money Validation)**: Bổ sung validation kiểm tra số dư ví nguồn (chặn số dư âm đối với ví không phải thẻ tín dụng) trên cả tầng Domain (`TransferMoneyUseCase`), Data Repositories và UI Form (`TransferEditor`).
- **Sửa Lỗi Tính % Tỷ Trọng Ví**: Khắc phục lỗi hiển thị tỷ trọng phần trăm âm/bất thường khi ví có số dư nợ hoặc tổng tài sản nhỏ hơn 0.

---
Tất cả những thay đổi quan trọng của dự án FinLux sẽ được ghi lại tại đây.
Định dạng dựa trên [Keep a Changelog](https://keepachangelog.com/vi/1.0.0/) và tuân thủ [Semantic Versioning](https://semver.org/).

## [1.7.7] - 2026-08-15

### Fixed & Improved
- **Đồng Bộ Nền & Tương Phản Cho Màn Hình Hồ Sơ & Cài Đặt (SettingsScreen Theme & Contrast Overhaul):**
  - **Loại bỏ hardcode nền tối:** Bọc toàn bộ `SettingsScreen` với `FinluxStyleBackdrop` / `ModernStyleBackdrop` tự động thích ứng chuẩn xác với UI Style (Classic / Modern) và Theme (Light / Dark mode).
  - **Tối ưu độ tương phản TopBar & Menu:** Tiêu đề "Hồ sơ & Cài đặt", icon Back và toàn bộ nhãn menu chuyển sang dùng `MaterialTheme.colorScheme.onSurface`, sắc nét và rõ ràng 100% trên nền sáng.
  - **Tái thiết kế Thẻ Profile Hero & Chống Cụt Tên Người Dùng:**
    * Khung hiển thị tên hỗ trợ co giãn linh hoạt và xuống dòng tối đa 2 dòng (`maxLines = 2`), không bao giờ bị cắt ngắn hoặc cụt chữ.
    * Thẻ Hero Profile sử dụng dải gradient xanh tươi sáng với hiệu ứng viền kính khúc xạ sang trọng.
    * Khung hiển thị "Tổng tài sản" chuyển sang lớp kính mờ Liquid Glass trong suốt (`Color.White.copy(alpha = 0.16f)`), loại bỏ hoàn toàn hộp nền tím tối lệch tông và icon logo chìm thừa.
  - **Tối ưu các nút phím tắt danh mục (ProfileFeatureTiles):** Bổ sung viền tròn icon `accent.copy(alpha = 0.16f)` và màu chữ tương phản cao đồng nhất với `HomeScreen` và `WalletsScreen`.

---

## [1.7.6] - 2026-08-15

### Fixed
- **Khắc Phục Triệt Để Lỗi Lộ Icon Thùng Rác Xuyên Thấu Thẻ Ví (Zero Ghosting Swipe-to-Delete):**
  - **Triệt tiêu hoàn toàn background khi chưa vuốt:** `backgroundContent` của `SwipeToDismissBox` được cấu hình render động, chỉ vẽ nền đỏ và icon thùng rác khi người dùng thực sự bắt đầu thao tác vuốt (`dismissDirection == EndToStart`).
  - **Hiệu ứng mờ dần mượt mà (Dynamic Alpha & Scale):** Khi vuốt thẻ, nền đỏ và icon thùng rác xuất hiện với độ mờ tăng dần theo quãng đường vuốt kết hợp phóng to nhẹ (`graphicsLayer`), mang lại cảm giác phản hồi xúc giác chân thực.
  - Khi thẻ ở vị trí bình thường (`Settled`), background hoàn toàn rỗng/trong suốt, đảm bảo 100% không bao giờ bị nhìn xuyên thấu qua lớp kính Liquid Glass làm che khuất số tiền và tỷ lệ %.
  - Đồng bộ trên cả `ModernWalletsScreen` và `ClassicWalletsScreen`.

---

## [1.7.5] - 2026-08-15

### Added & Improved
- **Khôi Phục & Nâng Cấp Vuốt Trái Xóa Ví An Toàn (Safe Swipe-to-Delete with Confirmation):**
  - Khôi phục cử chỉ vuốt thẻ ví từ Phải sang Trái (`SwipeToDismissBox`) với nền màu đỏ mềm mại bo cong 20dp chứa icon thùng rác `DeleteOutline`.
  - **Cơ chế hoàn trả & xác nhận an toàn:** Khi vuốt qua ngưỡng, thẻ ví tự động trượt êm ái về vị trí cũ và hiển thị Dialog xác nhận: *"Bạn có chắc chắn muốn xóa ví [Tên ví]? Tất cả giao dịch thuộc ví này sẽ bị ảnh hưởng"*. Chỉ xóa khi người dùng chọn [Xóa vĩnh viễn].
  - **Khóa cử chỉ bảo vệ Ví mặc định & Ví duy nhất:** Tự động vô hiệu hóa hoàn toàn cử chỉ vuốt (`enableDismissFromEndToStart = false`) đối với ví đang là mặc định hoặc ví duy nhất còn lại.
  - Thẻ ví ở trạng thái bình thường giữ nguyên bố cục sạch đẹp, không có icon rác trần gây dính cục.
  - Đồng bộ 100% trên cả `ModernWalletsScreen` và `ClassicWalletsScreen`.

---

## [1.7.4] - 2026-08-15

### Fixed & Improved
- **Tinh Chỉnh Bố Cục Thẻ Ví & UX Xóa Ví An Toàn (Refined Wallet Card Layout & Safety UX):**
  - **Bỏ hẳn nút xóa trần trên thẻ ví:** Loại bỏ `SwipeToDismissBox` và icon thùng rác dính sát số tiền, tái cấu trúc cột bên phải thẻ ví thành hiển thị Số tiền in đậm to rõ và Tỷ lệ % ngay bên dưới một cách cân đối, sang trọng.
  - **Chi tiết & Chỉnh sửa ví:** Khi bấm vào thẻ ví, mở `GlassBottomSheet` trực quan cho phép chỉnh sửa tên ví, số dư, loại ví, màu thẻ và nút gạt Switch "Đặt làm ví mặc định".
  - **Chống xóa nhầm & Bảo vệ ví mặc định:**
    * Nút [Xóa ví này] chỉ xuất hiện ở đáy BottomSheet chi tiết ví khi chỉnh sửa kèm Dialog xác nhận: "Bạn có chắc chắn muốn xóa ví này? Tất cả giao dịch thuộc ví sẽ bị ảnh hưởng".
    * Tự động nhận diện và khóa/ẩn nút Xóa đối với **Ví mặc định** hoặc **Ví duy nhất còn lại**, kèm cảnh báo: "Không thể xóa ví mặc định. Vui lòng đặt ví khác làm mặc định trước khi xóa!".
  - **Tinh chỉnh thanh cuộn Filter Chips:** Dãy chip lọc loại ví hỗ trợ `contentPadding = PaddingValues(horizontal = 16.dp)`, vuốt tràn lề mượt mà không dính mép màn hình.
  - Đồng bộ chuẩn 100% trên cả 2 phong cách giao diện `ModernWalletsScreen` và `ClassicWalletsScreen`.

---

## [1.7.3] - 2026-08-15

### Fixed & Redesigned
- **Tái Thiết Kế Toàn Diện UI "Thêm ví mới" & "Chuyển tiền" (Add & Transfer Wallet UI):**
  - Chuyển đổi dialog nổi thông thường sang `GlassBottomSheet` hiện đại, trượt lên mượt mà với scrim nền làm mờ sâu, triệt tiêu 100% hiện tượng chữ/danh sách ví phía sau bị lộ xuyên qua.
  - Tăng độ phủ đặc `GlassDialogSurface` lên `0.98f` kết hợp viền tán sắc Chromatic Rim chống lóa và chống xuyên thấu nền.
  - Bổ sung bộ chọn nhanh số dư dạng Chip thông minh (`+500K`, `+1M`, `+2M`, `+5M`, `+10M` và `+100K`, `+200K`...).
  - Thiết kế bảng chọn màu ví trực quan với viền active và icon loại ví động (`CASH`, `BANK`, `EWALLET`, `CARD`, `INVESTMENT`).
  - Hỗ trợ phím tắt chuyển tiền thông minh ngay từ `QuickAddSheet` kết nối trực tiếp vào `WalletsScreen`.
  - Quét sạch và chuẩn hóa toàn bộ font chữ tiếng Việt UTF-8 không lỗi bảng mã.

---

## [1.7.2] - 2026-08-15

### Added
- **Ráp Trọn Vẹn Giao Diện Modern Luxury từ commit `6535f24`:**
  - `ModernHomeScreen`: Bố cục Hero Balance Card phát quang đa lớp mới, các thẻ metric bo tròn 20dp, phân tích chi tiêu dạng spatial charts, và thanh điều hướng Floating Capsule Dock.
  - `ModernBudgetScreen`: Progress cards đa lớp Callstack Liquid Glass với hiệu ứng đổ bóng phát quang.
  - `ModernReportsScreen`: Analytics panels hiện đại với bộ chọn kỳ báo cáo dạng Capsule Pills.
  - `ModernWalletsScreen`: Thẻ ví kính lỏng `LiquidGlassMode.CLEAR`, hỗ trợ thao tác vuốt xóa / chỉnh sửa trực quan.
  - `ModernTransactionsScreen`: Nhóm giao dịch bo tròn với filter capsule hiện đại.
  - Chuẩn hóa 100% tiếng Việt UTF-8 sạch và kết nối chính xác vào `com.finlux.app.core.designsystem.modern.*`.

---

## [1.7.1] - 2026-08-15

### Fixed
- **Cách ly hoàn toàn Design System (100% Dual-UI Isolation):**
  - Khôi phục nguyên bản 100% các file Design System Cổ điển từ commit ổn định `280b722` (`LiquidGlass.kt`, `StyleBackdrop.kt`, `WaterGlass.kt`, `FinluxComponents.kt`, `FinluxBrand.kt`).
  - Đóng gói toàn bộ component Modern Callstack vào thư mục riêng `core/designsystem/modern/` (`ModernLiquidGlass.kt`, `ModernStyleBackdrop.kt`, `ModernWaterGlass.kt`, `ModernFinluxComponents.kt`), loại bỏ hoàn toàn hiện tượng lóa sáng, chồng chéo gradient, và bể vỡ layout.
  - Khôi phục 100% thanh điều hướng cổ điển chuẩn xác `ClassicMainBottomBar` (docked glass bar tiêu chuẩn từ `280b722`).
- **Nâng cấp Giao diện Chọn Phong Cách trong Cài đặt (`SettingsScreen.kt`):**
  - Thiết kế mục Card Cài đặt "Phong cách giao diện" có hiển thị tên phong cách hiện tại.
  - Khi bấm vào mở `GlassBottomSheet` với 2 tùy chọn Radio trực quan:
    * 🔘 **Liquid Glass (Cổ điển)**: "Giao diện thanh lịch, tương phản cao, ổn định".
    * 🔘 **Modern Luxury (Hiện đại)**: "Giao diện kính lỏng Callstack, bo tròn, phong cách mới".
  - Sửa lỗi encoding toàn bộ các chuỗi tiếng Việt trên các màn hình `modern/`.

---

## [1.7.0] - 2026-08-15

### Added
- **Kiến trúc Đa Phong Cách Giao Diện (Dual-UI Style Architecture):**
  - Giữ trọn vẹn phong cách **Liquid Glass Classic** (v1.5.9) ổn định và tích hợp phong cách mới **Modern Luxury** (Callstack Liquid Glass chuẩn iOS 26).
  - Bổ sung `enum class AppUiStyle { CLASSIC_LIQUID, MODERN_LUXURY }` trong tầng Domain Model và lưu trữ trong `DataStoreThemePreferenceRepository`.
  - Cung cấp `LocalAppUiStyle` CompositionLocal xuyên suốt toàn bộ cây Composable thông qua `FinluxTheme`.
  - Tách bạch cấu trúc màn hình và components theo cơ chế Dispatcher: `classic/` (ClassicHomeScreen, ClassicBudgetScreen, ClassicReportsScreen, ClassicWalletsScreen, ClassicTransactionsScreen, ClassicMainBottomBar) và `modern/` (ModernHomeScreen, ModernBudgetScreen, ModernReportsScreen, ModernWalletsScreen, ModernTransactionsScreen, ModernMainBottomBar).
  - Tích hợp mục chọn **[🎨 Phong cách giao diện]** trong màn hình Cài đặt (`SettingsScreen.kt`) cho phép người dùng chuyển đổi mượt mà và lưu lại tùy chọn ngay tức thì.
  - Bổ sung bộ kiểm thử đơn vị `RootViewModelTest` đạt 100% test coverage cho luồng chuyển đổi UI Style.

---

## [1.6.7] - 2026-08-15

### Added
- **Xử lý Atomic Transaction chống lỗi thiếu Budget document (`FirebaseTransactionRepository.kt`):** Thêm kiểm tra `budgetDoc.exists()` trước khi cập nhật `spentAmount` trong transaction Firestore cho mọi thao tác Thêm, Sửa, Xóa giao dịch.

### Changed
- **Tối ưu cử chỉ vuốt chuyển tab mượt mà (`FinluxNavHost.kt`):** Dọn dẹp các đoạn code navigation trùng lặp, tối ưu thuật toán phân định hướng vuốt ngang vs cuộn dọc tránh xung đột giật khựng khi cuộn danh sách.
- **Ngăn chặn reload trạng thái khi bấm Home (`FinluxNavHost.kt`):** Bổ sung kiểm tra `route != currentRoute` và sử dụng `saveState = true` / `restoreState = true` để giữ nguyên trạng thái UI khi người dùng chuyển đổi qua lại giữa các tab chính.

### Fixed
- **Lỗi `NOT_FOUND: No document to update` khi ghi giao dịch không có ngân sách:** Khắc phục triệt để lỗi crash khi thêm chi tiêu vào danh mục chưa khởi tạo hạn mức ngân sách tháng.

---

## [1.5.9] - 2026-08-14

### Changed
- **Chuẩn hóa Tên Release & File APK Gọn gàng (`release.yml`):**
  - Loại bỏ hậu tố `-build-*` khỏi Release Title và Tag Name.
  - Tên Release chính thức: `Release v<versionName>` (ví dụ `Release v1.5.9`).
  - Tên file APK Release đính kèm: `FinLux-v<versionName>.apk` (ví dụ `FinLux-v1.5.9.apk`).

---

## [1.5.8] - 2026-08-14

### Added
- **Tích hợp Trực tiếp Cấu hình Firebase `google-services.json`:**
  - Nhúng trực tiếp cấu hình `app/google-services.json` vào repository để quy trình CI/CD GitHub Actions luôn tự động biên dịch đầy đủ tính năng Đăng nhập Google vào file APK Release mà không phụ thuộc cấu hình Secret thủ công.
  - Cập nhật Web Client ID chính thức `927751753962-04paon2termkbeanbsv7m8t9a8m6tk5h.apps.googleusercontent.com` vào `AuthViewModel.kt`.
  - Tự động nhận diện JDK 17 và Android SDK từ cache toolchain trong script nạp `build_and_install.ps1`.

---

## [1.5.7] - 2026-08-14

### Changed
- **Chuẩn hóa Tên File APK Đính kèm GitHub Release (`release.yml`):**
  - Tự động đổi tên file APK artifact đầu ra từ `app-debug.apk` mặc định thành `FinLux-<TAG_NAME>.apk` (ví dụ `FinLux-v1.5.7-build-5.apk` hoặc `FinLux-v1.5.7.apk`).
  - Trích xuất tự động `versionName` từ `app/build.gradle.kts` khi build tự động trên nhánh `main` mà không cần đẩy tag thủ công.

---

## [1.5.6] - 2026-08-13

### Added
- **Tự động Xin quyền Runtime `POST_NOTIFICATIONS` (Android 13+):**
  - Tích hợp `NotificationPermissionHandler` tự động kiểm tra quyền thông báo khi mở Trang chủ (`HomeScreen.kt`) hoặc Nhắc nhở (`RemindersScreen.kt`).
  - Kích hoạt Popup xin quyền chính thức của hệ điều hành Android 13+ nếu ứng dụng chưa được cấp quyền.
- **Dialog Hướng dẫn Mở Cài đặt Ứng dụng khi bị Chặn / Từ chối:**
  - Nếu người dùng bấm Từ chối hoặc bị hệ điều hành (như Xiaomi MIUI, OPPO ColorOS, Vivo FuntouchOS...) tắt công tắc Thông báo/Thông báo nổi, hệ thống hiển thị Dialog Liquid Glass: *"Bật thông báo Finlux để không bỏ lỡ hạn thanh toán hóa đơn"*.
  - Nút bấm `[Bật trong Cài đặt]` mở trực tiếp trang `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` của FinLux để người dùng bật lại công tắc nhanh chóng.

---

## [1.5.5] - 2026-08-13

### Fixed
- **Khôi phục Thông báo Thả xuống dạng Banner (Heads-up Notification Banner Fix):**
  1. Cập nhật Kênh thông báo sang `finlux_reminders_v2` với cấu hình độ ưu tiên tối cao `IMPORTANCE_HIGH`, bật âm thanh, rung (`enableVibration`) và hiển thị công khai trên màn hình khóa.
  2. Bổ sung cấu hình `.setPriority(NotificationCompat.PRIORITY_MAX)`, `.setDefaults(NotificationCompat.DEFAULT_ALL)` và `.setCategory(NotificationCompat.CATEGORY_REMINDER)` giúp thông báo báo thức Finlux luôn tự động trượt thả xuống (Heads-up Dropdown Banner) từ đỉnh màn hình điện thoại khi nổ.

---

## [1.5.4] - 2026-08-13

### Fixed
- **Cập nhật Số tiền Thực trả trên Bản ghi Thông báo (`NotificationsViewModel.kt` & `NotificationsScreen.kt`):**
  1. Khi người dùng điều chỉnh số tiền thực tế trong Quick Payment Sheet (ví dụ `1.950.000 đ`), hệ thống tự động cập nhật trường `amount` và `body` của bản ghi `AppNotification` trong Database thành con số mới thực trả.
  2. Thẻ thông báo trên UI tự động hiển thị chính xác con số thực trả: `Đã thanh toán: 1.950.000 ₫` kèm nhãn màu xanh lá.

---

## [1.5.3] - 2026-08-13

### Added
- **Quick Payment Sheet Hỗ trợ Khoản chi Biến động (`NotificationsScreen.kt`):**
  - Khi bấm nút `[Xác nhận thanh toán]` trên thẻ thông báo, giao diện hiển thị `ModalBottomSheet` Liquid Glass "Xác nhận & Điều chỉnh số tiền".
  - Cho phép người dùng nhập/sửa số tiền thực tế (có preview định dạng VND phân cách hàng nghìn), chọn ví thanh toán và danh mục chi tiêu trước khi bấm `[Xác nhận trừ tiền]`.
- **Thêm Action `[✏️ Sửa số tiền]` trên Push Notification Hệ thống (`AlarmReminderScheduler.kt`):**
  - Bổ sung nút hành động `[Sửa số tiền]` trên thanh thông báo Push hệ thống.
  - Khi bấm: Tự động mở app, điều hướng thẳng đến `NotificationsScreen` và bật sẵn Quick Payment Sheet của thông báo đó.

---

## [1.5.2] - 2026-08-13

### Fixed
- **Fix triệt để Bug Trừ tiền 2 lần khi Thanh toán Thông báo (Double Payment Bug Fix):**
  1. Khi người dùng nhấn nút `[Đã thanh toán]` trực tiếp trên thanh thông báo Push hệ thống (`ReminderReceiver.kt`), hệ thống tự động cập nhật bản ghi `AppNotification` tương ứng thành `isPaid = true` trong Firestore/Database.
  2. Bổ sung kiểm tra an toàn `if (notification.isPaid) return` ngay đầu hàm `payNotification` trong `NotificationsViewModel.kt` để chống race condition và ngăn chặn hoàn toàn việc tạo 2 giao dịch chi tiêu trùng lặp.
  3. Bổ sung Unit Test `NotificationsViewModelTest.kt` đảm bảo tính idempotent 100%.

### Added
- **Tự động đóng gói APK & phát hành GitHub Release khi Push/Merge Git:** Tạo `.github/workflows/release.yml` tự động lắng nghe sự kiện push/merge code trên branch `main` hoặc khi đẩy git tag (`v*`).
- **Tự động khôi phục Google Services Config trong CI/CD:** Workflow tự động phát hiện và khôi phục `app/google-services.json` từ GitHub Secret `GOOGLE_SERVICES_JSON` hoặc dùng `google-services.json.example` nếu chưa cấu hình secret.
- **Tự động tải APK lên GitHub Releases công khai:** Sử dụng `softprops/action-gh-release@v2` tự động xuất bản GitHub Release công khai kèm file `app-debug.apk` đã được build và verify 100% unit tests.

---

## [1.5.1] - 2026-08-13

### Added
- **Nút Xác nhận Thanh toán Trực tiếp trên Màn hình Thông báo (Quick Pay Action):** Trên `NotificationsScreen.kt`, với các thẻ thông báo nhắc nhở thanh toán (có `reminderId` hoặc `amount > 0`), hiển thị nút bấm `[💳 Xác nhận thanh toán]`.
- **Ghi nhận Giao dịch & Trừ Số dư Tự động:** Khi người dùng bấm xác nhận:
  1. Tự động gọi `AddTransactionUseCase` tạo một giao dịch chi tiêu mới (`FinanceTransaction`) tương ứng với thông tin khoản chi.
  2. Tự động trừ số dư ví và cập nhật ngân sách realtime.
  3. Cập nhật trạng thái thẻ thông báo sang nhãn màu xanh lá `[✓ Đã thanh toán]` và ẩn nút bấm.
  4. Hiển thị Snackbar thông báo kết quả: `"Đã ghi nhận thanh toán [Tên khoản chi]!"`.

---

## [1.5.0] - 2026-08-13

### Added
- **Lưu lịch sử thông báo (Notification Persistence):** Khi báo thức nhắc nhở nổ (`ReminderReceiver.kt`), hệ thống tự động lưu bản ghi `AppNotification` vào Firestore subcollection `users/{uid}/notifications` (hoặc `DemoFinluxRepository`).
- **Tự động điều hướng Deep Link khi bấm thông báo:** Cập nhật `PendingIntent` gửi kèm extra `destination = "notifications"`. `MainActivity` và `FinluxNavHost` bắt extra intent (`onCreate` & `onNewIntent`) và tự động chuyển ngay sang màn hình `NotificationsScreen`.
- **Giao diện & ViewModel màn hình Thông báo:** Bổ sung `NotificationsViewModel.kt` và nâng cấp `NotificationsScreen.kt` theo giao diện Liquid Glass, hiển thị thời gian phát sinh, badge chưa đọc, nút đánh dấu đã đọc và tùy chọn xóa sạch lịch sử thông báo.

---

## [1.4.8] - 2026-08-13

### Fixed
- **Triệt tiêu hoàn toàn xung đột cử chỉ (Zero Gesture Collision):** Tháo bỏ toàn bộ khối `pointerInput` cử chỉ kéo trượt toàn màn hình và hiệu ứng `translationX` trong `FinluxNavHost.kt`. Việc chuyển đổi giữa các tab chính dùng 100% việc nhấn biểu tượng trên Bottom Navigation Bar, giúp các danh sách vuốt Card/Ví (`SwipeToDismissBox`) và danh sách ngang hoạt động độc lập, mượt mà tuyệt đối mà không bao giờ bị xô lệch khung màn hình cha.

---

## [1.4.7] - 2026-08-13

### Fixed
- **Xung đột cử chỉ vuốt (Gesture Collision Fix):** Cập nhật `FinluxNavHost.kt` chuyển lắng nghe touch sang `PointerEventPass.Main` và kiểm tra `change.isConsumed`. Khi người dùng vuốt item trong danh sách (như `SwipeToDismissBox` ở màn hình Ví), sự kiện vuốt ngang được con tiêu thụ hoàn toàn, hủy triệt để việc kéo lệch toàn bộ khung màn hình/chuyển tab cha.
- **Tràn bố cục nút bấm khi vuốt (UI Clipping Fix):** Bổ sung `Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))` cho `SwipeToDismissBox` trong `WalletsScreen.kt`. Khung nút "Sửa/Xóa" nay được bo góc và nằm gọn hoàn toàn bên trong Card item, không bị tràn/đè viền lên Bottom Navigation Bar.

---

## [1.4.6] - 2026-08-13

### Fixed
- **Google Auth CredentialProvider Compatibility:** Khai báo trực tiếp dependency `com.google.android.gms:play-services-auth:21.3.0` giúp `CredentialManager` của Android định vị thành công Play Auth Provider, khắc phục triệt để ngoại lệ `GetCredentialProviderConfigurationException` trên Android Emulator và thiết bị Android 13 trở xuống.

### Added
- **Hỗ trợ nạp APK đa thiết bị trong `build_and_install.ps1`:** Nâng cấp script tự động lọc danh sách tất cả các thiết bị ADB đang kết nối (Wireless / USB / Emulator) và cài đè APK song song thành công cho toàn bộ thiết bị.

---

## [1.4.5] - 2026-08-13

### Changed
- **Tên hiển thị ứng dụng (App Launcher Display Name):** Đổi nhãn hiển thị icon ứng dụng trên màn hình điện thoại từ `Finlux` thành **`Finance Luxury`** (`app/src/main/res/values/strings.xml`).

---

## [1.4.4] - 2026-08-13

### Added
- **Shared Project Debug Keystore (`app/debug.keystore`):** Đưa file keystore cố định vào repository tại đường dẫn `app/debug.keystore` để tất cả thành viên trong dự án dùng chung 1 chữ ký debug duy nhất.
- **Cấu hình Gradle `signingConfigs.debug`:** Cập nhật `app/build.gradle.kts` đảm bảo kiểu build `debug` tự động ký bằng `app/debug.keystore`.

### Changed
- Đồng bộ hóa mã SHA-1 Google Auth trên Firebase Console cho tất cả môi trường phát triển của nhóm.

---

## [1.4.3] - 2026-08-13

### Fixed
- **BudgetViewModel: spentAmount cộng dồn sai khi có cả giao dịch modern + legacy:** Sửa logic từ `?:` (short-circuit OR) sang `+` (cộng dồn) — nay gom tất cả giao dịch chi tiêu khớp theo `categoryId` (modern) **VÀ** khớp theo `category.name` (legacy fallback cho giao dịch phiên bản cũ), không bỏ sót bên nào. Guard tránh double-count khi `categoryId.lowercase() == name.lowercase()`.
- **Màn hình Ngân sách: "Còn lại" hiển thị số rút gọn:** Đổi sang `toVnd()` (ví dụ `Còn lại 1.225.000 ₫`) thay vì `toShortVnd()` (làm tròn thô như `1,2tr ₫`).

### Added
- **Unit Tests — BudgetViewModelTest (7 test cases):** Thêm 2 test mới cho kịch bản fallback:
  - `legacyTransactionWithCategoryNameFallbackCalculatesSpentAmountCorrectly`: giao dịch cũ lưu `categoryId = "An uong"` (tên danh mục) vẫn được tính đúng vào ngân sách qua name fallback.
  - `mixedModernAndLegacyTransactionsAccumulateSpentAmountCorrectly`: cộng dồn đúng cả 2 loại tx trong cùng 1 budget.
- **AGENTS.md — Document Management SOP:** Bổ sung quy tắc bắt buộc HANDOVER_LOG PRE/POST-EXECUTION và CHANGELOG chỉ được ghi sau khi test PASS + build thành công.

---

## [1.4.2] - 2026-08-13

### Fixed
- **"Ngân sách còn lại" không cập nhật sau giao dịch:** `HomeViewModel` nay tính `spentAmount` động từ `observeMonth(transactions)` grouped by `categoryId`, thay vì đọc trường `spentAmount` stored trong Firestore. Card cập nhật ngay lập tức khi bất kỳ giao dịch nào được thêm/sửa/xóa.
- **Item giao dịch gần nhất thiếu ngày giờ:** `ReferenceTransactionRow` nay luôn hiển thị cả 2 dòng: dòng Ghi chú (nếu có) + dòng Thời gian ("Hôm nay, HH:mm" / "Hôm qua, HH:mm" / "dd/MM/yyyy, HH:mm").

### Added
- `TransactionRepository.observeMonth(month: YearMonth)` — Flow real-time tất cả transactions trong tháng, dùng cho HomeViewModel và có thể tái sử dụng.

---

## [1.4.1] - 2026-08-13


### Fixed
- **Budget spentAmount không cập nhật real-time (BR-06):** `addWithBalanceUpdate`, `editWithBalanceUpdate`, `deleteWithBalanceUpdate` trong `FirebaseTransactionRepository` nay cập nhật `budget.spentAmount` ngay trong cùng Firestore atomic transaction khi giao dịch là `EXPENSE`. Khắc phục lỗi bấm `[Đã thanh toán]` trên Push Notification không phản ánh lên thanh tiến độ ngân sách.

### Added
- **Unit Tests BudgetViewModel (5 tests):** `BudgetViewModelTest` kiểm tra các kịch bản: `SAFE (0%)`, `SAFE (40%)` sau pay action từ notification, `WARNING (84%)`, `EXCEEDED (100%)`, và nhiều pay action liên tiếp dẫn đến `EXCEEDED`. Tổng 33/33 tests PASS.

---

## [1.4.0] - 2026-08-13


### Added
- **Push Notification Quick Actions (UC-18):** Thêm 2 nút bấm thao tác nhanh ngay trên thông báo Android khi đến hạn nhắc nhở:
  - `[Đã thanh toán]`: Tự động gọi `AddTransactionUseCase` tạo giao dịch chi tiêu mới và trừ số dư ví gán sẵn trong Firestore, sau đó đóng thông báo.
  - `[Nhắc lại sau 1h]`: Đặt lại lịch `AlarmManager` lùi 60 phút.
- **Khôi Phục Báo Thức Sau Reboot (BootReceiver):** Tạo `BootReceiver` lắng nghe `android.intent.action.BOOT_COMPLETED` để tự động đọc danh sách nhắc nhở từ Firestore/Local và khôi phục báo thức `AlarmManager` khi thiết bị khởi động lại.

---

## [1.3.1] - 2026-08-13

### Added
- **Định Dạng Tiền Tệ Tự Động (Currency Format Preview):** Tất cả các ô nhập số tiền (`AddTransactionSheet`, `BudgetEditor`, `WalletEditor`, `TransferEditor`, `ReminderEditor`, `GoalsScreen`) được bổ sung dòng Text Preview định dạng phân cách hàng nghìn (`x.xxx.xxx đ`). Nếu rỗng hoặc 0 sẽ hiển thị `0 đ`.
- **Nút Quay Lại (Back Button) Trên TopBar:** Bổ sung nút Quay lại (`ArrowBack`) ở góc trái Header cho toàn bộ các màn hình phụ (`BudgetScreen`, `WalletsScreen`, `ReportsScreen`, `TransactionsScreen`, `NotificationsScreen`...) gọi `navController.popBackStack()`.

---

## [1.3.0] - 2026-08-13

### Added
- **Modal Lưới Chọn Danh Mục (UC-12):** Thêm ModalBottomSheet chứa lưới 3 cột hiển thị toàn bộ danh mục theo loại Thu nhập/Chi tiêu trong `AddTransactionSheet.kt`.
- **Tạo Danh Mục Tùy Chỉnh (UC-12):** Nút `+ Tạo mới` cho phép nhập Tên, chọn Biểu tượng (Icon) và Màu sắc (Color Hex), gọi `SaveCategoryUseCase` để lưu vào Firestore/Database.
- **Quản lý (Sửa/Xóa) Danh Mục Bằng Nhấn Giữ (Long-Click):** Sự kiện `combinedClickable(onLongClick)` trên từng Card danh mục tùy chỉnh cho phép mở menu Chỉnh sửa hoặc Xóa danh mục (kèm AlertDialog xác nhận và `DeleteCategoryUseCase`).
- **Khóa Bảo Vệ Danh Mục Mặc Định:** Hiển thị Toast thông báo *"Danh mục mặc định không thể sửa/xóa"* khi người dùng nhấn giữ danh mục hệ thống.

---

## [1.2.1] - 2026-08-13

### Fixed
- **Xử lý An Toàn Firestore Security Rules (PERMISSION_DENIED):** Bọc khối `try-catch (e: FirebaseFirestoreException)` cho các tác vụ Firestore (`seedNewUser`, `register`, `signInWithGoogle`, `updateDisplayName`, `updateAvatar`). Log cảnh báo `Log.w("Firestore", ...)` và cho phép đăng nhập Firebase Auth thành công ngay cả khi Firestore chưa gán quyền.
- **Tối Ưu Unblock UI & Timeout 15 giây:** Khối `finally { mutableState.update { it.copy(isLoading = false) } }` trong `AuthViewModel.kt` đảm bảo UI loading overlay không bao giờ bị xoay vô hạn. Bọc tiến trình xác thực bằng `withTimeoutOrNull(15000)` tự động ngắt sau 15s.
- **Tự Động Lấy Client ID Động:** Lấy `serverClientId` trực tiếp từ `R.string.default_web_client_id` do Google Services Plugin tự động sinh từ `google-services.json`.
- **Bảo Vệ Layout Chống Bể Giao Diện:** Bổ sung `maxLines = 1`, `overflow = TextOverflow.Ellipsis`, và `Modifier.weight(1f, fill = false)` trên `HomeScreen.kt` và `SettingsScreen.kt` chống vỡ layout với tên/email dài.
- **Cập Nhật [firestore.rules](file:///d:/Sources/FinLux/firestore.rules):** Cập nhật bộ quy tắc `allow read, write: if request.auth != null;`.

---

## [1.2.0] - 2026-08-13

### Added
- **Đăng nhập Google Sign-In thật (UC-03):** Tích hợp Android Credential Manager SDK (`GetCredentialRequest` & `GetGoogleIdOption`) để lấy `GoogleIdTokenCredential` và xác thực với Firebase Authentication.
- **Trạng thái Loading & Overlay:** Bổ sung CircularProgressIndicator phủ overlay khi ứng dụng đang trong quá trình authenticate.
- **Thông báo cho các phương thức Social chưa hỗ trợ:** Hiển thị Toast thông báo *"Tính năng đăng nhập qua Apple/Facebook sắp ra mắt!"* khi nhấp vào.
- **Phương thức AuthRepository mới:** Bổ sung `signInWithGoogle(idToken: String)` trong domain model, `FirebaseAuthRepository`, và `DemoFinluxRepository`.

### Changed
- **Vô hiệu hóa tạm thời nút Apple & Facebook:** Nút Apple và Facebook trong `SocialCard` được hiển thị mờ 50% kèm mác `(Sắp có)`.

---

## [1.1.0] - 2026-08-13

### Added
- **FirebaseModule Hilt Provider:** Tạo mới `FirebaseModule.kt` cung cấp `@Singleton` cho `FirebaseAuth?`, `FirebaseFirestore?`, `FirebaseStorage?` kèm cơ chế fallback an toàn sang `DemoFinluxRepository` khi chạy ở môi trường Dev chưa cấu hình `google-services.json`.

### Changed
- **Refactor UseCases (SRP):** Tách 14 UseCases độc lập từ `TransactionUseCases.kt` và `ManagementUseCases.kt` ra từng file Kotlin riêng trong package `domain/usecase/` (`AddTransactionUseCase`, `EditTransactionUseCase`, `DeleteTransactionUseCase`, `SaveWalletUseCase`, `DeleteWalletUseCase`, `TransferMoneyUseCase`, `SaveCategoryUseCase`, `DeleteCategoryUseCase`, `SaveBudgetUseCase`, `DeleteBudgetUseCase`, `SaveReminderUseCase`, `DeleteReminderUseCase`, `SaveGoalUseCase`, `DeleteGoalUseCase`).
- **Tối ưu Hilt DI:** Cập nhật `RepositoryModule.kt` để tự động inject Firebase instances từ `FirebaseModule`.

### Fixed
- **Lỗi hiển thị chữ đen trên nền tối:** Chuẩn hóa `FinluxTheme.kt` và `HomeScreen.kt` sử dụng `MaterialTheme.colorScheme.onSurface` và `LocalContentColor`.
- **Lỗi Navigation SettingsScreen:** Sửa điều hướng tab chính qua `navigateMain()` giữ lại backstack sạch sẽ.

---

## [1.0.0] - 2026-08-12

### Added
- **Khởi tạo Dự án FinLux:** Xây dựng ứng dụng quản lý tài chính cá nhân Clean Architecture (3 layers: Domain, Data, Presentation) sử dụng Kotlin, Jetpack Compose, Hilt, DataStore, và Firebase.
- **Giao diện Liquid Glass Design:** Thiết kế hệ thống UI Liquid Glass với hiệu ứng kính làm mờ (glassmorphism), màu sắc dark mode/light mode tự động điều chỉnh.
- **Quản lý Giao dịch (UC-07 -> UC-10):** Thêm/Sửa/Xóa giao dịch thu chi cá nhân với **Firestore Atomic Transactions** tự động cập nhật số dư ví tức thì (BR-06, BR-14).
- **Quản lý Ví & Danh mục (UC-11 -> UC-13):** Tạo ví tài khoản, danh mục thu chi, chuyển tiền giữa các ví.
- **Báo cáo & Thống kê (UC-16, UC-17):** Hiển thị biểu đồ phân tích chi tiêu theo tháng, danh mục.
- **Hệ thống Demo Repository:** `DemoFinluxRepository` cho phép chạy và kiểm thử ứng dụng đầy đủ tính năng ngay cả khi chưa kết nối Firebase backend.
