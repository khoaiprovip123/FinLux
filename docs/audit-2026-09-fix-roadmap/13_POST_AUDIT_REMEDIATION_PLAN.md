# FinLux — Post-Audit Remediation Plan 09/2026

> **Trạng thái:** `[IN PROGRESS — LOCAL REMEDIATION PASS]` — chưa được phép sign-off Production Ready.  
> **Ngày lập:** 2026-09-07.  
> **Nguồn:** tái kiểm tra `12_FINAL_AUDIT_EXECUTION_REPORT.md` với code, Rules, test artifacts, Git, APK và signing hiện tại.  
> **Phạm vi:** sửa các blocker còn lại; không thêm tính năng sản phẩm mới trước khi P0 = 0.

## 1. Kết luận tái kiểm tra

Các gate local hiện có giá trị nhưng chưa đủ để kết luận production-ready:

- Android unit tests và debug APK build được.
- Functions typecheck/build pass.
- Firestore Rules emulator hiện có 42 test pass.
- Tuy nhiên vẫn còn đường bypass financial invariant, thiếu Storage Rules emulator tests,
  chưa đạt theme parity và chưa có release artifact production-signed chứa các audit fixes.
- Toàn bộ audit fixes vẫn nằm trong working tree local; remote `main` chưa chứa remediation.

Vì vậy `12_FINAL_AUDIT_EXECUTION_REPORT.md` được xem là **preliminary execution report**.
Chỉ được phát hành báo cáo sign-off mới sau khi toàn bộ gate trong tài liệu này đạt.

## 2. Danh sách vấn đề cần xử lý

| ID | Mức | Vấn đề | Rủi ro | Trạng thái |
|---|---|---|---|---|
| RA-P0-01 | P0 | `walletHasLedgerTransition` chấp nhận transaction cũ không đổi | Client có thể sửa `wallet.balance` bằng stale transaction ID | `[LOCAL DONE]` |
| RA-P0-02 | P0 | Goal/Debt cho phép sửa financial fields trực tiếp | `savedAmount`, `remainingBalance`, payment có thể lệch wallet/ledger | `[LOCAL DONE]` |
| RA-P0-03 | P0 | Deal aggregates và delete chưa server-authoritative | Có thể sửa P/L hoặc xóa Deal để lại transaction/orphan balance | `[CODE DONE / DEPLOY PENDING]` |
| RA-P0-04 | P0 | Release fixes chưa commit/push; version/tag trùng; artifact cũ ký debug | Remote/CI/release không chứa trạng thái đã audit | `[TODO]` |
| RA-P1-01 | P1 | Budget create cho phép spoof aggregate/notification flags | Sai `spentAmount` hoặc mất cảnh báo 80/100% | `[LOCAL DONE]` |
| RA-P1-02 | P1 | Không có Storage Rules emulator suite | Quyền avatar/receipt mới chỉ được kiểm tra tĩnh | `[LOCAL DONE]` |
| RA-P1-03 | P1 | Vẫn còn màu hardcode trong screen cluster | Vi phạm theme consistency và claim PR-09 | `[PRIORITY CLUSTER DONE / FULL SCAN OPEN]` |
| RA-P1-04 | P1 | UAT avatar/permissions/release chưa có evidence tái lập | Code support bị nhầm với device/production verification | `[TODO]` |
| RA-P1-05 | P1 | Home KPI, App Check, indexes và CI trace chưa đóng | Checklist/DoD không khớp trạng thái thực tế | `[TODO]` |
| RA-P2-01 | P2 | `DEAL_SETTLEMENT` còn trong legacy compatibility | Tài liệu ghi “xóa hoàn toàn” không chính xác | `[TODO]` |
| RA-P2-02 | P2 | Repo hygiene còn `git diff --check` warning | Giảm chất lượng gate và review noise | `[LOCAL GATE PASS]` |

## 3. Nguyên tắc triển khai bắt buộc

1. Không sửa balance/aggregate tài chính ngoài Firestore Transaction hoặc server command đã xác thực.
2. Rules phải chống được client cũ, REST client và client bị chỉnh sửa; không chỉ dựa vào Repository Android.
3. Một transaction ID chỉ là bằng chứng khi ledger document thực sự create/update/delete trong cùng request.
4. Financial aggregates của Goal, Debt, Deal và Budget phải xác định rõ owner: client-owned hay server-owned.
5. Mỗi lỗ hổng phải có regression test fail trước fix và pass sau fix.
6. Tách rõ bốn mức bằng chứng: code support, local tests, device UAT, production deploy/release.
7. Không rewrite dữ liệu production trước khi có export/backup, dry-run và kế hoạch rollback.
8. Không commit secret, production keystore hoặc Firebase service-account key.

