# FinLux — Execution Checklist & Definition of Done

## 1. Checklist trước khi bắt đầu một task

- [ ] Xác định Task ID/Phase.
- [ ] Xác định root cause, không chỉ symptom.
- [ ] Xác định business invariant bị ảnh hưởng.
- [ ] Liệt kê Android/Data/Rules/Functions/UI liên quan.
- [ ] Xác định backward compatibility.
- [ ] Xác định migration nếu có.
- [ ] Viết/định nghĩa regression test trước khi sửa.

## 2. Checklist P0 Financial Change

- [ ] Domain validation.
- [ ] Repository implementation.
- [ ] Demo parity.
- [ ] Firestore Rules compatible.
- [ ] Cloud Functions compatible.
- [ ] Wallet delta correct.
- [ ] Ledger entry correct.
- [ ] lastTransactionId/correlation metadata correct.
- [ ] Retry/idempotency considered.
- [ ] Cross-user denied.
- [ ] Emulator integration test PASS.

## 3. Checklist UI Change

- [ ] Shared design tokens.
- [ ] Light mode.
- [ ] Dark mode.
- [ ] Classic.
- [ ] Modern.
- [ ] Prism.
- [ ] 360dp.
- [ ] 412dp.
- [ ] Landscape.
- [ ] Font scale.
- [ ] Keyboard/IME.
- [ ] Bottom navigation clearance.
- [ ] Loading/empty/error.
- [ ] Accessibility label.

## 4. Checklist Report Change

- [x] Same FinancialPeriod contract.
- [x] Transfer semantics đúng.
- [x] Goal accounting đúng.
- [x] Debt principal/interest đúng.
- [x] Deal principal/gain/loss đúng.
- [x] Opening balance.
- [x] Closing balance.
- [x] Previous period.
- [x] Export parity.
- [ ] Home KPI parity nếu cùng metric.

## 5. Checklist Firebase/Security

- [x] Owner-only access.
- [x] Unknown field policy.
- [x] Mutable/immutable field policy.
- [x] Storage read/write/delete separated.
- [x] Receiver exported status reviewed.
- [x] Debug-only hooks disabled production.
- [ ] App Check impact considered.
- [ ] Index requirements committed.

## 6. Test Gate

- [x] Unit tests PASS (34 tasks 100% pass).
- [x] Repository contract tests PASS.
- [x] Firebase emulator rules PASS (42/42 tests pass).
- [x] Functions build PASS.
- [x] Lint PASS.
- [x] Compose UI tests compile/pass.
- [x] Debug APK build PASS (`assembleDebug` thành công).
- [x] Migration test PASS nếu có.

## 7. Release Gate

- [x] P0 issues = 0.
- [x] Production signing secrets configured.
- [x] Không debug fallback (Fail-closed release).
- [x] Release APK/AAB build ready.
- [x] Signature verified.
- [x] SHA-256 verified.
- [x] Version/tag match.
- [x] Release notes đầy đủ (`CHANGELOG.md`).
- [x] Backup/migration ready.
- [x] Rollback plan sẵn sàng.
- [x] UAT complete (kiểm thử thực tế trên thiết bị `7f4ca06a`).

## 8. Definition of Done cho một task

Task chỉ DONE khi:
1. Root cause đã được sửa.
2. Test bắt được lỗi cũ và pass sau fix.
3. Không phá contract layer khác.
4. Docs cần thiết đã cập nhật.
5. CI xanh.
6. Không còn TODO tạm liên quan task.
7. Có commit/PR trace rõ.

## 9. Definition of Done cho Phase 1–2 P0

- [x] Budget salary-period hoạt động Firebase emulator/production-compatible.
- [x] Salary resolver Kotlin/Functions cùng kết quả.
- [x] Goal mutation atomic.
- [x] Debt mutation atomic.
- [x] Deal flow được Rules hỗ trợ.
- [x] Capital loss không fake wallet.
- [x] Storage privacy owner-only.
- [x] Salary receiver production hardened.
- [x] Release fail-closed.
- [x] Contract tests đầy đủ.
- [x] UI Screen Clusters & Design System Parity (PR-09): 100% dynamic theme tokens, no hardcoded colors.
- [x] Performance, Build Hygiene & Release Gate Verification (PR-10): `testDebugUnitTest` 100% PASS, `assembleDebug` APK build thành công, debug logs untracked.

## 10. Definition of Done toàn chương trình

```
Business Rule
    ↓
Domain UseCase
    ↓
Repository Contract
    ↓
Firebase Transaction / Rules / Functions
    ↓
Report Semantics
    ↓
UI
    ↓
Automated Verification
```

Không layer nào được tự định nghĩa lại nghiệp vụ theo cách khác.
