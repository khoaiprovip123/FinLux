# BA SPEC — Finlux (Quản lý Thu Chi Cá Nhân)

## 1. Actor

| Actor | Mô tả |
|-------|-------|
| **User** | Người dùng cá nhân đã đăng ký tài khoản, thao tác toàn bộ nghiệp vụ trong app |
| **System (Scheduler)** | Cloud Functions chạy định kỳ để kiểm tra ngân sách, nhắc bill, tổng hợp thống kê |

Không có vai trò Admin/Manager trong V1 (app cá nhân, không multi-user).

---

## 2. Use Case List

| ID | Tên | Actor |
|----|-----|-------|
| UC-01 | Đăng ký tài khoản | User |
| UC-02 | Đăng nhập | User |
| UC-03 | Đăng nhập bằng Google | User |
| UC-04 | Quên mật khẩu | User |
| UC-05 | Đổi ảnh đại diện | User |
| UC-05A | Đổi tên hiển thị | User |
| UC-06 | Chuyển đổi chế độ Sáng/Tối | User |
| UC-07 | Thêm giao dịch (Thu/Chi) | User |
| UC-08 | Sửa giao dịch | User |
| UC-09 | Xóa giao dịch | User |
| UC-10 | Xem danh sách giao dịch (lọc/tìm kiếm) | User |
| UC-11 | Quản lý danh mục (thêm/sửa/xóa) | User |
| UC-12 | Quản lý ví/tài khoản (thêm/sửa/xóa) | User |
| UC-13 | Chuyển tiền giữa các ví | User |
| UC-14 | Đặt ngân sách theo danh mục | User |
| UC-15 | Nhận cảnh báo vượt ngân sách | System → User |
| UC-16 | Xem báo cáo/biểu đồ chi tiêu | User |
| UC-17 | Xuất báo cáo Excel/PDF | User |
| UC-18 | Đặt nhắc nhở giao dịch định kỳ (bill) | User |
| UC-19 | Đồng bộ dữ liệu đa thiết bị | System |
| UC-20 | Đăng xuất | User |
| UC-21 | Tùy biến giao diện Liquid Glass | User |
| UC-24 | Quét/chọn ảnh hóa đơn khi thêm Chi | User |
| UC-25 | Quản lý mục tiêu tài chính | User |
| UC-26 | Quản lý và thoát nợ (Debt Freedom & Credit Hub) | User |

---

## 3. Chi tiết Use Case (các use case trọng yếu)

### UC-01: Đăng ký tài khoản
```
Actor: User
Precondition: Chưa có tài khoản, có kết nối mạng
Main flow:
  1. User mở app → chọn "Đăng ký"
  2. Nhập Họ tên, Email, Mật khẩu, Xác nhận mật khẩu
  3. Nhấn "Tạo tài khoản"
  4. Hệ thống tạo user trên Firebase Auth + tạo document users/{uid} trên Firestore
     với ví mặc định "Tiền mặt" và danh mục mặc định (Ăn uống, Di chuyển, Lương, v.v.)
  5. Điều hướng vào màn hình Home
Alternative flow:
  A1. Email đã tồn tại → báo lỗi "Email đã được sử dụng"
  A2. Mật khẩu không khớp / yếu hơn 6 ký tự → báo lỗi validation
  A3. Mất mạng → báo lỗi, cho phép retry
Postcondition: Tài khoản được tạo, user đăng nhập, dữ liệu khởi tạo mặc định sẵn sàng
Business rule:
  BR-01: Mật khẩu tối thiểu 8 ký tự, có chữ và số
  BR-02: Mỗi user khi tạo mới tự động có 1 ví mặc định + bộ danh mục mặc định (seed data)
```

### UC-05: Đổi ảnh đại diện
```
Actor: User
Precondition: Đã đăng nhập
Main flow:
  1. Vào Settings/Profile → chạm vào avatar
  2. Chọn nguồn ảnh: Camera / Thư viện
  3. Crop ảnh (tỉ lệ 1:1)
  4. Upload lên Firebase Storage (path: avatars/{uid}.jpg)
  5. Cập nhật photoUrl trong users/{uid} và Firebase Auth profile
  6. Avatar mới hiển thị ngay (cache-busting bằng version/timestamp query param)
Alternative flow:
  A1. Không cấp quyền Camera/Storage → hiện dialog xin quyền, hướng dẫn vào Settings hệ thống nếu bị từ chối vĩnh viễn
  A2. Ảnh quá lớn (>5MB) → tự nén trước khi upload
  A3. Upload lỗi (mất mạng) → giữ ảnh cũ, báo lỗi, cho retry
Postcondition: Avatar mới được lưu và đồng bộ mọi thiết bị
Business rule: BR-03: Ảnh giới hạn tối đa 5MB, nén còn ~500KB trước upload
```