## 4. Dependency và thứ tự thực hiện

```text
R-00 Freeze & Rebaseline
  ↓
R-01 Wallet/Ledger Proof
  ↓
R-02 Goal & Debt Invariants
  ↓
R-03 Deal Server Authority & Cascade
  ↓
R-04 Budget Aggregate Ownership
  ├───────────────┐
  ↓               ↓
R-05 Storage QA   R-06 Theme Parity
  └───────┬───────┘
          ↓
R-07 Full Regression & Data Reconciliation
          ↓
R-08 Version, CI/CD, Deploy & Device UAT
          ↓
R-09 Corrected Final Sign-off
```

Không bắt đầu R-08 khi R-01 đến R-07 chưa đạt toàn bộ acceptance criteria.

## 5. Kế hoạch theo remediation PR

### R-00 — Freeze, preserve và rebaseline working tree

**Mục tiêu:** bảo toàn thay đổi hiện có và tạo baseline có thể truy nguyên trước khi sửa tiếp.

**Công việc:**

- Ghi lại branch, HEAD, remote `main`, tag, version và toàn bộ dirty/untracked files.
- Không reset/stash/xóa thay đổi chưa xác định ownership.
- Tách audit remediation sang branch riêng khi bắt đầu implementation.
- Chốt baseline test: Android, Functions, Firestore Rules và APK metadata.
- Xác định file generated/ignored; dọn log mà không xóa source/user data.

**Acceptance:**

- Có snapshot/diff có thể phục hồi.
- `git status` được phân loại rõ: source, docs, tests, generated artifacts.
- Không mất bất kỳ thay đổi audit nào.

### R-01 — Khóa Wallet ↔ Ledger transition thật

**Phạm vi chính:** `firestore.rules`, `functions/test/firestore.rules.test.ts`,
`FirebaseWalletMutation.kt`, các Firebase repositories.

**Thiết kế:**

- Thay logic chỉ kiểm tra `existsAfter()` bằng kiểm tra transition thực:
  - create: trước không tồn tại, sau tồn tại;
  - update: trước/sau tồn tại và ledger data thực sự thay đổi;
  - delete: trước tồn tại, sau không tồn tại.
- Bind chính xác `walletId`, transaction ID và delta trước/sau của wallet.
- Không chấp nhận no-op write hoặc stale transaction ID.
- Giữ đúng semantics cho expense, income, transfer hai leg, Goal, Debt, Deal split và delete rollback.

**Regression tests bắt buộc:**

- Deny đổi balance với transaction ID không tồn tại.
- Deny đổi balance khi tái sử dụng transaction cũ cùng wallet.
- Deny no-op ledger write dùng để hợp thức hóa balance tùy ý.
- Deny transaction đúng ID nhưng sai wallet hoặc sai delta.
- Allow exact create/update/delete delta.
- Allow paired transfer và Deal split đúng tổng delta.

**Acceptance:** không còn đường đổi `wallet.balance` nếu ledger không thực sự chuyển trạng thái tương ứng.

### R-02 — Goal và Debt financial invariants

**Phạm vi chính:** Goal/Debt use cases, Firebase repositories, Demo parity,
`firestore.rules` và Android/Rules tests.

**Goal:**

- `savedAmount` không được client sửa trực tiếp ngoài deposit/withdraw command.
- Deposit/withdraw phải atomically cập nhật Goal, Wallet và Transaction ledger.
- Bind correlation/transaction ID trên cả Goal và Wallet khi cần cho Rules.
- Giữ rule cấm xóa Goal khi `savedAmount > 0`.

**Debt:**

- Bắt buộc `amount == principalPaid + interestPaid`.
- `principalPaid` không vượt remaining principal trước payment.
- Payment, Debt remaining, Wallet balance và ledger phải cùng atomic command.
- Không cho client sửa trực tiếp financial aggregates của Debt.
- Không cho delete parent để lại `payments`; ưu tiên server-side cascade command hoặc soft-delete contract.

**Regression tests:** direct aggregate update, overpayment, invalid split, wrong wallet delta,
duplicate payment/correlation ID và orphan delete đều phải bị deny.

### R-03 — Deal authority, settlement và cascade

**Phạm vi chính:** Deal repository/use cases, Rules, Functions nếu dùng server command, Demo và tests.

**Công việc:**

