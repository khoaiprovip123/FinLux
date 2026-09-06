@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

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
internal fun ProfileHero(
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
        color = FinluxPalette.Transparent,
        border = BorderStroke(1.dp, FinluxPalette.White.copy(alpha = 0.25f)),
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            FinluxPalette.CFF2563EB,
                            FinluxPalette.CFF4F46E5,
                            FinluxPalette.CFF06B6D4,
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
                            color = FinluxPalette.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Text(
                            text = "Premium",
                            style = MaterialTheme.typography.labelSmall,
                            color = FinluxPalette.CFFFFD700,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(FinluxPalette.CFFFFD700.copy(alpha = 0.22f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                    if (email.isNotBlank()) {
                        Text(
                            text = email,
                            color = FinluxPalette.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Text(
                        text = "Chạm để chỉnh sửa thông tin",
                        color = FinluxPalette.White.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
                IconButton(onClick = onEditName) {
                    Icon(Icons.Default.Edit, "Đổi tên người dùng", tint = FinluxPalette.White)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(FinluxPalette.White.copy(alpha = 0.16f))
                    .border(1.dp, FinluxPalette.White.copy(alpha = 0.24f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Tổng tài sản", color = FinluxPalette.White.copy(alpha = 0.82f), style = MaterialTheme.typography.bodySmall)
                        Text(totalAssets.toVnd(), color = FinluxPalette.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Quản lý tập trung và an toàn", color = FinluxPalette.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall)
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(FinluxPalette.White.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = FinluxPalette.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ProfileFeatureTiles(walletCount: Int, onNavigate: (String) -> Unit) {
    val savingSpinAccent = MaterialTheme.colorScheme.primary
    val items = listOf(
        ProfileTile("Ví của tôi", "$walletCount ví", Icons.Default.AccountBalanceWallet, FinluxBlue, Route.Wallets.value),
        ProfileTile("Thương vụ", "Đầu tư / ROI", Icons.Default.TrendingUp, FinluxPalette.CFF10B981, Route.Deals.value),
        ProfileTile("Ngân sách", "Theo dõi", Icons.Default.Savings, FinluxPurple, Route.Budget.value),
        ProfileTile("Nợ & Tín dụng", "Thoát nợ", Icons.Default.CreditCard, FinluxPalette.CFFE11D48, Route.Debt.value),
        ProfileTile("Danh mục", "Tùy chỉnh", Icons.Default.Category, FinluxCyan, Route.Categories.value),
        ProfileTile("Nhắc nhở", "Định kỳ", Icons.Default.Alarm, FinluxPalette.CFFFF8A42, Route.Reminders.value),
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

internal data class ProfileTile(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val accent: Color,
    val route: String,
)

@Composable
internal fun ProfileMenuRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
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
internal fun AboutFinluxCard(onCheckUpdate: () -> Unit) {
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
internal fun AvatarSourceButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
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

internal fun createCameraUri(context: Context): Uri {
    val directory = File(context.cacheDir, "avatar-capture").apply { mkdirs() }
    val file = File.createTempFile("finlux-avatar-", ".jpg", directory)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
internal fun VisualStylePreview(option: VisualStyle, selected: Boolean, onClick: () -> Unit) {
    val colors = when (option) {
        VisualStyle.MODERN_DARK -> listOf(FinluxPalette.CFF020D1E, FinluxPalette.CFF0B2848, FinluxPalette.CFF087FE6)
        VisualStyle.GLASSMORPHISM -> listOf(FinluxPalette.CFF264990, FinluxPalette.CFF7457CE, FinluxPalette.CFF54B5E8)
        VisualStyle.DYNAMIC_GRADIENT -> listOf(FinluxPalette.CFF8B28F7, FinluxPalette.CFF4E56FF, FinluxPalette.CFF14D1D0)
    }
    Surface(
        modifier = Modifier.width(150.dp).height(116.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = FinluxPalette.Transparent,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(Modifier.background(Brush.linearGradient(colors)).padding(12.dp)) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    repeat(3) { index ->
                        Box(Modifier.weight(1f).height(if (index == 0) 31.dp else 23.dp).background(FinluxPalette.White.copy(alpha = if (option == VisualStyle.MODERN_DARK) .08f else .20f), RoundedCornerShape(7.dp)))
                    }
                }
                Column {
                    Text(option.label, color = FinluxPalette.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    Text(option.description, color = FinluxPalette.White.copy(alpha = .78f), style = MaterialTheme.typography.labelSmall)
                }
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                        .background(FinluxPalette.White, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Đang chọn",
                        tint = FinluxPalette.CFF3478F6,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

@Composable
internal fun UiStyleCard(
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
            FinluxPalette.CFF3478F6,
            FinluxPalette.CFF7758F6,
            FinluxPalette.CFF47C8FF,
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
            if (isDark) FinluxPalette.CFF1E293B.copy(alpha = 0.85f) else FinluxPalette.CFFF0F6FF
        } else {
            if (isDark) FinluxPalette.CFF131D2E.copy(alpha = 0.65f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
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
                                color = FinluxPalette.White,
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
                        selectedColor = FinluxPalette.CFF3478F6,
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
                                if (isSelected) FinluxPalette.CFF3478F6.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) FinluxPalette.CFF3478F6 else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

internal fun Brush.copyColorAlpha(alpha: Float): Brush = this

internal val ThemePreference.label: String get() = when (this) {
    ThemePreference.LIGHT -> "Sáng"
    ThemePreference.DARK -> "Tối"
    ThemePreference.SYSTEM -> "Hệ thống"
}
internal val AppUiStyle.label: String get() = when (this) {
    AppUiStyle.PRISM -> "FinLux Prism (Mới)"
    AppUiStyle.CLASSIC_LIQUID -> "Liquid Glass (Cổ điển)"
    AppUiStyle.MODERN_LUXURY -> "Modern Luxury (Hiện đại)"
}
internal val GlassIntensity.label: String get() = when (this) {
    GlassIntensity.SOFT -> "Nhẹ"
    GlassIntensity.BALANCED -> "Cân bằng"
    GlassIntensity.VIVID -> "Rực rỡ"
}
internal val CardDensity.label: String get() = when (this) {
    CardDensity.COMFORTABLE -> "Thoáng"
    CardDensity.COMPACT -> "Gọn"
}
internal val VisualStyle.label: String get() = when (this) {
    VisualStyle.MODERN_DARK -> "Tối giản hiện đại"
    VisualStyle.GLASSMORPHISM -> "Glassmorphism"
    VisualStyle.DYNAMIC_GRADIENT -> "Gradient năng động"
}
internal val VisualStyle.description: String get() = when (this) {
    VisualStyle.MODERN_DARK -> "Navy + ánh xanh"
    VisualStyle.GLASSMORPHISM -> "Kính mờ + glow"
    VisualStyle.DYNAMIC_GRADIENT -> "Tím + cyan"
}
