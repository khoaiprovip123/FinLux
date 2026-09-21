package com.finlux.app.presentation.settings.deleteaccount

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAccountBottomSheet(
    onDismiss: () -> Unit,
    onAccountDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DeleteAccountViewModel = hiltViewModel(),
) {
    val tokens = LocalFinluxTokens.current
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { !state.isPurging },
    )

    BackHandler(enabled = state.isPurging) {
        // Prevent back press during account deletion
    }

    LaunchedEffect(state.isPurgeCompleted) {
        if (state.isPurgeCompleted) {
            onAccountDeleted()
        }
    }

    fun shareBackup(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "FinLux Safety Backup")
            putExtra(Intent.EXTRA_TEXT, "Bản sao lưu dữ liệu an toàn FinLux trước khi xóa tài khoản")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Lưu hoặc chia sẻ bản sao lưu FinLux"))
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (!state.isPurging) {
                viewModel.resetState()
                onDismiss()
            }
        },
        sheetState = sheetState,
        containerColor = tokens.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                // Header Bar with step indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(tokens.error.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = tokens.error,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Column {
                            Text(
                                text = "Xóa tài khoản",
                                style = FinluxTextStyles.SectionTitle,
                                color = tokens.textPrimary,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Bước ${state.currentStep} / 3: ${
                                    when (state.currentStep) {
                                        1 -> "Thống kê thiệt hại"
                                        2 -> "Gợi ý sao lưu an toàn"
                                        else -> "Xác nhận & Hủy"
                                    }
                                }",
                                style = FinluxTextStyles.Caption,
                                color = tokens.textSecondary,
                            )
                        }
                    }

                    if (!state.isPurging) {
                        IconButton(onClick = {
                            viewModel.resetState()
                            onDismiss()
                        }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Đóng",
                                tint = tokens.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Step Progress Indicator
                StepProgressBar(currentStep = state.currentStep)

                // Error banner
                state.errorMessage?.let { error ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = tokens.error.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, tokens.error.copy(alpha = 0.35f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = tokens.error)
                            Text(
                                text = error,
                                style = FinluxTextStyles.Caption,
                                color = tokens.error,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                // Step Contents
                AnimatedContent(
                    targetState = state.currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "DeleteAccountStepsAnimation",
                ) { step ->
                    when (step) {
                        1 -> Step1DamagePreview(
                            state = state,
                            onNext = { viewModel.goToStep(2) },
                        )
                        2 -> Step2BackupSuggestion(
                            state = state,
                            onExportBackup = { viewModel.exportBackup(::shareBackup) },
                            onNext = { viewModel.goToStep(3) },
                            onBack = { viewModel.goToStep(1) },
                        )
                        3 -> Step3DoubleConfirmation(
                            state = state,
                            onPasswordChanged = viewModel::onPasswordChanged,
                            onConfirmationChanged = viewModel::onConfirmationInputChanged,
                            onGoogleReauth = { viewModel.reauthenticateWithGoogle(context) },
                            onExecuteDelete = { viewModel.executeDeleteAccount() },
                            onBack = { viewModel.goToStep(2) },
                        )
                    }
                }
            }

            // Purging Overlay
            if (state.isPurging) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(tokens.surface.copy(alpha = 0.94f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(54.dp),
                            color = tokens.error,
                            strokeWidth = 3.5.dp,
                        )
                        Text(
                            text = state.purgingStage.message,
                            style = FinluxTextStyles.Body,
                            color = tokens.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "Vui lòng giữ ứng dụng mở, quá trình dọn dẹp đang được thực hiện...",
                            style = FinluxTextStyles.Caption,
                            color = tokens.textSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepProgressBar(currentStep: Int) {
    val tokens = LocalFinluxTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (i in 1..3) {
            val isPassedOrCurrent = i <= currentStep
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isPassedOrCurrent) tokens.error else tokens.onSurface.copy(alpha = 0.10f),
                    ),
            )
        }
    }
}

@Composable
private fun Step1DamagePreview(
    state: DeleteAccountUiState,
    onNext: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.animateContentSize(),
    ) {
        // Warning Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = tokens.error.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, tokens.error.copy(alpha = 0.40f)),
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = tokens.error, modifier = Modifier.size(24.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "CẢNH BÁO NGUY HIỂM",
                        style = FinluxTextStyles.MicroLabel.copy(fontWeight = FontWeight.Bold),
                        color = tokens.error,
                    )
                    Text(
                        text = "Hành động này là vĩnh viễn và KHÔNG THỂ HOÀN TÁC. Toàn bộ dữ liệu đám mây và cục bộ của bạn sẽ bị xóa sạch hoàn toàn.",
                        style = FinluxTextStyles.Caption,
                        color = tokens.textPrimary,
                        lineHeight = 18.sp,
                    )
                }
            }
        }

        Text(
            text = "DỮ LIỆU SẮP BỊ HỦY BỎ VĨNH VIỄN",
            style = FinluxTextStyles.MicroLabel.copy(fontWeight = FontWeight.Bold),
            color = tokens.textSecondary,
        )

        if (state.isLoadingStats) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = tokens.primary, modifier = Modifier.size(28.dp))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DamageStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "Ví tài sản",
                        count = state.damageStats.walletCount,
                    )
                    DamageStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.ReceiptLong,
                        title = "Giao dịch",
                        count = state.damageStats.transactionCount,
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DamageStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CreditCard,
                        title = "Khoản nợ",
                        count = state.damageStats.debtCount,
                    )
                    DamageStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Savings,
                        title = "Mục tiêu",
                        count = state.damageStats.goalCount,
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DamageStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.NotificationsNone,
                        title = "Nhắc nhở & Báo thức",
                        count = state.damageStats.reminderCount,
                    )
                    DamageStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Security,
                        title = "Ngân sách & Chu kỳ",
                        count = state.damageStats.budgetCount,
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(tokens.radius.input),
            colors = ButtonDefaults.buttonColors(containerColor = tokens.surfaceSoft),
            border = BorderStroke(1.dp, tokens.onSurface.copy(alpha = 0.12f)),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Tôi hiểu rủi ro, tiếp tục",
                    color = tokens.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = tokens.textPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun DamageStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    count: Int,
) {
    val tokens = LocalFinluxTokens.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = tokens.surfaceSoft,
        border = BorderStroke(1.dp, tokens.onSurface.copy(alpha = 0.08f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tokens.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Column {
                Text(
                    text = title,
                    style = FinluxTextStyles.Caption,
                    color = tokens.textSecondary,
                )
                Text(
                    text = "$count mục",
                    style = FinluxTextStyles.Body.copy(fontWeight = FontWeight.Bold),
                    color = tokens.textPrimary,
                )
            }
        }
    }
}