- Khóa client mutation trực tiếp của `totalCapitalOutlay`, `totalRecovered`, `netProfitLoss` và status tài chính.
- Outlay/inflow/split/loss phải tạo ledger/business event atomically với đúng wallet delta.
- `CAPITAL_LOSS` tiếp tục là non-cash accounting entry, không tạo fake wallet mới.
- Delete Deal phải hoàn tác wallet theo ledger và xóa/cô lập toàn bộ transaction liên quan bằng server-authoritative cascade.
- Giữ đọc tương thích `DEAL_SETTLEMENT` chỉ trong migration window; cấm ghi mới.

**Regression tests:** spoof P/L, direct delete có ledger, partial cascade, split sai counterpart,
double settlement và legacy fake-wallet write phải bị deny.

### R-04 — Budget aggregate ownership và notification integrity

**Phạm vi chính:** `firestore.rules`, Functions budget reconciliation, Budget mapper/repository và tests.

**Công việc:**

- Budget create từ client chỉ được phép khởi tạo `spentAmount = 0`, `notified80 = false`, `notified100 = false`
  hoặc không gửi ba field server-owned này.
- Update từ client chỉ được đổi contract/hạn mức đã được BA_SPEC cho phép.
- Functions là bên duy nhất reconcile aggregate từ transaction ledger.
- Chốt nghiệp vụ reset notification flags khi chi tiêu giảm hoặc kỳ/hạn mức đổi; nếu đặc tả chưa rõ thì ghi
  `[Cần xác nhận]` trước implementation.
- Kiểm tra idempotency của notification IDs và trigger không lặp vô hạn.

**Regression tests:** spoof aggregate trên create/update, suppress flag, legacy period,
salary period, edit/delete transaction và threshold 80/100.

### R-05 — Storage Rules emulator suite

**Phạm vi chính:** `storage.rules`, `firebase.json`, `functions/package.json`, test Storage mới.

**Test matrix:**

- Owner read/create/update/delete avatar và receipt.
- Anonymous/cross-user bị deny.
- MIME không phải ảnh, file rỗng và file vượt 5 MB bị deny.
- Read/delete chạy đúng khi không có `request.resource`.
- Đường dẫn/extension ngoài contract bị deny.
- Metadata JPEG từ avatar uploader tương thích Rules.

**Acceptance:** có command emulator riêng hoặc combined gate chạy Firestore + Storage và trả exit code 0.

### R-06 — Theme tokens và UI verification

**Phạm vi ưu tiên:** `TransferMoneyScreen.kt`, `WalletTransactionsBottomSheet.kt`,
`PrismTransactionsScreen.kt`, sau đó scan toàn app.

**Công việc:**

- Loại `Color.White`, `Color.Black` và `Color(0x...)` khỏi product UI theo AGENTS.md.
- Dùng `LocalFinluxTokens.current`, `MaterialTheme.colorScheme` hoặc semantic `FinluxColors` đã được phê duyệt.
- Nếu có ngoại lệ thương hiệu/logo, ghi exception ở design system thay vì hardcode rải rác.
- Kiểm tra Light/Dark và Classic/Modern/Prism ở 360dp, 412dp, landscape và font scale.

**Acceptance:** static scan không còn hardcode ngoài allowlist có giải thích; có screenshot/evidence cho theme matrix.

### R-07 — Full regression, reconciliation và migration readiness

**Automated gates:**

1. `gradlew testDebugUnitTest`.
2. `gradlew lintDebug`.
3. Compose UI/instrumentation smoke nếu test target sẵn sàng.
4. Functions `check` + `build`.
5. Firestore Rules emulator, gồm toàn bộ regression mới.
6. Storage Rules emulator.
7. `assembleDebug` và `git diff --check`.

**Data audit:**

- Viết/read-only reconciliation để tìm Wallet không khớp ledger, Goal/Debt/Deal aggregate bất thường,
  orphan payments/transactions và legacy `DEAL_SETTLEMENT`.
- Chạy dry-run trước; xuất danh sách document/path và expected delta, không log dữ liệu nhạy cảm.
- Chỉ apply migration sau backup/export và phê duyệt rõ ràng.
- Sau apply phải read-back và chạy reconciliation lần hai với discrepancy = 0.

### R-08 — Version, remote CI/CD, Firebase deploy và device UAT

**Version/repository:**

- Không tái sử dụng `v1.22.0`; đề xuất release candidate `v1.23.0` vì phạm vi thay đổi rộng về integrity/security.
- Bump `versionCode` đúng workflow khi thực sự chuẩn bị release.
- Mỗi remediation PR có commit trace; merge qua required checks.
- Remote workflow phải chứa fail-closed signing trước khi trigger release.

**Signing/artifact:**

- Cấu hình đủ bốn production signing secrets trên CI.
- Negative gate: thiếu secret hoặc debug key phải fail.
- Positive gate: build release thành công bằng production key.
- `apksigner verify --print-certs` xác nhận signer không phải `CN=Android Debug`.
- Sinh và xác minh SHA-256; versionName/versionCode/tag phải khớp.

