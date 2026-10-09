# HƯỚNG DẪN DEPLOY FINLUX WEB LÊN RENDER (DASHBOARD.RENDER.COM)

FinLux Web là ứng dụng Fullstack Next.js (App Router + Server API + Liquid Glass UI) kết nối trực tiếp Firebase của hệ sinh thái FinLux.

---

## 🚀 Cách 1: Deploy Bằng Render Blueprint (Nhanh nhất - 1 Click)

Dự án đã có sẵn file cấu hình `render.yaml` tại thư mục gốc.

1. Đăng nhập vào [dashboard.render.com](https://dashboard.render.com/).
2. Nhấn nút **New +** ở góc trên bên phải → Chọn **Blueprint**.
3. Chọn repository GitHub **FinLux** (hoặc repo fork của bạn).
4. Render sẽ tự động đọc file `render.yaml` và nhận diện:
   - **Service Name:** `finlux-web`
   - **Root Directory:** `web`
   - **Build Command:** `npm install && npm run build`
   - **Start Command:** `npm start`
   - **Health Check Path:** `/api/health`
   - **Environment Variables:** Đã điền sẵn các biến Firebase client.
5. Nhấn **Apply**. Quá trình build và deploy sẽ tự động hoàn tất trong ~2 phút.
6. Render đã cấp tên miền hoạt động chính thức: `https://finlux-0wxc.onrender.com/`.

---

## 🛠️ Cách 2: Deploy Thủ Công (Manual Web Service)

Nếu bạn muốn tạo thủ công trên giao diện Render Dashboard:

1. Đăng nhập vào [dashboard.render.com](https://dashboard.render.com/).
2. Nhấn nút **New +** → Chọn **Web Service**.
3. Chọn **Build and deploy from a Git repository** → Nhấn **Next**.
4. Chọn repo GitHub chứa mã nguồn FinLux.
5. Điền các trường thông số cấu hình như sau:
   - **Name:** `finlux-web`
   - **Region:** `Singapore (Southeast Asia)` hoặc `Oregon (US West)`
   - **Branch:** `main` (hoặc branch bạn đang push lên)
   - **Root Directory:** `web` *(Rất quan trọng vì code Next.js nằm trong thư mục `web/`)*
   - **Runtime:** `Node`
   - **Build Command:** `npm install && npm run build`
   - **Start Command:** `npm start`
   - **Instance Type:** `Free`
6. Mở rộng mục **Advanced**:
   - **Health Check Path:** `/api/health`
7. Trong mục **Environment Variables**, thêm các biến sau:
   - `NODE_ENV` = `production`
   - `PORT` = `10000`
   - `NEXT_PUBLIC_FIREBASE_PROJECT_ID` = `finlux-d0297`
   - `NEXT_PUBLIC_FIREBASE_API_KEY` = `AIzaSyAyJF9HOzZwIZ6_MaFN4ISeWISFURuiJJE`
   - `NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN` = `finlux-d0297.firebaseapp.com`
   - `NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET` = `finlux-d0297.firebasestorage.app`
   - `NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID` = `927751753962`
   - `NEXT_PUBLIC_FIREBASE_APP_ID` = `1:927751753962:web:7f81a8b92c43eef6`
8. Nhấn **Create Web Service**.

---

## 🔍 Kiểm tra sau khi Deploy thành công

1. **Trang chủ:** Truy cập `https://finlux-0wxc.onrender.com/` để mở Dashboard Liquid Glass.
2. **Kiểm tra Health Check API:** Truy cập `https://finlux-0wxc.onrender.com/api/health` → nhận JSON:
   ```json
   {
     "status": "ok",
     "service": "finlux-web",
     "version": "1.0.0",
     "timestamp": "...",
     "uptime": 12.34
   }
   ```
3. **Kiểm tra API Thống kê:** Truy cập `https://<tên-service>.onrender.com/api/summary`.

---

## 📌 Lưu ý về gói Free trên Render
- Trên gói Free của Render, web service sẽ tự động ngủ (spin down) sau 15 phút không có lượt truy cập. Khi có lượt truy cập mới, Render mất khoảng 30 - 50 giây để khởi động lại máy chủ (cold start).
- Sau khi khởi động xong, giao diện sẽ phản hồi tức thì và mượt mà.
