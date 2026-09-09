# BÁO CÁO RÀ SOÁT TỒN ĐỌNG KỸ THUẬT & KẾ HOẠCH HÀNH ĐỘNG TUẦN TỰ
## (FinLux Master Pending Backlog & Sequential Action Plan)

**Phiên bản ứng dụng:** v1.22.1 (Build 166)  
**Nhánh Git:** `main` (Đã đồng bộ và push lên `origin/main` commit `467bc56`)  
**Tài liệu tham chiếu:** `docs/BA_SPEC.md`, `docs/DATA_SPEC.md`, `docs/UI_UX_AUDIT_FINDINGS.md`, `docs/audit-2026-09-fix-roadmap/`  
**Ngày lập:** 09/09/2026  

---

## I. TỔNG QUAN HIỆN TRẠNG TOÀN HỆ THỐNG

Sau khi hoàn tất việc sửa lỗi và nghiệm thu điều hướng thông báo (Push Notification Routing v1.22.1), hợp nhất nhánh `main` và chạy toàn diện 302 unit tests (100% PASS), hệ thống FinLux hiện đạt trạng thái vận hành ổn định trên local device. 

Tuy nhiên, qua rà soát sâu đối chiếu giữa **Mã nguồn Android**, **Firestore Rules**, **Cloud Functions backend**, **Storage Rules** và **Tài liệu đặc tả (BA/DATA SPEC)**, dự án còn một số điểm nghẽn nghiêm trọng (Contract Drift, Lỗ hổng bảo mật, Nợ kỹ thuật) cần được giải quyết dứt điểm trước khi mở rộng thêm các tính năng mới.

---

## II. BẢNG DANH MỤC CÁC HẠNG MỤC TỒN ĐỌNG KỸ THUẬT

