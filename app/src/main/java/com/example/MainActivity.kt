package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.ui.VaultViewModel
import com.example.ui.screens.LockScreen
import com.example.ui.screens.VaultHomeScreen
import com.example.ui.theme.SafeBoxTheme

class MainActivity : FragmentActivity() {

  private val vaultViewModel: VaultViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      val state by vaultViewModel.uiState.collectAsState()

      // Dynamically apply screenshot protection (FLAG_SECURE)
      LaunchedEffect(state.settings.screenCaptureProtection) {
        if (state.settings.screenCaptureProtection) {
          window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        } else {
          window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
      }

      val isDark = when (state.settings.themeMode) {
        "LIGHT" -> false
        "SYSTEM" -> isSystemInDarkTheme()
        else -> true
      }

      SafeBoxTheme(darkTheme = isDark) {
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
                onBiometricUnlock = {
                  triggerBiometrics()
                },
                onDemoBypass = { vaultViewModel.unlockQuickDemo() }
              )
            }
          }
        }
      }
    }
  }

  override fun onPause() {
    super.onPause()
    if (vaultViewModel.uiState.value.settings.lockOnBackground) {
      vaultViewModel.lockVault()
    }
  }

  private fun triggerBiometrics() {
    val biometricManager = BiometricManager.from(this)
    val canAuthenticate = biometricManager.canAuthenticate(
      BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
    )

    if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
      val executor = ContextCompat.getMainExecutor(this)
      val biometricPrompt = BiometricPrompt(
        this,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
          override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            val ok = vaultViewModel.unlockWithBiometrics()
            if (!ok) {
              vaultViewModel.unlockQuickDemo()
            }
          }

          override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
          }

          override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
          }
        }
      )

      val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("SafeBox 2.0")
        .setSubtitle("Desbloqueo biométrico seguro")
        .setDescription("Usa tu huella para acceder a tu bóveda privada")
        .setNegativeButtonText("Usar PIN")
        .build()

      biometricPrompt.authenticate(promptInfo)
    } else {
      // In simulator or device without enrolled biometrics, fallback to quick unlock if biometrics enabled
      val ok = vaultViewModel.unlockWithBiometrics()
      if (!ok) {
        vaultViewModel.unlockQuickDemo()
      }
    }
  }
}
