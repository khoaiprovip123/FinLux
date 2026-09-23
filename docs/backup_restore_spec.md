# BACKUP & RESTORE ENGINE SPECIFICATION
## FinLux — Full Data Snapshot Module

> **Trạng thái:** ✅ [APPROVED & FULLY IMPLEMENTED] — Đã nghiệm thu thực tế trên thiết bị & 100% PASS Unit Test
> **Phiên bản đặc tả:** 1.25.7 (versionCode 181)
> **Ngày phê duyệt:** 2026-09-21
> **Phạm vi:** Triển khai Full Data Snapshot Engine, Hợp nhất thông minh (Smart Merge), Ghi đè toàn bộ (Full Overwrite), Tự động điều hòa sổ cái tổng lực (Self-Healing Full Ledger Reconciliation) và DataSyncManager Realtime UI Refresh.
> **Liên kết kiến trúc:** `docs/FINLUX_SYSTEM_ARCHITECTURE_MATRIX.md` (Module 17), `docs/DATA_SPEC.md`, `docs/BA_SPEC.md`

---

## TÓM TẮT THIẾT KẾ (EXECUTIVE SUMMARY)

Hiện tại `PrismSettingsScreen.kt` (dòng 411–420) chỉ hiển thị `InfoDialog` thụ động về Firestore auto-sync — không có tính năng backup thực thụ. Module xuất báo cáo (`core/export/ReportExporter.kt`) chỉ phục vụ xuất báo cáo phân tích (PDF/XLSX), **không phải backup toàn bộ dữ liệu thô có khả năng khôi phục**.

Module này thiết kế **Full Data Snapshot Engine** — khả năng xuất **100% dữ liệu người dùng** ra file JSON chuẩn hóa và nhập lại hoàn toàn, độc lập với Firestore connectivity.

---

## 1. KIẾN TRÚC MÔ HÌNH DỮ LIỆU SNAPSHOT

### 1.1. Data Class `FinluxBackupSnapshot`

```kotlin
// domain/model/backup/FinluxBackupSnapshot.kt

data class FinluxBackupSnapshot(

    // ── METADATA BLOCK ──────────────────────────────────────────
    val schemaVersion: Int,          // Version của schema JSON (hiện tại: 1)
    val appVersion: String,          // versionName: "1.25.6"
    val appVersionCode: Int,         // versionCode: 180
    val exportedAt: Long,            // Unix epoch millis (UTC) lúc tạo snapshot
    val exportedByUid: String,       // UID Firebase người tạo backup
    val checksum: String,            // SHA-256 của toàn bộ payload JSON (không kể trường checksum)
    val payloadSizeBytes: Long,      // Kích thước uncompressed payload (bytes)

    // ── PAYLOAD BLOCK (9 entity cốt lõi) ────────────────────────
    val wallets: List<WalletSnapshot>,
    val categories: List<CategorySnapshot>,
    val transactions: List<TransactionSnapshot>,
    val budgets: List<BudgetSnapshot>,
    val debts: List<DebtSnapshot>,
    val debtPayments: List<DebtPaymentSnapshot>,
    val goals: List<GoalSnapshot>,
    val reminders: List<ReminderSnapshot>,
    val salaryCycleConfig: SalaryCycleConfigSnapshot?,
    val deals: List<DealSnapshot>,
    val savingSpinConfig: SavingSpinConfigSnapshot?,
)
```

### 1.2. Entity Snapshots — Ánh xạ 1-1 với Firestore Schema

> **Nguyên tắc:** Tất cả `Instant`/`Timestamp` serialize thành **ISO 8601 UTC string** (`"2026-09-21T07:00:00Z"`) để tránh lệch múi giờ.

