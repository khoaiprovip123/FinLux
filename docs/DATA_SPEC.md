# DATA & SERVICE SPEC — Finlux (Firebase)

App dùng Firebase nên không có REST API tự viết cho CRUD cơ bản (client gọi trực tiếp Firestore SDK
qua offline-first listener). Chỉ dùng **Cloud Functions** cho logic cần chạy phía server (tính ngân sách,
trigger thông báo). Phần dưới thay thế cho API_SPEC + DB_SCHEMA truyền thống.

---

## 1. Cấu trúc Firestore

```
users/{uid}
  ├─ displayName: string
  ├─ email: string
  ├─ photoUrl: string
  ├─ fcmTokens: string[]             -- token thiết bị, cập nhật bằng arrayUnion khi đăng nhập/onNewToken
  ├─ themePref: "light" | "dark" | "system"
  ├─ createdAt: timestamp
  │
  ├─ wallets/{walletId}
  │    ├─ name: string              -- "Tiền mặt", "Vietcombank", ...
  │    ├─ type: "cash" | "bank" | "ewallet" | "card" | "investment" | "other"
  │    ├─ balance: number           -- cập nhật qua Firestore Transaction (BR-14)
  │    ├─ color: string
  │    ├─ isDefault: boolean
  │    └─ createdAt: timestamp
  │         -- Logo không lưu vào Firestore. UI đối chiếu wallet.name với catalog VietQR cục bộ;
  │            ví nhập tự do dùng fallback theo WalletType để giữ tương thích dữ liệu cũ.
  │
  ├─ categories/{categoryId}
  │    ├─ name: string
  │    ├─ type: "income" | "expense"
  │    ├─ icon: string
  │    ├─ color: string
  │    ├─ isDefault: boolean        -- true = seed mặc định, không cho xóa nếu đã dùng
  │    └─ createdAt: timestamp
  │
  ├─ transactions/{transactionId}
  │    ├─ type: "income" | "expense" | "transfer_out" | "transfer_in"  (BR-07)
  │    ├─ amount: number            -- luôn dương (BR-05)
  │    ├─ categoryId: string?       -- null nếu type = transfer hoặc deal capital outlay
  │    ├─ walletId: string
  │    ├─ relatedWalletId: string?  -- chỉ dùng cho transfer (ví đối ứng)
  │    ├─ dealId: string?           -- liên kết với Deal (UC-29)
  │    ├─ dealFlowType: "OUTLAY_CAPITAL" | "PRINCIPAL_RECOVERY" | "CAPITAL_GAIN" | "CAPITAL_LOSS" | null
  │    ├─ note: string
  │    ├─ receiptImageUrl: string?  -- optional
  │    ├─ date: timestamp           -- ngày giao dịch thực tế (dùng để tính báo cáo, BR-10)
  │    ├─ createdAt: timestamp
  │    └─ updatedAt: timestamp
  │
  ├─ deals/{dealId}                 -- quản lý thương vụ & đầu tư sinh lời / cho vay (UC-29, BR-DEAL-01..05)
  │    ├─ title: string              -- "Mua bán xe lướt", "Cho bạn Nam mượn", ...
  │    ├─ description: string
  │    ├─ category: "investment" | "lending" -- phân loại: Đầu tư hoặc Cho vay
  │    ├─ targetAmount: number       -- mục tiêu thu về kỳ vọng
  │    ├─ totalCapitalOutlay: number -- tổng vốn đã chi xuất / nợ gốc đã cho vay (cập nhật qua Transaction)
  │    ├─ totalRecovered: number     -- tổng vốn/nợ gốc đã thu hồi
  │    ├─ netProfitLoss: number      -- lợi nhuận ròng / tiền lãi thực hiện tích lũy
  │    ├─ status: "ACTIVE" | "COMPLETED" | "CANCELLED"
  │    ├─ startDate: timestamp
  │    ├─ endDate: timestamp?
  │    ├─ createdAt: timestamp
  │    └─ updatedAt: timestamp
  │
  ├─ budgets/{budgetId}             -- id format: {categoryId}_{yyyyMM}
  │    ├─ categoryId: string
  │    ├─ month: string             -- "2026-08"
  │    ├─ limitAmount: number
  │    ├─ spentAmount: number       -- Cloud Function cập nhật khi có transaction mới (denormalize để đọc nhanh)
  │    ├─ notified80: boolean       -- đã gửi cảnh báo 80% chưa (BR-09)
  │    └─ notified100: boolean
  │
  ├─ reminders/{reminderId}         -- bill định kỳ (UC-18)
  │    ├─ title: string
  │    ├─ amount: number
  │    ├─ categoryId: string
  │    ├─ walletId: string
  │    ├─ recurrence: "daily" | "weekly" | "monthly"
  │    ├─ startDate: timestamp
  │    ├─ enabled: boolean
  │    └─ nextTriggerDate: timestamp
  │
  ├─ goals/{goalId}                 -- mục tiêu tài chính (UC-25)
  │    ├─ name: string
  │    ├─ targetAmount: number
  │    ├─ savedAmount: number
  │    ├─ deadline: timestamp
  │    ├─ category: string
  │    ├─ monthlyContribution: number
  │    ├─ imageUri: string          -- optional
  │    └─ createdAt: timestamp
  │
  ├─ debts/{debtId}                 -- quản lý công nợ & tín dụng (UC-26, BR-DEBT-01..03)
  │    ├─ name: string              -- "Thẻ VCB Signature", "Vay VPBank", ...
  │    ├─ type: "CREDIT_CARD" | "BANK_LOAN" | "PERSONAL_LOAN" | "INSTALLMENT"
  │    ├─ totalAmount: number       -- hạn mức / khoản vay gốc
  │    ├─ remainingBalance: number  -- dư nợ hiện tại (cập nhật qua Transaction)
  │    ├─ interestRateApr: number   -- lãi suất %/năm (vd: 18.5)
  │    ├─ minimumPayment: number    -- trả tối thiểu/tháng
  │    ├─ dueDate: number           -- ngày đến hạn (1..31)
  │    ├─ statementDate: number?    -- ngày chốt sao kê thẻ tín dụng
  │    ├─ colorHex: string
  │    ├─ isSettled: boolean
  │    ├─ createdAt: timestamp
  │    ├─ updatedAt: timestamp
  │    │
  │    └─ payments/{paymentId}      -- lịch sử thanh toán nợ
  │         ├─ debtId: string
  │         ├─ walletId: string
  │         ├─ amount: number
  │         ├─ principalPaid: number
  │         ├─ interestPaid: number
  │         ├─ paymentDate: timestamp
  │         └─ note: string
  │
  ├─ financialPreferences/salaryCycle -- cấu hình chu kỳ lương & tháng tài chính (UC-27, BR-SALARY-01..03)
  │    ├─ enabled: boolean
  │    ├─ paydayRuleType: "DAY_OF_MONTH" | "FIRST_DAY_OF_MONTH" | "LAST_DAY_OF_MONTH"
  │    ├─ paydayDay: number         -- 1..31 (mặc định 25)
  │    ├─ salaryWalletId: string?
  │    ├─ savingsWalletId: string?
  │    ├─ expectedSalary: number?
  │    ├─ rolloverRule: "KEEP_IN_WALLET" | "ASK_EACH_CYCLE" | "MOVE_TO_SAVINGS"
  │    ├─ budgetPeriodBasis: "CALENDAR_MONTH" | "SALARY_CYCLE"
  │    ├─ financeTimeZone: string   -- "Asia/Ho_Chi_Minh"
  │    └─ updatedAt: timestamp
  │
  └─ notifications/{notificationId}    -- ID định danh: reminder_{reminderId}_{epochDay} cho thông báo nhắc nhở để đảm bảo Idempotency
       ├─ title: string
       ├─ body: string
       ├─ type: "budget_warning" | "budget_exceeded" | "reminder"
       ├─ isRead: boolean
       ├─ isPaid: boolean               -- true khi hóa đơn nhắc nhở đã được thanh toán
       ├─ reminderId: string?
       ├─ amount: number?
       ├─ categoryId: string?
       ├─ walletId: string?
       ├─ timestamp: timestamp
       └─ createdAt: timestamp
```

