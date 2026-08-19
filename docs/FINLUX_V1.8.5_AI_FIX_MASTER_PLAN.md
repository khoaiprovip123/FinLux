FINLUX v1.8.5 — SECURITY & RELEASE HARDENING

Baseline: FinLux v1.8.4 / versionCode 103
Target đề xuất: v1.8.5 / versionCode 104 sau khi toàn bộ quality gate PASS.
Mục tiêu: Security → Release/OTA → Test reliability → Finance timezone.
Không thêm feature mới, không redesign UI, không rewrite project.

1. Nguyên tắc bắt buộc

Không đổi applicationId = com.finlux.app.

Không đổi Firebase project.

Không migration destructive.

Không đổi Money(Long) sang Double/Float.

Không commit production keystore hoặc password.

Không dùng Debug APK làm production OTA release.

Không tự bump version trước khi toàn bộ phase PASS.

Một nhóm logic = một commit.

Mỗi task phải theo chu trình:

READ → UNDERSTAND → TEST → PATCH → TEST → BUILD → REPORT

2. Thứ tự xử lý

P0-S01 Firestore Rules
↓
P0-S02 Release Signing
↓
P0-S03 Split CI / Release
↓
P0-S04 OTA Integrity Verification
↓
P0-T01 Fix Flaky Tests
↓
P0-T02 Complete Transaction Test Matrix
↓
P1-TZ01 Finance Timezone Strategy
↓
FINAL QUALITY GATE
↓
v1.8.5 / versionCode 104

3. P0-S01 — Fix Firestore Rules bypass

Severity

CRITICAL

File

firestore.rules

Vấn đề

Hiện các collection có rule riêng nhưng cuối file vẫn có wildcard kiểu:

match /{subcollection}/{docId} {
    allow read, write: if isOwner(uid);
}

Rule wildcard có thể làm bypass validation riêng của transactions, wallets, budgets.

Hướng xử lý

Chuyển sang:

DEFAULT DENY
+
EXPLICIT ALLOW

Yêu cầu

Xóa catch-all allow write.

Khai báo explicit từng collection.

Unknown collection = deny.

Giữ backward compatibility với payload hiện tại.

Đọc mapper/repository trước khi viết schema validation.

Schema tối thiểu

Transactions

type ∈ income/expense/transfer_out/transfer_in
amount: int > 0
walletId: string
categoryId: string|null
relatedWalletId: string|null
date: timestamp
createdAt: timestamp
updatedAt: timestamp

Wallets

name: string
type: allowed wallet type
balance: int
color: string
isDefault: bool
createdAt: timestamp

Budgets

categoryId: string
month: string
limitAmount: int >= 0
spentAmount: int >= 0
notified80: bool
notified100: bool

Acceptance Criteria

Không còn wildcard allow write.

Invalid transaction bị reject.

Amount <= 0 bị reject.

Invalid wallet bị reject.

Invalid budget bị reject.

User A không truy cập data User B.

Existing valid app writes vẫn chạy.

Có Firebase Emulator Rules test nếu khả thi.

4. P0-S02 — Production Release Signing

Severity

CRITICAL

Vấn đề

OTA production hiện không được phép dùng:

assembleDebug
→ app-debug.apk
→ GitHub Release

Target

assembleRelease
→ Private Release Keystore
→ Signed Release APK
→ Verify
→ GitHub Release

GitHub Secrets đề xuất

FINLUX_KEYSTORE_BASE64
FINLUX_KEYSTORE_PASSWORD
FINLUX_KEY_ALIAS
FINLUX_KEY_PASSWORD

Quy tắc

Production keystore không nằm trong repo.

Password không hardcode.

Một release key được giữ lâu dài.

Không tạo key mới cho mỗi version.

CI decode key tạm thời, build xong phải xóa.

Acceptance Criteria

Production APK là Release APK.

APK ký bằng private release certificate.

Không dùng debug keystore cho production.

Không có password/private key trong source.

Signing identity được lưu an toàn ngoài repo.

5. P0-S03 — Tách CI và Release

ci.yml

Trigger:

on:
  pull_request:
  push:
    branches:
      - main

Chạy:

testDebugUnitTest
lintDebug
assembleDebug

Không tạo Release.

release.yml

Trigger:

on:
  push:
    tags:
      - 'v*'
  workflow_dispatch:

Pipeline:

checkout
↓
verify tag == versionName
↓
unit test
↓
lint
↓
assembleRelease
↓
sign
↓
verify APK
↓
generate SHA-256
↓
GitHub Release

Version guard

Nếu tag:

v1.8.5

thì:

app/build.gradle.kts versionName phải == 1.8.5

Sai → release FAIL.

Acceptance Criteria

Push main không auto-release.

PR chạy CI.

Main chạy CI.

Tag v* mới tạo release.

Có lint.

Release fail nếu tag/version mismatch.

