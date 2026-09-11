# AGENTS.md — Hướng dẫn cho AI Coding Agent (Claude Code / Cursor / Antigravity...)

## Bối cảnh
Đây là project Android **Finlux** — quản lý thu chi cá nhân, Kotlin + Jetpack Compose + Firebase,
giao diện Liquid Glass. Đọc `docs/CONTEXT.md`, `docs/BA_SPEC.md`, `docs/UI_SPEC.md`, `docs/DATA_SPEC.md` trước khi code.

## 🎯 8 NGUYÊN TẮC CỐT LÕI BẮT BUỘC (MANDATORY CORE DIRECTIVES)

1. **ĐỒNG BỘ THEME & MÀU ĐỘNG (THEME CONSISTENCY):**
   - TUYỆT ĐỐI KHÔNG hardcode mã màu tĩnh (Color.Black, Color.White, #000000, #FFFFFF).
   - BẮT BUỘC sử dụng 100% hệ thống màu động từ `LocalFinluxTokens.current` và `MaterialTheme.colorScheme`.
   - BẮT BUỘC sử dụng `FinluxStyleBackdrop` và `containerColor = Color.Transparent` ở các màn hình có nền kính Liquid Glass.
   - Luôn tự kiểm tra độ tương phản và chuyển đổi mượt mà giữa các Theme (Dark Mode, Light Mode, Prism, Classic, Modern).

2. **TÁI SỬ DỤNG & TRÁNH PHÂN MẢNH CODE (REUSABILITY FIRST):**
   - Trước khi tạo component, formatter hay logic mới: BẮT BUỘC phải tìm kiếm trong codebase xem hệ thống ĐÃ CÓ component/tiện ích tương tự hay chưa (`GlassTopBar`, `formatVndAmount`, `toVnd`, `FinluxTokens`, `BiometricHelper`, dialog dùng chung...).
   - ƯU TIÊN tái sử dụng và mở rộng các component dùng chung hiện có thay vì tự vẽ lại từ đầu.

3. **CẬP NHẬT TÀI LIỆU QUY CHUẨN (SOP & DOCUMENTATION COMPLIANCE):**
   - Sau khi hoàn thành bất kỳ tính năng, fix bug hay nâng cấp kiến trúc nào: BẮT BUỘC phải rà soát và cập nhật đồng bộ các file tài liệu đặc tả liên quan (`BA_SPEC.md`, `DATA_SPEC.md`, `CONTEXT.md`, `PLAN.md`, `BACKLOG.md`, `UI_SPEC.md`).
   - BẮT BUỘC ghi log chi tiết các file đã sửa đổi, kết quả test và cập nhật trạng thái `[DONE]` trong `HANDOVER_LOG.md`.

4. **ĐỒNG BỘ FIRESTORE RULES & BẢO TOÀN TRƯỜNG BẤT BIẾN (FIRESTORE SECURITY INTEGRITY):**
   - Mỗi khi thêm/sửa trường dữ liệu trong Firestore Models/Preferences: BẮT BUỘC rà soát và cập nhật `firestore.rules` (whitelist `keys().hasOnly(...)` và logic validate tương ứng).
   - **Quy tắc trường bất biến (Immutability Pattern):** Đối với các trường cấm client tự ý sửa khi `update` (ví dụ `spentAmount`, `balance`...):
     * TUYỆT ĐỐI KHÔNG dùng `|| request.resource.data.<field> is <type>` vì sẽ làm hỏng ràng buộc và cho phép ghi đè tự do.
     * BẮT BUỘC dùng đúng chuẩn: `(!('<field>' in request.resource.data) || request.resource.data.<field> == resource.data.<field>)`.
   - **Pre-commit Gate:** Luôn chạy `npm --prefix functions run check` (TypeScript) và kiểm tra unit test của rules trong `functions/test/firestore.rules.test.ts`.

5. **NGUYÊN TẮC CẤM TẠO CODE TRÙNG LẶP & QUY TRÌNH SÁP NHẬP (ANTI-DUPLICATION & CONSOLIDATION MANDATE):**
   - Tuyệt đối **CẤM TẠO FILE MỚI** trong `component/` nếu chưa rà soát và so sánh đối chiếu với các component hiện có trong project.
   - Khi chuẩn hóa hoặc tạo component dùng chung mới để thay thế component cũ, BẮT BUỘC thực hiện song hành 3 bước trong cùng 1 task:
     * (a) Tạo/củng cố component chuẩn mới trong package quy định.
     * (b) Di chuyển (migrate) 100% các màn hình cũ sang component mới.
     * (c) **XÓA BỎ HOÀN TOÀN** file/component cũ, tuyệt đối không để 2 file cùng tồn tại giải quyết một bài toán trong codebase (chống Zombie Code).

6. **HỢP ĐỒNG FORM CONTROLS TIÊU CHUẨN TOÀN DỰ ÁN (UNIFIED FORM CONTROLS CONTRACT):**
   - Mọi màn hình/sheet có form nhập liệu (`AddTransaction`, `TransferMoney`, `DebtPayment`, `Goals`, `Deals`, `Wallets`, `Budget`...) **BẮT BUỘC 100%** phải kế thừa từ `com.finlux.app.core.designsystem.component.form.FinluxFormControls.kt`:
     * *Thời gian:* Bắt buộc dùng `FinluxDateTimePicker` (chọn cả Ngày VÀ Giờ:Phút, định dạng thông minh "Hôm nay / Hôm qua, dd/MM/yyyy • HH:mm"). Tuyệt đối cấm dùng DatePicker đơn lẻ bỏ sót giờ.
     * *Số tiền:* Bắt buộc dùng `FinluxAmountInput` (định dạng chấm phân cách hàng nghìn, auto-scaling font size chống tràn layout, inline `₫` suffix qua `VndSuffixVisualTransformation`, nút xóa nhanh `[x]`, quick suggestion chips). Cấm tự dựng `BasicTextField` riêng lẻ cho số tiền.
     * *Ghi chú:* Bắt buộc dùng `FinluxNoteInput` (kèm icon badge, giới hạn ký tự và nút xóa nhanh).
     * *Ví & Danh mục:* Bắt buộc dùng `FinluxWalletSelector`, `FinluxTransferWalletPair`, `FinluxWalletPickerBottomSheet`, `FinluxCategoryPickerBottomSheet`.

7. **NGUYÊN TẮC TRỊ TẬN GỐC & CẤM SỬA CHẮP VÁ (ROOT-CAUSE FIRST & ANTI-SINGLE-CASE PATCHING):**
   - Tuyệt đối **CẤM "Single-case Patching"** (thêm padding, spacer, hoặc offset chắp vá để đối phó tạm thời với một lỗi tức thời trên một màn hình đơn lẻ).
   - Khi phát sinh lỗi hiển thị (đè chữ, tràn số, lệch pha cảnh báo): Bắt buộc dừng lại, lập giả thuyết điều tra Root Cause và xử lý tận gốc ở Design System Token / Core Formatter hoặc Domain Invariant để toàn bộ các màn hình khác tự động được bảo vệ.

8. **CHỐT CHẶN 5 ĐIỂM NGHIỆM THU MÁY THẬT QUA ADB (PHYSICAL DEVICE ACCEPTANCE GATE):**
   - Sau khi chạy `gradlew assembleDebug` và nạp APK lên máy thật qua ADB, trước khi báo cáo và xin lệnh commit, agent bắt buộc phải tự đối chiếu qua Checklist Nghiệm Thu 5 Điểm:
     1. [ ] **Theme check:** Màn hình hiển thị chuẩn xác ở cả Dark Mode và Light Mode, không bị chìm chữ hay gãy tương phản Liquid Glass.
     2. [ ] **Large Number check:** Nhập số tiền lớn (từ 100 triệu đến 10 tỷ) chữ tự động thu nhỏ (auto-scaling), không bao giờ đè lên ký hiệu `₫` hay tràn viền.
     3. [ ] **Keyboard (IME) check:** Bàn phím số bật lên không che khuất ô nhập liệu và không che khuất nút hành động (Lưu / Chuyển).
     4. [ ] **Full Flow check:** Thực hiện 1 giao dịch thực tế từ đầu đến cuối trên máy thật, xác nhận số dư ví và sổ cái cập nhật chuẩn xác.
     5. [ ] **Logcat check:** Không có crash ngầm, không có Unhandled Exception hay warning nghiêm trọng trên Logcat.

## Nguyên tắc bắt buộc kỹ thuật
1. **Không bịa nghiệp vụ.** Nếu yêu cầu chưa có trong `docs/BA_SPEC.md`/`docs/UI_SPEC.md`, dừng lại hỏi hoặc
   ghi `// TODO: [Cần xác nhận] ...` thay vì tự suy diễn.
2. **Tuân thủ Clean Architecture** đã định nghĩa trong `docs/CONTEXT.md` — không viết logic nghiệp vụ
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
- Luôn tham chiếu đúng section trong `docs/UI_SPEC.md` (ví dụ: "SCREEN: Home / Dashboard") thay vì tự
  thiết kế lại bố cục.
- Giữ đúng tên field/action đã liệt kê để đồng bộ với `docs/BA_SPEC.md`.

## Khi sinh code data layer
- Đúng path Firestore trong `docs/DATA_SPEC.md` mục 1 (subcollection dưới `users/{uid}`).
- Security Rules tham khảo `docs/DATA_SPEC.md` mục 3, viết đầy đủ trước khi release (không để rule mở `allow read, write: if true`).

## Cập nhật tài liệu
Mỗi khi thay đổi phạm vi/nghiệp vụ trong lúc code, cập nhật lại `docs/BA_SPEC.md`/`docs/UI_SPEC.md` tương ứng
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

