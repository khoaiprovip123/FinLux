# AGENTS.md — Hướng dẫn cho AI Coding Agent (Claude Code / Cursor / Antigravity...)

## Bối cảnh
Đây là project Android **Finlux** — quản lý thu chi cá nhân, Kotlin + Jetpack Compose + Firebase,
giao diện Liquid Glass. Đọc `CONTEXT.md`, `BA_SPEC.md`, `UI_SPEC.md`, `DATA_SPEC.md` trước khi code.

## Nguyên tắc bắt buộc
1. **Không bịa nghiệp vụ.** Nếu yêu cầu chưa có trong `BA_SPEC.md`/`UI_SPEC.md`, dừng lại hỏi hoặc
   ghi `// TODO: [Cần xác nhận] ...` thay vì tự suy diễn.
2. **Tuân thủ Clean Architecture** đã định nghĩa trong `CONTEXT.md` — không viết logic nghiệp vụ
   trực tiếp trong Composable hoặc trong lớp data.
3. **Mọi thao tác ghi ảnh hưởng số dư ví PHẢI dùng Firestore Transaction** (BR-06, BR-14) — không
   dùng `set()`/`update()` rời rạc cho amount + balance.
4. **Component Liquid Glass dùng chung** từ `core/designsystem` — không tự tạo blur/glass riêng lẻ
   trong từng màn hình.
5. **Theme sáng/tối** đọc từ `ThemePreferenceRepository` (DataStore), áp dụng bằng
   `CompositionLocalProvider` ở root — không hard-code màu theo `isSystemInDarkTheme()` rải rác.
6. **Test:** mỗi UseCase quan trọng (Add/Edit/Delete Transaction, Budget check) cần unit test kèm theo.
7. **Không commit khóa bí mật** (`google-services.json` thật, service account key) — dùng file mẫu
   `.example` và thêm vào `.gitignore`.

## Thứ tự triển khai đề xuất
1. Design system (theme, LiquidGlassSurface, GlassCard, GlassTopBar, GlassBottomNav)
2. Auth module (Login/Register/Google Sign-In) + Firestore seed data khi tạo user mới
3. Home + Transaction CRUD (theo UC-07, UC-08, UC-09)
4. Category + Wallet module (UC-11, UC-12, UC-13)
5. Budget module + Cloud Functions liên quan (UC-14, UC-15)
6. Report + Export Excel/PDF (UC-16, UC-17)
7. Reminder + Notification (UC-18, UC-19)
8. Settings/Profile — avatar, theme switch (UC-05, UC-06)
9. Polish: animation Liquid Glass, empty/error states, accessibility contrast

## Khi sinh code UI
- Luôn tham chiếu đúng section trong `UI_SPEC.md` (ví dụ: "SCREEN: Home / Dashboard") thay vì tự
  thiết kế lại bố cục.
- Giữ đúng tên field/action đã liệt kê để đồng bộ với `BA_SPEC.md`.

## Khi sinh code data layer
- Đúng path Firestore trong `DATA_SPEC.md` mục 1 (subcollection dưới `users/{uid}`).
- Security Rules tham khảo `DATA_SPEC.md` mục 3, viết đầy đủ trước khi release (không để rule mở `allow read, write: if true`).

## Cập nhật tài liệu
Mỗi khi thay đổi phạm vi/nghiệp vụ trong lúc code, cập nhật lại `BA_SPEC.md`/`UI_SPEC.md` tương ứng
và ghi log vào `CHANGELOG.md` — không để code và tài liệu lệch nhau.
