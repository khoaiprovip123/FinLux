package com.finlux.app.presentation.settings

import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.SolidColor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CameraAlt
import com.finlux.app.presentation.settings.salary.SalaryCycleSettingsSheet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.BuildConfig
import com.finlux.app.core.designsystem.FinluxBlue
import com.finlux.app.core.designsystem.FinluxBrandMark
import com.finlux.app.core.designsystem.FinluxCyan
import com.finlux.app.core.designsystem.FinluxPurple
import com.finlux.app.core.designsystem.FinluxStyleBackdrop
import com.finlux.app.core.designsystem.FinluxUserAvatar
import com.finlux.app.core.designsystem.GlassBottomSheet
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.GlassTopBar
import com.finlux.app.core.navigation.Route
import com.finlux.app.domain.model.AppUiStyle
import com.finlux.app.domain.model.CardDensity
import com.finlux.app.domain.model.GlassIntensity
import com.finlux.app.domain.model.ThemePreference
import com.finlux.app.domain.model.UiPreferences
import com.finlux.app.domain.model.VisualStyle
import com.finlux.app.presentation.components.MainBottomBar
import com.finlux.app.presentation.home.toVnd
import java.io.File
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

import com.finlux.app.presentation.settings.prism.PrismSettingsScreen

@Composable
fun SettingsScreen(
    selectedTheme: ThemePreference,
    onThemeSelected: (ThemePreference) -> Unit,
    selectedUiStyle: AppUiStyle = AppUiStyle.CLASSIC_LIQUID,
    onUiStyleSelected: (AppUiStyle) -> Unit = {},
    uiPreferences: UiPreferences,
    onUiPreferencesChanged: (UiPreferences) -> Unit,
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit,
    onSignedOut: () -> Unit,
    onCheckUpdate: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    if (selectedUiStyle == AppUiStyle.PRISM) {
        PrismSettingsScreen(
            selectedTheme = selectedTheme,
            onThemeSelected = onThemeSelected,
            selectedUiStyle = selectedUiStyle,
            onUiStyleSelected = onUiStyleSelected,
            uiPreferences = uiPreferences,
            onUiPreferencesChanged = onUiPreferencesChanged,
            onNavigate = onNavigate,
            onAdd = onAdd,
            onSignedOut = onSignedOut,
            onCheckUpdate = onCheckUpdate,
            viewModel = viewModel,
        )
        return
    }

    val user = viewModel.user.collectAsStateWithLifecycle().value
    val wallets = viewModel.wallets.collectAsStateWithLifecycle().value
    val totalAssets = wallets.sumOf { it.balance.value }
    val avatarState = viewModel.avatarState.collectAsStateWithLifecycle().value
    val nameState = viewModel.nameState.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    var showAvatarSource by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showNameEditor by remember { mutableStateOf(false) }
    var showUiStyleSheet by remember { mutableStateOf(false) }
    var showSalaryCycleSheet by remember { mutableStateOf(false) }
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
        AlertDialog(
            onDismissRequest = { showAvatarSource = false },
            title = { Text("Đổi ảnh đại diện", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AvatarSourceButton(Icons.Default.PhotoLibrary, "Chọn từ thư viện") {
                        showAvatarSource = false
                        galleryLauncher.launch("image/*")
                    }
                    AvatarSourceButton(Icons.Default.CameraAlt, "Chụp ảnh mới") {
                        showAvatarSource = false
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            openCamera()
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showAvatarSource = false }) { Text("Hủy") } },
        )
    }

    if (showNameEditor) {
        AlertDialog(
            onDismissRequest = { if (!nameState.isLoading) showNameEditor = false },
            title = { Text("Đổi tên hiển thị", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameDraft,
                        onValueChange = { nameDraft = it.take(40) },
                        label = { Text("Tên người dùng") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                    )
                    nameState.message?.let { message ->
                        Text(
                            message,
                            color = if (nameState.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.updateDisplayName(nameDraft) },
                    enabled = nameDraft.isNotBlank() && !nameState.isLoading,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    if (nameState.isLoading) CircularProgressIndicator(Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    else Text("Lưu tên", fontWeight = FontWeight.Bold)
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
        if (selectedUiStyle == AppUiStyle.MODERN_LUXURY) {
            com.finlux.app.core.designsystem.modern.FinluxStyleBackdrop(Modifier.fillMaxSize())
        } else {
            FinluxStyleBackdrop(Modifier.fillMaxSize())
        }

        Scaffold(
            topBar = {
                GlassTopBar(
                    title = {
                        Text(
                            "Hồ sơ & Cài đặt",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { onNavigate(Route.Home.value) }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    },
                )
            },
            containerColor = Color.Transparent,
        ) { padding ->
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
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
                avatarState.message?.let { message ->
                    item {
                        Text(
                            message,
                            color = if (avatarState.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        LaunchedEffect(message) { delay(2_500); viewModel.clearAvatarMessage() }
                    }
                }
                nameState.message?.takeIf { !showNameEditor }?.let { message ->
                    item {
                        Text(
                            message,
                            color = if (nameState.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        LaunchedEffect(message) { delay(2_500); viewModel.clearNameMessage() }
                    }
                }
                item { ProfileFeatureTiles(wallets.size, onNavigate) }
                item {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column {
                            ProfileMenuRow(Icons.Default.Edit, "Thông tin cá nhân") { openNameEditor() }
                            ProfileMenuRow(Icons.Default.AccountBalanceWallet, "Ví và tài khoản") { onNavigate(Route.Wallets.value) }
                            ProfileMenuRow(Icons.Default.Savings, "Ngân sách cá nhân") { onNavigate(Route.Budget.value) }
                            ProfileMenuRow(Icons.Default.CalendarMonth, "Tháng tài chính & Chu kỳ lương") { showSalaryCycleSheet = true }
                            ProfileMenuRow(Icons.Default.Category, "Quản lý danh mục") { onNavigate(Route.Categories.value) }
                            ProfileMenuRow(Icons.Default.Alarm, "Nhắc nhở thanh toán") { onNavigate(Route.Reminders.value) }
                            ProfileMenuRow(Icons.Default.NotificationsNone, "Thông báo") { onNavigate(Route.Notifications.value) }
                        }
                    }
                }
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showUiStyleSheet = true },
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("🎨", fontSize = 20.sp)
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        "Phong cách giao diện",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        selectedUiStyle.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Chọn phong cách giao diện",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            )
                        }
                    }
                }
                item {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Chế độ màu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ThemePreference.entries.forEach { option ->
                                    FilterChip(
                                        selected = selectedTheme == option,
                                        onClick = { onThemeSelected(option) },
                                        label = { Text(option.label, fontWeight = if (selectedTheme == option) FontWeight.Bold else FontWeight.Medium) },
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Phong cách Palette", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "Chọn tông màu chủ đạo cho vật liệu Liquid Glass",
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
                            Text("Tùy biến Liquid Glass", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Độ nổi và ánh màu của các thẻ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                GlassIntensity.entries.forEach { option ->
                                    FilterChip(
                                        selected = uiPreferences.glassIntensity == option,
                                        onClick = { onUiPreferencesChanged(uiPreferences.copy(glassIntensity = option)) },
                                        label = { Text(option.label) },
                                    )
                                }
                            }
                            Text("Mật độ nội dung", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                CardDensity.entries.forEach { option ->
                                    FilterChip(
                                        selected = uiPreferences.cardDensity == option,
                                        onClick = { onUiPreferencesChanged(uiPreferences.copy(cardDensity = option)) },
                                        label = { Text(option.label) },
                                    )
                                }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Hiệu ứng chạm thẻ", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Co nhẹ và phản hồi chuyển động", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(checked = uiPreferences.animationsEnabled, onCheckedChange = { onUiPreferencesChanged(uiPreferences.copy(animationsEnabled = it)) })
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Khóa ứng dụng bằng Sinh trắc học", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Yêu cầu vân tay / khuôn mặt khi mở ứng dụng", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = uiPreferences.biometricEnabled,
                                    onCheckedChange = { isEnabled ->
                                        if (isEnabled) {
                                            if (com.finlux.app.core.security.BiometricHelper.canAuthenticate(context)) {
                                                onUiPreferencesChanged(uiPreferences.copy(biometricEnabled = true))
                                            } else {
                                                android.widget.Toast.makeText(context, "Thiết bị chưa thiết lập hoặc không hỗ trợ sinh trắc học", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            onUiPreferencesChanged(uiPreferences.copy(biometricEnabled = false))
                                        }
                                    },
                                )
                            }
                            if (uiPreferences.biometricEnabled) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        "Thời gian tự động khóa",
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        com.finlux.app.domain.model.BiometricLockTimeout.entries.forEach { timeout ->
                                            val isSelected = timeout == uiPreferences.biometricTimeout
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .clickable { onUiPreferencesChanged(uiPreferences.copy(biometricTimeout = timeout)) },
                                            ) {
                                                Text(
                                                    text = timeout.label,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    ),
                                                    modifier = Modifier.padding(vertical = 8.dp),
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item { AboutFinluxCard(onCheckUpdate = onCheckUpdate) }
                item {
                    Button(
                        onClick = { viewModel.signOut(onSignedOut) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text("Đăng xuất", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            if (showSalaryCycleSheet) {
                SalaryCycleSettingsSheet(onDismiss = { showSalaryCycleSheet = false })
            }

            if (showUiStyleSheet) {
                GlassBottomSheet(onDismiss = { showUiStyleSheet = false }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        // Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color(0xFF3478F6),
                                                Color(0xFF7758F6),
                                                Color(0xFF47C8FF),
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("🎨", fontSize = 22.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Phong cách giao diện",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    "Tùy biến động lực học & hiệu ứng kính",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                        // Option 1: FinLux Prism (Data-first + Spatial + Bento)
                        UiStyleCard(
                            title = "FinLux Prism",
                            badge = "Prism 2026",
                            badgeColors = listOf(Color(0xFF3A7BFF), Color(0xFF6F52F5), Color(0xFF23C7E8)),
                            description = "Data-first, bố cục Bento, bề mặt Soft Surface mượt mà, tối giản hiệu ứng kính để tập trung dữ liệu.",
                            icon = "💎",
                            tags = listOf("📊 Data-First", "🍱 Bento Layout", "✨ Soft Surface", "⚡ Mượt mà"),
                            isSelected = selectedUiStyle == AppUiStyle.PRISM,
                            onClick = {
                                onUiStyleSelected(AppUiStyle.PRISM)
                                showUiStyleSheet = false
                            },
                        )

                        // Option 2: Modern Luxury (Công nghệ hiện đại)
                        UiStyleCard(
                            title = "Modern Luxury",
                            badge = "NextGen 2026",
                            badgeColors = listOf(Color(0xFF3478F6), Color(0xFF7758F6)),
                            description = "Kính lỏng đa tầng, bo tròn sang trọng, chuyển động sống động và công nghệ hiện đại.",
                            icon = "✨",
                            tags = listOf("💎 Kính lỏng 3D", "⚡ 120 FPS", "🌌 Hiệu ứng Aurora"),
                            isSelected = selectedUiStyle == AppUiStyle.MODERN_LUXURY,
                            onClick = {
                                onUiStyleSelected(AppUiStyle.MODERN_LUXURY)
                                showUiStyleSheet = false
                            },
                        )

                        // Option 3: Liquid Glass Classic (Cổ điển tinh gọn)
                        UiStyleCard(
                            title = "Liquid Glass",
                            badge = "Classic v1.5",
                            badgeColors = listOf(Color(0xFF0284C7), Color(0xFF0D9488)),
                            description = "Thiết kế thanh lịch, độ tương phản cao, tối ưu trực quan và tập trung hiệu năng.",
                            icon = "💧",
                            tags = listOf("🎯 Trực quan", "📊 Tương phản cao", "⚡ Siêu nhẹ"),
                            isSelected = selectedUiStyle == AppUiStyle.CLASSIC_LIQUID,
                            onClick = {
                                onUiStyleSelected(AppUiStyle.CLASSIC_LIQUID)
                                showUiStyleSheet = false
                            },
                        )

                        Spacer(Modifier.height(20.dp))
                    }
                }
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF2563EB),
                            Color(0xFF4F46E5),
                            Color(0xFF06B6D4),
                        ),
                    ),
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                FinluxUserAvatar(photoUrl, name, 76.dp, loading = loading, editable = true, onClick = onAvatar)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp)
                        .clickable(onClick = onEditName),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = name.ifBlank { "Người dùng" },
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Text(
                            text = "Premium",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(Color(0xFFFFD700).copy(alpha = 0.22f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                    if (email.isNotBlank()) {
                        Text(
                            text = email,
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Text(
                        text = "Chạm để chỉnh sửa thông tin",
                        color = Color.White.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
                IconButton(onClick = onEditName) {
                    Icon(Icons.Default.Edit, "Đổi tên người dùng", tint = Color.White)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.16f))
                    .border(1.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Tổng tài sản", color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.bodySmall)
                        Text(totalAssets.toVnd(), color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Quản lý tập trung và an toàn", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall)
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileFeatureTiles(walletCount: Int, onNavigate: (String) -> Unit) {
    val savingSpinAccent = MaterialTheme.colorScheme.primary
    val items = listOf(
        ProfileTile("Ví của tôi", "$walletCount ví", Icons.Default.AccountBalanceWallet, FinluxBlue, Route.Wallets.value),
        ProfileTile("Ngân sách", "Theo dõi", Icons.Default.Savings, FinluxPurple, Route.Budget.value),
        ProfileTile("Nợ & Tín dụng", "Thoát nợ", Icons.Default.CreditCard, Color(0xFFE11D48), Route.Debt.value),
        ProfileTile("Danh mục", "Tùy chỉnh", Icons.Default.Category, FinluxCyan, Route.Categories.value),
        ProfileTile("Nhắc nhở", "Định kỳ", Icons.Default.Alarm, Color(0xFFFF8A42), Route.Reminders.value),
        ProfileTile("Mục tiêu", "Tích lũy", Icons.Default.Savings, FinluxPurple, Route.Goals.value),
        ProfileTile("Vòng quay", "Tiết kiệm", Icons.Default.Casino, savingSpinAccent, Route.SavingSpinSettings.value),
    )
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(items) { item ->
            GlassCard(
                modifier = Modifier.width(96.dp).height(102.dp),
                onClick = { onNavigate(item.route) },
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(item.accent.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(item.icon, null, tint = item.accent, modifier = Modifier.size(20.dp))
                    }
                    Text(
                        item.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        item.subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

private data class ProfileTile(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val accent: Color,
    val route: String,
)

@Composable
private fun ProfileMenuRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun AboutFinluxCard(onCheckUpdate: () -> Unit) {
    GlassCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FinluxBrandMark(size = 64.dp)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Giới thiệu FinLux", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Phiên bản ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    Text("Tài chính rõ ràng, cuộc sống nhẹ nhàng", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                "FinLux giúp bạn quản lý thu chi, ví, ngân sách, mục tiêu và báo cáo trong một trải nghiệm thống nhất.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedButton(
                onClick = onCheckUpdate,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.SystemUpdate,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text("Kiểm tra bản cập nhật mới", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun AvatarSourceButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
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
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(Modifier.background(Brush.linearGradient(colors)).padding(12.dp)) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
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
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Đang chọn",
                        tint = Color(0xFF3478F6),
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun UiStyleCard(
    title: String,
    badge: String,
    badgeColors: List<Color>,
    description: String,
    icon: String,
    tags: List<String>,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.4f
    val activeBorderBrush = Brush.horizontalGradient(
        listOf(
            Color(0xFF3478F6),
            Color(0xFF7758F6),
            Color(0xFF47C8FF),
        )
    )
    val inactiveBorder = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) {
            if (isDark) Color(0xFF1E293B).copy(alpha = 0.85f) else Color(0xFFF0F6FF)
        } else {
            if (isDark) Color(0xFF131D2E).copy(alpha = 0.65f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        border = if (isSelected) BorderStroke(1.8.dp, activeBorderBrush) else inactiveBorder,
        shadowElevation = if (isSelected) 4.dp else 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Row 1: Icon, Title, Badge & Radio
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) {
                                Brush.linearGradient(badgeColors).copyColorAlpha(0.20f)
                            } else {
                                Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f), MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)))
                            }
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(icon, fontSize = 20.sp)
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Brush.horizontalGradient(badgeColors))
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }
                }

                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color(0xFF3478F6),
                        unselectedColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                )
            }

            // Row 2: Description
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
            )

            // Row 3: Feature Tech Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) Color(0xFF3478F6).copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color(0xFF3478F6) else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

private fun Brush.copyColorAlpha(alpha: Float): Brush = this

private val ThemePreference.label: String get() = when (this) {
    ThemePreference.LIGHT -> "Sáng"
    ThemePreference.DARK -> "Tối"
    ThemePreference.SYSTEM -> "Hệ thống"
}
private val AppUiStyle.label: String get() = when (this) {
    AppUiStyle.PRISM -> "FinLux Prism (Mới)"
    AppUiStyle.CLASSIC_LIQUID -> "Liquid Glass (Cổ điển)"
    AppUiStyle.MODERN_LUXURY -> "Modern Luxury (Hiện đại)"
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
