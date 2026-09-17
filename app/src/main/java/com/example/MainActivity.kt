package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.MainScreen
import com.example.ui.theme.KatkatTheme
import com.example.viewmodel.KatkatViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: KatkatViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      KatkatTheme {
        MainScreen(viewModel = viewModel)
      }
    }
  }
}


