# KẾ HOẠCH KỸ THUẬT CHI TIẾT (MASTER IMPLEMENTATION PLAN)
## KHẮC PHỤC TRIỆT ĐỂ LỖI NGÂN SÁCH & MỤC TIÊU TRONG SAO LƯU & PHỤC HỒI (BACKUP & RESTORE)

- **Dự án:** FinLux — Quản lý Tài chính Cá nhân (Android / Kotlin Jetpack Compose / Firebase)
- **Tài liệu tham chiếu:**
  + `docs/FINLUX_SYSTEM_ARCHITECTURE_MATRIX.md` (Module 05: Budget & Module 06: Goals)
  + `docs/BA_SPEC.md` (Business Rules BR-09, BR-10, BR-15)
  + `docs/DATA_SPEC.md` (Schema `budgets`, `goals`, Backup Snapshot v1)
  + `AGENTS.md` (Điều II.9 Spec Parity & Quy tắc Planning-First Gate)
- **Trạng thái:** Chờ Tech Lead phê duyệt (Pre-Execution Gate)

---

## 1. TỔNG QUAN BỐI CẢNH & PHÁT HIỆN GỐC RỄ (ROOT-CAUSE AUDIT)

Qua kiểm thử thực tế trên thiết bị vật lý sau khi kích hoạt tính năng Sao lưu & Phục hồi:
1. **Ngân sách (Budgets):** Không thể lưu được vào file backup `.finlux` (mảng `"budgets": []` luôn luôn rỗng). Sau khi restore, toàn bộ ngân sách bị biến mất. Màn hình Backup cũng hiển thị `0 ngân sách` dù người dùng đã tạo đầy đủ.
2. **Mục tiêu (Goals):** Không hiển thị sau khi restore. Trong chế độ Full Overwrite, dữ liệu mục tiêu cũ bị xóa sạch nhưng không nạp lại được. Trong Smart Merge, các mục tiêu bị bỏ qua (skip) do lỗi logic so sánh `createdAt`.

### Bảng Đối Soát Nguyên Nhân Gốc Rễ:

| Hạng mục | Vị trí code | Cơ chế lỗi hiện tại | Hậu quả thực tế |
| :--- | :--- | :--- | :--- |
| **Budget Query Wildcard** | `FirebaseBudgetRepository.kt` (`observeBudgets`) | Gọi `whereIn("periodKey", ["*", "month:*"])` khi `periodKey == "*"` | Không có budget nào khớp $\rightarrow$ Trả về `[]` rỗng. File backup không có ngân sách. |
| **Budget Wipe & Load Stats** | `ExportBackupUseCase`, `RestoreBackupUseCase`, `BackupRestoreViewModel` | Đều gọi `observeBudgets("*")` | Stats báo `0 ngân sách`, Wipe không xóa được bản ghi cũ, Export rỗng. |
| **Goal Defensive Parsing** | `FirebaseGoalRepository.kt` (`toGoal`) | `requireNotNull(getTimestamp("deadline"))` ném `RuntimeException` nếu `deadline` là String ISO hoặc Long | Document goal bị `runCatching` nuốt lỗi $\rightarrow$ Trả về `null`. Mục tiêu biến mất. |
| **Goal Smart Merge Logic** | `RestoreBackupUseCase.kt` (line 442) | So sánh `snapshotCreated.isAfter(existing.createdAt)` | `FinancialGoal` không có `updatedAt`. Điều kiện luôn `false` $\rightarrow$ Luôn bị SKIP. |
| **UI Reactive Desync** | `GoalsViewModel.kt` & `BudgetViewModel.kt` | Không lắng nghe `dataSyncManager.refreshTrigger` | Restore xong nhưng màn hình giữ StateFlow cũ, không reload dữ liệu mới. |

---

## 2. NĂM TRỤ CỘT GIẢI PHÁP TRIỂN KHAI (5 ARCHITECTURAL PILLARS)

