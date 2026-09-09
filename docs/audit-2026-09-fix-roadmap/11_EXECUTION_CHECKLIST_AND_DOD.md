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

- [ ] Same FinancialPeriod contract.
- [ ] Transfer semantics đúng.
- [ ] Goal accounting đúng.
- [ ] Debt principal/interest đúng.
- [ ] Deal principal/gain/loss đúng.
- [ ] Opening balance.
- [ ] Closing balance.
- [ ] Previous period.
- [ ] Export parity.
- [ ] Home KPI parity nếu cùng metric.

## 5. Checklist Firebase/Security

- [ ] Owner-only access.
- [ ] Unknown field policy.
- [ ] Mutable/immutable field policy.
- [ ] Storage read/write/delete separated.
- [ ] Receiver exported status reviewed.
- [ ] Debug-only hooks disabled production.
- [ ] App Check impact considered.
- [ ] Index requirements committed.

## 6. Test Gate

- [ ] Unit tests PASS.
- [ ] Repository contract tests PASS.
- [ ] Firebase emulator rules PASS.
- [ ] Functions build PASS.
- [ ] Lint PASS.
- [ ] Compose UI tests compile/pass.
- [ ] Debug APK build PASS.
- [ ] Migration test PASS nếu có.

## 7. Release Gate

- [ ] P0 issues = 0.
- [ ] Production signing secrets present.
- [ ] Không debug fallback.
- [ ] Release APK/AAB generated.
- [ ] Signature verified.
- [ ] SHA-256 generated.
- [ ] Version/tag match.
- [ ] Release notes.
- [ ] Backup/migration ready.
- [ ] Rollback plan.
- [ ] UAT complete.

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

- [ ] Budget salary-period hoạt động Firebase emulator/production-compatible.
- [ ] Salary resolver Kotlin/Functions cùng kết quả.
- [ ] Goal mutation atomic.
- [ ] Debt mutation atomic.
- [ ] Deal flow được Rules hỗ trợ.
- [ ] Capital loss không fake wallet.
- [ ] Storage privacy owner-only.
- [ ] Salary receiver production hardened.
- [ ] Release fail-closed.
- [ ] Contract tests đầy đủ.

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
