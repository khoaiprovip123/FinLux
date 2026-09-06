@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.finlux.app.presentation.auth

import com.finlux.app.core.designsystem.theme.FinluxPalette
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.alpha
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.R
import com.finlux.app.core.designsystem.FinluxBrandMark
import kotlinx.coroutines.delay

@Composable
internal fun AuthHeaderSection(
    mode: AuthMode,
    onBack: () -> Unit,
) {
    if (mode == AuthMode.LOGIN) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.045f),
                        ),
                    ),
                )
                .statusBarsPadding()
                .height(270.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.auth_clipboard_3d_v2),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 2.dp, bottom = 2.dp)
                    .size(94.dp)
                    .alpha(0.5f),
            )
            Image(
                painter = painterResource(R.drawable.auth_wallet_3d_v2),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp)
                    .size(112.dp)
                    .alpha(0.84f),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                FinluxBrandMark(size = 74.dp, framed = true)
                Spacer(Modifier.height(9.dp))
                FinluxLogoHeader(fontSize = 38.sp, isDark = false)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Quản lý tài chính thông minh",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                )
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(FinluxPalette.CFF312E81, FinluxPalette.CFF5B21B6, FinluxPalette.CFF7C3AED),
                ),
            )
            .statusBarsPadding()
            .height(244.dp),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(start = 14.dp, top = 8.dp)
                .align(Alignment.TopStart),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Quay lại",
                tint = FinluxPalette.White,
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 26.dp, end = 150.dp, bottom = 28.dp),
        ) {
            Text(
                text = mode.heading,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FinluxPalette.White,
                lineHeight = 36.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = mode.description,
                fontSize = 14.sp,
                color = FinluxPalette.White.copy(alpha = 0.88f),
                lineHeight = 20.sp,
            )
        }

        Image(
            painter = painterResource(
                if (mode == AuthMode.REGISTER) R.drawable.auth_clipboard_3d_v2 else R.drawable.auth_wallet_3d_v2,
            ),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp, top = 24.dp)
                .size(146.dp),
        )
    }
}

@Composable
internal fun LoginFormContent(
    state: AuthUiState,
    viewModel: AuthViewModel,
    onNavigate: (AuthMode) -> Unit,
    onSocialSignIn: (SocialAuthProvider) -> Unit,
) {
    Column {
        Text(
            text = "Đăng nhập",
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Chào mừng bạn trở lại! Vui lòng đăng nhập để tiếp tục.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(26.dp))

        FinluxInput(
            value = state.email,
            onValueChange = viewModel::updateEmail,
            placeholder = "Email hoặc số điện thoại",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email
        )

        Spacer(Modifier.height(14.dp))

        FinluxInput(
            value = state.password,
            onValueChange = viewModel::updatePassword,
            placeholder = "Mật khẩu",
            leadingIcon = Icons.Default.Lock,
            isPassword = true
        )

        Spacer(Modifier.height(10.dp))

        // The reference keeps this row deliberately minimal: only the recovery action.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = { onNavigate(AuthMode.FORGOT) },
                colors = ButtonDefaults.textButtonColors(contentColor = FinluxPalette.CFF5B21B6),
            ) {
                Text(
                    text = "Quên mật khẩu?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        state.error?.let { errorMsg ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = errorMsg,
                color = FinluxPalette.CFFEF4444,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(20.dp))

        // Primary Login Button
        GradientButton(
            text = "Đăng nhập",
            isLoading = state.isLoading,
            onClick = viewModel::signIn
        )

        Spacer(Modifier.height(24.dp))

        // Divider
        SocialDivider(text = "hoặc đăng nhập với")

        Spacer(Modifier.height(18.dp))

        // Social Login Cards Row
        SocialLoginRow(onSocialClick = onSocialSignIn)

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Chưa có tài khoản? ", fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = "Đăng ký ngay",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = FinluxPalette.CFF5B21B6,
                modifier = Modifier.clickable { onNavigate(AuthMode.REGISTER) },
            )
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                modifier = Modifier
                    .clickable { viewModel.signInDemoMode() }
                    .padding(horizontal = 8.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Trải nghiệm ngay (Chế độ Dùng thử)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        LoginBottomWave()
    }
}

