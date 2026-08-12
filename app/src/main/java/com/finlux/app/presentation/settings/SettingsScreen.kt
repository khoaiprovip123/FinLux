package com.finlux.app.presentation.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.BuildConfig
import com.finlux.app.core.designsystem.FinluxBrandMark
import com.finlux.app.core.designsystem.FinluxUserAvatar
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.GlassTopBar
import com.finlux.app.domain.model.CardDensity
import com.finlux.app.domain.model.GlassIntensity
import com.finlux.app.domain.model.ThemePreference
import com.finlux.app.domain.model.UiPreferences
import com.finlux.app.domain.model.VisualStyle
import com.finlux.app.presentation.components.MainBottomBar
import java.io.File
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    selectedTheme: ThemePreference,
    onThemeSelected: (ThemePreference) -> Unit,
    uiPreferences: UiPreferences,
    onUiPreferencesChanged: (UiPreferences) -> Unit,
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val user = viewModel.user.collectAsStateWithLifecycle().value
    val avatarState = viewModel.avatarState.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    var showAvatarSource by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(viewModel::updateAvatar)
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        if (saved) pendingCameraUri?.let(viewModel::updateAvatar)
    }
    fun openCamera() {
        createCameraUri(context).also {
            pendingCameraUri = it
            cameraLauncher.launch(it)
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) openCamera()
    }

    if (showAvatarSource) {
        AlertDialog(
            onDismissRequest = { showAvatarSource = false },
            title = { Text("Đổi ảnh đại diện") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AvatarSourceButton(Icons.Default.PhotoLibrary, "Chọn từ thư viện") {
                        showAvatarSource = false
                        galleryLauncher.launch("image/*")
                    }
                    AvatarSourceButton(Icons.Default.CameraAlt, "Chụp ảnh mới") {
                        showAvatarSource = false
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            openCamera()
                        } else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showAvatarSource = false }) { Text("Đóng") } },
        )
    }

    Scaffold(
        topBar = { GlassTopBar(title = { Text("Cài đặt") }) },
        bottomBar = { MainBottomBar("settings", onNavigate, onAdd) },
        containerColor = Color.Transparent,
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { AboutFinluxCard() }
            item {
                GlassCard(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Hồ sơ người dùng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FinluxUserAvatar(
                                photoUrl = user?.photoUrl,
                                displayName = user?.displayName ?: "Người dùng",
                                size = 88.dp,
                                loading = avatarState.isLoading,
                                editable = true,
                                onClick = { showAvatarSource = true },
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(user?.displayName ?: "Người dùng", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(user?.email.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "Chạm vào ảnh để đặt hoặc đổi ảnh đại diện",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        avatarState.message?.let { message ->
                            Text(
                                message,
                                color = if (avatarState.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            LaunchedEffect(message) {
                                delay(2_500)
                                viewModel.clearAvatarMessage()
                            }
                        }
                    }
                }
            }
            item {
                GlassCard(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Phong cách giao diện", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Chọn một diện mạo; FinLux sẽ áp dụng đồng bộ cho toàn bộ ứng dụng.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(VisualStyle.entries) { option ->
                                VisualStylePreview(option, uiPreferences.visualStyle == option) {
                                    onUiPreferencesChanged(uiPreferences.copy(visualStyle = option))
                                }
                            }
                        }
                    }
                }
            }
            item {
                GlassCard(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Tùy biến Liquid Glass", style = MaterialTheme.typography.titleMedium)
                        Text("Độ nổi và ánh màu của các thẻ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            GlassIntensity.entries.forEach { option ->
                                FilterChip(uiPreferences.glassIntensity == option, { onUiPreferencesChanged(uiPreferences.copy(glassIntensity = option)) }, { Text(option.label) })
                            }
                        }
                        Text("Mật độ nội dung", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            CardDensity.entries.forEach { option ->
                                FilterChip(uiPreferences.cardDensity == option, { onUiPreferencesChanged(uiPreferences.copy(cardDensity = option)) }, { Text(option.label) })
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Hiệu ứng chạm thẻ")
                                Text("Co nhẹ và phản hồi chuyển động", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(uiPreferences.animationsEnabled, { onUiPreferencesChanged(uiPreferences.copy(animationsEnabled = it)) })
                        }
                    }
                }
            }
            item {
                GlassCard(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Chế độ màu", style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemePreference.entries.forEach { option ->
                                FilterChip(selectedTheme == option, { onThemeSelected(option) }, { Text(option.label) })
                            }
                        }
                    }
                }
            }
            item { SettingsLink("Quản lý Danh mục") { onNavigate("categories") } }
            item { SettingsLink("Quản lý Ví") { onNavigate("wallets") } }
            item { SettingsLink("Nhắc nhở định kỳ") { onNavigate("reminders") } }
            item { Button(onClick = { viewModel.signOut(onSignedOut) }, modifier = Modifier.fillMaxWidth()) { Text("Đăng xuất") } }
        }
    }
}