### TRỤ CỘT 1: SỬA TẬN GỐC TRUY VẤN NGÂN SÁCH (`FirebaseBudgetRepository.kt`)
- **Nguyên lý:** Xử lý ký tự đại diện `"*"` và chuỗi rỗng `""` như một truy vấn toàn bộ (Global Query) không phân vùng kỳ tài chính.
- **Chi tiết kỹ thuật:**
  ```kotlin
  override fun observeBudgets(periodKey: String): Flow<List<Budget>> = callbackFlow {
      val uid = auth.currentUser?.uid
      if (uid == null) {
          close()
          return@callbackFlow
      }
      val collection = firestore.collection("users").document(uid).collection("budgets")
      
      // Wildcard check: Nếu periodKey là "*" hoặc blank, query toàn bộ collection
      val query: Query = if (periodKey == "*" || periodKey.isBlank()) {
          collection
      } else {
          val keysToMatch = listOfNotNull(
              periodKey,
              if (periodKey.startsWith("month:")) periodKey.removePrefix("month:") else null,
              if (periodKey.startsWith("salary:")) periodKey.removePrefix("salary:") else null,
              if (!periodKey.startsWith("month:") && !periodKey.startsWith("salary:") && periodKey.isNotBlank()) "month:$periodKey" else null,
          ).distinct()
          collection.whereIn("periodKey", keysToMatch)
      }

      val registration = query.addSnapshotListener { snapshot, error ->
          if (error != null) close(error)
          else trySend(snapshot?.documents.orEmpty().mapNotNull { it.toBudget() })
      }
      awaitClose { registration.remove() }
  }
  ```
- **Tác động:**
  + `ExportBackupUseCase`: Thu thập đủ 100% ngân sách thuộc mọi chu kỳ (tháng, lương) nạp vào snapshot.
  + `BackupRestoreViewModel.loadDataStats()`: Hiển thị đúng số lượng `X ngân sách` trên UI sao lưu.
  + `RestoreBackupUseCase` (Full Overwrite): Quét sạch toàn bộ `oldBudgets` trước khi nạp lại.
  + `RestoreBackupUseCase` (Smart Merge): Nhận diện đúng danh sách ngân sách hiện có để hòa trộn.

---

### TRỤ CỘT 2: PHÒNG THỦ DỮ LIỆU & PARSING LINH HOẠT CHO MỤC TIÊU (`FirebaseGoalRepository.kt`)
- **Nguyên lý:** Defensive Reading — Bất biến không để ngoại lệ ép kiểu làm rơi rụng document.
- **Chi tiết kỹ thuật trong `toGoal()`:**
  ```kotlin
  internal fun DocumentSnapshot.toGoal(): FinancialGoal? = runCatching {
      // 1. Parse Deadline an toàn (Timestamp | Date | Long epoch millis | String ISO-8601)
      val deadlineInstant = when (val raw = get("deadline")) {
          is Timestamp -> raw.toDate().toInstant()
          is Date -> raw.toInstant()
          is Long -> Instant.ofEpochMilli(raw)
          is Number -> Instant.ofEpochMilli(raw.toLong())
          is String -> runCatching { Instant.parse(raw) }.getOrNull()
          else -> null
      } ?: Instant.now().plusSeconds(180L * 86400) // Fallback mặc định 6 tháng

      // 2. Parse CreatedAt an toàn
      val createdInstant = when (val raw = get("createdAt")) {
          is Timestamp -> raw.toDate().toInstant()
          is Date -> raw.toInstant()
          is Long -> Instant.ofEpochMilli(raw)
          is Number -> Instant.ofEpochMilli(raw.toLong())
          is String -> runCatching { Instant.parse(raw) }.getOrNull()
          else -> null
      } ?: Instant.now()

      // 3. Parse Amount an toàn (Long | Double | Number)
      val targetVal = when (val raw = get("targetAmount")) {
          is Number -> raw.toLong()
          is String -> raw.toLongOrNull() ?: 0L
          else -> 0L
      }
      val savedVal = when (val raw = get("savedAmount")) {
          is Number -> raw.toLong()
          is String -> raw.toLongOrNull() ?: 0L
          else -> 0L
      }
      val monthlyVal = when (val raw = get("monthlyContribution")) {
          is Number -> raw.toLong()
          is String -> raw.toLongOrNull() ?: 0L
          else -> 0L
      }

      FinancialGoal(
          id = id,
          name = getString("name").orEmpty().ifBlank { "Mục tiêu tài chính" },
          targetAmount = Money(targetVal),
          savedAmount = Money(savedVal),
          deadline = deadlineInstant,
          category = getString("category") ?: "Khác",
          monthlyContribution = Money(monthlyVal),
          imageUri = getString("imageUri"),
          createdAt = createdInstant,
      )
  }.getOrNull()
  ```

---

### TRỤ CỘT 3: CẢI TỔ LOGIC SMART MERGE CHO MỤC TIÊU & NGÂN SÁCH (`RestoreBackupUseCase.kt`)

