package com.finlux.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.finlux.app.presentation.FinluxRoot
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow

/** Single-activity host. Every product screen is rendered by Compose navigation. */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val destinationFlow = MutableStateFlow<String?>(null)
    private val payNotificationIdFlow = MutableStateFlow<String?>(null)

    private val initialPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // Permissions result received.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestInitialPermissionsOnFirstLaunch()
        handleIntent(intent)
        setContent {
            FinluxRoot(
                activity = this,
                destinationFlow = destinationFlow,
                payNotificationIdFlow = payNotificationIdFlow,
            )
        }
    }

    private fun requestInitialPermissionsOnFirstLaunch() {
        val prefs = getSharedPreferences("finlux_app_prefs", Context.MODE_PRIVATE)
        val hasRequested = prefs.getBoolean("has_requested_initial_permissions", false)
        if (!hasRequested) {
            prefs.edit().putBoolean("has_requested_initial_permissions", true).apply()
            val permissions = buildList {
                add(Manifest.permission.CAMERA)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.POST_NOTIFICATIONS)
                    add(Manifest.permission.READ_MEDIA_IMAGES)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                    }
                } else {
                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            val ungranted = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            if (ungranted.isNotEmpty()) {
                initialPermissionsLauncher.launch(ungranted.toTypedArray())
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val dest = intent?.getStringExtra("destination")
        if (!dest.isNullOrBlank()) {
            destinationFlow.value = dest
        }
        val payId = intent?.getStringExtra("pay_notification_id")
        if (!payId.isNullOrBlank()) {
            payNotificationIdFlow.value = payId
        }
    }
}
