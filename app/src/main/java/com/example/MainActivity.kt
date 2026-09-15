package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.VaultViewModel
import com.example.ui.screens.LockScreen
import com.example.ui.screens.VaultHomeScreen
import com.example.ui.theme.SafeBoxTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      SafeBoxTheme {
        val vaultViewModel: VaultViewModel = viewModel()
        val state by vaultViewModel.uiState.collectAsState()

        Surface(modifier = Modifier.fillMaxSize()) {
          Crossfade(targetState = state.isUnlocked, label = "VaultCrossfade") { unlocked ->
            if (unlocked) {
              VaultHomeScreen(viewModel = vaultViewModel)
            } else {
              LockScreen(
                state = state,
                onDigit = { vaultViewModel.onPinDigit(it) },
                onDelete = { vaultViewModel.onPinDelete() },
                onClear = { vaultViewModel.onPinClear() },
                onSubmit = {
                  if (!state.isSetup) vaultViewModel.submitSetupPin()
                  else vaultViewModel.submitUnlock()
                },
                onDemoBypass = { vaultViewModel.unlockQuickDemo() }
              )
            }
          }
        }
      }
    }
  }
}