```kotlin
data class WalletSnapshot(
    val id: String,
    val name: String,
    val type: String,      // "cash"|"bank"|"ewallet"|"card"|"investment"|"other"
    val balance: Long,     // VNĐ (không thập phân)
    val color: String,
    val isDefault: Boolean,
    val createdAt: String, // ISO 8601 UTC
)

data class CategorySnapshot(
    val id: String,
    val name: String,
    val type: String,      // "income"|"expense"
    val icon: String,
    val color: String,
    val isDefault: Boolean,
    val createdAt: String,
)

data class TransactionSnapshot(
    val id: String,
    val type: String,           // "income"|"expense"|"transfer_out"|"transfer_in"
    val amount: Long,
    val categoryId: String?,
    val walletId: String,
    val relatedWalletId: String?,
    val dealId: String?,
    val dealFlowType: String?,
    val note: String,
    val receiptImageUrl: String?, // NOTE: URL Firebase Storage — cần policy riêng khi restore
    val date: String,             // ISO 8601 UTC
    val createdAt: String,
    val updatedAt: String,
)

data class BudgetSnapshot(
    val id: String,              // format: {categoryId}_{periodKey}
    val categoryId: String,
    val periodKey: String,       // "month:2026-09" | "salary:2026-09-01"
    val limitAmount: Long,
    val spentAmount: Long,       // Denormalized — recalculate khi restore
    val notified80: Boolean,
    val notified100: Boolean,
)

data class DebtSnapshot(
    val id: String,
    val name: String,
    val type: String,            // "CREDIT_CARD"|"BANK_LOAN"|"INSTALLMENT"|"PERSONAL_LOAN"
    val initialAmount: Long,
    val remainingBalance: Long,
    val interestRateYearly: Double,
    val minPaymentMonthly: Long,
    val dueDate: Int?,
    val statementDate: Int?,
    val colorHex: String,
    val isSettled: Boolean,
    val isReminderEnabled: Boolean,
    val reminderDaysBefore: Int,
    val createdAt: String,
    val updatedAt: String,
)

data class DebtPaymentSnapshot(
    val id: String,
    val debtId: String,          // FK — rebuild subcollection khi restore
    val walletId: String,
    val amount: Long,
    val principalPaid: Long,
    val interestPaid: Long,
    val paymentDate: String,
    val note: String,
)

data class GoalSnapshot(
    val id: String,
    val name: String,
    val targetAmount: Long,
    val savedAmount: Long,
    val deadline: String,
    val category: String,
    val monthlyContribution: Long,
    val imageUri: String?,       // Local URI — set null khi restore trên máy khác
    val createdAt: String,
)

data class ReminderSnapshot(
    val id: String,
    val title: String,
    val amount: Long,
    val categoryId: String,
    val walletId: String,
    val recurrence: String,      // "daily"|"weekly"|"monthly"
    val startDate: String,
    val enabled: Boolean,
    val nextTriggerDate: String,
)

data class SalaryCycleConfigSnapshot(
    val enabled: Boolean,
    val scheduleType: String,    // "MONTHLY_ONCE"|"SEMI_MONTHLY"
    val paydayRuleType: String,
    val paydayDay: Int,
    val salaryWalletId: String?,
    val expectedSalary: Long?,
    val secondPaydayDay: Int?,
    val secondSalaryWalletId: String?,
    val secondExpectedSalary: Long?,
    val savingsWalletId: String?,
    val rolloverRule: String,
    val budgetPeriodBasis: String,
    val financeTimeZone: String, // "Asia/Ho_Chi_Minh"
    val updatedAt: String,
)

data class DealSnapshot(
    val id: String,
    val title: String,
    val description: String,
    val category: String,        // "investment"|"lending"
    val targetAmount: Long,
    val totalCapitalOutlay: Long,
    val totalRecovered: Long,
    val netProfitLoss: Long,
    val status: String,          // "ACTIVE"|"COMPLETED"|"CANCELLED"
    val startDate: String,
    val endDate: String?,
    val createdAt: String,
    val updatedAt: String,
)

data class SavingSpinConfigSnapshot(
    val enabled: Boolean,
    val showOnHome: Boolean,
    val minAmount: Long,
    val maxAmount: Long,
    val stepAmount: Long,
    val slotCount: Int,
    val frequency: String,
    val selectedWeekdays: List<Int>,
    val weeklyDay: Int,
    val reminderHour: Int,
    val reminderMinute: Int,
    val reminderEnabled: Boolean,
    val snoozeEnabled: Boolean,
    val allowSkip: Boolean,
    val defaultDestinationId: String?,
    val schemaVersion: Int,
    val updatedAt: String,
)
```

### 1.3. Cơ Chế Ánh Xạ Lại UID Khi Import Cross-Account

```
Vấn đề: Backup từ UID_A import vào UID_B → tất cả walletId trong transactions
         vẫn tham chiếu ID cũ tồn tại trong tài khoản UID_A.

Giải pháp: ID Remapping Table (HashMap trong memory khi restore):
  walletId_old    → walletId_new
  categoryId_old  → categoryId_new
  debtId_old      → debtId_new
  goalId_old      → goalId_new
  dealId_old      → dealId_new
  reminderId_old  → reminderId_new
```

