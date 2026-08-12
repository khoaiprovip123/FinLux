# CHANGELOG — Finlux

## 0.7.0 - 2026-08-12

- Thiết kế lại toàn bộ luồng Đăng nhập (Login) và Đăng ký (Register) bám sát 100% ảnh giao diện mẫu.
- Thêm header gradient kết hợp logo FinLux, tiêu đề, mô tả và minh họa 3D glass (Ví tiền & Hồ sơ).
- Form container bo góc 32.dp, tích hợp tab chuyển đổi Đăng nhập / Đăng ký mượt mà.
- Bổ sung ô nhập Số điện thoại, checkbox "Ghi nhớ đăng nhập", đồng ý Điều khoản & Chính sách bảo mật.
- Bổ sung thanh đo độ mạnh mật khẩu 4 cấp độ (Yếu / Trung bình / Mạnh / Rất mạnh) với chỉ báo màu sắc linh hoạt.
- Thêm các nút đăng nhập mạng xã hội (Google, Apple, Facebook) và nút Gradient chính với icon mũi tên.

## 0.6.3 - 2026-08-12


- Căn chỉnh lại màn Báo cáo sát ảnh tham chiếu: tiêu đề, bộ chọn Tháng/Quý/Năm/Tùy chọn và các panel compact.
- Tổng quan hiển thị Thu nhập/Chi tiêu/Tiết kiệm cùng tỷ lệ so với kỳ trước lấy từ dữ liệu thực.
- Biểu đồ thu–chi chuyển thành hai đường có điểm dữ liệu, đường focus và tooltip; phân bổ chi dùng treemap bất đối xứng.
- Báo cáo theo ví bổ sung icon, số tiền, tỷ trọng và thanh tiến độ; toàn màn hình tiếp tục kế thừa theme chung của ứng dụng.

## 0.6.2 - 2026-08-12

- Gỡ theme navy bị ép riêng trong màn Báo cáo; mọi route giờ dùng chung theme từ `FinluxRoot`.
- Panel, segmented control, chữ, viền và bottom navigation của Báo cáo tự thích ứng sáng/tối và phong cách đã chọn.
- Giữ màu xanh/đỏ/tím như màu dữ liệu và màu nhấn, không dùng chúng để thay đổi chế độ nền của màn hình.

## 0.6.1 - 2026-08-12

- Dựng lại ba màn Chi tiêu, Báo cáo và Hồ sơ theo tỷ lệ, màu sắc và phân cấp của ảnh chuẩn.
- Chi tiêu dùng hero coral riêng, card gọn, danh mục có cả số tiền/tỷ trọng và biểu đồ ngày có trục ngày.
- Báo cáo dùng nền navy chuyên biệt, segmented control, panel viền xanh, line chart và treemap gần reference.
- Hồ sơ chuyển hero người dùng và tổng tài sản lên đầu, bốn thẻ tính năng, menu quản lý; phần Giới thiệu chuyển xuống cuối.
- Chuẩn hóa panel compact dùng chung trong `core/designsystem`; đổi icon Hồ sơ sang biểu tượng người dùng.

## 0.6.0 - 2026-08-12

- Bổ sung màn Chi tiêu theo tháng: hero tổng chi, so sánh tháng trước, donut danh mục,
  biểu đồ chi từng ngày và danh sách giao dịch gần đây từ repository thật.
- Thẻ "Chi tháng này" trên Dashboard mở trực tiếp màn Chi tiêu.
- Thiết kế lại Báo cáo: kỳ Tháng/Quý/Năm/Tùy chọn, tổng quan Thu–Chi–Tiết kiệm,
  biểu đồ hai đường xu hướng, phân bổ chi tiêu dạng ô tỷ trọng và hoạt động theo ví.
- Thiết kế lại Hồ sơ: hero gradient, avatar có thể thay đổi, thẻ truy cập nhanh và danh sách quản lý;
  giữ phần Giới thiệu FinLux ở trên cùng cùng toàn bộ tùy biến giao diện hiện có.

## 0.5.5 - 2026-08-12

- Sửa triệt để vuốt từ Báo cáo sang Cài đặt bằng cách quan sát tọa độ ở pha đầu của pointer event.
- Màn hình bám 62% quãng đường ngón tay, tối đa 32% chiều rộng, thay vì chỉ phản hồi rất nhẹ.
- Hạ ngưỡng đổi trang và bổ sung nhận diện flick nhanh để thao tác ngắn nhưng dứt khoát vẫn có hiệu lực.

## 0.5.4 - 2026-08-12

