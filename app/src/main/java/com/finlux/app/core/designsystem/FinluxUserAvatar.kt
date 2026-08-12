package com.finlux.app.core.designsystem

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FinluxUserAvatar(
    photoUrl: String?,
    displayName: String,
    size: Dp,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    editable: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val image by produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, photoUrl) {
        value = photoUrl?.takeIf(String::isNotBlank)?.let { source ->
            withContext(Dispatchers.IO) {
                runCatching {
                    val stream = if (source.startsWith("http")) URL(source).openStream()
                    else context.contentResolver.openInputStream(Uri.parse(source)) ?: error("Không đọc được ảnh")
                    stream.use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
                }.getOrNull()
            }
        }
    }
    val interactive = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)
    Box(
        modifier.size(size).clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = .14f)).then(interactive),
        contentAlignment = Alignment.Center,
    ) {
        if (image != null) {
            Image(image!!, "Ảnh đại diện của $displayName", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Text(
                displayName.trim().firstOrNull()?.uppercase() ?: "F",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (loading) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .42f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(size * .34f), color = Color.White, strokeWidth = 3.dp)
            }
        } else if (editable) {
            Surface(Modifier.align(Alignment.BottomEnd).size(size * .34f), shape = CircleShape, color = MaterialTheme.colorScheme.primary, shadowElevation = 4.dp) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Edit, "Đổi ảnh đại diện", Modifier.size(size * .18f), tint = Color.White)
                }
            }
        }
    }
}