### UC-05A: Đổi tên hiển thị
```
Actor: User
Precondition: Đã đăng nhập
Main flow:
  1. Vào Settings/Profile → chạm tên hoặc mục "Thông tin cá nhân"
  2. Nhập tên hiển thị mới và nhấn "Lưu tên"
  3. Hệ thống cập nhật hồ sơ người dùng
  4. Tên mới hiển thị ngay tại Dashboard, Hồ sơ và avatar chữ cái
Alternative flow:
  A1. Tên trống → báo "Tên người dùng không được để trống"
  A2. Không thể đồng bộ → giữ tên hiện tại và hiển thị lỗi
Postcondition: displayName mới được lưu vào hồ sơ người dùng
```

### UC-06: Chuyển đổi chế độ Sáng/Tối
```
Actor: User
Precondition: Đã đăng nhập (hoặc kể cả trước khi đăng nhập, áp dụng toàn app)
Main flow:
  1. Vào Settings → chọn Giao diện: Sáng / Tối / Theo hệ thống
  2. App áp dụng theme ngay lập tức (không cần khởi động lại)
  3. Lựa chọn lưu vào DataStore (local) — không cần đồng bộ cloud (tùy thiết bị)
Business rule: BR-04: Hiệu ứng Liquid Glass phải tự đổi độ trong/màu overlay tương ứng theme
  sáng (kính sáng, viền trắng mờ) và tối (kính tối, viền sáng nhẹ, glow) để đảm bảo tương phản đọc được.
```

### UC-07: Thêm giao dịch
```
Actor: User
Precondition: Đã đăng nhập, có ít nhất 1 ví và 1 danh mục
Main flow:
  1. Nhấn nút "+" (FAB) trên Home
  2. Chọn loại: Thu / Chi
  3. Nhập Số tiền, chọn Danh mục, chọn Ví, chọn Ngày (mặc định hôm nay), Ghi chú (optional), Ảnh hóa đơn (optional)
  4. Nhấn Lưu
  5. Hệ thống tạo document transactions/{id}, cập nhật số dư ví (wallet.balance) trong 1 transaction (Firestore batch/transaction để đảm bảo toàn vẹn)
  6. Nếu giao dịch Chi thuộc danh mục có ngân sách → kiểm tra ngưỡng cảnh báo (xem UC-15)
Alternative flow:
  A1. Số tiền = 0 hoặc âm → validation lỗi
  A2. Số dư ví không đủ (nếu bật chế độ chặn âm) → cảnh báo, cho phép user xác nhận vẫn lưu (không chặn cứng)
Postcondition: Giao dịch được lưu, số dư ví cập nhật, đồng bộ realtime các thiết bị khác
Business rule:
  BR-05: Số tiền dương, tối đa 15 chữ số (VND)
  BR-06: Xóa/sửa giao dịch phải cập nhật lại số dư ví tương ứng (cộng/trừ ngược)
```

### UC-13: Chuyển tiền giữa các ví
```
Actor: User
Precondition: Có ≥ 2 ví, ví nguồn có đủ số dư (khuyến nghị, không bắt buộc)
Main flow:
  1. Vào màn Ví → chọn "Chuyển tiền"
  2. Chọn Ví nguồn, Ví đích, Số tiền, Ghi chú
  3. Xác nhận
  4. Hệ thống tạo 1 cặp giao dịch nội bộ (transfer_out ở ví nguồn, transfer_in ở ví đích) không tính vào báo cáo Thu/Chi tổng
Business rule: BR-07: Giao dịch loại "transfer" không được cộng dồn vào tổng Thu/Chi trong báo cáo,
  chỉ ảnh hưởng số dư ví.
```