**Thứ tự khôi phục bắt buộc (Dependency Order):**
1. `categories` — không phụ thuộc
2. `wallets` — không phụ thuộc
3. `salaryCycleConfig`
4. `reminders` — phụ thuộc categories, wallets
5. `goals` — độc lập
6. `debts` → `debtPayments` — phụ thuộc wallets
7. `deals` — độc lập
8. `budgets` — phụ thuộc categories
9. `transactions` — phụ thuộc wallets, categories, deals (cuối cùng)
10. `savingSpinConfig`

---

## 2. TẦNG DOMAIN & XỬ LÝ USECASES

### 2.1. `ExportBackupUseCase`

```
Package: domain/usecase/backup/ExportBackupUseCase.kt
Input:   currentUserId: String
Output:  AppResult<Uri>

Pipeline:
  1. Thu thập song song (coroutines) từ tất cả repositories:
       WalletRepository.observeWallets().first()
       CategoryRepository.observeCategories().first()
       TransactionRepository.observePeriod(Instant.EPOCH, Instant.now()).first()
         → Cảnh báo nếu > 50.000 giao dịch
       BudgetRepository → query tất cả period keys
       DebtRepository.observeDebts().first() + loadAllPayments()
       GoalRepository.observeGoals().first()
       ReminderRepository.observeReminders().first()
       SalaryCycleRepository.observe().first()
       DealRepository.observeDeals().first()
       SavingSpinRepository.observeConfig().first()

  2. Map sang Snapshot data classes (toSnapshot() extension)

  3. Tính checksum SHA-256:
       val payloadJson = gson.toJson(snapshot.copy(checksum = ""))
       val checksum = MessageDigest.getInstance("SHA-256")
                         .digest(payloadJson.toByteArray())
                         .joinToString("") { "%02x".format(it) }
       val finalSnapshot = snapshot.copy(checksum = checksum)

  4. Serialize to JSON (kotlinx.serialization hoặc Gson)
     Tùy chọn: nén GZIP nếu payloadSizeBytes > 2MB

  5. Ghi ra File(context.cacheDir, "backup/FinLux_Backup_YYYYMMDD_HHmm.finlux")
     Extension .finlux để nhận diện trên file manager

  6. Trả về FileProvider URI → ShareSheet hoặc SAF CreateDocument
```

**Tên file backup chuẩn:**
```
FinLux_Backup_YYYYMMDD_HHmm.finlux
MIME type: application/json
```

---

### 2.2. `ValidateBackupUseCase`

```
Package: domain/usecase/backup/ValidateBackupUseCase.kt
Input:   fileUri: Uri
Output:  AppResult<BackupPreviewSummary>

Pipeline:
  1. Đọc nội dung file (InputStreamReader)
  2. Kiểm tra cú pháp JSON → BadJson Error
  3. Kiểm tra trường bắt buộc: schemaVersion, appVersion, exportedAt, checksum, wallets, transactions
  4. Kiểm tra schemaVersion:
       > CURRENT_SCHEMA_VERSION → "Cần nâng cấp app"
       < MIN_SUPPORTED_SCHEMA_VERSION → "Backup quá cũ, không hỗ trợ"
  5. Verify SHA-256:
       val computed = sha256(gson.toJson(parsed.copy(checksum = "")))
       if (computed != parsed.checksum) → ChecksumMismatch Error
  6. Nhận diện cross-account: isCrossAccount = parsed.exportedByUid != currentUserId
  7. Xây dựng BackupPreviewSummary trả về UI
```

```kotlin
data class BackupPreviewSummary(
    val exportedAt: Instant,
    val appVersion: String,
    val schemaVersion: Int,
    val isCrossAccount: Boolean,
    val exportedByUid: String,
    val walletCount: Int,
    val transactionCount: Int,
    val categoryCount: Int,
    val budgetCount: Int,
    val debtCount: Int,
    val goalCount: Int,
    val reminderCount: Int,
    val dealCount: Int,
    val dateRangeStart: Instant?,
    val dateRangeEnd: Instant?,
    val estimatedSizeLabel: String, // "~2.4 MB"
    val isChecksumValid: Boolean,
    val hasSalaryCycleConfig: Boolean,
    val hasSavingSpinConfig: Boolean,
)
```

---

### 2.3. `RestoreBackupUseCase`

