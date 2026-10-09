# FinLux Web — Fullstack Personal Finance & Liquid Glass Atelier 💎

[![Live Web](https://img.shields.io/badge/Live%20Web-finlux--0wxc.onrender.com-00D1B2.svg?style=flat&logo=render)](https://finlux-0wxc.onrender.com/)
[![Next.js](https://img.shields.io/badge/Next.js-16.4.0-black.svg?logo=next.js)](https://nextjs.org/)
[![React](https://img.shields.io/badge/React-19.3.0-61DAFB.svg?logo=react)](https://react.dev/)
[![TailwindCSS](https://img.shields.io/badge/TailwindCSS-v4-38B2AC.svg?logo=tailwind-css)](https://tailwindcss.com/)
[![Firebase](https://img.shields.io/badge/Firebase-v13-FFCA28.svg?logo=firebase)](https://firebase.google.com/)

Ứng dụng web toàn diện của hệ sinh thái **FinLux**, đồng bộ dữ liệu thời gian thực (Real-time Cloud Sync) với app Android FinLux Native.

🌐 **Trải nghiệm ngay tại:** [https://finlux-0wxc.onrender.com/](https://finlux-0wxc.onrender.com/)

---

## ✨ Điểm nhấn Tính năng (Features)

1. **Atelier Landing Page**:
   - Vòng tròn quỹ đạo 16 điểm (`fx-orbit-glide`) quay quanh Logo FinLux trung tâm, thẻ luôn nằm ngang thẳng thớm chuẩn mực, rê chuột sáng lấp lánh như ngôi sao (`--star-glow`).
   - Header cố định (`position: fixed`) với thanh tiến trình cuộn trang (`Scroll Progress Bar`) đổi màu quang phổ tự động.
   - Slogan tinh hoa, trích dẫn tài chính danh giá thay đổi mượt mà.
2. **Quản lý Thu Chi & Kế toán Kép**:
   - Nhập liệu số tiền thông minh với auto-formatting hàng nghìn và inline `₫`.
   - Phân loại danh mục thu/chi chuẩn mực, chống tính trùng chi phí (`Zero Double-Counting`).
3. **Quản lý Đa ví & Ngân hàng**:
   - Hỗ trợ đầy đủ ngân hàng và ví điện tử phổ biến tại Việt Nam (VietQR, MoMo, ZaloPay, ViettelMoney...).
4. **Ngân sách Chu kỳ Lương (Salary Cycle Budget)**:
   - Theo dõi chi tiêu theo từng kỳ nhận lương thực tế, cảnh báo hạn mức thông minh.
5. **Dòng tiền Tự do (Free Cash Flow - FCF)** & Báo cáo trực quan (Donut / Bar Chart).
6. **Vòng quay Tích lũy (Saving Spin)** & Mục tiêu Tài chính (Financial Goals).
7. **Đồng bộ Real-time**:
   - Kết nối Firestore Cloud, dữ liệu thay đổi trên điện thoại hoặc web được cập nhật tức thì.

---

## 🛠️ Công nghệ Sử dụng (Tech Stack)

- **Framework:** Next.js 16 (App Router + Turbopack)
- **UI Library:** React 19 + Lucide Icons + Canvas Confetti
- **Styling:** Tailwind CSS v4 + Vanilla Liquid Glass Token System
- **Database & Auth:** Firebase Firestore + Firebase Authentication (Google Sign-In)
- **Deployment:** Render Web Service (Continuous Deployment từ nhánh `main`)

---

## 🚀 Khởi chạy Cục bộ (Local Development)

```bash
# Di chuyển vào thư mục web
cd web

# Cài đặt thư viện
npm install

# Chạy dev server
npm run dev
```

Mở trình duyệt tại [http://localhost:3000](http://localhost:3000) để trải nghiệm.

---

## 📦 Build & Production

```bash
npm run build
npm start
```

---

## 👥 Nhóm Phát triển (Developers)

- **Lead Developer:** [khoaiprovip123](https://github.com/khoaiprovip123)
- **Co-Developer:** [Long Louis (thanhlongts2k)](https://github.com/thanhlongts2k)
