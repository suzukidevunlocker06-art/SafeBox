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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
  onCopy: (String) -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.FileDownload,
          contentDescription = null,
          tint = SafePrimary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Copia de Seguridad Cifrada",
          fontSize = 19.sp,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Tu copia de seguridad contiene todos los registros cifrados con tu clave AES-256 derivada del PIN Maestro.",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
          lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .border(
              1.dp,
              MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
              RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
        ) {
          Text(
            text = encryptedJson ?: "No hay datos disponibles",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            maxLines = 6,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = SafeEmerald,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Totalmente seguro: Nadie sin tu PIN puede leer este archivo.",
            fontSize = 11.sp,
            color = SafeEmerald,
            fontWeight = FontWeight.Medium
          )
        }
      }
    },
    confirmButton = {
      if (encryptedJson != null) {
        Button(onClick = {
          onCopy(encryptedJson)
          onDismiss()
        }) {
          Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Copiar Cifrado")
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
