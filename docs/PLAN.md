# PLAN — Finlux

> Giả định team 1 dev (hoặc 1 dev + hỗ trợ AI coding agent). Điều chỉnh lại nếu team khác.
> `[Cần xác nhận]`: ngày bắt đầu thực tế để điền deadline cụ thể.

## Audit 09/2026 — Fix Roadmap
- [IN PROGRESS 2026-09-07] Post-audit remediation R-00..R-09 theo
  `audit-2026-09-fix-roadmap/13_POST_AUDIT_REMEDIATION_PLAN.md`.
- [LOCAL DONE 2026-09-07] R-00..R-05: khóa Wallet/Goal/Debt/Deal/Budget invariants, server cascade,
  Storage Rules suite; 50 emulator tests và 308 Android tests pass, debug APK build thành công.
- [PARTIAL] R-06: audit cluster Transfer/Wallet History/Prism History đã sạch hardcode; full-app theme scan và visual matrix còn mở.
- [BLOCKED] Production release/sign-off: chờ full theme parity, reconciliation production, CI/deploy,
  production signing và release-device UAT.
- [DONE 2026-09-07] PR-01 Salary Cycle shared contract: đồng bộ path/field Android–Functions,
  resolver timezone `[start, endExclusive)`, mặc định payday 25 và fixture contract dùng chung.
- [DONE 2026-09-07] PR-02 Budget period schema/rules/functions: Timestamp mới + legacy read,
  khóa aggregate server-owned, reconcile khi transaction/budget thay đổi và Rules emulator tests.
- [PRELIMINARY] PR-03..PR-10 đã có implementation local nhưng phải qua re-audit remediation trước khi xác nhận `[DONE]`.

## Sprint 0 — Setup (3-4 ngày)
- Khởi tạo project Android Studio, cấu hình Hilt, Navigation Compose
- Setup Firebase project (Auth, Firestore, Storage, FCM, Functions), `google-services.json`
- Build Design System nền tảng: color tokens (light/dark), `LiquidGlassSurface`, `GlassCard`, `GlassTopBar`
- Setup Firestore Security Rules cơ bản (DATA_SPEC mục 3)

## Sprint 1 — Auth + Profile + Theme (1 tuần)
- UC-01 Đăng ký, UC-02 Đăng nhập, UC-03 Google Sign-In, UC-04 Quên mật khẩu
- Seed dữ liệu mặc định khi tạo user (ví + danh mục mặc định)
- UC-05 Đổi ảnh đại diện (Storage upload + crop)
- UC-06 Theme Sáng/Tối + áp dụng Liquid Glass theo theme
- **Deliverable:** đăng nhập được, đổi avatar, đổi theme mượt

## Sprint 2 — Giao dịch + Danh mục + Ví (1-1.5 tuần)
- UC-07/08/09/10 CRUD + danh sách giao dịch (lọc/tìm kiếm)
- [DONE 2026-08-28] History: kỳ tài chính hiện tại/kỳ trước, search không dấu, khoảng tiền, badge filter và collapse transfer double-entry thành một dòng logic
- UC-11 Quản lý danh mục
- UC-12 Quản lý ví, UC-13 Chuyển tiền giữa ví
- [DONE 2026-08-27] Danh mục 65 tổ chức VietQR + ví bổ sung, logo offline, tìm theo tên/mã/BIN dùng chung mọi theme
- Đảm bảo BR-06, BR-14 (Firestore Transaction cho balance)
- **Deliverable:** dùng được vòng đời giao dịch đầy đủ, số dư ví chính xác

## Sprint 3 — Ngân sách + Báo cáo + Export (1-1.5 tuần)
- UC-14 Đặt ngân sách theo danh mục
- Cloud Functions: `onTransactionWrite`, `checkBudgetThreshold`, `monthlyBudgetReset`
- UC-15 Cảnh báo vượt ngân sách (FCM)
- UC-16 Báo cáo/biểu đồ (Pie + Bar/Line)
- [DONE 2026-08-28] Report semantic: tách dòng tiền còn lại/giữ lại/đóng góp mục tiêu, loại CARD khỏi tài sản, CTA empty state và insight dựa dữ liệu
- UC-17 Xuất Excel/PDF
- **Deliverable:** ngân sách hoạt động, cảnh báo đúng ngưỡng, xuất file thành công

