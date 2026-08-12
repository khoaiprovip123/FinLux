package com.finlux.app.presentation.notifications

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.finlux.app.core.designsystem.GlassTopBar

@Composable
fun NotificationsScreen() {
    Scaffold(topBar = { GlassTopBar(title = { Text("Thông báo") }) }, containerColor = Color.Transparent) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("Chưa có thông báo")
        }
    }
}