### UC-14 & UC-15: Ngân sách & Cảnh báo
```
UC-14 — Đặt ngân sách theo danh mục
Actor: User
Main flow:
  1. Vào Ngân sách → chọn Danh mục → nhập hạn mức (theo tháng)
  2. Lưu vào budgets/{uid}_{categoryId}_{yyyyMM}
Business rule: BR-08: Ngân sách áp dụng theo chu kỳ tháng (reset đầu tháng), không cộng dồn tháng trước.

UC-15 — Nhận cảnh báo vượt ngân sách
Actor: System (Cloud Function trigger on transaction write) → User (FCM)
Main flow:
  1. Khi giao dịch Chi mới được ghi, Cloud Function tính tổng chi trong tháng theo danh mục
  2. Nếu đạt 80% hạn mức → gửi thông báo cảnh báo "sắp vượt ngân sách"
  3. Nếu đạt/vượt 100% → gửi thông báo "đã vượt ngân sách [tên danh mục]"
Business rule: BR-09: Mỗi ngưỡng (80%, 100%) chỉ gửi thông báo 1 lần/chu kỳ tháng, tránh spam.
```

### UC-16 & UC-17: Báo cáo & Xuất file
```
UC-16 — Xem báo cáo/biểu đồ
Actor: User
Main flow:
  1. Vào tab Báo cáo → chọn khoảng thời gian (Tuần/Tháng/Năm/Tùy chọn)
  2. Xem: Pie chart cơ cấu chi theo danh mục, Bar/Line chart Thu-Chi theo thời gian,
     Tổng Thu, Tổng Chi, Số dư ròng
Business rule: BR-10: Báo cáo tính dựa trên transaction.date (ngày giao dịch thực tế), không phải
  ngày tạo record.

UC-17 — Xuất báo cáo Excel/PDF
Actor: User
Main flow:
  1. Từ màn Báo cáo → nhấn "Xuất file" → chọn định dạng (Excel/PDF) + khoảng thời gian
  2. App tổng hợp dữ liệu, sinh file cục bộ (thư mục Downloads) hoặc share qua Intent
Alternative flow: A1. Không có giao dịch trong khoảng chọn → báo "Không có dữ liệu để xuất"
Business rule: BR-11: File Excel gồm 2 sheet (Chi tiết giao dịch, Tổng hợp theo danh mục);
  File PDF là báo cáo tóm tắt có biểu đồ dạng ảnh.
```

### UC-18: Nhắc nhở giao dịch định kỳ (bill)
```
Actor: User
Main flow:
  1. Tạo "Giao dịch định kỳ": số tiền, danh mục, ví, chu kỳ (hàng ngày/tuần/tháng), ngày bắt đầu
  2. Local Notification (AlarmManager/WorkManager) nhắc đúng ngày để user xác nhận nhập giao dịch
     (không tự động tạo giao dịch để tránh sai số nếu user quên hủy)
Business rule: BR-12: Nhắc nhở là thông báo local trên thiết bị (không cần server), nhưng cấu hình
  được đồng bộ cloud để hiển thị nhất quán trên nhiều thiết bị (mỗi thiết bị tự lên lịch local).
```

### UC-19: Đồng bộ dữ liệu đa thiết bị
```
Actor: System
Main flow:
  1. Firestore offline persistence bật mặc định — user thao tác được cả khi offline
  2. Khi có mạng, Firestore tự đồng bộ realtime (listener) giữa các thiết bị cùng tài khoản
Business rule:
  BR-13: Conflict resolution theo chiến lược "last write wins" của Firestore (mặc định) —
  chấp nhận được vì app cá nhân, xác suất ghi đồng thời từ 2 thiết bị thấp.
  BR-14: Cập nhật số dư ví (wallet.balance) PHẢI qua Firestore Transaction để tránh race condition
  khi 2 thiết bị ghi giao dịch gần như đồng thời.
```

---

### UC-21: Tùy biến giao diện Liquid Glass
```
Actor: User
Main flow:
  1. Vào Cài đặt → Phong cách giao diện
  2. Chọn Tối giản hiện đại, Glassmorphism hoặc Gradient năng động
  3. Chọn cường độ kính (Nhẹ/Cân bằng/Rực rỡ), mật độ thẻ (Thoáng/Gọn)
     và bật/tắt hiệu ứng phản hồi khi chạm
  4. Ứng dụng áp dụng ngay trên toàn bộ component dùng chung và lưu lựa chọn bằng DataStore
Business rule: Tùy biến chỉ ảnh hưởng trình bày cục bộ, không thay đổi dữ liệu tài chính hoặc đồng bộ cloud.
```

---