## Sprint 4 — Nhắc nhở + Đồng bộ + Polish (1 tuần)
- UC-18 Nhắc nhở giao dịch định kỳ (Exact Alarm `setAlarmClock`, Zero Time Drift `ReminderUtils`, System Notifications)
- UC-19 Đồng bộ đa thiết bị — `ReminderSyncObserver` tự động nạp lịch báo thức khi đăng nhập/mở app
- [DONE 2026-08-28] Nâng cấp Exact Alarm (setAlarmClock), Zero Time Drift Engine và Multi-Device Sync cho Nhắc nhở định kỳ (UC-18, UC-19)
- Hoàn thiện animation/hiệu ứng Liquid Glass toàn app, empty/error states
- [DONE 2026-08-28] Vuốt bám ngón tay giữa 4 tab chính với spring trả vị trí, edge resistance và bottom dock cố định
- [DONE 2026-08-28] Home/Lịch sử Prism dùng chung thẻ nhóm giao dịch theo bố cục menu Hồ sơ
- [DONE 2026-08-28] Header Home Prism dạng CLEAR Liquid Glass capsule: avatar trái, tên co giãn, badge thông báo số lượng
- [DONE 2026-08-28] Carousel Home Thu/Chi/Dòng tiền: thẻ lớn dễ đọc, tab/vuốt tay, tự chuyển vòng mỗi 10 giây
- [DONE 2026-08-29] Hotfix header Home: khóa 68dp và sửa lớp quang học Liquid Glass không tham gia đo wrap-content
- [DONE 2026-08-29] Đồng bộ lại Home FinLux Prism: bỏ Liquid Glass khỏi header, carousel KPI và thẻ phân tích
- [DONE 2026-08-29] Gộp hero và KPI thành carousel Tổng quan tài chính 4 thẻ; mỗi thẻ có gradient và hình nền vector riêng
- [DONE 2026-08-29] Mở rộng carousel thành hero 196dp, bỏ header/tab rời, tăng typography và đặt chỉ báo trang bên trong thẻ
- [DONE 2026-08-27] Logo thương hiệu nền trắng + ẩn thẻ kỳ tài chính Home + cân giữa bộ ba KPI Prism
- [DONE 2026-08-27] Tăng tương phản Home Prism, KPI Liquid Glass co chữ thích ứng và chú giải chart hai dòng
- Kiểm tra accessibility, contrast trên nền kính mờ
- Crashlytics + Analytics tích hợp

## Sprint 5 — UAT & Release (3-5 ngày)
- Test case đầy đủ theo `BA_SPEC.md` (xem `TEST_PLAN.md` nếu cần chi tiết hơn)
- Fix bug UAT
- Chuẩn bị Play Store listing, build release (signing, obfuscation ProGuard/R8)
- Go-live

---

## Rủi ro cần lưu ý
| Rủi ro | Mức độ | Giải pháp |
|--------|--------|-----------|
| Hiệu ứng blur real-time (RenderEffect) chỉ chạy tốt từ Android 12 (API 31) — thiết bị cũ hơn giảm trải nghiệm | Trung bình | Áp dụng fallback overlay tĩnh như UI_SPEC mục 0 đã nêu, test kỹ trên thiết bị thật tầm trung/thấp |
| Cloud Functions cần thời gian phản hồi để tính ngân sách → cảnh báo có thể trễ vài giây | Thấp | Chấp nhận được cho use case cá nhân; nếu cần realtime tuyệt đối, cân nhắc tính client-side kèm double-check server |
| Firestore chi phí tăng theo số lượng đọc/ghi nếu không tối ưu listener | Trung bình | Dùng pagination, hạn chế listener toàn collection không cần thiết |
| Google Play yêu cầu Data Safety form đầy đủ do có Auth + dữ liệu tài chính cá nhân | Thấp | Chuẩn bị Privacy Policy + khai báo Data Safety trước khi submit |