@Composable
private fun AboutFinluxCard() {
    GlassCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FinluxBrandMark(size = 82.dp)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("Giới thiệu FinLux", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Phiên bản ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Text("Tài chính rõ ràng, cuộc sống nhẹ nhàng", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                "FinLux giúp anh quản lý thu chi, ví, ngân sách, mục tiêu và báo cáo trong một trải nghiệm thống nhất.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AvatarSourceButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(label, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun createCameraUri(context: Context): Uri {
    val directory = File(context.cacheDir, "avatar-capture").apply { mkdirs() }
    val file = File.createTempFile("finlux-avatar-", ".jpg", directory)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
private fun VisualStylePreview(option: VisualStyle, selected: Boolean, onClick: () -> Unit) {
    val colors = when (option) {
        VisualStyle.MODERN_DARK -> listOf(Color(0xFF020D1E), Color(0xFF0B2848), Color(0xFF087FE6))
        VisualStyle.GLASSMORPHISM -> listOf(Color(0xFF264990), Color(0xFF7457CE), Color(0xFF54B5E8))
        VisualStyle.DYNAMIC_GRADIENT -> listOf(Color(0xFF8B28F7), Color(0xFF4E56FF), Color(0xFF14D1D0))
    }
    Surface(
        modifier = Modifier.width(150.dp).height(116.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.background(Brush.linearGradient(colors)).padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(3) { index ->
                    Box(Modifier.weight(1f).height(if (index == 0) 31.dp else 23.dp).background(Color.White.copy(alpha = if (option == VisualStyle.MODERN_DARK) .08f else .20f), RoundedCornerShape(7.dp)))
                }
            }
            Column {
                Text(option.label, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                Text(option.description, color = Color.White.copy(alpha = .78f), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun SettingsLink(label: String, onClick: () -> Unit) {
    GlassCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) { Text(label, style = MaterialTheme.typography.titleMedium) }
}

private val ThemePreference.label: String get() = when (this) {
    ThemePreference.LIGHT -> "Sáng"
    ThemePreference.DARK -> "Tối"
    ThemePreference.SYSTEM -> "Hệ thống"
}
private val GlassIntensity.label: String get() = when (this) {
    GlassIntensity.SOFT -> "Nhẹ"
    GlassIntensity.BALANCED -> "Cân bằng"
    GlassIntensity.VIVID -> "Rực rỡ"
}
private val CardDensity.label: String get() = when (this) {
    CardDensity.COMFORTABLE -> "Thoáng"
    CardDensity.COMPACT -> "Gọn"
}
private val VisualStyle.label: String get() = when (this) {
    VisualStyle.MODERN_DARK -> "Tối giản hiện đại"
    VisualStyle.GLASSMORPHISM -> "Glassmorphism"
    VisualStyle.DYNAMIC_GRADIENT -> "Gradient năng động"
}
private val VisualStyle.description: String get() = when (this) {
    VisualStyle.MODERN_DARK -> "Navy + ánh xanh"
    VisualStyle.GLASSMORPHISM -> "Kính mờ + glow"
    VisualStyle.DYNAMIC_GRADIENT -> "Tím + cyan"
}
