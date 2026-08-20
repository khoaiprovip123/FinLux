package com.finlux.app.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens

import androidx.compose.foundation.layout.statusBarsPadding

/**
 * Standard Screen Header (FinLux Prism Spec 8.1 & 24)
 */
@Composable
fun FinluxScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    unreadNotificationsCount: Int = 0,
    onNotificationClick: (() -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
) {
    val tokens = LocalFinluxTokens.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false),
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(end = 8.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = tokens.onSurface,
                    )
                }
            } else if (navigationIcon != null) {
                Box(modifier = Modifier.padding(end = 8.dp)) {
                    navigationIcon()
                }
            }

            Column {
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = FinluxTextStyles.Caption,
                        color = tokens.onSurfaceVariant,
                    )
                }
                Text(
                    text = title,
                    style = FinluxTextStyles.ScreenTitle,
                    color = tokens.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (onNotificationClick != null) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = onNotificationClick,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Thông báo",
                        tint = tokens.onSurface,
                        modifier = Modifier.size(24.dp),
                    )
                    if (unreadNotificationsCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = FinluxColors.ExpenseRed,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(9.dp),
                        ) {}
                    }
                }
            }

            actions?.invoke()
        }
    }
}

/**
 * Standard Section Header (FinLux Prism Spec 24)
 */
@Composable
fun FinluxSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    val tokens = LocalFinluxTokens.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = FinluxTextStyles.SectionTitle,
            color = tokens.onSurface,
            fontWeight = FontWeight.SemiBold,
        )

        if (action != null && onActionClick != null) {
            Text(
                text = action,
                style = MaterialTheme.typography.labelLarge,
                color = tokens.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = onActionClick,
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}