6. P0-S04 — OTA Integrity Verification

Files

app/src/main/java/com/finlux/app/core/updater/AppUpdateManager.kt
app/src/main/java/com/finlux/app/presentation/updater/AppUpdateViewModel.kt
.github/workflows/release.yml

Target

check release
↓
compare versionCode
↓
download
↓
verify package
↓
verify SHA-256
↓
verify signing certificate
↓
install

6.1 Dùng versionCode làm authority

Không chỉ compare versionName.

Release metadata nên có:

{
  "versionName": "1.8.5",
  "versionCode": 104,
  "apk": "FinLux-v1.8.5.apk",
  "sha256": "<HASH>"
}

Update khi:

remoteVersionCode > BuildConfig.VERSION_CODE

6.2 Verify package

Downloaded APK phải có package:

com.finlux.app

Khác → delete file + reject install.

6.3 Verify SHA-256

Release tạo:

FinLux-v1.8.5.apk
FinLux-v1.8.5.sha256

hoặc hash nằm trong update.json.

Nếu:

actualSHA256 != expectedSHA256

→ delete APK, báo lỗi, không mở installer.

6.4 Verify signing certificate

So sánh signing certificate digest giữa:

installed FinLux

và:

downloaded APK

Khác → reject.

Thông báo gợi ý:

"Bản cập nhật không có chữ ký FinLux hợp lệ."

Acceptance Criteria

Compare versionCode.

Verify package.

Verify SHA-256.

Verify certificate.

Corrupted APK bị reject.

Wrong APK bị reject.

Network failure không crash.

GitHub 403/404/rate limit không crash.

Partial download không được install.

7. P0-T01 — Fix flaky tests

Không dùng:

Timestamp.now()
Instant.now()

trong test có expectation theo tháng cố định.

Thay bằng fixed time:

val fixedInstant =
    Instant.parse("2026-08-15T03:00:00Z")

Acceptance Criteria

Test chạy tháng 8 pass.

Test chạy tháng 9 pass.

Test không phụ thuộc timezone máy CI ngoài test timezone chuyên biệt.

8. P0-T02 — Complete Transaction Test Matrix

ADD

income wallet +.

expense wallet -.

expense budget +.

max valid amount.

zero blocked.

negative blocked.

overflow blocked.

missing wallet.

unauthenticated.

EDIT

same wallet.

different wallet.

same category.

different category.

different month.

EXPENSE → INCOME.

INCOME → EXPENSE.

stale amount.

stale wallet.

stale category.

stale date/month.

missing stored transaction.

DELETE

expense.

income.

stale caller amount.

stale wallet.

stale category.

stale month.

missing transaction.

missing wallet.

TRANSFER

normal.

same source/destination blocked.

insufficient non-card blocked.

CARD behavior đúng business rule.

zero blocked.

negative blocked.

> max blocked.

missing source.

missing destination.

overflow blocked.

OUT/IN amount equal.

relatedWalletId symmetric.

9. P1-TZ01 — Finance Timezone Strategy

Hiện trạng

FinLux đã có FinanceTime, nhưng default hiện vẫn dựa vào device timezone.

Đề xuất

Lưu timezone tài chính theo account:

{
  "financeTimeZone": "Asia/Ho_Chi_Minh"
}

Lúc tạo account:

financeTimeZone = device timezone hiện tại

Sau đó giữ cố định trừ khi user chủ động đổi.

Mọi logic phải dùng cùng finance zone

Budget
Reports
Dashboard
Transaction month query
Monthly summary
Reminder

Target architecture

interface FinanceClock {
    val zoneId: ZoneId
    fun now(): Instant
}

Acceptance Criteria

Đổi timezone thiết bị không tự làm đổi financial month cũ.

Budget/report/dashboard dùng cùng zone.

Boundary 23:30/00:30 được test.

Timestamp storage vẫn dùng Instant/Timestamp.

Không migration destructive.

10. Không làm trong v1.8.5

Không làm các mục sau trong hotfix này:

Move UI preferences out of domain
Split FinluxNavHost
Design system cleanup
AI Assistant
Forecasting
New screens
New UI feature
Room/offline-first
Ledger migration

Để dành cho:

FinLux v1.9.0 — Architecture Hardening

11. Quality Gate

Trước release phải chạy:

./gradlew testDebugUnitTest --no-daemon
./gradlew lintDebug --no-daemon
./gradlew assembleDebug --no-daemon
./gradlew assembleRelease --no-daemon

Nếu có:

./gradlew lintRelease --no-daemon

Sau build verify:

package
versionName
versionCode
signing certificate
SHA-256

12. Release Gate

Không phát hành v1.8.5 nếu chưa PASS:

