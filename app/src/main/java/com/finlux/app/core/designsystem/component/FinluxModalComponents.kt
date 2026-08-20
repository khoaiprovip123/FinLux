package com.finlux.app.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens

/**
 * Standard FinLux Modal Bottom Sheet (FinLux Prism Spec 6, 23/UI-FIX-07 & 24)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinluxBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    title: String? = null,
    subtitle: String? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val shape = RoundedCornerShape(
        topStart = tokens.radius.bottomSheet,
        topEnd = tokens.radius.bottomSheet,
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = shape,
        containerColor = tokens.surface,
        scrimColor = Color.Black.copy(alpha = 0.60f),
        dragHandle = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = tokens.onSurfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.size(width = 36.dp, height = 4.dp),
                ) {}

                if (title != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = title,
                        style = FinluxTextStyles.SectionTitle,
                        color = tokens.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = FinluxTextStyles.Caption,
                        color = tokens.onSurfaceVariant,
                    )
                }
            }
        },
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            content = content,
        )
    }
}

/**
 * Standard FinLux Dialog (FinLux Prism Spec 23/UI-FIX-06 & 24)
 */
@Composable
fun FinluxDialog(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    confirmLabel: String = "Xác nhận",
    onConfirm: (() -> Unit)? = null,
    isConfirmDestructive: Boolean = false,
    dismissLabel: String = "Hủy",
    properties: DialogProperties = DialogProperties(),
    content: (@Composable () -> Unit)? = null,
) {
    val tokens = LocalFinluxTokens.current
    val shape = RoundedCornerShape(tokens.radius.dialog)

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 20.dp,
                    shape = shape,
                    ambientColor = Color.Black.copy(alpha = 0.12f),
                    spotColor = Color.Black.copy(alpha = 0.18f),
                )
                .clip(shape)
                .background(tokens.surface)
                .border(
                    BorderStroke(1.dp, tokens.onSurface.copy(alpha = tokens.borderAlpha)),
                    shape,
                )
                .padding(24.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = title,
                    style = FinluxTextStyles.SectionTitle,
                    color = tokens.onSurface,
                    fontWeight = FontWeight.Bold,
                )

                if (message != null) {
                    Text(
                        text = message,
                        style = FinluxTextStyles.Body,
                        color = tokens.onSurfaceVariant,
                    )
                }

                content?.invoke()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        shape = RoundedCornerShape(tokens.radius.smallChip),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = tokens.onSurfaceVariant,
                        ),
                        border = BorderStroke(1.dp, tokens.onSurface.copy(alpha = tokens.borderAlpha)),
                    ) {
                        Text(dismissLabel, style = FinluxTextStyles.Caption, fontWeight = FontWeight.SemiBold)
                    }

                    if (onConfirm != null) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = onConfirm,
                            shape = RoundedCornerShape(tokens.radius.smallChip),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isConfirmDestructive) FinluxColors.ExpenseRed else tokens.primary,
                                contentColor = Color.White,
                            ),
                        ) {
                            Text(confirmLabel, style = FinluxTextStyles.Caption, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