@Composable
private fun Step2BackupSuggestion(
    state: DeleteAccountUiState,
    onExportBackup: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "BackupButtonSpring",
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.animateContentSize(),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = tokens.primary.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, tokens.primary.copy(alpha = 0.25f)),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = tokens.primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = "Khuyến nghị an toàn",
                        style = FinluxTextStyles.Body.copy(fontWeight = FontWeight.Bold),
                        color = tokens.primary,
                    )
                }
                Text(
                    text = "Trước khi xóa tài khoản vĩnh viễn, bạn nên xuất một bản sao lưu dữ liệu (.finlux) để lưu trên thiết bị. Bạn có thể dùng tệp này để nhập lại vào tài khoản mới trong tương lai nếu đổi ý.",
                    style = FinluxTextStyles.Caption,
                    color = tokens.textPrimary,
                    lineHeight = 19.sp,
                )
            }
        }

        // Action: Export Backup
        OutlinedButton(
            onClick = onExportBackup,
            enabled = !state.isExportingBackup,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .scale(scale),
            interactionSource = interactionSource,
            shape = RoundedCornerShape(tokens.radius.input),
            border = BorderStroke(1.5.dp, tokens.primary),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (state.isExportingBackup) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = tokens.primary,
                        strokeWidth = 2.dp,
                    )
                    Text("Đang tạo bản sao lưu...", color = tokens.primary, fontWeight = FontWeight.SemiBold)
                } else {
                    Icon(Icons.Default.Download, contentDescription = null, tint = tokens.primary)
                    Text("Tải bản sao lưu (.finlux) về máy", color = tokens.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        state.exportBackupSuccessMessage?.let { successMsg ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = tokens.primary.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, tokens.primary.copy(alpha = 0.30f)),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = tokens.primary)
                    Text(successMsg, style = FinluxTextStyles.Caption, color = tokens.primary)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(tokens.radius.input),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Quay lại")
                }
            }

            Button(
                onClick = onNext,
                modifier = Modifier.weight(1.5f).height(48.dp),
                shape = RoundedCornerShape(tokens.radius.input),
                colors = ButtonDefaults.buttonColors(containerColor = tokens.surfaceSoft),
                border = BorderStroke(1.dp, tokens.onSurface.copy(alpha = 0.12f)),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("Tiếp tục xác nhận", color = tokens.textPrimary, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = tokens.textPrimary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun Step3DoubleConfirmation(
    state: DeleteAccountUiState,
    onPasswordChanged: (String) -> Unit,
    onConfirmationChanged: (String) -> Unit,
    onGoogleReauth: () -> Unit,
    onExecuteDelete: () -> Unit,
    onBack: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.animateContentSize(),
    ) {
        // Re-authentication Gate
        Text(
            text = "1. XÁC THỰC DANH TÍNH CHỦ TÀI KHOẢN",
            style = FinluxTextStyles.MicroLabel.copy(fontWeight = FontWeight.Bold),
            color = tokens.textSecondary,
        )

        if (state.providerId == "google.com") {
            if (state.isGoogleReauthenticated || !state.googleIdToken.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = tokens.primary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, tokens.primary.copy(alpha = 0.35f)),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = tokens.primary)
                        Text(
                            text = "Đã xác thực danh tính Google thành công",
                            style = FinluxTextStyles.Body.copy(fontWeight = FontWeight.SemiBold),
                            color = tokens.primary,
                        )
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onGoogleReauth,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(tokens.radius.input),
                    border = BorderStroke(1.5.dp, tokens.primary),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = tokens.primary)
                        Text(
                            text = "Xác thực lại bằng tài khoản Google",
                            color = tokens.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        } else {
            // Password input
            OutlinedTextField(
                value = state.passwordInput,
                onValueChange = onPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nhập mật khẩu hiện tại") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = tokens.onSurfaceVariant) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                            tint = tokens.onSurfaceVariant,
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                singleLine = true,
                shape = RoundedCornerShape(tokens.radius.input),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = tokens.primary,
                    unfocusedBorderColor = tokens.onSurface.copy(alpha = 0.20f),
                ),
            )
        }

        // Phrase Confirmation Gate
        Text(
            text = "2. KHÓA AN TOÀN CHỐNG BẤM NHẦM",
            style = FinluxTextStyles.MicroLabel.copy(fontWeight = FontWeight.Bold),
            color = tokens.textSecondary,
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = tokens.surfaceSoft,
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Để xác nhận xóa, vui lòng nhập chính xác cụm từ sau:",
                    style = FinluxTextStyles.Caption,
                    color = tokens.textSecondary,
                )
                Text(
                    text = "XÓA TÀI KHOẢN",
                    style = FinluxTextStyles.Body.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                    color = tokens.error,
                )
            }
        }

        OutlinedTextField(
            value = state.confirmationInput,
            onValueChange = onConfirmationChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Gõ 'XÓA TÀI KHOẢN'") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            trailingIcon = {
                if (state.isConfirmationTextValid) {
                    Icon(Icons.Default.Check, contentDescription = "Khớp", tint = tokens.primary)
                }
            },
            shape = RoundedCornerShape(tokens.radius.input),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (state.isConfirmationTextValid) tokens.primary else tokens.error,
                unfocusedBorderColor = if (state.isConfirmationTextValid) tokens.primary.copy(alpha = 0.5f) else tokens.onSurface.copy(alpha = 0.20f),
            ),
        )

        Spacer(Modifier.height(8.dp))

        // Final Delete Button
        Button(
            onClick = {
                focusManager.clearFocus()
                onExecuteDelete()
            },
            enabled = state.canExecuteDelete,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(tokens.radius.input),
            colors = ButtonDefaults.buttonColors(
                containerColor = tokens.error,
                disabledContainerColor = tokens.error.copy(alpha = 0.25f),
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (state.canExecuteDelete) Color.White else Color.White.copy(alpha = 0.5f),
                )
                Text(
                    text = "XÓA VĨNH VIỄN TÀI KHOẢN",
                    color = if (state.canExecuteDelete) Color.White else Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }

        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Quay lại bước trước", color = tokens.textSecondary)
        }
    }
}
