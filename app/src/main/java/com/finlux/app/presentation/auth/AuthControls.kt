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
internal fun PasswordStrengthBar(
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
            color = FinluxPalette.CFF64748B
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(4) { index ->
                val active = index < score
                val activeColor = when (score) {
                    1 -> FinluxPalette.CFFEF4444
                    2 -> FinluxPalette.CFFF59E0B
                    3 -> FinluxPalette.CFF10B981
                    4 -> FinluxPalette.CFF059669
                    else -> FinluxPalette.CFFCBD5E1
                }
                Box(
                    modifier = Modifier
                        .width(26.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (active) activeColor else FinluxPalette.CFFE2E8F0)
                )
            }

            Spacer(Modifier.width(6.dp))

            Text(
                text = strengthText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = when (score) {
                    1 -> FinluxPalette.CFFEF4444
                    2 -> FinluxPalette.CFFF59E0B
                    3 -> FinluxPalette.CFF10B981
                    4 -> FinluxPalette.CFF059669
                    else -> FinluxPalette.CFF64748B
                }
            )
        }
    }
}

@Composable
internal fun FinluxInput(
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
                color = FinluxPalette.CFF94A3B8,
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = FinluxPalette.CFF94A3B8,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Hiển thị mật khẩu",
                        tint = FinluxPalette.CFF94A3B8,
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
            focusedBorderColor = FinluxPalette.CFF4F46E5,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        )
    )
}

@Composable
internal fun GradientButton(
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
                ambientColor = FinluxPalette.C307C3AED,
                spotColor = FinluxPalette.C405B21B6,
            ),
        enabled = !isLoading,
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(containerColor = FinluxPalette.Transparent),
        contentPadding = androidx.compose.foundation.layout.PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(FinluxPalette.CFF5B2BFF, FinluxPalette.CFF7C2CFF),
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = FinluxPalette.White,
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinluxPalette.White,
                )
            }
        }
    }
}

@Composable
internal fun LoginBottomWave() {
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
internal fun SocialDivider(text: String) {
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
internal fun SocialLoginRow(onSocialClick: (SocialAuthProvider) -> Unit) {
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
internal fun SocialCard(
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
            color = if (isDark) FinluxPalette.White else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Lux",
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            color = FinluxPalette.CFF3478F6
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
