package com.example.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SafePrimary
import com.example.ui.theme.SafeRose

@Composable
fun ChangePinDialog(
  onDismiss: () -> Unit,
  onChangePin: (oldPin: String, newPin: String) -> Boolean
) {
  var oldPin by remember { mutableStateOf("") }
  var newPin by remember { mutableStateOf("") }
  var confirmNewPin by remember { mutableStateOf("") }
  var errorMsg by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.LockReset,
          contentDescription = null,
          tint = SafePrimary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Cambiar PIN Maestro",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Al cambiar tu PIN, todos tus datos y archivos se recifrarán de inmediato con la nueva clave derivada.",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
          lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = oldPin,
          onValueChange = { oldPin = it; errorMsg = null },
          label = { Text("PIN Actual") },
          singleLine = true,
          visualTransformation = PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = newPin,
          onValueChange = { newPin = it; errorMsg = null },
          label = { Text("Nuevo PIN (mínimo 4 dígitos)") },
          singleLine = true,
          visualTransformation = PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = confirmNewPin,
          onValueChange = { confirmNewPin = it; errorMsg = null },
          label = { Text("Confirmar Nuevo PIN") },
          singleLine = true,
          visualTransformation = PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        )

        if (errorMsg != null) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = errorMsg ?: "",
            color = SafeRose,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (oldPin.isBlank() || newPin.isBlank() || confirmNewPin.isBlank()) {
            errorMsg = "Completa todos los campos"
            return@Button
          }
          if (newPin.length < 4) {
            errorMsg = "El nuevo PIN debe tener al menos 4 dígitos"
            return@Button
          }
          if (newPin != confirmNewPin) {
            errorMsg = "Los nuevos PINs no coinciden"
            return@Button
          }
          val ok = onChangePin(oldPin, newPin)
          if (!ok) {
            errorMsg = "El PIN actual no es correcto"
          }
        },
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SafePrimary)
      ) {
        Text("Actualizar PIN")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancelar")
      }
    }
  )
}
