package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.CryptoManager
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SafePrimary
import com.example.ui.theme.SafeRose

@Composable
fun PasswordGeneratorDialog(
  onDismiss: () -> Unit,
  onCopy: (String) -> Unit
) {
  var length by remember { mutableFloatStateOf(16f) }
  var incUpper by remember { mutableStateOf(true) }
  var incLower by remember { mutableStateOf(true) }
  var incDigits by remember { mutableStateOf(true) }
  var incSymbols by remember { mutableStateOf(true) }

  var generatedPassword by remember(length, incUpper, incLower, incDigits, incSymbols) {
    mutableStateOf(
      CryptoManager.generateSecurePassword(
        length = length.toInt(),
        includeUpper = incUpper,
        includeLower = incLower,
        includeDigits = incDigits,
        includeSymbols = incSymbols
      )
    )
  }

  fun regenerate() {
    generatedPassword = CryptoManager.generateSecurePassword(
      length = length.toInt(),
      includeUpper = incUpper,
      includeLower = incLower,
      includeDigits = incDigits,
      includeSymbols = incSymbols
    )
  }

  val strength = CryptoManager.evaluatePasswordStrength(generatedPassword)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Generador Criptográfico",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Display generated password in high-contrast box
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(
              1.dp,
              MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
              RoundedCornerShape(12.dp)
            )
            .padding(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = generatedPassword,
              fontFamily = FontFamily.Monospace,
              fontSize = 17.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { regenerate() }) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Regenerar contraseña",
                tint = SafePrimary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Strength indicator
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Robustez: ${strength.level.label}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = when (strength.level.score) {
              1, 2 -> SafeRose
              3 -> SafePrimary
              else -> SafeEmerald
            }
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Length slider
        Text(
          text = "Longitud: ${length.toInt()} caracteres",
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium
        )
        Slider(
          value = length,
          onValueChange = { length = it },
          valueRange = 8f..32f,
          steps = 23
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Checkbox options
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(checked = incUpper, onCheckedChange = { incUpper = it })
          Text("Mayúsculas (A-Z)", fontSize = 13.sp)
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(checked = incLower, onCheckedChange = { incLower = it })
          Text("Minúsculas (a-z)", fontSize = 13.sp)
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(checked = incDigits, onCheckedChange = { incDigits = it })
          Text("Números (0-9)", fontSize = 13.sp)
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(checked = incSymbols, onCheckedChange = { incSymbols = it })
          Text("Símbolos especiales (!@#$)", fontSize = 13.sp)
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onCopy(generatedPassword)
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = SafePrimary)
      ) {
        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Copiar Contraseña")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cerrar")
      }
    }
  )
}