[ ] Firestore wildcard bypass removed
[ ] Firestore invalid writes rejected
[ ] Release keystore private
[ ] Production APK is release build
[ ] Main push does not auto-release
[ ] Tag version matches Gradle version
[ ] Unit tests green
[ ] Lint green/reviewed
[ ] Debug build green
[ ] Release build green
[ ] APK package verified
[ ] SHA-256 generated
[ ] OTA verifies SHA-256
[ ] OTA verifies certificate
[ ] OTA compares versionCode
[ ] Flaky timestamp tests fixed
[ ] Transaction matrix completed
[ ] Finance timezone strategy documented

13. Version

Chỉ sau khi quality gate PASS:

versionCode = 104
versionName = "1.8.5"

14. Commit Plan

security(firestore): remove wildcard write bypass and validate finance documents

security(signing): configure private release signing for CI

ci: separate validation workflow from tagged release workflow

security(updater): verify version code package checksum and signing certificate

test(finance): remove time dependent transaction tests

test(finance): complete transaction integrity matrix

refactor(time): introduce account finance timezone source

bump(release): v1.8.5 security and release hardening

15. Master Prompt cho AI Coding Agent

Bạn là Senior Android/Kotlin Security & Release Engineer phụ trách hardening FinLux.

Repository:
khoaiprovip123/FinLux

Baseline:
- branch main
- versionName 1.8.4
- versionCode 103
- applicationId com.finlux.app
- Kotlin + Jetpack Compose + Hilt + Firebase
- Money tiếp tục dùng Long

Đọc toàn bộ:
docs/FINLUX_V1.8.5_SECURITY_RELEASE_HARDENING.md

QUY TẮC:
1. Không thêm feature mới.
2. Không redesign UI.
3. Không rewrite project.
4. Không đổi Firebase project.
5. Không migration destructive.
6. Không commit production keystore.
7. Không dùng debug APK làm production OTA release.
8. Không bump version trước final quality gate.
9. Mỗi task phải có test/build validation.
10. Một task logic = một commit.
11. Nếu task fail, dừng và báo root cause.
12. Không tự động nhảy sang task tiếp theo khi task hiện tại chưa PASS.

Bắt đầu CHỈ với:
P0-S01 — Fix Firestore Rules wildcard bypass.

Sau khi hoàn thành:
- liệt kê files changed;
- giải thích rule trước/sau;
- chạy Firebase Rules test/emulator nếu khả thi;
- báo PASS/PARTIAL/FAIL;
- không làm P0-S02 nếu P0-S01 chưa PASS.

16. Prompt P0-S01

Thực hiện P0-S01.

Mục tiêu:
Loại bỏ khả năng wildcard rule bypass validation của transactions/wallets/budgets.

Yêu cầu:
- default deny;
- explicit allow từng collection;
- không còn catch-all write;
- transaction amount phải > 0;
- wallet/budget schema validate tối thiểu;
- user chỉ truy cập dữ liệu của chính họ;
- backward compatible với payload hiện tại.

Đọc mapper/repository hiện tại trước khi viết rule.

Nếu Firebase Emulator khả dụng:
viết và chạy Rules test.

Không sửa task khác.

17. Prompt P0-S02 + P0-S03

Sau khi P0-S01 PASS, thực hiện release security.

Mục tiêu:
1. Dừng phát hành app-debug.apk làm production OTA.
2. Tạo release signing bằng private keystore qua GitHub Secrets.
3. Tách CI và Release.

CI:
- PR/main
- testDebugUnitTest
- lintDebug
- assembleDebug
- KHÔNG release

Release:
- tag v*
- verify tag == versionName
- test
- lint
- assembleRelease
- sign private release key
- generate SHA-256
- create GitHub Release

Không commit keystore/password.
Không tự bump version.

18. Prompt P0-S04

Thực hiện hardening AppUpdateManager.

Yêu cầu:
- dùng versionCode làm authority chính;
- verify package == com.finlux.app;
- verify SHA-256;
- verify signing certificate của APK tải về;
- reject/delete file nếu verification fail;
- handle network error, GitHub 403/404, redirect error, partial file;
- không mở installer nếu verification chưa PASS;
- thêm test phù hợp.

Không thay UI ngoài message lỗi cần thiết.

19. AI Report Format

## FINLUX HARDENING TASK RESULT

Task:
P0-...

Status:
PASS / PARTIAL / FAIL

Files changed:
- ...

Security impact:
- ...

Tests added:
- ...

Commands executed:
- ...

Results:
- unit test:
- lint:
- debug build:
- release build:

Remaining risks:
- ...

Next task:
- ...

20. Kết luận

Chiến lược v1.8.5:

SECURITY
↓
RELEASE IDENTITY
↓
OTA TRUST
↓
TEST RELIABILITY
↓
FINANCIAL TIME CONSISTENCY

FinLux là ứng dụng tài chính, vì vậy:

Tính đúng dữ liệu, chữ ký phát hành và chuỗi OTA đáng tin cậy phải được xử lý trước mọi tính năng mới.