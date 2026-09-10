# BÁO CÁO ĐIỀU TRA KỸ THUẬT & ĐỀ XUẤT KIẾN TRÚC: MAPPING DANH MỤC "TRẢ NỢ & TÍN DỤNG" KHI THANH TOÁN NỢ

> **Tình trạng:** Khảo sát & Đề xuất giải pháp (Chưa can thiệp mã nguồn)  
> **Dự án:** FinLux Android (Kotlin + Jetpack Compose + Clean Architecture + Firebase Firestore)  
> **Mục tiêu:** Giải quyết triệt để sự lệch pha giữa Quản lý danh mục, Tiến độ ngân sách nợ và Biên lai giao dịch thanh toán nợ.

---

## 1. TRUY VẾT NGUYÊN NHÂN LỆCH DANH MỤC (CATEGORY ID MISMATCH)

### 1.1. Hiện trạng trong mã nguồn: Repository đang gán `categoryId` là gì?
Khi người dùng thực hiện thanh toán nợ trong `ProcessDebtPaymentUseCase` và `FirebaseDebtRepository.processPayment(...)`:

Tại [`FirebaseDebtRepository.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseDebtRepository.kt) (dòng 218–226) và [`DemoFinluxRepository.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/data/demo/DemoFinluxRepository.kt) (dòng 417–423):
```kotlin
val txNote = if (note.isNotBlank()) note else "Thanh toán nợ: $debtName"
val categoryId = if (principalPaid > 0 && interestPaid == 0L) {
    "debt_principal"
} else if (interestPaid > 0 && principalPaid == 0L) {
    "debt_interest"
} else {
    "debt_payment"
}
```

* **Trường hợp giao dịch 10.000 đ trong hình 2 & 3:**
  Người dùng thanh toán 10.000 đ nợ gốc (`principalPaid = 10.000L`, `interestPaid = 0L`).
  ➡️ Biểu thức trên đánh giá nhánh đầu tiên: `categoryId = "debt_principal"`.
* **Ghi xuống Firestore:**
  Tài liệu tại `users/{uid}/transactions/{txId}` được lưu với trường:
  `"categoryId": "debt_principal"`.

---

### 1.2. ID thực tế của Danh mục hệ thống "Trả nợ & Tín dụng" trong Firestore là gì?
Tại [`FirebaseAuthRepository.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseAuthRepository.kt) (dòng 223–229) và [`DemoFinluxRepository.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/data/demo/DemoFinluxRepository.kt) (dòng 1111):
Danh mục mặc định của người dùng khi khởi tạo tài khoản được định nghĩa:
```kotlin
"debt_payment" to mapOf(
    "name" to "Trả nợ & Tín dụng",
    "type" to "expense",
    "icon" to "credit_card",
    "color" to "#E11D48",
    "isDefault" to true,
    "isEssential" to true
)
```
👉 **ID CHUẨN DUY NHẤT trong CSDL là `"debt_payment"`**.  
👉 Trong collection `users/{uid}/categories`, **HOÀN TOÀN KHÔNG TỒN TẠI** category nào có ID là `"debt_principal"` hay `"debt_interest"`.

---

### 1.3. Tại sao trên Biên lai giao dịch (#FLX-B562DB), danh mục hiển thị fallback thành "Chi tiêu"?
Tại [`TransactionDetailSheet.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/presentation/transaction/TransactionDetailSheet.kt) (dòng 329–335) và [`FinluxNavHost.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/core/navigation/FinluxNavHost.kt) (dòng 456):
```kotlin
// FinluxNavHost:
category = allCategories[tx.categoryId] // allCategories["debt_principal"] -> NULL!

// TransactionDetailSheet:
DetailItemRow(
    icon = headerIcon,
    label = if (isTransfer) "Loại giao dịch" else "Danh mục",
    value = if (isTransfer) "Chuyển tiền giữa các ví" else (category?.name ?: if (isIncome) "Thu nhập" else "Chi tiêu"),
)
```
1. Khi bấm xem biên lai #FLX-B562DB, giao dịch mang `categoryId = "debt_principal"`.
2. Hệ thống tra cứu `allCategories["debt_principal"]` ➡️ Kết quả trả về `null` vì không có category nào mang ID này.
3. Khi `category == null` và `transaction.type == EXPENSE`, code tự động nhảy vào nhánh fallback:
   `if (isIncome) "Thu nhập" else "Chi tiêu"` ➡️ **Hiển thị chữ "Chi tiêu"**!
