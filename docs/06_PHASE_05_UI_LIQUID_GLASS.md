# PHASE 05 — LIQUID GLASS DESIGN SYSTEM (06_PHASE_05_UI_LIQUID_GLASS.md)

## 1. Objective (Mục tiêu)
Phát triển hệ thống thiết kế độc quyền **Liquid Glass (iOS 26 Visual Style Layer)** cho ứng dụng FinLux, cung cấp bộ linh kiện UI dùng chung (Reusable Glass Components), hỗ trợ Shader/RenderEffect linh hoạt, hiệu ứng chuyển cảnh tự nhiên và thích ứng hoàn hảo với Theme Sáng/Tối.

## 2. Core Visual Tokens & Color Palette
- **Brand Colors**: Primary Blue `#3478F6`, Purple `#7758F6`, Cyan `#47C8FF`, Accent Coral `#FF6B6B`.
- **Glass Overlays**:
  - Light Theme: Kính sáng trong suốt (`Color.White.copy(alpha = 0.70f)`), viền mờ trắng sáng (`#FFFFFF`).
  - Dark Theme: Kính tối nồng độ cao (`Color(0xFF0F172A).copy(alpha = 0.85f)`), viền rực sáng nhẹ (rim-light glow).

## 3. Reusable Glass Components (`core/designsystem`)
- `LiquidGlassSurface`: Container hỗ trợ hiệu ứng RenderEffect Blur thời gian thực trên Android 12+ (API 31+) với fallback overlay trên Android 8-11.
- `GlassCard`: Thẻ hiển thị kính mờ viền bo cong (Rounded corners 16-24dp) tích hợp hiệu ứng rim-light.
- `GlassTopBar` / `GlassBottomNav`: Thanh công cụ và thanh điều hướng kính mờ tự động thích ứng System Insets (Edge-to-Edge).

## 4. Motion, Animation & Haptics
- **Transition Animations**: Sử dụng Spring Animation có giảm chấn (stiffness & damping ratio), kết hợp Fade & Scale depth khi chuyển tab.
- **Swipe Gestures**: Thao tác vuốt ngang bám ngón tay giữa các route chính (Trang chủ ↔ Chi tiêu ↔ Báo cáo ↔ Ngân sách ↔ Hồ sơ).
- **Haptic Feedback**: Rung phản hồi nhẹ khi chạm nút bấm chính hoặc thay đổi tab.

## 5. Exit Criteria & DoD
- [x] 100% màn hình sử dụng Design System chung từ `core/designsystem`.
- [x] Tỷ lệ khung hình và độ tương phản chữ đạt chuẩn WCAG AA trên nền kính mờ.
- [x] Hiệu năng duy trì 50-60 FPS trên thiết bị tầm trung.