#### A. Đối với Mục tiêu (Goals):
- **Khử bỏ hoàn toàn:** `snapshotCreated.isAfter(existing.createdAt)`.
- **Logic hòa trộn chuẩn:**
  1. Đối chiếu mục tiêu theo `id` hoặc theo `name.trim().lowercase()`.
  2. Nếu mục tiêu ĐÃ TỒN TẠI trên máy:
     - So sánh nếu có sự thay đổi về giá trị:
       + Tiến độ tiết kiệm (`domainGoal.savedAmount != existing.savedAmount`).
       + Hạn mức mục tiêu (`domainGoal.targetAmount != existing.targetAmount`).
       + Ngày hết hạn (`domainGoal.deadline != existing.deadline`).
       + Danh mục mục tiêu (`domainGoal.category != existing.category`).
     - Nếu có thay đổi: Cập nhật bản ghi, giữ nguyên `id` gốc trên máy để bảo toàn tính toàn vẹn; tăng `conflictsResolved++` và `goalsRestored++`.
     - Nếu giống hệt: Ghi nhận `skippedCount++`.
  3. Nếu mục tiêu CHƯA TỒN TẠI trên máy:
     - Nạp mới qua `goalRepository.upsertGoal(domainGoal)`; tăng `goalsRestored++`.

#### B. Đối với Ngân sách (Budgets):
- **Khóa tự nhiên:** `${categoryId}_${periodKey}`.
- **Logic hòa trộn chuẩn:**
  1. Đối chiếu theo `id` (hoặc `"${domainBudget.categoryId}_${domainBudget.periodKey}"`).
  2. Nếu ngân sách ĐÃ TỒN TẠI:
     - Nếu hạn mức `limitAmount` trong snapshot khác biệt so với hiện tại: Cập nhật theo snapshot; tăng `conflictsResolved++` và `budgetsRestored++`.
     - Nếu giống hệt: Ghi nhận `skippedCount++`.
  3. Nếu ngân sách CHƯA TỒN TẠI:
     - Nạp mới qua `budgetRepository.upsertBudget(domainBudget)`; tăng `budgetsRestored++`.

---

### TRỤ CỘT 4: ĐỒNG BỘ GIAO DIỆN REALTIME CHO GOALS & BUDGETS (`DataSyncManager`)
- **Vấn đề:** Hiện tại chỉ `WalletsViewModel`, `HomeViewModel` và `TransactionsViewModel` lắng nghe xung `dataSyncManager.refreshTrigger`. `GoalsViewModel` và `BudgetViewModel` bị cô lập.
- **Giải pháp:**
  + **`GoalsViewModel.kt`:**
    ```kotlin
    @HiltViewModel
    class GoalsViewModel @Inject constructor(
        repository: GoalRepository,
        walletRepository: WalletRepository,
        private val saveGoal: SaveGoalUseCase,
        private val deleteGoal: DeleteGoalUseCase,
        private val depositToGoalUseCase: DepositToGoalUseCase,
        private val withdrawFromGoalUseCase: WithdrawFromGoalUseCase,
        private val dataSyncManager: DataSyncManager, // Inject singleton
    ) : ViewModel() {

        val goals = dataSyncManager.refreshTrigger.flatMapLatest {
            repository.observeGoals()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        val wallets = dataSyncManager.refreshTrigger.flatMapLatest {
            walletRepository.observeWallets()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        ...
    }
    ```
  + **`BudgetViewModel.kt`:**
    Inject `dataSyncManager: DataSyncManager`.
    Kết hợp `dataSyncManager.refreshTrigger` vào `periodAndBudgetsFlow` qua `flatMapLatest` để lập tức hủy bỏ luồng lắng nghe cũ và thiết lập lại truy vấn ngân sách theo chu kỳ hiện hành ngay khi có thông báo `notifyDataRestored()`.

---

### TRỤ CỘT 5: MA TRẬN KIỂM THỬ TOÀN DIỆN (UNIT TEST MATRIX)

Mục tiêu kiểm thử: Bổ sung các test cases sau và đảm bảo **100% test pass**, nâng tổng số unit tests vượt mốc **515+ tests**:

| Test Case ID | Lớp kiểm thử | Mô tả mục tiêu kiểm thử | Kỳ vọng (Assertion) |
| :--- | :--- | :--- | :--- |
| `T-BGT-WILDCARD` | `FirebaseBudgetRepositoryTest` | Truy vấn `observeBudgets("*")` khi Firestore có nhiều ngân sách khác periodKey | Trả về đủ 100% các ngân sách, không bị lọc rỗng |
| `T-GOL-DEFENSIVE` | `FirebaseGoalRepositoryTest` | Parse `DocumentSnapshot` có `deadline` kiểu String ISO và Long | Trả về `FinancialGoal` hợp lệ, không ném exception |
| `T-EXP-BGT-GOL` | `ExportBackupUseCaseTest` | Tạo backup khi người dùng có sẵn Budgets và Goals | File backup JSON có trường `"budgets"` và `"goals"` đủ số lượng |
| `T-RST-SMART-GOL` | `RestoreBackupUseCaseTest` | Smart Merge khôi phục Goal đã có trên máy nhưng snapshot có `savedAmount` mới hơn | Cập nhật thành công, `conflictsResolved == 1`, không bị skip |
| `T-RST-SMART-BGT` | `RestoreBackupUseCaseTest` | Smart Merge khôi phục Budget đã có trên máy với `limitAmount` cập nhật | Cập nhật thành công `budgetRepository.upsertBudget`, `conflictsResolved == 1` |
| `T-RST-FULL-BGT-GOL`| `RestoreBackupUseCaseTest` | Full Overwrite xóa sạch và khôi phục nguyên vẹn 100% Budgets & Goals | Xóa đúng danh sách cũ, nạp đủ danh sách snapshot mới |
| `T-SYNC-GOL-VM` | `GoalsViewModelTest` | Phát tín hiệu `dataSyncManager.notifyDataRestored()` | Flow `goals` tự động kích hoạt query lại và cập nhật state |
| `T-SYNC-BGT-VM` | `BudgetViewModelTest` | Phát tín hiệu `dataSyncManager.notifyDataRestored()` | Flow `state` tự động tái đồng bộ ngân sách chu kỳ hiện hành |

---

## 3. KẾ HOẠCH THỰC THI (ACTION PLAN & FILE MANIFEST)

1. **[MODIFY]** `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseBudgetRepository.kt`
   - Bỏ filter `.whereIn("periodKey", ...)` khi `periodKey == "*"` hoặc blank.
   - Bổ sung đọc an toàn `periodStart`, `periodEndExclusive`, `periodBasis` trong `toBudget()`.
2. **[MODIFY]** `app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseGoalRepository.kt`
   - Cập nhật `toGoal()` với cơ chế Defensive Parsing (Timestamp, Date, Long, String, Number).
3. **[MODIFY]** `app/src/main/java/com/finlux/app/domain/usecase/backup/RestoreBackupUseCase.kt`
   - Smart Merge Goals: Bỏ `createdAt.isAfter`, đối chiếu id/name, cập nhật khi có biến động `savedAmount`/`targetAmount`/`deadline`/`category`.
   - Smart Merge Budgets: Đối chiếu id/key, cập nhật khi `limitAmount` khác biệt.
4. **[MODIFY]** `app/src/main/java/com/finlux/app/presentation/goal/GoalsViewModel.kt`
   - Inject `DataSyncManager`, kết nối `refreshTrigger` vào `goals` và `wallets`.
5. **[MODIFY]** `app/src/main/java/com/finlux/app/presentation/budget/BudgetViewModel.kt`
   - Inject `DataSyncManager`, kết nối `refreshTrigger` vào `periodAndBudgetsFlow`.
6. **[MODIFY / NEW]** Test Suites:
   - `app/src/test/java/com/finlux/app/domain/usecase/backup/RestoreBackupUseCaseTest.kt`
   - `app/src/test/java/com/finlux/app/domain/usecase/backup/ExportBackupUseCaseTest.kt`
   - `app/src/test/java/com/finlux/app/presentation/goal/GoalsViewModelTest.kt`
   - `app/src/test/java/com/finlux/app/presentation/budget/BudgetViewModelTest.kt`
7. **[VERIFY]**
   - Chạy toàn bộ test: `.\gradlew.bat testDebugUnitTest` (Mục tiêu: 100% PASS, 515+ tests).
   - Build APK: `.\gradlew.bat assembleDebug`.
   - Nạp đè lên thiết bị vật lý qua ADB: `adb install -r app/build/outputs/apk/debug/app-debug.apk`.

---

## 4. BẢO VỆ HIẾN PHÁP DÒNG TIỀN & ĐẶC TẢ NGHIỆP VỤ (INVARIANTS CHECK)
- Không làm thay đổi semantic của `SystemCategories`.
- Toàn bộ thao tác đọc ghi tuân thủ Whitelist Firestore Rules của `budgets` và `goals`.
- Tuân thủ Kỷ luật Git: Không tự ý chạy `git commit` hay `git push` cho đến khi người dùng nghiệm thu thực tế trên điện thoại.