- Thêm phản hồi vuốt bám theo ngón tay, thu phóng rất nhẹ và ánh sáng gradient ở cạnh màn hình.
- Chuyển cảnh giữa các tab chính dùng spring có giảm chấn, fade và chiều sâu thay cho trượt tuyến tính.
- Cử chỉ không đủ ngưỡng tự nảy về vị trí cũ; vẫn phân biệt với thao tác cuộn dọc.

## 0.5.3 - 2026-08-12

- Sửa vuốt từ Báo cáo sang Hồ sơ/Cài đặt khi thao tác bắt đầu trên biểu đồ hoặc vùng cuộn ngang.
- Root quan sát cử chỉ mà không chiếm sự kiện của nội dung; thêm ngưỡng phân biệt vuốt ngang và cuộn dọc.
- Bổ sung test cho luồng Báo cáo → Cài đặt và cử chỉ kéo chéo.

## 0.5.2 - 2026-08-12

- Chuyển thẻ "Chi tiêu theo danh mục" lên trước danh sách "Giao dịch gần nhất" trên Dashboard.

## 0.5.1 - 2026-08-12

- Xóa cụm "Lối tắt nhanh" khỏi Trang chủ để Dashboard gọn và bám sát mẫu hơn.
- Giữ đường vào màn Thu nhập bằng thao tác chạm trực tiếp vào KPI "Thu tháng này".

## 0.5.0 - 2026-08-12

- Thiết kế lại Dashboard theo hệ Material 3 fintech: header logo/avatar, hero tổng tài sản,
  ba KPI cân đều, lối tắt nhanh, danh sách giao dịch và donut chi tiêu theo danh mục.
- Nâng màn Ví thành tab chính: tổng số dư, lọc 6 nhóm ví, tỷ trọng từng ví, vuốt sửa/xóa,
  chuyển tiền trực tiếp và CTA thêm ví ở cuối danh sách.
- Bổ sung màn Thu nhập theo tháng với 4 chỉ số, phân tích theo nguồn thu và danh sách giao dịch.
- Bổ sung menu tạo nhanh từ FAB giữa thanh điều hướng: Thu, Chi, Chuyển tiền và nhập khoản chi từ hóa đơn.
- Mở rộng loại ví `EWALLET`, `INVESTMENT`; bổ sung dữ liệu mẫu cho nhiều ngân hàng, ví điện tử,
  đầu tư và các nguồn thu như freelance, lãi, hoàn tiền, cổ tức.
- Giữ giao diện sáng/tối và ba phong cách người dùng đã chọn thông qua design system dùng chung.

## 0.4.3 - 2026-08-12

- Thêm vuốt ngang hai chiều giữa Trang chủ, Báo cáo, Ngân sách và Cài đặt.
- Thêm chuyển cảnh trượt theo hướng điều hướng, đồng thời giữ cách chuyển trang bằng bottom navigation.
- Thiết kế lại icon điều hướng theo ba phong cách: nét xanh tối giản, orb kính mờ và ô gradient năng động.
- Làm mới nút thêm trung tâm bằng gradient và rim-light thích ứng chủ đề.

## 0.4.2 - 2026-08-12

- Sắp xếp lại Cài đặt: Giới thiệu FinLux → Hồ sơ người dùng → Phong cách giao diện.
- Hoàn thiện đặt/đổi ảnh đại diện từ thư viện hoặc camera ngay trên thẻ hồ sơ.
- Ảnh được center-crop tỉ lệ 1:1, giới hạn 1024 px và nén hướng tới 500 KB trước khi lưu.
- Chế độ demo lưu avatar cục bộ; Firebase lưu `avatars/{uid}.jpg`, cập nhật Firestore và Firebase Auth.

## 0.4.1 - 2026-08-12

- Thiết kế logo FinLux mới kết hợp chữ F, ví/thẻ và nét tăng trưởng bằng gradient xanh-tím-cyan.
- Đồng bộ logo mới cho icon ứng dụng, cửa sổ khởi động, Splash và màn hình Đăng nhập.
- Thêm thẻ “Giới thiệu FinLux” trong Cài đặt với logo, slogan, phiên bản và mô tả ứng dụng.

## 0.4.0 - 2026-08-12

- Bổ sung ba phong cách giao diện theo ảnh tham chiếu: Tối giản hiện đại, Glassmorphism và Gradient năng động.
- Đồng bộ token màu, nền, viền kính, thẻ, nút thêm và bottom navigation theo phong cách đang chọn.
- Thiết kế lại Splash, Đăng nhập và Dashboard để tự thích ứng với từng phong cách.
- Thêm bộ chọn phong cách trực quan trong Cài đặt; lựa chọn được lưu cục bộ bằng DataStore.
- Giữ tùy chỉnh cường độ kính, mật độ thẻ, hiệu ứng chạm và chế độ Sáng/Tối/Hệ thống.