**Firebase:**

- Deploy Rules/Functions/Indexes chỉ khi có đúng project/credential và người dùng cho phép.
- Lưu command output/deployment IDs; smoke test production-compatible sau deploy.
- Có rollback cho Rules, Functions và migration.

**Device UAT:**

- Cài đúng release-signed artifact, không dùng debug APK làm bằng chứng release.
- Test Transaction/Transfer, Goal, Debt, Deal, Budget, Reports, first-launch permissions,
  avatar Camera/Gallery và theme matrix.
- Ghi serial/model, app version, artifact SHA, timestamp và kết quả read-back; không chỉ ghi PID.

### R-09 — Corrected final report và sign-off

- Cập nhật `11_EXECUTION_CHECKLIST_AND_DOD.md` theo bằng chứng thực, không đánh dấu trước.
- Giữ `12_FINAL_AUDIT_EXECUTION_REPORT.md` làm báo cáo preliminary hoặc sửa rõ trạng thái superseded.
- Tạo corrected execution report chỉ sau khi R-01 đến R-08 đều `[DONE]`.
- Báo cáo cuối phải liệt kê commit/PR, test counts, CI run, signer fingerprint, artifact SHA,
  Firebase deployment evidence, UAT và các giới hạn còn lại.

## 6. Release hard-stop gates

Không được ghi `Production Ready` nếu bất kỳ điều kiện nào sau đây chưa đạt:

- [ ] RA-P0-01 đến RA-P0-04 đã đóng bằng regression tests.
- [ ] Không còn direct financial aggregate mutation trái contract.
- [ ] Firestore và Storage emulator tests pass.
- [ ] Android unit/lint/UI gates pass.
- [ ] Reconciliation production dry-run không còn discrepancy chưa giải thích.
- [ ] Audit fixes đã commit, push và CI chạy trên đúng commit.
- [ ] Version/tag chưa tồn tại và khớp artifact.
- [ ] Release artifact ký production, không phải debug certificate.
- [ ] Firebase deployment được xác minh hoặc báo cáo ghi rõ chưa deploy.
- [ ] Device UAT chạy trên chính release artifact.
- [ ] Rollback và backup sẵn sàng.
- [ ] `git diff --check` sạch và repo không còn generated logs tracked.

## 7. Definition of Done cho từng remediation PR

Một PR chỉ `[DONE]` khi:

1. Có test tái hiện lỗi cũ và test đó fail trên behavior cũ.
2. Code/Rules/Functions cùng tuân thủ một contract.
3. Existing tests và regression mới đều pass với command có exit code 0.
4. Tài liệu liên quan và `HANDOVER_LOG.md` đã đồng bộ.
5. Có commit/PR trace rõ; không dùng working tree local làm bằng chứng production.
6. Nếu liên quan thiết bị/deploy, có read-back hoặc artifact/deployment evidence tương ứng.

## 8. Trạng thái triển khai

| Remediation | Trạng thái | Ghi chú |
|---|---|---|
| R-00 Freeze & Rebaseline | `[DONE]` | Branch `codex/post-audit-remediation-2026-09`; giữ nguyên dirty worktree |
| R-01 Wallet/Ledger Proof | `[LOCAL DONE]` | Stale/no-op/metadata-only proof bị deny; exact delta pass |
| R-02 Goal & Debt | `[LOCAL DONE]` | Correlation + atomic payment; Debt direct delete bị deny |
| R-03 Deal | `[CODE DONE / DEPLOY PENDING]` | Aggregate-bound ledger + callable cascade |
| R-04 Budget | `[LOCAL DONE]` | Create aggregate/flags bắt buộc 0/false |
| R-05 Storage QA | `[LOCAL DONE]` | 4 Storage cases; suite tổng 50/50 pass |
| R-06 Theme Parity | `[LOCAL DONE]` | Đã chuẩn hóa hệ màu động (tokens) trên toàn bộ cụm Wallets, Transactions, Settings, Salary Cycle & Updater |
| R-07 Regression & Reconciliation | `[LOCAL PASS / PROD PENDING]` | Android 308/308; emulator 50/50; assembleDebug pass; UAT device online |
| R-08 Release/Deploy/UAT | `[IN PROGRESS]` | Đã cài đặt và kiểm thử APK trên thiết bị `7f4ca06a` (PID 31919); còn chờ production signing & deploy cloud |
| R-09 Corrected Sign-off | `[IN PROGRESS]` | Đang cập nhật tài liệu kiểm chứng và DoD |