| STT | Hạng mục / Tính năng | Phân loại | Mức độ ưu tiên | Hiện trạng chi tiết & Rủi ro nếu chưa xử lý |
| :---: | :--- | :---: | :---: | :--- |
| **01** | **Đồng bộ Firestore Rules cho Deals, Budgets & CorrelationId** | **Bảo mật & Backend** | 🔴 **P0 (Blocker)** | • `firestore.rules` **chưa có rule** cho subcollection `deals/{dealId}`.<br>• `validTransaction` thiếu các trường `dealId`, `dealFlowType`, `correlationId`.<br>• Subcollection `budgets` trong rules chỉ chấp nhận `month`, chưa chấp nhận các trường mới: `periodKey`, `periodStart`, `periodEndExclusive`, `periodBasis`.<br>⚠️ **Rủi ro:** Khi chạy trên Firebase Production, mọi thao tác tạo Deal, tạo giao dịch liên quan Deal hoặc tạo Ngân sách theo chu kỳ lương sẽ bị Firestore **từ chối hoàn toàn (PERMISSION_DENIED)**. |
| **02** | **Đồng bộ Cloud Functions với Chu kỳ lương & Kỳ tài chính** | **Nghiệp vụ & Backend** | 🔴 **P0 (Blocker)** | • `functions/src/index.ts` đang đọc đường dẫn cũ `users/${uid}/preferences/salaryCycle` (trong khi Android và Rules dùng `users/${uid}/financialPreferences/salaryCycle`).<br>• Dùng trường `baseDay` thay vì `paydayDay` và `paydayRuleType`.<br>⚠️ **Rủi ro:** Cloud Functions không thể đọc cấu hình chu kỳ tài chính của người dùng, dẫn đến chức năng quét tự động, chuyển kỳ lương và tính toán ngân sách trên backend bị hỏng hoàn toàn. |
| **03** | **Loại bỏ Ví ảo `DEAL_SETTLEMENT` trong Tất toán Lỗ Thương vụ** | **Toàn vẹn Dữ liệu (Ledger)** | 🔴 **P0 (Blocker)** | • Khi tất toán Deal bị lỗ vốn (Capital Loss), hệ thống đang sinh giao dịch với `walletId = "DEAL_SETTLEMENT"`.<br>• Đây là ví không có thật trong collection `wallets`.<br>⚠️ **Rủi ro:** Vi phạm bất biến sổ cái kép (Ledger Invariant: mọi giao dịch ví phải trỏ về ví hợp lệ). Làm sai lệch đối chiếu số dư và gây lỗi xác thực khi áp dụng ràng buộc khóa ví. |
| **04** | **Thắt chặt Bảo mật AndroidManifest & Firebase Storage Rules** | **Bảo mật (Security)** | 🔴 **P0 (Blocker)** | • `SalaryCycleReceiver` trong `AndroidManifest.xml` đang để `android:exported="true"` kèm action `ACTION_SALARY_PAYDAY`.<br>• `storage.rules`: Avatar đang mở quyền đọc cho mọi user đăng nhập (`request.auth != null`), Receipt rule đang kiểm tra kích thước file trong cả thao tác read/delete.<br>⚠️ **Rủi ro:** Ứng dụng độc hại trên máy có thể phát broadcast giả mạo kích hoạt luân chuyển tiền chu kỳ lương; người dùng này có thể xem trộm avatar của người dùng khác. |
| **05** | **Hoàn thiện Báo Cáo Chuyên Sâu 2.0 (Drill-down & True Net Worth Engine)** | **Nghiệp vụ (Feature)** | 🟠 **P1 (High)** | • Mặc dù Foundation của Reporting 2.0 (số dư đầu/cuối ngày, bảng kê dòng tiền, thẻ Hero số dư ví, theo dõi chuyển tiền) đã hoàn thành, luồng Drill-down chi tiết: `Tổng quan → Từng Ví → Danh mục → Giao dịch` vẫn chưa được kết nối trơn tru.<br>• Công thức tính **Giá trị tài sản ròng thực tế (True Net Worth)** cần được chuẩn hóa tập trung qua một UseCase thống nhất: $\text{True Net Worth} = \text{Ví khả dụng} + \text{Vốn Deal đang đầu tư/cho vay} - \text{Tổng nợ phải trả}$.<br>⚠️ **Rủi ro:** Báo cáo chưa phản ánh trọn vẹn bức tranh tài sản tổng thể đối với người dùng có nhiều hoạt động đầu tư và nợ. |
| **06** | **Chuẩn hóa Trải nghiệm Đồng bộ giữa 3 Giao diện (Prism / Modern / Classic)** | **Giao diện (UI/UX)** | 🟠 **P1 (High)** | • Nhiều màn hình mới (TransferMoneyScreen, Deal Tracking, Reporting 2.0 Foundation) tập trung tối ưu ở giao diện Prism Glass và Modern Luxury, trong khi Classic Theme có nguy cơ bị lệch trải nghiệm hoặc thiếu các tùy chỉnh chi tiết.<br>⚠️ **Rủi ro:** Người dùng chọn phong cách Classic không được hưởng đầy đủ các tiện ích tương tác tài chính hiện đại. |
| **07** | **Module hóa và Tách nhỏ các God Files trong Presentation Layer** | **Nợ Kỹ Thuật (Tech Debt)** | 🟡 **P2 (Medium)** | • Một số file giao diện đang có dung lượng quá lớn: `PrismReportsScreen.kt` (~3.600 dòng), `PrismHomeScreen.kt` (~2.800 dòng), `ReportsViewModel.kt` (~900 dòng).<br>⚠️ **Rủi ro:** Tăng thời gian biên dịch (build time), khó bảo trì, nguy cơ merge conflict cao khi nhiều agent cùng thao tác. |
| **08** | **Triệt tiêu toàn bộ Compiler Warnings (Deprecation & Opt-in Annotations)** | **Nợ Kỹ Thuật (Tech Debt)** | 🟡 **P2 (Medium)** | • Cảnh báo `@Deprecated` với `hiltViewModel()` (chuyển sang package `androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel`).<br>• Cảnh báo icon `Icons.Filled.TrendingUp` cần đổi sang `Icons.AutoMirrored.Filled.TrendingUp`.<br>• Cảnh báo `-Xannotation-default-target=param-property` trong Kotlin 2.x.<br>⚠️ **Rủi ro:** Nguy cơ phát sinh lỗi không tương thích khi nâng cấp Kotlin/Compose lên các phiên bản mới tiếp theo. |

---

## III. KẾ HOẠCH HÀNH ĐỘNG ĐỀ XUẤT TUẦN TỰ (SEQUENTIAL ACTION PLAN)

Để đảm bảo an toàn tuyệt đối cho hệ sinh thái FinLux theo đúng kim chỉ nam của `docs/audit-2026-09-fix-roadmap/`: **"Không thêm feature lớn trước khi hoàn tất P0 Financial Integrity & Backend Contract"**, lộ trình được đề xuất thực thi tuần tự theo 4 giai đoạn cụ thể:

### 🔹 GIAI ĐOẠN 1: KHÓA CHẶT BẢO MẬT & HỢP NHẤT CONTRACT BACKEND (P0 - BLOCKERS)
> **Mục tiêu:** Đồng bộ 100% hợp đồng dữ liệu giữa Android, Firestore Rules, Storage Rules và Cloud Functions, loại bỏ mọi nguy cơ bị từ chối quyền hoặc lỗ hổng bảo mật.

1. **Bước 1.1: Cập nhật & Kiểm thử `firestore.rules`**:
   - Thêm schema rule cho subcollection `users/{uid}/deals/{dealId}` (xác thực `title`, `capitalOutlay`, `status`, `flowType`, `currentValue`).
   - Cập nhật hàm `validTransaction` chấp nhận các trường: `dealId`, `dealFlowType`, `correlationId`.
   - Cập nhật subcollection `budgets` chấp nhận: `periodKey`, `periodStart`, `periodEndExclusive`, `periodBasis`.
2. **Bước 1.2: Cập nhật `functions/src/index.ts`**:
   - Chuyển đường dẫn đọc config sang `users/${uid}/financialPreferences/salaryCycle`.
   - Đồng bộ trường `paydayDay`, `paydayRuleType`, `budgetPeriodBasis`, `financeTimeZone`.
