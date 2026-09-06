package com.finlux.app.presentation.settings

import com.finlux.app.core.designsystem.theme.FinluxPalette

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
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.finlux.app.core.designsystem.component.FinluxSnackbarHost
import kotlinx.coroutines.launch
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
    selectedUiStyle: AppUiStyle = AppUiStyle.PRISM,
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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
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
            containerColor = FinluxPalette.Transparent,
            snackbarHost = { FinluxSnackbarHost(snackbarHostState, hasBottomBar = true) },
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
                            ProfileMenuRow(Icons.Default.TrendingUp, "Thương vụ & Đầu tư sinh lời") { onNavigate(Route.Deals.value) }
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
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Thiết bị chưa thiết lập hoặc không hỗ trợ sinh trắc học")
                                                }
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
                                                FinluxPalette.CFF3478F6,
                                                FinluxPalette.CFF7758F6,
                                                FinluxPalette.CFF47C8FF,
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
                            badgeColors = listOf(FinluxPalette.CFF3A7BFF, FinluxPalette.CFF6F52F5, FinluxPalette.CFF23C7E8),
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
                            badgeColors = listOf(FinluxPalette.CFF3478F6, FinluxPalette.CFF7758F6),
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
                            badgeColors = listOf(FinluxPalette.CFF0284C7, FinluxPalette.CFF0D9488),
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
