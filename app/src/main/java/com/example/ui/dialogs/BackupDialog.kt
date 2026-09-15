package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SafePrimary

@Composable
fun BackupDialog(
  encryptedJson: String?,
  onDismiss: () -> Unit,
  onCopy: (String) -> Unit,
  onRestore: (String) -> Unit = {}
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  var restoreInput by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = if (selectedTab == 0) Icons.Default.FileDownload else Icons.Default.FileUpload,
          contentDescription = null,
          tint = SafePrimary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Copia de Seguridad Cifrada",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        TabRow(selectedTabIndex = selectedTab) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text("Exportar", fontSize = 13.sp) }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text("Restaurar", fontSize = 13.sp) }
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
          Text(
            text = "Tu copia contiene todos tus elementos cifrados con AES-256 de forma 100% local. Guárdala en un lugar seguro (unidad USB, archivo local).",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            lineHeight = 16.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(130.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
              .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
              )
              .padding(10.dp)
          ) {
            Text(
              text = encryptedJson ?: "No hay datos disponibles para exportar",
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp,
              maxLines = 7,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              tint = SafeEmerald,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Cero conocimiento: los secretos están cifrados.",
              fontSize = 11.sp,
              color = SafeEmerald,
              fontWeight = FontWeight.Medium
            )
          }
        } else {
          Text(
            text = "Pega aquí el contenido JSON cifrado de tu copia de seguridad previa para restaurar tus registros:",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            lineHeight = 16.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = restoreInput,
            onValueChange = { restoreInput = it },
            placeholder = { Text("Pega el JSON de copia de seguridad...", fontSize = 12.sp) },
            modifier = Modifier
              .fillMaxWidth()
              .height(130.dp),
            maxLines = 6,
            shape = RoundedCornerShape(8.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Shield,
              contentDescription = null,
              tint = SafePrimary,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Se verificará la autenticidad con tu PIN actual.",
              fontSize = 11.sp,
              color = SafePrimary,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }
    },
    confirmButton = {
      if (selectedTab == 0) {
        Button(
          onClick = {
            if (encryptedJson != null) onCopy(encryptedJson)
          },
          enabled = encryptedJson != null,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = SafePrimary)
        ) {
          Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Copiar Copia", fontSize = 13.sp)
        }
      } else {
        Button(
          onClick = {
            if (restoreInput.isNotBlank()) onRestore(restoreInput)
          },
          enabled = restoreInput.isNotBlank(),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = SafePrimary)
        ) {
          Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Restaurar Bóveda", fontSize = 13.sp)
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cerrar")
      }
    }
  )
}
