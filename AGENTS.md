# AGENTS.md — Hướng dẫn cho AI Coding Agent & Tech Lead (Claude Code / Cursor / Antigravity...)

## Bối cảnh & Tài liệu bắt buộc
Đây là project Android **Finlux** — quản lý thu chi và hoạch định tài chính cá nhân (Kotlin + Jetpack Compose + Firebase, giao diện Liquid Glass).
Trước khi phân tích hay viết code, agent **BẮT BUỘC PHẢI ĐỌC & ĐỐI SOÁT**:
1. `docs/FINLUX_SYSTEM_ARCHITECTURE_MATRIX.md` (Hiến pháp kiến trúc, Ma trận 16 module & 15 luồng tiền, System Invariants).
2. `docs/BACKLOG.md` (Định vị Roadmap, Phase triển khai, Scope mục tiêu).
3. `docs/BA_SPEC.md` (Ràng buộc nghiệp vụ kế toán, Business Rules BR-01..BR-15).
4. `docs/DATA_SPEC.md` (Data Schema, Subcollections, Trường bất biến, Entity Keys).
5. `docs/UI_SPEC.md`, `docs/CONTEXT.md` (Kiến trúc Clean Architecture & Quy chuẩn UI Liquid Glass).

---

## 🧭 PHẦN I: TƯ DUY TECH LEAD & KHỞI ĐỘNG DỰ ÁN (TECH LEAD KICKOFF)

### 1. ĐỊNH VỊ ROADMAP & PHÂN TÍCH ẢNH HƯỞNG CHÉO (SPEC & CROSS-IMPACT AUDIT)
Mỗi khi tiếp nhận task mới hoặc bug phát sinh, tuyệt đối **KHÔNG** nhảy vào code ngay mà phải mở và đối soát ngay cụm tài liệu nguồn:
- **Vị trí trên Roadmap:** Tra cứu `docs/BACKLOG.md` để xác định hạng mục thuộc Phase nào, mục tiêu cốt lõi là gì, tránh lan man ra ngoài phạm vi.
- **Ràng buộc Nghiệp vụ:** Đối chiếu `docs/BA_SPEC.md` (Business Rules) và `docs/DATA_SPEC.md` (Schema, Subcollections, Keys) để nắm đúng bản chất kế toán và luồng dữ liệu.
- **Quét Ma trận Luồng Tiền:** Tra cứu `docs/FINLUX_SYSTEM_ARCHITECTURE_MATRIX.md` để xác định ngay:
  * Tính năng/bug này can thiệp vào những module nào trong 16 module cốt lõi?
  * Giao dịch phát sinh có tác động chéo đến: Ngân sách (Budget), Chu kỳ lương (Salary Cycle), Dòng tiền tự do (FCF), Báo cáo (Reports), hay Tài sản ròng (True Net Worth)?
- **Xác định Vùng ảnh hưởng (Blast Radius):** Liệt kê danh sách các màn hình, UseCase hoặc Repository gián tiếp chịu tác động trước khi lên kế hoạch chỉnh sửa.

