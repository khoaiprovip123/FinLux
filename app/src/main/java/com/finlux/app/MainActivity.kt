package com.finlux.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.finlux.app.presentation.FinluxRoot
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow

/** Single-activity host. Every product screen is rendered by Compose navigation. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val destinationFlow = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent { FinluxRoot(destinationFlow = destinationFlow) }
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
    }
}