```
Package: domain/usecase/backup/RestoreBackupUseCase.kt
Input:   snapshot: FinluxBackupSnapshot
         strategy: RestoreStrategy
         currentUserId: String
Output:  AppResult<RestoreReport>

enum class RestoreStrategy {
    FULL_OVERWRITE,  // Xóa sạch → nạp lại toàn bộ
    SMART_MERGE,     // Hợp nhất: giữ bản ghi mới hơn theo updatedAt
}
```

#### Chế độ 1: `FULL_OVERWRITE`
```
Bước 0: Hiển thị ConfirmationDialog cấp ĐỎ (2 bước xác nhận)
         "⚠️ Toàn bộ dữ liệu hiện tại sẽ bị XÓA VĨNH VIỄN. Không thể hoàn tác."

Bước 1: Xóa theo thứ tự ngược (tránh orphan):
         transactions → budgets → debtPayments → debts → goals → reminders
         → deals → savingSpinConfig → salaryCycleConfig → categories → wallets
         Dùng Firestore Batched Writes (chunk 500 docs/batch)

Bước 2: Khôi phục theo Dependency Order (mục 1.3)
         - Gắn userId = currentUserId (override exportedByUid)
         - Apply idRemapTable nếu cross-account
         - wallet.balance lấy từ snapshot (không recalculate từ tx)

Bước 3: WalletBalanceAudit — phát hiện lệch tiền sau restore

Bước 4: Reschedule tất cả Reminders qua AlarmManager
```

#### Chế độ 2: `SMART_MERGE`
```
Với mỗi entity trong snapshot:
  a. Tải bản ghi hiện có từ Firestore
  b. So sánh theo ID:
     - ID chưa tồn tại → Insert mới
     - ID đã tồn tại:
       * snapshot.updatedAt > existing.updatedAt → Update
       * snapshot.updatedAt <= existing.updatedAt → Bỏ qua (giữ bản mới hơn)
  c. SystemCategories (kiểm tra qua SystemCategories.kt) → TUYỆT ĐỐI không ghi đè
  d. Category trùng ID custom → Đổi tên thành "{name} (imported)"
  e. Sau merge: Tính lại wallet.balance từ toàn bộ transactions (an toàn hơn)
```

```kotlin
data class RestoreReport(
    val strategy: RestoreStrategy,
    val walletsRestored: Int,
    val transactionsRestored: Int,
    val categoriesRestored: Int,
    val budgetsRestored: Int,
    val debtsRestored: Int,
    val goalsRestored: Int,
    val remindersRestored: Int,
    val dealsRestored: Int,
    val skippedCount: Int,
    val conflictsResolved: Int,
    val balanceAuditPassed: Boolean,
    val balanceDiscrepancies: List<String>,
    val durationMs: Long,
)
```

---

## 3. TẦNG TRÌNH DIỄN & GIAO DIỆN LIQUID GLASS

### 3.1. Điểm Kích Hoạt (Entry Point)

**Thay đổi duy nhất tại `PrismSettingsScreen.kt`:**
- Thay `infoDialog = InfoDialogContent(...)` → `showBackupSheet = true`
- Gọi `BackupRestoreSheet(visible = showBackupSheet, onDismiss = { ... })`

### 3.2. Layout `BackupRestoreSheet`

