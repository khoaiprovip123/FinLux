# PROJECT PROFILE

## Thông tin chung
- **Tên dự án (tạm đặt):** Finlux — Quản lý Thu Chi Cá Nhân *(có thể đổi tên, đây là placeholder)*
- **Loại dự án:** Mobile Android (native)
- **Mô tả 1 dòng:** App Android quản lý thu chi cá nhân, đăng nhập + đồng bộ Cloud, giao diện Liquid Glass (giống iOS), hỗ trợ Sáng/Tối.
- **Người dùng cuối:** Cá nhân dùng để theo dõi thu nhập/chi tiêu hàng ngày, không có vai trò admin/multi-user trong V1.
- **Platform:** Android (minSdk khuyến nghị 26 – Android 8.0), targetSdk mới nhất.
- **Ngôn ngữ / Framework:** Kotlin + Jetpack Compose (Material 3) + Clean Architecture (MVVM).
- **Backend / Data:** Firebase (Authentication, Firestore, Storage, Cloud Messaging, Cloud Functions).
- **Trạng thái:** [x] Đang phát triển — đã có Android project build được, Clean Architecture,
  Liquid Glass nền tảng, Email Auth/demo mode, Home và transaction use case; các tính năng V1 còn lại
  tiếp tục triển khai theo sprint.

## Phạm vi V1 (MVP)

### In scope
1. Đăng ký / Đăng nhập (Email-Password + Google Sign-In) qua Firebase Auth
2. Đổi ảnh đại diện (upload/crop, lưu Firebase Storage)
3. Chế độ Sáng/Tối (Light/Dark, theo hệ thống hoặc chọn thủ công)
4. Hiệu ứng **Liquid Glass** áp dụng toàn app (thanh nav, card, dialog, bottom sheet…)
5. Quản lý giao dịch Thu/Chi (CRUD, gắn danh mục + ví)
6. Danh mục thu/chi (mặc định + tự tạo, icon + màu)
7. Đa ví/tài khoản (tiền mặt, ngân hàng, thẻ…) + chuyển tiền giữa ví
8. Ngân sách theo danh mục (đặt hạn mức, cảnh báo khi gần/vượt ngưỡng)
9. Báo cáo & biểu đồ chi tiêu (theo danh mục, theo thời gian — ngày/tuần/tháng/năm)
10. Nhắc nhở & thông báo (nhắc nhập giao dịch, bill định kỳ, cảnh báo ngân sách) qua FCM + Local Notification
11. Xuất báo cáo Excel/PDF theo khoảng thời gian
12. Đồng bộ dữ liệu nhiều thiết bị (Firestore realtime + offline persistence)

### Out of scope (V1 — để V2 xét sau)
- Ví chia sẻ nhiều người dùng / gia đình (multi-user shared wallet)
- Tự động nhận diện giao dịch từ SMS ngân hàng hoặc OCR hóa đơn
- Widget màn hình chính (home screen widget)
- Kết nối trực tiếp ngân hàng (Open Banking / Account Aggregator)
- Phiên bản iOS / Web
- Gợi ý tài chính bằng AI (insight tự động, dự báo chi tiêu)

> `[Cần xác nhận]`: Tên app chính thức, package name (com.hpc.xxx hoặc cá nhân), có cần khóa sinh trắc học (biometric lock) khi mở app không?

## Tích hợp

| Hệ thống | Loại | Ghi chú |
|----------|------|---------|
| Firebase Authentication | Auth (OAuth Google + Email/Password) | Quản lý phiên đăng nhập, refresh token |
| Cloud Firestore | NoSQL DB realtime | Lưu giao dịch, ví, danh mục, ngân sách |
| Firebase Storage | File storage | Lưu ảnh đại diện |
| Firebase Cloud Messaging (FCM) | Push notification | Nhắc nhở, cảnh báo ngân sách |
| Cloud Functions | Serverless | Tính toán ngân sách định kỳ, trigger thông báo, tổng hợp báo cáo nặng |

## Team & Vai trò

| Vai trò | Người phụ trách | Ghi chú |
|---------|----------------|---------|
| Product Owner | Khoa | |
| BA / Solution | Claude (hỗ trợ) | Xuất spec, review kỹ thuật |
| Tech Lead / Dev | `[Cần xác nhận]` | |
| UI/UX (Liquid Glass) | `[Cần xác nhận]` | Có thể dùng Stitch → Compose |
| QA | `[Cần xác nhận]` | |

## Timeline

| Milestone | Deadline | Status |
|-----------|----------|--------|
| Kickoff / BA hoàn tất | | Hoàn tất |
| Thiết kế UI (Liquid Glass) hoàn tất | | Đã có design system nền tảng; cần polish/QA thiết bị thật |
| Dev Sprint 1 — Auth + Profile + Theme | | Đang phát triển: Email Auth + theme xong; Google/avatar còn lại |
| Dev Sprint 2 — Giao dịch + Danh mục + Ví | | Đang phát triển: add + atomic repository xong; CRUD UI/transfer còn lại |
| Dev Sprint 3 — Ngân sách + Báo cáo + Export | | Đã có màn đọc nền tảng; Cloud Functions/export còn lại |
| Dev Sprint 4 — Thông báo + Đồng bộ + Polish | | Chưa bắt đầu |
| UAT | | Chưa bắt đầu |
| Go-live (Google Play) | | Chưa bắt đầu |

*(Chi tiết sprint xem `PLAN.md`)*