### 2. QUY TRÌNH 4 BƯỚC KHỞI ĐỘNG BẮT BUỘC KHI NHẬN TASK (4-STEP KICKOFF PROTOCOL)
Trước khi chạm vào bất kỳ file code (`.kt`) nào, agent bắt buộc phải thực hiện và báo cáo đủ 4 bước:
- **Bước 1 (Docs & Roadmap Check):** Đối soát Backlog, Specs và Ma trận luồng tiền để có bức tranh toàn cảnh (Bird's-eye View).
- **Bước 2 (Codebase Reality Check):** Rà soát codebase xem logic/component này đã có chưa, đang chạy đúng hay sai so với spec, tránh tạo file trùng lặp (Anti-Zombie Code).
- **Bước 3 (Sanity Check & Phản biện):** Đánh giá mô tả của người dùng có thực sự đúng, đủ, và hợp lý với bản chất kế toán thực tế không? Cảnh báo ngay nếu yêu cầu xung đột với Hiến pháp kiến trúc hoặc tiềm ẩn lỗi dây chuyền.
- **Bước 4 (Đề xuất tối thiểu 2 phương án):**
  * So sánh **Phương án A vs Phương án B**: Nêu rõ Ưu điểm, Nhược điểm, Mức độ rủi ro gãy code (Breaking Changes).
  * Đưa ra **Khuyến nghị (Recommendation)** phương án tối ưu nhất dựa trên tính bền vững dài hạn để người dùng phê duyệt.

### 3. TỐI ƯU HÓA TOKEN & KẾ HOẠCH THỰC THI (TOKEN & PERFORMANCE EFFICIENCY)
- **Tối ưu Token Context:** Nhờ định vị tài liệu từ sớm, chỉ đọc đúng các file liên quan thực sự; không grep/cat file tràn lan gây lãng phí bộ nhớ context; báo cáo cô đọng, đi thẳng vào trọng tâm kỹ thuật.
- **Kế hoạch thực thi chuẩn xác (Implementation Plan):** Sau khi chốt phương án, trình bày kế hoạch ngắn gọn theo mẫu: `[Tên File] -> [Mục tiêu sửa đổi] -> [Hàm/Component liên quan]`.
- **Tối ưu Hiệu năng Runtime:** Mã nguồn viết ra phải triệt tiêu Recomposition thừa trong Jetpack Compose, tối ưu số lượng truy vấn Firestore và bảo vệ bộ nhớ thiết bị.

---

## 🎯 PHẦN II: NGUYÊN TẮC HOẠT ĐỘNG CỐT LÕI (CORE DIRECTIVES)

### 1. QUY TẮC ĐIỀU TRA TRƯỚC KHI CODE (INVESTIGATION-FIRST GATE)
- Đối với mọi task liên quan đến kiến trúc, luồng tiền, liên kết liên module, hoặc bug chưa rõ nguyên nhân:
  * **TUYỆT ĐỐI KHÔNG GÕ CODE NGAY.**
  * Phải dừng lại rà soát codebase, xác định root-cause, lập báo cáo kỹ thuật và kế hoạch triển khai (Implementation Plan) để người dùng phê duyệt trước khi sửa code.

### 2. KỶ LUẬT QUẢN TRỊ GIT (GIT AUTHORIZATION GATE)
- **CẤM TUYỆT ĐỐI TỰ Ý COMMIT HOẶC PUSH:** Agent chỉ được phép chạy `git commit` và `git push` khi nhận được lệnh xác nhận bằng văn bản rõ ràng từ người dùng.
- Mọi thao tác code xong chỉ được dừng lại ở working tree local để phục vụ kiểm thử.

### 3. BẢO VỆ HIẾN PHÁP DÒNG TIỀN (MONEY FLOW INVARIANTS)
- **Single Source Category:** 100% category ID hệ thống phải lấy từ `SystemCategories.kt`. Cấm tuyệt đối dùng string literal tự do (`"food"`, `"savings"`, `"debt_payment"`...).
- **Phân tách ngữ nghĩa kế toán:** Trả nợ gốc (`DEBT_PAYMENT`), Tích lũy mục tiêu (`SAVINGS`), và Xuất vốn đầu tư (`OUTLAY_CAPITAL`) là hoán đổi/dịch chuyển tài sản — **CẤM tính vào `isLivingExpense()`**.
- **Chống tính trùng chi phí (Zero Double-Counting):** Thanh toán sao kê thẻ tín dụng bắt buộc là cặp `TRANSFER_OUT` / `TRANSFER_IN` giữa các ví, không gán category chi tiêu.
- **Rollback vòng đời giao dịch:** Mọi thao tác Sửa/Xóa giao dịch bắt buộc hoàn trả nguyên tử (Rollback) số dư ví và `spentAmount` của Ngân sách theo đúng `periodKey` gốc trước khi áp dụng số liệu mới.
- **Giải mã thời gian thực tế:** `resolvePeriodKey` phải dựa trên `transaction.date` kết hợp `financeTimeZone`, tuyệt đối không dùng `Instant.now()`.

### 4. ĐỒNG BỘ THEME & MÀU ĐỘNG (THEME CONSISTENCY)
- TUYỆT ĐỐI KHÔNG hardcode mã màu tĩnh (`Color.Black`, `Color.White`, `#000000`, `#FFFFFF`).
- BẮT BUỘC sử dụng 100% hệ thống màu động từ `LocalFinluxTokens.current` và `MaterialTheme.colorScheme`.
- BẮT BUỘC sử dụng `FinluxStyleBackdrop` và `containerColor = Color.Transparent` ở các màn hình có nền kính Liquid Glass.

### 5. CẤM TẠO CODE TRÙNG LẶP & QUY TRÌNH SÁP NHẬP (ANTI-DUPLICATION)
- CẤM tạo file mới trong `component/` nếu chưa rà soát đối chiếu với các component hiện có trong project.
- Khi tạo component dùng chung mới để thay thế component cũ, BẮT BUỘC thực hiện song hành 3 bước:
  1. Tạo/củng cố component chuẩn mới trong package quy định.
  2. Di chuyển (migrate) 100% các màn hình cũ sang component mới.
  3. **XÓA BỎ HOÀN TOÀN** file cũ, không để tồn tại 2 file cùng giải quyết một bài toán (Anti-Zombie Code).

### 6. HỢP ĐỒNG FORM CONTROLS TIÊU CHUẨN (UNIFIED FORM CONTROLS CONTRACT)
- Mọi màn hình/sheet có form nhập liệu (`AddTransaction`, `TransferMoney`, `DebtPayment`, `Goals`, `Deals`, `Wallets`, `Budget`...) **BẮT BUỘC 100%** phải kế thừa từ `FinluxFormControls.kt`:
  * *Thời gian:* Dùng `FinluxDateTimePicker` (chọn cả Ngày VÀ Giờ:Phút). Cấm dùng DatePicker đơn lẻ bỏ sót giờ.
  * *Số tiền:* Dùng `FinluxAmountInput` (chấm phân cách hàng nghìn, auto-scaling font size, inline `₫` suffix, clear button `[x]`, quick chips). Cấm tự dựng TextField cho số tiền.
  * *Ghi chú:* Dùng `FinluxNoteInput`.
  * *Ví & Danh mục:* Dùng `FinluxWalletSelector`, `FinluxTransferWalletPair`, `FinluxWalletPickerBottomSheet`, `FinluxCategoryPickerBottomSheet`.

### 7. NGUYÊN TẮC TRỊ TẬN GỐC & CẤM SỬA CHẮP VÁ (ROOT-CAUSE FIRST)
- Tuyệt đối CẤM "Single-case Patching" (thêm padding, spacer, hoặc offset chắp vá để đối phó tạm thời trên một màn hình đơn lẻ).
- Khi phát sinh lỗi hiển thị hoặc lệch pha: Sửa tận gốc ở Design System Token / Core Formatter hoặc Domain Invariant để toàn bộ hệ thống được bảo vệ đồng bộ.

### 8. ĐỒNG BỘ FIRESTORE RULES & BẢO TOÀN TRƯỜNG BẤT BIẾN
- Mỗi khi thêm/sửa trường dữ liệu: BẮT BUỘC cập nhật `firestore.rules` (whitelist `keys().hasOnly(...)`).
- Quy tắc trường bất biến (Immutability Pattern): Dùng chuẩn `(!('<field>' in request.resource.data) || request.resource.data.<field> == resource.data.<field>)`.
- Pre-commit Gate: Chạy `npm --prefix functions run check` và test trong `functions/test/firestore.rules.test.ts`.

---

## 📋 PHẦN III: QUY TRÌNH QUẢN LÝ TÀI LIỆU (DOCUMENTATION SOP)

### HANDOVER_LOG.md — Bắt buộc 2 bước
- **PRE-EXECUTION** (Trước khi code): Tạo mục task, ghi rõ scope, file dự kiến sửa, gán `[IN PROGRESS]`.
- **POST-EXECUTION** (Sau khi code): Ghi kết quả test, danh sách file thực tế đã sửa, đổi sang `[DONE]`.

### CHANGELOG.md — Ghi nhận release
- Chỉ ghi nhận phiên bản release SAU KHI unit test đạt 100% PASS và build APK thành công.

---

## 🚀 PHẦN IV: QUY TRÌNH RELEASE & ĐÓNG GÓI CHUẨN (RELEASE PIPELINE)
Khi nhận lệnh release, đóng gói hoặc bàn giao:
1. **UNIT TEST GATE:** Chạy `.\gradlew.bat testDebugUnitTest` đảm bảo 100% PASS.
2. **VERSION BUMP:** Tăng `versionCode` (+1) và cập nhật `versionName` trong `app/build.gradle.kts`.
3. **CHANGELOG SYNC:** Cập nhật `CHANGELOG.md` và `HANDOVER_LOG.md`.
4. **BUILD APK:** Chạy `.\gradlew.bat assembleDebug`.
5. **ADB DEPLOYMENT:** Nạp APK lên thiết bị (`adb install -r ...`) và bật app (`am start ...`).
6. **PHYSICAL DEVICE ACCEPTANCE:** Tự đối chiếu Checklist 5 điểm:
   - [ ] *Theme check:* Đẹp ở cả Dark Mode và Light Mode.
   - [ ] *Large Number check:* Tiền trăm triệu đến chục tỷ tự động co font, không tràn layout.
   - [ ] *Keyboard IME check:* Bàn phím số không che khuất ô nhập và nút hành động.
   - [ ] *Full Flow check:* Thực hiện 1 giao dịch thực tế, số dư ví và sổ cái nhảy đúng.
   - [ ] *Logcat check:* Không có exception/crash ngầm.
7. **WAIT FOR AUTHORIZATION:** Dừng lại báo cáo để người dùng nghiệm thu thực tế trước khi xin lệnh commit/push.