4. **Hệ quả dây chuyền trên Ngân sách (Budget):**
   - Ngân sách "Trả nợ & Tín dụng" (hình 3) được tạo với `budget.categoryId = "debt_payment"`.
   - Thuật toán tính `spentAmount` trong `BudgetViewModel.kt` (dòng 89–92) và `PrismHomeScreen.kt` (dòng 2449) chỉ gom các giao dịch có `tx.categoryId == budget.categoryId` (tức `"debt_payment"`).
   - Vì giao dịch 10.000 đ mang ID `"debt_principal"` (mồ côi), nó **BỊ BỎ QUA HOÀN TOÀN**, không được tính vào ngân sách nợ! Số 4.5 tr trong ngân sách trước đó là do các giao dịch khác vô tình rơi vào nhánh `else { "debt_payment" }` (khi trả cả gốc + lãi cùng lúc) hoặc do người dùng tạo tay.

---

## 2. PHÂN TÍCH TÍNH HỢP LÝ CỦA VIỆC MAPPING VÀO DANH MỤC

### 2.1. Nhu cầu thực tế về Ngân sách (Budget Allocation)
- **Hành vi người dùng:** Người dùng có lương 20 triệu, họ lên kế hoạch phân bổ dòng tiền:
  * 6 triệu ăn uống
  * 2 triệu xăng xe
  * **8 triệu để trả nợ ngân hàng / nợ cá nhân**
- Ngân sách bản chất là một **hạn mức dòng tiền chi ra (Cash Outflow Envelope)**.
- Nếu thanh toán nợ không được map vào danh mục "Trả nợ & Tín dụng" (`debt_payment`), tiến độ ngân sách nợ sẽ không phản ánh số tiền người dùng đã trích ra trả nợ, phá vỡ hoàn toàn công năng của tính năng Ngân sách.
- **Kết luận:** Giao dịch trả nợ **BẮT BUỘC PHẢI MAP VÀO DANH MỤC "Trả nợ & Tín dụng" (`debt_payment`)** để:
  1. Hiển thị biên lai đẹp mắt: Icon thẻ tín dụng hồng `#E11D48`, Tên danh mục "Trả nợ & Tín dụng".
  2. Đồng bộ tiến độ ngân sách: Trả 10.000 đ thì ngân sách nợ tăng thêm 10.000 đ.

---

### 2.2. Xung đột Kế toán: Chi phí sinh hoạt (Living Expenses) vs Hoán đổi Tài sản (Balance Sheet Transfer)
Ở các phiên làm việc trước, hệ thống FinLux đã xác lập nguyên lý tài chính cá nhân chuẩn mực:
* **Trả gốc nợ (Principal Repayment):**
  * Giảm Tiền mặt ở Ví (-10 tr) ➡️ Giảm Dư nợ Phải trả (-10 tr).
  * `Tài sản ròng (True Net Worth) = Tổng Tài Sản - Tổng Nợ = (-10 tr) - (-10 tr) = 0`.
  * Trả gốc **KHÔNG LÀM THAY ĐỔI TÀI SẢN RÒNG**, đây là chuyển dịch vốn (hoán đổi nghĩa vụ nợ), không phải chi phí tiêu hao sinh hoạt (Living Expense) như ăn uống, xem phim.
  * Nếu gộp trả gốc nợ vào "Tổng chi tiêu sinh hoạt tháng", người dùng sẽ thấy tháng đó mình "chi xài hoang phí", tỷ lệ tiết kiệm bị âm nặng dù thực tế họ đang giảm nợ để giàu lên.
* **Trả tiền lãi (Interest Payment):**
  * Giảm Tiền mặt ở Ví (-1 tr) ➡️ Không làm giảm dư nợ gốc.
  * Đây là **Chi phí tài chính thực sự (Financial Expense)**, làm hao hụt tài sản ròng.

---

### 2.3. Giải pháp phân tách ngữ nghĩa (Transaction Semantics & Separation of Concerns)
Làm thế nào để:
- **Biên lai & Ngân sách**: Vẫn hiển thị danh mục "Trả nợ & Tín dụng" (`debt_payment`), ngân sách đếm đủ 100% dòng tiền chi trả.
- **Báo cáo Thu/Chi (Living Expenses)**: Không bị đội ảo, không tính trả gốc nợ vào sinh hoạt phí tiêu dùng thường nhật?

