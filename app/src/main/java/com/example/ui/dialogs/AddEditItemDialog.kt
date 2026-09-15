package com.example.ui.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VaultCategory
import com.example.data.model.VaultEntry
import com.example.security.CryptoManager
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SafePrimary
import com.example.ui.theme.SafeRose
import org.json.JSONObject
import java.util.UUID

@Composable
fun AddEditItemDialog(
  initialItem: VaultEntry?,
  onDismiss: () -> Unit,
  onSave: (VaultEntry) -> Unit,
  onDelete: ((String) -> Unit)? = null
) {
  var selectedCategory by remember {
    mutableStateOf(initialItem?.type ?: VaultCategory.PASSWORD)
  }
  var title by remember { mutableStateOf(initialItem?.title ?: "") }
  var folder by remember { mutableStateOf(initialItem?.folder ?: "General") }

  // Password fields
  var username by remember {
    val json = parseJson(initialItem?.payloadJson)
    mutableStateOf(json.optString("username", ""))
  }
  var password by remember {
    val json = parseJson(initialItem?.payloadJson)
    mutableStateOf(json.optString("password", ""))
  }
  var url by remember {
    val json = parseJson(initialItem?.payloadJson)
    mutableStateOf(json.optString("url", ""))
  }
  var passwordNotes by remember {
    val json = parseJson(initialItem?.payloadJson)
    mutableStateOf(json.optString("notes", ""))
  }
  var showPasswordText by remember { mutableStateOf(false) }

  // Note fields
  var noteContent by remember {
    val json = parseJson(initialItem?.payloadJson)
    mutableStateOf(json.optString("content", ""))
  }
  var noteTag by remember {
    val json = parseJson(initialItem?.payloadJson)
    mutableStateOf(json.optString("tag", "Privado"))
  }

  // Card fields
  var cardholderName by remember {
    val json = parseJson(initialItem?.payloadJson)
    mutableStateOf(json.optString("cardholderName", ""))
  }
  var cardNumber by remember {
    val json = parseJson(initialItem?.payloadJson)
    mutableStateOf(json.optString("cardNumber", ""))
  }
  var expiryMonth by remember {
    val json = parseJson(initialItem?.payloadJson)
    mutableStateOf(json.optString("expiryMonth", "12"))
  }
  var expiryYear by remember {
    val json = parseJson(initialItem?.payloadJson)
    mutableStateOf(json.optString("expiryYear", "28"))
  }
  var cvv by remember {
    val json = parseJson(initialItem?.payloadJson)
    mutableStateOf(json.optString("cvv", ""))
  }
  var bank by remember {
    val json = parseJson(initialItem?.payloadJson)
    mutableStateOf(json.optString("bank", ""))
  }

  // Identity fields
  var idType by remember {
    val json = parseJson(initialItem?.payloadJson)
    mutableStateOf(json.optString("idType", "DNI / NIE"))
  }
  var idNumber by remember {
    val json = parseJson(initialItem?.payloadJson)
    mutableStateOf(json.optString("idNumber", ""))
  }
  var fullName by remember {
    val json = parseJson(initialItem?.payloadJson)
    mutableStateOf(json.optString("fullName", ""))
  }
  var country by remember {
    val json = parseJson(initialItem?.payloadJson)
    mutableStateOf(json.optString("country", ""))
  }

  var isConfirmingDelete by remember { mutableStateOf(false) }

  if (isConfirmingDelete && initialItem != null && onDelete != null) {
    AlertDialog(
      onDismissRequest = { isConfirmingDelete = false },
      title = { Text("¿Eliminar elemento?") },
      text = { Text("Esta acción es irreversible y eliminará el elemento de tu bóveda segura.") },
      confirmButton = {
        Button(
          onClick = {
            onDelete(initialItem.id)
            isConfirmingDelete = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = SafeRose)
        ) {
          Text("Eliminar")
        }
      },
      dismissButton = {
        TextButton(onClick = { isConfirmingDelete = false }) {
          Text("Cancelar")
        }
      }
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (initialItem == null) "Nuevo Elemento Seguro" else "Editar Elemento",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(vertical = 4.dp)
      ) {
        // Category Selector tabs if creating new
        if (initialItem == null) {
          TabRow(
            selectedTabIndex = selectedCategory.ordinal,
            modifier = Modifier.fillMaxWidth()
          ) {
            VaultCategory.values().forEach { category ->
              Tab(
                selected = selectedCategory == category,
                onClick = { selectedCategory = category },
                text = {
                  Text(
                    text = when (category) {
                      VaultCategory.PASSWORD -> "Cuenta"
                      VaultCategory.NOTE -> "Nota"
                      VaultCategory.CARD -> "Tarjeta"
                      VaultCategory.IDENTITY -> "ID"
                    },
                    fontSize = 13.sp
                  )
                }
              )
            }
          }
          Spacer(modifier = Modifier.height(14.dp))
        }

        // Common title
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Título (ej. Gmail, Router WiFi, Visa)") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Folder
        OutlinedTextField(
          value = folder,
          onValueChange = { folder = it },
          label = { Text("Carpeta / Categoría (ej. Personal, Trabajo)") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Dynamic fields per category
        when (selectedCategory) {
          VaultCategory.PASSWORD -> {
            OutlinedTextField(
              value = username,
              onValueChange = { username = it },
              label = { Text("Usuario / Correo") },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = password,
              onValueChange = { password = it },
              label = { Text("Contraseña secreta") },
              modifier = Modifier.fillMaxWidth(),
              visualTransformation = if (showPasswordText) VisualTransformation.None else PasswordVisualTransformation(),
              trailingIcon = {
                Row {
                  IconButton(onClick = {
                    password = CryptoManager.generateSecurePassword(16)
                  }) {
                    Icon(
                      imageVector = Icons.Default.AutoAwesome,
                      contentDescription = "Generar contraseña segura",
                      tint = SafeEmerald
                    )
                  }
                  IconButton(onClick = { showPasswordText = !showPasswordText }) {
                    Icon(
                      imageVector = if (showPasswordText) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                      contentDescription = "Mostrar/Ocultar contraseña"
                    )
                  }
                }
              },
              singleLine = true
            )

            // Password strength preview
            if (password.isNotEmpty()) {
              val strength = CryptoManager.evaluatePasswordStrength(password)
              Text(
                text = "Seguridad: ${strength.level.label} • ${strength.feedback}",
                fontSize = 11.sp,
                color = when (strength.level.score) {
                  1, 2 -> SafeRose
                  3 -> SafePrimary
                  else -> SafeEmerald
                },
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = url,
              onValueChange = { url = it },
              label = { Text("URL del sitio web (opcional)") },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = passwordNotes,
              onValueChange = { passwordNotes = it },
              label = { Text("Notas adicionales") },
              modifier = Modifier.fillMaxWidth(),
              maxLines = 3
            )
          }

          VaultCategory.NOTE -> {
            OutlinedTextField(
              value = noteContent,
              onValueChange = { noteContent = it },
              label = { Text("Contenido confidencial") },
              modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
              maxLines = 8
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = noteTag,
              onValueChange = { noteTag = it },
              label = { Text("Etiqueta (ej. Llaves, Diario, PIN)") },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true
            )
          }

          VaultCategory.CARD -> {
            OutlinedTextField(
              value = cardholderName,
              onValueChange = { cardholderName = it },
              label = { Text("Nombre del titular") },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = cardNumber,
              onValueChange = { cardNumber = it.filter { c -> c.isDigit() }.take(16) },
              label = { Text("Número de tarjeta (16 dígitos)") },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = expiryMonth,
                onValueChange = { expiryMonth = it.take(2) },
                label = { Text("Mes (MM)") },
                modifier = Modifier.weight(1f),
                singleLine = true
              )
              OutlinedTextField(
                value = expiryYear,
                onValueChange = { expiryYear = it.take(2) },
                label = { Text("Año (AA)") },
                modifier = Modifier.weight(1f),
                singleLine = true
              )
              OutlinedTextField(
                value = cvv,
                onValueChange = { cvv = it.take(4) },
                label = { Text("CVV") },
                modifier = Modifier.weight(1f),
                singleLine = true
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = bank,
              onValueChange = { bank = it },
              label = { Text("Entidad bancaria (ej. Santander, BBVA)") },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true
            )
          }

          VaultCategory.IDENTITY -> {
            OutlinedTextField(
              value = idType,
              onValueChange = { idType = it },
              label = { Text("Tipo (DNI, Pasaporte, Licencia conducir)") },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = idNumber,
              onValueChange = { idNumber = it },
              label = { Text("Número de documento") },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = fullName,
              onValueChange = { fullName = it },
              label = { Text("Nombre completo registrado") },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = country,
              onValueChange = { country = it },
              label = { Text("País emisor") },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isBlank()) return@Button
          val payload = JSONObject().apply {
            when (selectedCategory) {
              VaultCategory.PASSWORD -> {
                put("username", username)
                put("password", password)
                put("url", url)
                put("notes", passwordNotes)
              }
              VaultCategory.NOTE -> {
                put("content", noteContent)
                put("tag", noteTag)
              }
              VaultCategory.CARD -> {
                put("cardholderName", cardholderName)
                put("cardNumber", cardNumber)
                put("expiryMonth", expiryMonth)
                put("expiryYear", expiryYear)
                put("cvv", cvv)
                put("bank", bank)
              }
              VaultCategory.IDENTITY -> {
                put("idType", idType)
                put("idNumber", idNumber)
                put("fullName", fullName)
                put("country", country)
              }
            }
          }

          val finalEntry = VaultEntry(
            id = initialItem?.id ?: UUID.randomUUID().toString(),
            type = selectedCategory,
            title = title,
            folder = folder,
            isFavorite = initialItem?.isFavorite ?: false,
            updatedAt = System.currentTimeMillis(),
            payloadJson = payload.toString()
          )
          onSave(finalEntry)
        },
        enabled = title.isNotBlank()
      ) {
        Text("Guardar Cifrado")
      }
    },
    dismissButton = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (initialItem != null && onDelete != null) {
          IconButton(onClick = { isConfirmingDelete = true }) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Eliminar",
              tint = SafeRose
            )
          }
        }
        TextButton(onClick = onDismiss) {
          Text("Cancelar")
        }
      }
    }
  )
}

private fun parseJson(raw: String?): JSONObject {
  return try {
    if (raw.isNullOrBlank()) JSONObject() else JSONObject(raw)
  } catch (e: Exception) {
    JSONObject()
  }
}
