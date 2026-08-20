package com.finlux.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens

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
                    contentColor = Color.White,
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