## 0.3.2 - 2026-08-12

- Căn chỉnh lại Dashboard theo đúng bảng giao diện FinLux do chủ dự án cung cấp.
- Thay aura động mạnh bằng nền trắng-xanh sạch; thu gọn header, hero, KPI và bottom navigation.
- Đưa giao dịch gần nhất thành danh sách phẳng, tăng mật độ thông tin và độ giống reference.
- Giữ Liquid Glass nhẹ ở KPI/nav cùng water highlight trong hero để không làm giảm độ đọc.

## 0.3.1 - 2026-08-12

- Vẽ lại toàn bộ Dashboard theo Liquid Glass iOS 26: aura blur động, kính trong nhiều lớp,
  rim-light khúc xạ, water highlight và phản hồi chạm đàn hồi.
- Thêm ẩn/hiện toàn bộ số dư trên Home, thao tác nhanh Thu/Chi/Chuyển và thẻ thêm ví.
- Đồng bộ chiều cao metric/wallet cards, làm trong hơn bottom navigation và tăng chiều sâu thị giác.

## 0.3.0 - 2026-08-12

- Nâng cấp Liquid Glass: gradient rim-light, glow theo cường độ, hiệu ứng nhấn thẻ và mật độ thẻ tùy chỉnh.
- Sửa liên kết “Ví của bạn/Quản lý” trên Dashboard; toàn bộ thẻ ví mở màn quản lý đầy đủ.
- Hoàn thiện thêm/sửa/xóa ví, danh mục, ngân sách và nhắc nhở; bổ sung chuyển tiền nguyên tử giữa hai ví.
- Nhắc nhở dùng AlarmManager, Notification Channel và xin quyền thông báo trên Android 13+.
- Ngân sách hỗ trợ duyệt các tháng trước, về tháng hiện tại và quản lý hạn mức theo từng tháng.
- Form thêm giao dịch có số tiền nhanh, icon danh mục, chọn ví, chọn ngày, ghi chú và phần xác nhận báo cáo.
- Báo cáo hỗ trợ Tuần/Tháng/Năm/Tùy chọn, dòng tiền Thu-Chi, chỉ số trung bình và hoạt động theo ví.
- Danh sách giao dịch hỗ trợ xóa có xác nhận và hoàn số dư qua transaction.
- Bổ sung bộ icon/màu danh mục đa dạng và dữ liệu demo cho ngân sách lịch sử/nhắc nhở.

## [Unreleased]
### Added
- Khởi tạo bộ tài liệu BA/UI/Data spec ban đầu (PROJECT_PROFILE, BA_SPEC, UI_SPEC, DATA_SPEC, CONTEXT, AGENTS, PLAN)
- Khởi tạo Android project Kotlin + Jetpack Compose, Hilt, Navigation Compose, DataStore và Firebase SDK.
- Xây dựng nền tảng Clean Architecture (domain/data/presentation), Liquid Glass design system và theme Sáng/Tối/Hệ thống.
- Thêm luồng Splash, Email Auth, Home, thêm giao dịch, báo cáo, ngân sách, cài đặt, danh mục, ví, thông báo và nhắc nhở.
- Thêm Firebase transaction adapter bảo toàn số dư cho thao tác thêm/sửa/xóa giao dịch (BR-06, BR-14).
- Thêm data source demo để chạy UI khi chưa có cấu hình Firebase và unit test cho Transaction/Budget use case.
- Thêm Firestore/Storage Security Rules ban đầu cùng file cấu hình Firebase mẫu an toàn.
- Làm mới UI phiên bản 0.2.0 theo visual reference FinanceOS: gradient xanh-tím, card sáng tương phản cao,
  Splash/Login mới, Dashboard phân cấp số liệu, Ví, Ngân sách, danh sách giao dịch và Báo cáo trực quan hơn.
- Báo cáo Tuần/Tháng/Năm nay tổng hợp donut theo danh mục và bar chart theo ngày từ transaction repository thật.
- Mở rộng dữ liệu demo cho ví, danh mục, giao dịch và ngân sách để kiểm thử đầy đủ các trạng thái giao diện.

### Changed
- Xác nhận brand color: xanh `#3478F6`, tím `#7758F6`, cyan `#47C8FF` theo ảnh tham chiếu ngày 12/08/2026.
- Nâng version ứng dụng từ `0.1.0` lên `0.2.0` (`versionCode=2`).

### Pending confirmation
- Package/application ID chính thức (đang dùng tạm `com.finlux.app`).
- Phạm vi khóa sinh trắc học vẫn chưa được đưa vào V1.

---
*Cập nhật file này mỗi khi có thay đổi phạm vi, tính năng, hoặc kiến trúc trong quá trình phát triển.*
