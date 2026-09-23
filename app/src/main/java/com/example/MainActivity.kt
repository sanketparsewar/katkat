package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.MainScreen
import com.example.ui.theme.KatkatTheme
import com.example.viewmodel.KatkatViewModel

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {

  private val viewModel: KatkatViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
      KatkatTheme(themeMode = themeMode) {
        MainScreen(viewModel = viewModel)
      }
    }
  }

  override fun onStart() {
    super.onStart()
    try {
      com.example.service.KatkatNotificationSyncService.startService(this)
    } catch (e: Exception) {
      android.util.Log.w("MainActivity", "Notice launching sync service: ${e.message}")
    }
  }
}


