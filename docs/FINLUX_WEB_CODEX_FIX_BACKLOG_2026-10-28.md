# FINLUX WEB — CODEX FIX BACKLOG & ATOMIC TASK PLAYBOOK

> **Ngày lập:** 2026-10-28 · **Repository:** [`khoaiprovip123/FinLux`](https://github.com/khoaiprovip123/FinLux) · **Nhánh đối chiếu:** `main` @ `aa8a8e6079e5461b6aaaaef8c555e52fa8e1212b`.
> **Phạm vi:** FinLux Web tại `web/`, các hợp đồng Android/Firebase liên quan (`app/`, `functions/`, `firestore.rules`, `docs/`). **Trạng thái:** audit tĩnh dựa trên code/docs; **chưa chạy build/CI/emulator, chưa xác minh lỗi trên production**. Không coi các tình huống rủi ro là sự cố đã xảy ra.
> **Mục đích:** chia từng vấn đề thành nhiệm vụ nhỏ để điều phối Codex theo thứ tự, tránh đụng chéo và sửa chắp vá. Tài liệu này **không phải lệnh tự động sửa toàn bộ**.

## 0. Luật bắt buộc với mọi Codex agent

1. Đọc `AGENTS.md` và `web/AGENTS.md` trước khi đề xuất thay đổi; dùng Next.js docs của version cài trong `web/node_modules/next/dist/docs/` khi sửa API Next.js 16. Đọc **đúng tài liệu liên quan**: `docs/FINLUX_SYSTEM_ARCHITECTURE_MATRIX.md`, `docs/BA_SPEC.md`, `docs/DATA_SPEC.md`, `docs/UI_SPEC.md`, `docs/BACKLOG.md`, `docs/FINLUX_PRODUCT_MASTER_2026.md`.
2. **Quy trình 4 bước trước code:** (1) docs/roadmap & 15 luồng tiền; (2) codebase reality check; (3) phản biện và blast radius; (4) trình bày **A/B, ưu/nhược/rủi ro**, khuyến nghị, **chờ phê duyệt**. Không nhảy vào sửa chỉ vì task có giải pháp gợi ý.
3. **Một task = một phạm vi logic nhỏ = một PR dự kiến**, kể cả unit/rules tests và cập nhật spec cùng commit sau khi được phép. Nếu task lớn, đề xuất tách tiếp và ghi dependencies, không ghép nhiều task không liên quan.
4. **Cấm tự commit/push/merge/deploy hoặc sửa dữ liệu production** khi chưa có chỉ thị rõ bằng văn bản của người điều phối. Việc tạo file backlog này không cấp quyền mặc định cho các nhiệm vụ code tiếp theo. Không chạy migration dữ liệu thật.
5. **Bất biến tuyệt đối:** single-source category, không double-count chuyển tiền/nợ gốc/tích lũy/hoàn vốn; rollback gốc của transaction; financial period theo `transaction.date` và financeTimeZone; ghi ví/debt/goal nguyên tử; giữ full backward compatibility Android.
6. Nếu đổi nghiệp vụ, sửa `docs/BA_SPEC.md` và `docs/FINLUX_SYSTEM_ARCHITECTURE_MATRIX.md`; nếu đổi schema, sửa `docs/DATA_SPEC.md` và Rules; nếu đổi UI controls, sửa `docs/UI_SPEC.md`/`docs/FORM_COMPONENTS_SPEC.md`. Không triển khai logic mới mà để spec cũ.
7. Không thêm collection/field/chuyển khái niệm thu/chi khi chưa đối chiếu Android. Hướng dẫn `docs/FINLUX_WEB_PLATFORM_IMPLEMENTATION_PLAN.md` có chỗ **lỗi thời** về `website/`; code Next.js đang ở `web/`, `website/` là landing tĩnh; xử lý tại FW-039.
8. Không log dữ liệu nhạy cảm hoặc commit `.env`, Firebase service account, production key; Firebase **client API key** là cấu hình công khai thông thường, không tự phân loại thành secret lộ lọt. Test với Firebase Emulator/test project.

### Quy ước mức độ và bằng chứng

- **P0:** rủi ro làm sai sổ cái, tài sản, nợ, quyền ghi số dư; không bật chức năng phụ thuộc trước khi qua gate.
- **P1:** lệch nghiệp vụ, dữ liệu, tương thích hoặc thiếu tính năng chính cần hoàn thiện trước mở rộng người dùng.
- **P2:** nợ kỹ thuật, UX, hiệu năng, tài liệu, PWA; giải quyết sau correctness trừ khi phát sinh bằng chứng mức cao hơn.
- **CONFIRMED:** cấu trúc/đoạn code đã xác nhận qua đọc source; **GAP:** chức năng thiếu/chưa nối đã thấy trong repo; **VERIFY:** cần test/đối chiếu thêm để kết luận mức ảnh hưởng.
- `[ ]` trong file này là **checklist điều phối**, không phải bằng chứng task đã fail hay đã được giao; không tự gán người phụ trách, deadline, trạng thái vận hành.

### Ma trận ưu tiên nhanh

| ID | P | Loại | Task | Điều kiện trước |
|---|:---:|---|---|---|
| [ ] FW-001 | P0 | GATE | Khóa bộ case đối chiếu Android–Web và 15 luồng tiền | Không. |
| [ ] FW-002 | P0 | CONFIRMED | Sửa tài sản ròng: trừ khoản nợ thực và không đếm nợ thẻ hai lần | FW-001. |
| [ ] FW-003 | P0 | CONFIRMED | Thanh toán nợ vay phải nguyên tử (wallet + ledger + remainingBalance) | FW-001, FW-008; khóa phương án Rules/backend trước khi code. |
| [ ] FW-004 | P0 | CONFIRMED | Thanh toán sao kê thẻ tín dụng bằng transfer, không ghi thêm expense | FW-002, FW-003, FW-009; cần quyết định mapping card/debt. |
| [ ] FW-005 | P0 | CONFIRMED | Nạp mục tiêu: transaction nguyên tử + idempotency | FW-001, FW-008. |
| [ ] FW-006 | P0 | GAP | Rút mục tiêu: bổ sung use case nguyên tử và semantics báo cáo | FW-001, FW-005; chốt transaction category semantics với Android. |
| [ ] FW-007 | P0 | CONFIRMED | Đảo giao dịch đã liên kết nợ/mục tiêu phải giữ nguyên toàn bộ bất biến | FW-003, FW-004, FW-005, FW-006. |
| [ ] FW-008 | P0 | CONFIRMED | Không cho phép âm ví tiền mặt/ngân hàng ngoài chính sách đã thống nhất | FW-001. |
| [ ] FW-009 | P0 | CONFIRMED | Đổi khóa cặp transfer từ `Date.now()` sang ID duy nhất, truy vết được | FW-001. |
| [ ] FW-010 | P0 | CONFIRMED | Siết Rules: balance ví không thể tùy sửa chỉ bằng `lastTransactionId` giả | FW-001; cần phê duyệt chọn A/B. |
| [ ] FW-011 | P0 | CONFIRMED | Siết Rules: cấm xóa giao dịch trực tiếp bỏ qua rollback | FW-007, FW-010 (hoặc triển khai song song sau khi chốt thiết kế). |
| [ ] FW-012 | P0 | VERIFY | Chặn ghi dư nợ/goal sai phạm vi và giá trị âm từ SDK | FW-003, FW-005, FW-010; xác minh trước khi kết luận mức khai thác. |
| [ ] FW-013 | P1 | CONFIRMED | Sửa Cloud Functions period resolver theo múi giờ tài chính | FW-001; xác nhận cấu hình salary-cycle Android. |
| [ ] FW-014 | P1 | CONFIRMED | Đọc đầy đủ Salary Cycle, không chỉ đọc `financeTimeZone` | FW-001, FW-013. |
| [ ] FW-015 | P1 | CONFIRMED | Đọc `dealFlowType` và loại trừ hoàn vốn khỏi báo cáo thu nhập | FW-001, FW-002. |
| [ ] FW-016 | P1 | CONFIRMED | Gộp registry category và xử lý `housing`/`home` legacy | FW-001. |
| [ ] FW-017 | P1 | CONFIRMED | Không thay ngày thiếu/sai bằng ngày hiện tại khi đọc Firestore | FW-001. |
| [ ] FW-018 | P1 | CONFIRMED | Thống nhất metadata ví ngân hàng và Firestore whitelist | FW-010; chốt field schema cùng Android. |
| [ ] FW-019 | P1 | CONFIRMED | Form tạo ví chỉ đóng khi lưu Cloud thành công | FW-018 nếu bổ sung metadata cùng đợt; có thể làm độc lập trước. |
| [ ] FW-020 | P1 | CONFIRMED | Ngăn xóa ví đang có sổ cái và chuẩn hóa archive | FW-010, FW-011. |
| [ ] FW-021 | P1 | GAP | Bổ sung sửa giao dịch, không viết đè Firestore trực tiếp | FW-007, FW-008, FW-009, FW-013. |
| [ ] FW-022 | P1 | GAP | Màn Ngân sách: tạo/sửa hạn mức và chọn kỳ phù hợp | FW-013, FW-014, FW-016. |
| [ ] FW-023 | P1 | GAP | Mục tiêu: giao diện tạo/sửa/đóng và danh sách không bị giới hạn ở goal đầu | FW-017, FW-018 (nếu tái sử dụng form), không phụ thuộc FW-005 nếu chỉ metadata. |
| [ ] FW-024 | P1 | GAP | Mục tiêu: UI nạp/rút, lịch sử và trạng thái lưu | FW-005, FW-006, FW-007, FW-023. |
| [ ] FW-025 | P1 | GAP | Kết nối màn Quản lý nợ vào điều hướng chính | FW-003, FW-004, FW-018. |
| [ ] FW-026 | P1 | GAP | Xuất báo cáo Excel/PDF từ cùng một DTO báo cáo | FW-002, FW-014, FW-015, FW-017. |
| [ ] FW-027 | P1 | CONFIRMED | Phân trang giao dịch, tránh listener toàn bộ lịch sử | FW-001, FW-014; chốt contract aggregator trước khi tách. |
| [ ] FW-028 | P1 | CONFIRMED | Trạng thái `isCloudSynced` phải phản ánh snapshot thật | FW-001. |
| [ ] FW-029 | P1 | CONFIRMED | Bỏ auth-loading timeout cố định 1,2 giây làm nguồn xác định session | FW-028. |
| [ ] FW-030 | P1 | VERIFY | Gộp việc seed user/ví sau login để chống chạy lặp giữa hai luồng Auth | FW-028, FW-029. |
| [ ] FW-031 | P1 | CONFIRMED | Loại bỏ Saving Spin ghi thưởng thành income, triển khai ledger riêng theo spec | FW-001; đối chiếu `FINLUX_SAVING_SPIN_IMPLEMENTATION_PLAN.md`. |
| [ ] FW-032 | P1 | GAP | Thêm CI riêng Web và đồng bộ Node version cho Functions | Không; ưu tiên triển khai trước khi sửa nhiều task. |
| [ ] FW-033 | P1 | GAP | Thêm Firebase Rules/atomic mutation regression suite | FW-001; thiết lập sớm trước khi siết Rules. |
| [ ] FW-034 | P2 | CONFIRMED | Không trả dữ liệu demo dưới tên API summary production | FW-028 nếu chọn API authenticated. |
| [ ] FW-035 | P2 | CONFIRMED | Chỉ render một Toast toàn ứng dụng | Không. |
| [ ] FW-036 | P2 | GAP | Chuẩn hóa modal focus/keyboard/accessibility | FW-019, FW-025 nếu sửa chung modal. |
| [ ] FW-037 | P2 | VERIFY | Đo và tối ưu landing 3D có kiểm chứng | Không; không refactor mù. |
| [ ] FW-038 | P2 | VERIFY | Làm rõ Web/PWA/offline thực tế và trạng thái mất mạng | FW-028, FW-027. |
| [ ] FW-039 | P2 | CONFIRMED | Cập nhật tài liệu Web cũ tham chiếu `website/` thay vì `web/` | Không. |
| [ ] FW-040 | P2 | VERIFY | Đối chiếu chính sách xóa tài khoản với chức năng thực | Không; cần quyết định sản phẩm và quyền riêng tư. |
| [ ] FW-041 | P2 | CONFIRMED | Refactor `FinanceContext` theo domain mà không đổi hành vi | FW-002..FW-018 liên quan; làm theo từng cụm sau khi khóa invariant. |
| [ ] FW-042 | P2 | GAP | Báo cáo: kỳ tùy chỉnh, đối chiếu kỳ trước và drill-down | FW-014, FW-015, FW-026, FW-027. |

## 1. Phương thức thực thi và checkpoint

**Thứ tự khuyến nghị:** `FW-001` → (`FW-032`, `FW-033`) → nhánh tiền tệ (`FW-002` đến `FW-012`) → period/semantics (`FW-013` đến `FW-017`) → Web functions (`FW-018` đến `FW-031`) → UX/hardening (`FW-034` đến `FW-042`). Có thể làm song song task **không sửa chung file/contract**; nếu chung `FinanceContext.tsx`, Rules hoặc `functions/src/index.ts` thì tuần tự hóa/merge-rebase sau mỗi task.

**Điểm chặn 1 — trước bất kỳ sửa tiền:** tạo baseline reproducible (`FW-001`), chuẩn bị Rules Emulator (`FW-033`), rà soát task với Android, chọn A/B và được duyệt. **Điểm chặn 2 — trước khi nối Debt/Goal/Saving Spin UI:** luồng ghi nguyên tử và reversal tương ứng đã có tests. **Điểm chặn 3 — trước release:** CI (`FW-032`) xanh, đối soát 15 luồng và dữ liệu legacy, review tác động Rules trên Android.

**Lệnh kiểm tra đã xác nhận có scripts trong repo** (chạy từ root; không coi pass nếu chưa thật chạy):

```bash
npm --prefix web ci
npm --prefix web test
npm --prefix web run lint
npm --prefix web exec tsc -- --noEmit
npm --prefix web run build
npm --prefix functions ci
npm --prefix functions run check
npx firebase-tools emulators:exec --only firestore --project finlux-test "npm --prefix functions test"
```

Với test thay Rules/Functions, chạy qua **Emulator test project**; không chạy test với project Firebase production. Nếu CI/command khác phiên bản hiện tại hoặc môi trường thiếu dependency, ghi rõ blocker, không báo PASS khi chưa chạy. Khi run emulator, xác nhận cài Java/CLI phù hợp Node 22.

### Definition of Done (bắt buộc cho từng task)

- [ ] Ghi 4 bước kickoff và A/B, được người điều phối duyệt scope.
- [ ] Root cause/impact được chứng minh bằng test fail hoặc trace, phân biệt trường hợp chưa tái hiện.
- [ ] Chỉ sửa đúng scope, không thay contract Android/Firebase ngoài phần đã phê duyệt.
- [ ] Unit test + Emulator/RTL/E2E phù hợp; chạy toàn bộ hồi quy có liên quan.
- [ ] Nếu đổi Business Rule, Schema hoặc UI contract đã cập nhật tài liệu đặc tả tương ứng.
- [ ] PR/working-tree diff mô tả files changed, dữ liệu migration, rollback plan và kết quả lệnh test.
- [ ] **Không tự commit/push/deploy** khi chưa có lệnh; nếu được yêu cầu PR thì chỉ tạo PR theo đúng phê duyệt.

### Mẫu báo cáo Codex sau mỗi task

```text
Task ID: FW-xxx
BASE SHA / branch:
Docs vs Code Reality (B1, B2):
Sanity check + 15 money flows + blast radius (B3):
Option A / Option B / Breaking risk / Recommendation (B4):
Approval received? (yes/no):
Files changed (+ why):
Tests run (exact commands, PASS/FAIL/SKIP):
Remaining risks + migration/rollback:
Ready for review? (yes/no):
```

### Prompt dùng để giao từng task cho Codex (copy và thay ID)

```text
Bạn là Tech Lead/Codex cho FinLux. Chỉ nhận 01 task: <FW-XXX> trong
docs/FINLUX_WEB_CODEX_FIX_BACKLOG_2026-10-28.md của repo khoaiprovip123/FinLux.
Trước khi sửa code, đọc AGENTS.md, web/AGENTS.md, ma trận 15 luồng tiền,
BA_SPEC, DATA_SPEC và thực tế code ở HEAD. Báo cáo 4 bước kickoff:
(1) roadmap/spec; (2) code reality; (3) phản biện + blast radius;
(4) ít nhất 2 phương án A/B, ưu/nhược/rủi ro, khuyến nghị.
CHỜ tôi duyệt phương án trước khi sửa. Sau khi duyệt: sửa tối thiểu,
viết test tái hiện trước, chạy test liên quan + test hồi quy, update spec.
Không tự commit/push/merge/deploy hoặc thay dữ liệu production.
Trả báo cáo theo mẫu trong backlog; nếu thiếu căn cứ thì nêu rõ.
```

## 2. Danh sách task chi tiết

### Cụm A — Bảo toàn dữ liệu, luật ghi sổ và tính đúng (ưu tiên chặn release)

#### [ ] FW-001 — Khóa bộ case đối chiếu Android–Web và 15 luồng tiền

- **Ưu tiên / bằng chứng:** P0 · GATE.
- **Dấu vết mã nguồn:** `AGENTS.md`; `docs/FINLUX_SYSTEM_ARCHITECTURE_MATRIX.md` (15 luồng); `docs/BA_SPEC.md` (BR-DEBT/GOAL/ASSET).
- **Vấn đề / lý do:** Chưa có một bộ fixture kiểm chứng đồng thời ví, nợ, mục tiêu, ngân sách, báo cáo và dòng tiền trên Web; sửa rời rạc rất dễ tái phát lỗi.
- **Phạm vi cho Codex:** `web/test/` (bổ sung fixtures), `functions/test/` nếu cần; tài liệu hợp đồng đối chiếu, KHÔNG đổi logic production trong task này.
- **Hướng điều tra/triển khai:** Chốt bảng đầu vào/đầu ra chuẩn với Android; tối thiểu các case thu, chi, chuyển nội bộ, thanh toán thẻ, gốc/lãi vay, nạp/rút goal, deal outlay/recovery/gain/loss, Saving Spin, backdate, thay kỳ lương.
- **Tiêu chí nghiệm thu:**
  - [ ] Fixture có giá trị kỳ vọng cho `wallet.balance`, total Income, Living Expense, Budget, Debt, Goal, Net Worth.
  - [ ] Mỗi case nêu rõ period, timezone, bản ghi legacy và canonical nếu có.
  - [ ] Không lấy UI hoặc một phép tính Web hiện tại làm nguồn đúng duy nhất.
- **Kiểm thử riêng:** Chạy `npm --prefix web test`; lưu kết quả baseline (PASS/FAIL) và case đang fail vào báo cáo, không tự sửa để làm xanh.
- **Phụ thuộc / Gate:** Không.

#### [ ] FW-002 — Sửa tài sản ròng: trừ khoản nợ thực và không đếm nợ thẻ hai lần

- **Ưu tiên / bằng chứng:** P0 · CONFIRMED.
- **Dấu vết mã nguồn:** `web/src/context/FinanceContext.tsx` L554–L629; `web/src/lib/financeCalculations.ts` L120–L122; `docs/BA_SPEC.md` BR-ASSET-01.
- **Vấn đề / lý do:** `currentNetWorth` hiện lấy từ ví, trong khi `debts` chỉ vào `summary.totalDebt`; khoản vay không nằm trong ví có thể bị bỏ qua.
- **Phạm vi cho Codex:** Hàm tính domain trong `web/src/lib/financeCalculations.ts`, read model `FinanceContext.tsx`, tests; không sửa kiểu hiển thị tùy tiện.
- **Hướng điều tra/triển khai:** Định nghĩa thống nhất `grossAssets`, `creditCardLiabilities`, `nonCardDebts`, `trueNetWorth`; xác nhận mapping debt thẻ tín dụng ↔ ví card với Android để tránh double deduction; xử lý ví archived.
- **Tiêu chí nghiệm thu:**
  - [ ] Tài sản 100 triệu, nợ vay độc lập 40 triệu => tài sản ròng 60 triệu.
  - [ ] Nếu dư nợ thẻ đã biểu diễn bằng ví card âm, không trừ cùng một dư nợ thêm lần nữa.
  - [ ] Dashboard, kỳ trước và summary dùng một phép tính, có test cross-device/historical.
- **Kiểm thử riêng:** Unit test net worth: không nợ, vay độc lập, thẻ có/không có liên kết debt, ví archived, dữ liệu legacy.
- **Phụ thuộc / Gate:** FW-001.

#### [ ] FW-003 — Thanh toán nợ vay phải nguyên tử (wallet + ledger + remainingBalance)

- **Ưu tiên / bằng chứng:** P0 · CONFIRMED.
- **Dấu vết mã nguồn:** `web/src/context/FinanceContext.tsx` L964–L1006; `docs/BA_SPEC.md` BR-DEBT-03.
- **Vấn đề / lý do:** `makeDebtPayment()` gọi `addTransaction()` không `await`, vẫn cập nhật `remainingBalance` riêng. Có thể ghi một nửa nghiệp vụ.
- **Phạm vi cho Codex:** Tách use case thanh toán nợ; `FinanceContext.tsx`, Firestore repository/rules/test, đặc tả liên quan nếu đổi semantics.
- **Hướng điều tra/triển khai:** Thống nhất nợ vay khác thẻ; đọc debt và wallet trong cùng transaction/callable; validate amount > 0, <= remaining, ví tồn tại và khả dụng; tạo ledger/correlationId và cập nhật debt trong một commit; chống double submit.
- **Tiêu chí nghiệm thu:**
  - [ ] Nếu 1 bước thất bại thì ví, ledger và debt không đổi.
  - [ ] Hai client trả đồng thời không làm dư nợ âm hoặc trừ ví sai.
  - [ ] Nợ tất toán cập nhật `isSettled` và giữ lịch sử thanh toán phù hợp spec.
- **Kiểm thử riêng:** Emulator: success, insufficient balance, missing wallet/debt, retry, double-click, two concurrent clients; chạy Android regression liên quan.
- **Phụ thuộc / Gate:** FW-001, FW-008; khóa phương án Rules/backend trước khi code.

#### [ ] FW-004 — Thanh toán sao kê thẻ tín dụng bằng transfer, không ghi thêm expense

- **Ưu tiên / bằng chứng:** P0 · CONFIRMED.
- **Dấu vết mã nguồn:** `web/src/context/FinanceContext.tsx` L969–L976; `docs/FINLUX_SYSTEM_ARCHITECTURE_MATRIX.md` dòng 2–3.
- **Vấn đề / lý do:** `makeDebtPayment()` luôn ghi `expense`/`debt_payment`; với nợ thẻ có nguy cơ tính chi tiêu lần thứ hai.
- **Phạm vi cho Codex:** Nhánh `CREDIT_CARD` của use case debt payment, mapping ví card, ledger pairs, tests.
- **Hướng điều tra/triển khai:** Trước tiên xác nhận debt thẻ và ví thẻ nào là cùng nghĩa vụ; triển khai cặp `TRANSFER_OUT/TRANSFER_IN` nếu có ví card; không suy đoán liên kết ID khi thiếu dữ liệu.
- **Tiêu chí nghiệm thu:**
  - [ ] Quẹt thẻ 1 triệu tính chi sinh hoạt đúng 1 lần; thanh toán sao kê không tăng chi sinh hoạt.
  - [ ] Ví thanh toán giảm, ví card tăng (giảm số dư âm) nguyên tử.
  - [ ] Debt record liên quan chỉ đổi theo hợp đồng đã chốt; không double-count net worth.
- **Kiểm thử riêng:** Emulator: purchase -> statement payment -> report; thanh toán một phần, trả dư, thẻ không liên kết.
- **Phụ thuộc / Gate:** FW-002, FW-003, FW-009; cần quyết định mapping card/debt.

#### [ ] FW-005 — Nạp mục tiêu: transaction nguyên tử + idempotency

- **Ưu tiên / bằng chứng:** P0 · CONFIRMED.
- **Dấu vết mã nguồn:** `web/src/context/FinanceContext.tsx` L1038–L1075; `docs/BA_SPEC.md` BR-GOAL-01.
- **Vấn đề / lý do:** `depositToGoal()` ghi expense và cập nhật goal tách rời, không kiểm tra kết quả và dùng giá trị goal từ state có thể cũ.
- **Phạm vi cho Codex:** Goal deposit use case, Firestore repository/transaction, types/tests.
- **Hướng điều tra/triển khai:** Đọc goal và wallet trong một transaction; kiểm tra số dư, tiền nguyên, số tiền hợp lệ; viết ledger cùng `savedAmount` theo increment từ snapshot; liên kết bằng ID nghiệp vụ để hoàn nguyên.
- **Tiêu chí nghiệm thu:**
  - [ ] Ví 500.000, goal 50.000, nạp 100.000 => 400.000 và 150.000 cùng xuất hiện hoặc cùng không đổi.
  - [ ] Hai thiết bị nạp đồng thời không mất lần ghi.
  - [ ] Nạp goal không làm tăng Living Expense và không làm sai tài sản ròng.
- **Kiểm thử riêng:** Emulator: fail injection, hai client, replay same operation key, insufficient balance, backdated transaction.
- **Phụ thuộc / Gate:** FW-001, FW-008.

#### [ ] FW-006 — Rút mục tiêu: bổ sung use case nguyên tử và semantics báo cáo

- **Ưu tiên / bằng chứng:** P0 · GAP.
- **Dấu vết mã nguồn:** `web/src/context/FinanceContext.tsx` chỉ có `depositToGoal`; `docs/BA_SPEC.md` BR-GOAL-01; kiến trúc dòng 10.
- **Vấn đề / lý do:** Chưa thấy use case Web để rút goal; nếu triển khai giản đơn bằng `income` dễ tính nhầm thu nhập thực.
- **Phạm vi cho Codex:** Goal withdrawal use case + tests, giữ UI riêng ở FW-023.
- **Hướng điều tra/triển khai:** Kiểm tra goal đủ số tiền, ví đích hợp lệ; cập nhật goal giảm và ví tăng trong một transaction, ghi sổ nhất quán Android; đánh dấu phân loại loại trừ khỏi earned income.
- **Tiêu chí nghiệm thu:**
  - [ ] Rút 50.000 từ goal 150.000 về ví 400.000 => goal 100.000, ví 450.000.
  - [ ] Không xuất hiện thu nhập lương/thưởng giả từ vốn tích lũy rút về.
  - [ ] Rollback và thao tác đồng thời giữ số dư đúng.
- **Kiểm thử riêng:** Emulator: withdraw full/partial/overdraw, failed save, repeat operation, báo cáo thu nhập.
- **Phụ thuộc / Gate:** FW-001, FW-005; chốt transaction category semantics với Android.

#### [ ] FW-007 — Đảo giao dịch đã liên kết nợ/mục tiêu phải giữ nguyên toàn bộ bất biến

- **Ưu tiên / bằng chứng:** P0 · CONFIRMED.
- **Dấu vết mã nguồn:** `web/src/lib/transactionRollback.ts` L5–L53; `FinanceContext.tsx` L769–L809; BR-DEBT-03/BR-GOAL-01.
- **Vấn đề / lý do:** Rollback hiện phục hồi ví và xóa ledger, nhưng không xử lý dư nợ/goal liên kết, tạo nguy cơ lệch aggregate nếu xóa từ màn hình giao dịch.
- **Phạm vi cho Codex:** Transaction reversal use case, debt/goal link/correlation IDs; kiểm thử legacy records.
- **Hướng điều tra/triển khai:** Phân loại ledger liên kết và thiết kế reverse toàn bộ hoặc chặn xóa có hướng dẫn đến luồng nghiệp vụ tương ứng; không tự xóa để tạo mất audit trail; bảo vệ giao dịch legacy không có linkage.
- **Tiêu chí nghiệm thu:**
  - [ ] Xóa/reverse thanh toán nợ không để debt và wallet lệch.
  - [ ] Xóa/reverse nạp/rút goal không để savedAmount và ledger lệch.
  - [ ] Giao dịch thiếu companion/linked record phải abort có lý do rõ; không phục hồi một nửa.
- **Kiểm thử riêng:** Emulator rollback: debt, goal, transfer pair, legacy no-link, multi-user permission, concurrent mutation.
- **Phụ thuộc / Gate:** FW-003, FW-004, FW-005, FW-006.

#### [ ] FW-008 — Không cho phép âm ví tiền mặt/ngân hàng ngoài chính sách đã thống nhất

- **Ưu tiên / bằng chứng:** P0 · CONFIRMED.
- **Dấu vết mã nguồn:** `FinanceContext.tsx` L631–L723 (trừ trực tiếp `currentBal - data.amount`); `web/src/components/TransactionModal.tsx`.
- **Vấn đề / lý do:** Không có kiểm tra đủ số dư tại thời điểm commit cho chi và chuyển tiền; có thể âm ví thông thường.
- **Phạm vi cho Codex:** Validation domain + atomic writer; test; UI báo lỗi, không patch bằng disable button đơn thuần.
- **Hướng điều tra/triển khai:** Phân biệt cash/bank/ewallet/card/loan; validate `Number.isSafeInteger`, <=15 chữ số, amount>0, distinct wallets, status active và current balance trong transaction; đồng bộ quy tắc Android.
- **Tiêu chí nghiệm thu:**
  - [ ] Ví tiền mặt 100.000 không thể chi/chuyển 150.000 nếu policy không cho overdraft.
  - [ ] Ví thẻ hoạt động theo chính sách hạn mức riêng, không dùng chung chặn âm.
  - [ ] Hai thiết bị cùng chi hết tiền: chỉ các giao dịch hợp lệ được commit.
- **Kiểm thử riêng:** Unit + Emulator insufficient funds, large amount, zero, NaN/decimal, same-wallet transfer, race.
- **Phụ thuộc / Gate:** FW-001.

#### [ ] FW-009 — Đổi khóa cặp transfer từ `Date.now()` sang ID duy nhất, truy vết được

- **Ưu tiên / bằng chứng:** P0 · CONFIRMED.
- **Dấu vết mã nguồn:** `FinanceContext.tsx` L642–L646, L671–L693; `transactionRollback.ts` L12–L28.
- **Vấn đề / lý do:** `pairId = tx_${Date.now()}` có nguy cơ trùng ID nếu hai giao dịch trong cùng mili-giây; `_out`/`_in` là hợp đồng xóa.
- **Phạm vi cho Codex:** Transfer writer, `transactionRollback`, tests; nếu thêm `correlationId` phải cập nhật spec/rules và Android parity.
- **Hướng điều tra/triển khai:** Sinh document ID bằng Firestore auto ID/UUID an toàn, giữ đối ứng có thể tìm được; kiểm tra không overwrite pair cũ; tương thích cặp legacy `_out`/`_in`.
- **Tiêu chí nghiệm thu:**
  - [ ] 1000 lần tạo ID không đụng nhau trong test; hai client đồng thời không ghi đè.
  - [ ] Xóa/reverse cặp mới và cũ đều nhận đúng companion.
  - [ ] Bản ghi transfer có cùng amount, ví nguồn/đích đối ứng và timestamp.
- **Kiểm thử riêng:** Concurrency + rollback fixtures; Firebase Emulator thử tạo/xóa nhanh.
- **Phụ thuộc / Gate:** FW-001.

#### [ ] FW-010 — Siết Rules: balance ví không thể tùy sửa chỉ bằng `lastTransactionId` giả

- **Ưu tiên / bằng chứng:** P0 · CONFIRMED.
- **Dấu vết mã nguồn:** `firestore.rules` L193–L224.
- **Vấn đề / lý do:** Rules cho đổi balance nếu `lastTransactionId` là string không rỗng, chưa chứng minh giao dịch hợp lệ đã được tạo; rủi ro integrity dù user không đọc được dữ liệu người khác.
- **Phạm vi cho Codex:** `firestore.rules`, `functions/test/firestore.rules.test.ts` + các client writers liên quan.
- **Hướng điều tra/triển khai:** So sánh phương án A: chứng thực ledger delta/cặp document bằng Rules và `getAfter`; B: mutation đặc quyền qua server/callable với contract; đánh giá blast radius Android trước khi chọn.
- **Tiêu chí nghiệm thu:**
  - [ ] SDK trực tiếp sửa `wallet.balance` với marker giả bị từ chối.
  - [ ] Ghi thu/chi/transfer chính thống Web và Android vẫn thành công.
  - [ ] Không phá flow tạo ví, opening balance, edit/delete transaction, legacy migration.
- **Kiểm thử riêng:** Firestore Emulator: authorized valid/invalid balance changes; direct SDK abuse; Android + Web smoke.
- **Phụ thuộc / Gate:** FW-001; cần phê duyệt chọn A/B.

#### [ ] FW-011 — Siết Rules: cấm xóa giao dịch trực tiếp bỏ qua rollback

- **Ưu tiên / bằng chứng:** P0 · CONFIRMED.
- **Dấu vết mã nguồn:** `firestore.rules` L162–L190 (`allow delete: if isOwner(uid)`); `transactionRollback.ts`.
- **Vấn đề / lý do:** Xóa thẳng transaction bằng SDK không bắt buộc đảo ví, trong khi app dùng hàm rollback có transaction.
- **Phạm vi cho Codex:** `firestore.rules`, test rules và service xóa; tương thích Android.
- **Hướng điều tra/triển khai:** Thêm invariant cho delete, hoặc chuyển sang soft-delete/server-mediated reverse để chặn direct delete; khóa race và liên kết transfer pair, tránh giải pháp chỉ chặn UI.
- **Tiêu chí nghiệm thu:**
  - [ ] Direct delete không kèm đảo ví phải fail.
  - [ ] Xóa hợp lệ trên Web/Android đảo đúng balance hoặc đóng sổ bằng trạng thái theo spec.
  - [ ] Cặp transfer không còn trạng thái một nửa khi có lỗi.
- **Kiểm thử riêng:** Rules Emulator: direct delete, pair delete, rollback, missing wallet/companion; Android regression.
- **Phụ thuộc / Gate:** FW-007, FW-010 (hoặc triển khai song song sau khi chốt thiết kế).

#### [ ] FW-012 — Chặn ghi dư nợ/goal sai phạm vi và giá trị âm từ SDK

- **Ưu tiên / bằng chứng:** P0 · VERIFY.
- **Dấu vết mã nguồn:** `firestore.rules` L272–L279 và L370–L380; `FinanceContext.tsx` L981–L1062.
- **Vấn đề / lý do:** Rules kiểm tên nhưng không thấy ràng buộc đầy đủ `remainingBalance`, `savedAmount`, quyền cập nhật aggregate; cần xác minh khai thác bằng Emulator.
- **Phạm vi cho Codex:** Security rules cho debts/goals + regression tests; không đổi field schema tự phát.
- **Hướng điều tra/triển khai:** Thiết kế whitelist/validation số nguyên, giới hạn, bất biến ID/timestamps và quyền cập nhật balance mục tiêu; tương thích Android, Cloud Functions và dữ liệu legacy.
- **Tiêu chí nghiệm thu:**
  - [ ] Không thể đặt remainingBalance âm, paidAmount phi lý, hoặc savedAmount âm tùy ý.
  - [ ] Luồng nghiệp vụ chuẩn vẫn ghi được bằng cơ chế đã phê duyệt.
  - [ ] Rules tests bao phủ create/update/delete và user isolation.
- **Kiểm thử riêng:** Firestore Emulator với dữ liệu hợp lệ/sai, permission denied, type mismatch, legacy documents.
- **Phụ thuộc / Gate:** FW-003, FW-005, FW-010; xác minh trước khi kết luận mức khai thác.

### Cụm B — Nghiệp vụ Web, đồng bộ, chất lượng nền tảng

#### [ ] FW-013 — Sửa Cloud Functions period resolver theo múi giờ tài chính

- **Ưu tiên / bằng chứng:** P1 · CONFIRMED.
- **Dấu vết mã nguồn:** `functions/src/index.ts` L53–L100, L232–L290; `docs/BA_SPEC.md` BR-SALARY/BR-PERIOD.
- **Vấn đề / lý do:** Tác vụ chạy theo `Asia/Ho_Chi_Minh` nhưng xác định tháng/ngày bằng `getUTC*`; đầu ngày giờ Việt Nam có thể thuộc ngày UTC trước đó.
- **Phạm vi cho Codex:** `functions/src/index.ts` period utils, tests date/time; không đổi tên collection.
- **Hướng điều tra/triển khai:** Tách resolver thuần nhận instant + `financeTimeZone`, dùng [start,endExclusive) trong đúng zone; cron rollover dựa theo ngày nghiệp vụ, hỗ trợ 28/29/30/31 và DST tại timezone hỗ trợ; xử lý idempotency.
- **Tiêu chí nghiệm thu:**
  - [ ] 00:00 ngày 01/11/2026 Asia/Ho_Chi_Minh mở `month:2026-11`, không nhầm October.
  - [ ] Kỳ lương giao giữa năm, cuối tháng ngắn và hai đợt theo cấu hình được kiểm thử.
  - [ ] Chạy lại scheduler trong cùng kỳ không reset mất số liệu đã ghi.
- **Kiểm thử riêng:** Unit test transition 00:00 VN, leap year, timezone khác; Emulator theo ngân sách kỳ mới.
- **Phụ thuộc / Gate:** FW-001; xác nhận cấu hình salary-cycle Android.

#### [ ] FW-014 — Đọc đầy đủ Salary Cycle, không chỉ đọc `financeTimeZone`

- **Ưu tiên / bằng chứng:** P1 · CONFIRMED.
- **Dấu vết mã nguồn:** `FinanceContext.tsx` L352–L357 và L523–L537; `financeCalculations.ts` L75–L92; `Header.tsx` L73–L85.
- **Vấn đề / lý do:** Web chỉ dùng timezone từ salaryCycle settings, bộ lọc thực tế chủ yếu là 3/6 tháng lịch; chưa phản ánh macro/micro salary period.
- **Phạm vi cho Codex:** Read model salary config, `financeCalculations.ts`, `Header.tsx`, dashboard/history/reports/budget test.
- **Hướng điều tra/triển khai:** Đọc scheduleType, paydayRuleType, paydayDay, secondPaydayDay và budgetPeriodBasis theo DATA_SPEC; điều hướng kỳ lương và kỳ tháng nhất quán; không chỉ đổi tên label.
- **Tiêu chí nghiệm thu:**
  - [ ] User bật salary cycle: Home, History, Reports, Budget cùng khoảng kỳ tương ứng.
  - [ ] Tắt salary cycle: quay về tháng lịch; không đổi dữ liệu persisted.
  - [ ] Hai đợt lương và ngày 31 gặp tháng 28/29 ngày được tính nhất quán.
- **Kiểm thử riêng:** Unit period matrix + cross-view snapshots + Android parity.
- **Phụ thuộc / Gate:** FW-001, FW-013.

#### [ ] FW-015 — Đọc `dealFlowType` và loại trừ hoàn vốn khỏi báo cáo thu nhập

- **Ưu tiên / bằng chứng:** P1 · CONFIRMED.
- **Dấu vết mã nguồn:** `web/src/types/finance.ts` L26–L38; `FinanceContext.tsx` L411–L430, L540–L552; `web/src/lib/semantics.ts`.
- **Vấn đề / lý do:** Web bỏ qua `dealFlowType`; `periodIncome` cộng mọi `income`, có thể tính hoàn vốn đầu tư thành thu nhập, và chi phí xuất vốn thành living expense.
- **Phạm vi cho Codex:** Type/Firestore mapper/semantic helpers/report calculators/tests; tham chiếu DEAL rules Android.
- **Hướng điều tra/triển khai:** Đồng bộ `dealId`, `dealFlowType`, `correlationId` từ schema; phân loại OUTLAY_CAPITAL, PRINCIPAL_RECOVERY, CAPITAL_GAIN, CAPITAL_LOSS đúng 15 luồng; xử lý legacy thiếu field riêng.
- **Tiêu chí nghiệm thu:**
  - [ ] Thu hồi vốn 10 triệu không tăng earned income; lợi nhuận 2 triệu tăng income đúng 2 triệu.
  - [ ] Xuất vốn không tăng living expense; chốt lỗ không trừ ví lần hai.
  - [ ] Home và Reports dùng chung kết quả, xử lý bản ghi legacy.
- **Kiểm thử riêng:** Unit test 4 deal flow types, plus history/net worth/no-double-count fixtures.
- **Phụ thuộc / Gate:** FW-001, FW-002.

#### [ ] FW-016 — Gộp registry category và xử lý `housing`/`home` legacy

- **Ưu tiên / bằng chứng:** P1 · CONFIRMED.
- **Dấu vết mã nguồn:** `web/src/lib/constants.ts` L3–L18; `web/src/lib/semantics.ts` L7–L30; `docs/FINLUX_SYSTEM_ARCHITECTURE_MATRIX.md` Central Category Registry.
- **Vấn đề / lý do:** Đang có hai bảng định nghĩa category; `housing` và `home` cùng nhãn, gây drift với `SystemCategories.kt`.
- **Phạm vi cho Codex:** Domain category registry, mapper/filter UI, tests và tài liệu; không đổi hàng loạt ID Firestore không có migration.
- **Hướng điều tra/triển khai:** Chọn một nguồn category ID chuẩn theo Android; thêm alias đọc legacy; giữ category người dùng tự tạo; không làm mất lịch sử cũ.
- **Tiêu chí nghiệm thu:**
  - [ ] Mọi UI dùng một registry và một mapper, không hardcode category ID mới.
  - [ ] Các transaction `housing` cũ vẫn được đọc/hiển thị đúng, không gộp sai báo cáo.
  - [ ] Category user-defined không bị thay thế bằng defaults khi snapshot rỗng tạm thời.
- **Kiểm thử riêng:** Category matrix: default, alias, deleted/unknown category, user-defined.
- **Phụ thuộc / Gate:** FW-001.

#### [ ] FW-017 — Không thay ngày thiếu/sai bằng ngày hiện tại khi đọc Firestore

- **Ưu tiên / bằng chứng:** P1 · CONFIRMED.
- **Dấu vết mã nguồn:** `FinanceContext.tsx` L145–L153 và mapper L411–L488.
- **Vấn đề / lý do:** `parseDateValue` fallback sang `new Date()` khi giá trị vắng/sai có thể đưa transaction/goal/debt cũ vào hôm nay, làm sai báo cáo.
- **Phạm vi cho Codex:** Date mapper và UI invalid-state; test; nếu đổi DTO cần cập nhật DATA_SPEC.
- **Hướng điều tra/triển khai:** Áp dụng rõ ràng required vs nullable, fail closed hoặc đánh dấu bản ghi cần đối soát; không tự gán ngày phát sinh tài chính.
- **Tiêu chí nghiệm thu:**
  - [ ] Transaction thiếu/invalid date không tự chuyển thành ngày hôm nay.
  - [ ] Các trường tùy chọn deadline có thể null và không hiển thị deadline giả.
  - [ ] Dữ liệu hỏng có thông báo/telemetry không lộ PII.
- **Kiểm thử riêng:** Test Timestamp, ISO string, null, invalid, ngày biên timezone, date-only.
- **Phụ thuộc / Gate:** FW-001.

#### [ ] FW-018 — Thống nhất metadata ví ngân hàng và Firestore whitelist

- **Ưu tiên / bằng chứng:** P1 · CONFIRMED.
- **Dấu vết mã nguồn:** `web/src/components/WalletModal.tsx` L18–L46; `FinanceContext.tsx` L823–L834; `firestore.rules` L196–L210.
- **Vấn đề / lý do:** Form thu `bankName`/`accountNumber` nhưng write payload bỏ chúng; Rules hiện cũng chưa cho các key đó.
- **Phạm vi cho Codex:** Wallet DTO, create/update mapper, Security Rules, DATA_SPEC và Android parity nếu chọn lưu field.
- **Hướng điều tra/triển khai:** Quyết định có lưu bankId/bankName/masked accountNumber hay không; nếu có, cập nhật whitelist/validation đồng bộ và không tự lưu toàn bộ số thẻ nhạy cảm ngoài nhu cầu.
- **Tiêu chí nghiệm thu:**
  - [ ] Field hợp lệ không bị mất khi reload Web hoặc mở Android.
  - [ ] Không đưa full accountNumber vào log hoặc endpoint công khai; UI mask số phù hợp.
  - [ ] Firestore Rules cho phép payload mới đúng schema và chặn field lạ.
- **Kiểm thử riêng:** Emulator wallet create/read/update, legacy wallet; security tests ownership/whitelist.
- **Phụ thuộc / Gate:** FW-010; chốt field schema cùng Android.

#### [ ] FW-019 — Form tạo ví chỉ đóng khi lưu Cloud thành công

- **Ưu tiên / bằng chứng:** P1 · CONFIRMED.
- **Dấu vết mã nguồn:** `web/src/components/WalletModal.tsx` L30–L47; `FinanceContext.tsx` L811–L847.
- **Vấn đề / lý do:** `addWallet()` là async nhưng form không `await`, đóng modal ngay cả khi Firestore từ chối.
- **Phạm vi cho Codex:** WalletModal state/status + return contract của `addWallet`; không thay use case tài chính khác.
- **Hướng điều tra/triển khai:** Hàm trả kết quả có kiểu rõ (Result/boolean); `await` trong submit; giữ input và lỗi khi thất bại; chặn double-click và hiển thị loading.
- **Tiêu chí nghiệm thu:**
  - [ ] Cloud reject => form còn mở, dữ liệu giữ nguyên, thông báo nguyên nhân hợp lý.
  - [ ] Cloud success => đóng và reset form.
  - [ ] Hai click liên tiếp không tạo hai ví trùng.
- **Kiểm thử riêng:** UI test mock rejected/resolved promise và retry; kiểm tra bàn phím.
- **Phụ thuộc / Gate:** FW-018 nếu bổ sung metadata cùng đợt; có thể làm độc lập trước.

#### [ ] FW-020 — Ngăn xóa ví đang có sổ cái và chuẩn hóa archive

- **Ưu tiên / bằng chứng:** P1 · CONFIRMED.
- **Dấu vết mã nguồn:** `FinanceContext.tsx` L868–L884; `firestore.rules` L193–L195; `docs/FINLUX_WEB_PLATFORM_IMPLEMENTATION_PLAN.md` P4.
- **Vấn đề / lý do:** Hàm `deleteWallet()` và Rules hiện cho xóa ví, có thể làm giao dịch cũ mất đối chiếu và phá rollback.
- **Phạm vi cho Codex:** Wallet lifecycle domain, UI quản lý ví, Rules, tests; tránh xóa vĩnh viễn tùy tiện.
- **Hướng điều tra/triển khai:** Chọn archive/soft-delete làm mặc định; chặn archive/delete khi còn tham chiếu không được xử lý, định nghĩa hành vi ví archived trong báo cáo lịch sử.
- **Tiêu chí nghiệm thu:**
  - [ ] Ví có transaction không thể bị hard-delete không kiểm soát.
  - [ ] Lịch sử cũ vẫn xem được tên ví, số dư và hoàn nguyên theo policy.
  - [ ] Ví archived không được chọn cho giao dịch mới, trừ trường hợp phục hồi theo spec.
- **Kiểm thử riêng:** Rules Emulator: delete referenced, archive unused, historical statement, restored wallet.
- **Phụ thuộc / Gate:** FW-010, FW-011.

#### [ ] FW-021 — Bổ sung sửa giao dịch, không viết đè Firestore trực tiếp

- **Ưu tiên / bằng chứng:** P1 · GAP.
- **Dấu vết mã nguồn:** `web/src/components/TransactionDetailModal.tsx` L122–L140; `web/src/components/TransactionModal.tsx`; `docs/BA_SPEC.md` giao dịch.
- **Vấn đề / lý do:** Màn hình chi tiết hiện chỉ có xóa, chưa có edit; sửa giao dịch cần đảo số dư/kỳ cũ rồi áp delta mới.
- **Phạm vi cho Codex:** Edit transaction use case, form, rollback service/Firestore Rules, tests.
- **Hướng điều tra/triển khai:** Thiết kế chỉnh amount/date/wallet/category/note; reverse old ledger impact và apply new effect nguyên tử, xử lý transfer pair; cập nhật budget kỳ cũ/kỳ mới qua backend reconciliation.
- **Tiêu chí nghiệm thu:**
  - [ ] Sửa ngày chuyển kỳ cập nhật báo cáo cả hai kỳ đúng.
  - [ ] Sửa ví/số tiền không mất hoặc đếm đôi đồng nào.
  - [ ] Sửa transfer giữ cặp đối ứng; invalid operation trả lỗi không đổi dữ liệu.
- **Kiểm thử riêng:** Emulator amount/wallet/date/transfer change; failure injection và concurrent edits.
- **Phụ thuộc / Gate:** FW-007, FW-008, FW-009, FW-013.

#### [ ] FW-022 — Màn Ngân sách: tạo/sửa hạn mức và chọn kỳ phù hợp

- **Ưu tiên / bằng chứng:** P1 · GAP.
- **Dấu vết mã nguồn:** `web/src/components/views/BudgetsView.tsx` L8–L126; `FinanceContext.tsx` L887–L932.
- **Vấn đề / lý do:** View đang read-only, có hàm update hạn mức nhưng chỉ viết `month:${month}`, không đưa Salary Cycle vào form.
- **Phạm vi cho Codex:** BudgetsView form/dialog, budget use case, tests; không viết `spentAmount` phía client.
- **Hướng điều tra/triển khai:** Thêm chọn category, kỳ tháng/kỳ lương, edit existing, limit >=0; giữ `periodKey`, `periodStart`, `periodEndExclusive` theo schema; tách cảnh báo backend.
- **Tiêu chí nghiệm thu:**
  - [ ] Có thể tạo/edit ngân sách theo kỳ thực, không trùng canonical ID.
  - [ ] Trường `spentAmount` không bị UI thao túng, chỉ derived/reconciled.
  - [ ] Thay kỳ báo cáo vẫn giữ hạn mức thuộc kỳ gốc.
- **Kiểm thử riêng:** Budget window monthly/salary/legacy, validation, Rules Emulator, report filter.
- **Phụ thuộc / Gate:** FW-013, FW-014, FW-016.

#### [ ] FW-023 — Mục tiêu: giao diện tạo/sửa/đóng và danh sách không bị giới hạn ở goal đầu

- **Ưu tiên / bằng chứng:** P1 · GAP.
- **Dấu vết mã nguồn:** `web/src/components/views/GoalsView.tsx` L8–L152; `FinanceContext.tsx` L1009–L1035.
- **Vấn đề / lý do:** GoalsView chủ yếu hiển thị tiến độ, chưa có nút CRUD; goal đầu tiên bị dùng làm mục tiêu chính cố định.
- **Phạm vi cho Codex:** GoalsView component/dialog, Goal form, tests; chỉ gọi domain use case chuẩn.
- **Hướng điều tra/triển khai:** Thêm create/edit/archive goal; chọn mục tiêu chính có cơ sở; ngày deadline nullable; validation mục tiêu và đồng bộ persisted/legacy.
- **Tiêu chí nghiệm thu:**
  - [ ] Danh sách 0/1/n goals có empty state và thao tác hợp lệ.
  - [ ] Có thể thêm/sửa goal khi online; cloud reject giữ form mở.
  - [ ] Chuyển goal chính không làm thay đổi số dư/tiến độ tích lũy.
- **Kiểm thử riêng:** UI tests empty/single/multiple, invalid amount/deadline, success/failure.
- **Phụ thuộc / Gate:** FW-017, FW-018 (nếu tái sử dụng form), không phụ thuộc FW-005 nếu chỉ metadata.

#### [ ] FW-024 — Mục tiêu: UI nạp/rút, lịch sử và trạng thái lưu

- **Ưu tiên / bằng chứng:** P1 · GAP.
- **Dấu vết mã nguồn:** `GoalsView.tsx`; `FinanceContext.tsx` L1038–L1075.
- **Vấn đề / lý do:** Use cases nạp/rút không được trình bày thành workflow rõ trên giao diện chính.
- **Phạm vi cho Codex:** Goal detail controls, transaction history, reusable amount/wallet/date form.
- **Hướng điều tra/triển khai:** Gắn UI vào các use case nguyên tử FW-005/006; hiển thị nguồn/đích, số tiền trước/sau, confirm và lỗi; sau commit query lại snapshot, không tự tăng số.
- **Tiêu chí nghiệm thu:**
  - [ ] Nạp/rút UI thay đổi ví và goal nhất quán sau ack.
  - [ ] Không cho double submit, vượt mục tiêu/âm goal theo chính sách.
  - [ ] Lịch sử hiển thị các khoản nạp/rút thực và reversal nếu có.
- **Kiểm thử riêng:** React flow + Firebase Emulator integration, slow network, failed write.
- **Phụ thuộc / Gate:** FW-005, FW-006, FW-007, FW-023.

#### [ ] FW-025 — Kết nối màn Quản lý nợ vào điều hướng chính

- **Ưu tiên / bằng chứng:** P1 · GAP.
- **Dấu vết mã nguồn:** `web/src/components/views/DebtsView.tsx`; `web/src/app/page.tsx` L76–L95; `web/src/components/Sidebar.tsx` L8–L16.
- **Vấn đề / lý do:** Có DebtsView với form CRUD/payment nhưng chưa có route/tab; giao diện còn dùng màu nền cố định khác design mới.
- **Phạm vi cho Codex:** Page navigation, Sidebar/Header, Debt screen UI + tests; không thay logic thanh toán trước khi hoàn tất FW-003/004.
- **Hướng điều tra/triển khai:** Thêm tab/route Debt, responsive/card states, retry errors và hỗ trợ nợ vay/thẻ rõ ràng; ẩn/disable nút trả nợ nếu writer chưa đạt atomic DoD.
- **Tiêu chí nghiệm thu:**
  - [ ] Người dùng truy cập được màn nợ bằng menu/đường dẫn dự kiến.
  - [ ] Form không đóng khi submit lỗi, thông báo và dữ liệu vẫn còn.
  - [ ] Không deploy luồng payment cũ không atomic chỉ để mở màn nợ.
- **Kiểm thử riêng:** Navigation + desktop/mobile screenshot, keyboard, debt scenarios.
- **Phụ thuộc / Gate:** FW-003, FW-004, FW-018.

#### [ ] FW-026 — Xuất báo cáo Excel/PDF từ cùng một DTO báo cáo

- **Ưu tiên / bằng chứng:** P1 · GAP.
- **Dấu vết mã nguồn:** `web/src/components/views/ReportsView.tsx` L8–L215; `docs/FINLUX_PRODUCT_MASTER_2026.md` section Export.
- **Vấn đề / lý do:** ReportsView chỉ hiển thị dashboard/biểu đồ, chưa có nút export Web; xuất tự tính công thức riêng dễ lệch.
- **Phạm vi cho Codex:** Report DTO/use case, export adapter Excel/PDF, ReportsView controls, tests.
- **Hướng điều tra/triển khai:** Chốt nội dung sheet/biểu đồ/định dạng theo BA_SPEC; dữ liệu export lấy từ cùng selector/DTO mà UI dùng, có timezone và periodKey trong metadata.
- **Tiêu chí nghiệm thu:**
  - [ ] Excel/PDF khớp KPI trên màn hình theo cùng kỳ.
  - [ ] Không đưa dữ liệu tài chính ra endpoint public; xử lý giới hạn dữ liệu lớn.
  - [ ] File Việt hóa, VND và có khoảng thời gian báo cáo rõ.
- **Kiểm thử riêng:** Golden data export vs display DTO, file open/validity, empty/large period.
- **Phụ thuộc / Gate:** FW-002, FW-014, FW-015, FW-017.

#### [ ] FW-027 — Phân trang giao dịch, tránh listener toàn bộ lịch sử

- **Ưu tiên / bằng chứng:** P1 · CONFIRMED.
- **Dấu vết mã nguồn:** `FinanceContext.tsx` L411–L433 (`onSnapshot(collection(...transactions))`); L576–L616.
- **Vấn đề / lý do:** Tải toàn bộ ledger và chạy nhiều `filter()`/`reduce()` có thể tăng Firestore reads, memory và thời gian render khi lịch sử dài.
- **Phạm vi cho Codex:** Transactions data-access/query, pagination strategy, selectors và tests; quan trọng giữ đủ dữ liệu cho KPI/historical.
- **Hướng điều tra/triển khai:** Thiết kế query theo time window + cursor; summary/reports dùng aggregate/ledger query đúng kỳ hoặc server read model; cache theo user/period; cleanup listeners.
- **Tiêu chí nghiệm thu:**
  - [ ] Danh sách 10000+ giao dịch vẫn phân trang, không tải toàn bộ ngay khi vào dashboard.
  - [ ] KPI không sai do chỉ mới nạp một trang; badge có loading/partial rõ ràng.
  - [ ] Thay user/period hủy listener cũ, không trộn data hai tài khoản.
- **Kiểm thử riêng:** Firestore Emulator seed >=10k fixture hoặc load-test test project; kiểm truy vấn, read-count, correctness.
- **Phụ thuộc / Gate:** FW-001, FW-014; chốt contract aggregator trước khi tách.

#### [ ] FW-028 — Trạng thái `isCloudSynced` phải phản ánh snapshot thật

- **Ưu tiên / bằng chứng:** P1 · CONFIRMED.
- **Dấu vết mã nguồn:** `FinanceContext.tsx` L271–L285, L359–L500; `SettingsView.tsx` L52–L58.
- **Vấn đề / lý do:** Đặt `isCloudSynced=true` ngay khi Auth thành công, trong khi 6 listener thiếu error callback; có thể báo đã đồng bộ khi Firestore lỗi.
- **Phạm vi cho Codex:** User session/data query state, settings status, UI feedback, tests.
- **Hướng điều tra/triển khai:** Mô hình hóa `authenticating/loading/ready/offline/error` cho từng nguồn; chỉ báo synced khi dữ liệu cần thiết đã sẵn sàng; bắt onSnapshot error, reconnect behavior.
- **Tiêu chí nghiệm thu:**
  - [ ] Permission denied/network fail không hiển thị đã đồng bộ thành công.
  - [ ] Không nhầm dữ liệu rỗng thật với dữ liệu đang tải.
  - [ ] Logout/đổi tài khoản không hiện ảnh hay số liệu của phiên trước.
- **Kiểm thử riêng:** Mock onSnapshot errors, offline/reconnect, account switch and initial empty docs.
- **Phụ thuộc / Gate:** FW-001.

#### [ ] FW-029 — Bỏ auth-loading timeout cố định 1,2 giây làm nguồn xác định session

- **Ưu tiên / bằng chứng:** P1 · CONFIRMED.
- **Dấu vết mã nguồn:** `FinanceContext.tsx` L264–L341; `web/src/app/page.tsx` L36–L74.
- **Vấn đề / lý do:** Timer 1200ms kết thúc loading trước khi Auth thực sự trả kết quả, có thể flash landing hoặc hiển thị trạng thái sai.
- **Phạm vi cho Codex:** Auth initialization + loading/timeout/error UI tests.
- **Hướng điều tra/triển khai:** Để `onAuthStateChanged` quyết định authenticated/guest; nếu cần timeout thì biểu diễn trạng thái chậm/error có retry, không cưỡng ép thành guest.
- **Tiêu chí nghiệm thu:**
  - [ ] Auth trả về sau 2s không hiển thị guest/landing như phiên đã xác định.
  - [ ] Auth thật sự fail có fallback và retry rõ ràng.
  - [ ] Không hydration mismatch hoặc UI flash tài khoản khác.
- **Kiểm thử riêng:** Mock Auth callback delay/error/unsubscribe; RTL navigation snapshot.
- **Phụ thuộc / Gate:** FW-028.

#### [ ] FW-030 — Gộp việc seed user/ví sau login để chống chạy lặp giữa hai luồng Auth

- **Ưu tiên / bằng chứng:** P1 · VERIFY.
- **Dấu vết mã nguồn:** `AuthScreen.tsx` L92–L139; `AuthModal.tsx`; `FinanceContext.tsx` L286–L312.
- **Vấn đề / lý do:** Nhiều vị trí cùng kiểm tra ví rỗng rồi tạo wallet `cash`; có race khi hai thiết bị/vòng Auth chạy đồng thời.
- **Phạm vi cho Codex:** Shared account bootstrap service, auth components và tests.
- **Hướng điều tra/triển khai:** Làm bootstrap idempotent, chỉ tạo default wallet khi user mới theo hợp đồng; giữ tên/profile photo Android; không ghi đè thông tin user đã sửa.
- **Tiêu chí nghiệm thu:**
  - [ ] Đăng nhập lại hoặc hai thiết bị login cùng lúc không tạo trùng/ghi đè default wallet.
  - [ ] Profile Android không bị reset bởi Web seeding.
  - [ ] Chức năng đăng ký/Google Sign-in giữ hành vi chuẩn và lỗi có thể retry.
- **Kiểm thử riêng:** Emulator multi-client login, existing profile/custom wallet, empty account.
- **Phụ thuộc / Gate:** FW-028, FW-029.

#### [ ] FW-031 — Loại bỏ Saving Spin ghi thưởng thành income, triển khai ledger riêng theo spec

- **Ưu tiên / bằng chứng:** P1 · CONFIRMED.
- **Dấu vết mã nguồn:** `FinanceContext.tsx` L1077–L1094; `web/src/components/SavingSpinModal.tsx` L23–L51; `docs/BA_SPEC.md` BR-SS-03..08.
- **Vấn đề / lý do:** `addSpinReward()` cộng thưởng vào ví dạng thu nhập và chọn kết quả random client, trái contract Saving Spin ledger riêng và không tự tạo Thu/Chi.
- **Phạm vi cho Codex:** Saving Spin domain/repository/UI; task này chặn sai semantics và định nghĩa contract, có thể tách UI đầy đủ thành task sau.
- **Hướng điều tra/triển khai:** Không nối `SavingSpinModal` cũ vào production; thay logic bằng session persisted, anti-reroll, idempotent result, correct `SPUN_PENDING/COMPLETED/SKIPPED` states; không cộng ví/Income ngoài spec.
- **Tiêu chí nghiệm thu:**
  - [ ] Quay/xác nhận/skip không phát sinh `income` ảo.
  - [ ] Reload hoặc hai thiết bị không quay lại cùng một phiên để nhận thưởng mới.
  - [ ] Ledger spin và streak dùng duy nhất session đã hoàn thành.
- **Kiểm thử riêng:** Emulator concurrent spin, refresh/reopen, no wallet mutation, state transitions and rules.
- **Phụ thuộc / Gate:** FW-001; đối chiếu `FINLUX_SAVING_SPIN_IMPLEMENTATION_PLAN.md`.

#### [ ] FW-032 — Thêm CI riêng Web và đồng bộ Node version cho Functions

- **Ưu tiên / bằng chứng:** P1 · GAP.
- **Dấu vết mã nguồn:** `.github/workflows/ci.yml`; `.github/workflows/firebase-ci.yml` (Node 20); `functions/package.json` (Node 22); `web/package.json`.
- **Vấn đề / lý do:** Push thay `web/**` chưa có một job Web rõ ràng bắt buộc; Firebase CI dùng Node 20 khác engine Node 22 khai báo.
- **Phạm vi cho Codex:** `.github/workflows/` và scripts nếu thật sự cần; không sửa product behavior.
- **Hướng điều tra/triển khai:** Bổ sung workflow chạy `npm ci`, test, lint, `tsc --noEmit`, build cho web; Functions trên Node 22, check/test Rules trong emulator; upload log/summary lỗi rõ.
- **Tiêu chí nghiệm thu:**
  - [ ] PR đổi `web/**` gây test fail thì check đỏ.
  - [ ] CI chạy đúng Node engines và không dùng secret Firebase production khi chạy test.
  - [ ] Branch/PR checks có khả năng làm gate trước deploy; không tự bật branch protection.
- **Kiểm thử riêng:** Chạy workflow thử từ PR, intentionally failing test trên nhánh thử riêng, verify report.
- **Phụ thuộc / Gate:** Không; ưu tiên triển khai trước khi sửa nhiều task.

#### [ ] FW-033 — Thêm Firebase Rules/atomic mutation regression suite

- **Ưu tiên / bằng chứng:** P1 · GAP.
- **Dấu vết mã nguồn:** `functions/test/firestore.rules.test.ts`; `web/test/finance.test.mjs`; `firestore.rules`.
- **Vấn đề / lý do:** Test hiện có nghiêng về pure helpers và rollback mocks; cần kiểm chứng integrity khi ghi bằng SDK thực qua Rules Emulator.
- **Phạm vi cho Codex:** Emulator test harness và fixtures; KHÔNG đụng dữ liệu thật.
- **Hướng điều tra/triển khai:** Tạo suite user A/B, balance mutation, transaction create/update/delete, debt/goal, transfer, budget, timezones, malformed payload, concurrent retries. Chạy task FW-010/011/012 qua suite.
- **Tiêu chí nghiệm thu:**
  - [ ] Test từ chối hành vi phá invariant, cho phép flow chuẩn của cả Android/Web.
  - [ ] Có nhánh test dữ liệu legacy và transaction failure.
  - [ ] Chạy lặp không phụ thuộc trạng thái test trước.
- **Kiểm thử riêng:** `npm --prefix functions run check`; `npx firebase-tools emulators:exec --only firestore --project finlux-test "npm --prefix functions test"`.
- **Phụ thuộc / Gate:** FW-001; thiết lập sớm trước khi siết Rules.

### Cụm C — Trải nghiệm, kỹ thuật nợ và hoàn thiện sản phẩm

#### [ ] FW-034 — Không trả dữ liệu demo dưới tên API summary production

- **Ưu tiên / bằng chứng:** P2 · CONFIRMED.
- **Dấu vết mã nguồn:** `web/src/app/api/summary/route.ts` L1–L37; `web/src/lib/constants.ts`.
- **Vấn đề / lý do:** `/api/summary` trả các số giả từ `INITIAL_*` kèm timestamp thực, dễ bị hiểu là số liệu tài khoản; route không dùng Auth.
- **Phạm vi cho Codex:** Route API, tests, readme/API contract.
- **Hướng điều tra/triển khai:** Chọn xóa/disable endpoint, hoặc đổi rõ thành `/api/demo/summary` gắn nhãn giả, hoặc thiết kế API authenticated thật với server verification; tuyệt đối không expose dữ liệu của user khác.
- **Tiêu chí nghiệm thu:**
  - [ ] Không còn endpoint production trình bày số demo như KPI thật.
  - [ ] Nếu có API thật phải kiểm token/UID, từ chối anonymous và không cache sai user.
  - [ ] Tài liệu API mô tả nguồn dữ liệu rõ.
- **Kiểm thử riêng:** HTTP tests anonymous/authenticated/demo mode và response caching.
- **Phụ thuộc / Gate:** FW-028 nếu chọn API authenticated.

#### [ ] FW-035 — Chỉ render một Toast toàn ứng dụng

- **Ưu tiên / bằng chứng:** P2 · CONFIRMED.
- **Dấu vết mã nguồn:** `web/src/components/Providers.tsx` L4–L12; `web/src/app/page.tsx` L18, L132.
- **Vấn đề / lý do:** `Toast` đang được mount hai lần trong trang đã đăng nhập, có thể báo trùng và tạo hai timer.
- **Phạm vi cho Codex:** Provider/AppShell render + smoke test.
- **Hướng điều tra/triển khai:** Giữ singleton toast host ở cấp Providers, xóa instance con; không đổi data context.
- **Tiêu chí nghiệm thu:**
  - [ ] Một sự kiện chỉ hiện một toast với một live region.
  - [ ] Logout/route transition không nhân đôi toast.
  - [ ] Tự ẩn đúng thời gian.
- **Kiểm thử riêng:** UI test duy nhất 1 phần tử `#fx-toast` khi showToast.
- **Phụ thuộc / Gate:** Không.

#### [ ] FW-036 — Chuẩn hóa modal focus/keyboard/accessibility

- **Ưu tiên / bằng chứng:** P2 · GAP.
- **Dấu vết mã nguồn:** `TransactionModal.tsx`, `WalletModal.tsx`, `TransactionDetailModal.tsx`, `DebtsView.tsx`.
- **Vấn đề / lý do:** Một số modal có `role=dialog`, nhưng chưa thấy hệ thống focus trap, restore focus, Escape thống nhất; UI nợ có overlay tự dựng.
- **Phạm vi cho Codex:** Reusable modal primitives/focus helpers, 1–2 form pilot rồi migrate từng màn, QA.
- **Hướng điều tra/triển khai:** Đặt tên dialog via aria-labelledby, focus đầu vào, trap focus, Escape, restore focus sau đóng, scroll lock, nhãn lỗi và phím Enter; dùng design tokens.
- **Tiêu chí nghiệm thu:**
  - [ ] Chỉ bàn phím vẫn tạo/xem/đóng được form.
  - [ ] Không tab ra phía sau dialog mở.
  - [ ] Screen reader đọc tên dialog và lỗi lưu.
- **Kiểm thử riêng:** Keyboard E2E & axe-style audit, mobile viewport.
- **Phụ thuộc / Gate:** FW-019, FW-025 nếu sửa chung modal.

#### [ ] FW-037 — Đo và tối ưu landing 3D có kiểm chứng

- **Ưu tiên / bằng chứng:** P2 · VERIFY.
- **Dấu vết mã nguồn:** `web/src/components/views/LandingPage3D.tsx` (Three.js/WebGL); `web/src/app/layout.tsx` fonts.
- **Vấn đề / lý do:** Landing có nhiều effect, WebGL và assets; chưa có kết quả đo thực tế nên đây là hạng mục performance audit, không khẳng định đã chậm.
- **Phạm vi cho Codex:** Landing lazy-load strategy, image asset handling, performance budget/tests.
- **Hướng điều tra/triển khai:** Đo LCP, INP, CLS, initial JS, memory, FPS trên desktop/mobile; tôn trọng `prefers-reduced-motion`; giữ cleanup WebGL hiện có, có fallback nếu noGL.
- **Tiêu chí nghiệm thu:**
  - [ ] Có bảng before/after trên thiết bị và throttling giống nhau.
  - [ ] Không phá CTA login/register hay nội dung SEO khi vô hiệu 3D.
  - [ ] Không thêm animation liên tục khi tab background.
- **Kiểm thử riêng:** Lighthouse/Web Vitals, mobile low-end smoke, reduced motion, WebGL unavailable.
- **Phụ thuộc / Gate:** Không; không refactor mù.

#### [ ] FW-038 — Làm rõ Web/PWA/offline thực tế và trạng thái mất mạng

- **Ưu tiên / bằng chứng:** P2 · VERIFY.
- **Dấu vết mã nguồn:** `web/public/manifest.json`; `web/src/lib/firebase.ts`; `web/README.md`.
- **Vấn đề / lý do:** Có manifest PWA nhưng chưa thấy Service Worker/offline caching strategy; cần audit trước khi hứa offline-first như Android.
- **Phạm vi cho Codex:** PWA/service-worker (nếu chọn), Firebase persistence/cache, state/reconnect UI, docs.
- **Hướng điều tra/triển khai:** Quyết định web online-first hay offline-first, dữ liệu nào được cache an toàn; kiểm multiple tabs and pending writes; tách `offline` khỏi `synced`.
- **Tiêu chí nghiệm thu:**
  - [ ] Thông báo offline phản ánh đúng có/không thể ghi dữ liệu.
  - [ ] Không mất hoặc ghi hai lần khi reconnect trong kịch bản được hỗ trợ.
  - [ ] Manifest/icons/installability đạt yêu cầu sau khi triển khai PWA thực.
- **Kiểm thử riêng:** DevTools offline/online, reload/reconnect, two tabs, installability checks.
- **Phụ thuộc / Gate:** FW-028, FW-027.

#### [ ] FW-039 — Cập nhật tài liệu Web cũ tham chiếu `website/` thay vì `web/`

- **Ưu tiên / bằng chứng:** P2 · CONFIRMED.
- **Dấu vết mã nguồn:** `docs/FINLUX_WEB_PLATFORM_IMPLEMENTATION_PLAN.md` L15/L119; cây repo thực tế `web/` và `website/` tĩnh.
- **Vấn đề / lý do:** Kế hoạch cũ coi `website/` là ứng dụng Next.js, có thể khiến Codex sửa landing tĩnh thay vì FinLux Web thực.
- **Phạm vi cho Codex:** Chỉ tài liệu kế hoạch Web/index/AGENTS liên quan; không di chuyển project tự động.
- **Hướng điều tra/triển khai:** Đính chính baseline hiện tại, phân định rõ `web/` Next.js on Render và `website/` static/GitHub Pages; reconcile roadmap mục đã triển khai vs chưa; link file backlog này.
- **Tiêu chí nghiệm thu:**
  - [ ] Không còn hướng dẫn triển khai Next.js nhầm vào `website/`.
  - [ ] Mỗi phase mô tả trạng thái đã kiểm chứng và thư mục đúng.
  - [ ] Không phá lịch sử quyết định hoặc tự đổi nền tảng deploy.
- **Kiểm thử riêng:** Docs review với `web/README.md`, `render.yaml`, `.github/workflows/pages.yml`.
- **Phụ thuộc / Gate:** Không.

#### [ ] FW-040 — Đối chiếu chính sách xóa tài khoản với chức năng thực

- **Ưu tiên / bằng chứng:** P2 · VERIFY.
- **Dấu vết mã nguồn:** `web/src/app/terms/page.tsx` L566; `web/src/components/views/SettingsView.tsx`.
- **Vấn đề / lý do:** Chính sách nêu quyền xóa dữ liệu nhưng Settings Web không có luồng xóa; cần xem Android, Firebase Auth và Cloud Functions đã hỗ trợ đến đâu.
- **Phạm vi cho Codex:** Policy text, account deletion UX/API, test; không xóa tài khoản production khi audit.
- **Hướng điều tra/triển khai:** Xác minh đường xóa trên Android và backend, re-auth/confirmation, cleanup các subcollections, storage, FCM; hoặc chỉnh cách diễn đạt chính sách cho chính xác nếu tính năng chưa có.
- **Tiêu chí nghiệm thu:**
  - [ ] Người dùng có quy trình thực thi được để yêu cầu/xóa tài khoản theo cam kết.
  - [ ] Xóa account có kiểm quyền, xác nhận và kiểm thử dữ liệu liên quan.
  - [ ] Không khẳng định đã xóa khi còn dữ liệu tồn dư.
- **Kiểm thử riêng:** Staging-only end-to-end, permission/isolation, failed cleanup retry; kiểm legal/content.
- **Phụ thuộc / Gate:** Không; cần quyết định sản phẩm và quyền riêng tư.

#### [ ] FW-041 — Refactor `FinanceContext` theo domain mà không đổi hành vi

- **Ưu tiên / bằng chứng:** P2 · CONFIRMED.
- **Dấu vết mã nguồn:** `web/src/context/FinanceContext.tsx` (~1185 dòng) chứa Auth + queries + calculations + writes.
- **Vấn đề / lý do:** Single Context lớn làm tăng blast radius, khó test và dễ re-render ngoài phạm vi.
- **Phạm vi cho Codex:** Tách các services/hooks/domain repositories, không đổi schema/contract trong task refactor.
- **Hướng điều tra/triển khai:** Di chuyển từng cụm nhỏ: Auth state, wallet/transaction reads, reports selectors, write commands; bảo toàn external API hoặc migrate từng view có kiểm thử.
- **Tiêu chí nghiệm thu:**
  - [ ] Component UI không thao tác Firestore trực tiếp; domain calculations thuần TS.
  - [ ] Các unit tests parity chạy xanh, không xuất hiện duplicate listeners.
  - [ ] Có so sánh số render/read-count trước và sau.
- **Kiểm thử riêng:** Typecheck, unit tests, UI regression, Emulator flows, React profiling.
- **Phụ thuộc / Gate:** FW-002..FW-018 liên quan; làm theo từng cụm sau khi khóa invariant.

#### [ ] FW-042 — Báo cáo: kỳ tùy chỉnh, đối chiếu kỳ trước và drill-down

- **Ưu tiên / bằng chứng:** P2 · GAP.
- **Dấu vết mã nguồn:** `web/src/lib/financeCalculations.ts` L11–L92; `ReportsView.tsx`.
- **Vấn đề / lý do:** Bộ lọc mới có 3/6 tháng, tháng này/tháng trước/tất cả; chưa có custom range và drill-down từ biểu đồ xuống ledger tương ứng.
- **Phạm vi cho Codex:** Report period selector, readonly queries, chart interactions, tests.
- **Hướng điều tra/triển khai:** Thêm start/end custom inclusive UI nhưng query [start,endExclusive); so sánh với kỳ tương đương; deep-link vào TransactionsView kèm filter và timezone.
- **Tiêu chí nghiệm thu:**
  - [ ] Lọc 01/10–15/10 chỉ lấy giao dịch đúng ngày, không dính 16/10.
  - [ ] Nhấn một cột/danh mục mở đúng ledger cấu thành số liệu.
  - [ ] Export (nếu có) dùng đúng filter UI.
- **Kiểm thử riêng:** Period boundary, user timezone, legacy data, chart cross-filter.
- **Phụ thuộc / Gate:** FW-014, FW-015, FW-026, FW-027.

## 3. Ma trận kiểm thử hồi quy ưu tiên

| Luồng | Các case tối thiểu | KPI/Invariant cần đối chiếu | Task liên quan |
|---|---|---|---|
| Thu/chi | add / edit / delete / backdate / two clients | Số dư, living expense, budget, kỳ đúng | FW-001, 007, 008, 021 |
| Transfer | pair mới + legacy, double click, source=target, concurrent | Tổng tài sản không đổi, báo cáo không Thu/Chi | FW-008, 009, 010, 011 |
| Credit card | quẹt thẻ / thanh toán sao kê / trả một phần | Chi tiêu 1 lần, công nợ thẻ giảm, không đếm đôi Net Worth | FW-002, 004 |
| Nợ vay | trả gốc/lãi, 2 client, reverse, thiếu ví | Wallet + Debt + Ledger atomic; phân loại lãi/gốc | FW-003, 007, 012 |
| Goal | nạp / rút / reverse / retry / two devices | Wallet + Goal + Ledger atomic, không Income/Expense giả | FW-005, 006, 007, 024 |
| Deal | outlay / recovery / gain / loss | Không đếm hoàn vốn thành thu nhập | FW-015 |
| Saving Spin | ready/spun/completed/skipped/reopen | Không ghi income ảo, 1 kết quả mỗi phiên | FW-031 |
| Ngân sách kỳ lương | tháng/2 kỳ/cuối năm/28-31/múi giờ | `periodKey` đúng, 80/100 cảnh báo, không nhân limit | FW-013, 014, 022 |
| Auth và Cloud | sign-in/slow/offline/account switch | Không lộ dữ liệu tài khoản cũ, status thật | FW-028, 029, 030 |
| Báo cáo | historical/all/custom/export/large data | Dashboard = Reports = Export DTO | FW-002, 015, 026, 027, 042 |

## 4. Những điều KHÔNG được hiểu sai từ audit

1. Chưa chạy test/Emulator/đo Web Vitals trong đợt rà soát tạo backlog; công thức/lỗi đã chỉ ra là **đọc mã nguồn**, không phải incident production đã được xác minh.
2. Web đã có nhiều biện pháp đúng (Firestore transaction cho income/expense/transfer, rollback transfer pair, timezone conversion, dynamic report periods, reduced-motion/cleanup WebGL, unit regressions); **không viết lại từ đầu**.
3. `docs/BACKLOG.md` ghi nhận đợt sửa Web ngày 2026-10-21 (33 regression cases PASS theo tài liệu); các task này là **tồn đọng/rủi ro bổ sung tại baseline mới**, không phủ định kết quả đã bàn giao.
4. `web/src/components/views/DebtsView.tsx` và `SavingSpinModal.tsx` tồn tại nhưng chưa được nối vào main `page.tsx`; phân biệt **code latent** với chức năng user đang dùng.
5. Những nội dung còn khác nhau giữa BA spec/Architecture Matrix (ví dụ Saving Spin, credit liabilities, deal flow) phải được giải quyết ở bước B3/B4 trước khi đổi schema/logic.
6. Không thêm người phụ trách, deadline, tuyên bố đã fix hay đẩy production khi chưa có bằng chứng/ủy quyền.

## 5. Các đường dẫn nguồn (khởi động nhanh)

- [Web Next.js source](https://github.com/khoaiprovip123/FinLux/tree/main/web) — xem tại repo: `web/`.
- [Web app shell](https://github.com/khoaiprovip123/FinLux/blob/main/web/src/app/page.tsx) — đường dẫn: `web/src/app/page.tsx`.
- [Web finance context](https://github.com/khoaiprovip123/FinLux/blob/main/web/src/context/FinanceContext.tsx) — `web/src/context/FinanceContext.tsx`.
- [Financial helpers](https://github.com/khoaiprovip123/FinLux/blob/main/web/src/lib/financeCalculations.ts) — `web/src/lib/financeCalculations.ts`.
- [Transaction rollback](https://github.com/khoaiprovip123/FinLux/blob/main/web/src/lib/transactionRollback.ts) — `web/src/lib/transactionRollback.ts`.
- [Firestore rules](https://github.com/khoaiprovip123/FinLux/blob/main/firestore.rules) — `firestore.rules`.
- [Cloud Functions](https://github.com/khoaiprovip123/FinLux/blob/main/functions/src/index.ts) — `functions/src/index.ts`.
- [Project agent rules](https://github.com/khoaiprovip123/FinLux/blob/main/AGENTS.md) — `AGENTS.md`.
- [Web platform plan](FINLUX_WEB_PLATFORM_IMPLEMENTATION_PLAN.md) — `docs/FINLUX_WEB_PLATFORM_IMPLEMENTATION_PLAN.md`.
- [Product master](FINLUX_PRODUCT_MASTER_2026.md) — `docs/FINLUX_PRODUCT_MASTER_2026.md`.

---

**Quy tắc cập nhật backlog sau review:** đánh dấu checkbox *chỉ khi* đã đối chiếu diff, bằng chứng test và phê duyệt; ghi PR/commit SHA thực tế vào mục task tương ứng. Mỗi lần Codex làm một ID, không tự nhận toàn bộ backlog.
