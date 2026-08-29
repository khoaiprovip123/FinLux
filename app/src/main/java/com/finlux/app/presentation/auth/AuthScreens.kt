package com.finlux.app.presentation.auth

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
fun SplashScreen(
    onAuthenticated: () -> Unit,
    onGuest: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val session = viewModel.session.collectAsStateWithLifecycle().value
    LaunchedEffect(session) {
        if (session == SessionState.CHECKING) return@LaunchedEffect
        delay(800)
        if (session == SessionState.AUTHENTICATED) onAuthenticated() else onGuest()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF311042))
                )
            ),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FinluxBrandMark(size = 140.dp, framed = false)
            Spacer(Modifier.height(16.dp))
            FinluxLogoHeader(fontSize = 36.sp, isDark = true)
            Spacer(Modifier.height(6.dp))
            Text(
                "Quản lý tài chính thông minh",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(Modifier.size(24.dp), color = Color(0xFF6366F1), strokeWidth = 2.5.dp)
            Spacer(Modifier.height(12.dp))
            Text("Đang tải dữ liệu...", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
        }
    }
}

@Composable
fun AuthScreen(
    mode: AuthMode,
    onCompleted: () -> Unit,
    onNavigate: (AuthMode) -> Unit,
    onSocialSignIn: (SocialAuthProvider) -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    LaunchedEffect(state.completed) { if (state.completed) onCompleted() }

    val context = androidx.compose.ui.platform.LocalContext.current
    val handleSocialClick: (SocialAuthProvider) -> Unit = { provider ->
        when (provider) {
            SocialAuthProvider.GOOGLE -> viewModel.signInWithGoogle(context)
            SocialAuthProvider.APPLE -> {
                android.widget.Toast.makeText(context, "Đăng nhập bằng Apple sắp ra mắt!", android.widget.Toast.LENGTH_SHORT).show()
            }
            SocialAuthProvider.FACEBOOK -> {
                android.widget.Toast.makeText(context, "Đăng nhập bằng Facebook sắp ra mắt!", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            // Header Section
            AuthHeaderSection(
                mode = mode,
                onBack = { onNavigate(AuthMode.LOGIN) }
            )

            Spacer(Modifier.height(if (mode == AuthMode.LOGIN) 2.dp else 0.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (mode == AuthMode.LOGIN) 0.dp else 16.dp,
                        shape = if (mode == AuthMode.LOGIN) RoundedCornerShape(0.dp) else RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                        ambientColor = Color(0x203B82F6),
                        spotColor = Color(0x304F46E5)
                    ),
                shape = if (mode == AuthMode.LOGIN) RoundedCornerShape(0.dp) else RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 26.dp,
                            vertical = if (mode == AuthMode.LOGIN) 14.dp else 22.dp,
                        )
                ) {
                    // Form Fields according to Mode
                    when (mode) {
                        AuthMode.LOGIN -> LoginFormContent(state, viewModel, onNavigate, handleSocialClick)
                        AuthMode.REGISTER -> RegisterFormContent(state, viewModel, onNavigate, handleSocialClick)
                        AuthMode.FORGOT -> ForgotFormContent(state, viewModel, onNavigate)
                    }
                }
            }
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF4F46E5))
            }
        }
    }
}

@Composable
private fun AuthHeaderSection(
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
                    listOf(Color(0xFF312E81), Color(0xFF5B21B6), Color(0xFF7C3AED)),
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
                tint = Color.White,
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
                color = Color.White,
                lineHeight = 36.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = mode.description,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.88f),
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
private fun LoginFormContent(
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
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF5B21B6)),
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
                color = Color(0xFFEF4444),
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
                color = Color(0xFF5B21B6),
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
private fun RegisterFormContent(
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
                    checkedColor = Color(0xFF4F46E5),
                    uncheckedColor = Color(0xFFCBD5E1)
                ),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = buildAnnotatedString {
                    append("Tôi đồng ý với ")
                    withStyle(SpanStyle(color = Color(0xFF4F46E5), fontWeight = FontWeight.Bold)) {
                        append("Điều khoản sử dụng")
                    }
                    append(" và ")
                    withStyle(SpanStyle(color = Color(0xFF4F46E5), fontWeight = FontWeight.Bold)) {
                        append("Chính sách bảo mật")
                    }
                },
                fontSize = 12.5.sp,
                color = Color(0xFF475569),
                lineHeight = 17.sp
            )
        }

        state.error?.let { errorMsg ->
            Spacer(Modifier.height(10.dp))
            Text(
                text = errorMsg,
                color = Color(0xFFEF4444),
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
            Text("Đã có tài khoản? ", fontSize = 13.5.sp, color = Color(0xFF64748B))
            Text(
                text = "Đăng nhập ngay",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4F46E5),
                modifier = Modifier.clickable { onNavigate(AuthMode.LOGIN) }
            )
        }
    }
}