### UC-22: Xem tổng hợp thu nhập theo tháng
```
Actor: User
Main flow:
  1. Từ Dashboard chọn KPI "Thu tháng này"
  2. Chọn tháng cần xem
  3. Hệ thống lọc transaction có type = income theo transaction.date và hiển thị tổng thu,
     bình quân ngày, cao nhất, thấp nhất, số giao dịch và tỷ trọng từng danh mục
  4. User nhấn "+ Thêm" để mở form giao dịch ở trạng thái Thu nhập
Business rule: Các giao dịch transfer_in không được tính là thu nhập (BR-07).
```

### UC-23: Xem tổng hợp chi tiêu theo tháng
```
Actor: User
Main flow:
  1. Từ Dashboard chọn KPI "Chi tháng này"
  2. Chọn tháng cần xem
  3. Hệ thống lọc transaction có type = expense theo transaction.date và hiển thị tổng chi,
     so sánh tháng trước, tỷ trọng danh mục, chi theo ngày và danh sách giao dịch
  4. User nhấn "+ Thêm" để mở form giao dịch ở trạng thái Chi tiêu
Business rule: transfer_out không được tính là chi tiêu (BR-07).
```

### UC-24: Quét/chọn ảnh hóa đơn
```
Actor: User
Main flow:
  1. Nhấn FAB "+" → Quét hóa đơn
  2. Chụp ảnh bằng camera hoặc chọn ảnh có sẵn
  3. App mở form Thêm Chi với ảnh đã đính kèm để user kiểm tra, nhập số tiền, danh mục và ví
  4. Khi Lưu, ảnh được tải lên receipts/{uid}/ và URL được ghi cùng giao dịch
Business rule: Ảnh hóa đơn không tự tạo giao dịch; user luôn xác nhận dữ liệu trước khi lưu.
```

### UC-25: Quản lý mục tiêu tài chính & Nạp/Rút tích lũy
```
Actor: User
Main flow:
  1. Nhấn FAB "+" → Thêm mục tiêu hoặc mở Mục tiêu từ Trang chủ / Hồ sơ
  2. Nhập tên, số tiền cần đạt, hạn hoàn thành, danh mục, khoản tích lũy mỗi tháng và ảnh tùy chọn
  3. Lưu mục tiêu; danh sách cập nhật realtime và đồng bộ đa thiết bị khi dùng Firebase
  4. Người dùng có thể nhấn [Nạp tiền] trên thẻ Mục tiêu: Chọn ví nguồn, nhập số tiền nạp -> Hệ thống trừ tiền ví nguồn, tăng savedAmount của Mục tiêu và ghi giao dịch EXPENSE (danh mục "savings") vào Sổ cái nguyên tử (Firestore Transaction).
  5. Người dùng có thể nhấn [Rút tiền] trên thẻ Mục tiêu: Chọn ví đích, nhập số tiền rút -> Hệ thống giảm savedAmount của Mục tiêu, cộng tiền vào ví đích và ghi giao dịch INCOME (danh mục "savings") vào Sổ cái nguyên tử.
Business rule:
  BR-GOAL-01: Thao tác nạp/rút tiền mục tiêu tài chính bắt buộc thực thi qua Firestore Atomic Transaction, đảm bảo tính toàn vẹn giữa số dư ví nguồn/đích, số tiền tích lũy của mục tiêu và sổ cái dòng tiền.
```

### UC-26: Quản lý và thoát nợ (Debt Freedom & Credit Hub)
```
Actor: User
Main flow:
  1. Người dùng mở mục "Quản lý nợ & Tín dụng" từ Cài đặt hoặc Quick Action
  2. Xem tổng quan dư nợ hiện tại, tiến độ thanh toán, và biểu đồ Burndown Chart mô phỏng lộ trình giảm nợ
  3. Chọn chiến lược trả nợ tối ưu (Debt Snowball hoặc Debt Avalanche), kéo thanh trượt điều chỉnh tiền trả thêm (Extra Monthly Payment) để xem ngày sạch nợ và tiền lãi tiết kiệm được cập nhật realtime
  4. Tạo/Sửa các khoản nợ (Thẻ tín dụng, Vay ngân hàng, Vay cá nhân, Trả góp/BNPL)
  5. Nhấn [Trả nợ] trên từng khoản nợ: Chọn ví nguồn, nhập số tiền trả gốc và lãi, hệ thống thực hiện trừ số dư ví và giảm dư nợ nguyên tử (Firestore Atomic Transaction) với danh mục mặc định "debt_payment"
Business rule:
  BR-DEBT-01: Phân loại khoản nợ theo Thẻ tín dụng (hạn mức, sao kê, đến hạn, APR), Vay ngân hàng/cá nhân, và Trả góp.
  BR-DEBT-02: Mô phỏng Snowball (ưu tiên nợ nhỏ nhất trước) và Avalanche (ưu tiên APR cao nhất trước) kèm tính toán tiền lãi tiết kiệm và ngày sạch nợ.
  BR-DEBT-03: Mọi giao dịch trả nợ bắt buộc chạy qua Firestore Transaction (Trừ số dư ví -> Giảm dư nợ khoản vay -> Tạo giao dịch chi tiêu category "debt_payment" -> Ghi lịch sử trả nợ).
```