```
Package: presentation/settings/backup/BackupRestoreSheet.kt
Type:    ModalBottomSheet (Liquid Glass)

┌─────────────────────────────────────────────────────┐
│  ≡ Handle Bar                                       │
│  🔒 Sao lưu & Khôi phục dữ liệu      [×] Đóng     │
├─────────────────────────────────────────────────────┤
│  ☁️  KHU VỰC 1: ĐỒNG BỘ ĐÁM MÂY (FIRESTORE)      │
│  ┌─────────────────────────────────────────────────┐│
│  │ LiquidGlassSurface(REGULAR)                     ││
│  │ 🟢 Đang đồng bộ      [email@gmail.com]         ││
│  │ Lần cuối: vừa xong / 5 phút trước              ││
│  └─────────────────────────────────────────────────┘│
│  💾 KHU VỰC 2: SAO LƯU THỦ CÔNG (LOCAL SNAPSHOT)  │
│  ┌─────────────────────────────────────────────────┐│
│  │ LiquidGlassSurface(REGULAR)                     ││
│  │ Bản sao lưu bao gồm:                            ││
│  │   • 5 ví · 1.247 giao dịch · 12 danh mục       ││
│  │   • 3 mục tiêu · 4 khoản nợ · 6 nhắc nhở       ││
│  │   • Ước tính: ~1.8 MB                           ││
│  │                                                  ││
│  │  [📤 Tạo bản sao lưu & Chia sẻ]  ← primary btn ││
│  └─────────────────────────────────────────────────┘│
│  🔄 KHU VỰC 3: KHÔI PHỤC DỮ LIỆU                  │
│  ┌─────────────────────────────────────────────────┐│
│  │ LiquidGlassSurface(REGULAR)                     ││
│  │  [📂 Chọn file sao lưu (.finlux)]               ││
│  │                                                  ││
│  │  Preview Card (hiện khi file hợp lệ):           ││
│  │  ┌──────────────────────────────────────────┐   ││
│  │  │ Xuất ngày: 20/09/2026 lúc 22:15         │   ││
│  │  │ Phiên bản: v1.25.5 (versionCode 178)    │   ││
│  │  │ 1.247 giao dịch · 5 ví · 12 danh mục   │   ││
│  │  │ ✅ Chữ ký hợp lệ (SHA-256 OK)           │   ││
│  │  │ ⚠️ Dữ liệu từ tài khoản khác            │   ││
│  │  └──────────────────────────────────────────┘   ││
│  │  Phương thức:                                    ││
│  │  ○ Ghi đè toàn bộ (Wipe & Replace)             ││
│  │  ● Hợp nhất thông minh (Khuyến nghị)           ││
│  │                                                  ││
│  │  [🔄 Bắt đầu khôi phục]  ← warning button      ││
│  └─────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────┘
```

### 3.3. Quy Chuẩn Liquid Glass (Design Constraints)

| Constraint | Rule |
|:---|:---|
| **Màu sắc** | 100% `LocalFinluxTokens.current` — không hardcode hex |
| **Surface** | `FinluxStyleBackdrop` + `containerColor = Color.Transparent` |
| **Card** | `LiquidGlassSurface(mode = LiquidGlassMode.REGULAR)` |
| **Bo góc** | `tokens.radius.lg` (18dp) card, `tokens.radius.md` (12dp) button |
| **Button Primary** | `tokens.primary`, spring animation on click |
| **Button Outline** | Viền `tokens.outline`, background trong suốt |
| **Button Destructive** | `tokens.error`, xác nhận 2 bước |
| **Progress** | `LinearProgressIndicator` dùng `tokens.primary` |
| **Cloud Sync Dot** | `tokens.success` (online) / `tokens.outline` (offline) |

### 3.4. `BackupRestoreViewModel` States

```kotlin
sealed class BackupUiState {
    object Idle : BackupUiState()
    object LoadingStats : BackupUiState()
    data class StatsReady(val stats: DataStats) : BackupUiState()
    object Exporting : BackupUiState()
    data class ExportDone(val uri: Uri) : BackupUiState()
    data class ExportError(val message: String) : BackupUiState()
    object ValidatingFile : BackupUiState()
    data class FileValidated(val preview: BackupPreviewSummary) : BackupUiState()
    data class FileInvalid(val reason: String) : BackupUiState()
    object Restoring : BackupUiState()
    data class RestoreDone(val report: RestoreReport) : BackupUiState()
    data class RestoreError(val message: String) : BackupUiState()
}
```

---

## 4. ĐỘ AN TOÀN & XỬ LÝ CA BIÊN

### 4.1. Bảng Ma Trận Ca Biên (Edge Case Matrix)

| # | Tình huống | Phản ứng của hệ thống |
|:---:|:---|:---|
| **EC-01** | File JSON lỗi cú pháp | Error: "Định dạng file không hợp lệ" |
| **EC-02** | File thiếu trường `wallets`/`transactions` | Error: "File backup bị thiếu dữ liệu bắt buộc" |
| **EC-03** | SHA-256 checksum không khớp | Error: "⚠️ File có thể đã bị chỉnh sửa — từ chối khôi phục" |
| **EC-04** | schemaVersion > phiên bản app | Warning: "Cần nâng cấp FinLux để đọc file này" |
| **EC-05** | schemaVersion < MIN_SUPPORTED | Error: "File backup quá cũ, không hỗ trợ migrate" |
| **EC-06** | Cross-account import | Warning vàng: "⚠️ Dữ liệu từ tài khoản khác — ID sẽ được ánh xạ lại" |
| **EC-07** | File > 50MB | Error: "File quá lớn (>50MB). Có thể bị hỏng." |
| **EC-08** | Transactions > 50.000 records | Warning trong Export: "Export có thể mất vài phút" |
| **EC-09** | Mất mạng giữa Restore | Retry checkpoint: lưu phase hiện tại, retry từ entity cuối thất bại |
| **EC-10** | Conflict SystemCategory ID | Bỏ qua — không ghi đè SystemCategories (tuân thủ BR-CAT) |
| **EC-11** | walletId trong tx không tồn tại | Gắn vào wallet đầu tiên + ghi vào RestoreReport.discrepancies |
| **EC-12** | Ổ lưu trữ đầy | Bắt IOException → Error: "Không đủ dung lượng" |
| **EC-13** | User bấm Back trong khi Restore | Intercept back → Dialog xác nhận "Dừng khôi phục?" |
| **EC-14** | Balance lệch sau restore | WalletBalanceAudit phát hiện → Báo cáo + tùy chọn "Tính lại từ lịch sử" |