#### 💡 Cơ chế phân tách tối ưu:
1. **Tại tầng Lưu trữ (Firestore & Model):**
   - Mọi giao dịch trả nợ vay (Loan Payment) đều gán danh mục hiển thị chính: `categoryId = "debt_payment"`.
   - Để bóc tách nghiệp vụ kế toán, `FinanceTransaction` bổ sung hoặc tận dụng trường phân loại dòng tiền:
     * Sử dụng cờ ngữ nghĩa hoặc trường metadata có sẵn: `dealFlowType` hoặc chuẩn hóa một trường `financeSubtype = "DEBT_PRINCIPAL" | "DEBT_INTEREST"`.
     * Hoặc đơn giản và triệt để nhất: Định nghĩa hàm mở rộng trong [`TransactionSemantics.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/domain/model/TransactionSemantics.kt):
       ```kotlin
       /** Giao dịch có phải là chi phí sinh hoạt tiêu dùng thực tế hay không */
       fun FinanceTransaction.isLivingExpense(): Boolean {
           if (type != TransactionType.EXPENSE) return false
           if (dealFlowType == DealFlowType.OUTLAY_CAPITAL) return false
           // Trả gốc nợ là dịch chuyển tài sản, không tính vào chi tiêu sinh hoạt tiêu dùng
           if (categoryId == DEBT_PAYMENT_CATEGORY_ID && isDebtPrincipalOnly) return false
           return true
       }
       ```
2. **Trong Báo cáo Thu/Chi (`ReportsViewModel.kt`):**
   - **Báo cáo Chi tiêu Sinh hoạt (Living Expenses):** Lọc theo `isLivingExpense()`. Chỉ tính các khoản chi tiêu hàng ngày + Tiền lãi vay (nếu có).
   - **Báo cáo Dòng tiền Chi ra (Total Cash Outflow):** Tính toàn bộ dòng tiền rút khỏi ví (bao gồm cả Tiền sinh hoạt + Tiền trả nợ gốc + Đầu tư).
   - Giao diện Báo cáo hiển thị 2 dòng minh bạch:
     * *Chi phí sinh hoạt:* 12.000.000 đ
     * *Trả nợ & Tích lũy tài sản:* 8.000.000 đ
     * *Tổng dòng tiền ra:* 20.000.000 đ  
     ➡️ Người dùng sẽ cực kỳ tâm đắc vì app vừa quản lý chặt chẽ ngân sách 8 tr trả nợ, vừa thông minh không coi việc trả nợ là tiêu hoang!

---

## 3. XỬ LÝ KHÁC BIỆT GIỮA KHOẢN VAY VS THẺ TÍN DỤNG

### 3.1. Đối với Khoản vay thông thường (Bank Loan / Personal Loan / Mượn người thân)
* **Bản chất dòng tiền:** Rút tiền từ ví (Ví tiền mặt / ATM) chuyển cho bên thứ ba.
* **Giao dịch sinh ra:** `EXPENSE` (Khoản chi).
* **Danh mục gắn vào:** `"debt_payment"` ("Trả nợ & Tín dụng").
* **Hiển thị biên lai:** Biên lai chi tiền, có nhãn "Trả nợ & Tín dụng", ghi chú chi tiết: `"Thanh toán nợ: <Tên khoản nợ>"`.
* **Ảnh hưởng ngân sách:** Cộng vào Ngân sách "Trả nợ & Tín dụng".

---

### 3.2. Đối với Thẻ tín dụng (Credit Card Repayment / Trả sao kê)
* **Bản chất kế toán của Thẻ tín dụng:**
  1. Khi người dùng dùng thẻ tín dụng quẹt mua sắm thoại/ăn uống:
     - Giao dịch `EXPENSE` đã được ghi nhận ngay lúc quẹt thẻ (Ví: Thẻ tín dụng, Danh mục: Ăn uống, Tiền: 2.000.000 đ).
     - Ngân sách "Ăn uống" **đã bị trừ 2.000.000 đ** tại thời điểm đó.
     - Số dư ví Thẻ tín dụng bị âm (-2.000.000 đ).
  2. Đến ngày thanh toán sao kê:
     - Người dùng lấy 2.000.000 đ từ Ví Vietcombank nạp vào Ví Thẻ tín dụng để xóa số âm.
     - Đây thuần túy là **CHUYỂN TIỀN NỘI BỘ GIỮA CÁC VÍ (`TRANSFER`)**:
       `TRANSFER_OUT` từ Vietcombank ➔ `TRANSFER_IN` vào Thẻ tín dụng.
* **CẢNH BÁO NGUY CƠ TÍNH TRÙNG 2 LẦN (DOUBLE COUNTING):**
  * Nếu thao tác chuyển tiền trả thẻ tín dụng này lại bị ép thành `EXPENSE` và gán danh mục "Trả nợ & Tín dụng", người dùng sẽ bị **tính chi phí 2 lần cho cùng một khoản tiền**:
    - Lần 1: Mua đồ ăn (Chi phí 2 tr).
    - Lần 2: Trả nợ thẻ (Chi phí 2 tr).
    ➡️ Tổng chi phí thành 4 tr và làm vỡ cả 2 ngân sách!
* **Quy tắc chuẩn cho Thẻ tín dụng:**
  * Giữ nguyên cơ chế hiện tại của [`FirebaseDebtRepository.kt`](file:///d:/Sources/FinLux/app/src/main/java/com/finlux/app/data/remote/firebase/FirebaseDebtRepository.kt) (dòng 171–213):
    Tạo cặp giao dịch `TRANSFER_OUT` / `TRANSFER_IN`.
  * Giao dịch này **KHÔNG PHẢI EXPENSE**, không gắn danh mục chi tiêu, không tính vào ngân sách chi tiêu để chống double counting.
  * Hạn mức thanh toán thẻ được theo dõi độc lập qua hạn mức thẻ tín dụng trên màn hình Quản lý nợ & Thẻ tín dụng.

---

## 4. BẢNG TỔNG HỢP SO SÁNH MA TRẬN ĐỀ XUẤT

| Loại khoản nợ | Thao tác | Loại giao dịch (`type`) | `categoryId` gán vào | Hiển thị Biên lai | Tính vào Ngân sách "Trả nợ & Tín dụng"? | Tính vào Chi phí sinh hoạt (`Living Expense`)? |
|:---|:---|:---:|:---:|:---|:---:|:---:|
| **Khoản vay (Gốc)** | Trả nợ gốc | `EXPENSE` | `"debt_payment"` | ✨ Trả nợ & Tín dụng | ✅ Có (quản lý trích dòng tiền) | ❌ Không (hoán đổi tài sản) |
| **Khoản vay (Lãi)** | Trả tiền lãi | `EXPENSE` | `"debt_payment"` | ✨ Trả nợ & Tín dụng | ✅ Có | ✅ Có (chi phí tài chính thực) |
| **Khoản vay (Gốc + Lãi)** | Trả kỳ hạn | `EXPENSE` | `"debt_payment"` | ✨ Trả nợ & Tín dụng | ✅ Có | Chỉ tính phần Lãi |
| **Thẻ tín dụng** | Trả sao kê | `TRANSFER` | `null` | 🔄 Chuyển tiền nội bộ ví | ❌ Không (tránh double counting) | ❌ Không (đã tính lúc quẹt thẻ) |

---

## 5. KẾT LUẬN & ĐỀ XUẤT HÀNH ĐỘNG TIẾP THEO

1. **Sửa lỗi tận gốc Category ID Mismatch:**
   - Trong `FirebaseDebtRepository.kt` & `DemoFinluxRepository.kt`: Thay thế hoàn toàn chuỗi `"debt_principal"` và `"debt_interest"` bằng ID chuẩn hệ thống **`"debt_payment"`** (`DEBT_PAYMENT_CATEGORY_ID`).
   - Điều này ngay lập tức sửa được 2 lỗi:
     * Biên lai #FLX-B562DB sẽ hiển thị đúng tên **"Trả nợ & Tín dụng"** cùng icon thẻ tín dụng chuẩn thay vì fallback "Chi tiêu".
     * Tiến độ ngân sách "Trả nợ & Tín dụng" (8 tr) sẽ đếm chính xác 100% các khoản trả nợ.
2. **Bảo toàn Báo cáo Chi tiêu Sinh hoạt:**
   - Cập nhật hàm ngữ nghĩa trong `TransactionSemantics.kt` và áp dụng vào `ReportsViewModel.kt` để khi tính Chi phí sinh hoạt hàng ngày (Living Expenses), hệ thống nhận diện giao dịch trả gốc nợ không làm méo mó biểu đồ chi tiêu tiêu dùng.
3. **Tuân thủ chỉ đạo:**
   - Đã dừng lại ở báo cáo điều tra kỹ thuật.
   - Chờ Người dùng xem xét và phê duyệt trước khi viết code triển khai.
