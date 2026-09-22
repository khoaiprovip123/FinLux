package com.finlux.app.presentation.updater

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.updater.AppUpdateInfo

@Composable
fun AppUpdateDialog(
    uiState: UpdateUiState,
    onDownloadAndInstall: (AppUpdateInfo) -> Unit,
    onInstallDownloaded: (java.io.File) -> Unit,
    onDismiss: () -> Unit,
) {
    when (uiState) {
        is UpdateUiState.UpdateAvailable -> {
            UpdateAvailableContent(
                info = uiState.info,
                onUpdate = { onDownloadAndInstall(uiState.info) },
                onDismiss = onDismiss,
            )
        }
        is UpdateUiState.Downloading -> {
            DownloadingContent(
                info = uiState.info,
                progress = uiState.progress,
            )
        }
        is UpdateUiState.ReadyToInstall -> {
            ReadyToInstallContent(
                info = uiState.info,
                onInstall = { onInstallDownloaded(uiState.apkFile) },
                onDismiss = onDismiss,
            )
        }
        is UpdateUiState.NoUpdateAvailable -> {
            SimpleMessageDialog(
                title = "Đã là bản mới nhất",
                message = "Ứng dụng FinLux đang ở phiên bản mới nhất (${uiState.currentVersion}).",
                icon = Icons.Rounded.CheckCircle,
                iconColor = FinluxColors.IncomeGreen,
                onDismiss = onDismiss,
            )
        }
        is UpdateUiState.Error -> {
            SimpleMessageDialog(
                title = "Không thể kiểm tra",
                message = uiState.message,
                icon = Icons.Rounded.ErrorOutline,
                iconColor = FinluxColors.ExpenseRed,
                onDismiss = onDismiss,
            )
        }
        is UpdateUiState.Checking -> {
            val tokens = LocalFinluxTokens.current
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
            ) {
                Surface(
                    shape = RoundedCornerShape(tokens.radius.dialog),
                    color = tokens.surface,
                    tonalElevation = tokens.elevation,
                    modifier = Modifier.padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 3.dp,
                            color = tokens.primary,
                        )
                        Text(
                            text = "Đang kiểm tra bản cập nhật...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = tokens.textPrimary,
                        )
                    }
                }
            }
        }
        UpdateUiState.Idle -> Unit
    }
}

@Composable
private fun UpdateAvailableContent(
    info: AppUpdateInfo,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false),
    ) {
        Surface(
            shape = RoundedCornerShape(tokens.radius.dialog),
            color = tokens.surface,
            tonalElevation = tokens.elevation,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(tokens.primaryBrush),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RocketLaunch,
                        contentDescription = null,
                        tint = tokens.onHero,
                        modifier = Modifier.size(30.dp),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Bản cập nhật mới!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = tokens.textPrimary,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(tokens.radius.smallChip),
                    color = tokens.primary.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = "Phiên bản: v${info.latestVersionName}",
                        style = MaterialTheme.typography.labelMedium,
                        color = tokens.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (info.releaseNotes.isNotBlank()) {
                    Text(
                        text = "Nội dung cập nhật:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = tokens.textPrimary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                            .clip(RoundedCornerShape(tokens.contentRadius))
                            .background(tokens.surfaceSoft)
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Text(
                            text = info.releaseNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.textSecondary,
                            lineHeight = 18.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(tokens.radius.input),
                    ) {
                        Text("Để sau", color = tokens.textSecondary)
                    }

                    Button(
                        onClick = onUpdate,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(tokens.radius.input),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tokens.primary,
                            contentColor = tokens.onHero,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cập nhật", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadingContent(
    info: AppUpdateInfo,
    progress: Float,
) {
    val tokens = LocalFinluxTokens.current
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Surface(
            shape = RoundedCornerShape(tokens.radius.dialog),
            color = tokens.surface,
            tonalElevation = tokens.elevation,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(tokens.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SystemUpdate,
                        contentDescription = null,
                        tint = tokens.primary,
                        modifier = Modifier.size(30.dp),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Đang tải bản cập nhật...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = tokens.textPrimary,
                )

                Spacer(modifier = Modifier.height(8.dp))

                val percent = (progress * 100).toInt()
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.titleLarge,
                    color = tokens.primary,
                    fontWeight = FontWeight.ExtraBold,
                )

                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = tokens.primary,
                    trackColor = tokens.surfaceSoft,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Vui lòng giữ ứng dụng mở trong giây lát",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun ReadyToInstallContent(
    info: AppUpdateInfo,
    onInstall: () -> Unit,
    onDismiss: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false),
    ) {
        Surface(
            shape = RoundedCornerShape(tokens.radius.dialog),
            color = tokens.surface,
            tonalElevation = tokens.elevation,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(FinluxColors.IncomeGreen),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = tokens.onHero,
                        modifier = Modifier.size(32.dp),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Đã tải xong bản cập nhật",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = tokens.textPrimary,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Bản v${info.latestVersionName} đã sẵn sàng để cài đặt.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.textSecondary,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(tokens.radius.input),
                    ) {
                        Text("Đóng", color = tokens.textSecondary)
                    }

                    Button(
                        onClick = onInstall,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(tokens.radius.input),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FinluxColors.IncomeGreen,
                            contentColor = tokens.onHero,
                        ),
                    ) {
                        Text("Cài đặt ngay", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SimpleMessageDialog(
    title: String,
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    onDismiss: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(tokens.radius.dialog),
            color = tokens.surface,
            tonalElevation = tokens.elevation,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(28.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = tokens.textPrimary,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.textSecondary,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.radius.input),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.primary,
                        contentColor = tokens.onHero,
                    ),
                ) {
                    Text("Đã hiểu")
                }
            }
        }
    }
}
