package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VaultUiState
import com.example.ui.theme.SafeAmber
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SafePrimary
import com.example.ui.theme.SafeRose

@Composable
fun LockScreen(
  state: VaultUiState,
  onDigit: (String) -> Unit,
  onDelete: () -> Unit,
  onClear: () -> Unit,
  onSubmit: () -> Unit,
  onBiometricUnlock: () -> Unit = {},
  onDemoBypass: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isLockedOut = state.lockoutSecondsRemaining > 0

  Surface(
    modifier = modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp, vertical = 18.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Header & Icon
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 20.dp)
      ) {
        Box(
          modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(
              if (isLockedOut) SafeRose.copy(alpha = 0.15f)
              else SafePrimary.copy(alpha = 0.12f)
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = when {
              isLockedOut -> Icons.Default.Timer
              state.isSetup -> Icons.Default.Lock
              else -> Icons.Default.Shield
            },
            contentDescription = "SafeBox Lock",
            tint = if (isLockedOut) SafeRose else SafePrimary,
            modifier = Modifier.size(38.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "SafeBox 2.0",
          fontSize = 28.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        val titleText = when {
          isLockedOut -> "Bóveda bloqueada por seguridad"
          !state.isSetup && !state.isConfirmingPin -> "Crea tu PIN Maestro seguro"
          !state.isSetup && state.isConfirmingPin -> "Confirma tu PIN Maestro"
          else -> "🔐 Bóveda segura"
        }

        Text(
          text = titleText,
          fontSize = 15.sp,
          color = if (isLockedOut) SafeRose else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
          textAlign = TextAlign.Center,
          fontWeight = if (isLockedOut) FontWeight.SemiBold else FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lockout banner
        if (isLockedOut) {
          Surface(
            color = SafeRose.copy(alpha = 0.12f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = SafeRose,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Espera ${state.lockoutSecondsRemaining}s para reintentar",
                color = SafeRose,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        // PIN indicator dots
        Row(
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(top = 10.dp)
        ) {
          val dotCount = 4
          for (i in 0 until dotCount) {
            val isFilled = i < state.pinInput.length
            Box(
              modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(
                  if (isFilled) SafePrimary else Color.Transparent
                )
                .border(
                  width = 2.dp,
                  color = if (isFilled) SafePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                  shape = CircleShape
                )
            )
          }
        }

        // Error message
        AnimatedVisibility(
          visible = state.errorMessage != null && !isLockedOut,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          Text(
            text = state.errorMessage ?: "",
            color = SafeRose,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 10.dp),
            textAlign = TextAlign.Center
          )
        }
      }

      // Numeric Keypad
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 8.dp)
      ) {
        val rows = listOf(
          listOf("1", "2", "3"),
          listOf("4", "5", "6"),
          listOf("7", "8", "9"),
          listOf("C", "0", "DEL")
        )

        for (row in rows) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            for (item in row) {
              KeypadButton(
                text = item,
                enabled = !isLockedOut,
                onClick = {
                  when (item) {
                    "C" -> onClear()
                    "DEL" -> onDelete()
                    else -> onDigit(item)
                  }
                }
              )
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Confirm button for setup or unlock
        if (!state.isSetup || state.pinInput.length >= 4) {
          Button(
            onClick = onSubmit,
            enabled = !isLockedOut,
            modifier = Modifier
              .fillMaxWidth(0.85f)
              .height(46.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SafePrimary)
          ) {
            Text(
              text = if (!state.isSetup) "Continuar" else "Desbloquear",
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
        }

        // Biometric / Quick unlock
        Row(
          modifier = Modifier.fillMaxWidth(0.85f),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = onBiometricUnlock,
            enabled = !isLockedOut,
            modifier = Modifier
              .weight(1f)
              .height(44.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )
          ) {
            Icon(
              imageVector = Icons.Default.Fingerprint,
              contentDescription = "Desbloqueo biométrico",
              modifier = Modifier.size(19.dp),
              tint = SafeEmerald
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Huella",
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium
            )
          }

          OutlinedButton(
            onClick = onDemoBypass,
            modifier = Modifier
              .weight(1f)
              .height(44.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
          ) {
            Text(
              text = "Demo (1234)",
              fontSize = 13.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Security badge footer
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = SafeEmerald,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Cifrado local AES-256 • Cero conocimiento",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
          )
        }
      }
    }
  }
}

@Composable
private fun KeypadButton(
  text: String,
  enabled: Boolean,
  onClick: () -> Unit
) {
  val isSpecial = text == "C" || text == "DEL"
  Box(
    modifier = Modifier
      .size(66.dp)
      .clip(CircleShape)
      .background(
        if (isSpecial) Color.Transparent
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (enabled) 0.45f else 0.15f)
      )
      .clickable(enabled = enabled, onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    if (text == "DEL") {
      Icon(
        imageVector = Icons.Default.Backspace,
        contentDescription = "Borrar dígito",
        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.75f else 0.25f),
        modifier = Modifier.size(22.dp)
      )
    } else {
      Text(
        text = text,
        fontSize = if (isSpecial) 16.sp else 23.sp,
        fontWeight = if (isSpecial) FontWeight.Medium else FontWeight.SemiBold,
        color = when {
          !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
          isSpecial -> SafeRose
          else -> MaterialTheme.colorScheme.onSurface
        }
      )
    }
  }
}
