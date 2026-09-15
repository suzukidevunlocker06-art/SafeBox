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
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SafeNavyDark
import com.example.ui.theme.SafePrimary
import com.example.ui.theme.SafeRose

@Composable
fun LockScreen(
  state: VaultUiState,
  onDigit: (String) -> Unit,
  onDelete: () -> Unit,
  onClear: () -> Unit,
  onSubmit: () -> Unit,
  onDemoBypass: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Header & Icon
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 28.dp)
      ) {
        Box(
          modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(SafePrimary.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (state.isSetup) Icons.Default.Lock else Icons.Default.Shield,
            contentDescription = "SafeBox Lock",
            tint = SafePrimary,
            modifier = Modifier.size(38.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "SafeBox",
          fontSize = 28.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        val titleText = when {
          !state.isSetup && !state.isConfirmingPin -> "Crea tu PIN Maestro de seguridad"
          !state.isSetup && state.isConfirmingPin -> "Confirma tu PIN Maestro"
          else -> "Bóveda Bloqueada"
        }

        Text(
          text = titleText,
          fontSize = 15.sp,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // PIN indicator dots
        Row(
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          verticalAlignment = Alignment.CenterVertically
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
          visible = state.errorMessage != null,
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
        modifier = Modifier.padding(bottom = 12.dp)
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
          Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Confirm button for setup or unlock
        if (!state.isSetup || state.pinInput.length >= 4) {
          Button(
            onClick = onSubmit,
            modifier = Modifier
              .fillMaxWidth(0.82f)
              .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SafePrimary)
          ) {
            Text(
              text = if (!state.isSetup) "Continuar" else "Desbloquear",
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
        }

        // Quick demo unlock / Biometric option
        OutlinedButton(
          onClick = onDemoBypass,
          modifier = Modifier
            .fillMaxWidth(0.82f)
            .height(44.dp),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
          )
        ) {
          Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = "Desbloqueo biométrico o rápido",
            modifier = Modifier.size(18.dp),
            tint = SafeEmerald
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Acceso Rápido / Huella",
            fontSize = 14.sp
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Security badge footer
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = SafeEmerald,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Cifrado local AES-256 de conocimiento cero",
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
  onClick: () -> Unit
) {
  val isSpecial = text == "C" || text == "DEL"
  Box(
    modifier = Modifier
      .size(68.dp)
      .clip(CircleShape)
      .background(
        if (isSpecial) Color.Transparent
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
      )
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    if (text == "DEL") {
      Icon(
        imageVector = Icons.Default.Backspace,
        contentDescription = "Borrar dígito",
        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        modifier = Modifier.size(22.dp)
      )
    } else {
      Text(
        text = text,
        fontSize = if (isSpecial) 17.sp else 23.sp,
        fontWeight = if (isSpecial) FontWeight.Medium else FontWeight.SemiBold,
        color = if (isSpecial) SafeRose else MaterialTheme.colorScheme.onSurface
      )
    }
  }
}
