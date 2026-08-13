# PHASE 06 — AUTHENTICATION & SESSION MODULE (07_PHASE_06_AUTH.md)

## 1. Objective (Mục tiêu)
Hoàn thiện hệ thống xác thực người dùng (Authentication & Identity), bao gồm Đăng ký, Đăng nhập Email/Mật khẩu, Đăng nhập qua Mạng xã hội (Google, Apple, Facebook OAuth), Quên mật khẩu, Quản lý phiên làm việc (Session Persistence) và Khởi tạo dữ liệu người dùng mới (User Seed Data).

## 2. Scope & Screenshots Reference Alignment
- **Giao diện 3D Auth**: Bám sát 100% bản vẽ 3D Glass reference (Header gradient + minh họa Ví tiền / Hồ sơ 3D, Tab bar Đăng nhập / Đăng ký mượt mà).
- **Form Features**:
  - Ô nhập Họ tên, Email, Số điện thoại, Mật khẩu, Xác nhận mật khẩu.
  - Thanh đo độ mạnh mật khẩu 4 cấp độ (Yếu / Trung bình / Mạnh / Rất mạnh).
  - Checkbox "Ghi nhớ đăng nhập" & Tích chọn đồng ý "Điều khoản sử dụng & Chính sách bảo mật".
  - Bộ 3 Social Cards (Google, Apple, Facebook) chuẩn vector icons.

## 3. User Seed Data Setup (UC-01)
Khi người dùng mới đăng ký thành công, hệ thống tự động khởi tạo document `users/{uid}` kèm theo:
- Ví mặc định: "Tiền mặt" (`balance = 0 VND`, `type = CASH`).
- Bộ danh mục mặc định: Ăn uống, Di chuyển, Lương, Freelance, Mục khác...

## 4. Session State Machine
```text
                  ┌───────────────────────────────┐
                  │       SplashScreen Launch     │
                  └───────────────┬───────────────┘
                                  │
                       Check Firebase Session
                       ───────────┬───────────
                                  │
              ┌───────────────────┴───────────────────┐
              ▼                                       ▼
    [Session Active]                        [No Active Session]
    Navigate to Home                        Navigate to Login
```

## 5. Exit Criteria & DoD
- [x] Đăng nhập & Đăng ký hoạt động mượt mà cả ở Firebase Real Mode và Demo Mode.
- [x] Tạo user mới khởi tạo chính xác ví & danh mục mặc định.
- [x] Đổi thông tin cá nhân (Tên người dùng, Avatar) đồng bộ tức thì toàn app.
