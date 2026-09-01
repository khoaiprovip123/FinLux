package com.finlux.app.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.finlux.app.R
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens

/**
 * Standard Liquid Glass Capsule Toast / Snackbar (FinLux Spec — Floating Pill)
 *
 * Tinh chỉnh 1:1 chuẩn xác theo giao diện Native Floating Capsule cao cấp:
 * - Logo Finlux chính thức (finlux_logo) bo góc Squircle thanh lịch.
 * - Viên nang Capsule tròn trịa (CircleShape/32dp), nền trắng tinh tế / dark slate, bóng đổ khuếch tán mềm.
 * - Text 14sp Medium sắc nét, căn giữa hoàn hảo.
 * - Nút hành động Hoàn tác (Undo) gọn gàng bên phải.
 */
@Composable
fun FinluxGlassSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    iconRes: Int = R.drawable.finlux_logo,
) {
    val tokens = LocalFinluxTokens.current
    val cardShape = RoundedCornerShape(16.dp)

    Surface(
        shape = cardShape,
        color = if (tokens.isDark) Color(0xFF1E222D) else Color(0xFFFFFFFF),
        border = BorderStroke(
            width = 0.8.dp,
            color = if (tokens.isDark) Color(0x28FFFFFF) else Color(0x14000000),
        ),
        shadowElevation = 2.dp,
        modifier = modifier
            .wrapContentWidth()
            .widthIn(min = 140.dp, max = 340.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // App Logo Finlux
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp)),
            )

            // Message text
            Text(
                text = snackbarData.visuals.message,
                style = FinluxTextStyles.Body.copy(
                    fontSize = androidx.compose.ui.unit.TextUnit.Unspecified,
                ),
                color = if (tokens.isDark) Color(0xFFF3F4F6) else Color(0xFF1F2937),
                fontWeight = FontWeight.Normal,
                modifier = Modifier.weight(1f, fill = false),
            )

            // Optional Action (Undo / Hoàn tác)
            snackbarData.visuals.actionLabel?.let { actionLabel ->
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = tokens.primary.copy(alpha = if (tokens.isDark) 0.20f else 0.12f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            snackbarData.performAction()
                        },
                ) {
                    Text(
                        text = actionLabel,
                        style = FinluxTextStyles.Caption,
                        color = tokens.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

/**
 * FinluxSnackbarHost — Host quản lý và hiển thị FinluxGlassSnackbar tự động né Safe Insets & BottomBar.
 *
 * @param hostState Quản lý trạng thái SnackbarHostState
 * @param hasBottomBar Nếu true (ở 4 tab chính có BottomBar), tự động cộng bottomBarClearance (96dp) + 16dp để nổi phía trên thanh BottomBar.
 */
@Composable
fun FinluxSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    hasBottomBar: Boolean = false,
) {
    val tokens = LocalFinluxTokens.current
    val bottomClearance = if (hasBottomBar) tokens.spacing.bottomBarClearance + 16.dp else 28.dp

    SnackbarHost(
        hostState = hostState,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = bottomClearance, start = 20.dp, end = 20.dp),
        snackbar = { data ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                FinluxGlassSnackbar(snackbarData = data)
            }
        },
    )
}

/**
 * Standard Empty State (FinLux Prism Spec 23/UI-FIX-11 & 24)
 */
@Composable
fun FinluxEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector = Icons.Default.Inbox,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    val tokens = LocalFinluxTokens.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = tokens.primary.copy(alpha = if (tokens.isDark) 0.16f else 0.10f),
            modifier = Modifier.size(72.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tokens.primary,
                    modifier = Modifier.size(36.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = FinluxTextStyles.SectionTitle,
            color = tokens.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        if (description != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = FinluxTextStyles.Body,
                color = tokens.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onActionClick,
                shape = RoundedCornerShape(tokens.radius.smallChip),
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.primary,
                    contentColor = tokens.onHero,
                ),
            ) {
                Text(actionLabel, style = FinluxTextStyles.Caption, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Standard Error State (FinLux Prism Spec 23/UI-FIX-11 & 24)
 */
@Composable
fun FinluxErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    val tokens = LocalFinluxTokens.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = FinluxColors.ExpenseRed.copy(alpha = 0.14f),
            modifier = Modifier.size(64.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = FinluxColors.ExpenseRed,
                    modifier = Modifier.size(32.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Đã xảy ra lỗi",
            style = FinluxTextStyles.SectionTitle,
            color = tokens.onSurface,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = message,
            style = FinluxTextStyles.Body,
            color = tokens.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onRetry,
                shape = RoundedCornerShape(tokens.radius.smallChip),
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Thử lại", style = FinluxTextStyles.Caption, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/**
 * Standard Offline Banner (FinLux Prism Spec 23/UI-FIX-11 & 24)
 */
@Composable
fun FinluxOfflineState(
    modifier: Modifier = Modifier,
    message: String = "Đang chạy ở chế độ ngoại tuyến",
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = FinluxColors.WarningAmber.copy(alpha = 0.16f),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                tint = FinluxColors.WarningAmber,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = message,
                style = FinluxTextStyles.Caption,
                color = FinluxColors.WarningAmber,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