3. **Bước 1.3: Tái cấu trúc Tất toán Lỗ Deal (Xóa bỏ `DEAL_SETTLEMENT`)**:
   - Thay vì tạo transaction ví ảo `DEAL_SETTLEMENT`, chuyển sang ghi nhận trạng thái Deal `CLOSED` với `lossAmount`, lưu lịch sử dòng vốn trực tiếp trong bản ghi Deal mà không làm ô nhiễm bảng kê giao dịch ví.
4. **Bước 1.4: Hardening AndroidManifest & Storage**:
   - Đặt `android:exported="false"` cho `SalaryCycleReceiver`.
   - Phân quyền owner-only tuyệt đối cho `storage.rules` (`avatars/{uid}.jpg` và `receipts/{uid}/{txId}.jpg`).

---

### 🔹 GIAI ĐOẠN 2: HOÀN THIỆN BÁO CÁO CHUYÊN SÂU & ĐỐI CHIẾU TÀI SẢN RÒNG (P1)
> **Mục tiêu:** Nâng cấp Báo cáo 2.0 thành trung tâm điều hành tài chính minh bạch cho người dùng.

1. **Bước 2.1: Hoàn thiện Drill-down 4 Cấp Độ trong Báo Cáo**:
   - `Cấp 1: Tổng quan kỳ` ➔ `Cấp 2: Báo cáo theo Ví` ➔ `Cấp 3: Phân rã Danh mục` ➔ `Cấp 4: Danh sách Giao dịch cấu thành`.
   - Tối ưu hóa việc lọc kỳ (Tuần, Tháng, Chu kỳ lương, Năm, Tùy chọn).
2. **Bước 2.2: Tích hợp Động Cơ Tài Sản Ròng Thực Tế (`TrueNetWorthEngine`)**:
   - Đưa công thức tính Net Worth ra một UseCase độc lập (`GetTrueNetWorthUseCase`) dùng chung cho cả màn hình Trang chủ và màn hình Báo cáo, đảm bảo con số hiển thị giữa các tab luôn trùng khớp 100%.

---

### 🔹 GIAI ĐOẠN 3: ĐỒNG BỘ TRẢI NGHIỆM 3 PHONG CÁCH GIAO DIỆN (P1)
> **Mục tiêu:** Đảm bảo trải nghiệm nhất quán, không drift giữa Classic, Modern Luxury và Prism Liquid Glass.

1. **Bước 3.1: Rà soát Classic Theme**:
   - Đồng bộ màn hình Chuyển tiền toàn màn hình (`TransferMoneyScreen`) và Quản lý Deal vào menu của Classic Theme.
   - Kiểm tra độ tương phản màu sắc Light/Dark Mode theo hệ thống tokens `LocalFinluxTokens`.
2. **Bước 3.2: Chuẩn hóa Modal & BottomSheets**:
   - Đảm bảo toàn bộ Dialog/Sheet sử dụng thống nhất `FinluxStyleBackdrop` và màu nền kính không bị lỗi đè trắng.

---

### 🔹 GIAI ĐOẠN 4: TÁI CẤU TRÚC CODEBASE & DỌN DẸP WARNINGS (P2)
> **Mục tiêu:** Giảm kích thước file, nâng cao khả năng bảo trì và đón đầu các phiên bản Kotlin/Compose mới.

1. **Bước 4.1: Chia nhỏ God Files**:
   - Tách `PrismReportsScreen.kt` thành các component độc lập: `PrismDailyStatementSection.kt`, `PrismWalletStatementSection.kt`, `PrismExpenseCategorySection.kt`.
   - Tách `PrismHomeScreen.kt` thành các sub-components: `PrismHomeHeroSection.kt`, `PrismQuickActionSection.kt`, `PrismRecentListSection.kt`.
2. **Bước 4.2: Khắc phục triệt để Compiler Warnings**:
   - Cập nhật toàn bộ import `hiltViewModel()` về package mới `androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel`.
   - Chuyển `Icons.Filled.TrendingUp` sang `Icons.AutoMirrored.Filled.TrendingUp`.
   - Tinh chỉnh compiler flags trong `app/build.gradle.kts`.

---

## IV. TRẠNG THÁI HIỆN TẠI & HÀNH ĐỘNG TIẾP THEO

- Toàn bộ thay đổi về sửa lỗi thông báo v1.22.1 đã được nạp thành công vào nhánh `main` và push lên GitHub repository.
- File tài liệu này đã được lưu vào hệ thống tại: [docs/PENDING_BACKLOG_PLAN.md](file:///d:/Sources/FinLux/docs/PENDING_BACKLOG_PLAN.md).
- **Tuân thủ chỉ thị của User:** AI Agent đã **DỪNG MỌI HOẠT ĐỘNG VIẾT CODE MỚI** và chờ ý kiến phê duyệt của User trước khi bước vào triển khai bất kỳ giai đoạn nào tiếp theo.
