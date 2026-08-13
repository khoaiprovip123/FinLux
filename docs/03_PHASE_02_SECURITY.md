# PHASE 02 — SECURITY & ACCESS CONTROL (03_PHASE_02_SECURITY.md)

## 1. Objective (Mục tiêu)
Thiết lập toàn bộ cơ chế bảo mật cho ứng dụng FinLux, bao gồm cấu hình Firestore Security Rules, Firebase Storage Rules, mã hóa dữ liệu cục bộ và kiểm tra phân quyền truy cập tuyệt đối theo phân vùng dữ liệu người dùng (`users/{uid}`).

## 2. Business & Technical Security Goals
- **Business Goal**: Bảo vệ dữ liệu tài chính cá nhân của người dùng; đảm bảo không người dùng nào có thể đọc hoặc can thiệp dữ liệu của người dùng khác.
- **Technical Goal**: Áp dụng triệt để nguyên tắc **Least Privilege** trong Security Rules; chặn mọi truy cập chưa xác thực; kiểm tra dữ liệu đầu vào (Input Validation) tại cả Client và Server.

## 3. Firestore Security Rules Specifications
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // User root document
    match /users/{uid} {
      allow read, write: if request.auth != null && request.auth.uid == uid;

      // All financial subcollections (wallets, transactions, budgets, categories)
      match /{subcollection}/{docId} {
        allow read, write: if request.auth != null && request.auth.uid == uid;
      }
    }
  }
}
```

## 4. Firebase Storage Security Rules
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /avatars/{uid}.jpg {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == uid
                   && request.resource.size < 5 * 1024 * 1024
                   && request.resource.contentType.matches('image/.*');
    }
    match /receipts/{uid}/{transactionId}.jpg {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
  }
}
```

## 5. Security & Validation Rules Matrix
- **BR-01**: Mật khẩu người dùng tối thiểu 8 ký tự, chứa cả chữ cái và chữ số.
- **BR-03**: File ảnh avatar tải lên không vượt quá 5MB, tự động nén nắn về ~500KB tại thiết bị trước khi upload.
- **Secret Protection**: Không commit `google-services.json` thật hay chìa khóa private key lên Git (chỉ dùng `.example`).

## 6. Verification & Test Plan
- **Firebase Emulator Security Test**: Chạy Firebase Emulator Suite để verify các kịch bản try-read/write document của UID khác -> Bắt buộc bị từ chối (`permission-denied`).
- **Input Sanitization Test**: Kiểm tra chuỗi ghi chú giao dịch chống injection.

## 7. Exit Criteria & DoD
- [x] Security Rules cho Firestore và Storage được review và lưu trong repo (`firestore.rules`, `storage.rules`).
- [x] File cấu hình mẫu `.example` được thiết lập và ghi vào `.gitignore`.
