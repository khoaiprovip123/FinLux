package com.finlux.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.finlux.app.presentation.FinluxRoot
import dagger.hilt.android.AndroidEntryPoint

/** Single-activity host. Every product screen is rendered by Compose navigation. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { FinluxRoot() }
    }
}
