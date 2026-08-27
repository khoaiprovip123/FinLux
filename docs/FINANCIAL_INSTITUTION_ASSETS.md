# Financial Institution Logo Assets

## Phạm vi hiện tại

- Snapshot ngày 2026-08-27 gồm 65 tổ chức từ `GET https://api.vietqr.io/v2/banks`.
- Danh mục VietQR bao gồm ngân hàng nội địa, ngân hàng số, chi nhánh ngân hàng nước ngoài,
  công ty tài chính và các ví có BIN VietQR (MoMo, Viettel Money, VNPT Money).
- Nhóm ví bổ sung trong UI: ZaloPay, VNPAY, ShopeePay, Payoo, 9Pay, Foxpay, VTC Pay,
  Apple Pay và PayPal.

## Nguồn và cách cập nhật

- API/danh sách/logo ngân hàng: VietQR API — `https://api.vietqr.io/v2/banks`.
- Snapshot phản hồi đầy đủ: `docs/data/vietqr-financial-institutions.json`.
- Script tải và kiểm tra ảnh: `tools/sync-financial-institution-icons.ps1`.
- Logo Payoo, 9Pay, Foxpay và VTC Pay lấy từ trang ứng dụng Google Play của đúng nhà phát hành;
  các logo ví còn lại tái sử dụng vector thương hiệu đã có trong design system.

Chạy lại từ thư mục gốc dự án:

```powershell
.\tools\sync-financial-institution-icons.ps1
```

Script chỉ ghi các file có tiền tố `ic_vietqr_` và cập nhật manifest; sau khi API thêm/xóa tổ chức,
cần đồng bộ tương ứng `VietQrBankCatalog.kt`, chạy unit test và build APK trước khi phát hành.

Logo và tên thương hiệu thuộc quyền sở hữu của các tổ chức tương ứng; FinLux chỉ sử dụng để
giúp người dùng nhận diện tài khoản do chính họ quản lý.
