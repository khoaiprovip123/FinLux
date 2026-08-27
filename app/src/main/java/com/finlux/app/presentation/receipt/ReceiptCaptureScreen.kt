package com.finlux.app.presentation.receipt

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.FinluxCyan
import com.finlux.app.core.designsystem.FinluxPurple
import com.finlux.app.core.designsystem.FinluxStyleBackdrop
import com.finlux.app.core.designsystem.WaterGlassCard
import java.io.File

@Composable
fun ReceiptCaptureScreen(onDismiss: () -> Unit, onCaptured: (String) -> Unit) {
    val context = LocalContext.current
    val tokens = LocalFinluxTokens.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { it?.let { uri -> onCaptured(uri.toString()) } }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved -> if (saved) cameraUri?.let { onCaptured(it.toString()) } }
    fun launchCamera() {
        createReceiptUri(context).also { uri -> cameraUri = uri; camera.launch(uri) }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) launchCamera() }
    BackHandler(onBack = onDismiss)
    Box(Modifier.fillMaxSize()) {
        FinluxStyleBackdrop(Modifier.fillMaxSize())
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onDismiss) { Icon(Icons.Default.Close, "Đóng", tint = tokens.onSurface) }
                Text("Quét hóa đơn", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = tokens.onSurface)
                Icon(Icons.Default.Bolt, null, tint = tokens.primary)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Box(
                    Modifier.fillMaxWidth().size(300.dp).clip(RoundedCornerShape(28.dp))
                        .background(Brush.verticalGradient(listOf(tokens.surface.copy(alpha = .36f), tokens.surfaceSoft.copy(alpha = .60f))))
                        .border(2.dp, Brush.linearGradient(listOf(tokens.primary, tokens.primary.copy(alpha = 0.5f))), RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CameraAlt, null, Modifier.size(72.dp), tint = tokens.primary)
                        Text("Đưa toàn bộ hóa đơn vào khung", color = tokens.onSurface, fontWeight = FontWeight.Bold)
                        Text("Giữ điện thoại ổn định và đủ sáng", color = tokens.onSurfaceVariant)
                    }
                }
                Text("Sau khi chụp, ảnh được đính kèm vào form Thêm chi để anh kiểm tra và nhập số tiền chính xác.", color = tokens.onSurfaceVariant)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                WaterGlassCard(Modifier.size(60.dp), tint = tokens.primary, onClick = { gallery.launch("image/*") }, padding = androidx.compose.foundation.layout.PaddingValues(0.dp)) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Default.PhotoLibrary, "Thư viện", tint = tokens.primary) } }
                Box(Modifier.size(82.dp).background(Brush.linearGradient(listOf(tokens.primary, tokens.primary.copy(alpha = 0.8f))), CircleShape).border(3.dp, tokens.border, CircleShape), contentAlignment = Alignment.Center) {
                    IconButton(onClick = { if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) launchCamera() else permission.launch(Manifest.permission.CAMERA) }, modifier = Modifier.fillMaxSize()) { Icon(Icons.Default.CameraAlt, "Chụp", Modifier.size(36.dp), tint = Color.White) }
                }
                Box(Modifier.size(60.dp))
            }
        }
    }
}

private fun createReceiptUri(context: Context): Uri {
    val file = File(context.cacheDir, "receipt-${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
