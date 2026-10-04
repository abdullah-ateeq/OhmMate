package com.example.ohmmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ohmmate.ui.navigation.OhmMateNavHost
import com.example.ohmmate.ui.theme.OhmMateTheme
import com.example.ohmmate.ui.viewmodel.OhmMateViewModel
import com.example.ohmmate.ui.viewmodel.OhmMateViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: OhmMateViewModel = viewModel(
                factory = OhmMateViewModelFactory(applicationContext)
            )
            val settings by viewModel.settings.collectAsState()

            OhmMateTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor
            ) {
                OhmMateNavHost(viewModel = viewModel)
            }
        }
    }
}