### 4.2. Schema Migration Strategy

```kotlin
object BackupSchemaMigrator {
    const val CURRENT_SCHEMA_VERSION = 1
    const val MIN_SUPPORTED_SCHEMA_VERSION = 1

    fun migrate(snapshot: FinluxBackupSnapshot): FinluxBackupSnapshot {
        var current = snapshot
        // Mở rộng khi nâng lên schema v2:
        // if (current.schemaVersion == 1) current = migrateV1toV2(current)
        return current
    }
}
```

### 4.3. Firestore Batched Write Chunking

```
Giới hạn Firestore: 500 writes/batch

fun <T> List<T>.chunked500() = this.chunked(500)

for (chunk in transactions.chunked500()) {
    val batch = firestore.batch()
    chunk.forEach { tx -> batch.set(txRef(tx.id), tx.toFirestoreMap()) }
    batch.commit().await()  // Retry tối đa 3 lần, backoff 1s / 2s / 4s
}

Nếu chunk thứ N thất bại sau 3 retry:
  → Ghi vào RestoreReport.errors
  → Tiếp tục chunk kế tiếp (partial restore, không crash toàn bộ)
```

---

## 5. MA TRẬN KIỂM THỬ TỰ ĐỘNG

### 5.1. `ExportBackupUseCaseTest` (7 test cases)

| # | Tên test | Input | Expected |
|:---:|:---|:---|:---|
| T-EXP-01 | Export thành công cơ bản | 3 ví, 10 tx, 5 danh mục | `AppResult.Success(Uri)`, file `.finlux` tồn tại |
| T-EXP-02 | Checksum SHA-256 hợp lệ | Snapshot chuẩn | Checksum trong file == SHA-256 tính lại từ payload |
| T-EXP-03 | Metadata exportedAt đúng | Export lúc t=X | `exportedAt` chênh lệch < 1s so với t=X |
| T-EXP-04 | Export dataset rỗng | User mới, 0 ví, 0 tx | Export thành công, các list là `[]` |
| T-EXP-05 | ExportedByUid = currentUserId | uid="abc123" | `exportedByUid == "abc123"` |
| T-EXP-06 | Schema version đúng | — | `schemaVersion == CURRENT_SCHEMA_VERSION` |
| T-EXP-07 | DebtPayments bao gồm đủ | 2 debts × 3 payments | `debtPayments.size == 6` |

### 5.2. `ValidateBackupUseCaseTest` (9 test cases)

| # | Tên test | Input | Expected |
|:---:|:---|:---|:---|
| T-VAL-01 | File hợp lệ hoàn toàn | Snapshot chuẩn | `Success(BackupPreviewSummary)`, `isChecksumValid=true` |
| T-VAL-02 | JSON hỏng cú pháp | `{invalid json` | Error: "Định dạng file không hợp lệ" |
| T-VAL-03 | Checksum bị chỉnh sửa | Sửa 1 ký tự | `isChecksumValid=false` → Error |
| T-VAL-04 | Thiếu trường `wallets` | JSON không có key `wallets` | Error: thiếu trường bắt buộc |
| T-VAL-05 | schemaVersion quá mới | schemaVersion=999 | Error: yêu cầu nâng cấp app |
| T-VAL-06 | schemaVersion quá cũ | schemaVersion=0 | Error: không hỗ trợ schema |
| T-VAL-07 | Cross-account | exportedByUid≠currentUid | `isCrossAccount=true`, vẫn là Success |
| T-VAL-08 | File rỗng 0 bytes | Empty file | Error: không đọc được |
| T-VAL-09 | Preview count đúng | 5 ví, 100 tx, 8 cat | Summary phản ánh đúng số lượng |

