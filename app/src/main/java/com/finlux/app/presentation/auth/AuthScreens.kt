package com.finlux.app.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
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
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    LaunchedEffect(state.completed) { if (state.completed) onCompleted() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFEBF2FF),
                        Color(0xFFF3F7FF),
                        Color(0xFFF8FAFC),
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState()),
        ) {
            // Header Section
            AuthHeaderSection(
                mode = mode,
                onBack = { onNavigate(AuthMode.LOGIN) }
            )

            Spacer(Modifier.height(12.dp))

            // Form Card Section
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                        ambientColor = Color(0x203B82F6),
                        spotColor = Color(0x304F46E5)
                    ),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                ) {
                    // Mode Tabs (Login vs Register)
                    if (mode == AuthMode.LOGIN || mode == AuthMode.REGISTER) {
                        AuthModeTabs(
                            currentMode = mode,
                            onTabSelected = onNavigate
                        )
                        Spacer(Modifier.height(24.dp))
                    }

                    // Form Fields according to Mode
                    when (mode) {
                        AuthMode.LOGIN -> LoginFormContent(state, viewModel, onNavigate)
                        AuthMode.REGISTER -> RegisterFormContent(state, viewModel, onNavigate)
                        AuthMode.FORGOT -> ForgotFormContent(state, viewModel, onNavigate)
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthHeaderSection(
    mode: AuthMode,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val wallet3dRes = remember { context.resources.getIdentifier("auth_wallet_3d", "drawable", context.packageName) }
    val clipboard3dRes = remember { context.resources.getIdentifier("auth_clipboard_3d", "drawable", context.packageName) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 44.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (mode == AuthMode.REGISTER || mode == AuthMode.FORGOT) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.85f))
                            .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                }

                FinluxLogoHeader(fontSize = 28.sp, isDark = false)
                Spacer(Modifier.height(8.dp))

                Text(
                    text = mode.heading,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A),
                    lineHeight = 30.sp
                )
                Spacer(Modifier.height(6.dp))

                Text(
                    text = mode.description,
                    fontSize = 13.5.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 19.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            // 3D Illustration Graphic
            val imageRes = if (mode == AuthMode.REGISTER) clipboard3dRes else wallet3dRes
            if (imageRes != 0) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(125.dp)
                        .padding(top = 4.dp)
                )
            } else {
                FinluxBrandMark(size = 90.dp, framed = true)
            }
        }
    }
}

@Composable
private fun AuthModeTabs(
    currentMode: AuthMode,
    onTabSelected: (AuthMode) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        val loginSelected = currentMode == AuthMode.LOGIN
        val registerSelected = currentMode == AuthMode.REGISTER

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable { onTabSelected(AuthMode.LOGIN) }
                .padding(bottom = 6.dp)
        ) {
            Text(
                text = "Đăng nhập",
                fontSize = 16.sp,
                fontWeight = if (loginSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (loginSelected) Color(0xFF1E293B) else Color(0xFF94A3B8)
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (loginSelected) Color(0xFF4F46E5) else Color.Transparent)
            )
        }

        Spacer(Modifier.width(36.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable { onTabSelected(AuthMode.REGISTER) }
                .padding(bottom = 6.dp)
        ) {
            Text(
                text = "Đăng ký",
                fontSize = 16.sp,
                fontWeight = if (registerSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (registerSelected) Color(0xFF1E293B) else Color(0xFF94A3B8)
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (registerSelected) Color(0xFF4F46E5) else Color.Transparent)
            )
        }
    }
}

@Composable
private fun LoginFormContent(
    state: AuthUiState,
    viewModel: AuthViewModel,
    onNavigate: (AuthMode) -> Unit,
) {
    Column {
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

        // Remember Me & Forgot Password Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { viewModel.toggleRememberMe(!state.rememberMe) }
            ) {
                Checkbox(
                    checked = state.rememberMe,
                    onCheckedChange = viewModel::toggleRememberMe,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF4F46E5),
                        uncheckedColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Ghi nhớ đăng nhập",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            }

            TextButton(
                onClick = { onNavigate(AuthMode.FORGOT) },
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF4F46E5))
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
        SocialLoginRow(onSocialClick = viewModel::signIn)

        Spacer(Modifier.height(28.dp))

        // Terms Notice
        Text(
            text = buildAnnotatedString {
                append("Bằng việc đăng nhập, bạn đồng ý với ")
                withStyle(SpanStyle(color = Color(0xFF4F46E5), fontWeight = FontWeight.SemiBold, textDecoration = TextDecoration.Underline)) {
                    append("Điều khoản sử dụng")
                }
                append(" và ")
                withStyle(SpanStyle(color = Color(0xFF4F46E5), fontWeight = FontWeight.SemiBold, textDecoration = TextDecoration.Underline)) {
                    append("Chính sách bảo mật")
                }
            },
            fontSize = 12.sp,
            color = Color(0xFF94A3B8),
            lineHeight = 17.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun RegisterFormContent(
    state: AuthUiState,
    viewModel: AuthViewModel,
    onNavigate: (AuthMode) -> Unit,
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
            value = state.email,
            onValueChange = viewModel::updateEmail,
            placeholder = "Email",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email
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
            text = "Tạo tài khoản",
            isLoading = state.isLoading,
            onClick = viewModel::register
        )

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
            .height(54.dp),
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
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF4F46E5),
            unfocusedBorderColor = Color(0xFFE2E8F0),
            focusedContainerColor = Color(0xFFF8FAFC),
            unfocusedContainerColor = Color(0xFFF8FAFC),
            focusedTextColor = Color(0xFF0F172A),
            unfocusedTextColor = Color(0xFF0F172A),
        )
    )
}

@Composable
private fun GradientButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color(0x304F46E5),
                spotColor = Color(0x403B82F6)
            ),
        enabled = !isLoading,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = androidx.compose.foundation.layout.PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF4F46E5), Color(0xFF3B82F6))
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = text,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SocialDivider(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(Modifier.weight(1f), color = Color(0xFFE2E8F0))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(Modifier.weight(1f), color = Color(0xFFE2E8F0))
    }
}

@Composable
private fun SocialLoginRow(onSocialClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SocialCard(
            title = "Google",
            iconContent = { GoogleIcon() },
            onClick = onSocialClick,
            modifier = Modifier.weight(1f)
        )
        SocialCard(
            title = "Apple",
            iconContent = { AppleIcon() },
            onClick = onSocialClick,
            modifier = Modifier.weight(1f)
        )
        SocialCard(
            title = "Facebook",
            iconContent = { FacebookIcon() },
            onClick = onSocialClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SocialCard(
    title: String,
    iconContent: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            iconContent()
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
        }
    }
}

@Composable
private fun GoogleIcon() {
    Text("G", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFEA4335))
}

@Composable
private fun AppleIcon() {
    Text("", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF000000))
}

@Composable
private fun FacebookIcon() {
    Text("f", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1877F2))
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
            color = if (isDark) Color.White else Color(0xFF0F172A)
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
    REGISTER("Tạo tài khoản", "Tạo tài khoản", "Tạo tài khoản mới", "Bắt đầu hành trình quản lý tài chính thông minh"),
    FORGOT("Quên mật khẩu", "Gửi email khôi phục", "Khôi phục mật khẩu", "Nhập email để nhận liên kết đặt lại mật khẩu"),
}

