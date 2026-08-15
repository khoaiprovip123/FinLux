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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextOverflow
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
import com.finlux.app.core.designsystem.GlassAlertDialog
import com.finlux.app.core.designsystem.GlassTopBar
import com.finlux.app.domain.model.CardDensity
import com.finlux.app.domain.model.GlassIntensity
import com.finlux.app.domain.model.ThemePreference
import com.finlux.app.domain.model.UiPreferences
import com.finlux.app.domain.model.VisualStyle
import com.finlux.app.presentation.components.MainBottomBar
import com.finlux.app.presentation.home.toVnd
import com.finlux.app.core.designsystem.FinluxBlue
import com.finlux.app.core.designsystem.FinluxCyan
import com.finlux.app.core.designsystem.FinluxPurple
import com.finlux.app.core.navigation.Route
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
    val wallets = viewModel.wallets.collectAsStateWithLifecycle().value
    val totalAssets = wallets.sumOf { it.balance.value }
    val avatarState = viewModel.avatarState.collectAsStateWithLifecycle().value
    val nameState = viewModel.nameState.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    var showAvatarSource by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showNameEditor by remember { mutableStateOf(false) }
    var nameDraft by remember(user?.uid) { mutableStateOf(user?.displayName.orEmpty()) }

    fun openNameEditor() {
        nameDraft = user?.displayName.orEmpty()
        viewModel.clearNameMessage()
        showNameEditor = true
    }

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
        GlassAlertDialog(
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

    if (showNameEditor) {
        GlassAlertDialog(
            onDismissRequest = { if (!nameState.isLoading) showNameEditor = false },
            icon = { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Đổi tên người dùng") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tên mới sẽ hiển thị đồng bộ ở Trang chủ và Hồ sơ.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = nameDraft,
                        onValueChange = { nameDraft = it; viewModel.clearNameMessage() },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tên người dùng") },
                        singleLine = true,
                        isError = nameState.isError,
                        supportingText = nameState.message?.let { message -> { Text(message) } },
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.updateDisplayName(nameDraft) }, enabled = !nameState.isLoading) {
                    if (nameState.isLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Text("Lưu tên")
                }
            },
            dismissButton = { TextButton(onClick = { showNameEditor = false }, enabled = !nameState.isLoading) { Text("Hủy") } },
        )
    }
    LaunchedEffect(nameState.message, nameState.isError) {
        if (nameState.message != null && !nameState.isError) {
            delay(650)
            showNameEditor = false
        }
    }

    Box(Modifier.fillMaxSize()) {
        com.finlux.app.core.designsystem.FinluxStyleBackdrop(Modifier.fillMaxSize())
        Scaffold(
            topBar = {
                GlassTopBar(
                    title = { Text("Hồ sơ & Cài đặt", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = { IconButton(onClick = { onNavigate(Route.Home.value) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") } },
                )
            },
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
                    ProfileHero(
                        name = user?.displayName ?: "Người dùng",
                        email = user?.email.orEmpty(),
                        photoUrl = user?.photoUrl,
                        loading = avatarState.isLoading,
                        totalAssets = totalAssets,
                        onAvatar = { showAvatarSource = true },
                        onEditName = ::openNameEditor,
                    )
                }
                avatarState.message?.let { message -> item {
                    Text(message, color = if (avatarState.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                    LaunchedEffect(message) { delay(2_500); viewModel.clearAvatarMessage() }
                } }
                nameState.message?.takeIf { !showNameEditor }?.let { message -> item {
                    Text(message, color = if (nameState.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                    LaunchedEffect(message) { delay(2_500); viewModel.clearNameMessage() }
                } }
                item { ProfileFeatureTiles(wallets.size, onNavigate) }
                item {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column {
                            ProfileMenuRow(Icons.Default.Edit, "Thông tin cá nhân") { openNameEditor() }
                            ProfileMenuRow(Icons.Default.AccountBalanceWallet, "Ví và tài khoản") { onNavigate(Route.Wallets.value) }
                            ProfileMenuRow(Icons.Default.Savings, "Ngân sách cá nhân") { onNavigate(Route.Budget.value) }
                            ProfileMenuRow(Icons.Default.Category, "Quản lý danh mục") { onNavigate(Route.Categories.value) }
                            ProfileMenuRow(Icons.Default.Alarm, "Nhắc nhở thanh toán") { onNavigate(Route.Reminders.value) }
                            ProfileMenuRow(Icons.Default.NotificationsNone, "Thông báo") { onNavigate(Route.Notifications.value) }
                        }
                    }
                }

                item {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Tùy biến Liquid Glass", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Phong cách giao diện", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                items(VisualStyle.entries) { option ->
                                    com.finlux.app.core.designsystem.LiquidGlassCapsule(
                                        selected = uiPreferences.visualStyle == option,
                                        onClick = { onUiPreferencesChanged(uiPreferences.copy(visualStyle = option)) },
                                        accentColor = MaterialTheme.colorScheme.primary,
                                    ) {
                                        Text(option.label, style = MaterialTheme.typography.labelMedium, fontWeight = if (uiPreferences.visualStyle == option) FontWeight.Bold else FontWeight.Medium)
                                    }
                                }
                            }
                            Text("Độ nổi và ánh màu của các thẻ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                GlassIntensity.entries.forEach { option ->
                                    com.finlux.app.core.designsystem.LiquidGlassCapsule(
                                        selected = uiPreferences.glassIntensity == option,
                                        onClick = { onUiPreferencesChanged(uiPreferences.copy(glassIntensity = option)) },
                                        accentColor = MaterialTheme.colorScheme.primary,
                                    ) {
                                        Text(option.label, style = MaterialTheme.typography.labelMedium, fontWeight = if (uiPreferences.glassIntensity == option) FontWeight.Bold else FontWeight.Medium)
                                    }
                                }
                            }
                            Text("Mật độ nội dung", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                CardDensity.entries.forEach { option ->
                                    com.finlux.app.core.designsystem.LiquidGlassCapsule(
                                        selected = uiPreferences.cardDensity == option,
                                        onClick = { onUiPreferencesChanged(uiPreferences.copy(cardDensity = option)) },
                                        accentColor = MaterialTheme.colorScheme.primary,
                                    ) {
                                        Text(option.label, style = MaterialTheme.typography.labelMedium, fontWeight = if (uiPreferences.cardDensity == option) FontWeight.Bold else FontWeight.Medium)
                                    }
                                }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Hiệu ứng chạm thẻ", fontWeight = FontWeight.Medium)
                                    Text("Co nhẹ và phản hồi đàn hồi (Spring physics)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(uiPreferences.animationsEnabled, { onUiPreferencesChanged(uiPreferences.copy(animationsEnabled = it)) })
                            }
                        }
                    }
                }
                item {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Chế độ màu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ThemePreference.entries.forEach { option ->
                                    com.finlux.app.core.designsystem.LiquidGlassCapsule(
                                        selected = selectedTheme == option,
                                        onClick = { onThemeSelected(option) },
                                        accentColor = MaterialTheme.colorScheme.primary,
                                    ) {
                                        Text(option.label, style = MaterialTheme.typography.labelMedium, fontWeight = if (selectedTheme == option) FontWeight.Bold else FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
                item { Button(onClick = { viewModel.signOut(onSignedOut) }, modifier = Modifier.fillMaxWidth()) { Text("Đăng xuất") } }
            }
        }
    }
}

@Composable
private fun ProfileHero(
    name: String,
    email: String,
    photoUrl: String?,
    loading: Boolean,
    totalAssets: Long,
    onAvatar: () -> Unit,
    onEditName: () -> Unit,
) {
    GlassCard(
        Modifier.fillMaxWidth(),
        mode = com.finlux.app.core.designsystem.LiquidGlassMode.CLEAR,
        tint = FinluxPurple,
        padding = androidx.compose.foundation.layout.PaddingValues(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FinluxUserAvatar(photoUrl, name, 80.dp, loading = loading, editable = true, onClick = onAvatar)
                Column(Modifier.weight(1f).padding(start = 14.dp).clickable(onClick = onEditName)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(name, Modifier.weight(1f, fill = false), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(" Premium ", Modifier.padding(start = 8.dp).background(Color(0xFFFFB547).copy(alpha = .24f), RoundedCornerShape(8.dp)).padding(horizontal = 5.dp, vertical = 2.dp), color = Color(0xFFFFD37A), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Text(email, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Chạm tên để thay đổi", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
                IconButton(onClick = onEditName) { Icon(Icons.Default.Edit, "Đổi tên người dùng", tint = MaterialTheme.colorScheme.onSurface) }
            }
            GlassCard(
                Modifier.fillMaxWidth(),
                mode = com.finlux.app.core.designsystem.LiquidGlassMode.CLEAR,
                tint = FinluxBlue,
                shape = RoundedCornerShape(18.dp),
                padding = androidx.compose.foundation.layout.PaddingValues(14.dp),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Tổng tài sản", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(totalAssets.toVnd(), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("Quản lý tập trung và an toàn", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                    }
                    FinluxBrandMark(size = 40.dp, framed = false)
                }
            }
        }
    }
}

@Composable
private fun ProfileFeatureTiles(walletCount: Int, onNavigate: (String) -> Unit) {
    val items = listOf(
        ProfileTile("Ví của tôi", "$walletCount ví", Icons.Default.AccountBalanceWallet, FinluxBlue, Route.Wallets.value),
        ProfileTile("Ngân sách", "Theo dõi", Icons.Default.Savings, FinluxPurple, Route.Budget.value),
        ProfileTile("Danh mục", "Tùy chỉnh", Icons.Default.Category, FinluxCyan, Route.Categories.value),
        ProfileTile("Nhắc nhở", "Định kỳ", Icons.Default.Alarm, Color(0xFFFF8A42), Route.Reminders.value),
        ProfileTile("Mục tiêu", "Tích lũy", Icons.Default.Savings, FinluxPurple, Route.Goals.value),
    )
    LazyRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(items) { item ->
            GlassCard(Modifier.width(96.dp).height(106.dp), onClick = { onNavigate(item.route) }) {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(item.accent.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(item.icon, null, tint = item.accent, modifier = Modifier.size(20.dp))
                    }
                    Text(item.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    Text(item.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

private data class ProfileTile(val title: String, val subtitle: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val accent: Color, val route: String)

@Composable
private fun ProfileMenuRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(21.dp), tint = MaterialTheme.colorScheme.primary)
        Text(label, Modifier.weight(1f).padding(horizontal = 13.dp), fontWeight = FontWeight.Medium)
        Icon(Icons.Default.ChevronRight, null, Modifier.size(19.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        mode = com.finlux.app.core.designsystem.LiquidGlassMode.REGULAR,
        shape = RoundedCornerShape(14.dp),
        padding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        onClick = onClick,
    ) {
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
