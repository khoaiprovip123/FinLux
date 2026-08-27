package com.finlux.app.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.finlux.app.core.designsystem.FinluxStyleBackdrop

/**
 * FinluxScreenScaffold - Khung chuan Slot API duy nhat cho moi man hinh FinLux.
 *
 * Nguyen tac Insets: Scaffold dung contentWindowInsets = WindowInsets(0) de tra PaddingValues sach.
 * TopBar (GlassTopBar / FinluxScreenHeader) tu goi .statusBarsPadding() ben trong.
 *
 * Backdrop:
 *   showBackdrop = true (mac dinh): Bao gom FinluxStyleBackdrop dong theo Theme.
 *   showBackdrop = false: Dung cho man hinh su dung nen dac tu containerColor (vd: Prism Solid).
 *
 * @param modifier Modifier cho Box ngoai cung
 * @param topBar Slot TopBar
 * @param bottomBar Slot BottomBar
 * @param floatingActionButton Slot FAB
 * @param fabPosition Vi tri FAB
 * @param snackbarHost Slot Snackbar
 * @param showBackdrop Bat/tat FinluxStyleBackdrop nen kinh
 * @param containerColor Mau nen Scaffold noi bo. Chi co hieu luc khi showBackdrop = false.
 * @param content Lambda noi dung chinh nhan PaddingValues sach tu Scaffold
 */
@Composable
fun FinluxScreenScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    fabPosition: FabPosition = FabPosition.End,
    snackbarHost: @Composable () -> Unit = {},
    showBackdrop: Boolean = true,
    containerColor: Color = Color.Transparent,
    content: @Composable (PaddingValues) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (showBackdrop) {
            FinluxStyleBackdrop(modifier = Modifier.fillMaxSize())
        }
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = topBar,
            bottomBar = bottomBar,
            floatingActionButton = floatingActionButton,
            floatingActionButtonPosition = fabPosition,
            snackbarHost = snackbarHost,
            containerColor = if (showBackdrop) Color.Transparent else containerColor,
            contentColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            content = content,
        )
    }
}