**Ghi chú thiết kế:**
- Dùng **subcollection** dưới `users/{uid}` thay vì collection gốc kèm `ownerId` field → Security Rules đơn giản, tự nhiên phân vùng dữ liệu theo user, tránh vượt giới hạn document.
- `wallets.balance` và `budgets.spentAmount` là dữ liệu **denormalized** (tính sẵn) để đọc nhanh trên Home/Budget mà không cần aggregate query mỗi lần mở app — đánh đổi lấy việc phải cập nhật đồng bộ khi ghi transaction (qua Firestore Transaction ở client, và Cloud Function double-check).

---

### 1.1. Saving Spin ledger (UC-29)

```
users/{uid}/savingSpinConfigs/default
  ├─ enabled, showOnHome: boolean
  ├─ minAmount, maxAmount, stepAmount: number
  ├─ slotCount: 6 | 8 | 10 | 12
  ├─ frequency: DAILY | SELECTED_WEEKDAYS | WEEKLY | SALARY_CYCLE
  ├─ selectedWeekdays: number[]
  ├─ weeklyDay, reminderHour, reminderMinute: number
  ├─ reminderEnabled, snoozeEnabled, allowSkip: boolean
  ├─ defaultDestinationId: string?
  └─ schemaVersion, createdAt, updatedAt

users/{uid}/savingSpinDestinations/{destinationId}
  ├─ name: string
  ├─ method: CASH | BANK_TRANSFER
  ├─ linkedWalletId, institutionId, accountHint: string?
  └─ enabled, createdAt, updatedAt

users/{uid}/savingSpinSessions/{scheduleId}
  ├─ scheduleKey: string
  ├─ wheelValues: number[]
  ├─ selectedIndex, selectedAmount: number?
  ├─ status: READY | SPUN_PENDING | COMPLETED | SNOOZED | SKIPPED
  ├─ destinationId, method: string?
  └─ spunAt, completedAt, skippedAt, snoozedUntil, createdAt, updatedAt
```

