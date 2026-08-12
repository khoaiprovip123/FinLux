package com.finlux.app.presentation.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinluxBlue
import com.finlux.app.core.designsystem.FinluxCyan
import com.finlux.app.core.designsystem.FinluxPurple
import com.finlux.app.core.designsystem.FinluxStyleBackdrop
import com.finlux.app.core.designsystem.FinluxBrandMark
import com.finlux.app.core.designsystem.LocalUiPreferences
import com.finlux.app.core.designsystem.WaterGlassCard
import com.finlux.app.domain.model.VisualStyle
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onAuthenticated: () -> Unit,
    onGuest: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val session = viewModel.session.collectAsStateWithLifecycle().value
    val style = LocalUiPreferences.current.visualStyle
    LaunchedEffect(session) {
        if (session == SessionState.CHECKING) return@LaunchedEffect
        delay(900)
        if (session == SessionState.AUTHENTICATED) onAuthenticated() else onGuest()
    }
    Box(Modifier.fillMaxSize()) {
        FinluxStyleBackdrop(Modifier.fillMaxSize(), auth = true)
        SplashArtwork(style)
        Column(
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FinluxBrandMark(size = 172.dp, framed = false)
            Spacer(Modifier.height(8.dp))
            Text("FinLux", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                "Quản lý tài chính thông minh",
                color = Color.White.copy(alpha = .82f),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(Modifier.size(25.dp), color = Color.White, strokeWidth = 2.dp)
            Spacer(Modifier.height(12.dp))
            Text("Đang tải dữ liệu...", color = Color.White.copy(alpha = .76f))
        }
    }
}

@Composable
private fun SplashArtwork(style: VisualStyle) {
    val primary = when (style) {
        VisualStyle.MODERN_DARK -> FinluxBlue
        VisualStyle.GLASSMORPHISM -> Color(0xFFA88AFF)
        VisualStyle.DYNAMIC_GRADIENT -> FinluxCyan
    }
    Canvas(Modifier.fillMaxSize()) {
        val center = Offset(size.width * .5f, size.height * .59f)
        drawCircle(
            brush = Brush.radialGradient(listOf(primary.copy(alpha = .28f), Color.Transparent), center, size.width * .42f),
            radius = size.width * .42f,
            center = center,
        )
        repeat(5) { index ->
            val x = size.width * (.13f + index * .19f)
            val y = size.height * (.70f + (index % 2) * .025f)
            drawCircle(Color.White.copy(alpha = .12f), radius = 3.dp.toPx() + index, center = Offset(x, y))
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
    val style = LocalUiPreferences.current.visualStyle
    val brandTextColor = if (style == VisualStyle.DYNAMIC_GRADIENT) MaterialTheme.colorScheme.onSurface else Color.White
    LaunchedEffect(state.completed) { if (state.completed) onCompleted() }

    Box(Modifier.fillMaxSize()) {
        FinluxStyleBackdrop(Modifier.fillMaxSize(), auth = style != VisualStyle.DYNAMIC_GRADIENT)
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding()
                .padding(horizontal = 20.dp, vertical = 34.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FinluxBrandMark(size = 64.dp)
                Column(Modifier.padding(start = 13.dp)) {
                    Text("FinLux", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = brandTextColor)
                    Text("Tài chính rõ ràng, cuộc sống nhẹ nhàng", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(28.dp))

            val form: @Composable () -> Unit = {
                Column {
                AuthFormContent(mode = mode, state = state, viewModel = viewModel, onNavigate = onNavigate, visualStyle = style)
                }
            }
            when (style) {
                VisualStyle.MODERN_DARK -> Box(
                    Modifier.fillMaxWidth().border(1.dp, FinluxBlue.copy(alpha = .28f), RoundedCornerShape(22.dp))
                        .padding(20.dp),
                ) { form() }
                VisualStyle.GLASSMORPHISM -> WaterGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    tint = FinluxPurple,
                    padding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                    cornerRadius = 24,
                ) { form() }
                VisualStyle.DYNAMIC_GRADIENT -> Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = .96f),
                    shadowElevation = 14.dp,
                ) { Box(Modifier.padding(20.dp)) { form() } }
            }
        }
    }
}

@Composable
private fun AuthFormContent(
    mode: AuthMode,
    state: AuthUiState,
    viewModel: AuthViewModel,
    onNavigate: (AuthMode) -> Unit,
    visualStyle: VisualStyle,
) {
    val titleColor = if (visualStyle == VisualStyle.DYNAMIC_GRADIENT) MaterialTheme.colorScheme.onSurface else Color.White
    Text(mode.heading, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = titleColor)
    Spacer(Modifier.height(6.dp))
    Text(mode.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(24.dp))
    if (mode == AuthMode.REGISTER) {
        FinluxField(state.displayName, viewModel::updateDisplayName, "Họ tên", leading = { Icon(Icons.Default.Person, null) })
        Spacer(Modifier.height(12.dp))
    }
    FinluxField(state.email, viewModel::updateEmail, "Email hoặc số điện thoại", KeyboardType.Email, leading = { Icon(Icons.Default.Email, null) })
    if (mode != AuthMode.FORGOT) {
        Spacer(Modifier.height(12.dp))
        FinluxField(state.password, viewModel::updatePassword, "Mật khẩu", isPassword = true, leading = { Icon(Icons.Default.Lock, null) })
    }
    if (mode == AuthMode.REGISTER) {
        Spacer(Modifier.height(12.dp))
        FinluxField(state.confirmPassword, viewModel::updateConfirmPassword, "Xác nhận mật khẩu", isPassword = true, leading = { Icon(Icons.Default.Lock, null) })
    }
    if (mode == AuthMode.LOGIN) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { onNavigate(AuthMode.FORGOT) }) { Text("Quên mật khẩu?") }
        }
    } else Spacer(Modifier.height(12.dp))
    state.error?.let {
        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(10.dp))
    }
    Button(
        onClick = when (mode) {
            AuthMode.LOGIN -> viewModel::signIn
            AuthMode.REGISTER -> viewModel::register
            AuthMode.FORGOT -> viewModel::resetPassword
        },
        modifier = Modifier.fillMaxWidth().height(52.dp),
        enabled = !state.isLoading,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        if (state.isLoading) CircularProgressIndicator(Modifier.size(21.dp), color = Color.White, strokeWidth = 2.dp)
        else Text(mode.action, fontWeight = FontWeight.Bold)
    }
    Spacer(Modifier.height(22.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        Text("  Mã hóa và đồng bộ an toàn  ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
    }
    Spacer(Modifier.height(14.dp))
    val footer = if (mode == AuthMode.LOGIN) "Chưa có tài khoản? Đăng ký ngay" else "Quay lại đăng nhập"
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        TextButton(onClick = { onNavigate(if (mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN) }) {
            Text(footer, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FinluxField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    leading: @Composable () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = leading,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        singleLine = true,
        shape = RoundedCornerShape(13.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = .70f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = .55f),
        ),
    )
}

enum class AuthMode(val title: String, val action: String, val heading: String, val description: String) {
    LOGIN("Đăng nhập", "Đăng nhập", "Chào mừng trở lại 👋", "Đăng nhập để tiếp tục quản lý tài chính của bạn"),
    REGISTER("Tạo tài khoản", "Tạo tài khoản", "Bắt đầu với FinLux", "Tạo tài khoản để đồng bộ tài chính an toàn"),
    FORGOT("Quên mật khẩu", "Gửi email khôi phục", "Khôi phục mật khẩu", "Nhập email để nhận liên kết đặt lại mật khẩu"),
}
