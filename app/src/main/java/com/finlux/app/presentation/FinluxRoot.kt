package com.finlux.app.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinluxStyleBackdrop
import com.finlux.app.core.designsystem.FinluxTheme
import com.finlux.app.core.navigation.FinluxNavHost
import com.finlux.app.core.security.BiometricHelper
import kotlinx.coroutines.flow.MutableStateFlow

/** Root is the only place where theme preference enters Composition (AGENTS.md rule 5). */
@Composable
fun FinluxRoot(
    activity: FragmentActivity? = null,
    viewModel: RootViewModel = hiltViewModel(),
    destinationFlow: MutableStateFlow<String?>? = null,
    payNotificationIdFlow: MutableStateFlow<String?>? = null,
) {
    val theme = viewModel.theme.collectAsStateWithLifecycle().value
    val uiStyle = viewModel.uiStyle.collectAsStateWithLifecycle().value
    val uiPreferences = viewModel.uiPreferences.collectAsStateWithLifecycle().value

    var isUnlocked by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiPreferences.biometricEnabled) {
        if (uiPreferences.biometricEnabled && !isUnlocked && activity != null) {
            BiometricHelper.showPrompt(
                activity = activity,
                onSuccess = { isUnlocked = true },
            )
        }
    }

    FinluxTheme(
        preference = theme,
        uiStyle = uiStyle,
        uiPreferences = uiPreferences,
    ) {
        androidx.compose.material3.Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            if (uiPreferences.biometricEnabled && !isUnlocked) {
                BiometricLockScreen(
                    onUnlockClick = {
                        activity?.let {
                            BiometricHelper.showPrompt(
                                activity = it,
                                onSuccess = { isUnlocked = true },
                            )
                        }
                    }
                )
            } else {
                FinluxNavHost(
                    selectedTheme = theme,
                    onThemeSelected = viewModel::setTheme,
                    selectedUiStyle = uiStyle,
                    onUiStyleSelected = viewModel::setUiStyle,
                    uiPreferences = uiPreferences,
                    onUiPreferencesChanged = viewModel::setUiPreferences,
                    destinationFlow = destinationFlow,
                    payNotificationIdFlow = payNotificationIdFlow,
                )
            }
        }
    }
}

@Composable
private fun BiometricLockScreen(
    onUnlockClick: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        FinluxStyleBackdrop(Modifier.fillMaxSize())

        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Khóa bảo vệ",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp),
                )
            }

            Text(
                text = "FinLux được bảo vệ",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = "Xác thực sinh trắc học để tiếp tục sử dụng ứng dụng",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onUnlockClick,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.Fingerprint, null)
                Spacer(Modifier.size(8.dp))
                Text("Mở khóa bằng Vân tay / Face ID", fontWeight = FontWeight.Bold)
            }
        }
    }
}