- `lockSpinResult` và `getOrCreateSession` chạy bằng Firestore transaction; selected result đã khóa
  không được ghi đè.
- Saving Spin là ledger riêng, không tự tạo transaction và không cập nhật `wallet.balance` ở v1.
- Query report range theo `createdAt`; nếu production yêu cầu composite index phải bổ sung vào
  `firestore.indexes.json` trước release.

## 2. Firebase Storage

```
avatars/{uid}.jpg              -- ảnh đại diện, overwrite mỗi lần đổi (UC-05)
receipts/{uid}/{transactionId}.jpg   -- ảnh hóa đơn đính kèm giao dịch (optional)
```

---

## 3. Firestore Security Rules (nguyên tắc — viết chi tiết khi dev)

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid} {
      allow read, update: if request.auth != null && request.auth.uid == uid;
      allow create: if request.auth != null && request.auth.uid == uid;

      match /{subcollection}/{docId} {
        allow read, write: if request.auth != null && request.auth.uid == uid;
      }
    }
  }
}
```
> Nguyên tắc: user chỉ đọc/ghi được dữ liệu trong nhánh `users/{uid của chính mình}`. Cloud Functions
> chạy với quyền Admin SDK nên không bị giới hạn bởi rule này.

---

## 4. Cloud Functions (server-side logic)

| Function | Trigger | Mô tả |
|----------|---------|-------|
| `onTransactionWrite` | Firestore trigger: `users/{uid}/transactions/{id}` onCreate/onUpdate/onDelete | Đối soát `budgets.spentAmount` theo dữ liệu giao dịch thực tế và gửi cảnh báo 80%/100%; số dư ví đã được Security Rules bắt buộc ghi nguyên tử cùng transaction |
| `checkBudgetThreshold` | Gọi từ `onTransactionWrite` sau khi update `spentAmount` | So sánh với `limitAmount`, nếu ≥80%/100% và chưa `notified80/100` → gửi FCM + tạo `notifications` doc, set flag true (BR-09) |
| `monthlyBudgetReset` | Scheduled (Cloud Scheduler, 00:00 ngày 1 hàng tháng) | Tạo document `budgets` mới cho tháng mới dựa trên hạn mức tháng trước (giữ nguyên `limitAmount`, reset `spentAmount=0`) |
| `sendReminderPush` | Scheduled (chạy mỗi giờ, kiểm tra `reminders.nextTriggerDate`) | Gửi FCM nhắc user nhập giao dịch bill định kỳ, cập nhật `nextTriggerDate` kế tiếp |

---

## 5. Export Excel/PDF (xử lý client-side, không cần server)

- **Excel:** dùng thư viện Apache POI (hoặc thư viện Kotlin tương đương nhẹ hơn nếu cần giảm dung lượng APK) — sinh file `.xlsx` 2 sheet theo BR-11, lưu vào `Downloads/` qua `MediaStore` API (Android 10+ Scoped Storage).
- **PDF:** dùng `android.graphics.pdf.PdfDocument` kết hợp render Compose UI báo cáo thành ảnh (hoặc vẽ trực tiếp Canvas) để xuất file tóm tắt có biểu đồ.
- Sau khi tạo file → dùng `Intent.ACTION_SEND` / `FileProvider` để user chia sẻ (Zalo, Email, Drive…) ngay từ app.

---

## 6. Offline & Sync

- Bật `FirebaseFirestore.setPersistenceEnabled(true)` (mặc định Android đã bật offline cache).
- Toàn bộ UI đọc dữ liệu qua Firestore **snapshot listener** (`addSnapshotListener`) chứ không one-shot `get()`, để tự động nhận cập nhật cả từ cache offline lẫn realtime khi có mạng trở lại → đáp ứng UC-19.
- Ghi dữ liệu khi offline vẫn thành công tức thời trên UI (optimistic update của Firestore SDK), tự đẩy lên server khi có mạng.

---

## 7. Local UI Preferences (DataStore)

Các khóa `visual_style`, `glass_intensity`, `card_density`, `animations_enabled` chỉ lưu cục bộ trên thiết bị.
Chúng không nằm trong Firestore và không tác động tới số dư, giao dịch hoặc báo cáo.

Việc ẩn thẻ kỳ tài chính trên Home và căn giữa các KPI là presentation-only, không thêm khóa DataStore
hoặc thay đổi `financialPreferences/salaryCycle`; KPI vẫn dùng chu kỳ đã lưu khi tính toán.
Quy tắc co cỡ chữ KPI, bố cục hai dòng của chú giải biểu đồ và vật liệu kính REGULAR cũng chỉ thuộc
presentation layer; số tiền, tỷ trọng và dữ liệu giao dịch vẫn giữ nguyên độ chính xác trong repository.

`visual_style` nhận một trong ba giá trị enum: `MODERN_DARK`, `GLASSMORPHISM`, `DYNAMIC_GRADIENT`;
giá trị mặc định cho cài mới là `DYNAMIC_GRADIENT`.

## 8. Derived financial semantics (không đổi schema)

- Firestore tiếp tục lưu chuyển tiền theo cặp `<base>_out` và `<base>_in`; presentation gọi
  `collapseInternalTransferPairs()` để tạo một dòng logic. Bản ghi `_in` không có `_out` tương ứng
  vẫn được giữ lại để người dùng thấy dữ liệu đồng bộ chưa hoàn chỉnh.
- Tổng tài sản dùng `assetWallets()`/`totalAssetBalance()`: chỉ ví `status == active` và loại khác
  `CARD`. Thẻ tín dụng và dư nợ được trình bày ở liability, không cộng vào gross assets.
- Đóng góp mục tiêu trong kỳ dùng category mặc định `savings`: tổng EXPENSE nạp mục tiêu trừ tổng
  INCOME rút mục tiêu, chặn dưới ở 0. Đây là aggregate dẫn xuất, không thêm field Firestore.