---

## 4. Ma trận Business Rule tổng hợp

| Mã | Mô tả |
|----|-------|
| BR-01 | Mật khẩu ≥ 8 ký tự, có chữ + số |
| BR-02 | User mới tự động có ví mặc định + danh mục mặc định (seed) |
| BR-03 | Ảnh avatar giới hạn 5MB, tự nén |
| BR-04 | Liquid Glass overlay phải đổi độ trong/màu theo theme sáng/tối |
| BR-05 | Số tiền giao dịch > 0 |
| BR-06 | Sửa/xóa giao dịch phải cập nhật lại số dư ví |
| BR-07 | Giao dịch transfer không tính vào tổng Thu/Chi báo cáo |
| BR-08 | Ngân sách theo chu kỳ tháng, không cộng dồn |
| BR-09 | Cảnh báo ngân sách chỉ gửi 1 lần/ngưỡng/chu kỳ |
| BR-10 | Báo cáo tính theo ngày giao dịch, không theo ngày tạo |
| BR-11 | Export Excel 2 sheet, PDF có biểu đồ |
| BR-12 | Nhắc bill là local notification, cấu hình sync cloud |
| BR-13 | Conflict resolution: last-write-wins (Firestore default) |
| BR-14 | Cập nhật số dư ví bắt buộc qua Firestore Transaction |
| BR-DEBT-01 | Phân loại công nợ: Thẻ tín dụng, Vay ngân hàng/cá nhân, Trả góp/BNPL |
| BR-DEBT-02 | Thuật toán mô phỏng thoát nợ Snowball (nợ nhỏ trước) & Avalanche (lãi cao trước) |
| BR-DEBT-03 | Thanh toán nợ nguyên tử qua Firestore Transaction (trừ ví, giảm nợ, ghi sổ cái) |
| BR-GOAL-01 | Nạp/Rút tích lũy mục tiêu tài chính nguyên tử qua Firestore Transaction (trừ/cộng ví, cập nhật mục tiêu, ghi sổ cái) |

---

## 5. Yêu cầu phi chức năng (Non-functional Requirements)

| Nhóm | Yêu cầu |
|------|---------|
| Hiệu năng | Danh sách giao dịch dùng phân trang (paging 20-30 item/lần), hiệu ứng Liquid Glass (blur) phải giữ ≥ 50-60fps trên thiết bị tầm trung (dùng `RenderEffect`/`Modifier.blur` phần cứng-tăng-tốc, tránh blur bằng vẽ tay tốn CPU) |
| Bảo mật | Firestore Security Rules chặn user chỉ đọc/ghi dữ liệu của chính mình (`request.auth.uid == resource.data.ownerId`); mật khẩu không lưu client; tùy chọn khóa app bằng biometric/PIN `[Cần xác nhận]` |
| Khả năng mở rộng | Cấu trúc Firestore theo subcollection `users/{uid}/transactions` để scale tốt, tránh document 1MB limit |
| Offline-first | App phải dùng được khi mất mạng (Firestore offline cache), đồng bộ lại khi có mạng |
| Khả năng tương thích | Hỗ trợ Android 8.0 (API 26)+; hiệu ứng blur thời gian thực (RenderEffect) chỉ có từ Android 12 (API 31) — thiết bị cũ hơn dùng fallback (lớp overlay bán trong suốt + gradient, không blur động) |
| Giám sát | Tích hợp Firebase Crashlytics + Analytics để theo dõi lỗi và hành vi sử dụng |
| Đa ngôn ngữ | V1: chỉ Tiếng Việt. `[Cần xác nhận]` nếu cần thêm English |
| Accessibility | Contrast tối thiểu WCAG AA cho text trên nền kính mờ (glass), tránh chữ mờ khó đọc |
