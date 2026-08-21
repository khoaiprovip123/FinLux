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
  │    ├─ categoryId: string        -- null nếu type = transfer
  │    ├─ walletId: string
  │    ├─ relatedWalletId: string   -- chỉ dùng cho transfer (ví đối ứng)
  │    ├─ note: string
  │    ├─ receiptImageUrl: string   -- optional
  │    ├─ date: timestamp           -- ngày giao dịch thực tế (dùng để tính báo cáo, BR-10)
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
  └─ notifications/{notificationId}
       ├─ title: string
       ├─ body: string
       ├─ type: "budget_warning" | "budget_exceeded" | "reminder"
       ├─ isRead: boolean
       └─ createdAt: timestamp
```

**Ghi chú thiết kế:**
- Dùng **subcollection** dưới `users/{uid}` thay vì collection gốc kèm `ownerId` field → Security Rules đơn giản, tự nhiên phân vùng dữ liệu theo user, tránh vượt giới hạn document.
- `wallets.balance` và `budgets.spentAmount` là dữ liệu **denormalized** (tính sẵn) để đọc nhanh trên Home/Budget mà không cần aggregate query mỗi lần mở app — đánh đổi lấy việc phải cập nhật đồng bộ khi ghi transaction (qua Firestore Transaction ở client, và Cloud Function double-check).

---

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

`visual_style` nhận một trong ba giá trị enum: `MODERN_DARK`, `GLASSMORPHISM`, `DYNAMIC_GRADIENT`;
giá trị mặc định cho cài mới là `DYNAMIC_GRADIENT`.
