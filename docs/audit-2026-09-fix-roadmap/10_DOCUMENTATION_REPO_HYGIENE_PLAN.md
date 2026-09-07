# Phase Plan — Documentation & Repository Hygiene

## 1. Mục tiêu

Tài liệu trở thành source of truth thay vì nhiều file mô tả các phiên bản khác nhau.

## 2. Phase DOC-1 — Source of Truth

Quy định:
- `BA_SPEC.md`: business/use cases/rules.
- `DATA_SPEC.md`: schema/rules/index/migration.
- `UI_SPEC.md`: UI system + behavior.
- `CONTEXT.md`: architecture.
- `CHANGELOG.md`: released changes.
- `HANDOVER_LOG.md`: operational history.
- Bộ roadmap này: remediation plan sau audit 09/2026.

Không để cùng một contract được mô tả mâu thuẫn ở nhiều file.

## 3. Phase DOC-2 — Update stale version data

Cập nhật:
- README tech versions theo Gradle thực tế;
- DATA_SPEC budget period;
- Salary Cycle path/fields;
- Deal schema;
- Saving Spin schema;
- release process.

## 4. Phase DOC-3 — ADR

Tạo Architecture Decision Records:
- ADR-001 Financial Period Contract;
- ADR-002 Wallet + Ledger Atomicity;
- ADR-003 Goal Accounting;
- ADR-004 Deal Accounting;
- ADR-005 Theme Architecture;
- ADR-006 Release Signing.

## 5. Phase DOC-4 — Repo hygiene

Không commit generated cache/log:
- `firestore-debug.log`;
- `graphify-out/cache/**`;
- generated AST;
- temporary artifacts.

Nếu cần giữ Graphify thì giữ report, ignore cache.

## 6. Phase DOC-5 — Handover log

`HANDOVER_LOG.md` đang rất lớn. Đề xuất archive theo thời gian/version:
```
docs/handover/2026-H1.md
docs/handover/2026-H2.md
HANDOVER_LOG.md → current active window/index
```

Không thực hiện trong P0 nếu tạo diff lớn; làm sau stabilization.

## 7. Phase DOC-6 — Task traceability

Mỗi PR cần:
- Task ID;
- root cause;
- files;
- tests;
- migration;
- risk;
- rollback;
- docs updated.

## 8. Phase DOC-7 — Security docs

Ghi rõ:
- permissions rationale;
- Firebase Rules ownership;
- App Check;
- signing;
- secret handling;
- data retention;
- backup/migration.

## 9. Acceptance

- README phản ánh build thực.
- DATA_SPEC khớp Rules + Repository.
- Không generated cache tăng repo.
- Mỗi P0 fix trace được vào plan/test/PR.