### 5.3. `RestoreBackupUseCaseTest` (9 test cases)

| # | Tên test | Strategy | Input | Expected |
|:---:|:---|:---:|:---|:---|
| T-RST-01 | Full overwrite thành công | FULL_OVERWRITE | Snapshot hợp lệ | RestoreReport đầy đủ entity counts |
| T-RST-02 | Xóa dữ liệu cũ khi overwrite | FULL_OVERWRITE | 5 ví cũ, backup 3 ví | Sau restore chỉ còn 3 ví |
| T-RST-03 | Smart merge: bản mới hơn win | SMART_MERGE | Ví A trùng ID, snapshot.updatedAt mới hơn | Ví A được update |
| T-RST-04 | Smart merge: bỏ qua bản cũ | SMART_MERGE | Ví B trùng ID, snapshot.updatedAt cũ hơn | Giữ nguyên bản hiện tại |
| T-RST-05 | SystemCategory không bị overwrite | FULL_OVERWRITE | Cat ID="food" với name khác trong backup | Category "food" giữ nguyên từ SystemCategories |
| T-RST-06 | Cross-account walletId remap | FULL_OVERWRITE | exportedByUid≠currentUid | tx.walletId ánh xạ sang walletId mới |
| T-RST-07 | WalletBalanceAudit PASS | FULL_OVERWRITE | Snapshot nhất quán | `balanceAuditPassed=true` |
| T-RST-08 | WalletBalanceAudit phát hiện lệch | FULL_OVERWRITE | wallet.balance≠tổng tx | `balanceAuditPassed=false`, `discrepancies` không rỗng |
| T-RST-09 | Batched write 500 docs | FULL_OVERWRITE | 1.200 transactions | 3 batch commits (500+500+200) |

**Tổng: 25 test cases bao phủ toàn bộ pipeline.**

---

## 6. LỘ TRÌNH TRIỂN KHAI THEO PHASE

### Phase 1 — Foundation Layer (Ngày 1–2)
**Deliverables:**
- `domain/model/backup/FinluxBackupSnapshot.kt` + tất cả entity snapshot data classes
- `domain/usecase/backup/ExportBackupUseCase.kt`
- `domain/usecase/backup/ValidateBackupUseCase.kt`
- Constants `CURRENT_SCHEMA_VERSION`, `MIN_SUPPORTED_SCHEMA_VERSION`
- Unit tests: T-EXP-01..07 + T-VAL-01..09

**DoD Phase 1:**
- [ ] Tất cả data classes compile không lỗi
- [ ] ExportBackupUseCase thu thập đủ 9 entity từ repository mocks
- [ ] Checksum SHA-256 verified bởi ValidateBackupUseCase
- [ ] 16/16 unit tests PASS 100%
- [ ] `gradlew testDebugUnitTest` = 0 errors

---

### Phase 2 — Restore Engine (Ngày 3–4)
**Deliverables:**
- `domain/usecase/backup/RestoreBackupUseCase.kt` (cả 2 strategy)
- `domain/usecase/backup/BackupSchemaMigrator.kt`
- ID Remapping logic (cross-account)
- `WalletBalanceAudit` sau restore
- Firestore Batched Write chunking 500 docs + retry
- Unit tests: T-RST-01..09

**DoD Phase 2:**
- [ ] Full Overwrite xóa sạch và restore đúng thứ tự dependency
- [ ] Smart Merge ưu tiên bản ghi updatedAt mới hơn
- [ ] SystemCategories không bị ghi đè trong bất kỳ strategy nào
- [ ] Cross-account ID remapping không để lại orphan references
- [ ] 9/9 unit tests PASS 100%

---

### Phase 3 — UI/UX Liquid Glass (Ngày 5–6)
**Deliverables:**
- `presentation/settings/backup/BackupRestoreViewModel.kt`
- `presentation/settings/backup/BackupRestoreSheet.kt`
- SAF launcher (`ActivityResultContracts.CreateDocument` + `OpenDocument`)
- FileProvider config `.finlux` MIME trong `AndroidManifest.xml`
- Entry point: thay InfoDialog → BackupRestoreSheet tại `PrismSettingsScreen.kt`
- Edge case UI: loading states, error snackbars, confirmation dialogs

