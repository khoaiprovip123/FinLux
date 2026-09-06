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
                    listOf(FinluxPalette.CFF0F172A, FinluxPalette.CFF1E1B4B, FinluxPalette.CFF311042)
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
                color = FinluxPalette.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(Modifier.size(24.dp), color = FinluxPalette.CFF6366F1, strokeWidth = 2.5.dp)
            Spacer(Modifier.height(12.dp))
            Text("Đang tải dữ liệu...", color = FinluxPalette.White.copy(alpha = 0.7f), fontSize = 13.sp)
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
                        ambientColor = FinluxPalette.C203B82F6,
                        spotColor = FinluxPalette.C304F46E5
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
                    .background(FinluxPalette.Black.copy(alpha = 0.35f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = FinluxPalette.CFF4F46E5)
            }
        }
    }
}
