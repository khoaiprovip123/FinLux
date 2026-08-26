# Changelog

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
