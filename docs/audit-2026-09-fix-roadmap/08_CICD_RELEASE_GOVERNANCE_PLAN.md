# Phase Plan — CI/CD & Release Governance

## 1. Mục tiêu

Không cho code/release vượt qua khi thiếu test, thiếu production signing hoặc artifact không đúng.

## 2. Phase CI-1 — CI required gates

Workflow PR bắt buộc:
- unit tests;
- lint;
- Firebase Functions build;
- Firestore Rules emulator tests;
- integration contract tests;
- compile/run Android UI smoke phù hợp môi trường;
- assembleDebug.

## 3. Phase CI-2 — Branch Governance

`main` cần:
- pull request required;
- required status checks;
- hạn chế direct push;
- dismiss stale approval khi head thay đổi;
- squash/rebase policy thống nhất.

Nếu GitHub App không có quyền quản trị branch protection thì cấu hình thủ công ở GitHub Settings và ghi nhận vào docs.

## 4. Phase CI-3 — Release fail-closed

Loại bỏ hoàn toàn:
```
missing production key → debug key fallback
missing release APK → debug APK fallback
```

Thay bằng:
```
missing any release secret → FAIL
release artifact missing → FAIL
signature verification fail → FAIL
```

## 5. Phase CI-4 — Artifact verification

Release pipeline:
- assembleRelease;
- verify signing certificate;
- SHA-256;
- generate update.json;
- version/tag guard;
- upload APK/AAB;
- release notes;
- provenance metadata.

## 6. Phase CI-5 — Versioning

Chỉ bump sau khi tests/gates xanh và migration/schema ready.

Semantic version:
- PATCH: compatible bug fix;
- MINOR: feature/behavior extension;
- MAJOR: breaking data/product contract.

## 7. Phase CI-6 — Environment separation

Nên có:
- dev Firebase project;
- staging Firebase project;
- production Firebase project.

Không test destructive flow trên production.

## 8. Phase CI-7 — Deployment order

Khi có schema/rules migration:
1. backward-compatible backend/rules;
2. migration;
3. Functions;
4. Android release;
5. tighten rules sau adoption nếu cần.

## 9. Phase CI-8 — Rollback

Chuẩn bị:
- previous APK/release;
- previous Functions version;
- Rules rollback;
- migration backup;
- feature flag/kill switch cho risky feature nếu cần.

## 10. Acceptance

- Không thể publish debug-signed release.
- Main không merge khi P0 tests fail.
- Release reproducible.
- Có rollback procedure.
