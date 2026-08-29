package com.finlux.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.finlux.app.core.designsystem.FinluxStyleBackdrop
import com.finlux.app.core.designsystem.LocalAppUiStyle
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.AppUiStyle

/**
 * FinluxScreenScaffold - Khung chuẩn Slot API duy nhất với cơ chế "Zero-Config Theme Inheritance".
 *
 * **Nguyên tắc Insets:**
 * - Scaffold sử dụng `contentWindowInsets = WindowInsets(0)` để trả PaddingValues sạch.
 * - TopBar (GlassTopBar / FinluxScreenHeader) tự gọi `.statusBarsPadding()` bên trong.
 *
 * **Zero-Config Backdrop & Theme Inheritance:**
 * - Tự động nhận diện `LocalAppUiStyle`:
 *   + `AppUiStyle.PRISM`: Tự động áp dụng nền `tokens.background` (Dark `#0E1118` / Light `#F6F8FC`).
 *   + `CLASSIC_LIQUID` / `MODERN_LUXURY`: Tự động kích hoạt `FinluxStyleBackdrop` nền kính Liquid Glass động.
 * - Box ngoài cùng luôn được gán `.background(tokens.background)` bảo đảm 100% không bao giờ để lộ nền Window trắng ở Dark Mode.
 * - Tự động inject `LocalContentColor provides tokens.textPrimary` xuyên suốt toàn bộ các slots (`topBar`, `bottomBar`, `content`).
 *
 * @param modifier Modifier cho Box ngoài cùng
 * @param topBar Slot TopBar
 * @param bottomBar Slot BottomBar
 * @param floatingActionButton Slot FAB
 * @param fabPosition Vị trí FAB
 * @param snackbarHost Slot Snackbar
 * @param showBackdrop Bật/tắt FinluxStyleBackdrop (null = tự động nhận diện theo AppUiStyle)
 * @param containerColor Màu nền Scaffold nội bộ (null = tự động xác định theo backdrop)
 * @param content Lambda nội dung chính nhận PaddingValues sạch từ Scaffold
 */
@Composable
fun FinluxScreenScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    fabPosition: FabPosition = FabPosition.End,
    snackbarHost: @Composable () -> Unit = {},
    showBackdrop: Boolean? = null,
    containerColor: Color? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val uiStyle = LocalAppUiStyle.current

    val shouldShowBackdrop = showBackdrop ?: (uiStyle != AppUiStyle.PRISM)
    val resolvedContainerColor = containerColor ?: if (shouldShowBackdrop) Color.Transparent else tokens.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(tokens.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        if (shouldShowBackdrop) {
            FinluxStyleBackdrop(modifier = Modifier.fillMaxSize())
        }
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CompositionLocalProvider(
                    LocalContentColor provides tokens.textPrimary,
                ) {
                    topBar()
                }
            },
            bottomBar = {
                CompositionLocalProvider(
                    LocalContentColor provides tokens.textPrimary,
                ) {
                    bottomBar()
                }
            },
            floatingActionButton = floatingActionButton,
            floatingActionButtonPosition = fabPosition,
            snackbarHost = snackbarHost,
            containerColor = resolvedContainerColor,
            contentColor = tokens.textPrimary,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
        ) { paddingValues ->
            CompositionLocalProvider(
                LocalContentColor provides tokens.textPrimary,
            ) {
                content(paddingValues)
            }
        }
    }
}