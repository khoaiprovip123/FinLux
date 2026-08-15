---
name: callstack-liquid-glass
description: Hướng dẫn và quy chuẩn thiết kế Liquid Glass (iOS 26 / VisionOS / @callstack/liquid-glass) cho Jetpack Compose và Android. Bao gồm các chế độ effect (clear, regular), tương tác đàn hồi spring physics, viền lăng kính tán sắc (chromatic prism rim), bóng đổ môi trường (ambient glow) và tự động tương thích Light/Dark mode.
---

# Callstack Liquid Glass Design System (Jetpack Compose)

Quy chuẩn triển khai hiệu ứng Kính Lỏng (Liquid Glass) lấy cảm hứng từ thư viện `@callstack/liquid-glass` (Apple iOS 26 / VisionOS `UIGlassEffect` & `UIGlassContainerEffect`) trên nền tảng Jetpack Compose Android.

---

## 1. Nguyên Lý Cốt Lõi (Core Principles)

| Nguyên lý | Quy chuẩn kỹ thuật trong Jetpack Compose |
|---|---|
| **Effect Modes (`clear` vs `regular`)** | **`clear`:** Kính trong suốt cao (`alpha = 0.40f..0.60f`), viền sáng tán sắc mảnh `1.2.dp`, cho phép vệt màu nền (`LiquidAura`) xuyên thấu rực rỡ.<br/>**`regular`:** Kính mờ tiêu chuẩn (`alpha = 0.65f..0.85f`), độ khuếch tán ánh sáng mềm mại. |
| **Interactive Spring Physics** | Bắt buộc phản hồi xúc giác + hiệu ứng co giãn đàn hồi khi chạm: `scale` co lại `0.975f` với `spring(stiffness = 650f, dampingRatio = 0.72f)`. |
| **Chromatic Prism Rim (Viền lăng kính tán sắc)** | Viền gradient vát cạnh `1.2.dp` theo góc nghiêng quang học (`White ➔ Cyan ➔ Purple ➔ White`) mô phỏng sự khúc xạ ánh sáng qua mép kính vát. |
| **Dynamic Ambient Glow (Bóng đổ môi trường phát quang)** | Bóng đổ không dùng màu đen đặc thông thường, mà sử dụng màu tint của chính thẻ kính hoặc màu của nền phía dưới (`tint.copy(alpha = 0.14f..0.35f)`). |
| **Text Legibility Zero-Occlusion** | Tuyệt đối **KHÔNG** vẽ các vệt canvas (oval, circle glow) đè lên vùng văn bản. Toàn bộ nội dung chữ và icon phải đạt độ tương phản tuyệt đối trên nền kính. |
| **Adaptive Light/Dark Theme** | **Light Mode:** Kính pha lê trong suốt ánh xanh bạc (`White/Cyan/Frost`).<br/>**Dark Mode:** Kính hắc diện thạch obsidian trong suốt ánh neon (`Slate/Indigo/Cyan`). |

---

## 2. API Chuẩn Trong Jetpack Compose

```kotlin
enum class LiquidGlassMode {
    CLEAR,    // Trong suốt cao, thấy rõ aura nền
    REGULAR,  // Kính mờ tiêu chuẩn
    NONE,     // Trong suốt hoàn toàn (dematerialized)
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    mode: LiquidGlassMode = LiquidGlassMode.CLEAR,
    tint: Color = MaterialTheme.colorScheme.primary,
    interactive: Boolean = true,
    cornerRadius: Dp = 24.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
)
```

---

## 3. Quy Tắc Tránh Lỗi (Critical Gotchas)

1. **Không dùng `finluxBackgroundBlur` trực tiếp trên Container chứa Text/Icon:** `RenderEffect.createBlurEffect` trên Android 12+ làm mờ toàn bộ composable con. Chỉ blur layer nền độc lập phía dưới.
2. **SwipeToDismiss Background Isolation:** Chỉ render `backgroundContent` khi `dismissDirection != Settled`, tránh việc icon/chữ xóa bị lộ xuyên qua thẻ kính trong suốt khi ở trạng thái nghỉ.
3. **Floating Capsule Dock:** Dock menu nổi lơ lửng bo góc `34..36dp`, cách đáy và 2 biên `14..16dp`, không dán sát mép màn hình.