@Composable
private fun ForgotFormContent(
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
                color = Color(0xFFEF4444),
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
                color = Color(0xFF4F46E5),
                modifier = Modifier.clickable { onNavigate(AuthMode.LOGIN) }
            )
        }
    }
}

@Composable
private fun PasswordStrengthBar(
    score: Int,
    strengthText: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Độ mạnh mật khẩu",
            fontSize = 12.sp,
            color = Color(0xFF64748B)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(4) { index ->
                val active = index < score
                val activeColor = when (score) {
                    1 -> Color(0xFFEF4444)
                    2 -> Color(0xFFF59E0B)
                    3 -> Color(0xFF10B981)
                    4 -> Color(0xFF059669)
                    else -> Color(0xFFCBD5E1)
                }
                Box(
                    modifier = Modifier
                        .width(26.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (active) activeColor else Color(0xFFE2E8F0))
                )
            }

            Spacer(Modifier.width(6.dp))

            Text(
                text = strengthText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = when (score) {
                    1 -> Color(0xFFEF4444)
                    2 -> Color(0xFFF59E0B)
                    3 -> Color(0xFF10B981)
                    4 -> Color(0xFF059669)
                    else -> Color(0xFF64748B)
                }
            )
        }
    }
}

@Composable
private fun FinluxInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        placeholder = {
            Text(
                text = placeholder,
                color = Color(0xFF94A3B8),
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Hiển thị mật khẩu",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = true,
        shape = RoundedCornerShape(15.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF4F46E5),
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        )
    )
}

@Composable
private fun GradientButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.975f else 1f,
        animationSpec = spring(stiffness = 650f, dampingRatio = 0.72f),
        label = "authPrimaryButtonScale",
    )

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer {
                scaleX = buttonScale
                scaleY = buttonScale
            }
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color(0x307C3AED),
                spotColor = Color(0x405B21B6),
            ),
        enabled = !isLoading,
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = androidx.compose.foundation.layout.PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF5B2BFF), Color(0xFF7C2CFF)),
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun LoginBottomWave() {
    val waveColor = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
    ) {
        val backWave = Path().apply {
            moveTo(0f, size.height * 0.38f)
            quadraticTo(size.width * 0.22f, size.height * 0.92f, size.width * 0.52f, size.height * 0.58f)
            quadraticTo(size.width * 0.78f, size.height * 0.28f, size.width, size.height * 0.72f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        val frontWave = Path().apply {
            moveTo(0f, size.height * 0.64f)
            quadraticTo(size.width * 0.32f, size.height * 1.04f, size.width * 0.62f, size.height * 0.72f)
            quadraticTo(size.width * 0.84f, size.height * 0.48f, size.width, size.height * 0.82f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(backWave, waveColor.copy(alpha = 0.07f))
        drawPath(frontWave, waveColor.copy(alpha = 0.045f))
    }
}

@Composable
private fun SocialDivider(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun SocialLoginRow(onSocialClick: (SocialAuthProvider) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SocialCard(
            title = "Google",
            iconRes = R.drawable.ic_google_g,
            onClick = { onSocialClick(SocialAuthProvider.GOOGLE) },
            modifier = Modifier.weight(1f)
        )
        SocialCard(
            title = "Facebook",
            iconRes = R.drawable.ic_facebook,
            onClick = { onSocialClick(SocialAuthProvider.FACEBOOK) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SocialCard(
    title: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(62.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(15.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = "Đăng nhập bằng $title",
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun FinluxLogoHeader(
    fontSize: androidx.compose.ui.unit.TextUnit = 28.sp,
    isDark: Boolean = false,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Fin",
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Lux",
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF3478F6)
        )
    }
}

enum class AuthMode(val title: String, val action: String, val heading: String, val description: String) {
    LOGIN("Đăng nhập", "Đăng nhập", "Chào mừng trở lại! 👋", "Đăng nhập để tiếp tục quản lý tài chính của bạn"),
    REGISTER("Tạo tài khoản", "Tạo tài khoản", "Tạo tài khoản", "Tham gia FinLux để quản lý tài chính của bạn hiệu quả hơn"),
    FORGOT("Quên mật khẩu", "Gửi email khôi phục", "Khôi phục mật khẩu", "Nhập email để nhận liên kết đặt lại mật khẩu"),
}

/** UI contract for the future Credential Manager / provider SDK integrations. */
enum class SocialAuthProvider { GOOGLE, APPLE, FACEBOOK }