@Composable
internal fun RegisterFormContent(
    state: AuthUiState,
    viewModel: AuthViewModel,
    onNavigate: (AuthMode) -> Unit,
    onSocialSignIn: (SocialAuthProvider) -> Unit,
) {
    Column {
        FinluxInput(
            value = state.displayName,
            onValueChange = viewModel::updateDisplayName,
            placeholder = "Họ và tên",
            leadingIcon = Icons.Default.Person
        )

        Spacer(Modifier.height(14.dp))

        FinluxInput(
            value = state.phone,
            onValueChange = viewModel::updatePhone,
            placeholder = "Số điện thoại",
            leadingIcon = Icons.Default.Phone,
            keyboardType = KeyboardType.Phone
        )

        Spacer(Modifier.height(14.dp))

        FinluxInput(
            value = state.email,
            onValueChange = viewModel::updateEmail,
            placeholder = "Email",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email
        )

        Spacer(Modifier.height(14.dp))

        FinluxInput(
            value = state.password,
            onValueChange = viewModel::updatePassword,
            placeholder = "Mật khẩu",
            leadingIcon = Icons.Default.Lock,
            isPassword = true
        )

        // Password Strength Indicator
        if (state.password.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            PasswordStrengthBar(
                score = state.passwordStrengthScore,
                strengthText = state.passwordStrengthText
            )
        }

        Spacer(Modifier.height(14.dp))

        FinluxInput(
            value = state.confirmPassword,
            onValueChange = viewModel::updateConfirmPassword,
            placeholder = "Xác nhận mật khẩu",
            leadingIcon = Icons.Default.Lock,
            isPassword = true
        )

        Spacer(Modifier.height(14.dp))

        // Terms Agreement Checkbox
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.toggleAgreeTerms(!state.agreeTerms) }
        ) {
            Checkbox(
                checked = state.agreeTerms,
                onCheckedChange = viewModel::toggleAgreeTerms,
                colors = CheckboxDefaults.colors(
                    checkedColor = FinluxPalette.CFF4F46E5,
                    uncheckedColor = FinluxPalette.CFFCBD5E1
                ),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = buildAnnotatedString {
                    append("Tôi đồng ý với ")
                    withStyle(SpanStyle(color = FinluxPalette.CFF4F46E5, fontWeight = FontWeight.Bold)) {
                        append("Điều khoản sử dụng")
                    }
                    append(" và ")
                    withStyle(SpanStyle(color = FinluxPalette.CFF4F46E5, fontWeight = FontWeight.Bold)) {
                        append("Chính sách bảo mật")
                    }
                },
                fontSize = 12.5.sp,
                color = FinluxPalette.CFF475569,
                lineHeight = 17.sp
            )
        }

        state.error?.let { errorMsg ->
            Spacer(Modifier.height(10.dp))
            Text(
                text = errorMsg,
                color = FinluxPalette.CFFEF4444,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(20.dp))

        // Primary Register Button
        GradientButton(
            text = "Đăng ký",
            isLoading = state.isLoading,
            onClick = viewModel::register
        )

        Spacer(Modifier.height(22.dp))

        SocialDivider(text = "Hoặc đăng ký với")

        Spacer(Modifier.height(16.dp))

        SocialLoginRow(onSocialClick = onSocialSignIn)

        Spacer(Modifier.height(22.dp))

        // Footer Navigation Link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Đã có tài khoản? ", fontSize = 13.5.sp, color = FinluxPalette.CFF64748B)
            Text(
                text = "Đăng nhập ngay",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = FinluxPalette.CFF4F46E5,
                modifier = Modifier.clickable { onNavigate(AuthMode.LOGIN) }
            )
        }
    }
}

@Composable
internal fun ForgotFormContent(
    state: AuthUiState,
    viewModel: AuthViewModel,
    onNavigate: (AuthMode) -> Unit,
) {
    Column {
        FinluxInput(
            value = state.email,
            onValueChange = viewModel::updateEmail,
            placeholder = "Email đăng ký tài khoản",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email
        )

        state.error?.let { errorMsg ->
            Spacer(Modifier.height(10.dp))
            Text(
                text = errorMsg,
                color = FinluxPalette.CFFEF4444,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(22.dp))

        GradientButton(
            text = "Gửi email khôi phục",
            isLoading = state.isLoading,
            onClick = viewModel::resetPassword
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Quay lại đăng nhập",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = FinluxPalette.CFF4F46E5,
                modifier = Modifier.clickable { onNavigate(AuthMode.LOGIN) }
            )
        }
    }
}
