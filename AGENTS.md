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

## 📋 QUY TRÌNH QUẢN LÝ TÀI LIỆU CHUẨN (Document Management SOP)

### HANDOVER_LOG.md — Bắt buộc ghi 2 bước
**PRE-EXECUTION** (Trước khi gõ code):
- Tạo mục task mới trong `HANDOVER_LOG.md`.
- Ghi rõ: Mục tiêu, scope thay đổi, danh sách file dự kiến chỉnh sửa.
- Gán trạng thái `[IN PROGRESS]`.

**POST-EXECUTION** (Sau khi xong):
- Cập nhật kết quả chạy test (số test pass/fail).
- Liệt kê đầy đủ danh sách file đã thực sự chỉnh sửa.
- Đổi trạng thái sang `[DONE]`.

### CHANGELOG.md — Chỉ ghi sau khi build thành công
- Chỉ được ghi nhận thông tin phiên bản release (`[vX.Y.Z]`, ngày tháng, `[Added]`, `[Changed]`, `[Fixed]`)
  **SAU KHI** đã chạy `gradlew testDebugUnitTest` pass 100% **VÀ** build APK thành công.
- Không ghi CHANGELOG trước khi test hoàn tất.

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

## 🚀 MANDATORY RELEASE & VERSIONING WORKFLOW
Mỗi khi người dùng yêu cầu build release, đóng gói APK hoàn chỉnh, hoặc chuẩn bị commit tính năng mới:
1. **AUTO VERSION BUMP:**
   - Tự động kiểm tra `versionCode` và `versionName` trong `app/build.gradle.kts` (hoặc `libs.versions.toml`).
   - Tự động tăng `versionCode` lên +1.
   - Cập nhật `versionName` theo chuẩn Semantic Versioning (X.Y.Z) tương ứng với quy mô thay đổi (Patch/Minor/Major).

2. **AUTO CHANGELOG & DOCS SYNC:**
   - Tự động thêm mục phiên bản mới lên đầu file `CHANGELOG.md` theo chuẩn "Keep a Changelog".
   - Tóm tắt ngắn gọn các thay đổi vừa thực hiện vào 3 mục: `[Added]`, `[Changed]`, `[Fixed]`.
   - Cập nhật thông tin version tương ứng trong `HANDOVER_LOG.md`.

3. **VERIFY & COMMIT:**
   - Chạy `gradlew testDebugUnitTest` đảm bảo 100% PASS trước khi build.
   - Commit thay đổi với message: `bump(release): vX.Y.Z - [Tóm tắt ngắn]`.