**DoD Phase 3:**
- [ ] Khu vực 1: Firestore sync status real-time
- [ ] Khu vực 2: Export → Share sheet hoạt động trên thiết bị thật
- [ ] Khu vực 3: Chọn file → Validate → Preview → Confirm → Restore → Report
- [ ] Confirmation dialog 2 bước cho Full Overwrite
- [ ] 100% màu từ `LocalFinluxTokens.current`, không hardcode hex
- [ ] Light Mode & Dark Mode đều đẹp

---

### Phase 4 — Polish, Integration & Docs (Ngày 7)
**Deliverables:**
- Integration test trên thiết bị thật: Export → Restore → Kiểm tra số dư khớp 100%
- Cập nhật `firestore.rules` nếu cần thêm rule cho batch writes
- Cập nhật `docs/DATA_SPEC.md` → bổ sung backup snapshot format
- Cập nhật `docs/FINLUX_SYSTEM_ARCHITECTURE_MATRIX.md` → thêm Module 17: Backup Engine
- Cập nhật `CHANGELOG.md` + `HANDOVER_LOG.md`

**DoD Phase 4 (Full Release DoD):**
- [ ] Spec Parity: DATA_SPEC.md + ARCHITECTURE_MATRIX.md cập nhật đồng bộ
- [ ] Shared Code: SHA-256 + ID remapping nằm tại `core/common/BackupChecksumHelper.kt`
- [ ] Theme check: Đẹp Dark Mode & Light Mode
- [ ] Large Data check: 5.000+ giao dịch export/restore không OOM
- [ ] `testDebugUnitTest` tổng suite PASS 100% (25 test mới)
- [ ] Logcat: Không exception ngầm trong export/restore

---

## PHỤ LỤC A: Cấu Trúc Thư Mục Mới

```
app/src/main/java/com/finlux/app/
├── core/
│   └── common/
│       └── BackupChecksumHelper.kt         ← SHA-256 utils dùng chung
├── domain/
│   ├── model/
│   │   └── backup/
│   │       ├── FinluxBackupSnapshot.kt     ← Main snapshot + entity data classes
│   │       ├── BackupPreviewSummary.kt
│   │       └── RestoreReport.kt
│   └── usecase/
│       └── backup/
│           ├── ExportBackupUseCase.kt
│           ├── ValidateBackupUseCase.kt
│           ├── RestoreBackupUseCase.kt
│           └── BackupSchemaMigrator.kt
└── presentation/
    └── settings/
        └── backup/
            ├── BackupRestoreViewModel.kt
            └── BackupRestoreSheet.kt

app/src/test/java/com/finlux/app/usecase/backup/
    ├── ExportBackupUseCaseTest.kt
    ├── ValidateBackupUseCaseTest.kt
    └── RestoreBackupUseCaseTest.kt
```

---

## PHỤ LỤC B: Tác Động Chéo (Cross-Module Blast Radius)

| Module | Tác động | Mức độ |
|:---|:---|:---:|
| Auth & Profile (1) | Đọc `currentUserId` | 🟡 Thấp |
| Wallet & Assets (2) | Đọc + Ghi toàn bộ wallets | 🔴 Cao |
| Transaction Ledger (3) | Đọc + Ghi toàn bộ transactions | 🔴 Cao |
| Category System (4) | Đọc + Ghi (bảo vệ SystemCategories) | 🔴 Cao |
| Budgeting Engine (5) | Đọc + Ghi budgets; recalculate spentAmount | 🟠 Trung |
| Salary Cycle 2.0 (6) | Đọc + Ghi salaryCycleConfig | 🟡 Thấp |
| Debt Engine (7) | Đọc + Ghi debts + debtPayments | 🟠 Trung |
| Savings Goals (8) | Đọc + Ghi goals | 🟡 Thấp |
| Saving Spin (9) | Đọc + Ghi savingSpinConfig | 🟡 Thấp |
| Deal & Investments (10) | Đọc + Ghi deals | 🟡 Thấp |
| Reminders (12) | Đọc + Ghi + Reschedule AlarmManager | 🟠 Trung |
| Security (14) | Không tác động | 🟢 Không |
| Design System (16) | Tuân thủ tokens — Không thay đổi | 🟢 Không |

---

*Tài liệu này là đặc tả kỹ thuật cho mục đích lập kế hoạch. Mọi thay đổi mã nguồn (.kt) chỉ được thực hiện sau khi Tech Lead phê duyệt toàn bộ nội dung trên.*
